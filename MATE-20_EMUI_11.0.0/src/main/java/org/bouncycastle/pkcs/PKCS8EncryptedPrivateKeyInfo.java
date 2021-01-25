package org.bouncycastle.pkcs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.util.io.Streams;

public class PKCS8EncryptedPrivateKeyInfo {
    private EncryptedPrivateKeyInfo encryptedPrivateKeyInfo;

    public PKCS8EncryptedPrivateKeyInfo(EncryptedPrivateKeyInfo encryptedPrivateKeyInfo2) {
        this.encryptedPrivateKeyInfo = encryptedPrivateKeyInfo2;
    }

    public PKCS8EncryptedPrivateKeyInfo(byte[] bArr) throws IOException {
        this(parseBytes(bArr));
    }

    private static EncryptedPrivateKeyInfo parseBytes(byte[] bArr) throws IOException {
        try {
            return EncryptedPrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(bArr));
        } catch (ClassCastException e) {
            throw new PKCSIOException("malformed data: " + e.getMessage(), e);
        } catch (IllegalArgumentException e2) {
            throw new PKCSIOException("malformed data: " + e2.getMessage(), e2);
        }
    }

    public PrivateKeyInfo decryptPrivateKeyInfo(InputDecryptorProvider inputDecryptorProvider) throws PKCSException {
        try {
            return PrivateKeyInfo.getInstance(Streams.readAll(inputDecryptorProvider.get(this.encryptedPrivateKeyInfo.getEncryptionAlgorithm()).getInputStream(new ByteArrayInputStream(this.encryptedPrivateKeyInfo.getEncryptedData()))));
        } catch (Exception e) {
            throw new PKCSException("unable to read encrypted data: " + e.getMessage(), e);
        }
    }

    public byte[] getEncoded() throws IOException {
        return this.encryptedPrivateKeyInfo.getEncoded();
    }

    public byte[] getEncryptedData() {
        return this.encryptedPrivateKeyInfo.getEncryptedData();
    }

    public AlgorithmIdentifier getEncryptionAlgorithm() {
        return this.encryptedPrivateKeyInfo.getEncryptionAlgorithm();
    }

    public EncryptedPrivateKeyInfo toASN1Structure() {
        return this.encryptedPrivateKeyInfo;
    }
}
