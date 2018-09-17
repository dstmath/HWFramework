package com.android.server;

import android.app.ActivityThread;
import android.app.INotificationManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.hsm.HwSystemManager;
import android.os.BaseBundle;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.FactoryTest;
import android.os.FileUtils;
import android.os.IIncidentManager;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.provider.Settings.Global;
import android.util.BootTimingsTraceLog;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Slog;
import android.view.WindowManager;
import android.vr.VRManagerService;
import com.android.internal.app.NightDisplayController;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BinderInternal;
import com.android.internal.os.SamplingProfilerIntegration;
import com.android.internal.util.ConcurrentUtils;
import com.android.internal.widget.ILockSettings;
import com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass4;
import com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass5;
import com.android.server.HwServiceFactory.IHwAttestationServiceFactory;
import com.android.server.HwServiceFactory.IHwFingerprintService;
import com.android.server.HwServiceFactory.IHwForceRotationManagerServiceWrapper;
import com.android.server.HwServiceFactory.IHwLocationManagerService;
import com.android.server.HwServiceFactory.IHwTelephonyRegistry;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityManagerService.Lifecycle;
import com.android.server.audio.AudioService;
import com.android.server.camera.CameraServiceProxy;
import com.android.server.clipboard.ClipboardService;
import com.android.server.connectivity.IpConnectivityMetrics;
import com.android.server.coverage.CoverageService;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import com.android.server.display.DisplayManagerService;
import com.android.server.display.NightDisplayService;
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
import com.android.server.media.dtv.DTVService;
import com.android.server.media.projection.MediaProjectionManagerService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.om.OverlayManagerService;
import com.android.server.os.DeviceIdentifiersPolicyService;
import com.android.server.os.HwBootCheck;
import com.android.server.os.HwBootFail;
import com.android.server.os.SchedulingPolicyService;
import com.android.server.pg.PGManagerService;
import com.android.server.pm.BackgroundDexOptService;
import com.android.server.pm.Installer;
import com.android.server.pm.LauncherAppsService;
import com.android.server.pm.OtaDexoptService;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.ShortcutService;
import com.android.server.pm.UserManagerService.LifeCycle;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.power.PowerManagerService;
import com.android.server.power.ShutdownThread;
import com.android.server.restrictions.RestrictionsManagerService;
import com.android.server.retaildemo.RetailDemoModeService;
import com.android.server.security.KeyAttestationApplicationIdProviderService;
import com.android.server.security.KeyChainSystemService;
import com.android.server.soundtrigger.SoundTriggerService;
import com.android.server.telecom.TelecomLoaderService;
import com.android.server.trust.TrustManagerService;
import com.android.server.tv.TvInputManagerService;
import com.android.server.tv.TvRemoteService;
import com.android.server.twilight.TwilightService;
import com.android.server.usage.UsageStatsService;
import com.android.server.utils.LogBufferUtil;
import com.android.server.vr.VrManagerService;
import com.android.server.webkit.WebViewUpdateService;
import com.android.server.wm.WindowManagerService;
import dalvik.system.VMRuntime;
import huawei.android.app.HwCustEmergDataManager;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public final class SystemServer {
    private static final String ACCOUNT_SERVICE_CLASS = "com.android.server.accounts.AccountManagerService$Lifecycle";
    private static final String APPWIDGET_SERVICE_CLASS = "com.android.server.appwidget.AppWidgetService";
    private static final String AUTO_FILL_MANAGER_SERVICE_CLASS = "com.android.server.autofill.AutofillManagerService";
    private static final String BACKUP_MANAGER_SERVICE_CLASS = "com.android.server.backup.BackupManagerService$Lifecycle";
    private static final String BLOCK_MAP_FILE = "/cache/recovery/block.map";
    private static final BootTimingsTraceLog BOOT_TIMINGS_TRACE_LOG = new BootTimingsTraceLog(SYSTEM_SERVER_TIMING_TAG, 524288);
    private static final String COMPANION_DEVICE_MANAGER_SERVICE_CLASS = "com.android.server.companion.CompanionDeviceManagerService";
    private static final String CONTENT_SERVICE_CLASS = "com.android.server.content.ContentService$Lifecycle";
    private static final int DEFAULT_SYSTEM_THEME = 16974792;
    private static final long EARLIEST_SUPPORTED_TIME = 86400000;
    private static final String ENCRYPTED_STATE = "1";
    private static final String ENCRYPTING_STATE = "trigger_restart_min_framework";
    private static final String ETHERNET_SERVICE_CLASS = "com.android.server.ethernet.EthernetService";
    private static final String JOB_SCHEDULER_SERVICE_CLASS = "com.android.server.job.JobSchedulerService";
    private static final boolean LOCAL_LOGV = true;
    private static final String LOCK_SETTINGS_SERVICE_CLASS = "com.android.server.LockSettingsService$Lifecycle";
    private static final String MIDI_SERVICE_CLASS = "com.android.server.midi.MidiService$Lifecycle";
    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";
    private static final String PRINT_MANAGER_SERVICE_CLASS = "com.android.server.print.PrintManagerService";
    private static final String SEARCH_MANAGER_SERVICE_CLASS = "com.android.server.search.SearchManagerService$Lifecycle";
    private static final long SNAPSHOT_INTERVAL = 3600000;
    private static final String START_HIDL_SERVICES = "StartHidlServices";
    private static final String START_SENSOR_SERVICE = "StartSensorService";
    private static final String STORAGE_MANAGER_SERVICE_CLASS = "com.android.server.StorageManagerService$Lifecycle";
    private static final String STORAGE_STATS_SERVICE_CLASS = "com.android.server.usage.StorageStatsService$Lifecycle";
    private static final String SYSTEM_SERVER_TIMING_ASYNC_TAG = "SystemServerTimingAsync";
    private static final String SYSTEM_SERVER_TIMING_TAG = "SystemServerTiming";
    private static final String TAG = "SystemServer";
    private static final String THERMAL_OBSERVER_CLASS = "com.google.android.clockwork.ThermalObserver";
    private static final String UNCRYPT_PACKAGE_FILE = "/cache/recovery/uncrypt_file";
    private static final String USB_SERVICE_CLASS = "com.android.server.usb.UsbService$Lifecycle";
    private static final String VOICE_RECOGNITION_MANAGER_SERVICE_CLASS = "com.android.server.voiceinteraction.VoiceInteractionManagerService";
    private static final String WEAR_CONNECTIVITY_SERVICE_CLASS = "com.google.android.clockwork.connectivity.WearConnectivityService";
    private static final String WEAR_DISPLAY_SERVICE_CLASS = "com.google.android.clockwork.display.WearDisplayService";
    private static final String WEAR_TIME_SERVICE_CLASS = "com.google.android.clockwork.time.WearTimeService";
    private static final String WIFI_AWARE_SERVICE_CLASS = "com.android.server.wifi.aware.WifiAwareService";
    private static final String WIFI_P2P_SERVICE_CLASS = "com.android.server.wifi.p2p.WifiP2pService";
    private static final String WIFI_SERVICE_CLASS = "com.android.server.wifi.WifiService";
    private static final int sMaxBinderThreads = 31;
    final Thread fingerprintStartThread = new Thread(new Runnable() {
        public void run() {
            Slog.i(SystemServer.TAG, "start Finger Print Service async");
            Class serviceClass = null;
            try {
                IHwFingerprintService ifs = HwServiceFactory.getHwFingerprintService();
                if (ifs != null) {
                    serviceClass = ifs.createServiceClass();
                }
                if (serviceClass != null) {
                    SystemServer.this.mSystemServiceManager.startService(serviceClass);
                } else {
                    SystemServer.this.mSystemServiceManager.startService(FingerprintService.class);
                }
                Slog.i(SystemServer.TAG, "FingerPrintService ready");
            } catch (Throwable e) {
                Slog.e(SystemServer.TAG, "Start fingerprintservice error", e);
            }
        }
    });
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
    private Future<?> mSensorServiceStart;
    private Context mSystemContext;
    private SystemServiceManager mSystemServiceManager;
    private WebViewUpdateService mWebViewUpdateService;
    private Future<?> mZygotePreload;

    private static native void startHidlServices();

    private static native void startSensorService();

    public static void main(String[] args) {
        new SystemServer().run();
    }

    public SystemServer() {
        if (this.mFactoryTestMode != 0) {
            Jlog.d(26, "JL_FIRST_BOOT");
        }
        this.mRuntimeRestart = ENCRYPTED_STATE.equals(SystemProperties.get("sys.boot_completed"));
    }

    private void run() {
        try {
            traceBeginAndSlog("InitBeforeStartServices");
            if (System.currentTimeMillis() < 86400000) {
                Slog.w(TAG, "System clock is before 1970; setting to 1970.");
                SystemClock.setCurrentTimeMillis(86400000);
            }
            if (!SystemProperties.get("persist.sys.language").isEmpty()) {
                SystemProperties.set("persist.sys.locale", Locale.getDefault().toLanguageTag());
                SystemProperties.set("persist.sys.language", "");
                SystemProperties.set("persist.sys.country", "");
                SystemProperties.set("persist.sys.localevar", "");
            }
            Binder.setWarnOnBlocking(true);
            Slog.i(TAG, "Entered the Android system server!");
            int uptimeMillis = (int) SystemClock.elapsedRealtime();
            EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_SYSTEM_RUN, uptimeMillis);
            if (!this.mRuntimeRestart) {
                MetricsLogger.histogram(null, "boot_system_server_init", uptimeMillis);
                Jlog.d(30, "JL_BOOT_PROGRESS_SYSTEM_RUN");
            }
            SystemProperties.set("persist.sys.dalvik.vm.lib.2", VMRuntime.getRuntime().vmLibrary());
            if (SamplingProfilerIntegration.isEnabled()) {
                SamplingProfilerIntegration.start();
                this.mProfilerSnapshotTimer = new Timer();
                this.mProfilerSnapshotTimer.schedule(new TimerTask() {
                    public void run() {
                        SamplingProfilerIntegration.writeSnapshot("system_server", null);
                    }
                }, SNAPSHOT_INTERVAL, SNAPSHOT_INTERVAL);
            }
            VMRuntime.getRuntime().clearGrowthLimit();
            VMRuntime.getRuntime().setTargetHeapUtilization(0.8f);
            Build.ensureFingerprintProperty();
            Environment.setUserRequired(true);
            BaseBundle.setShouldDefuse(true);
            BinderInternal.disableBackgroundScheduling(true);
            BinderInternal.setMaxThreads(31);
            Process.setThreadPriority(-2);
            Process.setCanSelfBackground(false);
            Looper.prepareMainLooper();
            System.loadLibrary("android_servers");
            performPendingShutdown();
            createSystemContext();
            this.mSystemServiceManager = new SystemServiceManager(this.mSystemContext);
            this.mSystemServiceManager.setRuntimeRestarted(this.mRuntimeRestart);
            LocalServices.addService(SystemServiceManager.class, this.mSystemServiceManager);
            SystemServerInitThreadPool.get();
            try {
                traceBeginAndSlog("StartServices");
                startBootstrapServices();
                startCoreServices();
                startOtherServices();
                SystemServerInitThreadPool.shutdown();
                traceEnd();
                if (StrictMode.conditionallyEnableDebugLogging()) {
                    Slog.i(TAG, "Enabled StrictMode for system server main thread.");
                }
                if (!(this.mRuntimeRestart || (isFirstBootOrUpgrade() ^ 1) == 0)) {
                    uptimeMillis = (int) SystemClock.elapsedRealtime();
                    MetricsLogger.histogram(null, "boot_system_server_ready", uptimeMillis);
                    if (uptimeMillis > 60000) {
                        Slog.wtf(SYSTEM_SERVER_TIMING_TAG, "SystemServer init took too long. uptimeMillis=" + uptimeMillis);
                    }
                }
                LogBufferUtil.closeLogBufferAsNeed(this.mSystemContext);
                SmartShrinker.reclaim(Process.myPid(), 3);
                Looper.loop();
                throw new RuntimeException("Main thread loop unexpectedly exited");
            } catch (Throwable th) {
                traceEnd();
            }
        } finally {
            traceEnd();
        }
    }

    private boolean isFirstBootOrUpgrade() {
        return !this.mPackageManagerService.isFirstBoot() ? this.mPackageManagerService.isUpgrade() : true;
    }

    private void reportWtf(String msg, Throwable e) {
        Slog.w(TAG, "***********************************************");
        Slog.wtf(TAG, "BOOT FAILURE " + msg, e);
    }

    private void performPendingShutdown() {
        String shutdownAction = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY, "");
        if (shutdownAction != null && shutdownAction.length() > 0) {
            String reason;
            boolean reboot = shutdownAction.charAt(0) == '1';
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
                    if (!(filename == null || !filename.startsWith("/data") || new File(BLOCK_MAP_FILE).exists())) {
                        Slog.e(TAG, "Can't find block map file, uncrypt failed or unexpected runtime restart?");
                        return;
                    }
                }
            }
            ShutdownThread.rebootOrShutdown(null, reboot, reason);
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
        String TAG_SYSTEM_CONFIG = "ReadingSystemConfig";
        traceBeginAndSlog("ReadingSystemConfig");
        SystemServerInitThreadPool.get().submit(new -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo(), "ReadingSystemConfig");
        traceEnd();
        traceBeginAndSlog("StartInstaller");
        Installer installer = (Installer) this.mSystemServiceManager.startService(Installer.class);
        traceEnd();
        traceBeginAndSlog("DeviceIdentifiersPolicyService");
        this.mSystemServiceManager.startService(DeviceIdentifiersPolicyService.class);
        traceEnd();
        traceBeginAndSlog("StartActivityManager");
        this.mActivityManagerService = ((Lifecycle) this.mSystemServiceManager.startService(Lifecycle.class)).getService();
        this.mActivityManagerService.setSystemServiceManager(this.mSystemServiceManager);
        this.mActivityManagerService.setInstaller(installer);
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
        if (!SystemProperties.getBoolean("config.disable_noncore", false)) {
            traceBeginAndSlog("StartRecoverySystemService");
            this.mSystemServiceManager.startService(RecoverySystemService.class);
            traceEnd();
        }
        RescueParty.noteBoot(this.mSystemContext);
        traceBeginAndSlog("StartLightsService");
        try {
            this.mSystemServiceManager.startService("com.android.server.lights.HwLightsService");
        } catch (RuntimeException e3) {
            Slog.w(TAG, "create HwLightsService failed");
            this.mSystemServiceManager.startService(LightsService.class);
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
        this.mPackageManagerService = PackageManagerService.main(this.mSystemContext, installer, this.mFactoryTestMode != 0, this.mOnlyCore);
        this.mFirstBoot = this.mPackageManagerService.isFirstBoot();
        this.mPackageManager = this.mSystemContext.getPackageManager();
        traceEnd();
        if (!(this.mRuntimeRestart || (isFirstBootOrUpgrade() ^ 1) == 0)) {
            MetricsLogger.histogram(null, "boot_package_manager_init_ready", (int) SystemClock.elapsedRealtime());
            HwBootCheck.addBootInfo("[bootinfo]\nisFirstBoot: " + this.mFirstBoot + "\n" + "isUpgrade: " + this.mPackageManagerService.isUpgrade());
            HwBootCheck.bootSceneStart(101, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
            HwBootFail.setBootStage(HwBootFail.STAGE_FRAMEWORK_JAR_DEXOPT_END);
        }
        HwBootCheck.bootSceneEnd(105);
        if (!(this.mOnlyCore || SystemProperties.getBoolean("config.disable_otadexopt", false))) {
            traceBeginAndSlog("StartOtaDexOptService");
            try {
                OtaDexoptService.main(this.mSystemContext, this.mPackageManagerService);
            } catch (Throwable e22) {
                reportWtf("starting OtaDexOptService", e22);
            } finally {
                traceEnd();
            }
        }
        traceBeginAndSlog("StartUserManagerService");
        this.mSystemServiceManager.startService(LifeCycle.class);
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
        this.mSystemServiceManager.startService(new OverlayManagerService(this.mSystemContext, installer));
        traceEnd();
        this.mSensorServiceStart = SystemServerInitThreadPool.get().submit(new Runnable() {
            public final void run() {
                $m$0();
            }
        }, START_SENSOR_SERVICE);
    }

    static /* synthetic */ void lambda$-com_android_server_SystemServer_38251() {
        BootTimingsTraceLog traceLog = new BootTimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin(START_SENSOR_SERVICE);
        startSensorService();
        traceLog.traceEnd();
    }

    private void startCoreServices() {
        traceBeginAndSlog("StartDropBoxManager");
        this.mSystemServiceManager.startService(DropBoxManagerService.class);
        traceEnd();
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
        traceBeginAndSlog("StartWebViewUpdateService");
        this.mWebViewUpdateService = (WebViewUpdateService) this.mSystemServiceManager.startService(WebViewUpdateService.class);
        traceEnd();
    }

    /* JADX WARNING: Removed duplicated region for block: B:213:0x0871  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x08b6  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0951  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0981  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0a5b  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0a7e A:{Catch:{ Throwable -> 0x10bf }} */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x065e  */
    /* JADX WARNING: Removed duplicated region for block: B:457:0x0fd8  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x067e  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x069e  */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x0725  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x076f  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x07ab A:{SYNTHETIC, Splitter: B:180:0x07ab} */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x07b8  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x07e1  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x0871  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x08b6  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0951  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0981  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0a5b  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0a7e A:{Catch:{ Throwable -> 0x10bf }} */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x065e  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x067e  */
    /* JADX WARNING: Removed duplicated region for block: B:457:0x0fd8  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x069e  */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x0725  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x076f  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x07ab A:{SYNTHETIC, Splitter: B:180:0x07ab} */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x07b8  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x07e1  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x0871  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x08b6  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0951  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0981  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0a5b  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0a7e A:{Catch:{ Throwable -> 0x10bf }} */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0951  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0981  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0a5b  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0a7e A:{Catch:{ Throwable -> 0x10bf }} */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0951  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0981  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0a5b  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0a7e A:{Catch:{ Throwable -> 0x10bf }} */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0951  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0981  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0a5b  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0a7e A:{Catch:{ Throwable -> 0x10bf }} */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x040c  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x04cb  */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0500  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0951  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0981  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0a5b  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0a7e A:{Catch:{ Throwable -> 0x10bf }} */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x040c  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x04cb  */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0500  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x0871  */
    /* JADX WARNING: Removed duplicated region for block: B:221:0x08b6  */
    /* JADX WARNING: Removed duplicated region for block: B:246:0x0951  */
    /* JADX WARNING: Removed duplicated region for block: B:251:0x0981  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0a5b  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0a7e A:{Catch:{ Throwable -> 0x10bf }} */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0ac6  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0af0  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0b04  */
    /* JADX WARNING: Removed duplicated region for block: B:317:0x0b3f  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0b5f  */
    /* JADX WARNING: Removed duplicated region for block: B:323:0x0ba3  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0bee  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x0c21  */
    /* JADX WARNING: Removed duplicated region for block: B:356:0x0c9c  */
    /* JADX WARNING: Removed duplicated region for block: B:361:0x0cdd  */
    /* JADX WARNING: Removed duplicated region for block: B:506:0x1123  */
    /* JADX WARNING: Removed duplicated region for block: B:364:0x0d28  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0d4e A:{SYNTHETIC, Splitter: B:367:0x0d4e} */
    /* JADX WARNING: Removed duplicated region for block: B:373:0x0d75  */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0d8c A:{SYNTHETIC, Splitter: B:379:0x0d8c} */
    /* JADX WARNING: Removed duplicated region for block: B:386:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:389:0x0dfe  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startOtherServices() {
        Throwable e;
        Throwable e2;
        LocationManagerService location;
        CountryDetectorService countryDetector;
        ILockSettings lockSettings;
        MediaRouterService mediaRouter;
        boolean safeMode;
        MmsServiceBroker mmsService;
        Configuration config;
        DisplayMetrics metrics;
        Theme systemTheme;
        NetworkScoreService networkScoreF;
        MmsServiceBroker mmsServiceF;
        WindowManagerService windowManagerF;
        IHwAttestationServiceFactory attestation;
        NetworkTimeUpdateService networkTimeUpdateService;
        CommonTimeManagementService commonTimeManagementService;
        HardwarePropertiesManagerService hardwarePropertiesManagerService;
        HardwarePropertiesManagerService hardwarePropertiesManagerService2;
        Context context = this.mSystemContext;
        VibratorService vibrator = null;
        IStorageManager storageManager = null;
        NetworkManagementService networkManagement = null;
        NetworkStatsService networkStats = null;
        NetworkPolicyManagerService networkPolicy = null;
        ConnectivityService connectivity = null;
        NetworkScoreService networkScore = null;
        WindowManagerService wm = null;
        NetworkTimeUpdateService networkTimeUpdater = null;
        CommonTimeManagementService commonTimeMgmtService = null;
        InputManagerService inputManager = null;
        TelephonyRegistry telephonyRegistry = null;
        HwCustEmergDataManager emergDataManager = HwCustEmergDataManager.getDefault();
        if (!(emergDataManager == null || (emergDataManager.isEmergencyState() ^ 1) == 0)) {
            HwServiceFactory.activePlaceFile();
        }
        boolean disableStorage = SystemProperties.getBoolean("config.disable_storage", false);
        boolean disableBluetooth = SystemProperties.getBoolean("config.disable_bluetooth", false);
        boolean disableLocation = SystemProperties.getBoolean("config.disable_location", false);
        boolean disableSystemUI = SystemProperties.getBoolean("config.disable_systemui", false);
        boolean disableNonCoreServices = SystemProperties.getBoolean("config.disable_noncore", false);
        boolean disableNetwork = SystemProperties.getBoolean("config.disable_network", false);
        boolean disableNetworkTime = SystemProperties.getBoolean("config.disable_networktime", false);
        boolean disableRtt = SystemProperties.getBoolean("config.disable_rtt", false);
        boolean disableMediaProjection = SystemProperties.getBoolean("config.disable_mediaproj", false);
        boolean disableSerial = SystemProperties.getBoolean("config.disable_serial", false);
        boolean disableSearchManager = SystemProperties.getBoolean("config.disable_searchmanager", false);
        boolean disableTrustManager = SystemProperties.getBoolean("config.disable_trustmanager", false);
        boolean disableTextServices = SystemProperties.getBoolean("config.disable_textservices", false);
        boolean disableSamplingProfiler = SystemProperties.getBoolean("config.disable_samplingprof", false);
        boolean disableConsumerIr = SystemProperties.getBoolean("config.disable_consumerir", false);
        boolean disableVrManager = SystemProperties.getBoolean("config.disable_vrmanager", false);
        boolean disableCameraService = SystemProperties.getBoolean("config.disable_cameraservice", false);
        boolean isEmulator = SystemProperties.get("ro.kernel.qemu").equals(ENCRYPTED_STATE);
        boolean enableRms = SystemProperties.getBoolean("ro.config.enable_rms", false);
        boolean enableIaware = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
        boolean tuiEnable = SystemProperties.getBoolean("ro.tui.service", false);
        boolean vrDisplayEnable = SystemProperties.getBoolean("ro.vr_display.service", false);
        boolean dtvEnable = SystemProperties.getBoolean("ro.dtv.service", false);
        boolean isChinaArea = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
        boolean isSupportedSecIme = isChinaArea;
        if (!disableNonCoreServices) {
            try {
                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                    this.fingerprintStartThread.start();
                }
            } catch (Throwable e3) {
                Slog.e(TAG, "Start fingerprintservice thread error", e3);
            }
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("debug.crash_system", false)) {
            throw new RuntimeException();
        }
        try {
            String SECONDARY_ZYGOTE_PRELOAD = "SecondaryZygotePreload";
            this.mZygotePreload = SystemServerInitThreadPool.get().submit(new Runnable() {
                public final void run() {
                    $m$0();
                }
            }, "SecondaryZygotePreload");
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
                telephonyRegistry = new TelephonyRegistry(context);
            } else {
                IHwTelephonyRegistry itr = HwServiceFactory.getHwTelephonyRegistry();
                if (itr != null) {
                    telephonyRegistry = itr.getInstance(context);
                } else {
                    telephonyRegistry = new TelephonyRegistry(context);
                }
            }
            ServiceManager.addService("telephony.registry", telephonyRegistry);
            traceEnd();
            traceBeginAndSlog("StartEntropyMixer");
            this.mEntropyMixer = new EntropyMixer(context);
            traceEnd();
            this.mContentResolver = context.getContentResolver();
            if (!disableCameraService) {
                Slog.i(TAG, "Camera Service Proxy");
                traceBeginAndSlog("StartCameraServiceProxy");
                this.mSystemServiceManager.startService(CameraServiceProxy.class);
                traceEnd();
            }
            traceBeginAndSlog("StartAccountManagerService");
            this.mSystemServiceManager.startService(ACCOUNT_SERVICE_CLASS);
            traceEnd();
            traceBeginAndSlog("StartContentService");
            this.mSystemServiceManager.startService(CONTENT_SERVICE_CLASS);
            traceEnd();
            traceBeginAndSlog("InstallSystemProviders");
            this.mActivityManagerService.installSystemProviders();
            traceEnd();
            traceBeginAndSlog("StartVibratorService");
            VibratorService vibratorService = new VibratorService(context);
            ServiceManager.addService("vibrator", vibratorService);
            traceEnd();
            if (!disableConsumerIr) {
                traceBeginAndSlog("StartConsumerIrService");
                ConsumerIrService consumerIrService = new ConsumerIrService(context);
                ConsumerIrService consumerIrService2;
                try {
                    ServiceManager.addService("consumer_ir", consumerIrService);
                    traceEnd();
                    consumerIrService2 = consumerIrService;
                } catch (RuntimeException e4) {
                    e2 = e4;
                    consumerIrService2 = consumerIrService;
                    vibrator = vibratorService;
                    Slog.e("System", "******************************************");
                    Slog.e("System", "************ Failure starting core service", e2);
                    location = null;
                    countryDetector = null;
                    lockSettings = null;
                    mediaRouter = null;
                    if (this.mFactoryTestMode != 1) {
                    }
                    traceBeginAndSlog("MakeDisplayReady");
                    wm.displayReady();
                    traceEnd();
                    traceBeginAndSlog("StartStorageManagerService");
                    try {
                        this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                        storageManager = Stub.asInterface(ServiceManager.getService("mount"));
                    } catch (Throwable e32) {
                        reportWtf("starting StorageManagerService", e32);
                    }
                    traceEnd();
                    traceBeginAndSlog("StartStorageStatsService");
                    try {
                        this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                    } catch (Throwable e322) {
                        reportWtf("starting StorageStatsService", e322);
                    }
                    traceEnd();
                    traceBeginAndSlog("StartUiModeManager");
                    this.mSystemServiceManager.startService(UiModeManagerService.class);
                    traceEnd();
                    HwBootCheck.bootSceneEnd(101);
                    HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
                    if (!this.mOnlyCore) {
                    }
                    traceBeginAndSlog("PerformFstrimIfNeeded");
                    this.mPackageManagerService.performFstrimIfNeeded();
                    traceEnd();
                    HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
                    HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
                    if (this.mFactoryTestMode != 1) {
                    }
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    safeMode = wm.detectSafeMode();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    traceBeginAndSlog("StartRetailDemoModeService");
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                    }
                    this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                    if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                    }
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
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
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                    networkScoreF = networkScore;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                }
            }
            try {
                AlarmManagerService almService;
                traceBeginAndSlog("StartAlarmManagerService");
                try {
                    almService = (AlarmManagerService) this.mSystemServiceManager.startService("com.android.server.HwAlarmManagerService");
                } catch (Exception e5) {
                    this.mSystemServiceManager.startService(AlarmManagerService.class);
                    almService = null;
                }
                traceEnd();
                this.mActivityManagerService.setAlarmManager(almService);
                this.mActivityManagerService.setAlarmManagerExt(almService);
                traceBeginAndSlog("Init Watchdog");
                Watchdog.getInstance().init(context, this.mActivityManagerService);
                traceEnd();
                traceBeginAndSlog("StartInputManagerService");
                Slog.i(TAG, "Input Manager");
                inputManager = HwServiceFactory.getHwInputManagerService().getInstance(context, null);
                if (enableRms || enableIaware) {
                    try {
                        this.mSystemServiceManager.startService("com.android.server.rms.HwSysResManagerService");
                    } catch (Throwable e3222) {
                        Slog.e(TAG, e3222.toString());
                    }
                }
                traceEnd();
                traceBeginAndSlog("StartWindowManagerService");
                ConcurrentUtils.waitForFutureNoInterrupt(this.mSensorServiceStart, START_SENSOR_SERVICE);
                this.mSensorServiceStart = null;
                wm = WindowManagerService.main(context, inputManager, this.mFactoryTestMode != 1, this.mFirstBoot ^ 1, this.mOnlyCore, HwPolicyFactory.getHwPhoneWindowManager());
                initRogMode(wm, context);
                int dpi = SystemProperties.getInt("persist.sys.dpi", 0);
                if (SystemProperties.getInt("persist.sys.rog.width", 0) > 0) {
                    dpi = SystemProperties.getInt("persist.sys.realdpi", SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0))));
                }
                if (dpi > 0) {
                    wm.setForcedDisplayDensityForUser(0, dpi, UserHandle.myUserId());
                }
                ServiceManager.addService("window", wm);
                ServiceManager.addService("input", inputManager);
                traceEnd();
                SystemServerInitThreadPool.get().submit(new Runnable() {
                    public final void run() {
                        $m$0();
                    }
                }, START_HIDL_SERVICES);
                if (!disableVrManager) {
                    traceBeginAndSlog("StartVrManagerService");
                    this.mSystemServiceManager.startService(VrManagerService.class);
                    traceEnd();
                }
                traceBeginAndSlog("SetWindowManagerService");
                this.mActivityManagerService.setWindowManager(wm);
                traceEnd();
                traceBeginAndSlog("StartInputManager");
                inputManager.setWindowManagerCallbacks(wm.getInputMonitor());
                inputManager.start();
                traceEnd();
                traceBeginAndSlog("DisplayManagerWindowManagerAndInputReady");
                this.mDisplayManagerService.windowManagerAndInputReady();
                traceEnd();
                if (isEmulator) {
                    Slog.i(TAG, "No Bluetooth Service (emulator)");
                } else if (this.mFactoryTestMode == 1) {
                    Slog.i(TAG, "No Bluetooth Service (factory test)");
                } else if (!context.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
                    Slog.i(TAG, "No Bluetooth Service (Bluetooth Hardware Not Present)");
                } else if (disableBluetooth) {
                    Slog.i(TAG, "Bluetooth Service disabled by config");
                } else {
                    traceBeginAndSlog("StartBluetoothService");
                    this.mSystemServiceManager.startService(BluetoothService.class);
                    traceEnd();
                }
                traceBeginAndSlog("IpConnectivityMetrics");
                this.mSystemServiceManager.startService(IpConnectivityMetrics.class);
                traceEnd();
                traceBeginAndSlog("PinnerService");
                this.mSystemServiceManager.startService(PinnerService.class);
                traceEnd();
                if (dtvEnable) {
                    Slog.i(TAG, "To add DTVService");
                    ServiceManager.addService("dtvservice", new DTVService());
                }
                vibrator = vibratorService;
            } catch (RuntimeException e6) {
                e2 = e6;
                vibrator = vibratorService;
            }
        } catch (RuntimeException e7) {
            e2 = e7;
            Slog.e("System", "******************************************");
            Slog.e("System", "************ Failure starting core service", e2);
            location = null;
            countryDetector = null;
            lockSettings = null;
            mediaRouter = null;
            if (this.mFactoryTestMode != 1) {
            }
            traceBeginAndSlog("MakeDisplayReady");
            wm.displayReady();
            traceEnd();
            traceBeginAndSlog("StartStorageManagerService");
            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
            storageManager = Stub.asInterface(ServiceManager.getService("mount"));
            traceEnd();
            traceBeginAndSlog("StartStorageStatsService");
            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
            traceEnd();
            traceBeginAndSlog("StartUiModeManager");
            this.mSystemServiceManager.startService(UiModeManagerService.class);
            traceEnd();
            HwBootCheck.bootSceneEnd(101);
            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
            if (this.mOnlyCore) {
            }
            traceBeginAndSlog("PerformFstrimIfNeeded");
            this.mPackageManagerService.performFstrimIfNeeded();
            traceEnd();
            HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
            HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
            if (this.mFactoryTestMode != 1) {
            }
            traceBeginAndSlog("StartMediaProjectionManager");
            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
            traceEnd();
            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
            }
            safeMode = wm.detectSafeMode();
            this.mSystemServiceManager.setSafeMode(safeMode);
            if (safeMode) {
            }
            traceBeginAndSlog("StartMmsService");
            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
            traceEnd();
            traceBeginAndSlog("StartRetailDemoModeService");
            this.mSystemServiceManager.startService(RetailDemoModeService.class);
            traceEnd();
            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
            }
            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
            }
            this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
            if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
            }
            traceBeginAndSlog("MakeVibratorServiceReady");
            vibrator.systemReady();
            traceEnd();
            traceBeginAndSlog("MakeLockSettingsServiceReady");
            if (lockSettings != null) {
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
            config = wm.computeNewConfiguration(0);
            metrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
            networkScoreF = networkScore;
            mmsServiceF = mmsService;
            windowManagerF = wm;
            this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
        }
        location = null;
        countryDetector = null;
        lockSettings = null;
        mediaRouter = null;
        if (this.mFactoryTestMode != 1) {
            traceBeginAndSlog("StartInputMethodManagerLifecycle");
            try {
                Slog.i(TAG, "Input Method Service");
                this.mSystemServiceManager.startService(InputMethodManagerService.Lifecycle.class);
            } catch (Throwable e32222) {
                reportWtf("starting Input Manager Service", e32222);
            }
            if (isChinaArea) {
                try {
                    Slog.i(TAG, "Secure Input Method Service");
                    this.mSystemServiceManager.startService("com.android.server.HwSecureInputMethodManagerService$MyLifecycle");
                } catch (Throwable e322222) {
                    reportWtf("starting Secure Input Manager Service", e322222);
                }
            }
            traceEnd();
            traceBeginAndSlog("StartAccessibilityManagerService");
            try {
                ServiceManager.addService("accessibility", new AccessibilityManagerService(context));
            } catch (Throwable e3222222) {
                reportWtf("starting Accessibility Manager", e3222222);
            }
            traceEnd();
        }
        traceBeginAndSlog("MakeDisplayReady");
        try {
            wm.displayReady();
        } catch (Throwable e32222222) {
            reportWtf("making display ready", e32222222);
        }
        traceEnd();
        if (!(this.mFactoryTestMode == 1 || disableStorage || ("0".equals(SystemProperties.get("system_init.startmountservice")) ^ 1) == 0)) {
            traceBeginAndSlog("StartStorageManagerService");
            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
            storageManager = Stub.asInterface(ServiceManager.getService("mount"));
            traceEnd();
            traceBeginAndSlog("StartStorageStatsService");
            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
            traceEnd();
        }
        traceBeginAndSlog("StartUiModeManager");
        this.mSystemServiceManager.startService(UiModeManagerService.class);
        traceEnd();
        HwBootCheck.bootSceneEnd(101);
        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_START);
        if (this.mOnlyCore) {
            traceBeginAndSlog("UpdatePackagesIfNeeded");
            try {
                this.mPackageManagerService.updatePackagesIfNeeded();
            } catch (Throwable e322222222) {
                reportWtf("update packages", e322222222);
            }
            traceEnd();
        }
        traceBeginAndSlog("PerformFstrimIfNeeded");
        try {
            this.mPackageManagerService.performFstrimIfNeeded();
        } catch (Throwable e3222222222) {
            reportWtf("performing fstrim", e3222222222);
        }
        traceEnd();
        HwBootCheck.bootSceneStart(102, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
        HwBootFail.setBootStage(HwBootFail.STAGE_APP_DEXOPT_END);
        if (this.mFactoryTestMode != 1) {
            INotificationManager notification;
            startForceRotation(context);
            if (!disableNonCoreServices) {
                traceBeginAndSlog("StartLockSettingsService");
                try {
                    this.mSystemServiceManager.startService(LOCK_SETTINGS_SERVICE_CLASS);
                    lockSettings = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
                } catch (Throwable e32222222222) {
                    reportWtf("starting LockSettingsService service", e32222222222);
                }
                traceEnd();
                if (!SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP).equals("")) {
                    traceBeginAndSlog("StartPersistentDataBlock");
                    this.mSystemServiceManager.startService(PersistentDataBlockService.class);
                    traceEnd();
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
            }
            if (!disableSystemUI) {
                traceBeginAndSlog("StartStatusBarManagerService");
                try {
                    Slog.i(TAG, "Status Bar");
                    ServiceManager.addService("statusbar", HwServiceFactory.createHwStatusBarManagerService(context, wm));
                } catch (Throwable e322222222222) {
                    reportWtf("starting StatusBarManagerService", e322222222222);
                }
                traceEnd();
            }
            if (!disableNonCoreServices) {
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
            }
            if (!disableNetwork) {
                traceBeginAndSlog("StartNetworkManagementService");
                try {
                    networkManagement = NetworkManagementService.create(context);
                    ServiceManager.addService("network_management", networkManagement);
                } catch (Throwable e3222222222222) {
                    reportWtf("starting NetworkManagement Service", e3222222222222);
                }
                traceEnd();
            }
            if (!(disableNonCoreServices || (disableTextServices ^ 1) == 0)) {
                traceBeginAndSlog("StartTextServicesManager");
                this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                traceEnd();
            }
            if (!disableNetwork) {
                traceBeginAndSlog("StartNetworkScoreService");
                try {
                    NetworkScoreService networkScoreService = new NetworkScoreService(context);
                    try {
                        ServiceManager.addService("network_score", networkScoreService);
                        networkScore = networkScoreService;
                    } catch (Throwable th) {
                        e3222222222222 = th;
                        networkScore = networkScoreService;
                        reportWtf("starting Network Score Service", e3222222222222);
                        traceEnd();
                        traceBeginAndSlog("StartNetworkStatsService");
                        networkStats = NetworkStatsService.create(context, networkManagement);
                        ServiceManager.addService("netstats", networkStats);
                        traceEnd();
                        traceBeginAndSlog("StartNetworkPolicyManagerService");
                        networkPolicy = HwServiceFactory.getHwNetworkPolicyManagerService().getInstance(context, this.mActivityManagerService, (IPowerManager) ServiceManager.getService("power"), networkStats, networkManagement);
                        ServiceManager.addService("netpolicy", networkPolicy);
                        traceEnd();
                        traceBeginAndSlog("StartWifi");
                        this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartWifiScanning");
                        this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                        traceEnd();
                        if (!disableRtt) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                        }
                        traceBeginAndSlog("StartEthernet");
                        this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartConnectivityService");
                        Slog.i(TAG, "Connectivity Service");
                        connectivity = HwServiceFactory.getHwConnectivityManager().createHwConnectivityService(context, networkManagement, networkStats, networkPolicy);
                        ServiceManager.addService("connectivity", connectivity);
                        networkStats.bindConnectivityManager(connectivity);
                        networkPolicy.bindConnectivityManager(connectivity);
                        traceEnd();
                        traceBeginAndSlog("StartNsdService");
                        ServiceManager.addService("servicediscovery", NsdService.create(context));
                        traceEnd();
                        if (!disableNonCoreServices) {
                        }
                        traceBeginAndSlog("WaitForAsecScan");
                        try {
                            storageManager.waitForAsecScan();
                        } catch (RemoteException e8) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartNotificationManager");
                        this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
                        SystemNotificationChannels.createAll(context);
                        notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                        if (networkPolicy != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartDeviceMonitor");
                        this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
                        traceEnd();
                        Slog.i(TAG, "TUI Connect enable " + tuiEnable);
                        if (tuiEnable) {
                        }
                        if (vrDisplayEnable) {
                        }
                        if (!disableLocation) {
                        }
                        traceBeginAndSlog("StartSearchManagerService");
                        try {
                            this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                        } catch (Throwable e32222222222222) {
                            reportWtf("starting Search Service", e32222222222222);
                        }
                        traceEnd();
                        traceBeginAndSlog("StartWallpaperManagerService");
                        this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                        traceEnd();
                        traceBeginAndSlog("StartAudioService");
                        this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                        traceEnd();
                        if (!disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        traceEnd();
                        if (!disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        if (!disableNonCoreServices) {
                        }
                        HwServiceFactory.setupHwServices(context);
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        if (!disableSamplingProfiler) {
                        }
                        Slog.i(TAG, "attestation Service");
                        attestation = HwServiceFactory.getHwAttestationService();
                        if (attestation != null) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        try {
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            try {
                                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                networkTimeUpdater = networkTimeUpdateService;
                            } catch (Throwable th2) {
                                e32222222222222 = th2;
                                networkTimeUpdater = networkTimeUpdateService;
                                reportWtf("starting NetworkTimeUpdate service", e32222222222222);
                                traceEnd();
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeManagementService = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeManagementService);
                                commonTimeMgmtService = commonTimeManagementService;
                                traceEnd();
                                if (!disableNetwork) {
                                }
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                if (!disableNonCoreServices) {
                                }
                                if (!disableNonCoreServices) {
                                }
                                traceBeginAndSlog("AddCoverageService");
                                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                                traceEnd();
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
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                traceBeginAndSlog("StartTvInputManager");
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                if (!disableNonCoreServices) {
                                }
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaProjectionManager");
                                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                traceEnd();
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                }
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                traceBeginAndSlog("StartRetailDemoModeService");
                                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                                if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings != null) {
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
                                config = wm.computeNewConfiguration(0);
                                metrics = new DisplayMetrics();
                                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                                networkScoreF = networkScore;
                                mmsServiceF = mmsService;
                                windowManagerF = wm;
                                this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                            }
                        } catch (Throwable th3) {
                            e32222222222222 = th3;
                            reportWtf("starting NetworkTimeUpdate service", e32222222222222);
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            traceBeginAndSlog("StartTvInputManager");
                            this.mSystemServiceManager.startService(TvInputManagerService.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            safeMode = wm.detectSafeMode();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            if (safeMode) {
                            }
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            traceBeginAndSlog("StartRetailDemoModeService");
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                            }
                            this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                            if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                            }
                            traceBeginAndSlog("MakeVibratorServiceReady");
                            vibrator.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
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
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                            networkScoreF = networkScore;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        traceEnd();
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        try {
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                        } catch (Throwable th4) {
                            e32222222222222 = th4;
                            commonTimeMgmtService = commonTimeManagementService;
                            reportWtf("starting CommonTimeManagementService service", e32222222222222);
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            traceBeginAndSlog("StartTvInputManager");
                            this.mSystemServiceManager.startService(TvInputManagerService.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            safeMode = wm.detectSafeMode();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            if (safeMode) {
                            }
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            traceBeginAndSlog("StartRetailDemoModeService");
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                            }
                            this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                            if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                            }
                            traceBeginAndSlog("MakeVibratorServiceReady");
                            vibrator.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
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
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                            networkScoreF = networkScore;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        traceEnd();
                        if (disableNetwork) {
                        }
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("AddCoverageService");
                        ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                        traceEnd();
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
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        traceBeginAndSlog("StartTvInputManager");
                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        safeMode = wm.detectSafeMode();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        traceBeginAndSlog("StartRetailDemoModeService");
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                        }
                        this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                        }
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
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
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                        networkScoreF = networkScore;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (Throwable th5) {
                    e32222222222222 = th5;
                    reportWtf("starting Network Score Service", e32222222222222);
                    traceEnd();
                    traceBeginAndSlog("StartNetworkStatsService");
                    networkStats = NetworkStatsService.create(context, networkManagement);
                    ServiceManager.addService("netstats", networkStats);
                    traceEnd();
                    traceBeginAndSlog("StartNetworkPolicyManagerService");
                    networkPolicy = HwServiceFactory.getHwNetworkPolicyManagerService().getInstance(context, this.mActivityManagerService, (IPowerManager) ServiceManager.getService("power"), networkStats, networkManagement);
                    ServiceManager.addService("netpolicy", networkPolicy);
                    traceEnd();
                    traceBeginAndSlog("StartWifi");
                    this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartWifiScanning");
                    this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                    traceEnd();
                    if (disableRtt) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                    }
                    traceBeginAndSlog("StartEthernet");
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartConnectivityService");
                    Slog.i(TAG, "Connectivity Service");
                    connectivity = HwServiceFactory.getHwConnectivityManager().createHwConnectivityService(context, networkManagement, networkStats, networkPolicy);
                    ServiceManager.addService("connectivity", connectivity);
                    networkStats.bindConnectivityManager(connectivity);
                    networkPolicy.bindConnectivityManager(connectivity);
                    traceEnd();
                    traceBeginAndSlog("StartNsdService");
                    ServiceManager.addService("servicediscovery", NsdService.create(context));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("WaitForAsecScan");
                    storageManager.waitForAsecScan();
                    traceEnd();
                    traceBeginAndSlog("StartNotificationManager");
                    this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
                    SystemNotificationChannels.createAll(context);
                    notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                    if (networkPolicy != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartDeviceMonitor");
                    this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
                    traceEnd();
                    Slog.i(TAG, "TUI Connect enable " + tuiEnable);
                    if (tuiEnable) {
                    }
                    if (vrDisplayEnable) {
                    }
                    if (disableLocation) {
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                    traceEnd();
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    HwServiceFactory.setupHwServices(context);
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    if (disableSamplingProfiler) {
                    }
                    Slog.i(TAG, "attestation Service");
                    attestation = HwServiceFactory.getHwAttestationService();
                    if (attestation != null) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    traceBeginAndSlog("StartTvInputManager");
                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    safeMode = wm.detectSafeMode();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    traceBeginAndSlog("StartRetailDemoModeService");
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                    }
                    this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                    if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                    }
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
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
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                    networkScoreF = networkScore;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
                traceBeginAndSlog("StartNetworkStatsService");
                try {
                    networkStats = NetworkStatsService.create(context, networkManagement);
                    ServiceManager.addService("netstats", networkStats);
                } catch (Throwable e322222222222222) {
                    reportWtf("starting NetworkStats Service", e322222222222222);
                }
                traceEnd();
                traceBeginAndSlog("StartNetworkPolicyManagerService");
                try {
                    networkPolicy = HwServiceFactory.getHwNetworkPolicyManagerService().getInstance(context, this.mActivityManagerService, (IPowerManager) ServiceManager.getService("power"), networkStats, networkManagement);
                    ServiceManager.addService("netpolicy", networkPolicy);
                } catch (Throwable e3222222222222222) {
                    reportWtf("starting NetworkPolicy Service", e3222222222222222);
                }
                traceEnd();
                traceBeginAndSlog("StartWifi");
                this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartWifiScanning");
                this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                traceEnd();
                if (disableRtt) {
                    traceBeginAndSlog("StartWifiRtt");
                    this.mSystemServiceManager.startService("com.android.server.wifi.RttService");
                    traceEnd();
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                    traceBeginAndSlog("StartWifiAware");
                    this.mSystemServiceManager.startService(WIFI_AWARE_SERVICE_CLASS);
                    traceEnd();
                } else {
                    Slog.i(TAG, "No Wi-Fi Aware Service (Aware support Not Present)");
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                    traceBeginAndSlog("StartWifiP2P");
                    this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                    traceEnd();
                }
                if (this.mPackageManager.hasSystemFeature("android.hardware.ethernet") || this.mPackageManager.hasSystemFeature("android.hardware.usb.host")) {
                    traceBeginAndSlog("StartEthernet");
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                    traceEnd();
                }
                traceBeginAndSlog("StartConnectivityService");
                try {
                    Slog.i(TAG, "Connectivity Service");
                    connectivity = HwServiceFactory.getHwConnectivityManager().createHwConnectivityService(context, networkManagement, networkStats, networkPolicy);
                    ServiceManager.addService("connectivity", connectivity);
                    networkStats.bindConnectivityManager(connectivity);
                    networkPolicy.bindConnectivityManager(connectivity);
                } catch (Throwable e32222222222222222) {
                    reportWtf("starting Connectivity Service", e32222222222222222);
                }
                traceEnd();
                traceBeginAndSlog("StartNsdService");
                try {
                    ServiceManager.addService("servicediscovery", NsdService.create(context));
                } catch (Throwable e322222222222222222) {
                    reportWtf("starting Service Discovery Service", e322222222222222222);
                }
                traceEnd();
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartUpdateLockService");
                try {
                    ServiceManager.addService("updatelock", new UpdateLockService(context));
                } catch (Throwable e3222222222222222222) {
                    reportWtf("starting UpdateLockService", e3222222222222222222);
                }
                traceEnd();
            }
            if (!(storageManager == null || (this.mOnlyCore ^ 1) == 0)) {
                traceBeginAndSlog("WaitForAsecScan");
                storageManager.waitForAsecScan();
                traceEnd();
            }
            traceBeginAndSlog("StartNotificationManager");
            try {
                this.mSystemServiceManager.startService("com.android.server.notification.HwNotificationManagerService");
            } catch (RuntimeException e9) {
                this.mSystemServiceManager.startService(NotificationManagerService.class);
            }
            SystemNotificationChannels.createAll(context);
            notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
            if (networkPolicy != null) {
                networkPolicy.bindNotificationManager(notification);
            }
            traceEnd();
            traceBeginAndSlog("StartDeviceMonitor");
            this.mSystemServiceManager.startService(HwServiceFactory.getDeviceStorageMonitorServiceClassName());
            traceEnd();
            Slog.i(TAG, "TUI Connect enable " + tuiEnable);
            if (tuiEnable) {
                try {
                    ServiceManager.addService("tui", new TrustedUIService(context));
                } catch (Throwable e32222222222222222222) {
                    Slog.e(TAG, "Failure starting TUI Service ", e32222222222222222222);
                }
            }
            if (vrDisplayEnable) {
                Slog.i(TAG, "VR Display enable " + vrDisplayEnable);
                try {
                    ServiceManager.addService("vr_display", new VRManagerService(context));
                } catch (Throwable e322222222222222222222) {
                    Slog.e(TAG, "Failure starting VR Service ", e322222222222222222222);
                }
            }
            if (disableLocation) {
                traceBeginAndSlog("StartLocationManagerService");
                try {
                    Slog.i(TAG, "Location Manager");
                    IHwLocationManagerService hwLocation = HwServiceFactory.getHwLocationManagerService();
                    if (hwLocation != null) {
                        location = hwLocation.getInstance(context);
                    } else {
                        location = new LocationManagerService(context);
                    }
                    ServiceManager.addService("location", location);
                } catch (Throwable e3222222222222222222222) {
                    reportWtf("starting Location Manager", e3222222222222222222222);
                }
                traceEnd();
                traceBeginAndSlog("StartCountryDetectorService");
                try {
                    CountryDetectorService countryDetectorService = new CountryDetectorService(context);
                    try {
                        ServiceManager.addService("country_detector", countryDetectorService);
                        countryDetector = countryDetectorService;
                    } catch (Throwable th6) {
                        e3222222222222222222222 = th6;
                        countryDetector = countryDetectorService;
                        reportWtf("starting Country Detector", e3222222222222222222222);
                        traceEnd();
                        traceBeginAndSlog("StartSearchManagerService");
                        this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartWallpaperManagerService");
                        this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                        traceEnd();
                        traceBeginAndSlog("StartAudioService");
                        this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        HwServiceFactory.setupHwServices(context);
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        if (disableSamplingProfiler) {
                        }
                        Slog.i(TAG, "attestation Service");
                        attestation = HwServiceFactory.getHwAttestationService();
                        if (attestation != null) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        networkTimeUpdateService = new NetworkTimeUpdateService(context);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdater = networkTimeUpdateService;
                        traceEnd();
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeMgmtService = commonTimeManagementService;
                        traceEnd();
                        if (disableNetwork) {
                        }
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("AddCoverageService");
                        ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                        traceEnd();
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
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        traceBeginAndSlog("StartTvInputManager");
                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        safeMode = wm.detectSafeMode();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        traceBeginAndSlog("StartRetailDemoModeService");
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                        }
                        this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                        }
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
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
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                        networkScoreF = networkScore;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (Throwable th7) {
                    e3222222222222222222222 = th7;
                    reportWtf("starting Country Detector", e3222222222222222222222);
                    traceEnd();
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                    traceEnd();
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    HwServiceFactory.setupHwServices(context);
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    if (disableSamplingProfiler) {
                    }
                    Slog.i(TAG, "attestation Service");
                    attestation = HwServiceFactory.getHwAttestationService();
                    if (attestation != null) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    traceBeginAndSlog("StartTvInputManager");
                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    safeMode = wm.detectSafeMode();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    traceBeginAndSlog("StartRetailDemoModeService");
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                    }
                    this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                    if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                    }
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
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
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                    networkScoreF = networkScore;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
            }
            if (!(disableNonCoreServices || (disableSearchManager ^ 1) == 0)) {
                traceBeginAndSlog("StartSearchManagerService");
                this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                traceEnd();
            }
            if (!disableNonCoreServices && context.getResources().getBoolean(17956960)) {
                traceBeginAndSlog("StartWallpaperManagerService");
                this.mSystemServiceManager.startService(HwServiceFactory.getWallpaperManagerServiceClassName());
                traceEnd();
            }
            traceBeginAndSlog("StartAudioService");
            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
            traceEnd();
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartDockObserver");
                this.mSystemServiceManager.startService(DockObserver.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    traceBeginAndSlog("StartThermalObserver");
                    this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                    traceEnd();
                }
            }
            traceBeginAndSlog("StartWiredAccessoryManager");
            try {
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
            } catch (Throwable e32222222222222222222222) {
                reportWtf("starting WiredAccessoryManager", e32222222222222222222222);
            }
            traceEnd();
            if (disableNonCoreServices) {
                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                    traceBeginAndSlog("StartMidiManager");
                    this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                    traceEnd();
                }
                if (this.mPackageManager.hasSystemFeature("android.hardware.usb.host") || this.mPackageManager.hasSystemFeature("android.hardware.usb.accessory")) {
                    traceBeginAndSlog("StartUsbService");
                    this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                    traceEnd();
                }
                if (!disableSerial) {
                    traceBeginAndSlog("StartSerialService");
                    try {
                        SerialService serialService = new SerialService(context);
                        SerialService serialService2;
                        try {
                            ServiceManager.addService("serial", serialService);
                            serialService2 = serialService;
                        } catch (Throwable th8) {
                            e32222222222222222222222 = th8;
                            serialService2 = serialService;
                            Slog.e(TAG, "Failure starting SerialService", e32222222222222222222222);
                            traceEnd();
                            traceBeginAndSlog("StartHardwarePropertiesManagerService");
                            hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context);
                            try {
                                ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                                hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                            } catch (Throwable th9) {
                                e32222222222222222222222 = th9;
                                hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                                Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e32222222222222222222222);
                                traceEnd();
                                traceBeginAndSlog("StartTwilightService");
                                this.mSystemServiceManager.startService(TwilightService.class);
                                traceEnd();
                                if (NightDisplayController.isAvailable(context)) {
                                }
                                this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                                traceBeginAndSlog("StartSoundTrigger");
                                this.mSystemServiceManager.startService(SoundTriggerService.class);
                                traceEnd();
                                if (disableNonCoreServices) {
                                }
                                HwServiceFactory.setupHwServices(context);
                                traceBeginAndSlog("StartDiskStatsService");
                                ServiceManager.addService("diskstats", new DiskStatsService(context));
                                traceEnd();
                                if (disableSamplingProfiler) {
                                }
                                Slog.i(TAG, "attestation Service");
                                attestation = HwServiceFactory.getHwAttestationService();
                                if (attestation != null) {
                                }
                                traceBeginAndSlog("StartNetworkTimeUpdateService");
                                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                networkTimeUpdater = networkTimeUpdateService;
                                traceEnd();
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeManagementService = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeManagementService);
                                commonTimeMgmtService = commonTimeManagementService;
                                traceEnd();
                                if (disableNetwork) {
                                }
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                if (disableNonCoreServices) {
                                }
                                if (disableNonCoreServices) {
                                }
                                traceBeginAndSlog("AddCoverageService");
                                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                                traceEnd();
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
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                traceBeginAndSlog("StartTvInputManager");
                                this.mSystemServiceManager.startService(TvInputManagerService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                if (disableNonCoreServices) {
                                }
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                traceBeginAndSlog("StartMediaProjectionManager");
                                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                traceEnd();
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                }
                                safeMode = wm.detectSafeMode();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                traceBeginAndSlog("StartRetailDemoModeService");
                                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                                }
                                this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                                if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                                }
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings != null) {
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
                                config = wm.computeNewConfiguration(0);
                                metrics = new DisplayMetrics();
                                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                                networkScoreF = networkScore;
                                mmsServiceF = mmsService;
                                windowManagerF = wm;
                                this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartTwilightService");
                            this.mSystemServiceManager.startService(TwilightService.class);
                            traceEnd();
                            if (NightDisplayController.isAvailable(context)) {
                            }
                            this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                            traceBeginAndSlog("StartSoundTrigger");
                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            HwServiceFactory.setupHwServices(context);
                            traceBeginAndSlog("StartDiskStatsService");
                            ServiceManager.addService("diskstats", new DiskStatsService(context));
                            traceEnd();
                            if (disableSamplingProfiler) {
                            }
                            Slog.i(TAG, "attestation Service");
                            attestation = HwServiceFactory.getHwAttestationService();
                            if (attestation != null) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            traceBeginAndSlog("StartTvInputManager");
                            this.mSystemServiceManager.startService(TvInputManagerService.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            safeMode = wm.detectSafeMode();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            if (safeMode) {
                            }
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            traceBeginAndSlog("StartRetailDemoModeService");
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
                            if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                            }
                            this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                            if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                            }
                            traceBeginAndSlog("MakeVibratorServiceReady");
                            vibrator.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
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
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                            networkScoreF = networkScore;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                        }
                    } catch (Throwable th10) {
                        e32222222222222222222222 = th10;
                        Slog.e(TAG, "Failure starting SerialService", e32222222222222222222222);
                        traceEnd();
                        traceBeginAndSlog("StartHardwarePropertiesManagerService");
                        hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context);
                        ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                        hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                        traceEnd();
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        HwServiceFactory.setupHwServices(context);
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        if (disableSamplingProfiler) {
                        }
                        Slog.i(TAG, "attestation Service");
                        attestation = HwServiceFactory.getHwAttestationService();
                        if (attestation != null) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        networkTimeUpdateService = new NetworkTimeUpdateService(context);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdater = networkTimeUpdateService;
                        traceEnd();
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeMgmtService = commonTimeManagementService;
                        traceEnd();
                        if (disableNetwork) {
                        }
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("AddCoverageService");
                        ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                        traceEnd();
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
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        traceBeginAndSlog("StartTvInputManager");
                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        safeMode = wm.detectSafeMode();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        traceBeginAndSlog("StartRetailDemoModeService");
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                        }
                        this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                        }
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
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
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                        networkScoreF = networkScore;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                    }
                    traceEnd();
                }
                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                try {
                    hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context);
                    ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                    hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                } catch (Throwable th11) {
                    e32222222222222222222222 = th11;
                    Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e32222222222222222222222);
                    traceEnd();
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    HwServiceFactory.setupHwServices(context);
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    if (disableSamplingProfiler) {
                    }
                    Slog.i(TAG, "attestation Service");
                    attestation = HwServiceFactory.getHwAttestationService();
                    if (attestation != null) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    traceBeginAndSlog("StartTvInputManager");
                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    safeMode = wm.detectSafeMode();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    traceBeginAndSlog("StartRetailDemoModeService");
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                    }
                    this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                    if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                    }
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
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
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                    networkScoreF = networkScore;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
            }
            traceBeginAndSlog("StartTwilightService");
            this.mSystemServiceManager.startService(TwilightService.class);
            traceEnd();
            if (NightDisplayController.isAvailable(context)) {
                traceBeginAndSlog("StartNightDisplay");
                this.mSystemServiceManager.startService(NightDisplayService.class);
                traceEnd();
            }
            try {
                this.mSystemServiceManager.startService("com.android.server.job.HwJobSchedulerService");
            } catch (RuntimeException e10) {
                Slog.w(TAG, "create HwJobSchedulerService failed");
                this.mSystemServiceManager.startService(JobSchedulerService.class);
            }
            traceBeginAndSlog("StartSoundTrigger");
            this.mSystemServiceManager.startService(SoundTriggerService.class);
            traceEnd();
            if (disableNonCoreServices) {
                if (!disableTrustManager) {
                    traceBeginAndSlog("StartTrustManager");
                    this.mSystemServiceManager.startService(TrustManagerService.class);
                    traceEnd();
                }
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                    traceBeginAndSlog("StartBackupManager");
                    this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                    traceEnd();
                }
                if (this.mPackageManager.hasSystemFeature("android.software.app_widgets") || context.getResources().getBoolean(17956940)) {
                    traceBeginAndSlog("StartAppWidgerService");
                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                    traceEnd();
                }
                if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                    traceBeginAndSlog("StartVoiceRecognitionManager");
                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                    traceEnd();
                }
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
            }
            HwServiceFactory.setupHwServices(context);
            traceBeginAndSlog("StartDiskStatsService");
            try {
                ServiceManager.addService("diskstats", new DiskStatsService(context));
            } catch (Throwable e322222222222222222222222) {
                reportWtf("starting DiskStats Service", e322222222222222222222222);
            }
            traceEnd();
            if (disableSamplingProfiler) {
                traceBeginAndSlog("StartSamplingProfilerService");
                try {
                    ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context));
                } catch (Throwable e3222222222222222222222222) {
                    reportWtf("starting SamplingProfiler Service", e3222222222222222222222222);
                }
                traceEnd();
            }
            try {
                Slog.i(TAG, "attestation Service");
                attestation = HwServiceFactory.getHwAttestationService();
                if (attestation != null) {
                    ServiceManager.addService("attestation_service", attestation.getInstance(context));
                }
            } catch (Throwable e32222222222222222222222222) {
                Slog.i(TAG, "attestation_service failed");
                reportWtf("attestation Service", e32222222222222222222222222);
            }
            if (!(disableNetwork || (disableNetworkTime ^ 1) == 0)) {
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                traceEnd();
            }
            traceBeginAndSlog("StartCommonTimeManagementService");
            try {
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
            } catch (Throwable th12) {
                e32222222222222222222222222 = th12;
                reportWtf("starting CommonTimeManagementService service", e32222222222222222222222222);
                traceEnd();
                if (disableNetwork) {
                }
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                if (disableNonCoreServices) {
                }
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("AddCoverageService");
                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                traceEnd();
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
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                safeMode = wm.detectSafeMode();
                this.mSystemServiceManager.setSafeMode(safeMode);
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                traceBeginAndSlog("StartRetailDemoModeService");
                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                }
                this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                }
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
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
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                networkScoreF = networkScore;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            if (disableNetwork) {
                traceBeginAndSlog("CertBlacklister");
                try {
                    CertBlacklister certBlacklister = new CertBlacklister(context);
                } catch (Throwable e322222222222222222222222222) {
                    reportWtf("starting CertBlacklister", e322222222222222222222222222);
                }
                traceEnd();
            }
            if (!(disableNetwork || (disableNonCoreServices ^ 1) == 0)) {
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                traceEnd();
            }
            if (!disableNonCoreServices && CoverageService.ENABLED) {
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
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartMediaRouterService");
                try {
                    MediaRouterService mediaRouterService = new MediaRouterService(context);
                    try {
                        ServiceManager.addService("media_router", mediaRouterService);
                        mediaRouter = mediaRouterService;
                    } catch (Throwable th13) {
                        e322222222222222222222222222 = th13;
                        mediaRouter = mediaRouterService;
                        reportWtf("starting MediaRouterService", e322222222222222222222222222);
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
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        safeMode = wm.detectSafeMode();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        traceBeginAndSlog("StartRetailDemoModeService");
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                        }
                        this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                        }
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
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
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                        networkScoreF = networkScore;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (Throwable th14) {
                    e322222222222222222222222222 = th14;
                    reportWtf("starting MediaRouterService", e322222222222222222222222222);
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
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    safeMode = wm.detectSafeMode();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    traceBeginAndSlog("StartRetailDemoModeService");
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
                    }
                    this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
                    if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
                    }
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
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
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
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
                    networkScoreF = networkScore;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                try {
                    BackgroundDexOptService.schedule(context);
                } catch (Throwable e3222222222222222222222222222) {
                    reportWtf("starting StartBackgroundDexOptService", e3222222222222222222222222222);
                }
                traceEnd();
                traceBeginAndSlog("StartPruneInstantAppsJobService");
                try {
                    PruneInstantAppsJobService.schedule(context);
                } catch (Throwable e32222222222222222222222222222) {
                    reportWtf("StartPruneInstantAppsJobService", e32222222222222222222222222222);
                }
                traceEnd();
            }
            traceBeginAndSlog("StartShortcutServiceLifecycle");
            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
            traceEnd();
            traceBeginAndSlog("StartLauncherAppsService");
            this.mSystemServiceManager.startService(LauncherAppsService.class);
            traceEnd();
        }
        if (!(disableNonCoreServices || (disableMediaProjection ^ 1) == 0)) {
            traceBeginAndSlog("StartMediaProjectionManager");
            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
            traceEnd();
        }
        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
            traceBeginAndSlog("StartWearConnectivityService");
            this.mSystemServiceManager.startService(WEAR_CONNECTIVITY_SERVICE_CLASS);
            traceEnd();
            if (!disableNonCoreServices) {
                traceBeginAndSlog("StartWearTimeService");
                this.mSystemServiceManager.startService(WEAR_DISPLAY_SERVICE_CLASS);
                this.mSystemServiceManager.startService(WEAR_TIME_SERVICE_CLASS);
                traceEnd();
            }
        }
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
        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
        traceEnd();
        traceBeginAndSlog("StartRetailDemoModeService");
        this.mSystemServiceManager.startService(RetailDemoModeService.class);
        traceEnd();
        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
            traceBeginAndSlog("StartAutoFillService");
            this.mSystemServiceManager.startService(AUTO_FILL_MANAGER_SERVICE_CLASS);
            traceEnd();
        }
        if ("true".equals(SystemProperties.get("bastet.service.enable", "false"))) {
            try {
                this.mSystemServiceManager.startService("com.android.server.HwBastetService");
            } catch (Exception e11) {
                Slog.w(TAG, "HwBastetService not exists.");
            }
        }
        try {
            this.mSystemServiceManager.startService("com.android.server.HwDubaiService");
        } catch (Exception e12) {
            Slog.w(TAG, "HwDubaiService not exists.");
        }
        if ("true".equals(SystemProperties.get("ro.config.hw_emcom", "false"))) {
            startEmcomService();
        }
        traceBeginAndSlog("MakeVibratorServiceReady");
        try {
            vibrator.systemReady();
        } catch (Throwable e322222222222222222222222222222) {
            reportWtf("making Vibrator Service ready", e322222222222222222222222222222);
        }
        traceEnd();
        traceBeginAndSlog("MakeLockSettingsServiceReady");
        if (lockSettings != null) {
            try {
                lockSettings.systemReady();
            } catch (Throwable e3222222222222222222222222222222) {
                reportWtf("making Lock Settings Service ready", e3222222222222222222222222222222);
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
        try {
            wm.systemReady();
        } catch (Throwable e32222222222222222222222222222222) {
            reportWtf("making Window Manager Service ready", e32222222222222222222222222222222);
        }
        traceEnd();
        if (safeMode) {
            this.mActivityManagerService.showSafeModeOverlay();
        }
        config = wm.computeNewConfiguration(0);
        metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
        context.getResources().updateConfiguration(config, metrics);
        systemTheme = context.getTheme();
        if (systemTheme.getChangingConfigurations() != 0) {
            systemTheme.rebase();
        }
        traceBeginAndSlog("MakePowerManagerServiceReady");
        try {
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
        } catch (Throwable e322222222222222222222222222222222) {
            reportWtf("making Power Manager Service ready", e322222222222222222222222222222222);
        }
        traceEnd();
        try {
            this.mPGManagerService.systemReady(this.mActivityManagerService, this.mPowerManagerService, location);
        } catch (Throwable e3222222222222222222222222222222222) {
            reportWtf("making PG Manager Service ready", e3222222222222222222222222222222222);
        }
        traceBeginAndSlog("MakePackageManagerServiceReady");
        try {
            this.mPackageManagerService.systemReady();
        } catch (Throwable e32222222222222222222222222222222222) {
            reportWtf("making Package Manager Service ready", e32222222222222222222222222222222222);
        }
        traceEnd();
        traceBeginAndSlog("MakeDisplayManagerServiceReady");
        try {
            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
        } catch (Throwable e322222222222222222222222222222222222) {
            reportWtf("making Display Manager Service ready", e322222222222222222222222222222222222);
        }
        traceEnd();
        this.mSystemServiceManager.setSafeMode(safeMode);
        networkScoreF = networkScore;
        mmsServiceF = mmsService;
        windowManagerF = wm;
        this.mActivityManagerService.systemReady(new AnonymousClass5(enableIaware, this, context, windowManagerF, networkScoreF, networkManagement, networkPolicy, networkStats, connectivity, location, countryDetector, networkTimeUpdater, commonTimeMgmtService, inputManager, telephonyRegistry, mediaRouter, mmsService), BOOT_TIMINGS_TRACE_LOG);
    }

    static /* synthetic */ void lambda$-com_android_server_SystemServer_45948() {
        try {
            Slog.i(TAG, "SecondaryZygotePreload");
            BootTimingsTraceLog traceLog = new BootTimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
            traceLog.traceBegin("SecondaryZygotePreload");
            if (!Process.zygoteProcess.preloadDefault(Build.SUPPORTED_32_BIT_ABIS[0])) {
                Slog.e(TAG, "Unable to preload default resources");
            }
            traceLog.traceEnd();
        } catch (Exception ex) {
            Slog.e(TAG, "Exception preloading default resources", ex);
        }
    }

    static /* synthetic */ void lambda$-com_android_server_SystemServer_54841() {
        BootTimingsTraceLog traceLog = new BootTimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin(START_HIDL_SERVICES);
        startHidlServices();
        traceLog.traceEnd();
    }

    /* synthetic */ void lambda$-com_android_server_SystemServer_98907(Context context, WindowManagerService windowManagerF, NetworkScoreService networkScoreF, NetworkManagementService networkManagementF, NetworkPolicyManagerService networkPolicyF, NetworkStatsService networkStatsF, ConnectivityService connectivityF, LocationManagerService locationF, CountryDetectorService countryDetectorF, NetworkTimeUpdateService networkTimeUpdaterF, CommonTimeManagementService commonTimeMgmtServiceF, InputManagerService inputManagerF, TelephonyRegistry telephonyRegistryF, MediaRouterService mediaRouterF, MmsServiceBroker mmsServiceF, boolean enableIaware) {
        Slog.i(TAG, "Making services ready");
        traceBeginAndSlog("StartActivityManagerReadyPhase");
        this.mSystemServiceManager.startBootPhase(550);
        traceEnd();
        traceBeginAndSlog("StartObservingNativeCrashes");
        try {
            this.mActivityManagerService.startObservingNativeCrashes();
        } catch (Throwable e) {
            reportWtf("observing native crashes", e);
        }
        traceEnd();
        String WEBVIEW_PREPARATION = "WebViewFactoryPreparation";
        Future webviewPrep = null;
        if (!this.mOnlyCore) {
            webviewPrep = SystemServerInitThreadPool.get().submit(new AnonymousClass4(this), "WebViewFactoryPreparation");
        }
        traceBeginAndSlog("StartSystemUI");
        try {
            startSystemUi(context, windowManagerF);
        } catch (Throwable e2) {
            reportWtf("starting System UI", e2);
        }
        traceEnd();
        try {
            HwFrameworkFactory.getAudioEffectLowPowerTask(context);
        } catch (Throwable e22) {
            Slog.e(TAG, "AudioEffectLowPowerTask occure error:", e22);
        }
        traceBeginAndSlog("MakeNetworkScoreReady");
        if (networkScoreF != null) {
            try {
                networkScoreF.systemReady();
            } catch (Throwable e222) {
                reportWtf("making Network Score Service ready", e222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkManagementServiceReady");
        if (networkManagementF != null) {
            try {
                networkManagementF.systemReady();
            } catch (Throwable e2222) {
                reportWtf("making Network Managment Service ready", e2222);
            }
        }
        CountDownLatch networkPolicyInitReadySignal = null;
        if (networkPolicyF != null) {
            networkPolicyInitReadySignal = networkPolicyF.networkScoreAndNetworkManagementServiceReady();
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkStatsServiceReady");
        if (networkStatsF != null) {
            try {
                networkStatsF.systemReady();
            } catch (Throwable e22222) {
                reportWtf("making Network Stats Service ready", e22222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeConnectivityServiceReady");
        if (connectivityF != null) {
            try {
                connectivityF.systemReady();
            } catch (Throwable e222222) {
                reportWtf("making Connectivity Service ready", e222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkPolicyServiceReady");
        if (networkPolicyF != null) {
            try {
                networkPolicyF.systemReady(networkPolicyInitReadySignal);
            } catch (Throwable e2222222) {
                reportWtf("making Network Policy Service ready", e2222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("StartWatchdog");
        Watchdog.getInstance().start();
        traceEnd();
        this.mPackageManagerService.waitForAppDataPrepared();
        traceBeginAndSlog("PhaseThirdPartyAppsCanStart");
        if (webviewPrep != null) {
            ConcurrentUtils.waitForFutureNoInterrupt(webviewPrep, "WebViewFactoryPreparation");
        }
        this.mSystemServiceManager.startBootPhase(600);
        traceEnd();
        traceBeginAndSlog("MakeLocationServiceReady");
        if (locationF != null) {
            try {
                locationF.systemRunning();
            } catch (Throwable e22222222) {
                reportWtf("Notifying Location Service running", e22222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeCountryDetectionServiceReady");
        if (countryDetectorF != null) {
            try {
                countryDetectorF.systemRunning();
            } catch (Throwable e222222222) {
                reportWtf("Notifying CountryDetectorService running", e222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkTimeUpdateReady");
        if (networkTimeUpdaterF != null) {
            try {
                networkTimeUpdaterF.systemRunning();
            } catch (Throwable e2222222222) {
                reportWtf("Notifying NetworkTimeService running", e2222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeCommonTimeManagementServiceReady");
        if (commonTimeMgmtServiceF != null) {
            try {
                commonTimeMgmtServiceF.systemRunning();
            } catch (Throwable e22222222222) {
                reportWtf("Notifying CommonTimeManagementService running", e22222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeInputManagerServiceReady");
        if (inputManagerF != null) {
            try {
                inputManagerF.systemRunning();
            } catch (Throwable e222222222222) {
                reportWtf("Notifying InputManagerService running", e222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeTelephonyRegistryReady");
        if (telephonyRegistryF != null) {
            try {
                telephonyRegistryF.systemRunning();
            } catch (Throwable e2222222222222) {
                reportWtf("Notifying TelephonyRegistry running", e2222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeMediaRouterServiceReady");
        if (mediaRouterF != null) {
            try {
                mediaRouterF.systemRunning();
            } catch (Throwable e22222222222222) {
                reportWtf("Notifying MediaRouterService running", e22222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeMmsServiceReady");
        if (mmsServiceF != null) {
            try {
                mmsServiceF.systemRunning();
            } catch (Throwable e222222222222222) {
                reportWtf("Notifying MmsService running", e222222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkScoreServiceReady");
        if (networkScoreF != null) {
            try {
                networkScoreF.systemRunning();
            } catch (Throwable e2222222222222222) {
                reportWtf("Notifying NetworkScoreService running", e2222222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("IncidentDaemonReady");
        try {
            IIncidentManager incident = IIncidentManager.Stub.asInterface(ServiceManager.checkService("incident"));
            if (incident != null) {
                incident.systemRunning();
            }
        } catch (Throwable e22222222222222222) {
            reportWtf("Notifying incident daemon running", e22222222222222222);
        }
        traceEnd();
        if (enableIaware) {
            try {
                ServiceManager.addService("multi_task", HwServiceFactory.getMultiTaskManagerService().getInstance(context));
            } catch (Throwable e222222222222222222) {
                reportWtf("starting MultiTaskManagerService", e222222222222222222);
            }
        } else {
            Slog.e(TAG, "can not start multitask because the prop is false");
        }
        if (HwPCUtils.enabled()) {
            traceBeginAndSlog("StartPCManagerService");
            HwServiceFactory.addHwPCManagerService(context, this.mActivityManagerService);
            traceEnd();
        }
        HwServiceFactory.addHwFmService(context);
    }

    /* synthetic */ void lambda$-com_android_server_SystemServer_99812() {
        Slog.i(TAG, "WebViewFactoryPreparation");
        BootTimingsTraceLog traceLog = new BootTimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin("WebViewFactoryPreparation");
        ConcurrentUtils.waitForFutureNoInterrupt(this.mZygotePreload, "Zygote preload");
        this.mZygotePreload = null;
        this.mWebViewUpdateService.prepareWebViewInSystemServer();
        traceLog.traceEnd();
    }

    private void startEmcomService() {
        Trace.traceBegin(524288, "EmcomManagerService");
        try {
            this.mSystemServiceManager.startService("com.android.server.emcom.EmcomManagerService");
        } catch (Exception e) {
            Slog.w(TAG, "EmcomManagerService not exists");
        }
        Trace.traceEnd(524288);
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

    private void initRogMode(WindowManagerService wm, Context context) {
        if (!(wm == null || SystemProperties.get("ro.runmode", "normal").equals("factory") || SystemProperties.getInt("persist.sys.aps.firstboot", 1) <= 0)) {
            int initWidth = SystemProperties.getInt("sys.rog.width", 0);
            int initHeight = SystemProperties.getInt("sys.rog.height", 0);
            int initDensity = SystemProperties.getInt("sys.rog.density", 0);
            if (!(initWidth == 0 || initHeight == 0 || initDensity == 0)) {
                SystemProperties.set("persist.sys.realdpi", Integer.toString(initDensity));
                SystemProperties.set("persist.sys.rog.width", Integer.toString(initWidth));
                SystemProperties.set("persist.sys.rog.height", Integer.toString(initHeight));
                Global.putString(context.getContentResolver(), "display_size_forced", initWidth + "," + initHeight);
                wm.setForcedDisplaySize(0, initWidth, initHeight);
                SystemProperties.set("persist.sys.rog.configmode", ENCRYPTED_STATE);
                Slog.d(TAG, "initRogMode and setForcedDisplaySize, initWidth = " + initWidth + " initHeight = " + initHeight);
            }
        }
    }

    private void restoreRogMode(WindowManagerService wm, Context context) {
        if (wm != null) {
            if (SystemProperties.getInt("persist.sys.rog.configmode", 0) == 1) {
                Slog.i(TAG, "rog 2.0 funciton is open, resotre it");
                SystemProperties.set("persist.sys.realdpi", "");
                SystemProperties.set("persist.sys.rog.width", "");
                SystemProperties.set("persist.sys.rog.height", "");
                Global.putString(context.getContentResolver(), "display_size_forced", "");
                wm.setForcedDisplayDensityForUser(0, DisplayMetrics.DENSITY_DEVICE, UserHandle.myUserId());
                SystemProperties.set("persist.sys.rog.configmode", "0");
            } else if (SystemProperties.getInt("persist.sys.rog.width", 0) != 0) {
                SystemProperties.set("persist.sys.rog.width", "");
                SystemProperties.set("persist.sys.rog.height", "");
            }
        }
    }

    private void startForceRotation(Context context) {
        if (HwFrameworkFactory.getForceRotationManager().isForceRotationSupported()) {
            try {
                Slog.i(TAG, "Force rotation Service, name = forceRotationService");
                IHwForceRotationManagerServiceWrapper ifrsw = HwServiceFactory.getForceRotationManagerServiceWrapper();
                if (ifrsw != null) {
                    ServiceManager.addService("forceRotationService", ifrsw.getServiceInstance(context, UiThread.getHandler()));
                }
            } catch (Throwable e) {
                reportWtf("starting Force rotation service", e);
            }
        }
    }
}
