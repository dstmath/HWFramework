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
    private String mClientInterfaceName;
    private final Context mContext;
    private boolean mExpectedStop = false;
    private boolean mIfaceIsUp = false;
    private final Listener mListener;
    private final SarManager mSarManager;
    private final ScanOnlyModeStateMachine mStateMachine;
    private final WakeupController mWakeupController;
    private final WifiMetrics mWifiMetrics;
    private final WifiNative mWifiNative;

    public interface Listener {
        void onStateChanged(int i);
    }

    ScanOnlyModeManager(Context context, Looper looper, WifiNative wifiNative, Listener listener, WifiMetrics wifiMetrics, WakeupController wakeupController, SarManager sarManager) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mListener = listener;
        this.mWifiMetrics = wifiMetrics;
        this.mWakeupController = wakeupController;
        this.mSarManager = sarManager;
        this.mStateMachine = new ScanOnlyModeStateMachine(looper);
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void start() {
        this.mStateMachine.sendMessage(0);
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public void stop() {
        Log.i(TAG, " currentstate: " + getCurrentStateName());
        this.mExpectedStop = true;
        this.mStateMachine.quitNow();
    }

    @Override // com.android.server.wifi.ActiveModeManager
    public int getScanMode() {
        return 1;
    }

    @Override // com.android.server.wifi.ActiveModeManager
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
    /* access modifiers changed from: public */
    private void updateWifiState(int state) {
        if (this.mExpectedStop) {
            Log.i(TAG, "expected stop, not triggering callbacks: state = " + state);
            return;
        }
        if (state == 4 || state == 1) {
            this.mExpectedStop = true;
        }
        this.mListener.onStateChanged(state);
    }

    /* access modifiers changed from: private */
    public class ScanOnlyModeStateMachine extends StateMachine {
        public static final int CMD_INTERFACE_DESTROYED = 4;
        public static final int CMD_INTERFACE_DOWN = 5;
        public static final int CMD_INTERFACE_STATUS_CHANGED = 3;
        public static final int CMD_START = 0;
        private final State mIdleState = new IdleState();
        private final State mStartedState = new StartedState();
        private final WifiNative.InterfaceCallback mWifiNativeInterfaceCallback = new WifiNative.InterfaceCallback() {
            /* class com.android.server.wifi.ScanOnlyModeManager.ScanOnlyModeStateMachine.AnonymousClass1 */

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onDestroyed(String ifaceName) {
                if (ScanOnlyModeManager.this.mClientInterfaceName != null && ScanOnlyModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    ScanOnlyModeStateMachine.this.sendMessage(4);
                }
            }

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onUp(String ifaceName) {
                if (ScanOnlyModeManager.this.mClientInterfaceName != null && ScanOnlyModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    ScanOnlyModeStateMachine.this.sendMessage(3, 1);
                }
            }

            @Override // com.android.server.wifi.WifiNative.InterfaceCallback
            public void onDown(String ifaceName) {
                if (ScanOnlyModeManager.this.mClientInterfaceName != null && ScanOnlyModeManager.this.mClientInterfaceName.equals(ifaceName)) {
                    ScanOnlyModeStateMachine.this.sendMessage(3, 0);
                }
            }
        };

        ScanOnlyModeStateMachine(Looper looper) {
            super(ScanOnlyModeManager.TAG, looper);
            addState(this.mIdleState);
            addState(this.mStartedState);
            setInitialState(this.mIdleState);
            start();
        }

        private class IdleState extends State {
            private IdleState() {
            }

            public void enter() {
                Log.i(ScanOnlyModeManager.TAG, "entering IdleState");
                ScanOnlyModeManager.this.mClientInterfaceName = null;
            }

            public boolean processMessage(Message message) {
                if (message.what != 0) {
                    Log.i(ScanOnlyModeManager.TAG, "received an invalid message: " + message);
                    return false;
                }
                ScanOnlyModeManager.this.mClientInterfaceName = ScanOnlyModeManager.this.mWifiNative.setupInterfaceForClientInScanMode(ScanOnlyModeStateMachine.this.mWifiNativeInterfaceCallback);
                if (TextUtils.isEmpty(ScanOnlyModeManager.this.mClientInterfaceName)) {
                    Log.e(ScanOnlyModeManager.TAG, "Failed to create ClientInterface. Sit in Idle");
                    ScanOnlyModeManager.this.updateWifiState(4);
                    return true;
                }
                ScanOnlyModeStateMachine scanOnlyModeStateMachine = ScanOnlyModeStateMachine.this;
                scanOnlyModeStateMachine.transitionTo(scanOnlyModeStateMachine.mStartedState);
                return true;
            }
        }

        private class StartedState extends State {
            private StartedState() {
            }

            private void onUpChanged(boolean isUp) {
                if (isUp != ScanOnlyModeManager.this.mIfaceIsUp) {
                    ScanOnlyModeManager.this.mIfaceIsUp = isUp;
                    if (isUp) {
                        Log.i(ScanOnlyModeManager.TAG, "Wifi is ready to use for scanning");
                        ScanOnlyModeManager.this.mWakeupController.start();
                        ScanOnlyModeManager.this.updateWifiState(3);
                        return;
                    }
                    Log.i(ScanOnlyModeManager.TAG, "interface down - stop scan mode");
                    ScanOnlyModeManager.this.mStateMachine.sendMessage(5);
                }
            }

            public void enter() {
                Log.i(ScanOnlyModeManager.TAG, "entering StartedState");
                ScanOnlyModeManager.this.mIfaceIsUp = false;
                onUpChanged(ScanOnlyModeManager.this.mWifiNative.isInterfaceUp(ScanOnlyModeManager.this.mClientInterfaceName));
                ScanOnlyModeManager.this.mSarManager.setScanOnlyWifiState(3);
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i != 0) {
                    boolean isUp = false;
                    if (i == 3) {
                        if (message.arg1 == 1) {
                            isUp = true;
                        }
                        onUpChanged(isUp);
                    } else if (i == 4) {
                        Log.i(ScanOnlyModeManager.TAG, "Interface cleanly destroyed, report scan mode stop.");
                        ScanOnlyModeManager.this.mClientInterfaceName = null;
                        ScanOnlyModeStateMachine scanOnlyModeStateMachine = ScanOnlyModeStateMachine.this;
                        scanOnlyModeStateMachine.transitionTo(scanOnlyModeStateMachine.mIdleState);
                    } else if (i != 5) {
                        return false;
                    } else {
                        Log.i(ScanOnlyModeManager.TAG, "interface down!  stop mode");
                        ScanOnlyModeManager.this.updateWifiState(4);
                        ScanOnlyModeStateMachine scanOnlyModeStateMachine2 = ScanOnlyModeStateMachine.this;
                        scanOnlyModeStateMachine2.transitionTo(scanOnlyModeStateMachine2.mIdleState);
                    }
                }
                return true;
            }

            public void exit() {
                ScanOnlyModeManager.this.mWakeupController.stop();
                if (ScanOnlyModeManager.this.mClientInterfaceName != null) {
                    ScanOnlyModeManager.this.mWifiNative.teardownInterface(ScanOnlyModeManager.this.mClientInterfaceName);
                    ScanOnlyModeManager.this.mClientInterfaceName = null;
                }
                ScanOnlyModeManager.this.updateWifiState(1);
                ScanOnlyModeManager.this.mSarManager.setScanOnlyWifiState(1);
                ScanOnlyModeManager.this.mStateMachine.quitNow();
            }
        }
    }
}
