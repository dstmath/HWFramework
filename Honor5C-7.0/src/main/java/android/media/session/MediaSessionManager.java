package android.media.session;

import android.content.ComponentName;
import android.content.Context;
import android.media.IRemoteVolumeController;
import android.media.session.IActiveSessionsListener.Stub;
import android.media.session.MediaSession.CallbackStub;
import android.media.session.MediaSession.Token;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public final class MediaSessionManager {
    private static final String TAG = "SessionManager";
    private Context mContext;
    private final ArrayMap<OnActiveSessionsChangedListener, SessionsChangedWrapper> mListeners;
    private final Object mLock;
    private final ISessionManager mService;

    public interface OnActiveSessionsChangedListener {
        void onActiveSessionsChanged(List<MediaController> list);
    }

    private static final class SessionsChangedWrapper {
        private Context mContext;
        private Handler mHandler;
        private OnActiveSessionsChangedListener mListener;
        private final Stub mStub;

        public SessionsChangedWrapper(Context context, OnActiveSessionsChangedListener listener, Handler handler) {
            this.mStub = new Stub() {

                /* renamed from: android.media.session.MediaSessionManager.SessionsChangedWrapper.1.1 */
                class AnonymousClass1 implements Runnable {
                    final /* synthetic */ List val$tokens;

                    AnonymousClass1(List val$tokens) {
                        this.val$tokens = val$tokens;
                    }

                    public void run() {
                        if (SessionsChangedWrapper.this.mListener != null) {
                            ArrayList<MediaController> controllers = new ArrayList();
                            int size = this.val$tokens.size();
                            for (int i = 0; i < size; i++) {
                                controllers.add(new MediaController(SessionsChangedWrapper.this.mContext, (Token) this.val$tokens.get(i)));
                            }
                            SessionsChangedWrapper.this.mListener.onActiveSessionsChanged(controllers);
                        }
                    }
                }

                public void onActiveSessionsChanged(List<Token> tokens) {
                    if (SessionsChangedWrapper.this.mHandler != null) {
                        SessionsChangedWrapper.this.mHandler.post(new AnonymousClass1(tokens));
                    }
                }
            };
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
        this.mListeners = new ArrayMap();
        this.mLock = new Object();
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
}
