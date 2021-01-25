package android.support.v4.media;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.BaseMediaPlayer;
import android.support.v4.media.MediaController2;
import android.support.v4.media.MediaMetadata2;
import android.support.v4.media.MediaPlaylistAgent;
import android.support.v4.media.MediaSession2;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.util.ObjectsCompat;
import android.text.TextUtils;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;

/* access modifiers changed from: package-private */
@TargetApi(19)
public class MediaSession2ImplBase implements MediaSession2.SupportLibraryImpl {
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    static final String TAG = "MS2ImplBase";
    private final AudioFocusHandler mAudioFocusHandler;
    private final AudioManager mAudioManager;
    private final MediaSession2.SessionCallback mCallback;
    private final Executor mCallbackExecutor;
    private final Context mContext;
    @GuardedBy("mLock")
    private MediaSession2.OnDataSourceMissingHelper mDsmHelper;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private final MediaSession2 mInstance;
    final Object mLock = new Object();
    @GuardedBy("mLock")
    private MediaController2.PlaybackInfo mPlaybackInfo;
    @GuardedBy("mLock")
    private BaseMediaPlayer mPlayer;
    private final BaseMediaPlayer.PlayerEventCallback mPlayerEventCallback;
    @GuardedBy("mLock")
    private MediaPlaylistAgent mPlaylistAgent;
    private final MediaPlaylistAgent.PlaylistEventCallback mPlaylistEventCallback;
    private final MediaSession2Stub mSession2Stub;
    private final PendingIntent mSessionActivity;
    private final MediaSessionCompat mSessionCompat;
    private final MediaSessionLegacyStub mSessionLegacyStub;
    @GuardedBy("mLock")
    private SessionPlaylistAgentImplBase mSessionPlaylistAgent;
    private final SessionToken2 mSessionToken;
    @GuardedBy("mLock")
    private VolumeProviderCompat mVolumeProvider;

    /* access modifiers changed from: package-private */
    @FunctionalInterface
    public interface NotifyRunnable {
        void run(MediaSession2.ControllerCb controllerCb) throws RemoteException;
    }

    MediaSession2ImplBase(MediaSession2 instance, Context context, String id, BaseMediaPlayer player, MediaPlaylistAgent playlistAgent, VolumeProviderCompat volumeProvider, PendingIntent sessionActivity, Executor callbackExecutor, MediaSession2.SessionCallback callback) {
        this.mContext = context;
        this.mInstance = instance;
        this.mHandlerThread = new HandlerThread("MediaController2_Thread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mSession2Stub = new MediaSession2Stub(this);
        this.mSessionLegacyStub = new MediaSessionLegacyStub(this);
        this.mSessionActivity = sessionActivity;
        this.mCallback = callback;
        this.mCallbackExecutor = callbackExecutor;
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mPlayerEventCallback = new MyPlayerEventCallback();
        this.mPlaylistEventCallback = new MyPlaylistEventCallback();
        this.mAudioFocusHandler = new AudioFocusHandler(context, getInstance());
        String libraryService = getServiceName(context, MediaLibraryService2.SERVICE_INTERFACE, id);
        String sessionService = getServiceName(context, MediaSessionService2.SERVICE_INTERFACE, id);
        if (sessionService == null || libraryService == null) {
            if (libraryService != null) {
                this.mSessionToken = new SessionToken2(new SessionToken2ImplBase(Process.myUid(), 2, context.getPackageName(), libraryService, id, this.mSession2Stub));
            } else if (sessionService != null) {
                this.mSessionToken = new SessionToken2(new SessionToken2ImplBase(Process.myUid(), 1, context.getPackageName(), sessionService, id, this.mSession2Stub));
            } else {
                this.mSessionToken = new SessionToken2(new SessionToken2ImplBase(Process.myUid(), 0, context.getPackageName(), null, id, this.mSession2Stub));
            }
            this.mSessionCompat = new MediaSessionCompat(context, id, this.mSessionToken);
            this.mSessionCompat.setCallback(this.mSessionLegacyStub, this.mHandler);
            this.mSessionCompat.setSessionActivity(sessionActivity);
            updatePlayer(player, playlistAgent, volumeProvider);
            return;
        }
        throw new IllegalArgumentException("Ambiguous session type. Multiple session services define the same id=" + id);
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public void updatePlayer(@NonNull BaseMediaPlayer player, @Nullable MediaPlaylistAgent playlistAgent, @Nullable VolumeProviderCompat volumeProvider) {
        boolean hasPlaybackInfoChanged;
        Throwable th;
        boolean hasPlaybackInfoChanged2;
        boolean hasPlayerChanged;
        boolean hasAgentChanged;
        BaseMediaPlayer oldPlayer;
        MediaPlaylistAgent oldAgent;
        if (player != null) {
            final MediaController2.PlaybackInfo info = createPlaybackInfo(volumeProvider, player.getAudioAttributes());
            synchronized (this.mLock) {
                hasPlaybackInfoChanged = DEBUG;
                try {
                    hasPlayerChanged = this.mPlayer != player;
                    try {
                        hasAgentChanged = this.mPlaylistAgent != playlistAgent;
                    } catch (Throwable th2) {
                        th = th2;
                        hasPlaybackInfoChanged2 = false;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        throw th;
                    }
                    try {
                        if (this.mPlaybackInfo != info) {
                            hasPlaybackInfoChanged = true;
                        }
                        oldPlayer = this.mPlayer;
                    } catch (Throwable th4) {
                        th = th4;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                    try {
                        oldAgent = this.mPlaylistAgent;
                        this.mPlayer = player;
                        if (playlistAgent == null) {
                            this.mSessionPlaylistAgent = new SessionPlaylistAgentImplBase(this, this.mPlayer);
                            if (this.mDsmHelper != null) {
                                this.mSessionPlaylistAgent.setOnDataSourceMissingHelper(this.mDsmHelper);
                            }
                            playlistAgent = this.mSessionPlaylistAgent;
                        }
                        this.mPlaylistAgent = playlistAgent;
                        this.mVolumeProvider = volumeProvider;
                        this.mPlaybackInfo = info;
                    } catch (Throwable th5) {
                        th = th5;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    hasPlaybackInfoChanged2 = false;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
            if (volumeProvider == null) {
                this.mSessionCompat.setPlaybackToLocal(getLegacyStreamType(player.getAudioAttributes()));
            }
            if (player != oldPlayer) {
                player.registerPlayerEventCallback(this.mCallbackExecutor, this.mPlayerEventCallback);
                if (oldPlayer != null) {
                    oldPlayer.unregisterPlayerEventCallback(this.mPlayerEventCallback);
                }
            }
            if (playlistAgent != oldAgent) {
                playlistAgent.registerPlaylistEventCallback(this.mCallbackExecutor, this.mPlaylistEventCallback);
                if (oldAgent != null) {
                    oldAgent.unregisterPlaylistEventCallback(this.mPlaylistEventCallback);
                }
            }
            if (oldPlayer != null) {
                if (hasAgentChanged) {
                    notifyAgentUpdatedNotLocked(oldAgent);
                }
                if (hasPlayerChanged) {
                    notifyPlayerUpdatedNotLocked(oldPlayer);
                }
                if (hasPlaybackInfoChanged) {
                    notifyToAllControllers(new NotifyRunnable() {
                        /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass1 */

                        @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                        public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                            callback.onPlaybackInfoChanged(info);
                        }
                    });
                    return;
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException("player shouldn't be null");
    }

    /* JADX INFO: Multiple debug info for r0v3 android.support.v4.media.MediaController2$PlaybackInfo: [D('stream' int), D('info' android.support.v4.media.MediaController2$PlaybackInfo)] */
    private MediaController2.PlaybackInfo createPlaybackInfo(VolumeProviderCompat volumeProvider, AudioAttributesCompat attrs) {
        if (volumeProvider != null) {
            return MediaController2.PlaybackInfo.createPlaybackInfo(2, attrs, volumeProvider.getVolumeControl(), volumeProvider.getMaxVolume(), volumeProvider.getCurrentVolume());
        }
        int stream = getLegacyStreamType(attrs);
        int controlType = 2;
        if (Build.VERSION.SDK_INT >= 21 && this.mAudioManager.isVolumeFixed()) {
            controlType = 0;
        }
        return MediaController2.PlaybackInfo.createPlaybackInfo(1, attrs, controlType, this.mAudioManager.getStreamMaxVolume(stream), this.mAudioManager.getStreamVolume(stream));
    }

    private int getLegacyStreamType(@Nullable AudioAttributesCompat attrs) {
        int stream;
        if (attrs == null || (stream = attrs.getLegacyStreamType()) == Integer.MIN_VALUE) {
            return 3;
        }
        return stream;
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        synchronized (this.mLock) {
            if (this.mPlayer != null) {
                this.mAudioFocusHandler.close();
                this.mPlayer.unregisterPlayerEventCallback(this.mPlayerEventCallback);
                this.mPlayer = null;
                this.mSessionCompat.release();
                notifyToAllControllers(new NotifyRunnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass2 */

                    @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                    public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                        callback.onDisconnected();
                    }
                });
                this.mHandler.removeCallbacksAndMessages(null);
                if (this.mHandlerThread.isAlive()) {
                    if (Build.VERSION.SDK_INT >= 18) {
                        this.mHandlerThread.quitSafely();
                    } else {
                        this.mHandlerThread.quit();
                    }
                }
            }
        }
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    @NonNull
    public BaseMediaPlayer getPlayer() {
        BaseMediaPlayer baseMediaPlayer;
        synchronized (this.mLock) {
            baseMediaPlayer = this.mPlayer;
        }
        return baseMediaPlayer;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    @NonNull
    public MediaPlaylistAgent getPlaylistAgent() {
        MediaPlaylistAgent mediaPlaylistAgent;
        synchronized (this.mLock) {
            mediaPlaylistAgent = this.mPlaylistAgent;
        }
        return mediaPlaylistAgent;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    @Nullable
    public VolumeProviderCompat getVolumeProvider() {
        VolumeProviderCompat volumeProviderCompat;
        synchronized (this.mLock) {
            volumeProviderCompat = this.mVolumeProvider;
        }
        return volumeProviderCompat;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    @NonNull
    public SessionToken2 getToken() {
        return this.mSessionToken;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    @NonNull
    public List<MediaSession2.ControllerInfo> getConnectedControllers() {
        return this.mSession2Stub.getConnectedControllers();
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public void setCustomLayout(@NonNull MediaSession2.ControllerInfo controller, @NonNull final List<MediaSession2.CommandButton> layout) {
        if (controller == null) {
            throw new IllegalArgumentException("controller shouldn't be null");
        } else if (layout != null) {
            notifyToController(controller, new NotifyRunnable() {
                /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass3 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onCustomLayoutChanged(layout);
                }
            });
        } else {
            throw new IllegalArgumentException("layout shouldn't be null");
        }
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public void setAllowedCommands(@NonNull MediaSession2.ControllerInfo controller, @NonNull final SessionCommandGroup2 commands) {
        if (controller == null) {
            throw new IllegalArgumentException("controller shouldn't be null");
        } else if (commands != null) {
            this.mSession2Stub.setAllowedCommands(controller, commands);
            notifyToController(controller, new NotifyRunnable() {
                /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass4 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onAllowedCommandsChanged(commands);
                }
            });
        } else {
            throw new IllegalArgumentException("commands shouldn't be null");
        }
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public void sendCustomCommand(@NonNull final SessionCommand2 command, @Nullable final Bundle args) {
        if (command != null) {
            notifyToAllControllers(new NotifyRunnable() {
                /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass5 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onCustomCommand(command, args, null);
                }
            });
            return;
        }
        throw new IllegalArgumentException("command shouldn't be null");
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public void sendCustomCommand(@NonNull MediaSession2.ControllerInfo controller, @NonNull final SessionCommand2 command, @Nullable final Bundle args, @Nullable final ResultReceiver receiver) {
        if (controller == null) {
            throw new IllegalArgumentException("controller shouldn't be null");
        } else if (command != null) {
            notifyToController(controller, new NotifyRunnable() {
                /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass6 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onCustomCommand(command, args, receiver);
                }
            });
        } else {
            throw new IllegalArgumentException("command shouldn't be null");
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void play() {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            if (this.mAudioFocusHandler.onPlayRequested()) {
                player.play();
            } else {
                Log.w(TAG, "play() wouldn't be called because of the failure in audio focus");
            }
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void pause() {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            if (this.mAudioFocusHandler.onPauseRequested()) {
                player.pause();
            } else {
                Log.w(TAG, "pause() wouldn't be called of the failure in audio focus");
            }
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void reset() {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            player.reset();
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void prepare() {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            player.prepare();
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void seekTo(long pos) {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            player.seekTo(pos);
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlayer
    public void skipForward() {
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlayer
    public void skipBackward() {
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlayer
    public void notifyError(final int errorCode, @Nullable final Bundle extras) {
        notifyToAllControllers(new NotifyRunnable() {
            /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass7 */

            @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                callback.onError(errorCode, extras);
            }
        });
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public void notifyRoutesInfoChanged(@NonNull MediaSession2.ControllerInfo controller, @Nullable final List<Bundle> routes) {
        notifyToController(controller, new NotifyRunnable() {
            /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass8 */

            @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                callback.onRoutesInfoChanged(routes);
            }
        });
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public int getPlayerState() {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            return player.getPlayerState();
        }
        if (!DEBUG) {
            return 3;
        }
        Log.d(TAG, "API calls after the close()", new IllegalStateException());
        return 3;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public long getCurrentPosition() {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            return player.getCurrentPosition();
        }
        if (!DEBUG) {
            return -1;
        }
        Log.d(TAG, "API calls after the close()", new IllegalStateException());
        return -1;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public long getDuration() {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            return player.getDuration();
        }
        if (!DEBUG) {
            return -1;
        }
        Log.d(TAG, "API calls after the close()", new IllegalStateException());
        return -1;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public long getBufferedPosition() {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            return player.getBufferedPosition();
        }
        if (!DEBUG) {
            return -1;
        }
        Log.d(TAG, "API calls after the close()", new IllegalStateException());
        return -1;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public int getBufferingState() {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            return player.getBufferingState();
        }
        if (!DEBUG) {
            return 0;
        }
        Log.d(TAG, "API calls after the close()", new IllegalStateException());
        return 0;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public float getPlaybackSpeed() {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            return player.getPlaybackSpeed();
        }
        if (!DEBUG) {
            return 1.0f;
        }
        Log.d(TAG, "API calls after the close()", new IllegalStateException());
        return 1.0f;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaybackControl
    public void setPlaybackSpeed(float speed) {
        Throwable th;
        BaseMediaPlayer player;
        synchronized (this.mLock) {
            try {
                player = this.mPlayer;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (player != null) {
            player.setPlaybackSpeed(speed);
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void setOnDataSourceMissingHelper(@NonNull MediaSession2.OnDataSourceMissingHelper helper) {
        if (helper != null) {
            synchronized (this.mLock) {
                this.mDsmHelper = helper;
                if (this.mSessionPlaylistAgent != null) {
                    this.mSessionPlaylistAgent.setOnDataSourceMissingHelper(helper);
                }
            }
            return;
        }
        throw new IllegalArgumentException("helper shouldn't be null");
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void clearOnDataSourceMissingHelper() {
        synchronized (this.mLock) {
            this.mDsmHelper = null;
            if (this.mSessionPlaylistAgent != null) {
                this.mSessionPlaylistAgent.clearOnDataSourceMissingHelper();
            }
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public List<MediaItem2> getPlaylist() {
        Throwable th;
        MediaPlaylistAgent agent;
        synchronized (this.mLock) {
            try {
                agent = this.mPlaylistAgent;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (agent != null) {
            return agent.getPlaylist();
        }
        if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
        return null;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 metadata) {
        Throwable th;
        MediaPlaylistAgent agent;
        if (list != null) {
            synchronized (this.mLock) {
                try {
                    agent = this.mPlaylistAgent;
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
            if (agent != null) {
                agent.setPlaylist(list, metadata);
            } else if (DEBUG) {
                Log.d(TAG, "API calls after the close()", new IllegalStateException());
            }
        } else {
            throw new IllegalArgumentException("list shouldn't be null");
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void skipToPlaylistItem(@NonNull MediaItem2 item) {
        Throwable th;
        MediaPlaylistAgent agent;
        if (item != null) {
            synchronized (this.mLock) {
                try {
                    agent = this.mPlaylistAgent;
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
            if (agent != null) {
                agent.skipToPlaylistItem(item);
            } else if (DEBUG) {
                Log.d(TAG, "API calls after the close()", new IllegalStateException());
            }
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void skipToPreviousItem() {
        Throwable th;
        MediaPlaylistAgent agent;
        synchronized (this.mLock) {
            try {
                agent = this.mPlaylistAgent;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (agent != null) {
            agent.skipToPreviousItem();
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void skipToNextItem() {
        Throwable th;
        MediaPlaylistAgent agent;
        synchronized (this.mLock) {
            try {
                agent = this.mPlaylistAgent;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (agent != null) {
            agent.skipToNextItem();
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public MediaMetadata2 getPlaylistMetadata() {
        Throwable th;
        MediaPlaylistAgent agent;
        synchronized (this.mLock) {
            try {
                agent = this.mPlaylistAgent;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (agent != null) {
            return agent.getPlaylistMetadata();
        }
        if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
        return null;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void addPlaylistItem(int index, @NonNull MediaItem2 item) {
        Throwable th;
        MediaPlaylistAgent agent;
        if (index < 0) {
            throw new IllegalArgumentException("index shouldn't be negative");
        } else if (item != null) {
            synchronized (this.mLock) {
                try {
                    agent = this.mPlaylistAgent;
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
            if (agent != null) {
                agent.addPlaylistItem(index, item);
            } else if (DEBUG) {
                Log.d(TAG, "API calls after the close()", new IllegalStateException());
            }
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void removePlaylistItem(@NonNull MediaItem2 item) {
        Throwable th;
        MediaPlaylistAgent agent;
        if (item != null) {
            synchronized (this.mLock) {
                try {
                    agent = this.mPlaylistAgent;
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
            if (agent != null) {
                agent.removePlaylistItem(item);
            } else if (DEBUG) {
                Log.d(TAG, "API calls after the close()", new IllegalStateException());
            }
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void replacePlaylistItem(int index, @NonNull MediaItem2 item) {
        Throwable th;
        MediaPlaylistAgent agent;
        if (index < 0) {
            throw new IllegalArgumentException("index shouldn't be negative");
        } else if (item != null) {
            synchronized (this.mLock) {
                try {
                    agent = this.mPlaylistAgent;
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
            if (agent != null) {
                agent.replacePlaylistItem(index, item);
            } else if (DEBUG) {
                Log.d(TAG, "API calls after the close()", new IllegalStateException());
            }
        } else {
            throw new IllegalArgumentException("item shouldn't be null");
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public MediaItem2 getCurrentMediaItem() {
        Throwable th;
        MediaPlaylistAgent agent;
        synchronized (this.mLock) {
            try {
                agent = this.mPlaylistAgent;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (agent != null) {
            return agent.getCurrentMediaItem();
        }
        if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
        return null;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void updatePlaylistMetadata(@Nullable MediaMetadata2 metadata) {
        Throwable th;
        MediaPlaylistAgent agent;
        synchronized (this.mLock) {
            try {
                agent = this.mPlaylistAgent;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (agent != null) {
            agent.updatePlaylistMetadata(metadata);
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public int getRepeatMode() {
        Throwable th;
        MediaPlaylistAgent agent;
        synchronized (this.mLock) {
            try {
                agent = this.mPlaylistAgent;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (agent != null) {
            return agent.getRepeatMode();
        }
        if (!DEBUG) {
            return 0;
        }
        Log.d(TAG, "API calls after the close()", new IllegalStateException());
        return 0;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void setRepeatMode(int repeatMode) {
        Throwable th;
        MediaPlaylistAgent agent;
        synchronized (this.mLock) {
            try {
                agent = this.mPlaylistAgent;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (agent != null) {
            agent.setRepeatMode(repeatMode);
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public int getShuffleMode() {
        Throwable th;
        MediaPlaylistAgent agent;
        synchronized (this.mLock) {
            try {
                agent = this.mPlaylistAgent;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (agent != null) {
            return agent.getShuffleMode();
        }
        if (!DEBUG) {
            return 0;
        }
        Log.d(TAG, "API calls after the close()", new IllegalStateException());
        return 0;
    }

    @Override // android.support.v4.media.MediaInterface2.SessionPlaylistControl
    public void setShuffleMode(int shuffleMode) {
        Throwable th;
        MediaPlaylistAgent agent;
        synchronized (this.mLock) {
            try {
                agent = this.mPlaylistAgent;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (agent != null) {
            agent.setShuffleMode(shuffleMode);
        } else if (DEBUG) {
            Log.d(TAG, "API calls after the close()", new IllegalStateException());
        }
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    @NonNull
    public MediaSession2 getInstance() {
        return this.mInstance;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    @NonNull
    public IBinder getSessionBinder() {
        return this.mSession2Stub.asBinder();
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public Context getContext() {
        return this.mContext;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public Executor getCallbackExecutor() {
        return this.mCallbackExecutor;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public MediaSession2.SessionCallback getCallback() {
        return this.mCallback;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public MediaSessionCompat getSessionCompat() {
        return this.mSessionCompat;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public AudioFocusHandler getAudioFocusHandler() {
        return this.mAudioFocusHandler;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public boolean isClosed() {
        return !this.mHandlerThread.isAlive();
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public PlaybackStateCompat getPlaybackStateCompat() {
        PlaybackStateCompat build;
        synchronized (this.mLock) {
            build = new PlaybackStateCompat.Builder().setState(MediaUtils2.convertToPlaybackStateCompatState(getPlayerState(), getBufferingState()), getCurrentPosition(), getPlaybackSpeed()).setActions(3670015).setBufferedPosition(getBufferedPosition()).build();
        }
        return build;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public MediaController2.PlaybackInfo getPlaybackInfo() {
        MediaController2.PlaybackInfo playbackInfo;
        synchronized (this.mLock) {
            playbackInfo = this.mPlaybackInfo;
        }
        return playbackInfo;
    }

    @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
    public PendingIntent getSessionActivity() {
        return this.mSessionActivity;
    }

    private static String getServiceName(Context context, String serviceAction, String id) {
        PackageManager manager = context.getPackageManager();
        Intent serviceIntent = new Intent(serviceAction);
        serviceIntent.setPackage(context.getPackageName());
        List<ResolveInfo> services = manager.queryIntentServices(serviceIntent, 128);
        String serviceName = null;
        if (services != null) {
            for (int i = 0; i < services.size(); i++) {
                String serviceId = SessionToken2.getSessionId(services.get(i));
                if (!(serviceId == null || !TextUtils.equals(id, serviceId) || services.get(i).serviceInfo == null)) {
                    if (serviceName == null) {
                        serviceName = services.get(i).serviceInfo.name;
                    } else {
                        throw new IllegalArgumentException("Ambiguous session type. Multiple session services define the same id=" + id);
                    }
                }
            }
        }
        return serviceName;
    }

    private void notifyAgentUpdatedNotLocked(MediaPlaylistAgent oldAgent) {
        List<MediaItem2> oldPlaylist = oldAgent.getPlaylist();
        final List<MediaItem2> newPlaylist = getPlaylist();
        if (!ObjectsCompat.equals(oldPlaylist, newPlaylist)) {
            notifyToAllControllers(new NotifyRunnable() {
                /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass9 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onPlaylistChanged(newPlaylist, MediaSession2ImplBase.this.getPlaylistMetadata());
                }
            });
        } else {
            MediaMetadata2 oldMetadata = oldAgent.getPlaylistMetadata();
            final MediaMetadata2 newMetadata = getPlaylistMetadata();
            if (!ObjectsCompat.equals(oldMetadata, newMetadata)) {
                notifyToAllControllers(new NotifyRunnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass10 */

                    @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                    public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                        callback.onPlaylistMetadataChanged(newMetadata);
                    }
                });
            }
        }
        MediaItem2 oldCurrentItem = oldAgent.getCurrentMediaItem();
        final MediaItem2 newCurrentItem = getCurrentMediaItem();
        if (!ObjectsCompat.equals(oldCurrentItem, newCurrentItem)) {
            notifyToAllControllers(new NotifyRunnable() {
                /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass11 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onCurrentMediaItemChanged(newCurrentItem);
                }
            });
        }
        final int repeatMode = getRepeatMode();
        if (oldAgent.getRepeatMode() != repeatMode) {
            notifyToAllControllers(new NotifyRunnable() {
                /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass12 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onRepeatModeChanged(repeatMode);
                }
            });
        }
        final int shuffleMode = getShuffleMode();
        if (oldAgent.getShuffleMode() != shuffleMode) {
            notifyToAllControllers(new NotifyRunnable() {
                /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass13 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onShuffleModeChanged(shuffleMode);
                }
            });
        }
    }

    private void notifyPlayerUpdatedNotLocked(BaseMediaPlayer oldPlayer) {
        final long currentTimeMs = SystemClock.elapsedRealtime();
        final long positionMs = getCurrentPosition();
        final int playerState = getPlayerState();
        notifyToAllControllers(new NotifyRunnable() {
            /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass14 */

            @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                callback.onPlayerStateChanged(currentTimeMs, positionMs, playerState);
            }
        });
        final MediaItem2 item = getCurrentMediaItem();
        if (item != null) {
            final int bufferingState = getBufferingState();
            final long bufferedPositionMs = getBufferedPosition();
            notifyToAllControllers(new NotifyRunnable() {
                /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass15 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onBufferingStateChanged(item, bufferingState, bufferedPositionMs);
                }
            });
        }
        final float speed = getPlaybackSpeed();
        if (speed != oldPlayer.getPlaybackSpeed()) {
            notifyToAllControllers(new NotifyRunnable() {
                /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass16 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onPlaybackSpeedChanged(currentTimeMs, positionMs, speed);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPlaylistChangedOnExecutor(MediaPlaylistAgent playlistAgent, final List<MediaItem2> list, final MediaMetadata2 metadata) {
        synchronized (this.mLock) {
            if (playlistAgent == this.mPlaylistAgent) {
                this.mCallback.onPlaylistChanged(this.mInstance, playlistAgent, list, metadata);
                notifyToAllControllers(new NotifyRunnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass17 */

                    @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                    public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                        callback.onPlaylistChanged(list, metadata);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPlaylistMetadataChangedOnExecutor(MediaPlaylistAgent playlistAgent, final MediaMetadata2 metadata) {
        synchronized (this.mLock) {
            if (playlistAgent == this.mPlaylistAgent) {
                this.mCallback.onPlaylistMetadataChanged(this.mInstance, playlistAgent, metadata);
                notifyToAllControllers(new NotifyRunnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass18 */

                    @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                    public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                        callback.onPlaylistMetadataChanged(metadata);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyRepeatModeChangedOnExecutor(MediaPlaylistAgent playlistAgent, final int repeatMode) {
        synchronized (this.mLock) {
            if (playlistAgent == this.mPlaylistAgent) {
                this.mCallback.onRepeatModeChanged(this.mInstance, playlistAgent, repeatMode);
                notifyToAllControllers(new NotifyRunnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass19 */

                    @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                    public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                        callback.onRepeatModeChanged(repeatMode);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyShuffleModeChangedOnExecutor(MediaPlaylistAgent playlistAgent, final int shuffleMode) {
        synchronized (this.mLock) {
            if (playlistAgent == this.mPlaylistAgent) {
                this.mCallback.onShuffleModeChanged(this.mInstance, playlistAgent, shuffleMode);
                notifyToAllControllers(new NotifyRunnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass20 */

                    @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                    public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                        callback.onShuffleModeChanged(shuffleMode);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyToController(@NonNull final MediaSession2.ControllerInfo controller, @NonNull NotifyRunnable runnable) {
        if (controller != null) {
            try {
                runnable.run(controller.getControllerCb());
            } catch (DeadObjectException e) {
                if (DEBUG) {
                    Log.d(TAG, controller.toString() + " is gone", e);
                }
                this.mSession2Stub.removeControllerInfo(controller);
                this.mCallbackExecutor.execute(new Runnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.AnonymousClass21 */

                    @Override // java.lang.Runnable
                    public void run() {
                        MediaSession2ImplBase.this.mCallback.onDisconnected(MediaSession2ImplBase.this.getInstance(), controller);
                    }
                });
            } catch (RemoteException e2) {
                Log.w(TAG, "Exception in " + controller.toString(), e2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyToAllControllers(@NonNull NotifyRunnable runnable) {
        List<MediaSession2.ControllerInfo> controllers = getConnectedControllers();
        for (int i = 0; i < controllers.size(); i++) {
            notifyToController(controllers.get(i), runnable);
        }
    }

    private static class MyPlayerEventCallback extends BaseMediaPlayer.PlayerEventCallback {
        private final WeakReference<MediaSession2ImplBase> mSession;

        private MyPlayerEventCallback(MediaSession2ImplBase session) {
            this.mSession = new WeakReference<>(session);
        }

        @Override // android.support.v4.media.BaseMediaPlayer.PlayerEventCallback
        public void onCurrentDataSourceChanged(final BaseMediaPlayer player, final DataSourceDesc dsd) {
            final MediaSession2ImplBase session = getSession();
            if (session != null) {
                session.getCallbackExecutor().execute(new Runnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        final MediaItem2 item;
                        if (dsd == null) {
                            item = null;
                        } else {
                            item = MyPlayerEventCallback.this.getMediaItem(session, dsd);
                            if (item == null) {
                                Log.w(MediaSession2ImplBase.TAG, "Cannot obtain media item from the dsd=" + dsd);
                                return;
                            }
                        }
                        session.getCallback().onCurrentMediaItemChanged(session.getInstance(), player, item);
                        session.notifyToAllControllers(new NotifyRunnable() {
                            /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass1.AnonymousClass1 */

                            @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                                callback.onCurrentMediaItemChanged(item);
                            }
                        });
                    }
                });
            }
        }

        @Override // android.support.v4.media.BaseMediaPlayer.PlayerEventCallback
        public void onMediaPrepared(final BaseMediaPlayer mpb, final DataSourceDesc dsd) {
            final MediaSession2ImplBase session = getSession();
            if (session != null && dsd != null) {
                session.getCallbackExecutor().execute(new Runnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        MediaMetadata2 metadata;
                        MediaItem2 item = MyPlayerEventCallback.this.getMediaItem(session, dsd);
                        if (item != null) {
                            if (item.equals(session.getCurrentMediaItem())) {
                                long duration = session.getDuration();
                                if (duration >= 0) {
                                    MediaMetadata2 metadata2 = item.getMetadata();
                                    if (metadata2 == null) {
                                        metadata = new MediaMetadata2.Builder().putLong("android.media.metadata.DURATION", duration).putString("android.media.metadata.MEDIA_ID", item.getMediaId()).build();
                                    } else if (!metadata2.containsKey("android.media.metadata.DURATION")) {
                                        metadata = new MediaMetadata2.Builder(metadata2).putLong("android.media.metadata.DURATION", duration).build();
                                    } else {
                                        long durationFromMetadata = metadata2.getLong("android.media.metadata.DURATION");
                                        if (duration != durationFromMetadata) {
                                            Log.w(MediaSession2ImplBase.TAG, "duration mismatch for an item. duration from player=" + duration + " duration from metadata=" + durationFromMetadata + ". May be a timing issue?");
                                        }
                                        metadata = null;
                                    }
                                    if (metadata != null) {
                                        item.setMetadata(metadata);
                                        session.notifyToAllControllers(new NotifyRunnable() {
                                            /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass2.AnonymousClass1 */

                                            @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                                            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                                                callback.onPlaylistChanged(session.getPlaylist(), session.getPlaylistMetadata());
                                            }
                                        });
                                    }
                                } else {
                                    return;
                                }
                            }
                            session.getCallback().onMediaPrepared(session.getInstance(), mpb, item);
                        }
                    }
                });
            }
        }

        @Override // android.support.v4.media.BaseMediaPlayer.PlayerEventCallback
        public void onPlayerStateChanged(final BaseMediaPlayer player, final int state) {
            final MediaSession2ImplBase session = getSession();
            if (session != null) {
                session.getCallbackExecutor().execute(new Runnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        session.mAudioFocusHandler.onPlayerStateChanged(state);
                        session.getCallback().onPlayerStateChanged(session.getInstance(), player, state);
                        session.notifyToAllControllers(new NotifyRunnable() {
                            /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass3.AnonymousClass1 */

                            @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                                callback.onPlayerStateChanged(SystemClock.elapsedRealtime(), player.getCurrentPosition(), state);
                            }
                        });
                    }
                });
            }
        }

        @Override // android.support.v4.media.BaseMediaPlayer.PlayerEventCallback
        public void onBufferingStateChanged(final BaseMediaPlayer mpb, final DataSourceDesc dsd, final int state) {
            final MediaSession2ImplBase session = getSession();
            if (session != null && dsd != null) {
                session.getCallbackExecutor().execute(new Runnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass4 */

                    @Override // java.lang.Runnable
                    public void run() {
                        final MediaItem2 item = MyPlayerEventCallback.this.getMediaItem(session, dsd);
                        if (item != null) {
                            session.getCallback().onBufferingStateChanged(session.getInstance(), mpb, item, state);
                            session.notifyToAllControllers(new NotifyRunnable() {
                                /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass4.AnonymousClass1 */

                                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                                    callback.onBufferingStateChanged(item, state, mpb.getBufferedPosition());
                                }
                            });
                        }
                    }
                });
            }
        }

        @Override // android.support.v4.media.BaseMediaPlayer.PlayerEventCallback
        public void onPlaybackSpeedChanged(final BaseMediaPlayer mpb, final float speed) {
            final MediaSession2ImplBase session = getSession();
            if (session != null) {
                session.getCallbackExecutor().execute(new Runnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass5 */

                    @Override // java.lang.Runnable
                    public void run() {
                        session.getCallback().onPlaybackSpeedChanged(session.getInstance(), mpb, speed);
                        session.notifyToAllControllers(new NotifyRunnable() {
                            /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass5.AnonymousClass1 */

                            @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                                callback.onPlaybackSpeedChanged(SystemClock.elapsedRealtime(), session.getCurrentPosition(), speed);
                            }
                        });
                    }
                });
            }
        }

        @Override // android.support.v4.media.BaseMediaPlayer.PlayerEventCallback
        public void onSeekCompleted(final BaseMediaPlayer mpb, final long position) {
            final MediaSession2ImplBase session = getSession();
            if (session != null) {
                session.getCallbackExecutor().execute(new Runnable() {
                    /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass6 */

                    @Override // java.lang.Runnable
                    public void run() {
                        session.getCallback().onSeekCompleted(session.getInstance(), mpb, position);
                        session.notifyToAllControllers(new NotifyRunnable() {
                            /* class android.support.v4.media.MediaSession2ImplBase.MyPlayerEventCallback.AnonymousClass6.AnonymousClass1 */

                            @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                                callback.onSeekCompleted(SystemClock.elapsedRealtime(), session.getCurrentPosition(), position);
                            }
                        });
                    }
                });
            }
        }

        private MediaSession2ImplBase getSession() {
            MediaSession2ImplBase session = this.mSession.get();
            if (session == null && MediaSession2ImplBase.DEBUG) {
                Log.d(MediaSession2ImplBase.TAG, "Session is closed", new IllegalStateException());
            }
            return session;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private MediaItem2 getMediaItem(MediaSession2ImplBase session, DataSourceDesc dsd) {
            MediaPlaylistAgent agent = session.getPlaylistAgent();
            if (agent != null) {
                MediaItem2 item = agent.getMediaItem(dsd);
                if (item == null && MediaSession2ImplBase.DEBUG) {
                    Log.d(MediaSession2ImplBase.TAG, "Could not find matching item for dsd=" + dsd, new NoSuchElementException());
                }
                return item;
            } else if (!MediaSession2ImplBase.DEBUG) {
                return null;
            } else {
                Log.d(MediaSession2ImplBase.TAG, "Session is closed", new IllegalStateException());
                return null;
            }
        }
    }

    private static class MyPlaylistEventCallback extends MediaPlaylistAgent.PlaylistEventCallback {
        private final WeakReference<MediaSession2ImplBase> mSession;

        private MyPlaylistEventCallback(MediaSession2ImplBase session) {
            this.mSession = new WeakReference<>(session);
        }

        @Override // android.support.v4.media.MediaPlaylistAgent.PlaylistEventCallback
        public void onPlaylistChanged(MediaPlaylistAgent playlistAgent, List<MediaItem2> list, MediaMetadata2 metadata) {
            MediaSession2ImplBase session = this.mSession.get();
            if (session != null) {
                session.notifyPlaylistChangedOnExecutor(playlistAgent, list, metadata);
            }
        }

        @Override // android.support.v4.media.MediaPlaylistAgent.PlaylistEventCallback
        public void onPlaylistMetadataChanged(MediaPlaylistAgent playlistAgent, MediaMetadata2 metadata) {
            MediaSession2ImplBase session = this.mSession.get();
            if (session != null) {
                session.notifyPlaylistMetadataChangedOnExecutor(playlistAgent, metadata);
            }
        }

        @Override // android.support.v4.media.MediaPlaylistAgent.PlaylistEventCallback
        public void onRepeatModeChanged(MediaPlaylistAgent playlistAgent, int repeatMode) {
            MediaSession2ImplBase session = this.mSession.get();
            if (session != null) {
                session.notifyRepeatModeChangedOnExecutor(playlistAgent, repeatMode);
            }
        }

        @Override // android.support.v4.media.MediaPlaylistAgent.PlaylistEventCallback
        public void onShuffleModeChanged(MediaPlaylistAgent playlistAgent, int shuffleMode) {
            MediaSession2ImplBase session = this.mSession.get();
            if (session != null) {
                session.notifyShuffleModeChangedOnExecutor(playlistAgent, shuffleMode);
            }
        }
    }
}
