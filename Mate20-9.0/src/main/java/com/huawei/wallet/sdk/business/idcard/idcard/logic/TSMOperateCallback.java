package com.huawei.wallet.sdk.business.idcard.idcard.logic;

public interface TSMOperateCallback {
    void onFail(int i, String str, long j);

    void onSuccess(long j);
}
