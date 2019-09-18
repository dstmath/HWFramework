package android.media;

import android.app.PendingIntent;
import android.content.Context;
import android.media.MediaSession2;
import android.media.update.ApiLoader;
import android.media.update.MediaSession2Provider;
import android.media.update.ProviderCreator;
import android.net.Uri;
import android.os.Bundle;
import android.os.IInterface;
import android.os.ResultReceiver;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;

public class MediaSession2 implements AutoCloseable {
    public static final int ERROR_CODE_ACTION_ABORTED = 10;
    public static final int ERROR_CODE_APP_ERROR = 1;
    public static final int ERROR_CODE_AUTHENTICATION_EXPIRED = 3;
    public static final int ERROR_CODE_CONCURRENT_STREAM_LIMIT = 5;
    public static final int ERROR_CODE_CONTENT_ALREADY_PLAYING = 8;
    public static final int ERROR_CODE_END_OF_QUEUE = 11;
    public static final int ERROR_CODE_NOT_AVAILABLE_IN_REGION = 7;
    public static final int ERROR_CODE_NOT_SUPPORTED = 2;
    public static final int ERROR_CODE_PARENTAL_CONTROL_RESTRICTED = 6;
    public static final int ERROR_CODE_PREMIUM_ACCOUNT_REQUIRED = 4;
    public static final int ERROR_CODE_SETUP_REQUIRED = 12;
    public static final int ERROR_CODE_SKIP_LIMIT_REACHED = 9;
    public static final int ERROR_CODE_UNKNOWN_ERROR = 0;
    private final MediaSession2Provider mProvider;

    public static final class Builder extends BuilderBase<MediaSession2, Builder, SessionCallback> {
        public Builder(Context context) {
            super(new ProviderCreator() {
                public final Object createProvider(Object obj) {
                    return ApiLoader.getProvider().createMediaSession2Builder(Context.this, (MediaSession2.Builder) ((MediaSession2.BuilderBase) obj));
                }
            });
        }

        public Builder setPlayer(MediaPlayerBase player) {
            return (Builder) super.setPlayer(player);
        }

        public Builder setPlaylistAgent(MediaPlaylistAgent playlistAgent) {
            return (Builder) super.setPlaylistAgent(playlistAgent);
        }

        public Builder setVolumeProvider(VolumeProvider2 volumeProvider) {
            return (Builder) super.setVolumeProvider(volumeProvider);
        }

        public Builder setSessionActivity(PendingIntent pi) {
            return (Builder) super.setSessionActivity(pi);
        }

        public Builder setId(String id) {
            return (Builder) super.setId(id);
        }

        public Builder setSessionCallback(Executor executor, SessionCallback callback) {
            return (Builder) super.setSessionCallback(executor, callback);
        }

        public MediaSession2 build() {
            return super.build();
        }
    }

    static abstract class BuilderBase<T extends MediaSession2, U extends BuilderBase<T, U, C>, C extends SessionCallback> {
        private final MediaSession2Provider.BuilderBaseProvider<T, C> mProvider;

        BuilderBase(ProviderCreator<BuilderBase<T, U, C>, MediaSession2Provider.BuilderBaseProvider<T, C>> creator) {
            this.mProvider = creator.createProvider(this);
        }

        /* access modifiers changed from: package-private */
        public U setPlayer(MediaPlayerBase player) {
            this.mProvider.setPlayer_impl(player);
            return this;
        }

        /* access modifiers changed from: package-private */
        public U setPlaylistAgent(MediaPlaylistAgent playlistAgent) {
            this.mProvider.setPlaylistAgent_impl(playlistAgent);
            return this;
        }

        /* access modifiers changed from: package-private */
        public U setVolumeProvider(VolumeProvider2 volumeProvider) {
            this.mProvider.setVolumeProvider_impl(volumeProvider);
            return this;
        }

        /* access modifiers changed from: package-private */
        public U setSessionActivity(PendingIntent pi) {
            this.mProvider.setSessionActivity_impl(pi);
            return this;
        }

        /* access modifiers changed from: package-private */
        public U setId(String id) {
            this.mProvider.setId_impl(id);
            return this;
        }

        /* access modifiers changed from: package-private */
        public U setSessionCallback(Executor executor, C callback) {
            this.mProvider.setSessionCallback_impl(executor, callback);
            return this;
        }

        /* access modifiers changed from: package-private */
        public T build() {
            return this.mProvider.build_impl();
        }
    }

    public static final class CommandButton {
        private final MediaSession2Provider.CommandButtonProvider mProvider;

        public static final class Builder {
            private final MediaSession2Provider.CommandButtonProvider.BuilderProvider mProvider = ApiLoader.getProvider().createMediaSession2CommandButtonBuilder(this);

            public Builder setCommand(SessionCommand2 command) {
                return this.mProvider.setCommand_impl(command);
            }

            public Builder setIconResId(int resId) {
                return this.mProvider.setIconResId_impl(resId);
            }

            public Builder setDisplayName(String displayName) {
                return this.mProvider.setDisplayName_impl(displayName);
            }

            public Builder setEnabled(boolean enabled) {
                return this.mProvider.setEnabled_impl(enabled);
            }

            public Builder setExtras(Bundle extras) {
                return this.mProvider.setExtras_impl(extras);
            }

            public CommandButton build() {
                return this.mProvider.build_impl();
            }
        }

        public CommandButton(MediaSession2Provider.CommandButtonProvider provider) {
            this.mProvider = provider;
        }

        public SessionCommand2 getCommand() {
            return this.mProvider.getCommand_impl();
        }

        public int getIconResId() {
            return this.mProvider.getIconResId_impl();
        }

        public String getDisplayName() {
            return this.mProvider.getDisplayName_impl();
        }

        public Bundle getExtras() {
            return this.mProvider.getExtras_impl();
        }

        public boolean isEnabled() {
            return this.mProvider.isEnabled_impl();
        }

        public MediaSession2Provider.CommandButtonProvider getProvider() {
            return this.mProvider;
        }
    }

    public static final class ControllerInfo {
        private final MediaSession2Provider.ControllerInfoProvider mProvider;

        public ControllerInfo(Context context, int uid, int pid, String packageName, IInterface callback) {
            this.mProvider = ApiLoader.getProvider().createMediaSession2ControllerInfo(context, this, uid, pid, packageName, callback);
        }

        public String getPackageName() {
            return this.mProvider.getPackageName_impl();
        }

        public int getUid() {
            return this.mProvider.getUid_impl();
        }

        public boolean isTrusted() {
            return this.mProvider.isTrusted_impl();
        }

        public MediaSession2Provider.ControllerInfoProvider getProvider() {
            return this.mProvider;
        }

        public int hashCode() {
            return this.mProvider.hashCode_impl();
        }

        public boolean equals(Object obj) {
            return this.mProvider.equals_impl(obj);
        }

        public String toString() {
            return this.mProvider.toString_impl();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ErrorCode {
    }

    public interface OnDataSourceMissingHelper {
        DataSourceDesc onDataSourceMissing(MediaSession2 mediaSession2, MediaItem2 mediaItem2);
    }

    public static abstract class SessionCallback {
        public SessionCommandGroup2 onConnect(MediaSession2 session, ControllerInfo controller) {
            SessionCommandGroup2 commands = new SessionCommandGroup2();
            commands.addAllPredefinedCommands();
            return commands;
        }

        public void onDisconnected(MediaSession2 session, ControllerInfo controller) {
        }

        public boolean onCommandRequest(MediaSession2 session, ControllerInfo controller, SessionCommand2 command) {
            return true;
        }

        public void onSetRating(MediaSession2 session, ControllerInfo controller, String mediaId, Rating2 rating) {
        }

        public void onCustomCommand(MediaSession2 session, ControllerInfo controller, SessionCommand2 customCommand, Bundle args, ResultReceiver cb) {
        }

        public void onPlayFromMediaId(MediaSession2 session, ControllerInfo controller, String mediaId, Bundle extras) {
        }

        public void onPlayFromSearch(MediaSession2 session, ControllerInfo controller, String query, Bundle extras) {
        }

        public void onPlayFromUri(MediaSession2 session, ControllerInfo controller, Uri uri, Bundle extras) {
        }

        public void onPrepareFromMediaId(MediaSession2 session, ControllerInfo controller, String mediaId, Bundle extras) {
        }

        public void onPrepareFromSearch(MediaSession2 session, ControllerInfo controller, String query, Bundle extras) {
        }

        public void onPrepareFromUri(MediaSession2 session, ControllerInfo controller, Uri uri, Bundle extras) {
        }

        public void onFastForward(MediaSession2 session) {
        }

        public void onRewind(MediaSession2 session) {
        }

        public void onCurrentMediaItemChanged(MediaSession2 session, MediaPlayerBase player, MediaItem2 item) {
        }

        public void onMediaPrepared(MediaSession2 session, MediaPlayerBase player, MediaItem2 item) {
        }

        public void onPlayerStateChanged(MediaSession2 session, MediaPlayerBase player, int state) {
        }

        public void onBufferingStateChanged(MediaSession2 session, MediaPlayerBase player, MediaItem2 item, int state) {
        }

        public void onPlaybackSpeedChanged(MediaSession2 session, MediaPlayerBase player, float speed) {
        }

        public void onSeekCompleted(MediaSession2 session, MediaPlayerBase mpb, long position) {
        }

        public void onPlaylistChanged(MediaSession2 session, MediaPlaylistAgent playlistAgent, List<MediaItem2> list, MediaMetadata2 metadata) {
        }

        public void onPlaylistMetadataChanged(MediaSession2 session, MediaPlaylistAgent playlistAgent, MediaMetadata2 metadata) {
        }

        public void onShuffleModeChanged(MediaSession2 session, MediaPlaylistAgent playlistAgent, int shuffleMode) {
        }

        public void onRepeatModeChanged(MediaSession2 session, MediaPlaylistAgent playlistAgent, int repeatMode) {
        }
    }

    public MediaSession2(MediaSession2Provider provider) {
        this.mProvider = provider;
    }

    public MediaSession2Provider getProvider() {
        return this.mProvider;
    }

    public void updatePlayer(MediaPlayerBase player, MediaPlaylistAgent playlistAgent, VolumeProvider2 volumeProvider) {
        this.mProvider.updatePlayer_impl(player, playlistAgent, volumeProvider);
    }

    public void close() {
        this.mProvider.close_impl();
    }

    public MediaPlayerBase getPlayer() {
        return this.mProvider.getPlayer_impl();
    }

    public MediaPlaylistAgent getPlaylistAgent() {
        return this.mProvider.getPlaylistAgent_impl();
    }

    public VolumeProvider2 getVolumeProvider() {
        return this.mProvider.getVolumeProvider_impl();
    }

    public SessionToken2 getToken() {
        return this.mProvider.getToken_impl();
    }

    public List<ControllerInfo> getConnectedControllers() {
        return this.mProvider.getConnectedControllers_impl();
    }

    public void setAudioFocusRequest(AudioFocusRequest afr) {
    }

    public void setCustomLayout(ControllerInfo controller, List<CommandButton> layout) {
        this.mProvider.setCustomLayout_impl(controller, layout);
    }

    public void setAllowedCommands(ControllerInfo controller, SessionCommandGroup2 commands) {
        this.mProvider.setAllowedCommands_impl(controller, commands);
    }

    public void sendCustomCommand(SessionCommand2 command, Bundle args) {
        this.mProvider.sendCustomCommand_impl(command, args);
    }

    public void sendCustomCommand(ControllerInfo controller, SessionCommand2 command, Bundle args, ResultReceiver receiver) {
        this.mProvider.sendCustomCommand_impl(controller, command, args, receiver);
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

    public void seekTo(long pos) {
        this.mProvider.seekTo_impl(pos);
    }

    public void skipForward() {
    }

    public void skipBackward() {
    }

    public void notifyError(int errorCode, Bundle extras) {
        this.mProvider.notifyError_impl(errorCode, extras);
    }

    public int getPlayerState() {
        return this.mProvider.getPlayerState_impl();
    }

    public long getCurrentPosition() {
        return this.mProvider.getCurrentPosition_impl();
    }

    public long getBufferedPosition() {
        return this.mProvider.getBufferedPosition_impl();
    }

    public int getBufferingState() {
        return 0;
    }

    public float getPlaybackSpeed() {
        return -1.0f;
    }

    public void setPlaybackSpeed(float speed) {
    }

    public void setOnDataSourceMissingHelper(OnDataSourceMissingHelper helper) {
        this.mProvider.setOnDataSourceMissingHelper_impl(helper);
    }

    public void clearOnDataSourceMissingHelper() {
        this.mProvider.clearOnDataSourceMissingHelper_impl();
    }

    public List<MediaItem2> getPlaylist() {
        return this.mProvider.getPlaylist_impl();
    }

    public void setPlaylist(List<MediaItem2> list, MediaMetadata2 metadata) {
        this.mProvider.setPlaylist_impl(list, metadata);
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
        return this.mProvider.getCurrentPlaylistItem_impl();
    }

    public void updatePlaylistMetadata(MediaMetadata2 metadata) {
        this.mProvider.updatePlaylistMetadata_impl(metadata);
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
