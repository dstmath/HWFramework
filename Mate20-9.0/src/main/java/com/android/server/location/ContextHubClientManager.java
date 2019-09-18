package com.android.server.location;

import android.content.Context;
import android.hardware.contexthub.V1_0.ContextHubMsg;
import android.hardware.contexthub.V1_0.IContexthub;
import android.hardware.location.IContextHubClient;
import android.hardware.location.IContextHubClientCallback;
import android.hardware.location.NanoAppMessage;
import android.os.RemoteException;
import android.util.Log;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

class ContextHubClientManager {
    private static final boolean DEBUG_LOG_ENABLED = true;
    private static final int MAX_CLIENT_ID = 32767;
    private static final String TAG = "ContextHubClientManager";
    private final Context mContext;
    private final IContexthub mContextHubProxy;
    private final ConcurrentHashMap<Short, ContextHubClientBroker> mHostEndPointIdToClientMap = new ConcurrentHashMap<>();
    private int mNextHostEndpointId = 0;

    ContextHubClientManager(Context context, IContexthub contextHubProxy) {
        this.mContext = context;
        this.mContextHubProxy = contextHubProxy;
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.location.ContextHubClientBroker, android.os.IBinder] */
    /* access modifiers changed from: package-private */
    public IContextHubClient registerClient(IContextHubClientCallback clientCallback, int contextHubId) {
        ? createNewClientBroker = createNewClientBroker(clientCallback, contextHubId);
        try {
            createNewClientBroker.attachDeathRecipient();
            Log.d(TAG, "Registered client with host endpoint ID " + createNewClientBroker.getHostEndPointId());
            return IContextHubClient.Stub.asInterface(createNewClientBroker);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to attach death recipient to client");
            createNewClientBroker.close();
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void onMessageFromNanoApp(int contextHubId, ContextHubMsg message) {
        NanoAppMessage clientMessage = ContextHubServiceUtil.createNanoAppMessage(message);
        Log.v(TAG, "Received " + clientMessage);
        if (clientMessage.isBroadcastMessage()) {
            broadcastMessage(contextHubId, clientMessage);
            return;
        }
        ContextHubClientBroker proxy = this.mHostEndPointIdToClientMap.get(Short.valueOf(message.hostEndPoint));
        if (proxy != null) {
            proxy.sendMessageToClient(clientMessage);
            return;
        }
        Log.e(TAG, "Cannot send message to unregistered client (host endpoint ID = " + message.hostEndPoint + ")");
    }

    /* access modifiers changed from: package-private */
    public void unregisterClient(short hostEndPointId) {
        if (this.mHostEndPointIdToClientMap.remove(Short.valueOf(hostEndPointId)) != null) {
            Log.d(TAG, "Unregistered client with host endpoint ID " + hostEndPointId);
            return;
        }
        Log.e(TAG, "Cannot unregister non-existing client with host endpoint ID " + hostEndPointId);
    }

    /* access modifiers changed from: package-private */
    public void onNanoAppLoaded(int contextHubId, long nanoAppId) {
        forEachClientOfHub(contextHubId, new Consumer(nanoAppId) {
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((ContextHubClientBroker) obj).onNanoAppLoaded(this.f$0);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void onNanoAppUnloaded(int contextHubId, long nanoAppId) {
        forEachClientOfHub(contextHubId, new Consumer(nanoAppId) {
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((ContextHubClientBroker) obj).onNanoAppUnloaded(this.f$0);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void onHubReset(int contextHubId) {
        forEachClientOfHub(contextHubId, $$Lambda$ContextHubClientManager$aRAV9Gn84ao4XOiN6tFizfZjHo.INSTANCE);
    }

    /* access modifiers changed from: package-private */
    public void onNanoAppAborted(int contextHubId, long nanoAppId, int abortCode) {
        forEachClientOfHub(contextHubId, new Consumer(nanoAppId, abortCode) {
            private final /* synthetic */ long f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r3;
            }

            public final void accept(Object obj) {
                ((ContextHubClientBroker) obj).onNanoAppAborted(this.f$0, this.f$1);
            }
        });
    }

    private synchronized ContextHubClientBroker createNewClientBroker(IContextHubClientCallback clientCallback, int contextHubId) {
        ContextHubClientBroker broker;
        if (this.mHostEndPointIdToClientMap.size() != 32768) {
            broker = null;
            int i = 0;
            int id = this.mNextHostEndpointId;
            int i2 = 0;
            while (true) {
                if (i2 > MAX_CLIENT_ID) {
                    break;
                } else if (!this.mHostEndPointIdToClientMap.containsKey(Short.valueOf((short) id))) {
                    ContextHubClientBroker contextHubClientBroker = new ContextHubClientBroker(this.mContext, this.mContextHubProxy, this, contextHubId, (short) id, clientCallback);
                    broker = contextHubClientBroker;
                    this.mHostEndPointIdToClientMap.put(Short.valueOf((short) id), broker);
                    if (id != MAX_CLIENT_ID) {
                        i = id + 1;
                    }
                    this.mNextHostEndpointId = i;
                } else {
                    id = id == MAX_CLIENT_ID ? 0 : id + 1;
                    i2++;
                }
            }
        } else {
            throw new IllegalStateException("Could not register client - max limit exceeded");
        }
        return broker;
    }

    private void broadcastMessage(int contextHubId, NanoAppMessage message) {
        forEachClientOfHub(contextHubId, new Consumer(message) {
            private final /* synthetic */ NanoAppMessage f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((ContextHubClientBroker) obj).sendMessageToClient(this.f$0);
            }
        });
    }

    private void forEachClientOfHub(int contextHubId, Consumer<ContextHubClientBroker> callback) {
        for (ContextHubClientBroker broker : this.mHostEndPointIdToClientMap.values()) {
            if (broker.getAttachedContextHubId() == contextHubId) {
                callback.accept(broker);
            }
        }
    }
}
