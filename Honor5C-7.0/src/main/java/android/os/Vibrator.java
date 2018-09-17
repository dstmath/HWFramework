package android.os;

import android.app.ActivityThread;
import android.content.Context;
import android.media.AudioAttributes;

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
    private final String mPackageName;

    public abstract void cancel();

    public abstract boolean hasVibrator();

    public abstract void vibrate(int i, String str, long j, AudioAttributes audioAttributes);

    public abstract void vibrate(int i, String str, long[] jArr, int i2, AudioAttributes audioAttributes);

    public Vibrator() {
        this.mPackageName = ActivityThread.currentPackageName();
    }

    protected Vibrator(Context context) {
        this.mPackageName = context.getOpPackageName();
    }

    public void vibrate(long milliseconds) {
        vibrate(milliseconds, null);
    }

    public void vibrate(long milliseconds, AudioAttributes attributes) {
        vibrate(Process.myUid(), this.mPackageName, milliseconds, attributes);
    }

    public void hwVibrate(AudioAttributes attributes, int mode) {
        hwVibrate(Process.myUid(), this.mPackageName, attributes, mode);
    }

    protected void hwVibrate(int uid, String opPkg, AudioAttributes attributes, int mode) {
    }

    public void vibrate(long[] pattern, int repeat) {
        vibrate(pattern, repeat, null);
    }

    public void vibrate(long[] pattern, int repeat, AudioAttributes attributes) {
        vibrate(Process.myUid(), this.mPackageName, pattern, repeat, attributes);
    }
}
