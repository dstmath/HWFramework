package com.android.server.accessibility;

import android.accessibilityservice.GestureDescription;
import android.accessibilityservice.GestureDescription.GestureStep;
import android.accessibilityservice.GestureDescription.TouchPoint;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.IntArray;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.os.SomeArgs;
import java.util.ArrayList;
import java.util.List;

public class MotionEventInjector implements EventStreamTransformation, Callback {
    private static final int EVENT_BUTTON_STATE = 0;
    private static final int EVENT_DEVICE_ID = 0;
    private static final int EVENT_EDGE_FLAGS = 0;
    private static final int EVENT_FLAGS = 0;
    private static final int EVENT_META_STATE = 0;
    private static final int EVENT_SOURCE = 4098;
    private static final float EVENT_X_PRECISION = 1.0f;
    private static final float EVENT_Y_PRECISION = 1.0f;
    private static final String LOG_TAG = "MotionEventInjector";
    private static final int MESSAGE_INJECT_EVENTS = 2;
    private static final int MESSAGE_SEND_MOTION_EVENT = 1;
    private static PointerCoords[] sPointerCoords;
    private static PointerProperties[] sPointerProps;
    private long mDownTime;
    private final Handler mHandler;
    private boolean mIsDestroyed = false;
    private long mLastScheduledEventTime;
    private TouchPoint[] mLastTouchPoints;
    private EventStreamTransformation mNext;
    private int mNumLastTouchPoints;
    private final SparseArray<Boolean> mOpenGesturesInProgress = new SparseArray();
    private IntArray mSequencesInProgress = new IntArray(5);
    private IAccessibilityServiceClient mServiceInterfaceForCurrentGesture;
    private SparseIntArray mStrokeIdToPointerId = new SparseIntArray(5);

    public MotionEventInjector(Looper looper) {
        this.mHandler = new Handler(looper, this);
    }

    public MotionEventInjector(Handler handler) {
        this.mHandler = handler;
    }

    public void injectEvents(List<GestureStep> gestureSteps, IAccessibilityServiceClient serviceInterface, int sequence) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = gestureSteps;
        args.arg2 = serviceInterface;
        args.argi1 = sequence;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, args));
    }

    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        cancelAnyPendingInjectedEvents();
        sendMotionEventToNext(event, rawEvent, policyFlags);
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
        if (!this.mHandler.hasMessages(1)) {
            this.mOpenGesturesInProgress.put(inputSource, Boolean.valueOf(false));
        }
    }

    public void onDestroy() {
        cancelAnyPendingInjectedEvents();
        this.mIsDestroyed = true;
    }

    public boolean handleMessage(Message message) {
        if (message.what == 2) {
            SomeArgs args = message.obj;
            injectEventsMainThread((List) args.arg1, (IAccessibilityServiceClient) args.arg2, args.argi1);
            args.recycle();
            return true;
        } else if (message.what != 1) {
            Slog.e(LOG_TAG, "Unknown message: " + message.what);
            return false;
        } else {
            MotionEvent motionEvent = message.obj;
            sendMotionEventToNext(motionEvent, motionEvent, 1073741824);
            if (message.arg1 != 0) {
                notifyService(this.mServiceInterfaceForCurrentGesture, this.mSequencesInProgress.get(0), true);
                this.mSequencesInProgress.remove(0);
            }
            return true;
        }
    }

    private void injectEventsMainThread(List<GestureStep> gestureSteps, IAccessibilityServiceClient serviceInterface, int sequence) {
        if (this.mIsDestroyed) {
            try {
                serviceInterface.onPerformGestureResult(sequence, false);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error sending status with mIsDestroyed to " + serviceInterface, re);
            }
        } else if (this.mNext == null) {
            notifyService(serviceInterface, sequence, false);
        } else {
            boolean continuingGesture = newGestureTriesToContinueOldOne(gestureSteps);
            if (!continuingGesture || (serviceInterface == this.mServiceInterfaceForCurrentGesture && (prepareToContinueOldGesture(gestureSteps) ^ 1) == 0)) {
                if (!continuingGesture) {
                    cancelAnyPendingInjectedEvents();
                    cancelAnyGestureInProgress(EVENT_SOURCE);
                }
                this.mServiceInterfaceForCurrentGesture = serviceInterface;
                long currentTime = SystemClock.uptimeMillis();
                List<MotionEvent> events = getMotionEventsFromGestureSteps(gestureSteps, this.mSequencesInProgress.size() == 0 ? currentTime : this.mLastScheduledEventTime);
                if (events.isEmpty()) {
                    notifyService(serviceInterface, sequence, false);
                    return;
                }
                this.mSequencesInProgress.add(sequence);
                int i = 0;
                while (i < events.size()) {
                    MotionEvent event = (MotionEvent) events.get(i);
                    Message message = this.mHandler.obtainMessage(1, i == events.size() + -1 ? 1 : 0, 0, event);
                    this.mLastScheduledEventTime = event.getEventTime();
                    this.mHandler.sendMessageDelayed(message, Math.max(0, event.getEventTime() - currentTime));
                    i++;
                }
                return;
            }
            cancelAnyPendingInjectedEvents();
            notifyService(serviceInterface, sequence, false);
        }
    }

    private boolean newGestureTriesToContinueOldOne(List<GestureStep> gestureSteps) {
        if (gestureSteps.isEmpty()) {
            return false;
        }
        GestureStep firstStep = (GestureStep) gestureSteps.get(0);
        for (int i = 0; i < firstStep.numTouchPoints; i++) {
            if (!firstStep.touchPoints[i].mIsStartOfPath) {
                return true;
            }
        }
        return false;
    }

    private boolean prepareToContinueOldGesture(List<GestureStep> gestureSteps) {
        boolean z = false;
        if (gestureSteps.isEmpty() || this.mLastTouchPoints == null || this.mNumLastTouchPoints == 0) {
            return false;
        }
        int i;
        GestureStep firstStep = (GestureStep) gestureSteps.get(0);
        int numContinuedStrokes = 0;
        for (i = 0; i < firstStep.numTouchPoints; i++) {
            TouchPoint touchPoint = firstStep.touchPoints[i];
            if (!touchPoint.mIsStartOfPath) {
                int continuedPointerId = this.mStrokeIdToPointerId.get(touchPoint.mContinuedStrokeId, -1);
                if (continuedPointerId == -1) {
                    return false;
                }
                this.mStrokeIdToPointerId.put(touchPoint.mStrokeId, continuedPointerId);
                int lastPointIndex = findPointByStrokeId(this.mLastTouchPoints, this.mNumLastTouchPoints, touchPoint.mContinuedStrokeId);
                if (lastPointIndex < 0 || this.mLastTouchPoints[lastPointIndex].mIsEndOfPath || this.mLastTouchPoints[lastPointIndex].mX != touchPoint.mX || this.mLastTouchPoints[lastPointIndex].mY != touchPoint.mY) {
                    return false;
                }
                this.mLastTouchPoints[lastPointIndex].mStrokeId = touchPoint.mStrokeId;
            }
            numContinuedStrokes++;
        }
        for (i = 0; i < this.mNumLastTouchPoints; i++) {
            if (!this.mLastTouchPoints[i].mIsEndOfPath) {
                numContinuedStrokes--;
            }
        }
        if (numContinuedStrokes == 0) {
            z = true;
        }
        return z;
    }

    private void sendMotionEventToNext(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (this.mNext != null) {
            this.mNext.onMotionEvent(event, rawEvent, policyFlags);
            if (event.getActionMasked() == 0) {
                this.mOpenGesturesInProgress.put(event.getSource(), Boolean.valueOf(true));
            }
            if (event.getActionMasked() == 1 || event.getActionMasked() == 3) {
                this.mOpenGesturesInProgress.put(event.getSource(), Boolean.valueOf(false));
            }
        }
    }

    private void cancelAnyGestureInProgress(int source) {
        if (this.mNext != null && ((Boolean) this.mOpenGesturesInProgress.get(source, Boolean.valueOf(false))).booleanValue()) {
            long now = SystemClock.uptimeMillis();
            MotionEvent cancelEvent = obtainMotionEvent(now, now, 3, getLastTouchPoints(), 1);
            sendMotionEventToNext(cancelEvent, cancelEvent, 1073741824);
            this.mOpenGesturesInProgress.put(source, Boolean.valueOf(false));
        }
    }

    private void cancelAnyPendingInjectedEvents() {
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
            cancelAnyGestureInProgress(EVENT_SOURCE);
            for (int i = this.mSequencesInProgress.size() - 1; i >= 0; i--) {
                notifyService(this.mServiceInterfaceForCurrentGesture, this.mSequencesInProgress.get(i), false);
                this.mSequencesInProgress.remove(i);
            }
        } else if (this.mNumLastTouchPoints != 0) {
            cancelAnyGestureInProgress(EVENT_SOURCE);
        }
        this.mNumLastTouchPoints = 0;
        this.mStrokeIdToPointerId.clear();
    }

    private void notifyService(IAccessibilityServiceClient service, int sequence, boolean success) {
        try {
            service.onPerformGestureResult(sequence, success);
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error sending motion event injection status to " + this.mServiceInterfaceForCurrentGesture, re);
        }
    }

    private List<MotionEvent> getMotionEventsFromGestureSteps(List<GestureStep> steps, long startTime) {
        List<MotionEvent> motionEvents = new ArrayList();
        TouchPoint[] lastTouchPoints = getLastTouchPoints();
        for (int i = 0; i < steps.size(); i++) {
            GestureStep step = (GestureStep) steps.get(i);
            int currentTouchPointSize = step.numTouchPoints;
            if (currentTouchPointSize > lastTouchPoints.length) {
                this.mNumLastTouchPoints = 0;
                motionEvents.clear();
                return motionEvents;
            }
            appendMoveEventIfNeeded(motionEvents, step.touchPoints, currentTouchPointSize, step.timeSinceGestureStart + startTime);
            appendUpEvents(motionEvents, step.touchPoints, currentTouchPointSize, step.timeSinceGestureStart + startTime);
            appendDownEvents(motionEvents, step.touchPoints, currentTouchPointSize, step.timeSinceGestureStart + startTime);
        }
        return motionEvents;
    }

    private TouchPoint[] getLastTouchPoints() {
        if (this.mLastTouchPoints == null) {
            int capacity = GestureDescription.getMaxStrokeCount();
            this.mLastTouchPoints = new TouchPoint[capacity];
            for (int i = 0; i < capacity; i++) {
                this.mLastTouchPoints[i] = new TouchPoint();
            }
        }
        return this.mLastTouchPoints;
    }

    private void appendMoveEventIfNeeded(List<MotionEvent> motionEvents, TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
        int moveFound = 0;
        TouchPoint[] lastTouchPoints = getLastTouchPoints();
        for (int i = 0; i < currentTouchPointsSize; i++) {
            int lastPointsIndex = findPointByStrokeId(lastTouchPoints, this.mNumLastTouchPoints, currentTouchPoints[i].mStrokeId);
            if (lastPointsIndex >= 0) {
                int i2 = lastTouchPoints[lastPointsIndex].mX == currentTouchPoints[i].mX ? lastTouchPoints[lastPointsIndex].mY != currentTouchPoints[i].mY ? 1 : 0 : 1;
                moveFound |= i2;
                lastTouchPoints[lastPointsIndex].copyFrom(currentTouchPoints[i]);
            }
        }
        if (moveFound != 0) {
            motionEvents.add(obtainMotionEvent(this.mDownTime, currentTime, 2, lastTouchPoints, this.mNumLastTouchPoints));
        }
    }

    private void appendUpEvents(List<MotionEvent> motionEvents, TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
        TouchPoint[] lastTouchPoints = getLastTouchPoints();
        for (int i = 0; i < currentTouchPointsSize; i++) {
            if (currentTouchPoints[i].mIsEndOfPath) {
                int indexOfUpEvent = findPointByStrokeId(lastTouchPoints, this.mNumLastTouchPoints, currentTouchPoints[i].mStrokeId);
                if (indexOfUpEvent >= 0) {
                    int action;
                    if (this.mNumLastTouchPoints == 1) {
                        action = 1;
                    } else {
                        action = 6;
                    }
                    long j = currentTime;
                    motionEvents.add(obtainMotionEvent(this.mDownTime, j, action | (indexOfUpEvent << 8), lastTouchPoints, this.mNumLastTouchPoints));
                    for (int j2 = indexOfUpEvent; j2 < this.mNumLastTouchPoints - 1; j2++) {
                        lastTouchPoints[j2].copyFrom(this.mLastTouchPoints[j2 + 1]);
                    }
                    this.mNumLastTouchPoints--;
                    if (this.mNumLastTouchPoints == 0) {
                        this.mStrokeIdToPointerId.clear();
                    }
                }
            }
        }
    }

    private void appendDownEvents(List<MotionEvent> motionEvents, TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
        TouchPoint[] lastTouchPoints = getLastTouchPoints();
        for (int i = 0; i < currentTouchPointsSize; i++) {
            if (currentTouchPoints[i].mIsStartOfPath) {
                int action;
                int i2 = this.mNumLastTouchPoints;
                this.mNumLastTouchPoints = i2 + 1;
                lastTouchPoints[i2].copyFrom(currentTouchPoints[i]);
                if (this.mNumLastTouchPoints == 1) {
                    action = 0;
                } else {
                    action = 5;
                }
                if (action == 0) {
                    this.mDownTime = currentTime;
                }
                long j = currentTime;
                motionEvents.add(obtainMotionEvent(this.mDownTime, j, action | (i << 8), lastTouchPoints, this.mNumLastTouchPoints));
            }
        }
    }

    private MotionEvent obtainMotionEvent(long downTime, long eventTime, int action, TouchPoint[] touchPoints, int touchPointsSize) {
        int i;
        if (sPointerCoords == null || sPointerCoords.length < touchPointsSize) {
            sPointerCoords = new PointerCoords[touchPointsSize];
            for (i = 0; i < touchPointsSize; i++) {
                sPointerCoords[i] = new PointerCoords();
            }
        }
        if (sPointerProps == null || sPointerProps.length < touchPointsSize) {
            sPointerProps = new PointerProperties[touchPointsSize];
            for (i = 0; i < touchPointsSize; i++) {
                sPointerProps[i] = new PointerProperties();
            }
        }
        for (i = 0; i < touchPointsSize; i++) {
            int pointerId = this.mStrokeIdToPointerId.get(touchPoints[i].mStrokeId, -1);
            if (pointerId == -1) {
                pointerId = getUnusedPointerId();
                this.mStrokeIdToPointerId.put(touchPoints[i].mStrokeId, pointerId);
            }
            sPointerProps[i].id = pointerId;
            sPointerProps[i].toolType = 0;
            sPointerCoords[i].clear();
            sPointerCoords[i].pressure = 1.0f;
            sPointerCoords[i].size = 1.0f;
            sPointerCoords[i].x = touchPoints[i].mX;
            sPointerCoords[i].y = touchPoints[i].mY;
        }
        return MotionEvent.obtain(downTime, eventTime, action, touchPointsSize, sPointerProps, sPointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, EVENT_SOURCE, 0);
    }

    private static int findPointByStrokeId(TouchPoint[] touchPoints, int touchPointsSize, int strokeId) {
        for (int i = 0; i < touchPointsSize; i++) {
            if (touchPoints[i].mStrokeId == strokeId) {
                return i;
            }
        }
        return -1;
    }

    private int getUnusedPointerId() {
        int pointerId = 0;
        while (this.mStrokeIdToPointerId.indexOfValue(pointerId) >= 0) {
            pointerId++;
            if (pointerId >= 10) {
                return 10;
            }
        }
        return pointerId;
    }
}
