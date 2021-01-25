package com.android.internal.telephony.vsim.process;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimController;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.hwparttelephonyvsim.BuildConfig;
import java.util.Arrays;

public class HwVSimDefaultProcessor extends HwVSimProcessor {
    public static final String LOG_TAG = "VSimDefaultProcessor";
    private Handler mHandler = this.mVSimController.getHandler();
    protected HwVSimController mVSimController;

    public HwVSimDefaultProcessor(HwVSimController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(modemAdapter, request);
        this.mVSimController = controller;
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        logd("onEnter");
        if (this.mVSimController != null) {
            setProcessAction(HwVSimController.ProcessAction.PROCESS_ACTION_NONE);
            allowDefaultData();
        }
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logd("onExit");
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public Message obtainMessage(int what, Object obj) {
        return this.mVSimController.obtainMessage(what, obj);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void transitionToState(int state) {
        this.mVSimController.transitionToState(state);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
        if (!(ar == null || ar.getException() == null)) {
            logd("error, exception " + ar.getException());
        }
        notifyResult(request, BuildConfig.FLAVOR);
    }

    private void doProcessException(AsyncResultEx ar, HwVSimRequest request, Object cause) {
        if (!(ar == null || ar.getException() == null)) {
            logd("error, exception " + ar.getException());
        }
        notifyResult(request, cause);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i == 4) {
            return false;
        }
        if (i == 40) {
            onCmdEnableVSim(msg);
            return false;
        } else if (i == 52) {
            onCmdDisableVSim(msg);
            return false;
        } else if (i == 58) {
            onCmdSwitchWorkMode(msg);
            return false;
        } else if (i == 61) {
            onCardReloadTimeout();
            return false;
        } else if (i != 84) {
            switch (i) {
                case 12:
                    onCmdClearTrafficData(msg);
                    return false;
                case 13:
                    onClearTrafficDataDone(msg);
                    return false;
                case 14:
                    onCmdGetTrafficData(msg);
                    return false;
                case 15:
                    onGetTrafficDataDone(msg);
                    return false;
                case 16:
                    onCmdSetApDsFlowCfg(msg);
                    return false;
                case 17:
                    onSetApDsFlowCfgDone(msg);
                    return false;
                case 18:
                    onCmdSetDsFlowNvCfg(msg);
                    return false;
                case 19:
                    onSetDsFlowNvCfgDone(msg);
                    return false;
                case 20:
                    onCmdSetApnReady(msg);
                    return false;
                default:
                    switch (i) {
                        case 22:
                            onCmdGetSimStateViaSysinfoEx(msg);
                            return false;
                        case 23:
                            onGetSimStateViaSysinfoExDone(msg);
                            return false;
                        case HwVSimConstants.EVENT_NETWORK_SCAN_COMPLETED /* 24 */:
                            onNetworksScanDone(msg);
                            return false;
                        case HwVSimConstants.CMD_GET_DEVSUBMODE /* 25 */:
                            onCmdGetDevSubMode(msg);
                            return false;
                        case HwVSimConstants.EVENT_GET_DEVSUBMODE_DONE /* 26 */:
                            onGetDevSubModeDone(msg);
                            return false;
                        case HwVSimConstants.CMD_GET_PREFERREDNETWORKTYPE /* 27 */:
                            onCmdGetPreferredNetworkType(msg);
                            return false;
                        case HwVSimConstants.EVENT_GET_PREFERREDNETWORKTYPE_DONE /* 28 */:
                            onGetPreferredNetworkTypeDone(msg);
                            return false;
                        default:
                            switch (i) {
                                case HwVSimConstants.EVENT_SEND_OPEN_SESSION_CONFIG_DONE /* 88 */:
                                    onSendOpenSessionConfigDone(msg);
                                    return false;
                                case HwVSimConstants.EVENT_SEND_OPEN_SESSION_CONFIG_TIMEOUT /* 89 */:
                                    onSendOpenSessionConfigTimeout(msg);
                                    return false;
                                default:
                                    switch (i) {
                                        case HwVSimConstants.EVENT_SEND_VSIM_DATA_TO_MODEM_DONE /* 91 */:
                                            onSendVsimDataToModemDone(msg);
                                            return false;
                                        case HwVSimConstants.EVENT_SEND_VSIM_DATA_TO_MODEM_TIMEOUT /* 92 */:
                                            onSendVsimDataToModemTimeout(msg);
                                            return false;
                                        default:
                                            unhandledMessage(msg);
                                            return false;
                                    }
                            }
                    }
            }
        } else {
            onCmdRestartRildForNvMatch();
            return false;
        }
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean isAsyncResultValid(AsyncResultEx ar, Object cause) {
        if (ar == null) {
            doProcessException(null, null, cause);
            return false;
        }
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        if (request == null) {
            return false;
        }
        if (ar.getException() == null) {
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
        } else if (canProcessEnable(param.operation)) {
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
        if ((param.workMode == param.oldMode && !param.isHotplug) || !isEnabled) {
            notifyResult(request, true);
            cmdSem_release();
        } else if (canProcessSwitchMode()) {
            setSwitchModeRequest(request);
            transitionToState(11);
        } else {
            notifyResult(request, false);
            cmdSem_release();
        }
    }

    private void onCmdGetTrafficData(Message msg) {
        logd("onCmdGetTrafficData");
        this.mModemAdapter.getTrafficData(this, (HwVSimRequest) msg.obj);
    }

    private void onGetTrafficDataDone(Message msg) {
        logd("onGetReservedTrafficDataDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            notifyResult((HwVSimRequest) ar.getUserObj(), ar.getResult());
        }
    }

    private void onCmdClearTrafficData(Message msg) {
        logd("onCmdClearTrafficData");
        this.mModemAdapter.clearTrafficData(this, (HwVSimRequest) msg.obj);
    }

    private void onClearTrafficDataDone(Message msg) {
        logd("onClearTrafficDataDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar, false)) {
            notifyResult((HwVSimRequest) ar.getUserObj(), true);
        }
    }

    private void onCmdSetApDsFlowCfg(Message msg) {
        logd("onCmdSetApDsFlowCfg");
        this.mModemAdapter.setApDsFlowCfg(this, (HwVSimRequest) msg.obj);
    }

    private void onSetApDsFlowCfgDone(Message msg) {
        logd("onSetApDsFlowCfgDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar, false)) {
            notifyResult((HwVSimRequest) ar.getUserObj(), true);
        }
    }

    private void onCmdSetDsFlowNvCfg(Message msg) {
        logd("onCmdSetDsFlowNvCfg");
        this.mModemAdapter.setDsFlowNvCfg(this, (HwVSimRequest) msg.obj);
    }

    private void onSetDsFlowNvCfgDone(Message msg) {
        logd("onSetDsFlowNvCfgDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar, false)) {
            notifyResult((HwVSimRequest) ar.getUserObj(), true);
        }
    }

    private void onCmdGetSimStateViaSysinfoEx(Message msg) {
        logd("onCmdGetSimStateViaSysinfoEx");
        this.mModemAdapter.getSimStateViaSysinfoEx(this, (HwVSimRequest) msg.obj);
    }

    private void onGetSimStateViaSysinfoExDone(Message msg) {
        logd("onGetSimStateViaSysinfoExDone");
        int result = -1;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar, -1)) {
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            if (ar.getResult() != null && ((int[]) ar.getResult()).length > 5) {
                result = ((int[]) ar.getResult())[5];
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
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            String result = BuildConfig.FLAVOR;
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            if (ar.getResult() != null) {
                result = Arrays.toString((String[]) ar.getResult());
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
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            String result = BuildConfig.FLAVOR;
            HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
            if (ar.getResult() != null) {
                result = rilRatToString(((int[]) ar.getResult())[0]);
            }
            notifyResult(request, result);
        }
    }

    private void onCmdSetApnReady(Message msg) {
        logd("onCmdSetApnReady");
        HwVSimRequest request = (HwVSimRequest) msg.obj;
        this.mModemAdapter.openChipSession(this, request, 2);
        this.mHandler.sendMessageDelayed(obtainMessage(89, request), 5000);
    }

    private void onSendOpenSessionConfigDone(Message msg) {
        logd("onSendOpenSessionConfigDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            logd("onSendOpenSessionConfigDone : ar null");
            return;
        }
        this.mHandler.removeMessages(89);
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        if (ar.getException() != null) {
            if (isRequestNotSupport(ar.getException())) {
                logd("request not support, just skip");
            } else {
                logd("open fail, try to close session");
                closeChipSessionWhenOpenFailOrTimeout(request, 11, false);
                return;
            }
        }
        Object argument = request.getArgument();
        if (!(argument instanceof HwVSimController.EnableParam)) {
            closeChipSessionWhenOpenFailOrTimeout(request, 1, false);
            return;
        }
        HwVSimController.EnableParam param = (HwVSimController.EnableParam) argument;
        int result = this.mVSimController.writeApnToTA(param.imsi, param.cardType, param.apnType, param.taPath, param.challenge, false, param.supportVsimCa);
        this.mVSimController.setIsTaOpen(true);
        if (result != 0) {
            closeChipSessionWhenOpenFailOrTimeout(request, result, true);
            return;
        }
        this.mModemAdapter.sendVsimDataToModem(this, request, 2);
        this.mHandler.sendEmptyMessageDelayed(92, 5000);
    }

    private void onSendOpenSessionConfigTimeout(Message msg) {
        logd("onSendOpenSessionConfigTimeout, mRequest = " + msg.obj);
        closeChipSessionWhenOpenFailOrTimeout((HwVSimRequest) msg.obj, 11, true);
    }

    private void onSendVsimDataToModemDone(Message msg) {
        logd("onSendVsimDataToModemDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            logd("onSendVsimDataToModemDone, ar null");
            return;
        }
        this.mHandler.removeMessages(92);
        HwVSimRequest request = (HwVSimRequest) ar.getUserObj();
        if (ar.getException() != null) {
            if (isRequestNotSupport(ar.getException())) {
                logd("request not support, just skip");
            } else {
                logd("send fail, try to close session");
                closeChipSessionWhenOpenFailOrTimeout(request, 11, true);
                return;
            }
        }
        int result = this.mVSimController.writeVsimToTA(4, (HwVSimController.EnableParam) request.getArgument());
        if (result != 0) {
            doEnableProcessException(ar, request, Integer.valueOf(result));
            this.mVSimController.closeTaSafely(request);
            return;
        }
        closeChipSessionWhenSuccess(request);
    }

    private void onSendVsimDataToModemTimeout(Message msg) {
        logd("onSendVsimDataToModemTimeout, mRequest = " + msg.obj);
        closeChipSessionWhenOpenFailOrTimeout((HwVSimRequest) msg.obj, 11, true);
    }

    private void closeChipSessionWhenOpenFailOrTimeout(HwVSimRequest request, int failReason, boolean isNeedCloseTa) {
        this.mModemAdapter.closeChipSession(2);
        if (isNeedCloseTa) {
            this.mVSimController.closeTaSafely(request);
        }
        notifyResult(request, Integer.valueOf(failReason));
    }

    private void closeChipSessionWhenSuccess(HwVSimRequest request) {
        this.mModemAdapter.closeChipSession(2);
        this.mVSimController.closeTaSafely(request);
        notifyResult(request, 0);
    }

    private void onCardReloadTimeout() {
        logd("onCardReloadTimeout");
        this.mVSimController.clearAllMarkForCardReload();
        this.mVSimController.setVSimCauseCardReload(false);
    }

    private void onNetworksScanDone(Message msg) {
        logd("onNetworksScanDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
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

    private void broadcastQueryResults(AsyncResultEx ar) {
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
