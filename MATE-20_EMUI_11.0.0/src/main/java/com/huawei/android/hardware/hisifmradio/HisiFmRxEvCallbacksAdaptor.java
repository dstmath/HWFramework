package com.huawei.android.hardware.hisifmradio;

import com.huawei.android.hardware.fmradio.FmRxEvCallbacks;
import com.huawei.android.hardware.fmradio.FmRxEvCallbacksAdaptor;

public class HisiFmRxEvCallbacksAdaptor implements FmRxEvCallbacks {
    private FmRxEvCallbacksAdaptor mFmRxEvCallbacksAdaptor;

    public HisiFmRxEvCallbacksAdaptor(FmRxEvCallbacksAdaptor fmRxEvCallbacksAdaptor) {
        this.mFmRxEvCallbacksAdaptor = fmRxEvCallbacksAdaptor;
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvEnableReceiver() {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvDisableReceiver() {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRadioReset() {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRadioTuneStatus(int freq) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRadioTuneStatus(freq);
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRdsLockStatus(boolean rdsAvail) {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvStereoStatus(boolean stereo) {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvServiceAvailable(boolean service) {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvSearchInProgress() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSearchInProgress();
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvSearchComplete(int freq) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSearchComplete(freq);
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvSearchListComplete() {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRdsGroupData() {
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
    public void FmRxEvSignalUpdate() {
    }
}
