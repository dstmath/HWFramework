package com.android.org.bouncycastle.asn1.pkcs;

import java.io.IOException;

public class PrivateKeyInfoEx {
    public static byte[] parsePrivateKeyEncoded(Object obj) throws IOException {
        return PrivateKeyInfo.getInstance(obj).parsePrivateKey().toASN1Primitive().getEncoded();
    }
}
