package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.CaptivePortal;
import android.net.ConnectivityManager;
import android.net.ICaptivePortal;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Type;
import android.net.NetworkSpecifier;
import android.net.NetworkStatsHistory;
import android.net.NetworkStatsHistory.Entry;
import android.net.NetworkTemplate;
import android.net.StringNetworkSpecifier;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPhoneConstants;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.server.AbstractConnectivityService.DomainPreferType;
import com.android.server.ConnectivityService.NetworkFactoryInfo;
import com.android.server.ConnectivityService.NetworkRequestInfo;
import com.android.server.GcmFixer.HeartbeatReceiver;
import com.android.server.GcmFixer.NetworkStateReceiver;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.Vpn;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.hisi.mapcon.IMapconService;
import com.hisi.mapcon.IMapconService.Stub;
import com.huawei.deliver.info.HwDeliverInfo;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.HwFeatureConfig;
import huawei.android.telephony.wrapper.WrapperFactory;
import huawei.cust.HwCustUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HwConnectivityService extends ConnectivityService {
    private static final /* synthetic */ int[] -android-net-NetworkInfo$StateSwitchesValues = null;
    private static final String ACTION_BT_CONNECTION_CHANGED = "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_MAPCON_SERVICE_FAILED = "com.hisi.mapcon.servicefailed";
    public static final String ACTION_MAPCON_SERVICE_START = "com.hisi.mapcon.serviceStartResult";
    private static final String ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY = "com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY";
    private static final String ACTION_OF_ANDROID_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_SIM_RECORDS_READY = "com.huawei.intent.action.ACTION_SIM_RECORDS_READY";
    private static final int CONNECTIVITY_SERVICE_NEED_SET_USER_DATA = 1100;
    private static final String COUNTRY_CODE_CN = "460";
    public static final int DEFAULT_PHONE_ID = 0;
    private static final String DEFAULT_SERVER = "connectivitycheck.android.com";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36";
    private static final int DEVICE_NOT_PROVISIONED = 0;
    private static final int DEVICE_PROVISIONED = 1;
    public static final String DISABEL_DATA_SERVICE_ACTION = "android.net.conn.DISABEL_DATA_SERVICE_ACTION";
    private static final String DISABLE_PORTAL_CHECK = "disable_portal_check";
    private static String ENABLE_NOT_REMIND_FUNCTION = "enable_not_remind_function";
    public static final String FLAG_SETUP_WIZARD = "flag_setup_wizard";
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    public static final String MAPCON_START_INTENT = "com.hisi.mmsut.started";
    private static final String MDM_VPN_PERMISSION = "com.huawei.permission.sec.MDM_VPN";
    private static final String MODULE_POWERSAVING = "powersaving";
    private static final String MODULE_WIFI = "wifi";
    private static final int PREFER_NETWORK_TIMEOUT_INTERVAL = 10000;
    public static final int SERVICE_STATE_MMS = 1;
    public static final int SERVICE_STATE_OFF = 0;
    public static final int SERVICE_STATE_ON = 1;
    public static final int SERVICE_TYPE_MMS = 0;
    public static final int SERVICE_TYPE_OTHERS = 2;
    private static final String SYSTEM_MANAGER_PKG_NAME = "com.huawei.systemmanager";
    private static final String TAG = "HwConnectivityService";
    private static final int USER_SETUP_COMPLETE = 1;
    private static final int USER_SETUP_NOT_COMPLETE = 0;
    private static String VALUE_DISABLE_NOT_REMIND_FUNCTION = StorageUtils.SDCARD_RWMOUNTED_STATE;
    private static String VALUE_ENABLE_NOT_REMIND_FUNCTION = StorageUtils.SDCARD_ROMOUNTED_STATE;
    private static int VALUE_NOT_SHOW_PDP = 0;
    private static int VALUE_SHOW_PDP = 1;
    private static final String VALUE_SIM_CHANGE_ALERT_DATA_CONNECT = "0";
    private static String WHETHER_SHOW_PDP_WARNING = "whether_show_pdp_warning";
    private static final String WIFI_AP_MANUAL_CONNECT = "wifi_ap_manual_connect";
    public static final int WIFI_PULS_CSP_DISENABLED = 1;
    public static final int WIFI_PULS_CSP_ENABLED = 0;
    private static ConnectivityServiceUtils connectivityServiceUtils = ((ConnectivityServiceUtils) EasyInvokeFactory.getInvokeUtils(ConnectivityServiceUtils.class));
    private static final String descriptor = "android.net.IConnectivityManager";
    protected static final boolean isAlwaysAllowMMS = SystemProperties.getBoolean("ro.config.hw_always_allow_mms", false);
    private static final boolean isMapconOn = SystemProperties.getBoolean("ro.config.hw_vowifi", false);
    private static final boolean isWifiMmsUtOn = SystemProperties.getBoolean("ro.config.hw_vowifi_mmsut", false);
    private static int mLteMobileDataState = 3;
    private static INetworkStatsService mStatsService;
    private int curMmsDataSub = -1;
    private int curPrefDataSubscription = -1;
    private boolean isAlreadyPop = false;
    private ActivityManager mActivityManager;
    private Context mContext;
    private HwCustConnectivityService mCust = ((HwCustConnectivityService) HwCustUtils.createObj(HwCustConnectivityService.class, new Object[0]));
    private AlertDialog mDataServiceToPdpDialog = null;
    private DomainPreferHandler mDomainPreferHandler = null;
    private NetworkStateReceiver mGcmFixIntentReceiver = new NetworkStateReceiver();
    private Handler mHandler;
    private HeartbeatReceiver mHeartbeatReceiver = new HeartbeatReceiver();
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwConnectivityService.log("mIntentReceiver begin");
            String action = intent.getAction();
            if (HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED.equals(action)) {
                HwConnectivityService.log("receive Intent.ACTION_BOOT_COMPLETED!");
                HwConnectivityService.this.sendWifiBroadcastAfterBootCompleted = true;
            } else if (HwConnectivityService.ACTION_BT_CONNECTION_CHANGED.equals(action)) {
                HwConnectivityService.log("receive ACTION_BT_CONNECTION_CHANGED");
                if (intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == 0) {
                    HwConnectivityService.this.mIsBlueThConnected = false;
                }
            } else {
                HwConnectivityService.this.mRegisteredPushPkg.updateStatus(intent);
            }
        }
    };
    private boolean mIsBlueThConnected = false;
    private boolean mIsSimReady = false;
    private boolean mIsSimStateChanged = false;
    private boolean mIsWifiConnected = false;
    protected BroadcastReceiver mMapconIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent mapconIntent) {
            if (mapconIntent == null) {
                HwConnectivityService.log("intent is null");
                return;
            }
            String action = mapconIntent.getAction();
            HwConnectivityService.log("onReceive: action=" + action);
            if (HwConnectivityService.MAPCON_START_INTENT.equals(action)) {
                ServiceConnection mConnection = new ServiceConnection() {
                    public void onServiceConnected(ComponentName className, IBinder service) {
                        HwConnectivityService.this.mMapconService = Stub.asInterface(service);
                    }

                    public void onServiceDisconnected(ComponentName className) {
                        HwConnectivityService.this.mMapconService = null;
                    }
                };
                HwConnectivityService.this.mContext.bindServiceAsUser(new Intent().setClassName("com.hisi.mapcon", "com.hisi.mapcon.MapconService"), mConnection, 1, UserHandle.OWNER);
            } else if (HwConnectivityService.ACTION_MAPCON_SERVICE_FAILED.equals(action) && mapconIntent.getIntExtra("serviceType", 2) == 0) {
                int requestId = mapconIntent.getIntExtra("request-id", -1);
                HwConnectivityService.loge("Recive ACTION_MAPCON_SERVICE_FAILED, requestId = " + requestId);
                if (requestId > 0) {
                    HwConnectivityService.this.mDomainPreferHandler.sendMessageAtFrontOfQueue(HwConnectivityService.this.mDomainPreferHandler.obtainMessage(1, requestId, 0));
                }
            }
        }
    };
    protected IMapconService mMapconService;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            HwConnectivityService.this.updateCallState(state);
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state != null && !TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect) && !HwConnectivityService.this.isAlreadyPop) {
                boolean isConnect;
                Log.d(HwConnectivityService.TAG, "onServiceStateChanged:" + state);
                switch (state.getVoiceRegState()) {
                    case 1:
                    case 2:
                        if (state.getDataRegState() != 0) {
                            isConnect = false;
                            break;
                        } else {
                            isConnect = true;
                            break;
                        }
                    case 3:
                        isConnect = false;
                        break;
                    default:
                        isConnect = true;
                        break;
                }
                if (state.getRoaming()) {
                    HwTelephonyManagerInner.getDefault().setDataRoamingEnabledWithoutPromp(false);
                    HwConnectivityService.this.mShowWarningRoamingToPdp = true;
                }
                if (isConnect && HwConnectivityService.this.isSetupWizardCompleted() && HwConnectivityService.this.mIsSimReady) {
                    HwConnectivityService.this.mHandler.sendEmptyMessage(0);
                    HwConnectivityService.this.isAlreadyPop = true;
                }
            }
        }
    };
    private RegisteredPushPkg mRegisteredPushPkg = new RegisteredPushPkg();
    private boolean mRemindService = SystemProperties.getBoolean("ro.config.DataPopFirstBoot", false);
    private String mServer;
    private boolean mShowDlgEndCall = false;
    private boolean mShowDlgTurnOfDC = true;
    private boolean mShowWarningRoamingToPdp = false;
    private String mSimChangeAlertDataConnect = null;
    private BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                Log.d(HwConnectivityService.TAG, "CtrlSocket receive ACTION_SIM_STATE_CHANGED");
                HwConnectivityService.this.mIsSimStateChanged = true;
                if (!TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect)) {
                    HwConnectivityService.this.processWhenSimStateChange(intent);
                }
            }
        }
    };
    private BroadcastReceiver mTetheringReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && "android.hardware.usb.action.USB_STATE".equals(action)) {
                boolean usbConnected = intent.getBooleanExtra("connected", false);
                boolean rndisEnabled = intent.getBooleanExtra("rndis", false);
                int is_usb_tethering_on = Secure.getInt(HwConnectivityService.this.mContext.getContentResolver(), "usb_tethering_on", 0);
                Log.d(HwConnectivityService.TAG, "mTetheringReceiver usbConnected = " + usbConnected + ",rndisEnabled = " + rndisEnabled + ", is_usb_tethering_on = " + is_usb_tethering_on);
                if (1 == is_usb_tethering_on && usbConnected && (rndisEnabled ^ 1) != 0) {
                    new Thread() {
                        public void run() {
                            do {
                                try {
                                    if (HwConnectivityService.this.isSystemBootComplete()) {
                                        Thread.sleep(200);
                                    } else {
                                        Thread.sleep(1000);
                                    }
                                } catch (InterruptedException e) {
                                    Log.e(HwConnectivityService.TAG, "wait to boot complete error");
                                }
                            } while (!HwConnectivityService.this.isSystemBootComplete());
                            HwConnectivityService.this.setUsbTethering(true);
                        }
                    }.start();
                }
            }
        }
    };
    private URL mURL;
    private int phoneId = -1;
    private boolean sendWifiBroadcastAfterBootCompleted = false;
    private WifiDisconnectManager wifiDisconnectManager = new WifiDisconnectManager(this, null);

    private static class CtrlSocketInfo {
        public int mAllowCtrlSocketLevel;
        public List<String> mPushWhiteListPkg;
        public int mRegisteredCount;
        public List<String> mRegisteredPkg;
        public List<String> mScrOffActPkg;

        CtrlSocketInfo() {
            this.mRegisteredPkg = null;
            this.mScrOffActPkg = null;
            this.mPushWhiteListPkg = null;
            this.mAllowCtrlSocketLevel = 0;
            this.mRegisteredCount = 0;
            this.mRegisteredPkg = new ArrayList();
            this.mScrOffActPkg = new ArrayList();
            this.mPushWhiteListPkg = new ArrayList();
        }
    }

    private class DomainPreferHandler extends Handler {
        private static final int MSG_PREFER_NETWORK_FAIL = 1;
        private static final int MSG_PREFER_NETWORK_SUCCESS = 0;
        private static final int MSG_PREFER_NETWORK_TIMEOUT = 2;

        public DomainPreferHandler(Looper looper) {
            super(looper);
        }

        private String getMsgName(int whatMsg) {
            switch (whatMsg) {
                case 0:
                    return "PREFER_NETWORK_SUCCESS";
                case 1:
                    return "PREFER_NETWORK_FAIL";
                case 2:
                    return "PREFER_NETWORK_TIMEOUT";
                default:
                    return Integer.toString(whatMsg);
            }
        }

        public void handleMessage(Message msg) {
            HwConnectivityService.log("DomainPreferHandler handleMessage msg.what = " + getMsgName(msg.what));
            switch (msg.what) {
                case 0:
                    handlePreferNetworkSuccess(msg);
                    return;
                case 1:
                    handlePreferNetworkFail(msg);
                    return;
                case 2:
                    handlePreferNetworkTimeout(msg);
                    return;
                default:
                    return;
            }
        }

        private void handlePreferNetworkSuccess(Message msg) {
            NetworkRequest req = msg.obj;
            HwConnectivityService.log("handlePreferNetworkSuccess request = " + req);
            if (HwConnectivityService.this.mDomainPreferHandler.hasMessages(2, req)) {
                HwConnectivityService.this.mDomainPreferHandler.removeMessages(2, req);
            }
        }

        private void handlePreferNetworkFail(Message msg) {
            NetworkRequestInfo nri = HwConnectivityService.this.findExistingNetworkRequestInfo(msg.arg1);
            if (nri == null || nri.mPreferType == null) {
                HwConnectivityService.log("handlePreferNetworkFail, nri or preferType is null.");
                return;
            }
            NetworkRequest req = nri.request;
            int domainPrefer = nri.mPreferType.value();
            if (HwConnectivityService.this.mDomainPreferHandler.hasMessages(2, req)) {
                HwConnectivityService.this.mDomainPreferHandler.removeMessages(2, req);
                retryNetworkRequestWhenPreferException(req, DomainPreferType.fromInt(domainPrefer), "FAIL");
            }
        }

        private void handlePreferNetworkTimeout(Message msg) {
            retryNetworkRequestWhenPreferException(msg.obj, DomainPreferType.fromInt(msg.arg1), "TIMEOUT");
        }

        private void retryNetworkRequestWhenPreferException(NetworkRequest req, DomainPreferType prefer, String reason) {
            HwConnectivityService.log("retryNetworkRequestWhenPreferException req = " + req + " prefer = " + prefer + " reason = " + reason);
            NetworkRequestInfo nri = (NetworkRequestInfo) HwConnectivityService.this.mNetworkRequests.get(req);
            if (nri != null) {
                if ((prefer == DomainPreferType.DOMAIN_PREFER_WIFI || prefer == DomainPreferType.DOMAIN_PREFER_CELLULAR || prefer == DomainPreferType.DOMAIN_PREFER_VOLTE) && ((NetworkAgentInfo) HwConnectivityService.this.mNetworkForRequestId.get(req.requestId)) == null && prefer != null) {
                    for (NetworkFactoryInfo nfi : HwConnectivityService.this.mNetworkFactoryInfos.values()) {
                        nfi.asyncChannel.sendMessage(536577, req);
                    }
                    NetworkCapabilities networkCapabilities = req.networkCapabilities;
                    NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
                    HwConnectivityService.this.mNetworkRequests.remove(req);
                    HwConnectivityService.this.mNetworkRequestInfoLogs.log("UPDATE-RELEASE " + nri);
                    if (DomainPreferType.DOMAIN_PREFER_WIFI == prefer) {
                        networkCapabilities.setNetworkSpecifier(null);
                        networkCapabilities.addTransportType(0);
                        networkCapabilities.removeTransportType(1);
                        networkCapabilities.setNetworkSpecifier(networkSpecifier);
                    } else if (DomainPreferType.DOMAIN_PREFER_CELLULAR == prefer || DomainPreferType.DOMAIN_PREFER_VOLTE == prefer) {
                        networkCapabilities.setNetworkSpecifier(null);
                        networkCapabilities.addTransportType(1);
                        networkCapabilities.removeTransportType(0);
                        if (networkSpecifier == null) {
                            networkCapabilities.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(SubscriptionManager.getDefaultDataSubscriptionId())));
                        } else {
                            networkCapabilities.setNetworkSpecifier(networkSpecifier);
                        }
                    }
                    HwConnectivityService.this.mNetworkRequests.put(req, nri);
                    HwConnectivityService.this.mNetworkRequestInfoLogs.log("UPDATE-REGISTER " + nri);
                    HwConnectivityService.this.rematchAllNetworksAndRequests(null, 0);
                    if (HwConnectivityService.this.mNetworkForRequestId.get(req.requestId) == null) {
                        HwConnectivityService.this.sendUpdatedScoreToFactories(req, 0);
                    }
                }
            }
        }
    }

    private class HwConnectivityServiceHandler extends Handler {
        private static final int EVENT_SHOW_ENABLE_PDP_DIALOG = 0;

        /* synthetic */ HwConnectivityServiceHandler(HwConnectivityService this$0, HwConnectivityServiceHandler -this1) {
            this();
        }

        private HwConnectivityServiceHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwConnectivityService.this.handleShowEnablePdpDialog();
                    return;
                default:
                    return;
            }
        }
    }

    private class MobileEnabledSettingObserver extends ContentObserver {
        public MobileEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver().registerContentObserver(Global.getUriFor("device_provisioned"), true, this);
        }

        public void onChange(boolean selfChange) {
            if (HwConnectivityService.this.mRemindService || HwConnectivityService.this.checkDataServiceRemindMsim()) {
                super.onChange(selfChange);
                if (!HwConnectivityService.this.getMobileDataEnabled() && HwConnectivityService.this.mDataServiceToPdpDialog == null) {
                    HwConnectivityService.this.mDataServiceToPdpDialog = HwConnectivityService.this.createWarningToPdp();
                    HwConnectivityService.this.mDataServiceToPdpDialog.show();
                }
            }
        }
    }

    private class RegisteredPushPkg {
        private static final String MSG_ALL_CTRLSOCKET_ALLOWED = "android.ctrlsocket.all.allowed";
        private static final String MSG_SCROFF_CTRLSOCKET_STATS = "android.scroff.ctrlsocket.status";
        private static final String ctrl_socket_version = "v2";
        private int ALLOW_ALL_CTRL_SOCKET_LEVEL = 2;
        private int ALLOW_NO_CTRL_SOCKET_LEVEL = 0;
        private int ALLOW_PART_CTRL_SOCKET_LEVEL = 1;
        private int ALLOW_SPECIAL_CTRL_SOCKET_LEVEL = 3;
        private int MAX_REGISTERED_PKG_NUM = 10;
        private final Uri WHITELIST_URI = Secure.getUriFor("push_white_apps");
        private CtrlSocketInfo mCtrlSocketInfo = new CtrlSocketInfo();
        private boolean mEnable = SystemProperties.getBoolean("ro.config.hw_power_saving", false);

        RegisteredPushPkg() {
        }

        public void init(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED);
            filter.addAction(HwConnectivityService.ACTION_BT_CONNECTION_CHANGED);
            if (this.mEnable) {
                filter.addAction(MSG_SCROFF_CTRLSOCKET_STATS);
                filter.addAction(MSG_ALL_CTRLSOCKET_ALLOWED);
                getCtrlSocketRegisteredPkg();
                getCtrlSocketPushWhiteList();
                context.getContentResolver().registerContentObserver(this.WHITELIST_URI, false, new ContentObserver(new Handler()) {
                    public void onChange(boolean selfChange) {
                        RegisteredPushPkg.this.getCtrlSocketPushWhiteList();
                    }
                });
            }
            context.registerReceiver(HwConnectivityService.this.mIntentReceiver, filter);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            switch (code) {
                case 1001:
                    String register_pkg = data.readString();
                    Log.d(HwConnectivityService.TAG, "CtrlSocket registerPushSocket pkg = " + register_pkg);
                    registerPushSocket(register_pkg);
                    return true;
                case 1002:
                    String unregister_pkg = data.readString();
                    Log.d(HwConnectivityService.TAG, "CtrlSocket unregisterPushSocket pkg = " + unregister_pkg);
                    unregisterPushSocket(unregister_pkg);
                    return true;
                case 1004:
                    reply.writeString(getActPkgInWhiteList());
                    return true;
                case 1005:
                    reply.writeInt(this.mCtrlSocketInfo.mAllowCtrlSocketLevel);
                    return true;
                case HwPackageManagerService.transaction_sendLimitedPackageBroadcast /*1006*/:
                    Log.d(HwConnectivityService.TAG, "CtrlSocket getCtrlSocketVersion = v2");
                    reply.writeString(ctrl_socket_version);
                    return true;
                default:
                    return false;
            }
        }

        private void registerPushSocket(String pkgName) {
            if (pkgName != null) {
                boolean isToAdd = false;
                for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                    if (pkg.equals(pkgName)) {
                        return;
                    }
                }
                if (this.mCtrlSocketInfo.mRegisteredCount >= this.MAX_REGISTERED_PKG_NUM) {
                    for (String pkg2 : this.mCtrlSocketInfo.mPushWhiteListPkg) {
                        if (pkg2.equals(pkgName)) {
                            isToAdd = true;
                        }
                    }
                } else {
                    isToAdd = true;
                }
                if (isToAdd) {
                    CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                    ctrlSocketInfo.mRegisteredCount++;
                    this.mCtrlSocketInfo.mRegisteredPkg.add(pkgName);
                    updateRegisteredPkg();
                }
            }
        }

        private void unregisterPushSocket(String pkgName) {
            if (pkgName != null) {
                int count = 0;
                boolean isMatch = false;
                for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                    if (pkg.equals(pkgName)) {
                        isMatch = true;
                        break;
                    }
                    count++;
                }
                if (isMatch) {
                    CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                    ctrlSocketInfo.mRegisteredCount--;
                    this.mCtrlSocketInfo.mRegisteredPkg.remove(count);
                    updateRegisteredPkg();
                }
            }
        }

        private void getCtrlSocketPushWhiteList() {
            String wlPkg = Secure.getString(HwConnectivityService.this.mContext.getContentResolver(), "push_white_apps");
            if (wlPkg != null) {
                String[] str = wlPkg.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                if (str != null && str.length > 0) {
                    this.mCtrlSocketInfo.mPushWhiteListPkg.clear();
                    for (int i = 0; i < str.length; i++) {
                        this.mCtrlSocketInfo.mPushWhiteListPkg.add(str[i]);
                        Log.d(HwConnectivityService.TAG, "CtrlSocket PushWhiteList[" + i + "] = " + str[i]);
                    }
                }
            }
        }

        private void getCtrlSocketRegisteredPkg() {
            String registeredPkg = Secure.getString(HwConnectivityService.this.mContext.getContentResolver(), "registered_pkgs");
            if (registeredPkg != null) {
                String[] str = registeredPkg.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                if (str != null && str.length > 0) {
                    this.mCtrlSocketInfo.mRegisteredPkg.clear();
                    this.mCtrlSocketInfo.mRegisteredCount = 0;
                    for (Object add : str) {
                        this.mCtrlSocketInfo.mRegisteredPkg.add(add);
                        CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                        ctrlSocketInfo.mRegisteredCount++;
                    }
                }
            }
        }

        private void updateRegisteredPkg() {
            StringBuffer registeredPkg = new StringBuffer();
            for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                registeredPkg.append(pkg);
                registeredPkg.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            }
            Secure.putString(HwConnectivityService.this.mContext.getContentResolver(), "registered_pkgs", registeredPkg.toString());
        }

        private String getActPkgInWhiteList() {
            if (this.ALLOW_PART_CTRL_SOCKET_LEVEL != this.mCtrlSocketInfo.mAllowCtrlSocketLevel) {
                return null;
            }
            StringBuffer activePkg = new StringBuffer();
            for (String pkg : this.mCtrlSocketInfo.mScrOffActPkg) {
                activePkg.append(pkg);
                activePkg.append("\t");
            }
            return activePkg.toString();
        }

        public void updateStatus(Intent intent) {
            String action = intent.getAction();
            if (MSG_SCROFF_CTRLSOCKET_STATS.equals(action)) {
                if (intent.getBooleanExtra("ctrl_socket_status", false)) {
                    this.mCtrlSocketInfo.mScrOffActPkg.clear();
                    this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_PART_CTRL_SOCKET_LEVEL;
                    String actPkgs = intent.getStringExtra("ctrl_socket_list");
                    if (!TextUtils.isEmpty(actPkgs)) {
                        String[] whitePackages = actPkgs.split("\t");
                        if (whitePackages != null) {
                            for (Object add : whitePackages) {
                                this.mCtrlSocketInfo.mScrOffActPkg.add(add);
                            }
                        }
                    }
                }
            } else if (MSG_ALL_CTRLSOCKET_ALLOWED.equals(action)) {
                this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_NO_CTRL_SOCKET_LEVEL;
            }
        }
    }

    private class WifiDisconnectManager {
        private static final String ACTION_SWITCH_TO_MOBILE_NETWORK = "android.intent.action.SWITCH_TO_MOBILE_NETWORK";
        private static final String ACTION_WIFI_NETWORK_CONNECTION_CHANGED = "huawei.intent.action.WIFI_NETWORK_CONNECTION_CHANGED";
        private static final String CONNECT_STATE = "connect_state";
        private static final String SWITCH_STATE = "switch_state";
        private static final int SWITCH_TO_WIFI_AUTO = 0;
        private static final String SWITCH_TO_WIFI_TYPE = "wifi_connect_type";
        private static final String WIFI_TO_PDP = "wifi_to_pdp";
        private static final int WIFI_TO_PDP_AUTO = 1;
        private static final int WIFI_TO_PDP_NEVER = 2;
        private static final int WIFI_TO_PDP_NOTIFY = 0;
        private final boolean REMIND_WIFI_TO_PDP;
        private boolean mDialogHasShown;
        State mLastWifiState;
        private BroadcastReceiver mNetworkSwitchReceiver;
        private boolean mShouldStartMobile;
        private Handler mSwitchHandler;
        private OnDismissListener mSwitchPdpListener;
        protected AlertDialog mWifiToPdpDialog;
        private boolean shouldShowDialogWhenConnectFailed;

        /* synthetic */ WifiDisconnectManager(HwConnectivityService this$0, WifiDisconnectManager -this1) {
            this();
        }

        private WifiDisconnectManager() {
            this.REMIND_WIFI_TO_PDP = SystemProperties.getBoolean("ro.config.hw_RemindWifiToPdp", false);
            this.mWifiToPdpDialog = null;
            this.mShouldStartMobile = false;
            this.shouldShowDialogWhenConnectFailed = true;
            this.mDialogHasShown = false;
            this.mLastWifiState = State.DISCONNECTED;
            this.mSwitchPdpListener = new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendMonitorWifiSwitchToMobileMessage(5000);
                    if (WifiDisconnectManager.this.mShouldStartMobile) {
                        HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, true);
                        HwConnectivityService.log("you have restart Mobile data service!");
                    }
                    WifiDisconnectManager.this.mShouldStartMobile = false;
                    WifiDisconnectManager.this.mWifiToPdpDialog = null;
                }
            };
            this.mNetworkSwitchReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (!WifiDisconnectManager.ACTION_SWITCH_TO_MOBILE_NETWORK.equals(intent.getAction())) {
                        return;
                    }
                    if (intent.getBooleanExtra(WifiDisconnectManager.SWITCH_STATE, true)) {
                        HwConnectivityService.this.wifiDisconnectManager.switchToMobileNetwork();
                    } else {
                        HwConnectivityService.this.wifiDisconnectManager.cancelSwitchToMobileNetwork();
                    }
                }
            };
            this.mSwitchHandler = new Handler() {
                public void handleMessage(Message msg) {
                    HwConnectivityService.log("mSwitchHandler recieve msg =" + msg.what);
                    switch (msg.what) {
                        case 0:
                            WifiDisconnectManager.this.switchToMobileNetwork();
                            return;
                        default:
                            return;
                    }
                }
            };
        }

        private boolean getAirplaneModeEnable() {
            boolean retVal = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "airplane_mode_on", 0) == 1;
            HwConnectivityService.log("getAirplaneModeEnable returning " + retVal);
            return retVal;
        }

        private AlertDialog createSwitchToPdpWarning() {
            HwConnectivityService.log("create dialog of switch to pdp");
            HwTelephonyFactory.getHwDataServiceChrManager().removeMonitorWifiSwitchToMobileMessage();
            Builder buider = new Builder(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this), 33947691);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013282, null);
            final CheckBox checkBox = (CheckBox) view.findViewById(34603157);
            buider.setView(view);
            buider.setTitle(33685520);
            buider.setIcon(17301543);
            buider.setPositiveButton(33685567, new OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    WifiDisconnectManager.this.mShouldStartMobile = true;
                    HwConnectivityService.log("setPositiveButton: mShouldStartMobile set true");
                    WifiDisconnectManager.this.checkUserChoice(checkBox.isChecked(), true);
                }
            });
            buider.setNegativeButton(33685568, new OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    HwConnectivityService.log("you have chose to disconnect Mobile data service!");
                    WifiDisconnectManager.this.mShouldStartMobile = false;
                    WifiDisconnectManager.this.checkUserChoice(checkBox.isChecked(), false);
                }
            });
            AlertDialog dialog = buider.create();
            dialog.setCancelable(false);
            dialog.getWindow().setType(2008);
            return dialog;
        }

        private void checkUserChoice(boolean rememberChoice, boolean enableDataConnect) {
            int showPopState;
            if (!rememberChoice) {
                showPopState = 0;
            } else if (enableDataConnect) {
                showPopState = 1;
            } else {
                showPopState = 0;
            }
            HwConnectivityService.log("checkUserChoice showPopState:" + showPopState + ", enableDataConnect:" + enableDataConnect);
            System.putInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, showPopState);
        }

        private void sendWifiBroadcast(boolean isConnectingOrConnected) {
            if (ActivityManagerNative.isSystemReady() && HwConnectivityService.this.sendWifiBroadcastAfterBootCompleted) {
                HwConnectivityService.log("notify settings:" + isConnectingOrConnected);
                Intent intent = new Intent(ACTION_WIFI_NETWORK_CONNECTION_CHANGED);
                intent.putExtra(CONNECT_STATE, isConnectingOrConnected);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(intent);
            }
        }

        private boolean shouldNotifySettings() {
            if (!isSwitchToWifiSupported() || System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), SWITCH_TO_WIFI_TYPE, 0) == 0) {
                return false;
            }
            return true;
        }

        private boolean isSwitchToWifiSupported() {
            if ("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", ""))) {
                return true;
            }
            return HwConnectivityService.this.mCust.isSupportWifiConnectMode();
        }

        private void switchToMobileNetwork() {
            if (getAirplaneModeEnable()) {
                HwConnectivityService.this.enableDefaultTypeAPN(true);
            } else if (this.shouldShowDialogWhenConnectFailed || !this.mDialogHasShown) {
                int value = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
                HwConnectivityService.log("WIFI_TO_PDP value =" + value);
                int wifiplusvalue = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "wifi_csp_dispaly_state", 1);
                HwConnectivityService.log("wifiplus_csp_dispaly_state value =" + wifiplusvalue);
                HwVSimManager hwVSimManager = HwVSimManager.getDefault();
                if (hwVSimManager != null && hwVSimManager.isVSimEnabled()) {
                    HwConnectivityService.log("vsim is enabled and following process will execute enableDefaultTypeAPN(true), so do nothing that likes value == WIFI_TO_PDP_AUTO");
                } else if (value == 0) {
                    if (wifiplusvalue == 0) {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't create WifiToPdpDialog");
                        HwConnectivityService.log("enableDefaultTypeAPN(true) in switchToMobileNetwork()  ");
                        HwConnectivityService.this.shouldEnableDefaultAPN();
                        return;
                    }
                    HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, false);
                    this.mShouldStartMobile = true;
                    this.mDialogHasShown = true;
                    this.mWifiToPdpDialog = createSwitchToPdpWarning();
                    this.mWifiToPdpDialog.setOnDismissListener(this.mSwitchPdpListener);
                    this.mWifiToPdpDialog.show();
                } else if (value != 1) {
                    if (1 == wifiplusvalue) {
                        HwConnectivityService.this.setMobileDataEnabled(HwConnectivityService.MODULE_WIFI, false);
                    } else {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't setMobileDataEnabled");
                    }
                }
                HwConnectivityService.log("enableDefaultTypeAPN(true) in switchToMobileNetwork( )");
                HwConnectivityService.this.shouldEnableDefaultAPN();
            }
        }

        private void cancelSwitchToMobileNetwork() {
            if (this.mWifiToPdpDialog != null) {
                Log.d(HwConnectivityService.TAG, "cancelSwitchToMobileNetwork and mWifiToPdpDialog is showing");
                this.mShouldStartMobile = true;
                this.mWifiToPdpDialog.dismiss();
            }
        }

        private void registerReceiver() {
            if (this.REMIND_WIFI_TO_PDP && isSwitchToWifiSupported()) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_SWITCH_TO_MOBILE_NETWORK);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).registerReceiver(this.mNetworkSwitchReceiver, filter);
            }
        }

        protected void hintUserSwitchToMobileWhileWifiDisconnected(State state, int type) {
            HwConnectivityService.log("hintUserSwitchToMobileWhileWifiDisconnected, state=" + state + "  type =" + type);
            boolean shouldEnableDefaultTypeAPN = true;
            if (this.REMIND_WIFI_TO_PDP) {
                if (state == State.DISCONNECTED && type == 1 && HwConnectivityService.this.getMobileDataEnabled()) {
                    if (this.mLastWifiState == State.CONNECTED) {
                        int value = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
                        HwConnectivityService.log("WIFI_TO_PDP value     =" + value);
                        if (value == 1) {
                            HwConnectivityService.this.shouldEnableDefaultAPN();
                            return;
                        }
                        this.shouldShowDialogWhenConnectFailed = true;
                        HwConnectivityService.log("mShouldEnableDefaultTypeAPN was set false");
                        shouldEnableDefaultTypeAPN = false;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(false);
                    } else if (getAirplaneModeEnable()) {
                        shouldEnableDefaultTypeAPN = true;
                    } else {
                        this.mSwitchHandler.sendMessageDelayed(this.mSwitchHandler.obtainMessage(0), 5000);
                        HwConnectivityService.log("switch message will be send in 5 seconds");
                    }
                    if (this.mLastWifiState == State.CONNECTING) {
                        this.shouldShowDialogWhenConnectFailed = false;
                    }
                } else if ((state == State.CONNECTED || state == State.CONNECTING) && type == 1) {
                    if (state == State.CONNECTED) {
                        this.mDialogHasShown = false;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(true);
                    } else if (this.mSwitchHandler.hasMessages(0)) {
                        this.mSwitchHandler.removeMessages(0);
                        HwConnectivityService.log("switch message was removed");
                    }
                    if (this.mWifiToPdpDialog != null) {
                        this.mShouldStartMobile = true;
                        this.mWifiToPdpDialog.dismiss();
                    }
                }
                if (type == 1) {
                    HwConnectivityService.log("mLastWifiState =" + this.mLastWifiState);
                    this.mLastWifiState = state;
                }
            }
            if (shouldEnableDefaultTypeAPN && state == State.DISCONNECTED && type == 1) {
                HwConnectivityService.log("enableDefaultTypeAPN(true) in hintUserSwitchToMobileWhileWifiDisconnected");
                HwConnectivityService.this.shouldEnableDefaultAPN();
            }
        }

        protected void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        }
    }

    private static /* synthetic */ int[] -getandroid-net-NetworkInfo$StateSwitchesValues() {
        if (-android-net-NetworkInfo$StateSwitchesValues != null) {
            return -android-net-NetworkInfo$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CONNECTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DISCONNECTED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.SUSPENDED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -android-net-NetworkInfo$StateSwitchesValues = iArr;
        return iArr;
    }

    private static void log(String s) {
        Slog.d(TAG, s);
    }

    private static void loge(String s) {
        Slog.e(TAG, s);
    }

    public HwConnectivityService(Context context, INetworkManagementService netd, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        super(context, netd, statsService, policyManager);
        this.mContext = context;
        this.mSimChangeAlertDataConnect = System.getString(context.getContentResolver(), "hw_sim_change_alert_data_connect");
        this.mRegisteredPushPkg.init(context);
        registerSimStateReceiver(context);
        this.wifiDisconnectManager.registerReceiver();
        registerPhoneStateListener(context);
        registerBootStateListener(context);
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mHandler = new HwConnectivityServiceHandler(this, null);
        this.mDomainPreferHandler = new DomainPreferHandler(this.mHandlerThread.getLooper());
        if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue()) {
            registerMapconIntentReceiver(context);
        }
        this.mServer = Global.getString(context.getContentResolver(), "captive_portal_server");
        if (this.mServer == null) {
            this.mServer = DEFAULT_SERVER;
        }
        SystemProperties.set("sys.defaultapn.enabled", StorageUtils.SDCARD_ROMOUNTED_STATE);
        registerTetheringReceiver(context);
        initGCMFixer(context);
    }

    private void initGCMFixer(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OF_ANDROID_BOOT_COMPLETED);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mGcmFixIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(HeartbeatReceiver.HEARTBEAT_FIXER_ACTION);
        this.mContext.registerReceiver(this.mHeartbeatReceiver, filter, "android.permission.CONNECTIVITY_INTERNAL", null);
    }

    private String[] getFeature(String str) {
        if (str == null) {
            throw new IllegalArgumentException("getFeature() received null string");
        }
        String[] result = new String[2];
        int subId = 0;
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            subId = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
            if (str.equals("enableMMS_sub2")) {
                str = "enableMMS";
                subId = 1;
            } else if (str.equals("enableMMS_sub1")) {
                str = "enableMMS";
                subId = 0;
            }
        }
        result[0] = str;
        result[1] = String.valueOf(subId);
        Slog.d(TAG, "getFeature: return feature=" + str + " subId=" + subId);
        return result;
    }

    protected String getMmsFeature(String feature) {
        Slog.d(TAG, "getMmsFeature HwFeatureConfig.dual_card_mms_switch" + HwFeatureConfig.dual_card_mms_switch);
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return feature;
        }
        String[] result = getFeature(feature);
        feature = result[0];
        this.phoneId = Integer.parseInt(result[1]);
        this.curMmsDataSub = -1;
        return feature;
    }

    protected boolean isAlwaysAllowMMSforRoaming(int networkType, String feature) {
        if (networkType == 0) {
            boolean isAlwaysAllowMMSforRoaming = isAlwaysAllowMMS;
            if (HwPhoneConstants.IS_CHINA_TELECOM) {
                boolean roaming = WrapperFactory.getMSimTelephonyManagerWrapper().isNetworkRoaming(this.phoneId);
                if (isAlwaysAllowMMSforRoaming) {
                    isAlwaysAllowMMSforRoaming = roaming ^ 1;
                }
            }
        }
        return true;
    }

    protected boolean isMmsAutoSetSubDiffFromDataSub(int networkType, String feature) {
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return false;
        }
        this.curPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        this.curMmsDataSub = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (!feature.equals("enableMMS") || networkType != 0) {
            return false;
        }
        if ((this.curMmsDataSub != 0 && 1 != this.curMmsDataSub) || this.phoneId == this.curMmsDataSub) {
            return false;
        }
        log("DSMMS dds is switching now, do not response request from another card, curMmsDataSub: " + this.curMmsDataSub);
        return true;
    }

    protected boolean isMmsSubDiffFromDataSub(int networkType, String feature) {
        if (HwFeatureConfig.dual_card_mms_switch && feature.equals("enableMMS") && networkType == 0 && this.curPrefDataSubscription != this.phoneId) {
            return true;
        }
        return false;
    }

    protected boolean isNetRequestersPidsContainCurrentPid(List<Integer>[] mNetRequestersPids, int usedNetworkType, Integer currentPid) {
        if (!HwFeatureConfig.dual_card_mms_switch || !WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled() || mNetRequestersPids[usedNetworkType].contains(currentPid)) {
            return true;
        }
        Slog.w(TAG, "not tearing down special network - not found pid " + currentPid);
        return false;
    }

    protected boolean isNeedTearMmsAndRestoreData(int networkType, String feature, Handler mHandler) {
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return true;
        }
        if (networkType != 0 || !feature.equals("enableMMS")) {
            return true;
        }
        if (!WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return true;
        }
        int curMmsDataSub = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (curMmsDataSub != 0 && 1 != curMmsDataSub) {
            return true;
        }
        int lastPrefDataSubscription;
        if (curMmsDataSub == 0) {
            lastPrefDataSubscription = 1;
        } else {
            lastPrefDataSubscription = 0;
        }
        int curPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        log("isNeedTearDataAndRestoreData lastPrefDataSubscription" + lastPrefDataSubscription + "curPrefDataSubscription" + curPrefDataSubscription);
        if (lastPrefDataSubscription != curPrefDataSubscription) {
            log("DSMMS >>>> disable a connection, after MMS net disconnected will switch back to phone " + lastPrefDataSubscription);
            WrapperFactory.getMSimTelephonyManagerWrapper().setPreferredDataSubscription(lastPrefDataSubscription);
        } else {
            log("DSMMS unexpected case, data subscription is already on " + curPrefDataSubscription);
        }
        WrapperFactory.getMSimTelephonyManagerWrapper().setMmsAutoSetDataSubscription(-1);
        return true;
    }

    private boolean isConnectedOrConnectingOrSuspended(NetworkInfo info) {
        boolean z = true;
        synchronized (this) {
            if (!(info.getState() == State.CONNECTED || info.getState() == State.CONNECTING)) {
                if (info.getState() != State.SUSPENDED) {
                    z = false;
                }
            }
        }
        return z;
    }

    private AlertDialog createWarningRoamingToPdp() {
        Builder buider = new Builder(connectivityServiceUtils.getContext(this), connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        buider.setTitle(33685962);
        buider.setMessage(33685963);
        buider.setIcon(17301543);
        buider.setPositiveButton(17040084, new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                HwTelephonyManagerInner.getDefault().setDataRoamingEnabledWithoutPromp(true);
                Toast.makeText(HwConnectivityService.this.mContext, 33685965, 1).show();
            }
        });
        buider.setNegativeButton(17040083, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(HwConnectivityService.this.mContext, 33685966, 1).show();
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(2008);
        return dialog;
    }

    private AlertDialog createWarningToPdp() {
        Builder buider;
        final String enable_Not_Remind_Function = Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        CheckBox checkBox = null;
        if (VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function)) {
            int themeID = connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            buider = new Builder(new ContextThemeWrapper(connectivityServiceUtils.getContext(this), themeID), themeID);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013280, null);
            checkBox = (CheckBox) view.findViewById(34603158);
            buider.setView(view);
            buider.setTitle(17039380);
        } else {
            buider = new Builder(connectivityServiceUtils.getContext(this), connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
            buider.setTitle(17039380);
            buider.setMessage(33685526);
        }
        final CheckBox finalBox = checkBox;
        buider.setIcon(17301543);
        buider.setPositiveButton(17040084, new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(true);
                if (!TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect)) {
                    if (HwConnectivityService.this.mShowWarningRoamingToPdp) {
                        HwConnectivityService.this.createWarningRoamingToPdp().show();
                    }
                    Toast.makeText(HwConnectivityService.this.mContext, 33685967, 1).show();
                }
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                HwConnectivityService.this.mDataServiceToPdpDialog = null;
                HwConnectivityService.this.mShowWarningRoamingToPdp = false;
            }
        });
        buider.setNegativeButton(17040083, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(new Intent(HwConnectivityService.DISABEL_DATA_SERVICE_ACTION));
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
                if (!TextUtils.isEmpty(HwConnectivityService.this.mSimChangeAlertDataConnect)) {
                    Toast.makeText(HwConnectivityService.this.mContext, 33685968, 1).show();
                }
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                HwConnectivityService.this.mDataServiceToPdpDialog = null;
                HwConnectivityService.this.mShowWarningRoamingToPdp = false;
            }
        });
        buider.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(new Intent(HwConnectivityService.DISABEL_DATA_SERVICE_ACTION));
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                HwConnectivityService.this.mDataServiceToPdpDialog = null;
                HwConnectivityService.this.mShowWarningRoamingToPdp = false;
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(2008);
        return dialog;
    }

    protected void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 33);
    }

    private final void updateCallState(int state) {
        if (this.mRemindService || SystemProperties.getBoolean("gsm.huawei.RemindDataService", false)) {
            int phoneState = state;
            if (state == 0) {
                if (this.mShowDlgEndCall && this.mDataServiceToPdpDialog == null) {
                    this.mDataServiceToPdpDialog = createWarningToPdp();
                    this.mDataServiceToPdpDialog.show();
                    this.mShowDlgEndCall = false;
                }
            } else if (this.mDataServiceToPdpDialog != null) {
                this.mDataServiceToPdpDialog.dismiss();
                this.mDataServiceToPdpDialog = null;
                this.mShowDlgEndCall = true;
            }
        }
    }

    protected void registerBootStateListener(Context context) {
        new MobileEnabledSettingObserver(new Handler()).register();
    }

    protected boolean needSetUserDataEnabled(boolean enabled) {
        int dataStatus = Global.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), "mobile_data", 1);
        if (!shouldShowThePdpWarning() || dataStatus != 0 || !enabled) {
            return true;
        }
        if (this.mShowDlgTurnOfDC) {
            this.mHandler.sendEmptyMessage(0);
            return false;
        }
        this.mShowDlgTurnOfDC = true;
        return true;
    }

    private void updateReminderSetting(boolean chooseNotRemind) {
        if (chooseNotRemind) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_NOT_SHOW_PDP);
        }
    }

    private boolean shouldShowThePdpWarning() {
        boolean z = false;
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return shouldShowThePdpWarningMsim();
        }
        String enable_Not_Remind_Function = Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        int pdpWarningValue = System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (!VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function)) {
            return remindDataAllow;
        }
        if (remindDataAllow && pdpWarningValue == VALUE_SHOW_PDP) {
            z = true;
        }
        return z;
    }

    private boolean checkDataServiceRemindMsim() {
        int lDataVal = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        if (lDataVal == 0) {
            if (TelephonyManager.getDefault().hasIccCard(lDataVal)) {
                return SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
            }
            return SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else if (1 == lDataVal) {
            return SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else {
            return false;
        }
    }

    private boolean shouldShowThePdpWarningMsim() {
        boolean z = true;
        String enableNotRemindFunction = Systemex.getString(this.mContext.getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean remindDataAllow = false;
        int lDataVal = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        if (1 == lDataVal) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else if (lDataVal == 0) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        }
        int pdpWarningValue = System.getInt(this.mContext.getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (!VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction)) {
            return remindDataAllow;
        }
        if (!(remindDataAllow && pdpWarningValue == VALUE_SHOW_PDP)) {
            z = false;
        }
        return z;
    }

    private boolean shouldDisablePortalCheck(String ssid) {
        if (ssid != null) {
            log("wifi ssid: " + ssid);
            if (ssid.length() > 2 && ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) == '\"') {
                Object ssid2 = ssid2.substring(1, ssid2.length() - 1);
            }
        }
        if (1 == Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) && 1 == Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) && SystemProperties.getBoolean("ro.config.hw_disable_portal", false)) {
            log("stop portal check for orange");
            return true;
        } else if ("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", "")) && "CMCC".equals(ssid2)) {
            log("stop portal check for CMCC");
            return true;
        } else if (1 == System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, 0)) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, 0);
            log("stop portal check for airsharing");
            return true;
        } else if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0 && StorageUtils.SDCARD_ROMOUNTED_STATE.equals(Systemex.getString(this.mContext.getContentResolver(), "wifi.challenge.required"))) {
            log("setup guide wifi disable portal, and does not start browser!");
            return true;
        } else if (1 == System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0)) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0);
            log("portal ap manual connect");
            return false;
        } else if (WifiProCommonUtils.isWifiProSwitchOn(this.mContext) && WifiProCommonUtils.isQueryActivityMatched(this.mContext, WifiProCommonUtils.HUAWEI_SETTINGS_WLAN)) {
            return false;
        } else {
            log("portal ap auto connect");
            return true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0139 A:{SYNTHETIC, Splitter: B:33:0x0139} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0139 A:{SYNTHETIC, Splitter: B:33:0x0139} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean startBrowserForWifiPortal(Notification notification, String ssid) {
        if (WifiProCommonUtils.isWifiProSwitchOn(this.mContext) && WifiProCommonUtils.isPortalBackground()) {
            log("WLAN+ enabled, don't pop up portal notification status bar again!");
            WifiProCommonUtils.portalBackgroundStatusChanged(false);
            return true;
        } else if (shouldDisablePortalCheck(ssid)) {
            log("do not start browser, popup system notification");
            return false;
        } else {
            log("setNotificationVisible: cancel notification and start browser directly for TYPE_WIFI..");
            try {
                Intent intent;
                WifiInfo info;
                if (IS_CHINA) {
                    String operator = TelephonyManager.getDefault().getNetworkOperator();
                    if (operator == null || operator.length() == 0 || !operator.startsWith("460")) {
                        this.mURL = new URL("http://" + this.mServer + "/generate_204");
                        intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                        intent.setFlags(272629760);
                        notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), 0, intent, 0);
                        try {
                            info = ((WifiManager) connectivityServiceUtils.getContext(this).getSystemService(MODULE_WIFI)).getConnectionInfo();
                            if (info == null) {
                                return true;
                            }
                            if (1 != HiLinkUtil.getHiLinkSsidType(connectivityServiceUtils.getContext(this), WifiInfo.removeDoubleQuotes(info.getSSID()), info.getBSSID())) {
                                intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                                connectivityServiceUtils.getContext(this).startActivity(intent);
                            } else {
                                String uri = HiLinkUtil.getLaunchAppForSsid(connectivityServiceUtils.getContext(this), WifiInfo.removeDoubleQuotes(info.getSSID()), info.getBSSID());
                                log("launch HILINK_ROUTER  " + uri);
                                if (HiLinkUtil.SCHEME_GATEWAY.equals(uri)) {
                                    intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                                    connectivityServiceUtils.getContext(this).startActivity(intent);
                                } else {
                                    HiLinkUtil.startDeviceGuide(connectivityServiceUtils.getContext(this), uri);
                                }
                            }
                            return true;
                        } catch (ActivityNotFoundException e) {
                            try {
                                log("default browser not exist..");
                                if (isSetupWizardCompleted()) {
                                    notification.contentIntent.send();
                                } else {
                                    log("setup wizard is not completed");
                                    Network network = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetwork();
                                    String captivePortalUserAgent = getCaptivePortalUserAgent(this.mContext);
                                    Intent intentPortal = new Intent("android.net.conn.CAPTIVE_PORTAL");
                                    intentPortal.putExtra("android.net.extra.NETWORK", network);
                                    intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new ICaptivePortal.Stub() {
                                        public void appResponse(int response) {
                                        }
                                    }));
                                    intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL_URL", this.mURL.toString());
                                    intent.putExtra("android.net.extra.CAPTIVE_PORTAL_USER_AGENT", captivePortalUserAgent);
                                    intentPortal.setFlags(272629760);
                                    intentPortal.putExtra(FLAG_SETUP_WIZARD, true);
                                    connectivityServiceUtils.getContext(this).startActivity(intentPortal);
                                }
                            } catch (CanceledException e2) {
                                log("Sending contentIntent failed: " + e2);
                            } catch (ActivityNotFoundException e3) {
                                loge("Activity not found: " + e3);
                            }
                        }
                    } else {
                        this.mURL = new URL(HwNetworkPropertyChecker.CHINA_MAINLAND_MAIN_SERVER);
                        intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                        intent.setFlags(272629760);
                        notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), 0, intent, 0);
                        info = ((WifiManager) connectivityServiceUtils.getContext(this).getSystemService(MODULE_WIFI)).getConnectionInfo();
                        if (info == null) {
                        }
                    }
                } else {
                    this.mURL = new URL("http://" + this.mServer + "/generate_204");
                    intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                    intent.setFlags(272629760);
                    notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), 0, intent, 0);
                    info = ((WifiManager) connectivityServiceUtils.getContext(this).getSystemService(MODULE_WIFI)).getConnectionInfo();
                    if (info == null) {
                    }
                }
            } catch (MalformedURLException e4) {
                log("MalformedURLException " + e4);
            }
        }
    }

    public boolean isSystemBootComplete() {
        return this.sendWifiBroadcastAfterBootCompleted;
    }

    protected void hintUserSwitchToMobileWhileWifiDisconnected(State state, int type) {
        if (WifiProCommonUtils.isWifiSelfCuring() && state == State.DISCONNECTED && type == 1) {
            Log.d("HwSelfCureEngine", "DISCONNECTED, but enableDefaultTypeAPN-->UP is ignored due to wifi self curing.");
        } else {
            this.wifiDisconnectManager.hintUserSwitchToMobileWhileWifiDisconnected(state, type);
        }
    }

    protected void enableDefaultTypeApnWhenWifiConnectionStateChanged(State state, int type) {
        if (state == State.DISCONNECTED && type == 1) {
            this.mIsWifiConnected = false;
        } else if (state == State.CONNECTED && type == 1) {
            this.mIsWifiConnected = true;
            if (WifiProCommonUtils.isWifiSelfCuring()) {
                Log.d("HwSelfCureEngine", "CONNECTED, but enableDefaultTypeAPN-->DOWN is ignored due to wifi self curing.");
                return;
            }
            enableDefaultTypeAPN(false);
        }
    }

    private void shouldEnableDefaultAPN() {
        if (!this.mIsBlueThConnected) {
            enableDefaultTypeAPN(true);
        }
    }

    private void sendBlueToothTetheringBroadcast(boolean isBttConnected) {
        log("sendBroad bt_tethering_connect_state = " + isBttConnected);
        Intent intent = new Intent("android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED");
        intent.putExtra("btt_connect_state", isBttConnected);
        connectivityServiceUtils.getContext(this).sendBroadcast(intent);
    }

    protected void enableDefaultTypeApnWhenBlueToothTetheringStateChanged(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        if (newInfo.getType() == 7) {
            log("enter BlueToothTethering State Changed");
            State state = newInfo.getState();
            if (state == State.CONNECTED) {
                this.mIsBlueThConnected = true;
                sendBlueToothTetheringBroadcast(true);
                enableDefaultTypeAPN(false);
            } else if (state == State.DISCONNECTED) {
                this.mIsBlueThConnected = false;
                sendBlueToothTetheringBroadcast(false);
                if (!this.mIsWifiConnected) {
                    enableDefaultTypeAPN(true);
                }
            }
        }
    }

    public void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        this.wifiDisconnectManager.makeDefaultAndHintUser(newNetwork);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int i = 0;
        switch (code) {
            case 1101:
                boolean z;
                data.enforceInterface(descriptor);
                int enableInt = data.readInt();
                Log.d(TAG, "needSetUserDataEnabled enableInt = " + enableInt);
                if (enableInt == 1) {
                    z = true;
                } else {
                    z = false;
                }
                boolean result = needSetUserDataEnabled(z);
                Log.d(TAG, "needSetUserDataEnabled result = " + result);
                reply.writeNoException();
                if (result) {
                    i = 1;
                }
                reply.writeInt(i);
                return true;
            default:
                if (this.mRegisteredPushPkg.onTransact(code, data, reply, flags)) {
                    return true;
                }
                return super.onTransact(code, data, reply, flags);
        }
    }

    private void setMobileDataEnabled(String module, boolean enabled) {
        Log.d(TAG, "module:" + module + " setMobileDataEnabled enabled = " + enabled);
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            tm.setDataEnabled(enabled);
            tm.setDataEnabledProperties(module, enabled);
        }
    }

    private boolean getDataEnabled() {
        boolean ret = false;
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            ret = tm.getDataEnabled();
        }
        Log.d(TAG, "CtrlSocket getMobileDataEnabled enabled = " + ret);
        return ret;
    }

    public boolean getMobileDataEnabled() {
        boolean ret = false;
        if (!this.mIsSimStateChanged) {
            return false;
        }
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            boolean ret2 = false;
            try {
                int phoneCount = tm.getPhoneCount();
                for (int slotId = 0; slotId < phoneCount; slotId++) {
                    if (tm.getSimState(slotId) == 5) {
                        ret2 = true;
                    }
                }
                if (ret2) {
                    ret = tm.getDataEnabled();
                } else {
                    Log.d(TAG, "all sim card not ready,return false");
                    return false;
                }
            } catch (NullPointerException e) {
                Log.d(TAG, "getMobileDataEnabled NPE");
            }
        }
        Log.d(TAG, "CtrlSocket getMobileDataEnabled = " + ret);
        return ret;
    }

    private void enableDefaultTypeAPN(boolean enabled) {
        Log.d(TAG, "enableDefaultTypeAPN= " + enabled);
        Log.d(TAG, "DEFAULT_MOBILE_ENABLE before state is " + SystemProperties.get("sys.defaultapn.enabled", StorageUtils.SDCARD_ROMOUNTED_STATE));
        SystemProperties.set("sys.defaultapn.enabled", enabled ? StorageUtils.SDCARD_ROMOUNTED_STATE : StorageUtils.SDCARD_RWMOUNTED_STATE);
        HwTelephonyManagerInner hwTm = HwTelephonyManagerInner.getDefault();
        if (hwTm != null) {
            hwTm.setDefaultMobileEnable(enabled);
        }
    }

    private void registerSimStateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        context.registerReceiver(this.mSimStateReceiver, filter);
    }

    private void handleShowEnablePdpDialog() {
        if (this.mDataServiceToPdpDialog == null) {
            this.mDataServiceToPdpDialog = createWarningToPdp();
            this.mDataServiceToPdpDialog.show();
        }
    }

    private void registerTetheringReceiver(Context context) {
        if (HwDeliverInfo.isIOTVersion() && SystemProperties.getBoolean("ro.config.persist_usb_tethering", false)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.hardware.usb.action.USB_STATE");
            context.registerReceiver(this.mTetheringReceiver, filter);
        }
    }

    protected void setExplicitlyUnselected(NetworkAgentInfo nai) {
        if (nai != null) {
            nai.networkMisc.explicitlySelected = false;
            nai.networkMisc.acceptUnvalidated = false;
            if (nai.networkInfo != null && ConnectivityManager.getNetworkTypeName(1).equals(nai.networkInfo.getTypeName())) {
                log("setExplicitlyUnselected, WiFi+ switch from WiFi to Cellular, enableDefaultTypeAPN explicitly.");
                enableDefaultTypeAPN(true);
            }
        }
    }

    protected void updateNetworkConcurrently(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        NetworkInfo oldInfo;
        State state = newInfo.getState();
        INetworkManagementService netd = connectivityServiceUtils.getNetd(this);
        synchronized (networkAgent) {
            oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        if (oldInfo != null && oldInfo.getState() == state) {
            log("updateNetworkConcurrently, ignoring duplicate network state non-change");
        } else if (netd == null) {
            loge("updateNetworkConcurrently, invalid member, netd = null");
        } else {
            networkAgent.setCurrentScore(0);
            try {
                String str;
                int i = networkAgent.network.netId;
                if (networkAgent.networkCapabilities.hasCapability(13)) {
                    str = null;
                } else {
                    str = "SYSTEM";
                }
                netd.createPhysicalNetwork(i, str);
                networkAgent.created = true;
                connectivityServiceUtils.updateLinkProperties(this, networkAgent, null);
                log("updateNetworkConcurrently, nai.networkInfo = " + networkAgent.networkInfo);
                networkAgent.asyncChannel.sendMessage(528391, 4, 0, null);
            } catch (Exception e) {
                loge("updateNetworkConcurrently, Error creating network " + networkAgent.network.netId + ": " + e.getMessage());
            }
        }
    }

    public void triggerRoamingNetworkMonitor(NetworkAgentInfo networkAgent) {
        if (networkAgent != null && networkAgent.networkMonitor != null) {
            log("triggerRoamingNetworkMonitor, nai.networkInfo = " + networkAgent.networkInfo);
            networkAgent.networkMonitor.sendMessage(532581);
        }
    }

    protected boolean reportPortalNetwork(NetworkAgentInfo nai, int result) {
        if (result != 2) {
            return false;
        }
        nai.asyncChannel.sendMessage(528391, 3, 0, null);
        return true;
    }

    protected boolean ignoreRemovedByWifiPro(NetworkAgentInfo nai) {
        if (nai.networkInfo.getType() == 1 && WifiProCommonUtils.isWifiProSwitchOn(this.mContext)) {
            return true;
        }
        return false;
    }

    public Network getNetworkForTypeWifi() {
        Network activeNetwork = super.getActiveNetwork();
        NetworkInfo activeNetworkInfo = super.getActiveNetworkInfo();
        Network[] networks = super.getAllNetworks();
        if (activeNetworkInfo != null) {
            if (activeNetworkInfo.getType() == 1) {
                return activeNetwork;
            }
            if (activeNetworkInfo.getType() == 1) {
                return null;
            }
            int i = 0;
            while (i < networks.length) {
                if (!(activeNetwork == null || networks[i].netId == activeNetwork.netId)) {
                    NetworkCapabilities nc = super.getNetworkCapabilities(networks[i]);
                    if (nc != null && nc.hasTransport(1)) {
                        return networks[i];
                    }
                }
                i++;
            }
            return null;
        } else if (networks.length >= 1) {
            return networks[0];
        } else {
            return null;
        }
    }

    private NetworkInfo getNetworkInfoForBackgroundWifi() {
        NetworkInfo activeNetworkInfo = super.getActiveNetworkInfo();
        Network[] networks = super.getAllNetworks();
        if (activeNetworkInfo != null || networks.length != 1) {
            return null;
        }
        NetworkInfo result = new NetworkInfo(1, 0, ConnectivityManager.getNetworkTypeName(1), "");
        result.setDetailedState(DetailedState.CONNECTED, null, null);
        return result;
    }

    protected void setVpnSettingValue(boolean enable) {
        log("WiFi_PRO, setVpnSettingValue =" + enable);
        System.putInt(this.mContext.getContentResolver(), "wifipro_network_vpn_state", enable ? 1 : 0);
    }

    private boolean isRequestedByPkgName(int pID, String pkgName) {
        List<RunningAppProcessInfo> appProcessList = this.mActivityManager.getRunningAppProcesses();
        if (appProcessList == null || pkgName == null) {
            return false;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess != null && appProcess.pid == pID) {
                String[] pkgNameList = appProcess.pkgList;
                for (Object equals : pkgNameList) {
                    if (pkgName.equals(equals)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public NetworkInfo getActiveNetworkInfo() {
        NetworkInfo networkInfo = super.getActiveNetworkInfo();
        if (networkInfo != null || !isRequestedByPkgName(Binder.getCallingPid(), SYSTEM_MANAGER_PKG_NAME)) {
            return networkInfo;
        }
        Slog.d(TAG, "return the background wifi network info for system manager.");
        return getNetworkInfoForBackgroundWifi();
    }

    protected boolean isNetworkRequestBip(NetworkRequest nr) {
        if (nr == null) {
            loge("network request is null!");
            return false;
        } else if (nr.networkCapabilities.hasCapability(19) || nr.networkCapabilities.hasCapability(20) || nr.networkCapabilities.hasCapability(21) || nr.networkCapabilities.hasCapability(22) || nr.networkCapabilities.hasCapability(23) || nr.networkCapabilities.hasCapability(24) || nr.networkCapabilities.hasCapability(25)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean checkNetworkSupportBip(NetworkAgentInfo nai, NetworkRequest nri) {
        if (HwModemCapability.isCapabilitySupport(1)) {
            log("MODEM is support BIP!");
            return false;
        } else if (nai == null || nri == null || nai.networkInfo == null) {
            loge("network agent or request is null, just return false!");
            return false;
        } else if (nai.networkInfo.getType() != 0 || (nai.isInternet() ^ 1) != 0) {
            loge("NOT support internet or NOT mobile!");
            return false;
        } else if (isNetworkRequestBip(nri)) {
            String defaultApn = SystemProperties.get("gsm.default.apn");
            String bipApn = SystemProperties.get("gsm.bip.apn");
            if (defaultApn == null || bipApn == null) {
                loge("default apn is null or bip apn is null, default: " + defaultApn + ", bip: " + bipApn);
                return false;
            } else if (MemoryConstant.MEM_SCENE_DEFAULT.equals(bipApn)) {
                log("bip use default network, return true");
                return true;
            } else {
                String[] buffers = bipApn.split(",");
                if (buffers.length <= 1 || !defaultApn.equalsIgnoreCase(buffers[1].trim())) {
                    log("network do NOT support bip, default: " + defaultApn + ", bip: " + bipApn);
                    return false;
                }
                log("default apn support bip, default: " + defaultApn + ", bip: " + buffers[1].trim());
                return true;
            }
        } else {
            loge("network request is NOT bip!");
            return false;
        }
    }

    public void setLteMobileDataEnabled(boolean enable) {
        log("[enter]setLteMobileDataEnabled " + enable);
        enforceChangePermission();
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(enable ? 1 : 0);
        sendLteDataStateBroadcast(mLteMobileDataState);
    }

    public int checkLteConnectState() {
        enforceAccessPermission();
        return mLteMobileDataState;
    }

    protected void handleLteMobileDataStateChange(NetworkInfo info) {
        if (info == null) {
            Slog.e(TAG, "NetworkInfo got null!");
            return;
        }
        Slog.d(TAG, "[enter]handleLteMobileDataStateChange type=" + info.getType() + ",subType=" + info.getSubtype());
        if (info.getType() == 0) {
            int lteState;
            if (13 == info.getSubtype()) {
                lteState = mapDataStateToLteDataState(info.getState());
            } else {
                lteState = 3;
            }
            setLteMobileDataState(lteState);
        }
    }

    private int mapDataStateToLteDataState(State state) {
        log("[enter]mapDataStateToLteDataState state=" + state);
        switch (-getandroid-net-NetworkInfo$StateSwitchesValues()[state.ordinal()]) {
            case 1:
                return 1;
            case 2:
                return 0;
            case 3:
                return 3;
            case 4:
                return 2;
            default:
                Slog.d(TAG, "mapDataStateToLteDataState ignore state = " + state);
                return 3;
        }
    }

    private synchronized void setLteMobileDataState(int state) {
        Slog.d(TAG, "[enter]setLteMobileDataState state=" + state);
        mLteMobileDataState = state;
        sendLteDataStateBroadcast(mLteMobileDataState);
    }

    private void sendLteDataStateBroadcast(int state) {
        Intent intent = new Intent("android.net.wifi.LTEDATA_COMPLETED_ACTION");
        intent.putExtra("lte_mobile_data_status", state);
        Slog.d(TAG, "Send sticky broadcast from ConnectivityService. intent=" + intent);
        sendStickyBroadcast(intent);
    }

    public long getLteTotalRxBytes() {
        Slog.d(TAG, "[enter]getLteTotalRxBytes");
        enforceAccessPermission();
        long lteRxBytes = 0;
        try {
            Entry entry = getLteStatsEntry(2);
            if (entry != null) {
                lteRxBytes = entry.rxBytes;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "lteTotalRxBytes=" + lteRxBytes);
        return lteRxBytes;
    }

    public long getLteTotalTxBytes() {
        Slog.d(TAG, "[enter]getLteTotalTxBytes");
        enforceAccessPermission();
        long lteTxBytes = 0;
        try {
            Entry entry = getLteStatsEntry(8);
            if (entry != null) {
                lteTxBytes = entry.txBytes;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "LteTotalTxBytes=" + lteTxBytes);
        return lteTxBytes;
    }

    private Entry getLteStatsEntry(int fields) {
        Log.d(TAG, "[enter]getLteStatsEntry fields=" + fields);
        Entry entry = null;
        try {
            NetworkTemplate mobile4gTemplate = NetworkTemplate.buildTemplateMobile4g(((TelephonyManager) this.mContext.getSystemService("phone")).getSubscriberId());
            getStatsService().forceUpdate();
            INetworkStatsSession session = getStatsService().openSession();
            if (session != null) {
                NetworkStatsHistory networkStatsHistory = session.getHistoryForNetwork(mobile4gTemplate, fields);
                if (networkStatsHistory != null) {
                    entry = networkStatsHistory.getValues(Long.MIN_VALUE, Long.MAX_VALUE, null);
                }
            }
            TrafficStats.closeQuietly(session);
        } catch (Exception e) {
            e.printStackTrace();
            TrafficStats.closeQuietly(null);
        } catch (Throwable th) {
            TrafficStats.closeQuietly(null);
            throw th;
        }
        return entry;
    }

    private static synchronized INetworkStatsService getStatsService() {
        INetworkStatsService iNetworkStatsService;
        synchronized (HwConnectivityService.class) {
            Log.d(TAG, "[enter]getStatsService");
            if (mStatsService == null) {
                mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
            }
            iNetworkStatsService = mStatsService;
        }
        return iNetworkStatsService;
    }

    protected void registerMapconIntentReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MAPCON_START_INTENT);
        filter.addAction(ACTION_MAPCON_SERVICE_FAILED);
        context.registerReceiver(this.mMapconIntentReceiver, filter);
    }

    private int getVoWifiServiceDomain(int phoneId, int type) {
        int domainPrefer = -1;
        if (this.mMapconService == null) {
            return domainPrefer;
        }
        try {
            return this.mMapconService.getVoWifiServiceDomain(phoneId, type);
        } catch (RemoteException ex) {
            loge("getVoWifiServiceDomain failed, err = " + ex.toString());
            return domainPrefer;
        }
    }

    private int getVoWifiServiceState(int phoneId, int type) {
        int domainSwitch = -1;
        if (this.mMapconService == null) {
            return domainSwitch;
        }
        try {
            return this.mMapconService.getVoWifiServiceState(phoneId, type);
        } catch (RemoteException ex) {
            loge("getVoWifiServiceState failed, err = " + ex.toString());
            return domainSwitch;
        }
    }

    private int getPhoneIdFromNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        int subId = -1;
        int phoneId = -1;
        NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
        if (networkSpecifier != null) {
            try {
                if (networkSpecifier instanceof StringNetworkSpecifier) {
                    subId = Integer.parseInt(networkSpecifier.toString());
                }
            } catch (NumberFormatException ex) {
                loge("getPhoneIdFromNetworkCapabilities exception, ex = " + ex.toString());
            }
        }
        if (subId != -1) {
            phoneId = SubscriptionManager.getPhoneId(subId);
        }
        log("getPhoneIdFromNetworkCapabilities, subId = " + subId + " phoneId:" + phoneId);
        return phoneId;
    }

    private boolean isDomainPreferRequest(NetworkRequest request) {
        if (Type.REQUEST == request.type) {
            return request.networkCapabilities.hasCapability(0);
        }
        return false;
    }

    private boolean rebuildNetworkRequestByPrefer(NetworkRequestInfo nri, DomainPreferType prefer) {
        NetworkRequest clientRequest = new NetworkRequest(nri.request);
        NetworkCapabilities networkCapabilities = nri.request.networkCapabilities;
        NetworkSpecifier networkSpecifier = networkCapabilities.getNetworkSpecifier();
        if (DomainPreferType.DOMAIN_ONLY_WIFI == prefer || DomainPreferType.DOMAIN_PREFER_WIFI == prefer) {
            networkCapabilities.setNetworkSpecifier(null);
            nri.mPreferType = prefer;
            nri.clientRequest = clientRequest;
            networkCapabilities.addTransportType(1);
            networkCapabilities.removeTransportType(0);
            if (networkSpecifier == null) {
                networkCapabilities.setNetworkSpecifier(new StringNetworkSpecifier(Integer.toString(SubscriptionManager.getDefaultDataSubscriptionId())));
                return true;
            }
            networkCapabilities.setNetworkSpecifier(networkSpecifier);
            return true;
        } else if (DomainPreferType.DOMAIN_PREFER_CELLULAR != prefer && DomainPreferType.DOMAIN_PREFER_VOLTE != prefer) {
            return false;
        } else {
            networkCapabilities.setNetworkSpecifier(null);
            nri.mPreferType = prefer;
            nri.clientRequest = clientRequest;
            networkCapabilities.addTransportType(0);
            networkCapabilities.removeTransportType(1);
            networkCapabilities.setNetworkSpecifier(networkSpecifier);
            return true;
        }
    }

    protected void handleRegisterNetworkRequest(NetworkRequestInfo nri) {
        NetworkCapabilities cap = nri.request.networkCapabilities;
        int domainPrefer = -1;
        if (isWifiMmsUtOn && isMapconOn && isDomainPreferRequest(nri.request)) {
            int phoneId;
            if (cap.getNetworkSpecifier() != null) {
                phoneId = getPhoneIdFromNetworkCapabilities(cap);
            } else {
                phoneId = SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultDataSubscriptionId());
            }
            if (phoneId != -1 && cap.hasCapability(0) && 1 == getVoWifiServiceState(phoneId, 1)) {
                domainPrefer = getVoWifiServiceDomain(phoneId, 0);
            }
            DomainPreferType prefer = DomainPreferType.fromInt(domainPrefer);
            if (prefer == null || !rebuildNetworkRequestByPrefer(nri, prefer)) {
                StringBuilder append = new StringBuilder().append("request(").append(nri.request).append(") domainPrefer = ");
                if (prefer == null) {
                    prefer = "null";
                }
                log(append.append(prefer).toString());
            } else {
                log("Update request(" + nri.clientRequest + ") to " + nri.request + " by " + prefer);
                if (DomainPreferType.DOMAIN_PREFER_WIFI == prefer || DomainPreferType.DOMAIN_PREFER_CELLULAR == prefer || DomainPreferType.DOMAIN_PREFER_VOLTE == prefer) {
                    this.mDomainPreferHandler.sendMessageDelayed(this.mDomainPreferHandler.obtainMessage(2, prefer.value(), 0, nri.request), MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
                }
            }
        }
        super.handleRegisterNetworkRequest(nri);
    }

    protected void handleReleaseNetworkRequest(NetworkRequest request, int callingUid) {
        NetworkRequest req = request;
        if (isWifiMmsUtOn && isMapconOn && isDomainPreferRequest(request)) {
            NetworkRequestInfo nri = findExistingNetworkRequestInfo(request.requestId);
            if (!(nri == null || (nri.request.equals(request) ^ 1) == 0)) {
                if (nri.clientRequest == null || !nri.clientRequest.equals(request)) {
                    loge("BUG: Do not find request in mNetworkRequests for preferRequest:" + request);
                } else {
                    req = nri.request;
                }
            }
        }
        super.handleReleaseNetworkRequest(req, callingUid);
    }

    protected void handleRemoveNetworkRequest(NetworkRequestInfo nri, int whichCallback) {
        if (isWifiMmsUtOn && isMapconOn && isDomainPreferRequest(nri.request)) {
            this.mDomainPreferHandler.removeMessages(2, nri.request);
        }
        super.handleRemoveNetworkRequest(nri, whichCallback);
    }

    protected void notifyNetworkAvailable(NetworkAgentInfo nai, NetworkRequestInfo nri) {
        if (isWifiMmsUtOn && isMapconOn && nri != null && isDomainPreferRequest(nri.request)) {
            this.mDomainPreferHandler.sendMessageAtFrontOfQueue(Message.obtain(this.mDomainPreferHandler, 0, nri.request));
        }
        super.notifyNetworkAvailable(nai, nri);
    }

    private NetworkRequestInfo findExistingNetworkRequestInfo(int requestId) {
        for (Map.Entry<NetworkRequest, NetworkRequestInfo> entry : this.mNetworkRequests.entrySet()) {
            if (((NetworkRequestInfo) entry.getValue()).request.requestId == requestId) {
                return (NetworkRequestInfo) entry.getValue();
            }
        }
        return null;
    }

    public boolean turnOffVpn(String packageName, int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "ConnectivityService");
        }
        this.mContext.enforceCallingOrSelfPermission(MDM_VPN_PERMISSION, "NEED MDM_VPN PERMISSION");
        SparseArray<Vpn> mVpns = getmVpns();
        synchronized (mVpns) {
            Vpn vpn = (Vpn) mVpns.get(userId);
            if (vpn != null) {
                boolean turnOffAllVpn = vpn.turnOffAllVpn(packageName);
                return turnOffAllVpn;
            }
            return false;
        }
    }

    private void processWhenSimStateChange(Intent intent) {
        if (!TelephonyManager.getDefault().isMultiSimEnabled() && (this.isAlreadyPop ^ 1) != 0) {
            if (AwareJobSchedulerConstants.SIM_STATUS_READY.equals(intent.getStringExtra("ss"))) {
                this.mIsSimReady = true;
                connectivityServiceUtils.getContext(this).sendBroadcast(new Intent(DISABEL_DATA_SERVICE_ACTION));
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
            }
        }
    }

    private boolean isSetupWizardCompleted() {
        if (1 == Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0)) {
            return 1 == Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 0);
        } else {
            return false;
        }
    }

    private static String getCaptivePortalUserAgent(Context context) {
        return getGlobalSetting(context, "captive_portal_user_agent", DEFAULT_USER_AGENT);
    }

    private static String getGlobalSetting(Context context, String symbol, String defaultValue) {
        String value = Global.getString(context.getContentResolver(), symbol);
        return value != null ? value : defaultValue;
    }
}
