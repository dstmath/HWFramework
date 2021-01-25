package android.telecom;

import android.content.ComponentName;
import android.os.RemoteException;
import com.android.internal.telecom.IConnectionService;
import java.util.HashMap;
import java.util.Map;

public class RemoteConnectionManager {
    private final ConnectionService mOurConnectionServiceImpl;
    private final Map<ComponentName, RemoteConnectionService> mRemoteConnectionServices = new HashMap();

    public RemoteConnectionManager(ConnectionService ourConnectionServiceImpl) {
        this.mOurConnectionServiceImpl = ourConnectionServiceImpl;
    }

    /* access modifiers changed from: package-private */
    public void addConnectionService(ComponentName componentName, IConnectionService outgoingConnectionServiceRpc) {
        if (!this.mRemoteConnectionServices.containsKey(componentName)) {
            try {
                this.mRemoteConnectionServices.put(componentName, new RemoteConnectionService(outgoingConnectionServiceRpc, this.mOurConnectionServiceImpl));
            } catch (RemoteException e) {
            }
        }
    }

    public RemoteConnection createRemoteConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request, boolean isIncoming) {
        if (request.getAccountHandle() != null) {
            ComponentName componentName = request.getAccountHandle().getComponentName();
            if (this.mRemoteConnectionServices.containsKey(componentName)) {
                RemoteConnectionService remoteService = this.mRemoteConnectionServices.get(componentName);
                if (remoteService != null) {
                    return remoteService.createRemoteConnection(connectionManagerPhoneAccount, request, isIncoming);
                }
                return null;
            }
            throw new UnsupportedOperationException("accountHandle not supported: " + componentName);
        }
        throw new IllegalArgumentException("accountHandle must be specified.");
    }

    public void conferenceRemoteConnections(RemoteConnection a, RemoteConnection b) {
        if (a.getConnectionService() == b.getConnectionService()) {
            try {
                a.getConnectionService().conference(a.getId(), b.getId(), null);
            } catch (RemoteException e) {
            }
        } else {
            Log.w(this, "Request to conference incompatible remote connections (%s,%s) (%s,%s)", a.getConnectionService(), a.getId(), b.getConnectionService(), b.getId());
        }
    }
}
