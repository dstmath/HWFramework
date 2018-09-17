package org.apache.http;

@Deprecated
public interface HttpRequest extends HttpMessage {
    RequestLine getRequestLine();
}
