package android.os;

import android.app.ActivityThread;
import android.content.Context;
import android.media.AudioAttributes;
import android.util.Log;

public abstract class Vibrator {
    public static final int HW_VIBRATOR_TPYE_APP_NAVI = 5;
    public static final int HW_VIBRATOR_TPYE_CAMERA = 6;
    public static final int HW_VIBRATOR_TPYE_DIAL = 7;
    public static final int HW_VIBRATOR_TPYE_END = 9;
    public static final int HW_VIBRATOR_TPYE_FAILED = 2;
    public static final int HW_VIBRATOR_TPYE_GALLERY = 8;
    public static final int HW_VIBRATOR_TPYE_LONG = 3;
    public static final int HW_VIBRATOR_TPYE_NAVI = 4;
    public static final int HW_VIBRATOR_TPYE_SUCCESS = 1;
    private static final String TAG = "Vibrator";
    private final String mPackageName;

    public abstract void cancel();

    public abstract boolean hasAmplitudeControl();

    public abstract boolean hasVibrator();

    public abstract void vibrate(int i, String str, VibrationEffect vibrationEffect, AudioAttributes audioAttributes);

    public Vibrator() {
        this.mPackageName = ActivityThread.currentPackageName();
    }

    protected Vibrator(Context context) {
        this.mPackageName = context.getOpPackageName();
    }

    @Deprecated
    public void vibrate(long milliseconds) {
        vibrate(milliseconds, null);
    }

    @Deprecated
    public void vibrate(long milliseconds, AudioAttributes attributes) {
        try {
            vibrate(VibrationEffect.createOneShot(milliseconds, -1), attributes);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to create VibrationEffect", iae);
        }
    }

    public void hwVibrate(AudioAttributes attributes, int mode) {
        hwVibrate(Process.myUid(), this.mPackageName, attributes, mode);
    }

    protected void hwVibrate(int uid, String opPkg, AudioAttributes attributes, int mode) {
    }

    @Deprecated
    public void vibrate(long[] pattern, int repeat) {
        vibrate(pattern, repeat, null);
    }

    @Deprecated
    public void vibrate(long[] pattern, int repeat, AudioAttributes attributes) {
        if (repeat < -1 || repeat >= pattern.length) {
            Log.e(TAG, "vibrate called with repeat index out of bounds (pattern.length=" + pattern.length + ", index=" + repeat + ")");
            throw new ArrayIndexOutOfBoundsException();
        }
        try {
            vibrate(VibrationEffect.createWaveform(pattern, repeat), attributes);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to create VibrationEffect", iae);
        }
    }

    public void vibrate(VibrationEffect vibe) {
        vibrate(vibe, null);
    }

    public void vibrate(VibrationEffect vibe, AudioAttributes attributes) {
        vibrate(Process.myUid(), this.mPackageName, vibe, attributes);
    }
}
