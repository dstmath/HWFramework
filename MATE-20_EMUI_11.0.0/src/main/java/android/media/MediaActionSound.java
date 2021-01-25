package android.media;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

public class MediaActionSound {
    public static final int FOCUS_COMPLETE = 1;
    private static final int NUM_MEDIA_SOUND_STREAMS = 1;
    public static final int SHUTTER_CLICK = 0;
    private static final String[] SOUND_DIRS = {"/product/media/audio/ui/", "/system/media/audio/ui/"};
    private static final String[] SOUND_FILES = {"camera_click.ogg", "camera_focus.ogg", "VideoRecord.ogg", "VideoStop.ogg"};
    public static final int START_VIDEO_RECORDING = 2;
    private static final int STATE_LOADED = 3;
    private static final int STATE_LOADING = 1;
    private static final int STATE_LOADING_PLAY_REQUESTED = 2;
    private static final int STATE_NOT_LOADED = 0;
    public static final int STOP_VIDEO_RECORDING = 3;
    private static final String TAG = "MediaActionSound";
    private SoundPool.OnLoadCompleteListener mLoadCompleteListener = new SoundPool.OnLoadCompleteListener() {
        /* class android.media.MediaActionSound.AnonymousClass1 */

        @Override // android.media.SoundPool.OnLoadCompleteListener
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            SoundState[] soundStateArr = MediaActionSound.this.mSounds;
            for (SoundState sound : soundStateArr) {
                if (sound.id == sampleId) {
                    int playSoundId = 0;
                    synchronized (sound) {
                        if (status != 0) {
                            sound.state = 0;
                            sound.id = 0;
                            Log.e(MediaActionSound.TAG, "OnLoadCompleteListener() error: " + status + " loading sound: " + sound.name);
                            return;
                        }
                        int i = sound.state;
                        if (i == 1) {
                            sound.state = 3;
                        } else if (i != 2) {
                            Log.e(MediaActionSound.TAG, "OnLoadCompleteListener() called in wrong state: " + sound.state + " for sound: " + sound.name);
                        } else {
                            playSoundId = sound.id;
                            sound.state = 3;
                        }
                    }
                    if (playSoundId != 0) {
                        soundPool.play(playSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                        return;
                    }
                    return;
                }
            }
        }
    };
    private SoundPool mSoundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setFlags(1).setContentType(4).build()).build();
    private SoundState[] mSounds;

    /* access modifiers changed from: private */
    public class SoundState {
        public int id = 0;
        public final int name;
        public int state = 0;

        public SoundState(int name2) {
            this.name = name2;
        }
    }

    public MediaActionSound() {
        this.mSoundPool.setOnLoadCompleteListener(this.mLoadCompleteListener);
        this.mSounds = new SoundState[SOUND_FILES.length];
        int i = 0;
        while (true) {
            SoundState[] soundStateArr = this.mSounds;
            if (i < soundStateArr.length) {
                soundStateArr[i] = new SoundState(i);
                i++;
            } else {
                return;
            }
        }
    }

    private int loadSound(SoundState sound) {
        String soundFileName = SOUND_FILES[sound.name];
        String[] strArr = SOUND_DIRS;
        for (String soundDir : strArr) {
            int id = this.mSoundPool.load(soundDir + soundFileName, 1);
            if (id > 0) {
                sound.state = 1;
                sound.id = id;
                return id;
            }
        }
        return 0;
    }

    public void load(int soundName) {
        if (soundName < 0 || soundName >= SOUND_FILES.length) {
            throw new RuntimeException("Unknown sound requested: " + soundName);
        }
        SoundState sound = this.mSounds[soundName];
        synchronized (sound) {
            if (sound.state != 0) {
                Log.e(TAG, "load() called in wrong state: " + sound + " for sound: " + soundName);
            } else if (loadSound(sound) <= 0) {
                Log.e(TAG, "load() error loading sound: " + soundName);
            }
        }
    }

    public void play(int soundName) {
        if (soundName < 0 || soundName >= SOUND_FILES.length) {
            throw new RuntimeException("Unknown sound requested: " + soundName);
        }
        SoundState sound = this.mSounds[soundName];
        synchronized (sound) {
            int i = sound.state;
            if (i == 0) {
                loadSound(sound);
                if (loadSound(sound) <= 0) {
                    Log.e(TAG, "play() error loading sound: " + soundName);
                }
            } else if (i != 1) {
                if (i != 3) {
                    Log.e(TAG, "play() called in wrong state: " + sound.state + " for sound: " + soundName);
                } else {
                    this.mSoundPool.play(sound.id, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }
            sound.state = 2;
        }
    }

    public void release() {
        if (this.mSoundPool != null) {
            SoundState[] soundStateArr = this.mSounds;
            for (SoundState sound : soundStateArr) {
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
