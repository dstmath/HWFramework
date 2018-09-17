package com.android.server.accessibility;

import android.hardware.fingerprint.IFingerprintClientActiveCallback.Stub;
import android.hardware.fingerprint.IFingerprintService;
import android.os.Binder;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.RemoteException;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;

public class FingerprintGestureDispatcher extends Stub implements Callback {
    private static final String LOG_TAG = "FingerprintGestureDispatcher";
    private static final int MSG_REGISTER = 1;
    private static final int MSG_UNREGISTER = 2;
    private final List<FingerprintGestureClient> mCapturingClients = new ArrayList(0);
    private final IFingerprintService mFingerprintService;
    private final Handler mHandler;
    private final Object mLock;
    private boolean mRegisteredReadOnlyExceptInHandler;

    public interface FingerprintGestureClient {
        boolean isCapturingFingerprintGestures();

        void onFingerprintGesture(int i);

        void onFingerprintGestureDetectionActiveChanged(boolean z);
    }

    public FingerprintGestureDispatcher(IFingerprintService fingerprintService, Object lock) {
        this.mFingerprintService = fingerprintService;
        this.mLock = lock;
        this.mHandler = new Handler(this);
    }

    public FingerprintGestureDispatcher(IFingerprintService fingerprintService, Object lock, Handler handler) {
        this.mFingerprintService = fingerprintService;
        this.mLock = lock;
        this.mHandler = handler;
    }

    public void updateClientList(List<? extends FingerprintGestureClient> clientList) {
        synchronized (this.mLock) {
            this.mCapturingClients.clear();
            for (int i = 0; i < clientList.size(); i++) {
                FingerprintGestureClient client = (FingerprintGestureClient) clientList.get(i);
                if (client.isCapturingFingerprintGestures()) {
                    this.mCapturingClients.add(client);
                }
            }
            if (this.mCapturingClients.isEmpty()) {
                if (this.mRegisteredReadOnlyExceptInHandler) {
                    this.mHandler.obtainMessage(2).sendToTarget();
                }
            } else if (!this.mRegisteredReadOnlyExceptInHandler) {
                this.mHandler.obtainMessage(1).sendToTarget();
            }
        }
    }

    public void onClientActiveChanged(boolean nonGestureFingerprintClientActive) {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mCapturingClients.size(); i++) {
                ((FingerprintGestureClient) this.mCapturingClients.get(i)).onFingerprintGestureDetectionActiveChanged(nonGestureFingerprintClientActive ^ 1);
            }
        }
    }

    public boolean isFingerprintGestureDetectionAvailable() {
        boolean isClientActive;
        try {
            isClientActive = this.mFingerprintService.isClientActive() ^ 1;
            return isClientActive;
        } catch (RemoteException e) {
            isClientActive = false;
            return isClientActive;
        } finally {
            Binder.restoreCallingIdentity(Binder.clearCallingIdentity());
        }
    }

    /* JADX WARNING: Missing block: B:14:0x001c, code:
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:16:0x0021, code:
            if (r1 >= r0.size()) goto L_0x0039;
     */
    /* JADX WARNING: Missing block: B:17:0x0023, code:
            ((com.android.server.accessibility.FingerprintGestureDispatcher.FingerprintGestureClient) r0.get(r1)).onFingerprintGesture(r2);
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:25:0x003a, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onFingerprintGesture(int fingerprintKeyCode) {
        synchronized (this.mLock) {
            if (this.mCapturingClients.isEmpty()) {
                return false;
            }
            int idForFingerprintGestureManager;
            switch (fingerprintKeyCode) {
                case 280:
                    idForFingerprintGestureManager = 4;
                    break;
                case 281:
                    idForFingerprintGestureManager = 8;
                    break;
                case 282:
                    idForFingerprintGestureManager = 2;
                    break;
                case 283:
                    idForFingerprintGestureManager = 1;
                    break;
                default:
                    return false;
            }
            List<FingerprintGestureClient> clientList = new ArrayList(this.mCapturingClients);
        }
    }

    public boolean handleMessage(Message message) {
        long identity;
        if (message.what == 1) {
            identity = Binder.clearCallingIdentity();
            try {
                this.mFingerprintService.addClientActiveCallback(this);
                this.mRegisteredReadOnlyExceptInHandler = true;
            } catch (RemoteException e) {
                Slog.e(LOG_TAG, "Failed to register for fingerprint activity callbacks");
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
            return false;
        } else if (message.what == 2) {
            identity = Binder.clearCallingIdentity();
            try {
                this.mFingerprintService.removeClientActiveCallback(this);
            } catch (RemoteException e2) {
                Slog.e(LOG_TAG, "Failed to unregister for fingerprint activity callbacks");
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
            this.mRegisteredReadOnlyExceptInHandler = false;
            return true;
        } else {
            Slog.e(LOG_TAG, "Unknown message: " + message.what);
            return false;
        }
    }
}
