package android.support.v4.media;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.mediacompat.Rating2;
import android.support.v4.app.BundleCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaController2;
import android.support.v4.media.MediaSession2;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import java.util.List;
import java.util.concurrent.Executor;

@TargetApi(16)
class MediaController2ImplLegacy implements MediaController2.SupportLibraryImpl {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "MC2ImplLegacy";
    static final Bundle sDefaultRootExtras = new Bundle();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public SessionCommandGroup2 mAllowedCommands;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public MediaBrowserCompat mBrowserCompat;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mBufferingState;
    /* access modifiers changed from: private */
    public final MediaController2.ControllerCallback mCallback;
    /* access modifiers changed from: private */
    public final Executor mCallbackExecutor;
    @GuardedBy("mLock")
    private volatile boolean mConnected;
    /* access modifiers changed from: private */
    public final Context mContext;
    @GuardedBy("mLock")
    private MediaControllerCompat mControllerCompat;
    @GuardedBy("mLock")
    private ControllerCompatCallback mControllerCompatCallback;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public MediaItem2 mCurrentMediaItem;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public final HandlerThread mHandlerThread;
    /* access modifiers changed from: private */
    public MediaController2 mInstance;
    @GuardedBy("mLock")
    private boolean mIsReleased;
    final Object mLock = new Object();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public MediaMetadataCompat mMediaMetadataCompat;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public MediaController2.PlaybackInfo mPlaybackInfo;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public PlaybackStateCompat mPlaybackStateCompat;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mPlayerState;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public List<MediaItem2> mPlaylist;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public MediaMetadata2 mPlaylistMetadata;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mRepeatMode;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mShuffleMode;
    /* access modifiers changed from: private */
    public final SessionToken2 mToken;

    private class ConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        private ConnectionCallback() {
        }

        public void onConnected() {
            MediaBrowserCompat browser = MediaController2ImplLegacy.this.getBrowserCompat();
            if (browser != null) {
                MediaController2ImplLegacy.this.connectToSession(browser.getSessionToken());
            } else if (MediaController2ImplLegacy.DEBUG) {
                Log.d(MediaController2ImplLegacy.TAG, "Controller is closed prematually", new IllegalStateException());
            }
        }

        public void onConnectionSuspended() {
            MediaController2ImplLegacy.this.close();
        }

        public void onConnectionFailed() {
            MediaController2ImplLegacy.this.close();
        }
    }

    private final class ControllerCompatCallback extends MediaControllerCompat.Callback {
        private ControllerCompatCallback() {
        }

        public void onSessionReady() {
            MediaController2ImplLegacy.this.sendCommand("android.support.v4.media.controller.command.CONNECT", (ResultReceiver) new ResultReceiver(MediaController2ImplLegacy.this.mHandler) {
                /* access modifiers changed from: protected */
                public void onReceiveResult(int resultCode, Bundle resultData) {
                    if (MediaController2ImplLegacy.this.mHandlerThread.isAlive()) {
                        switch (resultCode) {
                            case -1:
                                MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                                    public void run() {
                                        MediaController2ImplLegacy.this.mCallback.onDisconnected(MediaController2ImplLegacy.this.mInstance);
                                    }
                                });
                                MediaController2ImplLegacy.this.close();
                                break;
                            case 0:
                                MediaController2ImplLegacy.this.onConnectedNotLocked(resultData);
                                break;
                        }
                    }
                }
            });
        }

        public void onSessionDestroyed() {
            MediaController2ImplLegacy.this.close();
        }

        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            synchronized (MediaController2ImplLegacy.this.mLock) {
                PlaybackStateCompat unused = MediaController2ImplLegacy.this.mPlaybackStateCompat = state;
            }
        }

        public void onMetadataChanged(MediaMetadataCompat metadata) {
            synchronized (MediaController2ImplLegacy.this.mLock) {
                MediaMetadataCompat unused = MediaController2ImplLegacy.this.mMediaMetadataCompat = metadata;
            }
        }

        public void onSessionEvent(String event, Bundle extras) {
            if (extras != null) {
                extras.setClassLoader(MediaSession2.class.getClassLoader());
            }
            char c = 65535;
            switch (event.hashCode()) {
                case -2076894204:
                    if (event.equals("android.support.v4.media.session.event.ON_BUFFERING_STATE_CHANGED")) {
                        c = 13;
                        break;
                    }
                    break;
                case -2060536131:
                    if (event.equals("android.support.v4.media.session.event.ON_PLAYBACK_SPEED_CHANGED")) {
                        c = 12;
                        break;
                    }
                    break;
                case -1588811870:
                    if (event.equals("android.support.v4.media.session.event.ON_PLAYBACK_INFO_CHANGED")) {
                        c = 11;
                        break;
                    }
                    break;
                case -1471144819:
                    if (event.equals("android.support.v4.media.session.event.ON_PLAYER_STATE_CHANGED")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1021916189:
                    if (event.equals("android.support.v4.media.session.event.ON_ERROR")) {
                        c = 3;
                        break;
                    }
                    break;
                case -617184370:
                    if (event.equals("android.support.v4.media.session.event.ON_CURRENT_MEDIA_ITEM_CHANGED")) {
                        c = 2;
                        break;
                    }
                    break;
                case -92092013:
                    if (event.equals("android.support.v4.media.session.event.ON_ROUTES_INFO_CHANGED")) {
                        c = 4;
                        break;
                    }
                    break;
                case -53555497:
                    if (event.equals("android.support.v4.media.session.event.ON_REPEAT_MODE_CHANGED")) {
                        c = 7;
                        break;
                    }
                    break;
                case 229988025:
                    if (event.equals("android.support.v4.media.session.event.SEND_CUSTOM_COMMAND")) {
                        c = 9;
                        break;
                    }
                    break;
                case 306321100:
                    if (event.equals("android.support.v4.media.session.event.ON_PLAYLIST_METADATA_CHANGED")) {
                        c = 6;
                        break;
                    }
                    break;
                case 408969344:
                    if (event.equals("android.support.v4.media.session.event.SET_CUSTOM_LAYOUT")) {
                        c = 10;
                        break;
                    }
                    break;
                case 806201420:
                    if (event.equals("android.support.v4.media.session.event.ON_PLAYLIST_CHANGED")) {
                        c = 5;
                        break;
                    }
                    break;
                case 896576579:
                    if (event.equals("android.support.v4.media.session.event.ON_SHUFFLE_MODE_CHANGED")) {
                        c = 8;
                        break;
                    }
                    break;
                case 1696119769:
                    if (event.equals("android.support.v4.media.session.event.ON_ALLOWED_COMMANDS_CHANGED")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1871849865:
                    if (event.equals("android.support.v4.media.session.event.ON_SEEK_COMPLETED")) {
                        c = 14;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    final SessionCommandGroup2 allowedCommands = SessionCommandGroup2.fromBundle(extras.getBundle("android.support.v4.media.argument.ALLOWED_COMMANDS"));
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        SessionCommandGroup2 unused = MediaController2ImplLegacy.this.mAllowedCommands = allowedCommands;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onAllowedCommandsChanged(MediaController2ImplLegacy.this.mInstance, allowedCommands);
                        }
                    });
                    break;
                case 1:
                    final int playerState = extras.getInt("android.support.v4.media.argument.PLAYER_STATE");
                    PlaybackStateCompat state = (PlaybackStateCompat) extras.getParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT");
                    if (state != null) {
                        synchronized (MediaController2ImplLegacy.this.mLock) {
                            int unused2 = MediaController2ImplLegacy.this.mPlayerState = playerState;
                            PlaybackStateCompat unused3 = MediaController2ImplLegacy.this.mPlaybackStateCompat = state;
                        }
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onPlayerStateChanged(MediaController2ImplLegacy.this.mInstance, playerState);
                            }
                        });
                        break;
                    } else {
                        return;
                    }
                case 2:
                    final MediaItem2 item = MediaItem2.fromBundle(extras.getBundle("android.support.v4.media.argument.MEDIA_ITEM"));
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        MediaItem2 unused4 = MediaController2ImplLegacy.this.mCurrentMediaItem = item;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onCurrentMediaItemChanged(MediaController2ImplLegacy.this.mInstance, item);
                        }
                    });
                    break;
                case 3:
                    final int errorCode = extras.getInt("android.support.v4.media.argument.ERROR_CODE");
                    final Bundle errorExtras = extras.getBundle("android.support.v4.media.argument.EXTRAS");
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onError(MediaController2ImplLegacy.this.mInstance, errorCode, errorExtras);
                        }
                    });
                    break;
                case 4:
                    final List<Bundle> routes = MediaUtils2.convertToBundleList(extras.getParcelableArray("android.support.v4.media.argument.ROUTE_BUNDLE"));
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onRoutesInfoChanged(MediaController2ImplLegacy.this.mInstance, routes);
                        }
                    });
                    break;
                case 5:
                    final MediaMetadata2 playlistMetadata = MediaMetadata2.fromBundle(extras.getBundle("android.support.v4.media.argument.PLAYLIST_METADATA"));
                    final List<MediaItem2> playlist = MediaUtils2.convertToMediaItem2List(extras.getParcelableArray("android.support.v4.media.argument.PLAYLIST"));
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        List unused5 = MediaController2ImplLegacy.this.mPlaylist = playlist;
                        MediaMetadata2 unused6 = MediaController2ImplLegacy.this.mPlaylistMetadata = playlistMetadata;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onPlaylistChanged(MediaController2ImplLegacy.this.mInstance, playlist, playlistMetadata);
                        }
                    });
                    break;
                case 6:
                    final MediaMetadata2 playlistMetadata2 = MediaMetadata2.fromBundle(extras.getBundle("android.support.v4.media.argument.PLAYLIST_METADATA"));
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        MediaMetadata2 unused7 = MediaController2ImplLegacy.this.mPlaylistMetadata = playlistMetadata2;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onPlaylistMetadataChanged(MediaController2ImplLegacy.this.mInstance, playlistMetadata2);
                        }
                    });
                    break;
                case 7:
                    final int repeatMode = extras.getInt("android.support.v4.media.argument.REPEAT_MODE");
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        int unused8 = MediaController2ImplLegacy.this.mRepeatMode = repeatMode;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onRepeatModeChanged(MediaController2ImplLegacy.this.mInstance, repeatMode);
                        }
                    });
                    break;
                case 8:
                    final int shuffleMode = extras.getInt("android.support.v4.media.argument.SHUFFLE_MODE");
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        int unused9 = MediaController2ImplLegacy.this.mShuffleMode = shuffleMode;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onShuffleModeChanged(MediaController2ImplLegacy.this.mInstance, shuffleMode);
                        }
                    });
                    break;
                case 9:
                    Bundle commandBundle = extras.getBundle("android.support.v4.media.argument.CUSTOM_COMMAND");
                    if (commandBundle != null) {
                        final SessionCommand2 command = SessionCommand2.fromBundle(commandBundle);
                        final Bundle args = extras.getBundle("android.support.v4.media.argument.ARGUMENTS");
                        final ResultReceiver receiver = (ResultReceiver) extras.getParcelable("android.support.v4.media.argument.RESULT_RECEIVER");
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onCustomCommand(MediaController2ImplLegacy.this.mInstance, command, args, receiver);
                            }
                        });
                        break;
                    } else {
                        return;
                    }
                case 10:
                    final List<MediaSession2.CommandButton> layout = MediaUtils2.convertToCommandButtonList(extras.getParcelableArray("android.support.v4.media.argument.COMMAND_BUTTONS"));
                    if (layout != null) {
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onCustomLayoutChanged(MediaController2ImplLegacy.this.mInstance, layout);
                            }
                        });
                        break;
                    } else {
                        return;
                    }
                case 11:
                    final MediaController2.PlaybackInfo info = MediaController2.PlaybackInfo.fromBundle(extras.getBundle("android.support.v4.media.argument.PLAYBACK_INFO"));
                    if (info != null) {
                        synchronized (MediaController2ImplLegacy.this.mLock) {
                            MediaController2.PlaybackInfo unused10 = MediaController2ImplLegacy.this.mPlaybackInfo = info;
                        }
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onPlaybackInfoChanged(MediaController2ImplLegacy.this.mInstance, info);
                            }
                        });
                        break;
                    } else {
                        return;
                    }
                case 12:
                    final PlaybackStateCompat state2 = (PlaybackStateCompat) extras.getParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT");
                    if (state2 != null) {
                        synchronized (MediaController2ImplLegacy.this.mLock) {
                            PlaybackStateCompat unused11 = MediaController2ImplLegacy.this.mPlaybackStateCompat = state2;
                        }
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onPlaybackSpeedChanged(MediaController2ImplLegacy.this.mInstance, state2.getPlaybackSpeed());
                            }
                        });
                        break;
                    } else {
                        return;
                    }
                case 13:
                    final MediaItem2 item2 = MediaItem2.fromBundle(extras.getBundle("android.support.v4.media.argument.MEDIA_ITEM"));
                    final int bufferingState = extras.getInt("android.support.v4.media.argument.BUFFERING_STATE");
                    PlaybackStateCompat state3 = (PlaybackStateCompat) extras.getParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT");
                    if (item2 != null && state3 != null) {
                        synchronized (MediaController2ImplLegacy.this.mLock) {
                            int unused12 = MediaController2ImplLegacy.this.mBufferingState = bufferingState;
                            PlaybackStateCompat unused13 = MediaController2ImplLegacy.this.mPlaybackStateCompat = state3;
                        }
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onBufferingStateChanged(MediaController2ImplLegacy.this.mInstance, item2, bufferingState);
                            }
                        });
                        break;
                    } else {
                        return;
                    }
                case 14:
                    final long position = extras.getLong("android.support.v4.media.argument.SEEK_POSITION");
                    PlaybackStateCompat state4 = (PlaybackStateCompat) extras.getParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT");
                    if (state4 != null) {
                        synchronized (MediaController2ImplLegacy.this.mLock) {
                            PlaybackStateCompat unused14 = MediaController2ImplLegacy.this.mPlaybackStateCompat = state4;
                        }
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onSeekCompleted(MediaController2ImplLegacy.this.mInstance, position);
                            }
                        });
                        break;
                    } else {
                        return;
                    }
            }
        }
    }

    static {
        sDefaultRootExtras.putBoolean("android.support.v4.media.root_default_root", true);
    }

    MediaController2ImplLegacy(@NonNull Context context, @NonNull MediaController2 instance, @NonNull SessionToken2 token, @NonNull Executor executor, @NonNull MediaController2.ControllerCallback callback) {
        this.mContext = context;
        this.mInstance = instance;
        this.mHandlerThread = new HandlerThread("MediaController2_Thread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mToken = token;
        this.mCallback = callback;
        this.mCallbackExecutor = executor;
        if (this.mToken.getType() == 0) {
            synchronized (this.mLock) {
                this.mBrowserCompat = null;
            }
            connectToSession((MediaSessionCompat.Token) this.mToken.getBinder());
            return;
        }
        connectToService();
    }

    public void close() {
        if (DEBUG) {
            Log.d(TAG, "release from " + this.mToken);
        }
        synchronized (this.mLock) {
            if (!this.mIsReleased) {
                this.mHandler.removeCallbacksAndMessages(null);
                if (Build.VERSION.SDK_INT >= 18) {
                    this.mHandlerThread.quitSafely();
                } else {
                    this.mHandlerThread.quit();
                }
                this.mIsReleased = true;
                sendCommand("android.support.v4.media.controller.command.DISCONNECT");
                if (this.mControllerCompat != null) {
                    this.mControllerCompat.unregisterCallback(this.mControllerCompatCallback);
                }
                if (this.mBrowserCompat != null) {
                    this.mBrowserCompat.disconnect();
                    this.mBrowserCompat = null;
                }
                if (this.mControllerCompat != null) {
                    this.mControllerCompat.unregisterCallback(this.mControllerCompatCallback);
                    this.mControllerCompat = null;
                }
                this.mConnected = DEBUG;
                this.mCallbackExecutor.execute(new Runnable() {
                    public void run() {
                        MediaController2ImplLegacy.this.mCallback.onDisconnected(MediaController2ImplLegacy.this.mInstance);
                    }
                });
            }
        }
    }

    @NonNull
    public SessionToken2 getSessionToken() {
        return this.mToken;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mConnected;
        }
        return z;
    }

    public void play() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(1);
            }
        }
    }

    public void pause() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(2);
            }
        }
    }

    public void reset() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(3);
            }
        }
    }

    public void prepare() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(6);
            }
        }
    }

    public void fastForward() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(7);
            }
        }
    }

    public void rewind() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(8);
            }
        }
    }

    public void seekTo(long pos) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putLong("android.support.v4.media.argument.SEEK_POSITION", pos);
            sendCommand(9, args);
        }
    }

    public void skipForward() {
    }

    public void skipBackward() {
    }

    public void playFromMediaId(@NonNull String mediaId, @Nullable Bundle extras) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putString("android.support.v4.media.argument.MEDIA_ID", mediaId);
            args.putBundle("android.support.v4.media.argument.EXTRAS", extras);
            sendCommand(22, args);
        }
    }

    public void playFromSearch(@NonNull String query, @Nullable Bundle extras) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putString("android.support.v4.media.argument.QUERY", query);
            args.putBundle("android.support.v4.media.argument.EXTRAS", extras);
            sendCommand(24, args);
        }
    }

    public void playFromUri(@NonNull Uri uri, @Nullable Bundle extras) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putParcelable("android.support.v4.media.argument.URI", uri);
            args.putBundle("android.support.v4.media.argument.EXTRAS", extras);
            sendCommand(23, args);
        }
    }

    public void prepareFromMediaId(@NonNull String mediaId, @Nullable Bundle extras) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putString("android.support.v4.media.argument.MEDIA_ID", mediaId);
            args.putBundle("android.support.v4.media.argument.EXTRAS", extras);
            sendCommand(25, args);
        }
    }

    public void prepareFromSearch(@NonNull String query, @Nullable Bundle extras) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putString("android.support.v4.media.argument.QUERY", query);
            args.putBundle("android.support.v4.media.argument.EXTRAS", extras);
            sendCommand(27, args);
        }
    }

    public void prepareFromUri(@NonNull Uri uri, @Nullable Bundle extras) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putParcelable("android.support.v4.media.argument.URI", uri);
            args.putBundle("android.support.v4.media.argument.EXTRAS", extras);
            sendCommand(26, args);
        }
    }

    public void setVolumeTo(int value, int flags) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putInt("android.support.v4.media.argument.VOLUME", value);
            args.putInt("android.support.v4.media.argument.VOLUME_FLAGS", flags);
            sendCommand(10, args);
        }
    }

    public void adjustVolume(int direction, int flags) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putInt("android.support.v4.media.argument.VOLUME_DIRECTION", direction);
            args.putInt("android.support.v4.media.argument.VOLUME_FLAGS", flags);
            sendCommand(11, args);
        }
    }

    @Nullable
    public PendingIntent getSessionActivity() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return null;
            }
            PendingIntent sessionActivity = this.mControllerCompat.getSessionActivity();
            return sessionActivity;
        }
    }

    public int getPlayerState() {
        int i;
        synchronized (this.mLock) {
            i = this.mPlayerState;
        }
        return i;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        return -1;
     */
    public long getDuration() {
        synchronized (this.mLock) {
            if (this.mMediaMetadataCompat != null && this.mMediaMetadataCompat.containsKey("android.media.metadata.DURATION")) {
                long j = this.mMediaMetadataCompat.getLong("android.media.metadata.DURATION");
                return j;
            }
        }
    }

    public long getCurrentPosition() {
        long timeDiff;
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return -1;
            } else if (this.mPlaybackStateCompat == null) {
                return -1;
            } else {
                if (this.mInstance.mTimeDiff != null) {
                    timeDiff = this.mInstance.mTimeDiff.longValue();
                } else {
                    timeDiff = SystemClock.elapsedRealtime() - this.mPlaybackStateCompat.getLastPositionUpdateTime();
                }
                long max = Math.max(0, this.mPlaybackStateCompat.getPosition() + ((long) (this.mPlaybackStateCompat.getPlaybackSpeed() * ((float) timeDiff))));
                return max;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0022, code lost:
        return r2;
     */
    public float getPlaybackSpeed() {
        synchronized (this.mLock) {
            float f = 0.0f;
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return 0.0f;
            } else if (this.mPlaybackStateCompat != null) {
                f = this.mPlaybackStateCompat.getPlaybackSpeed();
            }
        }
    }

    public void setPlaybackSpeed(float speed) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putFloat("android.support.v4.media.argument.PLAYBACK_SPEED", speed);
            sendCommand(39, args);
        }
    }

    public int getBufferingState() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return 0;
            }
            int i = this.mBufferingState;
            return i;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
        return r2;
     */
    public long getBufferedPosition() {
        synchronized (this.mLock) {
            long j = -1;
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return -1;
            } else if (this.mPlaybackStateCompat != null) {
                j = this.mPlaybackStateCompat.getBufferedPosition();
            }
        }
    }

    @Nullable
    public MediaController2.PlaybackInfo getPlaybackInfo() {
        MediaController2.PlaybackInfo playbackInfo;
        synchronized (this.mLock) {
            playbackInfo = this.mPlaybackInfo;
        }
        return playbackInfo;
    }

    public void setRating(@NonNull String mediaId, @NonNull Rating2 rating) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle args = new Bundle();
            args.putString("android.support.v4.media.argument.MEDIA_ID", mediaId);
            args.putBundle("android.support.v4.media.argument.RATING", rating.toBundle());
            sendCommand(28, args);
        }
    }

    public void sendCustomCommand(@NonNull SessionCommand2 command, @Nullable Bundle args, @Nullable ResultReceiver cb) {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putBundle("android.support.v4.media.argument.CUSTOM_COMMAND", command.toBundle());
            bundle.putBundle("android.support.v4.media.argument.ARGUMENTS", args);
            sendCommand("android.support.v4.media.controller.command.BY_CUSTOM_COMMAND", bundle, cb);
        }
    }

    @Nullable
    public List<MediaItem2> getPlaylist() {
        List<MediaItem2> list;
        synchronized (this.mLock) {
            list = this.mPlaylist;
        }
        return list;
    }

    public void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 metadata) {
        Bundle args = new Bundle();
        args.putParcelableArray("android.support.v4.media.argument.PLAYLIST", MediaUtils2.convertMediaItem2ListToParcelableArray(list));
        args.putBundle("android.support.v4.media.argument.PLAYLIST_METADATA", metadata == null ? null : metadata.toBundle());
        sendCommand(19, args);
    }

    public void updatePlaylistMetadata(@Nullable MediaMetadata2 metadata) {
        Bundle args = new Bundle();
        args.putBundle("android.support.v4.media.argument.PLAYLIST_METADATA", metadata == null ? null : metadata.toBundle());
        sendCommand(21, args);
    }

    @Nullable
    public MediaMetadata2 getPlaylistMetadata() {
        MediaMetadata2 mediaMetadata2;
        synchronized (this.mLock) {
            mediaMetadata2 = this.mPlaylistMetadata;
        }
        return mediaMetadata2;
    }

    public void addPlaylistItem(int index, @NonNull MediaItem2 item) {
        Bundle args = new Bundle();
        args.putInt("android.support.v4.media.argument.PLAYLIST_INDEX", index);
        args.putBundle("android.support.v4.media.argument.MEDIA_ITEM", item.toBundle());
        sendCommand(15, args);
    }

    public void removePlaylistItem(@NonNull MediaItem2 item) {
        Bundle args = new Bundle();
        args.putBundle("android.support.v4.media.argument.MEDIA_ITEM", item.toBundle());
        sendCommand(16, args);
    }

    public void replacePlaylistItem(int index, @NonNull MediaItem2 item) {
        Bundle args = new Bundle();
        args.putInt("android.support.v4.media.argument.PLAYLIST_INDEX", index);
        args.putBundle("android.support.v4.media.argument.MEDIA_ITEM", item.toBundle());
        sendCommand(17, args);
    }

    public MediaItem2 getCurrentMediaItem() {
        MediaItem2 mediaItem2;
        synchronized (this.mLock) {
            mediaItem2 = this.mCurrentMediaItem;
        }
        return mediaItem2;
    }

    public void skipToPreviousItem() {
        sendCommand(5);
    }

    public void skipToNextItem() {
        sendCommand(4);
    }

    public void skipToPlaylistItem(@NonNull MediaItem2 item) {
        Bundle args = new Bundle();
        args.putBundle("android.support.v4.media.argument.MEDIA_ITEM", item.toBundle());
        sendCommand(12, args);
    }

    public int getRepeatMode() {
        int i;
        synchronized (this.mLock) {
            i = this.mRepeatMode;
        }
        return i;
    }

    public void setRepeatMode(int repeatMode) {
        Bundle args = new Bundle();
        args.putInt("android.support.v4.media.argument.REPEAT_MODE", repeatMode);
        sendCommand(14, args);
    }

    public int getShuffleMode() {
        int i;
        synchronized (this.mLock) {
            i = this.mShuffleMode;
        }
        return i;
    }

    public void setShuffleMode(int shuffleMode) {
        Bundle args = new Bundle();
        args.putInt("android.support.v4.media.argument.SHUFFLE_MODE", shuffleMode);
        sendCommand(13, args);
    }

    public void subscribeRoutesInfo() {
        sendCommand(36);
    }

    public void unsubscribeRoutesInfo() {
        sendCommand(37);
    }

    public void selectRoute(@NonNull Bundle route) {
        Bundle args = new Bundle();
        args.putBundle("android.support.v4.media.argument.ROUTE_BUNDLE", route);
        sendCommand(38, args);
    }

    @NonNull
    public Context getContext() {
        return this.mContext;
    }

    @NonNull
    public MediaController2.ControllerCallback getCallback() {
        return this.mCallback;
    }

    @NonNull
    public Executor getCallbackExecutor() {
        return this.mCallbackExecutor;
    }

    @Nullable
    public MediaBrowserCompat getBrowserCompat() {
        MediaBrowserCompat mediaBrowserCompat;
        synchronized (this.mLock) {
            mediaBrowserCompat = this.mBrowserCompat;
        }
        return mediaBrowserCompat;
    }

    @NonNull
    public MediaController2 getInstance() {
        return this.mInstance;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0088, code lost:
        if (0 == 0) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x008a, code lost:
        close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x008d, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x009b, code lost:
        if (1 == 0) goto L_0x00a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x009d, code lost:
        close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00a0, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r14.mCallbackExecutor.execute(new android.support.v4.media.MediaController2ImplLegacy.AnonymousClass2(r14));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00c3, code lost:
        if (0 == 0) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00c5, code lost:
        close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00c8, code lost:
        return;
     */
    public void onConnectedNotLocked(Bundle data) {
        data.setClassLoader(MediaSession2.class.getClassLoader());
        final SessionCommandGroup2 allowedCommands = SessionCommandGroup2.fromBundle(data.getBundle("android.support.v4.media.argument.ALLOWED_COMMANDS"));
        int playerState = data.getInt("android.support.v4.media.argument.PLAYER_STATE");
        MediaItem2 currentMediaItem = MediaItem2.fromBundle(data.getBundle("android.support.v4.media.argument.MEDIA_ITEM"));
        int bufferingState = data.getInt("android.support.v4.media.argument.BUFFERING_STATE");
        PlaybackStateCompat playbackStateCompat = (PlaybackStateCompat) data.getParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT");
        int repeatMode = data.getInt("android.support.v4.media.argument.REPEAT_MODE");
        int shuffleMode = data.getInt("android.support.v4.media.argument.SHUFFLE_MODE");
        List<MediaItem2> playlist = MediaUtils2.convertToMediaItem2List(data.getParcelableArray("android.support.v4.media.argument.PLAYLIST"));
        MediaController2.PlaybackInfo playbackInfo = MediaController2.PlaybackInfo.fromBundle(data.getBundle("android.support.v4.media.argument.PLAYBACK_INFO"));
        MediaMetadata2 metadata = MediaMetadata2.fromBundle(data.getBundle("android.support.v4.media.argument.PLAYLIST_METADATA"));
        if (DEBUG) {
            Log.d(TAG, "onConnectedNotLocked token=" + this.mToken + ", allowedCommands=" + allowedCommands);
        }
        try {
            synchronized (this.mLock) {
                if (!this.mIsReleased) {
                    if (this.mConnected) {
                        Log.e(TAG, "Cannot be notified about the connection result many times. Probably a bug or malicious app.");
                    } else {
                        this.mAllowedCommands = allowedCommands;
                        this.mPlayerState = playerState;
                        this.mCurrentMediaItem = currentMediaItem;
                        this.mBufferingState = bufferingState;
                        this.mPlaybackStateCompat = playbackStateCompat;
                        this.mRepeatMode = repeatMode;
                        this.mShuffleMode = shuffleMode;
                        this.mPlaylist = playlist;
                        this.mPlaylistMetadata = metadata;
                        this.mConnected = true;
                        this.mPlaybackInfo = playbackInfo;
                    }
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void connectToSession(MediaSessionCompat.Token sessionCompatToken) {
        MediaControllerCompat controllerCompat = null;
        try {
            controllerCompat = new MediaControllerCompat(this.mContext, sessionCompatToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        synchronized (this.mLock) {
            this.mControllerCompat = controllerCompat;
            this.mControllerCompatCallback = new ControllerCompatCallback();
            this.mControllerCompat.registerCallback(this.mControllerCompatCallback, this.mHandler);
        }
        if (controllerCompat.isSessionReady()) {
            sendCommand("android.support.v4.media.controller.command.CONNECT", (ResultReceiver) new ResultReceiver(this.mHandler) {
                /* access modifiers changed from: protected */
                public void onReceiveResult(int resultCode, Bundle resultData) {
                    if (MediaController2ImplLegacy.this.mHandlerThread.isAlive()) {
                        switch (resultCode) {
                            case -1:
                                MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                                    public void run() {
                                        MediaController2ImplLegacy.this.mCallback.onDisconnected(MediaController2ImplLegacy.this.mInstance);
                                    }
                                });
                                MediaController2ImplLegacy.this.close();
                                break;
                            case 0:
                                MediaController2ImplLegacy.this.onConnectedNotLocked(resultData);
                                break;
                        }
                    }
                }
            });
        }
    }

    private void connectToService() {
        this.mCallbackExecutor.execute(new Runnable() {
            public void run() {
                synchronized (MediaController2ImplLegacy.this.mLock) {
                    MediaBrowserCompat unused = MediaController2ImplLegacy.this.mBrowserCompat = new MediaBrowserCompat(MediaController2ImplLegacy.this.mContext, MediaController2ImplLegacy.this.mToken.getComponentName(), new ConnectionCallback(), MediaController2ImplLegacy.sDefaultRootExtras);
                    MediaController2ImplLegacy.this.mBrowserCompat.connect();
                }
            }
        });
    }

    private void sendCommand(int commandCode) {
        sendCommand(commandCode, (Bundle) null);
    }

    private void sendCommand(int commandCode, Bundle args) {
        if (args == null) {
            args = new Bundle();
        }
        args.putInt("android.support.v4.media.argument.COMMAND_CODE", commandCode);
        sendCommand("android.support.v4.media.controller.command.BY_COMMAND_CODE", args, null);
    }

    private void sendCommand(String command) {
        sendCommand(command, null, null);
    }

    /* access modifiers changed from: private */
    public void sendCommand(String command, ResultReceiver receiver) {
        sendCommand(command, null, receiver);
    }

    private void sendCommand(String command, Bundle args, ResultReceiver receiver) {
        if (args == null) {
            args = new Bundle();
        }
        synchronized (this.mLock) {
            try {
                MediaControllerCompat controller = this.mControllerCompat;
                try {
                    ControllerCompatCallback callback = this.mControllerCompatCallback;
                    BundleCompat.putBinder(args, "android.support.v4.media.argument.ICONTROLLER_CALLBACK", callback.getIControllerCallback().asBinder());
                    args.putString("android.support.v4.media.argument.PACKAGE_NAME", this.mContext.getPackageName());
                    args.putInt("android.support.v4.media.argument.UID", Process.myUid());
                    args.putInt("android.support.v4.media.argument.PID", Process.myPid());
                    controller.sendCommand(command, args, receiver);
                } catch (Throwable th) {
                    th = th;
                    MediaControllerCompat mediaControllerCompat = controller;
                    while (true) {
                        try {
                            break;
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }
}
