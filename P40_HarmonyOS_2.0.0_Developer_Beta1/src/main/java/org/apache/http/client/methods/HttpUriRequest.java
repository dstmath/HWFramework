package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.HttpRequest;

@Deprecated
public interface HttpUriRequest extends HttpRequest {
    @Override // org.apache.http.client.methods.AbortableHttpRequest
    void abort() throws UnsupportedOperationException;

    String getMethod();

    URI getURI();

    boolean isAborted();
}
