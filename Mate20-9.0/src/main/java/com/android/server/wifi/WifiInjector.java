package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.net.NetworkScoreManager;
import android.net.wifi.IWifiScanner;
import android.net.wifi.IWificond;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.PowerProfile;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.BatteryStatsService;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.net.IpConfigStore;
import com.android.server.wifi.ClientModeManager;
import com.android.server.wifi.ScanOnlyModeManager;
import com.android.server.wifi.WifiConfigStoreLegacy;
import com.android.server.wifi.aware.WifiAwareMetrics;
import com.android.server.wifi.hotspot2.LegacyPasspointConfigParser;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.hotspot2.PasspointObjectFactory;
import com.android.server.wifi.p2p.SupplicantP2pIfaceHal;
import com.android.server.wifi.p2p.WifiP2pMonitor;
import com.android.server.wifi.p2p.WifiP2pNative;
import com.android.server.wifi.rtt.RttMetrics;
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
    private final CarrierNetworkConfig mCarrierNetworkConfig;
    private final CarrierNetworkNotifier mCarrierNetworkNotifier;
    private final Clock mClock = new Clock();
    private final LocalLog mConnectivityLocalLog;
    private final Context mContext;
    private final WifiCountryCode mCountryCode;
    private final FrameworkFacade mFrameworkFacade = new FrameworkFacade();
    private HalDeviceManager mHalDeviceManager;
    private final HostapdHal mHostapdHal;
    private final IpConfigStore mIpConfigStore;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private final WifiLockManager mLockManager;
    private final NetworkScoreManager mNetworkScoreManager;
    private final INetworkManagementService mNwManagementService;
    private final OpenNetworkNotifier mOpenNetworkNotifier;
    private final PasspointManager mPasspointManager;
    private final PasspointNetworkEvaluator mPasspointNetworkEvaluator;
    private final PropertyService mPropertyService = new SystemPropertyService();
    private HandlerThread mRttHandlerThread;
    private final SarManager mSarManager;
    private final SavedNetworkEvaluator mSavedNetworkEvaluator;
    private final ScanRequestProxy mScanRequestProxy;
    private final ScoredNetworkEvaluator mScoredNetworkEvaluator;
    private final ScoringParams mScoringParams;
    private final SelfRecovery mSelfRecovery;
    private final WifiSettingsStore mSettingsStore;
    private final SIMAccessor mSimAccessor;
    private final SupplicantP2pIfaceHal mSupplicantP2pIfaceHal;
    private final SupplicantStaIfaceHal mSupplicantStaIfaceHal;
    private final WifiTrafficPoller mTrafficPoller;
    private final boolean mUseRealLogger;
    private final WakeupController mWakeupController;
    private final WifiApConfigStore mWifiApConfigStore;
    private HandlerThread mWifiAwareHandlerThread;
    private final WifiBackupRestore mWifiBackupRestore;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiConfigStore mWifiConfigStore;
    private final WifiConfigStoreLegacy mWifiConfigStoreLegacy;
    private final WifiConnectivityHelper mWifiConnectivityHelper;
    private final WifiController mWifiController;
    private final BaseWifiDiagnostics mWifiDiagnostics;
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
    private final WifiStateMachinePrime mWifiStateMachinePrime;
    private final WifiStateTracker mWifiStateTracker;
    private final WifiVendorHal mWifiVendorHal;
    private final WificondControl mWificondControl;

    public WifiInjector(Context context) {
        Context context2 = context;
        if (context2 == null) {
            throw new IllegalStateException("WifiInjector should not be initialized with a null Context.");
        } else if (sWifiInjector == null) {
            sWifiInjector = this;
            this.mContext = context2;
            this.mUseRealLogger = this.mContext.getResources().getBoolean(17957077);
            this.mSettingsStore = new WifiSettingsStore(this.mContext);
            this.mWifiPermissionsWrapper = new WifiPermissionsWrapper(this.mContext);
            this.mNetworkScoreManager = (NetworkScoreManager) this.mContext.getSystemService(NetworkScoreManager.class);
            this.mWifiNetworkScoreCache = new WifiNetworkScoreCache(this.mContext);
            if (this.mNetworkScoreManager != null) {
                this.mNetworkScoreManager.registerNetworkScoreCache(1, this.mWifiNetworkScoreCache, 0);
            }
            WifiPermissionsUtil wifiPermissionsUtil = new WifiPermissionsUtil(this.mWifiPermissionsWrapper, this.mContext, this.mSettingsStore, UserManager.get(this.mContext), this);
            this.mWifiPermissionsUtil = wifiPermissionsUtil;
            this.mWifiBackupRestore = new WifiBackupRestore(this.mWifiPermissionsUtil);
            this.mBatteryStats = IBatteryStats.Stub.asInterface(this.mFrameworkFacade.getService("batterystats"));
            this.mWifiStateTracker = new WifiStateTracker(this.mBatteryStats);
            this.mWifiServiceHandlerThread = new HandlerThread("WifiService");
            this.mWifiServiceHandlerThread.start();
            this.mWifiServiceHandlerThread.getLooper().setSlowLogThresholdMs((long) 500, (long) 500);
            this.mWifiStateMachineHandlerThread = new HandlerThread("WifiStateMachine");
            this.mWifiStateMachineHandlerThread.start();
            Looper wifiStateMachineLooper = this.mWifiStateMachineHandlerThread.getLooper();
            this.mWifiStateMachineHandlerThread.getLooper().setSlowLogThresholdMs((long) 500, (long) 500);
            this.mCarrierNetworkConfig = new CarrierNetworkConfig(this.mContext, this.mWifiServiceHandlerThread.getLooper(), this.mFrameworkFacade);
            WifiAwareMetrics awareMetrics = new WifiAwareMetrics(this.mClock);
            RttMetrics rttMetrics = new RttMetrics(this.mClock);
            this.mWifiMetrics = new WifiMetrics(this.mClock, wifiStateMachineLooper, awareMetrics, rttMetrics);
            HwWifiServiceFactory.initWifiCHRService(this.mContext);
            this.mWifiMonitor = new WifiMonitor(this);
            this.mHalDeviceManager = new HalDeviceManager(this.mClock);
            this.mWifiVendorHal = new WifiVendorHal(this.mHalDeviceManager, this.mWifiStateMachineHandlerThread.getLooper());
            this.mSupplicantStaIfaceHal = new SupplicantStaIfaceHal(this.mContext, this.mWifiMonitor);
            this.mHostapdHal = new HostapdHal(this.mContext);
            this.mWificondControl = new WificondControl(this, this.mWifiMonitor, this.mCarrierNetworkConfig);
            this.mNwManagementService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
            WifiNative wifiNative = new WifiNative(this.mWifiVendorHal, this.mSupplicantStaIfaceHal, this.mHostapdHal, this.mWificondControl, this.mWifiMonitor, this.mNwManagementService, this.mPropertyService, this.mWifiMetrics);
            this.mWifiNative = wifiNative;
            this.mWifiP2pMonitor = new WifiP2pMonitor(this);
            this.mSupplicantP2pIfaceHal = new SupplicantP2pIfaceHal(this.mWifiP2pMonitor);
            this.mWifiP2pNative = new WifiP2pNative(this.mSupplicantP2pIfaceHal, this.mHalDeviceManager);
            this.mTrafficPoller = new WifiTrafficPoller(this.mContext, this.mWifiServiceHandlerThread.getLooper(), this.mWifiNative);
            this.mCountryCode = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiCountryCode(this.mContext, this.mWifiNative, SystemProperties.get(BOOT_DEFAULT_WIFI_COUNTRY_CODE), this.mContext.getResources().getBoolean(17957085));
            this.mWifiApConfigStore = new WifiApConfigStore(this.mContext, this.mBackupManagerProxy);
            this.mWifiKeyStore = new WifiKeyStore(this.mKeyStore);
            this.mWifiConfigStore = new WifiConfigStore(this.mContext, wifiStateMachineLooper, this.mClock, WifiConfigStore.createSharedFile());
            DelayedDiskWrite writer = new DelayedDiskWrite();
            this.mWifiNetworkHistory = new WifiNetworkHistory(this.mContext, writer);
            this.mIpConfigStore = new IpConfigStore(writer);
            this.mWifiConfigStoreLegacy = new WifiConfigStoreLegacy(this.mWifiNetworkHistory, this.mWifiNative, new WifiConfigStoreLegacy.IpConfigStoreWrapper(), new LegacyPasspointConfigParser());
            DelayedDiskWrite writer2 = writer;
            this.mWifiConfigManager = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiConfigManager(this.mContext, this.mClock, UserManager.get(this.mContext), TelephonyManager.from(this.mContext), this.mWifiKeyStore, this.mWifiConfigStore, this.mWifiConfigStoreLegacy, this.mWifiPermissionsUtil, this.mWifiPermissionsWrapper, new NetworkListStoreData(this.mContext), new DeletedEphemeralSsidsStoreData());
            this.mWifiMetrics.setWifiConfigManager(this.mWifiConfigManager);
            this.mWifiConnectivityHelper = new WifiConnectivityHelper(this.mWifiNative);
            this.mConnectivityLocalLog = new LocalLog(ActivityManager.isLowRamDeviceStatic() ? 256 : 512);
            this.mScoringParams = new ScoringParams(this.mContext, this.mFrameworkFacade, new Handler(wifiStateMachineLooper));
            this.mWifiMetrics.setScoringParams(this.mScoringParams);
            WifiNetworkSelector wifiNetworkSelector = new WifiNetworkSelector(this.mContext, this.mScoringParams, this.mWifiConfigManager, this.mClock, this.mConnectivityLocalLog);
            this.mWifiNetworkSelector = wifiNetworkSelector;
            this.mWifiMetrics.setWifiNetworkSelector(this.mWifiNetworkSelector);
            RttMetrics rttMetrics2 = rttMetrics;
            DelayedDiskWrite delayedDiskWrite = writer2;
            ScoredNetworkEvaluator scoredNetworkEvaluator = r0;
            ScoredNetworkEvaluator scoredNetworkEvaluator2 = new ScoredNetworkEvaluator(context2, wifiStateMachineLooper, this.mFrameworkFacade, this.mNetworkScoreManager, this.mWifiConfigManager, this.mConnectivityLocalLog, this.mWifiNetworkScoreCache, this.mWifiPermissionsUtil);
            this.mScoredNetworkEvaluator = scoredNetworkEvaluator;
            this.mSimAccessor = new SIMAccessor(this.mContext);
            PasspointManager passpointManager = new PasspointManager(this.mContext, this.mWifiNative, this.mWifiKeyStore, this.mClock, this.mSimAccessor, new PasspointObjectFactory(), this.mWifiConfigManager, this.mWifiConfigStore, this.mWifiMetrics);
            this.mPasspointManager = passpointManager;
            this.mPasspointNetworkEvaluator = new PasspointNetworkEvaluator(this.mPasspointManager, this.mWifiConfigManager, this.mConnectivityLocalLog);
            this.mWifiMetrics.setPasspointManager(this.mPasspointManager);
            ScanRequestProxy scanRequestProxy = new ScanRequestProxy(this.mContext, (AppOpsManager) this.mContext.getSystemService("appops"), (ActivityManager) this.mContext.getSystemService("activity"), this, this.mWifiConfigManager, this.mWifiPermissionsUtil, this.mWifiMetrics, this.mClock);
            this.mScanRequestProxy = scanRequestProxy;
            this.mSarManager = new SarManager(this.mContext, makeTelephonyManager(), wifiStateMachineLooper, this.mWifiNative);
            if (this.mUseRealLogger) {
                WifiDiagnostics wifiDiagnostics = new WifiDiagnostics(this.mContext, this, this.mWifiNative, this.mBuildProperties, new LastMileLogger(this));
                this.mWifiDiagnostics = wifiDiagnostics;
            } else {
                this.mWifiDiagnostics = new BaseWifiDiagnostics(this.mWifiNative);
            }
            RttMetrics rttMetrics3 = rttMetrics2;
            WifiAwareMetrics wifiAwareMetrics = awareMetrics;
            this.mWifiStateMachine = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiStateMachine(this.mContext, this.mFrameworkFacade, wifiStateMachineLooper, UserManager.get(this.mContext), this, this.mBackupManagerProxy, this.mCountryCode, this.mWifiNative, new WrongPasswordNotifier(this.mContext, this.mFrameworkFacade), this.mSarManager);
            this.mSavedNetworkEvaluator = HwWifiServiceFactory.getHwWifiServiceManager().createHwSavedNetworkEvaluator(this.mContext, this.mScoringParams, this.mWifiConfigManager, this.mClock, this.mConnectivityLocalLog, this.mWifiStateMachine, this.mWifiConnectivityHelper);
            WifiStateMachinePrime wifiStateMachinePrime = new WifiStateMachinePrime(this, this.mContext, wifiStateMachineLooper, this.mWifiNative, new DefaultModeManager(this.mContext, wifiStateMachineLooper), this.mBatteryStats);
            this.mWifiStateMachinePrime = wifiStateMachinePrime;
            OpenNetworkNotifier openNetworkNotifier = new OpenNetworkNotifier(this.mContext, this.mWifiStateMachineHandlerThread.getLooper(), this.mFrameworkFacade, this.mClock, this.mWifiMetrics, this.mWifiConfigManager, this.mWifiConfigStore, this.mWifiStateMachine, new ConnectToNetworkNotificationBuilder(this.mContext, this.mFrameworkFacade));
            this.mOpenNetworkNotifier = openNetworkNotifier;
            CarrierNetworkNotifier carrierNetworkNotifier = new CarrierNetworkNotifier(this.mContext, this.mWifiStateMachineHandlerThread.getLooper(), this.mFrameworkFacade, this.mClock, this.mWifiMetrics, this.mWifiConfigManager, this.mWifiConfigStore, this.mWifiStateMachine, new ConnectToNetworkNotificationBuilder(this.mContext, this.mFrameworkFacade));
            this.mCarrierNetworkNotifier = carrierNetworkNotifier;
            Context context3 = this.mContext;
            WakeupOnboarding wakeupOnboarding = new WakeupOnboarding(context3, this.mWifiConfigManager, this.mWifiStateMachineHandlerThread.getLooper(), this.mFrameworkFacade, new WakeupNotificationFactory(this.mContext, this.mFrameworkFacade));
            WakeupController wakeupController = r0;
            WakeupController wakeupController2 = new WakeupController(this.mContext, this.mWifiStateMachineHandlerThread.getLooper(), new WakeupLock(this.mWifiConfigManager, this.mWifiMetrics.getWakeupMetrics(), this.mClock), WakeupEvaluator.fromContext(this.mContext), wakeupOnboarding, this.mWifiConfigManager, this.mWifiConfigStore, this.mWifiMetrics.getWakeupMetrics(), this, this.mFrameworkFacade);
            this.mWakeupController = wakeupController;
            this.mLockManager = new WifiLockManager(this.mContext, BatteryStatsService.getService());
            Looper wifiStateMachineLooper2 = wifiStateMachineLooper;
            this.mWifiController = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiController(this.mContext, this.mWifiStateMachine, wifiStateMachineLooper2, this.mSettingsStore, this.mWifiServiceHandlerThread.getLooper(), this.mFrameworkFacade, this.mWifiStateMachinePrime);
            this.mSelfRecovery = new SelfRecovery(this.mWifiController, this.mClock);
            WifiLastResortWatchdog wifiLastResortWatchdog = new WifiLastResortWatchdog(this.mSelfRecovery, this.mClock, this.mWifiMetrics, this.mWifiStateMachine, wifiStateMachineLooper2);
            this.mWifiLastResortWatchdog = wifiLastResortWatchdog;
            this.mWifiMulticastLockManager = new WifiMulticastLockManager(this.mWifiStateMachine.getMcastLockManagerFilterController(), BatteryStatsService.getService());
        } else {
            throw new IllegalStateException("WifiInjector was already created, use getInstance instead.");
        }
    }

    public static WifiInjector getInstance() {
        if (sWifiInjector != null) {
            return sWifiInjector;
        }
        throw new IllegalStateException("Attempted to retrieve a WifiInjector instance before constructor was called.");
    }

    public void enableVerboseLogging(int verbose) {
        this.mWifiLastResortWatchdog.enableVerboseLogging(verbose);
        this.mWifiBackupRestore.enableVerboseLogging(verbose);
        this.mHalDeviceManager.enableVerboseLogging(verbose);
        this.mScanRequestProxy.enableVerboseLogging(verbose);
        this.mWakeupController.enableVerboseLogging(verbose);
        LogcatLog.enableVerboseLogging(verbose);
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

    public SarManager getSarManager() {
        return this.mSarManager;
    }

    public WifiStateMachine getWifiStateMachine() {
        return this.mWifiStateMachine;
    }

    public Handler getWifiStateMachineHandler() {
        return this.mWifiStateMachine.getHandler();
    }

    public WifiStateMachinePrime getWifiStateMachinePrime() {
        return this.mWifiStateMachinePrime;
    }

    public WifiSettingsStore getWifiSettingsStore() {
        return this.mSettingsStore;
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

    public WakeupController getWakeupController() {
        return this.mWakeupController;
    }

    public ScoringParams getScoringParams() {
        return this.mScoringParams;
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

    public SoftApManager makeSoftApManager(WifiManager.SoftApCallback callback, SoftApModeConfiguration config) {
        return HwWifiServiceFactory.getHwWifiServiceManager().createHwSoftApManager(this.mContext, this.mWifiStateMachineHandlerThread.getLooper(), this.mFrameworkFacade, this.mWifiNative, this.mCountryCode.getCountryCodeSentToDriver(), callback, this.mWifiApConfigStore, config, this.mWifiMetrics);
    }

    public ScanOnlyModeManager makeScanOnlyModeManager(ScanOnlyModeManager.Listener listener) {
        ScanOnlyModeManager scanOnlyModeManager = new ScanOnlyModeManager(this.mContext, this.mWifiStateMachineHandlerThread.getLooper(), this.mWifiNative, listener, this.mWifiMetrics, this.mScanRequestProxy, this.mWakeupController);
        return scanOnlyModeManager;
    }

    public ClientModeManager makeClientModeManager(ClientModeManager.Listener listener) {
        ClientModeManager clientModeManager = new ClientModeManager(this.mContext, this.mWifiStateMachineHandlerThread.getLooper(), this.mWifiNative, listener, this.mWifiMetrics, this.mScanRequestProxy, this.mWifiStateMachine);
        return clientModeManager;
    }

    public WifiLog makeLog(String tag) {
        return new LogcatLog(tag);
    }

    public BaseWifiDiagnostics getWifiDiagnostics() {
        return this.mWifiDiagnostics;
    }

    public synchronized WifiScanner getWifiScanner() {
        if (this.mWifiScanner == null) {
            this.mWifiScanner = new WifiScanner(this.mContext, IWifiScanner.Stub.asInterface(ServiceManager.getService("wifiscanner")), this.mWifiStateMachineHandlerThread.getLooper());
        }
        return this.mWifiScanner;
    }

    public WifiConnectivityManager makeWifiConnectivityManager(WifiInfo wifiInfo, boolean hasConnectionRequests) {
        HwWifiServiceManager hwWifiServiceManager = HwWifiServiceFactory.getHwWifiServiceManager();
        Context context = this.mContext;
        ScoringParams scoringParams = getScoringParams();
        WifiStateMachine wifiStateMachine = this.mWifiStateMachine;
        WifiScanner wifiScanner = getWifiScanner();
        WifiConfigManager wifiConfigManager = this.mWifiConfigManager;
        WifiNetworkSelector wifiNetworkSelector = this.mWifiNetworkSelector;
        WifiConnectivityHelper wifiConnectivityHelper = this.mWifiConnectivityHelper;
        WifiLastResortWatchdog wifiLastResortWatchdog = this.mWifiLastResortWatchdog;
        OpenNetworkNotifier openNetworkNotifier = this.mOpenNetworkNotifier;
        CarrierNetworkNotifier carrierNetworkNotifier = this.mCarrierNetworkNotifier;
        CarrierNetworkConfig carrierNetworkConfig = this.mCarrierNetworkConfig;
        WifiMetrics wifiMetrics = this.mWifiMetrics;
        Looper looper = this.mWifiStateMachineHandlerThread.getLooper();
        Clock clock = this.mClock;
        return hwWifiServiceManager.createHwWifiConnectivityManager(context, scoringParams, wifiStateMachine, wifiScanner, wifiConfigManager, wifiInfo, wifiNetworkSelector, wifiConnectivityHelper, wifiLastResortWatchdog, openNetworkNotifier, carrierNetworkNotifier, carrierNetworkConfig, wifiMetrics, looper, clock, this.mConnectivityLocalLog, hasConnectionRequests, this.mFrameworkFacade, this.mSavedNetworkEvaluator, this.mScoredNetworkEvaluator, this.mPasspointNetworkEvaluator);
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

    public HandlerThread getRttHandlerThread() {
        if (this.mRttHandlerThread == null) {
            this.mRttHandlerThread = new HandlerThread("wifiRttService");
            this.mRttHandlerThread.start();
        }
        return this.mRttHandlerThread;
    }

    public HalDeviceManager getHalDeviceManager() {
        return this.mHalDeviceManager;
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
        writer.println("================");
        writer.println("======WifiService stackTraces======");
        for (StackTraceElement stackTrace : this.mWifiServiceHandlerThread.getStackTrace()) {
            writer.println(stackTrace);
        }
        writer.println("======WifiStateMachine stackTraces======");
        for (StackTraceElement stackTrace2 : this.mWifiStateMachineHandlerThread.getStackTrace()) {
            writer.println(stackTrace2);
        }
        writer.println("================");
    }

    public PowerProfile getPowerProfile() {
        return new PowerProfile(this.mContext, false);
    }

    public ScanRequestProxy getScanRequestProxy() {
        return this.mScanRequestProxy;
    }

    public Runtime getJavaRuntime() {
        return Runtime.getRuntime();
    }

    public ActivityManagerService getActivityManagerService() {
        return ActivityManager.getService();
    }

    public void startHwWifiService() {
        Log.e("WifiInjector", "hw wifi start  phase----1");
        this.mWifiStateMachine.setLocalMacAddressFromMacfile();
        Log.e("WifiInjector", "hw wifi start  phase----2");
        this.mWifiController.setupHwSelfCureEngine(this.mContext, this.mWifiStateMachine);
        Log.e("WifiInjector", "hw wifi start  phase----3");
        this.mWifiController.createABSService(this.mContext, this.mWifiStateMachine);
        Log.e("WifiInjector", "hw wifi start  phase----4");
        this.mWifiController.createWiTasService(this.mContext, this.mWifiNative);
    }

    public void reportHwWiTasAntRssi(int index, int rssi) {
        this.mWifiController.reportWiTasAntRssi(index, rssi);
    }
}
