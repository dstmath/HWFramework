package com.huawei.android.hardware.fmradio;

public interface FmTransmitterCallbacks {
    void onContRDSGroupsComplete();

    void onRDSGroupsAvailable();

    void onRDSGroupsComplete();

    void onRadioDisabled();

    void onTuneStatusChange(int i);
}
