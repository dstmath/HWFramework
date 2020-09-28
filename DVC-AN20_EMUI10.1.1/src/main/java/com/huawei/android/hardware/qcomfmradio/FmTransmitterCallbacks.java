package com.huawei.android.hardware.qcomfmradio;

/* access modifiers changed from: package-private */
public interface FmTransmitterCallbacks {
    void FmTxEvContRDSGroupsComplete();

    void FmTxEvRDSGroupsAvailable();

    void FmTxEvRDSGroupsComplete();

    void FmTxEvRadioDisabled();

    void FmTxEvRadioEnabled();

    void FmTxEvRadioReset();

    void FmTxEvTuneStatusChange(int i);
}
