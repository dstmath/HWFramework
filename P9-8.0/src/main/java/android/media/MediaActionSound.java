package android.media;

import android.media.SoundPool.Builder;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

public class MediaActionSound {
    public static final int FOCUS_COMPLETE = 1;
    private static final int NUM_MEDIA_SOUND_STREAMS = 1;
    public static final int SHUTTER_CLICK = 0;
    private static final String[] SOUND_FILES = new String[]{"/system/media/audio/ui/camera_click.ogg", "/system/media/audio/ui/camera_focus.ogg", "/system/media/audio/ui/VideoRecord.ogg", "/system/media/audio/ui/VideoStop.ogg"};
    public static final int START_VIDEO_RECORDING = 2;
    private static final int STATE_LOADED = 3;
    private static final int STATE_LOADING = 1;
    private static final int STATE_LOADING_PLAY_REQUESTED = 2;
    private static final int STATE_NOT_LOADED = 0;
    public static final int STOP_VIDEO_RECORDING = 3;
    private static final String TAG = "MediaActionSound";
    private OnLoadCompleteListener mLoadCompleteListener = new OnLoadCompleteListener() {
        /* JADX WARNING: Missing block: B:18:0x0078, code:
            if (r1 == 0) goto L_0x0081;
     */
        /* JADX WARNING: Missing block: B:19:0x007a, code:
            r9.play(r1, 1.0f, 1.0f, 0, 0, 1.0f);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            SoundState[] -get0 = MediaActionSound.this.mSounds;
            int length = -get0.length;
            int i = 0;
            while (i < length) {
                SoundState sound = -get0[i];
                if (sound.id != sampleId) {
                    i++;
                } else {
                    int playSoundId = 0;
                    synchronized (sound) {
                        if (status != 0) {
                            sound.state = 0;
                            sound.id = 0;
                            Log.e(MediaActionSound.TAG, "OnLoadCompleteListener() error: " + status + " loading sound: " + sound.name);
                            return;
                        }
                        switch (sound.state) {
                            case 1:
                                sound.state = 3;
                                break;
                            case 2:
                                playSoundId = sound.id;
                                sound.state = 3;
                                break;
                            default:
                                Log.e(MediaActionSound.TAG, "OnLoadCompleteListener() called in wrong state: " + sound.state + " for sound: " + sound.name);
                                break;
                        }
                    }
                }
            }
        }
    };
    private SoundPool mSoundPool = new Builder().setMaxStreams(1).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setFlags(1).setContentType(4).build()).build();
    private SoundState[] mSounds;

    private class SoundState {
        public int id = 0;
        public final int name;
        public int state = 0;

        public SoundState(int name) {
            this.name = name;
        }
    }

    public MediaActionSound() {
        this.mSoundPool.setOnLoadCompleteListener(this.mLoadCompleteListener);
        this.mSounds = new SoundState[SOUND_FILES.length];
        for (int i = 0; i < this.mSounds.length; i++) {
            this.mSounds[i] = new SoundState(i);
        }
    }

    private int loadSound(SoundState sound) {
        int id = this.mSoundPool.load(SOUND_FILES[sound.name], 1);
        if (id > 0) {
            sound.state = 1;
            sound.id = id;
        }
        return id;
    }

    public void load(int soundName) {
        if (soundName < 0 || soundName >= SOUND_FILES.length) {
            throw new RuntimeException("Unknown sound requested: " + soundName);
        }
        SoundState sound = this.mSounds[soundName];
        synchronized (sound) {
            switch (sound.state) {
                case 0:
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
                case 0:
                    loadSound(sound);
                    if (loadSound(sound) <= 0) {
                        Log.e(TAG, "play() error loading sound: " + soundName);
                        break;
                    }
                    break;
                case 1:
                    break;
                case 3:
                    this.mSoundPool.play(sound.id, 1.0f, 1.0f, 0, 0, 1.0f);
                    break;
                default:
                    Log.e(TAG, "play() called in wrong state: " + sound.state + " for sound: " + soundName);
                    break;
            }
            sound.state = 2;
        }
    }

    public void release() {
        if (this.mSoundPool != null) {
            for (SoundState sound : this.mSounds) {
                synchronized (sound) {
                    sound.state = 0;
                    sound.id = 0;
                }
            }
            this.mSoundPool.release();
            this.mSoundPool = null;
        }
    }
}
