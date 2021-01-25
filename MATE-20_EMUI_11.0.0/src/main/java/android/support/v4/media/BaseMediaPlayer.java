package android.support.v4.media;

import android.annotation.TargetApi;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;

@TargetApi(19)
public abstract class BaseMediaPlayer implements AutoCloseable {
    public static final int BUFFERING_STATE_BUFFERING_AND_PLAYABLE = 1;
    public static final int BUFFERING_STATE_BUFFERING_AND_STARVED = 2;
    public static final int BUFFERING_STATE_BUFFERING_COMPLETE = 3;
    public static final int BUFFERING_STATE_UNKNOWN = 0;
    public static final int PLAYER_STATE_ERROR = 3;
    public static final int PLAYER_STATE_IDLE = 0;
    public static final int PLAYER_STATE_PAUSED = 1;
    public static final int PLAYER_STATE_PLAYING = 2;
    public static final long UNKNOWN_TIME = -1;

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface BuffState {
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface PlayerState {
    }

    @Nullable
    public abstract AudioAttributesCompat getAudioAttributes();

    public abstract int getBufferingState();

    @Nullable
    public abstract DataSourceDesc getCurrentDataSource();

    public abstract int getPlayerState();

    public abstract float getPlayerVolume();

    public abstract void loopCurrent(boolean z);

    public abstract void pause();

    public abstract void play();

    public abstract void prepare();

    public abstract void registerPlayerEventCallback(@NonNull Executor executor, @NonNull PlayerEventCallback playerEventCallback);

    public abstract void reset();

    public abstract void seekTo(long j);

    public abstract void setAudioAttributes(@NonNull AudioAttributesCompat audioAttributesCompat);

    public abstract void setDataSource(@NonNull DataSourceDesc dataSourceDesc);

    public abstract void setNextDataSource(@NonNull DataSourceDesc dataSourceDesc);

    public abstract void setNextDataSources(@NonNull List<DataSourceDesc> list);

    public abstract void setPlaybackSpeed(float f);

    public abstract void setPlayerVolume(float f);

    public abstract void skipToNext();

    public abstract void unregisterPlayerEventCallback(@NonNull PlayerEventCallback playerEventCallback);

    public long getCurrentPosition() {
        return -1;
    }

    public long getDuration() {
        return -1;
    }

    public long getBufferedPosition() {
        return -1;
    }

    public float getPlaybackSpeed() {
        return 1.0f;
    }

    public boolean isReversePlaybackSupported() {
        return false;
    }

    public float getMaxPlayerVolume() {
        return 1.0f;
    }

    public static abstract class PlayerEventCallback {
        public void onCurrentDataSourceChanged(@NonNull BaseMediaPlayer mpb, @Nullable DataSourceDesc dsd) {
        }

        public void onMediaPrepared(@NonNull BaseMediaPlayer mpb, @NonNull DataSourceDesc dsd) {
        }

        public void onPlayerStateChanged(@NonNull BaseMediaPlayer mpb, int state) {
        }

        public void onBufferingStateChanged(@NonNull BaseMediaPlayer mpb, @NonNull DataSourceDesc dsd, int state) {
        }

        public void onPlaybackSpeedChanged(@NonNull BaseMediaPlayer mpb, float speed) {
        }

        public void onSeekCompleted(@NonNull BaseMediaPlayer mpb, long position) {
        }
    }
}
