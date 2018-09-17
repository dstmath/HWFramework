package huawei.com.android.server.policy.fingersense;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.Log;
import android.view.MotionEvent;

public class KnockGestureDetector {
    private static final boolean DEBUG = false;
    private static final float DOUBLE_KNOCK_RADIUS_IN = 0.4f;
    private static final float DOUBLE_KNOCK_RADIUS_LONG_IN = 0.6f;
    private static final long DOUBLE_KNOCK_TIMEOUT_MS = 250;
    private static final long DOUBLE_KNOCK_TIMEOUT_SLOW_MS = 400;
    private static final int DOUBLE_KNOCK_TOGGLE_THRESHOLD = 5;
    private static final long KNUCKLE_POINTER_COUNT_TIMEOUT_MS = 250;
    private static final long MS_DELAY_TO_DISABLE_DOUBLE_KNOCK = 250;
    private static final long MS_DELAY_TO_ENABLE_DOUBLE_KNOCK = 550;
    private static final String TAG = "KnockGestureDetector";
    private int consecutiveTouchCount = 1;
    private float doubleKnockDistanceMm;
    private int doubleKnockDistanceTrigger;
    private float doubleKnockRadiusLongPx;
    private float doubleKnockRadiusPx;
    private long doubleKnockTimeDiffMs;
    private int doubleKnockTimeTrigger;
    private final MotionEventRunnable doubleKnuckleDoubleKnockRunnable = new MotionEventRunnable() {
        public void run() {
            if (KnockGestureDetector.this.consecutiveTouchCount < 3) {
                KnockGestureDetector.this.notifyDoubleKnuckleDoubleKnock("fingersense_knuckle_gesture_double_knock", this.event);
            } else {
                KnockGestureDetector.this.dumpDebugVariables();
            }
        }
    };
    private int knucklePointerCount;
    private int knucklePointerId = -1;
    private float lastKnuckleDist;
    private long lastKnuckleEventTimeDiffMs;
    private long lastKnuckleEventTimeMs;
    private float lastKnuckleMoveDist;
    private float lastKnuckleMoveX;
    private float lastKnuckleMoveY;
    private float lastKnuckleX;
    private float lastKnuckleY;
    private long lastMotionEventTimeDiffMs;
    private long lastMotionEventTimeMs;
    private Context mContext;
    private final Handler mHandler;
    private final OnKnockGestureListener mOnKnockGestureListener;
    private final MotionEventRunnable singleKnuckleDoubleKnockRunnable = new MotionEventRunnable() {
        public void run() {
            if (KnockGestureDetector.this.consecutiveTouchCount < 3) {
                KnockGestureDetector.this.notifySingleKnuckleDoubleKnock("fingersense_knuckle_gesture_knock", this.event);
            } else {
                KnockGestureDetector.this.dumpDebugVariables();
            }
        }
    };

    private static abstract class MotionEventRunnable implements Runnable {
        MotionEvent event;

        /* synthetic */ MotionEventRunnable(MotionEventRunnable -this0) {
            this();
        }

        private MotionEventRunnable() {
            this.event = null;
        }
    }

    public interface OnKnockGestureListener {
        void onDoubleKnocksNotYetConfirmed(String str, MotionEvent motionEvent);

        void onDoubleKnuckleDoubleKnock(String str, MotionEvent motionEvent);

        void onSingleKnuckleDoubleKnock(String str, MotionEvent motionEvent);
    }

    public KnockGestureDetector(Context context, OnKnockGestureListener listener) {
        this.mOnKnockGestureListener = listener;
        this.mHandler = new Handler();
        this.mContext = context;
        this.doubleKnockTimeDiffMs = System.getLong(this.mContext.getContentResolver(), "double_knock_timeout", 250);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        this.doubleKnockRadiusPx = (metrics.density * 0.4f) * 160.0f;
        this.doubleKnockRadiusLongPx = (metrics.density * 0.6f) * 160.0f;
        this.doubleKnockDistanceMm = System.getFloat(this.mContext.getContentResolver(), "double_knock_distance", this.doubleKnockRadiusPx);
        this.doubleKnockTimeTrigger = 0;
        this.doubleKnockDistanceTrigger = 0;
    }

    public int getKnucklePointerCount() {
        return this.knucklePointerCount;
    }

    public boolean onKnuckleTouchEvent(MotionEvent motionEvent) {
        long thisKnuckleEventTimeMs = motionEvent.getEventTime();
        this.lastKnuckleEventTimeDiffMs = thisKnuckleEventTimeMs - this.lastKnuckleEventTimeMs;
        this.lastKnuckleEventTimeMs = thisKnuckleEventTimeMs;
        boolean ret = false;
        switch (motionEvent.getAction()) {
            case 0:
                ret = touchDown(motionEvent);
                break;
            case 1:
                ret = touchUp(motionEvent);
                break;
            case 2:
                ret = touchMove(motionEvent);
                break;
            case 3:
                ret = touchCancel(motionEvent);
                break;
        }
        int count = motionEvent.getPointerCount();
        if (count > this.knucklePointerCount) {
            this.knucklePointerCount = count;
        } else if (this.lastKnuckleEventTimeDiffMs < 0 || this.lastKnuckleEventTimeDiffMs > 250) {
            this.knucklePointerCount = count;
        }
        if (motionEvent.getAction() == 0) {
            checkForDoubleKnocks(motionEvent);
        }
        return ret;
    }

    private boolean touchDown(MotionEvent motionEvent) {
        float thisKnuckleX = motionEvent.getX();
        float thisKnuckleY = motionEvent.getY();
        this.lastKnuckleDist = (float) Math.sqrt(Math.pow((double) (thisKnuckleX - this.lastKnuckleX), 2.0d) + Math.pow((double) (thisKnuckleY - this.lastKnuckleY), 2.0d));
        this.lastKnuckleMoveX = thisKnuckleX;
        this.lastKnuckleX = thisKnuckleX;
        this.lastKnuckleMoveY = thisKnuckleY;
        this.lastKnuckleY = thisKnuckleY;
        if (this.lastKnuckleEventTimeDiffMs < 0 || this.lastKnuckleEventTimeDiffMs > this.doubleKnockTimeDiffMs) {
            this.lastKnuckleMoveDist = 0.0f;
            this.knucklePointerId = motionEvent.getPointerId(0);
        }
        return true;
    }

    private boolean touchMove(MotionEvent motionEvent) {
        int pointerIndex = motionEvent.findPointerIndex(this.knucklePointerId);
        if (pointerIndex < 0) {
            return true;
        }
        float thisKnuckleX = motionEvent.getX(pointerIndex);
        float thisKnuckleY = motionEvent.getY(pointerIndex);
        this.lastKnuckleMoveDist += (float) Math.sqrt(Math.pow((double) (thisKnuckleX - this.lastKnuckleMoveX), 2.0d) + Math.pow((double) (thisKnuckleY - this.lastKnuckleMoveY), 2.0d));
        this.lastKnuckleMoveX = thisKnuckleX;
        this.lastKnuckleMoveY = thisKnuckleY;
        return true;
    }

    private boolean touchUp(MotionEvent motionEvent) {
        return true;
    }

    private boolean touchCancel(MotionEvent motionEvent) {
        return true;
    }

    private void toggleDoubleKnockSpeed() {
        long newTimeDiffMs = this.doubleKnockTimeDiffMs == DOUBLE_KNOCK_TIMEOUT_SLOW_MS ? 250 : DOUBLE_KNOCK_TIMEOUT_SLOW_MS;
        if (System.putLong(this.mContext.getContentResolver(), "double_knock_timeout", newTimeDiffMs)) {
            this.doubleKnockTimeDiffMs = newTimeDiffMs;
        } else {
            Log.d(TAG, "Write doubleKnockTimeDiffMs back to Settings failed!!");
        }
    }

    private void checkDoubleKnockSpeedToggle(long doubleKnockTime) {
        long timeInterval = doubleKnockTime;
        if (this.doubleKnockTimeDiffMs == 250) {
            if (doubleKnockTime < 250 || doubleKnockTime > DOUBLE_KNOCK_TIMEOUT_SLOW_MS) {
                return;
            }
        } else if (this.doubleKnockTimeDiffMs == DOUBLE_KNOCK_TIMEOUT_SLOW_MS && doubleKnockTime > 250) {
            return;
        }
        this.doubleKnockTimeTrigger++;
        if (this.doubleKnockTimeTrigger >= 5) {
            toggleDoubleKnockSpeed();
            this.doubleKnockTimeTrigger = 0;
        }
    }

    private void toggleDoubleKnockDistance() {
        float newDistance = this.doubleKnockDistanceMm == this.doubleKnockRadiusLongPx ? this.doubleKnockRadiusPx : this.doubleKnockRadiusLongPx;
        if (System.putFloat(this.mContext.getContentResolver(), "double_knock_distance", newDistance)) {
            this.doubleKnockDistanceMm = newDistance;
        } else {
            Log.d(TAG, "Write doubleKnockDistance back to Settings failed!!");
        }
    }

    private void checkDoubleKnockDistanceToggle(float doubleKnockDistance) {
        float distanceInterval = doubleKnockDistance;
        if (this.doubleKnockDistanceMm == this.doubleKnockRadiusPx) {
            if (doubleKnockDistance < this.doubleKnockRadiusPx || doubleKnockDistance > this.doubleKnockRadiusLongPx) {
                return;
            }
        } else if (this.doubleKnockDistanceMm == this.doubleKnockRadiusLongPx && doubleKnockDistance > this.doubleKnockRadiusPx) {
            return;
        }
        this.doubleKnockDistanceTrigger++;
        if (this.doubleKnockDistanceTrigger >= 5) {
            toggleDoubleKnockDistance();
            this.doubleKnockDistanceTrigger = 0;
        }
    }

    public boolean onAnyTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            long thisMotionEventTimeMs = motionEvent.getEventTime();
            this.lastMotionEventTimeDiffMs = thisMotionEventTimeMs - this.lastMotionEventTimeMs;
            this.lastMotionEventTimeMs = thisMotionEventTimeMs;
            if (this.lastMotionEventTimeDiffMs <= 0 || this.lastMotionEventTimeDiffMs >= MS_DELAY_TO_ENABLE_DOUBLE_KNOCK) {
                this.consecutiveTouchCount = 1;
            } else {
                this.consecutiveTouchCount++;
                if (this.consecutiveTouchCount > 2) {
                    Flog.i(1503, "Multiple rapid taps detecting, aborting Knuckle Gestures.");
                    this.mHandler.removeCallbacks(this.singleKnuckleDoubleKnockRunnable);
                }
            }
        }
        return true;
    }

    public boolean checkForDoubleKnocks(MotionEvent e) {
        final long doubleKnockTimeInterval = this.lastKnuckleEventTimeDiffMs;
        final float doubleKnockDistanceInterval = this.lastKnuckleDist;
        if (this.lastKnuckleEventTimeDiffMs >= this.doubleKnockTimeDiffMs) {
            if ((this.knucklePointerCount == 1 && this.lastKnuckleDist < this.doubleKnockDistanceMm) || (this.knucklePointerCount == 2 && this.lastKnuckleMoveDist < this.doubleKnockDistanceMm * 2.0f)) {
                checkDoubleKnockSpeedToggle(doubleKnockTimeInterval);
            }
            Flog.i(1503, "lastKnuckleEventTimeDiffMs too large, not triggering knock: " + this.lastKnuckleEventTimeDiffMs + " > " + this.doubleKnockTimeDiffMs);
        } else if (this.lastKnuckleMoveDist < this.doubleKnockDistanceMm * 2.0f) {
            MotionEvent motionEvent = e;
            final MotionEvent motionEvent2 = e;
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (KnockGestureDetector.this.knucklePointerCount == 1) {
                        if (KnockGestureDetector.this.lastKnuckleDist < KnockGestureDetector.this.doubleKnockDistanceMm) {
                            KnockGestureDetector.this.onDoubleKnocks(motionEvent2, "fingersense_knuckle_gesture_knock");
                            KnockGestureDetector.this.checkDoubleKnockSpeedToggle(doubleKnockTimeInterval);
                        } else {
                            Log.w(KnockGestureDetector.TAG, "Double knock, but knock too far apart, no screenshot.");
                        }
                        KnockGestureDetector.this.checkDoubleKnockDistanceToggle(doubleKnockDistanceInterval);
                        return;
                    }
                    KnockGestureDetector.this.onDoubleKnocks(motionEvent2, "fingersense_knuckle_gesture_double_knock");
                    KnockGestureDetector.this.checkDoubleKnockSpeedToggle(doubleKnockTimeInterval);
                }
            }, 25);
        } else {
            Flog.i(1503, "lastKnuckleMoveDist too far, not triggering knock: " + this.lastKnuckleMoveDist + " > " + this.doubleKnockRadiusPx);
        }
        return false;
    }

    public void onDoubleKnocks(MotionEvent event, String knuckleGesture) {
        notifyOnDoubleKnocksNotYetConfirmed(knuckleGesture, event);
        if (knuckleGesture != null && knuckleGesture.equals("fingersense_knuckle_gesture_knock")) {
            Flog.i(1503, "Single Knuckle Double Knock captured");
            this.singleKnuckleDoubleKnockRunnable.event = event;
            this.mHandler.postDelayed(this.singleKnuckleDoubleKnockRunnable, 250);
        } else if (knuckleGesture == null || !knuckleGesture.equals("fingersense_knuckle_gesture_double_knock")) {
            Flog.w(1503, "FingerSense Gesture Unrecognized Knock Pattern.");
        } else {
            Flog.i(1503, "Double Knuckle Double Knock captured");
            this.doubleKnuckleDoubleKnockRunnable.event = event;
            this.mHandler.postDelayed(this.doubleKnuckleDoubleKnockRunnable, 250);
        }
    }

    private void notifyDoubleKnuckleDoubleKnock(String gestureName, MotionEvent event) {
        Log.w(TAG, "Notifying Double Knuckle Double Knock");
        if (this.mOnKnockGestureListener != null) {
            this.mOnKnockGestureListener.onDoubleKnuckleDoubleKnock(gestureName, event);
        }
    }

    private void notifySingleKnuckleDoubleKnock(String gestureName, MotionEvent event) {
        Log.w(TAG, "Notifying Single Knuckle Double Knock");
        if (this.mOnKnockGestureListener != null) {
            this.mOnKnockGestureListener.onSingleKnuckleDoubleKnock(gestureName, event);
        }
    }

    private void notifyOnDoubleKnocksNotYetConfirmed(String gestureName, MotionEvent event) {
        if (this.mOnKnockGestureListener != null) {
            this.mOnKnockGestureListener.onDoubleKnocksNotYetConfirmed(gestureName, event);
        }
    }

    private void dumpDebugVariables() {
        Flog.i(1503, "lastMotionEventTimeDiffMs: " + this.lastMotionEventTimeDiffMs + " lastKnuckleEventTimeDiffMs: " + this.lastKnuckleEventTimeDiffMs + " lastKnuckleDist: " + this.lastKnuckleDist + " lastKnuckleMoveDist: " + this.lastKnuckleMoveDist + " knucklePointerCount: " + this.knucklePointerCount + " consecutiveTouchCount: " + this.consecutiveTouchCount + " doubleKnockTimeDiffMs: " + this.doubleKnockTimeDiffMs + " doubleKnockDistanceMm: " + this.doubleKnockDistanceMm);
    }
}
