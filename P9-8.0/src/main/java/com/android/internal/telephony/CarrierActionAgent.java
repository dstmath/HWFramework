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
import android.provider.Settings.Global;
import android.telephony.Rlog;
import android.util.LocalLog;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class CarrierActionAgent extends Handler {
    public static final int CARRIER_ACTION_RESET = 2;
    public static final int CARRIER_ACTION_SET_METERED_APNS_ENABLED = 0;
    public static final int CARRIER_ACTION_SET_RADIO_ENABLED = 1;
    private static final boolean DBG = true;
    private static final String LOG_TAG = "CarrierActionAgent";
    private static final boolean VDBG = Rlog.isLoggable(LOG_TAG, 2);
    private Boolean mCarrierActionOnMeteredApnEnabled = Boolean.valueOf(true);
    private Boolean mCarrierActionOnRadioEnabled = Boolean.valueOf(true);
    private RegistrantList mMeteredApnEnableRegistrants = new RegistrantList();
    private LocalLog mMeteredApnEnabledLog = new LocalLog(10);
    private final Phone mPhone;
    private RegistrantList mRadioEnableRegistrants = new RegistrantList();
    private LocalLog mRadioEnabledLog = new LocalLog(10);
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String iccState = intent.getStringExtra("ss");
            if (!(!"android.intent.action.SIM_STATE_CHANGED".equals(action) || intent.getBooleanExtra("rebroadcastOnUnlock", false) || "LOADED".equals(iccState))) {
                boolean equals = "ABSENT".equals(iccState);
            }
        }
    };
    private final SettingsObserver mSettingsObserver;

    private class SettingsObserver extends ContentObserver {
        SettingsObserver() {
            super(null);
        }

        public void onChange(boolean selfChange) {
            int i = Global.getInt(CarrierActionAgent.this.mPhone.getContext().getContentResolver(), "airplane_mode_on", 0);
        }
    }

    public CarrierActionAgent(Phone phone) {
        this.mPhone = phone;
        this.mPhone.getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        this.mSettingsObserver = new SettingsObserver();
        this.mPhone.getContext().getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), false, this.mSettingsObserver);
        log("Creating CarrierActionAgent");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                this.mCarrierActionOnMeteredApnEnabled = Boolean.valueOf(((Boolean) msg.obj).booleanValue());
                log("SET_METERED_APNS_ENABLED: " + this.mCarrierActionOnMeteredApnEnabled);
                this.mMeteredApnEnabledLog.log("SET_METERED_APNS_ENABLED: " + this.mCarrierActionOnMeteredApnEnabled);
                this.mMeteredApnEnableRegistrants.notifyRegistrants(new AsyncResult(null, this.mCarrierActionOnMeteredApnEnabled, null));
                return;
            case 1:
                this.mCarrierActionOnRadioEnabled = Boolean.valueOf(((Boolean) msg.obj).booleanValue());
                log("SET_RADIO_ENABLED: " + this.mCarrierActionOnRadioEnabled);
                this.mRadioEnabledLog.log("SET_RADIO_ENABLED: " + this.mCarrierActionOnRadioEnabled);
                this.mRadioEnableRegistrants.notifyRegistrants(new AsyncResult(null, this.mCarrierActionOnRadioEnabled, null));
                return;
            case 2:
                log("CARRIER_ACTION_RESET");
                carrierActionSetMeteredApnsEnabled(true);
                carrierActionSetRadioEnabled(true);
                this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(new Intent("com.android.internal.telephony.CARRIER_SIGNAL_RESET"));
                return;
            default:
                loge("Unknown carrier action: " + msg.what);
                return;
        }
    }

    public Object getCarrierActionValue(int action) {
        Object val = getCarrierAction(action);
        if (val != null) {
            return val;
        }
        throw new IllegalArgumentException("invalid carrier action: " + action);
    }

    public void carrierActionSetRadioEnabled(boolean enabled) {
        sendMessage(obtainMessage(1, Boolean.valueOf(enabled)));
    }

    public void carrierActionSetMeteredApnsEnabled(boolean enabled) {
        sendMessage(obtainMessage(0, Boolean.valueOf(enabled)));
    }

    private RegistrantList getRegistrantsFromAction(int action) {
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

    private Object getCarrierAction(int action) {
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
        Object carrierAction = getCarrierAction(action);
        if (carrierAction == null) {
            throw new IllegalArgumentException("invalid carrier action: " + action);
        }
        RegistrantList list = getRegistrantsFromAction(action);
        Registrant r = new Registrant(h, what, obj);
        list.add(r);
        if (notifyNow) {
            r.notifyRegistrant(new AsyncResult(null, carrierAction, null));
        }
    }

    public void unregisterForCarrierAction(Handler h, int action) {
        RegistrantList list = getRegistrantsFromAction(action);
        if (list == null) {
            throw new IllegalArgumentException("invalid carrier action: " + action);
        }
        list.remove(h);
    }

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
    }
}
