package com.huawei.android.hardware.fmradio;

interface FmRxEvCallbacks {
    void FmRxEvDisableReceiver();

    void FmRxEvEnableReceiver();

    void FmRxEvRadioReset();

    void FmRxEvRadioTuneStatus(int i);

    void FmRxEvRdsAfInfo();

    void FmRxEvRdsGroupData();

    void FmRxEvRdsLockStatus(boolean z);

    void FmRxEvRdsPsInfo();

    void FmRxEvRdsRtInfo();

    void FmRxEvSearchComplete(int i);

    void FmRxEvSearchInProgress();

    void FmRxEvSearchListComplete();

    void FmRxEvServiceAvailable(boolean z);

    void FmRxEvSignalUpdate();

    void FmRxEvStereoStatus(boolean z);
}
