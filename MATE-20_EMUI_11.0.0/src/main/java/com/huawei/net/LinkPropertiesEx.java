package com.huawei.net;

import android.net.LinkProperties;
import java.net.InetAddress;

public class LinkPropertiesEx {
    private LinkPropertiesEx() {
    }

    public static void addDnsServer(LinkProperties linkProperties, InetAddress dnsServer) {
        linkProperties.addDnsServer(dnsServer);
    }
}
