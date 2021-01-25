package org.bouncycastle.pqc.crypto.lms;

import java.io.IOException;
import org.bouncycastle.util.Encodable;

/* access modifiers changed from: package-private */
public class LMSSignedPubKey implements Encodable {
    private final LMSPublicKeyParameters publicKey;
    private final LMSSignature signature;

    public LMSSignedPubKey(LMSSignature lMSSignature, LMSPublicKeyParameters lMSPublicKeyParameters) {
        this.signature = lMSSignature;
        this.publicKey = lMSPublicKeyParameters;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LMSSignedPubKey lMSSignedPubKey = (LMSSignedPubKey) obj;
        LMSSignature lMSSignature = this.signature;
        if (lMSSignature == null ? lMSSignedPubKey.signature != null : !lMSSignature.equals(lMSSignedPubKey.signature)) {
            return false;
        }
        LMSPublicKeyParameters lMSPublicKeyParameters = this.publicKey;
        LMSPublicKeyParameters lMSPublicKeyParameters2 = lMSSignedPubKey.publicKey;
        return lMSPublicKeyParameters != null ? lMSPublicKeyParameters.equals(lMSPublicKeyParameters2) : lMSPublicKeyParameters2 == null;
    }

    @Override // org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        return Composer.compose().bytes(this.signature.getEncoded()).bytes(this.publicKey.getEncoded()).build();
    }

    public LMSPublicKeyParameters getPublicKey() {
        return this.publicKey;
    }

    public LMSSignature getSignature() {
        return this.signature;
    }

    public int hashCode() {
        LMSSignature lMSSignature = this.signature;
        int i = 0;
        int hashCode = (lMSSignature != null ? lMSSignature.hashCode() : 0) * 31;
        LMSPublicKeyParameters lMSPublicKeyParameters = this.publicKey;
        if (lMSPublicKeyParameters != null) {
            i = lMSPublicKeyParameters.hashCode();
        }
        return hashCode + i;
    }
}
