package android.view;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.view.GestureDetectorEx;
import com.huawei.immersion.Vibetonz;
import huawei.com.android.internal.policy.HiTouchSensor;

public class StylusTouchListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final int LONGPRESS_TIMEOUT = SystemPropertiesEx.getInt("ro.config.stylus_longpress_timeout", (int) Vibetonz.HAPTIC_EVENT_SCROLL_INDICATOR_POP);
    private static final String TAG = "StylusTouchListener";
    private Context mContext = null;
    private final GestureDetector mGestureDetector;
    private boolean mIsDoubleTapOccur = false;
    private int mWindowType = 0;

    public StylusTouchListener(Context context) {
        this.mGestureDetector = new GestureDetector(context, this);
        this.mGestureDetector.setIsLongpressEnabled(true);
        this.mGestureDetector.setOnDoubleTapListener(this);
        GestureDetectorEx.setCustomLongpressTimeout(this.mGestureDetector, LONGPRESS_TIMEOUT);
    }

    public boolean onDoubleTap(MotionEvent e) {
        Log.d(TAG, "onDoubleTap");
        this.mIsDoubleTapOccur = true;
        return false;
    }

    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.d(TAG, "onDoubleTapEvent");
        return false;
    }

    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG, "onSingleTapConfirmed");
        return false;
    }

    public boolean onDown(MotionEvent e) {
        Log.d(TAG, "onDown");
        return false;
    }

    public void onShowPress(MotionEvent e) {
        Log.d(TAG, "onShowPress");
    }

    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG, "onSingleTapUp");
        return false;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onScroll");
        return false;
    }

    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "onLongPress");
        if (!this.mIsDoubleTapOccur) {
            this.mIsDoubleTapOccur = false;
            if (this.mContext != null && !inSingleHandWindow()) {
                startHitouchSensor(e);
            }
        }
    }

    private void startHitouchSensor(MotionEvent e) {
        Log.d(TAG, "StylusTouchListener and mWindowType is: " + this.mWindowType);
        try {
            new HiTouchSensor(this.mContext).processStylusGessture(this.mContext, this.mWindowType, e.getX(), e.getY());
        } catch (Exception e2) {
            Log.d(TAG, "startHitouchSensor error");
        }
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "onFling");
        return false;
    }

    public void onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 1 || ev.getAction() == 3) {
            Log.d(TAG, "StylusTouchListener <- onTouchEvent.ACTION_UP");
            this.mIsDoubleTapOccur = false;
        }
        this.mGestureDetector.onTouchEvent(ev);
    }

    public void updateViewContext(Context context, int windowType) {
        this.mContext = context;
        this.mWindowType = windowType;
    }

    private boolean inSingleHandWindow() {
        String value = Settings.Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        Log.d(TAG, "inSingleHandWindow value: " + value);
        if (TextUtils.equals(value, "left") || TextUtils.equals(value, "right")) {
            return true;
        }
        return false;
    }
}
