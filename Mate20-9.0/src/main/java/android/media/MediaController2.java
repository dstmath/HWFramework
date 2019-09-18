package android.media;

import android.app.PendingIntent;
import android.content.Context;
import android.media.MediaSession2;
import android.media.update.ApiLoader;
import android.media.update.MediaController2Provider;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import java.util.List;
import java.util.concurrent.Executor;

public class MediaController2 implements AutoCloseable {
    private final MediaController2Provider mProvider;

    public static abstract class ControllerCallback {
        public void onConnected(MediaController2 controller, SessionCommandGroup2 allowedCommands) {
        }

        public void onDisconnected(MediaController2 controller) {
        }

        public void onCustomLayoutChanged(MediaController2 controller, List<MediaSession2.CommandButton> list) {
        }

        public void onPlaybackInfoChanged(MediaController2 controller, PlaybackInfo info) {
        }

        public void onAllowedCommandsChanged(MediaController2 controller, SessionCommandGroup2 commands) {
        }

        public void onCustomCommand(MediaController2 controller, SessionCommand2 command, Bundle args, ResultReceiver receiver) {
        }

        public void onPlayerStateChanged(MediaController2 controller, int state) {
        }

        public void onPlaybackSpeedChanged(MediaController2 controller, float speed) {
        }

        public void onBufferingStateChanged(MediaController2 controller, MediaItem2 item, int state) {
        }

        public void onSeekCompleted(MediaController2 controller, long position) {
        }

        public void onError(MediaController2 controller, int errorCode, Bundle extras) {
        }

        public void onCurrentMediaItemChanged(MediaController2 controller, MediaItem2 item) {
        }

        public void onPlaylistChanged(MediaController2 controller, List<MediaItem2> list, MediaMetadata2 metadata) {
        }

        public void onPlaylistMetadataChanged(MediaController2 controller, MediaMetadata2 metadata) {
        }

        public void onShuffleModeChanged(MediaController2 controller, int shuffleMode) {
        }

        public void onRepeatModeChanged(MediaController2 controller, int repeatMode) {
        }
    }

    public static final class PlaybackInfo {
        public static final int PLAYBACK_TYPE_LOCAL = 1;
        public static final int PLAYBACK_TYPE_REMOTE = 2;
        private final MediaController2Provider.PlaybackInfoProvider mProvider;

        public PlaybackInfo(MediaController2Provider.PlaybackInfoProvider provider) {
            this.mProvider = provider;
        }

        public MediaController2Provider.PlaybackInfoProvider getProvider() {
            return this.mProvider;
        }

        public int getPlaybackType() {
            return this.mProvider.getPlaybackType_impl();
        }

        public AudioAttributes getAudioAttributes() {
            return this.mProvider.getAudioAttributes_impl();
        }

        public int getControlType() {
            return this.mProvider.getControlType_impl();
        }

        public int getMaxVolume() {
            return this.mProvider.getMaxVolume_impl();
        }

        public int getCurrentVolume() {
            return this.mProvider.getCurrentVolume_impl();
        }
    }

    public MediaController2(Context context, SessionToken2 token, Executor executor, ControllerCallback callback) {
        this.mProvider = createProvider(context, token, executor, callback);
        this.mProvider.initialize();
    }

    /* access modifiers changed from: package-private */
    public MediaController2Provider createProvider(Context context, SessionToken2 token, Executor executor, ControllerCallback callback) {
        return ApiLoader.getProvider().createMediaController2(context, this, token, executor, callback);
    }

    public void close() {
        this.mProvider.close_impl();
    }

    public MediaController2Provider getProvider() {
        return this.mProvider;
    }

    public SessionToken2 getSessionToken() {
        return this.mProvider.getSessionToken_impl();
    }

    public boolean isConnected() {
        return this.mProvider.isConnected_impl();
    }

    public void play() {
        this.mProvider.play_impl();
    }

    public void pause() {
        this.mProvider.pause_impl();
    }

    public void stop() {
        this.mProvider.stop_impl();
    }

    public void prepare() {
        this.mProvider.prepare_impl();
    }

    public void fastForward() {
        this.mProvider.fastForward_impl();
    }

    public void rewind() {
        this.mProvider.rewind_impl();
    }

    public void seekTo(long pos) {
        this.mProvider.seekTo_impl(pos);
    }

    public void skipForward() {
    }

    public void skipBackward() {
    }

    public void playFromMediaId(String mediaId, Bundle extras) {
        this.mProvider.playFromMediaId_impl(mediaId, extras);
    }

    public void playFromSearch(String query, Bundle extras) {
        this.mProvider.playFromSearch_impl(query, extras);
    }

    public void playFromUri(Uri uri, Bundle extras) {
        this.mProvider.playFromUri_impl(uri, extras);
    }

    public void prepareFromMediaId(String mediaId, Bundle extras) {
        this.mProvider.prepareFromMediaId_impl(mediaId, extras);
    }

    public void prepareFromSearch(String query, Bundle extras) {
        this.mProvider.prepareFromSearch_impl(query, extras);
    }

    public void prepareFromUri(Uri uri, Bundle extras) {
        this.mProvider.prepareFromUri_impl(uri, extras);
    }

    public void setVolumeTo(int value, int flags) {
        this.mProvider.setVolumeTo_impl(value, flags);
    }

    public void adjustVolume(int direction, int flags) {
        this.mProvider.adjustVolume_impl(direction, flags);
    }

    public PendingIntent getSessionActivity() {
        return this.mProvider.getSessionActivity_impl();
    }

    public int getPlayerState() {
        return this.mProvider.getPlayerState_impl();
    }

    public long getCurrentPosition() {
        return this.mProvider.getCurrentPosition_impl();
    }

    public float getPlaybackSpeed() {
        return this.mProvider.getPlaybackSpeed_impl();
    }

    public void setPlaybackSpeed(float speed) {
    }

    public int getBufferingState() {
        return 0;
    }

    public long getBufferedPosition() {
        return this.mProvider.getBufferedPosition_impl();
    }

    public PlaybackInfo getPlaybackInfo() {
        return this.mProvider.getPlaybackInfo_impl();
    }

    public void setRating(String mediaId, Rating2 rating) {
        this.mProvider.setRating_impl(mediaId, rating);
    }

    public void sendCustomCommand(SessionCommand2 command, Bundle args, ResultReceiver cb) {
        this.mProvider.sendCustomCommand_impl(command, args, cb);
    }

    public List<MediaItem2> getPlaylist() {
        return this.mProvider.getPlaylist_impl();
    }

    public void setPlaylist(List<MediaItem2> list, MediaMetadata2 metadata) {
        this.mProvider.setPlaylist_impl(list, metadata);
    }

    public void updatePlaylistMetadata(MediaMetadata2 metadata) {
        this.mProvider.updatePlaylistMetadata_impl(metadata);
    }

    public MediaMetadata2 getPlaylistMetadata() {
        return this.mProvider.getPlaylistMetadata_impl();
    }

    public void addPlaylistItem(int index, MediaItem2 item) {
        this.mProvider.addPlaylistItem_impl(index, item);
    }

    public void removePlaylistItem(MediaItem2 item) {
        this.mProvider.removePlaylistItem_impl(item);
    }

    public void replacePlaylistItem(int index, MediaItem2 item) {
        this.mProvider.replacePlaylistItem_impl(index, item);
    }

    public MediaItem2 getCurrentMediaItem() {
        return this.mProvider.getCurrentMediaItem_impl();
    }

    public void skipToPreviousItem() {
        this.mProvider.skipToPreviousItem_impl();
    }

    public void skipToNextItem() {
        this.mProvider.skipToNextItem_impl();
    }

    public void skipToPlaylistItem(MediaItem2 item) {
        this.mProvider.skipToPlaylistItem_impl(item);
    }

    public int getRepeatMode() {
        return this.mProvider.getRepeatMode_impl();
    }

    public void setRepeatMode(int repeatMode) {
        this.mProvider.setRepeatMode_impl(repeatMode);
    }

    public int getShuffleMode() {
        return this.mProvider.getShuffleMode_impl();
    }

    public void setShuffleMode(int shuffleMode) {
        this.mProvider.setShuffleMode_impl(shuffleMode);
    }
}
