package com.android.org.bouncycastle.jcajce.util;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import java.io.IOException;
import java.security.AlgorithmParameters;

public class AlgorithmParametersUtils {
    private AlgorithmParametersUtils() {
    }

    public static ASN1Encodable extractParameters(AlgorithmParameters params) throws IOException {
        try {
            return ASN1Primitive.fromByteArray(params.getEncoded("ASN.1"));
        } catch (Exception e) {
            return ASN1Primitive.fromByteArray(params.getEncoded());
        }
    }

    public static void loadParameters(AlgorithmParameters params, ASN1Encodable sParams) throws IOException {
        try {
            params.init(sParams.toASN1Primitive().getEncoded(), "ASN.1");
        } catch (Exception e) {
            params.init(sParams.toASN1Primitive().getEncoded());
        }
    }
}
