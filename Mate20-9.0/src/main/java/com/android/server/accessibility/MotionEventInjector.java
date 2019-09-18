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

    public /* bridge */ /* synthetic */ EventStreamTransformation getNext() {
        return super.getNext();
    }

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
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, args));
    }

    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        cancelAnyPendingInjectedEvents();
        sendMotionEventToNext(event, rawEvent, policyFlags);
    }

    public void clearEvents(int inputSource) {
        if (!this.mHandler.hasMessages(1)) {
            this.mOpenGesturesInProgress.put(inputSource, false);
        }
    }

    public void onDestroy() {
        cancelAnyPendingInjectedEvents();
        this.mIsDestroyed = true;
    }

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
        long j;
        MotionEventInjector motionEventInjector = this;
        IAccessibilityServiceClient iAccessibilityServiceClient = serviceInterface;
        int i = sequence;
        if (motionEventInjector.mIsDestroyed) {
            try {
                iAccessibilityServiceClient.onPerformGestureResult(i, false);
            } catch (RemoteException re) {
                RemoteException remoteException = re;
                Slog.e(LOG_TAG, "Error sending status with mIsDestroyed to " + iAccessibilityServiceClient, re);
            }
        } else if (getNext() == null) {
            motionEventInjector.notifyService(iAccessibilityServiceClient, i, false);
        } else {
            boolean continuingGesture = newGestureTriesToContinueOldOne(gestureSteps);
            if (!continuingGesture || (iAccessibilityServiceClient == motionEventInjector.mServiceInterfaceForCurrentGesture && prepareToContinueOldGesture(gestureSteps))) {
                if (!continuingGesture) {
                    cancelAnyPendingInjectedEvents();
                    motionEventInjector.cancelAnyGestureInProgress(4098);
                }
                motionEventInjector.mServiceInterfaceForCurrentGesture = iAccessibilityServiceClient;
                long currentTime = SystemClock.uptimeMillis();
                if (motionEventInjector.mSequencesInProgress.size() == 0) {
                    j = currentTime;
                } else {
                    j = motionEventInjector.mLastScheduledEventTime;
                }
                List<MotionEvent> events = motionEventInjector.getMotionEventsFromGestureSteps(gestureSteps, j);
                if (events.isEmpty()) {
                    motionEventInjector.notifyService(iAccessibilityServiceClient, i, false);
                    return;
                }
                motionEventInjector.mSequencesInProgress.add(i);
                int i2 = 0;
                while (i2 < events.size()) {
                    MotionEvent event = events.get(i2);
                    Message message = motionEventInjector.mHandler.obtainMessage(1, i2 == events.size() - 1 ? 1 : 0, 0, event);
                    motionEventInjector.mLastScheduledEventTime = event.getEventTime();
                    motionEventInjector.mHandler.sendMessageDelayed(message, Math.max(0, event.getEventTime() - currentTime));
                    i2++;
                    continuingGesture = continuingGesture;
                    motionEventInjector = this;
                }
                return;
            }
            cancelAnyPendingInjectedEvents();
            motionEventInjector.notifyService(iAccessibilityServiceClient, i, false);
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
        boolean z = false;
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
            z = true;
        }
        return z;
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
        ArrayList arrayList = new ArrayList();
        GestureDescription.TouchPoint[] lastTouchPoints = getLastTouchPoints();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= steps.size()) {
                return arrayList;
            }
            GestureDescription.GestureStep step = steps.get(i2);
            int currentTouchPointSize = step.numTouchPoints;
            if (currentTouchPointSize > lastTouchPoints.length) {
                this.mNumLastTouchPoints = 0;
                arrayList.clear();
                return arrayList;
            }
            int i3 = currentTouchPointSize;
            appendMoveEventIfNeeded(arrayList, step.touchPoints, i3, startTime + step.timeSinceGestureStart);
            appendUpEvents(arrayList, step.touchPoints, i3, startTime + step.timeSinceGestureStart);
            appendDownEvents(arrayList, step.touchPoints, i3, startTime + step.timeSinceGestureStart);
            i = i2 + 1;
        }
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
        GestureDescription.TouchPoint[] lastTouchPoints = getLastTouchPoints();
        boolean moveFound = false;
        for (int i = 0; i < currentTouchPointsSize; i++) {
            int lastPointsIndex = findPointByStrokeId(lastTouchPoints, this.mNumLastTouchPoints, currentTouchPoints[i].mStrokeId);
            if (lastPointsIndex >= 0) {
                boolean z = (lastTouchPoints[lastPointsIndex].mX == currentTouchPoints[i].mX && lastTouchPoints[lastPointsIndex].mY == currentTouchPoints[i].mY) ? false : true;
                lastTouchPoints[lastPointsIndex].copyFrom(currentTouchPoints[i]);
                moveFound = z | moveFound;
            }
        }
        if (moveFound) {
            motionEvents.add(obtainMotionEvent(this.mDownTime, currentTime, 2, lastTouchPoints, this.mNumLastTouchPoints));
        } else {
            List<MotionEvent> list = motionEvents;
        }
    }

    private void appendUpEvents(List<MotionEvent> motionEvents, GestureDescription.TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
        GestureDescription.TouchPoint[] lastTouchPoints = getLastTouchPoints();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < currentTouchPointsSize) {
                if (currentTouchPoints[i2].mIsEndOfPath) {
                    int indexOfUpEvent = findPointByStrokeId(lastTouchPoints, this.mNumLastTouchPoints, currentTouchPoints[i2].mStrokeId);
                    if (indexOfUpEvent >= 0) {
                        motionEvents.add(obtainMotionEvent(this.mDownTime, currentTime, (this.mNumLastTouchPoints == 1 ? 1 : 6) | (indexOfUpEvent << 8), lastTouchPoints, this.mNumLastTouchPoints));
                        for (int j = indexOfUpEvent; j < this.mNumLastTouchPoints - 1; j++) {
                            lastTouchPoints[j].copyFrom(this.mLastTouchPoints[j + 1]);
                        }
                        this.mNumLastTouchPoints--;
                        if (this.mNumLastTouchPoints == 0) {
                            this.mStrokeIdToPointerId.clear();
                        }
                        i = i2 + 1;
                    }
                }
                List<MotionEvent> list = motionEvents;
                i = i2 + 1;
            } else {
                List<MotionEvent> list2 = motionEvents;
                return;
            }
        }
    }

    private void appendDownEvents(List<MotionEvent> motionEvents, GestureDescription.TouchPoint[] currentTouchPoints, int currentTouchPointsSize, long currentTime) {
        long j;
        GestureDescription.TouchPoint[] lastTouchPoints = getLastTouchPoints();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < currentTouchPointsSize) {
                if (currentTouchPoints[i2].mIsStartOfPath) {
                    int i3 = this.mNumLastTouchPoints;
                    this.mNumLastTouchPoints = i3 + 1;
                    lastTouchPoints[i3].copyFrom(currentTouchPoints[i2]);
                    int action = this.mNumLastTouchPoints == 1 ? 0 : 5;
                    if (action == 0) {
                        j = currentTime;
                        this.mDownTime = j;
                    } else {
                        j = currentTime;
                    }
                    motionEvents.add(obtainMotionEvent(this.mDownTime, j, action | (i2 << 8), lastTouchPoints, this.mNumLastTouchPoints));
                } else {
                    List<MotionEvent> list = motionEvents;
                    long j2 = currentTime;
                }
                i = i2 + 1;
            } else {
                List<MotionEvent> list2 = motionEvents;
                long j3 = currentTime;
                return;
            }
        }
    }

    private MotionEvent obtainMotionEvent(long downTime, long eventTime, int action, GestureDescription.TouchPoint[] touchPoints, int touchPointsSize) {
        int i = touchPointsSize;
        if (sPointerCoords == null || sPointerCoords.length < i) {
            sPointerCoords = new MotionEvent.PointerCoords[i];
            for (int i2 = 0; i2 < i; i2++) {
                sPointerCoords[i2] = new MotionEvent.PointerCoords();
            }
        }
        if (sPointerProps == null || sPointerProps.length < i) {
            sPointerProps = new MotionEvent.PointerProperties[i];
            for (int i3 = 0; i3 < i; i3++) {
                sPointerProps[i3] = new MotionEvent.PointerProperties();
            }
        }
        for (int i4 = 0; i4 < i; i4++) {
            int pointerId = this.mStrokeIdToPointerId.get(touchPoints[i4].mStrokeId, -1);
            if (pointerId == -1) {
                pointerId = getUnusedPointerId();
                this.mStrokeIdToPointerId.put(touchPoints[i4].mStrokeId, pointerId);
            }
            sPointerProps[i4].id = pointerId;
            sPointerProps[i4].toolType = 0;
            sPointerCoords[i4].clear();
            sPointerCoords[i4].pressure = 1.0f;
            sPointerCoords[i4].size = 1.0f;
            sPointerCoords[i4].x = touchPoints[i4].mX;
            sPointerCoords[i4].y = touchPoints[i4].mY;
        }
        return MotionEvent.obtain(downTime, eventTime, action, i, sPointerProps, sPointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0);
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
