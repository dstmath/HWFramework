package org.apache.http.client;

import org.apache.http.protocol.HttpContext;

@Deprecated
public interface UserTokenHandler {
    Object getUserToken(HttpContext httpContext);
}
