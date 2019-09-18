package huawei.com.android.server.policy.stylus;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.KeyguardManager;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.WindowManagerPolicyConstants;
import android.widget.Toast;
import com.android.internal.util.ScreenshotHelper;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.mplink.HwMpLinkWifiImpl;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.inputmethod.HwInputMethodManager;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector;
import huawei.com.android.server.policy.fingersense.pixiedust.PointerLocationView;
import huawei.com.android.server.policy.stylus.StylusGestureDetector;
import java.util.List;
import java.util.ServiceConfigurationError;

public class StylusGestureListener implements WindowManagerPolicyConstants.PointerEventListener, CustomGestureDetector.OnGesturePerformedListener, StylusGestureDetector.StylusGestureRecognizeListener {
    private static final String ACTION_KNOCK_DOWN = "com.qeexo.syswideactions.KNOCK_DOWN";
    private static final String EXTRA_SCREENSHOT_BITMAP = "com.qeexo.syswideactions.screenshot.bitmap";
    private static final CustomGestureDetector.OrientationFix[] ORIENTATION_FIXES = {new CustomGestureDetector.OrientationFix(StylusGestureSettings.STYLUS_GESTURE_C_SUFFIX, StylusGestureSettings.STYLUS_GESTURE_W_SUFFIX, null), new CustomGestureDetector.OrientationFix(StylusGestureSettings.STYLUS_GESTURE_C_SUFFIX, StylusGestureSettings.STYLUS_GESTURE_M_SUFFIX, null)};
    private static final String STYLUS_GESTURES_PATH = "/product/bin/stylus_gestures.bin";
    private static final String TAG = "StylusGestureListener";
    private static final String WRITE_IME_ID = "com.visionobjects.stylusmobile.v3_2_huawei/com.visionobjects.stylusmobile.v3_2.StylusIMService";
    private static final String mNotchProp = SystemProperties.get("ro.config.hw_notch_size", "");
    private int STYLUS_USAGE_TIME_THRESHOLD = 300000;
    private boolean forPCModeOnly = false;
    private boolean hasKnockDownOccured;
    private boolean isActionOnKeyboard = false;
    private boolean isStylusViewAdded = false;
    private final Context mContext;
    private final CustomGestureDetector mCustomGestureDetector;
    private int mDisplayHeight;
    private int mDisplayWidth;
    private long mFingerTime = 0;
    private boolean mFirstTime = true;
    private Handler mHandler;
    private boolean mHasNotchInScreen = false;
    private KeyguardManager mKeyguardManager;
    private long mLastFingerUsageTime = 0;
    private long mLastStylusUsageTime = 0;
    private int mLastToolType = -1;
    private WindowManager.LayoutParams mLayoutParams;
    private final HwPhoneWindowManager mPhoneWindowManager;
    private String mPkg;
    private Bitmap mScreenshotBitmap;
    private ScreenshotHelper mScreenshotHelper;
    private final StylusGestureDetector mStylusGestureDetector;
    private StylusGestureManager mStylusGestureManager;
    private PointerLocationView mStylusGestureView;
    private long mStylusTime = 0;
    private WindowManager mWindowManager;
    private WindowManagerInternal mWindowManagerInternal;
    private final MotionEventRunnable onKnockDownRunnable = new MotionEventRunnable() {
        public void run() {
            StylusGestureListener.this.notifyKnockDown(this.event);
        }
    };
    private Object viewLock = new Object();

    private static abstract class MotionEventRunnable implements Runnable {
        MotionEvent event;

        private MotionEventRunnable() {
            this.event = null;
        }
    }

    public void setForPCModeOnly(boolean only4PCMode) {
        this.forPCModeOnly = only4PCMode;
    }

    public StylusGestureListener(Context context, HwPhoneWindowManager phoneWindowManager) {
        this.mContext = context;
        this.mPhoneWindowManager = phoneWindowManager;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mStylusGestureDetector = new StylusGestureDetector(this.mContext, this);
        this.hasKnockDownOccured = false;
        this.mHandler = new Handler();
        updateConfiguration();
        this.mCustomGestureDetector = new CustomGestureDetector(this.mContext, STYLUS_GESTURES_PATH, (CustomGestureDetector.OnGesturePerformedListener) this);
        this.mCustomGestureDetector.setMinPredictionScore(2.0f);
        this.mCustomGestureDetector.setOrientationFixes(ORIENTATION_FIXES);
        this.mCustomGestureDetector.setMinLineGestureStrokeLength(this.mDisplayWidth / 2);
        this.mCustomGestureDetector.setLineGestureStrokePortraitAngle(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        this.mCustomGestureDetector.setLineGestureStrokeLandscapeAngle(90.0f);
        this.mCustomGestureDetector.setMaxLineGestureStrokeAngleDeviation(10.0f);
        this.mCustomGestureDetector.setLineGestureStrokeStraightness(4.0f);
        this.mCustomGestureDetector.setRegionDiagonalThreshold(0.5f);
        this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mScreenshotHelper = new ScreenshotHelper(context);
        this.mStylusGestureManager = new StylusGestureManager(context);
        this.mHasNotchInScreen = !TextUtils.isEmpty(mNotchProp);
    }

    public void cancelStylusGesture() {
        MotionEvent cancelEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 3, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0);
        if (this.mStylusGestureView != null) {
            this.mStylusGestureView.onTouchEvent(cancelEvent);
        }
        this.mCustomGestureDetector.onTouchEvent(cancelEvent);
        removeStylusGestureView();
        this.hasKnockDownOccured = false;
        cancelEvent.recycle();
    }

    private void recycleScreenshot() {
        if (this.mScreenshotBitmap != null) {
            this.mScreenshotBitmap.recycle();
            this.mScreenshotBitmap = null;
        }
    }

    private void saveScreenBitmap() {
        recycleScreenshot();
        this.mScreenshotBitmap = SurfaceControl.screenshot_ext_hw(new Rect(0, 0, this.mDisplayWidth, this.mDisplayHeight), this.mDisplayWidth, this.mDisplayHeight, 0);
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
            Log.w(TAG, "keyguardManager is null");
            return;
        }
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            Log.d(TAG, "phoneWindowManager.dismissKeyguardLw()");
            this.mPhoneWindowManager.dismissKeyguardLw(null, null);
        }
    }

    public void updateConfiguration() {
        Display display = this.mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mDisplayWidth = size.x > size.y ? size.y : size.x;
        this.mDisplayHeight = size.x > size.y ? size.x : size.y;
    }

    public void setToolType() {
        if (this.mStylusGestureDetector != null) {
            this.mStylusGestureDetector.setToolType();
        }
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        showStylusIntroducePage(motionEvent);
        if (StylusGestureDetector.IS_TABLET || this.mStylusGestureManager.isStylusEnabled()) {
            this.mStylusGestureDetector.shouldSwtichInputMethod(motionEvent);
            reportStylusUsageDuration(motionEvent);
            reportStylusUsageCount(motionEvent);
            if (!this.forPCModeOnly) {
                if (motionEvent.getAction() == 0) {
                    Log.i(TAG, "stylusMotionEvent ACTION_DOWN " + motionEvent);
                }
                if (isValidStylusMotionEvent(motionEvent)) {
                    if (motionEvent.getAction() == 0) {
                        this.onKnockDownRunnable.event = motionEvent;
                        this.mHandler.post(this.onKnockDownRunnable);
                        notifyKnockDown(motionEvent);
                        this.hasKnockDownOccured = true;
                        saveScreenBitmap();
                        addStylusGestureView();
                        Log.d(TAG, "stylus actionDown occured");
                    }
                    this.mStylusGestureDetector.onTouchEvent(motionEvent);
                    this.mCustomGestureDetector.onTouchEvent(motionEvent);
                    if (this.mStylusGestureView != null) {
                        this.mStylusGestureView.onTouchEvent(motionEvent);
                    }
                    if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                        Log.d(TAG, "stylus actionUp occured");
                        cancelStylusGesture();
                    }
                } else if (this.hasKnockDownOccured) {
                    cancelStylusGesture();
                }
            }
        }
    }

    private void reportStylusUsageDuration(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            int currentToolType = motionEvent.getToolType(0);
            if (currentToolType == 2) {
                this.mPkg = getTopActivity();
                if (this.mLastToolType != currentToolType) {
                    this.mStylusTime = SystemClock.uptimeMillis();
                    this.mLastToolType = currentToolType;
                }
            } else if (currentToolType != this.mLastToolType && this.mLastToolType > 0) {
                this.mLastToolType = currentToolType;
                this.mFingerTime = SystemClock.uptimeMillis();
                String currentPkgName = getTopActivity();
                if (currentPkgName != null) {
                    try {
                        String[] result = currentPkgName.split("\\/");
                        if (result != null && result.length > 0 && this.mPkg != null && this.mPkg.contains(result[0])) {
                            int duration = Integer.parseInt(String.valueOf((this.mFingerTime - this.mStylusTime) / 1000));
                            Context context = this.mContext;
                            Flog.bdReport(context, 950, "{package:" + result[0] + ",duration:" + duration + "}");
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "stylus reportStylusUsageDuration :" + ex);
                    }
                }
            }
        }
    }

    private String getTopActivity() {
        ActivityManagerService am = ServiceManager.getService("activity");
        if (am != null) {
            return am.topAppName();
        }
        return null;
    }

    private void reportStylusUsageCount(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            if (motionEvent.getToolType(0) != 2) {
                this.mLastFingerUsageTime = SystemClock.uptimeMillis();
            } else if (this.mFirstTime) {
                Flog.bdReport(this.mContext, 951);
                this.mFirstTime = false;
            } else {
                long stylusUsageTime = SystemClock.uptimeMillis();
                if (this.mLastStylusUsageTime == 0) {
                    this.mLastStylusUsageTime = stylusUsageTime;
                }
                if (this.mLastFingerUsageTime > 0 && this.mLastFingerUsageTime < stylusUsageTime && stylusUsageTime - this.mLastStylusUsageTime >= ((long) this.STYLUS_USAGE_TIME_THRESHOLD)) {
                    this.mLastStylusUsageTime = 0;
                    this.mLastFingerUsageTime = 0;
                    Flog.bdReport(this.mContext, 951);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyKnockDown(MotionEvent event) {
        Intent intent = new Intent(ACTION_KNOCK_DOWN);
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
    }

    private boolean isValidStylusMotionEvent(MotionEvent motionEvent) {
        boolean isStylusScreenshotMode = motionEvent.getToolType(0) == 2 && motionEvent.getButtonState() == 32;
        WindowManagerPolicy.WindowState windowState = this.mPhoneWindowManager.getFocusedWindow();
        boolean areStylusScreenshotEnabled = windowState == null || (windowState.getAttrs().flags & 4096) == 0;
        if (!isStylusScreenshotMode || !areStylusScreenshotEnabled) {
            return false;
        }
        return true;
    }

    public void onRegionGesture(String gestureName, Gesture gesture, double predictionScore) {
        Log.i(TAG, "onRegionGesture, gestureName = " + gestureName + " predictionScore = " + predictionScore);
        Intent intent = StylusGestureSettings.getIntentForStylusGesture(gestureName, gesture, predictionScore);
        if (intent == null) {
            Log.d(TAG, "Ignoring " + gestureName + " gesture.");
        } else if (this.mScreenshotBitmap == null) {
            Log.d(TAG, "ScreenshotBitmap is null, failed to take Screenshot.");
        } else {
            intent.addFlags(32768);
            intent.putExtra(EXTRA_SCREENSHOT_BITMAP, this.mScreenshotBitmap.copy(Bitmap.Config.ARGB_8888, false));
            recycleScreenshot();
            dismissKeyguardIfCurrentlyShown();
            intent.setPackage("com.qeexo.smartshot");
            try {
                this.mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
                Log.i(TAG, "Start screenshot regionCrop activity");
                StatisticalUtils.reportc(this.mContext, 162);
                Flog.bdReport(this.mContext, 952, "true");
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "Failed to start activity: " + e);
            }
        }
    }

    public void onLetterGesture(String gestureName, Gesture gesture, double predictionScore) {
        Log.i(TAG, "onLetterGesture, gestureName = " + gestureName);
        Intent intent = StylusGestureSettings.getIntentForStylusGesture(gestureName, gesture, predictionScore);
        if (intent == null) {
            Log.d(TAG, "Ignoring " + gestureName + " gesture.");
            return;
        }
        try {
            ParceledListSlice<ActivityManager.RecentTaskInfo> slice = ActivityManagerNative.getDefault().getRecentTasks(1, 1, ActivityManagerNative.getDefault().getCurrentUser().id);
            if (slice != null) {
                List<ActivityManager.RecentTaskInfo> recentTask = slice.getList();
                if (recentTask != null && recentTask.size() > 0) {
                    UserInfo user = ((UserManager) this.mContext.getSystemService("user")).getUserInfo(recentTask.get(0).userId);
                    if (user == null || !user.isManagedProfile()) {
                        this.mContext.startServiceAsUser(intent, ActivityManagerNative.getDefault().getCurrentUser().getUserHandle());
                    } else {
                        this.mContext.startServiceAsUser(intent, user.getUserHandle());
                    }
                    Flog.bdReport(this.mContext, 952, "true");
                }
            }
            Log.i(TAG, "Start MultiScreenShotService.");
            StatisticalUtils.reportc(this.mContext, 163);
        } catch (Exception e) {
            Log.w(TAG, "Can not find the service for: " + e.getMessage());
        }
    }

    public void onLineGesture(String gestureName, Gesture gesture, double predictionScore) {
        Log.i(TAG, "onLineGesture, gestureName = " + gestureName);
        if (isTopTaskRecentOrHome()) {
            Log.i(TAG, "Top task is home or recent, return.");
            Toast toast = Toast.makeText(this.mContext, 33685924, 0);
            toast.getWindowParams().privateFlags |= 16;
            toast.show();
            return;
        }
        ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
        Flog.bdReport(this.mContext, 952, "true");
    }

    public void onStylusSingleTapPerformed(MotionEvent event, boolean showMenuView) {
        if (StylusGestureDetector.IS_TABLET) {
            int positionX = (int) event.getRawX();
            int positionY = (int) event.getRawY();
            Log.i(TAG, "onStylusSingleTapPerformed positionX = " + positionX + " positionY = " + positionY);
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(StylusGestureSettings.FLOAT_ENTRANCE_PACKAGE_NAME, StylusGestureSettings.FLOAT_ENTRANCE_CLASSNAME));
            intent.putExtra("positionX", positionX);
            intent.putExtra("positionY", positionY);
            if (showMenuView) {
                try {
                    StatisticalUtils.reportc(this.mContext, HwMpLinkWifiImpl.BAND_WIDTH_160MHZ);
                    intent.putExtra("prepareStatus", 1);
                    this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
                } catch (ServiceConfigurationError e) {
                    Log.w(TAG, "can not start service: " + e);
                }
            } else {
                this.mContext.stopServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
            }
        } else if (showMenuView) {
            Log.i(TAG, "onStylusSingleTapPerformed and wakeupNoteEditor.");
            wakeupNoteEditor();
        }
    }

    public void onStylusDoubleTapPerformed() {
        if (this.mPhoneWindowManager != null) {
            if (this.mStylusGestureView.isShown()) {
                removeStylusGestureView();
                Log.d(TAG, "mStylusGestureView isShown(), removing...");
            }
            Log.i(TAG, "onStylusDoubleTapPerformed and takeScreenshot.");
            StatisticalUtils.reportc(this.mContext, 161);
            this.mScreenshotHelper.takeScreenshot(1, true, true, this.mHandler);
            Flog.bdReport(this.mContext, 952, "true");
        }
    }

    public void notifySwtichInputMethod(boolean isStylus) {
        String ime = isStylus ? "handwrite IME" : "general IME";
        Log.i(TAG, "notifySwtichInputMethod and swtich current IME to " + ime);
        StatisticalUtils.reportc(this.mContext, 164);
        if (isStylus) {
            try {
                HwInputMethodManager.setDefaultIme(WRITE_IME_ID);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "SwtichInputMethod IllegalArgumentException:" + e);
            } catch (Exception e2) {
                Log.w(TAG, "SwtichInputMethod error occured:" + e2);
            }
        } else {
            HwInputMethodManager.setDefaultIme("");
        }
    }

    private void showStylusIntroducePage(MotionEvent motionEvent) {
        if (!StylusGestureDetector.IS_TABLET && motionEvent.getToolType(0) == 2 && motionEvent.getAction() == 0 && !this.mStylusGestureManager.isStylusIntroduced()) {
            Log.d(TAG, "show stylus introduce page!");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(StylusGestureSettings.FLOAT_ENTRANCE_PACKAGE_NAME, "com.huawei.stylus.floatmenu.IntroduceDialogActivity"));
            intent.addFlags(268435456);
            try {
                this.mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            } catch (ActivityNotFoundException exp) {
                Log.e(TAG, "start introduce Activity failed! message : " + exp.getMessage());
            } catch (Exception ex) {
                Log.e(TAG, "startActivity failed! message : " + ex.getMessage());
            }
        }
    }

    private void wakeupNoteEditor() {
        Intent notePadEditorIntent = new Intent("android.huawei.intent.action.note.handwriting");
        notePadEditorIntent.setPackage("com.example.android.notepad");
        try {
            this.mContext.startActivityAsUser(notePadEditorIntent, UserHandle.CURRENT_OR_SELF);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "startActivity failed! message : " + ex.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "startActivityAsUser(): Exception = " + e);
        }
    }

    private boolean isTopTaskRecentOrHome() {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ActivityManager.getService().getFilteredTasks(1, 0, 2);
            if (tasks.isEmpty()) {
                return false;
            }
            int activityType = tasks.get(0).configuration.windowConfiguration.getActivityType();
            return activityType == 2 || activityType == 3;
        } catch (RemoteException e) {
            return false;
        }
    }

    private void addStylusGestureView() {
        boolean isVRMode = false;
        if (HwFrameworkFactory.getVRSystemServiceManager() != null) {
            isVRMode = HwFrameworkFactory.getVRSystemServiceManager().isVRMode();
        }
        if (isVRMode) {
            Log.d(TAG, "current is in VR Mode,view cannot added!");
            return;
        }
        if (this.mStylusGestureView == null) {
            this.mStylusGestureView = new PointerLocationView(this.mContext, this);
            this.mLayoutParams = new WindowManager.LayoutParams(-1, -1);
            this.mLayoutParams.type = HwArbitrationDEFS.MSG_MPLINK_STOP_COEX_SUCC;
            this.mLayoutParams.flags = 1304;
            this.mLayoutParams.privateFlags |= 16;
            if (ActivityManager.isHighEndGfx()) {
                this.mLayoutParams.flags |= 16777216;
                this.mLayoutParams.privateFlags |= 2;
            }
            this.mLayoutParams.format = -3;
            this.mLayoutParams.setTitle("StylusGestureView");
            this.mLayoutParams.inputFeatures |= 2;
            if (this.mHasNotchInScreen) {
                this.mLayoutParams.layoutInDisplayCutoutMode = 1;
            }
        }
        synchronized (this.viewLock) {
            if (!this.isStylusViewAdded) {
                Log.d(TAG, "addStylusGestureView");
                this.mWindowManager.addView(this.mStylusGestureView, this.mLayoutParams);
                this.isStylusViewAdded = true;
            }
        }
    }

    private void removeStylusGestureView() {
        synchronized (this.viewLock) {
            if (this.mStylusGestureView != null && this.isStylusViewAdded) {
                Log.d(TAG, "removeStylusGestureView");
                this.mWindowManager.removeView(this.mStylusGestureView);
                this.isStylusViewAdded = false;
            }
        }
    }
}
