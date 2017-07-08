package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.asn1.x509.ReasonFlags;
import java.io.IOException;

public class DERTaggedObject extends ASN1TaggedObject {
    private static final byte[] ZERO_BYTES = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.DERTaggedObject.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.DERTaggedObject.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.DERTaggedObject.<clinit>():void");
    }

    public DERTaggedObject(boolean explicit, int tagNo, ASN1Encodable obj) {
        super(explicit, tagNo, obj);
    }

    public DERTaggedObject(int tagNo, ASN1Encodable encodable) {
        super(true, tagNo, encodable);
    }

    boolean isConstructed() {
        if (this.empty || this.explicit) {
            return true;
        }
        return this.obj.toASN1Primitive().toDERObject().isConstructed();
    }

    int encodedLength() throws IOException {
        if (this.empty) {
            return StreamUtil.calculateTagLength(this.tagNo) + 1;
        }
        int length = this.obj.toASN1Primitive().toDERObject().encodedLength();
        if (this.explicit) {
            return (StreamUtil.calculateTagLength(this.tagNo) + StreamUtil.calculateBodyLength(length)) + length;
        }
        return StreamUtil.calculateTagLength(this.tagNo) + (length - 1);
    }

    void encode(ASN1OutputStream out) throws IOException {
        if (this.empty) {
            out.writeEncoded(160, this.tagNo, ZERO_BYTES);
            return;
        }
        ASN1Primitive primitive = this.obj.toASN1Primitive().toDERObject();
        if (this.explicit) {
            out.writeTag(160, this.tagNo);
            out.writeLength(primitive.encodedLength());
            out.writeObject(primitive);
            return;
        }
        int flags;
        if (primitive.isConstructed()) {
            flags = 160;
        } else {
            flags = ReasonFlags.unused;
        }
        out.writeTag(flags, this.tagNo);
        out.writeImplicitObject(primitive);
    }
}
