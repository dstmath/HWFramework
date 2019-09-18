package com.huawei.nb.coordinator.helper.http;

import java.util.ArrayList;
import java.util.List;

public class HttpRequestBody {
    /* access modifiers changed from: private */
    public List<Parameter> bodyMap = new ArrayList();
    /* access modifiers changed from: private */
    public String jsonBody;
    /* access modifiers changed from: private */
    public boolean useJson = false;

    public static class Builder {
        private HttpRequestBody requestBody = new HttpRequestBody();

        public Builder add(String k, String v) {
            Parameter parameter = new Parameter();
            parameter.setKey(k);
            parameter.setValue(v);
            this.requestBody.bodyMap.add(parameter);
            return this;
        }

        public Builder addJsonBody(String jsonString) {
            String unused = this.requestBody.jsonBody = jsonString;
            boolean unused2 = this.requestBody.useJson = true;
            return this;
        }

        public HttpRequestBody build() {
            return this.requestBody;
        }
    }

    public static class Parameter {
        private String key;
        private String value;

        public void setKey(String key2) {
            this.key = key2;
        }

        public void setValue(String value2) {
            this.value = value2;
        }

        public String getK() {
            return this.key;
        }

        public String getV() {
            return this.value;
        }
    }

    public List<Parameter> getBodyList() {
        return this.bodyMap;
    }

    public String getJsonBody() {
        return this.jsonBody;
    }

    public boolean useJson() {
        return this.useJson;
    }
}
