package com.android.server.wifi;

import android.content.Context;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SarManager {
    private static final String TAG = "WifiSarManager";
    private boolean mCellOn = false;
    private final Context mContext;
    private int mCurrentSarScenario = 0;
    /* access modifiers changed from: private */
    public boolean mEnableSarTxPowerLimit;
    private final Looper mLooper;
    private final WifiPhoneStateListener mPhoneStateListener;
    private final TelephonyManager mTelephonyManager;
    private boolean mVerboseLoggingEnabled = true;
    private final WifiNative mWifiNative;
    private boolean mWifiStaEnabled = false;

    private class WifiPhoneStateListener extends PhoneStateListener {
        WifiPhoneStateListener(Looper looper) {
            super(looper);
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            Log.d(SarManager.TAG, "Received Phone State Change: " + state);
            if (SarManager.this.mEnableSarTxPowerLimit) {
                SarManager.this.onCellStateChangeEvent(state);
            }
        }
    }

    SarManager(Context context, TelephonyManager telephonyManager, Looper looper, WifiNative wifiNative) {
        this.mContext = context;
        this.mTelephonyManager = telephonyManager;
        this.mWifiNative = wifiNative;
        this.mLooper = looper;
        this.mPhoneStateListener = new WifiPhoneStateListener(looper);
        registerListeners();
    }

    private void registerListeners() {
        this.mEnableSarTxPowerLimit = this.mContext.getResources().getBoolean(17957082);
        if (this.mEnableSarTxPowerLimit) {
            Log.d(TAG, "Registering Listeners for the SAR Manager");
            registerPhoneListener();
        }
    }

    /* access modifiers changed from: private */
    public void onCellStateChangeEvent(int state) {
        boolean currentCellOn = this.mCellOn;
        switch (state) {
            case 0:
                this.mCellOn = false;
                break;
            case 1:
            case 2:
                this.mCellOn = true;
                break;
            default:
                Log.e(TAG, "Invalid Cell State: " + state);
                break;
        }
        if (this.mCellOn != currentCellOn) {
            updateSarScenario();
        }
    }

    public void setClientWifiState(int state) {
        if (this.mEnableSarTxPowerLimit) {
            if (state == 1 && this.mWifiStaEnabled) {
                this.mWifiStaEnabled = false;
            } else if (state == 3 && !this.mWifiStaEnabled) {
                this.mWifiStaEnabled = true;
                sendTxPowerScenario(this.mCurrentSarScenario);
            }
        }
    }

    public void enableVerboseLogging(int verbose) {
        Log.d(TAG, "Inside enableVerboseLogging: " + verbose);
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("*** WiFi SAR Manager Dump ***");
        pw.println("Current SAR Scenario is " + scenarioToString(this.mCurrentSarScenario));
    }

    private void registerPhoneListener() {
        Log.i(TAG, "Registering for telephony call state changes");
        this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
    }

    private void updateSarScenario() {
        int newSarScenario;
        if (this.mCellOn) {
            newSarScenario = 1;
        } else {
            newSarScenario = 0;
        }
        if (newSarScenario != this.mCurrentSarScenario) {
            if (this.mWifiStaEnabled) {
                Log.d(TAG, "Sending SAR Scenario #" + scenarioToString(newSarScenario));
                sendTxPowerScenario(newSarScenario);
            }
            this.mCurrentSarScenario = newSarScenario;
        }
    }

    private void sendTxPowerScenario(int newSarScenario) {
        if (!this.mWifiNative.selectTxPowerScenario(newSarScenario)) {
            Log.e(TAG, "Failed to set TX power scenario");
        }
    }

    private String scenarioToString(int scenario) {
        switch (scenario) {
            case 0:
                return "TX_POWER_SCENARIO_NORMAL";
            case 1:
                return "TX_POWER_SCENARIO_VOICE_CALL";
            default:
                return "Invalid Scenario";
        }
    }
}
