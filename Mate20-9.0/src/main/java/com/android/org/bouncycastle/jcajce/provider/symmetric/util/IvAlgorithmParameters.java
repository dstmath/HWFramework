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

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded() throws IOException {
        return engineGetEncoded("ASN.1");
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetEncoded(String format) throws IOException {
        if (isASN1FormatString(format)) {
            return new DEROctetString(engineGetEncoded("RAW")).getEncoded();
        }
        if (format.equals("RAW")) {
            return Arrays.clone(this.iv);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameterSpec localEngineGetParameterSpec(Class paramSpec) throws InvalidParameterSpecException {
        if (paramSpec == IvParameterSpec.class || paramSpec == AlgorithmParameterSpec.class) {
            return new IvParameterSpec(this.iv);
        }
        throw new InvalidParameterSpecException("unknown parameter spec passed to IV parameters object.");
    }

    /* access modifiers changed from: protected */
    public void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        if (paramSpec instanceof IvParameterSpec) {
            this.iv = ((IvParameterSpec) paramSpec).getIV();
            return;
        }
        throw new InvalidParameterSpecException("IvParameterSpec required to initialise a IV parameters algorithm parameters object");
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] params) throws IOException {
        if (params.length % 8 != 0 && params[0] == 4 && params[1] == params.length - 2) {
            params = ((ASN1OctetString) ASN1Primitive.fromByteArray(params)).getOctets();
        }
        this.iv = Arrays.clone(params);
    }

    /* access modifiers changed from: protected */
    public void engineInit(byte[] params, String format) throws IOException {
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

    /* access modifiers changed from: protected */
    public String engineToString() {
        return "IV Parameters";
    }
}
