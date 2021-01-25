package android.net.metrics;

import android.annotation.SystemApi;
import android.net.ConnectivityMetricsEvent;
import android.net.IIpConnectivityMetrics;
import android.net.Network;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.BitUtils;

@SystemApi
public class IpConnectivityLog {
    private static final boolean DBG = false;
    public static final String SERVICE_NAME = "connmetrics";
    private static final String TAG = IpConnectivityLog.class.getSimpleName();
    private IIpConnectivityMetrics mService;

    public interface Event extends Parcelable {
    }

    @SystemApi
    public IpConnectivityLog() {
    }

    @VisibleForTesting
    public IpConnectivityLog(IIpConnectivityMetrics service) {
        this.mService = service;
    }

    private boolean checkLoggerService() {
        if (this.mService != null) {
            return true;
        }
        IIpConnectivityMetrics service = IIpConnectivityMetrics.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
        if (service == null) {
            return false;
        }
        this.mService = service;
        return true;
    }

    public boolean log(ConnectivityMetricsEvent ev) {
        if (!checkLoggerService()) {
            return false;
        }
        if (ev.timestamp == 0) {
            ev.timestamp = System.currentTimeMillis();
        }
        try {
            if (this.mService.logEvent(ev) >= 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error logging event", e);
            return false;
        }
    }

    public boolean log(long timestamp, Event data) {
        ConnectivityMetricsEvent ev = makeEv(data);
        ev.timestamp = timestamp;
        return log(ev);
    }

    public boolean log(String ifname, Event data) {
        ConnectivityMetricsEvent ev = makeEv(data);
        ev.ifname = ifname;
        return log(ev);
    }

    public boolean log(Network network, int[] transports, Event data) {
        return log(network.netId, transports, data);
    }

    public boolean log(int netid, int[] transports, Event data) {
        ConnectivityMetricsEvent ev = makeEv(data);
        ev.netId = netid;
        ev.transports = BitUtils.packBits(transports);
        return log(ev);
    }

    public boolean log(Event data) {
        return log(makeEv(data));
    }

    private static ConnectivityMetricsEvent makeEv(Event data) {
        ConnectivityMetricsEvent ev = new ConnectivityMetricsEvent();
        ev.data = data;
        return ev;
    }
}
