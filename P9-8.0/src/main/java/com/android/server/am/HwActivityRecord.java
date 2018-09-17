package com.android.server.am;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.HwPCUtils;
import android.util.Slog;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.huawei.forcerotation.HwForceRotationManager;
import java.util.Set;

public class HwActivityRecord extends ActivityRecord {
    int mCustomRequestedOrientation = 0;
    private boolean mSplitMode;

    public HwActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityContainer container, ActivityOptions options, ActivityRecord sourceRecord) {
        super(_service, _caller, _launchedFromPid, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultTo, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisor, container, options, sourceRecord);
    }

    private boolean isLaunchIntentForCamera(String shortComponentName, Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories == null || ((!categories.contains("android.intent.category.INFO") && !categories.contains("android.intent.category.LAUNCHER")) || shortComponentName == null || !shortComponentName.contains("camera"))) {
            return false;
        }
        return true;
    }

    private boolean isChooserActivity(Intent aIntent) {
        if (aIntent == null || aIntent.getComponent() == null || aIntent.getComponent().getClassName() == null || !"com.huawei.android.internal.app.HwResolverActivity".equalsIgnoreCase(aIntent.getComponent().getClassName())) {
            return false;
        }
        return true;
    }

    void scheduleMultiWindowModeChanged(Configuration overrideConfig) {
        super.scheduleMultiWindowModeChanged(overrideConfig);
        if (this.task != null) {
            this.service.onMultiWindowModeChanged(this.task.mFullscreen ^ 1);
        }
    }

    protected void initSplitMode(Intent intent) {
        boolean z = false;
        if (intent != null) {
            if ((intent.getHwFlags() & 4) != 0 && (intent.getHwFlags() & 8) == 0) {
                z = true;
            }
            this.mSplitMode = z;
        }
    }

    protected boolean isSplitMode() {
        return this.mSplitMode;
    }

    public void schedulePCWindowStateChanged() {
        if (this.task != null && this.task.getStack() != null && this.app != null && this.app.thread != null) {
            try {
                this.app.thread.schedulePCWindowStateChanged(this.appToken, this.task.getWindowState());
            } catch (RemoteException e) {
                Slog.d("HwActivityRecord", "on schedulePCWindowStateChanged error", e);
            }
        }
    }

    protected void computeBounds(Rect outBounds) {
        if (this.task == null || !HwPCUtils.isPcDynamicStack(this.task.getStackId())) {
            super.computeBounds(outBounds);
        } else {
            outBounds.setEmpty();
        }
    }

    void setRequestedOrientation(int requestedOrientation) {
        super.setRequestedOrientation(requestedOrientation);
        HwPCUtils.log("HwPCMultiWindowManager", "requestedOrientation: " + requestedOrientation);
        if (this.task != null && HwPCUtils.isPcDynamicStack(this.task.getStackId()) && this.task.getTopActivity() == this && (this.service instanceof HwActivityManagerService)) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this.service);
            if (multiWindowMgr != null) {
                if (HwPCMultiWindowManager.isFixedOrientationPortrait(requestedOrientation)) {
                    this.mCustomRequestedOrientation = 1;
                } else if (HwPCMultiWindowManager.isFixedOrientationLandscape(requestedOrientation)) {
                    this.mCustomRequestedOrientation = 2;
                }
                HwPCUtils.log("HwPCMultiWindowManager", "doCustomRequestedOrientation: " + this.mCustomRequestedOrientation + " (" + toString() + ")");
                multiWindowMgr.updateTaskByRequestedOrientation(this.task, this.mCustomRequestedOrientation);
            }
        }
    }

    protected boolean isForceRotationMode(String packageName, Intent _intent) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported() || !forceRotationManager.isForceRotationSwitchOpen() || UserHandle.isIsolated(Binder.getCallingUid())) {
            return false;
        }
        boolean isAppInForceRotationWhiteList = false;
        if (packageName != null) {
            isAppInForceRotationWhiteList = forceRotationManager.isAppInForceRotationWhiteList(packageName);
        }
        boolean isFirstActivity = (_intent.getFlags() & 67108864) != 0;
        if (!(isFirstActivity || _intent.getCategories() == null)) {
            isFirstActivity = _intent.getCategories().contains("android.intent.category.LAUNCHER");
        }
        if (!isAppInForceRotationWhiteList) {
            isFirstActivity = false;
        }
        return isFirstActivity;
    }

    protected int overrideRealConfigChanged(ActivityInfo info) {
        int realConfigChange = info.getRealConfigChanged();
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported() || !forceRotationManager.isForceRotationSwitchOpen()) {
            return realConfigChange;
        }
        if (this.task.mFullscreen && forceRotationManager.isAppInForceRotationWhiteList(info.packageName)) {
            realConfigChange |= 3232;
        }
        return realConfigChange;
    }

    protected int getConfigurationChanges(Configuration lastReportedConfig) {
        if (HwPCUtils.isPcDynamicStack(getStackId())) {
            int changes = lastReportedConfig.diff(getConfiguration());
            if (((changes & 4) | (1073741824 & changes)) == 0) {
                return 0;
            }
        }
        return super.getConfigurationChanges(lastReportedConfig);
    }

    public String getPackageName() {
        return this.packageName;
    }

    protected boolean isSplitBaseActivity() {
        ActivityRecord lastResumed = this.service.getLastResumedActivity();
        if (lastResumed != null && lastResumed.isSplitMode() && lastResumed.getTask() != null && lastResumed.getTask() == getTask() && this == getTask().getRootActivity()) {
            return true;
        }
        return false;
    }

    public int getWindowState() {
        if (this.task == null) {
            return -1;
        }
        return this.task.getWindowState();
    }
}
