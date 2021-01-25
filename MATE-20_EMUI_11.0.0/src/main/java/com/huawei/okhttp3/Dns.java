package com.huawei.okhttp3;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public interface Dns {
    public static final Dns SYSTEM = $$Lambda$Dns$Xju4WM2ZUgbyy4rfcZtHwwGYfW8.INSTANCE;

    List<InetAddress> lookup(String str) throws UnknownHostException;

    static /* synthetic */ List lambda$static$0(String hostname) throws UnknownHostException {
        if (hostname != null) {
            try {
                return Arrays.asList(InetAddress.getAllByName(hostname));
            } catch (NullPointerException e) {
                UnknownHostException unknownHostException = new UnknownHostException("Broken system behaviour for dns lookup of " + hostname);
                unknownHostException.initCause(e);
                throw unknownHostException;
            }
        } else {
            throw new UnknownHostException("hostname == null");
        }
    }
}
