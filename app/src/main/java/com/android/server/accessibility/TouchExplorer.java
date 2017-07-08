package com.android.server.accessibility;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.server.accessibility.AccessibilityGestureDetector.Listener;
import com.android.server.wm.WindowManagerService.H;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class TouchExplorer implements EventStreamTransformation, Listener {
    private static final int ALL_POINTER_ID_BITS = -1;
    private static final int CLICK_LOCATION_ACCESSIBILITY_FOCUS = 1;
    private static final int CLICK_LOCATION_LAST_TOUCH_EXPLORED = 2;
    private static final int CLICK_LOCATION_NONE = 0;
    private static final boolean DEBUG = false;
    private static final int EXIT_GESTURE_DETECTION_TIMEOUT = 2000;
    private static final int INVALID_POINTER_ID = -1;
    private static final String LOG_TAG = "TouchExplorer";
    private static final float MAX_DRAGGING_ANGLE_COS = 0.52532196f;
    private static final int MAX_POINTER_COUNT = 32;
    private static final int MIN_POINTER_DISTANCE_TO_USE_MIDDLE_LOCATION_DIP = 200;
    private static final int STATE_DELEGATING = 4;
    private static final int STATE_DRAGGING = 2;
    private static final int STATE_GESTURE_DETECTING = 5;
    private static final int STATE_TOUCH_EXPLORING = 1;
    private final AccessibilityManagerService mAms;
    private final Context mContext;
    private int mCurrentState;
    private final int mDetermineUserIntentTimeout;
    private final int mDoubleTapSlop;
    private int mDraggingPointerId;
    private final ExitGestureDetectionModeDelayed mExitGestureDetectionModeDelayed;
    private final AccessibilityGestureDetector mGestureDetector;
    private final Handler mHandler;
    private final InjectedPointerTracker mInjectedPointerTracker;
    private int mLastTouchedWindowId;
    private int mLongPressingPointerDeltaX;
    private int mLongPressingPointerDeltaY;
    private int mLongPressingPointerId;
    private EventStreamTransformation mNext;
    private final ReceivedPointerTracker mReceivedPointerTracker;
    private final int mScaledMinPointerDistanceToUseMiddleLocation;
    private final SendHoverEnterAndMoveDelayed mSendHoverEnterAndMoveDelayed;
    private final SendHoverExitDelayed mSendHoverExitDelayed;
    private final SendAccessibilityEventDelayed mSendTouchExplorationEndDelayed;
    private final SendAccessibilityEventDelayed mSendTouchInteractionEndDelayed;
    private final Point mTempPoint;
    private boolean mTouchExplorationInProgress;

    private final class ExitGestureDetectionModeDelayed implements Runnable {
        private ExitGestureDetectionModeDelayed() {
        }

        public void post() {
            TouchExplorer.this.mHandler.postDelayed(this, 2000);
        }

        public void cancel() {
            TouchExplorer.this.mHandler.removeCallbacks(this);
        }

        public void run() {
            TouchExplorer.this.sendAccessibilityEvent(DumpState.DUMP_FROZEN);
            TouchExplorer.this.sendAccessibilityEvent(DumpState.DUMP_MESSAGES);
            TouchExplorer.this.clear();
        }
    }

    class InjectedPointerTracker {
        private static final String LOG_TAG_INJECTED_POINTER_TRACKER = "InjectedPointerTracker";
        private int mInjectedPointersDown;
        private long mLastInjectedDownEventTime;
        private MotionEvent mLastInjectedHoverEvent;
        private MotionEvent mLastInjectedHoverEventForClick;

        public void onMotionEvent(android.view.MotionEvent r1) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.accessibility.TouchExplorer.InjectedPointerTracker.onMotionEvent(android.view.MotionEvent):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.TouchExplorer.InjectedPointerTracker.onMotionEvent(android.view.MotionEvent):void");
        }

        InjectedPointerTracker() {
        }

        public void clear() {
            this.mInjectedPointersDown = TouchExplorer.CLICK_LOCATION_NONE;
        }

        public long getLastInjectedDownEventTime() {
            return this.mLastInjectedDownEventTime;
        }

        public int getInjectedPointerDownCount() {
            return Integer.bitCount(this.mInjectedPointersDown);
        }

        public int getInjectedPointersDown() {
            return this.mInjectedPointersDown;
        }

        public boolean isInjectedPointerDown(int pointerId) {
            if ((this.mInjectedPointersDown & (TouchExplorer.STATE_TOUCH_EXPLORING << pointerId)) != 0) {
                return true;
            }
            return TouchExplorer.DEBUG;
        }

        public MotionEvent getLastInjectedHoverEvent() {
            return this.mLastInjectedHoverEvent;
        }

        public MotionEvent getLastInjectedHoverEventForClick() {
            return this.mLastInjectedHoverEventForClick;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("=========================");
            builder.append("\nDown pointers #");
            builder.append(Integer.bitCount(this.mInjectedPointersDown));
            builder.append(" [ ");
            for (int i = TouchExplorer.CLICK_LOCATION_NONE; i < TouchExplorer.MAX_POINTER_COUNT; i += TouchExplorer.STATE_TOUCH_EXPLORING) {
                if ((this.mInjectedPointersDown & i) != 0) {
                    builder.append(i);
                    builder.append(" ");
                }
            }
            builder.append("]");
            builder.append("\n=========================");
            return builder.toString();
        }
    }

    class ReceivedPointerTracker {
        private static final String LOG_TAG_RECEIVED_POINTER_TRACKER = "ReceivedPointerTracker";
        private int mLastReceivedDownEdgeFlags;
        private MotionEvent mLastReceivedEvent;
        private long mLastReceivedUpPointerDownTime;
        private float mLastReceivedUpPointerDownX;
        private float mLastReceivedUpPointerDownY;
        private int mPrimaryPointerId;
        private final long[] mReceivedPointerDownTime;
        private final float[] mReceivedPointerDownX;
        private final float[] mReceivedPointerDownY;
        private int mReceivedPointersDown;

        private int findPrimaryPointerId() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.accessibility.TouchExplorer.ReceivedPointerTracker.findPrimaryPointerId():int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.TouchExplorer.ReceivedPointerTracker.findPrimaryPointerId():int");
        }

        private void handleReceivedPointerUp(int r1, android.view.MotionEvent r2) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.accessibility.TouchExplorer.ReceivedPointerTracker.handleReceivedPointerUp(int, android.view.MotionEvent):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.TouchExplorer.ReceivedPointerTracker.handleReceivedPointerUp(int, android.view.MotionEvent):void");
        }

        ReceivedPointerTracker() {
            this.mReceivedPointerDownX = new float[TouchExplorer.MAX_POINTER_COUNT];
            this.mReceivedPointerDownY = new float[TouchExplorer.MAX_POINTER_COUNT];
            this.mReceivedPointerDownTime = new long[TouchExplorer.MAX_POINTER_COUNT];
        }

        public void clear() {
            Arrays.fill(this.mReceivedPointerDownX, 0.0f);
            Arrays.fill(this.mReceivedPointerDownY, 0.0f);
            Arrays.fill(this.mReceivedPointerDownTime, 0);
            this.mReceivedPointersDown = TouchExplorer.CLICK_LOCATION_NONE;
            this.mPrimaryPointerId = TouchExplorer.CLICK_LOCATION_NONE;
            this.mLastReceivedUpPointerDownTime = 0;
            this.mLastReceivedUpPointerDownX = 0.0f;
            this.mLastReceivedUpPointerDownY = 0.0f;
        }

        public void onMotionEvent(MotionEvent event) {
            if (this.mLastReceivedEvent != null) {
                this.mLastReceivedEvent.recycle();
            }
            this.mLastReceivedEvent = MotionEvent.obtain(event);
            switch (event.getActionMasked()) {
                case TouchExplorer.CLICK_LOCATION_NONE /*0*/:
                    handleReceivedPointerDown(event.getActionIndex(), event);
                case TouchExplorer.STATE_TOUCH_EXPLORING /*1*/:
                    handleReceivedPointerUp(event.getActionIndex(), event);
                case TouchExplorer.STATE_GESTURE_DETECTING /*5*/:
                    handleReceivedPointerDown(event.getActionIndex(), event);
                case H.REMOVE_STARTING /*6*/:
                    handleReceivedPointerUp(event.getActionIndex(), event);
                default:
            }
        }

        public MotionEvent getLastReceivedEvent() {
            return this.mLastReceivedEvent;
        }

        public int getReceivedPointerDownCount() {
            return Integer.bitCount(this.mReceivedPointersDown);
        }

        public boolean isReceivedPointerDown(int pointerId) {
            if ((this.mReceivedPointersDown & (TouchExplorer.STATE_TOUCH_EXPLORING << pointerId)) != 0) {
                return true;
            }
            return TouchExplorer.DEBUG;
        }

        public float getReceivedPointerDownX(int pointerId) {
            return this.mReceivedPointerDownX[pointerId];
        }

        public float getReceivedPointerDownY(int pointerId) {
            return this.mReceivedPointerDownY[pointerId];
        }

        public long getReceivedPointerDownTime(int pointerId) {
            return this.mReceivedPointerDownTime[pointerId];
        }

        public int getPrimaryPointerId() {
            if (this.mPrimaryPointerId == TouchExplorer.INVALID_POINTER_ID) {
                this.mPrimaryPointerId = findPrimaryPointerId();
            }
            return this.mPrimaryPointerId;
        }

        public long getLastReceivedUpPointerDownTime() {
            return this.mLastReceivedUpPointerDownTime;
        }

        public float getLastReceivedUpPointerDownX() {
            return this.mLastReceivedUpPointerDownX;
        }

        public float getLastReceivedUpPointerDownY() {
            return this.mLastReceivedUpPointerDownY;
        }

        public int getLastReceivedDownEdgeFlags() {
            return this.mLastReceivedDownEdgeFlags;
        }

        private void handleReceivedPointerDown(int pointerIndex, MotionEvent event) {
            int pointerId = event.getPointerId(pointerIndex);
            int pointerFlag = TouchExplorer.STATE_TOUCH_EXPLORING << pointerId;
            this.mLastReceivedUpPointerDownTime = 0;
            this.mLastReceivedUpPointerDownX = 0.0f;
            this.mLastReceivedUpPointerDownX = 0.0f;
            this.mLastReceivedDownEdgeFlags = event.getEdgeFlags();
            this.mReceivedPointersDown |= pointerFlag;
            this.mReceivedPointerDownX[pointerId] = event.getX(pointerIndex);
            this.mReceivedPointerDownY[pointerId] = event.getY(pointerIndex);
            this.mReceivedPointerDownTime[pointerId] = event.getEventTime();
            this.mPrimaryPointerId = pointerId;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("=========================");
            builder.append("\nDown pointers #");
            builder.append(getReceivedPointerDownCount());
            builder.append(" [ ");
            for (int i = TouchExplorer.CLICK_LOCATION_NONE; i < TouchExplorer.MAX_POINTER_COUNT; i += TouchExplorer.STATE_TOUCH_EXPLORING) {
                if (isReceivedPointerDown(i)) {
                    builder.append(i);
                    builder.append(" ");
                }
            }
            builder.append("]");
            builder.append("\nPrimary pointer id [ ");
            builder.append(getPrimaryPointerId());
            builder.append(" ]");
            builder.append("\n=========================");
            return builder.toString();
        }
    }

    private class SendAccessibilityEventDelayed implements Runnable {
        private final int mDelay;
        private final int mEventType;

        public SendAccessibilityEventDelayed(int eventType, int delay) {
            this.mEventType = eventType;
            this.mDelay = delay;
        }

        public void cancel() {
            TouchExplorer.this.mHandler.removeCallbacks(this);
        }

        public void post() {
            TouchExplorer.this.mHandler.postDelayed(this, (long) this.mDelay);
        }

        public boolean isPending() {
            return TouchExplorer.this.mHandler.hasCallbacks(this);
        }

        public void forceSendAndRemove() {
            if (isPending()) {
                run();
                cancel();
            }
        }

        public void run() {
            TouchExplorer.this.sendAccessibilityEvent(this.mEventType);
        }
    }

    class SendHoverEnterAndMoveDelayed implements Runnable {
        private final String LOG_TAG_SEND_HOVER_DELAYED;
        private final List<MotionEvent> mEvents;
        private int mPointerIdBits;
        private int mPolicyFlags;

        SendHoverEnterAndMoveDelayed() {
            this.LOG_TAG_SEND_HOVER_DELAYED = "SendHoverEnterAndMoveDelayed";
            this.mEvents = new ArrayList();
        }

        public void post(MotionEvent event, boolean touchExplorationInProgress, int pointerIdBits, int policyFlags) {
            cancel();
            addEvent(event);
            this.mPointerIdBits = pointerIdBits;
            this.mPolicyFlags = policyFlags;
            TouchExplorer.this.mHandler.postDelayed(this, (long) TouchExplorer.this.mDetermineUserIntentTimeout);
        }

        public void addEvent(MotionEvent event) {
            this.mEvents.add(MotionEvent.obtain(event));
        }

        public void cancel() {
            if (isPending()) {
                TouchExplorer.this.mHandler.removeCallbacks(this);
                clear();
            }
        }

        private boolean isPending() {
            return TouchExplorer.this.mHandler.hasCallbacks(this);
        }

        private void clear() {
            this.mPointerIdBits = TouchExplorer.INVALID_POINTER_ID;
            this.mPolicyFlags = TouchExplorer.CLICK_LOCATION_NONE;
            for (int i = this.mEvents.size() + TouchExplorer.INVALID_POINTER_ID; i >= 0; i += TouchExplorer.INVALID_POINTER_ID) {
                ((MotionEvent) this.mEvents.remove(i)).recycle();
            }
        }

        public void forceSendAndRemove() {
            if (isPending()) {
                run();
                cancel();
            }
        }

        public void run() {
            TouchExplorer.this.sendAccessibilityEvent(DumpState.DUMP_MESSAGES);
            if (!this.mEvents.isEmpty()) {
                TouchExplorer.this.sendMotionEvent((MotionEvent) this.mEvents.get(TouchExplorer.CLICK_LOCATION_NONE), 9, this.mPointerIdBits, this.mPolicyFlags);
                int eventCount = this.mEvents.size();
                for (int i = TouchExplorer.STATE_TOUCH_EXPLORING; i < eventCount; i += TouchExplorer.STATE_TOUCH_EXPLORING) {
                    TouchExplorer.this.sendMotionEvent((MotionEvent) this.mEvents.get(i), 7, this.mPointerIdBits, this.mPolicyFlags);
                }
            }
            clear();
        }
    }

    class SendHoverExitDelayed implements Runnable {
        private final String LOG_TAG_SEND_HOVER_DELAYED;
        private int mPointerIdBits;
        private int mPolicyFlags;
        private MotionEvent mPrototype;

        SendHoverExitDelayed() {
            this.LOG_TAG_SEND_HOVER_DELAYED = "SendHoverExitDelayed";
        }

        public void post(MotionEvent prototype, int pointerIdBits, int policyFlags) {
            cancel();
            this.mPrototype = MotionEvent.obtain(prototype);
            this.mPointerIdBits = pointerIdBits;
            this.mPolicyFlags = policyFlags;
            TouchExplorer.this.mHandler.postDelayed(this, (long) TouchExplorer.this.mDetermineUserIntentTimeout);
        }

        public void cancel() {
            if (isPending()) {
                TouchExplorer.this.mHandler.removeCallbacks(this);
                clear();
            }
        }

        private boolean isPending() {
            return TouchExplorer.this.mHandler.hasCallbacks(this);
        }

        private void clear() {
            this.mPrototype.recycle();
            this.mPrototype = null;
            this.mPointerIdBits = TouchExplorer.INVALID_POINTER_ID;
            this.mPolicyFlags = TouchExplorer.CLICK_LOCATION_NONE;
        }

        public void forceSendAndRemove() {
            if (isPending()) {
                run();
                cancel();
            }
        }

        public void run() {
            TouchExplorer.this.sendMotionEvent(this.mPrototype, 10, this.mPointerIdBits, this.mPolicyFlags);
            if (!TouchExplorer.this.mSendTouchExplorationEndDelayed.isPending()) {
                TouchExplorer.this.mSendTouchExplorationEndDelayed.cancel();
                TouchExplorer.this.mSendTouchExplorationEndDelayed.post();
            }
            if (TouchExplorer.this.mSendTouchInteractionEndDelayed.isPending()) {
                TouchExplorer.this.mSendTouchInteractionEndDelayed.cancel();
                TouchExplorer.this.mSendTouchInteractionEndDelayed.post();
            }
            clear();
        }
    }

    public TouchExplorer(Context context, AccessibilityManagerService service) {
        this.mCurrentState = STATE_TOUCH_EXPLORING;
        this.mTempPoint = new Point();
        this.mLongPressingPointerId = INVALID_POINTER_ID;
        this.mContext = context;
        this.mAms = service;
        this.mReceivedPointerTracker = new ReceivedPointerTracker();
        this.mInjectedPointerTracker = new InjectedPointerTracker();
        this.mDetermineUserIntentTimeout = ViewConfiguration.getDoubleTapTimeout();
        this.mDoubleTapSlop = ViewConfiguration.get(context).getScaledDoubleTapSlop();
        this.mHandler = new Handler(context.getMainLooper());
        this.mExitGestureDetectionModeDelayed = new ExitGestureDetectionModeDelayed();
        this.mSendHoverEnterAndMoveDelayed = new SendHoverEnterAndMoveDelayed();
        this.mSendHoverExitDelayed = new SendHoverExitDelayed();
        this.mSendTouchExplorationEndDelayed = new SendAccessibilityEventDelayed(DumpState.DUMP_PROVIDERS, this.mDetermineUserIntentTimeout);
        this.mSendTouchInteractionEndDelayed = new SendAccessibilityEventDelayed(2097152, this.mDetermineUserIntentTimeout);
        this.mGestureDetector = new AccessibilityGestureDetector(context, this);
        this.mScaledMinPointerDistanceToUseMiddleLocation = (int) (200.0f * context.getResources().getDisplayMetrics().density);
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
        if (this.mReceivedPointerTracker.getLastReceivedEvent() != null) {
            clear(this.mReceivedPointerTracker.getLastReceivedEvent(), 33554432);
        }
    }

    private void clear(MotionEvent event, int policyFlags) {
        switch (this.mCurrentState) {
            case STATE_TOUCH_EXPLORING /*1*/:
                sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
                break;
            case STATE_DRAGGING /*2*/:
                this.mDraggingPointerId = INVALID_POINTER_ID;
                sendUpForInjectedDownPointers(event, policyFlags);
                break;
            case STATE_DELEGATING /*4*/:
                sendUpForInjectedDownPointers(event, policyFlags);
                break;
        }
        this.mSendHoverEnterAndMoveDelayed.cancel();
        this.mSendHoverExitDelayed.cancel();
        this.mExitGestureDetectionModeDelayed.cancel();
        this.mSendTouchExplorationEndDelayed.cancel();
        this.mSendTouchInteractionEndDelayed.cancel();
        this.mReceivedPointerTracker.clear();
        this.mInjectedPointerTracker.clear();
        this.mGestureDetector.clear();
        this.mLongPressingPointerId = INVALID_POINTER_ID;
        this.mLongPressingPointerDeltaX = CLICK_LOCATION_NONE;
        this.mLongPressingPointerDeltaY = CLICK_LOCATION_NONE;
        this.mCurrentState = STATE_TOUCH_EXPLORING;
        this.mTouchExplorationInProgress = DEBUG;
        this.mAms.onTouchInteractionEnd();
    }

    public void setNext(EventStreamTransformation next) {
        this.mNext = next;
    }

    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (event.isFromSource(4098)) {
            this.mReceivedPointerTracker.onMotionEvent(rawEvent);
            if (!this.mGestureDetector.onMotionEvent(rawEvent, policyFlags)) {
                if (event.getActionMasked() == 3) {
                    clear(event, policyFlags);
                    return;
                }
                switch (this.mCurrentState) {
                    case STATE_TOUCH_EXPLORING /*1*/:
                        handleMotionEventStateTouchExploring(event, rawEvent, policyFlags);
                        break;
                    case STATE_DRAGGING /*2*/:
                        handleMotionEventStateDragging(event, policyFlags);
                        break;
                    case STATE_DELEGATING /*4*/:
                        handleMotionEventStateDelegating(event, policyFlags);
                        break;
                    case STATE_GESTURE_DETECTING /*5*/:
                        break;
                    default:
                        throw new IllegalStateException("Illegal state: " + this.mCurrentState);
                }
                return;
            }
            return;
        }
        if (this.mNext != null) {
            this.mNext.onMotionEvent(event, rawEvent, policyFlags);
        }
    }

    public void onKeyEvent(KeyEvent event, int policyFlags) {
        if (this.mNext != null) {
            this.mNext.onKeyEvent(event, policyFlags);
        }
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        if (this.mSendTouchExplorationEndDelayed.isPending() && eventType == DumpState.DUMP_SHARED_USERS) {
            this.mSendTouchExplorationEndDelayed.cancel();
            sendAccessibilityEvent(DumpState.DUMP_PROVIDERS);
        }
        if (this.mSendTouchInteractionEndDelayed.isPending() && eventType == DumpState.DUMP_SHARED_USERS) {
            this.mSendTouchInteractionEndDelayed.cancel();
            sendAccessibilityEvent(2097152);
        }
        switch (eventType) {
            case MAX_POINTER_COUNT /*32*/:
            case DumpState.DUMP_VERSION /*32768*/:
                if (this.mInjectedPointerTracker.mLastInjectedHoverEventForClick != null) {
                    this.mInjectedPointerTracker.mLastInjectedHoverEventForClick.recycle();
                    this.mInjectedPointerTracker.mLastInjectedHoverEventForClick = null;
                }
                this.mLastTouchedWindowId = INVALID_POINTER_ID;
                break;
            case DumpState.DUMP_PACKAGES /*128*/:
            case DumpState.DUMP_SHARED_USERS /*256*/:
                this.mLastTouchedWindowId = event.getWindowId();
                break;
        }
        if (this.mNext != null) {
            this.mNext.onAccessibilityEvent(event);
        }
    }

    public void onDoubleTapAndHold(MotionEvent event, int policyFlags) {
        if (this.mCurrentState == STATE_TOUCH_EXPLORING && this.mReceivedPointerTracker.getLastReceivedEvent().getPointerCount() != 0) {
            int pointerIndex = event.getActionIndex();
            int pointerId = event.getPointerId(pointerIndex);
            Point clickLocation = this.mTempPoint;
            if (computeClickLocation(clickLocation) != 0) {
                this.mLongPressingPointerId = pointerId;
                this.mLongPressingPointerDeltaX = ((int) event.getX(pointerIndex)) - clickLocation.x;
                this.mLongPressingPointerDeltaY = ((int) event.getY(pointerIndex)) - clickLocation.y;
                sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
                this.mCurrentState = STATE_DELEGATING;
                sendDownForAllNotInjectedPointers(event, policyFlags);
            }
        }
    }

    public boolean onDoubleTap(MotionEvent event, int policyFlags) {
        if (this.mCurrentState != STATE_TOUCH_EXPLORING) {
            return DEBUG;
        }
        this.mSendHoverEnterAndMoveDelayed.cancel();
        this.mSendHoverExitDelayed.cancel();
        if (this.mSendTouchExplorationEndDelayed.isPending()) {
            this.mSendTouchExplorationEndDelayed.forceSendAndRemove();
        }
        if (this.mSendTouchInteractionEndDelayed.isPending()) {
            this.mSendTouchInteractionEndDelayed.forceSendAndRemove();
        }
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        Point clickLocation = this.mTempPoint;
        int result = computeClickLocation(clickLocation);
        if (result == 0) {
            return true;
        }
        PointerProperties[] properties = new PointerProperties[STATE_TOUCH_EXPLORING];
        properties[CLICK_LOCATION_NONE] = new PointerProperties();
        event.getPointerProperties(pointerIndex, properties[CLICK_LOCATION_NONE]);
        PointerCoords[] coords = new PointerCoords[STATE_TOUCH_EXPLORING];
        coords[CLICK_LOCATION_NONE] = new PointerCoords();
        coords[CLICK_LOCATION_NONE].x = (float) clickLocation.x;
        coords[CLICK_LOCATION_NONE].y = (float) clickLocation.y;
        MotionEvent click_event = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), CLICK_LOCATION_NONE, STATE_TOUCH_EXPLORING, properties, coords, CLICK_LOCATION_NONE, CLICK_LOCATION_NONE, 1.0f, 1.0f, event.getDeviceId(), CLICK_LOCATION_NONE, event.getSource(), event.getFlags());
        sendActionDownAndUp(click_event, policyFlags, result == STATE_TOUCH_EXPLORING ? true : DEBUG);
        click_event.recycle();
        return true;
    }

    public boolean onGestureStarted() {
        this.mCurrentState = STATE_GESTURE_DETECTING;
        this.mSendHoverEnterAndMoveDelayed.cancel();
        this.mSendHoverExitDelayed.cancel();
        this.mExitGestureDetectionModeDelayed.post();
        sendAccessibilityEvent(DumpState.DUMP_DOMAIN_PREFERRED);
        return DEBUG;
    }

    public boolean onGestureCompleted(int gestureId) {
        if (this.mCurrentState != STATE_GESTURE_DETECTING) {
            return DEBUG;
        }
        endGestureDetection();
        this.mAms.onGesture(gestureId);
        return true;
    }

    public boolean onGestureCancelled(MotionEvent event, int policyFlags) {
        if (this.mCurrentState == STATE_GESTURE_DETECTING) {
            endGestureDetection();
            return true;
        } else if (this.mCurrentState != STATE_TOUCH_EXPLORING || event.getActionMasked() != STATE_DRAGGING) {
            return DEBUG;
        } else {
            int pointerIdBits = STATE_TOUCH_EXPLORING << this.mReceivedPointerTracker.getPrimaryPointerId();
            this.mSendHoverEnterAndMoveDelayed.addEvent(event);
            this.mSendHoverEnterAndMoveDelayed.forceSendAndRemove();
            this.mSendHoverExitDelayed.cancel();
            sendMotionEvent(event, 7, pointerIdBits, policyFlags);
            return true;
        }
    }

    private void handleMotionEventStateTouchExploring(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        ReceivedPointerTracker receivedTracker = this.mReceivedPointerTracker;
        int pointerIdBits;
        switch (event.getActionMasked()) {
            case CLICK_LOCATION_NONE /*0*/:
                this.mAms.onTouchInteractionStart();
                sendAccessibilityEvent(DumpState.DUMP_DEXOPT);
                this.mSendHoverEnterAndMoveDelayed.cancel();
                this.mSendHoverExitDelayed.cancel();
                if (this.mSendTouchExplorationEndDelayed.isPending()) {
                    this.mSendTouchExplorationEndDelayed.forceSendAndRemove();
                }
                if (this.mSendTouchInteractionEndDelayed.isPending()) {
                    this.mSendTouchInteractionEndDelayed.forceSendAndRemove();
                }
                if (!this.mGestureDetector.firstTapDetected() && !this.mTouchExplorationInProgress) {
                    if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                        this.mSendHoverEnterAndMoveDelayed.addEvent(event);
                        return;
                    }
                    this.mSendHoverEnterAndMoveDelayed.post(event, true, STATE_TOUCH_EXPLORING << receivedTracker.getPrimaryPointerId(), policyFlags);
                }
            case STATE_TOUCH_EXPLORING /*1*/:
                this.mAms.onTouchInteractionEnd();
                pointerIdBits = STATE_TOUCH_EXPLORING << event.getPointerId(event.getActionIndex());
                if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                    this.mSendHoverExitDelayed.post(event, pointerIdBits, policyFlags);
                } else {
                    sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
                }
                if (!this.mSendTouchInteractionEndDelayed.isPending()) {
                    this.mSendTouchInteractionEndDelayed.post();
                }
            case STATE_DRAGGING /*2*/:
                int pointerId = receivedTracker.getPrimaryPointerId();
                int pointerIndex = event.findPointerIndex(pointerId);
                pointerIdBits = STATE_TOUCH_EXPLORING << pointerId;
                switch (event.getPointerCount()) {
                    case STATE_TOUCH_EXPLORING /*1*/:
                        if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                            this.mSendHoverEnterAndMoveDelayed.addEvent(event);
                        } else if (this.mTouchExplorationInProgress) {
                            sendTouchExplorationGestureStartAndHoverEnterIfNeeded(policyFlags);
                            sendMotionEvent(event, 7, pointerIdBits, policyFlags);
                        }
                    case STATE_DRAGGING /*2*/:
                        if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                            this.mSendHoverEnterAndMoveDelayed.cancel();
                            this.mSendHoverExitDelayed.cancel();
                        } else if (this.mTouchExplorationInProgress) {
                            if (Math.hypot((double) (receivedTracker.getReceivedPointerDownX(pointerId) - rawEvent.getX(pointerIndex)), (double) (receivedTracker.getReceivedPointerDownY(pointerId) - rawEvent.getY(pointerIndex))) >= ((double) this.mDoubleTapSlop)) {
                                sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
                            } else {
                                return;
                            }
                        }
                        if (isDraggingGesture(event)) {
                            this.mCurrentState = STATE_DRAGGING;
                            this.mDraggingPointerId = pointerId;
                            event.setEdgeFlags(receivedTracker.getLastReceivedDownEdgeFlags());
                            sendMotionEvent(event, CLICK_LOCATION_NONE, pointerIdBits, policyFlags);
                            return;
                        }
                        this.mCurrentState = STATE_DELEGATING;
                        sendDownForAllNotInjectedPointers(event, policyFlags);
                    default:
                        if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                            this.mSendHoverEnterAndMoveDelayed.cancel();
                            this.mSendHoverExitDelayed.cancel();
                        } else {
                            sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
                        }
                        this.mCurrentState = STATE_DELEGATING;
                        sendDownForAllNotInjectedPointers(event, policyFlags);
                }
            case STATE_GESTURE_DETECTING /*5*/:
                this.mSendHoverEnterAndMoveDelayed.cancel();
                this.mSendHoverExitDelayed.cancel();
            default:
        }
    }

    private void handleMotionEventStateDragging(MotionEvent event, int policyFlags) {
        int pointerIdBits = CLICK_LOCATION_NONE;
        if (event.findPointerIndex(this.mDraggingPointerId) == INVALID_POINTER_ID) {
            Slog.e(LOG_TAG, "mDraggingPointerId doesn't match any pointers on current event. mDraggingPointerId: " + Integer.toString(this.mDraggingPointerId) + ", Event: " + event);
            this.mDraggingPointerId = INVALID_POINTER_ID;
        } else {
            pointerIdBits = STATE_TOUCH_EXPLORING << this.mDraggingPointerId;
        }
        switch (event.getActionMasked()) {
            case CLICK_LOCATION_NONE /*0*/:
                throw new IllegalStateException("Dragging state can be reached only if two pointers are already down");
            case STATE_TOUCH_EXPLORING /*1*/:
                this.mAms.onTouchInteractionEnd();
                sendAccessibilityEvent(2097152);
                if (event.getPointerId(event.getActionIndex()) == this.mDraggingPointerId) {
                    this.mDraggingPointerId = INVALID_POINTER_ID;
                    sendMotionEvent(event, STATE_TOUCH_EXPLORING, pointerIdBits, policyFlags);
                }
                this.mCurrentState = STATE_TOUCH_EXPLORING;
            case STATE_DRAGGING /*2*/:
                if (this.mDraggingPointerId != INVALID_POINTER_ID) {
                    switch (event.getPointerCount()) {
                        case STATE_TOUCH_EXPLORING /*1*/:
                        case STATE_DRAGGING /*2*/:
                            if (isDraggingGesture(event)) {
                                float firstPtrX = event.getX(CLICK_LOCATION_NONE);
                                float firstPtrY = event.getY(CLICK_LOCATION_NONE);
                                float deltaX = firstPtrX - event.getX(STATE_TOUCH_EXPLORING);
                                float deltaY = firstPtrY - event.getY(STATE_TOUCH_EXPLORING);
                                if (Math.hypot((double) deltaX, (double) deltaY) > ((double) this.mScaledMinPointerDistanceToUseMiddleLocation)) {
                                    event.setLocation(deltaX / 2.0f, deltaY / 2.0f);
                                }
                                sendMotionEvent(event, STATE_DRAGGING, pointerIdBits, policyFlags);
                                return;
                            }
                            this.mCurrentState = STATE_DELEGATING;
                            sendMotionEvent(event, STATE_TOUCH_EXPLORING, pointerIdBits, policyFlags);
                            sendDownForAllNotInjectedPointers(event, policyFlags);
                        default:
                            this.mCurrentState = STATE_DELEGATING;
                            sendMotionEvent(event, STATE_TOUCH_EXPLORING, pointerIdBits, policyFlags);
                            sendDownForAllNotInjectedPointers(event, policyFlags);
                    }
                }
            case STATE_GESTURE_DETECTING /*5*/:
                this.mCurrentState = STATE_DELEGATING;
                if (this.mDraggingPointerId != INVALID_POINTER_ID) {
                    sendMotionEvent(event, STATE_TOUCH_EXPLORING, pointerIdBits, policyFlags);
                }
                sendDownForAllNotInjectedPointers(event, policyFlags);
            case H.REMOVE_STARTING /*6*/:
                if (event.getPointerId(event.getActionIndex()) == this.mDraggingPointerId) {
                    this.mDraggingPointerId = INVALID_POINTER_ID;
                    sendMotionEvent(event, STATE_TOUCH_EXPLORING, pointerIdBits, policyFlags);
                }
            default:
        }
    }

    private void handleMotionEventStateDelegating(MotionEvent event, int policyFlags) {
        switch (event.getActionMasked()) {
            case CLICK_LOCATION_NONE /*0*/:
                throw new IllegalStateException("Delegating state can only be reached if there is at least one pointer down!");
            case STATE_TOUCH_EXPLORING /*1*/:
                if (this.mLongPressingPointerId >= 0) {
                    event = offsetEvent(event, -this.mLongPressingPointerDeltaX, -this.mLongPressingPointerDeltaY);
                    this.mLongPressingPointerId = INVALID_POINTER_ID;
                    this.mLongPressingPointerDeltaX = CLICK_LOCATION_NONE;
                    this.mLongPressingPointerDeltaY = CLICK_LOCATION_NONE;
                }
                sendMotionEvent(event, event.getAction(), INVALID_POINTER_ID, policyFlags);
                this.mAms.onTouchInteractionEnd();
                sendAccessibilityEvent(2097152);
                this.mCurrentState = STATE_TOUCH_EXPLORING;
            default:
                sendMotionEvent(event, event.getAction(), INVALID_POINTER_ID, policyFlags);
        }
    }

    private void endGestureDetection() {
        this.mAms.onTouchInteractionEnd();
        sendAccessibilityEvent(DumpState.DUMP_FROZEN);
        sendAccessibilityEvent(2097152);
        this.mExitGestureDetectionModeDelayed.cancel();
        this.mCurrentState = STATE_TOUCH_EXPLORING;
    }

    private void sendAccessibilityEvent(int type) {
        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(this.mContext);
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(type);
            event.setWindowId(this.mAms.getActiveWindowId());
            accessibilityManager.sendAccessibilityEvent(event);
            switch (type) {
                case DumpState.DUMP_MESSAGES /*512*/:
                    this.mTouchExplorationInProgress = true;
                case DumpState.DUMP_PROVIDERS /*1024*/:
                    this.mTouchExplorationInProgress = DEBUG;
                default:
            }
        }
    }

    private void sendDownForAllNotInjectedPointers(MotionEvent prototype, int policyFlags) {
        InjectedPointerTracker injectedPointers = this.mInjectedPointerTracker;
        int pointerIdBits = CLICK_LOCATION_NONE;
        int pointerCount = prototype.getPointerCount();
        for (int i = CLICK_LOCATION_NONE; i < pointerCount; i += STATE_TOUCH_EXPLORING) {
            int pointerId = prototype.getPointerId(i);
            if (!injectedPointers.isInjectedPointerDown(pointerId)) {
                pointerIdBits |= STATE_TOUCH_EXPLORING << pointerId;
                sendMotionEvent(prototype, computeInjectionAction(CLICK_LOCATION_NONE, i), pointerIdBits, policyFlags);
            }
        }
    }

    private void sendHoverExitAndTouchExplorationGestureEndIfNeeded(int policyFlags) {
        MotionEvent event = this.mInjectedPointerTracker.getLastInjectedHoverEvent();
        if (event != null && event.getActionMasked() != 10) {
            int pointerIdBits = event.getPointerIdBits();
            if (!this.mSendTouchExplorationEndDelayed.isPending()) {
                this.mSendTouchExplorationEndDelayed.post();
            }
            sendMotionEvent(event, 10, pointerIdBits, policyFlags);
        }
    }

    private void sendTouchExplorationGestureStartAndHoverEnterIfNeeded(int policyFlags) {
        MotionEvent event = this.mInjectedPointerTracker.getLastInjectedHoverEvent();
        if (event != null && event.getActionMasked() == 10) {
            int pointerIdBits = event.getPointerIdBits();
            sendAccessibilityEvent(DumpState.DUMP_MESSAGES);
            sendMotionEvent(event, 9, pointerIdBits, policyFlags);
        }
    }

    private void sendUpForInjectedDownPointers(MotionEvent prototype, int policyFlags) {
        InjectedPointerTracker injectedTracked = this.mInjectedPointerTracker;
        int pointerIdBits = CLICK_LOCATION_NONE;
        int pointerCount = prototype.getPointerCount();
        for (int i = CLICK_LOCATION_NONE; i < pointerCount; i += STATE_TOUCH_EXPLORING) {
            int pointerId = prototype.getPointerId(i);
            if (injectedTracked.isInjectedPointerDown(pointerId)) {
                pointerIdBits |= STATE_TOUCH_EXPLORING << pointerId;
                sendMotionEvent(prototype, computeInjectionAction(STATE_TOUCH_EXPLORING, i), pointerIdBits, policyFlags);
            }
        }
    }

    private void sendActionDownAndUp(MotionEvent prototype, int policyFlags, boolean targetAccessibilityFocus) {
        int pointerIdBits = STATE_TOUCH_EXPLORING << prototype.getPointerId(prototype.getActionIndex());
        prototype.setTargetAccessibilityFocus(targetAccessibilityFocus);
        sendMotionEvent(prototype, CLICK_LOCATION_NONE, pointerIdBits, policyFlags);
        prototype.setTargetAccessibilityFocus(targetAccessibilityFocus);
        sendMotionEvent(prototype, STATE_TOUCH_EXPLORING, pointerIdBits, policyFlags);
    }

    private void sendMotionEvent(MotionEvent prototype, int action, int pointerIdBits, int policyFlags) {
        prototype.setAction(action);
        MotionEvent event = null;
        if (pointerIdBits == INVALID_POINTER_ID) {
            event = prototype;
        } else {
            try {
                event = prototype.split(pointerIdBits);
            } catch (IllegalArgumentException e) {
                Slog.e(LOG_TAG, "AccessibilityInputFilter error: idBits did not match any ids in the event.");
                event.recycle();
                event = prototype;
            }
        }
        if (action == 0) {
            event.setDownTime(event.getEventTime());
        } else {
            event.setDownTime(this.mInjectedPointerTracker.getLastInjectedDownEventTime());
        }
        if (this.mLongPressingPointerId >= 0) {
            event = offsetEvent(event, -this.mLongPressingPointerDeltaX, -this.mLongPressingPointerDeltaY);
        }
        policyFlags |= 1073741824;
        if (this.mNext != null) {
            this.mNext.onMotionEvent(event, null, policyFlags);
        }
        this.mInjectedPointerTracker.onMotionEvent(event);
        if (event != prototype) {
            event.recycle();
        }
    }

    private MotionEvent offsetEvent(MotionEvent event, int offsetX, int offsetY) {
        if (offsetX == 0 && offsetY == 0) {
            return event;
        }
        int remappedIndex = event.findPointerIndex(this.mLongPressingPointerId);
        int pointerCount = event.getPointerCount();
        PointerProperties[] props = PointerProperties.createArray(pointerCount);
        PointerCoords[] coords = PointerCoords.createArray(pointerCount);
        for (int i = CLICK_LOCATION_NONE; i < pointerCount; i += STATE_TOUCH_EXPLORING) {
            event.getPointerProperties(i, props[i]);
            event.getPointerCoords(i, coords[i]);
            if (i == remappedIndex) {
                PointerCoords pointerCoords = coords[i];
                pointerCoords.x += (float) offsetX;
                pointerCoords = coords[i];
                pointerCoords.y += (float) offsetY;
            }
        }
        return MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getPointerCount(), props, coords, event.getMetaState(), event.getButtonState(), 1.0f, 1.0f, event.getDeviceId(), event.getEdgeFlags(), event.getSource(), event.getFlags());
    }

    private int computeInjectionAction(int actionMasked, int pointerIndex) {
        switch (actionMasked) {
            case CLICK_LOCATION_NONE /*0*/:
            case STATE_GESTURE_DETECTING /*5*/:
                if (this.mInjectedPointerTracker.getInjectedPointerDownCount() == 0) {
                    return CLICK_LOCATION_NONE;
                }
                return (pointerIndex << 8) | STATE_GESTURE_DETECTING;
            case H.REMOVE_STARTING /*6*/:
                return this.mInjectedPointerTracker.getInjectedPointerDownCount() == STATE_TOUCH_EXPLORING ? STATE_TOUCH_EXPLORING : (pointerIndex << 8) | 6;
            default:
                return actionMasked;
        }
    }

    private boolean isDraggingGesture(MotionEvent event) {
        ReceivedPointerTracker receivedTracker = this.mReceivedPointerTracker;
        return GestureUtils.isDraggingGesture(receivedTracker.getReceivedPointerDownX(CLICK_LOCATION_NONE), receivedTracker.getReceivedPointerDownY(CLICK_LOCATION_NONE), receivedTracker.getReceivedPointerDownX(STATE_TOUCH_EXPLORING), receivedTracker.getReceivedPointerDownY(STATE_TOUCH_EXPLORING), event.getX(CLICK_LOCATION_NONE), event.getY(CLICK_LOCATION_NONE), event.getX(STATE_TOUCH_EXPLORING), event.getY(STATE_TOUCH_EXPLORING), MAX_DRAGGING_ANGLE_COS);
    }

    private int computeClickLocation(Point outLocation) {
        MotionEvent lastExploreEvent = this.mInjectedPointerTracker.getLastInjectedHoverEventForClick();
        if (lastExploreEvent != null) {
            int lastExplorePointerIndex = lastExploreEvent.getActionIndex();
            outLocation.x = (int) lastExploreEvent.getX(lastExplorePointerIndex);
            outLocation.y = (int) lastExploreEvent.getY(lastExplorePointerIndex);
            if (!this.mAms.accessibilityFocusOnlyInActiveWindow() || this.mLastTouchedWindowId == this.mAms.getActiveWindowId()) {
                if (this.mAms.getAccessibilityFocusClickPointInScreen(outLocation)) {
                    return STATE_TOUCH_EXPLORING;
                }
                return STATE_DRAGGING;
            }
        }
        if (this.mAms.getAccessibilityFocusClickPointInScreen(outLocation)) {
            return STATE_TOUCH_EXPLORING;
        }
        return CLICK_LOCATION_NONE;
    }

    private static String getStateSymbolicName(int state) {
        switch (state) {
            case STATE_TOUCH_EXPLORING /*1*/:
                return "STATE_TOUCH_EXPLORING";
            case STATE_DRAGGING /*2*/:
                return "STATE_DRAGGING";
            case STATE_DELEGATING /*4*/:
                return "STATE_DELEGATING";
            case STATE_GESTURE_DETECTING /*5*/:
                return "STATE_GESTURE_DETECTING";
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    public String toString() {
        return LOG_TAG;
    }
}
