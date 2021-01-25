package org.bouncycastle.pkcs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.operator.OutputEncryptor;

public class PKCS8EncryptedPrivateKeyInfoBuilder {
    private PrivateKeyInfo privateKeyInfo;

    public PKCS8EncryptedPrivateKeyInfoBuilder(PrivateKeyInfo privateKeyInfo2) {
        this.privateKeyInfo = privateKeyInfo2;
    }

    public PKCS8EncryptedPrivateKeyInfoBuilder(byte[] bArr) {
        this(PrivateKeyInfo.getInstance(bArr));
    }

    public PKCS8EncryptedPrivateKeyInfo build(OutputEncryptor outputEncryptor) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OutputStream outputStream = outputEncryptor.getOutputStream(byteArrayOutputStream);
            outputStream.write(this.privateKeyInfo.getEncoded());
            outputStream.close();
            return new PKCS8EncryptedPrivateKeyInfo(new EncryptedPrivateKeyInfo(outputEncryptor.getAlgorithmIdentifier(), byteArrayOutputStream.toByteArray()));
        } catch (IOException e) {
            throw new IllegalStateException("cannot encode privateKeyInfo");
        }
    }
}
