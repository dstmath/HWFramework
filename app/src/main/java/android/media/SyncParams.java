package android.media;

import android.speech.tts.TextToSpeech.Engine;

public final class SyncParams {
    public static final int AUDIO_ADJUST_MODE_DEFAULT = 0;
    public static final int AUDIO_ADJUST_MODE_RESAMPLE = 2;
    public static final int AUDIO_ADJUST_MODE_STRETCH = 1;
    private static final int SET_AUDIO_ADJUST_MODE = 2;
    private static final int SET_FRAME_RATE = 8;
    private static final int SET_SYNC_SOURCE = 1;
    private static final int SET_TOLERANCE = 4;
    public static final int SYNC_SOURCE_AUDIO = 2;
    public static final int SYNC_SOURCE_DEFAULT = 0;
    public static final int SYNC_SOURCE_SYSTEM_CLOCK = 1;
    public static final int SYNC_SOURCE_VSYNC = 3;
    private int mAudioAdjustMode;
    private float mFrameRate;
    private int mSet;
    private int mSyncSource;
    private float mTolerance;

    public SyncParams() {
        this.mSet = SYNC_SOURCE_DEFAULT;
        this.mAudioAdjustMode = SYNC_SOURCE_DEFAULT;
        this.mSyncSource = SYNC_SOURCE_DEFAULT;
        this.mTolerance = 0.0f;
        this.mFrameRate = 0.0f;
    }

    public SyncParams allowDefaults() {
        this.mSet |= 7;
        return this;
    }

    public SyncParams setAudioAdjustMode(int audioAdjustMode) {
        this.mAudioAdjustMode = audioAdjustMode;
        this.mSet |= SYNC_SOURCE_AUDIO;
        return this;
    }

    public int getAudioAdjustMode() {
        if ((this.mSet & SYNC_SOURCE_AUDIO) != 0) {
            return this.mAudioAdjustMode;
        }
        throw new IllegalStateException("audio adjust mode not set");
    }

    public SyncParams setSyncSource(int syncSource) {
        this.mSyncSource = syncSource;
        this.mSet |= SYNC_SOURCE_SYSTEM_CLOCK;
        return this;
    }

    public int getSyncSource() {
        if ((this.mSet & SYNC_SOURCE_SYSTEM_CLOCK) != 0) {
            return this.mSyncSource;
        }
        throw new IllegalStateException("sync source not set");
    }

    public SyncParams setTolerance(float tolerance) {
        if (tolerance < 0.0f || tolerance >= Engine.DEFAULT_VOLUME) {
            throw new IllegalArgumentException("tolerance must be less than one and non-negative");
        }
        this.mTolerance = tolerance;
        this.mSet |= SET_TOLERANCE;
        return this;
    }

    public float getTolerance() {
        if ((this.mSet & SET_TOLERANCE) != 0) {
            return this.mTolerance;
        }
        throw new IllegalStateException("tolerance not set");
    }

    public SyncParams setFrameRate(float frameRate) {
        this.mFrameRate = frameRate;
        this.mSet |= SET_FRAME_RATE;
        return this;
    }

    public float getFrameRate() {
        if ((this.mSet & SET_FRAME_RATE) != 0) {
            return this.mFrameRate;
        }
        throw new IllegalStateException("frame rate not set");
    }
}
