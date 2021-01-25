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

/* access modifiers changed from: package-private */
@TargetApi(16)
public class MediaController2ImplLegacy implements MediaController2.SupportLibraryImpl {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "MC2ImplLegacy";
    static final Bundle sDefaultRootExtras = new Bundle();
    @GuardedBy("mLock")
    private SessionCommandGroup2 mAllowedCommands;
    @GuardedBy("mLock")
    private MediaBrowserCompat mBrowserCompat;
    @GuardedBy("mLock")
    private int mBufferingState;
    private final MediaController2.ControllerCallback mCallback;
    private final Executor mCallbackExecutor;
    @GuardedBy("mLock")
    private volatile boolean mConnected;
    private final Context mContext;
    @GuardedBy("mLock")
    private MediaControllerCompat mControllerCompat;
    @GuardedBy("mLock")
    private ControllerCompatCallback mControllerCompatCallback;
    @GuardedBy("mLock")
    private MediaItem2 mCurrentMediaItem;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private MediaController2 mInstance;
    @GuardedBy("mLock")
    private boolean mIsReleased;
    final Object mLock = new Object();
    @GuardedBy("mLock")
    private MediaMetadataCompat mMediaMetadataCompat;
    @GuardedBy("mLock")
    private MediaController2.PlaybackInfo mPlaybackInfo;
    @GuardedBy("mLock")
    private PlaybackStateCompat mPlaybackStateCompat;
    @GuardedBy("mLock")
    private int mPlayerState;
    @GuardedBy("mLock")
    private List<MediaItem2> mPlaylist;
    @GuardedBy("mLock")
    private MediaMetadata2 mPlaylistMetadata;
    @GuardedBy("mLock")
    private int mRepeatMode;
    @GuardedBy("mLock")
    private int mShuffleMode;
    private final SessionToken2 mToken;

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

    @Override // java.lang.AutoCloseable
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
                    /* class android.support.v4.media.MediaController2ImplLegacy.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        MediaController2ImplLegacy.this.mCallback.onDisconnected(MediaController2ImplLegacy.this.mInstance);
                    }
                });
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @NonNull
    public SessionToken2 getSessionToken() {
        return this.mToken;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public boolean isConnected() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mConnected;
        }
        return z;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void play() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(1);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void pause() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(2);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void reset() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(3);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void prepare() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(6);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void fastForward() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(7);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void rewind() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
            } else {
                sendCommand(8);
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void skipForward() {
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void skipBackward() {
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @Nullable
    public PendingIntent getSessionActivity() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return null;
            }
            return this.mControllerCompat.getSessionActivity();
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public int getPlayerState() {
        int i;
        synchronized (this.mLock) {
            i = this.mPlayerState;
        }
        return i;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public long getDuration() {
        synchronized (this.mLock) {
            if (this.mMediaMetadataCompat == null || !this.mMediaMetadataCompat.containsKey("android.media.metadata.DURATION")) {
                return -1;
            }
            return this.mMediaMetadataCompat.getLong("android.media.metadata.DURATION");
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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
                return Math.max(0L, this.mPlaybackStateCompat.getPosition() + ((long) (this.mPlaybackStateCompat.getPlaybackSpeed() * ((float) timeDiff))));
            }
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public float getPlaybackSpeed() {
        synchronized (this.mLock) {
            float f = 0.0f;
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return 0.0f;
            }
            if (this.mPlaybackStateCompat != null) {
                f = this.mPlaybackStateCompat.getPlaybackSpeed();
            }
            return f;
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public int getBufferingState() {
        synchronized (this.mLock) {
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return 0;
            }
            return this.mBufferingState;
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public long getBufferedPosition() {
        synchronized (this.mLock) {
            long j = -1;
            if (!this.mConnected) {
                Log.w(TAG, "Session isn't active", new IllegalStateException());
                return -1;
            }
            if (this.mPlaybackStateCompat != null) {
                j = this.mPlaybackStateCompat.getBufferedPosition();
            }
            return j;
        }
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @Nullable
    public MediaController2.PlaybackInfo getPlaybackInfo() {
        MediaController2.PlaybackInfo playbackInfo;
        synchronized (this.mLock) {
            playbackInfo = this.mPlaybackInfo;
        }
        return playbackInfo;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
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

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @Nullable
    public List<MediaItem2> getPlaylist() {
        List<MediaItem2> list;
        synchronized (this.mLock) {
            list = this.mPlaylist;
        }
        return list;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void setPlaylist(@NonNull List<MediaItem2> list, @Nullable MediaMetadata2 metadata) {
        Bundle args = new Bundle();
        args.putParcelableArray("android.support.v4.media.argument.PLAYLIST", MediaUtils2.convertMediaItem2ListToParcelableArray(list));
        args.putBundle("android.support.v4.media.argument.PLAYLIST_METADATA", metadata == null ? null : metadata.toBundle());
        sendCommand(19, args);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void updatePlaylistMetadata(@Nullable MediaMetadata2 metadata) {
        Bundle args = new Bundle();
        args.putBundle("android.support.v4.media.argument.PLAYLIST_METADATA", metadata == null ? null : metadata.toBundle());
        sendCommand(21, args);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @Nullable
    public MediaMetadata2 getPlaylistMetadata() {
        MediaMetadata2 mediaMetadata2;
        synchronized (this.mLock) {
            mediaMetadata2 = this.mPlaylistMetadata;
        }
        return mediaMetadata2;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void addPlaylistItem(int index, @NonNull MediaItem2 item) {
        Bundle args = new Bundle();
        args.putInt("android.support.v4.media.argument.PLAYLIST_INDEX", index);
        args.putBundle("android.support.v4.media.argument.MEDIA_ITEM", item.toBundle());
        sendCommand(15, args);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void removePlaylistItem(@NonNull MediaItem2 item) {
        Bundle args = new Bundle();
        args.putBundle("android.support.v4.media.argument.MEDIA_ITEM", item.toBundle());
        sendCommand(16, args);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void replacePlaylistItem(int index, @NonNull MediaItem2 item) {
        Bundle args = new Bundle();
        args.putInt("android.support.v4.media.argument.PLAYLIST_INDEX", index);
        args.putBundle("android.support.v4.media.argument.MEDIA_ITEM", item.toBundle());
        sendCommand(17, args);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public MediaItem2 getCurrentMediaItem() {
        MediaItem2 mediaItem2;
        synchronized (this.mLock) {
            mediaItem2 = this.mCurrentMediaItem;
        }
        return mediaItem2;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void skipToPreviousItem() {
        sendCommand(5);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void skipToNextItem() {
        sendCommand(4);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void skipToPlaylistItem(@NonNull MediaItem2 item) {
        Bundle args = new Bundle();
        args.putBundle("android.support.v4.media.argument.MEDIA_ITEM", item.toBundle());
        sendCommand(12, args);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public int getRepeatMode() {
        int i;
        synchronized (this.mLock) {
            i = this.mRepeatMode;
        }
        return i;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void setRepeatMode(int repeatMode) {
        Bundle args = new Bundle();
        args.putInt("android.support.v4.media.argument.REPEAT_MODE", repeatMode);
        sendCommand(14, args);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public int getShuffleMode() {
        int i;
        synchronized (this.mLock) {
            i = this.mShuffleMode;
        }
        return i;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void setShuffleMode(int shuffleMode) {
        Bundle args = new Bundle();
        args.putInt("android.support.v4.media.argument.SHUFFLE_MODE", shuffleMode);
        sendCommand(13, args);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void subscribeRoutesInfo() {
        sendCommand(36);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void unsubscribeRoutesInfo() {
        sendCommand(37);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    public void selectRoute(@NonNull Bundle route) {
        Bundle args = new Bundle();
        args.putBundle("android.support.v4.media.argument.ROUTE_BUNDLE", route);
        sendCommand(38, args);
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @NonNull
    public Context getContext() {
        return this.mContext;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @NonNull
    public MediaController2.ControllerCallback getCallback() {
        return this.mCallback;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @NonNull
    public Executor getCallbackExecutor() {
        return this.mCallbackExecutor;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @Nullable
    public MediaBrowserCompat getBrowserCompat() {
        MediaBrowserCompat mediaBrowserCompat;
        synchronized (this.mLock) {
            mediaBrowserCompat = this.mBrowserCompat;
        }
        return mediaBrowserCompat;
    }

    @Override // android.support.v4.media.MediaController2.SupportLibraryImpl
    @NonNull
    public MediaController2 getInstance() {
        return this.mInstance;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00b9, code lost:
        r14.mCallbackExecutor.execute(new android.support.v4.media.MediaController2ImplLegacy.AnonymousClass2(r14));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00c3, code lost:
        if (0 == 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00c5, code lost:
        close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
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
            if (1 != 0) {
                close();
            }
        } finally {
            if (0 != 0) {
                close();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToSession(MediaSessionCompat.Token sessionCompatToken) {
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
            sendCommand("android.support.v4.media.controller.command.CONNECT", new ResultReceiver(this.mHandler) {
                /* class android.support.v4.media.MediaController2ImplLegacy.AnonymousClass3 */

                /* access modifiers changed from: protected */
                @Override // android.os.ResultReceiver
                public void onReceiveResult(int resultCode, Bundle resultData) {
                    if (MediaController2ImplLegacy.this.mHandlerThread.isAlive()) {
                        switch (resultCode) {
                            case -1:
                                MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                                    /* class android.support.v4.media.MediaController2ImplLegacy.AnonymousClass3.AnonymousClass1 */

                                    @Override // java.lang.Runnable
                                    public void run() {
                                        MediaController2ImplLegacy.this.mCallback.onDisconnected(MediaController2ImplLegacy.this.mInstance);
                                    }
                                });
                                MediaController2ImplLegacy.this.close();
                                return;
                            case 0:
                                MediaController2ImplLegacy.this.onConnectedNotLocked(resultData);
                                return;
                            default:
                                return;
                        }
                    }
                }
            });
        }
    }

    private void connectToService() {
        this.mCallbackExecutor.execute(new Runnable() {
            /* class android.support.v4.media.MediaController2ImplLegacy.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (MediaController2ImplLegacy.this.mLock) {
                    MediaController2ImplLegacy.this.mBrowserCompat = new MediaBrowserCompat(MediaController2ImplLegacy.this.mContext, MediaController2ImplLegacy.this.mToken.getComponentName(), new ConnectionCallback(), MediaController2ImplLegacy.sDefaultRootExtras);
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
    /* access modifiers changed from: public */
    private void sendCommand(String command, ResultReceiver receiver) {
        sendCommand(command, null, receiver);
    }

    private void sendCommand(String command, Bundle args, ResultReceiver receiver) {
        Throwable th;
        if (args == null) {
            args = new Bundle();
        }
        synchronized (this.mLock) {
            try {
                MediaControllerCompat controller = this.mControllerCompat;
                try {
                    BundleCompat.putBinder(args, "android.support.v4.media.argument.ICONTROLLER_CALLBACK", this.mControllerCompatCallback.getIControllerCallback().asBinder());
                    args.putString("android.support.v4.media.argument.PACKAGE_NAME", this.mContext.getPackageName());
                    args.putInt("android.support.v4.media.argument.UID", Process.myUid());
                    args.putInt("android.support.v4.media.argument.PID", Process.myPid());
                    controller.sendCommand(command, args, receiver);
                } catch (Throwable th2) {
                    th = th2;
                    while (true) {
                        try {
                            break;
                        } catch (Throwable th3) {
                            th = th3;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    private class ConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        private ConnectionCallback() {
        }

        @Override // android.support.v4.media.MediaBrowserCompat.ConnectionCallback
        public void onConnected() {
            MediaBrowserCompat browser = MediaController2ImplLegacy.this.getBrowserCompat();
            if (browser != null) {
                MediaController2ImplLegacy.this.connectToSession(browser.getSessionToken());
            } else if (MediaController2ImplLegacy.DEBUG) {
                Log.d(MediaController2ImplLegacy.TAG, "Controller is closed prematually", new IllegalStateException());
            }
        }

        @Override // android.support.v4.media.MediaBrowserCompat.ConnectionCallback
        public void onConnectionSuspended() {
            MediaController2ImplLegacy.this.close();
        }

        @Override // android.support.v4.media.MediaBrowserCompat.ConnectionCallback
        public void onConnectionFailed() {
            MediaController2ImplLegacy.this.close();
        }
    }

    /* access modifiers changed from: private */
    public final class ControllerCompatCallback extends MediaControllerCompat.Callback {
        private ControllerCompatCallback() {
        }

        @Override // android.support.v4.media.session.MediaControllerCompat.Callback
        public void onSessionReady() {
            MediaController2ImplLegacy.this.sendCommand("android.support.v4.media.controller.command.CONNECT", new ResultReceiver(MediaController2ImplLegacy.this.mHandler) {
                /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass1 */

                /* access modifiers changed from: protected */
                @Override // android.os.ResultReceiver
                public void onReceiveResult(int resultCode, Bundle resultData) {
                    if (MediaController2ImplLegacy.this.mHandlerThread.isAlive()) {
                        switch (resultCode) {
                            case -1:
                                MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                                    /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass1.AnonymousClass1 */

                                    @Override // java.lang.Runnable
                                    public void run() {
                                        MediaController2ImplLegacy.this.mCallback.onDisconnected(MediaController2ImplLegacy.this.mInstance);
                                    }
                                });
                                MediaController2ImplLegacy.this.close();
                                return;
                            case 0:
                                MediaController2ImplLegacy.this.onConnectedNotLocked(resultData);
                                return;
                            default:
                                return;
                        }
                    }
                }
            });
        }

        @Override // android.support.v4.media.session.MediaControllerCompat.Callback
        public void onSessionDestroyed() {
            MediaController2ImplLegacy.this.close();
        }

        @Override // android.support.v4.media.session.MediaControllerCompat.Callback
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            synchronized (MediaController2ImplLegacy.this.mLock) {
                MediaController2ImplLegacy.this.mPlaybackStateCompat = state;
            }
        }

        @Override // android.support.v4.media.session.MediaControllerCompat.Callback
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            synchronized (MediaController2ImplLegacy.this.mLock) {
                MediaController2ImplLegacy.this.mMediaMetadataCompat = metadata;
            }
        }

        @Override // android.support.v4.media.session.MediaControllerCompat.Callback
        public void onSessionEvent(String event, Bundle extras) {
            if (extras != null) {
                extras.setClassLoader(MediaSession2.class.getClassLoader());
            }
            char c = 65535;
            switch (event.hashCode()) {
                case -2076894204:
                    if (event.equals("android.support.v4.media.session.event.ON_BUFFERING_STATE_CHANGED")) {
                        c = '\r';
                        break;
                    }
                    break;
                case -2060536131:
                    if (event.equals("android.support.v4.media.session.event.ON_PLAYBACK_SPEED_CHANGED")) {
                        c = '\f';
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
                        c = '\t';
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
                        c = '\n';
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
                        c = '\b';
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
                        MediaController2ImplLegacy.this.mAllowedCommands = allowedCommands;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass2 */

                        @Override // java.lang.Runnable
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onAllowedCommandsChanged(MediaController2ImplLegacy.this.mInstance, allowedCommands);
                        }
                    });
                    return;
                case 1:
                    final int playerState = extras.getInt("android.support.v4.media.argument.PLAYER_STATE");
                    PlaybackStateCompat state = (PlaybackStateCompat) extras.getParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT");
                    if (state != null) {
                        synchronized (MediaController2ImplLegacy.this.mLock) {
                            MediaController2ImplLegacy.this.mPlayerState = playerState;
                            MediaController2ImplLegacy.this.mPlaybackStateCompat = state;
                        }
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass3 */

                            @Override // java.lang.Runnable
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onPlayerStateChanged(MediaController2ImplLegacy.this.mInstance, playerState);
                            }
                        });
                        return;
                    }
                    return;
                case 2:
                    final MediaItem2 item = MediaItem2.fromBundle(extras.getBundle("android.support.v4.media.argument.MEDIA_ITEM"));
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        MediaController2ImplLegacy.this.mCurrentMediaItem = item;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass4 */

                        @Override // java.lang.Runnable
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onCurrentMediaItemChanged(MediaController2ImplLegacy.this.mInstance, item);
                        }
                    });
                    return;
                case 3:
                    final int errorCode = extras.getInt("android.support.v4.media.argument.ERROR_CODE");
                    final Bundle errorExtras = extras.getBundle("android.support.v4.media.argument.EXTRAS");
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass5 */

                        @Override // java.lang.Runnable
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onError(MediaController2ImplLegacy.this.mInstance, errorCode, errorExtras);
                        }
                    });
                    return;
                case 4:
                    final List<Bundle> routes = MediaUtils2.convertToBundleList(extras.getParcelableArray("android.support.v4.media.argument.ROUTE_BUNDLE"));
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass6 */

                        @Override // java.lang.Runnable
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onRoutesInfoChanged(MediaController2ImplLegacy.this.mInstance, routes);
                        }
                    });
                    return;
                case 5:
                    final MediaMetadata2 playlistMetadata = MediaMetadata2.fromBundle(extras.getBundle("android.support.v4.media.argument.PLAYLIST_METADATA"));
                    final List<MediaItem2> playlist = MediaUtils2.convertToMediaItem2List(extras.getParcelableArray("android.support.v4.media.argument.PLAYLIST"));
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        MediaController2ImplLegacy.this.mPlaylist = playlist;
                        MediaController2ImplLegacy.this.mPlaylistMetadata = playlistMetadata;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass7 */

                        @Override // java.lang.Runnable
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onPlaylistChanged(MediaController2ImplLegacy.this.mInstance, playlist, playlistMetadata);
                        }
                    });
                    return;
                case 6:
                    final MediaMetadata2 playlistMetadata2 = MediaMetadata2.fromBundle(extras.getBundle("android.support.v4.media.argument.PLAYLIST_METADATA"));
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        MediaController2ImplLegacy.this.mPlaylistMetadata = playlistMetadata2;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass8 */

                        @Override // java.lang.Runnable
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onPlaylistMetadataChanged(MediaController2ImplLegacy.this.mInstance, playlistMetadata2);
                        }
                    });
                    return;
                case 7:
                    final int repeatMode = extras.getInt("android.support.v4.media.argument.REPEAT_MODE");
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        MediaController2ImplLegacy.this.mRepeatMode = repeatMode;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass9 */

                        @Override // java.lang.Runnable
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onRepeatModeChanged(MediaController2ImplLegacy.this.mInstance, repeatMode);
                        }
                    });
                    return;
                case '\b':
                    final int shuffleMode = extras.getInt("android.support.v4.media.argument.SHUFFLE_MODE");
                    synchronized (MediaController2ImplLegacy.this.mLock) {
                        MediaController2ImplLegacy.this.mShuffleMode = shuffleMode;
                    }
                    MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                        /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass10 */

                        @Override // java.lang.Runnable
                        public void run() {
                            MediaController2ImplLegacy.this.mCallback.onShuffleModeChanged(MediaController2ImplLegacy.this.mInstance, shuffleMode);
                        }
                    });
                    return;
                case '\t':
                    Bundle commandBundle = extras.getBundle("android.support.v4.media.argument.CUSTOM_COMMAND");
                    if (commandBundle != null) {
                        final SessionCommand2 command = SessionCommand2.fromBundle(commandBundle);
                        final Bundle args = extras.getBundle("android.support.v4.media.argument.ARGUMENTS");
                        final ResultReceiver receiver = (ResultReceiver) extras.getParcelable("android.support.v4.media.argument.RESULT_RECEIVER");
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass11 */

                            @Override // java.lang.Runnable
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onCustomCommand(MediaController2ImplLegacy.this.mInstance, command, args, receiver);
                            }
                        });
                        return;
                    }
                    return;
                case '\n':
                    final List<MediaSession2.CommandButton> layout = MediaUtils2.convertToCommandButtonList(extras.getParcelableArray("android.support.v4.media.argument.COMMAND_BUTTONS"));
                    if (layout != null) {
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass12 */

                            @Override // java.lang.Runnable
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onCustomLayoutChanged(MediaController2ImplLegacy.this.mInstance, layout);
                            }
                        });
                        return;
                    }
                    return;
                case 11:
                    final MediaController2.PlaybackInfo info = MediaController2.PlaybackInfo.fromBundle(extras.getBundle("android.support.v4.media.argument.PLAYBACK_INFO"));
                    if (info != null) {
                        synchronized (MediaController2ImplLegacy.this.mLock) {
                            MediaController2ImplLegacy.this.mPlaybackInfo = info;
                        }
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass13 */

                            @Override // java.lang.Runnable
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onPlaybackInfoChanged(MediaController2ImplLegacy.this.mInstance, info);
                            }
                        });
                        return;
                    }
                    return;
                case '\f':
                    final PlaybackStateCompat state2 = (PlaybackStateCompat) extras.getParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT");
                    if (state2 != null) {
                        synchronized (MediaController2ImplLegacy.this.mLock) {
                            MediaController2ImplLegacy.this.mPlaybackStateCompat = state2;
                        }
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass14 */

                            @Override // java.lang.Runnable
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onPlaybackSpeedChanged(MediaController2ImplLegacy.this.mInstance, state2.getPlaybackSpeed());
                            }
                        });
                        return;
                    }
                    return;
                case '\r':
                    final MediaItem2 item2 = MediaItem2.fromBundle(extras.getBundle("android.support.v4.media.argument.MEDIA_ITEM"));
                    final int bufferingState = extras.getInt("android.support.v4.media.argument.BUFFERING_STATE");
                    PlaybackStateCompat state3 = (PlaybackStateCompat) extras.getParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT");
                    if (item2 != null && state3 != null) {
                        synchronized (MediaController2ImplLegacy.this.mLock) {
                            MediaController2ImplLegacy.this.mBufferingState = bufferingState;
                            MediaController2ImplLegacy.this.mPlaybackStateCompat = state3;
                        }
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass15 */

                            @Override // java.lang.Runnable
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onBufferingStateChanged(MediaController2ImplLegacy.this.mInstance, item2, bufferingState);
                            }
                        });
                        return;
                    }
                    return;
                case 14:
                    final long position = extras.getLong("android.support.v4.media.argument.SEEK_POSITION");
                    PlaybackStateCompat state4 = (PlaybackStateCompat) extras.getParcelable("android.support.v4.media.argument.PLAYBACK_STATE_COMPAT");
                    if (state4 != null) {
                        synchronized (MediaController2ImplLegacy.this.mLock) {
                            MediaController2ImplLegacy.this.mPlaybackStateCompat = state4;
                        }
                        MediaController2ImplLegacy.this.mCallbackExecutor.execute(new Runnable() {
                            /* class android.support.v4.media.MediaController2ImplLegacy.ControllerCompatCallback.AnonymousClass16 */

                            @Override // java.lang.Runnable
                            public void run() {
                                MediaController2ImplLegacy.this.mCallback.onSeekCompleted(MediaController2ImplLegacy.this.mInstance, position);
                            }
                        });
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }
}
