package ohos.miscservices.httpaccess.data;

import java.util.List;
import java.util.Map;

public class ResponseData {
    private int code;
    private String data;
    private Map<String, List<String>> headers;
    private long token;
    private String uri;

    public void setCode(int i) {
        this.code = i;
    }

    public int getCode() {
        return this.code;
    }

    public long getToken() {
        return this.token;
    }

    public void setToken(long j) {
        this.token = j;
    }

    public void setHeaders(Map<String, List<String>> map) {
        this.headers = map;
    }

    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    public void setData(String str) {
        this.data = str;
    }

    public String getData() {
        String str = this.data;
        return str == null ? "" : str;
    }

    public String getUri() {
        String str = this.uri;
        return str == null ? "" : str;
    }

    public void setUri(String str) {
        this.uri = str;
    }
}
