package com.android.server.accessibility;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GesturePoint;
import android.gesture.GestureStroke;
import android.gesture.Prediction;
import android.util.Slog;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.util.ArrayList;

class AccessibilityGestureDetector extends SimpleOnGestureListener {
    private static final long CANCEL_ON_PAUSE_THRESHOLD_NOT_STARTED_MS = 200;
    private static final long CANCEL_ON_PAUSE_THRESHOLD_STARTED_MS = 500;
    private static final boolean DEBUG = false;
    private static final int GESTURE_CONFIRM_MM = 10;
    private static final String LOG_TAG = "AccessibilityGestureDetector";
    private static final float MIN_PREDICTION_SCORE = 2.0f;
    private static final int TOUCH_TOLERANCE = 3;
    private long mBaseTime;
    private float mBaseX;
    private float mBaseY;
    private boolean mDoubleTapDetected;
    private boolean mFirstTapDetected;
    private final float mGestureDetectionThreshold;
    private final GestureDetector mGestureDetector;
    private final GestureLibrary mGestureLibrary;
    private boolean mGestureStarted;
    private final Listener mListener;
    private int mPolicyFlags;
    private float mPreviousGestureX;
    private float mPreviousGestureY;
    private boolean mRecognizingGesture;
    private boolean mSecondFingerDoubleTap;
    private long mSecondPointerDownTime;
    private final ArrayList<GesturePoint> mStrokeBuffer;

    public interface Listener {
        boolean onDoubleTap(MotionEvent motionEvent, int i);

        void onDoubleTapAndHold(MotionEvent motionEvent, int i);

        boolean onGestureCancelled(MotionEvent motionEvent, int i);

        boolean onGestureCompleted(int i);

        boolean onGestureStarted();
    }

    AccessibilityGestureDetector(Context context, Listener listener) {
        this.mStrokeBuffer = new ArrayList(100);
        this.mListener = listener;
        this.mGestureDetector = new GestureDetector(context, this);
        this.mGestureDetector.setOnDoubleTapListener(this);
        this.mGestureLibrary = GestureLibraries.fromRawResource(context, 17825794);
        this.mGestureLibrary.setOrientationStyle(8);
        this.mGestureLibrary.setSequenceType(2);
        this.mGestureLibrary.load();
        this.mGestureDetectionThreshold = TypedValue.applyDimension(5, 1.0f, context.getResources().getDisplayMetrics()) * 10.0f;
    }

    public boolean onMotionEvent(MotionEvent event, int policyFlags) {
        float x = event.getX();
        float y = event.getY();
        long time = event.getEventTime();
        this.mPolicyFlags = policyFlags;
        switch (event.getActionMasked()) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                this.mDoubleTapDetected = DEBUG;
                this.mSecondFingerDoubleTap = DEBUG;
                this.mRecognizingGesture = true;
                this.mGestureStarted = DEBUG;
                this.mPreviousGestureX = x;
                this.mPreviousGestureY = y;
                this.mStrokeBuffer.clear();
                this.mStrokeBuffer.add(new GesturePoint(x, y, time));
                this.mBaseX = x;
                this.mBaseY = y;
                this.mBaseTime = time;
                break;
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                if (this.mDoubleTapDetected) {
                    return finishDoubleTap(event, policyFlags);
                }
                if (this.mGestureStarted) {
                    this.mStrokeBuffer.add(new GesturePoint(x, y, time));
                    return recognizeGesture(event, policyFlags);
                }
                break;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                if (this.mRecognizingGesture) {
                    float deltaX = this.mBaseX - x;
                    if (Math.hypot((double) deltaX, (double) (this.mBaseY - y)) > ((double) this.mGestureDetectionThreshold)) {
                        this.mBaseX = x;
                        this.mBaseY = y;
                        this.mBaseTime = time;
                        this.mFirstTapDetected = DEBUG;
                        this.mDoubleTapDetected = DEBUG;
                        if (!this.mGestureStarted) {
                            this.mGestureStarted = true;
                            return this.mListener.onGestureStarted();
                        }
                    } else if (!this.mFirstTapDetected) {
                        long threshold;
                        long timeDelta = time - this.mBaseTime;
                        if (this.mGestureStarted) {
                            threshold = CANCEL_ON_PAUSE_THRESHOLD_STARTED_MS;
                        } else {
                            threshold = CANCEL_ON_PAUSE_THRESHOLD_NOT_STARTED_MS;
                        }
                        if (timeDelta > threshold) {
                            cancelGesture();
                            return this.mListener.onGestureCancelled(event, policyFlags);
                        }
                    }
                    float dX = Math.abs(x - this.mPreviousGestureX);
                    float dY = Math.abs(y - this.mPreviousGestureY);
                    if (dX >= 3.0f || dY >= 3.0f) {
                        this.mPreviousGestureX = x;
                        this.mPreviousGestureY = y;
                        this.mStrokeBuffer.add(new GesturePoint(x, y, time));
                        break;
                    }
                }
                break;
            case TOUCH_TOLERANCE /*3*/:
                clear();
                break;
            case H.ADD_STARTING /*5*/:
                cancelGesture();
                if (event.getPointerCount() != 2) {
                    this.mSecondFingerDoubleTap = DEBUG;
                    break;
                }
                this.mSecondFingerDoubleTap = true;
                this.mSecondPointerDownTime = time;
                break;
            case H.REMOVE_STARTING /*6*/:
                if (this.mSecondFingerDoubleTap && this.mDoubleTapDetected) {
                    return finishDoubleTap(event, policyFlags);
                }
        }
        if (this.mSecondFingerDoubleTap) {
            MotionEvent newEvent = mapSecondPointerToFirstPointer(event);
            if (newEvent == null) {
                return DEBUG;
            }
            boolean handled = this.mGestureDetector.onTouchEvent(newEvent);
            newEvent.recycle();
            return handled;
        } else if (!this.mRecognizingGesture) {
            return DEBUG;
        } else {
            return this.mGestureDetector.onTouchEvent(event);
        }
    }

    public void clear() {
        this.mFirstTapDetected = DEBUG;
        this.mDoubleTapDetected = DEBUG;
        this.mSecondFingerDoubleTap = DEBUG;
        this.mGestureStarted = DEBUG;
        cancelGesture();
    }

    public boolean firstTapDetected() {
        return this.mFirstTapDetected;
    }

    public void onLongPress(MotionEvent e) {
        maybeSendLongPress(e, this.mPolicyFlags);
    }

    public boolean onSingleTapUp(MotionEvent event) {
        this.mFirstTapDetected = true;
        return DEBUG;
    }

    public boolean onSingleTapConfirmed(MotionEvent event) {
        clear();
        return DEBUG;
    }

    public boolean onDoubleTap(MotionEvent event) {
        this.mDoubleTapDetected = true;
        return DEBUG;
    }

    private void maybeSendLongPress(MotionEvent event, int policyFlags) {
        if (this.mDoubleTapDetected) {
            clear();
            this.mListener.onDoubleTapAndHold(event, policyFlags);
        }
    }

    private boolean finishDoubleTap(MotionEvent event, int policyFlags) {
        clear();
        return this.mListener.onDoubleTap(event, policyFlags);
    }

    private void cancelGesture() {
        this.mRecognizingGesture = DEBUG;
        this.mGestureStarted = DEBUG;
        this.mStrokeBuffer.clear();
    }

    private boolean recognizeGesture(MotionEvent event, int policyFlags) {
        Gesture gesture = new Gesture();
        gesture.addStroke(new GestureStroke(this.mStrokeBuffer));
        ArrayList<Prediction> predictions = this.mGestureLibrary.recognize(gesture);
        if (!predictions.isEmpty()) {
            Prediction bestPrediction = (Prediction) predictions.get(0);
            if (bestPrediction.score >= 2.0d) {
                try {
                    return this.mListener.onGestureCompleted(Integer.parseInt(bestPrediction.name));
                } catch (NumberFormatException e) {
                    Slog.w(LOG_TAG, "Non numeric gesture id:" + bestPrediction.name);
                }
            }
        }
        return this.mListener.onGestureCancelled(event, policyFlags);
    }

    private MotionEvent mapSecondPointerToFirstPointer(MotionEvent event) {
        if (event.getPointerCount() != 2 || (event.getActionMasked() != 5 && event.getActionMasked() != 6 && event.getActionMasked() != 2)) {
            return null;
        }
        int action = event.getActionMasked();
        if (action == 5) {
            action = 0;
        } else if (action == 6) {
            action = 1;
        }
        return MotionEvent.obtain(this.mSecondPointerDownTime, event.getEventTime(), action, event.getX(1), event.getY(1), event.getPressure(1), event.getSize(1), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags());
    }
}
