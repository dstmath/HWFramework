package android.media.audiofx;

import android.util.Log;

public class AcousticEchoCanceler extends AudioEffect {
    private static final String TAG = "AcousticEchoCanceler";

    public static boolean isAvailable() {
        return AudioEffect.isEffectTypeAvailable(AudioEffect.EFFECT_TYPE_AEC);
    }

    public static AcousticEchoCanceler create(int audioSession) {
        try {
            return new AcousticEchoCanceler(audioSession);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "not implemented on this device" + null);
            return null;
        } catch (UnsupportedOperationException e2) {
            Log.w(TAG, "not enough resources");
            return null;
        } catch (RuntimeException e3) {
            Log.w(TAG, "not enough memory");
            return null;
        }
    }

    private AcousticEchoCanceler(int audioSession) throws IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_AEC, EFFECT_TYPE_NULL, 0, audioSession);
    }
}
