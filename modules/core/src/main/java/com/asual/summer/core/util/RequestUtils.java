/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.WebUtils;

import com.asual.summer.core.RequestFilter;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Named
public class RequestUtils implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext;

	private static final String QUERY_STRING_SEPARATOR = "?";
	private static final String PARAMETER_SEPARATOR = "&";
	private static final String NAME_VALUE_SEPARATOR = "=";

	public static HttpServletRequest getRequest() {
		return RequestFilter.getRequest();
	}

	public static String getRequestUri() {
		String requestUri = (String) getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE);
		if (requestUri != null) {
			return requestUri;
		}
		return getRequest().getRequestURI();
	}

	public static String getQueryString() {
		String requestURI = (String) getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE);
		if (requestURI != null) {
			return (String) getAttribute(WebUtils.FORWARD_QUERY_STRING_ATTRIBUTE);
		}
		return getRequest().getQueryString();
	}
	
	public static String getUrl() {
		return getRequestUri() + (getQueryString() != null ? QUERY_STRING_SEPARATOR + getQueryString() : "");
	}
	
	public static UrlBuilder getUrlBuilder() {
		return getUrlBuilder(getUrl());
	}
	
	public static UrlBuilder getUrlBuilder(String url) {
		return new UrlBuilder(url);
	}
	
	public static Map<String, Object[]> getParameterMap() {
		Map<String, Object[]> normalized = new HashMap<String, Object[]>();
		Map<String, String[]> params = getRequest().getParameterMap();
		for (String key : params.keySet()) {
			String[] value = (String[]) params.get(key);
			Object[] result = new Object[value.length];
			for (int i = 0; i < value.length; i++) {
				result[i] = ObjectUtils.convert(value[i]);
			}
			normalized.put(key, result);
		}
		return normalized;
	}
	
	public static Object getParameter(String name) {
		if (getParameterMap().get(name) != null) {
			return getParameterMap().get(name)[0];
		}
		return null;
	}
	
	public static Object[] getParameterValues(String name) {
		return getParameterMap().get(name);
	}
	
	public static String getHeader(String name) {
		return getRequest().getHeader(name);
	}

	public static String getUserAgent() {
		return getHeader("User-Agent");
	}
	
	public static boolean isValidation() {
		return "Validation".equalsIgnoreCase(getHeader("X-Requested-Operation"));
	}
	
	public static boolean isAjaxRequest() {
		return "XMLHttpRequest".equalsIgnoreCase(getHeader("X-Requested-With"));
	}
	
	public static boolean isGetRequest() {
		return "GET".equalsIgnoreCase(getRequest().getMethod());
	}

	public static boolean isPostRequest() {
		return "POST".equalsIgnoreCase(getRequest().getMethod());
	}
	
	public static boolean isMethodBrowserSupported(String method) {
		return ("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method));
	}
	
	public static boolean isGecko() {
		String userAgent = getUserAgent();
		if (userAgent != null) {
			return Pattern.compile("Mozilla").matcher(userAgent).find() && !Pattern.compile("compatible|WebKit").matcher(userAgent).find();
		}
		return false;
	}
	
	public static boolean isTrident() {
		String userAgent = getUserAgent();
		if (userAgent != null) {
			return Pattern.compile("MSIE").matcher(userAgent).find() && !Pattern.compile("Opera").matcher(userAgent).find();
		}
		return false;
	}
	
	public static boolean isPresto() {
		String userAgent = getUserAgent();
		if (userAgent != null) {
			return Pattern.compile("Opera").matcher(userAgent).find();
		}
		return false;
	}
	
	public static boolean isWebKit() {
		String userAgent = getUserAgent();
		if (userAgent != null) {
			return Pattern.compile("WebKit").matcher(userAgent).find();
		}
		return false;
	}
	
	public static String getEngine() {
		if (isGecko()) {
			return "gecko";
		} else if (isTrident()) {
			return "trident";
		} else if (isPresto()) {
			return "presto";
		} else if (isWebKit()) {
			return "webkit";
		}
		return null;
	}
	
	public static void setAttribute(String name, Object value) {
		getRequest().setAttribute(name, value);
	}

	public static Object getAttribute(String name) {
		return getRequest().getAttribute(name);
	}
	
	public static String serializeParameters(Map<String, String[]> parameterMap) {
		List<String> pairs = new ArrayList<String>();
		for (String key : parameterMap.keySet()) {
			for (String value : parameterMap.get(key)) {
				pairs.add(key + NAME_VALUE_SEPARATOR + StringUtils.encode(value));
			}
		}
		return StringUtils.join(pairs, PARAMETER_SEPARATOR);
	}
	
	public static String contextRelative(String uri, boolean contextRelative) {
		if (uri != null && uri.startsWith("/")) {
			String contextPath = getRequest().getContextPath();
			uri = uri.replaceFirst("^" + contextPath + "/?", "/");
			if (contextRelative) {
				uri = contextPath.concat(uri);
			}
		}
		return uri;
	}

	public static Throwable getError() {
		return (Throwable) getAttribute("javax.servlet.error.exception");
	}

	public static int getErrorCode() {
		return (Integer) getAttribute("javax.servlet.error.status_code");
	}
	
	public static ServletContext getServletContext() {
		if (applicationContext instanceof WebApplicationContext) {
			return ((WebApplicationContext) applicationContext).getServletContext();
		}
		return null;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		RequestUtils.applicationContext = applicationContext;
	}
	
	private static class UrlBuilder {
		
		private String path;
		private String extension;
		private Map<String, List<String>> parameters = new LinkedHashMap<String, List<String>>();

		public UrlBuilder(String url) {
			int index = url.indexOf(QUERY_STRING_SEPARATOR);
			String urlPath = index != -1 ? url.substring(0, index) : url;
			setPath(urlPath.replaceFirst("\\.[^/]*$", ""));
			setExtension(urlPath.equals(path) ? null : urlPath.substring(path.length() + 1));
			if (index != -1) {
				addParameters(url.substring(index + 1));
			}
		}

		public UrlBuilder setPath(String path) {
			this.path = path;
			return this;
		}
		
		public UrlBuilder setExtension(String extension) {
			this.extension = extension;
			return this;
		}
		
		@SuppressWarnings("unused")
		public UrlBuilder addPath(String path) {
			setPath(this.path.concat("/" + path.replaceFirst("^/", "")));
			return this;
		}
		
		@SuppressWarnings("unused")
		public UrlBuilder removePath(String path) {
			setPath(this.path.replaceFirst(path + "$", ""));
			return this;
		}
		
		public UrlBuilder addParameter(String parameter) {
			if (!StringUtils.isEmpty(parameter)) {
				String[] pair = parameter.split(NAME_VALUE_SEPARATOR);
				if (pair.length > 0) {
					addParameter(pair[0], pair.length > 1 ? StringUtils.decode(pair[1]) : null);
				}
			}
			return this;
		}
		
		public UrlBuilder addParameter(String name, Object value) {
			if (value != null) {
				List<String> values;
				if (parameters.containsKey(name)) {
					values = parameters.get(name);
				} else {
					values = new ArrayList<String>();
				}
				if (value instanceof String) {
					values.add((String) value);
				} else if (value.getClass().isArray()) {
					for (Object v : (Object[]) value) {
						values.add(String.valueOf(v));
					}
				} else if (value instanceof Collection) {
					for (Object v : (Collection<?>) value) {
						values.add(String.valueOf(v));
					}
				} else {
					values.add(String.valueOf(value));
				}
				parameters.put(name, values);
			}
			return this;
		}
		
		public UrlBuilder setParameter(String parameter) {
			if (!StringUtils.isEmpty(parameter)) {
				String[] pair = parameter.split(NAME_VALUE_SEPARATOR);
				if (pair.length > 0) {
					setParameter(pair[0], pair.length > 1 ? StringUtils.decode(pair[1]) : null);
				}
			}
			return this;
		}
		
		public UrlBuilder setParameter(String name, Object value) {
			if (value != null) {
				parameters.put(name, new ArrayList<String>());
				addParameter(name, value);
			} else {
				removeParameter(name);
			}
			return this;
		}
		
		public UrlBuilder removeParameter(String name) {
			parameters.remove(name);
			return this;
		}
		
		public UrlBuilder addParameters(String parameters) {
			if (!StringUtils.isEmpty(parameters)) {
				String[] params = parameters.replaceAll("&amp;", PARAMETER_SEPARATOR).split(PARAMETER_SEPARATOR);
				for (String param : params) {
					addParameter(param);
				}
			}
			return this;
		}
		
		@SuppressWarnings("unused")
		public UrlBuilder setParameters(String parameters) {
			if (!StringUtils.isEmpty(parameters)) {
				String[] params = parameters.replaceAll("&amp;", PARAMETER_SEPARATOR).split(PARAMETER_SEPARATOR);
				for (String param : params) {
					setParameter(param);
				}
			}
			return this;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (!StringUtils.isEmpty(extension)) {
				sb.append(path.replaceAll("(.+)/$", "$1"));
				if (!extension.startsWith(".")) {
					sb.append(".");
				}
				sb.append(extension);
			} else {
				sb.append(path);
			}
			for (String key : parameters.keySet()) {
				sb.append(sb.toString().contains(QUERY_STRING_SEPARATOR) ? PARAMETER_SEPARATOR : QUERY_STRING_SEPARATOR);
				int count = 0;
				for (String v : parameters.get(key)) {
					if (count != 0) {
						sb.append(PARAMETER_SEPARATOR);
					}
					sb.append(key);
					sb.append(NAME_VALUE_SEPARATOR);
					sb.append(StringUtils.encode(v));
					count++;
				}
			}
			return sb.toString();
		}
	}
}