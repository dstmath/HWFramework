package org.bouncycastle.jcajce.provider.asymmetric.ies;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.jce.spec.IESParameterSpec;

public class AlgorithmParametersSpi extends java.security.AlgorithmParametersSpi {
    IESParameterSpec currentSpec;

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded() {
        try {
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            if (this.currentSpec.getDerivationV() != null) {
                aSN1EncodableVector.add(new DERTaggedObject(false, 0, new DEROctetString(this.currentSpec.getDerivationV())));
            }
            if (this.currentSpec.getEncodingV() != null) {
                aSN1EncodableVector.add(new DERTaggedObject(false, 1, new DEROctetString(this.currentSpec.getEncodingV())));
            }
            aSN1EncodableVector.add(new ASN1Integer((long) this.currentSpec.getMacKeySize()));
            if (this.currentSpec.getNonce() != null) {
                ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
                aSN1EncodableVector2.add(new ASN1Integer((long) this.currentSpec.getCipherKeySize()));
                aSN1EncodableVector2.add(new ASN1Integer(this.currentSpec.getNonce()));
                aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
            }
            return new DERSequence(aSN1EncodableVector).getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new RuntimeException("Error encoding IESParameters");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded(String str) {
        if (isASN1FormatString(str) || str.equalsIgnoreCase("X.509")) {
            return engineGetEncoded();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameterSpec engineGetParameterSpec(Class cls) throws InvalidParameterSpecException {
        if (cls != null) {
            return localEngineGetParameterSpec(cls);
        }
        throw new NullPointerException("argument to getParameterSpec must not be null");
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException {
        if (algorithmParameterSpec instanceof IESParameterSpec) {
            this.currentSpec = (IESParameterSpec) algorithmParameterSpec;
            return;
        }
        throw new InvalidParameterSpecException("IESParameterSpec required to initialise a IES algorithm parameters object");
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bArr) throws IOException {
        try {
            ASN1Sequence aSN1Sequence = (ASN1Sequence) ASN1Primitive.fromByteArray(bArr);
            if (aSN1Sequence.size() == 1) {
                this.currentSpec = new IESParameterSpec(null, null, ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0)).getValue().intValue());
            } else if (aSN1Sequence.size() == 2) {
                ASN1TaggedObject instance = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(0));
                this.currentSpec = instance.getTagNo() == 0 ? new IESParameterSpec(ASN1OctetString.getInstance(instance, false).getOctets(), null, ASN1Integer.getInstance(aSN1Sequence.getObjectAt(1)).getValue().intValue()) : new IESParameterSpec(null, ASN1OctetString.getInstance(instance, false).getOctets(), ASN1Integer.getInstance(aSN1Sequence.getObjectAt(1)).getValue().intValue());
            } else if (aSN1Sequence.size() == 3) {
                this.currentSpec = new IESParameterSpec(ASN1OctetString.getInstance(ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(0)), false).getOctets(), ASN1OctetString.getInstance(ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(1)), false).getOctets(), ASN1Integer.getInstance(aSN1Sequence.getObjectAt(2)).getValue().intValue());
            } else {
                if (aSN1Sequence.size() == 4) {
                    ASN1TaggedObject instance2 = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(0));
                    ASN1TaggedObject instance3 = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(1));
                    ASN1Sequence instance4 = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(3));
                    IESParameterSpec iESParameterSpec = new IESParameterSpec(ASN1OctetString.getInstance(instance2, false).getOctets(), ASN1OctetString.getInstance(instance3, false).getOctets(), ASN1Integer.getInstance(aSN1Sequence.getObjectAt(2)).getValue().intValue(), ASN1Integer.getInstance(instance4.getObjectAt(0)).getValue().intValue(), ASN1OctetString.getInstance(instance4.getObjectAt(1)).getOctets());
                    this.currentSpec = iESParameterSpec;
                }
            }
        } catch (ClassCastException e) {
            throw new IOException("Not a valid IES Parameter encoding.");
        } catch (ArrayIndexOutOfBoundsException e2) {
            throw new IOException("Not a valid IES Parameter encoding.");
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bArr, String str) throws IOException {
        if (isASN1FormatString(str) || str.equalsIgnoreCase("X.509")) {
            engineInit(bArr);
            return;
        }
        throw new IOException("Unknown parameter format " + str);
    }

    /* access modifiers changed from: protected */
    public String engineToString() {
        return "IES Parameters";
    }

    /* access modifiers changed from: protected */
    public boolean isASN1FormatString(String str) {
        return str == null || str.equals("ASN.1");
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameterSpec localEngineGetParameterSpec(Class cls) throws InvalidParameterSpecException {
        if (cls == IESParameterSpec.class || cls == AlgorithmParameterSpec.class) {
            return this.currentSpec;
        }
        throw new InvalidParameterSpecException("unknown parameter spec passed to ElGamal parameters object.");
    }
}
