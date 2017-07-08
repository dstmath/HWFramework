package android.media;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.session.MediaController;
import android.media.session.MediaController.Callback;
import android.media.session.MediaSessionLegacyHelper;
import android.media.session.MediaSessionManager;
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener;
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
    private static final Object mInfoLock = null;
    private int mArtworkHeight;
    private int mArtworkWidth;
    private final Context mContext;
    private MediaController mCurrentSession;
    private boolean mEnabled;
    private final EventHandler mEventHandler;
    private boolean mIsRegistered;
    private PlaybackInfo mLastPlaybackInfo;
    private final int mMaxBitmapDimension;
    private MetadataEditor mMetadataEditor;
    private OnClientUpdateListener mOnClientUpdateListener;
    private Callback mSessionCb;
    private OnActiveSessionsChangedListener mSessionListener;
    private MediaSessionManager mSessionManager;

    private class EventHandler extends Handler {
        public EventHandler(RemoteController rc, Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case RemoteController.SENDMSG_REPLACE /*0*/:
                    RemoteController remoteController = RemoteController.this;
                    if (msg.arg2 != RemoteController.SENDMSG_NOOP) {
                        z = RemoteController.DEBUG;
                    }
                    remoteController.onClientChange(z);
                case RemoteController.SENDMSG_NOOP /*1*/:
                    RemoteController.this.onNewPlaybackState((PlaybackState) msg.obj);
                case RemoteController.SENDMSG_QUEUE /*2*/:
                    RemoteController.this.onNewMediaMetadata((MediaMetadata) msg.obj);
                default:
                    Log.e(RemoteController.TAG, "unknown event " + msg.what);
            }
        }
    }

    private class MediaControllerCallback extends Callback {
        private MediaControllerCallback() {
        }

        public void onPlaybackStateChanged(PlaybackState state) {
            RemoteController.this.onNewPlaybackState(state);
        }

        public void onMetadataChanged(MediaMetadata metadata) {
            RemoteController.this.onNewMediaMetadata(metadata);
        }
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
            this.mApplied = RemoteController.DEBUG;
        }

        private void cleanupBitmapFromBundle(int key) {
            if (METADATA_KEYS_TYPE.get(key, -1) == RemoteController.SENDMSG_QUEUE) {
                this.mEditorMetadata.remove(String.valueOf(key));
            }
        }

        public synchronized void apply() {
            if (this.mMetadataChanged) {
                synchronized (RemoteController.mInfoLock) {
                    if (RemoteController.this.mCurrentSession != null && this.mEditorMetadata.containsKey(String.valueOf(MediaMetadataEditor.RATING_KEY_BY_USER))) {
                        Rating rating = (Rating) getObject(MediaMetadataEditor.RATING_KEY_BY_USER, null);
                        if (rating != null) {
                            RemoteController.this.mCurrentSession.getTransportControls().setRating(rating);
                        }
                    }
                }
                this.mApplied = RemoteController.DEBUG;
            }
        }
    }

    public interface OnClientUpdateListener {
        void onClientChange(boolean z);

        void onClientMetadataUpdate(MetadataEditor metadataEditor);

        void onClientPlaybackStateUpdate(int i);

        void onClientPlaybackStateUpdate(int i, long j, long j2, float f);

        void onClientTransportControlUpdate(int i);
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

    private class TopTransportSessionListener implements OnActiveSessionsChangedListener {
        private TopTransportSessionListener() {
        }

        public void onActiveSessionsChanged(List<MediaController> controllers) {
            int size = controllers.size();
            for (int i = RemoteController.SENDMSG_REPLACE; i < size; i += RemoteController.SENDMSG_NOOP) {
                MediaController controller = (MediaController) controllers.get(i);
                if ((2 & controller.getFlags()) != 0) {
                    RemoteController.this.updateController(controller);
                    return;
                }
            }
            RemoteController.this.updateController(null);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.RemoteController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.RemoteController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteController.<clinit>():void");
    }

    public RemoteController(Context context, OnClientUpdateListener updateListener) throws IllegalArgumentException {
        this(context, updateListener, null);
    }

    public RemoteController(Context context, OnClientUpdateListener updateListener, Looper looper) throws IllegalArgumentException {
        this.mSessionCb = new MediaControllerCallback();
        this.mIsRegistered = DEBUG;
        this.mArtworkWidth = -1;
        this.mArtworkHeight = -1;
        this.mEnabled = true;
        if (context == null) {
            throw new IllegalArgumentException("Invalid null Context");
        } else if (updateListener == null) {
            throw new IllegalArgumentException("Invalid null OnClientUpdateListener");
        } else {
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
                this.mMaxBitmapDimension = MAX_BITMAP_DIMENSION;
                return;
            }
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            this.mMaxBitmapDimension = Math.max(dm.widthPixels, dm.heightPixels);
        }
    }

    public long getEstimatedMediaPosition() {
        synchronized (mInfoLock) {
            if (this.mCurrentSession != null) {
                PlaybackState state = this.mCurrentSession.getPlaybackState();
                if (state != null) {
                    long position = state.getPosition();
                    return position;
                }
            }
            return -1;
        }
    }

    public boolean sendMediaKeyEvent(KeyEvent keyEvent) throws IllegalArgumentException {
        if (KeyEvent.isMediaKey(keyEvent.getKeyCode())) {
            synchronized (mInfoLock) {
                if (this.mCurrentSession != null) {
                    boolean dispatchMediaButtonEvent = this.mCurrentSession.dispatchMediaButtonEvent(keyEvent);
                    return dispatchMediaButtonEvent;
                }
                return DEBUG;
            }
        }
        throw new IllegalArgumentException("not a media key event");
    }

    public boolean seekTo(long timeMs) throws IllegalArgumentException {
        if (!this.mEnabled) {
            Log.e(TAG, "Cannot use seekTo() from a disabled RemoteController");
            return DEBUG;
        } else if (timeMs < 0) {
            throw new IllegalArgumentException("illegal negative time value");
        } else {
            synchronized (mInfoLock) {
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.getTransportControls().seekTo(timeMs);
                }
            }
            return true;
        }
    }

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
        return setArtworkConfiguration(DEBUG, -1, -1);
    }

    public boolean setSynchronizationMode(int sync) throws IllegalArgumentException {
        if (sync != 0 && sync != SENDMSG_NOOP) {
            throw new IllegalArgumentException("Unknown synchronization mode " + sync);
        } else if (this.mIsRegistered) {
            return true;
        } else {
            Log.e(TAG, "Cannot set synchronization mode on an unregistered RemoteController");
            return DEBUG;
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

    void startListeningToSessions() {
        ComponentName listenerComponent = new ComponentName(this.mContext, this.mOnClientUpdateListener.getClass());
        Handler handler = null;
        if (Looper.myLooper() == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        this.mSessionManager.addOnActiveSessionsChangedListener(this.mSessionListener, listenerComponent, UserHandle.myUserId(), handler);
        this.mSessionListener.onActiveSessionsChanged(this.mSessionManager.getActiveSessions(listenerComponent));
    }

    void stopListeningToSessions() {
        this.mSessionManager.removeOnActiveSessionsChangedListener(this.mSessionListener);
    }

    private static void sendMsg(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delayMs) {
        if (handler == null) {
            Log.e(TAG, "null event handler, will not deliver message " + msg);
            return;
        }
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == SENDMSG_NOOP && handler.hasMessages(msg)) {
            return;
        }
        handler.sendMessageDelayed(handler.obtainMessage(msg, arg1, arg2, obj), (long) delayMs);
    }

    private void onClientChange(boolean clearing) {
        synchronized (mInfoLock) {
            OnClientUpdateListener l = this.mOnClientUpdateListener;
            this.mMetadataEditor = null;
        }
        if (l != null) {
            l.onClientChange(clearing);
        }
    }

    private void updateController(MediaController controller) {
        synchronized (mInfoLock) {
            if (controller == null) {
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.unregisterCallback(this.mSessionCb);
                    this.mCurrentSession = null;
                    sendMsg(this.mEventHandler, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_NOOP, null, SENDMSG_REPLACE);
                }
            } else if (this.mCurrentSession == null || !controller.getSessionToken().equals(this.mCurrentSession.getSessionToken())) {
                if (this.mCurrentSession != null) {
                    this.mCurrentSession.unregisterCallback(this.mSessionCb);
                }
                sendMsg(this.mEventHandler, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, null, SENDMSG_REPLACE);
                this.mCurrentSession = controller;
                this.mCurrentSession.registerCallback(this.mSessionCb, this.mEventHandler);
                sendMsg(this.mEventHandler, SENDMSG_NOOP, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, controller.getPlaybackState(), SENDMSG_REPLACE);
                sendMsg(this.mEventHandler, SENDMSG_QUEUE, SENDMSG_REPLACE, SENDMSG_REPLACE, SENDMSG_REPLACE, controller.getMetadata(), SENDMSG_REPLACE);
            }
        }
    }

    private void onNewPlaybackState(PlaybackState state) {
        synchronized (mInfoLock) {
            OnClientUpdateListener l = this.mOnClientUpdateListener;
        }
        if (l != null) {
            int playstate = state == null ? SENDMSG_REPLACE : PlaybackState.getRccStateFromState(state.getState());
            if (state == null || state.getPosition() == -1) {
                l.onClientPlaybackStateUpdate(playstate);
            } else {
                l.onClientPlaybackStateUpdate(playstate, state.getLastPositionUpdateTime(), state.getPosition(), state.getPlaybackSpeed());
            }
            if (state != null) {
                l.onClientTransportControlUpdate(PlaybackState.getRccControlFlagsFromActions(state.getActions()));
            }
        }
    }

    private void onNewMediaMetadata(MediaMetadata metadata) {
        int i = SENDMSG_REPLACE;
        if (metadata != null) {
            OnClientUpdateListener l;
            MetadataEditor metadataEditor;
            synchronized (mInfoLock) {
                l = this.mOnClientUpdateListener;
                boolean canRate = this.mCurrentSession != null ? this.mCurrentSession.getRatingType() != 0 ? true : DEBUG : DEBUG;
                if (canRate) {
                    i = MediaMetadataEditor.RATING_KEY_BY_USER;
                }
                this.mMetadataEditor = new MetadataEditor(MediaSessionLegacyHelper.getOldMetadata(metadata, this.mArtworkWidth, this.mArtworkHeight), (long) i);
                metadataEditor = this.mMetadataEditor;
            }
            if (l != null) {
                l.onClientMetadataUpdate(metadataEditor);
            }
        }
    }

    OnClientUpdateListener getUpdateListener() {
        return this.mOnClientUpdateListener;
    }
}
