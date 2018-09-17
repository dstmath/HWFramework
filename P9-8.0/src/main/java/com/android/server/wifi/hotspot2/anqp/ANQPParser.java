package com.android.server.wifi.hotspot2.anqp;

import com.android.server.wifi.ByteBufferReader;
import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ANQPParser {
    private static final /* synthetic */ int[] -com-android-server-wifi-hotspot2-anqp-Constants$ANQPElementTypeSwitchesValues = null;
    public static final int VENDOR_SPECIFIC_HS20_OI = 5271450;
    public static final int VENDOR_SPECIFIC_HS20_TYPE = 17;

    private static /* synthetic */ int[] -getcom-android-server-wifi-hotspot2-anqp-Constants$ANQPElementTypeSwitchesValues() {
        if (-com-android-server-wifi-hotspot2-anqp-Constants$ANQPElementTypeSwitchesValues != null) {
            return -com-android-server-wifi-hotspot2-anqp-Constants$ANQPElementTypeSwitchesValues;
        }
        int[] iArr = new int[ANQPElementType.values().length];
        try {
            iArr[ANQPElementType.ANQP3GPPNetwork.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ANQPElementType.ANQPDomName.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ANQPElementType.ANQPIPAddrAvailability.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ANQPElementType.ANQPNAIRealm.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ANQPElementType.ANQPQueryList.ordinal()] = 12;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ANQPElementType.ANQPRoamingConsortium.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ANQPElementType.ANQPVendorSpec.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ANQPElementType.ANQPVenueName.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ANQPElementType.HSConnCapability.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ANQPElementType.HSFriendlyName.ordinal()] = 9;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ANQPElementType.HSIconRequest.ordinal()] = 13;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ANQPElementType.HSNAIHomeRealmQuery.ordinal()] = 14;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ANQPElementType.HSOSUProviders.ordinal()] = 10;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ANQPElementType.HSQueryList.ordinal()] = 15;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ANQPElementType.HSWANMetrics.ordinal()] = 11;
        } catch (NoSuchFieldError e15) {
        }
        -com-android-server-wifi-hotspot2-anqp-Constants$ANQPElementTypeSwitchesValues = iArr;
        return iArr;
    }

    public static ANQPElement parseElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        switch (-getcom-android-server-wifi-hotspot2-anqp-Constants$ANQPElementTypeSwitchesValues()[infoID.ordinal()]) {
            case 1:
                return ThreeGPPNetworkElement.parse(payload);
            case 2:
                return DomainNameElement.parse(payload);
            case 3:
                return IPAddressTypeAvailabilityElement.parse(payload);
            case 4:
                return NAIRealmElement.parse(payload);
            case 5:
                return RoamingConsortiumElement.parse(payload);
            case 6:
                return parseVendorSpecificElement(payload);
            case 7:
                return VenueNameElement.parse(payload);
            default:
                throw new ProtocolException("Unknown element ID: " + infoID);
        }
    }

    public static ANQPElement parseHS20Element(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        switch (-getcom-android-server-wifi-hotspot2-anqp-Constants$ANQPElementTypeSwitchesValues()[infoID.ordinal()]) {
            case 8:
                return HSConnectionCapabilityElement.parse(payload);
            case 9:
                return HSFriendlyNameElement.parse(payload);
            case 10:
                return RawByteElement.parse(infoID, payload);
            case 11:
                return HSWanMetricsElement.parse(payload);
            default:
                throw new ProtocolException("Unknown element ID: " + infoID);
        }
    }

    private static ANQPElement parseVendorSpecificElement(ByteBuffer payload) throws ProtocolException {
        int oi = (int) ByteBufferReader.readInteger(payload, ByteOrder.BIG_ENDIAN, 3);
        int type = payload.get() & Constants.BYTE_MASK;
        if (oi == VENDOR_SPECIFIC_HS20_OI && type == 17) {
            int subType = payload.get() & Constants.BYTE_MASK;
            ANQPElementType hs20ID = Constants.mapHS20Element(subType);
            if (hs20ID == null) {
                throw new ProtocolException("Unsupported subtype: " + subType);
            }
            payload.get();
            return parseHS20Element(hs20ID, payload);
        }
        throw new ProtocolException("Unsupported vendor specific OI=" + oi + " type=" + type);
    }
}
