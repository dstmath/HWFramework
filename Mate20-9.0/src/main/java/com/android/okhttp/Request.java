package com.android.okhttp;

import com.android.okhttp.Headers;
import com.android.okhttp.internal.http.HttpMethod;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

public final class Request {
    /* access modifiers changed from: private */
    public final RequestBody body;
    private volatile CacheControl cacheControl;
    /* access modifiers changed from: private */
    public final Headers headers;
    private volatile URI javaNetUri;
    private volatile URL javaNetUrl;
    /* access modifiers changed from: private */
    public final String method;
    /* access modifiers changed from: private */
    public final Object tag;
    /* access modifiers changed from: private */
    public final HttpUrl url;

    public static class Builder {
        /* access modifiers changed from: private */
        public RequestBody body;
        /* access modifiers changed from: private */
        public Headers.Builder headers;
        /* access modifiers changed from: private */
        public String method;
        /* access modifiers changed from: private */
        public Object tag;
        /* access modifiers changed from: private */
        public HttpUrl url;

        public Builder() {
            this.method = "GET";
            this.headers = new Headers.Builder();
        }

        private Builder(Request request) {
            this.url = request.url;
            this.method = request.method;
            this.body = request.body;
            this.tag = request.tag;
            this.headers = request.headers.newBuilder();
        }

        public Builder url(HttpUrl url2) {
            if (url2 != null) {
                this.url = url2;
                return this;
            }
            throw new IllegalArgumentException("url == null");
        }

        public Builder url(String url2) {
            if (url2 != null) {
                if (url2.regionMatches(true, 0, "ws:", 0, 3)) {
                    url2 = "http:" + url2.substring(3);
                } else if (url2.regionMatches(true, 0, "wss:", 0, 4)) {
                    url2 = "https:" + url2.substring(4);
                }
                HttpUrl parsed = HttpUrl.parse(url2);
                if (parsed != null) {
                    return url(parsed);
                }
                throw new IllegalArgumentException("unexpected url: " + url2);
            }
            throw new IllegalArgumentException("url == null");
        }

        public Builder url(URL url2) {
            if (url2 != null) {
                HttpUrl parsed = HttpUrl.get(url2);
                if (parsed != null) {
                    return url(parsed);
                }
                throw new IllegalArgumentException("unexpected url: " + url2);
            }
            throw new IllegalArgumentException("url == null");
        }

        public Builder header(String name, String value) {
            this.headers.set(name, value);
            return this;
        }

        public Builder addHeader(String name, String value) {
            this.headers.add(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            this.headers.removeAll(name);
            return this;
        }

        public Builder headers(Headers headers2) {
            this.headers = headers2.newBuilder();
            return this;
        }

        public Builder cacheControl(CacheControl cacheControl) {
            String value = cacheControl.toString();
            if (value.isEmpty()) {
                return removeHeader("Cache-Control");
            }
            return header("Cache-Control", value);
        }

        public Builder get() {
            return method("GET", null);
        }

        public Builder head() {
            return method("HEAD", null);
        }

        public Builder post(RequestBody body2) {
            return method("POST", body2);
        }

        public Builder delete(RequestBody body2) {
            return method("DELETE", body2);
        }

        public Builder delete() {
            return delete(RequestBody.create((MediaType) null, new byte[0]));
        }

        public Builder put(RequestBody body2) {
            return method("PUT", body2);
        }

        public Builder patch(RequestBody body2) {
            return method("PATCH", body2);
        }

        public Builder method(String method2, RequestBody body2) {
            if (method2 == null || method2.length() == 0) {
                throw new IllegalArgumentException("method == null || method.length() == 0");
            } else if (body2 != null && !HttpMethod.permitsRequestBody(method2)) {
                throw new IllegalArgumentException("method " + method2 + " must not have a request body.");
            } else if (body2 != null || !HttpMethod.requiresRequestBody(method2)) {
                this.method = method2;
                this.body = body2;
                return this;
            } else {
                throw new IllegalArgumentException("method " + method2 + " must have a request body.");
            }
        }

        public Builder tag(Object tag2) {
            this.tag = tag2;
            return this;
        }

        public Request build() {
            if (this.url != null) {
                return new Request(this);
            }
            throw new IllegalStateException("url == null");
        }
    }

    private Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.tag = builder.tag != null ? builder.tag : this;
    }

    public HttpUrl httpUrl() {
        return this.url;
    }

    public URL url() {
        URL result = this.javaNetUrl;
        if (result != null) {
            return result;
        }
        URL url2 = this.url.url();
        this.javaNetUrl = url2;
        return url2;
    }

    public URI uri() throws IOException {
        try {
            URI result = this.javaNetUri;
            if (result != null) {
                return result;
            }
            URI uri = this.url.uri();
            this.javaNetUri = uri;
            return uri;
        } catch (IllegalStateException e) {
            throw new IOException(e.getMessage());
        }
    }

    public String urlString() {
        return this.url.toString();
    }

    public String method() {
        return this.method;
    }

    public Headers headers() {
        return this.headers;
    }

    public String header(String name) {
        return this.headers.get(name);
    }

    public List<String> headers(String name) {
        return this.headers.values(name);
    }

    public RequestBody body() {
        return this.body;
    }

    public Object tag() {
        return this.tag;
    }

    public Builder newBuilder() {
        return new Builder();
    }

    public CacheControl cacheControl() {
        CacheControl result = this.cacheControl;
        if (result != null) {
            return result;
        }
        CacheControl parse = CacheControl.parse(this.headers);
        this.cacheControl = parse;
        return parse;
    }

    public boolean isHttps() {
        return this.url.isHttps();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Request{method=");
        sb.append(this.method);
        sb.append(", url=");
        sb.append(this.url);
        sb.append(", tag=");
        sb.append(this.tag != this ? this.tag : null);
        sb.append('}');
        return sb.toString();
    }
}
