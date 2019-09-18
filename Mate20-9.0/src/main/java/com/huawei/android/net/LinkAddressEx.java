package com.huawei.android.net;

import android.net.LinkAddress;
import java.net.InetAddress;

public class LinkAddressEx {
    public static LinkAddress create(InetAddress serverAddress, int prefixLength) {
        return new LinkAddress(serverAddress, prefixLength);
    }

    public static LinkAddress create(String serverAddressAndPrefixLength) {
        return new LinkAddress(serverAddressAndPrefixLength);
    }
}
