package android.support.v4.media;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.mediacompat.Rating2;
import android.support.v4.media.MediaSession2;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;

@TargetApi(19)
public class MediaController2 implements AutoCloseable {
    private final SupportLibraryImpl mImpl;
    Long mTimeDiff;

    /* access modifiers changed from: package-private */
    public interface SupportLibraryImpl extends AutoCloseable {
        void addPlaylistItem(int i, @NonNull MediaItem2 mediaItem2);

        void adjustVolume(int i, int i2);

        void fastForward();

        @Nullable
        MediaBrowserCompat getBrowserCompat();

        long getBufferedPosition();

        int getBufferingState();

        @NonNull
        ControllerCallback getCallback();

        @NonNull
        Executor getCallbackExecutor();

        @NonNull
        Context getContext();

        MediaItem2 getCurrentMediaItem();

        long getCurrentPosition();

        long getDuration();

        @NonNull
        MediaController2 getInstance();

        @Nullable
        PlaybackInfo getPlaybackInfo();

        float getPlaybackSpeed();

        int getPlayerState();

        @Nullable
        List<MediaItem2> getPlaylist();

        @Nullable
        MediaMetadata2 getPlaylistMetadata();

        int getRepeatMode();

        @Nullable
        PendingIntent getSessionActivity();

        SessionToken2 getSessionToken();

        int getShuffleMode();

        boolean isConnected();

        void pause();

        void play();

        void playFromMediaId(@NonNull String str, @Nullable Bundle bundle);

        void playFromSearch(@NonNull String str, @Nullable Bundle bundle);

        void playFromUri(@NonNull Uri uri, @Nullable Bundle bundle);

        void prepare();

        void prepareFromMediaId(@NonNull String str, @Nullable Bundle bundle);

        void prepareFromSearch(@NonNull String str, @Nullable Bundle bundle);

        void prepareFromUri(@NonNull Uri uri, @Nullable Bundle bundle);

        void removePlaylistItem(@NonNull MediaItem2 mediaItem2);

        void replacePlaylistItem(int i, @NonNull MediaItem2 mediaItem2);

        void reset();

        void rewind();

        void seekTo(long j);

        void selectRoute(@NonNull Bundle bundle);

        void sendCustomCommand(@NonNull SessionCommand2 sessionCommand2, @Nullable Bundle bundle, @Nullable ResultReceiver resultReceiver);

        void setPlaybackSpeed(float f);

        void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 mediaMetadata2);

        void setRating(@NonNull String str, @NonNull Rating2 rating2);

        void setRepeatMode(int i);

        void setShuffleMode(int i);

        void setVolumeTo(int i, int i2);

        void skipBackward();

        void skipForward();

        void skipToNextItem();

        void skipToPlaylistItem(@NonNull MediaItem2 mediaItem2);

        void skipToPreviousItem();

        void subscribeRoutesInfo();

        void unsubscribeRoutesInfo();

        void updatePlaylistMetadata(@Nullable MediaMetadata2 mediaMetadata2);
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface VolumeDirection {
    }

    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public @interface VolumeFlags {
    }

    public MediaController2(@NonNull Context context, @NonNull SessionToken2 token, @NonNull Executor executor, @NonNull ControllerCallback callback) {
        if (context == null) {
            throw new IllegalArgumentException("context shouldn't be null");
        } else if (token == null) {
            throw new IllegalArgumentException("token shouldn't be null");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback shouldn't be null");
        } else if (executor != null) {
            this.mImpl = createImpl(context, token, executor, callback);
        } else {
            throw new IllegalArgumentException("executor shouldn't be null");
        }
    }

    /* access modifiers changed from: package-private */
    public SupportLibraryImpl createImpl(@NonNull Context context, @NonNull SessionToken2 token, @NonNull Executor executor, @NonNull ControllerCallback callback) {
        if (token.isLegacySession()) {
            return new MediaController2ImplLegacy(context, this, token, executor, callback);
        }
        return new MediaController2ImplBase(context, this, token, executor, callback);
    }

    /* access modifiers changed from: package-private */
    public SupportLibraryImpl getImpl() {
        return this.mImpl;
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        try {
            this.mImpl.close();
        } catch (Exception e) {
        }
    }

    @NonNull
    public SessionToken2 getSessionToken() {
        return this.mImpl.getSessionToken();
    }

    public boolean isConnected() {
        return this.mImpl.isConnected();
    }

    public void play() {
        this.mImpl.play();
    }

    public void pause() {
        this.mImpl.pause();
    }

    public void reset() {
        this.mImpl.reset();
    }

    public void prepare() {
        this.mImpl.prepare();
    }

    public void fastForward() {
        this.mImpl.fastForward();
    }

    public void rewind() {
        this.mImpl.rewind();
    }

    public void seekTo(long pos) {
        this.mImpl.seekTo(pos);
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void skipForward() {
        this.mImpl.skipForward();
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void skipBackward() {
        this.mImpl.skipBackward();
    }

    public void playFromMediaId(@NonNull String mediaId, @Nullable Bundle extras) {
        if (mediaId != null) {
            this.mImpl.playFromMediaId(mediaId, extras);
            return;
        }
        throw new IllegalArgumentException("mediaId shouldn't be null");
    }

    public void playFromSearch(@NonNull String query, @Nullable Bundle extras) {
        if (!TextUtils.isEmpty(query)) {
            this.mImpl.playFromSearch(query, extras);
            return;
        }
        throw new IllegalArgumentException("query shouldn't be empty");
    }

    public void playFromUri(@NonNull Uri uri, @Nullable Bundle extras) {
        if (uri != null) {
            this.mImpl.playFromUri(uri, extras);
            return;
        }
        throw new IllegalArgumentException("uri shouldn't be null");
    }

    public void prepareFromMediaId(@NonNull String mediaId, @Nullable Bundle extras) {
        if (mediaId != null) {
            this.mImpl.prepareFromMediaId(mediaId, extras);
            return;
        }
        throw new IllegalArgumentException("mediaId shouldn't be null");
    }

    public void prepareFromSearch(@NonNull String query, @Nullable Bundle extras) {
        if (!TextUtils.isEmpty(query)) {
            this.mImpl.prepareFromSearch(query, extras);
            return;
        }
        throw new IllegalArgumentException("query shouldn't be empty");
    }

    public void prepareFromUri(@NonNull Uri uri, @Nullable Bundle extras) {
        if (uri != null) {
            this.mImpl.prepareFromUri(uri, extras);
            return;
        }
        throw new IllegalArgumentException("uri shouldn't be null");
    }

    public void setVolumeTo(int value, int flags) {
        this.mImpl.setVolumeTo(value, flags);
    }

    public void adjustVolume(int direction, int flags) {
        this.mImpl.adjustVolume(direction, flags);
    }

    @Nullable
    public PendingIntent getSessionActivity() {
        return this.mImpl.getSessionActivity();
    }

    public int getPlayerState() {
        return this.mImpl.getPlayerState();
    }

    public long getDuration() {
        return this.mImpl.getDuration();
    }

    public long getCurrentPosition() {
        return this.mImpl.getCurrentPosition();
    }

    public float getPlaybackSpeed() {
        return this.mImpl.getPlaybackSpeed();
    }

    public void setPlaybackSpeed(float speed) {
        this.mImpl.setPlaybackSpeed(speed);
    }

    public int getBufferingState() {
        return this.mImpl.getBufferingState();
    }

    public long getBufferedPosition() {
        return this.mImpl.getBufferedPosition();
    }

    @Nullable
    public PlaybackInfo getPlaybackInfo() {
        return this.mImpl.getPlaybackInfo();
    }

    public void setRating(@NonNull String mediaId, @NonNull Rating2 rating) {
        if (mediaId == null) {
            throw new IllegalArgumentException("mediaId shouldn't be null");
        } else if (rating != null) {
            this.mImpl.setRating(mediaId, rating);
        } else {
            throw new IllegalArgumentException("rating shouldn't be null");
        }
    }

    public void sendCustomCommand(@NonNull SessionCommand2 command, @Nullable Bundle args, @Nullable ResultReceiver cb) {
        if (command != null) {
            this.mImpl.sendCustomCommand(command, args, cb);
            return;
        }
        throw new IllegalArgumentException("command shouldn't be null");
    }

    @Nullable
    public List<MediaItem2> getPlaylist() {
        return this.mImpl.getPlaylist();
    }

    public void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 metadata) {
        if (list != null) {
            this.mImpl.setPlaylist(list, metadata);
            return;
        }
        throw new IllegalArgumentException("list shouldn't be null");
    }

    public void updatePlaylistMetadata(@Nullable MediaMetadata2 metadata) {
        this.mImpl.updatePlaylistMetadata(metadata);
    }

    @Nullable
    public MediaMetadata2 getPlaylistMetadata() {
        return this.mImpl.getPlaylistMetadata();
    }

    public void addPlaylistItem(int index, @NonNull MediaItem2 item) {
        if (index < 0) {
            throw new IllegalArgumentException("index shouldn't be negative");
        } else if (item != null) {
            this.mImpl.addPlaylistItem(index, item);
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    public void removePlaylistItem(@NonNull MediaItem2 item) {
        if (item != null) {
            this.mImpl.removePlaylistItem(item);
            return;
        }
        throw new IllegalArgumentException("item shouldn't be null");
    }

    public void replacePlaylistItem(int index, @NonNull MediaItem2 item) {
        if (index < 0) {
            throw new IllegalArgumentException("index shouldn't be negative");
        } else if (item != null) {
            this.mImpl.replacePlaylistItem(index, item);
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    public MediaItem2 getCurrentMediaItem() {
        return this.mImpl.getCurrentMediaItem();
    }

    public void skipToPreviousItem() {
        this.mImpl.skipToPreviousItem();
    }

    public void skipToNextItem() {
        this.mImpl.skipToNextItem();
    }

    public void skipToPlaylistItem(@NonNull MediaItem2 item) {
        if (item != null) {
            this.mImpl.skipToPlaylistItem(item);
            return;
        }
        throw new IllegalArgumentException("item shouldn't be null");
    }

    public int getRepeatMode() {
        return this.mImpl.getRepeatMode();
    }

    public void setRepeatMode(int repeatMode) {
        this.mImpl.setRepeatMode(repeatMode);
    }

    public int getShuffleMode() {
        return this.mImpl.getShuffleMode();
    }

    public void setShuffleMode(int shuffleMode) {
        this.mImpl.setShuffleMode(shuffleMode);
    }

    public void subscribeRoutesInfo() {
        this.mImpl.subscribeRoutesInfo();
    }

    public void unsubscribeRoutesInfo() {
        this.mImpl.unsubscribeRoutesInfo();
    }

    public void selectRoute(@NonNull Bundle route) {
        if (route != null) {
            this.mImpl.selectRoute(route);
            return;
        }
        throw new IllegalArgumentException("route shouldn't be null");
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void setTimeDiff(Long timeDiff) {
        this.mTimeDiff = timeDiff;
    }

    /* access modifiers changed from: package-private */
    @NonNull
    public Context getContext() {
        return this.mImpl.getContext();
    }

    /* access modifiers changed from: package-private */
    @NonNull
    public ControllerCallback getCallback() {
        return this.mImpl.getCallback();
    }

    /* access modifiers changed from: package-private */
    @NonNull
    public Executor getCallbackExecutor() {
        return this.mImpl.getCallbackExecutor();
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public MediaBrowserCompat getBrowserCompat() {
        return this.mImpl.getBrowserCompat();
    }

    public static abstract class ControllerCallback {
        public void onConnected(@NonNull MediaController2 controller, @NonNull SessionCommandGroup2 allowedCommands) {
        }

        public void onDisconnected(@NonNull MediaController2 controller) {
        }

        public void onCustomLayoutChanged(@NonNull MediaController2 controller, @NonNull List<MediaSession2.CommandButton> list) {
        }

        public void onPlaybackInfoChanged(@NonNull MediaController2 controller, @NonNull PlaybackInfo info) {
        }

        public void onAllowedCommandsChanged(@NonNull MediaController2 controller, @NonNull SessionCommandGroup2 commands) {
        }

        public void onCustomCommand(@NonNull MediaController2 controller, @NonNull SessionCommand2 command, @Nullable Bundle args, @Nullable ResultReceiver receiver) {
        }

        public void onPlayerStateChanged(@NonNull MediaController2 controller, int state) {
        }

        public void onPlaybackSpeedChanged(@NonNull MediaController2 controller, float speed) {
        }

        public void onBufferingStateChanged(@NonNull MediaController2 controller, @NonNull MediaItem2 item, int state) {
        }

        public void onSeekCompleted(@NonNull MediaController2 controller, long position) {
        }

        public void onError(@NonNull MediaController2 controller, int errorCode, @Nullable Bundle extras) {
        }

        public void onCurrentMediaItemChanged(@NonNull MediaController2 controller, @Nullable MediaItem2 item) {
        }

        public void onPlaylistChanged(@NonNull MediaController2 controller, @NonNull List<MediaItem2> list, @Nullable MediaMetadata2 metadata) {
        }

        public void onPlaylistMetadataChanged(@NonNull MediaController2 controller, @Nullable MediaMetadata2 metadata) {
        }

        public void onShuffleModeChanged(@NonNull MediaController2 controller, int shuffleMode) {
        }

        public void onRepeatModeChanged(@NonNull MediaController2 controller, int repeatMode) {
        }

        public void onRoutesInfoChanged(@NonNull MediaController2 controller, @Nullable List<Bundle> list) {
        }
    }

    public static final class PlaybackInfo {
        private static final String KEY_AUDIO_ATTRIBUTES = "android.media.audio_info.audio_attrs";
        private static final String KEY_CONTROL_TYPE = "android.media.audio_info.control_type";
        private static final String KEY_CURRENT_VOLUME = "android.media.audio_info.current_volume";
        private static final String KEY_MAX_VOLUME = "android.media.audio_info.max_volume";
        private static final String KEY_PLAYBACK_TYPE = "android.media.audio_info.playback_type";
        public static final int PLAYBACK_TYPE_LOCAL = 1;
        public static final int PLAYBACK_TYPE_REMOTE = 2;
        private final AudioAttributesCompat mAudioAttrsCompat;
        private final int mControlType;
        private final int mCurrentVolume;
        private final int mMaxVolume;
        private final int mPlaybackType;

        PlaybackInfo(int playbackType, AudioAttributesCompat attrs, int controlType, int max, int current) {
            this.mPlaybackType = playbackType;
            this.mAudioAttrsCompat = attrs;
            this.mControlType = controlType;
            this.mMaxVolume = max;
            this.mCurrentVolume = current;
        }

        public int getPlaybackType() {
            return this.mPlaybackType;
        }

        public AudioAttributesCompat getAudioAttributes() {
            return this.mAudioAttrsCompat;
        }

        public int getControlType() {
            return this.mControlType;
        }

        public int getMaxVolume() {
            return this.mMaxVolume;
        }

        public int getCurrentVolume() {
            return this.mCurrentVolume;
        }

        /* access modifiers changed from: package-private */
        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_PLAYBACK_TYPE, this.mPlaybackType);
            bundle.putInt(KEY_CONTROL_TYPE, this.mControlType);
            bundle.putInt(KEY_MAX_VOLUME, this.mMaxVolume);
            bundle.putInt(KEY_CURRENT_VOLUME, this.mCurrentVolume);
            if (this.mAudioAttrsCompat != null) {
                bundle.putBundle(KEY_AUDIO_ATTRIBUTES, this.mAudioAttrsCompat.toBundle());
            }
            return bundle;
        }

        static PlaybackInfo createPlaybackInfo(int playbackType, AudioAttributesCompat attrs, int controlType, int max, int current) {
            return new PlaybackInfo(playbackType, attrs, controlType, max, current);
        }

        static PlaybackInfo fromBundle(Bundle bundle) {
            if (bundle == null) {
                return null;
            }
            return createPlaybackInfo(bundle.getInt(KEY_PLAYBACK_TYPE), AudioAttributesCompat.fromBundle(bundle.getBundle(KEY_AUDIO_ATTRIBUTES)), bundle.getInt(KEY_CONTROL_TYPE), bundle.getInt(KEY_MAX_VOLUME), bundle.getInt(KEY_CURRENT_VOLUME));
        }
    }
}
