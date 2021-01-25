package com.android.server.location;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.contexthub.V1_0.ContextHubMsg;
import android.hardware.contexthub.V1_0.IContexthub;
import android.hardware.location.ContextHubInfo;
import android.hardware.location.IContextHubClient;
import android.hardware.location.IContextHubClientCallback;
import android.hardware.location.NanoAppMessage;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
public class ContextHubClientManager {
    private static final boolean DEBUG_LOG_ENABLED = false;
    private static final int MAX_CLIENT_ID = 32767;
    private static final String TAG = "ContextHubClientManager";
    private final Context mContext;
    private final IContexthub mContextHubProxy;
    private final ConcurrentHashMap<Short, ContextHubClientBroker> mHostEndPointIdToClientMap = new ConcurrentHashMap<>();
    private int mNextHostEndPointId = 0;

    ContextHubClientManager(Context context, IContexthub contextHubProxy) {
        this.mContext = context;
        this.mContextHubProxy = contextHubProxy;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: java.util.concurrent.ConcurrentHashMap<java.lang.Short, com.android.server.location.ContextHubClientBroker> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r7v0, types: [com.android.server.location.ContextHubClientBroker, java.lang.Object, android.os.IBinder] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IContextHubClient registerClient(ContextHubInfo contextHubInfo, IContextHubClientCallback clientCallback) {
        ?? contextHubClientBroker;
        synchronized (this) {
            short hostEndPointId = getHostEndPointId();
            contextHubClientBroker = new ContextHubClientBroker(this.mContext, this.mContextHubProxy, this, contextHubInfo, hostEndPointId, clientCallback);
            this.mHostEndPointIdToClientMap.put(Short.valueOf(hostEndPointId), contextHubClientBroker);
        }
        try {
            contextHubClientBroker.attachDeathRecipient();
            Log.d(TAG, "Registered client with host endpoint ID " + ((int) contextHubClientBroker.getHostEndPointId()));
            return IContextHubClient.Stub.asInterface((IBinder) contextHubClientBroker);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to attach death recipient to client");
            contextHubClientBroker.close();
            return null;
        }
    }

    /* JADX WARN: Type inference failed for: r0v5, types: [com.android.server.location.ContextHubClientBroker, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v7 */
    /* JADX WARN: Type inference failed for: r0v8 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IContextHubClient registerClient(ContextHubInfo contextHubInfo, PendingIntent pendingIntent, long nanoAppId) {
        Throwable th;
        String registerString = "Regenerated";
        synchronized (this) {
            try {
                try {
                    ContextHubClientBroker broker = getClientBroker(contextHubInfo.getId(), pendingIntent, nanoAppId);
                    ?? r0 = broker;
                    if (broker == null) {
                        short hostEndPointId = getHostEndPointId();
                        ContextHubClientBroker broker2 = new ContextHubClientBroker(this.mContext, this.mContextHubProxy, this, contextHubInfo, hostEndPointId, pendingIntent, nanoAppId);
                        this.mHostEndPointIdToClientMap.put(Short.valueOf(hostEndPointId), broker2);
                        registerString = "Registered";
                        r0 = broker2;
                    }
                    Log.d(TAG, registerString + " client with host endpoint ID " + ((int) r0.getHostEndPointId()));
                    return IContextHubClient.Stub.asInterface((IBinder) r0);
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onMessageFromNanoApp(int contextHubId, ContextHubMsg message) {
        NanoAppMessage clientMessage = ContextHubServiceUtil.createNanoAppMessage(message);
        if (clientMessage.isBroadcastMessage()) {
            broadcastMessage(contextHubId, clientMessage);
            return;
        }
        ContextHubClientBroker proxy = this.mHostEndPointIdToClientMap.get(Short.valueOf(message.hostEndPoint));
        if (proxy != null) {
            proxy.sendMessageToClient(clientMessage);
            return;
        }
        Log.e(TAG, "Cannot send message to unregistered client (host endpoint ID = " + ((int) message.hostEndPoint) + ")");
    }

    /* access modifiers changed from: package-private */
    public void unregisterClient(short hostEndPointId) {
        if (this.mHostEndPointIdToClientMap.remove(Short.valueOf(hostEndPointId)) != null) {
            Log.d(TAG, "Unregistered client with host endpoint ID " + ((int) hostEndPointId));
            return;
        }
        Log.e(TAG, "Cannot unregister non-existing client with host endpoint ID " + ((int) hostEndPointId));
    }

    /* access modifiers changed from: package-private */
    public void onNanoAppLoaded(int contextHubId, long nanoAppId) {
        forEachClientOfHub(contextHubId, new Consumer(nanoAppId) {
            /* class com.android.server.location.$$Lambda$ContextHubClientManager$VPD5ebhe8Z67S8QKuTR4KzeshK8 */
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((ContextHubClientBroker) obj).onNanoAppLoaded(this.f$0);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void onNanoAppUnloaded(int contextHubId, long nanoAppId) {
        forEachClientOfHub(contextHubId, new Consumer(nanoAppId) {
            /* class com.android.server.location.$$Lambda$ContextHubClientManager$gN_vRogwyzr9qBjrQpKwwHzrFAo */
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
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
            /* class com.android.server.location.$$Lambda$ContextHubClientManager$WHzSH2fYJ3FaiF7JXPP7oX9EE */
            private final /* synthetic */ long f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((ContextHubClientBroker) obj).onNanoAppAborted(this.f$0, this.f$1);
            }
        });
    }

    private short getHostEndPointId() {
        if (this.mHostEndPointIdToClientMap.size() != 32768) {
            int id = this.mNextHostEndPointId;
            int i = 0;
            while (true) {
                if (i > MAX_CLIENT_ID) {
                    break;
                }
                int i2 = 0;
                if (!this.mHostEndPointIdToClientMap.containsKey(Short.valueOf((short) id))) {
                    if (id != MAX_CLIENT_ID) {
                        i2 = id + 1;
                    }
                    this.mNextHostEndPointId = i2;
                } else {
                    if (id != MAX_CLIENT_ID) {
                        i2 = id + 1;
                    }
                    id = i2;
                    i++;
                }
            }
            return (short) id;
        }
        throw new IllegalStateException("Could not register client - max limit exceeded");
    }

    private void broadcastMessage(int contextHubId, NanoAppMessage message) {
        forEachClientOfHub(contextHubId, new Consumer(message) {
            /* class com.android.server.location.$$Lambda$ContextHubClientManager$f15OSYbsSONpkXn7GinnrBPeumw */
            private final /* synthetic */ NanoAppMessage f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
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

    private ContextHubClientBroker getClientBroker(int contextHubId, PendingIntent pendingIntent, long nanoAppId) {
        for (ContextHubClientBroker broker : this.mHostEndPointIdToClientMap.values()) {
            if (broker.hasPendingIntent(pendingIntent, nanoAppId) && broker.getAttachedContextHubId() == contextHubId) {
                return broker;
            }
        }
        return null;
    }
}
