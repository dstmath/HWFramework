package com.android.server.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.server.am.BatteryStatsService;
import com.android.server.policy.PhoneWindowManager;

public class DataConnectionStats extends BroadcastReceiver {
    private static final boolean DEBUG = false;
    private static final String TAG = "DataConnectionStats";
    private final IBatteryStats mBatteryStats;
    private final Context mContext;
    private int mDataState = 0;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            DataConnectionStats.this.mSignalStrength = signalStrength;
        }

        public void onServiceStateChanged(ServiceState state) {
            DataConnectionStats.this.mServiceState = state;
            DataConnectionStats.this.notePhoneDataConnectionState();
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            DataConnectionStats.this.mDataState = state;
            DataConnectionStats.this.notePhoneDataConnectionState();
        }

        public void onDataActivity(int direction) {
            DataConnectionStats.this.notePhoneDataConnectionState();
        }
    };
    private ServiceState mServiceState;
    private SignalStrength mSignalStrength;
    private State mSimState = State.READY;

    public DataConnectionStats(Context context) {
        this.mContext = context;
        this.mBatteryStats = BatteryStatsService.getService();
    }

    public void startMonitoring() {
        ((TelephonyManager) this.mContext.getSystemService("phone")).listen(this.mPhoneStateListener, 449);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.conn.INET_CONDITION_ACTION");
        this.mContext.registerReceiver(this, filter);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
            updateSimState(intent);
            notePhoneDataConnectionState();
        } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE") || action.equals("android.net.conn.INET_CONDITION_ACTION")) {
            notePhoneDataConnectionState();
        }
    }

    private void notePhoneDataConnectionState() {
        if (this.mServiceState != null) {
            boolean simReadyOrUnknown = this.mSimState != State.READY ? this.mSimState == State.UNKNOWN : true;
            boolean visible = ((simReadyOrUnknown || isCdma()) && hasService()) ? this.mDataState == 2 : false;
            try {
                this.mBatteryStats.notePhoneDataConnectionState(this.mServiceState.getDataNetworkType(), visible);
            } catch (RemoteException e) {
                Log.w(TAG, "Error noting data connection state", e);
            }
        }
    }

    private final void updateSimState(Intent intent) {
        String stateExtra = intent.getStringExtra("ss");
        if ("ABSENT".equals(stateExtra)) {
            this.mSimState = State.ABSENT;
        } else if ("READY".equals(stateExtra)) {
            this.mSimState = State.READY;
        } else if ("LOCKED".equals(stateExtra)) {
            String lockedReason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
            if ("PIN".equals(lockedReason)) {
                this.mSimState = State.PIN_REQUIRED;
            } else if ("PUK".equals(lockedReason)) {
                this.mSimState = State.PUK_REQUIRED;
            } else {
                this.mSimState = State.NETWORK_LOCKED;
            }
        } else {
            this.mSimState = State.UNKNOWN;
        }
    }

    private boolean isCdma() {
        return this.mSignalStrength != null ? this.mSignalStrength.isGsm() ^ 1 : false;
    }

    private boolean hasService() {
        if (this.mServiceState == null || this.mServiceState.getState() == 1) {
            return false;
        }
        return this.mServiceState.getState() != 3;
    }
}
