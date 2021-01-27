package com.huawei.android.hardware.mtkfmradio;

import com.huawei.android.hardware.fmradio.FmRxEvCallbacks;
import com.huawei.android.hardware.fmradio.FmRxEvCallbacksAdaptor;

public class MtkFmRxEvCallbacksAdaptor implements FmRxEvCallbacks {
    private FmRxEvCallbacksAdaptor mFmRxEvCallbacksAdaptor;

    public MtkFmRxEvCallbacksAdaptor(FmRxEvCallbacksAdaptor fmRxEvCallbacksAdaptor) {
        this.mFmRxEvCallbacksAdaptor = fmRxEvCallbacksAdaptor;
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRadioTuneStatus(int freq) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRadioTuneStatus(freq);
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRdsLockStatus(boolean rdsAvail) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRdsLockStatus(rdsAvail);
    }

    public void FmRxEvSearchCancelled() {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvSearchInProgress() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSearchInProgress();
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvSearchComplete(int freq) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSearchComplete(freq);
    }

    public void FmRxEvRdsInfo(int event) {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRdsPsInfo() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRdsPsInfo();
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRdsRtInfo() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRdsRtInfo();
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRdsAfInfo() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRdsAfInfo();
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvEnableReceiver() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvEnableReceiver();
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvDisableReceiver() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvDisableReceiver();
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRadioReset() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRadioReset();
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvStereoStatus(boolean stereo) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvStereoStatus(stereo);
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvServiceAvailable(boolean service) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvServiceAvailable(service);
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvSearchListComplete() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSearchListComplete();
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRdsGroupData() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRdsGroupData();
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvSignalUpdate() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSignalUpdate();
    }
}
