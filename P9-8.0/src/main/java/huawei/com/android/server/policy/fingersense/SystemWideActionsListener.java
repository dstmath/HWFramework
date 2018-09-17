package huawei.com.android.server.policy.fingersense;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManagerNative;
import android.app.KeyguardManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.view.WindowManagerPolicy.WindowState;
import android.widget.Toast;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.IStatusBarService.Stub;
import com.android.server.emcom.SmartcareConstants;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.provider.FingerSenseSettings;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector.OnGesturePerformedListener;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector.OrientationFix;
import huawei.com.android.server.policy.fingersense.KnockGestureDetector.OnKnockGestureListener;
import huawei.com.android.server.policy.fingersense.pixiedust.PointerLocationView;
import huawei.com.android.server.policy.stylus.StylusGestureSettings;
import java.util.List;

public class SystemWideActionsListener implements PointerEventListener, OnGesturePerformedListener, OnKnockGestureListener {
    private static final String ACTION_KNOCK_DOWN = "com.qeexo.syswideactions.KNOCK_DOWN";
    private static final String CAMERA_PACKAGE_NAME = "com.huawei.camera";
    private static final boolean DEBUG = false;
    public static final String EXTRA_EVENT1 = "com.qeexo.syswideactions.event1";
    public static final String EXTRA_GESTURE = "com.qeexo.syswideactions.gesture";
    public static final String EXTRA_GESTURE_NAME = "com.qeexo.syswideactions.gesture.name";
    public static final String EXTRA_GESTURE_PREDICTION_SCORE = "com.qeexo.syswideactions.gesture.score";
    public static final String EXTRA_SCREENSHOT_BITMAP = "com.qeexo.syswideactions.screenshot.bitmap";
    private static boolean HWFLOW = false;
    private static final String KNUCKLE_GESTURES_PATH = "/system/bin/knuckle_gestures.bin";
    private static final String LEFT = "left";
    private static final int MAX_PATH_LENGTH = 6144;
    private static final OrientationFix[] ORIENTATION_FIXES = new OrientationFix[]{new OrientationFix(StylusGestureSettings.STYLUS_GESTURE_C_SUFFIX, StylusGestureSettings.STYLUS_GESTURE_W_SUFFIX, null), new OrientationFix(StylusGestureSettings.STYLUS_GESTURE_C_SUFFIX, StylusGestureSettings.STYLUS_GESTURE_M_SUFFIX, null)};
    private static final String RIGHT = "right";
    private static final float SCALE = 0.75f;
    private static final int SPLIT_SCREEN_MIN_SCREEN_HEIGHT_PERCENTAGE = 10;
    private static final int STATE_LEFT = 1;
    private static final int STATE_MIDDLE = 0;
    private static final int STATE_RIGHT = 2;
    private static final String TAG = "SystemWideActionsListener";
    private boolean FLOG_FLAG = true;
    private final ActivityManager activityManager;
    private final Context context;
    private final CustomGestureDetector customGestureDetector;
    private Handler handler;
    private boolean hasKnuckleDownOccured;
    private boolean isActionOnKeyboard = false;
    private KeyguardManager keyguardManager;
    private final KnockGestureDetector knockGestureDetector;
    private LayoutParams layoutParams;
    private int mDisplayHeight;
    private int mDisplayWidth;
    private int mNavBarLandscapeHeight;
    private int mNavBarPortraitHeight;
    private Bitmap mScreenshotBitmap;
    final Object mServiceAquireLock = new Object();
    IStatusBarService mStatusBarService;
    private final MotionEventRunnable onKnockDownRunnable = new MotionEventRunnable() {
        public void run() {
            SystemWideActionsListener.this.notifyKnockDown(this.event);
        }
    };
    private final HwPhoneWindowManager phoneWindowManager;
    private PointerLocationView pointerLocationView;
    private Intent startServiceIntent;
    private Vibrator vibrator;
    private boolean viewAdd = false;
    private Object viewLock = new Object();
    private final WindowManager windowManager;

    private static abstract class MotionEventRunnable implements Runnable {
        MotionEvent event;

        /* synthetic */ MotionEventRunnable(MotionEventRunnable -this0) {
            this();
        }

        private MotionEventRunnable() {
            this.event = null;
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    IStatusBarService getHWStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = Stub.asInterface(ServiceManager.getService("statusbar"));
            }
            iStatusBarService = this.mStatusBarService;
        }
        return iStatusBarService;
    }

    public SystemWideActionsListener(Context context, HwPhoneWindowManager phoneWindowManager) {
        this.context = context;
        this.phoneWindowManager = phoneWindowManager;
        this.activityManager = (ActivityManager) context.getSystemService("activity");
        this.windowManager = (WindowManager) context.getSystemService("window");
        this.mNavBarPortraitHeight = context.getResources().getDimensionPixelSize(17105141);
        this.mNavBarLandscapeHeight = context.getResources().getDimensionPixelSize(17105143);
        Display display = this.windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mDisplayWidth = size.x > size.y ? size.y : size.x;
        this.mDisplayHeight = size.x > size.y ? size.x : size.y;
        this.customGestureDetector = new CustomGestureDetector(context, KNUCKLE_GESTURES_PATH, (OnGesturePerformedListener) this);
        this.customGestureDetector.setMinPredictionScore(2.0f);
        this.customGestureDetector.setOrientationFixes(ORIENTATION_FIXES);
        this.customGestureDetector.setMinLineGestureStrokeLength(this.mDisplayWidth / 2);
        this.customGestureDetector.setLineGestureStrokePortraitAngle(0.0f);
        this.customGestureDetector.setLineGestureStrokeLandscapeAngle(90.0f);
        this.customGestureDetector.setMaxLineGestureStrokeAngleDeviation(10.0f);
        this.customGestureDetector.setLineGestureStrokeStraightness(4.0f);
        this.knockGestureDetector = new KnockGestureDetector(context, this);
        this.hasKnuckleDownOccured = false;
        this.keyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        this.handler = new Handler();
        this.vibrator = (Vibrator) context.getSystemService("vibrator");
    }

    public void cancelSystemWideAction() {
        MotionEvent cancelEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 3, 0.0f, 0.0f, 0);
        this.customGestureDetector.onTouchEvent(cancelEvent);
        if (this.pointerLocationView != null) {
            this.pointerLocationView.onTouchEvent(cancelEvent);
        }
        removePointerLocationView();
        this.hasKnuckleDownOccured = false;
        cancelEvent.recycle();
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        if (!isInValidAction(motionEvent.getAction())) {
            this.knockGestureDetector.onAnyTouchEvent(motionEvent);
            if (shouldProcessMotionEvent(motionEvent)) {
                if (ActivityManagerEx.isGameGestureDisabled()) {
                    Log.i(TAG, "GameGestureDisabled is true,return");
                    return;
                }
                if (motionEvent.getAction() == 0) {
                    StatisticalUtils.reportc(this.context, 101);
                    this.onKnockDownRunnable.event = motionEvent;
                    this.handler.post(this.onKnockDownRunnable);
                    if (HWFLOW) {
                        Log.i(TAG, "FingerSense Down Event.");
                    }
                    this.hasKnuckleDownOccured = true;
                    saveScreenBitmap();
                }
                this.knockGestureDetector.onKnuckleTouchEvent(motionEvent);
                if (this.hasKnuckleDownOccured && this.knockGestureDetector.getKnucklePointerCount() < 2) {
                    addPointerLocationView();
                    this.customGestureDetector.onTouchEvent(motionEvent);
                    if (this.pointerLocationView != null) {
                        this.pointerLocationView.onTouchEvent(motionEvent);
                    }
                }
                if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                    cancelSystemWideAction();
                }
            } else if (this.hasKnuckleDownOccured) {
                cancelSystemWideAction();
            }
        }
    }

    private boolean isInValidAction(int action) {
        if (action == 0 || (action & 255) == 5 || action == 1 || (action & 255) == 6 || action == 3) {
            return false;
        }
        return action != 2;
    }

    private static int getLazyState(Context context) {
        String str = Global.getString(context.getContentResolver(), "single_hand_mode");
        if (str == null || "".equals(str)) {
            return 0;
        }
        if (str.contains(LEFT)) {
            return 1;
        }
        if (str.contains(RIGHT)) {
            return 2;
        }
        return 0;
    }

    private static boolean isLazyMode(Context context) {
        return getLazyState(context) != 0;
    }

    private static Rect getScreenshotRect(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        int state = getLazyState(context);
        if (1 == state) {
            return new Rect(0, (int) (((float) displayMetrics.heightPixels) * 0.25f), (int) (((float) displayMetrics.widthPixels) * 0.75f), displayMetrics.heightPixels);
        }
        if (2 == state) {
            return new Rect((int) (((float) displayMetrics.widthPixels) * 0.25f), (int) (((float) displayMetrics.heightPixels) * 0.25f), displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
        return null;
    }

    private void notifyKnockDown(MotionEvent event) {
        Intent intent = new Intent(ACTION_KNOCK_DOWN);
        intent.addFlags(536870912);
        this.context.sendBroadcast(intent);
    }

    private boolean shouldProcessMotionEvent(MotionEvent motionEvent) {
        int my_count = motionEvent.getPointerCount();
        for (int i = 0; i < my_count; i++) {
            if (motionEvent.getToolType(i) != 7) {
                return false;
            }
        }
        if (this.FLOG_FLAG) {
            this.FLOG_FLAG = false;
            Flog.i(1503, "Get event type TOOL_TYPE_FINGER_KNUCKLE!");
        }
        int inputWindowHeight = this.phoneWindowManager.getInputMethodWindowVisibleHeightLw();
        if (inputWindowHeight > 0 && motionEvent.getY() > ((float) (this.phoneWindowManager.getRestrictedScreenHeight() - inputWindowHeight)) && motionEvent.getAction() == 0) {
            this.isActionOnKeyboard = true;
        }
        if (this.isActionOnKeyboard) {
            if (motionEvent.getAction() == 1) {
                this.isActionOnKeyboard = false;
            }
            return false;
        }
        WindowState windowState = this.phoneWindowManager.getFocusedWindow();
        boolean areSystemWideActionsEnabled = windowState == null || (windowState.getAttrs().flags & 4096) == 0;
        return areSystemWideActionsEnabled;
    }

    private void saveScreenBitmap() {
        recycleScreenshot();
        Rect sourceCrop = getScreenshotRect(this.context);
        if (isLazyMode(this.context)) {
            this.mScreenshotBitmap = SurfaceControl.screenshot_ext_hw(sourceCrop, this.mDisplayWidth, this.mDisplayHeight, 0, SmartcareConstants.INVALID, false, 0);
        } else {
            this.mScreenshotBitmap = SurfaceControl.screenshot_ext_hw(this.mDisplayWidth, this.mDisplayHeight);
        }
    }

    private void recycleScreenshot() {
        if (this.mScreenshotBitmap != null) {
            this.mScreenshotBitmap.recycle();
            this.mScreenshotBitmap = null;
        }
    }

    private Intent getIntentForCustomGesture(String gestureName, Gesture gesture, double predictionScore) {
        if (FingerSenseSettings.isKnuckleGestureEnable(gestureName, this.context.getContentResolver())) {
            Intent intent = FingerSenseSettings.getIntentForGesture(gestureName, this.context);
            if (intent == null) {
                Log.w(TAG, "Gesture '" + gestureName + "' recognized but application intent was null.");
                return null;
            }
            intent.addFlags(268435456);
            intent.putExtra(EXTRA_GESTURE, gesture);
            intent.putExtra(EXTRA_GESTURE_NAME, gestureName);
            intent.putExtra(EXTRA_GESTURE_PREDICTION_SCORE, predictionScore);
            return intent;
        }
        Log.w(TAG, "Gesture '" + gestureName + "' recognized but no application assigned.");
        return null;
    }

    public void onRegionGesture(String gestureName, Gesture gesture, double predictionScore) {
        Intent intent = getIntentForCustomGesture(gestureName, gesture, predictionScore);
        if (intent == null) {
            Log.d(TAG, "Ignoring " + gestureName + " gesture.");
            return;
        }
        intent.addFlags(32768);
        Log.d(TAG, "Fingersense Region Gesture");
        StatisticalUtils.reportc(this.context, 104);
        if (this.mScreenshotBitmap == null) {
            Log.w(TAG, "SystemWideActionsListener failed to take Screenshot.");
            return;
        }
        intent.putExtra(EXTRA_SCREENSHOT_BITMAP, this.mScreenshotBitmap.copy(Config.ARGB_8888, false));
        recycleScreenshot();
        dismissKeyguardIfCurrentlyShown();
        intent.setPackage("com.qeexo.smartshot");
        this.context.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }

    public void onLetterGesture(String gestureName, Gesture gesture, double predictionScore) {
        if (StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX.equals(gestureName.toLowerCase()) && FingerSenseSettings.isFingerSenseSmartshotEnabled(this.context.getContentResolver())) {
            Intent startIntent = FingerSenseSettings.getIntentForMultiScreenShot(this.context);
            if (startIntent != null) {
                try {
                    ParceledListSlice<RecentTaskInfo> slice = ActivityManagerNative.getDefault().getRecentTasks(1, 5, ActivityManagerNative.getDefault().getCurrentUser().id);
                    if (slice != null) {
                        List<RecentTaskInfo> recentTask = slice.getList();
                        if (recentTask != null && recentTask.size() > 0) {
                            UserInfo user = ((UserManager) this.context.getSystemService("user")).getUserInfo(((RecentTaskInfo) recentTask.get(0)).userId);
                            if (user == null || !user.isManagedProfile()) {
                                this.context.startServiceAsUser(startIntent, ActivityManagerNative.getDefault().getCurrentUser().getUserHandle());
                            } else {
                                this.context.startServiceAsUser(startIntent, user.getUserHandle());
                            }
                        }
                    }
                    StatisticalUtils.reportc(this.context, CPUFeature.MSG_STOP_BIGDATAPROCRECORD);
                    if (HWFLOW) {
                        Log.i(TAG, "Start Service for MultiScreenShotService.");
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Can not find the service for: " + e.getMessage());
                }
            } else {
                Log.w(TAG, "Letter Gesture '" + gestureName + "' recognized but application intent was null.");
            }
            return;
        }
        Intent intent = getIntentForCustomGesture(gestureName, gesture, predictionScore);
        if (intent == null) {
            Log.d(TAG, "Ignoring " + gestureName + " gesture.");
            return;
        }
        Flog.i(1503, "onLetterGesture gesture= " + gesture);
        intent.addFlags(536870912);
        if (!gestureName.toLowerCase().equals(StylusGestureSettings.STYLUS_GESTURE_S_SUFFIX)) {
            Log.d(TAG, "Fingersense Letter Gesture " + gestureName);
            StatisticalUtils.reportc(this.context, 105);
        }
        if (intent.getComponent() != null && "com.huawei.camera".equals(intent.getComponent().getPackageName()) && this.keyguardManager != null && this.keyguardManager.isKeyguardLocked() && this.keyguardManager.isKeyguardSecure()) {
            intent = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").setPackage("com.huawei.camera").addFlags(8388608);
            Log.d(TAG, "Keyguard is locked, starting secured camera.");
            this.context.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            StatisticalUtils.reporte(this.context, 110, "{letter:" + gestureName + ",pkg:null}");
            return;
        }
        this.context.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        StatisticalUtils.reporte(this.context, 110, "{letter:" + gestureName + ",pkg:" + intent.getComponent().getPackageName() + "}");
        this.handler.postDelayed(new Runnable() {
            public void run() {
                SystemWideActionsListener.this.dismissKeyguardIfCurrentlyShown();
            }
        }, 500);
    }

    public void onLineGesture(String gestureName, Gesture gesture, double predictionScore) {
        if (FingerSenseSettings.isFingerSenseLineGestureEnabled(this.context.getContentResolver())) {
            float orientedDistance;
            int navBarHeight;
            Log.d(TAG, "Fingersense Line Gesture");
            StatisticalUtils.reportc(this.context, 102);
            RectF boundingBox = gesture.getBoundingBox();
            if (this.context.getResources().getConfiguration().orientation == 2) {
                orientedDistance = boundingBox.centerX();
                navBarHeight = this.mNavBarLandscapeHeight;
            } else {
                orientedDistance = boundingBox.centerY();
                navBarHeight = this.mNavBarPortraitHeight;
            }
            if (!this.phoneWindowManager.isNavigationBarVisible()) {
                navBarHeight = 0;
            }
            int availableScreenSize = this.mDisplayHeight - navBarHeight;
            int minWindowSize = (availableScreenSize * 10) / 100;
            if (orientedDistance <= ((float) minWindowSize) || orientedDistance >= ((float) (availableScreenSize - minWindowSize))) {
                Log.w(TAG, "Line Gesture recognized, but too close to screen edge.");
            } else if (this.keyguardManager == null) {
                Log.e(TAG, "keyguardManager is null");
                return;
            } else if (!this.keyguardManager.isKeyguardLocked()) {
                try {
                    if (WindowManagerGlobal.getWindowManagerService().getDockedStackSide() == -1) {
                        toggleSplitScreenByLineGesture(107, "toggleSplitScreenByLineGesture", (int) boundingBox.centerX(), (int) boundingBox.centerY());
                    }
                } catch (RemoteException e) {
                    Log.w(TAG, "Failed to get dock side: " + e);
                }
            }
            return;
        }
        Log.w(TAG, gestureName + " gesture disabled");
    }

    public void onDoubleKnuckleDoubleKnock(String gestureName, MotionEvent event) {
        if (!FingerSenseSettings.isFingerSenseSmartshotEnabled(this.context.getContentResolver())) {
            Flog.w(1503, "FingerSense SmartShot Disabled!");
        } else if (this.keyguardManager == null) {
            Log.e(TAG, "keyguardManager is null");
        } else if (this.keyguardManager.inKeyguardRestrictedInputMode()) {
            Flog.w(1503, "On lock screen SmartShot Disabled!");
        } else {
            Flog.i(1503, "FingerSense Double Knuckle Double Knock");
            StatisticalUtils.reportc(this.context, 107);
            this.startServiceIntent = new Intent();
            this.startServiceIntent.setAction("com.huawei.screenrecorder.Start");
            this.startServiceIntent.setClassName("com.huawei.screenrecorder", "com.huawei.screenrecorder.ScreenRecordService");
            this.context.startServiceAsUser(this.startServiceIntent, UserHandle.CURRENT_OR_SELF);
        }
    }

    public void onSingleKnuckleDoubleKnock(String gestureName, MotionEvent event) {
        if (!FingerSenseSettings.isFingerSenseSmartshotEnabled(this.context.getContentResolver())) {
            Flog.i(1503, "FingerSense SmartShot Disabled!");
        } else if (HwFrameworkFactory.getCoverManager().isCoverOpen()) {
            Flog.i(1503, "FingerSense Single Knuckle Double Knock");
            StatisticalUtils.reportc(this.context, 103);
            while (this.pointerLocationView.isShown()) {
                removePointerLocationView();
                Flog.i(1503, "pointerLocationView isShown(), removing...");
            }
            if (HWFLOW) {
                Flog.i(1503, "double_knock ScreenShot enter.");
            }
            sendScreenshotNotification();
            this.phoneWindowManager.takeScreenshot(1);
        } else {
            Flog.i(1503, "Cover is closed.");
        }
    }

    private void sendScreenshotNotification() {
        Intent screenshotIntent = new Intent("com.huawei.recsys.action.RECEIVE_EVENT");
        screenshotIntent.putExtra("eventOperator", "sysScreenShot");
        screenshotIntent.putExtra("eventItem", "double_knock");
        screenshotIntent.setPackage("com.huawei.recsys");
        this.context.sendBroadcastAsUser(screenshotIntent, UserHandle.CURRENT, "com.huawei.tips.permission.SHOW_TIPS");
    }

    public void onDoubleKnocksNotYetConfirmed(String gestureName, MotionEvent event) {
        cancelSystemWideAction();
    }

    public void createPointerLocationView() {
        if (this.pointerLocationView == null) {
            this.pointerLocationView = new PointerLocationView(this.context, this);
            this.layoutParams = new LayoutParams(-1, -1);
            this.layoutParams.type = 2015;
            this.layoutParams.flags = 1304;
            LayoutParams layoutParams = this.layoutParams;
            layoutParams.privateFlags |= 16;
            if (ActivityManager.isHighEndGfx()) {
                layoutParams = this.layoutParams;
                layoutParams.flags |= HwGlobalActionsData.FLAG_SHUTDOWN;
                layoutParams = this.layoutParams;
                layoutParams.privateFlags |= 2;
            }
            this.layoutParams.format = -3;
            this.layoutParams.setTitle("KnucklePointerLocationView");
            layoutParams = this.layoutParams;
            layoutParams.inputFeatures |= 2;
        }
    }

    private void addPointerLocationView() {
        boolean isVRMode = false;
        if (HwFrameworkFactory.getVRSystemServiceManager() != null) {
            isVRMode = HwFrameworkFactory.getVRSystemServiceManager().isVRMode();
        }
        if (isVRMode) {
            Flog.i(1503, "current is in VR Mode,view cannot added!");
            return;
        }
        synchronized (this.viewLock) {
            if (!(this.viewAdd || this.pointerLocationView == null)) {
                Flog.i(1503, "addPointerLocationView layoutParams flags= " + Integer.toHexString(this.layoutParams.flags) + " privateFlags= " + Integer.toHexString(this.layoutParams.privateFlags) + " type= " + Integer.toHexString(this.layoutParams.type) + " inputFeatures= " + Integer.toHexString(this.layoutParams.inputFeatures));
                this.windowManager.addView(this.pointerLocationView, this.layoutParams);
                this.viewAdd = true;
            }
        }
    }

    public void destroyPointerLocationView() {
        if (this.pointerLocationView != null) {
            removePointerLocationView();
            this.pointerLocationView = null;
        }
    }

    private void removePointerLocationView() {
        synchronized (this.viewLock) {
            if (this.viewAdd && this.pointerLocationView != null) {
                this.windowManager.removeView(this.pointerLocationView);
                this.viewAdd = false;
            }
        }
    }

    private void toast(String message, boolean vibrate) {
        Toast.makeText(this.context, message, 0).show();
        if (vibrate) {
            vibrate(new long[]{0, 20, 100, 20});
        }
    }

    private void vibrate(long[] pattern) {
        this.vibrator.vibrate(pattern, -1);
    }

    private void dismissKeyguardIfCurrentlyShown() {
        if (this.keyguardManager == null) {
            Log.e(TAG, "keyguardManager is null");
            return;
        }
        if (this.keyguardManager.inKeyguardRestrictedInputMode()) {
            this.phoneWindowManager.dismissKeyguardLw(null);
        }
    }

    private String getAppName(String packageName) {
        CharSequence applicationLabel;
        PackageManager pm = this.context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
        }
        if (applicationInfo != null) {
            applicationLabel = pm.getApplicationLabel(applicationInfo);
        } else {
            Object applicationLabel2 = packageName;
        }
        return (String) applicationLabel2;
    }

    public void updateConfiguration() {
        Display display = this.windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mDisplayWidth = size.x > size.y ? size.y : size.x;
        this.mDisplayHeight = size.x > size.y ? size.x : size.y;
    }

    private void toggleSplitScreenByLineGesture(int code, String transactName, int param1, int param2) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getHWStatusBarService() != null) {
                IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
                if (statusBarServiceBinder != null) {
                    Log.d(TAG, "Transact:" + transactName + " to status bar service");
                    data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    data.writeInt(param1);
                    data.writeInt(param2);
                    statusBarServiceBinder.transact(code, data, reply, 0);
                }
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
            reply.recycle();
            data.recycle();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
    }
}
