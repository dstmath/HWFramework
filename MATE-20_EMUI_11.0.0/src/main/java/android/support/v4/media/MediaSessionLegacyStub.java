package android.support.v4.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.BundleCompat;
import android.support.v4.media.MediaController2;
import android.support.v4.media.MediaSession2;
import android.support.v4.media.session.IMediaControllerCallback;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* access modifiers changed from: package-private */
@TargetApi(19)
public class MediaSessionLegacyStub extends MediaSessionCompat.Callback {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "MediaSessionLegacyStub";
    private static final SparseArray<SessionCommand2> sCommandsForOnCommandRequest = new SparseArray<>();
    @GuardedBy("mLock")
    private final ArrayMap<MediaSession2.ControllerInfo, SessionCommandGroup2> mAllowedCommandGroupMap = new ArrayMap<>();
    @GuardedBy("mLock")
    private final Set<IBinder> mConnectingControllers = new HashSet();
    final Context mContext;
    @GuardedBy("mLock")
    private final ArrayMap<IBinder, MediaSession2.ControllerInfo> mControllers = new ArrayMap<>();
    private final Object mLock = new Object();
    final MediaSession2.SupportLibraryImpl mSession;

    /* access modifiers changed from: private */
    @FunctionalInterface
    public interface Session2Runnable {
        void run(MediaSession2.ControllerInfo controllerInfo) throws RemoteException;
    }

    static {
        SessionCommandGroup2 group = new SessionCommandGroup2();
        group.addAllPlaybackCommands();
        group.addAllPlaylistCommands();
        group.addAllVolumeCommands();
        for (SessionCommand2 command : group.getCommands()) {
            sCommandsForOnCommandRequest.append(command.getCommandCode(), command);
        }
    }

    MediaSessionLegacyStub(MediaSession2.SupportLibraryImpl session) {
        this.mSession = session;
        this.mContext = this.mSession.getContext();
    }

    @Override // android.support.v4.media.session.MediaSessionCompat.Callback
    public void onPrepare() {
        this.mSession.getCallbackExecutor().execute(new Runnable() {
            /* class android.support.v4.media.MediaSessionLegacyStub.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                if (!MediaSessionLegacyStub.this.mSession.isClosed()) {
                    MediaSessionLegacyStub.this.mSession.prepare();
                }
            }
        });
    }

    @Override // android.support.v4.media.session.MediaSessionCompat.Callback
    public void onPlay() {
        this.mSession.getCallbackExecutor().execute(new Runnable() {
            /* class android.support.v4.media.MediaSessionLegacyStub.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                if (!MediaSessionLegacyStub.this.mSession.isClosed()) {
                    MediaSessionLegacyStub.this.mSession.play();
                }
            }
        });
    }

    @Override // android.support.v4.media.session.MediaSessionCompat.Callback
    public void onPause() {
        this.mSession.getCallbackExecutor().execute(new Runnable() {
            /* class android.support.v4.media.MediaSessionLegacyStub.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                if (!MediaSessionLegacyStub.this.mSession.isClosed()) {
                    MediaSessionLegacyStub.this.mSession.pause();
                }
            }
        });
    }

    @Override // android.support.v4.media.session.MediaSessionCompat.Callback
    public void onStop() {
        this.mSession.getCallbackExecutor().execute(new Runnable() {
            /* class android.support.v4.media.MediaSessionLegacyStub.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                if (!MediaSessionLegacyStub.this.mSession.isClosed()) {
                    MediaSessionLegacyStub.this.mSession.reset();
                }
            }
        });
    }

    @Override // android.support.v4.media.session.MediaSessionCompat.Callback
    public void onSeekTo(final long pos) {
        this.mSession.getCallbackExecutor().execute(new Runnable() {
            /* class android.support.v4.media.MediaSessionLegacyStub.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                if (!MediaSessionLegacyStub.this.mSession.isClosed()) {
                    MediaSessionLegacyStub.this.mSession.seekTo(pos);
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public List<MediaSession2.ControllerInfo> getConnectedControllers() {
        ArrayList<MediaSession2.ControllerInfo> controllers = new ArrayList<>();
        synchronized (this.mLock) {
            for (int i = 0; i < this.mControllers.size(); i++) {
                controllers.add(this.mControllers.valueAt(i));
            }
        }
        return controllers;
    }

    /* access modifiers changed from: package-private */
    public void setAllowedCommands(MediaSession2.ControllerInfo controller, SessionCommandGroup2 commands) {
        synchronized (this.mLock) {
            this.mAllowedCommandGroupMap.put(controller, commands);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAllowedCommand(MediaSession2.ControllerInfo controller, SessionCommand2 command) {
        SessionCommandGroup2 allowedCommands;
        synchronized (this.mLock) {
            allowedCommands = this.mAllowedCommandGroupMap.get(controller);
        }
        if (allowedCommands == null || !allowedCommands.hasCommand(command)) {
            return DEBUG;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAllowedCommand(MediaSession2.ControllerInfo controller, int commandCode) {
        SessionCommandGroup2 allowedCommands;
        synchronized (this.mLock) {
            allowedCommands = this.mAllowedCommandGroupMap.get(controller);
        }
        if (allowedCommands == null || !allowedCommands.hasCommand(commandCode)) {
            return DEBUG;
        }
        return true;
    }

    private void onCommand2(@NonNull IBinder caller, int commandCode, @NonNull Session2Runnable runnable) {
        onCommand2Internal(caller, null, commandCode, runnable);
    }

    private void onCommand2(@NonNull IBinder caller, @NonNull SessionCommand2 sessionCommand, @NonNull Session2Runnable runnable) {
        onCommand2Internal(caller, sessionCommand, 0, runnable);
    }

    private void onCommand2Internal(@NonNull IBinder caller, @Nullable final SessionCommand2 sessionCommand, final int commandCode, @NonNull final Session2Runnable runnable) {
        final MediaSession2.ControllerInfo controller;
        synchronized (this.mLock) {
            controller = this.mControllers.get(caller);
        }
        if (this.mSession != null && controller != null) {
            this.mSession.getCallbackExecutor().execute(new Runnable() {
                /* class android.support.v4.media.MediaSessionLegacyStub.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    SessionCommand2 command;
                    if (sessionCommand != null) {
                        if (MediaSessionLegacyStub.this.isAllowedCommand(controller, sessionCommand)) {
                            command = (SessionCommand2) MediaSessionLegacyStub.sCommandsForOnCommandRequest.get(sessionCommand.getCommandCode());
                        } else {
                            return;
                        }
                    } else if (MediaSessionLegacyStub.this.isAllowedCommand(controller, commandCode)) {
                        command = (SessionCommand2) MediaSessionLegacyStub.sCommandsForOnCommandRequest.get(commandCode);
                    } else {
                        return;
                    }
                    if (command == null || MediaSessionLegacyStub.this.mSession.getCallback().onCommandRequest(MediaSessionLegacyStub.this.mSession.getInstance(), controller, command)) {
                        try {
                            runnable.run(controller);
                        } catch (RemoteException e) {
                            Log.w(MediaSessionLegacyStub.TAG, "Exception in " + controller.toString(), e);
                        }
                    } else if (MediaSessionLegacyStub.DEBUG) {
                        Log.d(MediaSessionLegacyStub.TAG, "Command (" + command + ") from " + controller + " was rejected by " + MediaSessionLegacyStub.this.mSession);
                    }
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void removeControllerInfo(MediaSession2.ControllerInfo controller) {
        synchronized (this.mLock) {
            MediaSession2.ControllerInfo controller2 = this.mControllers.remove(controller.getId());
            if (DEBUG) {
                Log.d(TAG, "releasing " + controller2);
            }
        }
    }

    private MediaSession2.ControllerInfo createControllerInfo(Bundle extras) {
        IMediaControllerCallback callback = IMediaControllerCallback.Stub.asInterface(BundleCompat.getBinder(extras, "android.support.v4.media.argument.ICONTROLLER_CALLBACK"));
        return new MediaSession2.ControllerInfo(extras.getString("android.support.v4.media.argument.PACKAGE_NAME"), extras.getInt("android.support.v4.media.argument.PID"), extras.getInt("android.support.v4.media.argument.UID"), new ControllerLegacyCb(callback));
    }

    private void connect(Bundle extras, final ResultReceiver cb) {
        final MediaSession2.ControllerInfo controllerInfo = createControllerInfo(extras);
        this.mSession.getCallbackExecutor().execute(new Runnable() {
            /* class android.support.v4.media.MediaSessionLegacyStub.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                SessionCommandGroup2 allowedCommands;
                if (!MediaSessionLegacyStub.this.mSession.isClosed()) {
                    synchronized (MediaSessionLegacyStub.this.mLock) {
                        MediaSessionLegacyStub.this.mConnectingControllers.add(controllerInfo.getId());
                    }
                    SessionCommandGroup2 allowedCommands2 = MediaSessionLegacyStub.this.mSession.getCallback().onConnect(MediaSessionLegacyStub.this.mSession.getInstance(), controllerInfo);
                    MediaItem2 currentMediaItem = null;
                    if (allowedCommands2 != null || controllerInfo.isTrusted()) {
                        if (MediaSessionLegacyStub.DEBUG) {
                            Log.d(MediaSessionLegacyStub.TAG, "Accepting connection, controllerInfo=" + controllerInfo + " allowedCommands=" + allowedCommands2);
                        }
                        if (allowedCommands2 == null) {
                            allowedCommands = new SessionCommandGroup2();
                        } else {
                            allowedCommands = allowedCommands2;
                        }
                        synchronized (MediaSessionLegacyStub.this.mLock) {
                            MediaSessionLegacyStub.this.mConnectingControllers.remove(controllerInfo.getId());
                            MediaSessionLegacyStub.this.mControllers.put(controllerInfo.getId(), controllerInfo);
                            MediaSessionLegacyStub.this.mAllowedCommandGroupMap.put(controllerInfo, allowedCommands);
                        }
                        Bundle resultData = new Bundle();
                        resultData.putBundle("android.support.v4.media.argument.ALLOWED_COMMANDS", allowedCommands.toBundle());
                        resultData.putInt("android.support.v4.media.argument.PLAYER_STATE", MediaSessionLegacyStub.this.mSession.getPlayerState());
                        resultData.putInt("android.support.v4.media.argument.BUFFERING_STATE", MediaSessionLegacyStub.this.mSession.getBufferingState());
                        resultData.putParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT", MediaSessionLegacyStub.this.mSession.getPlaybackStateCompat());
                        resultData.putInt("android.support.v4.media.argument.REPEAT_MODE", MediaSessionLegacyStub.this.mSession.getRepeatMode());
                        resultData.putInt("android.support.v4.media.argument.SHUFFLE_MODE", MediaSessionLegacyStub.this.mSession.getShuffleMode());
                        List<MediaItem2> playlist = allowedCommands.hasCommand(18) ? MediaSessionLegacyStub.this.mSession.getPlaylist() : null;
                        if (playlist != null) {
                            resultData.putParcelableArray("android.support.v4.media.argument.PLAYLIST", MediaUtils2.convertMediaItem2ListToParcelableArray(playlist));
                        }
                        if (allowedCommands.hasCommand(20)) {
                            currentMediaItem = MediaSessionLegacyStub.this.mSession.getCurrentMediaItem();
                        }
                        if (currentMediaItem != null) {
                            resultData.putBundle("android.support.v4.media.argument.MEDIA_ITEM", currentMediaItem.toBundle());
                        }
                        resultData.putBundle("android.support.v4.media.argument.PLAYBACK_INFO", MediaSessionLegacyStub.this.mSession.getPlaybackInfo().toBundle());
                        MediaMetadata2 playlistMetadata = MediaSessionLegacyStub.this.mSession.getPlaylistMetadata();
                        if (playlistMetadata != null) {
                            resultData.putBundle("android.support.v4.media.argument.PLAYLIST_METADATA", playlistMetadata.toBundle());
                        }
                        if (!MediaSessionLegacyStub.this.mSession.isClosed()) {
                            cb.send(0, resultData);
                            return;
                        }
                        return;
                    }
                    synchronized (MediaSessionLegacyStub.this.mLock) {
                        MediaSessionLegacyStub.this.mConnectingControllers.remove(controllerInfo.getId());
                    }
                    if (MediaSessionLegacyStub.DEBUG) {
                        Log.d(MediaSessionLegacyStub.TAG, "Rejecting connection, controllerInfo=" + controllerInfo);
                    }
                    cb.send(-1, null);
                }
            }
        });
    }

    private void disconnect(Bundle extras) {
        final MediaSession2.ControllerInfo controllerInfo = createControllerInfo(extras);
        this.mSession.getCallbackExecutor().execute(new Runnable() {
            /* class android.support.v4.media.MediaSessionLegacyStub.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                if (!MediaSessionLegacyStub.this.mSession.isClosed()) {
                    MediaSessionLegacyStub.this.mSession.getCallback().onDisconnected(MediaSessionLegacyStub.this.mSession.getInstance(), controllerInfo);
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public final class ControllerLegacyCb extends MediaSession2.ControllerCb {
        private final IMediaControllerCallback mIControllerCallback;

        ControllerLegacyCb(@NonNull IMediaControllerCallback callback) {
            this.mIControllerCallback = callback;
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        @NonNull
        public IBinder getId() {
            return this.mIControllerCallback.asBinder();
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onCustomLayoutChanged(List<MediaSession2.CommandButton> layout) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putParcelableArray("android.support.v4.media.argument.COMMAND_BUTTONS", MediaUtils2.convertCommandButtonListToParcelableArray(layout));
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.SET_CUSTOM_LAYOUT", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onPlaybackInfoChanged(MediaController2.PlaybackInfo info) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putBundle("android.support.v4.media.argument.PLAYBACK_INFO", info.toBundle());
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_PLAYBACK_INFO_CHANGED", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onAllowedCommandsChanged(SessionCommandGroup2 commands) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putBundle("android.support.v4.media.argument.ALLOWED_COMMANDS", commands.toBundle());
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_ALLOWED_COMMANDS_CHANGED", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onCustomCommand(SessionCommand2 command, Bundle args, ResultReceiver receiver) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putBundle("android.support.v4.media.argument.CUSTOM_COMMAND", command.toBundle());
            bundle.putBundle("android.support.v4.media.argument.ARGUMENTS", args);
            bundle.putParcelable("android.support.v4.media.argument.RESULT_RECEIVER", receiver);
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.SEND_CUSTOM_COMMAND", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onPlayerStateChanged(long eventTimeMs, long positionMs, int playerState) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putInt("android.support.v4.media.argument.PLAYER_STATE", playerState);
            bundle.putParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT", MediaSessionLegacyStub.this.mSession.getPlaybackStateCompat());
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_PLAYER_STATE_CHANGED", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onPlaybackSpeedChanged(long eventTimeMs, long positionMs, float speed) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT", MediaSessionLegacyStub.this.mSession.getPlaybackStateCompat());
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_PLAYBACK_SPEED_CHANGED", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onBufferingStateChanged(MediaItem2 item, int state, long bufferedPositionMs) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putBundle("android.support.v4.media.argument.MEDIA_ITEM", item.toBundle());
            bundle.putInt("android.support.v4.media.argument.BUFFERING_STATE", state);
            bundle.putParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT", MediaSessionLegacyStub.this.mSession.getPlaybackStateCompat());
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_BUFFERING_STATE_CHANGED", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onSeekCompleted(long eventTimeMs, long positionMs, long position) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putLong("android.support.v4.media.argument.SEEK_POSITION", position);
            bundle.putParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT", MediaSessionLegacyStub.this.mSession.getPlaybackStateCompat());
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_SEEK_COMPLETED", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onError(int errorCode, Bundle extras) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putInt("android.support.v4.media.argument.ERROR_CODE", errorCode);
            bundle.putBundle("android.support.v4.media.argument.EXTRAS", extras);
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_ERROR", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onCurrentMediaItemChanged(MediaItem2 item) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putBundle("android.support.v4.media.argument.MEDIA_ITEM", item == null ? null : item.toBundle());
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_CURRENT_MEDIA_ITEM_CHANGED", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onPlaylistChanged(List<MediaItem2> playlist, MediaMetadata2 metadata) throws RemoteException {
            Bundle bundle;
            Bundle bundle2 = new Bundle();
            bundle2.putParcelableArray("android.support.v4.media.argument.PLAYLIST", MediaUtils2.convertMediaItem2ListToParcelableArray(playlist));
            if (metadata == null) {
                bundle = null;
            } else {
                bundle = metadata.toBundle();
            }
            bundle2.putBundle("android.support.v4.media.argument.PLAYLIST_METADATA", bundle);
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_PLAYLIST_CHANGED", bundle2);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onPlaylistMetadataChanged(MediaMetadata2 metadata) throws RemoteException {
            Bundle bundle;
            Bundle bundle2 = new Bundle();
            if (metadata == null) {
                bundle = null;
            } else {
                bundle = metadata.toBundle();
            }
            bundle2.putBundle("android.support.v4.media.argument.PLAYLIST_METADATA", bundle);
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_PLAYLIST_METADATA_CHANGED", bundle2);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onShuffleModeChanged(int shuffleMode) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putInt("android.support.v4.media.argument.SHUFFLE_MODE", shuffleMode);
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_SHUFFLE_MODE_CHANGED", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onRepeatModeChanged(int repeatMode) throws RemoteException {
            Bundle bundle = new Bundle();
            bundle.putInt("android.support.v4.media.argument.REPEAT_MODE", repeatMode);
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_REPEAT_MODE_CHANGED", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onRoutesInfoChanged(List<Bundle> routes) throws RemoteException {
            Bundle bundle = null;
            if (routes != null) {
                bundle = new Bundle();
                bundle.putParcelableArray("android.support.v4.media.argument.ROUTE_BUNDLE", (Parcelable[]) routes.toArray(new Bundle[0]));
            }
            this.mIControllerCallback.onEvent("android.support.v4.media.session.event.ON_ROUTES_INFO_CHANGED", bundle);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onGetLibraryRootDone(Bundle rootHints, String rootMediaId, Bundle rootExtra) throws RemoteException {
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onChildrenChanged(String parentId, int itemCount, Bundle extras) throws RemoteException {
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onGetChildrenDone(String parentId, int page, int pageSize, List<MediaItem2> list, Bundle extras) throws RemoteException {
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onGetItemDone(String mediaId, MediaItem2 result) throws RemoteException {
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onSearchResultChanged(String query, int itemCount, Bundle extras) throws RemoteException {
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onGetSearchResultDone(String query, int page, int pageSize, List<MediaItem2> list, Bundle extras) throws RemoteException {
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2.ControllerCb
        public void onDisconnected() throws RemoteException {
            this.mIControllerCallback.onSessionDestroyed();
        }
    }
}
