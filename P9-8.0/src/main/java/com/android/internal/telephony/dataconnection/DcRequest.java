package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkRequest;
import android.telephony.Rlog;
import java.util.HashMap;

public class DcRequest implements Comparable<DcRequest> {
    private static final String LOG_TAG = "DcRequest";
    private static final HashMap<Integer, Integer> sApnPriorityMap = new HashMap();
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
        if (nc.getTransportTypes().length > 0 && !nc.hasTransport(0)) {
            int hasCapability;
            if (nc.hasTransport(1)) {
                hasCapability = nc.hasCapability(0);
            } else {
                hasCapability = 0;
            }
            if ((hasCapability ^ 1) != 0) {
                return -1;
            }
        }
        int apnId = -1;
        boolean error = false;
        if (nc.hasCapability(12)) {
            apnId = 0;
        }
        if (nc.hasCapability(0)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 1;
        }
        if (nc.hasCapability(1)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 2;
        }
        if (nc.hasCapability(2)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 3;
        }
        if (nc.hasCapability(3)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 6;
        }
        if (nc.hasCapability(4)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 5;
        }
        if (nc.hasCapability(5)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 7;
        }
        if (nc.hasCapability(7)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 8;
        }
        if (nc.hasCapability(8)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = -1;
            loge("RCS APN type not yet supported");
        }
        if (nc.hasCapability(9)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 19;
        }
        if (nc.hasCapability(10)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 9;
        }
        if (nc.hasCapability(19)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 12;
        }
        if (nc.hasCapability(20)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 13;
        }
        if (nc.hasCapability(21)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 14;
        }
        if (nc.hasCapability(22)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 15;
        }
        if (nc.hasCapability(23)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 16;
        }
        if (nc.hasCapability(24)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 17;
        }
        if (nc.hasCapability(25)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 18;
        }
        if (nc.hasCapability(26)) {
            if (apnId != -1) {
                error = true;
            }
            apnId = 20;
        }
        if (error) {
            loge("Multiple apn types specified in request - result is unspecified!");
        }
        if (apnId == -1) {
            loge("Unsupported NetworkRequest in Telephony: nr=" + nr);
        }
        return apnId;
    }

    private void initApnPriorities(Context context) {
        synchronized (sApnPriorityMap) {
            if (sApnPriorityMap.isEmpty()) {
                for (String networkConfigString : context.getResources().getStringArray(17236057)) {
                    NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
                    sApnPriorityMap.put(Integer.valueOf(ApnContext.apnIdForType(networkConfig.type)), Integer.valueOf(networkConfig.priority));
                }
            }
        }
    }

    private int priorityForApnId(int apnId) {
        Integer priority = (Integer) sApnPriorityMap.get(Integer.valueOf(apnId));
        return priority != null ? priority.intValue() : 0;
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }
}
