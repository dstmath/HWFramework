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
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String iccState = intent.getStringExtra("ss");
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action) && !intent.getBooleanExtra("rebroadcastOnUnlock", false)) {
                CarrierActionAgent.this.sendMessage(CarrierActionAgent.this.obtainMessage(7, iccState));
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

    public void handleMessage(Message msg) {
        Boolean enabled = getCarrierActionEnabled(msg.what);
        if (enabled == null || enabled.booleanValue() != ((Boolean) msg.obj).booleanValue()) {
            switch (msg.what) {
                case 0:
                    this.mCarrierActionOnMeteredApnEnabled = Boolean.valueOf(((Boolean) msg.obj).booleanValue());
                    log("SET_METERED_APNS_ENABLED: " + this.mCarrierActionOnMeteredApnEnabled);
                    this.mMeteredApnEnabledLog.log("SET_METERED_APNS_ENABLED: " + this.mCarrierActionOnMeteredApnEnabled);
                    this.mMeteredApnEnableRegistrants.notifyRegistrants(new AsyncResult(null, this.mCarrierActionOnMeteredApnEnabled, null));
                    break;
                case 1:
                    this.mCarrierActionOnRadioEnabled = Boolean.valueOf(((Boolean) msg.obj).booleanValue());
                    log("SET_RADIO_ENABLED: " + this.mCarrierActionOnRadioEnabled);
                    this.mRadioEnabledLog.log("SET_RADIO_ENABLED: " + this.mCarrierActionOnRadioEnabled);
                    this.mRadioEnableRegistrants.notifyRegistrants(new AsyncResult(null, this.mCarrierActionOnRadioEnabled, null));
                    break;
                case 2:
                    log("CARRIER_ACTION_RESET");
                    carrierActionReset();
                    break;
                case 3:
                    this.mCarrierActionReportDefaultNetworkStatus = Boolean.valueOf(((Boolean) msg.obj).booleanValue());
                    log("CARRIER_ACTION_REPORT_AT_DEFAULT_NETWORK_STATUS: " + this.mCarrierActionReportDefaultNetworkStatus);
                    this.mReportDefaultNetworkStatusLog.log("REGISTER_DEFAULT_NETWORK_STATUS: " + this.mCarrierActionReportDefaultNetworkStatus);
                    this.mDefaultNetworkReportRegistrants.notifyRegistrants(new AsyncResult(null, this.mCarrierActionReportDefaultNetworkStatus, null));
                    break;
                case 4:
                    log("EVENT_APM_SETTINGS_CHANGED");
                    if (Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "airplane_mode_on", 0) != 0) {
                        carrierActionReset();
                        break;
                    }
                    break;
                case 5:
                    log("EVENT_MOBILE_DATA_SETTINGS_CHANGED");
                    if (!this.mPhone.isUserDataEnabled()) {
                        carrierActionReset();
                        break;
                    }
                    break;
                case 6:
                    log("EVENT_DATA_ROAMING_OFF");
                    carrierActionReset();
                    break;
                case 7:
                    String iccState = (String) msg.obj;
                    if (!"LOADED".equals(iccState)) {
                        if ("ABSENT".equals(iccState)) {
                            log("EVENT_SIM_STATE_CHANGED status: " + iccState);
                            carrierActionReset();
                            this.mSettingsObserver.unobserve();
                            if (this.mPhone.getServiceStateTracker() != null) {
                                this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(this);
                                break;
                            }
                        }
                    } else {
                        log("EVENT_SIM_STATE_CHANGED status: " + iccState);
                        carrierActionReset();
                        String mobileData = "mobile_data";
                        if (TelephonyManager.getDefault().getSimCount() != 1) {
                            mobileData = mobileData + this.mPhone.getSubId();
                        }
                        this.mSettingsObserver.observe(Settings.Global.getUriFor(mobileData), 5);
                        this.mSettingsObserver.observe(Settings.Global.getUriFor("airplane_mode_on"), 4);
                        this.mSettingsObserver.observe(Telephony.Carriers.CONTENT_URI, 8);
                        if (this.mPhone.getServiceStateTracker() != null) {
                            this.mPhone.getServiceStateTracker().registerForDataRoamingOff(this, 6, null, false);
                            break;
                        }
                    }
                    break;
                case 8:
                    log("EVENT_APN_SETTINGS_CHANGED");
                    carrierActionReset();
                    break;
                default:
                    loge("Unknown carrier action: " + msg.what);
                    break;
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

    private void carrierActionReset() {
        carrierActionReportDefaultNetworkStatus(false);
        carrierActionSetMeteredApnsEnabled(true);
        carrierActionSetRadioEnabled(true);
        this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(new Intent("com.android.internal.telephony.CARRIER_SIGNAL_RESET"));
    }

    private RegistrantList getRegistrantsFromAction(int action) {
        if (action == 3) {
            return this.mDefaultNetworkReportRegistrants;
        }
        switch (action) {
            case 0:
                return this.mMeteredApnEnableRegistrants;
            case 1:
                return this.mRadioEnableRegistrants;
            default:
                loge("Unsupported action: " + action);
                return null;
        }
    }

    private Boolean getCarrierActionEnabled(int action) {
        if (action == 3) {
            return this.mCarrierActionReportDefaultNetworkStatus;
        }
        switch (action) {
            case 0:
                return this.mCarrierActionOnMeteredApnEnabled;
            case 1:
                return this.mCarrierActionOnRadioEnabled;
            default:
                loge("Unsupported action: " + action);
                return null;
        }
    }

    public void registerForCarrierAction(int action, Handler h, int what, Object obj, boolean notifyNow) {
        Boolean carrierAction = getCarrierActionEnabled(action);
        if (carrierAction != null) {
            RegistrantList list = getRegistrantsFromAction(action);
            Registrant r = new Registrant(h, what, obj);
            list.add(r);
            if (notifyNow) {
                r.notifyRegistrant(new AsyncResult(null, carrierAction, null));
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
