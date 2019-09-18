package com.android.server.wifi.hotspot2.anqp;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.ByteBufferReader;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ANQPParser {
    @VisibleForTesting
    public static final int VENDOR_SPECIFIC_HS20_OI = 5271450;
    @VisibleForTesting
    public static final int VENDOR_SPECIFIC_HS20_TYPE = 17;

    public static ANQPElement parseElement(Constants.ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        switch (infoID) {
            case ANQPVenueName:
                return VenueNameElement.parse(payload);
            case ANQPRoamingConsortium:
                return RoamingConsortiumElement.parse(payload);
            case ANQPIPAddrAvailability:
                return IPAddressTypeAvailabilityElement.parse(payload);
            case ANQPNAIRealm:
                return NAIRealmElement.parse(payload);
            case ANQP3GPPNetwork:
                return ThreeGPPNetworkElement.parse(payload);
            case ANQPDomName:
                return DomainNameElement.parse(payload);
            case ANQPVendorSpec:
                return parseVendorSpecificElement(payload);
            default:
                throw new ProtocolException("Unknown element ID: " + infoID);
        }
    }

    public static ANQPElement parseHS20Element(Constants.ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        switch (infoID) {
            case HSFriendlyName:
                return HSFriendlyNameElement.parse(payload);
            case HSWANMetrics:
                return HSWanMetricsElement.parse(payload);
            case HSConnCapability:
                return HSConnectionCapabilityElement.parse(payload);
            case HSOSUProviders:
                return HSOsuProvidersElement.parse(payload);
            default:
                throw new ProtocolException("Unknown element ID: " + infoID);
        }
    }

    private static ANQPElement parseVendorSpecificElement(ByteBuffer payload) throws ProtocolException {
        int oi = (int) ByteBufferReader.readInteger(payload, ByteOrder.BIG_ENDIAN, 3);
        int type = payload.get() & Constants.BYTE_MASK;
        if (oi == 5271450 && type == 17) {
            int subType = payload.get() & Constants.BYTE_MASK;
            Constants.ANQPElementType hs20ID = Constants.mapHS20Element(subType);
            if (hs20ID != null) {
                payload.get();
                return parseHS20Element(hs20ID, payload);
            }
            throw new ProtocolException("Unsupported subtype: " + subType);
        }
        throw new ProtocolException("Unsupported vendor specific OI=" + oi + " type=" + type);
    }
}
