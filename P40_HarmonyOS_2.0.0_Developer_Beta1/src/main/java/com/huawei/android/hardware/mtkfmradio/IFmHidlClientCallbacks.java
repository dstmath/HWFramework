package com.huawei.android.hardware.mtkfmradio;

/* access modifiers changed from: package-private */
public interface IFmHidlClientCallbacks {
    void onRadioTunedStatus(int i);

    void onRdsInfo(int i);

    void onRdsLockedStatus(boolean z);

    void onSearchCancelled();

    void onSearchComplete(int i);

    void onSearchInProgress();
}
