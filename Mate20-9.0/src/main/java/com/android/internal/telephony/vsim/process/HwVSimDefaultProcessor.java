package com.android.internal.telephony.vsim.process;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import java.util.Arrays;

public class HwVSimDefaultProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "VSimDefaultProcessor";
    protected HwVSimController mVSimController;

    public HwVSimDefaultProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    public void onEnter() {
        logd("onEnter");
        if (this.mVSimController != null) {
            setProcessAction(HwVSimController.ProcessAction.PROCESS_ACTION_NONE);
            allowDefaultData();
        }
    }

    public void onExit() {
        logd("onExit");
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

    /* access modifiers changed from: protected */
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i != 4) {
            if (i == 40) {
                onCmdEnableVSim(msg);
            } else if (i == 52) {
                onCmdDisableVSim(msg);
            } else if (i == 58) {
                onCmdSwitchWorkMode(msg);
            } else if (i == 61) {
                onCardReloadTimeout();
            } else if (i != 84) {
                switch (i) {
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
                    case 23:
                        onGetSimStateViaSysinfoExDone(msg);
                        break;
                    case HwVSimConstants.EVENT_NETWORK_SCAN_COMPLETED:
                        onNetworksScanDone(msg);
                        break;
                    case HwVSimConstants.CMD_GET_DEVSUBMODE:
                        onCmdGetDevSubMode(msg);
                        break;
                    case HwVSimConstants.EVENT_GET_DEVSUBMODE_DONE:
                        onGetDevSubModeDone(msg);
                        break;
                    case HwVSimConstants.CMD_GET_PREFERREDNETWORKTYPE:
                        onCmdGetPreferredNetworkType(msg);
                        break;
                    case HwVSimConstants.EVENT_GET_PREFERREDNETWORKTYPE_DONE:
                        onGetPreferredNetworkTypeDone(msg);
                        break;
                    case HwVSimConstants.CMD_GET_MODEMSUPPORTVSIMVER_INNER:
                        onCmdGetModemSupportVSimVersionInner(msg);
                        break;
                    case 30:
                        onGetModemSupportVSimVersionInnerDone(msg);
                        break;
                    default:
                        unhandledMessage(msg);
                        break;
                }
            } else {
                onCmdRestartRildForNvMatch();
            }
        }
        return false;
    }

    public boolean isAsyncResultValid(AsyncResult ar, Object cause) {
        if (ar == null) {
            doProcessException(null, null, cause);
            return false;
        }
        HwVSimRequest request = (HwVSimRequest) ar.userObj;
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

    private void onCmdEnableVSim(Message msg) {
        logd("onCmdEnableVSim");
        HwVSimRequest request = (HwVSimRequest) msg.obj;
        if (request == null) {
            cmdSem_release();
            return;
        }
        HwVSimController.EnableParam param = getEnableParam(request);
        if (param == null) {
            cmdSem_release();
            notifyResult(request, 3);
        } else if (this.mVSimController.isEnableProhibitByDisableRetry()) {
            logd("onCmdEnableVSim: fast fail due to prohibit by disable retry");
            cmdSem_release();
            notifyResult(request, 3);
        } else if (this.mVSimController.isVSimCauseCardReload()) {
            cmdSem_release();
            notifyResult(request, 5);
        } else {
            if (canProcessEnable(param.operation)) {
                setEnableRequest(request);
                transitionToState(2);
            } else {
                cmdSem_release();
                if (param.operation == 5) {
                    notifyResult(request, 3);
                } else {
                    notifyResult(request, 5);
                }
            }
        }
    }

    private void onCmdDisableVSim(Message msg) {
        logd("onCmdDisableVSim");
        setDisableRequest((HwVSimRequest) msg.obj);
        transitionToState(5);
    }

    private void onCmdSwitchWorkMode(Message msg) {
        logd("onCmdSwitchWorkMode");
        HwVSimRequest request = (HwVSimRequest) msg.obj;
        if (request == null) {
            cmdSem_release();
            return;
        }
        HwVSimController.WorkModeParam param = getWorkModeParam(request);
        if (param == null) {
            cmdSem_release();
            notifyResult(request, false);
            return;
        }
        boolean isEnabled = isVSimEnabled();
        logd("onCmdSwitchWorkMode : isEnabled = " + isEnabled + ", old mode = " + param.oldMode + ", new mode = " + param.workMode + ", isHotplug = " + param.isHotplug);
        if ((param.workMode != param.oldMode || param.isHotplug) && isEnabled) {
            if (canProcessSwitchMode()) {
                setSwitchModeRequest(request);
                transitionToState(11);
            } else {
                notifyResult(request, false);
                cmdSem_release();
            }
            return;
        }
        notifyResult(request, true);
        cmdSem_release();
    }

    private void onCmdGetReservedPlmn(Message msg) {
        logd("onCmdGetReservedPlmn");
        this.mModemAdapter.getRegPlmn(this, (HwVSimRequest) msg.obj);
    }

    private void onCmdGetTrafficData(Message msg) {
        logd("onCmdGetTrafficData");
        this.mModemAdapter.getTrafficData(this, (HwVSimRequest) msg.obj);
    }

    private void onGetReservedPlmnDone(Message msg) {
        logd("onGetReservedPlmnDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null) {
            logd("onGetReservedPlmnDone, ar is null");
            return;
        }
        HwVSimRequest request = (HwVSimRequest) ar.userObj;
        Object result = "";
        if (ar.exception == null && ar.result != null) {
            result = ar.result;
        }
        logd("onGetReservedPlmnDone, result = " + result);
        notifyResult(request, result);
    }

    private void onGetTrafficDataDone(Message msg) {
        logd("onGetReservedTrafficDataDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar)) {
            notifyResult((HwVSimRequest) ar.userObj, ar.result);
        }
    }

    private void onCmdClearTrafficData(Message msg) {
        logd("onCmdClearTrafficData");
        this.mModemAdapter.clearTrafficData(this, (HwVSimRequest) msg.obj);
    }

    private void onClearTrafficDataDone(Message msg) {
        logd("onClearTrafficDataDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar, false)) {
            notifyResult((HwVSimRequest) ar.userObj, true);
        }
    }

    private void onCmdSetApDsFlowCfg(Message msg) {
        logd("onCmdSetApDsFlowCfg");
        this.mModemAdapter.setApDsFlowCfg(this, (HwVSimRequest) msg.obj);
    }

    private void onSetApDsFlowCfgDone(Message msg) {
        logd("onSetApDsFlowCfgDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar, false)) {
            notifyResult((HwVSimRequest) ar.userObj, true);
        }
    }

    private void onCmdSetDsFlowNvCfg(Message msg) {
        logd("onCmdSetDsFlowNvCfg");
        this.mModemAdapter.setDsFlowNvCfg(this, (HwVSimRequest) msg.obj);
    }

    private void onSetDsFlowNvCfgDone(Message msg) {
        logd("onSetDsFlowNvCfgDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar, false)) {
            notifyResult((HwVSimRequest) ar.userObj, true);
        }
    }

    private void onCmdGetSimStateViaSysinfoEx(Message msg) {
        logd("onCmdGetSimStateViaSysinfoEx");
        this.mModemAdapter.getSimStateViaSysinfoEx(this, (HwVSimRequest) msg.obj);
    }

    private void onGetSimStateViaSysinfoExDone(Message msg) {
        logd("onGetSimStateViaSysinfoExDone");
        int result = -1;
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar, -1)) {
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            if (ar.result != null && ((int[]) ar.result).length > 5) {
                result = ((int[]) ar.result)[5];
            }
            notifyResult(request, Integer.valueOf(result));
        }
    }

    private void onCmdGetDevSubMode(Message msg) {
        logd("onCmdGetDevSubMode");
        this.mModemAdapter.getDevSubMode(this, (HwVSimRequest) msg.obj);
    }

    private void onGetDevSubModeDone(Message msg) {
        logd("onGetDevSubModeDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar)) {
            String result = "";
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            if (ar.result != null) {
                result = Arrays.toString((String[]) ar.result);
            }
            notifyResult(request, result);
        }
    }

    private void onCmdGetPreferredNetworkType(Message msg) {
        logd("onCmdGetPreferredNetworkType");
        this.mModemAdapter.getPreferredNetworkTypeVSim(this, (HwVSimRequest) msg.obj);
    }

    private void onGetPreferredNetworkTypeDone(Message msg) {
        logd("onGetPreferredNetworkTypeDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar)) {
            String result = "";
            HwVSimRequest request = (HwVSimRequest) ar.userObj;
            if (ar.result != null) {
                result = rilRatToString(((int[]) ar.result)[0]);
            }
            notifyResult(request, result);
        }
    }

    private void onCmdGetModemSupportVSimVersionInner(Message msg) {
        logd("onCmdGetModemSupportVSimVersionInner");
        this.mModemAdapter.getModemSupportVSimVersionInner(this, (HwVSimRequest) msg.obj);
    }

    private void onGetModemSupportVSimVersionInnerDone(Message msg) {
        logd("onGetModemSupportVSimVersionInnerDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar != null) {
            notifyResult((HwVSimRequest) ar.userObj, Integer.valueOf(this.mModemAdapter.parseModemSupportVSimVersionResult(this, ar)));
        }
    }

    private void onCmdSetApnReady(Message msg) {
        logd("onCmdSetApnReady");
        notifyResult((HwVSimRequest) msg.obj, 0);
    }

    private void onSetApnReadyDone(Message msg) {
        logd("onSetApnReadyDone");
        AsyncResult ar = (AsyncResult) msg.obj;
        if (isAsyncResultValid(ar, 4)) {
            notifyResult((HwVSimRequest) ar.userObj, 0);
        }
    }

    private void onCardReloadTimeout() {
        logd("onCardReloadTimeout");
        this.mVSimController.clearAllMarkForCardReload();
        this.mVSimController.setVSimCauseCardReload(false);
    }

    private void onNetworksScanDone(Message msg) {
        logd("onNetworksScanDone");
        AsyncResult ar = (AsyncResult) msg.obj;
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
        return this.mVSimController != null && this.mVSimController.canProcessEnable(operation);
    }

    private boolean canProcessSwitchMode() {
        return this.mVSimController != null && this.mVSimController.canProcessSwitchMode();
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
        if (rat == 9) {
            return "LTE, GSM/WCDMA";
        }
        switch (rat) {
            case 0:
                return "Auto";
            case 1:
                return "GSM only";
            case 2:
                return "WCDMA only";
            case 3:
                return "GSM/WCDMA (auto)";
            default:
                switch (rat) {
                    case 11:
                        return "LTE Only";
                    case 12:
                        return "LTE/WCDMA";
                    default:
                        return "(unknown)";
                }
        }
    }

    private HwVSimController.EnableParam getEnableParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getEnableParam(request);
    }

    private HwVSimController.WorkModeParam getWorkModeParam(HwVSimRequest request) {
        if (this.mVSimController == null) {
            return null;
        }
        return this.mVSimController.getWorkModeParam(request);
    }

    /* access modifiers changed from: protected */
    public boolean isVSimEnabled() {
        return this.mVSimController != null && this.mVSimController.isVSimEnabled();
    }

    private void cmdSem_release() {
        if (this.mVSimController != null) {
            this.mVSimController.cmdSem_release();
        }
    }
}
