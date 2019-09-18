package android.media;

import android.app.DownloadManager;
import android.app.slice.Slice;
import android.content.Context;
import android.media.AudioRouting;
import android.media.BufferingParams;
import android.media.MediaDrm;
import android.media.MediaPlayerBase;
import android.media.SubtitleController;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

public abstract class MediaPlayer2 extends MediaPlayerBase implements SubtitleController.Listener, AudioRouting {
    public static final boolean APPLY_METADATA_FILTER = true;
    public static final boolean BYPASS_METADATA_FILTER = false;
    public static final int CALL_COMPLETED_ATTACH_AUX_EFFECT = 1;
    public static final int CALL_COMPLETED_DESELECT_TRACK = 2;
    public static final int CALL_COMPLETED_LOOP_CURRENT = 3;
    public static final int CALL_COMPLETED_NOTIFY_WHEN_COMMAND_LABEL_REACHED = 1003;
    public static final int CALL_COMPLETED_PAUSE = 4;
    public static final int CALL_COMPLETED_PLAY = 5;
    public static final int CALL_COMPLETED_PREPARE = 6;
    public static final int CALL_COMPLETED_RELEASE_DRM = 12;
    public static final int CALL_COMPLETED_RESTORE_DRM_KEYS = 13;
    public static final int CALL_COMPLETED_SEEK_TO = 14;
    public static final int CALL_COMPLETED_SELECT_TRACK = 15;
    public static final int CALL_COMPLETED_SET_AUDIO_ATTRIBUTES = 16;
    public static final int CALL_COMPLETED_SET_AUDIO_SESSION_ID = 17;
    public static final int CALL_COMPLETED_SET_AUX_EFFECT_SEND_LEVEL = 18;
    public static final int CALL_COMPLETED_SET_BUFFERING_PARAMS = 1001;
    public static final int CALL_COMPLETED_SET_DATA_SOURCE = 19;
    public static final int CALL_COMPLETED_SET_NEXT_DATA_SOURCE = 22;
    public static final int CALL_COMPLETED_SET_NEXT_DATA_SOURCES = 23;
    public static final int CALL_COMPLETED_SET_PLAYBACK_PARAMS = 24;
    public static final int CALL_COMPLETED_SET_PLAYBACK_SPEED = 25;
    public static final int CALL_COMPLETED_SET_PLAYER_VOLUME = 26;
    public static final int CALL_COMPLETED_SET_SURFACE = 27;
    public static final int CALL_COMPLETED_SET_SYNC_PARAMS = 28;
    public static final int CALL_COMPLETED_SET_VIDEO_SCALING_MODE = 1002;
    public static final int CALL_COMPLETED_SKIP_TO_NEXT = 29;
    public static final int CALL_STATUS_BAD_VALUE = 2;
    public static final int CALL_STATUS_ERROR_IO = 4;
    public static final int CALL_STATUS_ERROR_UNKNOWN = Integer.MIN_VALUE;
    public static final int CALL_STATUS_INVALID_OPERATION = 1;
    public static final int CALL_STATUS_NO_DRM_SCHEME = 5;
    public static final int CALL_STATUS_NO_ERROR = 0;
    public static final int CALL_STATUS_PERMISSION_DENIED = 3;
    public static final int MEDIAPLAYER2_STATE_ERROR = 5;
    public static final int MEDIAPLAYER2_STATE_IDLE = 1;
    public static final int MEDIAPLAYER2_STATE_PAUSED = 3;
    public static final int MEDIAPLAYER2_STATE_PLAYING = 4;
    public static final int MEDIAPLAYER2_STATE_PREPARED = 2;
    public static final int MEDIA_ERROR_IO = -1004;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
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
    public static final int MEDIA_INFO_EXTERNAL_METADATA_UPDATE = 803;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_PLAYBACK_COMPLETE = 5;
    public static final int MEDIA_INFO_PLAYLIST_END = 6;
    public static final int MEDIA_INFO_PREPARED = 100;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    public static final int MEDIA_INFO_VIDEO_NOT_PLAYING = 805;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    public static final String MEDIA_MIMETYPE_TEXT_CEA_608 = "text/cea-608";
    public static final String MEDIA_MIMETYPE_TEXT_CEA_708 = "text/cea-708";
    public static final String MEDIA_MIMETYPE_TEXT_SUBRIP = "application/x-subrip";
    public static final String MEDIA_MIMETYPE_TEXT_VTT = "text/vtt";
    public static final boolean METADATA_ALL = false;
    public static final boolean METADATA_UPDATE_ONLY = true;
    public static final int PLAYBACK_RATE_AUDIO_MODE_DEFAULT = 0;
    public static final int PLAYBACK_RATE_AUDIO_MODE_RESAMPLE = 2;
    public static final int PLAYBACK_RATE_AUDIO_MODE_STRETCH = 1;
    public static final int PREPARE_DRM_STATUS_PREPARATION_ERROR = 3;
    public static final int PREPARE_DRM_STATUS_PROVISIONING_NETWORK_ERROR = 1;
    public static final int PREPARE_DRM_STATUS_PROVISIONING_SERVER_ERROR = 2;
    public static final int PREPARE_DRM_STATUS_SUCCESS = 0;
    public static final int SEEK_CLOSEST = 3;
    public static final int SEEK_CLOSEST_SYNC = 2;
    public static final int SEEK_NEXT_SYNC = 1;
    public static final int SEEK_PREVIOUS_SYNC = 0;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CallCompleted {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CallStatus {
    }

    public static abstract class DrmEventCallback {
        public void onDrmInfo(MediaPlayer2 mp, DataSourceDesc dsd, DrmInfo drmInfo) {
        }

        public void onDrmPrepared(MediaPlayer2 mp, DataSourceDesc dsd, int status) {
        }
    }

    public static abstract class DrmInfo {
        public abstract Map<UUID, byte[]> getPssh();

        public abstract List<UUID> getSupportedSchemes();
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MediaError {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MediaInfo {
    }

    public static abstract class MediaPlayer2EventCallback {
        public void onVideoSizeChanged(MediaPlayer2 mp, DataSourceDesc dsd, int width, int height) {
        }

        public void onTimedText(MediaPlayer2 mp, DataSourceDesc dsd, TimedText text) {
        }

        public void onTimedMetaDataAvailable(MediaPlayer2 mp, DataSourceDesc dsd, TimedMetaData data) {
        }

        public void onError(MediaPlayer2 mp, DataSourceDesc dsd, int what, int extra) {
        }

        public void onInfo(MediaPlayer2 mp, DataSourceDesc dsd, int what, int extra) {
        }

        public void onCallCompleted(MediaPlayer2 mp, DataSourceDesc dsd, int what, int status) {
        }

        public void onMediaTimeChanged(MediaPlayer2 mp, DataSourceDesc dsd, MediaTimestamp timestamp) {
        }

        public void onCommandLabelReached(MediaPlayer2 mp, Object label) {
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MediaPlayer2State {
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

    public static abstract class NoDrmSchemeException extends MediaDrmException {
        protected NoDrmSchemeException(String detailMessage) {
            super(detailMessage);
        }
    }

    public interface OnDrmConfigHelper {
        void onDrmConfig(MediaPlayer2 mediaPlayer2, DataSourceDesc dataSourceDesc);
    }

    public interface OnSubtitleDataListener {
        void onSubtitleData(MediaPlayer2 mediaPlayer2, SubtitleData subtitleData);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PlaybackRateAudioMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PrepareDrmStatusCode {
    }

    public static abstract class ProvisioningNetworkErrorException extends MediaDrmException {
        protected ProvisioningNetworkErrorException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static abstract class ProvisioningServerErrorException extends MediaDrmException {
        protected ProvisioningServerErrorException(String detailMessage) {
            super(detailMessage);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SeekMode {
    }

    public static abstract class TrackInfo {
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_METADATA = 5;
        public static final int MEDIA_TRACK_TYPE_SUBTITLE = 4;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;

        public abstract MediaFormat getFormat();

        public abstract String getLanguage();

        public abstract int getTrackType();

        public abstract String toString();
    }

    public abstract void addOnRoutingChangedListener(AudioRouting.OnRoutingChangedListener onRoutingChangedListener, Handler handler);

    public abstract void addTimedTextSource(FileDescriptor fileDescriptor, long j, long j2, String str);

    public abstract void attachAuxEffect(int i);

    public abstract void clearDrmEventCallback();

    public abstract void clearMediaPlayer2EventCallback();

    public abstract void clearPendingCommands();

    public abstract void close();

    public abstract void deselectTrack(int i);

    public abstract AudioAttributes getAudioAttributes();

    public abstract int getAudioSessionId();

    public abstract long getBufferedPosition();

    public abstract int getBufferingState();

    public abstract DataSourceDesc getCurrentDataSource();

    public abstract long getCurrentPosition();

    public abstract DrmInfo getDrmInfo();

    public abstract MediaDrm.KeyRequest getDrmKeyRequest(byte[] bArr, byte[] bArr2, String str, int i, Map<String, String> map) throws NoDrmSchemeException;

    public abstract String getDrmPropertyString(String str) throws NoDrmSchemeException;

    public abstract long getDuration();

    public abstract int getMediaPlayer2State();

    public abstract PersistableBundle getMetrics();

    public abstract PlaybackParams getPlaybackParams();

    public abstract int getPlayerState();

    public abstract float getPlayerVolume();

    public abstract AudioDeviceInfo getPreferredDevice();

    public abstract AudioDeviceInfo getRoutedDevice();

    public abstract int getSelectedTrack(int i);

    public abstract SyncParams getSyncParams();

    public abstract MediaTimestamp getTimestamp();

    public abstract List<TrackInfo> getTrackInfo();

    public abstract int getVideoHeight();

    public abstract int getVideoWidth();

    public abstract boolean isPlaying();

    public abstract void loopCurrent(boolean z);

    public abstract void pause();

    public abstract void play();

    public abstract void prepare();

    public abstract void prepareDrm(UUID uuid) throws UnsupportedSchemeException, ResourceBusyException, ProvisioningNetworkErrorException, ProvisioningServerErrorException;

    public abstract byte[] provideDrmKeyResponse(byte[] bArr, byte[] bArr2) throws NoDrmSchemeException, DeniedByServerException;

    public abstract void registerPlayerEventCallback(Executor executor, MediaPlayerBase.PlayerEventCallback playerEventCallback);

    public abstract void releaseDrm() throws NoDrmSchemeException;

    public abstract void removeOnRoutingChangedListener(AudioRouting.OnRoutingChangedListener onRoutingChangedListener);

    public abstract void reset();

    public abstract void restoreDrmKeys(byte[] bArr) throws NoDrmSchemeException;

    public abstract void seekTo(long j, int i);

    public abstract void selectTrack(int i);

    public abstract void setAudioAttributes(AudioAttributes audioAttributes);

    public abstract void setAudioSessionId(int i);

    public abstract void setAuxEffectSendLevel(float f);

    public abstract void setDataSource(DataSourceDesc dataSourceDesc);

    public abstract void setDisplay(SurfaceHolder surfaceHolder);

    public abstract void setDrmEventCallback(Executor executor, DrmEventCallback drmEventCallback);

    public abstract void setDrmPropertyString(String str, String str2) throws NoDrmSchemeException;

    public abstract void setMediaPlayer2EventCallback(Executor executor, MediaPlayer2EventCallback mediaPlayer2EventCallback);

    public abstract void setNextDataSource(DataSourceDesc dataSourceDesc);

    public abstract void setNextDataSources(List<DataSourceDesc> list);

    public abstract void setOnDrmConfigHelper(OnDrmConfigHelper onDrmConfigHelper);

    public abstract void setPlaybackParams(PlaybackParams playbackParams);

    public abstract void setPlaybackSpeed(float f);

    public abstract void setPlayerVolume(float f);

    public abstract boolean setPreferredDevice(AudioDeviceInfo audioDeviceInfo);

    public abstract void setScreenOnWhilePlaying(boolean z);

    public abstract void setSurface(Surface surface);

    public abstract void setSyncParams(SyncParams syncParams);

    public abstract void setWakeMode(Context context, int i);

    public abstract void skipToNext();

    public abstract void unregisterPlayerEventCallback(MediaPlayerBase.PlayerEventCallback playerEventCallback);

    public static final MediaPlayer2 create() {
        return new MediaPlayer2Impl();
    }

    private static final String[] decodeMediaPlayer2Uri(String location) {
        Uri uri = Uri.parse(location);
        if (!"mediaplayer2".equals(uri.getScheme())) {
            return new String[]{location};
        }
        List<String> uris = uri.getQueryParameters(DownloadManager.COLUMN_URI);
        if (uris.isEmpty()) {
            return new String[]{location};
        }
        List<String> keys = uri.getQueryParameters("key");
        List<String> values = uri.getQueryParameters(Slice.SUBTYPE_VALUE);
        if (keys.size() != values.size()) {
            return new String[]{uris.get(0)};
        }
        List<String> ls = new ArrayList<>();
        ls.add(uris.get(0));
        for (int i = 0; i < keys.size(); i++) {
            ls.add(keys.get(i));
            ls.add(values.get(i));
        }
        return (String[]) ls.toArray(new String[ls.size()]);
    }

    private static final String encodeMediaPlayer2Uri(String uri, String[] keys, String[] values) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("mediaplayer2").path("/").appendQueryParameter(DownloadManager.COLUMN_URI, uri);
        if (keys == null || values == null || keys.length != values.length) {
            return builder.build().toString();
        }
        for (int i = 0; i < keys.length; i++) {
            builder.appendQueryParameter("key", keys[i]).appendQueryParameter(Slice.SUBTYPE_VALUE, values[i]);
        }
        return builder.build().toString();
    }

    public void seekTo(long msec) {
        seekTo(msec, 0);
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

    public Parcel newRequest() {
        return null;
    }

    public void invoke(Parcel request, Parcel reply) {
    }

    public void notifyWhenCommandLabelReached(Object label) {
    }

    public void setVideoScalingMode(int mode) {
    }

    public void stop() {
    }

    public BufferingParams getBufferingParams() {
        return new BufferingParams.Builder().build();
    }

    public void setBufferingParams(BufferingParams params) {
    }

    public PlaybackParams easyPlaybackParams(float rate, int audioMode) {
        return new PlaybackParams();
    }

    public Metadata getMetadata(boolean update_only, boolean apply_filter) {
        return null;
    }

    public int setMetadataFilter(Set<Integer> set, Set<Integer> set2) {
        return 0;
    }

    public void notifyAt(long mediaTimeUs) {
    }

    public boolean isLooping() {
        return false;
    }

    public void setSubtitleAnchor(SubtitleController controller, SubtitleController.Anchor anchor) {
    }

    public void onSubtitleTrackSelected(SubtitleTrack track) {
    }

    public void addSubtitleSource(InputStream is, MediaFormat format) {
    }

    public void addTimedTextSource(String path, String mimeType) throws IOException {
    }

    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IOException {
    }

    public void addTimedTextSource(FileDescriptor fd, String mimeType) {
    }

    public MediaTimeProvider getMediaTimeProvider() {
        return null;
    }

    public void setOnSubtitleDataListener(OnSubtitleDataListener listener) {
    }
}
