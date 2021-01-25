package org.bouncycastle.crypto.util;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public final class DERMacData {
    private final byte[] macData;

    /* renamed from: org.bouncycastle.crypto.util.DERMacData$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$org$bouncycastle$crypto$util$DERMacData$Type = new int[Type.values().length];

        static {
            try {
                $SwitchMap$org$bouncycastle$crypto$util$DERMacData$Type[Type.UNILATERALU.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$bouncycastle$crypto$util$DERMacData$Type[Type.BILATERALU.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$bouncycastle$crypto$util$DERMacData$Type[Type.UNILATERALV.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$bouncycastle$crypto$util$DERMacData$Type[Type.BILATERALV.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public static final class Builder {
        private ASN1OctetString ephemDataU;
        private ASN1OctetString ephemDataV;
        private ASN1OctetString idU;
        private ASN1OctetString idV;
        private byte[] text;
        private final Type type;

        public Builder(Type type2, byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4) {
            this.type = type2;
            this.idU = DerUtil.getOctetString(bArr);
            this.idV = DerUtil.getOctetString(bArr2);
            this.ephemDataU = DerUtil.getOctetString(bArr3);
            this.ephemDataV = DerUtil.getOctetString(bArr4);
        }

        private byte[] concatenate(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5, byte[] bArr6) {
            return Arrays.concatenate(Arrays.concatenate(bArr, bArr2, bArr3), Arrays.concatenate(bArr4, bArr5, bArr6));
        }

        public DERMacData build() {
            int i = AnonymousClass1.$SwitchMap$org$bouncycastle$crypto$util$DERMacData$Type[this.type.ordinal()];
            if (i == 1 || i == 2) {
                return new DERMacData(concatenate(this.type.getHeader(), DerUtil.toByteArray(this.idU), DerUtil.toByteArray(this.idV), DerUtil.toByteArray(this.ephemDataU), DerUtil.toByteArray(this.ephemDataV), this.text), null);
            }
            if (i == 3 || i == 4) {
                return new DERMacData(concatenate(this.type.getHeader(), DerUtil.toByteArray(this.idV), DerUtil.toByteArray(this.idU), DerUtil.toByteArray(this.ephemDataV), DerUtil.toByteArray(this.ephemDataU), this.text), null);
            }
            throw new IllegalStateException("Unknown type encountered in build");
        }

        public Builder withText(byte[] bArr) {
            this.text = DerUtil.toByteArray(new DERTaggedObject(false, 0, DerUtil.getOctetString(bArr)));
            return this;
        }
    }

    public enum Type {
        UNILATERALU("KC_1_U"),
        UNILATERALV("KC_1_V"),
        BILATERALU("KC_2_U"),
        BILATERALV("KC_2_V");
        
        private final String enc;

        private Type(String str) {
            this.enc = str;
        }

        public byte[] getHeader() {
            return Strings.toByteArray(this.enc);
        }
    }

    private DERMacData(byte[] bArr) {
        this.macData = bArr;
    }

    /* synthetic */ DERMacData(byte[] bArr, AnonymousClass1 r2) {
        this(bArr);
    }

    public byte[] getMacData() {
        return Arrays.clone(this.macData);
    }
}
