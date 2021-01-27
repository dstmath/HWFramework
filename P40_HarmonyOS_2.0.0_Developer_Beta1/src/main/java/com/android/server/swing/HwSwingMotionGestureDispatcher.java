package com.android.server.swing;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.server.lights.LightsManagerEx;

public class HwSwingMotionGestureDispatcher {
    private static final int SCANCODE_DEFAULT = 0;
    private static final int SCANCODE_PINCH_CLOSE = 2;
    private static final int SCANCODE_PINCH_OPEN = 1;
    private static final int SCANCODE_START_MUTE = 3;
    private static final int SCANCODE_START_OPEN_HAND = 4;
    private static final String TAG = "HwSwingMotionGestureDispatcher";
    private Context mContext;

    public HwSwingMotionGestureDispatcher(Context context) {
        this.mContext = context;
    }

    private int getKeyCode(String motionGesture) {
        if (HwSwingMotionGestureConstant.VALUE_SLIDE_LEFT.equals(motionGesture)) {
            return 710;
        }
        if (HwSwingMotionGestureConstant.VALUE_SLIDE_RIGHT.equals(motionGesture)) {
            return 711;
        }
        if (HwSwingMotionGestureConstant.VALUE_SLIDE_UP.equals(motionGesture)) {
            return 712;
        }
        if (HwSwingMotionGestureConstant.VALUE_SLIDE_DOWN.equals(motionGesture)) {
            return 713;
        }
        if (HwSwingMotionGestureConstant.VALUE_PUSH.equals(motionGesture)) {
            return 714;
        }
        return 0;
    }

    private int getTvKeyCode(int gestureStatus, int gestureAction) {
        if (gestureStatus != 13) {
            if (gestureStatus == 12) {
                if (gestureAction == 1) {
                    return 164;
                }
                if (gestureAction == 2) {
                    return 731;
                }
                if (gestureAction == 4) {
                    return 728;
                }
                if (gestureAction == 8) {
                    return 730;
                }
                if (gestureAction == 16 || gestureAction == 32 || gestureAction == 64 || gestureAction == 128 || gestureAction == 256) {
                    return 734;
                }
                if (gestureAction == 512) {
                    return 729;
                }
            }
            return 0;
        } else if (gestureAction == 1 || gestureAction == 2) {
            return 732;
        } else {
            return 0;
        }
    }

    private int getTvScanCode(int gestureAction, int gestureOffset) {
        if (gestureAction == 1 || gestureAction == 2 || gestureAction == 4 || gestureAction == 8) {
            return gestureOffset;
        }
        if (gestureAction == 32) {
            return 1;
        }
        if (gestureAction == 64) {
            return 2;
        }
        if (gestureAction != 128) {
            return gestureAction != 256 ? 0 : 4;
        }
        return 3;
    }

    public void dispatchTvMotionGesture(int gestureStatus, int gestureAction, int gestureOffset) {
        Slog.i(TAG, "dispatchTvMotionGesture: gestureAction:" + gestureAction + " ,gestureOffset:" + gestureOffset);
        dispatchMotionGestureKeyEvent(getTvKeyCode(gestureStatus, gestureAction), getTvScanCode(gestureAction, gestureOffset));
    }

    public void dispatchMotionGesture(String motionGesture) {
        Slog.i(TAG, "dispatchMotionGesture:" + motionGesture);
        dispatchMotionGestureKeyEvent(getKeyCode(motionGesture), 0);
    }

    private void dispatchMotionGestureKeyEvent(int keyCode, int scanCode) {
        Slog.i(TAG, "dispatchMotionGestureKeyEvents: keyCode: " + keyCode + " ,scanCode: " + scanCode);
        if (keyCode != 0) {
            long now = SystemClock.uptimeMillis();
            KeyEvent downEvent = new KeyEvent(now, now, 0, keyCode, 0, 0, -1, scanCode, 0, LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT);
            KeyEvent upEvent = new KeyEvent(now, now, 1, keyCode, 0, 0, -1, scanCode, 0, LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT);
            InputManager.getInstance().injectInputEvent(downEvent, 0);
            InputManager.getInstance().injectInputEvent(upEvent, 0);
        }
    }
}
