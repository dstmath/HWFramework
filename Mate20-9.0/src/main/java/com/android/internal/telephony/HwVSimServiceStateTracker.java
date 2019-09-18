package com.android.internal.telephony;

import android.content.Intent;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.vsim.HwVSimConstants;
import java.io.FileDescriptor;
import java.io.PrintWriter;

final class HwVSimServiceStateTracker extends HwVSimSSTBridge {
    private static final int EVENT_NETWORK_REJECTED_CASE = 106;
    private static final int EVENT_RPLMNS_STATE_CHANGED = 105;
    private static final int EVENT_VSIM_AP_TRAFFIC = 103;
    private static final int EVENT_VSIM_BASE = 100;
    private static final int EVENT_VSIM_PLMN_SELINFO = 102;
    private static final int EVENT_VSIM_RDH_NEEDED = 101;
    private static final int EVENT_VSIM_TIMER_TASK_EXPIRED = 104;
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final int VSIM_AUTH_FAIL_CAUSE = 65538;
    private static final boolean sIsPlatformSupportVSim = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private String LOG_TAG = "VSimSST";
    private HwVSimUiccController mVSimUiccController;

    /* JADX WARNING: type inference failed for: r3v0, types: [com.android.internal.telephony.HwVSimServiceStateTracker, android.os.Handler] */
    public HwVSimServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        super(phone, ci);
        this.LOG_TAG += "[VSIM]";
        this.mCi.setOnVsimRDH(this, EVENT_VSIM_RDH_NEEDED, null);
        this.mCi.setOnVsimRegPLMNSelInfo(this, EVENT_VSIM_PLMN_SELINFO, null);
        this.mCi.setOnVsimApDsFlowInfo(this, EVENT_VSIM_AP_TRAFFIC, null);
        this.mCi.setOnVsimTimerTaskExpired(this, EVENT_VSIM_TIMER_TASK_EXPIRED, null);
        this.mCi.registerForRplmnsStateChanged(this, EVENT_RPLMNS_STATE_CHANGED, null);
        sendMessage(obtainMessage(EVENT_RPLMNS_STATE_CHANGED));
        this.mCi.setOnNetReject(this, EVENT_NETWORK_REJECTED_CASE, null);
        if (sIsPlatformSupportVSim && getPhoneId() == 2) {
            initUiccController();
            registerForIccChanged();
        }
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [com.android.internal.telephony.HwVSimServiceStateTracker, android.os.Handler, com.android.internal.telephony.HwVSimSSTBridge] */
    public void dispose() {
        log("ServiceStateTracker dispose");
        this.mCi.unSetOnVsimRDH(this);
        this.mCi.unSetOnVsimRegPLMNSelInfo(this);
        this.mCi.unSetOnVsimApDsFlowInfo(this);
        this.mCi.unSetOnVsimTimerTaskExpired(this);
        this.mCi.unregisterForRplmnsStateChanged(this);
        this.mCi.unSetOnNetReject(this);
        if (sIsPlatformSupportVSim && getPhoneId() == 2) {
            unregisterForIccChanged();
        }
        super.dispose();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_VSIM_RDH_NEEDED /*101*/:
                log("EVENT_VSIM_RDH_NEEDED");
                sendBroadcastRDHNeeded();
                return;
            case EVENT_VSIM_PLMN_SELINFO /*102*/:
                log("EVENT_VSIM_PLMN_SELINFO");
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    int[] response = (int[]) ar.result;
                    if (response.length != 0) {
                        sendBroadcastRegPLMNSelInfo(response[0], response[1]);
                        return;
                    }
                    return;
                }
                return;
            case EVENT_VSIM_AP_TRAFFIC /*103*/:
                log("EVENT_VSIM_AP_TRAFFIC");
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception == null) {
                    String[] response2 = (String[]) ar2.result;
                    if (response2.length != 0) {
                        sendBroadcastApTraffic(response2[0], response2[1], response2[2], response2[3], response2[4], response2[5], response2[6]);
                        return;
                    }
                    return;
                }
                return;
            case EVENT_VSIM_TIMER_TASK_EXPIRED /*104*/:
                log("EVENT_VSIM_TIMER_TASK_EXPIRED");
                AsyncResult ar3 = (AsyncResult) msg.obj;
                if (ar3.exception == null) {
                    sendBroadcastTimerTaskExpired(((int[]) ar3.result)[0]);
                    return;
                }
                return;
            case EVENT_RPLMNS_STATE_CHANGED /*105*/:
                log("EVENT_RPLMNS_STATE_CHANGED");
                sendBroadcastRPLMNChanged(SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, ""));
                return;
            case EVENT_NETWORK_REJECTED_CASE /*106*/:
                log("EVENT_NETWORK_REJECTED_CASE");
                onNetworkReject((AsyncResult) msg.obj);
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void updateVSimOperatorProp() {
        if (this.mSS != null) {
            SystemProperties.set("gsm.operator.alpha.vsim", this.mSS.getOperatorAlphaLong());
        }
    }

    /* access modifiers changed from: protected */
    public UiccCardApplication getVSimUiccCardApplication() {
        HwVSimUiccController hwVSimUiccController = this.mVSimUiccController;
        HwVSimUiccController hwVSimUiccController2 = this.mVSimUiccController;
        return hwVSimUiccController.getUiccCardApplication(1);
    }

    /* access modifiers changed from: protected */
    public void initUiccController() {
        this.mVSimUiccController = HwVSimUiccController.getInstance();
    }

    /* JADX WARNING: type inference failed for: r3v0, types: [com.android.internal.telephony.HwVSimServiceStateTracker, android.os.Handler] */
    /* access modifiers changed from: protected */
    public void registerForIccChanged() {
        this.mVSimUiccController.registerForIccChanged(this, 42, null);
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.internal.telephony.HwVSimServiceStateTracker, android.os.Handler] */
    /* access modifiers changed from: protected */
    public void unregisterForIccChanged() {
        this.mVSimUiccController.unregisterForIccChanged(this);
    }

    /* access modifiers changed from: protected */
    public boolean isUiccControllerValid() {
        return this.mVSimUiccController != null;
    }

    /* access modifiers changed from: protected */
    public UiccCard getUiccCard() {
        return this.mVSimUiccController.getUiccCard();
    }

    /* access modifiers changed from: protected */
    public Intent createSpnIntent() {
        return new Intent("com.huawei.vsim.action.SPN_STRINGS_UPDATED_VSIM");
    }

    /* access modifiers changed from: protected */
    public void putPhoneIdAndSubIdExtra(Intent intent, int phoneId) {
        intent.putExtra("subscription", 2);
        intent.putExtra("phone", 2);
        intent.putExtra("slot", 2);
    }

    /* access modifiers changed from: protected */
    public void setNetworkCountryIsoForPhone(TelephonyManager tm, int phoneId, String iso) {
        SystemProperties.set("gsm.operator.iso-country.vsim", iso);
    }

    /* access modifiers changed from: protected */
    public String getNetworkOperatorForPhone(TelephonyManager tm, int phoneId) {
        return SystemProperties.get("gsm.operator.numeric.vsim", "");
    }

    /* access modifiers changed from: protected */
    public void setNetworkOperatorNumericForPhone(TelephonyManager tm, int phoneId, String numeric) {
        SystemProperties.set("gsm.operator.numeric.vsim", numeric);
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        String str = this.LOG_TAG;
        Rlog.d(str, "[VSimSST] " + s);
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        String str = this.LOG_TAG;
        Rlog.e(str, "[VSimSST] " + s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("VSimServiceStateTracker extends:");
        super.dump(fd, pw, args);
    }

    private void onNetworkReject(AsyncResult ar) {
        if (ar.exception == null) {
            String[] data = (String[]) ar.result;
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
                } catch (Exception ex) {
                    Rlog.e(this.LOG_TAG, "error parsing NetworkReject!", ex);
                }
                String str = this.LOG_TAG;
                Rlog.d(str, "NetworkReject:PLMN = " + rejectplmn + " domain = " + rejectdomain + " cause = " + rejectcause + " RAT = " + rejectrat);
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
        String str = curr_ds_time;
        String str2 = tx_rate;
        String str3 = rx_rate;
        String str4 = curr_tx_flow;
        String str5 = curr_rx_flow;
        String str6 = total_tx_flow;
        String str7 = total_rx_flow;
        log("sendBroadcastApTraffic");
        Object obj = HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID;
        Object obj2 = "curr_ds_time";
        Intent intent = new Intent("com.huawei.vsim.action.SIM_TRAFFIC");
        int subId = getPhoneId();
        Object obj3 = "tx_rate";
        intent.putExtra(HwVSimConstants.EXTRA_NETWORK_SCAN_SUBID, subId);
        intent.putExtra("curr_ds_time", str);
        intent.putExtra("tx_rate", str2);
        intent.putExtra("rx_rate", str3);
        intent.putExtra("curr_tx_flow", str4);
        intent.putExtra("curr_rx_flow", str5);
        intent.putExtra("total_tx_flow", str6);
        intent.putExtra("total_rx_flow", str7);
        StringBuilder sb = new StringBuilder();
        Object obj4 = "rx_rate";
        sb.append("subId: ");
        sb.append(subId);
        sb.append(" curr_ds_time: ");
        sb.append(str);
        sb.append(" tx_rate: ");
        sb.append(str2);
        sb.append(" rx_rate: ");
        sb.append(str3);
        sb.append(" curr_tx_flow: ");
        sb.append(str4);
        sb.append(" curr_rx_flow: ");
        sb.append(str5);
        sb.append(" total_tx_flow: ");
        sb.append(str6);
        sb.append(" total_rx_flow: ");
        sb.append(str7);
        log(sb.toString());
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
