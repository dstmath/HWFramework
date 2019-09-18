package com.android.server;

import android.accounts.AuthenticatorDescription;
import android.app.ActivityManager;
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
import android.hardware.display.HwFoldScreenState;
import android.location.ILocationManager;
import android.media.AudioAttributes;
import android.media.AudioFocusInfo;
import android.media.IAudioFocusDispatcher;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.PowerManager;
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
import android.view.Display;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.HwServiceFactory;
import com.android.server.NetworkManagementService;
import com.android.server.accounts.HwAccountHelper;
import com.android.server.am.AbsHwMtmBroadcastResourceManager;
import com.android.server.am.ActiveServices;
import com.android.server.am.ActivityDisplay;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityRecord;
import com.android.server.am.ActivityStack;
import com.android.server.am.ActivityStackSupervisor;
import com.android.server.am.ActivityStartController;
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
import com.android.server.am.HwTaskRecord;
import com.android.server.am.ProcessRecord;
import com.android.server.am.TaskRecord;
import com.android.server.aps.HwApsManagerService;
import com.android.server.audio.AudioService;
import com.android.server.audio.FocusRequester;
import com.android.server.audio.HwAudioService;
import com.android.server.audio.HwFocusRequester;
import com.android.server.audio.HwMediaFocusControl;
import com.android.server.audio.MediaFocusControl;
import com.android.server.audio.PlayerFocusEnforcer;
import com.android.server.camera.HwCameraServiceProxy;
import com.android.server.camera.IHwCameraServiceProxy;
import com.android.server.connectivity.HwNotificationTethering;
import com.android.server.connectivity.HwNotificationTetheringImpl;
import com.android.server.content.HwSyncManager;
import com.android.server.content.SyncManager;
import com.android.server.display.AutomaticBrightnessController;
import com.android.server.display.BrightnessMappingStrategy;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.DisplayEngineService;
import com.android.server.display.DisplayPowerState;
import com.android.server.display.HwNormalizedAutomaticBrightnessController;
import com.android.server.display.HwNormalizedManualBrightnessController;
import com.android.server.display.HwNormalizedRampAnimator;
import com.android.server.display.HwPersistentDataStoreEx;
import com.android.server.display.HwSmartBackLightNormalizedController;
import com.android.server.display.HwUibcReceiver;
import com.android.server.display.HwWifiDisplayAdapterEx;
import com.android.server.display.HwWifiDisplayControllerEx;
import com.android.server.display.HysteresisLevels;
import com.android.server.display.IHwPersistentDataStoreEx;
import com.android.server.display.IHwWifiDisplayAdapterEx;
import com.android.server.display.IHwWifiDisplayControllerEx;
import com.android.server.display.IPersistentDataStoreInner;
import com.android.server.display.IWifiDisplayAdapterInner;
import com.android.server.display.IWifiDisplayControllerInner;
import com.android.server.display.ManualBrightnessController;
import com.android.server.display.RampAnimator;
import com.android.server.forcerotation.HwForceRotationManagerService;
import com.android.server.fsm.HwFoldScreenStateImpl;
import com.android.server.input.HwInputManagerService;
import com.android.server.input.IHwInputManagerService;
import com.android.server.input.InputManagerService;
import com.android.server.lights.LightsManager;
import com.android.server.lights.LightsService;
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
import com.android.server.location.HwLbsLogger;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.location.HwLocationProviderProxy;
import com.android.server.location.IHwCmccGpsFeature;
import com.android.server.location.IHwGpsActionReporter;
import com.android.server.location.IHwGpsLocationCustFeature;
import com.android.server.location.IHwGpsLocationManager;
import com.android.server.location.IHwGpsLogServices;
import com.android.server.location.IHwGpsXtraDownloadReceiver;
import com.android.server.location.IHwLocalLocationProvider;
import com.android.server.location.LocationProviderProxy;
import com.android.server.locksettings.HwLockSettingsService;
import com.android.server.locksettings.LockSettingsService;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.net.HwNetworkPolicyManagerService;
import com.android.server.net.HwNetworkStatsService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsObservers;
import com.android.server.net.NetworkStatsService;
import com.android.server.notification.HwNotificationManagerService;
import com.android.server.notification.HwZenModeFiltering;
import com.android.server.notification.NotificationManagerService;
import com.android.server.notification.ValidateNotificationPeople;
import com.android.server.os.FreezeScreenWindowMonitor;
import com.android.server.os.IFreezeScreenWindowMonitor;
import com.android.server.pc.HwPCManagerService;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.pm.HwPackageManagerServiceEx;
import com.android.server.pm.HwPackageServiceManager;
import com.android.server.pm.HwPackageServiceManagerImpl;
import com.android.server.pm.HwUserManagerService;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserDataPreparer;
import com.android.server.pm.UserManagerService;
import com.android.server.pm.permission.DefaultPermissionGrantPolicy;
import com.android.server.pm.permission.HwDefaultPermissionGrantPolicy;
import com.android.server.pm.permission.PermissionManagerService;
import com.android.server.policy.HWExtMotionRotationProcessorEx;
import com.android.server.policy.IHWExtMotionRotationProcessor;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.HwDisplayPowerController;
import com.android.server.power.HwPowerManagerService;
import com.android.server.power.HwShutdownThreadImpl;
import com.android.server.power.IHwShutdownThread;
import com.android.server.power.PowerManagerService;
import com.android.server.rms.IHwIpcChecker;
import com.android.server.rms.IHwIpcMonitor;
import com.android.server.rms.ipcchecker.HwIpcChecker;
import com.android.server.rms.ipcchecker.HwIpcMonitorImpl;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.security.securityprofile.SecurityProfileControllerImpl;
import com.android.server.security.trustspace.ITrustSpaceController;
import com.android.server.security.trustspace.TrustSpaceControllerImpl;
import com.android.server.statusbar.HwStatusBarManagerService;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.storage.HwDeviceStorageMonitorService;
import com.android.server.wallpaper.HwWallpaperManagerService;
import com.android.server.wallpaper.WallpaperManagerService;
import com.android.server.wifipro.IHwWifiProCommonUtilsEx;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.AppTransition;
import com.android.server.wm.DisplayContent;
import com.android.server.wm.DisplayWindowController;
import com.android.server.wm.HwAppTransition;
import com.android.server.wm.HwAppTransitionImpl;
import com.android.server.wm.HwAppWindowContainerControllerImpl;
import com.android.server.wm.HwDisplayContent;
import com.android.server.wm.HwScreenRotationAnimationImpl;
import com.android.server.wm.HwTaskStack;
import com.android.server.wm.HwWindowManagerService;
import com.android.server.wm.HwWindowStateAnimator;
import com.android.server.wm.IHwAppTransition;
import com.android.server.wm.IHwAppWindowContainerController;
import com.android.server.wm.IHwScreenRotationAnimation;
import com.android.server.wm.StackWindowController;
import com.android.server.wm.TaskStack;
import com.android.server.wm.WallpaperController;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import com.android.server.wm.WindowStateAnimator;
import com.android.server.zrhung.IZRHungService;
import com.android.server.zrhung.ZRHungService;
import com.huawei.android.hardware.fmradio.HwFmService;
import com.huawei.displayengine.DisplayEngineInterface;
import huawei.android.os.HwGeneralManager;
import java.io.File;
import java.time.Clock;
import java.util.ArrayList;

public class HwServiceFactoryImpl implements HwServiceFactory.Factory {
    private static final String TAG = "HwServiceFactoryImpl";
    /* access modifiers changed from: private */
    public static DisplayEngineInterface mDisplayEngineInterface = null;
    /* access modifiers changed from: private */
    public static HWDrmDialogsService mDrmDialogService = null;
    /* access modifiers changed from: private */
    public static HwSmartBackLightNormalizedController mHwSmartBackLightController = null;

    public static class DisplayEngineInterfaceImpl implements HwServiceFactory.IDisplayEngineInterface {
        public void initialize() {
            if (HwServiceFactoryImpl.mDisplayEngineInterface == null) {
                DisplayEngineInterface unused = HwServiceFactoryImpl.mDisplayEngineInterface = new DisplayEngineInterface();
            }
        }

        public boolean getSupported(String feature) {
            boolean z = false;
            if (HwServiceFactoryImpl.mDisplayEngineInterface == null) {
                return false;
            }
            int ret = HwServiceFactoryImpl.mDisplayEngineInterface.getSupported(feature);
            DisplayEngineInterface unused = HwServiceFactoryImpl.mDisplayEngineInterface;
            if (ret != 0) {
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

    public static class HwActiveServicesImpl implements HwServiceFactory.IHwActiveServices {
        public ActiveServices getInstance(ActivityManagerService ams) {
            return new HwActiveServices(ams);
        }
    }

    public static class HwActivityManagerServiceImpl implements HwServiceFactory.IHwActivityManagerService {
        public ActivityManagerService getInstance(Context context) {
            Slog.i(HwServiceFactoryImpl.TAG, "HwActivityManagerServiceImpl getInstance starts");
            return new HwActivityManagerService(context);
        }
    }

    public static class HwActivityStackSupervisorImpl implements HwServiceFactory.IHwActivityStackSupervisor {
        public ActivityStackSupervisor getInstance(ActivityManagerService service, Looper looper) {
            return new HwActivityStackSupervisor(service, looper);
        }
    }

    public static class HwActivityStarterImpl implements HwServiceFactory.IHwActivityStarter {
        public ActivityStarter getInstance(ActivityStartController controller, ActivityManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
            return new HwActivityStarter(controller, service, supervisor, interceptor);
        }
    }

    public static class HwAppOpsServiceImpl implements HwServiceFactory.IHwAppOpsService {
        public AppOpsService getInstance(File storagePath) {
            return new HwAppOpsService(storagePath);
        }

        public AppOpsService getInstance(File storagePath, Handler handler) {
            return new HwAppOpsService(storagePath, handler);
        }
    }

    public static class HwAttestationServiceImpl implements HwServiceFactory.IHwAttestationServiceFactory {
        /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.HwAttestationService, android.os.IBinder] */
        public IBinder getInstance(Context context) {
            return new HwAttestationService(context);
        }
    }

    public static class HwAudioServiceImpl implements HwServiceFactory.IHwAudioService {
        public AudioService getInstance(Context context) {
            return new HwAudioService(context);
        }
    }

    public static class HwAutomaticBrightnessControllerImpl implements HwServiceFactory.IHwAutomaticBrightnessController {
        public AutomaticBrightnessController getInstance(AutomaticBrightnessController.Callbacks callbacks, Looper looper, SensorManager sensorManager, BrightnessMappingStrategy mapper, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, Context context) {
            HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController = new HwNormalizedAutomaticBrightnessController(callbacks, looper, sensorManager, mapper, lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, context);
            return hwNormalizedAutomaticBrightnessController;
        }

        public AutomaticBrightnessController getInstance(AutomaticBrightnessController.Callbacks callbacks, Looper looper, SensorManager sensorManager, BrightnessMappingStrategy mapper, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, int initialLightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, HysteresisLevels hysteresisLevels, Context context) {
            HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController = new HwNormalizedAutomaticBrightnessController(callbacks, looper, sensorManager, mapper, lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounceConfig, darkeningLightDebounceConfig, resetAmbientLuxAfterWarmUpConfig, hysteresisLevels, context);
            return hwNormalizedAutomaticBrightnessController;
        }
    }

    public static class HwBluetoothBigDataServiceImpl implements HwServiceFactory.IHwBluetoothBigDataService {
        private HwBluetoothBigDataService mHwBluetoothBigDataService;

        private HwBluetoothBigDataService getInstance() {
            this.mHwBluetoothBigDataService = new HwBluetoothBigDataService();
            return this.mHwBluetoothBigDataService;
        }

        public void sendBigDataEvent(Context context, String bigDataEvent) {
            getInstance().sendBigDataEvent(context, bigDataEvent);
        }
    }

    public static class HwBluetoothManagerServiceImpl implements HwServiceFactory.IHwBluetoothManagerService {
        public BluetoothManagerService createHwBluetoothManagerService(Context context) {
            return new HwBluetoothManagerService(context);
        }
    }

    public static class HwDrmDialogServiceImpl implements HwServiceFactory.IHwDrmDialogService {
        public void startDrmDialogService(Context context) {
            if (HwServiceFactoryImpl.mDrmDialogService == null) {
                HWDrmDialogsService unused = HwServiceFactoryImpl.mDrmDialogService = new HWDrmDialogsService(context);
                HwServiceFactoryImpl.mDrmDialogService.start();
            }
        }
    }

    public static class HwFingerprintServiceImpl implements HwServiceFactory.IHwFingerprintService {
        public Class<SystemService> createServiceClass() {
            try {
                return Class.forName("com.android.server.fingerprint.HwFingerprintService");
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    public static class HwForceRotationManagerServiceWrapper implements HwServiceFactory.IHwForceRotationManagerServiceWrapper {
        public HwForceRotationManagerService getServiceInstance(Context context, Handler uiHandler) {
            return new HwForceRotationManagerService(context, uiHandler);
        }
    }

    public static class HwIMonitorManagerImpl implements HwServiceFactory.IHwIMonitorManager {
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

    public static class HwInputMethodManagerServiceImpl implements HwServiceFactory.IHwInputMethodManagerService {
        public InputMethodManagerService getInstance(Context context) {
            return new HwInputMethodManagerService(context);
        }
    }

    public static class HwLocationManagerServiceImpl implements HwServiceFactory.IHwLocationManagerService {
        public LocationManagerService getInstance(Context context) {
            return new HwLocationManagerService(context);
        }
    }

    public static class HwLockSettingsServiceImpl implements HwServiceFactory.IHwLockSettingsService {
        public LockSettingsService getInstance(Context context) {
            return new HwLockSettingsService(context);
        }
    }

    public static class HwNetworkManagementServiceImpl implements HwServiceFactory.IHwNetworkManagermentService {
        private HwNetworkManagementService mHwNMService;

        public NetworkManagementService getInstance(Context context, String socket, NetworkManagementService.SystemServices services) {
            this.mHwNMService = new HwNetworkManagementService(context, socket, services);
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

        public void sendDSCPChangeMessage(String[] cooked, String raw) {
            this.mHwNMService.sendDSCPChangeMessage(cooked, raw);
        }

        public boolean handleApLinkedStaListChange(String raw, String[] cooked) {
            return this.mHwNMService.handleApLinkedStaListChange(raw, cooked);
        }

        public void sendApkDownloadUrlBroadcast(String[] cooked, String raw) {
            this.mHwNMService.sendApkDownloadUrlBroadcast(cooked, raw);
        }

        public void reportVodParams(int videoSegState, int videoProtocol, int videoRemainingPlayTime, int videoStatus, int aveCodeRate, int segSize, int flowInfoRemote, int flowInfoLocal, int segDuration, int segIndex) {
            this.mHwNMService.reportVodParams(videoSegState, videoProtocol, videoRemainingPlayTime, videoStatus, aveCodeRate, segSize, flowInfoRemote, flowInfoLocal, segDuration, segIndex);
        }

        public void reportKsiParams(int slowType, int avgAmp, int duration, int timeStart) {
            this.mHwNMService.reportKsiParams(slowType, avgAmp, duration, timeStart);
        }
    }

    public static class HwNetworkPolicyManagerServiceImpl implements HwServiceFactory.IHwNetworkPolicyManagerService {
        public NetworkPolicyManagerService getInstance(Context context, IActivityManager activityManager, INetworkManagementService networkManagement) {
            Slog.i(HwServiceFactoryImpl.TAG, "HwNetworkPolicyManagerService created");
            return new HwNetworkPolicyManagerService(context, activityManager, networkManagement);
        }
    }

    public static class HwNetworkStatsServiceImpl implements HwServiceFactory.IHwNetworkStatsService {
        public NetworkStatsService getInstance(Context context, INetworkManagementService networkManager, AlarmManager alarmManager, PowerManager.WakeLock wakeLock, Clock clock, TelephonyManager teleManager, NetworkStatsService.NetworkStatsSettings settings, NetworkStatsObservers statsObservers, File systemDir, File baseDir) {
            HwNetworkStatsService hwNetworkStatsService = new HwNetworkStatsService(context, networkManager, alarmManager, wakeLock, clock, teleManager, settings, statsObservers, systemDir, baseDir);
            return hwNetworkStatsService;
        }
    }

    public static class HwNormalizedManualBrightnessControllerImpl implements HwServiceFactory.IHwNormalizedManualBrightnessController {
        public ManualBrightnessController getInstance(ManualBrightnessController.ManualBrightnessCallbacks callbacks, Context context, SensorManager sensorManager) {
            return new HwNormalizedManualBrightnessController(callbacks, context, sensorManager);
        }
    }

    public static class HwNormalizedRampAnimatorImpl implements HwServiceFactory.IHwRampAnimator {
        public RampAnimator<DisplayPowerState> getInstance(DisplayPowerState object, IntProperty<DisplayPowerState> property) {
            return new HwNormalizedRampAnimator(object, property);
        }
    }

    public static class HwNotificationManagerServiceImpl implements HwServiceFactory.IHwNotificationManagerService {
        public NotificationManagerService getInstance(Context context, StatusBarManagerService statusBar, LightsService lights) {
            return new HwNotificationManagerService(context, statusBar, lights);
        }
    }

    public static class HwPowerManagerServiceImpl implements HwServiceFactory.IHwPowerManagerService {
        public PowerManagerService getInstance(Context context) {
            return new HwPowerManagerService(context);
        }
    }

    public static class HwSmartBackLightControllerImpl implements HwServiceFactory.IHwSmartBackLightController {
        public boolean checkIfUsingHwSBL() {
            return HwSmartBackLightNormalizedController.checkIfUsingHwSBL();
        }

        public void StartHwSmartBackLightController(Context context, LightsManager lightsManager, SensorManager sensorManager) {
            if (HwServiceFactoryImpl.mHwSmartBackLightController == null) {
                HwSmartBackLightNormalizedController unused = HwServiceFactoryImpl.mHwSmartBackLightController = new HwSmartBackLightNormalizedController(context, lightsManager, sensorManager);
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

    public static class HwStorageManagerServiceImpl implements HwServiceFactory.IHwStorageManagerService {
        public StorageManagerService getInstance(Context context) {
            Slog.i(HwServiceFactoryImpl.TAG, "HwStorageManagerServiceImpl getInstance starts");
            return new HwStorageManagerService(context);
        }
    }

    public static class HwTelephonyRegistryImpl implements HwServiceFactory.IHwTelephonyRegistry {
        public TelephonyRegistry getInstance(Context context) {
            return new StubTelephonyRegistry(context);
        }
    }

    public static class HwUserManagerServiceImpl implements HwServiceFactory.IHwUserManagerService {
        public UserManagerService getInstance(Context context, PackageManagerService pm, UserDataPreparer userDataPreparer, Object packagesLock) {
            return new HwUserManagerService(context, pm, userDataPreparer, packagesLock);
        }
    }

    public static class HwWallpaperManagerServiceImpl implements HwServiceFactory.IHwWallpaperManagerService {
        public WallpaperManagerService getInstance(Context context) {
            return new HwWallpaperManagerService(context);
        }
    }

    public static class HwWindowManagerServiceImpl implements HwServiceFactory.IHwWindowManagerService {
        public WindowManagerService getInstance(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore, WindowManagerPolicy policy) {
            HwWindowManagerService hwWindowManagerService = new HwWindowManagerService(context, inputManager, haveInputMethods, showBootMsgs, onlyCore, policy);
            return hwWindowManagerService;
        }
    }

    public static class HwWindowStateAnimatorImpl implements HwServiceFactory.IHwWindowStateAnimator {
        public WindowStateAnimator getInstance(WindowState win) {
            return new HwWindowStateAnimator(win);
        }
    }

    public static class HwZenModeFilteringImpl implements HwServiceFactory.IHwZenModeFiltering {
        public boolean matchesCallFilter(Context context, int zen, ZenModeConfig config, UserHandle userHandle, Bundle extras, ValidateNotificationPeople validator, int contactsTimeoutMs, float timeoutAffinity) {
            return HwZenModeFiltering.matchesCallFilter(context, zen, config, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
        }
    }

    public static class MultiTaskManagerServiceImpl implements HwServiceFactory.IMultiTaskManagerServiceFactory {
        /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.mtm.MultiTaskManagerService, android.os.IBinder] */
        public IBinder getInstance(Context context) {
            return new MultiTaskManagerService(context);
        }
    }

    public HwServiceFactory.IHwAppOpsService getHwAppOpsService() {
        return new HwAppOpsServiceImpl();
    }

    public HwServiceFactory.IHwZenModeFiltering getHwZenModeFiltering() {
        return new HwZenModeFilteringImpl();
    }

    public HwServiceFactory.IHwWallpaperManagerService getHuaweiWallpaperManagerService() {
        return new HwWallpaperManagerServiceImpl();
    }

    public String getWallpaperManagerServiceClassName() {
        return "com.android.server.wallpaper.HwWallpaperManagerService$Lifecycle";
    }

    public HwServiceFactory.IHwPowerManagerService getHwPowerManagerService() {
        return new HwPowerManagerServiceImpl();
    }

    public HwServiceFactory.IHwNotificationManagerService getHwNotificationManagerService() {
        return new HwNotificationManagerServiceImpl();
    }

    public HwServiceFactory.IHwNetworkManagermentService getHwNetworkManagermentService() {
        return new HwNetworkManagementServiceImpl();
    }

    public HwServiceFactory.IHwNetworkPolicyManagerService getHwNetworkPolicyManagerService() {
        return new HwNetworkPolicyManagerServiceImpl();
    }

    public HwServiceFactory.IHwInputMethodManagerService getHwInputMethodManagerService() {
        return new HwInputMethodManagerServiceImpl();
    }

    public HwServiceFactory.IHwActivityStackSupervisor getHwActivityStackSupervisor() {
        return new HwActivityStackSupervisorImpl();
    }

    public HwServiceFactory.IHwActivityStarter getHwActivityStarter() {
        return new HwActivityStarterImpl();
    }

    public HwServiceFactory.IHwTelephonyRegistry getHwTelephonyRegistry() {
        return new HwTelephonyRegistryImpl();
    }

    public HwServiceFactory.IHwAudioService getHwAudioService() {
        return new HwAudioServiceImpl();
    }

    public HwServiceFactory.IHwBluetoothManagerService getHwBluetoothManagerService() {
        return new HwBluetoothManagerServiceImpl();
    }

    public HwServiceFactory.IHwActivityManagerService getHwActivityManagerService() {
        Slog.i(TAG, "getHwActivityManagerService starts");
        return new HwActivityManagerServiceImpl();
    }

    public HwServiceFactory.IHwStorageManagerService getHwStorageManagerService() {
        Slog.i(TAG, "getHwStorageManagerService starts");
        return new HwStorageManagerServiceImpl();
    }

    public IHwShutdownThread getHwShutdownThreadImpl() {
        return new HwShutdownThreadImpl();
    }

    public HwConnectivityManager getHwConnectivityManager() {
        return HwConnectivityManagerImpl.getDefault();
    }

    public HwServiceFactory.IHwLockSettingsService getHuaweiLockSettingsService() {
        return new HwLockSettingsServiceImpl();
    }

    public PackageManagerService getHuaweiPackageManagerService(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        return HwPackageManagerService.getInstance(context, installer, factoryTest, onlyCore);
    }

    public DefaultPermissionGrantPolicy getHwDefaultPermissionGrantPolicy(Context context, Looper looper, DefaultPermissionGrantPolicy.DefaultPermissionGrantedCallback callback, PermissionManagerService permissionManager) {
        return new HwDefaultPermissionGrantPolicy(context, looper, callback, permissionManager);
    }

    public HwServiceFactory.IHwWindowStateAnimator getHuaweiWindowStateAnimator() {
        return new HwWindowStateAnimatorImpl();
    }

    public HwServiceFactory.IHwWindowManagerService getHuaweiWindowManagerService() {
        return new HwWindowManagerServiceImpl();
    }

    public HwServiceFactory.IHwUserManagerService getHwUserManagerService() {
        return new HwUserManagerServiceImpl();
    }

    public StatusBarManagerService createHwStatusBarManagerService(Context context, WindowManagerService windowManager) {
        return new HwStatusBarManagerService(context, windowManager);
    }

    public HwServiceFactory.IHwActiveServices getHwActiveServices() {
        return new HwActiveServicesImpl();
    }

    public void setAlarmService(AlarmManagerService alarm) {
        SMCSAMSHelper.getInstance().setAlarmService(alarm);
    }

    public boolean isCustedCouldStopped(String pkg, boolean block, boolean stopped) {
        return HwPackageManagerServiceEx.isCustedCouldStopped(pkg, block, stopped);
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

    /* JADX WARNING: type inference failed for: r0v1, types: [com.android.server.HwConnectivityExService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r2v6, types: [com.android.server.display.DisplayEngineService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r2v9, types: [com.android.server.aps.HwApsManagerService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r2v11, types: [com.android.server.HwGeneralService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r2v13, types: [com.android.server.HwSmartDisplayService, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r1v5, types: [com.android.server.HwGeneralService, android.os.IBinder] */
    public void setupHwServices(Context context) {
        Slog.i(TAG, "Hw Connectivity Service");
        ServiceManager.addService("hwConnectivityExService", new HwConnectivityExService(context));
        try {
            ? hwGeneralService = new HwGeneralService(context, UiThread.getHandler());
            Slog.i(TAG, "General Service");
            ServiceManager.addService("hwGeneralService", hwGeneralService);
        } catch (Throwable e) {
            Slog.e(TAG, "Failure starting generalService", e);
        }
        try {
            Slog.i(TAG, "smartdisplay Service");
            ? hwSmartDisplayService = new HwSmartDisplayService(context);
            boolean isSupport_HIACE_ARSR1P = (hwSmartDisplayService.getDisplayEffectSupported(0) & 17) != 0;
            if (("true".equals(SystemProperties.get("ro.config.eyesprotect_support", "false")) && hwSmartDisplayService.isFeatureSupported(1)) || hwSmartDisplayService.isFeatureSupported(2) || "true".equals(SystemProperties.get("ro.config.hw_displayeffect_en", "false")) || isSupport_HIACE_ARSR1P) {
                ServiceManager.addService("smartDisplay_service", hwSmartDisplayService);
            }
        } catch (Throwable th) {
            Slog.i(TAG, "smartdisplay_service failed");
        }
        if (HwGeneralManager.getInstance().isSupportForce()) {
            try {
                ? hwGeneralService2 = new HwGeneralService(context, UiThread.getHandler());
                Slog.i(TAG, "General Service");
                ServiceManager.addService("hwGeneralService", hwGeneralService2);
            } catch (Throwable e2) {
                Slog.e(TAG, "Failure starting generalService", e2);
            }
        }
        int apsSupportValue = SystemProperties.getInt("sys.aps.support", 0);
        if (apsSupportValue != 0) {
            try {
                ? hwApsManagerService = new HwApsManagerService(context);
                ServiceManager.addService("aps_service", hwApsManagerService);
                hwApsManagerService.systemReady();
            } catch (Throwable e3) {
                Slog.e(TAG, "Failure starting apsService", e3);
            }
        } else {
            Slog.e(TAG, "Not support APS! sys.aps.support = " + apsSupportValue);
        }
        try {
            ServiceManager.addService("DisplayEngineExService", new DisplayEngineService(context));
            Slog.i(TAG, "[effect] DisplayEngineServiceEx success");
        } catch (Throwable e4) {
            Slog.e(TAG, "[effect] Failed to start DisplayEngineServiceEx, " + e4.getMessage());
        }
        if (HwMapleHelper.isSupportMapleHelper()) {
            HwMapleHelper.startMapleHelper();
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

    public HwServiceFactory.IHwDrmDialogService getHwDrmDialogService() {
        return new HwDrmDialogServiceImpl();
    }

    public HwServiceFactory.IHwAttestationServiceFactory getHwAttestationService() {
        return new HwAttestationServiceImpl();
    }

    public HwServiceFactory.IMultiTaskManagerServiceFactory getMultiTaskManagerService() {
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

    public ActivityRecord createActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityOptions options, ActivityRecord sourceRecord) {
        HwActivityRecord hwActivityRecord = new HwActivityRecord(_service, _caller, _launchedFromPid, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultTo, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisor, options, sourceRecord);
        return hwActivityRecord;
    }

    public ActivityStartInterceptor createActivityStartInterceptor(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        return new HwActivityStartInterceptor(service, supervisor);
    }

    public AppTransition createHwAppTransition(Context context, WindowManagerService w) {
        return new HwAppTransition(context, w);
    }

    public TaskStack createTaskStack(WindowManagerService service, int stackId, StackWindowController controller) {
        return new HwTaskStack(service, stackId, controller);
    }

    public DisplayContent createDisplayContent(Display display, WindowManagerService service, WallpaperController wallpaperController, DisplayWindowController controller) {
        return new HwDisplayContent(display, service, wallpaperController, controller);
    }

    public TaskRecord createTaskRecord(ActivityManagerService service, int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        HwTaskRecord hwTaskRecord = new HwTaskRecord(service, taskId, info, intent, voiceSession, voiceInteractor);
        return hwTaskRecord;
    }

    public TaskRecord createTaskRecord(ActivityManagerService service, int taskId, ActivityInfo info, Intent intent, ActivityManager.TaskDescription taskDescription) {
        HwTaskRecord hwTaskRecord = new HwTaskRecord(service, taskId, info, intent, taskDescription);
        return hwTaskRecord;
    }

    public TaskRecord createTaskRecord(ActivityManagerService service, int taskId, Intent intent, Intent affinityIntent, String affinity, String rootAffinity, ComponentName realActivity, ComponentName origActivity, boolean rootWasReset, boolean autoRemoveRecents, boolean askedCompatMode, int userId, int effectiveUid, String lastDescription, ArrayList<ActivityRecord> activities, long lastTimeMoved, boolean neverRelinquishIdentity, ActivityManager.TaskDescription lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        HwTaskRecord hwTaskRecord = new HwTaskRecord(service, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootWasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeMoved, neverRelinquishIdentity, lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete, minWidth, minHeight);
        return hwTaskRecord;
    }

    public ActivityStack createActivityStack(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, int windowingMode, int activityType, boolean onTop) {
        HwActivityStack hwActivityStack = new HwActivityStack(display, stackId, supervisor, windowingMode, activityType, onTop);
        return hwActivityStack;
    }

    public HwServiceFactory.IHwSmartBackLightController getHwSmartBackLightController() {
        return new HwSmartBackLightControllerImpl();
    }

    public HwServiceFactory.IDisplayEngineInterface getDisplayEngineInterface() {
        return new DisplayEngineInterfaceImpl();
    }

    public HwServiceFactory.IHwAutomaticBrightnessController getHuaweiAutomaticBrightnessController() {
        return new HwAutomaticBrightnessControllerImpl();
    }

    public HwServiceFactory.IDisplayEffectMonitor getDisplayEffectMonitor(Context context) {
        return DisplayEffectMonitor.getInstance(context);
    }

    public HwServiceFactory.IHwNormalizedManualBrightnessController getHuaweiManualBrightnessController() {
        return new HwNormalizedManualBrightnessControllerImpl();
    }

    public HwServiceFactory.IHwBluetoothBigDataService getHwBluetoothBigDataService() {
        return new HwBluetoothBigDataServiceImpl();
    }

    public HwServiceFactory.IHwIMonitorManager getHwIMonitorManager() {
        return new HwIMonitorManagerImpl();
    }

    public IZRHungService getZRHungService() {
        return ZRHungService.getInstance();
    }

    public HwServiceFactory.IHwRampAnimator getHwNormalizedRampAnimator() {
        return new HwNormalizedRampAnimatorImpl();
    }

    public HwServiceFactory.IHwLocationManagerService getHwLocationManagerService() {
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

    public HwServiceFactory.IHwFingerprintService getHwFingerprintService() {
        return new HwFingerprintServiceImpl();
    }

    public AbsHwMtmBroadcastResourceManager getMtmBRManagerImpl(HwBroadcastQueue queue) {
        return new HwMtmBroadcastResourceManager(queue);
    }

    public HwServiceFactory.ISystemBlockMonitor getISystemBlockMonitor() {
        return SystemBlockMonitor.getInstance();
    }

    public HwServiceFactory.IHwBinderMonitor getIHwBinderMonitor() {
        return new HwBinderMonitor();
    }

    public IHwIpcChecker getIHwIpcChecker(Object object, Handler handler, long waitMaxMillis) {
        return new HwIpcChecker(object, handler, waitMaxMillis);
    }

    public IHwIpcMonitor getIHwIpcMonitor(Object object, String type, String name) {
        return HwIpcMonitorImpl.getHwIpcMonitor(object, type, name);
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

    public HwServiceFactory.IHwNetworkStatsService getHwNetworkStatsService() {
        return new HwNetworkStatsServiceImpl();
    }

    public ISDCardCryptedHelper getSDCardCryptedHelper() {
        return new SDCardCryptedHelper();
    }

    public void reportProximitySensorEventToIAware(boolean positive) {
        HwSysResManager.getInstance().reportProximitySensorEventToIAware(positive);
    }

    /* JADX WARNING: type inference failed for: r1v1, types: [com.android.server.pc.HwPCManagerService, android.os.IBinder] */
    public void addHwPCManagerService(Context context, ActivityManagerService ams) {
        Slog.i(TAG, "PCManager, addService");
        ServiceManager.addService("hwPcManager", new HwPCManagerService(context, ams));
    }

    public void reportVibratorToIAware(int uid) {
        HwSysResManager.getInstance().reportVibratorToIAware(uid);
    }

    public HwServiceFactory.IHwForceRotationManagerServiceWrapper getForceRotationManagerServiceWrapper() {
        return new HwForceRotationManagerServiceWrapper();
    }

    public void updateLocalesWhenOTA(Context context) {
        UpdateConfig.updateLocalesWhenOTA(context);
    }

    public void updateLocalesWhenOTAEX(Context context, int preSdkVersion) {
        UpdateConfig.updateLocalesWhenOTAEX(context, preSdkVersion);
    }

    public IHwCameraServiceProxy getHwCameraServiceProxy(Context context) {
        return new HwCameraServiceProxy(context);
    }

    public MediaFocusControl getHwMediaFocusControl(Context cntxt, PlayerFocusEnforcer pfe) {
        return new HwMediaFocusControl(cntxt, pfe);
    }

    public FocusRequester getHwFocusRequester(AudioAttributes aa, int focusRequest, int grantFlags, IAudioFocusDispatcher afl, IBinder source, String id, MediaFocusControl.AudioFocusDeathHandler hdlr, String pn, int uid, MediaFocusControl ctlr, int sdk, boolean isInExternal) {
        HwFocusRequester hwFocusRequester = new HwFocusRequester(aa, focusRequest, grantFlags, afl, source, id, hdlr, pn, uid, ctlr, sdk, isInExternal);
        return hwFocusRequester;
    }

    public FocusRequester getHwFocusRequester(AudioFocusInfo afi, IAudioFocusDispatcher afl, IBinder source, MediaFocusControl.AudioFocusDeathHandler hdlr, MediaFocusControl ctlr, boolean isInExternal) {
        HwFocusRequester hwFocusRequester = new HwFocusRequester(afi, afl, source, hdlr, ctlr, isInExternal);
        return hwFocusRequester;
    }

    public void reportMediaKeyToIAware(int uid) {
        HwSysResManager.getInstance().reportMediaKeyToIAware(uid);
    }

    public ISecurityProfileController getSecurityProfileController() {
        return new SecurityProfileControllerImpl();
    }

    public ITrustSpaceController getTrustSpaceController() {
        return new TrustSpaceControllerImpl();
    }

    public IHwScreenRotationAnimation getHwScreenRotationAnimation() {
        return new HwScreenRotationAnimationImpl();
    }

    public void reportToastHiddenToIAware(int pid, int hcode) {
        HwSysResManager.getInstance().reportToastHiddenToIAware(pid, hcode);
    }

    public void reportSysWakeUp(String reason) {
        HwSysResManager.getInstance().reportSysWakeUp(reason);
    }

    public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        return HwSysResManager.getInstance().getNetLocationStrategy(pkgName, uid, type);
    }

    public HWExtMotionRotationProcessorEx getHWExtMotionRotationProcessor(IHWExtMotionRotationProcessor.WindowOrientationListenerProxy proxy) {
        return new HWExtMotionRotationProcessorEx(proxy);
    }

    public void setPowerState(int powerState) {
        HwDisplayPowerController.setPowerState(powerState);
    }

    public void reportGoogleConn(boolean conn) {
        HwSysResManager.getInstance().reportGoogleConn(conn);
    }

    public HwLbsLogger getHwLbsLogger(Context context) {
        return HwLbsLogger.getInstance(context);
    }

    public boolean hwBrightnessSetData(String name, Bundle data, int[] result) {
        return HwDisplayPowerController.hwBrightnessSetData(name, data, result);
    }

    public boolean hwBrightnessGetData(String name, Bundle data, int[] result) {
        return HwDisplayPowerController.hwBrightnessGetData(name, data, result);
    }

    public void loadHwBrightnessProcessors(AutomaticBrightnessController autoController, ManualBrightnessController manualController) {
        HwDisplayPowerController.loadHwBrightnessProcessors(autoController, manualController);
    }

    public HwUibcReceiver getHwUibcReceiver() {
        return HwUibcReceiver.getInstance();
    }

    public void clearHwUibcReceiver() {
        HwUibcReceiver.clearInstance();
    }

    public IHwAppWindowContainerController getHwAppWindowContainerController() {
        return new HwAppWindowContainerControllerImpl();
    }

    public IHwWifiDisplayAdapterEx getHwWifiDisplayAdapterEx(IWifiDisplayAdapterInner wfda) {
        return new HwWifiDisplayAdapterEx(wfda);
    }

    public IHwWifiDisplayControllerEx getHwWifiDisplayControllerEx(IWifiDisplayControllerInner wfdc, Context context, Handler handler) {
        return new HwWifiDisplayControllerEx(wfdc, context, handler);
    }

    public IHwWifiProCommonUtilsEx getWifiProCommonUtilsEx() {
        return WifiProCommonUtils.getDefault();
    }

    public HwFoldScreenState getHwFoldScreenState(Context context) {
        return new HwFoldScreenStateImpl(context);
    }

    public IHwPersistentDataStoreEx getHwPersistentDataStoreEx(IPersistentDataStoreInner pds) {
        return new HwPersistentDataStoreEx(pds);
    }
}
