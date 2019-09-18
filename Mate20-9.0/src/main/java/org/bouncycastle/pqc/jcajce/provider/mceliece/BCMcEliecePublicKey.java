package org.bouncycastle.pqc.jcajce.provider.mceliece;

import java.io.IOException;
import java.security.PublicKey;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.asn1.McEliecePublicKey;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.crypto.mceliece.McEliecePublicKeyParameters;
import org.bouncycastle.pqc.math.linearalgebra.GF2Matrix;

public class BCMcEliecePublicKey implements PublicKey {
    private static final long serialVersionUID = 1;
    private McEliecePublicKeyParameters params;

    public BCMcEliecePublicKey(McEliecePublicKeyParameters mcEliecePublicKeyParameters) {
        this.params = mcEliecePublicKeyParameters;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BCMcEliecePublicKey)) {
            return false;
        }
        BCMcEliecePublicKey bCMcEliecePublicKey = (BCMcEliecePublicKey) obj;
        return this.params.getN() == bCMcEliecePublicKey.getN() && this.params.getT() == bCMcEliecePublicKey.getT() && this.params.getG().equals(bCMcEliecePublicKey.getG());
    }

    public String getAlgorithm() {
        return "McEliece";
    }

    public byte[] getEncoded() {
        try {
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.mcEliece), (ASN1Encodable) new McEliecePublicKey(this.params.getN(), this.params.getT(), this.params.getG())).getEncoded();
        } catch (IOException e) {
            return null;
        }
    }

    public String getFormat() {
        return "X.509";
    }

    public GF2Matrix getG() {
        return this.params.getG();
    }

    public int getK() {
        return this.params.getK();
    }

    /* access modifiers changed from: package-private */
    public AsymmetricKeyParameter getKeyParams() {
        return this.params;
    }

    public int getN() {
        return this.params.getN();
    }

    public int getT() {
        return this.params.getT();
    }

    public int hashCode() {
        return (37 * (this.params.getN() + (this.params.getT() * 37))) + this.params.getG().hashCode();
    }

    public String toString() {
        String str = "McEliecePublicKey:\n" + " length of the code         : " + this.params.getN() + "\n";
        String str2 = str + " error correction capability: " + this.params.getT() + "\n";
        return str2 + " generator matrix           : " + this.params.getG();
    }
}
