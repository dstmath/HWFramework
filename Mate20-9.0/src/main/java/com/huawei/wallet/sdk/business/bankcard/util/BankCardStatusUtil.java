package com.huawei.wallet.sdk.business.bankcard.util;

public class BankCardStatusUtil {
    private static BankCardStatusUtil instance = new BankCardStatusUtil();
    private boolean executingCMD = false;

    public static BankCardStatusUtil getInstance() {
        return instance;
    }

    public synchronized boolean isExecutingCMD() {
        return this.executingCMD;
    }

    public synchronized void setExecutingCMD(boolean executingCMD2) {
        this.executingCMD = executingCMD2;
    }
}
