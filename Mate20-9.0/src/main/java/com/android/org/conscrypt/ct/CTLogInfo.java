package com.android.org.conscrypt.ct;

import com.android.org.conscrypt.ct.VerifiedSCT;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

public class CTLogInfo {
    private final String description;
    private final byte[] logId;
    private final PublicKey publicKey;
    private final String url;

    public CTLogInfo(PublicKey publicKey2, String description2, String url2) {
        try {
            this.logId = MessageDigest.getInstance("SHA-256").digest(publicKey2.getEncoded());
            this.publicKey = publicKey2;
            this.description = description2;
            this.url = url2;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getID() {
        return this.logId;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public String getDescription() {
        return this.description;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof CTLogInfo)) {
            return false;
        }
        CTLogInfo that = (CTLogInfo) other;
        if (!this.publicKey.equals(that.publicKey) || !this.description.equals(that.description) || !this.url.equals(that.url)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (((((1 * 31) + this.publicKey.hashCode()) * 31) + this.description.hashCode()) * 31) + this.url.hashCode();
    }

    public VerifiedSCT.Status verifySingleSCT(SignedCertificateTimestamp sct, CertificateEntry entry) {
        if (!Arrays.equals(sct.getLogID(), getID())) {
            return VerifiedSCT.Status.UNKNOWN_LOG;
        }
        try {
            byte[] toVerify = sct.encodeTBS(entry);
            try {
                Signature signature = Signature.getInstance(sct.getSignature().getAlgorithm());
                try {
                    signature.initVerify(this.publicKey);
                    try {
                        signature.update(toVerify);
                        if (!signature.verify(sct.getSignature().getSignature())) {
                            return VerifiedSCT.Status.INVALID_SIGNATURE;
                        }
                        return VerifiedSCT.Status.VALID;
                    } catch (SignatureException e) {
                        throw new RuntimeException(e);
                    }
                } catch (InvalidKeyException e2) {
                    return VerifiedSCT.Status.INVALID_SCT;
                }
            } catch (NoSuchAlgorithmException e3) {
                return VerifiedSCT.Status.INVALID_SCT;
            }
        } catch (SerializationException e4) {
            return VerifiedSCT.Status.INVALID_SCT;
        }
    }
}
