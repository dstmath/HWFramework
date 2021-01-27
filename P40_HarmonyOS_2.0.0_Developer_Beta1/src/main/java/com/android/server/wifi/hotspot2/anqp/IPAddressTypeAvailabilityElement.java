package com.android.server.wifi.hotspot2.anqp;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class IPAddressTypeAvailabilityElement extends ANQPElement {
    @VisibleForTesting
    public static final int EXPECTED_BUFFER_LENGTH = 1;
    private static final Set<Integer> IPV4_AVAILABILITY = new HashSet();
    private static final int IPV4_AVAILABILITY_MASK = 63;
    public static final int IPV4_DOUBLE_NAT = 4;
    public static final int IPV4_NOT_AVAILABLE = 0;
    public static final int IPV4_PORT_RESTRICTED = 2;
    public static final int IPV4_PORT_RESTRICTED_AND_DOUBLE_NAT = 6;
    public static final int IPV4_PORT_RESTRICTED_AND_SINGLE_NAT = 5;
    public static final int IPV4_PUBLIC = 1;
    public static final int IPV4_SINGLE_NAT = 3;
    public static final int IPV4_UNKNOWN = 7;
    private static final Set<Integer> IPV6_AVAILABILITY = new HashSet();
    private static final int IPV6_AVAILABILITY_MASK = 3;
    public static final int IPV6_AVAILABLE = 1;
    public static final int IPV6_NOT_AVAILABLE = 0;
    public static final int IPV6_UNKNOWN = 2;
    private final int mV4Availability;
    private final int mV6Availability;

    static {
        IPV4_AVAILABILITY.add(0);
        IPV4_AVAILABILITY.add(1);
        IPV4_AVAILABILITY.add(2);
        IPV4_AVAILABILITY.add(3);
        IPV4_AVAILABILITY.add(4);
        IPV4_AVAILABILITY.add(5);
        IPV4_AVAILABILITY.add(6);
        IPV6_AVAILABILITY.add(0);
        IPV6_AVAILABILITY.add(1);
        IPV6_AVAILABILITY.add(2);
    }

    @VisibleForTesting
    public IPAddressTypeAvailabilityElement(int v4Availability, int v6Availability) {
        super(Constants.ANQPElementType.ANQPIPAddrAvailability);
        this.mV4Availability = v4Availability;
        this.mV6Availability = v6Availability;
    }

    public static IPAddressTypeAvailabilityElement parse(ByteBuffer payload) throws ProtocolException {
        if (payload.remaining() == 1) {
            int ipField = payload.get() & 255;
            int v6Availability = ipField & 3;
            if (!IPV6_AVAILABILITY.contains(Integer.valueOf(v6Availability))) {
                v6Availability = 2;
            }
            int v4Availability = (ipField >> 2) & 63;
            if (!IPV4_AVAILABILITY.contains(Integer.valueOf(v4Availability))) {
                v4Availability = 7;
            }
            return new IPAddressTypeAvailabilityElement(v4Availability, v6Availability);
        }
        throw new ProtocolException("Unexpected buffer length: " + payload.remaining());
    }

    public int getV4Availability() {
        return this.mV4Availability;
    }

    public int getV6Availability() {
        return this.mV6Availability;
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof IPAddressTypeAvailabilityElement)) {
            return false;
        }
        IPAddressTypeAvailabilityElement that = (IPAddressTypeAvailabilityElement) thatObject;
        if (this.mV4Availability == that.mV4Availability && this.mV6Availability == that.mV6Availability) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.mV4Availability << (this.mV6Availability + 2);
    }

    public String toString() {
        return "IPAddressTypeAvailability{mV4Availability=" + this.mV4Availability + ", mV6Availability=" + this.mV6Availability + '}';
    }
}
