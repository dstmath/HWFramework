package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.anqp.eap.EAP;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.NetworkDetail.HSRelease;
import com.google.protobuf.nano.Extension;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class ANQPFactory {
    private static final /* synthetic */ int[] -com-android-server-wifi-anqp-Constants$ANQPElementTypeSwitchesValues = null;
    private static final List<ANQPElementType> BaseANQPSet1 = null;
    private static final List<ANQPElementType> BaseANQPSet2 = null;
    private static final List<ANQPElementType> HS20ANQPSet = null;
    private static final List<ANQPElementType> HS20ANQPSetwOSU = null;

    private static /* synthetic */ int[] -getcom-android-server-wifi-anqp-Constants$ANQPElementTypeSwitchesValues() {
        if (-com-android-server-wifi-anqp-Constants$ANQPElementTypeSwitchesValues != null) {
            return -com-android-server-wifi-anqp-Constants$ANQPElementTypeSwitchesValues;
        }
        int[] iArr = new int[ANQPElementType.values().length];
        try {
            iArr[ANQPElementType.ANQP3GPPNetwork.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ANQPElementType.ANQPCapabilityList.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ANQPElementType.ANQPCivicLoc.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ANQPElementType.ANQPDomName.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ANQPElementType.ANQPEmergencyAlert.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ANQPElementType.ANQPEmergencyNAI.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ANQPElementType.ANQPEmergencyNumber.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ANQPElementType.ANQPGeoLoc.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ANQPElementType.ANQPIPAddrAvailability.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ANQPElementType.ANQPLocURI.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ANQPElementType.ANQPNAIRealm.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ANQPElementType.ANQPNeighborReport.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ANQPElementType.ANQPNwkAuthType.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ANQPElementType.ANQPQueryList.ordinal()] = 25;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ANQPElementType.ANQPRoamingConsortium.ordinal()] = 14;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ANQPElementType.ANQPTDLSCap.ordinal()] = 15;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ANQPElementType.ANQPVendorSpec.ordinal()] = 16;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ANQPElementType.ANQPVenueName.ordinal()] = 17;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ANQPElementType.HSCapabilityList.ordinal()] = 18;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ANQPElementType.HSConnCapability.ordinal()] = 19;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ANQPElementType.HSFriendlyName.ordinal()] = 20;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ANQPElementType.HSIconFile.ordinal()] = 21;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ANQPElementType.HSIconRequest.ordinal()] = 26;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ANQPElementType.HSNAIHomeRealmQuery.ordinal()] = 27;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ANQPElementType.HSOSUProviders.ordinal()] = 22;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ANQPElementType.HSOperatingclass.ordinal()] = 23;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ANQPElementType.HSQueryList.ordinal()] = 28;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ANQPElementType.HSWANMetrics.ordinal()] = 24;
        } catch (NoSuchFieldError e28) {
        }
        -com-android-server-wifi-anqp-Constants$ANQPElementTypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.ANQPFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.ANQPFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.ANQPFactory.<clinit>():void");
    }

    public static List<ANQPElementType> getBaseANQPSet(boolean includeRC) {
        return includeRC ? BaseANQPSet1 : BaseANQPSet2;
    }

    public static List<ANQPElementType> getHS20ANQPSet(boolean includeOSU) {
        return includeOSU ? HS20ANQPSetwOSU : HS20ANQPSet;
    }

    public static List<ANQPElementType> buildQueryList(NetworkDetail networkDetail, boolean matchSet, boolean osu) {
        boolean z = false;
        List<ANQPElementType> querySet = new ArrayList();
        if (matchSet) {
            if (networkDetail.getAnqpOICount() > 0) {
                z = true;
            }
            querySet.addAll(getBaseANQPSet(z));
        }
        if (networkDetail.getHSRelease() != null) {
            boolean includeOSU = osu && networkDetail.getHSRelease() == HSRelease.R2;
            if (matchSet) {
                querySet.addAll(getHS20ANQPSet(includeOSU));
            } else if (includeOSU) {
                querySet.add(ANQPElementType.HSOSUProviders);
            }
        }
        return querySet;
    }

    public static ByteBuffer buildQueryRequest(Set<ANQPElementType> elements, ByteBuffer target) {
        List<ANQPElementType> list = new ArrayList(elements);
        Collections.sort(list);
        ListIterator<ANQPElementType> elementIterator = list.listIterator();
        target.order(ByteOrder.LITTLE_ENDIAN);
        target.putShort((short) 256);
        int lenPos = target.position();
        target.putShort((short) 0);
        while (elementIterator.hasNext()) {
            Integer id = Constants.getANQPElementID((ANQPElementType) elementIterator.next());
            if (id == null) {
                elementIterator.previous();
                break;
            }
            target.putShort(id.shortValue());
        }
        target.putShort(lenPos, (short) ((target.position() - lenPos) - 2));
        if (elementIterator.hasNext()) {
            target.putShort((short) -8739);
            int vsLenPos = target.position();
            target.putShort((short) 0);
            target.putInt(Constants.HS20_PREFIX);
            target.put((byte) 1);
            target.put((byte) 0);
            while (elementIterator.hasNext()) {
                ANQPElementType elementType = (ANQPElementType) elementIterator.next();
                id = Constants.getHS20ElementID(elementType);
                if (id == null) {
                    throw new RuntimeException("Unmapped ANQPElementType: " + elementType);
                }
                target.put(id.byteValue());
            }
            target.putShort(vsLenPos, (short) ((target.position() - vsLenPos) - 2));
        }
        target.flip();
        return target;
    }

    public static ByteBuffer buildHomeRealmRequest(List<String> realmNames, ByteBuffer target) {
        target.order(ByteOrder.LITTLE_ENDIAN);
        target.putShort((short) -8739);
        int lenPos = target.position();
        target.putShort((short) 0);
        target.putInt(Constants.HS20_PREFIX);
        target.put((byte) 6);
        target.put((byte) 0);
        target.put((byte) realmNames.size());
        for (String realmName : realmNames) {
            target.put((byte) 1);
            byte[] octets = realmName.getBytes(StandardCharsets.UTF_8);
            target.put((byte) octets.length);
            target.put(octets);
        }
        target.putShort(lenPos, (short) ((target.position() - lenPos) - 2));
        target.flip();
        return target;
    }

    public static ByteBuffer buildIconRequest(String fileName, ByteBuffer target) {
        target.order(ByteOrder.LITTLE_ENDIAN);
        target.putShort((short) -8739);
        int lenPos = target.position();
        target.putShort((short) 0);
        target.putInt(Constants.HS20_PREFIX);
        target.put((byte) 10);
        target.put((byte) 0);
        target.put(fileName.getBytes(StandardCharsets.UTF_8));
        target.putShort(lenPos, (short) ((target.position() - lenPos) - 2));
        target.flip();
        return target;
    }

    public static List<ANQPElement> parsePayload(ByteBuffer payload) throws ProtocolException {
        payload.order(ByteOrder.LITTLE_ENDIAN);
        List<ANQPElement> elements = new ArrayList();
        while (payload.hasRemaining()) {
            elements.add(buildElement(payload));
        }
        return elements;
    }

    private static ANQPElement buildElement(ByteBuffer payload) throws ProtocolException {
        if (payload.remaining() < 4) {
            throw new ProtocolException("Runt payload: " + payload.remaining());
        }
        int infoIDNumber = payload.getShort() & Constants.SHORT_MASK;
        ANQPElementType infoID = Constants.mapANQPElement(infoIDNumber);
        if (infoID == null) {
            throw new ProtocolException("Bad info ID: " + infoIDNumber);
        }
        int length = payload.getShort() & Constants.SHORT_MASK;
        if (payload.remaining() >= length) {
            return buildElement(payload, infoID, length);
        }
        throw new ProtocolException("Truncated payload: " + payload.remaining() + " vs " + length);
    }

    public static ANQPElement buildElement(ByteBuffer payload, ANQPElementType infoID, int length) throws ProtocolException {
        try {
            ByteBuffer elementPayload = payload.duplicate().order(ByteOrder.LITTLE_ENDIAN);
            payload.position(payload.position() + length);
            elementPayload.limit(elementPayload.position() + length);
            switch (-getcom-android-server-wifi-anqp-Constants$ANQPElementTypeSwitchesValues()[infoID.ordinal()]) {
                case Extension.TYPE_DOUBLE /*1*/:
                    return new ThreeGPPNetworkElement(infoID, elementPayload);
                case Extension.TYPE_FLOAT /*2*/:
                    return new CapabilityListElement(infoID, elementPayload);
                case Extension.TYPE_INT64 /*3*/:
                    return new CivicLocationElement(infoID, elementPayload);
                case Extension.TYPE_UINT64 /*4*/:
                    return new DomainNameElement(infoID, elementPayload);
                case Extension.TYPE_INT32 /*5*/:
                    return new GenericStringElement(infoID, elementPayload);
                case Extension.TYPE_FIXED64 /*6*/:
                    return new GenericStringElement(infoID, elementPayload);
                case Extension.TYPE_FIXED32 /*7*/:
                    return new EmergencyNumberElement(infoID, elementPayload);
                case Extension.TYPE_BOOL /*8*/:
                    return new GEOLocationElement(infoID, elementPayload);
                case Extension.TYPE_STRING /*9*/:
                    return new IPAddressTypeAvailabilityElement(infoID, elementPayload);
                case Extension.TYPE_GROUP /*10*/:
                    return new GenericStringElement(infoID, elementPayload);
                case Extension.TYPE_MESSAGE /*11*/:
                    return new NAIRealmElement(infoID, elementPayload);
                case Extension.TYPE_BYTES /*12*/:
                    return new GenericBlobElement(infoID, elementPayload);
                case Extension.TYPE_UINT32 /*13*/:
                    return new NetworkAuthenticationTypeElement(infoID, elementPayload);
                case Extension.TYPE_ENUM /*14*/:
                    return new RoamingConsortiumElement(infoID, elementPayload);
                case Extension.TYPE_SFIXED32 /*15*/:
                    return new GenericBlobElement(infoID, elementPayload);
                case Extension.TYPE_SFIXED64 /*16*/:
                    if (elementPayload.remaining() <= 5) {
                        return new GenericBlobElement(infoID, elementPayload);
                    }
                    if (elementPayload.getInt() != Constants.HS20_PREFIX) {
                        return null;
                    }
                    int subType = elementPayload.get() & Constants.BYTE_MASK;
                    ANQPElementType hs20ID = Constants.mapHS20Element(subType);
                    if (hs20ID == null) {
                        throw new ProtocolException("Bad HS20 info ID: " + subType);
                    }
                    elementPayload.get();
                    return buildHS20Element(hs20ID, elementPayload);
                case Extension.TYPE_SINT32 /*17*/:
                    return new VenueNameElement(infoID, elementPayload);
                default:
                    throw new ProtocolException("Unknown element ID: " + infoID);
            }
        } catch (ProtocolException e) {
            throw e;
        } catch (Exception e2) {
            throw new ProtocolException("Unknown parsing error", e2);
        }
    }

    public static ANQPElement buildHS20Element(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        try {
            switch (-getcom-android-server-wifi-anqp-Constants$ANQPElementTypeSwitchesValues()[infoID.ordinal()]) {
                case Extension.TYPE_SINT64 /*18*/:
                    return new HSCapabilityListElement(infoID, payload);
                case CivicLocationElement.HOUSE_NUMBER /*19*/:
                    return new HSConnectionCapabilityElement(infoID, payload);
                case CivicLocationElement.HOUSE_NUMBER_SUFFIX /*20*/:
                    return new HSFriendlyNameElement(infoID, payload);
                case EAP.EAP_TTLS /*21*/:
                    return new HSIconFileElement(infoID, payload);
                case CivicLocationElement.ADDITIONAL_LOCATION /*22*/:
                    return new RawByteElement(infoID, payload);
                case EAP.EAP_AKA /*23*/:
                    return new GenericBlobElement(infoID, payload);
                case EAP.EAP_3Com /*24*/:
                    return new HSWanMetricsElement(infoID, payload);
                default:
                    return null;
            }
        } catch (ProtocolException e) {
            throw e;
        } catch (Exception e2) {
            throw new ProtocolException("Unknown parsing error", e2);
        }
    }
}
