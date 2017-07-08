package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;

public class IPAddressTypeAvailabilityElement extends ANQPElement {
    private final IPv4Availability mV4Availability;
    private final IPv6Availability mV6Availability;

    public enum IPv4Availability {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement.IPv4Availability.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement.IPv4Availability.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement.IPv4Availability.<clinit>():void");
        }
    }

    public enum IPv6Availability {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement.IPv6Availability.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement.IPv6Availability.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement.IPv6Availability.<clinit>():void");
        }
    }

    public IPAddressTypeAvailabilityElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        if (payload.remaining() != 1) {
            throw new ProtocolException("Bad IP Address Type Availability length: " + payload.remaining());
        }
        IPv4Availability iPv4Availability;
        int ipField = payload.get();
        this.mV6Availability = IPv6Availability.values()[ipField & 3];
        ipField = (ipField >> 2) & 63;
        if (ipField < IPv4Availability.values().length) {
            iPv4Availability = IPv4Availability.values()[ipField];
        } else {
            iPv4Availability = IPv4Availability.Unknown;
        }
        this.mV4Availability = iPv4Availability;
    }

    public IPv4Availability getV4Availability() {
        return this.mV4Availability;
    }

    public IPv6Availability getV6Availability() {
        return this.mV6Availability;
    }

    public String toString() {
        return "IPAddressTypeAvailability{mV4Availability=" + this.mV4Availability + ", mV6Availability=" + this.mV6Availability + '}';
    }
}
