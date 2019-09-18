package org.bouncycastle.crypto.ec;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

public class ECElGamalDecryptor implements ECDecryptor {
    private ECPrivateKeyParameters key;

    public ECPoint decrypt(ECPair eCPair) {
        if (this.key != null) {
            ECCurve curve = this.key.getParameters().getCurve();
            return ECAlgorithms.cleanPoint(curve, eCPair.getY()).subtract(ECAlgorithms.cleanPoint(curve, eCPair.getX()).multiply(this.key.getD())).normalize();
        }
        throw new IllegalStateException("ECElGamalDecryptor not initialised");
    }

    public void init(CipherParameters cipherParameters) {
        if (cipherParameters instanceof ECPrivateKeyParameters) {
            this.key = (ECPrivateKeyParameters) cipherParameters;
            return;
        }
        throw new IllegalArgumentException("ECPrivateKeyParameters are required for decryption.");
    }
}
