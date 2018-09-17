package android.media.audiofx;

import java.util.StringTokenizer;

public class EnvironmentalReverb extends AudioEffect {
    public static final int PARAM_DECAY_HF_RATIO = 3;
    public static final int PARAM_DECAY_TIME = 2;
    public static final int PARAM_DENSITY = 9;
    public static final int PARAM_DIFFUSION = 8;
    private static final int PARAM_PROPERTIES = 10;
    public static final int PARAM_REFLECTIONS_DELAY = 5;
    public static final int PARAM_REFLECTIONS_LEVEL = 4;
    public static final int PARAM_REVERB_DELAY = 7;
    public static final int PARAM_REVERB_LEVEL = 6;
    public static final int PARAM_ROOM_HF_LEVEL = 1;
    public static final int PARAM_ROOM_LEVEL = 0;
    private static int PROPERTY_SIZE = 26;
    private static final String TAG = "EnvironmentalReverb";
    private BaseParameterListener mBaseParamListener = null;
    private OnParameterChangeListener mParamListener = null;
    private final Object mParamListenerLock = new Object();

    private class BaseParameterListener implements android.media.audiofx.AudioEffect.OnParameterChangeListener {
        /* synthetic */ BaseParameterListener(EnvironmentalReverb this$0, BaseParameterListener -this1) {
            this();
        }

        private BaseParameterListener() {
        }

        public void onParameterChange(AudioEffect effect, int status, byte[] param, byte[] value) {
            OnParameterChangeListener l = null;
            synchronized (EnvironmentalReverb.this.mParamListenerLock) {
                if (EnvironmentalReverb.this.mParamListener != null) {
                    l = EnvironmentalReverb.this.mParamListener;
                }
            }
            if (l != null) {
                int p = -1;
                int v = -1;
                if (param.length == 4) {
                    p = AudioEffect.byteArrayToInt(param, 0);
                }
                if (value.length == 2) {
                    v = AudioEffect.byteArrayToShort(value, 0);
                } else if (value.length == 4) {
                    v = AudioEffect.byteArrayToInt(value, 0);
                }
                if (p != -1 && v != -1) {
                    l.onParameterChange(EnvironmentalReverb.this, status, p, v);
                }
            }
        }
    }

    public interface OnParameterChangeListener {
        void onParameterChange(EnvironmentalReverb environmentalReverb, int i, int i2, int i3);
    }

    public static class Settings {
        public short decayHFRatio;
        public int decayTime;
        public short density;
        public short diffusion;
        public int reflectionsDelay;
        public short reflectionsLevel;
        public int reverbDelay;
        public short reverbLevel;
        public short roomHFLevel;
        public short roomLevel;

        public Settings(String settings) {
            StringTokenizer st = new StringTokenizer(settings, "=;");
            int tokens = st.countTokens();
            if (st.countTokens() != 21) {
                throw new IllegalArgumentException("settings: " + settings);
            }
            String key = st.nextToken();
            if (key.equals(EnvironmentalReverb.TAG)) {
                try {
                    key = st.nextToken();
                    if (key.equals("roomLevel")) {
                        this.roomLevel = Short.parseShort(st.nextToken());
                        key = st.nextToken();
                        if (key.equals("roomHFLevel")) {
                            this.roomHFLevel = Short.parseShort(st.nextToken());
                            key = st.nextToken();
                            if (key.equals("decayTime")) {
                                this.decayTime = Integer.parseInt(st.nextToken());
                                key = st.nextToken();
                                if (key.equals("decayHFRatio")) {
                                    this.decayHFRatio = Short.parseShort(st.nextToken());
                                    key = st.nextToken();
                                    if (key.equals("reflectionsLevel")) {
                                        this.reflectionsLevel = Short.parseShort(st.nextToken());
                                        key = st.nextToken();
                                        if (key.equals("reflectionsDelay")) {
                                            this.reflectionsDelay = Integer.parseInt(st.nextToken());
                                            key = st.nextToken();
                                            if (key.equals("reverbLevel")) {
                                                this.reverbLevel = Short.parseShort(st.nextToken());
                                                key = st.nextToken();
                                                if (key.equals("reverbDelay")) {
                                                    this.reverbDelay = Integer.parseInt(st.nextToken());
                                                    key = st.nextToken();
                                                    if (key.equals("diffusion")) {
                                                        this.diffusion = Short.parseShort(st.nextToken());
                                                        key = st.nextToken();
                                                        if (key.equals("density")) {
                                                            this.density = Short.parseShort(st.nextToken());
                                                            return;
                                                        }
                                                        throw new IllegalArgumentException("invalid key name: " + key);
                                                    }
                                                    throw new IllegalArgumentException("invalid key name: " + key);
                                                }
                                                throw new IllegalArgumentException("invalid key name: " + key);
                                            }
                                            throw new IllegalArgumentException("invalid key name: " + key);
                                        }
                                        throw new IllegalArgumentException("invalid key name: " + key);
                                    }
                                    throw new IllegalArgumentException("invalid key name: " + key);
                                }
                                throw new IllegalArgumentException("invalid key name: " + key);
                            }
                            throw new IllegalArgumentException("invalid key name: " + key);
                        }
                        throw new IllegalArgumentException("invalid key name: " + key);
                    }
                    throw new IllegalArgumentException("invalid key name: " + key);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("invalid value for key: " + key);
                }
            }
            throw new IllegalArgumentException("invalid settings for EnvironmentalReverb: " + key);
        }

        public String toString() {
            return new String("EnvironmentalReverb;roomLevel=" + Short.toString(this.roomLevel) + ";roomHFLevel=" + Short.toString(this.roomHFLevel) + ";decayTime=" + Integer.toString(this.decayTime) + ";decayHFRatio=" + Short.toString(this.decayHFRatio) + ";reflectionsLevel=" + Short.toString(this.reflectionsLevel) + ";reflectionsDelay=" + Integer.toString(this.reflectionsDelay) + ";reverbLevel=" + Short.toString(this.reverbLevel) + ";reverbDelay=" + Integer.toString(this.reverbDelay) + ";diffusion=" + Short.toString(this.diffusion) + ";density=" + Short.toString(this.density));
        }
    }

    public EnvironmentalReverb(int priority, int audioSession) throws IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_ENV_REVERB, EFFECT_TYPE_NULL, priority, audioSession);
    }

    public void setRoomLevel(short room) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(0, AudioEffect.shortToByteArray(room)));
    }

    public short getRoomLevel() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[2];
        checkStatus(getParameter(0, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setRoomHFLevel(short roomHF) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(1, AudioEffect.shortToByteArray(roomHF)));
    }

    public short getRoomHFLevel() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[2];
        checkStatus(getParameter(1, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setDecayTime(int decayTime) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(2, AudioEffect.intToByteArray(decayTime)));
    }

    public int getDecayTime() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[4];
        checkStatus(getParameter(2, param));
        return AudioEffect.byteArrayToInt(param);
    }

    public void setDecayHFRatio(short decayHFRatio) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(3, AudioEffect.shortToByteArray(decayHFRatio)));
    }

    public short getDecayHFRatio() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[2];
        checkStatus(getParameter(3, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setReflectionsLevel(short reflectionsLevel) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(4, AudioEffect.shortToByteArray(reflectionsLevel)));
    }

    public short getReflectionsLevel() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[2];
        checkStatus(getParameter(4, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setReflectionsDelay(int reflectionsDelay) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(5, AudioEffect.intToByteArray(reflectionsDelay)));
    }

    public int getReflectionsDelay() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[4];
        checkStatus(getParameter(5, param));
        return AudioEffect.byteArrayToInt(param);
    }

    public void setReverbLevel(short reverbLevel) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(6, AudioEffect.shortToByteArray(reverbLevel)));
    }

    public short getReverbLevel() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[2];
        checkStatus(getParameter(6, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setReverbDelay(int reverbDelay) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(7, AudioEffect.intToByteArray(reverbDelay)));
    }

    public int getReverbDelay() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[4];
        checkStatus(getParameter(7, param));
        return AudioEffect.byteArrayToInt(param);
    }

    public void setDiffusion(short diffusion) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(8, AudioEffect.shortToByteArray(diffusion)));
    }

    public short getDiffusion() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[2];
        checkStatus(getParameter(8, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setDensity(short density) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(9, AudioEffect.shortToByteArray(density)));
    }

    public short getDensity() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[2];
        checkStatus(getParameter(9, param));
        return AudioEffect.byteArrayToShort(param);
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
        byte[] param = new byte[PROPERTY_SIZE];
        checkStatus(getParameter(10, param));
        Settings settings = new Settings();
        settings.roomLevel = AudioEffect.byteArrayToShort(param, 0);
        settings.roomHFLevel = AudioEffect.byteArrayToShort(param, 2);
        settings.decayTime = AudioEffect.byteArrayToInt(param, 4);
        settings.decayHFRatio = AudioEffect.byteArrayToShort(param, 8);
        settings.reflectionsLevel = AudioEffect.byteArrayToShort(param, 10);
        settings.reflectionsDelay = AudioEffect.byteArrayToInt(param, 12);
        settings.reverbLevel = AudioEffect.byteArrayToShort(param, 16);
        settings.reverbDelay = AudioEffect.byteArrayToInt(param, 18);
        settings.diffusion = AudioEffect.byteArrayToShort(param, 22);
        settings.density = AudioEffect.byteArrayToShort(param, 24);
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(10, AudioEffect.concatArrays(AudioEffect.shortToByteArray(settings.roomLevel), AudioEffect.shortToByteArray(settings.roomHFLevel), AudioEffect.intToByteArray(settings.decayTime), AudioEffect.shortToByteArray(settings.decayHFRatio), AudioEffect.shortToByteArray(settings.reflectionsLevel), AudioEffect.intToByteArray(settings.reflectionsDelay), AudioEffect.shortToByteArray(settings.reverbLevel), AudioEffect.intToByteArray(settings.reverbDelay), AudioEffect.shortToByteArray(settings.diffusion), AudioEffect.shortToByteArray(settings.density))));
    }
}
