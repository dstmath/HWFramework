package android.media;

import android.media.update.ApiLoader;
import android.media.update.MediaPlaylistAgentProvider;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class MediaPlaylistAgent {
    public static final int REPEAT_MODE_ALL = 2;
    public static final int REPEAT_MODE_GROUP = 3;
    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ONE = 1;
    public static final int SHUFFLE_MODE_ALL = 1;
    public static final int SHUFFLE_MODE_GROUP = 2;
    public static final int SHUFFLE_MODE_NONE = 0;
    private final MediaPlaylistAgentProvider mProvider = ApiLoader.getProvider().createMediaPlaylistAgent(this);

    public static abstract class PlaylistEventCallback {
        public void onPlaylistChanged(MediaPlaylistAgent playlistAgent, List<MediaItem2> list, MediaMetadata2 metadata) {
        }

        public void onPlaylistMetadataChanged(MediaPlaylistAgent playlistAgent, MediaMetadata2 metadata) {
        }

        public void onShuffleModeChanged(MediaPlaylistAgent playlistAgent, int shuffleMode) {
        }

        public void onRepeatModeChanged(MediaPlaylistAgent playlistAgent, int repeatMode) {
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RepeatMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ShuffleMode {
    }

    public final void registerPlaylistEventCallback(Executor executor, PlaylistEventCallback callback) {
        this.mProvider.registerPlaylistEventCallback_impl(executor, callback);
    }

    public final void unregisterPlaylistEventCallback(PlaylistEventCallback callback) {
        this.mProvider.unregisterPlaylistEventCallback_impl(callback);
    }

    public final void notifyPlaylistChanged() {
        this.mProvider.notifyPlaylistChanged_impl();
    }

    public final void notifyPlaylistMetadataChanged() {
        this.mProvider.notifyPlaylistMetadataChanged_impl();
    }

    public final void notifyShuffleModeChanged() {
        this.mProvider.notifyShuffleModeChanged_impl();
    }

    public final void notifyRepeatModeChanged() {
        this.mProvider.notifyRepeatModeChanged_impl();
    }

    public List<MediaItem2> getPlaylist() {
        return this.mProvider.getPlaylist_impl();
    }

    public void setPlaylist(List<MediaItem2> list, MediaMetadata2 metadata) {
        this.mProvider.setPlaylist_impl(list, metadata);
    }

    public MediaMetadata2 getPlaylistMetadata() {
        return this.mProvider.getPlaylistMetadata_impl();
    }

    public void updatePlaylistMetadata(MediaMetadata2 metadata) {
        this.mProvider.updatePlaylistMetadata_impl(metadata);
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

    public void skipToPlaylistItem(MediaItem2 item) {
        this.mProvider.skipToPlaylistItem_impl(item);
    }

    public void skipToPreviousItem() {
        this.mProvider.skipToPreviousItem_impl();
    }

    public void skipToNextItem() {
        this.mProvider.skipToNextItem_impl();
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

    public MediaItem2 getMediaItem(DataSourceDesc dsd) {
        return this.mProvider.getMediaItem_impl(dsd);
    }
}
