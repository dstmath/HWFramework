package android.hardware.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.location.IContextHubCallback.Stub;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceManager.ServiceNotFoundException;
import android.util.Log;

public final class ContextHubManager {
    private static final String TAG = "ContextHubManager";
    private Callback mCallback;
    private Handler mCallbackHandler;
    private final Stub mClientCallback = new Stub() {
        public void onMessageReceipt(int hubId, int nanoAppId, ContextHubMessage message) {
            if (ContextHubManager.this.mCallback != null) {
                synchronized (this) {
                    final Callback callback = ContextHubManager.this.mCallback;
                    final int i = hubId;
                    final int i2 = nanoAppId;
                    final ContextHubMessage contextHubMessage = message;
                    (ContextHubManager.this.mCallbackHandler == null ? new Handler(ContextHubManager.this.mMainLooper) : ContextHubManager.this.mCallbackHandler).post(new Runnable() {
                        public void run() {
                            callback.onMessageReceipt(i, i2, contextHubMessage);
                        }
                    });
                }
            } else if (ContextHubManager.this.mLocalCallback != null) {
                synchronized (this) {
                    ContextHubManager.this.mLocalCallback.onMessageReceipt(hubId, nanoAppId, message);
                }
            } else {
                Log.d(ContextHubManager.TAG, "Context hub manager client callback is NULL");
                return;
            }
        }
    };
    @Deprecated
    private ICallback mLocalCallback;
    private final Looper mMainLooper;
    private final IContextHubService mService;

    public static abstract class Callback {
        public abstract void onMessageReceipt(int i, int i2, ContextHubMessage contextHubMessage);

        protected Callback() {
        }
    }

    @Deprecated
    public interface ICallback {
        void onMessageReceipt(int i, int i2, ContextHubMessage contextHubMessage);
    }

    public int[] getContextHubHandles() {
        try {
            return this.mService.getContextHubHandles();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ContextHubInfo getContextHubInfo(int hubHandle) {
        try {
            return this.mService.getContextHubInfo(hubHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int loadNanoApp(int hubHandle, NanoApp app) {
        try {
            return this.mService.loadNanoApp(hubHandle, app);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int unloadNanoApp(int nanoAppHandle) {
        try {
            return this.mService.unloadNanoApp(nanoAppHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public NanoAppInstanceInfo getNanoAppInstanceInfo(int nanoAppHandle) {
        try {
            return this.mService.getNanoAppInstanceInfo(nanoAppHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int[] findNanoAppOnHub(int hubHandle, NanoAppFilter filter) {
        try {
            return this.mService.findNanoAppOnHub(hubHandle, filter);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int sendMessage(int hubHandle, int nanoAppHandle, ContextHubMessage message) {
        try {
            return this.mService.sendMessage(hubHandle, nanoAppHandle, message);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SuppressLint({"Doclava125"})
    public int registerCallback(Callback callback) {
        return registerCallback(callback, null);
    }

    @Deprecated
    public int registerCallback(ICallback callback) {
        if (this.mLocalCallback != null) {
            Log.w(TAG, "Max number of local callbacks reached!");
            return -1;
        }
        this.mLocalCallback = callback;
        return 0;
    }

    @SuppressLint({"Doclava125"})
    public int registerCallback(Callback callback, Handler handler) {
        synchronized (this) {
            if (this.mCallback != null) {
                Log.w(TAG, "Max number of callbacks reached!");
                return -1;
            }
            this.mCallback = callback;
            this.mCallbackHandler = handler;
            return 0;
        }
    }

    @SuppressLint({"Doclava125"})
    public int unregisterCallback(Callback callback) {
        synchronized (this) {
            if (callback != this.mCallback) {
                Log.w(TAG, "Cannot recognize callback!");
                return -1;
            }
            this.mCallback = null;
            this.mCallbackHandler = null;
            return 0;
        }
    }

    @Deprecated
    public synchronized int unregisterCallback(ICallback callback) {
        if (callback != this.mLocalCallback) {
            Log.w(TAG, "Cannot recognize local callback!");
            return -1;
        }
        this.mLocalCallback = null;
        return 0;
    }

    public ContextHubManager(Context context, Looper mainLooper) throws ServiceNotFoundException {
        this.mMainLooper = mainLooper;
        this.mService = IContextHubService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CONTEXTHUB_SERVICE));
        try {
            this.mService.registerCallback(this.mClientCallback);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not register callback:" + e);
        }
    }
}
