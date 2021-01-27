package android.media.audiofx;

import android.media.audiofx.AudioEffect;
import android.util.Log;
import java.util.StringTokenizer;

public class BassBoost extends AudioEffect {
    public static final int PARAM_STRENGTH = 1;
    public static final int PARAM_STRENGTH_SUPPORTED = 0;
    private static final String TAG = "BassBoost";
    private BaseParameterListener mBaseParamListener = null;
    private OnParameterChangeListener mParamListener = null;
    private final Object mParamListenerLock = new Object();
    private boolean mStrengthSupported = false;

    public interface OnParameterChangeListener {
        void onParameterChange(BassBoost bassBoost, int i, int i2, short s);
    }

    public BassBoost(int priority, int audioSession) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_BASS_BOOST, EFFECT_TYPE_NULL, priority, audioSession);
        boolean z = false;
        if (audioSession == 0) {
            Log.w(TAG, "WARNING: attaching a BassBoost to global output mix is deprecated!");
        }
        int[] value = new int[1];
        checkStatus(getParameter(0, value));
        this.mStrengthSupported = value[0] != 0 ? true : z;
    }

    public boolean getStrengthSupported() {
        return this.mStrengthSupported;
    }

    public void setStrength(short strength) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(1, strength));
    }

    public short getRoundedStrength() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] value = new short[1];
        checkStatus(getParameter(1, value));
        return value[0];
    }

    private class BaseParameterListener implements AudioEffect.OnParameterChangeListener {
        private BaseParameterListener() {
        }

        @Override // android.media.audiofx.AudioEffect.OnParameterChangeListener
        public void onParameterChange(AudioEffect effect, int status, byte[] param, byte[] value) {
            OnParameterChangeListener l = null;
            synchronized (BassBoost.this.mParamListenerLock) {
                if (BassBoost.this.mParamListener != null) {
                    l = BassBoost.this.mParamListener;
                }
            }
            if (l != null) {
                int p = -1;
                short v = -1;
                if (param.length == 4) {
                    p = AudioEffect.byteArrayToInt(param, 0);
                }
                if (value.length == 2) {
                    v = AudioEffect.byteArrayToShort(value, 0);
                }
                if (p != -1 && v != -1) {
                    l.onParameterChange(BassBoost.this, status, p, v);
                }
            }
        }
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

    public static class Settings {
        public short strength;

        public Settings() {
        }

        public Settings(String settings) {
            StringTokenizer st = new StringTokenizer(settings, "=;");
            st.countTokens();
            if (st.countTokens() == 3) {
                String key = st.nextToken();
                if (key.equals(BassBoost.TAG)) {
                    try {
                        String key2 = st.nextToken();
                        if (key2.equals("strength")) {
                            this.strength = Short.parseShort(st.nextToken());
                            return;
                        }
                        throw new IllegalArgumentException("invalid key name: " + key2);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("invalid value for key: " + key);
                    }
                } else {
                    throw new IllegalArgumentException("invalid settings for BassBoost: " + key);
                }
            } else {
                throw new IllegalArgumentException("settings: " + settings);
            }
        }

        public String toString() {
            return new String("BassBoost;strength=" + Short.toString(this.strength));
        }
    }

    public Settings getProperties() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        Settings settings = new Settings();
        short[] value = new short[1];
        checkStatus(getParameter(1, value));
        settings.strength = value[0];
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(1, settings.strength));
    }
}
