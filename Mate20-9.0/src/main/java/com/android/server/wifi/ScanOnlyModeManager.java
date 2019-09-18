package com.android.server.wifi;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.IState;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.WifiNative;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ScanOnlyModeManager implements ActiveModeManager {
    private static final String TAG = "WifiScanOnlyModeManager";
    /* access modifiers changed from: private */
    public String mClientInterfaceName;
    private final Context mContext;
    private boolean mExpectedStop = false;
    /* access modifiers changed from: private */
    public boolean mIfaceIsUp = false;
    private final Listener mListener;
    /* access modifiers changed from: private */
    public final ScanRequestProxy mScanRequestProxy;
    /* access modifiers changed from: private */
    public final ScanOnlyModeStateMachine mStateMachine;
    /* access modifiers changed from: private */
    public final WakeupController mWakeupController;
    private final WifiMetrics mWifiMetrics;
    /* access modifiers changed from: private */
    public final WifiNative mWifiNative;

    public interface Listener {
        void onStateChanged(int i);
    }

    private class ScanOnlyModeStateMachine extends StateMachine {
        public static final int CMD_INTERFACE_DESTROYED = 4;
        public static final int CMD_INTERFACE_DOWN = 5;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_START = 0;
        /* access modifiers changed from: private */
        public final State mIdleState = new IdleState();
        /* access modifiers changed from: private */
        public final State mStartedState = new StartedState();
        /* access modifiers changed from: private */
        public final WifiNative.InterfaceCallback mWifiNativeInterfaceCallback = new WifiNative.InterfaceCallback() {
            public void onDestroyed(String ifaceName) {
                if (ScanOnlyModeManager.this.mClientInterfaceName != null && ScanOnlyModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    ScanOnlyModeStateMachine.this.sendMessage(4);
                }
            }

            public void onUp(String ifaceName) {
                if (ScanOnlyModeManager.this.mClientInterfaceName != null && ScanOnlyModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    ScanOnlyModeStateMachine.this.sendMessage(3, 1);
                }
            }

            public void onDown(String ifaceName) {
                if (ScanOnlyModeManager.this.mClientInterfaceName != null && ScanOnlyModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    ScanOnlyModeStateMachine.this.sendMessage(3, 0);
                }
            }
        };

        private class IdleState extends State {
            private IdleState() {
            }

            public void enter() {
                Log.d(ScanOnlyModeManager.TAG, "entering IdleState");
                String unused = ScanOnlyModeManager.this.mClientInterfaceName = null;
            }

            public boolean processMessage(Message message) {
                if (message.what != 0) {
                    Log.d(ScanOnlyModeManager.TAG, "received an invalid message: " + message);
                    return false;
                }
                String unused = ScanOnlyModeManager.this.mClientInterfaceName = ScanOnlyModeManager.this.mWifiNative.setupInterfaceForClientMode(true, ScanOnlyModeStateMachine.this.mWifiNativeInterfaceCallback);
                if (TextUtils.isEmpty(ScanOnlyModeManager.this.mClientInterfaceName)) {
                    Log.e(ScanOnlyModeManager.TAG, "Failed to create ClientInterface. Sit in Idle");
                    ScanOnlyModeManager.this.updateWifiState(4);
                } else {
                    ScanOnlyModeManager.this.sendScanAvailableBroadcast(false);
                    ScanOnlyModeManager.this.mScanRequestProxy.enableScanningForHiddenNetworks(false);
                    ScanOnlyModeManager.this.mScanRequestProxy.clearScanResults();
                    ScanOnlyModeStateMachine.this.transitionTo(ScanOnlyModeStateMachine.this.mStartedState);
                }
                return true;
            }
        }

        private class StartedState extends State {
            private StartedState() {
            }

            private void onUpChanged(boolean isUp) {
                if (isUp != ScanOnlyModeManager.this.mIfaceIsUp) {
                    boolean unused = ScanOnlyModeManager.this.mIfaceIsUp = isUp;
                    if (isUp) {
                        Log.d(ScanOnlyModeManager.TAG, "Wifi is ready to use for scanning");
                        ScanOnlyModeManager.this.mWakeupController.start();
                        ScanOnlyModeManager.this.sendScanAvailableBroadcast(true);
                        ScanOnlyModeManager.this.updateWifiState(3);
                    } else {
                        Log.d(ScanOnlyModeManager.TAG, "interface down - stop scan mode");
                        ScanOnlyModeManager.this.mStateMachine.sendMessage(5);
                    }
                }
            }

            public void enter() {
                Log.d(ScanOnlyModeManager.TAG, "entering StartedState");
                ScanOnlyModeManager.this.mScanRequestProxy.enableScanningForHiddenNetworks(false);
                boolean unused = ScanOnlyModeManager.this.mIfaceIsUp = false;
                onUpChanged(ScanOnlyModeManager.this.mWifiNative.isInterfaceUp(ScanOnlyModeManager.this.mClientInterfaceName));
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i != 0) {
                    boolean isUp = false;
                    switch (i) {
                        case 3:
                            if (message.arg1 == 1) {
                                isUp = true;
                            }
                            onUpChanged(isUp);
                            break;
                        case 4:
                            Log.d(ScanOnlyModeManager.TAG, "Interface cleanly destroyed, report scan mode stop.");
                            String unused = ScanOnlyModeManager.this.mClientInterfaceName = null;
                            ScanOnlyModeStateMachine.this.transitionTo(ScanOnlyModeStateMachine.this.mIdleState);
                            break;
                        case 5:
                            Log.d(ScanOnlyModeManager.TAG, "interface down!  stop mode");
                            ScanOnlyModeManager.this.updateWifiState(4);
                            ScanOnlyModeStateMachine.this.transitionTo(ScanOnlyModeStateMachine.this.mIdleState);
                            break;
                        default:
                            return false;
                    }
                }
                return true;
            }

            public void exit() {
                ScanOnlyModeManager.this.mWakeupController.stop();
                if (ScanOnlyModeManager.this.mClientInterfaceName != null) {
                    ScanOnlyModeManager.this.mWifiNative.teardownInterface(ScanOnlyModeManager.this.mClientInterfaceName);
                    String unused = ScanOnlyModeManager.this.mClientInterfaceName = null;
                }
                ScanOnlyModeManager.this.updateWifiState(1);
                ScanOnlyModeManager.this.mStateMachine.quitNow();
            }
        }

        ScanOnlyModeStateMachine(Looper looper) {
            super(ScanOnlyModeManager.TAG, looper);
            addState(this.mIdleState);
            addState(this.mStartedState);
            setInitialState(this.mIdleState);
            start();
        }
    }

    ScanOnlyModeManager(Context context, Looper looper, WifiNative wifiNative, Listener listener, WifiMetrics wifiMetrics, ScanRequestProxy scanRequestProxy, WakeupController wakeupController) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mListener = listener;
        this.mWifiMetrics = wifiMetrics;
        this.mScanRequestProxy = scanRequestProxy;
        this.mWakeupController = wakeupController;
        this.mStateMachine = new ScanOnlyModeStateMachine(looper);
    }

    public void start() {
        this.mStateMachine.sendMessage(0);
    }

    public void stop() {
        Log.d(TAG, " currentstate: " + getCurrentStateName());
        this.mExpectedStop = true;
        this.mStateMachine.quitNow();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("--Dump of ScanOnlyModeManager--");
        pw.println("current StateMachine mode: " + getCurrentStateName());
        pw.println("mClientInterfaceName: " + this.mClientInterfaceName);
        pw.println("mIfaceIsUp: " + this.mIfaceIsUp);
    }

    private String getCurrentStateName() {
        IState currentState = this.mStateMachine.getCurrentState();
        if (currentState != null) {
            return currentState.getName();
        }
        return "StateMachine not active";
    }

    /* access modifiers changed from: private */
    public void updateWifiState(int state) {
        if (this.mExpectedStop) {
            Log.d(TAG, "expected stop, not triggering callbacks: state = " + state);
            return;
        }
        if (state == 4 || state == 1) {
            this.mExpectedStop = true;
        }
        this.mListener.onStateChanged(state);
    }

    /* access modifiers changed from: private */
    public void sendScanAvailableBroadcast(boolean available) {
        sendScanAvailableBroadcast(this.mContext, available);
    }
}
