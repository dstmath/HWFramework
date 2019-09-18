package com.huawei.nb.coordinator.helper.http;

import com.huawei.nb.coordinator.helper.http.HttpRequestBody;
import com.huawei.nb.utils.logger.DSLog;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;
    private static final String DELETE_TYPE = "DELETE";
    private static final String GET_TYPE = "GET";
    private static final String POST_TYPE = "POST";
    private static final String TAG = "HttpRequest";
    /* access modifiers changed from: private */
    public int connectTimeout = 10000;
    /* access modifiers changed from: private */
    public int readTimeout = 10000;
    /* access modifiers changed from: private */
    public String requestBodyString;
    /* access modifiers changed from: private */
    public Map<String, String> requestHeaders;
    /* access modifiers changed from: private */
    public String requestMethod = "GET";
    /* access modifiers changed from: private */
    public String url;

    public static class Builder {
        private HttpRequest httpRequest = new HttpRequest();

        public Builder() {
            Map unused = this.httpRequest.requestHeaders = new LinkedHashMap();
        }

        public Builder url(String url) {
            String unused = this.httpRequest.url = url;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            int unused = this.httpRequest.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            int unused = this.httpRequest.readTimeout = readTimeout;
            return this;
        }

        public Builder addRequestHeader(String k, String v) {
            this.httpRequest.requestHeaders.put(k, v);
            return this;
        }

        public Builder get(HttpRequestBody requestBody) {
            String unused = this.httpRequest.requestMethod = "GET";
            String unused2 = this.httpRequest.requestBodyString = bodyString(requestBody);
            return this;
        }

        public Builder post() {
            String unused = this.httpRequest.requestMethod = "POST";
            return this;
        }

        public Builder post(HttpRequestBody requestBody) {
            String unused = this.httpRequest.requestMethod = "POST";
            if (requestBody.useJson()) {
                String unused2 = this.httpRequest.requestBodyString = requestBody.getJsonBody();
            } else {
                String unused3 = this.httpRequest.requestBodyString = bodyString(requestBody);
            }
            return this;
        }

        public Builder delete(HttpRequestBody requestBody) {
            String unused = this.httpRequest.requestMethod = "DELETE";
            if (requestBody.useJson()) {
                String unused2 = this.httpRequest.requestBodyString = requestBody.getJsonBody();
            } else {
                String unused3 = this.httpRequest.requestBodyString = bodyString(requestBody);
            }
            return this;
        }

        private String bodyString(HttpRequestBody requsetBody) {
            String v;
            List<HttpRequestBody.Parameter> bodyList = requsetBody.getBodyList();
            StringBuilder builder = new StringBuilder();
            int size = bodyList.size();
            for (int i = 0; i < size; i++) {
                try {
                    builder.append("&");
                    builder.append(bodyList.get(i).getK());
                    builder.append("=");
                    if (this.httpRequest.requestMethod.equals("POST")) {
                        v = URLEncoder.encode(bodyList.get(i).getV(), "UTF-8");
                    } else {
                        v = bodyList.get(i).getV();
                    }
                    builder.append(v);
                } catch (UnsupportedEncodingException e) {
                    DSLog.e("HttpRequestHttpRequest post UnsupportedEncodingException! requestBodyString:" + this.httpRequest.requestBodyString, new Object[0]);
                }
            }
            if (size > 0) {
                builder.deleteCharAt(0);
            }
            return builder.toString();
        }

        public HttpRequest build() {
            return this.httpRequest;
        }
    }

    public String getUrl() {
        return this.url;
    }

    public String getRequestBodyString() {
        return this.requestBodyString;
    }

    public String getRequestMethod() {
        return this.requestMethod;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public Map<String, String> getRequestHeaders() {
        return this.requestHeaders;
    }
}
