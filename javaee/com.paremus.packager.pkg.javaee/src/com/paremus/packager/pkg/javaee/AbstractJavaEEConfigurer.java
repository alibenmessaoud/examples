package com.paremus.packager.pkg.javaee;

import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.framework.Bundle.RESOLVED;
import static org.osgi.framework.Bundle.STARTING;
import static org.osgi.framework.Bundle.STOPPING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.bndtools.service.endpoint.Endpoint;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;

import com.paremus.service.javaee.JavaEENamespace;
import com.paremus.service.javaee.JavaEEProperties;

public abstract class AbstractJavaEEConfigurer implements BundleTrackerCustomizer<ServiceTracker<Endpoint, Object>> {

	private final String factoryPID;
	private final BundleCapability javaEECapability;

	private BundleTracker<ServiceTracker<Endpoint, Object>> bundleTracker;
	private volatile BundleContext context;
	private volatile ConfigurationAdmin configAdmin;
	
	protected AbstractJavaEEConfigurer(String factoryPID) {
		this.factoryPID = factoryPID;
		javaEECapability = FrameworkUtil.getBundle(getClass()).adapt(BundleWiring.class).
			getCapabilities(JavaEENamespace.JAVA_EE_NAMESPACE).get(0);
	}
	
	protected synchronized void activate(BundleContext context, ConfigurationAdmin configAdmin) {
		this.context = context;
		this.configAdmin = configAdmin;		
		bundleTracker = new BundleTracker<ServiceTracker<Endpoint,Object>>(context, 
				RESOLVED | STARTING | ACTIVE | STOPPING, this);
		bundleTracker.open();
	}

	protected synchronized void deactivate() {
		bundleTracker.close();
	}
	
	
	@Override
	public ServiceTracker<Endpoint, Object> addingBundle(Bundle bundle,
			BundleEvent event) {
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		for(BundleRequirement br : wiring.getRequirements(JavaEENamespace.JAVA_EE_NAMESPACE)) {
			if(br.matches(javaEECapability)) {
				ServiceTracker<Endpoint, Object> listener = new EndpointTracker(context,
						wiring, br);
				listener.open();
				return listener;
			}
		}
		return null;
	}

	@Override
	public void modifiedBundle(Bundle bundle, BundleEvent event,
			ServiceTracker<Endpoint, Object> object) {}

	@Override
	public void removedBundle(Bundle bundle, BundleEvent event,
			ServiceTracker<Endpoint, Object> listener) {
		listener.close();
	}

	private class EndpointTracker extends ServiceTracker<Endpoint, Object> {

		private final List<RequirementMatch> requirementsToMatch;
		private final BundleRequirement javaEERequirement;
		
		private Configuration configuration;
		
		
		public EndpointTracker(BundleContext context, BundleWiring wiring, BundleRequirement javaEERequirement) {
			super(context, Endpoint.class, null);
			this.javaEERequirement = javaEERequirement;

			String dbFilter = new StringBuilder('(')
			.append(JavaEENamespace.ENDPOINT_TYPE_SERVICE_PROPERTY_NAME)
			.append('=').append(JavaEENamespace.ENDPOINT_TYPE_DATABASE)
			.append(')').toString();

			List<RequirementMatch> toMatch = new ArrayList<RequirementMatch>();
			for(BundleRequirement br : wiring.getRequirements(JavaEENamespace.JAVA_EE_DB_NAMESPACE)) {
				String requirementFilter = br.getDirectives().get(JavaEENamespace.REQUIREMENT_FILTER_DIRECTIVE);
				Filter filter;
				try {
					if(requirementFilter != null) {
						filter = FrameworkUtil.createFilter(new StringBuilder("(&")
							.append(dbFilter).append(requirementFilter).append(')')
							.toString());
					} else {
						filter = FrameworkUtil.createFilter(dbFilter);
					}
				} catch (InvalidSyntaxException ise) {
					throw new IllegalArgumentException(ise);
				}
				toMatch.add(new RequirementMatch(filter, JavaEENamespace.ENDPOINT_TYPE_DATABASE, 
						br.getAttributes()));
			}
			requirementsToMatch = Collections.unmodifiableList(toMatch);
		}

		@Override
		public void open(boolean trackAllServices) {
			if(requirementsToMatch.isEmpty()) {
				updateConfig();
			} else {
				super.open(trackAllServices);
			}
		}

		@Override
		public void close() {
			if(requirementsToMatch.isEmpty()) {
				deleteConfig();
			} else {
				super.close();
			}
		}

		@Override
		public synchronized Object addingService(ServiceReference<Endpoint> reference) {
			Object toTrack = null;
			//Hope for the best
			MatchState state = MatchState.MATCHED;
			for(RequirementMatch rm : requirementsToMatch) {
				MatchState tmp = rm.add(reference);
				//Make sure we track things that we might use
				if(toTrack == null && tmp.isUsed()) {
					toTrack = new Object();
				}
				state = state.keepScore(tmp);
			}
			handleState(state);
			return toTrack;
		}

		@Override
		public synchronized void modifiedService(ServiceReference<Endpoint> reference,
				Object service) {
			//Hope for the best
			MatchState state = MatchState.MATCHED;
			for(RequirementMatch rm : requirementsToMatch) {
				state = state.keepScore(rm.update(reference));
			}
			handleState(state);
		}

		@Override
		public synchronized void removedService(ServiceReference<Endpoint> reference,
				Object service) {
			//Hope for the best
			MatchState state = MatchState.MATCHED;
			for(RequirementMatch rm : requirementsToMatch) {
				state = state.keepScore(rm.remove(reference));
			}
			handleState(state);
		}

		private void handleState(MatchState state) {
			switch (state) {
				case CHANGED:
					updateConfig();
					break;
				case UNMATCHED :
				case UNMATCHED_NOT_USED :
					deleteConfig();
				default:
			}
		}

		private void deleteConfig() {
			if(configuration != null) {
				try {
					configuration.delete();
				} catch (IOException e) {
					throw new IllegalStateException("Problem with config admin. Discarding configuration", e);
				} finally {
					configuration = null;
				}
			}
		}

		private void updateConfig() {
			try {
				if(configuration == null) {
					configuration = configAdmin.createFactoryConfiguration(factoryPID);
				}
				Dictionary<String, Object> props = createStandardJavaEEProps();
				
				Map<String, Integer> prefixGeneratorMap = new HashMap<String, Integer>();
	
				for(RequirementMatch rm : requirementsToMatch) {
					String prefix = generatePrefix(rm.type, prefixGeneratorMap);
					for(String key : rm.currentMatch.getPropertyKeys()) {
						props.put(prefix + key, rm.currentMatch.getProperty(key));
					}
					for(Entry<String, Object> e : rm.attributes.entrySet()) {
						props.put(prefix + e.getKey(), e.getValue());
					}
				}
				configuration.update(props);
			} catch (IOException ioe) {
				throw new IllegalStateException("Problem with config admin.", ioe);
			}
		}

		private Dictionary<String, Object> createStandardJavaEEProps() {
			Dictionary<String, Object> props = new Hashtable<String, Object>();
			
			Bundle appBundle = javaEERequirement.getRevision().getBundle();
			
			props.put(JavaEEProperties.APP_SYMBOLIC_NAME, appBundle.getSymbolicName());
			props.put(JavaEEProperties.APP_VERSION, appBundle.getVersion().toString());
			props.put(JavaEEProperties.APP_BUNDLE_ID, appBundle.getBundleId());
			
			Object binariesPath = javaEERequirement.getAttributes().get(
					JavaEENamespace.PATH_TO_BINARIES_ATTRIBUTE);
			props.put(JavaEEProperties.PATH_TO_BINARIES, binariesPath == null ? 
					JavaEEProperties.DEFAULT_PATH_TO_BINARIES : binariesPath.toString());
			
			Object contextRoots = javaEERequirement.getAttributes().get(
					JavaEENamespace.CONTEXT_ROOTS_ATTRIBUTE);
			if(contextRoots != null) {
				String[] roots = contextRoots.toString().split(",");
				for (int i = 0; i < roots.length; i++) {
					roots[i] = roots[i].trim();
				}
				props.put(JavaEEProperties.CONTEXT_ROOTS, roots);
			}
			return props;
		}

		private String generatePrefix(String type, Map<String, Integer> prefixGeneratorMap) {
			Integer i = prefixGeneratorMap.get(type);
			if(i == null) {	i = 0;}
			
			prefixGeneratorMap.put(type, i + 1);
			
			return type + '.' + i + '.';
		}
		
	}
	
	private static class RequirementMatch {
		private ServiceReference<Endpoint> currentMatch;
		private SortedSet<ServiceReference<Endpoint>> allMatches;
		
		private final Filter filter;
		private final String type;
		private final Map<String, Object> attributes;
		
		public RequirementMatch(Filter filter, String type, Map<String, Object> attributes) {
			this.filter = filter;
			this.type = type;
			this.attributes = attributes;
		}
		
		public MatchState add(ServiceReference<Endpoint> ref) {
			if(filter.match(ref)) {
				allMatches.add(ref);
				if(currentMatch == null) {
					currentMatch = ref;
					return MatchState.CHANGED;
				} else {
					return MatchState.MATCHED;
				}
			} else {
				return notUsed();
			}
		}

		public MatchState update(ServiceReference<Endpoint> ref) {
			MatchState m = add(ref);
			if(!m.isUsed()) {
				return remove(ref);
			} 
			return m;
		}
		
		public MatchState remove(ServiceReference<Endpoint> ref) {
			
			if(allMatches.remove(ref)) {
				if(ref.equals(currentMatch)) {
					if(allMatches.isEmpty()) {
						currentMatch = null;
						return MatchState.UNMATCHED;
					} else {
						currentMatch = allMatches.first();
						return MatchState.CHANGED;
					}
				} else {
					return currentMatch == null ? MatchState.UNMATCHED : MatchState.MATCHED;
				}
			} else {
				return notUsed();
			}
		}

		private MatchState notUsed() {
			return currentMatch == null ? MatchState.UNMATCHED_NOT_USED : MatchState.MATCHED_NOT_USED;
		}
	}
	
	private enum MatchState {
		UNMATCHED_NOT_USED(false), UNMATCHED(true), CHANGED(true), MATCHED_NOT_USED(false), MATCHED(true);
		
		private final boolean used;
		
		private MatchState(boolean used) {
			this.used = used;
		}

		public boolean isUsed() {
			return used;
		}
		
		public MatchState keepScore(MatchState otherState) {
			return compareTo(otherState) < 0 ? this : otherState;
		}
	}
}