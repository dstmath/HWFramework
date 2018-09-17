package com.android.okhttp;

import com.android.okhttp.internal.http.HttpMethod;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

public final class Request {
    private final RequestBody body;
    private volatile CacheControl cacheControl;
    private final Headers headers;
    private volatile URI javaNetUri;
    private volatile URL javaNetUrl;
    private final String method;
    private final Object tag;
    private final HttpUrl url;

    public static class Builder {
        private RequestBody body;
        private com.android.okhttp.Headers.Builder headers;
        private String method;
        private Object tag;
        private HttpUrl url;

        /* synthetic */ Builder(Request request, Builder -this1) {
            this(request);
        }

        public Builder() {
            this.method = "GET";
            this.headers = new com.android.okhttp.Headers.Builder();
        }

        private Builder(Request request) {
            this.url = request.url;
            this.method = request.method;
            this.body = request.body;
            this.tag = request.tag;
            this.headers = request.headers.newBuilder();
        }

        public Builder url(HttpUrl url) {
            if (url == null) {
                throw new IllegalArgumentException("url == null");
            }
            this.url = url;
            return this;
        }

        public Builder url(String url) {
            if (url == null) {
                throw new IllegalArgumentException("url == null");
            }
            if (url.regionMatches(true, 0, "ws:", 0, 3)) {
                url = "http:" + url.substring(3);
            } else {
                if (url.regionMatches(true, 0, "wss:", 0, 4)) {
                    url = "https:" + url.substring(4);
                }
            }
            HttpUrl parsed = HttpUrl.parse(url);
            if (parsed != null) {
                return url(parsed);
            }
            throw new IllegalArgumentException("unexpected url: " + url);
        }

        public Builder url(URL url) {
            if (url == null) {
                throw new IllegalArgumentException("url == null");
            }
            HttpUrl parsed = HttpUrl.get(url);
            if (parsed != null) {
                return url(parsed);
            }
            throw new IllegalArgumentException("unexpected url: " + url);
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

        public Builder headers(Headers headers) {
            this.headers = headers.newBuilder();
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

        public Builder post(RequestBody body) {
            return method("POST", body);
        }

        public Builder delete(RequestBody body) {
            return method("DELETE", body);
        }

        public Builder delete() {
            return delete(RequestBody.create(null, new byte[0]));
        }

        public Builder put(RequestBody body) {
            return method("PUT", body);
        }

        public Builder patch(RequestBody body) {
            return method("PATCH", body);
        }

        public Builder method(String method, RequestBody body) {
            if (method == null || method.length() == 0) {
                throw new IllegalArgumentException("method == null || method.length() == 0");
            } else if (body != null && (HttpMethod.permitsRequestBody(method) ^ 1) != 0) {
                throw new IllegalArgumentException("method " + method + " must not have a request body.");
            } else if (body == null && HttpMethod.requiresRequestBody(method)) {
                throw new IllegalArgumentException("method " + method + " must have a request body.");
            } else {
                this.method = method;
                this.body = body;
                return this;
            }
        }

        public Builder tag(Object tag) {
            this.tag = tag;
            return this;
        }

        public Request build() {
            if (this.url != null) {
                return new Request(this, null);
            }
            throw new IllegalStateException("url == null");
        }
    }

    /* synthetic */ Request(Builder builder, Request -this1) {
        this(builder);
    }

    private Request(Builder builder) {
        Object -get3;
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers.build();
        this.body = builder.body;
        if (builder.tag != null) {
            -get3 = builder.tag;
        } else {
            Request -get32 = this;
        }
        this.tag = -get32;
    }

    public HttpUrl httpUrl() {
        return this.url;
    }

    public URL url() {
        URL result = this.javaNetUrl;
        if (result != null) {
            return result;
        }
        result = this.url.url();
        this.javaNetUrl = result;
        return result;
    }

    public URI uri() throws IOException {
        try {
            URI result = this.javaNetUri;
            if (result != null) {
                return result;
            }
            result = this.url.uri();
            this.javaNetUri = result;
            return result;
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
        return new Builder(this, null);
    }

    public CacheControl cacheControl() {
        CacheControl result = this.cacheControl;
        if (result != null) {
            return result;
        }
        result = CacheControl.parse(this.headers);
        this.cacheControl = result;
        return result;
    }

    public boolean isHttps() {
        return this.url.isHttps();
    }

    public String toString() {
        Object obj;
        StringBuilder append = new StringBuilder().append("Request{method=").append(this.method).append(", url=").append(this.url).append(", tag=");
        if (this.tag != this) {
            obj = this.tag;
        } else {
            obj = null;
        }
        return append.append(obj).append('}').toString();
    }
}
