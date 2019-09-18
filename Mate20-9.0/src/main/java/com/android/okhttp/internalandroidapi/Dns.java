package com.android.okhttp.internalandroidapi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public interface Dns {
    List<InetAddress> lookup(String str) throws UnknownHostException;
}
