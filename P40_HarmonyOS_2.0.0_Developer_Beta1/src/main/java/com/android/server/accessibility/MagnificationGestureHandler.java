package com.android.server.accessibility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.MathUtils;
import android.util.Slog;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.LocalServices;
import com.android.server.usb.descriptors.UsbACInterface;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import java.util.Queue;

/* access modifiers changed from: package-private */
public class MagnificationGestureHandler extends BaseEventStreamTransformation {
    private static final boolean DEBUG_ALL = false;
    private static final boolean DEBUG_DETECTING = false;
    private static final boolean DEBUG_EVENT_STREAM = false;
    private static final boolean DEBUG_PANNING_SCALING = false;
    private static final boolean DEBUG_STATE_TRANSITIONS = false;
    private static final String LOG_TAG = "MagnificationGestureHandler";
    private static final float MAX_SCALE = 8.0f;
    private static final float MIN_SCALE = 2.0f;
    private final Context mContext;
    @VisibleForTesting
    State mCurrentState;
    private final Queue<MotionEvent> mDebugInputEventHistory;
    private final Queue<MotionEvent> mDebugOutputEventHistory;
    @VisibleForTesting
    final DelegatingState mDelegatingState;
    final boolean mDetectShortcutTrigger;
    final boolean mDetectTripleTap;
    @VisibleForTesting
    final DetectingState mDetectingState;
    private final int mDisplayId;
    private HwFoldScreenManagerInternal mFsmInternal;
    private boolean mIsFoldableScreen = false;
    @VisibleForTesting
    final MagnificationController mMagnificationController;
    @VisibleForTesting
    final PanningScalingState mPanningScalingState;
    @VisibleForTesting
    State mPreviousState;
    private final ScreenStateReceiver mScreenStateReceiver;
    private MotionEvent.PointerCoords[] mTempPointerCoords;
    private MotionEvent.PointerProperties[] mTempPointerProperties;
    @VisibleForTesting
    final ViewportDraggingState mViewportDraggingState;

    public MagnificationGestureHandler(Context context, MagnificationController magnificationController, boolean detectTripleTap, boolean detectShortcutTrigger, int displayId) {
        this.mContext = context;
        this.mMagnificationController = magnificationController;
        this.mDisplayId = displayId;
        this.mDelegatingState = new DelegatingState();
        this.mDetectingState = new DetectingState(context);
        this.mViewportDraggingState = new ViewportDraggingState();
        this.mPanningScalingState = new PanningScalingState(context);
        this.mDetectTripleTap = detectTripleTap;
        this.mDetectShortcutTrigger = detectShortcutTrigger;
        if (this.mDetectShortcutTrigger) {
            this.mScreenStateReceiver = new ScreenStateReceiver(context, this);
            this.mScreenStateReceiver.register();
        } else {
            this.mScreenStateReceiver = null;
        }
        this.mDebugInputEventHistory = null;
        this.mDebugOutputEventHistory = null;
        transitionTo(this.mDetectingState);
        this.mIsFoldableScreen = !SystemProperties.get("ro.config.hw_fold_disp").isEmpty();
        if (this.mIsFoldableScreen) {
            this.mFsmInternal = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        }
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        onMotionEventInternal(event, rawEvent, policyFlags);
    }

    private void onMotionEventInternal(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if ((this.mDetectTripleTap || this.mDetectShortcutTrigger) && event.isFromSource(UsbACInterface.FORMAT_II_AC3)) {
            handleEventWith(this.mCurrentState, event, rawEvent, policyFlags);
        } else {
            dispatchTransformedEvent(event, rawEvent, policyFlags);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEventWith(State stateHandler, MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        this.mPanningScalingState.mScrollGestureDetector.onTouchEvent(event);
        this.mPanningScalingState.mScaleGestureDetector.onTouchEvent(event);
        stateHandler.onMotionEvent(event, rawEvent, policyFlags);
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void clearEvents(int inputSource) {
        if (inputSource == 4098) {
            clearAndTransitionToStateDetecting();
        }
        super.clearEvents(inputSource);
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onDestroy() {
        ScreenStateReceiver screenStateReceiver = this.mScreenStateReceiver;
        if (screenStateReceiver != null) {
            screenStateReceiver.unregister();
        }
        this.mMagnificationController.resetAllIfNeeded(0);
        clearAndTransitionToStateDetecting();
    }

    /* access modifiers changed from: package-private */
    public void notifyShortcutTriggered() {
        if (!this.mDetectShortcutTrigger) {
            return;
        }
        if (this.mMagnificationController.resetIfNeeded(this.mDisplayId, true)) {
            clearAndTransitionToStateDetecting();
        } else {
            this.mDetectingState.toggleShortcutTriggered();
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAndTransitionToStateDetecting() {
        DetectingState detectingState = this.mDetectingState;
        this.mCurrentState = detectingState;
        detectingState.clear();
        this.mViewportDraggingState.clear();
        this.mPanningScalingState.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchTransformedEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        super.onMotionEvent(event, rawEvent, policyFlags);
    }

    private static void storeEventInto(Queue<MotionEvent> queue, MotionEvent event) {
        queue.add(MotionEvent.obtain(event));
        while (!queue.isEmpty() && event.getEventTime() - queue.peek().getEventTime() > 5000) {
            queue.remove().recycle();
        }
    }

    private MotionEvent.PointerCoords[] getTempPointerCoordsWithMinSize(int size) {
        MotionEvent.PointerCoords[] pointerCoordsArr = this.mTempPointerCoords;
        int oldSize = pointerCoordsArr != null ? pointerCoordsArr.length : 0;
        if (oldSize < size) {
            MotionEvent.PointerCoords[] oldTempPointerCoords = this.mTempPointerCoords;
            this.mTempPointerCoords = new MotionEvent.PointerCoords[size];
            if (oldTempPointerCoords != null) {
                System.arraycopy(oldTempPointerCoords, 0, this.mTempPointerCoords, 0, oldSize);
            }
        }
        for (int i = oldSize; i < size; i++) {
            this.mTempPointerCoords[i] = new MotionEvent.PointerCoords();
        }
        return this.mTempPointerCoords;
    }

    private MotionEvent.PointerProperties[] getTempPointerPropertiesWithMinSize(int size) {
        MotionEvent.PointerProperties[] pointerPropertiesArr = this.mTempPointerProperties;
        int oldSize = pointerPropertiesArr != null ? pointerPropertiesArr.length : 0;
        if (oldSize < size) {
            MotionEvent.PointerProperties[] oldTempPointerProperties = this.mTempPointerProperties;
            this.mTempPointerProperties = new MotionEvent.PointerProperties[size];
            if (oldTempPointerProperties != null) {
                System.arraycopy(oldTempPointerProperties, 0, this.mTempPointerProperties, 0, oldSize);
            }
        }
        for (int i = oldSize; i < size; i++) {
            this.mTempPointerProperties[i] = new MotionEvent.PointerProperties();
        }
        return this.mTempPointerProperties;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void transitionTo(State state) {
        if (State.nameOf(this.mViewportDraggingState).equals(State.nameOf(this.mCurrentState)) || State.nameOf(this.mViewportDraggingState).equals(State.nameOf(state))) {
            Slog.i(LOG_TAG, State.nameOf(this.mCurrentState) + " -> " + State.nameOf(state));
        }
        this.mPreviousState = this.mCurrentState;
        this.mCurrentState = state;
    }

    /* access modifiers changed from: package-private */
    public interface State {
        void onMotionEvent(MotionEvent motionEvent, MotionEvent motionEvent2, int i);

        default void clear() {
        }

        default String name() {
            return getClass().getSimpleName();
        }

        static String nameOf(State s) {
            return s != null ? s.name() : "null";
        }
    }

    /* access modifiers changed from: package-private */
    public final class PanningScalingState extends GestureDetector.SimpleOnGestureListener implements ScaleGestureDetector.OnScaleGestureListener, State {
        float mInitialScaleFactor = -1.0f;
        private final ScaleGestureDetector mScaleGestureDetector;
        boolean mScaling;
        final float mScalingThreshold;
        private final GestureDetector mScrollGestureDetector;

        public PanningScalingState(Context context) {
            TypedValue scaleValue = new TypedValue();
            context.getResources().getValue(17105075, scaleValue, false);
            this.mScalingThreshold = scaleValue.getFloat();
            this.mScaleGestureDetector = new ScaleGestureDetector(context, this, Handler.getMain());
            this.mScaleGestureDetector.setQuickScaleEnabled(false);
            this.mScrollGestureDetector = new GestureDetector(context, this, Handler.getMain());
        }

        @Override // com.android.server.accessibility.MagnificationGestureHandler.State
        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            int action = event.getActionMasked();
            if (action == 6 && event.getPointerCount() == 2 && MagnificationGestureHandler.this.mPreviousState == MagnificationGestureHandler.this.mViewportDraggingState) {
                persistScaleAndTransitionTo(MagnificationGestureHandler.this.mViewportDraggingState);
            } else if (action == 1 || action == 3) {
                persistScaleAndTransitionTo(MagnificationGestureHandler.this.mDetectingState);
            }
        }

        public void persistScaleAndTransitionTo(State state) {
            MagnificationGestureHandler.this.mMagnificationController.persistScale();
            clear();
            MagnificationGestureHandler.this.transitionTo(state);
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onScroll(MotionEvent first, MotionEvent second, float distanceX, float distanceY) {
            if (MagnificationGestureHandler.this.mCurrentState != MagnificationGestureHandler.this.mPanningScalingState) {
                return true;
            }
            MagnificationGestureHandler.this.mMagnificationController.offsetMagnifiedRegion(MagnificationGestureHandler.this.mDisplayId, distanceX, distanceY, 0);
            return true;
        }

        @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
        public boolean onScale(ScaleGestureDetector detector) {
            float scale;
            boolean z = true;
            if (this.mScaling) {
                float initialScale = MagnificationGestureHandler.this.mMagnificationController.getScale(MagnificationGestureHandler.this.mDisplayId);
                float targetScale = detector.getScaleFactor() * initialScale;
                if (targetScale > 8.0f && targetScale > initialScale) {
                    scale = 8.0f;
                } else if (targetScale >= MagnificationGestureHandler.MIN_SCALE || targetScale >= initialScale) {
                    scale = targetScale;
                } else {
                    scale = MagnificationGestureHandler.MIN_SCALE;
                }
                MagnificationGestureHandler.this.mMagnificationController.setScale(MagnificationGestureHandler.this.mDisplayId, scale, detector.getFocusX(), detector.getFocusY(), false, 0);
                return true;
            } else if (this.mInitialScaleFactor < 0.0f) {
                this.mInitialScaleFactor = detector.getScaleFactor();
                return false;
            } else {
                if (Math.abs(detector.getScaleFactor() - this.mInitialScaleFactor) <= this.mScalingThreshold) {
                    z = false;
                }
                this.mScaling = z;
                return this.mScaling;
            }
        }

        @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return MagnificationGestureHandler.this.mCurrentState == MagnificationGestureHandler.this.mPanningScalingState;
        }

        @Override // android.view.ScaleGestureDetector.OnScaleGestureListener
        public void onScaleEnd(ScaleGestureDetector detector) {
            clear();
        }

        @Override // com.android.server.accessibility.MagnificationGestureHandler.State
        public void clear() {
            this.mInitialScaleFactor = -1.0f;
            this.mScaling = false;
        }

        @Override // java.lang.Object
        public String toString() {
            return "PanningScalingState{mInitialScaleFactor=" + this.mInitialScaleFactor + ", mScaling=" + this.mScaling + '}';
        }
    }

    /* access modifiers changed from: package-private */
    public final class ViewportDraggingState implements State {
        private boolean mLastMoveOutsideMagnifiedRegion;
        boolean mZoomedInBeforeDrag;

        ViewportDraggingState() {
        }

        @Override // com.android.server.accessibility.MagnificationGestureHandler.State
        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            int action = event.getActionMasked();
            if (action != 0) {
                if (action != 1) {
                    if (action != 2) {
                        if (action != 3) {
                            if (action == 5) {
                                clear();
                                MagnificationGestureHandler magnificationGestureHandler = MagnificationGestureHandler.this;
                                magnificationGestureHandler.transitionTo(magnificationGestureHandler.mPanningScalingState);
                                return;
                            } else if (action != 6) {
                                return;
                            }
                        }
                    } else if (event.getPointerCount() == 1) {
                        float eventX = event.getX();
                        float eventY = event.getY();
                        if (MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(MagnificationGestureHandler.this.mDisplayId, eventX, eventY)) {
                            MagnificationGestureHandler.this.mMagnificationController.setCenter(MagnificationGestureHandler.this.mDisplayId, eventX, eventY, this.mLastMoveOutsideMagnifiedRegion, 0);
                            this.mLastMoveOutsideMagnifiedRegion = false;
                            return;
                        }
                        this.mLastMoveOutsideMagnifiedRegion = true;
                        return;
                    } else {
                        throw new IllegalStateException("Should have one pointer down.");
                    }
                }
                if (!this.mZoomedInBeforeDrag) {
                    MagnificationGestureHandler.this.zoomOff();
                }
                clear();
                MagnificationGestureHandler magnificationGestureHandler2 = MagnificationGestureHandler.this;
                magnificationGestureHandler2.transitionTo(magnificationGestureHandler2.mDetectingState);
                return;
            }
            Slog.e(MagnificationGestureHandler.LOG_TAG, "Illegal Action: " + action + " curState " + State.nameOf(MagnificationGestureHandler.this.mCurrentState));
            if (!this.mZoomedInBeforeDrag) {
                MagnificationGestureHandler.this.zoomOff();
            }
            clear();
            MagnificationGestureHandler magnificationGestureHandler3 = MagnificationGestureHandler.this;
            magnificationGestureHandler3.transitionTo(magnificationGestureHandler3.mDetectingState);
        }

        @Override // com.android.server.accessibility.MagnificationGestureHandler.State
        public void clear() {
            this.mLastMoveOutsideMagnifiedRegion = false;
        }

        public String toString() {
            return "ViewportDraggingState{mZoomedInBeforeDrag=" + this.mZoomedInBeforeDrag + ", mLastMoveOutsideMagnifiedRegion=" + this.mLastMoveOutsideMagnifiedRegion + '}';
        }
    }

    /* access modifiers changed from: package-private */
    public final class DelegatingState implements State {
        public long mLastDelegatedDownEventTime;

        DelegatingState() {
        }

        @Override // com.android.server.accessibility.MagnificationGestureHandler.State
        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            int actionMasked = event.getActionMasked();
            if (actionMasked == 0) {
                MagnificationGestureHandler magnificationGestureHandler = MagnificationGestureHandler.this;
                magnificationGestureHandler.transitionTo(magnificationGestureHandler.mDelegatingState);
                this.mLastDelegatedDownEventTime = event.getDownTime();
            } else if (actionMasked == 1 || actionMasked == 3) {
                MagnificationGestureHandler magnificationGestureHandler2 = MagnificationGestureHandler.this;
                magnificationGestureHandler2.transitionTo(magnificationGestureHandler2.mDetectingState);
            }
            if (MagnificationGestureHandler.this.getNext() != null) {
                event.setDownTime(this.mLastDelegatedDownEventTime);
                MagnificationGestureHandler.this.dispatchTransformedEvent(event, rawEvent, policyFlags);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class DetectingState implements State, Handler.Callback {
        private static final int MESSAGE_ON_TRIPLE_TAP_AND_HOLD = 1;
        private static final int MESSAGE_TRANSITION_TO_DELEGATING_STATE = 2;
        private MotionEventInfo mDelayedEventQueue;
        @VisibleForTesting
        Handler mHandler = new Handler(Looper.getMainLooper(), this);
        MotionEvent mLastDown;
        private MotionEvent mLastUp;
        final int mLongTapMinDelay = ViewConfiguration.getLongPressTimeout();
        final int mMultiTapMaxDelay;
        final int mMultiTapMaxDistance;
        private MotionEvent mPreLastDown;
        private MotionEvent mPreLastUp;
        @VisibleForTesting
        boolean mShortcutTriggered;
        final int mSwipeMinDistance;

        public DetectingState(Context context) {
            this.mMultiTapMaxDelay = ViewConfiguration.getDoubleTapTimeout() + context.getResources().getInteger(17694890);
            this.mSwipeMinDistance = ViewConfiguration.get(context).getScaledTouchSlop();
            this.mMultiTapMaxDistance = ViewConfiguration.get(context).getScaledDoubleTapSlop();
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message message) {
            int type = message.what;
            if (type == 1) {
                MotionEvent down = (MotionEvent) message.obj;
                transitionToViewportDraggingStateAndClear(down);
                down.recycle();
            } else if (type == 2) {
                transitionToDelegatingStateAndClear();
            } else {
                throw new IllegalArgumentException("Unknown message type: " + type);
            }
            return true;
        }

        @Override // com.android.server.accessibility.MagnificationGestureHandler.State
        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            cacheDelayedMotionEvent(event, rawEvent, policyFlags);
            int actionMasked = event.getActionMasked();
            if (actionMasked == 0) {
                this.mHandler.removeMessages(2);
                if (!MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(MagnificationGestureHandler.this.mDisplayId, event.getX(), event.getY())) {
                    transitionToDelegatingStateAndClear();
                } else if (isMultiTapTriggered(2)) {
                    afterLongTapTimeoutTransitionToDraggingState(event);
                } else if (isTapOutOfDistanceSlop()) {
                    transitionToDelegatingStateAndClear();
                } else if (MagnificationGestureHandler.this.mDetectTripleTap || MagnificationGestureHandler.this.mMagnificationController.isMagnifying(MagnificationGestureHandler.this.mDisplayId)) {
                    afterMultiTapTimeoutTransitionToDelegatingState();
                } else {
                    transitionToDelegatingStateAndClear();
                }
            } else if (actionMasked == 1) {
                this.mHandler.removeMessages(1);
                if (!MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(MagnificationGestureHandler.this.mDisplayId, event.getX(), event.getY())) {
                    transitionToDelegatingStateAndClear();
                } else if (isMultiTapTriggered(3)) {
                    onTripleTap(event);
                } else if (!isFingerDown()) {
                } else {
                    if (timeBetween(this.mLastDown, this.mLastUp) >= ((long) this.mLongTapMinDelay) || GestureUtils.distance(this.mLastDown, this.mLastUp) >= ((double) this.mSwipeMinDistance)) {
                        transitionToDelegatingStateAndClear();
                    }
                }
            } else if (actionMasked != 2) {
                if (actionMasked == 5) {
                    if (MagnificationGestureHandler.this.mMagnificationController.isMagnifying(MagnificationGestureHandler.this.mDisplayId)) {
                        MagnificationGestureHandler magnificationGestureHandler = MagnificationGestureHandler.this;
                        magnificationGestureHandler.transitionTo(magnificationGestureHandler.mPanningScalingState);
                        clear();
                        return;
                    }
                    transitionToDelegatingStateAndClear();
                }
            } else if (isFingerDown() && GestureUtils.distance(this.mLastDown, event) > ((double) this.mSwipeMinDistance)) {
                if (isMultiTapTriggered(2)) {
                    transitionToViewportDraggingStateAndClear(event);
                } else {
                    transitionToDelegatingStateAndClear();
                }
            }
        }

        public boolean isMultiTapTriggered(int numTaps) {
            return this.mShortcutTriggered ? tapCount() + 2 >= numTaps : MagnificationGestureHandler.this.mDetectTripleTap && tapCount() >= numTaps && isMultiTap(this.mPreLastDown, this.mLastDown) && isMultiTap(this.mPreLastUp, this.mLastUp);
        }

        private boolean isMultiTap(MotionEvent first, MotionEvent second) {
            return GestureUtils.isMultiTap(first, second, this.mMultiTapMaxDelay, this.mMultiTapMaxDistance);
        }

        public boolean isFingerDown() {
            return this.mLastDown != null;
        }

        private long timeBetween(MotionEvent a, MotionEvent b) {
            if (a == null && b == null) {
                return 0;
            }
            return Math.abs(timeOf(a) - timeOf(b));
        }

        private long timeOf(MotionEvent event) {
            if (event != null) {
                return event.getEventTime();
            }
            return Long.MIN_VALUE;
        }

        public int tapCount() {
            return MotionEventInfo.countOf(this.mDelayedEventQueue, 1);
        }

        public void afterMultiTapTimeoutTransitionToDelegatingState() {
            this.mHandler.sendEmptyMessageDelayed(2, (long) this.mMultiTapMaxDelay);
        }

        public void afterLongTapTimeoutTransitionToDraggingState(MotionEvent event) {
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(1, MotionEvent.obtain(event)), (long) ViewConfiguration.getLongPressTimeout());
        }

        @Override // com.android.server.accessibility.MagnificationGestureHandler.State
        public void clear() {
            setShortcutTriggered(false);
            removePendingDelayedMessages();
            clearDelayedMotionEvents();
        }

        private void removePendingDelayedMessages() {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
        }

        private void cacheDelayedMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            if (event.getActionMasked() == 0) {
                this.mPreLastDown = this.mLastDown;
                this.mLastDown = MotionEvent.obtain(event);
            } else if (event.getActionMasked() == 1) {
                this.mPreLastUp = this.mLastUp;
                this.mLastUp = MotionEvent.obtain(event);
            }
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
                MagnificationGestureHandler magnificationGestureHandler = MagnificationGestureHandler.this;
                magnificationGestureHandler.handleEventWith(magnificationGestureHandler.mDelegatingState, info.event, info.rawEvent, info.policyFlags);
                info.recycle();
            }
        }

        private void clearDelayedMotionEvents() {
            while (this.mDelayedEventQueue != null) {
                MotionEventInfo info = this.mDelayedEventQueue;
                this.mDelayedEventQueue = info.mNext;
                info.recycle();
            }
            this.mPreLastDown = null;
            this.mPreLastUp = null;
            this.mLastDown = null;
            this.mLastUp = null;
        }

        /* access modifiers changed from: package-private */
        public void transitionToDelegatingStateAndClear() {
            MagnificationGestureHandler magnificationGestureHandler = MagnificationGestureHandler.this;
            magnificationGestureHandler.transitionTo(magnificationGestureHandler.mDelegatingState);
            sendDelayedMotionEvents();
            removePendingDelayedMessages();
        }

        private void onTripleTap(MotionEvent up) {
            clear();
            if (MagnificationGestureHandler.this.mMagnificationController.isMagnifying(MagnificationGestureHandler.this.mDisplayId)) {
                MagnificationGestureHandler.this.zoomOff();
            } else {
                MagnificationGestureHandler.this.zoomOn(up.getX(), up.getY());
            }
        }

        /* access modifiers changed from: package-private */
        public void transitionToViewportDraggingStateAndClear(MotionEvent down) {
            clear();
            MagnificationGestureHandler.this.mViewportDraggingState.mZoomedInBeforeDrag = MagnificationGestureHandler.this.mMagnificationController.isMagnifying(MagnificationGestureHandler.this.mDisplayId);
            MagnificationGestureHandler.this.zoomOn(down.getX(), down.getY());
            MagnificationGestureHandler magnificationGestureHandler = MagnificationGestureHandler.this;
            magnificationGestureHandler.transitionTo(magnificationGestureHandler.mViewportDraggingState);
        }

        @Override // java.lang.Object
        public String toString() {
            return "DetectingState{tapCount()=" + tapCount() + ", mShortcutTriggered=" + this.mShortcutTriggered + ", mDelayedEventQueue=" + MotionEventInfo.toString(this.mDelayedEventQueue) + '}';
        }

        /* access modifiers changed from: package-private */
        public void toggleShortcutTriggered() {
            setShortcutTriggered(!this.mShortcutTriggered);
        }

        /* access modifiers changed from: package-private */
        public void setShortcutTriggered(boolean state) {
            if (this.mShortcutTriggered != state) {
                this.mShortcutTriggered = state;
                MagnificationGestureHandler.this.mMagnificationController.setForceShowMagnifiableBounds(MagnificationGestureHandler.this.mDisplayId, state);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isTapOutOfDistanceSlop() {
            MotionEvent motionEvent;
            MotionEvent motionEvent2;
            if (!MagnificationGestureHandler.this.mDetectTripleTap || (motionEvent = this.mPreLastDown) == null || (motionEvent2 = this.mLastDown) == null) {
                return false;
            }
            boolean outOfDistanceSlop = GestureUtils.distance(motionEvent, motionEvent2) > ((double) this.mMultiTapMaxDistance);
            if (tapCount() > 0) {
                return outOfDistanceSlop;
            }
            if (!outOfDistanceSlop || GestureUtils.isTimedOut(this.mPreLastDown, this.mLastDown, this.mMultiTapMaxDelay)) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void zoomOn(float centerX, float centerY) {
        HwFoldScreenManagerInternal hwFoldScreenManagerInternal;
        int mode;
        if (!this.mIsFoldableScreen || (hwFoldScreenManagerInternal = this.mFsmInternal) == null || (mode = hwFoldScreenManagerInternal.getDisplayMode()) == 1 || mode == 2) {
            this.mMagnificationController.setScaleAndCenter(this.mDisplayId, MathUtils.constrain(this.mMagnificationController.getPersistedScale(), (float) MIN_SCALE, 8.0f), centerX, centerY, true, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void zoomOff() {
        this.mMagnificationController.reset(this.mDisplayId, true);
    }

    /* access modifiers changed from: private */
    public static MotionEvent recycleAndNullify(MotionEvent event) {
        if (event == null) {
            return null;
        }
        event.recycle();
        return null;
    }

    public String toString() {
        return "MagnificationGesture{mDetectingState=" + this.mDetectingState + ", mDelegatingState=" + this.mDelegatingState + ", mMagnifiedInteractionState=" + this.mPanningScalingState + ", mViewportDraggingState=" + this.mViewportDraggingState + ", mDetectTripleTap=" + this.mDetectTripleTap + ", mDetectShortcutTrigger=" + this.mDetectShortcutTrigger + ", mCurrentState=" + State.nameOf(this.mCurrentState) + ", mPreviousState=" + State.nameOf(this.mPreviousState) + ", mMagnificationController=" + this.mMagnificationController + ", mDisplayId=" + this.mDisplayId + '}';
    }

    /* access modifiers changed from: private */
    public static final class MotionEventInfo {
        private static final int MAX_POOL_SIZE = 10;
        private static final Object sLock = new Object();
        private static MotionEventInfo sPool;
        private static int sPoolSize;
        public MotionEvent event;
        private boolean mInPool;
        private MotionEventInfo mNext;
        public int policyFlags;
        public MotionEvent rawEvent;

        private MotionEventInfo() {
        }

        public static MotionEventInfo obtain(MotionEvent event2, MotionEvent rawEvent2, int policyFlags2) {
            MotionEventInfo info;
            synchronized (sLock) {
                info = obtainInternal();
                info.initialize(event2, rawEvent2, policyFlags2);
            }
            return info;
        }

        private static MotionEventInfo obtainInternal() {
            int i = sPoolSize;
            if (i <= 0) {
                return new MotionEventInfo();
            }
            sPoolSize = i - 1;
            MotionEventInfo info = sPool;
            sPool = info.mNext;
            info.mNext = null;
            info.mInPool = false;
            return info;
        }

        private void initialize(MotionEvent event2, MotionEvent rawEvent2, int policyFlags2) {
            this.event = MotionEvent.obtain(event2);
            this.rawEvent = MotionEvent.obtain(rawEvent2);
            this.policyFlags = policyFlags2;
        }

        public void recycle() {
            synchronized (sLock) {
                if (!this.mInPool) {
                    clear();
                    if (sPoolSize < 10) {
                        sPoolSize++;
                        this.mNext = sPool;
                        sPool = this;
                        this.mInPool = true;
                    }
                } else {
                    throw new IllegalStateException("Already recycled.");
                }
            }
        }

        private void clear() {
            this.event = MagnificationGestureHandler.recycleAndNullify(this.event);
            this.rawEvent = MagnificationGestureHandler.recycleAndNullify(this.rawEvent);
            this.policyFlags = 0;
        }

        static int countOf(MotionEventInfo info, int eventType) {
            int i = 0;
            if (info == null) {
                return 0;
            }
            if (info.event.getAction() == eventType) {
                i = 1;
            }
            return i + countOf(info.mNext, eventType);
        }

        public static String toString(MotionEventInfo info) {
            if (info == null) {
                return "";
            }
            return MotionEvent.actionToString(info.event.getAction()).replace("ACTION_", "") + " " + toString(info.mNext);
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

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            this.mGestureHandler.mDetectingState.setShortcutTriggered(false);
        }
    }
}
