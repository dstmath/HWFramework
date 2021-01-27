package com.android.server.gesture;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import com.android.server.gesture.GestureUtils;
import com.android.server.input.HwInputManagerService;
import com.android.server.input.InputManagerServiceEx;
import com.android.server.input.InputScaleConfiguration;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.hwdockbar.IDockAidlInterface;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import huawei.com.android.server.policy.HwFalseTouchMonitor;
import java.util.ArrayList;

public class GestureNavBaseStrategy {
    private static final float FIRST_PENDING_PROCESS = 0.2f;
    public static final int GESTURE_FAIL_REASON_ANGLE_TOO_LARGE = 2;
    public static final int GESTURE_FAIL_REASON_FORCE_CANCEL = 7;
    public static final int GESTURE_FAIL_REASON_INVALID_EVENT = 8;
    public static final int GESTURE_FAIL_REASON_MULTI_TOUCH = 6;
    public static final int GESTURE_FAIL_REASON_SLIDE_CANCEL = 5;
    public static final int GESTURE_FAIL_REASON_SLIDE_TOO_SHORT = 4;
    public static final int GESTURE_FAIL_REASON_TIMEOUT = 1;
    public static final int GESTURE_FAIL_REASON_UNKNOWN = 0;
    public static final int GESTURE_FAIL_REASON_UP_IN_REGION = 3;
    private static final int MAX_PENDING_DATA_SIZE = 30;
    private static final int MSG_COMPENSATE_MOVE_EVENT = 2;
    private static final int MSG_COMPENSATE_SINGLE_EVENT = 1;
    private static final int MSG_FAST_SLIDING_PRECHECK_TIMEOUT = 4;
    private static final int MSG_SEND_KEY_EVENT = 3;
    private static final int MSG_SEND_SHOW_DOCK = 99;
    private static final int PENDING_DATA_SIZE_TOO_SMALL = 4;
    private static final float SCATTER_PROCESS_THRESHOLD = 0.5f;
    private static final int SLIDE_EXCEED_MAJOR_AXIS_THRESHOLD = 1;
    private static final int SLIDE_EXCEED_MINOR_AXIS_THRESHOLD = 2;
    private static final int SLIDE_NOT_EXCEED_THRESHOLD = 0;
    protected static final Object TIMER_LOCK = new Object();
    private static final int USE_MOVE_OUT_TIME_DEFAULT_THRESHOLD = 0;
    private static final int USE_MOVE_OUT_TIME_MAX_THRESHOLD = 2;
    private static final int USE_MOVE_OUT_TIME_MIN_THRESHOLD = 1;
    protected static IDockAidlInterface sDockService;
    private ArrayList<GestureUtils.PointerState> mAbnormalTouchPointers;
    private int mBackDistance;
    private int mButtonState;
    protected Context mContext;
    private int mDeviceId;
    protected int mDisplayHeight;
    protected int mDisplayWidth;
    protected final float mFastVelocityThreshold;
    private int mFirstPointerId;
    private float mGestureAcceleration;
    protected int mGestureFailedReason;
    private int mGestureMoveMinDistance;
    private float mGestureSpeed;
    private int mGestureStayTimeThrehold;
    private boolean mHasCheckAngle;
    private boolean mHasCheckMinorSlideOut;
    private boolean mHasCheckSamePoint;
    private boolean mHasCheckTimeout;
    private boolean mHasCompensateBeforeUp;
    private int mHomeSwipeThreshold;
    private HwInputManagerService.HwInputManagerLocalService mHwInputManagerInternal;
    private boolean mIsCheckDistanceY;
    private boolean mIsDescAccelerate;
    private boolean mIsDockGesture;
    private boolean mIsDockGestureReady;
    private boolean mIsFastSlideMajorAxisChecking;
    private boolean mIsFastSlidePreCheckingTimeout;
    private boolean mIsFirstPointerUpInMiddle;
    private boolean mIsFirstTimeToNotify;
    protected boolean mIsGestureEnd;
    protected boolean mIsGestureFailed;
    protected boolean mIsGestureSlowProcessStarted;
    protected boolean mIsGestureSuccessFinished;
    protected boolean mIsGuestureReallyStarted;
    protected boolean mIsHorizontalSwitch;
    protected boolean mIsInHomeOfLauncher;
    private boolean mIsPointMoved;
    protected boolean mIsShowDockPreCondSatisfied;
    protected boolean mIsSubScreenGestureNav;
    protected long mLastAccelerationTime;
    private float mLastPendingDistance;
    protected float mLastSpeed;
    protected long mLastTime;
    protected float mLastX;
    protected float mLastY;
    protected Looper mLooper;
    private int mMaxPointCount;
    protected final int mMaximumVelocity;
    private int mMetaState;
    private int mMoveOutTimeThreshold;
    private int mMultiTouchThreshold;
    protected int mNavId;
    private ArrayList<GestureUtils.PointerState> mPendingDownUpPointers;
    private ArrayList<Float> mPendingMoveDistances;
    private ArrayList<PointF> mPendingMovePoints;
    protected int mRotation;
    private final InputScaleConfiguration mScaleConfig;
    private int mSlideOutThresholdMajorAxis;
    private int mSlideOutThresholdMinorAxis;
    private int mSlideUpThreshold;
    private int mSource;
    private long mStartMoveTime;
    protected float mStayX;
    private Handler mStrategyHandler;
    private int mToolType;
    protected float mTouchCurrentRawX;
    protected float mTouchCurrentRawY;
    protected float mTouchDownRawX;
    protected float mTouchDownRawY;
    private Rect mTouchDownRegion;
    protected long mTouchDownTime;
    protected long mTouchFailedTime;
    protected float mTouchUpRawX;
    protected float mTouchUpRawY;
    protected long mTouchUpTime;
    private boolean mUseProxyAngleStrategy;
    protected VelocityTracker mVelocityTracker;
    private int mWindowThreshold;
    private float mXOffset;
    private float mYOffset;

    /* access modifiers changed from: protected */
    public static final class PointEvent {
        public int action;
        public int buttonState;
        public int deviceId;
        public long downTime;
        public long durationTime;
        public float endX;
        public float endY;
        public int metaState;
        public int source;
        public float startX;
        public float startY;
        public int toolType;

        public PointEvent(float startX2, float startY2, long downTime2, int action2) {
            this.startX = startX2;
            this.startY = startY2;
            this.endX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.endY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.downTime = downTime2;
            this.durationTime = 0;
            this.action = action2;
        }

        public PointEvent(float startX2, float startY2, float endX2, float endY2, long time) {
            this.startX = startX2;
            this.startY = startY2;
            this.endX = endX2;
            this.endY = endY2;
            this.downTime = 0;
            this.durationTime = time;
            this.action = 0;
        }

        public void setEventState(int deviceId2, int source2, int toolType2, int buttonState2, int metaState2) {
            this.deviceId = deviceId2;
            this.source = source2;
            this.toolType = toolType2;
            this.buttonState = buttonState2;
            this.metaState = metaState2;
        }
    }

    public GestureNavBaseStrategy(int navId, Context context) {
        this(navId, context, Looper.getMainLooper());
    }

    public GestureNavBaseStrategy(int navId, Context context, Looper looper) {
        this.mLastSpeed = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mLastTime = 0;
        this.mLastY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mLastX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mLastAccelerationTime = 0;
        this.mStayX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        boolean z = false;
        this.mIsInHomeOfLauncher = false;
        this.mIsSubScreenGestureNav = false;
        this.mIsShowDockPreCondSatisfied = false;
        this.mTouchDownRegion = new Rect();
        this.mDeviceId = 0;
        this.mSource = 4098;
        this.mToolType = 0;
        this.mButtonState = 0;
        this.mMetaState = 0;
        this.mPendingMoveDistances = new ArrayList<>();
        this.mPendingMovePoints = new ArrayList<>();
        this.mPendingDownUpPointers = new ArrayList<>();
        this.mAbnormalTouchPointers = new ArrayList<>();
        this.mIsFirstTimeToNotify = false;
        this.mScaleConfig = new InputScaleConfiguration();
        this.mNavId = navId;
        this.mContext = context;
        this.mLooper = looper;
        this.mIsCheckDistanceY = this.mNavId == 3 ? true : z;
        this.mStrategyHandler = new StrategyHandler(looper);
        this.mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        this.mFastVelocityThreshold = ((float) ViewConfiguration.get(context).getScaledMinimumFlingVelocity()) * 10.0f;
        InputManagerServiceEx.DefaultHwInputManagerLocalService services = (InputManagerServiceEx.DefaultHwInputManagerLocalService) LocalServicesExt.getService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class);
        if (services instanceof HwInputManagerService.HwInputManagerLocalService) {
            this.mHwInputManagerInternal = (HwInputManagerService.HwInputManagerLocalService) services;
        }
        initDockParam();
    }

    private final class StrategyHandler extends Handler {
        StrategyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                GestureNavBaseStrategy.this.compensateSingleEvent((PointEvent) msg.obj);
            } else if (i == 2) {
                GestureNavBaseStrategy.this.checkCompensateEvent((PointEvent) msg.obj, null, null, 1, false);
            } else if (i == 3) {
                GestureUtils.sendKeyEvent(msg.arg1);
            } else if (i == 4) {
                GestureNavBaseStrategy.this.mIsFastSlidePreCheckingTimeout = true;
            } else if (i == GestureNavBaseStrategy.MSG_SEND_SHOW_DOCK) {
                GestureNavBaseStrategy.this.showDock();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendKeyEvent(int keycode) {
        sendKeyEvent(keycode, false);
    }

    /* access modifiers changed from: protected */
    public void sendKeyEvent(int keycode, boolean isAsync) {
        if (isAsync) {
            this.mStrategyHandler.sendMessage(this.mStrategyHandler.obtainMessage(3, keycode, 0, null));
            return;
        }
        GestureUtils.sendKeyEvent(keycode);
    }

    public void updateKeyguardState(boolean isKeyguardShowing) {
    }

    public void updateScreenConfigState(boolean isLand) {
    }

    public void updateNavTipsState(boolean isTipsEnable) {
    }

    public void onNavCreate(GestureNavView view) {
    }

    public void onNavUpdate() {
    }

    public void onNavDestroy() {
    }

    public void updateHorizontalSwitch() {
    }

    public void updateConfig(int displayWidth, int displayHeight, Rect rect, int rotation) {
        this.mDisplayWidth = displayWidth;
        this.mDisplayHeight = displayHeight;
        this.mTouchDownRegion.set(rect);
        this.mRotation = rotation;
        int backOffset = 0;
        int bottomOffset = 0;
        int gestureNavOffset = GestureNavConst.getGestureCurvedOffset(this.mContext);
        if (GestureUtils.isCurvedSideDisp()) {
            if (rotation != 0) {
                if (rotation == 1) {
                    bottomOffset = GestureUtils.getCurvedSideLeftDisp() + gestureNavOffset;
                } else if (rotation != 2) {
                    if (rotation == 3) {
                        bottomOffset = GestureUtils.getCurvedSideRightDisp() + gestureNavOffset;
                    }
                }
            }
            int i = this.mNavId;
            if (i == 1) {
                backOffset = GestureUtils.getCurvedSideLeftDisp() + gestureNavOffset;
            } else if (i == 2) {
                backOffset = GestureUtils.getCurvedSideRightDisp() + gestureNavOffset;
            }
        }
        this.mWindowThreshold = this.mIsCheckDistanceY ? this.mTouchDownRegion.height() - bottomOffset : this.mTouchDownRegion.width() - backOffset;
        this.mSlideOutThresholdMajorAxis = ((int) (((float) this.mWindowThreshold) * 1.2f)) + slipOutThresholdOffset();
        this.mSlideOutThresholdMinorAxis = ((int) (((float) this.mWindowThreshold) * 1.6f)) + slipOutThresholdOffset();
        this.mSlideUpThreshold = ((int) (((float) this.mWindowThreshold) * 2.0f)) + slipOverThresholdOffset();
        this.mMultiTouchThreshold = ((int) (((float) this.mWindowThreshold) * 3.0f)) + slipOverThresholdOffset();
        this.mHomeSwipeThreshold = GestureNavConst.convertDpToPixel(8.0f);
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "navId:" + this.mNavId + ", width:" + this.mDisplayWidth + ", height:" + this.mDisplayHeight + ", region:" + rect + ", slipOutMajor:" + this.mSlideOutThresholdMajorAxis + ", slipOutMinor:" + this.mSlideOutThresholdMinorAxis + ", slipUp:" + this.mSlideUpThreshold + ", windowThreshold:" + this.mWindowThreshold + ", rotation" + rotation);
        }
    }

    /* access modifiers changed from: protected */
    public Rect getRegion() {
        return this.mTouchDownRegion;
    }

    /* access modifiers changed from: protected */
    public boolean frameContainsPoint(float positionX, float positionY) {
        return this.mTouchDownRegion.contains((int) positionX, (int) positionY);
    }

    /* access modifiers changed from: protected */
    public int getWindowThreshold() {
        return this.mWindowThreshold;
    }

    /* access modifiers changed from: protected */
    public int slipOutThresholdOffset() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int slipOverThresholdOffset() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public int moveOutAngleThreshold() {
        return 70;
    }

    /* access modifiers changed from: protected */
    public int moveOutTimeThreshold(int useThreshold) {
        if (useThreshold == 1) {
            return 100;
        }
        if (useThreshold == 2 || this.mIsCheckDistanceY) {
            return GestureNavConst.GESTURE_MOVE_TIME_THRESHOLD_4;
        }
        if (this.mToolType == 2) {
            return 200;
        }
        return GestureNavConst.GESTURE_MOVE_TIME_THRESHOLD_1;
    }

    /* access modifiers changed from: protected */
    public int distanceExceedThreshold(float distanceX, float distanceY) {
        float distanceMajor = this.mIsCheckDistanceY ? distanceY : distanceX;
        float distanceMinor = this.mIsCheckDistanceY ? distanceX : distanceY;
        if (distanceMajor > ((float) slideOutThresholdMajorAxis())) {
            return 1;
        }
        if (distanceMinor > ((float) slideOutThresholdMinorAxis())) {
            return 2;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int slideOutThresholdMajorAxis() {
        return this.mSlideOutThresholdMajorAxis;
    }

    /* access modifiers changed from: protected */
    public int slideOutThresholdMinorAxis() {
        return this.mSlideOutThresholdMinorAxis;
    }

    /* access modifiers changed from: protected */
    public float fastVelocityTheshold() {
        return this.mIsCheckDistanceY ? 4500.0f : 6000.0f;
    }

    /* access modifiers changed from: protected */
    public int fastTimeoutTheshold() {
        if (this.mIsCheckDistanceY) {
            return GestureNavConst.GESTURE_MOVE_TIME_THRESHOLD_4;
        }
        return 200;
    }

    /* access modifiers changed from: protected */
    public void setUseProxyAngleStrategy(boolean isUseProxyAngleStrategy) {
        this.mUseProxyAngleStrategy = isUseProxyAngleStrategy;
    }

    /* access modifiers changed from: protected */
    public boolean shouldCheckAbnormalTouch() {
        return GestureUtils.isGameAppForeground();
    }

    /* access modifiers changed from: protected */
    public boolean shouldDropMultiTouch() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isMultiTouchBad(float activePointerRawX, float activePointerRawY) {
        float distance = diff(this.mTouchCurrentRawX, this.mTouchCurrentRawY, activePointerRawX, activePointerRawY);
        if (distance > ((float) this.mMultiTouchThreshold)) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "MultiTouch rawX=" + activePointerRawX + ", rawY=" + activePointerRawY + ", distance=" + distance);
            }
            return true;
        } else if (!this.mIsCheckDistanceY) {
            return false;
        } else {
            float moveDistance = absOffset(this.mTouchCurrentRawY - this.mTouchDownRawY);
            float pointerXDistance = absOffset(activePointerRawX - this.mTouchCurrentRawX);
            if (moveDistance <= ((float) this.mSlideUpThreshold) || pointerXDistance <= ((float) this.mMultiTouchThreshold)) {
                return false;
            }
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "MultiTouch move=" + moveDistance + ", xDis=" + pointerXDistance + ", dis=" + distance);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSlowProcessStarted() {
        return this.mIsGestureSlowProcessStarted;
    }

    public boolean onTouchEvent(MotionEvent event, boolean isFromSubScreenView) {
        int action = event.getAction();
        int i = action & GestureNavConst.VIBRATION_AMPLITUDE;
        if (i != 0) {
            if (i != 1) {
                if (i == 2) {
                    handleActionMove(event, action);
                } else if (i != 3) {
                    if (i == 5) {
                        handleMultiTouchDown(event, action);
                    } else if (i == 6) {
                        handleMultiTouchUp(event, action);
                    }
                }
            }
            handleActionUp(event, action);
        } else {
            this.mIsSubScreenGestureNav = isFromSubScreenView;
            handleActionDown(event, action);
        }
        return this.mIsGestureFailed;
    }

    private void handleActionDown(MotionEvent event, int action) {
        gestureReset();
        this.mTouchDownTime = event.getEventTime();
        this.mTouchDownRawX = event.getRawX();
        this.mTouchDownRawY = event.getRawY();
        this.mXOffset = event.getX() - this.mTouchDownRawX;
        float y = event.getY();
        float f = this.mTouchDownRawY;
        this.mYOffset = y - f;
        this.mTouchCurrentRawX = this.mTouchDownRawX;
        this.mTouchCurrentRawY = f;
        this.mLastY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mLastX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mStayX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mLastTime = this.mTouchDownTime;
        this.mLastSpeed = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mDeviceId = event.getDeviceId();
        this.mSource = event.getSource();
        this.mToolType = event.getToolType(0);
        this.mButtonState = event.getButtonState();
        this.mMetaState = event.getMetaState();
        this.mStartMoveTime = this.mTouchDownTime;
        this.mMoveOutTimeThreshold = moveOutTimeThreshold(2);
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "Down rawX=" + this.mTouchDownRawX + ", rawY=" + this.mTouchDownRawY + ", rect=" + getRegion() + ", navId=" + this.mNavId + ", xOffset=" + this.mXOffset + ", yOffset=" + this.mYOffset);
        }
        this.mMaxPointCount++;
        this.mFirstPointerId = GestureUtils.getActivePointerId(event, action);
        this.mPendingDownUpPointers.add(new GestureUtils.PointerState(this.mFirstPointerId, action, transformToRawInTp(this.mTouchDownRawX, this.mTouchDownRawY)));
        this.mVelocityTracker = VelocityTracker.obtain();
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(event);
        }
        onGestureStarted(this.mTouchDownRawX, this.mTouchDownRawY);
    }

    private void handleMultiTouchDown(MotionEvent event, int action) {
        int pointerId;
        this.mMaxPointCount++;
        if (!this.mIsGestureFailed && !this.mIsGuestureReallyStarted && (pointerId = GestureUtils.getActivePointerId(event, action)) == this.mFirstPointerId) {
            gestureFailed(8, action, ", invalid pointer:" + pointerId, event.getEventTime());
        }
        if (!this.mIsGestureFailed && !this.mIsGuestureReallyStarted) {
            PointF activePointer = recordPendingDownUpDatas(event, action, false);
            if (shouldDropMultiTouch() || isMultiTouchBad(activePointer.x, activePointer.y)) {
                gestureFailed(6, action, ", multi down before started", event.getEventTime());
            }
        }
        if (!this.mIsGestureFailed && this.mIsGuestureReallyStarted && event.getPointerCount() == 2 && shouldCheckAbnormalTouch()) {
            int actionIndex = GestureUtils.getActiveActionIndex(action);
            PointF activePointer2 = getRawPointCoords(event, actionIndex);
            if (shouldDropMultiTouch() || isMultiTouchBad(activePointer2.x, activePointer2.y)) {
                int activePointerId = event.getPointerId(actionIndex);
                int i = 0;
                while (true) {
                    if (i >= 2) {
                        break;
                    } else if (i != actionIndex) {
                        int downPointerId = event.getPointerId(i);
                        PointF downPointer = getRawPointCoords(event, i);
                        this.mAbnormalTouchPointers.add(new GestureUtils.PointerState(downPointerId, 0, transformToRawInTp(downPointer.x, downPointer.y)));
                        if (GestureNavConst.DEBUG) {
                            Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "downId:" + downPointerId + ", downX:" + downPointer.x + ", downY:" + downPointer.y + ", activeId:" + activePointerId + ", action:" + action);
                        }
                    } else {
                        i++;
                    }
                }
                this.mAbnormalTouchPointers.add(new GestureUtils.PointerState(activePointerId, action, transformToRawInTp(activePointer2.x, activePointer2.y)));
                gestureFailed(7, action, ", two pointers down after started", event.getEventTime());
            }
        }
    }

    private void handleMultiTouchUp(MotionEvent event, int action) {
        if (!this.mIsGestureFailed && !this.mIsGuestureReallyStarted) {
            recordPendingDownUpDatas(event, action, true);
        }
        if (this.mIsGestureFailed && this.mHasCompensateBeforeUp) {
            int count = event.getPointerCount();
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "Receive point up after gesture failed count:" + count);
            if (count == 2) {
                GestureUtils.sendPointerUp(event);
            }
        }
    }

    /* access modifiers changed from: protected */
    public float getRubberbandProcess(float distance) {
        return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    }

    private void handleActionMove(MotionEvent event, int action) {
        int slipOutMode;
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(event);
        }
        this.mTouchCurrentRawX = event.getRawX();
        this.mTouchCurrentRawY = event.getRawY();
        float offsetX = this.mTouchCurrentRawX - this.mTouchDownRawX;
        float offsetY = this.mTouchCurrentRawY - this.mTouchDownRawY;
        float distanceX = absOffset(offsetX);
        float distanceY = absOffset(offsetY);
        float distance = diff(distanceX, distanceY);
        if (GestureNavConst.DEBUG_ALL) {
            Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "Move rawX=" + this.mTouchCurrentRawX + ", rawY=" + this.mTouchCurrentRawY + ", distance=" + distance);
        }
        if (!this.mIsGestureFailed && !this.mIsGestureSlowProcessStarted && !this.mIsFirstPointerUpInMiddle) {
            recordPendingMoveDatas(event, distance, this.mTouchCurrentRawX, this.mTouchCurrentRawY);
        }
        if (this.mIsFastSlidePreCheckingTimeout && this.mIsFastSlideMajorAxisChecking) {
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "fast check timeout and move arrived");
            }
            this.mIsFastSlideMajorAxisChecking = false;
        }
        if (this.mIsGestureFailed || this.mHasCheckAngle) {
            slipOutMode = 0;
        } else {
            slipOutMode = distanceExceedThreshold(distanceX, distanceY);
            if (slipOutMode == 1) {
                checkAngleForActionMove(distanceX, distanceY, event, action, slipOutMode);
            }
        }
        checkTimeOut(distanceX, distanceY, event, action, slipOutMode);
        checkAllReady(distance, offsetX, offsetY);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x007b  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00d2  */
    private void checkAngleForActionMove(float distanceX, float distanceY, MotionEvent event, int action, int slipOutMode) {
        double angle;
        this.mHasCheckAngle = true;
        float velocity = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        long delayTime = 0;
        if (this.mIsHorizontalSwitch || this.mUseProxyAngleStrategy) {
            angle = 0.0d;
        } else {
            double angle2 = angle(distanceX, distanceY);
            angle = angle2;
            if (angle2 > ((double) moveOutAngleThreshold())) {
                gestureFailed(2, action, ", angle=" + angle, event.getEventTime());
                if (!GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "angle:" + angle + ", velocity:" + velocity + ", checkTimeout:" + this.mHasCheckTimeout + ", fastChecking:" + this.mIsFastSlideMajorAxisChecking + ", delayTime:" + delayTime + ", slipOutMode:" + slipOutMode + ", dx:" + distanceX + ", dy:" + distanceY);
                    return;
                }
                return;
            }
        }
        if (!this.mHasCheckTimeout) {
            this.mHasCheckTimeout = true;
            VelocityTracker vt = this.mVelocityTracker;
            if (vt != null) {
                vt.computeCurrentVelocity(HwFalseTouchMonitor.NoEffectClickChecker.CLICK_INTERVAL_TIMEOUT, (float) this.mMaximumVelocity);
                velocity = getVelocity(vt);
            }
            if (velocity > fastVelocityTheshold()) {
                delayTime = ((long) fastTimeoutTheshold()) - (event.getEventTime() - this.mStartMoveTime);
                if (delayTime > 0) {
                    this.mIsFastSlideMajorAxisChecking = true;
                    this.mStrategyHandler.sendEmptyMessageDelayed(4, delayTime);
                }
            }
        }
        if (!GestureNavConst.DEBUG) {
        }
    }

    private void checkTimeOut(float distanceX, float distanceY, MotionEvent event, int action, int slipOutMode) {
        String str;
        String str2;
        if (!this.mIsGestureFailed && !this.mHasCheckTimeout) {
            long eventTime = event.getEventTime();
            if (!this.mHasCheckSamePoint) {
                if (isPointChanged((int) distanceX, (int) distanceY)) {
                    this.mHasCheckSamePoint = true;
                    this.mStartMoveTime = eventTime;
                    this.mMoveOutTimeThreshold = moveOutTimeThreshold(0);
                    if (GestureNavConst.DEBUG) {
                        Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "point moved, threshold:" + this.mMoveOutTimeThreshold);
                    }
                }
            }
            if (!this.mHasCheckMinorSlideOut) {
                if (slipOutMode == 2) {
                    this.mHasCheckMinorSlideOut = true;
                    this.mStartMoveTime = this.mTouchDownTime;
                    this.mMoveOutTimeThreshold = moveOutTimeThreshold(1);
                    if (GestureNavConst.DEBUG) {
                        Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "slide in minor axis, threshold:" + this.mMoveOutTimeThreshold);
                    }
                }
            }
            long durationTime = eventTime - this.mStartMoveTime;
            if (durationTime > ((long) this.mMoveOutTimeThreshold)) {
                float distance = distance(distanceX, distanceY);
                this.mHasCheckTimeout = true;
                double angle = 0.0d;
                if ((this.mIsHorizontalSwitch || !frameContainsPoint(this.mTouchCurrentRawX, this.mTouchCurrentRawY)) && isPointMovedMinDistance(distance)) {
                    str2 = "ms, point(";
                    str = ", ";
                    if (!this.mHasCheckAngle) {
                        this.mHasCheckAngle = true;
                        double angle2 = angle(distanceX, distanceY);
                        if (this.mIsHorizontalSwitch || angle2 <= ((double) moveOutAngleThreshold())) {
                            angle = angle2;
                        } else {
                            angle = angle2;
                            gestureFailed(2, action, ", timeout angle=" + angle2, eventTime);
                        }
                    }
                } else {
                    str2 = "ms, point(";
                    str = ", ";
                    gestureFailed(1, action, ", time=" + durationTime + "ms, point(" + this.mTouchCurrentRawX + ", " + this.mTouchCurrentRawY + "), dist=" + distance, eventTime);
                }
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "move out time:" + durationTime + "ms, threshold:" + this.mMoveOutTimeThreshold + str2 + this.mTouchCurrentRawX + str + this.mTouchCurrentRawY + "), angle:" + angle);
                }
            }
        }
        if (this.mIsShowDockPreCondSatisfied) {
            boolean isDisLargeEnough = isPointMovedDockMinDistance(diff(distanceX, distanceY));
            if (event.getSource() == 8194) {
                switchAnimationForDockIfNeed(this.mIsDockGesture, isDisLargeEnough);
                if (this.mIsDockGesture || !isDisLargeEnough) {
                    this.mStrategyHandler.removeMessages(MSG_SEND_SHOW_DOCK);
                } else {
                    Message message = this.mStrategyHandler.obtainMessage(MSG_SEND_SHOW_DOCK);
                    if (!this.mStrategyHandler.hasMessages(MSG_SEND_SHOW_DOCK)) {
                        this.mStrategyHandler.sendMessageDelayed(message, (long) this.mGestureStayTimeThrehold);
                    }
                }
            } else {
                if (!this.mIsDockGesture && isDisLargeEnough) {
                    isGestureReady(event, distanceX);
                }
                switchAnimationForDockIfNeed(this.mIsDockGesture, this.mIsDockGestureReady);
                if (this.mIsDockGestureReady && !this.mIsDockGesture) {
                    showDock();
                }
            }
            if (this.mIsDockGestureReady) {
                float f = this.mStayX;
                if ((f > distanceX && Math.abs(f - distanceX) > ((float) this.mBackDistance)) || !isDisLargeEnough) {
                    switchAnimationForDockIfNeed(this.mIsDockGesture, isDisLargeEnough);
                    this.mStayX = distanceX;
                    this.mIsDockGestureReady = false;
                    this.mIsDescAccelerate = false;
                    this.mLastAccelerationTime = 0;
                    dismissDockBar();
                    this.mIsDockGesture = false;
                }
            }
        }
    }

    private void isGestureReady(MotionEvent event, float distanceX) {
        float speedY = (this.mTouchCurrentRawY - this.mLastY) / ((float) (event.getEventTime() - this.mLastTime));
        float speedX = (this.mTouchCurrentRawX - this.mLastX) / ((float) (event.getEventTime() - this.mLastTime));
        float speedNow = new Float(Math.sqrt((double) ((speedY * speedY) + (speedX * speedX)))).floatValue();
        if ((speedNow - this.mLastSpeed) / ((float) (event.getEventTime() - this.mLastTime)) >= this.mGestureAcceleration || speedNow >= this.mGestureSpeed || this.mStayX >= distanceX) {
            this.mLastAccelerationTime = event.getEventTime();
            this.mIsDescAccelerate = false;
        } else {
            if (!this.mIsDescAccelerate) {
                this.mLastAccelerationTime = event.getEventTime();
                this.mIsDescAccelerate = true;
            }
            if (event.getEventTime() - this.mLastAccelerationTime >= ((long) this.mGestureStayTimeThrehold)) {
                if (!this.mIsDockGestureReady) {
                    this.mStayX = distanceX;
                }
                this.mIsDockGestureReady = true;
            }
        }
        this.mLastSpeed = speedNow;
        this.mLastY = this.mTouchCurrentRawY;
        this.mLastX = this.mTouchCurrentRawX;
        this.mLastTime = event.getEventTime();
    }

    /* access modifiers changed from: protected */
    public void switchAnimationForDockIfNeed(boolean isDockShowing, boolean isDisLargeEnough) {
    }

    private void checkAllReady(float distance, float offsetX, float offsetY) {
        float firstPendingMoveProcess;
        if (!this.mIsGuestureReallyStarted && gestureReady()) {
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "gesture really started");
            }
            this.mIsGuestureReallyStarted = true;
            onGestureReallyStarted();
        }
        if (this.mIsGuestureReallyStarted && !this.mIsGestureFailed) {
            if (!this.mIsFirstTimeToNotify) {
                this.mIsFirstTimeToNotify = true;
                float process = getRubberbandProcess(distance);
                if (this.mPendingMoveDistances.isEmpty()) {
                    firstPendingMoveProcess = FIRST_PENDING_PROCESS;
                } else {
                    firstPendingMoveProcess = getRubberbandProcess(this.mPendingMoveDistances.get(0).floatValue());
                }
                Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "try to animate scatter process: process = " + process + ", fist pending moving process = " + firstPendingMoveProcess);
                if (process > SCATTER_PROCESS_THRESHOLD) {
                    onGestureAnimateScatterProcess(firstPendingMoveProcess, process);
                    return;
                }
            }
            if (!this.mIsGestureSlowProcessStarted) {
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "gesture slow process started");
                }
                this.mIsGestureSlowProcessStarted = true;
                onGestureSlowProcessStarted(this.mPendingMoveDistances);
            }
            onGestureSlowProcess(distance, offsetX, offsetY);
        }
    }

    private void handleActionUp(MotionEvent event, int action) {
        this.mStrategyHandler.removeMessages(4);
        this.mStrategyHandler.removeMessages(MSG_SEND_SHOW_DOCK);
        this.mTouchUpTime = event.getEventTime();
        this.mTouchUpRawX = event.getRawX();
        this.mTouchUpRawY = event.getRawY();
        this.mTouchCurrentRawX = this.mTouchUpRawX;
        this.mTouchCurrentRawY = this.mTouchUpRawY;
        float velocity = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(event);
            this.mVelocityTracker.computeCurrentVelocity(HwFalseTouchMonitor.NoEffectClickChecker.CLICK_INTERVAL_TIMEOUT, (float) this.mMaximumVelocity);
            velocity = getVelocity(this.mVelocityTracker);
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
        if (!this.mIsGestureFailed && !this.mIsGuestureReallyStarted) {
            recordPendingDownUpDatas(event, action, true);
        }
        if (this.mIsGestureFailed && action == 1 && this.mHasCompensateBeforeUp) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "Receive up after gesture failed");
            PointF rawInTp = transformToRawInTp(this.mTouchUpRawX, this.mTouchUpRawY);
            onGestureUpArrivedAfterFailed(rawInTp.x, rawInTp.y, this.mTouchDownTime);
        }
        if (!this.mIsGestureFailed && !this.mIsGuestureReallyStarted && ((!this.mIsHorizontalSwitch && frameContainsPoint(this.mTouchUpRawX, this.mTouchUpRawY)) || isNotMovedInHome())) {
            gestureFailed(3, action, BuildConfig.FLAVOR, this.mTouchUpTime);
        }
        float offsetDistance = offsetDistance(this.mTouchDownRawX, this.mTouchDownRawY, this.mTouchUpRawX, this.mTouchUpRawY);
        float distance = absOffset(offsetDistance);
        if (!this.mIsGestureFailed && !this.mIsGuestureReallyStarted && ((!this.mIsHorizontalSwitch && distance < ((float) getUpDistanceThreshold())) || isNotMovedInHome())) {
            gestureFailed(4, action, ", distance:" + distance, this.mTouchUpTime);
        }
        long durationTime = this.mTouchUpTime - this.mTouchDownTime;
        if (!this.mIsGestureFailed && this.mIsGuestureReallyStarted && ((offsetDistance < ((float) getUpDistanceThreshold()) && durationTime > 150) || action == 3)) {
            gestureFailed(5, action, ", duration:" + durationTime + "ms, offsetDistance:" + offsetDistance + ", action:" + action, this.mTouchUpTime);
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "Up rawX=" + this.mTouchUpRawX + ", rawY=" + this.mTouchUpRawY + ", failed=" + this.mIsGestureFailed + ", rellayStarted=" + this.mIsGuestureReallyStarted + ", velocity=" + velocity + ", offsetDistance=" + offsetDistance + ", duration=" + durationTime);
        }
        gestureFinished(distance, durationTime, velocity);
        gestureEnd(action);
    }

    private boolean isNotMovedInHome() {
        int i = this.mHomeSwipeThreshold;
        return this.mIsHorizontalSwitch && squaredHypot(this.mTouchDownRawX - this.mTouchUpRawX, this.mTouchDownRawY - this.mTouchUpRawY) < ((float) (i * i));
    }

    private float squaredHypot(float x, float y) {
        return (x * x) + (y * y);
    }

    private void gestureReset() {
        this.mGestureFailedReason = 0;
        this.mIsGestureFailed = false;
        this.mIsGuestureReallyStarted = false;
        this.mIsGestureSlowProcessStarted = false;
        this.mIsGestureSuccessFinished = false;
        this.mIsGestureEnd = false;
        this.mIsFirstTimeToNotify = false;
        this.mStrategyHandler.removeMessages(4);
        this.mIsFastSlidePreCheckingTimeout = false;
        this.mIsFastSlideMajorAxisChecking = false;
        this.mUseProxyAngleStrategy = false;
        this.mHasCheckAngle = false;
        this.mHasCheckTimeout = false;
        this.mHasCheckSamePoint = false;
        this.mHasCheckMinorSlideOut = false;
        this.mHasCompensateBeforeUp = false;
        this.mLastPendingDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mPendingMoveDistances.clear();
        this.mPendingMovePoints.clear();
        this.mIsFirstPointerUpInMiddle = false;
        this.mIsPointMoved = false;
        this.mMaxPointCount = 0;
        this.mPendingDownUpPointers.clear();
        this.mAbnormalTouchPointers.clear();
        this.mIsDockGesture = false;
        this.mIsDockGestureReady = false;
        this.mIsDescAccelerate = false;
        InputScaleConfiguration inputScaleConfiguration = this.mScaleConfig;
        HwInputManagerService.HwInputManagerLocalService hwInputManagerLocalService = this.mHwInputManagerInternal;
        inputScaleConfiguration.set(hwInputManagerLocalService != null ? hwInputManagerLocalService.getInputScaleConfig() : null);
    }

    private void gestureFailed(int reason, int action, String appendStr, long failedTime) {
        Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, translateFailedReason(reason) + appendStr);
        this.mGestureFailedReason = reason;
        this.mTouchFailedTime = failedTime;
        this.mIsGestureFailed = true;
        onGestureFailed(this.mGestureFailedReason, action);
    }

    private boolean gestureReady() {
        return !this.mIsGestureFailed && this.mHasCheckTimeout && this.mHasCheckAngle;
    }

    private void gestureFinished(float distance, long durationTime, float velocity) {
        if (!this.mIsGestureFailed) {
            boolean isFastSlideGesture = false;
            if (!this.mIsGestureSlowProcessStarted && (this.mIsFastSlideMajorAxisChecking || velocity > 6000.0f)) {
                isFastSlideGesture = true;
            }
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "gesture success, reallyStarted=" + this.mIsGuestureReallyStarted + ", slowStarted=" + this.mIsGestureSlowProcessStarted + ", velocity=" + velocity + ", fastChecking=" + this.mIsFastSlideMajorAxisChecking + ", fastGesture=" + isFastSlideGesture);
            }
            this.mIsGestureSuccessFinished = true;
            onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture, this.mIsDockGesture);
        }
    }

    private void gestureEnd(int action) {
        this.mIsGestureEnd = true;
        this.mPendingMoveDistances.clear();
        this.mPendingMovePoints.clear();
        this.mPendingDownUpPointers.clear();
        this.mAbnormalTouchPointers.clear();
        onGestureEnd(action);
    }

    /* access modifiers changed from: protected */
    public void onGestureStarted(float rawX, float rawY) {
    }

    /* access modifiers changed from: protected */
    public void onGestureReallyStarted() {
    }

    /* access modifiers changed from: protected */
    public void onGestureSlowProcessStarted(ArrayList<Float> arrayList) {
    }

    /* access modifiers changed from: protected */
    public void onGestureAnimateScatterProcess(float fromProcess, float toProcess) {
    }

    /* access modifiers changed from: protected */
    public void onGestureSlowProcess(float distance, float offsetX, float offsetY) {
    }

    /* access modifiers changed from: protected */
    public void onGestureFailed(int reason, int action) {
        if (!this.mIsSubScreenGestureNav) {
            switch (reason) {
                case 0:
                case 8:
                default:
                    return;
                case 1:
                case 2:
                    this.mHasCompensateBeforeUp = true;
                    PointF transformToRawInTp = transformToRawInTp(this.mTouchDownRawX, this.mTouchDownRawY);
                    ArrayList<PointF> arrayList = this.mPendingMovePoints;
                    long j = this.mTouchDownTime;
                    compensateBatchDownEvent(transformToRawInTp, arrayList, j, this.mTouchFailedTime - j, this.mDeviceId, this.mSource, this.mToolType);
                    return;
                case 3:
                case 4:
                    long durationTime = this.mTouchFailedTime - this.mTouchDownTime;
                    if (durationTime >= 500 || action == 3) {
                        Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "no need compensate as duration:" + durationTime + ", action:" + action);
                        return;
                    }
                    PointF rawDownInTp = transformToRawInTp(this.mTouchDownRawX, this.mTouchDownRawY);
                    PointF rawUpInTp = transformToRawInTp(this.mTouchUpRawX, this.mTouchUpRawY);
                    PointEvent pointEvent = new PointEvent(rawDownInTp.x, rawDownInTp.y, rawUpInTp.x, rawUpInTp.y, durationTime);
                    pointEvent.setEventState(this.mDeviceId, this.mSource, this.mToolType, this.mButtonState, this.mMetaState);
                    checkCompensateEvent(pointEvent, this.mPendingMovePoints, this.mPendingDownUpPointers, this.mMaxPointCount, this.mIsPointMoved);
                    return;
                case 5:
                    if (GestureNavConst.DEBUG) {
                        Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "no need compensate as gesture cancel");
                        return;
                    }
                    return;
                case 6:
                    this.mHasCompensateBeforeUp = true;
                    ArrayList<GestureUtils.PointerState> arrayList2 = this.mPendingDownUpPointers;
                    int i = this.mMaxPointCount;
                    long j2 = this.mTouchDownTime;
                    compensateMultiDownEvent(arrayList2, i, j2, this.mTouchFailedTime - j2, this.mDeviceId, this.mSource, this.mToolType);
                    return;
                case 7:
                    this.mHasCompensateBeforeUp = true;
                    ArrayList<GestureUtils.PointerState> arrayList3 = this.mAbnormalTouchPointers;
                    int i2 = this.mMaxPointCount;
                    long j3 = this.mTouchDownTime;
                    compensateMultiDownEvent(arrayList3, i2, j3, this.mTouchFailedTime - j3, this.mDeviceId, this.mSource, this.mToolType);
                    return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onGestureSuccessFinished(float distance, long durationTime, float velocity, boolean isFastSlideGesture, boolean isDockGesture) {
    }

    /* access modifiers changed from: protected */
    public void onGesturePreLoad() {
    }

    /* access modifiers changed from: protected */
    public void onGestureEnd(int action) {
    }

    /* access modifiers changed from: protected */
    public void rmvDockDeathRecipient() {
    }

    /* access modifiers changed from: protected */
    public void onGestureUpArrivedAfterFailed(float rawX, float rawY, long pointDownTime) {
        PointEvent pointEvent = new PointEvent(rawX, rawY, pointDownTime, 1);
        pointEvent.setEventState(this.mDeviceId, this.mSource, this.mToolType, this.mButtonState, this.mMetaState);
        this.mStrategyHandler.sendMessage(this.mStrategyHandler.obtainMessage(1, 0, 0, pointEvent));
    }

    /* access modifiers changed from: protected */
    public void dismissDockBar() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void compensateSingleEvent(PointEvent event) {
        long eventTime;
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "compensateSingleEvent x=" + event.startX + ", y=" + event.startY + ", action=" + event.action);
        }
        if (event.action == 0) {
            eventTime = event.downTime;
        } else {
            eventTime = SystemClock.uptimeMillis();
        }
        GestureUtils.injectMotionEvent(event.action, event.downTime, eventTime, event.startX, event.startY, event.deviceId, event.source, event.toolType, event.buttonState, event.metaState);
    }

    private void compensateBatchDownEvent(PointF startPoint, ArrayList<PointF> pendingMovePoints, long downTime, long durationTime, int deviceId, int source, int toolType) {
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "compensateBatchDownEvent x=" + startPoint.x + ", y=" + startPoint.y + ", durationTime=" + durationTime);
        }
        GestureUtils.injectDownWithBatchMoveEvent(downTime, startPoint.x, startPoint.y, pendingMovePoints, durationTime, deviceId, source, toolType);
    }

    private void compensateMultiDownEvent(ArrayList<GestureUtils.PointerState> pendingDownUps, int maxPointerCount, long downTime, long durationTime, int deviceId, int source, int toolType) {
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "compensateMultiDownEvent count=" + maxPointerCount + ", durationTime=" + durationTime);
        }
        GestureUtils.sendMultiPointerDown(pendingDownUps, maxPointerCount, deviceId, source, toolType, downTime, durationTime);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkCompensateEvent(PointEvent event, ArrayList<PointF> pendingMovePoints, ArrayList<GestureUtils.PointerState> pendingDownUps, int maxPointerCount, boolean isMultiPointMoved) {
        boolean hasMultiTouched = true;
        if (maxPointerCount <= 1) {
            hasMultiTouched = false;
        }
        if (!hasMultiTouched || isMultiPointMoved) {
            float distance = distance(event.startX, event.startY, event.endX, event.endY);
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "checkCompensateEvent distance=" + distance);
            }
            if (isPointMovedMinDistance(distance)) {
                compensateMoveEvent(event, pendingMovePoints, hasMultiTouched);
            } else {
                compensateTapEvent(event);
            }
        } else {
            GestureUtils.sendMultiPointerTap(pendingDownUps, maxPointerCount, event.deviceId, event.source, event.toolType);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDock() {
        onGesturePreLoad();
        this.mIsDockGesture = true;
    }

    private void compensateTapEvent(PointEvent event) {
        GestureUtils.sendTap(event.startX, event.startY, event.deviceId, event.source, event.toolType, event.buttonState, event.metaState);
    }

    private void compensateMoveEvent(PointEvent event, ArrayList<PointF> pendingMovePoints, boolean hasMultiTouched) {
        boolean canUsePendingPoints = true;
        if (pendingMovePoints == null) {
            canUsePendingPoints = false;
        } else if (pendingMovePoints.size() >= MAX_PENDING_DATA_SIZE || pendingMovePoints.size() < 4) {
            canUsePendingPoints = false;
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "pending points size:" + pendingMovePoints.size());
            }
        }
        GestureUtils.sendSwipe(event.startX, event.startY, event.endX, event.endY, (int) event.durationTime, event.deviceId, event.source, event.toolType, event.buttonState, event.metaState, canUsePendingPoints ? pendingMovePoints : null, hasMultiTouched);
    }

    private void recordPendingMoveDatas(MotionEvent event, float distance, float rawX, float rawY) {
        if (this.mPendingMoveDistances.size() < MAX_PENDING_DATA_SIZE) {
            int dist = (int) Math.abs(distance - this.mLastPendingDistance);
            int dt = 150;
            if (dist > 150) {
                int times = dist / 150;
                int restSize = 30 - this.mPendingMoveDistances.size();
                if (times > restSize) {
                    times = restSize;
                }
                if (distance <= this.mLastPendingDistance) {
                    dt = -150;
                }
                for (int i = 0; i < times; i++) {
                    this.mPendingMoveDistances.add(Float.valueOf(distance - ((float) ((times - i) * dt))));
                }
            }
            this.mPendingMoveDistances.add(Float.valueOf(distance));
            this.mLastPendingDistance = distance;
        }
        if (this.mPendingMovePoints.size() < MAX_PENDING_DATA_SIZE) {
            int historySize = event.getHistorySize();
            int restSize2 = MAX_PENDING_DATA_SIZE - this.mPendingMovePoints.size();
            if (historySize > restSize2) {
                historySize = restSize2;
            }
            for (int h = 0; h < historySize; h++) {
                PointF rawInTp = transformToRawInTp(event.getHistoricalX(h) - this.mXOffset, event.getHistoricalY(h) - this.mYOffset);
                this.mPendingMovePoints.add(rawInTp);
                if (GestureNavConst.DEBUG_ALL) {
                    Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "record rawInTp=" + rawInTp);
                }
            }
            this.mPendingMovePoints.add(transformToRawInTp(rawX, rawY));
        }
    }

    private PointF recordPendingDownUpDatas(MotionEvent event, int action, boolean isUp) {
        int actionIndex = GestureUtils.getActiveActionIndex(action);
        int activePointerId = event.getPointerId(actionIndex);
        PointF activePointerRawCoords = getRawPointCoords(event, actionIndex);
        PointF activePointerRawInTp = transformToRawInTp(activePointerRawCoords.x, activePointerRawCoords.y);
        if (isUp) {
            if (!this.mIsFirstPointerUpInMiddle && (action & GestureNavConst.VIBRATION_AMPLITUDE) == 6 && activePointerId == this.mFirstPointerId) {
                this.mIsFirstPointerUpInMiddle = true;
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "first pointer up, id=" + this.mFirstPointerId);
                }
            }
            if (!this.mIsPointMoved) {
                int size = this.mPendingDownUpPointers.size();
                for (int i = 0; i < size; i++) {
                    GestureUtils.PointerState ps = this.mPendingDownUpPointers.get(i);
                    if (ps.activePointerId == activePointerId && isDown(ps.action)) {
                        this.mIsPointMoved = isPointMovedMinDistance(ps.point.x, ps.point.y, activePointerRawCoords.x, activePointerRawCoords.y);
                        if (this.mIsPointMoved) {
                            break;
                        }
                    }
                }
            }
        }
        if (GestureNavConst.DEBUG_ALL) {
            Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "record id=" + activePointerId + ", action=" + Integer.toHexString(action) + ", rawX=" + activePointerRawCoords.x + ", rawY=" + activePointerRawCoords.y + ", moved=" + this.mIsPointMoved);
        }
        this.mPendingDownUpPointers.add(new GestureUtils.PointerState(activePointerId, action, activePointerRawInTp));
        return activePointerRawCoords;
    }

    private PointF getRawPointCoords(MotionEvent event, int actionIndex) {
        return new PointF(event.getX(actionIndex) - this.mXOffset, event.getY(actionIndex) - this.mYOffset);
    }

    private PointF transformToRawInTp(float rawX, float rawY) {
        float pointX = rawX;
        float pointY = rawY;
        if (!this.mIsSubScreenGestureNav && this.mScaleConfig.isInputScaleEnabled()) {
            int i = this.mScaleConfig.side;
            if (i == 1) {
                pointX = rawX * this.mScaleConfig.xScale;
                pointY = rawY * this.mScaleConfig.yScale;
            } else if (i == 2) {
                pointX = rawX * this.mScaleConfig.xScale;
                pointY = (this.mScaleConfig.yScale * rawY) + (((float) this.mDisplayHeight) * (1.0f - this.mScaleConfig.yScale));
            } else if (i == 3) {
                pointX = (this.mScaleConfig.xScale * rawX) + (((float) this.mDisplayWidth) * (1.0f - this.mScaleConfig.xScale));
                pointY = (this.mScaleConfig.yScale * rawY) + (((float) this.mDisplayHeight) * (1.0f - this.mScaleConfig.yScale));
            } else if (i == 4) {
                pointX = (this.mScaleConfig.xScale * rawX) + (((float) this.mDisplayWidth) * (1.0f - this.mScaleConfig.xScale));
                pointY = rawY * this.mScaleConfig.yScale;
            }
        }
        return new PointF(pointX, pointY);
    }

    private int getUpDistanceThreshold() {
        return (this.mMaxPointCount <= 1 || !this.mIsFirstPointerUpInMiddle) ? this.mSlideUpThreshold : this.mMultiTouchThreshold;
    }

    private static float distance(float startX, float startY, float endX, float endY) {
        float dx = startX - endX;
        float dy = startY - endY;
        return (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
    }

    private float absOffset(float offset) {
        return Math.abs(offset);
    }

    private float distance(float diffX, float diffY) {
        if (this.mIsHorizontalSwitch) {
            return (float) Math.sqrt((double) ((diffX * diffX) + (diffY * diffY)));
        }
        return this.mIsCheckDistanceY ? diffY : diffX;
    }

    private float offsetDistance(float startX, float startY, float endX, float endY) {
        int i = this.mNavId;
        if (i == 1) {
            return endX - startX;
        }
        if (i != 2) {
            return startY - endY;
        }
        return startX - endX;
    }

    private float diff(float diffX, float diffY) {
        return this.mIsCheckDistanceY ? diffY : diffX;
    }

    private float diff(float startX, float startY, float endX, float endY) {
        return absOffset(this.mIsCheckDistanceY ? endY - startY : endX - startX);
    }

    private double angle(float distanceX, float distanceY) {
        return GestureUtils.angle(distanceX, distanceY, this.mIsCheckDistanceY);
    }

    private float getVelocity(VelocityTracker velocityTracker) {
        return Math.abs(this.mIsCheckDistanceY ? velocityTracker.getYVelocity() : velocityTracker.getXVelocity());
    }

    private boolean isPointChanged(int distanceX, int distanceY) {
        return (distanceX == 0 && distanceY == 0) ? false : true;
    }

    private boolean isPointMovedMinDistance(float startX, float startY, float endX, float endY) {
        return isPointMovedMinDistance(distance(startX, startY, endX, endY));
    }

    private boolean isPointMovedMinDistance(float distance) {
        return distance > 15.0f;
    }

    private boolean isPointMovedDockMinDistance(float distance) {
        int i = this.mGestureMoveMinDistance;
        return i != 0 ? distance > ((float) i) : distance > ((float) GestureNavConst.getBackMaxDistanceOne(this.mContext)) * 0.8f;
    }

    private boolean isDown(int action) {
        return action == 0 || action == 5;
    }

    /* access modifiers changed from: protected */
    public boolean isEffectiveFailedReason(int reason) {
        if (reason == 2 || reason == 4 || reason == 5) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String translateFailedReason(int reason) {
        switch (reason) {
            case 1:
                return "Gesture failed as move out region timeout";
            case 2:
                return "Gesture failed as move out gesture angle too large";
            case 3:
                return "Gesture failed as up in region(maybe sliping in region)";
            case 4:
                return "Gesture failed as sliding distance too short";
            case 5:
                return "Gesture failed as sliding rollback or canceled";
            case 6:
                return "Gesture failed as multi finger sliding";
            case 7:
                return "Gesture failed as force cancel";
            case 8:
                return "Gesture failed as invalid event";
            default:
                return "Gesture failed as other";
        }
    }

    /* access modifiers changed from: protected */
    public void focusOut(String packageName) {
    }

    private void initDockParam() {
        try {
            String[] dockParams = GestureNavConst.DOCK_PARAM_MAP.get(GestureUtils.getDeviceType()).split(",");
            if (dockParams.length == 5) {
                this.mGestureMoveMinDistance = GestureNavConst.dipToPx(this.mContext, Integer.parseInt(dockParams[0]));
                this.mGestureStayTimeThrehold = Integer.parseInt(dockParams[1]);
                this.mGestureAcceleration = Float.parseFloat(dockParams[2]);
                this.mGestureSpeed = Float.parseFloat(dockParams[3]);
                this.mBackDistance = GestureNavConst.dipToPx(this.mContext, Integer.parseInt(dockParams[4]));
            }
        } catch (NumberFormatException e) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "get Dock param error");
        }
    }
}
