package com.android.server.accessibility;

import android.accessibilityservice.IAccessibilityServiceClient;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.os.SomeArgs;
import java.util.List;

public class MotionEventInjector implements EventStreamTransformation {
    private static final String LOG_TAG = "MotionEventInjector";
    private static final int MAX_POINTERS = 11;
    private static final int MESSAGE_INJECT_EVENTS = 2;
    private static final int MESSAGE_SEND_MOTION_EVENT = 1;
    private final Handler mHandler;
    private boolean mIsDestroyed;
    private EventStreamTransformation mNext;
    private final SparseArray<Boolean> mOpenGesturesInProgress;
    private PointerCoords[] mPointerCoords;
    private PointerProperties[] mPointerProperties;
    private int mSequenceForCurrentGesture;
    private IAccessibilityServiceClient mServiceInterfaceForCurrentGesture;
    private int mSourceOfInjectedGesture;

    private class Callback implements android.os.Handler.Callback {
        private Callback() {
        }

        public boolean handleMessage(Message message) {
            if (message.what == MotionEventInjector.MESSAGE_INJECT_EVENTS) {
                SomeArgs args = message.obj;
                MotionEventInjector.this.injectEventsMainThread((List) args.arg1, (IAccessibilityServiceClient) args.arg2, args.argi1);
                args.recycle();
                return true;
            } else if (message.what != MotionEventInjector.MESSAGE_SEND_MOTION_EVENT) {
                throw new IllegalArgumentException("Unknown message: " + message.what);
            } else {
                MotionEvent motionEvent = message.obj;
                MotionEventInjector.this.sendMotionEventToNext(motionEvent, motionEvent, 1073741824);
                if (!MotionEventInjector.this.mHandler.hasMessages(MotionEventInjector.MESSAGE_SEND_MOTION_EVENT)) {
                    MotionEventInjector.this.notifyService(true);
                }
                return true;
            }
        }
    }

    public MotionEventInjector(Looper looper) {
        this.mOpenGesturesInProgress = new SparseArray();
        this.mPointerProperties = new PointerProperties[MAX_POINTERS];
        this.mPointerCoords = new PointerCoords[MAX_POINTERS];
        this.mSourceOfInjectedGesture = 0;
        this.mIsDestroyed = false;
        this.mHandler = new Handler(looper, new Callback());
    }

    public void injectEvents(List<MotionEvent> events, IAccessibilityServiceClient serviceInterface, int sequence) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = events;
        args.arg2 = serviceInterface;
        args.argi1 = sequence;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(MESSAGE_INJECT_EVENTS, args));
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
        if (!this.mHandler.hasMessages(MESSAGE_SEND_MOTION_EVENT)) {
            this.mOpenGesturesInProgress.put(inputSource, Boolean.valueOf(false));
        }
    }

    public void onDestroy() {
        cancelAnyPendingInjectedEvents();
        this.mIsDestroyed = true;
    }

    private void injectEventsMainThread(List<MotionEvent> events, IAccessibilityServiceClient serviceInterface, int sequence) {
        if (this.mIsDestroyed) {
            try {
                serviceInterface.onPerformGestureResult(sequence, false);
            } catch (Throwable re) {
                Slog.e(LOG_TAG, "Error sending status with mIsDestroyed to " + serviceInterface, re);
            }
            return;
        }
        cancelAnyPendingInjectedEvents();
        this.mSourceOfInjectedGesture = ((MotionEvent) events.get(0)).getSource();
        cancelAnyGestureInProgress(this.mSourceOfInjectedGesture);
        this.mServiceInterfaceForCurrentGesture = serviceInterface;
        this.mSequenceForCurrentGesture = sequence;
        if (this.mNext == null) {
            notifyService(false);
            return;
        }
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < events.size(); i += MESSAGE_SEND_MOTION_EVENT) {
            MotionEvent event = (MotionEvent) events.get(i);
            int numPointers = event.getPointerCount();
            if (numPointers > this.mPointerCoords.length) {
                this.mPointerCoords = new PointerCoords[numPointers];
                this.mPointerProperties = new PointerProperties[numPointers];
            }
            for (int j = 0; j < numPointers; j += MESSAGE_SEND_MOTION_EVENT) {
                if (this.mPointerCoords[j] == null) {
                    this.mPointerCoords[j] = new PointerCoords();
                    this.mPointerProperties[j] = new PointerProperties();
                }
                event.getPointerCoords(j, this.mPointerCoords[j]);
                event.getPointerProperties(j, this.mPointerProperties[j]);
            }
            Message obtainMessage = this.mHandler.obtainMessage(MESSAGE_SEND_MOTION_EVENT, MotionEvent.obtain(event.getDownTime() + startTime, event.getEventTime() + startTime, event.getAction(), numPointers, this.mPointerProperties, this.mPointerCoords, event.getMetaState(), event.getButtonState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags(), event.getSource(), event.getFlags()));
            this.mHandler.sendMessageDelayed(message, event.getEventTime());
        }
    }

    private void sendMotionEventToNext(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (this.mNext != null) {
            this.mNext.onMotionEvent(event, rawEvent, policyFlags);
            if (event.getActionMasked() == 0) {
                this.mOpenGesturesInProgress.put(event.getSource(), Boolean.valueOf(true));
            }
            if (event.getActionMasked() == MESSAGE_SEND_MOTION_EVENT || event.getActionMasked() == 3) {
                this.mOpenGesturesInProgress.put(event.getSource(), Boolean.valueOf(false));
            }
        }
    }

    private void cancelAnyGestureInProgress(int source) {
        if (this.mNext != null && ((Boolean) this.mOpenGesturesInProgress.get(source, Boolean.valueOf(false))).booleanValue()) {
            long now = SystemClock.uptimeMillis();
            MotionEvent cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
            sendMotionEventToNext(cancelEvent, cancelEvent, 1073741824);
        }
    }

    private void cancelAnyPendingInjectedEvents() {
        if (this.mHandler.hasMessages(MESSAGE_SEND_MOTION_EVENT)) {
            cancelAnyGestureInProgress(this.mSourceOfInjectedGesture);
            this.mHandler.removeMessages(MESSAGE_SEND_MOTION_EVENT);
            notifyService(false);
        }
    }

    private void notifyService(boolean success) {
        try {
            this.mServiceInterfaceForCurrentGesture.onPerformGestureResult(this.mSequenceForCurrentGesture, success);
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error sending motion event injection status to " + this.mServiceInterfaceForCurrentGesture, re);
        }
    }
}
