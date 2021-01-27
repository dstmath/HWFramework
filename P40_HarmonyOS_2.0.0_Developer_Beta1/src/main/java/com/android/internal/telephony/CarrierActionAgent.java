package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.huawei.internal.telephony.IccCardConstantsEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class CarrierActionAgent extends Handler {
    public static final int CARRIER_ACTION_REPORT_DEFAULT_NETWORK_STATUS = 3;
    public static final int CARRIER_ACTION_RESET = 2;
    public static final int CARRIER_ACTION_SET_METERED_APNS_ENABLED = 0;
    public static final int CARRIER_ACTION_SET_RADIO_ENABLED = 1;
    private static final boolean DBG = true;
    public static final int EVENT_APM_SETTINGS_CHANGED = 4;
    public static final int EVENT_APN_SETTINGS_CHANGED = 8;
    public static final int EVENT_DATA_ROAMING_OFF = 6;
    public static final int EVENT_MOBILE_DATA_SETTINGS_CHANGED = 5;
    public static final int EVENT_SIM_STATE_CHANGED = 7;
    private static final String LOG_TAG = "CarrierActionAgent";
    private static final boolean VDBG = Rlog.isLoggable(LOG_TAG, 2);
    private Boolean mCarrierActionOnMeteredApnEnabled = true;
    private Boolean mCarrierActionOnRadioEnabled = true;
    private Boolean mCarrierActionReportDefaultNetworkStatus = false;
    private RegistrantList mDefaultNetworkReportRegistrants = new RegistrantList();
    private RegistrantList mMeteredApnEnableRegistrants = new RegistrantList();
    private LocalLog mMeteredApnEnabledLog = new LocalLog(10);
    private final Phone mPhone;
    private RegistrantList mRadioEnableRegistrants = new RegistrantList();
    private LocalLog mRadioEnabledLog = new LocalLog(10);
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.CarrierActionAgent.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String iccState = intent.getStringExtra(IccCardConstantsEx.INTENT_KEY_ICC_STATE);
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action) && !intent.getBooleanExtra("rebroadcastOnUnlock", false)) {
                CarrierActionAgent carrierActionAgent = CarrierActionAgent.this;
                carrierActionAgent.sendMessage(carrierActionAgent.obtainMessage(7, iccState));
            }
        }
    };
    private LocalLog mReportDefaultNetworkStatusLog = new LocalLog(10);
    private final SettingsObserver mSettingsObserver;

    public CarrierActionAgent(Phone phone) {
        this.mPhone = phone;
        this.mPhone.getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        this.mSettingsObserver = new SettingsObserver(this.mPhone.getContext(), this);
        log("Creating CarrierActionAgent");
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        Boolean enabled = getCarrierActionEnabled(msg.what);
        if (enabled == null || enabled.booleanValue() != ((Boolean) msg.obj).booleanValue()) {
            int otaspState = 5;
            switch (msg.what) {
                case 0:
                    this.mCarrierActionOnMeteredApnEnabled = Boolean.valueOf(((Boolean) msg.obj).booleanValue());
                    log("SET_METERED_APNS_ENABLED: " + this.mCarrierActionOnMeteredApnEnabled);
                    this.mMeteredApnEnabledLog.log("SET_METERED_APNS_ENABLED: " + this.mCarrierActionOnMeteredApnEnabled);
                    if (this.mCarrierActionOnMeteredApnEnabled.booleanValue()) {
                        otaspState = this.mPhone.getServiceStateTracker().getOtasp();
                    }
                    this.mPhone.notifyOtaspChanged(otaspState);
                    this.mMeteredApnEnableRegistrants.notifyRegistrants(new AsyncResult((Object) null, this.mCarrierActionOnMeteredApnEnabled, (Throwable) null));
                    return;
                case 1:
                    this.mCarrierActionOnRadioEnabled = Boolean.valueOf(((Boolean) msg.obj).booleanValue());
                    log("SET_RADIO_ENABLED: " + this.mCarrierActionOnRadioEnabled);
                    this.mRadioEnabledLog.log("SET_RADIO_ENABLED: " + this.mCarrierActionOnRadioEnabled);
                    this.mRadioEnableRegistrants.notifyRegistrants(new AsyncResult((Object) null, this.mCarrierActionOnRadioEnabled, (Throwable) null));
                    return;
                case 2:
                    log("CARRIER_ACTION_RESET");
                    carrierActionReset();
                    return;
                case 3:
                    this.mCarrierActionReportDefaultNetworkStatus = Boolean.valueOf(((Boolean) msg.obj).booleanValue());
                    log("CARRIER_ACTION_REPORT_AT_DEFAULT_NETWORK_STATUS: " + this.mCarrierActionReportDefaultNetworkStatus);
                    this.mReportDefaultNetworkStatusLog.log("REGISTER_DEFAULT_NETWORK_STATUS: " + this.mCarrierActionReportDefaultNetworkStatus);
                    this.mDefaultNetworkReportRegistrants.notifyRegistrants(new AsyncResult((Object) null, this.mCarrierActionReportDefaultNetworkStatus, (Throwable) null));
                    return;
                case 4:
                    log("EVENT_APM_SETTINGS_CHANGED");
                    if (Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", 0) != 0) {
                        carrierActionReset();
                        return;
                    }
                    return;
                case 5:
                    log("EVENT_MOBILE_DATA_SETTINGS_CHANGED");
                    if (!this.mPhone.isUserDataEnabled()) {
                        carrierActionReset();
                        return;
                    }
                    return;
                case 6:
                    log("EVENT_DATA_ROAMING_OFF");
                    carrierActionReset();
                    return;
                case 7:
                    String iccState = (String) msg.obj;
                    if (IccCardConstantsEx.INTENT_VALUE_ICC_LOADED.equals(iccState)) {
                        log("EVENT_SIM_STATE_CHANGED status: " + iccState);
                        carrierActionReset();
                        String mobileData = "mobile_data";
                        if (TelephonyManager.getDefault().getSimCount() != 1) {
                            mobileData = mobileData + this.mPhone.getSubId();
                        }
                        this.mSettingsObserver.unobserve();
                        this.mSettingsObserver.observe(Settings.Global.getUriFor(mobileData), 5);
                        this.mSettingsObserver.observe(Settings.Global.getUriFor("airplane_mode_on"), 4);
                        this.mSettingsObserver.observe(Telephony.Carriers.CONTENT_URI, 8);
                        if (this.mPhone.getServiceStateTracker() != null) {
                            this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(this);
                            this.mPhone.getServiceStateTracker().registerForDataRoamingOff(this, 6, null, false);
                            return;
                        }
                        return;
                    } else if (IccCardConstantsEx.INTENT_VALUE_ICC_ABSENT.equals(iccState)) {
                        log("EVENT_SIM_STATE_CHANGED status: " + iccState);
                        carrierActionReset();
                        this.mSettingsObserver.unobserve();
                        if (this.mPhone.getServiceStateTracker() != null) {
                            this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(this);
                            return;
                        }
                        return;
                    } else {
                        return;
                    }
                case 8:
                    log("EVENT_APN_SETTINGS_CHANGED");
                    carrierActionReset();
                    return;
                default:
                    loge("Unknown carrier action: " + msg.what);
                    return;
            }
        }
    }

    public void carrierActionSetRadioEnabled(boolean enabled) {
        sendMessage(obtainMessage(1, Boolean.valueOf(enabled)));
    }

    public void carrierActionSetMeteredApnsEnabled(boolean enabled) {
        sendMessage(obtainMessage(0, Boolean.valueOf(enabled)));
    }

    public void carrierActionReportDefaultNetworkStatus(boolean report) {
        sendMessage(obtainMessage(3, Boolean.valueOf(report)));
    }

    public void carrierActionReset() {
        carrierActionReportDefaultNetworkStatus(false);
        carrierActionSetMeteredApnsEnabled(true);
        carrierActionSetRadioEnabled(true);
        this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(new Intent("com.android.internal.telephony.CARRIER_SIGNAL_RESET"));
    }

    private RegistrantList getRegistrantsFromAction(int action) {
        if (action == 0) {
            return this.mMeteredApnEnableRegistrants;
        }
        if (action == 1) {
            return this.mRadioEnableRegistrants;
        }
        if (action == 3) {
            return this.mDefaultNetworkReportRegistrants;
        }
        loge("Unsupported action: " + action);
        return null;
    }

    private Boolean getCarrierActionEnabled(int action) {
        if (action == 0) {
            return this.mCarrierActionOnMeteredApnEnabled;
        }
        if (action == 1) {
            return this.mCarrierActionOnRadioEnabled;
        }
        if (action == 3) {
            return this.mCarrierActionReportDefaultNetworkStatus;
        }
        loge("Unsupported action: " + action);
        return null;
    }

    public void registerForCarrierAction(int action, Handler h, int what, Object obj, boolean notifyNow) {
        Boolean carrierAction = getCarrierActionEnabled(action);
        if (carrierAction != null) {
            RegistrantList list = getRegistrantsFromAction(action);
            Registrant r = new Registrant(h, what, obj);
            list.add(r);
            if (notifyNow) {
                r.notifyRegistrant(new AsyncResult((Object) null, carrierAction, (Throwable) null));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("invalid carrier action: " + action);
    }

    public void unregisterForCarrierAction(Handler h, int action) {
        RegistrantList list = getRegistrantsFromAction(action);
        if (list != null) {
            list.remove(h);
            return;
        }
        throw new IllegalArgumentException("invalid carrier action: " + action);
    }

    @VisibleForTesting
    public ContentObserver getContentObserver() {
        return this.mSettingsObserver;
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    private void logv(String s) {
        Rlog.v(LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        pw.println(" mCarrierActionOnMeteredApnsEnabled Log:");
        ipw.increaseIndent();
        this.mMeteredApnEnabledLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        pw.println(" mCarrierActionOnRadioEnabled Log:");
        ipw.increaseIndent();
        this.mRadioEnabledLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        pw.println(" mCarrierActionReportDefaultNetworkStatus Log:");
        ipw.increaseIndent();
        this.mReportDefaultNetworkStatusLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
    }
}
