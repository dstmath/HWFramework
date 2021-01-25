package org.bouncycastle.asn1.pkcs;

import java.math.BigInteger;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.Arrays;

public class PBKDF2Params extends ASN1Object {
    private static final AlgorithmIdentifier algid_hmacWithSHA1 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA1, DERNull.INSTANCE);
    private final ASN1Integer iterationCount;
    private final ASN1Integer keyLength;
    private final ASN1OctetString octStr;
    private final AlgorithmIdentifier prf;

    private PBKDF2Params(ASN1Sequence aSN1Sequence) {
        Object obj;
        Enumeration objects = aSN1Sequence.getObjects();
        this.octStr = (ASN1OctetString) objects.nextElement();
        this.iterationCount = (ASN1Integer) objects.nextElement();
        if (objects.hasMoreElements()) {
            Object nextElement = objects.nextElement();
            if (nextElement instanceof ASN1Integer) {
                this.keyLength = ASN1Integer.getInstance(nextElement);
                obj = objects.hasMoreElements() ? objects.nextElement() : null;
            } else {
                this.keyLength = null;
                obj = nextElement;
            }
            if (obj != null) {
                this.prf = AlgorithmIdentifier.getInstance(obj);
                return;
            }
        } else {
            this.keyLength = null;
        }
        this.prf = null;
    }

    public PBKDF2Params(byte[] bArr, int i) {
        this(bArr, i, 0);
    }

    public PBKDF2Params(byte[] bArr, int i, int i2) {
        this(bArr, i, i2, null);
    }

    public PBKDF2Params(byte[] bArr, int i, int i2, AlgorithmIdentifier algorithmIdentifier) {
        this.octStr = new DEROctetString(Arrays.clone(bArr));
        this.iterationCount = new ASN1Integer((long) i);
        this.keyLength = i2 > 0 ? new ASN1Integer((long) i2) : null;
        this.prf = algorithmIdentifier;
    }

    public PBKDF2Params(byte[] bArr, int i, AlgorithmIdentifier algorithmIdentifier) {
        this(bArr, i, 0, algorithmIdentifier);
    }

    public static PBKDF2Params getInstance(Object obj) {
        if (obj instanceof PBKDF2Params) {
            return (PBKDF2Params) obj;
        }
        if (obj != null) {
            return new PBKDF2Params(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public BigInteger getIterationCount() {
        return this.iterationCount.getValue();
    }

    public BigInteger getKeyLength() {
        ASN1Integer aSN1Integer = this.keyLength;
        if (aSN1Integer != null) {
            return aSN1Integer.getValue();
        }
        return null;
    }

    public AlgorithmIdentifier getPrf() {
        AlgorithmIdentifier algorithmIdentifier = this.prf;
        return algorithmIdentifier != null ? algorithmIdentifier : algid_hmacWithSHA1;
    }

    public byte[] getSalt() {
        return this.octStr.getOctets();
    }

    public boolean isDefaultPrf() {
        AlgorithmIdentifier algorithmIdentifier = this.prf;
        return algorithmIdentifier == null || algorithmIdentifier.equals(algid_hmacWithSHA1);
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(4);
        aSN1EncodableVector.add(this.octStr);
        aSN1EncodableVector.add(this.iterationCount);
        ASN1Integer aSN1Integer = this.keyLength;
        if (aSN1Integer != null) {
            aSN1EncodableVector.add(aSN1Integer);
        }
        AlgorithmIdentifier algorithmIdentifier = this.prf;
        if (algorithmIdentifier != null && !algorithmIdentifier.equals(algid_hmacWithSHA1)) {
            aSN1EncodableVector.add(this.prf);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
