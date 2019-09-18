package android.media.update;

import android.media.DataSourceDesc;
import android.media.MediaItem2;
import android.media.MediaMetadata2;
import android.media.MediaPlaylistAgent;
import java.util.List;
import java.util.concurrent.Executor;

public interface MediaPlaylistAgentProvider {
    void addPlaylistItem_impl(int i, MediaItem2 mediaItem2);

    MediaItem2 getMediaItem_impl(DataSourceDesc dataSourceDesc);

    MediaMetadata2 getPlaylistMetadata_impl();

    List<MediaItem2> getPlaylist_impl();

    int getRepeatMode_impl();

    int getShuffleMode_impl();

    void notifyPlaylistChanged_impl();

    void notifyPlaylistMetadataChanged_impl();

    void notifyRepeatModeChanged_impl();

    void notifyShuffleModeChanged_impl();

    void registerPlaylistEventCallback_impl(Executor executor, MediaPlaylistAgent.PlaylistEventCallback playlistEventCallback);

    void removePlaylistItem_impl(MediaItem2 mediaItem2);

    void replacePlaylistItem_impl(int i, MediaItem2 mediaItem2);

    void setPlaylist_impl(List<MediaItem2> list, MediaMetadata2 mediaMetadata2);

    void setRepeatMode_impl(int i);

    void setShuffleMode_impl(int i);

    void skipToNextItem_impl();

    void skipToPlaylistItem_impl(MediaItem2 mediaItem2);

    void skipToPreviousItem_impl();

    void unregisterPlaylistEventCallback_impl(MediaPlaylistAgent.PlaylistEventCallback playlistEventCallback);

    void updatePlaylistMetadata_impl(MediaMetadata2 mediaMetadata2);
}
