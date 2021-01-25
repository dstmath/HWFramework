package com.huawei.nb.coordinator.helper.http;

import java.util.ArrayList;
import java.util.List;

public class HttpRequestBody {
    private List<Parameter> bodies = new ArrayList();
    private boolean isUseJson = false;
    private String jsonBody;

    public List<Parameter> getBodyList() {
        return this.bodies;
    }

    public String getJsonBody() {
        return this.jsonBody;
    }

    public boolean useJson() {
        return this.isUseJson;
    }

    public static class Builder {
        private HttpRequestBody requestBody = new HttpRequestBody();

        public Builder add(String str, String str2) {
            Parameter parameter = new Parameter();
            parameter.setKey(str);
            parameter.setValue(str2);
            this.requestBody.bodies.add(parameter);
            return this;
        }

        public Builder addJsonBody(String str) {
            this.requestBody.jsonBody = str;
            this.requestBody.isUseJson = true;
            return this;
        }

        public HttpRequestBody build() {
            return this.requestBody;
        }
    }

    public static class Parameter {
        private String key;
        private String value;

        public void setKey(String str) {
            this.key = str;
        }

        public void setValue(String str) {
            this.value = str;
        }

        public String getK() {
            return this.key;
        }

        public String getV() {
            return this.value;
        }
    }
}
