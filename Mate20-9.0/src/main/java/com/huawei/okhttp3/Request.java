package com.huawei.okhttp3;

import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.http.HttpMethod;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public final class Request {
    final ArrayList<InetAddress> additionalInetAddresses;
    @Nullable
    final RequestBody body;
    private volatile CacheControl cacheControl;
    final boolean concurrentConnectEnabled;
    final Headers headers;
    final String method;
    final Map<Class<?>, Object> tags;
    final HttpUrl url;

    public static class Builder {
        ArrayList<InetAddress> additionalInetAddresses;
        RequestBody body;
        boolean concurrentConnectEnabled;
        Headers.Builder headers;
        String method;
        Map<Class<?>, Object> tags;
        HttpUrl url;

        public Builder() {
            this.tags = Collections.emptyMap();
            this.concurrentConnectEnabled = false;
            this.additionalInetAddresses = new ArrayList<>();
            this.method = "GET";
            this.headers = new Headers.Builder();
        }

        Builder(Request request) {
            Map<Class<?>, Object> map;
            this.tags = Collections.emptyMap();
            this.concurrentConnectEnabled = false;
            this.additionalInetAddresses = new ArrayList<>();
            this.url = request.url;
            this.method = request.method;
            this.body = request.body;
            if (request.tags.isEmpty()) {
                map = Collections.emptyMap();
            } else {
                map = new LinkedHashMap<>(request.tags);
            }
            this.tags = map;
            this.headers = request.headers.newBuilder();
            this.concurrentConnectEnabled = request.concurrentConnectEnabled;
            this.additionalInetAddresses = request.additionalInetAddresses;
        }

        public Builder url(HttpUrl url2) {
            if (url2 != null) {
                this.url = url2;
                return this;
            }
            throw new NullPointerException("url == null");
        }

        public Builder url(String url2) {
            if (url2 != null) {
                if (url2.regionMatches(true, 0, "ws:", 0, 3)) {
                    url2 = "http:" + url2.substring(3);
                } else if (url2.regionMatches(true, 0, "wss:", 0, 4)) {
                    url2 = "https:" + url2.substring(4);
                }
                return url(HttpUrl.get(url2));
            }
            throw new NullPointerException("url == null");
        }

        public Builder url(URL url2) {
            if (url2 != null) {
                return url(HttpUrl.get(url2.toString()));
            }
            throw new NullPointerException("url == null");
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

        public Builder delete(@Nullable RequestBody body2) {
            return method("DELETE", body2);
        }

        public Builder delete() {
            return delete(Util.EMPTY_REQUEST);
        }

        public Builder put(RequestBody body2) {
            return method("PUT", body2);
        }

        public Builder patch(RequestBody body2) {
            return method("PATCH", body2);
        }

        public Builder method(String method2, @Nullable RequestBody body2) {
            if (method2 == null) {
                throw new NullPointerException("method == null");
            } else if (method2.length() == 0) {
                throw new IllegalArgumentException("method.length() == 0");
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

        public Builder tag(@Nullable Object tag) {
            return tag(Object.class, tag);
        }

        public <T> Builder tag(Class<? super T> type, @Nullable T tag) {
            if (type != null) {
                if (tag == null) {
                    this.tags.remove(type);
                } else {
                    if (this.tags.isEmpty()) {
                        this.tags = new LinkedHashMap();
                    }
                    this.tags.put(type, type.cast(tag));
                }
                return this;
            }
            throw new NullPointerException("type == null");
        }

        public Builder concurrentConnectEnabled(boolean concurrentConnectEnabled2) {
            this.concurrentConnectEnabled = concurrentConnectEnabled2;
            return this;
        }

        public Builder additionalIpAddresses(ArrayList<String> additionalIpAddresses) throws UnknownHostException {
            if (additionalIpAddresses != null) {
                Iterator<String> it = additionalIpAddresses.iterator();
                while (it.hasNext()) {
                    addIpAddress(it.next());
                }
                return this;
            }
            throw new IllegalArgumentException("additionalIpAddresses is null");
        }

        public Builder addIpAddress(String ipAddress) throws UnknownHostException {
            if (ipAddress != null) {
                try {
                    for (InetAddress inetAddress : InetAddress.getAllByName(ipAddress)) {
                        this.additionalInetAddresses.add(inetAddress);
                    }
                    return this;
                } catch (NullPointerException e) {
                    UnknownHostException unknownHostException = new UnknownHostException("Broken system behaviour for dns lookup of " + ipAddress);
                    unknownHostException.initCause(e);
                    throw unknownHostException;
                }
            } else {
                throw new IllegalArgumentException("IP address is null");
            }
        }

        public Request build() {
            if (this.url != null) {
                return new Request(this);
            }
            throw new IllegalStateException("url == null");
        }
    }

    Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.tags = Util.immutableMap(builder.tags);
        this.concurrentConnectEnabled = builder.concurrentConnectEnabled;
        this.additionalInetAddresses = builder.additionalInetAddresses;
    }

    public HttpUrl url() {
        return this.url;
    }

    public String method() {
        return this.method;
    }

    public Headers headers() {
        return this.headers;
    }

    @Nullable
    public String header(String name) {
        return this.headers.get(name);
    }

    public List<String> headers(String name) {
        return this.headers.values(name);
    }

    @Nullable
    public RequestBody body() {
        return this.body;
    }

    @Nullable
    public Object tag() {
        return tag(Object.class);
    }

    @Nullable
    public <T> T tag(Class<? extends T> type) {
        return type.cast(this.tags.get(type));
    }

    public boolean concurrentConnectEnabled() {
        return this.concurrentConnectEnabled;
    }

    public ArrayList<InetAddress> additionalInetAddresses() {
        return this.additionalInetAddresses;
    }

    public Builder newBuilder() {
        return new Builder(this);
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

    public boolean isCreateConnectionRequest() {
        return header("Http2ConnectionIndex") != null;
    }

    public String toString() {
        return "Request{method=" + this.method + ", url=" + this.url + ", tags=" + this.tags + '}';
    }
}
