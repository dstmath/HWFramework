package huawei.com.android.server.policy.stylus;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
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
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.view.WindowManagerPolicy.WindowState;
import com.android.server.policy.HwPhoneWindowManager;
import com.huawei.android.inputmethod.HwInputMethodManager;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector.OnGesturePerformedListener;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector.OrientationFix;
import huawei.com.android.server.policy.fingersense.pixiedust.PointerLocationView;
import huawei.com.android.server.policy.stylus.StylusGestureDetector.StylusGestureRecognizeListener;
import java.util.List;
import java.util.ServiceConfigurationError;

public class StylusGestureListener implements PointerEventListener, OnGesturePerformedListener, StylusGestureRecognizeListener {
    private static final String ACTION_KNOCK_DOWN = "com.qeexo.syswideactions.KNOCK_DOWN";
    private static final String EXTRA_SCREENSHOT_BITMAP = "com.qeexo.syswideactions.screenshot.bitmap";
    private static final OrientationFix[] ORIENTATION_FIXES = new OrientationFix[]{new OrientationFix(StylusGestureSettings.STYLUS_GESTURE_C_SUFFIX, StylusGestureSettings.STYLUS_GESTURE_W_SUFFIX, null), new OrientationFix(StylusGestureSettings.STYLUS_GESTURE_C_SUFFIX, StylusGestureSettings.STYLUS_GESTURE_M_SUFFIX, null)};
    private static final String STYLUS_GESTURES_PATH = "/product/bin/stylus_gestures.bin";
    private static final String TAG = "StylusGestureListener";
    private static final String WRITE_IME_ID = "com.visionobjects.stylusmobile.v3_2_huawei/com.visionobjects.stylusmobile.v3_2.StylusIMService";
    private boolean forPCModeOnly = false;
    private boolean hasKnockDownOccured;
    private boolean isActionOnKeyboard = false;
    private boolean isStylusViewAdded = false;
    private final Context mContext;
    private final CustomGestureDetector mCustomGestureDetector;
    private int mDisplayHeight;
    private int mDisplayWidth;
    private Handler mHandler;
    private KeyguardManager mKeyguardManager;
    private LayoutParams mLayoutParams;
    private final HwPhoneWindowManager mPhoneWindowManager;
    private Bitmap mScreenshotBitmap;
    private final StylusGestureDetector mStylusGestureDetector;
    private PointerLocationView mStylusGestureView;
    private WindowManager mWindowManager;
    private final MotionEventRunnable onKnockDownRunnable = new MotionEventRunnable() {
        public void run() {
            StylusGestureListener.this.notifyKnockDown(this.event);
        }
    };
    private Object viewLock = new Object();

    private static abstract class MotionEventRunnable implements Runnable {
        MotionEvent event;

        /* synthetic */ MotionEventRunnable(MotionEventRunnable -this0) {
            this();
        }

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
        this.mCustomGestureDetector = new CustomGestureDetector(this.mContext, STYLUS_GESTURES_PATH, (OnGesturePerformedListener) this);
        this.mCustomGestureDetector.setMinPredictionScore(2.0f);
        this.mCustomGestureDetector.setOrientationFixes(ORIENTATION_FIXES);
        this.mCustomGestureDetector.setMinLineGestureStrokeLength(this.mDisplayWidth / 2);
        this.mCustomGestureDetector.setLineGestureStrokePortraitAngle(0.0f);
        this.mCustomGestureDetector.setLineGestureStrokeLandscapeAngle(90.0f);
        this.mCustomGestureDetector.setMaxLineGestureStrokeAngleDeviation(10.0f);
        this.mCustomGestureDetector.setLineGestureStrokeStraightness(4.0f);
        this.mCustomGestureDetector.setRegionDiagonalThreshold(0.5f);
    }

    public void cancelStylusGesture() {
        MotionEvent cancelEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 3, 0.0f, 0.0f, 0);
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
        this.mScreenshotBitmap = SurfaceControl.screenshot_ext_hw(this.mDisplayWidth, this.mDisplayHeight);
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
            this.mPhoneWindowManager.dismissKeyguardLw(null);
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
        this.mStylusGestureDetector.shouldSwtichInputMethod(motionEvent);
        if (!this.forPCModeOnly) {
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

    private void notifyKnockDown(MotionEvent event) {
        Intent intent = new Intent(ACTION_KNOCK_DOWN);
        intent.addFlags(536870912);
        this.mContext.sendBroadcast(intent);
    }

    private boolean isValidStylusMotionEvent(MotionEvent motionEvent) {
        boolean isStylusScreenshotMode = motionEvent.getToolType(0) == 2 ? motionEvent.getButtonState() == 32 : false;
        if (motionEvent.getAction() == 1) {
            this.isActionOnKeyboard = false;
        }
        int inputWindowHeight = this.mPhoneWindowManager.getInputMethodWindowVisibleHeightLw();
        int visibleScreenHeight = this.mPhoneWindowManager.getRestrictedScreenHeight() - inputWindowHeight;
        if (inputWindowHeight > 0 && motionEvent.getY() > ((float) visibleScreenHeight) && motionEvent.getAction() == 0) {
            this.isActionOnKeyboard = true;
        }
        if (this.isActionOnKeyboard) {
            return false;
        }
        WindowState windowState = this.mPhoneWindowManager.getFocusedWindow();
        boolean areStylusScreenshotEnabled = windowState == null || (windowState.getAttrs().hwFlags & 4096) == 0;
        if (!isStylusScreenshotMode) {
            areStylusScreenshotEnabled = false;
        }
        return areStylusScreenshotEnabled;
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
            intent.putExtra("com.qeexo.syswideactions.screenshot.bitmap", this.mScreenshotBitmap.copy(Config.ARGB_8888, false));
            recycleScreenshot();
            dismissKeyguardIfCurrentlyShown();
            intent.setPackage("com.qeexo.smartshot");
            try {
                this.mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
                Log.i(TAG, "Start screenshot regionCrop activity");
                StatisticalUtils.reportc(this.mContext, 162);
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
            ParceledListSlice<RecentTaskInfo> slice = ActivityManagerNative.getDefault().getRecentTasks(1, 5, ActivityManagerNative.getDefault().getCurrentUser().id);
            if (slice != null) {
                List<RecentTaskInfo> recentTask = slice.getList();
                if (recentTask != null && recentTask.size() > 0) {
                    UserInfo user = ((UserManager) this.mContext.getSystemService("user")).getUserInfo(((RecentTaskInfo) recentTask.get(0)).userId);
                    if (user == null || !user.isManagedProfile()) {
                        this.mContext.startServiceAsUser(intent, ActivityManagerNative.getDefault().getCurrentUser().getUserHandle());
                    } else {
                        this.mContext.startServiceAsUser(intent, user.getUserHandle());
                    }
                }
            }
            Log.i(TAG, "Start MultiScreenShotService.");
            StatisticalUtils.reportc(this.mContext, 163);
        } catch (Exception e) {
            Log.w(TAG, "Can not find the service for: " + e.getMessage());
        }
    }

    public void onLineGesture(String gestureName, Gesture gesture, double predictionScore) {
    }

    public void onStylusSingleTapPerformed(MotionEvent event, boolean showMenuView) {
        int positionX = (int) event.getRawX();
        int positionY = (int) event.getRawY();
        Log.i(TAG, "onStylusSingleTapPerformed positionX = " + positionX + " positionY = " + positionY);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(StylusGestureSettings.FLOAT_ENTRANCE_PACKAGE_NAME, StylusGestureSettings.FLOAT_ENTRANCE_CLASSNAME));
        intent.putExtra("positionX", positionX);
        intent.putExtra("positionY", positionY);
        if (showMenuView) {
            try {
                StatisticalUtils.reportc(this.mContext, 160);
                intent.putExtra("prepareStatus", 1);
                this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
                return;
            } catch (ServiceConfigurationError e) {
                Log.w(TAG, "can not start service: " + e);
                return;
            }
        }
        this.mContext.stopServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }

    public void onStylusDoubleTapPerformed() {
        if (this.mPhoneWindowManager != null) {
            if (this.mStylusGestureView.isShown()) {
                removeStylusGestureView();
                Log.d(TAG, "mStylusGestureView isShown(), removing...");
            }
            Log.i(TAG, "onStylusDoubleTapPerformed and takeScreenshot.");
            StatisticalUtils.reportc(this.mContext, 161);
            this.mPhoneWindowManager.takeScreenshot(1);
        }
    }

    public void notifySwtichInputMethod(boolean isStylus) {
        Log.i(TAG, "notifySwtichInputMethod and swtich current IME to " + (isStylus ? "handwrite IME" : "general IME"));
        StatisticalUtils.reportc(this.mContext, 164);
        if (isStylus) {
            try {
                HwInputMethodManager.setDefaultIme(WRITE_IME_ID);
                return;
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "SwtichInputMethod IllegalArgumentException:" + e);
                return;
            } catch (Exception e2) {
                Log.w(TAG, "SwtichInputMethod error occured:" + e2);
                return;
            }
        }
        HwInputMethodManager.setDefaultIme("");
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
            this.mLayoutParams = new LayoutParams(-1, -1);
            this.mLayoutParams.type = 2015;
            this.mLayoutParams.flags = 1304;
            LayoutParams layoutParams = this.mLayoutParams;
            layoutParams.privateFlags |= 16;
            if (ActivityManager.isHighEndGfx()) {
                layoutParams = this.mLayoutParams;
                layoutParams.flags |= HwGlobalActionsData.FLAG_SHUTDOWN;
                layoutParams = this.mLayoutParams;
                layoutParams.privateFlags |= 2;
            }
            this.mLayoutParams.format = -3;
            this.mLayoutParams.setTitle("StylusGestureView");
            layoutParams = this.mLayoutParams;
            layoutParams.inputFeatures |= 2;
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
