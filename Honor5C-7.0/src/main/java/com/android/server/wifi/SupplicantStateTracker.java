package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.scanner.ChannelHelper;
import com.google.protobuf.nano.Extension;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SupplicantStateTracker extends StateMachine {
    private static final /* synthetic */ int[] -android-net-wifi-SupplicantStateSwitchesValues = null;
    private static boolean DBG = false;
    protected static final boolean HWFLOW = false;
    private static final int MAX_RETRIES_ON_ASSOCIATION_REJECT = 3;
    private static final int MAX_RETRIES_ON_AUTHENTICATION_FAILURE = 2;
    private static final String TAG = "SupplicantStateTracker";
    private boolean mAuthFailureInSupplicantBroadcast;
    private final IBatteryStats mBatteryStats;
    private final State mCompletedState;
    private final Context mContext;
    private final State mDefaultState;
    private final State mDisconnectState;
    private final State mDormantState;
    private final State mHandshakeState;
    private final State mInactiveState;
    private boolean mNetworksDisabledDuringConnect;
    private final State mScanState;
    private final State mUninitializedState;
    private final WifiConfigManager mWifiConfigManager;

    class CompletedState extends State {
        CompletedState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
            if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                SupplicantStateTracker.this.mWifiConfigManager.enableAllNetworks();
                SupplicantStateTracker.this.mNetworksDisabledDuringConnect = SupplicantStateTracker.HWFLOW;
            }
        }

        public boolean processMessage(Message message) {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + message.toString() + "\n");
            }
            switch (message.what) {
                case 131183:
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(SupplicantState.DISCONNECTED, SupplicantStateTracker.HWFLOW);
                    SupplicantStateTracker.this.transitionTo(SupplicantStateTracker.this.mUninitializedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    SupplicantState state = stateChangeResult.state;
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast);
                    if (!SupplicantState.isConnecting(state)) {
                        SupplicantStateTracker.this.transitionOnSupplicantStateChange(stateChangeResult);
                        break;
                    }
                    break;
                default:
                    return SupplicantStateTracker.HWFLOW;
            }
            return true;
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
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(stateChangeResult.state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast);
                    SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast = SupplicantStateTracker.HWFLOW;
                    SupplicantStateTracker.this.transitionOnSupplicantStateChange(stateChangeResult);
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case WifiMonitor.WAPI_AUTHENTICATION_FAILURE_EVENT /*147474*/:
                    SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast = true;
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
                        return SupplicantStateTracker.HWFLOW;
                    }
                    if (this.mLoopDetectIndex > state.ordinal()) {
                        this.mLoopDetectCount++;
                        if (SupplicantStateTracker.DBG) {
                            Log.d(SupplicantStateTracker.TAG, "mLoopDetectIndex:" + this.mLoopDetectIndex + ", state:" + state.ordinal() + ", increase mLoopDetectCount:" + this.mLoopDetectCount);
                        }
                    }
                    if (this.mLoopDetectCount > MAX_SUPPLICANT_LOOP_ITERATIONS) {
                        if (SupplicantStateTracker.DBG) {
                            Log.d(SupplicantStateTracker.TAG, "Supplicant loop detected, disabling network " + stateChangeResult.networkId);
                        }
                        SupplicantStateTracker.this.handleNetworkConnectionFailure(stateChangeResult.networkId, SupplicantStateTracker.MAX_RETRIES_ON_ASSOCIATION_REJECT);
                    }
                    this.mLoopDetectIndex = state.ordinal();
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast);
                    return true;
                default:
                    return SupplicantStateTracker.HWFLOW;
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
            iArr[SupplicantState.ASSOCIATING.ordinal()] = MAX_RETRIES_ON_AUTHENTICATION_FAILURE;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SupplicantState.AUTHENTICATING.ordinal()] = MAX_RETRIES_ON_ASSOCIATION_REJECT;
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.SupplicantStateTracker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.SupplicantStateTracker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.SupplicantStateTracker.<clinit>():void");
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = HWFLOW;
        }
    }

    public String getSupplicantStateName() {
        return getCurrentState().getName();
    }

    public SupplicantStateTracker(Context c, WifiConfigManager wcs, Handler t) {
        super(TAG, t.getLooper());
        this.mAuthFailureInSupplicantBroadcast = HWFLOW;
        this.mNetworksDisabledDuringConnect = HWFLOW;
        this.mUninitializedState = new UninitializedState();
        this.mDefaultState = new DefaultState();
        this.mInactiveState = new InactiveState();
        this.mDisconnectState = new DisconnectedState();
        this.mScanState = new ScanState();
        this.mHandshakeState = new HandshakeState();
        this.mCompletedState = new CompletedState();
        this.mDormantState = new DormantState();
        this.mContext = c;
        this.mWifiConfigManager = wcs;
        this.mBatteryStats = (IBatteryStats) ServiceManager.getService("batterystats");
        addState(this.mDefaultState);
        addState(this.mUninitializedState, this.mDefaultState);
        addState(this.mInactiveState, this.mDefaultState);
        addState(this.mDisconnectState, this.mDefaultState);
        addState(this.mScanState, this.mDefaultState);
        addState(this.mHandshakeState, this.mDefaultState);
        addState(this.mCompletedState, this.mDefaultState);
        addState(this.mDormantState, this.mDefaultState);
        setInitialState(this.mUninitializedState);
        setLogRecSize(ChannelHelper.SCAN_PERIOD_PER_CHANNEL_MS);
        setLogOnlyTransitions(true);
        start();
    }

    private void handleNetworkConnectionFailure(int netId, int disableReason) {
        if (DBG) {
            Log.d(TAG, "handleNetworkConnectionFailure netId=" + Integer.toString(netId) + " reason " + Integer.toString(disableReason) + " mNetworksDisabledDuringConnect=" + this.mNetworksDisabledDuringConnect);
        }
        if (this.mNetworksDisabledDuringConnect) {
            this.mWifiConfigManager.enableAllNetworks();
            this.mNetworksDisabledDuringConnect = HWFLOW;
        }
        DataUploader.getInstance().e(54, "{RT:" + Integer.toString(disableReason) + ",SPEED:0}");
        this.mWifiConfigManager.updateNetworkSelectionStatus(netId, disableReason);
        WifiStateMachine globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
        if (globalHwWsm != null) {
            globalHwWsm.handleConnectFailedInWifiPro(netId, disableReason);
        }
    }

    private void transitionOnSupplicantStateChange(StateChangeResult stateChangeResult) {
        SupplicantState supState = stateChangeResult.state;
        if (DBG) {
            Log.d(TAG, "Supplicant state: " + supState.toString() + "\n");
        }
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[supState.ordinal()]) {
            case Extension.TYPE_DOUBLE /*1*/:
            case MAX_RETRIES_ON_AUTHENTICATION_FAILURE /*2*/:
            case MAX_RETRIES_ON_ASSOCIATION_REJECT /*3*/:
            case Extension.TYPE_FIXED32 /*7*/:
            case Extension.TYPE_BOOL /*8*/:
                transitionTo(this.mHandshakeState);
            case Extension.TYPE_UINT64 /*4*/:
                transitionTo(this.mCompletedState);
            case Extension.TYPE_INT32 /*5*/:
                transitionTo(this.mDisconnectState);
            case Extension.TYPE_FIXED64 /*6*/:
                transitionTo(this.mDormantState);
            case Extension.TYPE_STRING /*9*/:
                transitionTo(this.mInactiveState);
            case Extension.TYPE_GROUP /*10*/:
            case Extension.TYPE_MESSAGE /*11*/:
            case Extension.TYPE_UINT32 /*13*/:
                transitionTo(this.mUninitializedState);
            case Extension.TYPE_BYTES /*12*/:
                transitionTo(this.mScanState);
            default:
                Log.e(TAG, "Unknown supplicant state " + supState);
        }
    }

    private void sendSupplicantStateChangedBroadcast(SupplicantState state, boolean failedAuth) {
        int supplState;
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
            case Extension.TYPE_DOUBLE /*1*/:
                supplState = 7;
                break;
            case MAX_RETRIES_ON_AUTHENTICATION_FAILURE /*2*/:
                supplState = 6;
                break;
            case MAX_RETRIES_ON_ASSOCIATION_REJECT /*3*/:
                supplState = 5;
                break;
            case Extension.TYPE_UINT64 /*4*/:
                supplState = 10;
                break;
            case Extension.TYPE_INT32 /*5*/:
                supplState = 1;
                break;
            case Extension.TYPE_FIXED64 /*6*/:
                supplState = 11;
                break;
            case Extension.TYPE_FIXED32 /*7*/:
                supplState = 8;
                break;
            case Extension.TYPE_BOOL /*8*/:
                supplState = 9;
                break;
            case Extension.TYPE_STRING /*9*/:
                supplState = MAX_RETRIES_ON_ASSOCIATION_REJECT;
                break;
            case Extension.TYPE_GROUP /*10*/:
                supplState = MAX_RETRIES_ON_AUTHENTICATION_FAILURE;
                break;
            case Extension.TYPE_MESSAGE /*11*/:
                supplState = 0;
                break;
            case Extension.TYPE_BYTES /*12*/:
                supplState = 4;
                break;
            case Extension.TYPE_UINT32 /*13*/:
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
        }
        WifiStateMachine globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
        if (globalHwWsm == null || !globalHwWsm.ignoreSupplicantStateChange(state)) {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        pw.println("mAuthFailureInSupplicantBroadcast " + this.mAuthFailureInSupplicantBroadcast);
        pw.println("mNetworksDisabledDuringConnect " + this.mNetworksDisabledDuringConnect);
        pw.println();
    }
}
