package com.android.server;

import android.accounts.AuthenticatorDescription;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.encrypt.ISDCardCryptedHelper;
import android.encrypt.SDCardCryptedHelper;
import android.hardware.Sensor;
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
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.HwSysResManager;
import android.rms.iaware.NetLocationStrategy;
import android.service.notification.ZenModeConfig;
import android.service.voice.IVoiceInteractionSession;
import android.service.voice.IVoiceInteractionSessionEx;
import android.util.IntProperty;
import android.util.Slog;
import android.view.Display;
import com.android.commgmt.HwDFRServicesFactory;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.HwServiceFactory;
import com.android.server.NetworkManagementService;
import com.android.server.accounts.HwAccountHelper;
import com.android.server.adb.AdbService;
import com.android.server.am.AbsHwMtmBroadcastResourceManager;
import com.android.server.am.ActiveServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.am.BroadcastQueueExUtils;
import com.android.server.am.HwActiveServices;
import com.android.server.am.HwActivityManagerService;
import com.android.server.am.HwBroadcastQueue;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.aps.HwApsManagerService;
import com.android.server.audio.AudioService;
import com.android.server.audio.FocusRequester;
import com.android.server.audio.HwAudioService;
import com.android.server.audio.HwFocusRequester;
import com.android.server.audio.HwMediaFocusControl;
import com.android.server.audio.MediaFocusControl;
import com.android.server.audio.PlayerFocusEnforcer;
import com.android.server.camera.IHwCameraServiceProxy;
import com.android.server.connectivity.HwNotificationTethering;
import com.android.server.connectivity.HwNotificationTetheringImpl;
import com.android.server.connectivity.HwTetheringEx;
import com.android.server.connectivity.IHwTetheringEx;
import com.android.server.content.HwSyncManager;
import com.android.server.content.SyncManager;
import com.android.server.display.AutomaticBrightnessController;
import com.android.server.display.BrightnessMappingStrategy;
import com.android.server.display.DisplayEffectMonitor;
import com.android.server.display.DisplayEngineService;
import com.android.server.display.DisplayPowerState;
import com.android.server.display.FoldPolicy;
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
import com.android.server.input.IHwInputManagerService;
import com.android.server.input.InputManagerService;
import com.android.server.inputmethod.HwInputMethodManagerService;
import com.android.server.inputmethod.InputMethodManagerService;
import com.android.server.jankshield.JankShieldService;
import com.android.server.lights.LightsManager;
import com.android.server.lights.LightsService;
import com.android.server.location.AbstractLocationProvider;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GpsFreezeProc;
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
import com.android.server.location.HwNullQuickTTFFMonitor;
import com.android.server.location.HwQuickTTFFMonitor;
import com.android.server.location.IGpsFreezeProc;
import com.android.server.location.IHwCmccGpsFeature;
import com.android.server.location.IHwGnssLocationProvider;
import com.android.server.location.IHwGpsActionReporter;
import com.android.server.location.IHwGpsLocationCustFeature;
import com.android.server.location.IHwGpsLocationManager;
import com.android.server.location.IHwGpsLogServices;
import com.android.server.location.IHwGpsXtraDownloadReceiver;
import com.android.server.location.IHwLocalLocationProvider;
import com.android.server.location.IHwQuickTTFFMonitor;
import com.android.server.location.LocationProviderProxy;
import com.android.server.locksettings.HwLockSettingsService;
import com.android.server.locksettings.LockSettingsService;
import com.android.server.net.HwNetworkPolicyManagerService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.notification.HwNotificationManagerService;
import com.android.server.notification.HwZenModeFiltering;
import com.android.server.notification.NotificationManagerService;
import com.android.server.notification.ValidateNotificationPeople;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.pm.HwPackageServiceManager;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.pm.UserDataPreparer;
import com.android.server.pm.UserDataPreparerEx;
import com.android.server.pm.UserManagerService;
import com.android.server.pm.permission.DefaultPermissionGrantPolicy;
import com.android.server.pm.permission.PermissionManagerService;
import com.android.server.pm.permission.PermissionManagerServiceEx;
import com.android.server.policy.HWExtMotionRotationProcessorEx;
import com.android.server.policy.IHWExtMotionRotationProcessor;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.HwDisplayPowerController;
import com.android.server.power.HwPowerManagerService;
import com.android.server.power.HwShutdownThreadImpl;
import com.android.server.power.IHwShutdownThread;
import com.android.server.power.PowerManagerService;
import com.android.server.rms.IHwIpcMonitor;
import com.android.server.rms.ipcchecker.HwIpcMonitorImpl;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.security.trustspace.ITrustSpaceController;
import com.android.server.statusbar.HwStatusBarManagerService;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.storage.HwDeviceStorageMonitorService;
import com.android.server.swing.HwSwingPolicyService;
import com.android.server.usb.HwUsbManagerExService;
import com.android.server.wifipro.IHwWifiProCommonUtilsEx;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.ActivityDisplay;
import com.android.server.wm.ActivityDisplayEx;
import com.android.server.wm.ActivityRecord;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.ActivityStackEx;
import com.android.server.wm.ActivityStackSupervisor;
import com.android.server.wm.ActivityStackSupervisorEx;
import com.android.server.wm.ActivityStartController;
import com.android.server.wm.ActivityStartInterceptor;
import com.android.server.wm.ActivityStarter;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.AppTransition;
import com.android.server.wm.DisplayContent;
import com.android.server.wm.HwActivityStarter;
import com.android.server.wm.HwAppTransition;
import com.android.server.wm.HwAppTransitionImpl;
import com.android.server.wm.HwRogEx;
import com.android.server.wm.HwWindowManagerService;
import com.android.server.wm.IHwAppTransition;
import com.android.server.wm.IHwAppWindowTokenEx;
import com.android.server.wm.IHwScreenRotationAnimation;
import com.android.server.wm.TaskRecord;
import com.android.server.wm.TaskStack;
import com.android.server.wm.TransactionFactory;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowManagerServiceEx;
import com.android.server.wm.WindowProcessController;
import com.android.server.wm.WindowProcessControllerEx;
import com.android.server.wm.WindowState;
import com.android.server.wm.WindowStateAnimator;
import com.android.server.wm.WindowStateEx;
import com.android.server.zrhung.IHwBinderMonitor;
import com.android.server.zrhung.IZRHungService;
import com.huawei.android.hardware.fmradio.HwFmService;
import com.huawei.android.internal.app.IVoiceInteractorEx;
import com.huawei.displayengine.DisplayEngineInterface;
import com.huawei.server.HwBasicPlatformFactory;
import com.huawei.server.HwPCFactory;
import com.huawei.server.HwPartFsmFactory;
import com.huawei.server.HwPartIawareServiceFactory;
import com.huawei.server.HwPartIawareServiceFactoryEx;
import com.huawei.server.HwPartMagicWindowServiceFactory;
import com.huawei.server.camera.HwCameraServiceFactory;
import com.huawei.server.security.HwServiceSecurityPartsFactory;
import huawei.android.aod.HwAodManager;
import huawei.android.os.HwGeneralManager;
import java.io.File;
import java.util.ArrayList;

public class HwServiceFactoryImpl implements HwServiceFactory.Factory {
    private static final String TAG = "HwServiceFactoryImpl";
    private static DisplayEngineInterface sDisplayEngineInterface = null;
    private static HWDrmDialogsService sDrmDialogService = null;
    private static HwSmartBackLightNormalizedController sHwSmartBackLightController = null;

    public HwServiceFactory.IHwZenModeFiltering getHwZenModeFiltering() {
        return new HwZenModeFilteringImpl();
    }

    public AlarmManagerService getHwAlarmManagerService(Context context) {
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwAlarmManagerService(context).getAlarmManagerService();
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

    public static class HwAudioServiceImpl implements HwServiceFactory.IHwAudioService {
        public AudioService getInstance(Context context) {
            return new HwAudioService(context);
        }
    }

    public HwServiceFactory.IHwAudioService getHwAudioService() {
        return new HwAudioServiceImpl();
    }

    public static class HwBluetoothManagerServiceImpl implements HwServiceFactory.IHwBluetoothManagerService {
        public BluetoothManagerService createHwBluetoothManagerService(Context context) {
            return new HwBluetoothManagerService(context);
        }
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

    public static class HwLockSettingsServiceImpl implements HwServiceFactory.IHwLockSettingsService {
        public LockSettingsService getInstance(Context context) {
            return new HwLockSettingsService(context);
        }
    }

    public static class HwPowerManagerServiceImpl implements HwServiceFactory.IHwPowerManagerService {
        public PowerManagerService getInstance(Context context) {
            return new HwPowerManagerService(context);
        }
    }

    public static class HwNotificationManagerServiceImpl implements HwServiceFactory.IHwNotificationManagerService {
        public NotificationManagerService getInstance(Context context, StatusBarManagerService statusBar, LightsService lights) {
            return new HwNotificationManagerService(context, statusBar, lights);
        }
    }

    public static class HwNetworkManagementServiceImpl implements HwServiceFactory.IHwNetworkManagermentService {
        private HwNetworkManagementService mHwNMService;

        public NetworkManagementService getInstance(Context context, NetworkManagementService.SystemServices services) {
            this.mHwNMService = new HwNetworkManagementService(context, services);
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

    public static class HwInputMethodManagerServiceImpl implements HwServiceFactory.IHwInputMethodManagerService {
        public InputMethodManagerService getInstance(Context context) {
            return new HwInputMethodManagerService(context);
        }
    }

    public static class HwActivityStackSupervisorImpl implements HwServiceFactory.IHwActivityStackSupervisor {
        public ActivityStackSupervisor getInstance(ActivityTaskManagerService service, Looper looper) {
            ActivityTaskManagerServiceEx activityTaskManagerServiceEx = new ActivityTaskManagerServiceEx();
            activityTaskManagerServiceEx.setActivityTaskManagerService(service);
            return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwActivityStackSupervisor(activityTaskManagerServiceEx, looper).getActivityStackSupervisor();
        }
    }

    public static class HwActivityStarterImpl implements HwServiceFactory.IHwActivityStarter {
        public ActivityStarter getInstance(ActivityStartController controller, ActivityTaskManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
            return new HwActivityStarter(controller, service, supervisor, interceptor);
        }
    }

    public PackageManagerService getHuaweiPackageManagerService(Context context, Installer installer, boolean isFactoryTest, boolean isOnlyCore) {
        return HwPackageManagerService.getInstance(context, installer, isFactoryTest, isOnlyCore);
    }

    public DefaultPermissionGrantPolicy getHwDefaultPermissionGrantPolicy(Context context, Looper looper, PermissionManagerService permissionManager) {
        PermissionManagerServiceEx serviceEx = new PermissionManagerServiceEx();
        serviceEx.setPermissionManagerService(permissionManager);
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getDefaultPermissionGrantPolicy(context, looper, serviceEx).getDefaultPermissionGrantPolicy();
    }

    public HwServiceFactory.IHwWindowStateAnimator getHuaweiWindowStateAnimator() {
        return new HwWindowStateAnimatorImpl();
    }

    public static class HwWindowStateAnimatorImpl implements HwServiceFactory.IHwWindowStateAnimator {
        public WindowStateAnimator getInstance(WindowState win) {
            return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).createWindowStateAnimator(new WindowStateEx(win)).getWindowStateAnimatorBridge();
        }
    }

    public HwServiceFactory.IHwWindowManagerService getHuaweiWindowManagerService() {
        return new HwWindowManagerServiceImpl();
    }

    public static class HwWindowManagerServiceImpl implements HwServiceFactory.IHwWindowManagerService {
        public WindowManagerService getInstance(Context context, InputManagerService inputManager, boolean isShowBootMsgs, boolean isOnlyCore, WindowManagerPolicy policy, ActivityTaskManagerService atm, TransactionFactory transactionFactory) {
            return new HwWindowManagerService(context, inputManager, isShowBootMsgs, isOnlyCore, policy, atm, transactionFactory);
        }
    }

    public HwServiceFactory.IHwUserManagerService getHwUserManagerService() {
        return new HwUserManagerServiceImpl();
    }

    public static class HwUserManagerServiceImpl implements HwServiceFactory.IHwUserManagerService {
        public UserManagerService getInstance(Context context, PackageManagerService pm, UserDataPreparer userDataPreparer, Object packagesLock) {
            PackageManagerServiceEx pmEx = new PackageManagerServiceEx();
            pmEx.setPackageManagerService(pm);
            UserDataPreparerEx userDataPreparerEx = new UserDataPreparerEx();
            userDataPreparerEx.setUserDataPreparer(userDataPreparer);
            return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getUserManagerService(context, pmEx, userDataPreparerEx, packagesLock).getUserManagerService();
        }
    }

    public static class HwZenModeFilteringImpl implements HwServiceFactory.IHwZenModeFiltering {
        public boolean matchesCallFilter(Context context, int zen, NotificationManager.Policy consolidatedPolicy, ZenModeConfig config, UserHandle userHandle, Bundle extras, ValidateNotificationPeople validator, int contactsTimeoutMs, float timeoutAffinity) {
            return HwZenModeFiltering.matchesCallFilter(context, zen, consolidatedPolicy, config, userHandle, extras, validator, contactsTimeoutMs, timeoutAffinity);
        }

        public void initZenmodeChangeObserver(Context context) {
            new HwZenModeFiltering(context).initZenmodeChangeObserver();
        }
    }

    public StatusBarManagerService createHwStatusBarManagerService(Context context, WindowManagerService windowManager) {
        return new HwStatusBarManagerService(context, windowManager);
    }

    public static class HwActivityManagerServiceImpl implements HwServiceFactory.IHwActivityManagerService {
        public ActivityManagerService getInstance(Context context, ActivityTaskManagerService atm) {
            Slog.i(HwServiceFactoryImpl.TAG, "HwActivityManagerServiceImpl getInstance starts");
            return new HwActivityManagerService(context, atm);
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

    public HwServiceFactory.IHwActiveServices getHwActiveServices() {
        return new HwActiveServicesImpl();
    }

    public static class HwActiveServicesImpl implements HwServiceFactory.IHwActiveServices {
        public ActiveServices getInstance(ActivityManagerService ams) {
            return new HwActiveServices(ams);
        }
    }

    public void setAlarmService(AlarmManagerService alarm) {
        SMCSAMSHelper.getInstance().setAlarmService(alarm);
    }

    public boolean isPrivAppNonSystemPartitionDir(File path) {
        return false;
    }

    public boolean isCustedCouldStopped(String pkg, boolean isBlock, boolean isStopped) {
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwPackageManagerServiceEx().isCustedCouldStoppedEx(pkg, isBlock, isStopped);
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

    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.HwConnectivityExService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v15, types: [com.android.server.swing.HwSwingPolicyService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v19, types: [android.os.IBinder, com.android.server.HwAftPolicyService] */
    /* JADX WARN: Type inference failed for: r4v7, types: [com.android.server.usb.HwUsbManagerExService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v23, types: [com.android.server.HwSaveDataService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v26, types: [com.android.server.display.DisplayEngineService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v30, types: [com.android.server.aps.HwApsManagerService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v33, types: [com.android.server.DefaultHwGeneralService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v35, types: [com.android.server.HwSmartDisplayService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v38, types: [com.android.server.DefaultHwGeneralService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 10 */
    public void setupHwServices(Context context) {
        Slog.i(TAG, "Hw Connectivity Service");
        ServiceManager.addService("hwConnectivityExService", (IBinder) new HwConnectivityExService(context));
        try {
            ?? hwGeneralService = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwGeneralService(context, UiThread.getHandler());
            Slog.i(TAG, "General Service");
            ServiceManager.addService("hwGeneralService", (IBinder) hwGeneralService);
        } catch (Throwable e) {
            Slog.e(TAG, "Failure starting generalService", e);
        }
        try {
            Slog.i(TAG, "smartdisplay Service");
            ?? hwSmartDisplayService = new HwSmartDisplayService(context);
            boolean isSupport_HIACE_ARSR1P = (hwSmartDisplayService.getDisplayEffectSupported(0) & 17) != 0;
            if ((AppActConstant.VALUE_TRUE.equals(SystemProperties.get("ro.config.eyesprotect_support", AppActConstant.VALUE_FALSE)) && hwSmartDisplayService.isFeatureSupported(1)) || hwSmartDisplayService.isFeatureSupported(2) || AppActConstant.VALUE_TRUE.equals(SystemProperties.get("ro.config.hw_displayeffect_en", AppActConstant.VALUE_FALSE)) || isSupport_HIACE_ARSR1P) {
                ServiceManager.addService("smartDisplay_service", (IBinder) hwSmartDisplayService);
            }
        } catch (Throwable th) {
            Slog.i(TAG, "smartdisplay_service failed");
        }
        if (HwGeneralManager.getInstance().isSupportForce()) {
            try {
                ?? hwGeneralService2 = HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwGeneralService(context, UiThread.getHandler());
                if (hwGeneralService2 != 0) {
                    Slog.i(TAG, "General Service");
                    ServiceManager.addService("hwGeneralService", (IBinder) hwGeneralService2);
                }
            } catch (Throwable e2) {
                Slog.e(TAG, "Failure starting generalService", e2);
            }
        }
        int apsSupportValue = SystemProperties.getInt("sys.aps.support", 0);
        if (apsSupportValue != 0) {
            try {
                ?? hwApsManagerService = new HwApsManagerService(context);
                ServiceManager.addService("aps_service", (IBinder) hwApsManagerService);
                hwApsManagerService.systemReady();
            } catch (Throwable e3) {
                Slog.e(TAG, "Failure starting apsService", e3);
            }
        } else {
            Slog.e(TAG, "Not support APS! sys.aps.support = " + apsSupportValue);
        }
        try {
            ServiceManager.addService("DisplayEngineExService", (IBinder) new DisplayEngineService(context));
            Slog.i(TAG, "[effect] DisplayEngineServiceEx success");
        } catch (Throwable e4) {
            Slog.e(TAG, "[effect] Failed to start DisplayEngineServiceEx, " + e4.getMessage());
        }
        if (SystemProperties.getBoolean("ro.config.savedata.disable", false)) {
            Slog.w(TAG, "SaveDataService is disabled.");
        } else {
            try {
                ServiceManager.addService(HwSaveDataService.SERVICE_NAME, (IBinder) HwSaveDataService.getInstance(context));
                Slog.i(TAG, "Start HwSaveDataService success");
            } catch (Throwable th2) {
                Slog.e(TAG, "Failure starting HwSaveDataService.");
            }
        }
        if (HwMapleHelper.isSupportMapleHelper()) {
            HwMapleHelper.startMapleHelper();
        }
        try {
            if (SystemProperties.getBoolean("persist.service.hdb.enable", false)) {
                Slog.i(TAG, "start HwUsbManagerExService");
                ServiceManager.addService("hwUsbExService", (IBinder) HwUsbManagerExService.getInstance(context));
            } else {
                Slog.i(TAG, "prop hdb is false, do not add hwUsbExService");
            }
        } catch (Throwable e5) {
            Slog.e(TAG, "Failure starting HwUsbManagerExService", e5);
        }
        boolean isTv = "tv".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT));
        boolean isWatch = "watch".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT));
        if (isTv || isWatch) {
            Slog.w(TAG, "watch or television is not start HwAftPolicyService");
        } else {
            try {
                ServiceManager.addService("hwaftpolicy", (IBinder) new HwAftPolicyService(context));
                Slog.i(TAG, "Start HwAftPolicyService success");
            } catch (Throwable e6) {
                Slog.e(TAG, "Failure starting HwAftPolicyService", e6);
            }
        }
        int swingEnabled = SystemProperties.getInt("ro.config.swing_enabled", 0);
        boolean isTvAivisionEnaled = SystemProperties.getBoolean("hw_mc.visionkit.tv_aivision_enable", false);
        if (swingEnabled == 1 || isTvAivisionEnaled) {
            try {
                ServiceManager.addService("hwswing", (IBinder) new HwSwingPolicyService(context));
                Slog.i(TAG, "Start HwSwingPolicyService success");
            } catch (Throwable e7) {
                Slog.e(TAG, "Failure starting HwSwingPolicyService", e7);
            }
        } else {
            Slog.w(TAG, "not start HwSwingPolicyService");
        }
    }

    public HwNativeDaemonConnector getHwNativeDaemonConnector() {
        return HwNativeDaemonConnectorImpl.getInstance();
    }

    public HwPackageServiceManager getHwPackageServiceManager() {
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwPackageServiceManager();
    }

    public IHwGnssLocationProvider createHwGnssLocationProvider(Context context, GnssLocationProvider gnssLocationProvider, Looper looper) {
        return HwGnssLocationProvider.getInstance(context, gnssLocationProvider, looper);
    }

    public SyncManager createHwSyncManager(Context context, boolean isFactoryTest) {
        return new HwSyncManager(context, isFactoryTest);
    }

    public IHwAppTransition getHwAppTransitionImpl() {
        return new HwAppTransitionImpl();
    }

    public HwServiceFactory.IHwDrmDialogService getHwDrmDialogService() {
        return new HwDrmDialogServiceImpl();
    }

    public static class HwDrmDialogServiceImpl implements HwServiceFactory.IHwDrmDialogService {
        public void startDrmDialogService(Context context) {
            if (HwServiceFactoryImpl.sDrmDialogService == null) {
                HWDrmDialogsService unused = HwServiceFactoryImpl.sDrmDialogService = new HWDrmDialogsService(context);
                HwServiceFactoryImpl.sDrmDialogService.start();
            }
        }
    }

    public HwServiceFactory.IMultiTaskManagerServiceFactory getMultiTaskManagerService() {
        return new MultiTaskManagerServiceImpl();
    }

    public static class MultiTaskManagerServiceImpl implements HwServiceFactory.IMultiTaskManagerServiceFactory {
        /* JADX WARN: Type inference failed for: r0v2, types: [com.android.server.mtm.DefaultMultiTaskManagerService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        public IBinder getInstance(Context context) {
            return HwPartIawareServiceFactoryEx.loadFactory(HwPartIawareServiceFactoryEx.IAWARE_SERVICE_FACTORY_EX_IMPL_NAME).getMultiTaskManagerService(context);
        }
    }

    public boolean removeProtectAppInPrivacyMode(AuthenticatorDescription desc, boolean isRemoved, Context context) {
        return HwAccountHelper.removeProtectAppInPrivacyMode(desc, isRemoved, context);
    }

    public static class HwInputManagerServiceImpl implements IHwInputManagerService {
        public InputManagerService getInstance(Context context, Handler handler) {
            Slog.i(HwServiceFactoryImpl.TAG, "Input Manager ,getInstance");
            return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwInputManagerService(context, handler).getInputManagerService();
        }
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

    public IHwTetheringEx getHwTetheringEx() {
        return HwTetheringEx.getDefault();
    }

    public ActivityRecord createActivityRecord(ActivityTaskManagerService service, WindowProcessController caller, int launchedFromPid, int launchedFromUid, String launchedFromPackage, Intent intent, String resolvedType, ActivityInfo activityInfo, Configuration configuration, ActivityRecord resultTo, String resultWho, int reqCode, boolean isComponentSpecified, boolean isRootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityOptions options, ActivityRecord sourceRecord) {
        ActivityTaskManagerServiceEx atmsEx = new ActivityTaskManagerServiceEx();
        atmsEx.setActivityTaskManagerService(service);
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).createActivityRecord(atmsEx, new WindowProcessControllerEx(caller), launchedFromPid, launchedFromUid, launchedFromPackage, intent, resolvedType, activityInfo, configuration, new ActivityRecordEx(resultTo), resultWho, reqCode, isComponentSpecified, isRootVoiceInteraction, new ActivityStackSupervisorEx(supervisor), options, new ActivityRecordEx(sourceRecord)).getActivityRecordBridge();
    }

    public ActivityStartInterceptor createActivityStartInterceptor(ActivityTaskManagerService service, ActivityStackSupervisor supervisor) {
        ActivityTaskManagerServiceEx atmsEx = new ActivityTaskManagerServiceEx();
        atmsEx.setActivityTaskManagerService(service);
        ActivityStackSupervisorEx activityStackSupervisorEx = new ActivityStackSupervisorEx();
        activityStackSupervisorEx.setActivityStackSupervisor(supervisor);
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).createActivityStartInterceptor(atmsEx, activityStackSupervisorEx).getHwActivityStartInterceptor();
    }

    public AppTransition createHwAppTransition(Context context, WindowManagerService wms, DisplayContent displayContent) {
        return new HwAppTransition(context, wms, displayContent);
    }

    public TaskStack createTaskStack(WindowManagerService service, int stackId, ActivityStack activityStack) {
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).createTaskStack(new WindowManagerServiceEx(service), stackId, new ActivityStackEx(activityStack)).getTaskStackBridge();
    }

    public DisplayContent createDisplayContent(Display display, WindowManagerService service, ActivityDisplay activityDisplay) {
        WindowManagerServiceEx serviceEx = null;
        ActivityDisplayEx activityDisplayEx = null;
        if (service != null) {
            serviceEx = new WindowManagerServiceEx(service);
        }
        if (activityDisplay != null) {
            activityDisplayEx = new ActivityDisplayEx(activityDisplay);
        }
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).createDisplayContent(display, serviceEx, activityDisplayEx).getHwDisplayContent();
    }

    public TaskRecord createTaskRecord(ActivityTaskManagerService service, int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx = new ActivityTaskManagerServiceEx();
        activityTaskManagerServiceEx.setActivityTaskManagerService(service);
        IVoiceInteractionSessionEx iVoiceInteractionSessionEx = new IVoiceInteractionSessionEx();
        iVoiceInteractionSessionEx.setIVoiceInteractionSession(voiceSession);
        IVoiceInteractorEx iVoiceInteractorEx = new IVoiceInteractorEx();
        iVoiceInteractorEx.setVoiceInteractor(voiceInteractor);
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).createTaskRecord(activityTaskManagerServiceEx, taskId, info, intent, iVoiceInteractionSessionEx, iVoiceInteractorEx).getTaskRecordBridge();
    }

    public TaskRecord createTaskRecord(ActivityTaskManagerService service, int taskId, ActivityInfo info, Intent intent, ActivityManager.TaskDescription taskDescription) {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx = new ActivityTaskManagerServiceEx();
        activityTaskManagerServiceEx.setActivityTaskManagerService(service);
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).createTaskRecord(activityTaskManagerServiceEx, taskId, info, intent, taskDescription).getTaskRecordBridge();
    }

    public TaskRecord createTaskRecord(ActivityTaskManagerService service, int taskId, Intent intent, Intent affinityIntent, String affinity, String rootAffinity, ComponentName realActivity, ComponentName origActivity, boolean isRootWasReset, boolean isAutoRemoveRecents, boolean isAskedCompatMode, int userId, int effectiveUid, String lastDescription, ArrayList<ActivityRecord> activities, long lastTimeMoved, boolean isNeverRelinquishIdentity, ActivityManager.TaskDescription lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean isSupportsPictureInPicture, boolean isRealActivitySuspended, boolean isUserSetupComplete, int minWidth, int minHeight) {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx = new ActivityTaskManagerServiceEx();
        activityTaskManagerServiceEx.setActivityTaskManagerService(service);
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).createTaskRecord(activityTaskManagerServiceEx, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, isRootWasReset, isAutoRemoveRecents, isAskedCompatMode, userId, effectiveUid, lastDescription, ActivityRecordEx.getActivityRecordExs(activities), lastTimeMoved, isNeverRelinquishIdentity, lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, isSupportsPictureInPicture, isRealActivitySuspended, isUserSetupComplete, minWidth, minHeight).getTaskRecordBridge();
    }

    public ActivityStack createActivityStack(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, int windowingMode, int activityType, boolean isOnTop) {
        ActivityDisplayEx activityDisplayEx = new ActivityDisplayEx();
        activityDisplayEx.setActivityDisplay(display);
        ActivityStackSupervisorEx activityStackSupervisorEx = new ActivityStackSupervisorEx();
        activityStackSupervisorEx.setActivityStackSupervisor(supervisor);
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).createActivityStack(activityDisplayEx, stackId, activityStackSupervisorEx, windowingMode, activityType, isOnTop).getHwActivityStack();
    }

    public HwServiceFactory.IHwSmartBackLightController getHwSmartBackLightController() {
        return new HwSmartBackLightControllerImpl();
    }

    public static class HwSmartBackLightControllerImpl implements HwServiceFactory.IHwSmartBackLightController {
        public boolean checkIfUsingHwSBL() {
            return HwSmartBackLightNormalizedController.checkIfUsingHwSBL();
        }

        public void StartHwSmartBackLightController(Context context, LightsManager lightsManager, SensorManager sensorManager) {
            if (HwServiceFactoryImpl.sHwSmartBackLightController == null) {
                HwSmartBackLightNormalizedController unused = HwServiceFactoryImpl.sHwSmartBackLightController = new HwSmartBackLightNormalizedController(context, lightsManager, sensorManager);
            }
        }

        public void updatePowerState(int state, boolean isUseSmartBacklight) {
            if (HwServiceFactoryImpl.sHwSmartBackLightController != null) {
                HwServiceFactoryImpl.sHwSmartBackLightController.updatePowerState(state, isUseSmartBacklight);
            }
        }

        public void updateBrightnessState(int state) {
            if (HwServiceFactoryImpl.sHwSmartBackLightController != null) {
                HwServiceFactoryImpl.sHwSmartBackLightController.updateBrightnessState(state);
            }
        }
    }

    public HwServiceFactory.IDisplayEngineInterface getDisplayEngineInterface() {
        return new DisplayEngineInterfaceImpl();
    }

    public static class DisplayEngineInterfaceImpl implements HwServiceFactory.IDisplayEngineInterface {
        public void initialize() {
            if (HwServiceFactoryImpl.sDisplayEngineInterface == null) {
                DisplayEngineInterface unused = HwServiceFactoryImpl.sDisplayEngineInterface = new DisplayEngineInterface();
            }
        }

        public boolean getSupported(String feature) {
            if (HwServiceFactoryImpl.sDisplayEngineInterface == null) {
                return false;
            }
            int ret = HwServiceFactoryImpl.sDisplayEngineInterface.getSupported(feature);
            DisplayEngineInterface unused = HwServiceFactoryImpl.sDisplayEngineInterface;
            if (ret != 0) {
                return true;
            }
            return false;
        }

        public void setScene(String scene, String action) {
            if (HwServiceFactoryImpl.sDisplayEngineInterface != null) {
                HwServiceFactoryImpl.sDisplayEngineInterface.setScene(scene, action);
            }
        }

        public void updateLightSensorState(boolean isSensorEnable) {
            if (HwServiceFactoryImpl.sDisplayEngineInterface != null) {
                HwServiceFactoryImpl.sDisplayEngineInterface.updateLightSensorState(isSensorEnable);
            }
        }
    }

    public HwServiceFactory.IHwAutomaticBrightnessController getHuaweiAutomaticBrightnessController() {
        return new HwAutomaticBrightnessControllerImpl();
    }

    public static class HwAutomaticBrightnessControllerImpl implements HwServiceFactory.IHwAutomaticBrightnessController {
        public AutomaticBrightnessController getInstance(AutomaticBrightnessController.Callbacks callbacks, Looper looper, SensorManager sensorManager, Sensor lightSensor, BrightnessMappingStrategy mapper, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, int initialLightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean isResetAmbientLuxAfterWarmUpConfig, HysteresisLevels ambientBrightnessThresholds, HysteresisLevels screenBrightnessThresholds, long shortTermModelTimeout, PackageManager packageManager, Context context) {
            return new HwNormalizedAutomaticBrightnessController(callbacks, looper, sensorManager, lightSensor, mapper, lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, lightSensorRate, initialLightSensorRate, brighteningLightDebounceConfig, darkeningLightDebounceConfig, isResetAmbientLuxAfterWarmUpConfig, ambientBrightnessThresholds, screenBrightnessThresholds, shortTermModelTimeout, packageManager, context);
        }
    }

    public HwServiceFactory.IDisplayEffectMonitor getDisplayEffectMonitor(Context context) {
        return DisplayEffectMonitor.getInstance(context);
    }

    public HwServiceFactory.IHwNormalizedManualBrightnessController getHuaweiManualBrightnessController() {
        return new HwNormalizedManualBrightnessControllerImpl();
    }

    public static class HwNormalizedManualBrightnessControllerImpl implements HwServiceFactory.IHwNormalizedManualBrightnessController {
        public ManualBrightnessController getInstance(ManualBrightnessController.ManualBrightnessCallbacks callbacks, Context context, SensorManager sensorManager) {
            return new HwNormalizedManualBrightnessController(callbacks, context, sensorManager);
        }
    }

    public HwServiceFactory.IHwBluetoothBigDataService getHwBluetoothBigDataService() {
        return new HwBluetoothBigDataServiceImpl();
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

    public HwServiceFactory.IHwIMonitorManager getHwIMonitorManager() {
        return new HwIMonitorManagerImpl();
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

    public IZRHungService getZRHungService() {
        return HwDFRServicesFactory.getZrHungServicesFactory().getDefaultZRHungService((Context) null);
    }

    public HwServiceFactory.IHwRampAnimator getHwNormalizedRampAnimator() {
        return new HwNormalizedRampAnimatorImpl();
    }

    public static class HwNormalizedRampAnimatorImpl implements HwServiceFactory.IHwRampAnimator {
        public RampAnimator<DisplayPowerState> getInstance(DisplayPowerState object, IntProperty<DisplayPowerState> property, Context context) {
            return new HwNormalizedRampAnimator(object, property, context);
        }
    }

    public HwServiceFactory.IHwLocationManagerService getHwLocationManagerService() {
        return new HwLocationManagerServiceImpl();
    }

    public static class HwLocationManagerServiceImpl implements HwServiceFactory.IHwLocationManagerService {
        public LocationManagerService getInstance(Context context) {
            return new HwLocationManagerService(context);
        }
    }

    public IHwLocalLocationProvider getHwLocalLocationProvider(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager) {
        return HwLocalLocationProvider.getInstance(context, locationProviderManager);
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
        return HwGpsXtraDownloadReceiver.getInstance();
    }

    public IHwGpsLocationManager getHwGpsLocationManager(Context context) {
        return HwGpsLocationManager.getInstance(context);
    }

    public HwServiceFactory.IHwFingerprintService getHwFingerprintService() {
        return new HwFingerprintServiceImpl();
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

    public AbsHwMtmBroadcastResourceManager getMtmBRManagerImpl(HwBroadcastQueue queue) {
        return HwPartIawareServiceFactory.loadFactory("com.huawei.server.HwPartIawareServiceFactoryImpl").getHwMtmBroadcastResourceManagerImpl(BroadcastQueueExUtils.createBroadcastQueueEx(queue));
    }

    public HwServiceFactory.ISystemBlockMonitor getISystemBlockMonitor() {
        return SystemBlockMonitor.getInstance();
    }

    public IHwBinderMonitor getIHwBinderMonitor() {
        return new HwBinderMonitor();
    }

    public IHwIpcMonitor getIHwIpcMonitor(Object object, String type, String name) {
        return HwIpcMonitorImpl.getHwIpcMonitor(object, type, name);
    }

    public IHwGpsActionReporter getHwGpsActionReporter(Context context, ILocationManager iLocationManager) {
        return HwGpsActionReporter.getInstance(context, iLocationManager);
    }

    public LocationProviderProxy locationProviderProxyCreateAndBind(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId) {
        return HwLocationProviderProxy.createAndBind(context, locationProviderManager, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId);
    }

    public GeocoderProxy geocoderProxyCreateAndBind(Context context, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId) {
        return HwGeocoderProxy.createAndBind(context, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId);
    }

    public void addHwFmService(Context context) {
        ServiceManager.addService("hwfm_service", new HwFmService(context));
    }

    public ISDCardCryptedHelper getSDCardCryptedHelper() {
        return new SDCardCryptedHelper();
    }

    public void reportProximitySensorEventToIAware(boolean isPositive) {
        HwSysResManager.getInstance().reportProximitySensorEventToIAware(isPositive);
    }

    /* JADX WARN: Type inference failed for: r1v6, types: [com.huawei.server.pc.DefaultHwPCManagerService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void addHwPCManagerService(Context context, ActivityManagerService ams) {
        Slog.i(TAG, "PCManager, addService");
        try {
            if (Class.forName("android.pc.HwPCManagerImpl") == null) {
                Slog.i(TAG, "HwPCManagerImpl null! skip add pc service");
                return;
            }
            ActivityManagerServiceEx amsEx = new ActivityManagerServiceEx();
            amsEx.setActivityManagerService(ams);
            ServiceManager.addService("hwPcManager", (IBinder) HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwPCManagerService(context, amsEx));
        } catch (ClassNotFoundException e) {
            Slog.i(TAG, "Class Not Found Exception HwPCManagerImpl");
        }
    }

    /* JADX WARN: Type inference failed for: r2v2, types: [com.huawei.server.magicwin.DefaultHwMagicWindowManagerService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void addHwMagicWindowService(Context context, ActivityManagerService ams, WindowManagerService wms) {
        Slog.i(TAG, "HwMagicWindow, addService");
        ActivityManagerServiceEx amsEx = new ActivityManagerServiceEx();
        amsEx.setActivityManagerService(ams);
        WindowManagerServiceEx wmsEx = new WindowManagerServiceEx();
        wmsEx.setWindowManagerService(wms);
        ServiceManager.addService("HwMagicWindowService", (IBinder) HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWindowService(context, amsEx, wmsEx));
    }

    public void reportVibratorToIAware(int uid) {
        HwSysResManager.getInstance().reportVibratorToIAware(uid);
    }

    public HwServiceFactory.IHwForceRotationManagerServiceWrapper getForceRotationManagerServiceWrapper() {
        return new HwForceRotationManagerServiceWrapper();
    }

    public static class HwForceRotationManagerServiceWrapper implements HwServiceFactory.IHwForceRotationManagerServiceWrapper {
        public HwForceRotationManagerService getServiceInstance(Context context, Handler uiHandler) {
            return new HwForceRotationManagerService(context, uiHandler);
        }
    }

    public void updateLocalesWhenOTA(Context context) {
        UpdateConfig.updateLocalesWhenOTA(context);
    }

    public void updateLocalesWhenOTAEX(Context context, int preSdkVersion) {
        UpdateConfig.updateLocalesWhenOTAEX(context, preSdkVersion);
    }

    public IHwCameraServiceProxy getHwCameraServiceProxy(Context context) {
        if (context != null) {
            return HwCameraServiceFactory.getHwCameraServiceFactory().getHwCameraServiceProxy(context);
        }
        Slog.e(TAG, "context is null");
        return null;
    }

    public MediaFocusControl getHwMediaFocusControl(Context cntxt, PlayerFocusEnforcer pfe) {
        return new HwMediaFocusControl(cntxt, pfe);
    }

    public FocusRequester getHwFocusRequester(AudioAttributes audioAttributes, int focusRequest, int grantFlags, IAudioFocusDispatcher afl, IBinder source, String id, MediaFocusControl.AudioFocusDeathHandler hdlr, String pn, int uid, MediaFocusControl ctlr, int sdk, boolean isInExternal) {
        return new HwFocusRequester(audioAttributes, focusRequest, grantFlags, afl, source, id, hdlr, pn, uid, ctlr, sdk, isInExternal);
    }

    public FocusRequester getHwFocusRequester(AudioFocusInfo afi, IAudioFocusDispatcher afl, IBinder source, MediaFocusControl.AudioFocusDeathHandler hdlr, MediaFocusControl ctlr, boolean isInExternal) {
        return new HwFocusRequester(afi, afl, source, hdlr, ctlr, isInExternal);
    }

    public void reportMediaKeyToIAware(int uid) {
        HwSysResManager.getInstance().reportMediaKeyToIAware(uid);
    }

    public ISecurityProfileController getSecurityProfileController() {
        return HwServiceSecurityPartsFactory.getInstance().getSecurityProfileController();
    }

    public ITrustSpaceController getTrustSpaceController() {
        return HwServiceSecurityPartsFactory.getInstance().getTrustSpaceController();
    }

    public IHwScreenRotationAnimation getHwScreenRotationAnimation() {
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwScreenRotationAnimation().getScreenRotationAnimationBridge();
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
        HwAodManager.getInstance().setPowerState(powerState);
    }

    public void reportGoogleConn(boolean isConn) {
        HwSysResManager.getInstance().reportGoogleConn(isConn);
    }

    public HwLbsLogger getHwLbsLogger(Context context) {
        return HwLbsLogger.getInstance(context);
    }

    public HwUibcReceiver getHwUibcReceiver() {
        return HwUibcReceiver.getInstance();
    }

    public void clearHwUibcReceiver() {
        HwUibcReceiver.clearInstance();
    }

    public IHwAppWindowTokenEx getHwAppWindowTokenEx() {
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwAppWindowTokenEx().getHwAppWindowTokenBridge();
    }

    public IHwWifiDisplayAdapterEx getHwWifiDisplayAdapterEx(IWifiDisplayAdapterInner wfda, Context context) {
        return new HwWifiDisplayAdapterEx(wfda, context);
    }

    public IHwWifiDisplayControllerEx getHwWifiDisplayControllerEx(IWifiDisplayControllerInner wfdc, Context context, Handler handler) {
        return new HwWifiDisplayControllerEx(wfdc, context, handler);
    }

    public IHwWifiProCommonUtilsEx getWifiProCommonUtilsEx() {
        return WifiProCommonUtils.getDefault();
    }

    public FoldPolicy getHwFoldPolicy(Context context) {
        if (HwFoldScreenState.isInwardFoldDevice()) {
            return HwPartFsmFactory.loadFactory().getHwInwardFoldPolicy(context);
        }
        if (HwFoldScreenState.isOutFoldDevice()) {
            return HwPartFsmFactory.loadFactory().getHwOutwardFoldPolicy(context);
        }
        return null;
    }

    public AdbService getHwAdbService(Context context) {
        return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwAdbService(context).getAdbService();
    }

    public IGpsFreezeProc getGpsFreezeProc() {
        return GpsFreezeProc.getInstance();
    }

    public HwRogEx getHwRogEx() {
        return HwRogEx.getDefault();
    }

    public IHwPersistentDataStoreEx getHwPersistentDataStoreEx(IPersistentDataStoreInner pds) {
        return new HwPersistentDataStoreEx(pds);
    }

    public IHwQuickTTFFMonitor getHwQuickTTFFMonitor(Context context, GnssLocationProvider gnssLocationProvider) {
        if (gnssLocationProvider != null) {
            return HwQuickTTFFMonitor.getInstance(context, gnssLocationProvider);
        }
        return new HwNullQuickTTFFMonitor();
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.jankshield.JankShieldService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void addJankShieldService(Context context) {
        ServiceManager.addService("jankshield", (IBinder) new JankShieldService(context));
    }
}
