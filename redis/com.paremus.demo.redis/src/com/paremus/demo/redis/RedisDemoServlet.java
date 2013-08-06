package com.paremus.demo.redis;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bndtools.service.endpoint.Endpoint;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paremus.demo.redis.Snapshot.Availability;

@Component
public class RedisDemoServlet extends HttpServlet{

	private static final long serialVersionUID = 4797783096696829653L;

	private final class RandomPriceChange implements Runnable {
		@Override
		public void run() {
			lock.readLock().lock(); 
			try {
				if(!writableMap.isEmpty()) {
					JedisPool pool = writableMap.values().iterator().next();
					Jedis jedis = pool.getResource();
					try {
						int increment  = (int) (random.nextFloat() * 100.0 - 48.0);
						jedis.incrBy("stock." + TICKER_SYMBOLS[random.nextInt(TICKER_SYMBOLS.length)], 
								increment);
					} finally {
						pool.returnResource(jedis);
					}
				}
			} catch (Exception e) {
				//Just swallow this
			} finally {
				lock.readLock().unlock();
			}
		}
	}

	private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	private final ConcurrentMap<String, JedisPool> writableMap = new ConcurrentHashMap<String, JedisPool>();
	private final ConcurrentMap<String, JedisPool> readableMap = new ConcurrentHashMap<String, JedisPool>();
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private ServiceTracker<Endpoint, Endpoint> tracker;
	private ScheduledFuture<?> future;
	
	private final Random random = new Random();
	
	private boolean initializedRedis;
	
	private static final String[] TICKER_SYMBOLS  = {"LLOY", "BARC", "VOD", "BSY", "HSBA", "GLEN", "BP", "OML", "ITV", "RBS"};
	
	@Activate
	void start(BundleContext context) {
		lock.writeLock().lock();
		try {
			tracker = new ServiceTracker<Endpoint, Endpoint>(context, Endpoint.class, null) {

				@Override
				public Endpoint addingService(
						ServiceReference<Endpoint> reference) {
					if(!"redis".equals(reference.getProperty("type"))) {
						return null;
					}
					setEndpoint(reference);
					return super.addingService(reference);
				}

				@Override
				public void modifiedService(
						ServiceReference<Endpoint> reference, Endpoint service) {
					lock.writeLock().lock();
					try {
						unsetEndpoint(reference);
						setEndpoint(reference);
					} finally {
						lock.writeLock().unlock();
					}
					super.modifiedService(reference, service);
				}

				@Override
				public void removedService(
						ServiceReference<Endpoint> reference, Endpoint service) {
					unsetEndpoint(reference);
				}
			};
			
			tracker.open();
			future = executor.scheduleAtFixedRate(new RandomPriceChange(), 0, 500, TimeUnit.MILLISECONDS);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Deactivate
	void stop() throws InterruptedException, ExecutionException {
		lock.writeLock().lock();
		try {
			tracker.close();
			if(future != null) {
				future.cancel(false);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Reference(optional=true)
	void setHttpService(HttpService httpService) throws ServletException, NamespaceException {
		httpService.registerServlet("/paremus/demo/redis/rest", this, null, null);
		httpService.registerResources("/paremus/demo/redis", "/web", null);
	}

	void unsetHttpService(HttpService httpService) {
		httpService.unregister("/paremus/demo/redis/rest");
		httpService.unregister("/paremus/demo/redis");
	}
	
	private void setEndpoint(ServiceReference<Endpoint> endpoint) {
		lock.writeLock().lock();
		try {
			String uriString = endpoint.getProperty(Endpoint.URI).toString();
			URI uri = new URI(uriString);
			JedisPool pool = new JedisPool(uri.getHost(), uri.getPort());
			if("master".equals(endpoint.getProperty("node.type"))) {
				if(!initializedRedis) {
					initializeRedis(pool);
					initializedRedis = true;
				}
				pool = writableMap.put(uriString, pool);
			} else if ("slave".equals(endpoint.getProperty("node.type"))) {
				pool = readableMap.put(uriString, pool);
			}
			if(pool != null) pool.destroy();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	private void initializeRedis(JedisPool pool) {
		Jedis jedis = pool.getResource();
		try {
			for(String ticker : TICKER_SYMBOLS) {
				String key = "stock." + ticker;
				if(jedis.get(key) != null) continue;
				
				String value = String.valueOf((int)(1000.0 - 200.0 * random.nextFloat()));
				jedis.set(key, value);
				jedis.set(key + ".start", value);
			}
		} finally {
			pool.returnResource(jedis);
		}
	}

	private void unsetEndpoint(ServiceReference<Endpoint> endpoint) {
		lock.writeLock().lock(); 
		try {
			String uriString = endpoint.getProperty(Endpoint.URI).toString();
			JedisPool pool;
			
			pool = readableMap.remove(uriString);
			if(pool != null) pool.destroy();
			pool = writableMap.remove(uriString);
			if(pool != null) pool.destroy();
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("application/json; charset=utf-8");
		boolean openingPrices = Boolean.parseBoolean(req.getParameter("openingPrices"));
		
		new ObjectMapper().writeValue(resp.getWriter(), getSnapshot(openingPrices));
	}

	private Map<String, Double> getPrices(Map<String, JedisPool> toUse,
			String uri, boolean openingPrices) {
		Map<String, Double> prices = new HashMap<String, Double>();
		
		JedisPool pool = toUse.get(uri);
		Jedis jedis = pool.getResource();
		try {
			String[] keys = new String[TICKER_SYMBOLS.length];
			int i = 0;
			for(String symbol : TICKER_SYMBOLS) {
				symbol = (openingPrices) ? symbol + ".start" : symbol;
				keys[i++] = "stock." + symbol;
			}
			
			List<String> values = jedis.mget(keys);
			i = 0;
			for(String value : values) {
				if(value == null) {
					throw new IllegalStateException("Redis should contain a value for stock " + 
							TICKER_SYMBOLS[i++]);
				}
				prices.put(TICKER_SYMBOLS[i++], Double.valueOf(value));
			}
		} finally {
			pool.returnResource(jedis);
		}
		return prices;
	}

	/**
	 * Should only be called while holding a lock from {@link #lock}
	 * @return
	 */
	private Availability getCurrentAvailability() {
		Availability availability = Availability.NONE;
		if(!writableMap.isEmpty()) {
			availability = Availability.WRITE;
		} else if (!readableMap.isEmpty()) {
			availability = Availability.READ;
		}
		return availability;
	}

	/**
	 * Should only be called while holding a lock from {@link #lock}
	 * @return
	 */
	private String getARandomJedis(Map<String, JedisPool> toUse) {
		Iterator<String> keyIt = toUse.keySet().iterator();
		int finishIndex = random.nextInt(toUse.size());
		int i = 0;
		String uri;
		do {
			uri = keyIt.next();
		} while(i++ < finishIndex);
		return uri;
	}

	/**
	 * Should only be called while holding a lock from {@link #lock}
	 * @return
	 */
	private Snapshot getSnapshot(boolean openingPrices) {
		lock.readLock().lock();
		try {
			Availability availability = getCurrentAvailability();
			/* Nothing we can check */
			if(availability == Availability.NONE) {
				return new Snapshot(availability, null, Collections.<String, Double>emptyMap());
			}
			
			/* Pick a random Jedis to call */
			Map<String, JedisPool> toUse = new HashMap<String, JedisPool>(writableMap);
			toUse.putAll(readableMap);
			String uri = getARandomJedis(toUse);
			
			/* Get the prices */
			Map<String, Double> prices = getPrices(toUse, uri, openingPrices);
			/* Return the data */
			return new Snapshot(availability, uri, prices);
		} finally {
			lock.readLock().unlock();
		}
	}
}