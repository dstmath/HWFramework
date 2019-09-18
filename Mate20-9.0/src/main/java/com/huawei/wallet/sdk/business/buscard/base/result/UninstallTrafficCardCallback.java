package com.huawei.wallet.sdk.business.buscard.base.result;

public interface UninstallTrafficCardCallback extends TrafficCardBaseCallback {
    public static final int RETURN_FAILED_HAS_NO_RECHARGE_RECORD = 2001;
    public static final int RETURN_FAILED_NOT_OPENCARD_ACCOUT = 2003;
    public static final int UPDATE_TA_STATUS_ERROR = 2002;

    void uninstallTrafficCardCallback(int i);
}
