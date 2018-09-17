package huawei.com.android.server.policy.fingersense;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.KeyguardManager;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.android.server.display.Utils;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.provider.FingerSenseSettings;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector.OnGesturePerformedListener;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector.OrientationFix;
import huawei.com.android.server.policy.fingersense.KnockGestureDetector.OnKnockGestureListener;
import huawei.com.android.server.policy.fingersense.pixiedust.PointerLocationView;

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
    private static final OrientationFix[] ORIENTATION_FIXES = null;
    private static final String RIGHT = "right";
    private static final float SCALE = 0.75f;
    private static final int SPLIT_SCREEN_MIN_SCREEN_HEIGHT_PERCENTAGE = 10;
    private static final int STATE_LEFT = 1;
    private static final int STATE_MIDDLE = 0;
    private static final int STATE_RIGHT = 2;
    private static final String TAG = "SystemWideActionsListener";
    private boolean FLOG_FLAG;
    private final ActivityManager activityManager;
    private final Context context;
    private final CustomGestureDetector customGestureDetector;
    private Handler handler;
    private boolean hasKnuckleDownOccured;
    private boolean isActionOnKeyboard;
    private KeyguardManager keyguardManager;
    private final KnockGestureDetector knockGestureDetector;
    private LayoutParams layoutParams;
    private int mDisplayHeight;
    private int mDisplayWidth;
    private int mNavBarLandscapeHeight;
    private int mNavBarPortraitHeight;
    private Bitmap mScreenshotBitmap;
    final Object mServiceAquireLock;
    IStatusBarService mStatusBarService;
    private final MotionEventRunnable onKnockDownRunnable;
    private final HwPhoneWindowManager phoneWindowManager;
    private PointerLocationView pointerLocationView;
    private Intent startServiceIntent;
    private Vibrator vibrator;
    private boolean viewAdd;
    private Object viewLock;
    private final WindowManager windowManager;

    private static abstract class MotionEventRunnable implements Runnable {
        MotionEvent event;

        private MotionEventRunnable() {
            this.event = null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.server.policy.fingersense.SystemWideActionsListener.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.server.policy.fingersense.SystemWideActionsListener.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.server.policy.fingersense.SystemWideActionsListener.<clinit>():void");
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
        this.FLOG_FLAG = true;
        this.isActionOnKeyboard = DEBUG;
        this.mServiceAquireLock = new Object();
        this.onKnockDownRunnable = new MotionEventRunnable() {
            public void run() {
                SystemWideActionsListener.this.notifyKnockDown(this.event);
            }
        };
        this.viewLock = new Object();
        this.viewAdd = DEBUG;
        this.context = context;
        this.phoneWindowManager = phoneWindowManager;
        this.activityManager = (ActivityManager) context.getSystemService("activity");
        this.windowManager = (WindowManager) context.getSystemService("window");
        this.mNavBarPortraitHeight = context.getResources().getDimensionPixelSize(17104920);
        this.mNavBarLandscapeHeight = context.getResources().getDimensionPixelSize(17104921);
        Display display = this.windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mDisplayWidth = Math.min(size.x, size.y);
        this.mDisplayHeight = Math.max(size.x, size.y);
        this.customGestureDetector = new CustomGestureDetector(context, KNUCKLE_GESTURES_PATH, (OnGesturePerformedListener) this);
        this.customGestureDetector.setMinPredictionScore(2.0f);
        this.customGestureDetector.setOrientationFixes(ORIENTATION_FIXES);
        this.customGestureDetector.setMinLineGestureStrokeLength(this.mDisplayWidth / STATE_RIGHT);
        this.customGestureDetector.setLineGestureStrokePortraitAngle(0.0f);
        this.customGestureDetector.setLineGestureStrokeLandscapeAngle(90.0f);
        this.customGestureDetector.setMaxLineGestureStrokeAngleDeviation(10.0f);
        this.customGestureDetector.setLineGestureStrokeStraightness(4.0f);
        this.knockGestureDetector = new KnockGestureDetector(context, this);
        this.hasKnuckleDownOccured = DEBUG;
        this.keyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        this.handler = new Handler();
        this.vibrator = (Vibrator) context.getSystemService("vibrator");
    }

    public void cancelSystemWideAction() {
        MotionEvent cancelEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 3, 0.0f, 0.0f, STATE_MIDDLE);
        this.customGestureDetector.onTouchEvent(cancelEvent);
        if (this.pointerLocationView != null) {
            this.pointerLocationView.onTouchEvent(cancelEvent);
        }
        removePointerLocationView();
        this.hasKnuckleDownOccured = DEBUG;
        cancelEvent.recycle();
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0 || (action & Utils.MAXINUM_TEMPERATURE) == 5 || action == STATE_LEFT || (action & Utils.MAXINUM_TEMPERATURE) == 6 || action == 3 || action == STATE_RIGHT) {
            this.knockGestureDetector.onAnyTouchEvent(motionEvent);
            if (shouldProcessMotionEvent(motionEvent)) {
                if (motionEvent.getAction() == 0) {
                    StatisticalUtils.reportc(this.context, WifiProCommonDefs.TYEP_HAS_INTERNET);
                    this.onKnockDownRunnable.event = motionEvent;
                    this.handler.post(this.onKnockDownRunnable);
                    if (HWFLOW) {
                        Log.i(TAG, "FingerSense Down Event.");
                    }
                    this.hasKnuckleDownOccured = true;
                    saveScreenBitmap();
                }
                this.knockGestureDetector.onKnuckleTouchEvent(motionEvent);
                if (this.hasKnuckleDownOccured && this.knockGestureDetector.getKnucklePointerCount() < STATE_RIGHT) {
                    addPointerLocationView();
                    this.customGestureDetector.onTouchEvent(motionEvent);
                    if (this.pointerLocationView != null) {
                        this.pointerLocationView.onTouchEvent(motionEvent);
                    }
                }
                if (motionEvent.getAction() == STATE_LEFT || motionEvent.getAction() == 3) {
                    cancelSystemWideAction();
                }
            } else if (this.hasKnuckleDownOccured) {
                cancelSystemWideAction();
            }
        }
    }

    private static int getLazyState(Context context) {
        String str = Global.getString(context.getContentResolver(), "single_hand_mode");
        if (str == null || AppHibernateCst.INVALID_PKG.equals(str)) {
            return STATE_MIDDLE;
        }
        if (str.contains(LEFT)) {
            return STATE_LEFT;
        }
        if (str.contains(RIGHT)) {
            return STATE_RIGHT;
        }
        return STATE_MIDDLE;
    }

    private static boolean isLazyMode(Context context) {
        return getLazyState(context) == 0 ? DEBUG : true;
    }

    private static Rect getScreenshotRect(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        int state = getLazyState(context);
        if (STATE_LEFT == state) {
            return new Rect(STATE_MIDDLE, (int) (((float) displayMetrics.heightPixels) * 0.25f), (int) (((float) displayMetrics.widthPixels) * SCALE), displayMetrics.heightPixels);
        }
        if (STATE_RIGHT == state) {
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
        boolean areSystemWideActionsEnabled = true;
        for (int i = STATE_MIDDLE; i < motionEvent.getPointerCount(); i += STATE_LEFT) {
            if (motionEvent.getToolType(i) != 7) {
                return DEBUG;
            }
        }
        if (this.FLOG_FLAG) {
            this.FLOG_FLAG = DEBUG;
            Flog.i(1503, "Get event type TOOL_TYPE_FINGER_KNUCKLE!");
        }
        int inputWindowHeight = this.phoneWindowManager.getInputMethodWindowVisibleHeightLw();
        if (inputWindowHeight > 0 && motionEvent.getY() > ((float) (this.phoneWindowManager.getRestrictedScreenHeight() - inputWindowHeight)) && motionEvent.getAction() == 0) {
            this.isActionOnKeyboard = true;
        }
        if (this.isActionOnKeyboard) {
            if (motionEvent.getAction() == STATE_LEFT) {
                this.isActionOnKeyboard = DEBUG;
            }
            return DEBUG;
        }
        WindowState windowState = this.phoneWindowManager.getFocusedWindow();
        if (!(windowState == null || (windowState.getAttrs().hwFlags & 4) == 0)) {
            areSystemWideActionsEnabled = DEBUG;
        }
        return areSystemWideActionsEnabled;
    }

    private void saveScreenBitmap() {
        recycleScreenshot();
        Rect sourceCrop = getScreenshotRect(this.context);
        if (isLazyMode(this.context)) {
            this.mScreenshotBitmap = SurfaceControl.screenshot(sourceCrop, this.mDisplayWidth, this.mDisplayHeight, STATE_MIDDLE, -1, DEBUG, STATE_MIDDLE);
        } else {
            this.mScreenshotBitmap = SurfaceControl.screenshot(this.mDisplayWidth, this.mDisplayHeight);
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
        StatisticalUtils.reportc(this.context, IOTController.EV_CANCEL_AUTH_ALL);
        if (this.mScreenshotBitmap == null) {
            Log.w(TAG, "SystemWideActionsListener failed to take Screenshot.");
            return;
        }
        intent.putExtra(EXTRA_SCREENSHOT_BITMAP, this.mScreenshotBitmap.copy(Config.ARGB_8888, DEBUG));
        recycleScreenshot();
        dismissKeyguardIfCurrentlyShown();
        intent.setPackage("com.qeexo.smartshot");
        this.context.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }

    public void onLetterGesture(String gestureName, Gesture gesture, double predictionScore) {
        if ("s".equals(gestureName.toLowerCase()) && FingerSenseSettings.isFingerSenseSmartshotEnabled(this.context.getContentResolver())) {
            Intent startIntent = FingerSenseSettings.getIntentForMultiScreenShot(this.context);
            if (startIntent != null) {
                try {
                    this.context.startServiceAsUser(startIntent, ActivityManagerNative.getDefault().getCurrentUser().getUserHandle());
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
        if (!gestureName.toLowerCase().equals("s")) {
            Log.d(TAG, "Fingersense Letter Gesture " + gestureName);
            StatisticalUtils.reportc(this.context, CPUFeature.MSG_MOVETO_BACKGROUND);
        }
        if (intent.getComponent() != null && CAMERA_PACKAGE_NAME.equals(intent.getComponent().getPackageName()) && this.keyguardManager != null && this.keyguardManager.isKeyguardLocked() && this.keyguardManager.isKeyguardSecure()) {
            intent = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").setPackage(CAMERA_PACKAGE_NAME).addFlags(8388608);
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
            StatisticalUtils.reportc(this.context, WifiProCommonUtils.HISTORY_TYPE_PORTAL);
            RectF boundingBox = gesture.getBoundingBox();
            if (this.context.getResources().getConfiguration().orientation == STATE_RIGHT) {
                orientedDistance = boundingBox.centerX();
                navBarHeight = this.mNavBarLandscapeHeight;
            } else {
                orientedDistance = boundingBox.centerY();
                navBarHeight = this.mNavBarPortraitHeight;
            }
            if (!this.phoneWindowManager.isNavigationBarVisible()) {
                navBarHeight = STATE_MIDDLE;
            }
            int availableScreenSize = this.mDisplayHeight - navBarHeight;
            int minWindowSize = (availableScreenSize * SPLIT_SCREEN_MIN_SCREEN_HEIGHT_PERCENTAGE) / 100;
            if (orientedDistance <= ((float) minWindowSize) || orientedDistance >= ((float) (availableScreenSize - minWindowSize))) {
                Log.w(TAG, "Line Gesture recognized, but too close to screen edge.");
            } else if (!this.keyguardManager.isKeyguardLocked()) {
                try {
                    if (WindowManagerGlobal.getWindowManagerService().getDockedStackSide() == -1) {
                        toggleSplitScreenByLineGesture(CPUFeature.MSG_BOOST_KILL_SWITCH, "toggleSplitScreenByLineGesture", (int) boundingBox.centerX(), (int) boundingBox.centerY());
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
        } else if (this.keyguardManager.inKeyguardRestrictedInputMode()) {
            Flog.w(1503, "On lock screen SmartShot Disabled!");
        } else {
            Flog.i(1503, "FingerSense Double Knuckle Double Knock");
            StatisticalUtils.reportc(this.context, CPUFeature.MSG_BOOST_KILL_SWITCH);
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
            StatisticalUtils.reportc(this.context, WifiProCommonUtils.HISTORY_TYPE_EMPTY);
            while (this.pointerLocationView.isShown()) {
                removePointerLocationView();
                Flog.i(1503, "pointerLocationView isShown(), removing...");
            }
            this.phoneWindowManager.takeScreenshot(STATE_LEFT);
        } else {
            Flog.i(1503, "Cover is closed.");
        }
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
                layoutParams.privateFlags |= STATE_RIGHT;
            }
            this.layoutParams.format = -3;
            this.layoutParams.setTitle("KnucklePointerLocationView");
            layoutParams = this.layoutParams;
            layoutParams.inputFeatures |= STATE_RIGHT;
        }
    }

    private void addPointerLocationView() {
        boolean isVRMode = DEBUG;
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
                this.viewAdd = DEBUG;
            }
        }
    }

    private void toast(String message, boolean vibrate) {
        Toast.makeText(this.context, message, STATE_MIDDLE).show();
        if (vibrate) {
            vibrate(new long[]{0, 20, 100, 20});
        }
    }

    private void vibrate(long[] pattern) {
        this.vibrator.vibrate(pattern, -1);
    }

    private void dismissKeyguardIfCurrentlyShown() {
        if (this.keyguardManager.inKeyguardRestrictedInputMode()) {
            this.phoneWindowManager.dismissKeyguardLw();
        }
    }

    private boolean checkSameAsRunningTask(Intent intent) {
        ComponentName componentRunning = ((RunningTaskInfo) this.activityManager.getRunningTasks(STATE_LEFT).get(STATE_MIDDLE)).topActivity;
        ComponentName componentLaunching = intent.getComponent();
        if (!(componentRunning == null || componentLaunching == null)) {
            String componentRunningString = componentRunning.toString();
            String componentLaunchingString = componentLaunching.toString();
            if (!(componentRunningString == null || componentLaunchingString == null || !componentLaunchingString.equals(componentRunningString))) {
                Log.w(TAG, "not starting intent" + componentLaunchingString + " because it is already running!");
                return true;
            }
        }
        return DEBUG;
    }

    private String getAppName(String packageName) {
        CharSequence applicationLabel;
        PackageManager pm = this.context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = pm.getApplicationInfo(packageName, STATE_MIDDLE);
        } catch (NameNotFoundException e) {
        }
        if (applicationInfo != null) {
            applicationLabel = pm.getApplicationLabel(applicationInfo);
        } else {
            Object obj = packageName;
        }
        return (String) applicationLabel;
    }

    public void updateConfiguration() {
        Display display = this.windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        this.mDisplayWidth = Math.min(size.x, size.y);
        this.mDisplayHeight = Math.max(size.x, size.y);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    statusBarServiceBinder.transact(code, data, reply, STATE_MIDDLE);
                }
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }
}
