package com.android.server;

import android.app.ActivityThread;
import android.app.INotificationManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteCompatibilityWalFlags;
import android.hsm.HwSystemManager;
import android.net.INetd;
import android.net.INetworkStatsService;
import android.os.BaseBundle;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.FactoryTest;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.IIncidentManager;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Slog;
import android.util.TimingsTraceLog;
import android.view.WindowManager;
import android.vr.VRManagerService;
import com.android.internal.app.ColorDisplayController;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BinderInternal;
import com.android.internal.util.ConcurrentUtils;
import com.android.internal.widget.ILockSettings;
import com.android.server.HwServiceFactory;
import com.android.server.InputMethodManagerService;
import com.android.server.NetworkScoreService;
import com.android.server.TextServicesManagerService;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.audio.AudioService;
import com.android.server.camera.CameraServiceProxy;
import com.android.server.clipboard.ClipboardService;
import com.android.server.connectivity.IpConnectivityMetrics;
import com.android.server.coverage.CoverageService;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import com.android.server.display.ColorDisplayService;
import com.android.server.display.DisplayManagerService;
import com.android.server.dreams.DreamManagerService;
import com.android.server.emergency.EmergencyAffordanceService;
import com.android.server.fingerprint.FingerprintService;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.input.InputManagerService;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.LightsService;
import com.android.server.media.MediaResourceMonitorService;
import com.android.server.media.MediaRouterService;
import com.android.server.media.MediaSessionService;
import com.android.server.media.MediaUpdateService;
import com.android.server.media.dtv.DTVService;
import com.android.server.media.projection.MediaProjectionManagerService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsService;
import com.android.server.net.watchlist.NetworkWatchlistService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oemlock.OemLockService;
import com.android.server.om.OverlayManagerService;
import com.android.server.os.DeviceIdentifiersPolicyService;
import com.android.server.os.HwBootCheck;
import com.android.server.os.HwBootFail;
import com.android.server.os.SchedulingPolicyService;
import com.android.server.pg.PGManagerService;
import com.android.server.pm.BackgroundDexOptService;
import com.android.server.pm.CrossProfileAppsService;
import com.android.server.pm.Installer;
import com.android.server.pm.LauncherAppsService;
import com.android.server.pm.OtaDexoptService;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.ShortcutService;
import com.android.server.pm.UserManagerService;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.power.PowerManagerService;
import com.android.server.power.ShutdownThread;
import com.android.server.restrictions.RestrictionsManagerService;
import com.android.server.security.KeyAttestationApplicationIdProviderService;
import com.android.server.security.KeyChainSystemService;
import com.android.server.soundtrigger.SoundTriggerService;
import com.android.server.stats.StatsCompanionService;
import com.android.server.telecom.TelecomLoaderService;
import com.android.server.textclassifier.TextClassificationManagerService;
import com.android.server.trust.TrustManagerService;
import com.android.server.tv.TvInputManagerService;
import com.android.server.tv.TvRemoteService;
import com.android.server.twilight.TwilightService;
import com.android.server.usage.UsageStatsService;
import com.android.server.utils.LogBufferUtil;
import com.android.server.vr.VrManagerService;
import com.android.server.webkit.WebViewUpdateService;
import com.android.server.wm.WindowManagerService;
import com.huawei.featurelayer.HwFeatureLoader;
import dalvik.system.VMRuntime;
import huawei.android.app.HwCustEmergDataManager;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public final class SystemServer {
    private static final String ACCOUNT_SERVICE_CLASS = "com.android.server.accounts.AccountManagerService$Lifecycle";
    private static final String APPWIDGET_SERVICE_CLASS = "com.android.server.appwidget.AppWidgetService";
    private static final String AUTO_FILL_MANAGER_SERVICE_CLASS = "com.android.server.autofill.AutofillManagerService";
    private static final String BACKUP_MANAGER_SERVICE_CLASS = "com.android.server.backup.BackupManagerService$Lifecycle";
    private static final String BLOCK_MAP_FILE = "/cache/recovery/block.map";
    private static final TimingsTraceLog BOOT_TIMINGS_TRACE_LOG = new TimingsTraceLog(SYSTEM_SERVER_TIMING_TAG, 524288);
    private static final String CAR_SERVICE_HELPER_SERVICE_CLASS = "com.android.internal.car.CarServiceHelperService";
    private static final String COMPANION_DEVICE_MANAGER_SERVICE_CLASS = "com.android.server.companion.CompanionDeviceManagerService";
    private static final String CONTENT_SERVICE_CLASS = "com.android.server.content.ContentService$Lifecycle";
    private static final int DEFAULT_SYSTEM_THEME = 16974803;
    private static final long EARLIEST_SUPPORTED_TIME = 86400000;
    private static final String ENCRYPTED_STATE = "1";
    private static final String ENCRYPTING_STATE = "trigger_restart_min_framework";
    private static final String ETHERNET_SERVICE_CLASS = "com.android.server.ethernet.EthernetService";
    public static final int FIRST_ON_SMART_FHD = 1;
    private static final String IOT_SERVICE_CLASS = "com.google.android.things.services.IoTSystemService";
    private static final String JOB_SCHEDULER_SERVICE_CLASS = "com.android.server.job.JobSchedulerService";
    private static final boolean LOCAL_LOGV = true;
    private static final String LOCK_SETTINGS_SERVICE_CLASS = "com.android.server.locksettings.LockSettingsService$Lifecycle";
    private static final String LOWPAN_SERVICE_CLASS = "com.android.server.lowpan.LowpanService";
    private static final String MIDI_SERVICE_CLASS = "com.android.server.midi.MidiService$Lifecycle";
    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";
    private static final String PRINT_MANAGER_SERVICE_CLASS = "com.android.server.print.PrintManagerService";
    private static final String SEARCH_MANAGER_SERVICE_CLASS = "com.android.server.search.SearchManagerService$Lifecycle";
    private static final String SLICE_MANAGER_SERVICE_CLASS = "com.android.server.slice.SliceManagerService$Lifecycle";
    private static final long SLOW_DELIVERY_THRESHOLD_MS = 200;
    private static final long SLOW_DISPATCH_THRESHOLD_MS = 100;
    private static final long SNAPSHOT_INTERVAL = 3600000;
    private static final String START_HIDL_SERVICES = "StartHidlServices";
    private static final String START_SENSOR_SERVICE = "StartSensorService";
    private static final String STORAGE_MANAGER_SERVICE_CLASS = "com.android.server.StorageManagerService$Lifecycle";
    private static final String STORAGE_STATS_SERVICE_CLASS = "com.android.server.usage.StorageStatsService$Lifecycle";
    private static final String SYSTEM_SERVER_TIMING_ASYNC_TAG = "SystemServerTimingAsync";
    private static final String SYSTEM_SERVER_TIMING_TAG = "SystemServerTiming";
    private static final String TAG = "SystemServer";
    private static final String THERMAL_OBSERVER_CLASS = "com.google.android.clockwork.ThermalObserver";
    private static final String TIME_ZONE_RULES_MANAGER_SERVICE_CLASS = "com.android.server.timezone.RulesManagerService$Lifecycle";
    private static final String UNCRYPT_PACKAGE_FILE = "/cache/recovery/uncrypt_file";
    private static final String USB_SERVICE_CLASS = "com.android.server.usb.UsbService$Lifecycle";
    private static final String VOICE_RECOGNITION_MANAGER_SERVICE_CLASS = "com.android.server.voiceinteraction.VoiceInteractionManagerService";
    private static final String WEAR_CONFIG_SERVICE_CLASS = "com.google.android.clockwork.WearConfigManagerService";
    private static final String WEAR_CONNECTIVITY_SERVICE_CLASS = "com.android.clockwork.connectivity.WearConnectivityService";
    private static final String WEAR_DISPLAY_SERVICE_CLASS = "com.google.android.clockwork.display.WearDisplayService";
    private static final String WEAR_GLOBAL_ACTIONS_SERVICE_CLASS = "com.android.clockwork.globalactions.GlobalActionsService";
    private static final String WEAR_LEFTY_SERVICE_CLASS = "com.google.android.clockwork.lefty.WearLeftyService";
    private static final String WEAR_SIDEKICK_SERVICE_CLASS = "com.google.android.clockwork.sidekick.SidekickService";
    private static final String WEAR_TIME_SERVICE_CLASS = "com.google.android.clockwork.time.WearTimeService";
    private static final String WIFI_AWARE_SERVICE_CLASS = "com.android.server.wifi.aware.WifiAwareService";
    private static final String WIFI_P2P_SERVICE_CLASS = "com.android.server.wifi.p2p.WifiP2pService";
    private static final String WIFI_SERVICE_CLASS = "com.android.server.wifi.WifiService";
    private static final int sMaxBinderThreads = 31;
    private Installer installer;
    private ActivityManagerService mActivityManagerService;
    private ContentResolver mContentResolver;
    private DisplayManagerService mDisplayManagerService;
    private EntropyMixer mEntropyMixer;
    private final int mFactoryTestMode = FactoryTest.getMode();
    private boolean mFirstBoot;
    private boolean mOnlyCore;
    private PGManagerService mPGManagerService;
    private PackageManager mPackageManager;
    private PackageManagerService mPackageManagerService;
    private PowerManagerService mPowerManagerService;
    private Timer mProfilerSnapshotTimer;
    private final boolean mRuntimeRestart;
    private final long mRuntimeStartElapsedTime;
    private final long mRuntimeStartUptime;
    private Future<?> mSensorServiceStart;
    private Context mSystemContext;
    private SystemServiceManager mSystemServiceManager;
    private WebViewUpdateService mWebViewUpdateService;
    private Future<?> mZygotePreload;

    private static native void startHidlServices();

    private static native void startSensorService();

    private static native void startSysSvcCallRecordService();

    public static void main(String[] args) {
        new SystemServer().run();
    }

    public SystemServer() {
        if (this.mFactoryTestMode != 0) {
            Jlog.d(26, "JL_FIRST_BOOT");
        }
        this.mRuntimeRestart = ENCRYPTED_STATE.equals(SystemProperties.get("sys.boot_completed"));
        this.mRuntimeStartElapsedTime = SystemClock.elapsedRealtime();
        this.mRuntimeStartUptime = SystemClock.uptimeMillis();
    }

    /* JADX INFO: finally extract failed */
    private void run() {
        try {
            traceBeginAndSlog("InitBeforeStartServices");
            if (System.currentTimeMillis() < 86400000) {
                Slog.w(TAG, "System clock is before 1970; setting to 1970.");
                SystemClock.setCurrentTimeMillis(86400000);
            }
            if (!SystemProperties.get("persist.sys.language").isEmpty()) {
                SystemProperties.set("persist.sys.locale", Locale.getDefault().toLanguageTag());
                SystemProperties.set("persist.sys.language", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                SystemProperties.set("persist.sys.country", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                SystemProperties.set("persist.sys.localevar", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            }
            Binder.setWarnOnBlocking(true);
            PackageItemInfo.setForceSafeLabels(true);
            SQLiteCompatibilityWalFlags.init(null);
            Slog.i(TAG, "Entered the Android system server!");
            int uptimeMillis = (int) SystemClock.elapsedRealtime();
            EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_SYSTEM_RUN, uptimeMillis);
            if (!this.mRuntimeRestart) {
                MetricsLogger.histogram(null, "boot_system_server_init", uptimeMillis);
                Jlog.d(30, "JL_BOOT_PROGRESS_SYSTEM_RUN");
            }
            SystemProperties.set("persist.sys.dalvik.vm.lib.2", VMRuntime.getRuntime().vmLibrary());
            VMRuntime.getRuntime().clearGrowthLimit();
            VMRuntime.getRuntime().setTargetHeapUtilization(0.8f);
            Build.ensureFingerprintProperty();
            Environment.setUserRequired(true);
            BaseBundle.setShouldDefuse(true);
            Parcel.setStackTraceParceling(true);
            BinderInternal.disableBackgroundScheduling(true);
            BinderInternal.setMaxThreads(31);
            Process.setThreadPriority(-2);
            Process.setCanSelfBackground(false);
            Looper.prepareMainLooper();
            Looper.getMainLooper().setSlowLogThresholdMs(SLOW_DISPATCH_THRESHOLD_MS, SLOW_DELIVERY_THRESHOLD_MS);
            System.loadLibrary("android_servers");
            performPendingShutdown();
            createSystemContext();
            HwFeatureLoader.SystemServiceFeature.loadFeatureFramework(this.mSystemContext);
            this.mSystemServiceManager = new SystemServiceManager(this.mSystemContext);
            this.mSystemServiceManager.setStartInfo(this.mRuntimeRestart, this.mRuntimeStartElapsedTime, this.mRuntimeStartUptime);
            LocalServices.addService(SystemServiceManager.class, this.mSystemServiceManager);
            SystemServerInitThreadPool.get();
            traceEnd();
            try {
                traceBeginAndSlog("StartServices");
                startBootstrapServices();
                startCoreServices();
                startOtherServices();
                SystemServerInitThreadPool.shutdown();
                Slog.i(TAG, "Finish_StartServices");
                traceEnd();
                this.mPackageManagerService.onSystemServiceStartComplete();
                StrictMode.initVmDefaults(null);
                if (!this.mRuntimeRestart && !isFirstBootOrUpgrade()) {
                    int uptimeMillis2 = (int) SystemClock.elapsedRealtime();
                    MetricsLogger.histogram(null, "boot_system_server_ready", uptimeMillis2);
                    if (uptimeMillis2 > 60000) {
                        Slog.wtf(SYSTEM_SERVER_TIMING_TAG, "SystemServer init took too long. uptimeMillis=" + uptimeMillis2);
                    }
                }
                LogBufferUtil.closeLogBufferAsNeed(this.mSystemContext);
                SmartShrinker.reclaim(Process.myPid(), 3);
                Looper.loop();
                throw new RuntimeException("Main thread loop unexpectedly exited");
            } catch (Throwable th) {
                Slog.i(TAG, "Finish_StartServices");
                traceEnd();
                throw th;
            }
        } catch (Throwable th2) {
            traceEnd();
            throw th2;
        }
    }

    private boolean isFirstBootOrUpgrade() {
        return this.mPackageManagerService.isFirstBoot() || this.mPackageManagerService.isUpgrade();
    }

    private void reportWtf(String msg, Throwable e) {
        Slog.w(TAG, "***********************************************");
        Slog.wtf(TAG, "BOOT FAILURE " + msg, e);
    }

    private void performPendingShutdown() {
        final String reason;
        String shutdownAction = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        if (shutdownAction != null && shutdownAction.length() > 0) {
            final boolean reboot = shutdownAction.charAt(0) == '1';
            if (shutdownAction.length() > 1) {
                reason = shutdownAction.substring(1, shutdownAction.length());
            } else {
                reason = null;
            }
            if (reason != null && reason.startsWith("recovery-update")) {
                File packageFile = new File(UNCRYPT_PACKAGE_FILE);
                if (packageFile.exists()) {
                    String filename = null;
                    try {
                        filename = FileUtils.readTextFile(packageFile, 0, null);
                    } catch (IOException e) {
                        Slog.e(TAG, "Error reading uncrypt package file", e);
                    }
                    if (filename != null && filename.startsWith("/data") && !new File(BLOCK_MAP_FILE).exists()) {
                        Slog.e(TAG, "Can't find block map file, uncrypt failed or unexpected runtime restart?");
                        return;
                    }
                }
            }
            Message msg = Message.obtain(UiThread.getHandler(), new Runnable() {
                public void run() {
                    synchronized (this) {
                        ShutdownThread.rebootOrShutdown(null, reboot, reason);
                    }
                }
            });
            msg.setAsynchronous(true);
            UiThread.getHandler().sendMessage(msg);
        }
    }

    private void createSystemContext() {
        ActivityThread activityThread = ActivityThread.systemMain();
        this.mSystemContext = activityThread.getSystemContext();
        this.mSystemContext.setTheme(DEFAULT_SYSTEM_THEME);
        activityThread.getSystemUiContext().setTheme(DEFAULT_SYSTEM_THEME);
    }

    private void startBootstrapServices() {
        Slog.i(TAG, "Reading configuration...");
        traceBeginAndSlog("ReadingSystemConfig");
        SystemServerInitThreadPool.get().submit($$Lambda$YWiwiKm_Qgqb55C6tTuq_n2JzdY.INSTANCE, "ReadingSystemConfig");
        traceEnd();
        traceBeginAndSlog("StartInstaller");
        this.installer = (Installer) this.mSystemServiceManager.startService(Installer.class);
        traceEnd();
        traceBeginAndSlog("DeviceIdentifiersPolicyService");
        this.mSystemServiceManager.startService(DeviceIdentifiersPolicyService.class);
        traceEnd();
        traceBeginAndSlog("StartActivityManager");
        this.mActivityManagerService = ((ActivityManagerService.Lifecycle) this.mSystemServiceManager.startService(ActivityManagerService.Lifecycle.class)).getService();
        this.mActivityManagerService.setSystemServiceManager(this.mSystemServiceManager);
        this.mActivityManagerService.setInstaller(this.installer);
        traceEnd();
        traceBeginAndSlog("StartPowerManager");
        try {
            this.mPowerManagerService = (PowerManagerService) this.mSystemServiceManager.startService("com.android.server.power.HwPowerManagerService");
        } catch (RuntimeException e) {
            Slog.w(TAG, "create HwPowerManagerService failed");
            this.mPowerManagerService = (PowerManagerService) this.mSystemServiceManager.startService(PowerManagerService.class);
        }
        traceEnd();
        try {
            Slog.i(TAG, "PG Manager service");
            this.mPGManagerService = PGManagerService.getInstance(this.mSystemContext);
        } catch (Throwable e2) {
            reportWtf("PG Manager service", e2);
        }
        traceBeginAndSlog("InitPowerManagement");
        this.mActivityManagerService.initPowerManagement();
        traceEnd();
        traceBeginAndSlog("StartRecoverySystemService");
        this.mSystemServiceManager.startService(RecoverySystemService.class);
        traceEnd();
        RescueParty.noteBoot(this.mSystemContext);
        traceBeginAndSlog("StartLightsService");
        try {
            this.mSystemServiceManager.startService("com.android.server.lights.HwLightsService");
        } catch (RuntimeException e3) {
            Slog.w(TAG, "create HwLightsService failed");
            this.mSystemServiceManager.startService(LightsService.class);
        }
        traceEnd();
        traceBeginAndSlog("StartSidekickService");
        if (SystemProperties.getBoolean("config.enable_sidekick_graphics", false)) {
            this.mSystemServiceManager.startService(WEAR_SIDEKICK_SERVICE_CLASS);
        }
        traceEnd();
        traceBeginAndSlog("StartDisplayManager");
        this.mDisplayManagerService = (DisplayManagerService) this.mSystemServiceManager.startService(DisplayManagerService.class);
        traceEnd();
        try {
            this.mSystemServiceManager.startService("com.android.server.security.HwSecurityService");
            Slog.i(TAG, "HwSecurityService start success");
        } catch (Exception e4) {
            Slog.e(TAG, "can't start HwSecurityService service");
        }
        traceBeginAndSlog("WaitForDisplay");
        this.mSystemServiceManager.startBootPhase(100);
        traceEnd();
        String cryptState = SystemProperties.get("vold.decrypt");
        boolean z = true;
        if (ENCRYPTING_STATE.equals(cryptState)) {
            Slog.w(TAG, "Detected encryption in progress - only parsing core apps");
            this.mOnlyCore = true;
        } else if (ENCRYPTED_STATE.equals(cryptState)) {
            Slog.w(TAG, "Device encrypted - only parsing core apps");
            this.mOnlyCore = true;
        }
        HwBootCheck.bootSceneEnd(100);
        HwBootFail.setBootStage(HwBootFail.STAGE_FRAMEWORK_JAR_DEXOPT_START);
        HwBootCheck.bootSceneStart(105, 900000);
        if (!this.mRuntimeRestart) {
            MetricsLogger.histogram(null, "boot_package_manager_init_start", (int) SystemClock.elapsedRealtime());
        }
        HwCustEmergDataManager emergDataManager = HwCustEmergDataManager.getDefault();
        if (emergDataManager != null && emergDataManager.isEmergencyState()) {
            this.mOnlyCore = true;
            if (emergDataManager.isEmergencyMountState()) {
                emergDataManager.backupEmergencyDataFile();
            }
        }
        traceBeginAndSlog("StartPackageManagerService");
        Slog.i(TAG, "Package Manager");
        Context context = this.mSystemContext;
        Installer installer2 = this.installer;
        if (this.mFactoryTestMode == 0) {
            z = false;
        }
        this.mPackageManagerService = PackageManagerService.main(context, installer2, z, this.mOnlyCore);
        this.mFirstBoot = this.mPackageManagerService.isFirstBoot();
        this.mPackageManager = this.mSystemContext.getPackageManager();
        Slog.i(TAG, "Finish_StartPackageManagerService");
        traceEnd();
        if (!this.mRuntimeRestart && !isFirstBootOrUpgrade()) {
            MetricsLogger.histogram(null, "boot_package_manager_init_ready", (int) SystemClock.elapsedRealtime());
        }
        HwBootFail.setBootStage(HwBootFail.STAGE_FRAMEWORK_JAR_DEXOPT_END);
        HwBootCheck.bootSceneEnd(105);
        HwBootCheck.addBootInfo("[bootinfo]\nisFirstBoot: " + this.mFirstBoot + "\nisUpgrade: " + this.mPackageManagerService.isUpgrade());
        HwBootCheck.bootSceneStart(101, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
        if (!this.mOnlyCore && !SystemProperties.getBoolean("config.disable_otadexopt", false)) {
            traceBeginAndSlog("StartOtaDexOptService");
            try {
                OtaDexoptService.main(this.mSystemContext, this.mPackageManagerService);
            } catch (Throwable th) {
                traceEnd();
                throw th;
            }
            traceEnd();
        }
        traceBeginAndSlog("StartUserManagerService");
        this.mSystemServiceManager.startService(UserManagerService.LifeCycle.class);
        traceEnd();
        if (this.mFirstBoot && this.mPackageManagerService.isUpgrade()) {
            Jlog.d(26, "JL_FIRST_BOOT");
        }
        traceBeginAndSlog("InitAttributerCache");
        AttributeCache.init(this.mSystemContext);
        traceEnd();
        traceBeginAndSlog("SetSystemProcess");
        this.mActivityManagerService.setSystemProcess();
        traceEnd();
        this.mDisplayManagerService.setupSchedulerPolicies();
        traceBeginAndSlog("StartOverlayManagerService");
        this.mSystemServiceManager.startService((SystemService) new OverlayManagerService(this.mSystemContext, this.installer));
        traceEnd();
        this.mSensorServiceStart = SystemServerInitThreadPool.get().submit($$Lambda$SystemServer$UyrPns7R814gZEylCbDKhe8It4.INSTANCE, START_SENSOR_SERVICE);
    }

    static /* synthetic */ void lambda$startBootstrapServices$0() {
        TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin(START_SENSOR_SERVICE);
        startSensorService();
        traceLog.traceEnd();
    }

    private void startCoreServices() {
        traceBeginAndSlog("StartBatteryService");
        try {
            this.mSystemServiceManager.startService("com.android.server.HwBatteryService");
        } catch (RuntimeException e) {
            Slog.w(TAG, "create HwBatteryService failed");
            this.mSystemServiceManager.startService(BatteryService.class);
        }
        traceEnd();
        traceBeginAndSlog("StartUsageService");
        this.mSystemServiceManager.startService(UsageStatsService.class);
        this.mActivityManagerService.setUsageStatsManager((UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class));
        traceEnd();
        if (this.mPackageManager.hasSystemFeature("android.software.webview")) {
            traceBeginAndSlog("StartWebViewUpdateService");
            this.mWebViewUpdateService = (WebViewUpdateService) this.mSystemServiceManager.startService(WebViewUpdateService.class);
            traceEnd();
        }
        traceBeginAndSlog("StartBinderCallsStatsService");
        BinderCallsStatsService.start();
        traceEnd();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v0, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: com.android.server.wm.WindowManagerService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v1, resolved type: android.os.IBinder} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v8, resolved type: com.android.server.IpSecService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v2, resolved type: android.os.IBinder} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v4, resolved type: android.os.IBinder} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v15, resolved type: com.android.server.wm.WindowManagerService} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v46, resolved type: com.android.server.wm.WindowManagerService} */
    /* JADX WARNING: type inference failed for: r5v51, types: [com.android.server.GraphicsStatsService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r5v68, types: [android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r0v297, types: [com.android.server.HardwarePropertiesManagerService] */
    /* JADX WARNING: type inference failed for: r0v303, types: [com.android.server.SerialService] */
    /* JADX WARNING: type inference failed for: r10v9, types: [android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r5v88, types: [android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r5v95, types: [com.android.server.TrustedUIService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r5v99, types: [com.android.server.UpdateLockService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r5v101, types: [com.android.server.SystemUpdateManagerService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r0v341, types: [com.android.server.NsdService] */
    /* JADX WARNING: type inference failed for: r8v22, types: [android.os.IBinder, android.net.IConnectivityManager] */
    /* JADX WARNING: type inference failed for: r0v384, types: [com.android.server.IpSecService] */
    /* JADX WARNING: type inference failed for: r0v392, types: [com.android.server.statusbar.StatusBarManagerService] */
    /* JADX WARNING: type inference failed for: r10v29, types: [com.android.server.accessibility.AccessibilityManagerService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v17, types: [com.android.server.security.KeyAttestationApplicationIdProviderService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v19, types: [com.android.server.os.SchedulingPolicyService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r8v51, types: [android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r6v6, types: [android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v34, types: [android.os.IBinder, com.android.server.wm.WindowManagerService] */
    /* JADX WARNING: type inference failed for: r5v129, types: [com.android.server.input.InputManagerService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r8v58, types: [com.android.server.media.dtv.DTVService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r0v535, types: [com.android.server.SystemService] */
    /* JADX WARNING: type inference failed for: r8v59 */
    /* JADX WARNING: type inference failed for: r8v60 */
    /* JADX WARNING: type inference failed for: r5v141 */
    /* JADX WARNING: type inference failed for: r5v144 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x033b A[Catch:{ RuntimeException -> 0x0414 }] */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x036b A[Catch:{ RuntimeException -> 0x0414 }] */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x0373 A[Catch:{ RuntimeException -> 0x0414 }] */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x03da A[Catch:{ RuntimeException -> 0x0414 }] */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x0407 A[Catch:{ RuntimeException -> 0x0414 }] */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x04ff  */
    /* JADX WARNING: Removed duplicated region for block: B:209:0x05da  */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x05ee  */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x062e  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0916  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x092c  */
    /* JADX WARNING: Removed duplicated region for block: B:346:0x0968 A[Catch:{ Throwable -> 0x097c }] */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x096d A[Catch:{ Throwable -> 0x097c }] */
    /* JADX WARNING: Removed duplicated region for block: B:369:0x09ae  */
    /* JADX WARNING: Removed duplicated region for block: B:377:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0a12  */
    /* JADX WARNING: Removed duplicated region for block: B:388:0x0a43  */
    /* JADX WARNING: Removed duplicated region for block: B:396:0x0a79  */
    /* JADX WARNING: Removed duplicated region for block: B:408:0x0aa6  */
    /* JADX WARNING: Removed duplicated region for block: B:422:0x0ae4  */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x0b13  */
    /* JADX WARNING: Removed duplicated region for block: B:433:0x0b61  */
    /* JADX WARNING: Removed duplicated region for block: B:441:0x0baf  */
    /* JADX WARNING: Removed duplicated region for block: B:447:0x0bc2  */
    /* JADX WARNING: Removed duplicated region for block: B:452:0x0bde A[Catch:{ Throwable -> 0x0bef }] */
    /* JADX WARNING: Removed duplicated region for block: B:457:0x0bec  */
    /* JADX WARNING: Removed duplicated region for block: B:462:0x0c00  */
    /* JADX WARNING: Removed duplicated region for block: B:490:0x0c8a  */
    /* JADX WARNING: Removed duplicated region for block: B:493:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:496:0x0cbf  */
    /* JADX WARNING: Removed duplicated region for block: B:499:0x0d05  */
    /* JADX WARNING: Removed duplicated region for block: B:507:0x0d41  */
    /* JADX WARNING: Removed duplicated region for block: B:510:0x0d5a  */
    /* JADX WARNING: Removed duplicated region for block: B:524:0x0d98  */
    /* JADX WARNING: Removed duplicated region for block: B:548:0x0dfd  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x01f7  */
    /* JADX WARNING: Removed duplicated region for block: B:560:0x0e6e  */
    /* JADX WARNING: Removed duplicated region for block: B:562:0x0e8e  */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0ea2  */
    /* JADX WARNING: Removed duplicated region for block: B:570:0x0ef8  */
    /* JADX WARNING: Removed duplicated region for block: B:572:0x0f09  */
    /* JADX WARNING: Removed duplicated region for block: B:575:0x0f24  */
    /* JADX WARNING: Removed duplicated region for block: B:578:0x0f4d  */
    /* JADX WARNING: Removed duplicated region for block: B:579:0x0f62  */
    /* JADX WARNING: Removed duplicated region for block: B:582:0x0f8f  */
    /* JADX WARNING: Removed duplicated region for block: B:585:0x0faf A[SYNTHETIC, Splitter:B:585:0x0faf] */
    /* JADX WARNING: Removed duplicated region for block: B:590:0x0fc1  */
    /* JADX WARNING: Removed duplicated region for block: B:598:0x0fdf A[SYNTHETIC, Splitter:B:598:0x0fdf] */
    /* JADX WARNING: Removed duplicated region for block: B:609:0x1022  */
    /* JADX WARNING: Removed duplicated region for block: B:612:0x1054  */
    /* JADX WARNING: Removed duplicated region for block: B:630:0x10c4  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0227  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x02cd A[Catch:{ RuntimeException -> 0x0446 }] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x02cf A[Catch:{ RuntimeException -> 0x0446 }] */
    /* JADX WARNING: Unknown variable types count: 6 */
    private void startOtherServices() {
        boolean z;
        IStorageManager storageManager;
        ConnectivityService connectivity;
        TelephonyRegistry telephonyRegistry;
        boolean tuiEnable;
        InputManagerService inputManager;
        int i;
        VibratorService vibrator;
        WindowManagerService wm;
        MediaRouterService mediaRouter;
        CommonTimeManagementService commonTimeMgmtService;
        CountryDetectorService countryDetector;
        NetworkTimeUpdateService networkTimeUpdater;
        ILockSettings lockSettings;
        INetworkStatsService iNetworkStatsService;
        IBinder ipSecServiceF;
        INetworkManagementService iNetworkManagementService;
        LocationManagerService location;
        ILockSettings lockSettings2;
        boolean safeMode;
        Resources.Theme systemTheme;
        int length;
        int i2;
        int i3;
        ConnectivityService connectivity2;
        IBinder iBinder;
        LocationManagerService location2;
        CountryDetectorService countryDetector2;
        LocationManagerService location3;
        IBinder iBinder2;
        boolean startRulesManagerService;
        CommonTimeManagementService commonTimeMgmtService2;
        MediaRouterService mediaRouter2;
        Class<SystemService> serviceClass;
        MediaRouterService mediaRouter3;
        ? mediaRouterService;
        NetworkTimeUpdateService networkTimeUpdater2;
        HwServiceFactory.IHwAttestationServiceFactory attestation;
        IBinder iBinder3;
        IBinder iBinder4;
        CountryDetectorService countryDetector3;
        ? countryDetectorService;
        LocationManagerService location4;
        HwServiceFactory.IHwLocationManagerService hwLocation;
        ? r5;
        ConnectivityService connectivity3;
        IStorageManager storageManager2;
        InputManagerService inputManager2;
        VibratorService vibrator2;
        AlarmManagerService almService;
        InputManagerService inputManager3;
        IStorageManager storageManager3;
        ConnectivityService connectivity4;
        boolean tuiEnable2;
        ? r8;
        ? vibratorService;
        AlarmManagerService almService2;
        Watchdog watchdog;
        InputManagerService inputManager4;
        AlarmManagerService alarmManagerService;
        InputManagerService inputManagerService;
        AlarmManagerService almService3;
        TelephonyRegistry telephonyRegistry2;
        IStorageManager storageManager4;
        ConnectivityService connectivity5;
        int i4;
        AlarmManagerService almService4;
        VibratorService vibrator3;
        InputManagerService inputManagerService2;
        ? main;
        ? r52;
        TelephonyRegistry telephonyRegistry3;
        VibratorService vibrator4;
        VibratorService vibratorService2;
        Context context = this.mSystemContext;
        INetworkManagementService iNetworkManagementService2 = null;
        IBinder iBinder5 = null;
        INetworkStatsService iNetworkStatsService2 = null;
        ILockSettings iLockSettings = null;
        Object obj = null;
        IBinder iBinder6 = null;
        NetworkTimeUpdateService networkTimeUpdater3 = null;
        HwCustEmergDataManager emergDataManager = HwCustEmergDataManager.getDefault();
        if (emergDataManager != null && !emergDataManager.isEmergencyState()) {
            HwServiceFactory.activePlaceFile();
        }
        boolean disableSystemTextClassifier = SystemProperties.getBoolean("config.disable_systemtextclassifier", false);
        boolean disableCameraService = SystemProperties.getBoolean("config.disable_cameraservice", false);
        boolean disableSlices = SystemProperties.getBoolean("config.disable_slices", false);
        boolean enableLeftyService = SystemProperties.getBoolean("config.enable_lefty", false);
        boolean isEmulator = SystemProperties.get("ro.kernel.qemu").equals(ENCRYPTED_STATE);
        boolean enableRms = SystemProperties.getBoolean("ro.config.enable_rms", false);
        boolean enableIaware = SystemProperties.getBoolean("ro.config.enable_iaware", false);
        boolean tuiEnable3 = SystemProperties.getBoolean("ro.vendor.tui.service", false);
        boolean vrDisplayEnable = SystemProperties.getBoolean("ro.vr_display.service", false);
        boolean dtvEnable = SystemProperties.getBoolean("ro.dtv.service", false);
        boolean isSupportedSecIme = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
        boolean isWatch = context.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        boolean isStartSysSvcCallRecord = "3".equals(SystemProperties.get("ro.logsystem.usertype", "0")) && "true".equals(SystemProperties.get("ro.syssvccallrecord.enable", "false"));
        boolean isStartHwFSMService = !SystemProperties.get("ro.config.hw_fold_disp").isEmpty() || !SystemProperties.get("persist.sys.fold.disp.size").isEmpty();
        if (Build.IS_DEBUGGABLE) {
            z = false;
            if (SystemProperties.getBoolean("debug.crash_system", false)) {
                throw new RuntimeException();
            }
        } else {
            z = false;
        }
        Object obj2 = "SecondaryZygotePreload";
        try {
            try {
                this.mZygotePreload = SystemServerInitThreadPool.get().submit($$Lambda$SystemServer$VBGb9VpEls6bUcVBPwYLtX7qDTs.INSTANCE, "SecondaryZygotePreload");
                traceBeginAndSlog("StartKeyAttestationApplicationIdProviderService");
                ServiceManager.addService("sec_key_att_app_id_provider", new KeyAttestationApplicationIdProviderService(context));
                traceEnd();
                traceBeginAndSlog("StartKeyChainSystemService");
                this.mSystemServiceManager.startService(KeyChainSystemService.class);
                traceEnd();
                traceBeginAndSlog("StartSchedulingPolicyService");
                ServiceManager.addService("scheduling_policy", new SchedulingPolicyService());
                traceEnd();
                traceBeginAndSlog("StartTelecomLoaderService");
                this.mSystemServiceManager.startService(TelecomLoaderService.class);
                traceEnd();
                traceBeginAndSlog("StartTelephonyRegistry");
                Slog.i(TAG, "Telephony Registry");
                if (HwSystemManager.mPermissionEnabled == 0) {
                    try {
                        telephonyRegistry3 = new TelephonyRegistry(context);
                    } catch (RuntimeException e) {
                        e = e;
                        inputManager2 = null;
                        telephonyRegistry = null;
                        HwCustEmergDataManager hwCustEmergDataManager = emergDataManager;
                        storageManager = null;
                        connectivity = null;
                        vibratorService2 = null;
                        almService = null;
                        vibrator4 = vibratorService2;
                        tuiEnable = tuiEnable3;
                        i = 1;
                        vibrator2 = vibrator4;
                        Slog.e("System", "******************************************");
                        Slog.e("System", "************ Failure starting core service", e);
                        AlarmManagerService alarmManagerService2 = almService;
                        wm = obj;
                        inputManager = inputManager2;
                        vibrator = vibrator2;
                        IBinder iBinder7 = null;
                        ILockSettings lockSettings3 = null;
                        if (this.mFactoryTestMode != i) {
                        }
                        traceBeginAndSlog("MakeDisplayReady");
                        wm.displayReady();
                        traceEnd();
                        traceBeginAndSlog("StartStorageManagerService");
                        this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                        storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                        traceEnd();
                        traceBeginAndSlog("StartStorageStatsService");
                        this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                        traceEnd();
                        IStorageManager iStorageManager = storageManager2;
                        traceBeginAndSlog("StartUiModeManager");
                        this.mSystemServiceManager.startService(UiModeManagerService.class);
                        traceEnd();
                        HwBootCheck.bootSceneEnd(101);
                        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                        if (!this.mRuntimeRestart) {
                        }
                        HwBootCheck.bootSceneStart(104, 900000);
                        if (!this.mOnlyCore) {
                        }
                        traceBeginAndSlog("PerformFstrimIfNeeded");
                        this.mPackageManagerService.performFstrimIfNeeded();
                        traceEnd();
                        HwBootCheck.bootSceneEnd(104);
                        HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                        if (this.mFactoryTestMode == 1) {
                        }
                        if (!isWatch) {
                        }
                        MediaProjectionManagerService.sHasStartedInSystemserver = true;
                        if (isWatch) {
                        }
                        if (!disableSlices) {
                        }
                        if (!disableCameraService) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                        }
                        traceBeginAndSlog("StartStatsCompanionService");
                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                        traceEnd();
                        safeMode = wm.detectSafeMode();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        MmsServiceBroker mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                        }
                        if (isStartSysSvcCallRecord) {
                        }
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings2 != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                        this.mSystemServiceManager.startBootPhase(480);
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                        this.mSystemServiceManager.startBootPhase(500);
                        traceEnd();
                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                        wm.systemReady();
                        traceEnd();
                        if (safeMode) {
                        }
                        Configuration config = wm.computeNewConfiguration(0);
                        DisplayMetrics metrics = new DisplayMetrics();
                        WindowManager w = (WindowManager) context.getSystemService("window");
                        w.getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        traceBeginAndSlog("StartDeviceSpecificServices");
                        String[] classes = this.mSystemContext.getResources().getStringArray(17236002);
                        length = classes.length;
                        ILockSettings lockSettings4 = lockSettings2;
                        i2 = 0;
                        WindowManagerService wm2 = wm;
                        while (i2 < length) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                        traceEnd();
                        String[] strArr = classes;
                        VibratorService vibratorService3 = vibrator;
                        Resources.Theme theme = systemTheme;
                        WindowManager windowManager = w;
                        DisplayMetrics displayMetrics = metrics;
                        Configuration configuration = config;
                        boolean z2 = tuiEnable;
                        LocationManagerService locationManagerService = location;
                        ILockSettings iLockSettings2 = lockSettings4;
                        ActivityManagerService activityManagerService = this.mActivityManagerService;
                        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0 = r1;
                        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1 = new Runnable(this, context, wm2, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService, enableIaware, safeMode) {
                            private final /* synthetic */ SystemServer f$0;
                            private final /* synthetic */ Context f$1;
                            private final /* synthetic */ NetworkTimeUpdateService f$10;
                            private final /* synthetic */ CommonTimeManagementService f$11;
                            private final /* synthetic */ InputManagerService f$12;
                            private final /* synthetic */ TelephonyRegistry f$13;
                            private final /* synthetic */ MediaRouterService f$14;
                            private final /* synthetic */ MmsServiceBroker f$15;
                            private final /* synthetic */ boolean f$16;
                            private final /* synthetic */ boolean f$17;
                            private final /* synthetic */ WindowManagerService f$2;
                            private final /* synthetic */ NetworkManagementService f$3;
                            private final /* synthetic */ NetworkPolicyManagerService f$4;
                            private final /* synthetic */ IpSecService f$5;
                            private final /* synthetic */ NetworkStatsService f$6;
                            private final /* synthetic */ ConnectivityService f$7;
                            private final /* synthetic */ LocationManagerService f$8;
                            private final /* synthetic */ CountryDetectorService f$9;

                            {
                                this.f$0 = r3;
                                this.f$1 = r4;
                                this.f$2 = r5;
                                this.f$3 = r6;
                                this.f$4 = r7;
                                this.f$5 = r8;
                                this.f$6 = r9;
                                this.f$7 = r10;
                                this.f$8 = r11;
                                this.f$9 = r12;
                                this.f$10 = r13;
                                this.f$11 = r14;
                                this.f$12 = r15;
                                this.f$13 = r16;
                                this.f$14 = r17;
                                this.f$15 = r18;
                                this.f$16 = r19;
                                this.f$17 = r20;
                            }

                            public final void run() {
                                SystemServer systemServer = this.f$0;
                                Context context = this.f$1;
                                WindowManagerService windowManagerService = this.f$2;
                                NetworkManagementService networkManagementService = this.f$3;
                                NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                IpSecService ipSecService = this.f$5;
                                NetworkStatsService networkStatsService = this.f$6;
                                ConnectivityService connectivityService = this.f$7;
                                LocationManagerService locationManagerService = this.f$8;
                                CountryDetectorService countryDetectorService = this.f$9;
                                NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                CommonTimeManagementService commonTimeManagementService = this.f$11;
                                InputManagerService inputManagerService = this.f$12;
                                TelephonyRegistry telephonyRegistry = this.f$13;
                                MediaRouterService mediaRouterService = this.f$14;
                                MmsServiceBroker mmsServiceBroker = this.f$15;
                                boolean z = this.f$16;
                                boolean z2 = z;
                                SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                            }
                        };
                        activityManagerService.systemReady(r0, BOOT_TIMINGS_TRACE_LOG);
                    }
                } else {
                    HwServiceFactory.IHwTelephonyRegistry itr = HwServiceFactory.getHwTelephonyRegistry();
                    if (itr != null) {
                        r8 = itr.getInstance(context);
                        ServiceManager.addService("telephony.registry", r8);
                        traceEnd();
                        traceBeginAndSlog("StartEntropyMixer");
                        this.mEntropyMixer = new EntropyMixer(context);
                        traceEnd();
                        this.mContentResolver = context.getContentResolver();
                        traceBeginAndSlog("StartAccountManagerService");
                        this.mSystemServiceManager.startService(ACCOUNT_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartContentService");
                        this.mSystemServiceManager.startService(CONTENT_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("InstallSystemProviders");
                        this.mActivityManagerService.installSystemProviders();
                        SQLiteCompatibilityWalFlags.reset();
                        traceEnd();
                        traceBeginAndSlog("StartDropBoxManager");
                        this.mSystemServiceManager.startService(DropBoxManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartVibratorService");
                        vibratorService = new VibratorService(context);
                        try {
                            ServiceManager.addService("vibrator", vibratorService);
                            traceEnd();
                            if (isWatch) {
                                try {
                                    traceBeginAndSlog("StartConsumerIrService");
                                    try {
                                        inputManager2 = null;
                                        try {
                                            SystemServerInitThreadPool.get().submit(new Runnable(context) {
                                                private final /* synthetic */ Context f$0;

                                                {
                                                    this.f$0 = r1;
                                                }

                                                public final void run() {
                                                    ServiceManager.addService("consumer_ir", new ConsumerIrService(this.f$0));
                                                }
                                            }, "StartConsumerIrService");
                                            traceEnd();
                                        } catch (RuntimeException e2) {
                                            e = e2;
                                        }
                                    } catch (RuntimeException e3) {
                                        e = e3;
                                        inputManager2 = null;
                                    }
                                } catch (RuntimeException e4) {
                                    e = e4;
                                    inputManager2 = null;
                                    telephonyRegistry = r8;
                                    HwCustEmergDataManager hwCustEmergDataManager2 = emergDataManager;
                                    storageManager = null;
                                    connectivity = null;
                                    almService = null;
                                    tuiEnable = tuiEnable3;
                                    i = 1;
                                    vibrator2 = vibratorService;
                                    Slog.e("System", "******************************************");
                                    Slog.e("System", "************ Failure starting core service", e);
                                    AlarmManagerService alarmManagerService22 = almService;
                                    wm = obj;
                                    inputManager = inputManager2;
                                    vibrator = vibrator2;
                                    IBinder iBinder72 = null;
                                    ILockSettings lockSettings32 = null;
                                    if (this.mFactoryTestMode != i) {
                                    }
                                    traceBeginAndSlog("MakeDisplayReady");
                                    wm.displayReady();
                                    traceEnd();
                                    traceBeginAndSlog("StartStorageManagerService");
                                    this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                                    storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                                    traceEnd();
                                    traceBeginAndSlog("StartStorageStatsService");
                                    this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                                    traceEnd();
                                    IStorageManager iStorageManager2 = storageManager2;
                                    traceBeginAndSlog("StartUiModeManager");
                                    this.mSystemServiceManager.startService(UiModeManagerService.class);
                                    traceEnd();
                                    HwBootCheck.bootSceneEnd(101);
                                    HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                                    if (!this.mRuntimeRestart) {
                                    }
                                    HwBootCheck.bootSceneStart(104, 900000);
                                    if (!this.mOnlyCore) {
                                    }
                                    traceBeginAndSlog("PerformFstrimIfNeeded");
                                    this.mPackageManagerService.performFstrimIfNeeded();
                                    traceEnd();
                                    HwBootCheck.bootSceneEnd(104);
                                    HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                                    HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                                    if (this.mFactoryTestMode == 1) {
                                    }
                                    if (!isWatch) {
                                    }
                                    MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                    if (isWatch) {
                                    }
                                    if (!disableSlices) {
                                    }
                                    if (!disableCameraService) {
                                    }
                                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                    }
                                    traceBeginAndSlog("StartStatsCompanionService");
                                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                    traceEnd();
                                    safeMode = wm.detectSafeMode();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    if (safeMode) {
                                    }
                                    traceBeginAndSlog("StartMmsService");
                                    MmsServiceBroker mmsService2 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                    }
                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                    }
                                    if (isStartSysSvcCallRecord) {
                                    }
                                    traceBeginAndSlog("MakeVibratorServiceReady");
                                    vibrator.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                                    if (lockSettings2 != null) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                    this.mSystemServiceManager.startBootPhase(480);
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                    this.mSystemServiceManager.startBootPhase(500);
                                    traceEnd();
                                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                                    wm.systemReady();
                                    traceEnd();
                                    if (safeMode) {
                                    }
                                    Configuration config2 = wm.computeNewConfiguration(0);
                                    DisplayMetrics metrics2 = new DisplayMetrics();
                                    WindowManager w2 = (WindowManager) context.getSystemService("window");
                                    w2.getDefaultDisplay().getMetrics(metrics2);
                                    context.getResources().updateConfiguration(config2, metrics2);
                                    systemTheme = context.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                    }
                                    traceBeginAndSlog("MakePowerManagerServiceReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    traceEnd();
                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                    traceBeginAndSlog("MakePackageManagerServiceReady");
                                    this.mPackageManagerService.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    traceEnd();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    traceBeginAndSlog("StartDeviceSpecificServices");
                                    String[] classes2 = this.mSystemContext.getResources().getStringArray(17236002);
                                    length = classes2.length;
                                    ILockSettings lockSettings42 = lockSettings2;
                                    i2 = 0;
                                    WindowManagerService wm22 = wm;
                                    while (i2 < length) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                    traceEnd();
                                    String[] strArr2 = classes2;
                                    VibratorService vibratorService32 = vibrator;
                                    Resources.Theme theme2 = systemTheme;
                                    WindowManager windowManager2 = w2;
                                    DisplayMetrics displayMetrics2 = metrics2;
                                    Configuration configuration2 = config2;
                                    boolean z22 = tuiEnable;
                                    LocationManagerService locationManagerService2 = location;
                                    ILockSettings iLockSettings22 = lockSettings42;
                                    ActivityManagerService activityManagerService2 = this.mActivityManagerService;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02 = r1;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12 = new Runnable(this, context, wm22, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2, enableIaware, safeMode) {
                                        private final /* synthetic */ SystemServer f$0;
                                        private final /* synthetic */ Context f$1;
                                        private final /* synthetic */ NetworkTimeUpdateService f$10;
                                        private final /* synthetic */ CommonTimeManagementService f$11;
                                        private final /* synthetic */ InputManagerService f$12;
                                        private final /* synthetic */ TelephonyRegistry f$13;
                                        private final /* synthetic */ MediaRouterService f$14;
                                        private final /* synthetic */ MmsServiceBroker f$15;
                                        private final /* synthetic */ boolean f$16;
                                        private final /* synthetic */ boolean f$17;
                                        private final /* synthetic */ WindowManagerService f$2;
                                        private final /* synthetic */ NetworkManagementService f$3;
                                        private final /* synthetic */ NetworkPolicyManagerService f$4;
                                        private final /* synthetic */ IpSecService f$5;
                                        private final /* synthetic */ NetworkStatsService f$6;
                                        private final /* synthetic */ ConnectivityService f$7;
                                        private final /* synthetic */ LocationManagerService f$8;
                                        private final /* synthetic */ CountryDetectorService f$9;

                                        {
                                            this.f$0 = r3;
                                            this.f$1 = r4;
                                            this.f$2 = r5;
                                            this.f$3 = r6;
                                            this.f$4 = r7;
                                            this.f$5 = r8;
                                            this.f$6 = r9;
                                            this.f$7 = r10;
                                            this.f$8 = r11;
                                            this.f$9 = r12;
                                            this.f$10 = r13;
                                            this.f$11 = r14;
                                            this.f$12 = r15;
                                            this.f$13 = r16;
                                            this.f$14 = r17;
                                            this.f$15 = r18;
                                            this.f$16 = r19;
                                            this.f$17 = r20;
                                        }

                                        public final void run() {
                                            SystemServer systemServer = this.f$0;
                                            Context context = this.f$1;
                                            WindowManagerService windowManagerService = this.f$2;
                                            NetworkManagementService networkManagementService = this.f$3;
                                            NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                            IpSecService ipSecService = this.f$5;
                                            NetworkStatsService networkStatsService = this.f$6;
                                            ConnectivityService connectivityService = this.f$7;
                                            LocationManagerService locationManagerService = this.f$8;
                                            CountryDetectorService countryDetectorService = this.f$9;
                                            NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                            CommonTimeManagementService commonTimeManagementService = this.f$11;
                                            InputManagerService inputManagerService = this.f$12;
                                            TelephonyRegistry telephonyRegistry = this.f$13;
                                            MediaRouterService mediaRouterService = this.f$14;
                                            MmsServiceBroker mmsServiceBroker = this.f$15;
                                            boolean z = this.f$16;
                                            boolean z2 = z;
                                            SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                        }
                                    };
                                    activityManagerService2.systemReady(r02, BOOT_TIMINGS_TRACE_LOG);
                                }
                            } else {
                                inputManager2 = null;
                            }
                        } catch (RuntimeException e5) {
                            e = e5;
                            inputManager2 = null;
                            Object obj3 = vibratorService;
                            telephonyRegistry = r8;
                            HwCustEmergDataManager hwCustEmergDataManager3 = emergDataManager;
                            storageManager = null;
                            connectivity = null;
                            tuiEnable = tuiEnable3;
                            i = 1;
                            almService = null;
                            vibrator2 = vibratorService;
                            Slog.e("System", "******************************************");
                            Slog.e("System", "************ Failure starting core service", e);
                            AlarmManagerService alarmManagerService222 = almService;
                            wm = obj;
                            inputManager = inputManager2;
                            vibrator = vibrator2;
                            IBinder iBinder722 = null;
                            ILockSettings lockSettings322 = null;
                            if (this.mFactoryTestMode != i) {
                            }
                            traceBeginAndSlog("MakeDisplayReady");
                            wm.displayReady();
                            traceEnd();
                            traceBeginAndSlog("StartStorageManagerService");
                            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                            storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                            traceEnd();
                            traceBeginAndSlog("StartStorageStatsService");
                            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                            traceEnd();
                            IStorageManager iStorageManager22 = storageManager2;
                            traceBeginAndSlog("StartUiModeManager");
                            this.mSystemServiceManager.startService(UiModeManagerService.class);
                            traceEnd();
                            HwBootCheck.bootSceneEnd(101);
                            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                            if (!this.mRuntimeRestart) {
                            }
                            HwBootCheck.bootSceneStart(104, 900000);
                            if (!this.mOnlyCore) {
                            }
                            traceBeginAndSlog("PerformFstrimIfNeeded");
                            this.mPackageManagerService.performFstrimIfNeeded();
                            traceEnd();
                            HwBootCheck.bootSceneEnd(104);
                            HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                            if (this.mFactoryTestMode == 1) {
                            }
                            if (!isWatch) {
                            }
                            MediaProjectionManagerService.sHasStartedInSystemserver = true;
                            if (isWatch) {
                            }
                            if (!disableSlices) {
                            }
                            if (!disableCameraService) {
                            }
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                            }
                            traceBeginAndSlog("StartStatsCompanionService");
                            this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                            traceEnd();
                            safeMode = wm.detectSafeMode();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            if (safeMode) {
                            }
                            traceBeginAndSlog("StartMmsService");
                            MmsServiceBroker mmsService22 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                            }
                            if (isStartSysSvcCallRecord) {
                            }
                            traceBeginAndSlog("MakeVibratorServiceReady");
                            vibrator.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                            if (lockSettings2 != null) {
                            }
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                            this.mSystemServiceManager.startBootPhase(480);
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                            this.mSystemServiceManager.startBootPhase(500);
                            traceEnd();
                            traceBeginAndSlog("MakeWindowManagerServiceReady");
                            wm.systemReady();
                            traceEnd();
                            if (safeMode) {
                            }
                            Configuration config22 = wm.computeNewConfiguration(0);
                            DisplayMetrics metrics22 = new DisplayMetrics();
                            WindowManager w22 = (WindowManager) context.getSystemService("window");
                            w22.getDefaultDisplay().getMetrics(metrics22);
                            context.getResources().updateConfiguration(config22, metrics22);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            traceBeginAndSlog("StartDeviceSpecificServices");
                            String[] classes22 = this.mSystemContext.getResources().getStringArray(17236002);
                            length = classes22.length;
                            ILockSettings lockSettings422 = lockSettings2;
                            i2 = 0;
                            WindowManagerService wm222 = wm;
                            while (i2 < length) {
                            }
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                            traceEnd();
                            String[] strArr22 = classes22;
                            VibratorService vibratorService322 = vibrator;
                            Resources.Theme theme22 = systemTheme;
                            WindowManager windowManager22 = w22;
                            DisplayMetrics displayMetrics22 = metrics22;
                            Configuration configuration22 = config22;
                            boolean z222 = tuiEnable;
                            LocationManagerService locationManagerService22 = location;
                            ILockSettings iLockSettings222 = lockSettings422;
                            ActivityManagerService activityManagerService22 = this.mActivityManagerService;
                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022 = r12;
                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122 = new Runnable(this, context, wm222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22, enableIaware, safeMode) {
                                private final /* synthetic */ SystemServer f$0;
                                private final /* synthetic */ Context f$1;
                                private final /* synthetic */ NetworkTimeUpdateService f$10;
                                private final /* synthetic */ CommonTimeManagementService f$11;
                                private final /* synthetic */ InputManagerService f$12;
                                private final /* synthetic */ TelephonyRegistry f$13;
                                private final /* synthetic */ MediaRouterService f$14;
                                private final /* synthetic */ MmsServiceBroker f$15;
                                private final /* synthetic */ boolean f$16;
                                private final /* synthetic */ boolean f$17;
                                private final /* synthetic */ WindowManagerService f$2;
                                private final /* synthetic */ NetworkManagementService f$3;
                                private final /* synthetic */ NetworkPolicyManagerService f$4;
                                private final /* synthetic */ IpSecService f$5;
                                private final /* synthetic */ NetworkStatsService f$6;
                                private final /* synthetic */ ConnectivityService f$7;
                                private final /* synthetic */ LocationManagerService f$8;
                                private final /* synthetic */ CountryDetectorService f$9;

                                {
                                    this.f$0 = r3;
                                    this.f$1 = r4;
                                    this.f$2 = r5;
                                    this.f$3 = r6;
                                    this.f$4 = r7;
                                    this.f$5 = r8;
                                    this.f$6 = r9;
                                    this.f$7 = r10;
                                    this.f$8 = r11;
                                    this.f$9 = r12;
                                    this.f$10 = r13;
                                    this.f$11 = r14;
                                    this.f$12 = r15;
                                    this.f$13 = r16;
                                    this.f$14 = r17;
                                    this.f$15 = r18;
                                    this.f$16 = r19;
                                    this.f$17 = r20;
                                }

                                public final void run() {
                                    SystemServer systemServer = this.f$0;
                                    Context context = this.f$1;
                                    WindowManagerService windowManagerService = this.f$2;
                                    NetworkManagementService networkManagementService = this.f$3;
                                    NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                    IpSecService ipSecService = this.f$5;
                                    NetworkStatsService networkStatsService = this.f$6;
                                    ConnectivityService connectivityService = this.f$7;
                                    LocationManagerService locationManagerService = this.f$8;
                                    CountryDetectorService countryDetectorService = this.f$9;
                                    NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                    CommonTimeManagementService commonTimeManagementService = this.f$11;
                                    InputManagerService inputManagerService = this.f$12;
                                    TelephonyRegistry telephonyRegistry = this.f$13;
                                    MediaRouterService mediaRouterService = this.f$14;
                                    MmsServiceBroker mmsServiceBroker = this.f$15;
                                    boolean z = this.f$16;
                                    boolean z2 = z;
                                    SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                }
                            };
                            activityManagerService22.systemReady(r022, BOOT_TIMINGS_TRACE_LOG);
                        }
                        try {
                            traceBeginAndSlog("StartAlarmManagerService");
                            try {
                                almService2 = this.mSystemServiceManager.startService("com.android.server.HwAlarmManagerService");
                            } catch (Exception e6) {
                                this.mSystemServiceManager.startService(AlarmManagerService.class);
                                almService2 = null;
                            }
                            almService = almService2;
                            try {
                                traceEnd();
                                this.mActivityManagerService.setAlarmManager(almService);
                                traceBeginAndSlog("Init Watchdog");
                                Watchdog watchdog2 = Watchdog.getInstance();
                                watchdog2.init(context, this.mActivityManagerService);
                                traceEnd();
                                traceBeginAndSlog("StartInputManagerService");
                                watchdog = watchdog2;
                                Slog.i(TAG, "Input Manager");
                                inputManager4 = HwServiceFactory.getHwInputManagerService().getInstance(context, null);
                                traceEnd();
                                traceBeginAndSlog("StartHwSysResManagerService");
                                if (enableRms || enableIaware) {
                                    try {
                                        this.mSystemServiceManager.startService("com.android.server.rms.HwSysResManagerService");
                                    } catch (RuntimeException e7) {
                                        e = e7;
                                        telephonyRegistry = r8;
                                        HwCustEmergDataManager hwCustEmergDataManager4 = emergDataManager;
                                        storageManager = null;
                                        connectivity = null;
                                        inputManager2 = inputManager4;
                                        vibrator4 = vibratorService;
                                        tuiEnable = tuiEnable3;
                                        i = 1;
                                        vibrator2 = vibrator4;
                                        Slog.e("System", "******************************************");
                                        Slog.e("System", "************ Failure starting core service", e);
                                        AlarmManagerService alarmManagerService2222 = almService;
                                        wm = obj;
                                        inputManager = inputManager2;
                                        vibrator = vibrator2;
                                        IBinder iBinder7222 = null;
                                        ILockSettings lockSettings3222 = null;
                                        if (this.mFactoryTestMode != i) {
                                        }
                                        traceBeginAndSlog("MakeDisplayReady");
                                        wm.displayReady();
                                        traceEnd();
                                        traceBeginAndSlog("StartStorageManagerService");
                                        this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                                        storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                                        traceEnd();
                                        traceBeginAndSlog("StartStorageStatsService");
                                        this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                                        traceEnd();
                                        IStorageManager iStorageManager222 = storageManager2;
                                        traceBeginAndSlog("StartUiModeManager");
                                        this.mSystemServiceManager.startService(UiModeManagerService.class);
                                        traceEnd();
                                        HwBootCheck.bootSceneEnd(101);
                                        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                                        if (!this.mRuntimeRestart) {
                                        }
                                        HwBootCheck.bootSceneStart(104, 900000);
                                        if (!this.mOnlyCore) {
                                        }
                                        traceBeginAndSlog("PerformFstrimIfNeeded");
                                        this.mPackageManagerService.performFstrimIfNeeded();
                                        traceEnd();
                                        HwBootCheck.bootSceneEnd(104);
                                        HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                                        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                                        if (this.mFactoryTestMode == 1) {
                                        }
                                        if (!isWatch) {
                                        }
                                        MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                        if (isWatch) {
                                        }
                                        if (!disableSlices) {
                                        }
                                        if (!disableCameraService) {
                                        }
                                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                        }
                                        traceBeginAndSlog("StartStatsCompanionService");
                                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                        traceEnd();
                                        safeMode = wm.detectSafeMode();
                                        this.mSystemServiceManager.setSafeMode(safeMode);
                                        if (safeMode) {
                                        }
                                        traceBeginAndSlog("StartMmsService");
                                        MmsServiceBroker mmsService222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                        traceEnd();
                                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                        }
                                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                        }
                                        if (isStartSysSvcCallRecord) {
                                        }
                                        traceBeginAndSlog("MakeVibratorServiceReady");
                                        vibrator.systemReady();
                                        traceEnd();
                                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                                        if (lockSettings2 != null) {
                                        }
                                        traceEnd();
                                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                        this.mSystemServiceManager.startBootPhase(480);
                                        traceEnd();
                                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                        this.mSystemServiceManager.startBootPhase(500);
                                        traceEnd();
                                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                                        wm.systemReady();
                                        traceEnd();
                                        if (safeMode) {
                                        }
                                        Configuration config222 = wm.computeNewConfiguration(0);
                                        DisplayMetrics metrics222 = new DisplayMetrics();
                                        WindowManager w222 = (WindowManager) context.getSystemService("window");
                                        w222.getDefaultDisplay().getMetrics(metrics222);
                                        context.getResources().updateConfiguration(config222, metrics222);
                                        systemTheme = context.getTheme();
                                        if (systemTheme.getChangingConfigurations() != 0) {
                                        }
                                        traceBeginAndSlog("MakePowerManagerServiceReady");
                                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                        traceEnd();
                                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                        traceBeginAndSlog("MakePackageManagerServiceReady");
                                        this.mPackageManagerService.systemReady();
                                        traceEnd();
                                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                        traceEnd();
                                        this.mSystemServiceManager.setSafeMode(safeMode);
                                        traceBeginAndSlog("StartDeviceSpecificServices");
                                        String[] classes222 = this.mSystemContext.getResources().getStringArray(17236002);
                                        length = classes222.length;
                                        ILockSettings lockSettings4222 = lockSettings2;
                                        i2 = 0;
                                        WindowManagerService wm2222 = wm;
                                        while (i2 < length) {
                                        }
                                        traceEnd();
                                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                        traceEnd();
                                        String[] strArr222 = classes222;
                                        VibratorService vibratorService3222 = vibrator;
                                        Resources.Theme theme222 = systemTheme;
                                        WindowManager windowManager222 = w222;
                                        DisplayMetrics displayMetrics222 = metrics222;
                                        Configuration configuration222 = config222;
                                        boolean z2222 = tuiEnable;
                                        LocationManagerService locationManagerService222 = location;
                                        ILockSettings iLockSettings2222 = lockSettings4222;
                                        ActivityManagerService activityManagerService222 = this.mActivityManagerService;
                                        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222 = r122;
                                        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222 = new Runnable(this, context, wm2222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222, enableIaware, safeMode) {
                                            private final /* synthetic */ SystemServer f$0;
                                            private final /* synthetic */ Context f$1;
                                            private final /* synthetic */ NetworkTimeUpdateService f$10;
                                            private final /* synthetic */ CommonTimeManagementService f$11;
                                            private final /* synthetic */ InputManagerService f$12;
                                            private final /* synthetic */ TelephonyRegistry f$13;
                                            private final /* synthetic */ MediaRouterService f$14;
                                            private final /* synthetic */ MmsServiceBroker f$15;
                                            private final /* synthetic */ boolean f$16;
                                            private final /* synthetic */ boolean f$17;
                                            private final /* synthetic */ WindowManagerService f$2;
                                            private final /* synthetic */ NetworkManagementService f$3;
                                            private final /* synthetic */ NetworkPolicyManagerService f$4;
                                            private final /* synthetic */ IpSecService f$5;
                                            private final /* synthetic */ NetworkStatsService f$6;
                                            private final /* synthetic */ ConnectivityService f$7;
                                            private final /* synthetic */ LocationManagerService f$8;
                                            private final /* synthetic */ CountryDetectorService f$9;

                                            {
                                                this.f$0 = r3;
                                                this.f$1 = r4;
                                                this.f$2 = r5;
                                                this.f$3 = r6;
                                                this.f$4 = r7;
                                                this.f$5 = r8;
                                                this.f$6 = r9;
                                                this.f$7 = r10;
                                                this.f$8 = r11;
                                                this.f$9 = r12;
                                                this.f$10 = r13;
                                                this.f$11 = r14;
                                                this.f$12 = r15;
                                                this.f$13 = r16;
                                                this.f$14 = r17;
                                                this.f$15 = r18;
                                                this.f$16 = r19;
                                                this.f$17 = r20;
                                            }

                                            public final void run() {
                                                SystemServer systemServer = this.f$0;
                                                Context context = this.f$1;
                                                WindowManagerService windowManagerService = this.f$2;
                                                NetworkManagementService networkManagementService = this.f$3;
                                                NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                                IpSecService ipSecService = this.f$5;
                                                NetworkStatsService networkStatsService = this.f$6;
                                                ConnectivityService connectivityService = this.f$7;
                                                LocationManagerService locationManagerService = this.f$8;
                                                CountryDetectorService countryDetectorService = this.f$9;
                                                NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                                CommonTimeManagementService commonTimeManagementService = this.f$11;
                                                InputManagerService inputManagerService = this.f$12;
                                                TelephonyRegistry telephonyRegistry = this.f$13;
                                                MediaRouterService mediaRouterService = this.f$14;
                                                MmsServiceBroker mmsServiceBroker = this.f$15;
                                                boolean z = this.f$16;
                                                boolean z2 = z;
                                                SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                            }
                                        };
                                        activityManagerService222.systemReady(r0222, BOOT_TIMINGS_TRACE_LOG);
                                    } catch (Throwable e8) {
                                        almService3 = almService;
                                        try {
                                            Slog.e(TAG, e8.toString());
                                        } catch (RuntimeException e9) {
                                            e = e9;
                                            telephonyRegistry2 = r8;
                                            HwCustEmergDataManager hwCustEmergDataManager5 = emergDataManager;
                                            storageManager4 = null;
                                            connectivity5 = null;
                                            inputManagerService = inputManager4;
                                            alarmManagerService = almService3;
                                            AlarmManagerService almService5 = vibratorService;
                                            tuiEnable = tuiEnable3;
                                            i4 = 1;
                                            inputManager2 = inputManagerService;
                                            almService = alarmManagerService;
                                            vibrator2 = vibratorService;
                                            Slog.e("System", "******************************************");
                                            Slog.e("System", "************ Failure starting core service", e);
                                            AlarmManagerService alarmManagerService22222 = almService;
                                            wm = obj;
                                            inputManager = inputManager2;
                                            vibrator = vibrator2;
                                            IBinder iBinder72222 = null;
                                            ILockSettings lockSettings32222 = null;
                                            if (this.mFactoryTestMode != i) {
                                            }
                                            traceBeginAndSlog("MakeDisplayReady");
                                            wm.displayReady();
                                            traceEnd();
                                            traceBeginAndSlog("StartStorageManagerService");
                                            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                                            storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                                            traceEnd();
                                            traceBeginAndSlog("StartStorageStatsService");
                                            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                                            traceEnd();
                                            IStorageManager iStorageManager2222 = storageManager2;
                                            traceBeginAndSlog("StartUiModeManager");
                                            this.mSystemServiceManager.startService(UiModeManagerService.class);
                                            traceEnd();
                                            HwBootCheck.bootSceneEnd(101);
                                            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                                            if (!this.mRuntimeRestart) {
                                            }
                                            HwBootCheck.bootSceneStart(104, 900000);
                                            if (!this.mOnlyCore) {
                                            }
                                            traceBeginAndSlog("PerformFstrimIfNeeded");
                                            this.mPackageManagerService.performFstrimIfNeeded();
                                            traceEnd();
                                            HwBootCheck.bootSceneEnd(104);
                                            HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                                            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                                            if (this.mFactoryTestMode == 1) {
                                            }
                                            if (!isWatch) {
                                            }
                                            MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                            if (isWatch) {
                                            }
                                            if (!disableSlices) {
                                            }
                                            if (!disableCameraService) {
                                            }
                                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                            }
                                            traceBeginAndSlog("StartStatsCompanionService");
                                            this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                            traceEnd();
                                            safeMode = wm.detectSafeMode();
                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                            if (safeMode) {
                                            }
                                            traceBeginAndSlog("StartMmsService");
                                            MmsServiceBroker mmsService2222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                            traceEnd();
                                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                            }
                                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                            }
                                            if (isStartSysSvcCallRecord) {
                                            }
                                            traceBeginAndSlog("MakeVibratorServiceReady");
                                            vibrator.systemReady();
                                            traceEnd();
                                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                                            if (lockSettings2 != null) {
                                            }
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                            this.mSystemServiceManager.startBootPhase(480);
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                            this.mSystemServiceManager.startBootPhase(500);
                                            traceEnd();
                                            traceBeginAndSlog("MakeWindowManagerServiceReady");
                                            wm.systemReady();
                                            traceEnd();
                                            if (safeMode) {
                                            }
                                            Configuration config2222 = wm.computeNewConfiguration(0);
                                            DisplayMetrics metrics2222 = new DisplayMetrics();
                                            WindowManager w2222 = (WindowManager) context.getSystemService("window");
                                            w2222.getDefaultDisplay().getMetrics(metrics2222);
                                            context.getResources().updateConfiguration(config2222, metrics2222);
                                            systemTheme = context.getTheme();
                                            if (systemTheme.getChangingConfigurations() != 0) {
                                            }
                                            traceBeginAndSlog("MakePowerManagerServiceReady");
                                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                            traceEnd();
                                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                            traceBeginAndSlog("MakePackageManagerServiceReady");
                                            this.mPackageManagerService.systemReady();
                                            traceEnd();
                                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                            traceEnd();
                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                            traceBeginAndSlog("StartDeviceSpecificServices");
                                            String[] classes2222 = this.mSystemContext.getResources().getStringArray(17236002);
                                            length = classes2222.length;
                                            ILockSettings lockSettings42222 = lockSettings2;
                                            i2 = 0;
                                            WindowManagerService wm22222 = wm;
                                            while (i2 < length) {
                                            }
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                            traceEnd();
                                            String[] strArr2222 = classes2222;
                                            VibratorService vibratorService32222 = vibrator;
                                            Resources.Theme theme2222 = systemTheme;
                                            WindowManager windowManager2222 = w2222;
                                            DisplayMetrics displayMetrics2222 = metrics2222;
                                            Configuration configuration2222 = config2222;
                                            boolean z22222 = tuiEnable;
                                            LocationManagerService locationManagerService2222 = location;
                                            ILockSettings iLockSettings22222 = lockSettings42222;
                                            ActivityManagerService activityManagerService2222 = this.mActivityManagerService;
                                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222 = r1222;
                                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222 = new Runnable(this, context, wm22222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222, enableIaware, safeMode) {
                                                private final /* synthetic */ SystemServer f$0;
                                                private final /* synthetic */ Context f$1;
                                                private final /* synthetic */ NetworkTimeUpdateService f$10;
                                                private final /* synthetic */ CommonTimeManagementService f$11;
                                                private final /* synthetic */ InputManagerService f$12;
                                                private final /* synthetic */ TelephonyRegistry f$13;
                                                private final /* synthetic */ MediaRouterService f$14;
                                                private final /* synthetic */ MmsServiceBroker f$15;
                                                private final /* synthetic */ boolean f$16;
                                                private final /* synthetic */ boolean f$17;
                                                private final /* synthetic */ WindowManagerService f$2;
                                                private final /* synthetic */ NetworkManagementService f$3;
                                                private final /* synthetic */ NetworkPolicyManagerService f$4;
                                                private final /* synthetic */ IpSecService f$5;
                                                private final /* synthetic */ NetworkStatsService f$6;
                                                private final /* synthetic */ ConnectivityService f$7;
                                                private final /* synthetic */ LocationManagerService f$8;
                                                private final /* synthetic */ CountryDetectorService f$9;

                                                {
                                                    this.f$0 = r3;
                                                    this.f$1 = r4;
                                                    this.f$2 = r5;
                                                    this.f$3 = r6;
                                                    this.f$4 = r7;
                                                    this.f$5 = r8;
                                                    this.f$6 = r9;
                                                    this.f$7 = r10;
                                                    this.f$8 = r11;
                                                    this.f$9 = r12;
                                                    this.f$10 = r13;
                                                    this.f$11 = r14;
                                                    this.f$12 = r15;
                                                    this.f$13 = r16;
                                                    this.f$14 = r17;
                                                    this.f$15 = r18;
                                                    this.f$16 = r19;
                                                    this.f$17 = r20;
                                                }

                                                public final void run() {
                                                    SystemServer systemServer = this.f$0;
                                                    Context context = this.f$1;
                                                    WindowManagerService windowManagerService = this.f$2;
                                                    NetworkManagementService networkManagementService = this.f$3;
                                                    NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                                    IpSecService ipSecService = this.f$5;
                                                    NetworkStatsService networkStatsService = this.f$6;
                                                    ConnectivityService connectivityService = this.f$7;
                                                    LocationManagerService locationManagerService = this.f$8;
                                                    CountryDetectorService countryDetectorService = this.f$9;
                                                    NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                                    CommonTimeManagementService commonTimeManagementService = this.f$11;
                                                    InputManagerService inputManagerService = this.f$12;
                                                    TelephonyRegistry telephonyRegistry = this.f$13;
                                                    MediaRouterService mediaRouterService = this.f$14;
                                                    MmsServiceBroker mmsServiceBroker = this.f$15;
                                                    boolean z = this.f$16;
                                                    boolean z2 = z;
                                                    SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                                }
                                            };
                                            activityManagerService2222.systemReady(r02222, BOOT_TIMINGS_TRACE_LOG);
                                        }
                                    }
                                }
                                almService3 = almService;
                                traceEnd();
                                traceBeginAndSlog("StartWindowManagerService");
                                ConcurrentUtils.waitForFutureNoInterrupt(this.mSensorServiceStart, START_SENSOR_SERVICE);
                                this.mSensorServiceStart = null;
                                try {
                                    Watchdog watchdog3 = watchdog;
                                    almService4 = almService3;
                                    vibrator3 = vibratorService;
                                    connectivity = null;
                                    tuiEnable = tuiEnable3;
                                    telephonyRegistry = r8;
                                    storageManager = null;
                                    HwCustEmergDataManager hwCustEmergDataManager6 = emergDataManager;
                                    try {
                                        main = WindowManagerService.main(context, inputManager4, this.mFactoryTestMode == 1, !this.mFirstBoot, this.mOnlyCore, HwPolicyFactory.getHwPhoneWindowManager());
                                        try {
                                            initRogMode(main, context);
                                            processMultiDPI(main);
                                            ServiceManager.addService("window", main, false, 17);
                                            r52 = inputManager4;
                                            i = 1;
                                        } catch (RuntimeException e10) {
                                            e = e10;
                                            inputManagerService2 = inputManager4;
                                            i = 1;
                                            obj = main;
                                            inputManager2 = inputManagerService2;
                                            almService = almService4;
                                            vibrator2 = vibrator3;
                                            Slog.e("System", "******************************************");
                                            Slog.e("System", "************ Failure starting core service", e);
                                            AlarmManagerService alarmManagerService222222 = almService;
                                            wm = obj;
                                            inputManager = inputManager2;
                                            vibrator = vibrator2;
                                            IBinder iBinder722222 = null;
                                            ILockSettings lockSettings322222 = null;
                                            if (this.mFactoryTestMode != i) {
                                            }
                                            traceBeginAndSlog("MakeDisplayReady");
                                            wm.displayReady();
                                            traceEnd();
                                            traceBeginAndSlog("StartStorageManagerService");
                                            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                                            storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                                            traceEnd();
                                            traceBeginAndSlog("StartStorageStatsService");
                                            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                                            traceEnd();
                                            IStorageManager iStorageManager22222 = storageManager2;
                                            traceBeginAndSlog("StartUiModeManager");
                                            this.mSystemServiceManager.startService(UiModeManagerService.class);
                                            traceEnd();
                                            HwBootCheck.bootSceneEnd(101);
                                            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                                            if (!this.mRuntimeRestart) {
                                            }
                                            HwBootCheck.bootSceneStart(104, 900000);
                                            if (!this.mOnlyCore) {
                                            }
                                            traceBeginAndSlog("PerformFstrimIfNeeded");
                                            this.mPackageManagerService.performFstrimIfNeeded();
                                            traceEnd();
                                            HwBootCheck.bootSceneEnd(104);
                                            HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                                            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                                            if (this.mFactoryTestMode == 1) {
                                            }
                                            if (!isWatch) {
                                            }
                                            MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                            if (isWatch) {
                                            }
                                            if (!disableSlices) {
                                            }
                                            if (!disableCameraService) {
                                            }
                                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                            }
                                            traceBeginAndSlog("StartStatsCompanionService");
                                            this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                            traceEnd();
                                            safeMode = wm.detectSafeMode();
                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                            if (safeMode) {
                                            }
                                            traceBeginAndSlog("StartMmsService");
                                            MmsServiceBroker mmsService22222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                            traceEnd();
                                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                            }
                                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                            }
                                            if (isStartSysSvcCallRecord) {
                                            }
                                            traceBeginAndSlog("MakeVibratorServiceReady");
                                            vibrator.systemReady();
                                            traceEnd();
                                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                                            if (lockSettings2 != null) {
                                            }
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                            this.mSystemServiceManager.startBootPhase(480);
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                            this.mSystemServiceManager.startBootPhase(500);
                                            traceEnd();
                                            traceBeginAndSlog("MakeWindowManagerServiceReady");
                                            wm.systemReady();
                                            traceEnd();
                                            if (safeMode) {
                                            }
                                            Configuration config22222 = wm.computeNewConfiguration(0);
                                            DisplayMetrics metrics22222 = new DisplayMetrics();
                                            WindowManager w22222 = (WindowManager) context.getSystemService("window");
                                            w22222.getDefaultDisplay().getMetrics(metrics22222);
                                            context.getResources().updateConfiguration(config22222, metrics22222);
                                            systemTheme = context.getTheme();
                                            if (systemTheme.getChangingConfigurations() != 0) {
                                            }
                                            traceBeginAndSlog("MakePowerManagerServiceReady");
                                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                            traceEnd();
                                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                            traceBeginAndSlog("MakePackageManagerServiceReady");
                                            this.mPackageManagerService.systemReady();
                                            traceEnd();
                                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                            traceEnd();
                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                            traceBeginAndSlog("StartDeviceSpecificServices");
                                            String[] classes22222 = this.mSystemContext.getResources().getStringArray(17236002);
                                            length = classes22222.length;
                                            ILockSettings lockSettings422222 = lockSettings2;
                                            i2 = 0;
                                            WindowManagerService wm222222 = wm;
                                            while (i2 < length) {
                                            }
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                            traceEnd();
                                            String[] strArr22222 = classes22222;
                                            VibratorService vibratorService322222 = vibrator;
                                            Resources.Theme theme22222 = systemTheme;
                                            WindowManager windowManager22222 = w22222;
                                            DisplayMetrics displayMetrics22222 = metrics22222;
                                            Configuration configuration22222 = config22222;
                                            boolean z222222 = tuiEnable;
                                            LocationManagerService locationManagerService22222 = location;
                                            ILockSettings iLockSettings222222 = lockSettings422222;
                                            ActivityManagerService activityManagerService22222 = this.mActivityManagerService;
                                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022222 = r12222;
                                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122222 = new Runnable(this, context, wm222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22222, enableIaware, safeMode) {
                                                private final /* synthetic */ SystemServer f$0;
                                                private final /* synthetic */ Context f$1;
                                                private final /* synthetic */ NetworkTimeUpdateService f$10;
                                                private final /* synthetic */ CommonTimeManagementService f$11;
                                                private final /* synthetic */ InputManagerService f$12;
                                                private final /* synthetic */ TelephonyRegistry f$13;
                                                private final /* synthetic */ MediaRouterService f$14;
                                                private final /* synthetic */ MmsServiceBroker f$15;
                                                private final /* synthetic */ boolean f$16;
                                                private final /* synthetic */ boolean f$17;
                                                private final /* synthetic */ WindowManagerService f$2;
                                                private final /* synthetic */ NetworkManagementService f$3;
                                                private final /* synthetic */ NetworkPolicyManagerService f$4;
                                                private final /* synthetic */ IpSecService f$5;
                                                private final /* synthetic */ NetworkStatsService f$6;
                                                private final /* synthetic */ ConnectivityService f$7;
                                                private final /* synthetic */ LocationManagerService f$8;
                                                private final /* synthetic */ CountryDetectorService f$9;

                                                {
                                                    this.f$0 = r3;
                                                    this.f$1 = r4;
                                                    this.f$2 = r5;
                                                    this.f$3 = r6;
                                                    this.f$4 = r7;
                                                    this.f$5 = r8;
                                                    this.f$6 = r9;
                                                    this.f$7 = r10;
                                                    this.f$8 = r11;
                                                    this.f$9 = r12;
                                                    this.f$10 = r13;
                                                    this.f$11 = r14;
                                                    this.f$12 = r15;
                                                    this.f$13 = r16;
                                                    this.f$14 = r17;
                                                    this.f$15 = r18;
                                                    this.f$16 = r19;
                                                    this.f$17 = r20;
                                                }

                                                public final void run() {
                                                    SystemServer systemServer = this.f$0;
                                                    Context context = this.f$1;
                                                    WindowManagerService windowManagerService = this.f$2;
                                                    NetworkManagementService networkManagementService = this.f$3;
                                                    NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                                    IpSecService ipSecService = this.f$5;
                                                    NetworkStatsService networkStatsService = this.f$6;
                                                    ConnectivityService connectivityService = this.f$7;
                                                    LocationManagerService locationManagerService = this.f$8;
                                                    CountryDetectorService countryDetectorService = this.f$9;
                                                    NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                                    CommonTimeManagementService commonTimeManagementService = this.f$11;
                                                    InputManagerService inputManagerService = this.f$12;
                                                    TelephonyRegistry telephonyRegistry = this.f$13;
                                                    MediaRouterService mediaRouterService = this.f$14;
                                                    MmsServiceBroker mmsServiceBroker = this.f$15;
                                                    boolean z = this.f$16;
                                                    boolean z2 = z;
                                                    SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                                }
                                            };
                                            activityManagerService22222.systemReady(r022222, BOOT_TIMINGS_TRACE_LOG);
                                        }
                                    } catch (RuntimeException e11) {
                                        e = e11;
                                        inputManagerService2 = inputManager4;
                                        i = 1;
                                        inputManager2 = inputManagerService2;
                                        almService = almService4;
                                        vibrator2 = vibrator3;
                                        Slog.e("System", "******************************************");
                                        Slog.e("System", "************ Failure starting core service", e);
                                        AlarmManagerService alarmManagerService2222222 = almService;
                                        wm = obj;
                                        inputManager = inputManager2;
                                        vibrator = vibrator2;
                                        IBinder iBinder7222222 = null;
                                        ILockSettings lockSettings3222222 = null;
                                        if (this.mFactoryTestMode != i) {
                                        }
                                        traceBeginAndSlog("MakeDisplayReady");
                                        wm.displayReady();
                                        traceEnd();
                                        traceBeginAndSlog("StartStorageManagerService");
                                        this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                                        storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                                        traceEnd();
                                        traceBeginAndSlog("StartStorageStatsService");
                                        this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                                        traceEnd();
                                        IStorageManager iStorageManager222222 = storageManager2;
                                        traceBeginAndSlog("StartUiModeManager");
                                        this.mSystemServiceManager.startService(UiModeManagerService.class);
                                        traceEnd();
                                        HwBootCheck.bootSceneEnd(101);
                                        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                                        if (!this.mRuntimeRestart) {
                                        }
                                        HwBootCheck.bootSceneStart(104, 900000);
                                        if (!this.mOnlyCore) {
                                        }
                                        traceBeginAndSlog("PerformFstrimIfNeeded");
                                        this.mPackageManagerService.performFstrimIfNeeded();
                                        traceEnd();
                                        HwBootCheck.bootSceneEnd(104);
                                        HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                                        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                                        if (this.mFactoryTestMode == 1) {
                                        }
                                        if (!isWatch) {
                                        }
                                        MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                        if (isWatch) {
                                        }
                                        if (!disableSlices) {
                                        }
                                        if (!disableCameraService) {
                                        }
                                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                        }
                                        traceBeginAndSlog("StartStatsCompanionService");
                                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                        traceEnd();
                                        safeMode = wm.detectSafeMode();
                                        this.mSystemServiceManager.setSafeMode(safeMode);
                                        if (safeMode) {
                                        }
                                        traceBeginAndSlog("StartMmsService");
                                        MmsServiceBroker mmsService222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                        traceEnd();
                                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                        }
                                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                        }
                                        if (isStartSysSvcCallRecord) {
                                        }
                                        traceBeginAndSlog("MakeVibratorServiceReady");
                                        vibrator.systemReady();
                                        traceEnd();
                                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                                        if (lockSettings2 != null) {
                                        }
                                        traceEnd();
                                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                        this.mSystemServiceManager.startBootPhase(480);
                                        traceEnd();
                                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                        this.mSystemServiceManager.startBootPhase(500);
                                        traceEnd();
                                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                                        wm.systemReady();
                                        traceEnd();
                                        if (safeMode) {
                                        }
                                        Configuration config222222 = wm.computeNewConfiguration(0);
                                        DisplayMetrics metrics222222 = new DisplayMetrics();
                                        WindowManager w222222 = (WindowManager) context.getSystemService("window");
                                        w222222.getDefaultDisplay().getMetrics(metrics222222);
                                        context.getResources().updateConfiguration(config222222, metrics222222);
                                        systemTheme = context.getTheme();
                                        if (systemTheme.getChangingConfigurations() != 0) {
                                        }
                                        traceBeginAndSlog("MakePowerManagerServiceReady");
                                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                        traceEnd();
                                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                        traceBeginAndSlog("MakePackageManagerServiceReady");
                                        this.mPackageManagerService.systemReady();
                                        traceEnd();
                                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                        traceEnd();
                                        this.mSystemServiceManager.setSafeMode(safeMode);
                                        traceBeginAndSlog("StartDeviceSpecificServices");
                                        String[] classes222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                        length = classes222222.length;
                                        ILockSettings lockSettings4222222 = lockSettings2;
                                        i2 = 0;
                                        WindowManagerService wm2222222 = wm;
                                        while (i2 < length) {
                                        }
                                        traceEnd();
                                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                        traceEnd();
                                        String[] strArr222222 = classes222222;
                                        VibratorService vibratorService3222222 = vibrator;
                                        Resources.Theme theme222222 = systemTheme;
                                        WindowManager windowManager222222 = w222222;
                                        DisplayMetrics displayMetrics222222 = metrics222222;
                                        Configuration configuration222222 = config222222;
                                        boolean z2222222 = tuiEnable;
                                        LocationManagerService locationManagerService222222 = location;
                                        ILockSettings iLockSettings2222222 = lockSettings4222222;
                                        ActivityManagerService activityManagerService222222 = this.mActivityManagerService;
                                        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222222 = r122222;
                                        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222222 = new Runnable(this, context, wm2222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222222, enableIaware, safeMode) {
                                            private final /* synthetic */ SystemServer f$0;
                                            private final /* synthetic */ Context f$1;
                                            private final /* synthetic */ NetworkTimeUpdateService f$10;
                                            private final /* synthetic */ CommonTimeManagementService f$11;
                                            private final /* synthetic */ InputManagerService f$12;
                                            private final /* synthetic */ TelephonyRegistry f$13;
                                            private final /* synthetic */ MediaRouterService f$14;
                                            private final /* synthetic */ MmsServiceBroker f$15;
                                            private final /* synthetic */ boolean f$16;
                                            private final /* synthetic */ boolean f$17;
                                            private final /* synthetic */ WindowManagerService f$2;
                                            private final /* synthetic */ NetworkManagementService f$3;
                                            private final /* synthetic */ NetworkPolicyManagerService f$4;
                                            private final /* synthetic */ IpSecService f$5;
                                            private final /* synthetic */ NetworkStatsService f$6;
                                            private final /* synthetic */ ConnectivityService f$7;
                                            private final /* synthetic */ LocationManagerService f$8;
                                            private final /* synthetic */ CountryDetectorService f$9;

                                            {
                                                this.f$0 = r3;
                                                this.f$1 = r4;
                                                this.f$2 = r5;
                                                this.f$3 = r6;
                                                this.f$4 = r7;
                                                this.f$5 = r8;
                                                this.f$6 = r9;
                                                this.f$7 = r10;
                                                this.f$8 = r11;
                                                this.f$9 = r12;
                                                this.f$10 = r13;
                                                this.f$11 = r14;
                                                this.f$12 = r15;
                                                this.f$13 = r16;
                                                this.f$14 = r17;
                                                this.f$15 = r18;
                                                this.f$16 = r19;
                                                this.f$17 = r20;
                                            }

                                            public final void run() {
                                                SystemServer systemServer = this.f$0;
                                                Context context = this.f$1;
                                                WindowManagerService windowManagerService = this.f$2;
                                                NetworkManagementService networkManagementService = this.f$3;
                                                NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                                IpSecService ipSecService = this.f$5;
                                                NetworkStatsService networkStatsService = this.f$6;
                                                ConnectivityService connectivityService = this.f$7;
                                                LocationManagerService locationManagerService = this.f$8;
                                                CountryDetectorService countryDetectorService = this.f$9;
                                                NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                                CommonTimeManagementService commonTimeManagementService = this.f$11;
                                                InputManagerService inputManagerService = this.f$12;
                                                TelephonyRegistry telephonyRegistry = this.f$13;
                                                MediaRouterService mediaRouterService = this.f$14;
                                                MmsServiceBroker mmsServiceBroker = this.f$15;
                                                boolean z = this.f$16;
                                                boolean z2 = z;
                                                SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                            }
                                        };
                                        activityManagerService222222.systemReady(r0222222, BOOT_TIMINGS_TRACE_LOG);
                                    }
                                } catch (RuntimeException e12) {
                                    e = e12;
                                    telephonyRegistry2 = r8;
                                    HwCustEmergDataManager hwCustEmergDataManager7 = emergDataManager;
                                    storageManager4 = null;
                                    connectivity5 = null;
                                    inputManagerService = inputManager4;
                                    alarmManagerService = almService3;
                                    AlarmManagerService almService6 = vibratorService;
                                    tuiEnable = tuiEnable3;
                                    i4 = 1;
                                    inputManager2 = inputManagerService;
                                    almService = alarmManagerService;
                                    vibrator2 = vibratorService;
                                    Slog.e("System", "******************************************");
                                    Slog.e("System", "************ Failure starting core service", e);
                                    AlarmManagerService alarmManagerService22222222 = almService;
                                    wm = obj;
                                    inputManager = inputManager2;
                                    vibrator = vibrator2;
                                    IBinder iBinder72222222 = null;
                                    ILockSettings lockSettings32222222 = null;
                                    if (this.mFactoryTestMode != i) {
                                    }
                                    traceBeginAndSlog("MakeDisplayReady");
                                    wm.displayReady();
                                    traceEnd();
                                    traceBeginAndSlog("StartStorageManagerService");
                                    this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                                    storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                                    traceEnd();
                                    traceBeginAndSlog("StartStorageStatsService");
                                    this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                                    traceEnd();
                                    IStorageManager iStorageManager2222222 = storageManager2;
                                    traceBeginAndSlog("StartUiModeManager");
                                    this.mSystemServiceManager.startService(UiModeManagerService.class);
                                    traceEnd();
                                    HwBootCheck.bootSceneEnd(101);
                                    HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                                    if (!this.mRuntimeRestart) {
                                    }
                                    HwBootCheck.bootSceneStart(104, 900000);
                                    if (!this.mOnlyCore) {
                                    }
                                    traceBeginAndSlog("PerformFstrimIfNeeded");
                                    this.mPackageManagerService.performFstrimIfNeeded();
                                    traceEnd();
                                    HwBootCheck.bootSceneEnd(104);
                                    HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                                    HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                                    if (this.mFactoryTestMode == 1) {
                                    }
                                    if (!isWatch) {
                                    }
                                    MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                    if (isWatch) {
                                    }
                                    if (!disableSlices) {
                                    }
                                    if (!disableCameraService) {
                                    }
                                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                    }
                                    traceBeginAndSlog("StartStatsCompanionService");
                                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                    traceEnd();
                                    safeMode = wm.detectSafeMode();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    if (safeMode) {
                                    }
                                    traceBeginAndSlog("StartMmsService");
                                    MmsServiceBroker mmsService2222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                    }
                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                    }
                                    if (isStartSysSvcCallRecord) {
                                    }
                                    traceBeginAndSlog("MakeVibratorServiceReady");
                                    vibrator.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                                    if (lockSettings2 != null) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                    this.mSystemServiceManager.startBootPhase(480);
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                    this.mSystemServiceManager.startBootPhase(500);
                                    traceEnd();
                                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                                    wm.systemReady();
                                    traceEnd();
                                    if (safeMode) {
                                    }
                                    Configuration config2222222 = wm.computeNewConfiguration(0);
                                    DisplayMetrics metrics2222222 = new DisplayMetrics();
                                    WindowManager w2222222 = (WindowManager) context.getSystemService("window");
                                    w2222222.getDefaultDisplay().getMetrics(metrics2222222);
                                    context.getResources().updateConfiguration(config2222222, metrics2222222);
                                    systemTheme = context.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                    }
                                    traceBeginAndSlog("MakePowerManagerServiceReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    traceEnd();
                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                    traceBeginAndSlog("MakePackageManagerServiceReady");
                                    this.mPackageManagerService.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    traceEnd();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    traceBeginAndSlog("StartDeviceSpecificServices");
                                    String[] classes2222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                    length = classes2222222.length;
                                    ILockSettings lockSettings42222222 = lockSettings2;
                                    i2 = 0;
                                    WindowManagerService wm22222222 = wm;
                                    while (i2 < length) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                    traceEnd();
                                    String[] strArr2222222 = classes2222222;
                                    VibratorService vibratorService32222222 = vibrator;
                                    Resources.Theme theme2222222 = systemTheme;
                                    WindowManager windowManager2222222 = w2222222;
                                    DisplayMetrics displayMetrics2222222 = metrics2222222;
                                    Configuration configuration2222222 = config2222222;
                                    boolean z22222222 = tuiEnable;
                                    LocationManagerService locationManagerService2222222 = location;
                                    ILockSettings iLockSettings22222222 = lockSettings42222222;
                                    ActivityManagerService activityManagerService2222222 = this.mActivityManagerService;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222222 = r1222222;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222222 = new Runnable(this, context, wm22222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222222, enableIaware, safeMode) {
                                        private final /* synthetic */ SystemServer f$0;
                                        private final /* synthetic */ Context f$1;
                                        private final /* synthetic */ NetworkTimeUpdateService f$10;
                                        private final /* synthetic */ CommonTimeManagementService f$11;
                                        private final /* synthetic */ InputManagerService f$12;
                                        private final /* synthetic */ TelephonyRegistry f$13;
                                        private final /* synthetic */ MediaRouterService f$14;
                                        private final /* synthetic */ MmsServiceBroker f$15;
                                        private final /* synthetic */ boolean f$16;
                                        private final /* synthetic */ boolean f$17;
                                        private final /* synthetic */ WindowManagerService f$2;
                                        private final /* synthetic */ NetworkManagementService f$3;
                                        private final /* synthetic */ NetworkPolicyManagerService f$4;
                                        private final /* synthetic */ IpSecService f$5;
                                        private final /* synthetic */ NetworkStatsService f$6;
                                        private final /* synthetic */ ConnectivityService f$7;
                                        private final /* synthetic */ LocationManagerService f$8;
                                        private final /* synthetic */ CountryDetectorService f$9;

                                        {
                                            this.f$0 = r3;
                                            this.f$1 = r4;
                                            this.f$2 = r5;
                                            this.f$3 = r6;
                                            this.f$4 = r7;
                                            this.f$5 = r8;
                                            this.f$6 = r9;
                                            this.f$7 = r10;
                                            this.f$8 = r11;
                                            this.f$9 = r12;
                                            this.f$10 = r13;
                                            this.f$11 = r14;
                                            this.f$12 = r15;
                                            this.f$13 = r16;
                                            this.f$14 = r17;
                                            this.f$15 = r18;
                                            this.f$16 = r19;
                                            this.f$17 = r20;
                                        }

                                        public final void run() {
                                            SystemServer systemServer = this.f$0;
                                            Context context = this.f$1;
                                            WindowManagerService windowManagerService = this.f$2;
                                            NetworkManagementService networkManagementService = this.f$3;
                                            NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                            IpSecService ipSecService = this.f$5;
                                            NetworkStatsService networkStatsService = this.f$6;
                                            ConnectivityService connectivityService = this.f$7;
                                            LocationManagerService locationManagerService = this.f$8;
                                            CountryDetectorService countryDetectorService = this.f$9;
                                            NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                            CommonTimeManagementService commonTimeManagementService = this.f$11;
                                            InputManagerService inputManagerService = this.f$12;
                                            TelephonyRegistry telephonyRegistry = this.f$13;
                                            MediaRouterService mediaRouterService = this.f$14;
                                            MmsServiceBroker mmsServiceBroker = this.f$15;
                                            boolean z = this.f$16;
                                            boolean z2 = z;
                                            SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                        }
                                    };
                                    activityManagerService2222222.systemReady(r02222222, BOOT_TIMINGS_TRACE_LOG);
                                }
                            } catch (RuntimeException e13) {
                                e = e13;
                                AlarmManagerService alarmManagerService3 = almService;
                                Object obj4 = vibratorService;
                                telephonyRegistry = r8;
                                HwCustEmergDataManager hwCustEmergDataManager8 = emergDataManager;
                                storageManager = null;
                                connectivity = null;
                                tuiEnable = tuiEnable3;
                                i = 1;
                                vibrator2 = vibratorService;
                                Slog.e("System", "******************************************");
                                Slog.e("System", "************ Failure starting core service", e);
                                AlarmManagerService alarmManagerService222222222 = almService;
                                wm = obj;
                                inputManager = inputManager2;
                                vibrator = vibrator2;
                                IBinder iBinder722222222 = null;
                                ILockSettings lockSettings322222222 = null;
                                if (this.mFactoryTestMode != i) {
                                }
                                traceBeginAndSlog("MakeDisplayReady");
                                wm.displayReady();
                                traceEnd();
                                traceBeginAndSlog("StartStorageManagerService");
                                this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                                storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                                traceEnd();
                                traceBeginAndSlog("StartStorageStatsService");
                                this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                                traceEnd();
                                IStorageManager iStorageManager22222222 = storageManager2;
                                traceBeginAndSlog("StartUiModeManager");
                                this.mSystemServiceManager.startService(UiModeManagerService.class);
                                traceEnd();
                                HwBootCheck.bootSceneEnd(101);
                                HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                                if (!this.mRuntimeRestart) {
                                }
                                HwBootCheck.bootSceneStart(104, 900000);
                                if (!this.mOnlyCore) {
                                }
                                traceBeginAndSlog("PerformFstrimIfNeeded");
                                this.mPackageManagerService.performFstrimIfNeeded();
                                traceEnd();
                                HwBootCheck.bootSceneEnd(104);
                                HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                                HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                                if (this.mFactoryTestMode == 1) {
                                }
                                if (!isWatch) {
                                }
                                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                if (isWatch) {
                                }
                                if (!disableSlices) {
                                }
                                if (!disableCameraService) {
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                }
                                traceBeginAndSlog("StartStatsCompanionService");
                                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                traceEnd();
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                MmsServiceBroker mmsService22222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                if (isStartSysSvcCallRecord) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings2 != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                this.mSystemServiceManager.startBootPhase(480);
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                this.mSystemServiceManager.startBootPhase(500);
                                traceEnd();
                                traceBeginAndSlog("MakeWindowManagerServiceReady");
                                wm.systemReady();
                                traceEnd();
                                if (safeMode) {
                                }
                                Configuration config22222222 = wm.computeNewConfiguration(0);
                                DisplayMetrics metrics22222222 = new DisplayMetrics();
                                WindowManager w22222222 = (WindowManager) context.getSystemService("window");
                                w22222222.getDefaultDisplay().getMetrics(metrics22222222);
                                context.getResources().updateConfiguration(config22222222, metrics22222222);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                traceBeginAndSlog("StartDeviceSpecificServices");
                                String[] classes22222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                length = classes22222222.length;
                                ILockSettings lockSettings422222222 = lockSettings2;
                                i2 = 0;
                                WindowManagerService wm222222222 = wm;
                                while (i2 < length) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                traceEnd();
                                String[] strArr22222222 = classes22222222;
                                VibratorService vibratorService322222222 = vibrator;
                                Resources.Theme theme22222222 = systemTheme;
                                WindowManager windowManager22222222 = w22222222;
                                DisplayMetrics displayMetrics22222222 = metrics22222222;
                                Configuration configuration22222222 = config22222222;
                                boolean z222222222 = tuiEnable;
                                LocationManagerService locationManagerService22222222 = location;
                                ILockSettings iLockSettings222222222 = lockSettings422222222;
                                ActivityManagerService activityManagerService22222222 = this.mActivityManagerService;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022222222 = r12222222;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122222222 = new Runnable(this, context, wm222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22222222, enableIaware, safeMode) {
                                    private final /* synthetic */ SystemServer f$0;
                                    private final /* synthetic */ Context f$1;
                                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                                    private final /* synthetic */ CommonTimeManagementService f$11;
                                    private final /* synthetic */ InputManagerService f$12;
                                    private final /* synthetic */ TelephonyRegistry f$13;
                                    private final /* synthetic */ MediaRouterService f$14;
                                    private final /* synthetic */ MmsServiceBroker f$15;
                                    private final /* synthetic */ boolean f$16;
                                    private final /* synthetic */ boolean f$17;
                                    private final /* synthetic */ WindowManagerService f$2;
                                    private final /* synthetic */ NetworkManagementService f$3;
                                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                                    private final /* synthetic */ IpSecService f$5;
                                    private final /* synthetic */ NetworkStatsService f$6;
                                    private final /* synthetic */ ConnectivityService f$7;
                                    private final /* synthetic */ LocationManagerService f$8;
                                    private final /* synthetic */ CountryDetectorService f$9;

                                    {
                                        this.f$0 = r3;
                                        this.f$1 = r4;
                                        this.f$2 = r5;
                                        this.f$3 = r6;
                                        this.f$4 = r7;
                                        this.f$5 = r8;
                                        this.f$6 = r9;
                                        this.f$7 = r10;
                                        this.f$8 = r11;
                                        this.f$9 = r12;
                                        this.f$10 = r13;
                                        this.f$11 = r14;
                                        this.f$12 = r15;
                                        this.f$13 = r16;
                                        this.f$14 = r17;
                                        this.f$15 = r18;
                                        this.f$16 = r19;
                                        this.f$17 = r20;
                                    }

                                    public final void run() {
                                        SystemServer systemServer = this.f$0;
                                        Context context = this.f$1;
                                        WindowManagerService windowManagerService = this.f$2;
                                        NetworkManagementService networkManagementService = this.f$3;
                                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                        IpSecService ipSecService = this.f$5;
                                        NetworkStatsService networkStatsService = this.f$6;
                                        ConnectivityService connectivityService = this.f$7;
                                        LocationManagerService locationManagerService = this.f$8;
                                        CountryDetectorService countryDetectorService = this.f$9;
                                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                                        InputManagerService inputManagerService = this.f$12;
                                        TelephonyRegistry telephonyRegistry = this.f$13;
                                        MediaRouterService mediaRouterService = this.f$14;
                                        MmsServiceBroker mmsServiceBroker = this.f$15;
                                        boolean z = this.f$16;
                                        boolean z2 = z;
                                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                    }
                                };
                                activityManagerService22222222.systemReady(r022222222, BOOT_TIMINGS_TRACE_LOG);
                            }
                        } catch (RuntimeException e14) {
                            e = e14;
                            Object obj5 = vibratorService;
                            telephonyRegistry = r8;
                            HwCustEmergDataManager hwCustEmergDataManager9 = emergDataManager;
                            storageManager = null;
                            connectivity = null;
                            tuiEnable = tuiEnable3;
                            i = 1;
                            almService = null;
                            vibrator2 = vibratorService;
                            Slog.e("System", "******************************************");
                            Slog.e("System", "************ Failure starting core service", e);
                            AlarmManagerService alarmManagerService2222222222 = almService;
                            wm = obj;
                            inputManager = inputManager2;
                            vibrator = vibrator2;
                            IBinder iBinder7222222222 = null;
                            ILockSettings lockSettings3222222222 = null;
                            if (this.mFactoryTestMode != i) {
                            }
                            traceBeginAndSlog("MakeDisplayReady");
                            wm.displayReady();
                            traceEnd();
                            traceBeginAndSlog("StartStorageManagerService");
                            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                            storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                            traceEnd();
                            traceBeginAndSlog("StartStorageStatsService");
                            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                            traceEnd();
                            IStorageManager iStorageManager222222222 = storageManager2;
                            traceBeginAndSlog("StartUiModeManager");
                            this.mSystemServiceManager.startService(UiModeManagerService.class);
                            traceEnd();
                            HwBootCheck.bootSceneEnd(101);
                            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                            if (!this.mRuntimeRestart) {
                            }
                            HwBootCheck.bootSceneStart(104, 900000);
                            if (!this.mOnlyCore) {
                            }
                            traceBeginAndSlog("PerformFstrimIfNeeded");
                            this.mPackageManagerService.performFstrimIfNeeded();
                            traceEnd();
                            HwBootCheck.bootSceneEnd(104);
                            HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                            if (this.mFactoryTestMode == 1) {
                            }
                            if (!isWatch) {
                            }
                            MediaProjectionManagerService.sHasStartedInSystemserver = true;
                            if (isWatch) {
                            }
                            if (!disableSlices) {
                            }
                            if (!disableCameraService) {
                            }
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                            }
                            traceBeginAndSlog("StartStatsCompanionService");
                            this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                            traceEnd();
                            safeMode = wm.detectSafeMode();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            if (safeMode) {
                            }
                            traceBeginAndSlog("StartMmsService");
                            MmsServiceBroker mmsService222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                            }
                            if (isStartSysSvcCallRecord) {
                            }
                            traceBeginAndSlog("MakeVibratorServiceReady");
                            vibrator.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                            if (lockSettings2 != null) {
                            }
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                            this.mSystemServiceManager.startBootPhase(480);
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                            this.mSystemServiceManager.startBootPhase(500);
                            traceEnd();
                            traceBeginAndSlog("MakeWindowManagerServiceReady");
                            wm.systemReady();
                            traceEnd();
                            if (safeMode) {
                            }
                            Configuration config222222222 = wm.computeNewConfiguration(0);
                            DisplayMetrics metrics222222222 = new DisplayMetrics();
                            WindowManager w222222222 = (WindowManager) context.getSystemService("window");
                            w222222222.getDefaultDisplay().getMetrics(metrics222222222);
                            context.getResources().updateConfiguration(config222222222, metrics222222222);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            traceBeginAndSlog("StartDeviceSpecificServices");
                            String[] classes222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                            length = classes222222222.length;
                            ILockSettings lockSettings4222222222 = lockSettings2;
                            i2 = 0;
                            WindowManagerService wm2222222222 = wm;
                            while (i2 < length) {
                            }
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                            traceEnd();
                            String[] strArr222222222 = classes222222222;
                            VibratorService vibratorService3222222222 = vibrator;
                            Resources.Theme theme222222222 = systemTheme;
                            WindowManager windowManager222222222 = w222222222;
                            DisplayMetrics displayMetrics222222222 = metrics222222222;
                            Configuration configuration222222222 = config222222222;
                            boolean z2222222222 = tuiEnable;
                            LocationManagerService locationManagerService222222222 = location;
                            ILockSettings iLockSettings2222222222 = lockSettings4222222222;
                            ActivityManagerService activityManagerService222222222 = this.mActivityManagerService;
                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222222222 = r122222222;
                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222222222 = new Runnable(this, context, wm2222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222222222, enableIaware, safeMode) {
                                private final /* synthetic */ SystemServer f$0;
                                private final /* synthetic */ Context f$1;
                                private final /* synthetic */ NetworkTimeUpdateService f$10;
                                private final /* synthetic */ CommonTimeManagementService f$11;
                                private final /* synthetic */ InputManagerService f$12;
                                private final /* synthetic */ TelephonyRegistry f$13;
                                private final /* synthetic */ MediaRouterService f$14;
                                private final /* synthetic */ MmsServiceBroker f$15;
                                private final /* synthetic */ boolean f$16;
                                private final /* synthetic */ boolean f$17;
                                private final /* synthetic */ WindowManagerService f$2;
                                private final /* synthetic */ NetworkManagementService f$3;
                                private final /* synthetic */ NetworkPolicyManagerService f$4;
                                private final /* synthetic */ IpSecService f$5;
                                private final /* synthetic */ NetworkStatsService f$6;
                                private final /* synthetic */ ConnectivityService f$7;
                                private final /* synthetic */ LocationManagerService f$8;
                                private final /* synthetic */ CountryDetectorService f$9;

                                {
                                    this.f$0 = r3;
                                    this.f$1 = r4;
                                    this.f$2 = r5;
                                    this.f$3 = r6;
                                    this.f$4 = r7;
                                    this.f$5 = r8;
                                    this.f$6 = r9;
                                    this.f$7 = r10;
                                    this.f$8 = r11;
                                    this.f$9 = r12;
                                    this.f$10 = r13;
                                    this.f$11 = r14;
                                    this.f$12 = r15;
                                    this.f$13 = r16;
                                    this.f$14 = r17;
                                    this.f$15 = r18;
                                    this.f$16 = r19;
                                    this.f$17 = r20;
                                }

                                public final void run() {
                                    SystemServer systemServer = this.f$0;
                                    Context context = this.f$1;
                                    WindowManagerService windowManagerService = this.f$2;
                                    NetworkManagementService networkManagementService = this.f$3;
                                    NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                    IpSecService ipSecService = this.f$5;
                                    NetworkStatsService networkStatsService = this.f$6;
                                    ConnectivityService connectivityService = this.f$7;
                                    LocationManagerService locationManagerService = this.f$8;
                                    CountryDetectorService countryDetectorService = this.f$9;
                                    NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                    CommonTimeManagementService commonTimeManagementService = this.f$11;
                                    InputManagerService inputManagerService = this.f$12;
                                    TelephonyRegistry telephonyRegistry = this.f$13;
                                    MediaRouterService mediaRouterService = this.f$14;
                                    MmsServiceBroker mmsServiceBroker = this.f$15;
                                    boolean z = this.f$16;
                                    boolean z2 = z;
                                    SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                }
                            };
                            activityManagerService222222222.systemReady(r0222222222, BOOT_TIMINGS_TRACE_LOG);
                        }
                        try {
                            ServiceManager.addService("input", r52, false, 1);
                            traceEnd();
                            traceBeginAndSlog("SetWindowManagerService");
                            this.mActivityManagerService.setWindowManager(main);
                            traceEnd();
                            traceBeginAndSlog("WindowManagerServiceOnInitReady");
                            main.onInitReady();
                            traceEnd();
                            SystemServerInitThreadPool.get().submit($$Lambda$SystemServer$JQH6ND0PqyyiRiz7lXLvUmRhwRM.INSTANCE, START_HIDL_SERVICES);
                            if (!isWatch) {
                                traceBeginAndSlog("StartVrManagerService");
                                this.mSystemServiceManager.startService(VrManagerService.class);
                                traceEnd();
                            }
                            traceBeginAndSlog("StartInputManager");
                            r52.setWindowManagerCallbacks(main.getInputMonitor());
                            r52.start();
                            traceEnd();
                            traceBeginAndSlog("DisplayManagerWindowManagerAndInputReady");
                            this.mDisplayManagerService.windowManagerAndInputReady();
                            traceEnd();
                            if (!isEmulator) {
                                Slog.i(TAG, "No Bluetooth Service (emulator)");
                            } else if (this.mFactoryTestMode == 1) {
                                Slog.i(TAG, "No Bluetooth Service (factory test)");
                            } else if (!context.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
                                Slog.i(TAG, "No Bluetooth Service (Bluetooth Hardware Not Present)");
                            } else {
                                traceBeginAndSlog("StartBluetoothService");
                                this.mSystemServiceManager.startService(BluetoothService.class);
                                traceEnd();
                            }
                            traceBeginAndSlog("IpConnectivityMetrics");
                            this.mSystemServiceManager.startService(IpConnectivityMetrics.class);
                            traceEnd();
                            traceBeginAndSlog("NetworkWatchlistService");
                            this.mSystemServiceManager.startService(NetworkWatchlistService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("PinnerService");
                            ((PinnerService) this.mSystemServiceManager.startService(PinnerService.class)).setInstaller(this.installer);
                            traceEnd();
                            if (dtvEnable) {
                                Slog.i(TAG, "To add DTVService");
                                ServiceManager.addService("dtvservice", new DTVService());
                            }
                            traceBeginAndSlog("ZrHungService");
                            try {
                                this.mSystemServiceManager.startService("com.android.server.zrhung.ZRHungService");
                            } catch (Throwable e15) {
                                Slog.e(TAG, e15.toString());
                            }
                            traceEnd();
                            if (isStartHwFSMService) {
                                this.mSystemServiceManager.startService("com.android.server.fsm.HwFoldScreenManagerService");
                            }
                            inputManager = r52;
                            vibrator = vibrator3;
                            wm = main;
                        } catch (RuntimeException e16) {
                            e = e16;
                            obj = main;
                            inputManager2 = r52;
                            almService = almService4;
                            vibrator2 = vibrator3;
                            Slog.e("System", "******************************************");
                            Slog.e("System", "************ Failure starting core service", e);
                            AlarmManagerService alarmManagerService22222222222 = almService;
                            wm = obj;
                            inputManager = inputManager2;
                            vibrator = vibrator2;
                            IBinder iBinder72222222222 = null;
                            ILockSettings lockSettings32222222222 = null;
                            if (this.mFactoryTestMode != i) {
                            }
                            traceBeginAndSlog("MakeDisplayReady");
                            wm.displayReady();
                            traceEnd();
                            traceBeginAndSlog("StartStorageManagerService");
                            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                            storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                            traceEnd();
                            traceBeginAndSlog("StartStorageStatsService");
                            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                            traceEnd();
                            IStorageManager iStorageManager2222222222 = storageManager2;
                            traceBeginAndSlog("StartUiModeManager");
                            this.mSystemServiceManager.startService(UiModeManagerService.class);
                            traceEnd();
                            HwBootCheck.bootSceneEnd(101);
                            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                            if (!this.mRuntimeRestart) {
                            }
                            HwBootCheck.bootSceneStart(104, 900000);
                            if (!this.mOnlyCore) {
                            }
                            traceBeginAndSlog("PerformFstrimIfNeeded");
                            this.mPackageManagerService.performFstrimIfNeeded();
                            traceEnd();
                            HwBootCheck.bootSceneEnd(104);
                            HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                            if (this.mFactoryTestMode == 1) {
                            }
                            if (!isWatch) {
                            }
                            MediaProjectionManagerService.sHasStartedInSystemserver = true;
                            if (isWatch) {
                            }
                            if (!disableSlices) {
                            }
                            if (!disableCameraService) {
                            }
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                            }
                            traceBeginAndSlog("StartStatsCompanionService");
                            this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                            traceEnd();
                            safeMode = wm.detectSafeMode();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            if (safeMode) {
                            }
                            traceBeginAndSlog("StartMmsService");
                            MmsServiceBroker mmsService2222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                            }
                            if (isStartSysSvcCallRecord) {
                            }
                            traceBeginAndSlog("MakeVibratorServiceReady");
                            vibrator.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                            if (lockSettings2 != null) {
                            }
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                            this.mSystemServiceManager.startBootPhase(480);
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                            this.mSystemServiceManager.startBootPhase(500);
                            traceEnd();
                            traceBeginAndSlog("MakeWindowManagerServiceReady");
                            wm.systemReady();
                            traceEnd();
                            if (safeMode) {
                            }
                            Configuration config2222222222 = wm.computeNewConfiguration(0);
                            DisplayMetrics metrics2222222222 = new DisplayMetrics();
                            WindowManager w2222222222 = (WindowManager) context.getSystemService("window");
                            w2222222222.getDefaultDisplay().getMetrics(metrics2222222222);
                            context.getResources().updateConfiguration(config2222222222, metrics2222222222);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            traceBeginAndSlog("StartDeviceSpecificServices");
                            String[] classes2222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                            length = classes2222222222.length;
                            ILockSettings lockSettings42222222222 = lockSettings2;
                            i2 = 0;
                            WindowManagerService wm22222222222 = wm;
                            while (i2 < length) {
                            }
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                            traceEnd();
                            String[] strArr2222222222 = classes2222222222;
                            VibratorService vibratorService32222222222 = vibrator;
                            Resources.Theme theme2222222222 = systemTheme;
                            WindowManager windowManager2222222222 = w2222222222;
                            DisplayMetrics displayMetrics2222222222 = metrics2222222222;
                            Configuration configuration2222222222 = config2222222222;
                            boolean z22222222222 = tuiEnable;
                            LocationManagerService locationManagerService2222222222 = location;
                            ILockSettings iLockSettings22222222222 = lockSettings42222222222;
                            ActivityManagerService activityManagerService2222222222 = this.mActivityManagerService;
                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222222222 = r1222222222;
                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222222222 = new Runnable(this, context, wm22222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222222222, enableIaware, safeMode) {
                                private final /* synthetic */ SystemServer f$0;
                                private final /* synthetic */ Context f$1;
                                private final /* synthetic */ NetworkTimeUpdateService f$10;
                                private final /* synthetic */ CommonTimeManagementService f$11;
                                private final /* synthetic */ InputManagerService f$12;
                                private final /* synthetic */ TelephonyRegistry f$13;
                                private final /* synthetic */ MediaRouterService f$14;
                                private final /* synthetic */ MmsServiceBroker f$15;
                                private final /* synthetic */ boolean f$16;
                                private final /* synthetic */ boolean f$17;
                                private final /* synthetic */ WindowManagerService f$2;
                                private final /* synthetic */ NetworkManagementService f$3;
                                private final /* synthetic */ NetworkPolicyManagerService f$4;
                                private final /* synthetic */ IpSecService f$5;
                                private final /* synthetic */ NetworkStatsService f$6;
                                private final /* synthetic */ ConnectivityService f$7;
                                private final /* synthetic */ LocationManagerService f$8;
                                private final /* synthetic */ CountryDetectorService f$9;

                                {
                                    this.f$0 = r3;
                                    this.f$1 = r4;
                                    this.f$2 = r5;
                                    this.f$3 = r6;
                                    this.f$4 = r7;
                                    this.f$5 = r8;
                                    this.f$6 = r9;
                                    this.f$7 = r10;
                                    this.f$8 = r11;
                                    this.f$9 = r12;
                                    this.f$10 = r13;
                                    this.f$11 = r14;
                                    this.f$12 = r15;
                                    this.f$13 = r16;
                                    this.f$14 = r17;
                                    this.f$15 = r18;
                                    this.f$16 = r19;
                                    this.f$17 = r20;
                                }

                                public final void run() {
                                    SystemServer systemServer = this.f$0;
                                    Context context = this.f$1;
                                    WindowManagerService windowManagerService = this.f$2;
                                    NetworkManagementService networkManagementService = this.f$3;
                                    NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                    IpSecService ipSecService = this.f$5;
                                    NetworkStatsService networkStatsService = this.f$6;
                                    ConnectivityService connectivityService = this.f$7;
                                    LocationManagerService locationManagerService = this.f$8;
                                    CountryDetectorService countryDetectorService = this.f$9;
                                    NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                    CommonTimeManagementService commonTimeManagementService = this.f$11;
                                    InputManagerService inputManagerService = this.f$12;
                                    TelephonyRegistry telephonyRegistry = this.f$13;
                                    MediaRouterService mediaRouterService = this.f$14;
                                    MmsServiceBroker mmsServiceBroker = this.f$15;
                                    boolean z = this.f$16;
                                    boolean z2 = z;
                                    SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                }
                            };
                            activityManagerService2222222222.systemReady(r02222222222, BOOT_TIMINGS_TRACE_LOG);
                        }
                        IBinder iBinder722222222222 = null;
                        ILockSettings lockSettings322222222222 = null;
                        if (this.mFactoryTestMode != i) {
                            traceBeginAndSlog("StartInputMethodManagerLifecycle");
                            try {
                                Slog.i(TAG, "Input Method Service");
                                this.mSystemServiceManager.startService(InputMethodManagerService.Lifecycle.class);
                            } catch (Throwable e17) {
                                reportWtf("starting Input Manager Service", e17);
                            }
                            if (isSupportedSecIme) {
                                try {
                                    Slog.i(TAG, "Secure Input Method Service");
                                    this.mSystemServiceManager.startService("com.android.server.HwSecureInputMethodManagerService$MyLifecycle");
                                } catch (Throwable e18) {
                                    reportWtf("starting Secure Input Manager Service", e18);
                                }
                            }
                            traceEnd();
                            traceBeginAndSlog("StartAccessibilityManagerService");
                            try {
                                ServiceManager.addService("accessibility", new AccessibilityManagerService(context));
                            } catch (Throwable e19) {
                                reportWtf("starting Accessibility Manager", e19);
                            }
                            traceEnd();
                        }
                        traceBeginAndSlog("MakeDisplayReady");
                        wm.displayReady();
                        traceEnd();
                        if (this.mFactoryTestMode != i && !"0".equals(SystemProperties.get("system_init.startmountservice"))) {
                            traceBeginAndSlog("StartStorageManagerService");
                            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                            storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                            traceEnd();
                            traceBeginAndSlog("StartStorageStatsService");
                            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                            traceEnd();
                            IStorageManager iStorageManager22222222222 = storageManager2;
                        }
                        traceBeginAndSlog("StartUiModeManager");
                        this.mSystemServiceManager.startService(UiModeManagerService.class);
                        traceEnd();
                        HwBootCheck.bootSceneEnd(101);
                        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                        if (!this.mRuntimeRestart || isFirstBootOrUpgrade()) {
                            HwBootCheck.bootSceneStart(104, 900000);
                        } else {
                            HwBootCheck.bootSceneStart(104, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                        }
                        if (!this.mOnlyCore) {
                            traceBeginAndSlog("UpdatePackagesIfNeeded");
                            try {
                                this.mPackageManagerService.updatePackagesIfNeeded();
                            } catch (Throwable e20) {
                                reportWtf("update packages", e20);
                            }
                            traceEnd();
                        }
                        traceBeginAndSlog("PerformFstrimIfNeeded");
                        this.mPackageManagerService.performFstrimIfNeeded();
                        traceEnd();
                        HwBootCheck.bootSceneEnd(104);
                        HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                        if (this.mFactoryTestMode == 1) {
                            startForceRotation(context);
                            traceBeginAndSlog("StartLockSettingsService");
                            try {
                                this.mSystemServiceManager.startService(LOCK_SETTINGS_SERVICE_CLASS);
                                lockSettings322222222222 = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
                            } catch (Throwable e21) {
                                reportWtf("starting LockSettingsService service", e21);
                            }
                            traceEnd();
                            boolean hasPdb = !SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP).equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                            if (hasPdb) {
                                traceBeginAndSlog("StartPersistentDataBlock");
                                this.mSystemServiceManager.startService(PersistentDataBlockService.class);
                                traceEnd();
                            }
                            if (hasPdb || OemLockService.isHalPresent()) {
                                traceBeginAndSlog("StartOemLockService");
                                this.mSystemServiceManager.startService(OemLockService.class);
                                traceEnd();
                            }
                            traceBeginAndSlog("StartDeviceIdleController");
                            this.mSystemServiceManager.startService(DeviceIdleController.class);
                            traceEnd();
                            traceBeginAndSlog("StartDevicePolicyManager");
                            this.mSystemServiceManager.startService(DevicePolicyManagerService.Lifecycle.class);
                            traceEnd();
                            if (!isWatch) {
                                traceBeginAndSlog("StartStatusBarManagerService");
                                try {
                                    Slog.i(TAG, "Status Bar");
                                    iBinder722222222222 = HwServiceFactory.createHwStatusBarManagerService(context, wm);
                                    ServiceManager.addService("statusbar", iBinder722222222222);
                                } catch (Throwable e22) {
                                    reportWtf("starting StatusBarManagerService", e22);
                                }
                                traceEnd();
                            }
                            traceBeginAndSlog("StartClipboardService");
                            this.mSystemServiceManager.startService(ClipboardService.class);
                            traceEnd();
                            traceBeginAndSlog("StartNetworkManagementService");
                            try {
                                iNetworkManagementService2 = NetworkManagementService.create(context);
                                ServiceManager.addService("network_management", iNetworkManagementService2);
                            } catch (Throwable e23) {
                                reportWtf("starting NetworkManagement Service", e23);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartIpSecService");
                            try {
                                iBinder5 = IpSecService.create(context);
                                ServiceManager.addService(INetd.IPSEC_INTERFACE_PREFIX, iBinder5);
                            } catch (Throwable e24) {
                                reportWtf("starting IpSec Service", e24);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartTextServicesManager");
                            this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                            traceEnd();
                            if (!disableSystemTextClassifier) {
                                traceBeginAndSlog("StartTextClassificationManagerService");
                                this.mSystemServiceManager.startService(TextClassificationManagerService.Lifecycle.class);
                                traceEnd();
                            }
                            traceBeginAndSlog("StartNetworkScoreService");
                            this.mSystemServiceManager.startService(NetworkScoreService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartNetworkStatsService");
                            try {
                                iNetworkStatsService2 = NetworkStatsService.create(context, iNetworkManagementService2);
                                ServiceManager.addService("netstats", iNetworkStatsService2);
                            } catch (Throwable e25) {
                                reportWtf("starting NetworkStats Service", e25);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartNetworkPolicyManagerService");
                            try {
                                iLockSettings = HwServiceFactory.getHwNetworkPolicyManagerService().getInstance(context, this.mActivityManagerService, iNetworkManagementService2);
                                ServiceManager.addService("netpolicy", iLockSettings);
                            } catch (Throwable e26) {
                                reportWtf("starting NetworkPolicy Service", e26);
                            }
                            traceEnd();
                            if (!this.mOnlyCore) {
                                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi")) {
                                    traceBeginAndSlog("StartWifi");
                                    this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                                    traceEnd();
                                    traceBeginAndSlog("StartWifiScanning");
                                    this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                                    traceEnd();
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.rtt")) {
                                    traceBeginAndSlog("StartRttService");
                                    this.mSystemServiceManager.startService("com.android.server.wifi.rtt.RttService");
                                    traceEnd();
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                                    traceBeginAndSlog("StartWifiAware");
                                    this.mSystemServiceManager.startService(WIFI_AWARE_SERVICE_CLASS);
                                    traceEnd();
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                                    traceBeginAndSlog("StartWifiP2P");
                                    this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                                    traceEnd();
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.lowpan")) {
                                    traceBeginAndSlog("StartLowpan");
                                    this.mSystemServiceManager.startService(LOWPAN_SERVICE_CLASS);
                                    traceEnd();
                                }
                            }
                            if (this.mPackageManager.hasSystemFeature("android.hardware.ethernet") || this.mPackageManager.hasSystemFeature("android.hardware.usb.host")) {
                                traceBeginAndSlog("StartEthernet");
                                this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                                traceEnd();
                            }
                            traceBeginAndSlog("StartConnectivityService");
                            try {
                                ? createHwConnectivityService = HwServiceFactory.getHwConnectivityManager().createHwConnectivityService(context, iNetworkManagementService2, iNetworkStatsService2, iLockSettings);
                                IBinder iBinder8 = iBinder722222222222;
                                try {
                                    ServiceManager.addService("connectivity", createHwConnectivityService, false, 6);
                                    iNetworkStatsService2.bindConnectivityManager(createHwConnectivityService);
                                    iLockSettings.bindConnectivityManager(createHwConnectivityService);
                                    connectivity2 = createHwConnectivityService;
                                } catch (Throwable th) {
                                    e = th;
                                    connectivity3 = createHwConnectivityService;
                                }
                            } catch (Throwable th2) {
                                e = th2;
                                IBinder iBinder9 = iBinder722222222222;
                                connectivity3 = connectivity;
                                reportWtf("starting Connectivity Service", e);
                                connectivity2 = connectivity3;
                                traceEnd();
                                traceBeginAndSlog("StartNsdService");
                                iBinder = NsdService.create(context);
                                try {
                                    ServiceManager.addService("servicediscovery", iBinder);
                                } catch (Throwable th3) {
                                    e = th3;
                                }
                                IBinder iBinder10 = iBinder;
                                traceEnd();
                                traceBeginAndSlog("StartSystemUpdateManagerService");
                                ServiceManager.addService("system_update", new SystemUpdateManagerService(context));
                                traceEnd();
                                traceBeginAndSlog("StartUpdateLockService");
                                ServiceManager.addService("updatelock", new UpdateLockService(context));
                                traceEnd();
                                traceBeginAndSlog("StartNotificationManager");
                                this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
                                SystemNotificationChannels.createAll(context);
                                INotificationManager notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                                traceEnd();
                                traceBeginAndSlog("StartDeviceMonitor");
                                this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
                                traceEnd();
                                StringBuilder sb = new StringBuilder();
                                INotificationManager notification2 = notification;
                                sb.append("TUI Connect enable ");
                                sb.append(tuiEnable);
                                Slog.i(TAG, sb.toString());
                                if (tuiEnable) {
                                }
                                if (vrDisplayEnable) {
                                }
                                traceBeginAndSlog("StartLocationManagerService");
                                Slog.i(TAG, "Location Manager");
                                hwLocation = HwServiceFactory.getHwLocationManagerService();
                                if (hwLocation != null) {
                                }
                                try {
                                    ServiceManager.addService("location", r5);
                                    location2 = r5;
                                } catch (Throwable th4) {
                                    e = th4;
                                    location4 = r5;
                                }
                                traceEnd();
                                traceBeginAndSlog("StartCountryDetectorService");
                                countryDetectorService = new CountryDetectorService(context);
                                try {
                                    ServiceManager.addService("country_detector", countryDetectorService);
                                    boolean z3 = vrDisplayEnable;
                                    countryDetector2 = countryDetectorService;
                                } catch (Throwable th5) {
                                    e = th5;
                                    countryDetector3 = countryDetectorService;
                                    boolean z4 = vrDisplayEnable;
                                    reportWtf("starting Country Detector", e);
                                    countryDetector2 = countryDetector3;
                                    traceEnd();
                                    if (!isWatch) {
                                    }
                                    if (context.getResources().getBoolean(17956968)) {
                                    }
                                    traceBeginAndSlog("StartTrustManager");
                                    this.mSystemServiceManager.startService(TrustManagerService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartAudioService");
                                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartDockObserver");
                                    this.mSystemServiceManager.startService(DockObserver.class);
                                    traceEnd();
                                    if (isWatch) {
                                    }
                                    traceBeginAndSlog("StartWiredAccessoryManager");
                                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                                    }
                                    traceBeginAndSlog("StartUsbService");
                                    this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                                    traceEnd();
                                    if (isWatch) {
                                    }
                                    traceBeginAndSlog("StartHardwarePropertiesManagerService");
                                    iBinder3 = new HardwarePropertiesManagerService(context);
                                    ServiceManager.addService("hardware_properties", iBinder3);
                                    iBinder2 = iBinder3;
                                    traceEnd();
                                    traceBeginAndSlog("StartTwilightService");
                                    this.mSystemServiceManager.startService(TwilightService.class);
                                    traceEnd();
                                    if (ColorDisplayController.isAvailable(context)) {
                                    }
                                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                                    traceBeginAndSlog("StartSoundTrigger");
                                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                    }
                                    traceBeginAndSlog("StartAppWidgerService");
                                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                    traceEnd();
                                    traceBeginAndSlog("StartVoiceRecognitionManager");
                                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                    traceEnd();
                                    if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                                    }
                                    traceBeginAndSlog("StartSensorNotification");
                                    this.mSystemServiceManager.startService(SensorNotificationService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartContextHubSystemService");
                                    this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                    traceEnd();
                                    HwServiceFactory.setupHwServices(context);
                                    traceBeginAndSlog("StartDiskStatsService");
                                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                                    traceEnd();
                                    startRulesManagerService = this.mOnlyCore && context.getResources().getBoolean(17956967);
                                    if (startRulesManagerService) {
                                    }
                                    Slog.i(TAG, "attestation Service");
                                    attestation = HwServiceFactory.getHwAttestationService();
                                    if (attestation == null) {
                                    }
                                    if (!isWatch) {
                                    }
                                    traceBeginAndSlog("StartCommonTimeManagementService");
                                    commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                    ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                    traceEnd();
                                    traceBeginAndSlog("CertBlacklister");
                                    new CertBlacklister(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartEmergencyAffordanceService");
                                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartDreamManager");
                                    this.mSystemServiceManager.startService(DreamManagerService.class);
                                    traceEnd();
                                    traceBeginAndSlog("AddGraphicsStatsService");
                                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                    traceEnd();
                                    if (CoverageService.ENABLED) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                    }
                                    traceBeginAndSlog("StartRestrictionManager");
                                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartMediaSessionService");
                                    this.mSystemServiceManager.startService(MediaSessionService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartMediaUpdateService");
                                    this.mSystemServiceManager.startService(MediaUpdateService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                    }
                                    traceBeginAndSlog("StartTvInputManager");
                                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                    }
                                    traceBeginAndSlog("StartMediaRouterService");
                                    mediaRouterService = new MediaRouterService(context);
                                    ServiceManager.addService("media_router", mediaRouterService);
                                    commonTimeMgmtService = commonTimeMgmtService2;
                                    mediaRouter2 = mediaRouterService;
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                    }
                                    traceBeginAndSlog("StartBackgroundDexOptService");
                                    BackgroundDexOptService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartPruneInstantAppsJobService");
                                    PruneInstantAppsJobService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartLauncherAppsService");
                                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartCrossProfileAppsService");
                                    this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                    traceEnd();
                                    connectivity = connectivity2;
                                    countryDetector = countryDetector2;
                                    iNetworkManagementService = iNetworkManagementService2;
                                    iNetworkStatsService = iNetworkStatsService2;
                                    IBinder iBinder11 = iBinder10;
                                    IBinder iBinder12 = iBinder6;
                                    networkTimeUpdater = networkTimeUpdater3;
                                    IBinder iBinder13 = iBinder2;
                                    lockSettings2 = lockSettings322222222222;
                                    INotificationManager iNotificationManager = notification2;
                                    location = location3;
                                    ipSecServiceF = iBinder5;
                                    lockSettings = iLockSettings;
                                    if (!isWatch) {
                                    }
                                    MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                    if (isWatch) {
                                    }
                                    if (!disableSlices) {
                                    }
                                    if (!disableCameraService) {
                                    }
                                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                    }
                                    traceBeginAndSlog("StartStatsCompanionService");
                                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                    traceEnd();
                                    safeMode = wm.detectSafeMode();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    if (safeMode) {
                                    }
                                    traceBeginAndSlog("StartMmsService");
                                    MmsServiceBroker mmsService22222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                    }
                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                    }
                                    if (isStartSysSvcCallRecord) {
                                    }
                                    traceBeginAndSlog("MakeVibratorServiceReady");
                                    vibrator.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                                    if (lockSettings2 != null) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                    this.mSystemServiceManager.startBootPhase(480);
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                    this.mSystemServiceManager.startBootPhase(500);
                                    traceEnd();
                                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                                    wm.systemReady();
                                    traceEnd();
                                    if (safeMode) {
                                    }
                                    Configuration config22222222222 = wm.computeNewConfiguration(0);
                                    DisplayMetrics metrics22222222222 = new DisplayMetrics();
                                    WindowManager w22222222222 = (WindowManager) context.getSystemService("window");
                                    w22222222222.getDefaultDisplay().getMetrics(metrics22222222222);
                                    context.getResources().updateConfiguration(config22222222222, metrics22222222222);
                                    systemTheme = context.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                    }
                                    traceBeginAndSlog("MakePowerManagerServiceReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    traceEnd();
                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                    traceBeginAndSlog("MakePackageManagerServiceReady");
                                    this.mPackageManagerService.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    traceEnd();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    traceBeginAndSlog("StartDeviceSpecificServices");
                                    String[] classes22222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                    length = classes22222222222.length;
                                    ILockSettings lockSettings422222222222 = lockSettings2;
                                    i2 = 0;
                                    WindowManagerService wm222222222222 = wm;
                                    while (i2 < length) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                    traceEnd();
                                    String[] strArr22222222222 = classes22222222222;
                                    VibratorService vibratorService322222222222 = vibrator;
                                    Resources.Theme theme22222222222 = systemTheme;
                                    WindowManager windowManager22222222222 = w22222222222;
                                    DisplayMetrics displayMetrics22222222222 = metrics22222222222;
                                    Configuration configuration22222222222 = config22222222222;
                                    boolean z222222222222 = tuiEnable;
                                    LocationManagerService locationManagerService22222222222 = location;
                                    ILockSettings iLockSettings222222222222 = lockSettings422222222222;
                                    ActivityManagerService activityManagerService22222222222 = this.mActivityManagerService;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022222222222 = r12222222222;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122222222222 = new Runnable(this, context, wm222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22222222222, enableIaware, safeMode) {
                                        private final /* synthetic */ SystemServer f$0;
                                        private final /* synthetic */ Context f$1;
                                        private final /* synthetic */ NetworkTimeUpdateService f$10;
                                        private final /* synthetic */ CommonTimeManagementService f$11;
                                        private final /* synthetic */ InputManagerService f$12;
                                        private final /* synthetic */ TelephonyRegistry f$13;
                                        private final /* synthetic */ MediaRouterService f$14;
                                        private final /* synthetic */ MmsServiceBroker f$15;
                                        private final /* synthetic */ boolean f$16;
                                        private final /* synthetic */ boolean f$17;
                                        private final /* synthetic */ WindowManagerService f$2;
                                        private final /* synthetic */ NetworkManagementService f$3;
                                        private final /* synthetic */ NetworkPolicyManagerService f$4;
                                        private final /* synthetic */ IpSecService f$5;
                                        private final /* synthetic */ NetworkStatsService f$6;
                                        private final /* synthetic */ ConnectivityService f$7;
                                        private final /* synthetic */ LocationManagerService f$8;
                                        private final /* synthetic */ CountryDetectorService f$9;

                                        {
                                            this.f$0 = r3;
                                            this.f$1 = r4;
                                            this.f$2 = r5;
                                            this.f$3 = r6;
                                            this.f$4 = r7;
                                            this.f$5 = r8;
                                            this.f$6 = r9;
                                            this.f$7 = r10;
                                            this.f$8 = r11;
                                            this.f$9 = r12;
                                            this.f$10 = r13;
                                            this.f$11 = r14;
                                            this.f$12 = r15;
                                            this.f$13 = r16;
                                            this.f$14 = r17;
                                            this.f$15 = r18;
                                            this.f$16 = r19;
                                            this.f$17 = r20;
                                        }

                                        public final void run() {
                                            SystemServer systemServer = this.f$0;
                                            Context context = this.f$1;
                                            WindowManagerService windowManagerService = this.f$2;
                                            NetworkManagementService networkManagementService = this.f$3;
                                            NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                            IpSecService ipSecService = this.f$5;
                                            NetworkStatsService networkStatsService = this.f$6;
                                            ConnectivityService connectivityService = this.f$7;
                                            LocationManagerService locationManagerService = this.f$8;
                                            CountryDetectorService countryDetectorService = this.f$9;
                                            NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                            CommonTimeManagementService commonTimeManagementService = this.f$11;
                                            InputManagerService inputManagerService = this.f$12;
                                            TelephonyRegistry telephonyRegistry = this.f$13;
                                            MediaRouterService mediaRouterService = this.f$14;
                                            MmsServiceBroker mmsServiceBroker = this.f$15;
                                            boolean z = this.f$16;
                                            boolean z2 = z;
                                            SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                        }
                                    };
                                    activityManagerService22222222222.systemReady(r022222222222, BOOT_TIMINGS_TRACE_LOG);
                                }
                                traceEnd();
                                if (!isWatch) {
                                }
                                if (context.getResources().getBoolean(17956968)) {
                                }
                                traceBeginAndSlog("StartTrustManager");
                                this.mSystemServiceManager.startService(TrustManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartAudioService");
                                this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartDockObserver");
                                this.mSystemServiceManager.startService(DockObserver.class);
                                traceEnd();
                                if (isWatch) {
                                }
                                traceBeginAndSlog("StartWiredAccessoryManager");
                                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                                }
                                traceBeginAndSlog("StartUsbService");
                                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                                traceEnd();
                                if (isWatch) {
                                }
                                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                                iBinder3 = new HardwarePropertiesManagerService(context);
                                try {
                                    ServiceManager.addService("hardware_properties", iBinder3);
                                    iBinder2 = iBinder3;
                                } catch (Throwable th6) {
                                    e = th6;
                                    Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e);
                                    iBinder2 = iBinder3;
                                    traceEnd();
                                    traceBeginAndSlog("StartTwilightService");
                                    this.mSystemServiceManager.startService(TwilightService.class);
                                    traceEnd();
                                    if (ColorDisplayController.isAvailable(context)) {
                                    }
                                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                                    traceBeginAndSlog("StartSoundTrigger");
                                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                    }
                                    traceBeginAndSlog("StartAppWidgerService");
                                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                    traceEnd();
                                    traceBeginAndSlog("StartVoiceRecognitionManager");
                                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                    traceEnd();
                                    if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                                    }
                                    traceBeginAndSlog("StartSensorNotification");
                                    this.mSystemServiceManager.startService(SensorNotificationService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartContextHubSystemService");
                                    this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                    traceEnd();
                                    HwServiceFactory.setupHwServices(context);
                                    traceBeginAndSlog("StartDiskStatsService");
                                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                                    traceEnd();
                                    startRulesManagerService = this.mOnlyCore && context.getResources().getBoolean(17956967);
                                    if (startRulesManagerService) {
                                    }
                                    Slog.i(TAG, "attestation Service");
                                    attestation = HwServiceFactory.getHwAttestationService();
                                    if (attestation == null) {
                                    }
                                    if (!isWatch) {
                                    }
                                    traceBeginAndSlog("StartCommonTimeManagementService");
                                    commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                    ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                    traceEnd();
                                    traceBeginAndSlog("CertBlacklister");
                                    new CertBlacklister(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartEmergencyAffordanceService");
                                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartDreamManager");
                                    this.mSystemServiceManager.startService(DreamManagerService.class);
                                    traceEnd();
                                    traceBeginAndSlog("AddGraphicsStatsService");
                                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                    traceEnd();
                                    if (CoverageService.ENABLED) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                    }
                                    traceBeginAndSlog("StartRestrictionManager");
                                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartMediaSessionService");
                                    this.mSystemServiceManager.startService(MediaSessionService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartMediaUpdateService");
                                    this.mSystemServiceManager.startService(MediaUpdateService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                    }
                                    traceBeginAndSlog("StartTvInputManager");
                                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                    }
                                    traceBeginAndSlog("StartMediaRouterService");
                                    mediaRouterService = new MediaRouterService(context);
                                    ServiceManager.addService("media_router", mediaRouterService);
                                    commonTimeMgmtService = commonTimeMgmtService2;
                                    mediaRouter2 = mediaRouterService;
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                    }
                                    traceBeginAndSlog("StartBackgroundDexOptService");
                                    BackgroundDexOptService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartPruneInstantAppsJobService");
                                    PruneInstantAppsJobService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartLauncherAppsService");
                                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartCrossProfileAppsService");
                                    this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                    traceEnd();
                                    connectivity = connectivity2;
                                    countryDetector = countryDetector2;
                                    iNetworkManagementService = iNetworkManagementService2;
                                    iNetworkStatsService = iNetworkStatsService2;
                                    IBinder iBinder112 = iBinder10;
                                    IBinder iBinder122 = iBinder6;
                                    networkTimeUpdater = networkTimeUpdater3;
                                    IBinder iBinder132 = iBinder2;
                                    lockSettings2 = lockSettings322222222222;
                                    INotificationManager iNotificationManager2 = notification2;
                                    location = location3;
                                    ipSecServiceF = iBinder5;
                                    lockSettings = iLockSettings;
                                    if (!isWatch) {
                                    }
                                    MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                    if (isWatch) {
                                    }
                                    if (!disableSlices) {
                                    }
                                    if (!disableCameraService) {
                                    }
                                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                    }
                                    traceBeginAndSlog("StartStatsCompanionService");
                                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                    traceEnd();
                                    safeMode = wm.detectSafeMode();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    if (safeMode) {
                                    }
                                    traceBeginAndSlog("StartMmsService");
                                    MmsServiceBroker mmsService222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                    }
                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                    }
                                    if (isStartSysSvcCallRecord) {
                                    }
                                    traceBeginAndSlog("MakeVibratorServiceReady");
                                    vibrator.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                                    if (lockSettings2 != null) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                    this.mSystemServiceManager.startBootPhase(480);
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                    this.mSystemServiceManager.startBootPhase(500);
                                    traceEnd();
                                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                                    wm.systemReady();
                                    traceEnd();
                                    if (safeMode) {
                                    }
                                    Configuration config222222222222 = wm.computeNewConfiguration(0);
                                    DisplayMetrics metrics222222222222 = new DisplayMetrics();
                                    WindowManager w222222222222 = (WindowManager) context.getSystemService("window");
                                    w222222222222.getDefaultDisplay().getMetrics(metrics222222222222);
                                    context.getResources().updateConfiguration(config222222222222, metrics222222222222);
                                    systemTheme = context.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                    }
                                    traceBeginAndSlog("MakePowerManagerServiceReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    traceEnd();
                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                    traceBeginAndSlog("MakePackageManagerServiceReady");
                                    this.mPackageManagerService.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    traceEnd();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    traceBeginAndSlog("StartDeviceSpecificServices");
                                    String[] classes222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                    length = classes222222222222.length;
                                    ILockSettings lockSettings4222222222222 = lockSettings2;
                                    i2 = 0;
                                    WindowManagerService wm2222222222222 = wm;
                                    while (i2 < length) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                    traceEnd();
                                    String[] strArr222222222222 = classes222222222222;
                                    VibratorService vibratorService3222222222222 = vibrator;
                                    Resources.Theme theme222222222222 = systemTheme;
                                    WindowManager windowManager222222222222 = w222222222222;
                                    DisplayMetrics displayMetrics222222222222 = metrics222222222222;
                                    Configuration configuration222222222222 = config222222222222;
                                    boolean z2222222222222 = tuiEnable;
                                    LocationManagerService locationManagerService222222222222 = location;
                                    ILockSettings iLockSettings2222222222222 = lockSettings4222222222222;
                                    ActivityManagerService activityManagerService222222222222 = this.mActivityManagerService;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222222222222 = r122222222222;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222222222222 = new Runnable(this, context, wm2222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222222222222, enableIaware, safeMode) {
                                        private final /* synthetic */ SystemServer f$0;
                                        private final /* synthetic */ Context f$1;
                                        private final /* synthetic */ NetworkTimeUpdateService f$10;
                                        private final /* synthetic */ CommonTimeManagementService f$11;
                                        private final /* synthetic */ InputManagerService f$12;
                                        private final /* synthetic */ TelephonyRegistry f$13;
                                        private final /* synthetic */ MediaRouterService f$14;
                                        private final /* synthetic */ MmsServiceBroker f$15;
                                        private final /* synthetic */ boolean f$16;
                                        private final /* synthetic */ boolean f$17;
                                        private final /* synthetic */ WindowManagerService f$2;
                                        private final /* synthetic */ NetworkManagementService f$3;
                                        private final /* synthetic */ NetworkPolicyManagerService f$4;
                                        private final /* synthetic */ IpSecService f$5;
                                        private final /* synthetic */ NetworkStatsService f$6;
                                        private final /* synthetic */ ConnectivityService f$7;
                                        private final /* synthetic */ LocationManagerService f$8;
                                        private final /* synthetic */ CountryDetectorService f$9;

                                        {
                                            this.f$0 = r3;
                                            this.f$1 = r4;
                                            this.f$2 = r5;
                                            this.f$3 = r6;
                                            this.f$4 = r7;
                                            this.f$5 = r8;
                                            this.f$6 = r9;
                                            this.f$7 = r10;
                                            this.f$8 = r11;
                                            this.f$9 = r12;
                                            this.f$10 = r13;
                                            this.f$11 = r14;
                                            this.f$12 = r15;
                                            this.f$13 = r16;
                                            this.f$14 = r17;
                                            this.f$15 = r18;
                                            this.f$16 = r19;
                                            this.f$17 = r20;
                                        }

                                        public final void run() {
                                            SystemServer systemServer = this.f$0;
                                            Context context = this.f$1;
                                            WindowManagerService windowManagerService = this.f$2;
                                            NetworkManagementService networkManagementService = this.f$3;
                                            NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                            IpSecService ipSecService = this.f$5;
                                            NetworkStatsService networkStatsService = this.f$6;
                                            ConnectivityService connectivityService = this.f$7;
                                            LocationManagerService locationManagerService = this.f$8;
                                            CountryDetectorService countryDetectorService = this.f$9;
                                            NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                            CommonTimeManagementService commonTimeManagementService = this.f$11;
                                            InputManagerService inputManagerService = this.f$12;
                                            TelephonyRegistry telephonyRegistry = this.f$13;
                                            MediaRouterService mediaRouterService = this.f$14;
                                            MmsServiceBroker mmsServiceBroker = this.f$15;
                                            boolean z = this.f$16;
                                            boolean z2 = z;
                                            SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                        }
                                    };
                                    activityManagerService222222222222.systemReady(r0222222222222, BOOT_TIMINGS_TRACE_LOG);
                                }
                                traceEnd();
                                traceBeginAndSlog("StartTwilightService");
                                this.mSystemServiceManager.startService(TwilightService.class);
                                traceEnd();
                                if (ColorDisplayController.isAvailable(context)) {
                                }
                                this.mSystemServiceManager.startService(JobSchedulerService.class);
                                traceBeginAndSlog("StartSoundTrigger");
                                this.mSystemServiceManager.startService(SoundTriggerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                }
                                traceBeginAndSlog("StartAppWidgerService");
                                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                traceEnd();
                                traceBeginAndSlog("StartVoiceRecognitionManager");
                                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                traceEnd();
                                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                                }
                                traceBeginAndSlog("StartSensorNotification");
                                this.mSystemServiceManager.startService(SensorNotificationService.class);
                                traceEnd();
                                traceBeginAndSlog("StartContextHubSystemService");
                                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                traceEnd();
                                HwServiceFactory.setupHwServices(context);
                                traceBeginAndSlog("StartDiskStatsService");
                                ServiceManager.addService("diskstats", new DiskStatsService(context));
                                traceEnd();
                                startRulesManagerService = this.mOnlyCore && context.getResources().getBoolean(17956967);
                                if (startRulesManagerService) {
                                }
                                Slog.i(TAG, "attestation Service");
                                attestation = HwServiceFactory.getHwAttestationService();
                                if (attestation == null) {
                                }
                                if (!isWatch) {
                                }
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                try {
                                    ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                } catch (Throwable th7) {
                                    e = th7;
                                }
                                traceEnd();
                                traceBeginAndSlog("CertBlacklister");
                                new CertBlacklister(context);
                                traceEnd();
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                traceBeginAndSlog("StartDreamManager");
                                this.mSystemServiceManager.startService(DreamManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("AddGraphicsStatsService");
                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                traceEnd();
                                if (CoverageService.ENABLED) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                }
                                traceBeginAndSlog("StartRestrictionManager");
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaSessionService");
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaUpdateService");
                                this.mSystemServiceManager.startService(MediaUpdateService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                traceBeginAndSlog("StartTvInputManager");
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                traceBeginAndSlog("StartMediaRouterService");
                                mediaRouterService = new MediaRouterService(context);
                                try {
                                    ServiceManager.addService("media_router", mediaRouterService);
                                    commonTimeMgmtService = commonTimeMgmtService2;
                                    mediaRouter2 = mediaRouterService;
                                } catch (Throwable th8) {
                                    e = th8;
                                    mediaRouter3 = mediaRouterService;
                                    commonTimeMgmtService = commonTimeMgmtService2;
                                    reportWtf("starting MediaRouterService", e);
                                    mediaRouter2 = mediaRouter3;
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                    }
                                    traceBeginAndSlog("StartBackgroundDexOptService");
                                    BackgroundDexOptService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartPruneInstantAppsJobService");
                                    PruneInstantAppsJobService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartLauncherAppsService");
                                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartCrossProfileAppsService");
                                    this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                    traceEnd();
                                    connectivity = connectivity2;
                                    countryDetector = countryDetector2;
                                    iNetworkManagementService = iNetworkManagementService2;
                                    iNetworkStatsService = iNetworkStatsService2;
                                    IBinder iBinder1122 = iBinder10;
                                    IBinder iBinder1222 = iBinder6;
                                    networkTimeUpdater = networkTimeUpdater3;
                                    IBinder iBinder1322 = iBinder2;
                                    lockSettings2 = lockSettings322222222222;
                                    INotificationManager iNotificationManager22 = notification2;
                                    location = location3;
                                    ipSecServiceF = iBinder5;
                                    lockSettings = iLockSettings;
                                    if (!isWatch) {
                                    }
                                    MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                    if (isWatch) {
                                    }
                                    if (!disableSlices) {
                                    }
                                    if (!disableCameraService) {
                                    }
                                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                    }
                                    traceBeginAndSlog("StartStatsCompanionService");
                                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                    traceEnd();
                                    safeMode = wm.detectSafeMode();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    if (safeMode) {
                                    }
                                    traceBeginAndSlog("StartMmsService");
                                    MmsServiceBroker mmsService2222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                    }
                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                    }
                                    if (isStartSysSvcCallRecord) {
                                    }
                                    traceBeginAndSlog("MakeVibratorServiceReady");
                                    vibrator.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                                    if (lockSettings2 != null) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                    this.mSystemServiceManager.startBootPhase(480);
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                    this.mSystemServiceManager.startBootPhase(500);
                                    traceEnd();
                                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                                    wm.systemReady();
                                    traceEnd();
                                    if (safeMode) {
                                    }
                                    Configuration config2222222222222 = wm.computeNewConfiguration(0);
                                    DisplayMetrics metrics2222222222222 = new DisplayMetrics();
                                    WindowManager w2222222222222 = (WindowManager) context.getSystemService("window");
                                    w2222222222222.getDefaultDisplay().getMetrics(metrics2222222222222);
                                    context.getResources().updateConfiguration(config2222222222222, metrics2222222222222);
                                    systemTheme = context.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                    }
                                    traceBeginAndSlog("MakePowerManagerServiceReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    traceEnd();
                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                    traceBeginAndSlog("MakePackageManagerServiceReady");
                                    this.mPackageManagerService.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    traceEnd();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    traceBeginAndSlog("StartDeviceSpecificServices");
                                    String[] classes2222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                    length = classes2222222222222.length;
                                    ILockSettings lockSettings42222222222222 = lockSettings2;
                                    i2 = 0;
                                    WindowManagerService wm22222222222222 = wm;
                                    while (i2 < length) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                    traceEnd();
                                    String[] strArr2222222222222 = classes2222222222222;
                                    VibratorService vibratorService32222222222222 = vibrator;
                                    Resources.Theme theme2222222222222 = systemTheme;
                                    WindowManager windowManager2222222222222 = w2222222222222;
                                    DisplayMetrics displayMetrics2222222222222 = metrics2222222222222;
                                    Configuration configuration2222222222222 = config2222222222222;
                                    boolean z22222222222222 = tuiEnable;
                                    LocationManagerService locationManagerService2222222222222 = location;
                                    ILockSettings iLockSettings22222222222222 = lockSettings42222222222222;
                                    ActivityManagerService activityManagerService2222222222222 = this.mActivityManagerService;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222222222222 = r1222222222222;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222222222222 = new Runnable(this, context, wm22222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222222222222, enableIaware, safeMode) {
                                        private final /* synthetic */ SystemServer f$0;
                                        private final /* synthetic */ Context f$1;
                                        private final /* synthetic */ NetworkTimeUpdateService f$10;
                                        private final /* synthetic */ CommonTimeManagementService f$11;
                                        private final /* synthetic */ InputManagerService f$12;
                                        private final /* synthetic */ TelephonyRegistry f$13;
                                        private final /* synthetic */ MediaRouterService f$14;
                                        private final /* synthetic */ MmsServiceBroker f$15;
                                        private final /* synthetic */ boolean f$16;
                                        private final /* synthetic */ boolean f$17;
                                        private final /* synthetic */ WindowManagerService f$2;
                                        private final /* synthetic */ NetworkManagementService f$3;
                                        private final /* synthetic */ NetworkPolicyManagerService f$4;
                                        private final /* synthetic */ IpSecService f$5;
                                        private final /* synthetic */ NetworkStatsService f$6;
                                        private final /* synthetic */ ConnectivityService f$7;
                                        private final /* synthetic */ LocationManagerService f$8;
                                        private final /* synthetic */ CountryDetectorService f$9;

                                        {
                                            this.f$0 = r3;
                                            this.f$1 = r4;
                                            this.f$2 = r5;
                                            this.f$3 = r6;
                                            this.f$4 = r7;
                                            this.f$5 = r8;
                                            this.f$6 = r9;
                                            this.f$7 = r10;
                                            this.f$8 = r11;
                                            this.f$9 = r12;
                                            this.f$10 = r13;
                                            this.f$11 = r14;
                                            this.f$12 = r15;
                                            this.f$13 = r16;
                                            this.f$14 = r17;
                                            this.f$15 = r18;
                                            this.f$16 = r19;
                                            this.f$17 = r20;
                                        }

                                        public final void run() {
                                            SystemServer systemServer = this.f$0;
                                            Context context = this.f$1;
                                            WindowManagerService windowManagerService = this.f$2;
                                            NetworkManagementService networkManagementService = this.f$3;
                                            NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                            IpSecService ipSecService = this.f$5;
                                            NetworkStatsService networkStatsService = this.f$6;
                                            ConnectivityService connectivityService = this.f$7;
                                            LocationManagerService locationManagerService = this.f$8;
                                            CountryDetectorService countryDetectorService = this.f$9;
                                            NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                            CommonTimeManagementService commonTimeManagementService = this.f$11;
                                            InputManagerService inputManagerService = this.f$12;
                                            TelephonyRegistry telephonyRegistry = this.f$13;
                                            MediaRouterService mediaRouterService = this.f$14;
                                            MmsServiceBroker mmsServiceBroker = this.f$15;
                                            boolean z = this.f$16;
                                            boolean z2 = z;
                                            SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                        }
                                    };
                                    activityManagerService2222222222222.systemReady(r02222222222222, BOOT_TIMINGS_TRACE_LOG);
                                }
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                }
                                traceBeginAndSlog("StartBackgroundDexOptService");
                                BackgroundDexOptService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartPruneInstantAppsJobService");
                                PruneInstantAppsJobService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                traceBeginAndSlog("StartCrossProfileAppsService");
                                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                traceEnd();
                                connectivity = connectivity2;
                                countryDetector = countryDetector2;
                                iNetworkManagementService = iNetworkManagementService2;
                                iNetworkStatsService = iNetworkStatsService2;
                                IBinder iBinder11222 = iBinder10;
                                IBinder iBinder12222 = iBinder6;
                                networkTimeUpdater = networkTimeUpdater3;
                                IBinder iBinder13222 = iBinder2;
                                lockSettings2 = lockSettings322222222222;
                                INotificationManager iNotificationManager222 = notification2;
                                location = location3;
                                ipSecServiceF = iBinder5;
                                lockSettings = iLockSettings;
                                if (!isWatch) {
                                }
                                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                if (isWatch) {
                                }
                                if (!disableSlices) {
                                }
                                if (!disableCameraService) {
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                }
                                traceBeginAndSlog("StartStatsCompanionService");
                                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                traceEnd();
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                MmsServiceBroker mmsService22222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                if (isStartSysSvcCallRecord) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings2 != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                this.mSystemServiceManager.startBootPhase(480);
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                this.mSystemServiceManager.startBootPhase(500);
                                traceEnd();
                                traceBeginAndSlog("MakeWindowManagerServiceReady");
                                wm.systemReady();
                                traceEnd();
                                if (safeMode) {
                                }
                                Configuration config22222222222222 = wm.computeNewConfiguration(0);
                                DisplayMetrics metrics22222222222222 = new DisplayMetrics();
                                WindowManager w22222222222222 = (WindowManager) context.getSystemService("window");
                                w22222222222222.getDefaultDisplay().getMetrics(metrics22222222222222);
                                context.getResources().updateConfiguration(config22222222222222, metrics22222222222222);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                traceBeginAndSlog("StartDeviceSpecificServices");
                                String[] classes22222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                length = classes22222222222222.length;
                                ILockSettings lockSettings422222222222222 = lockSettings2;
                                i2 = 0;
                                WindowManagerService wm222222222222222 = wm;
                                while (i2 < length) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                traceEnd();
                                String[] strArr22222222222222 = classes22222222222222;
                                VibratorService vibratorService322222222222222 = vibrator;
                                Resources.Theme theme22222222222222 = systemTheme;
                                WindowManager windowManager22222222222222 = w22222222222222;
                                DisplayMetrics displayMetrics22222222222222 = metrics22222222222222;
                                Configuration configuration22222222222222 = config22222222222222;
                                boolean z222222222222222 = tuiEnable;
                                LocationManagerService locationManagerService22222222222222 = location;
                                ILockSettings iLockSettings222222222222222 = lockSettings422222222222222;
                                ActivityManagerService activityManagerService22222222222222 = this.mActivityManagerService;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022222222222222 = r12222222222222;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122222222222222 = new Runnable(this, context, wm222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22222222222222, enableIaware, safeMode) {
                                    private final /* synthetic */ SystemServer f$0;
                                    private final /* synthetic */ Context f$1;
                                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                                    private final /* synthetic */ CommonTimeManagementService f$11;
                                    private final /* synthetic */ InputManagerService f$12;
                                    private final /* synthetic */ TelephonyRegistry f$13;
                                    private final /* synthetic */ MediaRouterService f$14;
                                    private final /* synthetic */ MmsServiceBroker f$15;
                                    private final /* synthetic */ boolean f$16;
                                    private final /* synthetic */ boolean f$17;
                                    private final /* synthetic */ WindowManagerService f$2;
                                    private final /* synthetic */ NetworkManagementService f$3;
                                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                                    private final /* synthetic */ IpSecService f$5;
                                    private final /* synthetic */ NetworkStatsService f$6;
                                    private final /* synthetic */ ConnectivityService f$7;
                                    private final /* synthetic */ LocationManagerService f$8;
                                    private final /* synthetic */ CountryDetectorService f$9;

                                    {
                                        this.f$0 = r3;
                                        this.f$1 = r4;
                                        this.f$2 = r5;
                                        this.f$3 = r6;
                                        this.f$4 = r7;
                                        this.f$5 = r8;
                                        this.f$6 = r9;
                                        this.f$7 = r10;
                                        this.f$8 = r11;
                                        this.f$9 = r12;
                                        this.f$10 = r13;
                                        this.f$11 = r14;
                                        this.f$12 = r15;
                                        this.f$13 = r16;
                                        this.f$14 = r17;
                                        this.f$15 = r18;
                                        this.f$16 = r19;
                                        this.f$17 = r20;
                                    }

                                    public final void run() {
                                        SystemServer systemServer = this.f$0;
                                        Context context = this.f$1;
                                        WindowManagerService windowManagerService = this.f$2;
                                        NetworkManagementService networkManagementService = this.f$3;
                                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                        IpSecService ipSecService = this.f$5;
                                        NetworkStatsService networkStatsService = this.f$6;
                                        ConnectivityService connectivityService = this.f$7;
                                        LocationManagerService locationManagerService = this.f$8;
                                        CountryDetectorService countryDetectorService = this.f$9;
                                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                                        InputManagerService inputManagerService = this.f$12;
                                        TelephonyRegistry telephonyRegistry = this.f$13;
                                        MediaRouterService mediaRouterService = this.f$14;
                                        MmsServiceBroker mmsServiceBroker = this.f$15;
                                        boolean z = this.f$16;
                                        boolean z2 = z;
                                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                    }
                                };
                                activityManagerService22222222222222.systemReady(r022222222222222, BOOT_TIMINGS_TRACE_LOG);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartNsdService");
                            try {
                                iBinder = NsdService.create(context);
                                ServiceManager.addService("servicediscovery", iBinder);
                            } catch (Throwable th9) {
                                e = th9;
                                iBinder = null;
                                reportWtf("starting Service Discovery Service", e);
                                IBinder iBinder102 = iBinder;
                                traceEnd();
                                traceBeginAndSlog("StartSystemUpdateManagerService");
                                ServiceManager.addService("system_update", new SystemUpdateManagerService(context));
                                traceEnd();
                                traceBeginAndSlog("StartUpdateLockService");
                                ServiceManager.addService("updatelock", new UpdateLockService(context));
                                traceEnd();
                                traceBeginAndSlog("StartNotificationManager");
                                this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
                                SystemNotificationChannels.createAll(context);
                                INotificationManager notification3 = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                                traceEnd();
                                traceBeginAndSlog("StartDeviceMonitor");
                                this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
                                traceEnd();
                                StringBuilder sb2 = new StringBuilder();
                                INotificationManager notification22 = notification3;
                                sb2.append("TUI Connect enable ");
                                sb2.append(tuiEnable);
                                Slog.i(TAG, sb2.toString());
                                if (tuiEnable) {
                                }
                                if (vrDisplayEnable) {
                                }
                                traceBeginAndSlog("StartLocationManagerService");
                                Slog.i(TAG, "Location Manager");
                                hwLocation = HwServiceFactory.getHwLocationManagerService();
                                if (hwLocation != null) {
                                }
                                ServiceManager.addService("location", r5);
                                location2 = r5;
                                traceEnd();
                                traceBeginAndSlog("StartCountryDetectorService");
                                countryDetectorService = new CountryDetectorService(context);
                                ServiceManager.addService("country_detector", countryDetectorService);
                                boolean z32 = vrDisplayEnable;
                                countryDetector2 = countryDetectorService;
                                traceEnd();
                                if (!isWatch) {
                                }
                                if (context.getResources().getBoolean(17956968)) {
                                }
                                traceBeginAndSlog("StartTrustManager");
                                this.mSystemServiceManager.startService(TrustManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartAudioService");
                                this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartDockObserver");
                                this.mSystemServiceManager.startService(DockObserver.class);
                                traceEnd();
                                if (isWatch) {
                                }
                                traceBeginAndSlog("StartWiredAccessoryManager");
                                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                                }
                                traceBeginAndSlog("StartUsbService");
                                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                                traceEnd();
                                if (isWatch) {
                                }
                                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                                iBinder3 = new HardwarePropertiesManagerService(context);
                                ServiceManager.addService("hardware_properties", iBinder3);
                                iBinder2 = iBinder3;
                                traceEnd();
                                traceBeginAndSlog("StartTwilightService");
                                this.mSystemServiceManager.startService(TwilightService.class);
                                traceEnd();
                                if (ColorDisplayController.isAvailable(context)) {
                                }
                                this.mSystemServiceManager.startService(JobSchedulerService.class);
                                traceBeginAndSlog("StartSoundTrigger");
                                this.mSystemServiceManager.startService(SoundTriggerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                }
                                traceBeginAndSlog("StartAppWidgerService");
                                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                traceEnd();
                                traceBeginAndSlog("StartVoiceRecognitionManager");
                                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                traceEnd();
                                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                                }
                                traceBeginAndSlog("StartSensorNotification");
                                this.mSystemServiceManager.startService(SensorNotificationService.class);
                                traceEnd();
                                traceBeginAndSlog("StartContextHubSystemService");
                                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                traceEnd();
                                HwServiceFactory.setupHwServices(context);
                                traceBeginAndSlog("StartDiskStatsService");
                                ServiceManager.addService("diskstats", new DiskStatsService(context));
                                traceEnd();
                                startRulesManagerService = this.mOnlyCore && context.getResources().getBoolean(17956967);
                                if (startRulesManagerService) {
                                }
                                Slog.i(TAG, "attestation Service");
                                attestation = HwServiceFactory.getHwAttestationService();
                                if (attestation == null) {
                                }
                                if (!isWatch) {
                                }
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                traceEnd();
                                traceBeginAndSlog("CertBlacklister");
                                new CertBlacklister(context);
                                traceEnd();
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                traceBeginAndSlog("StartDreamManager");
                                this.mSystemServiceManager.startService(DreamManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("AddGraphicsStatsService");
                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                traceEnd();
                                if (CoverageService.ENABLED) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                }
                                traceBeginAndSlog("StartRestrictionManager");
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaSessionService");
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaUpdateService");
                                this.mSystemServiceManager.startService(MediaUpdateService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                traceBeginAndSlog("StartTvInputManager");
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                traceBeginAndSlog("StartMediaRouterService");
                                mediaRouterService = new MediaRouterService(context);
                                ServiceManager.addService("media_router", mediaRouterService);
                                commonTimeMgmtService = commonTimeMgmtService2;
                                mediaRouter2 = mediaRouterService;
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                }
                                traceBeginAndSlog("StartBackgroundDexOptService");
                                BackgroundDexOptService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartPruneInstantAppsJobService");
                                PruneInstantAppsJobService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                traceBeginAndSlog("StartCrossProfileAppsService");
                                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                traceEnd();
                                connectivity = connectivity2;
                                countryDetector = countryDetector2;
                                iNetworkManagementService = iNetworkManagementService2;
                                iNetworkStatsService = iNetworkStatsService2;
                                IBinder iBinder112222 = iBinder102;
                                IBinder iBinder122222 = iBinder6;
                                networkTimeUpdater = networkTimeUpdater3;
                                IBinder iBinder132222 = iBinder2;
                                lockSettings2 = lockSettings322222222222;
                                INotificationManager iNotificationManager2222 = notification22;
                                location = location3;
                                ipSecServiceF = iBinder5;
                                lockSettings = iLockSettings;
                                if (!isWatch) {
                                }
                                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                if (isWatch) {
                                }
                                if (!disableSlices) {
                                }
                                if (!disableCameraService) {
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                }
                                traceBeginAndSlog("StartStatsCompanionService");
                                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                traceEnd();
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                MmsServiceBroker mmsService222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                if (isStartSysSvcCallRecord) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings2 != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                this.mSystemServiceManager.startBootPhase(480);
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                this.mSystemServiceManager.startBootPhase(500);
                                traceEnd();
                                traceBeginAndSlog("MakeWindowManagerServiceReady");
                                wm.systemReady();
                                traceEnd();
                                if (safeMode) {
                                }
                                Configuration config222222222222222 = wm.computeNewConfiguration(0);
                                DisplayMetrics metrics222222222222222 = new DisplayMetrics();
                                WindowManager w222222222222222 = (WindowManager) context.getSystemService("window");
                                w222222222222222.getDefaultDisplay().getMetrics(metrics222222222222222);
                                context.getResources().updateConfiguration(config222222222222222, metrics222222222222222);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                traceBeginAndSlog("StartDeviceSpecificServices");
                                String[] classes222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                length = classes222222222222222.length;
                                ILockSettings lockSettings4222222222222222 = lockSettings2;
                                i2 = 0;
                                WindowManagerService wm2222222222222222 = wm;
                                while (i2 < length) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                traceEnd();
                                String[] strArr222222222222222 = classes222222222222222;
                                VibratorService vibratorService3222222222222222 = vibrator;
                                Resources.Theme theme222222222222222 = systemTheme;
                                WindowManager windowManager222222222222222 = w222222222222222;
                                DisplayMetrics displayMetrics222222222222222 = metrics222222222222222;
                                Configuration configuration222222222222222 = config222222222222222;
                                boolean z2222222222222222 = tuiEnable;
                                LocationManagerService locationManagerService222222222222222 = location;
                                ILockSettings iLockSettings2222222222222222 = lockSettings4222222222222222;
                                ActivityManagerService activityManagerService222222222222222 = this.mActivityManagerService;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222222222222222 = r122222222222222;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222222222222222 = new Runnable(this, context, wm2222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222222222222222, enableIaware, safeMode) {
                                    private final /* synthetic */ SystemServer f$0;
                                    private final /* synthetic */ Context f$1;
                                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                                    private final /* synthetic */ CommonTimeManagementService f$11;
                                    private final /* synthetic */ InputManagerService f$12;
                                    private final /* synthetic */ TelephonyRegistry f$13;
                                    private final /* synthetic */ MediaRouterService f$14;
                                    private final /* synthetic */ MmsServiceBroker f$15;
                                    private final /* synthetic */ boolean f$16;
                                    private final /* synthetic */ boolean f$17;
                                    private final /* synthetic */ WindowManagerService f$2;
                                    private final /* synthetic */ NetworkManagementService f$3;
                                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                                    private final /* synthetic */ IpSecService f$5;
                                    private final /* synthetic */ NetworkStatsService f$6;
                                    private final /* synthetic */ ConnectivityService f$7;
                                    private final /* synthetic */ LocationManagerService f$8;
                                    private final /* synthetic */ CountryDetectorService f$9;

                                    {
                                        this.f$0 = r3;
                                        this.f$1 = r4;
                                        this.f$2 = r5;
                                        this.f$3 = r6;
                                        this.f$4 = r7;
                                        this.f$5 = r8;
                                        this.f$6 = r9;
                                        this.f$7 = r10;
                                        this.f$8 = r11;
                                        this.f$9 = r12;
                                        this.f$10 = r13;
                                        this.f$11 = r14;
                                        this.f$12 = r15;
                                        this.f$13 = r16;
                                        this.f$14 = r17;
                                        this.f$15 = r18;
                                        this.f$16 = r19;
                                        this.f$17 = r20;
                                    }

                                    public final void run() {
                                        SystemServer systemServer = this.f$0;
                                        Context context = this.f$1;
                                        WindowManagerService windowManagerService = this.f$2;
                                        NetworkManagementService networkManagementService = this.f$3;
                                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                        IpSecService ipSecService = this.f$5;
                                        NetworkStatsService networkStatsService = this.f$6;
                                        ConnectivityService connectivityService = this.f$7;
                                        LocationManagerService locationManagerService = this.f$8;
                                        CountryDetectorService countryDetectorService = this.f$9;
                                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                                        InputManagerService inputManagerService = this.f$12;
                                        TelephonyRegistry telephonyRegistry = this.f$13;
                                        MediaRouterService mediaRouterService = this.f$14;
                                        MmsServiceBroker mmsServiceBroker = this.f$15;
                                        boolean z = this.f$16;
                                        boolean z2 = z;
                                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                    }
                                };
                                activityManagerService222222222222222.systemReady(r0222222222222222, BOOT_TIMINGS_TRACE_LOG);
                            }
                            IBinder iBinder1022 = iBinder;
                            traceEnd();
                            traceBeginAndSlog("StartSystemUpdateManagerService");
                            try {
                                ServiceManager.addService("system_update", new SystemUpdateManagerService(context));
                            } catch (Throwable e27) {
                                reportWtf("starting SystemUpdateManagerService", e27);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartUpdateLockService");
                            try {
                                ServiceManager.addService("updatelock", new UpdateLockService(context));
                            } catch (Throwable e28) {
                                reportWtf("starting UpdateLockService", e28);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartNotificationManager");
                            try {
                                this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
                            } catch (RuntimeException e29) {
                                this.mSystemServiceManager.startService(NotificationManagerService.class);
                            }
                            SystemNotificationChannels.createAll(context);
                            INotificationManager notification32 = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                            traceEnd();
                            traceBeginAndSlog("StartDeviceMonitor");
                            this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
                            traceEnd();
                            StringBuilder sb22 = new StringBuilder();
                            INotificationManager notification222 = notification32;
                            sb22.append("TUI Connect enable ");
                            sb22.append(tuiEnable);
                            Slog.i(TAG, sb22.toString());
                            if (tuiEnable) {
                                try {
                                    ServiceManager.addService("tui", new TrustedUIService(context));
                                } catch (Throwable e30) {
                                    Slog.e(TAG, "Failure starting TUI Service ", e30);
                                }
                            }
                            if (vrDisplayEnable) {
                                Slog.i(TAG, "VR Display enable " + vrDisplayEnable);
                                try {
                                    ServiceManager.addService("vr_display", new VRManagerService(context));
                                } catch (Throwable e31) {
                                    Slog.e(TAG, "Failure starting VR Service ", e31);
                                }
                            }
                            traceBeginAndSlog("StartLocationManagerService");
                            try {
                                Slog.i(TAG, "Location Manager");
                                hwLocation = HwServiceFactory.getHwLocationManagerService();
                                if (hwLocation != null) {
                                    r5 = hwLocation.getInstance(context);
                                } else {
                                    r5 = new LocationManagerService(context);
                                }
                                ServiceManager.addService("location", r5);
                                location2 = r5;
                            } catch (Throwable th10) {
                                e = th10;
                                location4 = null;
                                reportWtf("starting Location Manager", e);
                                location2 = location4;
                                traceEnd();
                                traceBeginAndSlog("StartCountryDetectorService");
                                countryDetectorService = new CountryDetectorService(context);
                                ServiceManager.addService("country_detector", countryDetectorService);
                                boolean z322 = vrDisplayEnable;
                                countryDetector2 = countryDetectorService;
                                traceEnd();
                                if (!isWatch) {
                                }
                                if (context.getResources().getBoolean(17956968)) {
                                }
                                traceBeginAndSlog("StartTrustManager");
                                this.mSystemServiceManager.startService(TrustManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartAudioService");
                                this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartDockObserver");
                                this.mSystemServiceManager.startService(DockObserver.class);
                                traceEnd();
                                if (isWatch) {
                                }
                                traceBeginAndSlog("StartWiredAccessoryManager");
                                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                                }
                                traceBeginAndSlog("StartUsbService");
                                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                                traceEnd();
                                if (isWatch) {
                                }
                                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                                iBinder3 = new HardwarePropertiesManagerService(context);
                                ServiceManager.addService("hardware_properties", iBinder3);
                                iBinder2 = iBinder3;
                                traceEnd();
                                traceBeginAndSlog("StartTwilightService");
                                this.mSystemServiceManager.startService(TwilightService.class);
                                traceEnd();
                                if (ColorDisplayController.isAvailable(context)) {
                                }
                                this.mSystemServiceManager.startService(JobSchedulerService.class);
                                traceBeginAndSlog("StartSoundTrigger");
                                this.mSystemServiceManager.startService(SoundTriggerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                }
                                traceBeginAndSlog("StartAppWidgerService");
                                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                traceEnd();
                                traceBeginAndSlog("StartVoiceRecognitionManager");
                                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                traceEnd();
                                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                                }
                                traceBeginAndSlog("StartSensorNotification");
                                this.mSystemServiceManager.startService(SensorNotificationService.class);
                                traceEnd();
                                traceBeginAndSlog("StartContextHubSystemService");
                                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                traceEnd();
                                HwServiceFactory.setupHwServices(context);
                                traceBeginAndSlog("StartDiskStatsService");
                                ServiceManager.addService("diskstats", new DiskStatsService(context));
                                traceEnd();
                                startRulesManagerService = this.mOnlyCore && context.getResources().getBoolean(17956967);
                                if (startRulesManagerService) {
                                }
                                Slog.i(TAG, "attestation Service");
                                attestation = HwServiceFactory.getHwAttestationService();
                                if (attestation == null) {
                                }
                                if (!isWatch) {
                                }
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                traceEnd();
                                traceBeginAndSlog("CertBlacklister");
                                new CertBlacklister(context);
                                traceEnd();
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                traceBeginAndSlog("StartDreamManager");
                                this.mSystemServiceManager.startService(DreamManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("AddGraphicsStatsService");
                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                traceEnd();
                                if (CoverageService.ENABLED) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                }
                                traceBeginAndSlog("StartRestrictionManager");
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaSessionService");
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaUpdateService");
                                this.mSystemServiceManager.startService(MediaUpdateService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                traceBeginAndSlog("StartTvInputManager");
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                traceBeginAndSlog("StartMediaRouterService");
                                mediaRouterService = new MediaRouterService(context);
                                ServiceManager.addService("media_router", mediaRouterService);
                                commonTimeMgmtService = commonTimeMgmtService2;
                                mediaRouter2 = mediaRouterService;
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                }
                                traceBeginAndSlog("StartBackgroundDexOptService");
                                BackgroundDexOptService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartPruneInstantAppsJobService");
                                PruneInstantAppsJobService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                traceBeginAndSlog("StartCrossProfileAppsService");
                                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                traceEnd();
                                connectivity = connectivity2;
                                countryDetector = countryDetector2;
                                iNetworkManagementService = iNetworkManagementService2;
                                iNetworkStatsService = iNetworkStatsService2;
                                IBinder iBinder1122222 = iBinder1022;
                                IBinder iBinder1222222 = iBinder6;
                                networkTimeUpdater = networkTimeUpdater3;
                                IBinder iBinder1322222 = iBinder2;
                                lockSettings2 = lockSettings322222222222;
                                INotificationManager iNotificationManager22222 = notification222;
                                location = location3;
                                ipSecServiceF = iBinder5;
                                lockSettings = iLockSettings;
                                if (!isWatch) {
                                }
                                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                if (isWatch) {
                                }
                                if (!disableSlices) {
                                }
                                if (!disableCameraService) {
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                }
                                traceBeginAndSlog("StartStatsCompanionService");
                                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                traceEnd();
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                MmsServiceBroker mmsService2222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                if (isStartSysSvcCallRecord) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings2 != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                this.mSystemServiceManager.startBootPhase(480);
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                this.mSystemServiceManager.startBootPhase(500);
                                traceEnd();
                                traceBeginAndSlog("MakeWindowManagerServiceReady");
                                wm.systemReady();
                                traceEnd();
                                if (safeMode) {
                                }
                                Configuration config2222222222222222 = wm.computeNewConfiguration(0);
                                DisplayMetrics metrics2222222222222222 = new DisplayMetrics();
                                WindowManager w2222222222222222 = (WindowManager) context.getSystemService("window");
                                w2222222222222222.getDefaultDisplay().getMetrics(metrics2222222222222222);
                                context.getResources().updateConfiguration(config2222222222222222, metrics2222222222222222);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                traceBeginAndSlog("StartDeviceSpecificServices");
                                String[] classes2222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                length = classes2222222222222222.length;
                                ILockSettings lockSettings42222222222222222 = lockSettings2;
                                i2 = 0;
                                WindowManagerService wm22222222222222222 = wm;
                                while (i2 < length) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                traceEnd();
                                String[] strArr2222222222222222 = classes2222222222222222;
                                VibratorService vibratorService32222222222222222 = vibrator;
                                Resources.Theme theme2222222222222222 = systemTheme;
                                WindowManager windowManager2222222222222222 = w2222222222222222;
                                DisplayMetrics displayMetrics2222222222222222 = metrics2222222222222222;
                                Configuration configuration2222222222222222 = config2222222222222222;
                                boolean z22222222222222222 = tuiEnable;
                                LocationManagerService locationManagerService2222222222222222 = location;
                                ILockSettings iLockSettings22222222222222222 = lockSettings42222222222222222;
                                ActivityManagerService activityManagerService2222222222222222 = this.mActivityManagerService;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222222222222222 = r1222222222222222;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222222222222222 = new Runnable(this, context, wm22222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222222222222222, enableIaware, safeMode) {
                                    private final /* synthetic */ SystemServer f$0;
                                    private final /* synthetic */ Context f$1;
                                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                                    private final /* synthetic */ CommonTimeManagementService f$11;
                                    private final /* synthetic */ InputManagerService f$12;
                                    private final /* synthetic */ TelephonyRegistry f$13;
                                    private final /* synthetic */ MediaRouterService f$14;
                                    private final /* synthetic */ MmsServiceBroker f$15;
                                    private final /* synthetic */ boolean f$16;
                                    private final /* synthetic */ boolean f$17;
                                    private final /* synthetic */ WindowManagerService f$2;
                                    private final /* synthetic */ NetworkManagementService f$3;
                                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                                    private final /* synthetic */ IpSecService f$5;
                                    private final /* synthetic */ NetworkStatsService f$6;
                                    private final /* synthetic */ ConnectivityService f$7;
                                    private final /* synthetic */ LocationManagerService f$8;
                                    private final /* synthetic */ CountryDetectorService f$9;

                                    {
                                        this.f$0 = r3;
                                        this.f$1 = r4;
                                        this.f$2 = r5;
                                        this.f$3 = r6;
                                        this.f$4 = r7;
                                        this.f$5 = r8;
                                        this.f$6 = r9;
                                        this.f$7 = r10;
                                        this.f$8 = r11;
                                        this.f$9 = r12;
                                        this.f$10 = r13;
                                        this.f$11 = r14;
                                        this.f$12 = r15;
                                        this.f$13 = r16;
                                        this.f$14 = r17;
                                        this.f$15 = r18;
                                        this.f$16 = r19;
                                        this.f$17 = r20;
                                    }

                                    public final void run() {
                                        SystemServer systemServer = this.f$0;
                                        Context context = this.f$1;
                                        WindowManagerService windowManagerService = this.f$2;
                                        NetworkManagementService networkManagementService = this.f$3;
                                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                        IpSecService ipSecService = this.f$5;
                                        NetworkStatsService networkStatsService = this.f$6;
                                        ConnectivityService connectivityService = this.f$7;
                                        LocationManagerService locationManagerService = this.f$8;
                                        CountryDetectorService countryDetectorService = this.f$9;
                                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                                        InputManagerService inputManagerService = this.f$12;
                                        TelephonyRegistry telephonyRegistry = this.f$13;
                                        MediaRouterService mediaRouterService = this.f$14;
                                        MmsServiceBroker mmsServiceBroker = this.f$15;
                                        boolean z = this.f$16;
                                        boolean z2 = z;
                                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                    }
                                };
                                activityManagerService2222222222222222.systemReady(r02222222222222222, BOOT_TIMINGS_TRACE_LOG);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartCountryDetectorService");
                            try {
                                countryDetectorService = new CountryDetectorService(context);
                                ServiceManager.addService("country_detector", countryDetectorService);
                                boolean z3222 = vrDisplayEnable;
                                countryDetector2 = countryDetectorService;
                            } catch (Throwable th11) {
                                e = th11;
                                countryDetector3 = null;
                                boolean z42 = vrDisplayEnable;
                                reportWtf("starting Country Detector", e);
                                countryDetector2 = countryDetector3;
                                traceEnd();
                                if (!isWatch) {
                                }
                                if (context.getResources().getBoolean(17956968)) {
                                }
                                traceBeginAndSlog("StartTrustManager");
                                this.mSystemServiceManager.startService(TrustManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartAudioService");
                                this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartDockObserver");
                                this.mSystemServiceManager.startService(DockObserver.class);
                                traceEnd();
                                if (isWatch) {
                                }
                                traceBeginAndSlog("StartWiredAccessoryManager");
                                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                                }
                                traceBeginAndSlog("StartUsbService");
                                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                                traceEnd();
                                if (isWatch) {
                                }
                                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                                iBinder3 = new HardwarePropertiesManagerService(context);
                                ServiceManager.addService("hardware_properties", iBinder3);
                                iBinder2 = iBinder3;
                                traceEnd();
                                traceBeginAndSlog("StartTwilightService");
                                this.mSystemServiceManager.startService(TwilightService.class);
                                traceEnd();
                                if (ColorDisplayController.isAvailable(context)) {
                                }
                                this.mSystemServiceManager.startService(JobSchedulerService.class);
                                traceBeginAndSlog("StartSoundTrigger");
                                this.mSystemServiceManager.startService(SoundTriggerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                }
                                traceBeginAndSlog("StartAppWidgerService");
                                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                traceEnd();
                                traceBeginAndSlog("StartVoiceRecognitionManager");
                                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                traceEnd();
                                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                                }
                                traceBeginAndSlog("StartSensorNotification");
                                this.mSystemServiceManager.startService(SensorNotificationService.class);
                                traceEnd();
                                traceBeginAndSlog("StartContextHubSystemService");
                                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                traceEnd();
                                HwServiceFactory.setupHwServices(context);
                                traceBeginAndSlog("StartDiskStatsService");
                                ServiceManager.addService("diskstats", new DiskStatsService(context));
                                traceEnd();
                                startRulesManagerService = this.mOnlyCore && context.getResources().getBoolean(17956967);
                                if (startRulesManagerService) {
                                }
                                Slog.i(TAG, "attestation Service");
                                attestation = HwServiceFactory.getHwAttestationService();
                                if (attestation == null) {
                                }
                                if (!isWatch) {
                                }
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                traceEnd();
                                traceBeginAndSlog("CertBlacklister");
                                new CertBlacklister(context);
                                traceEnd();
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                traceBeginAndSlog("StartDreamManager");
                                this.mSystemServiceManager.startService(DreamManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("AddGraphicsStatsService");
                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                traceEnd();
                                if (CoverageService.ENABLED) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                }
                                traceBeginAndSlog("StartRestrictionManager");
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaSessionService");
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaUpdateService");
                                this.mSystemServiceManager.startService(MediaUpdateService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                traceBeginAndSlog("StartTvInputManager");
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                traceBeginAndSlog("StartMediaRouterService");
                                mediaRouterService = new MediaRouterService(context);
                                ServiceManager.addService("media_router", mediaRouterService);
                                commonTimeMgmtService = commonTimeMgmtService2;
                                mediaRouter2 = mediaRouterService;
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                }
                                traceBeginAndSlog("StartBackgroundDexOptService");
                                BackgroundDexOptService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartPruneInstantAppsJobService");
                                PruneInstantAppsJobService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                traceBeginAndSlog("StartCrossProfileAppsService");
                                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                traceEnd();
                                connectivity = connectivity2;
                                countryDetector = countryDetector2;
                                iNetworkManagementService = iNetworkManagementService2;
                                iNetworkStatsService = iNetworkStatsService2;
                                IBinder iBinder11222222 = iBinder1022;
                                IBinder iBinder12222222 = iBinder6;
                                networkTimeUpdater = networkTimeUpdater3;
                                IBinder iBinder13222222 = iBinder2;
                                lockSettings2 = lockSettings322222222222;
                                INotificationManager iNotificationManager222222 = notification222;
                                location = location3;
                                ipSecServiceF = iBinder5;
                                lockSettings = iLockSettings;
                                if (!isWatch) {
                                }
                                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                if (isWatch) {
                                }
                                if (!disableSlices) {
                                }
                                if (!disableCameraService) {
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                }
                                traceBeginAndSlog("StartStatsCompanionService");
                                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                traceEnd();
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                MmsServiceBroker mmsService22222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                if (isStartSysSvcCallRecord) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings2 != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                this.mSystemServiceManager.startBootPhase(480);
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                this.mSystemServiceManager.startBootPhase(500);
                                traceEnd();
                                traceBeginAndSlog("MakeWindowManagerServiceReady");
                                wm.systemReady();
                                traceEnd();
                                if (safeMode) {
                                }
                                Configuration config22222222222222222 = wm.computeNewConfiguration(0);
                                DisplayMetrics metrics22222222222222222 = new DisplayMetrics();
                                WindowManager w22222222222222222 = (WindowManager) context.getSystemService("window");
                                w22222222222222222.getDefaultDisplay().getMetrics(metrics22222222222222222);
                                context.getResources().updateConfiguration(config22222222222222222, metrics22222222222222222);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                traceBeginAndSlog("StartDeviceSpecificServices");
                                String[] classes22222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                length = classes22222222222222222.length;
                                ILockSettings lockSettings422222222222222222 = lockSettings2;
                                i2 = 0;
                                WindowManagerService wm222222222222222222 = wm;
                                while (i2 < length) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                traceEnd();
                                String[] strArr22222222222222222 = classes22222222222222222;
                                VibratorService vibratorService322222222222222222 = vibrator;
                                Resources.Theme theme22222222222222222 = systemTheme;
                                WindowManager windowManager22222222222222222 = w22222222222222222;
                                DisplayMetrics displayMetrics22222222222222222 = metrics22222222222222222;
                                Configuration configuration22222222222222222 = config22222222222222222;
                                boolean z222222222222222222 = tuiEnable;
                                LocationManagerService locationManagerService22222222222222222 = location;
                                ILockSettings iLockSettings222222222222222222 = lockSettings422222222222222222;
                                ActivityManagerService activityManagerService22222222222222222 = this.mActivityManagerService;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022222222222222222 = r12222222222222222;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122222222222222222 = new Runnable(this, context, wm222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22222222222222222, enableIaware, safeMode) {
                                    private final /* synthetic */ SystemServer f$0;
                                    private final /* synthetic */ Context f$1;
                                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                                    private final /* synthetic */ CommonTimeManagementService f$11;
                                    private final /* synthetic */ InputManagerService f$12;
                                    private final /* synthetic */ TelephonyRegistry f$13;
                                    private final /* synthetic */ MediaRouterService f$14;
                                    private final /* synthetic */ MmsServiceBroker f$15;
                                    private final /* synthetic */ boolean f$16;
                                    private final /* synthetic */ boolean f$17;
                                    private final /* synthetic */ WindowManagerService f$2;
                                    private final /* synthetic */ NetworkManagementService f$3;
                                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                                    private final /* synthetic */ IpSecService f$5;
                                    private final /* synthetic */ NetworkStatsService f$6;
                                    private final /* synthetic */ ConnectivityService f$7;
                                    private final /* synthetic */ LocationManagerService f$8;
                                    private final /* synthetic */ CountryDetectorService f$9;

                                    {
                                        this.f$0 = r3;
                                        this.f$1 = r4;
                                        this.f$2 = r5;
                                        this.f$3 = r6;
                                        this.f$4 = r7;
                                        this.f$5 = r8;
                                        this.f$6 = r9;
                                        this.f$7 = r10;
                                        this.f$8 = r11;
                                        this.f$9 = r12;
                                        this.f$10 = r13;
                                        this.f$11 = r14;
                                        this.f$12 = r15;
                                        this.f$13 = r16;
                                        this.f$14 = r17;
                                        this.f$15 = r18;
                                        this.f$16 = r19;
                                        this.f$17 = r20;
                                    }

                                    public final void run() {
                                        SystemServer systemServer = this.f$0;
                                        Context context = this.f$1;
                                        WindowManagerService windowManagerService = this.f$2;
                                        NetworkManagementService networkManagementService = this.f$3;
                                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                        IpSecService ipSecService = this.f$5;
                                        NetworkStatsService networkStatsService = this.f$6;
                                        ConnectivityService connectivityService = this.f$7;
                                        LocationManagerService locationManagerService = this.f$8;
                                        CountryDetectorService countryDetectorService = this.f$9;
                                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                                        InputManagerService inputManagerService = this.f$12;
                                        TelephonyRegistry telephonyRegistry = this.f$13;
                                        MediaRouterService mediaRouterService = this.f$14;
                                        MmsServiceBroker mmsServiceBroker = this.f$15;
                                        boolean z = this.f$16;
                                        boolean z2 = z;
                                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                    }
                                };
                                activityManagerService22222222222222222.systemReady(r022222222222222222, BOOT_TIMINGS_TRACE_LOG);
                            }
                            traceEnd();
                            if (!isWatch) {
                                traceBeginAndSlog("StartSearchManagerService");
                                try {
                                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                                } catch (Throwable e32) {
                                    reportWtf("starting Search Service", e32);
                                }
                                traceEnd();
                            }
                            if (context.getResources().getBoolean(17956968)) {
                                traceBeginAndSlog("StartWallpaperManagerService");
                                this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                                traceEnd();
                            }
                            traceBeginAndSlog("StartTrustManager");
                            this.mSystemServiceManager.startService(TrustManagerService.class);
                            traceEnd();
                            traceBeginAndSlog("StartAudioService");
                            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartDockObserver");
                            this.mSystemServiceManager.startService(DockObserver.class);
                            traceEnd();
                            if (isWatch) {
                                traceBeginAndSlog("StartThermalObserver");
                                this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                                traceEnd();
                            }
                            traceBeginAndSlog("StartWiredAccessoryManager");
                            try {
                                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                            } catch (Throwable e33) {
                                reportWtf("starting WiredAccessoryManager", e33);
                            }
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                                traceBeginAndSlog("StartMidiManager");
                                this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                                traceEnd();
                            }
                            if (this.mPackageManager.hasSystemFeature("android.hardware.usb.host") || this.mPackageManager.hasSystemFeature("android.hardware.usb.accessory") || isEmulator) {
                                traceBeginAndSlog("StartUsbService");
                                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                                traceEnd();
                            }
                            if (isWatch) {
                                traceBeginAndSlog("StartSerialService");
                                try {
                                    iBinder4 = new SerialService(context);
                                    try {
                                        ServiceManager.addService("serial", iBinder4);
                                        location3 = location2;
                                    } catch (Throwable th12) {
                                        e = th12;
                                        location3 = location2;
                                        Slog.e(TAG, "Failure starting SerialService", e);
                                        iBinder4 = iBinder4;
                                        traceEnd();
                                        iBinder6 = iBinder4;
                                        traceBeginAndSlog("StartHardwarePropertiesManagerService");
                                        iBinder3 = new HardwarePropertiesManagerService(context);
                                        ServiceManager.addService("hardware_properties", iBinder3);
                                        iBinder2 = iBinder3;
                                        traceEnd();
                                        traceBeginAndSlog("StartTwilightService");
                                        this.mSystemServiceManager.startService(TwilightService.class);
                                        traceEnd();
                                        if (ColorDisplayController.isAvailable(context)) {
                                        }
                                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                                        traceBeginAndSlog("StartSoundTrigger");
                                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                                        traceEnd();
                                        if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                        }
                                        traceBeginAndSlog("StartAppWidgerService");
                                        this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                        traceEnd();
                                        traceBeginAndSlog("StartVoiceRecognitionManager");
                                        this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                        traceEnd();
                                        if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                                        }
                                        traceBeginAndSlog("StartSensorNotification");
                                        this.mSystemServiceManager.startService(SensorNotificationService.class);
                                        traceEnd();
                                        traceBeginAndSlog("StartContextHubSystemService");
                                        this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                        traceEnd();
                                        HwServiceFactory.setupHwServices(context);
                                        traceBeginAndSlog("StartDiskStatsService");
                                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                                        traceEnd();
                                        startRulesManagerService = this.mOnlyCore && context.getResources().getBoolean(17956967);
                                        if (startRulesManagerService) {
                                        }
                                        Slog.i(TAG, "attestation Service");
                                        attestation = HwServiceFactory.getHwAttestationService();
                                        if (attestation == null) {
                                        }
                                        if (!isWatch) {
                                        }
                                        traceBeginAndSlog("StartCommonTimeManagementService");
                                        commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                        ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                        traceEnd();
                                        traceBeginAndSlog("CertBlacklister");
                                        new CertBlacklister(context);
                                        traceEnd();
                                        traceBeginAndSlog("StartEmergencyAffordanceService");
                                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                        traceEnd();
                                        traceBeginAndSlog("StartDreamManager");
                                        this.mSystemServiceManager.startService(DreamManagerService.class);
                                        traceEnd();
                                        traceBeginAndSlog("AddGraphicsStatsService");
                                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                        traceEnd();
                                        if (CoverageService.ENABLED) {
                                        }
                                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                        }
                                        if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                        }
                                        traceBeginAndSlog("StartRestrictionManager");
                                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                        traceEnd();
                                        traceBeginAndSlog("StartMediaSessionService");
                                        this.mSystemServiceManager.startService(MediaSessionService.class);
                                        traceEnd();
                                        traceBeginAndSlog("StartMediaUpdateService");
                                        this.mSystemServiceManager.startService(MediaUpdateService.class);
                                        traceEnd();
                                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                        }
                                        traceBeginAndSlog("StartTvInputManager");
                                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                                        traceEnd();
                                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                        }
                                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                        }
                                        traceBeginAndSlog("StartMediaRouterService");
                                        mediaRouterService = new MediaRouterService(context);
                                        ServiceManager.addService("media_router", mediaRouterService);
                                        commonTimeMgmtService = commonTimeMgmtService2;
                                        mediaRouter2 = mediaRouterService;
                                        traceEnd();
                                        if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                        }
                                        traceBeginAndSlog("StartBackgroundDexOptService");
                                        BackgroundDexOptService.schedule(context);
                                        traceEnd();
                                        traceBeginAndSlog("StartPruneInstantAppsJobService");
                                        PruneInstantAppsJobService.schedule(context);
                                        traceEnd();
                                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                        traceEnd();
                                        traceBeginAndSlog("StartLauncherAppsService");
                                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                                        traceEnd();
                                        traceBeginAndSlog("StartCrossProfileAppsService");
                                        this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                        traceEnd();
                                        connectivity = connectivity2;
                                        countryDetector = countryDetector2;
                                        iNetworkManagementService = iNetworkManagementService2;
                                        iNetworkStatsService = iNetworkStatsService2;
                                        IBinder iBinder112222222 = iBinder1022;
                                        IBinder iBinder122222222 = iBinder6;
                                        networkTimeUpdater = networkTimeUpdater3;
                                        IBinder iBinder132222222 = iBinder2;
                                        lockSettings2 = lockSettings322222222222;
                                        INotificationManager iNotificationManager2222222 = notification222;
                                        location = location3;
                                        ipSecServiceF = iBinder5;
                                        lockSettings = iLockSettings;
                                        if (!isWatch) {
                                        }
                                        MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                        if (isWatch) {
                                        }
                                        if (!disableSlices) {
                                        }
                                        if (!disableCameraService) {
                                        }
                                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                        }
                                        traceBeginAndSlog("StartStatsCompanionService");
                                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                        traceEnd();
                                        safeMode = wm.detectSafeMode();
                                        this.mSystemServiceManager.setSafeMode(safeMode);
                                        if (safeMode) {
                                        }
                                        traceBeginAndSlog("StartMmsService");
                                        MmsServiceBroker mmsService222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                        traceEnd();
                                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                        }
                                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                        }
                                        if (isStartSysSvcCallRecord) {
                                        }
                                        traceBeginAndSlog("MakeVibratorServiceReady");
                                        vibrator.systemReady();
                                        traceEnd();
                                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                                        if (lockSettings2 != null) {
                                        }
                                        traceEnd();
                                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                        this.mSystemServiceManager.startBootPhase(480);
                                        traceEnd();
                                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                        this.mSystemServiceManager.startBootPhase(500);
                                        traceEnd();
                                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                                        wm.systemReady();
                                        traceEnd();
                                        if (safeMode) {
                                        }
                                        Configuration config222222222222222222 = wm.computeNewConfiguration(0);
                                        DisplayMetrics metrics222222222222222222 = new DisplayMetrics();
                                        WindowManager w222222222222222222 = (WindowManager) context.getSystemService("window");
                                        w222222222222222222.getDefaultDisplay().getMetrics(metrics222222222222222222);
                                        context.getResources().updateConfiguration(config222222222222222222, metrics222222222222222222);
                                        systemTheme = context.getTheme();
                                        if (systemTheme.getChangingConfigurations() != 0) {
                                        }
                                        traceBeginAndSlog("MakePowerManagerServiceReady");
                                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                        traceEnd();
                                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                        traceBeginAndSlog("MakePackageManagerServiceReady");
                                        this.mPackageManagerService.systemReady();
                                        traceEnd();
                                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                        traceEnd();
                                        this.mSystemServiceManager.setSafeMode(safeMode);
                                        traceBeginAndSlog("StartDeviceSpecificServices");
                                        String[] classes222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                        length = classes222222222222222222.length;
                                        ILockSettings lockSettings4222222222222222222 = lockSettings2;
                                        i2 = 0;
                                        WindowManagerService wm2222222222222222222 = wm;
                                        while (i2 < length) {
                                        }
                                        traceEnd();
                                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                        traceEnd();
                                        String[] strArr222222222222222222 = classes222222222222222222;
                                        VibratorService vibratorService3222222222222222222 = vibrator;
                                        Resources.Theme theme222222222222222222 = systemTheme;
                                        WindowManager windowManager222222222222222222 = w222222222222222222;
                                        DisplayMetrics displayMetrics222222222222222222 = metrics222222222222222222;
                                        Configuration configuration222222222222222222 = config222222222222222222;
                                        boolean z2222222222222222222 = tuiEnable;
                                        LocationManagerService locationManagerService222222222222222222 = location;
                                        ILockSettings iLockSettings2222222222222222222 = lockSettings4222222222222222222;
                                        ActivityManagerService activityManagerService222222222222222222 = this.mActivityManagerService;
                                        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222222222222222222 = r122222222222222222;
                                        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222222222222222222 = new Runnable(this, context, wm2222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222222222222222222, enableIaware, safeMode) {
                                            private final /* synthetic */ SystemServer f$0;
                                            private final /* synthetic */ Context f$1;
                                            private final /* synthetic */ NetworkTimeUpdateService f$10;
                                            private final /* synthetic */ CommonTimeManagementService f$11;
                                            private final /* synthetic */ InputManagerService f$12;
                                            private final /* synthetic */ TelephonyRegistry f$13;
                                            private final /* synthetic */ MediaRouterService f$14;
                                            private final /* synthetic */ MmsServiceBroker f$15;
                                            private final /* synthetic */ boolean f$16;
                                            private final /* synthetic */ boolean f$17;
                                            private final /* synthetic */ WindowManagerService f$2;
                                            private final /* synthetic */ NetworkManagementService f$3;
                                            private final /* synthetic */ NetworkPolicyManagerService f$4;
                                            private final /* synthetic */ IpSecService f$5;
                                            private final /* synthetic */ NetworkStatsService f$6;
                                            private final /* synthetic */ ConnectivityService f$7;
                                            private final /* synthetic */ LocationManagerService f$8;
                                            private final /* synthetic */ CountryDetectorService f$9;

                                            {
                                                this.f$0 = r3;
                                                this.f$1 = r4;
                                                this.f$2 = r5;
                                                this.f$3 = r6;
                                                this.f$4 = r7;
                                                this.f$5 = r8;
                                                this.f$6 = r9;
                                                this.f$7 = r10;
                                                this.f$8 = r11;
                                                this.f$9 = r12;
                                                this.f$10 = r13;
                                                this.f$11 = r14;
                                                this.f$12 = r15;
                                                this.f$13 = r16;
                                                this.f$14 = r17;
                                                this.f$15 = r18;
                                                this.f$16 = r19;
                                                this.f$17 = r20;
                                            }

                                            public final void run() {
                                                SystemServer systemServer = this.f$0;
                                                Context context = this.f$1;
                                                WindowManagerService windowManagerService = this.f$2;
                                                NetworkManagementService networkManagementService = this.f$3;
                                                NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                                IpSecService ipSecService = this.f$5;
                                                NetworkStatsService networkStatsService = this.f$6;
                                                ConnectivityService connectivityService = this.f$7;
                                                LocationManagerService locationManagerService = this.f$8;
                                                CountryDetectorService countryDetectorService = this.f$9;
                                                NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                                CommonTimeManagementService commonTimeManagementService = this.f$11;
                                                InputManagerService inputManagerService = this.f$12;
                                                TelephonyRegistry telephonyRegistry = this.f$13;
                                                MediaRouterService mediaRouterService = this.f$14;
                                                MmsServiceBroker mmsServiceBroker = this.f$15;
                                                boolean z = this.f$16;
                                                boolean z2 = z;
                                                SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                            }
                                        };
                                        activityManagerService222222222222222222.systemReady(r0222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                                    }
                                } catch (Throwable th13) {
                                    e = th13;
                                    iBinder4 = null;
                                    location3 = location2;
                                    Slog.e(TAG, "Failure starting SerialService", e);
                                    iBinder4 = iBinder4;
                                    traceEnd();
                                    iBinder6 = iBinder4;
                                    traceBeginAndSlog("StartHardwarePropertiesManagerService");
                                    iBinder3 = new HardwarePropertiesManagerService(context);
                                    ServiceManager.addService("hardware_properties", iBinder3);
                                    iBinder2 = iBinder3;
                                    traceEnd();
                                    traceBeginAndSlog("StartTwilightService");
                                    this.mSystemServiceManager.startService(TwilightService.class);
                                    traceEnd();
                                    if (ColorDisplayController.isAvailable(context)) {
                                    }
                                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                                    traceBeginAndSlog("StartSoundTrigger");
                                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                    }
                                    traceBeginAndSlog("StartAppWidgerService");
                                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                    traceEnd();
                                    traceBeginAndSlog("StartVoiceRecognitionManager");
                                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                    traceEnd();
                                    if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                                    }
                                    traceBeginAndSlog("StartSensorNotification");
                                    this.mSystemServiceManager.startService(SensorNotificationService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartContextHubSystemService");
                                    this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                    traceEnd();
                                    HwServiceFactory.setupHwServices(context);
                                    traceBeginAndSlog("StartDiskStatsService");
                                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                                    traceEnd();
                                    startRulesManagerService = this.mOnlyCore && context.getResources().getBoolean(17956967);
                                    if (startRulesManagerService) {
                                    }
                                    Slog.i(TAG, "attestation Service");
                                    attestation = HwServiceFactory.getHwAttestationService();
                                    if (attestation == null) {
                                    }
                                    if (!isWatch) {
                                    }
                                    traceBeginAndSlog("StartCommonTimeManagementService");
                                    commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                    ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                    traceEnd();
                                    traceBeginAndSlog("CertBlacklister");
                                    new CertBlacklister(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartEmergencyAffordanceService");
                                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartDreamManager");
                                    this.mSystemServiceManager.startService(DreamManagerService.class);
                                    traceEnd();
                                    traceBeginAndSlog("AddGraphicsStatsService");
                                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                    traceEnd();
                                    if (CoverageService.ENABLED) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                    }
                                    traceBeginAndSlog("StartRestrictionManager");
                                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartMediaSessionService");
                                    this.mSystemServiceManager.startService(MediaSessionService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartMediaUpdateService");
                                    this.mSystemServiceManager.startService(MediaUpdateService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                    }
                                    traceBeginAndSlog("StartTvInputManager");
                                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                    }
                                    traceBeginAndSlog("StartMediaRouterService");
                                    mediaRouterService = new MediaRouterService(context);
                                    ServiceManager.addService("media_router", mediaRouterService);
                                    commonTimeMgmtService = commonTimeMgmtService2;
                                    mediaRouter2 = mediaRouterService;
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                    }
                                    traceBeginAndSlog("StartBackgroundDexOptService");
                                    BackgroundDexOptService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartPruneInstantAppsJobService");
                                    PruneInstantAppsJobService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartLauncherAppsService");
                                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartCrossProfileAppsService");
                                    this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                    traceEnd();
                                    connectivity = connectivity2;
                                    countryDetector = countryDetector2;
                                    iNetworkManagementService = iNetworkManagementService2;
                                    iNetworkStatsService = iNetworkStatsService2;
                                    IBinder iBinder1122222222 = iBinder1022;
                                    IBinder iBinder1222222222 = iBinder6;
                                    networkTimeUpdater = networkTimeUpdater3;
                                    IBinder iBinder1322222222 = iBinder2;
                                    lockSettings2 = lockSettings322222222222;
                                    INotificationManager iNotificationManager22222222 = notification222;
                                    location = location3;
                                    ipSecServiceF = iBinder5;
                                    lockSettings = iLockSettings;
                                    if (!isWatch) {
                                    }
                                    MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                    if (isWatch) {
                                    }
                                    if (!disableSlices) {
                                    }
                                    if (!disableCameraService) {
                                    }
                                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                    }
                                    traceBeginAndSlog("StartStatsCompanionService");
                                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                    traceEnd();
                                    safeMode = wm.detectSafeMode();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    if (safeMode) {
                                    }
                                    traceBeginAndSlog("StartMmsService");
                                    MmsServiceBroker mmsService2222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                    }
                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                    }
                                    if (isStartSysSvcCallRecord) {
                                    }
                                    traceBeginAndSlog("MakeVibratorServiceReady");
                                    vibrator.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                                    if (lockSettings2 != null) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                    this.mSystemServiceManager.startBootPhase(480);
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                    this.mSystemServiceManager.startBootPhase(500);
                                    traceEnd();
                                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                                    wm.systemReady();
                                    traceEnd();
                                    if (safeMode) {
                                    }
                                    Configuration config2222222222222222222 = wm.computeNewConfiguration(0);
                                    DisplayMetrics metrics2222222222222222222 = new DisplayMetrics();
                                    WindowManager w2222222222222222222 = (WindowManager) context.getSystemService("window");
                                    w2222222222222222222.getDefaultDisplay().getMetrics(metrics2222222222222222222);
                                    context.getResources().updateConfiguration(config2222222222222222222, metrics2222222222222222222);
                                    systemTheme = context.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                    }
                                    traceBeginAndSlog("MakePowerManagerServiceReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    traceEnd();
                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                    traceBeginAndSlog("MakePackageManagerServiceReady");
                                    this.mPackageManagerService.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    traceEnd();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    traceBeginAndSlog("StartDeviceSpecificServices");
                                    String[] classes2222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                    length = classes2222222222222222222.length;
                                    ILockSettings lockSettings42222222222222222222 = lockSettings2;
                                    i2 = 0;
                                    WindowManagerService wm22222222222222222222 = wm;
                                    while (i2 < length) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                    traceEnd();
                                    String[] strArr2222222222222222222 = classes2222222222222222222;
                                    VibratorService vibratorService32222222222222222222 = vibrator;
                                    Resources.Theme theme2222222222222222222 = systemTheme;
                                    WindowManager windowManager2222222222222222222 = w2222222222222222222;
                                    DisplayMetrics displayMetrics2222222222222222222 = metrics2222222222222222222;
                                    Configuration configuration2222222222222222222 = config2222222222222222222;
                                    boolean z22222222222222222222 = tuiEnable;
                                    LocationManagerService locationManagerService2222222222222222222 = location;
                                    ILockSettings iLockSettings22222222222222222222 = lockSettings42222222222222222222;
                                    ActivityManagerService activityManagerService2222222222222222222 = this.mActivityManagerService;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222222222222222222 = r1222222222222222222;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222222222222222222 = new Runnable(this, context, wm22222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222222222222222222, enableIaware, safeMode) {
                                        private final /* synthetic */ SystemServer f$0;
                                        private final /* synthetic */ Context f$1;
                                        private final /* synthetic */ NetworkTimeUpdateService f$10;
                                        private final /* synthetic */ CommonTimeManagementService f$11;
                                        private final /* synthetic */ InputManagerService f$12;
                                        private final /* synthetic */ TelephonyRegistry f$13;
                                        private final /* synthetic */ MediaRouterService f$14;
                                        private final /* synthetic */ MmsServiceBroker f$15;
                                        private final /* synthetic */ boolean f$16;
                                        private final /* synthetic */ boolean f$17;
                                        private final /* synthetic */ WindowManagerService f$2;
                                        private final /* synthetic */ NetworkManagementService f$3;
                                        private final /* synthetic */ NetworkPolicyManagerService f$4;
                                        private final /* synthetic */ IpSecService f$5;
                                        private final /* synthetic */ NetworkStatsService f$6;
                                        private final /* synthetic */ ConnectivityService f$7;
                                        private final /* synthetic */ LocationManagerService f$8;
                                        private final /* synthetic */ CountryDetectorService f$9;

                                        {
                                            this.f$0 = r3;
                                            this.f$1 = r4;
                                            this.f$2 = r5;
                                            this.f$3 = r6;
                                            this.f$4 = r7;
                                            this.f$5 = r8;
                                            this.f$6 = r9;
                                            this.f$7 = r10;
                                            this.f$8 = r11;
                                            this.f$9 = r12;
                                            this.f$10 = r13;
                                            this.f$11 = r14;
                                            this.f$12 = r15;
                                            this.f$13 = r16;
                                            this.f$14 = r17;
                                            this.f$15 = r18;
                                            this.f$16 = r19;
                                            this.f$17 = r20;
                                        }

                                        public final void run() {
                                            SystemServer systemServer = this.f$0;
                                            Context context = this.f$1;
                                            WindowManagerService windowManagerService = this.f$2;
                                            NetworkManagementService networkManagementService = this.f$3;
                                            NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                            IpSecService ipSecService = this.f$5;
                                            NetworkStatsService networkStatsService = this.f$6;
                                            ConnectivityService connectivityService = this.f$7;
                                            LocationManagerService locationManagerService = this.f$8;
                                            CountryDetectorService countryDetectorService = this.f$9;
                                            NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                            CommonTimeManagementService commonTimeManagementService = this.f$11;
                                            InputManagerService inputManagerService = this.f$12;
                                            TelephonyRegistry telephonyRegistry = this.f$13;
                                            MediaRouterService mediaRouterService = this.f$14;
                                            MmsServiceBroker mmsServiceBroker = this.f$15;
                                            boolean z = this.f$16;
                                            boolean z2 = z;
                                            SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                        }
                                    };
                                    activityManagerService2222222222222222222.systemReady(r02222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                                }
                                traceEnd();
                                iBinder6 = iBinder4;
                            } else {
                                location3 = location2;
                            }
                            traceBeginAndSlog("StartHardwarePropertiesManagerService");
                            try {
                                iBinder3 = new HardwarePropertiesManagerService(context);
                                ServiceManager.addService("hardware_properties", iBinder3);
                                iBinder2 = iBinder3;
                            } catch (Throwable th14) {
                                e = th14;
                                iBinder3 = null;
                                Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e);
                                iBinder2 = iBinder3;
                                traceEnd();
                                traceBeginAndSlog("StartTwilightService");
                                this.mSystemServiceManager.startService(TwilightService.class);
                                traceEnd();
                                if (ColorDisplayController.isAvailable(context)) {
                                }
                                this.mSystemServiceManager.startService(JobSchedulerService.class);
                                traceBeginAndSlog("StartSoundTrigger");
                                this.mSystemServiceManager.startService(SoundTriggerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                }
                                traceBeginAndSlog("StartAppWidgerService");
                                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                traceEnd();
                                traceBeginAndSlog("StartVoiceRecognitionManager");
                                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                                traceEnd();
                                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                                }
                                traceBeginAndSlog("StartSensorNotification");
                                this.mSystemServiceManager.startService(SensorNotificationService.class);
                                traceEnd();
                                traceBeginAndSlog("StartContextHubSystemService");
                                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                                traceEnd();
                                HwServiceFactory.setupHwServices(context);
                                traceBeginAndSlog("StartDiskStatsService");
                                ServiceManager.addService("diskstats", new DiskStatsService(context));
                                traceEnd();
                                startRulesManagerService = this.mOnlyCore && context.getResources().getBoolean(17956967);
                                if (startRulesManagerService) {
                                }
                                Slog.i(TAG, "attestation Service");
                                attestation = HwServiceFactory.getHwAttestationService();
                                if (attestation == null) {
                                }
                                if (!isWatch) {
                                }
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                traceEnd();
                                traceBeginAndSlog("CertBlacklister");
                                new CertBlacklister(context);
                                traceEnd();
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                traceBeginAndSlog("StartDreamManager");
                                this.mSystemServiceManager.startService(DreamManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("AddGraphicsStatsService");
                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                traceEnd();
                                if (CoverageService.ENABLED) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                }
                                traceBeginAndSlog("StartRestrictionManager");
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaSessionService");
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaUpdateService");
                                this.mSystemServiceManager.startService(MediaUpdateService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                traceBeginAndSlog("StartTvInputManager");
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                traceBeginAndSlog("StartMediaRouterService");
                                mediaRouterService = new MediaRouterService(context);
                                ServiceManager.addService("media_router", mediaRouterService);
                                commonTimeMgmtService = commonTimeMgmtService2;
                                mediaRouter2 = mediaRouterService;
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                }
                                traceBeginAndSlog("StartBackgroundDexOptService");
                                BackgroundDexOptService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartPruneInstantAppsJobService");
                                PruneInstantAppsJobService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                traceBeginAndSlog("StartCrossProfileAppsService");
                                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                traceEnd();
                                connectivity = connectivity2;
                                countryDetector = countryDetector2;
                                iNetworkManagementService = iNetworkManagementService2;
                                iNetworkStatsService = iNetworkStatsService2;
                                IBinder iBinder11222222222 = iBinder1022;
                                IBinder iBinder12222222222 = iBinder6;
                                networkTimeUpdater = networkTimeUpdater3;
                                IBinder iBinder13222222222 = iBinder2;
                                lockSettings2 = lockSettings322222222222;
                                INotificationManager iNotificationManager222222222 = notification222;
                                location = location3;
                                ipSecServiceF = iBinder5;
                                lockSettings = iLockSettings;
                                if (!isWatch) {
                                }
                                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                if (isWatch) {
                                }
                                if (!disableSlices) {
                                }
                                if (!disableCameraService) {
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                }
                                traceBeginAndSlog("StartStatsCompanionService");
                                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                traceEnd();
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                MmsServiceBroker mmsService22222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                if (isStartSysSvcCallRecord) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings2 != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                this.mSystemServiceManager.startBootPhase(480);
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                this.mSystemServiceManager.startBootPhase(500);
                                traceEnd();
                                traceBeginAndSlog("MakeWindowManagerServiceReady");
                                wm.systemReady();
                                traceEnd();
                                if (safeMode) {
                                }
                                Configuration config22222222222222222222 = wm.computeNewConfiguration(0);
                                DisplayMetrics metrics22222222222222222222 = new DisplayMetrics();
                                WindowManager w22222222222222222222 = (WindowManager) context.getSystemService("window");
                                w22222222222222222222.getDefaultDisplay().getMetrics(metrics22222222222222222222);
                                context.getResources().updateConfiguration(config22222222222222222222, metrics22222222222222222222);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                traceBeginAndSlog("StartDeviceSpecificServices");
                                String[] classes22222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                length = classes22222222222222222222.length;
                                ILockSettings lockSettings422222222222222222222 = lockSettings2;
                                i2 = 0;
                                WindowManagerService wm222222222222222222222 = wm;
                                while (i2 < length) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                traceEnd();
                                String[] strArr22222222222222222222 = classes22222222222222222222;
                                VibratorService vibratorService322222222222222222222 = vibrator;
                                Resources.Theme theme22222222222222222222 = systemTheme;
                                WindowManager windowManager22222222222222222222 = w22222222222222222222;
                                DisplayMetrics displayMetrics22222222222222222222 = metrics22222222222222222222;
                                Configuration configuration22222222222222222222 = config22222222222222222222;
                                boolean z222222222222222222222 = tuiEnable;
                                LocationManagerService locationManagerService22222222222222222222 = location;
                                ILockSettings iLockSettings222222222222222222222 = lockSettings422222222222222222222;
                                ActivityManagerService activityManagerService22222222222222222222 = this.mActivityManagerService;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022222222222222222222 = r12222222222222222222;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122222222222222222222 = new Runnable(this, context, wm222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22222222222222222222, enableIaware, safeMode) {
                                    private final /* synthetic */ SystemServer f$0;
                                    private final /* synthetic */ Context f$1;
                                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                                    private final /* synthetic */ CommonTimeManagementService f$11;
                                    private final /* synthetic */ InputManagerService f$12;
                                    private final /* synthetic */ TelephonyRegistry f$13;
                                    private final /* synthetic */ MediaRouterService f$14;
                                    private final /* synthetic */ MmsServiceBroker f$15;
                                    private final /* synthetic */ boolean f$16;
                                    private final /* synthetic */ boolean f$17;
                                    private final /* synthetic */ WindowManagerService f$2;
                                    private final /* synthetic */ NetworkManagementService f$3;
                                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                                    private final /* synthetic */ IpSecService f$5;
                                    private final /* synthetic */ NetworkStatsService f$6;
                                    private final /* synthetic */ ConnectivityService f$7;
                                    private final /* synthetic */ LocationManagerService f$8;
                                    private final /* synthetic */ CountryDetectorService f$9;

                                    {
                                        this.f$0 = r3;
                                        this.f$1 = r4;
                                        this.f$2 = r5;
                                        this.f$3 = r6;
                                        this.f$4 = r7;
                                        this.f$5 = r8;
                                        this.f$6 = r9;
                                        this.f$7 = r10;
                                        this.f$8 = r11;
                                        this.f$9 = r12;
                                        this.f$10 = r13;
                                        this.f$11 = r14;
                                        this.f$12 = r15;
                                        this.f$13 = r16;
                                        this.f$14 = r17;
                                        this.f$15 = r18;
                                        this.f$16 = r19;
                                        this.f$17 = r20;
                                    }

                                    public final void run() {
                                        SystemServer systemServer = this.f$0;
                                        Context context = this.f$1;
                                        WindowManagerService windowManagerService = this.f$2;
                                        NetworkManagementService networkManagementService = this.f$3;
                                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                        IpSecService ipSecService = this.f$5;
                                        NetworkStatsService networkStatsService = this.f$6;
                                        ConnectivityService connectivityService = this.f$7;
                                        LocationManagerService locationManagerService = this.f$8;
                                        CountryDetectorService countryDetectorService = this.f$9;
                                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                                        InputManagerService inputManagerService = this.f$12;
                                        TelephonyRegistry telephonyRegistry = this.f$13;
                                        MediaRouterService mediaRouterService = this.f$14;
                                        MmsServiceBroker mmsServiceBroker = this.f$15;
                                        boolean z = this.f$16;
                                        boolean z2 = z;
                                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                    }
                                };
                                activityManagerService22222222222222222222.systemReady(r022222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartTwilightService");
                            this.mSystemServiceManager.startService(TwilightService.class);
                            traceEnd();
                            if (ColorDisplayController.isAvailable(context)) {
                                traceBeginAndSlog("StartNightDisplay");
                                this.mSystemServiceManager.startService(ColorDisplayService.class);
                                traceEnd();
                            }
                            this.mSystemServiceManager.startService(JobSchedulerService.class);
                            traceBeginAndSlog("StartSoundTrigger");
                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                                traceBeginAndSlog("StartBackupManager");
                                this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                                traceEnd();
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.app_widgets") || context.getResources().getBoolean(17956948)) {
                                traceBeginAndSlog("StartAppWidgerService");
                                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                                traceEnd();
                            }
                            traceBeginAndSlog("StartVoiceRecognitionManager");
                            this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                            traceEnd();
                            if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                                traceBeginAndSlog("StartGestureLauncher");
                                this.mSystemServiceManager.startService(GestureLauncherService.class);
                                traceEnd();
                            }
                            traceBeginAndSlog("StartSensorNotification");
                            this.mSystemServiceManager.startService(SensorNotificationService.class);
                            traceEnd();
                            traceBeginAndSlog("StartContextHubSystemService");
                            this.mSystemServiceManager.startService(ContextHubSystemService.class);
                            traceEnd();
                            HwServiceFactory.setupHwServices(context);
                            traceBeginAndSlog("StartDiskStatsService");
                            try {
                                ServiceManager.addService("diskstats", new DiskStatsService(context));
                            } catch (Throwable e34) {
                                reportWtf("starting DiskStats Service", e34);
                            }
                            traceEnd();
                            startRulesManagerService = this.mOnlyCore && context.getResources().getBoolean(17956967);
                            if (startRulesManagerService) {
                                traceBeginAndSlog("StartTimeZoneRulesManagerService");
                                this.mSystemServiceManager.startService(TIME_ZONE_RULES_MANAGER_SERVICE_CLASS);
                                traceEnd();
                            }
                            try {
                                Slog.i(TAG, "attestation Service");
                                attestation = HwServiceFactory.getHwAttestationService();
                                if (attestation == null) {
                                    boolean z5 = startRulesManagerService;
                                    try {
                                        ServiceManager.addService("attestation_service", attestation.getInstance(context));
                                    } catch (Throwable th15) {
                                        e = th15;
                                    }
                                }
                            } catch (Throwable th16) {
                                e = th16;
                                boolean z6 = startRulesManagerService;
                                Slog.i(TAG, "attestation_service failed");
                                reportWtf("attestation Service", e);
                                if (!isWatch) {
                                }
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                traceEnd();
                                traceBeginAndSlog("CertBlacklister");
                                new CertBlacklister(context);
                                traceEnd();
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                traceBeginAndSlog("StartDreamManager");
                                this.mSystemServiceManager.startService(DreamManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("AddGraphicsStatsService");
                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                traceEnd();
                                if (CoverageService.ENABLED) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                }
                                traceBeginAndSlog("StartRestrictionManager");
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaSessionService");
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaUpdateService");
                                this.mSystemServiceManager.startService(MediaUpdateService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                traceBeginAndSlog("StartTvInputManager");
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                traceBeginAndSlog("StartMediaRouterService");
                                mediaRouterService = new MediaRouterService(context);
                                ServiceManager.addService("media_router", mediaRouterService);
                                commonTimeMgmtService = commonTimeMgmtService2;
                                mediaRouter2 = mediaRouterService;
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                }
                                traceBeginAndSlog("StartBackgroundDexOptService");
                                BackgroundDexOptService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartPruneInstantAppsJobService");
                                PruneInstantAppsJobService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                traceBeginAndSlog("StartCrossProfileAppsService");
                                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                traceEnd();
                                connectivity = connectivity2;
                                countryDetector = countryDetector2;
                                iNetworkManagementService = iNetworkManagementService2;
                                iNetworkStatsService = iNetworkStatsService2;
                                IBinder iBinder112222222222 = iBinder1022;
                                IBinder iBinder122222222222 = iBinder6;
                                networkTimeUpdater = networkTimeUpdater3;
                                IBinder iBinder132222222222 = iBinder2;
                                lockSettings2 = lockSettings322222222222;
                                INotificationManager iNotificationManager2222222222 = notification222;
                                location = location3;
                                ipSecServiceF = iBinder5;
                                lockSettings = iLockSettings;
                                if (!isWatch) {
                                }
                                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                if (isWatch) {
                                }
                                if (!disableSlices) {
                                }
                                if (!disableCameraService) {
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                }
                                traceBeginAndSlog("StartStatsCompanionService");
                                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                traceEnd();
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                MmsServiceBroker mmsService222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                if (isStartSysSvcCallRecord) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings2 != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                this.mSystemServiceManager.startBootPhase(480);
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                this.mSystemServiceManager.startBootPhase(500);
                                traceEnd();
                                traceBeginAndSlog("MakeWindowManagerServiceReady");
                                wm.systemReady();
                                traceEnd();
                                if (safeMode) {
                                }
                                Configuration config222222222222222222222 = wm.computeNewConfiguration(0);
                                DisplayMetrics metrics222222222222222222222 = new DisplayMetrics();
                                WindowManager w222222222222222222222 = (WindowManager) context.getSystemService("window");
                                w222222222222222222222.getDefaultDisplay().getMetrics(metrics222222222222222222222);
                                context.getResources().updateConfiguration(config222222222222222222222, metrics222222222222222222222);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                traceBeginAndSlog("StartDeviceSpecificServices");
                                String[] classes222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                length = classes222222222222222222222.length;
                                ILockSettings lockSettings4222222222222222222222 = lockSettings2;
                                i2 = 0;
                                WindowManagerService wm2222222222222222222222 = wm;
                                while (i2 < length) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                traceEnd();
                                String[] strArr222222222222222222222 = classes222222222222222222222;
                                VibratorService vibratorService3222222222222222222222 = vibrator;
                                Resources.Theme theme222222222222222222222 = systemTheme;
                                WindowManager windowManager222222222222222222222 = w222222222222222222222;
                                DisplayMetrics displayMetrics222222222222222222222 = metrics222222222222222222222;
                                Configuration configuration222222222222222222222 = config222222222222222222222;
                                boolean z2222222222222222222222 = tuiEnable;
                                LocationManagerService locationManagerService222222222222222222222 = location;
                                ILockSettings iLockSettings2222222222222222222222 = lockSettings4222222222222222222222;
                                ActivityManagerService activityManagerService222222222222222222222 = this.mActivityManagerService;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222222222222222222222 = r122222222222222222222;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222222222222222222222 = new Runnable(this, context, wm2222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222222222222222222222, enableIaware, safeMode) {
                                    private final /* synthetic */ SystemServer f$0;
                                    private final /* synthetic */ Context f$1;
                                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                                    private final /* synthetic */ CommonTimeManagementService f$11;
                                    private final /* synthetic */ InputManagerService f$12;
                                    private final /* synthetic */ TelephonyRegistry f$13;
                                    private final /* synthetic */ MediaRouterService f$14;
                                    private final /* synthetic */ MmsServiceBroker f$15;
                                    private final /* synthetic */ boolean f$16;
                                    private final /* synthetic */ boolean f$17;
                                    private final /* synthetic */ WindowManagerService f$2;
                                    private final /* synthetic */ NetworkManagementService f$3;
                                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                                    private final /* synthetic */ IpSecService f$5;
                                    private final /* synthetic */ NetworkStatsService f$6;
                                    private final /* synthetic */ ConnectivityService f$7;
                                    private final /* synthetic */ LocationManagerService f$8;
                                    private final /* synthetic */ CountryDetectorService f$9;

                                    {
                                        this.f$0 = r3;
                                        this.f$1 = r4;
                                        this.f$2 = r5;
                                        this.f$3 = r6;
                                        this.f$4 = r7;
                                        this.f$5 = r8;
                                        this.f$6 = r9;
                                        this.f$7 = r10;
                                        this.f$8 = r11;
                                        this.f$9 = r12;
                                        this.f$10 = r13;
                                        this.f$11 = r14;
                                        this.f$12 = r15;
                                        this.f$13 = r16;
                                        this.f$14 = r17;
                                        this.f$15 = r18;
                                        this.f$16 = r19;
                                        this.f$17 = r20;
                                    }

                                    public final void run() {
                                        SystemServer systemServer = this.f$0;
                                        Context context = this.f$1;
                                        WindowManagerService windowManagerService = this.f$2;
                                        NetworkManagementService networkManagementService = this.f$3;
                                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                        IpSecService ipSecService = this.f$5;
                                        NetworkStatsService networkStatsService = this.f$6;
                                        ConnectivityService connectivityService = this.f$7;
                                        LocationManagerService locationManagerService = this.f$8;
                                        CountryDetectorService countryDetectorService = this.f$9;
                                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                                        InputManagerService inputManagerService = this.f$12;
                                        TelephonyRegistry telephonyRegistry = this.f$13;
                                        MediaRouterService mediaRouterService = this.f$14;
                                        MmsServiceBroker mmsServiceBroker = this.f$15;
                                        boolean z = this.f$16;
                                        boolean z2 = z;
                                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                    }
                                };
                                activityManagerService222222222222222222222.systemReady(r0222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                            }
                            if (!isWatch) {
                                traceBeginAndSlog("StartNetworkTimeUpdateService");
                                try {
                                    networkTimeUpdater2 = new NetworkTimeUpdateService(context);
                                    try {
                                        ServiceManager.addService("network_time_update_service", networkTimeUpdater2);
                                    } catch (Throwable th17) {
                                        e = th17;
                                    }
                                } catch (Throwable th18) {
                                    e = th18;
                                    networkTimeUpdater2 = null;
                                    reportWtf("starting NetworkTimeUpdate service", e);
                                    networkTimeUpdater3 = networkTimeUpdater2;
                                    traceEnd();
                                    traceBeginAndSlog("StartCommonTimeManagementService");
                                    commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                    ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                                    traceEnd();
                                    traceBeginAndSlog("CertBlacklister");
                                    new CertBlacklister(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartEmergencyAffordanceService");
                                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartDreamManager");
                                    this.mSystemServiceManager.startService(DreamManagerService.class);
                                    traceEnd();
                                    traceBeginAndSlog("AddGraphicsStatsService");
                                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                    traceEnd();
                                    if (CoverageService.ENABLED) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                    }
                                    traceBeginAndSlog("StartRestrictionManager");
                                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartMediaSessionService");
                                    this.mSystemServiceManager.startService(MediaSessionService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartMediaUpdateService");
                                    this.mSystemServiceManager.startService(MediaUpdateService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                    }
                                    traceBeginAndSlog("StartTvInputManager");
                                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                    }
                                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                    }
                                    traceBeginAndSlog("StartMediaRouterService");
                                    mediaRouterService = new MediaRouterService(context);
                                    ServiceManager.addService("media_router", mediaRouterService);
                                    commonTimeMgmtService = commonTimeMgmtService2;
                                    mediaRouter2 = mediaRouterService;
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                    }
                                    traceBeginAndSlog("StartBackgroundDexOptService");
                                    BackgroundDexOptService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartPruneInstantAppsJobService");
                                    PruneInstantAppsJobService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartLauncherAppsService");
                                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartCrossProfileAppsService");
                                    this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                    traceEnd();
                                    connectivity = connectivity2;
                                    countryDetector = countryDetector2;
                                    iNetworkManagementService = iNetworkManagementService2;
                                    iNetworkStatsService = iNetworkStatsService2;
                                    IBinder iBinder1122222222222 = iBinder1022;
                                    IBinder iBinder1222222222222 = iBinder6;
                                    networkTimeUpdater = networkTimeUpdater3;
                                    IBinder iBinder1322222222222 = iBinder2;
                                    lockSettings2 = lockSettings322222222222;
                                    INotificationManager iNotificationManager22222222222 = notification222;
                                    location = location3;
                                    ipSecServiceF = iBinder5;
                                    lockSettings = iLockSettings;
                                    if (!isWatch) {
                                    }
                                    MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                    if (isWatch) {
                                    }
                                    if (!disableSlices) {
                                    }
                                    if (!disableCameraService) {
                                    }
                                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                    }
                                    traceBeginAndSlog("StartStatsCompanionService");
                                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                    traceEnd();
                                    safeMode = wm.detectSafeMode();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    if (safeMode) {
                                    }
                                    traceBeginAndSlog("StartMmsService");
                                    MmsServiceBroker mmsService2222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                    }
                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                    }
                                    if (isStartSysSvcCallRecord) {
                                    }
                                    traceBeginAndSlog("MakeVibratorServiceReady");
                                    vibrator.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                                    if (lockSettings2 != null) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                    this.mSystemServiceManager.startBootPhase(480);
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                    this.mSystemServiceManager.startBootPhase(500);
                                    traceEnd();
                                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                                    wm.systemReady();
                                    traceEnd();
                                    if (safeMode) {
                                    }
                                    Configuration config2222222222222222222222 = wm.computeNewConfiguration(0);
                                    DisplayMetrics metrics2222222222222222222222 = new DisplayMetrics();
                                    WindowManager w2222222222222222222222 = (WindowManager) context.getSystemService("window");
                                    w2222222222222222222222.getDefaultDisplay().getMetrics(metrics2222222222222222222222);
                                    context.getResources().updateConfiguration(config2222222222222222222222, metrics2222222222222222222222);
                                    systemTheme = context.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                    }
                                    traceBeginAndSlog("MakePowerManagerServiceReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    traceEnd();
                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                    traceBeginAndSlog("MakePackageManagerServiceReady");
                                    this.mPackageManagerService.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    traceEnd();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    traceBeginAndSlog("StartDeviceSpecificServices");
                                    String[] classes2222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                    length = classes2222222222222222222222.length;
                                    ILockSettings lockSettings42222222222222222222222 = lockSettings2;
                                    i2 = 0;
                                    WindowManagerService wm22222222222222222222222 = wm;
                                    while (i2 < length) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                    traceEnd();
                                    String[] strArr2222222222222222222222 = classes2222222222222222222222;
                                    VibratorService vibratorService32222222222222222222222 = vibrator;
                                    Resources.Theme theme2222222222222222222222 = systemTheme;
                                    WindowManager windowManager2222222222222222222222 = w2222222222222222222222;
                                    DisplayMetrics displayMetrics2222222222222222222222 = metrics2222222222222222222222;
                                    Configuration configuration2222222222222222222222 = config2222222222222222222222;
                                    boolean z22222222222222222222222 = tuiEnable;
                                    LocationManagerService locationManagerService2222222222222222222222 = location;
                                    ILockSettings iLockSettings22222222222222222222222 = lockSettings42222222222222222222222;
                                    ActivityManagerService activityManagerService2222222222222222222222 = this.mActivityManagerService;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222222222222222222222 = r1222222222222222222222;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222222222222222222222 = new Runnable(this, context, wm22222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222222222222222222222, enableIaware, safeMode) {
                                        private final /* synthetic */ SystemServer f$0;
                                        private final /* synthetic */ Context f$1;
                                        private final /* synthetic */ NetworkTimeUpdateService f$10;
                                        private final /* synthetic */ CommonTimeManagementService f$11;
                                        private final /* synthetic */ InputManagerService f$12;
                                        private final /* synthetic */ TelephonyRegistry f$13;
                                        private final /* synthetic */ MediaRouterService f$14;
                                        private final /* synthetic */ MmsServiceBroker f$15;
                                        private final /* synthetic */ boolean f$16;
                                        private final /* synthetic */ boolean f$17;
                                        private final /* synthetic */ WindowManagerService f$2;
                                        private final /* synthetic */ NetworkManagementService f$3;
                                        private final /* synthetic */ NetworkPolicyManagerService f$4;
                                        private final /* synthetic */ IpSecService f$5;
                                        private final /* synthetic */ NetworkStatsService f$6;
                                        private final /* synthetic */ ConnectivityService f$7;
                                        private final /* synthetic */ LocationManagerService f$8;
                                        private final /* synthetic */ CountryDetectorService f$9;

                                        {
                                            this.f$0 = r3;
                                            this.f$1 = r4;
                                            this.f$2 = r5;
                                            this.f$3 = r6;
                                            this.f$4 = r7;
                                            this.f$5 = r8;
                                            this.f$6 = r9;
                                            this.f$7 = r10;
                                            this.f$8 = r11;
                                            this.f$9 = r12;
                                            this.f$10 = r13;
                                            this.f$11 = r14;
                                            this.f$12 = r15;
                                            this.f$13 = r16;
                                            this.f$14 = r17;
                                            this.f$15 = r18;
                                            this.f$16 = r19;
                                            this.f$17 = r20;
                                        }

                                        public final void run() {
                                            SystemServer systemServer = this.f$0;
                                            Context context = this.f$1;
                                            WindowManagerService windowManagerService = this.f$2;
                                            NetworkManagementService networkManagementService = this.f$3;
                                            NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                            IpSecService ipSecService = this.f$5;
                                            NetworkStatsService networkStatsService = this.f$6;
                                            ConnectivityService connectivityService = this.f$7;
                                            LocationManagerService locationManagerService = this.f$8;
                                            CountryDetectorService countryDetectorService = this.f$9;
                                            NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                            CommonTimeManagementService commonTimeManagementService = this.f$11;
                                            InputManagerService inputManagerService = this.f$12;
                                            TelephonyRegistry telephonyRegistry = this.f$13;
                                            MediaRouterService mediaRouterService = this.f$14;
                                            MmsServiceBroker mmsServiceBroker = this.f$15;
                                            boolean z = this.f$16;
                                            boolean z2 = z;
                                            SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                        }
                                    };
                                    activityManagerService2222222222222222222222.systemReady(r02222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                                }
                                networkTimeUpdater3 = networkTimeUpdater2;
                                traceEnd();
                            }
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            try {
                                commonTimeMgmtService2 = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeMgmtService2);
                            } catch (Throwable th19) {
                                e = th19;
                                commonTimeMgmtService2 = null;
                                reportWtf("starting CommonTimeManagementService service", e);
                                traceEnd();
                                traceBeginAndSlog("CertBlacklister");
                                new CertBlacklister(context);
                                traceEnd();
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                traceBeginAndSlog("StartDreamManager");
                                this.mSystemServiceManager.startService(DreamManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("AddGraphicsStatsService");
                                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                                traceEnd();
                                if (CoverageService.ENABLED) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                }
                                traceBeginAndSlog("StartRestrictionManager");
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaSessionService");
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaUpdateService");
                                this.mSystemServiceManager.startService(MediaUpdateService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                traceBeginAndSlog("StartTvInputManager");
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                traceBeginAndSlog("StartMediaRouterService");
                                mediaRouterService = new MediaRouterService(context);
                                ServiceManager.addService("media_router", mediaRouterService);
                                commonTimeMgmtService = commonTimeMgmtService2;
                                mediaRouter2 = mediaRouterService;
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                }
                                traceBeginAndSlog("StartBackgroundDexOptService");
                                BackgroundDexOptService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartPruneInstantAppsJobService");
                                PruneInstantAppsJobService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                traceBeginAndSlog("StartCrossProfileAppsService");
                                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                traceEnd();
                                connectivity = connectivity2;
                                countryDetector = countryDetector2;
                                iNetworkManagementService = iNetworkManagementService2;
                                iNetworkStatsService = iNetworkStatsService2;
                                IBinder iBinder11222222222222 = iBinder1022;
                                IBinder iBinder12222222222222 = iBinder6;
                                networkTimeUpdater = networkTimeUpdater3;
                                IBinder iBinder13222222222222 = iBinder2;
                                lockSettings2 = lockSettings322222222222;
                                INotificationManager iNotificationManager222222222222 = notification222;
                                location = location3;
                                ipSecServiceF = iBinder5;
                                lockSettings = iLockSettings;
                                if (!isWatch) {
                                }
                                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                if (isWatch) {
                                }
                                if (!disableSlices) {
                                }
                                if (!disableCameraService) {
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                }
                                traceBeginAndSlog("StartStatsCompanionService");
                                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                traceEnd();
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                MmsServiceBroker mmsService22222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                if (isStartSysSvcCallRecord) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings2 != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                this.mSystemServiceManager.startBootPhase(480);
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                this.mSystemServiceManager.startBootPhase(500);
                                traceEnd();
                                traceBeginAndSlog("MakeWindowManagerServiceReady");
                                wm.systemReady();
                                traceEnd();
                                if (safeMode) {
                                }
                                Configuration config22222222222222222222222 = wm.computeNewConfiguration(0);
                                DisplayMetrics metrics22222222222222222222222 = new DisplayMetrics();
                                WindowManager w22222222222222222222222 = (WindowManager) context.getSystemService("window");
                                w22222222222222222222222.getDefaultDisplay().getMetrics(metrics22222222222222222222222);
                                context.getResources().updateConfiguration(config22222222222222222222222, metrics22222222222222222222222);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                traceBeginAndSlog("StartDeviceSpecificServices");
                                String[] classes22222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                length = classes22222222222222222222222.length;
                                ILockSettings lockSettings422222222222222222222222 = lockSettings2;
                                i2 = 0;
                                WindowManagerService wm222222222222222222222222 = wm;
                                while (i2 < length) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                traceEnd();
                                String[] strArr22222222222222222222222 = classes22222222222222222222222;
                                VibratorService vibratorService322222222222222222222222 = vibrator;
                                Resources.Theme theme22222222222222222222222 = systemTheme;
                                WindowManager windowManager22222222222222222222222 = w22222222222222222222222;
                                DisplayMetrics displayMetrics22222222222222222222222 = metrics22222222222222222222222;
                                Configuration configuration22222222222222222222222 = config22222222222222222222222;
                                boolean z222222222222222222222222 = tuiEnable;
                                LocationManagerService locationManagerService22222222222222222222222 = location;
                                ILockSettings iLockSettings222222222222222222222222 = lockSettings422222222222222222222222;
                                ActivityManagerService activityManagerService22222222222222222222222 = this.mActivityManagerService;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022222222222222222222222 = r12222222222222222222222;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122222222222222222222222 = new Runnable(this, context, wm222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22222222222222222222222, enableIaware, safeMode) {
                                    private final /* synthetic */ SystemServer f$0;
                                    private final /* synthetic */ Context f$1;
                                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                                    private final /* synthetic */ CommonTimeManagementService f$11;
                                    private final /* synthetic */ InputManagerService f$12;
                                    private final /* synthetic */ TelephonyRegistry f$13;
                                    private final /* synthetic */ MediaRouterService f$14;
                                    private final /* synthetic */ MmsServiceBroker f$15;
                                    private final /* synthetic */ boolean f$16;
                                    private final /* synthetic */ boolean f$17;
                                    private final /* synthetic */ WindowManagerService f$2;
                                    private final /* synthetic */ NetworkManagementService f$3;
                                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                                    private final /* synthetic */ IpSecService f$5;
                                    private final /* synthetic */ NetworkStatsService f$6;
                                    private final /* synthetic */ ConnectivityService f$7;
                                    private final /* synthetic */ LocationManagerService f$8;
                                    private final /* synthetic */ CountryDetectorService f$9;

                                    {
                                        this.f$0 = r3;
                                        this.f$1 = r4;
                                        this.f$2 = r5;
                                        this.f$3 = r6;
                                        this.f$4 = r7;
                                        this.f$5 = r8;
                                        this.f$6 = r9;
                                        this.f$7 = r10;
                                        this.f$8 = r11;
                                        this.f$9 = r12;
                                        this.f$10 = r13;
                                        this.f$11 = r14;
                                        this.f$12 = r15;
                                        this.f$13 = r16;
                                        this.f$14 = r17;
                                        this.f$15 = r18;
                                        this.f$16 = r19;
                                        this.f$17 = r20;
                                    }

                                    public final void run() {
                                        SystemServer systemServer = this.f$0;
                                        Context context = this.f$1;
                                        WindowManagerService windowManagerService = this.f$2;
                                        NetworkManagementService networkManagementService = this.f$3;
                                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                        IpSecService ipSecService = this.f$5;
                                        NetworkStatsService networkStatsService = this.f$6;
                                        ConnectivityService connectivityService = this.f$7;
                                        LocationManagerService locationManagerService = this.f$8;
                                        CountryDetectorService countryDetectorService = this.f$9;
                                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                                        InputManagerService inputManagerService = this.f$12;
                                        TelephonyRegistry telephonyRegistry = this.f$13;
                                        MediaRouterService mediaRouterService = this.f$14;
                                        MmsServiceBroker mmsServiceBroker = this.f$15;
                                        boolean z = this.f$16;
                                        boolean z2 = z;
                                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                    }
                                };
                                activityManagerService22222222222222222222222.systemReady(r022222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                            }
                            traceEnd();
                            traceBeginAndSlog("CertBlacklister");
                            try {
                                new CertBlacklister(context);
                            } catch (Throwable e35) {
                                reportWtf("starting CertBlacklister", e35);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            traceBeginAndSlog("StartDreamManager");
                            this.mSystemServiceManager.startService(DreamManagerService.class);
                            traceEnd();
                            traceBeginAndSlog("AddGraphicsStatsService");
                            ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                            traceEnd();
                            if (CoverageService.ENABLED) {
                                traceBeginAndSlog("AddCoverageService");
                                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                                traceEnd();
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                traceBeginAndSlog("StartPrintManager");
                                this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                                traceEnd();
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                                traceBeginAndSlog("StartCompanionDeviceManager");
                                this.mSystemServiceManager.startService(COMPANION_DEVICE_MANAGER_SERVICE_CLASS);
                                traceEnd();
                            }
                            traceBeginAndSlog("StartRestrictionManager");
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            traceEnd();
                            traceBeginAndSlog("StartMediaSessionService");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            traceEnd();
                            traceBeginAndSlog("StartMediaUpdateService");
                            this.mSystemServiceManager.startService(MediaUpdateService.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                traceBeginAndSlog("StartHdmiControlService");
                                this.mSystemServiceManager.startService(HdmiControlService.class);
                                traceEnd();
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv") || this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                traceBeginAndSlog("StartTvInputManager");
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                traceEnd();
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                traceBeginAndSlog("StartMediaResourceMonitor");
                                this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                                traceEnd();
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                traceBeginAndSlog("StartTvRemoteService");
                                this.mSystemServiceManager.startService(TvRemoteService.class);
                                traceEnd();
                            }
                            traceBeginAndSlog("StartMediaRouterService");
                            try {
                                mediaRouterService = new MediaRouterService(context);
                                ServiceManager.addService("media_router", mediaRouterService);
                                commonTimeMgmtService = commonTimeMgmtService2;
                                mediaRouter2 = mediaRouterService;
                            } catch (Throwable th20) {
                                e = th20;
                                mediaRouter3 = null;
                                commonTimeMgmtService = commonTimeMgmtService2;
                                reportWtf("starting MediaRouterService", e);
                                mediaRouter2 = mediaRouter3;
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                }
                                traceBeginAndSlog("StartBackgroundDexOptService");
                                BackgroundDexOptService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartPruneInstantAppsJobService");
                                PruneInstantAppsJobService.schedule(context);
                                traceEnd();
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                traceBeginAndSlog("StartCrossProfileAppsService");
                                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                traceEnd();
                                connectivity = connectivity2;
                                countryDetector = countryDetector2;
                                iNetworkManagementService = iNetworkManagementService2;
                                iNetworkStatsService = iNetworkStatsService2;
                                IBinder iBinder112222222222222 = iBinder1022;
                                IBinder iBinder122222222222222 = iBinder6;
                                networkTimeUpdater = networkTimeUpdater3;
                                IBinder iBinder132222222222222 = iBinder2;
                                lockSettings2 = lockSettings322222222222;
                                INotificationManager iNotificationManager2222222222222 = notification222;
                                location = location3;
                                ipSecServiceF = iBinder5;
                                lockSettings = iLockSettings;
                                if (!isWatch) {
                                }
                                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                if (isWatch) {
                                }
                                if (!disableSlices) {
                                }
                                if (!disableCameraService) {
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                }
                                traceBeginAndSlog("StartStatsCompanionService");
                                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                traceEnd();
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                MmsServiceBroker mmsService222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                if (isStartSysSvcCallRecord) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings2 != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                this.mSystemServiceManager.startBootPhase(480);
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                this.mSystemServiceManager.startBootPhase(500);
                                traceEnd();
                                traceBeginAndSlog("MakeWindowManagerServiceReady");
                                wm.systemReady();
                                traceEnd();
                                if (safeMode) {
                                }
                                Configuration config222222222222222222222222 = wm.computeNewConfiguration(0);
                                DisplayMetrics metrics222222222222222222222222 = new DisplayMetrics();
                                WindowManager w222222222222222222222222 = (WindowManager) context.getSystemService("window");
                                w222222222222222222222222.getDefaultDisplay().getMetrics(metrics222222222222222222222222);
                                context.getResources().updateConfiguration(config222222222222222222222222, metrics222222222222222222222222);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                traceBeginAndSlog("StartDeviceSpecificServices");
                                String[] classes222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                length = classes222222222222222222222222.length;
                                ILockSettings lockSettings4222222222222222222222222 = lockSettings2;
                                i2 = 0;
                                WindowManagerService wm2222222222222222222222222 = wm;
                                while (i2 < length) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                traceEnd();
                                String[] strArr222222222222222222222222 = classes222222222222222222222222;
                                VibratorService vibratorService3222222222222222222222222 = vibrator;
                                Resources.Theme theme222222222222222222222222 = systemTheme;
                                WindowManager windowManager222222222222222222222222 = w222222222222222222222222;
                                DisplayMetrics displayMetrics222222222222222222222222 = metrics222222222222222222222222;
                                Configuration configuration222222222222222222222222 = config222222222222222222222222;
                                boolean z2222222222222222222222222 = tuiEnable;
                                LocationManagerService locationManagerService222222222222222222222222 = location;
                                ILockSettings iLockSettings2222222222222222222222222 = lockSettings4222222222222222222222222;
                                ActivityManagerService activityManagerService222222222222222222222222 = this.mActivityManagerService;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222222222222222222222222 = r122222222222222222222222;
                                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222222222222222222222222 = new Runnable(this, context, wm2222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222222222222222222222222, enableIaware, safeMode) {
                                    private final /* synthetic */ SystemServer f$0;
                                    private final /* synthetic */ Context f$1;
                                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                                    private final /* synthetic */ CommonTimeManagementService f$11;
                                    private final /* synthetic */ InputManagerService f$12;
                                    private final /* synthetic */ TelephonyRegistry f$13;
                                    private final /* synthetic */ MediaRouterService f$14;
                                    private final /* synthetic */ MmsServiceBroker f$15;
                                    private final /* synthetic */ boolean f$16;
                                    private final /* synthetic */ boolean f$17;
                                    private final /* synthetic */ WindowManagerService f$2;
                                    private final /* synthetic */ NetworkManagementService f$3;
                                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                                    private final /* synthetic */ IpSecService f$5;
                                    private final /* synthetic */ NetworkStatsService f$6;
                                    private final /* synthetic */ ConnectivityService f$7;
                                    private final /* synthetic */ LocationManagerService f$8;
                                    private final /* synthetic */ CountryDetectorService f$9;

                                    {
                                        this.f$0 = r3;
                                        this.f$1 = r4;
                                        this.f$2 = r5;
                                        this.f$3 = r6;
                                        this.f$4 = r7;
                                        this.f$5 = r8;
                                        this.f$6 = r9;
                                        this.f$7 = r10;
                                        this.f$8 = r11;
                                        this.f$9 = r12;
                                        this.f$10 = r13;
                                        this.f$11 = r14;
                                        this.f$12 = r15;
                                        this.f$13 = r16;
                                        this.f$14 = r17;
                                        this.f$15 = r18;
                                        this.f$16 = r19;
                                        this.f$17 = r20;
                                    }

                                    public final void run() {
                                        SystemServer systemServer = this.f$0;
                                        Context context = this.f$1;
                                        WindowManagerService windowManagerService = this.f$2;
                                        NetworkManagementService networkManagementService = this.f$3;
                                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                        IpSecService ipSecService = this.f$5;
                                        NetworkStatsService networkStatsService = this.f$6;
                                        ConnectivityService connectivityService = this.f$7;
                                        LocationManagerService locationManagerService = this.f$8;
                                        CountryDetectorService countryDetectorService = this.f$9;
                                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                                        InputManagerService inputManagerService = this.f$12;
                                        TelephonyRegistry telephonyRegistry = this.f$13;
                                        MediaRouterService mediaRouterService = this.f$14;
                                        MmsServiceBroker mmsServiceBroker = this.f$15;
                                        boolean z = this.f$16;
                                        boolean z2 = z;
                                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                    }
                                };
                                activityManagerService222222222222222222222222.systemReady(r0222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                            }
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                                traceBeginAndSlog("StartFingerprintSensor");
                                try {
                                    HwServiceFactory.IHwFingerprintService ifs = HwServiceFactory.getHwFingerprintService();
                                    if (ifs != null) {
                                        try {
                                            Class<SystemService> serviceClass2 = ifs.createServiceClass();
                                            HwServiceFactory.IHwFingerprintService iHwFingerprintService = ifs;
                                            Slog.i(TAG, "serviceClass doesn't null");
                                            serviceClass = serviceClass2;
                                        } catch (Throwable th21) {
                                            e = th21;
                                            mediaRouter = mediaRouter2;
                                            Slog.e(TAG, "Start fingerprintservice error", e);
                                            traceEnd();
                                            traceBeginAndSlog("StartBackgroundDexOptService");
                                            BackgroundDexOptService.schedule(context);
                                            traceEnd();
                                            traceBeginAndSlog("StartPruneInstantAppsJobService");
                                            PruneInstantAppsJobService.schedule(context);
                                            traceEnd();
                                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                            traceEnd();
                                            traceBeginAndSlog("StartLauncherAppsService");
                                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                                            traceEnd();
                                            traceBeginAndSlog("StartCrossProfileAppsService");
                                            this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                            traceEnd();
                                            connectivity = connectivity2;
                                            countryDetector = countryDetector2;
                                            iNetworkManagementService = iNetworkManagementService2;
                                            iNetworkStatsService = iNetworkStatsService2;
                                            IBinder iBinder1122222222222222 = iBinder1022;
                                            IBinder iBinder1222222222222222 = iBinder6;
                                            networkTimeUpdater = networkTimeUpdater3;
                                            IBinder iBinder1322222222222222 = iBinder2;
                                            lockSettings2 = lockSettings322222222222;
                                            INotificationManager iNotificationManager22222222222222 = notification222;
                                            location = location3;
                                            ipSecServiceF = iBinder5;
                                            lockSettings = iLockSettings;
                                            if (!isWatch) {
                                            }
                                            MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                            if (isWatch) {
                                            }
                                            if (!disableSlices) {
                                            }
                                            if (!disableCameraService) {
                                            }
                                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                            }
                                            traceBeginAndSlog("StartStatsCompanionService");
                                            this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                            traceEnd();
                                            safeMode = wm.detectSafeMode();
                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                            if (safeMode) {
                                            }
                                            traceBeginAndSlog("StartMmsService");
                                            MmsServiceBroker mmsService2222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                            traceEnd();
                                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                            }
                                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                            }
                                            if (isStartSysSvcCallRecord) {
                                            }
                                            traceBeginAndSlog("MakeVibratorServiceReady");
                                            vibrator.systemReady();
                                            traceEnd();
                                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                                            if (lockSettings2 != null) {
                                            }
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                            this.mSystemServiceManager.startBootPhase(480);
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                            this.mSystemServiceManager.startBootPhase(500);
                                            traceEnd();
                                            traceBeginAndSlog("MakeWindowManagerServiceReady");
                                            wm.systemReady();
                                            traceEnd();
                                            if (safeMode) {
                                            }
                                            Configuration config2222222222222222222222222 = wm.computeNewConfiguration(0);
                                            DisplayMetrics metrics2222222222222222222222222 = new DisplayMetrics();
                                            WindowManager w2222222222222222222222222 = (WindowManager) context.getSystemService("window");
                                            w2222222222222222222222222.getDefaultDisplay().getMetrics(metrics2222222222222222222222222);
                                            context.getResources().updateConfiguration(config2222222222222222222222222, metrics2222222222222222222222222);
                                            systemTheme = context.getTheme();
                                            if (systemTheme.getChangingConfigurations() != 0) {
                                            }
                                            traceBeginAndSlog("MakePowerManagerServiceReady");
                                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                            traceEnd();
                                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                            traceBeginAndSlog("MakePackageManagerServiceReady");
                                            this.mPackageManagerService.systemReady();
                                            traceEnd();
                                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                            traceEnd();
                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                            traceBeginAndSlog("StartDeviceSpecificServices");
                                            String[] classes2222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                            length = classes2222222222222222222222222.length;
                                            ILockSettings lockSettings42222222222222222222222222 = lockSettings2;
                                            i2 = 0;
                                            WindowManagerService wm22222222222222222222222222 = wm;
                                            while (i2 < length) {
                                            }
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                            traceEnd();
                                            String[] strArr2222222222222222222222222 = classes2222222222222222222222222;
                                            VibratorService vibratorService32222222222222222222222222 = vibrator;
                                            Resources.Theme theme2222222222222222222222222 = systemTheme;
                                            WindowManager windowManager2222222222222222222222222 = w2222222222222222222222222;
                                            DisplayMetrics displayMetrics2222222222222222222222222 = metrics2222222222222222222222222;
                                            Configuration configuration2222222222222222222222222 = config2222222222222222222222222;
                                            boolean z22222222222222222222222222 = tuiEnable;
                                            LocationManagerService locationManagerService2222222222222222222222222 = location;
                                            ILockSettings iLockSettings22222222222222222222222222 = lockSettings42222222222222222222222222;
                                            ActivityManagerService activityManagerService2222222222222222222222222 = this.mActivityManagerService;
                                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222222222222222222222222 = r1222222222222222222222222;
                                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222222222222222222222222 = new Runnable(this, context, wm22222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222222222222222222222222, enableIaware, safeMode) {
                                                private final /* synthetic */ SystemServer f$0;
                                                private final /* synthetic */ Context f$1;
                                                private final /* synthetic */ NetworkTimeUpdateService f$10;
                                                private final /* synthetic */ CommonTimeManagementService f$11;
                                                private final /* synthetic */ InputManagerService f$12;
                                                private final /* synthetic */ TelephonyRegistry f$13;
                                                private final /* synthetic */ MediaRouterService f$14;
                                                private final /* synthetic */ MmsServiceBroker f$15;
                                                private final /* synthetic */ boolean f$16;
                                                private final /* synthetic */ boolean f$17;
                                                private final /* synthetic */ WindowManagerService f$2;
                                                private final /* synthetic */ NetworkManagementService f$3;
                                                private final /* synthetic */ NetworkPolicyManagerService f$4;
                                                private final /* synthetic */ IpSecService f$5;
                                                private final /* synthetic */ NetworkStatsService f$6;
                                                private final /* synthetic */ ConnectivityService f$7;
                                                private final /* synthetic */ LocationManagerService f$8;
                                                private final /* synthetic */ CountryDetectorService f$9;

                                                {
                                                    this.f$0 = r3;
                                                    this.f$1 = r4;
                                                    this.f$2 = r5;
                                                    this.f$3 = r6;
                                                    this.f$4 = r7;
                                                    this.f$5 = r8;
                                                    this.f$6 = r9;
                                                    this.f$7 = r10;
                                                    this.f$8 = r11;
                                                    this.f$9 = r12;
                                                    this.f$10 = r13;
                                                    this.f$11 = r14;
                                                    this.f$12 = r15;
                                                    this.f$13 = r16;
                                                    this.f$14 = r17;
                                                    this.f$15 = r18;
                                                    this.f$16 = r19;
                                                    this.f$17 = r20;
                                                }

                                                public final void run() {
                                                    SystemServer systemServer = this.f$0;
                                                    Context context = this.f$1;
                                                    WindowManagerService windowManagerService = this.f$2;
                                                    NetworkManagementService networkManagementService = this.f$3;
                                                    NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                                    IpSecService ipSecService = this.f$5;
                                                    NetworkStatsService networkStatsService = this.f$6;
                                                    ConnectivityService connectivityService = this.f$7;
                                                    LocationManagerService locationManagerService = this.f$8;
                                                    CountryDetectorService countryDetectorService = this.f$9;
                                                    NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                                    CommonTimeManagementService commonTimeManagementService = this.f$11;
                                                    InputManagerService inputManagerService = this.f$12;
                                                    TelephonyRegistry telephonyRegistry = this.f$13;
                                                    MediaRouterService mediaRouterService = this.f$14;
                                                    MmsServiceBroker mmsServiceBroker = this.f$15;
                                                    boolean z = this.f$16;
                                                    boolean z2 = z;
                                                    SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                                }
                                            };
                                            activityManagerService2222222222222222222222222.systemReady(r02222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                                        }
                                    } else {
                                        HwServiceFactory.IHwFingerprintService iHwFingerprintService2 = ifs;
                                        Slog.e(TAG, "HwFingerPrintService is null!");
                                        serviceClass = null;
                                    }
                                    if (serviceClass != null) {
                                        mediaRouter = mediaRouter2;
                                        try {
                                            Slog.i(TAG, "start HwFingerPrintService");
                                            this.mSystemServiceManager.startService(serviceClass);
                                        } catch (Throwable th22) {
                                            e = th22;
                                            Slog.e(TAG, "Start fingerprintservice error", e);
                                            traceEnd();
                                            traceBeginAndSlog("StartBackgroundDexOptService");
                                            BackgroundDexOptService.schedule(context);
                                            traceEnd();
                                            traceBeginAndSlog("StartPruneInstantAppsJobService");
                                            PruneInstantAppsJobService.schedule(context);
                                            traceEnd();
                                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                            traceEnd();
                                            traceBeginAndSlog("StartLauncherAppsService");
                                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                                            traceEnd();
                                            traceBeginAndSlog("StartCrossProfileAppsService");
                                            this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                            traceEnd();
                                            connectivity = connectivity2;
                                            countryDetector = countryDetector2;
                                            iNetworkManagementService = iNetworkManagementService2;
                                            iNetworkStatsService = iNetworkStatsService2;
                                            IBinder iBinder11222222222222222 = iBinder1022;
                                            IBinder iBinder12222222222222222 = iBinder6;
                                            networkTimeUpdater = networkTimeUpdater3;
                                            IBinder iBinder13222222222222222 = iBinder2;
                                            lockSettings2 = lockSettings322222222222;
                                            INotificationManager iNotificationManager222222222222222 = notification222;
                                            location = location3;
                                            ipSecServiceF = iBinder5;
                                            lockSettings = iLockSettings;
                                            if (!isWatch) {
                                            }
                                            MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                            if (isWatch) {
                                            }
                                            if (!disableSlices) {
                                            }
                                            if (!disableCameraService) {
                                            }
                                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                            }
                                            traceBeginAndSlog("StartStatsCompanionService");
                                            this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                            traceEnd();
                                            safeMode = wm.detectSafeMode();
                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                            if (safeMode) {
                                            }
                                            traceBeginAndSlog("StartMmsService");
                                            MmsServiceBroker mmsService22222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                            traceEnd();
                                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                            }
                                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                            }
                                            if (isStartSysSvcCallRecord) {
                                            }
                                            traceBeginAndSlog("MakeVibratorServiceReady");
                                            vibrator.systemReady();
                                            traceEnd();
                                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                                            if (lockSettings2 != null) {
                                            }
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                            this.mSystemServiceManager.startBootPhase(480);
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                            this.mSystemServiceManager.startBootPhase(500);
                                            traceEnd();
                                            traceBeginAndSlog("MakeWindowManagerServiceReady");
                                            wm.systemReady();
                                            traceEnd();
                                            if (safeMode) {
                                            }
                                            Configuration config22222222222222222222222222 = wm.computeNewConfiguration(0);
                                            DisplayMetrics metrics22222222222222222222222222 = new DisplayMetrics();
                                            WindowManager w22222222222222222222222222 = (WindowManager) context.getSystemService("window");
                                            w22222222222222222222222222.getDefaultDisplay().getMetrics(metrics22222222222222222222222222);
                                            context.getResources().updateConfiguration(config22222222222222222222222222, metrics22222222222222222222222222);
                                            systemTheme = context.getTheme();
                                            if (systemTheme.getChangingConfigurations() != 0) {
                                            }
                                            traceBeginAndSlog("MakePowerManagerServiceReady");
                                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                            traceEnd();
                                            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                            traceBeginAndSlog("MakePackageManagerServiceReady");
                                            this.mPackageManagerService.systemReady();
                                            traceEnd();
                                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                            traceEnd();
                                            this.mSystemServiceManager.setSafeMode(safeMode);
                                            traceBeginAndSlog("StartDeviceSpecificServices");
                                            String[] classes22222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                            length = classes22222222222222222222222222.length;
                                            ILockSettings lockSettings422222222222222222222222222 = lockSettings2;
                                            i2 = 0;
                                            WindowManagerService wm222222222222222222222222222 = wm;
                                            while (i2 < length) {
                                            }
                                            traceEnd();
                                            traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                            traceEnd();
                                            String[] strArr22222222222222222222222222 = classes22222222222222222222222222;
                                            VibratorService vibratorService322222222222222222222222222 = vibrator;
                                            Resources.Theme theme22222222222222222222222222 = systemTheme;
                                            WindowManager windowManager22222222222222222222222222 = w22222222222222222222222222;
                                            DisplayMetrics displayMetrics22222222222222222222222222 = metrics22222222222222222222222222;
                                            Configuration configuration22222222222222222222222222 = config22222222222222222222222222;
                                            boolean z222222222222222222222222222 = tuiEnable;
                                            LocationManagerService locationManagerService22222222222222222222222222 = location;
                                            ILockSettings iLockSettings222222222222222222222222222 = lockSettings422222222222222222222222222;
                                            ActivityManagerService activityManagerService22222222222222222222222222 = this.mActivityManagerService;
                                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022222222222222222222222222 = r12222222222222222222222222;
                                            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122222222222222222222222222 = new Runnable(this, context, wm222222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22222222222222222222222222, enableIaware, safeMode) {
                                                private final /* synthetic */ SystemServer f$0;
                                                private final /* synthetic */ Context f$1;
                                                private final /* synthetic */ NetworkTimeUpdateService f$10;
                                                private final /* synthetic */ CommonTimeManagementService f$11;
                                                private final /* synthetic */ InputManagerService f$12;
                                                private final /* synthetic */ TelephonyRegistry f$13;
                                                private final /* synthetic */ MediaRouterService f$14;
                                                private final /* synthetic */ MmsServiceBroker f$15;
                                                private final /* synthetic */ boolean f$16;
                                                private final /* synthetic */ boolean f$17;
                                                private final /* synthetic */ WindowManagerService f$2;
                                                private final /* synthetic */ NetworkManagementService f$3;
                                                private final /* synthetic */ NetworkPolicyManagerService f$4;
                                                private final /* synthetic */ IpSecService f$5;
                                                private final /* synthetic */ NetworkStatsService f$6;
                                                private final /* synthetic */ ConnectivityService f$7;
                                                private final /* synthetic */ LocationManagerService f$8;
                                                private final /* synthetic */ CountryDetectorService f$9;

                                                {
                                                    this.f$0 = r3;
                                                    this.f$1 = r4;
                                                    this.f$2 = r5;
                                                    this.f$3 = r6;
                                                    this.f$4 = r7;
                                                    this.f$5 = r8;
                                                    this.f$6 = r9;
                                                    this.f$7 = r10;
                                                    this.f$8 = r11;
                                                    this.f$9 = r12;
                                                    this.f$10 = r13;
                                                    this.f$11 = r14;
                                                    this.f$12 = r15;
                                                    this.f$13 = r16;
                                                    this.f$14 = r17;
                                                    this.f$15 = r18;
                                                    this.f$16 = r19;
                                                    this.f$17 = r20;
                                                }

                                                public final void run() {
                                                    SystemServer systemServer = this.f$0;
                                                    Context context = this.f$1;
                                                    WindowManagerService windowManagerService = this.f$2;
                                                    NetworkManagementService networkManagementService = this.f$3;
                                                    NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                                    IpSecService ipSecService = this.f$5;
                                                    NetworkStatsService networkStatsService = this.f$6;
                                                    ConnectivityService connectivityService = this.f$7;
                                                    LocationManagerService locationManagerService = this.f$8;
                                                    CountryDetectorService countryDetectorService = this.f$9;
                                                    NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                                    CommonTimeManagementService commonTimeManagementService = this.f$11;
                                                    InputManagerService inputManagerService = this.f$12;
                                                    TelephonyRegistry telephonyRegistry = this.f$13;
                                                    MediaRouterService mediaRouterService = this.f$14;
                                                    MmsServiceBroker mmsServiceBroker = this.f$15;
                                                    boolean z = this.f$16;
                                                    boolean z2 = z;
                                                    SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                                }
                                            };
                                            activityManagerService22222222222222222222222222.systemReady(r022222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                                        }
                                    } else {
                                        mediaRouter = mediaRouter2;
                                        this.mSystemServiceManager.startService(FingerprintService.class);
                                    }
                                    Slog.i(TAG, "FingerPrintService ready");
                                } catch (Throwable th23) {
                                    e = th23;
                                    mediaRouter = mediaRouter2;
                                    Slog.e(TAG, "Start fingerprintservice error", e);
                                    traceEnd();
                                    traceBeginAndSlog("StartBackgroundDexOptService");
                                    BackgroundDexOptService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartPruneInstantAppsJobService");
                                    PruneInstantAppsJobService.schedule(context);
                                    traceEnd();
                                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartLauncherAppsService");
                                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartCrossProfileAppsService");
                                    this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                                    traceEnd();
                                    connectivity = connectivity2;
                                    countryDetector = countryDetector2;
                                    iNetworkManagementService = iNetworkManagementService2;
                                    iNetworkStatsService = iNetworkStatsService2;
                                    IBinder iBinder112222222222222222 = iBinder1022;
                                    IBinder iBinder122222222222222222 = iBinder6;
                                    networkTimeUpdater = networkTimeUpdater3;
                                    IBinder iBinder132222222222222222 = iBinder2;
                                    lockSettings2 = lockSettings322222222222;
                                    INotificationManager iNotificationManager2222222222222222 = notification222;
                                    location = location3;
                                    ipSecServiceF = iBinder5;
                                    lockSettings = iLockSettings;
                                    if (!isWatch) {
                                    }
                                    MediaProjectionManagerService.sHasStartedInSystemserver = true;
                                    if (isWatch) {
                                    }
                                    if (!disableSlices) {
                                    }
                                    if (!disableCameraService) {
                                    }
                                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                    }
                                    traceBeginAndSlog("StartStatsCompanionService");
                                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                    traceEnd();
                                    safeMode = wm.detectSafeMode();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    if (safeMode) {
                                    }
                                    traceBeginAndSlog("StartMmsService");
                                    MmsServiceBroker mmsService222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                    }
                                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                    }
                                    if (isStartSysSvcCallRecord) {
                                    }
                                    traceBeginAndSlog("MakeVibratorServiceReady");
                                    vibrator.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                                    if (lockSettings2 != null) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                    this.mSystemServiceManager.startBootPhase(480);
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                    this.mSystemServiceManager.startBootPhase(500);
                                    traceEnd();
                                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                                    wm.systemReady();
                                    traceEnd();
                                    if (safeMode) {
                                    }
                                    Configuration config222222222222222222222222222 = wm.computeNewConfiguration(0);
                                    DisplayMetrics metrics222222222222222222222222222 = new DisplayMetrics();
                                    WindowManager w222222222222222222222222222 = (WindowManager) context.getSystemService("window");
                                    w222222222222222222222222222.getDefaultDisplay().getMetrics(metrics222222222222222222222222222);
                                    context.getResources().updateConfiguration(config222222222222222222222222222, metrics222222222222222222222222222);
                                    systemTheme = context.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                    }
                                    traceBeginAndSlog("MakePowerManagerServiceReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    traceEnd();
                                    this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                                    traceBeginAndSlog("MakePackageManagerServiceReady");
                                    this.mPackageManagerService.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    traceEnd();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    traceBeginAndSlog("StartDeviceSpecificServices");
                                    String[] classes222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                                    length = classes222222222222222222222222222.length;
                                    ILockSettings lockSettings4222222222222222222222222222 = lockSettings2;
                                    i2 = 0;
                                    WindowManagerService wm2222222222222222222222222222 = wm;
                                    while (i2 < length) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                    traceEnd();
                                    String[] strArr222222222222222222222222222 = classes222222222222222222222222222;
                                    VibratorService vibratorService3222222222222222222222222222 = vibrator;
                                    Resources.Theme theme222222222222222222222222222 = systemTheme;
                                    WindowManager windowManager222222222222222222222222222 = w222222222222222222222222222;
                                    DisplayMetrics displayMetrics222222222222222222222222222 = metrics222222222222222222222222222;
                                    Configuration configuration222222222222222222222222222 = config222222222222222222222222222;
                                    boolean z2222222222222222222222222222 = tuiEnable;
                                    LocationManagerService locationManagerService222222222222222222222222222 = location;
                                    ILockSettings iLockSettings2222222222222222222222222222 = lockSettings4222222222222222222222222222;
                                    ActivityManagerService activityManagerService222222222222222222222222222 = this.mActivityManagerService;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222222222222222222222222222 = r122222222222222222222222222;
                                    $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222222222222222222222222222 = new Runnable(this, context, wm2222222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222222222222222222222222222, enableIaware, safeMode) {
                                        private final /* synthetic */ SystemServer f$0;
                                        private final /* synthetic */ Context f$1;
                                        private final /* synthetic */ NetworkTimeUpdateService f$10;
                                        private final /* synthetic */ CommonTimeManagementService f$11;
                                        private final /* synthetic */ InputManagerService f$12;
                                        private final /* synthetic */ TelephonyRegistry f$13;
                                        private final /* synthetic */ MediaRouterService f$14;
                                        private final /* synthetic */ MmsServiceBroker f$15;
                                        private final /* synthetic */ boolean f$16;
                                        private final /* synthetic */ boolean f$17;
                                        private final /* synthetic */ WindowManagerService f$2;
                                        private final /* synthetic */ NetworkManagementService f$3;
                                        private final /* synthetic */ NetworkPolicyManagerService f$4;
                                        private final /* synthetic */ IpSecService f$5;
                                        private final /* synthetic */ NetworkStatsService f$6;
                                        private final /* synthetic */ ConnectivityService f$7;
                                        private final /* synthetic */ LocationManagerService f$8;
                                        private final /* synthetic */ CountryDetectorService f$9;

                                        {
                                            this.f$0 = r3;
                                            this.f$1 = r4;
                                            this.f$2 = r5;
                                            this.f$3 = r6;
                                            this.f$4 = r7;
                                            this.f$5 = r8;
                                            this.f$6 = r9;
                                            this.f$7 = r10;
                                            this.f$8 = r11;
                                            this.f$9 = r12;
                                            this.f$10 = r13;
                                            this.f$11 = r14;
                                            this.f$12 = r15;
                                            this.f$13 = r16;
                                            this.f$14 = r17;
                                            this.f$15 = r18;
                                            this.f$16 = r19;
                                            this.f$17 = r20;
                                        }

                                        public final void run() {
                                            SystemServer systemServer = this.f$0;
                                            Context context = this.f$1;
                                            WindowManagerService windowManagerService = this.f$2;
                                            NetworkManagementService networkManagementService = this.f$3;
                                            NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                            IpSecService ipSecService = this.f$5;
                                            NetworkStatsService networkStatsService = this.f$6;
                                            ConnectivityService connectivityService = this.f$7;
                                            LocationManagerService locationManagerService = this.f$8;
                                            CountryDetectorService countryDetectorService = this.f$9;
                                            NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                            CommonTimeManagementService commonTimeManagementService = this.f$11;
                                            InputManagerService inputManagerService = this.f$12;
                                            TelephonyRegistry telephonyRegistry = this.f$13;
                                            MediaRouterService mediaRouterService = this.f$14;
                                            MmsServiceBroker mmsServiceBroker = this.f$15;
                                            boolean z = this.f$16;
                                            boolean z2 = z;
                                            SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                                        }
                                    };
                                    activityManagerService222222222222222222222222222.systemReady(r0222222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                                }
                                traceEnd();
                            } else {
                                mediaRouter = mediaRouter2;
                            }
                            traceBeginAndSlog("StartBackgroundDexOptService");
                            try {
                                BackgroundDexOptService.schedule(context);
                            } catch (Throwable e36) {
                                Throwable th24 = e36;
                                reportWtf("starting StartBackgroundDexOptService", e36);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartPruneInstantAppsJobService");
                            try {
                                PruneInstantAppsJobService.schedule(context);
                            } catch (Throwable e37) {
                                Throwable th25 = e37;
                                reportWtf("StartPruneInstantAppsJobService", e37);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            traceBeginAndSlog("StartCrossProfileAppsService");
                            this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                            traceEnd();
                            connectivity = connectivity2;
                            countryDetector = countryDetector2;
                            iNetworkManagementService = iNetworkManagementService2;
                            iNetworkStatsService = iNetworkStatsService2;
                            IBinder iBinder1122222222222222222 = iBinder1022;
                            IBinder iBinder1222222222222222222 = iBinder6;
                            networkTimeUpdater = networkTimeUpdater3;
                            IBinder iBinder1322222222222222222 = iBinder2;
                            lockSettings2 = lockSettings322222222222;
                            INotificationManager iNotificationManager22222222222222222 = notification222;
                            location = location3;
                            ipSecServiceF = iBinder5;
                            lockSettings = iLockSettings;
                        } else {
                            location = null;
                            networkTimeUpdater = null;
                            commonTimeMgmtService = null;
                            countryDetector = null;
                            lockSettings2 = null;
                            mediaRouter = null;
                            iNetworkManagementService = null;
                            ipSecServiceF = null;
                            iNetworkStatsService = null;
                            lockSettings = null;
                        }
                        if (!isWatch) {
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                        }
                        MediaProjectionManagerService.sHasStartedInSystemserver = true;
                        if (isWatch) {
                            traceBeginAndSlog("StartWearConfigService");
                            this.mSystemServiceManager.startService(WEAR_CONFIG_SERVICE_CLASS);
                            traceEnd();
                            traceBeginAndSlog("StartWearConnectivityService");
                            this.mSystemServiceManager.startService(WEAR_CONNECTIVITY_SERVICE_CLASS);
                            traceEnd();
                            traceBeginAndSlog("StartWearTimeService");
                            this.mSystemServiceManager.startService(WEAR_DISPLAY_SERVICE_CLASS);
                            this.mSystemServiceManager.startService(WEAR_TIME_SERVICE_CLASS);
                            traceEnd();
                            if (enableLeftyService) {
                                traceBeginAndSlog("StartWearLeftyService");
                                this.mSystemServiceManager.startService(WEAR_LEFTY_SERVICE_CLASS);
                                traceEnd();
                            }
                            traceBeginAndSlog("StartWearGlobalActionsService");
                            this.mSystemServiceManager.startService(WEAR_GLOBAL_ACTIONS_SERVICE_CLASS);
                            traceEnd();
                        }
                        if (!disableSlices) {
                            traceBeginAndSlog("StartSliceManagerService");
                            this.mSystemServiceManager.startService(SLICE_MANAGER_SERVICE_CLASS);
                            traceEnd();
                        }
                        if (!disableCameraService) {
                            traceBeginAndSlog("StartCameraServiceProxy");
                            this.mSystemServiceManager.startService(CameraServiceProxy.class);
                            traceEnd();
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                            traceBeginAndSlog("StartIoTSystemService");
                            this.mSystemServiceManager.startService(IOT_SERVICE_CLASS);
                            traceEnd();
                        }
                        traceBeginAndSlog("StartStatsCompanionService");
                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                        traceEnd();
                        safeMode = wm.detectSafeMode();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        if (safeMode) {
                            traceBeginAndSlog("EnterSafeModeAndDisableJitCompilation");
                            this.mActivityManagerService.enterSafeMode();
                            VMRuntime.getRuntime().disableJitCompilation();
                            traceEnd();
                        } else {
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                        }
                        traceBeginAndSlog("StartMmsService");
                        MmsServiceBroker mmsService2222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            traceBeginAndSlog("StartAutoFillService");
                            this.mSystemServiceManager.startService(AUTO_FILL_MANAGER_SERVICE_CLASS);
                            traceEnd();
                        }
                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                            try {
                                this.mSystemServiceManager.startService("com.android.server.HwBastetService");
                            } catch (Exception e38) {
                                Slog.w(TAG, "HwBastetService not exists.");
                            }
                        }
                        if (isStartSysSvcCallRecord) {
                            startSysSvcCallRecordService();
                        }
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings2 != null) {
                            try {
                                lockSettings2.systemReady();
                            } catch (Throwable e39) {
                                Throwable th26 = e39;
                                reportWtf("making Lock Settings Service ready", e39);
                            }
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                        this.mSystemServiceManager.startBootPhase(480);
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                        this.mSystemServiceManager.startBootPhase(500);
                        traceEnd();
                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                        wm.systemReady();
                        traceEnd();
                        if (safeMode) {
                            this.mActivityManagerService.showSafeModeOverlay();
                        }
                        Configuration config2222222222222222222222222222 = wm.computeNewConfiguration(0);
                        DisplayMetrics metrics2222222222222222222222222222 = new DisplayMetrics();
                        WindowManager w2222222222222222222222222222 = (WindowManager) context.getSystemService("window");
                        w2222222222222222222222222222.getDefaultDisplay().getMetrics(metrics2222222222222222222222222222);
                        context.getResources().updateConfiguration(config2222222222222222222222222222, metrics2222222222222222222222222222);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                            systemTheme.rebase();
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        traceBeginAndSlog("StartDeviceSpecificServices");
                        String[] classes2222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                        length = classes2222222222222222222222222222.length;
                        ILockSettings lockSettings42222222222222222222222222222 = lockSettings2;
                        i2 = 0;
                        WindowManagerService wm22222222222222222222222222222 = wm;
                        while (i2 < length) {
                            Context context2 = context;
                            String className = classes2222222222222222222222222222[i2];
                            StringBuilder sb3 = new StringBuilder();
                            WindowManagerService wm3 = wm22222222222222222222222222222;
                            sb3.append("StartDeviceSpecificServices ");
                            sb3.append(className);
                            traceBeginAndSlog(sb3.toString());
                            try {
                                this.mSystemServiceManager.startService(className);
                                i3 = length;
                            } catch (Throwable e40) {
                                StringBuilder sb4 = new StringBuilder();
                                i3 = length;
                                sb4.append("starting ");
                                sb4.append(className);
                                reportWtf(sb4.toString(), e40);
                            }
                            traceEnd();
                            i2++;
                            context = context2;
                            wm22222222222222222222222222222 = wm3;
                            length = i3;
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                        traceEnd();
                        String[] strArr2222222222222222222222222222 = classes2222222222222222222222222222;
                        VibratorService vibratorService32222222222222222222222222222 = vibrator;
                        Resources.Theme theme2222222222222222222222222222 = systemTheme;
                        WindowManager windowManager2222222222222222222222222222 = w2222222222222222222222222222;
                        DisplayMetrics displayMetrics2222222222222222222222222222 = metrics2222222222222222222222222222;
                        Configuration configuration2222222222222222222222222222 = config2222222222222222222222222222;
                        boolean z22222222222222222222222222222 = tuiEnable;
                        LocationManagerService locationManagerService2222222222222222222222222222 = location;
                        ILockSettings iLockSettings22222222222222222222222222222 = lockSettings42222222222222222222222222222;
                        ActivityManagerService activityManagerService2222222222222222222222222222 = this.mActivityManagerService;
                        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222222222222222222222222222 = r1222222222222222222222222222;
                        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222222222222222222222222222 = new Runnable(this, context, wm22222222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222222222222222222222222222, enableIaware, safeMode) {
                            private final /* synthetic */ SystemServer f$0;
                            private final /* synthetic */ Context f$1;
                            private final /* synthetic */ NetworkTimeUpdateService f$10;
                            private final /* synthetic */ CommonTimeManagementService f$11;
                            private final /* synthetic */ InputManagerService f$12;
                            private final /* synthetic */ TelephonyRegistry f$13;
                            private final /* synthetic */ MediaRouterService f$14;
                            private final /* synthetic */ MmsServiceBroker f$15;
                            private final /* synthetic */ boolean f$16;
                            private final /* synthetic */ boolean f$17;
                            private final /* synthetic */ WindowManagerService f$2;
                            private final /* synthetic */ NetworkManagementService f$3;
                            private final /* synthetic */ NetworkPolicyManagerService f$4;
                            private final /* synthetic */ IpSecService f$5;
                            private final /* synthetic */ NetworkStatsService f$6;
                            private final /* synthetic */ ConnectivityService f$7;
                            private final /* synthetic */ LocationManagerService f$8;
                            private final /* synthetic */ CountryDetectorService f$9;

                            {
                                this.f$0 = r3;
                                this.f$1 = r4;
                                this.f$2 = r5;
                                this.f$3 = r6;
                                this.f$4 = r7;
                                this.f$5 = r8;
                                this.f$6 = r9;
                                this.f$7 = r10;
                                this.f$8 = r11;
                                this.f$9 = r12;
                                this.f$10 = r13;
                                this.f$11 = r14;
                                this.f$12 = r15;
                                this.f$13 = r16;
                                this.f$14 = r17;
                                this.f$15 = r18;
                                this.f$16 = r19;
                                this.f$17 = r20;
                            }

                            public final void run() {
                                SystemServer systemServer = this.f$0;
                                Context context = this.f$1;
                                WindowManagerService windowManagerService = this.f$2;
                                NetworkManagementService networkManagementService = this.f$3;
                                NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                                IpSecService ipSecService = this.f$5;
                                NetworkStatsService networkStatsService = this.f$6;
                                ConnectivityService connectivityService = this.f$7;
                                LocationManagerService locationManagerService = this.f$8;
                                CountryDetectorService countryDetectorService = this.f$9;
                                NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                                CommonTimeManagementService commonTimeManagementService = this.f$11;
                                InputManagerService inputManagerService = this.f$12;
                                TelephonyRegistry telephonyRegistry = this.f$13;
                                MediaRouterService mediaRouterService = this.f$14;
                                MmsServiceBroker mmsServiceBroker = this.f$15;
                                boolean z = this.f$16;
                                boolean z2 = z;
                                SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                            }
                        };
                        activityManagerService2222222222222222222222222222.systemReady(r02222222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
                    }
                    telephonyRegistry3 = new TelephonyRegistry(context);
                }
                r8 = telephonyRegistry3;
            } catch (RuntimeException e41) {
                e = e41;
                inputManager3 = null;
                HwCustEmergDataManager hwCustEmergDataManager10 = emergDataManager;
                storageManager3 = null;
                connectivity4 = null;
                tuiEnable2 = tuiEnable3;
                i = 1;
                telephonyRegistry = null;
                vibrator2 = null;
                almService = null;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e);
                AlarmManagerService alarmManagerService222222222222 = almService;
                wm = obj;
                inputManager = inputManager2;
                vibrator = vibrator2;
                IBinder iBinder7222222222222 = null;
                ILockSettings lockSettings3222222222222 = null;
                if (this.mFactoryTestMode != i) {
                }
                traceBeginAndSlog("MakeDisplayReady");
                wm.displayReady();
                traceEnd();
                traceBeginAndSlog("StartStorageManagerService");
                this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                traceEnd();
                traceBeginAndSlog("StartStorageStatsService");
                this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                traceEnd();
                IStorageManager iStorageManager222222222222 = storageManager2;
                traceBeginAndSlog("StartUiModeManager");
                this.mSystemServiceManager.startService(UiModeManagerService.class);
                traceEnd();
                HwBootCheck.bootSceneEnd(101);
                HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                if (!this.mRuntimeRestart) {
                }
                HwBootCheck.bootSceneStart(104, 900000);
                if (!this.mOnlyCore) {
                }
                traceBeginAndSlog("PerformFstrimIfNeeded");
                this.mPackageManagerService.performFstrimIfNeeded();
                traceEnd();
                HwBootCheck.bootSceneEnd(104);
                HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                if (this.mFactoryTestMode == 1) {
                }
                if (!isWatch) {
                }
                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                safeMode = wm.detectSafeMode();
                this.mSystemServiceManager.setSafeMode(safeMode);
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService22222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                }
                if (isStartSysSvcCallRecord) {
                }
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings2 != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(480);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config22222222222222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics22222222222222222222222222222 = new DisplayMetrics();
                WindowManager w22222222222222222222222222222 = (WindowManager) context.getSystemService("window");
                w22222222222222222222222222222.getDefaultDisplay().getMetrics(metrics22222222222222222222222222222);
                context.getResources().updateConfiguration(config22222222222222222222222222222, metrics22222222222222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                traceBeginAndSlog("MakePackageManagerServiceReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes22222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                length = classes22222222222222222222222222222.length;
                ILockSettings lockSettings422222222222222222222222222222 = lockSettings2;
                i2 = 0;
                WindowManagerService wm222222222222222222222222222222 = wm;
                while (i2 < length) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                String[] strArr22222222222222222222222222222 = classes22222222222222222222222222222;
                VibratorService vibratorService322222222222222222222222222222 = vibrator;
                Resources.Theme theme22222222222222222222222222222 = systemTheme;
                WindowManager windowManager22222222222222222222222222222 = w22222222222222222222222222222;
                DisplayMetrics displayMetrics22222222222222222222222222222 = metrics22222222222222222222222222222;
                Configuration configuration22222222222222222222222222222 = config22222222222222222222222222222;
                boolean z222222222222222222222222222222 = tuiEnable;
                LocationManagerService locationManagerService22222222222222222222222222222 = location;
                ILockSettings iLockSettings222222222222222222222222222222 = lockSettings422222222222222222222222222222;
                ActivityManagerService activityManagerService22222222222222222222222222222 = this.mActivityManagerService;
                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022222222222222222222222222222 = r12222222222222222222222222222;
                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122222222222222222222222222222 = new Runnable(this, context, wm222222222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22222222222222222222222222222, enableIaware, safeMode) {
                    private final /* synthetic */ SystemServer f$0;
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                    private final /* synthetic */ CommonTimeManagementService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ MmsServiceBroker f$15;
                    private final /* synthetic */ boolean f$16;
                    private final /* synthetic */ boolean f$17;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ NetworkManagementService f$3;
                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                    private final /* synthetic */ IpSecService f$5;
                    private final /* synthetic */ NetworkStatsService f$6;
                    private final /* synthetic */ ConnectivityService f$7;
                    private final /* synthetic */ LocationManagerService f$8;
                    private final /* synthetic */ CountryDetectorService f$9;

                    {
                        this.f$0 = r3;
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                        this.f$17 = r20;
                    }

                    public final void run() {
                        SystemServer systemServer = this.f$0;
                        Context context = this.f$1;
                        WindowManagerService windowManagerService = this.f$2;
                        NetworkManagementService networkManagementService = this.f$3;
                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                        IpSecService ipSecService = this.f$5;
                        NetworkStatsService networkStatsService = this.f$6;
                        ConnectivityService connectivityService = this.f$7;
                        LocationManagerService locationManagerService = this.f$8;
                        CountryDetectorService countryDetectorService = this.f$9;
                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                        InputManagerService inputManagerService = this.f$12;
                        TelephonyRegistry telephonyRegistry = this.f$13;
                        MediaRouterService mediaRouterService = this.f$14;
                        MmsServiceBroker mmsServiceBroker = this.f$15;
                        boolean z = this.f$16;
                        boolean z2 = z;
                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                    }
                };
                activityManagerService22222222222222222222222222222.systemReady(r022222222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
            }
            try {
                ServiceManager.addService("telephony.registry", r8);
                traceEnd();
                traceBeginAndSlog("StartEntropyMixer");
                this.mEntropyMixer = new EntropyMixer(context);
                traceEnd();
                this.mContentResolver = context.getContentResolver();
                traceBeginAndSlog("StartAccountManagerService");
                this.mSystemServiceManager.startService(ACCOUNT_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartContentService");
                this.mSystemServiceManager.startService(CONTENT_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("InstallSystemProviders");
                this.mActivityManagerService.installSystemProviders();
                SQLiteCompatibilityWalFlags.reset();
                traceEnd();
                traceBeginAndSlog("StartDropBoxManager");
                this.mSystemServiceManager.startService(DropBoxManagerService.class);
                traceEnd();
                traceBeginAndSlog("StartVibratorService");
                vibratorService = new VibratorService(context);
                ServiceManager.addService("vibrator", vibratorService);
                traceEnd();
                if (isWatch) {
                }
                traceBeginAndSlog("StartAlarmManagerService");
                almService2 = this.mSystemServiceManager.startService("com.android.server.HwAlarmManagerService");
                almService = almService2;
                traceEnd();
                this.mActivityManagerService.setAlarmManager(almService);
                traceBeginAndSlog("Init Watchdog");
                Watchdog watchdog22 = Watchdog.getInstance();
                watchdog22.init(context, this.mActivityManagerService);
                traceEnd();
                traceBeginAndSlog("StartInputManagerService");
                watchdog = watchdog22;
                Slog.i(TAG, "Input Manager");
                inputManager4 = HwServiceFactory.getHwInputManagerService().getInstance(context, null);
            } catch (RuntimeException e42) {
                e = e42;
                inputManager2 = null;
                telephonyRegistry = r8;
                HwCustEmergDataManager hwCustEmergDataManager11 = emergDataManager;
                storageManager = null;
                connectivity = null;
                tuiEnable = tuiEnable3;
                i = 1;
                vibrator2 = null;
                almService = null;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e);
                AlarmManagerService alarmManagerService2222222222222 = almService;
                wm = obj;
                inputManager = inputManager2;
                vibrator = vibrator2;
                IBinder iBinder72222222222222 = null;
                ILockSettings lockSettings32222222222222 = null;
                if (this.mFactoryTestMode != i) {
                }
                traceBeginAndSlog("MakeDisplayReady");
                wm.displayReady();
                traceEnd();
                traceBeginAndSlog("StartStorageManagerService");
                this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                traceEnd();
                traceBeginAndSlog("StartStorageStatsService");
                this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                traceEnd();
                IStorageManager iStorageManager2222222222222 = storageManager2;
                traceBeginAndSlog("StartUiModeManager");
                this.mSystemServiceManager.startService(UiModeManagerService.class);
                traceEnd();
                HwBootCheck.bootSceneEnd(101);
                HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                if (!this.mRuntimeRestart) {
                }
                HwBootCheck.bootSceneStart(104, 900000);
                if (!this.mOnlyCore) {
                }
                traceBeginAndSlog("PerformFstrimIfNeeded");
                this.mPackageManagerService.performFstrimIfNeeded();
                traceEnd();
                HwBootCheck.bootSceneEnd(104);
                HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                if (this.mFactoryTestMode == 1) {
                }
                if (!isWatch) {
                }
                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                safeMode = wm.detectSafeMode();
                this.mSystemServiceManager.setSafeMode(safeMode);
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService222222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                }
                if (isStartSysSvcCallRecord) {
                }
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings2 != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(480);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config222222222222222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics222222222222222222222222222222 = new DisplayMetrics();
                WindowManager w222222222222222222222222222222 = (WindowManager) context.getSystemService("window");
                w222222222222222222222222222222.getDefaultDisplay().getMetrics(metrics222222222222222222222222222222);
                context.getResources().updateConfiguration(config222222222222222222222222222222, metrics222222222222222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                traceBeginAndSlog("MakePackageManagerServiceReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes222222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                length = classes222222222222222222222222222222.length;
                ILockSettings lockSettings4222222222222222222222222222222 = lockSettings2;
                i2 = 0;
                WindowManagerService wm2222222222222222222222222222222 = wm;
                while (i2 < length) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                String[] strArr222222222222222222222222222222 = classes222222222222222222222222222222;
                VibratorService vibratorService3222222222222222222222222222222 = vibrator;
                Resources.Theme theme222222222222222222222222222222 = systemTheme;
                WindowManager windowManager222222222222222222222222222222 = w222222222222222222222222222222;
                DisplayMetrics displayMetrics222222222222222222222222222222 = metrics222222222222222222222222222222;
                Configuration configuration222222222222222222222222222222 = config222222222222222222222222222222;
                boolean z2222222222222222222222222222222 = tuiEnable;
                LocationManagerService locationManagerService222222222222222222222222222222 = location;
                ILockSettings iLockSettings2222222222222222222222222222222 = lockSettings4222222222222222222222222222222;
                ActivityManagerService activityManagerService222222222222222222222222222222 = this.mActivityManagerService;
                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222222222222222222222222222222 = r122222222222222222222222222222;
                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222222222222222222222222222222 = new Runnable(this, context, wm2222222222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222222222222222222222222222222, enableIaware, safeMode) {
                    private final /* synthetic */ SystemServer f$0;
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                    private final /* synthetic */ CommonTimeManagementService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ MmsServiceBroker f$15;
                    private final /* synthetic */ boolean f$16;
                    private final /* synthetic */ boolean f$17;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ NetworkManagementService f$3;
                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                    private final /* synthetic */ IpSecService f$5;
                    private final /* synthetic */ NetworkStatsService f$6;
                    private final /* synthetic */ ConnectivityService f$7;
                    private final /* synthetic */ LocationManagerService f$8;
                    private final /* synthetic */ CountryDetectorService f$9;

                    {
                        this.f$0 = r3;
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                        this.f$17 = r20;
                    }

                    public final void run() {
                        SystemServer systemServer = this.f$0;
                        Context context = this.f$1;
                        WindowManagerService windowManagerService = this.f$2;
                        NetworkManagementService networkManagementService = this.f$3;
                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                        IpSecService ipSecService = this.f$5;
                        NetworkStatsService networkStatsService = this.f$6;
                        ConnectivityService connectivityService = this.f$7;
                        LocationManagerService locationManagerService = this.f$8;
                        CountryDetectorService countryDetectorService = this.f$9;
                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                        InputManagerService inputManagerService = this.f$12;
                        TelephonyRegistry telephonyRegistry = this.f$13;
                        MediaRouterService mediaRouterService = this.f$14;
                        MmsServiceBroker mmsServiceBroker = this.f$15;
                        boolean z = this.f$16;
                        boolean z2 = z;
                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                    }
                };
                activityManagerService222222222222222222222222222222.systemReady(r0222222222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
            }
            try {
                traceEnd();
                traceBeginAndSlog("StartHwSysResManagerService");
                this.mSystemServiceManager.startService("com.android.server.rms.HwSysResManagerService");
                almService3 = almService;
                traceEnd();
                traceBeginAndSlog("StartWindowManagerService");
                ConcurrentUtils.waitForFutureNoInterrupt(this.mSensorServiceStart, START_SENSOR_SERVICE);
                this.mSensorServiceStart = null;
                Watchdog watchdog32 = watchdog;
                almService4 = almService3;
                vibrator3 = vibratorService;
                connectivity = null;
                tuiEnable = tuiEnable3;
                telephonyRegistry = r8;
                storageManager = null;
                HwCustEmergDataManager hwCustEmergDataManager62 = emergDataManager;
                main = WindowManagerService.main(context, inputManager4, this.mFactoryTestMode == 1, !this.mFirstBoot, this.mOnlyCore, HwPolicyFactory.getHwPhoneWindowManager());
                initRogMode(main, context);
                processMultiDPI(main);
                ServiceManager.addService("window", main, false, 17);
                r52 = inputManager4;
                i = 1;
                ServiceManager.addService("input", r52, false, 1);
                traceEnd();
                traceBeginAndSlog("SetWindowManagerService");
                this.mActivityManagerService.setWindowManager(main);
                traceEnd();
                traceBeginAndSlog("WindowManagerServiceOnInitReady");
                main.onInitReady();
                traceEnd();
                SystemServerInitThreadPool.get().submit($$Lambda$SystemServer$JQH6ND0PqyyiRiz7lXLvUmRhwRM.INSTANCE, START_HIDL_SERVICES);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartInputManager");
                r52.setWindowManagerCallbacks(main.getInputMonitor());
                r52.start();
                traceEnd();
                traceBeginAndSlog("DisplayManagerWindowManagerAndInputReady");
                this.mDisplayManagerService.windowManagerAndInputReady();
                traceEnd();
                if (!isEmulator) {
                }
                traceBeginAndSlog("IpConnectivityMetrics");
                this.mSystemServiceManager.startService(IpConnectivityMetrics.class);
                traceEnd();
                traceBeginAndSlog("NetworkWatchlistService");
                this.mSystemServiceManager.startService(NetworkWatchlistService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("PinnerService");
                ((PinnerService) this.mSystemServiceManager.startService(PinnerService.class)).setInstaller(this.installer);
                traceEnd();
                if (dtvEnable) {
                }
                traceBeginAndSlog("ZrHungService");
                this.mSystemServiceManager.startService("com.android.server.zrhung.ZRHungService");
                traceEnd();
                if (isStartHwFSMService) {
                }
                inputManager = r52;
                vibrator = vibrator3;
                wm = main;
            } catch (RuntimeException e43) {
                e = e43;
                Object obj6 = vibratorService;
                telephonyRegistry = r8;
                HwCustEmergDataManager hwCustEmergDataManager12 = emergDataManager;
                storageManager = null;
                connectivity = null;
                tuiEnable = tuiEnable3;
                i = 1;
                inputManager2 = inputManager4;
                almService = almService;
                vibrator2 = vibratorService;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e);
                AlarmManagerService alarmManagerService22222222222222 = almService;
                wm = obj;
                inputManager = inputManager2;
                vibrator = vibrator2;
                IBinder iBinder722222222222222 = null;
                ILockSettings lockSettings322222222222222 = null;
                if (this.mFactoryTestMode != i) {
                }
                traceBeginAndSlog("MakeDisplayReady");
                wm.displayReady();
                traceEnd();
                traceBeginAndSlog("StartStorageManagerService");
                this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                traceEnd();
                traceBeginAndSlog("StartStorageStatsService");
                this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                traceEnd();
                IStorageManager iStorageManager22222222222222 = storageManager2;
                traceBeginAndSlog("StartUiModeManager");
                this.mSystemServiceManager.startService(UiModeManagerService.class);
                traceEnd();
                HwBootCheck.bootSceneEnd(101);
                HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                if (!this.mRuntimeRestart) {
                }
                HwBootCheck.bootSceneStart(104, 900000);
                if (!this.mOnlyCore) {
                }
                traceBeginAndSlog("PerformFstrimIfNeeded");
                this.mPackageManagerService.performFstrimIfNeeded();
                traceEnd();
                HwBootCheck.bootSceneEnd(104);
                HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                if (this.mFactoryTestMode == 1) {
                }
                if (!isWatch) {
                }
                MediaProjectionManagerService.sHasStartedInSystemserver = true;
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                safeMode = wm.detectSafeMode();
                this.mSystemServiceManager.setSafeMode(safeMode);
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService2222222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                }
                if (isStartSysSvcCallRecord) {
                }
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings2 != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(480);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config2222222222222222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics2222222222222222222222222222222 = new DisplayMetrics();
                WindowManager w2222222222222222222222222222222 = (WindowManager) context.getSystemService("window");
                w2222222222222222222222222222222.getDefaultDisplay().getMetrics(metrics2222222222222222222222222222222);
                context.getResources().updateConfiguration(config2222222222222222222222222222222, metrics2222222222222222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
                traceBeginAndSlog("MakePackageManagerServiceReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes2222222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
                length = classes2222222222222222222222222222222.length;
                ILockSettings lockSettings42222222222222222222222222222222 = lockSettings2;
                i2 = 0;
                WindowManagerService wm22222222222222222222222222222222 = wm;
                while (i2 < length) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                String[] strArr2222222222222222222222222222222 = classes2222222222222222222222222222222;
                VibratorService vibratorService32222222222222222222222222222222 = vibrator;
                Resources.Theme theme2222222222222222222222222222222 = systemTheme;
                WindowManager windowManager2222222222222222222222222222222 = w2222222222222222222222222222222;
                DisplayMetrics displayMetrics2222222222222222222222222222222 = metrics2222222222222222222222222222222;
                Configuration configuration2222222222222222222222222222222 = config2222222222222222222222222222222;
                boolean z22222222222222222222222222222222 = tuiEnable;
                LocationManagerService locationManagerService2222222222222222222222222222222 = location;
                ILockSettings iLockSettings22222222222222222222222222222222 = lockSettings42222222222222222222222222222222;
                ActivityManagerService activityManagerService2222222222222222222222222222222 = this.mActivityManagerService;
                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222222222222222222222222222222 = r1222222222222222222222222222222;
                $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222222222222222222222222222222 = new Runnable(this, context, wm22222222222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222222222222222222222222222222, enableIaware, safeMode) {
                    private final /* synthetic */ SystemServer f$0;
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ NetworkTimeUpdateService f$10;
                    private final /* synthetic */ CommonTimeManagementService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ MmsServiceBroker f$15;
                    private final /* synthetic */ boolean f$16;
                    private final /* synthetic */ boolean f$17;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ NetworkManagementService f$3;
                    private final /* synthetic */ NetworkPolicyManagerService f$4;
                    private final /* synthetic */ IpSecService f$5;
                    private final /* synthetic */ NetworkStatsService f$6;
                    private final /* synthetic */ ConnectivityService f$7;
                    private final /* synthetic */ LocationManagerService f$8;
                    private final /* synthetic */ CountryDetectorService f$9;

                    {
                        this.f$0 = r3;
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                        this.f$17 = r20;
                    }

                    public final void run() {
                        SystemServer systemServer = this.f$0;
                        Context context = this.f$1;
                        WindowManagerService windowManagerService = this.f$2;
                        NetworkManagementService networkManagementService = this.f$3;
                        NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                        IpSecService ipSecService = this.f$5;
                        NetworkStatsService networkStatsService = this.f$6;
                        ConnectivityService connectivityService = this.f$7;
                        LocationManagerService locationManagerService = this.f$8;
                        CountryDetectorService countryDetectorService = this.f$9;
                        NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                        CommonTimeManagementService commonTimeManagementService = this.f$11;
                        InputManagerService inputManagerService = this.f$12;
                        TelephonyRegistry telephonyRegistry = this.f$13;
                        MediaRouterService mediaRouterService = this.f$14;
                        MmsServiceBroker mmsServiceBroker = this.f$15;
                        boolean z = this.f$16;
                        boolean z2 = z;
                        SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                    }
                };
                activityManagerService2222222222222222222222222222222.systemReady(r02222222222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
            }
        } catch (RuntimeException e44) {
            e = e44;
            inputManager3 = null;
            HwCustEmergDataManager hwCustEmergDataManager13 = emergDataManager;
            storageManager3 = null;
            connectivity4 = null;
            tuiEnable2 = tuiEnable3;
            boolean z7 = z;
            i = 1;
            telephonyRegistry = null;
            vibrator2 = null;
            almService = null;
            Slog.e("System", "******************************************");
            Slog.e("System", "************ Failure starting core service", e);
            AlarmManagerService alarmManagerService222222222222222 = almService;
            wm = obj;
            inputManager = inputManager2;
            vibrator = vibrator2;
            IBinder iBinder7222222222222222 = null;
            ILockSettings lockSettings3222222222222222 = null;
            if (this.mFactoryTestMode != i) {
            }
            traceBeginAndSlog("MakeDisplayReady");
            wm.displayReady();
            traceEnd();
            traceBeginAndSlog("StartStorageManagerService");
            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
            storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
            traceEnd();
            traceBeginAndSlog("StartStorageStatsService");
            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
            traceEnd();
            IStorageManager iStorageManager222222222222222 = storageManager2;
            traceBeginAndSlog("StartUiModeManager");
            this.mSystemServiceManager.startService(UiModeManagerService.class);
            traceEnd();
            HwBootCheck.bootSceneEnd(101);
            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
            if (!this.mRuntimeRestart) {
            }
            HwBootCheck.bootSceneStart(104, 900000);
            if (!this.mOnlyCore) {
            }
            traceBeginAndSlog("PerformFstrimIfNeeded");
            this.mPackageManagerService.performFstrimIfNeeded();
            traceEnd();
            HwBootCheck.bootSceneEnd(104);
            HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
            if (this.mFactoryTestMode == 1) {
            }
            if (!isWatch) {
            }
            MediaProjectionManagerService.sHasStartedInSystemserver = true;
            if (isWatch) {
            }
            if (!disableSlices) {
            }
            if (!disableCameraService) {
            }
            if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
            }
            traceBeginAndSlog("StartStatsCompanionService");
            this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
            traceEnd();
            safeMode = wm.detectSafeMode();
            this.mSystemServiceManager.setSafeMode(safeMode);
            if (safeMode) {
            }
            traceBeginAndSlog("StartMmsService");
            MmsServiceBroker mmsService22222222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
            traceEnd();
            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
            }
            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
            }
            if (isStartSysSvcCallRecord) {
            }
            traceBeginAndSlog("MakeVibratorServiceReady");
            vibrator.systemReady();
            traceEnd();
            traceBeginAndSlog("MakeLockSettingsServiceReady");
            if (lockSettings2 != null) {
            }
            traceEnd();
            traceBeginAndSlog("StartBootPhaseLockSettingsReady");
            this.mSystemServiceManager.startBootPhase(480);
            traceEnd();
            traceBeginAndSlog("StartBootPhaseSystemServicesReady");
            this.mSystemServiceManager.startBootPhase(500);
            traceEnd();
            traceBeginAndSlog("MakeWindowManagerServiceReady");
            wm.systemReady();
            traceEnd();
            if (safeMode) {
            }
            Configuration config22222222222222222222222222222222 = wm.computeNewConfiguration(0);
            DisplayMetrics metrics22222222222222222222222222222222 = new DisplayMetrics();
            WindowManager w22222222222222222222222222222222 = (WindowManager) context.getSystemService("window");
            w22222222222222222222222222222222.getDefaultDisplay().getMetrics(metrics22222222222222222222222222222222);
            context.getResources().updateConfiguration(config22222222222222222222222222222222, metrics22222222222222222222222222222222);
            systemTheme = context.getTheme();
            if (systemTheme.getChangingConfigurations() != 0) {
            }
            traceBeginAndSlog("MakePowerManagerServiceReady");
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
            traceEnd();
            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
            traceBeginAndSlog("MakePackageManagerServiceReady");
            this.mPackageManagerService.systemReady();
            traceEnd();
            traceBeginAndSlog("MakeDisplayManagerServiceReady");
            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
            traceEnd();
            this.mSystemServiceManager.setSafeMode(safeMode);
            traceBeginAndSlog("StartDeviceSpecificServices");
            String[] classes22222222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
            length = classes22222222222222222222222222222222.length;
            ILockSettings lockSettings422222222222222222222222222222222 = lockSettings2;
            i2 = 0;
            WindowManagerService wm222222222222222222222222222222222 = wm;
            while (i2 < length) {
            }
            traceEnd();
            traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
            traceEnd();
            String[] strArr22222222222222222222222222222222 = classes22222222222222222222222222222222;
            VibratorService vibratorService322222222222222222222222222222222 = vibrator;
            Resources.Theme theme22222222222222222222222222222222 = systemTheme;
            WindowManager windowManager22222222222222222222222222222222 = w22222222222222222222222222222222;
            DisplayMetrics displayMetrics22222222222222222222222222222222 = metrics22222222222222222222222222222222;
            Configuration configuration22222222222222222222222222222222 = config22222222222222222222222222222222;
            boolean z222222222222222222222222222222222 = tuiEnable;
            LocationManagerService locationManagerService22222222222222222222222222222222 = location;
            ILockSettings iLockSettings222222222222222222222222222222222 = lockSettings422222222222222222222222222222222;
            ActivityManagerService activityManagerService22222222222222222222222222222222 = this.mActivityManagerService;
            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r022222222222222222222222222222222 = r12222222222222222222222222222222;
            $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r122222222222222222222222222222222 = new Runnable(this, context, wm222222222222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService22222222222222222222222222222222, enableIaware, safeMode) {
                private final /* synthetic */ SystemServer f$0;
                private final /* synthetic */ Context f$1;
                private final /* synthetic */ NetworkTimeUpdateService f$10;
                private final /* synthetic */ CommonTimeManagementService f$11;
                private final /* synthetic */ InputManagerService f$12;
                private final /* synthetic */ TelephonyRegistry f$13;
                private final /* synthetic */ MediaRouterService f$14;
                private final /* synthetic */ MmsServiceBroker f$15;
                private final /* synthetic */ boolean f$16;
                private final /* synthetic */ boolean f$17;
                private final /* synthetic */ WindowManagerService f$2;
                private final /* synthetic */ NetworkManagementService f$3;
                private final /* synthetic */ NetworkPolicyManagerService f$4;
                private final /* synthetic */ IpSecService f$5;
                private final /* synthetic */ NetworkStatsService f$6;
                private final /* synthetic */ ConnectivityService f$7;
                private final /* synthetic */ LocationManagerService f$8;
                private final /* synthetic */ CountryDetectorService f$9;

                {
                    this.f$0 = r3;
                    this.f$1 = r4;
                    this.f$2 = r5;
                    this.f$3 = r6;
                    this.f$4 = r7;
                    this.f$5 = r8;
                    this.f$6 = r9;
                    this.f$7 = r10;
                    this.f$8 = r11;
                    this.f$9 = r12;
                    this.f$10 = r13;
                    this.f$11 = r14;
                    this.f$12 = r15;
                    this.f$13 = r16;
                    this.f$14 = r17;
                    this.f$15 = r18;
                    this.f$16 = r19;
                    this.f$17 = r20;
                }

                public final void run() {
                    SystemServer systemServer = this.f$0;
                    Context context = this.f$1;
                    WindowManagerService windowManagerService = this.f$2;
                    NetworkManagementService networkManagementService = this.f$3;
                    NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                    IpSecService ipSecService = this.f$5;
                    NetworkStatsService networkStatsService = this.f$6;
                    ConnectivityService connectivityService = this.f$7;
                    LocationManagerService locationManagerService = this.f$8;
                    CountryDetectorService countryDetectorService = this.f$9;
                    NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                    CommonTimeManagementService commonTimeManagementService = this.f$11;
                    InputManagerService inputManagerService = this.f$12;
                    TelephonyRegistry telephonyRegistry = this.f$13;
                    MediaRouterService mediaRouterService = this.f$14;
                    MmsServiceBroker mmsServiceBroker = this.f$15;
                    boolean z = this.f$16;
                    boolean z2 = z;
                    SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
                }
            };
            activityManagerService22222222222222222222222222222222.systemReady(r022222222222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
        }
        IBinder iBinder72222222222222222 = null;
        ILockSettings lockSettings32222222222222222 = null;
        if (this.mFactoryTestMode != i) {
        }
        traceBeginAndSlog("MakeDisplayReady");
        try {
            wm.displayReady();
        } catch (Throwable e45) {
            Throwable th27 = e45;
            reportWtf("making display ready", e45);
        }
        traceEnd();
        traceBeginAndSlog("StartStorageManagerService");
        try {
            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
            storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        } catch (Throwable e46) {
            reportWtf("starting StorageManagerService", e46);
            storageManager2 = storageManager;
        }
        traceEnd();
        traceBeginAndSlog("StartStorageStatsService");
        try {
            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
        } catch (Throwable e47) {
            reportWtf("starting StorageStatsService", e47);
        }
        traceEnd();
        IStorageManager iStorageManager2222222222222222 = storageManager2;
        traceBeginAndSlog("StartUiModeManager");
        this.mSystemServiceManager.startService(UiModeManagerService.class);
        traceEnd();
        HwBootCheck.bootSceneEnd(101);
        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
        if (!this.mRuntimeRestart) {
        }
        HwBootCheck.bootSceneStart(104, 900000);
        if (!this.mOnlyCore) {
        }
        traceBeginAndSlog("PerformFstrimIfNeeded");
        try {
            this.mPackageManagerService.performFstrimIfNeeded();
        } catch (Throwable e48) {
            reportWtf("performing fstrim", e48);
        }
        traceEnd();
        HwBootCheck.bootSceneEnd(104);
        HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
        if (this.mFactoryTestMode == 1) {
        }
        if (!isWatch) {
        }
        MediaProjectionManagerService.sHasStartedInSystemserver = true;
        if (isWatch) {
        }
        if (!disableSlices) {
        }
        if (!disableCameraService) {
        }
        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
        }
        traceBeginAndSlog("StartStatsCompanionService");
        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
        traceEnd();
        safeMode = wm.detectSafeMode();
        this.mSystemServiceManager.setSafeMode(safeMode);
        if (safeMode) {
        }
        traceBeginAndSlog("StartMmsService");
        MmsServiceBroker mmsService222222222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
        traceEnd();
        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
        }
        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
        }
        if (isStartSysSvcCallRecord) {
        }
        traceBeginAndSlog("MakeVibratorServiceReady");
        try {
            vibrator.systemReady();
        } catch (Throwable e49) {
            Throwable th28 = e49;
            reportWtf("making Vibrator Service ready", e49);
        }
        traceEnd();
        traceBeginAndSlog("MakeLockSettingsServiceReady");
        if (lockSettings2 != null) {
        }
        traceEnd();
        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
        this.mSystemServiceManager.startBootPhase(480);
        traceEnd();
        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
        this.mSystemServiceManager.startBootPhase(500);
        traceEnd();
        traceBeginAndSlog("MakeWindowManagerServiceReady");
        try {
            wm.systemReady();
        } catch (Throwable e50) {
            Throwable th29 = e50;
            reportWtf("making Window Manager Service ready", e50);
        }
        traceEnd();
        if (safeMode) {
        }
        Configuration config222222222222222222222222222222222 = wm.computeNewConfiguration(0);
        DisplayMetrics metrics222222222222222222222222222222222 = new DisplayMetrics();
        WindowManager w222222222222222222222222222222222 = (WindowManager) context.getSystemService("window");
        w222222222222222222222222222222222.getDefaultDisplay().getMetrics(metrics222222222222222222222222222222222);
        context.getResources().updateConfiguration(config222222222222222222222222222222222, metrics222222222222222222222222222222222);
        systemTheme = context.getTheme();
        if (systemTheme.getChangingConfigurations() != 0) {
        }
        traceBeginAndSlog("MakePowerManagerServiceReady");
        try {
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
        } catch (Throwable e51) {
            reportWtf("making Power Manager Service ready", e51);
        }
        traceEnd();
        try {
            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
        } catch (Throwable e52) {
            reportWtf("making PG Manager Service ready", e52);
        }
        traceBeginAndSlog("MakePackageManagerServiceReady");
        this.mPackageManagerService.systemReady();
        traceEnd();
        traceBeginAndSlog("MakeDisplayManagerServiceReady");
        try {
            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
        } catch (Throwable e53) {
            reportWtf("making Display Manager Service ready", e53);
        }
        traceEnd();
        this.mSystemServiceManager.setSafeMode(safeMode);
        traceBeginAndSlog("StartDeviceSpecificServices");
        String[] classes222222222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
        length = classes222222222222222222222222222222222.length;
        ILockSettings lockSettings4222222222222222222222222222222222 = lockSettings2;
        i2 = 0;
        WindowManagerService wm2222222222222222222222222222222222 = wm;
        while (i2 < length) {
        }
        traceEnd();
        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
        traceEnd();
        String[] strArr222222222222222222222222222222222 = classes222222222222222222222222222222222;
        VibratorService vibratorService3222222222222222222222222222222222 = vibrator;
        Resources.Theme theme222222222222222222222222222222222 = systemTheme;
        WindowManager windowManager222222222222222222222222222222222 = w222222222222222222222222222222222;
        DisplayMetrics displayMetrics222222222222222222222222222222222 = metrics222222222222222222222222222222222;
        Configuration configuration222222222222222222222222222222222 = config222222222222222222222222222222222;
        boolean z2222222222222222222222222222222222 = tuiEnable;
        LocationManagerService locationManagerService222222222222222222222222222222222 = location;
        ILockSettings iLockSettings2222222222222222222222222222222222 = lockSettings4222222222222222222222222222222222;
        ActivityManagerService activityManagerService222222222222222222222222222222222 = this.mActivityManagerService;
        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r0222222222222222222222222222222222 = r122222222222222222222222222222222;
        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r1222222222222222222222222222222222 = new Runnable(this, context, wm2222222222222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService222222222222222222222222222222222, enableIaware, safeMode) {
            private final /* synthetic */ SystemServer f$0;
            private final /* synthetic */ Context f$1;
            private final /* synthetic */ NetworkTimeUpdateService f$10;
            private final /* synthetic */ CommonTimeManagementService f$11;
            private final /* synthetic */ InputManagerService f$12;
            private final /* synthetic */ TelephonyRegistry f$13;
            private final /* synthetic */ MediaRouterService f$14;
            private final /* synthetic */ MmsServiceBroker f$15;
            private final /* synthetic */ boolean f$16;
            private final /* synthetic */ boolean f$17;
            private final /* synthetic */ WindowManagerService f$2;
            private final /* synthetic */ NetworkManagementService f$3;
            private final /* synthetic */ NetworkPolicyManagerService f$4;
            private final /* synthetic */ IpSecService f$5;
            private final /* synthetic */ NetworkStatsService f$6;
            private final /* synthetic */ ConnectivityService f$7;
            private final /* synthetic */ LocationManagerService f$8;
            private final /* synthetic */ CountryDetectorService f$9;

            {
                this.f$0 = r3;
                this.f$1 = r4;
                this.f$2 = r5;
                this.f$3 = r6;
                this.f$4 = r7;
                this.f$5 = r8;
                this.f$6 = r9;
                this.f$7 = r10;
                this.f$8 = r11;
                this.f$9 = r12;
                this.f$10 = r13;
                this.f$11 = r14;
                this.f$12 = r15;
                this.f$13 = r16;
                this.f$14 = r17;
                this.f$15 = r18;
                this.f$16 = r19;
                this.f$17 = r20;
            }

            public final void run() {
                SystemServer systemServer = this.f$0;
                Context context = this.f$1;
                WindowManagerService windowManagerService = this.f$2;
                NetworkManagementService networkManagementService = this.f$3;
                NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                IpSecService ipSecService = this.f$5;
                NetworkStatsService networkStatsService = this.f$6;
                ConnectivityService connectivityService = this.f$7;
                LocationManagerService locationManagerService = this.f$8;
                CountryDetectorService countryDetectorService = this.f$9;
                NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                CommonTimeManagementService commonTimeManagementService = this.f$11;
                InputManagerService inputManagerService = this.f$12;
                TelephonyRegistry telephonyRegistry = this.f$13;
                MediaRouterService mediaRouterService = this.f$14;
                MmsServiceBroker mmsServiceBroker = this.f$15;
                boolean z = this.f$16;
                boolean z2 = z;
                SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
            }
        };
        activityManagerService222222222222222222222222222222222.systemReady(r0222222222222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
        telephonyRegistry = r8;
        storageManager = null;
        connectivity = null;
        vibratorService2 = vibratorService;
        almService = null;
        vibrator4 = vibratorService2;
        tuiEnable = tuiEnable3;
        i = 1;
        vibrator2 = vibrator4;
        Slog.e("System", "******************************************");
        Slog.e("System", "************ Failure starting core service", e);
        AlarmManagerService alarmManagerService2222222222222222 = almService;
        wm = obj;
        inputManager = inputManager2;
        vibrator = vibrator2;
        IBinder iBinder722222222222222222 = null;
        ILockSettings lockSettings322222222222222222 = null;
        if (this.mFactoryTestMode != i) {
        }
        traceBeginAndSlog("MakeDisplayReady");
        wm.displayReady();
        traceEnd();
        traceBeginAndSlog("StartStorageManagerService");
        this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
        storageManager2 = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        traceEnd();
        traceBeginAndSlog("StartStorageStatsService");
        this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
        traceEnd();
        IStorageManager iStorageManager22222222222222222 = storageManager2;
        traceBeginAndSlog("StartUiModeManager");
        this.mSystemServiceManager.startService(UiModeManagerService.class);
        traceEnd();
        HwBootCheck.bootSceneEnd(101);
        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
        if (!this.mRuntimeRestart) {
        }
        HwBootCheck.bootSceneStart(104, 900000);
        if (!this.mOnlyCore) {
        }
        traceBeginAndSlog("PerformFstrimIfNeeded");
        this.mPackageManagerService.performFstrimIfNeeded();
        traceEnd();
        HwBootCheck.bootSceneEnd(104);
        HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
        if (this.mFactoryTestMode == 1) {
        }
        if (!isWatch) {
        }
        MediaProjectionManagerService.sHasStartedInSystemserver = true;
        if (isWatch) {
        }
        if (!disableSlices) {
        }
        if (!disableCameraService) {
        }
        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
        }
        traceBeginAndSlog("StartStatsCompanionService");
        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
        traceEnd();
        safeMode = wm.detectSafeMode();
        this.mSystemServiceManager.setSafeMode(safeMode);
        if (safeMode) {
        }
        traceBeginAndSlog("StartMmsService");
        MmsServiceBroker mmsService2222222222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
        traceEnd();
        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
        }
        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
        }
        if (isStartSysSvcCallRecord) {
        }
        traceBeginAndSlog("MakeVibratorServiceReady");
        vibrator.systemReady();
        traceEnd();
        traceBeginAndSlog("MakeLockSettingsServiceReady");
        if (lockSettings2 != null) {
        }
        traceEnd();
        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
        this.mSystemServiceManager.startBootPhase(480);
        traceEnd();
        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
        this.mSystemServiceManager.startBootPhase(500);
        traceEnd();
        traceBeginAndSlog("MakeWindowManagerServiceReady");
        wm.systemReady();
        traceEnd();
        if (safeMode) {
        }
        Configuration config2222222222222222222222222222222222 = wm.computeNewConfiguration(0);
        DisplayMetrics metrics2222222222222222222222222222222222 = new DisplayMetrics();
        WindowManager w2222222222222222222222222222222222 = (WindowManager) context.getSystemService("window");
        w2222222222222222222222222222222222.getDefaultDisplay().getMetrics(metrics2222222222222222222222222222222222);
        context.getResources().updateConfiguration(config2222222222222222222222222222222222, metrics2222222222222222222222222222222222);
        systemTheme = context.getTheme();
        if (systemTheme.getChangingConfigurations() != 0) {
        }
        traceBeginAndSlog("MakePowerManagerServiceReady");
        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
        traceEnd();
        this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
        traceBeginAndSlog("MakePackageManagerServiceReady");
        this.mPackageManagerService.systemReady();
        traceEnd();
        traceBeginAndSlog("MakeDisplayManagerServiceReady");
        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
        traceEnd();
        this.mSystemServiceManager.setSafeMode(safeMode);
        traceBeginAndSlog("StartDeviceSpecificServices");
        String[] classes2222222222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236002);
        length = classes2222222222222222222222222222222222.length;
        ILockSettings lockSettings42222222222222222222222222222222222 = lockSettings2;
        i2 = 0;
        WindowManagerService wm22222222222222222222222222222222222 = wm;
        while (i2 < length) {
        }
        traceEnd();
        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
        traceEnd();
        String[] strArr2222222222222222222222222222222222 = classes2222222222222222222222222222222222;
        VibratorService vibratorService32222222222222222222222222222222222 = vibrator;
        Resources.Theme theme2222222222222222222222222222222222 = systemTheme;
        WindowManager windowManager2222222222222222222222222222222222 = w2222222222222222222222222222222222;
        DisplayMetrics displayMetrics2222222222222222222222222222222222 = metrics2222222222222222222222222222222222;
        Configuration configuration2222222222222222222222222222222222 = config2222222222222222222222222222222222;
        boolean z22222222222222222222222222222222222 = tuiEnable;
        LocationManagerService locationManagerService2222222222222222222222222222222222 = location;
        ILockSettings iLockSettings22222222222222222222222222222222222 = lockSettings42222222222222222222222222222222222;
        ActivityManagerService activityManagerService2222222222222222222222222222222222 = this.mActivityManagerService;
        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r02222222222222222222222222222222222 = r1222222222222222222222222222222222;
        $$Lambda$SystemServer$DxV6Zs68yyqhXzk9mQPyjjvrF8 r12222222222222222222222222222222222 = new Runnable(this, context, wm22222222222222222222222222222222222, iNetworkManagementService, lockSettings, ipSecServiceF, iNetworkStatsService, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService2222222222222222222222222222222222, enableIaware, safeMode) {
            private final /* synthetic */ SystemServer f$0;
            private final /* synthetic */ Context f$1;
            private final /* synthetic */ NetworkTimeUpdateService f$10;
            private final /* synthetic */ CommonTimeManagementService f$11;
            private final /* synthetic */ InputManagerService f$12;
            private final /* synthetic */ TelephonyRegistry f$13;
            private final /* synthetic */ MediaRouterService f$14;
            private final /* synthetic */ MmsServiceBroker f$15;
            private final /* synthetic */ boolean f$16;
            private final /* synthetic */ boolean f$17;
            private final /* synthetic */ WindowManagerService f$2;
            private final /* synthetic */ NetworkManagementService f$3;
            private final /* synthetic */ NetworkPolicyManagerService f$4;
            private final /* synthetic */ IpSecService f$5;
            private final /* synthetic */ NetworkStatsService f$6;
            private final /* synthetic */ ConnectivityService f$7;
            private final /* synthetic */ LocationManagerService f$8;
            private final /* synthetic */ CountryDetectorService f$9;

            {
                this.f$0 = r3;
                this.f$1 = r4;
                this.f$2 = r5;
                this.f$3 = r6;
                this.f$4 = r7;
                this.f$5 = r8;
                this.f$6 = r9;
                this.f$7 = r10;
                this.f$8 = r11;
                this.f$9 = r12;
                this.f$10 = r13;
                this.f$11 = r14;
                this.f$12 = r15;
                this.f$13 = r16;
                this.f$14 = r17;
                this.f$15 = r18;
                this.f$16 = r19;
                this.f$17 = r20;
            }

            public final void run() {
                SystemServer systemServer = this.f$0;
                Context context = this.f$1;
                WindowManagerService windowManagerService = this.f$2;
                NetworkManagementService networkManagementService = this.f$3;
                NetworkPolicyManagerService networkPolicyManagerService = this.f$4;
                IpSecService ipSecService = this.f$5;
                NetworkStatsService networkStatsService = this.f$6;
                ConnectivityService connectivityService = this.f$7;
                LocationManagerService locationManagerService = this.f$8;
                CountryDetectorService countryDetectorService = this.f$9;
                NetworkTimeUpdateService networkTimeUpdateService = this.f$10;
                CommonTimeManagementService commonTimeManagementService = this.f$11;
                InputManagerService inputManagerService = this.f$12;
                TelephonyRegistry telephonyRegistry = this.f$13;
                MediaRouterService mediaRouterService = this.f$14;
                MmsServiceBroker mmsServiceBroker = this.f$15;
                boolean z = this.f$16;
                boolean z2 = z;
                SystemServer.lambda$startOtherServices$5(systemServer, context, windowManagerService, networkManagementService, networkPolicyManagerService, ipSecService, networkStatsService, connectivityService, locationManagerService, countryDetectorService, networkTimeUpdateService, commonTimeManagementService, inputManagerService, telephonyRegistry, mediaRouterService, mmsServiceBroker, z2, this.f$17);
            }
        };
        activityManagerService2222222222222222222222222222222222.systemReady(r02222222222222222222222222222222222, BOOT_TIMINGS_TRACE_LOG);
    }

    static /* synthetic */ void lambda$startOtherServices$1() {
        try {
            Slog.i(TAG, "SecondaryZygotePreload");
            TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
            traceLog.traceBegin("SecondaryZygotePreload");
            if (!Process.zygoteProcess.preloadDefault(Build.SUPPORTED_32_BIT_ABIS[0])) {
                Slog.e(TAG, "Unable to preload default resources");
            }
            traceLog.traceEnd();
        } catch (Exception ex) {
            Slog.e(TAG, "Exception preloading default resources", ex);
        }
    }

    static /* synthetic */ void lambda$startOtherServices$3() {
        TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin(START_HIDL_SERVICES);
        startHidlServices();
        traceLog.traceEnd();
    }

    public static /* synthetic */ void lambda$startOtherServices$5(SystemServer systemServer, Context context, WindowManagerService windowManagerF, NetworkManagementService networkManagementF, NetworkPolicyManagerService networkPolicyF, IpSecService ipSecServiceF, NetworkStatsService networkStatsF, ConnectivityService connectivityF, LocationManagerService locationF, CountryDetectorService countryDetectorF, NetworkTimeUpdateService networkTimeUpdaterF, CommonTimeManagementService commonTimeMgmtServiceF, InputManagerService inputManagerF, TelephonyRegistry telephonyRegistryF, MediaRouterService mediaRouterF, MmsServiceBroker mmsServiceF, boolean enableIaware, boolean safeMode) {
        SystemServer systemServer2 = systemServer;
        Context context2 = context;
        NetworkPolicyManagerService networkPolicyManagerService = networkPolicyF;
        Slog.i(TAG, "Making services ready");
        traceBeginAndSlog("StartActivityManagerReadyPhase");
        systemServer2.mSystemServiceManager.startBootPhase(550);
        traceEnd();
        traceBeginAndSlog("StartObservingNativeCrashes");
        try {
            systemServer2.mActivityManagerService.startObservingNativeCrashes();
        } catch (Throwable e) {
            systemServer2.reportWtf("observing native crashes", e);
        }
        traceEnd();
        Future<?> webviewPrep = null;
        if (!systemServer2.mOnlyCore && systemServer2.mWebViewUpdateService != null) {
            webviewPrep = SystemServerInitThreadPool.get().submit(new Runnable() {
                public final void run() {
                    SystemServer.lambda$startOtherServices$4(SystemServer.this);
                }
            }, "WebViewFactoryPreparation");
        }
        Future<?> webviewPrep2 = webviewPrep;
        if (systemServer2.mPackageManager.hasSystemFeature("android.hardware.type.automotive")) {
            traceBeginAndSlog("StartCarServiceHelperService");
            systemServer2.mSystemServiceManager.startService(CAR_SERVICE_HELPER_SERVICE_CLASS);
            traceEnd();
        }
        traceBeginAndSlog("StartSystemUI");
        try {
            startSystemUi(context, windowManagerF);
        } catch (Throwable e2) {
            Throwable th = e2;
            systemServer2.reportWtf("starting System UI", e2);
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkManagementServiceReady");
        if (networkManagementF != null) {
            try {
                networkManagementF.systemReady();
            } catch (Throwable e3) {
                Throwable th2 = e3;
                systemServer2.reportWtf("making Network Managment Service ready", e3);
            }
        }
        CountDownLatch networkPolicyInitReadySignal = null;
        if (networkPolicyManagerService != null) {
            networkPolicyInitReadySignal = networkPolicyF.networkScoreAndNetworkManagementServiceReady();
        }
        CountDownLatch networkPolicyInitReadySignal2 = networkPolicyInitReadySignal;
        traceEnd();
        traceBeginAndSlog("MakeIpSecServiceReady");
        if (ipSecServiceF != null) {
            try {
                ipSecServiceF.systemReady();
            } catch (Throwable e4) {
                Throwable th3 = e4;
                systemServer2.reportWtf("making IpSec Service ready", e4);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkStatsServiceReady");
        if (networkStatsF != null) {
            try {
                networkStatsF.systemReady();
            } catch (Throwable e5) {
                Throwable th4 = e5;
                systemServer2.reportWtf("making Network Stats Service ready", e5);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeConnectivityServiceReady");
        if (connectivityF != null) {
            try {
                connectivityF.systemReady();
            } catch (Throwable e6) {
                Throwable th5 = e6;
                systemServer2.reportWtf("making Connectivity Service ready", e6);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkPolicyServiceReady");
        if (networkPolicyManagerService != null) {
            try {
                networkPolicyManagerService.systemReady(networkPolicyInitReadySignal2);
            } catch (Throwable e7) {
                Throwable th6 = e7;
                systemServer2.reportWtf("making Network Policy Service ready", e7);
            }
        }
        traceEnd();
        traceBeginAndSlog("StartWatchdog");
        Watchdog.getInstance().start();
        traceEnd();
        systemServer2.mPackageManagerService.waitForAppDataPrepared();
        traceBeginAndSlog("PhaseThirdPartyAppsCanStart");
        if (webviewPrep2 != null) {
            ConcurrentUtils.waitForFutureNoInterrupt(webviewPrep2, "WebViewFactoryPreparation");
        }
        systemServer2.mSystemServiceManager.startBootPhase(600);
        traceEnd();
        traceBeginAndSlog("MakeLocationServiceReady");
        if (locationF != null) {
            try {
                locationF.systemRunning();
            } catch (Throwable e8) {
                Throwable th7 = e8;
                systemServer2.reportWtf("Notifying Location Service running", e8);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeCountryDetectionServiceReady");
        if (countryDetectorF != null) {
            try {
                countryDetectorF.systemRunning();
            } catch (Throwable e9) {
                Throwable th8 = e9;
                systemServer2.reportWtf("Notifying CountryDetectorService running", e9);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkTimeUpdateReady");
        if (networkTimeUpdaterF != null) {
            try {
                networkTimeUpdaterF.systemRunning();
            } catch (Throwable e10) {
                Throwable th9 = e10;
                systemServer2.reportWtf("Notifying NetworkTimeService running", e10);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeCommonTimeManagementServiceReady");
        if (commonTimeMgmtServiceF != null) {
            try {
                commonTimeMgmtServiceF.systemRunning();
            } catch (Throwable e11) {
                Throwable th10 = e11;
                systemServer2.reportWtf("Notifying CommonTimeManagementService running", e11);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeInputManagerServiceReady");
        if (inputManagerF != null) {
            try {
                inputManagerF.systemRunning();
            } catch (Throwable e12) {
                Throwable th11 = e12;
                systemServer2.reportWtf("Notifying InputManagerService running", e12);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeTelephonyRegistryReady");
        if (telephonyRegistryF != null) {
            try {
                telephonyRegistryF.systemRunning();
            } catch (Throwable e13) {
                Throwable th12 = e13;
                systemServer2.reportWtf("Notifying TelephonyRegistry running", e13);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeMediaRouterServiceReady");
        if (mediaRouterF != null) {
            try {
                mediaRouterF.systemRunning();
            } catch (Throwable e14) {
                Throwable th13 = e14;
                systemServer2.reportWtf("Notifying MediaRouterService running", e14);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeMmsServiceReady");
        if (mmsServiceF != null) {
            try {
                mmsServiceF.systemRunning();
            } catch (Throwable e15) {
                Throwable th14 = e15;
                systemServer2.reportWtf("Notifying MmsService running", e15);
            }
        }
        traceEnd();
        traceBeginAndSlog("IncidentDaemonReady");
        try {
            IIncidentManager incident = IIncidentManager.Stub.asInterface(ServiceManager.getService("incident"));
            if (incident != null) {
                incident.systemRunning();
            }
        } catch (Throwable e16) {
            systemServer2.reportWtf("Notifying incident daemon running", e16);
        }
        traceEnd();
        if (enableIaware) {
            try {
                Object obj = "WebViewFactoryPreparation";
                ServiceManager.addService("multi_task", HwServiceFactory.getMultiTaskManagerService().getInstance(context2));
            } catch (Throwable th15) {
                e = th15;
                systemServer2.reportWtf("starting MultiTaskManagerService", e);
                traceBeginAndSlog("StartPCManagerService");
                HwServiceFactory.addHwPCManagerService(context2, systemServer2.mActivityManagerService);
                traceEnd();
                HwServiceFactory.addHwFmService(context);
                HwServiceFactory.updateLocalesWhenOTAEX(context2, systemServer2.mPackageManagerService.getSdkVersion());
            }
        } else {
            String WEBVIEW_PREPARATION = "WebViewFactoryPreparation";
            Slog.e(TAG, "can not start multitask because the prop is false");
        }
        if (HwPCUtils.enabled() && !safeMode) {
            traceBeginAndSlog("StartPCManagerService");
            HwServiceFactory.addHwPCManagerService(context2, systemServer2.mActivityManagerService);
            traceEnd();
        }
        HwServiceFactory.addHwFmService(context);
        HwServiceFactory.updateLocalesWhenOTAEX(context2, systemServer2.mPackageManagerService.getSdkVersion());
    }

    public static /* synthetic */ void lambda$startOtherServices$4(SystemServer systemServer) {
        Slog.i(TAG, "WebViewFactoryPreparation");
        TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin("WebViewFactoryPreparation");
        ConcurrentUtils.waitForFutureNoInterrupt(systemServer.mZygotePreload, "Zygote preload");
        systemServer.mZygotePreload = null;
        systemServer.mWebViewUpdateService.prepareWebViewInSystemServer();
        traceLog.traceEnd();
    }

    static final void startSystemUi(Context context, WindowManagerService windowManager) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.SystemUIService"));
        intent.addFlags(256);
        context.startServiceAsUser(intent, UserHandle.SYSTEM);
        windowManager.onSystemUiStarted();
    }

    private static void traceBeginAndSlog(String name) {
        Slog.i(TAG, name);
        BOOT_TIMINGS_TRACE_LOG.traceBegin(name);
    }

    private static void traceEnd() {
        BOOT_TIMINGS_TRACE_LOG.traceEnd();
    }

    private void processMultiDPI(WindowManagerService wm) {
        int dpi = SystemProperties.getInt("persist.sys.dpi", 0);
        if (SystemProperties.getInt("persist.sys.rog.width", 0) > 0) {
            dpi = SystemProperties.getInt("persist.sys.realdpi", SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0))));
        }
        if (dpi > 0) {
            wm.setForcedDisplayDensityForUser(0, dpi, UserHandle.myUserId());
        }
    }

    private void initRogMode(WindowManagerService wm, Context context) {
        if (wm != null && !SystemProperties.get("ro.runmode", "normal").equals("factory") && (SystemProperties.getInt("persist.sys.aps.firstboot", 1) > 0 || SystemProperties.getInt("persist.sys.rog.width", 0) == 0)) {
            int initWidth = SystemProperties.getInt("sys.rog.width", 0);
            int initHeight = SystemProperties.getInt("sys.rog.height", 0);
            int initDensity = SystemProperties.getInt("sys.rog.density", 0);
            if (!(initWidth == 0 || initHeight == 0 || initDensity == 0)) {
                int density = getRealDpiBasedRog(initDensity);
                SystemProperties.set("persist.sys.realdpi", Integer.toString(density));
                SystemProperties.set("persist.sys.rog.width", Integer.toString(initWidth));
                SystemProperties.set("persist.sys.rog.height", Integer.toString(initHeight));
                ContentResolver contentResolver = context.getContentResolver();
                Settings.Global.putString(contentResolver, "display_size_forced", initWidth + "," + initHeight);
                wm.setForcedDisplaySize(0, initWidth, initHeight);
                wm.setForcedDisplayDensityForUser(0, density, UserHandle.myUserId());
                SystemProperties.set("persist.sys.rog.configmode", ENCRYPTED_STATE);
                Slog.d(TAG, "initRogMode and setForcedDisplaySize, initWidth = " + initWidth + "; initHeight = " + initHeight + "; density = " + density);
                Settings.Global.putInt(context.getContentResolver(), "aps_display_resolution", 2);
                Settings.Global.putInt(context.getContentResolver(), "low_resolution_switch", 1);
            }
        }
    }

    private int getRealDpiBasedRog(int rogDpi) {
        int originLcdDpi = SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
        return (SystemProperties.getInt("persist.sys.dpi", originLcdDpi) * rogDpi) / originLcdDpi;
    }

    private void restoreRogMode(WindowManagerService wm, Context context) {
        if (wm != null) {
            if (SystemProperties.getInt("persist.sys.rog.configmode", 0) == 1) {
                SystemProperties.set("persist.sys.realdpi", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                SystemProperties.set("persist.sys.rog.width", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                SystemProperties.set("persist.sys.rog.height", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                Settings.Global.putString(context.getContentResolver(), "display_size_forced", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                wm.setForcedDisplayDensityForUser(0, SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0)), UserHandle.myUserId());
                SystemProperties.set("persist.sys.rog.configmode", "0");
            } else if (SystemProperties.getInt("persist.sys.rog.width", 0) != 0) {
                SystemProperties.set("persist.sys.rog.width", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                SystemProperties.set("persist.sys.rog.height", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            }
        }
    }

    /* JADX WARNING: type inference failed for: r3v1, types: [com.android.server.forcerotation.HwForceRotationManagerService, android.os.IBinder] */
    private void startForceRotation(Context context) {
        if (HwFrameworkFactory.getForceRotationManager().isForceRotationSupported()) {
            try {
                Slog.i(TAG, "Force rotation Service, name = forceRotationService");
                HwServiceFactory.IHwForceRotationManagerServiceWrapper ifrsw = HwServiceFactory.getForceRotationManagerServiceWrapper();
                if (ifrsw != null) {
                    ServiceManager.addService("forceRotationService", ifrsw.getServiceInstance(context, UiThread.getHandler()));
                }
            } catch (Throwable e) {
                reportWtf("starting Force rotation service", e);
            }
        }
    }
}
