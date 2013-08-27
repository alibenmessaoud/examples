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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

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
					JedisPool pool = writableMap.get(getARandomJedis(writableMap));
					Jedis jedis = null;
					try {
						jedis = pool.getResource();
						initializeRedis(pool);
						int increment  = (int) (random.nextFloat() * 100.0 - 48.0);
						jedis.incrBy("stock." + TICKER_SYMBOLS[random.nextInt(TICKER_SYMBOLS.length)], 
								increment);
						pool.returnResource(jedis);
					} catch (RuntimeException re) {
						if(jedis != null) pool.returnBrokenResource(jedis);
					}
				}
			} catch (Exception e) {
				//Just swallow this
			} finally {
				lock.readLock().unlock();
			}
		}

		private void initializeRedis(JedisPool pool) {
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				for(String ticker : TICKER_SYMBOLS) {
					String key = "stock." + ticker;
					if(jedis.get(key + ".start") != null) continue;
					
					String value = String.valueOf((int)(1000.0 - 200.0 * random.nextFloat()));
					jedis.set(key, value);
					jedis.set(key + ".start", value);
				}
				pool.returnResource(jedis);
			} catch (RuntimeException re) {
				if(jedis != null)pool.returnBrokenResource(jedis);
			}
		}
	}

	private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	private final ConcurrentMap<ServiceReference<Endpoint>, JedisPool> writableMap = new ConcurrentHashMap<ServiceReference<Endpoint>, JedisPool>();
	private final ConcurrentMap<ServiceReference<Endpoint>, JedisPool> readableMap = new ConcurrentHashMap<ServiceReference<Endpoint>, JedisPool>();
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private ScheduledFuture<?> future;
	
	private final Random random = new Random();
	
	private static final String[] TICKER_SYMBOLS  = {"LLOY", "BARC", "VOD", "BSY", "HSBA", "GLEN", "BP", "OML", "ITV", "RBS"};
	
	@Activate
	void start(BundleContext context) {
		lock.writeLock().lock();
		try {
			future = executor.scheduleAtFixedRate(new RandomPriceChange(), 0, 500, TimeUnit.MILLISECONDS);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Deactivate
	void stop() throws InterruptedException, ExecutionException {
		lock.writeLock().lock();
		try {
			if(future != null) {
				future.cancel(false);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Reference(cardinality = ReferenceCardinality.OPTIONAL,
			policy = ReferencePolicy.DYNAMIC)
	void setHttpService(HttpService httpService) throws ServletException, NamespaceException {
		httpService.registerServlet("/paremus/demo/redis/rest", this, null, null);
		httpService.registerResources("/paremus/demo/redis", "/web", null);
	}

	void unsetHttpService(HttpService httpService) {
		httpService.unregister("/paremus/demo/redis/rest");
		httpService.unregister("/paremus/demo/redis");
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC,
			service = Endpoint.class,
			target = "(type=redis)")
	void setEndpoint(ServiceReference<Endpoint> endpoint) {
		lock.writeLock().lock();
		try {
			String uriString = endpoint.getProperty(Endpoint.URI).toString();
			URI uri = new URI(uriString);
			JedisPool pool = new JedisPool(uri.getHost(), uri.getPort());
			if("master".equals(endpoint.getProperty("node.type"))) {
				writableMap.put(endpoint, pool);
			} else if ("slave".equals(endpoint.getProperty("node.type"))) {
				readableMap.put(endpoint, pool);
			}
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	void updatedEndpoint(ServiceReference<Endpoint> endpoint) {
		lock.writeLock().lock();
		try {
			JedisPool oldPool = writableMap.remove(endpoint);
			if(oldPool == null) oldPool = readableMap.remove(endpoint);
			try {
				if(oldPool != null) oldPool.destroy();
			} catch (RuntimeException re){}
			
			setEndpoint(endpoint);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	void unsetEndpoint(ServiceReference<Endpoint> endpoint) {
		lock.writeLock().lock(); 
		try {
			JedisPool pool = readableMap.remove(endpoint);
			if(pool != null) pool.destroy();
			pool = writableMap.remove(endpoint);
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

	private Map<String, Double> getPrices(JedisPool pool, boolean openingPrices) {
		Map<String, Double> prices = new HashMap<String, Double>();

		Jedis jedis = null;
		try {
			jedis = pool.getResource();
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
			pool.returnResource(jedis);
		} catch (RuntimeException re) {
			if(jedis != null) pool.returnBrokenResource(jedis);
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
	private ServiceReference<Endpoint> getARandomJedis(Map<ServiceReference<Endpoint>, JedisPool> toUse) {
		Iterator<ServiceReference<Endpoint>> keyIt = toUse.keySet().iterator();
		int finishIndex = random.nextInt(toUse.size());
		int i = 0;
		ServiceReference<Endpoint> ref;
		do {
			ref = keyIt.next();
		} while(i++ < finishIndex);
		return ref;
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
			Map<ServiceReference<Endpoint>, JedisPool> toUse = new HashMap<ServiceReference<Endpoint>, JedisPool>(writableMap);
			toUse.putAll(readableMap);
			ServiceReference<Endpoint> ref = getARandomJedis(toUse);
			
			/* Get the prices */
			Map<String, Double> prices = getPrices(toUse.get(ref), openingPrices);
			while(prices == null && !toUse.isEmpty()) {
				//This Jedis didn't work!
				toUse.remove(ref);
				ref = getARandomJedis(toUse);
				prices = getPrices(toUse.get(ref), openingPrices);
			}
			/* Return the data */
			return (prices == null) ?  new Snapshot(Availability.NONE, null, Collections.<String, Double>emptyMap()) :
					new Snapshot(availability, (String)ref.getProperty(Endpoint.URI), prices);
		} finally {
			lock.readLock().unlock();
		}
	}
}