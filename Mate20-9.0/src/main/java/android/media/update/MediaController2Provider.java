package android.media.update;

import android.app.PendingIntent;
import android.media.AudioAttributes;
import android.media.MediaController2;
import android.media.MediaItem2;
import android.media.MediaMetadata2;
import android.media.Rating2;
import android.media.SessionCommand2;
import android.media.SessionToken2;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import java.util.List;

public interface MediaController2Provider extends TransportControlProvider {

    public interface PlaybackInfoProvider {
        AudioAttributes getAudioAttributes_impl();

        int getControlType_impl();

        int getCurrentVolume_impl();

        int getMaxVolume_impl();

        int getPlaybackType_impl();
    }

    void addPlaylistItem_impl(int i, MediaItem2 mediaItem2);

    void adjustVolume_impl(int i, int i2);

    void close_impl();

    void fastForward_impl();

    long getBufferedPosition_impl();

    MediaItem2 getCurrentMediaItem_impl();

    long getCurrentPosition_impl();

    MediaController2.PlaybackInfo getPlaybackInfo_impl();

    float getPlaybackSpeed_impl();

    int getPlayerState_impl();

    MediaMetadata2 getPlaylistMetadata_impl();

    List<MediaItem2> getPlaylist_impl();

    PendingIntent getSessionActivity_impl();

    SessionToken2 getSessionToken_impl();

    void initialize();

    boolean isConnected_impl();

    void playFromMediaId_impl(String str, Bundle bundle);

    void playFromSearch_impl(String str, Bundle bundle);

    void playFromUri_impl(Uri uri, Bundle bundle);

    void prepareFromMediaId_impl(String str, Bundle bundle);

    void prepareFromSearch_impl(String str, Bundle bundle);

    void prepareFromUri_impl(Uri uri, Bundle bundle);

    void removePlaylistItem_impl(MediaItem2 mediaItem2);

    void replacePlaylistItem_impl(int i, MediaItem2 mediaItem2);

    void rewind_impl();

    void sendCustomCommand_impl(SessionCommand2 sessionCommand2, Bundle bundle, ResultReceiver resultReceiver);

    void setPlaylist_impl(List<MediaItem2> list, MediaMetadata2 mediaMetadata2);

    void setRating_impl(String str, Rating2 rating2);

    void setVolumeTo_impl(int i, int i2);

    void updatePlaylistMetadata_impl(MediaMetadata2 mediaMetadata2);
}
