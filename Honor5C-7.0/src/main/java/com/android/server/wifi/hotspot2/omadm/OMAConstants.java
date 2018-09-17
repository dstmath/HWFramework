package com.android.server.wifi.hotspot2.omadm;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class OMAConstants {
    public static final String DevDetailURN = "urn:oma:mo:oma-dm-devdetail:1.0";
    public static final String DevDetailXURN = "urn:wfa:mo-ext:hotspot2dot0-devdetail-ext:1.0";
    public static final String DevInfoURN = "urn:oma:mo:oma-dm-devinfo:1.0";
    private static final byte[] INDENT = null;
    public static final String MOVersion = "1.0";
    public static final String OMAVersion = "1.2";
    public static final String PPS_URN = "urn:wfa:mo:hotspot2dot0-perprovidersubscription:1.0";
    public static final String SppMOAttribute = "spp:moURN";
    public static final String[] SupportedMO_URNs = null;
    public static final String SyncML = "syncml:dmddf1.2";
    public static final String SyncMLVersionTag = "VerDTD";
    public static final String TAG_Error = "spp:sppError";
    public static final String TAG_MOContainer = "spp:moContainer";
    public static final String TAG_PostDevData = "spp:sppPostDevData";
    public static final String TAG_SessionID = "spp:sessionID";
    public static final String TAG_Status = "spp:sppStatus";
    public static final String TAG_SupportedMOs = "spp:supportedMOList";
    public static final String TAG_SupportedVersions = "spp:supportedSPPVersions";
    public static final String TAG_UpdateResponse = "spp:sppUpdateResponse";
    public static final String TAG_Version = "spp:sppVersion";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.hotspot2.omadm.OMAConstants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.hotspot2.omadm.OMAConstants.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.omadm.OMAConstants.<clinit>():void");
    }

    private OMAConstants() {
    }

    public static void serializeString(String s, OutputStream out) throws IOException {
        out.write(String.format("%x:", new Object[]{Integer.valueOf(s.getBytes(StandardCharsets.UTF_8).length)}).getBytes(StandardCharsets.UTF_8));
        out.write(octets);
    }

    public static void indent(int level, OutputStream out) throws IOException {
        out.write(INDENT, 0, level);
    }

    public static String deserializeString(InputStream in) throws IOException {
        StringBuilder prefix = new StringBuilder();
        while (true) {
            byte b = (byte) in.read();
            if (b == 46) {
                return null;
            }
            if (b == 58) {
                break;
            } else if (b > 32) {
                prefix.append((char) b);
            }
        }
        byte[] octets = new byte[Integer.parseInt(prefix.toString(), 16)];
        int offset = 0;
        while (offset < octets.length) {
            int amount = in.read(octets, offset, octets.length - offset);
            if (amount <= 0) {
                throw new EOFException();
            }
            offset += amount;
        }
        return new String(octets, StandardCharsets.UTF_8);
    }

    public static String readURN(InputStream in) throws IOException {
        StringBuilder urn = new StringBuilder();
        while (true) {
            byte b = (byte) in.read();
            if (b == 41) {
                return urn.toString();
            }
            urn.append((char) b);
        }
    }
}
