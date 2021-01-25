package android.support.v4.media;

import android.annotation.TargetApi;
import android.media.DeniedByServerException;
import android.media.MediaDrm;
import android.media.MediaDrmException;
import android.media.MediaFormat;
import android.media.ResourceBusyException;
import android.media.TimedMetaData;
import android.media.UnsupportedSchemeException;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.view.Surface;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

@TargetApi(28)
public abstract class MediaPlayer2 {
    public static final int CALL_COMPLETED_ATTACH_AUX_EFFECT = 1;
    public static final int CALL_COMPLETED_DESELECT_TRACK = 2;
    public static final int CALL_COMPLETED_LOOP_CURRENT = 3;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static final int CALL_COMPLETED_NOTIFY_WHEN_COMMAND_LABEL_REACHED = 1003;
    public static final int CALL_COMPLETED_PAUSE = 4;
    public static final int CALL_COMPLETED_PLAY = 5;
    public static final int CALL_COMPLETED_PREPARE = 6;
    public static final int CALL_COMPLETED_SEEK_TO = 14;
    public static final int CALL_COMPLETED_SELECT_TRACK = 15;
    public static final int CALL_COMPLETED_SET_AUDIO_ATTRIBUTES = 16;
    public static final int CALL_COMPLETED_SET_AUDIO_SESSION_ID = 17;
    public static final int CALL_COMPLETED_SET_AUX_EFFECT_SEND_LEVEL = 18;
    public static final int CALL_COMPLETED_SET_DATA_SOURCE = 19;
    public static final int CALL_COMPLETED_SET_NEXT_DATA_SOURCE = 22;
    public static final int CALL_COMPLETED_SET_NEXT_DATA_SOURCES = 23;
    public static final int CALL_COMPLETED_SET_PLAYBACK_PARAMS = 24;
    public static final int CALL_COMPLETED_SET_PLAYER_VOLUME = 26;
    public static final int CALL_COMPLETED_SET_SURFACE = 27;
    public static final int CALL_COMPLETED_SKIP_TO_NEXT = 29;
    public static final int CALL_STATUS_BAD_VALUE = 2;
    public static final int CALL_STATUS_ERROR_IO = 4;
    public static final int CALL_STATUS_ERROR_UNKNOWN = Integer.MIN_VALUE;
    public static final int CALL_STATUS_INVALID_OPERATION = 1;
    public static final int CALL_STATUS_NO_ERROR = 0;
    public static final int CALL_STATUS_PERMISSION_DENIED = 3;
    public static final int MEDIAPLAYER2_STATE_ERROR = 1005;
    public static final int MEDIAPLAYER2_STATE_IDLE = 1001;
    public static final int MEDIAPLAYER2_STATE_PAUSED = 1003;
    public static final int MEDIAPLAYER2_STATE_PLAYING = 1004;
    public static final int MEDIAPLAYER2_STATE_PREPARED = 1002;
    public static final int MEDIA_ERROR_IO = -1004;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static final int MEDIA_ERROR_SYSTEM = Integer.MIN_VALUE;
    public static final int MEDIA_ERROR_TIMED_OUT = -110;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    public static final int MEDIA_INFO_AUDIO_NOT_PLAYING = 804;
    public static final int MEDIA_INFO_AUDIO_RENDERING_START = 4;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_BUFFERING_UPDATE = 704;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static final int MEDIA_INFO_EXTERNAL_METADATA_UPDATE = 803;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static final int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_PLAYBACK_COMPLETE = 5;
    public static final int MEDIA_INFO_PLAYLIST_END = 6;
    public static final int MEDIA_INFO_PREPARED = 100;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    public static final int MEDIA_INFO_VIDEO_NOT_PLAYING = 805;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    public static final int PREPARE_DRM_STATUS_PREPARATION_ERROR = 3;
    public static final int PREPARE_DRM_STATUS_PROVISIONING_NETWORK_ERROR = 1;
    public static final int PREPARE_DRM_STATUS_PROVISIONING_SERVER_ERROR = 2;
    public static final int PREPARE_DRM_STATUS_SUCCESS = 0;
    public static final int SEEK_CLOSEST = 3;
    public static final int SEEK_CLOSEST_SYNC = 2;
    public static final int SEEK_NEXT_SYNC = 1;
    public static final int SEEK_PREVIOUS_SYNC = 0;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface CallCompleted {
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface CallStatus {
    }

    public static abstract class DrmInfo {
        public abstract Map<UUID, byte[]> getPssh();

        public abstract List<UUID> getSupportedSchemes();
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface MediaError {
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface MediaInfo {
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface MediaPlayer2State {
    }

    public interface OnDrmConfigHelper {
        void onDrmConfig(MediaPlayer2 mediaPlayer2, DataSourceDesc dataSourceDesc);
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface PrepareDrmStatusCode {
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface SeekMode {
    }

    public static abstract class TrackInfo {
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_METADATA = 5;
        public static final int MEDIA_TRACK_TYPE_SUBTITLE = 4;
        @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;

        public abstract MediaFormat getFormat();

        public abstract String getLanguage();

        public abstract int getTrackType();

        public abstract String toString();
    }

    public abstract void attachAuxEffect(int i);

    public abstract void clearDrmEventCallback();

    public abstract void clearEventCallback();

    public abstract void clearPendingCommands();

    public abstract void close();

    public abstract void deselectTrack(int i);

    @Nullable
    public abstract AudioAttributesCompat getAudioAttributes();

    public abstract int getAudioSessionId();

    public abstract BaseMediaPlayer getBaseMediaPlayer();

    public abstract long getBufferedPosition();

    @NonNull
    public abstract DataSourceDesc getCurrentDataSource();

    public abstract long getCurrentPosition();

    public abstract DrmInfo getDrmInfo();

    @NonNull
    public abstract MediaDrm.KeyRequest getDrmKeyRequest(@Nullable byte[] bArr, @Nullable byte[] bArr2, @Nullable String str, int i, @Nullable Map<String, String> map) throws NoDrmSchemeException;

    @NonNull
    public abstract String getDrmPropertyString(@NonNull String str) throws NoDrmSchemeException;

    public abstract long getDuration();

    public abstract PersistableBundle getMetrics();

    @NonNull
    public abstract PlaybackParams2 getPlaybackParams();

    public abstract float getPlayerVolume();

    public abstract int getSelectedTrack(int i);

    public abstract int getState();

    @Nullable
    public abstract MediaTimestamp2 getTimestamp();

    public abstract List<TrackInfo> getTrackInfo();

    public abstract int getVideoHeight();

    public abstract int getVideoWidth();

    public abstract void loopCurrent(boolean z);

    public abstract void pause();

    public abstract void play();

    public abstract void prepare();

    public abstract void prepareDrm(@NonNull UUID uuid) throws UnsupportedSchemeException, ResourceBusyException, ProvisioningNetworkErrorException, ProvisioningServerErrorException;

    public abstract byte[] provideDrmKeyResponse(@Nullable byte[] bArr, @NonNull byte[] bArr2) throws NoDrmSchemeException, DeniedByServerException;

    public abstract void releaseDrm() throws NoDrmSchemeException;

    public abstract void reset();

    public abstract void restoreDrmKeys(@NonNull byte[] bArr) throws NoDrmSchemeException;

    public abstract void seekTo(long j, int i);

    public abstract void selectTrack(int i);

    public abstract void setAudioAttributes(@NonNull AudioAttributesCompat audioAttributesCompat);

    public abstract void setAudioSessionId(int i);

    public abstract void setAuxEffectSendLevel(float f);

    public abstract void setDataSource(@NonNull DataSourceDesc dataSourceDesc);

    public abstract void setDrmEventCallback(@NonNull Executor executor, @NonNull DrmEventCallback drmEventCallback);

    public abstract void setDrmPropertyString(@NonNull String str, @NonNull String str2) throws NoDrmSchemeException;

    public abstract void setEventCallback(@NonNull Executor executor, @NonNull EventCallback eventCallback);

    public abstract void setNextDataSource(@NonNull DataSourceDesc dataSourceDesc);

    public abstract void setNextDataSources(@NonNull List<DataSourceDesc> list);

    public abstract void setOnDrmConfigHelper(OnDrmConfigHelper onDrmConfigHelper);

    public abstract void setPlaybackParams(@NonNull PlaybackParams2 playbackParams2);

    public abstract void setPlayerVolume(float f);

    public abstract void setSurface(Surface surface);

    public abstract void skipToNext();

    public static final MediaPlayer2 create() {
        return new MediaPlayer2Impl();
    }

    public void seekTo(long msec) {
        seekTo(msec, 0);
    }

    public float getMaxPlayerVolume() {
        return 1.0f;
    }

    public void notifyWhenCommandLabelReached(@NonNull Object label) {
    }

    public static abstract class EventCallback {
        public void onVideoSizeChanged(MediaPlayer2 mp, DataSourceDesc dsd, int width, int height) {
        }

        public void onTimedMetaDataAvailable(MediaPlayer2 mp, DataSourceDesc dsd, TimedMetaData data) {
        }

        public void onError(MediaPlayer2 mp, DataSourceDesc dsd, int what, int extra) {
        }

        public void onInfo(MediaPlayer2 mp, DataSourceDesc dsd, int what, int extra) {
        }

        public void onCallCompleted(MediaPlayer2 mp, DataSourceDesc dsd, int what, int status) {
        }

        public void onMediaTimeDiscontinuity(MediaPlayer2 mp, DataSourceDesc dsd, MediaTimestamp2 timestamp) {
        }

        public void onCommandLabelReached(MediaPlayer2 mp, @NonNull Object label) {
        }

        public void onSubtitleData(MediaPlayer2 mp, DataSourceDesc dsd, @NonNull SubtitleData2 data) {
        }
    }

    public static abstract class DrmEventCallback {
        public void onDrmInfo(MediaPlayer2 mp, DataSourceDesc dsd, DrmInfo drmInfo) {
        }

        public void onDrmPrepared(MediaPlayer2 mp, DataSourceDesc dsd, int status) {
        }
    }

    public static class NoDrmSchemeException extends MediaDrmException {
        public NoDrmSchemeException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static class ProvisioningNetworkErrorException extends MediaDrmException {
        public ProvisioningNetworkErrorException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static class ProvisioningServerErrorException extends MediaDrmException {
        public ProvisioningServerErrorException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static final class MetricsConstants {
        public static final String CODEC_AUDIO = "android.media.mediaplayer.audio.codec";
        public static final String CODEC_VIDEO = "android.media.mediaplayer.video.codec";
        public static final String DURATION = "android.media.mediaplayer.durationMs";
        public static final String ERRORS = "android.media.mediaplayer.err";
        public static final String ERROR_CODE = "android.media.mediaplayer.errcode";
        public static final String FRAMES = "android.media.mediaplayer.frames";
        public static final String FRAMES_DROPPED = "android.media.mediaplayer.dropped";
        public static final String HEIGHT = "android.media.mediaplayer.height";
        public static final String MIME_TYPE_AUDIO = "android.media.mediaplayer.audio.mime";
        public static final String MIME_TYPE_VIDEO = "android.media.mediaplayer.video.mime";
        public static final String PLAYING = "android.media.mediaplayer.playingMs";
        public static final String WIDTH = "android.media.mediaplayer.width";

        private MetricsConstants() {
        }
    }
}
