package com.huawei.wallet.sdk.business.bankcard.response;

import java.util.HashMap;
import java.util.Map;

public class RouterResponse {
    public static final String ERROR_CODE_ACTION_NOTFOUND = "99998";
    public static final String ERROR_CODE_PROVIDER_NOTFOUND = "99999";
    private boolean isAsync = true;
    private boolean isSuccess = true;
    private String resultCode;
    private String resultDesc;
    private Map<String, Object> resultParamMap = new HashMap();

    public void setSuccess(boolean success) {
        this.isSuccess = success;
    }

    public RouterResponse() {
    }

    public RouterResponse(String resultCode2, String resultDesc2) {
        this.resultCode = resultCode2;
        this.resultDesc = resultDesc2;
    }

    public String getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(String resultCode2) {
        this.resultCode = resultCode2;
    }

    public String getResultDesc() {
        return this.resultDesc;
    }

    public void setResultDesc(String resultDesc2) {
        this.resultDesc = resultDesc2;
    }

    public boolean isAsync() {
        return this.isAsync;
    }

    public void setAsync(boolean async) {
        this.isAsync = async;
    }

    public Object getResultParam(String key) {
        return this.resultParamMap.get(key);
    }

    public void addResultMap(Map<String, Object> resultParamMap2) {
        this.resultParamMap.putAll(resultParamMap2);
    }

    public void putResultParam(String key, Object value) {
        this.resultParamMap.put(key, value);
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }
}
