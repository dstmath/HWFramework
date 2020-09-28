package com.huawei.android.hardware.fmradio;

import com.huawei.android.hardware.fmradio.common.FmUtils;
import com.huawei.android.hardware.hisifmradio.HisiFmRxEvCallbacksAdaptor;
import com.huawei.android.hardware.mtkfmradio.MtkFmRxEvCallbacksAdaptor;
import com.huawei.android.hardware.qcomfmradio.QcomFmRxEvCallbacksAdaptor;

public class FmRxEvCallbacksAdaptor {
    private static FmRxEvCallbacks mBaseFmRxEvCallbacks;
    public static FmRxEvCallbacksAdaptor mInstance;

    public FmRxEvCallbacksAdaptor() {
        mInstance = this;
        if (FmUtils.isMtkPlatform()) {
            mBaseFmRxEvCallbacks = new MtkFmRxEvCallbacksAdaptor(this);
        } else if (FmUtils.isQcomPlatform()) {
            mBaseFmRxEvCallbacks = new QcomFmRxEvCallbacksAdaptor(this);
        } else {
            mBaseFmRxEvCallbacks = new HisiFmRxEvCallbacksAdaptor(this);
        }
    }

    public FmRxEvCallbacks getBaseFmRxEvCallbacks() {
        return mBaseFmRxEvCallbacks;
    }

    public static FmRxEvCallbacksAdaptor getInstance() {
        return mInstance;
    }

    public void FmRxEvEnableReceiver() {
    }

    public void FmRxEvDisableReceiver() {
    }

    public void FmRxEvRadioTuneStatus(int freq) {
    }

    public void FmRxEvRdsLockStatus(boolean rdsAvail) {
    }

    public void FmRxEvSearchInProgress() {
    }

    public void FmRxEvSearchComplete(int freq) {
    }

    public void FmRxEvRdsPsInfo() {
    }

    public void FmRxEvRdsRtInfo() {
    }

    public void FmRxEvRdsAfInfo() {
    }

    public void FmRxEvRadioReset() {
    }

    public void FmRxEvStereoStatus(boolean stereo) {
    }

    public void FmRxEvServiceAvailable(boolean service) {
    }

    public void FmRxEvSearchListComplete() {
    }

    public void FmRxEvRdsGroupData() {
    }

    public void FmRxEvSignalUpdate() {
    }
}
