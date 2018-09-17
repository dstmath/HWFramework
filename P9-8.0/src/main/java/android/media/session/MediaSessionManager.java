package android.media.session;

import android.content.ComponentName;
import android.content.Context;
import android.media.IRemoteVolumeController;
import android.media.session.ICallback.Stub;
import android.media.session.MediaSession.CallbackStub;
import android.media.session.MediaSession.Token;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public final class MediaSessionManager {
    public static final int RESULT_MEDIA_KEY_HANDLED = 1;
    public static final int RESULT_MEDIA_KEY_NOT_HANDLED = 0;
    private static final String TAG = "SessionManager";
    private CallbackImpl mCallback;
    private Context mContext;
    private final ArrayMap<OnActiveSessionsChangedListener, SessionsChangedWrapper> mListeners = new ArrayMap();
    private final Object mLock = new Object();
    private OnMediaKeyListenerImpl mOnMediaKeyListener;
    private OnVolumeKeyLongPressListenerImpl mOnVolumeKeyLongPressListener;
    private final ISessionManager mService;

    public interface OnActiveSessionsChangedListener {
        void onActiveSessionsChanged(List<MediaController> list);
    }

    public static abstract class Callback {
        public abstract void onAddressedPlayerChanged(ComponentName componentName);

        public abstract void onAddressedPlayerChanged(Token token);

        public abstract void onMediaKeyEventDispatched(KeyEvent keyEvent, ComponentName componentName);

        public abstract void onMediaKeyEventDispatched(KeyEvent keyEvent, Token token);
    }

    private static final class CallbackImpl extends Stub {
        private final Callback mCallback;
        private final Handler mHandler;

        public CallbackImpl(Callback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
        }

        public void onMediaKeyEventDispatchedToMediaSession(final KeyEvent event, final Token sessionToken) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CallbackImpl.this.mCallback.onMediaKeyEventDispatched(event, sessionToken);
                }
            });
        }

        public void onMediaKeyEventDispatchedToMediaButtonReceiver(final KeyEvent event, final ComponentName mediaButtonReceiver) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CallbackImpl.this.mCallback.onMediaKeyEventDispatched(event, mediaButtonReceiver);
                }
            });
        }

        public void onAddressedPlayerChangedToMediaSession(final Token sessionToken) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CallbackImpl.this.mCallback.onAddressedPlayerChanged(sessionToken);
                }
            });
        }

        public void onAddressedPlayerChangedToMediaButtonReceiver(final ComponentName mediaButtonReceiver) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    CallbackImpl.this.mCallback.onAddressedPlayerChanged(mediaButtonReceiver);
                }
            });
        }
    }

    public interface OnMediaKeyListener {
        boolean onMediaKey(KeyEvent keyEvent);
    }

    private static final class OnMediaKeyListenerImpl extends IOnMediaKeyListener.Stub {
        private Handler mHandler;
        private OnMediaKeyListener mListener;

        public OnMediaKeyListenerImpl(OnMediaKeyListener listener, Handler handler) {
            this.mListener = listener;
            this.mHandler = handler;
        }

        public void onMediaKey(final KeyEvent event, final ResultReceiver result) {
            if (this.mListener == null || this.mHandler == null) {
                Log.w(MediaSessionManager.TAG, "Failed to call media key listener. Either mListener or mHandler is null");
            } else {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        boolean handled = OnMediaKeyListenerImpl.this.mListener.onMediaKey(event);
                        Log.d(MediaSessionManager.TAG, "The media key listener is returned " + handled);
                        if (result != null) {
                            result.send(handled ? 1 : 0, null);
                        }
                    }
                });
            }
        }
    }

    public interface OnVolumeKeyLongPressListener {
        void onVolumeKeyLongPress(KeyEvent keyEvent);
    }

    private static final class OnVolumeKeyLongPressListenerImpl extends IOnVolumeKeyLongPressListener.Stub {
        private Handler mHandler;
        private OnVolumeKeyLongPressListener mListener;

        public OnVolumeKeyLongPressListenerImpl(OnVolumeKeyLongPressListener listener, Handler handler) {
            this.mListener = listener;
            this.mHandler = handler;
        }

        public void onVolumeKeyLongPress(final KeyEvent event) {
            if (this.mListener == null || this.mHandler == null) {
                Log.w(MediaSessionManager.TAG, "Failed to call volume key long-press listener. Either mListener or mHandler is null");
            } else {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        OnVolumeKeyLongPressListenerImpl.this.mListener.onVolumeKeyLongPress(event);
                    }
                });
            }
        }
    }

    private static final class SessionsChangedWrapper {
        private Context mContext;
        private Handler mHandler;
        private OnActiveSessionsChangedListener mListener;
        private final IActiveSessionsListener.Stub mStub = new IActiveSessionsListener.Stub() {
            public void onActiveSessionsChanged(final List<Token> tokens) {
                if (SessionsChangedWrapper.this.mHandler != null) {
                    SessionsChangedWrapper.this.mHandler.post(new Runnable() {
                        public void run() {
                            if (SessionsChangedWrapper.this.mListener != null) {
                                ArrayList<MediaController> controllers = new ArrayList();
                                int size = tokens.size();
                                for (int i = 0; i < size; i++) {
                                    controllers.add(new MediaController(SessionsChangedWrapper.this.mContext, (Token) tokens.get(i)));
                                }
                                SessionsChangedWrapper.this.mListener.onActiveSessionsChanged(controllers);
                            }
                        }
                    });
                }
            }
        };

        public SessionsChangedWrapper(Context context, OnActiveSessionsChangedListener listener, Handler handler) {
            this.mContext = context;
            this.mListener = listener;
            this.mHandler = handler;
        }

        private void release() {
            this.mContext = null;
            this.mListener = null;
            this.mHandler = null;
        }
    }

    public MediaSessionManager(Context context) {
        this.mContext = context;
        this.mService = ISessionManager.Stub.asInterface(ServiceManager.getService(Context.MEDIA_SESSION_SERVICE));
    }

    public ISession createSession(CallbackStub cbStub, String tag, int userId) throws RemoteException {
        return this.mService.createSession(this.mContext.getPackageName(), cbStub, tag, userId);
    }

    public List<MediaController> getActiveSessions(ComponentName notificationListener) {
        return getActiveSessionsForUser(notificationListener, UserHandle.myUserId());
    }

    public List<MediaController> getActiveSessionsForUser(ComponentName notificationListener, int userId) {
        ArrayList<MediaController> controllers = new ArrayList();
        try {
            List<IBinder> binders = this.mService.getSessions(notificationListener, userId);
            int size = binders.size();
            for (int i = 0; i < size; i++) {
                controllers.add(new MediaController(this.mContext, ISessionController.Stub.asInterface((IBinder) binders.get(i))));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get active sessions: ", e);
        }
        return controllers;
    }

    public void addOnActiveSessionsChangedListener(OnActiveSessionsChangedListener sessionListener, ComponentName notificationListener) {
        addOnActiveSessionsChangedListener(sessionListener, notificationListener, null);
    }

    public void addOnActiveSessionsChangedListener(OnActiveSessionsChangedListener sessionListener, ComponentName notificationListener, Handler handler) {
        addOnActiveSessionsChangedListener(sessionListener, notificationListener, UserHandle.myUserId(), handler);
    }

    public void addOnActiveSessionsChangedListener(OnActiveSessionsChangedListener sessionListener, ComponentName notificationListener, int userId, Handler handler) {
        if (sessionListener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
        if (handler == null) {
            handler = new Handler();
        }
        synchronized (this.mLock) {
            if (this.mListeners.get(sessionListener) != null) {
                Log.w(TAG, "Attempted to add session listener twice, ignoring.");
                return;
            }
            SessionsChangedWrapper wrapper = new SessionsChangedWrapper(this.mContext, sessionListener, handler);
            try {
                this.mService.addSessionsListener(wrapper.mStub, notificationListener, userId);
                this.mListeners.put(sessionListener, wrapper);
            } catch (RemoteException e) {
                Log.e(TAG, "Error in addOnActiveSessionsChangedListener.", e);
            }
        }
    }

    public void removeOnActiveSessionsChangedListener(OnActiveSessionsChangedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
        synchronized (this.mLock) {
            SessionsChangedWrapper wrapper = (SessionsChangedWrapper) this.mListeners.remove(listener);
            if (wrapper != null) {
                try {
                    this.mService.removeSessionsListener(wrapper.mStub);
                    wrapper.release();
                } catch (RemoteException e) {
                    Log.e(TAG, "Error in removeOnActiveSessionsChangedListener.", e);
                    wrapper.release();
                } catch (Throwable th) {
                    wrapper.release();
                }
            }
        }
        return;
    }

    public void setRemoteVolumeController(IRemoteVolumeController rvc) {
        try {
            this.mService.setRemoteVolumeController(rvc);
        } catch (RemoteException e) {
            Log.e(TAG, "Error in setRemoteVolumeController.", e);
        }
    }

    public void dispatchMediaKeyEvent(KeyEvent keyEvent) {
        dispatchMediaKeyEvent(keyEvent, false);
    }

    public void dispatchMediaKeyEvent(KeyEvent keyEvent, boolean needWakeLock) {
        try {
            this.mService.dispatchMediaKeyEvent(keyEvent, needWakeLock);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to send key event.", e);
        }
    }

    public void dispatchVolumeKeyEvent(KeyEvent keyEvent, int stream, boolean musicOnly) {
        try {
            this.mService.dispatchVolumeKeyEvent(keyEvent, stream, musicOnly);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to send volume key event.", e);
        }
    }

    public void dispatchAdjustVolume(int suggestedStream, int direction, int flags) {
        try {
            this.mService.dispatchAdjustVolume(suggestedStream, direction, flags);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to send adjust volume.", e);
        }
    }

    public boolean isGlobalPriorityActive() {
        try {
            return this.mService.isGlobalPriorityActive();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to check if the global priority is active.", e);
            return false;
        }
    }

    public void setOnVolumeKeyLongPressListener(OnVolumeKeyLongPressListener listener, Handler handler) {
        synchronized (this.mLock) {
            if (listener == null) {
                try {
                    this.mOnVolumeKeyLongPressListener = null;
                    this.mService.setOnVolumeKeyLongPressListener(null);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to set volume key long press listener", e);
                }
            } else {
                if (handler == null) {
                    handler = new Handler();
                }
                this.mOnVolumeKeyLongPressListener = new OnVolumeKeyLongPressListenerImpl(listener, handler);
                this.mService.setOnVolumeKeyLongPressListener(this.mOnVolumeKeyLongPressListener);
            }
        }
        return;
    }

    public void setOnMediaKeyListener(OnMediaKeyListener listener, Handler handler) {
        synchronized (this.mLock) {
            if (listener == null) {
                try {
                    this.mOnMediaKeyListener = null;
                    this.mService.setOnMediaKeyListener(null);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to set media key listener", e);
                }
            } else {
                if (handler == null) {
                    handler = new Handler();
                }
                this.mOnMediaKeyListener = new OnMediaKeyListenerImpl(listener, handler);
                this.mService.setOnMediaKeyListener(this.mOnMediaKeyListener);
            }
        }
        return;
    }

    public void setCallback(Callback callback, Handler handler) {
        synchronized (this.mLock) {
            if (callback == null) {
                try {
                    this.mCallback = null;
                    this.mService.setCallback(null);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to set media key callback", e);
                }
            } else {
                if (handler == null) {
                    handler = new Handler();
                }
                this.mCallback = new CallbackImpl(callback, handler);
                this.mService.setCallback(this.mCallback);
            }
        }
        return;
    }
}
