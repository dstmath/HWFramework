package com.android.server.wifi.anqp.eap;

import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.anqp.eap.EAP.AuthInfoID;
import java.net.ProtocolException;
import java.nio.ByteBuffer;

public class Credential implements AuthParam {
    private final AuthInfoID mAuthInfoID;
    private final CredType mCredType;

    public enum CredType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.eap.Credential.CredType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.eap.Credential.CredType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.eap.Credential.CredType.<clinit>():void");
        }
    }

    public Credential(AuthInfoID infoID, int length, ByteBuffer payload) throws ProtocolException {
        if (length != 1) {
            throw new ProtocolException("Bad length: " + length);
        }
        CredType credType;
        this.mAuthInfoID = infoID;
        int typeID = payload.get() & Constants.BYTE_MASK;
        if (typeID < CredType.values().length) {
            credType = CredType.values()[typeID];
        } else {
            credType = CredType.Reserved;
        }
        this.mCredType = credType;
    }

    public AuthInfoID getAuthInfoID() {
        return this.mAuthInfoID;
    }

    public int hashCode() {
        return (this.mAuthInfoID.hashCode() * 31) + this.mCredType.hashCode();
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (thatObject == this) {
            return true;
        }
        if (thatObject == null || thatObject.getClass() != Credential.class) {
            return false;
        }
        if (((Credential) thatObject).getCredType() != getCredType()) {
            z = false;
        }
        return z;
    }

    public CredType getCredType() {
        return this.mCredType;
    }

    public String toString() {
        return "Auth method " + this.mAuthInfoID + " = " + this.mCredType + "\n";
    }
}
