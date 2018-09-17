package com.android.server.net;

import java.net.InetAddress;

/* compiled from: NetlinkTracker */
class DnsServerEntry implements Comparable<DnsServerEntry> {
    public final InetAddress address;
    public long expiry;

    public DnsServerEntry(InetAddress address, long expiry) throws IllegalArgumentException {
        this.address = address;
        this.expiry = expiry;
    }

    public int compareTo(DnsServerEntry other) {
        return Long.compare(other.expiry, this.expiry);
    }
}
