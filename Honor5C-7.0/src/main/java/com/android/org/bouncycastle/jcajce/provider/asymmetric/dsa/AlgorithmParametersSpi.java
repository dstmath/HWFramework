package com.android.org.bouncycastle.jcajce.provider.asymmetric.dsa;

import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.x509.DSAParameter;
import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;

public class AlgorithmParametersSpi extends java.security.AlgorithmParametersSpi {
    DSAParameterSpec currentSpec;

    protected boolean isASN1FormatString(String format) {
        return format != null ? format.equals("ASN.1") : true;
    }

    protected AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
        if (paramSpec != null) {
            return localEngineGetParameterSpec(paramSpec);
        }
        throw new NullPointerException("argument to getParameterSpec must not be null");
    }

    protected byte[] engineGetEncoded() {
        try {
            return new DSAParameter(this.currentSpec.getP(), this.currentSpec.getQ(), this.currentSpec.getG()).getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new RuntimeException("Error encoding DSAParameters");
        }
    }

    protected byte[] engineGetEncoded(String format) {
        if (isASN1FormatString(format)) {
            return engineGetEncoded();
        }
        return null;
    }

    protected AlgorithmParameterSpec localEngineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
        if (paramSpec == DSAParameterSpec.class || paramSpec == AlgorithmParameterSpec.class) {
            return this.currentSpec;
        }
        throw new InvalidParameterSpecException("unknown parameter spec passed to DSA parameters object.");
    }

    protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        if (paramSpec instanceof DSAParameterSpec) {
            this.currentSpec = (DSAParameterSpec) paramSpec;
            return;
        }
        throw new InvalidParameterSpecException("DSAParameterSpec required to initialise a DSA algorithm parameters object");
    }

    protected void engineInit(byte[] params) throws IOException {
        try {
            DSAParameter dsaP = DSAParameter.getInstance(ASN1Primitive.fromByteArray(params));
            this.currentSpec = new DSAParameterSpec(dsaP.getP(), dsaP.getQ(), dsaP.getG());
        } catch (ClassCastException e) {
            throw new IOException("Not a valid DSA Parameter encoding.");
        } catch (ArrayIndexOutOfBoundsException e2) {
            throw new IOException("Not a valid DSA Parameter encoding.");
        }
    }

    protected void engineInit(byte[] params, String format) throws IOException {
        if (isASN1FormatString(format) || format.equalsIgnoreCase("X.509")) {
            engineInit(params);
            return;
        }
        throw new IOException("Unknown parameter format " + format);
    }

    protected String engineToString() {
        return "DSA Parameters";
    }
}
