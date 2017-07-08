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
    private BaseParameterListener mBaseParamListener;
    private short mNumBands;
    private int mNumPresets;
    private OnParameterChangeListener mParamListener;
    private final Object mParamListenerLock;
    private String[] mPresetNames;

    private class BaseParameterListener implements android.media.audiofx.AudioEffect.OnParameterChangeListener {
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
                if (param.length >= Equalizer.PARAM_BAND_FREQ_RANGE) {
                    p1 = AudioEffect.byteArrayToInt(param, Equalizer.PARAM_NUM_BANDS);
                    if (param.length >= Equalizer.PARAM_GET_PRESET_NAME) {
                        p2 = AudioEffect.byteArrayToInt(param, Equalizer.PARAM_BAND_FREQ_RANGE);
                    }
                }
                if (value.length == Equalizer.PARAM_BAND_LEVEL) {
                    v = AudioEffect.byteArrayToShort(value, Equalizer.PARAM_NUM_BANDS);
                } else if (value.length == Equalizer.PARAM_BAND_FREQ_RANGE) {
                    v = AudioEffect.byteArrayToInt(value, Equalizer.PARAM_NUM_BANDS);
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
        public short[] bandLevels;
        public short curPreset;
        public short numBands;

        public Settings() {
            this.numBands = (short) 0;
            this.bandLevels = null;
        }

        public Settings(String settings) {
            this.numBands = (short) 0;
            this.bandLevels = null;
            StringTokenizer st = new StringTokenizer(settings, "=;");
            int tokens = st.countTokens();
            if (st.countTokens() < Equalizer.PARAM_GET_BAND) {
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
                            if (st.countTokens() != this.numBands * Equalizer.PARAM_BAND_LEVEL) {
                                throw new IllegalArgumentException("settings: " + settings);
                            }
                            this.bandLevels = new short[this.numBands];
                            short i = (short) 0;
                            while (i < this.numBands) {
                                key = st.nextToken();
                                if (key.equals("band" + (i + Equalizer.PARAM_LEVEL_RANGE) + "Level")) {
                                    this.bandLevels[i] = Short.parseShort(st.nextToken());
                                    i += Equalizer.PARAM_LEVEL_RANGE;
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
            for (short i = (short) 0; i < this.numBands; i += Equalizer.PARAM_LEVEL_RANGE) {
                str = str.concat(";band" + (i + Equalizer.PARAM_LEVEL_RANGE) + "Level=" + Short.toString(this.bandLevels[i]));
            }
            return str;
        }
    }

    public Equalizer(int priority, int audioSession) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_EQUALIZER, EFFECT_TYPE_NULL, priority, audioSession);
        this.mNumBands = (short) 0;
        this.mParamListener = null;
        this.mBaseParamListener = null;
        this.mParamListenerLock = new Object();
        if (audioSession == 0) {
            Log.w(TAG, "WARNING: attaching an Equalizer to global output mix is deprecated!");
        }
        getNumberOfBands();
        this.mNumPresets = getNumberOfPresets();
        if (this.mNumPresets != 0) {
            this.mPresetNames = new String[this.mNumPresets];
            byte[] value = new byte[PARAM_STRING_SIZE_MAX];
            int[] param = new int[PARAM_BAND_LEVEL];
            param[PARAM_NUM_BANDS] = PARAM_GET_PRESET_NAME;
            for (int i = PARAM_NUM_BANDS; i < this.mNumPresets; i += PARAM_LEVEL_RANGE) {
                param[PARAM_LEVEL_RANGE] = i;
                checkStatus(getParameter(param, value));
                int length = PARAM_NUM_BANDS;
                while (value[length] != null) {
                    length += PARAM_LEVEL_RANGE;
                }
                try {
                    this.mPresetNames[i] = new String(value, PARAM_NUM_BANDS, length, "ISO-8859-1");
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
        int[] param = new int[PARAM_LEVEL_RANGE];
        param[PARAM_NUM_BANDS] = PARAM_NUM_BANDS;
        short[] result = new short[PARAM_LEVEL_RANGE];
        checkStatus(getParameter(param, result));
        this.mNumBands = result[PARAM_NUM_BANDS];
        return this.mNumBands;
    }

    public short[] getBandLevelRange() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] result = new short[PARAM_BAND_LEVEL];
        checkStatus(getParameter((int) PARAM_LEVEL_RANGE, result));
        return result;
    }

    public void setBandLevel(short band, short level) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        int[] param = new int[PARAM_BAND_LEVEL];
        short[] value = new short[PARAM_LEVEL_RANGE];
        param[PARAM_NUM_BANDS] = PARAM_BAND_LEVEL;
        param[PARAM_LEVEL_RANGE] = band;
        value[PARAM_NUM_BANDS] = level;
        checkStatus(setParameter(param, value));
    }

    public short getBandLevel(short band) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        int[] param = new int[PARAM_BAND_LEVEL];
        short[] result = new short[PARAM_LEVEL_RANGE];
        param[PARAM_NUM_BANDS] = PARAM_BAND_LEVEL;
        param[PARAM_LEVEL_RANGE] = band;
        checkStatus(getParameter(param, result));
        return result[PARAM_NUM_BANDS];
    }

    public int getCenterFreq(short band) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        int[] param = new int[PARAM_BAND_LEVEL];
        int[] result = new int[PARAM_LEVEL_RANGE];
        param[PARAM_NUM_BANDS] = PARAM_CENTER_FREQ;
        param[PARAM_LEVEL_RANGE] = band;
        checkStatus(getParameter(param, result));
        return result[PARAM_NUM_BANDS];
    }

    public int[] getBandFreqRange(short band) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        int[] param = new int[PARAM_BAND_LEVEL];
        int[] result = new int[PARAM_BAND_LEVEL];
        param[PARAM_NUM_BANDS] = PARAM_BAND_FREQ_RANGE;
        param[PARAM_LEVEL_RANGE] = band;
        checkStatus(getParameter(param, result));
        return result;
    }

    public short getBand(int frequency) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        int[] param = new int[PARAM_BAND_LEVEL];
        short[] result = new short[PARAM_LEVEL_RANGE];
        param[PARAM_NUM_BANDS] = PARAM_GET_BAND;
        param[PARAM_LEVEL_RANGE] = frequency;
        checkStatus(getParameter(param, result));
        return result[PARAM_NUM_BANDS];
    }

    public short getCurrentPreset() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] result = new short[PARAM_LEVEL_RANGE];
        checkStatus(getParameter((int) PARAM_CURRENT_PRESET, result));
        return result[PARAM_NUM_BANDS];
    }

    public void usePreset(short preset) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_CURRENT_PRESET, preset));
    }

    public short getNumberOfPresets() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        short[] result = new short[PARAM_LEVEL_RANGE];
        checkStatus(getParameter((int) PARAM_GET_NUM_OF_PRESETS, result));
        return result[PARAM_NUM_BANDS];
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
                this.mBaseParamListener = new BaseParameterListener();
                super.setParameterListener(this.mBaseParamListener);
            }
        }
    }

    public Settings getProperties() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[((this.mNumBands * PARAM_BAND_LEVEL) + PARAM_BAND_FREQ_RANGE)];
        checkStatus(getParameter((int) PARAM_PROPERTIES, param));
        Settings settings = new Settings();
        settings.curPreset = AudioEffect.byteArrayToShort(param, PARAM_NUM_BANDS);
        settings.numBands = AudioEffect.byteArrayToShort(param, PARAM_BAND_LEVEL);
        settings.bandLevels = new short[this.mNumBands];
        for (short i = (short) 0; i < this.mNumBands; i += PARAM_LEVEL_RANGE) {
            settings.bandLevels[i] = AudioEffect.byteArrayToShort(param, (i * PARAM_BAND_LEVEL) + PARAM_BAND_FREQ_RANGE);
        }
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        if (settings.numBands == settings.bandLevels.length && settings.numBands == this.mNumBands) {
            byte[][] bArr = new byte[PARAM_BAND_LEVEL][];
            bArr[PARAM_NUM_BANDS] = AudioEffect.shortToByteArray(settings.curPreset);
            bArr[PARAM_LEVEL_RANGE] = AudioEffect.shortToByteArray(this.mNumBands);
            byte[] param = AudioEffect.concatArrays(bArr);
            for (short i = (short) 0; i < this.mNumBands; i += PARAM_LEVEL_RANGE) {
                bArr = new byte[PARAM_BAND_LEVEL][];
                bArr[PARAM_NUM_BANDS] = param;
                bArr[PARAM_LEVEL_RANGE] = AudioEffect.shortToByteArray(settings.bandLevels[i]);
                param = AudioEffect.concatArrays(bArr);
            }
            checkStatus(setParameter((int) PARAM_PROPERTIES, param));
            return;
        }
        throw new IllegalArgumentException("settings invalid band count: " + settings.numBands);
    }
}
