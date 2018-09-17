package android.media.audiofx;

import android.net.ProxyInfo;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

public class Equalizer extends AudioEffect {
    public static final int PARAM_BAND_FREQ_RANGE = 4;
    public static final int PARAM_BAND_LEVEL = 2;
    public static final int PARAM_CENTER_FREQ = 3;
    public static final int PARAM_CURRENT_PRESET = 6;
    public static final int PARAM_GET_BAND = 5;
    public static final int PARAM_GET_NUM_OF_PRESETS = 7;
    public static final int PARAM_GET_PRESET_NAME = 8;
    public static final int PARAM_LEVEL_RANGE = 1;
    public static final int PARAM_NUM_BANDS = 0;
    private static final int PARAM_PROPERTIES = 9;
    public static final int PARAM_STRING_SIZE_MAX = 32;
    private static final String TAG = "Equalizer";
    private BaseParameterListener mBaseParamListener = null;
    private short mNumBands = (short) 0;
    private int mNumPresets;
    private OnParameterChangeListener mParamListener = null;
    private final Object mParamListenerLock = new Object();
    private String[] mPresetNames;

    private class BaseParameterListener implements android.media.audiofx.AudioEffect.OnParameterChangeListener {
        /* synthetic */ BaseParameterListener(Equalizer this$0, BaseParameterListener -this1) {
            this();
        }

        private BaseParameterListener() {
        }

        public void onParameterChange(AudioEffect effect, int status, byte[] param, byte[] value) {
            OnParameterChangeListener l = null;
            synchronized (Equalizer.this.mParamListenerLock) {
                if (Equalizer.this.mParamListener != null) {
                    l = Equalizer.this.mParamListener;
                }
            }
            if (l != null) {
                int p1 = -1;
                int p2 = -1;
                int v = -1;
                if (param.length >= 4) {
                    p1 = AudioEffect.byteArrayToInt(param, 0);
                    if (param.length >= 8) {
                        p2 = AudioEffect.byteArrayToInt(param, 4);
                    }
                }
                if (value.length == 2) {
                    v = AudioEffect.byteArrayToShort(value, 0);
                } else if (value.length == 4) {
                    v = AudioEffect.byteArrayToInt(value, 0);
                }
                if (p1 != -1 && v != -1) {
                    l.onParameterChange(Equalizer.this, status, p1, p2, v);
                }
            }
        }
    }

    public interface OnParameterChangeListener {
        void onParameterChange(Equalizer equalizer, int i, int i2, int i3, int i4);
    }

    public static class Settings {
        public short[] bandLevels = null;
        public short curPreset;
        public short numBands = (short) 0;

        public Settings(String settings) {
            StringTokenizer st = new StringTokenizer(settings, "=;");
            int tokens = st.countTokens();
            if (st.countTokens() < 5) {
                throw new IllegalArgumentException("settings: " + settings);
            }
            String key = st.nextToken();
            if (key.equals(Equalizer.TAG)) {
                try {
                    key = st.nextToken();
                    if (key.equals("curPreset")) {
                        this.curPreset = Short.parseShort(st.nextToken());
                        key = st.nextToken();
                        if (key.equals("numBands")) {
                            this.numBands = Short.parseShort(st.nextToken());
                            if (st.countTokens() != this.numBands * 2) {
                                throw new IllegalArgumentException("settings: " + settings);
                            }
                            this.bandLevels = new short[this.numBands];
                            short i = (short) 0;
                            while (i < this.numBands) {
                                key = st.nextToken();
                                if (key.equals("band" + (i + 1) + "Level")) {
                                    this.bandLevels[i] = Short.parseShort(st.nextToken());
                                    i++;
                                } else {
                                    throw new IllegalArgumentException("invalid key name: " + key);
                                }
                            }
                            return;
                        }
                        throw new IllegalArgumentException("invalid key name: " + key);
                    }
                    throw new IllegalArgumentException("invalid key name: " + key);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("invalid value for key: " + key);
                }
            }
            throw new IllegalArgumentException("invalid settings for Equalizer: " + key);
        }

        public String toString() {
            String str = new String("Equalizer;curPreset=" + Short.toString(this.curPreset) + ";numBands=" + Short.toString(this.numBands));
            for (short i = (short) 0; i < this.numBands; i++) {
                str = str.concat(";band" + (i + 1) + "Level=" + Short.toString(this.bandLevels[i]));
            }
            return str;
        }
    }

    public Equalizer(int priority, int audioSession) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_EQUALIZER, EFFECT_TYPE_NULL, priority, audioSession);
        if (audioSession == 0) {
            Log.w(TAG, "WARNING: attaching an Equalizer to global output mix is deprecated!");
        }
        getNumberOfBands();
        this.mNumPresets = getNumberOfPresets();
        if (this.mNumPresets != 0) {
            this.mPresetNames = new String[this.mNumPresets];
            byte[] value = new byte[32];
            int[] param = new int[2];
            param[0] = 8;
            for (int i = 0; i < this.mNumPresets; i++) {
                param[1] = i;
                checkStatus(getParameter(param, value));
                int length = 0;
                while (value[length] != (byte) 0) {
                    length++;
                }
                try {
                    this.mPresetNames[i] = new String(value, 0, length, "ISO-8859-1");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "preset name decode error");
                }
            }
        }
    }

    public short getNumberOfBands() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        if (this.mNumBands != (short) 0) {
            return this.mNumBands;
        }
        short[] result = new short[1];
        checkStatus(getParameter(new int[]{0}, result));
        this.mNumBands = result[0];
        return this.mNumBands;
    }

    public short[] getBandLevelRange() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] result = new short[2];
        checkStatus(getParameter(1, result));
        return result;
    }

    public void setBandLevel(short band, short level) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        param = new int[2];
        short[] value = new short[]{2};
        param[1] = band;
        value[0] = level;
        checkStatus(setParameter(param, value));
    }

    public short getBandLevel(short band) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        param = new int[2];
        short[] result = new short[]{2};
        param[1] = band;
        checkStatus(getParameter(param, result));
        return result[0];
    }

    public int getCenterFreq(short band) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        param = new int[2];
        int[] result = new int[]{3};
        param[1] = band;
        checkStatus(getParameter(param, result));
        return result[0];
    }

    public int[] getBandFreqRange(short band) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        param = new int[2];
        int[] result = new int[]{4, band};
        checkStatus(getParameter(param, result));
        return result;
    }

    public short getBand(int frequency) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        param = new int[2];
        short[] result = new short[]{5};
        param[1] = frequency;
        checkStatus(getParameter(param, result));
        return result[0];
    }

    public short getCurrentPreset() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] result = new short[1];
        checkStatus(getParameter(6, result));
        return result[0];
    }

    public void usePreset(short preset) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(6, preset));
    }

    public short getNumberOfPresets() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] result = new short[1];
        checkStatus(getParameter(7, result));
        return result[0];
    }

    public String getPresetName(short preset) {
        if (preset < (short) 0 || preset >= this.mNumPresets) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        return this.mPresetNames[preset];
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
        byte[] param = new byte[((this.mNumBands * 2) + 4)];
        checkStatus(getParameter(9, param));
        Settings settings = new Settings();
        settings.curPreset = AudioEffect.byteArrayToShort(param, 0);
        settings.numBands = AudioEffect.byteArrayToShort(param, 2);
        settings.bandLevels = new short[this.mNumBands];
        for (short i = (short) 0; i < this.mNumBands; i++) {
            settings.bandLevels[i] = AudioEffect.byteArrayToShort(param, (i * 2) + 4);
        }
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        if (settings.numBands == settings.bandLevels.length && settings.numBands == this.mNumBands) {
            byte[] param = AudioEffect.concatArrays(AudioEffect.shortToByteArray(settings.curPreset), AudioEffect.shortToByteArray(this.mNumBands));
            for (short i = (short) 0; i < this.mNumBands; i++) {
                param = AudioEffect.concatArrays(param, AudioEffect.shortToByteArray(settings.bandLevels[i]));
            }
            checkStatus(setParameter(9, param));
            return;
        }
        throw new IllegalArgumentException("settings invalid band count: " + settings.numBands);
    }
}
