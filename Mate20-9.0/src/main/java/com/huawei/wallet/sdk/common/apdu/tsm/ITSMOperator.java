package com.huawei.wallet.sdk.common.apdu.tsm;

public interface ITSMOperator {
    TSMOperateResponse deleteApplet(String str);

    TSMOperateResponse deleteApplet(String str, int i);

    TSMOperateResponse deleteApplet(String str, String str2, String str3, String str4);

    TSMOperateResponse deleteApplet(String str, String str2, String str3, String str4, int i);

    TSMOperateResponse deleteSSD(String str, String str2, String str3, String str4, boolean z);

    TSMOperateResponse deleteSSD(String str, String str2, String str3, String str4, boolean z, int i);

    TSMOperateResponse deleteSSD(String str, String str2, boolean z);

    TSMOperateResponse deleteSSD(String str, String str2, boolean z, int i);

    TSMOperateResponse deleteSSD(String str, boolean z);

    TSMOperateResponse deleteSSD(String str, boolean z, int i);

    TSMOperateResponse initEse();

    TSMOperateResponse initEse(int i);

    TSMOperateResponse initEseByBasicChannel();

    TSMOperateResponse initEseByBasicChannel(int i);

    TSMOperateResponse resetOpt(String str, int i);

    TSMOperateResponse syncEseInfo(String str);

    TSMOperateResponse syncEseInfo(String str, int i);

    TSMOperateResponse syncEseInfo(String str, String str2, String str3, boolean z);

    TSMOperateResponse syncEseInfo(String str, String str2, String str3, boolean z, int i);
}
