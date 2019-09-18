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
import java.util.ArrayList;

public class GestureNavBaseStrategy {
    public static final int GESTURE_FAIL_REASON_ANGLE_TOO_LARGE = 2;
    public static final int GESTURE_FAIL_REASON_FORCE_CANCEL = 7;
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
    private static final int PENDING_DATA_SIZE_TOO_SMALL = 4;
    private static final int SLIDE_EXCEED_MAJOR_AXIS_THRESHOLD = 1;
    private static final int SLIDE_EXCEED_MINOR_AXIS_THRESHOLD = 2;
    private static final int SLIDE_NOT_EXCEED_THRESHOLD = 0;
    private static final int USE_MOVE_OUT_TIME_DEFAULT_THRESHOLD = 0;
    private static final int USE_MOVE_OUT_TIME_MAX_THRESHOLD = 2;
    private static final int USE_MOVE_OUT_TIME_MIN_THRESHOLD = 1;
    private ArrayList<GestureUtils.PointerState> mAbnormalTouchPointers;
    private boolean mCheckDistanceY;
    protected Context mContext;
    private int mDeviceId;
    protected int mDisplayHeight;
    protected int mDisplayWidth;
    private boolean mFastSlideMajorAxisChecking;
    /* access modifiers changed from: private */
    public boolean mFastSlidePreCheckingTimeout;
    protected final float mFastVelocityThreshold;
    private int mFirstPointerId;
    private boolean mFirstPointerUpInMiddle;
    protected boolean mGestureEnd;
    protected boolean mGestureFailed;
    protected int mGestureFailedReason;
    protected boolean mGestureSlowProcessStarted;
    protected boolean mGestureSuccessFinished;
    protected boolean mGuestureReallyStarted;
    private boolean mHasCheckAngle;
    private boolean mHasCheckMinorSlideOut;
    private boolean mHasCheckSamePoint;
    private boolean mHasCheckTimeout;
    private boolean mHasCompensateBeforeUp;
    private float mLastPendingDistance;
    protected Looper mLooper;
    private int mMaxPointCount;
    protected final int mMaximumVelocity;
    private int mMoveOutTimeThreshold;
    private int mMultiTouchThreshold;
    protected int mNavId;
    private ArrayList<GestureUtils.PointerState> mPendingDownUpPointers;
    private ArrayList<Float> mPendingMoveDistance;
    private ArrayList<PointF> mPendingMovePoints;
    private boolean mPointMoved;
    private int mSlideOutThresholdMajorAxis;
    private int mSlideOutThresholdMinorAxis;
    private int mSlideUpThreshold;
    private int mSource;
    private long mStartMoveTime;
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
    private float mXOffset;
    private float mYOffset;

    protected static final class PointEvent {
        public int action;
        public int deviceId;
        public long downTime;
        public long durationTime;
        public float endX;
        public float endY;
        public int source;
        public float startX;
        public float startY;
        public int toolType;

        public PointEvent(float _startX, float _startY, long _downTime, int _action, int _deviceId, int _source, int _toolType) {
            this.startX = _startX;
            this.startY = _startY;
            this.endX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.endY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.downTime = _downTime;
            this.durationTime = 0;
            this.action = _action;
            this.deviceId = _deviceId;
            this.source = _source;
            this.toolType = _toolType;
        }

        public PointEvent(float _startX, float _startY, float _endX, float _endY, long _time, int _deviceId, int _source, int _toolType) {
            this.startX = _startX;
            this.startY = _startY;
            this.endX = _endX;
            this.endY = _endY;
            this.downTime = 0;
            this.durationTime = _time;
            this.action = 0;
            this.deviceId = _deviceId;
            this.source = _source;
            this.toolType = _toolType;
        }
    }

    private final class StrategyHandler extends Handler {
        public StrategyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    GestureNavBaseStrategy.this.compensateSingleEvent((PointEvent) msg.obj);
                    return;
                case 2:
                    GestureNavBaseStrategy.this.checkCompensateEvent((PointEvent) msg.obj, null, null, 1, false);
                    return;
                case 3:
                    GestureUtils.sendKeyEvent(msg.arg1);
                    return;
                case 4:
                    boolean unused = GestureNavBaseStrategy.this.mFastSlidePreCheckingTimeout = true;
                    return;
                default:
                    return;
            }
        }
    }

    public GestureNavBaseStrategy(int navId, Context context) {
        this(navId, context, Looper.getMainLooper());
    }

    public GestureNavBaseStrategy(int navId, Context context, Looper looper) {
        this.mTouchDownRegion = new Rect();
        boolean z = false;
        this.mDeviceId = 0;
        this.mSource = 4098;
        this.mToolType = 0;
        this.mPendingMoveDistance = new ArrayList<>();
        this.mPendingMovePoints = new ArrayList<>();
        this.mPendingDownUpPointers = new ArrayList<>();
        this.mAbnormalTouchPointers = new ArrayList<>();
        this.mNavId = navId;
        this.mContext = context;
        this.mLooper = looper;
        this.mCheckDistanceY = this.mNavId == 3 ? true : z;
        this.mStrategyHandler = new StrategyHandler(looper);
        this.mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        this.mFastVelocityThreshold = 10.0f * ((float) ViewConfiguration.get(context).getScaledMinimumFlingVelocity());
    }

    /* access modifiers changed from: protected */
    public void sendKeyEvent(int keycode) {
        sendKeyEvent(keycode, false);
    }

    /* access modifiers changed from: protected */
    public void sendKeyEvent(int keycode, boolean async) {
        if (async) {
            this.mStrategyHandler.sendMessage(this.mStrategyHandler.obtainMessage(3, keycode, 0, null));
            return;
        }
        GestureUtils.sendKeyEvent(keycode);
    }

    public void updateKeyguardState(boolean keyguardShowing) {
    }

    public void updateScreenConfigState(boolean isLand) {
    }

    public void updateNavTipsState(boolean tipsEnable) {
    }

    public void onNavCreate(GestureNavView view) {
    }

    public void onNavUpdate() {
    }

    public void onNavDestroy() {
    }

    public void updateConfig(int displayWidth, int displayHeight, Rect r) {
        this.mDisplayWidth = displayWidth;
        this.mDisplayHeight = displayHeight;
        this.mTouchDownRegion.set(r);
        int windowThreshold = this.mCheckDistanceY ? this.mTouchDownRegion.height() : this.mTouchDownRegion.width();
        this.mSlideOutThresholdMajorAxis = ((int) (((float) windowThreshold) * 1.2f)) + slipOutThresholdOffset();
        this.mSlideOutThresholdMinorAxis = ((int) (((float) windowThreshold) * 1.6f)) + slipOutThresholdOffset();
        this.mSlideUpThreshold = ((int) (((float) windowThreshold) * 2.0f)) + slipOverThresholdOffset();
        this.mMultiTouchThreshold = ((int) (((float) windowThreshold) * 3.0f)) + slipOverThresholdOffset();
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "navId:" + this.mNavId + ", width:" + this.mDisplayWidth + ", height:" + this.mDisplayHeight + ", region:" + r + ", slipOutMajor:" + this.mSlideOutThresholdMajorAxis + ", slipOutMinor:" + this.mSlideOutThresholdMinorAxis + ", slipUp:" + this.mSlideUpThreshold);
        }
    }

    /* access modifiers changed from: protected */
    public Rect getRegion() {
        return this.mTouchDownRegion;
    }

    /* access modifiers changed from: protected */
    public boolean frameContainsPoint(float x, float y) {
        return this.mTouchDownRegion.contains((int) x, (int) y);
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
        int i;
        switch (useThreshold) {
            case 1:
                return 100;
            case 2:
                return GestureNavConst.GESTURE_MOVE_TIME_THRESHOLD_4;
            default:
                if (this.mCheckDistanceY) {
                    return GestureNavConst.GESTURE_MOVE_TIME_THRESHOLD_4;
                }
                if (this.mToolType == 2) {
                    i = 200;
                } else {
                    i = 120;
                }
                return i;
        }
    }

    /* access modifiers changed from: protected */
    public int distanceExceedThreshold(float distanceX, float distanceY) {
        float distanceMajor = this.mCheckDistanceY ? distanceY : distanceX;
        float distanceMinor = this.mCheckDistanceY ? distanceX : distanceY;
        if (distanceMajor > ((float) this.mSlideOutThresholdMajorAxis)) {
            return 1;
        }
        if (distanceMinor > ((float) this.mSlideOutThresholdMinorAxis)) {
            return 2;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public float fastVelocityTheshold() {
        return this.mCheckDistanceY ? 4500.0f : 6000.0f;
    }

    /* access modifiers changed from: protected */
    public int fastTimeoutTheshold() {
        if (this.mCheckDistanceY) {
            return GestureNavConst.GESTURE_MOVE_TIME_THRESHOLD_4;
        }
        return 200;
    }

    /* access modifiers changed from: protected */
    public void setUseProxyAngleStrategy(boolean useProxyAngleStrategy) {
        this.mUseProxyAngleStrategy = useProxyAngleStrategy;
    }

    /* access modifiers changed from: protected */
    public boolean shouldCheckAbnormalTouch() {
        return (this.mNavId == 1 || this.mNavId == 2) && GestureUtils.isGameAppForeground();
    }

    /* access modifiers changed from: protected */
    public boolean shouldDropMultiTouch() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isMultiTouchBad(float activePointerRawX, float activePointerRawY) {
        float distance = diff(this.mTouchCurrentRawX, this.mTouchCurrentRawY, activePointerRawX, activePointerRawY);
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "MultiTouch rawX=" + activePointerRawX + ", rawY=" + activePointerRawY + ", distance=" + distance);
        }
        return distance > ((float) this.mMultiTouchThreshold);
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action & 255) {
            case 0:
                handleActionDown(event, action);
                break;
            case 1:
            case 3:
                handleActionUp(event, action);
                break;
            case 2:
                handleActionMove(event, action);
                break;
            case 5:
                handleMultiTouchDown(event, action);
                break;
            case 6:
                handleMultiTouchUp(event, action);
                break;
        }
        return this.mGestureFailed;
    }

    private void handleActionDown(MotionEvent event, int action) {
        gestureReset();
        this.mTouchDownTime = event.getEventTime();
        this.mTouchDownRawX = event.getRawX();
        this.mTouchDownRawY = event.getRawY();
        this.mXOffset = event.getX() - this.mTouchDownRawX;
        this.mYOffset = event.getY() - this.mTouchDownRawY;
        this.mTouchCurrentRawX = this.mTouchDownRawX;
        this.mTouchCurrentRawY = this.mTouchDownRawY;
        this.mDeviceId = event.getDeviceId();
        this.mSource = event.getSource();
        this.mToolType = event.getToolType(0);
        this.mStartMoveTime = this.mTouchDownTime;
        this.mMoveOutTimeThreshold = moveOutTimeThreshold(2);
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "Down rawX=" + this.mTouchDownRawX + ", rawY=" + this.mTouchDownRawY + ", rect=" + getRegion() + ", navId=" + this.mNavId + ", xOffset=" + this.mXOffset + ", yOffset=" + this.mYOffset);
        }
        this.mMaxPointCount++;
        this.mFirstPointerId = GestureUtils.getActivePointerId(event, action);
        this.mPendingDownUpPointers.add(new GestureUtils.PointerState(this.mFirstPointerId, action, this.mTouchDownRawX, this.mTouchDownRawY));
        this.mVelocityTracker = VelocityTracker.obtain();
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(event);
        }
        onGestureStarted(this.mTouchDownRawX, this.mTouchDownRawY);
    }

    private void handleMultiTouchDown(MotionEvent event, int action) {
        this.mMaxPointCount++;
        if (!this.mGestureFailed && !this.mGuestureReallyStarted) {
            PointF activePointer = recordPendingDownUpDatas(event, action, false);
            if (shouldDropMultiTouch() || isMultiTouchBad(activePointer.x, activePointer.y)) {
                gestureFailed(6, action, ", multi down before started", event.getEventTime());
            }
        }
        if (!this.mGestureFailed && this.mGuestureReallyStarted && event.getPointerCount() == 2 && shouldCheckAbnormalTouch()) {
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
                        this.mAbnormalTouchPointers.add(new GestureUtils.PointerState(downPointerId, 0, downPointer.x, downPointer.y));
                        if (GestureNavConst.DEBUG) {
                            Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "downId:" + downPointerId + ", downX:" + downPointer.x + ", downY:" + downPointer.y + ", activeId:" + activePointerId + ", action:" + action);
                        }
                    } else {
                        i++;
                    }
                }
                this.mAbnormalTouchPointers.add(new GestureUtils.PointerState(activePointerId, action, activePointer2.x, activePointer2.y));
                gestureFailed(7, action, ", two pointers down after started", event.getEventTime());
            }
        }
    }

    private void handleMultiTouchUp(MotionEvent event, int action) {
        if (!this.mGestureFailed && !this.mGuestureReallyStarted) {
            recordPendingDownUpDatas(event, action, true);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:0x02f8  */
    /* JADX WARNING: Removed duplicated region for block: B:112:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0125  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0298  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x02e7  */
    private void handleActionMove(MotionEvent event, int action) {
        int slipOutMode;
        long durationTime;
        double angle;
        long durationTime2;
        double angle2;
        long delayTime;
        double angle3;
        float velocity;
        MotionEvent motionEvent = event;
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(motionEvent);
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
        if (!this.mGestureFailed && !this.mGestureSlowProcessStarted && !this.mFirstPointerUpInMiddle) {
            recordPendingMoveDatas(motionEvent, distance, this.mTouchCurrentRawX, this.mTouchCurrentRawY);
        }
        if (this.mFastSlidePreCheckingTimeout && this.mFastSlideMajorAxisChecking) {
            if (GestureNavConst.DEBUG) {
                Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "fast check timeout and move arrived");
            }
            this.mFastSlideMajorAxisChecking = false;
        }
        if (this.mGestureFailed || this.mHasCheckAngle) {
            slipOutMode = 0;
        } else {
            int distanceExceedThreshold = distanceExceedThreshold(distanceX, distanceY);
            slipOutMode = distanceExceedThreshold;
            if (distanceExceedThreshold == 1) {
                this.mHasCheckAngle = true;
                float velocity2 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                if (!this.mUseProxyAngleStrategy) {
                    double angle4 = angle(distanceX, distanceY);
                    angle3 = angle4;
                    if (angle4 > ((double) moveOutAngleThreshold())) {
                        gestureFailed(2, action, ", angle=" + angle3, event.getEventTime());
                        velocity = 0.0f;
                        delayTime = 0;
                        angle3 = angle3;
                        if (GestureNavConst.DEBUG) {
                            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "angle:" + angle3 + ", velocity:" + velocity + ", checkTimeout:" + this.mHasCheckTimeout + ", fastChecking:" + this.mFastSlideMajorAxisChecking + ", delayTime:" + delayTime + ", slipOutMode:" + slipOutMode + ", dx:" + distanceX + ", dy:" + distanceY);
                        }
                    }
                } else {
                    angle3 = 0.0d;
                }
                if (!this.mHasCheckTimeout) {
                    this.mHasCheckTimeout = true;
                    VelocityTracker vt = this.mVelocityTracker;
                    if (vt != null) {
                        vt.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                        velocity2 = getVelocity(vt);
                    }
                    if (velocity2 > fastVelocityTheshold()) {
                        delayTime = ((long) fastTimeoutTheshold()) - (event.getEventTime() - this.mStartMoveTime);
                        if (delayTime > 0) {
                            this.mFastSlideMajorAxisChecking = true;
                            VelocityTracker velocityTracker = vt;
                            this.mStrategyHandler.sendEmptyMessageDelayed(4, delayTime);
                        }
                        velocity = velocity2;
                        if (GestureNavConst.DEBUG) {
                        }
                    }
                }
                velocity = velocity2;
                delayTime = 0;
                if (GestureNavConst.DEBUG) {
                }
            }
        }
        if (this.mGestureFailed == 0 && !this.mHasCheckTimeout) {
            long eventTime = event.getEventTime();
            if (!this.mHasCheckSamePoint && isPointChanged((int) distanceX, (int) distanceY)) {
                this.mHasCheckSamePoint = true;
                this.mStartMoveTime = eventTime;
                this.mMoveOutTimeThreshold = moveOutTimeThreshold(0);
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "point moved, threshold:" + this.mMoveOutTimeThreshold);
                }
            }
            if (!this.mHasCheckMinorSlideOut && slipOutMode == 2) {
                this.mHasCheckMinorSlideOut = true;
                this.mStartMoveTime = this.mTouchDownTime;
                this.mMoveOutTimeThreshold = moveOutTimeThreshold(1);
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "slide in minor axis, threshold:" + this.mMoveOutTimeThreshold);
                }
            }
            long durationTime3 = eventTime - this.mStartMoveTime;
            if (durationTime3 > ((long) this.mMoveOutTimeThreshold)) {
                this.mHasCheckTimeout = true;
                if (frameContainsPoint(this.mTouchCurrentRawX, this.mTouchCurrentRawY)) {
                    float f = distanceY;
                    durationTime2 = durationTime3;
                } else if (!isPointMovedMinDistance(distance)) {
                    float f2 = distanceX;
                    float f3 = distanceY;
                    durationTime2 = durationTime3;
                } else if (!this.mHasCheckAngle) {
                    this.mHasCheckAngle = true;
                    double angle5 = angle(distanceX, distanceY);
                    if (angle5 > ((double) moveOutAngleThreshold())) {
                        angle2 = angle5;
                        float f4 = distanceX;
                        float f5 = distanceY;
                        durationTime = durationTime3;
                        gestureFailed(2, action, ", timeout angle=" + angle5, eventTime);
                    } else {
                        angle2 = angle5;
                        float f6 = distanceX;
                        float f7 = distanceY;
                        durationTime = durationTime3;
                    }
                    angle = angle2;
                    if (GestureNavConst.DEBUG) {
                        Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "move out time:" + durationTime + "ms, threshold:" + this.mMoveOutTimeThreshold + "ms, point(" + this.mTouchCurrentRawX + ", " + this.mTouchCurrentRawY + "), angle:" + angle);
                    }
                    if (!this.mGuestureReallyStarted && gestureReady()) {
                        if (GestureNavConst.DEBUG) {
                            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "gesture really started");
                        }
                        this.mGuestureReallyStarted = true;
                        onGestureReallyStarted();
                    }
                    if (this.mGuestureReallyStarted && !this.mGestureFailed && !this.mFastSlideMajorAxisChecking) {
                        if (!this.mGestureSlowProcessStarted) {
                            if (GestureNavConst.DEBUG) {
                                Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "gesture slow process started");
                            }
                            this.mGestureSlowProcessStarted = true;
                            onGestureSlowProcessStarted(this.mPendingMoveDistance);
                        }
                        onGestureSlowProcess(distance, offsetX, offsetY);
                        return;
                    }
                    return;
                } else {
                    float f8 = distanceY;
                    durationTime2 = durationTime3;
                    angle = 0.0d;
                    if (GestureNavConst.DEBUG) {
                    }
                    if (GestureNavConst.DEBUG) {
                    }
                    this.mGuestureReallyStarted = true;
                    onGestureReallyStarted();
                    if (this.mGuestureReallyStarted) {
                        return;
                    }
                    return;
                }
                gestureFailed(1, action, ", time=" + durationTime2 + "ms, point(" + this.mTouchCurrentRawX + ", " + this.mTouchCurrentRawY + "), dist=" + distance, eventTime);
                angle = 0.0d;
                if (GestureNavConst.DEBUG) {
                }
                if (GestureNavConst.DEBUG) {
                }
                this.mGuestureReallyStarted = true;
                onGestureReallyStarted();
                if (this.mGuestureReallyStarted) {
                }
            }
        }
        float f9 = distanceY;
        if (GestureNavConst.DEBUG) {
        }
        this.mGuestureReallyStarted = true;
        onGestureReallyStarted();
        if (this.mGuestureReallyStarted) {
        }
    }

    private void handleActionUp(MotionEvent event, int action) {
        this.mStrategyHandler.removeMessages(4);
        this.mTouchUpTime = event.getEventTime();
        this.mTouchUpRawX = event.getRawX();
        this.mTouchUpRawY = event.getRawY();
        this.mTouchCurrentRawX = this.mTouchUpRawX;
        this.mTouchCurrentRawY = this.mTouchUpRawY;
        float velocity = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(event);
            this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
            velocity = getVelocity(this.mVelocityTracker);
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
        if (!this.mGestureFailed && !this.mGuestureReallyStarted) {
            recordPendingDownUpDatas(event, action, true);
        }
        if (this.mGestureFailed && action == 1 && this.mHasCompensateBeforeUp) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "Receive up after gesture failed");
            onGestureUpArrivedAfterFailed(this.mTouchUpRawX, this.mTouchUpRawY, this.mTouchDownTime);
        }
        if (!this.mGestureFailed && !this.mGuestureReallyStarted && frameContainsPoint(this.mTouchUpRawX, this.mTouchUpRawY)) {
            gestureFailed(3, action, "", this.mTouchUpTime);
        }
        float distance = diff(this.mTouchDownRawX, this.mTouchDownRawY, this.mTouchUpRawX, this.mTouchUpRawY);
        if (!this.mGestureFailed && !this.mGuestureReallyStarted && distance < ((float) getUpDistanceThreshold())) {
            gestureFailed(4, action, ", distance:" + distance, this.mTouchUpTime);
        }
        long durationTime = this.mTouchUpTime - this.mTouchDownTime;
        if (!this.mGestureFailed && this.mGuestureReallyStarted && ((distance < ((float) getUpDistanceThreshold()) && durationTime > 150) || action == 3)) {
            gestureFailed(5, action, ", duration:" + durationTime + "ms, distance:" + distance + ", action:" + action, this.mTouchUpTime);
        }
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "Up rawX=" + this.mTouchUpRawX + ", rawY=" + this.mTouchUpRawY + ", failed=" + this.mGestureFailed + ", rellayStarted=" + this.mGuestureReallyStarted + ", velocity=" + velocity + ", distance=" + distance + ", duration=" + durationTime);
        }
        gestureFinished(distance, durationTime, velocity);
        gestureEnd(action);
    }

    private void gestureReset() {
        this.mGestureFailedReason = 0;
        this.mGestureFailed = false;
        this.mGuestureReallyStarted = false;
        this.mGestureSlowProcessStarted = false;
        this.mGestureSuccessFinished = false;
        this.mGestureEnd = false;
        this.mStrategyHandler.removeMessages(4);
        this.mFastSlidePreCheckingTimeout = false;
        this.mFastSlideMajorAxisChecking = false;
        this.mUseProxyAngleStrategy = false;
        this.mHasCheckAngle = false;
        this.mHasCheckTimeout = false;
        this.mHasCheckSamePoint = false;
        this.mHasCheckMinorSlideOut = false;
        this.mHasCompensateBeforeUp = false;
        this.mLastPendingDistance = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mPendingMoveDistance.clear();
        this.mPendingMovePoints.clear();
        this.mFirstPointerUpInMiddle = false;
        this.mPointMoved = false;
        this.mMaxPointCount = 0;
        this.mPendingDownUpPointers.clear();
        this.mAbnormalTouchPointers.clear();
    }

    private void gestureFailed(int reason, int action, String appendStr, long failedTime) {
        Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, translateFailedReason(reason) + appendStr);
        this.mGestureFailedReason = reason;
        this.mTouchFailedTime = failedTime;
        this.mGestureFailed = true;
        onGestureFailed(this.mGestureFailedReason, action);
    }

    private boolean gestureReady() {
        return !this.mGestureFailed && this.mHasCheckTimeout && this.mHasCheckAngle;
    }

    private void gestureFinished(float distance, long durationTime, float velocity) {
        if (!this.mGestureFailed) {
            boolean isFastSlideGesture = false;
            if (!this.mGestureSlowProcessStarted && (this.mFastSlideMajorAxisChecking || velocity > 6000.0f)) {
                isFastSlideGesture = true;
            }
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "gesture success, reallyStarted=" + this.mGuestureReallyStarted + ", slowStarted=" + this.mGestureSlowProcessStarted + ", velocity=" + velocity + ", fastChecking=" + this.mFastSlideMajorAxisChecking + ", fastGesture=" + isFastSlideGesture);
            }
            this.mGestureSuccessFinished = true;
            onGestureSuccessFinished(distance, durationTime, velocity, isFastSlideGesture);
        }
    }

    private void gestureEnd(int action) {
        this.mGestureEnd = true;
        this.mPendingMoveDistance.clear();
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
    public void onGestureSlowProcess(float distance, float offsetX, float offsetY) {
    }

    /* access modifiers changed from: protected */
    public void onGestureFailed(int reason, int action) {
        int i = action;
        switch (reason) {
            case 1:
            case 2:
                this.mHasCompensateBeforeUp = true;
                compensateBatchDownEvent(this.mTouchDownRawX, this.mTouchDownRawY, this.mPendingMovePoints, this.mTouchDownTime, this.mTouchFailedTime - this.mTouchDownTime, this.mDeviceId, this.mSource, this.mToolType);
                return;
            case 3:
            case 4:
                long durationTime = this.mTouchFailedTime - this.mTouchDownTime;
                if (durationTime >= 500 || i == 3) {
                    Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "no need compensate as duration:" + durationTime + ", action:" + i);
                    return;
                }
                PointEvent pointEvent = new PointEvent(this.mTouchDownRawX, this.mTouchDownRawY, this.mTouchUpRawX, this.mTouchUpRawY, durationTime, this.mDeviceId, this.mSource, this.mToolType);
                checkCompensateEvent(pointEvent, this.mPendingMovePoints, this.mPendingDownUpPointers, this.mMaxPointCount, this.mPointMoved);
                return;
            case 5:
                if (GestureNavConst.DEBUG) {
                    Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "no need compensate as gesture cancel");
                    return;
                }
                return;
            case 6:
                this.mHasCompensateBeforeUp = true;
                compensateMultiDownEvent(this.mPendingDownUpPointers, this.mMaxPointCount, this.mTouchDownTime, this.mTouchFailedTime - this.mTouchDownTime, this.mDeviceId, this.mSource, this.mToolType);
                return;
            case 7:
                this.mHasCompensateBeforeUp = true;
                compensateMultiDownEvent(this.mAbnormalTouchPointers, this.mMaxPointCount, this.mTouchDownTime, this.mTouchFailedTime - this.mTouchDownTime, this.mDeviceId, this.mSource, this.mToolType);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void onGestureSuccessFinished(float distance, long durationTime, float velocity, boolean isFastSlideGesture) {
    }

    /* access modifiers changed from: protected */
    public void onGestureEnd(int action) {
    }

    /* access modifiers changed from: protected */
    public void onGestureUpArrivedAfterFailed(float rawX, float rawY, long pointDownTime) {
        Handler handler = this.mStrategyHandler;
        PointEvent pointEvent = new PointEvent(rawX, rawY, pointDownTime, 1, this.mDeviceId, this.mSource, this.mToolType);
        this.mStrategyHandler.sendMessage(handler.obtainMessage(1, 0, 0, pointEvent));
    }

    /* access modifiers changed from: private */
    public void compensateSingleEvent(PointEvent p) {
        long eventTime;
        if (GestureNavConst.DEBUG) {
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "compensateSingleEvent x=" + p.startX + ", y=" + p.startY + ", action=" + p.action);
        }
        if (p.action == 0) {
            eventTime = p.downTime;
        } else {
            eventTime = SystemClock.uptimeMillis();
        }
        GestureUtils.injectMotionEvent(p.action, p.downTime, eventTime, p.startX, p.startY, p.deviceId, p.source, p.toolType);
    }

    private void compensateBatchDownEvent(float startX, float startY, ArrayList<PointF> pendingMovePoints, long downTime, long durationTime, int deviceId, int source, int toolType) {
        long j;
        float f;
        float f2;
        if (GestureNavConst.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("compensateBatchDownEvent x=");
            f2 = startX;
            sb.append(f2);
            sb.append(", y=");
            f = startY;
            sb.append(f);
            sb.append(", durationTime=");
            j = durationTime;
            sb.append(j);
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, sb.toString());
        } else {
            f2 = startX;
            f = startY;
            j = durationTime;
        }
        GestureUtils.injectDownWithBatchMoveEvent(downTime, f2, f, pendingMovePoints, j, deviceId, source, toolType);
    }

    private void compensateMultiDownEvent(ArrayList<GestureUtils.PointerState> pendingDownUps, int maxPointerCount, long downTime, long durationTime, int deviceId, int source, int toolType) {
        long j;
        int i;
        if (GestureNavConst.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("compensateMultiDownEvent count=");
            i = maxPointerCount;
            sb.append(i);
            sb.append(", durationTime=");
            j = durationTime;
            sb.append(j);
            Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, sb.toString());
        } else {
            i = maxPointerCount;
            j = durationTime;
        }
        GestureUtils.sendMultiPointerDown(pendingDownUps, i, deviceId, source, toolType, downTime, j);
    }

    /* access modifiers changed from: private */
    public void checkCompensateEvent(PointEvent p, ArrayList<PointF> pendingMovePoints, ArrayList<GestureUtils.PointerState> pendingDownUps, int maxPointerCount, boolean multiPointMoved) {
        boolean hasMultiTouched = true;
        if (maxPointerCount <= 1) {
            hasMultiTouched = false;
        }
        if (!hasMultiTouched || multiPointMoved) {
            float distance = distance(p.startX, p.startY, p.endX, p.endY);
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "checkCompensateEvent distance=" + distance);
            }
            if (isPointMovedMinDistance(distance)) {
                compensateMoveEvent(p, pendingMovePoints, hasMultiTouched);
            } else {
                compensateTapEvent(p);
            }
        } else {
            GestureUtils.sendMultiPointerTap(pendingDownUps, maxPointerCount, p.deviceId, p.source, p.toolType);
        }
    }

    private void compensateTapEvent(PointEvent p) {
        GestureUtils.sendTap(p.startX, p.startY, p.deviceId, p.source, p.toolType);
    }

    private void compensateMoveEvent(PointEvent p, ArrayList<PointF> pendingMovePoints, boolean hasMultiTouched) {
        ArrayList<PointF> arrayList;
        boolean canUsePendingPoints = true;
        if (pendingMovePoints == null) {
            canUsePendingPoints = false;
        } else if (pendingMovePoints.size() >= 30 || pendingMovePoints.size() < 4) {
            canUsePendingPoints = false;
            if (GestureNavConst.DEBUG) {
                Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "pending points size:" + pendingMovePoints.size());
            }
        }
        float f = p.startX;
        float f2 = p.startY;
        float f3 = p.endX;
        float f4 = p.endY;
        int i = (int) p.durationTime;
        int i2 = p.deviceId;
        int i3 = p.source;
        int i4 = p.toolType;
        if (canUsePendingPoints) {
            arrayList = pendingMovePoints;
        } else {
            arrayList = null;
        }
        GestureUtils.sendSwipe(f, f2, f3, f4, i, i2, i3, i4, arrayList, hasMultiTouched);
    }

    private void recordPendingMoveDatas(MotionEvent event, float distance, float rawX, float rawY) {
        if (this.mPendingMoveDistance.size() < 30) {
            int dist = (int) Math.abs(distance - this.mLastPendingDistance);
            int dt = 150;
            if (dist > 150) {
                int times = dist / 150;
                if (distance <= this.mLastPendingDistance) {
                    dt = -150;
                }
                for (int i = 0; i < times; i++) {
                    this.mPendingMoveDistance.add(Float.valueOf(distance - ((float) ((times - i) * dt))));
                }
            }
            this.mPendingMoveDistance.add(Float.valueOf(distance));
            this.mLastPendingDistance = distance;
        }
        if (this.mPendingMovePoints.size() < 30) {
            int historySize = event.getHistorySize();
            for (int h = 0; h < historySize; h++) {
                this.mPendingMovePoints.add(new PointF(event.getHistoricalX(h) - this.mXOffset, event.getHistoricalY(h) - this.mYOffset));
                if (GestureNavConst.DEBUG_ALL) {
                    Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "record rx=" + rx + ", ry=" + ry);
                }
            }
            this.mPendingMovePoints.add(new PointF(rawX, rawY));
        }
    }

    private PointF recordPendingDownUpDatas(MotionEvent event, int action, boolean isUp) {
        int actionIndex = GestureUtils.getActiveActionIndex(action);
        int activePointerId = event.getPointerId(actionIndex);
        PointF activePointerRawCoords = getRawPointCoords(event, actionIndex);
        if (isUp) {
            if (!this.mFirstPointerUpInMiddle && (action & 255) == 6 && activePointerId == this.mFirstPointerId) {
                this.mFirstPointerUpInMiddle = true;
                if (GestureNavConst.DEBUG) {
                    Log.i(GestureNavConst.TAG_GESTURE_STRATEGY, "first pointer up, id=" + this.mFirstPointerId);
                }
            }
            if (!this.mPointMoved) {
                int size = this.mPendingDownUpPointers.size();
                for (int i = 0; i < size; i++) {
                    GestureUtils.PointerState ps = this.mPendingDownUpPointers.get(i);
                    if (ps.activePointerId == activePointerId && isDown(ps.action)) {
                        this.mPointMoved = isPointMovedMinDistance(ps.x, ps.y, activePointerRawCoords.x, activePointerRawCoords.y);
                        if (this.mPointMoved) {
                            break;
                        }
                    }
                }
            }
        }
        if (GestureNavConst.DEBUG_ALL != 0) {
            Log.d(GestureNavConst.TAG_GESTURE_STRATEGY, "record id=" + activePointerId + ", action=" + Integer.toHexString(action) + ", rawX=" + activePointerRawCoords.x + ", rawY=" + activePointerRawCoords.y + ", moved=" + this.mPointMoved);
        }
        this.mPendingDownUpPointers.add(new GestureUtils.PointerState(activePointerId, action, activePointerRawCoords.x, activePointerRawCoords.y));
        return activePointerRawCoords;
    }

    private PointF getRawPointCoords(MotionEvent event, int actionIndex) {
        return new PointF(event.getX(actionIndex) - this.mXOffset, event.getY(actionIndex) - this.mYOffset);
    }

    private int getUpDistanceThreshold() {
        return (this.mMaxPointCount <= 1 || !this.mFirstPointerUpInMiddle) ? this.mSlideUpThreshold : this.mMultiTouchThreshold;
    }

    private static final float distance(float startX, float startY, float endX, float endY) {
        float dx = startX - endX;
        float dy = startY - endY;
        return (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
    }

    private float absOffset(float offset) {
        return Math.abs(offset);
    }

    private float diff(float diffX, float diffY) {
        return this.mCheckDistanceY ? diffY : diffX;
    }

    private float diff(float startX, float startY, float endX, float endY) {
        return absOffset(this.mCheckDistanceY ? endY - startY : endX - startX);
    }

    private double angle(float distanceX, float distanceY) {
        return GestureUtils.angle(distanceX, distanceY, this.mCheckDistanceY);
    }

    private float getVelocity(VelocityTracker velocityTracker) {
        return Math.abs(this.mCheckDistanceY ? velocityTracker.getYVelocity() : velocityTracker.getXVelocity());
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

    private boolean isDown(int action) {
        return action == 0 || action == 5;
    }

    /* access modifiers changed from: protected */
    public boolean isEffectiveFailedReason(int reason) {
        if (reason != 2) {
            switch (reason) {
                case 4:
                case 5:
                    break;
                default:
                    return false;
            }
        }
        return true;
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
            default:
                return "Gesture failed as other";
        }
    }
}
