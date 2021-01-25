package com.android.server.backup.transport;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.android.server.backup.TransportManager;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public class TransportClientManager {
    private static final String TAG = "TransportClientManager";
    private final Context mContext;
    private Map<TransportClient, String> mTransportClientsCallerMap = new WeakHashMap();
    private int mTransportClientsCreated = 0;
    private final Object mTransportClientsLock = new Object();
    private final TransportStats mTransportStats;
    private final int mUserId;

    public TransportClientManager(int userId, Context context, TransportStats transportStats) {
        this.mUserId = userId;
        this.mContext = context;
        this.mTransportStats = transportStats;
    }

    public TransportClient getTransportClient(ComponentName transportComponent, String caller) {
        return getTransportClient(transportComponent, caller, new Intent(TransportManager.SERVICE_ACTION_TRANSPORT_HOST).setComponent(transportComponent));
    }

    public TransportClient getTransportClient(ComponentName transportComponent, Bundle extras, String caller) {
        Intent bindIntent = new Intent(TransportManager.SERVICE_ACTION_TRANSPORT_HOST).setComponent(transportComponent);
        bindIntent.putExtras(extras);
        return getTransportClient(transportComponent, caller, bindIntent);
    }

    private TransportClient getTransportClient(ComponentName transportComponent, String caller, Intent bindIntent) {
        TransportClient transportClient;
        synchronized (this.mTransportClientsLock) {
            transportClient = new TransportClient(this.mUserId, this.mContext, this.mTransportStats, bindIntent, transportComponent, Integer.toString(this.mTransportClientsCreated), caller);
            this.mTransportClientsCallerMap.put(transportClient, caller);
            this.mTransportClientsCreated++;
            TransportUtils.log(3, TAG, TransportUtils.formatMessage(null, caller, "Retrieving " + transportClient));
        }
        return transportClient;
    }

    public void disposeOfTransportClient(TransportClient transportClient, String caller) {
        transportClient.unbind(caller);
        transportClient.markAsDisposed();
        synchronized (this.mTransportClientsLock) {
            TransportUtils.log(3, TAG, TransportUtils.formatMessage(null, caller, "Disposing of " + transportClient));
            this.mTransportClientsCallerMap.remove(transportClient);
        }
    }

    public void dump(PrintWriter pw) {
        pw.println("Transport clients created: " + this.mTransportClientsCreated);
        synchronized (this.mTransportClientsLock) {
            pw.println("Current transport clients: " + this.mTransportClientsCallerMap.size());
            for (TransportClient transportClient : this.mTransportClientsCallerMap.keySet()) {
                pw.println("    " + transportClient + " [" + this.mTransportClientsCallerMap.get(transportClient) + "]");
                Iterator<String> it = transportClient.getLogBuffer().iterator();
                while (it.hasNext()) {
                    pw.println("        " + it.next());
                }
            }
        }
    }
}
