package com.android.server.accessibility;

import android.content.Context;
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
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;

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
    private final boolean mDetectControlGestures;
    private final DetectingStateHandler mDetectingStateHandler;
    private final MagnificationController mMagnificationController;
    private final MagnifiedContentInteractionStateHandler mMagnifiedContentInteractionStateHandler;
    private EventStreamTransformation mNext;
    private int mPreviousState;
    private float mScaleX;
    private float mScaleY;
    private final StateViewportDraggingHandler mStateViewportDraggingHandler;
    private PointerCoords[] mTempPointerCoords;
    private PointerProperties[] mTempPointerProperties;
    private boolean mTranslationEnabledBeforePan;

    private interface MotionEventHandler {
        void clear();

        void onMotionEvent(MotionEvent motionEvent, MotionEvent motionEvent2, int i);
    }

    private final class DetectingStateHandler implements MotionEventHandler {
        private static final int ACTION_TAP_COUNT = 3;
        private static final int MESSAGE_ON_ACTION_TAP_AND_HOLD = 1;
        private static final int MESSAGE_TRANSITION_TO_DELEGATING_STATE = 2;
        private MotionEventInfo mDelayedEventQueue;
        private final Handler mHandler;
        private MotionEvent mLastDownEvent;
        private MotionEvent mLastTapUpEvent;
        private final int mMultiTapDistanceSlop;
        private final int mMultiTapTimeSlop;
        private int mTapCount;
        private final int mTapDistanceSlop;
        private final int mTapTimeSlop;

        public DetectingStateHandler(Context context) {
            this.mTapTimeSlop = ViewConfiguration.getJumpTapTimeout();
            this.mHandler = new Handler() {
                public void handleMessage(Message message) {
                    int type = message.what;
                    switch (type) {
                        case DetectingStateHandler.MESSAGE_ON_ACTION_TAP_AND_HOLD /*1*/:
                            DetectingStateHandler.this.onActionTapAndHold(message.obj, message.arg1);
                        case DetectingStateHandler.MESSAGE_TRANSITION_TO_DELEGATING_STATE /*2*/:
                            MagnificationGestureHandler.this.transitionToState(DetectingStateHandler.MESSAGE_ON_ACTION_TAP_AND_HOLD);
                            DetectingStateHandler.this.sendDelayedMotionEvents();
                            DetectingStateHandler.this.clear();
                        default:
                            throw new IllegalArgumentException("Unknown message type: " + type);
                    }
                }
            };
            this.mMultiTapTimeSlop = ViewConfiguration.getDoubleTapTimeout() + context.getResources().getInteger(17694874);
            this.mTapDistanceSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            this.mMultiTapDistanceSlop = ViewConfiguration.get(context).getScaledDoubleTapSlop();
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            cacheDelayedMotionEvent(event, rawEvent, policyFlags);
            switch (event.getActionMasked()) {
                case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                    this.mHandler.removeMessages(MESSAGE_TRANSITION_TO_DELEGATING_STATE);
                    if (MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(event.getX(), event.getY())) {
                        if (this.mTapCount == MESSAGE_TRANSITION_TO_DELEGATING_STATE && this.mLastDownEvent != null && GestureUtils.isMultiTap(this.mLastDownEvent, event, this.mMultiTapTimeSlop, this.mMultiTapDistanceSlop, 0)) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MESSAGE_ON_ACTION_TAP_AND_HOLD, policyFlags, 0, event), (long) ViewConfiguration.getLongPressTimeout());
                        } else if (this.mTapCount < ACTION_TAP_COUNT) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MESSAGE_TRANSITION_TO_DELEGATING_STATE), (long) this.mMultiTapTimeSlop);
                        }
                        clearLastDownEvent();
                        this.mLastDownEvent = MotionEvent.obtain(event);
                        break;
                    }
                    transitionToDelegatingStateAndClear();
                case MESSAGE_ON_ACTION_TAP_AND_HOLD /*1*/:
                    if (this.mLastDownEvent != null) {
                        this.mHandler.removeMessages(MESSAGE_ON_ACTION_TAP_AND_HOLD);
                        if (!MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(event.getX(), event.getY())) {
                            transitionToDelegatingStateAndClear();
                            return;
                        } else if (!GestureUtils.isTap(this.mLastDownEvent, event, this.mTapTimeSlop, this.mTapDistanceSlop, 0)) {
                            transitionToDelegatingStateAndClear();
                            return;
                        } else if (this.mLastTapUpEvent == null || GestureUtils.isMultiTap(this.mLastTapUpEvent, event, this.mMultiTapTimeSlop, this.mMultiTapDistanceSlop, 0)) {
                            this.mTapCount += MESSAGE_ON_ACTION_TAP_AND_HOLD;
                            if (this.mTapCount != ACTION_TAP_COUNT) {
                                clearLastTapUpEvent();
                                this.mLastTapUpEvent = MotionEvent.obtain(event);
                                break;
                            }
                            clear();
                            onActionTap(event, policyFlags);
                            return;
                        } else {
                            transitionToDelegatingStateAndClear();
                            return;
                        }
                    }
                    return;
                    break;
                case MESSAGE_TRANSITION_TO_DELEGATING_STATE /*2*/:
                    if (this.mLastDownEvent != null && this.mTapCount < MESSAGE_TRANSITION_TO_DELEGATING_STATE && Math.abs(GestureUtils.computeDistance(this.mLastDownEvent, event, 0)) > ((double) this.mTapDistanceSlop)) {
                        transitionToDelegatingStateAndClear();
                        break;
                    }
                case H.ADD_STARTING /*5*/:
                    if (!MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                        transitionToDelegatingStateAndClear();
                        break;
                    }
                    MagnificationGestureHandler.this.transitionToState(MagnificationGestureHandler.STATE_MAGNIFIED_INTERACTION);
                    clear();
                    break;
            }
        }

        public void clear() {
            this.mHandler.removeMessages(MESSAGE_ON_ACTION_TAP_AND_HOLD);
            this.mHandler.removeMessages(MESSAGE_TRANSITION_TO_DELEGATING_STATE);
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

        private void transitionToDelegatingStateAndClear() {
            MagnificationGestureHandler.this.transitionToState(MESSAGE_ON_ACTION_TAP_AND_HOLD);
            sendDelayedMotionEvents();
            clear();
        }

        private void onActionTap(MotionEvent up, int policyFlags) {
            if (MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                MagnificationGestureHandler.this.mMagnificationController.reset(true);
                return;
            }
            MagnificationGestureHandler.this.mScaleX = up.getX();
            MagnificationGestureHandler.this.mScaleY = up.getY();
            if (!MagnificationGestureHandler.this.showMagnDialog(MagnificationGestureHandler.this.mContext)) {
                MagnificationGestureHandler.this.mMagnificationController.setScaleAndCenter(MathUtils.constrain(MagnificationGestureHandler.this.mMagnificationController.getPersistedScale(), MagnificationGestureHandler.MIN_SCALE, MagnificationGestureHandler.MAX_SCALE), MagnificationGestureHandler.this.mScaleX, MagnificationGestureHandler.this.mScaleY, true, 0);
            }
        }

        private void onActionTapAndHold(MotionEvent down, int policyFlags) {
            clear();
            MagnificationGestureHandler.this.mTranslationEnabledBeforePan = MagnificationGestureHandler.this.mMagnificationController.isMagnifying();
            MagnificationGestureHandler.this.mMagnificationController.setScaleAndCenter(MathUtils.constrain(MagnificationGestureHandler.this.mMagnificationController.getPersistedScale(), MagnificationGestureHandler.MIN_SCALE, MagnificationGestureHandler.MAX_SCALE), down.getX(), down.getY(), true, 0);
            MagnificationGestureHandler.this.transitionToState(ACTION_TAP_COUNT);
        }
    }

    private final class MagnifiedContentInteractionStateHandler extends SimpleOnGestureListener implements OnScaleGestureListener, MotionEventHandler {
        private final GestureDetector mGestureDetector;
        private float mInitialScaleFactor;
        private final ScaleGestureDetector mScaleGestureDetector;
        private boolean mScaling;
        private final float mScalingThreshold;

        public MagnifiedContentInteractionStateHandler(Context context) {
            this.mInitialScaleFactor = -1.0f;
            TypedValue scaleValue = new TypedValue();
            context.getResources().getValue(17104917, scaleValue, MagnificationGestureHandler.DEBUG_STATE_TRANSITIONS);
            this.mScalingThreshold = scaleValue.getFloat();
            this.mScaleGestureDetector = new ScaleGestureDetector(context, this);
            this.mScaleGestureDetector.setQuickScaleEnabled(MagnificationGestureHandler.DEBUG_STATE_TRANSITIONS);
            this.mGestureDetector = new GestureDetector(context, this);
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            this.mScaleGestureDetector.onTouchEvent(event);
            this.mGestureDetector.onTouchEvent(event);
            if (MagnificationGestureHandler.this.mCurrentState == MagnificationGestureHandler.STATE_MAGNIFIED_INTERACTION && event.getActionMasked() == MagnificationGestureHandler.STATE_DELEGATING) {
                clear();
                MagnificationGestureHandler.this.mMagnificationController.persistScale();
                if (MagnificationGestureHandler.this.mPreviousState == MagnificationGestureHandler.STATE_VIEWPORT_DRAGGING) {
                    MagnificationGestureHandler.this.transitionToState(MagnificationGestureHandler.STATE_VIEWPORT_DRAGGING);
                } else {
                    MagnificationGestureHandler.this.transitionToState(MagnificationGestureHandler.STATE_DETECTING);
                }
            }
        }

        public boolean onScroll(MotionEvent first, MotionEvent second, float distanceX, float distanceY) {
            if (MagnificationGestureHandler.this.mCurrentState != MagnificationGestureHandler.STATE_MAGNIFIED_INTERACTION) {
                return true;
            }
            MagnificationGestureHandler.this.mMagnificationController.offsetMagnifiedRegionCenter(distanceX, distanceY, 0);
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            if (this.mScaling) {
                float scale;
                float initialScale = MagnificationGestureHandler.this.mMagnificationController.getScale();
                float targetScale = initialScale * detector.getScaleFactor();
                if (targetScale > MagnificationGestureHandler.MAX_SCALE && targetScale > initialScale) {
                    scale = MagnificationGestureHandler.MAX_SCALE;
                } else if (targetScale >= MagnificationGestureHandler.MIN_SCALE || targetScale >= initialScale) {
                    scale = targetScale;
                } else {
                    scale = MagnificationGestureHandler.MIN_SCALE;
                }
                MagnificationGestureHandler.this.mMagnificationController.setScale(scale, detector.getFocusX(), detector.getFocusY(), MagnificationGestureHandler.DEBUG_STATE_TRANSITIONS, 0);
                return true;
            }
            if (this.mInitialScaleFactor < 0.0f) {
                this.mInitialScaleFactor = detector.getScaleFactor();
            } else if (Math.abs(detector.getScaleFactor() - this.mInitialScaleFactor) > this.mScalingThreshold) {
                this.mScaling = true;
                return true;
            }
            return MagnificationGestureHandler.DEBUG_STATE_TRANSITIONS;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return MagnificationGestureHandler.this.mCurrentState == MagnificationGestureHandler.STATE_MAGNIFIED_INTERACTION ? true : MagnificationGestureHandler.DEBUG_STATE_TRANSITIONS;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            clear();
        }

        public void clear() {
            this.mInitialScaleFactor = -1.0f;
            this.mScaling = MagnificationGestureHandler.DEBUG_STATE_TRANSITIONS;
        }
    }

    private static final class MotionEventInfo {
        private static final int MAX_POOL_SIZE = 10;
        private static final Object sLock = null;
        private static MotionEventInfo sPool;
        private static int sPoolSize;
        public MotionEvent mEvent;
        private boolean mInPool;
        private MotionEventInfo mNext;
        public int mPolicyFlags;
        public MotionEvent mRawEvent;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.accessibility.MagnificationGestureHandler.MotionEventInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.accessibility.MagnificationGestureHandler.MotionEventInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.MagnificationGestureHandler.MotionEventInfo.<clinit>():void");
        }

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
                    info.mInPool = MagnificationGestureHandler.DEBUG_STATE_TRANSITIONS;
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
                if (sPoolSize < MAX_POOL_SIZE) {
                    sPoolSize += MagnificationGestureHandler.STATE_DELEGATING;
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

    private final class StateViewportDraggingHandler implements MotionEventHandler {
        private boolean mLastMoveOutsideMagnifiedRegion;
        final /* synthetic */ MagnificationGestureHandler this$0;

        /* synthetic */ StateViewportDraggingHandler(MagnificationGestureHandler this$0, StateViewportDraggingHandler stateViewportDraggingHandler) {
            this(this$0);
        }

        private StateViewportDraggingHandler(MagnificationGestureHandler this$0) {
            this.this$0 = this$0;
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            switch (event.getActionMasked()) {
                case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                    throw new IllegalArgumentException("Unexpected event type: ACTION_DOWN");
                case MagnificationGestureHandler.STATE_DELEGATING /*1*/:
                    if (!this.this$0.mTranslationEnabledBeforePan) {
                        this.this$0.mMagnificationController.reset(true);
                    }
                    clear();
                    this.this$0.transitionToState(MagnificationGestureHandler.STATE_DETECTING);
                case MagnificationGestureHandler.STATE_DETECTING /*2*/:
                    if (event.getPointerCount() != MagnificationGestureHandler.STATE_DELEGATING) {
                        throw new IllegalStateException("Should have one pointer down.");
                    }
                    float eventX = event.getX();
                    float eventY = event.getY();
                    if (!this.this$0.mMagnificationController.magnificationRegionContains(eventX, eventY)) {
                        this.mLastMoveOutsideMagnifiedRegion = true;
                    } else if (this.mLastMoveOutsideMagnifiedRegion) {
                        this.mLastMoveOutsideMagnifiedRegion = MagnificationGestureHandler.DEBUG_STATE_TRANSITIONS;
                        this.this$0.mMagnificationController.setCenter(eventX, eventY, true, 0);
                    } else {
                        this.this$0.mMagnificationController.setCenter(eventX, eventY, MagnificationGestureHandler.DEBUG_STATE_TRANSITIONS, 0);
                    }
                case H.ADD_STARTING /*5*/:
                    clear();
                    this.this$0.transitionToState(MagnificationGestureHandler.STATE_MAGNIFIED_INTERACTION);
                case H.REMOVE_STARTING /*6*/:
                    throw new IllegalArgumentException("Unexpected event type: ACTION_POINTER_UP");
                default:
            }
        }

        public void clear() {
            this.mLastMoveOutsideMagnifiedRegion = MagnificationGestureHandler.DEBUG_STATE_TRANSITIONS;
        }
    }

    public MagnificationGestureHandler(Context context, AccessibilityManagerService ams, boolean detectControlGestures) {
        this.mScaleX = 0.0f;
        this.mScaleY = 0.0f;
        this.mContext = context;
        this.mMagnificationController = ams.getMagnificationController();
        this.mDetectingStateHandler = new DetectingStateHandler(context);
        this.mStateViewportDraggingHandler = new StateViewportDraggingHandler();
        this.mMagnifiedContentInteractionStateHandler = new MagnifiedContentInteractionStateHandler(context);
        this.mDetectControlGestures = detectControlGestures;
        transitionToState(STATE_DETECTING);
    }

    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (!event.isFromSource(4098)) {
            if (this.mNext != null) {
                this.mNext.onMotionEvent(event, rawEvent, policyFlags);
            }
        } else if (this.mDetectControlGestures) {
            this.mMagnifiedContentInteractionStateHandler.onMotionEvent(event, rawEvent, policyFlags);
            switch (this.mCurrentState) {
                case STATE_DELEGATING /*1*/:
                    handleMotionEventStateDelegating(event, rawEvent, policyFlags);
                    break;
                case STATE_DETECTING /*2*/:
                    this.mDetectingStateHandler.onMotionEvent(event, rawEvent, policyFlags);
                    break;
                case STATE_VIEWPORT_DRAGGING /*3*/:
                    this.mStateViewportDraggingHandler.onMotionEvent(event, rawEvent, policyFlags);
                    break;
                case STATE_MAGNIFIED_INTERACTION /*4*/:
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
        clear();
    }

    private void clear() {
        this.mCurrentState = STATE_DETECTING;
        this.mDetectingStateHandler.clear();
        this.mStateViewportDraggingHandler.clear();
        this.mMagnifiedContentInteractionStateHandler.clear();
    }

    private void handleMotionEventStateDelegating(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        switch (event.getActionMasked()) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                this.mDelegatingStateDownTime = event.getDownTime();
                break;
            case STATE_DELEGATING /*1*/:
                if (this.mDetectingStateHandler.mDelayedEventQueue == null) {
                    transitionToState(STATE_DETECTING);
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
            for (int i = 0; i < pointerCount; i += STATE_DELEGATING) {
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
        int oldSize;
        if (this.mTempPointerCoords != null) {
            oldSize = this.mTempPointerCoords.length;
        } else {
            oldSize = 0;
        }
        if (oldSize < size) {
            PointerCoords[] oldTempPointerCoords = this.mTempPointerCoords;
            this.mTempPointerCoords = new PointerCoords[size];
            if (oldTempPointerCoords != null) {
                System.arraycopy(oldTempPointerCoords, 0, this.mTempPointerCoords, 0, oldSize);
            }
        }
        for (int i = oldSize; i < size; i += STATE_DELEGATING) {
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
        for (int i = oldSize; i < size; i += STATE_DELEGATING) {
            this.mTempPointerProperties[i] = new PointerProperties();
        }
        return this.mTempPointerProperties;
    }

    private void transitionToState(int state) {
        this.mPreviousState = this.mCurrentState;
        this.mCurrentState = state;
    }

    protected void scaleAndMagnifiedRegionCenter() {
        this.mMagnificationController.setScaleAndCenter(MathUtils.constrain(this.mMagnificationController.getPersistedScale(), MIN_SCALE, MAX_SCALE), this.mScaleX, this.mScaleY, true, 0);
    }
}
