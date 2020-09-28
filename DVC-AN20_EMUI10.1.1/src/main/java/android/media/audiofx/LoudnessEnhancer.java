package android.media.audiofx;

import android.media.audiofx.AudioEffect;
import android.util.Log;
import java.util.StringTokenizer;

public class LoudnessEnhancer extends AudioEffect {
    public static final int PARAM_TARGET_GAIN_MB = 0;
    private static final String TAG = "LoudnessEnhancer";
    private BaseParameterListener mBaseParamListener = null;
    private OnParameterChangeListener mParamListener = null;
    private final Object mParamListenerLock = new Object();

    public interface OnParameterChangeListener {
        void onParameterChange(LoudnessEnhancer loudnessEnhancer, int i, int i2);
    }

    public LoudnessEnhancer(int audioSession) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_LOUDNESS_ENHANCER, EFFECT_TYPE_NULL, 0, audioSession);
        if (audioSession == 0) {
            Log.w(TAG, "WARNING: attaching a LoudnessEnhancer to global output mix is deprecated!");
        }
    }

    public LoudnessEnhancer(int priority, int audioSession) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_LOUDNESS_ENHANCER, EFFECT_TYPE_NULL, priority, audioSession);
        if (audioSession == 0) {
            Log.w(TAG, "WARNING: attaching a LoudnessEnhancer to global output mix is deprecated!");
        }
    }

    public void setTargetGain(int gainmB) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(0, gainmB));
    }

    public float getTargetGain() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        int[] value = new int[1];
        checkStatus(getParameter(0, value));
        return (float) value[0];
    }

    private class BaseParameterListener implements AudioEffect.OnParameterChangeListener {
        private BaseParameterListener() {
        }

        @Override // android.media.audiofx.AudioEffect.OnParameterChangeListener
        public void onParameterChange(AudioEffect effect, int status, byte[] param, byte[] value) {
            if (status == 0) {
                OnParameterChangeListener l = null;
                synchronized (LoudnessEnhancer.this.mParamListenerLock) {
                    if (LoudnessEnhancer.this.mParamListener != null) {
                        l = LoudnessEnhancer.this.mParamListener;
                    }
                }
                if (l != null) {
                    int p = -1;
                    int v = Integer.MIN_VALUE;
                    if (param.length == 4) {
                        p = AudioEffect.byteArrayToInt(param, 0);
                    }
                    if (value.length == 4) {
                        v = AudioEffect.byteArrayToInt(value, 0);
                    }
                    if (p != -1 && v != Integer.MIN_VALUE) {
                        l.onParameterChange(LoudnessEnhancer.this, p, v);
                    }
                }
            }
        }
    }

    public void setParameterListener(OnParameterChangeListener listener) {
        synchronized (this.mParamListenerLock) {
            if (this.mParamListener == null) {
                this.mBaseParamListener = new BaseParameterListener();
                super.setParameterListener(this.mBaseParamListener);
            }
            this.mParamListener = listener;
        }
    }

    public static class Settings {
        public int targetGainmB;

        public Settings() {
        }

        public Settings(String settings) {
            StringTokenizer st = new StringTokenizer(settings, "=;");
            if (st.countTokens() == 3) {
                String key = st.nextToken();
                if (key.equals(LoudnessEnhancer.TAG)) {
                    try {
                        String key2 = st.nextToken();
                        if (key2.equals("targetGainmB")) {
                            this.targetGainmB = Integer.parseInt(st.nextToken());
                            return;
                        }
                        throw new IllegalArgumentException("invalid key name: " + key2);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("invalid value for key: " + key);
                    }
                } else {
                    throw new IllegalArgumentException("invalid settings for LoudnessEnhancer: " + key);
                }
            } else {
                throw new IllegalArgumentException("settings: " + settings);
            }
        }

        public String toString() {
            return new String("LoudnessEnhancer;targetGainmB=" + Integer.toString(this.targetGainmB));
        }
    }

    public Settings getProperties() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        Settings settings = new Settings();
        int[] value = new int[1];
        checkStatus(getParameter(0, value));
        settings.targetGainmB = value[0];
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(0, settings.targetGainmB));
    }
}
