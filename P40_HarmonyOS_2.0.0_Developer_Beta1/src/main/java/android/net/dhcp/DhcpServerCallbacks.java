package android.net.dhcp;

import android.net.dhcp.IDhcpServerCallbacks;

public abstract class DhcpServerCallbacks extends IDhcpServerCallbacks.Stub {
    @Override // android.net.dhcp.IDhcpServerCallbacks
    public int getInterfaceVersion() {
        return 0;
    }
}
