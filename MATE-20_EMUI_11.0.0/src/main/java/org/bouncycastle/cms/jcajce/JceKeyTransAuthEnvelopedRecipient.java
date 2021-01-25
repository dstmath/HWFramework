package org.bouncycastle.cms.jcajce;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import javax.crypto.Cipher;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientOperator;
import org.bouncycastle.jcajce.io.CipherInputStream;
import org.bouncycastle.operator.InputAEADDecryptor;

public class JceKeyTransAuthEnvelopedRecipient extends JceKeyTransRecipient {

    private static class AADStream extends OutputStream {
        private Cipher cipher;
        private byte[] oneByte = new byte[1];

        public AADStream(Cipher cipher2) {
            this.cipher = cipher2;
        }

        @Override // java.io.OutputStream
        public void write(int i) throws IOException {
            byte[] bArr = this.oneByte;
            bArr[0] = (byte) i;
            this.cipher.updateAAD(bArr);
        }

        @Override // java.io.OutputStream
        public void write(byte[] bArr, int i, int i2) throws IOException {
            this.cipher.updateAAD(bArr, i, i2);
        }
    }

    public JceKeyTransAuthEnvelopedRecipient(PrivateKey privateKey) {
        super(privateKey);
    }

    @Override // org.bouncycastle.cms.KeyTransRecipient
    public RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, final AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CMSException {
        final Cipher createContentCipher = this.contentHelper.createContentCipher(extractSecretKey(algorithmIdentifier, algorithmIdentifier2, bArr), algorithmIdentifier2);
        return new RecipientOperator(new InputAEADDecryptor() {
            /* class org.bouncycastle.cms.jcajce.JceKeyTransAuthEnvelopedRecipient.AnonymousClass1 */

            @Override // org.bouncycastle.operator.AADProcessor
            public OutputStream getAADStream() {
                return new AADStream(createContentCipher);
            }

            @Override // org.bouncycastle.operator.InputDecryptor
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return algorithmIdentifier2;
            }

            @Override // org.bouncycastle.operator.InputDecryptor
            public InputStream getInputStream(InputStream inputStream) {
                return new CipherInputStream(inputStream, createContentCipher);
            }

            @Override // org.bouncycastle.operator.AADProcessor
            public byte[] getMAC() {
                return new byte[0];
            }
        });
    }
}
