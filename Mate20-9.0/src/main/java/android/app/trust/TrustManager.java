package android.app.trust;

import android.app.trust.ITrustListener;
import android.app.trust.ITrustManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.ArrayMap;

public class TrustManager {
    private static final String DATA_FLAGS = "initiatedByUser";
    private static final String DATA_MESSAGE = "message";
    private static final int MSG_TRUST_CHANGED = 1;
    private static final int MSG_TRUST_ERROR = 3;
    private static final int MSG_TRUST_MANAGED_CHANGED = 2;
    private static final String TAG = "TrustManager";
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 1:
                    int flags = msg.peekData() != null ? msg.peekData().getInt(TrustManager.DATA_FLAGS) : 0;
                    TrustListener trustListener = (TrustListener) msg.obj;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    trustListener.onTrustChanged(z, msg.arg2, flags);
                    return;
                case 2:
                    TrustListener trustListener2 = (TrustListener) msg.obj;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    trustListener2.onTrustManagedChanged(z, msg.arg2);
                    return;
                case 3:
                    ((TrustListener) msg.obj).onTrustError(msg.peekData().getCharSequence("message"));
                    return;
                default:
                    return;
            }
        }
    };
    private final ITrustManager mService;
    private final ArrayMap<TrustListener, ITrustListener> mTrustListeners;

    public interface TrustListener {
        void onTrustChanged(boolean z, int i, int i2);

        void onTrustError(CharSequence charSequence);

        void onTrustManagedChanged(boolean z, int i);
    }

    public TrustManager(IBinder b) {
        this.mService = ITrustManager.Stub.asInterface(b);
        this.mTrustListeners = new ArrayMap<>();
    }

    public void setDeviceLockedForUser(int userId, boolean locked) {
        try {
            this.mService.setDeviceLockedForUser(userId, locked);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reportUnlockAttempt(boolean successful, int userId) {
        try {
            this.mService.reportUnlockAttempt(successful, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reportUnlockLockout(int timeoutMs, int userId) {
        try {
            this.mService.reportUnlockLockout(timeoutMs, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reportEnabledTrustAgentsChanged(int userId) {
        try {
            this.mService.reportEnabledTrustAgentsChanged(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reportKeyguardShowingChanged() {
        try {
            this.mService.reportKeyguardShowingChanged();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerTrustListener(final TrustListener trustListener) {
        try {
            ITrustListener.Stub iTrustListener = new ITrustListener.Stub() {
                public void onTrustChanged(boolean enabled, int userId, int flags) {
                    Message m = TrustManager.this.mHandler.obtainMessage(1, enabled, userId, trustListener);
                    if (flags != 0) {
                        m.getData().putInt(TrustManager.DATA_FLAGS, flags);
                    }
                    m.sendToTarget();
                }

                public void onTrustManagedChanged(boolean managed, int userId) {
                    TrustManager.this.mHandler.obtainMessage(2, managed, userId, trustListener).sendToTarget();
                }

                public void onTrustError(CharSequence message) {
                    Message m = TrustManager.this.mHandler.obtainMessage(3);
                    m.getData().putCharSequence("message", message);
                    m.sendToTarget();
                }
            };
            this.mService.registerTrustListener(iTrustListener);
            this.mTrustListeners.put(trustListener, iTrustListener);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unregisterTrustListener(TrustListener trustListener) {
        ITrustListener iTrustListener = this.mTrustListeners.remove(trustListener);
        if (iTrustListener != null) {
            try {
                this.mService.unregisterTrustListener(iTrustListener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public boolean isTrustUsuallyManaged(int userId) {
        try {
            return this.mService.isTrustUsuallyManaged(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unlockedByFingerprintForUser(int userId) {
        try {
            this.mService.unlockedByFingerprintForUser(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearAllFingerprints() {
        try {
            this.mService.clearAllFingerprints();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
