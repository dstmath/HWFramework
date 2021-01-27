package com.huawei.internal.telephony.dataconnection;

import android.net.IConnectivityManager;
import android.net.LinkProperties;
import android.net.ProxyInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DataConnection;

public class DataConnectionEx {
    private DataConnection mDataConnection;

    public static IBinder getConnectivityManagerBinder() throws RemoteException {
        return IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity")).asBinder();
    }

    public DataConnection getDataConnection() {
        return this.mDataConnection;
    }

    public void setDataConnection(DataConnection dataConnection) {
        this.mDataConnection = dataConnection;
    }

    public LinkProperties getLinkProperties() {
        DataConnection dataConnection = this.mDataConnection;
        if (dataConnection != null) {
            return dataConnection.getLinkPropertiesHw();
        }
        return null;
    }

    public void setLinkPropertiesHttpProxy(ProxyInfo proxy) {
        DataConnection dataConnection = this.mDataConnection;
        if (dataConnection != null) {
            dataConnection.setLinkPropertiesHttpProxyHw(proxy);
        }
    }

    public ApnContextEx getApnContextFromCp() {
        DataConnection.ConnectionParams cp;
        DataConnection dataConnection = this.mDataConnection;
        if (dataConnection == null || (cp = dataConnection.getConnectionParams()) == null) {
            return null;
        }
        ApnContext apnContext = cp.mApnContext;
        ApnContextEx apnContextEx = new ApnContextEx();
        apnContextEx.setApnContext(apnContext);
        return apnContextEx;
    }

    public void clearLink() {
        DataConnection dataConnection = this.mDataConnection;
        if (dataConnection != null) {
            dataConnection.clearLink();
        }
    }

    public void resumeLink() {
        DataConnection dataConnection = this.mDataConnection;
        if (dataConnection != null) {
            dataConnection.resumeLink();
        }
    }
}
