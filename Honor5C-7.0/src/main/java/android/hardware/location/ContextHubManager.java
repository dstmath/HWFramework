package android.hardware.location;

import android.content.Context;
import android.hardware.location.IContextHubCallback.Stub;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public final class ContextHubManager {
    private static final String TAG = "ContextHubManager";
    private Callback mCallback;
    private Handler mCallbackHandler;
    private Stub mClientCallback;
    private IContextHubService mContextHubService;
    @Deprecated
    private ICallback mLocalCallback;
    private final Looper mMainLooper;

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
        int[] retVal = null;
        try {
            retVal = getBinder().getContextHubHandles();
        } catch (RemoteException e) {
            Log.w(TAG, "Could not fetch context hub handles : " + e);
        }
        return retVal;
    }

    public ContextHubInfo getContextHubInfo(int hubHandle) {
        ContextHubInfo retVal = null;
        try {
            retVal = getBinder().getContextHubInfo(hubHandle);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not fetch context hub info :" + e);
        }
        return retVal;
    }

    public int loadNanoApp(int hubHandle, NanoApp app) {
        int retVal = -1;
        if (app == null) {
            return retVal;
        }
        try {
            retVal = getBinder().loadNanoApp(hubHandle, app);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not load nanoApp :" + e);
        }
        return retVal;
    }

    public int unloadNanoApp(int nanoAppHandle) {
        int retVal = -1;
        try {
            retVal = getBinder().unloadNanoApp(nanoAppHandle);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not fetch unload nanoApp :" + e);
        }
        return retVal;
    }

    public NanoAppInstanceInfo getNanoAppInstanceInfo(int nanoAppHandle) {
        NanoAppInstanceInfo retVal = null;
        try {
            retVal = getBinder().getNanoAppInstanceInfo(nanoAppHandle);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not fetch nanoApp info :" + e);
        }
        return retVal;
    }

    public int[] findNanoAppOnHub(int hubHandle, NanoAppFilter filter) {
        int[] retVal = null;
        try {
            retVal = getBinder().findNanoAppOnHub(hubHandle, filter);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not query nanoApp instance :" + e);
        }
        return retVal;
    }

    public int sendMessage(int hubHandle, int nanoAppHandle, ContextHubMessage message) {
        int retVal = -1;
        if (message == null || message.getData() == null) {
            Log.w(TAG, "null ptr");
            return retVal;
        }
        try {
            retVal = getBinder().sendMessage(hubHandle, nanoAppHandle, message);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not send message :" + e.toString());
        }
        return retVal;
    }

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

    public synchronized int unregisterCallback(ICallback callback) {
        if (callback != this.mLocalCallback) {
            Log.w(TAG, "Cannot recognize local callback!");
            return -1;
        }
        this.mLocalCallback = null;
        return 0;
    }

    public ContextHubManager(Context context, Looper mainLooper) {
        this.mClientCallback = new Stub() {

            /* renamed from: android.hardware.location.ContextHubManager.1.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ Callback val$callback;
                final /* synthetic */ int val$hubId;
                final /* synthetic */ ContextHubMessage val$message;
                final /* synthetic */ int val$nanoAppId;

                AnonymousClass1(Callback val$callback, int val$hubId, int val$nanoAppId, ContextHubMessage val$message) {
                    this.val$callback = val$callback;
                    this.val$hubId = val$hubId;
                    this.val$nanoAppId = val$nanoAppId;
                    this.val$message = val$message;
                }

                public void run() {
                    this.val$callback.onMessageReceipt(this.val$hubId, this.val$nanoAppId, this.val$message);
                }
            }

            public void onMessageReceipt(int hubId, int nanoAppId, ContextHubMessage message) {
                if (ContextHubManager.this.mCallback != null) {
                    synchronized (this) {
                        (ContextHubManager.this.mCallbackHandler == null ? new Handler(ContextHubManager.this.mMainLooper) : ContextHubManager.this.mCallbackHandler).post(new AnonymousClass1(ContextHubManager.this.mCallback, hubId, nanoAppId, message));
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
        this.mMainLooper = mainLooper;
        IBinder b = ServiceManager.getService(ContextHubService.CONTEXTHUB_SERVICE);
        if (b != null) {
            this.mContextHubService = IContextHubService.Stub.asInterface(b);
            try {
                getBinder().registerCallback(this.mClientCallback);
                return;
            } catch (RemoteException e) {
                Log.w(TAG, "Could not register callback:" + e);
                return;
            }
        }
        Log.w(TAG, "failed to getService");
    }

    private IContextHubService getBinder() throws RemoteException {
        if (this.mContextHubService != null) {
            return this.mContextHubService;
        }
        throw new RemoteException("Service not connected.");
    }
}
