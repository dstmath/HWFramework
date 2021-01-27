package com.android.server.net;

import java.net.InetAddress;

/* compiled from: NetlinkTracker */
class DnsServerEntry implements Comparable<DnsServerEntry> {
    public final InetAddress address;
    public long expiry;

    public DnsServerEntry(InetAddress address2, long expiry2) throws IllegalArgumentException {
        this.address = address2;
        this.expiry = expiry2;
    }

    public int compareTo(DnsServerEntry other) {
        return Long.compare(other.expiry, this.expiry);
    }
}
