package libcore.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Set;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;

public class RecoverySystem {
    private RecoverySystem() {
    }

    public static void verify(InputStream blockStream, InputStream contentStream, Set<X509Certificate> trustedCerts) throws IOException, SignatureException, NoSuchAlgorithmException {
        PKCS7 block = new PKCS7(blockStream);
        X509Certificate[] certificates = block.getCertificates();
        if (certificates == null || certificates.length == 0) {
            throw new SignatureException("signature contains no certificates");
        }
        PublicKey signatureKey = certificates[0].getPublicKey();
        SignerInfo[] signerInfos = block.getSignerInfos();
        if (signerInfos == null || signerInfos.length == 0) {
            throw new SignatureException("signature contains no signedData");
        }
        SignerInfo signerInfo = signerInfos[0];
        boolean verified = false;
        for (X509Certificate c : trustedCerts) {
            if (c.getPublicKey().equals(signatureKey)) {
                verified = true;
                break;
            }
        }
        if (!verified) {
            throw new SignatureException("signature doesn't match any trusted key");
        } else if (block.verify(signerInfo, contentStream) == null) {
            throw new SignatureException("signature digest verification failed");
        }
    }
}
