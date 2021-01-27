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
import com.android.server.HwAlarmManagerService;
import com.android.server.HwBatteryService;
import com.android.server.HwGeneralService;
import com.android.server.accessibility.HwMagnificationGestureHandler;
import com.android.server.accessibility.MagnificationControllerEx;
import com.android.server.adb.HwAdbService;
import com.android.server.gesture.DeviceStateController;
import com.android.server.gesture.GestureNavConst;
import com.android.server.gesture.GestureNavManager;
import com.android.server.gesture.GestureUtils;
import com.android.server.gesture.HwGestureNavWhiteConfig;
import com.android.server.input.HwInputManagerService;
import com.android.server.input.HwInputManagerServiceEx;
import com.android.server.input.IHwInputManagerInner;
import com.android.server.lights.HwLightsService;
import com.android.server.notch.HwNotchScreenWhiteConfig;
import com.android.server.pm.HwPackageManagerServiceEx;
import com.android.server.pm.HwPackageManagerUtils;
import com.android.server.pm.HwPackageServiceManagerImpl;
import com.android.server.pm.HwPluginPackage;
import com.android.server.pm.HwUserManagerService;
import com.android.server.pm.IHwPackageManagerInnerEx;
import com.android.server.pm.MultiWinWhiteListManager;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.pm.UserDataPreparerEx;
import com.android.server.pm.permission.DefaultPermissionGrantPolicyEx;
import com.android.server.pm.permission.HwDefaultPermissionGrantPolicy;
import com.android.server.pm.permission.PermissionManagerServiceEx;
import com.android.server.policy.EasyWakeUpManager;
import com.android.server.policy.FingerprintActionsListener;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.PhoneWindowManagerEx;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.power.HwPowerManagerServiceEx;
import com.android.server.power.IHwPowerManagerInnerEx;
import com.android.server.usb.HwUsbDeviceManager;
import com.android.server.usb.UsbAlsaManagerEx;
import com.android.server.usb.UsbSettingsManagerEx;
import com.android.server.wm.ActivityDisplayEx;
import com.android.server.wm.ActivityRecordBridgeEx;
import com.android.server.wm.ActivityRecordEx;
import com.android.server.wm.ActivityStackBridgeEx;
import com.android.server.wm.ActivityStackEx;
import com.android.server.wm.ActivityStackSupervisorEx;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.DisplayContentBridgeEx;
import com.android.server.wm.DisplayContentEx;
import com.android.server.wm.HwActivityRecord;
import com.android.server.wm.HwActivityStack;
import com.android.server.wm.HwActivityStackSupervisor;
import com.android.server.wm.HwActivityStartInterceptor;
import com.android.server.wm.HwAppWindowTokenBridgeEx;
import com.android.server.wm.HwAppWindowTokenImpl;
import com.android.server.wm.HwDisplayContent;
import com.android.server.wm.HwDisplayRotationEx;
import com.android.server.wm.HwRootActivityContainerEx;
import com.android.server.wm.HwScreenRotationAnimationImpl;
import com.android.server.wm.HwScreenRotationAnimationImplBridgeEx;
import com.android.server.wm.HwTaskLaunchParamsModifierEx;
import com.android.server.wm.HwTaskPositionerEx;
import com.android.server.wm.HwTaskRecord;
import com.android.server.wm.HwTaskRecordEx;
import com.android.server.wm.HwTaskStack;
import com.android.server.wm.HwTaskStackEx;
import com.android.server.wm.HwWindowStateAnimator;
import com.android.server.wm.HwWindowStateEx;
import com.android.server.wm.IHwRootActivityContainerInner;
import com.android.server.wm.RootActivityContainerBridgeEx;
import com.android.server.wm.TaskStackBridgeEx;
import com.android.server.wm.TaskStackEx;
import com.android.server.wm.WindowManagerServiceEx;
import com.android.server.wm.WindowProcessControllerEx;
import com.android.server.wm.WindowStateAnimatorBridgeEx;
import com.android.server.wm.WindowStateBridgeEx;
import com.android.server.wm.WindowStateEx;
import com.huawei.android.internal.app.IVoiceInteractorEx;
import com.huawei.server.policy.keyguard.KeyguardServiceDelegateEx;
import com.huawei.server.wm.IHwDisplayRotationEx;
import huawei.com.android.server.policy.HwFalseTouchMonitor;
import huawei.com.android.server.policy.HwScreenOnProximityLock;
import java.util.ArrayList;

public class HwPartBasicPlatformServicesFactory extends DefaultHwBasicPlatformPartFactory {
    public HwAdbService getHwAdbService(Context context) {
        return new HwAdbService(context);
    }

    public HwInputManagerService getHwInputManagerService(Context context, Handler handler) {
        return new HwInputManagerService(context, handler);
    }

    public HwLightsService getHwLightsService(Context context) {
        return new HwLightsService(context);
    }

    public HwAlarmManagerService getHwAlarmManagerService(Context context) {
        return new HwAlarmManagerService(context);
    }

    public HwBatteryService getHwBatteryService(Context context) {
        return new HwBatteryService(context);
    }

    public HwGeneralService getHwGeneralService(Context context, Handler handler) {
        return new HwGeneralService(context, handler);
    }

    public HwMagnificationGestureHandler getHwMagnificationGestureHandler(Context context, MagnificationControllerEx magnificationControllerEx, boolean isDetectControlGestures, boolean isTriggerable, int displayId) {
        return new HwMagnificationGestureHandler(context, magnificationControllerEx, isDetectControlGestures, isTriggerable, displayId);
    }

    public DeviceStateController getDeviceStateController(Context context) {
        return DeviceStateController.getInstance(context);
    }

    public GestureNavConst getGestureNavConst() {
        return new GestureNavConst();
    }

    public GestureNavManager getGestureNavManager(Context context) {
        return new GestureNavManager(context);
    }

    public GestureUtils getGestureUtils() {
        return new GestureUtils();
    }

    public HwGestureNavWhiteConfig getHwGestureNavWhiteConfig() {
        return HwGestureNavWhiteConfig.getInstance();
    }

    public HwInputManagerServiceEx getHwInputManagerServiceEx(IHwInputManagerInner ims, Context context) {
        return new HwInputManagerServiceEx(ims, context);
    }

    public EasyWakeUpManager getEasyWakeUpManager(Context context, Handler handler, KeyguardServiceDelegateEx keyguardDelegate) {
        return EasyWakeUpManager.getInstance(context, handler, keyguardDelegate);
    }

    public FingerprintActionsListener getFingerprintActionsListener(Context context, PhoneWindowManagerEx policy) {
        return FingerprintActionsListener.getInstance(context, policy);
    }

    public HwFalseTouchMonitor getHwFalseTouchMonitor() {
        return HwFalseTouchMonitor.getInstance();
    }

    public HwScreenOnProximityLock getHwScreenOnProximityLock(Context context, HwPhoneWindowManager phoneWindowManager, WindowManagerPolicyEx.WindowManagerFuncsEx windowFuncs, Handler handler) {
        return new HwScreenOnProximityLock(context, phoneWindowManager, windowFuncs, handler);
    }

    public HwPowerManagerServiceEx getHwPowerManagerServiceEx(IHwPowerManagerInnerEx pms, Context context) {
        return new HwPowerManagerServiceEx(pms, context);
    }

    public HwUsbDeviceManager getHwUsbDeviceManager(Context context, UsbAlsaManagerEx alsaManager, UsbSettingsManagerEx settingsManager) {
        return new HwUsbDeviceManager(context, alsaManager, settingsManager);
    }

    public ActivityRecordBridgeEx createActivityRecord(ActivityTaskManagerServiceEx serviceEx, WindowProcessControllerEx callerEx, int launchedFromPid, int launchedFromUid, String launchedFromPackage, Intent intent, String resolvedType, ActivityInfo activityInfo, Configuration configuration, ActivityRecordEx resultToEx, String resultWho, int reqCode, boolean isComponentSpecified, boolean isRootVoiceInteraction, ActivityStackSupervisorEx supervisorEx, ActivityOptions options, ActivityRecordEx sourceRecordEx) {
        return new HwActivityRecord(serviceEx, callerEx, launchedFromPid, launchedFromUid, launchedFromPackage, intent, resolvedType, activityInfo, configuration, resultToEx, resultWho, reqCode, isComponentSpecified, isRootVoiceInteraction, supervisorEx, options, sourceRecordEx);
    }

    public HwAppWindowTokenBridgeEx getHwAppWindowTokenEx() {
        return new HwAppWindowTokenImpl();
    }

    public DisplayContentBridgeEx createDisplayContent(Display display, WindowManagerServiceEx service, ActivityDisplayEx activityDisplay) {
        return new HwDisplayContent(display, service, activityDisplay);
    }

    public IHwDisplayRotationEx getHwDisplayRotationEx(WindowManagerServiceEx serviceEx, DisplayContentEx displayContentEx, boolean isDefaultDisplay) {
        return new HwDisplayRotationEx(serviceEx, displayContentEx, isDefaultDisplay);
    }

    public RootActivityContainerBridgeEx getHwRootActivityContainerEx(IHwRootActivityContainerInner rac, ActivityTaskManagerServiceEx serviceEx) {
        return new HwRootActivityContainerEx(rac, serviceEx);
    }

    public HwScreenRotationAnimationImplBridgeEx getHwScreenRotationAnimation() {
        return new HwScreenRotationAnimationImpl();
    }

    public TaskStackBridgeEx createTaskStack(WindowManagerServiceEx serviceEx, int stackId, ActivityStackEx activityStackEx) {
        return new HwTaskStack(serviceEx, stackId, activityStackEx);
    }

    public WindowStateAnimatorBridgeEx createWindowStateAnimator(WindowStateEx winEx) {
        return new HwWindowStateAnimator(winEx);
    }

    public WindowStateBridgeEx getHwWindowStateEx(WindowManagerServiceEx wmsEx, WindowStateEx windowStateEx) {
        return new HwWindowStateEx(wmsEx, windowStateEx);
    }

    public HwNotchScreenWhiteConfig getHwNotchScreenWhiteConfig() {
        return HwNotchScreenWhiteConfig.getInstance();
    }

    public HwTaskLaunchParamsModifierEx getHwTaskLaunchParamsModifierEx() {
        return new HwTaskLaunchParamsModifierEx();
    }

    public HwTaskPositionerEx getHwTaskPositionerEx(WindowManagerServiceEx windowManagerServiceEx) {
        return new HwTaskPositionerEx(windowManagerServiceEx);
    }

    public HwTaskRecordEx getHwTaskRecordEx() {
        return new HwTaskRecordEx();
    }

    public HwTaskStackEx getHwTaskStackEx(TaskStackEx taskStackEx, WindowManagerServiceEx windowManagerServiceEx) {
        return new HwTaskStackEx(taskStackEx, windowManagerServiceEx);
    }

    public ActivityStackBridgeEx createActivityStack(ActivityDisplayEx display, int stackId, ActivityStackSupervisorEx supervisor, int windowingMode, int activityType, boolean isOnTop) {
        return new HwActivityStack(display, stackId, supervisor, windowingMode, activityType, isOnTop);
    }

    public HwActivityStartInterceptor createActivityStartInterceptor(ActivityTaskManagerServiceEx service, ActivityStackSupervisorEx supervisor) {
        return new HwActivityStartInterceptor(service, supervisor);
    }

    public HwTaskRecord createTaskRecord(ActivityTaskManagerServiceEx service, int taskId, ActivityInfo info, Intent intent, IVoiceInteractionSessionEx voiceSession, IVoiceInteractorEx voiceInteractor) {
        return new HwTaskRecord(service, taskId, info, intent, voiceSession, voiceInteractor);
    }

    public HwTaskRecord createTaskRecord(ActivityTaskManagerServiceEx service, int taskId, ActivityInfo info, Intent intent, ActivityManager.TaskDescription taskDescription) {
        return new HwTaskRecord(service, taskId, info, intent, taskDescription);
    }

    public HwTaskRecord createTaskRecord(ActivityTaskManagerServiceEx service, int taskId, Intent intent, Intent affinityIntent, String affinity, String rootAffinity, ComponentName realActivity, ComponentName origActivity, boolean rootWasReset, boolean autoRemoveRecents, boolean askedCompatMode, int userId, int effectiveUid, String lastDescription, ArrayList<ActivityRecordEx> activities, long lastTimeMoved, boolean neverRelinquishIdentity, ActivityManager.TaskDescription lastTaskDescription, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean supportsPictureInPicture, boolean realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        return new HwTaskRecord(service, taskId, intent, affinityIntent, affinity, rootAffinity, realActivity, origActivity, rootWasReset, autoRemoveRecents, askedCompatMode, userId, effectiveUid, lastDescription, activities, lastTimeMoved, neverRelinquishIdentity, lastTaskDescription, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, supportsPictureInPicture, realActivitySuspended, userSetupComplete, minWidth, minHeight);
    }

    public HwActivityStackSupervisor getHwActivityStackSupervisor(ActivityTaskManagerServiceEx service, Looper looper) {
        return new HwActivityStackSupervisor(service, looper);
    }

    public DefaultPermissionGrantPolicyEx getDefaultPermissionGrantPolicy(Context context, Looper looper, PermissionManagerServiceEx permissionManager) {
        return new HwDefaultPermissionGrantPolicy(context, looper, permissionManager);
    }

    public HwPackageManagerServiceEx getHwPackageManagerServiceEx(PackageManagerServiceEx pms, Context context) {
        return new HwPackageManagerServiceEx(pms, context);
    }

    public HwPluginPackage getHwPluginPackage(IHwPackageManagerInnerEx pm, String packageName) {
        return new HwPluginPackage(pm, packageName);
    }

    public HwPackageServiceManagerImpl getHwPackageServiceManager() {
        return HwPackageServiceManagerImpl.getDefault();
    }

    public HwUserManagerService getUserManagerService(Context context, PackageManagerServiceEx pm, UserDataPreparerEx userDataPreparer, Object packagesLock) {
        return new HwUserManagerService(context, pm, userDataPreparer, packagesLock);
    }

    public HwPackageManagerServiceEx getHwPackageManagerServiceEx() {
        return new HwPackageManagerServiceEx();
    }

    public HwPackageManagerUtils getHwPackageManagerUtils() {
        return new HwPackageManagerUtils();
    }

    public MultiWinWhiteListManager getMultiWinWhiteListManager() {
        return MultiWinWhiteListManager.getInstance();
    }
}
