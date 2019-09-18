package com.android.server.location;

import android.hardware.contexthub.V1_0.IContexthub;
import android.hardware.location.IContextHubTransactionCallback;
import android.hardware.location.NanoAppBinary;
import android.hardware.location.NanoAppState;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class ContextHubTransactionManager {
    private static final int MAX_PENDING_REQUESTS = 10000;
    private static final String TAG = "ContextHubTransactionManager";
    /* access modifiers changed from: private */
    public final ContextHubClientManager mClientManager;
    /* access modifiers changed from: private */
    public final IContexthub mContextHubProxy;
    /* access modifiers changed from: private */
    public final NanoAppStateManager mNanoAppStateManager;
    private final AtomicInteger mNextAvailableId = new AtomicInteger();
    private final ScheduledThreadPoolExecutor mTimeoutExecutor = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture<?> mTimeoutFuture = null;
    private final ArrayDeque<ContextHubServiceTransaction> mTransactionQueue = new ArrayDeque<>();

    ContextHubTransactionManager(IContexthub contextHubProxy, ContextHubClientManager clientManager, NanoAppStateManager nanoAppStateManager) {
        this.mContextHubProxy = contextHubProxy;
        this.mClientManager = clientManager;
        this.mNanoAppStateManager = nanoAppStateManager;
    }

    /* access modifiers changed from: package-private */
    public ContextHubServiceTransaction createLoadTransaction(int contextHubId, NanoAppBinary nanoAppBinary, IContextHubTransactionCallback onCompleteCallback) {
        final NanoAppBinary nanoAppBinary2 = nanoAppBinary;
        final int i = contextHubId;
        final IContextHubTransactionCallback iContextHubTransactionCallback = onCompleteCallback;
        AnonymousClass1 r0 = new ContextHubServiceTransaction(this.mNextAvailableId.getAndIncrement(), 0) {
            /* access modifiers changed from: package-private */
            public int onTransact() {
                try {
                    return ContextHubTransactionManager.this.mContextHubProxy.loadNanoApp(i, ContextHubServiceUtil.createHidlNanoAppBinary(nanoAppBinary2), getTransactionId());
                } catch (RemoteException e) {
                    Log.e(ContextHubTransactionManager.TAG, "RemoteException while trying to load nanoapp with ID 0x" + Long.toHexString(nanoAppBinary2.getNanoAppId()), e);
                    return 1;
                }
            }

            /* access modifiers changed from: package-private */
            public void onTransactionComplete(int result) {
                if (result == 0) {
                    ContextHubTransactionManager.this.mNanoAppStateManager.addNanoAppInstance(i, nanoAppBinary2.getNanoAppId(), nanoAppBinary2.getNanoAppVersion());
                }
                try {
                    iContextHubTransactionCallback.onTransactionComplete(result);
                    if (result == 0) {
                        ContextHubTransactionManager.this.mClientManager.onNanoAppLoaded(i, nanoAppBinary2.getNanoAppId());
                    }
                } catch (RemoteException e) {
                    Log.e(ContextHubTransactionManager.TAG, "RemoteException while calling client onTransactionComplete", e);
                }
            }
        };
        return r0;
    }

    /* access modifiers changed from: package-private */
    public ContextHubServiceTransaction createUnloadTransaction(int contextHubId, long nanoAppId, IContextHubTransactionCallback onCompleteCallback) {
        final int i = contextHubId;
        final long j = nanoAppId;
        final IContextHubTransactionCallback iContextHubTransactionCallback = onCompleteCallback;
        AnonymousClass2 r0 = new ContextHubServiceTransaction(this.mNextAvailableId.getAndIncrement(), 1) {
            /* access modifiers changed from: package-private */
            public int onTransact() {
                try {
                    return ContextHubTransactionManager.this.mContextHubProxy.unloadNanoApp(i, j, getTransactionId());
                } catch (RemoteException e) {
                    Log.e(ContextHubTransactionManager.TAG, "RemoteException while trying to unload nanoapp with ID 0x" + Long.toHexString(j), e);
                    return 1;
                }
            }

            /* access modifiers changed from: package-private */
            public void onTransactionComplete(int result) {
                if (result == 0) {
                    ContextHubTransactionManager.this.mNanoAppStateManager.removeNanoAppInstance(i, j);
                }
                try {
                    iContextHubTransactionCallback.onTransactionComplete(result);
                    if (result == 0) {
                        ContextHubTransactionManager.this.mClientManager.onNanoAppUnloaded(i, j);
                    }
                } catch (RemoteException e) {
                    Log.e(ContextHubTransactionManager.TAG, "RemoteException while calling client onTransactionComplete", e);
                }
            }
        };
        return r0;
    }

    /* access modifiers changed from: package-private */
    public ContextHubServiceTransaction createEnableTransaction(int contextHubId, long nanoAppId, IContextHubTransactionCallback onCompleteCallback) {
        final int i = contextHubId;
        final long j = nanoAppId;
        final IContextHubTransactionCallback iContextHubTransactionCallback = onCompleteCallback;
        AnonymousClass3 r0 = new ContextHubServiceTransaction(this.mNextAvailableId.getAndIncrement(), 2) {
            /* access modifiers changed from: package-private */
            public int onTransact() {
                try {
                    return ContextHubTransactionManager.this.mContextHubProxy.enableNanoApp(i, j, getTransactionId());
                } catch (RemoteException e) {
                    Log.e(ContextHubTransactionManager.TAG, "RemoteException while trying to enable nanoapp with ID 0x" + Long.toHexString(j), e);
                    return 1;
                }
            }

            /* access modifiers changed from: package-private */
            public void onTransactionComplete(int result) {
                try {
                    iContextHubTransactionCallback.onTransactionComplete(result);
                } catch (RemoteException e) {
                    Log.e(ContextHubTransactionManager.TAG, "RemoteException while calling client onTransactionComplete", e);
                }
            }
        };
        return r0;
    }

    /* access modifiers changed from: package-private */
    public ContextHubServiceTransaction createDisableTransaction(int contextHubId, long nanoAppId, IContextHubTransactionCallback onCompleteCallback) {
        final int i = contextHubId;
        final long j = nanoAppId;
        final IContextHubTransactionCallback iContextHubTransactionCallback = onCompleteCallback;
        AnonymousClass4 r0 = new ContextHubServiceTransaction(this.mNextAvailableId.getAndIncrement(), 3) {
            /* access modifiers changed from: package-private */
            public int onTransact() {
                try {
                    return ContextHubTransactionManager.this.mContextHubProxy.disableNanoApp(i, j, getTransactionId());
                } catch (RemoteException e) {
                    Log.e(ContextHubTransactionManager.TAG, "RemoteException while trying to disable nanoapp with ID 0x" + Long.toHexString(j), e);
                    return 1;
                }
            }

            /* access modifiers changed from: package-private */
            public void onTransactionComplete(int result) {
                try {
                    iContextHubTransactionCallback.onTransactionComplete(result);
                } catch (RemoteException e) {
                    Log.e(ContextHubTransactionManager.TAG, "RemoteException while calling client onTransactionComplete", e);
                }
            }
        };
        return r0;
    }

    /* access modifiers changed from: package-private */
    public ContextHubServiceTransaction createQueryTransaction(int contextHubId, IContextHubTransactionCallback onCompleteCallback) {
        final int i = contextHubId;
        final IContextHubTransactionCallback iContextHubTransactionCallback = onCompleteCallback;
        AnonymousClass5 r0 = new ContextHubServiceTransaction(this.mNextAvailableId.getAndIncrement(), 4) {
            /* access modifiers changed from: package-private */
            public int onTransact() {
                try {
                    return ContextHubTransactionManager.this.mContextHubProxy.queryApps(i);
                } catch (RemoteException e) {
                    Log.e(ContextHubTransactionManager.TAG, "RemoteException while trying to query for nanoapps", e);
                    return 1;
                }
            }

            /* access modifiers changed from: package-private */
            public void onTransactionComplete(int result) {
                onQueryResponse(result, Collections.emptyList());
            }

            /* access modifiers changed from: package-private */
            public void onQueryResponse(int result, List<NanoAppState> nanoAppStateList) {
                try {
                    iContextHubTransactionCallback.onQueryResponse(result, nanoAppStateList);
                } catch (RemoteException e) {
                    Log.e(ContextHubTransactionManager.TAG, "RemoteException while calling client onQueryComplete", e);
                }
            }
        };
        return r0;
    }

    /* access modifiers changed from: package-private */
    public synchronized void addTransaction(ContextHubServiceTransaction transaction) throws IllegalStateException {
        if (this.mTransactionQueue.size() != 10000) {
            this.mTransactionQueue.add(transaction);
            if (this.mTransactionQueue.size() == 1) {
                startNextTransaction();
            }
        } else {
            throw new IllegalStateException("Transaction queue is full (capacity = 10000)");
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void onTransactionResponse(int transactionId, int result) {
        int i;
        ContextHubServiceTransaction transaction = this.mTransactionQueue.peek();
        if (transaction == null) {
            Log.w(TAG, "Received unexpected transaction response (no transaction pending)");
        } else if (transaction.getTransactionId() != transactionId) {
            Log.w(TAG, "Received unexpected transaction response (expected ID = " + transaction.getTransactionId() + ", received ID = " + transactionId + ")");
        } else {
            if (result == 0) {
                i = 0;
            } else {
                i = 5;
            }
            transaction.onTransactionComplete(i);
            removeTransactionAndStartNext();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void onQueryResponse(List<NanoAppState> nanoAppStateList) {
        ContextHubServiceTransaction transaction = this.mTransactionQueue.peek();
        if (transaction == null) {
            Log.w(TAG, "Received unexpected query response (no transaction pending)");
        } else if (transaction.getTransactionType() != 4) {
            Log.w(TAG, "Received unexpected query response (expected " + transaction + ")");
        } else {
            transaction.onQueryResponse(0, nanoAppStateList);
            removeTransactionAndStartNext();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void onHubReset() {
        if (this.mTransactionQueue.peek() != null) {
            removeTransactionAndStartNext();
        }
    }

    private void removeTransactionAndStartNext() {
        this.mTimeoutFuture.cancel(false);
        this.mTransactionQueue.remove().setComplete();
        if (!this.mTransactionQueue.isEmpty()) {
            startNextTransaction();
        }
    }

    private void startNextTransaction() {
        int result = 1;
        while (result != 0 && !this.mTransactionQueue.isEmpty()) {
            ContextHubServiceTransaction transaction = this.mTransactionQueue.peek();
            result = transaction.onTransact();
            if (result == 0) {
                this.mTimeoutFuture = this.mTimeoutExecutor.schedule(new Runnable(transaction) {
                    private final /* synthetic */ ContextHubServiceTransaction f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ContextHubTransactionManager.lambda$startNextTransaction$0(ContextHubTransactionManager.this, this.f$1);
                    }
                }, transaction.getTimeout(TimeUnit.SECONDS), TimeUnit.SECONDS);
            } else {
                transaction.onTransactionComplete(ContextHubServiceUtil.toTransactionResult(result));
                this.mTransactionQueue.remove();
            }
        }
    }

    public static /* synthetic */ void lambda$startNextTransaction$0(ContextHubTransactionManager contextHubTransactionManager, ContextHubServiceTransaction transaction) {
        synchronized (contextHubTransactionManager) {
            if (!transaction.isComplete()) {
                Log.d(TAG, transaction + " timed out");
                transaction.onTransactionComplete(6);
                contextHubTransactionManager.removeTransactionAndStartNext();
            }
        }
    }
}
