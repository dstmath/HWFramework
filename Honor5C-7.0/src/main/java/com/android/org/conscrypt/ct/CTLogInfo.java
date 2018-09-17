package com.android.org.conscrypt.ct;

import com.android.org.conscrypt.ct.VerifiedSCT.Status;
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

    public CTLogInfo(PublicKey publicKey, String description, String url) {
        try {
            this.logId = MessageDigest.getInstance("SHA-256").digest(publicKey.getEncoded());
            this.publicKey = publicKey;
            this.description = description;
            this.url = url;
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
        boolean z = false;
        if (this == other) {
            return true;
        }
        if (!(other instanceof CTLogInfo)) {
            return false;
        }
        CTLogInfo that = (CTLogInfo) other;
        if (this.publicKey.equals(that.publicKey) && this.description.equals(that.description)) {
            z = this.url.equals(that.url);
        }
        return z;
    }

    public int hashCode() {
        return ((((this.publicKey.hashCode() + 31) * 31) + this.description.hashCode()) * 31) + this.url.hashCode();
    }

    public Status verifySingleSCT(SignedCertificateTimestamp sct, CertificateEntry entry) {
        if (!Arrays.equals(sct.getLogID(), getID())) {
            return Status.UNKNOWN_LOG;
        }
        try {
            byte[] toVerify = sct.encodeTBS(entry);
            try {
                Signature signature = Signature.getInstance(sct.getSignature().getAlgorithm());
                try {
                    signature.initVerify(this.publicKey);
                    try {
                        signature.update(toVerify);
                        if (signature.verify(sct.getSignature().getSignature())) {
                            return Status.VALID;
                        }
                        return Status.INVALID_SIGNATURE;
                    } catch (SignatureException e) {
                        throw new RuntimeException(e);
                    }
                } catch (InvalidKeyException e2) {
                    return Status.INVALID_SCT;
                }
            } catch (NoSuchAlgorithmException e3) {
                return Status.INVALID_SCT;
            }
        } catch (SerializationException e4) {
            return Status.INVALID_SCT;
        }
    }
}
