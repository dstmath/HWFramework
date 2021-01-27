package com.huawei.nb.coordinator.helper.http;

import com.huawei.nb.utils.logger.DSLog;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpResponse {
    private static final int DEFAULT_STATUS_CODE = -1;
    private static final int MAX_HEADER_VALUE_LENGTH = 1000000;
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

    public void setResponseSize(long j) {
        this.responseSize = j;
    }

    public String getHttpExceptionMsg() {
        return this.httpExceptionMsg;
    }

    public void setHttpExceptionMsg(String str) {
        this.httpExceptionMsg = str;
    }

    public String getResponseString() {
        return this.responseString;
    }

    public void setResponseString(String str) {
        this.responseString = str;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int i) {
        this.statusCode = i;
    }

    public String getResponseMsg() {
        return this.responseMsg;
    }

    public void setResponseMsg(String str) {
        this.responseMsg = str;
    }

    public String getHeaderValue(String str) {
        List<String> list = this.headerFields.get(str);
        StringBuilder sb = new StringBuilder();
        if (list == null) {
            return sb.toString();
        }
        Iterator<String> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String next = it.next();
            if (sb.length() > MAX_HEADER_VALUE_LENGTH) {
                DSLog.e("HttpResponse response header's length is more than 1000000", new Object[0]);
                break;
            }
            sb.append(next);
        }
        return sb.toString();
    }

    public void setHeaderFields(Map<String, List<String>> map) {
        if (map == null) {
            DSLog.e("HttpResponse HeaderFields is empty.", new Object[0]);
        } else {
            this.headerFields.putAll(map);
        }
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public boolean isDownloadStart() {
        return this.isDownloadStart;
    }

    public void setDownloadStart(boolean z) {
        this.isDownloadStart = z;
    }
}
