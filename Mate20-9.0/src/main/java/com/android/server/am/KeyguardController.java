package com.android.server.am;

import android.app.ActivityManagerInternal;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.server.wm.WindowManagerService;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

class KeyguardController {
    private static final int FINGERPRINT_BOOST_WHITELIST_COUNT = 79;
    private static final Set<String> FINGERPRINT_UNLOCK_BOOST_WHITELIST = new HashSet(79);
    private static final boolean PROP_FINGER_BOOST = SystemProperties.getBoolean("persist.debug.finger_boost", true);
    private static final String TAG = "ActivityManager";
    private boolean mAodShowing;
    private int mBeforeUnoccludeTransit;
    private boolean mDismissalRequested;
    private ActivityRecord mDismissingKeyguardActivity;
    private boolean mFingerprintUnlockBoostEnable;
    private boolean mKeyguardGoingAway;
    private boolean mKeyguardShowing;
    private boolean mOccluded;
    private int mSecondaryDisplayShowing = -1;
    private final ActivityManagerService mService;
    private ActivityManagerInternal.SleepToken mSleepToken;
    private final ActivityStackSupervisor mStackSupervisor;
    private int mVisibilityTransactionDepth;
    private WindowManagerService mWindowManager;

    static {
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.tencent.mm");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.tencent.mobileqq");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.sina.weibo");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.happyelements.AndroidAnimal");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.tencent.tmgp.pubgmhd");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.youku.phone");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.smile.gifmaker");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.ss.android.ugc.aweme");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.taobao.taobao");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.jingdong.app.mall");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.xunmeng.pinduoduo");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.baidu.searchbox");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.tencent.mtt");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.ss.android.article.news");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.autonavi.minimap");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.tencent.map");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.mt.mtxx.mtxx");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.meitu.meiyancamera");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.campmobile.snowcamera");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.lemon.faceu");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.tmall.wireless");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.chaozh.iReaderFree");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.qq.reader");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.ophone.reader.ui");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.ss.android.ugc.live");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.tencent.tmgp.cf");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.tencent.tmgp.minitech.miniworld");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.android.launcher");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.stk");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.documentsui");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.hicloud.android.clone");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.KoBackup");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.compass");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.android.remotecontroller");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.hwmirror");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.android.findmyphone");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.soundrecorder");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.hidisk");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.calculator2");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.contacts");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.hwvplayer.youku");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.hiskytone");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.fans");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.smarthome");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.browser");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.camera");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.mms");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.android.tips");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.gamebox");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.lives");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.email");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.calendar");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.deskclock");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.example.android.notepad");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.settings");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.systemmanager");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.phoneservice");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.health");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.appmarket");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.vmall.client");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.android.thememanager");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.hwireader");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.mediacenter");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.huawei.himovie");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.waze");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.linkedin.android");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.instagram.android");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.whatsapp");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.android.chrome");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.google.android.youtube");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.netflix.mediaclient");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.microsoft.office.word");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.microsoft.office.excel");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.booking");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.yahoo.mobile.client.android.mail");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.tencent.ig");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.UCMobile.intl");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.kiloo.subwaysurf");
        FINGERPRINT_UNLOCK_BOOST_WHITELIST.add("com.h8games.helixjump");
    }

    KeyguardController(ActivityManagerService service, ActivityStackSupervisor stackSupervisor) {
        this.mService = service;
        this.mStackSupervisor = stackSupervisor;
        this.mFingerprintUnlockBoostEnable = false;
    }

    /* access modifiers changed from: package-private */
    public void setWindowManager(WindowManagerService windowManager) {
        this.mWindowManager = windowManager;
    }

    /* access modifiers changed from: package-private */
    public void setFingerprintUnlockBoostStatus(boolean boostEnable) {
        this.mFingerprintUnlockBoostEnable = boostEnable;
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardOrAodShowing(int displayId) {
        return (this.mKeyguardShowing || this.mAodShowing) && !this.mKeyguardGoingAway && (displayId != 0 ? displayId == this.mSecondaryDisplayShowing : !this.mOccluded);
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardShowing(int displayId) {
        return this.mKeyguardShowing && !this.mKeyguardGoingAway && (displayId != 0 ? displayId == this.mSecondaryDisplayShowing : !this.mOccluded);
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardLocked() {
        return this.mKeyguardShowing && !this.mKeyguardGoingAway;
    }

    /* access modifiers changed from: package-private */
    public boolean isKeyguardGoingAway() {
        return this.mKeyguardGoingAway && this.mKeyguardShowing;
    }

    /* access modifiers changed from: package-private */
    public void setKeyguardShown(boolean keyguardShowing, boolean aodShowing, int secondaryDisplayShowing) {
        boolean z = true;
        boolean showingChanged = (keyguardShowing == this.mKeyguardShowing && aodShowing == this.mAodShowing) ? false : true;
        if (!this.mKeyguardGoingAway || !keyguardShowing) {
            z = false;
        }
        boolean showingChanged2 = showingChanged | z;
        if (showingChanged2 || secondaryDisplayShowing != this.mSecondaryDisplayShowing) {
            if (ActivityManagerDebugConfig.DEBUG_KEYGUARD) {
                Flog.i(107, "last mKeyguardShowing:" + this.mKeyguardShowing + " mAodShowing:" + this.mAodShowing + " mSecondaryDisplayShowing:" + this.mSecondaryDisplayShowing);
            }
            this.mKeyguardShowing = keyguardShowing;
            this.mAodShowing = aodShowing;
            this.mSecondaryDisplayShowing = secondaryDisplayShowing;
            this.mWindowManager.setAodShowing(aodShowing);
            if (showingChanged2) {
                dismissDockedStackIfNeeded();
                setKeyguardGoingAway(false);
                this.mWindowManager.setKeyguardOrAodShowingOnDefaultDisplay(isKeyguardOrAodShowing(0));
                if (keyguardShowing) {
                    this.mDismissalRequested = false;
                }
            }
            this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
            updateKeyguardSleepToken();
        }
    }

    /* access modifiers changed from: package-private */
    public void keyguardGoingAway(int flags) {
        String str;
        if (this.mKeyguardShowing) {
            Trace.traceBegin(64, "keyguardGoingAway");
            this.mWindowManager.deferSurfaceLayout();
            try {
                setKeyguardGoingAway(true);
                this.mWindowManager.prepareAppTransition(20, false, convertTransitFlags(flags), false);
                updateKeyguardSleepToken();
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                if (!isActivityVisiableInFingerBoost(this.mService.getLastResumedActivity())) {
                    this.mStackSupervisor.addStartingWindowsForVisibleActivities(true);
                }
                this.mWindowManager.executeAppTransition();
            } finally {
                str = "keyguardGoingAway: surfaceLayout";
                Trace.traceBegin(64, str);
                this.mWindowManager.continueSurfaceLayout();
                Trace.traceEnd(64);
                Trace.traceEnd(64);
            }
        }
    }

    private boolean isActivityVisiableInFingerBoost(ActivityRecord r) {
        if (!this.mFingerprintUnlockBoostEnable || !PROP_FINGER_BOOST || r == null || r.info == null || !FINGERPRINT_UNLOCK_BOOST_WHITELIST.contains(r.info.packageName) || isLandscapeActivity(r)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void dismissKeyguard(IBinder token, IKeyguardDismissCallback callback, CharSequence message) {
        ActivityRecord activityRecord = ActivityRecord.forTokenLocked(token);
        if (activityRecord == null || !activityRecord.visibleIgnoringKeyguard) {
            failCallback(callback);
            return;
        }
        Flog.i(107, "Activity requesting to dismiss Keyguard: " + activityRecord);
        if (activityRecord.getTurnScreenOnFlag() && activityRecord.isTopRunningActivity()) {
            this.mStackSupervisor.wakeUp("dismissKeyguard");
        }
        this.mWindowManager.dismissKeyguard(callback, message);
    }

    private void setKeyguardGoingAway(boolean keyguardGoingAway) {
        if (ActivityManagerDebugConfig.DEBUG_KEYGUARD) {
            Flog.i(107, "change mKeyguardGoingAway from:" + this.mKeyguardGoingAway + " to:" + keyguardGoingAway);
        }
        this.mKeyguardGoingAway = keyguardGoingAway;
        this.mWindowManager.setKeyguardGoingAway(keyguardGoingAway);
    }

    private void failCallback(IKeyguardDismissCallback callback) {
        try {
            callback.onDismissError();
        } catch (RemoteException e) {
            Slog.w("ActivityManager", "Failed to call callback", e);
        }
    }

    private int convertTransitFlags(int keyguardGoingAwayFlags) {
        int result = 0;
        if ((keyguardGoingAwayFlags & 1) != 0) {
            result = 0 | 1;
        }
        if ((keyguardGoingAwayFlags & 2) != 0) {
            result |= 2;
        }
        if ((keyguardGoingAwayFlags & 4) != 0) {
            return result | 4;
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public void beginActivityVisibilityUpdate() {
        this.mVisibilityTransactionDepth++;
    }

    /* access modifiers changed from: package-private */
    public void endActivityVisibilityUpdate() {
        this.mVisibilityTransactionDepth--;
        if (this.mVisibilityTransactionDepth == 0) {
            visibilitiesUpdated();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canShowActivityWhileKeyguardShowing(ActivityRecord r, boolean dismissKeyguard) {
        boolean z = true;
        if (isActivityVisiableInFingerBoost(r) && !this.mOccluded) {
            return true;
        }
        if (!dismissKeyguard || !canDismissKeyguard() || this.mAodShowing || (!this.mDismissalRequested && r == this.mDismissingKeyguardActivity)) {
            z = false;
        }
        return z;
    }

    private boolean isLandscapeActivity(ActivityRecord r) {
        if (r == null) {
            return false;
        }
        int iOrientation = r.getRequestedOrientation();
        if (iOrientation == 0 || 6 == iOrientation || 8 == iOrientation || 11 == iOrientation) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean canShowWhileOccluded(boolean dismissKeyguard, boolean showWhenLocked) {
        return showWhenLocked || (dismissKeyguard && !this.mWindowManager.isKeyguardSecure());
    }

    private void visibilitiesUpdated() {
        boolean lastOccluded = this.mOccluded;
        ActivityRecord lastDismissingKeyguardActivity = this.mDismissingKeyguardActivity;
        this.mOccluded = false;
        this.mDismissingKeyguardActivity = null;
        boolean isPadPcCastModeInServer = HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer();
        for (int displayNdx = this.mStackSupervisor.getChildCount() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display = this.mStackSupervisor.getChildAt(displayNdx);
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display.getChildAt(stackNdx);
                if ((display.mDisplayId == 0 || isPadPcCastModeInServer) && this.mStackSupervisor.isFocusedStack(stack)) {
                    ActivityRecord topDismissing = stack.getTopDismissingKeyguardActivity();
                    this.mOccluded = stack.topActivityOccludesKeyguard() || (topDismissing != null && stack.topRunningActivityLocked() == topDismissing && canShowWhileOccluded(true, false));
                    if (ActivityManagerDebugConfig.DEBUG_KEYGUARD) {
                        Flog.i(107, "topDismissing:" + topDismissing + " mOccluded:" + this.mOccluded);
                    }
                }
                if (this.mDismissingKeyguardActivity == null && stack.getTopDismissingKeyguardActivity() != null) {
                    this.mDismissingKeyguardActivity = stack.getTopDismissingKeyguardActivity();
                }
            }
        }
        this.mOccluded |= this.mWindowManager.isShowingDream();
        if (this.mOccluded != lastOccluded) {
            handleOccludedChanged();
        }
        if (ActivityManagerDebugConfig.DEBUG_KEYGUARD && this.mDismissingKeyguardActivity != null) {
            Flog.i(107, "dismissingKeyguardActivity:" + this.mDismissingKeyguardActivity);
        }
        if (this.mDismissingKeyguardActivity != lastDismissingKeyguardActivity) {
            handleDismissKeyguard();
        }
    }

    private void handleOccludedChanged() {
        Flog.i(107, "handleOccludedChanged mOccluded: " + this.mOccluded);
        this.mWindowManager.onKeyguardOccludedChanged(this.mOccluded);
        if (isKeyguardLocked()) {
            this.mWindowManager.deferSurfaceLayout();
            try {
                this.mWindowManager.prepareAppTransition(resolveOccludeTransit(), false, 0, true);
                updateKeyguardSleepToken();
                this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                this.mWindowManager.executeAppTransition();
            } finally {
                this.mWindowManager.continueSurfaceLayout();
            }
        }
        dismissDockedStackIfNeeded();
    }

    private void handleDismissKeyguard() {
        if (!this.mOccluded && this.mDismissingKeyguardActivity != null && this.mWindowManager.isKeyguardSecure()) {
            Flog.i(107, "handleDismissKeyguard");
            this.mWindowManager.dismissKeyguard(null, null);
            this.mDismissalRequested = true;
            if (this.mKeyguardShowing && canDismissKeyguard() && this.mWindowManager.getPendingAppTransition() == 23) {
                this.mWindowManager.prepareAppTransition(this.mBeforeUnoccludeTransit, false, 0, true);
                this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                this.mWindowManager.executeAppTransition();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canDismissKeyguard() {
        return this.mWindowManager.isKeyguardTrusted() || !this.mWindowManager.isKeyguardSecure();
    }

    private int resolveOccludeTransit() {
        if (this.mBeforeUnoccludeTransit != -1 && this.mWindowManager.getPendingAppTransition() == 23 && this.mOccluded) {
            return this.mBeforeUnoccludeTransit;
        }
        if (this.mOccluded) {
            return 22;
        }
        this.mBeforeUnoccludeTransit = this.mWindowManager.getPendingAppTransition();
        return 23;
    }

    private void dismissDockedStackIfNeeded() {
        if (this.mKeyguardShowing && this.mOccluded) {
            ActivityStack stack = this.mStackSupervisor.getDefaultDisplay().getSplitScreenPrimaryStack();
            if (stack != null) {
                this.mStackSupervisor.moveTasksToFullscreenStackLocked(stack, this.mStackSupervisor.mFocusedStack == stack);
            }
        }
    }

    private void updateKeyguardSleepToken() {
        if (this.mSleepToken == null && isKeyguardOrAodShowing(0)) {
            if (ActivityManagerDebugConfig.DEBUG_KEYGUARD) {
                Flog.i(107, "acquireSleepToken");
            }
            this.mSleepToken = this.mService.acquireSleepToken("Keyguard", 0);
        } else if (this.mSleepToken != null && !isKeyguardOrAodShowing(0)) {
            if (ActivityManagerDebugConfig.DEBUG_KEYGUARD) {
                Flog.i(107, "releaseSleepToken");
            }
            this.mSleepToken.release();
            this.mSleepToken = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "KeyguardController:");
        pw.println(prefix + "  mKeyguardShowing=" + this.mKeyguardShowing);
        pw.println(prefix + "  mAodShowing=" + this.mAodShowing);
        pw.println(prefix + "  mKeyguardGoingAway=" + this.mKeyguardGoingAway);
        pw.println(prefix + "  mOccluded=" + this.mOccluded);
        pw.println(prefix + "  mDismissingKeyguardActivity=" + this.mDismissingKeyguardActivity);
        pw.println(prefix + "  mDismissalRequested=" + this.mDismissalRequested);
        pw.println(prefix + "  mVisibilityTransactionDepth=" + this.mVisibilityTransactionDepth);
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1133871366145L, this.mKeyguardShowing);
        proto.write(1133871366146L, this.mOccluded);
        proto.end(token);
    }
}
