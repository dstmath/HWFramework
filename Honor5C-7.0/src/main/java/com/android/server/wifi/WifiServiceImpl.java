package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.Network;
import android.net.NetworkScorerAppManager;
import android.net.NetworkUtils;
import android.net.Uri;
import android.net.wifi.HiSiWifiComm;
import android.net.wifi.IWifiManager.Stub;
import android.net.wifi.PasspointManagementObjectDefinition;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanSettings;
import android.net.wifi.WifiActivityEnergyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConnectionStatistics;
import android.net.wifi.WifiDetectConfInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.wifipro.WifiProStatusUtils;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.ContextThemeWrapper;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.AsyncChannel;
import com.android.server.am.BatteryStatsService;
import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.configparse.ConfigBuilder;
import com.google.protobuf.nano.Extension;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WifiServiceImpl extends Stub {
    private static final String BOOT_DEFAULT_WIFI_COUNTRY_CODE = "ro.boot.wificountrycode";
    private static final boolean DBG = true;
    private static final String TAG = "WifiService";
    private static final boolean VDBG = false;
    private final AppOpsManager mAppOps;
    private final IBatteryStats mBatteryStats;
    private final WifiCertManager mCertManager;
    private ClientHandler mClientHandler;
    private final Context mContext;
    private final WifiCountryCode mCountryCode;
    private final FrameworkFacade mFacade;
    private int mFullHighPerfLocksAcquired;
    private int mFullHighPerfLocksReleased;
    private int mFullLocksAcquired;
    private int mFullLocksReleased;
    private HiSiWifiComm mHiSiWifiComm;
    private HwWifiCHRService mHwWifiCHRService;
    boolean mInIdleMode;
    private boolean mIsApDialogNeedShow;
    private boolean mIsDialogNeedShow;
    private boolean mIsP2pCloseDialogExist;
    final LockList mLocks;
    private int mMulticastDisabled;
    private int mMulticastEnabled;
    private final List<Multicaster> mMulticasters;
    private WifiNotificationController mNotificationController;
    private final BroadcastReceiver mPackageOrUserReceiver;
    private final PowerManager mPowerManager;
    private final BroadcastReceiver mReceiver;
    private int mScanLocksAcquired;
    private int mScanLocksReleased;
    boolean mScanPending;
    final WifiSettingsStore mSettingsStore;
    private WifiTrafficPoller mTrafficPoller;
    private final UserManager mUserManager;
    private HwWifiCHRStateManager mWiFiCHRManager;
    private WifiController mWifiController;
    private final WifiInjector mWifiInjector;
    private final WifiMetrics mWifiMetrics;
    WifiServiceHisiExt mWifiServiceHisiExt;
    private HwWifiStatStore mWifiStatStore;
    final WifiStateMachine mWifiStateMachine;
    private AsyncChannel mWifiStateMachineChannel;
    WifiStateMachineHandler mWifiStateMachineHandler;
    private int scanRequestCounter;
    private DataUploader uploader;

    /* renamed from: com.android.server.wifi.WifiServiceImpl.11 */
    class AnonymousClass11 extends ContentObserver {
        AnonymousClass11(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            WifiServiceImpl.this.mSettingsStore.handleWifiScanAlwaysAvailableToggled();
            WifiServiceImpl.this.mWifiController.sendMessage(155655);
        }
    }

    /* renamed from: com.android.server.wifi.WifiServiceImpl.7 */
    class AnonymousClass7 implements OnClickListener {
        final /* synthetic */ boolean val$enabled;
        final /* synthetic */ WifiConfiguration val$wifiConfig;

        AnonymousClass7(boolean val$enabled, WifiConfiguration val$wifiConfig) {
            this.val$enabled = val$enabled;
            this.val$wifiConfig = val$wifiConfig;
        }

        public void onClick(DialogInterface dialog, int which) {
            WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
            Slog.d(WifiServiceImpl.TAG, "PositiveButton is click");
            if (WifiServiceImpl.this.mWifiServiceHisiExt.isAirplaneModeOn()) {
                Slog.d(WifiServiceImpl.TAG, "Cann't start AP with airPlaneMode on");
                return;
            }
            WifiServiceImpl.this.mHiSiWifiComm.changeShowDialogFlag("show_ap_dialog_flag", WifiServiceImpl.this.mIsApDialogNeedShow);
            WifiServiceImpl.this.setHisiWifiApEnabled(this.val$enabled, this.val$wifiConfig, WifiServiceImpl.this.mWifiController);
        }
    }

    private class ClientHandler extends Handler {
        ClientHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    if (msg.arg1 == 0) {
                        Slog.d(WifiServiceImpl.TAG, "New client listening to asynchronous messages");
                        WifiServiceImpl.this.mTrafficPoller.addClient(msg.replyTo);
                        return;
                    }
                    Slog.e(WifiServiceImpl.TAG, "Client connection failure, error=" + msg.arg1);
                case 69633:
                    new AsyncChannel().connect(WifiServiceImpl.this.mContext, this, msg.replyTo);
                case 69636:
                    if (msg.arg1 == 2) {
                        Slog.d(WifiServiceImpl.TAG, "Send failed, client connection lost");
                    } else {
                        Slog.d(WifiServiceImpl.TAG, "Client connection lost with reason: " + msg.arg1);
                    }
                    WifiServiceImpl.this.mTrafficPoller.removeClient(msg.replyTo);
                case 151553:
                case 151559:
                    WifiConfiguration config = msg.obj;
                    int networkId = msg.arg1;
                    if (msg.what == 151559) {
                        Slog.d("WiFiServiceImpl ", "SAVE nid=" + Integer.toString(networkId) + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                    }
                    if (msg.what == 151553) {
                        if (config != null) {
                            WifiServiceImpl.this.mWiFiCHRManager.checkAppName(config, WifiServiceImpl.this.mContext);
                        }
                        Slog.d("WiFiServiceImpl ", "CONNECT  nid=" + Integer.toString(networkId) + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        WifiServiceImpl.this.mWifiStateMachine.setCHRConnectingSartTimestamp(SystemClock.elapsedRealtime());
                        int callingPid = SystemProperties.getInt("hw.wifichr.connect_thread_pid", 0);
                        if (WifiServiceImpl.this.mWiFiCHRManager != null) {
                            WifiServiceImpl.this.mWiFiCHRManager.updateConnectThreadName(WifiServiceImpl.this.getAppName(callingPid));
                        }
                    }
                    if (config != null && WifiServiceImpl.isValid(config)) {
                        Slog.d(WifiServiceImpl.TAG, "Connect with config" + config);
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                    } else if (config != null || networkId == -1) {
                        Slog.e(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring invalid msg=" + msg);
                        if (msg.what == 151553) {
                            replyFailed(msg, 151554, 8);
                        } else {
                            replyFailed(msg, 151560, 8);
                        }
                    } else {
                        Slog.d(WifiServiceImpl.TAG, "Connect with networkId" + networkId);
                        if (WifiServiceImpl.this.mWiFiCHRManager != null) {
                            WifiServiceImpl.this.mWiFiCHRManager.setLastNetIdFromUI(networkId);
                        }
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                    }
                case 151556:
                    WifiServiceImpl.this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
                    if (WifiServiceImpl.this.mHwWifiCHRService != null) {
                        WifiServiceImpl.this.mHwWifiCHRService.forgetFromUser(msg.arg1);
                    }
                    if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue()) {
                        WifiServiceImpl.this.handleForgetNetwork(Message.obtain(msg));
                    } else {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                    }
                case 151562:
                case 151566:
                case 151569:
                case 151572:
                case 151575:
                    WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                default:
                    Slog.d(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring msg=" + msg);
            }
        }

        private void replyFailed(Message msg, int what, int why) {
            Message reply = Message.obtain();
            reply.what = what;
            reply.arg1 = why;
            try {
                msg.replyTo.send(reply);
            } catch (RemoteException e) {
            }
        }
    }

    private abstract class DeathRecipient implements android.os.IBinder.DeathRecipient {
        IBinder mBinder;
        String mTag;
        int mUid;

        DeathRecipient(String tag, IBinder binder) {
            this.mTag = tag;
            this.mUid = Binder.getCallingUid();
            this.mBinder = binder;
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        void unlinkDeathRecipient() {
            this.mBinder.unlinkToDeath(this, 0);
        }

        public int getUid() {
            return this.mUid;
        }
    }

    public class LockList {
        private List<WifiLock> mList;

        private LockList() {
            this.mList = new ArrayList();
        }

        synchronized boolean hasLocks() {
            return this.mList.isEmpty() ? false : WifiServiceImpl.DBG;
        }

        synchronized int getStrongestLockMode() {
            if (this.mList.isEmpty()) {
                return 1;
            }
            if (WifiServiceImpl.this.mFullHighPerfLocksAcquired > WifiServiceImpl.this.mFullHighPerfLocksReleased) {
                return 3;
            }
            if (WifiServiceImpl.this.mFullLocksAcquired > WifiServiceImpl.this.mFullLocksReleased) {
                return 1;
            }
            return 2;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        synchronized void updateWorkSource(WorkSource ws) {
            int i = 0;
            while (true) {
                if (i < WifiServiceImpl.this.mLocks.mList.size()) {
                    ws.add(((WifiLock) WifiServiceImpl.this.mLocks.mList.get(i)).mWorkSource);
                    i++;
                }
            }
        }

        synchronized boolean clearLocks() {
            String EXCEPT = "WiFiDirectFT";
            List<WifiLock> copyList = new ArrayList();
            copyList.addAll(this.mList);
            for (WifiLock l : copyList) {
                if (EXCEPT.equals(l.mTag)) {
                    Slog.d(WifiServiceImpl.TAG, "don't release module: " + EXCEPT);
                } else if (!WifiServiceImpl.this.releaseWifiLock(l.mBinder)) {
                    Slog.d(WifiServiceImpl.TAG, "releaseWifiLock failed , don't send CMD_LACKS_CHANGED");
                }
            }
            return WifiServiceImpl.DBG;
        }

        private void addLock(WifiLock lock) {
            if (findLockByBinder(lock.mBinder) < 0) {
                this.mList.add(lock);
            }
        }

        private WifiLock removeLock(IBinder binder) {
            int index = findLockByBinder(binder);
            if (index < 0) {
                return null;
            }
            WifiLock ret = (WifiLock) this.mList.remove(index);
            ret.unlinkDeathRecipient();
            return ret;
        }

        private int findLockByBinder(IBinder binder) {
            for (int i = this.mList.size() - 1; i >= 0; i--) {
                if (((WifiLock) this.mList.get(i)).mBinder == binder) {
                    return i;
                }
            }
            return -1;
        }

        private void dump(PrintWriter pw) {
            for (WifiLock l : this.mList) {
                pw.print("    ");
                pw.println(l);
            }
        }
    }

    private class Multicaster extends DeathRecipient {
        Multicaster(String tag, IBinder binder) {
            super(tag, binder);
        }

        public void binderDied() {
            Slog.e(WifiServiceImpl.TAG, "Multicaster binderDied");
            synchronized (WifiServiceImpl.this.mMulticasters) {
                int i = WifiServiceImpl.this.mMulticasters.indexOf(this);
                if (i != -1) {
                    WifiServiceImpl.this.removeMulticasterLocked(i, this.mUid);
                }
            }
        }

        public String toString() {
            return "Multicaster{" + this.mTag + " uid=" + this.mUid + "}";
        }
    }

    class TdlsTask extends AsyncTask<TdlsTaskParams, Integer, Integer> {
        TdlsTask() {
        }

        protected Integer doInBackground(TdlsTaskParams... params) {
            Throwable th;
            TdlsTaskParams param = params[0];
            String remoteIpAddress = param.remoteIpAddress.trim();
            boolean enable = param.enable;
            String macAddress = null;
            BufferedReader bufferedReader = null;
            try {
                BufferedReader reader = new BufferedReader(new FileReader("/proc/net/arp"));
                try {
                    String mac;
                    String readLine = reader.readLine();
                    while (true) {
                        readLine = reader.readLine();
                        if (readLine == null) {
                            break;
                        }
                        String[] tokens = readLine.split("[ ]+");
                        if (tokens.length >= 6) {
                            String ip = tokens[0];
                            mac = tokens[3];
                            if (remoteIpAddress.equals(ip)) {
                                break;
                            }
                        }
                    }
                    macAddress = mac;
                    if (macAddress == null) {
                        Slog.w(WifiServiceImpl.TAG, "Did not find remoteAddress {" + remoteIpAddress + "} in " + "/proc/net/arp");
                    } else {
                        WifiServiceImpl.this.enableTdlsWithMacAddress(macAddress, enable);
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                        }
                    }
                    bufferedReader = reader;
                } catch (FileNotFoundException e2) {
                    bufferedReader = reader;
                    try {
                        Slog.e(WifiServiceImpl.TAG, "Could not open /proc/net/arp to lookup mac address");
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                            }
                        }
                        return Integer.valueOf(0);
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    bufferedReader = reader;
                    Slog.e(WifiServiceImpl.TAG, "Could not read /proc/net/arp to lookup mac address");
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e6) {
                        }
                    }
                    return Integer.valueOf(0);
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = reader;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                Slog.e(WifiServiceImpl.TAG, "Could not open /proc/net/arp to lookup mac address");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                return Integer.valueOf(0);
            } catch (IOException e8) {
                Slog.e(WifiServiceImpl.TAG, "Could not read /proc/net/arp to lookup mac address");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                return Integer.valueOf(0);
            }
            return Integer.valueOf(0);
        }
    }

    class TdlsTaskParams {
        public boolean enable;
        public String remoteIpAddress;

        TdlsTaskParams() {
        }
    }

    private class WifiLock extends DeathRecipient {
        int mMode;
        WorkSource mWorkSource;

        WifiLock(int lockMode, String tag, IBinder binder, WorkSource ws) {
            super(tag, binder);
            this.mMode = lockMode;
            this.mWorkSource = ws;
        }

        public void binderDied() {
            synchronized (WifiServiceImpl.this.mLocks) {
                WifiServiceImpl.this.releaseWifiLockLocked(this.mBinder);
            }
        }

        public String toString() {
            return "WifiLock{" + this.mTag + " type=" + this.mMode + " uid=" + this.mUid + "}";
        }
    }

    private class WifiStateMachineHandler extends Handler {
        private AsyncChannel mWsmChannel;

        WifiStateMachineHandler(Looper looper) {
            super(looper);
            this.mWsmChannel = new AsyncChannel();
            this.mWsmChannel.connect(WifiServiceImpl.this.mContext, this, WifiServiceImpl.this.mWifiStateMachine.getHandler());
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    if (msg.arg1 == 0) {
                        WifiServiceImpl.this.mWifiStateMachineChannel = this.mWsmChannel;
                        return;
                    }
                    Slog.e(WifiServiceImpl.TAG, "WifiStateMachine connection failure, error=" + msg.arg1);
                    WifiServiceImpl.this.mWifiStateMachineChannel = null;
                case 69636:
                    Slog.e(WifiServiceImpl.TAG, "WifiStateMachine channel lost, msg.arg1 =" + msg.arg1);
                    WifiServiceImpl.this.mWifiStateMachineChannel = null;
                    this.mWsmChannel.connect(WifiServiceImpl.this.mContext, this, WifiServiceImpl.this.mWifiStateMachine.getHandler());
                case WifiStateMachine.CMD_CHANGE_TO_STA_P2P_CONNECT /*131573*/:
                    Slog.e(WifiServiceImpl.TAG, "handleMessage CMD_CHANGE_TO_STA_P2P_CONNECT");
                    WifiServiceImpl.this.showP2pToStaDialog();
                case WifiStateMachine.CMD_CHANGE_TO_AP_P2P_CONNECT /*131574*/:
                    Slog.e(WifiServiceImpl.TAG, "handleMessage CMD_CHANGE_TO_STA_AP_CONNECT");
                    Bundle data = msg.getData();
                    WifiConfiguration wifiConfig = (WifiConfiguration) data.getParcelable("wifiConfig");
                    WifiServiceImpl.this.showP2pToAPDialog(wifiConfig, data.getBoolean("isWifiApEnabled"));
                default:
                    Slog.d(WifiServiceImpl.TAG, "WifiStateMachineHandler.handleMessage ignoring msg=" + msg);
            }
        }
    }

    public WifiServiceImpl(Context context) {
        this.mWifiServiceHisiExt = null;
        this.mLocks = new LockList();
        this.mMulticasters = new ArrayList();
        this.scanRequestCounter = 0;
        this.mIsP2pCloseDialogExist = false;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int i = 1;
                if (intent != null) {
                    String action = intent.getAction();
                    if (action != null) {
                        Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action);
                        if (action.equals("android.intent.action.SCREEN_ON")) {
                            WifiServiceImpl.this.mWifiController.sendMessage(155650);
                        } else if (action.equals("android.intent.action.USER_PRESENT")) {
                            WifiServiceImpl.this.mWifiController.sendMessage(155660);
                        } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                            WifiServiceImpl.this.mWifiController.sendMessage(155651);
                        } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                            int pluggedType = intent.getIntExtra("plugged", 0);
                            Slog.d(WifiServiceImpl.TAG, "ACTION_BATTERY_CHANGED pluggedType: " + pluggedType);
                            WifiServiceImpl.this.mWifiController.sendMessage(155652, pluggedType, 0, null);
                        } else if (action.equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
                            WifiServiceImpl.this.mWifiStateMachine.sendBluetoothAdapterStateChange(intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", 0));
                        } else if (action.equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                            boolean emergencyMode = intent.getBooleanExtra("phoneinECMState", false);
                            r8 = WifiServiceImpl.this.mWifiController;
                            if (!emergencyMode) {
                                i = 0;
                            }
                            r8.sendMessage(155649, i, 0);
                        } else if (action.equals("android.intent.action.EMERGENCY_CALL_STATE_CHANGED")) {
                            boolean inCall = intent.getBooleanExtra("phoneInEmergencyCall", false);
                            r8 = WifiServiceImpl.this.mWifiController;
                            if (!inCall) {
                                i = 0;
                            }
                            r8.sendMessage(155662, i, 0);
                        } else if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                            WifiServiceImpl.this.handleIdleModeChanged();
                        } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                            int wifiApState = intent.getIntExtra("wifi_state", 14);
                            Slog.d(WifiServiceImpl.TAG, "wifiApState=" + wifiApState);
                            if (wifiApState == 14) {
                                WifiServiceImpl.this.setWifiApEnabled(null, false);
                            }
                        } else {
                            WifiServiceImpl.this.onReceiveEx(context, intent);
                        }
                    }
                }
            }
        };
        this.mPackageOrUserReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action);
                String action2 = intent.getAction();
                if (action2.equals("android.intent.action.PACKAGE_REMOVED")) {
                    if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                        int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                        Uri uri = intent.getData();
                        if (uid != -1 && uri != null) {
                            WifiServiceImpl.this.mWifiStateMachine.removeAppConfigs(uri.getSchemeSpecificPart(), uid);
                        }
                    }
                } else if (action2.equals("android.intent.action.USER_REMOVED")) {
                    WifiServiceImpl.this.mWifiStateMachine.removeUserConfigs(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else {
                    Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action + " no handle");
                }
            }
        };
        this.mContext = context;
        this.mWifiInjector = WifiInjector.getInstance();
        this.mFacade = new FrameworkFacade();
        HandlerThread wifiThread = new HandlerThread(TAG);
        wifiThread.start();
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mTrafficPoller = new WifiTrafficPoller(this.mContext, wifiThread.getLooper(), WifiNative.getWlanNativeInterface().getInterfaceName());
        this.mUserManager = UserManager.get(this.mContext);
        HandlerThread wifiStateMachineThread = new HandlerThread("WifiStateMachine");
        wifiStateMachineThread.start();
        this.mCountryCode = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiCountryCode(this.mContext, WifiNative.getWlanNativeInterface(), SystemProperties.get(BOOT_DEFAULT_WIFI_COUNTRY_CODE), this.mFacade.getStringSetting(this.mContext, "wifi_country_code"), this.mContext.getResources().getBoolean(17956891));
        HwWifiServiceFactory.initWifiCHRService(this.mContext);
        this.mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
        this.mWifiStatStore = HwWifiServiceFactory.getHwWifiStatStore();
        this.mWifiStateMachine = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiStateMachine(this.mContext, this.mFacade, wifiStateMachineThread.getLooper(), this.mUserManager, this.mWifiInjector, new BackupManagerProxy(), this.mCountryCode);
        this.mSettingsStore = new WifiSettingsStore(this.mContext);
        this.mWifiStateMachine.enableRssiPolling(DBG);
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            this.mWifiServiceHisiExt = new WifiServiceHisiExt(this.mContext);
            this.mHiSiWifiComm = new HiSiWifiComm(this.mContext);
            this.mWifiServiceHisiExt.mWifiStateMachineHisiExt = this.mWifiStateMachine.mWifiStateMachineHisiExt;
        }
        this.mBatteryStats = BatteryStatsService.getService();
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mCertManager = new WifiCertManager(this.mContext);
        this.mNotificationController = new WifiNotificationController(this.mContext, wifiThread.getLooper(), this.mWifiStateMachine, this.mFacade, null);
        this.mWifiStateMachine.setmSettingsStore(this.mSettingsStore);
        this.mClientHandler = new ClientHandler(wifiThread.getLooper());
        this.mWifiStateMachineHandler = new WifiStateMachineHandler(wifiThread.getLooper());
        this.mWifiController = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiController(this.mContext, this.mWifiStateMachine, this.mSettingsStore, this.mLocks, wifiThread.getLooper(), this.mFacade);
        this.uploader = DataUploader.getInstance();
        this.uploader.setContext(this.mContext);
    }

    public void checkAndStartWifi() {
        boolean wifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        Slog.i(TAG, "WifiService starting up with Wi-Fi " + (wifiEnabled ? "enabled" : "disabled"));
        this.mWifiStateMachine.setWifiRepeaterStoped();
        registerForScanModeChange();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WifiServiceImpl.this.mSettingsStore.handleAirplaneModeToggled()) {
                    if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue()) {
                        WifiServiceImpl.this.handleAirplaneModeToggled();
                    } else {
                        WifiServiceImpl.this.mWifiController.sendMessage(155657);
                    }
                }
                if (WifiServiceImpl.this.mSettingsStore.isAirplaneModeOn()) {
                    Log.d(WifiServiceImpl.TAG, "resetting country code because Airplane mode is ON");
                    WifiServiceImpl.this.mCountryCode.airplaneModeEnabled();
                }
            }
        }, new IntentFilter("android.intent.action.AIRPLANE_MODE"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("ABSENT".equals(intent.getStringExtra("ss"))) {
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM was removed");
                    WifiServiceImpl.this.mWifiStateMachine.resetSimAuthNetworks();
                    Log.d(WifiServiceImpl.TAG, "resetting country code because SIM is removed");
                    WifiServiceImpl.this.mCountryCode.simCardRemoved();
                }
            }
        }, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        if (!WifiServiceHisiExt.hisiWifiEnabled()) {
            registerForBroadcasts();
            registerForPackageOrUserRemoval();
        } else if (!this.mWifiServiceHisiExt.mIsReceiverRegistered) {
            registerForBroadcasts();
            registerForPackageOrUserRemoval();
            this.mWifiServiceHisiExt.mIsReceiverRegistered = DBG;
        }
        this.mInIdleMode = this.mPowerManager.isDeviceIdleMode();
        this.mWifiController.start();
        HwWifiServiceFactory.getHwWifiServiceManager().createHwArpVerifier(this.mContext);
        if (wifiEnabled) {
            setWifiEnabled(wifiEnabled);
        }
        this.mWifiStateMachine.setLocalMacAddressFromMacfile();
        this.mWifiController.setupHwSelfCureEngine(this.mContext, this.mWifiStateMachine);
        if (WifiProStatusUtils.isWifiProEnabledViaProperties()) {
            this.mWifiController.createWifiProStateMachine(this.mContext, this.mWifiStateMachine.getMessenger());
        }
        this.mWifiController.createABSService(this.mContext, this.mWifiStateMachine);
    }

    public void handleUserSwitch(int userId) {
        this.mWifiStateMachine.handleUserSwitch(userId);
    }

    public boolean pingSupplicant() {
        enforceAccessPermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncPingSupplicant(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public void startScan(ScanSettings settings, WorkSource workSource) {
        String appName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Slog.d(TAG, "startScan, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        if (this.mPowerManager.isScreenOn() || "com.huawei.ca".equals(appName) || "com.huawei.parentcontrol".equals(appName) || Binder.getCallingUid() == 1000) {
            this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
            if (this.mHwWifiCHRService != null) {
                this.mHwWifiCHRService.updateApkChangewWifiStatus(4, appName);
            }
            enforceChangePermission();
            synchronized (this) {
                if (this.mInIdleMode) {
                    long callingIdentity = Binder.clearCallingIdentity();
                    try {
                        this.mWifiStateMachine.sendScanResultsAvailableBroadcast(false);
                        Binder.restoreCallingIdentity(callingIdentity);
                        this.mScanPending = DBG;
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(callingIdentity);
                    }
                } else {
                    if (settings != null) {
                        ScanSettings settings2 = new ScanSettings(settings);
                        if (settings2.isValid()) {
                            settings = settings2;
                        } else {
                            Slog.e(TAG, "invalid scan setting");
                            return;
                        }
                    }
                    if (workSource != null) {
                        enforceWorkSourcePermission();
                        workSource.clearNames();
                    }
                    if (workSource == null && Binder.getCallingUid() >= 0) {
                        workSource = new WorkSource(Binder.getCallingUid());
                    }
                    int uid = Binder.getCallingUid();
                    boolean canReadPeerMacAddresses = checkPeersMacAddress();
                    boolean isActiveNetworkScorer = NetworkScorerAppManager.isCallerActiveScorer(this.mContext, uid);
                    try {
                        String callingPackageName = getPackageName(Binder.getCallingPid());
                        if (!canReadPeerMacAddresses && !isActiveNetworkScorer && !isLocationEnabled()) {
                            Slog.i(TAG, callingPackageName + " cannot get scan results:Location Service is disable.");
                        } else if (canReadPeerMacAddresses || isActiveNetworkScorer || checkCallerCanAccessScanResults(callingPackageName, uid)) {
                            if (this.mAppOps.noteOp(10, uid, callingPackageName) != 0) {
                                Slog.i(TAG, callingPackageName + " cannot get scan results.");
                                return;
                            }
                            WifiStateMachine wifiStateMachine = this.mWifiStateMachine;
                            int callingUid = Binder.getCallingUid();
                            int i = this.scanRequestCounter;
                            this.scanRequestCounter = i + 1;
                            wifiStateMachine.startScan(callingUid, i, settings, workSource);
                        } else {
                            Slog.i(TAG, callingPackageName + " cannot get scan results:caller cannot access scan results.");
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "error happened when start scan.", e);
                    }
                }
            }
        } else {
            Slog.i(TAG, "Screen is off, " + appName + " startScan is skipped.");
        }
    }

    public String getWpsNfcConfigurationToken(int netId) {
        enforceConnectivityInternalPermission();
        return this.mWifiStateMachine.syncGetWpsNfcConfigurationToken(netId);
    }

    void handleIdleModeChanged() {
        boolean doScan = false;
        synchronized (this) {
            boolean idle = this.mPowerManager.isDeviceIdleMode();
            if (this.mInIdleMode != idle) {
                this.mInIdleMode = idle;
                if (!idle && this.mScanPending) {
                    this.mScanPending = false;
                    doScan = DBG;
                }
            }
        }
        if (doScan) {
            startScan(null, null);
        }
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", TAG);
    }

    private void enforceChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
    }

    private void enforceLocationHardwarePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.LOCATION_HARDWARE", "LocationHardware");
    }

    private void enforceReadCredentialPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_WIFI_CREDENTIAL", TAG);
    }

    private void enforceWorkSourcePermission() {
        this.mContext.enforceCallingPermission("android.permission.UPDATE_DEVICE_STATS", TAG);
    }

    private void enforceMulticastChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_MULTICAST_STATE", TAG);
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "ConnectivityService");
    }

    protected String getAppName(int pID) {
        String processName = "";
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pID) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private void setHisiWifiApEnabled(boolean enabled, WifiConfiguration wifiConfig, WifiController mWifiController) {
        this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.setWifiApEnabled(enabled);
        if (wifiConfig == null || isValid(wifiConfig)) {
            int i;
            if (enabled) {
                i = 1;
            } else {
                i = 0;
            }
            mWifiController.obtainMessage(155658, i, 0, wifiConfig).sendToTarget();
            return;
        }
        Slog.e(TAG, "Invalid WifiConfiguration");
    }

    private void showP2pToAPDialog(WifiConfiguration wifiConfig, boolean enabled) {
        if (this.mIsP2pCloseDialogExist) {
            Slog.d(TAG, "the dialog already exist don't show dialog again");
            return;
        }
        Slog.d(TAG, "showP2pToAPDialog enter");
        Resources r = Resources.getSystem();
        CheckBox checkBox = new CheckBox(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)));
        checkBox.setChecked(false);
        checkBox.setText(r.getString(33685808));
        checkBox.setTextSize(14.0f);
        checkBox.setTextColor(-16777216);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WifiServiceImpl.this.mIsApDialogNeedShow = isChecked;
            }
        });
        AlertDialog dialog = new Builder(this.mContext, 33947691).setCancelable(false).setTitle(r.getString(33685806)).setMessage(r.getString(33685807)).setView(checkBox).setNegativeButton(r.getString(17039360), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
                Slog.d(WifiServiceImpl.TAG, "NegativeButton is click");
                WifiServiceImpl.this.setWifiApStateByManual(false);
            }
        }).setPositiveButton(r.getString(17039370), new AnonymousClass7(enabled, wifiConfig)).create();
        dialog.getWindow().setType(2014);
        dialog.show();
        this.mIsP2pCloseDialogExist = DBG;
        Slog.d(TAG, "dialog showed");
    }

    private void showP2pToStaDialog() {
        if (this.mIsP2pCloseDialogExist) {
            Slog.d(TAG, "the dialog already exist don't show dialog again");
            return;
        }
        Slog.d(TAG, "showP2pToStaDialog enter");
        Resources r = Resources.getSystem();
        CheckBox checkBox = new CheckBox(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)));
        checkBox.setChecked(false);
        checkBox.setText(r.getString(33685808));
        checkBox.setTextSize(14.0f);
        checkBox.setTextColor(-16777216);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WifiServiceImpl.this.mIsDialogNeedShow = isChecked;
            }
        });
        AlertDialog dialog = new Builder(this.mContext, 33947691).setCancelable(false).setTitle(r.getString(33685804)).setMessage(r.getString(33685805)).setView(checkBox).setNegativeButton(r.getString(17039360), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
                Slog.d(WifiServiceImpl.TAG, "NegativeButton is click");
                WifiServiceImpl.this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.sendWifiStateDisabledBroadcast();
            }
        }).setPositiveButton(r.getString(17039370), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiServiceImpl.this.mIsP2pCloseDialogExist = false;
                Slog.d(WifiServiceImpl.TAG, "PositiveButton is click");
                if (WifiServiceImpl.this.mWifiServiceHisiExt.isWifiP2pEnabled() || !(1 == WifiServiceImpl.this.getWifiEnabledState() || WifiServiceImpl.this.getWifiEnabledState() == 0)) {
                    WifiServiceImpl.this.mHiSiWifiComm.changeShowDialogFlag("show_sta_dialog_flag", WifiServiceImpl.this.mIsDialogNeedShow);
                    WifiServiceImpl.this.setWifiStateByManual(WifiServiceImpl.DBG);
                    WifiServiceImpl.this.mWifiServiceHisiExt.setWifiP2pEnabled(3);
                    return;
                }
                Slog.d(WifiServiceImpl.TAG, "supplicant is closed ,enble wifi with start supplicant");
                WifiServiceImpl.this.setWifiEnabled(WifiServiceImpl.DBG);
            }
        }).create();
        dialog.getWindow().setType(2014);
        dialog.show();
        this.mIsP2pCloseDialogExist = DBG;
        Slog.d(TAG, "dialog showed");
    }

    public synchronized boolean setWifiEnabled(boolean enable) {
        String appName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        enforceChangePermission();
        if (HwDeviceManager.disallowOp(0)) {
            Slog.i(TAG, "Wifi has been restricted by MDM apk.");
            return false;
        }
        Slog.d(TAG, "setWifiEnabled: " + enable + " pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + appName);
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        if (!(this.mHwWifiCHRService == null || enable)) {
            this.mHwWifiCHRService.updateApkChangewWifiStatus(1, appName);
        }
        if (this.mContext == null || HwSystemManager.allowOp(this.mContext, 2097152, enable)) {
            if (WifiServiceHisiExt.hisiWifiEnabled()) {
                Slog.d(TAG, "setWifiEnabled, P2P enable flag is " + this.mWifiServiceHisiExt.isWifiP2pEnabled());
                SystemProperties.set("sys.open_wifi_pid", Integer.toString(Binder.getCallingPid()));
                if (this.mWifiServiceHisiExt.isWifiP2pEnabled() && enable) {
                    int flag = this.mHiSiWifiComm.getSettingsGlobalIntValue("show_sta_dialog_flag");
                    if (!this.mWifiServiceHisiExt.checkUseNotCoexistPermission()) {
                        Slog.d(TAG, "the software have no some important permissions,return false.");
                        return false;
                    } else if (this.mHiSiWifiComm.isP2pConnect() && flag != 1) {
                        Slog.e(TAG, "sendEmptyMessage CMD_CHANGE_TO_STA_P2P_CONNECT");
                        this.mWifiStateMachineHandler.sendEmptyMessage(WifiStateMachine.CMD_CHANGE_TO_STA_P2P_CONNECT);
                    } else if (this.mIsP2pCloseDialogExist) {
                        Slog.d(TAG, "the p2p to AP dialog already exist cann't open wifi");
                        return false;
                    } else {
                        setWifiStateByManual(DBG);
                        this.mWifiServiceHisiExt.setWifiP2pEnabled(3);
                    }
                } else if (this.mWifiServiceHisiExt.isWifiP2pEnabled() && !enable) {
                    if (this.mWifiServiceHisiExt.checkUseNotCoexistPermission()) {
                        this.mWifiServiceHisiExt.setWifiP2pEnabled(3);
                    } else {
                        Slog.d(TAG, "the software have no some important permissions.");
                        return false;
                    }
                }
            }
            if (Binder.getCallingUid() == 0 || !"factory".equals(SystemProperties.get("ro.runmode", "normal")) || SystemProperties.getInt("wlan.wltest.status", 0) <= 0) {
                long ident = Binder.clearCallingIdentity();
                try {
                    if (this.mSettingsStore.handleWifiToggled(enable)) {
                        Binder.restoreCallingIdentity(ident);
                        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
                        if (this.mHwWifiCHRService != null) {
                            this.mHwWifiCHRService.updateWifiTriggerState(enable);
                        }
                        if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue()) {
                            setWifiEnabledAfterVoWifiOff(enable);
                        } else {
                            this.mWifiController.sendMessage(155656);
                        }
                        return DBG;
                    }
                    Slog.d(TAG, "setWifiEnabled,Nothing to do if wifi cannot be toggled.");
                    if (enable) {
                        this.uploader.e(52, "{ACT:1,STATUS:failed,DETAIL:cannot be toggled}");
                    } else {
                        this.uploader.e(52, "{ACT:0,STATUS:failed,DETAIL:cannot be toggled}");
                    }
                    return DBG;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                Slog.e(TAG, "in wltest mode, dont allow to enable WiFi");
                return false;
            }
        }
        if (enable) {
            this.uploader.e(52, "{ACT:1,STATUS:failed,DETAIL:permission deny}");
        } else {
            this.uploader.e(52, "{ACT:0,STATUS:failed,DETAIL:permission deny}");
        }
        return false;
    }

    public void setWifiStateByManual(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            Slog.d(TAG, "setWifiStateByManual:" + enable + ",mIsReceiverRegistered:" + this.mWifiServiceHisiExt.mIsReceiverRegistered);
            if (enable) {
                if (!this.mWifiServiceHisiExt.mIsReceiverRegistered) {
                    registerForBroadcasts();
                    registerForPackageOrUserRemoval();
                    this.mWifiServiceHisiExt.mIsReceiverRegistered = DBG;
                }
            } else if (this.mWifiServiceHisiExt.mIsReceiverRegistered) {
                this.mContext.unregisterReceiver(this.mReceiver);
                this.mContext.unregisterReceiver(this.mPackageOrUserReceiver);
                this.mWifiServiceHisiExt.mIsReceiverRegistered = false;
            }
            this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.setWifiStateByManual(enable);
        }
    }

    public void setWifiApStateByManual(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.setWifiApStateByManual(enable);
        }
    }

    public void setWifiEnableForP2p(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            this.mWifiServiceHisiExt.mWifiStateMachineHisiExt.setWifiEnableForP2p(enable);
        }
    }

    public int getWifiEnabledState() {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncGetWifiState();
    }

    public void setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        int i = 1;
        enforceChangePermission();
        Slog.d(TAG, "setWifiApEnabled: " + enabled + " pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
        if (this.mUserManager.hasUserRestriction("no_config_tethering")) {
            throw new SecurityException("DISALLOW_CONFIG_TETHERING is enabled for this user.");
        }
        if (WifiServiceHisiExt.hisiWifiEnabled()) {
            int flag = this.mHiSiWifiComm.getSettingsGlobalIntValue("show_ap_dialog_flag");
            if (enabled) {
                SystemProperties.set("sys.ap_open_flag", "1");
                Slog.d(TAG, "sys.ap_open_flag is on");
            } else {
                SystemProperties.set("sys.ap_open_flag", HwWifiCHRStateManager.TYPE_AP_VENDOR);
                Slog.d(TAG, "sys.ap_open_flag is off");
            }
            if (this.mWifiServiceHisiExt.isWifiP2pEnabled() && !this.mWifiServiceHisiExt.checkUseNotCoexistPermission()) {
                Slog.d(TAG, "the software have no some important permissions,start ap failed.");
            } else if (this.mHiSiWifiComm.isP2pConnect() && flag != 1) {
                Slog.e(TAG, "sendEmptyMessage CMD_CHANGE_TO_AP_P2P_CONNECT");
                Bundle data = new Bundle();
                data.putBoolean("isWifiApEnabled", enabled);
                data.putParcelable("wifiConfig", wifiConfig);
                Message msg = new Message();
                msg.what = WifiStateMachine.CMD_CHANGE_TO_AP_P2P_CONNECT;
                msg.setData(data);
                this.mWifiStateMachineHandler.sendMessage(msg);
            } else if (this.mIsP2pCloseDialogExist) {
                Slog.d(TAG, "the p2p to STA dialog already exist cann't open AP");
            } else {
                setHisiWifiApEnabled(enabled, wifiConfig, this.mWifiController);
            }
        } else if (wifiConfig == null || isValid(wifiConfig)) {
            WifiController wifiController = this.mWifiController;
            if (!enabled) {
                i = 0;
            }
            wifiController.obtainMessage(155658, i, 0, wifiConfig).sendToTarget();
        } else {
            Slog.e(TAG, "Invalid WifiConfiguration");
        }
    }

    public int getWifiApEnabledState() {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncGetWifiApState();
    }

    public WifiConfiguration getWifiApConfiguration() {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncGetWifiApConfiguration();
    }

    public WifiConfiguration buildWifiConfig(String uriString, String mimeType, byte[] data) {
        if (mimeType.equals(ConfigBuilder.WifiConfigType)) {
            try {
                return ConfigBuilder.buildConfig(uriString, data, this.mContext);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse wi-fi configuration: " + e);
            }
        } else {
            Log.i(TAG, "Unknown wi-fi config type: " + mimeType);
            return null;
        }
    }

    public void setWifiApConfiguration(WifiConfiguration wifiConfig) {
        enforceChangePermission();
        if (wifiConfig != null) {
            if (isValid(wifiConfig)) {
                this.mWifiStateMachine.setWifiApConfiguration(wifiConfig);
            } else {
                Slog.e(TAG, "Invalid WifiConfiguration");
            }
        }
    }

    public boolean isScanAlwaysAvailable() {
        enforceAccessPermission();
        return this.mSettingsStore.isScanAlwaysAvailable();
    }

    public void disconnect() {
        Slog.d(TAG, "disconnect:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        this.mWifiStateMachine.disconnectCommand();
    }

    public void reconnect() {
        Slog.d(TAG, "reconnect:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        this.mWifiStateMachine.reconnectCommand();
    }

    public void reassociate() {
        Slog.d(TAG, "reassociate:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        this.mWifiStateMachine.reassociateCommand();
    }

    public int getSupportedFeatures() {
        enforceAccessPermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetSupportedFeatures(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return 0;
    }

    public void requestActivityInfo(ResultReceiver result) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("controller_activity", reportActivityInfo());
        result.send(0, bundle);
    }

    public WifiActivityEnergyInfo reportActivityInfo() {
        enforceAccessPermission();
        if ((getSupportedFeatures() & 65536) == 0) {
            return null;
        }
        WifiActivityEnergyInfo wifiActivityEnergyInfo = null;
        if (this.mWifiStateMachineChannel != null) {
            WifiLinkLayerStats stats = this.mWifiStateMachine.syncGetLinkLayerStats(this.mWifiStateMachineChannel);
            if (stats != null) {
                long[] txTimePerLevel;
                long rxIdleCurrent = (long) this.mContext.getResources().getInteger(17694785);
                long rxCurrent = (long) this.mContext.getResources().getInteger(17694786);
                long txCurrent = (long) this.mContext.getResources().getInteger(17694787);
                double voltage = ((double) this.mContext.getResources().getInteger(17694788)) / 1000.0d;
                long rxIdleTime = (long) ((stats.on_time - stats.tx_time) - stats.rx_time);
                if (stats.tx_time_per_level != null) {
                    txTimePerLevel = new long[stats.tx_time_per_level.length];
                    for (int i = 0; i < txTimePerLevel.length; i++) {
                        txTimePerLevel[i] = (long) stats.tx_time_per_level[i];
                    }
                } else {
                    txTimePerLevel = new long[0];
                }
                long energyUsed = (long) (((double) (((((long) stats.tx_time) * txCurrent) + (((long) stats.rx_time) * rxCurrent)) + (rxIdleTime * rxIdleCurrent))) * voltage);
                if (rxIdleTime >= 0 && stats.on_time >= 0 && stats.tx_time >= 0 && stats.rx_time >= 0) {
                    if (energyUsed < 0) {
                    }
                    wifiActivityEnergyInfo = new WifiActivityEnergyInfo(SystemClock.elapsedRealtime(), 3, (long) stats.tx_time, txTimePerLevel, (long) stats.rx_time, rxIdleTime, energyUsed);
                }
                StringBuilder sb = new StringBuilder();
                sb.append(" rxIdleCur=").append(rxIdleCurrent);
                sb.append(" rxCur=").append(rxCurrent);
                sb.append(" txCur=").append(txCurrent);
                sb.append(" voltage=").append(voltage);
                sb.append(" on_time=").append(stats.on_time);
                sb.append(" tx_time=").append(stats.tx_time);
                sb.append(" tx_time_per_level=").append(Arrays.toString(txTimePerLevel));
                sb.append(" rx_time=").append(stats.rx_time);
                sb.append(" rxIdleTime=").append(rxIdleTime);
                sb.append(" energy=").append(energyUsed);
                Log.d(TAG, " reportActivityInfo: " + sb.toString());
                wifiActivityEnergyInfo = new WifiActivityEnergyInfo(SystemClock.elapsedRealtime(), 3, (long) stats.tx_time, txTimePerLevel, (long) stats.rx_time, rxIdleTime, energyUsed);
            }
            if (wifiActivityEnergyInfo == null || !wifiActivityEnergyInfo.isValid()) {
                return null;
            }
            return wifiActivityEnergyInfo;
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        enforceAccessPermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetConfiguredNetworks(Binder.getCallingUid(), this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public List<WifiConfiguration> getPrivilegedConfiguredNetworks() {
        enforceReadCredentialPermission();
        enforceAccessPermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetPrivilegedConfiguredNetwork(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncGetMatchingWifiConfig(scanResult, this.mWifiStateMachineChannel);
    }

    public int addOrUpdateNetwork(WifiConfiguration config) {
        Slog.d(TAG, "addOrUpdateNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", config:" + config + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        if (isValid(config) && isValidPasspoint(config)) {
            WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
            if (config.isPasspoint() && (enterpriseConfig.getEapMethod() == 1 || enterpriseConfig.getEapMethod() == 2)) {
                if (config.updateIdentifier != null) {
                    enforceAccessPermission();
                } else {
                    try {
                        verifyCert(enterpriseConfig.getCaCertificate());
                    } catch (CertPathValidatorException cpve) {
                        Slog.e(TAG, "CA Cert " + enterpriseConfig.getCaCertificate().getSubjectX500Principal() + " untrusted: " + cpve.getMessage());
                        return -1;
                    } catch (Exception e) {
                        Slog.e(TAG, "Failed to verify certificate" + enterpriseConfig.getCaCertificate().getSubjectX500Principal() + ": " + e);
                        return -1;
                    }
                }
            }
            Slog.i("addOrUpdateNetwork", " uid = " + Integer.toString(Binder.getCallingUid()) + " SSID " + config.SSID + " nid=" + Integer.toString(config.networkId));
            if (config.networkId == -1) {
                config.creatorUid = Binder.getCallingUid();
            } else {
                config.lastUpdateUid = Binder.getCallingUid();
            }
            if (this.mWifiStateMachineChannel != null) {
                return this.mWifiStateMachine.syncAddOrUpdateNetwork(this.mWifiStateMachineChannel, config);
            }
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return -1;
        }
        Slog.e(TAG, "bad network configuration");
        return -1;
    }

    public static void verifyCert(X509Certificate caCert) throws GeneralSecurityException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        CertPathValidator validator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
        CertPath path = factory.generateCertPath(Arrays.asList(new X509Certificate[]{caCert}));
        KeyStore ks = KeyStore.getInstance("AndroidCAStore");
        ks.load(null, null);
        PKIXParameters params = new PKIXParameters(ks);
        params.setRevocationEnabled(false);
        validator.validate(path, params);
    }

    public boolean removeNetwork(int netId) {
        Slog.d(TAG, "removeNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncRemoveNetwork(this.mWifiStateMachineChannel, netId);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public boolean enableNetwork(int netId, boolean disableOthers) {
        Slog.d(TAG, "enableNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", disableOthers:" + disableOthers + ", name=" + getAppName(Binder.getCallingPid()));
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncEnableNetwork(this.mWifiStateMachineChannel, netId, disableOthers);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public boolean disableNetwork(int netId) {
        String appName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        Slog.d(TAG, "disableNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", name=" + appName);
        enforceChangePermission();
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateApkChangewWifiStatus(2, appName);
        }
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncDisableNetwork(this.mWifiStateMachineChannel, netId);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public WifiInfo getConnectionInfo() {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncRequestConnectionInfo();
    }

    public List<ScanResult> getScanResults(String callingPackage) {
        enforceAccessPermission();
        int userId = UserHandle.getCallingUserId();
        int uid = Binder.getCallingUid();
        boolean canReadPeerMacAddresses = checkPeersMacAddress();
        boolean isActiveNetworkScorer = NetworkScorerAppManager.isCallerActiveScorer(this.mContext, uid);
        boolean hasInteractUsersFull = checkInteractAcrossUsersFull();
        long ident = Binder.clearCallingIdentity();
        if (!(canReadPeerMacAddresses || isActiveNetworkScorer)) {
            if (!isLocationEnabled()) {
                List arrayList = new ArrayList();
                Binder.restoreCallingIdentity(ident);
                return arrayList;
            }
        }
        if (!(canReadPeerMacAddresses || isActiveNetworkScorer)) {
            if (!checkCallerCanAccessScanResults(callingPackage, uid)) {
                arrayList = new ArrayList();
                Binder.restoreCallingIdentity(ident);
                return arrayList;
            }
        }
        try {
            List<ScanResult> arrayList2;
            if (this.mAppOps.noteOp(10, uid, callingPackage) != 0) {
                arrayList2 = new ArrayList();
                return arrayList2;
            } else if (isCurrentProfile(userId) || hasInteractUsersFull) {
                arrayList2 = this.mWifiStateMachine.syncGetScanResultsList();
                Binder.restoreCallingIdentity(ident);
                return arrayList2;
            } else {
                arrayList = new ArrayList();
                Binder.restoreCallingIdentity(ident);
                return arrayList;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int addPasspointManagementObject(String mo) {
        return this.mWifiStateMachine.syncAddPasspointManagementObject(this.mWifiStateMachineChannel, mo);
    }

    public int modifyPasspointManagementObject(String fqdn, List<PasspointManagementObjectDefinition> mos) {
        return this.mWifiStateMachine.syncModifyPasspointManagementObject(this.mWifiStateMachineChannel, fqdn, mos);
    }

    public void queryPasspointIcon(long bssid, String fileName) {
        this.mWifiStateMachine.syncQueryPasspointIcon(this.mWifiStateMachineChannel, bssid, fileName);
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        return this.mWifiStateMachine.matchProviderWithCurrentNetwork(this.mWifiStateMachineChannel, fqdn);
    }

    public void deauthenticateNetwork(long holdoff, boolean ess) {
        this.mWifiStateMachine.deauthenticateNetwork(this.mWifiStateMachineChannel, holdoff, ess);
    }

    private boolean isLocationEnabled() {
        return Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0) != 0 ? DBG : false;
    }

    private boolean checkInteractAcrossUsersFull() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0 ? DBG : false;
    }

    private boolean checkPeersMacAddress() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.PEERS_MAC_ADDRESS") == 0 ? DBG : false;
    }

    private boolean isCurrentProfile(int userId) {
        int currentUser = ActivityManager.getCurrentUser();
        if (userId == currentUser) {
            return DBG;
        }
        for (UserInfo user : this.mUserManager.getProfiles(currentUser)) {
            if (userId == user.id) {
                return DBG;
            }
        }
        return false;
    }

    public boolean saveConfiguration() {
        Slog.d(TAG, "saveConfiguration, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncSaveConfig(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public void setCountryCode(String countryCode, boolean persist) {
        Slog.i(TAG, "WifiService trying to set country code to " + countryCode + " with persist set to " + persist);
        enforceConnectivityInternalPermission();
        long token = Binder.clearCallingIdentity();
        try {
            if (this.mCountryCode.setCountryCode(countryCode, persist) && persist) {
                this.mFacade.setStringSetting(this.mContext, "wifi_country_code", countryCode);
            }
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public String getCountryCode() {
        enforceConnectivityInternalPermission();
        return this.mCountryCode.getCurrentCountryCode();
    }

    public void setFrequencyBand(int band, boolean persist) {
        enforceChangePermission();
        if (isDualBandSupported()) {
            Slog.i(TAG, "WifiService trying to set frequency band to " + band + " with persist set to " + persist);
            long token = Binder.clearCallingIdentity();
            try {
                this.mWifiStateMachine.setFrequencyBand(band, persist);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public int getFrequencyBand() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getFrequencyBand();
    }

    public boolean isDualBandSupported() {
        return this.mContext.getResources().getBoolean(17956886);
    }

    public DhcpInfo getDhcpInfo() {
        enforceAccessPermission();
        DhcpResults dhcpResults = this.mWifiStateMachine.syncGetDhcpResults();
        DhcpInfo info = new DhcpInfo();
        if (dhcpResults.ipAddress != null && (dhcpResults.ipAddress.getAddress() instanceof Inet4Address)) {
            info.ipAddress = NetworkUtils.inetAddressToInt((Inet4Address) dhcpResults.ipAddress.getAddress());
            info.netmask = NetworkUtils.prefixLengthToNetmaskInt(dhcpResults.ipAddress.getPrefixLength());
            Log.d("wifiserviceimpl", "netmask =" + info.netmask);
        }
        if (dhcpResults.gateway != null && (dhcpResults.gateway instanceof Inet4Address)) {
            info.gateway = NetworkUtils.inetAddressToInt((Inet4Address) dhcpResults.gateway);
        }
        int dnsFound = 0;
        for (InetAddress dns : dhcpResults.dnsServers) {
            if (dns instanceof Inet4Address) {
                if (dnsFound == 0) {
                    info.dns1 = NetworkUtils.inetAddressToInt((Inet4Address) dns);
                } else {
                    info.dns2 = NetworkUtils.inetAddressToInt((Inet4Address) dns);
                }
                dnsFound++;
                if (dnsFound > 1) {
                    break;
                }
            }
        }
        Inet4Address serverAddress = dhcpResults.serverAddress;
        if (serverAddress != null) {
            info.serverAddress = NetworkUtils.inetAddressToInt(serverAddress);
        }
        info.leaseDuration = dhcpResults.leaseDuration;
        return info;
    }

    public void addToBlacklist(String bssid) {
        enforceChangePermission();
        this.mWifiStateMachine.addToBlacklist(bssid);
    }

    public void clearBlacklist() {
        enforceChangePermission();
        this.mWifiStateMachine.clearBlacklist();
    }

    public void enableTdls(String remoteAddress, boolean enable) {
        if (remoteAddress == null) {
            throw new IllegalArgumentException("remoteAddress cannot be null");
        }
        TdlsTaskParams params = new TdlsTaskParams();
        params.remoteIpAddress = remoteAddress;
        params.enable = enable;
        new TdlsTask().execute(new TdlsTaskParams[]{params});
    }

    public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) {
        if (remoteMacAddress == null) {
            throw new IllegalArgumentException("remoteMacAddress cannot be null");
        }
        this.mWifiStateMachine.enableTdls(remoteMacAddress, enable);
    }

    public Messenger getWifiServiceMessenger() {
        Slog.d(TAG, "getWifiServiceMessenger, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        enforceAccessPermission();
        enforceChangePermission();
        return new Messenger(this.mClientHandler);
    }

    public void disableEphemeralNetwork(String SSID) {
        enforceAccessPermission();
        enforceChangePermission();
        this.mWifiStateMachine.disableEphemeralNetwork(SSID);
    }

    public String getConfigFile() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getConfigFile();
    }

    private void registerForScanModeChange() {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_scan_always_enabled"), false, new AnonymousClass11(null));
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        if (this.mContext.getResources().getBoolean(17957042)) {
            intentFilter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        }
        intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        if (this.mContext.getResources().getBoolean(17956893)) {
            intentFilter.addAction("android.intent.action.EMERGENCY_CALL_STATE_CHANGED");
        }
        registerForBroadcastsEx(intentFilter);
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    private void registerForPackageOrUserRemoval() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(this.mPackageOrUserReceiver, UserHandle.ALL, intentFilter, null, null);
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        if (args.length > 0 && WifiMetrics.PROTO_DUMP_ARG.equals(args[0])) {
            this.mWifiStateMachine.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
        } else if (args.length <= 0 || !"ipmanager".equals(args[0])) {
            pw.println("Wi-Fi is " + this.mWifiStateMachine.syncGetWifiStateByName());
            pw.println("Stay-awake conditions: " + Global.getInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in", 0));
            pw.println("mMulticastEnabled " + this.mMulticastEnabled);
            pw.println("mMulticastDisabled " + this.mMulticastDisabled);
            pw.println("mInIdleMode " + this.mInIdleMode);
            pw.println("mScanPending " + this.mScanPending);
            this.mWifiController.dump(fd, pw, args);
            this.mSettingsStore.dump(fd, pw, args);
            this.mNotificationController.dump(fd, pw, args);
            this.mTrafficPoller.dump(fd, pw, args);
            pw.println("Latest scan results:");
            List<ScanResult> scanResults = this.mWifiStateMachine.syncGetScanResultsList();
            long nowMs = System.currentTimeMillis();
            if (!(scanResults == null || scanResults.size() == 0)) {
                pw.println("    BSSID              Frequency  RSSI    Age      SSID                                 Flags");
                for (ScanResult r : scanResults) {
                    long ageSec = 0;
                    long ageMilli = 0;
                    if (nowMs > r.seen) {
                        if (r.seen > 0) {
                            ageSec = (nowMs - r.seen) / 1000;
                            ageMilli = (nowMs - r.seen) % 1000;
                        }
                    }
                    String candidate = " ";
                    if (r.isAutoJoinCandidate > 0) {
                        candidate = "+";
                    }
                    String str = "  %17s  %9d  %5d  %3d.%03d%s   %-32s  %s\n";
                    r19 = new Object[8];
                    r19[0] = r.BSSID;
                    r19[1] = Integer.valueOf(r.frequency);
                    r19[2] = Integer.valueOf(r.level);
                    r19[3] = Long.valueOf(ageSec);
                    r19[4] = Long.valueOf(ageMilli);
                    r19[5] = candidate;
                    r19[6] = r.SSID == null ? "" : r.SSID;
                    r19[7] = r.capabilities;
                    pw.printf(str, r19);
                }
            }
            pw.println();
            int i = this.mFullLocksAcquired;
            pw.println("Locks acquired: " + r0 + " full, " + this.mFullHighPerfLocksAcquired + " full high perf, " + this.mScanLocksAcquired + " scan");
            i = this.mFullLocksReleased;
            pw.println("Locks released: " + r0 + " full, " + this.mFullHighPerfLocksReleased + " full high perf, " + this.mScanLocksReleased + " scan");
            pw.println();
            pw.println("Locks held:");
            this.mLocks.dump(pw);
            pw.println("Multicast Locks held:");
            for (Multicaster l : this.mMulticasters) {
                pw.print("    ");
                pw.println(l);
            }
            pw.println();
            this.mWifiStateMachine.dump(fd, pw, args);
            pw.println();
        } else {
            String[] ipManagerArgs = new String[(args.length - 1)];
            System.arraycopy(args, 1, ipManagerArgs, 0, ipManagerArgs.length);
            this.mWifiStateMachine.dumpIpManager(fd, pw, ipManagerArgs);
        }
    }

    void enforceWakeSourcePermission(int uid, int pid) {
        if (uid != Process.myUid()) {
            this.mContext.enforcePermission("android.permission.UPDATE_DEVICE_STATS", pid, uid, null);
        }
    }

    public boolean acquireWifiLock(IBinder binder, int lockMode, String tag, WorkSource ws) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
        if (lockMode == 1 || lockMode == 2 || lockMode == 3) {
            boolean acquireWifiLockLocked;
            if (ws != null && ws.size() == 0) {
                ws = null;
            }
            if (ws != null) {
                enforceWakeSourcePermission(Binder.getCallingUid(), Binder.getCallingPid());
            }
            if (ws == null) {
                ws = new WorkSource(Binder.getCallingUid());
            }
            WifiLock wifiLock = new WifiLock(lockMode, tag, binder, ws);
            synchronized (this.mLocks) {
                acquireWifiLockLocked = acquireWifiLockLocked(wifiLock);
            }
            return acquireWifiLockLocked;
        }
        Slog.e(TAG, "Illegal argument, lockMode= " + lockMode);
        throw new IllegalArgumentException("lockMode=" + lockMode);
    }

    private void noteAcquireWifiLock(WifiLock wifiLock) throws RemoteException {
        switch (wifiLock.mMode) {
            case Extension.TYPE_DOUBLE /*1*/:
            case Extension.TYPE_FLOAT /*2*/:
            case Extension.TYPE_INT64 /*3*/:
                this.mBatteryStats.noteFullWifiLockAcquiredFromSource(wifiLock.mWorkSource);
            default:
        }
    }

    private void noteReleaseWifiLock(WifiLock wifiLock) throws RemoteException {
        switch (wifiLock.mMode) {
            case Extension.TYPE_DOUBLE /*1*/:
            case Extension.TYPE_FLOAT /*2*/:
            case Extension.TYPE_INT64 /*3*/:
                this.mBatteryStats.noteFullWifiLockReleasedFromSource(wifiLock.mWorkSource);
            default:
        }
    }

    private boolean acquireWifiLockLocked(WifiLock wifiLock) {
        boolean z;
        Slog.d(TAG, "acquireWifiLockLocked: " + wifiLock);
        this.mLocks.addLock(wifiLock);
        long ident = Binder.clearCallingIdentity();
        try {
            noteAcquireWifiLock(wifiLock);
            switch (wifiLock.mMode) {
                case Extension.TYPE_DOUBLE /*1*/:
                    this.mFullLocksAcquired++;
                    break;
                case Extension.TYPE_FLOAT /*2*/:
                    this.mScanLocksAcquired++;
                    break;
                case Extension.TYPE_INT64 /*3*/:
                    this.mFullHighPerfLocksAcquired++;
                    break;
            }
            this.mWifiController.sendMessage(155654);
            z = DBG;
            return z;
        } catch (RemoteException e) {
            z = false;
            return z;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void updateWifiLockWorkSource(IBinder lock, WorkSource ws) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (ws != null && ws.size() == 0) {
            ws = null;
        }
        if (ws != null) {
            enforceWakeSourcePermission(uid, pid);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLocks) {
                int index = this.mLocks.findLockByBinder(lock);
                if (index < 0) {
                    throw new IllegalArgumentException("Wifi lock not active");
                }
                WifiLock wl = (WifiLock) this.mLocks.mList.get(index);
                noteReleaseWifiLock(wl);
                wl.mWorkSource = ws != null ? new WorkSource(ws) : new WorkSource(uid);
                noteAcquireWifiLock(wl);
            }
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean releaseWifiLock(IBinder lock) {
        boolean releaseWifiLockLocked;
        this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
        synchronized (this.mLocks) {
            releaseWifiLockLocked = releaseWifiLockLocked(lock);
        }
        return releaseWifiLockLocked;
    }

    private boolean releaseWifiLockLocked(IBinder lock) {
        WifiLock wifiLock = this.mLocks.removeLock(lock);
        Slog.d(TAG, "releaseWifiLockLocked: " + wifiLock);
        boolean hadLock = wifiLock != null ? DBG : false;
        long ident = Binder.clearCallingIdentity();
        if (hadLock) {
            try {
                noteReleaseWifiLock(wifiLock);
                switch (wifiLock.mMode) {
                    case Extension.TYPE_DOUBLE /*1*/:
                        this.mFullLocksReleased++;
                        break;
                    case Extension.TYPE_FLOAT /*2*/:
                        this.mScanLocksReleased++;
                        break;
                    case Extension.TYPE_INT64 /*3*/:
                        this.mFullHighPerfLocksReleased++;
                        break;
                }
                this.mWifiController.sendMessage(155654);
            } catch (RemoteException e) {
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        Binder.restoreCallingIdentity(ident);
        return hadLock;
    }

    public void initializeMulticastFiltering() {
        enforceMulticastChangePermission();
        synchronized (this.mMulticasters) {
            if (this.mMulticasters.size() != 0) {
                return;
            }
            this.mWifiStateMachine.startFilteringMulticastPackets();
        }
    }

    public void acquireMulticastLock(IBinder binder, String tag) {
        enforceMulticastChangePermission();
        synchronized (this.mMulticasters) {
            this.mMulticastEnabled++;
            this.mMulticasters.add(new Multicaster(tag, binder));
            this.mWifiStateMachine.stopFilteringMulticastPackets();
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.noteWifiMulticastEnabled(uid);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void releaseMulticastLock() {
        enforceMulticastChangePermission();
        int uid = Binder.getCallingUid();
        synchronized (this.mMulticasters) {
            this.mMulticastDisabled++;
            for (int i = this.mMulticasters.size() - 1; i >= 0; i--) {
                Multicaster m = (Multicaster) this.mMulticasters.get(i);
                if (m != null && m.getUid() == uid) {
                    removeMulticasterLocked(i, uid);
                }
            }
        }
    }

    private void removeMulticasterLocked(int i, int uid) {
        Multicaster removed = (Multicaster) this.mMulticasters.remove(i);
        if (removed != null) {
            removed.unlinkDeathRecipient();
        }
        if (this.mMulticasters.size() == 0) {
            this.mWifiStateMachine.startFilteringMulticastPackets();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.noteWifiMulticastDisabled(uid);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean isMulticastEnabled() {
        boolean z = false;
        enforceAccessPermission();
        synchronized (this.mMulticasters) {
            if (this.mMulticasters.size() > 0) {
                z = DBG;
            }
        }
        return z;
    }

    public void enableVerboseLogging(int verbose) {
        enforceAccessPermission();
        this.mWifiStateMachine.enableVerboseLogging(verbose);
    }

    public int getVerboseLoggingLevel() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getVerboseLoggingLevel();
    }

    public void enableAggressiveHandover(int enabled) {
        enforceAccessPermission();
        this.mWifiStateMachine.enableAggressiveHandover(enabled);
    }

    public int getAggressiveHandover() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getAggressiveHandover();
    }

    public void setAllowScansWithTraffic(int enabled) {
        enforceAccessPermission();
        this.mWifiStateMachine.setAllowScansWithTraffic(enabled);
    }

    public int getAllowScansWithTraffic() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getAllowScansWithTraffic();
    }

    public boolean setEnableAutoJoinWhenAssociated(boolean enabled) {
        enforceChangePermission();
        return this.mWifiStateMachine.setEnableAutoJoinWhenAssociated(enabled);
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getEnableAutoJoinWhenAssociated();
    }

    public WifiConnectionStatistics getConnectionStatistics() {
        enforceAccessPermission();
        enforceReadCredentialPermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetConnectionStatistics(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public void factoryReset() {
        enforceConnectivityInternalPermission();
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                setWifiApEnabled(null, false);
            }
            if (!this.mUserManager.hasUserRestriction("no_config_wifi")) {
                setWifiEnabled(DBG);
                int i = 0;
                while (i < 10) {
                    List<WifiConfiguration> networks = getConfiguredNetworks();
                    if (networks != null) {
                        for (WifiConfiguration config : networks) {
                            removeNetwork(config.networkId);
                        }
                        saveConfiguration();
                    } else {
                        i++;
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    static boolean logAndReturnFalse(String s) {
        Log.d(TAG, s);
        return false;
    }

    public static boolean isValid(WifiConfiguration config) {
        String validity = checkValidity(config);
        return validity != null ? logAndReturnFalse(validity) : DBG;
    }

    public static boolean isValidPasspoint(WifiConfiguration config) {
        String validity = checkPasspointValidity(config);
        return validity != null ? logAndReturnFalse(validity) : DBG;
    }

    public static String checkValidity(WifiConfiguration config) {
        if (config.allowedKeyManagement == null) {
            return "allowed kmgmt";
        }
        if (config.allowedKeyManagement.cardinality() > 1) {
            if (config.allowedKeyManagement.get(8) && config.allowedKeyManagement.get(9)) {
                if (config.allowedKeyManagement.cardinality() != 4) {
                    return "include WAPI_PSK and WAPI_CERT but is still invalid for cardinality != 4";
                }
            } else if (config.allowedKeyManagement.cardinality() != 2) {
                return "invalid for cardinality != 2";
            }
            if (!config.allowedKeyManagement.get(2)) {
                return "not WPA_EAP";
            }
            if (!(config.allowedKeyManagement.get(3) || config.allowedKeyManagement.get(1))) {
                return "not PSK or 8021X";
            }
        }
        return null;
    }

    public static String checkPasspointValidity(WifiConfiguration config) {
        if (!TextUtils.isEmpty(config.FQDN)) {
            if (!TextUtils.isEmpty(config.SSID)) {
                return "SSID not expected for Passpoint: '" + config.SSID + "' FQDN " + toHexString(config.FQDN);
            }
            if (TextUtils.isEmpty(config.providerFriendlyName)) {
                return "no provider friendly name";
            }
            WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
            if (enterpriseConfig == null || enterpriseConfig.getEapMethod() == -1) {
                return "no enterprise config";
            }
            if ((enterpriseConfig.getEapMethod() == 1 || enterpriseConfig.getEapMethod() == 2 || enterpriseConfig.getEapMethod() == 0) && enterpriseConfig.getCaCertificate() == null) {
                return "no CA certificate";
            }
        }
        return null;
    }

    public Network getCurrentNetwork() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getCurrentNetwork();
    }

    public static String toHexString(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\'').append(s).append('\'');
        for (int n = 0; n < s.length(); n++) {
            sb.append(String.format(" %02x", new Object[]{Integer.valueOf(s.charAt(n) & Constants.SHORT_MASK)}));
        }
        return sb.toString();
    }

    private boolean checkCallerCanAccessScanResults(String callingPackage, int uid) {
        if (ActivityManager.checkUidPermission("android.permission.ACCESS_FINE_LOCATION", uid) == 0 && checkAppOppAllowed(1, callingPackage, uid)) {
            return DBG;
        }
        if (ActivityManager.checkUidPermission("android.permission.ACCESS_COARSE_LOCATION", uid) == 0 && checkAppOppAllowed(0, callingPackage, uid)) {
            return DBG;
        }
        if (!isMApp(this.mContext, callingPackage) && isForegroundApp(callingPackage)) {
            return DBG;
        }
        Log.e(TAG, "Permission denial: Need ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get scan results");
        return false;
    }

    private boolean checkAppOppAllowed(int op, String callingPackage, int uid) {
        return this.mAppOps.noteOp(op, uid, callingPackage) == 0 ? DBG : false;
    }

    private static boolean isMApp(Context context, String pkgName) {
        boolean z = DBG;
        try {
            if (context.getPackageManager().getApplicationInfo(pkgName, 0).targetSdkVersion < 23) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            return DBG;
        }
    }

    public void hideCertFromUnaffiliatedUsers(String alias) {
        this.mCertManager.hideCertFromUnaffiliatedUsers(alias);
    }

    public String[] listClientCertsForCurrentUser() {
        return this.mCertManager.listClientCertsForCurrentUser();
    }

    private boolean isForegroundApp(String pkgName) {
        List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (tasks.isEmpty()) {
            return false;
        }
        return pkgName.equals(((RunningTaskInfo) tasks.get(0)).topActivity.getPackageName());
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        enforceConnectivityInternalPermission();
        this.mWifiStateMachine.enableWifiConnectivityManager(enabled);
    }

    protected void handleAirplaneModeToggled() {
        this.mWifiController.sendMessage(155657);
    }

    protected void onReceiveEx(Context context, Intent intent) {
    }

    protected void registerForBroadcastsEx(IntentFilter intentFilter) {
    }

    protected void setWifiEnabledAfterVoWifiOff(boolean enable) {
    }

    public boolean setVoWifiDetectMode(WifiDetectConfInfo info) {
        return false;
    }

    protected void handleForgetNetwork(Message msg) {
    }

    protected String getPackageName(int pID) {
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pID && appProcess.pkgList != null && appProcess.pkgList.length > 0) {
                return appProcess.pkgList[0];
            }
        }
        return null;
    }
}
