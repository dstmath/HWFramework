package org.apache.http.protocol;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;

@Deprecated
public interface HttpProcessor extends HttpRequestInterceptor, HttpResponseInterceptor {
}
