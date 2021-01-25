package com.android.server.wm;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import com.huawei.android.app.ProfilerInfoEx;

public class ActivityStackSupervisorEx {
    private ActivityStackSupervisor mActivityStackSupervisor;
    private RootActivityContainerEx mRootActivityContainerEx;

    public ActivityStackSupervisorEx() {
    }

    public ActivityStackSupervisorEx(ActivityStackSupervisor supervisor) {
        this.mActivityStackSupervisor = supervisor;
    }

    public ActivityStackSupervisor getActivityStackSupervisor() {
        return this.mActivityStackSupervisor;
    }

    public void setActivityStackSupervisor(ActivityStackSupervisor activityStackSupervisor) {
        this.mActivityStackSupervisor = activityStackSupervisor;
    }

    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int userId, int flags, int filterCallingUid) {
        return this.mActivityStackSupervisor.resolveIntent(intent, resolvedType, userId, flags, filterCallingUid);
    }

    public ActivityInfo resolveActivity(Intent intent, ResolveInfo resolveInfo, int startFlags, ProfilerInfoEx profilerInfo) {
        return this.mActivityStackSupervisor.resolveActivity(intent, resolveInfo, startFlags, profilerInfo == null ? null : profilerInfo.getProfilerInfo());
    }

    public KeyguardControllerEx getKeyguardController() {
        KeyguardControllerEx keyguardControllerEx = new KeyguardControllerEx();
        ActivityStackSupervisor activityStackSupervisor = this.mActivityStackSupervisor;
        if (!(activityStackSupervisor == null || activityStackSupervisor.getKeyguardController() == null)) {
            keyguardControllerEx.setKeyguardController(this.mActivityStackSupervisor.getKeyguardController());
        }
        return keyguardControllerEx;
    }

    public RootActivityContainerEx getRootActivityContainerEx() {
        ActivityStackSupervisor activityStackSupervisor;
        if (!(this.mRootActivityContainerEx != null || (activityStackSupervisor = this.mActivityStackSupervisor) == null || activityStackSupervisor.mRootActivityContainer == null)) {
            this.mRootActivityContainerEx = new RootActivityContainerEx();
            this.mRootActivityContainerEx.setRootActivityContainer(this.mActivityStackSupervisor.mRootActivityContainer);
        }
        return this.mRootActivityContainerEx;
    }

    public String getActivityLaunchTrack() {
        ActivityStackSupervisor activityStackSupervisor = this.mActivityStackSupervisor;
        if (activityStackSupervisor != null) {
            return activityStackSupervisor.mActivityLaunchTrack;
        }
        return "";
    }
}
