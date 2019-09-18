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
import com.android.server.wifi.scanner.ChannelHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SupplicantStateTracker extends StateMachine {
    /* access modifiers changed from: private */
    public static boolean DBG = HWFLOW;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MAX_RETRIES_ON_ASSOCIATION_REJECT = 3;
    private static final int MAX_RETRIES_ON_AUTHENTICATION_FAILURE = 2;
    private static final String TAG = "SupplicantStateTracker";
    /* access modifiers changed from: private */
    public boolean mAuthFailureInSupplicantBroadcast = false;
    /* access modifiers changed from: private */
    public int mAuthFailureReason;
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
    /* access modifiers changed from: private */
    public boolean mNetworksDisabledDuringConnect = false;
    private final State mScanState = new ScanState();
    /* access modifiers changed from: private */
    public final State mUninitializedState = new UninitializedState();
    private final WifiConfigManager mWifiConfigManager;

    /* renamed from: com.android.server.wifi.SupplicantStateTracker$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$net$wifi$SupplicantState = new int[SupplicantState.values().length];

        static {
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.DISCONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.INTERFACE_DISABLED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.SCANNING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.AUTHENTICATING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.ASSOCIATING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.ASSOCIATED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.FOUR_WAY_HANDSHAKE.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.GROUP_HANDSHAKE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.COMPLETED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.DORMANT.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.INACTIVE.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.UNINITIALIZED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.INVALID.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
        }
    }

    class CompletedState extends State {
        CompletedState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
            if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                boolean unused = SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
            }
        }

        public boolean processMessage(Message message) {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + message.toString() + "\n");
            }
            if (message.what != 147462) {
                return false;
            }
            StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
            SupplicantState state = stateChangeResult.state;
            SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast, SupplicantStateTracker.this.mAuthFailureReason);
            if (!SupplicantState.isConnecting(state)) {
                SupplicantStateTracker.this.transitionOnSupplicantStateChange(stateChangeResult);
            }
            return true;
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
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(stateChangeResult.state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast, SupplicantStateTracker.this.mAuthFailureReason);
                    boolean unused = SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast = false;
                    int unused2 = SupplicantStateTracker.this.mAuthFailureReason = 0;
                    SupplicantStateTracker.this.transitionOnSupplicantStateChange(stateChangeResult);
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case 147474:
                    boolean unused3 = SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast = true;
                    int unused4 = SupplicantStateTracker.this.mAuthFailureReason = message.arg1;
                    break;
                case 151553:
                    if (HwWifiServiceFactory.getHwWifiServiceManager().autoConnectByMode(message)) {
                        boolean unused5 = SupplicantStateTracker.this.mNetworksDisabledDuringConnect = true;
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
            if (message.what != 147462) {
                return false;
            }
            StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
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

    /* access modifiers changed from: package-private */
    public void enableVerboseLogging(int verbose) {
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
        setLogRecSize(ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS);
        setLogOnlyTransitions(true);
        start();
    }

    /* access modifiers changed from: private */
    public void handleNetworkConnectionFailure(int netId, int disableReason) {
        if (DBG) {
            Log.d(TAG, "handleNetworkConnectionFailure netId=" + Integer.toString(netId) + " reason " + Integer.toString(disableReason) + " mNetworksDisabledDuringConnect=" + this.mNetworksDisabledDuringConnect);
        }
        if (this.mNetworksDisabledDuringConnect) {
            this.mWifiConfigManager.enableAllNetworks();
            this.mNetworksDisabledDuringConnect = false;
        }
        DataUploader instance = DataUploader.getInstance();
        instance.e(54, "{RT:" + Integer.toString(disableReason) + ",SPEED:0}");
        this.mWifiConfigManager.updateNetworkSelectionStatus(netId, disableReason);
        WifiInjector.getInstance().getWifiStateMachine().handleConnectFailedInWifiPro(netId, disableReason);
    }

    /* access modifiers changed from: private */
    public void transitionOnSupplicantStateChange(StateChangeResult stateChangeResult) {
        SupplicantState supState = stateChangeResult.state;
        if (DBG) {
            Log.d(TAG, "Supplicant state: " + supState.toString() + "\n");
        }
        switch (AnonymousClass1.$SwitchMap$android$net$wifi$SupplicantState[supState.ordinal()]) {
            case 1:
                transitionTo(this.mDisconnectState);
                return;
            case 2:
                return;
            case 3:
                transitionTo(this.mScanState);
                return;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                transitionTo(this.mHandshakeState);
                return;
            case 9:
                transitionTo(this.mCompletedState);
                return;
            case 10:
                transitionTo(this.mDormantState);
                return;
            case 11:
                transitionTo(this.mInactiveState);
                return;
            case 12:
            case 13:
                transitionTo(this.mUninitializedState);
                return;
            default:
                Log.e(TAG, "Unknown supplicant state " + supState);
                return;
        }
    }

    /* access modifiers changed from: private */
    public void sendSupplicantStateChangedBroadcast(SupplicantState state, boolean failedAuth) {
        sendSupplicantStateChangedBroadcast(state, failedAuth, 0);
    }

    /* access modifiers changed from: private */
    public void sendSupplicantStateChangedBroadcast(SupplicantState state, boolean failedAuth, int reasonCode) {
        int supplState;
        switch (AnonymousClass1.$SwitchMap$android$net$wifi$SupplicantState[state.ordinal()]) {
            case 1:
                supplState = 1;
                break;
            case 2:
                supplState = 2;
                break;
            case 3:
                supplState = 4;
                break;
            case 4:
                supplState = 5;
                break;
            case 5:
                supplState = 6;
                break;
            case 6:
                supplState = 7;
                break;
            case 7:
                supplState = 8;
                break;
            case 8:
                supplState = 9;
                break;
            case 9:
                supplState = 10;
                break;
            case 10:
                supplState = 11;
                break;
            case 11:
                supplState = 3;
                break;
            case 12:
                supplState = 12;
                break;
            case 13:
                supplState = 0;
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
        SupplicantStateTracker.super.dump(fd, pw, args);
        pw.println("mAuthFailureInSupplicantBroadcast " + this.mAuthFailureInSupplicantBroadcast);
        pw.println("mAuthFailureReason " + this.mAuthFailureReason);
        pw.println("mNetworksDisabledDuringConnect " + this.mNetworksDisabledDuringConnect);
        pw.println();
    }
}
