package com.paremus.demo.fractal.http;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.http.HttpContext;

/**
 * A simple handler that delivers static content, and redirects if the root path doesn't contain a trailing slash
 */
final class RedirectingStaticContentHandler implements HttpContext {
	/**
	 * Redirect any request that comes in for the root with no trailing /.
	 * If we don't do this then the css and javascript can't be found properly.
	 */
	@Override
	public boolean handleSecurity(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if(Bootstrap.ROOT_APP_PATH.equals(request.getRequestURI())) {
			response.sendRedirect(Bootstrap.ROOT_APP_PATH + "/");
			return false;
		}
		return true;
	}

	/**
	 * Default to serving the main page if the user requests the root application path.
	 * Otherwise give them what they ask for.
	 */
	@Override
	public URL getResource(String name) {
		if("static/".equals(name)) {
			name = "/static/index.html";
		}
		return FrameworkUtil.getBundle(Bootstrap.class).getEntry(name);
	}

	/**
	 * Use default mime types.
	 */
	@Override
	public String getMimeType(String name) {
		return null;
	}
}