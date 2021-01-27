package android.net;

import android.os.IBinder;
import android.os.RemoteException;
import com.android.internal.util.Preconditions;

public class TestNetworkManager {
    private static final String TAG = TestNetworkManager.class.getSimpleName();
    private final ITestNetworkManager mService;

    public TestNetworkManager(ITestNetworkManager service) {
        this.mService = (ITestNetworkManager) Preconditions.checkNotNull(service, "missing ITestNetworkManager");
    }

    public void teardownTestNetwork(Network network) {
        try {
            this.mService.teardownTestNetwork(network.netId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setupTestNetwork(String iface, IBinder binder) {
        try {
            this.mService.setupTestNetwork(iface, binder);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public TestNetworkInterface createTunInterface(LinkAddress[] linkAddrs) {
        try {
            return this.mService.createTunInterface(linkAddrs);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public TestNetworkInterface createTapInterface() {
        try {
            return this.mService.createTapInterface();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
