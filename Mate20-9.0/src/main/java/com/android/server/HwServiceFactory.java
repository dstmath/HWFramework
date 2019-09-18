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
import android.os.UserHandle;
import android.rms.iaware.NetLocationStrategy;
import android.service.notification.ZenModeConfig;
import android.service.voice.IVoiceInteractionSession;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.IntProperty;
import android.util.Log;
import android.view.Display;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.NetworkManagementService;
import com.android.server.Watchdog;
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
import com.android.server.am.HwBroadcastQueue;
import com.android.server.am.ProcessRecord;
import com.android.server.am.TaskRecord;
import com.android.server.audio.AudioService;
import com.android.server.audio.FocusRequester;
import com.android.server.audio.MediaFocusControl;
import com.android.server.audio.PlayerFocusEnforcer;
import com.android.server.camera.IHwCameraServiceProxy;
import com.android.server.connectivity.HwNotificationTethering;
import com.android.server.connectivity.HwNotificationTetheringDummy;
import com.android.server.content.SyncManager;
import com.android.server.display.AutomaticBrightnessController;
import com.android.server.display.BrightnessMappingStrategy;
import com.android.server.display.DisplayPowerState;
import com.android.server.display.HysteresisLevels;
import com.android.server.display.IHwPersistentDataStoreEx;
import com.android.server.display.IHwUibcReceiver;
import com.android.server.display.IHwWifiDisplayAdapterEx;
import com.android.server.display.IHwWifiDisplayControllerEx;
import com.android.server.display.IPersistentDataStoreInner;
import com.android.server.display.IWifiDisplayAdapterInner;
import com.android.server.display.IWifiDisplayControllerInner;
import com.android.server.display.ManualBrightnessController;
import com.android.server.display.RampAnimator;
import com.android.server.forcerotation.HwForceRotationManagerService;
import com.android.server.input.IHwInputManagerService;
import com.android.server.input.InputManagerService;
import com.android.server.lights.LightsManager;
import com.android.server.lights.LightsService;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.IHwCmccGpsFeature;
import com.android.server.location.IHwGpsActionReporter;
import com.android.server.location.IHwGpsLocationCustFeature;
import com.android.server.location.IHwGpsLocationManager;
import com.android.server.location.IHwGpsLogServices;
import com.android.server.location.IHwGpsXtraDownloadReceiver;
import com.android.server.location.IHwLbsLogger;
import com.android.server.location.IHwLocalLocationProvider;
import com.android.server.location.LocationProviderProxy;
import com.android.server.locksettings.LockSettingsService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsObservers;
import com.android.server.net.NetworkStatsService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.notification.ValidateNotificationPeople;
import com.android.server.os.IFreezeScreenWindowMonitor;
import com.android.server.pm.DummyHwPackageServiceManager;
import com.android.server.pm.HwPackageServiceManager;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserDataPreparer;
import com.android.server.pm.UserManagerService;
import com.android.server.pm.permission.DefaultPermissionGrantPolicy;
import com.android.server.pm.permission.PermissionManagerService;
import com.android.server.policy.IHWExtMotionRotationProcessor;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.HwShutdownThreadDummy;
import com.android.server.power.IHwShutdownThread;
import com.android.server.power.PowerManagerService;
import com.android.server.rms.IHwIpcChecker;
import com.android.server.rms.IHwIpcMonitor;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.security.trustspace.ITrustSpaceController;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.storage.DeviceStorageMonitorService;
import com.android.server.wallpaper.WallpaperManagerService;
import com.android.server.wifipro.IHwWifiProCommonUtilsEx;
import com.android.server.wm.AppTransition;
import com.android.server.wm.DisplayContent;
import com.android.server.wm.DisplayWindowController;
import com.android.server.wm.HwAppTransitionDummy;
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
import java.io.File;
import java.time.Clock;
import java.util.ArrayList;

public class HwServiceFactory {
    private static final String TAG = "HwServiceFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface Factory {
        void activePlaceFile();

        void addHwFmService(Context context);

        void addHwPCManagerService(Context context, ActivityManagerService activityManagerService);

        void clearHwUibcReceiver();

        boolean clearWipeDataFactory(Context context, Intent intent);

        boolean clearWipeDataFactoryLowlevel(Context context, Intent intent);

        ActivityRecord createActivityRecord(ActivityManagerService activityManagerService, ProcessRecord processRecord, int i, int i2, String str, Intent intent, String str2, ActivityInfo activityInfo, Configuration configuration, ActivityRecord activityRecord, String str3, int i3, boolean z, boolean z2, ActivityStackSupervisor activityStackSupervisor, ActivityOptions activityOptions, ActivityRecord activityRecord2);

        ActivityStack createActivityStack(ActivityDisplay activityDisplay, int i, ActivityStackSupervisor activityStackSupervisor, int i2, int i3, boolean z);

        ActivityStartInterceptor createActivityStartInterceptor(ActivityManagerService activityManagerService, ActivityStackSupervisor activityStackSupervisor);

        DisplayContent createDisplayContent(Display display, WindowManagerService windowManagerService, WallpaperController wallpaperController, DisplayWindowController displayWindowController);

        AppTransition createHwAppTransition(Context context, WindowManagerService windowManagerService);

        GnssLocationProvider createHwGnssLocationProvider(Context context, ILocationManager iLocationManager, Looper looper);

        StatusBarManagerService createHwStatusBarManagerService(Context context, WindowManagerService windowManagerService);

        SyncManager createHwSyncManager(Context context, boolean z);

        TaskRecord createTaskRecord(ActivityManagerService activityManagerService, int i, Intent intent, Intent intent2, String str, String str2, ComponentName componentName, ComponentName componentName2, boolean z, boolean z2, boolean z3, int i2, int i3, String str3, ArrayList<ActivityRecord> arrayList, long j, boolean z4, ActivityManager.TaskDescription taskDescription, int i4, int i5, int i6, int i7, int i8, String str4, int i9, boolean z5, boolean z6, boolean z7, int i10, int i11);

        TaskRecord createTaskRecord(ActivityManagerService activityManagerService, int i, ActivityInfo activityInfo, Intent intent, ActivityManager.TaskDescription taskDescription);

        TaskRecord createTaskRecord(ActivityManagerService activityManagerService, int i, ActivityInfo activityInfo, Intent intent, IVoiceInteractionSession iVoiceInteractionSession, IVoiceInteractor iVoiceInteractor);

        TaskStack createTaskStack(WindowManagerService windowManagerService, int i, StackWindowController stackWindowController);

        GeocoderProxy geocoderProxyCreateAndBind(Context context, int i, int i2, int i3, Handler handler);

        String getDeviceStorageMonitorServiceClassName();

        IDisplayEffectMonitor getDisplayEffectMonitor(Context context);

        IDisplayEngineInterface getDisplayEngineInterface();

        IHwForceRotationManagerServiceWrapper getForceRotationManagerServiceWrapper();

        IHWExtMotionRotationProcessor getHWExtMotionRotationProcessor(IHWExtMotionRotationProcessor.WindowOrientationListenerProxy windowOrientationListenerProxy);

        IHwAutomaticBrightnessController getHuaweiAutomaticBrightnessController();

        IHwLockSettingsService getHuaweiLockSettingsService();

        IHwNormalizedManualBrightnessController getHuaweiManualBrightnessController();

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

        IHwAppWindowContainerController getHwAppWindowContainerController();

        IHwAttestationServiceFactory getHwAttestationService();

        IHwAudioService getHwAudioService();

        IHwBluetoothBigDataService getHwBluetoothBigDataService();

        IHwBluetoothManagerService getHwBluetoothManagerService();

        IHwCameraServiceProxy getHwCameraServiceProxy(Context context);

        IHwCmccGpsFeature getHwCmccGpsFeature(Context context, GnssLocationProvider gnssLocationProvider);

        HwConnectivityManager getHwConnectivityManager();

        DefaultPermissionGrantPolicy getHwDefaultPermissionGrantPolicy(Context context, Looper looper, DefaultPermissionGrantPolicy.DefaultPermissionGrantedCallback defaultPermissionGrantedCallback, PermissionManagerService permissionManagerService);

        IHwDrmDialogService getHwDrmDialogService();

        IHwFingerprintService getHwFingerprintService();

        FocusRequester getHwFocusRequester(AudioAttributes audioAttributes, int i, int i2, IAudioFocusDispatcher iAudioFocusDispatcher, IBinder iBinder, String str, MediaFocusControl.AudioFocusDeathHandler audioFocusDeathHandler, String str2, int i3, MediaFocusControl mediaFocusControl, int i4, boolean z);

        FocusRequester getHwFocusRequester(AudioFocusInfo audioFocusInfo, IAudioFocusDispatcher iAudioFocusDispatcher, IBinder iBinder, MediaFocusControl.AudioFocusDeathHandler audioFocusDeathHandler, MediaFocusControl mediaFocusControl, boolean z);

        HwFoldScreenState getHwFoldScreenState(Context context);

        IHwGpsActionReporter getHwGpsActionReporter(Context context, ILocationManager iLocationManager);

        IHwGpsLocationCustFeature getHwGpsLocationCustFeature();

        IHwGpsLocationManager getHwGpsLocationManager(Context context);

        IHwGpsLogServices getHwGpsLogServices(Context context);

        IHwGpsXtraDownloadReceiver getHwGpsXtraDownloadReceiver();

        IHwIMonitorManager getHwIMonitorManager();

        IHwInputManagerService getHwInputManagerService();

        IHwInputMethodManagerService getHwInputMethodManagerService();

        IHwLbsLogger getHwLbsLogger(Context context);

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

        IHwPersistentDataStoreEx getHwPersistentDataStoreEx(IPersistentDataStoreInner iPersistentDataStoreInner);

        IHwPowerManagerService getHwPowerManagerService();

        IHwScreenRotationAnimation getHwScreenRotationAnimation();

        IHwShutdownThread getHwShutdownThreadImpl();

        IHwSmartBackLightController getHwSmartBackLightController();

        IHwStorageManagerService getHwStorageManagerService();

        IHwTelephonyRegistry getHwTelephonyRegistry();

        IHwUibcReceiver getHwUibcReceiver();

        IHwUserManagerService getHwUserManagerService();

        IHwWifiDisplayAdapterEx getHwWifiDisplayAdapterEx(IWifiDisplayAdapterInner iWifiDisplayAdapterInner);

        IHwWifiDisplayControllerEx getHwWifiDisplayControllerEx(IWifiDisplayControllerInner iWifiDisplayControllerInner, Context context, Handler handler);

        IHwZenModeFiltering getHwZenModeFiltering();

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

        ITrustSpaceController getTrustSpaceController();

        String getWallpaperManagerServiceClassName();

        IHwWifiProCommonUtilsEx getWifiProCommonUtilsEx();

        IFreezeScreenWindowMonitor getWinFreezeScreenMonitor();

        IZRHungService getZRHungService();

        boolean hwBrightnessGetData(String str, Bundle bundle, int[] iArr);

        boolean hwBrightnessSetData(String str, Bundle bundle, int[] iArr);

        boolean isCoverClosed();

        boolean isCustedCouldStopped(String str, boolean z, boolean z2);

        void loadHwBrightnessProcessors(AutomaticBrightnessController automaticBrightnessController, ManualBrightnessController manualBrightnessController);

        LocationProviderProxy locationProviderProxyCreateAndBind(Context context, String str, String str2, int i, int i2, int i3, Handler handler);

        boolean removeProtectAppInPrivacyMode(AuthenticatorDescription authenticatorDescription, boolean z, Context context);

        void reportGoogleConn(boolean z);

        void reportMediaKeyToIAware(int i);

        void reportProximitySensorEventToIAware(boolean z);

        void reportSysWakeUp(String str);

        void reportToastHiddenToIAware(int i, int i2);

        void reportVibratorToIAware(int i);

        void setAlarmService(AlarmManagerService alarmManagerService);

        void setIfCoverClosed(boolean z);

        void setPowerState(int i);

        void setupHwServices(Context context);

        boolean shouldFilteInvalidSensorVal(float f);

        void updateLocalesWhenOTA(Context context);

        void updateLocalesWhenOTAEX(Context context, int i);
    }

    public interface IContinuousRebootChecker {
        void checkAbnormalReboot();
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
        ActivityStarter getInstance(ActivityStartController activityStartController, ActivityManagerService activityManagerService, ActivityStackSupervisor activityStackSupervisor, ActivityStartInterceptor activityStartInterceptor);
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
        AutomaticBrightnessController getInstance(AutomaticBrightnessController.Callbacks callbacks, Looper looper, SensorManager sensorManager, BrightnessMappingStrategy brightnessMappingStrategy, int i, int i2, int i3, float f, int i4, int i5, long j, long j2, boolean z, HysteresisLevels hysteresisLevels, Context context);

        AutomaticBrightnessController getInstance(AutomaticBrightnessController.Callbacks callbacks, Looper looper, SensorManager sensorManager, BrightnessMappingStrategy brightnessMappingStrategy, int i, int i2, int i3, float f, Context context);
    }

    public interface IHwBinderMonitor {
        void addBinderPid(ArrayList<Integer> arrayList, int i);

        void writeTransactonToTrace(String str);
    }

    public interface IHwBluetoothBigDataService {
        public static final String GET_OPEN_BT_APP_NAME = "android.bluetooth.GET_OPEN_BT_APP_NAME";

        void sendBigDataEvent(Context context, String str);
    }

    public interface IHwBluetoothManagerService {
        BluetoothManagerService createHwBluetoothManagerService(Context context);
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

    public interface IHwNetworkManagermentService {
        NetworkManagementService getInstance(Context context, String str, NetworkManagementService.SystemServices systemServices);

        boolean handleApLinkedStaListChange(String str, String[] strArr);

        void reportKsiParams(int i, int i2, int i3, int i4);

        void reportVodParams(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10);

        void sendApkDownloadUrlBroadcast(String[] strArr, String str);

        void sendDSCPChangeMessage(String[] strArr, String str);

        void sendDataSpeedSlowMessage(String[] strArr, String str);

        void sendWebStatMessage(String[] strArr, String str);

        void setNativeDaemonConnector(Object obj, Object obj2);

        void startAccessPointWithChannel(WifiConfiguration wifiConfiguration, String str);
    }

    public interface IHwNetworkPolicyManagerService {
        NetworkPolicyManagerService getInstance(Context context, IActivityManager iActivityManager, INetworkManagementService iNetworkManagementService);
    }

    public interface IHwNetworkStatsService {
        NetworkStatsService getInstance(Context context, INetworkManagementService iNetworkManagementService, AlarmManager alarmManager, PowerManager.WakeLock wakeLock, Clock clock, TelephonyManager telephonyManager, NetworkStatsService.NetworkStatsSettings networkStatsSettings, NetworkStatsObservers networkStatsObservers, File file, File file2);
    }

    public interface IHwNormalizedManualBrightnessController {
        ManualBrightnessController getInstance(ManualBrightnessController.ManualBrightnessCallbacks manualBrightnessCallbacks, Context context, SensorManager sensorManager);
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
        void addMonitor(Watchdog.Monitor monitor);

        void addThread(Handler handler);

        int checkRecentLockedState();

        void init(Context context, ActivityManagerService activityManagerService);

        void startRun();
    }

    public static IHwAppOpsService getHwAppOpsService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAppOpsService();
        }
        return null;
    }

    public static IHwZenModeFiltering getHwZenModeFiltering() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwZenModeFiltering();
        }
        return null;
    }

    public static IHwWallpaperManagerService getHuaweiWallpaperManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiWallpaperManagerService();
        }
        return null;
    }

    public static String getWallpaperManagerServiceClassName() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getWallpaperManagerServiceClassName();
        }
        return "com.android.server.wallpaper.WallpaperManagerService$Lifecycle";
    }

    public static IHwLockSettingsService getHuaweiLockSettingsService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiLockSettingsService();
        }
        return null;
    }

    public static PackageManagerService getHuaweiPackageManagerService(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiPackageManagerService(context, installer, factoryTest, onlyCore);
        }
        return new PackageManagerService(context, installer, factoryTest, onlyCore);
    }

    public static DefaultPermissionGrantPolicy getHwDefaultPermissionGrantPolicy(Context context, Looper looper, DefaultPermissionGrantPolicy.DefaultPermissionGrantedCallback callback, PermissionManagerService permissionManager) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwDefaultPermissionGrantPolicy(context, looper, callback, permissionManager);
        }
        return new DefaultPermissionGrantPolicy(context, looper, callback, permissionManager);
    }

    public static IHwWindowManagerService getHuaweiWindowManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiWindowManagerService();
        }
        return null;
    }

    public static IHwAudioService getHwAudioService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAudioService();
        }
        return null;
    }

    public static IHwBluetoothManagerService getHwBluetoothManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwBluetoothManagerService();
        }
        return DummyHwBluetoothManagerService.getDefault();
    }

    public static IHwWindowStateAnimator getHuaweiWindowStateAnimator() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiWindowStateAnimator();
        }
        return null;
    }

    public static IHwPowerManagerService getHwPowerManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPowerManagerService();
        }
        return null;
    }

    public static IHwNotificationManagerService getHwNotificationManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNotificationManagerService();
        }
        return null;
    }

    public static IHwNetworkManagermentService getHwNetworkManagermentService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNetworkManagermentService();
        }
        return null;
    }

    public static IHwNetworkPolicyManagerService getHwNetworkPolicyManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNetworkPolicyManagerService();
        }
        return null;
    }

    public static IHwInputMethodManagerService getHwInputMethodManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwInputMethodManagerService();
        }
        return null;
    }

    public static IHwActivityStackSupervisor getHwActivityStackSupervisor() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwActivityStackSupervisor();
        }
        return null;
    }

    public static IHwActivityStarter getHwActivityStarter() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwActivityStarter();
        }
        return null;
    }

    public static IHwAttestationServiceFactory getHwAttestationService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAttestationService();
        }
        return null;
    }

    public static IMultiTaskManagerServiceFactory getMultiTaskManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getMultiTaskManagerService();
        }
        return null;
    }

    public static IHwActivityManagerService getHwActivityManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            Log.i(TAG, "IHwActivityManagerService getHwActivityManagerService, return obj.getHwActivityManagerService");
            return obj2.getHwActivityManagerService();
        }
        Log.i(TAG, "IHwActivityManagerService getHwActivityManagerService, return null");
        return null;
    }

    public static IHwStorageManagerService getHwStorageManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            Log.i(TAG, "IHwStorageManagerService getHwStorageManagerService, return obj.getHwStorageManagerService");
            return obj2.getHwStorageManagerService();
        }
        Log.i(TAG, "IHwStorageManagerService getHwStorageManagerService, return null");
        return null;
    }

    public static IHwShutdownThread getHwShutdownThread() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwShutdownThreadImpl();
        }
        return new HwShutdownThreadDummy();
    }

    public static IHwTelephonyRegistry getHwTelephonyRegistry() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwTelephonyRegistry();
        }
        return null;
    }

    public static void setAlarmService(AlarmManagerService alarm) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.setAlarmService(alarm);
        }
    }

    public static IHwUserManagerService getHwUserManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwUserManagerService();
        }
        return null;
    }

    public static void activePlaceFile() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.activePlaceFile();
        }
    }

    public static IHwActiveServices getHwActiveServices() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwActiveServices();
        }
        return null;
    }

    public static IHwUibcReceiver getHwUibcReceiver() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwUibcReceiver();
        }
        return null;
    }

    public static void clearHwUibcReceiver() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.clearHwUibcReceiver();
        }
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

    public static IHwCameraServiceProxy getHwCameraServiceProxy(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwCameraServiceProxy(context);
        }
        return null;
    }

    public static StatusBarManagerService createHwStatusBarManagerService(Context context, WindowManagerService windowManager) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createHwStatusBarManagerService(context, windowManager);
        }
        return new StatusBarManagerService(context, windowManager);
    }

    public static IHwBluetoothBigDataService getHwBluetoothBigDataService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwBluetoothBigDataService();
        }
        return null;
    }

    public static IHwIMonitorManager getHwIMonitorManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwIMonitorManager();
        }
        return null;
    }

    public static IZRHungService getZRHungService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getZRHungService();
        }
        return null;
    }

    public static IHwLocationManagerService getHwLocationManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLocationManagerService();
        }
        return null;
    }

    public static IHwLocalLocationProvider getHwLocalLocationProvider(Context context, ILocationManager ilocationManager) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLocalLocationProvider(context, ilocationManager);
        }
        return null;
    }

    public static IHwCmccGpsFeature getHwCmccGpsFeature(Context context, GnssLocationProvider gnssLocationProvider) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwCmccGpsFeature(context, gnssLocationProvider);
        }
        return null;
    }

    public static IHwGpsLogServices getHwGpsLogServices(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwGpsLogServices(context);
        }
        return null;
    }

    public static IHwGpsLogServices getNewHwGpsLogService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getNewHwGpsLogService();
        }
        return null;
    }

    public static IHwGpsLocationCustFeature getHwGpsLocationCustFeature() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwGpsLocationCustFeature();
        }
        return null;
    }

    public static IHwGpsXtraDownloadReceiver getHwGpsXtraDownloadReceiver() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwGpsXtraDownloadReceiver();
        }
        return null;
    }

    public static IHwGpsLocationManager getHwGpsLocationManager(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwGpsLocationManager(context);
        }
        return null;
    }

    public static IHwFingerprintService getHwFingerprintService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwFingerprintService();
        }
        return null;
    }

    public static IFreezeScreenWindowMonitor getWinFreezeScreenMonitor() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getWinFreezeScreenMonitor();
        }
        return null;
    }

    public static boolean isCustedCouldStopped(String pkg, boolean block, boolean stopped) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isCustedCouldStopped(pkg, block, stopped);
        }
        return false;
    }

    public static HwNativeDaemonConnector getHwNativeDaemonConnector() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNativeDaemonConnector();
        }
        return null;
    }

    public static boolean shouldFilteInvalidSensorVal(float lux) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.shouldFilteInvalidSensorVal(lux);
        }
        return false;
    }

    public static void setIfCoverClosed(boolean isClosed) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.setIfCoverClosed(isClosed);
        }
    }

    public static boolean isCoverClosed() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isCoverClosed();
        }
        return false;
    }

    public static boolean clearWipeDataFactoryLowlevel(Context context, Intent intent) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.clearWipeDataFactoryLowlevel(context, intent);
        }
        return false;
    }

    public static boolean clearWipeDataFactory(Context context, Intent intent) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.clearWipeDataFactory(context, intent);
        }
        return false;
    }

    public static HwNLPManager getHwNLPManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNLPManager();
        }
        return DummyHwNLPManager.getDefault();
    }

    public static void setupHwServices(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.setupHwServices(context);
        }
    }

    public static HwPackageServiceManager getHwPackageServiceManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPackageServiceManager();
        }
        return DummyHwPackageServiceManager.getDefault();
    }

    public static GnssLocationProvider createHwGnssLocationProvider(Context context, ILocationManager ilocationManager, Looper looper) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createHwGnssLocationProvider(context, ilocationManager, looper);
        }
        return new GnssLocationProvider(context, ilocationManager, looper);
    }

    public static SyncManager createHwSyncManager(Context context, boolean factoryTest) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createHwSyncManager(context, factoryTest);
        }
        return new SyncManager(context, factoryTest);
    }

    public static IHwAppTransition getHwAppTransition() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAppTransitionImpl();
        }
        return HwAppTransitionDummy.getDefault();
    }

    public static IHwSmartBackLightController getHwSmartBackLightController() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwSmartBackLightController();
        }
        return null;
    }

    public static IDisplayEngineInterface getDisplayEngineInterface() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getDisplayEngineInterface();
        }
        return null;
    }

    public static IHwAutomaticBrightnessController getHuaweiAutomaticBrightnessController() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiAutomaticBrightnessController();
        }
        return null;
    }

    public static IHwRampAnimator getHwNormalizedRampAnimator() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNormalizedRampAnimator();
        }
        return null;
    }

    public static IDisplayEffectMonitor getDisplayEffectMonitor(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getDisplayEffectMonitor(context);
        }
        return null;
    }

    public static IHwNormalizedManualBrightnessController getHuaweiManualBrightnessController() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiManualBrightnessController();
        }
        return null;
    }

    public static IHwDrmDialogService getHwDrmDialogService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwDrmDialogService();
        }
        return null;
    }

    public static boolean removeProtectAppInPrivacyMode(AuthenticatorDescription desc, boolean removed, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.removeProtectAppInPrivacyMode(desc, removed, context);
        }
        return false;
    }

    public static IHwInputManagerService getHwInputManagerService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwInputManagerService();
        }
        return null;
    }

    public static String getDeviceStorageMonitorServiceClassName() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getDeviceStorageMonitorServiceClassName();
        }
        return DeviceStorageMonitorService.class.getName();
    }

    public static HwNotificationTethering getHwNotificationTethering(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNotificationTethering(context);
        }
        return new HwNotificationTetheringDummy();
    }

    public static ActivityStack createActivityStack(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, int windowingMode, int activityType, boolean onTop) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createActivityStack(display, stackId, supervisor, windowingMode, activityType, onTop);
        }
        ActivityStack activityStack = new ActivityStack(display, stackId, supervisor, windowingMode, activityType, onTop);
        return activityStack;
    }

    public static ActivityRecord createActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityOptions options, ActivityRecord sourceRecord) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createActivityRecord(_service, _caller, _launchedFromPid, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultTo, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisor, options, sourceRecord);
        }
        ActivityRecord activityRecord = new ActivityRecord(_service, _caller, _launchedFromPid, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultTo, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisor, options, sourceRecord);
        return activityRecord;
    }

    public static ActivityStartInterceptor createActivityStartInterceptor(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createActivityStartInterceptor(service, supervisor);
        }
        return new ActivityStartInterceptor(service, supervisor);
    }

    public static AppTransition createHwAppTransition(Context context, WindowManagerService w) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createHwAppTransition(context, w);
        }
        return new AppTransition(context, w);
    }

    public static TaskStack createTaskStack(WindowManagerService service, int stackId, StackWindowController controller) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createTaskStack(service, stackId, controller);
        }
        return new TaskStack(service, stackId, controller);
    }

    public static DisplayContent createDisplayContent(Display display, WindowManagerService service, WallpaperController wallpaperController, DisplayWindowController controller) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createDisplayContent(display, service, wallpaperController, controller);
        }
        return new DisplayContent(display, service, wallpaperController, controller);
    }

    public static TaskRecord createTaskRecord(ActivityManagerService service, int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createTaskRecord(service, taskId, info, intent, voiceSession, voiceInteractor);
        }
        TaskRecord taskRecord = new TaskRecord(service, taskId, info, intent, voiceSession, voiceInteractor);
        return taskRecord;
    }

    public static TaskRecord createTaskRecord(ActivityManagerService service, int taskId, ActivityInfo info, Intent intent, ActivityManager.TaskDescription taskDescription) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createTaskRecord(service, taskId, info, intent, taskDescription);
        }
        TaskRecord taskRecord = new TaskRecord(service, taskId, info, intent, taskDescription);
        return taskRecord;
    }

    public static TaskRecord createTaskRecord(ActivityManagerService service, int taskId, Intent intent, Intent affinityIntent, String affinity, String rootAffinity, ComponentName realActivity, ComponentName origActivity, boolean rootWasReset, boolean autoRemoveRecents, boolean askedCompatMode, int userId, int effectiveUid, String lastDescription, ArrayList<ActivityRecord> activities, long lastTimeMoved, boolean neverRelinquishIdentity, ActivityManager.TaskDescription lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createTaskRecord(service, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootWasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeMoved, neverRelinquishIdentity, lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete, minWidth, minHeight);
        }
        TaskRecord taskRecord = new TaskRecord(service, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootWasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeMoved, neverRelinquishIdentity, lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete, minWidth, minHeight);
        return taskRecord;
    }

    public static AbsHwMtmBroadcastResourceManager getMtmBRManager(HwBroadcastQueue queue) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getMtmBRManagerImpl(queue);
        }
        return null;
    }

    public static ISystemBlockMonitor getISystemBlockMonitor() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getISystemBlockMonitor();
        }
        return null;
    }

    public static IHwBinderMonitor getIHwBinderMonitor() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getIHwBinderMonitor();
        }
        return null;
    }

    public static IHwIpcChecker getIHwIpcChecker(Object object, Handler handler, long waitMaxMillis) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getIHwIpcChecker(object, handler, waitMaxMillis);
        }
        return null;
    }

    public static IHwIpcMonitor getIHwIpcMonitor(Object object, String type, String name) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getIHwIpcMonitor(object, type, name);
        }
        return null;
    }

    public static IHwGpsActionReporter getHwGpsActionReporter(Context context, ILocationManager iLocationManager) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwGpsActionReporter(context, iLocationManager);
        }
        return null;
    }

    public static LocationProviderProxy locationProviderProxyCreateAndBind(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.locationProviderProxyCreateAndBind(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        }
        return LocationProviderProxy.createAndBind(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
    }

    public static GeocoderProxy geocoderProxyCreateAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.geocoderProxyCreateAndBind(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        }
        return GeocoderProxy.createAndBind(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
    }

    public static void addHwFmService(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.addHwFmService(context);
        }
    }

    public static void addHwPCManagerService(Context context, ActivityManagerService ams) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.addHwPCManagerService(context, ams);
        }
    }

    public static IHwNetworkStatsService getHwNetworkStatsService() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNetworkStatsService();
        }
        return null;
    }

    public static ISDCardCryptedHelper getSDCardCryptedHelper() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getSDCardCryptedHelper();
        }
        return null;
    }

    public static void updateLocalesWhenOTA(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.updateLocalesWhenOTA(context);
        }
    }

    public static void updateLocalesWhenOTAEX(Context context, int preSdkVersion) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.updateLocalesWhenOTAEX(context, preSdkVersion);
        }
    }

    public static void reportProximitySensorEventToIAware(boolean positive) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.reportProximitySensorEventToIAware(positive);
        }
    }

    public static void reportVibratorToIAware(int uid) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.reportVibratorToIAware(uid);
        }
    }

    public static IHwForceRotationManagerServiceWrapper getForceRotationManagerServiceWrapper() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getForceRotationManagerServiceWrapper();
        }
        return null;
    }

    public static MediaFocusControl getHwMediaFocusControl(Context cntxt, PlayerFocusEnforcer pfe) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMediaFocusControl(cntxt, pfe);
        }
        return null;
    }

    public static FocusRequester getHwFocusRequester(AudioAttributes aa, int focusRequest, int grantFlags, IAudioFocusDispatcher afl, IBinder source, String id, MediaFocusControl.AudioFocusDeathHandler hdlr, String pn, int uid, MediaFocusControl ctlr, int sdk, boolean isInExternal) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwFocusRequester(aa, focusRequest, grantFlags, afl, source, id, hdlr, pn, uid, ctlr, sdk, isInExternal);
        }
        FocusRequester focusRequester = new FocusRequester(aa, focusRequest, grantFlags, afl, source, id, hdlr, pn, uid, ctlr, sdk);
        return focusRequester;
    }

    public static FocusRequester getHwFocusRequester(AudioFocusInfo afi, IAudioFocusDispatcher afl, IBinder source, MediaFocusControl.AudioFocusDeathHandler hdlr, MediaFocusControl ctlr, boolean isInExternal) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwFocusRequester(afi, afl, source, hdlr, ctlr, isInExternal);
        }
        FocusRequester focusRequester = new FocusRequester(afi, afl, source, hdlr, ctlr);
        return focusRequester;
    }

    public static void reportMediaKeyToIAware(int uid) {
        Factory obj2 = getImplObject();
        if (obj2 != null && uid > 0) {
            obj2.reportMediaKeyToIAware(uid);
        }
    }

    public static ISecurityProfileController getSecurityProfileController() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getSecurityProfileController();
        }
        return null;
    }

    public static ITrustSpaceController getTrustSpaceController() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getTrustSpaceController();
        }
        return null;
    }

    public static IHwScreenRotationAnimation getHwScreenRotationAnimation() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwScreenRotationAnimation();
        }
        return null;
    }

    public static void reportToastHiddenToIAware(int pid, int hcode) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.reportToastHiddenToIAware(pid, hcode);
        }
    }

    public static void reportSysWakeUp(String reason) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.reportSysWakeUp(reason);
        }
    }

    public static NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) {
        if (pkgName == null) {
            return null;
        }
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getNetLocationStrategy(pkgName, uid, type);
        }
        return null;
    }

    public static IHWExtMotionRotationProcessor getHWExtMotionRotationProcessor(IHWExtMotionRotationProcessor.WindowOrientationListenerProxy proxy) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHWExtMotionRotationProcessor(proxy);
        }
        return null;
    }

    public static void setPowerState(int powerState) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.setPowerState(powerState);
        }
    }

    public static void reportGoogleConn(boolean conn) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.reportGoogleConn(conn);
        }
    }

    public static IHwLbsLogger getHwLbsLogger(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLbsLogger(context);
        }
        return null;
    }

    public static boolean hwBrightnessSetData(String name, Bundle data, int[] result) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.hwBrightnessSetData(name, data, result);
        }
        return false;
    }

    public static boolean hwBrightnessGetData(String name, Bundle data, int[] result) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.hwBrightnessGetData(name, data, result);
        }
        return false;
    }

    public static void loadHwBrightnessProcessors(AutomaticBrightnessController autoController, ManualBrightnessController manualController) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.loadHwBrightnessProcessors(autoController, manualController);
        }
    }

    public static IHwAppWindowContainerController getHwAppWindowContainerController() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAppWindowContainerController();
        }
        return null;
    }

    public static IHwWifiDisplayAdapterEx getHwWifiDisplayAdapterEx(IWifiDisplayAdapterInner wfda) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwWifiDisplayAdapterEx(wfda);
        }
        return null;
    }

    public static IHwWifiDisplayControllerEx getHwWifiDisplayControllerEx(IWifiDisplayControllerInner wfdc, Context context, Handler handler) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwWifiDisplayControllerEx(wfdc, context, handler);
        }
        return null;
    }

    public static IHwWifiProCommonUtilsEx getWifiProCommonUtilsEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getWifiProCommonUtilsEx();
        }
        return null;
    }

    public static HwFoldScreenState getHwFoldScreenState(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwFoldScreenState(context);
        }
        return null;
    }

    public static IHwPersistentDataStoreEx getHwPersistentDataStoreEx(IPersistentDataStoreInner pds) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPersistentDataStoreEx(pds);
        }
        return null;
    }
}
