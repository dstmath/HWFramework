package android.media.audiofx;

import android.util.Log;

public class NoiseSuppressor extends AudioEffect {
    private static final String TAG = "NoiseSuppressor";

    public static boolean isAvailable() {
        return AudioEffect.isEffectTypeAvailable(AudioEffect.EFFECT_TYPE_NS);
    }

    public static NoiseSuppressor create(int audioSession) {
        try {
            return new NoiseSuppressor(audioSession);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "not implemented on this device " + null);
            return null;
        } catch (UnsupportedOperationException e2) {
            Log.w(TAG, "not enough resources");
            return null;
        } catch (RuntimeException e3) {
            Log.w(TAG, "not enough memory");
            return null;
        }
    }

    private NoiseSuppressor(int audioSession) throws IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_NS, EFFECT_TYPE_NULL, 0, audioSession);
    }
}
