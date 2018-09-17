package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CivicLocationElement extends ANQPElement {
    public static final int ADDITIONAL_CODE = 32;
    public static final int ADDITIONAL_LOCATION = 22;
    public static final int BLOCK = 5;
    public static final int BRANCH_ROAD = 36;
    public static final int BUILDING = 25;
    public static final int CITY = 3;
    public static final int COUNTY_DISTRICT = 2;
    public static final int DIVISION_BOROUGH = 4;
    public static final int FLOOR = 27;
    private static final int GEOCONF_CIVIC4 = 99;
    public static final int HOUSE_NUMBER = 19;
    public static final int HOUSE_NUMBER_SUFFIX = 20;
    public static final int LANDMARK = 21;
    public static final int LANGUAGE = 0;
    public static final int LEADING_STREET_SUFFIX = 17;
    public static final int NAME = 23;
    public static final int POSTAL_COMMUNITY = 30;
    public static final int POSTAL_ZIP = 24;
    public static final int PO_BOX = 31;
    public static final int PRIMARY_ROAD = 34;
    public static final int RESERVED = 255;
    private static final int RFC4776 = 0;
    public static final int ROAD_SECTION = 35;
    public static final int ROOM = 28;
    public static final int SCRIPT = 128;
    public static final int SEAT_DESK = 33;
    public static final int STATE_PROVINCE = 1;
    public static final int STREET_DIRECTION = 16;
    public static final int STREET_GROUP = 6;
    public static final int STREET_NAME_POST_MOD = 39;
    public static final int STREET_NAME_PRE_MOD = 38;
    public static final int STREET_SUFFIX = 18;
    public static final int SUB_BRANCH_ROAD = 37;
    public static final int TYPE = 29;
    public static final int UNIT = 26;
    private static final Map<Integer, CAType> s_caTypes = null;
    private final Locale mLocale;
    private final LocationType mLocationType;
    private final Map<CAType, String> mValues;

    public enum CAType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.CivicLocationElement.CAType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.CivicLocationElement.CAType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.CivicLocationElement.CAType.<clinit>():void");
        }
    }

    public enum LocationType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.CivicLocationElement.LocationType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.CivicLocationElement.LocationType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.CivicLocationElement.LocationType.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.CivicLocationElement.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.CivicLocationElement.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.CivicLocationElement.<clinit>():void");
    }

    public CivicLocationElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        LocationType locationType = null;
        super(infoID);
        if (payload.remaining() < STREET_GROUP) {
            throw new ProtocolException("Runt civic location:" + payload.remaining());
        }
        int locType = payload.get() & RESERVED;
        if (locType != 0) {
            throw new ProtocolException("Bad Civic location type: " + locType);
        }
        int locSubType = payload.get() & RESERVED;
        if (locSubType != GEOCONF_CIVIC4) {
            throw new ProtocolException("Unexpected Civic location sub-type: " + locSubType + " (cannot handle sub elements)");
        }
        int length = payload.get() & RESERVED;
        if (length > payload.remaining()) {
            throw new ProtocolException("Invalid CA type length: " + length);
        }
        int what = payload.get() & RESERVED;
        if (what < LocationType.values().length) {
            locationType = LocationType.values()[what];
        }
        this.mLocationType = locationType;
        this.mLocale = Locale.forLanguageTag(Constants.getString(payload, COUNTY_DISTRICT, StandardCharsets.US_ASCII));
        this.mValues = new HashMap();
        while (payload.hasRemaining()) {
            CAType caType = (CAType) s_caTypes.get(Integer.valueOf(payload.get() & RESERVED));
            int caValLen = payload.get() & RESERVED;
            if (caValLen > payload.remaining()) {
                throw new ProtocolException("Bad CA value length: " + caValLen);
            }
            byte[] caValOctets = new byte[caValLen];
            payload.get(caValOctets);
            if (caType != null) {
                this.mValues.put(caType, new String(caValOctets, StandardCharsets.UTF_8));
            }
        }
    }

    public LocationType getLocationType() {
        return this.mLocationType;
    }

    public Locale getLocale() {
        return this.mLocale;
    }

    public Map<CAType, String> getValues() {
        return Collections.unmodifiableMap(this.mValues);
    }

    public String toString() {
        return "CivicLocation{mLocationType=" + this.mLocationType + ", mLocale=" + this.mLocale + ", mValues=" + this.mValues + '}';
    }
}
