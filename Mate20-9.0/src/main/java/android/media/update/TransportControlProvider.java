package android.media.update;

import android.media.MediaItem2;

public interface TransportControlProvider {
    int getRepeatMode_impl();

    int getShuffleMode_impl();

    void pause_impl();

    void play_impl();

    void prepare_impl();

    void seekTo_impl(long j);

    void setRepeatMode_impl(int i);

    void setShuffleMode_impl(int i);

    void skipToNextItem_impl();

    void skipToPlaylistItem_impl(MediaItem2 mediaItem2);

    void skipToPreviousItem_impl();

    void stop_impl();
}
