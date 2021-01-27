package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfoEx;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import com.android.server.gesture.GestureNavConst;
import com.huawei.android.content.IntentExEx;
import com.huawei.android.content.res.ConfigurationAdapter;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.forcerotation.HwForceRotationManager;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import com.huawei.server.HwPCFactory;

public class HwActivityRecord extends ActivityRecordBridgeEx {
    private static final boolean IS_TABLET = GestureNavConst.DEVICE_TYPE_TABLET.equals(SystemPropertiesEx.get("ro.build.characteristics", BuildConfig.FLAVOR));
    public boolean isDelayFinished;
    private long mCreateTime;
    int mCustomRequestedOrientation;
    private boolean mIsAniRunningBelow;
    private boolean mIsFinishAllRightBottom;
    private boolean mIsFromFullscreenToMagicWin;
    private boolean mIsFullScreenVideoInLandscape;
    private boolean mIsSplitMode;
    private boolean mIsStartFromLauncher;
    private int mLastActivityHash;
    private Rect mLastBound;
    private Bundle mMagicWinConfigBundle;
    private int mMagicWindowPageType;

    public HwActivityRecord(ActivityTaskManagerServiceEx _serviceEx, WindowProcessControllerEx _callerEx, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecordEx _resultToEx, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisorEx supervisorEx, ActivityOptions options, ActivityRecordEx sourceRecordEx) {
        super(_serviceEx, _callerEx, _launchedFromPid, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultToEx, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisorEx, options, sourceRecordEx);
        this.isDelayFinished = false;
        this.mCustomRequestedOrientation = 0;
        this.mMagicWindowPageType = 0;
        this.mIsFullScreenVideoInLandscape = false;
        this.mIsFinishAllRightBottom = false;
        this.mIsAniRunningBelow = false;
        this.mIsFromFullscreenToMagicWin = false;
        this.mCreateTime = System.currentTimeMillis();
        this.mLastActivityHash = -1;
        this.mIsStartFromLauncher = false;
        this.mIsFullScreenVideoInLandscape = ActivityInfoEx.isFixedOrientationLandscape(getActivityInfo().screenOrientation);
        initSplitMode(_intent);
    }

    /* access modifiers changed from: protected */
    public void scheduleMultiWindowModeChanged(Configuration overrideConfig) {
        if (!inHwMagicWindowingMode()) {
            HwActivityRecord.super.aospScheduleMultiWindowModeChanged(overrideConfig);
            if (!HwMultiWindowManager.IS_HW_MULTIWINDOW_SUPPORTED) {
                onMultiWindowModeChanged(inMultiWindowMode());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initSplitMode(Intent intent) {
        if (intent != null) {
            this.mIsSplitMode = (IntentExEx.getHwFlags(intent) & 4) != 0 && (IntentExEx.getHwFlags(intent) & 8) == 0;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSplitMode() {
        return this.mIsSplitMode;
    }

    public void schedulePCWindowStateChanged() {
        HwActivityRecord.super.schedulePCWindowStateChanged();
    }

    /* access modifiers changed from: protected */
    public void computeBounds(Rect outBounds, Rect containingAppBounds) {
        if (isTaskEmpty() || !HwPCUtils.isExtDynamicStack(getStackIdFromTask())) {
            HwActivityRecord.super.aospComputeBounds(outBounds, containingAppBounds);
        } else {
            outBounds.setEmpty();
        }
    }

    /* access modifiers changed from: protected */
    public void setRequestedOrientation(int requestedOrientation) {
        DefaultHwPCMultiWindowManager multiWindowMgr;
        if (!HwMwUtils.ENABLED || !getBundle(41, requestedOrientation).getBoolean("RESULT_REJECT_ORIENTATION", false)) {
            updateTaskByRequestedOrientationForPCCast(aospGetTaskId(), requestedOrientation);
            HwActivityRecord.super.setAospRequestedOrientation(requestedOrientation);
            HwPCUtils.log("HwPCMultiWindowManager", "requestedOrientation: " + requestedOrientation);
            if (HwPCUtils.enabledInPad() && HwPCUtils.isExtDynamicStack(getStackIdFromTask()) && isSameTask() && (multiWindowMgr = getHwPCMultiWindowManager(buildAtmsEx())) != null) {
                if (multiWindowMgr.isFixedOrientationPortrait(requestedOrientation)) {
                    this.mCustomRequestedOrientation = 1;
                } else if (multiWindowMgr.isFixedOrientationLandscape(requestedOrientation)) {
                    this.mCustomRequestedOrientation = 2;
                }
                HwPCUtils.log("HwPCMultiWindowManager", "doCustomRequestedOrientation: " + this.mCustomRequestedOrientation + " (" + toString() + ")");
                multiWindowMgr.updateTaskByRequestedOrientation(buildTaskRecordEx(), this.mCustomRequestedOrientation);
            }
            if (HwMwUtils.ENABLED) {
                HwMwUtils.performPolicy(24, new Object[]{Integer.valueOf(requestedOrientation)});
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isForceRotationMode(String packageName, Intent _intent) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported() || !forceRotationManager.isForceRotationSwitchOpen() || UserHandleEx.isIsolated(Binder.getCallingUid())) {
            return false;
        }
        boolean isAppInForceRotationWhiteList = false;
        if (packageName != null) {
            isAppInForceRotationWhiteList = forceRotationManager.isAppInForceRotationWhiteList(packageName);
        }
        boolean isFirstActivity = (_intent.getFlags() & 67108864) != 0;
        if (!isFirstActivity && _intent.getCategories() != null) {
            isFirstActivity = _intent.getCategories().contains("android.intent.category.LAUNCHER");
        }
        if (!isAppInForceRotationWhiteList || !isFirstActivity) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public int overrideRealConfigChanged(ActivityInfo info) {
        int realConfigChange = ActivityInfoEx.getRealConfigChanged(info);
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (forceRotationManager.isForceRotationSupported() && forceRotationManager.isForceRotationSwitchOpen() && !inMultiWindowMode() && forceRotationManager.isAppInForceRotationWhiteList(info.packageName)) {
            return realConfigChange | 3232;
        }
        return realConfigChange;
    }

    /* access modifiers changed from: protected */
    public int getConfigurationChanges(Configuration lastReportedConfig) {
        if (HwPCUtils.isExtDynamicStack(getStackId())) {
            int changes = lastReportedConfig.diff(getConfiguration());
            if (((changes & 4) | (1073741824 & changes)) == 0) {
                return 0;
            }
        }
        int changes2 = HwActivityRecord.super.getAospConfigurationChanges(lastReportedConfig);
        if (HwMwUtils.ENABLED && HwMwUtils.IS_FOLD_SCREEN_DEVICE && (ActivityInfoEx.getRealConfigChanged(getActivityInfo()) & 2048) == 0) {
            Configuration currentConfig = getConfiguration();
            if (ConfigurationAdapter.getWindowingMode(lastReportedConfig) == 103 && ConfigurationAdapter.getWindowingMode(currentConfig) == 1 && currentConfig.orientation == 2) {
                changes2 &= -2049;
            }
            if (ConfigurationAdapter.getWindowingMode(lastReportedConfig) == 1 && lastReportedConfig.orientation == 2 && ConfigurationAdapter.getWindowingMode(currentConfig) == 103) {
                changes2 &= -2049;
            }
        }
        if (IS_TABLET) {
            if ((changes2 & 16) != 0) {
                changes2 &= -17;
            }
            if ((changes2 & 32) != 0) {
                changes2 &= -33;
                if ((changes2 & 64) != 0) {
                    changes2 &= -65;
                }
            }
        }
        if (isPadCastVirtualDisplayFullScreen(lastReportedConfig)) {
            return 0;
        }
        return changes2;
    }

    private boolean isPadCastVirtualDisplayFullScreen(Configuration lastConfig) {
        Configuration currentConfig = getConfiguration();
        return isVirtualDisplayId("padCast") && ((isFullscreen(lastConfig) && isCenter(currentConfig)) || (isCenter(lastConfig) && isFullscreen(currentConfig)));
    }

    private boolean isFullscreen(Configuration config) {
        if (ConfigurationAdapter.getWindowingMode(config) == 1 && ConfigurationAdapter.getBounds(config) != null && ConfigurationAdapter.getBounds(config).left == 0 && ConfigurationAdapter.getBounds(config).top == 0) {
            return true;
        }
        return false;
    }

    private boolean isCenter(Configuration config) {
        if (ConfigurationAdapter.getWindowingMode(config) != 1 || ConfigurationAdapter.getBounds(config) == null || (ConfigurationAdapter.getBounds(config).left == 0 && ConfigurationAdapter.getBounds(config).top == 0)) {
            return false;
        }
        return true;
    }

    public String getPackageName() {
        return HwActivityRecord.super.aospGetPackageName();
    }

    /* access modifiers changed from: protected */
    public boolean isSplitBaseActivity() {
        return HwActivityRecord.super.isSplitBaseActivity();
    }

    public int getWindowState() {
        if (isTaskEmpty() || buildTaskRecordEx() == null) {
            return -1;
        }
        return buildTaskRecordEx().getWindowState();
    }

    public void resize() {
        HwActivityRecord.super.updateOverrideConfiguration();
        HwActivityRecord.super.resizeAppWindowToken();
    }

    public void setLastActivityHash(int hashValue) {
        this.mLastActivityHash = hashValue;
    }

    public int getLastActivityHash() {
        return this.mLastActivityHash;
    }

    public boolean isStartFromLauncher() {
        return this.mIsStartFromLauncher;
    }

    public void setIsStartFromLauncher(boolean isStartFromLauncher) {
        this.mIsStartFromLauncher = isStartFromLauncher;
    }

    private DefaultHwPCMultiWindowManager getHwPCMultiWindowManager(ActivityTaskManagerServiceEx atmsEx) {
        return HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwPCMultiWindowManager(atmsEx);
    }

    public boolean isFullScreenVideoInLandscape() {
        return this.mIsFullScreenVideoInLandscape;
    }

    public void setFullScreenVideoInLandscape(boolean isInLandscape) {
        this.mIsFullScreenVideoInLandscape = isInLandscape;
    }

    public int getMagicWindowPageType() {
        return this.mMagicWindowPageType;
    }

    public void setMagicWindowPageType(int magicWindowPageType) {
        this.mMagicWindowPageType = magicWindowPageType;
    }

    public Rect getLastBound() {
        return this.mLastBound;
    }

    public void setLastBound(Rect lastBound) {
        this.mLastBound = lastBound;
    }

    public boolean isFinishAllRightBottom() {
        return this.mIsFinishAllRightBottom;
    }

    public void setFinishAllRightBottom(boolean isFinishAllRightBottom) {
        this.mIsFinishAllRightBottom = isFinishAllRightBottom;
    }

    public boolean isFromFullscreenToMagicWin() {
        return this.mIsFromFullscreenToMagicWin;
    }

    public void setFromFullscreenToMagicWin(boolean isFromFullscreenToMagicWin) {
        this.mIsFromFullscreenToMagicWin = isFromFullscreenToMagicWin;
    }

    public boolean isAniRunningBelow() {
        return this.mIsAniRunningBelow;
    }

    public void setIsAniRunningBelow(boolean isRunningBelow) {
        this.mIsAniRunningBelow = isRunningBelow;
    }

    public boolean isDelayFinished() {
        return this.isDelayFinished;
    }

    public void setIsDelayFinished(boolean isFinished) {
        this.isDelayFinished = isFinished;
    }

    public long getCreateTime() {
        return this.mCreateTime;
    }

    public void setCreateTime(long time) {
        this.mCreateTime = time;
    }

    public int getCustomRequestedOrientation() {
        return this.mCustomRequestedOrientation;
    }

    public Bundle getMagicWindowExtras() {
        if (this.mMagicWinConfigBundle == null) {
            this.mMagicWinConfigBundle = new Bundle();
        }
        return this.mMagicWinConfigBundle;
    }

    public void setMagicWindowExtras(Bundle bundle) {
        this.mMagicWinConfigBundle = bundle;
    }
}
