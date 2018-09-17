package android.media.audiofx;

import java.util.StringTokenizer;

public class PresetReverb extends AudioEffect {
    public static final int PARAM_PRESET = 0;
    public static final short PRESET_LARGEHALL = (short) 5;
    public static final short PRESET_LARGEROOM = (short) 3;
    public static final short PRESET_MEDIUMHALL = (short) 4;
    public static final short PRESET_MEDIUMROOM = (short) 2;
    public static final short PRESET_NONE = (short) 0;
    public static final short PRESET_PLATE = (short) 6;
    public static final short PRESET_SMALLROOM = (short) 1;
    private static final String TAG = "PresetReverb";
    private BaseParameterListener mBaseParamListener = null;
    private OnParameterChangeListener mParamListener = null;
    private final Object mParamListenerLock = new Object();

    private class BaseParameterListener implements android.media.audiofx.AudioEffect.OnParameterChangeListener {
        /* synthetic */ BaseParameterListener(PresetReverb this$0, BaseParameterListener -this1) {
            this();
        }

        private BaseParameterListener() {
        }

        public void onParameterChange(AudioEffect effect, int status, byte[] param, byte[] value) {
            OnParameterChangeListener l = null;
            synchronized (PresetReverb.this.mParamListenerLock) {
                if (PresetReverb.this.mParamListener != null) {
                    l = PresetReverb.this.mParamListener;
                }
            }
            if (l != null) {
                int p = -1;
                short v = (short) -1;
                if (param.length == 4) {
                    p = AudioEffect.byteArrayToInt(param, 0);
                }
                if (value.length == 2) {
                    v = AudioEffect.byteArrayToShort(value, 0);
                }
                if (p != -1 && v != (short) -1) {
                    l.onParameterChange(PresetReverb.this, status, p, v);
                }
            }
        }
    }

    public interface OnParameterChangeListener {
        void onParameterChange(PresetReverb presetReverb, int i, int i2, short s);
    }

    public static class Settings {
        public short preset;

        public Settings(String settings) {
            StringTokenizer st = new StringTokenizer(settings, "=;");
            int tokens = st.countTokens();
            if (st.countTokens() != 3) {
                throw new IllegalArgumentException("settings: " + settings);
            }
            String key = st.nextToken();
            if (key.equals(PresetReverb.TAG)) {
                try {
                    key = st.nextToken();
                    if (key.equals("preset")) {
                        this.preset = Short.parseShort(st.nextToken());
                        return;
                    }
                    throw new IllegalArgumentException("invalid key name: " + key);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("invalid value for key: " + key);
                }
            }
            throw new IllegalArgumentException("invalid settings for PresetReverb: " + key);
        }

        public String toString() {
            return new String("PresetReverb;preset=" + Short.toString(this.preset));
        }
    }

    public PresetReverb(int priority, int audioSession) throws IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_PRESET_REVERB, EFFECT_TYPE_NULL, priority, audioSession);
    }

    public void setPreset(short preset) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(0, preset));
    }

    public short getPreset() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] value = new short[1];
        checkStatus(getParameter(0, value));
        return value[0];
    }

    public void setParameterListener(OnParameterChangeListener listener) {
        synchronized (this.mParamListenerLock) {
            if (this.mParamListener == null) {
                this.mParamListener = listener;
                this.mBaseParamListener = new BaseParameterListener(this, null);
                super.setParameterListener(this.mBaseParamListener);
            }
        }
    }

    public Settings getProperties() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        Settings settings = new Settings();
        short[] value = new short[1];
        checkStatus(getParameter(0, value));
        settings.preset = value[0];
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(0, settings.preset));
    }
}
