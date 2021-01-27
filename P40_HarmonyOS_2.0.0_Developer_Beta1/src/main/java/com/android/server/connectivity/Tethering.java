package com.android.server.connectivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.usb.UsbManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.ITetheringEventCallback;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkState;
import android.net.NetworkUtils;
import android.net.ip.IpServer;
import android.net.util.InterfaceSet;
import android.net.util.PrefixUtils;
import android.net.util.SharedLog;
import android.net.util.VersionedBroadcastListener;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.ConnectivityService;
import com.android.server.HwConnectivityManager;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.connectivity.tethering.EntitlementManager;
import com.android.server.connectivity.tethering.IPv6TetheringCoordinator;
import com.android.server.connectivity.tethering.OffloadController;
import com.android.server.connectivity.tethering.TetheringConfiguration;
import com.android.server.connectivity.tethering.TetheringDependencies;
import com.android.server.connectivity.tethering.TetheringInterfaceUtils;
import com.android.server.connectivity.tethering.UpstreamNetworkMonitor;
import com.android.server.job.controllers.JobStatus;
import com.android.server.net.BaseNetworkObserver;
import com.android.server.pm.DumpState;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class Tethering extends BaseNetworkObserver {
    private static final boolean DBG;
    private static final boolean HWDBG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static boolean HWLOGW_E = true;
    private static final int MAX_SLEEP_RETRY_TIMES = 10;
    private static final boolean MOBILE_NETWORK_SHARING_INTEGRATION = SystemProperties.getBoolean("ro.feature.mobile_network_sharing_integration", true);
    private static final int NOTIFICATION_TYPE_BLUETOOTH = 2;
    private static final int NOTIFICATION_TYPE_MULTIPLE = 4;
    private static final int NOTIFICATION_TYPE_NONE = -1;
    private static final int NOTIFICATION_TYPE_P2P = 3;
    private static final int NOTIFICATION_TYPE_USB = 1;
    private static final int NOTIFICATION_TYPE_WIFI = 0;
    private static final String SETTINGS_USB_TETHER_AVOID_NET = "usb_tether_avoid_net";
    private static final String TAG = "Tethering";
    private static final String TETHERING_GROUP_KEY = "TetheredNotification";
    private static final int USB_TETHER_AVOID_NET = 1;
    private static final int USB_TETHER_DEFAULT = 0;
    private static final boolean VDBG;
    private static final int WAIT_SLEEP_TIME = 100;
    private static final Class[] messageClasses = {Tethering.class, TetherMasterSM.class, IpServer.class};
    private static final SparseArray<String> sMagicDecoderRing = MessageUtils.findMessageNames(messageClasses);
    private final String PROPERTY_BTHOTSPOT_ON = "sys.isbthotspoton";
    private final VersionedBroadcastListener mCarrierConfigChange;
    private volatile TetheringConfiguration mConfig;
    private final Context mContext;
    private InterfaceSet mCurrentUpstreamIfaceSet;
    HwCustTethering mCust = null;
    private final VersionedBroadcastListener mDefaultSubscriptionChange;
    private final TetheringDependencies mDeps;
    private final EntitlementManager mEntitlementMgr;
    private final HashSet<IpServer> mForwardedDownstreams;
    private final Handler mHandler;
    private HwNotificationTethering mHwNotificationTethering;
    private final IHwTetheringEx mHwTetheringEx;
    private int mLastNotificationId;
    private final SharedLog mLog = new SharedLog(TAG);
    private final Looper mLooper;
    private final INetworkManagementService mNMService;
    private int mNotificationType = -1;
    private final OffloadController mOffloadController;
    private boolean mP2pTethered = false;
    private final INetworkPolicyManager mPolicyManager;
    private final Object mPublicSync;
    private boolean mRndisEnabled;
    private final BroadcastReceiver mStateReceiver;
    private final INetworkStatsService mStatsService;
    private final StateMachine mTetherMasterSM;
    private final ArrayMap<String, TetherState> mTetherStates;
    private Network mTetherUpstream;
    private Notification.Builder mTetheredNotificationBuilder;
    private final RemoteCallbackList<ITetheringEventCallback> mTetheringEventCallbacks = new RemoteCallbackList<>();
    private final UpstreamNetworkMonitor mUpstreamNetworkMonitor;
    private boolean mUsbTetherRequested;
    private boolean mWifiTetherRequested;

    static {
        boolean z = HWFLOW;
        DBG = z;
        VDBG = z;
    }

    /* access modifiers changed from: private */
    public static class TetherState {
        public final IpServer ipServer;
        public int lastError = 0;
        public int lastState = 1;

        public TetherState(IpServer ipServer2) {
            this.ipServer = ipServer2;
        }

        public boolean isCurrentlyServing() {
            int i = this.lastState;
            if (i == 2 || i == 3) {
                return true;
            }
            return false;
        }
    }

    public Tethering(Context context, INetworkManagementService nmService, INetworkStatsService statsService, INetworkPolicyManager policyManager, Looper looper, MockableSystemProperties systemProperties, TetheringDependencies deps) {
        this.mLog.mark("constructed");
        this.mContext = context;
        this.mNMService = nmService;
        this.mStatsService = statsService;
        this.mPolicyManager = policyManager;
        this.mLooper = looper;
        this.mDeps = deps;
        this.mPublicSync = new Object();
        this.mTetherStates = new ArrayMap<>();
        this.mCust = (HwCustTethering) HwCustUtils.createObj(HwCustTethering.class, new Object[]{this.mContext});
        this.mTetherMasterSM = new TetherMasterSM("TetherMaster", this.mLooper, deps);
        this.mTetherMasterSM.start();
        this.mHandler = this.mTetherMasterSM.getHandler();
        Handler handler = this.mHandler;
        this.mOffloadController = new OffloadController(handler, this.mDeps.getOffloadHardwareInterface(handler, this.mLog), this.mContext.getContentResolver(), this.mNMService, this.mLog);
        this.mUpstreamNetworkMonitor = deps.getUpstreamNetworkMonitor(this.mContext, this.mTetherMasterSM, this.mLog, 327685);
        this.mForwardedDownstreams = new HashSet<>();
        this.mHwTetheringEx = HwServiceFactory.getHwTetheringEx();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mEntitlementMgr = this.mDeps.getEntitlementManager(this.mContext, this.mTetherMasterSM, this.mLog, 327688, systemProperties);
        this.mEntitlementMgr.setOnUiEntitlementFailedListener(new EntitlementManager.OnUiEntitlementFailedListener() {
            /* class com.android.server.connectivity.$$Lambda$Tethering$3zIHfISJxjng2YhMI1EBDdSKsk */

            @Override // com.android.server.connectivity.tethering.EntitlementManager.OnUiEntitlementFailedListener
            public final void onUiEntitlementFailed(int i) {
                Tethering.this.lambda$new$0$Tethering(i);
            }
        });
        this.mEntitlementMgr.setTetheringConfigurationFetcher(new EntitlementManager.TetheringConfigurationFetcher() {
            /* class com.android.server.connectivity.$$Lambda$Tethering$n3LtFaPEJryBHWNNaGBvLgh7QQk */

            @Override // com.android.server.connectivity.tethering.EntitlementManager.TetheringConfigurationFetcher
            public final TetheringConfiguration fetchTetheringConfiguration() {
                return Tethering.this.lambda$new$1$Tethering();
            }
        });
        this.mCarrierConfigChange = new VersionedBroadcastListener("CarrierConfigChangeListener", this.mContext, this.mHandler, filter, new Consumer() {
            /* class com.android.server.connectivity.$$Lambda$Tethering$a_wqxo60onQxTR27G2Ub5703PoY */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                Tethering.this.lambda$new$2$Tethering((Intent) obj);
            }
        });
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        this.mDefaultSubscriptionChange = new VersionedBroadcastListener("DefaultSubscriptionChangeListener", this.mContext, this.mHandler, filter2, new Consumer() {
            /* class com.android.server.connectivity.$$Lambda$Tethering$Ou4huZtFpEPgpllOn6YJeTgvhHw */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                Tethering.this.lambda$new$3$Tethering((Intent) obj);
            }
        });
        this.mStateReceiver = new StateReceiver();
        updateConfiguration();
        startStateMachineUpdaters(this.mHandler);
    }

    public /* synthetic */ void lambda$new$0$Tethering(int downstream) {
        this.mLog.log("OBSERVED UiEnitlementFailed");
        stopTethering(downstream);
    }

    public /* synthetic */ TetheringConfiguration lambda$new$1$Tethering() {
        maybeDefaultDataSubChanged();
        return this.mConfig;
    }

    public /* synthetic */ void lambda$new$2$Tethering(Intent ignored) {
        this.mLog.log("OBSERVED carrier config change");
        updateConfiguration();
        this.mEntitlementMgr.reevaluateSimCardProvisioning(this.mConfig);
    }

    public /* synthetic */ void lambda$new$3$Tethering(Intent ignored) {
        this.mLog.log("OBSERVED default data subscription change");
        maybeDefaultDataSubChanged();
        if (this.mEntitlementMgr.getCarrierConfig(this.mConfig) != null) {
            this.mEntitlementMgr.reevaluateSimCardProvisioning(this.mConfig);
        } else {
            this.mLog.log("IGNORED reevaluate provisioning due to no carrier config loaded");
        }
    }

    private void startStateMachineUpdaters(Handler handler) {
        this.mCarrierConfigChange.startListening();
        this.mDefaultSubscriptionChange.startListening();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiver(this.mStateReceiver, filter, null, handler);
        HwCustTethering hwCustTethering = this.mCust;
        if (hwCustTethering != null) {
            hwCustTethering.registerBroadcast(this.mPublicSync);
        }
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.MEDIA_SHARED");
        filter2.addAction("android.intent.action.MEDIA_UNSHARED");
        filter2.addDataScheme("file");
        this.mContext.registerReceiver(this.mStateReceiver, filter2, null, handler);
        UserManagerInternal umi = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        if (umi != null) {
            umi.addUserRestrictionsListener(new TetheringUserRestrictionListener(this));
        }
        this.mHwNotificationTethering = HwServiceFactory.getHwNotificationTethering(this.mContext);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.connectivity.Tethering.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("com.android.server.connectivity.action.STOP_TETHERING".equals(intent.getAction())) {
                    Tethering.this.mHwNotificationTethering.stopTethering();
                    Tethering.this.clearTetheredNotification();
                }
            }
        }, new IntentFilter("com.android.server.connectivity.action.STOP_TETHERING"), "com.android.server.connectivity.permission.STOP_TETHERING", null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WifiManager getWifiManager() {
        return (WifiManager) this.mContext.getSystemService("wifi");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConfiguration() {
        updateConfiguration(this.mDeps.getDefaultDataSubscriptionId());
    }

    private void updateConfiguration(int subId) {
        this.mConfig = new TetheringConfiguration(this.mContext, this.mLog, subId);
        this.mUpstreamNetworkMonitor.updateMobileRequiresDun(this.mConfig.isDunRequired);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeDunSettingChanged() {
        if (TetheringConfiguration.checkDunRequired(this.mContext) != this.mConfig.isDunRequired) {
            updateConfiguration();
        }
    }

    private void maybeDefaultDataSubChanged() {
        int subId = this.mDeps.getDefaultDataSubscriptionId();
        if (subId != this.mConfig.subId) {
            updateConfiguration(subId);
        }
    }

    public void interfaceStatusChanged(String iface, boolean up) {
        if (VDBG) {
            Log.d(TAG, "interfaceStatusChanged " + iface + ", " + up);
        }
        synchronized (this.mPublicSync) {
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
        this.mEntitlementMgr.startProvisioningIfNeeded(type, showProvisioningUi);
        enableTetheringInternal(type, true, receiver);
    }

    public void stopTethering(int type) {
        HwNotificationTethering hwNotificationTethering;
        if (type == 0 && (hwNotificationTethering = this.mHwNotificationTethering) != null) {
            hwNotificationTethering.setWifiTetherd(false);
        }
        enableTetheringInternal(type, false, null);
        this.mEntitlementMgr.stopProvisioningIfNeeded(type);
    }

    private void enableTetheringInternal(int type, boolean enable, ResultReceiver receiver) {
        if (type == 0) {
            sendTetherResult(receiver, setWifiTethering(enable));
        } else if (type == 1) {
            sendTetherResult(receiver, setUsbTethering(enable));
        } else if (type == 2) {
            setBluetoothTethering(enable, receiver);
        } else if (type != 3) {
            Log.w(TAG, "Invalid tether type.");
            sendTetherResult(receiver, 1);
        } else {
            if (VDBG) {
                Log.d(TAG, "type: " + type + " enable: " + enable);
            }
            if (!enable) {
                HwServiceFactory.getHwConnectivityManager().stopP2pTether(this.mContext);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendTetherResult(ResultReceiver receiver, int result) {
        if (receiver != null) {
            receiver.send(result, null);
        }
    }

    private int setWifiTethering(boolean enable) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPublicSync) {
                WifiManager mgr = getWifiManager();
                if (mgr == null) {
                    this.mLog.e("setWifiTethering: failed to get WifiManager!");
                    return 2;
                } else if ((!enable || !mgr.startSoftAp(null)) && (enable || !mgr.stopSoftAp())) {
                    Binder.restoreCallingIdentity(ident);
                    return 5;
                } else {
                    this.mWifiTetherRequested = enable;
                    Binder.restoreCallingIdentity(ident);
                    return 0;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void setBluetoothTethering(final boolean enable, final ResultReceiver receiver) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Tried to enable bluetooth tethering with null or disabled adapter. null: ");
            sb.append(adapter == null);
            Log.w(TAG, sb.toString());
            sendTetherResult(receiver, 2);
            return;
        }
        adapter.getProfileProxy(this.mContext, new BluetoothProfile.ServiceListener() {
            /* class com.android.server.connectivity.Tethering.AnonymousClass2 */

            @Override // android.bluetooth.BluetoothProfile.ServiceListener
            public void onServiceDisconnected(int profile) {
            }

            @Override // android.bluetooth.BluetoothProfile.ServiceListener
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
                adapter.closeProfileProxy(5, proxy);
            }
        }, 5);
    }

    public int tether(String iface) {
        return tether(iface, 2);
    }

    private int tether(String iface, int requestedState) {
        if (DBG) {
            Log.d(TAG, "Tethering " + iface);
        }
        synchronized (this.mPublicSync) {
            TetherState tetherState = this.mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to Tether an unknown iface: " + iface + ", ignoring");
                return 1;
            } else if (tetherState.lastState != 1) {
                Log.e(TAG, "Tried to Tether an unavailable iface: " + iface + ", ignoring");
                return 4;
            } else {
                tetherState.ipServer.setIsFixed(HwServiceFactory.getHwConnectivityManager().isApIpv4AddressFixed());
                tetherState.ipServer.sendMessage(IpServer.CMD_TETHER_REQUESTED, requestedState);
                return 0;
            }
        }
    }

    public int untether(String iface) {
        if (DBG) {
            Log.d(TAG, "Untethering " + iface);
        }
        synchronized (this.mPublicSync) {
            TetherState tetherState = this.mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to Untether an unknown iface :" + iface + ", ignoring");
                return 1;
            } else if (!tetherState.isCurrentlyServing()) {
                Log.e(TAG, "Tried to untether an inactive iface :" + iface + ", ignoring");
                return 4;
            } else {
                if (tetherState.ipServer.interfaceType() != 1) {
                    tetherState.ipServer.sendMessage(IpServer.CMD_TETHER_UNREQUESTED);
                } else if (this.mHwTetheringEx.isConflictWithUsbP2p(2)) {
                    tetherState.ipServer.sendMessage(IpServer.CMD_TETHER_UNREQUESTED, 1);
                }
                return 0;
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
            TetherState tetherState = this.mTetherStates.get(iface);
            if (tetherState == null) {
                Log.e(TAG, "Tried to getLastTetherError on an unknown iface :" + iface + ", ignoring");
                return 1;
            }
            return tetherState.lastError;
        }
    }

    private void sendTetherStateChangedBroadcast() {
        boolean bluetoothTethered;
        boolean wifiTethered;
        Throwable th;
        int waitRetry;
        int waitRetry2 = 0;
        while (waitRetry2 < 10 && this.mDeps == null) {
            Log.e(TAG, "sleep to wait ConnectivityManager init completely:" + waitRetry2);
            try {
                Thread.sleep(100);
                waitRetry2++;
            } catch (InterruptedException e) {
                Log.e(TAG, "exception happened");
            }
        }
        TetheringDependencies tetheringDependencies = this.mDeps;
        if (tetheringDependencies == null) {
            Log.e(TAG, "start ConnectivityManager exception");
        } else if (tetheringDependencies.isTetheringSupported()) {
            ArrayList<String> availableList = new ArrayList<>();
            ArrayList<String> tetherList = new ArrayList<>();
            ArrayList<String> localOnlyList = new ArrayList<>();
            ArrayList<String> erroredList = new ArrayList<>();
            ArrayList<String> tetheringNumbers = new ArrayList<>();
            boolean usbTethered = false;
            this.mP2pTethered = false;
            TetheringConfiguration cfg = this.mConfig;
            synchronized (this.mPublicSync) {
                int i = 0;
                bluetoothTethered = false;
                wifiTethered = false;
                while (i < this.mTetherStates.size()) {
                    try {
                        TetherState tetherState = this.mTetherStates.valueAt(i);
                        String iface = this.mTetherStates.keyAt(i);
                        if (tetherState.lastError != 0) {
                            try {
                                erroredList.add(iface);
                                waitRetry = waitRetry2;
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        } else {
                            waitRetry = waitRetry2;
                            if (tetherState.lastState == 1) {
                                try {
                                    availableList.add(iface);
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
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
                                    this.mP2pTethered = true;
                                    tetheringNumbers.add("p2p");
                                }
                                tetherList.add(iface);
                            }
                        }
                        i++;
                        waitRetry2 = waitRetry;
                    } catch (Throwable th4) {
                        th = th4;
                        throw th;
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
                Log.d(TAG, String.format("sendTetherStateChangedBroadcast %s=[%s] %s=[%s] %s=[%s] %s=[%s]", "avail", TextUtils.join(",", availableList), "local_only", TextUtils.join(",", localOnlyList), "tether", TextUtils.join(",", tetherList), "error", TextUtils.join(",", erroredList)));
            }
            this.mHwNotificationTethering.setTetheringNumber(tetheringNumbers);
            if (usbTethered || wifiTethered || bluetoothTethered || this.mP2pTethered) {
                this.mNotificationType = this.mHwNotificationTethering.getNotificationType(tetheringNumbers);
                showTetheredNotification(this.mHwNotificationTethering.getNotificationIcon(this.mNotificationType));
                return;
            }
            clearTetheredNotification();
        }
    }

    private void showTetheredNotification(int id) {
        showTetheredNotification(id, true);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void showTetheredNotification(int id, boolean tetheringOn) {
        int icon;
        if (DBG) {
            Log.d(TAG, "showTetheredNotification icon:" + id);
        }
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (notificationManager != null) {
            if (id == 15) {
                icon = 17303609;
            } else if (id != 16) {
                icon = 17303608;
            } else {
                icon = 17303607;
            }
            int i = this.mLastNotificationId;
            if (i != 0) {
                if (i == icon) {
                    this.mHwNotificationTethering.sendTetherNotification();
                    return;
                } else {
                    notificationManager.cancelAsUser(null, i, UserHandle.ALL);
                    this.mLastNotificationId = 0;
                }
            }
            Intent intent = this.mHwNotificationTethering.getNotificationIntent(this.mNotificationType);
            if (MOBILE_NETWORK_SHARING_INTEGRATION) {
                intent.setFlags(805306368);
            }
            PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT);
            PendingIntent pIntentCancel = PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.server.connectivity.action.STOP_TETHERING"), DumpState.DUMP_HWFEATURES);
            Resources r = Resources.getSystem();
            CharSequence title = this.mHwNotificationTethering.getNotificationTitle(this.mNotificationType);
            CharSequence message = r.getText(33685848);
            CharSequence action_text = this.mHwNotificationTethering.getNotificationActionText(this.mNotificationType);
            if (this.mTetheredNotificationBuilder == null) {
                this.mTetheredNotificationBuilder = new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS);
                this.mTetheredNotificationBuilder.setWhen(0).setOngoing(false).setVisibility(1).setCategory("status").addAction(new Notification.Action(0, action_text, pIntentCancel));
            }
            this.mTetheredNotificationBuilder.setSmallIcon(getNotificationBitampIcon(id)).setGroup(TETHERING_GROUP_KEY).setContentTitle(title).setContentText(message).setContentIntent(pi);
            Notification notification = this.mTetheredNotificationBuilder.build();
            notification.icon = id;
            this.mLastNotificationId = id;
            this.mHwNotificationTethering.showTetheredNotification(this.mNotificationType, notification, pi);
        }
    }

    private Icon getNotificationBitampIcon(int resId) {
        Bitmap.Config config;
        Drawable drawable = this.mContext.getResources().getDrawable(resId);
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Bitmap.Config.ARGB_8888;
        } else {
            config = Bitmap.Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return Icon.createWithBitmap(bitmap);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void clearTetheredNotification() {
        if (DBG) {
            Log.d(TAG, "clearTetheredNotification");
        }
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (notificationManager != null && this.mLastNotificationId != 0) {
            Log.i(TAG, "clearTetheredNotification succ");
            notificationManager.cancelAsUser(null, this.mLastNotificationId, UserHandle.ALL);
            this.mLastNotificationId = 0;
            this.mHwNotificationTethering.clearTetheredNotification();
        }
    }

    private class StateReceiver extends BroadcastReceiver {
        private StateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
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
                    Tethering.this.mLog.log("OBSERVED configuration changed");
                    Tethering.this.updateConfiguration();
                }
            }
        }

        private void handleConnectivityAction(Intent intent) {
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (networkInfo != null && networkInfo.getDetailedState() != NetworkInfo.DetailedState.FAILED) {
                if (Tethering.VDBG) {
                    Log.d(Tethering.TAG, "Tethering got CONNECTIVITY_ACTION: " + networkInfo.toString());
                }
                Tethering.this.mTetherMasterSM.sendMessage(327683);
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:20:0x007d A[Catch:{ all -> 0x005a }] */
        private void handleUsbAction(Intent intent) {
            boolean z = false;
            boolean usbConnected = intent.getBooleanExtra("connected", false);
            boolean usbConfigured = intent.getBooleanExtra("configured", false);
            boolean rndisEnabled = intent.getBooleanExtra("rndis", false);
            Tethering.this.mLog.log(String.format("USB bcast connected:%s configured:%s rndis:%s", Boolean.valueOf(usbConnected), Boolean.valueOf(usbConfigured), Boolean.valueOf(rndisEnabled)));
            synchronized (Tethering.this.mPublicSync) {
                if (!usbConnected) {
                    try {
                        if (Tethering.this.mRndisEnabled) {
                            Tethering.this.tetherMatchingInterfaces(1, 1);
                            Tethering.this.mEntitlementMgr.stopProvisioningIfNeeded(1);
                            Tethering tethering = Tethering.this;
                            if (usbConfigured && rndisEnabled) {
                                z = true;
                            }
                            tethering.mRndisEnabled = z;
                            if (Tethering.DBG) {
                                Log.d(Tethering.TAG, "StateReceiver onReceive action synchronized: usbConnected = " + usbConnected + ", mRndisEnabled = " + Tethering.this.mRndisEnabled + ", mUsbTetherRequested = " + Tethering.this.mUsbTetherRequested);
                            }
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (usbConfigured && rndisEnabled && Tethering.this.mUsbTetherRequested) {
                    Tethering.this.tetherMatchingInterfaces(2, 1);
                }
                Tethering tethering2 = Tethering.this;
                z = true;
                tethering2.mRndisEnabled = z;
                if (Tethering.DBG) {
                }
            }
        }

        private void handleWifiApAction(Intent intent) {
            int curState = intent.getIntExtra("wifi_state", 11);
            String ifname = intent.getStringExtra("wifi_ap_interface_name");
            int ipmode = intent.getIntExtra("wifi_ap_mode", -1);
            synchronized (Tethering.this.mPublicSync) {
                if (curState != 12) {
                    if (curState != 13) {
                        try {
                            Tethering.this.disableWifiIpServingLocked(ifname, curState);
                            Tethering.this.mEntitlementMgr.stopProvisioningIfNeeded(0);
                        } catch (Throwable th) {
                            throw th;
                        }
                    } else {
                        Tethering.this.enableWifiIpServingLocked(ifname, ipmode);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public static class TetheringUserRestrictionListener implements UserManagerInternal.UserRestrictionsListener {
        private final Tethering mWrapper;

        public TetheringUserRestrictionListener(Tethering wrapper) {
            this.mWrapper = wrapper;
        }

        public void onUserRestrictionsChanged(int userId, Bundle newRestrictions, Bundle prevRestrictions) {
            boolean newlyDisallowed = newRestrictions.getBoolean("no_config_tethering");
            boolean isTetheringActiveOnDevice = true;
            if (newlyDisallowed != prevRestrictions.getBoolean("no_config_tethering")) {
                this.mWrapper.clearTetheredNotification();
                if (this.mWrapper.getTetheredIfaces().length == 0) {
                    isTetheringActiveOnDevice = false;
                }
                if (newlyDisallowed && isTetheringActiveOnDevice) {
                    this.mWrapper.showTetheredNotification(17303608, false);
                    this.mWrapper.untetherAll();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableWifiIpServingLocked(String ifname, int apState) {
        TetherState ts;
        this.mLog.log("Canceling WiFi tethering request - AP_STATE=" + apState);
        this.mWifiTetherRequested = false;
        if (TextUtils.isEmpty(ifname) || (ts = this.mTetherStates.get(ifname)) == null) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                IpServer ipServer = this.mTetherStates.valueAt(i).ipServer;
                if (ipServer.interfaceType() == 0) {
                    ipServer.unwanted();
                    return;
                }
            }
            SharedLog sharedLog = this.mLog;
            StringBuilder sb = new StringBuilder();
            sb.append("Error disabling Wi-Fi IP serving; ");
            sb.append(TextUtils.isEmpty(ifname) ? "no interface name specified" : "specified interface: " + ifname);
            sharedLog.log(sb.toString());
            return;
        }
        ts.ipServer.unwanted();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableWifiIpServingLocked(String ifname, int wifiIpMode) {
        int ipServingMode;
        if (wifiIpMode == 1) {
            ipServingMode = 2;
        } else if (wifiIpMode != 2) {
            SharedLog sharedLog = this.mLog;
            sharedLog.e("Cannot enable IP serving in unknown WiFi mode: " + wifiIpMode);
            return;
        } else {
            ipServingMode = 3;
        }
        if (!TextUtils.isEmpty(ifname)) {
            maybeTrackNewInterfaceLocked(ifname, 0);
            changeInterfaceState(ifname, ipServingMode);
            return;
        }
        this.mLog.e(String.format("Cannot enable IP serving in mode %s on missing interface name", Integer.valueOf(ipServingMode)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tetherMatchingInterfaces(int requestedState, int interfaceType) {
        if (VDBG) {
            Log.d(TAG, "tetherMatchingInterfaces(" + requestedState + ", " + interfaceType + ")");
        }
        try {
            String[] ifaces = this.mNMService.listInterfaces();
            String chosenIface = null;
            if (ifaces != null) {
                int length = ifaces.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String iface = ifaces[i];
                    if (ifaceNameToType(iface) == interfaceType) {
                        chosenIface = iface;
                        break;
                    }
                    i++;
                }
            }
            if (chosenIface == null) {
                Log.e(TAG, "could not find iface of type " + interfaceType);
                return;
            }
            changeInterfaceState(chosenIface, requestedState);
        } catch (Exception e) {
            Log.e(TAG, "Error listing Interfaces", e);
        }
    }

    private void changeInterfaceState(String ifname, int requestedState) {
        int result;
        if (requestedState == 0 || requestedState == 1) {
            result = untether(ifname);
        } else if (requestedState == 2 || requestedState == 3) {
            result = tether(ifname, requestedState);
        } else {
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
        return (cfg.tetherableUsbRegexs.length != 0 || cfg.tetherableWifiRegexs.length != 0 || cfg.tetherableBluetoothRegexs.length != 0) && (!cfg.preferredUpstreamIfaceTypes.isEmpty() || cfg.chooseUpstreamAutomatically);
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

    /* JADX INFO: finally extract failed */
    public int setUsbTethering(boolean enable) {
        if (VDBG) {
            Log.d(TAG, "setUsbTethering(" + enable + ")");
        }
        UsbManager usbManager = (UsbManager) this.mContext.getSystemService("usb");
        if (usbManager == null) {
            this.mLog.e("setUsbTethering: failed to get UsbManager!");
            return 2;
        }
        synchronized (this.mPublicSync) {
            if (!enable) {
                long ident = Binder.clearCallingIdentity();
                try {
                    tetherMatchingInterfaces(1, 1);
                    Binder.restoreCallingIdentity(ident);
                    if (this.mRndisEnabled && !this.mHwTetheringEx.isConflictWithUsbP2p(2)) {
                        usbManager.setCurrentFunction(null, false);
                    }
                    HwServiceFactory.getHwConnectivityManager().setUsbFunctionForTethering(this.mContext, usbManager, false);
                    this.mUsbTetherRequested = false;
                    this.mHandler.post(new Runnable() {
                        /* class com.android.server.connectivity.$$Lambda$Tethering$wAnkunVFjAQTSMphQvAdWZ7nn5E */

                        @Override // java.lang.Runnable
                        public final void run() {
                            Tethering.this.lambda$setUsbTethering$4$Tethering();
                        }
                    });
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } else if (this.mRndisEnabled) {
                long ident2 = Binder.clearCallingIdentity();
                try {
                    tetherMatchingInterfaces(2, 1);
                } finally {
                    Binder.restoreCallingIdentity(ident2);
                }
            } else {
                this.mUsbTetherRequested = true;
                if (!HwServiceFactory.getHwConnectivityManager().setUsbFunctionForTethering(this.mContext, usbManager, true)) {
                    usbManager.setCurrentFunction("rndis", false);
                }
            }
        }
        return 0;
    }

    public /* synthetic */ void lambda$setUsbTethering$4$Tethering() {
        Settings.Global.putInt(this.mContext.getContentResolver(), SETTINGS_USB_TETHER_AVOID_NET, 0);
    }

    public String[] getTetheredIfaces() {
        ArrayList<String> list = new ArrayList<>();
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                if (this.mTetherStates.valueAt(i).lastState == 2) {
                    list.add(this.mTetherStates.keyAt(i));
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String[] getTetherableIfaces() {
        ArrayList<String> list = new ArrayList<>();
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                if (this.mTetherStates.valueAt(i).lastState == 1) {
                    list.add(this.mTetherStates.keyAt(i));
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String[] getTetheredDhcpRanges() {
        return this.mConfig.legacyDhcpRanges;
    }

    public String[] getErroredIfaces() {
        ArrayList<String> list = new ArrayList<>();
        synchronized (this.mPublicSync) {
            for (int i = 0; i < this.mTetherStates.size(); i++) {
                if (this.mTetherStates.valueAt(i).lastError != 0) {
                    list.add(this.mTetherStates.keyAt(i));
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logMessage(State state, int what) {
        SharedLog sharedLog = this.mLog;
        sharedLog.log(state.getName() + " got " + sMagicDecoderRing.get(what, Integer.toString(what)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean upstreamWanted() {
        boolean z;
        if (!this.mForwardedDownstreams.isEmpty()) {
            return true;
        }
        synchronized (this.mPublicSync) {
            z = this.mWifiTetherRequested;
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean pertainsToCurrentUpstream(NetworkState ns) {
        if (ns == null || ns.linkProperties == null || this.mCurrentUpstreamIfaceSet == null) {
            return false;
        }
        for (String ifname : ns.linkProperties.getAllInterfaceNames()) {
            if (this.mCurrentUpstreamIfaceSet.ifnames.contains(ifname)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public class TetherMasterSM extends StateMachine {
        private static final int BASE_MASTER = 327680;
        static final int CMD_CLEAR_ERROR = 327686;
        static final int CMD_RETRY_UPSTREAM = 327684;
        static final int CMD_UPSTREAM_CHANGED = 327683;
        static final int EVENT_IFACE_SERVING_STATE_ACTIVE = 327681;
        static final int EVENT_IFACE_SERVING_STATE_INACTIVE = 327682;
        static final int EVENT_IFACE_UPDATE_LINKPROPERTIES = 327687;
        static final int EVENT_UPSTREAM_CALLBACK = 327685;
        static final int EVENT_UPSTREAM_PERMISSION_CHANGED = 327688;
        private static final String PROPERTY_USBTETHERING_ON = "sys.isusbtetheringon";
        private static final String PROPERTY_WIFIHOTSPOT_ON = "sys.iswifihotspoton";
        private static final int UPSTREAM_SETTLE_TIME_MS = 10000;
        private static final int USB_IS_CONNECTED = 1;
        private static final int USB_NOT_CONNECTED = 0;
        private String mCloseGroFaceName = null;
        private final IPv6TetheringCoordinator mIPv6TetheringCoordinator;
        private final State mInitialState = new InitialState();
        private boolean mIsCloseGroFlag = false;
        private final ArrayList<IpServer> mNotifyList;
        private final OffloadWrapper mOffload;
        private final State mSetDnsForwardersErrorState = new SetDnsForwardersErrorState();
        private final State mSetIpForwardingDisabledErrorState = new SetIpForwardingDisabledErrorState();
        private final State mSetIpForwardingEnabledErrorState = new SetIpForwardingEnabledErrorState();
        private final State mStartTetheringErrorState = new StartTetheringErrorState();
        private final State mStopTetheringErrorState = new StopTetheringErrorState();
        private final State mTetherModeAliveState = new TetherModeAliveState();

        TetherMasterSM(String name, Looper looper, TetheringDependencies deps) {
            super(name, looper);
            addState(this.mInitialState);
            addState(this.mTetherModeAliveState);
            addState(this.mSetIpForwardingEnabledErrorState);
            addState(this.mSetIpForwardingDisabledErrorState);
            addState(this.mStartTetheringErrorState);
            addState(this.mStopTetheringErrorState);
            addState(this.mSetDnsForwardersErrorState);
            this.mNotifyList = new ArrayList<>();
            this.mIPv6TetheringCoordinator = deps.getIPv6TetheringCoordinator(this.mNotifyList, Tethering.this.mLog);
            this.mOffload = new OffloadWrapper();
            setInitialState(this.mInitialState);
        }

        class InitialState extends State {
            InitialState() {
            }

            public boolean processMessage(Message message) {
                Tethering.this.logMessage(this, message.what);
                switch (message.what) {
                    case TetherMasterSM.EVENT_IFACE_SERVING_STATE_ACTIVE /* 327681 */:
                        IpServer who = (IpServer) message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode requested by " + who);
                        }
                        TetherMasterSM.this.handleInterfaceServingStateActive(message.arg1, who);
                        TetherMasterSM tetherMasterSM = TetherMasterSM.this;
                        tetherMasterSM.transitionTo(tetherMasterSM.mTetherModeAliveState);
                        TetherMasterSM.this.handleTetherStatusToChangeGro(who.interfaceName());
                        return true;
                    case TetherMasterSM.EVENT_IFACE_SERVING_STATE_INACTIVE /* 327682 */:
                        IpServer who2 = (IpServer) message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode unrequested by " + who2);
                        }
                        TetherMasterSM.this.handleInterfaceServingStateInactive(who2);
                        TetherMasterSM.this.handleTetherStatusToChangeGro(who2.interfaceName());
                        return true;
                    case TetherMasterSM.EVENT_IFACE_UPDATE_LINKPROPERTIES /* 327687 */:
                        return true;
                    default:
                        return false;
                }
            }
        }

        /* access modifiers changed from: protected */
        public boolean turnOnMasterTetherSettings() {
            String[] dhcpRanges;
            TetheringConfiguration cfg = Tethering.this.mConfig;
            try {
                Tethering.this.mNMService.setIpForwardingEnabled(true);
                if (cfg.enableLegacyDhcpServer) {
                    dhcpRanges = cfg.legacyDhcpRanges;
                } else {
                    dhcpRanges = new String[0];
                }
                try {
                    Tethering.this.mNMService.startTethering(dhcpRanges);
                } catch (Exception e) {
                    try {
                        Tethering.this.mNMService.stopTethering();
                        Tethering.this.mNMService.startTethering(dhcpRanges);
                    } catch (Exception ee) {
                        Tethering.this.mLog.e(ee);
                        transitionTo(this.mStartTetheringErrorState);
                        return false;
                    }
                }
                Tethering.this.mLog.log("SET master tether settings: ON");
                return true;
            } catch (Exception e2) {
                Tethering.this.mLog.e(e2);
                transitionTo(this.mSetIpForwardingEnabledErrorState);
                return false;
            }
        }

        /* access modifiers changed from: protected */
        public boolean turnOffMasterTetherSettings() {
            try {
                if (!Tethering.this.mHwTetheringEx.isConflictWithUsbP2p(2)) {
                    Tethering.this.mNMService.stopTethering();
                }
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(false);
                    transitionTo(this.mInitialState);
                    Tethering.this.mLog.log("SET master tether settings: OFF");
                    return true;
                } catch (Exception e) {
                    Tethering.this.mLog.e(e);
                    transitionTo(this.mSetIpForwardingDisabledErrorState);
                    return false;
                }
            } catch (Exception e2) {
                Tethering.this.mLog.e(e2);
                transitionTo(this.mStopTetheringErrorState);
                return false;
            }
        }

        /* access modifiers changed from: protected */
        public void chooseUpstreamType(boolean tryCell) {
            NetworkState ns;
            Tethering.this.maybeDunSettingChanged();
            TetheringConfiguration config = Tethering.this.mConfig;
            if (config.chooseUpstreamAutomatically) {
                ns = Tethering.this.mUpstreamNetworkMonitor.getCurrentPreferredUpstream();
            } else {
                ns = Tethering.this.mUpstreamNetworkMonitor.selectPreferredUpstreamType(config.preferredUpstreamIfaceTypes);
            }
            if (ns == null) {
                if (!tryCell) {
                    sendMessageDelayed(CMD_RETRY_UPSTREAM, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                } else if (!Tethering.this.mP2pTethered) {
                    Tethering.this.mUpstreamNetworkMonitor.registerMobileNetworkRequest();
                }
            }
            setUpstreamNetwork(ns);
            Network newUpstream = ns != null ? ns.network : null;
            if (Tethering.this.mTetherUpstream != newUpstream) {
                Tethering.this.mTetherUpstream = newUpstream;
                Tethering.this.mUpstreamNetworkMonitor.setCurrentUpstream(Tethering.this.mTetherUpstream);
                Tethering tethering = Tethering.this;
                tethering.reportUpstreamChanged(tethering.mTetherUpstream);
            }
        }

        /* access modifiers changed from: protected */
        public void setUpstreamNetwork(NetworkState ns) {
            if (!Tethering.this.mUsbTetherRequested || Settings.Global.getInt(Tethering.this.mContext.getContentResolver(), Tethering.SETTINGS_USB_TETHER_AVOID_NET, 0) != 1) {
                InterfaceSet ifaces = null;
                if (ns != null) {
                    SharedLog sharedLog = Tethering.this.mLog;
                    sharedLog.i("Looking for default routes on: " + ns.linkProperties);
                    ifaces = TetheringInterfaceUtils.getTetheringInterfaces(ns);
                    SharedLog sharedLog2 = Tethering.this.mLog;
                    sharedLog2.i("Found upstream interface(s): " + ifaces);
                }
                if (ifaces != null) {
                    setDnsForwarders(ns.network, ns.linkProperties);
                }
                notifyDownstreamsOfNewUpstreamIface(ifaces);
                if (ns != null && Tethering.this.pertainsToCurrentUpstream(ns)) {
                    handleNewUpstreamNetworkState(ns);
                } else if (Tethering.this.mCurrentUpstreamIfaceSet == null) {
                    handleNewUpstreamNetworkState(null);
                }
            } else {
                log("setUpstreamNetwork, Usb tether avoid net, return.");
            }
        }

        /* access modifiers changed from: protected */
        public void setDnsForwarders(Network network, LinkProperties lp) {
            if (lp != null) {
                String[] dnsServers = Tethering.this.mConfig.defaultIPv4DNS;
                Collection<InetAddress> dnses = lp.getDnsServers();
                if (dnses != null && !dnses.isEmpty()) {
                    dnsServers = NetworkUtils.makeStrings(dnses);
                }
                try {
                    Tethering.this.mNMService.setDnsForwarders(network, dnsServers);
                    Tethering.this.mLog.log(String.format("SET DNS forwarders: network=%s dnsServers=%s", network, Arrays.toString(dnsServers)));
                } catch (Exception e) {
                    SharedLog sharedLog = Tethering.this.mLog;
                    sharedLog.e("setting DNS forwarders failed, " + e);
                    transitionTo(this.mSetDnsForwardersErrorState);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void notifyDownstreamsOfNewUpstreamIface(InterfaceSet ifaces) {
            Tethering.this.mCurrentUpstreamIfaceSet = ifaces;
            Iterator<IpServer> it = this.mNotifyList.iterator();
            while (it.hasNext()) {
                IpServer ipServer = it.next();
                ipServer.sendMessage(IpServer.CMD_TETHER_CONNECTION_CHANGED, ifaces);
                handleTetherStatusToChangeGro(ipServer.interfaceName());
            }
        }

        /* access modifiers changed from: protected */
        public void handleNewUpstreamNetworkState(NetworkState ns) {
            this.mIPv6TetheringCoordinator.updateUpstreamNetworkState(ns);
            this.mOffload.updateUpstreamNetworkState(ns);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleInterfaceServingStateActive(int mode, IpServer who) {
            if (this.mNotifyList.indexOf(who) < 0) {
                this.mNotifyList.add(who);
                this.mIPv6TetheringCoordinator.addActiveDownstream(who, mode);
            }
            if (mode == 2) {
                Tethering.this.mForwardedDownstreams.add(who);
            } else {
                this.mOffload.excludeDownstreamInterface(who.interfaceName());
                Tethering.this.mForwardedDownstreams.remove(who);
            }
            if (who.interfaceType() == 0) {
                WifiManager mgr = Tethering.this.getWifiManager();
                String iface = who.interfaceName();
                if (mode == 2) {
                    mgr.updateInterfaceIpState(iface, 1);
                } else if (mode != 3) {
                    Log.wtf(Tethering.TAG, "Unknown active serving mode: " + mode);
                } else {
                    mgr.updateInterfaceIpState(iface, 2);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleInterfaceServingStateInactive(IpServer who) {
            this.mNotifyList.remove(who);
            this.mIPv6TetheringCoordinator.removeActiveDownstream(who);
            this.mOffload.excludeDownstreamInterface(who.interfaceName());
            Tethering.this.mForwardedDownstreams.remove(who);
            if (who.interfaceType() == 0 && who.lastError() != 0) {
                Tethering.this.getWifiManager().updateInterfaceIpState(who.interfaceName(), 0);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleUpstreamNetworkMonitorCallback(int arg1, Object o) {
            if (arg1 == 10) {
                this.mOffload.sendOffloadExemptPrefixes((Set) o);
                return;
            }
            NetworkState ns = (NetworkState) o;
            if (ns == null || !Tethering.this.pertainsToCurrentUpstream(ns)) {
                if (Tethering.this.mCurrentUpstreamIfaceSet == null) {
                    chooseUpstreamType(false);
                }
            } else if (arg1 == 1) {
                handleNewUpstreamNetworkState(ns);
            } else if (arg1 == 2) {
                chooseUpstreamType(false);
            } else if (arg1 != 3) {
                SharedLog sharedLog = Tethering.this.mLog;
                sharedLog.e("Unknown arg1 value: " + arg1);
            } else {
                handleNewUpstreamNetworkState(null);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void informModemTetherStatusToChangeGro(int enable, String faceName) {
            HwServiceFactory.getHwConnectivityManager().informModemTetherStatusToChangeGRO(enable, faceName);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleTetherStatusToChangeGro(String downIfaceName) {
            boolean isUsbTetherOn = SystemProperties.getBoolean(PROPERTY_USBTETHERING_ON, false);
            boolean isWifiHotspotOn = SystemProperties.getBoolean(PROPERTY_WIFIHOTSPOT_ON, false);
            if (!isUsbTetherOn && !isWifiHotspotOn) {
                String str = this.mCloseGroFaceName;
                if (str != null && this.mIsCloseGroFlag) {
                    informModemTetherStatusToChangeGro(0, str);
                    Log.d(Tethering.TAG, "informModemTetherStatusToChangeGro close GroFaceName = " + this.mCloseGroFaceName);
                    this.mCloseGroFaceName = null;
                    this.mIsCloseGroFlag = false;
                }
            } else if (Tethering.this.mCurrentUpstreamIfaceSet != null) {
                if ("rndis0".equals(downIfaceName) || "wlan0".equals(downIfaceName)) {
                    for (String upIfaceName : Tethering.this.mCurrentUpstreamIfaceSet.ifnames) {
                        boolean isUsbConditionOk = !this.mIsCloseGroFlag && isUsbTetherOn && upIfaceName.contains("v4-rmnet");
                        boolean isWifiConditionOk = !this.mIsCloseGroFlag && isWifiHotspotOn && upIfaceName.contains("v4-rmnet");
                        if (isUsbConditionOk || isWifiConditionOk) {
                            this.mCloseGroFaceName = upIfaceName.substring(upIfaceName.indexOf("-") + 1);
                            informModemTetherStatusToChangeGro(1, this.mCloseGroFaceName);
                            this.mIsCloseGroFlag = true;
                            Log.d(Tethering.TAG, "informModemTetherStatusToChangeGro open GroFaceName = " + this.mCloseGroFaceName);
                        }
                    }
                }
            }
        }

        class TetherModeAliveState extends State {
            boolean mTryCell = true;
            boolean mUpstreamWanted = false;

            TetherModeAliveState() {
            }

            public void enter() {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "TetherModeAliveState enter");
                }
                if (TetherMasterSM.this.turnOnMasterTetherSettings()) {
                    Tethering.this.mUpstreamNetworkMonitor.startObserveAllNetworks();
                    if (Tethering.this.upstreamWanted()) {
                        this.mUpstreamWanted = true;
                        TetherMasterSM.this.mOffload.start();
                        TetherMasterSM.this.chooseUpstreamType(true);
                        this.mTryCell = false;
                    }
                }
            }

            public void exit() {
                TetherMasterSM.this.mOffload.stop();
                Tethering.this.mUpstreamNetworkMonitor.stop();
                TetherMasterSM.this.notifyDownstreamsOfNewUpstreamIface(null);
                TetherMasterSM.this.handleNewUpstreamNetworkState(null);
                if (Tethering.this.mTetherUpstream != null) {
                    Tethering.this.mTetherUpstream = null;
                    Tethering.this.reportUpstreamChanged(null);
                }
                if (TetherMasterSM.this.mCloseGroFaceName != null && TetherMasterSM.this.mIsCloseGroFlag) {
                    Log.d(Tethering.TAG, "informModemTetherStatusToChangeGro exit GroFaceName = " + TetherMasterSM.this.mCloseGroFaceName);
                    TetherMasterSM tetherMasterSM = TetherMasterSM.this;
                    tetherMasterSM.informModemTetherStatusToChangeGro(0, tetherMasterSM.mCloseGroFaceName);
                    TetherMasterSM.this.mCloseGroFaceName = null;
                    TetherMasterSM.this.mIsCloseGroFlag = false;
                }
            }

            private boolean updateUpstreamWanted() {
                boolean previousUpstreamWanted = this.mUpstreamWanted;
                this.mUpstreamWanted = Tethering.this.upstreamWanted();
                boolean z = this.mUpstreamWanted;
                if (z != previousUpstreamWanted) {
                    if (z) {
                        TetherMasterSM.this.mOffload.start();
                    } else {
                        TetherMasterSM.this.mOffload.stop();
                    }
                }
                return previousUpstreamWanted;
            }

            public boolean processMessage(Message message) {
                Tethering.this.logMessage(this, message.what);
                switch (message.what) {
                    case TetherMasterSM.EVENT_IFACE_SERVING_STATE_ACTIVE /* 327681 */:
                        IpServer who = (IpServer) message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode requested by " + who);
                        }
                        TetherMasterSM.this.handleInterfaceServingStateActive(message.arg1, who);
                        who.sendMessage(IpServer.CMD_TETHER_CONNECTION_CHANGED, Tethering.this.mCurrentUpstreamIfaceSet);
                        if (!updateUpstreamWanted() && this.mUpstreamWanted) {
                            TetherMasterSM.this.chooseUpstreamType(true);
                        }
                        TetherMasterSM.this.handleTetherStatusToChangeGro(who.interfaceName());
                        return true;
                    case TetherMasterSM.EVENT_IFACE_SERVING_STATE_INACTIVE /* 327682 */:
                        IpServer who2 = (IpServer) message.obj;
                        if (Tethering.VDBG) {
                            Log.d(Tethering.TAG, "Tether Mode unrequested by " + who2);
                        }
                        TetherMasterSM.this.handleInterfaceServingStateInactive(who2);
                        TetherMasterSM.this.handleTetherStatusToChangeGro(who2.interfaceName());
                        if (TetherMasterSM.this.mNotifyList.isEmpty()) {
                            TetherMasterSM.this.turnOffMasterTetherSettings();
                            return true;
                        }
                        if (Tethering.DBG) {
                            Log.d(Tethering.TAG, "TetherModeAlive still has " + TetherMasterSM.this.mNotifyList.size() + " live requests:");
                            Iterator it = TetherMasterSM.this.mNotifyList.iterator();
                            while (it.hasNext()) {
                                Log.d(Tethering.TAG, "  " + ((IpServer) it.next()));
                            }
                        }
                        if (!updateUpstreamWanted() || this.mUpstreamWanted) {
                            return true;
                        }
                        Tethering.this.mUpstreamNetworkMonitor.releaseMobileNetworkRequest();
                        return true;
                    case TetherMasterSM.CMD_UPSTREAM_CHANGED /* 327683 */:
                    case TetherMasterSM.EVENT_UPSTREAM_PERMISSION_CHANGED /* 327688 */:
                        updateUpstreamWanted();
                        if (!this.mUpstreamWanted) {
                            return true;
                        }
                        long iotDelayPropTimer = SystemProperties.getLong("persist.radio.telecom_apn_delay", 0);
                        Log.d(Tethering.TAG, "CMD_UPSTREAM_CHANGED, iotDelayPropTimer = " + iotDelayPropTimer);
                        if (iotDelayPropTimer > 0) {
                            return true;
                        }
                        TetherMasterSM.this.chooseUpstreamType(true);
                        this.mTryCell = false;
                        return true;
                    case TetherMasterSM.CMD_RETRY_UPSTREAM /* 327684 */:
                        updateUpstreamWanted();
                        if (!this.mUpstreamWanted) {
                            return true;
                        }
                        TetherMasterSM.this.chooseUpstreamType(this.mTryCell);
                        this.mTryCell = !this.mTryCell;
                        return true;
                    case TetherMasterSM.EVENT_UPSTREAM_CALLBACK /* 327685 */:
                        updateUpstreamWanted();
                        if (!this.mUpstreamWanted) {
                            return true;
                        }
                        TetherMasterSM.this.handleUpstreamNetworkMonitorCallback(message.arg1, message.obj);
                        return true;
                    case TetherMasterSM.CMD_CLEAR_ERROR /* 327686 */:
                    default:
                        return false;
                    case TetherMasterSM.EVENT_IFACE_UPDATE_LINKPROPERTIES /* 327687 */:
                        LinkProperties newLp = (LinkProperties) message.obj;
                        if (message.arg1 == 2) {
                            TetherMasterSM.this.mOffload.updateDownstreamLinkProperties(newLp);
                            return true;
                        }
                        TetherMasterSM.this.mOffload.excludeDownstreamInterface(newLp.getInterfaceName());
                        return true;
                }
            }
        }

        class ErrorState extends State {
            private int mErrorNotification;

            ErrorState() {
            }

            public boolean processMessage(Message message) {
                if (Tethering.DBG) {
                    Log.d(Tethering.TAG, "ErrorState processMessage what=" + message.what);
                }
                int i = message.what;
                if (i == TetherMasterSM.EVENT_IFACE_SERVING_STATE_ACTIVE) {
                    ((IpServer) message.obj).sendMessage(this.mErrorNotification);
                    return true;
                } else if (i != TetherMasterSM.CMD_CLEAR_ERROR) {
                    return false;
                } else {
                    this.mErrorNotification = 0;
                    TetherMasterSM tetherMasterSM = TetherMasterSM.this;
                    tetherMasterSM.transitionTo(tetherMasterSM.mInitialState);
                    return true;
                }
            }

            /* access modifiers changed from: package-private */
            public void notify(int msgType) {
                this.mErrorNotification = msgType;
                Iterator it = TetherMasterSM.this.mNotifyList.iterator();
                while (it.hasNext()) {
                    ((IpServer) it.next()).sendMessage(msgType);
                }
            }
        }

        class SetIpForwardingEnabledErrorState extends ErrorState {
            SetIpForwardingEnabledErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in setIpForwardingEnabled");
                notify(IpServer.CMD_IP_FORWARDING_ENABLE_ERROR);
            }
        }

        class SetIpForwardingDisabledErrorState extends ErrorState {
            SetIpForwardingDisabledErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in setIpForwardingDisabled");
                notify(IpServer.CMD_IP_FORWARDING_DISABLE_ERROR);
            }
        }

        class StartTetheringErrorState extends ErrorState {
            StartTetheringErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in startTethering");
                notify(IpServer.CMD_START_TETHERING_ERROR);
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
                notify(IpServer.CMD_STOP_TETHERING_ERROR);
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(false);
                } catch (Exception e) {
                }
            }
        }

        class SetDnsForwardersErrorState extends ErrorState {
            SetDnsForwardersErrorState() {
                super();
            }

            public void enter() {
                Log.e(Tethering.TAG, "Error in setDnsForwarders");
                notify(IpServer.CMD_SET_DNS_FORWARDERS_ERROR);
                try {
                    if (!Tethering.this.mHwTetheringEx.isConflictWithUsbP2p(2)) {
                        Tethering.this.mNMService.stopTethering();
                    }
                } catch (Exception e) {
                }
                try {
                    Tethering.this.mNMService.setIpForwardingEnabled(false);
                } catch (Exception e2) {
                }
            }
        }

        /* access modifiers changed from: package-private */
        public class OffloadWrapper {
            OffloadWrapper() {
            }

            public void start() {
                Tethering.this.mOffloadController.start();
                sendOffloadExemptPrefixes();
            }

            public void stop() {
                Tethering.this.mOffloadController.stop();
            }

            public void updateUpstreamNetworkState(NetworkState ns) {
                Tethering.this.mOffloadController.setUpstreamLinkProperties(ns != null ? ns.linkProperties : null);
            }

            public void updateDownstreamLinkProperties(LinkProperties newLp) {
                sendOffloadExemptPrefixes();
                Tethering.this.mOffloadController.notifyDownstreamLinkProperties(newLp);
            }

            public void excludeDownstreamInterface(String ifname) {
                sendOffloadExemptPrefixes();
                Tethering.this.mOffloadController.removeDownstreamInterface(ifname);
            }

            public void sendOffloadExemptPrefixes() {
                sendOffloadExemptPrefixes(Tethering.this.mUpstreamNetworkMonitor.getLocalPrefixes());
            }

            public void sendOffloadExemptPrefixes(Set<IpPrefix> localPrefixes) {
                PrefixUtils.addNonForwardablePrefixes(localPrefixes);
                localPrefixes.add(PrefixUtils.DEFAULT_WIFI_P2P_PREFIX);
                Iterator it = TetherMasterSM.this.mNotifyList.iterator();
                while (it.hasNext()) {
                    IpServer ipServer = (IpServer) it.next();
                    LinkProperties lp = ipServer.linkProperties();
                    int servingMode = ipServer.servingMode();
                    if (!(servingMode == 0 || servingMode == 1)) {
                        if (servingMode == 2) {
                            for (LinkAddress addr : lp.getAllLinkAddresses()) {
                                InetAddress ip = addr.getAddress();
                                if (!ip.isLinkLocalAddress()) {
                                    localPrefixes.add(PrefixUtils.ipAddressAsPrefix(ip));
                                }
                            }
                        } else if (servingMode == 3) {
                            localPrefixes.addAll(PrefixUtils.localPrefixesFrom(lp));
                        }
                    }
                }
                Tethering.this.mOffloadController.setLocalPrefixes(localPrefixes);
            }
        }
    }

    public void systemReady() {
        this.mUpstreamNetworkMonitor.startTrackDefaultNetwork(this.mDeps.getDefaultNetworkRequest(), this.mEntitlementMgr);
    }

    public void getLatestTetheringEntitlementResult(int type, ResultReceiver receiver, boolean showEntitlementUi) {
        if (receiver != null) {
            this.mEntitlementMgr.getLatestTetheringEntitlementResult(type, receiver, showEntitlementUi);
        }
    }

    public void registerTetheringEventCallback(ITetheringEventCallback callback) {
        this.mHandler.post(new Runnable(callback) {
            /* class com.android.server.connectivity.$$Lambda$Tethering$vPSw1Q25isAQK1A3ASpuKvV7oHY */
            private final /* synthetic */ ITetheringEventCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                Tethering.this.lambda$registerTetheringEventCallback$5$Tethering(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$registerTetheringEventCallback$5$Tethering(ITetheringEventCallback callback) {
        try {
            callback.onUpstreamChanged(this.mTetherUpstream);
        } catch (RemoteException e) {
        }
        this.mTetheringEventCallbacks.register(callback);
    }

    public void unregisterTetheringEventCallback(ITetheringEventCallback callback) {
        this.mHandler.post(new Runnable(callback) {
            /* class com.android.server.connectivity.$$Lambda$Tethering$sTWcXFSHWdzOCgo5LU8_LDs9kDY */
            private final /* synthetic */ ITetheringEventCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                Tethering.this.lambda$unregisterTetheringEventCallback$6$Tethering(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterTetheringEventCallback$6$Tethering(ITetheringEventCallback callback) {
        this.mTetheringEventCallbacks.unregister(callback);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportUpstreamChanged(Network network) {
        int length = this.mTetheringEventCallbacks.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mTetheringEventCallbacks.getBroadcastItem(i).onUpstreamChanged(network);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                this.mTetheringEventCallbacks.finishBroadcast();
                throw th;
            }
        }
        this.mTetheringEventCallbacks.finishBroadcast();
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
            pw.println("Entitlement:");
            pw.increaseIndent();
            this.mEntitlementMgr.dump(pw);
            pw.decreaseIndent();
            synchronized (this.mPublicSync) {
                pw.println("Tether state:");
                pw.increaseIndent();
                for (int i = 0; i < this.mTetherStates.size(); i++) {
                    TetherState tetherState = this.mTetherStates.valueAt(i);
                    pw.print(this.mTetherStates.keyAt(i) + " - ");
                    int i2 = tetherState.lastState;
                    if (i2 == 0) {
                        pw.print("UnavailableState");
                    } else if (i2 == 1) {
                        pw.print("AvailableState");
                    } else if (i2 == 2) {
                        pw.print("TetheredState");
                    } else if (i2 != 3) {
                        pw.print("UnknownState");
                    } else {
                        pw.print("LocalHotspotState");
                    }
                    pw.println(" - lastError = " + tetherState.lastError);
                }
                pw.println("Upstream wanted: " + upstreamWanted());
                pw.println("Current upstream interface(s): " + this.mCurrentUpstreamIfaceSet);
                pw.decreaseIndent();
            }
            pw.println("Hardware offload:");
            pw.increaseIndent();
            this.mOffloadController.dump(pw);
            pw.decreaseIndent();
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
            if (target.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private IpServer.Callback makeControlCallback() {
        return new IpServer.Callback() {
            /* class com.android.server.connectivity.Tethering.AnonymousClass3 */

            @Override // android.net.ip.IpServer.Callback
            public void updateInterfaceState(IpServer who, int state, int lastError) {
                Tethering.this.notifyInterfaceStateChange(who, state, lastError);
            }

            @Override // android.net.ip.IpServer.Callback
            public void updateLinkProperties(IpServer who, LinkProperties newLp) {
                Tethering.this.notifyLinkPropertiesChanged(who, newLp);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyInterfaceStateChange(IpServer who, int state, int error) {
        int which;
        String iface = who.interfaceName();
        synchronized (this.mPublicSync) {
            TetherState tetherState = this.mTetherStates.get(iface);
            if (tetherState != null && tetherState.ipServer.equals(who)) {
                tetherState.lastState = state;
                tetherState.lastError = error;
            } else if (DBG) {
                Log.d(TAG, "got notification from stale iface " + iface);
            }
        }
        boolean z = true;
        this.mLog.log(String.format("OBSERVED iface=%s state=%s error=%s", iface, Integer.valueOf(state), Integer.valueOf(error)));
        try {
            this.mPolicyManager.onTetheringChanged(iface, state == 2);
        } catch (RemoteException e) {
        }
        if (error == 5) {
            this.mTetherMasterSM.sendMessage(327686, who);
        }
        if (state == 0 || state == 1) {
            which = 327682;
            HwConnectivityManager hwConnectivityManager = HwServiceFactory.getHwConnectivityManager();
            if (1 != ifaceNameToType(iface)) {
                z = false;
            }
            hwConnectivityManager.setTetheringProp(this, false, z, iface);
            Log.d(TAG, "setApIpv4AddressFixed false");
            HwServiceFactory.getHwConnectivityManager().setApIpv4AddressFixed(false);
        } else if (state == 2 || state == 3) {
            which = 327681;
            HwServiceFactory.getHwConnectivityManager().setTetheringProp(this, true, 1 == ifaceNameToType(iface), iface);
        } else {
            Log.wtf(TAG, "Unknown interface state: " + state);
            return;
        }
        this.mTetherMasterSM.sendMessage(which, state, 0, who);
        sendTetherStateChangedBroadcast();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyLinkPropertiesChanged(IpServer who, LinkProperties newLp) {
        String iface = who.interfaceName();
        synchronized (this.mPublicSync) {
            TetherState tetherState = this.mTetherStates.get(iface);
            if (tetherState == null || !tetherState.ipServer.equals(who)) {
                SharedLog sharedLog = this.mLog;
                sharedLog.log("got notification from stale iface " + iface);
                return;
            }
            int state = tetherState.lastState;
            this.mLog.log(String.format("OBSERVED LinkProperties update iface=%s state=%s lp=%s", iface, IpServer.getStateString(state), newLp));
            this.mTetherMasterSM.sendMessage(327687, state, 0, newLp);
        }
    }

    private void maybeTrackNewInterfaceLocked(String iface) {
        int interfaceType = ifaceNameToType(iface);
        if (interfaceType == -1) {
            SharedLog sharedLog = this.mLog;
            sharedLog.log(iface + " is not a tetherable iface, ignoring");
            return;
        }
        maybeTrackNewInterfaceLocked(iface, interfaceType);
    }

    private void maybeTrackNewInterfaceLocked(String iface, int interfaceType) {
        if (this.mTetherStates.containsKey(iface)) {
            SharedLog sharedLog = this.mLog;
            sharedLog.log("active iface (" + iface + ") reported as added, ignoring");
            return;
        }
        SharedLog sharedLog2 = this.mLog;
        sharedLog2.log("adding TetheringInterfaceStateMachine for: " + iface);
        TetherState tetherState = new TetherState(new IpServer(iface, this.mLooper, interfaceType, this.mLog, this.mNMService, this.mStatsService, makeControlCallback(), this.mConfig.enableLegacyDhcpServer, this.mDeps.getIpServerDependencies()));
        this.mTetherStates.put(iface, tetherState);
        tetherState.ipServer.start();
    }

    private void stopTrackingInterfaceLocked(String iface) {
        TetherState tetherState = this.mTetherStates.get(iface);
        if (tetherState == null) {
            SharedLog sharedLog = this.mLog;
            sharedLog.log("attempting to remove unknown iface (" + iface + "), ignoring");
            return;
        }
        tetherState.ipServer.stop();
        SharedLog sharedLog2 = this.mLog;
        sharedLog2.log("removing TetheringInterfaceStateMachine for: " + iface);
        this.mTetherStates.remove(iface);
    }

    private static String[] copy(String[] strarray) {
        return (String[]) Arrays.copyOf(strarray, strarray.length);
    }
}
