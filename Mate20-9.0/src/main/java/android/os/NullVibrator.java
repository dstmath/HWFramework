package android.os;

import android.media.AudioAttributes;

public class NullVibrator extends Vibrator {
    private static final NullVibrator sInstance = new NullVibrator();

    private NullVibrator() {
    }

    public static NullVibrator getInstance() {
        return sInstance;
    }

    public boolean hasVibrator() {
        return false;
    }

    public boolean hasAmplitudeControl() {
        return false;
    }

    public void vibrate(int uid, String opPkg, VibrationEffect effect, AudioAttributes attributes) {
    }

    public void cancel() {
    }
}
