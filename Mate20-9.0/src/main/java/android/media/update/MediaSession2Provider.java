package android.media.update;

import android.app.PendingIntent;
import android.media.AudioFocusRequest;
import android.media.MediaItem2;
import android.media.MediaMetadata2;
import android.media.MediaPlayerBase;
import android.media.MediaPlaylistAgent;
import android.media.MediaSession2;
import android.media.SessionCommand2;
import android.media.SessionCommandGroup2;
import android.media.SessionToken2;
import android.media.VolumeProvider2;
import android.os.Bundle;
import android.os.ResultReceiver;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public interface MediaSession2Provider extends TransportControlProvider {

    public interface BuilderBaseProvider<T extends MediaSession2, C extends MediaSession2.SessionCallback> {
        T build_impl();

        void setId_impl(String str);

        void setPlayer_impl(MediaPlayerBase mediaPlayerBase);

        void setPlaylistAgent_impl(MediaPlaylistAgent mediaPlaylistAgent);

        void setSessionActivity_impl(PendingIntent pendingIntent);

        void setSessionCallback_impl(Executor executor, C c);

        void setVolumeProvider_impl(VolumeProvider2 volumeProvider2);
    }

    public interface CommandButtonProvider {

        public interface BuilderProvider {
            MediaSession2.CommandButton build_impl();

            MediaSession2.CommandButton.Builder setCommand_impl(SessionCommand2 sessionCommand2);

            MediaSession2.CommandButton.Builder setDisplayName_impl(String str);

            MediaSession2.CommandButton.Builder setEnabled_impl(boolean z);

            MediaSession2.CommandButton.Builder setExtras_impl(Bundle bundle);

            MediaSession2.CommandButton.Builder setIconResId_impl(int i);
        }

        SessionCommand2 getCommand_impl();

        String getDisplayName_impl();

        Bundle getExtras_impl();

        int getIconResId_impl();

        boolean isEnabled_impl();
    }

    public interface CommandGroupProvider {
        void addAllPredefinedCommands_impl();

        void addCommand_impl(SessionCommand2 sessionCommand2);

        Set<SessionCommand2> getCommands_impl();

        boolean hasCommand_impl(int i);

        boolean hasCommand_impl(SessionCommand2 sessionCommand2);

        void removeCommand_impl(SessionCommand2 sessionCommand2);

        Bundle toBundle_impl();
    }

    public interface CommandProvider {
        boolean equals_impl(Object obj);

        int getCommandCode_impl();

        String getCustomCommand_impl();

        Bundle getExtras_impl();

        int hashCode_impl();

        Bundle toBundle_impl();
    }

    public interface ControllerInfoProvider {
        boolean equals_impl(Object obj);

        String getPackageName_impl();

        int getUid_impl();

        int hashCode_impl();

        boolean isTrusted_impl();

        String toString_impl();
    }

    void addPlaylistItem_impl(int i, MediaItem2 mediaItem2);

    void clearOnDataSourceMissingHelper_impl();

    void close_impl();

    long getBufferedPosition_impl();

    List<MediaSession2.ControllerInfo> getConnectedControllers_impl();

    MediaItem2 getCurrentPlaylistItem_impl();

    long getCurrentPosition_impl();

    int getPlayerState_impl();

    MediaPlayerBase getPlayer_impl();

    MediaPlaylistAgent getPlaylistAgent_impl();

    MediaMetadata2 getPlaylistMetadata_impl();

    List<MediaItem2> getPlaylist_impl();

    SessionToken2 getToken_impl();

    VolumeProvider2 getVolumeProvider_impl();

    void notifyError_impl(int i, Bundle bundle);

    void removePlaylistItem_impl(MediaItem2 mediaItem2);

    void replacePlaylistItem_impl(int i, MediaItem2 mediaItem2);

    void sendCustomCommand_impl(MediaSession2.ControllerInfo controllerInfo, SessionCommand2 sessionCommand2, Bundle bundle, ResultReceiver resultReceiver);

    void sendCustomCommand_impl(SessionCommand2 sessionCommand2, Bundle bundle);

    void setAllowedCommands_impl(MediaSession2.ControllerInfo controllerInfo, SessionCommandGroup2 sessionCommandGroup2);

    void setAudioFocusRequest_impl(AudioFocusRequest audioFocusRequest);

    void setCustomLayout_impl(MediaSession2.ControllerInfo controllerInfo, List<MediaSession2.CommandButton> list);

    void setOnDataSourceMissingHelper_impl(MediaSession2.OnDataSourceMissingHelper onDataSourceMissingHelper);

    void setPlaylist_impl(List<MediaItem2> list, MediaMetadata2 mediaMetadata2);

    void updatePlayer_impl(MediaPlayerBase mediaPlayerBase, MediaPlaylistAgent mediaPlaylistAgent, VolumeProvider2 volumeProvider2);

    void updatePlaylistMetadata_impl(MediaMetadata2 mediaMetadata2);
}
