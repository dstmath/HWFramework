package com.android.internal.telephony;

import android.content.Intent;
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
    private static final String LOG_TAG = "HwVSimServiceStateTracker";
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final int VSIM_AUTH_FAIL_CAUSE = 65538;
    private HwVSimUiccController mVSimUiccController;

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: com.android.internal.telephony.HwVSimServiceStateTracker */
    /* JADX WARN: Multi-variable type inference failed */
    HwVSimServiceStateTracker(PhoneExt phone, CommandsInterfaceEx ci) {
        initServiceStateTracker(phone, ci);
        initUiccController();
        registerForIccChanged();
        this.mCi.setOnVsimRDH(this, (int) EVENT_VSIM_RDH_NEEDED, (Object) null);
        this.mCi.setOnVsimRegPLMNSelInfo(this, (int) EVENT_VSIM_PLMN_SELINFO, (Object) null);
        this.mCi.setOnVsimApDsFlowInfo(this, (int) EVENT_VSIM_AP_TRAFFIC, (Object) null);
        this.mCi.setOnVsimTimerTaskExpired(this, (int) EVENT_VSIM_TIMER_TASK_EXPIRED, (Object) null);
        this.mCi.registerForRplmnsStateChanged(this, (int) EVENT_RPLMNS_STATE_CHANGED, (Object) null);
        sendMessage(obtainMessage(EVENT_RPLMNS_STATE_CHANGED));
        this.mCi.setOnNetReject(this, (int) EVENT_NETWORK_REJECTED_CASE, (Object) null);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.android.internal.telephony.HwVSimServiceStateTracker */
    /* JADX WARN: Multi-variable type inference failed */
    public void dispose() {
        log("ServiceStateTracker dispose");
        this.mCi.unSetOnVsimRDH(this);
        this.mCi.unSetOnVsimRegPLMNSelInfo(this);
        this.mCi.unSetOnVsimApDsFlowInfo(this);
        this.mCi.unSetOnVsimTimerTaskExpired(this);
        this.mCi.unregisterForRplmnsStateChanged(this);
        this.mCi.unSetOnNetReject(this);
        unregisterForIccChanged();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_VSIM_RDH_NEEDED /*{ENCODED_INT: 101}*/:
                log("EVENT_VSIM_RDH_NEEDED");
                sendBroadcastRDHNeeded();
                return;
            case EVENT_VSIM_PLMN_SELINFO /*{ENCODED_INT: 102}*/:
                log("EVENT_VSIM_PLMN_SELINFO");
                AsyncResultEx ar = AsyncResultEx.from(msg.obj);
                if (ar.getException() == null) {
                    int[] response = (int[]) ar.getResult();
                    if (response.length != 0) {
                        sendBroadcastRegPLMNSelInfo(response[0], response[1]);
                        return;
                    }
                    return;
                }
                return;
            case EVENT_VSIM_AP_TRAFFIC /*{ENCODED_INT: 103}*/:
                log("EVENT_VSIM_AP_TRAFFIC");
                AsyncResultEx ar2 = AsyncResultEx.from(msg.obj);
                if (ar2.getException() == null) {
                    String[] response2 = (String[]) ar2.getResult();
                    if (response2.length != 0) {
                        sendBroadcastApTraffic(response2[0], response2[1], response2[2], response2[3], response2[4], response2[5], response2[6]);
                        return;
                    }
                    return;
                }
                return;
            case EVENT_VSIM_TIMER_TASK_EXPIRED /*{ENCODED_INT: 104}*/:
                log("EVENT_VSIM_TIMER_TASK_EXPIRED");
                AsyncResultEx ar3 = AsyncResultEx.from(msg.obj);
                if (ar3.getException() == null) {
                    sendBroadcastTimerTaskExpired(((int[]) ar3.getResult())[0]);
                    return;
                }
                return;
            case EVENT_RPLMNS_STATE_CHANGED /*{ENCODED_INT: 105}*/:
                log("EVENT_RPLMNS_STATE_CHANGED");
                sendBroadcastRPLMNChanged(SystemPropertiesEx.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, BuildConfig.FLAVOR));
                return;
            case EVENT_NETWORK_REJECTED_CASE /*{ENCODED_INT: 106}*/:
                log("EVENT_NETWORK_REJECTED_CASE");
                onNetworkReject(AsyncResultEx.from(msg.obj));
                return;
            default:
                HwVSimServiceStateTracker.super.handleMessage(msg);
                return;
        }
    }

    private void initUiccController() {
        this.mVSimUiccController = HwVSimUiccController.getInstance();
    }

    private void registerForIccChanged() {
        this.mVSimUiccController.registerForIccChanged(getSSTHandler(), EVENT_ICC_CHANGED, null);
    }

    private void unregisterForIccChanged() {
        this.mVSimUiccController.unregisterForIccChanged(getSSTHandler());
    }

    private void log(String s) {
        RlogEx.d(LOG_TAG, "[VSimSST] " + s);
    }

    private void loge(String s) {
        RlogEx.e(LOG_TAG, "[VSimSST] " + s);
    }

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
                log("NetworkReject:PLMN = " + rejectplmn + " domain = " + rejectdomain + " cause = " + rejectcause + " RAT = " + rejectrat);
                if (VSIM_AUTH_FAIL_CAUSE == rejectcause) {
                    sendBroadcastRejInfo(rejectcause, rejectplmn);
                }
            }
        }
    }

    private void sendBroadcastRDHNeeded() {
        log("sendBroadcastRDHNeeded");
        Intent intent = new Intent("com.huawei.vsim.action.NEED_NEGOTIATION");
        int subId = getPhoneId();
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, subId);
        log("subId: " + subId);
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    public void sendBroadcastRejInfo(int rejectcause, String plmn) {
        log("sendBroadcastRejInfo");
        int subId = getPhoneId();
        Intent intent = new Intent("com.huawei.vsim.action.SIM_REJINFO_ACTION");
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, subId);
        intent.putExtra("errcode", rejectcause);
        intent.putExtra("plmn", plmn);
        log("subId: " + subId + ", rejectcause: " + rejectcause + ", plmn: " + plmn);
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    public void sendBroadcastRPLMNChanged(String rplmn) {
        log("sendBroadcastRPLMNChanged rplmn: " + rplmn);
        int subId = getPhoneId();
        Intent intent = new Intent("com.huawei.vsim.action.SIM_RESIDENT_PLMN");
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, subId);
        intent.putExtra("resident", rplmn);
        log("subId: " + subId + " resident: " + rplmn);
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    private void sendBroadcastRegPLMNSelInfo(int flag, int result) {
        log("sendBroadcastRegPLMNSelInfo");
        Intent intent = new Intent("com.huawei.vsim.action.SIM_PLMN_SELINFO");
        int subId = getPhoneId();
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, subId);
        intent.putExtra("flag", flag);
        intent.putExtra("res", result);
        log("subId: " + subId + " flag: " + flag + " result: " + result);
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }

    private void sendBroadcastApTraffic(String curr_ds_time, String tx_rate, String rx_rate, String curr_tx_flow, String curr_rx_flow, String total_tx_flow, String total_rx_flow) {
        log("sendBroadcastApTraffic");
        Intent intent = new Intent("com.huawei.vsim.action.SIM_TRAFFIC");
        int subId = getPhoneId();
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, subId);
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

    private void sendBroadcastTimerTaskExpired(int type) {
        log("sendBroadcastTimerTaskExpired");
        Intent intent = new Intent("com.huawei.vsim.action.TIMERTASK_EXPIRED_ACTION");
        int subId = getPhoneId();
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, subId);
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_TYPE, type);
        log("subId: " + subId + " type: " + type);
        getPhone().getContext().sendBroadcast(intent, HwVSimConstants.VSIM_BUSSINESS_PERMISSION);
    }
}
