package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SupplicantStateTracker extends StateMachine {
    private static final /* synthetic */ int[] -android-net-wifi-SupplicantStateSwitchesValues = null;
    private static boolean DBG = HWFLOW;
    protected static final boolean HWFLOW;
    private static final int MAX_RETRIES_ON_ASSOCIATION_REJECT = 3;
    private static final int MAX_RETRIES_ON_AUTHENTICATION_FAILURE = 2;
    private static final String TAG = "SupplicantStateTracker";
    private boolean mAuthFailureInSupplicantBroadcast = false;
    private int mAuthFailureReason;
    private final IBatteryStats mBatteryStats;
    private final State mCompletedState = new CompletedState();
    private final State mConnectionActiveState = new ConnectionActiveState();
    private final Context mContext;
    private final State mDefaultState = new DefaultState();
    private final State mDisconnectState = new DisconnectedState();
    private final State mDormantState = new DormantState();
    private FrameworkFacade mFacade;
    private final State mHandshakeState = new HandshakeState();
    private final State mInactiveState = new InactiveState();
    private boolean mNetworksDisabledDuringConnect = false;
    private final State mScanState = new ScanState();
    private final State mUninitializedState = new UninitializedState();
    private final WifiConfigManager mWifiConfigManager;

    class CompletedState extends State {
        CompletedState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
            if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
            }
        }

        public boolean processMessage(Message message) {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + message.toString() + "\n");
            }
            switch (message.what) {
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    SupplicantState state = stateChangeResult.state;
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast, SupplicantStateTracker.this.mAuthFailureReason);
                    if (!SupplicantState.isConnecting(state)) {
                        SupplicantStateTracker.this.transitionOnSupplicantStateChange(stateChangeResult);
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    class ConnectionActiveState extends State {
        ConnectionActiveState() {
        }

        public boolean processMessage(Message message) {
            if (message.what == 131183) {
                SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(SupplicantState.DISCONNECTED, false);
            }
            return false;
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
        }

        public boolean processMessage(Message message) {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + message.toString() + "\n");
            }
            switch (message.what) {
                case 131183:
                    SupplicantStateTracker.this.transitionTo(SupplicantStateTracker.this.mUninitializedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(stateChangeResult.state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast, SupplicantStateTracker.this.mAuthFailureReason);
                    SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast = false;
                    SupplicantStateTracker.this.mAuthFailureReason = 0;
                    SupplicantStateTracker.this.transitionOnSupplicantStateChange(stateChangeResult);
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case 147474:
                    SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast = true;
                    SupplicantStateTracker.this.mAuthFailureReason = message.arg2;
                    break;
                case 151553:
                    if (HwWifiServiceFactory.getHwWifiServiceManager().autoConnectByMode(message)) {
                        SupplicantStateTracker.this.mNetworksDisabledDuringConnect = true;
                        break;
                    }
                    break;
                default:
                    Log.e(SupplicantStateTracker.TAG, "Ignoring " + message);
                    break;
            }
            return true;
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
        }
    }

    class DormantState extends State {
        DormantState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
        }
    }

    class HandshakeState extends State {
        private static final int MAX_SUPPLICANT_LOOP_ITERATIONS = 4;
        private int mLoopDetectCount;
        private int mLoopDetectIndex;

        HandshakeState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
            this.mLoopDetectIndex = 0;
            this.mLoopDetectCount = 0;
        }

        public boolean processMessage(Message message) {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + message.toString() + "\n");
            }
            switch (message.what) {
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    SupplicantState state = stateChangeResult.state;
                    if (!SupplicantState.isHandshakeState(state)) {
                        return false;
                    }
                    if (this.mLoopDetectIndex > state.ordinal()) {
                        this.mLoopDetectCount++;
                        if (SupplicantStateTracker.DBG) {
                            Log.d(SupplicantStateTracker.TAG, "mLoopDetectIndex:" + this.mLoopDetectIndex + ", state:" + state.ordinal() + ", increase mLoopDetectCount:" + this.mLoopDetectCount);
                        }
                    }
                    if (this.mLoopDetectCount > 4) {
                        if (SupplicantStateTracker.DBG) {
                            Log.d(SupplicantStateTracker.TAG, "Supplicant loop detected, disabling network " + stateChangeResult.networkId);
                        }
                        SupplicantStateTracker.this.handleNetworkConnectionFailure(stateChangeResult.networkId, 3);
                    }
                    this.mLoopDetectIndex = state.ordinal();
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast, SupplicantStateTracker.this.mAuthFailureReason);
                    return true;
                default:
                    return false;
            }
        }
    }

    class InactiveState extends State {
        InactiveState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
        }
    }

    class ScanState extends State {
        ScanState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
        }
    }

    class UninitializedState extends State {
        UninitializedState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
        }
    }

    private static /* synthetic */ int[] -getandroid-net-wifi-SupplicantStateSwitchesValues() {
        if (-android-net-wifi-SupplicantStateSwitchesValues != null) {
            return -android-net-wifi-SupplicantStateSwitchesValues;
        }
        int[] iArr = new int[SupplicantState.values().length];
        try {
            iArr[SupplicantState.ASSOCIATED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SupplicantState.ASSOCIATING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SupplicantState.AUTHENTICATING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SupplicantState.COMPLETED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SupplicantState.DISCONNECTED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SupplicantState.DORMANT.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SupplicantState.FOUR_WAY_HANDSHAKE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[SupplicantState.GROUP_HANDSHAKE.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[SupplicantState.INACTIVE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[SupplicantState.INTERFACE_DISABLED.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[SupplicantState.INVALID.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[SupplicantState.SCANNING.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[SupplicantState.UNINITIALIZED.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        -android-net-wifi-SupplicantStateSwitchesValues = iArr;
        return iArr;
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
    }

    public String getSupplicantStateName() {
        return getCurrentState().getName();
    }

    public SupplicantStateTracker(Context c, WifiConfigManager wcs, FrameworkFacade facade, Handler t) {
        super(TAG, t.getLooper());
        this.mContext = c;
        this.mWifiConfigManager = wcs;
        this.mFacade = facade;
        this.mBatteryStats = this.mFacade.getBatteryService();
        addState(this.mDefaultState);
        addState(this.mUninitializedState, this.mDefaultState);
        addState(this.mInactiveState, this.mDefaultState);
        addState(this.mDisconnectState, this.mDefaultState);
        addState(this.mConnectionActiveState, this.mDefaultState);
        addState(this.mScanState, this.mConnectionActiveState);
        addState(this.mHandshakeState, this.mConnectionActiveState);
        addState(this.mCompletedState, this.mConnectionActiveState);
        addState(this.mDormantState, this.mConnectionActiveState);
        setInitialState(this.mUninitializedState);
        setLogRecSize(200);
        setLogOnlyTransitions(true);
        start();
    }

    private void handleNetworkConnectionFailure(int netId, int disableReason) {
        if (DBG) {
            Log.d(TAG, "handleNetworkConnectionFailure netId=" + Integer.toString(netId) + " reason " + Integer.toString(disableReason) + " mNetworksDisabledDuringConnect=" + this.mNetworksDisabledDuringConnect);
        }
        if (this.mNetworksDisabledDuringConnect) {
            this.mWifiConfigManager.enableAllNetworks();
            this.mNetworksDisabledDuringConnect = false;
        }
        DataUploader.getInstance().e(54, "{RT:" + Integer.toString(disableReason) + ",SPEED:0}");
        this.mWifiConfigManager.updateNetworkSelectionStatus(netId, disableReason);
        WifiInjector.getInstance().getWifiStateMachine().handleConnectFailedInWifiPro(netId, disableReason);
    }

    private void transitionOnSupplicantStateChange(StateChangeResult stateChangeResult) {
        SupplicantState supState = stateChangeResult.state;
        if (DBG) {
            Log.d(TAG, "Supplicant state: " + supState.toString() + "\n");
        }
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[supState.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 7:
            case 8:
                transitionTo(this.mHandshakeState);
                return;
            case 4:
                transitionTo(this.mCompletedState);
                return;
            case 5:
                transitionTo(this.mDisconnectState);
                return;
            case 6:
                transitionTo(this.mDormantState);
                return;
            case 9:
                transitionTo(this.mInactiveState);
                return;
            case 10:
                return;
            case 11:
            case 13:
                transitionTo(this.mUninitializedState);
                return;
            case 12:
                transitionTo(this.mScanState);
                return;
            default:
                Log.e(TAG, "Unknown supplicant state " + supState);
                return;
        }
    }

    private void sendSupplicantStateChangedBroadcast(SupplicantState state, boolean failedAuth) {
        sendSupplicantStateChangedBroadcast(state, failedAuth, 0);
    }

    private void sendSupplicantStateChangedBroadcast(SupplicantState state, boolean failedAuth, int reasonCode) {
        int supplState;
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
            case 1:
                supplState = 7;
                break;
            case 2:
                supplState = 6;
                break;
            case 3:
                supplState = 5;
                break;
            case 4:
                supplState = 10;
                break;
            case 5:
                supplState = 1;
                break;
            case 6:
                supplState = 11;
                break;
            case 7:
                supplState = 8;
                break;
            case 8:
                supplState = 9;
                break;
            case 9:
                supplState = 3;
                break;
            case 10:
                supplState = 2;
                break;
            case 11:
                supplState = 0;
                break;
            case 12:
                supplState = 4;
                break;
            case 13:
                supplState = 12;
                break;
            default:
                Slog.w(TAG, "Unknown supplicant state " + state);
                supplState = 0;
                break;
        }
        try {
            this.mBatteryStats.noteWifiSupplicantStateChanged(supplState, failedAuth);
        } catch (RemoteException e) {
        }
        Intent intent = new Intent("android.net.wifi.supplicant.STATE_CHANGE");
        intent.addFlags(603979776);
        intent.putExtra("newState", state);
        if (failedAuth) {
            intent.putExtra("supplicantError", 1);
            intent.putExtra("supplicantErrorReason", reasonCode);
        }
        if (!WifiInjector.getInstance().getWifiStateMachine().ignoreSupplicantStateChange(state)) {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        pw.println("mAuthFailureInSupplicantBroadcast " + this.mAuthFailureInSupplicantBroadcast);
        pw.println("mAuthFailureReason " + this.mAuthFailureReason);
        pw.println("mNetworksDisabledDuringConnect " + this.mNetworksDisabledDuringConnect);
        pw.println();
    }
}
