package com.android.org.conscrypt;

import com.android.org.conscrypt.OpenSSLX509CertificateFactory;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public final class InternalUtil {
    public static PublicKey logKeyToPublicKey(byte[] logKey) throws NoSuchAlgorithmException {
        try {
            return new OpenSSLKey(NativeCrypto.EVP_parse_public_key(logKey)).getPublicKey();
        } catch (OpenSSLX509CertificateFactory.ParsingException e) {
            throw new NoSuchAlgorithmException(e);
        }
    }

    public static PublicKey readPublicKeyPem(InputStream pem) throws InvalidKeyException, NoSuchAlgorithmException {
        return OpenSSLKey.fromPublicKeyPemInputStream(pem).getPublicKey();
    }

    public static byte[] getOcspSingleExtension(byte[] ocspResponse, String oid, OpenSSLX509Certificate x509, OpenSSLX509Certificate issuerX509) {
        return NativeCrypto.get_ocsp_single_extension(ocspResponse, oid, x509.getContext(), x509, issuerX509.getContext(), issuerX509);
    }

    private InternalUtil() {
    }
}
