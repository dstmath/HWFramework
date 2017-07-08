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
    private static int PROPERTY_SIZE = 0;
    private static final String TAG = "EnvironmentalReverb";
    private BaseParameterListener mBaseParamListener;
    private OnParameterChangeListener mParamListener;
    private final Object mParamListenerLock;

    private class BaseParameterListener implements android.media.audiofx.AudioEffect.OnParameterChangeListener {
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
                if (param.length == EnvironmentalReverb.PARAM_REFLECTIONS_LEVEL) {
                    p = AudioEffect.byteArrayToInt(param, EnvironmentalReverb.PARAM_ROOM_LEVEL);
                }
                if (value.length == EnvironmentalReverb.PARAM_DECAY_TIME) {
                    v = AudioEffect.byteArrayToShort(value, EnvironmentalReverb.PARAM_ROOM_LEVEL);
                } else if (value.length == EnvironmentalReverb.PARAM_REFLECTIONS_LEVEL) {
                    v = AudioEffect.byteArrayToInt(value, EnvironmentalReverb.PARAM_ROOM_LEVEL);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.audiofx.EnvironmentalReverb.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.audiofx.EnvironmentalReverb.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.audiofx.EnvironmentalReverb.<clinit>():void");
    }

    public EnvironmentalReverb(int priority, int audioSession) throws IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(EFFECT_TYPE_ENV_REVERB, EFFECT_TYPE_NULL, priority, audioSession);
        this.mParamListener = null;
        this.mBaseParamListener = null;
        this.mParamListenerLock = new Object();
    }

    public void setRoomLevel(short room) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_ROOM_LEVEL, AudioEffect.shortToByteArray(room)));
    }

    public short getRoomLevel() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[PARAM_DECAY_TIME];
        checkStatus(getParameter((int) PARAM_ROOM_LEVEL, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setRoomHFLevel(short roomHF) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_ROOM_HF_LEVEL, AudioEffect.shortToByteArray(roomHF)));
    }

    public short getRoomHFLevel() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[PARAM_DECAY_TIME];
        checkStatus(getParameter((int) PARAM_ROOM_HF_LEVEL, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setDecayTime(int decayTime) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_DECAY_TIME, AudioEffect.intToByteArray(decayTime)));
    }

    public int getDecayTime() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[PARAM_REFLECTIONS_LEVEL];
        checkStatus(getParameter((int) PARAM_DECAY_TIME, param));
        return AudioEffect.byteArrayToInt(param);
    }

    public void setDecayHFRatio(short decayHFRatio) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_DECAY_HF_RATIO, AudioEffect.shortToByteArray(decayHFRatio)));
    }

    public short getDecayHFRatio() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[PARAM_DECAY_TIME];
        checkStatus(getParameter((int) PARAM_DECAY_HF_RATIO, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setReflectionsLevel(short reflectionsLevel) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_REFLECTIONS_LEVEL, AudioEffect.shortToByteArray(reflectionsLevel)));
    }

    public short getReflectionsLevel() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[PARAM_DECAY_TIME];
        checkStatus(getParameter((int) PARAM_REFLECTIONS_LEVEL, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setReflectionsDelay(int reflectionsDelay) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_REFLECTIONS_DELAY, AudioEffect.intToByteArray(reflectionsDelay)));
    }

    public int getReflectionsDelay() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[PARAM_REFLECTIONS_LEVEL];
        checkStatus(getParameter((int) PARAM_REFLECTIONS_DELAY, param));
        return AudioEffect.byteArrayToInt(param);
    }

    public void setReverbLevel(short reverbLevel) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_REVERB_LEVEL, AudioEffect.shortToByteArray(reverbLevel)));
    }

    public short getReverbLevel() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[PARAM_DECAY_TIME];
        checkStatus(getParameter((int) PARAM_REVERB_LEVEL, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setReverbDelay(int reverbDelay) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_REVERB_DELAY, AudioEffect.intToByteArray(reverbDelay)));
    }

    public int getReverbDelay() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[PARAM_REFLECTIONS_LEVEL];
        checkStatus(getParameter((int) PARAM_REVERB_DELAY, param));
        return AudioEffect.byteArrayToInt(param);
    }

    public void setDiffusion(short diffusion) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_DIFFUSION, AudioEffect.shortToByteArray(diffusion)));
    }

    public short getDiffusion() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[PARAM_DECAY_TIME];
        checkStatus(getParameter((int) PARAM_DIFFUSION, param));
        return AudioEffect.byteArrayToShort(param);
    }

    public void setDensity(short density) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter((int) PARAM_DENSITY, AudioEffect.shortToByteArray(density)));
    }

    public short getDensity() throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[] param = new byte[PARAM_DECAY_TIME];
        checkStatus(getParameter((int) PARAM_DENSITY, param));
        return AudioEffect.byteArrayToShort(param);
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
        byte[] param = new byte[PROPERTY_SIZE];
        checkStatus(getParameter((int) PARAM_PROPERTIES, param));
        Settings settings = new Settings();
        settings.roomLevel = AudioEffect.byteArrayToShort(param, PARAM_ROOM_LEVEL);
        settings.roomHFLevel = AudioEffect.byteArrayToShort(param, PARAM_DECAY_TIME);
        settings.decayTime = AudioEffect.byteArrayToInt(param, PARAM_REFLECTIONS_LEVEL);
        settings.decayHFRatio = AudioEffect.byteArrayToShort(param, PARAM_DIFFUSION);
        settings.reflectionsLevel = AudioEffect.byteArrayToShort(param, PARAM_PROPERTIES);
        settings.reflectionsDelay = AudioEffect.byteArrayToInt(param, 12);
        settings.reverbLevel = AudioEffect.byteArrayToShort(param, 16);
        settings.reverbDelay = AudioEffect.byteArrayToInt(param, 18);
        settings.diffusion = AudioEffect.byteArrayToShort(param, 22);
        settings.density = AudioEffect.byteArrayToShort(param, 24);
        return settings;
    }

    public void setProperties(Settings settings) throws IllegalStateException, IllegalArgumentException, UnsupportedOperationException {
        byte[][] bArr = new byte[PARAM_PROPERTIES][];
        bArr[PARAM_ROOM_LEVEL] = AudioEffect.shortToByteArray(settings.roomLevel);
        bArr[PARAM_ROOM_HF_LEVEL] = AudioEffect.shortToByteArray(settings.roomHFLevel);
        bArr[PARAM_DECAY_TIME] = AudioEffect.intToByteArray(settings.decayTime);
        bArr[PARAM_DECAY_HF_RATIO] = AudioEffect.shortToByteArray(settings.decayHFRatio);
        bArr[PARAM_REFLECTIONS_LEVEL] = AudioEffect.shortToByteArray(settings.reflectionsLevel);
        bArr[PARAM_REFLECTIONS_DELAY] = AudioEffect.intToByteArray(settings.reflectionsDelay);
        bArr[PARAM_REVERB_LEVEL] = AudioEffect.shortToByteArray(settings.reverbLevel);
        bArr[PARAM_REVERB_DELAY] = AudioEffect.intToByteArray(settings.reverbDelay);
        bArr[PARAM_DIFFUSION] = AudioEffect.shortToByteArray(settings.diffusion);
        bArr[PARAM_DENSITY] = AudioEffect.shortToByteArray(settings.density);
        checkStatus(setParameter((int) PARAM_PROPERTIES, AudioEffect.concatArrays(bArr)));
    }
}
