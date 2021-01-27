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
    private int connectTimeout = 10000;
    private int readTimeout = 10000;
    private String requestBodyString;
    private Map<String, String> requestHeaders;
    private String requestMethod = "GET";
    private String url;

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

    public static class Builder {
        private HttpRequest httpRequest = new HttpRequest();

        public Builder() {
            this.httpRequest.requestHeaders = new LinkedHashMap();
        }

        public Builder url(String str) {
            this.httpRequest.url = str;
            return this;
        }

        public Builder connectTimeout(int i) {
            this.httpRequest.connectTimeout = i;
            return this;
        }

        public Builder readTimeout(int i) {
            this.httpRequest.readTimeout = i;
            return this;
        }

        public Builder addRequestHeader(String str, String str2) {
            this.httpRequest.requestHeaders.put(str, str2);
            return this;
        }

        public Builder get(HttpRequestBody httpRequestBody) {
            this.httpRequest.requestMethod = "GET";
            this.httpRequest.requestBodyString = bodyString(httpRequestBody);
            return this;
        }

        public Builder post() {
            this.httpRequest.requestMethod = "POST";
            return this;
        }

        public Builder post(HttpRequestBody httpRequestBody) {
            this.httpRequest.requestMethod = "POST";
            if (httpRequestBody.useJson()) {
                this.httpRequest.requestBodyString = httpRequestBody.getJsonBody();
            } else {
                this.httpRequest.requestBodyString = bodyString(httpRequestBody);
            }
            return this;
        }

        public Builder delete(HttpRequestBody httpRequestBody) {
            this.httpRequest.requestMethod = "DELETE";
            if (httpRequestBody.useJson()) {
                this.httpRequest.requestBodyString = httpRequestBody.getJsonBody();
            } else {
                this.httpRequest.requestBodyString = bodyString(httpRequestBody);
            }
            return this;
        }

        private String bodyString(HttpRequestBody httpRequestBody) {
            String str;
            List<HttpRequestBody.Parameter> bodyList = httpRequestBody.getBodyList();
            StringBuilder sb = new StringBuilder();
            int size = bodyList.size();
            for (int i = 0; i < size; i++) {
                try {
                    sb.append("&");
                    sb.append(bodyList.get(i).getK());
                    sb.append("=");
                    if (this.httpRequest.requestMethod.equals("POST")) {
                        str = URLEncoder.encode(bodyList.get(i).getV(), "UTF-8");
                    } else {
                        str = bodyList.get(i).getV();
                    }
                    sb.append(str);
                } catch (UnsupportedEncodingException unused) {
                    DSLog.e("HttpRequestHttpRequest post UnsupportedEncodingException!", new Object[0]);
                }
            }
            if (size > 0) {
                sb.deleteCharAt(0);
            }
            return sb.toString();
        }

        public HttpRequest build() {
            return this.httpRequest;
        }
    }
}
