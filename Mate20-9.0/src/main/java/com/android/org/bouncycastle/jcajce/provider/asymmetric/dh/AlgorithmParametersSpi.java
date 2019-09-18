package com.android.org.bouncycastle.jcajce.provider.asymmetric.dh;

import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.pkcs.DHParameter;
import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.DHParameterSpec;

public class AlgorithmParametersSpi extends java.security.AlgorithmParametersSpi {
    DHParameterSpec currentSpec;

    /* access modifiers changed from: protected */
    public boolean isASN1FormatString(String format) {
        return format == null || format.equals("ASN.1");
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
        if (paramSpec != null) {
            return localEngineGetParameterSpec(paramSpec);
        }
        throw new NullPointerException("argument to getParameterSpec must not be null");
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded() {
        try {
            return new DHParameter(this.currentSpec.getP(), this.currentSpec.getG(), this.currentSpec.getL()).getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new RuntimeException("Error encoding DHParameters");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded(String format) {
        if (isASN1FormatString(format)) {
            return engineGetEncoded();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameterSpec localEngineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
        if (paramSpec == DHParameterSpec.class || paramSpec == AlgorithmParameterSpec.class) {
            return this.currentSpec;
        }
        throw new InvalidParameterSpecException("unknown parameter spec passed to DH parameters object.");
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        if (paramSpec instanceof DHParameterSpec) {
            this.currentSpec = (DHParameterSpec) paramSpec;
            return;
        }
        throw new InvalidParameterSpecException("DHParameterSpec required to initialise a Diffie-Hellman algorithm parameters object");
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] params) throws IOException {
        try {
            DHParameter dhP = DHParameter.getInstance(params);
            if (dhP.getL() != null) {
                this.currentSpec = new DHParameterSpec(dhP.getP(), dhP.getG(), dhP.getL().intValue());
            } else {
                this.currentSpec = new DHParameterSpec(dhP.getP(), dhP.getG());
            }
        } catch (ClassCastException e) {
            throw new IOException("Not a valid DH Parameter encoding.");
        } catch (ArrayIndexOutOfBoundsException e2) {
            throw new IOException("Not a valid DH Parameter encoding.");
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] params, String format) throws IOException {
        if (isASN1FormatString(format)) {
            engineInit(params);
            return;
        }
        throw new IOException("Unknown parameter format " + format);
    }

    /* access modifiers changed from: protected */
    public String engineToString() {
        return "Diffie-Hellman Parameters";
    }
}
