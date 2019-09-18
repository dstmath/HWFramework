package com.huawei.wallet.sdk.business.idcard.walletbase.model;

public interface IWalletCardBaseInfo {
    String getAction();

    String getName();

    Object getObject();

    int getStatus();

    int getType();

    boolean isDefault();
}
