package com.android.server.gesture;

import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Flog;
import android.util.Log;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.internal.app.AssistUtils;
import com.android.server.LocalServices;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.huawei.android.app.PackageManagerEx;
import java.io.PrintWriter;
import java.util.List;

public class QuickSlideOutController extends QuickStartupStub {
    private static final String ACTION_STATUSBAR_CHANGE = "com.android.systemui.statusbar.visible.change";
    private static final String ASSISTANT_ACTION = "com.huawei.action.VOICE_ASSISTANT";
    private static final String ASSISTANT_ACTIVITY_NAME = "com.huawei.vassistant.ui.main.VAssistantActivity";
    private static final String ASSISTANT_ICON_METADATA_NAME = "com.android.systemui.action_assist_icon";
    private static final String GOOGLE_ASSISTANT_PACKAGE_NAME = "com.google.android.googlequicksearchbox";
    private static final String HW_ASSISTANT_PACKAGE_NAME = "com.huawei.vassistant";
    private static final String KEY_INVOKE = "invoke";
    private static final String KYE_VDRIVE_IS_RUN = "vdrive_is_run_state";
    private static final int MSG_CLOSE_VIEW = 1;
    private static final int MSG_UPDATE_WINDOW_VIEW = 2;
    private static final String SCANNER_APK_NAME = "HiVision";
    private static final String SCANNER_CLASS_NAME = "com.huawei.scanner.view.ScannerActivity";
    private static final String SCANNER_PACKAGE_NAME = "com.huawei.scanner";
    private static final int TYPE_NOT_PREINSTALL = 1;
    private static final int TYPE_PREINSTALL_AND_EXIST = 3;
    private static final int TYPE_PREINSTALL_BUT_UNINSTALLED = 2;
    private static final String VALUE_INVOKE = "gesture_nav";
    private static final int VALUE_VDRIVE_IS_RUN = 1;
    private static final int VALUE_VDRIVE_IS_UNRUN = 0;
    private static ComponentName sVAssistantComponentName = null;
    private AssistUtils mAssistUtils;
    private float mCurrentTouch;
    private boolean mGestureNavReady;
    /* access modifiers changed from: private */
    public Handler mHandler = new MyHandler();
    private boolean mInDriveMode = false;
    private boolean mIsScannerExist = true;
    private boolean mIsScannerPreInstalled = true;
    /* access modifiers changed from: private */
    public boolean mIsStatusBarExplaned = false;
    private final Object mLock = new Object();
    private PackageMonitorReceiver mPackageMonitorReceiver;
    private int mScannerAvailableType = 3;
    private SettingsObserver mSettingsObserver;
    private int mSlideMaxDistance;
    private boolean mSlideOutEnabled;
    private boolean mSlideOverThreshold;
    private float mSlidePhasePos;
    private int mSlideStartThreshold;
    private boolean mSlidingOnLeft = true;
    private boolean mSlowAnimTriggered;
    private float mStartTouch;
    private StatusBarStatesChangedReceiver mStatusBarReceiver;
    private final Runnable mSuccessRunnable = new Runnable() {
        public void run() {
            QuickSlideOutController.this.gestureSuccessAtEnd(false);
        }
    };
    private boolean mThresholdTriggered;
    private SlideOutContainer mViewContainer;
    private WindowManager mWindowManager;
    private boolean mWindowViewSetuped;

    private final class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    QuickSlideOutController.this.hideSlideOutView();
                    return;
                case 2:
                    QuickSlideOutController.this.updateSlideOutWindow();
                    return;
                default:
                    return;
            }
        }
    }

    private final class PackageMonitorReceiver extends BroadcastReceiver {
        private PackageMonitorReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            QuickSlideOutController.this.updatePackageExistState();
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            QuickSlideOutController.this.updateSettings();
        }
    }

    private final class StatusBarStatesChangedReceiver extends BroadcastReceiver {
        private StatusBarStatesChangedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && QuickSlideOutController.ACTION_STATUSBAR_CHANGE.equals(intent.getAction())) {
                String visible = "false";
                if (intent.getExtras() != null) {
                    visible = intent.getExtras().getString("visible");
                }
                if (visible != null) {
                    boolean unused = QuickSlideOutController.this.mIsStatusBarExplaned = Boolean.valueOf(visible).booleanValue();
                }
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_QSO, "mIsStatusBarExplaned:" + QuickSlideOutController.this.mIsStatusBarExplaned);
                }
            }
        }
    }

    private class TouchOutsideListener implements View.OnTouchListener {
        private int mMsg;

        public TouchOutsideListener(int msg) {
            this.mMsg = msg;
        }

        public boolean onTouch(View v, MotionEvent ev) {
            int action = ev.getAction();
            if (action != 4 && action != 0) {
                return false;
            }
            QuickSlideOutController.this.mHandler.removeMessages(this.mMsg);
            QuickSlideOutController.this.mHandler.sendEmptyMessage(this.mMsg);
            return true;
        }
    }

    public QuickSlideOutController(Context context, Looper looper) {
        super(context);
        this.mAssistUtils = new AssistUtils(context);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
    }

    private void notifyStart() {
        this.mGestureNavReady = true;
        this.mStatusBarReceiver = new StatusBarStatesChangedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STATUSBAR_CHANGE);
        this.mContext.registerReceiverAsUser(this.mStatusBarReceiver, UserHandle.ALL, filter, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", this.mHandler);
        this.mPackageMonitorReceiver = new PackageMonitorReceiver();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.PACKAGE_ADDED");
        filter2.addAction("android.intent.action.PACKAGE_REMOVED");
        filter2.addDataScheme("package");
        filter2.addDataSchemeSpecificPart(SCANNER_PACKAGE_NAME, 0);
        this.mContext.registerReceiverAsUser(this.mPackageMonitorReceiver, UserHandle.ALL, filter2, null, this.mHandler);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.registerContentObserver(Settings.Secure.getUriFor(GestureNavConst.KEY_SECURE_GESTURE_NAVIGATION_ASSISTANT), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor(KYE_VDRIVE_IS_RUN), false, this.mSettingsObserver, -1);
        updateSettings();
        updateConfig();
        updatePackageExistState();
    }

    private void notifyStop() {
        if (this.mStatusBarReceiver != null) {
            this.mContext.unregisterReceiver(this.mStatusBarReceiver);
            this.mStatusBarReceiver = null;
        }
        if (this.mPackageMonitorReceiver != null) {
            this.mContext.unregisterReceiver(this.mPackageMonitorReceiver);
            this.mPackageMonitorReceiver = null;
        }
        if (this.mSettingsObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
            this.mSettingsObserver = null;
        }
        this.mGestureNavReady = false;
    }

    private boolean isSuperPowerSaveMode() {
        return GestureUtils.isSuperPowerSaveMode();
    }

    private boolean isInLockTaskMode() {
        return GestureUtils.isInLockTaskMode();
    }

    private boolean isTargetLaunched(boolean onLeft) {
        String focusApp = this.mDeviceStateController.getFocusPackageName();
        if (focusApp == null) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QSO, "onLeft:" + onLeft + ", focusApp:" + focusApp);
        }
        if (!GestureNavConst.CHINA_REGION) {
            return focusApp.equals(GOOGLE_ASSISTANT_PACKAGE_NAME);
        }
        if (onLeft) {
            return focusApp.equals(HW_ASSISTANT_PACKAGE_NAME);
        }
        return focusApp.equals(SCANNER_PACKAGE_NAME);
    }

    /* access modifiers changed from: private */
    public void updateSlideOutWindow() {
        synchronized (this.mLock) {
            if (!this.mSlideOutEnabled || !this.mGestureNavReady) {
                if (this.mWindowViewSetuped) {
                    destroySlideOutView();
                }
            } else if (!this.mWindowViewSetuped) {
                createSlideOutView();
            } else {
                updateSlideOutView();
            }
        }
    }

    public boolean isSlideOutEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mSlideOutEnabled;
        }
        return z;
    }

    public void onNavCreate(GestureNavView navView) {
        super.onNavCreate(navView);
        notifyStart();
        updateSlideOutWindow();
    }

    public void onNavUpdate() {
        super.onNavUpdate();
        updateSlideOutWindow();
    }

    public void onNavDestroy() {
        super.onNavDestroy();
        notifyStop();
        updateSlideOutWindow();
    }

    public void updateConfig() {
        this.mSlideStartThreshold = this.mContext.getResources().getDimensionPixelSize(34472526);
        this.mSlideMaxDistance = this.mContext.getResources().getDimensionPixelSize(34472525);
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QSO, "threshold=" + this.mSlideStartThreshold + ", max=" + this.mSlideMaxDistance);
        }
    }

    public void updateSettings() {
        synchronized (this.mLock) {
            boolean lastSlideOutEnabled = this.mSlideOutEnabled;
            ContentResolver resolver = this.mContext.getContentResolver();
            this.mSlideOutEnabled = GestureNavConst.isSlideOutEnabled(this.mContext, -2);
            boolean z = false;
            if (Settings.Global.getInt(resolver, KYE_VDRIVE_IS_RUN, 0) == 1) {
                z = true;
            }
            this.mInDriveMode = z;
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "enabled=" + this.mSlideOutEnabled + ", driveMode=" + this.mInDriveMode);
            }
            if (this.mSlideOutEnabled != lastSlideOutEnabled) {
                this.mHandler.sendEmptyMessage(2);
            }
        }
    }

    public boolean isPreConditionNotReady(boolean onLeft) {
        if (!isSuperPowerSaveMode() && !this.mIsStatusBarExplaned && !this.mInDriveMode && !isInLockTaskMode() && !this.mDeviceStateController.isKeyguardLocked()) {
            return false;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "StatusBarExplaned:" + this.mIsStatusBarExplaned + ",inDriveMode:" + this.mInDriveMode);
        }
        return true;
    }

    public void setSlidingSide(boolean onLeft) {
        this.mSlidingOnLeft = onLeft;
        if (GestureNavConst.CHINA_REGION && !this.mSlidingOnLeft) {
            this.mScannerAvailableType = getScannerAvailableType();
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "onLeft:" + onLeft + ", scannerType:" + this.mScannerAvailableType);
        }
        if (this.mViewContainer != null) {
            this.mViewContainer.setSlidingSide(onLeft, this.mScannerAvailableType);
        }
    }

    public void handleTouchEvent(MotionEvent event) {
        if (this.mViewContainer != null) {
            switch (event.getActionMasked()) {
                case 0:
                    handleActionDown(event);
                    break;
                case 1:
                case 3:
                    handleActionUp(event);
                    break;
                case 2:
                    handleActionMove(event);
                    break;
            }
        }
    }

    private void handleActionDown(MotionEvent event) {
        super.resetAtDown();
        this.mViewContainer.reset();
        this.mThresholdTriggered = false;
        this.mSlideOverThreshold = false;
        this.mSlowAnimTriggered = false;
        float y = event.getY();
        this.mCurrentTouch = y;
        this.mStartTouch = y;
    }

    private void handleActionMove(MotionEvent event) {
        this.mCurrentTouch = event.getY();
        if (this.mGestureReallyStarted && !this.mViewContainer.isShowing()) {
            showOrb();
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_QSO, "start showOrb");
            }
        }
        if (!this.mSlowAnimTriggered && (!GestureNavConst.USE_ANIM_LEGACY ? this.mGestureSlowProcessStarted : this.mGestureReallyStarted)) {
            this.mSlowAnimTriggered = true;
            notifyAnimStarted();
        }
        if (!this.mThresholdTriggered && this.mViewContainer.isVisible() && (slideOverThreshold(false) || !this.mViewContainer.isAnimationRunning())) {
            this.mThresholdTriggered = true;
            this.mSlidePhasePos = this.mCurrentTouch;
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_QSO, "slide over threshold, slidePos=" + this.mSlidePhasePos);
            }
        }
        if (this.mThresholdTriggered && this.mSlowAnimTriggered) {
            this.mSlideOverThreshold = slideOverThreshold(false);
            this.mViewContainer.setSlideOverThreshold(this.mSlideOverThreshold);
            float offset = this.mSlidePhasePos - this.mCurrentTouch;
            if (offset < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                offset = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            }
            this.mViewContainer.setSlideDistance(offset, MathUtils.constrain(offset, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, (float) this.mSlideMaxDistance) / ((float) this.mSlideMaxDistance));
        }
    }

    private void handleActionUp(MotionEvent event) {
        int reportType;
        this.mCurrentTouch = event.getY();
        this.mSlideOverThreshold = slideOverThreshold(true);
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "slideOver=" + this.mSlideOverThreshold + ", valid=" + this.mIsValidGuesture + ", fast=" + this.mIsFastSlideGesture);
        }
        if (this.mSlidingOnLeft) {
            reportType = 850;
        } else {
            reportType = 851;
        }
        if (!this.mIsValidGuesture || !this.mSlideOverThreshold) {
            this.mViewContainer.startExitAnimation(false, false);
            Flog.bdReport(this.mContext, reportType, GestureNavConst.reportResultStr(false, this.mGestureFailedReason));
            return;
        }
        performSlideEndAction(this.mIsFastSlideGesture);
        Flog.bdReport(this.mContext, reportType, GestureNavConst.reportResultStr(true, -1));
    }

    private boolean slideOverThreshold(boolean checkAtEnd) {
        boolean z = true;
        if (!GestureNavConst.USE_ANIM_LEGACY && !checkAtEnd) {
            return true;
        }
        if (Math.abs(this.mStartTouch - this.mCurrentTouch) <= ((float) this.mSlideStartThreshold)) {
            z = false;
        }
        return z;
    }

    private void performSlideEndAction(boolean isFastSlide) {
        if (this.mViewContainer.isAnimationRunning()) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "preform action until anim finished");
            }
            this.mViewContainer.performOnAnimationFinished(this.mSuccessRunnable);
            return;
        }
        gestureSuccessAtEnd(isFastSlide);
    }

    /* access modifiers changed from: private */
    public void gestureSuccessAtEnd(boolean isFastSlide) {
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "execute animation and start target, isFastSlide:" + isFastSlide);
        }
        this.mViewContainer.startExitAnimation(isFastSlide, true);
        startTarget();
    }

    private void startTarget() {
        if (!GestureNavConst.CHINA_REGION) {
            startVoiceAssist();
        } else if (this.mSlidingOnLeft) {
            startVoiceAssist();
        } else if (this.mScannerAvailableType == 3) {
            startScanner();
        } else if (this.mScannerAvailableType == 1) {
            startVoiceAssist();
        } else {
            showNotAvailableToast();
        }
    }

    private void startVoiceAssist() {
        try {
            StatusBarManagerInternal statusBarManager = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
            if (statusBarManager != null) {
                Log.i(GestureNavConst.TAG_GESTURE_QSO, "startAssist");
                Bundle bundle = new Bundle();
                bundle.putBoolean("isFromGesture", true);
                statusBarManager.startAssist(bundle);
            }
        } catch (Exception exp) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "startVoiceAssist error:" + exp.getMessage());
        }
    }

    private ComponentName getAssistInfo() {
        if (this.mAssistUtils != null) {
            return this.mAssistUtils.getAssistComponentForUser(-2);
        }
        return null;
    }

    private boolean hasAssist() {
        if (GestureNavConst.CHINA_REGION) {
            return checkPackageExist(HW_ASSISTANT_PACKAGE_NAME, ActivityManager.getCurrentUser());
        }
        if (getAssistInfo() != null) {
            return true;
        }
        Log.w(GestureNavConst.TAG_GESTURE_QSO, "assist is null, return");
        return false;
    }

    private ComponentName getVoiceInteractorComponentName() {
        if (this.mAssistUtils != null) {
            return this.mAssistUtils.getActiveServiceComponentName();
        }
        return null;
    }

    private void createSlideOutView() {
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QSO, "createSlideOutView");
        }
        this.mViewContainer = (SlideOutContainer) LayoutInflater.from(this.mContext).inflate(34013299, null);
        this.mViewContainer.setOnTouchListener(new TouchOutsideListener(1));
        this.mViewContainer.setVisibility(8);
        GestureUtils.addWindowView(this.mWindowManager, this.mViewContainer, getSlideOutLayoutParams());
        this.mWindowViewSetuped = true;
    }

    private void updateSlideOutView() {
        GestureUtils.updateViewLayout(this.mWindowManager, this.mViewContainer, getSlideOutLayoutParams());
    }

    private void destroySlideOutView() {
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QSO, "destroySlideOutView");
        }
        this.mWindowViewSetuped = false;
        GestureUtils.removeWindowView(this.mWindowManager, this.mViewContainer, true);
        this.mViewContainer = null;
    }

    /* access modifiers changed from: private */
    public void hideSlideOutView() {
        try {
            if (this.mViewContainer != null) {
                this.mViewContainer.hide(true);
            }
        } catch (Exception exp) {
            Log.e(GestureNavConst.TAG_GESTURE_QSO, "hideSearchPanelView" + exp.getMessage());
        }
    }

    private WindowManager.LayoutParams getSlideOutLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(HwArbitrationDEFS.MSG_VPN_STATE_OPEN, 8519936, -3);
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= 16777216;
        }
        lp.gravity = 8388691;
        lp.width = -1;
        lp.height = -1;
        lp.windowAnimations = 16974590;
        lp.softInputMode = 49;
        lp.setTitle("GestureSildeOut");
        return lp;
    }

    private void showOrb() {
        maybeSwapSearchIcon();
        this.mViewContainer.show(true);
    }

    private void notifyAnimStarted() {
        this.mViewContainer.startEnterAnimation();
    }

    private void maybeSwapSearchIcon() {
        if (GestureNavConst.USE_ANIM_LEGACY) {
            ImageView logo = this.mViewContainer.getMaybeSwapLogo();
            if (logo != null) {
                if (!GestureNavConst.CHINA_REGION) {
                    logo.setImageResource(33752014);
                } else if (!this.mSlidingOnLeft) {
                    logo.setImageResource(33752013);
                } else if (hasAssist()) {
                    replaceDrawable(logo, getVAssistantComponentName(), ASSISTANT_ICON_METADATA_NAME);
                } else {
                    Intent intent = ((SearchManager) this.mContext.getSystemService("search")).getAssistIntent(false);
                    if (intent != null) {
                        replaceDrawable(logo, intent.getComponent(), ASSISTANT_ICON_METADATA_NAME);
                    } else {
                        logo.setImageDrawable(null);
                    }
                }
            }
        }
    }

    private void replaceDrawable(ImageView imageView, ComponentName component, String name) {
        replaceDrawable(imageView, component, name, false);
    }

    private void replaceDrawable(ImageView imageView, ComponentName component, String name, boolean isService) {
        Bundle metaData;
        Resources res;
        if (component != null) {
            try {
                PackageManager packageManager = this.mContext.getPackageManager();
                if (isService) {
                    metaData = packageManager.getServiceInfo(component, 128).metaData;
                } else {
                    metaData = packageManager.getActivityInfo(component, 128).metaData;
                }
                if (metaData != null) {
                    int iconResId = metaData.getInt(name);
                    if (iconResId != 0) {
                        if (isService) {
                            res = packageManager.getResourcesForApplication(component.getPackageName());
                        } else {
                            res = packageManager.getResourcesForActivity(component);
                        }
                        imageView.setImageDrawable(res.getDrawable(iconResId));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(GestureNavConst.TAG_GESTURE_QSO, "Failed to swap drawable; " + component.flattenToShortString() + " not found", e);
            } catch (Resources.NotFoundException nfe) {
                Log.w(GestureNavConst.TAG_GESTURE_QSO, "Failed to swap drawable from " + component.flattenToShortString(), nfe);
            }
        }
    }

    private ComponentName getVAssistantComponentName() {
        if (sVAssistantComponentName != null) {
            return sVAssistantComponentName;
        }
        if (GestureNavConst.CHINA_REGION) {
            sVAssistantComponentName = new ComponentName(HW_ASSISTANT_PACKAGE_NAME, ASSISTANT_ACTIVITY_NAME);
            PackageManager packageManager = this.mContext.getPackageManager();
            if (packageManager == null) {
                Log.w(GestureNavConst.TAG_GESTURE_QSO, "packageManager is null");
                return sVAssistantComponentName;
            }
            List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(new Intent(ASSISTANT_ACTION), 65536);
            if (resolveInfos == null || resolveInfos.size() == 0) {
                Log.w(GestureNavConst.TAG_GESTURE_QSO, "resolveInfos is null");
                return sVAssistantComponentName;
            }
            ComponentInfo info = resolveInfos.get(0).activityInfo;
            if (info == null) {
                Log.w(GestureNavConst.TAG_GESTURE_QSO, "activityInfo is null");
                return sVAssistantComponentName;
            }
            sVAssistantComponentName = new ComponentName(info.packageName, info.name);
        } else {
            sVAssistantComponentName = getAssistInfo();
        }
        return sVAssistantComponentName;
    }

    private boolean startScanner() {
        if (this.mContext == null) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "context error");
            return false;
        }
        Intent aiIntent = new Intent();
        aiIntent.setFlags(268435456);
        aiIntent.setFlags(65536);
        aiIntent.setClassName(SCANNER_PACKAGE_NAME, SCANNER_CLASS_NAME);
        aiIntent.putExtra(KEY_INVOKE, VALUE_INVOKE);
        try {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "start scanner");
            this.mContext.startActivityAsUser(aiIntent, UserHandle.CURRENT);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void updatePackageExistState() {
        boolean isAppExistInMainUser;
        int currentUserId = ActivityManager.getCurrentUser();
        this.mIsScannerExist = checkPackageExist(SCANNER_PACKAGE_NAME, currentUserId);
        if (!this.mIsScannerExist) {
            int i = 0;
            this.mIsScannerPreInstalled = false;
            if (currentUserId != 0) {
                isAppExistInMainUser = checkPackageExist(SCANNER_PACKAGE_NAME, 0);
            } else {
                isAppExistInMainUser = false;
            }
            if (isAppExistInMainUser) {
                this.mIsScannerPreInstalled = true;
            } else {
                List<String> list = PackageManagerEx.getScanInstallList();
                if (list != null) {
                    int size = list.size();
                    while (true) {
                        if (i >= size) {
                            break;
                        }
                        String apkPatch = list.get(i);
                        if (apkPatch != null && apkPatch.contains(SCANNER_APK_NAME)) {
                            this.mIsScannerPreInstalled = true;
                            break;
                        }
                        i++;
                    }
                }
            }
        } else {
            this.mIsScannerPreInstalled = true;
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_QSO, "scannerExist=" + this.mIsScannerExist + ", scannerPreInstalled=" + this.mIsScannerPreInstalled);
        }
    }

    private boolean checkPackageExist(String packageName, int userId) {
        try {
            this.mContext.getPackageManager().getPackageInfoAsUser(packageName, 128, userId);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(GestureNavConst.TAG_GESTURE_QSO, packageName + " not found for userId:" + userId);
            return false;
        } catch (Exception e2) {
            Log.w(GestureNavConst.TAG_GESTURE_QSO, packageName + " not available for userId:" + userId);
            return false;
        }
    }

    private int getScannerAvailableType() {
        if (this.mIsScannerExist) {
            return 3;
        }
        if (this.mIsScannerPreInstalled) {
            return 2;
        }
        return 1;
    }

    private void showNotAvailableToast() {
        if (GestureNavConst.DEBUG) {
            Log.d(GestureNavConst.TAG_GESTURE_QSO, "showNotAvailableToast");
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(QuickSlideOutController.this.mContext, 33686242, 0);
                toast.getWindowParams().type = HwArbitrationDEFS.MSG_MPLINK_UNBIND_FAIL;
                toast.getWindowParams().privateFlags |= 16;
                toast.show();
            }
        });
    }

    public void dump(String prefix, PrintWriter pw, String[] args) {
        pw.print(prefix);
        pw.print("isSidleOutEnabled=" + this.mSlideOutEnabled);
        pw.print(" inDriveMode=" + this.mInDriveMode);
        pw.print(" statusBarExplaned=" + this.mIsStatusBarExplaned);
        pw.print(" isSuperPowerSaveMode=" + isSuperPowerSaveMode());
        pw.println();
    }
}
