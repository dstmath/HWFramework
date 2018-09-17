package android.media.audiofx;

import android.util.Log;
import java.util.StringTokenizer;

public class BassBoost extends AudioEffect {
    public static final int PARAM_STRENGTH = 1;
    public static final int PARAM_STRENGTH_SUPPORTED = 0;
    private static final String TAG = "BassBoost";
    private BaseParameterListener mBaseParamListener;
    private OnParameterChangeListener mParamListener;
    private final Object mParamListenerLock;
    private boolean mStrengthSupported;

    private class BaseParameterListener implements android.media.audiofx.AudioEffect.OnParameterChangeListener {
        private BaseParameterListener() {
        }

        public void onParameterChange(AudioEffect effect, int status, byte[] param, byte[] value) {
            OnParameterChangeListener l = null;
            synchronized (BassBoost.this.mParamListenerLock) {
                if (BassBoost.this.mParamListener != null) {
                    l = BassBoost.this.mParamListener;
                }
            }
            if (l != null) {
                int p = -1;
                short v = (short) -1;
                if (param.length == 4) {
                    p = AudioEffect.byteArrayToInt(param, BassBoost.PARAM_STRENGTH_SUPPORTED);
                }
                if (value.length == 2) {
                    v = AudioEffect.byteArrayToShort(value, BassBoost.PARAM_STRENGTH_SUPPORTED);
                }
                if (p != -1 && v != (short) -1) {
                    l.onParameterChange(BassBoost.this, status, p, v);
                }
            }
        }
    }

    public interface OnParameterChangeListener {
        void onParameterChange(BassBoost bassBoost, int i, int i2, short s);
    }

    public static class Settings {
        public short strength;

        public Settings(String settings) {
            StringTokenizer st = new StringTokenizer(settings, "=;");
            int tokens = st.countTokens();
            if (st.countTokens() != 3) {
                throw new IllegalArgumentException("settings: " + settings);
            }
            String key = st.nextToken();
            if (key.equals(BassBoost.TAG)) {
                try {
                    key = st.nextToken();
                    if (key.equals("strength")) {
                        this.strength = Short.parseShort(st.nextToken());
                        return;
                    }
                    throw new IllegalArgumentException("invalid key name: " + key);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("invalid value for key: " + key);
                }
            }
            throw new IllegalArgumentException("invalid settings for BassBoost: " + key);
        }

        public String toString() {
            return new String("BassBoost;strength=" + Short.toString(this.strength));
        }
    }

    public BassBoost(int priority, int audioSession) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        boolean z = true;
        super(EFFECT_TYPE_BASS_BOOST, EFFECT_TYPE_NULL, priority, audioSession);
        this.mStrengthSupported = false;
        this.mParamListener = null;
        this.mBaseParamListener = null;
        this.mParamListenerLock = new Object();
        if (audioSession == 0) {
            Log.w(TAG, "WARNING: attaching a BassBoost to global output mix is deprecated!");
        }
        int[] value = new int[PARAM_STRENGTH];
        checkStatus(getParameter((int) PARAM_STRENGTH_SUPPORTED, value));
        if (value[PARAM_STRENGTH_SUPPORTED] == 0) {
            z = false;
        }
        this.mStrengthSupported = z;
    }

    public boolean getStrengthSupported() {
        return this.mStrengthSupported;
    }

    public void setStrength(short strength) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_STRENGTH, strength));
    }

    public short getRoundedStrength() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] value = new short[PARAM_STRENGTH];
        checkStatus(getParameter((int) PARAM_STRENGTH, value));
        return value[PARAM_STRENGTH_SUPPORTED];
    }

    public void setParameterListener(OnParameterChangeListener listener) {
        synchronized (this.mParamListenerLock) {
            if (this.mParamListener == null) {
                this.mParamListener = listener;
                this.mBaseParamListener = new BaseParameterListener();
                super.setParameterListener(this.mBaseParamListener);
            }
        }
    }

    public Settings getProperties() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        Settings settings = new Settings();
        short[] value = new short[PARAM_STRENGTH];
        checkStatus(getParameter((int) PARAM_STRENGTH, value));
        settings.strength = value[PARAM_STRENGTH_SUPPORTED];
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_STRENGTH, settings.strength));
    }
}
