package java.security.cert;

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;
import sun.security.x509.X509CertImpl;

public abstract class Certificate implements Serializable {
    private static final long serialVersionUID = -3585440601605666277L;
    private final String type;

    protected static class CertificateRep implements Serializable {
        private static final long serialVersionUID = -8563758940495660020L;
        private byte[] data;
        private String type;

        protected CertificateRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        protected Object readResolve() throws ObjectStreamException {
            try {
                return CertificateFactory.getInstance(this.type).generateCertificate(new ByteArrayInputStream(this.data));
            } catch (CertificateException e) {
                throw new NotSerializableException("java.security.cert.Certificate: " + this.type + ": " + e.getMessage());
            }
        }
    }

    public abstract byte[] getEncoded() throws CertificateEncodingException;

    public abstract PublicKey getPublicKey();

    public abstract String toString();

    public abstract void verify(PublicKey publicKey) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException;

    public abstract void verify(PublicKey publicKey, String str) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException;

    protected Certificate(String type) {
        this.type = type;
    }

    public final String getType() {
        return this.type;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Certificate)) {
            return false;
        }
        try {
            return Arrays.equals(X509CertImpl.getEncodedInternal(this), X509CertImpl.getEncodedInternal((Certificate) other));
        } catch (CertificateException e) {
            return false;
        }
    }

    public int hashCode() {
        int retval = 0;
        try {
            byte[] certData = X509CertImpl.getEncodedInternal(this);
            for (int i = 1; i < certData.length; i++) {
                retval += certData[i] * i;
            }
            return retval;
        } catch (CertificateException e) {
            return 0;
        }
    }

    protected Object writeReplace() throws ObjectStreamException {
        try {
            return new CertificateRep(this.type, getEncoded());
        } catch (CertificateException e) {
            throw new NotSerializableException("java.security.cert.Certificate: " + this.type + ": " + e.getMessage());
        }
    }
}
