package com.huawei.internal.telephony.vsim.process;

import android.os.Message;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.android.internal.telephony.vsim.HwVSimModemAdapter;
import com.android.internal.telephony.vsim.HwVSimRequest;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.hwparttelephonyvsim.BuildConfig;
import com.huawei.internal.telephony.vsim.HwVSimMtkController;

public class HwVSimMtkDefaultProcessor extends HwVSimMtkProcessor {
    private static final String LOG_TAG = "HwVsimMtkDefaultProcess";

    public HwVSimMtkDefaultProcessor(HwVSimMtkController controller, HwVSimModemAdapter modemAdapter, HwVSimRequest request) {
        super(controller, modemAdapter, request);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onEnter() {
        logi("onEnter");
        setProcessAction(HwVSimConstants.ProcessAction.PROCESS_ACTION_NONE);
        this.mController.allowDefaultData();
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void onExit() {
        logi("onExit");
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public Message obtainMessage(int what, Object obj) {
        return this.mController.obtainMessage(what, obj);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void transitionToState(int state) {
        this.mController.transitionToState(state);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public void doProcessException(AsyncResultEx ar, HwVSimRequest request) {
        if (!(ar == null || ar.getException() == null)) {
            logi("error, exception " + ar.getException());
        }
        notifyResult(request, BuildConfig.FLAVOR);
    }

    private void doProcessException(AsyncResultEx ar, HwVSimRequest request, Object cause) {
        if (!(ar == null || ar.getException() == null)) {
            logi("error, exception " + ar.getException());
        }
        notifyResult(request, cause);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.internal.telephony.vsim.process.HwVSimMtkProcessor
    public void logi(String content) {
        HwVSimLog.info(LOG_TAG, content);
    }

    @Override // com.android.internal.telephony.vsim.process.HwVSimProcessor
    public boolean processMessage(Message msg) {
        int i = msg.what;
        if (i == 40) {
            onCmdEnableVsim(msg);
            return false;
        } else if (i == 52) {
            onCmdDisableVsim(msg);
            return false;
        } else if (i != 61) {
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
                            unhandledMessage(msg);
                            return false;
                    }
            }
        } else {
            onCardReloadTimeout();
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

    public int networksScan(int slotId) {
        this.mModemAdapter.networksScan(this, slotId);
        return 0;
    }

    private void onCmdEnableVsim(Message msg) {
        logi("onCmdEnableVsim");
        HwVSimRequest request = (HwVSimRequest) msg.obj;
        if (request == null) {
            cmdSemRelease();
            return;
        }
        HwVSimConstants.EnableParam param = this.mController.getEnableParam(request);
        if (param == null) {
            cmdSemRelease();
            notifyResult(request, 3);
        } else if (this.mController.isEnableProhibitByDisableRetry()) {
            logi("onCmdEnableVSim: fast fail due to prohibit by disable retry");
            cmdSemRelease();
            notifyResult(request, 3);
        } else if (this.mController.isVSimCauseCardReload()) {
            cmdSemRelease();
            notifyResult(request, 5);
        } else if (this.mController.canProcessEnable()) {
            this.mController.setEnableRequest(request);
            transitionToState(2);
        } else {
            cmdSemRelease();
            if (param.operation == 5) {
                notifyResult(request, 3);
            } else {
                notifyResult(request, 5);
            }
        }
    }

    private void onCmdGetPreferredNetworkType(Message msg) {
        logi("onCmdGetPreferredNetworkType");
        this.mModemAdapter.getPreferredNetworkTypeVSim(this, (HwVSimRequest) msg.obj);
    }

    private void onGetPreferredNetworkTypeDone(Message msg) {
        logi("onGetPreferredNetworkTypeDone");
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

    private void onCmdDisableVsim(Message msg) {
        logi("onCmdDisableVsim");
        this.mController.setDisableRequest((HwVSimRequest) msg.obj);
        transitionToState(5);
    }

    private void onCmdGetTrafficData(Message msg) {
        logi("onCmdGetTrafficData");
        this.mModemAdapter.getTrafficData(this, (HwVSimRequest) msg.obj);
    }

    private void onGetTrafficDataDone(Message msg) {
        logi("onGetReservedTrafficDataDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar)) {
            notifyResult((HwVSimRequest) ar.getUserObj(), ar.getResult());
        }
    }

    private void onCmdClearTrafficData(Message msg) {
        logi("onCmdClearTrafficData");
        this.mModemAdapter.clearTrafficData(this, (HwVSimRequest) msg.obj);
    }

    private void onClearTrafficDataDone(Message msg) {
        logi("onClearTrafficDataDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar, false)) {
            notifyResult((HwVSimRequest) ar.getUserObj(), true);
        }
    }

    private void onCmdSetApDsFlowCfg(Message msg) {
        logi("onCmdSetApDsFlowCfg");
        this.mModemAdapter.setApDsFlowCfg(this, (HwVSimRequest) msg.obj);
    }

    private void onSetApDsFlowCfgDone(Message msg) {
        logi("onSetApDsFlowCfgDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar, false)) {
            notifyResult((HwVSimRequest) ar.getUserObj(), true);
        }
    }

    private void onCmdSetDsFlowNvCfg(Message msg) {
        logi("onCmdSetDsFlowNvCfg");
        this.mModemAdapter.setDsFlowNvCfg(this, (HwVSimRequest) msg.obj);
    }

    private void onSetDsFlowNvCfgDone(Message msg) {
        logi("onSetDsFlowNvCfgDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (isAsyncResultValid(ar, false)) {
            notifyResult((HwVSimRequest) ar.getUserObj(), true);
        }
    }

    private void onCmdGetSimStateViaSysinfoEx(Message msg) {
        logi("onCmdGetSimStateViaSysinfoEx");
    }

    private void onGetSimStateViaSysinfoExDone(Message msg) {
        logi("onGetSimStateViaSysinfoExDone");
    }

    private void onCmdGetDevSubMode(Message msg) {
        logi("onCmdGetDevSubMode");
    }

    private void onGetDevSubModeDone(Message msg) {
        logi("onGetDevSubModeDone");
    }

    private void onCardReloadTimeout() {
        logi("onCardReloadTimeout");
    }

    private void onNetworksScanDone(Message msg) {
        logi("onNetworksScanDone");
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar == null) {
            logi("onNetworksScanDone : ar null");
        } else {
            broadcastQueryResults(ar);
        }
    }

    private void broadcastQueryResults(AsyncResultEx ar) {
        logi("broadcastQueryResults");
        if (this.mController != null) {
            this.mController.broadcastQueryResults(ar);
        }
    }

    private void allowDefaultData() {
        logi("allowDefaultData");
        if (this.mController != null) {
            this.mController.allowDefaultData();
        }
    }

    private String rilRatToString(int rat) {
        if (rat == 0) {
            return "Auto";
        }
        if (rat == 1) {
            return "GSM only";
        }
        if (rat == 2) {
            return "WCDMA only";
        }
        if (rat == 3) {
            return "GSM/WCDMA (auto)";
        }
        if (rat == 9) {
            return "LTE, GSM/WCDMA";
        }
        if (rat == 11) {
            return "LTE Only";
        }
        if (rat != 12) {
            return "(unknown)";
        }
        return "LTE/WCDMA";
    }

    private void cmdSemRelease() {
        this.mController.cmdSemRelease();
    }
}
