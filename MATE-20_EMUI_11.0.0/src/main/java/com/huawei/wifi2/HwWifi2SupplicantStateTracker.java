package com.huawei.wifi2;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.UserHandle;
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.HwWifiServiceFactory;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwWifi2SupplicantStateTracker extends StateMachine {
    private static final String TAG = "HwWifi2SupplicantStateTracker";
    private final Context mContext;
    private final State mDisconnectState = new Wifi2DisconnectedState();
    private boolean mIsNetworksDisabledDuringConnect = false;
    private boolean mIsWifi2AuthFailureInSupplicantBroadcast = false;
    private int mWifi2AuthFailureReason;
    private final State mWifi2CompletedState = new Wifi2CompletedState();
    private final State mWifi2ConnectionActiveState = new Wifi2ConnectionActiveState();
    private final State mWifi2DefaultState = new Wifi2DefaultState();
    private final State mWifi2DormantState = new Wifi2DormantState();
    private final State mWifi2HandshakeState = new Wifi2HandshakeState();
    private final State mWifi2InactiveState = new Wifi2InactiveState();
    private final State mWifi2ScanState = new Wifi2ScanState();
    private final State mWifi2UninitializedState = new Wifi2UninitializedState();

    public HwWifi2SupplicantStateTracker(Context context, Handler handler) {
        super(TAG, handler.getLooper());
        this.mContext = context;
        addState(this.mWifi2DefaultState);
        addState(this.mWifi2UninitializedState, this.mWifi2DefaultState);
        addState(this.mWifi2InactiveState, this.mWifi2DefaultState);
        addState(this.mDisconnectState, this.mWifi2DefaultState);
        addState(this.mWifi2ConnectionActiveState, this.mWifi2DefaultState);
        addState(this.mWifi2ScanState, this.mWifi2ConnectionActiveState);
        addState(this.mWifi2HandshakeState, this.mWifi2ConnectionActiveState);
        addState(this.mWifi2CompletedState, this.mWifi2ConnectionActiveState);
        addState(this.mWifi2DormantState, this.mWifi2ConnectionActiveState);
        setInitialState(this.mWifi2UninitializedState);
        start();
    }

    public String getSupplicantStateName() {
        return getCurrentState().getName();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkConnectionFailure(int netId, int disableReason) {
        HwHiLog.i(TAG, false, "handleNetworkConnectionFailure netId= %{public}d reason = %{public}d mIsNetworksDisabledDuringConnect= %{public}s", new Object[]{Integer.valueOf(netId), Integer.valueOf(disableReason), String.valueOf(this.mIsNetworksDisabledDuringConnect)});
        this.mIsNetworksDisabledDuringConnect = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void transitionOnSupplicantStateChange(StateChangeResult stateChangeResult) {
        if (stateChangeResult.state instanceof SupplicantState) {
            SupplicantState newSupplicantState = stateChangeResult.state;
            HwHiLog.i(TAG, false, "Supplicant state: %{public}s", new Object[]{newSupplicantState});
            switch (AnonymousClass1.$SwitchMap$android$net$wifi$SupplicantState[newSupplicantState.ordinal()]) {
                case 1:
                    return;
                case 2:
                    transitionTo(this.mWifi2InactiveState);
                    return;
                case 3:
                    transitionTo(this.mDisconnectState);
                    return;
                case 4:
                    transitionTo(this.mWifi2ScanState);
                    return;
                case 5:
                    transitionTo(this.mWifi2DormantState);
                    return;
                case 6:
                    transitionTo(this.mWifi2CompletedState);
                    return;
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                    transitionTo(this.mWifi2HandshakeState);
                    return;
                case 12:
                case HwWifi2CondControl.HW_SIGNAL_POLL_PARA_LENGTH /* 13 */:
                    transitionTo(this.mWifi2UninitializedState);
                    return;
                default:
                    HwHiLog.i(TAG, false, "Unknown supplicant state %{public}s", new Object[]{String.valueOf(newSupplicantState)});
                    return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.wifi2.HwWifi2SupplicantStateTracker$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$net$wifi$SupplicantState = new int[SupplicantState.values().length];

        static {
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.INTERFACE_DISABLED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.INACTIVE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.DISCONNECTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.SCANNING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.DORMANT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.COMPLETED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.ASSOCIATED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.ASSOCIATING.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.AUTHENTICATING.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.FOUR_WAY_HANDSHAKE.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.GROUP_HANDSHAKE.ordinal()] = 11;
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSupplicantStateChangedBroadcast(SupplicantState state, boolean isFailedAuth) {
        sendSupplicantStateChangedBroadcast(state, isFailedAuth, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSupplicantStateChangedBroadcast(SupplicantState state, boolean isFailedAuth, int reasonCode) {
        Intent intent = new Intent("huawei.net.slave_wifi.supplicant.STATE_CHANGE");
        intent.addFlags(603979776);
        intent.putExtra("newState", (Parcelable) state);
        if (isFailedAuth) {
            intent.putExtra("supplicantError", 1);
            intent.putExtra("supplicantErrorReason", reasonCode);
        }
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    class Wifi2DefaultState extends State {
        Wifi2DefaultState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Enter Wifi2DefaultState %{public}s", new Object[]{getName()});
        }

        public boolean processMessage(Message message) {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Wifi2DefaultState processMessage %{public}s", new Object[]{HwWifi2ClientModeImplConst.messageNumToString(message.what)});
            switch (message.what) {
                case HwWifi2ClientModeImplConst.CMD_RESET_SUPPLICANT_STATE /* 131183 */:
                    HwWifi2SupplicantStateTracker hwWifi2SupplicantStateTracker = HwWifi2SupplicantStateTracker.this;
                    hwWifi2SupplicantStateTracker.transitionTo(hwWifi2SupplicantStateTracker.mWifi2UninitializedState);
                    break;
                case HwWifi2Monitor.SUPPLICANT_STATE_CHANGE_EVENT /* 147462 */:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    SupplicantState state = stateChangeResult.state;
                    HwWifi2SupplicantStateTracker hwWifi2SupplicantStateTracker2 = HwWifi2SupplicantStateTracker.this;
                    hwWifi2SupplicantStateTracker2.sendSupplicantStateChangedBroadcast(state, hwWifi2SupplicantStateTracker2.mIsWifi2AuthFailureInSupplicantBroadcast, HwWifi2SupplicantStateTracker.this.mWifi2AuthFailureReason);
                    HwWifi2SupplicantStateTracker.this.mIsWifi2AuthFailureInSupplicantBroadcast = false;
                    HwWifi2SupplicantStateTracker.this.mWifi2AuthFailureReason = 0;
                    HwWifi2SupplicantStateTracker.this.transitionOnSupplicantStateChange(stateChangeResult);
                    break;
                case HwWifi2Monitor.AUTHENTICATION_FAILURE_EVENT /* 147463 */:
                case HwWifi2Monitor.WAPI_AUTHENTICATION_FAILURE_EVENT /* 147474 */:
                    HwWifi2SupplicantStateTracker.this.mIsWifi2AuthFailureInSupplicantBroadcast = true;
                    HwWifi2SupplicantStateTracker.this.mWifi2AuthFailureReason = message.arg1;
                    break;
                case HwWifi2Monitor.ASSOCIATION_REJECTION_EVENT /* 147499 */:
                    break;
                case 151553:
                    if (HwWifiServiceFactory.getHwWifiServiceManager().autoConnectByMode(message)) {
                        HwWifi2SupplicantStateTracker.this.mIsNetworksDisabledDuringConnect = true;
                        break;
                    }
                    break;
                default:
                    HwHiLog.e(HwWifi2SupplicantStateTracker.TAG, false, "Ignoring %{public}s", new Object[]{message});
                    break;
            }
            return true;
        }
    }

    class Wifi2UninitializedState extends State {
        Wifi2UninitializedState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Enter Wifi2UninitializedState {public}s", new Object[]{getName()});
        }
    }

    class Wifi2InactiveState extends State {
        Wifi2InactiveState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Enter Wifi2InactiveState %{public}s", new Object[]{getName()});
        }
    }

    class Wifi2DisconnectedState extends State {
        Wifi2DisconnectedState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Ente Wifi2DisconnectedState %{public}s", new Object[]{getName()});
        }
    }

    class Wifi2ScanState extends State {
        Wifi2ScanState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Enter Wifi2ScanState %{public}s", new Object[]{getName()});
        }
    }

    class Wifi2ConnectionActiveState extends State {
        Wifi2ConnectionActiveState() {
        }

        public boolean processMessage(Message message) {
            if (message.what == 131183) {
                HwWifi2SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(SupplicantState.DISCONNECTED, false);
            }
            return false;
        }
    }

    class Wifi2HandshakeState extends State {
        private static final int SUPPLICANT_AUTH_MAX_ITERATIONS = 4;
        private int mAuthDetectCount;
        private int mAuthDetectIndex;

        Wifi2HandshakeState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Enter Wifi2HandshakeState %{public}s", new Object[]{getName()});
            this.mAuthDetectIndex = 0;
            this.mAuthDetectCount = 0;
        }

        public boolean processMessage(Message message) {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Wifi2HandshakeState processMessage %{public}s", new Object[]{HwWifi2ClientModeImplConst.messageNumToString(message.what)});
            if (message.what != 147462) {
                return false;
            }
            StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
            SupplicantState state = stateChangeResult.state;
            if (!SupplicantState.isHandshakeState(state)) {
                return false;
            }
            if (this.mAuthDetectIndex > state.ordinal()) {
                this.mAuthDetectCount++;
                HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "mAuthDetectIndex: %{public}d, state: %{public}d, increase mAuthDetectCount: %{public}d", new Object[]{Integer.valueOf(this.mAuthDetectIndex), Integer.valueOf(state.ordinal()), Integer.valueOf(this.mAuthDetectCount)});
            }
            if (this.mAuthDetectCount > 4) {
                HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Supplicant loop detected, disabling network %{public}d", new Object[]{Integer.valueOf(stateChangeResult.networkId)});
                HwWifi2SupplicantStateTracker.this.handleNetworkConnectionFailure(stateChangeResult.networkId, 3);
            }
            this.mAuthDetectIndex = state.ordinal();
            HwWifi2SupplicantStateTracker hwWifi2SupplicantStateTracker = HwWifi2SupplicantStateTracker.this;
            hwWifi2SupplicantStateTracker.sendSupplicantStateChangedBroadcast(state, hwWifi2SupplicantStateTracker.mIsWifi2AuthFailureInSupplicantBroadcast, HwWifi2SupplicantStateTracker.this.mWifi2AuthFailureReason);
            return true;
        }
    }

    class Wifi2CompletedState extends State {
        Wifi2CompletedState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "enter Wifi2CompletedState %{public}s", new Object[]{getName()});
            if (HwWifi2SupplicantStateTracker.this.mIsNetworksDisabledDuringConnect) {
                HwWifi2SupplicantStateTracker.this.mIsNetworksDisabledDuringConnect = false;
            }
        }

        public boolean processMessage(Message message) {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Wifi2CompletedState processMessage = %{public}s", new Object[]{HwWifi2ClientModeImplConst.messageNumToString(message.what)});
            if (message.what != 147462) {
                return false;
            }
            if (message.obj instanceof StateChangeResult) {
                StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                SupplicantState state = stateChangeResult.state;
                HwWifi2SupplicantStateTracker hwWifi2SupplicantStateTracker = HwWifi2SupplicantStateTracker.this;
                hwWifi2SupplicantStateTracker.sendSupplicantStateChangedBroadcast(state, hwWifi2SupplicantStateTracker.mIsWifi2AuthFailureInSupplicantBroadcast, HwWifi2SupplicantStateTracker.this.mWifi2AuthFailureReason);
                if (!SupplicantState.isConnecting(state)) {
                    HwWifi2SupplicantStateTracker.this.transitionOnSupplicantStateChange(stateChangeResult);
                }
            }
            return true;
        }
    }

    class Wifi2DormantState extends State {
        Wifi2DormantState() {
        }

        public void enter() {
            HwHiLog.i(HwWifi2SupplicantStateTracker.TAG, false, "Enter Wifi2DormantState %{public}s", new Object[]{getName()});
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        HwWifi2SupplicantStateTracker.super.dump(fd, pw, args);
        pw.println("mIsWifi2AuthFailureInSupplicantBroadcast " + this.mIsWifi2AuthFailureInSupplicantBroadcast);
        pw.println("mWifi2AuthFailureReason " + this.mWifi2AuthFailureReason);
        pw.println("mIsNetworksDisabledDuringConnect " + this.mIsNetworksDisabledDuringConnect);
        pw.println();
    }
}
