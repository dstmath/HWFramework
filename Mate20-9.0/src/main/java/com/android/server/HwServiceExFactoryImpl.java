package com.android.server;

import android.content.Context;
import android.hardware.SensorManager;
import com.android.server.HwServiceExFactory;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.HwActivityManagerServiceEx;
import com.android.server.am.HwActivityStackSupervisorEx;
import com.android.server.am.HwActivityStarterEx;
import com.android.server.am.HwTaskLaunchParamsModifierEx;
import com.android.server.am.HwTaskRecordEx;
import com.android.server.am.IHwActivityManagerInner;
import com.android.server.am.IHwActivityManagerServiceEx;
import com.android.server.audio.HwAudioServiceEx;
import com.android.server.audio.IHwAudioServiceEx;
import com.android.server.audio.IHwAudioServiceInner;
import com.android.server.connectivity.IHwConnectivityServiceInner;
import com.android.server.display.HwDisplayManagerServiceEx;
import com.android.server.display.HwDisplayPowerControllerEx;
import com.android.server.display.IHwDisplayManagerInner;
import com.android.server.display.IHwDisplayManagerServiceEx;
import com.android.server.display.IHwDisplayPowerControllerEx;
import com.android.server.imm.HwInputMethodManagerServiceEx;
import com.android.server.imm.IHwInputMethodManagerInner;
import com.android.server.imm.IHwInputMethodManagerServiceEx;
import com.android.server.input.HwInputManagerServiceEx;
import com.android.server.input.IHwInputManagerInner;
import com.android.server.input.IHwInputManagerServiceEx;
import com.android.server.media.projection.HwMediaProjectionManagerServiceEx;
import com.android.server.media.projection.IHwMediaProjectionManagerServiceInner;
import com.android.server.net.HwNetworkStatsServiceEx;
import com.android.server.net.IHwNetworkStatsInner;
import com.android.server.net.IHwNetworkStatsServiceEx;
import com.android.server.pm.HwBackgroundDexOptServiceEx;
import com.android.server.pm.HwPackageManagerServiceEx;
import com.android.server.pm.IHwBackgroundDexOptInner;
import com.android.server.pm.IHwBackgroundDexOptServiceEx;
import com.android.server.pm.IHwPackageManagerInner;
import com.android.server.pm.IHwPackageManagerServiceEx;
import com.android.server.policy.HwPhoneWindowManagerEx;
import com.android.server.policy.IHwPhoneWindowManagerEx;
import com.android.server.policy.IHwPhoneWindowManagerInner;
import com.android.server.power.HwPowerManagerServiceEx;
import com.android.server.power.IHwPowerManagerInner;
import com.android.server.power.IHwPowerManagerServiceEx;
import com.android.server.vr.HwVrManagerServiceEx;
import com.android.server.vr.IHwVrManagerServiceEx;
import com.android.server.wm.HwTaskPositionerEx;
import com.android.server.wm.HwTaskStackEx;
import com.android.server.wm.HwWindowManagerServiceEx;
import com.android.server.wm.HwWindowStateEx;
import com.android.server.wm.IHwTaskPositionerEx;
import com.android.server.wm.IHwTaskStackEx;
import com.android.server.wm.IHwWindowManagerInner;
import com.android.server.wm.IHwWindowManagerServiceEx;
import com.android.server.wm.IHwWindowStateEx;
import com.android.server.wm.TaskStack;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import com.huawei.server.am.IHwActivityStackSupervisorEx;
import com.huawei.server.am.IHwActivityStarterEx;
import com.huawei.server.am.IHwTaskLaunchParamsModifierEx;
import com.huawei.server.am.IHwTaskRecordEx;
import com.huawei.server.connectivity.IHwConnectivityServiceEx;
import com.huawei.server.media.projection.IHwMediaProjectionManagerServiceEx;
import huawei.com.android.server.connectivity.HwConnectivityServiceEx;

public class HwServiceExFactoryImpl implements HwServiceExFactory.Factory {
    private static final String TAG = "HwServiceExFactoryImpl";

    public IHwActivityManagerServiceEx getHwActivityManagerServiceEx(IHwActivityManagerInner ams, Context context) {
        return new HwActivityManagerServiceEx(ams, context);
    }

    public IHwWindowManagerServiceEx getHwWindowManagerServiceEx(IHwWindowManagerInner wms, Context context) {
        return new HwWindowManagerServiceEx(wms, context);
    }

    public IHwPackageManagerServiceEx getHwPackageManagerServiceEx(IHwPackageManagerInner pms, Context context) {
        return new HwPackageManagerServiceEx(pms, context);
    }

    public IHwInputMethodManagerServiceEx getHwInputMethodManagerServiceEx(IHwInputMethodManagerInner ims, Context context) {
        return new HwInputMethodManagerServiceEx(ims, context);
    }

    public IHwBackgroundDexOptServiceEx getHwBackgroundDexOptServiceEx(IHwBackgroundDexOptInner bdos, Context context) {
        return new HwBackgroundDexOptServiceEx(bdos, context);
    }

    public IHwActivityStarterEx getHwActivityStarterEx(ActivityManagerService ams) {
        return new HwActivityStarterEx(ams);
    }

    public IHwAudioServiceEx getHwAudioServiceEx(IHwAudioServiceInner ias, Context context) {
        return new HwAudioServiceEx(ias, context);
    }

    public IHwPowerManagerServiceEx getHwPowerManagerServiceEx(IHwPowerManagerInner pms, Context context) {
        return new HwPowerManagerServiceEx(pms, context);
    }

    public IHwInputManagerServiceEx getHwInputManagerServiceEx(IHwInputManagerInner ims, Context context) {
        return new HwInputManagerServiceEx(ims, context);
    }

    public IHwDisplayManagerServiceEx getHwDisplayManagerServiceEx(IHwDisplayManagerInner dms, Context context) {
        return new HwDisplayManagerServiceEx(dms, context);
    }

    public IHwTaskPositionerEx getHwTaskPositionerEx(WindowManagerService wms) {
        return new HwTaskPositionerEx(wms);
    }

    public IHwWindowStateEx getHwWindowStateEx(WindowManagerService wms, WindowState windowState) {
        return new HwWindowStateEx(wms, windowState);
    }

    public IHwPhoneWindowManagerEx getHwPhoneWindowManagerEx(IHwPhoneWindowManagerInner pws, Context context) {
        return new HwPhoneWindowManagerEx(pws, context);
    }

    public IHwTaskStackEx getHwTaskStackEx(TaskStack taskStack, WindowManagerService wms) {
        return new HwTaskStackEx(taskStack, wms);
    }

    public IHwActivityStackSupervisorEx getHwActivityStackSupervisorEx(ActivityManagerService ams) {
        return new HwActivityStackSupervisorEx(ams);
    }

    public IHwTaskLaunchParamsModifierEx getHwTaskLaunchParamsModifierEx() {
        return new HwTaskLaunchParamsModifierEx();
    }

    public IHwTaskRecordEx getHwTaskRecordEx() {
        return new HwTaskRecordEx();
    }

    public IHwVrManagerServiceEx getHwVrManagerServiceEx() {
        return new HwVrManagerServiceEx();
    }

    public IHwConnectivityServiceEx getHwConnectivityServiceEx(IHwConnectivityServiceInner csi, Context context) {
        return new HwConnectivityServiceEx(csi, context);
    }

    public IHwMediaProjectionManagerServiceEx getHwMediaProjectionManagerServiceEx(IHwMediaProjectionManagerServiceInner mpms, Context context) {
        return new HwMediaProjectionManagerServiceEx(mpms, context);
    }

    public IHwNetworkStatsServiceEx getHwNetworkStatsServiceEx(IHwNetworkStatsInner nss, Context context) {
        return new HwNetworkStatsServiceEx(nss, context);
    }

    public IHwDisplayPowerControllerEx getHwDisplayPowerControllerEx(Context context, IHwDisplayPowerControllerEx.Callbacks callbacks, SensorManager sensorManager) {
        return new HwDisplayPowerControllerEx(context, callbacks, sensorManager);
    }
}
