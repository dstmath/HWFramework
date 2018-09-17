package android.media.session;

import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.media.AudioAttributes;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaMetadata.Builder;
import android.media.Rating;
import android.media.VolumeProvider;
import android.media.session.ISessionCallback.Stub;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.List;

public final class MediaSession {
    public static final int FLAG_EXCLUSIVE_GLOBAL_PRIORITY = 65536;
    @Deprecated
    public static final int FLAG_HANDLES_MEDIA_BUTTONS = 1;
    @Deprecated
    public static final int FLAG_HANDLES_TRANSPORT_CONTROLS = 2;
    private static final String TAG = "MediaSession";
    private boolean mActive;
    private final ISession mBinder;
    private CallbackMessageHandler mCallback;
    private final CallbackStub mCbStub;
    private Context mContext;
    private final MediaController mController;
    private final Object mLock;
    private final int mMaxBitmapSize;
    private PlaybackState mPlaybackState;
    private final Token mSessionToken;
    private VolumeProvider mVolumeProvider;

    public static abstract class Callback {
        private MediaSession mSession;

        public void onCommand(String command, Bundle args, ResultReceiver cb) {
        }

        public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
            if (this.mSession != null && Intent.ACTION_MEDIA_BUTTON.equals(mediaButtonIntent.getAction())) {
                KeyEvent ke = (KeyEvent) mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (ke != null && ke.getAction() == 0) {
                    PlaybackState state = this.mSession.mPlaybackState;
                    long validActions = state == null ? 0 : state.getActions();
                    switch (ke.getKeyCode()) {
                        case 79:
                        case 85:
                            boolean isPlaying = state == null ? false : state.getState() == 3;
                            boolean canPlay = (516 & validActions) != 0;
                            boolean canPause = (514 & validActions) != 0;
                            if (isPlaying && canPause) {
                                onPause();
                                return true;
                            } else if (!isPlaying && canPlay) {
                                onPlay();
                                return true;
                            }
                            break;
                        case 86:
                            if ((1 & validActions) != 0) {
                                onStop();
                                return true;
                            }
                            break;
                        case 87:
                            if ((32 & validActions) != 0) {
                                onSkipToNext();
                                return true;
                            }
                            break;
                        case 88:
                            if ((16 & validActions) != 0) {
                                onSkipToPrevious();
                                return true;
                            }
                            break;
                        case 89:
                            if ((8 & validActions) != 0) {
                                onRewind();
                                return true;
                            }
                            break;
                        case 90:
                            if ((64 & validActions) != 0) {
                                onFastForward();
                                return true;
                            }
                            break;
                        case 126:
                            if ((4 & validActions) != 0) {
                                onPlay();
                                return true;
                            }
                            break;
                        case 127:
                            if ((2 & validActions) != 0) {
                                onPause();
                                return true;
                            }
                            break;
                    }
                }
            }
            return false;
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

        public void onPlayFromSearch(String query, Bundle extras) {
        }

        public void onPlayFromMediaId(String mediaId, Bundle extras) {
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

        public void onSetRating(Rating rating) {
        }

        public void onCustomAction(String action, Bundle extras) {
        }
    }

    private class CallbackMessageHandler extends Handler {
        private static final int MSG_ADJUST_VOLUME = 21;
        private static final int MSG_COMMAND = 1;
        private static final int MSG_CUSTOM_ACTION = 20;
        private static final int MSG_FAST_FORWARD = 16;
        private static final int MSG_MEDIA_BUTTON = 2;
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
        private static final int MSG_REWIND = 17;
        private static final int MSG_SEEK_TO = 18;
        private static final int MSG_SET_VOLUME = 22;
        private static final int MSG_SKIP_TO_ITEM = 11;
        private static final int MSG_STOP = 13;
        private Callback mCallback;

        public CallbackMessageHandler(Looper looper, Callback callback) {
            super(looper, null, true);
            this.mCallback = callback;
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
            VolumeProvider vp;
            switch (msg.what) {
                case 1:
                    Command cmd = msg.obj;
                    this.mCallback.onCommand(cmd.command, cmd.extras, cmd.stub);
                    return;
                case 2:
                    this.mCallback.onMediaButtonEvent((Intent) msg.obj);
                    return;
                case 3:
                    this.mCallback.onPrepare();
                    return;
                case 4:
                    this.mCallback.onPrepareFromMediaId((String) msg.obj, msg.getData());
                    return;
                case 5:
                    this.mCallback.onPrepareFromSearch((String) msg.obj, msg.getData());
                    return;
                case 6:
                    this.mCallback.onPrepareFromUri((Uri) msg.obj, msg.getData());
                    return;
                case 7:
                    this.mCallback.onPlay();
                    return;
                case 8:
                    this.mCallback.onPlayFromMediaId((String) msg.obj, msg.getData());
                    return;
                case 9:
                    this.mCallback.onPlayFromSearch((String) msg.obj, msg.getData());
                    return;
                case 10:
                    this.mCallback.onPlayFromUri((Uri) msg.obj, msg.getData());
                    return;
                case 11:
                    this.mCallback.onSkipToQueueItem(((Long) msg.obj).longValue());
                    return;
                case 12:
                    this.mCallback.onPause();
                    return;
                case 13:
                    this.mCallback.onStop();
                    return;
                case 14:
                    this.mCallback.onSkipToNext();
                    return;
                case 15:
                    this.mCallback.onSkipToPrevious();
                    return;
                case 16:
                    this.mCallback.onFastForward();
                    return;
                case 17:
                    this.mCallback.onRewind();
                    return;
                case 18:
                    this.mCallback.onSeekTo(((Long) msg.obj).longValue());
                    return;
                case 19:
                    this.mCallback.onSetRating((Rating) msg.obj);
                    return;
                case 20:
                    this.mCallback.onCustomAction((String) msg.obj, msg.getData());
                    return;
                case 21:
                    synchronized (MediaSession.this.mLock) {
                        vp = MediaSession.this.mVolumeProvider;
                    }
                    if (vp != null) {
                        vp.onAdjustVolume(((Integer) msg.obj).intValue());
                        return;
                    }
                    return;
                case 22:
                    synchronized (MediaSession.this.mLock) {
                        vp = MediaSession.this.mVolumeProvider;
                    }
                    if (vp != null) {
                        vp.onSetVolumeTo(((Integer) msg.obj).intValue());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public static class CallbackStub extends Stub {
        private WeakReference<MediaSession> mMediaSession;

        public CallbackStub(MediaSession session) {
            this.mMediaSession = new WeakReference(session);
        }

        public void onCommand(String command, Bundle args, ResultReceiver cb) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.postCommand(command, args, cb);
            }
        }

        public void onMediaButton(Intent mediaButtonIntent, int sequenceNumber, ResultReceiver cb) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                try {
                    session.dispatchMediaButton(mediaButtonIntent);
                } catch (Throwable th) {
                    if (cb != null) {
                        cb.send(sequenceNumber, null);
                    }
                }
            }
            if (cb != null) {
                cb.send(sequenceNumber, null);
            }
        }

        public void onPrepare() {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchPrepare();
            }
        }

        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchPrepareFromMediaId(mediaId, extras);
            }
        }

        public void onPrepareFromSearch(String query, Bundle extras) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchPrepareFromSearch(query, extras);
            }
        }

        public void onPrepareFromUri(Uri uri, Bundle extras) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchPrepareFromUri(uri, extras);
            }
        }

        public void onPlay() {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchPlay();
            }
        }

        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchPlayFromMediaId(mediaId, extras);
            }
        }

        public void onPlayFromSearch(String query, Bundle extras) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchPlayFromSearch(query, extras);
            }
        }

        public void onPlayFromUri(Uri uri, Bundle extras) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchPlayFromUri(uri, extras);
            }
        }

        public void onSkipToTrack(long id) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchSkipToItem(id);
            }
        }

        public void onPause() {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchPause();
            }
        }

        public void onStop() {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchStop();
            }
        }

        public void onNext() {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchNext();
            }
        }

        public void onPrevious() {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchPrevious();
            }
        }

        public void onFastForward() {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchFastForward();
            }
        }

        public void onRewind() {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchRewind();
            }
        }

        public void onSeekTo(long pos) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchSeekTo(pos);
            }
        }

        public void onRate(Rating rating) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchRate(rating);
            }
        }

        public void onCustomAction(String action, Bundle args) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchCustomAction(action, args);
            }
        }

        public void onAdjustVolume(int direction) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchAdjustVolume(direction);
            }
        }

        public void onSetVolumeTo(int value) {
            MediaSession session = (MediaSession) this.mMediaSession.get();
            if (session != null) {
                session.dispatchSetVolumeTo(value);
            }
        }
    }

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

    public static final class QueueItem implements Parcelable {
        public static final Creator<QueueItem> CREATOR = new Creator<QueueItem>() {
            public QueueItem createFromParcel(Parcel p) {
                return new QueueItem(p, null);
            }

            public QueueItem[] newArray(int size) {
                return new QueueItem[size];
            }
        };
        public static final int UNKNOWN_ID = -1;
        private final MediaDescription mDescription;
        private final long mId;

        public QueueItem(MediaDescription description, long id) {
            if (description == null) {
                throw new IllegalArgumentException("Description cannot be null.");
            } else if (id == -1) {
                throw new IllegalArgumentException("Id cannot be QueueItem.UNKNOWN_ID");
            } else {
                this.mDescription = description;
                this.mId = id;
            }
        }

        private QueueItem(Parcel in) {
            this.mDescription = (MediaDescription) MediaDescription.CREATOR.createFromParcel(in);
            this.mId = in.readLong();
        }

        public MediaDescription getDescription() {
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

        public String toString() {
            return "MediaSession.QueueItem {Description=" + this.mDescription + ", Id=" + this.mId + " }";
        }
    }

    public static final class Token implements Parcelable {
        public static final Creator<Token> CREATOR = new Creator<Token>() {
            public Token createFromParcel(Parcel in) {
                return new Token(ISessionController.Stub.asInterface(in.readStrongBinder()));
            }

            public Token[] newArray(int size) {
                return new Token[size];
            }
        };
        private ISessionController mBinder;

        public Token(ISessionController binder) {
            this.mBinder = binder;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStrongBinder(this.mBinder.asBinder());
        }

        public int hashCode() {
            return (this.mBinder == null ? 0 : this.mBinder.asBinder().hashCode()) + 31;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Token other = (Token) obj;
            if (this.mBinder == null) {
                if (other.mBinder != null) {
                    return false;
                }
            } else if (!this.mBinder.asBinder().equals(other.mBinder.asBinder())) {
                return false;
            }
            return true;
        }

        ISessionController getBinder() {
            return this.mBinder;
        }
    }

    public MediaSession(Context context, String tag) {
        this(context, tag, UserHandle.myUserId());
    }

    public MediaSession(Context context, String tag, int userId) {
        this.mLock = new Object();
        this.mActive = false;
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null.");
        }
        this.mContext = context;
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("tag cannot be null or empty");
        }
        this.mMaxBitmapSize = context.getResources().getDimensionPixelSize(17104948);
        this.mCbStub = new CallbackStub(this);
        try {
            this.mBinder = ((MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE)).createSession(this.mCbStub, tag, userId);
            this.mSessionToken = new Token(this.mBinder.getController());
            this.mController = new MediaController(context, this.mSessionToken);
        } catch (RemoteException e) {
            throw new RuntimeException("Remote error creating session.", e);
        }
    }

    public void setCallback(Callback callback) {
        setCallback(callback, null);
    }

    public void setCallback(Callback callback, Handler handler) {
        synchronized (this.mLock) {
            if (callback == null) {
                if (this.mCallback != null) {
                    this.mCallback.mCallback.mSession = null;
                }
                this.mCallback = null;
                return;
            }
            if (this.mCallback != null) {
                this.mCallback.mCallback.mSession = null;
            }
            if (handler == null) {
                handler = new Handler();
            }
            callback.mSession = this;
            this.mCallback = new CallbackMessageHandler(handler.getLooper(), callback);
        }
    }

    public void setSessionActivity(PendingIntent pi) {
        try {
            this.mBinder.setLaunchPendingIntent(pi);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failure in setLaunchPendingIntent.", e);
        }
    }

    public void setMediaButtonReceiver(PendingIntent mbr) {
        try {
            this.mBinder.setMediaButtonReceiver(mbr);
            HwFrameworkFactory.getHwMediaSession().updateTargetInService(mbr, this.mContext);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failure in setMediaButtonReceiver.", e);
        }
    }

    public void setFlags(int flags) {
        try {
            this.mBinder.setFlags(flags);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failure in setFlags.", e);
        }
    }

    public void setPlaybackToLocal(AudioAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("Attributes cannot be null for local playback.");
        }
        try {
            this.mBinder.setPlaybackToLocal(attributes);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failure in setPlaybackToLocal.", e);
        }
    }

    public void setPlaybackToRemote(VolumeProvider volumeProvider) {
        if (volumeProvider == null) {
            throw new IllegalArgumentException("volumeProvider may not be null!");
        }
        synchronized (this.mLock) {
            this.mVolumeProvider = volumeProvider;
        }
        volumeProvider.setCallback(new android.media.VolumeProvider.Callback() {
            public void onVolumeChanged(VolumeProvider volumeProvider) {
                MediaSession.this.notifyRemoteVolumeChanged(volumeProvider);
            }
        });
        try {
            this.mBinder.setPlaybackToRemote(volumeProvider.getVolumeControl(), volumeProvider.getMaxVolume());
            this.mBinder.setCurrentVolume(volumeProvider.getCurrentVolume());
        } catch (RemoteException e) {
            Log.wtf(TAG, "Failure in setPlaybackToRemote.", e);
        }
    }

    public void setActive(boolean active) {
        if (this.mActive != active) {
            try {
                this.mBinder.setActive(active);
                this.mActive = active;
            } catch (RemoteException e) {
                Log.wtf(TAG, "Failure in setActive.", e);
            }
        }
    }

    public boolean isActive() {
        return this.mActive;
    }

    public void sendSessionEvent(String event, Bundle extras) {
        if (TextUtils.isEmpty(event)) {
            throw new IllegalArgumentException("event cannot be null or empty");
        }
        try {
            this.mBinder.sendEvent(event, extras);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error sending event", e);
        }
    }

    public void release() {
        try {
            this.mBinder.destroy();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error releasing session: ", e);
        }
    }

    public Token getSessionToken() {
        return this.mSessionToken;
    }

    public MediaController getController() {
        return this.mController;
    }

    public void setPlaybackState(PlaybackState state) {
        this.mPlaybackState = state;
        try {
            this.mBinder.setPlaybackState(state);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Dead object in setPlaybackState.", e);
        }
    }

    public void setMetadata(MediaMetadata metadata) {
        if (metadata != null) {
            metadata = new Builder(metadata, this.mMaxBitmapSize).build();
        }
        try {
            this.mBinder.setMetadata(metadata);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Dead object in setPlaybackState.", e);
        }
    }

    public void setQueue(List<QueueItem> queue) {
        ParceledListSlice parceledListSlice = null;
        try {
            ISession iSession = this.mBinder;
            if (queue != null) {
                parceledListSlice = new ParceledListSlice(queue);
            }
            iSession.setQueue(parceledListSlice);
        } catch (RemoteException e) {
            Log.wtf("Dead object in setQueue.", e);
        }
    }

    public void setQueueTitle(CharSequence title) {
        try {
            this.mBinder.setQueueTitle(title);
        } catch (RemoteException e) {
            Log.wtf("Dead object in setQueueTitle.", e);
        }
    }

    public void setRatingType(int type) {
        try {
            this.mBinder.setRatingType(type);
        } catch (RemoteException e) {
            Log.e(TAG, "Error in setRatingType.", e);
        }
    }

    public void setExtras(Bundle extras) {
        try {
            this.mBinder.setExtras(extras);
        } catch (RemoteException e) {
            Log.wtf("Dead object in setExtras.", e);
        }
    }

    /* JADX WARNING: Missing block: B:11:?, code:
            r4.mBinder.setCurrentVolume(r5.getCurrentVolume());
     */
    /* JADX WARNING: Missing block: B:16:0x0022, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:17:0x0023, code:
            android.util.Log.e(TAG, "Error in notifyVolumeChanged", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyRemoteVolumeChanged(VolumeProvider provider) {
        synchronized (this.mLock) {
            if (provider != null) {
                if (provider == this.mVolumeProvider) {
                }
            }
            Log.w(TAG, "Received update from stale volume provider");
        }
    }

    public String getCallingPackage() {
        try {
            return this.mBinder.getCallingPackage();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Dead object in getCallingPackage.", e);
            return null;
        }
    }

    private void dispatchPrepare() {
        postToCallback(3);
    }

    private void dispatchPrepareFromMediaId(String mediaId, Bundle extras) {
        postToCallback(4, mediaId, extras);
    }

    private void dispatchPrepareFromSearch(String query, Bundle extras) {
        postToCallback(5, query, extras);
    }

    private void dispatchPrepareFromUri(Uri uri, Bundle extras) {
        postToCallback(6, uri, extras);
    }

    private void dispatchPlay() {
        postToCallback(7);
    }

    private void dispatchPlayFromMediaId(String mediaId, Bundle extras) {
        postToCallback(8, mediaId, extras);
    }

    private void dispatchPlayFromSearch(String query, Bundle extras) {
        postToCallback(9, query, extras);
    }

    private void dispatchPlayFromUri(Uri uri, Bundle extras) {
        postToCallback(10, uri, extras);
    }

    private void dispatchSkipToItem(long id) {
        postToCallback(11, Long.valueOf(id));
    }

    private void dispatchPause() {
        postToCallback(12);
    }

    private void dispatchStop() {
        postToCallback(13);
    }

    private void dispatchNext() {
        postToCallback(14);
    }

    private void dispatchPrevious() {
        postToCallback(15);
    }

    private void dispatchFastForward() {
        postToCallback(16);
    }

    private void dispatchRewind() {
        postToCallback(17);
    }

    private void dispatchSeekTo(long pos) {
        postToCallback(18, Long.valueOf(pos));
    }

    private void dispatchRate(Rating rating) {
        postToCallback(19, rating);
    }

    private void dispatchCustomAction(String action, Bundle args) {
        postToCallback(20, action, args);
    }

    private void dispatchMediaButton(Intent mediaButtonIntent) {
        postToCallback(2, mediaButtonIntent);
    }

    private void dispatchAdjustVolume(int direction) {
        postToCallback(21, Integer.valueOf(direction));
    }

    private void dispatchSetVolumeTo(int volume) {
        postToCallback(22, Integer.valueOf(volume));
    }

    private void postToCallback(int what) {
        postToCallback(what, null);
    }

    private void postCommand(String command, Bundle args, ResultReceiver resultCb) {
        postToCallback(1, new Command(command, args, resultCb));
    }

    private void postToCallback(int what, Object obj) {
        postToCallback(what, obj, null);
    }

    private void postToCallback(int what, Object obj, Bundle extras) {
        synchronized (this.mLock) {
            if (this.mCallback != null) {
                this.mCallback.post(what, obj, extras);
            }
        }
    }

    public static boolean isActiveState(int state) {
        switch (state) {
            case 3:
            case 4:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
                return true;
            default:
                return false;
        }
    }
}
