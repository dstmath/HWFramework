package org.bouncycastle.jcajce.provider.asymmetric.edec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.Ed448PublicKeyParameters;
import org.bouncycastle.jcajce.interfaces.EdDSAPublicKey;
import org.bouncycastle.jcajce.spec.EdDSAParameterSpec;
import org.bouncycastle.util.Arrays;

public class BCEdDSAPublicKey implements EdDSAPublicKey {
    static final long serialVersionUID = 1;
    private transient AsymmetricKeyParameter eddsaPublicKey;

    BCEdDSAPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        populateFromPubKeyInfo(subjectPublicKeyInfo);
    }

    BCEdDSAPublicKey(AsymmetricKeyParameter asymmetricKeyParameter) {
        this.eddsaPublicKey = asymmetricKeyParameter;
    }

    BCEdDSAPublicKey(byte[] bArr, byte[] bArr2) throws InvalidKeySpecException {
        AsymmetricKeyParameter ed25519PublicKeyParameters;
        int length = bArr.length;
        if (Utils.isValidPrefix(bArr, bArr2)) {
            if (bArr2.length - length == 57) {
                ed25519PublicKeyParameters = new Ed448PublicKeyParameters(bArr2, length);
            } else if (bArr2.length - length == 32) {
                ed25519PublicKeyParameters = new Ed25519PublicKeyParameters(bArr2, length);
            } else {
                throw new InvalidKeySpecException("raw key data not recognised");
            }
            this.eddsaPublicKey = ed25519PublicKeyParameters;
            return;
        }
        throw new InvalidKeySpecException("raw key data not recognised");
    }

    private void populateFromPubKeyInfo(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this.eddsaPublicKey = EdECObjectIdentifiers.id_Ed448.equals(subjectPublicKeyInfo.getAlgorithm().getAlgorithm()) ? new Ed448PublicKeyParameters(subjectPublicKeyInfo.getPublicKeyData().getOctets(), 0) : new Ed25519PublicKeyParameters(subjectPublicKeyInfo.getPublicKeyData().getOctets(), 0);
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        populateFromPubKeyInfo(SubjectPublicKeyInfo.getInstance((byte[]) objectInputStream.readObject()));
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(getEncoded());
    }

    /* access modifiers changed from: package-private */
    public AsymmetricKeyParameter engineGetKeyParameters() {
        return this.eddsaPublicKey;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BCEdDSAPublicKey)) {
            return false;
        }
        return Arrays.areEqual(((BCEdDSAPublicKey) obj).getEncoded(), getEncoded());
    }

    @Override // java.security.Key
    public String getAlgorithm() {
        return this.eddsaPublicKey instanceof Ed448PublicKeyParameters ? EdDSAParameterSpec.Ed448 : EdDSAParameterSpec.Ed25519;
    }

    @Override // java.security.Key
    public byte[] getEncoded() {
        if (this.eddsaPublicKey instanceof Ed448PublicKeyParameters) {
            byte[] bArr = new byte[(KeyFactorySpi.Ed448Prefix.length + 57)];
            System.arraycopy(KeyFactorySpi.Ed448Prefix, 0, bArr, 0, KeyFactorySpi.Ed448Prefix.length);
            ((Ed448PublicKeyParameters) this.eddsaPublicKey).encode(bArr, KeyFactorySpi.Ed448Prefix.length);
            return bArr;
        }
        byte[] bArr2 = new byte[(KeyFactorySpi.Ed25519Prefix.length + 32)];
        System.arraycopy(KeyFactorySpi.Ed25519Prefix, 0, bArr2, 0, KeyFactorySpi.Ed25519Prefix.length);
        ((Ed25519PublicKeyParameters) this.eddsaPublicKey).encode(bArr2, KeyFactorySpi.Ed25519Prefix.length);
        return bArr2;
    }

    @Override // java.security.Key
    public String getFormat() {
        return "X.509";
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Arrays.hashCode(getEncoded());
    }

    @Override // java.lang.Object
    public String toString() {
        return Utils.keyToString("Public Key", getAlgorithm(), this.eddsaPublicKey);
    }
}
