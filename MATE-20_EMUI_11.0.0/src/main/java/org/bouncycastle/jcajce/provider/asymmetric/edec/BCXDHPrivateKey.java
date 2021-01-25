package org.bouncycastle.jcajce.provider.asymmetric.edec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X448PrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.jcajce.interfaces.XDHPrivateKey;
import org.bouncycastle.jcajce.interfaces.XDHPublicKey;
import org.bouncycastle.jcajce.spec.XDHParameterSpec;
import org.bouncycastle.util.Arrays;

public class BCXDHPrivateKey implements XDHPrivateKey {
    static final long serialVersionUID = 1;
    private final byte[] attributes;
    private final boolean hasPublicKey;
    protected transient AsymmetricKeyParameter xdhPrivateKey;

    BCXDHPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        this.hasPublicKey = privateKeyInfo.hasPublicKey();
        this.attributes = privateKeyInfo.getAttributes() != null ? privateKeyInfo.getAttributes().getEncoded() : null;
        populateFromPrivateKeyInfo(privateKeyInfo);
    }

    BCXDHPrivateKey(AsymmetricKeyParameter asymmetricKeyParameter) {
        this.hasPublicKey = true;
        this.attributes = null;
        this.xdhPrivateKey = asymmetricKeyParameter;
    }

    private void populateFromPrivateKeyInfo(PrivateKeyInfo privateKeyInfo) throws IOException {
        ASN1Encodable parsePrivateKey = privateKeyInfo.parsePrivateKey();
        this.xdhPrivateKey = EdECObjectIdentifiers.id_X448.equals(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm()) ? new X448PrivateKeyParameters(ASN1OctetString.getInstance(parsePrivateKey).getOctets(), 0) : new X25519PrivateKeyParameters(ASN1OctetString.getInstance(parsePrivateKey).getOctets(), 0);
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
        return this.xdhPrivateKey;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BCXDHPrivateKey)) {
            return false;
        }
        return Arrays.areEqual(((BCXDHPrivateKey) obj).getEncoded(), getEncoded());
    }

    @Override // java.security.Key
    public String getAlgorithm() {
        return this.xdhPrivateKey instanceof X448PrivateKeyParameters ? XDHParameterSpec.X448 : XDHParameterSpec.X25519;
    }

    @Override // java.security.Key
    public byte[] getEncoded() {
        try {
            ASN1Set instance = ASN1Set.getInstance(this.attributes);
            PrivateKeyInfo createPrivateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(this.xdhPrivateKey, instance);
            return this.hasPublicKey ? createPrivateKeyInfo.getEncoded() : new PrivateKeyInfo(createPrivateKeyInfo.getPrivateKeyAlgorithm(), createPrivateKeyInfo.parsePrivateKey(), instance).getEncoded();
        } catch (IOException e) {
            return null;
        }
    }

    @Override // java.security.Key
    public String getFormat() {
        return "PKCS#8";
    }

    @Override // org.bouncycastle.jcajce.interfaces.XDHPrivateKey
    public XDHPublicKey getPublicKey() {
        AsymmetricKeyParameter asymmetricKeyParameter = this.xdhPrivateKey;
        return asymmetricKeyParameter instanceof X448PrivateKeyParameters ? new BCXDHPublicKey(((X448PrivateKeyParameters) asymmetricKeyParameter).generatePublicKey()) : new BCXDHPublicKey(((X25519PrivateKeyParameters) asymmetricKeyParameter).generatePublicKey());
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Arrays.hashCode(getEncoded());
    }

    @Override // java.lang.Object
    public String toString() {
        AsymmetricKeyParameter asymmetricKeyParameter = this.xdhPrivateKey;
        return Utils.keyToString("Private Key", getAlgorithm(), asymmetricKeyParameter instanceof X448PrivateKeyParameters ? ((X448PrivateKeyParameters) asymmetricKeyParameter).generatePublicKey() : ((X25519PrivateKeyParameters) asymmetricKeyParameter).generatePublicKey());
    }
}
