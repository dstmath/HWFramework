package org.bouncycastle.jcajce.provider.asymmetric.dsa;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.DSAParameter;

public class AlgorithmParametersSpi extends java.security.AlgorithmParametersSpi {
    DSAParameterSpec currentSpec;

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded() {
        try {
            return new DSAParameter(this.currentSpec.getP(), this.currentSpec.getQ(), this.currentSpec.getG()).getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new RuntimeException("Error encoding DSAParameters");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded(String str) {
        if (isASN1FormatString(str)) {
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
        if (algorithmParameterSpec instanceof DSAParameterSpec) {
            this.currentSpec = (DSAParameterSpec) algorithmParameterSpec;
            return;
        }
        throw new InvalidParameterSpecException("DSAParameterSpec required to initialise a DSA algorithm parameters object");
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] bArr) throws IOException {
        try {
            DSAParameter instance = DSAParameter.getInstance(ASN1Primitive.fromByteArray(bArr));
            this.currentSpec = new DSAParameterSpec(instance.getP(), instance.getQ(), instance.getG());
        } catch (ClassCastException e) {
            throw new IOException("Not a valid DSA Parameter encoding.");
        } catch (ArrayIndexOutOfBoundsException e2) {
            throw new IOException("Not a valid DSA Parameter encoding.");
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
        return "DSA Parameters";
    }

    /* access modifiers changed from: protected */
    public boolean isASN1FormatString(String str) {
        return str == null || str.equals("ASN.1");
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameterSpec localEngineGetParameterSpec(Class cls) throws InvalidParameterSpecException {
        if (cls == DSAParameterSpec.class || cls == AlgorithmParameterSpec.class) {
            return this.currentSpec;
        }
        throw new InvalidParameterSpecException("unknown parameter spec passed to DSA parameters object.");
    }
}
