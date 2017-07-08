package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.ProcessState;
import com.android.internal.telephony.vsim.HwVSimEventReport.VSimEventInfoUtils;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwVSimRcReadyProcessor extends HwVSimReadyProcessor {
    public static final String LOG_TAG = "VSimRcReadyProcessor";
    protected HwVSimRequest mReconnectRequest;

    public HwVSimRcReadyProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
        this.mReconnectRequest = null;
    }

    public void onEnter() {
        logd("onEnter");
        setProcessState(ProcessState.PROCESS_STATE_READY);
        this.mIsM0Ready = false;
        if (this.mVSimController != null) {
            this.mVSimController.setOnVsimRegPLMNSelInfo(this.mHandler, 65, null);
            this.mVSimController.registerNetStateReceiver();
        }
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(71, 60000);
        }
    }

    public void onExit() {
        logd("onExit");
        if (this.mHandler != null) {
            this.mHandler.removeMessages(71);
        }
        if (this.mVSimController != null) {
            this.mVSimController.unregisterNetStateReceiver();
            this.mVSimController.unSetOnVsimRegPLMNSelInfo(this.mHandler);
        }
        setProcessState(ProcessState.PROCESS_STATE_NONE);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case HwVSimConstants.EVENT_RADIO_POWER_ON_DONE /*46*/:
                onRadioPowerOnDone(msg);
                return true;
            case HwVSimConstants.EVENT_NETWORK_CONNECTED /*50*/:
                onNetworkConnected(msg);
                return true;
            case HwVSimConstants.EVENT_RECONNECT_DONE /*64*/:
                onReconnectDone(msg);
                return true;
            case HwVSimConstants.EVENT_VSIM_PLMN_SELINFO /*65*/:
                onPlmnSelInfoDone(msg);
                return true;
            case HwVSimConstants.EVENT_NETWORK_CONNECT_TIMEOUT /*71*/:
                onNetworkConnectTimeout(msg);
                return true;
            default:
                return false;
        }
    }

    protected void onReconnectDone(Message msg) {
        if (msg.obj != null && (msg.obj instanceof HwVSimRequest)) {
            this.mReconnectRequest = (HwVSimRequest) msg.obj;
        }
        logd("onReconnectDone mReconnectRequest:" + this.mReconnectRequest);
    }

    protected void onPlmnSelInfoDone(Message msg) {
        logd("onPlmnSelInfoDone");
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 15);
        AsyncResult ar = msg.obj;
        int flag = -1;
        int result = -1;
        if (ar.exception == null) {
            int[] response = ar.result;
            if (response.length != 0) {
                flag = response[0];
                result = response[1];
            }
        }
        logd("PLMNSELEINFO: " + flag + " " + result);
        if (flag == 1) {
            this.mIsM0Ready = true;
            if (result == 0) {
                if (this.mVSimController.isNetworkConnected() && this.mHandler != null) {
                    this.mHandler.obtainMessage(50).sendToTarget();
                }
                VSimEventInfoUtils.setPsRegTime(this.mVSimController.mEventInfo, SystemClock.elapsedRealtime() - this.mVSimController.mVSimEnterTime);
                return;
            } else if (this.mHandler != null) {
                this.mHandler.removeMessages(71);
                this.mHandler.obtainMessage(71).sendToTarget();
                return;
            } else {
                return;
            }
        }
        this.mIsM0Ready = false;
    }

    protected void onNetworkConnected(Message msg) {
        logd("onNetworkConnected mIsM0Ready: " + this.mIsM0Ready);
        VSimEventInfoUtils.setCauseType(this.mVSimController.mEventInfo, 16);
        if (this.mIsM0Ready) {
            if (this.mHandler != null) {
                this.mHandler.removeMessages(71);
            }
            this.mVSimController.unregisterNetStateReceiver();
            this.mIsM0Ready = false;
            if (!powerOnRadioM2()) {
                transitionToState(0);
            }
        }
    }

    protected void onNetworkConnectTimeout(Message msg) {
        logd("onNetworkConnectTimeout");
        this.mVSimController.unregisterNetStateReceiver();
        if (this.mHandler != null) {
            this.mHandler.removeMessages(65);
            this.mHandler.removeMessages(50);
        }
        this.mIsM0Ready = false;
        if (!powerOnRadioM2()) {
            transitionToState(0);
        }
    }

    private boolean powerOnRadioM2() {
        if (this.mReconnectRequest == null) {
            return false;
        }
        int insertedCount = this.mVSimController.getInsertedSimCount();
        boolean isSupportGsm = HwVSimUtilsInner.isVsimSupportGSM();
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        logd("powerOnRadioM2  insertedCount = " + insertedCount + ", isSupportGsm = " + isSupportGsm);
        if (insertedCount < phoneCount || (insertedCount == phoneCount && isSupportGsm)) {
            return false;
        }
        HwVSimRequest request = this.mReconnectRequest.clone();
        request.mSubId = this.mReconnectRequest.getMainSlot();
        this.mModemAdapter.radioPowerOn(this, request, request.mSubId);
        this.mReconnectRequest = null;
        return true;
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        doReconnectProcessException(ar, request);
    }

    protected void onRadioPowerOnDone(Message msg) {
        logd("onRadioPowerOnDone");
        transitionToState(0);
    }
}
