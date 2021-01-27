package com.android.internal.telephony;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyvsim.BuildConfig;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.ServiceStateTrackerEx;

/* access modifiers changed from: package-private */
public final class HwVSimServiceStateTracker extends ServiceStateTrackerEx {
    private static final int EVENT_NETWORK_REJECTED_CASE = 106;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 105;
    private static final int EVENT_VSIM_AP_TRAFFIC = 103;
    private static final int EVENT_VSIM_BASE = 100;
    private static final int EVENT_VSIM_PLMN_SELINFO = 102;
    private static final int EVENT_VSIM_RDH_NEEDED = 101;
    private static final int EVENT_VSIM_TIMER_TASK_EXPIRED = 104;
    private static final boolean HW_DBG = SystemPropertiesEx.getBoolean("ro.debuggable", false);
    private static final String LOG_TAG = "HwVSimServiceStateTracker";
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final int VSIM_AUTH_FAIL_CAUSE = 65538;
    private HwVsimServiceStateTrackerHandler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread("VSimSSTThread");
    private HwVSimUiccController mVSimUiccController;

    HwVSimServiceStateTracker(PhoneExt phone, CommandsInterfaceEx ci) {
        this.mHandlerThread.start();
        this.mHandler = new HwVsimServiceStateTrackerHandler(this.mHandlerThread.getLooper());
        initServiceStateTracker(phone, ci);
        initUiccController();
        registerForIccChanged();
        this.mCi.setOnVsimRDH(this.mHandler, 101, (Object) null);
        this.mCi.setOnVsimRegPLMNSelInfo(this.mHandler, 102, (Object) null);
        this.mCi.setOnVsimApDsFlowInfo(this.mHandler, 103, (Object) null);
        this.mCi.setOnVsimTimerTaskExpired(this.mHandler, (int) EVENT_VSIM_TIMER_TASK_EXPIRED, (Object) null);
        this.mCi.registerForRplmnsStateChanged(this.mHandler, (int) EVENT_RPLMNS_STATE_CHANGED, (Object) null);
        HwVsimServiceStateTrackerHandler hwVsimServiceStateTrackerHandler = this.mHandler;
        hwVsimServiceStateTrackerHandler.sendMessage(hwVsimServiceStateTrackerHandler.obtainMessage(EVENT_RPLMNS_STATE_CHANGED));
        this.mCi.setOnNetReject(this.mHandler, (int) EVENT_NETWORK_REJECTED_CASE, (Object) null);
    }

    public void dispose() {
        log("ServiceStateTracker dispose");
        this.mCi.unSetOnVsimRDH(this.mHandler);
        this.mCi.unSetOnVsimRegPLMNSelInfo(this.mHandler);
        this.mCi.unSetOnVsimApDsFlowInfo(this.mHandler);
        this.mCi.unSetOnVsimTimerTaskExpired(this.mHandler);
        this.mCi.unregisterForRplmnsStateChanged(this.mHandler);
        this.mCi.unSetOnNetReject(this.mHandler);
        unregisterForIccChanged();
        HandlerThread handlerThread = this.mHandlerThread;
        if (!(handlerThread == null || handlerThread.getLooper() == null)) {
            this.mHandlerThread.getLooper().quit();
        }
        this.mHandlerThread = null;
        this.mHandler = null;
    }

    private void initUiccController() {
        this.mVSimUiccController = HwVSimUiccController.getInstance();
    }

    private void registerForIccChanged() {
        this.mVSimUiccController.registerForIccChanged(getSSTHandler(), 42, null);
    }

    private void unregisterForIccChanged() {
        this.mVSimUiccController.unregisterForIccChanged(getSSTHandler());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        RlogEx.i(LOG_TAG, "[VSimSST] " + s);
    }

    private void loge(String s) {
        RlogEx.e(LOG_TAG, "[VSimSST] " + s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNetworkReject(AsyncResultEx ar) {
        if (ar.getException() == null) {
            String[] data = (String[]) ar.getResult();
            String rejectplmn = null;
            int rejectdomain = -1;
            int rejectcause = -1;
            int rejectrat = -1;
            if (data.length > 0) {
                try {
                    if (data[0] != null && data[0].length() > 0) {
                        rejectplmn = data[0];
                    }
                    if (data[1] != null && data[1].length() > 0) {
                        rejectdomain = Integer.parseInt(data[1]);
                    }
                    if (data[2] != null && data[2].length() > 0) {
                        rejectcause = Integer.parseInt(data[2]);
                    }
                    if (data[3] != null && data[3].length() > 0) {
                        rejectrat = Integer.parseInt(data[3]);
                    }
                } catch (NumberFormatException e) {
                    loge("Number format has exception");
                } catch (Exception e2) {
                    loge("error parsing NetworkReject!");
                }
                StringBuilder sb = new StringBuilder();
                sb.append("networkReject:PLMN = ");
                sb.append(HW_DBG ? rejectplmn : "***");
                sb.append(" domain = ");
                sb.append(rejectdomain);
                sb.append(" cause = ");
                sb.append(rejectcause);
                sb.append(" RAT = ");
                sb.append(rejectrat);
                log(sb.toString());
                sendBroadcastRejInfo(rejectcause, rejectplmn);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadcastRDHNeeded() {
        log("sendBroadcastRDHNeeded");
        Intent intent = new Intent("com.huawei.vsim.action.NEED_NEGOTIATION");
        int subId = getPhoneId();
        intent.putExtra("subId", subId);
        log("subId: " + subId);
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    public void sendBroadcastRejInfo(int rejectcause, String plmn) {
        log("sendBroadcastRejInfo");
        int subId = getPhoneId();
        Intent intent = new Intent("com.huawei.vsim.action.SIM_REJINFO_ACTION");
        intent.putExtra("subId", subId);
        intent.putExtra("errcode", rejectcause);
        intent.putExtra("plmn", plmn);
        StringBuilder sb = new StringBuilder();
        sb.append("subId: ");
        sb.append(subId);
        sb.append(", rejectcause: ");
        sb.append(rejectcause);
        sb.append(", plmn: ");
        sb.append(HW_DBG ? plmn : "***");
        log(sb.toString());
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    public void sendBroadcastRPLMNChanged(String rplmn) {
        StringBuilder sb = new StringBuilder();
        sb.append("sendBroadcastRPLMNChanged rplmn: ");
        String str = "***";
        sb.append(HW_DBG ? rplmn : str);
        log(sb.toString());
        int subId = getPhoneId();
        Intent intent = new Intent("com.huawei.vsim.action.SIM_RESIDENT_PLMN");
        intent.putExtra("subId", subId);
        intent.putExtra("resident", rplmn);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("subId: ");
        sb2.append(subId);
        sb2.append(" resident: ");
        if (HW_DBG) {
            str = rplmn;
        }
        sb2.append(str);
        log(sb2.toString());
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadcastRegPLMNSelInfo(int flag, int result) {
        log("sendBroadcastRegPLMNSelInfo");
        Intent intent = new Intent("com.huawei.vsim.action.SIM_PLMN_SELINFO");
        int subId = getPhoneId();
        intent.putExtra("subId", subId);
        intent.putExtra("flag", flag);
        intent.putExtra("res", result);
        log("subId: " + subId + " flag: " + flag + " result: " + result);
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadcastApTraffic(String curr_ds_time, String tx_rate, String rx_rate, String curr_tx_flow, String curr_rx_flow, String total_tx_flow, String total_rx_flow) {
        log("sendBroadcastApTraffic");
        Intent intent = new Intent("com.huawei.vsim.action.SIM_TRAFFIC");
        int subId = getPhoneId();
        intent.putExtra("subId", subId);
        intent.putExtra("curr_ds_time", curr_ds_time);
        intent.putExtra("tx_rate", tx_rate);
        intent.putExtra("rx_rate", rx_rate);
        intent.putExtra("curr_tx_flow", curr_tx_flow);
        intent.putExtra("curr_rx_flow", curr_rx_flow);
        intent.putExtra("total_tx_flow", total_tx_flow);
        intent.putExtra("total_rx_flow", total_rx_flow);
        log("subId: " + subId + " curr_ds_time: " + curr_ds_time + " tx_rate: " + tx_rate + " rx_rate: " + rx_rate + " curr_tx_flow: " + curr_tx_flow + " curr_rx_flow: " + curr_rx_flow + " total_tx_flow: " + total_tx_flow + " total_rx_flow: " + total_rx_flow);
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadcastTimerTaskExpired(int type) {
        log("sendBroadcastTimerTaskExpired");
        Intent intent = new Intent("com.huawei.vsim.action.TIMERTASK_EXPIRED_ACTION");
        int subId = getPhoneId();
        intent.putExtra("subId", subId);
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_TYPE, type);
        log("subId: " + subId + " type: " + type);
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    /* access modifiers changed from: private */
    public class HwVsimServiceStateTrackerHandler extends Handler {
        HwVsimServiceStateTrackerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    HwVSimServiceStateTracker.this.log("EVENT_VSIM_RDH_NEEDED");
                    HwVSimServiceStateTracker.this.sendBroadcastRDHNeeded();
                    return;
                case 102:
                    handleVsimPlmnSelinfo(msg);
                    return;
                case 103:
                    HwVSimServiceStateTracker.this.log("EVENT_VSIM_AP_TRAFFIC");
                    AsyncResultEx ar = AsyncResultEx.from(msg.obj);
                    if (ar.getException() == null) {
                        String[] response = (String[]) ar.getResult();
                        if (response.length != 0) {
                            HwVSimServiceStateTracker.this.sendBroadcastApTraffic(response[0], response[1], response[2], response[3], response[4], response[5], response[6]);
                            return;
                        }
                        return;
                    }
                    return;
                case HwVSimServiceStateTracker.EVENT_VSIM_TIMER_TASK_EXPIRED /* 104 */:
                    HwVSimServiceStateTracker.this.log("EVENT_VSIM_TIMER_TASK_EXPIRED");
                    AsyncResultEx ar2 = AsyncResultEx.from(msg.obj);
                    if (ar2.getException() == null) {
                        HwVSimServiceStateTracker.this.sendBroadcastTimerTaskExpired(((int[]) ar2.getResult())[0]);
                        return;
                    }
                    return;
                case HwVSimServiceStateTracker.EVENT_RPLMNS_STATE_CHANGED /* 105 */:
                    HwVSimServiceStateTracker.this.log("EVENT_RPLMNS_STATE_CHANGED");
                    HwVSimServiceStateTracker.this.sendBroadcastRPLMNChanged(SystemPropertiesEx.get(HwVSimServiceStateTracker.PROPERTY_GLOBAL_OPERATOR_NUMERIC, BuildConfig.FLAVOR));
                    return;
                case HwVSimServiceStateTracker.EVENT_NETWORK_REJECTED_CASE /* 106 */:
                    HwVSimServiceStateTracker.this.log("EVENT_NETWORK_REJECTED_CASE");
                    HwVSimServiceStateTracker.this.onNetworkReject(AsyncResultEx.from(msg.obj));
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }

        private void handleVsimPlmnSelinfo(Message msg) {
            HwVSimServiceStateTracker.this.log("EVENT_VSIM_PLMN_SELINFO");
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar != null && ar.getException() == null) {
                int[] response = (int[]) ar.getResult();
                if (response.length != 0) {
                    HwVSimServiceStateTracker.this.sendBroadcastRegPLMNSelInfo(response[0], response[1]);
                }
            }
        }
    }
}
