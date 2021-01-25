package com.huawei.net;

import android.net.LinkAddress;
import android.net.RouteInfo;
import java.net.InetAddress;

public class RouteInfoEx {
    private RouteInfoEx() {
    }

    public static RouteInfo makeRouteInfo(LinkAddress destination, InetAddress gateway, String iface) {
        return new RouteInfo(destination, gateway, iface);
    }
}
