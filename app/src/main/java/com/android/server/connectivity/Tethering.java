package com.android.server.connectivity;

import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.INetworkStatsService;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkRequest;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.IState;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.HwServiceFactory;
import com.android.server.IoThread;
import com.android.server.NetPluginDelegate;
import com.android.server.job.controllers.JobStatus;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class Tethering extends BaseNetworkObserver {
    private static final boolean DBG = false;
    private static final String[] DHCP_DEFAULT_RANGE = null;
    private static final String DNS_DEFAULT_SERVER1 = "8.8.8.8";
    private static final String DNS_DEFAULT_SERVER2 = "8.8.4.4";
    private static final Integer DUN_TYPE = null;
    private static final Integer HIPRI_TYPE = null;
    private static final boolean HWDBG = false;
    protected static final boolean HWFLOW = false;
    private static boolean HWLOGW_E = false;
    private static final Integer MOBILE_TYPE = null;
    private static final String TAG = "Tethering";
    private static final ComponentName TETHER_SERVICE = null;
    private static final String USB_NEAR_IFACE_ADDR = "192.168.42.129";
    private static final int USB_PREFIX_LENGTH = 24;
    private static final boolean VDBG = false;
    private static final Class[] messageClasses = null;
    private static final SparseArray<String> sMagicDecoderRing = null;
    private final Context mContext;
    private String mCurrentUpstreamIface;
    private String[] mDefaultDnsServers;
    private String[] mDhcpRange;
    private HwNotificationTethering mHwNotificationTethering;
    private HashMap<String, TetherInterfaceSM> mIfaces;
    private int mLastNotificationId;
    private final Looper mLooper;
    private final INetworkManagementService mNMService;
    private int mPreferredUpstreamMobileApn;
    private final Object mPublicSync;
    private boolean mRndisEnabled;
    private BroadcastReceiver mStateReceiver;
    private final INetworkStatsService mStatsService;
    private final StateMachine mTetherMasterSM;
    private String[] mTetherableBluetoothRegexs;
    private String[] mTetherableUsbRegexs;
    private String[] mTetherableWifiRegexs;
    private Builder mTetheredNotificationBuilder;
    private Collection<Integer> mUpstreamIfaceTypes;
    private final UpstreamNetworkMonitor mUpstreamNetworkMonitor;
    private boolean mUsbTetherRequested;

    /* renamed from: com.android.server.connectivity.Tethering.2 */
    class AnonymousClass2 implements ServiceListener {
        final /* synthetic */ BluetoothAdapter val$adapter;
        final /* synthetic */ boolean val$enable;
        final /* synthetic */ ResultReceiver val$receiver;

        AnonymousClass2(boolean val$enable, ResultReceiver val$receiver, BluetoothAdapter val$adapter) {
            this.val$enable = val$enable;
            this.val$receiver = val$receiver;
            this.val$adapter = val$adapter;
        }

        public void onServiceDisconnected(int profile) {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            int result;
            ((BluetoothPan) proxy).setBluetoothTethering(this.val$enable);
            if (((BluetoothPan) proxy).isTetheringOn() == this.val$enable) {
                result = 0;
            } else {
                result = 5;
            }
            Tethering.this.sendTetherResult(this.val$receiver, result);
            if (this.val$enable && Tethering.this.isTetherProvisioningRequired()) {
                Tethering.this.scheduleProvisioningRechecks(2);
            }
            this.val$adapter.closeProfileProxy(5, proxy);
        }
    }

    /* renamed from: com.android.server.connectivity.Tethering.3 */
    class AnonymousClass3 extends ResultReceiver {
        final /* synthetic */ ResultReceiver val$receiver;
        final /* synthetic */ int val$type;

        AnonymousClass3(Handler $anonymous0, int val$type, ResultReceiver val$receiver) {
            this.val$type = val$type;
            this.val$receiver = val$receiver;
            super($anonymous0);
        }

        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 0) {
                Tethering.this.enableTetheringInternal(this.val$type, true, this.val$receiver);
            } else {
                Tethering.this.sendTetherResult(this.val$receiver, resultCode);
            }
        }
    }

    private class StateReceiver extends BroadcastReceiver {
        private StateReceiver() {
        }

        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "StateReceiver onReceive action:" + action);
                }
                if (action.equals("android.hardware.usb.action.USB_STATE")) {
                    synchronized (Tethering.this.mPublicSync) {
                        boolean usbConnected = intent.getBooleanExtra("connected", Tethering.HWFLOW);
                        Tethering.this.mRndisEnabled = intent.getBooleanExtra("rndis", Tethering.HWFLOW);
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "StateReceiver onReceive action synchronized: usbConnected = " + usbConnected + ", mRndisEnabled = " + Tethering.this.mRndisEnabled + ", mUsbTetherRequested = " + Tethering.this.mUsbTetherRequested);
                        }
                        if (usbConnected && Tethering.this.mRndisEnabled && Tethering.this.mUsbTetherRequested) {
                            Tethering.this.tetherUsb(true);
                        }
                        Tethering.this.mUsbTetherRequested = Tethering.HWFLOW;
                    }
                } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (!(networkInfo == null || networkInfo.getDetailedState() == DetailedState.FAILED)) {
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tethering got CONNECTIVITY_ACTION");
                        }
                        Tethering.this.mTetherMasterSM.sendMessage(327683);
                    }
                } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                    Tethering.this.updateConfiguration();
                }
            }
        }
    }

    class TetherInterfaceSM extends StateMachine {
        private static final int BASE_IFACE = 327780;
        static final int CMD_CELL_DUN_ERROR = 327786;
        static final int CMD_INTERFACE_DOWN = 327784;
        static final int CMD_INTERFACE_UP = 327785;
        static final int CMD_IP_FORWARDING_DISABLE_ERROR = 327788;
        static final int CMD_IP_FORWARDING_ENABLE_ERROR = 327787;
        static final int CMD_SET_DNS_FORWARDERS_ERROR = 327791;
        static final int CMD_START_TETHERING_ERROR = 327789;
        static final int CMD_STOP_TETHERING_ERROR = 327790;
        static final int CMD_TETHER_CONNECTION_CHANGED = 327792;
        static final int CMD_TETHER_MODE_DEAD = 327781;
        static final int CMD_TETHER_REQUESTED = 327782;
        static final int CMD_TETHER_UNREQUESTED = 327783;
        private boolean mAvailable;
        private State mDefaultState;
        String mIfaceName;
        private State mInitialState;
        int mLastError;
        String mMyUpstreamIfaceName;
        private State mStartingState;
        private boolean mTethered;
        private State mTetheredState;
        private State mUnavailableState;
        boolean mUsb;

        class InitialState extends State {
            InitialState() {
            }

            public void enter() {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "InitialState enter, sendTetherStateChangedBroadcast");
                }
                TetherInterfaceSM.this.setAvailable(true);
                TetherInterfaceSM.this.setTethered(Tethering.HWFLOW);
                HwServiceFactory.getHwConnectivityManager().setTetheringProp(Tethering.this, Tethering.HWFLOW, TetherInterfaceSM.this.mUsb, TetherInterfaceSM.this.mIfaceName);
                Tethering.this.sendTetherStateChangedBroadcast();
            }

            public boolean processMessage(Message message) {
                Tethering.this.maybeLogMessage(this, message.what);
                switch (message.what) {
                    case TetherInterfaceSM.CMD_TETHER_REQUESTED /*327782*/:
                        TetherInterfaceSM.this.setLastError(0);
                        Tethering.this.mTetherMasterSM.sendMessage(327681, TetherInterfaceSM.this);
                        TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mStartingState);
                        return true;
                    case TetherInterfaceSM.CMD_INTERFACE_DOWN /*327784*/:
                        TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mUnavailableState);
                        return true;
                    default:
                        return Tethering.HWFLOW;
                }
            }
        }

        class StartingState extends State {
            StartingState() {
            }

            public void enter() {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "StartingState enter");
                }
                TetherInterfaceSM.this.setAvailable(Tethering.HWFLOW);
                if (!TetherInterfaceSM.this.mUsb || Tethering.this.configureUsbIface(true)) {
                    HwServiceFactory.getHwConnectivityManager().setTetheringProp(Tethering.this, true, TetherInterfaceSM.this.mUsb, TetherInterfaceSM.this.mIfaceName);
                    if (Tethering.DBG) {
                        Log.d(Tethering.TAG, "StartingState sendTetherStateChangedBroadcast");
                    }
                    Tethering.this.sendTetherStateChangedBroadcast();
                    TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mTetheredState);
                    return;
                }
                Tethering.this.mTetherMasterSM.sendMessage(327682, TetherInterfaceSM.this);
                TetherInterfaceSM.this.setLastError(10);
                TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mInitialState);
            }

            public boolean processMessage(Message message) {
                Tethering.this.maybeLogMessage(this, message.what);
                switch (message.what) {
                    case TetherInterfaceSM.CMD_TETHER_UNREQUESTED /*327783*/:
                        Tethering.this.mTetherMasterSM.sendMessage(327682, TetherInterfaceSM.this);
                        if (!TetherInterfaceSM.this.mUsb || Tethering.this.configureUsbIface(Tethering.HWFLOW)) {
                            TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mInitialState);
                            return true;
                        }
                        TetherInterfaceSM.this.setLastErrorAndTransitionToInitialState(10);
                        return true;
                    case TetherInterfaceSM.CMD_INTERFACE_DOWN /*327784*/:
                        Tethering.this.mTetherMasterSM.sendMessage(327682, TetherInterfaceSM.this);
                        TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mUnavailableState);
                        return true;
                    case TetherInterfaceSM.CMD_CELL_DUN_ERROR /*327786*/:
                    case TetherInterfaceSM.CMD_IP_FORWARDING_ENABLE_ERROR /*327787*/:
                    case TetherInterfaceSM.CMD_IP_FORWARDING_DISABLE_ERROR /*327788*/:
                    case TetherInterfaceSM.CMD_START_TETHERING_ERROR /*327789*/:
                    case TetherInterfaceSM.CMD_STOP_TETHERING_ERROR /*327790*/:
                    case TetherInterfaceSM.CMD_SET_DNS_FORWARDERS_ERROR /*327791*/:
                        TetherInterfaceSM.this.setLastErrorAndTransitionToInitialState(5);
                        return true;
                    default:
                        return Tethering.HWFLOW;
                }
            }
        }

        class TetheredState extends State {
            TetheredState() {
            }

            public void enter() {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "TetheredState enter");
                }
                try {
                    if (Tethering.this.isWifi(TetherInterfaceSM.this.mIfaceName)) {
                        Tethering.this.mNMService.enableIpv6(TetherInterfaceSM.this.mIfaceName);
                    }
                } catch (RemoteException e) {
                    Log.e(Tethering.TAG, "Failed to enable IPv6:RemoteException");
                } catch (IllegalStateException e2) {
                    Log.e(Tethering.TAG, "Failed to enable IPv6:IllegalStateException");
                }
                try {
                    Tethering.this.mNMService.tetherInterface(TetherInterfaceSM.this.mIfaceName);
                    if (Tethering.DBG) {
                        Log.d(Tethering.TAG, "Tethered " + TetherInterfaceSM.this.mIfaceName);
                    }
                    TetherInterfaceSM.this.setAvailable(Tethering.HWFLOW);
                    TetherInterfaceSM.this.setTethered(true);
                    HwServiceFactory.getHwConnectivityManager().setTetheringProp(Tethering.this, true, TetherInterfaceSM.this.mUsb, TetherInterfaceSM.this.mIfaceName);
                    Tethering.this.sendTetherStateChangedBroadcast();
                } catch (Exception e3) {
                    Log.e(Tethering.TAG, "Error Tethering: " + e3.toString());
                    TetherInterfaceSM.this.setLastError(6);
                    try {
                        Tethering.this.mNMService.untetherInterface(TetherInterfaceSM.this.mIfaceName);
                    } catch (Exception ee) {
                        Log.e(Tethering.TAG, "Error untethering after failure!" + ee.toString());
                    }
                    TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mInitialState);
                }
            }

            private void cleanupUpstream() {
                if (TetherInterfaceSM.this.mMyUpstreamIfaceName != null) {
                    try {
                        Tethering.this.mStatsService.forceUpdate();
                    } catch (Exception e) {
                        if (Tethering.HWLOGW_E) {
                            Log.e(Tethering.TAG, "Exception in forceUpdate: " + e.toString());
                        }
                    }
                    try {
                        Tethering.this.mNMService.stopInterfaceForwarding(TetherInterfaceSM.this.mIfaceName, TetherInterfaceSM.this.mMyUpstreamIfaceName);
                    } catch (Exception e2) {
                        if (Tethering.HWLOGW_E) {
                            Log.e(Tethering.TAG, "Exception in removeInterfaceForward: " + e2.toString());
                        }
                    }
                    try {
                        Tethering.this.mNMService.disableNat(TetherInterfaceSM.this.mIfaceName, TetherInterfaceSM.this.mMyUpstreamIfaceName);
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "cleanupUpstream disableNat(" + TetherInterfaceSM.this.mIfaceName + ", " + TetherInterfaceSM.this.mMyUpstreamIfaceName + ")");
                        }
                    } catch (Exception e22) {
                        if (Tethering.HWLOGW_E) {
                            Log.e(Tethering.TAG, "Exception in disableNat: " + e22.toString());
                        }
                    }
                    TetherInterfaceSM.this.mMyUpstreamIfaceName = null;
                }
            }

            public boolean processMessage(Message message) {
                Tethering.this.maybeLogMessage(this, message.what);
                boolean retValue = true;
                boolean error = Tethering.HWFLOW;
                switch (message.what) {
                    case TetherInterfaceSM.CMD_TETHER_MODE_DEAD /*327781*/:
                        break;
                    case TetherInterfaceSM.CMD_TETHER_UNREQUESTED /*327783*/:
                    case TetherInterfaceSM.CMD_INTERFACE_DOWN /*327784*/:
                        cleanupUpstream();
                        try {
                            Tethering.this.mNMService.untetherInterface(TetherInterfaceSM.this.mIfaceName);
                            Tethering.this.mTetherMasterSM.sendMessage(327682, TetherInterfaceSM.this);
                            if (message.what == TetherInterfaceSM.CMD_TETHER_UNREQUESTED) {
                                if (TetherInterfaceSM.this.mUsb && !Tethering.this.configureUsbIface(Tethering.HWFLOW)) {
                                    TetherInterfaceSM.this.setLastError(10);
                                }
                                TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mInitialState);
                            } else if (message.what == TetherInterfaceSM.CMD_INTERFACE_DOWN) {
                                TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mUnavailableState);
                            }
                            if (Tethering.DBG) {
                                Log.d(Tethering.TAG, "Untethered " + TetherInterfaceSM.this.mIfaceName);
                                break;
                            }
                        } catch (Exception e) {
                            TetherInterfaceSM.this.setLastErrorAndTransitionToInitialState(7);
                            break;
                        }
                        break;
                    case TetherInterfaceSM.CMD_CELL_DUN_ERROR /*327786*/:
                    case TetherInterfaceSM.CMD_IP_FORWARDING_ENABLE_ERROR /*327787*/:
                    case TetherInterfaceSM.CMD_IP_FORWARDING_DISABLE_ERROR /*327788*/:
                    case TetherInterfaceSM.CMD_START_TETHERING_ERROR /*327789*/:
                    case TetherInterfaceSM.CMD_STOP_TETHERING_ERROR /*327790*/:
                    case TetherInterfaceSM.CMD_SET_DNS_FORWARDERS_ERROR /*327791*/:
                        error = true;
                        break;
                    case TetherInterfaceSM.CMD_TETHER_CONNECTION_CHANGED /*327792*/:
                        String newUpstreamIfaceName = message.obj;
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "TetheredState CMD_TETHER_CONNECTION_CHANGED mMyUpstreamIfaceName: " + TetherInterfaceSM.this.mMyUpstreamIfaceName + ", newUpstreamIfaceName: " + newUpstreamIfaceName);
                        }
                        if ((TetherInterfaceSM.this.mMyUpstreamIfaceName == null && newUpstreamIfaceName == null) || (TetherInterfaceSM.this.mMyUpstreamIfaceName != null && TetherInterfaceSM.this.mMyUpstreamIfaceName.equals(newUpstreamIfaceName))) {
                            if (Tethering.VDBG) {
                                Log.d(Tethering.TAG, "Connection changed noop - dropping");
                                break;
                            }
                        }
                        cleanupUpstream();
                        if (newUpstreamIfaceName != null) {
                            if (Tethering.HWDBG) {
                                Log.d(Tethering.TAG, "tether connection changed, mIfaceName=" + TetherInterfaceSM.this.mIfaceName + ", newUpstreamIfaceName" + newUpstreamIfaceName);
                            }
                            try {
                                Tethering.this.mNMService.enableNat(TetherInterfaceSM.this.mIfaceName, newUpstreamIfaceName);
                                Tethering.this.mNMService.startInterfaceForwarding(TetherInterfaceSM.this.mIfaceName, newUpstreamIfaceName);
                            } catch (Exception e2) {
                                Log.e(Tethering.TAG, "Exception enabling Nat: " + e2.toString());
                                try {
                                    Tethering.this.mNMService.disableNat(TetherInterfaceSM.this.mIfaceName, newUpstreamIfaceName);
                                } catch (Exception ee) {
                                    if (Tethering.DBG) {
                                        Log.e(Tethering.TAG, "TetheredState untetherInterface failed, exception: " + ee);
                                    }
                                }
                                try {
                                    Tethering.this.mNMService.untetherInterface(TetherInterfaceSM.this.mIfaceName);
                                } catch (Exception e3) {
                                }
                                TetherInterfaceSM.this.setLastError(8);
                                TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mInitialState);
                                return true;
                            }
                        }
                        TetherInterfaceSM.this.mMyUpstreamIfaceName = newUpstreamIfaceName;
                        break;
                        break;
                    default:
                        retValue = Tethering.HWFLOW;
                        break;
                }
                cleanupUpstream();
                try {
                    Tethering.this.mNMService.untetherInterface(TetherInterfaceSM.this.mIfaceName);
                    if (error) {
                        TetherInterfaceSM.this.setLastErrorAndTransitionToInitialState(5);
                    } else {
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "Tether lost upstream connection " + TetherInterfaceSM.this.mIfaceName);
                        }
                        Tethering.this.sendTetherStateChangedBroadcast();
                        if (TetherInterfaceSM.this.mUsb && !Tethering.this.configureUsbIface(Tethering.HWFLOW)) {
                            TetherInterfaceSM.this.setLastError(10);
                        }
                        TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mInitialState);
                    }
                } catch (Exception e4) {
                    TetherInterfaceSM.this.setLastErrorAndTransitionToInitialState(7);
                }
                return retValue;
            }
        }

        class UnavailableState extends State {
            UnavailableState() {
            }

            public void enter() {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "[ISM_Unavailable] enter, sendTetherStateChangedBroadcast");
                }
                TetherInterfaceSM.this.setAvailable(Tethering.HWFLOW);
                TetherInterfaceSM.this.setLastError(0);
                TetherInterfaceSM.this.setTethered(Tethering.HWFLOW);
                HwServiceFactory.getHwConnectivityManager().setTetheringProp(Tethering.this, Tethering.HWFLOW, TetherInterfaceSM.this.mUsb, TetherInterfaceSM.this.mIfaceName);
                Tethering.this.sendTetherStateChangedBroadcast();
            }

            public boolean processMessage(Message message) {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "[ISM_Unavailable] processMessage what=" + message.what);
                }
                switch (message.what) {
                    case TetherInterfaceSM.CMD_INTERFACE_UP /*327785*/:
                        TetherInterfaceSM.this.transitionTo(TetherInterfaceSM.this.mInitialState);
                        return true;
                    default:
                        return Tethering.HWFLOW;
                }
            }
        }

        TetherInterfaceSM(String name, Looper looper, boolean usb) {
            super(name, looper);
            this.mIfaceName = name;
            this.mUsb = usb;
            setLastError(0);
            this.mInitialState = new InitialState();
            addState(this.mInitialState);
            this.mStartingState = new StartingState();
            addState(this.mStartingState);
            this.mTetheredState = new TetheredState();
            addState(this.mTetheredState);
            this.mUnavailableState = new UnavailableState();
            addState(this.mUnavailableState);
            setInitialState(this.mInitialState);
        }

        public String toString() {
            String res = new String() + this.mIfaceName + " - ";
            IState current = getCurrentState();
            if (current == this.mInitialState) {
                res = res + "InitialState";
            }
            if (current == this.mStartingState) {
                res = res + "StartingState";
            }
            if (current == this.mTetheredState) {
                res = res + "TetheredState";
            }
            if (current == this.mUnavailableState) {
                res = res + "UnavailableState";
            }
            if (this.mAvailable) {
                res = res + " - Available";
            }
            if (this.mTethered) {
                res = res + " - Tethered";
            }
            return res + " - lastError =" + this.mLastError;
        }

        public int getLastError() {
            int i;
            synchronized (Tethering.this.mPublicSync) {
                if (Tethering.VDBG) {
                    Log.d(Tethering.TAG, "getLastError:" + this.mLastError);
                }
                i = this.mLastError;
            }
            return i;
        }

        private void setLastError(int error) {
            synchronized (Tethering.this.mPublicSync) {
                this.mLastError = error;
                if (Tethering.VDBG) {
                    Log.d(Tethering.TAG, "setLastError: " + this.mLastError);
                }
                if (isErrored() && this.mUsb) {
                    Tethering.this.configureUsbIface(Tethering.HWFLOW);
                }
            }
        }

        public boolean isAvailable() {
            boolean z;
            synchronized (Tethering.this.mPublicSync) {
                z = this.mAvailable;
            }
            return z;
        }

        private void setAvailable(boolean available) {
            synchronized (Tethering.this.mPublicSync) {
                this.mAvailable = available;
            }
        }

        public boolean isTethered() {
            boolean z;
            synchronized (Tethering.this.mPublicSync) {
                z = this.mTethered;
            }
            return z;
        }

        private void setTethered(boolean tethered) {
            synchronized (Tethering.this.mPublicSync) {
                this.mTethered = tethered;
            }
        }

        public boolean isErrored() {
            boolean z = Tethering.HWFLOW;
            synchronized (Tethering.this.mPublicSync) {
                if (this.mLastError != 0) {
                    z = true;
                }
            }
            return z;
        }

        void setLastErrorAndTransitionToInitialState(int error) {
            setLastError(error);
            transitionTo(this.mInitialState);
        }
    }

    class TetherMasterSM extends StateMachine {
        private static final int BASE_MASTER = 327680;
        static final int CMD_RETRY_UPSTREAM = 327684;
        static final int CMD_TETHER_MODE_REQUESTED = 327681;
        static final int CMD_TETHER_MODE_UNREQUESTED = 327682;
        static final int CMD_UPSTREAM_CHANGED = 327683;
        static final int EVENT_UPSTREAM_LINKPROPERTIES_CHANGED = 327685;
        static final int EVENT_UPSTREAM_LOST = 327686;
        private static final int UPSTREAM_SETTLE_TIME_MS = 10000;
        private SimChangeBroadcastReceiver mBroadcastReceiver;
        private State mInitialState;
        private int mMobileApnReserved;
        private NetworkCallback mMobileUpstreamCallback;
        private NetworkCallback mNetworkCallback;
        private ArrayList<TetherInterfaceSM> mNotifyList;
        private int mSequenceNumber;
        private State mSetDnsForwardersErrorState;
        private State mSetIpForwardingDisabledErrorState;
        private State mSetIpForwardingEnabledErrorState;
        private final AtomicInteger mSimBcastGenerationNumber;
        private State mStartTetheringErrorState;
        private State mStopTetheringErrorState;
        private State mTetherModeAliveState;
        boolean prevIPV6Connected;

        class ErrorState extends State {
            int mErrorNotification;

            ErrorState() {
            }

            public boolean processMessage(Message message) {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "ErrorState processMessage what=" + message.what);
                }
                switch (message.what) {
                    case TetherMasterSM.CMD_TETHER_MODE_REQUESTED /*327681*/:
                        message.obj.sendMessage(this.mErrorNotification);
                        return true;
                    default:
                        return Tethering.HWFLOW;
                }
            }

            void notify(int msgType) {
                this.mErrorNotification = msgType;
                Iterator<TetherInterfaceSM> iterator = TetherMasterSM.this.mNotifyList.iterator();
                while (iterator.hasNext()) {
                    TetherInterfaceSM sm = (TetherInterfaceSM) iterator.next();
                    sm.sendMessage(msgType);
                    if (msgType == 327791 && TetherMasterSM.this.mNotifyList.indexOf(sm) != -1) {
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "ErrorState removing " + sm);
                        }
                        iterator.remove();
                    }
                }
                if (msgType == 327791) {
                    Log.e(Tethering.TAG, "msgType=" + msgType + ",transitionTo mInitialState");
                    TetherMasterSM.this.transitionTo(TetherMasterSM.this.mInitialState);
                }
            }
        }

        class TetherMasterUtilState extends State {
            TetherMasterUtilState() {
            }

            public boolean processMessage(Message m) {
                return Tethering.HWFLOW;
            }

            protected boolean turnOnUpstreamMobileConnection(int apnType) {
                if (apnType == -1) {
                    return Tethering.HWFLOW;
                }
                if (apnType != TetherMasterSM.this.mMobileApnReserved) {
                    turnOffUpstreamMobileConnection();
                }
                if (TetherMasterSM.this.mMobileUpstreamCallback != null) {
                    return true;
                }
                switch (apnType) {
                    case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                    case H.DO_TRAVERSAL /*4*/:
                    case H.ADD_STARTING /*5*/:
                        TetherMasterSM.this.mMobileApnReserved = apnType;
                        NetworkRequest.Builder builder = new NetworkRequest.Builder().addTransportType(0);
                        if (apnType == 4) {
                            builder.removeCapability(13).addCapability(2);
                        } else {
                            builder.addCapability(12);
                        }
                        NetworkRequest mobileUpstreamRequest = builder.build();
                        TetherMasterSM.this.mMobileUpstreamCallback = new NetworkCallback();
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "requesting mobile upstream network: " + mobileUpstreamRequest);
                        }
                        Tethering.this.getConnectivityManager().requestNetwork(mobileUpstreamRequest, TetherMasterSM.this.mMobileUpstreamCallback, 0, apnType);
                        return true;
                    default:
                        return Tethering.HWFLOW;
                }
            }

            protected void turnOffUpstreamMobileConnection() {
                if (TetherMasterSM.this.mMobileUpstreamCallback != null) {
                    Tethering.this.getConnectivityManager().unregisterNetworkCallback(TetherMasterSM.this.mMobileUpstreamCallback);
                    TetherMasterSM.this.mMobileUpstreamCallback = null;
                }
                TetherMasterSM.this.mMobileApnReserved = -1;
            }

            protected boolean turnOnMasterTetherSettings() {
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(true);
                    try {
                        Tethering.this.mNMService.startTethering(Tethering.this.mDhcpRange);
                        Log.e(Tethering.TAG, "sleep 100ms to wait for dnsmaq start up completely.");
                        Thread.currentThread();
                        Thread.sleep(100);
                    } catch (Exception e) {
                        try {
                            Tethering.this.mNMService.stopTethering();
                            Tethering.this.mNMService.startTethering(Tethering.this.mDhcpRange);
                        } catch (Exception e2) {
                            TetherMasterSM.this.transitionTo(TetherMasterSM.this.mStartTetheringErrorState);
                            return Tethering.HWFLOW;
                        }
                    }
                    return true;
                } catch (Exception e3) {
                    TetherMasterSM.this.transitionTo(TetherMasterSM.this.mSetIpForwardingEnabledErrorState);
                    return Tethering.HWFLOW;
                }
            }

            protected boolean turnOffMasterTetherSettings() {
                try {
                    Tethering.this.mNMService.stopTethering();
                    try {
                        Tethering.this.mNMService.setIpForwardingEnabled(Tethering.HWFLOW);
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mInitialState);
                        return true;
                    } catch (Exception e) {
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mSetIpForwardingDisabledErrorState);
                        return Tethering.HWFLOW;
                    }
                } catch (Exception e2) {
                    TetherMasterSM.this.transitionTo(TetherMasterSM.this.mStopTetheringErrorState);
                    return Tethering.HWFLOW;
                }
            }

            protected void addUpstreamV6Interface(String iface) {
                INetworkManagementService service = Stub.asInterface(ServiceManager.getService("network_management"));
                Log.d(Tethering.TAG, "adding v6 interface " + iface);
                try {
                    service.addUpstreamV6Interface(iface);
                } catch (RemoteException e) {
                    Log.e(Tethering.TAG, "Unable to append v6 upstream interface");
                }
            }

            protected void removeUpstreamV6Interface(String iface) {
                INetworkManagementService service = Stub.asInterface(ServiceManager.getService("network_management"));
                Log.d(Tethering.TAG, "removing v6 interface " + iface);
                try {
                    service.removeUpstreamV6Interface(iface);
                } catch (RemoteException e) {
                    Log.e(Tethering.TAG, "Unable to remove v6 upstream interface");
                }
            }

            private boolean isIpv6Connected(LinkProperties lp) {
                boolean ret = Tethering.HWFLOW;
                if (lp == null) {
                    return Tethering.HWFLOW;
                }
                try {
                    for (InetAddress addr : lp.getAddresses()) {
                        if (addr instanceof Inet6Address) {
                            Inet6Address i6addr = (Inet6Address) addr;
                            if (!(i6addr.isAnyLocalAddress() || i6addr.isLinkLocalAddress() || i6addr.isLoopbackAddress() || i6addr.isMulticastAddress())) {
                                ret = true;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(Tethering.TAG, "Exception getting LinkProperties", e);
                }
                return ret;
            }

            private NetworkRequest getNetworkRequest(int upType) {
                int ncType;
                if (Tethering.VDBG) {
                    Log.d(Tethering.TAG, "getNetworkRequest upType=" + upType);
                }
                int transportType = -1;
                switch (upType) {
                    case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                    case H.ADD_STARTING /*5*/:
                        ncType = 12;
                        transportType = 0;
                        break;
                    case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                        ncType = 12;
                        transportType = 1;
                        break;
                    case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                        ncType = 0;
                        transportType = 0;
                        break;
                    case H.REPORT_LOSING_FOCUS /*3*/:
                        ncType = 1;
                        transportType = 0;
                        break;
                    case H.DO_TRAVERSAL /*4*/:
                        ncType = 2;
                        transportType = 0;
                        break;
                    case H.FINISHED_STARTING /*7*/:
                        ncType = 12;
                        transportType = 2;
                        break;
                    case H.REPORT_APPLICATION_TOKEN_DRAWN /*9*/:
                        ncType = 12;
                        transportType = 3;
                        break;
                    case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                        ncType = 3;
                        transportType = 0;
                        break;
                    case H.WINDOW_FREEZE_TIMEOUT /*11*/:
                        ncType = 4;
                        transportType = 0;
                        break;
                    case AppTransition.TRANSIT_WALLPAPER_CLOSE /*12*/:
                        ncType = 5;
                        transportType = 0;
                        break;
                    case H.APP_TRANSITION_TIMEOUT /*13*/:
                        ncType = 6;
                        transportType = 1;
                        break;
                    case H.PERSIST_ANIMATION_SCALE /*14*/:
                        ncType = 7;
                        transportType = 0;
                        break;
                    case H.FORCE_GC /*15*/:
                        ncType = 10;
                        transportType = 0;
                        break;
                    default:
                        ncType = -1;
                        break;
                }
                if (Tethering.VDBG) {
                    Log.d(Tethering.TAG, "ncType =" + ncType + " transportType = " + transportType);
                }
                return new NetworkRequest.Builder().addCapability(ncType).addTransportType(transportType).build();
            }

            private NetworkCallback getNetworkCallback() {
                return new NetworkCallback() {
                    boolean currentIPV6Connected;
                    String currentUpstreamIface;
                    String lastUpstreamIface;

                    {
                        this.lastUpstreamIface = null;
                        this.currentIPV6Connected = Tethering.HWFLOW;
                    }

                    public void onAvailable(Network network) {
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "network available: " + network);
                        }
                        try {
                            LinkProperties lp = Tethering.this.getConnectivityManager().getLinkProperties(network);
                            this.currentIPV6Connected = TetherMasterUtilState.this.isIpv6Connected(lp);
                            if (lp != null) {
                                this.currentUpstreamIface = lp.getInterfaceName();
                            }
                            this.lastUpstreamIface = this.currentUpstreamIface;
                            if (TetherMasterSM.this.prevIPV6Connected != this.currentIPV6Connected && this.currentIPV6Connected) {
                                TetherMasterUtilState.this.addUpstreamV6Interface(this.currentUpstreamIface);
                            }
                        } catch (Exception e) {
                            Log.e(Tethering.TAG, "Exception querying ConnectivityManager", e);
                        }
                        TetherMasterSM.this.prevIPV6Connected = this.currentIPV6Connected;
                    }

                    public void onLost(Network network) {
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "network lost: " + network.toString());
                        }
                        if (TetherMasterSM.this.mNetworkCallback != null) {
                            TetherMasterUtilState.this.removeUpstreamV6Interface(this.lastUpstreamIface);
                            if (Tethering.DBG) {
                                Log.d(Tethering.TAG, "Unregistering NetworkCallback()");
                            }
                            Tethering.this.getConnectivityManager().unregisterNetworkCallback(TetherMasterSM.this.mNetworkCallback);
                            TetherMasterSM.this.mNetworkCallback = null;
                            TetherMasterSM.this.prevIPV6Connected = Tethering.HWFLOW;
                            this.lastUpstreamIface = null;
                        }
                    }

                    public void onLinkPropertiesChanged(Network network, LinkProperties lp) {
                        this.currentIPV6Connected = TetherMasterUtilState.this.isIpv6Connected(lp);
                        this.currentUpstreamIface = lp.getInterfaceName();
                        this.lastUpstreamIface = this.currentUpstreamIface;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "NetworkCallback.onLinkPropertiesChanged: network=" + network + ", LP = " + lp + "currentIPV6Connected=" + this.currentIPV6Connected + "prevIPV6Connected =" + TetherMasterSM.this.prevIPV6Connected);
                        }
                        if (TetherMasterSM.this.prevIPV6Connected != this.currentIPV6Connected) {
                            if (this.currentIPV6Connected) {
                                TetherMasterUtilState.this.addUpstreamV6Interface(this.currentUpstreamIface);
                            } else {
                                TetherMasterUtilState.this.removeUpstreamV6Interface(this.currentUpstreamIface);
                            }
                            TetherMasterSM.this.prevIPV6Connected = this.currentIPV6Connected;
                        }
                    }
                };
            }

            protected void chooseUpstreamType(boolean tryCell) {
                int upType = -1;
                String iface = null;
                Tethering.this.updateConfiguration();
                synchronized (Tethering.this.mPublicSync) {
                    if (Tethering.VDBG) {
                        Log.d(Tethering.TAG, "chooseUpstreamType has upstream iface types:");
                        for (Integer netType : Tethering.this.mUpstreamIfaceTypes) {
                            Log.d(Tethering.TAG, " " + netType);
                        }
                    }
                    for (Integer netType2 : Tethering.this.mUpstreamIfaceTypes) {
                        NetworkInfo info = Tethering.this.getConnectivityManager().getNetworkInfo(netType2.intValue());
                        if (info != null && info.isConnected()) {
                            upType = netType2.intValue();
                            break;
                        }
                    }
                    if (upType == 0 && TetherMasterSM.this.mMobileApnReserved == 5) {
                        info = Tethering.this.getConnectivityManager().getNetworkInfo(5);
                        if (info != null && info.isConnected()) {
                            if (Tethering.DBG) {
                                Log.d(Tethering.TAG, "hipri connected, ignore default mobile upstream");
                            }
                            upType = 5;
                        }
                    }
                }
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "chooseUpstreamType(" + tryCell + ")," + " preferredApn=" + ConnectivityManager.getNetworkTypeName(Tethering.this.mPreferredUpstreamMobileApn) + ", got type=" + ConnectivityManager.getNetworkTypeName(upType));
                }
                switch (upType) {
                    case AppTransition.TRANSIT_UNSET /*-1*/:
                        if (!(tryCell && turnOnUpstreamMobileConnection(Tethering.this.mPreferredUpstreamMobileApn))) {
                            TetherMasterSM.this.sendMessageDelayed(TetherMasterSM.CMD_RETRY_UPSTREAM, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                            break;
                        }
                    case H.DO_TRAVERSAL /*4*/:
                    case H.ADD_STARTING /*5*/:
                        turnOnUpstreamMobileConnection(upType);
                        break;
                    default:
                        turnOffUpstreamMobileConnection();
                        break;
                }
                if (upType != -1) {
                    Network network = Tethering.this.getConnectivityManager().getNetworkForType(upType);
                    if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                        NetPluginDelegate.setUpstream(network);
                    } else {
                        try {
                            info = Tethering.this.getConnectivityManager().getNetworkInfo(upType);
                            if (info != null && info.isConnected() && TetherMasterSM.this.mNetworkCallback == null) {
                                TetherMasterSM.this.mNetworkCallback = getNetworkCallback();
                                NetworkRequest networkRequest = getNetworkRequest(upType);
                                if (Tethering.DBG) {
                                    Log.d(Tethering.TAG, "Registering NetworkCallback");
                                }
                                Tethering.this.getConnectivityManager().registerNetworkCallback(networkRequest, TetherMasterSM.this.mNetworkCallback);
                            }
                        } catch (Exception e) {
                            Log.e(Tethering.TAG, "Exception querying ConnectivityManager", e);
                        }
                    }
                    LinkProperties linkProperties = Tethering.this.getConnectivityManager().getLinkProperties(upType);
                    if (linkProperties != null) {
                        if (Tethering.DBG) {
                            Log.i(Tethering.TAG, "Finding IPv4 upstream interface on: " + linkProperties);
                        }
                        RouteInfo ipv4Default = RouteInfo.selectBestRoute(linkProperties.getAllRoutes(), Inet4Address.ANY);
                        if (ipv4Default != null) {
                            iface = ipv4Default.getInterface();
                            if (Tethering.VDBG) {
                                Log.i(Tethering.TAG, "Found interface " + ipv4Default.getInterface());
                            }
                        } else {
                            Log.i(Tethering.TAG, "No IPv4 upstream interface, giving up.");
                        }
                    }
                    if (iface != null) {
                        if (network == null) {
                            Log.e(Tethering.TAG, "No Network for upstream type " + upType + "!");
                        }
                        setDnsForwarders(network, linkProperties);
                    }
                }
                notifyTetheredOfNewUpstreamIface(iface);
            }

            protected void setDnsForwarders(Network network, LinkProperties lp) {
                String[] dnsServers = Tethering.this.mDefaultDnsServers;
                Collection<InetAddress> dnses = lp.getDnsServers();
                if (!(dnses == null || dnses.isEmpty())) {
                    dnsServers = NetworkUtils.makeStrings(dnses);
                }
                if (Tethering.VDBG) {
                    Log.d(Tethering.TAG, "Setting DNS forwarders: Network=" + network + ", dnsServers=" + Arrays.toString(dnsServers));
                }
                try {
                    Tethering.this.mNMService.setDnsForwarders(network, dnsServers);
                } catch (Exception e) {
                    Log.e(Tethering.TAG, "Setting DNS forwarders failed!");
                    TetherMasterSM.this.transitionTo(TetherMasterSM.this.mSetDnsForwardersErrorState);
                }
            }

            protected void notifyTetheredOfNewUpstreamIface(String ifaceName) {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "Notifying tethered with upstream=" + ifaceName);
                }
                Tethering.this.mCurrentUpstreamIface = ifaceName;
                for (TetherInterfaceSM sm : TetherMasterSM.this.mNotifyList) {
                    sm.sendMessage(327792, ifaceName);
                }
            }
        }

        class InitialState extends TetherMasterUtilState {
            InitialState() {
                super();
            }

            public void enter() {
            }

            public boolean processMessage(Message message) {
                Tethering.this.maybeLogMessage(this, message.what);
                TetherInterfaceSM who;
                switch (message.what) {
                    case TetherMasterSM.CMD_TETHER_MODE_REQUESTED /*327681*/:
                        who = message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode requested by " + who);
                        }
                        TetherMasterSM.this.mNotifyList.add(who);
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mTetherModeAliveState);
                        return true;
                    case TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED /*327682*/:
                        who = (TetherInterfaceSM) message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode unrequested by " + who);
                        }
                        if (TetherMasterSM.this.mNotifyList.indexOf(who) == -1) {
                            return true;
                        }
                        TetherMasterSM.this.mNotifyList.remove(who);
                        return true;
                    default:
                        return Tethering.HWFLOW;
                }
            }
        }

        class SetDnsForwardersErrorState extends ErrorState {
            SetDnsForwardersErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in setDnsForwarders");
                notify(327791);
                try {
                    Tethering.this.mNMService.stopTethering();
                } catch (Exception e) {
                }
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(Tethering.HWFLOW);
                } catch (Exception e2) {
                }
            }
        }

        class SetIpForwardingDisabledErrorState extends ErrorState {
            SetIpForwardingDisabledErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in setIpForwardingDisabled");
                notify(327788);
            }
        }

        class SetIpForwardingEnabledErrorState extends ErrorState {
            SetIpForwardingEnabledErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in setIpForwardingEnabled");
                notify(327787);
            }
        }

        class SimChangeBroadcastReceiver extends BroadcastReceiver {
            private final int mGenerationNumber;
            private boolean mSimNotLoadedSeen;

            public SimChangeBroadcastReceiver(int generationNumber) {
                this.mSimNotLoadedSeen = Tethering.HWFLOW;
                this.mGenerationNumber = generationNumber;
            }

            public void onReceive(Context context, Intent intent) {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "simchange mGenerationNumber=" + this.mGenerationNumber + ", current generationNumber=" + TetherMasterSM.this.mSimBcastGenerationNumber.get());
                }
                if (this.mGenerationNumber == TetherMasterSM.this.mSimBcastGenerationNumber.get()) {
                    String state = intent.getStringExtra("ss");
                    if (Tethering.DBG) {
                        Log.d(Tethering.TAG, "got Sim changed to state " + state + ", mSimNotLoadedSeen=" + this.mSimNotLoadedSeen);
                    }
                    if (!(this.mSimNotLoadedSeen || "LOADED".equals(state))) {
                        this.mSimNotLoadedSeen = true;
                    }
                    if (this.mSimNotLoadedSeen && "LOADED".equals(state)) {
                        this.mSimNotLoadedSeen = Tethering.HWFLOW;
                        try {
                            if (Tethering.this.mContext.getResources().getString(17039412).isEmpty()) {
                                Log.d(Tethering.TAG, "no prov-check needed for new SIM");
                            } else {
                                ArrayList<Integer> tethered = new ArrayList();
                                synchronized (Tethering.this.mPublicSync) {
                                    for (Object iface : Tethering.this.mIfaces.keySet()) {
                                        TetherInterfaceSM sm = (TetherInterfaceSM) Tethering.this.mIfaces.get(iface);
                                        if (sm != null && sm.isTethered()) {
                                            if (Tethering.this.isUsb((String) iface)) {
                                                tethered.add(new Integer(1));
                                            } else {
                                                if (Tethering.this.isWifi((String) iface)) {
                                                    tethered.add(new Integer(0));
                                                } else if (Tethering.this.isBluetooth((String) iface)) {
                                                    tethered.add(new Integer(2));
                                                }
                                            }
                                        }
                                    }
                                }
                                for (Integer intValue : tethered) {
                                    int tetherType = intValue.intValue();
                                    Intent startProvIntent = new Intent();
                                    startProvIntent.putExtra("extraAddTetherType", tetherType);
                                    startProvIntent.putExtra("extraRunProvision", true);
                                    startProvIntent.setComponent(Tethering.TETHER_SERVICE);
                                    Tethering.this.mContext.startServiceAsUser(startProvIntent, UserHandle.CURRENT);
                                }
                                Log.d(Tethering.TAG, "re-evaluate provisioning");
                            }
                        } catch (NotFoundException e) {
                            Log.d(Tethering.TAG, "no prov-check needed for new SIM");
                        }
                    }
                }
            }
        }

        class StartTetheringErrorState extends ErrorState {
            StartTetheringErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in startTethering");
                notify(327789);
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(Tethering.HWFLOW);
                } catch (Exception e) {
                }
            }
        }

        class StopTetheringErrorState extends ErrorState {
            StopTetheringErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in stopTethering");
                notify(327790);
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(Tethering.HWFLOW);
                } catch (Exception e) {
                }
            }
        }

        class TetherModeAliveState extends TetherMasterUtilState {
            boolean mTryCell;

            TetherModeAliveState() {
                super();
                this.mTryCell = true;
            }

            public void enter() {
                boolean z = true;
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "TetherModeAliveState enter");
                }
                turnOnMasterTetherSettings();
                TetherMasterSM.this.startListeningForSimChanges();
                Tethering.this.mUpstreamNetworkMonitor.start();
                this.mTryCell = true;
                chooseUpstreamType(this.mTryCell);
                if (this.mTryCell) {
                    z = Tethering.HWFLOW;
                }
                this.mTryCell = z;
            }

            public void exit() {
                turnOffUpstreamMobileConnection();
                Tethering.this.mUpstreamNetworkMonitor.stop();
                TetherMasterSM.this.stopListeningForSimChanges();
                notifyTetheredOfNewUpstreamIface(null);
            }

            public boolean processMessage(Message message) {
                boolean z = Tethering.HWFLOW;
                Tethering.this.maybeLogMessage(this, message.what);
                TetherInterfaceSM who;
                switch (message.what) {
                    case TetherMasterSM.CMD_TETHER_MODE_REQUESTED /*327681*/:
                        who = message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode requested by " + who);
                        }
                        TetherMasterSM.this.mNotifyList.add(who);
                        who.sendMessage(327792, Tethering.this.mCurrentUpstreamIface);
                        return true;
                    case TetherMasterSM.CMD_TETHER_MODE_UNREQUESTED /*327682*/:
                        who = (TetherInterfaceSM) message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode unrequested by " + who);
                        }
                        int index = TetherMasterSM.this.mNotifyList.indexOf(who);
                        if (index != -1) {
                            if (Tethering.DBG) {
                                Log.d(Tethering.TAG, "TetherModeAlive removing notifyee " + who);
                            }
                            TetherMasterSM.this.mNotifyList.remove(index);
                            if (TetherMasterSM.this.mNotifyList.isEmpty()) {
                                turnOffMasterTetherSettings();
                                return true;
                            } else if (!Tethering.DBG) {
                                return true;
                            } else {
                                Log.d(Tethering.TAG, "TetherModeAlive still has " + TetherMasterSM.this.mNotifyList.size() + " live requests:");
                                for (Object o : TetherMasterSM.this.mNotifyList) {
                                    Log.d(Tethering.TAG, "  " + o);
                                }
                                return true;
                            }
                        }
                        Log.e(Tethering.TAG, "TetherModeAliveState UNREQUESTED has unknown who: " + who);
                        return true;
                    case TetherMasterSM.CMD_UPSTREAM_CHANGED /*327683*/:
                        this.mTryCell = true;
                        chooseUpstreamType(this.mTryCell);
                        if (!this.mTryCell) {
                            z = true;
                        }
                        this.mTryCell = z;
                        return true;
                    case TetherMasterSM.CMD_RETRY_UPSTREAM /*327684*/:
                        chooseUpstreamType(this.mTryCell);
                        if (!this.mTryCell) {
                            z = true;
                        }
                        this.mTryCell = z;
                        return true;
                    case TetherMasterSM.EVENT_UPSTREAM_LINKPROPERTIES_CHANGED /*327685*/:
                        NetworkState state = message.obj;
                        if (Tethering.this.mUpstreamNetworkMonitor.processLinkPropertiesChanged(state)) {
                            setDnsForwarders(state.network, state.linkProperties);
                            return true;
                        } else if (Tethering.this.mCurrentUpstreamIface != null) {
                            return true;
                        } else {
                            chooseUpstreamType(Tethering.HWFLOW);
                            return true;
                        }
                    case TetherMasterSM.EVENT_UPSTREAM_LOST /*327686*/:
                        Tethering.this.mUpstreamNetworkMonitor.processNetworkLost((Network) message.obj);
                        return true;
                    default:
                        return Tethering.HWFLOW;
                }
            }
        }

        TetherMasterSM(String name, Looper looper) {
            super(name, looper);
            this.mMobileApnReserved = -1;
            this.mNetworkCallback = null;
            this.prevIPV6Connected = Tethering.HWFLOW;
            this.mSimBcastGenerationNumber = new AtomicInteger(0);
            this.mBroadcastReceiver = null;
            this.mInitialState = new InitialState();
            addState(this.mInitialState);
            this.mTetherModeAliveState = new TetherModeAliveState();
            addState(this.mTetherModeAliveState);
            this.mSetIpForwardingEnabledErrorState = new SetIpForwardingEnabledErrorState();
            addState(this.mSetIpForwardingEnabledErrorState);
            this.mSetIpForwardingDisabledErrorState = new SetIpForwardingDisabledErrorState();
            addState(this.mSetIpForwardingDisabledErrorState);
            this.mStartTetheringErrorState = new StartTetheringErrorState();
            addState(this.mStartTetheringErrorState);
            this.mStopTetheringErrorState = new StopTetheringErrorState();
            addState(this.mStopTetheringErrorState);
            this.mSetDnsForwardersErrorState = new SetDnsForwardersErrorState();
            addState(this.mSetDnsForwardersErrorState);
            this.mNotifyList = new ArrayList();
            setInitialState(this.mInitialState);
        }

        private void startListeningForSimChanges() {
            if (Tethering.DBG) {
                Log.d(Tethering.TAG, "startListeningForSimChanges");
            }
            if (this.mBroadcastReceiver == null) {
                this.mBroadcastReceiver = new SimChangeBroadcastReceiver(this.mSimBcastGenerationNumber.incrementAndGet());
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.SIM_STATE_CHANGED");
                Tethering.this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
            }
        }

        private void stopListeningForSimChanges() {
            if (Tethering.DBG) {
                Log.d(Tethering.TAG, "stopListeningForSimChanges");
            }
            if (this.mBroadcastReceiver != null) {
                this.mSimBcastGenerationNumber.incrementAndGet();
                Tethering.this.mContext.unregisterReceiver(this.mBroadcastReceiver);
                this.mBroadcastReceiver = null;
            }
        }
    }

    class UpstreamNetworkCallback extends NetworkCallback {
        UpstreamNetworkCallback() {
        }

        public void onLinkPropertiesChanged(Network network, LinkProperties newLp) {
            Tethering.this.mTetherMasterSM.sendMessage(327685, new NetworkState(null, newLp, null, network, null, null));
        }

        public void onLost(Network network) {
            Tethering.this.mTetherMasterSM.sendMessage(327686, network);
        }
    }

    class UpstreamNetworkMonitor {
        NetworkCallback mDefaultNetworkCallback;
        NetworkCallback mDunTetheringCallback;
        final HashMap<Network, NetworkState> mNetworkMap;

        UpstreamNetworkMonitor() {
            this.mNetworkMap = new HashMap();
        }

        void start() {
            stop();
            this.mDefaultNetworkCallback = new UpstreamNetworkCallback();
            Tethering.this.getConnectivityManager().registerDefaultNetworkCallback(this.mDefaultNetworkCallback);
            NetworkRequest dunTetheringRequest = new NetworkRequest.Builder().addTransportType(0).removeCapability(13).addCapability(2).build();
            this.mDunTetheringCallback = new UpstreamNetworkCallback();
            Tethering.this.getConnectivityManager().registerNetworkCallback(dunTetheringRequest, this.mDunTetheringCallback);
        }

        void stop() {
            if (this.mDefaultNetworkCallback != null) {
                Tethering.this.getConnectivityManager().unregisterNetworkCallback(this.mDefaultNetworkCallback);
                this.mDefaultNetworkCallback = null;
            }
            if (this.mDunTetheringCallback != null) {
                Tethering.this.getConnectivityManager().unregisterNetworkCallback(this.mDunTetheringCallback);
                this.mDunTetheringCallback = null;
            }
            this.mNetworkMap.clear();
        }

        boolean processLinkPropertiesChanged(NetworkState networkState) {
            if (networkState == null || networkState.network == null || networkState.linkProperties == null) {
                return Tethering.HWFLOW;
            }
            this.mNetworkMap.put(networkState.network, networkState);
            if (Tethering.this.mCurrentUpstreamIface != null) {
                for (String ifname : networkState.linkProperties.getAllInterfaceNames()) {
                    if (Tethering.this.mCurrentUpstreamIface.equals(ifname)) {
                        return true;
                    }
                }
            }
            return Tethering.HWFLOW;
        }

        void processNetworkLost(Network network) {
            if (network != null) {
                this.mNetworkMap.remove(network);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.connectivity.Tethering.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.connectivity.Tethering.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.Tethering.<clinit>():void");
    }

    public Tethering(Context context, INetworkManagementService nmService, INetworkStatsService statsService) {
        this.mPreferredUpstreamMobileApn = -1;
        this.mContext = context;
        this.mNMService = nmService;
        this.mStatsService = statsService;
        this.mPublicSync = new Object();
        HwCustTethering mCust = (HwCustTethering) HwCustUtils.createObj(HwCustTethering.class, new Object[]{this.mContext});
        this.mIfaces = new HashMap();
        this.mLooper = IoThread.get().getLooper();
        this.mTetherMasterSM = new TetherMasterSM("TetherMaster", this.mLooper);
        this.mTetherMasterSM.start();
        this.mUpstreamNetworkMonitor = new UpstreamNetworkMonitor();
        this.mStateReceiver = new StateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiver(this.mStateReceiver, filter);
        if (mCust != null) {
            mCust.registerBroadcast(this.mPublicSync, this, this.mIfaces);
        }
        filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_SHARED");
        filter.addAction("android.intent.action.MEDIA_UNSHARED");
        filter.addDataScheme("file");
        this.mContext.registerReceiver(this.mStateReceiver, filter);
        this.mDhcpRange = context.getResources().getStringArray(17235991);
        if (this.mDhcpRange.length == 0 || this.mDhcpRange.length % 2 == 1) {
            this.mDhcpRange = DHCP_DEFAULT_RANGE;
        }
        updateConfiguration();
        this.mDefaultDnsServers = new String[2];
        this.mDefaultDnsServers[0] = DNS_DEFAULT_SERVER1;
        this.mDefaultDnsServers[1] = DNS_DEFAULT_SERVER2;
        this.mHwNotificationTethering = HwServiceFactory.getHwNotificationTethering(this.mContext);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("com.android.server.connectivity.action.STOP_TETHERING".equals(intent.getAction())) {
                    Tethering.this.mHwNotificationTethering.stopTethering();
                    Tethering.this.clearTetheredNotification();
                }
            }
        }, new IntentFilter("com.android.server.connectivity.action.STOP_TETHERING"), "com.android.server.connectivity.permission.STOP_TETHERING", null);
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) this.mContext.getSystemService("connectivity");
    }

    void updateConfiguration() {
        String[] tetherableUsbRegexs = this.mContext.getResources().getStringArray(17235987);
        String[] tetherableWifiRegexs = this.mContext.getResources().getStringArray(17235988);
        String[] tetherableBluetoothRegexs = this.mContext.getResources().getStringArray(17235990);
        int[] ifaceTypes = this.mContext.getResources().getIntArray(17235993);
        Collection<Integer> upstreamIfaceTypes = new ArrayList();
        for (int i : ifaceTypes) {
            if (VDBG) {
                Log.d(TAG, "upstreamIfaceTypes.add:" + i);
            }
            upstreamIfaceTypes.add(new Integer(i));
        }
        synchronized (this.mPublicSync) {
            this.mTetherableUsbRegexs = tetherableUsbRegexs;
            this.mTetherableWifiRegexs = tetherableWifiRegexs;
            this.mTetherableBluetoothRegexs = tetherableBluetoothRegexs;
            this.mUpstreamIfaceTypes = upstreamIfaceTypes;
        }
        checkDunRequired();
    }

    public void interfaceStatusChanged(String iface, boolean up) {
        if (VDBG) {
            Log.d(TAG, "interfaceStatusChanged " + iface + ", " + up);
        }
        boolean found = HWFLOW;
        boolean usb = HWFLOW;
        synchronized (this.mPublicSync) {
            if (isWifi(iface)) {
                found = true;
            } else if (isUsb(iface)) {
                found = true;
                usb = true;
            } else if (isBluetooth(iface)) {
                found = true;
            } else if (HwServiceFactory.getHwConnectivityManager().isP2pTether(iface)) {
                found = true;
            }
            if (found) {
                TetherInterfaceSM sm = (TetherInterfaceSM) this.mIfaces.get(iface);
                if (up) {
                    if (sm == null) {
                        sm = new TetherInterfaceSM(iface, this.mLooper, usb);
                        this.mIfaces.put(iface, sm);
                        sm.start();
                    }
                } else if (isUsb(iface)) {
                    if (VDBG) {
                        Log.d(TAG, "ignore interface down for " + iface);
                    }
                } else if (sm != null) {
                    if (VDBG) {
                        Log.d(TAG, "interfaceLinkStatusChanged, sm!=null, sendMessage:CMD_INTERFACE_DOWN");
                    }
                    sm.sendMessage(327784);
                    this.mIfaces.remove(iface);
                }
                return;
            }
        }
    }

    public void interfaceLinkStateChanged(String iface, boolean up) {
        interfaceStatusChanged(iface, up);
    }

    private boolean isUsb(String iface) {
        synchronized (this.mPublicSync) {
            for (String regex : this.mTetherableUsbRegexs) {
                if (iface.matches(regex)) {
                    return true;
                }
            }
            return HWFLOW;
        }
    }

    public boolean isWifi(String iface) {
        synchronized (this.mPublicSync) {
            for (String regex : this.mTetherableWifiRegexs) {
                if (iface.matches(regex)) {
                    return true;
                }
            }
            return HWFLOW;
        }
    }

    public boolean isBluetooth(String iface) {
        synchronized (this.mPublicSync) {
            for (String regex : this.mTetherableBluetoothRegexs) {
                if (iface.matches(regex)) {
                    return true;
                }
            }
            return HWFLOW;
        }
    }

    public void interfaceAdded(String iface) {
        if (VDBG) {
            Log.d(TAG, "interfaceAdded " + iface);
        }
        boolean found = HWFLOW;
        boolean usb = HWFLOW;
        synchronized (this.mPublicSync) {
            if (isWifi(iface)) {
                found = true;
            }
            if (isUsb(iface)) {
                found = true;
                usb = true;
            }
            if (isBluetooth(iface)) {
                found = true;
            }
            if (HwServiceFactory.getHwConnectivityManager().isP2pTether(iface)) {
                found = true;
            }
            if (!found) {
                if (VDBG) {
                    Log.d(TAG, iface + " is not a tetherable iface, ignoring");
                }
            } else if (((TetherInterfaceSM) this.mIfaces.get(iface)) != null) {
                if (VDBG) {
                    Log.d(TAG, "active iface (" + iface + ") reported as added, ignoring");
                }
            } else {
                TetherInterfaceSM sm = new TetherInterfaceSM(iface, this.mLooper, usb);
                this.mIfaces.put(iface, sm);
                sm.start();
            }
        }
    }

    public void interfaceRemoved(String iface) {
        if (VDBG) {
            Log.d(TAG, "interfaceRemoved " + iface);
        }
        synchronized (this.mPublicSync) {
            TetherInterfaceSM sm = (TetherInterfaceSM) this.mIfaces.get(iface);
            if (sm == null) {
                if (HWLOGW_E) {
                    Log.e(TAG, "attempting to remove unknown iface (" + iface + "), ignoring");
                }
                return;
            }
            sm.sendMessage(327784);
            this.mIfaces.remove(iface);
        }
    }

    public void startTethering(int type, ResultReceiver receiver, boolean showProvisioningUi) {
        if (isTetherProvisioningRequired()) {
            if (showProvisioningUi) {
                runUiTetherProvisioningAndEnable(type, receiver);
            } else {
                runSilentTetherProvisioningAndEnable(type, receiver);
            }
            return;
        }
        enableTetheringInternal(type, true, receiver);
    }

    public void stopTethering(int type) {
        enableTetheringInternal(type, HWFLOW, null);
        if (isTetherProvisioningRequired()) {
            cancelTetherProvisioningRechecks(type);
        }
    }

    private boolean isTetherProvisioningRequired() {
        boolean z = HWFLOW;
        String[] provisionApp = this.mContext.getResources().getStringArray(17235992);
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", HWFLOW) || provisionApp == null) {
            return HWFLOW;
        }
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        boolean isEntitlementCheckRequired = HWFLOW;
        if (!(configManager == null || configManager.getConfig() == null)) {
            isEntitlementCheckRequired = configManager.getConfig().getBoolean("require_entitlement_checks_bool");
        }
        if (!isEntitlementCheckRequired) {
            return HWFLOW;
        }
        if (provisionApp.length == 2) {
            z = true;
        }
        return z;
    }

    private void enableTetheringInternal(int type, boolean enable, ResultReceiver receiver) {
        boolean isProvisioningRequired = isTetherProvisioningRequired();
        switch (type) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                if (((WifiManager) this.mContext.getSystemService("wifi")).setWifiApEnabled(null, enable)) {
                    sendTetherResult(receiver, 0);
                    if (enable && isProvisioningRequired) {
                        scheduleProvisioningRechecks(type);
                        return;
                    }
                    return;
                }
                sendTetherResult(receiver, 5);
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                int result = setUsbTethering(enable);
                if (enable && isProvisioningRequired && result == 0) {
                    scheduleProvisioningRechecks(type);
                }
                sendTetherResult(receiver, result);
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                setBluetoothTethering(enable, receiver);
            case H.REPORT_LOSING_FOCUS /*3*/:
                if (VDBG) {
                    Log.d(TAG, "type: " + type + " enable: " + enable);
                }
                if (!enable) {
                    HwServiceFactory.getHwConnectivityManager().stopP2pTether(this.mContext);
                }
            default:
                Log.w(TAG, "Invalid tether type.");
                sendTetherResult(receiver, 1);
        }
    }

    private void sendTetherResult(ResultReceiver receiver, int result) {
        if (receiver != null) {
            receiver.send(result, null);
        }
    }

    private void setBluetoothTethering(boolean enable, ResultReceiver receiver) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Log.w(TAG, "Tried to enable bluetooth tethering with null or disabled adapter. null: " + (adapter == null ? true : HWFLOW));
            sendTetherResult(receiver, 2);
            return;
        }
        adapter.getProfileProxy(this.mContext, new AnonymousClass2(enable, receiver, adapter), 5);
    }

    private void runUiTetherProvisioningAndEnable(int type, ResultReceiver receiver) {
        sendUiTetherProvisionIntent(type, getProxyReceiver(type, receiver));
    }

    private void sendUiTetherProvisionIntent(int type, ResultReceiver receiver) {
        Intent intent = new Intent("android.settings.TETHER_PROVISIONING_UI");
        intent.putExtra("extraAddTetherType", type);
        intent.putExtra("extraProvisionCallback", receiver);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private ResultReceiver getProxyReceiver(int type, ResultReceiver receiver) {
        ResultReceiver rr = new AnonymousClass3(null, type, receiver);
        Parcel parcel = Parcel.obtain();
        rr.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ResultReceiver receiverForSending = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return receiverForSending;
    }

    private void scheduleProvisioningRechecks(int type) {
        Intent intent = new Intent();
        intent.putExtra("extraAddTetherType", type);
        intent.putExtra("extraSetAlarm", true);
        intent.setComponent(TETHER_SERVICE);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void runSilentTetherProvisioningAndEnable(int type, ResultReceiver receiver) {
        sendSilentTetherProvisionIntent(type, getProxyReceiver(type, receiver));
    }

    private void sendSilentTetherProvisionIntent(int type, ResultReceiver receiver) {
        Intent intent = new Intent();
        intent.putExtra("extraAddTetherType", type);
        intent.putExtra("extraRunProvision", true);
        intent.putExtra("extraProvisionCallback", receiver);
        intent.setComponent(TETHER_SERVICE);
        long ident = Binder.clearCallingIdentity();
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void cancelTetherProvisioningRechecks(int type) {
        if (getConnectivityManager().isTetheringSupported()) {
            Intent intent = new Intent();
            intent.putExtra("extraRemTetherType", type);
            intent.setComponent(TETHER_SERVICE);
            long ident = Binder.clearCallingIdentity();
            try {
                this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public int tether(String iface) {
        if (DBG) {
            Log.d(TAG, "Tethering " + iface);
        }
        synchronized (this.mPublicSync) {
            TetherInterfaceSM sm = (TetherInterfaceSM) this.mIfaces.get(iface);
        }
        if (sm == null) {
            Log.e(TAG, "Tried to Tether an unknown iface :" + iface + ", ignoring");
            return 1;
        } else if (sm.isAvailable() || sm.isErrored()) {
            sm.sendMessage(327782);
            return 0;
        } else {
            Log.e(TAG, "Tried to Tether an unavailable iface :" + iface + ", ignoring");
            return 4;
        }
    }

    public int untether(String iface) {
        if (DBG) {
            Log.d(TAG, "Untethering " + iface);
        }
        synchronized (this.mPublicSync) {
            TetherInterfaceSM sm = (TetherInterfaceSM) this.mIfaces.get(iface);
        }
        if (sm == null) {
            Log.e(TAG, "Tried to Untether an unknown iface :" + iface + ", ignoring");
            return 1;
        } else if (sm.isErrored()) {
            Log.e(TAG, "Tried to Untethered an errored iface :" + iface + ", ignoring");
            return 4;
        } else {
            sm.sendMessage(327783);
            return 0;
        }
    }

    public void untetherAll() {
        if (DBG) {
            Log.d(TAG, "Untethering " + this.mIfaces);
        }
        for (String iface : this.mIfaces.keySet()) {
            untether(iface);
        }
    }

    public int getLastTetherError(String iface) {
        synchronized (this.mPublicSync) {
            TetherInterfaceSM sm = (TetherInterfaceSM) this.mIfaces.get(iface);
            if (sm == null) {
                Log.e(TAG, "Tried to getLastTetherError on an unknown iface :" + iface + ", ignoring");
                return 1;
            }
            int lastError = sm.getLastError();
            return lastError;
        }
    }

    private void sendTetherStateChangedBroadcast() {
        if (getConnectivityManager().isTetheringSupported()) {
            String str;
            ArrayList<String> availableList = new ArrayList();
            ArrayList<String> activeList = new ArrayList();
            ArrayList<String> erroredList = new ArrayList();
            boolean wifiTethered = HWFLOW;
            boolean usbTethered = HWFLOW;
            boolean bluetoothTethered = HWFLOW;
            boolean p2pTethered = HWFLOW;
            synchronized (this.mPublicSync) {
                for (Object iface : this.mIfaces.keySet()) {
                    TetherInterfaceSM sm = (TetherInterfaceSM) this.mIfaces.get(iface);
                    if (sm != null) {
                        if (sm.isErrored()) {
                            erroredList.add((String) iface);
                        } else if (sm.isAvailable()) {
                            availableList.add((String) iface);
                        } else if (sm.isTethered()) {
                            if (isUsb((String) iface)) {
                                usbTethered = true;
                            } else {
                                if (isWifi((String) iface)) {
                                    wifiTethered = true;
                                } else {
                                    if (isBluetooth((String) iface)) {
                                        bluetoothTethered = true;
                                    } else {
                                        str = (String) iface;
                                        if (HwServiceFactory.getHwConnectivityManager().isP2pTether(str)) {
                                            p2pTethered = true;
                                        }
                                    }
                                }
                            }
                            activeList.add((String) iface);
                        }
                    }
                }
            }
            Intent broadcast = new Intent("android.net.conn.TETHER_STATE_CHANGED");
            broadcast.addFlags(603979776);
            broadcast.putStringArrayListExtra("availableArray", availableList);
            broadcast.putStringArrayListExtra("activeArray", activeList);
            broadcast.putStringArrayListExtra("erroredArray", erroredList);
            long broadcastId = SystemClock.elapsedRealtime();
            Log.d(TAG, "sendTetherStateChangedBroadcast: broadcastId= " + broadcastId);
            broadcast.putExtra("broadcastId", broadcastId);
            this.mContext.sendStickyBroadcastAsUser(broadcast, UserHandle.ALL);
            if (DBG) {
                str = TAG;
                Object[] objArr = new Object[3];
                objArr[0] = TextUtils.join(",", availableList);
                objArr[1] = TextUtils.join(",", activeList);
                objArr[2] = TextUtils.join(",", erroredList);
                Log.d(str, String.format("sendTetherStateChangedBroadcast avail=[%s] active=[%s] error=[%s]", objArr));
            }
            this.mHwNotificationTethering.setTetheringNumber(wifiTethered, usbTethered, bluetoothTethered);
            if (usbTethered || wifiTethered || bluetoothTethered || p2pTethered) {
                showTetheredNotification(this.mHwNotificationTethering.getTetheredIcon(usbTethered, wifiTethered, bluetoothTethered, p2pTethered));
            } else {
                clearTetheredNotification();
            }
        }
    }

    private void showTetheredNotification(int icon) {
        if (DBG) {
            Log.d(TAG, "showTetheredNotification icon:" + icon);
        }
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (notificationManager != null) {
            if (this.mLastNotificationId != 0) {
                if (this.mLastNotificationId == icon) {
                    this.mHwNotificationTethering.sendTetherNotification();
                    return;
                } else {
                    notificationManager.cancelAsUser(null, this.mLastNotificationId, UserHandle.ALL);
                    this.mLastNotificationId = 0;
                }
            }
            Intent intent = new Intent();
            if (17303284 == icon) {
                intent.setAction("android.settings.WIFI_AP_SETTINGS");
            } else {
                intent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
            }
            PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT);
            PendingIntent pIntentCancel = PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.server.connectivity.action.STOP_TETHERING"), 134217728);
            Resources r = Resources.getSystem();
            CharSequence title = r.getText(33685840);
            CharSequence message = r.getText(33685841);
            CharSequence close_shortcut = r.getText(33685842);
            if (this.mTetheredNotificationBuilder == null) {
                this.mTetheredNotificationBuilder = new Builder(this.mContext);
                this.mTetheredNotificationBuilder.setWhen(0).setOngoing(HWFLOW).setVisibility(1).setCategory("status").addAction(new Action(0, close_shortcut, pIntentCancel));
            }
            this.mTetheredNotificationBuilder.setSmallIcon(getNotificationBitampIcon(icon)).setContentTitle(title).setContentText(message).setContentIntent(pi);
            Notification notification = this.mTetheredNotificationBuilder.build();
            notification.icon = icon;
            this.mLastNotificationId = icon;
            this.mHwNotificationTethering.sendTetherNotification(notification, title, message, pi);
        }
    }

    private Icon getNotificationBitampIcon(int resId) {
        Config config;
        Drawable drawable = this.mContext.getResources().getDrawable(resId);
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return Icon.createWithBitmap(bitmap);
    }

    private void clearTetheredNotification() {
        if (DBG) {
            Log.d(TAG, "clearTetheredNotification");
        }
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (notificationManager != null && this.mLastNotificationId != 0) {
            notificationManager.cancelAsUser(null, this.mLastNotificationId, UserHandle.ALL);
            this.mLastNotificationId = 0;
            this.mHwNotificationTethering.clearTetheredNotification();
        }
    }

    private void tetherUsb(boolean enable) {
        int i = 0;
        if (VDBG) {
            Log.d(TAG, "tetherUsb " + enable);
        }
        String[] ifaces = new String[0];
        try {
            ifaces = this.mNMService.listInterfaces();
            int length = ifaces.length;
            while (i < length) {
                String iface = ifaces[i];
                if (isUsb(iface)) {
                    if ((enable ? tether(iface) : untether(iface)) == 0) {
                        return;
                    }
                }
                i++;
            }
            Log.e(TAG, "unable start or stop USB tethering");
        } catch (Exception e) {
            Log.e(TAG, "Error listing Interfaces", e);
        }
    }

    private boolean configureUsbIface(boolean enabled) {
        if (VDBG) {
            Log.d(TAG, "configureUsbIface(" + enabled + ")");
        }
        String[] ifaces = new String[0];
        try {
            for (String iface : this.mNMService.listInterfaces()) {
                if (isUsb(iface)) {
                    InterfaceConfiguration ifcg = this.mNMService.getInterfaceConfig(iface);
                    if (ifcg != null) {
                        ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(USB_NEAR_IFACE_ADDR), USB_PREFIX_LENGTH));
                        if (enabled) {
                            ifcg.setInterfaceUp();
                        } else {
                            try {
                                ifcg.setInterfaceDown();
                            } catch (Exception e) {
                                Log.e(TAG, "Error configuring interface " + iface, e);
                                return HWFLOW;
                            }
                        }
                        ifcg.clearFlag("running");
                        this.mNMService.setInterfaceConfig(iface, ifcg);
                    } else {
                        continue;
                    }
                }
            }
            return true;
        } catch (Exception e2) {
            Log.e(TAG, "Error listing Interfaces", e2);
            return HWFLOW;
        }
    }

    public String[] getTetherableUsbRegexs() {
        return this.mTetherableUsbRegexs;
    }

    public String[] getTetherableWifiRegexs() {
        return this.mTetherableWifiRegexs;
    }

    public String[] getTetherableBluetoothRegexs() {
        return this.mTetherableBluetoothRegexs;
    }

    public int setUsbTethering(boolean r9) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.server.connectivity.Tethering.setUsbTethering(boolean):int. bs: [B:11:0x003f, B:26:0x006b]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r8 = this;
        r7 = 0;
        r3 = VDBG;
        if (r3 == 0) goto L_0x0026;
    L_0x0005:
        r3 = "Tethering";
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "setUsbTethering(";
        r4 = r4.append(r5);
        r4 = r4.append(r9);
        r5 = ")";
        r4 = r4.append(r5);
        r4 = r4.toString();
        android.util.Log.d(r3, r4);
    L_0x0026:
        r3 = r8.mContext;
        r4 = "usb";
        r2 = r3.getSystemService(r4);
        r2 = (android.hardware.usb.UsbManager) r2;
        r4 = r8.mPublicSync;
        monitor-enter(r4);
        if (r9 == 0) goto L_0x0066;
    L_0x0036:
        r3 = r8.mRndisEnabled;	 Catch:{ all -> 0x004c }
        if (r3 == 0) goto L_0x004f;	 Catch:{ all -> 0x004c }
    L_0x003a:
        r0 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x004c }
        r3 = 1;
        r8.tetherUsb(r3);	 Catch:{ all -> 0x0047 }
        android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x004c }
    L_0x0045:
        monitor-exit(r4);
        return r7;
    L_0x0047:
        r3 = move-exception;
        android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x004c }
        throw r3;	 Catch:{ all -> 0x004c }
    L_0x004c:
        r3 = move-exception;
        monitor-exit(r4);
        throw r3;
    L_0x004f:
        r3 = 1;
        r8.mUsbTetherRequested = r3;	 Catch:{ all -> 0x004c }
        r3 = com.android.server.HwServiceFactory.getHwConnectivityManager();	 Catch:{ all -> 0x004c }
        r5 = r8.mContext;	 Catch:{ all -> 0x004c }
        r6 = 1;	 Catch:{ all -> 0x004c }
        r3 = r3.setUsbFunctionForTethering(r5, r2, r6);	 Catch:{ all -> 0x004c }
        if (r3 != 0) goto L_0x0045;	 Catch:{ all -> 0x004c }
    L_0x005f:
        r3 = "rndis";	 Catch:{ all -> 0x004c }
        r2.setCurrentFunction(r3);	 Catch:{ all -> 0x004c }
        goto L_0x0045;	 Catch:{ all -> 0x004c }
    L_0x0066:
        r0 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x004c }
        r3 = 0;
        r8.tetherUsb(r3);	 Catch:{ all -> 0x0087 }
        android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x004c }
        r3 = r8.mRndisEnabled;	 Catch:{ all -> 0x004c }
        if (r3 == 0) goto L_0x0079;	 Catch:{ all -> 0x004c }
    L_0x0075:
        r3 = 0;	 Catch:{ all -> 0x004c }
        r2.setCurrentFunction(r3);	 Catch:{ all -> 0x004c }
    L_0x0079:
        r3 = com.android.server.HwServiceFactory.getHwConnectivityManager();	 Catch:{ all -> 0x004c }
        r5 = r8.mContext;	 Catch:{ all -> 0x004c }
        r6 = 0;	 Catch:{ all -> 0x004c }
        r3.setUsbFunctionForTethering(r5, r2, r6);	 Catch:{ all -> 0x004c }
        r3 = 0;	 Catch:{ all -> 0x004c }
        r8.mUsbTetherRequested = r3;	 Catch:{ all -> 0x004c }
        goto L_0x0045;	 Catch:{ all -> 0x004c }
    L_0x0087:
        r3 = move-exception;	 Catch:{ all -> 0x004c }
        android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x004c }
        throw r3;	 Catch:{ all -> 0x004c }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.Tethering.setUsbTethering(boolean):int");
    }

    public int[] getUpstreamIfaceTypes() {
        int[] values;
        synchronized (this.mPublicSync) {
            updateConfiguration();
            values = new int[this.mUpstreamIfaceTypes.size()];
            Iterator<Integer> iterator = this.mUpstreamIfaceTypes.iterator();
            for (int i = 0; i < this.mUpstreamIfaceTypes.size(); i++) {
                values[i] = ((Integer) iterator.next()).intValue();
            }
        }
        return values;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkDunRequired() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            int secureSetting = tm.getTetherApnRequired();
        }
        synchronized (this.mPublicSync) {
            int requiredApn;
            if (HwServiceFactory.getHwConnectivityManager().checkDunExisted(this.mContext)) {
                requiredApn = 4;
            } else {
                requiredApn = 5;
            }
            if (requiredApn == 4) {
                while (this.mUpstreamIfaceTypes.contains(MOBILE_TYPE)) {
                    this.mUpstreamIfaceTypes.remove(MOBILE_TYPE);
                }
                while (true) {
                    if (!this.mUpstreamIfaceTypes.contains(HIPRI_TYPE)) {
                        break;
                    }
                    this.mUpstreamIfaceTypes.remove(HIPRI_TYPE);
                }
            } else {
                while (true) {
                    if (!this.mUpstreamIfaceTypes.contains(DUN_TYPE)) {
                        break;
                    }
                    this.mUpstreamIfaceTypes.remove(DUN_TYPE);
                }
            }
            if (this.mUpstreamIfaceTypes.contains(DUN_TYPE)) {
                this.mPreferredUpstreamMobileApn = 4;
            } else {
                this.mPreferredUpstreamMobileApn = 5;
            }
        }
    }

    public String[] getTetheredIfaces() {
        ArrayList<String> list = new ArrayList();
        synchronized (this.mPublicSync) {
            for (Object key : this.mIfaces.keySet()) {
                if (((TetherInterfaceSM) this.mIfaces.get(key)).isTethered()) {
                    list.add((String) key);
                }
            }
        }
        String[] retVal = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            retVal[i] = (String) list.get(i);
        }
        return retVal;
    }

    public String[] getTetherableIfaces() {
        ArrayList<String> list = new ArrayList();
        synchronized (this.mPublicSync) {
            for (Object key : this.mIfaces.keySet()) {
                if (((TetherInterfaceSM) this.mIfaces.get(key)).isAvailable()) {
                    list.add((String) key);
                }
            }
        }
        String[] retVal = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            retVal[i] = (String) list.get(i);
        }
        return retVal;
    }

    public String[] getTetheredDhcpRanges() {
        return this.mDhcpRange;
    }

    public String[] getErroredIfaces() {
        ArrayList<String> list = new ArrayList();
        synchronized (this.mPublicSync) {
            for (Object key : this.mIfaces.keySet()) {
                if (((TetherInterfaceSM) this.mIfaces.get(key)).isErrored()) {
                    list.add((String) key);
                }
            }
        }
        String[] retVal = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            retVal[i] = (String) list.get(i);
        }
        return retVal;
    }

    private void maybeLogMessage(State state, int what) {
        if (DBG) {
            Log.d(TAG, state.getName() + " got " + ((String) sMagicDecoderRing.get(what, Integer.toString(what))));
        }
    }

    private boolean isTetherOpenUpstream() {
        if (5 != this.mPreferredUpstreamMobileApn) {
            return HWFLOW;
        }
        if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
            return SystemProperties.getBoolean("gsm.check_is_single_pdp", HWFLOW);
        }
        return !SystemProperties.getBoolean("gsm.check_is_single_pdp_sub1", HWFLOW) ? SystemProperties.getBoolean("gsm.check_is_single_pdp_sub2", HWFLOW) : true;
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump ConnectivityService.Tether from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("Tethering:");
        pw.increaseIndent();
        pw.print("mUpstreamIfaceTypes:");
        synchronized (this.mPublicSync) {
            for (Integer netType : this.mUpstreamIfaceTypes) {
                pw.print(" " + ConnectivityManager.getNetworkTypeName(netType.intValue()));
            }
            pw.println();
            pw.println("Tether state:");
            pw.increaseIndent();
            for (Object o : this.mIfaces.values()) {
                pw.println(o);
            }
            pw.decreaseIndent();
        }
        pw.decreaseIndent();
    }
}
