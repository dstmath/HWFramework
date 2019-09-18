package com.huawei.wallet.sdk.business.bankcard.request;

import java.util.Map;

public class WalletActionRequest {
    private String actionName;

    public String getActionName() {
        return this.actionName;
    }

    public WalletActionRequest(String actionName2, Map<String, Object> map) {
        this.actionName = actionName2;
    }
}
