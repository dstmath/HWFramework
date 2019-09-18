package com.huawei.nb.coordinator.helper.http;

import com.huawei.nb.utils.logger.DSLog;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpResponse {
    private static final int DEFAULT_STATUS_CODE = -1;
    private static final String TAG = "HttpResponse";
    private Map<String, List<String>> headerFields = new LinkedHashMap();
    private String httpExceptionMsg = "";
    private boolean isDownloadStart = false;
    private String responseMsg = "";
    private long responseSize = 0;
    private String responseString = "";
    private int statusCode = -1;
    private String url = "";

    public long getResponseSize() {
        return this.responseSize;
    }

    public void setResponseSize(long responseSize2) {
        this.responseSize = responseSize2;
    }

    public String getHttpExceptionMsg() {
        return this.httpExceptionMsg;
    }

    public void setHttpExceptionMsg(String httpExceptionMsg2) {
        this.httpExceptionMsg = httpExceptionMsg2;
    }

    public String getResponseString() {
        return this.responseString;
    }

    public void setResponseString(String responseString2) {
        this.responseString = responseString2;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode2) {
        this.statusCode = statusCode2;
    }

    public String getResponseMsg() {
        return this.responseMsg;
    }

    public void setResponseMsg(String responseMsg2) {
        this.responseMsg = responseMsg2;
    }

    public String getHeaderValue(String headerKey) {
        List<String> tokenList = this.headerFields.get(headerKey);
        StringBuilder builder = new StringBuilder();
        if (tokenList == null) {
            return builder.toString();
        }
        for (String s : tokenList) {
            builder.append(s);
        }
        return builder.toString();
    }

    public void setHeaderFields(Map<String, List<String>> headerFields2) {
        if (headerFields2 == null) {
            DSLog.e("HttpResponse HeaderFields is empty.", new Object[0]);
        } else {
            this.headerFields.putAll(headerFields2);
        }
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public boolean isDownloadStart() {
        return this.isDownloadStart;
    }

    public void setDownloadStart(boolean downloadStart) {
        this.isDownloadStart = downloadStart;
    }
}
