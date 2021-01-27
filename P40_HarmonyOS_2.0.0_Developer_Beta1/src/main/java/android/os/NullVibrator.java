package android.os;

import android.media.AudioAttributes;

public class NullVibrator extends Vibrator {
    private static final NullVibrator sInstance = new NullVibrator();

    private NullVibrator() {
    }

    public static NullVibrator getInstance() {
        return sInstance;
    }

    @Override // android.os.Vibrator
    public boolean hasVibrator() {
        return false;
    }

    @Override // android.os.Vibrator
    public boolean hasAmplitudeControl() {
        return false;
    }

    @Override // android.os.Vibrator
    public void vibrate(int uid, String opPkg, VibrationEffect effect, String reason, AudioAttributes attributes) {
    }

    @Override // android.os.Vibrator
    public void cancel() {
    }
}
