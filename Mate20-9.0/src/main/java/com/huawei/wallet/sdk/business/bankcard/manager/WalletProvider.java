package com.huawei.wallet.sdk.business.bankcard.manager;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.api.WalletAction;
import com.huawei.wallet.sdk.business.bankcard.request.WalletActionRequest;
import com.huawei.wallet.sdk.business.bankcard.response.WalletActionResult;
import java.util.HashMap;
import java.util.Map;

public abstract class WalletProvider {
    private HashMap<String, WalletAction> actionMap = new HashMap<>();
    private String domain;
    private String schema;

    public abstract String getName();

    /* access modifiers changed from: protected */
    public abstract void registerActions();

    public void setDomain(String domain2) {
        this.domain = domain2;
    }

    public String getDomain() {
        return this.domain;
    }

    public WalletActionResult invokeAction(String actionName, Context context, Map<String, Object> actionRequest) {
        WalletAction action = findAction(actionName);
        if (action != null) {
            return action.invoke(context, new WalletActionRequest(actionName, actionRequest));
        }
        WalletActionResult actionResult = new WalletActionResult();
        actionResult.setResultCode("");
        actionResult.setResultDesc("Action not found,domain: " + this.domain + " actionName: " + actionName);
        return actionResult;
    }

    /* access modifiers changed from: protected */
    public void registerAction(String actionName, WalletAction action) {
        this.actionMap.put(actionName, action);
    }

    /* access modifiers changed from: protected */
    public WalletAction findAction(String actionName) {
        return this.actionMap.get(actionName);
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema2) {
        this.schema = schema2;
    }

    public String getAbsoluteName() {
        if (getSchema() != null) {
            return getSchema() + ":" + getDomain() + "_" + getName();
        }
        return getDomain() + "_" + getName();
    }
}
