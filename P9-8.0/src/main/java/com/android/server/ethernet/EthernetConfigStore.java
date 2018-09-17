package com.android.server.ethernet;

import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.net.IpConfigStore;

public class EthernetConfigStore extends IpConfigStore {
    private static final String TAG = "EthernetConfigStore";
    private static final String ipConfigFile = (Environment.getDataDirectory() + "/misc/ethernet/ipconfig.txt");

    public IpConfiguration readIpAndProxyConfigurations() {
        SparseArray<IpConfiguration> networks = readIpAndProxyConfigurations(ipConfigFile);
        if (networks.size() == 0) {
            Log.w(TAG, "No Ethernet configuration found. Using default.");
            return new IpConfiguration(IpAssignment.DHCP, ProxySettings.NONE, null, null);
        }
        if (networks.size() > 1) {
            Log.w(TAG, "Multiple Ethernet configurations detected. Only reading first one.");
        }
        return (IpConfiguration) networks.valueAt(0);
    }

    public void writeIpAndProxyConfigurations(IpConfiguration config) {
        SparseArray<IpConfiguration> networks = new SparseArray();
        networks.put(0, config);
        writeIpAndProxyConfigurations(ipConfigFile, networks);
    }
}
