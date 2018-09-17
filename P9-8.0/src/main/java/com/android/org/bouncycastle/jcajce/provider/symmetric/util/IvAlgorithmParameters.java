package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.IvParameterSpec;

public class IvAlgorithmParameters extends BaseAlgorithmParameters {
    private byte[] iv;

    protected byte[] engineGetEncoded() throws IOException {
        return engineGetEncoded("ASN.1");
    }

    protected byte[] engineGetEncoded(String format) throws IOException {
        if (isASN1FormatString(format)) {
            return new DEROctetString(engineGetEncoded("RAW")).getEncoded();
        }
        if (format.equals("RAW")) {
            return Arrays.clone(this.iv);
        }
        return null;
    }

    protected AlgorithmParameterSpec localEngineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
        if (paramSpec == IvParameterSpec.class || paramSpec == AlgorithmParameterSpec.class) {
            return new IvParameterSpec(this.iv);
        }
        throw new InvalidParameterSpecException("unknown parameter spec passed to IV parameters object.");
    }

    protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        if (paramSpec instanceof IvParameterSpec) {
            this.iv = ((IvParameterSpec) paramSpec).getIV();
            return;
        }
        throw new InvalidParameterSpecException("IvParameterSpec required to initialise a IV parameters algorithm parameters object");
    }

    protected void engineInit(byte[] params) throws IOException {
        if (params.length % 8 != 0 && params[0] == (byte) 4 && params[1] == params.length - 2) {
            params = ((ASN1OctetString) ASN1Primitive.fromByteArray(params)).getOctets();
        }
        this.iv = Arrays.clone(params);
    }

    protected void engineInit(byte[] params, String format) throws IOException {
        if (isASN1FormatString(format)) {
            try {
                engineInit(((ASN1OctetString) ASN1Primitive.fromByteArray(params)).getOctets());
            } catch (Exception e) {
                throw new IOException("Exception decoding: " + e);
            }
        } else if (format.equals("RAW")) {
            engineInit(params);
        } else {
            throw new IOException("Unknown parameters format in IV parameters object");
        }
    }

    protected String engineToString() {
        return "IV Parameters";
    }
}
