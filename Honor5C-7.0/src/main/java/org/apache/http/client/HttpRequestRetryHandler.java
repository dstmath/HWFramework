package org.apache.http.client;

import java.io.IOException;
import org.apache.http.protocol.HttpContext;

@Deprecated
public interface HttpRequestRetryHandler {
    boolean retryRequest(IOException iOException, int i, HttpContext httpContext);
}
