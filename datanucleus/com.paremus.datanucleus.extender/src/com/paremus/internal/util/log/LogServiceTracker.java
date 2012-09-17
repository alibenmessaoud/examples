package com.paremus.internal.util.log;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class LogServiceTracker extends ServiceTracker implements LogService {
	
	public LogServiceTracker(BundleContext context) {
		super(context, LogService.class.getName(), null);
	}

	public void log(int level, String message) {
		log(null, level, message, null);
	}

	public void log(int level, String message, Throwable exception) {
		log(null, level, message, exception);
	}

	public void log(ServiceReference sr, int level, String message) {
		log(sr, level, message, null);
	}

	public void log(ServiceReference sr, int level, String message, Throwable exception) {
		LogService service = (LogService) getService();
		if(service != null) {
			service.log(sr, level, message, exception);
		} else {
			System.err.println(levelToString(level) + " " + message);
			if(exception != null) {
				exception.printStackTrace(System.err);
			}
		}
	}
	
	private String levelToString(int level) {
		String result;
		switch (level) {
		case LogService.LOG_DEBUG:
			result = "DEBUG";
			break;
		case LogService.LOG_INFO:
			result = "INFO";
			break;
		case LogService.LOG_WARNING:
			result = "WARNING";
			break;
		case LogService.LOG_ERROR:
			result = "ERROR";
			break;
		default:
			result = "UNKNOWN";
			break;
		}
		return result;
	}

}
