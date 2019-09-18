package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkRequest;
import android.telephony.Rlog;
import java.util.HashMap;

public class DcRequest implements Comparable<DcRequest> {
    private static final String LOG_TAG = "DcRequest";
    private static final HashMap<Integer, Integer> sApnPriorityMap = new HashMap<>();
    public final int apnId = apnIdForNetworkRequest(this.networkRequest);
    public final NetworkRequest networkRequest;
    public final int priority = priorityForApnId(this.apnId);

    public DcRequest(NetworkRequest nr, Context context) {
        initApnPriorities(context);
        this.networkRequest = nr;
    }

    public String toString() {
        return this.networkRequest.toString() + ", priority=" + this.priority + ", apnId=" + this.apnId;
    }

    public int hashCode() {
        return this.networkRequest.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof DcRequest) {
            return this.networkRequest.equals(((DcRequest) o).networkRequest);
        }
        return false;
    }

    public int compareTo(DcRequest o) {
        return o.priority - this.priority;
    }

    private int apnIdForNetworkRequest(NetworkRequest nr) {
        NetworkCapabilities nc = nr.networkCapabilities;
        if (nc.getTransportTypes().length > 0 && !nc.hasTransport(0) && (!nc.hasTransport(1) || !nc.hasCapability(0))) {
            return -1;
        }
        int apnId2 = -1;
        boolean error = false;
        if (nc.hasCapability(12)) {
            if (-1 != -1) {
                error = true;
            }
            apnId2 = 0;
        }
        if (nc.hasCapability(0)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 1;
        }
        if (nc.hasCapability(1)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 2;
        }
        if (nc.hasCapability(2)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 3;
        }
        if (nc.hasCapability(3)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 6;
        }
        if (nc.hasCapability(4)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 5;
        }
        if (nc.hasCapability(5)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 7;
        }
        if (nc.hasCapability(7)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 8;
        }
        if (nc.hasCapability(8)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = -1;
            loge("RCS APN type not yet supported");
        }
        if (nc.hasCapability(9)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 19;
        }
        if (nc.hasCapability(10)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 9;
        }
        if (nc.hasCapability(23)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 12;
        }
        if (nc.hasCapability(24)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 13;
        }
        if (nc.hasCapability(25)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 14;
        }
        if (nc.hasCapability(26)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 15;
        }
        if (nc.hasCapability(27)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 16;
        }
        if (nc.hasCapability(28)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 17;
        }
        if (nc.hasCapability(29)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 18;
        }
        if (nc.hasCapability(30)) {
            if (apnId2 != -1) {
                error = true;
            }
            apnId2 = 20;
        }
        if (error) {
            loge("Multiple apn types specified in request - result is unspecified!");
        }
        if (apnId2 == -1) {
            loge("Unsupported NetworkRequest in Telephony: nr=" + nr);
        }
        return apnId2;
    }

    private void initApnPriorities(Context context) {
        synchronized (sApnPriorityMap) {
            if (sApnPriorityMap.isEmpty()) {
                for (String networkConfigString : context.getResources().getStringArray(17236063)) {
                    NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
                    sApnPriorityMap.put(Integer.valueOf(ApnContext.apnIdForType(networkConfig.type)), Integer.valueOf(networkConfig.priority));
                }
            }
        }
    }

    private int priorityForApnId(int apnId2) {
        Integer priority2 = sApnPriorityMap.get(Integer.valueOf(apnId2));
        if (priority2 != null) {
            return priority2.intValue();
        }
        return 0;
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }
}
