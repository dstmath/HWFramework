package android.support.v4.media.session;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.Rating;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.media.RemoteControlClient.OnMetadataUpdateListener;
import android.media.RemoteControlClient.OnPlaybackPositionUpdateListener;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.app.BundleCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.MediaMetadataCompat.Builder;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v4.media.session.IMediaSession.Stub;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MediaSessionCompat {
    static final String ACTION_ARGUMENT_CAPTIONING_ENABLED = "android.support.v4.media.session.action.ARGUMENT_CAPTIONING_ENABLED";
    static final String ACTION_ARGUMENT_EXTRAS = "android.support.v4.media.session.action.ARGUMENT_EXTRAS";
    public static final String ACTION_ARGUMENT_MEDIA_ATTRIBUTE = "android.support.v4.media.session.action.ARGUMENT_MEDIA_ATTRIBUTE";
    public static final String ACTION_ARGUMENT_MEDIA_ATTRIBUTE_VALUE = "android.support.v4.media.session.action.ARGUMENT_MEDIA_ATTRIBUTE_VALUE";
    static final String ACTION_ARGUMENT_MEDIA_ID = "android.support.v4.media.session.action.ARGUMENT_MEDIA_ID";
    static final String ACTION_ARGUMENT_QUERY = "android.support.v4.media.session.action.ARGUMENT_QUERY";
    static final String ACTION_ARGUMENT_REPEAT_MODE = "android.support.v4.media.session.action.ARGUMENT_REPEAT_MODE";
    static final String ACTION_ARGUMENT_SHUFFLE_MODE = "android.support.v4.media.session.action.ARGUMENT_SHUFFLE_MODE";
    static final String ACTION_ARGUMENT_SHUFFLE_MODE_ENABLED = "android.support.v4.media.session.action.ARGUMENT_SHUFFLE_MODE_ENABLED";
    static final String ACTION_ARGUMENT_URI = "android.support.v4.media.session.action.ARGUMENT_URI";
    public static final String ACTION_FLAG_AS_INAPPROPRIATE = "android.support.v4.media.session.action.FLAG_AS_INAPPROPRIATE";
    public static final String ACTION_FOLLOW = "android.support.v4.media.session.action.FOLLOW";
    static final String ACTION_PLAY_FROM_URI = "android.support.v4.media.session.action.PLAY_FROM_URI";
    static final String ACTION_PREPARE = "android.support.v4.media.session.action.PREPARE";
    static final String ACTION_PREPARE_FROM_MEDIA_ID = "android.support.v4.media.session.action.PREPARE_FROM_MEDIA_ID";
    static final String ACTION_PREPARE_FROM_SEARCH = "android.support.v4.media.session.action.PREPARE_FROM_SEARCH";
    static final String ACTION_PREPARE_FROM_URI = "android.support.v4.media.session.action.PREPARE_FROM_URI";
    static final String ACTION_SET_CAPTIONING_ENABLED = "android.support.v4.media.session.action.SET_CAPTIONING_ENABLED";
    static final String ACTION_SET_REPEAT_MODE = "android.support.v4.media.session.action.SET_REPEAT_MODE";
    static final String ACTION_SET_SHUFFLE_MODE = "android.support.v4.media.session.action.SET_SHUFFLE_MODE";
    static final String ACTION_SET_SHUFFLE_MODE_ENABLED = "android.support.v4.media.session.action.SET_SHUFFLE_MODE_ENABLED";
    public static final String ACTION_SKIP_AD = "android.support.v4.media.session.action.SKIP_AD";
    public static final String ACTION_UNFOLLOW = "android.support.v4.media.session.action.UNFOLLOW";
    static final String EXTRA_BINDER = "android.support.v4.media.session.EXTRA_BINDER";
    public static final int FLAG_HANDLES_MEDIA_BUTTONS = 1;
    public static final int FLAG_HANDLES_QUEUE_COMMANDS = 4;
    public static final int FLAG_HANDLES_TRANSPORT_CONTROLS = 2;
    private static final int MAX_BITMAP_SIZE_IN_DP = 320;
    public static final int MEDIA_ATTRIBUTE_ALBUM = 1;
    public static final int MEDIA_ATTRIBUTE_ARTIST = 0;
    public static final int MEDIA_ATTRIBUTE_PLAYLIST = 2;
    static final String TAG = "MediaSessionCompat";
    static int sMaxBitmapSize;
    private final ArrayList<OnActiveChangeListener> mActiveListeners;
    private final MediaControllerCompat mController;
    private final MediaSessionImpl mImpl;

    public static abstract class Callback {
        private CallbackHandler mCallbackHandler = null;
        final Object mCallbackObj;
        private boolean mMediaPlayPauseKeyHandled;
        private WeakReference<MediaSessionImpl> mSessionImpl;

        private class CallbackHandler extends Handler {
            private static final int MSG_MEDIA_PLAY_PAUSE_KEY_DOUBLE_TAP_TIMEOUT = 1;

            CallbackHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    Callback.this.handleMediaPlayPauseKeySingleTapIfUnhandled();
                }
            }
        }

        @RequiresApi(21)
        private class StubApi21 implements Callback {
            StubApi21() {
            }

            public void onCommand(String command, Bundle extras, ResultReceiver cb) {
                IBinder iBinder = null;
                try {
                    MediaSessionImplApi21 impl;
                    if (command.equals("android.support.v4.media.session.command.GET_EXTRA_BINDER")) {
                        impl = (MediaSessionImplApi21) Callback.this.mSessionImpl.get();
                        if (impl != null) {
                            Bundle result = new Bundle();
                            IMediaSession extraBinder = impl.getSessionToken().getExtraBinder();
                            String str = MediaSessionCompat.EXTRA_BINDER;
                            if (extraBinder != null) {
                                iBinder = extraBinder.asBinder();
                            }
                            BundleCompat.putBinder(result, str, iBinder);
                            cb.send(0, result);
                        }
                    } else if (command.equals("android.support.v4.media.session.command.ADD_QUEUE_ITEM")) {
                        extras.setClassLoader(MediaDescriptionCompat.class.getClassLoader());
                        Callback.this.onAddQueueItem((MediaDescriptionCompat) extras.getParcelable("android.support.v4.media.session.command.ARGUMENT_MEDIA_DESCRIPTION"));
                    } else if (command.equals("android.support.v4.media.session.command.ADD_QUEUE_ITEM_AT")) {
                        extras.setClassLoader(MediaDescriptionCompat.class.getClassLoader());
                        Callback.this.onAddQueueItem((MediaDescriptionCompat) extras.getParcelable("android.support.v4.media.session.command.ARGUMENT_MEDIA_DESCRIPTION"), extras.getInt("android.support.v4.media.session.command.ARGUMENT_INDEX"));
                    } else if (command.equals("android.support.v4.media.session.command.REMOVE_QUEUE_ITEM")) {
                        extras.setClassLoader(MediaDescriptionCompat.class.getClassLoader());
                        Callback.this.onRemoveQueueItem((MediaDescriptionCompat) extras.getParcelable("android.support.v4.media.session.command.ARGUMENT_MEDIA_DESCRIPTION"));
                    } else if (command.equals("android.support.v4.media.session.command.REMOVE_QUEUE_ITEM_AT")) {
                        impl = (MediaSessionImplApi21) Callback.this.mSessionImpl.get();
                        if (impl != null && impl.mQueue != null) {
                            int index = extras.getInt("android.support.v4.media.session.command.ARGUMENT_INDEX", -1);
                            QueueItem item = (index < 0 || index >= impl.mQueue.size()) ? null : (QueueItem) impl.mQueue.get(index);
                            if (item != null) {
                                Callback.this.onRemoveQueueItem(item.getDescription());
                            }
                        }
                    } else {
                        Callback.this.onCommand(command, extras, cb);
                    }
                } catch (BadParcelableException e) {
                    Log.e(MediaSessionCompat.TAG, "Could not unparcel the extra data.");
                }
            }

            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                return Callback.this.onMediaButtonEvent(mediaButtonIntent);
            }

            public void onPlay() {
                Callback.this.onPlay();
            }

            public void onPlayFromMediaId(String mediaId, Bundle extras) {
                Callback.this.onPlayFromMediaId(mediaId, extras);
            }

            public void onPlayFromSearch(String search, Bundle extras) {
                Callback.this.onPlayFromSearch(search, extras);
            }

            public void onSkipToQueueItem(long id) {
                Callback.this.onSkipToQueueItem(id);
            }

            public void onPause() {
                Callback.this.onPause();
            }

            public void onSkipToNext() {
                Callback.this.onSkipToNext();
            }

            public void onSkipToPrevious() {
                Callback.this.onSkipToPrevious();
            }

            public void onFastForward() {
                Callback.this.onFastForward();
            }

            public void onRewind() {
                Callback.this.onRewind();
            }

            public void onStop() {
                Callback.this.onStop();
            }

            public void onSeekTo(long pos) {
                Callback.this.onSeekTo(pos);
            }

            public void onSetRating(Object ratingObj) {
                Callback.this.onSetRating(RatingCompat.fromRating(ratingObj));
            }

            public void onCustomAction(String action, Bundle extras) {
                if (action.equals(MediaSessionCompat.ACTION_PLAY_FROM_URI)) {
                    Callback.this.onPlayFromUri((Uri) extras.getParcelable(MediaSessionCompat.ACTION_ARGUMENT_URI), (Bundle) extras.getParcelable(MediaSessionCompat.ACTION_ARGUMENT_EXTRAS));
                } else if (action.equals(MediaSessionCompat.ACTION_PREPARE)) {
                    Callback.this.onPrepare();
                } else if (action.equals(MediaSessionCompat.ACTION_PREPARE_FROM_MEDIA_ID)) {
                    Callback.this.onPrepareFromMediaId(extras.getString(MediaSessionCompat.ACTION_ARGUMENT_MEDIA_ID), extras.getBundle(MediaSessionCompat.ACTION_ARGUMENT_EXTRAS));
                } else if (action.equals(MediaSessionCompat.ACTION_PREPARE_FROM_SEARCH)) {
                    Callback.this.onPrepareFromSearch(extras.getString(MediaSessionCompat.ACTION_ARGUMENT_QUERY), extras.getBundle(MediaSessionCompat.ACTION_ARGUMENT_EXTRAS));
                } else if (action.equals(MediaSessionCompat.ACTION_PREPARE_FROM_URI)) {
                    Callback.this.onPrepareFromUri((Uri) extras.getParcelable(MediaSessionCompat.ACTION_ARGUMENT_URI), extras.getBundle(MediaSessionCompat.ACTION_ARGUMENT_EXTRAS));
                } else if (action.equals(MediaSessionCompat.ACTION_SET_CAPTIONING_ENABLED)) {
                    Callback.this.onSetCaptioningEnabled(extras.getBoolean(MediaSessionCompat.ACTION_ARGUMENT_CAPTIONING_ENABLED));
                } else if (action.equals(MediaSessionCompat.ACTION_SET_REPEAT_MODE)) {
                    Callback.this.onSetRepeatMode(extras.getInt(MediaSessionCompat.ACTION_ARGUMENT_REPEAT_MODE));
                } else if (action.equals(MediaSessionCompat.ACTION_SET_SHUFFLE_MODE_ENABLED)) {
                    Callback.this.onSetShuffleModeEnabled(extras.getBoolean(MediaSessionCompat.ACTION_ARGUMENT_SHUFFLE_MODE_ENABLED));
                } else if (action.equals(MediaSessionCompat.ACTION_SET_SHUFFLE_MODE)) {
                    Callback.this.onSetShuffleMode(extras.getInt(MediaSessionCompat.ACTION_ARGUMENT_SHUFFLE_MODE));
                } else {
                    Callback.this.onCustomAction(action, extras);
                }
            }
        }

        @RequiresApi(23)
        private class StubApi23 extends StubApi21 implements android.support.v4.media.session.MediaSessionCompatApi23.Callback {
            StubApi23() {
                super();
            }

            public void onPlayFromUri(Uri uri, Bundle extras) {
                Callback.this.onPlayFromUri(uri, extras);
            }
        }

        @RequiresApi(24)
        private class StubApi24 extends StubApi23 implements android.support.v4.media.session.MediaSessionCompatApi24.Callback {
            StubApi24() {
                super();
            }

            public void onPrepare() {
                Callback.this.onPrepare();
            }

            public void onPrepareFromMediaId(String mediaId, Bundle extras) {
                Callback.this.onPrepareFromMediaId(mediaId, extras);
            }

            public void onPrepareFromSearch(String query, Bundle extras) {
                Callback.this.onPrepareFromSearch(query, extras);
            }

            public void onPrepareFromUri(Uri uri, Bundle extras) {
                Callback.this.onPrepareFromUri(uri, extras);
            }
        }

        public Callback() {
            if (VERSION.SDK_INT >= 24) {
                this.mCallbackObj = MediaSessionCompatApi24.createCallback(new StubApi24());
            } else if (VERSION.SDK_INT >= 23) {
                this.mCallbackObj = MediaSessionCompatApi23.createCallback(new StubApi23());
            } else if (VERSION.SDK_INT >= 21) {
                this.mCallbackObj = MediaSessionCompatApi21.createCallback(new StubApi21());
            } else {
                this.mCallbackObj = null;
            }
        }

        private void setSessionImpl(MediaSessionImpl impl, Handler handler) {
            this.mSessionImpl = new WeakReference(impl);
            if (this.mCallbackHandler != null) {
                this.mCallbackHandler.removeCallbacksAndMessages(null);
            }
            this.mCallbackHandler = new CallbackHandler(handler.getLooper());
        }

        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
        }

        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            MediaSessionImpl impl = (MediaSessionImpl) this.mSessionImpl.get();
            if (impl == null || this.mCallbackHandler == null) {
                return false;
            }
            KeyEvent keyEvent = (KeyEvent) mediaButtonEvent.getParcelableExtra("android.intent.extra.KEY_EVENT");
            if (keyEvent == null || keyEvent.getAction() != 0) {
                return false;
            }
            switch (keyEvent.getKeyCode()) {
                case 79:
                case 85:
                    if (keyEvent.getRepeatCount() > 0) {
                        this.mCallbackHandler.removeMessages(1);
                        if (keyEvent.getRepeatCount() == 1) {
                            handleMediaPlayPauseKeySingleTapIfUnhandled();
                        }
                    } else if (this.mCallbackHandler.hasMessages(1)) {
                        this.mCallbackHandler.removeMessages(1);
                        PlaybackStateCompat state = impl.getPlaybackState();
                        if ((32 & (state == null ? 0 : state.getActions())) != 0) {
                            onSkipToNext();
                        }
                        this.mMediaPlayPauseKeyHandled = true;
                    } else {
                        this.mMediaPlayPauseKeyHandled = false;
                        this.mCallbackHandler.sendEmptyMessageDelayed(1, (long) ViewConfiguration.getDoubleTapTimeout());
                    }
                    return true;
                default:
                    return false;
            }
        }

        private void handleMediaPlayPauseKeySingleTapIfUnhandled() {
            if (!this.mMediaPlayPauseKeyHandled) {
                MediaSessionImpl impl = (MediaSessionImpl) this.mSessionImpl.get();
                if (impl != null) {
                    this.mMediaPlayPauseKeyHandled = true;
                    PlaybackStateCompat state = impl.getPlaybackState();
                    long validActions = state == null ? 0 : state.getActions();
                    boolean isPlaying = state != null ? state.getState() == 3 : false;
                    boolean canPlay = (516 & validActions) != 0;
                    boolean canPause = (514 & validActions) != 0;
                    if (isPlaying && canPause) {
                        onPause();
                    } else if (!isPlaying && canPlay) {
                        onPlay();
                    }
                }
            }
        }

        public void onPrepare() {
        }

        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
        }

        public void onPrepareFromSearch(String query, Bundle extras) {
        }

        public void onPrepareFromUri(Uri uri, Bundle extras) {
        }

        public void onPlay() {
        }

        public void onPlayFromMediaId(String mediaId, Bundle extras) {
        }

        public void onPlayFromSearch(String query, Bundle extras) {
        }

        public void onPlayFromUri(Uri uri, Bundle extras) {
        }

        public void onSkipToQueueItem(long id) {
        }

        public void onPause() {
        }

        public void onSkipToNext() {
        }

        public void onSkipToPrevious() {
        }

        public void onFastForward() {
        }

        public void onRewind() {
        }

        public void onStop() {
        }

        public void onSeekTo(long pos) {
        }

        public void onSetRating(RatingCompat rating) {
        }

        public void onSetCaptioningEnabled(boolean enabled) {
        }

        public void onSetRepeatMode(int repeatMode) {
        }

        @Deprecated
        public void onSetShuffleModeEnabled(boolean enabled) {
        }

        public void onSetShuffleMode(int shuffleMode) {
        }

        public void onCustomAction(String action, Bundle extras) {
        }

        public void onAddQueueItem(MediaDescriptionCompat description) {
        }

        public void onAddQueueItem(MediaDescriptionCompat description, int index) {
        }

        public void onRemoveQueueItem(MediaDescriptionCompat description) {
        }

        @Deprecated
        public void onRemoveQueueItemAt(int index) {
        }
    }

    interface MediaSessionImpl {
        String getCallingPackage();

        Object getMediaSession();

        PlaybackStateCompat getPlaybackState();

        Object getRemoteControlClient();

        Token getSessionToken();

        boolean isActive();

        void release();

        void sendSessionEvent(String str, Bundle bundle);

        void setActive(boolean z);

        void setCallback(Callback callback, Handler handler);

        void setCaptioningEnabled(boolean z);

        void setExtras(Bundle bundle);

        void setFlags(int i);

        void setMediaButtonReceiver(PendingIntent pendingIntent);

        void setMetadata(MediaMetadataCompat mediaMetadataCompat);

        void setPlaybackState(PlaybackStateCompat playbackStateCompat);

        void setPlaybackToLocal(int i);

        void setPlaybackToRemote(VolumeProviderCompat volumeProviderCompat);

        void setQueue(List<QueueItem> list);

        void setQueueTitle(CharSequence charSequence);

        void setRatingType(int i);

        void setRepeatMode(int i);

        void setSessionActivity(PendingIntent pendingIntent);

        void setShuffleMode(int i);

        void setShuffleModeEnabled(boolean z);
    }

    static class MediaSessionImplBase implements MediaSessionImpl {
        static final int RCC_PLAYSTATE_NONE = 0;
        final AudioManager mAudioManager;
        volatile Callback mCallback;
        boolean mCaptioningEnabled;
        private final Context mContext;
        final RemoteCallbackList<IMediaControllerCallback> mControllerCallbacks = new RemoteCallbackList();
        boolean mDestroyed = false;
        Bundle mExtras;
        int mFlags;
        private MessageHandler mHandler;
        boolean mIsActive = false;
        private boolean mIsMbrRegistered = false;
        private boolean mIsRccRegistered = false;
        int mLocalStream;
        final Object mLock = new Object();
        private final ComponentName mMediaButtonReceiverComponentName;
        private final PendingIntent mMediaButtonReceiverIntent;
        MediaMetadataCompat mMetadata;
        final String mPackageName;
        List<QueueItem> mQueue;
        CharSequence mQueueTitle;
        int mRatingType;
        final RemoteControlClient mRcc;
        int mRepeatMode;
        PendingIntent mSessionActivity;
        int mShuffleMode;
        boolean mShuffleModeEnabled;
        PlaybackStateCompat mState;
        private final MediaSessionStub mStub;
        final String mTag;
        private final Token mToken;
        private android.support.v4.media.VolumeProviderCompat.Callback mVolumeCallback = new android.support.v4.media.VolumeProviderCompat.Callback() {
            public void onVolumeChanged(VolumeProviderCompat volumeProvider) {
                if (MediaSessionImplBase.this.mVolumeProvider == volumeProvider) {
                    MediaSessionImplBase.this.sendVolumeInfoChanged(new ParcelableVolumeInfo(MediaSessionImplBase.this.mVolumeType, MediaSessionImplBase.this.mLocalStream, volumeProvider.getVolumeControl(), volumeProvider.getMaxVolume(), volumeProvider.getCurrentVolume()));
                }
            }
        };
        VolumeProviderCompat mVolumeProvider;
        int mVolumeType;

        private static final class Command {
            public final String command;
            public final Bundle extras;
            public final ResultReceiver stub;

            public Command(String command, Bundle extras, ResultReceiver stub) {
                this.command = command;
                this.extras = extras;
                this.stub = stub;
            }
        }

        class MediaSessionStub extends Stub {
            MediaSessionStub() {
            }

            public void sendCommand(String command, Bundle args, ResultReceiverWrapper cb) {
                MediaSessionImplBase.this.postToHandler(1, new Command(command, args, cb.mResultReceiver));
            }

            public boolean sendMediaButton(KeyEvent mediaButton) {
                boolean handlesMediaButtons = (MediaSessionImplBase.this.mFlags & 1) != 0;
                if (handlesMediaButtons) {
                    MediaSessionImplBase.this.postToHandler(21, (Object) mediaButton);
                }
                return handlesMediaButtons;
            }

            public void registerCallbackListener(IMediaControllerCallback cb) {
                if (MediaSessionImplBase.this.mDestroyed) {
                    try {
                        cb.onSessionDestroyed();
                    } catch (Exception e) {
                    }
                } else {
                    MediaSessionImplBase.this.mControllerCallbacks.register(cb);
                }
            }

            public void unregisterCallbackListener(IMediaControllerCallback cb) {
                MediaSessionImplBase.this.mControllerCallbacks.unregister(cb);
            }

            public String getPackageName() {
                return MediaSessionImplBase.this.mPackageName;
            }

            public String getTag() {
                return MediaSessionImplBase.this.mTag;
            }

            public PendingIntent getLaunchPendingIntent() {
                PendingIntent pendingIntent;
                synchronized (MediaSessionImplBase.this.mLock) {
                    pendingIntent = MediaSessionImplBase.this.mSessionActivity;
                }
                return pendingIntent;
            }

            public long getFlags() {
                long j;
                synchronized (MediaSessionImplBase.this.mLock) {
                    j = (long) MediaSessionImplBase.this.mFlags;
                }
                return j;
            }

            public ParcelableVolumeInfo getVolumeAttributes() {
                int volumeType;
                int stream;
                int controlType;
                int max;
                int current;
                synchronized (MediaSessionImplBase.this.mLock) {
                    volumeType = MediaSessionImplBase.this.mVolumeType;
                    stream = MediaSessionImplBase.this.mLocalStream;
                    VolumeProviderCompat vp = MediaSessionImplBase.this.mVolumeProvider;
                    if (volumeType == 2) {
                        controlType = vp.getVolumeControl();
                        max = vp.getMaxVolume();
                        current = vp.getCurrentVolume();
                    } else {
                        controlType = 2;
                        max = MediaSessionImplBase.this.mAudioManager.getStreamMaxVolume(stream);
                        current = MediaSessionImplBase.this.mAudioManager.getStreamVolume(stream);
                    }
                }
                return new ParcelableVolumeInfo(volumeType, stream, controlType, max, current);
            }

            public void adjustVolume(int direction, int flags, String packageName) {
                MediaSessionImplBase.this.adjustVolume(direction, flags);
            }

            public void setVolumeTo(int value, int flags, String packageName) {
                MediaSessionImplBase.this.setVolumeTo(value, flags);
            }

            public void prepare() throws RemoteException {
                MediaSessionImplBase.this.postToHandler(3);
            }

            public void prepareFromMediaId(String mediaId, Bundle extras) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(4, (Object) mediaId, extras);
            }

            public void prepareFromSearch(String query, Bundle extras) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(5, (Object) query, extras);
            }

            public void prepareFromUri(Uri uri, Bundle extras) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(6, (Object) uri, extras);
            }

            public void play() throws RemoteException {
                MediaSessionImplBase.this.postToHandler(7);
            }

            public void playFromMediaId(String mediaId, Bundle extras) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(8, (Object) mediaId, extras);
            }

            public void playFromSearch(String query, Bundle extras) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(9, (Object) query, extras);
            }

            public void playFromUri(Uri uri, Bundle extras) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(10, (Object) uri, extras);
            }

            public void skipToQueueItem(long id) {
                MediaSessionImplBase.this.postToHandler(11, Long.valueOf(id));
            }

            public void pause() throws RemoteException {
                MediaSessionImplBase.this.postToHandler(12);
            }

            public void stop() throws RemoteException {
                MediaSessionImplBase.this.postToHandler(13);
            }

            public void next() throws RemoteException {
                MediaSessionImplBase.this.postToHandler(14);
            }

            public void previous() throws RemoteException {
                MediaSessionImplBase.this.postToHandler(15);
            }

            public void fastForward() throws RemoteException {
                MediaSessionImplBase.this.postToHandler(16);
            }

            public void rewind() throws RemoteException {
                MediaSessionImplBase.this.postToHandler(17);
            }

            public void seekTo(long pos) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(18, Long.valueOf(pos));
            }

            public void rate(RatingCompat rating) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(19, (Object) rating);
            }

            public void setCaptioningEnabled(boolean enabled) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(29, Boolean.valueOf(enabled));
            }

            public void setRepeatMode(int repeatMode) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(23, repeatMode);
            }

            public void setShuffleModeEnabledDeprecated(boolean enabled) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(24, Boolean.valueOf(enabled));
            }

            public void setShuffleMode(int shuffleMode) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(30, shuffleMode);
            }

            public void sendCustomAction(String action, Bundle args) throws RemoteException {
                MediaSessionImplBase.this.postToHandler(20, (Object) action, args);
            }

            public MediaMetadataCompat getMetadata() {
                return MediaSessionImplBase.this.mMetadata;
            }

            public PlaybackStateCompat getPlaybackState() {
                PlaybackStateCompat state;
                MediaMetadataCompat metadata;
                synchronized (MediaSessionImplBase.this.mLock) {
                    state = MediaSessionImplBase.this.mState;
                    metadata = MediaSessionImplBase.this.mMetadata;
                }
                return MediaSessionCompat.getStateWithUpdatedPosition(state, metadata);
            }

            public List<QueueItem> getQueue() {
                List<QueueItem> list;
                synchronized (MediaSessionImplBase.this.mLock) {
                    list = MediaSessionImplBase.this.mQueue;
                }
                return list;
            }

            public void addQueueItem(MediaDescriptionCompat description) {
                MediaSessionImplBase.this.postToHandler(25, (Object) description);
            }

            public void addQueueItemAt(MediaDescriptionCompat description, int index) {
                MediaSessionImplBase.this.postToHandler(26, (Object) description, index);
            }

            public void removeQueueItem(MediaDescriptionCompat description) {
                MediaSessionImplBase.this.postToHandler(27, (Object) description);
            }

            public void removeQueueItemAt(int index) {
                MediaSessionImplBase.this.postToHandler(28, index);
            }

            public CharSequence getQueueTitle() {
                return MediaSessionImplBase.this.mQueueTitle;
            }

            public Bundle getExtras() {
                Bundle bundle;
                synchronized (MediaSessionImplBase.this.mLock) {
                    bundle = MediaSessionImplBase.this.mExtras;
                }
                return bundle;
            }

            public int getRatingType() {
                return MediaSessionImplBase.this.mRatingType;
            }

            public boolean isCaptioningEnabled() {
                return MediaSessionImplBase.this.mCaptioningEnabled;
            }

            public int getRepeatMode() {
                return MediaSessionImplBase.this.mRepeatMode;
            }

            public boolean isShuffleModeEnabledDeprecated() {
                return MediaSessionImplBase.this.mShuffleModeEnabled;
            }

            public int getShuffleMode() {
                return MediaSessionImplBase.this.mShuffleMode;
            }

            public boolean isTransportControlEnabled() {
                return (MediaSessionImplBase.this.mFlags & 2) != 0;
            }
        }

        class MessageHandler extends Handler {
            private static final int KEYCODE_MEDIA_PAUSE = 127;
            private static final int KEYCODE_MEDIA_PLAY = 126;
            private static final int MSG_ADD_QUEUE_ITEM = 25;
            private static final int MSG_ADD_QUEUE_ITEM_AT = 26;
            private static final int MSG_ADJUST_VOLUME = 2;
            private static final int MSG_COMMAND = 1;
            private static final int MSG_CUSTOM_ACTION = 20;
            private static final int MSG_FAST_FORWARD = 16;
            private static final int MSG_MEDIA_BUTTON = 21;
            private static final int MSG_NEXT = 14;
            private static final int MSG_PAUSE = 12;
            private static final int MSG_PLAY = 7;
            private static final int MSG_PLAY_MEDIA_ID = 8;
            private static final int MSG_PLAY_SEARCH = 9;
            private static final int MSG_PLAY_URI = 10;
            private static final int MSG_PREPARE = 3;
            private static final int MSG_PREPARE_MEDIA_ID = 4;
            private static final int MSG_PREPARE_SEARCH = 5;
            private static final int MSG_PREPARE_URI = 6;
            private static final int MSG_PREVIOUS = 15;
            private static final int MSG_RATE = 19;
            private static final int MSG_REMOVE_QUEUE_ITEM = 27;
            private static final int MSG_REMOVE_QUEUE_ITEM_AT = 28;
            private static final int MSG_REWIND = 17;
            private static final int MSG_SEEK_TO = 18;
            private static final int MSG_SET_CAPTIONING_ENABLED = 29;
            private static final int MSG_SET_REPEAT_MODE = 23;
            private static final int MSG_SET_SHUFFLE_MODE = 30;
            private static final int MSG_SET_SHUFFLE_MODE_ENABLED = 24;
            private static final int MSG_SET_VOLUME = 22;
            private static final int MSG_SKIP_TO_ITEM = 11;
            private static final int MSG_STOP = 13;

            public MessageHandler(Looper looper) {
                super(looper);
            }

            public void post(int what, Object obj, Bundle bundle) {
                Message msg = obtainMessage(what, obj);
                msg.setData(bundle);
                msg.sendToTarget();
            }

            public void post(int what, Object obj) {
                obtainMessage(what, obj).sendToTarget();
            }

            public void post(int what) {
                post(what, null);
            }

            public void post(int what, Object obj, int arg1) {
                obtainMessage(what, arg1, 0, obj).sendToTarget();
            }

            public void handleMessage(Message msg) {
                Callback cb = MediaSessionImplBase.this.mCallback;
                if (cb != null) {
                    switch (msg.what) {
                        case 1:
                            Command cmd = msg.obj;
                            cb.onCommand(cmd.command, cmd.extras, cmd.stub);
                            break;
                        case 2:
                            MediaSessionImplBase.this.adjustVolume(msg.arg1, 0);
                            break;
                        case 3:
                            cb.onPrepare();
                            break;
                        case 4:
                            cb.onPrepareFromMediaId((String) msg.obj, msg.getData());
                            break;
                        case 5:
                            cb.onPrepareFromSearch((String) msg.obj, msg.getData());
                            break;
                        case 6:
                            cb.onPrepareFromUri((Uri) msg.obj, msg.getData());
                            break;
                        case 7:
                            cb.onPlay();
                            break;
                        case 8:
                            cb.onPlayFromMediaId((String) msg.obj, msg.getData());
                            break;
                        case 9:
                            cb.onPlayFromSearch((String) msg.obj, msg.getData());
                            break;
                        case 10:
                            cb.onPlayFromUri((Uri) msg.obj, msg.getData());
                            break;
                        case 11:
                            cb.onSkipToQueueItem(((Long) msg.obj).longValue());
                            break;
                        case 12:
                            cb.onPause();
                            break;
                        case 13:
                            cb.onStop();
                            break;
                        case 14:
                            cb.onSkipToNext();
                            break;
                        case 15:
                            cb.onSkipToPrevious();
                            break;
                        case 16:
                            cb.onFastForward();
                            break;
                        case 17:
                            cb.onRewind();
                            break;
                        case 18:
                            cb.onSeekTo(((Long) msg.obj).longValue());
                            break;
                        case 19:
                            cb.onSetRating((RatingCompat) msg.obj);
                            break;
                        case 20:
                            cb.onCustomAction((String) msg.obj, msg.getData());
                            break;
                        case 21:
                            KeyEvent keyEvent = msg.obj;
                            Intent intent = new Intent("android.intent.action.MEDIA_BUTTON");
                            intent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
                            if (!cb.onMediaButtonEvent(intent)) {
                                onMediaButtonEvent(keyEvent, cb);
                                break;
                            }
                            break;
                        case 22:
                            MediaSessionImplBase.this.setVolumeTo(msg.arg1, 0);
                            break;
                        case 23:
                            cb.onSetRepeatMode(msg.arg1);
                            break;
                        case 24:
                            cb.onSetShuffleModeEnabled(((Boolean) msg.obj).booleanValue());
                            break;
                        case 25:
                            cb.onAddQueueItem((MediaDescriptionCompat) msg.obj);
                            break;
                        case 26:
                            cb.onAddQueueItem((MediaDescriptionCompat) msg.obj, msg.arg1);
                            break;
                        case 27:
                            cb.onRemoveQueueItem((MediaDescriptionCompat) msg.obj);
                            break;
                        case 28:
                            if (MediaSessionImplBase.this.mQueue != null) {
                                QueueItem item = (msg.arg1 < 0 || msg.arg1 >= MediaSessionImplBase.this.mQueue.size()) ? null : (QueueItem) MediaSessionImplBase.this.mQueue.get(msg.arg1);
                                if (item != null) {
                                    cb.onRemoveQueueItem(item.getDescription());
                                    break;
                                }
                            }
                            break;
                        case MSG_SET_CAPTIONING_ENABLED /*29*/:
                            cb.onSetCaptioningEnabled(((Boolean) msg.obj).booleanValue());
                            break;
                        case MSG_SET_SHUFFLE_MODE /*30*/:
                            cb.onSetShuffleMode(msg.arg1);
                            break;
                    }
                }
            }

            private void onMediaButtonEvent(KeyEvent ke, Callback cb) {
                if (ke != null && ke.getAction() == 0) {
                    long validActions = MediaSessionImplBase.this.mState == null ? 0 : MediaSessionImplBase.this.mState.getActions();
                    switch (ke.getKeyCode()) {
                        case 79:
                        case 85:
                            Log.w(MediaSessionCompat.TAG, "KEYCODE_MEDIA_PLAY_PAUSE and KEYCODE_HEADSETHOOK are handled already");
                            break;
                        case 86:
                            if ((1 & validActions) != 0) {
                                cb.onStop();
                                break;
                            }
                            break;
                        case 87:
                            if ((32 & validActions) != 0) {
                                cb.onSkipToNext();
                                break;
                            }
                            break;
                        case 88:
                            if ((16 & validActions) != 0) {
                                cb.onSkipToPrevious();
                                break;
                            }
                            break;
                        case 89:
                            if ((8 & validActions) != 0) {
                                cb.onRewind();
                                break;
                            }
                            break;
                        case 90:
                            if ((64 & validActions) != 0) {
                                cb.onFastForward();
                                break;
                            }
                            break;
                        case KEYCODE_MEDIA_PLAY /*126*/:
                            if ((4 & validActions) != 0) {
                                cb.onPlay();
                                break;
                            }
                            break;
                        case KEYCODE_MEDIA_PAUSE /*127*/:
                            if ((2 & validActions) != 0) {
                                cb.onPause();
                                break;
                            }
                            break;
                    }
                }
            }
        }

        public MediaSessionImplBase(Context context, String tag, ComponentName mbrComponent, PendingIntent mbrIntent) {
            if (mbrComponent == null) {
                throw new IllegalArgumentException("MediaButtonReceiver component may not be null.");
            }
            this.mContext = context;
            this.mPackageName = context.getPackageName();
            this.mAudioManager = (AudioManager) context.getSystemService("audio");
            this.mTag = tag;
            this.mMediaButtonReceiverComponentName = mbrComponent;
            this.mMediaButtonReceiverIntent = mbrIntent;
            this.mStub = new MediaSessionStub();
            this.mToken = new Token(this.mStub);
            this.mRatingType = 0;
            this.mVolumeType = 1;
            this.mLocalStream = 3;
            this.mRcc = new RemoteControlClient(mbrIntent);
        }

        public void setCallback(Callback callback, Handler handler) {
            this.mCallback = callback;
            if (callback != null) {
                if (handler == null) {
                    handler = new Handler();
                }
                synchronized (this.mLock) {
                    if (this.mHandler != null) {
                        this.mHandler.removeCallbacksAndMessages(null);
                    }
                    this.mHandler = new MessageHandler(handler.getLooper());
                    this.mCallback.setSessionImpl(this, handler);
                }
            }
        }

        void postToHandler(int what) {
            postToHandler(what, null);
        }

        void postToHandler(int what, int arg1) {
            postToHandler(what, null, arg1);
        }

        void postToHandler(int what, Object obj) {
            postToHandler(what, obj, null);
        }

        void postToHandler(int what, Object obj, int arg1) {
            synchronized (this.mLock) {
                if (this.mHandler != null) {
                    this.mHandler.post(what, obj, arg1);
                }
            }
        }

        void postToHandler(int what, Object obj, Bundle extras) {
            synchronized (this.mLock) {
                if (this.mHandler != null) {
                    this.mHandler.post(what, obj, extras);
                }
            }
        }

        public void setFlags(int flags) {
            synchronized (this.mLock) {
                this.mFlags = flags;
            }
            update();
        }

        public void setPlaybackToLocal(int stream) {
            if (this.mVolumeProvider != null) {
                this.mVolumeProvider.setCallback(null);
            }
            this.mVolumeType = 1;
            sendVolumeInfoChanged(new ParcelableVolumeInfo(this.mVolumeType, this.mLocalStream, 2, this.mAudioManager.getStreamMaxVolume(this.mLocalStream), this.mAudioManager.getStreamVolume(this.mLocalStream)));
        }

        public void setPlaybackToRemote(VolumeProviderCompat volumeProvider) {
            if (volumeProvider == null) {
                throw new IllegalArgumentException("volumeProvider may not be null");
            }
            if (this.mVolumeProvider != null) {
                this.mVolumeProvider.setCallback(null);
            }
            this.mVolumeType = 2;
            this.mVolumeProvider = volumeProvider;
            sendVolumeInfoChanged(new ParcelableVolumeInfo(this.mVolumeType, this.mLocalStream, this.mVolumeProvider.getVolumeControl(), this.mVolumeProvider.getMaxVolume(), this.mVolumeProvider.getCurrentVolume()));
            volumeProvider.setCallback(this.mVolumeCallback);
        }

        public void setActive(boolean active) {
            if (active != this.mIsActive) {
                this.mIsActive = active;
                if (update()) {
                    setMetadata(this.mMetadata);
                    setPlaybackState(this.mState);
                }
            }
        }

        public boolean isActive() {
            return this.mIsActive;
        }

        public void sendSessionEvent(String event, Bundle extras) {
            sendEvent(event, extras);
        }

        public void release() {
            this.mIsActive = false;
            this.mDestroyed = true;
            update();
            sendSessionDestroyed();
        }

        public Token getSessionToken() {
            return this.mToken;
        }

        public void setPlaybackState(PlaybackStateCompat state) {
            synchronized (this.mLock) {
                this.mState = state;
            }
            sendState(state);
            if (this.mIsActive) {
                if (state == null) {
                    this.mRcc.setPlaybackState(0);
                    this.mRcc.setTransportControlFlags(0);
                } else {
                    setRccState(state);
                    this.mRcc.setTransportControlFlags(getRccTransportControlFlagsFromActions(state.getActions()));
                }
            }
        }

        public PlaybackStateCompat getPlaybackState() {
            PlaybackStateCompat playbackStateCompat;
            synchronized (this.mLock) {
                playbackStateCompat = this.mState;
            }
            return playbackStateCompat;
        }

        void setRccState(PlaybackStateCompat state) {
            this.mRcc.setPlaybackState(getRccStateFromState(state.getState()));
        }

        int getRccStateFromState(int state) {
            switch (state) {
                case 0:
                    return 0;
                case 1:
                    return 1;
                case 2:
                    return 2;
                case 3:
                    return 3;
                case 4:
                    return 4;
                case 5:
                    return 5;
                case 6:
                case 8:
                    return 8;
                case 7:
                    return 9;
                case 9:
                    return 7;
                case 10:
                case 11:
                    return 6;
                default:
                    return -1;
            }
        }

        int getRccTransportControlFlagsFromActions(long actions) {
            int transportControlFlags = 0;
            if ((1 & actions) != 0) {
                transportControlFlags = 32;
            }
            if ((2 & actions) != 0) {
                transportControlFlags |= 16;
            }
            if ((4 & actions) != 0) {
                transportControlFlags |= 4;
            }
            if ((8 & actions) != 0) {
                transportControlFlags |= 2;
            }
            if ((16 & actions) != 0) {
                transportControlFlags |= 1;
            }
            if ((32 & actions) != 0) {
                transportControlFlags |= 128;
            }
            if ((64 & actions) != 0) {
                transportControlFlags |= 64;
            }
            if ((512 & actions) != 0) {
                return transportControlFlags | 8;
            }
            return transportControlFlags;
        }

        public void setMetadata(MediaMetadataCompat metadata) {
            Bundle bundle = null;
            if (metadata != null) {
                metadata = new Builder(metadata, MediaSessionCompat.sMaxBitmapSize).build();
            }
            synchronized (this.mLock) {
                this.mMetadata = metadata;
            }
            sendMetadata(metadata);
            if (this.mIsActive) {
                if (metadata != null) {
                    bundle = metadata.getBundle();
                }
                buildRccMetadata(bundle).apply();
            }
        }

        MetadataEditor buildRccMetadata(Bundle metadata) {
            MetadataEditor editor = this.mRcc.editMetadata(true);
            if (metadata == null) {
                return editor;
            }
            Bitmap art;
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_ART)) {
                art = (Bitmap) metadata.getParcelable(MediaMetadataCompat.METADATA_KEY_ART);
                if (art != null) {
                    art = art.copy(art.getConfig(), false);
                }
                editor.putBitmap(100, art);
            } else if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)) {
                art = (Bitmap) metadata.getParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
                if (art != null) {
                    art = art.copy(art.getConfig(), false);
                }
                editor.putBitmap(100, art);
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_ALBUM)) {
                editor.putString(1, metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST)) {
                editor.putString(13, metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_ARTIST)) {
                editor.putString(2, metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_AUTHOR)) {
                editor.putString(3, metadata.getString(MediaMetadataCompat.METADATA_KEY_AUTHOR));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_COMPILATION)) {
                editor.putString(15, metadata.getString(MediaMetadataCompat.METADATA_KEY_COMPILATION));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_COMPOSER)) {
                editor.putString(4, metadata.getString(MediaMetadataCompat.METADATA_KEY_COMPOSER));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_DATE)) {
                editor.putString(5, metadata.getString(MediaMetadataCompat.METADATA_KEY_DATE));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_DISC_NUMBER)) {
                editor.putLong(14, metadata.getLong(MediaMetadataCompat.METADATA_KEY_DISC_NUMBER));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_DURATION)) {
                editor.putLong(9, metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_GENRE)) {
                editor.putString(6, metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_TITLE)) {
                editor.putString(7, metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER)) {
                editor.putLong(0, metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_WRITER)) {
                editor.putString(11, metadata.getString(MediaMetadataCompat.METADATA_KEY_WRITER));
            }
            return editor;
        }

        public void setSessionActivity(PendingIntent pi) {
            synchronized (this.mLock) {
                this.mSessionActivity = pi;
            }
        }

        public void setMediaButtonReceiver(PendingIntent mbr) {
        }

        public void setQueue(List<QueueItem> queue) {
            this.mQueue = queue;
            sendQueue(queue);
        }

        public void setQueueTitle(CharSequence title) {
            this.mQueueTitle = title;
            sendQueueTitle(title);
        }

        public Object getMediaSession() {
            return null;
        }

        public Object getRemoteControlClient() {
            return null;
        }

        public String getCallingPackage() {
            return null;
        }

        public void setRatingType(int type) {
            this.mRatingType = type;
        }

        public void setCaptioningEnabled(boolean enabled) {
            if (this.mCaptioningEnabled != enabled) {
                this.mCaptioningEnabled = enabled;
                sendCaptioningEnabled(enabled);
            }
        }

        public void setRepeatMode(int repeatMode) {
            if (this.mRepeatMode != repeatMode) {
                this.mRepeatMode = repeatMode;
                sendRepeatMode(repeatMode);
            }
        }

        public void setShuffleModeEnabled(boolean enabled) {
            if (this.mShuffleModeEnabled != enabled) {
                this.mShuffleModeEnabled = enabled;
                sendShuffleModeEnabled(enabled);
            }
        }

        public void setShuffleMode(int shuffleMode) {
            if (this.mShuffleMode != shuffleMode) {
                this.mShuffleMode = shuffleMode;
                sendShuffleMode(shuffleMode);
            }
        }

        public void setExtras(Bundle extras) {
            this.mExtras = extras;
            sendExtras(extras);
        }

        boolean update() {
            if (this.mIsActive) {
                if (!this.mIsMbrRegistered && (this.mFlags & 1) != 0) {
                    registerMediaButtonEventReceiver(this.mMediaButtonReceiverIntent, this.mMediaButtonReceiverComponentName);
                    this.mIsMbrRegistered = true;
                } else if (this.mIsMbrRegistered && (this.mFlags & 1) == 0) {
                    unregisterMediaButtonEventReceiver(this.mMediaButtonReceiverIntent, this.mMediaButtonReceiverComponentName);
                    this.mIsMbrRegistered = false;
                }
                if (!this.mIsRccRegistered && (this.mFlags & 2) != 0) {
                    this.mAudioManager.registerRemoteControlClient(this.mRcc);
                    this.mIsRccRegistered = true;
                    return true;
                } else if (!this.mIsRccRegistered || (this.mFlags & 2) != 0) {
                    return false;
                } else {
                    this.mRcc.setPlaybackState(0);
                    this.mAudioManager.unregisterRemoteControlClient(this.mRcc);
                    this.mIsRccRegistered = false;
                    return false;
                }
            }
            if (this.mIsMbrRegistered) {
                unregisterMediaButtonEventReceiver(this.mMediaButtonReceiverIntent, this.mMediaButtonReceiverComponentName);
                this.mIsMbrRegistered = false;
            }
            if (!this.mIsRccRegistered) {
                return false;
            }
            this.mRcc.setPlaybackState(0);
            this.mAudioManager.unregisterRemoteControlClient(this.mRcc);
            this.mIsRccRegistered = false;
            return false;
        }

        void registerMediaButtonEventReceiver(PendingIntent mbrIntent, ComponentName mbrComponent) {
            this.mAudioManager.registerMediaButtonEventReceiver(mbrComponent);
        }

        void unregisterMediaButtonEventReceiver(PendingIntent mbrIntent, ComponentName mbrComponent) {
            this.mAudioManager.unregisterMediaButtonEventReceiver(mbrComponent);
        }

        void adjustVolume(int direction, int flags) {
            if (this.mVolumeType != 2) {
                this.mAudioManager.adjustStreamVolume(this.mLocalStream, direction, flags);
            } else if (this.mVolumeProvider != null) {
                this.mVolumeProvider.onAdjustVolume(direction);
            }
        }

        void setVolumeTo(int value, int flags) {
            if (this.mVolumeType != 2) {
                this.mAudioManager.setStreamVolume(this.mLocalStream, value, flags);
            } else if (this.mVolumeProvider != null) {
                this.mVolumeProvider.onSetVolumeTo(value);
            }
        }

        void sendVolumeInfoChanged(ParcelableVolumeInfo info) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onVolumeInfoChanged(info);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }

        private void sendSessionDestroyed() {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onSessionDestroyed();
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
            this.mControllerCallbacks.kill();
        }

        private void sendEvent(String event, Bundle extras) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onEvent(event, extras);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }

        private void sendState(PlaybackStateCompat state) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onPlaybackStateChanged(state);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }

        private void sendMetadata(MediaMetadataCompat metadata) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onMetadataChanged(metadata);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }

        private void sendQueue(List<QueueItem> queue) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onQueueChanged(queue);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }

        private void sendQueueTitle(CharSequence queueTitle) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onQueueTitleChanged(queueTitle);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }

        private void sendCaptioningEnabled(boolean enabled) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onCaptioningEnabledChanged(enabled);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }

        private void sendRepeatMode(int repeatMode) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onRepeatModeChanged(repeatMode);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }

        private void sendShuffleModeEnabled(boolean enabled) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onShuffleModeChangedDeprecated(enabled);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }

        private void sendShuffleMode(int shuffleMode) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onShuffleModeChanged(shuffleMode);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }

        private void sendExtras(Bundle extras) {
            for (int i = this.mControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mControllerCallbacks.getBroadcastItem(i)).onExtrasChanged(extras);
                } catch (RemoteException e) {
                }
            }
            this.mControllerCallbacks.finishBroadcast();
        }
    }

    @RequiresApi(18)
    static class MediaSessionImplApi18 extends MediaSessionImplBase {
        private static boolean sIsMbrPendingIntentSupported = true;

        MediaSessionImplApi18(Context context, String tag, ComponentName mbrComponent, PendingIntent mbrIntent) {
            super(context, tag, mbrComponent, mbrIntent);
        }

        public void setCallback(Callback callback, Handler handler) {
            super.setCallback(callback, handler);
            if (callback == null) {
                this.mRcc.setPlaybackPositionUpdateListener(null);
                return;
            }
            this.mRcc.setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener() {
                public void onPlaybackPositionUpdate(long newPositionMs) {
                    MediaSessionImplApi18.this.postToHandler(18, (Object) Long.valueOf(newPositionMs));
                }
            });
        }

        void setRccState(PlaybackStateCompat state) {
            long position = state.getPosition();
            float speed = state.getPlaybackSpeed();
            long updateTime = state.getLastPositionUpdateTime();
            long currTime = SystemClock.elapsedRealtime();
            if (state.getState() == 3 && position > 0) {
                long diff = 0;
                if (updateTime > 0) {
                    diff = currTime - updateTime;
                    if (speed > 0.0f && speed != 1.0f) {
                        diff = (long) (((float) diff) * speed);
                    }
                }
                position += diff;
            }
            this.mRcc.setPlaybackState(getRccStateFromState(state.getState()), position, speed);
        }

        int getRccTransportControlFlagsFromActions(long actions) {
            int transportControlFlags = super.getRccTransportControlFlagsFromActions(actions);
            if ((256 & actions) != 0) {
                return transportControlFlags | 256;
            }
            return transportControlFlags;
        }

        void registerMediaButtonEventReceiver(PendingIntent mbrIntent, ComponentName mbrComponent) {
            if (sIsMbrPendingIntentSupported) {
                try {
                    this.mAudioManager.registerMediaButtonEventReceiver(mbrIntent);
                } catch (NullPointerException e) {
                    Log.w(MediaSessionCompat.TAG, "Unable to register media button event receiver with PendingIntent, falling back to ComponentName.");
                    sIsMbrPendingIntentSupported = false;
                }
            }
            if (!sIsMbrPendingIntentSupported) {
                super.registerMediaButtonEventReceiver(mbrIntent, mbrComponent);
            }
        }

        void unregisterMediaButtonEventReceiver(PendingIntent mbrIntent, ComponentName mbrComponent) {
            if (sIsMbrPendingIntentSupported) {
                this.mAudioManager.unregisterMediaButtonEventReceiver(mbrIntent);
            } else {
                super.unregisterMediaButtonEventReceiver(mbrIntent, mbrComponent);
            }
        }
    }

    @RequiresApi(19)
    static class MediaSessionImplApi19 extends MediaSessionImplApi18 {
        MediaSessionImplApi19(Context context, String tag, ComponentName mbrComponent, PendingIntent mbrIntent) {
            super(context, tag, mbrComponent, mbrIntent);
        }

        public void setCallback(Callback callback, Handler handler) {
            super.setCallback(callback, handler);
            if (callback == null) {
                this.mRcc.setMetadataUpdateListener(null);
                return;
            }
            this.mRcc.setMetadataUpdateListener(new OnMetadataUpdateListener() {
                public void onMetadataUpdate(int key, Object newValue) {
                    if (key == 268435457 && (newValue instanceof Rating)) {
                        MediaSessionImplApi19.this.postToHandler(19, (Object) RatingCompat.fromRating(newValue));
                    }
                }
            });
        }

        int getRccTransportControlFlagsFromActions(long actions) {
            int transportControlFlags = super.getRccTransportControlFlagsFromActions(actions);
            if ((128 & actions) != 0) {
                return transportControlFlags | 512;
            }
            return transportControlFlags;
        }

        MetadataEditor buildRccMetadata(Bundle metadata) {
            MetadataEditor editor = super.buildRccMetadata(metadata);
            if ((128 & (this.mState == null ? 0 : this.mState.getActions())) != 0) {
                editor.addEditableKey(268435457);
            }
            if (metadata == null) {
                return editor;
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_YEAR)) {
                editor.putLong(8, metadata.getLong(MediaMetadataCompat.METADATA_KEY_YEAR));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_RATING)) {
                editor.putObject(101, metadata.getParcelable(MediaMetadataCompat.METADATA_KEY_RATING));
            }
            if (metadata.containsKey(MediaMetadataCompat.METADATA_KEY_USER_RATING)) {
                editor.putObject(268435457, metadata.getParcelable(MediaMetadataCompat.METADATA_KEY_USER_RATING));
            }
            return editor;
        }
    }

    @RequiresApi(21)
    static class MediaSessionImplApi21 implements MediaSessionImpl {
        boolean mCaptioningEnabled;
        private boolean mDestroyed = false;
        private final RemoteCallbackList<IMediaControllerCallback> mExtraControllerCallbacks = new RemoteCallbackList();
        private MediaMetadataCompat mMetadata;
        private PlaybackStateCompat mPlaybackState;
        private List<QueueItem> mQueue;
        int mRatingType;
        int mRepeatMode;
        private final Object mSessionObj;
        int mShuffleMode;
        boolean mShuffleModeEnabled;
        private final Token mToken;

        class ExtraSession extends Stub {
            ExtraSession() {
            }

            public void sendCommand(String command, Bundle args, ResultReceiverWrapper cb) {
                throw new AssertionError();
            }

            public boolean sendMediaButton(KeyEvent mediaButton) {
                throw new AssertionError();
            }

            public void registerCallbackListener(IMediaControllerCallback cb) {
                if (!MediaSessionImplApi21.this.mDestroyed) {
                    MediaSessionImplApi21.this.mExtraControllerCallbacks.register(cb);
                }
            }

            public void unregisterCallbackListener(IMediaControllerCallback cb) {
                MediaSessionImplApi21.this.mExtraControllerCallbacks.unregister(cb);
            }

            public String getPackageName() {
                throw new AssertionError();
            }

            public String getTag() {
                throw new AssertionError();
            }

            public PendingIntent getLaunchPendingIntent() {
                throw new AssertionError();
            }

            public long getFlags() {
                throw new AssertionError();
            }

            public ParcelableVolumeInfo getVolumeAttributes() {
                throw new AssertionError();
            }

            public void adjustVolume(int direction, int flags, String packageName) {
                throw new AssertionError();
            }

            public void setVolumeTo(int value, int flags, String packageName) {
                throw new AssertionError();
            }

            public void prepare() throws RemoteException {
                throw new AssertionError();
            }

            public void prepareFromMediaId(String mediaId, Bundle extras) throws RemoteException {
                throw new AssertionError();
            }

            public void prepareFromSearch(String query, Bundle extras) throws RemoteException {
                throw new AssertionError();
            }

            public void prepareFromUri(Uri uri, Bundle extras) throws RemoteException {
                throw new AssertionError();
            }

            public void play() throws RemoteException {
                throw new AssertionError();
            }

            public void playFromMediaId(String mediaId, Bundle extras) throws RemoteException {
                throw new AssertionError();
            }

            public void playFromSearch(String query, Bundle extras) throws RemoteException {
                throw new AssertionError();
            }

            public void playFromUri(Uri uri, Bundle extras) throws RemoteException {
                throw new AssertionError();
            }

            public void skipToQueueItem(long id) {
                throw new AssertionError();
            }

            public void pause() throws RemoteException {
                throw new AssertionError();
            }

            public void stop() throws RemoteException {
                throw new AssertionError();
            }

            public void next() throws RemoteException {
                throw new AssertionError();
            }

            public void previous() throws RemoteException {
                throw new AssertionError();
            }

            public void fastForward() throws RemoteException {
                throw new AssertionError();
            }

            public void rewind() throws RemoteException {
                throw new AssertionError();
            }

            public void seekTo(long pos) throws RemoteException {
                throw new AssertionError();
            }

            public void rate(RatingCompat rating) throws RemoteException {
                throw new AssertionError();
            }

            public void setCaptioningEnabled(boolean enabled) throws RemoteException {
                throw new AssertionError();
            }

            public void setRepeatMode(int repeatMode) throws RemoteException {
                throw new AssertionError();
            }

            public void setShuffleModeEnabledDeprecated(boolean enabled) throws RemoteException {
                throw new AssertionError();
            }

            public void setShuffleMode(int shuffleMode) throws RemoteException {
                throw new AssertionError();
            }

            public void sendCustomAction(String action, Bundle args) throws RemoteException {
                throw new AssertionError();
            }

            public MediaMetadataCompat getMetadata() {
                throw new AssertionError();
            }

            public PlaybackStateCompat getPlaybackState() {
                return MediaSessionCompat.getStateWithUpdatedPosition(MediaSessionImplApi21.this.mPlaybackState, MediaSessionImplApi21.this.mMetadata);
            }

            public List<QueueItem> getQueue() {
                return null;
            }

            public void addQueueItem(MediaDescriptionCompat descriptionCompat) {
                throw new AssertionError();
            }

            public void addQueueItemAt(MediaDescriptionCompat descriptionCompat, int index) {
                throw new AssertionError();
            }

            public void removeQueueItem(MediaDescriptionCompat description) {
                throw new AssertionError();
            }

            public void removeQueueItemAt(int index) {
                throw new AssertionError();
            }

            public CharSequence getQueueTitle() {
                throw new AssertionError();
            }

            public Bundle getExtras() {
                throw new AssertionError();
            }

            public int getRatingType() {
                return MediaSessionImplApi21.this.mRatingType;
            }

            public boolean isCaptioningEnabled() {
                return MediaSessionImplApi21.this.mCaptioningEnabled;
            }

            public int getRepeatMode() {
                return MediaSessionImplApi21.this.mRepeatMode;
            }

            public boolean isShuffleModeEnabledDeprecated() {
                return MediaSessionImplApi21.this.mShuffleModeEnabled;
            }

            public int getShuffleMode() {
                return MediaSessionImplApi21.this.mShuffleMode;
            }

            public boolean isTransportControlEnabled() {
                throw new AssertionError();
            }
        }

        public MediaSessionImplApi21(Context context, String tag) {
            this.mSessionObj = MediaSessionCompatApi21.createSession(context, tag);
            this.mToken = new Token(MediaSessionCompatApi21.getSessionToken(this.mSessionObj), new ExtraSession());
        }

        public MediaSessionImplApi21(Object mediaSession) {
            this.mSessionObj = MediaSessionCompatApi21.verifySession(mediaSession);
            this.mToken = new Token(MediaSessionCompatApi21.getSessionToken(this.mSessionObj), new ExtraSession());
        }

        public void setCallback(Callback callback, Handler handler) {
            Object obj = null;
            Object obj2 = this.mSessionObj;
            if (callback != null) {
                obj = callback.mCallbackObj;
            }
            MediaSessionCompatApi21.setCallback(obj2, obj, handler);
            if (callback != null) {
                callback.setSessionImpl(this, handler);
            }
        }

        public void setFlags(int flags) {
            MediaSessionCompatApi21.setFlags(this.mSessionObj, flags);
        }

        public void setPlaybackToLocal(int stream) {
            MediaSessionCompatApi21.setPlaybackToLocal(this.mSessionObj, stream);
        }

        public void setPlaybackToRemote(VolumeProviderCompat volumeProvider) {
            MediaSessionCompatApi21.setPlaybackToRemote(this.mSessionObj, volumeProvider.getVolumeProvider());
        }

        public void setActive(boolean active) {
            MediaSessionCompatApi21.setActive(this.mSessionObj, active);
        }

        public boolean isActive() {
            return MediaSessionCompatApi21.isActive(this.mSessionObj);
        }

        public void sendSessionEvent(String event, Bundle extras) {
            if (VERSION.SDK_INT < 23) {
                for (int i = this.mExtraControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                    try {
                        ((IMediaControllerCallback) this.mExtraControllerCallbacks.getBroadcastItem(i)).onEvent(event, extras);
                    } catch (RemoteException e) {
                    }
                }
                this.mExtraControllerCallbacks.finishBroadcast();
            }
            MediaSessionCompatApi21.sendSessionEvent(this.mSessionObj, event, extras);
        }

        public void release() {
            this.mDestroyed = true;
            MediaSessionCompatApi21.release(this.mSessionObj);
        }

        public Token getSessionToken() {
            return this.mToken;
        }

        public void setPlaybackState(PlaybackStateCompat state) {
            Object obj = null;
            this.mPlaybackState = state;
            for (int i = this.mExtraControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    ((IMediaControllerCallback) this.mExtraControllerCallbacks.getBroadcastItem(i)).onPlaybackStateChanged(state);
                } catch (RemoteException e) {
                }
            }
            this.mExtraControllerCallbacks.finishBroadcast();
            Object obj2 = this.mSessionObj;
            if (state != null) {
                obj = state.getPlaybackState();
            }
            MediaSessionCompatApi21.setPlaybackState(obj2, obj);
        }

        public PlaybackStateCompat getPlaybackState() {
            return this.mPlaybackState;
        }

        public void setMetadata(MediaMetadataCompat metadata) {
            Object obj = null;
            this.mMetadata = metadata;
            Object obj2 = this.mSessionObj;
            if (metadata != null) {
                obj = metadata.getMediaMetadata();
            }
            MediaSessionCompatApi21.setMetadata(obj2, obj);
        }

        public void setSessionActivity(PendingIntent pi) {
            MediaSessionCompatApi21.setSessionActivity(this.mSessionObj, pi);
        }

        public void setMediaButtonReceiver(PendingIntent mbr) {
            MediaSessionCompatApi21.setMediaButtonReceiver(this.mSessionObj, mbr);
        }

        public void setQueue(List<QueueItem> queue) {
            this.mQueue = queue;
            List queueObjs = null;
            if (queue != null) {
                queueObjs = new ArrayList();
                for (QueueItem item : queue) {
                    queueObjs.add(item.getQueueItem());
                }
            }
            MediaSessionCompatApi21.setQueue(this.mSessionObj, queueObjs);
        }

        public void setQueueTitle(CharSequence title) {
            MediaSessionCompatApi21.setQueueTitle(this.mSessionObj, title);
        }

        public void setRatingType(int type) {
            if (VERSION.SDK_INT < 22) {
                this.mRatingType = type;
            } else {
                MediaSessionCompatApi22.setRatingType(this.mSessionObj, type);
            }
        }

        public void setCaptioningEnabled(boolean enabled) {
            if (this.mCaptioningEnabled != enabled) {
                this.mCaptioningEnabled = enabled;
                for (int i = this.mExtraControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                    try {
                        ((IMediaControllerCallback) this.mExtraControllerCallbacks.getBroadcastItem(i)).onCaptioningEnabledChanged(enabled);
                    } catch (RemoteException e) {
                    }
                }
                this.mExtraControllerCallbacks.finishBroadcast();
            }
        }

        public void setRepeatMode(int repeatMode) {
            if (this.mRepeatMode != repeatMode) {
                this.mRepeatMode = repeatMode;
                for (int i = this.mExtraControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                    try {
                        ((IMediaControllerCallback) this.mExtraControllerCallbacks.getBroadcastItem(i)).onRepeatModeChanged(repeatMode);
                    } catch (RemoteException e) {
                    }
                }
                this.mExtraControllerCallbacks.finishBroadcast();
            }
        }

        public void setShuffleModeEnabled(boolean enabled) {
            if (this.mShuffleModeEnabled != enabled) {
                this.mShuffleModeEnabled = enabled;
                for (int i = this.mExtraControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                    try {
                        ((IMediaControllerCallback) this.mExtraControllerCallbacks.getBroadcastItem(i)).onShuffleModeChangedDeprecated(enabled);
                    } catch (RemoteException e) {
                    }
                }
                this.mExtraControllerCallbacks.finishBroadcast();
            }
        }

        public void setShuffleMode(int shuffleMode) {
            if (this.mShuffleMode != shuffleMode) {
                this.mShuffleMode = shuffleMode;
                for (int i = this.mExtraControllerCallbacks.beginBroadcast() - 1; i >= 0; i--) {
                    try {
                        ((IMediaControllerCallback) this.mExtraControllerCallbacks.getBroadcastItem(i)).onShuffleModeChanged(shuffleMode);
                    } catch (RemoteException e) {
                    }
                }
                this.mExtraControllerCallbacks.finishBroadcast();
            }
        }

        public void setExtras(Bundle extras) {
            MediaSessionCompatApi21.setExtras(this.mSessionObj, extras);
        }

        public Object getMediaSession() {
            return this.mSessionObj;
        }

        public Object getRemoteControlClient() {
            return null;
        }

        public String getCallingPackage() {
            if (VERSION.SDK_INT < 24) {
                return null;
            }
            return MediaSessionCompatApi24.getCallingPackage(this.mSessionObj);
        }
    }

    public interface OnActiveChangeListener {
        void onActiveChanged();
    }

    public static final class QueueItem implements Parcelable {
        public static final Creator<QueueItem> CREATOR = new Creator<QueueItem>() {
            public QueueItem createFromParcel(Parcel p) {
                return new QueueItem(p);
            }

            public QueueItem[] newArray(int size) {
                return new QueueItem[size];
            }
        };
        public static final int UNKNOWN_ID = -1;
        private final MediaDescriptionCompat mDescription;
        private final long mId;
        private Object mItem;

        public QueueItem(MediaDescriptionCompat description, long id) {
            this(null, description, id);
        }

        private QueueItem(Object queueItem, MediaDescriptionCompat description, long id) {
            if (description == null) {
                throw new IllegalArgumentException("Description cannot be null.");
            } else if (id == -1) {
                throw new IllegalArgumentException("Id cannot be QueueItem.UNKNOWN_ID");
            } else {
                this.mDescription = description;
                this.mId = id;
                this.mItem = queueItem;
            }
        }

        QueueItem(Parcel in) {
            this.mDescription = (MediaDescriptionCompat) MediaDescriptionCompat.CREATOR.createFromParcel(in);
            this.mId = in.readLong();
        }

        public MediaDescriptionCompat getDescription() {
            return this.mDescription;
        }

        public long getQueueId() {
            return this.mId;
        }

        public void writeToParcel(Parcel dest, int flags) {
            this.mDescription.writeToParcel(dest, flags);
            dest.writeLong(this.mId);
        }

        public int describeContents() {
            return 0;
        }

        public Object getQueueItem() {
            if (this.mItem != null || VERSION.SDK_INT < 21) {
                return this.mItem;
            }
            this.mItem = QueueItem.createItem(this.mDescription.getMediaDescription(), this.mId);
            return this.mItem;
        }

        public static QueueItem fromQueueItem(Object queueItem) {
            if (queueItem == null || VERSION.SDK_INT < 21) {
                return null;
            }
            return new QueueItem(queueItem, MediaDescriptionCompat.fromMediaDescription(QueueItem.getDescription(queueItem)), QueueItem.getQueueId(queueItem));
        }

        public static List<QueueItem> fromQueueItemList(List<?> itemList) {
            if (itemList == null || VERSION.SDK_INT < 21) {
                return null;
            }
            List<QueueItem> items = new ArrayList();
            for (Object itemObj : itemList) {
                items.add(fromQueueItem(itemObj));
            }
            return items;
        }

        public String toString() {
            return "MediaSession.QueueItem {Description=" + this.mDescription + ", Id=" + this.mId + " }";
        }
    }

    static final class ResultReceiverWrapper implements Parcelable {
        public static final Creator<ResultReceiverWrapper> CREATOR = new Creator<ResultReceiverWrapper>() {
            public ResultReceiverWrapper createFromParcel(Parcel p) {
                return new ResultReceiverWrapper(p);
            }

            public ResultReceiverWrapper[] newArray(int size) {
                return new ResultReceiverWrapper[size];
            }
        };
        private ResultReceiver mResultReceiver;

        public ResultReceiverWrapper(ResultReceiver resultReceiver) {
            this.mResultReceiver = resultReceiver;
        }

        ResultReceiverWrapper(Parcel in) {
            this.mResultReceiver = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(in);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            this.mResultReceiver.writeToParcel(dest, flags);
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SessionFlags {
    }

    public static final class Token implements Parcelable {
        public static final Creator<Token> CREATOR = new Creator<Token>() {
            public Token createFromParcel(Parcel in) {
                Object inner;
                if (VERSION.SDK_INT >= 21) {
                    inner = in.readParcelable(null);
                } else {
                    inner = in.readStrongBinder();
                }
                return new Token(inner);
            }

            public Token[] newArray(int size) {
                return new Token[size];
            }
        };
        private final IMediaSession mExtraBinder;
        private final Object mInner;

        Token(Object inner) {
            this(inner, null);
        }

        Token(Object inner, IMediaSession extraBinder) {
            this.mInner = inner;
            this.mExtraBinder = extraBinder;
        }

        public static Token fromToken(Object token) {
            return fromToken(token, null);
        }

        @RestrictTo({Scope.LIBRARY_GROUP})
        public static Token fromToken(Object token, IMediaSession extraBinder) {
            if (token == null || VERSION.SDK_INT < 21) {
                return null;
            }
            return new Token(MediaSessionCompatApi21.verifyToken(token), extraBinder);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            if (VERSION.SDK_INT >= 21) {
                dest.writeParcelable((Parcelable) this.mInner, flags);
            } else {
                dest.writeStrongBinder((IBinder) this.mInner);
            }
        }

        public int hashCode() {
            if (this.mInner == null) {
                return 0;
            }
            return this.mInner.hashCode();
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Token)) {
                return false;
            }
            Token other = (Token) obj;
            if (this.mInner == null) {
                if (other.mInner != null) {
                    z = false;
                }
                return z;
            } else if (other.mInner == null) {
                return false;
            } else {
                return this.mInner.equals(other.mInner);
            }
        }

        public Object getToken() {
            return this.mInner;
        }

        @RestrictTo({Scope.LIBRARY_GROUP})
        public IMediaSession getExtraBinder() {
            return this.mExtraBinder;
        }
    }

    public MediaSessionCompat(Context context, String tag) {
        this(context, tag, null, null);
    }

    public MediaSessionCompat(Context context, String tag, ComponentName mbrComponent, PendingIntent mbrIntent) {
        this.mActiveListeners = new ArrayList();
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        } else if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("tag must not be null or empty");
        } else {
            if (mbrComponent == null) {
                mbrComponent = MediaButtonReceiver.getMediaButtonReceiverComponent(context);
                if (mbrComponent == null) {
                    Log.w(TAG, "Couldn't find a unique registered media button receiver in the given context.");
                }
            }
            if (mbrComponent != null && mbrIntent == null) {
                Intent mediaButtonIntent = new Intent("android.intent.action.MEDIA_BUTTON");
                mediaButtonIntent.setComponent(mbrComponent);
                mbrIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, 0);
            }
            if (VERSION.SDK_INT >= 21) {
                this.mImpl = new MediaSessionImplApi21(context, tag);
                setCallback(new Callback() {
                });
                this.mImpl.setMediaButtonReceiver(mbrIntent);
            } else if (VERSION.SDK_INT >= 19) {
                this.mImpl = new MediaSessionImplApi19(context, tag, mbrComponent, mbrIntent);
            } else if (VERSION.SDK_INT >= 18) {
                this.mImpl = new MediaSessionImplApi18(context, tag, mbrComponent, mbrIntent);
            } else {
                this.mImpl = new MediaSessionImplBase(context, tag, mbrComponent, mbrIntent);
            }
            this.mController = new MediaControllerCompat(context, this);
            if (sMaxBitmapSize == 0) {
                sMaxBitmapSize = (int) TypedValue.applyDimension(1, 320.0f, context.getResources().getDisplayMetrics());
            }
        }
    }

    private MediaSessionCompat(Context context, MediaSessionImpl impl) {
        this.mActiveListeners = new ArrayList();
        this.mImpl = impl;
        if (VERSION.SDK_INT >= 21 && (MediaSessionCompatApi21.hasCallback(impl.getMediaSession()) ^ 1) != 0) {
            setCallback(new Callback() {
            });
        }
        this.mController = new MediaControllerCompat(context, this);
    }

    public void setCallback(Callback callback) {
        setCallback(callback, null);
    }

    public void setCallback(Callback callback, Handler handler) {
        MediaSessionImpl mediaSessionImpl = this.mImpl;
        if (handler == null) {
            handler = new Handler();
        }
        mediaSessionImpl.setCallback(callback, handler);
    }

    public void setSessionActivity(PendingIntent pi) {
        this.mImpl.setSessionActivity(pi);
    }

    public void setMediaButtonReceiver(PendingIntent mbr) {
        this.mImpl.setMediaButtonReceiver(mbr);
    }

    public void setFlags(int flags) {
        this.mImpl.setFlags(flags);
    }

    public void setPlaybackToLocal(int stream) {
        this.mImpl.setPlaybackToLocal(stream);
    }

    public void setPlaybackToRemote(VolumeProviderCompat volumeProvider) {
        if (volumeProvider == null) {
            throw new IllegalArgumentException("volumeProvider may not be null!");
        }
        this.mImpl.setPlaybackToRemote(volumeProvider);
    }

    public void setActive(boolean active) {
        this.mImpl.setActive(active);
        for (OnActiveChangeListener listener : this.mActiveListeners) {
            listener.onActiveChanged();
        }
    }

    public boolean isActive() {
        return this.mImpl.isActive();
    }

    public void sendSessionEvent(String event, Bundle extras) {
        if (TextUtils.isEmpty(event)) {
            throw new IllegalArgumentException("event cannot be null or empty");
        }
        this.mImpl.sendSessionEvent(event, extras);
    }

    public void release() {
        this.mImpl.release();
    }

    public Token getSessionToken() {
        return this.mImpl.getSessionToken();
    }

    public MediaControllerCompat getController() {
        return this.mController;
    }

    public void setPlaybackState(PlaybackStateCompat state) {
        this.mImpl.setPlaybackState(state);
    }

    public void setMetadata(MediaMetadataCompat metadata) {
        this.mImpl.setMetadata(metadata);
    }

    public void setQueue(List<QueueItem> queue) {
        this.mImpl.setQueue(queue);
    }

    public void setQueueTitle(CharSequence title) {
        this.mImpl.setQueueTitle(title);
    }

    public void setRatingType(int type) {
        this.mImpl.setRatingType(type);
    }

    public void setCaptioningEnabled(boolean enabled) {
        this.mImpl.setCaptioningEnabled(enabled);
    }

    public void setRepeatMode(int repeatMode) {
        this.mImpl.setRepeatMode(repeatMode);
    }

    @Deprecated
    public void setShuffleModeEnabled(boolean enabled) {
        this.mImpl.setShuffleModeEnabled(enabled);
    }

    public void setShuffleMode(int shuffleMode) {
        this.mImpl.setShuffleMode(shuffleMode);
    }

    public void setExtras(Bundle extras) {
        this.mImpl.setExtras(extras);
    }

    public Object getMediaSession() {
        return this.mImpl.getMediaSession();
    }

    public Object getRemoteControlClient() {
        return this.mImpl.getRemoteControlClient();
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public String getCallingPackage() {
        return this.mImpl.getCallingPackage();
    }

    public void addOnActiveChangeListener(OnActiveChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener may not be null");
        }
        this.mActiveListeners.add(listener);
    }

    public void removeOnActiveChangeListener(OnActiveChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener may not be null");
        }
        this.mActiveListeners.remove(listener);
    }

    public static MediaSessionCompat fromMediaSession(Context context, Object mediaSession) {
        if (context == null || mediaSession == null || VERSION.SDK_INT < 21) {
            return null;
        }
        return new MediaSessionCompat(context, new MediaSessionImplApi21(mediaSession));
    }

    private static PlaybackStateCompat getStateWithUpdatedPosition(PlaybackStateCompat state, MediaMetadataCompat metadata) {
        if (state == null || state.getPosition() == -1) {
            return state;
        }
        if (state.getState() == 3 || state.getState() == 4 || state.getState() == 5) {
            long updateTime = state.getLastPositionUpdateTime();
            if (updateTime > 0) {
                long currentTime = SystemClock.elapsedRealtime();
                long position = ((long) (state.getPlaybackSpeed() * ((float) (currentTime - updateTime)))) + state.getPosition();
                long duration = -1;
                if (metadata != null && metadata.containsKey(MediaMetadataCompat.METADATA_KEY_DURATION)) {
                    duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                }
                if (duration >= 0 && position > duration) {
                    position = duration;
                } else if (position < 0) {
                    position = 0;
                }
                return new PlaybackStateCompat.Builder(state).setState(state.getState(), position, state.getPlaybackSpeed(), currentTime).build();
            }
        }
        return state;
    }
}
