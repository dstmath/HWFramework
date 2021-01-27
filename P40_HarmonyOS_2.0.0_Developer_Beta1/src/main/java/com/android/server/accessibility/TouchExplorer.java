package com.android.server.accessibility;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.server.LocalServices;
import com.android.server.accessibility.AccessibilityGestureDetector;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.usb.descriptors.UsbACInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* access modifiers changed from: package-private */
public class TouchExplorer extends BaseEventStreamTransformation implements AccessibilityGestureDetector.Listener {
    private static final int ALL_POINTER_ID_BITS = -1;
    private static final int CLICK_LOCATION_ACCESSIBILITY_FOCUS = 1;
    private static final int CLICK_LOCATION_LAST_TOUCH_EXPLORED = 2;
    private static final int CLICK_LOCATION_NONE = 0;
    private static final boolean DEBUG = false;
    private static final int EXIT_GESTURE_DETECTION_TIMEOUT = 2000;
    private static final String HW_GLOBAL_ACTIONS_TITLE = "HwGlobalActions";
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
    private PhoneWindowManager mPhoneWindowManager;
    private final ReceivedPointerTracker mReceivedPointerTracker;
    private final int mScaledMinPointerDistanceToUseMiddleLocation;
    private final SendHoverEnterAndMoveDelayed mSendHoverEnterAndMoveDelayed;
    private final SendHoverExitDelayed mSendHoverExitDelayed;
    private final SendAccessibilityEventDelayed mSendTouchExplorationEndDelayed;
    private final SendAccessibilityEventDelayed mSendTouchInteractionEndDelayed;
    private final Point mTempPoint;
    private boolean mTouchExplorationInProgress;

    public TouchExplorer(Context context, AccessibilityManagerService service) {
        this(context, service, null);
    }

    public TouchExplorer(Context context, AccessibilityManagerService service, AccessibilityGestureDetector detector) {
        this.mCurrentState = 1;
        this.mTempPoint = new Point();
        this.mLongPressingPointerId = -1;
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
        this.mSendTouchExplorationEndDelayed = new SendAccessibilityEventDelayed(1024, this.mDetermineUserIntentTimeout);
        this.mSendTouchInteractionEndDelayed = new SendAccessibilityEventDelayed(DumpState.DUMP_COMPILER_STATS, this.mDetermineUserIntentTimeout);
        if (detector == null) {
            this.mGestureDetector = new AccessibilityGestureDetector(context, this);
        } else {
            this.mGestureDetector = detector;
        }
        this.mScaledMinPointerDistanceToUseMiddleLocation = (int) (200.0f * context.getResources().getDisplayMetrics().density);
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void clearEvents(int inputSource) {
        if (inputSource == 4098) {
            clear();
        }
        super.clearEvents(inputSource);
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onDestroy() {
        clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clear() {
        if (this.mReceivedPointerTracker.getLastReceivedEvent() != null) {
            clear(this.mReceivedPointerTracker.getLastReceivedEvent(), DumpState.DUMP_APEX);
        }
    }

    private void clear(MotionEvent event, int policyFlags) {
        int i = this.mCurrentState;
        if (i == 1) {
            sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
        } else if (i == 2) {
            this.mDraggingPointerId = -1;
            sendUpForInjectedDownPointers(event, policyFlags);
        } else if (i == 4) {
            sendUpForInjectedDownPointers(event, policyFlags);
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

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (!event.isFromSource(UsbACInterface.FORMAT_II_AC3)) {
            super.onMotionEvent(event, rawEvent, policyFlags);
            return;
        }
        this.mReceivedPointerTracker.onMotionEvent(rawEvent);
        if (!this.mGestureDetector.onMotionEvent(event, rawEvent, policyFlags)) {
            if (event.getActionMasked() == 3) {
                clear(event, policyFlags);
                return;
            }
            int i = this.mCurrentState;
            if (i == 1) {
                handleMotionEventStateTouchExploring(event, rawEvent, policyFlags);
            } else if (i == 2) {
                handleMotionEventStateDragging(event, policyFlags);
            } else if (i == 4) {
                handleMotionEventStateDelegating(event, policyFlags);
            } else if (i != 5) {
                Slog.e(LOG_TAG, "Illegal state: " + this.mCurrentState);
                clear(event, policyFlags);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003b, code lost:
        if (r0 != 32768) goto L_0x0060;
     */
    @Override // com.android.server.accessibility.EventStreamTransformation
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
        if (eventType != 32) {
            if (eventType == 128 || eventType == 256) {
                this.mLastTouchedWindowId = event.getWindowId();
                super.onAccessibilityEvent(event);
            }
        }
        if (this.mInjectedPointerTracker.mLastInjectedHoverEventForClick != null) {
            this.mInjectedPointerTracker.mLastInjectedHoverEventForClick.recycle();
            this.mInjectedPointerTracker.mLastInjectedHoverEventForClick = null;
        }
        this.mLastTouchedWindowId = -1;
        super.onAccessibilityEvent(event);
    }

    @Override // com.android.server.accessibility.AccessibilityGestureDetector.Listener
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

    @Override // com.android.server.accessibility.AccessibilityGestureDetector.Listener
    public boolean onDoubleTap(MotionEvent event, int policyFlags) {
        boolean targetAccessibilityFocus = false;
        if (this.mCurrentState != 1) {
            return false;
        }
        this.mAms.onTouchInteractionEnd();
        this.mSendHoverEnterAndMoveDelayed.cancel();
        this.mSendHoverExitDelayed.cancel();
        if (this.mSendTouchExplorationEndDelayed.isPending()) {
            this.mSendTouchExplorationEndDelayed.forceSendAndRemove();
        }
        sendAccessibilityEvent(DumpState.DUMP_COMPILER_STATS);
        if (this.mAms.performActionOnAccessibilityFocusedItem(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
            return true;
        }
        Slog.e(LOG_TAG, "ACTION_CLICK failed. Dispatching motion events to simulate click.");
        int pointerIndex = event.getActionIndex();
        event.getPointerId(pointerIndex);
        Point clickLocation = this.mTempPoint;
        int result = computeClickLocation(clickLocation);
        if (result == 0) {
            return true;
        }
        MotionEvent.PointerProperties[] properties = {new MotionEvent.PointerProperties()};
        event.getPointerProperties(pointerIndex, properties[0]);
        MotionEvent.PointerCoords[] coords = {new MotionEvent.PointerCoords()};
        coords[0].x = (float) clickLocation.x;
        coords[0].y = (float) clickLocation.y;
        MotionEvent click_event = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), 0, 1, properties, coords, 0, 0, 1.0f, 1.0f, event.getDeviceId(), 0, event.getSource(), event.getDisplayId(), event.getFlags());
        if (result == 1) {
            targetAccessibilityFocus = true;
        }
        sendActionDownAndUp(click_event, policyFlags, targetAccessibilityFocus);
        click_event.recycle();
        return true;
    }

    @Override // com.android.server.accessibility.AccessibilityGestureDetector.Listener
    public boolean onGestureStarted() {
        this.mCurrentState = 5;
        this.mSendHoverEnterAndMoveDelayed.cancel();
        this.mSendHoverExitDelayed.cancel();
        this.mExitGestureDetectionModeDelayed.post();
        sendAccessibilityEvent(DumpState.DUMP_DOMAIN_PREFERRED);
        return false;
    }

    @Override // com.android.server.accessibility.AccessibilityGestureDetector.Listener
    public boolean onGestureCompleted(int gestureId) {
        if (this.mCurrentState != 5) {
            return false;
        }
        endGestureDetection(true);
        this.mAms.onGesture(gestureId);
        return true;
    }

    @Override // com.android.server.accessibility.AccessibilityGestureDetector.Listener
    public boolean onGestureCancelled(MotionEvent event, int policyFlags) {
        int i = this.mCurrentState;
        boolean z = false;
        if (i == 5) {
            if (event.getActionMasked() == 1) {
                z = true;
            }
            endGestureDetection(z);
            return true;
        } else if (i != 1 || event.getActionMasked() != 2) {
            return false;
        } else {
            this.mSendHoverEnterAndMoveDelayed.addEvent(event);
            this.mSendHoverEnterAndMoveDelayed.forceSendAndRemove();
            this.mSendHoverExitDelayed.cancel();
            sendMotionEvent(event, 7, 1 << this.mReceivedPointerTracker.getPrimaryPointerId(), policyFlags);
            return true;
        }
    }

    private void handleMotionEventStateTouchExploring(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        ReceivedPointerTracker receivedTracker = this.mReceivedPointerTracker;
        int actionMasked = event.getActionMasked();
        if (actionMasked == 0) {
            this.mAms.onTouchInteractionStart();
            this.mSendHoverEnterAndMoveDelayed.cancel();
            this.mSendHoverExitDelayed.cancel();
            if (this.mTouchExplorationInProgress) {
                sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
            }
            if (!this.mGestureDetector.firstTapDetected()) {
                this.mSendTouchExplorationEndDelayed.forceSendAndRemove();
                this.mSendTouchInteractionEndDelayed.forceSendAndRemove();
                sendAccessibilityEvent(DumpState.DUMP_DEXOPT);
            } else {
                this.mSendTouchInteractionEndDelayed.cancel();
            }
            if (!this.mGestureDetector.firstTapDetected() && !this.mTouchExplorationInProgress) {
                if (!this.mSendHoverEnterAndMoveDelayed.isPending()) {
                    this.mSendHoverEnterAndMoveDelayed.post(event, true, 1 << receivedTracker.getPrimaryPointerId(), policyFlags);
                    return;
                }
                this.mSendHoverEnterAndMoveDelayed.addEvent(event);
            }
        } else if (actionMasked == 1) {
            this.mAms.onTouchInteractionEnd();
            int pointerIdBits = 1 << event.getPointerId(event.getActionIndex());
            if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                this.mSendHoverExitDelayed.post(event, pointerIdBits, policyFlags);
            } else {
                sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
            }
            if (!this.mSendTouchInteractionEndDelayed.isPending()) {
                this.mSendTouchInteractionEndDelayed.post();
            }
        } else if (actionMasked == 2) {
            int pointerId = receivedTracker.getPrimaryPointerId();
            int pointerIndex = event.findPointerIndex(pointerId);
            int pointerIdBits2 = 1 << pointerId;
            int pointerCount = event.getPointerCount();
            if (pointerCount != 1) {
                if (pointerCount != 2) {
                    if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                        this.mSendHoverEnterAndMoveDelayed.cancel();
                        this.mSendHoverExitDelayed.cancel();
                    } else {
                        sendHoverExitAndTouchExplorationGestureEndIfNeeded(policyFlags);
                    }
                    this.mCurrentState = 4;
                    sendDownForAllNotInjectedPointers(MotionEvent.obtainNoHistory(event), policyFlags);
                    return;
                }
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
                MotionEvent event2 = MotionEvent.obtainNoHistory(event);
                if (isDraggingGesture(event2)) {
                    this.mCurrentState = 2;
                    this.mDraggingPointerId = pointerId;
                    event2.setEdgeFlags(receivedTracker.getLastReceivedDownEdgeFlags());
                    sendMotionEvent(event2, 0, pointerIdBits2, policyFlags);
                    return;
                }
                this.mCurrentState = 4;
                sendDownForAllNotInjectedPointers(event2, policyFlags);
            } else if (this.mSendHoverEnterAndMoveDelayed.isPending()) {
                this.mSendHoverEnterAndMoveDelayed.addEvent(event);
            } else if (this.mTouchExplorationInProgress) {
                sendTouchExplorationGestureStartAndHoverEnterIfNeeded(policyFlags);
                sendMotionEvent(event, 7, pointerIdBits2, policyFlags);
            }
        } else if (actionMasked == 5) {
            this.mSendHoverEnterAndMoveDelayed.cancel();
            this.mSendHoverExitDelayed.cancel();
        }
    }

    private void handleMotionEventStateDragging(MotionEvent event, int policyFlags) {
        int pointerCount;
        float deltaX;
        float deltaY;
        int pointerIdBits = 0;
        if (event.findPointerIndex(this.mDraggingPointerId) == -1) {
            Slog.e(LOG_TAG, "mDraggingPointerId doesn't match any pointers on current event. mDraggingPointerId: " + Integer.toString(this.mDraggingPointerId) + ", Event: " + event);
            this.mDraggingPointerId = -1;
        } else {
            pointerIdBits = 1 << this.mDraggingPointerId;
        }
        int actionMasked = event.getActionMasked();
        if (actionMasked == 0) {
            Slog.e(LOG_TAG, "Dragging state can be reached only if two pointers are already down");
            clear(event, policyFlags);
        } else if (actionMasked == 1) {
            this.mAms.onTouchInteractionEnd();
            sendAccessibilityEvent(DumpState.DUMP_COMPILER_STATS);
            if (event.getPointerId(event.getActionIndex()) == this.mDraggingPointerId) {
                this.mDraggingPointerId = -1;
                sendMotionEvent(event, 1, pointerIdBits, policyFlags);
            }
            this.mCurrentState = 1;
        } else if (actionMasked != 2) {
            if (actionMasked == 5) {
                this.mCurrentState = 4;
                if (this.mDraggingPointerId != -1) {
                    sendMotionEvent(event, 1, pointerIdBits, policyFlags);
                }
                sendDownForAllNotInjectedPointers(event, policyFlags);
            } else if (actionMasked == 6 && event.getPointerId(event.getActionIndex()) == this.mDraggingPointerId) {
                this.mDraggingPointerId = -1;
                sendMotionEvent(event, 1, pointerIdBits, policyFlags);
            }
        } else if (this.mDraggingPointerId != -1 && (pointerCount = event.getPointerCount()) != 1) {
            if (pointerCount != 2) {
                this.mCurrentState = 4;
                MotionEvent event2 = MotionEvent.obtainNoHistory(event);
                sendMotionEvent(event2, 1, pointerIdBits, policyFlags);
                sendDownForAllNotInjectedPointers(event2, policyFlags);
            } else if (isDraggingGesture(event) || isHwGlobalActionsFocused()) {
                float firstPtrX = event.getX(0);
                float firstPtrY = event.getY(0);
                float secondPtrX = event.getX(1);
                float secondPtrY = event.getY(1);
                int pointerIndex = event.findPointerIndex(this.mDraggingPointerId);
                if (pointerIndex == 0) {
                    deltaX = secondPtrX - firstPtrX;
                } else {
                    deltaX = firstPtrX - secondPtrX;
                }
                if (pointerIndex == 0) {
                    deltaY = secondPtrY - firstPtrY;
                } else {
                    deltaY = firstPtrY - secondPtrY;
                }
                if (Math.hypot((double) deltaX, (double) deltaY) > ((double) this.mScaledMinPointerDistanceToUseMiddleLocation)) {
                    event.offsetLocation(deltaX / 2.0f, deltaY / 2.0f);
                }
                sendMotionEvent(event, 2, pointerIdBits, policyFlags);
            } else {
                this.mCurrentState = 4;
                MotionEvent event3 = MotionEvent.obtainNoHistory(event);
                sendMotionEvent(event3, 1, pointerIdBits, policyFlags);
                sendDownForAllNotInjectedPointers(event3, policyFlags);
            }
        }
    }

    private void handleMotionEventStateDelegating(MotionEvent event, int policyFlags) {
        int actionMasked = event.getActionMasked();
        if (actionMasked == 0) {
            Slog.e(LOG_TAG, "Delegating state can only be reached if there is at least one pointer down!");
            clear(event, policyFlags);
        } else if (actionMasked != 1) {
            sendMotionEvent(event, event.getAction(), -1, policyFlags);
        } else {
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
        }
    }

    private void endGestureDetection(boolean interactionEnd) {
        this.mAms.onTouchInteractionEnd();
        sendAccessibilityEvent(DumpState.DUMP_FROZEN);
        if (interactionEnd) {
            sendAccessibilityEvent(DumpState.DUMP_COMPILER_STATS);
        }
        this.mExitGestureDetectionModeDelayed.cancel();
        this.mCurrentState = 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAccessibilityEvent(int type) {
        AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(this.mContext);
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(type);
            event.setWindowId(this.mAms.getActiveWindowId());
            accessibilityManager.sendAccessibilityEvent(event);
            if (type == 512) {
                this.mTouchExplorationInProgress = true;
            } else if (type == 1024) {
                this.mTouchExplorationInProgress = false;
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
            sendMotionEvent(event, 9, event.getPointerIdBits(), policyFlags);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        super.onMotionEvent(event, null, policyFlags | 1073741824);
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
        MotionEvent.PointerProperties[] props = MotionEvent.PointerProperties.createArray(pointerCount);
        MotionEvent.PointerCoords[] coords = MotionEvent.PointerCoords.createArray(pointerCount);
        for (int i = 0; i < pointerCount; i++) {
            event.getPointerProperties(i, props[i]);
            event.getPointerCoords(i, coords[i]);
            if (i == remappedIndex) {
                coords[i].x += (float) offsetX;
                coords[i].y += (float) offsetY;
            }
        }
        return MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getPointerCount(), props, coords, event.getMetaState(), event.getButtonState(), 1.0f, 1.0f, event.getDeviceId(), event.getEdgeFlags(), event.getSource(), event.getDisplayId(), event.getFlags());
    }

    private int computeInjectionAction(int actionMasked, int pointerIndex) {
        if (actionMasked == 0 || actionMasked == 5) {
            if (this.mInjectedPointerTracker.getInjectedPointerDownCount() == 0) {
                return 0;
            }
            return 5 | (pointerIndex << 8);
        } else if (actionMasked != 6) {
            return actionMasked;
        } else {
            if (this.mInjectedPointerTracker.getInjectedPointerDownCount() == 1) {
                return 1;
            }
            return 6 | (pointerIndex << 8);
        }
    }

    private boolean isDraggingGesture(MotionEvent event) {
        ReceivedPointerTracker receivedTracker = this.mReceivedPointerTracker;
        return GestureUtils.isDraggingGesture(receivedTracker.getReceivedPointerDownX(0), receivedTracker.getReceivedPointerDownY(0), receivedTracker.getReceivedPointerDownX(1), receivedTracker.getReceivedPointerDownY(1), event.getX(0), event.getY(0), event.getX(1), event.getY(1), MAX_DRAGGING_ANGLE_COS);
    }

    private boolean isHwGlobalActionsFocused() {
        PhoneWindowManager pwm = getPhoneWindowManager();
        if (pwm == null) {
            Slog.e(LOG_TAG, "PhoneWindowManager is null");
            return false;
        }
        WindowManagerPolicy.WindowState focusedWindow = pwm.getFocusedWindow();
        if (focusedWindow != null && focusedWindow.getAttrs() != null) {
            return HW_GLOBAL_ACTIONS_TITLE.equals(focusedWindow.getAttrs().getTitle().toString());
        }
        Slog.e(LOG_TAG, "focusedWindow or attrs is null");
        return false;
    }

    private PhoneWindowManager getPhoneWindowManager() {
        if (this.mPhoneWindowManager == null) {
            this.mPhoneWindowManager = (PhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        }
        return this.mPhoneWindowManager;
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
        if (state == 1) {
            return "STATE_TOUCH_EXPLORING";
        }
        if (state == 2) {
            return "STATE_DRAGGING";
        }
        if (state == 4) {
            return "STATE_DELEGATING";
        }
        if (state == 5) {
            return "STATE_GESTURE_DETECTING";
        }
        return "Unknown state: " + state;
    }

    /* access modifiers changed from: private */
    public final class ExitGestureDetectionModeDelayed implements Runnable {
        private ExitGestureDetectionModeDelayed() {
        }

        public void post() {
            TouchExplorer.this.mHandler.postDelayed(this, 2000);
        }

        public void cancel() {
            TouchExplorer.this.mHandler.removeCallbacks(this);
        }

        @Override // java.lang.Runnable
        public void run() {
            TouchExplorer.this.sendAccessibilityEvent(DumpState.DUMP_FROZEN);
            TouchExplorer.this.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public class SendHoverEnterAndMoveDelayed implements Runnable {
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

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isPending() {
            return TouchExplorer.this.mHandler.hasCallbacks(this);
        }

        private void clear() {
            this.mPointerIdBits = -1;
            this.mPolicyFlags = 0;
            for (int i = this.mEvents.size() - 1; i >= 0; i--) {
                this.mEvents.remove(i).recycle();
            }
        }

        public void forceSendAndRemove() {
            if (isPending()) {
                run();
                cancel();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            TouchExplorer.this.sendAccessibilityEvent(512);
            if (!this.mEvents.isEmpty()) {
                TouchExplorer.this.sendMotionEvent(this.mEvents.get(0), 9, this.mPointerIdBits, this.mPolicyFlags);
                int eventCount = this.mEvents.size();
                for (int i = 1; i < eventCount; i++) {
                    TouchExplorer.this.sendMotionEvent(this.mEvents.get(i), 7, this.mPointerIdBits, this.mPolicyFlags);
                }
            }
            clear();
        }
    }

    /* access modifiers changed from: package-private */
    public class SendHoverExitDelayed implements Runnable {
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

        @Override // java.lang.Runnable
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

    /* access modifiers changed from: private */
    public class SendAccessibilityEventDelayed implements Runnable {
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

        @Override // java.lang.Runnable
        public void run() {
            TouchExplorer.this.sendAccessibilityEvent(this.mEventType);
        }
    }

    public String toString() {
        return "TouchExplorer { mCurrentState: " + getStateSymbolicName(this.mCurrentState) + ", mDetermineUserIntentTimeout: " + this.mDetermineUserIntentTimeout + ", mDoubleTapSlop: " + this.mDoubleTapSlop + ", mDraggingPointerId: " + this.mDraggingPointerId + ", mLongPressingPointerId: " + this.mLongPressingPointerId + ", mLongPressingPointerDeltaX: " + this.mLongPressingPointerDeltaX + ", mLongPressingPointerDeltaY: " + this.mLongPressingPointerDeltaY + ", mLastTouchedWindowId: " + this.mLastTouchedWindowId + ", mScaledMinPointerDistanceToUseMiddleLocation: " + this.mScaledMinPointerDistanceToUseMiddleLocation + ", mTempPoint: " + this.mTempPoint + ", mTouchExplorationInProgress: " + this.mTouchExplorationInProgress + " }";
    }

    /* access modifiers changed from: package-private */
    public class InjectedPointerTracker {
        private static final String LOG_TAG_INJECTED_POINTER_TRACKER = "InjectedPointerTracker";
        private int mInjectedPointersDown;
        private long mLastInjectedDownEventTime;
        private MotionEvent mLastInjectedHoverEvent;
        private MotionEvent mLastInjectedHoverEventForClick;

        InjectedPointerTracker() {
        }

        public void onMotionEvent(MotionEvent event) {
            int action = event.getActionMasked();
            if (action != 0) {
                if (action != 1) {
                    if (action != 5) {
                        if (action != 6) {
                            if (action == 7 || action == 9 || action == 10) {
                                MotionEvent motionEvent = this.mLastInjectedHoverEvent;
                                if (motionEvent != null) {
                                    motionEvent.recycle();
                                }
                                this.mLastInjectedHoverEvent = MotionEvent.obtain(event);
                                MotionEvent motionEvent2 = this.mLastInjectedHoverEventForClick;
                                if (motionEvent2 != null) {
                                    motionEvent2.recycle();
                                }
                                this.mLastInjectedHoverEventForClick = MotionEvent.obtain(event);
                                return;
                            }
                            return;
                        }
                    }
                }
                this.mInjectedPointersDown &= ~(1 << event.getPointerId(event.getActionIndex()));
                if (this.mInjectedPointersDown == 0) {
                    this.mLastInjectedDownEventTime = 0;
                    return;
                }
                return;
            }
            this.mInjectedPointersDown |= 1 << event.getPointerId(event.getActionIndex());
            this.mLastInjectedDownEventTime = event.getDownTime();
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

    /* access modifiers changed from: package-private */
    public class ReceivedPointerTracker {
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
            Arrays.fill(this.mReceivedPointerDownTime, 0L);
            this.mReceivedPointersDown = 0;
            this.mPrimaryPointerId = 0;
            this.mLastReceivedUpPointerDownTime = 0;
            this.mLastReceivedUpPointerDownX = 0.0f;
            this.mLastReceivedUpPointerDownY = 0.0f;
        }

        public void onMotionEvent(MotionEvent event) {
            MotionEvent motionEvent = this.mLastReceivedEvent;
            if (motionEvent != null) {
                motionEvent.recycle();
            }
            this.mLastReceivedEvent = MotionEvent.obtain(event);
            int action = event.getActionMasked();
            if (action == 0) {
                handleReceivedPointerDown(event.getActionIndex(), event);
            } else if (action == 1) {
                handleReceivedPointerUp(event.getActionIndex(), event);
            } else if (action == 5) {
                handleReceivedPointerDown(event.getActionIndex(), event);
            } else if (action == 6) {
                handleReceivedPointerUp(event.getActionIndex(), event);
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
            this.mLastReceivedUpPointerDownTime = 0;
            this.mLastReceivedUpPointerDownX = 0.0f;
            this.mLastReceivedUpPointerDownX = 0.0f;
            this.mLastReceivedDownEdgeFlags = event.getEdgeFlags();
            this.mReceivedPointersDown |= 1 << pointerId;
            this.mReceivedPointerDownX[pointerId] = event.getX(pointerIndex);
            this.mReceivedPointerDownY[pointerId] = event.getY(pointerIndex);
            this.mReceivedPointerDownTime[pointerId] = event.getEventTime();
            this.mPrimaryPointerId = pointerId;
        }

        private void handleReceivedPointerUp(int pointerIndex, MotionEvent event) {
            int pointerId = event.getPointerId(pointerIndex);
            this.mLastReceivedUpPointerDownTime = getReceivedPointerDownTime(pointerId);
            float[] fArr = this.mReceivedPointerDownX;
            this.mLastReceivedUpPointerDownX = fArr[pointerId];
            float[] fArr2 = this.mReceivedPointerDownY;
            this.mLastReceivedUpPointerDownY = fArr2[pointerId];
            this.mReceivedPointersDown &= ~(1 << pointerId);
            fArr[pointerId] = 0.0f;
            fArr2[pointerId] = 0.0f;
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
}
