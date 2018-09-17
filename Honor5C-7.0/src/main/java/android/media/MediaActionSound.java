package android.media;

import android.media.SoundPool.Builder;
import android.media.SoundPool.OnLoadCompleteListener;
import android.speech.tts.TextToSpeech.Engine;
import android.util.Log;

public class MediaActionSound {
    public static final int FOCUS_COMPLETE = 1;
    private static final int NUM_MEDIA_SOUND_STREAMS = 1;
    public static final int SHUTTER_CLICK = 0;
    private static final String[] SOUND_FILES = null;
    public static final int START_VIDEO_RECORDING = 2;
    private static final int STATE_LOADED = 3;
    private static final int STATE_LOADING = 1;
    private static final int STATE_LOADING_PLAY_REQUESTED = 2;
    private static final int STATE_NOT_LOADED = 0;
    public static final int STOP_VIDEO_RECORDING = 3;
    private static final String TAG = "MediaActionSound";
    private OnLoadCompleteListener mLoadCompleteListener;
    private SoundPool mSoundPool;
    private SoundState[] mSounds;

    private class SoundState {
        public int id;
        public final int name;
        public int state;

        public SoundState(int name) {
            this.name = name;
            this.id = MediaActionSound.STATE_NOT_LOADED;
            this.state = MediaActionSound.STATE_NOT_LOADED;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaActionSound.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaActionSound.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaActionSound.<clinit>():void");
    }

    public MediaActionSound() {
        this.mLoadCompleteListener = new OnLoadCompleteListener() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                SoundState[] -get0 = MediaActionSound.this.mSounds;
                int length = -get0.length;
                int i = MediaActionSound.STATE_NOT_LOADED;
                while (i < length) {
                    SoundState sound = -get0[i];
                    if (sound.id != sampleId) {
                        i += MediaActionSound.STATE_LOADING;
                    } else {
                        int playSoundId = MediaActionSound.STATE_NOT_LOADED;
                        synchronized (sound) {
                            if (status == 0) {
                                switch (sound.state) {
                                    case MediaActionSound.STATE_LOADING /*1*/:
                                        sound.state = MediaActionSound.STOP_VIDEO_RECORDING;
                                        break;
                                    case MediaActionSound.STATE_LOADING_PLAY_REQUESTED /*2*/:
                                        playSoundId = sound.id;
                                        sound.state = MediaActionSound.STOP_VIDEO_RECORDING;
                                        break;
                                    default:
                                        Log.e(MediaActionSound.TAG, "OnLoadCompleteListener() called in wrong state: " + sound.state + " for sound: " + sound.name);
                                        break;
                                }
                            }
                            sound.state = MediaActionSound.STATE_NOT_LOADED;
                            sound.id = MediaActionSound.STATE_NOT_LOADED;
                            Log.e(MediaActionSound.TAG, "OnLoadCompleteListener() error: " + status + " loading sound: " + sound.name);
                            return;
                        }
                    }
                }
            }
        };
        this.mSoundPool = new Builder().setMaxStreams(STATE_LOADING).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setFlags(STATE_LOADING).setContentType(4).build()).build();
        this.mSoundPool.setOnLoadCompleteListener(this.mLoadCompleteListener);
        this.mSounds = new SoundState[SOUND_FILES.length];
        for (int i = STATE_NOT_LOADED; i < this.mSounds.length; i += STATE_LOADING) {
            this.mSounds[i] = new SoundState(i);
        }
    }

    private int loadSound(SoundState sound) {
        int id = this.mSoundPool.load(SOUND_FILES[sound.name], (int) STATE_LOADING);
        if (id > 0) {
            sound.state = STATE_LOADING;
            sound.id = id;
        }
        return id;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void load(int soundName) {
        if (soundName < 0 || soundName >= SOUND_FILES.length) {
            throw new RuntimeException("Unknown sound requested: " + soundName);
        }
        SoundState sound = this.mSounds[soundName];
        synchronized (sound) {
            switch (sound.state) {
                case STATE_NOT_LOADED /*0*/:
                    if (loadSound(sound) <= 0) {
                        Log.e(TAG, "load() error loading sound: " + soundName);
                        break;
                    }
                    break;
                default:
                    Log.e(TAG, "load() called in wrong state: " + sound + " for sound: " + soundName);
                    break;
            }
        }
    }

    public void play(int soundName) {
        if (soundName < 0 || soundName >= SOUND_FILES.length) {
            throw new RuntimeException("Unknown sound requested: " + soundName);
        }
        SoundState sound = this.mSounds[soundName];
        synchronized (sound) {
            switch (sound.state) {
                case STATE_NOT_LOADED /*0*/:
                    loadSound(sound);
                    if (loadSound(sound) <= 0) {
                        Log.e(TAG, "play() error loading sound: " + soundName);
                        break;
                    }
                    break;
                case STATE_LOADING /*1*/:
                    break;
                case STOP_VIDEO_RECORDING /*3*/:
                    this.mSoundPool.play(sound.id, Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME, STATE_NOT_LOADED, STATE_NOT_LOADED, Engine.DEFAULT_VOLUME);
                    break;
                default:
                    Log.e(TAG, "play() called in wrong state: " + sound.state + " for sound: " + soundName);
                    break;
            }
            sound.state = STATE_LOADING_PLAY_REQUESTED;
        }
    }

    public void release() {
        if (this.mSoundPool != null) {
            SoundState[] soundStateArr = this.mSounds;
            int length = soundStateArr.length;
            for (int i = STATE_NOT_LOADED; i < length; i += STATE_LOADING) {
                SoundState sound = soundStateArr[i];
                synchronized (sound) {
                    sound.state = STATE_NOT_LOADED;
                    sound.id = STATE_NOT_LOADED;
                }
            }
            this.mSoundPool.release();
            this.mSoundPool = null;
        }
    }
}
