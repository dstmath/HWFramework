package com.android.server.intellicom.networkslice.model;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FqdnIps {
    private static String CHAR_ENCODING = "ISO_8859_1";
    private static String IPV4_MASK = null;
    private static String IPV6_PREFIX = null;
    private static final String TAG = "FqdnIps";
    private final Set<InetAddress> mIpv4Addr;
    private byte[] mIpv4AddrAndMask;
    private final Set<InetAddress> mIpv6Addr;
    private byte[] mIpv6AddrAndPrefix;

    static {
        try {
            IPV4_MASK = new String(new byte[]{-1, -1, -1, -1}, CHAR_ENCODING);
            IPV6_PREFIX = new String(new byte[]{Byte.MIN_VALUE}, CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "FQDN ip init failed");
        }
    }

    private FqdnIps(Builder builder) {
        this.mIpv4Addr = builder.mIpv4Addr != null ? builder.mIpv4Addr : new HashSet<>();
        this.mIpv6Addr = builder.mIpv6Addr != null ? builder.mIpv6Addr : new HashSet<>();
        try {
            setIpv4AddrAndMask();
        } catch (UnsupportedEncodingException e) {
            this.mIpv4AddrAndMask = new byte[0];
        }
        try {
            setIpv6AddrAndPrefix();
        } catch (UnsupportedEncodingException e2) {
            this.mIpv6AddrAndPrefix = new byte[0];
        }
    }

    public boolean hasNewFqdnIps(FqdnIps fqdnIps) {
        return !this.mIpv4Addr.containsAll(fqdnIps.mIpv4Addr) || !this.mIpv6Addr.containsAll(fqdnIps.mIpv6Addr);
    }

    public void mergeFqdnIps(FqdnIps newFqdnIps) {
        this.mIpv4Addr.addAll(newFqdnIps.getIpv4Addr());
        this.mIpv6Addr.addAll(newFqdnIps.getIpv6Addr());
    }

    public FqdnIps getNewFqdnIps(FqdnIps fqdnIps) {
        if (fqdnIps == null) {
            return new Builder().setIpv4Addr(null).setIpv6Addr(null).build();
        }
        Set<InetAddress> ipv4 = new HashSet<>();
        for (InetAddress fqdnIpv4 : fqdnIps.mIpv4Addr) {
            if (!this.mIpv4Addr.contains(fqdnIpv4)) {
                ipv4.add(fqdnIpv4);
            }
        }
        Set<InetAddress> ipv6 = new HashSet<>();
        for (InetAddress fqdnIpv6 : fqdnIps.mIpv6Addr) {
            if (!this.mIpv6Addr.contains(fqdnIpv6)) {
                ipv6.add(fqdnIpv6);
            }
        }
        return new Builder().setIpv4Addr(ipv4).setIpv6Addr(ipv6).build();
    }

    public boolean isEmpty() {
        return this.mIpv4Addr.size() == 0 && this.mIpv6Addr.size() == 0;
    }

    private void setIpv4AddrAndMask() throws UnsupportedEncodingException {
        StringBuilder ipv4AddrAndMask = new StringBuilder();
        for (InetAddress ip : this.mIpv4Addr) {
            ipv4AddrAndMask.append(new String(ip.getAddress(), CHAR_ENCODING));
            ipv4AddrAndMask.append(IPV4_MASK);
        }
        this.mIpv4AddrAndMask = ipv4AddrAndMask.toString().getBytes(CHAR_ENCODING);
    }

    private void setIpv6AddrAndPrefix() throws UnsupportedEncodingException {
        StringBuilder ipv6AddrAndPrefix = new StringBuilder();
        for (InetAddress ip : this.mIpv6Addr) {
            ipv6AddrAndPrefix.append(new String(ip.getAddress(), CHAR_ENCODING));
            ipv6AddrAndPrefix.append(IPV6_PREFIX);
        }
        this.mIpv6AddrAndPrefix = ipv6AddrAndPrefix.toString().getBytes(CHAR_ENCODING);
    }

    public Set<InetAddress> getIpv4Addr() {
        return this.mIpv4Addr;
    }

    public Set<InetAddress> getIpv6Addr() {
        return this.mIpv6Addr;
    }

    public byte getIpv4Num() {
        Set<InetAddress> set = this.mIpv4Addr;
        if (set == null) {
            return 0;
        }
        return (byte) set.size();
    }

    public byte getIpv6Num() {
        Set<InetAddress> set = this.mIpv6Addr;
        if (set == null) {
            return 0;
        }
        return (byte) set.size();
    }

    public byte[] getIpv4AddrAndMask() {
        return this.mIpv4AddrAndMask;
    }

    public byte[] getIpv6AddrAndPrefix() {
        return this.mIpv6AddrAndPrefix;
    }

    public static final class Builder {
        private Set<InetAddress> mIpv4Addr;
        private Set<InetAddress> mIpv6Addr;

        public Builder setIpv4Addr(Set<InetAddress> ipv4Addr) {
            this.mIpv4Addr = ipv4Addr;
            return this;
        }

        public Builder setIpv6Addr(Set<InetAddress> ipv6Addr) {
            this.mIpv6Addr = ipv6Addr;
            return this;
        }

        public FqdnIps build() {
            return new FqdnIps(this);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FqdnIps fqdnIps = (FqdnIps) o;
        if (!Objects.equals(this.mIpv4Addr, fqdnIps.mIpv4Addr) || !Objects.equals(this.mIpv6Addr, fqdnIps.mIpv6Addr)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mIpv4Addr, this.mIpv6Addr);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FqdnIps{Ipv4Num=");
        Set<InetAddress> set = this.mIpv4Addr;
        int i = 0;
        sb.append(set == null ? 0 : set.size());
        sb.append(", Ipv6Num=");
        Set<InetAddress> set2 = this.mIpv6Addr;
        if (set2 != null) {
            i = set2.size();
        }
        sb.append(i);
        sb.append('}');
        return sb.toString();
    }
}
