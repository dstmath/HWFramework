package com.android.internal.telephony;

import android.os.SystemClock;
import android.telephony.ClientRequestStats;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ClientWakelockTracker {
    public static final String LOG_TAG = "ClientWakelockTracker";
    @VisibleForTesting
    public ArrayList<ClientWakelockAccountant> mActiveClients = new ArrayList<>();
    @VisibleForTesting
    public HashMap<String, ClientWakelockAccountant> mClients = new HashMap<>();

    @VisibleForTesting
    public void startTracking(String clientId, int requestId, int token, int numRequestsInQueue) {
        ClientWakelockAccountant client = getClientWakelockAccountant(clientId);
        long uptime = SystemClock.uptimeMillis();
        client.startAttributingWakelock(requestId, token, numRequestsInQueue, uptime);
        updateConcurrentRequests(numRequestsInQueue, uptime);
        synchronized (this.mActiveClients) {
            if (!this.mActiveClients.contains(client)) {
                this.mActiveClients.add(client);
            }
        }
    }

    @VisibleForTesting
    public void stopTracking(String clientId, int requestId, int token, int numRequestsInQueue) {
        ClientWakelockAccountant client = getClientWakelockAccountant(clientId);
        long uptime = SystemClock.uptimeMillis();
        client.stopAttributingWakelock(requestId, token, uptime);
        if (client.getPendingRequestCount() == 0) {
            synchronized (this.mActiveClients) {
                this.mActiveClients.remove(client);
            }
        }
        updateConcurrentRequests(numRequestsInQueue, uptime);
    }

    @VisibleForTesting
    public void stopTrackingAll() {
        long uptime = SystemClock.uptimeMillis();
        synchronized (this.mActiveClients) {
            Iterator<ClientWakelockAccountant> it = this.mActiveClients.iterator();
            while (it.hasNext()) {
                it.next().stopAllPendingRequests(uptime);
            }
            this.mActiveClients.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public List<ClientRequestStats> getClientRequestStats() {
        List<ClientRequestStats> list;
        long uptime = SystemClock.uptimeMillis();
        synchronized (this.mClients) {
            list = new ArrayList<>(this.mClients.size());
            for (String key : this.mClients.keySet()) {
                ClientWakelockAccountant client = this.mClients.get(key);
                client.updatePendingRequestWakelockTime(uptime);
                list.add(new ClientRequestStats(client.mRequestStats));
            }
        }
        return list;
    }

    private ClientWakelockAccountant getClientWakelockAccountant(String clientId) {
        ClientWakelockAccountant client;
        synchronized (this.mClients) {
            if (this.mClients.containsKey(clientId)) {
                client = this.mClients.get(clientId);
            } else {
                client = new ClientWakelockAccountant(clientId);
                this.mClients.put(clientId, client);
            }
        }
        return client;
    }

    private void updateConcurrentRequests(int numRequestsInQueue, long time) {
        if (numRequestsInQueue != 0) {
            synchronized (this.mActiveClients) {
                Iterator<ClientWakelockAccountant> it = this.mActiveClients.iterator();
                while (it.hasNext()) {
                    it.next().changeConcurrentRequests(numRequestsInQueue, time);
                }
            }
        }
    }

    public boolean isClientActive(String clientId) {
        ClientWakelockAccountant client = getClientWakelockAccountant(clientId);
        synchronized (this.mActiveClients) {
            if (this.mActiveClients.contains(client)) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpClientRequestTracker(PrintWriter pw) {
        pw.println("-------mClients---------------");
        synchronized (this.mClients) {
            for (String key : this.mClients.keySet()) {
                pw.println("Client : " + key);
                pw.println(this.mClients.get(key).toString());
            }
        }
    }
}
