package android.hardware.location;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.PendingIntent;
import android.content.Context;
import android.hardware.location.ContextHubManager;
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;

@SystemApi
public final class ContextHubManager {
    public static final int EVENT_HUB_RESET = 6;
    public static final int EVENT_NANOAPP_ABORTED = 4;
    public static final int EVENT_NANOAPP_DISABLED = 3;
    public static final int EVENT_NANOAPP_ENABLED = 2;
    public static final int EVENT_NANOAPP_LOADED = 0;
    public static final int EVENT_NANOAPP_MESSAGE = 5;
    public static final int EVENT_NANOAPP_UNLOADED = 1;
    public static final String EXTRA_CONTEXT_HUB_INFO = "android.hardware.location.extra.CONTEXT_HUB_INFO";
    public static final String EXTRA_EVENT_TYPE = "android.hardware.location.extra.EVENT_TYPE";
    public static final String EXTRA_MESSAGE = "android.hardware.location.extra.MESSAGE";
    public static final String EXTRA_NANOAPP_ABORT_CODE = "android.hardware.location.extra.NANOAPP_ABORT_CODE";
    public static final String EXTRA_NANOAPP_ID = "android.hardware.location.extra.NANOAPP_ID";
    private static final String TAG = "ContextHubManager";
    private Callback mCallback;
    private Handler mCallbackHandler;
    private final IContextHubCallback.Stub mClientCallback = new IContextHubCallback.Stub() {
        /* class android.hardware.location.ContextHubManager.AnonymousClass4 */

        @Override // android.hardware.location.IContextHubCallback
        public void onMessageReceipt(int hubId, int nanoAppId, ContextHubMessage message) {
            synchronized (ContextHubManager.this) {
                if (ContextHubManager.this.mCallback != null) {
                    ContextHubManager.this.mCallbackHandler.post(new Runnable(hubId, nanoAppId, message) {
                        /* class android.hardware.location.$$Lambda$ContextHubManager$4$sylEfC1Rx_cxuQRnKuthZXmV8KI */
                        private final /* synthetic */ int f$1;
                        private final /* synthetic */ int f$2;
                        private final /* synthetic */ ContextHubMessage f$3;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                        }

                        public final void run() {
                            ContextHubManager.AnonymousClass4.this.lambda$onMessageReceipt$0$ContextHubManager$4(this.f$1, this.f$2, this.f$3);
                        }
                    });
                } else if (ContextHubManager.this.mLocalCallback != null) {
                    ContextHubManager.this.mLocalCallback.onMessageReceipt(hubId, nanoAppId, message);
                }
            }
        }

        public /* synthetic */ void lambda$onMessageReceipt$0$ContextHubManager$4(int hubId, int nanoAppId, ContextHubMessage message) {
            ContextHubManager.this.invokeOnMessageReceiptCallback(hubId, nanoAppId, message);
        }
    };
    @Deprecated
    private ICallback mLocalCallback;
    private final Looper mMainLooper;
    private final IContextHubService mService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Event {
    }

    @Deprecated
    public interface ICallback {
        void onMessageReceipt(int i, int i2, ContextHubMessage contextHubMessage);
    }

    @Deprecated
    public static abstract class Callback {
        public abstract void onMessageReceipt(int i, int i2, ContextHubMessage contextHubMessage);

        protected Callback() {
        }
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
            /* class android.hardware.location.ContextHubManager.AnonymousClass1 */

            @Override // android.hardware.location.IContextHubTransactionCallback
            public void onQueryResponse(int result, List<NanoAppState> list) {
                Log.e(ContextHubManager.TAG, "Received a query callback on a non-query request");
                transaction.setResponse(new ContextHubTransaction.Response(7, null));
            }

            @Override // android.hardware.location.IContextHubTransactionCallback
            public void onTransactionComplete(int result) {
                transaction.setResponse(new ContextHubTransaction.Response(result, null));
            }
        };
    }

    private IContextHubTransactionCallback createQueryCallback(final ContextHubTransaction<List<NanoAppState>> transaction) {
        return new IContextHubTransactionCallback.Stub() {
            /* class android.hardware.location.ContextHubManager.AnonymousClass2 */

            @Override // android.hardware.location.IContextHubTransactionCallback
            public void onQueryResponse(int result, List<NanoAppState> nanoappList) {
                transaction.setResponse(new ContextHubTransaction.Response(result, nanoappList));
            }

            @Override // android.hardware.location.IContextHubTransactionCallback
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
            this.mCallbackHandler = handler == null ? new Handler(this.mMainLooper) : handler;
            return 0;
        }
    }

    private IContextHubClientCallback createClientCallback(final ContextHubClient client, final ContextHubClientCallback callback, final Executor executor) {
        return new IContextHubClientCallback.Stub() {
            /* class android.hardware.location.ContextHubManager.AnonymousClass3 */

            @Override // android.hardware.location.IContextHubClientCallback
            public void onMessageFromNanoApp(NanoAppMessage message) {
                executor.execute(new Runnable(client, message) {
                    /* class android.hardware.location.$$Lambda$ContextHubManager$3$U9x_HK_GdADIEQ3mS5mDWMNWMu8 */
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

            @Override // android.hardware.location.IContextHubClientCallback
            public void onHubReset() {
                executor.execute(new Runnable(client) {
                    /* class android.hardware.location.$$Lambda$ContextHubManager$3$kLhhBRChCeue1LKohd5lK_lfKTU */
                    private final /* synthetic */ ContextHubClient f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ContextHubClientCallback.this.onHubReset(this.f$1);
                    }
                });
            }

            @Override // android.hardware.location.IContextHubClientCallback
            public void onNanoAppAborted(long nanoAppId, int abortCode) {
                executor.execute(new Runnable(client, nanoAppId, abortCode) {
                    /* class android.hardware.location.$$Lambda$ContextHubManager$3$hASoxw9hzmd9l2NpC91O5tXLzxU */
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
                });
            }

            @Override // android.hardware.location.IContextHubClientCallback
            public void onNanoAppLoaded(long nanoAppId) {
                executor.execute(new Runnable(client, nanoAppId) {
                    /* class android.hardware.location.$$Lambda$ContextHubManager$3$5yx25kUuvL9qy3uBcIzI3sQQoL8 */
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

            @Override // android.hardware.location.IContextHubClientCallback
            public void onNanoAppUnloaded(long nanoAppId) {
                executor.execute(new Runnable(client, nanoAppId) {
                    /* class android.hardware.location.$$Lambda$ContextHubManager$3$KgVQePwT_QpjU9EQTp2L3LsHE5Y */
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

            @Override // android.hardware.location.IContextHubClientCallback
            public void onNanoAppEnabled(long nanoAppId) {
                executor.execute(new Runnable(client, nanoAppId) {
                    /* class android.hardware.location.$$Lambda$ContextHubManager$3$8oeFzBAC_VuH1d32Kod8BVn0Os8 */
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

            @Override // android.hardware.location.IContextHubClientCallback
            public void onNanoAppDisabled(long nanoAppId) {
                executor.execute(new Runnable(client, nanoAppId) {
                    /* class android.hardware.location.$$Lambda$ContextHubManager$3$On2Q5Obzm4zLY0UP3Xs4E3PV0 */
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
        ContextHubClient client = new ContextHubClient(hubInfo, false);
        try {
            client.setClientProxy(this.mService.createClient(hubInfo.getId(), createClientCallback(client, callback, executor)));
            return client;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ContextHubClient createClient(ContextHubInfo hubInfo, ContextHubClientCallback callback) {
        return createClient(hubInfo, callback, new HandlerExecutor(Handler.getMain()));
    }

    public ContextHubClient createClient(ContextHubInfo hubInfo, PendingIntent pendingIntent, long nanoAppId) {
        Preconditions.checkNotNull(pendingIntent);
        Preconditions.checkNotNull(hubInfo);
        ContextHubClient client = new ContextHubClient(hubInfo, true);
        try {
            client.setClientProxy(this.mService.createPendingIntentClient(hubInfo.getId(), pendingIntent, nanoAppId));
            return client;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void invokeOnMessageReceiptCallback(int hubId, int nanoAppId, ContextHubMessage message) {
        if (this.mCallback != null) {
            this.mCallback.onMessageReceipt(hubId, nanoAppId, message);
        }
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
