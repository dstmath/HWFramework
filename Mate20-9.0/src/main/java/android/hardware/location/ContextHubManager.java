package android.hardware.location;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.content.Context;
import android.hardware.location.ContextHubTransaction;
import android.hardware.location.IContextHubCallback;
import android.hardware.location.IContextHubClientCallback;
import android.hardware.location.IContextHubService;
import android.hardware.location.IContextHubTransactionCallback;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.List;
import java.util.concurrent.Executor;

@SystemApi
public final class ContextHubManager {
    private static final String TAG = "ContextHubManager";
    /* access modifiers changed from: private */
    public Callback mCallback;
    /* access modifiers changed from: private */
    public Handler mCallbackHandler;
    private final IContextHubCallback.Stub mClientCallback = new IContextHubCallback.Stub() {
        public void onMessageReceipt(int hubId, int nanoAppId, ContextHubMessage message) {
            if (ContextHubManager.this.mCallback != null) {
                synchronized (this) {
                    final Callback callback = ContextHubManager.this.mCallback;
                    Handler handler = ContextHubManager.this.mCallbackHandler == null ? new Handler(ContextHubManager.this.mMainLooper) : ContextHubManager.this.mCallbackHandler;
                    final int i = hubId;
                    final int i2 = nanoAppId;
                    final ContextHubMessage contextHubMessage = message;
                    AnonymousClass1 r1 = new Runnable() {
                        public void run() {
                            callback.onMessageReceipt(i, i2, contextHubMessage);
                        }
                    };
                    handler.post(r1);
                }
            } else if (ContextHubManager.this.mLocalCallback != null) {
                synchronized (this) {
                    ContextHubManager.this.mLocalCallback.onMessageReceipt(hubId, nanoAppId, message);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    @Deprecated
    public ICallback mLocalCallback;
    /* access modifiers changed from: private */
    public final Looper mMainLooper;
    private final IContextHubService mService;

    @Deprecated
    public static abstract class Callback {
        public abstract void onMessageReceipt(int i, int i2, ContextHubMessage contextHubMessage);

        protected Callback() {
        }
    }

    @Deprecated
    public interface ICallback {
        void onMessageReceipt(int i, int i2, ContextHubMessage contextHubMessage);
    }

    @Deprecated
    public int[] getContextHubHandles() {
        try {
            return this.mService.getContextHubHandles();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public ContextHubInfo getContextHubInfo(int hubHandle) {
        try {
            return this.mService.getContextHubInfo(hubHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public int loadNanoApp(int hubHandle, NanoApp app) {
        try {
            return this.mService.loadNanoApp(hubHandle, app);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public int unloadNanoApp(int nanoAppHandle) {
        try {
            return this.mService.unloadNanoApp(nanoAppHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public NanoAppInstanceInfo getNanoAppInstanceInfo(int nanoAppHandle) {
        try {
            return this.mService.getNanoAppInstanceInfo(nanoAppHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public int[] findNanoAppOnHub(int hubHandle, NanoAppFilter filter) {
        try {
            return this.mService.findNanoAppOnHub(hubHandle, filter);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public int sendMessage(int hubHandle, int nanoAppHandle, ContextHubMessage message) {
        try {
            return this.mService.sendMessage(hubHandle, nanoAppHandle, message);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ContextHubInfo> getContextHubs() {
        try {
            return this.mService.getContextHubs();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private IContextHubTransactionCallback createTransactionCallback(final ContextHubTransaction<Void> transaction) {
        return new IContextHubTransactionCallback.Stub() {
            public void onQueryResponse(int result, List<NanoAppState> list) {
                Log.e(ContextHubManager.TAG, "Received a query callback on a non-query request");
                transaction.setResponse(new ContextHubTransaction.Response(7, null));
            }

            public void onTransactionComplete(int result) {
                transaction.setResponse(new ContextHubTransaction.Response(result, null));
            }
        };
    }

    private IContextHubTransactionCallback createQueryCallback(final ContextHubTransaction<List<NanoAppState>> transaction) {
        return new IContextHubTransactionCallback.Stub() {
            public void onQueryResponse(int result, List<NanoAppState> nanoappList) {
                transaction.setResponse(new ContextHubTransaction.Response(result, nanoappList));
            }

            public void onTransactionComplete(int result) {
                Log.e(ContextHubManager.TAG, "Received a non-query callback on a query request");
                transaction.setResponse(new ContextHubTransaction.Response(7, null));
            }
        };
    }

    public ContextHubTransaction<Void> loadNanoApp(ContextHubInfo hubInfo, NanoAppBinary appBinary) {
        Preconditions.checkNotNull(hubInfo, "ContextHubInfo cannot be null");
        Preconditions.checkNotNull(appBinary, "NanoAppBinary cannot be null");
        ContextHubTransaction<Void> transaction = new ContextHubTransaction<>(0);
        try {
            this.mService.loadNanoAppOnHub(hubInfo.getId(), createTransactionCallback(transaction), appBinary);
            return transaction;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ContextHubTransaction<Void> unloadNanoApp(ContextHubInfo hubInfo, long nanoAppId) {
        Preconditions.checkNotNull(hubInfo, "ContextHubInfo cannot be null");
        ContextHubTransaction<Void> transaction = new ContextHubTransaction<>(1);
        try {
            this.mService.unloadNanoAppFromHub(hubInfo.getId(), createTransactionCallback(transaction), nanoAppId);
            return transaction;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ContextHubTransaction<Void> enableNanoApp(ContextHubInfo hubInfo, long nanoAppId) {
        Preconditions.checkNotNull(hubInfo, "ContextHubInfo cannot be null");
        ContextHubTransaction<Void> transaction = new ContextHubTransaction<>(2);
        try {
            this.mService.enableNanoApp(hubInfo.getId(), createTransactionCallback(transaction), nanoAppId);
            return transaction;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ContextHubTransaction<Void> disableNanoApp(ContextHubInfo hubInfo, long nanoAppId) {
        Preconditions.checkNotNull(hubInfo, "ContextHubInfo cannot be null");
        ContextHubTransaction<Void> transaction = new ContextHubTransaction<>(3);
        try {
            this.mService.disableNanoApp(hubInfo.getId(), createTransactionCallback(transaction), nanoAppId);
            return transaction;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ContextHubTransaction<List<NanoAppState>> queryNanoApps(ContextHubInfo hubInfo) {
        Preconditions.checkNotNull(hubInfo, "ContextHubInfo cannot be null");
        ContextHubTransaction<List<NanoAppState>> transaction = new ContextHubTransaction<>(4);
        try {
            this.mService.queryNanoApps(hubInfo.getId(), createQueryCallback(transaction));
            return transaction;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SuppressLint({"Doclava125"})
    @Deprecated
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
    @Deprecated
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

    private IContextHubClientCallback createClientCallback(final ContextHubClient client, final ContextHubClientCallback callback, final Executor executor) {
        return new IContextHubClientCallback.Stub() {
            public void onMessageFromNanoApp(NanoAppMessage message) {
                executor.execute(new Runnable(client, message) {
                    private final /* synthetic */ ContextHubClient f$1;
                    private final /* synthetic */ NanoAppMessage f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        ContextHubClientCallback.this.onMessageFromNanoApp(this.f$1, this.f$2);
                    }
                });
            }

            public void onHubReset() {
                executor.execute(new Runnable(client) {
                    private final /* synthetic */ ContextHubClient f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ContextHubClientCallback.this.onHubReset(this.f$1);
                    }
                });
            }

            public void onNanoAppAborted(long nanoAppId, int abortCode) {
                Executor executor = executor;
                $$Lambda$ContextHubManager$3$hASoxw9hzmd9l2NpC91O5tXLzxU r1 = new Runnable(client, nanoAppId, abortCode) {
                    private final /* synthetic */ ContextHubClient f$1;
                    private final /* synthetic */ long f$2;
                    private final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r5;
                    }

                    public final void run() {
                        ContextHubClientCallback.this.onNanoAppAborted(this.f$1, this.f$2, this.f$3);
                    }
                };
                executor.execute(r1);
            }

            public void onNanoAppLoaded(long nanoAppId) {
                executor.execute(new Runnable(client, nanoAppId) {
                    private final /* synthetic */ ContextHubClient f$1;
                    private final /* synthetic */ long f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        ContextHubClientCallback.this.onNanoAppLoaded(this.f$1, this.f$2);
                    }
                });
            }

            public void onNanoAppUnloaded(long nanoAppId) {
                executor.execute(new Runnable(client, nanoAppId) {
                    private final /* synthetic */ ContextHubClient f$1;
                    private final /* synthetic */ long f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        ContextHubClientCallback.this.onNanoAppUnloaded(this.f$1, this.f$2);
                    }
                });
            }

            public void onNanoAppEnabled(long nanoAppId) {
                executor.execute(new Runnable(client, nanoAppId) {
                    private final /* synthetic */ ContextHubClient f$1;
                    private final /* synthetic */ long f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        ContextHubClientCallback.this.onNanoAppEnabled(this.f$1, this.f$2);
                    }
                });
            }

            public void onNanoAppDisabled(long nanoAppId) {
                executor.execute(new Runnable(client, nanoAppId) {
                    private final /* synthetic */ ContextHubClient f$1;
                    private final /* synthetic */ long f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        ContextHubClientCallback.this.onNanoAppDisabled(this.f$1, this.f$2);
                    }
                });
            }
        };
    }

    public ContextHubClient createClient(ContextHubInfo hubInfo, ContextHubClientCallback callback, Executor executor) {
        Preconditions.checkNotNull(callback, "Callback cannot be null");
        Preconditions.checkNotNull(hubInfo, "ContextHubInfo cannot be null");
        Preconditions.checkNotNull(executor, "Executor cannot be null");
        ContextHubClient client = new ContextHubClient(hubInfo);
        try {
            client.setClientProxy(this.mService.createClient(createClientCallback(client, callback, executor), hubInfo.getId()));
            return client;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ContextHubClient createClient(ContextHubInfo hubInfo, ContextHubClientCallback callback) {
        return createClient(hubInfo, callback, new HandlerExecutor(Handler.getMain()));
    }

    @SuppressLint({"Doclava125"})
    @Deprecated
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

    public ContextHubManager(Context context, Looper mainLooper) throws ServiceManager.ServiceNotFoundException {
        this.mMainLooper = mainLooper;
        this.mService = IContextHubService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CONTEXTHUB_SERVICE));
        try {
            this.mService.registerCallback(this.mClientCallback);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
