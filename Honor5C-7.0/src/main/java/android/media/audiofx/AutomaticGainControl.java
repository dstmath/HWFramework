package android.media.audiofx;

import android.util.Log;

public class AutomaticGainControl extends AudioEffect {
    private static final String TAG = "AutomaticGainControl";

    public static boolean isAvailable() {
        return AudioEffect.isEffectTypeAvailable(AudioEffect.EFFECT_TYPE_AGC);
    }

    public static AutomaticGainControl create(int audioSession) {
        try {
            return new AutomaticGainControl(audioSession);
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

    private AutomaticGainControl(int audioSession) throws IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_AGC, EFFECT_TYPE_NULL, 0, audioSession);
    }
}
