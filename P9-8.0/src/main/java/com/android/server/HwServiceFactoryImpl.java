package com.android.server;

import android.accounts.AuthenticatorDescription;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskThumbnailInfo;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.encrypt.ISDCardCryptedHelper;
import android.encrypt.SDCardCryptedHelper;
import android.hardware.SensorManager;
import android.location.ILocationManager;
import android.net.INetworkStatsService;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.PowerManager.WakeLock;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.HwSysResManager;
import android.rms.iaware.NetLocationStrategy;
import android.service.notification.ZenModeConfig;
import android.service.voice.IVoiceInteractionSession;
import android.telephony.TelephonyManager;
import android.util.IntProperty;
import android.util.Slog;
import android.util.Spline;
import android.util.TrustedTime;
import android.view.Display;
import android.view.WindowManagerPolicy;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.HwServiceFactory.Factory;
import com.android.server.HwServiceFactory.IDisplayEffectMonitor;
import com.android.server.HwServiceFactory.IDisplayEngineInterface;
import com.android.server.HwServiceFactory.IHwActiveServices;
import com.android.server.HwServiceFactory.IHwActivityManagerService;
import com.android.server.HwServiceFactory.IHwActivityStackSupervisor;
import com.android.server.HwServiceFactory.IHwActivityStarter;
import com.android.server.HwServiceFactory.IHwAppOpsService;
import com.android.server.HwServiceFactory.IHwAttestationServiceFactory;
import com.android.server.HwServiceFactory.IHwAudioService;
import com.android.server.HwServiceFactory.IHwAutomaticBrightnessController;
import com.android.server.HwServiceFactory.IHwBinderMonitor;
import com.android.server.HwServiceFactory.IHwBluetoothBigDataService;
import com.android.server.HwServiceFactory.IHwBluetoothManagerService;
import com.android.server.HwServiceFactory.IHwDrmDialogService;
import com.android.server.HwServiceFactory.IHwFingerprintService;
import com.android.server.HwServiceFactory.IHwForceRotationManagerServiceWrapper;
import com.android.server.HwServiceFactory.IHwIMonitorManager;
import com.android.server.HwServiceFactory.IHwInputMethodManagerService;
import com.android.server.HwServiceFactory.IHwLocationManagerService;
import com.android.server.HwServiceFactory.IHwLockSettingsService;
import com.android.server.HwServiceFactory.IHwMediaSessionStack;
import com.android.server.HwServiceFactory.IHwNetworkManagermentService;
import com.android.server.HwServiceFactory.IHwNetworkPolicyManagerService;
import com.android.server.HwServiceFactory.IHwNetworkStatsService;
import com.android.server.HwServiceFactory.IHwNormalizedManualBrightnessController;
import com.android.server.HwServiceFactory.IHwNotificationManagerService;
import com.android.server.HwServiceFactory.IHwPowerManagerService;
import com.android.server.HwServiceFactory.IHwRampAnimator;
import com.android.server.HwServiceFactory.IHwSmartBackLightController;
import com.android.server.HwServiceFactory.IHwStorageManagerService;
import com.android.server.HwServiceFactory.IHwTelephonyRegistry;
import com.android.server.HwServiceFactory.IHwUserManagerService;
import com.android.server.HwServiceFactory.IHwWallpaperManagerService;
import com.android.server.HwServiceFactory.IHwWindowManagerService;
import com.android.server.HwServiceFactory.IHwWindowStateAnimator;
import com.android.server.HwServiceFactory.IHwZenModeFiltering;
import com.android.server.HwServiceFactory.IMultiTaskManagerServiceFactory;
import com.android.server.HwServiceFactory.ISystemBlockMonitor;
import com.android.server.accounts.HwAccountHelper;
import com.android.server.am.AbsHwMtmBroadcastResourceManager;
import com.android.server.am.ActiveServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityRecord;
import com.android.server.am.ActivityStack;
import com.android.server.am.ActivityStackSupervisor;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.am.ActivityStartInterceptor;
import com.android.server.am.ActivityStarter;
import com.android.server.am.HwActiveServices;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.HwActivityRecord;
import com.android.server.am.HwActivityStack;
import com.android.server.am.HwActivityStackSupervisor;
import com.android.server.am.HwActivityStartInterceptor;
import com.android.server.am.HwActivityStarter;
import com.android.server.am.HwBroadcastQueue;
import com.android.server.am.HwMtmBroadcastResourceManager;
import com.android.server.am.HwPowerInfoService;
import com.android.server.am.HwTaskRecord;
import com.android.server.am.IHwPowerInfoService;
import com.android.server.am.ProcessRecord;
import com.android.server.am.RecentTasks;
import com.android.server.am.TaskRecord;
import com.android.server.aps.HwApsManagerService;
import com.android.server.audio.AudioService;
import com.android.server.audio.HwAudioService;
import com.android.server.audio.HwMediaFocusControl;
import com.android.server.audio.MediaFocusControl;
import com.android.server.audio.PlayerFocusEnforcer;
import com.android.server.connectivity.HwNotificationTethering;
import com.android.server.connectivity.HwNotificationTetheringImpl;
import com.android.server.content.HwSyncManager;
import com.android.server.content.SyncManager;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.display.AutomaticBrightnessController;
import com.android.server.display.AutomaticBrightnessController.Callbacks;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.DisplayEngineService;
import com.android.server.display.DisplayPowerState;
import com.android.server.display.HwNormalizedAutomaticBrightnessController;
import com.android.server.display.HwNormalizedManualBrightnessController;
import com.android.server.display.HwNormalizedRampAnimator;
import com.android.server.display.HwSmartBackLightNormalizedController;
import com.android.server.display.HysteresisLevels;
import com.android.server.display.ManualBrightnessController;
import com.android.server.display.ManualBrightnessController.ManualBrightnessCallbacks;
import com.android.server.display.RampAnimator;
import com.android.server.forcerotation.HwForceRotationManagerService;
import com.android.server.input.HwInputManagerService;
import com.android.server.input.IHwInputManagerService;
import com.android.server.input.InputManagerService;
import com.android.server.lights.LightsManager;
import com.android.server.lights.LightsService;
import com.android.server.location.AgpsConnectProvider;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.HwCmccGpsFeature;
import com.android.server.location.HwGeocoderProxy;
import com.android.server.location.HwGnssLocationProvider;
import com.android.server.location.HwGpsActionReporter;
import com.android.server.location.HwGpsLocationCustFeature;
import com.android.server.location.HwGpsLocationManager;
import com.android.server.location.HwGpsLogServices;
import com.android.server.location.HwGpsXtraDownloadReceiver;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.location.HwLocationProviderProxy;
import com.android.server.location.IAgpsConnectProvider;
import com.android.server.location.IHwCmccGpsFeature;
import com.android.server.location.IHwGpsActionReporter;
import com.android.server.location.IHwGpsLocationCustFeature;
import com.android.server.location.IHwGpsLocationManager;
import com.android.server.location.IHwGpsLogServices;
import com.android.server.location.IHwGpsXtraDownloadReceiver;
import com.android.server.location.IHwLocalLocationProvider;
import com.android.server.location.LocationProviderProxy;
import com.android.server.media.HwMediaSessionStack;
import com.android.server.media.MediaSessionStack;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.net.HwNetworkPolicyManagerService;
import com.android.server.net.HwNetworkStatsService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsObservers;
import com.android.server.net.NetworkStatsService;
import com.android.server.net.NetworkStatsService.NetworkStatsSettings;
import com.android.server.notification.HwNotificationManagerService;
import com.android.server.notification.HwZenModeFiltering;
import com.android.server.notification.NotificationManagerService;
import com.android.server.notification.ValidateNotificationPeople;
import com.android.server.os.FreezeScreenWindowMonitor;
import com.android.server.os.IFreezeScreenWindowMonitor;
import com.android.server.pc.HwPCManagerService;
import com.android.server.pm.DefaultPermissionGrantPolicy;
import com.android.server.pm.HwDefaultPermissionGrantPolicy;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.pm.HwPackageServiceManager;
import com.android.server.pm.HwPackageServiceManagerImpl;
import com.android.server.pm.HwUserManagerService;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserDataPreparer;
import com.android.server.pm.UserManagerService;
import com.android.server.power.HwDisplayPowerController;
import com.android.server.power.HwPowerManagerService;
import com.android.server.power.HwShutdownThreadImpl;
import com.android.server.power.IHwShutdownThread;
import com.android.server.power.PowerManagerService;
import com.android.server.rms.IDaemonRecoverHandler;
import com.android.server.rms.IHwIpcChecker;
import com.android.server.rms.IHwIpcMonitor;
import com.android.server.rms.handler.DaemonRecoverHandler;
import com.android.server.rms.ipcchecker.HwIpcChecker;
import com.android.server.rms.ipcchecker.HwIpcMonitorImpl;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.security.securityprofile.SecurityProfileControllerImpl;
import com.android.server.statusbar.HwStatusBarManagerService;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.storage.HwDeviceStorageMonitorService;
import com.android.server.wallpaper.HwWallpaperManagerService;
import com.android.server.wallpaper.WallpaperManagerService;
import com.android.server.wm.AppTransition;
import com.android.server.wm.DisplayContent;
import com.android.server.wm.HwAppTransition;
import com.android.server.wm.HwAppTransitionImpl;
import com.android.server.wm.HwDisplayContent;
import com.android.server.wm.HwScreenRotationAnimationImpl;
import com.android.server.wm.HwTaskStack;
import com.android.server.wm.HwWindowManagerService;
import com.android.server.wm.HwWindowStateAnimator;
import com.android.server.wm.IHwAppTransition;
import com.android.server.wm.IHwScreenRotationAnimation;
import com.android.server.wm.TaskStack;
import com.android.server.wm.WallpaperController;
import com.android.server.wm.WindowLayersController;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import com.android.server.wm.WindowStateAnimator;
import com.huawei.android.hardware.fmradio.HwFmService;
import com.huawei.displayengine.DisplayEngineInterface;
import huawei.android.os.HwGeneralManager;
import java.io.File;
import java.util.ArrayList;

public class HwServiceFactoryImpl implements Factory {
    private static final String TAG = "HwServiceFactoryImpl";
    private static DisplayEngineInterface mDisplayEngineInterface = null;
    private static HWDrmDialogsService mDrmDialogService = null;
    private static HwSmartBackLightNormalizedController mHwSmartBackLightController = null;

    public static class DisplayEngineInterfaceImpl implements IDisplayEngineInterface {
        public void initialize() {
            if (HwServiceFactoryImpl.mDisplayEngineInterface == null) {
                HwServiceFactoryImpl.mDisplayEngineInterface = new DisplayEngineInterface();
            }
        }

        public boolean getSupported(String feature) {
            boolean z = false;
            if (HwServiceFactoryImpl.mDisplayEngineInterface == null) {
                return false;
            }
            if (HwServiceFactoryImpl.mDisplayEngineInterface.getSupported(feature) != 0) {
                z = true;
            }
            return z;
        }

        public void setScene(String scene, String action) {
            if (HwServiceFactoryImpl.mDisplayEngineInterface != null) {
                HwServiceFactoryImpl.mDisplayEngineInterface.setScene(scene, action);
            }
        }

        public void updateLightSensorState(boolean sensorEnable) {
            if (HwServiceFactoryImpl.mDisplayEngineInterface != null) {
                HwServiceFactoryImpl.mDisplayEngineInterface.updateLightSensorState(sensorEnable);
            }
        }
    }

    public static class HwActiveServicesImpl implements IHwActiveServices {
        public ActiveServices getInstance(ActivityManagerService ams) {
            return new HwActiveServices(ams);
        }
    }

    public static class HwActivityManagerServiceImpl implements IHwActivityManagerService {
        public ActivityManagerService getInstance(Context context) {
            Slog.i(HwServiceFactoryImpl.TAG, "HwActivityManagerServiceImpl getInstance starts");
            return new HwActivityManagerService(context);
        }
    }

    public static class HwActivityStackSupervisorImpl implements IHwActivityStackSupervisor {
        public ActivityStackSupervisor getInstance(ActivityManagerService service, Looper looper) {
            return new HwActivityStackSupervisor(service, looper);
        }
    }

    public static class HwActivityStarterImpl implements IHwActivityStarter {
        public ActivityStarter getInstance(ActivityManagerService service, ActivityStackSupervisor supervisor) {
            return new HwActivityStarter(service, supervisor);
        }
    }

    public static class HwAppOpsServiceImpl implements IHwAppOpsService {
        public AppOpsService getInstance(File storagePath) {
            return new HwAppOpsService(storagePath);
        }

        public AppOpsService getInstance(File storagePath, Handler handler) {
            return new HwAppOpsService(storagePath, handler);
        }
    }

    public static class HwAttestationServiceImpl implements IHwAttestationServiceFactory {
        public IBinder getInstance(Context context) {
            return new HwAttestationService(context);
        }
    }

    public static class HwAudioServiceImpl implements IHwAudioService {
        public AudioService getInstance(Context context) {
            return new HwAudioService(context);
        }
    }

    public static class HwAutomaticBrightnessControllerImpl implements IHwAutomaticBrightnessController {
        public AutomaticBrightnessController getInstance(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, Context context) {
            return new HwNormalizedAutomaticBrightnessController(callbacks, looper, sensorManager, autoBrightnessSpline, lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, context);
        }

        public AutomaticBrightnessController getInstance(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, int initialLightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, int ambientLightHorizon, float autoBrightnessAdjustmentMaxGamma, HysteresisLevels dynamicHysteresis, Context context) {
            return new HwNormalizedAutomaticBrightnessController(callbacks, looper, sensorManager, autoBrightnessSpline, lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounceConfig, darkeningLightDebounceConfig, resetAmbientLuxAfterWarmUpConfig, ambientLightHorizon, autoBrightnessAdjustmentMaxGamma, dynamicHysteresis, context);
        }
    }

    public static class HwBluetoothBigDataServiceImpl implements IHwBluetoothBigDataService {
        private HwBluetoothBigDataService mHwBluetoothBigDataService;

        private HwBluetoothBigDataService getInstance() {
            this.mHwBluetoothBigDataService = new HwBluetoothBigDataService();
            return this.mHwBluetoothBigDataService;
        }

        public void sendBigDataEvent(Context context, String bigDataEvent) {
            getInstance().sendBigDataEvent(context, bigDataEvent);
        }
    }

    public static class HwBluetoothManagerServiceImpl implements IHwBluetoothManagerService {
        public BluetoothManagerService createHwBluetoothManagerService(Context context) {
            return new HwBluetoothManagerService(context);
        }
    }

    public static class HwDrmDialogServiceImpl implements IHwDrmDialogService {
        public void startDrmDialogService(Context context) {
            if (HwServiceFactoryImpl.mDrmDialogService == null) {
                HwServiceFactoryImpl.mDrmDialogService = new HWDrmDialogsService(context);
                HwServiceFactoryImpl.mDrmDialogService.start();
            }
        }
    }

    public static class HwFingerprintServiceImpl implements IHwFingerprintService {
        public Class<SystemService> createServiceClass() {
            try {
                return Class.forName("com.android.server.fingerprint.HwFingerprintService");
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    public static class HwForceRotationManagerServiceWrapper implements IHwForceRotationManagerServiceWrapper {
        public HwForceRotationManagerService getServiceInstance(Context context, Handler uiHandler) {
            return new HwForceRotationManagerService(context, uiHandler);
        }
    }

    public static class HwIMonitorManagerImpl implements IHwIMonitorManager {
        private HwIMonitorManager mHwIMonitorManager;

        private HwIMonitorManager getInstance() {
            this.mHwIMonitorManager = new HwIMonitorManager();
            return this.mHwIMonitorManager;
        }

        public boolean uploadBtRadarEvent(int event, String exception) {
            return getInstance().uploadBtRadarEvent(event, exception);
        }
    }

    public static class HwInputManagerServiceImpl implements IHwInputManagerService {
        public HwInputManagerService getInstance(Context context, Handler handler) {
            Slog.i(HwServiceFactoryImpl.TAG, "Input Manager ,getInstance");
            return new HwInputManagerService(context, handler);
        }
    }

    public static class HwInputMethodManagerServiceImpl implements IHwInputMethodManagerService {
        public InputMethodManagerService getInstance(Context context) {
            return new HwInputMethodManagerService(context);
        }
    }

    public static class HwLocationManagerServiceImpl implements IHwLocationManagerService {
        public LocationManagerService getInstance(Context context) {
            return new HwLocationManagerService(context);
        }
    }

    public static class HwLockSettingsServiceImpl implements IHwLockSettingsService {
        public LockSettingsService getInstance(Context context) {
            return new HwLockSettingsService(context);
        }
    }

    public static class HwMediaSessionStackImpl implements IHwMediaSessionStack {
        public MediaSessionStack getInstance(Context context) {
            return new HwMediaSessionStack(context);
        }
    }

    public static class HwNetworkManagementServiceImpl implements IHwNetworkManagermentService {
        private HwNetworkManagementService mHwNMService;

        public NetworkManagementService getInstance(Context context, String socket) {
            this.mHwNMService = new HwNetworkManagementService(context, socket);
            return this.mHwNMService;
        }

        public void setNativeDaemonConnector(Object service, Object connect) {
            if ((service instanceof HwNetworkManagementService) && (connect instanceof NativeDaemonConnector)) {
                ((HwNetworkManagementService) service).setConnector((NativeDaemonConnector) connect);
            }
        }

        public void startAccessPointWithChannel(WifiConfiguration wifiConfig, String wlanIface) {
            this.mHwNMService.startAccessPointWithChannel(wifiConfig, wlanIface);
        }

        public void sendDataSpeedSlowMessage(String[] cooked, String raw) {
            this.mHwNMService.sendDataSpeedSlowMessage(cooked, raw);
        }

        public void sendWebStatMessage(String[] cooked, String raw) {
            this.mHwNMService.sendWebStatMessage(cooked, raw);
        }

        public boolean handleApLinkedStaListChange(String raw, String[] cooked) {
            return this.mHwNMService.handleApLinkedStaListChange(raw, cooked);
        }

        public void sendApkDownloadUrlBroadcast(String[] cooked, String raw) {
            this.mHwNMService.sendApkDownloadUrlBroadcast(cooked, raw);
        }
    }

    public static class HwNetworkPolicyManagerServiceImpl implements IHwNetworkPolicyManagerService {
        public NetworkPolicyManagerService getInstance(Context context, IActivityManager activityManager, IPowerManager powerManager, INetworkStatsService networkStats, INetworkManagementService networkManagement) {
            Slog.i(HwServiceFactoryImpl.TAG, "HwNetworkPolicyManagerService created");
            return new HwNetworkPolicyManagerService(context, activityManager, powerManager, networkStats, networkManagement);
        }
    }

    public static class HwNetworkStatsServiceImpl implements IHwNetworkStatsService {
        public NetworkStatsService getInstance(Context context, INetworkManagementService networkManager, AlarmManager alarmManager, WakeLock wakeLock, TrustedTime time, TelephonyManager teleManager, NetworkStatsSettings settings, NetworkStatsObservers statsObservers, File systemDir, File baseDir) {
            return new HwNetworkStatsService(context, networkManager, alarmManager, wakeLock, time, teleManager, settings, statsObservers, systemDir, baseDir);
        }
    }

    public static class HwNormalizedManualBrightnessControllerImpl implements IHwNormalizedManualBrightnessController {
        public ManualBrightnessController getInstance(ManualBrightnessCallbacks callbacks, Context context, SensorManager sensorManager) {
            return new HwNormalizedManualBrightnessController(callbacks, context, sensorManager);
        }
    }

    public static class HwNormalizedRampAnimatorImpl implements IHwRampAnimator {
        public RampAnimator<DisplayPowerState> getInstance(DisplayPowerState object, IntProperty<DisplayPowerState> property) {
            return new HwNormalizedRampAnimator(object, property);
        }
    }

    public static class HwNotificationManagerServiceImpl implements IHwNotificationManagerService {
        public NotificationManagerService getInstance(Context context, StatusBarManagerService statusBar, LightsService lights) {
            return new HwNotificationManagerService(context, statusBar, lights);
        }
    }

    public static class HwPowerManagerServiceImpl implements IHwPowerManagerService {
        public PowerManagerService getInstance(Context context) {
            return new HwPowerManagerService(context);
        }
    }

    public static class HwSmartBackLightControllerImpl implements IHwSmartBackLightController {
        public boolean checkIfUsingHwSBL() {
            return HwSmartBackLightNormalizedController.checkIfUsingHwSBL();
        }

        public void StartHwSmartBackLightController(Context context, LightsManager lightsManager, SensorManager sensorManager) {
            if (HwServiceFactoryImpl.mHwSmartBackLightController == null) {
                HwServiceFactoryImpl.mHwSmartBackLightController = new HwSmartBackLightNormalizedController(context, lightsManager, sensorManager);
            }
        }

        public void updatePowerState(int state, boolean useSmartBacklight) {
            if (HwServiceFactoryImpl.mHwSmartBackLightController != null) {
                HwServiceFactoryImpl.mHwSmartBackLightController.updatePowerState(state, useSmartBacklight);
            }
        }

        public void updateBrightnessState(int state) {
            if (HwServiceFactoryImpl.mHwSmartBackLightController != null) {
                HwServiceFactoryImpl.mHwSmartBackLightController.updateBrightnessState(state);
            }
        }
    }

    public static class HwStorageManagerServiceImpl implements IHwStorageManagerService {
        public StorageManagerService getInstance(Context context) {
            Slog.i(HwServiceFactoryImpl.TAG, "HwStorageManagerServiceImpl getInstance starts");
            return new HwStorageManagerService(context);
        }
    }

    public static class HwTelephonyRegistryImpl implements IHwTelephonyRegistry {
        public TelephonyRegistry getInstance(Context context) {
            return new StubTelephonyRegistry(context);
        }
    }

    public static class HwUserManagerServiceImpl implements IHwUserManagerService {
        public UserManagerService getInstance(Context context, PackageManagerService pm, UserDataPreparer userDataPreparer, Object packagesLock) {
            return new HwUserManagerService(context, pm, userDataPreparer, packagesLock);
        }
    }

    public static class HwWallpaperManagerServiceImpl implements IHwWallpaperManagerService {
        public WallpaperManagerService getInstance(Context context) {
            return new HwWallpaperManagerService(context);
        }
    }

    public static class HwWindowManagerServiceImpl implements IHwWindowManagerService {
        public WindowManagerService getInstance(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
            return new HwWindowManagerService(context, inputManager, haveInputMethods, showBootMsgs, onlyCore, policy);
        }
    }

    public static class HwWindowStateAnimatorImpl implements IHwWindowStateAnimator {
        public WindowStateAnimator getInstance(WindowState win) {
            return new HwWindowStateAnimator(win);
        }
    }

    public static class HwZenModeFilteringImpl implements IHwZenModeFiltering {
        public boolean matchesCallFilter(Context context, int zen, ZenModeConfig config, UserHandle userHandle, Bundle extras, ValidateNotificationPeople validator, int contactsTimeoutMs, float timeoutAffinity) {
            return HwZenModeFiltering.matchesCallFilter(context, zen, config, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
        }
    }

    public static class MultiTaskManagerServiceImpl implements IMultiTaskManagerServiceFactory {
        public IBinder getInstance(Context context) {
            return new MultiTaskManagerService(context);
        }
    }

    public IHwAppOpsService getHwAppOpsService() {
        return new HwAppOpsServiceImpl();
    }

    public IHwZenModeFiltering getHwZenModeFiltering() {
        return new HwZenModeFilteringImpl();
    }

    public IHwWallpaperManagerService getHuaweiWallpaperManagerService() {
        return new HwWallpaperManagerServiceImpl();
    }

    public String getWallpaperManagerServiceClassName() {
        return "com.android.server.wallpaper.HwWallpaperManagerService$Lifecycle";
    }

    public IHwPowerManagerService getHwPowerManagerService() {
        return new HwPowerManagerServiceImpl();
    }

    public IHwNotificationManagerService getHwNotificationManagerService() {
        return new HwNotificationManagerServiceImpl();
    }

    public IHwNetworkManagermentService getHwNetworkManagermentService() {
        return new HwNetworkManagementServiceImpl();
    }

    public IHwNetworkPolicyManagerService getHwNetworkPolicyManagerService() {
        return new HwNetworkPolicyManagerServiceImpl();
    }

    public IHwInputMethodManagerService getHwInputMethodManagerService() {
        return new HwInputMethodManagerServiceImpl();
    }

    public IHwActivityStackSupervisor getHwActivityStackSupervisor() {
        return new HwActivityStackSupervisorImpl();
    }

    public IHwActivityStarter getHwActivityStarter() {
        return new HwActivityStarterImpl();
    }

    public IHwTelephonyRegistry getHwTelephonyRegistry() {
        return new HwTelephonyRegistryImpl();
    }

    public IHwAudioService getHwAudioService() {
        return new HwAudioServiceImpl();
    }

    public IHwBluetoothManagerService getHwBluetoothManagerService() {
        return new HwBluetoothManagerServiceImpl();
    }

    public IHwActivityManagerService getHwActivityManagerService() {
        Slog.i(TAG, "getHwActivityManagerService starts");
        return new HwActivityManagerServiceImpl();
    }

    public IHwStorageManagerService getHwStorageManagerService() {
        Slog.i(TAG, "getHwStorageManagerService starts");
        return new HwStorageManagerServiceImpl();
    }

    public IHwShutdownThread getHwShutdownThreadImpl() {
        return new HwShutdownThreadImpl();
    }

    public HwConnectivityManager getHwConnectivityManager() {
        return HwConnectivityManagerImpl.getDefault();
    }

    public IHwLockSettingsService getHuaweiLockSettingsService() {
        return new HwLockSettingsServiceImpl();
    }

    public PackageManagerService getHuaweiPackageManagerService(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        return HwPackageManagerService.getInstance(context, installer, factoryTest, onlyCore);
    }

    public DefaultPermissionGrantPolicy getHwDefaultPermissionGrantPolicy(Context context, PackageManagerService service) {
        return new HwDefaultPermissionGrantPolicy(context, service);
    }

    public IHwWindowStateAnimator getHuaweiWindowStateAnimator() {
        return new HwWindowStateAnimatorImpl();
    }

    public IHwMediaSessionStack getHuaweiMediaSessionStack() {
        return new HwMediaSessionStackImpl();
    }

    public IHwWindowManagerService getHuaweiWindowManagerService() {
        return new HwWindowManagerServiceImpl();
    }

    public IHwUserManagerService getHwUserManagerService() {
        return new HwUserManagerServiceImpl();
    }

    public StatusBarManagerService createHwStatusBarManagerService(Context context, WindowManagerService windowManager) {
        return new HwStatusBarManagerService(context, windowManager);
    }

    public IHwActiveServices getHwActiveServices() {
        return new HwActiveServicesImpl();
    }

    public void setAlarmService(AlarmManagerService alarm) {
        SMCSAMSHelper.getInstance().setAlarmService(alarm);
    }

    public boolean isPrivAppInData(File path, String apkListFile) {
        return HwPackageManagerService.isPrivAppInData(path, apkListFile);
    }

    public boolean isPrivAppNonSystemPartitionDir(File path) {
        return HwPackageManagerService.isPrivAppNonSystemPartitionDir(path);
    }

    public boolean isPrivAppInCust(File file) {
        return HwPackageManagerService.isPrivAppInCust(file);
    }

    public boolean isCustedCouldStopped(String pkg, boolean block, boolean stopped) {
        return HwPackageManagerService.isCustedCouldStopped(pkg, block, stopped);
    }

    public boolean shouldFilteInvalidSensorVal(float lux) {
        return HwDisplayPowerController.shouldFilteInvalidSensorVal(lux);
    }

    public void setIfCoverClosed(boolean isClosed) {
        HwDisplayPowerController.setIfCoverClosed(isClosed);
    }

    public boolean isCoverClosed() {
        return HwDisplayPowerController.isCoverClosed();
    }

    public boolean clearWipeDataFactoryLowlevel(Context context, Intent intent) {
        return HwRecoverySystemHelper.clearWipeDataFactoryLowlevel(context, intent);
    }

    public boolean clearWipeDataFactory(Context context, Intent intent) {
        return HwRecoverySystemHelper.clearWipeDataFactory(context, intent);
    }

    public void activePlaceFile() {
        HWDataSpaceHolder.activePlaceFile();
    }

    public HwNLPManager getHwNLPManager() {
        return HwNLPManagerImpl.getDefault();
    }

    public void setupHwServices(Context context) {
        HwGeneralService generalService;
        if (StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get("ro.config.hw_eapsim", StorageUtils.SDCARD_RWMOUNTED_STATE))) {
            try {
                Slog.i(TAG, "Smartcard System Service");
                ServiceManager.addService("smartcardservice", new SmartcardSystemService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "Failure starting Smartcard System Service", e);
            }
        }
        Slog.i(TAG, "Hw Connectivity Service");
        HwConnectivityExService connectivity = new HwConnectivityExService(context);
        if (connectivity != null) {
            ServiceManager.addService("hwConnectivityExService", connectivity);
        }
        try {
            generalService = new HwGeneralService(context, UiThread.getHandler());
            Slog.i(TAG, "General Service");
            ServiceManager.addService("hwGeneralService", generalService);
        } catch (Throwable e2) {
            Slog.e(TAG, "Failure starting generalService", e2);
        }
        try {
            Slog.i(TAG, "smartdisplay Service");
            HwSmartDisplayService smartdisplay = new HwSmartDisplayService(context);
            boolean isSupport_HIACE_ARSR1P = (smartdisplay.getDisplayEffectSupported(0) & 17) != 0;
            if ((StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get("ro.config.eyesprotect_support", StorageUtils.SDCARD_RWMOUNTED_STATE)) && smartdisplay.isFeatureSupported(1)) || smartdisplay.isFeatureSupported(2) || StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get("ro.config.hw_displayeffect_en", StorageUtils.SDCARD_RWMOUNTED_STATE)) || isSupport_HIACE_ARSR1P) {
                ServiceManager.addService("smartDisplay_service", smartdisplay);
            }
        } catch (Throwable th) {
            Slog.i(TAG, "smartdisplay_service failed");
        }
        if (HwGeneralManager.getInstance().isSupportForce()) {
            try {
                generalService = new HwGeneralService(context, UiThread.getHandler());
                if (generalService != null) {
                    Slog.i(TAG, "General Service");
                    ServiceManager.addService("hwGeneralService", generalService);
                }
            } catch (Throwable e22) {
                Slog.e(TAG, "Failure starting generalService", e22);
            }
        }
        int apsSupportValue = SystemProperties.getInt("sys.aps.support", 0);
        if (apsSupportValue != 268435456) {
            try {
                HwApsManagerService apsService = new HwApsManagerService(context);
                ServiceManager.addService("aps_service", apsService);
                apsService.systemReady();
            } catch (Throwable e222) {
                Slog.e(TAG, "Failure starting apsService", e222);
            }
        } else {
            Slog.e(TAG, "Not support APS! sys.aps.support=" + apsSupportValue);
        }
        try {
            ServiceManager.addService("DisplayEngineExService", new DisplayEngineService(context));
            Slog.i(TAG, "[effect] DisplayEngineServiceEx success");
        } catch (Throwable e2222) {
            Slog.e(TAG, "[effect] Failed to start DisplayEngineServiceEx, " + e2222.getMessage());
        }
    }

    public HwNativeDaemonConnector getHwNativeDaemonConnector() {
        return HwNativeDaemonConnectorImpl.getInstance();
    }

    public HwPackageServiceManager getHwPackageServiceManager() {
        return HwPackageServiceManagerImpl.getDefault();
    }

    public GnssLocationProvider createHwGnssLocationProvider(Context context, ILocationManager ilocationManager, Looper looper) {
        return new HwGnssLocationProvider(context, ilocationManager, looper);
    }

    public SyncManager createHwSyncManager(Context context, boolean factoryTest) {
        return new HwSyncManager(context, factoryTest);
    }

    public IHwAppTransition getHwAppTransitionImpl() {
        return new HwAppTransitionImpl();
    }

    public IHwDrmDialogService getHwDrmDialogService() {
        return new HwDrmDialogServiceImpl();
    }

    public IHwAttestationServiceFactory getHwAttestationService() {
        return new HwAttestationServiceImpl();
    }

    public IMultiTaskManagerServiceFactory getMultiTaskManagerService() {
        return new MultiTaskManagerServiceImpl();
    }

    public boolean removeProtectAppInPrivacyMode(AuthenticatorDescription desc, boolean removed, Context context) {
        return HwAccountHelper.removeProtectAppInPrivacyMode(desc, removed, context);
    }

    public IHwInputManagerService getHwInputManagerService() {
        return new HwInputManagerServiceImpl();
    }

    public String getDeviceStorageMonitorServiceClassName() {
        return HwDeviceStorageMonitorService.class.getName();
    }

    public HwNotificationTethering getHwNotificationTethering(Context context) {
        return new HwNotificationTetheringImpl(context);
    }

    public ActivityRecord createActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityContainer container, ActivityOptions options, ActivityRecord sourceRecord) {
        return new HwActivityRecord(_service, _caller, _launchedFromPid, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultTo, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisor, container, options, sourceRecord);
    }

    public ActivityStartInterceptor createActivityStartInterceptor(ActivityManagerService _service, ActivityStackSupervisor _supervisor) {
        return new HwActivityStartInterceptor(_service, _supervisor);
    }

    public AppTransition createHwAppTransition(Context context, WindowManagerService w) {
        return new HwAppTransition(context, w);
    }

    public TaskStack createTaskStack(WindowManagerService service, int stackId) {
        return new HwTaskStack(service, stackId);
    }

    public DisplayContent createDisplayContent(Display display, WindowManagerService service, WindowLayersController layersController, WallpaperController wallpaperController) {
        return new HwDisplayContent(display, service, layersController, wallpaperController);
    }

    public TaskRecord createTaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, IVoiceInteractionSession _voiceSession, IVoiceInteractor _voiceInteractor, int type) {
        return new HwTaskRecord(service, _taskId, info, _intent, _voiceSession, _voiceInteractor, type);
    }

    public TaskRecord createTaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, TaskDescription _taskDescription, TaskThumbnailInfo thumbnailInfo) {
        return new HwTaskRecord(service, _taskId, info, _intent, _taskDescription, thumbnailInfo);
    }

    public TaskRecord createTaskRecord(ActivityManagerService service, int _taskId, Intent _intent, Intent _affinityIntent, String _affinity, String _rootAffinity, ComponentName _realActivity, ComponentName _origActivity, boolean _rootWasReset, boolean _autoRemoveRecents, boolean _askedCompatMode, int _taskType, int _userId, int _effectiveUid, String _lastDescription, ArrayList<ActivityRecord> activities, long _firstActiveTime, long _lastActiveTime, long lastTimeMoved, boolean neverRelinquishIdentity, TaskDescription _lastTaskDescription, TaskThumbnailInfo lastThumbnailInfo, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean privileged, boolean _realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        return new HwTaskRecord(service, _taskId, _intent, _affinityIntent, _affinity, _rootAffinity, _realActivity, _origActivity, _rootWasReset, _autoRemoveRecents, _askedCompatMode, _taskType, _userId, _effectiveUid, _lastDescription, activities, _firstActiveTime, _lastActiveTime, lastTimeMoved, neverRelinquishIdentity, _lastTaskDescription, lastThumbnailInfo, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, privileged, _realActivitySuspended, userSetupComplete, minWidth, minHeight);
    }

    public ActivityStack createActivityStack(ActivityContainer activityContainer, RecentTasks recentTasks, boolean onTop) {
        return new HwActivityStack(activityContainer, recentTasks, onTop);
    }

    public IHwSmartBackLightController getHwSmartBackLightController() {
        return new HwSmartBackLightControllerImpl();
    }

    public IDisplayEngineInterface getDisplayEngineInterface() {
        return new DisplayEngineInterfaceImpl();
    }

    public IHwAutomaticBrightnessController getHuaweiAutomaticBrightnessController() {
        return new HwAutomaticBrightnessControllerImpl();
    }

    public IDisplayEffectMonitor getDisplayEffectMonitor(Context context) {
        return DisplayEffectMonitor.getInstance(context);
    }

    public IHwNormalizedManualBrightnessController getHuaweiManualBrightnessController() {
        return new HwNormalizedManualBrightnessControllerImpl();
    }

    public IHwBluetoothBigDataService getHwBluetoothBigDataService() {
        return new HwBluetoothBigDataServiceImpl();
    }

    public IHwIMonitorManager getHwIMonitorManager() {
        return new HwIMonitorManagerImpl();
    }

    public IHwRampAnimator getHwNormalizedRampAnimator() {
        return new HwNormalizedRampAnimatorImpl();
    }

    public IHwLocationManagerService getHwLocationManagerService() {
        return new HwLocationManagerServiceImpl();
    }

    public IHwLocalLocationProvider getHwLocalLocationProvider(Context context, ILocationManager ilocationManager) {
        return HwLocalLocationProvider.getInstance(context, ilocationManager);
    }

    public IHwCmccGpsFeature getHwCmccGpsFeature(Context context, GnssLocationProvider gnssLocationProvider) {
        return new HwCmccGpsFeature(context, gnssLocationProvider);
    }

    public IHwGpsLogServices getHwGpsLogServices(Context context) {
        return HwGpsLogServices.getInstance(context);
    }

    public IHwGpsLogServices getNewHwGpsLogService() {
        return HwGpsLogServices.getGpsLogService();
    }

    public IHwGpsLocationCustFeature getHwGpsLocationCustFeature() {
        return new HwGpsLocationCustFeature();
    }

    public IHwGpsXtraDownloadReceiver getHwGpsXtraDownloadReceiver() {
        return new HwGpsXtraDownloadReceiver();
    }

    public IHwGpsLocationManager getHwGpsLocationManager(Context context) {
        return HwGpsLocationManager.getInstance(context);
    }

    public IHwFingerprintService getHwFingerprintService() {
        return new HwFingerprintServiceImpl();
    }

    public AbsHwMtmBroadcastResourceManager getMtmBRManagerImpl(HwBroadcastQueue queue) {
        return new HwMtmBroadcastResourceManager(queue);
    }

    public ISystemBlockMonitor getISystemBlockMonitor() {
        return SystemBlockMonitor.getInstance();
    }

    public IHwBinderMonitor getIHwBinderMonitor() {
        return new HwBinderMonitor();
    }

    public IHwIpcChecker getIHwIpcChecker(Object object, Handler handler, long waitMaxMillis) {
        return new HwIpcChecker(object, handler, waitMaxMillis);
    }

    public IHwIpcMonitor getIHwIpcMonitor(Object object, String type, String name) {
        return HwIpcMonitorImpl.getHwIpcMonitor(object, type, name);
    }

    public IDaemonRecoverHandler getIDaemonRecoverHandler() {
        return new DaemonRecoverHandler();
    }

    public IHwGpsActionReporter getHwGpsActionReporter(Context context, ILocationManager iLocationManager) {
        return HwGpsActionReporter.getInstance(context, iLocationManager);
    }

    public IFreezeScreenWindowMonitor getWinFreezeScreenMonitor() {
        return FreezeScreenWindowMonitor.getInstance();
    }

    public LocationProviderProxy locationProviderProxyCreateAndBind(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        return HwLocationProviderProxy.createAndBind(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
    }

    public GeocoderProxy geocoderProxyCreateAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        return HwGeocoderProxy.createAndBind(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
    }

    public void addHwFmService(Context context) {
        ServiceManager.addService("hwfm_service", new HwFmService(context));
    }

    public IHwPowerInfoService getHwPowerInfoService(Context context, boolean isSystemReady) {
        return HwPowerInfoService.getInstance(context, isSystemReady);
    }

    public IHwNetworkStatsService getHwNetworkStatsService() {
        return new HwNetworkStatsServiceImpl();
    }

    public ISDCardCryptedHelper getSDCardCryptedHelper() {
        return new SDCardCryptedHelper();
    }

    public IAgpsConnectProvider getAgpsConnectProvider(Context context) {
        return AgpsConnectProvider.createAgpsConnectProvider(context);
    }

    public void reportProximitySensorEventToIAware(boolean positive) {
        HwSysResManager.getInstance().reportProximitySensorEventToIAware(positive);
    }

    public void addHwPCManagerService(Context context, ActivityManagerService ams) {
        Slog.i(TAG, "PCManager, addService");
        ServiceManager.addService("hwPcManager", new HwPCManagerService(context, ams));
    }

    public void reportVibratorToIAware(int uid) {
        HwSysResManager.getInstance().reportVibratorToIAware(uid);
    }

    public IHwForceRotationManagerServiceWrapper getForceRotationManagerServiceWrapper() {
        return new HwForceRotationManagerServiceWrapper();
    }

    public MediaFocusControl getHwMediaFocusControl(Context cntxt, PlayerFocusEnforcer pfe) {
        return new HwMediaFocusControl(cntxt, pfe);
    }

    public void reportMediaKeyToIAware(int uid) {
        HwSysResManager.getInstance().reportMediaKeyToIAware(uid);
    }

    public ISecurityProfileController getSecurityProfileController() {
        return new SecurityProfileControllerImpl();
    }

    public IHwScreenRotationAnimation getHwScreenRotationAnimation() {
        return new HwScreenRotationAnimationImpl();
    }

    public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        return HwSysResManager.getInstance().getNetLocationStrategy(pkgName, uid, type);
    }
}
