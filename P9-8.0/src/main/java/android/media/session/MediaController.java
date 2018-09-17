package android.media.session;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.Rating;
import android.media.session.ISessionControllerCallback.Stub;
import android.media.session.MediaSession.QueueItem;
import android.media.session.MediaSession.Token;
import android.media.session.PlaybackState.CustomAction;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class MediaController {
    private static final int MSG_DESTROYED = 8;
    private static final int MSG_EVENT = 1;
    private static final int MSG_UPDATE_EXTRAS = 7;
    private static final int MSG_UPDATE_METADATA = 3;
    private static final int MSG_UPDATE_PLAYBACK_STATE = 2;
    private static final int MSG_UPDATE_QUEUE = 5;
    private static final int MSG_UPDATE_QUEUE_TITLE = 6;
    private static final int MSG_UPDATE_VOLUME = 4;
    private static final String TAG = "MediaController";
    private final ArrayList<MessageHandler> mCallbacks;
    private boolean mCbRegistered;
    private final CallbackStub mCbStub;
    private final Context mContext;
    private final Object mLock;
    private String mPackageName;
    private final ISessionController mSessionBinder;
    private String mTag;
    private final Token mToken;
    private final TransportControls mTransportControls;

    public static abstract class Callback {
        public void onSessionDestroyed() {
        }

        public void onSessionEvent(String event, Bundle extras) {
        }

        public void onPlaybackStateChanged(PlaybackState state) {
        }

        public void onMetadataChanged(MediaMetadata metadata) {
        }

        public void onQueueChanged(List<QueueItem> list) {
        }

        public void onQueueTitleChanged(CharSequence title) {
        }

        public void onExtrasChanged(Bundle extras) {
        }

        public void onAudioInfoChanged(PlaybackInfo info) {
        }
    }

    private static final class CallbackStub extends Stub {
        private final WeakReference<MediaController> mController;

        public CallbackStub(MediaController controller) {
            this.mController = new WeakReference(controller);
        }

        public void onSessionDestroyed() {
            MediaController controller = (MediaController) this.mController.get();
            if (controller != null) {
                controller.postMessage(8, null, null);
            }
        }

        public void onEvent(String event, Bundle extras) {
            MediaController controller = (MediaController) this.mController.get();
            if (controller != null) {
                controller.postMessage(1, event, extras);
            }
        }

        public void onPlaybackStateChanged(PlaybackState state) {
            MediaController controller = (MediaController) this.mController.get();
            if (controller != null) {
                controller.postMessage(2, state, null);
            }
        }

        public void onMetadataChanged(MediaMetadata metadata) {
            MediaController controller = (MediaController) this.mController.get();
            if (controller != null) {
                controller.postMessage(3, metadata, null);
            }
        }

        public void onQueueChanged(ParceledListSlice parceledQueue) {
            Object queue = parceledQueue == null ? null : parceledQueue.getList();
            MediaController controller = (MediaController) this.mController.get();
            if (controller != null) {
                controller.postMessage(5, queue, null);
            }
        }

        public void onQueueTitleChanged(CharSequence title) {
            MediaController controller = (MediaController) this.mController.get();
            if (controller != null) {
                controller.postMessage(6, title, null);
            }
        }

        public void onExtrasChanged(Bundle extras) {
            MediaController controller = (MediaController) this.mController.get();
            if (controller != null) {
                controller.postMessage(7, extras, null);
            }
        }

        public void onVolumeInfoChanged(ParcelableVolumeInfo pvi) {
            MediaController controller = (MediaController) this.mController.get();
            if (controller != null) {
                controller.postMessage(4, new PlaybackInfo(pvi.volumeType, pvi.audioAttrs, pvi.controlType, pvi.maxVolume, pvi.currentVolume), null);
            }
        }
    }

    private static final class MessageHandler extends Handler {
        private final Callback mCallback;
        private boolean mRegistered = false;

        public MessageHandler(Looper looper, Callback cb) {
            super(looper, null, true);
            this.mCallback = cb;
        }

        public void handleMessage(Message msg) {
            if (this.mRegistered) {
                switch (msg.what) {
                    case 1:
                        this.mCallback.onSessionEvent((String) msg.obj, msg.getData());
                        break;
                    case 2:
                        this.mCallback.onPlaybackStateChanged((PlaybackState) msg.obj);
                        break;
                    case 3:
                        this.mCallback.onMetadataChanged((MediaMetadata) msg.obj);
                        break;
                    case 4:
                        this.mCallback.onAudioInfoChanged((PlaybackInfo) msg.obj);
                        break;
                    case 5:
                        this.mCallback.onQueueChanged((List) msg.obj);
                        break;
                    case 6:
                        this.mCallback.onQueueTitleChanged((CharSequence) msg.obj);
                        break;
                    case 7:
                        this.mCallback.onExtrasChanged((Bundle) msg.obj);
                        break;
                    case 8:
                        this.mCallback.onSessionDestroyed();
                        break;
                }
            }
        }

        public void post(int what, Object obj, Bundle data) {
            Message msg = obtainMessage(what, obj);
            msg.setData(data);
            msg.sendToTarget();
        }
    }

    public static final class PlaybackInfo {
        public static final int PLAYBACK_TYPE_LOCAL = 1;
        public static final int PLAYBACK_TYPE_REMOTE = 2;
        private final AudioAttributes mAudioAttrs;
        private final int mCurrentVolume;
        private final int mMaxVolume;
        private final int mVolumeControl;
        private final int mVolumeType;

        public PlaybackInfo(int type, AudioAttributes attrs, int control, int max, int current) {
            this.mVolumeType = type;
            this.mAudioAttrs = attrs;
            this.mVolumeControl = control;
            this.mMaxVolume = max;
            this.mCurrentVolume = current;
        }

        public int getPlaybackType() {
            return this.mVolumeType;
        }

        public AudioAttributes getAudioAttributes() {
            return this.mAudioAttrs;
        }

        public int getVolumeControl() {
            return this.mVolumeControl;
        }

        public int getMaxVolume() {
            return this.mMaxVolume;
        }

        public int getCurrentVolume() {
            return this.mCurrentVolume;
        }
    }

    public final class TransportControls {
        private static final String TAG = "TransportController";

        /* synthetic */ TransportControls(MediaController this$0, TransportControls -this1) {
            this();
        }

        private TransportControls() {
        }

        public void prepare() {
            try {
                MediaController.this.mSessionBinder.prepare();
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling prepare.", e);
            }
        }

        public void prepareFromMediaId(String mediaId, Bundle extras) {
            if (TextUtils.isEmpty(mediaId)) {
                throw new IllegalArgumentException("You must specify a non-empty String for prepareFromMediaId.");
            }
            try {
                MediaController.this.mSessionBinder.prepareFromMediaId(mediaId, extras);
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling prepare(" + mediaId + ").", e);
            }
        }

        public void prepareFromSearch(String query, Bundle extras) {
            if (query == null) {
                query = ProxyInfo.LOCAL_EXCL_LIST;
            }
            try {
                MediaController.this.mSessionBinder.prepareFromSearch(query, extras);
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling prepare(" + query + ").", e);
            }
        }

        public void prepareFromUri(Uri uri, Bundle extras) {
            if (uri == null || Uri.EMPTY.equals(uri)) {
                throw new IllegalArgumentException("You must specify a non-empty Uri for prepareFromUri.");
            }
            try {
                MediaController.this.mSessionBinder.prepareFromUri(uri, extras);
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling prepare(" + uri + ").", e);
            }
        }

        public void play() {
            try {
                MediaController.this.mSessionBinder.play();
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling play.", e);
            }
        }

        public void playFromMediaId(String mediaId, Bundle extras) {
            if (TextUtils.isEmpty(mediaId)) {
                throw new IllegalArgumentException("You must specify a non-empty String for playFromMediaId.");
            }
            try {
                MediaController.this.mSessionBinder.playFromMediaId(mediaId, extras);
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling play(" + mediaId + ").", e);
            }
        }

        public void playFromSearch(String query, Bundle extras) {
            if (query == null) {
                query = ProxyInfo.LOCAL_EXCL_LIST;
            }
            try {
                MediaController.this.mSessionBinder.playFromSearch(query, extras);
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling play(" + query + ").", e);
            }
        }

        public void playFromUri(Uri uri, Bundle extras) {
            if (uri == null || Uri.EMPTY.equals(uri)) {
                throw new IllegalArgumentException("You must specify a non-empty Uri for playFromUri.");
            }
            try {
                MediaController.this.mSessionBinder.playFromUri(uri, extras);
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling play(" + uri + ").", e);
            }
        }

        public void skipToQueueItem(long id) {
            try {
                MediaController.this.mSessionBinder.skipToQueueItem(id);
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling skipToItem(" + id + ").", e);
            }
        }

        public void pause() {
            try {
                MediaController.this.mSessionBinder.pause();
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling pause.", e);
            }
        }

        public void stop() {
            try {
                MediaController.this.mSessionBinder.stop();
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling stop.", e);
            }
        }

        public void seekTo(long pos) {
            try {
                MediaController.this.mSessionBinder.seekTo(pos);
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling seekTo.", e);
            }
        }

        public void fastForward() {
            try {
                MediaController.this.mSessionBinder.fastForward();
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling fastForward.", e);
            }
        }

        public void skipToNext() {
            try {
                MediaController.this.mSessionBinder.next();
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling next.", e);
            }
        }

        public void rewind() {
            try {
                MediaController.this.mSessionBinder.rewind();
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling rewind.", e);
            }
        }

        public void skipToPrevious() {
            try {
                MediaController.this.mSessionBinder.previous();
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling previous.", e);
            }
        }

        public void setRating(Rating rating) {
            try {
                MediaController.this.mSessionBinder.rate(rating);
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error calling rate.", e);
            }
        }

        public void sendCustomAction(CustomAction customAction, Bundle args) {
            if (customAction == null) {
                throw new IllegalArgumentException("CustomAction cannot be null.");
            }
            sendCustomAction(customAction.getAction(), args);
        }

        public void sendCustomAction(String action, Bundle args) {
            if (TextUtils.isEmpty(action)) {
                throw new IllegalArgumentException("CustomAction cannot be null.");
            }
            try {
                MediaController.this.mSessionBinder.sendCustomAction(action, args);
            } catch (RemoteException e) {
                Log.d(TAG, "Dead object in sendCustomAction.", e);
            }
        }
    }

    public MediaController(Context context, ISessionController sessionBinder) {
        this.mCbStub = new CallbackStub(this);
        this.mCallbacks = new ArrayList();
        this.mLock = new Object();
        this.mCbRegistered = false;
        if (sessionBinder == null) {
            throw new IllegalArgumentException("Session token cannot be null");
        } else if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        } else {
            this.mSessionBinder = sessionBinder;
            this.mTransportControls = new TransportControls(this, null);
            this.mToken = new Token(sessionBinder);
            this.mContext = context;
        }
    }

    public MediaController(Context context, Token token) {
        this(context, token.getBinder());
    }

    public TransportControls getTransportControls() {
        return this.mTransportControls;
    }

    public boolean dispatchMediaButtonEvent(KeyEvent keyEvent) {
        if (keyEvent == null) {
            throw new IllegalArgumentException("KeyEvent may not be null");
        } else if (!KeyEvent.isMediaKey(keyEvent.getKeyCode())) {
            return false;
        } else {
            try {
                return this.mSessionBinder.sendMediaButton(keyEvent);
            } catch (RemoteException e) {
                return false;
            }
        }
    }

    public PlaybackState getPlaybackState() {
        try {
            return this.mSessionBinder.getPlaybackState();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling getPlaybackState.", e);
            return null;
        }
    }

    public MediaMetadata getMetadata() {
        try {
            return this.mSessionBinder.getMetadata();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling getMetadata.", e);
            return null;
        }
    }

    public List<QueueItem> getQueue() {
        try {
            ParceledListSlice queue = this.mSessionBinder.getQueue();
            if (queue != null) {
                return queue.getList();
            }
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling getQueue.", e);
        }
        return null;
    }

    public CharSequence getQueueTitle() {
        try {
            return this.mSessionBinder.getQueueTitle();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling getQueueTitle", e);
            return null;
        }
    }

    public Bundle getExtras() {
        try {
            return this.mSessionBinder.getExtras();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling getExtras", e);
            return null;
        }
    }

    public int getRatingType() {
        try {
            return this.mSessionBinder.getRatingType();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling getRatingType.", e);
            return 0;
        }
    }

    public long getFlags() {
        try {
            return this.mSessionBinder.getFlags();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling getFlags.", e);
            return 0;
        }
    }

    public PlaybackInfo getPlaybackInfo() {
        try {
            ParcelableVolumeInfo result = this.mSessionBinder.getVolumeAttributes();
            return new PlaybackInfo(result.volumeType, result.audioAttrs, result.controlType, result.maxVolume, result.currentVolume);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling getAudioInfo.", e);
            return null;
        }
    }

    public PendingIntent getSessionActivity() {
        try {
            return this.mSessionBinder.getLaunchPendingIntent();
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling getPendingIntent.", e);
            return null;
        }
    }

    public Token getSessionToken() {
        return this.mToken;
    }

    public void setVolumeTo(int value, int flags) {
        try {
            this.mSessionBinder.setVolumeTo(value, flags, this.mContext.getPackageName());
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling setVolumeTo.", e);
        }
    }

    public void adjustVolume(int direction, int flags) {
        try {
            this.mSessionBinder.adjustVolume(direction, flags, this.mContext.getPackageName());
        } catch (RemoteException e) {
            Log.wtf(TAG, "Error calling adjustVolumeBy.", e);
        }
    }

    public void registerCallback(Callback callback) {
        registerCallback(callback, null);
    }

    public void registerCallback(Callback callback, Handler handler) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        if (handler == null) {
            handler = new Handler();
        }
        synchronized (this.mLock) {
            addCallbackLocked(callback, handler);
        }
    }

    public void unregisterCallback(Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        synchronized (this.mLock) {
            removeCallbackLocked(callback);
        }
    }

    public void sendCommand(String command, Bundle args, ResultReceiver cb) {
        if (TextUtils.isEmpty(command)) {
            throw new IllegalArgumentException("command cannot be null or empty");
        }
        try {
            this.mSessionBinder.sendCommand(command, args, cb);
        } catch (RemoteException e) {
            Log.d(TAG, "Dead object in sendCommand.", e);
        }
    }

    public String getPackageName() {
        if (this.mPackageName == null) {
            try {
                this.mPackageName = this.mSessionBinder.getPackageName();
            } catch (RemoteException e) {
                Log.d(TAG, "Dead object in getPackageName.", e);
            }
        }
        return this.mPackageName;
    }

    public String getTag() {
        if (this.mTag == null) {
            try {
                this.mTag = this.mSessionBinder.getTag();
            } catch (RemoteException e) {
                Log.d(TAG, "Dead object in getTag.", e);
            }
        }
        return this.mTag;
    }

    ISessionController getSessionBinder() {
        return this.mSessionBinder;
    }

    public boolean controlsSameSession(MediaController other) {
        boolean z = false;
        if (other == null) {
            return false;
        }
        if (this.mSessionBinder.asBinder() == other.getSessionBinder().asBinder()) {
            z = true;
        }
        return z;
    }

    private void addCallbackLocked(Callback cb, Handler handler) {
        if (getHandlerForCallbackLocked(cb) != null) {
            Log.w(TAG, "Callback is already added, ignoring");
            return;
        }
        MessageHandler holder = new MessageHandler(handler.getLooper(), cb);
        this.mCallbacks.add(holder);
        holder.mRegistered = true;
        if (!this.mCbRegistered) {
            try {
                this.mSessionBinder.registerCallbackListener(this.mCbStub);
                this.mCbRegistered = true;
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in registerCallback", e);
            }
        }
    }

    private boolean removeCallbackLocked(Callback cb) {
        boolean success = false;
        for (int i = this.mCallbacks.size() - 1; i >= 0; i--) {
            MessageHandler handler = (MessageHandler) this.mCallbacks.get(i);
            if (cb == handler.mCallback) {
                this.mCallbacks.remove(i);
                success = true;
                handler.mRegistered = false;
            }
        }
        if (this.mCbRegistered && this.mCallbacks.size() == 0) {
            try {
                this.mSessionBinder.unregisterCallbackListener(this.mCbStub);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead object in removeCallbackLocked");
            }
            this.mCbRegistered = false;
        }
        return success;
    }

    private MessageHandler getHandlerForCallbackLocked(Callback cb) {
        if (cb == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        for (int i = this.mCallbacks.size() - 1; i >= 0; i--) {
            MessageHandler handler = (MessageHandler) this.mCallbacks.get(i);
            if (cb == handler.mCallback) {
                return handler;
            }
        }
        return null;
    }

    private final void postMessage(int what, Object obj, Bundle data) {
        synchronized (this.mLock) {
            for (int i = this.mCallbacks.size() - 1; i >= 0; i--) {
                ((MessageHandler) this.mCallbacks.get(i)).post(what, obj, data);
            }
        }
    }
}
