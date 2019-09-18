package huawei.com.android.internal.policy;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import com.android.internal.policy.IPressGestureDetector;

public class PressGestureDetector implements IPressGestureDetector {
    private static final int COUNTS_FINGER_ONE = 1;
    private static final int COUNTS_FINGER_TWO = 2;
    private static final int COUNTS_FINGER_ZERO = 0;
    private static final long DEFAULT_GESTURE_TIME_OUT_LIMIT = 1100;
    private static final int POINTERS_MIN_DISTANCE_DP = 16;
    private static final int SCREEN_POINTER_BOTTOM_MARGIN_DP = 41;
    private static final int SCREEN_POINTER_MARGIN_DP = 24;
    private static final int SCREEN_POINTER_TOP_MARGIN_DP = 80;
    private static final String TAG = "HiTouch_PressGestureDetector";
    private static final String TALK_BACK = "talkback";
    private static final float TOUCH_MOVE_LIMIT_X = 20.0f;
    private static final float TOUCH_MOVE_LIMIT_Y = 20.0f;
    private static final long TOUCH_TWO_FINGERS_TIME_OUT_LIMIT = 150;
    private boolean isMoveTooMuch = false;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final Context mContextActivity;
    private final FrameLayout mDecorView;
    private int mDisplayHeigh = 0;
    private float mDisplayScale = 0.0f;
    private int mDisplayWidth = 0;
    private boolean mGestureInterrupted;
    private final Handler mHandler;
    private boolean mHasTriggered;
    /* access modifiers changed from: private */
    public boolean mHiTouchRestricted;
    private boolean mIsPhoneLongClickSwipe = false;
    /* access modifiers changed from: private */
    public float mLongPressDownX = 0.0f;
    /* access modifiers changed from: private */
    public float mLongPressDownY = 0.0f;
    /* access modifiers changed from: private */
    public float mLongPressPointerDownX = 0.0f;
    /* access modifiers changed from: private */
    public float mLongPressPointerDownY = 0.0f;
    /* access modifiers changed from: private */
    public final HiTouchSensor mSensor;
    private boolean mSensorRegistered = false;
    private boolean mStatus;
    private boolean mStatusChecked;
    private long mTouchDownTime;
    private long mTouchPointerDownTime;
    private final int mTouchSlop;

    public PressGestureDetector(Context context, FrameLayout docerView, Context contextActivity) {
        this.mContext = context;
        this.mContextActivity = contextActivity;
        this.mDecorView = docerView;
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mSensor = new HiTouchSensor(this.mContext, this.mContextActivity);
        this.mHandler = new Handler();
        updateDisplayParameters();
    }

    public boolean isLongPressSwipe() {
        return this.mIsPhoneLongClickSwipe;
    }

    public void onAttached(final int windowType) {
        new Thread() {
            public void run() {
                boolean unused = PressGestureDetector.this.mHiTouchRestricted = PressGestureDetector.this.mSensor.isUnsupportScence(windowType);
                Log.d(PressGestureDetector.TAG, "onAttached, package=" + PressGestureDetector.this.mContext.getPackageName() + ", windowType=" + windowType + ", mHiTouchRestricted=" + PressGestureDetector.this.mHiTouchRestricted);
            }
        }.start();
    }

    public void onDetached() {
        if (!this.mHiTouchRestricted) {
            if (this.mSensorRegistered) {
                this.mSensor.unregisterObserver();
                this.mSensorRegistered = false;
            }
            resetSwipeFlag();
        }
    }

    public void handleBackKey() {
        if (!this.mHiTouchRestricted) {
            resetSwipeFlag();
        }
    }

    public void handleConfigurationChanged(Configuration newConfig) {
        updateDisplayParameters();
        this.mSensor.onConfigurationChanged(newConfig);
    }

    public boolean dispatchTouchEvent(MotionEvent ev, boolean isHandling) {
        if (isHandling || this.mHiTouchRestricted || !this.mSensor.checkDeviceProvisioned()) {
            return false;
        }
        if (this.mDecorView.getParent() != this.mDecorView.getViewRootImpl()) {
            hwLog(TAG, "mDecorView.getParent(): " + this.mDecorView.getParent() + " mDecorView.getViewRootImpl(): " + this.mDecorView.getViewRootImpl());
            resetSwipeFlag();
            return false;
        }
        switch (ev.getAction() & 255) {
            case 0:
                hwLog(TAG, "ACTION_DOWN.");
                this.mHasTriggered = false;
                this.isMoveTooMuch = false;
                this.mGestureInterrupted = false;
                this.mIsPhoneLongClickSwipe = false;
                int actionIndexDown = ev.getActionIndex();
                this.mLongPressDownX = ev.getX(actionIndexDown);
                this.mLongPressDownY = ev.getY(actionIndexDown);
                this.mTouchDownTime = SystemClock.uptimeMillis();
                break;
            case 1:
                hwLog(TAG, "ACTION_UP");
                resetSwipeFlag();
                break;
            case 2:
                if (!this.mHasTriggered && !this.isMoveTooMuch) {
                    checkMoveDistance(Math.abs(this.mLongPressDownX - ev.getX()), Math.abs(this.mLongPressDownY - ev.getY()), 20.0f, 20.0f);
                    if (!this.mGestureInterrupted) {
                        if (ev.getPointerCount() == 2) {
                            float mainPointX = ev.getX(0);
                            float mainPointY = ev.getY(0);
                            float secondPointX = ev.getX(1);
                            float secondPointY = ev.getY(1);
                            this.mIsPhoneLongClickSwipe = true;
                            if (!checkIfPointsOutOfDecorView(mainPointX, mainPointY, secondPointX, secondPointY)) {
                                checkMoveDistance(Math.abs(this.mLongPressDownX - mainPointX), Math.abs(this.mLongPressDownY - mainPointY), 20.0f, 20.0f);
                                checkMoveDistance(Math.abs(this.mLongPressPointerDownX - secondPointX), Math.abs(this.mLongPressPointerDownY - secondPointY), 20.0f, 20.0f);
                                if (!checkPointsLocation(mainPointX, mainPointY, secondPointX, secondPointY)) {
                                    this.mGestureInterrupted = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        resetSwipeFlag();
                        break;
                    }
                }
                break;
            case 3:
                this.mGestureInterrupted = true;
                hwLog(TAG, "HiTouch ACTION_CANCEL.");
                resetSwipeFlag();
                break;
            case 5:
                hwLog(TAG, "ACTION_POINTER_DOWN. pointer count:" + ev.getPointerCount());
                this.mStatusChecked = false;
                this.mIsPhoneLongClickSwipe = false;
                if (ev.getPointerCount() <= 2) {
                    if (ev.getPointerCount() == 2) {
                        int actionIndexPointerDown = ev.getActionIndex();
                        this.mLongPressPointerDownX = ev.getX(actionIndexPointerDown);
                        this.mLongPressPointerDownY = ev.getY(actionIndexPointerDown);
                        this.mTouchPointerDownTime = SystemClock.uptimeMillis();
                    }
                    long intervalTwoFingers = Math.abs(this.mTouchPointerDownTime - this.mTouchDownTime);
                    if (intervalTwoFingers <= TOUCH_TWO_FINGERS_TIME_OUT_LIMIT) {
                        if (!this.mStatusChecked) {
                            this.mStatus = this.mSensor.getStatus();
                            this.mStatusChecked = true;
                            hwLog(TAG, "HiTouch mStatus: " + this.mStatus);
                        }
                        registerSensorObserver();
                        if (checkOnePointPosition(this.mLongPressDownX, this.mLongPressDownY) && checkOnePointPosition(this.mLongPressPointerDownX, this.mLongPressPointerDownY)) {
                            if (!checkPointersTooClose(this.mLongPressDownX, this.mLongPressDownY, this.mLongPressPointerDownX, this.mLongPressPointerDownY)) {
                                if (!checkIfPointsOutOfDecorView(this.mLongPressDownX, this.mLongPressDownY, this.mLongPressPointerDownX, this.mLongPressPointerDownY)) {
                                    startCountingTime();
                                    break;
                                }
                            } else {
                                this.mGestureInterrupted = true;
                                break;
                            }
                        } else {
                            this.mGestureInterrupted = true;
                            break;
                        }
                    } else {
                        Log.w(TAG, "HiTouch Miss: Too large time interval(TwoFingers), " + intervalTwoFingers);
                        this.mGestureInterrupted = true;
                        break;
                    }
                } else {
                    Log.w(TAG, "HiTouch Miss: more than two pointers.");
                    this.mGestureInterrupted = true;
                    break;
                }
                break;
            case 6:
                this.mGestureInterrupted = true;
                resetSwipeFlag();
                hwLog(TAG, "HiTouch ACTION_POINTER_UP. Finger Count:" + ev.getPointerCount());
                break;
        }
        checkTriggerCondition();
        return false;
    }

    private void resetSwipeFlag() {
        this.mIsPhoneLongClickSwipe = false;
    }

    private boolean checkIfPointsOutOfDecorView(float x1, float y1, float x2, float y2) {
        if (!checkIfOutOfDecorView(x1, y1) && !checkIfOutOfDecorView(x2, y2)) {
            return false;
        }
        this.mGestureInterrupted = true;
        Log.w(TAG, "HiTouch Miss: point OUT of DecorView");
        return true;
    }

    private boolean checkIfOutOfDecorView(float x, float y) {
        if (!this.mDecorView.pointInView(x, y, (float) this.mTouchSlop)) {
            return true;
        }
        return false;
    }

    private void checkMoveDistance(float horizontalMove, float verticalMove, float horizontalLimit, float verticalLimit) {
        int horizontalLimitPx = dp2px(horizontalLimit);
        int verticalLimitPx = dp2px(verticalLimit);
        if (horizontalMove > ((float) horizontalLimitPx) || verticalMove > ((float) verticalLimitPx)) {
            Log.w(TAG, "Touch pointer move a lot. The moving distance of X is:" + horizontalMove + ", limit is:" + horizontalLimitPx + "The moving distance of Y is:" + verticalMove + ", limit is:" + verticalLimitPx);
            this.isMoveTooMuch = true;
        }
    }

    private void startCountingTime() {
        long actualTriggerTime = DEFAULT_GESTURE_TIME_OUT_LIMIT - (SystemClock.uptimeMillis() - this.mTouchDownTime);
        hwLog(TAG, "StandardTriggerTime:1100,actualTriggerTime:" + actualTriggerTime);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                boolean unused = PressGestureDetector.this.triggerLaunchHiTouch(PressGestureDetector.this.mLongPressDownX, PressGestureDetector.this.mLongPressDownY, PressGestureDetector.this.mLongPressPointerDownX, PressGestureDetector.this.mLongPressPointerDownY);
            }
        }, actualTriggerTime);
    }

    private void stopCountingTime() {
        this.mHandler.removeCallbacksAndMessages(null);
    }

    /* access modifiers changed from: private */
    public boolean triggerLaunchHiTouch(float x1, float y1, float x2, float y2) {
        Log.i(TAG, "launchHiTouch");
        this.mSensor.launchHiTouchService(x1, y1, x2, y2, 0);
        this.mHasTriggered = true;
        return true;
    }

    private void checkTriggerCondition() {
        if (this.mGestureInterrupted || this.isMoveTooMuch || !this.mStatus) {
            stopCountingTime();
        }
    }

    public double getDistance(float x1, float y1, float x2, float y2) {
        float distanceX = Math.abs(x1 - x2);
        float distanceY = Math.abs(y1 - y2);
        return Math.sqrt((double) ((distanceX * distanceX) + (distanceY * distanceY)));
    }

    private int dp2px(float dpValue) {
        return (int) ((this.mDisplayScale * dpValue) + 0.5f);
    }

    private int px2dp(float pxValue) {
        if (this.mDisplayScale == 0.0f) {
            return 0;
        }
        return (int) ((pxValue / this.mDisplayScale) + 0.5f);
    }

    private boolean checkPointsLocation(float pointX1, float pointY1, float pointX2, float pointY2) {
        if (!checkOnePointPosition(pointX1, pointY1) || !checkOnePointPosition(pointX2, pointY2)) {
            Log.w(TAG, "Points are out of bound");
            return false;
        } else if (checkPointersTooClose(pointX1, pointY1, pointX2, pointY2)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkPointersTooClose(float pointX1, float pointY1, float pointX2, float pointY2) {
        int minDistance = dp2px(16.0f);
        if (getDistance(pointX1, pointY1, pointX2, pointY2) >= ((double) minDistance)) {
            return false;
        }
        Log.w(TAG, "HiTouch pointers are too close. The minDistance is " + minDistance);
        return true;
    }

    private boolean checkOnePointPosition(float pointX, float pointY) {
        int margin = dp2px(24.0f);
        int topMargin = dp2px(80.0f);
        int bottomMargin = dp2px(41.0f);
        if (pointX < ((float) margin) || pointX > ((float) (this.mDisplayWidth - margin))) {
            Log.w(TAG, "X Point is out of bound. Left: " + margin + " Right:" + (this.mDisplayWidth - margin));
            return false;
        } else if (pointY >= ((float) topMargin) && pointY <= ((float) (this.mDisplayHeigh - bottomMargin))) {
            return true;
        } else {
            Log.w(TAG, "Y Point is out of bound. Top: " + topMargin + " Bottom:" + (this.mDisplayHeigh - bottomMargin));
            return false;
        }
    }

    private void updateDisplayParameters() {
        DisplayMetrics dm = this.mContextActivity.getResources().getDisplayMetrics();
        this.mDisplayScale = dm.density;
        this.mDisplayWidth = dm.widthPixels;
        this.mDisplayHeigh = dm.heightPixels;
        if (!SystemProperties.get("ro.config.hw_notch_size", "").trim().isEmpty()) {
            int notchHeight = this.mContextActivity.getResources().getDimensionPixelSize(17105318);
            if (this.mContextActivity.getResources().getConfiguration().orientation == 1) {
                this.mDisplayHeigh += notchHeight;
                hwLog(TAG, "HiTouch on notch display, height corret:" + notchHeight);
                return;
            }
            this.mDisplayWidth += notchHeight;
            hwLog(TAG, "HiTouch on notch display, width corret:" + notchHeight);
        }
    }

    private void registerSensorObserver() {
        if (!this.mSensorRegistered) {
            this.mSensor.registerObserver();
            this.mSensorRegistered = true;
        }
    }

    private void hwLog(String tag, String msg) {
        if (Log.HWLog) {
            Log.i(tag, msg);
        }
    }
}
