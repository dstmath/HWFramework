package android.support.v4.media;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.mediacompat.Rating2;
import android.support.v4.media.IMediaSession2;
import android.support.v4.media.MediaController2;
import android.support.v4.media.MediaLibraryService2;
import android.support.v4.media.MediaSession2;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@TargetApi(19)
class MediaSession2Stub extends IMediaSession2.Stub {
    private static final boolean DEBUG = true;
    private static final String TAG = "MediaSession2Stub";
    /* access modifiers changed from: private */
    public static final SparseArray<SessionCommand2> sCommandsForOnCommandRequest = new SparseArray<>();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final ArrayMap<MediaSession2.ControllerInfo, SessionCommandGroup2> mAllowedCommandGroupMap = new ArrayMap<>();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final Set<IBinder> mConnectingControllers = new HashSet();
    final Context mContext;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final ArrayMap<IBinder, MediaSession2.ControllerInfo> mControllers = new ArrayMap<>();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    final MediaSession2.SupportLibraryImpl mSession;

    static final class Controller2Cb extends MediaSession2.ControllerCb {
        private final IMediaController2 mIControllerCallback;

        Controller2Cb(@NonNull IMediaController2 callback) {
            this.mIControllerCallback = callback;
        }

        /* access modifiers changed from: package-private */
        @NonNull
        public IBinder getId() {
            return this.mIControllerCallback.asBinder();
        }

        /* access modifiers changed from: package-private */
        public void onCustomLayoutChanged(List<MediaSession2.CommandButton> layout) throws RemoteException {
            this.mIControllerCallback.onCustomLayoutChanged(MediaUtils2.convertCommandButtonListToBundleList(layout));
        }

        /* access modifiers changed from: package-private */
        public void onPlaybackInfoChanged(MediaController2.PlaybackInfo info) throws RemoteException {
            this.mIControllerCallback.onPlaybackInfoChanged(info.toBundle());
        }

        /* access modifiers changed from: package-private */
        public void onAllowedCommandsChanged(SessionCommandGroup2 commands) throws RemoteException {
            this.mIControllerCallback.onAllowedCommandsChanged(commands.toBundle());
        }

        /* access modifiers changed from: package-private */
        public void onCustomCommand(SessionCommand2 command, Bundle args, ResultReceiver receiver) throws RemoteException {
            this.mIControllerCallback.onCustomCommand(command.toBundle(), args, receiver);
        }

        /* access modifiers changed from: package-private */
        public void onPlayerStateChanged(long eventTimeMs, long positionMs, int playerState) throws RemoteException {
            this.mIControllerCallback.onPlayerStateChanged(eventTimeMs, positionMs, playerState);
        }

        /* access modifiers changed from: package-private */
        public void onPlaybackSpeedChanged(long eventTimeMs, long positionMs, float speed) throws RemoteException {
            this.mIControllerCallback.onPlaybackSpeedChanged(eventTimeMs, positionMs, speed);
        }

        /* access modifiers changed from: package-private */
        public void onBufferingStateChanged(MediaItem2 item, int state, long bufferedPositionMs) throws RemoteException {
            Bundle bundle;
            IMediaController2 iMediaController2 = this.mIControllerCallback;
            if (item == null) {
                bundle = null;
            } else {
                bundle = item.toBundle();
            }
            iMediaController2.onBufferingStateChanged(bundle, state, bufferedPositionMs);
        }

        /* access modifiers changed from: package-private */
        public void onSeekCompleted(long eventTimeMs, long positionMs, long seekPositionMs) throws RemoteException {
            this.mIControllerCallback.onSeekCompleted(eventTimeMs, positionMs, seekPositionMs);
        }

        /* access modifiers changed from: package-private */
        public void onError(int errorCode, Bundle extras) throws RemoteException {
            this.mIControllerCallback.onError(errorCode, extras);
        }

        /* access modifiers changed from: package-private */
        public void onCurrentMediaItemChanged(MediaItem2 item) throws RemoteException {
            this.mIControllerCallback.onCurrentMediaItemChanged(item == null ? null : item.toBundle());
        }

        /* access modifiers changed from: package-private */
        public void onPlaylistChanged(List<MediaItem2> playlist, MediaMetadata2 metadata) throws RemoteException {
            Bundle bundle;
            IMediaController2 iMediaController2 = this.mIControllerCallback;
            List<Bundle> convertMediaItem2ListToBundleList = MediaUtils2.convertMediaItem2ListToBundleList(playlist);
            if (metadata == null) {
                bundle = null;
            } else {
                bundle = metadata.toBundle();
            }
            iMediaController2.onPlaylistChanged(convertMediaItem2ListToBundleList, bundle);
        }

        /* access modifiers changed from: package-private */
        public void onPlaylistMetadataChanged(MediaMetadata2 metadata) throws RemoteException {
            this.mIControllerCallback.onPlaylistMetadataChanged(metadata.toBundle());
        }

        /* access modifiers changed from: package-private */
        public void onShuffleModeChanged(int shuffleMode) throws RemoteException {
            this.mIControllerCallback.onShuffleModeChanged(shuffleMode);
        }

        /* access modifiers changed from: package-private */
        public void onRepeatModeChanged(int repeatMode) throws RemoteException {
            this.mIControllerCallback.onRepeatModeChanged(repeatMode);
        }

        /* access modifiers changed from: package-private */
        public void onRoutesInfoChanged(List<Bundle> routes) throws RemoteException {
            this.mIControllerCallback.onRoutesInfoChanged(routes);
        }

        /* access modifiers changed from: package-private */
        public void onGetLibraryRootDone(Bundle rootHints, String rootMediaId, Bundle rootExtra) throws RemoteException {
            this.mIControllerCallback.onGetLibraryRootDone(rootHints, rootMediaId, rootExtra);
        }

        /* access modifiers changed from: package-private */
        public void onChildrenChanged(String parentId, int itemCount, Bundle extras) throws RemoteException {
            this.mIControllerCallback.onChildrenChanged(parentId, itemCount, extras);
        }

        /* access modifiers changed from: package-private */
        public void onGetChildrenDone(String parentId, int page, int pageSize, List<MediaItem2> result, Bundle extras) throws RemoteException {
            this.mIControllerCallback.onGetChildrenDone(parentId, page, pageSize, MediaUtils2.convertMediaItem2ListToBundleList(result), extras);
        }

        /* access modifiers changed from: package-private */
        public void onGetItemDone(String mediaId, MediaItem2 result) throws RemoteException {
            this.mIControllerCallback.onGetItemDone(mediaId, result == null ? null : result.toBundle());
        }

        /* access modifiers changed from: package-private */
        public void onSearchResultChanged(String query, int itemCount, Bundle extras) throws RemoteException {
            this.mIControllerCallback.onSearchResultChanged(query, itemCount, extras);
        }

        /* access modifiers changed from: package-private */
        public void onGetSearchResultDone(String query, int page, int pageSize, List<MediaItem2> result, Bundle extras) throws RemoteException {
            this.mIControllerCallback.onGetSearchResultDone(query, page, pageSize, MediaUtils2.convertMediaItem2ListToBundleList(result), extras);
        }

        /* access modifiers changed from: package-private */
        public void onDisconnected() throws RemoteException {
            this.mIControllerCallback.onDisconnected();
        }
    }

    @FunctionalInterface
    private interface SessionRunnable {
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

    MediaSession2Stub(MediaSession2.SupportLibraryImpl session) {
        this.mSession = session;
        this.mContext = this.mSession.getContext();
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
    public boolean isAllowedCommand(MediaSession2.ControllerInfo controller, SessionCommand2 command) {
        SessionCommandGroup2 allowedCommands;
        synchronized (this.mLock) {
            allowedCommands = this.mAllowedCommandGroupMap.get(controller);
        }
        if (allowedCommands == null || !allowedCommands.hasCommand(command)) {
            return false;
        }
        return DEBUG;
    }

    /* access modifiers changed from: private */
    public boolean isAllowedCommand(MediaSession2.ControllerInfo controller, int commandCode) {
        SessionCommandGroup2 allowedCommands;
        synchronized (this.mLock) {
            allowedCommands = this.mAllowedCommandGroupMap.get(controller);
        }
        if (allowedCommands == null || !allowedCommands.hasCommand(commandCode)) {
            return false;
        }
        return DEBUG;
    }

    private void onSessionCommand(@NonNull IMediaController2 caller, int commandCode, @NonNull SessionRunnable runnable) {
        onSessionCommandInternal(caller, null, commandCode, runnable);
    }

    private void onSessionCommand(@NonNull IMediaController2 caller, @NonNull SessionCommand2 sessionCommand, @NonNull SessionRunnable runnable) {
        onSessionCommandInternal(caller, sessionCommand, 0, runnable);
    }

    private void onSessionCommandInternal(@NonNull IMediaController2 caller, @Nullable SessionCommand2 sessionCommand, int commandCode, @NonNull SessionRunnable runnable) {
        MediaSession2.ControllerInfo controller;
        synchronized (this.mLock) {
            controller = null;
            if (caller != null) {
                controller = this.mControllers.get(caller.asBinder());
            }
        }
        if (!this.mSession.isClosed() && controller != null) {
            Executor callbackExecutor = this.mSession.getCallbackExecutor();
            final MediaSession2.ControllerInfo controllerInfo = controller;
            final SessionCommand2 sessionCommand2 = sessionCommand;
            final int i = commandCode;
            final SessionRunnable sessionRunnable = runnable;
            AnonymousClass1 r2 = new Runnable() {
                /* JADX WARNING: Code restructure failed: missing block: B:11:0x0026, code lost:
                    if (android.support.v4.media.MediaSession2Stub.access$200(r5.this$0, r4, r5) != false) goto L_0x0029;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:12:0x0028, code lost:
                    return;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:13:0x0029, code lost:
                    r0 = (android.support.v4.media.SessionCommand2) android.support.v4.media.MediaSession2Stub.access$300().get(r5.getCommandCode());
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:15:0x0044, code lost:
                    if (android.support.v4.media.MediaSession2Stub.access$400(r5.this$0, r4, r6) != false) goto L_0x0047;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:16:0x0046, code lost:
                    return;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:17:0x0047, code lost:
                    r0 = (android.support.v4.media.SessionCommand2) android.support.v4.media.MediaSession2Stub.access$300().get(r6);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:18:0x0053, code lost:
                    if (r0 == null) goto L_0x009a;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:20:0x006b, code lost:
                    if (r5.this$0.mSession.getCallback().onCommandRequest(r5.this$0.mSession.getInstance(), r4, r0) != false) goto L_0x009a;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:21:0x006d, code lost:
                    android.util.Log.d(android.support.v4.media.MediaSession2Stub.TAG, "Command (" + r0 + ") from " + r4 + " was rejected by " + r5.this$0.mSession);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:22:0x0099, code lost:
                    return;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
                    r7.run(r4);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a2, code lost:
                    r1 = move-exception;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a3, code lost:
                    android.util.Log.w(android.support.v4.media.MediaSession2Stub.TAG, "Exception in " + r4.toString(), r1);
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:9:0x001a, code lost:
                    if (r5 == null) goto L_0x003a;
                 */
                public void run() {
                    synchronized (MediaSession2Stub.this.mLock) {
                        if (!MediaSession2Stub.this.mControllers.containsValue(controllerInfo)) {
                        }
                    }
                }
            };
            callbackExecutor.execute(r2);
        }
    }

    private void onBrowserCommand(@NonNull IMediaController2 caller, int commandCode, @NonNull SessionRunnable runnable) {
        if (this.mSession instanceof MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl) {
            onSessionCommandInternal(caller, null, commandCode, runnable);
            return;
        }
        throw new RuntimeException("MediaSession2 cannot handle MediaLibrarySession command");
    }

    /* access modifiers changed from: package-private */
    public void removeControllerInfo(MediaSession2.ControllerInfo controller) {
        synchronized (this.mLock) {
            Log.d(TAG, "releasing " + this.mControllers.remove(controller.getId()));
        }
    }

    private void releaseController(IMediaController2 iController) {
        final MediaSession2.ControllerInfo controller;
        synchronized (this.mLock) {
            controller = this.mControllers.remove(iController.asBinder());
            Log.d(TAG, "releasing " + controller);
        }
        if (!this.mSession.isClosed() && controller != null) {
            this.mSession.getCallbackExecutor().execute(new Runnable() {
                public void run() {
                    MediaSession2Stub.this.mSession.getCallback().onDisconnected(MediaSession2Stub.this.mSession.getInstance(), controller);
                }
            });
        }
    }

    public void connect(final IMediaController2 caller, String callingPackage) throws RuntimeException {
        final MediaSession2.ControllerInfo controllerInfo = new MediaSession2.ControllerInfo(callingPackage, Binder.getCallingPid(), Binder.getCallingUid(), new Controller2Cb(caller));
        this.mSession.getCallbackExecutor().execute(new Runnable() {
            public void run() {
                if (!MediaSession2Stub.this.mSession.isClosed()) {
                    synchronized (MediaSession2Stub.this.mLock) {
                        MediaSession2Stub.this.mConnectingControllers.add(controllerInfo.getId());
                    }
                    SessionCommandGroup2 allowedCommands = MediaSession2Stub.this.mSession.getCallback().onConnect(MediaSession2Stub.this.mSession.getInstance(), controllerInfo);
                    if ((allowedCommands != null || controllerInfo.isTrusted()) ? MediaSession2Stub.DEBUG : false) {
                        Log.d(MediaSession2Stub.TAG, "Accepting connection, controllerInfo=" + controllerInfo + " allowedCommands=" + allowedCommands);
                        if (allowedCommands == null) {
                            allowedCommands = new SessionCommandGroup2();
                        }
                        SessionCommandGroup2 allowedCommands2 = allowedCommands;
                        synchronized (MediaSession2Stub.this.mLock) {
                            MediaSession2Stub.this.mConnectingControllers.remove(controllerInfo.getId());
                            MediaSession2Stub.this.mControllers.put(controllerInfo.getId(), controllerInfo);
                            MediaSession2Stub.this.mAllowedCommandGroupMap.put(controllerInfo, allowedCommands2);
                        }
                        int playerState = MediaSession2Stub.this.mSession.getPlayerState();
                        List<MediaItem2> list = null;
                        Bundle currentItem = MediaSession2Stub.this.mSession.getCurrentMediaItem() == null ? null : MediaSession2Stub.this.mSession.getCurrentMediaItem().toBundle();
                        long positionEventTimeMs = SystemClock.elapsedRealtime();
                        long positionMs = MediaSession2Stub.this.mSession.getCurrentPosition();
                        float playbackSpeed = MediaSession2Stub.this.mSession.getPlaybackSpeed();
                        long bufferedPositionMs = MediaSession2Stub.this.mSession.getBufferedPosition();
                        Bundle playbackInfoBundle = MediaSession2Stub.this.mSession.getPlaybackInfo().toBundle();
                        int repeatMode = MediaSession2Stub.this.mSession.getRepeatMode();
                        int shuffleMode = MediaSession2Stub.this.mSession.getShuffleMode();
                        PendingIntent sessionActivity = MediaSession2Stub.this.mSession.getSessionActivity();
                        if (allowedCommands2.hasCommand(18)) {
                            list = MediaSession2Stub.this.mSession.getPlaylist();
                        }
                        List<MediaItem2> playlist = list;
                        List<Bundle> playlistBundle = MediaUtils2.convertMediaItem2ListToBundleList(playlist);
                        if (!MediaSession2Stub.this.mSession.isClosed()) {
                            try {
                                List<MediaItem2> list2 = playlist;
                                try {
                                    caller.onConnected(MediaSession2Stub.this, allowedCommands2.toBundle(), playerState, currentItem, positionEventTimeMs, positionMs, playbackSpeed, bufferedPositionMs, playbackInfoBundle, repeatMode, shuffleMode, playlistBundle, sessionActivity);
                                } catch (RemoteException e) {
                                }
                            } catch (RemoteException e2) {
                                List<MediaItem2> list3 = playlist;
                            }
                        }
                    } else {
                        synchronized (MediaSession2Stub.this.mLock) {
                            MediaSession2Stub.this.mConnectingControllers.remove(controllerInfo.getId());
                        }
                        Log.d(MediaSession2Stub.TAG, "Rejecting connection, controllerInfo=" + controllerInfo);
                        try {
                            caller.onDisconnected();
                        } catch (RemoteException e3) {
                        }
                        SessionCommandGroup2 sessionCommandGroup2 = allowedCommands;
                    }
                }
            }
        });
    }

    public void release(IMediaController2 caller) throws RemoteException {
        releaseController(caller);
    }

    public void setVolumeTo(IMediaController2 caller, final int value, final int flags) throws RuntimeException {
        onSessionCommand(caller, 10, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                VolumeProviderCompat volumeProvider = MediaSession2Stub.this.mSession.getVolumeProvider();
                if (volumeProvider == null) {
                    MediaSessionCompat sessionCompat = MediaSession2Stub.this.mSession.getSessionCompat();
                    if (sessionCompat != null) {
                        sessionCompat.getController().setVolumeTo(value, flags);
                        return;
                    }
                    return;
                }
                volumeProvider.onSetVolumeTo(value);
            }
        });
    }

    public void adjustVolume(IMediaController2 caller, final int direction, final int flags) throws RuntimeException {
        onSessionCommand(caller, 11, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                VolumeProviderCompat volumeProvider = MediaSession2Stub.this.mSession.getVolumeProvider();
                if (volumeProvider == null) {
                    MediaSessionCompat sessionCompat = MediaSession2Stub.this.mSession.getSessionCompat();
                    if (sessionCompat != null) {
                        sessionCompat.getController().adjustVolume(direction, flags);
                        return;
                    }
                    return;
                }
                volumeProvider.onAdjustVolume(direction);
            }
        });
    }

    public void play(IMediaController2 caller) throws RuntimeException {
        onSessionCommand(caller, 1, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.play();
            }
        });
    }

    public void pause(IMediaController2 caller) throws RuntimeException {
        onSessionCommand(caller, 2, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.pause();
            }
        });
    }

    public void reset(IMediaController2 caller) throws RuntimeException {
        onSessionCommand(caller, 3, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.reset();
            }
        });
    }

    public void prepare(IMediaController2 caller) throws RuntimeException {
        onSessionCommand(caller, 6, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.prepare();
            }
        });
    }

    public void fastForward(IMediaController2 caller) {
        onSessionCommand(caller, 7, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onFastForward(MediaSession2Stub.this.mSession.getInstance(), controller);
            }
        });
    }

    public void rewind(IMediaController2 caller) {
        onSessionCommand(caller, 8, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onRewind(MediaSession2Stub.this.mSession.getInstance(), controller);
            }
        });
    }

    public void seekTo(IMediaController2 caller, final long pos) throws RuntimeException {
        onSessionCommand(caller, 9, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.seekTo(pos);
            }
        });
    }

    public void sendCustomCommand(IMediaController2 caller, Bundle commandBundle, final Bundle args, final ResultReceiver receiver) {
        final SessionCommand2 command = SessionCommand2.fromBundle(commandBundle);
        onSessionCommand(caller, SessionCommand2.fromBundle(commandBundle), (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onCustomCommand(MediaSession2Stub.this.mSession.getInstance(), controller, command, args, receiver);
            }
        });
    }

    public void prepareFromUri(IMediaController2 caller, final Uri uri, final Bundle extras) {
        onSessionCommand(caller, 26, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (uri == null) {
                    Log.w(MediaSession2Stub.TAG, "prepareFromUri(): Ignoring null uri from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPrepareFromUri(MediaSession2Stub.this.mSession.getInstance(), controller, uri, extras);
            }
        });
    }

    public void prepareFromSearch(IMediaController2 caller, final String query, final Bundle extras) {
        onSessionCommand(caller, 27, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (TextUtils.isEmpty(query)) {
                    Log.w(MediaSession2Stub.TAG, "prepareFromSearch(): Ignoring empty query from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPrepareFromSearch(MediaSession2Stub.this.mSession.getInstance(), controller, query, extras);
            }
        });
    }

    public void prepareFromMediaId(IMediaController2 caller, final String mediaId, final Bundle extras) {
        onSessionCommand(caller, 25, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (mediaId == null) {
                    Log.w(MediaSession2Stub.TAG, "prepareFromMediaId(): Ignoring null mediaId from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPrepareFromMediaId(MediaSession2Stub.this.mSession.getInstance(), controller, mediaId, extras);
            }
        });
    }

    public void playFromUri(IMediaController2 caller, final Uri uri, final Bundle extras) {
        onSessionCommand(caller, 23, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (uri == null) {
                    Log.w(MediaSession2Stub.TAG, "playFromUri(): Ignoring null uri from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPlayFromUri(MediaSession2Stub.this.mSession.getInstance(), controller, uri, extras);
            }
        });
    }

    public void playFromSearch(IMediaController2 caller, final String query, final Bundle extras) {
        onSessionCommand(caller, 24, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (TextUtils.isEmpty(query)) {
                    Log.w(MediaSession2Stub.TAG, "playFromSearch(): Ignoring empty query from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPlayFromSearch(MediaSession2Stub.this.mSession.getInstance(), controller, query, extras);
            }
        });
    }

    public void playFromMediaId(IMediaController2 caller, final String mediaId, final Bundle extras) {
        onSessionCommand(caller, 22, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (mediaId == null) {
                    Log.w(MediaSession2Stub.TAG, "playFromMediaId(): Ignoring null mediaId from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getCallback().onPlayFromMediaId(MediaSession2Stub.this.mSession.getInstance(), controller, mediaId, extras);
            }
        });
    }

    public void setRating(IMediaController2 caller, final String mediaId, final Bundle ratingBundle) {
        onSessionCommand(caller, 28, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (mediaId == null) {
                    Log.w(MediaSession2Stub.TAG, "setRating(): Ignoring null mediaId from " + controller);
                } else if (ratingBundle == null) {
                    Log.w(MediaSession2Stub.TAG, "setRating(): Ignoring null ratingBundle from " + controller);
                } else {
                    Rating2 rating = Rating2.fromBundle(ratingBundle);
                    if (rating != null) {
                        MediaSession2Stub.this.mSession.getCallback().onSetRating(MediaSession2Stub.this.mSession.getInstance(), controller, mediaId, rating);
                    } else if (ratingBundle == null) {
                        Log.w(MediaSession2Stub.TAG, "setRating(): Ignoring null rating from " + controller);
                    }
                }
            }
        });
    }

    public void setPlaybackSpeed(IMediaController2 caller, final float speed) {
        onSessionCommand(caller, 39, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().setPlaybackSpeed(speed);
            }
        });
    }

    public void setPlaylist(IMediaController2 caller, final List<Bundle> playlist, final Bundle metadata) {
        onSessionCommand(caller, 19, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (playlist == null) {
                    Log.w(MediaSession2Stub.TAG, "setPlaylist(): Ignoring null playlist from " + controller);
                    return;
                }
                MediaSession2Stub.this.mSession.getInstance().setPlaylist(MediaUtils2.convertBundleListToMediaItem2List(playlist), MediaMetadata2.fromBundle(metadata));
            }
        });
    }

    public void updatePlaylistMetadata(IMediaController2 caller, final Bundle metadata) {
        onSessionCommand(caller, 21, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().updatePlaylistMetadata(MediaMetadata2.fromBundle(metadata));
            }
        });
    }

    public void addPlaylistItem(IMediaController2 caller, final int index, final Bundle mediaItem) {
        onSessionCommand(caller, 15, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().addPlaylistItem(index, MediaItem2.fromBundle(mediaItem, null));
            }
        });
    }

    public void removePlaylistItem(IMediaController2 caller, final Bundle mediaItem) {
        onSessionCommand(caller, 16, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().removePlaylistItem(MediaItem2.fromBundle(mediaItem));
            }
        });
    }

    public void replacePlaylistItem(IMediaController2 caller, final int index, final Bundle mediaItem) {
        onSessionCommand(caller, 17, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().replacePlaylistItem(index, MediaItem2.fromBundle(mediaItem, null));
            }
        });
    }

    public void skipToPlaylistItem(IMediaController2 caller, final Bundle mediaItem) {
        onSessionCommand(caller, 12, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (mediaItem == null) {
                    Log.w(MediaSession2Stub.TAG, "skipToPlaylistItem(): Ignoring null mediaItem from " + controller);
                }
                MediaSession2Stub.this.mSession.getInstance().skipToPlaylistItem(MediaItem2.fromBundle(mediaItem));
            }
        });
    }

    public void skipToPreviousItem(IMediaController2 caller) {
        onSessionCommand(caller, 5, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().skipToPreviousItem();
            }
        });
    }

    public void skipToNextItem(IMediaController2 caller) {
        onSessionCommand(caller, 4, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().skipToNextItem();
            }
        });
    }

    public void setRepeatMode(IMediaController2 caller, final int repeatMode) {
        onSessionCommand(caller, 14, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().setRepeatMode(repeatMode);
            }
        });
    }

    public void setShuffleMode(IMediaController2 caller, final int shuffleMode) {
        onSessionCommand(caller, 13, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getInstance().setShuffleMode(shuffleMode);
            }
        });
    }

    public void subscribeRoutesInfo(IMediaController2 caller) {
        onSessionCommand(caller, 36, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onSubscribeRoutesInfo(MediaSession2Stub.this.mSession.getInstance(), controller);
            }
        });
    }

    public void unsubscribeRoutesInfo(IMediaController2 caller) {
        onSessionCommand(caller, 37, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onUnsubscribeRoutesInfo(MediaSession2Stub.this.mSession.getInstance(), controller);
            }
        });
    }

    public void selectRoute(IMediaController2 caller, final Bundle route) {
        onSessionCommand(caller, 37, (SessionRunnable) new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.mSession.getCallback().onSelectRoute(MediaSession2Stub.this.mSession.getInstance(), controller, route);
            }
        });
    }

    /* access modifiers changed from: private */
    public MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl getLibrarySession() {
        if (this.mSession instanceof MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl) {
            return (MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl) this.mSession;
        }
        throw new RuntimeException("Session cannot be casted to library session");
    }

    public void getLibraryRoot(IMediaController2 caller, final Bundle rootHints) throws RuntimeException {
        onBrowserCommand(caller, 31, new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                MediaSession2Stub.this.getLibrarySession().onGetLibraryRootOnExecutor(controller, rootHints);
            }
        });
    }

    public void getItem(IMediaController2 caller, final String mediaId) throws RuntimeException {
        onBrowserCommand(caller, 30, new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (mediaId == null) {
                    Log.w(MediaSession2Stub.TAG, "getItem(): Ignoring null mediaId from " + controller);
                    return;
                }
                MediaSession2Stub.this.getLibrarySession().onGetItemOnExecutor(controller, mediaId);
            }
        });
    }

    public void getChildren(IMediaController2 caller, String parentId, int page, int pageSize, Bundle extras) throws RuntimeException {
        final String str = parentId;
        final int i = page;
        final int i2 = pageSize;
        final Bundle bundle = extras;
        AnonymousClass37 r0 = new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (str == null) {
                    Log.w(MediaSession2Stub.TAG, "getChildren(): Ignoring null parentId from " + controller);
                } else if (i < 1 || i2 < 1) {
                    Log.w(MediaSession2Stub.TAG, "getChildren(): Ignoring page nor pageSize less than 1 from " + controller);
                } else {
                    MediaSession2Stub.this.getLibrarySession().onGetChildrenOnExecutor(controller, str, i, i2, bundle);
                }
            }
        };
        onBrowserCommand(caller, 29, r0);
    }

    public void search(IMediaController2 caller, final String query, final Bundle extras) {
        onBrowserCommand(caller, 33, new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (TextUtils.isEmpty(query)) {
                    Log.w(MediaSession2Stub.TAG, "search(): Ignoring empty query from " + controller);
                    return;
                }
                MediaSession2Stub.this.getLibrarySession().onSearchOnExecutor(controller, query, extras);
            }
        });
    }

    public void getSearchResult(IMediaController2 caller, String query, int page, int pageSize, Bundle extras) {
        final String str = query;
        final int i = page;
        final int i2 = pageSize;
        final Bundle bundle = extras;
        AnonymousClass39 r0 = new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (TextUtils.isEmpty(str)) {
                    Log.w(MediaSession2Stub.TAG, "getSearchResult(): Ignoring empty query from " + controller);
                } else if (i < 1 || i2 < 1) {
                    Log.w(MediaSession2Stub.TAG, "getSearchResult(): Ignoring page nor pageSize less than 1  from " + controller);
                } else {
                    MediaSession2Stub.this.getLibrarySession().onGetSearchResultOnExecutor(controller, str, i, i2, bundle);
                }
            }
        };
        onBrowserCommand(caller, 32, r0);
    }

    public void subscribe(IMediaController2 caller, final String parentId, final Bundle option) {
        onBrowserCommand(caller, 34, new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (parentId == null) {
                    Log.w(MediaSession2Stub.TAG, "subscribe(): Ignoring null parentId from " + controller);
                    return;
                }
                MediaSession2Stub.this.getLibrarySession().onSubscribeOnExecutor(controller, parentId, option);
            }
        });
    }

    public void unsubscribe(IMediaController2 caller, final String parentId) {
        onBrowserCommand(caller, 35, new SessionRunnable() {
            public void run(MediaSession2.ControllerInfo controller) throws RemoteException {
                if (parentId == null) {
                    Log.w(MediaSession2Stub.TAG, "unsubscribe(): Ignoring null parentId from " + controller);
                    return;
                }
                MediaSession2Stub.this.getLibrarySession().onUnsubscribeOnExecutor(controller, parentId);
            }
        });
    }
}
