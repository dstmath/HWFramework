package com.huawei.wallet.sdk.common.apdu.whitecard;

public abstract class WalletProcessTrace implements WalletProcessTraceBase {
    private String processPrefix = "";
    private String subProcessPrefix = "";

    public void setProcessPrefix(String processPrefix2, String tag) {
        this.processPrefix = processPrefix2;
        this.subProcessPrefix = this.processPrefix + tag;
    }

    public void resetProcessPrefix() {
        this.processPrefix = "";
        this.subProcessPrefix = "";
    }

    public String getProcessPrefix() {
        return this.processPrefix;
    }

    public String getSubProcessPrefix() {
        return this.subProcessPrefix;
    }
}
