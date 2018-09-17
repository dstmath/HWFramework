package com.android.server.wifi;

import android.app.ActivityManager;
import android.content.Context;
import android.net.NetworkScoreManager;
import android.net.wifi.IApInterface;
import android.net.wifi.IWifiScanner;
import android.net.wifi.IWificond;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiNetworkScoreCache;
import android.net.wifi.WifiScanner;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.app.IBatteryStats.Stub;
import com.android.server.am.BatteryStatsService;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.net.IpConfigStore;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.hotspot2.LegacyPasspointConfigParser;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.hotspot2.PasspointObjectFactory;
import com.android.server.wifi.p2p.SupplicantP2pIfaceHal;
import com.android.server.wifi.p2p.WifiP2pMonitor;
import com.android.server.wifi.p2p.WifiP2pNative;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WifiInjector {
    private static final String BOOT_DEFAULT_WIFI_COUNTRY_CODE = "ro.boot.wificountrycode";
    private static final String WIFICOND_SERVICE_NAME = "wificond";
    static WifiInjector sWifiInjector = null;
    private final BackupManagerProxy mBackupManagerProxy = new BackupManagerProxy();
    private final IBatteryStats mBatteryStats;
    private final BuildProperties mBuildProperties = new SystemBuildProperties();
    private final WifiCertManager mCertManager;
    private final Clock mClock = new Clock();
    private final LocalLog mConnectivityLocalLog;
    private final Context mContext;
    private final WifiCountryCode mCountryCode;
    private final FrameworkFacade mFrameworkFacade = new FrameworkFacade();
    private HalDeviceManager mHalDeviceManager;
    private final IpConfigStore mIpConfigStore;
    private final Runtime mJavaRuntime;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private final WifiLockManager mLockManager;
    private final NetworkScoreManager mNetworkScoreManager;
    private final WifiNotificationController mNotificationController;
    private final PasspointManager mPasspointManager;
    private final PasspointNetworkEvaluator mPasspointNetworkEvaluator;
    private final PropertyService mPropertyService = new SystemPropertyService();
    private final SavedNetworkEvaluator mSavedNetworkEvaluator;
    private final ScoredNetworkEvaluator mScoredNetworkEvaluator;
    private final SelfRecovery mSelfRecovery;
    private final WifiSettingsStore mSettingsStore;
    private final SIMAccessor mSimAccessor;
    private final SupplicantP2pIfaceHal mSupplicantP2pIfaceHal;
    private final SupplicantStaIfaceHal mSupplicantStaIfaceHal;
    private final WifiTrafficPoller mTrafficPoller;
    private final boolean mUseRealLogger;
    private final WifiApConfigStore mWifiApConfigStore;
    private HandlerThread mWifiAwareHandlerThread;
    private final WifiBackupRestore mWifiBackupRestore;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiConfigStore mWifiConfigStore;
    private final WifiConfigStoreLegacy mWifiConfigStoreLegacy;
    private final WifiConnectivityHelper mWifiConnectivityHelper;
    private final WifiController mWifiController;
    private final WifiKeyStore mWifiKeyStore;
    private final WifiLastResortWatchdog mWifiLastResortWatchdog;
    private final WifiMetrics mWifiMetrics;
    private final WifiMonitor mWifiMonitor;
    private final WifiMulticastLockManager mWifiMulticastLockManager;
    private final WifiNative mWifiNative;
    private final WifiNetworkHistory mWifiNetworkHistory;
    private final WifiNetworkScoreCache mWifiNetworkScoreCache;
    private final WifiNetworkSelector mWifiNetworkSelector;
    private final WifiP2pMonitor mWifiP2pMonitor;
    private final WifiP2pNative mWifiP2pNative;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;
    private WifiScanner mWifiScanner;
    private final HandlerThread mWifiServiceHandlerThread;
    private final WifiStateMachine mWifiStateMachine;
    private final HandlerThread mWifiStateMachineHandlerThread;
    private final WifiStateTracker mWifiStateTracker;
    private final WifiVendorHal mWifiVendorHal;
    private final WificondControl mWificondControl;

    public WifiInjector(Context context) {
        if (context == null) {
            throw new IllegalStateException("WifiInjector should not be initialized with a null Context.");
        } else if (sWifiInjector != null) {
            throw new IllegalStateException("WifiInjector was already created, use getInstance instead.");
        } else {
            sWifiInjector = this;
            this.mContext = context;
            this.mUseRealLogger = this.mContext.getResources().getBoolean(17957054);
            this.mSettingsStore = new WifiSettingsStore(this.mContext);
            this.mWifiPermissionsWrapper = new WifiPermissionsWrapper(this.mContext);
            this.mNetworkScoreManager = (NetworkScoreManager) this.mContext.getSystemService(NetworkScoreManager.class);
            this.mWifiNetworkScoreCache = new WifiNetworkScoreCache(this.mContext);
            if (this.mNetworkScoreManager != null) {
                this.mNetworkScoreManager.registerNetworkScoreCache(1, this.mWifiNetworkScoreCache, 0);
            }
            this.mWifiPermissionsUtil = new WifiPermissionsUtil(this.mWifiPermissionsWrapper, this.mContext, this.mSettingsStore, UserManager.get(this.mContext), this.mNetworkScoreManager, this);
            this.mWifiBackupRestore = new WifiBackupRestore(this.mWifiPermissionsUtil);
            this.mBatteryStats = Stub.asInterface(this.mFrameworkFacade.getService("batterystats"));
            this.mWifiStateTracker = new WifiStateTracker(this.mBatteryStats);
            this.mWifiServiceHandlerThread = new HandlerThread("WifiService");
            this.mWifiServiceHandlerThread.start();
            this.mWifiServiceHandlerThread.getLooper().setSlowDispatchThresholdMs(500);
            this.mWifiStateMachineHandlerThread = new HandlerThread("WifiStateMachine");
            this.mWifiStateMachineHandlerThread.start();
            Looper wifiStateMachineLooper = this.mWifiStateMachineHandlerThread.getLooper();
            this.mWifiStateMachineHandlerThread.getLooper().setSlowDispatchThresholdMs(500);
            this.mWifiMetrics = new WifiMetrics(this.mClock, wifiStateMachineLooper);
            HwWifiServiceFactory.initWifiStatStore(this.mContext);
            HwWifiServiceFactory.initWifiCHRStateManager(this.mContext);
            HwWifiServiceFactory.initWifiCHRService(this.mContext);
            this.mWifiMonitor = new WifiMonitor(this);
            this.mHalDeviceManager = new HalDeviceManager();
            this.mWifiVendorHal = new WifiVendorHal(this.mHalDeviceManager, this.mWifiStateMachineHandlerThread.getLooper());
            this.mSupplicantStaIfaceHal = new SupplicantStaIfaceHal(this.mContext, this.mWifiMonitor);
            this.mWificondControl = new WificondControl(this, this.mWifiMonitor);
            this.mWifiNative = new WifiNative(SystemProperties.get("wifi.interface", HwWifiCHRStateManager.MAIN_IFACE), this.mWifiVendorHal, this.mSupplicantStaIfaceHal, this.mWificondControl);
            this.mWifiP2pMonitor = new WifiP2pMonitor(this);
            this.mSupplicantP2pIfaceHal = new SupplicantP2pIfaceHal(this.mWifiP2pMonitor);
            this.mWifiP2pNative = new WifiP2pNative(SystemProperties.get("wifi.direct.interface", "p2p0"), this.mSupplicantP2pIfaceHal);
            this.mTrafficPoller = new WifiTrafficPoller(this.mContext, this.mWifiServiceHandlerThread.getLooper(), this.mWifiNative.getInterfaceName());
            this.mCountryCode = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiCountryCode(this.mContext, this.mWifiNative, SystemProperties.get(BOOT_DEFAULT_WIFI_COUNTRY_CODE), this.mContext.getResources().getBoolean(17957061));
            this.mWifiApConfigStore = new WifiApConfigStore(this.mContext, this.mBackupManagerProxy);
            this.mWifiKeyStore = new WifiKeyStore(this.mKeyStore);
            this.mWifiConfigStore = new WifiConfigStore(this.mContext, wifiStateMachineLooper, this.mClock, WifiConfigStore.createSharedFile());
            DelayedDiskWrite writer = new DelayedDiskWrite();
            this.mWifiNetworkHistory = new WifiNetworkHistory(this.mContext, writer);
            this.mIpConfigStore = new IpConfigStore(writer);
            this.mWifiConfigStoreLegacy = new WifiConfigStoreLegacy(this.mWifiNetworkHistory, this.mWifiNative, this.mIpConfigStore, new LegacyPasspointConfigParser());
            this.mWifiConfigManager = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiConfigManager(this.mContext, this.mClock, UserManager.get(this.mContext), TelephonyManager.from(this.mContext), this.mWifiKeyStore, this.mWifiConfigStore, this.mWifiConfigStoreLegacy, this.mWifiPermissionsUtil, this.mWifiPermissionsWrapper, new NetworkListStoreData(), new DeletedEphemeralSsidsStoreData());
            this.mWifiConnectivityHelper = new WifiConnectivityHelper(this.mWifiNative);
            this.mConnectivityLocalLog = new LocalLog(ActivityManager.isLowRamDeviceStatic() ? 256 : 512);
            this.mWifiNetworkSelector = new WifiNetworkSelector(this.mContext, this.mWifiConfigManager, this.mClock, this.mConnectivityLocalLog);
            this.mScoredNetworkEvaluator = new ScoredNetworkEvaluator(context, wifiStateMachineLooper, this.mFrameworkFacade, this.mNetworkScoreManager, this.mWifiConfigManager, this.mConnectivityLocalLog, this.mWifiNetworkScoreCache);
            this.mSimAccessor = new SIMAccessor(this.mContext);
            this.mPasspointManager = new PasspointManager(this.mContext, this.mWifiNative, this.mWifiKeyStore, this.mClock, this.mSimAccessor, new PasspointObjectFactory(), this.mWifiConfigManager, this.mWifiConfigStore);
            this.mPasspointNetworkEvaluator = new PasspointNetworkEvaluator(this.mPasspointManager, this.mWifiConfigManager, this.mConnectivityLocalLog);
            this.mJavaRuntime = Runtime.getRuntime();
            this.mWifiStateMachine = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiStateMachine(this.mContext, this.mFrameworkFacade, wifiStateMachineLooper, UserManager.get(this.mContext), this, this.mBackupManagerProxy, this.mCountryCode, this.mWifiNative);
            this.mSavedNetworkEvaluator = HwWifiServiceFactory.getHwWifiServiceManager().createHwSavedNetworkEvaluator(this.mContext, this.mWifiConfigManager, this.mClock, this.mConnectivityLocalLog, this.mWifiStateMachine, this.mWifiConnectivityHelper);
            this.mCertManager = new WifiCertManager(this.mContext);
            this.mNotificationController = new WifiNotificationController(this.mContext, this.mWifiServiceHandlerThread.getLooper(), this.mFrameworkFacade, null, this);
            this.mLockManager = new WifiLockManager(this.mContext, BatteryStatsService.getService());
            this.mWifiController = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiController(this.mContext, this.mWifiStateMachine, this.mSettingsStore, this.mLockManager, this.mWifiServiceHandlerThread.getLooper(), this.mFrameworkFacade);
            this.mSelfRecovery = new SelfRecovery(this.mWifiController);
            this.mWifiLastResortWatchdog = new WifiLastResortWatchdog(this.mSelfRecovery, this.mWifiMetrics);
            this.mWifiMulticastLockManager = new WifiMulticastLockManager(this.mWifiStateMachine, BatteryStatsService.getService());
        }
    }

    public static WifiInjector getInstance() {
        if (sWifiInjector != null) {
            return sWifiInjector;
        }
        throw new IllegalStateException("Attempted to retrieve a WifiInjector instance before constructor was called.");
    }

    public UserManager getUserManager() {
        return UserManager.get(this.mContext);
    }

    public WifiMetrics getWifiMetrics() {
        return this.mWifiMetrics;
    }

    public SupplicantStaIfaceHal getSupplicantStaIfaceHal() {
        return this.mSupplicantStaIfaceHal;
    }

    public BackupManagerProxy getBackupManagerProxy() {
        return this.mBackupManagerProxy;
    }

    public FrameworkFacade getFrameworkFacade() {
        return this.mFrameworkFacade;
    }

    public HandlerThread getWifiServiceHandlerThread() {
        return this.mWifiServiceHandlerThread;
    }

    public HandlerThread getWifiStateMachineHandlerThread() {
        return this.mWifiStateMachineHandlerThread;
    }

    public WifiTrafficPoller getWifiTrafficPoller() {
        return this.mTrafficPoller;
    }

    public WifiCountryCode getWifiCountryCode() {
        return this.mCountryCode;
    }

    public WifiApConfigStore getWifiApConfigStore() {
        return this.mWifiApConfigStore;
    }

    public WifiStateMachine getWifiStateMachine() {
        return this.mWifiStateMachine;
    }

    public WifiSettingsStore getWifiSettingsStore() {
        return this.mSettingsStore;
    }

    public WifiCertManager getWifiCertManager() {
        return this.mCertManager;
    }

    public WifiNotificationController getWifiNotificationController() {
        return this.mNotificationController;
    }

    public WifiLockManager getWifiLockManager() {
        return this.mLockManager;
    }

    public WifiController getWifiController() {
        return this.mWifiController;
    }

    public WifiLastResortWatchdog getWifiLastResortWatchdog() {
        return this.mWifiLastResortWatchdog;
    }

    public Clock getClock() {
        return this.mClock;
    }

    public PropertyService getPropertyService() {
        return this.mPropertyService;
    }

    public BuildProperties getBuildProperties() {
        return this.mBuildProperties;
    }

    public KeyStore getKeyStore() {
        return this.mKeyStore;
    }

    public WifiBackupRestore getWifiBackupRestore() {
        return this.mWifiBackupRestore;
    }

    public WifiMulticastLockManager getWifiMulticastLockManager() {
        return this.mWifiMulticastLockManager;
    }

    public WifiConfigManager getWifiConfigManager() {
        return this.mWifiConfigManager;
    }

    public PasspointManager getPasspointManager() {
        return this.mPasspointManager;
    }

    public TelephonyManager makeTelephonyManager() {
        return (TelephonyManager) this.mContext.getSystemService("phone");
    }

    public WifiStateTracker getWifiStateTracker() {
        return this.mWifiStateTracker;
    }

    public IWificond makeWificond() {
        return IWificond.Stub.asInterface(ServiceManager.getService(WIFICOND_SERVICE_NAME));
    }

    public SoftApManager makeSoftApManager(INetworkManagementService nmService, Listener listener, IApInterface apInterface, WifiConfiguration config) {
        return HwWifiServiceFactory.getHwWifiServiceManager().createHwSoftApManager(this.mContext, this.mWifiServiceHandlerThread.getLooper(), this.mWifiNative, this.mCountryCode.getCountryCodeSentToDriver(), listener, apInterface, nmService, this.mWifiApConfigStore, config, this.mWifiMetrics);
    }

    public WifiLog makeLog(String tag) {
        return new LogcatLog(tag);
    }

    public BaseWifiDiagnostics makeWifiDiagnostics(WifiNative wifiNative) {
        if (!this.mUseRealLogger) {
            return new BaseWifiDiagnostics(wifiNative);
        }
        return new WifiDiagnostics(this.mContext, this, this.mWifiStateMachine, wifiNative, this.mBuildProperties, new LastMileLogger(this));
    }

    public synchronized WifiScanner getWifiScanner() {
        if (this.mWifiScanner == null) {
            this.mWifiScanner = new WifiScanner(this.mContext, IWifiScanner.Stub.asInterface(ServiceManager.getService("wifiscanner")), this.mWifiStateMachineHandlerThread.getLooper());
        }
        return this.mWifiScanner;
    }

    public WifiConnectivityManager makeWifiConnectivityManager(WifiInfo wifiInfo, boolean hasConnectionRequests) {
        return HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiConnectivityManager(this.mContext, this.mWifiStateMachine, getWifiScanner(), this.mWifiConfigManager, wifiInfo, this.mWifiNetworkSelector, this.mWifiConnectivityHelper, this.mWifiLastResortWatchdog, this.mWifiMetrics, this.mWifiStateMachineHandlerThread.getLooper(), this.mClock, this.mConnectivityLocalLog, hasConnectionRequests, this.mFrameworkFacade, this.mSavedNetworkEvaluator, this.mScoredNetworkEvaluator, this.mPasspointNetworkEvaluator);
    }

    public WifiPermissionsUtil getWifiPermissionsUtil() {
        return this.mWifiPermissionsUtil;
    }

    public WifiPermissionsWrapper getWifiPermissionsWrapper() {
        return this.mWifiPermissionsWrapper;
    }

    public HandlerThread getWifiAwareHandlerThread() {
        if (this.mWifiAwareHandlerThread == null) {
            this.mWifiAwareHandlerThread = new HandlerThread("wifiAwareService");
            this.mWifiAwareHandlerThread.start();
        }
        return this.mWifiAwareHandlerThread;
    }

    public HalDeviceManager getHalDeviceManager() {
        return this.mHalDeviceManager;
    }

    public Runtime getJavaRuntime() {
        return this.mJavaRuntime;
    }

    public WifiNative getWifiNative() {
        return this.mWifiNative;
    }

    public WifiMonitor getWifiMonitor() {
        return this.mWifiMonitor;
    }

    public WifiP2pNative getWifiP2pNative() {
        return this.mWifiP2pNative;
    }

    public WifiP2pMonitor getWifiP2pMonitor() {
        return this.mWifiP2pMonitor;
    }

    public SavedNetworkEvaluator getSavedNetworkEvaluator() {
        return this.mSavedNetworkEvaluator;
    }

    public SelfRecovery getSelfRecovery() {
        return this.mSelfRecovery;
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        int i = 0;
        writer.println("================");
        writer.println("======WifiService stackTraces======");
        for (StackTraceElement stackTrace : this.mWifiServiceHandlerThread.getStackTrace()) {
            writer.println(stackTrace);
        }
        writer.println("======WifiStateMachine stackTraces======");
        StackTraceElement[] stackTraces = this.mWifiStateMachineHandlerThread.getStackTrace();
        int length = stackTraces.length;
        while (i < length) {
            writer.println(stackTraces[i]);
            i++;
        }
        writer.println("================");
    }
}
