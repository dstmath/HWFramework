package com.huawei.wallet.sdk.business.bankcard.response;

import java.util.HashMap;
import java.util.Map;

public class WalletActionResult {
    private boolean isSuccess = true;
    private String resultCode;
    private String resultDesc;
    private Map<String, Object> resultParamMap = new HashMap();

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

    public boolean isSuccess() {
        return this.isSuccess;
    }

    public Map<String, Object> getResultParamMap() {
        return this.resultParamMap;
    }

    public void putResultParam(String key, Object value) {
        this.resultParamMap.put(key, value);
    }
}
