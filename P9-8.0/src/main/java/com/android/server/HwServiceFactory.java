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
import android.os.UserHandle;
import android.rms.iaware.NetLocationStrategy;
import android.service.notification.ZenModeConfig;
import android.service.voice.IVoiceInteractionSession;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.IntProperty;
import android.util.Log;
import android.util.Spline;
import android.util.TrustedTime;
import android.view.Display;
import android.view.WindowManagerPolicy;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.AbsHwMtmBroadcastResourceManager;
import com.android.server.am.ActiveServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityRecord;
import com.android.server.am.ActivityStack;
import com.android.server.am.ActivityStackSupervisor;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.am.ActivityStartInterceptor;
import com.android.server.am.ActivityStarter;
import com.android.server.am.HwBroadcastQueue;
import com.android.server.am.IHwPowerInfoService;
import com.android.server.am.ProcessRecord;
import com.android.server.am.RecentTasks;
import com.android.server.am.TaskRecord;
import com.android.server.audio.AudioService;
import com.android.server.audio.MediaFocusControl;
import com.android.server.audio.PlayerFocusEnforcer;
import com.android.server.connectivity.HwNotificationTethering;
import com.android.server.connectivity.HwNotificationTetheringDummy;
import com.android.server.content.SyncManager;
import com.android.server.display.AutomaticBrightnessController;
import com.android.server.display.AutomaticBrightnessController.Callbacks;
import com.android.server.display.DisplayPowerState;
import com.android.server.display.HysteresisLevels;
import com.android.server.display.ManualBrightnessController;
import com.android.server.display.ManualBrightnessController.ManualBrightnessCallbacks;
import com.android.server.display.RampAnimator;
import com.android.server.forcerotation.HwForceRotationManagerService;
import com.android.server.input.IHwInputManagerService;
import com.android.server.input.InputManagerService;
import com.android.server.lights.LightsManager;
import com.android.server.lights.LightsService;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.IAgpsConnectProvider;
import com.android.server.location.IHwCmccGpsFeature;
import com.android.server.location.IHwGpsActionReporter;
import com.android.server.location.IHwGpsLocationCustFeature;
import com.android.server.location.IHwGpsLocationManager;
import com.android.server.location.IHwGpsLogServices;
import com.android.server.location.IHwGpsXtraDownloadReceiver;
import com.android.server.location.IHwLocalLocationProvider;
import com.android.server.location.LocationProviderProxy;
import com.android.server.media.MediaSessionStack;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsObservers;
import com.android.server.net.NetworkStatsService;
import com.android.server.net.NetworkStatsService.NetworkStatsSettings;
import com.android.server.notification.NotificationManagerService;
import com.android.server.notification.ValidateNotificationPeople;
import com.android.server.os.IFreezeScreenWindowMonitor;
import com.android.server.pm.DefaultPermissionGrantPolicy;
import com.android.server.pm.DummyHwPackageServiceManager;
import com.android.server.pm.HwPackageServiceManager;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserDataPreparer;
import com.android.server.pm.UserManagerService;
import com.android.server.power.HwShutdownThreadDummy;
import com.android.server.power.IHwShutdownThread;
import com.android.server.power.PowerManagerService;
import com.android.server.rms.IDaemonRecoverHandler;
import com.android.server.rms.IHwIpcChecker;
import com.android.server.rms.IHwIpcMonitor;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.storage.DeviceStorageMonitorService;
import com.android.server.wallpaper.WallpaperManagerService;
import com.android.server.wm.AppTransition;
import com.android.server.wm.DisplayContent;
import com.android.server.wm.HwAppTransitionDummy;
import com.android.server.wm.IHwAppTransition;
import com.android.server.wm.IHwScreenRotationAnimation;
import com.android.server.wm.TaskStack;
import com.android.server.wm.WallpaperController;
import com.android.server.wm.WindowLayersController;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import com.android.server.wm.WindowStateAnimator;
import java.io.File;
import java.util.ArrayList;

public class HwServiceFactory {
    private static final String TAG = "HwServiceFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface IHwBluetoothManagerService {
        BluetoothManagerService createHwBluetoothManagerService(Context context);
    }

    public interface IContinuousRebootChecker {
        void checkAbnormalReboot();
    }

    public interface Factory {
        void activePlaceFile();

        void addHwFmService(Context context);

        void addHwPCManagerService(Context context, ActivityManagerService activityManagerService);

        boolean clearWipeDataFactory(Context context, Intent intent);

        boolean clearWipeDataFactoryLowlevel(Context context, Intent intent);

        ActivityRecord createActivityRecord(ActivityManagerService activityManagerService, ProcessRecord processRecord, int i, int i2, String str, Intent intent, String str2, ActivityInfo activityInfo, Configuration configuration, ActivityRecord activityRecord, String str3, int i3, boolean z, boolean z2, ActivityStackSupervisor activityStackSupervisor, ActivityContainer activityContainer, ActivityOptions activityOptions, ActivityRecord activityRecord2);

        ActivityStack createActivityStack(ActivityContainer activityContainer, RecentTasks recentTasks, boolean z);

        ActivityStartInterceptor createActivityStartInterceptor(ActivityManagerService activityManagerService, ActivityStackSupervisor activityStackSupervisor);

        DisplayContent createDisplayContent(Display display, WindowManagerService windowManagerService, WindowLayersController windowLayersController, WallpaperController wallpaperController);

        AppTransition createHwAppTransition(Context context, WindowManagerService windowManagerService);

        GnssLocationProvider createHwGnssLocationProvider(Context context, ILocationManager iLocationManager, Looper looper);

        StatusBarManagerService createHwStatusBarManagerService(Context context, WindowManagerService windowManagerService);

        SyncManager createHwSyncManager(Context context, boolean z);

        TaskRecord createTaskRecord(ActivityManagerService activityManagerService, int i, Intent intent, Intent intent2, String str, String str2, ComponentName componentName, ComponentName componentName2, boolean z, boolean z2, boolean z3, int i2, int i3, int i4, String str3, ArrayList<ActivityRecord> arrayList, long j, long j2, long j3, boolean z4, TaskDescription taskDescription, TaskThumbnailInfo taskThumbnailInfo, int i5, int i6, int i7, int i8, int i9, String str4, int i10, boolean z5, boolean z6, boolean z7, boolean z8, int i11, int i12);

        TaskRecord createTaskRecord(ActivityManagerService activityManagerService, int i, ActivityInfo activityInfo, Intent intent, TaskDescription taskDescription, TaskThumbnailInfo taskThumbnailInfo);

        TaskRecord createTaskRecord(ActivityManagerService activityManagerService, int i, ActivityInfo activityInfo, Intent intent, IVoiceInteractionSession iVoiceInteractionSession, IVoiceInteractor iVoiceInteractor, int i2);

        TaskStack createTaskStack(WindowManagerService windowManagerService, int i);

        GeocoderProxy geocoderProxyCreateAndBind(Context context, int i, int i2, int i3, Handler handler);

        IAgpsConnectProvider getAgpsConnectProvider(Context context);

        String getDeviceStorageMonitorServiceClassName();

        IDisplayEffectMonitor getDisplayEffectMonitor(Context context);

        IDisplayEngineInterface getDisplayEngineInterface();

        IHwForceRotationManagerServiceWrapper getForceRotationManagerServiceWrapper();

        IHwAutomaticBrightnessController getHuaweiAutomaticBrightnessController();

        IHwLockSettingsService getHuaweiLockSettingsService();

        IHwNormalizedManualBrightnessController getHuaweiManualBrightnessController();

        IHwMediaSessionStack getHuaweiMediaSessionStack();

        PackageManagerService getHuaweiPackageManagerService(Context context, Installer installer, boolean z, boolean z2);

        IHwWallpaperManagerService getHuaweiWallpaperManagerService();

        IHwWindowManagerService getHuaweiWindowManagerService();

        IHwWindowStateAnimator getHuaweiWindowStateAnimator();

        IHwActiveServices getHwActiveServices();

        IHwActivityManagerService getHwActivityManagerService();

        IHwActivityStackSupervisor getHwActivityStackSupervisor();

        IHwActivityStarter getHwActivityStarter();

        IHwAppOpsService getHwAppOpsService();

        IHwAppTransition getHwAppTransitionImpl();

        IHwAttestationServiceFactory getHwAttestationService();

        IHwAudioService getHwAudioService();

        IHwBluetoothBigDataService getHwBluetoothBigDataService();

        IHwBluetoothManagerService getHwBluetoothManagerService();

        IHwCmccGpsFeature getHwCmccGpsFeature(Context context, GnssLocationProvider gnssLocationProvider);

        HwConnectivityManager getHwConnectivityManager();

        DefaultPermissionGrantPolicy getHwDefaultPermissionGrantPolicy(Context context, PackageManagerService packageManagerService);

        IHwDrmDialogService getHwDrmDialogService();

        IHwFingerprintService getHwFingerprintService();

        IHwGpsActionReporter getHwGpsActionReporter(Context context, ILocationManager iLocationManager);

        IHwGpsLocationCustFeature getHwGpsLocationCustFeature();

        IHwGpsLocationManager getHwGpsLocationManager(Context context);

        IHwGpsLogServices getHwGpsLogServices(Context context);

        IHwGpsXtraDownloadReceiver getHwGpsXtraDownloadReceiver();

        IHwIMonitorManager getHwIMonitorManager();

        IHwInputManagerService getHwInputManagerService();

        IHwInputMethodManagerService getHwInputMethodManagerService();

        IHwLocalLocationProvider getHwLocalLocationProvider(Context context, ILocationManager iLocationManager);

        IHwLocationManagerService getHwLocationManagerService();

        MediaFocusControl getHwMediaFocusControl(Context context, PlayerFocusEnforcer playerFocusEnforcer);

        HwNLPManager getHwNLPManager();

        HwNativeDaemonConnector getHwNativeDaemonConnector();

        IHwNetworkManagermentService getHwNetworkManagermentService();

        IHwNetworkPolicyManagerService getHwNetworkPolicyManagerService();

        IHwNetworkStatsService getHwNetworkStatsService();

        IHwRampAnimator getHwNormalizedRampAnimator();

        IHwNotificationManagerService getHwNotificationManagerService();

        HwNotificationTethering getHwNotificationTethering(Context context);

        HwPackageServiceManager getHwPackageServiceManager();

        IHwPowerInfoService getHwPowerInfoService(Context context, boolean z);

        IHwPowerManagerService getHwPowerManagerService();

        IHwScreenRotationAnimation getHwScreenRotationAnimation();

        IHwShutdownThread getHwShutdownThreadImpl();

        IHwSmartBackLightController getHwSmartBackLightController();

        IHwStorageManagerService getHwStorageManagerService();

        IHwTelephonyRegistry getHwTelephonyRegistry();

        IHwUserManagerService getHwUserManagerService();

        IHwZenModeFiltering getHwZenModeFiltering();

        IDaemonRecoverHandler getIDaemonRecoverHandler();

        IHwBinderMonitor getIHwBinderMonitor();

        IHwIpcChecker getIHwIpcChecker(Object obj, Handler handler, long j);

        IHwIpcMonitor getIHwIpcMonitor(Object obj, String str, String str2);

        ISystemBlockMonitor getISystemBlockMonitor();

        AbsHwMtmBroadcastResourceManager getMtmBRManagerImpl(HwBroadcastQueue hwBroadcastQueue);

        IMultiTaskManagerServiceFactory getMultiTaskManagerService();

        NetLocationStrategy getNetLocationStrategy(String str, int i, int i2);

        IHwGpsLogServices getNewHwGpsLogService();

        ISDCardCryptedHelper getSDCardCryptedHelper();

        ISecurityProfileController getSecurityProfileController();

        String getWallpaperManagerServiceClassName();

        IFreezeScreenWindowMonitor getWinFreezeScreenMonitor();

        boolean isCoverClosed();

        boolean isCustedCouldStopped(String str, boolean z, boolean z2);

        boolean isPrivAppInCust(File file);

        boolean isPrivAppInData(File file, String str);

        boolean isPrivAppNonSystemPartitionDir(File file);

        LocationProviderProxy locationProviderProxyCreateAndBind(Context context, String str, String str2, int i, int i2, int i3, Handler handler);

        boolean removeProtectAppInPrivacyMode(AuthenticatorDescription authenticatorDescription, boolean z, Context context);

        void reportMediaKeyToIAware(int i);

        void reportProximitySensorEventToIAware(boolean z);

        void reportVibratorToIAware(int i);

        void setAlarmService(AlarmManagerService alarmManagerService);

        void setIfCoverClosed(boolean z);

        void setupHwServices(Context context);

        boolean shouldFilteInvalidSensorVal(float f);
    }

    public interface IDisplayEffectMonitor {
        void sendMonitorParam(ArrayMap<String, Object> arrayMap);
    }

    public interface IDisplayEngineInterface {
        boolean getSupported(String str);

        void initialize();

        void setScene(String str, String str2);

        void updateLightSensorState(boolean z);
    }

    public interface IHwActiveServices {
        ActiveServices getInstance(ActivityManagerService activityManagerService);
    }

    public interface IHwActivityManagerService {
        ActivityManagerService getInstance(Context context);
    }

    public interface IHwActivityStackSupervisor {
        ActivityStackSupervisor getInstance(ActivityManagerService activityManagerService, Looper looper);
    }

    public interface IHwActivityStarter {
        ActivityStarter getInstance(ActivityManagerService activityManagerService, ActivityStackSupervisor activityStackSupervisor);
    }

    public interface IHwAlarmManagerService {
        Object getHwAlarmHandler(AlarmManagerService alarmManagerService);

        AlarmManagerService getInstance(Context context);
    }

    public interface IHwAppOpsService {
        AppOpsService getInstance(File file);

        AppOpsService getInstance(File file, Handler handler);
    }

    public interface IHwAttestationServiceFactory {
        IBinder getInstance(Context context);
    }

    public interface IHwAudioService {
        AudioService getInstance(Context context);
    }

    public interface IHwAutomaticBrightnessController {
        AutomaticBrightnessController getInstance(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline spline, int i, int i2, int i3, float f, int i4, int i5, long j, long j2, boolean z, int i6, float f2, HysteresisLevels hysteresisLevels, Context context);

        AutomaticBrightnessController getInstance(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline spline, int i, int i2, int i3, float f, Context context);
    }

    public interface IHwBinderMonitor {
        void addBinderPid(ArrayList<Integer> arrayList, int i);

        void writeTransactonToTrace(String str);
    }

    public interface IHwBluetoothBigDataService {
        public static final String GET_OPEN_BT_APP_NAME = "android.bluetooth.GET_OPEN_BT_APP_NAME";

        void sendBigDataEvent(Context context, String str);
    }

    public interface IHwDrmDialogService {
        void startDrmDialogService(Context context);
    }

    public interface IHwFingerprintService {
        Class<SystemService> createServiceClass();
    }

    public interface IHwForceRotationManagerServiceWrapper {
        HwForceRotationManagerService getServiceInstance(Context context, Handler handler);
    }

    public interface IHwIMonitorManager {
        public static final int IMONITOR_BINDER_FAILED = 907102006;

        boolean uploadBtRadarEvent(int i, String str);
    }

    public interface IHwInputMethodManagerService {
        InputMethodManagerService getInstance(Context context);
    }

    public interface IHwLocationManagerService {
        LocationManagerService getInstance(Context context);
    }

    public interface IHwLockSettingsService {
        LockSettingsService getInstance(Context context);
    }

    public interface IHwMediaSessionStack {
        MediaSessionStack getInstance(Context context);
    }

    public interface IHwNetworkManagermentService {
        NetworkManagementService getInstance(Context context, String str);

        boolean handleApLinkedStaListChange(String str, String[] strArr);

        void sendApkDownloadUrlBroadcast(String[] strArr, String str);

        void sendDataSpeedSlowMessage(String[] strArr, String str);

        void sendWebStatMessage(String[] strArr, String str);

        void setNativeDaemonConnector(Object obj, Object obj2);

        void startAccessPointWithChannel(WifiConfiguration wifiConfiguration, String str);
    }

    public interface IHwNetworkPolicyManagerService {
        NetworkPolicyManagerService getInstance(Context context, IActivityManager iActivityManager, IPowerManager iPowerManager, INetworkStatsService iNetworkStatsService, INetworkManagementService iNetworkManagementService);
    }

    public interface IHwNetworkStatsService {
        NetworkStatsService getInstance(Context context, INetworkManagementService iNetworkManagementService, AlarmManager alarmManager, WakeLock wakeLock, TrustedTime trustedTime, TelephonyManager telephonyManager, NetworkStatsSettings networkStatsSettings, NetworkStatsObservers networkStatsObservers, File file, File file2);
    }

    public interface IHwNormalizedManualBrightnessController {
        ManualBrightnessController getInstance(ManualBrightnessCallbacks manualBrightnessCallbacks, Context context, SensorManager sensorManager);
    }

    public interface IHwNotificationManagerService {
        NotificationManagerService getInstance(Context context, StatusBarManagerService statusBarManagerService, LightsService lightsService);
    }

    public interface IHwPowerManagerService {
        PowerManagerService getInstance(Context context);
    }

    public interface IHwRampAnimator {
        RampAnimator<DisplayPowerState> getInstance(DisplayPowerState displayPowerState, IntProperty<DisplayPowerState> intProperty);
    }

    public interface IHwSecureInputMethodManagerService {
        InputMethodManagerService getInstance(Context context, WindowManagerService windowManagerService);
    }

    public interface IHwSmartBackLightController {
        public static final int BRIGHTNESS_UPDATE_END = 1;
        public static final int BRIGHTNESS_UPDATE_START = 0;

        void StartHwSmartBackLightController(Context context, LightsManager lightsManager, SensorManager sensorManager);

        boolean checkIfUsingHwSBL();

        void updateBrightnessState(int i);

        void updatePowerState(int i, boolean z);
    }

    public interface IHwStorageManagerService {
        StorageManagerService getInstance(Context context);
    }

    public interface IHwTelephonyRegistry {
        TelephonyRegistry getInstance(Context context);
    }

    public interface IHwUserManagerService {
        UserManagerService getInstance(Context context, PackageManagerService packageManagerService, UserDataPreparer userDataPreparer, Object obj);
    }

    public interface IHwWallpaperManagerService {
        WallpaperManagerService getInstance(Context context);
    }

    public interface IHwWindowManagerService {
        WindowManagerService getInstance(Context context, InputManagerService inputManagerService, boolean z, boolean z2, boolean z3, WindowManagerPolicy windowManagerPolicy);
    }

    public interface IHwWindowStateAnimator {
        WindowStateAnimator getInstance(WindowState windowState);
    }

    public interface IHwZenModeFiltering {
        boolean matchesCallFilter(Context context, int i, ZenModeConfig zenModeConfig, UserHandle userHandle, Bundle bundle, ValidateNotificationPeople validateNotificationPeople, int i2, float f);
    }

    public interface IMultiTaskManagerServiceFactory {
        IBinder getInstance(Context context);
    }

    public interface ISystemBlockMonitor {
        void addMonitor(Monitor monitor);

        void addThread(Handler handler);

        int checkRecentLockedState();

        void init(Context context, ActivityManagerService activityManagerService);

        void startRun();
    }

    public static IHwAppOpsService getHwAppOpsService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwAppOpsService();
        }
        return null;
    }

    public static IHwZenModeFiltering getHwZenModeFiltering() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwZenModeFiltering();
        }
        return null;
    }

    public static IHwWallpaperManagerService getHuaweiWallpaperManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiWallpaperManagerService();
        }
        return null;
    }

    public static String getWallpaperManagerServiceClassName() {
        Factory obj = getImplObject();
        String WALLPAPER_SERVICE_CLASS = "com.android.server.wallpaper.WallpaperManagerService$Lifecycle";
        if (obj != null) {
            return obj.getWallpaperManagerServiceClassName();
        }
        return WALLPAPER_SERVICE_CLASS;
    }

    public static IHwLockSettingsService getHuaweiLockSettingsService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiLockSettingsService();
        }
        return null;
    }

    public static PackageManagerService getHuaweiPackageManagerService(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiPackageManagerService(context, installer, factoryTest, onlyCore);
        }
        return new PackageManagerService(context, installer, factoryTest, onlyCore);
    }

    public static DefaultPermissionGrantPolicy getHwDefaultPermissionGrantPolicy(Context context, PackageManagerService service) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwDefaultPermissionGrantPolicy(context, service);
        }
        return new DefaultPermissionGrantPolicy(service);
    }

    public static IHwWindowManagerService getHuaweiWindowManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiWindowManagerService();
        }
        return null;
    }

    public static IHwAudioService getHwAudioService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwAudioService();
        }
        return null;
    }

    public static IHwBluetoothManagerService getHwBluetoothManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwBluetoothManagerService();
        }
        return DummyHwBluetoothManagerService.getDefault();
    }

    public static IHwWindowStateAnimator getHuaweiWindowStateAnimator() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiWindowStateAnimator();
        }
        return null;
    }

    public static IHwMediaSessionStack getHuaweiMediaSessionStack() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiMediaSessionStack();
        }
        return null;
    }

    public static IHwPowerManagerService getHwPowerManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPowerManagerService();
        }
        return null;
    }

    public static IHwNotificationManagerService getHwNotificationManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNotificationManagerService();
        }
        return null;
    }

    public static IHwNetworkManagermentService getHwNetworkManagermentService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNetworkManagermentService();
        }
        return null;
    }

    public static IHwNetworkPolicyManagerService getHwNetworkPolicyManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNetworkPolicyManagerService();
        }
        return null;
    }

    public static IHwInputMethodManagerService getHwInputMethodManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwInputMethodManagerService();
        }
        return null;
    }

    public static IHwActivityStackSupervisor getHwActivityStackSupervisor() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwActivityStackSupervisor();
        }
        return null;
    }

    public static IHwActivityStarter getHwActivityStarter() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwActivityStarter();
        }
        return null;
    }

    public static IHwAttestationServiceFactory getHwAttestationService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwAttestationService();
        }
        return null;
    }

    public static IMultiTaskManagerServiceFactory getMultiTaskManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getMultiTaskManagerService();
        }
        return null;
    }

    public static IHwActivityManagerService getHwActivityManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            Log.i(TAG, "IHwActivityManagerService getHwActivityManagerService, return obj.getHwActivityManagerService");
            return obj.getHwActivityManagerService();
        }
        Log.i(TAG, "IHwActivityManagerService getHwActivityManagerService, return null");
        return null;
    }

    public static IHwStorageManagerService getHwStorageManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            Log.i(TAG, "IHwStorageManagerService getHwStorageManagerService, return obj.getHwStorageManagerService");
            return obj.getHwStorageManagerService();
        }
        Log.i(TAG, "IHwStorageManagerService getHwStorageManagerService, return null");
        return null;
    }

    public static IHwShutdownThread getHwShutdownThread() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwShutdownThreadImpl();
        }
        return new HwShutdownThreadDummy();
    }

    public static IHwTelephonyRegistry getHwTelephonyRegistry() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwTelephonyRegistry();
        }
        return null;
    }

    public static void setAlarmService(AlarmManagerService alarm) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.setAlarmService(alarm);
        }
    }

    public static IHwUserManagerService getHwUserManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwUserManagerService();
        }
        return null;
    }

    public static void activePlaceFile() {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.activePlaceFile();
        }
    }

    public static IHwActiveServices getHwActiveServices() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwActiveServices();
        }
        return null;
    }

    private static Factory getImplObject() {
        if (obj == null) {
            synchronized (mLock) {
                if (obj == null) {
                    try {
                        obj = (Factory) Class.forName("com.android.server.HwServiceFactoryImpl").newInstance();
                        Log.v(TAG, "get AllImpl object = " + obj);
                    } catch (Exception e) {
                        Log.e(TAG, ": reflection exception is " + e);
                    }
                }
            }
        }
        return obj;
    }

    public static HwConnectivityManager getHwConnectivityManager() {
        return getImplObject().getHwConnectivityManager();
    }

    public static IHwPowerInfoService getHwPowerInfoService(Context context, boolean isSystemReady) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPowerInfoService(context, isSystemReady);
        }
        return null;
    }

    public static StatusBarManagerService createHwStatusBarManagerService(Context context, WindowManagerService windowManager) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createHwStatusBarManagerService(context, windowManager);
        }
        return new StatusBarManagerService(context, windowManager);
    }

    public static IHwBluetoothBigDataService getHwBluetoothBigDataService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwBluetoothBigDataService();
        }
        return null;
    }

    public static IHwIMonitorManager getHwIMonitorManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwIMonitorManager();
        }
        return null;
    }

    public static IHwLocationManagerService getHwLocationManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwLocationManagerService();
        }
        return null;
    }

    public static IHwLocalLocationProvider getHwLocalLocationProvider(Context context, ILocationManager ilocationManager) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwLocalLocationProvider(context, ilocationManager);
        }
        return null;
    }

    public static IHwCmccGpsFeature getHwCmccGpsFeature(Context context, GnssLocationProvider gnssLocationProvider) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwCmccGpsFeature(context, gnssLocationProvider);
        }
        return null;
    }

    public static IHwGpsLogServices getHwGpsLogServices(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwGpsLogServices(context);
        }
        return null;
    }

    public static IHwGpsLogServices getNewHwGpsLogService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getNewHwGpsLogService();
        }
        return null;
    }

    public static IHwGpsLocationCustFeature getHwGpsLocationCustFeature() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwGpsLocationCustFeature();
        }
        return null;
    }

    public static IHwGpsXtraDownloadReceiver getHwGpsXtraDownloadReceiver() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwGpsXtraDownloadReceiver();
        }
        return null;
    }

    public static IHwGpsLocationManager getHwGpsLocationManager(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwGpsLocationManager(context);
        }
        return null;
    }

    public static IHwFingerprintService getHwFingerprintService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwFingerprintService();
        }
        return null;
    }

    public static IFreezeScreenWindowMonitor getWinFreezeScreenMonitor() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getWinFreezeScreenMonitor();
        }
        return null;
    }

    public static boolean isPrivAppInData(File path, String apkListFile) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isPrivAppInData(path, apkListFile);
        }
        return false;
    }

    public static boolean isPrivAppNonSystemPartitionDir(File path) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isPrivAppNonSystemPartitionDir(path);
        }
        return false;
    }

    public static boolean isPrivAppInCust(File file) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isPrivAppInCust(file);
        }
        return false;
    }

    public static boolean isCustedCouldStopped(String pkg, boolean block, boolean stopped) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isCustedCouldStopped(pkg, block, stopped);
        }
        return false;
    }

    public static HwNativeDaemonConnector getHwNativeDaemonConnector() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNativeDaemonConnector();
        }
        return null;
    }

    public static boolean shouldFilteInvalidSensorVal(float lux) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.shouldFilteInvalidSensorVal(lux);
        }
        return false;
    }

    public static void setIfCoverClosed(boolean isClosed) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.setIfCoverClosed(isClosed);
        }
    }

    public static boolean isCoverClosed() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isCoverClosed();
        }
        return false;
    }

    public static boolean clearWipeDataFactoryLowlevel(Context context, Intent intent) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.clearWipeDataFactoryLowlevel(context, intent);
        }
        return false;
    }

    public static boolean clearWipeDataFactory(Context context, Intent intent) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.clearWipeDataFactory(context, intent);
        }
        return false;
    }

    public static HwNLPManager getHwNLPManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNLPManager();
        }
        return DummyHwNLPManager.getDefault();
    }

    public static void setupHwServices(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.setupHwServices(context);
        }
    }

    public static HwPackageServiceManager getHwPackageServiceManager() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwPackageServiceManager();
        }
        return DummyHwPackageServiceManager.getDefault();
    }

    public static GnssLocationProvider createHwGnssLocationProvider(Context context, ILocationManager ilocationManager, Looper looper) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createHwGnssLocationProvider(context, ilocationManager, looper);
        }
        return new GnssLocationProvider(context, ilocationManager, looper);
    }

    public static SyncManager createHwSyncManager(Context context, boolean factoryTest) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createHwSyncManager(context, factoryTest);
        }
        return new SyncManager(context, factoryTest);
    }

    public static IHwAppTransition getHwAppTransition() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwAppTransitionImpl();
        }
        return HwAppTransitionDummy.getDefault();
    }

    public static IHwSmartBackLightController getHwSmartBackLightController() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwSmartBackLightController();
        }
        return null;
    }

    public static IDisplayEngineInterface getDisplayEngineInterface() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getDisplayEngineInterface();
        }
        return null;
    }

    public static IHwAutomaticBrightnessController getHuaweiAutomaticBrightnessController() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiAutomaticBrightnessController();
        }
        return null;
    }

    public static IHwRampAnimator getHwNormalizedRampAnimator() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNormalizedRampAnimator();
        }
        return null;
    }

    public static IDisplayEffectMonitor getDisplayEffectMonitor(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getDisplayEffectMonitor(context);
        }
        return null;
    }

    public static IHwNormalizedManualBrightnessController getHuaweiManualBrightnessController() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiManualBrightnessController();
        }
        return null;
    }

    public static IHwDrmDialogService getHwDrmDialogService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwDrmDialogService();
        }
        return null;
    }

    public static boolean removeProtectAppInPrivacyMode(AuthenticatorDescription desc, boolean removed, Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.removeProtectAppInPrivacyMode(desc, removed, context);
        }
        return false;
    }

    public static IHwInputManagerService getHwInputManagerService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwInputManagerService();
        }
        return null;
    }

    public static String getDeviceStorageMonitorServiceClassName() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getDeviceStorageMonitorServiceClassName();
        }
        return DeviceStorageMonitorService.class.getName();
    }

    public static HwNotificationTethering getHwNotificationTethering(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNotificationTethering(context);
        }
        return new HwNotificationTetheringDummy();
    }

    public static ActivityStack createActivityStack(ActivityContainer activityContainer, RecentTasks recentTasks, boolean onTop) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createActivityStack(activityContainer, recentTasks, onTop);
        }
        return new ActivityStack(activityContainer, recentTasks, onTop);
    }

    public static ActivityRecord createActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityContainer container, ActivityOptions options, ActivityRecord sourceRecord) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createActivityRecord(_service, _caller, _launchedFromPid, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultTo, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisor, container, options, sourceRecord);
        }
        return new ActivityRecord(_service, _caller, _launchedFromPid, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultTo, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisor, container, options, sourceRecord);
    }

    public static ActivityStartInterceptor createActivityStartInterceptor(ActivityManagerService _service, ActivityStackSupervisor _supervisor) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createActivityStartInterceptor(_service, _supervisor);
        }
        return new ActivityStartInterceptor(_service, _supervisor);
    }

    public static AppTransition createHwAppTransition(Context context, WindowManagerService w) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createHwAppTransition(context, w);
        }
        return new AppTransition(context, w);
    }

    public static TaskStack createTaskStack(WindowManagerService service, int stackId) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createTaskStack(service, stackId);
        }
        return new TaskStack(service, stackId);
    }

    public static DisplayContent createDisplayContent(Display display, WindowManagerService service, WindowLayersController layersController, WallpaperController wallpaperController) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createDisplayContent(display, service, layersController, wallpaperController);
        }
        return new DisplayContent(display, service, layersController, wallpaperController);
    }

    public static TaskRecord createTaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, IVoiceInteractionSession _voiceSession, IVoiceInteractor _voiceInteractor, int type) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createTaskRecord(service, _taskId, info, _intent, _voiceSession, _voiceInteractor, type);
        }
        return new TaskRecord(service, _taskId, info, _intent, _voiceSession, _voiceInteractor, type);
    }

    public static TaskRecord createTaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, TaskDescription _taskDescription, TaskThumbnailInfo thumbnailInfo) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createTaskRecord(service, _taskId, info, _intent, _taskDescription, thumbnailInfo);
        }
        return new TaskRecord(service, _taskId, info, _intent, _taskDescription, thumbnailInfo);
    }

    public static TaskRecord createTaskRecord(ActivityManagerService service, int _taskId, Intent _intent, Intent _affinityIntent, String _affinity, String _rootAffinity, ComponentName _realActivity, ComponentName _origActivity, boolean _rootWasReset, boolean _autoRemoveRecents, boolean _askedCompatMode, int _taskType, int _userId, int _effectiveUid, String _lastDescription, ArrayList<ActivityRecord> activities, long _firstActiveTime, long _lastActiveTime, long lastTimeMoved, boolean neverRelinquishIdentity, TaskDescription _lastTaskDescription, TaskThumbnailInfo lastThumbnailInfo, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean privileged, boolean _realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.createTaskRecord(service, _taskId, _intent, _affinityIntent, _affinity, _rootAffinity, _realActivity, _origActivity, _rootWasReset, _autoRemoveRecents, _askedCompatMode, _taskType, _userId, _effectiveUid, _lastDescription, activities, _firstActiveTime, _lastActiveTime, lastTimeMoved, neverRelinquishIdentity, _lastTaskDescription, lastThumbnailInfo, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, privileged, _realActivitySuspended, userSetupComplete, minWidth, minHeight);
        }
        return new TaskRecord(service, _taskId, _intent, _affinityIntent, _affinity, _rootAffinity, _realActivity, _origActivity, _rootWasReset, _autoRemoveRecents, _askedCompatMode, _taskType, _userId, _effectiveUid, _lastDescription, activities, _firstActiveTime, _lastActiveTime, lastTimeMoved, neverRelinquishIdentity, _lastTaskDescription, lastThumbnailInfo, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, privileged, _realActivitySuspended, userSetupComplete, minWidth, minHeight);
    }

    public static AbsHwMtmBroadcastResourceManager getMtmBRManager(HwBroadcastQueue queue) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getMtmBRManagerImpl(queue);
        }
        return null;
    }

    public static ISystemBlockMonitor getISystemBlockMonitor() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getISystemBlockMonitor();
        }
        return null;
    }

    public static IHwBinderMonitor getIHwBinderMonitor() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getIHwBinderMonitor();
        }
        return null;
    }

    public static IHwIpcChecker getIHwIpcChecker(Object object, Handler handler, long waitMaxMillis) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getIHwIpcChecker(object, handler, waitMaxMillis);
        }
        return null;
    }

    public static IHwIpcMonitor getIHwIpcMonitor(Object object, String type, String name) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getIHwIpcMonitor(object, type, name);
        }
        return null;
    }

    public static IDaemonRecoverHandler getIDaemonRecoverHandler() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getIDaemonRecoverHandler();
        }
        return null;
    }

    public static IHwGpsActionReporter getHwGpsActionReporter(Context context, ILocationManager iLocationManager) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwGpsActionReporter(context, iLocationManager);
        }
        return null;
    }

    public static LocationProviderProxy locationProviderProxyCreateAndBind(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.locationProviderProxyCreateAndBind(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        }
        return LocationProviderProxy.createAndBind(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
    }

    public static GeocoderProxy geocoderProxyCreateAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.geocoderProxyCreateAndBind(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        }
        return GeocoderProxy.createAndBind(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
    }

    public static void addHwFmService(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.addHwFmService(context);
        }
    }

    public static void addHwPCManagerService(Context context, ActivityManagerService ams) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.addHwPCManagerService(context, ams);
        }
    }

    public static IHwNetworkStatsService getHwNetworkStatsService() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwNetworkStatsService();
        }
        return null;
    }

    public static ISDCardCryptedHelper getSDCardCryptedHelper() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getSDCardCryptedHelper();
        }
        return null;
    }

    public static IAgpsConnectProvider getAgpsConnectProvider(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getAgpsConnectProvider(context);
        }
        return null;
    }

    public static void reportProximitySensorEventToIAware(boolean positive) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.reportProximitySensorEventToIAware(positive);
        }
    }

    public static void reportVibratorToIAware(int uid) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.reportVibratorToIAware(uid);
        }
    }

    public static IHwForceRotationManagerServiceWrapper getForceRotationManagerServiceWrapper() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getForceRotationManagerServiceWrapper();
        }
        return null;
    }

    public static MediaFocusControl getHwMediaFocusControl(Context cntxt, PlayerFocusEnforcer pfe) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMediaFocusControl(cntxt, pfe);
        }
        return null;
    }

    public static void reportMediaKeyToIAware(int uid) {
        Factory obj = getImplObject();
        if (obj != null && uid > 0) {
            obj.reportMediaKeyToIAware(uid);
        }
    }

    public static ISecurityProfileController getSecurityProfileController() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getSecurityProfileController();
        }
        return null;
    }

    public static IHwScreenRotationAnimation getHwScreenRotationAnimation() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwScreenRotationAnimation();
        }
        return null;
    }

    public static NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        if (pkgName == null) {
            return null;
        }
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getNetLocationStrategy(pkgName, uid, type);
        }
        return null;
    }
}
