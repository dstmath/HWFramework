package com.android.internal.telephony;

import android.os.SystemClock;
import android.telephony.ClientRequestStats;
import android.telephony.Rlog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientWakelockTracker {
    public static final String LOG_TAG = "ClientWakelockTracker";
    public ArrayList<ClientWakelockAccountant> mActiveClients = new ArrayList();
    public HashMap<String, ClientWakelockAccountant> mClients = new HashMap();

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

    public void stopTrackingAll() {
        long uptime = SystemClock.uptimeMillis();
        synchronized (this.mActiveClients) {
            for (ClientWakelockAccountant client : this.mActiveClients) {
                client.stopAllPendingRequests(uptime);
            }
            this.mActiveClients.clear();
        }
    }

    List<ClientRequestStats> getClientRequestStats() {
        List<ClientRequestStats> list;
        long uptime = SystemClock.uptimeMillis();
        synchronized (this.mClients) {
            list = new ArrayList(this.mClients.size());
            for (String key : this.mClients.keySet()) {
                ClientWakelockAccountant client = (ClientWakelockAccountant) this.mClients.get(key);
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
                client = (ClientWakelockAccountant) this.mClients.get(clientId);
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
                for (ClientWakelockAccountant cI : this.mActiveClients) {
                    cI.changeConcurrentRequests(numRequestsInQueue, time);
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

    void dumpClientRequestTracker() {
        Rlog.d("RILJ", "-------mClients---------------");
        synchronized (this.mClients) {
            for (String key : this.mClients.keySet()) {
                Rlog.d("RILJ", "Client : " + key);
                Rlog.d("RILJ", ((ClientWakelockAccountant) this.mClients.get(key)).toString());
            }
        }
    }
}
