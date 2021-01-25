package android.media;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.session.MediaController;
import android.media.session.MediaSessionLegacyHelper;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import java.util.List;

@Deprecated
public final class RemoteController {
    private static final boolean DEBUG = false;
    private static final int MAX_BITMAP_DIMENSION = 512;
    private static final int MSG_CLIENT_CHANGE = 0;
    private static final int MSG_NEW_MEDIA_METADATA = 2;
    private static final int MSG_NEW_PLAYBACK_STATE = 1;
    public static final int POSITION_SYNCHRONIZATION_CHECK = 1;
    public static final int POSITION_SYNCHRONIZATION_NONE = 0;
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int SENDMSG_REPLACE = 0;
    private static final String TAG = "RemoteController";
    private static final Object mInfoLock = new Object();
    private int mArtworkHeight;
    private int mArtworkWidth;
    private final Context mContext;
    @UnsupportedAppUsage
    private MediaController mCurrentSession;
    private boolean mEnabled;
    private final EventHandler mEventHandler;
    private boolean mIsRegistered;
    private PlaybackInfo mLastPlaybackInfo;
    private final int mMaxBitmapDimension;
    private MetadataEditor mMetadataEditor;
    private OnClientUpdateListener mOnClientUpdateListener;
    private MediaController.Callback mSessionCb;
    private MediaSessionManager.OnActiveSessionsChangedListener mSessionListener;
    private MediaSessionManager mSessionManager;

    public interface OnClientUpdateListener {
        void onClientChange(boolean z);

        void onClientMetadataUpdate(MetadataEditor metadataEditor);

        void onClientPlaybackStateUpdate(int i);

        void onClientPlaybackStateUpdate(int i, long j, long j2, float f);

        void onClientTransportControlUpdate(int i);
    }

    public RemoteController(Context context, OnClientUpdateListener updateListener) throws IllegalArgumentException {
        this(context, updateListener, null);
    }

    public RemoteController(Context context, OnClientUpdateListener updateListener, Looper looper) throws IllegalArgumentException {
        this.mSessionCb = new MediaControllerCallback();
        this.mIsRegistered = false;
        this.mArtworkWidth = -1;
        this.mArtworkHeight = -1;
        this.mEnabled = true;
        if (context == null) {
            throw new IllegalArgumentException("Invalid null Context");
        } else if (updateListener != null) {
            if (looper != null) {
                this.mEventHandler = new EventHandler(this, looper);
            } else {
                Looper l = Looper.myLooper();
                if (l != null) {
                    this.mEventHandler = new EventHandler(this, l);
                } else {
                    throw new IllegalArgumentException("Calling thread not associated with a looper");
                }
            }
            this.mOnClientUpdateListener = updateListener;
            this.mContext = context;
            this.mSessionManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
            this.mSessionListener = new TopTransportSessionListener();
            if (ActivityManager.isLowRamDeviceStatic()) {
                this.mMaxBitmapDimension = 512;
                return;
            }
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            this.mMaxBitmapDimension = Math.max(dm.widthPixels, dm.heightPixels);
        } else {
            throw new IllegalArgumentException("Invalid null OnClientUpdateListener");
        }
    }

    public long getEstimatedMediaPosition() {
        PlaybackState state;
        synchronized (mInfoLock) {
            if (this.mCurrentSession == null || (state = this.mCurrentSession.getPlaybackState()) == null) {
                return -1;
            }
            return state.getPosition();
        }
    }

    public boolean sendMediaKeyEvent(KeyEvent keyEvent) throws IllegalArgumentException {
        if (KeyEvent.isMediaSessionKey(keyEvent.getKeyCode())) {
            synchronized (mInfoLock) {
                if (this.mCurrentSession == null) {
                    return false;
                }
                return this.mCurrentSession.dispatchMediaButtonEvent(keyEvent);
            }
        }
        throw new IllegalArgumentException("not a media key event");
    }

    public boolean seekTo(long timeMs) throws IllegalArgumentException {
        if (!this.mEnabled) {
            Log.e(TAG, "Cannot use seekTo() from a disabled RemoteController");
            return false;
        } else if (timeMs >= 0) {
            synchronized (mInfoLock) {
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.getTransportControls().seekTo(timeMs);
                }
            }
            return true;
        } else {
            throw new IllegalArgumentException("illegal negative time value");
        }
    }

    @UnsupportedAppUsage
    public boolean setArtworkConfiguration(boolean wantBitmap, int width, int height) throws IllegalArgumentException {
        synchronized (mInfoLock) {
            if (!wantBitmap) {
                this.mArtworkWidth = -1;
                this.mArtworkHeight = -1;
            } else if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Invalid dimensions");
            } else {
                if (width > this.mMaxBitmapDimension) {
                    width = this.mMaxBitmapDimension;
                }
                if (height > this.mMaxBitmapDimension) {
                    height = this.mMaxBitmapDimension;
                }
                this.mArtworkWidth = width;
                this.mArtworkHeight = height;
            }
        }
        return true;
    }

    public boolean setArtworkConfiguration(int width, int height) throws IllegalArgumentException {
        return setArtworkConfiguration(true, width, height);
    }

    public boolean clearArtworkConfiguration() {
        return setArtworkConfiguration(false, -1, -1);
    }

    public boolean setSynchronizationMode(int sync) throws IllegalArgumentException {
        if (sync != 0 && sync != 1) {
            throw new IllegalArgumentException("Unknown synchronization mode " + sync);
        } else if (this.mIsRegistered) {
            return true;
        } else {
            Log.e(TAG, "Cannot set synchronization mode on an unregistered RemoteController");
            return false;
        }
    }

    public MetadataEditor editMetadata() {
        MetadataEditor editor = new MetadataEditor();
        editor.mEditorMetadata = new Bundle();
        editor.mEditorArtwork = null;
        editor.mMetadataChanged = true;
        editor.mArtworkChanged = true;
        editor.mEditableKeys = 0;
        return editor;
    }

    public class MetadataEditor extends MediaMetadataEditor {
        protected MetadataEditor() {
        }

        protected MetadataEditor(Bundle metadata, long editableKeys) {
            this.mEditorMetadata = metadata;
            this.mEditableKeys = editableKeys;
            this.mEditorArtwork = (Bitmap) metadata.getParcelable(String.valueOf(100));
            if (this.mEditorArtwork != null) {
                cleanupBitmapFromBundle(100);
            }
            this.mMetadataChanged = true;
            this.mArtworkChanged = true;
            this.mApplied = false;
        }

        private void cleanupBitmapFromBundle(int key) {
            if (METADATA_KEYS_TYPE.get(key, -1) == 2) {
                this.mEditorMetadata.remove(String.valueOf(key));
            }
        }

        @Override // android.media.MediaMetadataEditor
        public synchronized void apply() {
            Throwable th;
            Rating rating;
            if (this.mMetadataChanged) {
                synchronized (RemoteController.mInfoLock) {
                    try {
                        if (RemoteController.this.mCurrentSession != null) {
                            try {
                                if (this.mEditorMetadata.containsKey(String.valueOf((int) MediaMetadataEditor.RATING_KEY_BY_USER)) && (rating = (Rating) getObject(MediaMetadataEditor.RATING_KEY_BY_USER, null)) != null) {
                                    RemoteController.this.mCurrentSession.getTransportControls().setRating(rating);
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                        this.mApplied = false;
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            }
        }
    }

    private class MediaControllerCallback extends MediaController.Callback {
        private MediaControllerCallback() {
        }

        @Override // android.media.session.MediaController.Callback
        public void onPlaybackStateChanged(PlaybackState state) {
            RemoteController.this.onNewPlaybackState(state);
        }

        @Override // android.media.session.MediaController.Callback
        public void onMetadataChanged(MediaMetadata metadata) {
            RemoteController.this.onNewMediaMetadata(metadata);
        }
    }

    private class TopTransportSessionListener implements MediaSessionManager.OnActiveSessionsChangedListener {
        private TopTransportSessionListener() {
        }

        @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
        public void onActiveSessionsChanged(List<MediaController> controllers) {
            int size = controllers.size();
            for (int i = 0; i < size; i++) {
                MediaController controller = controllers.get(i);
                if ((2 & controller.getFlags()) != 0) {
                    RemoteController.this.updateController(controller);
                    return;
                }
            }
            RemoteController.this.updateController(null);
        }
    }

    /* access modifiers changed from: private */
    public class EventHandler extends Handler {
        public EventHandler(RemoteController rc, Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            boolean z = true;
            if (i == 0) {
                RemoteController remoteController = RemoteController.this;
                if (msg.arg2 != 1) {
                    z = false;
                }
                remoteController.onClientChange(z);
            } else if (i == 1) {
                RemoteController.this.onNewPlaybackState((PlaybackState) msg.obj);
            } else if (i != 2) {
                Log.e(RemoteController.TAG, "unknown event " + msg.what);
            } else {
                RemoteController.this.onNewMediaMetadata((MediaMetadata) msg.obj);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startListeningToSessions() {
        ComponentName listenerComponent = new ComponentName(this.mContext, this.mOnClientUpdateListener.getClass());
        Handler handler = null;
        if (Looper.myLooper() == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        this.mSessionManager.addOnActiveSessionsChangedListener(this.mSessionListener, listenerComponent, UserHandle.myUserId(), handler);
        this.mSessionListener.onActiveSessionsChanged(this.mSessionManager.getActiveSessions(listenerComponent));
    }

    /* access modifiers changed from: package-private */
    public void stopListeningToSessions() {
        this.mSessionManager.removeOnActiveSessionsChangedListener(this.mSessionListener);
    }

    private static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delayMs) {
        if (handler == null) {
            Log.e(TAG, "null event handler, will not deliver message " + msg);
            return;
        }
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == 1 && handler.hasMessages(msg)) {
            return;
        }
        handler.sendMessageDelayed(handler.obtainMessage(msg, arg1, arg2, obj), (long) delayMs);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onClientChange(boolean clearing) {
        OnClientUpdateListener l;
        synchronized (mInfoLock) {
            l = this.mOnClientUpdateListener;
            this.mMetadataEditor = null;
        }
        if (l != null) {
            l.onClientChange(clearing);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateController(MediaController controller) {
        synchronized (mInfoLock) {
            if (controller == null) {
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.unregisterCallback(this.mSessionCb);
                    this.mCurrentSession = null;
                    sendMsg(this.mEventHandler, 0, 0, 0, 1, null, 0);
                }
            } else if (this.mCurrentSession == null || !controller.getSessionToken().equals(this.mCurrentSession.getSessionToken())) {
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.unregisterCallback(this.mSessionCb);
                }
                sendMsg(this.mEventHandler, 0, 0, 0, 0, null, 0);
                this.mCurrentSession = controller;
                this.mCurrentSession.registerCallback(this.mSessionCb, this.mEventHandler);
                sendMsg(this.mEventHandler, 1, 0, 0, 0, controller.getPlaybackState(), 0);
                sendMsg(this.mEventHandler, 2, 0, 0, 0, controller.getMetadata(), 0);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNewPlaybackState(PlaybackState state) {
        OnClientUpdateListener l;
        int playstate;
        synchronized (mInfoLock) {
            l = this.mOnClientUpdateListener;
        }
        if (l != null) {
            if (state == null) {
                playstate = 0;
            } else {
                playstate = RemoteControlClient.getRccStateFromState(state.getState());
            }
            if (state == null || state.getPosition() == -1) {
                l.onClientPlaybackStateUpdate(playstate);
            } else {
                l.onClientPlaybackStateUpdate(playstate, state.getLastPositionUpdateTime(), state.getPosition(), state.getPlaybackSpeed());
            }
            if (state != null) {
                l.onClientTransportControlUpdate(RemoteControlClient.getRccControlFlagsFromActions(state.getActions()));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNewMediaMetadata(MediaMetadata metadata) {
        OnClientUpdateListener l;
        MetadataEditor metadataEditor;
        if (metadata != null) {
            synchronized (mInfoLock) {
                l = this.mOnClientUpdateListener;
                this.mMetadataEditor = new MetadataEditor(MediaSessionLegacyHelper.getOldMetadata(metadata, this.mArtworkWidth, this.mArtworkHeight), this.mCurrentSession != null && this.mCurrentSession.getRatingType() != 0 ? 268435457 : 0);
                metadataEditor = this.mMetadataEditor;
            }
            if (l != null) {
                l.onClientMetadataUpdate(metadataEditor);
            }
        }
    }

    private static class PlaybackInfo {
        long mCurrentPosMs;
        float mSpeed;
        int mState;
        long mStateChangeTimeMs;

        PlaybackInfo(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
            this.mState = state;
            this.mStateChangeTimeMs = stateChangeTimeMs;
            this.mCurrentPosMs = currentPosMs;
            this.mSpeed = speed;
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public OnClientUpdateListener getUpdateListener() {
        return this.mOnClientUpdateListener;
    }
}
