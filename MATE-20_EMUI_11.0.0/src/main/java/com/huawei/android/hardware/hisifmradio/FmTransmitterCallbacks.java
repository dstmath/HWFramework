package com.huawei.android.hardware.hisifmradio;

interface FmTransmitterCallbacks {
    void onContRDSGroupsComplete();

    void onRDSGroupsAvailable();

    void onRDSGroupsComplete();

    void onRadioDisabled();

    void onTuneStatusChange(int i);
}
