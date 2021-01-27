package com.huawei.distributed.net;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.util.Log;
import com.huawei.net.LinkPropertiesEx;
import com.huawei.net.RouteInfoEx;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DistributedNetLinkProperties {
    private static final String TAG = "DistributedNetLinkProperties";
    private final LinkProperties mLinkProperties = new LinkProperties();

    public DistributedNetLinkProperties() {
        this.mLinkProperties.setInterfaceName(DistributedNetworkConstants.NET_IFACE_NAME);
        this.mLinkProperties.setMtu(DistributedNetworkConstants.NET_MTU);
        try {
            this.mLinkProperties.addRoute(RouteInfoEx.makeRouteInfo((LinkAddress) null, InetAddress.getByName(DistributedNetworkConstants.NET_IFACE_ADDRESS), DistributedNetworkConstants.NET_IFACE_NAME));
            LinkPropertiesEx.addDnsServer(this.mLinkProperties, InetAddress.getByName(DistributedNetworkConstants.DNS_SERVER1));
            LinkPropertiesEx.addDnsServer(this.mLinkProperties, InetAddress.getByName(DistributedNetworkConstants.DNS_SERVER2));
        } catch (UnknownHostException e) {
            Log.e(TAG, "Could not resolve address.");
        }
    }

    public LinkProperties getLinkProperties() {
        return this.mLinkProperties;
    }
}
