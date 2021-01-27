package com.android.server.accessibility;

import android.accessibilityservice.GestureDescription;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.IntArray;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import com.android.internal.os.SomeArgs;
import com.android.server.usb.descriptors.UsbACInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MotionEventInjector extends BaseEventStreamTransformation implements Handler.Callback {
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
    private static MotionEvent.PointerCoords[] sPointerCoords;
    private static MotionEvent.PointerProperties[] sPointerProps;
    private long mDownTime;
    private final Handler mHandler;
    private boolean mIsDestroyed = false;
    private long mLastScheduledEventTime;
    private GestureDescription.TouchPoint[] mLastTouchPoints;
    private int mNumLastTouchPoints;
    private final SparseArray<Boolean> mOpenGesturesInProgress = new SparseArray<>();
    private IntArray mSequencesInProgress = new IntArray(5);
    private IAccessibilityServiceClient mServiceInterfaceForCurrentGesture;
    private SparseIntArray mStrokeIdToPointerId = new SparseIntArray(5);

    @Override // com.android.server.accessibility.BaseEventStreamTransformation, com.android.server.accessibility.EventStreamTransformation
    public /* bridge */ /* synthetic */ EventStreamTransformation getNext() {
        return super.getNext();
    }

    @Override // com.android.server.accessibility.BaseEventStreamTransformation, com.android.server.accessibility.EventStreamTransformation
    public /* bridge */ /* synthetic */ void setNext(EventStreamTransformation eventStreamTransformation) {
        super.setNext(eventStreamTransformation);
    }

    public MotionEventInjector(Looper looper) {
        this.mHandler = new Handler(looper, this);
    }

    public MotionEventInjector(Handler handler) {
        this.mHandler = handler;
    }

    public void injectEvents(List<GestureDescription.GestureStep> gestureSteps, IAccessibilityServiceClient serviceInterface, int sequence) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = gestureSteps;
        args.arg2 = serviceInterface;
        args.argi1 = sequence;
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(2, args));
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (!event.isFromSource(UsbACInterface.FORMAT_III_IEC1937_MPEG1_Layer1) || event.getActionMasked() != 7 || !this.mOpenGesturesInProgress.get(4098, false).booleanValue()) {
            cancelAnyPendingInjectedEvents();
            sendMotionEventToNext(event, rawEvent, policyFlags);
        }
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void clearEvents(int inputSource) {
        if (!this.mHandler.hasMessages(1)) {
            this.mOpenGesturesInProgress.put(inputSource, false);
        }
    }

    @Override // com.android.server.accessibility.EventStreamTransformation
    public void onDestroy() {
        cancelAnyPendingInjectedEvents();
        this.mIsDestroyed = true;
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message message) {
        if (message.what == 2) {
            SomeArgs args = (SomeArgs) message.obj;
            injectEventsMainThread((List) args.arg1, (IAccessibilityServiceClient) args.arg2, args.argi1);
            args.recycle();
            return true;
        } else if (message.what != 1) {
            Slog.e(LOG_TAG, "Unknown message: " + message.what);
            return false;
        } else {
            MotionEvent motionEvent = (MotionEvent) message.obj;
            sendMotionEventToNext(motionEvent, motionEvent, 1073741824);
            if (message.arg1 != 0) {
                notifyService(this.mServiceInterfaceForCurrentGesture, this.mSequencesInProgress.get(0), true);
                this.mSequencesInProgress.remove(0);
            }
            return true;
        }
    }

    private void injectEventsMainThread(List<GestureDescription.GestureStep> gestureSteps, IAccessibilityServiceClient serviceInterface, int sequence) {
        MotionEventInjector motionEventInjector = this;
        if (motionEventInjector.mIsDestroyed) {
            try {
                serviceInterface.onPerformGestureResult(sequence, false);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error sending status with mIsDestroyed to " + serviceInterface, re);
            }
        } else if (getNext() == null) {
            motionEventInjector.notifyService(serviceInterface, sequence, false);
        } else {
            boolean continuingGesture = newGestureTriesToContinueOldOne(gestureSteps);
            if (!continuingGesture || (serviceInterface == motionEventInjector.mServiceInterfaceForCurrentGesture && prepareToContinueOldGesture(gestureSteps))) {
                if (!continuingGesture) {
                    cancelAnyPendingInjectedEvents();
                    motionEventInjector.cancelAnyGestureInProgress(4098);
                }
                motionEventInjector.mServiceInterfaceForCurrentGesture = serviceInterface;
                long currentTime = SystemClock.uptimeMillis();
                List<MotionEvent> events = motionEventInjector.getMotionEventsFromGestureSteps(gestureSteps, motionEventInjector.mSequencesInProgress.size() == 0 ? currentTime : motionEventInjector.mLastScheduledEventTime);
                if (events.isEmpty()) {
                    motionEventInjector.notifyService(serviceInterface, sequence, false);
                    return;
                }
                motionEventInjector.mSequencesInProgress.add(sequence);
                int i = 0;
                while (i < events.size()) {
                    MotionEvent event = events.get(i);
                    Message message = motionEventInjector.mHandler.obtainMessage(1, i == events.size() - 1 ? 1 : 0, 0, event);
                    motionEventInjector.mLastScheduledEventTime = event.getEventTime();
                    motionEventInjector.mHandler.sendMessageDelayed(message, Math.max(0L, event.getEventTime() - currentTime));
                    i++;
                    motionEventInjector = this;
                    continuingGesture = continuingGesture;
                }
                return;
            }
            cancelAnyPendingInjectedEvents();
            motionEventInjector.notifyService(serviceInterface, sequence, false);
        }
    }

    private boolean newGestureTriesToContinueOldOne(List<GestureDescription.GestureStep> gestureSteps) {
        if (gestureSteps.isEmpty()) {
            return false;
        }
        GestureDescription.GestureStep firstStep = gestureSteps.get(0);
        for (int i = 0; i < firstStep.numTouchPoints; i++) {
            if (!firstStep.touchPoints[i].mIsStartOfPath) {
                return true;
            }
        }
        return false;
    }

    private boolean prepareToContinueOldGesture(List<GestureDescription.GestureStep> gestureSteps) {
        if (gestureSteps.isEmpty() || this.mLastTouchPoints == null || this.mNumLastTouchPoints == 0) {
            return false;
        }
        GestureDescription.GestureStep firstStep = gestureSteps.get(0);
        int numContinuedStrokes = 0;
        for (int i = 0; i < firstStep.numTouchPoints; i++) {
            GestureDescription.TouchPoint touchPoint = firstStep.touchPoints[i];
            if (!touchPoint.mIsStartOfPath) {
                int continuedPointerId = this.mStrokeIdToPointerId.get(touchPoint.mContinuedStrokeId, -1);
                if (continuedPointerId == -1) {
                    Slog.w(LOG_TAG, "Can't continue gesture due to unknown continued stroke id in " + touchPoint);
                    return false;
                }
                this.mStrokeIdToPointerId.put(touchPoint.mStrokeId, continuedPointerId);
                int lastPointIndex = findPointByStrokeId(this.mLastTouchPoints, this.mNumLastTouchPoints, touchPoint.mContinuedStrokeId);
                if (lastPointIndex < 0) {
                    Slog.w(LOG_TAG, "Can't continue gesture due continued gesture id of " + touchPoint + " not matching any previous strokes in " + Arrays.asList(this.mLastTouchPoints));
                    return false;
                } else if (!this.mLastTouchPoints[lastPointIndex].mIsEndOfPath && this.mLastTouchPoints[lastPointIndex].mX == touchPoint.mX && this.mLastTouchPoints[lastPointIndex].mY == touchPoint.mY) {
                    this.mLastTouchPoints[lastPointIndex].mStrokeId = touchPoint.mStrokeId;
                } else {
                    Slog.w(LOG_TAG, "Can't continue gesture due to points mismatch between " + this.mLastTouchPoints[lastPointIndex] + " and " + touchPoint);
                    return false;
                }
            }
            numContinuedStrokes++;
        }
        for (int i2 = 0; i2 < this.mNumLastTouchPoints; i2++) {
            if (!this.mLastTouchPoints[i2].mIsEndOfPath) {
                numContinuedStrokes--;
            }
        }
        if (numContinuedStrokes == 0) {
            return true;
        }
        return false;
    }

    private void sendMotionEventToNext(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (getNext() != null) {
            super.onMotionEvent(event, rawEvent, policyFlags);
            if (event.getActionMasked() == 0) {
                this.mOpenGesturesInProgress.put(event.getSource(), true);
            }
            if (event.getActionMasked() == 1 || event.getActionMasked() == 3) {
                this.mOpenGesturesInProgress.put(event.getSource(), false);
            }
        }
    }

    private void cancelAnyGestureInProgress(int source) {
        if (getNext() != null && this.mOpenGesturesInProgress.get(source, false).booleanValue()) {
            long now = SystemClock.uptimeMillis();
            MotionEvent cancelEvent = obtainMotionEvent(now, now, 3, getLastTouchPoints(), 1);
            sendMotionEventToNext(cancelEvent, cancelEvent, 1073741824);
            this.mOpenGesturesInProgress.put(source, false);
        }
    }

    private void cancelAnyPendingInjectedEvents() {
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
            cancelAnyGestureInProgress(4098);
            for (int i = this.mSequencesInProgress.size() - 1; i >= 0; i--) {
                notifyService(this.mServiceInterfaceForCurrentGesture, this.mSequencesInProgress.get(i), false);
                this.mSequencesInProgress.remove(i);
            }
        } else if (this.mNumLastTouchPoints != 0) {
            cancelAnyGestureInProgress(4098);
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

    private List<MotionEvent> getMotionEventsFromGestureSteps(List<GestureDescription.GestureStep> steps, long startTime) {
        List<MotionEvent> motionEvents = new ArrayList<>();
        GestureDescription.TouchPoint[] lastTouchPoints = getLastTouchPoints();
        for (int i = 0; i < steps.size(); i++) {
            GestureDescription.GestureStep step = steps.get(i);
            int currentTouchPointSize = step.numTouchPoints;
            if (currentTouchPointSize > lastTouchPoints.length) {
                this.mNumLastTouchPoints = 0;
                motionEvents.clear();
                return motionEvents;
            }
            appendMoveEventIfNeeded(motionEvents, step.touchPoints, currentTouchPointSize, startTime + step.timeSinceGestureStart);
            appendUpEvents(motionEvents, step.touchPoints, currentTouchPointSize, startTime + step.timeSinceGestureStart);
            appendDownEvents(motionEvents, step.touchPoints, currentTouchPointSize, startTime + step.timeSinceGestureStart);
        }
        return motionEvents;
    }

    private GestureDescription.TouchPoint[] getLastTouchPoints() {
        if (this.mLastTouchPoints == null) {
            int capacity = GestureDescription.getMaxStrokeCount();
            this.mLastTouchPoints = new GestureDescription.TouchPoint[capacity];
            for (int i = 0; i < capacity; i++) {
                this.mLastTouchPoints[i] = new GestureDescription.TouchPoint();
            }
        }
        return this.mLastTouchPoints;
    }

    private void appendMoveEventIfNeeded(List<MotionEvent> motionEvents, GestureDescription.TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
        boolean moveFound = false;
        GestureDescription.TouchPoint[] lastTouchPoints = getLastTouchPoints();
        for (int i = 0; i < currentTouchPointsSize; i++) {
            int lastPointsIndex = findPointByStrokeId(lastTouchPoints, this.mNumLastTouchPoints, currentTouchPoints[i].mStrokeId);
            if (lastPointsIndex >= 0) {
                moveFound |= (lastTouchPoints[lastPointsIndex].mX == currentTouchPoints[i].mX && lastTouchPoints[lastPointsIndex].mY == currentTouchPoints[i].mY) ? false : true;
                lastTouchPoints[lastPointsIndex].copyFrom(currentTouchPoints[i]);
            }
        }
        if (moveFound) {
            motionEvents.add(obtainMotionEvent(this.mDownTime, currentTime, 2, lastTouchPoints, this.mNumLastTouchPoints));
        }
    }

    private void appendUpEvents(List<MotionEvent> motionEvents, GestureDescription.TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
        int i;
        GestureDescription.TouchPoint[] lastTouchPoints = getLastTouchPoints();
        for (int i2 = 0; i2 < currentTouchPointsSize; i2++) {
            if (currentTouchPoints[i2].mIsEndOfPath) {
                int indexOfUpEvent = findPointByStrokeId(lastTouchPoints, this.mNumLastTouchPoints, currentTouchPoints[i2].mStrokeId);
                if (indexOfUpEvent >= 0) {
                    int action = 1;
                    if (this.mNumLastTouchPoints != 1) {
                        action = 6;
                    }
                    motionEvents.add(obtainMotionEvent(this.mDownTime, currentTime, action | (indexOfUpEvent << 8), lastTouchPoints, this.mNumLastTouchPoints));
                    int j = indexOfUpEvent;
                    while (true) {
                        i = this.mNumLastTouchPoints;
                        if (j >= i - 1) {
                            break;
                        }
                        lastTouchPoints[j].copyFrom(this.mLastTouchPoints[j + 1]);
                        j++;
                    }
                    this.mNumLastTouchPoints = i - 1;
                    if (this.mNumLastTouchPoints == 0) {
                        this.mStrokeIdToPointerId.clear();
                    }
                }
            }
        }
    }

    private void appendDownEvents(List<MotionEvent> motionEvents, GestureDescription.TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
        int action;
        GestureDescription.TouchPoint[] lastTouchPoints = getLastTouchPoints();
        for (int i = 0; i < currentTouchPointsSize; i++) {
            if (currentTouchPoints[i].mIsStartOfPath) {
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
                motionEvents.add(obtainMotionEvent(this.mDownTime, currentTime, action | (i << 8), lastTouchPoints, this.mNumLastTouchPoints));
            }
        }
    }

    private MotionEvent obtainMotionEvent(long downTime, long eventTime, int action, GestureDescription.TouchPoint[] touchPoints, int touchPointsSize) {
        MotionEvent.PointerCoords[] pointerCoordsArr = sPointerCoords;
        if (pointerCoordsArr == null || pointerCoordsArr.length < touchPointsSize) {
            sPointerCoords = new MotionEvent.PointerCoords[touchPointsSize];
            for (int i = 0; i < touchPointsSize; i++) {
                sPointerCoords[i] = new MotionEvent.PointerCoords();
            }
        }
        MotionEvent.PointerProperties[] pointerPropertiesArr = sPointerProps;
        if (pointerPropertiesArr == null || pointerPropertiesArr.length < touchPointsSize) {
            sPointerProps = new MotionEvent.PointerProperties[touchPointsSize];
            for (int i2 = 0; i2 < touchPointsSize; i2++) {
                sPointerProps[i2] = new MotionEvent.PointerProperties();
            }
        }
        for (int i3 = 0; i3 < touchPointsSize; i3++) {
            int pointerId = this.mStrokeIdToPointerId.get(touchPoints[i3].mStrokeId, -1);
            if (pointerId == -1) {
                pointerId = getUnusedPointerId();
                this.mStrokeIdToPointerId.put(touchPoints[i3].mStrokeId, pointerId);
            }
            MotionEvent.PointerProperties[] pointerPropertiesArr2 = sPointerProps;
            pointerPropertiesArr2[i3].id = pointerId;
            pointerPropertiesArr2[i3].toolType = 0;
            sPointerCoords[i3].clear();
            MotionEvent.PointerCoords[] pointerCoordsArr2 = sPointerCoords;
            pointerCoordsArr2[i3].pressure = 1.0f;
            pointerCoordsArr2[i3].size = 1.0f;
            pointerCoordsArr2[i3].x = touchPoints[i3].mX;
            sPointerCoords[i3].y = touchPoints[i3].mY;
        }
        return MotionEvent.obtain(downTime, eventTime, action, touchPointsSize, sPointerProps, sPointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0);
    }

    private static int findPointByStrokeId(GestureDescription.TouchPoint[] touchPoints, int touchPointsSize, int strokeId) {
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
