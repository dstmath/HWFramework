package com.huawei.wallet.sdk.business.bankcard.request;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;

public class RouterRequest {
    private Map<String, String> abTestParamMap = null;
    private String action = null;
    private Context context = null;
    private String domain = null;
    private Map<String, Object> paramMap = null;
    private String provider = null;

    public Context getContext() {
        return this.context;
    }

    public String getAction() {
        return this.action;
    }

    public Map<String, Object> getParamMap() {
        return this.paramMap;
    }

    public RouterRequest(Context context2) {
        this.context = context2;
        this.paramMap = new HashMap();
        this.abTestParamMap = new HashMap();
    }

    public String getProviderName() {
        return this.domain + "_" + this.provider;
    }

    public RouterRequest setDomain(String domain2) {
        this.domain = domain2;
        return this;
    }

    public RouterRequest setProvider(String provider2) {
        this.provider = provider2;
        return this;
    }

    public RouterRequest setAction(String action2) {
        this.action = action2;
        return this;
    }

    public void putParam(String paramName, Object paramValue) {
        this.paramMap.put(paramName, paramValue);
    }

    public void putABTestParam(String paramName, String paramValue) {
        this.abTestParamMap.put(paramName, paramValue);
    }

    public String getAbsoluteActionName() {
        return getProviderName() + ":" + getAction();
    }
}
