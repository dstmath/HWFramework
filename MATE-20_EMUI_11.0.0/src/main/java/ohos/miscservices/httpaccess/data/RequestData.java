package ohos.miscservices.httpaccess.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestData {
    private String data;
    private String description;
    private String fileName;
    private List<FormFileData> files;
    private Map<String, String> header = new HashMap();
    private String method;
    private String responseType;
    private String token;
    private String url;

    public void setUrl(String str) {
        this.url = str;
    }

    public String getUrl() {
        String str = this.url;
        return str == null ? "" : str;
    }

    public void setResponseType(String str) {
        this.responseType = str;
    }

    public String getResponseType() {
        String str = this.responseType;
        return str == null ? "" : str;
    }

    public void setMethod(String str) {
        this.method = str;
    }

    public String getMethod() {
        String str = this.method;
        return str == null ? "" : str;
    }

    public void setData(String str) {
        this.data = str;
    }

    public String getData() {
        String str = this.data;
        return str == null ? "" : str;
    }

    public void setDescription(String str) {
        this.description = str;
    }

    public String getDescription() {
        String str = this.description;
        return str == null ? "" : str;
    }

    public void setHeader(Map<String, String> map) {
        this.header = map;
    }

    public Map<String, String> getHeader() {
        return this.header;
    }

    public void setFileName(String str) {
        this.fileName = str;
    }

    public String getFileName() {
        String str = this.fileName;
        return str == null ? "" : str;
    }

    public String getToken() {
        String str = this.token;
        return str == null ? "" : str;
    }

    public void setToken(String str) {
        this.token = str;
    }

    public List<FormFileData> getFiles() {
        return this.files;
    }

    public void setFiles(List<FormFileData> list) {
        this.files = list;
    }
}
