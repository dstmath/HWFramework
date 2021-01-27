package huawei.com.android.server.policy.stylus;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityOptionsEx;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.WindowManagerEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.UserInfoEx;
import com.huawei.android.inputmethod.HwInputMethodManager;
import com.huawei.android.internal.util.ScreenshotHelperEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.android.vrsystem.IVRSystemServiceManagerEx;
import com.huawei.hwpartstylusgestureopt.BuildConfig;
import com.huawei.screenrecorder.activities.SurfaceControlEx;
import com.huawei.server.policy.DefaultStylusGestureListener;
import huawei.com.android.server.policy.stylus.HwGestureDetector;
import huawei.com.android.server.policy.stylus.StylusGestureDetector;
import huawei.com.android.server.policy.stylus.glow.HwPointPositionView;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class StylusGestureListener extends DefaultStylusGestureListener implements PointerEventListenerEx, HwGestureDetector.GestureListener, StylusGestureDetector.StylusGestureRecognizeListener {
    private static final String ACTION_KNOCK_DOWN = "com.qeexo.syswideactions.KNOCK_DOWN";
    private static final int DEFAULT_TOOL_TYPE = -1;
    private static final String EXTRA_SCREENSHOT_BITMAP = "com.qeexo.syswideactions.screenshot.bitmap";
    private static final String EXTRA_SCREENSHOT_PACKAGENAME = "com.qeexo.syswideactions.screenshot.packagename";
    private static final String NOTCH_PROP = SystemPropertiesEx.get("ro.config.hw_notch_size", BuildConfig.FLAVOR);
    private static final int PERMILLAGE = 1000;
    private static final String STYLUS_GESTURES_PATH = "/hw_product/bin/knuckle_gestures.bin";
    private static final int STYLUS_USAGED_DURATION_TIMEOUT = 60000;
    private static final int STYLUS_USAGE_TIME_THRESHOLD = 300000;
    private static final String TAG = "StylusGestureListener";
    private static final String WRITE_IME_ID = "com.visionobjects.stylusmobile.v3_2_huawei/com.visionobjects.stylusmobile.v3_2.StylusIMService";
    private ActivityManager mActivityManager;
    private final Context mContext;
    private int mDisplayHeight;
    private int mDisplayWidth;
    private long mFingerTime = 0;
    private final HwGestureDetector mGestureDetector;
    private Handler mHandler;
    private boolean mHasKnockDownOccured;
    private boolean mHasNotchInScreen = false;
    private boolean mIsCanceledTiming = false;
    private boolean mIsFirstTime = true;
    private boolean mIsForPcModeOnly = false;
    private boolean mIsInitialPenButtonPressed = false;
    private boolean mIsPenButtonPressed = false;
    private boolean mIsStylusViewAdded = false;
    private KeyguardManager mKeyguardManager;
    private long mLastFingerUsageTime = 0;
    private long mLastStylusUsageTime = 0;
    private int mLastToolType = DEFAULT_TOOL_TYPE;
    private WindowManager.LayoutParams mLayoutParams;
    private String mPkg;
    private Bitmap mScreenshotBitmap;
    private ScreenshotHelperEx mScreenshotHelper;
    private final StylusGestureDetector mStylusGestureDetector;
    private StylusGestureManager mStylusGestureManager;
    private HwPointPositionView mStylusGestureView;
    private long mStylusTime = 0;
    private final Object mViewLock = new Object();
    private WindowManager mWindowManager;
    private final MotionEventRunnable onKnockDownRunnable = new MotionEventRunnable() {
        /* class huawei.com.android.server.policy.stylus.StylusGestureListener.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            StylusGestureListener.this.notifyKnockDown(this.event);
        }
    };
    private final MotionEventRunnable usageDurationTimeout = new MotionEventRunnable() {
        /* class huawei.com.android.server.policy.stylus.StylusGestureListener.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            Log.i(StylusGestureListener.TAG, "handle Uasage Duration Timeout");
            StylusGestureListener.this.reportStylusUasageDurationTimeout();
        }
    };

    public StylusGestureListener(Context context) {
        super(context);
        this.mContext = context;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        this.mStylusGestureDetector = new StylusGestureDetector(this.mContext, this);
        this.mHasKnockDownOccured = false;
        this.mHandler = new Handler();
        updateConfiguration();
        this.mGestureDetector = new HwGestureDetector(this.mContext, STYLUS_GESTURES_PATH);
        this.mGestureDetector.setGestureListener(this);
        this.mScreenshotHelper = new ScreenshotHelperEx(context);
        this.mStylusGestureManager = new StylusGestureManager(context);
        this.mHasNotchInScreen = !TextUtils.isEmpty(NOTCH_PROP);
    }

    public void onRotationChange(int newRotation) {
        cancelDoubleKnockIfNeeded("onRotate to " + newRotation);
    }

    @Override // huawei.com.android.server.policy.stylus.StylusGestureDetector.StylusGestureRecognizeListener
    public boolean isPenButtonPressed() {
        return this.mIsPenButtonPressed;
    }

    public void cancelStylusGesture() {
        MotionEvent cancelEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 3, 0.0f, 0.0f, 0);
        HwPointPositionView hwPointPositionView = this.mStylusGestureView;
        if (hwPointPositionView != null) {
            hwPointPositionView.onTouchEvent(cancelEvent);
        }
        this.mGestureDetector.onTouchEvent(cancelEvent);
        removeStylusGestureView();
        this.mHasKnockDownOccured = false;
        cancelEvent.recycle();
    }

    private void recycleScreenshot() {
        Bitmap bitmap = this.mScreenshotBitmap;
        if (bitmap != null) {
            bitmap.recycle();
            this.mScreenshotBitmap = null;
        }
    }

    private void saveScreenBitmap() {
        recycleScreenshot();
        this.mScreenshotBitmap = SurfaceControlEx.screenshotHW(new Rect(0, 0, this.mDisplayWidth, this.mDisplayHeight), this.mDisplayWidth, this.mDisplayHeight, 0);
    }

    private void startActivityOneStep(Context context, Intent intent) {
        if (context == null || intent == null) {
            Log.w(TAG, "startActivityOneStep : context is null, return false");
            return;
        }
        try {
            ActivityOptionsEx options = ActivityOptionsEx.makeBasic();
            options.setLaunchWindowingMode(102);
            intent.addFlags(268435456);
            ContextEx.startActivityAsUser(this.mContext, intent, options.toBundle(), UserHandleEx.getCurrentOrSelfUserHandle());
            Log.i(TAG, "startActivityOneStep success");
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "startActivityOneStep notepad activity failed!");
        }
    }

    private KeyguardManager getKeyguardManager() {
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        }
        return this.mKeyguardManager;
    }

    private void dismissKeyguardIfCurrentlyShown() {
        KeyguardManager keyguardManager = getKeyguardManager();
        if (keyguardManager == null) {
            Log.e(TAG, "keyguardManager is null");
        } else if (keyguardManager.inKeyguardRestrictedInputMode()) {
            Log.d(TAG, "phoneWindowManager.dismissKeyguardLw()");
            WindowManagerEx.dismissKeyguardLw();
        }
    }

    public final void updateConfiguration() {
        Display display = this.mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mDisplayWidth = size.x > size.y ? size.y : size.x;
        this.mDisplayHeight = size.x > size.y ? size.x : size.y;
    }

    public void setToolType() {
        StylusGestureDetector stylusGestureDetector = this.mStylusGestureDetector;
        if (stylusGestureDetector != null) {
            stylusGestureDetector.setToolType();
        }
    }

    @Override // huawei.com.android.server.policy.stylus.PointerEventListenerEx
    public void onPointerEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            Log.e(TAG, "onPointerEvent MotionEvent is null");
            return;
        }
        showStylusIntroducePage(motionEvent);
        if (Constants.IS_TABLET || this.mStylusGestureManager.isStylusEnabled()) {
            this.mStylusGestureDetector.shouldSwtichInputMethod(motionEvent);
            reportStylusUsageDuration(motionEvent.getAction(), motionEvent.getToolType(0));
            reportStylusUsageCount(motionEvent);
            if (!this.mIsForPcModeOnly) {
                if (motionEvent.getAction() == 0) {
                    Log.i(TAG, "stylusMotionEvent ACTION_DOWN " + motionEvent);
                }
                if (isValidStylusMotionEvent(motionEvent)) {
                    if (motionEvent.getAction() == 0) {
                        MotionEventRunnable motionEventRunnable = this.onKnockDownRunnable;
                        motionEventRunnable.event = motionEvent;
                        this.mHandler.post(motionEventRunnable);
                        notifyKnockDown(motionEvent);
                        this.mHasKnockDownOccured = true;
                        saveScreenBitmap();
                        addStylusGestureView();
                        Log.d(TAG, "stylus actionDown occured");
                    }
                    this.mStylusGestureDetector.onTouchEvent(motionEvent);
                    this.mGestureDetector.onTouchEvent(motionEvent);
                    HwPointPositionView hwPointPositionView = this.mStylusGestureView;
                    if (hwPointPositionView != null) {
                        hwPointPositionView.onTouchEvent(motionEvent);
                    }
                    if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                        Log.d(TAG, "stylus actionUp occured");
                        cancelStylusGesture();
                    }
                } else if (this.mHasKnockDownOccured) {
                    cancelStylusGesture();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static abstract class MotionEventRunnable implements Runnable {
        MotionEvent event;

        private MotionEventRunnable() {
            this.event = null;
        }
    }

    private void cancelUasageDurationTimeout() {
        this.mHandler.removeCallbacks(this.usageDurationTimeout);
        this.mIsCanceledTiming = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportStylusUasageDurationTimeout() {
        reportStylusUsageDuration(0, 1);
    }

    private void reportStylusUsageDuration(int action, int currentToolType) {
        if (action == 1 || (action == 3 && currentToolType == 2)) {
            cancelUasageDurationTimeout();
            this.mIsCanceledTiming = false;
            this.mHandler.postDelayed(this.usageDurationTimeout, 60000);
        }
        if (!this.mIsCanceledTiming && action == 2 && currentToolType == 2) {
            cancelUasageDurationTimeout();
        }
        if (action != 0) {
            return;
        }
        if (currentToolType == 2) {
            this.mPkg = getTopActivity();
            if (this.mLastToolType != currentToolType) {
                this.mStylusTime = SystemClock.uptimeMillis();
                this.mLastToolType = currentToolType;
                return;
            }
            return;
        }
        int i = this.mLastToolType;
        if (currentToolType != i && i > 0) {
            cancelUasageDurationTimeout();
            this.mLastToolType = currentToolType;
            this.mFingerTime = SystemClock.uptimeMillis();
            String currentPkgName = getTopActivity();
            if (currentPkgName != null) {
                try {
                    String[] results = currentPkgName.split("\\/");
                    if (results.length > 0 && this.mPkg != null) {
                        if (this.mPkg.contains(results[0])) {
                            int duration = Integer.parseInt(String.valueOf((this.mFingerTime - this.mStylusTime) / 1000));
                            Flog.bdReport(991310950, "{package:" + results[0] + ",duration:" + duration + "}");
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "stylus reportStylusUsageDuration NumberFormatException");
                } catch (Exception e2) {
                    Log.e(TAG, "stylus reportStylusUsageDuration");
                }
            }
        }
    }

    private ComponentName getComponentName(ActivityInfo activityInfo) {
        try {
            Class classActivityManagerEx = Class.forName("android.content.pm.ActivityInfoEx");
            Object obj = classActivityManagerEx.getMethod("getComponentName", ActivityInfo.class).invoke(classActivityManagerEx, activityInfo);
            if (obj instanceof ComponentName) {
                Log.i(TAG, "getComponentName success");
                return (ComponentName) obj;
            }
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.w(TAG, "getComponentName: invoke exception " + e.getClass().getName());
        }
        Log.i(TAG, "getComponentName error");
        return null;
    }

    private String getTopActivity() {
        ActivityInfo activityInfo = HwActivityTaskManager.getLastResumedActivity();
        if (activityInfo == null) {
            return null;
        }
        ComponentName componentName = getComponentName(activityInfo);
        if (componentName != null) {
            return componentName.flattenToShortString();
        }
        Log.e(TAG, "componentName is null.");
        return null;
    }

    private void reportStylusUsageCount(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            if (motionEvent.getToolType(0) != 2) {
                this.mLastFingerUsageTime = SystemClock.uptimeMillis();
            } else if (this.mIsFirstTime) {
                Flog.bdReport(991310951);
                this.mIsFirstTime = false;
            } else {
                long stylusUsageTime = SystemClock.uptimeMillis();
                if (this.mLastStylusUsageTime == 0) {
                    this.mLastStylusUsageTime = stylusUsageTime;
                }
                long j = this.mLastFingerUsageTime;
                if (j > 0 && j < stylusUsageTime && stylusUsageTime - this.mLastStylusUsageTime >= 300000) {
                    this.mLastStylusUsageTime = 0;
                    this.mLastFingerUsageTime = 0;
                    Flog.bdReport(991310951);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyKnockDown(MotionEvent event) {
        Intent intent = new Intent(ACTION_KNOCK_DOWN);
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
    }

    private boolean isValidStylusMotionEvent(MotionEvent motionEvent) {
        if (motionEvent.getToolType(0) != 2 || !WindowManagerEx.isWindowSupportKnuckle()) {
            return false;
        }
        boolean isButtonPressed = motionEvent.getButtonState() == 32;
        if (motionEvent.getAction() == 0) {
            this.mIsInitialPenButtonPressed = this.mIsPenButtonPressed;
        }
        if (isButtonPressed || this.mIsInitialPenButtonPressed) {
            return true;
        }
        return false;
    }

    @Override // huawei.com.android.server.policy.stylus.HwGestureDetector.GestureListener
    public void onRegionGesture(String gestureName, Gesture gesture, double predictionScore) {
        if (gestureName == null || gesture == null) {
            Log.e(TAG, "onRegionGesture param error");
            return;
        }
        Log.i(TAG, "onRegionGesture, gestureName = " + gestureName + " predictionScore = " + predictionScore);
        Intent intent = StylusGestureSettings.getIntentForStylusGesture(gestureName, gesture, predictionScore, this.mContext);
        if (intent == null) {
            Log.d(TAG, "Ignoring " + gestureName + " gesture.");
        } else if (this.mScreenshotBitmap == null) {
            Log.e(TAG, "ScreenshotBitmap is null, failed to take Screenshot.");
        } else {
            String packageName = getCurrentPackageName(this.mContext);
            intent.addFlags(32768);
            intent.putExtra(EXTRA_SCREENSHOT_BITMAP, this.mScreenshotBitmap.copy(Bitmap.Config.ARGB_8888, false));
            intent.putExtra(EXTRA_SCREENSHOT_PACKAGENAME, packageName);
            recycleScreenshot();
            dismissKeyguardIfCurrentlyShown();
            if (StylusGestureSettings.checkPackageInstalled(this.mContext, Constants.PKG_QEEXO_SMARTSHOT)) {
                intent.setPackage(Constants.PKG_QEEXO_SMARTSHOT);
            } else if (StylusGestureSettings.checkPackageInstalled(this.mContext, Constants.PKG_HUAWEI_SMARTSHOT)) {
                intent.setPackage(Constants.PKG_HUAWEI_SMARTSHOT);
            } else {
                Log.w(TAG, "both qeexo and huawei smartshot not exists");
                return;
            }
            try {
                ContextEx.startActivityAsUser(this.mContext, intent, (Bundle) null, UserHandleEx.getCurrentOrSelfUserHandle());
                Log.i(TAG, "Start screenshot regionCrop activity");
                Flog.bdReport(991310162);
                Flog.bdReport(991310952, "issuccess", "true");
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Failed to start activity: ");
            }
        }
    }

    @Override // huawei.com.android.server.policy.stylus.HwGestureDetector.GestureListener
    public void onLetterGesture(String gestureName, Gesture gesture, double predictionScore) {
        if (gestureName == null || gesture == null) {
            Log.e(TAG, "onLetterGesture param error");
            return;
        }
        Log.i(TAG, "onLetterGesture, gestureName = " + gestureName);
        Intent intent = StylusGestureSettings.getIntentForStylusGesture(gestureName, gesture, predictionScore, this.mContext);
        if (intent == null) {
            Log.d(TAG, "Ignoring " + gestureName + " gesture.");
            return;
        }
        try {
            List<ActivityManager.RecentTaskInfo> recentTask = ActivityManagerEx.getRecentTasksForUser(this.mActivityManager, 1, 1, ActivityManagerEx.getCurrentUser());
            if (recentTask != null && recentTask.size() > 0) {
                UserInfoEx user = UserManagerEx.getUserInfoEx((UserManager) this.mContext.getSystemService("user"), (int) ActivityManagerEx.getUserId(recentTask.get(0)));
                if (user == null || !user.isManagedProfile()) {
                    ContextEx.startServiceAsUser(this.mContext, intent, UserHandleEx.getUserHandle(ActivityManagerEx.getCurrentUser()));
                } else {
                    ContextEx.startServiceAsUser(this.mContext, intent, UserHandleEx.getUserHandle(user.getUserInfoId()));
                }
                Flog.bdReport(991310952, "issuccess", "true");
            }
            Log.i(TAG, "Start MultiScreenShotService.");
            Flog.bdReport(991310163);
        } catch (SecurityException e) {
            Log.e(TAG, "Can not find the service with SecurityException");
        } catch (IllegalStateException e2) {
            Log.e(TAG, "Can not find the service with IllegalStateException");
        } catch (Exception e3) {
            Log.e(TAG, "Can not find the service");
        }
    }

    @Override // huawei.com.android.server.policy.stylus.StylusGestureDetector.StylusGestureRecognizeListener
    public void onStylusSingleTapPerformed(MotionEvent event, boolean isShowMenuView) {
        if (event != null && isShowMenuView) {
            Log.i(TAG, "onStylusSingleTapPerformed and wakeupNoteEditor.");
            wakeupNoteEditor();
        }
    }

    @Override // huawei.com.android.server.policy.stylus.StylusGestureDetector.StylusGestureRecognizeListener
    public void onStylusDoubleTapPerformed() {
        HwPointPositionView hwPointPositionView = this.mStylusGestureView;
        if (hwPointPositionView != null && hwPointPositionView.isShown()) {
            removeStylusGestureView();
            Log.d(TAG, "mStylusGestureView isShown(), removing...");
        }
        Log.i(TAG, "onStylusDoubleTapPerformed and takeScreenshot.");
        Flog.bdReport(991310161);
        this.mScreenshotHelper.takeScreenshot(1, true, true, this.mHandler);
        Flog.bdReport(991310952, "issuccess", "true");
    }

    @Override // huawei.com.android.server.policy.stylus.StylusGestureDetector.StylusGestureRecognizeListener
    public void notifySwtichInputMethod(boolean isStylus) {
        String ime = isStylus ? "handwrite IME" : "general IME";
        Log.i(TAG, "notifySwtichInputMethod and swtich current IME to " + ime);
        Flog.bdReport(991310164);
        if (isStylus) {
            try {
                HwInputMethodManager.setDefaultIme(WRITE_IME_ID);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "SwtichInputMethod IllegalArgumentException:" + e.getMessage());
            } catch (Exception e2) {
                Log.e(TAG, "SwtichInputMethod error");
            }
        } else {
            HwInputMethodManager.setDefaultIme(BuildConfig.FLAVOR);
        }
    }

    private String getCurrentPackageName(Context context) {
        try {
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
            if (runningTaskInfos != null) {
                if (!runningTaskInfos.isEmpty()) {
                    ActivityManager.RunningTaskInfo runningTaskInfo = runningTaskInfos.get(0);
                    if (runningTaskInfo == null) {
                        Log.e(TAG, "failed to get runningTaskInfo");
                        return BuildConfig.FLAVOR;
                    }
                    String packageName = runningTaskInfo.topActivity.getPackageName();
                    if (!TextUtils.isEmpty(packageName)) {
                        return packageName;
                    }
                    return BuildConfig.FLAVOR;
                }
            }
            Log.e(TAG, "running task is null");
            return BuildConfig.FLAVOR;
        } catch (SecurityException e) {
            Log.e(TAG, "get current package name error with SecurityException");
            return BuildConfig.FLAVOR;
        } catch (Exception e2) {
            Log.e(TAG, "get current package name error");
            return BuildConfig.FLAVOR;
        }
    }

    private void showStylusIntroducePage(MotionEvent motionEvent) {
        if (!Constants.IS_TABLET && !SystemPropertiesEx.getBoolean("runtime.mmitest.isrunning", false) && motionEvent.getToolType(0) == 2 && motionEvent.getAction() == 0 && !this.mStylusGestureManager.isStylusIntroduced()) {
            Log.d(TAG, "show stylus introduce page!");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(Constants.FLOAT_ENTRANCE_PACKAGE_NAME, "com.huawei.stylus.floatmenu.IntroduceDialogActivity"));
            intent.addFlags(268435456);
            try {
                ContextEx.startActivityAsUser(this.mContext, intent, (Bundle) null, UserHandleEx.getCurrentOrSelfUserHandle());
            } catch (ActivityNotFoundException exp) {
                Log.e(TAG, "start introduce Activity failed! message : " + exp.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "startActivity failed!");
            }
        }
    }

    private void wakeupNoteEditor() {
        if (this.mContext == null) {
            Log.e(TAG, "wakeupNoteEditor mContext is null");
            return;
        }
        Intent notePadEditorIntent = new Intent("android.huawei.intent.action.note.handwriting");
        boolean isLocked = false;
        try {
            KeyguardManager keyguardManager = getKeyguardManager();
            if (keyguardManager != null) {
                isLocked = keyguardManager.isKeyguardLocked();
            }
            Log.i(TAG, "wakeupNoteEditor isLocked = " + isLocked);
            if (isLocked) {
                ContextEx.startActivityAsUser(this.mContext, notePadEditorIntent, (Bundle) null, UserHandleEx.getCurrentOrSelfUserHandle());
            } else {
                startActivityOneStep(this.mContext, notePadEditorIntent);
            }
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "startActivity notepad activity failed! message : " + ex.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "startActivityAsUser(): Exception");
        }
    }

    public void addStylusGestureView() {
        IVRSystemServiceManagerEx ivrSystemServiceManagerEx = IVRSystemServiceManagerEx.create(this.mContext);
        if (ivrSystemServiceManagerEx == null || !ivrSystemServiceManagerEx.isVRMode()) {
            if (this.mStylusGestureView == null) {
                this.mStylusGestureView = new HwPointPositionView(this.mContext, this);
                this.mLayoutParams = new WindowManager.LayoutParams(DEFAULT_TOOL_TYPE, DEFAULT_TOOL_TYPE);
                WindowManager.LayoutParams layoutParams = this.mLayoutParams;
                layoutParams.type = Constants.TYPE_SECURE_SYSTEM_OVERLAY;
                layoutParams.flags = 1304;
                WindowManagerEx.LayoutParamsEx layoutParamsEx = new WindowManagerEx.LayoutParamsEx(layoutParams);
                layoutParamsEx.addPrivateFlags(WindowManagerEx.LayoutParamsEx.getPrivateFlagShowForAllUsers());
                if (ActivityManagerEx.isHighEndGfx()) {
                    this.mLayoutParams.flags |= 16777216;
                    layoutParamsEx.addPrivateFlags(WindowManagerEx.LayoutParamsEx.getPrivateFlagForceHardwareAccelerated());
                }
                WindowManager.LayoutParams layoutParams2 = this.mLayoutParams;
                layoutParams2.format = -3;
                layoutParams2.setTitle("StylusGestureView");
                layoutParamsEx.addInputFeatures(WindowManagerEx.LayoutParamsEx.getInputFetureNoInputChannel());
                if (this.mHasNotchInScreen) {
                    this.mLayoutParams.layoutInDisplayCutoutMode = WindowManagerEx.LayoutParamsEx.getLayoutInDisplayCutoutModeAlways();
                }
            }
            synchronized (this.mViewLock) {
                if (!this.mIsStylusViewAdded) {
                    Log.d(TAG, "addStylusGestureView");
                    this.mWindowManager.addView(this.mStylusGestureView, this.mLayoutParams);
                    this.mIsStylusViewAdded = true;
                }
            }
            return;
        }
        Log.i(TAG, "current is in VR Mode,view cannot added!");
    }

    public void removeStylusGestureView() {
        synchronized (this.mViewLock) {
            if (this.mStylusGestureView != null && this.mIsStylusViewAdded) {
                Log.d(TAG, "removeStylusGestureView");
                this.mWindowManager.removeView(this.mStylusGestureView);
                this.mIsStylusViewAdded = false;
            }
        }
    }

    public void onKeyEvent(int keyCode, boolean isDown) {
        if (keyCode == 718) {
            Log.i(TAG, "KEYCODE_F20 onKeyEvent");
            if (isDown) {
                this.mIsPenButtonPressed = true;
            } else {
                this.mIsPenButtonPressed = false;
            }
        }
    }

    public void onScreenTurnedOff() {
        cancelDoubleKnockIfNeeded("onScreenTurnedOff");
    }

    private void cancelDoubleKnockIfNeeded(String reason) {
        Log.i(TAG, "cancel DoubleKnockAction due to " + reason);
        final MotionEvent cancelEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 3, 0.0f, 0.0f, 0);
        this.mHandler.post(new Runnable() {
            /* class huawei.com.android.server.policy.stylus.StylusGestureListener.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                StylusGestureListener.this.onPointerEvent(cancelEvent);
            }
        });
    }
}
