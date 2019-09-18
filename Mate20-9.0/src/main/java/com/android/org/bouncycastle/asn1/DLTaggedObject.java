package com.android.org.bouncycastle.asn1;

import java.io.IOException;

public class DLTaggedObject extends ASN1TaggedObject {
    private static final byte[] ZERO_BYTES = new byte[0];

    public DLTaggedObject(boolean explicit, int tagNo, ASN1Encodable obj) {
        super(explicit, tagNo, obj);
    }

    /* access modifiers changed from: package-private */
    public boolean isConstructed() {
        if (this.empty || this.explicit) {
            return true;
        }
        return this.obj.toASN1Primitive().toDLObject().isConstructed();
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() throws IOException {
        if (this.empty) {
            return StreamUtil.calculateTagLength(this.tagNo) + 1;
        }
        int length = this.obj.toASN1Primitive().toDLObject().encodedLength();
        if (this.explicit) {
            return StreamUtil.calculateTagLength(this.tagNo) + StreamUtil.calculateBodyLength(length) + length;
        }
        return StreamUtil.calculateTagLength(this.tagNo) + (length - 1);
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream out) throws IOException {
        int flags;
        if (!this.empty) {
            ASN1Primitive primitive = this.obj.toASN1Primitive().toDLObject();
            if (this.explicit) {
                out.writeTag(160, this.tagNo);
                out.writeLength(primitive.encodedLength());
                out.writeObject(primitive);
                return;
            }
            if (primitive.isConstructed()) {
                flags = 160;
            } else {
                flags = 128;
            }
            out.writeTag(flags, this.tagNo);
            out.writeImplicitObject(primitive);
            return;
        }
        out.writeEncoded(160, this.tagNo, ZERO_BYTES);
    }
}
