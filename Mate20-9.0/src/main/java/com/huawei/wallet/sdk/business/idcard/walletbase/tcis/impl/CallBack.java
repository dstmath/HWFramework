package com.huawei.wallet.sdk.business.idcard.walletbase.tcis.impl;

public interface CallBack<T> {
    void onFail(int i);

    void onSucess(T t);
}
