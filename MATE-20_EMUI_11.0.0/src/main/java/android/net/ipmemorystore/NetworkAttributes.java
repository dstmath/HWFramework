package android.net.ipmemorystore;

import com.android.internal.annotations.VisibleForTesting;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class NetworkAttributes {
    private static final boolean DBG = true;
    private static final float NULL_MATCH_WEIGHT = 0.25f;
    @VisibleForTesting
    public static final float TOTAL_WEIGHT = 850.0f;
    private static final float TOTAL_WEIGHT_CUTOFF = 520.0f;
    private static final float WEIGHT_ASSIGNEDV4ADDR = 300.0f;
    private static final float WEIGHT_ASSIGNEDV4ADDREXPIRY = 0.0f;
    private static final float WEIGHT_DNSADDRESSES = 200.0f;
    private static final float WEIGHT_GROUPHINT = 300.0f;
    private static final float WEIGHT_MTU = 50.0f;
    public final Inet4Address assignedV4Address;
    public final Long assignedV4AddressExpiry;
    public final List<InetAddress> dnsAddresses;
    public final String groupHint;
    public final Integer mtu;

    @VisibleForTesting
    public NetworkAttributes(Inet4Address assignedV4Address2, Long assignedV4AddressExpiry2, String groupHint2, List<InetAddress> dnsAddresses2, Integer mtu2) {
        List<InetAddress> list;
        if (mtu2 != null && mtu2.intValue() < 0) {
            throw new IllegalArgumentException("MTU can't be negative");
        } else if (assignedV4AddressExpiry2 == null || assignedV4AddressExpiry2.longValue() > 0) {
            this.assignedV4Address = assignedV4Address2;
            this.assignedV4AddressExpiry = assignedV4AddressExpiry2;
            this.groupHint = groupHint2;
            if (dnsAddresses2 == null) {
                list = null;
            } else {
                list = Collections.unmodifiableList(new ArrayList(dnsAddresses2));
            }
            this.dnsAddresses = list;
            this.mtu = mtu2;
        } else {
            throw new IllegalArgumentException("lease expiry can't be negative or zero");
        }
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    @VisibleForTesting
    public NetworkAttributes(NetworkAttributesParcelable parcelable) {
        this((Inet4Address) getByAddressOrNull(parcelable.assignedV4Address), parcelable.assignedV4AddressExpiry > 0 ? Long.valueOf(parcelable.assignedV4AddressExpiry) : null, parcelable.groupHint, blobArrayToInetAddressList(parcelable.dnsAddresses), parcelable.mtu >= 0 ? Integer.valueOf(parcelable.mtu) : null);
    }

    private static InetAddress getByAddressOrNull(byte[] address) {
        if (address == null) {
            return null;
        }
        try {
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private static List<InetAddress> blobArrayToInetAddressList(Blob[] blobs) {
        if (blobs == null) {
            return null;
        }
        ArrayList<InetAddress> list = new ArrayList<>(blobs.length);
        for (Blob b : blobs) {
            InetAddress addr = getByAddressOrNull(b.data);
            if (addr != null) {
                list.add(addr);
            }
        }
        return list;
    }

    private static Blob[] inetAddressListToBlobArray(List<InetAddress> addresses) {
        if (addresses == null) {
            return null;
        }
        ArrayList<Blob> blobs = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            InetAddress addr = addresses.get(i);
            if (addr != null) {
                Blob b = new Blob();
                b.data = addr.getAddress();
                blobs.add(b);
            }
        }
        return (Blob[]) blobs.toArray(new Blob[0]);
    }

    public NetworkAttributesParcelable toParcelable() {
        NetworkAttributesParcelable parcelable = new NetworkAttributesParcelable();
        Inet4Address inet4Address = this.assignedV4Address;
        parcelable.assignedV4Address = inet4Address == null ? null : inet4Address.getAddress();
        Long l = this.assignedV4AddressExpiry;
        parcelable.assignedV4AddressExpiry = l == null ? 0 : l.longValue();
        parcelable.groupHint = this.groupHint;
        parcelable.dnsAddresses = inetAddressListToBlobArray(this.dnsAddresses);
        Integer num = this.mtu;
        parcelable.mtu = num == null ? -1 : num.intValue();
        return parcelable;
    }

    private float samenessContribution(float weight, Object o1, Object o2) {
        if (o1 == null) {
            if (o2 == null) {
                return NULL_MATCH_WEIGHT * weight;
            }
            return WEIGHT_ASSIGNEDV4ADDREXPIRY;
        } else if (Objects.equals(o1, o2)) {
            return weight;
        } else {
            return WEIGHT_ASSIGNEDV4ADDREXPIRY;
        }
    }

    public float getNetworkGroupSamenessConfidence(NetworkAttributes o) {
        float samenessScore = samenessContribution(300.0f, this.assignedV4Address, o.assignedV4Address) + samenessContribution(WEIGHT_ASSIGNEDV4ADDREXPIRY, this.assignedV4AddressExpiry, o.assignedV4AddressExpiry) + samenessContribution(300.0f, this.groupHint, o.groupHint) + samenessContribution(WEIGHT_DNSADDRESSES, this.dnsAddresses, o.dnsAddresses) + samenessContribution(WEIGHT_MTU, this.mtu, o.mtu);
        if (samenessScore < TOTAL_WEIGHT_CUTOFF) {
            return samenessScore / 1040.0f;
        }
        return (((samenessScore - TOTAL_WEIGHT_CUTOFF) / 330.0f) / 2.0f) + 0.5f;
    }

    public static class Builder {
        private Inet4Address mAssignedAddress;
        private Long mAssignedAddressExpiry;
        private List<InetAddress> mDnsAddresses;
        private String mGroupHint;
        private Integer mMtu;

        public Builder setAssignedV4Address(Inet4Address assignedV4Address) {
            this.mAssignedAddress = assignedV4Address;
            return this;
        }

        public Builder setAssignedV4AddressExpiry(Long assignedV4AddressExpiry) {
            if (assignedV4AddressExpiry == null || assignedV4AddressExpiry.longValue() > 0) {
                this.mAssignedAddressExpiry = assignedV4AddressExpiry;
                return this;
            }
            throw new IllegalArgumentException("lease expiry can't be negative or zero");
        }

        public Builder setGroupHint(String groupHint) {
            this.mGroupHint = groupHint;
            return this;
        }

        public Builder setDnsAddresses(List<InetAddress> dnsAddresses) {
            if (dnsAddresses != null) {
                for (InetAddress address : dnsAddresses) {
                    if (address == null) {
                        throw new IllegalArgumentException("Null DNS address");
                    }
                }
            }
            this.mDnsAddresses = dnsAddresses;
            return this;
        }

        public Builder setMtu(Integer mtu) {
            if (mtu == null || mtu.intValue() >= 0) {
                this.mMtu = mtu;
                return this;
            }
            throw new IllegalArgumentException("MTU can't be negative");
        }

        public NetworkAttributes build() {
            return new NetworkAttributes(this.mAssignedAddress, this.mAssignedAddressExpiry, this.mGroupHint, this.mDnsAddresses, this.mMtu);
        }
    }

    public boolean isEmpty() {
        return this.assignedV4Address == null && this.assignedV4AddressExpiry == null && this.groupHint == null && this.dnsAddresses == null && this.mtu == null;
    }

    public boolean equals(Object o) {
        if (!(o instanceof NetworkAttributes)) {
            return false;
        }
        NetworkAttributes other = (NetworkAttributes) o;
        if (!Objects.equals(this.assignedV4Address, other.assignedV4Address) || !Objects.equals(this.assignedV4AddressExpiry, other.assignedV4AddressExpiry) || !Objects.equals(this.groupHint, other.groupHint) || !Objects.equals(this.dnsAddresses, other.dnsAddresses) || !Objects.equals(this.mtu, other.mtu)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.assignedV4Address, this.assignedV4AddressExpiry, this.groupHint, this.dnsAddresses, this.mtu);
    }

    public String toString() {
        StringJoiner resultJoiner = new StringJoiner(" ", "{", "}");
        ArrayList<String> nullFields = new ArrayList<>();
        if (this.assignedV4Address != null) {
            resultJoiner.add("assignedV4Addr :");
            resultJoiner.add(this.assignedV4Address.toString());
        } else {
            nullFields.add("assignedV4Addr");
        }
        if (this.assignedV4AddressExpiry != null) {
            resultJoiner.add("assignedV4AddressExpiry :");
            resultJoiner.add(this.assignedV4AddressExpiry.toString());
        } else {
            nullFields.add("assignedV4AddressExpiry");
        }
        if (this.groupHint != null) {
            resultJoiner.add("groupHint :");
            resultJoiner.add(this.groupHint);
        } else {
            nullFields.add("groupHint");
        }
        if (this.dnsAddresses != null) {
            resultJoiner.add("dnsAddr : [");
            for (InetAddress addr : this.dnsAddresses) {
                resultJoiner.add(addr.getHostAddress());
            }
            resultJoiner.add("]");
        } else {
            nullFields.add("dnsAddr");
        }
        if (this.mtu != null) {
            resultJoiner.add("mtu :");
            resultJoiner.add(this.mtu.toString());
        } else {
            nullFields.add("mtu");
        }
        if (!nullFields.isEmpty()) {
            resultJoiner.add("; Null fields : [");
            Iterator<String> it = nullFields.iterator();
            while (it.hasNext()) {
                resultJoiner.add(it.next());
            }
            resultJoiner.add("]");
        }
        return resultJoiner.toString();
    }
}
