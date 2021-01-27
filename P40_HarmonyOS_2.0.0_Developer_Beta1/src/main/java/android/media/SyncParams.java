package android.media;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
    private int mAudioAdjustMode = 0;
    private float mFrameRate = 0.0f;
    private int mSet = 0;
    private int mSyncSource = 0;
    private float mTolerance = 0.0f;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AudioAdjustMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncSource {
    }

    public SyncParams allowDefaults() {
        this.mSet |= 7;
        return this;
    }

    public SyncParams setAudioAdjustMode(int audioAdjustMode) {
        this.mAudioAdjustMode = audioAdjustMode;
        this.mSet |= 2;
        return this;
    }

    public int getAudioAdjustMode() {
        if ((this.mSet & 2) != 0) {
            return this.mAudioAdjustMode;
        }
        throw new IllegalStateException("audio adjust mode not set");
    }

    public SyncParams setSyncSource(int syncSource) {
        this.mSyncSource = syncSource;
        this.mSet |= 1;
        return this;
    }

    public int getSyncSource() {
        if ((this.mSet & 1) != 0) {
            return this.mSyncSource;
        }
        throw new IllegalStateException("sync source not set");
    }

    public SyncParams setTolerance(float tolerance) {
        if (tolerance < 0.0f || tolerance >= 1.0f) {
            throw new IllegalArgumentException("tolerance must be less than one and non-negative");
        }
        this.mTolerance = tolerance;
        this.mSet |= 4;
        return this;
    }

    public float getTolerance() {
        if ((this.mSet & 4) != 0) {
            return this.mTolerance;
        }
        throw new IllegalStateException("tolerance not set");
    }

    public SyncParams setFrameRate(float frameRate) {
        this.mFrameRate = frameRate;
        this.mSet |= 8;
        return this;
    }

    public float getFrameRate() {
        if ((this.mSet & 8) != 0) {
            return this.mFrameRate;
        }
        throw new IllegalStateException("frame rate not set");
    }
}
