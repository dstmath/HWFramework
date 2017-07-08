package com.android.server.wifi.anqp.eap;

import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.anqp.eap.EAP.AuthInfoID;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.Map;

public class NonEAPInnerAuth implements AuthParam {
    private static final Map<NonEAPType, String> sOmaMap = null;
    private static final Map<String, NonEAPType> sRevOmaMap = null;
    private final NonEAPType mType;

    public enum NonEAPType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.eap.NonEAPInnerAuth.NonEAPType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.eap.NonEAPInnerAuth.NonEAPType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.eap.NonEAPInnerAuth.NonEAPType.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.eap.NonEAPInnerAuth.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.eap.NonEAPInnerAuth.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.eap.NonEAPInnerAuth.<clinit>():void");
    }

    public NonEAPInnerAuth(int length, ByteBuffer payload) throws ProtocolException {
        if (length != 1) {
            throw new ProtocolException("Bad length: " + payload.remaining());
        }
        NonEAPType nonEAPType;
        int typeID = payload.get() & Constants.BYTE_MASK;
        if (typeID < NonEAPType.values().length) {
            nonEAPType = NonEAPType.values()[typeID];
        } else {
            nonEAPType = NonEAPType.Reserved;
        }
        this.mType = nonEAPType;
    }

    public NonEAPInnerAuth(NonEAPType type) {
        this.mType = type;
    }

    public NonEAPInnerAuth(String eapType) {
        this.mType = (NonEAPType) sRevOmaMap.get(eapType);
    }

    public AuthInfoID getAuthInfoID() {
        return AuthInfoID.NonEAPInnerAuthType;
    }

    public NonEAPType getType() {
        return this.mType;
    }

    public String getOMAtype() {
        return (String) sOmaMap.get(this.mType);
    }

    public static String mapInnerType(NonEAPType type) {
        return (String) sOmaMap.get(type);
    }

    public int hashCode() {
        return this.mType.hashCode();
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (thatObject == this) {
            return true;
        }
        if (thatObject == null || thatObject.getClass() != NonEAPInnerAuth.class) {
            return false;
        }
        if (((NonEAPInnerAuth) thatObject).getType() != getType()) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return "Auth method NonEAPInnerAuthEAP, inner = " + this.mType + '\n';
    }
}
