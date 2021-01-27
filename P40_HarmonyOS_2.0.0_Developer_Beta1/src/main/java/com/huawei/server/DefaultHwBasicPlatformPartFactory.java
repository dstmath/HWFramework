package com.huawei.server;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.service.voice.IVoiceInteractionSessionEx;
import android.view.Display;
import com.android.server.AlarmManagerServiceExt;
import com.android.server.BatteryServiceEx;
import com.android.server.DefaultHwGeneralService;
import com.android.server.accessibility.MagnificationControllerEx;
import com.android.server.accessibility.MagnificationGestureHandlerEx;
import com.android.server.adb.AdbServiceEx;
import com.android.server.display.HwNormalizedAutomaticBrightnessController;
import com.android.server.gesture.DefaultDeviceStateController;
import com.android.server.gesture.DefaultGestureNavConst;
import com.android.server.gesture.DefaultGestureNavManager;
import com.android.server.gesture.DefaultGestureUtils;
import com.android.server.gesture.DefaultHwGestureNavWhiteConfig;
import com.android.server.input.DefaultHwInputManagerServiceEx;
import com.android.server.input.IHwInputManagerInner;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.lights.LightsServiceEx;
import com.android.server.notch.DefaultHwNotchScreenWhiteConfig;
import com.android.server.pm.DefaultHwPackageManagerServiceExt;
import com.android.server.pm.DefaultHwPackageManagerUtils;
import com.android.server.pm.DefaultHwPackageServiceManager;
import com.android.server.pm.DefaultHwPluginPackageEx;
import com.android.server.pm.DefaultMultiWinWhiteListManager;
import com.android.server.pm.IHwPackageManagerInnerEx;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.pm.UserDataPreparerEx;
import com.android.server.pm.UserManagerServiceEx;
import com.android.server.pm.permission.DefaultPermissionGrantPolicyEx;
import com.android.server.pm.permission.PermissionManagerServiceEx;
import com.android.server.policy.DefaultEasyWakeUpManager;
import com.android.server.policy.DefaultFingerprintActionsListener;
import com.android.server.policy.DefaultHwFalseTouchMonitor;
import com.android.server.policy.DefaultHwScreenOnProximityLock;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.PhoneWindowManagerEx;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.power.DefaultHwPowerManagerServiceEx;
import com.android.server.power.IHwPowerManagerInnerEx;
import com.android.server.usb.UsbAlsaManagerEx;
import com.android.server.usb.UsbDeviceManagerEx;
import com.android.server.usb.UsbSettingsManagerEx;
import com.android.server.wm.ActivityDisplayEx;
import com.android.server.wm.ActivityRecordBridgeEx;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.ActivityStackBridgeEx;
import com.android.server.wm.ActivityStackEx;
import com.android.server.wm.ActivityStackSupervisorBridgeEx;
import com.android.server.wm.ActivityStackSupervisorEx;
import com.android.server.wm.ActivityStartInterceptorBridgeEx;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.DefaultDisplayRotationExImpl;
import com.android.server.wm.DisplayContentBridgeEx;
import com.android.server.wm.DisplayContentEx;
import com.android.server.wm.HwAppWindowTokenBridgeEx;
import com.android.server.wm.HwScreenRotationAnimationImplBridgeEx;
import com.android.server.wm.HwTaskLaunchParamsModifierBridgeEx;
import com.android.server.wm.HwTaskPositionerBridgeEx;
import com.android.server.wm.HwTaskRecordExBridgeEx;
import com.android.server.wm.HwTaskStackExBridgeEx;
import com.android.server.wm.IHwRootActivityContainerInner;
import com.android.server.wm.RootActivityContainerBridgeEx;
import com.android.server.wm.TaskRecordBridgeEx;
import com.android.server.wm.TaskStackBridgeEx;
import com.android.server.wm.TaskStackEx;
import com.android.server.wm.WindowManagerServiceEx;
import com.android.server.wm.WindowProcessControllerEx;
import com.android.server.wm.WindowStateAnimatorBridgeEx;
import com.android.server.wm.WindowStateBridgeEx;
import com.android.server.wm.WindowStateEx;
import com.huawei.android.internal.app.IVoiceInteractorEx;
import com.huawei.server.display.DefaultHwEyeProtectionController;
import com.huawei.server.policy.DefaultPickUpWakeScreenManager;
import com.huawei.server.policy.DefaultStylusGestureListener;
import com.huawei.server.policy.keyguard.KeyguardServiceDelegateEx;
import com.huawei.server.sidetouch.DefaultHwDisplaySideRegionConfig;
import com.huawei.server.sidetouch.DefaultHwSideTouchPolicy;
import com.huawei.server.wm.IHwDisplayRotationEx;
import java.util.ArrayList;

public class DefaultHwBasicPlatformPartFactory {
    private static DefaultHwBasicPlatformPartFactory sInstance = null;

    public static synchronized DefaultHwBasicPlatformPartFactory getInstance() {
        DefaultHwBasicPlatformPartFactory defaultHwBasicPlatformPartFactory;
        synchronized (DefaultHwBasicPlatformPartFactory.class) {
            if (sInstance == null) {
                sInstance = new DefaultHwBasicPlatformPartFactory();
            }
            defaultHwBasicPlatformPartFactory = sInstance;
        }
        return defaultHwBasicPlatformPartFactory;
    }

    public DefaultHwEyeProtectionController getHwEyeProtectionController(Context context, HwNormalizedAutomaticBrightnessController automaticBrightnessController) {
        return new DefaultHwEyeProtectionController(context, automaticBrightnessController);
    }

    public DefaultPickUpWakeScreenManager getPickUpWakeScreenManager() {
        return DefaultPickUpWakeScreenManager.getInstance();
    }

    public DefaultStylusGestureListener getStylusGestureListener(Context context) {
        return DefaultStylusGestureListener.getInstance(context);
    }

    public AdbServiceEx getHwAdbService(Context context) {
        return new AdbServiceEx(context);
    }

    public InputManagerServiceEx getHwInputManagerService(Context context, Handler handler) {
        return new InputManagerServiceEx(context);
    }

    public LightsServiceEx getHwLightsService(Context context) {
        return new LightsServiceEx(context);
    }

    public AlarmManagerServiceExt getHwAlarmManagerService(Context context) {
        return new AlarmManagerServiceExt(context);
    }

    public BatteryServiceEx getHwBatteryService(Context context) {
        return new BatteryServiceEx(context);
    }

    public DefaultHwGeneralService getHwGeneralService(Context context, Handler handler) {
        return new DefaultHwGeneralService(context, handler);
    }

    public MagnificationGestureHandlerEx getHwMagnificationGestureHandler(Context context, MagnificationControllerEx magnificationControllerEx, boolean isDetectControlGestures, boolean isTriggerable, int displayId) {
        return new MagnificationGestureHandlerEx(context, magnificationControllerEx, isDetectControlGestures, isTriggerable, displayId);
    }

    public DefaultDeviceStateController getDeviceStateController(Context context) {
        return DefaultDeviceStateController.getInstance(context);
    }

    public DefaultGestureNavConst getGestureNavConst() {
        return new DefaultGestureNavConst();
    }

    public DefaultGestureNavManager getGestureNavManager(Context context) {
        return new DefaultGestureNavManager(context);
    }

    public DefaultGestureUtils getGestureUtils() {
        return new DefaultGestureUtils();
    }

    public DefaultHwGestureNavWhiteConfig getHwGestureNavWhiteConfig() {
        return DefaultHwGestureNavWhiteConfig.getInstance();
    }

    public DefaultHwInputManagerServiceEx getHwInputManagerServiceEx(IHwInputManagerInner ims, Context context) {
        return new DefaultHwInputManagerServiceEx(ims, context);
    }

    public DefaultEasyWakeUpManager getEasyWakeUpManager(Context context, Handler handler, KeyguardServiceDelegateEx keyguardDelegate) {
        return DefaultEasyWakeUpManager.getInstance(context, handler, keyguardDelegate);
    }

    public DefaultFingerprintActionsListener getFingerprintActionsListener(Context context, PhoneWindowManagerEx policy) {
        return new DefaultFingerprintActionsListener(context, policy);
    }

    public DefaultHwFalseTouchMonitor getHwFalseTouchMonitor() {
        return DefaultHwFalseTouchMonitor.getInstance();
    }

    public DefaultHwScreenOnProximityLock getHwScreenOnProximityLock(Context context, HwPhoneWindowManager phoneWindowManager, WindowManagerPolicyEx.WindowManagerFuncsEx windowFuncs, Handler handler) {
        return new DefaultHwScreenOnProximityLock(context, phoneWindowManager, windowFuncs, handler);
    }

    public DefaultHwPowerManagerServiceEx getHwPowerManagerServiceEx(IHwPowerManagerInnerEx pms, Context context) {
        return new DefaultHwPowerManagerServiceEx(pms, context);
    }

    public UsbDeviceManagerEx getHwUsbDeviceManager(Context context, UsbAlsaManagerEx alsaManager, UsbSettingsManagerEx settingsManager) {
        return new UsbDeviceManagerEx(context, alsaManager, settingsManager);
    }

    public DefaultHwNotchScreenWhiteConfig getHwNotchScreenWhiteConfig() {
        return DefaultHwNotchScreenWhiteConfig.getInstance();
    }

    public HwTaskLaunchParamsModifierBridgeEx getHwTaskLaunchParamsModifierEx() {
        return new HwTaskLaunchParamsModifierBridgeEx();
    }

    public HwTaskPositionerBridgeEx getHwTaskPositionerEx(WindowManagerServiceEx wmsEx) {
        return new HwTaskPositionerBridgeEx(wmsEx);
    }

    public HwTaskRecordExBridgeEx getHwTaskRecordEx() {
        return new HwTaskRecordExBridgeEx();
    }

    public HwTaskStackExBridgeEx getHwTaskStackEx(TaskStackEx taskStackEx, WindowManagerServiceEx windowManagerServiceEx) {
        return new HwTaskStackExBridgeEx(taskStackEx, windowManagerServiceEx);
    }

    public ActivityStackBridgeEx createActivityStack(ActivityDisplayEx display, int stackId, ActivityStackSupervisorEx supervisor, int windowingMode, int activityType, boolean isOnTop) {
        return new ActivityStackBridgeEx(display, stackId, supervisor, windowingMode, activityType, isOnTop);
    }

    public ActivityStartInterceptorBridgeEx createActivityStartInterceptor(ActivityTaskManagerServiceEx service, ActivityStackSupervisorEx supervisor) {
        return new ActivityStartInterceptorBridgeEx(service, supervisor);
    }

    public TaskRecordBridgeEx createTaskRecord(ActivityTaskManagerServiceEx service, int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSessionEx voiceSession, IVoiceInteractorEx voiceInteractor) {
        return new TaskRecordBridgeEx(service, taskId, info, intent, voiceSession, voiceInteractor);
    }

    public TaskRecordBridgeEx createTaskRecord(ActivityTaskManagerServiceEx service, int taskId, ActivityInfo info, Intent intent, ActivityManager.TaskDescription taskDescription) {
        return new TaskRecordBridgeEx(service, taskId, info, intent, taskDescription);
    }

    public TaskRecordBridgeEx createTaskRecord(ActivityTaskManagerServiceEx service, int _taskId, Intent _intent, Intent _affinityIntent, String _affinity, String _rootAffinity, ComponentName _realActivity, ComponentName _origActivity, boolean _rootWasReset, boolean _autoRemoveRecents, boolean _askedCompatMode, int _userId, int _effectiveUid, String _lastDescription, ArrayList<ActivityRecordEx> activities, long lastTimeMoved, boolean neverRelinquishIdentity, ActivityManager.TaskDescription _lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean _realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        return new TaskRecordBridgeEx(service, _taskId, _intent, _affinityIntent, _affinity, _rootAffinity, _realActivity, _origActivity, _rootWasReset, _autoRemoveRecents, _askedCompatMode, _userId, _effectiveUid, _lastDescription, activities, lastTimeMoved, neverRelinquishIdentity, _lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, _realActivitySuspended, userSetupComplete, minWidth, minHeight);
    }

    public ActivityStackSupervisorBridgeEx getHwActivityStackSupervisor(ActivityTaskManagerServiceEx service, Looper looper) {
        return new ActivityStackSupervisorBridgeEx(service, looper);
    }

    public ActivityRecordBridgeEx createActivityRecord(ActivityTaskManagerServiceEx serviceEx, WindowProcessControllerEx callerEx, int launchedFromPid, int launchedFromUid, String launchedFromPackage, Intent intent, String resolvedType, ActivityInfo activityInfo, Configuration configuration, ActivityRecordEx resultToEx, String resultWho, int reqCode, boolean isComponentSpecified, boolean isRootVoiceInteraction, ActivityStackSupervisorEx supervisorEx, ActivityOptions options, ActivityRecordEx sourceRecordEx) {
        return new ActivityRecordBridgeEx(serviceEx, callerEx, launchedFromPid, launchedFromUid, launchedFromPackage, intent, resolvedType, activityInfo, configuration, resultToEx, resultWho, reqCode, isComponentSpecified, isRootVoiceInteraction, supervisorEx, options, sourceRecordEx);
    }

    public HwAppWindowTokenBridgeEx getHwAppWindowTokenEx() {
        return new HwAppWindowTokenBridgeEx();
    }

    public DisplayContentBridgeEx createDisplayContent(Display display, WindowManagerServiceEx service, ActivityDisplayEx activityDisplay) {
        return new DisplayContentBridgeEx(display, service, activityDisplay);
    }

    public IHwDisplayRotationEx getHwDisplayRotationEx(WindowManagerServiceEx serviceEx, DisplayContentEx displayContentEx, boolean isDefaultDisplay) {
        return new DefaultDisplayRotationExImpl(serviceEx, displayContentEx, isDefaultDisplay);
    }

    public RootActivityContainerBridgeEx getHwRootActivityContainerEx(IHwRootActivityContainerInner rac, ActivityTaskManagerServiceEx serviceEx) {
        return new RootActivityContainerBridgeEx(rac, serviceEx);
    }

    public HwScreenRotationAnimationImplBridgeEx getHwScreenRotationAnimation() {
        return new HwScreenRotationAnimationImplBridgeEx();
    }

    public TaskStackBridgeEx createTaskStack(WindowManagerServiceEx serviceEx, int stackId, ActivityStackEx activityStackEx) {
        return new TaskStackBridgeEx(serviceEx, stackId, activityStackEx);
    }

    public WindowStateBridgeEx getHwWindowStateEx(WindowManagerServiceEx wmsEx, WindowStateEx windowStateEx) {
        return new WindowStateBridgeEx(wmsEx, windowStateEx);
    }

    public WindowStateAnimatorBridgeEx createWindowStateAnimator(WindowStateEx winEx) {
        return new WindowStateAnimatorBridgeEx(winEx);
    }

    public DefaultHwSideTouchPolicy getHwSideTouchPolicyInstance(Context context) {
        return DefaultHwSideTouchPolicy.getInstance();
    }

    public DefaultHwDisplaySideRegionConfig getHwDisplaySideRegionConfigInstance() {
        return DefaultHwDisplaySideRegionConfig.getInstance();
    }

    public DefaultPermissionGrantPolicyEx getDefaultPermissionGrantPolicy(Context context, Looper looper, PermissionManagerServiceEx permissionManager) {
        return new DefaultPermissionGrantPolicyEx(context, looper, permissionManager);
    }

    public DefaultHwPackageManagerServiceExt getHwPackageManagerServiceEx(PackageManagerServiceEx pms, Context context) {
        return new DefaultHwPackageManagerServiceExt();
    }

    public DefaultHwPluginPackageEx getHwPluginPackage(IHwPackageManagerInnerEx pm, String packageName) {
        return new DefaultHwPluginPackageEx();
    }

    public DefaultHwPackageServiceManager getHwPackageServiceManager() {
        return new DefaultHwPackageServiceManager();
    }

    public UserManagerServiceEx getUserManagerService(Context context, PackageManagerServiceEx pm, UserDataPreparerEx userDataPreparer, Object packagesLock) {
        return new UserManagerServiceEx();
    }

    public DefaultHwPackageManagerServiceExt getHwPackageManagerServiceEx() {
        return new DefaultHwPackageManagerServiceExt();
    }

    public DefaultHwPackageManagerUtils getHwPackageManagerUtils() {
        return new DefaultHwPackageManagerUtils();
    }

    public DefaultMultiWinWhiteListManager getMultiWinWhiteListManager() {
        return new DefaultMultiWinWhiteListManager();
    }
}
