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
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkRequest;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.util.SharedLog;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
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
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.ConnectivityService;
import com.android.server.HwConnectivityManager;
import com.android.server.HwServiceFactory;
import com.android.server.NetPluginDelegate;
import com.android.server.connectivity.tethering.IControlsTethering;
import com.android.server.connectivity.tethering.IPv6TetheringCoordinator;
import com.android.server.connectivity.tethering.IPv6TetheringInterfaceServices;
import com.android.server.connectivity.tethering.OffloadController;
import com.android.server.connectivity.tethering.TetherInterfaceStateMachine;
import com.android.server.connectivity.tethering.TetheringConfiguration;
import com.android.server.connectivity.tethering.UpstreamNetworkMonitor;
import com.android.server.job.controllers.JobStatus;
import com.android.server.net.BaseNetworkObserver;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Tethering extends BaseNetworkObserver implements IControlsTethering {
    private static final boolean DBG = HWFLOW;
    protected static final String DISABLE_PROVISIONING_SYSPROP_KEY = "net.tethering.noprovisioning";
    private static final boolean HWDBG;
    protected static final boolean HWFLOW;
    private static boolean HWLOGW_E = true;
    private static final int MAX_SLEEP_RETRY_TIMES = 10;
    private static final int NOTIFICATION_TYPE_BLUETOOTH = 2;
    private static final int NOTIFICATION_TYPE_MULTIPLE = 4;
    private static final int NOTIFICATION_TYPE_NONE = -1;
    private static final int NOTIFICATION_TYPE_P2P = 3;
    private static final int NOTIFICATION_TYPE_USB = 1;
    private static final int NOTIFICATION_TYPE_WIFI = 0;
    private static final String TAG = "Tethering";
    private static final ComponentName TETHER_SERVICE = ComponentName.unflattenFromString(Resources.getSystem().getString(17039818));
    private static final boolean VDBG = HWFLOW;
    private static final int WAIT_SLEEP_TIME = 100;
    private static final Class[] messageClasses = new Class[]{Tethering.class, TetherMasterSM.class, TetherInterfaceStateMachine.class};
    private static final SparseArray<String> sMagicDecoderRing = MessageUtils.findMessageNames(messageClasses);
    private final String PROPERTY_BTHOTSPOT_ON = "sys.isbthotspoton";
    private volatile TetheringConfiguration mConfig;
    private final Context mContext;
    private String mCurrentUpstreamIface;
    private final HashSet<TetherInterfaceStateMachine> mForwardedDownstreams;
    private HwNotificationTethering mHwNotificationTethering;
    private int mLastNotificationId;
    private final SharedLog mLog = new SharedLog(TAG);
    private final Looper mLooper;
    private final INetworkManagementService mNMService;
    private int mNotificationType = -1;
    private final OffloadController mOffloadController;
    private final INetworkPolicyManager mPolicyManager;
    private final Object mPublicSync;
    private boolean mRndisEnabled;
    private final BroadcastReceiver mStateReceiver;
    private final INetworkStatsService mStatsService;
    private final MockableSystemProperties mSystemProperties;
    private final StateMachine mTetherMasterSM;
    private final ArrayMap<String, TetherState> mTetherStates;
    private Builder mTetheredNotificationBuilder;
    private final UpstreamNetworkMonitor mUpstreamNetworkMonitor;
    private boolean mUsbTetherRequested;
    private boolean mWifiTetherRequested;

    private class StateReceiver extends BroadcastReceiver {
        /* synthetic */ StateReceiver(Tethering this$0, StateReceiver -this1) {
            this();
        }

        private StateReceiver() {
        }

        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "StateReceiver onReceive action:" + action);
                }
                if (action.equals("android.hardware.usb.action.USB_STATE")) {
                    handleUsbAction(intent);
                } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    handleConnectivityAction(intent);
                } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                    handleWifiApAction(intent);
                } else if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                    Tethering.this.updateConfiguration();
                }
            }
        }

        private void handleConnectivityAction(Intent intent) {
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (networkInfo != null && networkInfo.getDetailedState() != DetailedState.FAILED) {
                if (Tethering.VDBG) {
                    Log.d(Tethering.TAG, "Tethering got CONNECTIVITY_ACTION: " + networkInfo.toString());
                }
                Tethering.this.mTetherMasterSM.sendMessage(327683);
            }
        }

        private void handleUsbAction(Intent intent) {
            boolean usbConnected = intent.getBooleanExtra("connected", false);
            boolean rndisEnabled = intent.getBooleanExtra("rndis", false);
            synchronized (Tethering.this.mPublicSync) {
                Tethering.this.mRndisEnabled = rndisEnabled;
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "StateReceiver onReceive action synchronized: usbConnected = " + usbConnected + ", mRndisEnabled = " + Tethering.this.mRndisEnabled + ", mUsbTetherRequested = " + Tethering.this.mUsbTetherRequested);
                }
                if (usbConnected && Tethering.this.mRndisEnabled && Tethering.this.mUsbTetherRequested) {
                    Tethering.this.tetherMatchingInterfaces(2, 1);
                }
                Tethering.this.mUsbTetherRequested = false;
            }
        }

        private void handleWifiApAction(Intent intent) {
            int curState = intent.getIntExtra("wifi_state", 11);
            String ifname = intent.getStringExtra("wifi_ap_interface_name");
            int ipmode = intent.getIntExtra("wifi_ap_mode", -1);
            synchronized (Tethering.this.mPublicSync) {
                switch (curState) {
                    case 12:
                        break;
                    case 13:
                        Tethering.this.enableWifiIpServingLocked(ifname, ipmode);
                        break;
                    default:
                        Tethering.this.disableWifiIpServingLocked(ifname, curState);
                        break;
                }
            }
        }
    }

    class TetherMasterSM extends StateMachine {
        private static final int BASE_MASTER = 327680;
        static final int CMD_CLEAR_ERROR = 327686;
        static final int CMD_RETRY_UPSTREAM = 327684;
        static final int CMD_UPSTREAM_CHANGED = 327683;
        static final int EVENT_IFACE_SERVING_STATE_ACTIVE = 327681;
        static final int EVENT_IFACE_SERVING_STATE_INACTIVE = 327682;
        static final int EVENT_UPSTREAM_CALLBACK = 327685;
        private static final int UPSTREAM_SETTLE_TIME_MS = 10000;
        private final IPv6TetheringCoordinator mIPv6TetheringCoordinator;
        private State mInitialState = new InitialState();
        private NetworkCallback mNetworkCallback = null;
        private final ArrayList<TetherInterfaceStateMachine> mNotifyList;
        private State mSetDnsForwardersErrorState = new SetDnsForwardersErrorState();
        private State mSetIpForwardingDisabledErrorState = new SetIpForwardingDisabledErrorState();
        private State mSetIpForwardingEnabledErrorState = new SetIpForwardingEnabledErrorState();
        private State mStartTetheringErrorState = new StartTetheringErrorState();
        private State mStopTetheringErrorState = new StopTetheringErrorState();
        private State mTetherModeAliveState = new TetherModeAliveState();
        boolean prevIPV6Connected = false;

        class ErrorState extends State {
            private int mErrorNotification;

            ErrorState() {
            }

            public boolean processMessage(Message message) {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "ErrorState processMessage what=" + message.what);
                }
                switch (message.what) {
                    case TetherMasterSM.EVENT_IFACE_SERVING_STATE_ACTIVE /*327681*/:
                        message.obj.sendMessage(this.mErrorNotification);
                        return true;
                    case TetherMasterSM.CMD_CLEAR_ERROR /*327686*/:
                        this.mErrorNotification = 0;
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mInitialState);
                        return true;
                    default:
                        return false;
                }
            }

            void notify(int msgType) {
                this.mErrorNotification = msgType;
                for (TetherInterfaceStateMachine sm : TetherMasterSM.this.mNotifyList) {
                    sm.sendMessage(msgType);
                }
            }
        }

        class InitialState extends State {
            InitialState() {
            }

            public boolean processMessage(Message message) {
                Tethering.this.maybeLogMessage(this, message.what);
                TetherInterfaceStateMachine who;
                switch (message.what) {
                    case TetherMasterSM.EVENT_IFACE_SERVING_STATE_ACTIVE /*327681*/:
                        who = message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode requested by " + who);
                        }
                        TetherMasterSM.this.handleInterfaceServingStateActive(message.arg1, who);
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mTetherModeAliveState);
                        break;
                    case TetherMasterSM.EVENT_IFACE_SERVING_STATE_INACTIVE /*327682*/:
                        who = (TetherInterfaceStateMachine) message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode unrequested by " + who);
                        }
                        TetherMasterSM.this.handleInterfaceServingStateInactive(who);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        class SetDnsForwardersErrorState extends ErrorState {
            SetDnsForwardersErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in setDnsForwarders");
                notify(TetherInterfaceStateMachine.CMD_SET_DNS_FORWARDERS_ERROR);
                try {
                    Tethering.this.mNMService.stopTethering();
                } catch (Exception e) {
                }
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(false);
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
                notify(TetherInterfaceStateMachine.CMD_IP_FORWARDING_DISABLE_ERROR);
            }
        }

        class SetIpForwardingEnabledErrorState extends ErrorState {
            SetIpForwardingEnabledErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in setIpForwardingEnabled");
                notify(TetherInterfaceStateMachine.CMD_IP_FORWARDING_ENABLE_ERROR);
            }
        }

        private class SimChangeListener {
            private BroadcastReceiver mBroadcastReceiver;
            private final Context mContext;
            private final AtomicInteger mSimBcastGenerationNumber = new AtomicInteger(0);

            private class SimChangeBroadcastReceiver extends BroadcastReceiver {
                private final int mGenerationNumber;
                private boolean mSimNotLoadedSeen = false;

                public SimChangeBroadcastReceiver(int generationNumber) {
                    this.mGenerationNumber = generationNumber;
                }

                public void onReceive(Context context, Intent intent) {
                    int currentGenerationNumber = SimChangeListener.this.mSimBcastGenerationNumber.get();
                    if (Tethering.DBG) {
                        Log.d(Tethering.TAG, "simchange mGenerationNumber=" + this.mGenerationNumber + ", current generationNumber=" + currentGenerationNumber);
                    }
                    if (this.mGenerationNumber == currentGenerationNumber) {
                        String state = intent.getStringExtra("ss");
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "got Sim changed to state " + state + ", mSimNotLoadedSeen=" + this.mSimNotLoadedSeen);
                        }
                        if (SimChangeListener.this.isSimCardLoaded(state)) {
                            if (SimChangeListener.this.isSimCardLoaded(state) && this.mSimNotLoadedSeen) {
                                this.mSimNotLoadedSeen = false;
                                if (SimChangeListener.this.hasMobileHotspotProvisionApp()) {
                                    ArrayList<Integer> tethered = new ArrayList();
                                    synchronized (Tethering.this.mPublicSync) {
                                        for (int i = 0; i < Tethering.this.mTetherStates.size(); i++) {
                                            if (((TetherState) Tethering.this.mTetherStates.valueAt(i)).lastState == 2) {
                                                int interfaceType = Tethering.this.ifaceNameToType((String) Tethering.this.mTetherStates.keyAt(i));
                                                if (interfaceType != -1) {
                                                    tethered.add(new Integer(interfaceType));
                                                }
                                            }
                                        }
                                    }
                                    for (Integer intValue : tethered) {
                                        SimChangeListener.this.startProvisionIntent(intValue.intValue());
                                    }
                                } else {
                                    return;
                                }
                            }
                            return;
                        }
                        if (!this.mSimNotLoadedSeen) {
                            this.mSimNotLoadedSeen = true;
                        }
                    }
                }
            }

            SimChangeListener(Context ctx) {
                this.mContext = ctx;
            }

            public int generationNumber() {
                return this.mSimBcastGenerationNumber.get();
            }

            public void startListening() {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "startListening for SIM changes");
                }
                if (this.mBroadcastReceiver == null) {
                    this.mBroadcastReceiver = new SimChangeBroadcastReceiver(this.mSimBcastGenerationNumber.incrementAndGet());
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.intent.action.SIM_STATE_CHANGED");
                    this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, Tethering.this.mTetherMasterSM.getHandler());
                }
            }

            public void stopListening() {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "stopListening for SIM changes");
                }
                if (this.mBroadcastReceiver != null) {
                    this.mSimBcastGenerationNumber.incrementAndGet();
                    this.mContext.unregisterReceiver(this.mBroadcastReceiver);
                    this.mBroadcastReceiver = null;
                }
            }

            public boolean hasMobileHotspotProvisionApp() {
                try {
                    if (!this.mContext.getResources().getString(17039801).isEmpty()) {
                        Log.d(Tethering.TAG, "re-evaluate provisioning");
                        return true;
                    }
                } catch (NotFoundException e) {
                }
                Log.d(Tethering.TAG, "no prov-check needed for new SIM");
                return false;
            }

            private boolean isSimCardLoaded(String state) {
                return "LOADED".equals(state);
            }

            private void startProvisionIntent(int tetherType) {
                Intent startProvIntent = new Intent();
                startProvIntent.putExtra("extraAddTetherType", tetherType);
                startProvIntent.putExtra("extraRunProvision", true);
                startProvIntent.setComponent(Tethering.TETHER_SERVICE);
                this.mContext.startServiceAsUser(startProvIntent, UserHandle.CURRENT);
            }
        }

        class StartTetheringErrorState extends ErrorState {
            StartTetheringErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in startTethering");
                notify(TetherInterfaceStateMachine.CMD_START_TETHERING_ERROR);
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(false);
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
                notify(TetherInterfaceStateMachine.CMD_STOP_TETHERING_ERROR);
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {
                }
            }
        }

        class TetherMasterUtilState extends State {
            TetherMasterUtilState() {
            }

            public boolean processMessage(Message m) {
                return false;
            }

            protected void requestUpstreamMobileConnection() {
                Tethering.this.mUpstreamNetworkMonitor.updateMobileRequiresDun(Tethering.this.mConfig.isDunRequired);
                Tethering.this.mUpstreamNetworkMonitor.registerMobileNetworkRequest();
            }

            protected void unrequestUpstreamMobileConnection() {
                Tethering.this.mUpstreamNetworkMonitor.releaseMobileNetworkRequest();
            }

            protected boolean turnOnMasterTetherSettings() {
                TetheringConfiguration cfg = Tethering.this.mConfig;
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(true);
                    try {
                        Tethering.this.mNMService.startTethering(cfg.dhcpRanges);
                        Log.e(Tethering.TAG, "sleep 100ms to wait for dnsmaq start up completely.");
                        Thread.currentThread();
                        Thread.sleep(100);
                    } catch (Exception e) {
                        try {
                            Tethering.this.mNMService.stopTethering();
                            Tethering.this.mNMService.startTethering(cfg.dhcpRanges);
                        } catch (Exception ee) {
                            Tethering.this.mLog.e(ee);
                            TetherMasterSM.this.transitionTo(TetherMasterSM.this.mStartTetheringErrorState);
                            return false;
                        }
                    }
                    Tethering.this.mLog.log("SET master tether settings: ON");
                    return true;
                } catch (Exception e2) {
                    Tethering.this.mLog.e(e2);
                    TetherMasterSM.this.transitionTo(TetherMasterSM.this.mSetIpForwardingEnabledErrorState);
                    return false;
                }
            }

            protected boolean turnOffMasterTetherSettings() {
                try {
                    Tethering.this.mNMService.stopTethering();
                    try {
                        Tethering.this.mNMService.setIpForwardingEnabled(false);
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mInitialState);
                        Tethering.this.mLog.log("SET master tether settings: OFF");
                        return true;
                    } catch (Exception e) {
                        Tethering.this.mLog.e(e);
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mSetIpForwardingDisabledErrorState);
                        return false;
                    }
                } catch (Exception e2) {
                    Tethering.this.mLog.e(e2);
                    TetherMasterSM.this.transitionTo(TetherMasterSM.this.mStopTetheringErrorState);
                    return false;
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
                boolean ret = false;
                if (lp == null) {
                    return false;
                }
                try {
                    for (InetAddress addr : lp.getAddresses()) {
                        if (addr instanceof Inet6Address) {
                            Inet6Address i6addr = (Inet6Address) addr;
                            if (!(i6addr.isAnyLocalAddress() || (i6addr.isLinkLocalAddress() ^ 1) == 0 || (i6addr.isLoopbackAddress() ^ 1) == 0 || (i6addr.isMulticastAddress() ^ 1) == 0)) {
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
                    case 0:
                    case 5:
                        ncType = 12;
                        transportType = 0;
                        break;
                    case 1:
                        ncType = 12;
                        transportType = 1;
                        break;
                    case 2:
                        ncType = 0;
                        transportType = 0;
                        break;
                    case 3:
                        ncType = 1;
                        transportType = 0;
                        break;
                    case 4:
                        ncType = 2;
                        transportType = 0;
                        break;
                    case 7:
                        ncType = 12;
                        transportType = 2;
                        break;
                    case 9:
                        ncType = 12;
                        transportType = 3;
                        break;
                    case 10:
                        ncType = 3;
                        transportType = 0;
                        break;
                    case 11:
                        ncType = 4;
                        transportType = 0;
                        break;
                    case 12:
                        ncType = 5;
                        transportType = 0;
                        break;
                    case 13:
                        ncType = 6;
                        transportType = 1;
                        break;
                    case 14:
                        ncType = 7;
                        transportType = 0;
                        break;
                    case 15:
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
                    boolean currentIPV6Connected = false;
                    String currentUpstreamIface;
                    String lastUpstreamIface = null;

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
                            TetherMasterSM.this.prevIPV6Connected = false;
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
                setUpstreamByType(findPreferredUpstreamType(tryCell));
            }

            protected int findPreferredUpstreamType(boolean tryCell) {
                NetworkInfo info;
                int preferredUpstreamMobileApn;
                ConnectivityManager cm = Tethering.this.getConnectivityManager();
                int upType = -1;
                Tethering.this.updateConfiguration();
                TetheringConfiguration cfg = Tethering.this.mConfig;
                if (Tethering.VDBG) {
                    Log.d(Tethering.TAG, "chooseUpstreamType has upstream iface types:");
                    for (Integer netType : cfg.preferredUpstreamIfaceTypes) {
                        Log.d(Tethering.TAG, " " + netType);
                    }
                }
                for (Integer netType2 : cfg.preferredUpstreamIfaceTypes) {
                    info = cm.getNetworkInfo(netType2.intValue());
                    if (info != null && info.isConnected()) {
                        upType = netType2.intValue();
                        break;
                    }
                }
                if (upType == 0) {
                    info = Tethering.this.getConnectivityManager().getNetworkInfo(5);
                    if (info != null && info.isConnected()) {
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "hipri connected, ignore default mobile upstream");
                        }
                        upType = 5;
                    }
                }
                if (cfg.isDunRequired) {
                    preferredUpstreamMobileApn = 4;
                } else {
                    preferredUpstreamMobileApn = 5;
                }
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "chooseUpstreamType(" + tryCell + ")," + " preferredApn=" + ConnectivityManager.getNetworkTypeName(preferredUpstreamMobileApn) + ", got type=" + ConnectivityManager.getNetworkTypeName(upType));
                }
                switch (upType) {
                    case -1:
                        if (!tryCell) {
                            TetherMasterSM.this.sendMessageDelayed(TetherMasterSM.CMD_RETRY_UPSTREAM, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                            break;
                        }
                        requestUpstreamMobileConnection();
                        break;
                    case 4:
                    case 5:
                        requestUpstreamMobileConnection();
                        break;
                    default:
                        unrequestUpstreamMobileConnection();
                        break;
                }
                return upType;
            }

            protected void setUpstreamByType(int upType) {
                ConnectivityManager cm = Tethering.this.getConnectivityManager();
                Network network = null;
                String iface = null;
                if (upType != -1) {
                    network = Tethering.this.getConnectivityManager().getNetworkForType(upType);
                    if (HuaweiTelephonyConfigs.isQcomPlatform()) {
                        NetPluginDelegate.setUpstream(network);
                    } else {
                        try {
                            NetworkInfo info = Tethering.this.getConnectivityManager().getNetworkInfo(upType);
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
                    LinkProperties linkProperties = cm.getLinkProperties(upType);
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
                        if (iface == null) {
                            Log.d(Tethering.TAG, "Get iface from linkproperties.");
                            iface = linkProperties.getInterfaceName();
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
                NetworkState ns = Tethering.this.mUpstreamNetworkMonitor.lookup(network);
                if (ns != null && Tethering.this.pertainsToCurrentUpstream(ns)) {
                    handleNewUpstreamNetworkState(ns);
                } else if (Tethering.this.mCurrentUpstreamIface == null) {
                    handleNewUpstreamNetworkState(null);
                }
            }

            protected void setDnsForwarders(Network network, LinkProperties lp) {
                if (lp != null) {
                    String[] dnsServers = Tethering.this.mConfig.defaultIPv4DNS;
                    Collection<InetAddress> dnses = lp.getDnsServers();
                    if (!(dnses == null || (dnses.isEmpty() ^ 1) == 0)) {
                        dnsServers = NetworkUtils.makeStrings(dnses);
                    }
                    try {
                        Tethering.this.mNMService.setDnsForwarders(network, dnsServers);
                        Tethering.this.mLog.log(String.format("SET DNS forwarders: network=%s dnsServers=%s", new Object[]{network, Arrays.toString(dnsServers)}));
                    } catch (Exception e) {
                        Tethering.this.mLog.e("setting DNS forwarders failed, " + e);
                        TetherMasterSM.this.transitionTo(TetherMasterSM.this.mSetDnsForwardersErrorState);
                    }
                }
            }

            protected void notifyTetheredOfNewUpstreamIface(String ifaceName) {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "Notifying tethered with upstream=" + ifaceName);
                }
                Tethering.this.mCurrentUpstreamIface = ifaceName;
                for (TetherInterfaceStateMachine sm : TetherMasterSM.this.mNotifyList) {
                    sm.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_CONNECTION_CHANGED, ifaceName);
                }
            }

            protected void handleNewUpstreamNetworkState(NetworkState ns) {
                LinkProperties linkProperties = null;
                TetherMasterSM.this.mIPv6TetheringCoordinator.updateUpstreamNetworkState(ns);
                OffloadController -get10 = Tethering.this.mOffloadController;
                if (ns != null) {
                    linkProperties = ns.linkProperties;
                }
                -get10.setUpstreamLinkProperties(linkProperties);
            }
        }

        class TetherModeAliveState extends TetherMasterUtilState {
            boolean mTryCell = true;
            boolean mUpstreamWanted = false;
            final SimChangeListener simChange = new SimChangeListener(Tethering.this.mContext);

            TetherModeAliveState() {
                super();
            }

            public void enter() {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "TetherModeAliveState enter");
                }
                if (turnOnMasterTetherSettings()) {
                    this.simChange.startListening();
                    Tethering.this.mUpstreamNetworkMonitor.start();
                    Tethering.this.mOffloadController.start();
                    if (Tethering.this.upstreamWanted()) {
                        this.mUpstreamWanted = true;
                        chooseUpstreamType(true);
                        this.mTryCell = false;
                    }
                }
            }

            public void exit() {
                Tethering.this.mOffloadController.stop();
                unrequestUpstreamMobileConnection();
                Tethering.this.mUpstreamNetworkMonitor.stop();
                this.simChange.stopListening();
                notifyTetheredOfNewUpstreamIface(null);
                handleNewUpstreamNetworkState(null);
            }

            private boolean updateUpstreamWanted() {
                boolean previousUpstreamWanted = this.mUpstreamWanted;
                this.mUpstreamWanted = Tethering.this.upstreamWanted();
                return previousUpstreamWanted;
            }

            public boolean processMessage(Message message) {
                Tethering.this.maybeLogMessage(this, message.what);
                TetherInterfaceStateMachine who;
                switch (message.what) {
                    case TetherMasterSM.EVENT_IFACE_SERVING_STATE_ACTIVE /*327681*/:
                        who = message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode requested by " + who);
                        }
                        TetherMasterSM.this.handleInterfaceServingStateActive(message.arg1, who);
                        who.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_CONNECTION_CHANGED, Tethering.this.mCurrentUpstreamIface);
                        if (updateUpstreamWanted() || !this.mUpstreamWanted) {
                            return true;
                        }
                        chooseUpstreamType(true);
                        return true;
                    case TetherMasterSM.EVENT_IFACE_SERVING_STATE_INACTIVE /*327682*/:
                        who = (TetherInterfaceStateMachine) message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode unrequested by " + who);
                        }
                        TetherMasterSM.this.handleInterfaceServingStateInactive(who);
                        if (TetherMasterSM.this.mNotifyList.isEmpty()) {
                            turnOffMasterTetherSettings();
                            return true;
                        }
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "TetherModeAlive still has " + TetherMasterSM.this.mNotifyList.size() + " live requests:");
                            for (TetherInterfaceStateMachine o : TetherMasterSM.this.mNotifyList) {
                                Log.d(Tethering.TAG, "  " + o);
                            }
                        }
                        if (!updateUpstreamWanted() || (this.mUpstreamWanted ^ 1) == 0) {
                            return true;
                        }
                        Tethering.this.mUpstreamNetworkMonitor.releaseMobileNetworkRequest();
                        return true;
                    case TetherMasterSM.CMD_UPSTREAM_CHANGED /*327683*/:
                        updateUpstreamWanted();
                        if (!this.mUpstreamWanted) {
                            return true;
                        }
                        chooseUpstreamType(true);
                        this.mTryCell = false;
                        return true;
                    case TetherMasterSM.CMD_RETRY_UPSTREAM /*327684*/:
                        updateUpstreamWanted();
                        if (!this.mUpstreamWanted) {
                            return true;
                        }
                        chooseUpstreamType(this.mTryCell);
                        this.mTryCell ^= 1;
                        return true;
                    case TetherMasterSM.EVENT_UPSTREAM_CALLBACK /*327685*/:
                        updateUpstreamWanted();
                        if (!this.mUpstreamWanted) {
                            return true;
                        }
                        NetworkState ns = message.obj;
                        if (ns != null && (Tethering.this.pertainsToCurrentUpstream(ns) ^ 1) == 0) {
                            switch (message.arg1) {
                                case 2:
                                    handleNewUpstreamNetworkState(ns);
                                    return true;
                                case 3:
                                    setDnsForwarders(ns.network, ns.linkProperties);
                                    handleNewUpstreamNetworkState(ns);
                                    return true;
                                case 4:
                                    handleNewUpstreamNetworkState(null);
                                    return true;
                                default:
                                    return true;
                            }
                        } else if (Tethering.this.mCurrentUpstreamIface != null) {
                            return true;
                        } else {
                            chooseUpstreamType(false);
                            return true;
                        }
                    default:
                        return false;
                }
            }
        }

        TetherMasterSM(String name, Looper looper) {
            super(name, looper);
            addState(this.mInitialState);
            addState(this.mTetherModeAliveState);
            addState(this.mSetIpForwardingEnabledErrorState);
            addState(this.mSetIpForwardingDisabledErrorState);
            addState(this.mStartTetheringErrorState);
            addState(this.mStopTetheringErrorState);
            addState(this.mSetDnsForwardersErrorState);
            this.mNotifyList = new ArrayList();
            this.mIPv6TetheringCoordinator = new IPv6TetheringCoordinator(this.mNotifyList, Tethering.this.mLog);
            setInitialState(this.mInitialState);
        }

        private void handleInterfaceServingStateActive(int mode, TetherInterfaceStateMachine who) {
            if (this.mNotifyList.indexOf(who) < 0) {
                this.mNotifyList.add(who);
                this.mIPv6TetheringCoordinator.addActiveDownstream(who, mode);
            }
            if (mode == 2) {
                Tethering.this.mForwardedDownstreams.add(who);
            } else {
                Tethering.this.mForwardedDownstreams.remove(who);
            }
            if (who.interfaceType() == 0) {
                WifiManager mgr = Tethering.this.getWifiManager();
                String iface = who.interfaceName();
                switch (mode) {
                    case 2:
                        mgr.updateInterfaceIpState(iface, 1);
                        return;
                    case 3:
                        mgr.updateInterfaceIpState(iface, 2);
                        return;
                    default:
                        Log.wtf(Tethering.TAG, "Unknown active serving mode: " + mode);
                        return;
                }
            }
        }

        private void handleInterfaceServingStateInactive(TetherInterfaceStateMachine who) {
            this.mNotifyList.remove(who);
            this.mIPv6TetheringCoordinator.removeActiveDownstream(who);
            Tethering.this.mForwardedDownstreams.remove(who);
            if (who.interfaceType() == 0 && who.lastError() != 0) {
                Tethering.this.getWifiManager().updateInterfaceIpState(who.interfaceName(), 0);
            }
        }
    }

    private static class TetherState {
        public int lastError = 0;
        public int lastState = 1;
        public final TetherInterfaceStateMachine stateMachine;

        public TetherState(TetherInterfaceStateMachine sm) {
            this.stateMachine = sm;
        }

        public boolean isCurrentlyServing() {
            switch (this.lastState) {
                case 2:
                case 3:
                    return true;
                default:
                    return false;
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
        if (Log.HWLog) {
            isLoggable = true;
        } else if (Log.HWModuleLog) {
            isLoggable = Log.isLoggable(TAG, 3);
        } else {
            isLoggable = false;
        }
        HWDBG = isLoggable;
    }

    public Tethering(Context context, INetworkManagementService nmService, INetworkStatsService statsService, INetworkPolicyManager policyManager, Looper looper, MockableSystemProperties systemProperties) {
        this.mLog.mark("constructed");
        this.mContext = context;
        this.mNMService = nmService;
        this.mStatsService = statsService;
        this.mPolicyManager = policyManager;
        this.mLooper = looper;
        this.mSystemProperties = systemProperties;
        this.mPublicSync = new Object();
        this.mTetherStates = new ArrayMap();
        HwCustTethering mCust = (HwCustTethering) HwCustUtils.createObj(HwCustTethering.class, new Object[]{this.mContext});
        this.mTetherMasterSM = new TetherMasterSM("TetherMaster", this.mLooper);
        this.mTetherMasterSM.start();
        this.mOffloadController = new OffloadController(this.mTetherMasterSM.getHandler(), this.mLog);
        this.mUpstreamNetworkMonitor = new UpstreamNetworkMonitor(this.mContext, this.mTetherMasterSM, 327685, this.mLog);
        this.mForwardedDownstreams = new HashSet();
        this.mStateReceiver = new StateReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiver(this.mStateReceiver, filter, null, this.mTetherMasterSM.getHandler());
        if (mCust != null) {
            mCust.registerBroadcast(this.mPublicSync);
        }
        filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_SHARED");
        filter.addAction("android.intent.action.MEDIA_UNSHARED");
        filter.addDataScheme("file");
        this.mContext.registerReceiver(this.mStateReceiver, filter, null, this.mTetherMasterSM.getHandler());
        updateConfiguration();
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

    private WifiManager getWifiManager() {
        return (WifiManager) this.mContext.getSystemService("wifi");
    }

    private void updateConfiguration() {
        this.mConfig = new TetheringConfiguration(this.mContext);
    }

    public void interfaceStatusChanged(String iface, boolean up) {
        if (VDBG) {
            Log.d(TAG, "interfaceStatusChanged " + iface + ", " + up);
        }
        synchronized (this.mPublicSync) {
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (up) {
                maybeTrackNewInterfaceLocked(iface);
            } else if (ifaceNameToType(iface) == 2) {
                stopTrackingInterfaceLocked(iface);
            } else if (VDBG) {
                Log.d(TAG, "ignore interface down for " + iface);
            }
        }
    }

    public void interfaceLinkStateChanged(String iface, boolean up) {
        interfaceStatusChanged(iface, up);
    }

    private int ifaceNameToType(String iface) {
        TetheringConfiguration cfg = this.mConfig;
        if (cfg.isWifi(iface)) {
            return 0;
        }
        if (cfg.isUsb(iface)) {
            return 1;
        }
        if (cfg.isBluetooth(iface)) {
            return 2;
        }
        if (HwServiceFactory.getHwConnectivityManager().isP2pTether(iface)) {
            return 3;
        }
        return -1;
    }

    public void interfaceAdded(String iface) {
        if (VDBG) {
            Log.d(TAG, "interfaceAdded " + iface);
        }
        synchronized (this.mPublicSync) {
            maybeTrackNewInterfaceLocked(iface);
        }
    }

    public void interfaceRemoved(String iface) {
        if (VDBG) {
            Log.d(TAG, "interfaceRemoved " + iface);
        }
        synchronized (this.mPublicSync) {
            stopTrackingInterfaceLocked(iface);
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
        enableTetheringInternal(type, false, null);
        if (isTetherProvisioningRequired()) {
            cancelTetherProvisioningRechecks(type);
        }
    }

    protected boolean isTetherProvisioningRequired() {
        boolean z = true;
        String[] provisionApp = this.mContext.getResources().getStringArray(17236017);
        if (this.mSystemProperties.getBoolean(DISABLE_PROVISIONING_SYSPROP_KEY, false) || provisionApp == null) {
            return false;
        }
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager != null && configManager.getConfig() != null && !configManager.getConfig().getBoolean("require_entitlement_checks_bool")) {
            return false;
        }
        if (provisionApp.length == 2) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName(provisionApp[0], provisionApp[1]);
            if (!(this.mContext == null || this.mContext.getPackageManager() == null || !this.mContext.getPackageManager().queryIntentActivities(intent, 65536).isEmpty())) {
                Log.e(TAG, "isTetherProvisioningRequired Provisioning app is configured, but not available.");
                return false;
            }
        }
        if (provisionApp.length != 2) {
            z = false;
        }
        return z;
    }

    private void enableTetheringInternal(int type, boolean enable, ResultReceiver receiver) {
        boolean isProvisioningRequired = enable ? isTetherProvisioningRequired() : false;
        int result;
        switch (type) {
            case 0:
                result = setWifiTethering(enable);
                if (isProvisioningRequired && result == 0) {
                    scheduleProvisioningRechecks(type);
                }
                sendTetherResult(receiver, result);
                return;
            case 1:
                result = setUsbTethering(enable);
                if (isProvisioningRequired && result == 0) {
                    scheduleProvisioningRechecks(type);
                }
                sendTetherResult(receiver, result);
                return;
            case 2:
                setBluetoothTethering(enable, receiver);
                return;
            case 3:
                if (VDBG) {
                    Log.d(TAG, "type: " + type + " enable: " + enable);
                }
                if (!enable) {
                    HwServiceFactory.getHwConnectivityManager().stopP2pTether(this.mContext);
                    return;
                }
                return;
            default:
                Log.w(TAG, "Invalid tether type.");
                sendTetherResult(receiver, 1);
                return;
        }
    }

    private void sendTetherResult(ResultReceiver receiver, int result) {
        if (receiver != null) {
            receiver.send(result, null);
        }
    }

    private int setWifiTethering(boolean enable) {
        int rval = 5;
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPublicSync) {
                this.mWifiTetherRequested = enable;
                WifiManager mgr = getWifiManager();
                if ((enable && mgr.startSoftAp(null)) || (!enable && mgr.stopSoftAp())) {
                    rval = 0;
                }
            }
            return rval;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void setBluetoothTethering(final boolean enable, final ResultReceiver receiver) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || (adapter.isEnabled() ^ 1) != 0) {
            Log.w(TAG, "Tried to enable bluetooth tethering with null or disabled adapter. null: " + (adapter == null));
            sendTetherResult(receiver, 2);
            return;
        }
        adapter.getProfileProxy(this.mContext, new ServiceListener() {
            public void onServiceDisconnected(int profile) {
            }

            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                int result;
                ((BluetoothPan) proxy).setBluetoothTethering(enable);
                if (((BluetoothPan) proxy).isTetheringOn() == enable) {
                    result = 0;
                } else {
                    result = 5;
                }
                Log.d(Tethering.TAG, "setBluetoothTethering.ServiceListener.onServiceConnected called, enable : " + enable + ", result : " + result);
                if (result == 0) {
                    SystemProperties.set("sys.isbthotspoton", enable ? "true" : "false");
                }
                Tethering.this.sendTetherResult(receiver, result);
                if (enable && Tethering.this.isTetherProvisioningRequired()) {
                    Tethering.this.scheduleProvisioningRechecks(2);
                }
                adapter.closeProfileProxy(5, proxy);
            }
        }, 5);
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

    private ResultReceiver getProxyReceiver(final int type, final ResultReceiver receiver) {
        ResultReceiver rr = new ResultReceiver(null) {
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == 0) {
                    Tethering.this.enableTetheringInternal(type, true, receiver);
                } else {
                    Tethering.this.sendTetherResult(receiver, resultCode);
                }
            }
        };
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
        return tether(iface, 2);
    }

    private int tether(String iface, int requestedState) {
        if (DBG) {
            Log.d(TAG, "Tethering " + iface);
        }
        synchronized (this.mPublicSync) {
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to Tether an unknown iface: " + iface + ", ignoring");
                return 1;
            } else if (tetherState.lastState != 1) {
                Log.e(TAG, "Tried to Tether an unavailable iface: " + iface + ", ignoring");
                return 4;
            } else {
                tetherState.stateMachine.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_REQUESTED, requestedState);
                return 0;
            }
        }
    }

    public int untether(String iface) {
        if (DBG) {
            Log.d(TAG, "Untethering " + iface);
        }
        synchronized (this.mPublicSync) {
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to Untether an unknown iface :" + iface + ", ignoring");
                return 1;
            } else if (tetherState.isCurrentlyServing()) {
                tetherState.stateMachine.sendMessage(TetherInterfaceStateMachine.CMD_TETHER_UNREQUESTED);
                return 0;
            } else {
                Log.e(TAG, "Tried to untether an inactive iface :" + iface + ", ignoring");
                return 4;
            }
        }
    }

    public void untetherAll() {
        stopTethering(0);
        stopTethering(1);
        stopTethering(2);
    }

    public int getLastTetherError(String iface) {
        synchronized (this.mPublicSync) {
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to getLastTetherError on an unknown iface :" + iface + ", ignoring");
                return 1;
            }
            int i = tetherState.lastError;
            return i;
        }
    }

    private void sendTetherStateChangedBroadcast() {
        int waitRetry = 0;
        while (waitRetry < 10 && getConnectivityManager() == null) {
            Log.e(TAG, "sleep to wait ConnectivityManager init completely:" + waitRetry);
            try {
                Thread.sleep(100);
                waitRetry++;
            } catch (InterruptedException e) {
                Log.e(TAG, "exception happened");
            }
        }
        if (getConnectivityManager() == null) {
            Log.e(TAG, "start ConnectivityManager exception");
        } else if (getConnectivityManager().isTetheringSupported()) {
            ArrayList<String> availableList = new ArrayList();
            ArrayList<String> tetherList = new ArrayList();
            ArrayList<String> localOnlyList = new ArrayList();
            ArrayList<String> erroredList = new ArrayList();
            ArrayList<String> tetheringNumbers = new ArrayList();
            boolean wifiTethered = false;
            boolean usbTethered = false;
            boolean bluetoothTethered = false;
            boolean p2pTethered = false;
            TetheringConfiguration cfg = this.mConfig;
            synchronized (this.mPublicSync) {
                for (int i = 0; i < this.mTetherStates.size(); i++) {
                    TetherState tetherState = (TetherState) this.mTetherStates.valueAt(i);
                    String iface = (String) this.mTetherStates.keyAt(i);
                    if (tetherState.lastError != 0) {
                        erroredList.add(iface);
                    } else if (tetherState.lastState == 1) {
                        availableList.add(iface);
                    } else if (tetherState.lastState == 3) {
                        localOnlyList.add(iface);
                    } else if (tetherState.lastState == 2) {
                        if (cfg.isUsb(iface)) {
                            usbTethered = true;
                            tetheringNumbers.add("usb");
                        } else if (cfg.isWifi(iface)) {
                            wifiTethered = true;
                            tetheringNumbers.add("wifi");
                        } else if (cfg.isBluetooth(iface)) {
                            bluetoothTethered = true;
                            tetheringNumbers.add("bluetooth");
                        } else if (HwServiceFactory.getHwConnectivityManager().isP2pTether(iface)) {
                            p2pTethered = true;
                            tetheringNumbers.add("p2p");
                        }
                        tetherList.add(iface);
                    }
                }
            }
            Intent bcast = new Intent("android.net.conn.TETHER_STATE_CHANGED");
            bcast.addFlags(620756992);
            bcast.putStringArrayListExtra("availableArray", availableList);
            bcast.putStringArrayListExtra("localOnlyArray", localOnlyList);
            bcast.putStringArrayListExtra("tetherArray", tetherList);
            bcast.putStringArrayListExtra("erroredArray", erroredList);
            long broadcastId = SystemClock.elapsedRealtime();
            Log.d(TAG, "sendTetherStateChangedBroadcast: broadcastId= " + broadcastId);
            bcast.putExtra("broadcastId", broadcastId);
            this.mContext.sendStickyBroadcastAsUser(bcast, UserHandle.ALL);
            if (DBG) {
                Log.d(TAG, String.format("sendTetherStateChangedBroadcast %s=[%s] %s=[%s] %s=[%s] %s=[%s]", new Object[]{"avail", TextUtils.join(",", availableList), "local_only", TextUtils.join(",", localOnlyList), "tether", TextUtils.join(",", tetherList), "error", TextUtils.join(",", erroredList)}));
            }
            this.mHwNotificationTethering.setTetheringNumber(tetheringNumbers);
            if (usbTethered || wifiTethered || bluetoothTethered || p2pTethered) {
                this.mNotificationType = this.mHwNotificationTethering.getNotificationType(tetheringNumbers);
                showTetheredNotification(this.mHwNotificationTethering.getNotificationIcon(this.mNotificationType));
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
            PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, this.mHwNotificationTethering.getNotificationIntent(this.mNotificationType), 0, null, UserHandle.CURRENT);
            PendingIntent pIntentCancel = PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.server.connectivity.action.STOP_TETHERING"), 134217728);
            Resources r = Resources.getSystem();
            CharSequence title = this.mHwNotificationTethering.getNotificationTitle(this.mNotificationType);
            CharSequence message = r.getText(33685848);
            CharSequence action_text = this.mHwNotificationTethering.getNotificationActionText(this.mNotificationType);
            if (this.mTetheredNotificationBuilder == null) {
                this.mTetheredNotificationBuilder = new Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS);
                this.mTetheredNotificationBuilder.setWhen(0).setOngoing(false).setVisibility(1).setCategory("status").addAction(new Action(0, action_text, pIntentCancel));
            }
            this.mTetheredNotificationBuilder.setSmallIcon(getNotificationBitampIcon(icon)).setContentTitle(title).setContentText(message).setContentIntent(pi);
            Notification notification = this.mTetheredNotificationBuilder.build();
            notification.icon = icon;
            this.mLastNotificationId = icon;
            this.mHwNotificationTethering.showTetheredNotification(this.mNotificationType, notification, pi);
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

    private void disableWifiIpServingLocked(String ifname, int apState) {
        String str;
        this.mLog.log("Canceling WiFi tethering request - AP_STATE=" + apState);
        this.mWifiTetherRequested = false;
        if (!TextUtils.isEmpty(ifname)) {
            TetherState ts = (TetherState) this.mTetherStates.get(ifname);
            if (ts != null) {
                ts.stateMachine.unwanted();
                return;
            }
        }
        for (int i = 0; i < this.mTetherStates.size(); i++) {
            TetherInterfaceStateMachine tism = ((TetherState) this.mTetherStates.valueAt(i)).stateMachine;
            if (tism.interfaceType() == 0) {
                tism.unwanted();
                return;
            }
        }
        SharedLog sharedLog = this.mLog;
        StringBuilder append = new StringBuilder().append("Error disabling Wi-Fi IP serving; ");
        if (TextUtils.isEmpty(ifname)) {
            str = "no interface name specified";
        } else {
            str = "specified interface: " + ifname;
        }
        sharedLog.log(append.append(str).toString());
    }

    private void enableWifiIpServingLocked(String ifname, int wifiIpMode) {
        int ipServingMode;
        switch (wifiIpMode) {
            case 1:
                ipServingMode = 2;
                break;
            case 2:
                ipServingMode = 3;
                break;
            default:
                this.mLog.e("Cannot enable IP serving in unknown WiFi mode: " + wifiIpMode);
                return;
        }
        if (TextUtils.isEmpty(ifname)) {
            this.mLog.e(String.format("Cannot enable IP serving in mode %s on missing interface name", new Object[]{Integer.valueOf(ipServingMode)}));
        } else {
            maybeTrackNewInterfaceLocked(ifname, 0);
            changeInterfaceState(ifname, ipServingMode);
        }
    }

    private void tetherMatchingInterfaces(int requestedState, int interfaceType) {
        if (VDBG) {
            Log.d(TAG, "tetherMatchingInterfaces(" + requestedState + ", " + interfaceType + ")");
        }
        try {
            String[] ifaces = this.mNMService.listInterfaces();
            String chosenIface = null;
            if (ifaces != null) {
                for (String iface : ifaces) {
                    if (ifaceNameToType(iface) == interfaceType) {
                        chosenIface = iface;
                        break;
                    }
                }
            }
            if (chosenIface == null) {
                Log.e(TAG, "could not find iface of type " + interfaceType);
            } else {
                changeInterfaceState(chosenIface, requestedState);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing Interfaces", e);
        }
    }

    private void changeInterfaceState(String ifname, int requestedState) {
        int result;
        switch (requestedState) {
            case 0:
            case 1:
                result = untether(ifname);
                break;
            case 2:
            case 3:
                result = tether(ifname, requestedState);
                break;
            default:
                Log.wtf(TAG, "Unknown interface state: " + requestedState);
                return;
        }
        if (result != 0) {
            Log.e(TAG, "unable start or stop tethering on iface " + ifname);
        }
    }

    public TetheringConfiguration getTetheringConfiguration() {
        return this.mConfig;
    }

    public boolean hasTetherableConfiguration() {
        TetheringConfiguration cfg = this.mConfig;
        boolean hasDownstreamConfiguration = (cfg.tetherableUsbRegexs.length == 0 && cfg.tetherableWifiRegexs.length == 0) ? cfg.tetherableBluetoothRegexs.length != 0 : true;
        boolean hasUpstreamConfiguration = cfg.preferredUpstreamIfaceTypes.isEmpty() ^ 1;
        if (hasDownstreamConfiguration) {
            return hasUpstreamConfiguration;
        }
        return false;
    }

    public String[] getTetherableUsbRegexs() {
        return copy(this.mConfig.tetherableUsbRegexs);
    }

    public String[] getTetherableWifiRegexs() {
        return copy(this.mConfig.tetherableWifiRegexs);
    }

    public String[] getTetherableBluetoothRegexs() {
        return copy(this.mConfig.tetherableBluetoothRegexs);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.server.connectivity.Tethering.setUsbTethering(boolean):int, dom blocks: [B:11:0x003f, B:26:0x006d]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public int setUsbTethering(boolean r9) {
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
        r4 = android.hardware.usb.UsbManager.class;
        r2 = r3.getSystemService(r4);
        r2 = (android.hardware.usb.UsbManager) r2;
        r4 = r8.mPublicSync;
        monitor-enter(r4);
        if (r9 == 0) goto L_0x0067;
    L_0x0035:
        r3 = r8.mRndisEnabled;	 Catch:{ all -> 0x004c }
        if (r3 == 0) goto L_0x004f;	 Catch:{ all -> 0x004c }
    L_0x0039:
        r0 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x004c }
        r3 = 2;
        r5 = 1;
        r8.tetherMatchingInterfaces(r3, r5);	 Catch:{ all -> 0x0047 }
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
        r5 = 0;	 Catch:{ all -> 0x004c }
        r2.setCurrentFunction(r3, r5);	 Catch:{ all -> 0x004c }
        goto L_0x0045;	 Catch:{ all -> 0x004c }
    L_0x0067:
        r0 = android.os.Binder.clearCallingIdentity();	 Catch:{ all -> 0x004c }
        r3 = 1;
        r5 = 1;
        r8.tetherMatchingInterfaces(r3, r5);	 Catch:{ all -> 0x008a }
        android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x004c }
        r3 = r8.mRndisEnabled;	 Catch:{ all -> 0x004c }
        if (r3 == 0) goto L_0x007c;	 Catch:{ all -> 0x004c }
    L_0x0077:
        r3 = 0;	 Catch:{ all -> 0x004c }
        r5 = 0;	 Catch:{ all -> 0x004c }
        r2.setCurrentFunction(r3, r5);	 Catch:{ all -> 0x004c }
    L_0x007c:
        r3 = com.android.server.HwServiceFactory.getHwConnectivityManager();	 Catch:{ all -> 0x004c }
        r5 = r8.mContext;	 Catch:{ all -> 0x004c }
        r6 = 0;	 Catch:{ all -> 0x004c }
        r3.setUsbFunctionForTethering(r5, r2, r6);	 Catch:{ all -> 0x004c }
        r3 = 0;	 Catch:{ all -> 0x004c }
        r8.mUsbTetherRequested = r3;	 Catch:{ all -> 0x004c }
        goto L_0x0045;	 Catch:{ all -> 0x004c }
    L_0x008a:
        r3 = move-exception;	 Catch:{ all -> 0x004c }
        android.os.Binder.restoreCallingIdentity(r0);	 Catch:{ all -> 0x004c }
        throw r3;	 Catch:{ all -> 0x004c }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.Tethering.setUsbTethering(boolean):int");
    }

    public String[] getTetheredIfaces() {
        ArrayList<String> list = new ArrayList();
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                if (((TetherState) this.mTetherStates.valueAt(i)).lastState == 2) {
                    list.add((String) this.mTetherStates.keyAt(i));
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String[] getTetherableIfaces() {
        ArrayList<String> list = new ArrayList();
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                if (((TetherState) this.mTetherStates.valueAt(i)).lastState == 1) {
                    list.add((String) this.mTetherStates.keyAt(i));
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String[] getTetheredDhcpRanges() {
        return this.mConfig.dhcpRanges;
    }

    public String[] getErroredIfaces() {
        ArrayList<String> list = new ArrayList();
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                if (((TetherState) this.mTetherStates.valueAt(i)).lastError != 0) {
                    list.add((String) this.mTetherStates.keyAt(i));
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    private void maybeLogMessage(State state, int what) {
        if (DBG) {
            Log.d(TAG, state.getName() + " got " + ((String) sMagicDecoderRing.get(what, Integer.toString(what))));
        }
    }

    private boolean upstreamWanted() {
        boolean z = true;
        if (!this.mForwardedDownstreams.isEmpty()) {
            return true;
        }
        synchronized (this.mPublicSync) {
            if (!this.mUsbTetherRequested) {
                z = this.mWifiTetherRequested;
            }
        }
        return z;
    }

    boolean pertainsToCurrentUpstream(NetworkState ns) {
        if (!(ns == null || ns.linkProperties == null || this.mCurrentUpstreamIface == null)) {
            for (String ifname : ns.linkProperties.getAllInterfaceNames()) {
                if (this.mCurrentUpstreamIface.equals(ifname)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTetherOpenUpstream() {
        if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
            return SystemProperties.getBoolean("gsm.check_is_single_pdp", false);
        }
        return !SystemProperties.getBoolean("gsm.check_is_single_pdp_sub1", false) ? SystemProperties.getBoolean("gsm.check_is_single_pdp_sub2", false) : true;
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("Tethering:");
            pw.increaseIndent();
            pw.println("Configuration:");
            pw.increaseIndent();
            this.mConfig.dump(pw);
            pw.decreaseIndent();
            synchronized (this.mPublicSync) {
                pw.println("Tether state:");
                pw.increaseIndent();
                for (int i = 0; i < this.mTetherStates.size(); i++) {
                    TetherState tetherState = (TetherState) this.mTetherStates.valueAt(i);
                    pw.print(((String) this.mTetherStates.keyAt(i)) + " - ");
                    switch (tetherState.lastState) {
                        case 0:
                            pw.print("UnavailableState");
                            break;
                        case 1:
                            pw.print("AvailableState");
                            break;
                        case 2:
                            pw.print("TetheredState");
                            break;
                        case 3:
                            pw.print("LocalHotspotState");
                            break;
                        default:
                            pw.print("UnknownState");
                            break;
                    }
                    pw.println(" - lastError = " + tetherState.lastError);
                }
                pw.println("Upstream wanted: " + upstreamWanted());
                pw.decreaseIndent();
            }
            pw.println("Log:");
            pw.increaseIndent();
            if (argsContain(args, ConnectivityService.SHORT_ARG)) {
                pw.println("<log removed for brevity>");
            } else {
                this.mLog.dump(fd, pw, args);
            }
            pw.decreaseIndent();
            pw.decreaseIndent();
        }
    }

    private static boolean argsContain(String[] args, String target) {
        for (String arg : args) {
            if (arg.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public void notifyInterfaceStateChange(String iface, TetherInterfaceStateMachine who, int state, int error) {
        int which;
        boolean z = true;
        synchronized (this.mPublicSync) {
            TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
            if (tetherState != null && tetherState.stateMachine.equals(who)) {
                tetherState.lastState = state;
                tetherState.lastError = error;
            } else if (DBG) {
                Log.d(TAG, "got notification from stale iface " + iface);
            }
        }
        this.mLog.log(String.format("OBSERVED iface=%s state=%s error=%s", new Object[]{iface, Integer.valueOf(state), Integer.valueOf(error)}));
        try {
            boolean z2;
            INetworkPolicyManager iNetworkPolicyManager = this.mPolicyManager;
            if (state == 2) {
                z2 = true;
            } else {
                z2 = false;
            }
            iNetworkPolicyManager.onTetheringChanged(iface, z2);
        } catch (RemoteException e) {
        }
        if (error == 5) {
            this.mTetherMasterSM.sendMessage(327686, who);
        }
        switch (state) {
            case 0:
            case 1:
                which = 327682;
                HwConnectivityManager hwConnectivityManager = HwServiceFactory.getHwConnectivityManager();
                if (1 != ifaceNameToType(iface)) {
                    z = false;
                }
                hwConnectivityManager.setTetheringProp(this, false, z, iface);
                break;
            case 2:
            case 3:
                which = 327681;
                HwServiceFactory.getHwConnectivityManager().setTetheringProp(this, true, 1 == ifaceNameToType(iface), iface);
                break;
            default:
                Log.wtf(TAG, "Unknown interface state: " + state);
                return;
        }
        this.mTetherMasterSM.sendMessage(which, state, 0, who);
        sendTetherStateChangedBroadcast();
    }

    private void maybeTrackNewInterfaceLocked(String iface) {
        int interfaceType = ifaceNameToType(iface);
        if (interfaceType == -1) {
            this.mLog.log(iface + " is not a tetherable iface, ignoring");
        } else {
            maybeTrackNewInterfaceLocked(iface, interfaceType);
        }
    }

    private void maybeTrackNewInterfaceLocked(String iface, int interfaceType) {
        if (this.mTetherStates.containsKey(iface)) {
            this.mLog.log("active iface (" + iface + ") reported as added, ignoring");
            return;
        }
        this.mLog.log("adding TetheringInterfaceStateMachine for: " + iface);
        TetherState tetherState = new TetherState(new TetherInterfaceStateMachine(iface, this.mLooper, interfaceType, this.mLog, this.mNMService, this.mStatsService, this, new IPv6TetheringInterfaceServices(iface, this.mNMService, this.mLog)));
        this.mTetherStates.put(iface, tetherState);
        tetherState.stateMachine.start();
    }

    private void stopTrackingInterfaceLocked(String iface) {
        TetherState tetherState = (TetherState) this.mTetherStates.get(iface);
        if (tetherState == null) {
            this.mLog.log("attempting to remove unknown iface (" + iface + "), ignoring");
            return;
        }
        tetherState.stateMachine.sendMessage(TetherInterfaceStateMachine.CMD_INTERFACE_DOWN);
        this.mLog.log("removing TetheringInterfaceStateMachine for: " + iface);
        this.mTetherStates.remove(iface);
    }

    private static String[] copy(String[] strarray) {
        return (String[]) Arrays.copyOf(strarray, strarray.length);
    }
}
