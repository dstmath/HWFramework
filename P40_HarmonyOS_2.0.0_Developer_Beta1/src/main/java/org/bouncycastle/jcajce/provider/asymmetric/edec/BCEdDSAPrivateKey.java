package org.bouncycastle.jcajce.provider.asymmetric.edec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed448PrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey;
import org.bouncycastle.jcajce.interfaces.EdDSAPublicKey;
import org.bouncycastle.jcajce.spec.EdDSAParameterSpec;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Properties;

public class BCEdDSAPrivateKey implements EdDSAPrivateKey {
    static final long serialVersionUID = 1;
    private final byte[] attributes;
    transient AsymmetricKeyParameter eddsaPrivateKey;
    private final boolean hasPublicKey;

    BCEdDSAPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        this.hasPublicKey = privateKeyInfo.hasPublicKey();
        this.attributes = privateKeyInfo.getAttributes() != null ? privateKeyInfo.getAttributes().getEncoded() : null;
        populateFromPrivateKeyInfo(privateKeyInfo);
    }

    BCEdDSAPrivateKey(AsymmetricKeyParameter asymmetricKeyParameter) {
        this.hasPublicKey = true;
        this.attributes = null;
        this.eddsaPrivateKey = asymmetricKeyParameter;
    }

    private void populateFromPrivateKeyInfo(PrivateKeyInfo privateKeyInfo) throws IOException {
        ASN1Encodable parsePrivateKey = privateKeyInfo.parsePrivateKey();
        this.eddsaPrivateKey = EdECObjectIdentifiers.id_Ed448.equals(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm()) ? new Ed448PrivateKeyParameters(ASN1OctetString.getInstance(parsePrivateKey).getOctets(), 0) : new Ed25519PrivateKeyParameters(ASN1OctetString.getInstance(parsePrivateKey).getOctets(), 0);
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        populateFromPrivateKeyInfo(PrivateKeyInfo.getInstance((byte[]) objectInputStream.readObject()));
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(getEncoded());
    }

    /* access modifiers changed from: package-private */
    public AsymmetricKeyParameter engineGetKeyParameters() {
        return this.eddsaPrivateKey;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PrivateKey)) {
            return false;
        }
        return Arrays.areEqual(((PrivateKey) obj).getEncoded(), getEncoded());
    }

    @Override // java.security.Key
    public String getAlgorithm() {
        return this.eddsaPrivateKey instanceof Ed448PrivateKeyParameters ? EdDSAParameterSpec.Ed448 : EdDSAParameterSpec.Ed25519;
    }

    @Override // java.security.Key
    public byte[] getEncoded() {
        try {
            ASN1Set instance = ASN1Set.getInstance(this.attributes);
            PrivateKeyInfo createPrivateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(this.eddsaPrivateKey, instance);
            return (!this.hasPublicKey || Properties.isOverrideSet("org.bouncycastle.pkcs8.v1_info_only")) ? new PrivateKeyInfo(createPrivateKeyInfo.getPrivateKeyAlgorithm(), createPrivateKeyInfo.parsePrivateKey(), instance).getEncoded() : createPrivateKeyInfo.getEncoded();
        } catch (IOException e) {
            return null;
        }
    }

    @Override // java.security.Key
    public String getFormat() {
        return "PKCS#8";
    }

    @Override // org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey
    public EdDSAPublicKey getPublicKey() {
        AsymmetricKeyParameter asymmetricKeyParameter = this.eddsaPrivateKey;
        return asymmetricKeyParameter instanceof Ed448PrivateKeyParameters ? new BCEdDSAPublicKey(((Ed448PrivateKeyParameters) asymmetricKeyParameter).generatePublicKey()) : new BCEdDSAPublicKey(((Ed25519PrivateKeyParameters) asymmetricKeyParameter).generatePublicKey());
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Arrays.hashCode(getEncoded());
    }

    @Override // java.lang.Object
    public String toString() {
        AsymmetricKeyParameter asymmetricKeyParameter = this.eddsaPrivateKey;
        return Utils.keyToString("Private Key", getAlgorithm(), asymmetricKeyParameter instanceof Ed448PrivateKeyParameters ? ((Ed448PrivateKeyParameters) asymmetricKeyParameter).generatePublicKey() : ((Ed25519PrivateKeyParameters) asymmetricKeyParameter).generatePublicKey());
    }
}
