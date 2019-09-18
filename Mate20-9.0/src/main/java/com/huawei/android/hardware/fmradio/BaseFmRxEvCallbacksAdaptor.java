package com.huawei.android.hardware.fmradio;

public class BaseFmRxEvCallbacksAdaptor implements FmRxEvCallbacks {
    private FmRxEvCallbacksAdaptor mFmRxEvCallbacksAdaptor = FmRxEvCallbacksAdaptor.getInstance();

    public void FmRxEvEnableReceiver() {
    }

    public void FmRxEvDisableReceiver() {
    }

    public void FmRxEvRadioReset() {
    }

    public void FmRxEvRadioTuneStatus(int freq) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRadioTuneStatus(freq);
    }

    public void FmRxEvRdsLockStatus(boolean rdsAvail) {
    }

    public void FmRxEvStereoStatus(boolean stereo) {
    }

    public void FmRxEvServiceAvailable(boolean service) {
    }

    public void FmRxEvSearchInProgress() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSearchInProgress();
    }

    public void FmRxEvSearchComplete(int freq) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSearchComplete(freq);
    }

    public void FmRxEvSearchListComplete() {
    }

    public void FmRxEvRdsGroupData() {
    }

    public void FmRxEvRdsPsInfo() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRdsPsInfo();
    }

    public void FmRxEvRdsRtInfo() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRdsRtInfo();
    }

    public void FmRxEvRdsAfInfo() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRdsAfInfo();
    }

    public void FmRxEvSignalUpdate() {
    }
}
