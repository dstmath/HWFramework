package com.android.server.accessibility;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import com.android.server.accessibility.AccessibilityGestureDetector.Listener;
import com.android.server.job.controllers.JobStatus;
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
    private int mCurrentState = 1;
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
    private int mLongPressingPointerId = -1;
    private EventStreamTransformation mNext;
    private final ReceivedPointerTracker mReceivedPointerTracker;
    private final int mScaledMinPointerDistanceToUseMiddleLocation;
    private final SendHoverEnterAndMoveDelayed mSendHoverEnterAndMoveDelayed;
    private final SendHoverExitDelayed mSendHoverExitDelayed;
    private final SendAccessibilityEventDelayed mSendTouchExplorationEndDelayed;
    private final SendAccessibilityEventDelayed mSendTouchInteractionEndDelayed;
    private final Point mTempPoint = new Point();
    private boolean mTouchExplorationInProgress;

    private final class ExitGestureDetectionModeDelayed implements Runnable {
        /* synthetic */ ExitGestureDetectionModeDelayed(TouchExplorer this$0, ExitGestureDetectionModeDelayed -this1) {
            this();
        }

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
            TouchExplorer.this.sendAccessibilityEvent(512);
            TouchExplorer.this.clear();
        }
    }

    class InjectedPointerTracker {
        private static final String LOG_TAG_INJECTED_POINTER_TRACKER = "InjectedPointerTracker";
        private int mInjectedPointersDown;
        private long mLastInjectedDownEventTime;
        private MotionEvent mLastInjectedHoverEvent;
        private MotionEvent mLastInjectedHoverEventForClick;

        InjectedPointerTracker() {
        }

        public void onMotionEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case 0:
                case 5:
                    this.mInjectedPointersDown |= 1 << event.getPointerId(event.getActionIndex());
                    this.mLastInjectedDownEventTime = event.getDownTime();
                    return;
                case 1:
                case 6:
                    this.mInjectedPointersDown &= ~(1 << event.getPointerId(event.getActionIndex()));
                    if (this.mInjectedPointersDown == 0) {
                        this.mLastInjectedDownEventTime = 0;
                        return;
                    }
                    return;
                case 7:
                case 9:
                case 10:
                    if (this.mLastInjectedHoverEvent != null) {
                        this.mLastInjectedHoverEvent.recycle();
                    }
                    this.mLastInjectedHoverEvent = MotionEvent.obtain(event);
                    if (this.mLastInjectedHoverEventForClick != null) {
                        this.mLastInjectedHoverEventForClick.recycle();
                    }
                    this.mLastInjectedHoverEventForClick = MotionEvent.obtain(event);
                    return;
                default:
                    return;
            }
        }

        public void clear() {
            this.mInjectedPointersDown = 0;
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
            if ((this.mInjectedPointersDown & (1 << pointerId)) != 0) {
                return true;
            }
            return false;
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
            for (int i = 0; i < 32; i++) {
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
        private final long[] mReceivedPointerDownTime = new long[32];
        private final float[] mReceivedPointerDownX = new float[32];
        private final float[] mReceivedPointerDownY = new float[32];
        private int mReceivedPointersDown;

        ReceivedPointerTracker() {
        }

        public void clear() {
            Arrays.fill(this.mReceivedPointerDownX, 0.0f);
            Arrays.fill(this.mReceivedPointerDownY, 0.0f);
            Arrays.fill(this.mReceivedPointerDownTime, 0);
            this.mReceivedPointersDown = 0;
            this.mPrimaryPointerId = 0;
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
                case 0:
                    handleReceivedPointerDown(event.getActionIndex(), event);
                    return;
                case 1:
                    handleReceivedPointerUp(event.getActionIndex(), event);
                    return;
                case 5:
                    handleReceivedPointerDown(event.getActionIndex(), event);
                    return;
                case 6:
                    handleReceivedPointerUp(event.getActionIndex(), event);
                    return;
                default:
                    return;
            }
        }

        public MotionEvent getLastReceivedEvent() {
            return this.mLastReceivedEvent;
        }

        public int getReceivedPointerDownCount() {
            return Integer.bitCount(this.mReceivedPointersDown);
        }

        public boolean isReceivedPointerDown(int pointerId) {
            if ((this.mReceivedPointersDown & (1 << pointerId)) != 0) {
                return true;
            }
            return false;
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
            if (this.mPrimaryPointerId == -1) {
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
            int pointerFlag = 1 << pointerId;
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

        private void handleReceivedPointerUp(int pointerIndex, MotionEvent event) {
            int pointerId = event.getPointerId(pointerIndex);
            int pointerFlag = 1 << pointerId;
            this.mLastReceivedUpPointerDownTime = getReceivedPointerDownTime(pointerId);
            this.mLastReceivedUpPointerDownX = this.mReceivedPointerDownX[pointerId];
            this.mLastReceivedUpPointerDownY = this.mReceivedPointerDownY[pointerId];
            this.mReceivedPointersDown &= ~pointerFlag;
            this.mReceivedPointerDownX[pointerId] = 0.0f;
            this.mReceivedPointerDownY[pointerId] = 0.0f;
            this.mReceivedPointerDownTime[pointerId] = 0;
            if (this.mPrimaryPointerId == pointerId) {
                this.mPrimaryPointerId = -1;
            }
        }

        private int findPrimaryPointerId() {
            int primaryPointerId = -1;
            long minDownTime = JobStatus.NO_LATEST_RUNTIME;
            int pointerIdBits = this.mReceivedPointersDown;
            while (pointerIdBits > 0) {
                int pointerId = Integer.numberOfTrailingZeros(pointerIdBits);
                pointerIdBits &= ~(1 << pointerId);
                long downPointerTime = this.mReceivedPointerDownTime[pointerId];
                if (downPointerTime < minDownTime) {
                    minDownTime = downPointerTime;
                    primaryPointerId = pointerId;
                }
            }
            return primaryPointerId;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("=========================");
            builder.append("\nDown pointers #");
            builder.append(getReceivedPointerDownCount());
            builder.append(" [ ");
            for (int i = 0; i < 32; i++) {
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
        private final String LOG_TAG_SEND_HOVER_DELAYED = "SendHoverEnterAndMoveDelayed";
        private final List<MotionEvent> mEvents = new ArrayList();
        private int mPointerIdBits;
        private int mPolicyFlags;

        SendHoverEnterAndMoveDelayed() {
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
            this.mPointerIdBits = -1;
            this.mPolicyFlags = 0;
            for (int i = this.mEvents.size() - 1; i >= 0; i--) {
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
            TouchExplorer.this.sendAccessibilityEvent(512);
            if (!this.mEvents.isEmpty()) {
                TouchExplorer.this.sendMotionEvent((MotionEvent) this.mEvents.get(0), 9, this.mPointerIdBits, this.mPolicyFlags);
                int eventCount = this.mEvents.size();
                for (int i = 1; i < eventCount; i++) {
                    TouchExplorer.this.sendMotionEvent((MotionEvent) this.mEvents.get(i), 7, this.mPointerIdBits, this.mPolicyFlags);
                }
            }
            clear();
        }
    }

    class SendHoverExitDelayed implements Runnable {
        private final String LOG_TAG_SEND_HOVER_DELAYED = "SendHoverExitDelayed";
        private int mPointerIdBits;
        private int mPolicyFlags;
        private MotionEvent mPrototype;

        SendHoverExitDelayed() {
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
            this.mPointerIdBits = -1;
            this.mPolicyFlags = 0;
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
        this.mContext = context;
        this.mAms = service;
        this.mReceivedPointerTracker = new ReceivedPointerTracker();
        this.mInjectedPointerTracker = new InjectedPointerTracker();
        this.mDetermineUserIntentTimeout = ViewConfiguration.getDoubleTapTimeout();
        this.mDoubleTapSlop = ViewConfiguration.get(context).getScaledDoubleTapSlop();
        this.mHandler = new Handler(context.getMainLooper());
        this.mExitGestureDetectionModeDelayed = new ExitGestureDetectionModeDelayed(this, null);
        this.mSendHoverEnterAndMoveDelayed = new SendHoverEnterAndMoveDelayed();
        this.mSendHoverExitDelayed = new SendHoverExitDelayed();
        this.mSendTouchExplorationEndDelayed = new SendAccessibilityEventDelayed(1024, this.mDetermineUserIntentTimeout);
        this.mSendTouchInteractionEndDelayed = new SendAccessibilityEventDelayed(DumpState.DUMP_COMPILER_STATS, this.mDetermineUserIntentTimeout);
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
            case 1:
                sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
                break;
            case 2:
                this.mDraggingPointerId = -1;
                sendUpForInjectedDownPointers(event, policyFlags);
                break;
            case 4:
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
        this.mLongPressingPointerId = -1;
        this.mLongPressingPointerDeltaX = 0;
        this.mLongPressingPointerDeltaY = 0;
        this.mCurrentState = 1;
        this.mTouchExplorationInProgress = false;
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
                    case 1:
                        handleMotionEventStateTouchExploring(event, rawEvent, policyFlags);
                        break;
                    case 2:
                        handleMotionEventStateDragging(event, policyFlags);
                        break;
                    case 4:
                        handleMotionEventStateDelegating(event, policyFlags);
                        break;
                    case 5:
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
        if (this.mSendTouchExplorationEndDelayed.isPending() && eventType == 256) {
            this.mSendTouchExplorationEndDelayed.cancel();
            sendAccessibilityEvent(1024);
        }
        if (this.mSendTouchInteractionEndDelayed.isPending() && eventType == 256) {
            this.mSendTouchInteractionEndDelayed.cancel();
            sendAccessibilityEvent(DumpState.DUMP_COMPILER_STATS);
        }
        switch (eventType) {
            case 32:
            case 32768:
                if (this.mInjectedPointerTracker.mLastInjectedHoverEventForClick != null) {
                    this.mInjectedPointerTracker.mLastInjectedHoverEventForClick.recycle();
                    this.mInjectedPointerTracker.mLastInjectedHoverEventForClick = null;
                }
                this.mLastTouchedWindowId = -1;
                break;
            case 128:
            case 256:
                this.mLastTouchedWindowId = event.getWindowId();
                break;
        }
        if (this.mNext != null) {
            this.mNext.onAccessibilityEvent(event);
        }
    }

    public void onDoubleTapAndHold(MotionEvent event, int policyFlags) {
        if (this.mCurrentState == 1 && this.mReceivedPointerTracker.getLastReceivedEvent().getPointerCount() != 0) {
            int pointerIndex = event.getActionIndex();
            int pointerId = event.getPointerId(pointerIndex);
            Point clickLocation = this.mTempPoint;
            if (computeClickLocation(clickLocation) != 0) {
                this.mLongPressingPointerId = pointerId;
                this.mLongPressingPointerDeltaX = ((int) event.getX(pointerIndex)) - clickLocation.x;
                this.mLongPressingPointerDeltaY = ((int) event.getY(pointerIndex)) - clickLocation.y;
                sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
                this.mCurrentState = 4;
                sendDownForAllNotInjectedPointers(event, policyFlags);
            }
        }
    }

    public boolean onDoubleTap(MotionEvent event, int policyFlags) {
        if (this.mCurrentState != 1) {
            return false;
        }
        this.mSendHoverEnterAndMoveDelayed.cancel();
        this.mSendHoverExitDelayed.cancel();
        if (this.mSendTouchExplorationEndDelayed.isPending()) {
            this.mSendTouchExplorationEndDelayed.forceSendAndRemove();
        }
        if (this.mSendTouchInteractionEndDelayed.isPending()) {
            this.mSendTouchInteractionEndDelayed.forceSendAndRemove();
        }
        if (this.mAms.performActionOnAccessibilityFocusedItem(AccessibilityAction.ACTION_CLICK)) {
            return true;
        }
        Slog.e(LOG_TAG, "ACTION_CLICK failed. Dispatching motion events to simulate click.");
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        Point clickLocation = this.mTempPoint;
        int result = computeClickLocation(clickLocation);
        if (result == 0) {
            return true;
        }
        PointerProperties[] properties = new PointerProperties[]{new PointerProperties()};
        event.getPointerProperties(pointerIndex, properties[0]);
        PointerCoords[] coords = new PointerCoords[]{new PointerCoords()};
        coords[0].x = (float) clickLocation.x;
        coords[0].y = (float) clickLocation.y;
        MotionEvent click_event = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), 0, 1, properties, coords, 0, 0, 1.0f, 1.0f, event.getDeviceId(), 0, event.getSource(), event.getFlags());
        sendActionDownAndUp(click_event, policyFlags, result == 1);
        click_event.recycle();
        return true;
    }

    public boolean onGestureStarted() {
        this.mCurrentState = 5;
        this.mSendHoverEnterAndMoveDelayed.cancel();
        this.mSendHoverExitDelayed.cancel();
        this.mExitGestureDetectionModeDelayed.post();
        sendAccessibilityEvent(DumpState.DUMP_DOMAIN_PREFERRED);
        return false;
    }

    public boolean onGestureCompleted(int gestureId) {
        if (this.mCurrentState != 5) {
            return false;
        }
        endGestureDetection();
        this.mAms.onGesture(gestureId);
        return true;
    }

    public boolean onGestureCancelled(MotionEvent event, int policyFlags) {
        if (this.mCurrentState == 5) {
            endGestureDetection();
            return true;
        } else if (this.mCurrentState != 1 || event.getActionMasked() != 2) {
            return false;
        } else {
            int pointerIdBits = 1 << this.mReceivedPointerTracker.getPrimaryPointerId();
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
            case 0:
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
                if (!this.mGestureDetector.firstTapDetected() && (this.mTouchExplorationInProgress ^ 1) != 0) {
                    if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                        this.mSendHoverEnterAndMoveDelayed.addEvent(event);
                        return;
                    }
                    this.mSendHoverEnterAndMoveDelayed.post(event, true, 1 << receivedTracker.getPrimaryPointerId(), policyFlags);
                    return;
                }
                return;
            case 1:
                if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad()) {
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            TouchExplorer.this.mAms.onTouchInteractionEnd();
                        }
                    }, 500);
                } else {
                    this.mAms.onTouchInteractionEnd();
                }
                pointerIdBits = 1 << event.getPointerId(event.getActionIndex());
                if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                    this.mSendHoverExitDelayed.post(event, pointerIdBits, policyFlags);
                } else {
                    sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
                }
                if (!this.mSendTouchInteractionEndDelayed.isPending()) {
                    this.mSendTouchInteractionEndDelayed.post();
                    return;
                }
                return;
            case 2:
                int pointerId = receivedTracker.getPrimaryPointerId();
                int pointerIndex = event.findPointerIndex(pointerId);
                pointerIdBits = 1 << pointerId;
                switch (event.getPointerCount()) {
                    case 1:
                        if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                            this.mSendHoverEnterAndMoveDelayed.addEvent(event);
                            return;
                        } else if (this.mTouchExplorationInProgress) {
                            sendTouchExplorationGestureStartAndHoverEnterIfNeeded(policyFlags);
                            sendMotionEvent(event, 7, pointerIdBits, policyFlags);
                            return;
                        } else {
                            return;
                        }
                    case 2:
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
                            this.mCurrentState = 2;
                            this.mDraggingPointerId = pointerId;
                            event.setEdgeFlags(receivedTracker.getLastReceivedDownEdgeFlags());
                            sendMotionEvent(event, 0, pointerIdBits, policyFlags);
                            return;
                        }
                        this.mCurrentState = 4;
                        sendDownForAllNotInjectedPointers(event, policyFlags);
                        return;
                    default:
                        if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                            this.mSendHoverEnterAndMoveDelayed.cancel();
                            this.mSendHoverExitDelayed.cancel();
                        } else {
                            sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
                        }
                        this.mCurrentState = 4;
                        sendDownForAllNotInjectedPointers(event, policyFlags);
                        return;
                }
            case 5:
                this.mSendHoverEnterAndMoveDelayed.cancel();
                this.mSendHoverExitDelayed.cancel();
                return;
            default:
                return;
        }
    }

    private void handleMotionEventStateDragging(MotionEvent event, int policyFlags) {
        int pointerIdBits = 0;
        if (event.findPointerIndex(this.mDraggingPointerId) == -1) {
            Slog.e(LOG_TAG, "mDraggingPointerId doesn't match any pointers on current event. mDraggingPointerId: " + Integer.toString(this.mDraggingPointerId) + ", Event: " + event);
            this.mDraggingPointerId = -1;
        } else {
            pointerIdBits = 1 << this.mDraggingPointerId;
        }
        switch (event.getActionMasked()) {
            case 0:
                throw new IllegalStateException("Dragging state can be reached only if two pointers are already down");
            case 1:
                this.mAms.onTouchInteractionEnd();
                sendAccessibilityEvent(DumpState.DUMP_COMPILER_STATS);
                if (event.getPointerId(event.getActionIndex()) == this.mDraggingPointerId) {
                    this.mDraggingPointerId = -1;
                    sendMotionEvent(event, 1, pointerIdBits, policyFlags);
                }
                this.mCurrentState = 1;
                return;
            case 2:
                if (this.mDraggingPointerId != -1) {
                    switch (event.getPointerCount()) {
                        case 1:
                            return;
                        case 2:
                            if (isDraggingGesture(event)) {
                                float firstPtrX = event.getX(0);
                                float firstPtrY = event.getY(0);
                                float deltaX = firstPtrX - event.getX(1);
                                float deltaY = firstPtrY - event.getY(1);
                                if (Math.hypot((double) deltaX, (double) deltaY) > ((double) this.mScaledMinPointerDistanceToUseMiddleLocation)) {
                                    event.setLocation(deltaX / 2.0f, deltaY / 2.0f);
                                }
                                sendMotionEvent(event, 2, pointerIdBits, policyFlags);
                                return;
                            }
                            this.mCurrentState = 4;
                            sendMotionEvent(event, 1, pointerIdBits, policyFlags);
                            sendDownForAllNotInjectedPointers(event, policyFlags);
                            return;
                        default:
                            this.mCurrentState = 4;
                            sendMotionEvent(event, 1, pointerIdBits, policyFlags);
                            sendDownForAllNotInjectedPointers(event, policyFlags);
                            return;
                    }
                }
                return;
            case 5:
                this.mCurrentState = 4;
                if (this.mDraggingPointerId != -1) {
                    sendMotionEvent(event, 1, pointerIdBits, policyFlags);
                }
                sendDownForAllNotInjectedPointers(event, policyFlags);
                return;
            case 6:
                if (event.getPointerId(event.getActionIndex()) == this.mDraggingPointerId) {
                    this.mDraggingPointerId = -1;
                    sendMotionEvent(event, 1, pointerIdBits, policyFlags);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void handleMotionEventStateDelegating(MotionEvent event, int policyFlags) {
        switch (event.getActionMasked()) {
            case 0:
                throw new IllegalStateException("Delegating state can only be reached if there is at least one pointer down!");
            case 1:
                if (this.mLongPressingPointerId >= 0) {
                    event = offsetEvent(event, -this.mLongPressingPointerDeltaX, -this.mLongPressingPointerDeltaY);
                    this.mLongPressingPointerId = -1;
                    this.mLongPressingPointerDeltaX = 0;
                    this.mLongPressingPointerDeltaY = 0;
                }
                sendMotionEvent(event, event.getAction(), -1, policyFlags);
                this.mAms.onTouchInteractionEnd();
                sendAccessibilityEvent(DumpState.DUMP_COMPILER_STATS);
                this.mCurrentState = 1;
                return;
            default:
                sendMotionEvent(event, event.getAction(), -1, policyFlags);
                return;
        }
    }

    private void endGestureDetection() {
        this.mAms.onTouchInteractionEnd();
        sendAccessibilityEvent(DumpState.DUMP_FROZEN);
        sendAccessibilityEvent(DumpState.DUMP_COMPILER_STATS);
        this.mExitGestureDetectionModeDelayed.cancel();
        this.mCurrentState = 1;
    }

    private void sendAccessibilityEvent(int type) {
        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(this.mContext);
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(type);
            event.setWindowId(this.mAms.getActiveWindowId());
            accessibilityManager.sendAccessibilityEvent(event);
            switch (type) {
                case 512:
                    this.mTouchExplorationInProgress = true;
                    return;
                case 1024:
                    this.mTouchExplorationInProgress = false;
                    return;
                default:
                    return;
            }
        }
    }

    private void sendDownForAllNotInjectedPointers(MotionEvent prototype, int policyFlags) {
        InjectedPointerTracker injectedPointers = this.mInjectedPointerTracker;
        int pointerIdBits = 0;
        int pointerCount = prototype.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            int pointerId = prototype.getPointerId(i);
            if (!injectedPointers.isInjectedPointerDown(pointerId)) {
                pointerIdBits |= 1 << pointerId;
                sendMotionEvent(prototype, computeInjectionAction(0, i), pointerIdBits, policyFlags);
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
            sendAccessibilityEvent(512);
            sendMotionEvent(event, 9, pointerIdBits, policyFlags);
        }
    }

    private void sendUpForInjectedDownPointers(MotionEvent prototype, int policyFlags) {
        InjectedPointerTracker injectedTracked = this.mInjectedPointerTracker;
        int pointerIdBits = 0;
        int pointerCount = prototype.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            int pointerId = prototype.getPointerId(i);
            if (injectedTracked.isInjectedPointerDown(pointerId)) {
                pointerIdBits |= 1 << pointerId;
                sendMotionEvent(prototype, computeInjectionAction(1, i), pointerIdBits, policyFlags);
            }
        }
    }

    private void sendActionDownAndUp(MotionEvent prototype, int policyFlags, boolean targetAccessibilityFocus) {
        int pointerIdBits = 1 << prototype.getPointerId(prototype.getActionIndex());
        prototype.setTargetAccessibilityFocus(targetAccessibilityFocus);
        sendMotionEvent(prototype, 0, pointerIdBits, policyFlags);
        prototype.setTargetAccessibilityFocus(targetAccessibilityFocus);
        sendMotionEvent(prototype, 1, pointerIdBits, policyFlags);
    }

    private void sendMotionEvent(MotionEvent prototype, int action, int pointerIdBits, int policyFlags) {
        MotionEvent event;
        prototype.setAction(action);
        if (pointerIdBits == -1) {
            event = prototype;
        } else {
            try {
                event = prototype.split(pointerIdBits);
            } catch (IllegalArgumentException e) {
                Slog.e(LOG_TAG, "sendMotionEvent: Failed to split motion event: " + e);
                return;
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
        for (int i = 0; i < pointerCount; i++) {
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
            case 0:
            case 5:
                if (this.mInjectedPointerTracker.getInjectedPointerDownCount() == 0) {
                    return 0;
                }
                return (pointerIndex << 8) | 5;
            case 6:
                return this.mInjectedPointerTracker.getInjectedPointerDownCount() == 1 ? 1 : (pointerIndex << 8) | 6;
            default:
                return actionMasked;
        }
    }

    private boolean isDraggingGesture(MotionEvent event) {
        ReceivedPointerTracker receivedTracker = this.mReceivedPointerTracker;
        return GestureUtils.isDraggingGesture(receivedTracker.getReceivedPointerDownX(0), receivedTracker.getReceivedPointerDownY(0), receivedTracker.getReceivedPointerDownX(1), receivedTracker.getReceivedPointerDownY(1), event.getX(0), event.getY(0), event.getX(1), event.getY(1), MAX_DRAGGING_ANGLE_COS);
    }

    private int computeClickLocation(Point outLocation) {
        MotionEvent lastExploreEvent = this.mInjectedPointerTracker.getLastInjectedHoverEventForClick();
        if (lastExploreEvent != null) {
            int lastExplorePointerIndex = lastExploreEvent.getActionIndex();
            outLocation.x = (int) lastExploreEvent.getX(lastExplorePointerIndex);
            outLocation.y = (int) lastExploreEvent.getY(lastExplorePointerIndex);
            if (!this.mAms.accessibilityFocusOnlyInActiveWindow() || this.mLastTouchedWindowId == this.mAms.getActiveWindowId()) {
                if (this.mAms.getAccessibilityFocusClickPointInScreen(outLocation)) {
                    return 1;
                }
                return 2;
            }
        }
        if (this.mAms.getAccessibilityFocusClickPointInScreen(outLocation)) {
            return 1;
        }
        return 0;
    }

    private static String getStateSymbolicName(int state) {
        switch (state) {
            case 1:
                return "STATE_TOUCH_EXPLORING";
            case 2:
                return "STATE_DRAGGING";
            case 4:
                return "STATE_DELEGATING";
            case 5:
                return "STATE_GESTURE_DETECTING";
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    public String toString() {
        return LOG_TAG;
    }
}
