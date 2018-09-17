package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimController.EnableParam;
import com.android.internal.telephony.vsim.HwVSimController.ProcessAction;
import com.android.internal.telephony.vsim.HwVSimController.WorkModeParam;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;
import java.util.Arrays;

public class HwVSimDefaultProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "VSimDefaultProcessor";
    protected Handler mHandler = this.mVSimController.getHandler();
    protected HwVSimController mVSimController;

    public HwVSimDefaultProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    public void onEnter() {
        logd("onEnter");
        if (this.mVSimController != null) {
            setProcessAction(ProcessAction.PROCESS_ACTION_NONE);
            this.mVSimController.registerForVSimOn(this.mHandler, 62, null);
            this.mVSimController.registerForVSimNotAvailable(this.mHandler, 63, null);
            allowDefaultData();
        }
    }

    public void onExit() {
        logd("onExit");
        if (this.mVSimController != null) {
            this.mVSimController.unregisterForVSimOn(this.mHandler);
            this.mVSimController.unregisterForVSimNotAvailable(this.mHandler);
        }
    }

    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    public void doProcessException(AsyncResult ar, HwVSimRequest request) {
        if (!(ar == null || ar.exception == null)) {
            logd("error, exception " + ar.exception);
        }
        notifyResult(request, "");
    }

    private void doProcessException(AsyncResult ar, HwVSimRequest request, Object cause) {
        if (!(ar == null || ar.exception == null)) {
            logd("error, exception " + ar.exception);
        }
        notifyResult(request, cause);
    }

    protected void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    public boolean processMessage(Message msg) {
        switch (msg.what) {
            case 4:
                break;
            case 10:
                onCmdGetReservedPlmn(msg);
                break;
            case 11:
                onGetReservedPlmnDone(msg);
                break;
            case 12:
                onCmdClearTrafficData(msg);
                break;
            case 13:
                onClearTrafficDataDone(msg);
                break;
            case 14:
                onCmdGetTrafficData(msg);
                break;
            case 15:
                onGetTrafficDataDone(msg);
                break;
            case 16:
                onCmdSetApDsFlowCfg(msg);
                break;
            case 17:
                onSetApDsFlowCfgDone(msg);
                break;
            case 18:
                onCmdSetDsFlowNvCfg(msg);
                break;
            case 19:
                onSetDsFlowNvCfgDone(msg);
                break;
            case 20:
                onCmdSetApnReady(msg);
                break;
            case 21:
                onSetApnReadyDone(msg);
                break;
            case 22:
                onCmdGetSimStateViaSysinfoEx(msg);
                break;
            case HwVSimConstants.EVENT_GET_SIM_STATE_VIA_SYSINFOEX /*23*/:
                onGetSimStateViaSysinfoExDone(msg);
                break;
            case HwVSimConstants.EVENT_NETWORK_SCAN_COMPLETED /*24*/:
                onNetworksScanDone(msg);
                break;
            case HwVSimConstants.CMD_GET_DEVSUBMODE /*25*/:
                onCmdGetDevSubMode(msg);
                break;
            case HwVSimConstants.EVENT_GET_DEVSUBMODE_DONE /*26*/:
                onGetDevSubModeDone(msg);
                break;
            case HwVSimConstants.CMD_GET_PREFERREDNETWORKTYPE /*27*/:
                onCmdGetPreferredNetworkType(msg);
                break;
            case HwVSimConstants.EVENT_GET_PREFERREDNETWORKTYPE_DONE /*28*/:
                onGetPreferredNetworkTypeDone(msg);
                break;
            case HwVSimConstants.CMD_GET_MODEMSUPPORTVSIMVER_INNER /*29*/:
                onCmdGetModemSupportVSimVersionInner(msg);
                break;
            case 30:
                onGetModemSupportVSimVersionInnerDone(msg);
                break;
            case 40:
                onCmdEnableVSim(msg);
                break;
            case HwVSimConstants.CMD_DISABLE_VSIM /*52*/:
                onCmdDisableVSim(msg);
                break;
            case HwVSimConstants.CMD_SWITCH_WORKMODE /*58*/:
                onCmdSwitchWorkMode(msg);
                break;
            case HwVSimConstants.EVENT_CARD_RELOAD_TIMEOUT /*61*/:
                onCardReloadTimeout(msg);
                break;
            case HwVSimConstants.EVENT_RADIO_STATE_CHANGED_ON /*62*/:
                onRadioStateChangedOn(msg);
                break;
            case HwVSimConstants.EVENT_RADIO_STATE_CHANGED_NOTAVAILABLE /*63*/:
                onRadioStateChangedNotavailable(msg);
                break;
            case HwVSimConstants.EVENT_HV_CHECK_CARD /*69*/:
                onHvCheckCard(msg);
                break;
            case HwVSimConstants.EVENT_HV_CHECK_CARD_DONE /*70*/:
                onHvCheckCardDone(msg);
                break;
            case HwVSimConstants.CMD_RESTART_RILD_FOR_NV_MATCH /*84*/:
                onCmdRestartRildForNvMatch();
                break;
            default:
                unhandledMessage(msg);
                break;
        }
        return false;
    }

    public boolean isAsyncResultValid(AsyncResult ar, Object cause) {
        if (ar == null) {
            doProcessException(null, null, cause);
            return false;
        }
        HwVSimRequest request = ar.userObj;
        if (request == null) {
            return false;
        }
        if (ar.exception == null) {
            return true;
        }
        doProcessException(ar, request, cause);
        return false;
    }

    public int networksScan(int subId, int type) {
        this.mModemAdapter.networksScan(this);
        return 0;
    }

    protected void onRadioStateChangedOn(Message msg) {
    }

    protected void onRadioStateChangedNotavailable(Message msg) {
        if (!HwVSimUtilsInner.isPlatformRealTripple()) {
            if (HwVSimUtils.isVSimInProcess()) {
                logd("onRadioStateChangedNotavailable vsim in process, busy");
            } else if (HwVSimUtils.isVSimCauseCardReload()) {
                logd("onRadioStateChangedNotavailable vsim cause card reload, busy");
            } else if (HwVSimUtils.isVSimOn()) {
                if (!this.mVSimController.isVSimInProcess()) {
                    transitionToState(8);
                }
            } else {
                logd("onRadioStateChangedNotavailable vsim enbale process not end");
            }
        }
    }

    protected void onCmdEnableVSim(Message msg) {
        logd("onCmdEnableVSim");
        HwVSimRequest request = msg.obj;
        if (request == null) {
            cmdSem_release();
            return;
        }
        EnableParam param = getEnableParam(request);
        if (param == null) {
            cmdSem_release();
            notifyResult(request, Integer.valueOf(3));
        } else if (this.mVSimController.isEnableProhibitByDisableRetry()) {
            logd("onCmdEnableVSim: fast fail due to prohibit by disable retry");
            cmdSem_release();
            notifyResult(request, Integer.valueOf(3));
        } else if (this.mVSimController.isVSimCauseCardReload()) {
            cmdSem_release();
            notifyResult(request, Integer.valueOf(5));
        } else {
            if (canProcessEnable(param.operation)) {
                setEnableRequest(request);
                transitionToState(2);
            } else {
                cmdSem_release();
                if (param.operation == 5) {
                    notifyResult(request, Integer.valueOf(3));
                } else {
                    notifyResult(request, Integer.valueOf(5));
                }
            }
        }
    }

    protected void onCmdDisableVSim(Message msg) {
        logd("onCmdDisableVSim");
        setDisableRequest(msg.obj);
        transitionToState(5);
    }

    protected void onCmdSwitchWorkMode(Message msg) {
        logd("onCmdSwitchWorkMode");
        HwVSimRequest request = msg.obj;
        if (request == null) {
            cmdSem_release();
            return;
        }
        WorkModeParam param = getWorkModeParam(request);
        if (param == null) {
            cmdSem_release();
            notifyResult(request, Boolean.valueOf(false));
            return;
        }
        boolean isEnabled = isVSimEnabled();
        logd("onCmdSwitchWorkMode : isEnabled = " + isEnabled + ", old mode = " + param.oldMode + ", new mode = " + param.workMode + ", isHotplug = " + param.isHotplug);
        if ((param.workMode != param.oldMode || (param.isHotplug ^ 1) == 0) && (isEnabled ^ 1) == 0) {
            if (canProcessSwitchMode()) {
                setSwitchModeRequest(request);
                transitionToState(11);
            } else {
                notifyResult(request, Boolean.valueOf(false));
                cmdSem_release();
            }
            return;
        }
        notifyResult(request, Boolean.valueOf(true));
        cmdSem_release();
    }

    protected void onCmdGetReservedPlmn(Message msg) {
        logd("onCmdGetReservedPlmn");
        this.mModemAdapter.getRegPlmn(this, msg.obj);
    }

    protected void onCmdGetTrafficData(Message msg) {
        logd("onCmdGetTrafficData");
        this.mModemAdapter.getTrafficData(this, msg.obj);
    }

    protected void onGetReservedPlmnDone(Message msg) {
        logd("onGetReservedPlmnDone");
        AsyncResult ar = msg.obj;
        if (ar == null) {
            logd("onGetReservedPlmnDone, ar is null");
            return;
        }
        HwVSimRequest request = ar.userObj;
        Object result = "";
        if (ar.exception == null && ar.result != null) {
            result = ar.result;
        }
        logd("onGetReservedPlmnDone, result = " + result);
        notifyResult(request, result);
    }

    protected void onGetTrafficDataDone(Message msg) {
        logd("onGetReservedTrafficDataDone");
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            notifyResult(ar.userObj, ar.result);
        }
    }

    protected void onCmdClearTrafficData(Message msg) {
        logd("onCmdClearTrafficData");
        this.mModemAdapter.clearTrafficData(this, msg.obj);
    }

    protected void onClearTrafficDataDone(Message msg) {
        logd("onClearTrafficDataDone");
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar, Boolean.valueOf(false))) {
            notifyResult(ar.userObj, Boolean.valueOf(true));
        }
    }

    protected void onCmdSetApDsFlowCfg(Message msg) {
        logd("onCmdSetApDsFlowCfg");
        this.mModemAdapter.setApDsFlowCfg(this, msg.obj);
    }

    protected void onSetApDsFlowCfgDone(Message msg) {
        logd("onSetApDsFlowCfgDone");
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar, Boolean.valueOf(false))) {
            notifyResult(ar.userObj, Boolean.valueOf(true));
        }
    }

    protected void onCmdSetDsFlowNvCfg(Message msg) {
        logd("onCmdSetDsFlowNvCfg");
        this.mModemAdapter.setDsFlowNvCfg(this, msg.obj);
    }

    protected void onSetDsFlowNvCfgDone(Message msg) {
        logd("onSetDsFlowNvCfgDone");
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar, Boolean.valueOf(false))) {
            notifyResult(ar.userObj, Boolean.valueOf(true));
        }
    }

    protected void onCmdGetSimStateViaSysinfoEx(Message msg) {
        logd("onCmdGetSimStateViaSysinfoEx");
        this.mModemAdapter.getSimStateViaSysinfoEx(this, msg.obj);
    }

    protected void onGetSimStateViaSysinfoExDone(Message msg) {
        logd("onGetSimStateViaSysinfoExDone");
        int result = -1;
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar, Integer.valueOf(-1))) {
            HwVSimRequest request = ar.userObj;
            if (ar.result != null && ((int[]) ar.result).length > 5) {
                result = ((int[]) ar.result)[5];
            }
            notifyResult(request, Integer.valueOf(result));
        }
    }

    protected void onCmdGetDevSubMode(Message msg) {
        logd("onCmdGetDevSubMode");
        this.mModemAdapter.getDevSubMode(this, msg.obj);
    }

    protected void onGetDevSubModeDone(Message msg) {
        logd("onGetDevSubModeDone");
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            String result = "";
            HwVSimRequest request = ar.userObj;
            if (ar.result != null) {
                result = Arrays.toString((String[]) ar.result);
            }
            notifyResult(request, result);
        }
    }

    protected void onCmdGetPreferredNetworkType(Message msg) {
        logd("onCmdGetPreferredNetworkType");
        this.mModemAdapter.getPreferredNetworkTypeVSim(this, msg.obj);
    }

    protected void onGetPreferredNetworkTypeDone(Message msg) {
        logd("onGetPreferredNetworkTypeDone");
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar)) {
            String result = "";
            HwVSimRequest request = ar.userObj;
            if (ar.result != null) {
                result = rilRatToString(ar.result[0]);
            }
            notifyResult(request, result);
        }
    }

    protected void onCmdGetModemSupportVSimVersionInner(Message msg) {
        logd("onCmdGetModemSupportVSimVersionInner");
        this.mModemAdapter.getModemSupportVSimVersionInner(this, msg.obj);
    }

    protected void onGetModemSupportVSimVersionInnerDone(Message msg) {
        logd("onGetModemSupportVSimVersionInnerDone");
        AsyncResult ar = msg.obj;
        if (ar != null) {
            notifyResult(ar.userObj, Integer.valueOf(this.mModemAdapter.parseModemSupportVSimVersionResult(this, ar)));
        }
    }

    protected void onHvCheckCard(Message msg) {
        logd("onHvCheckCard");
        HwVSimRequest request = msg.obj;
        int subId = request.mSubId;
        if (!HwVSimUtils.isRadioAvailable(request.mSubId)) {
            subId = 2;
        }
        this.mModemAdapter.hvCheckCard(this, request, subId);
    }

    protected void onHvCheckCardDone(Message msg) {
        logd("onGetDevSubModeDone");
        boolean result = false;
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar, Boolean.valueOf(false))) {
            HwVSimRequest request = ar.userObj;
            if (ar.result != null && ((int[]) ar.result).length > 0) {
                int hvCheckCardResult = ((int[]) ar.result)[0];
                logd("hvCheckCardResult is: " + hvCheckCardResult);
                result = hvCheckCardResult == 0;
            }
            notifyResult(request, Boolean.valueOf(result));
        }
    }

    protected void onCmdSetApnReady(Message msg) {
        logd("onCmdSetApnReady");
        HwVSimRequest request = msg.obj;
        if (HwVSimUtilsInner.isPlatformRealTripple()) {
            notifyResult(request, Integer.valueOf(0));
        } else {
            this.mModemAdapter.setApnReady(this, request, 2);
        }
    }

    protected void onSetApnReadyDone(Message msg) {
        logd("onSetApnReadyDone");
        AsyncResult ar = msg.obj;
        if (isAsyncResultValid(ar, Integer.valueOf(4))) {
            notifyResult(ar.userObj, Integer.valueOf(0));
        }
    }

    protected void onCardReloadTimeout(Message msg) {
        logd("onCardReloadTimeout");
        this.mVSimController.clearAllMarkForCardReload();
        this.mVSimController.setVSimCauseCardReload(false);
    }

    protected void onNetworksScanDone(Message msg) {
        logd("onNetworksScanDone");
        AsyncResult ar = msg.obj;
        if (ar == null) {
            logd("onNetworksScanDone : ar null");
        } else {
            broadcastQueryResults(ar);
        }
    }

    private void onCmdRestartRildForNvMatch() {
        logd("onCmdRestartRildForNvMatch");
        if (this.mVSimController.canProcessRestartRild()) {
            this.mVSimController.transitionToState(14);
        }
    }

    private boolean canProcessEnable(int operation) {
        if (this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.canProcessEnable(operation);
    }

    private boolean canProcessSwitchMode() {
        if (this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.canProcessSwitchMode();
    }

    private void setEnableRequest(HwVSimRequest request) {
        if (this.mVSimController != null) {
            this.mVSimController.setEnableRequest(request);
        }
    }

    private void setDisableRequest(HwVSimRequest request) {
        if (this.mVSimController != null) {
            this.mVSimController.setDisableRequest(request);
        }
    }

    private void setSwitchModeRequest(HwVSimRequest request) {
        if (this.mVSimController != null) {
            this.mVSimController.setSwitchModeRequest(request);
        }
    }

    private void broadcastQueryResults(AsyncResult ar) {
        logd("broadcastQueryResults");
        if (this.mVSimController != null) {
            this.mVSimController.broadcastQueryResults(ar);
        }
    }

    private void allowDefaultData() {
        logd("allowDefaultData");
        if (this.mVSimController != null) {
            this.mVSimController.allowDefaultData();
        }
    }

    private String rilRatToString(int rat) {
        String ratString = "(unknown)";
        switch (rat) {
            case 0:
                return "Auto";
            case 1:
                return "GSM only";
            case 2:
                return "WCDMA only";
            case 3:
                return "GSM/WCDMA (auto)";
            case 9:
                return "LTE, GSM/WCDMA";
            case 11:
                return "LTE Only";
            case 12:
                return "LTE/WCDMA";
            default:
                return ratString;
        }
    }

    protected EnableParam getEnableParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getEnableParam(request);
    }

    protected WorkModeParam getWorkModeParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getWorkModeParam(request);
    }

    protected boolean isVSimEnabled() {
        if (this.mVSimController == null) {
            return false;
        }
        return this.mVSimController.isVSimEnabled();
    }

    protected void cmdSem_release() {
        if (this.mVSimController != null) {
            this.mVSimController.cmdSem_release();
        }
    }
}
