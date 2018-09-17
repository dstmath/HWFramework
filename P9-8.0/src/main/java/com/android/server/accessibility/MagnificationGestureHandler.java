package com.android.server.accessibility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.MathUtils;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;

class MagnificationGestureHandler extends AbsMagnificationGestureHandler implements EventStreamTransformation {
    private static final boolean DEBUG_DETECTING = false;
    private static final boolean DEBUG_PANNING = false;
    private static final boolean DEBUG_STATE_TRANSITIONS = false;
    private static final String LOG_TAG = "MagnificationEventHandler";
    private static final float MAX_SCALE = 5.0f;
    private static final float MIN_SCALE = 2.0f;
    private static final int STATE_DELEGATING = 1;
    private static final int STATE_DETECTING = 2;
    private static final int STATE_MAGNIFIED_INTERACTION = 4;
    private static final int STATE_VIEWPORT_DRAGGING = 3;
    private final Context mContext;
    private int mCurrentState;
    private long mDelegatingStateDownTime;
    private final boolean mDetectTripleTap;
    private final DetectingStateHandler mDetectingStateHandler;
    private final MagnificationController mMagnificationController;
    private final MagnifiedContentInteractionStateHandler mMagnifiedContentInteractionStateHandler;
    private EventStreamTransformation mNext;
    private int mPreviousState;
    private float mScaleX = 0.0f;
    private float mScaleY = 0.0f;
    private final ScreenStateReceiver mScreenStateReceiver;
    private boolean mShortcutTriggered;
    private final StateViewportDraggingHandler mStateViewportDraggingHandler;
    private PointerCoords[] mTempPointerCoords;
    private PointerProperties[] mTempPointerProperties;
    private boolean mTranslationEnabledBeforePan;
    private final boolean mTriggerable;

    private interface MotionEventHandler {
        void clear();

        void onMotionEvent(MotionEvent motionEvent, MotionEvent motionEvent2, int i);
    }

    private final class DetectingStateHandler implements MotionEventHandler {
        private static final int ACTION_TAP_COUNT = 3;
        private static final int MESSAGE_ON_ACTION_TAP_AND_HOLD = 1;
        private static final int MESSAGE_TRANSITION_TO_DELEGATING_STATE = 2;
        private MotionEventInfo mDelayedEventQueue;
        private final Handler mHandler = new Handler() {
            public void handleMessage(Message message) {
                int type = message.what;
                switch (type) {
                    case 1:
                        DetectingStateHandler.this.onActionTapAndHold(message.obj, message.arg1);
                        return;
                    case 2:
                        MagnificationGestureHandler.this.transitionToState(1);
                        DetectingStateHandler.this.sendDelayedMotionEvents();
                        DetectingStateHandler.this.clear();
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown message type: " + type);
                }
            }
        };
        private MotionEvent mLastDownEvent;
        private MotionEvent mLastTapUpEvent;
        private final int mMultiTapDistanceSlop;
        private final int mMultiTapTimeSlop;
        private int mTapCount;
        private final int mTapDistanceSlop;
        private final int mTapTimeSlop = ViewConfiguration.getJumpTapTimeout();

        public DetectingStateHandler(Context context) {
            this.mMultiTapTimeSlop = ViewConfiguration.getDoubleTapTimeout() + context.getResources().getInteger(17694850);
            this.mTapDistanceSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            this.mMultiTapDistanceSlop = ViewConfiguration.get(context).getScaledDoubleTapSlop();
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            cacheDelayedMotionEvent(event, rawEvent, policyFlags);
            switch (event.getActionMasked()) {
                case 0:
                    this.mHandler.removeMessages(2);
                    if (!MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(event.getX(), event.getY())) {
                        transitionToDelegatingState(MagnificationGestureHandler.this.mShortcutTriggered ^ 1);
                        return;
                    } else if (MagnificationGestureHandler.this.mShortcutTriggered) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, policyFlags, 0, event), (long) ViewConfiguration.getLongPressTimeout());
                        return;
                    } else if (MagnificationGestureHandler.this.mDetectTripleTap) {
                        if (this.mTapCount == 2 && this.mLastDownEvent != null && GestureUtils.isMultiTap(this.mLastDownEvent, event, this.mMultiTapTimeSlop, this.mMultiTapDistanceSlop, 0)) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, policyFlags, 0, event), (long) ViewConfiguration.getLongPressTimeout());
                        } else if (this.mTapCount < 3) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), (long) this.mMultiTapTimeSlop);
                        }
                        clearLastDownEvent();
                        this.mLastDownEvent = MotionEvent.obtain(event);
                        break;
                    } else if (MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), (long) this.mMultiTapTimeSlop);
                        return;
                    } else {
                        transitionToDelegatingState(true);
                        return;
                    }
                    break;
                case 1:
                    if (!MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(event.getX(), event.getY())) {
                        transitionToDelegatingState(MagnificationGestureHandler.this.mShortcutTriggered ^ 1);
                        return;
                    } else if (MagnificationGestureHandler.this.mShortcutTriggered) {
                        clear();
                        onActionTap(event, policyFlags);
                        return;
                    } else if (this.mLastDownEvent != null) {
                        this.mHandler.removeMessages(1);
                        if (!GestureUtils.isTap(this.mLastDownEvent, event, this.mTapTimeSlop, this.mTapDistanceSlop, 0)) {
                            transitionToDelegatingState(true);
                            return;
                        } else if (this.mLastTapUpEvent == null || (GestureUtils.isMultiTap(this.mLastTapUpEvent, event, this.mMultiTapTimeSlop, this.mMultiTapDistanceSlop, 0) ^ 1) == 0) {
                            this.mTapCount++;
                            if (this.mTapCount != 3) {
                                clearLastTapUpEvent();
                                this.mLastTapUpEvent = MotionEvent.obtain(event);
                                break;
                            }
                            clear();
                            onActionTap(event, policyFlags);
                            return;
                        } else {
                            transitionToDelegatingState(true);
                            return;
                        }
                    } else {
                        return;
                    }
                case 2:
                    if (this.mLastDownEvent != null && this.mTapCount < 2 && Math.abs(GestureUtils.computeDistance(this.mLastDownEvent, event, 0)) > ((double) this.mTapDistanceSlop)) {
                        transitionToDelegatingState(true);
                        break;
                    }
                case 5:
                    if (!MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                        transitionToDelegatingState(true);
                        break;
                    }
                    this.mHandler.removeMessages(2);
                    MagnificationGestureHandler.this.transitionToState(4);
                    clear();
                    break;
            }
        }

        public void clear() {
            MagnificationGestureHandler.this.setMagnificationShortcutTriggered(false);
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            clearTapDetectionState();
            clearDelayedMotionEvents();
        }

        private void clearTapDetectionState() {
            this.mTapCount = 0;
            clearLastTapUpEvent();
            clearLastDownEvent();
        }

        private void clearLastTapUpEvent() {
            if (this.mLastTapUpEvent != null) {
                this.mLastTapUpEvent.recycle();
                this.mLastTapUpEvent = null;
            }
        }

        private void clearLastDownEvent() {
            if (this.mLastDownEvent != null) {
                this.mLastDownEvent.recycle();
                this.mLastDownEvent = null;
            }
        }

        private void cacheDelayedMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            MotionEventInfo info = MotionEventInfo.obtain(event, rawEvent, policyFlags);
            if (this.mDelayedEventQueue == null) {
                this.mDelayedEventQueue = info;
                return;
            }
            MotionEventInfo tail = this.mDelayedEventQueue;
            while (tail.mNext != null) {
                tail = tail.mNext;
            }
            tail.mNext = info;
        }

        private void sendDelayedMotionEvents() {
            while (this.mDelayedEventQueue != null) {
                MotionEventInfo info = this.mDelayedEventQueue;
                this.mDelayedEventQueue = info.mNext;
                MagnificationGestureHandler.this.onMotionEvent(info.mEvent, info.mRawEvent, info.mPolicyFlags);
                info.recycle();
            }
        }

        private void clearDelayedMotionEvents() {
            while (this.mDelayedEventQueue != null) {
                MotionEventInfo info = this.mDelayedEventQueue;
                this.mDelayedEventQueue = info.mNext;
                info.recycle();
            }
        }

        private void transitionToDelegatingState(boolean andClear) {
            MagnificationGestureHandler.this.transitionToState(1);
            sendDelayedMotionEvents();
            if (andClear) {
                clear();
            }
        }

        private void onActionTap(MotionEvent up, int policyFlags) {
            if (MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                MagnificationGestureHandler.this.mMagnificationController.reset(true);
                return;
            }
            MagnificationGestureHandler.this.mScaleX = up.getX();
            MagnificationGestureHandler.this.mScaleY = up.getY();
            if (!MagnificationGestureHandler.this.showMagnDialog(MagnificationGestureHandler.this.mContext)) {
                MagnificationGestureHandler.this.mMagnificationController.setScaleAndCenter(MathUtils.constrain(MagnificationGestureHandler.this.mMagnificationController.getPersistedScale(), MagnificationGestureHandler.MIN_SCALE, 5.0f), MagnificationGestureHandler.this.mScaleX, MagnificationGestureHandler.this.mScaleY, true, 0);
            }
        }

        private void onActionTapAndHold(MotionEvent down, int policyFlags) {
            clear();
            MagnificationGestureHandler.this.mTranslationEnabledBeforePan = MagnificationGestureHandler.this.mMagnificationController.isMagnifying();
            MagnificationGestureHandler.this.mMagnificationController.setScaleAndCenter(MathUtils.constrain(MagnificationGestureHandler.this.mMagnificationController.getPersistedScale(), MagnificationGestureHandler.MIN_SCALE, 5.0f), down.getX(), down.getY(), true, 0);
            MagnificationGestureHandler.this.transitionToState(3);
        }
    }

    private final class MagnifiedContentInteractionStateHandler extends SimpleOnGestureListener implements OnScaleGestureListener, MotionEventHandler {
        private final GestureDetector mGestureDetector;
        private float mInitialScaleFactor = -1.0f;
        private final ScaleGestureDetector mScaleGestureDetector;
        private boolean mScaling;
        private final float mScalingThreshold;

        public MagnifiedContentInteractionStateHandler(Context context) {
            TypedValue scaleValue = new TypedValue();
            context.getResources().getValue(17104958, scaleValue, false);
            this.mScalingThreshold = scaleValue.getFloat();
            this.mScaleGestureDetector = new ScaleGestureDetector(context, this);
            this.mScaleGestureDetector.setQuickScaleEnabled(false);
            this.mGestureDetector = new GestureDetector(context, this);
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            this.mScaleGestureDetector.onTouchEvent(event);
            this.mGestureDetector.onTouchEvent(event);
            if (MagnificationGestureHandler.this.mCurrentState == 4 && event.getActionMasked() == 1) {
                clear();
                MagnificationGestureHandler.this.mMagnificationController.persistScale();
                if (MagnificationGestureHandler.this.mPreviousState == 3) {
                    MagnificationGestureHandler.this.transitionToState(3);
                } else {
                    MagnificationGestureHandler.this.transitionToState(2);
                }
            }
        }

        public boolean onScroll(MotionEvent first, MotionEvent second, float distanceX, float distanceY) {
            if (MagnificationGestureHandler.this.mCurrentState != 4) {
                return true;
            }
            MagnificationGestureHandler.this.mMagnificationController.offsetMagnifiedRegion(distanceX, distanceY, 0);
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            if (this.mScaling) {
                float scale;
                float initialScale = MagnificationGestureHandler.this.mMagnificationController.getScale();
                float targetScale = initialScale * detector.getScaleFactor();
                if (targetScale > 5.0f && targetScale > initialScale) {
                    scale = 5.0f;
                } else if (targetScale >= MagnificationGestureHandler.MIN_SCALE || targetScale >= initialScale) {
                    scale = targetScale;
                } else {
                    scale = MagnificationGestureHandler.MIN_SCALE;
                }
                MagnificationGestureHandler.this.mMagnificationController.setScale(scale, detector.getFocusX(), detector.getFocusY(), false, 0);
                return true;
            }
            if (this.mInitialScaleFactor < 0.0f) {
                this.mInitialScaleFactor = detector.getScaleFactor();
            } else if (Math.abs(detector.getScaleFactor() - this.mInitialScaleFactor) > this.mScalingThreshold) {
                this.mScaling = true;
                return true;
            }
            return false;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return MagnificationGestureHandler.this.mCurrentState == 4;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            clear();
        }

        public void clear() {
            this.mInitialScaleFactor = -1.0f;
            this.mScaling = false;
        }
    }

    private static final class MotionEventInfo {
        private static final int MAX_POOL_SIZE = 10;
        private static final Object sLock = new Object();
        private static MotionEventInfo sPool;
        private static int sPoolSize;
        public MotionEvent mEvent;
        private boolean mInPool;
        private MotionEventInfo mNext;
        public int mPolicyFlags;
        public MotionEvent mRawEvent;

        private MotionEventInfo() {
        }

        public static MotionEventInfo obtain(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            MotionEventInfo info;
            synchronized (sLock) {
                if (sPoolSize > 0) {
                    sPoolSize--;
                    info = sPool;
                    sPool = info.mNext;
                    info.mNext = null;
                    info.mInPool = false;
                } else {
                    info = new MotionEventInfo();
                }
                info.initialize(event, rawEvent, policyFlags);
            }
            return info;
        }

        private void initialize(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            this.mEvent = MotionEvent.obtain(event);
            this.mRawEvent = MotionEvent.obtain(rawEvent);
            this.mPolicyFlags = policyFlags;
        }

        public void recycle() {
            synchronized (sLock) {
                if (this.mInPool) {
                    throw new IllegalStateException("Already recycled.");
                }
                clear();
                if (sPoolSize < 10) {
                    sPoolSize++;
                    this.mNext = sPool;
                    sPool = this;
                    this.mInPool = true;
                }
            }
        }

        private void clear() {
            this.mEvent.recycle();
            this.mEvent = null;
            this.mRawEvent.recycle();
            this.mRawEvent = null;
            this.mPolicyFlags = 0;
        }
    }

    private static class ScreenStateReceiver extends BroadcastReceiver {
        private final Context mContext;
        private final MagnificationGestureHandler mGestureHandler;

        public ScreenStateReceiver(Context context, MagnificationGestureHandler gestureHandler) {
            this.mContext = context;
            this.mGestureHandler = gestureHandler;
        }

        public void register() {
            this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.SCREEN_OFF"));
        }

        public void unregister() {
            this.mContext.unregisterReceiver(this);
        }

        public void onReceive(Context context, Intent intent) {
            this.mGestureHandler.setMagnificationShortcutTriggered(false);
        }
    }

    private final class StateViewportDraggingHandler implements MotionEventHandler {
        private boolean mLastMoveOutsideMagnifiedRegion;

        /* synthetic */ StateViewportDraggingHandler(MagnificationGestureHandler this$0, StateViewportDraggingHandler -this1) {
            this();
        }

        private StateViewportDraggingHandler() {
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            switch (event.getActionMasked()) {
                case 0:
                    throw new IllegalArgumentException("Unexpected event type: ACTION_DOWN");
                case 1:
                    if (!MagnificationGestureHandler.this.mTranslationEnabledBeforePan) {
                        MagnificationGestureHandler.this.mMagnificationController.reset(true);
                    }
                    clear();
                    MagnificationGestureHandler.this.transitionToState(2);
                    return;
                case 2:
                    if (event.getPointerCount() != 1) {
                        throw new IllegalStateException("Should have one pointer down.");
                    }
                    float eventX = event.getX();
                    float eventY = event.getY();
                    if (!MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(eventX, eventY)) {
                        this.mLastMoveOutsideMagnifiedRegion = true;
                        return;
                    } else if (this.mLastMoveOutsideMagnifiedRegion) {
                        this.mLastMoveOutsideMagnifiedRegion = false;
                        MagnificationGestureHandler.this.mMagnificationController.setCenter(eventX, eventY, true, 0);
                        return;
                    } else {
                        MagnificationGestureHandler.this.mMagnificationController.setCenter(eventX, eventY, false, 0);
                        return;
                    }
                case 5:
                    clear();
                    MagnificationGestureHandler.this.transitionToState(4);
                    return;
                case 6:
                    throw new IllegalArgumentException("Unexpected event type: ACTION_POINTER_UP");
                default:
                    return;
            }
        }

        public void clear() {
            this.mLastMoveOutsideMagnifiedRegion = false;
        }
    }

    public MagnificationGestureHandler(Context context, AccessibilityManagerService ams, boolean detectTripleTap, boolean triggerable) {
        this.mContext = context;
        this.mMagnificationController = ams.getMagnificationController();
        this.mDetectingStateHandler = new DetectingStateHandler(context);
        this.mStateViewportDraggingHandler = new StateViewportDraggingHandler(this, null);
        this.mMagnifiedContentInteractionStateHandler = new MagnifiedContentInteractionStateHandler(context);
        this.mDetectTripleTap = detectTripleTap;
        this.mTriggerable = triggerable;
        if (triggerable) {
            this.mScreenStateReceiver = new ScreenStateReceiver(context, this);
            this.mScreenStateReceiver.register();
        } else {
            this.mScreenStateReceiver = null;
        }
        transitionToState(2);
    }

    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (!event.isFromSource(4098)) {
            if (this.mNext != null) {
                this.mNext.onMotionEvent(event, rawEvent, policyFlags);
            }
        } else if (this.mDetectTripleTap || (this.mTriggerable ^ 1) == 0) {
            this.mMagnifiedContentInteractionStateHandler.onMotionEvent(event, rawEvent, policyFlags);
            switch (this.mCurrentState) {
                case 1:
                    handleMotionEventStateDelegating(event, rawEvent, policyFlags);
                    break;
                case 2:
                    this.mDetectingStateHandler.onMotionEvent(event, rawEvent, policyFlags);
                    break;
                case 3:
                    this.mStateViewportDraggingHandler.onMotionEvent(event, rawEvent, policyFlags);
                    break;
                case 4:
                    break;
                default:
                    throw new IllegalStateException("Unknown state: " + this.mCurrentState);
            }
        } else {
            if (this.mNext != null) {
                dispatchTransformedEvent(event, rawEvent, policyFlags);
            }
        }
    }

    public void onKeyEvent(KeyEvent event, int policyFlags) {
        if (this.mNext != null) {
            this.mNext.onKeyEvent(event, policyFlags);
        }
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (this.mNext != null) {
            this.mNext.onAccessibilityEvent(event);
        }
    }

    public void setNext(EventStreamTransformation next) {
        this.mNext = next;
    }

    public void clearEvents(int inputSource) {
        if (inputSource == 4098) {
            clear();
        }
        if (this.mNext != null) {
            this.mNext.clearEvents(inputSource);
        }
    }

    public void onDestroy() {
        if (this.mScreenStateReceiver != null) {
            this.mScreenStateReceiver.unregister();
        }
        clear();
    }

    void notifyShortcutTriggered() {
        if (!this.mTriggerable) {
            return;
        }
        if (this.mMagnificationController.resetIfNeeded(true)) {
            clear();
        } else {
            setMagnificationShortcutTriggered(this.mShortcutTriggered ^ 1);
        }
    }

    private void setMagnificationShortcutTriggered(boolean state) {
        if (this.mShortcutTriggered != state) {
            this.mShortcutTriggered = state;
            this.mMagnificationController.setForceShowMagnifiableBounds(state);
        }
    }

    private void clear() {
        this.mCurrentState = 2;
        setMagnificationShortcutTriggered(false);
        this.mDetectingStateHandler.clear();
        this.mStateViewportDraggingHandler.clear();
        this.mMagnifiedContentInteractionStateHandler.clear();
    }

    private void handleMotionEventStateDelegating(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        switch (event.getActionMasked()) {
            case 0:
                this.mDelegatingStateDownTime = event.getDownTime();
                break;
            case 1:
                if (this.mDetectingStateHandler.mDelayedEventQueue == null) {
                    transitionToState(2);
                    break;
                }
                break;
        }
        if (this.mNext != null) {
            event.setDownTime(this.mDelegatingStateDownTime);
            dispatchTransformedEvent(event, rawEvent, policyFlags);
        }
    }

    private void dispatchTransformedEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        float eventX = event.getX();
        float eventY = event.getY();
        if (this.mMagnificationController.isMagnifying() && this.mMagnificationController.magnificationRegionContains(eventX, eventY)) {
            float scale = this.mMagnificationController.getScale();
            float scaledOffsetX = this.mMagnificationController.getOffsetX();
            float scaledOffsetY = this.mMagnificationController.getOffsetY();
            int pointerCount = event.getPointerCount();
            PointerCoords[] coords = getTempPointerCoordsWithMinSize(pointerCount);
            PointerProperties[] properties = getTempPointerPropertiesWithMinSize(pointerCount);
            for (int i = 0; i < pointerCount; i++) {
                event.getPointerCoords(i, coords[i]);
                coords[i].x = (coords[i].x - scaledOffsetX) / scale;
                coords[i].y = (coords[i].y - scaledOffsetY) / scale;
                event.getPointerProperties(i, properties[i]);
            }
            event = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), pointerCount, properties, coords, 0, 0, 1.0f, 1.0f, event.getDeviceId(), 0, event.getSource(), event.getFlags());
        }
        this.mNext.onMotionEvent(event, rawEvent, policyFlags);
    }

    private PointerCoords[] getTempPointerCoordsWithMinSize(int size) {
        int oldSize = this.mTempPointerCoords != null ? this.mTempPointerCoords.length : 0;
        if (oldSize < size) {
            PointerCoords[] oldTempPointerCoords = this.mTempPointerCoords;
            this.mTempPointerCoords = new PointerCoords[size];
            if (oldTempPointerCoords != null) {
                System.arraycopy(oldTempPointerCoords, 0, this.mTempPointerCoords, 0, oldSize);
            }
        }
        for (int i = oldSize; i < size; i++) {
            this.mTempPointerCoords[i] = new PointerCoords();
        }
        return this.mTempPointerCoords;
    }

    private PointerProperties[] getTempPointerPropertiesWithMinSize(int size) {
        int oldSize;
        if (this.mTempPointerProperties != null) {
            oldSize = this.mTempPointerProperties.length;
        } else {
            oldSize = 0;
        }
        if (oldSize < size) {
            PointerProperties[] oldTempPointerProperties = this.mTempPointerProperties;
            this.mTempPointerProperties = new PointerProperties[size];
            if (oldTempPointerProperties != null) {
                System.arraycopy(oldTempPointerProperties, 0, this.mTempPointerProperties, 0, oldSize);
            }
        }
        for (int i = oldSize; i < size; i++) {
            this.mTempPointerProperties[i] = new PointerProperties();
        }
        return this.mTempPointerProperties;
    }

    private void transitionToState(int state) {
        this.mPreviousState = this.mCurrentState;
        this.mCurrentState = state;
    }

    protected void scaleAndMagnifiedRegionCenter() {
        this.mMagnificationController.setScaleAndCenter(MathUtils.constrain(this.mMagnificationController.getPersistedScale(), MIN_SCALE, 5.0f), this.mScaleX, this.mScaleY, true, 0);
    }
}
