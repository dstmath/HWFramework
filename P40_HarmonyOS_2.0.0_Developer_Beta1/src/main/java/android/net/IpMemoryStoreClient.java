package android.net;

import android.content.Context;
import android.net.ipmemorystore.Blob;
import android.net.ipmemorystore.NetworkAttributes;
import android.net.ipmemorystore.OnBlobRetrievedListener;
import android.net.ipmemorystore.OnL2KeyResponseListener;
import android.net.ipmemorystore.OnNetworkAttributesRetrievedListener;
import android.net.ipmemorystore.OnSameL3NetworkResponseListener;
import android.net.ipmemorystore.OnStatusListener;
import android.net.ipmemorystore.Status;
import android.os.RemoteException;
import android.util.Log;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public abstract class IpMemoryStoreClient {
    private static final String TAG = IpMemoryStoreClient.class.getSimpleName();
    private final Context mContext;

    public interface ThrowingRunnable {
        void run() throws RemoteException;
    }

    /* access modifiers changed from: protected */
    public abstract void runWhenServiceReady(Consumer<IIpMemoryStore> consumer) throws ExecutionException;

    public IpMemoryStoreClient(Context context) {
        if (context != null) {
            this.mContext = context;
            return;
        }
        throw new IllegalArgumentException("missing context");
    }

    private void ignoringRemoteException(ThrowingRunnable r) {
        ignoringRemoteException("Failed to execute remote procedure call", r);
    }

    private void ignoringRemoteException(String message, ThrowingRunnable r) {
        try {
            r.run();
        } catch (RemoteException e) {
            Log.e(TAG, message, e);
        }
    }

    public /* synthetic */ void lambda$storeNetworkAttributes$1$IpMemoryStoreClient(String l2Key, NetworkAttributes attributes, OnStatusListener listener, IIpMemoryStore service) {
        ignoringRemoteException(new ThrowingRunnable(l2Key, attributes, listener) {
            /* class android.net.$$Lambda$IpMemoryStoreClient$4LLLcxcDI48Nnc_rkm7mdSQsa2U */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ NetworkAttributes f$2;
            private final /* synthetic */ OnStatusListener f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
            public final void run() {
                IpMemoryStoreClient.lambda$storeNetworkAttributes$0(IIpMemoryStore.this, this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public void storeNetworkAttributes(String l2Key, NetworkAttributes attributes, OnStatusListener listener) {
        try {
            runWhenServiceReady(new Consumer(l2Key, attributes, listener) {
                /* class android.net.$$Lambda$IpMemoryStoreClient$0LhXdcPG7yJtV5UggjyJkRbARKU */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ NetworkAttributes f$2;
                private final /* synthetic */ OnStatusListener f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    IpMemoryStoreClient.this.lambda$storeNetworkAttributes$1$IpMemoryStoreClient(this.f$1, this.f$2, this.f$3, (IIpMemoryStore) obj);
                }
            });
        } catch (ExecutionException e) {
            ignoringRemoteException("Error storing network attributes", new ThrowingRunnable() {
                /* class android.net.$$Lambda$IpMemoryStoreClient$FjB7dm6lAwZ6pH1lqvrhxtLFOm8 */

                @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
                public final void run() {
                    IpMemoryStoreClient.lambda$storeNetworkAttributes$2(OnStatusListener.this);
                }
            });
        }
    }

    static /* synthetic */ void lambda$storeNetworkAttributes$0(IIpMemoryStore service, String l2Key, NetworkAttributes attributes, OnStatusListener listener) {
        service.storeNetworkAttributes(l2Key, attributes.toParcelable(), OnStatusListener.toAIDL(listener));
    }

    static /* synthetic */ void lambda$storeNetworkAttributes$2(OnStatusListener listener) {
        listener.onComplete(new Status(-5));
    }

    public /* synthetic */ void lambda$storeBlob$4$IpMemoryStoreClient(String l2Key, String clientId, String name, Blob data, OnStatusListener listener, IIpMemoryStore service) {
        ignoringRemoteException(new ThrowingRunnable(l2Key, clientId, name, data, listener) {
            /* class android.net.$$Lambda$IpMemoryStoreClient$4eqTtDGA25PNMyU_1yqQCF2gOo */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ String f$3;
            private final /* synthetic */ Blob f$4;
            private final /* synthetic */ OnStatusListener f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
            public final void run() {
                IpMemoryStoreClient.lambda$storeBlob$3(IIpMemoryStore.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        });
    }

    public void storeBlob(String l2Key, String clientId, String name, Blob data, OnStatusListener listener) {
        try {
            runWhenServiceReady(new Consumer(l2Key, clientId, name, data, listener) {
                /* class android.net.$$Lambda$IpMemoryStoreClient$OI4Zw2djhZoG0D4IE2ujC0Iv6G4 */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ String f$2;
                private final /* synthetic */ String f$3;
                private final /* synthetic */ Blob f$4;
                private final /* synthetic */ OnStatusListener f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    IpMemoryStoreClient.this.lambda$storeBlob$4$IpMemoryStoreClient(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, (IIpMemoryStore) obj);
                }
            });
        } catch (ExecutionException e) {
            ignoringRemoteException("Error storing blob", new ThrowingRunnable() {
                /* class android.net.$$Lambda$IpMemoryStoreClient$Rs7okZ0ViR35WkNSGbyhqEXxJxc */

                @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
                public final void run() {
                    IpMemoryStoreClient.lambda$storeBlob$5(OnStatusListener.this);
                }
            });
        }
    }

    static /* synthetic */ void lambda$storeBlob$3(IIpMemoryStore service, String l2Key, String clientId, String name, Blob data, OnStatusListener listener) {
        service.storeBlob(l2Key, clientId, name, data, OnStatusListener.toAIDL(listener));
    }

    static /* synthetic */ void lambda$storeBlob$5(OnStatusListener listener) {
        listener.onComplete(new Status(-5));
    }

    public void findL2Key(NetworkAttributes attributes, OnL2KeyResponseListener listener) {
        try {
            runWhenServiceReady(new Consumer(attributes, listener) {
                /* class android.net.$$Lambda$IpMemoryStoreClient$uI7nYxd7GfJucRXO9KcNTbbWOlc */
                private final /* synthetic */ NetworkAttributes f$1;
                private final /* synthetic */ OnL2KeyResponseListener f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    IpMemoryStoreClient.this.lambda$findL2Key$7$IpMemoryStoreClient(this.f$1, this.f$2, (IIpMemoryStore) obj);
                }
            });
        } catch (ExecutionException e) {
            ignoringRemoteException("Error finding L2 Key", new ThrowingRunnable() {
                /* class android.net.$$Lambda$IpMemoryStoreClient$xx1upXTRGTVc0003KEhaxwIwwN8 */

                @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
                public final void run() {
                    OnL2KeyResponseListener.this.onL2KeyResponse(new Status(-5), null);
                }
            });
        }
    }

    public /* synthetic */ void lambda$findL2Key$7$IpMemoryStoreClient(NetworkAttributes attributes, OnL2KeyResponseListener listener, IIpMemoryStore service) {
        ignoringRemoteException(new ThrowingRunnable(attributes, listener) {
            /* class android.net.$$Lambda$IpMemoryStoreClient$2bQLFhsJeYf5bkZg091OSOTEJY */
            private final /* synthetic */ NetworkAttributes f$1;
            private final /* synthetic */ OnL2KeyResponseListener f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
            public final void run() {
                IpMemoryStoreClient.lambda$findL2Key$6(IIpMemoryStore.this, this.f$1, this.f$2);
            }
        });
    }

    public void isSameNetwork(String l2Key1, String l2Key2, OnSameL3NetworkResponseListener listener) {
        try {
            runWhenServiceReady(new Consumer(l2Key1, l2Key2, listener) {
                /* class android.net.$$Lambda$IpMemoryStoreClient$uHUebZ3pZ1jD5N1tZNdyTy8zBCE */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ String f$2;
                private final /* synthetic */ OnSameL3NetworkResponseListener f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    IpMemoryStoreClient.this.lambda$isSameNetwork$10$IpMemoryStoreClient(this.f$1, this.f$2, this.f$3, (IIpMemoryStore) obj);
                }
            });
        } catch (ExecutionException e) {
            ignoringRemoteException("Error checking for network sameness", new ThrowingRunnable() {
                /* class android.net.$$Lambda$IpMemoryStoreClient$V28n1xp79cKTZf0npSvzf7FUo8 */

                @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
                public final void run() {
                    OnSameL3NetworkResponseListener.this.onSameL3NetworkResponse(new Status(-5), null);
                }
            });
        }
    }

    public /* synthetic */ void lambda$isSameNetwork$10$IpMemoryStoreClient(String l2Key1, String l2Key2, OnSameL3NetworkResponseListener listener, IIpMemoryStore service) {
        ignoringRemoteException(new ThrowingRunnable(l2Key1, l2Key2, listener) {
            /* class android.net.$$Lambda$IpMemoryStoreClient$A2hOjZriLOXFq3Aij0wHaYZQOSc */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ OnSameL3NetworkResponseListener f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
            public final void run() {
                IpMemoryStoreClient.lambda$isSameNetwork$9(IIpMemoryStore.this, this.f$1, this.f$2, this.f$3);
            }
        });
    }

    static /* synthetic */ void lambda$isSameNetwork$9(IIpMemoryStore service, String l2Key1, String l2Key2, OnSameL3NetworkResponseListener listener) {
        service.isSameNetwork(l2Key1, l2Key2, OnSameL3NetworkResponseListener.toAIDL(listener));
    }

    public /* synthetic */ void lambda$retrieveNetworkAttributes$13$IpMemoryStoreClient(String l2Key, OnNetworkAttributesRetrievedListener listener, IIpMemoryStore service) {
        ignoringRemoteException(new ThrowingRunnable(l2Key, listener) {
            /* class android.net.$$Lambda$IpMemoryStoreClient$Uc0QFR5a_MhzwuvUoWpz73NAAEs */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ OnNetworkAttributesRetrievedListener f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
            public final void run() {
                IIpMemoryStore.this.retrieveNetworkAttributes(this.f$1, OnNetworkAttributesRetrievedListener.toAIDL(this.f$2));
            }
        });
    }

    public void retrieveNetworkAttributes(String l2Key, OnNetworkAttributesRetrievedListener listener) {
        try {
            runWhenServiceReady(new Consumer(l2Key, listener) {
                /* class android.net.$$Lambda$IpMemoryStoreClient$OnrcybvxwSrQUBY_VqGsD_5lQfI */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ OnNetworkAttributesRetrievedListener f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    IpMemoryStoreClient.this.lambda$retrieveNetworkAttributes$13$IpMemoryStoreClient(this.f$1, this.f$2, (IIpMemoryStore) obj);
                }
            });
        } catch (ExecutionException e) {
            ignoringRemoteException("Error retrieving network attributes", new ThrowingRunnable() {
                /* class android.net.$$Lambda$IpMemoryStoreClient$JTvBo0T3ntOmEDS60qZyBJUlJio */

                @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
                public final void run() {
                    IpMemoryStoreClient.lambda$retrieveNetworkAttributes$14(OnNetworkAttributesRetrievedListener.this);
                }
            });
        }
    }

    static /* synthetic */ void lambda$retrieveNetworkAttributes$14(OnNetworkAttributesRetrievedListener listener) {
        listener.onNetworkAttributesRetrieved(new Status(-5), null, null);
    }

    public /* synthetic */ void lambda$retrieveBlob$16$IpMemoryStoreClient(String l2Key, String clientId, String name, OnBlobRetrievedListener listener, IIpMemoryStore service) {
        ignoringRemoteException(new ThrowingRunnable(l2Key, clientId, name, listener) {
            /* class android.net.$$Lambda$IpMemoryStoreClient$284VFgqq7BBkkwVNFLIrF3c59Es */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ String f$3;
            private final /* synthetic */ OnBlobRetrievedListener f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
            public final void run() {
                IpMemoryStoreClient.lambda$retrieveBlob$15(IIpMemoryStore.this, this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public void retrieveBlob(String l2Key, String clientId, String name, OnBlobRetrievedListener listener) {
        try {
            runWhenServiceReady(new Consumer(l2Key, clientId, name, listener) {
                /* class android.net.$$Lambda$IpMemoryStoreClient$3VeddAdCuqfXquVC2DlGvI3eVPM */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ String f$2;
                private final /* synthetic */ String f$3;
                private final /* synthetic */ OnBlobRetrievedListener f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    IpMemoryStoreClient.this.lambda$retrieveBlob$16$IpMemoryStoreClient(this.f$1, this.f$2, this.f$3, this.f$4, (IIpMemoryStore) obj);
                }
            });
        } catch (ExecutionException e) {
            ignoringRemoteException("Error retrieving blob", new ThrowingRunnable() {
                /* class android.net.$$Lambda$IpMemoryStoreClient$hPxhgsDi3PN7OFwwZBxGXYZTs */

                @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
                public final void run() {
                    OnBlobRetrievedListener.this.onBlobRetrieved(new Status(-5), null, null, null);
                }
            });
        }
    }

    public void factoryReset() {
        try {
            runWhenServiceReady(new Consumer() {
                /* class android.net.$$Lambda$IpMemoryStoreClient$jkIRcIYXFgUFqiYMobpnQ9tj1vc */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    IpMemoryStoreClient.this.lambda$factoryReset$19$IpMemoryStoreClient((IIpMemoryStore) obj);
                }
            });
        } catch (ExecutionException m) {
            Log.e(TAG, "Error executing factory reset", m);
        }
    }

    public /* synthetic */ void lambda$factoryReset$19$IpMemoryStoreClient(IIpMemoryStore service) {
        ignoringRemoteException(new ThrowingRunnable() {
            /* class android.net.$$Lambda$IpMemoryStoreClient$y9CML5H8l7LhlZfPXBMWllilSs */

            @Override // android.net.IpMemoryStoreClient.ThrowingRunnable
            public final void run() {
                IIpMemoryStore.this.factoryReset();
            }
        });
    }
}
