package javax.security.cert;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;

public abstract class Certificate {
    public abstract byte[] getEncoded() throws CertificateEncodingException;

    public abstract PublicKey getPublicKey();

    public abstract String toString();

    public abstract void verify(PublicKey publicKey) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException;

    public abstract void verify(PublicKey publicKey, String str) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException;

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Certificate)) {
            return false;
        }
        try {
            byte[] thisCert = getEncoded();
            byte[] otherCert = ((Certificate) other).getEncoded();
            if (thisCert.length != otherCert.length) {
                return false;
            }
            for (int i = 0; i < thisCert.length; i++) {
                if (thisCert[i] != otherCert[i]) {
                    return false;
                }
            }
            return true;
        } catch (CertificateException e) {
            return false;
        }
    }

    public int hashCode() {
        int retval = 0;
        try {
            byte[] certData = getEncoded();
            for (int i = 1; i < certData.length; i++) {
                retval += certData[i] * i;
            }
            return retval;
        } catch (CertificateException e) {
            return 0;
        }
    }
}
