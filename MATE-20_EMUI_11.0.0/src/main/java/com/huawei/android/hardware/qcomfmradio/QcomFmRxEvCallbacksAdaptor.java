package com.huawei.android.hardware.qcomfmradio;

import com.huawei.android.hardware.fmradio.FmRxEvCallbacks;
import com.huawei.android.hardware.fmradio.FmRxEvCallbacksAdaptor;

public class QcomFmRxEvCallbacksAdaptor implements FmRxEvCallbacks {
    private FmRxEvCallbacksAdaptor mFmRxEvCallbacksAdaptor;

    public QcomFmRxEvCallbacksAdaptor(FmRxEvCallbacksAdaptor fmRxEvCallbacksAdaptor) {
        this.mFmRxEvCallbacksAdaptor = fmRxEvCallbacksAdaptor;
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
    public void FmRxEvRadioTuneStatus(int freq) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRadioTuneStatus(freq);
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvRdsLockStatus(boolean rdsAvail) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvRdsLockStatus(rdsAvail);
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
    public void FmRxEvSearchInProgress() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSearchInProgress();
    }

    public void FmRxEvSearchCancelled() {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvSearchComplete(int freq) {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSearchComplete(freq);
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

    public void FmRxEvRTPlus() {
    }

    public void FmRxEvERTInfo() {
    }

    public void FmRxEvECCInfo() {
    }

    public void FmRxEvGetSignalThreshold(int val, int status) {
    }

    public void FmRxEvGetChDetThreshold(int val, int status) {
    }

    public void FmRxEvDefDataRead(int val, int status) {
    }

    public void FmRxEvGetBlend(int val, int status) {
    }

    public void FmRxEvSetChDetThreshold(int status) {
    }

    public void FmRxEvDefDataWrite(int status) {
    }

    public void FmRxEvSetBlend(int status) {
    }

    public void FmRxGetStationParam(int val, int status) {
    }

    public void FmRxGetStationDbgParam(int val, int status) {
    }

    public void FmRxEvEnableSlimbus(int status) {
    }

    @Override // com.huawei.android.hardware.fmradio.FmRxEvCallbacks
    public void FmRxEvSignalUpdate() {
        this.mFmRxEvCallbacksAdaptor.FmRxEvSignalUpdate();
    }
}
