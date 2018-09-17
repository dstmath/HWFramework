package android.net.metrics;

import android.net.ConnectivityMetricsEvent;
import android.net.IIpConnectivityMetrics;
import android.net.IIpConnectivityMetrics.Stub;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.util.BitUtils;

public class IpConnectivityLog {
    private static final boolean DBG = false;
    public static final String SERVICE_NAME = "connmetrics";
    private static final String TAG = IpConnectivityLog.class.getSimpleName();
    private IIpConnectivityMetrics mService;

    public IpConnectivityLog(IIpConnectivityMetrics service) {
        this.mService = service;
    }

    private boolean checkLoggerService() {
        if (this.mService != null) {
            return true;
        }
        IIpConnectivityMetrics service = Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
        if (service == null) {
            return false;
        }
        this.mService = service;
        return true;
    }

    public boolean log(ConnectivityMetricsEvent ev) {
        boolean z = false;
        if (!checkLoggerService()) {
            return false;
        }
        if (ev.timestamp == 0) {
            ev.timestamp = System.currentTimeMillis();
        }
        try {
            if (this.mService.logEvent(ev) >= 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "Error logging event", e);
            return false;
        }
    }

    public boolean log(long timestamp, Parcelable data) {
        ConnectivityMetricsEvent ev = makeEv(data);
        ev.timestamp = timestamp;
        return log(ev);
    }

    public boolean log(String ifname, Parcelable data) {
        ConnectivityMetricsEvent ev = makeEv(data);
        ev.ifname = ifname;
        return log(ev);
    }

    public boolean log(int netid, int[] transports, Parcelable data) {
        ConnectivityMetricsEvent ev = makeEv(data);
        ev.netId = netid;
        ev.transports = BitUtils.packBits(transports);
        return log(ev);
    }

    public boolean log(Parcelable data) {
        return log(makeEv(data));
    }

    private static ConnectivityMetricsEvent makeEv(Parcelable data) {
        ConnectivityMetricsEvent ev = new ConnectivityMetricsEvent();
        ev.data = data;
        return ev;
    }
}
