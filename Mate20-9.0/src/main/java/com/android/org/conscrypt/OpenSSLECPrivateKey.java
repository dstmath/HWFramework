package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef;
import com.android.org.conscrypt.OpenSSLX509CertificateFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

final class OpenSSLECPrivateKey implements ECPrivateKey, OpenSSLKeyHolder {
    private static final String ALGORITHM = "EC";
    private static final long serialVersionUID = -4036633595001083922L;
    protected transient OpenSSLECGroupContext group;
    protected transient OpenSSLKey key;

    OpenSSLECPrivateKey(OpenSSLECGroupContext group2, OpenSSLKey key2) {
        this.group = group2;
        this.key = key2;
    }

    OpenSSLECPrivateKey(OpenSSLKey key2) {
        this.group = new OpenSSLECGroupContext(new NativeRef.EC_GROUP(NativeCrypto.EC_KEY_get1_group(key2.getNativeRef())));
        this.key = key2;
    }

    OpenSSLECPrivateKey(ECPrivateKeySpec ecKeySpec) throws InvalidKeySpecException {
        try {
            this.group = OpenSSLECGroupContext.getInstance(ecKeySpec.getParams());
            this.key = new OpenSSLKey(NativeCrypto.EVP_PKEY_new_EC_KEY(this.group.getNativeRef(), null, ecKeySpec.getS().toByteArray()));
        } catch (Exception e) {
            throw new InvalidKeySpecException(e);
        }
    }

    static OpenSSLKey wrapPlatformKey(ECPrivateKey ecPrivateKey) throws InvalidKeyException {
        try {
            return wrapPlatformKey(ecPrivateKey, OpenSSLECGroupContext.getInstance(ecPrivateKey.getParams()));
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException("Unknown group parameters", e);
        }
    }

    static OpenSSLKey wrapJCAPrivateKeyForTLSStackOnly(PrivateKey privateKey, PublicKey publicKey) throws InvalidKeyException {
        ECParameterSpec params = null;
        if (privateKey instanceof ECKey) {
            params = ((ECKey) privateKey).getParams();
        } else if (publicKey instanceof ECKey) {
            params = ((ECKey) publicKey).getParams();
        }
        if (params != null) {
            return wrapJCAPrivateKeyForTLSStackOnly(privateKey, params);
        }
        throw new InvalidKeyException("EC parameters not available. Private: " + privateKey + ", public: " + publicKey);
    }

    static OpenSSLKey wrapJCAPrivateKeyForTLSStackOnly(PrivateKey privateKey, ECParameterSpec params) throws InvalidKeyException {
        if (params == null && (privateKey instanceof ECKey)) {
            params = ((ECKey) privateKey).getParams();
        }
        if (params != null) {
            try {
                return new OpenSSLKey(NativeCrypto.getECPrivateKeyWrapper(privateKey, OpenSSLECGroupContext.getInstance(params).getNativeRef()), true);
            } catch (InvalidAlgorithmParameterException e) {
                throw new InvalidKeyException("Invalid EC parameters: " + params);
            }
        } else {
            throw new InvalidKeyException("EC parameters not available: " + privateKey);
        }
    }

    private static OpenSSLKey wrapPlatformKey(ECPrivateKey ecPrivateKey, OpenSSLECGroupContext group2) throws InvalidKeyException {
        return new OpenSSLKey(NativeCrypto.getECPrivateKeyWrapper(ecPrivateKey, group2.getNativeRef()), true);
    }

    static OpenSSLKey getInstance(ECPrivateKey ecPrivateKey) throws InvalidKeyException {
        try {
            OpenSSLECGroupContext group2 = OpenSSLECGroupContext.getInstance(ecPrivateKey.getParams());
            if (ecPrivateKey.getFormat() == null) {
                return wrapPlatformKey(ecPrivateKey, group2);
            }
            return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_EC_KEY(group2.getNativeRef(), null, ecPrivateKey.getS().toByteArray()));
        } catch (Exception e) {
            throw new InvalidKeyException(e);
        }
    }

    public String getAlgorithm() {
        return ALGORITHM;
    }

    public String getFormat() {
        return "PKCS#8";
    }

    public byte[] getEncoded() {
        return NativeCrypto.EVP_marshal_private_key(this.key.getNativeRef());
    }

    public ECParameterSpec getParams() {
        return this.group.getECParameterSpec();
    }

    public BigInteger getS() {
        return getPrivateKey();
    }

    private BigInteger getPrivateKey() {
        return new BigInteger(NativeCrypto.EC_KEY_get_private_key(this.key.getNativeRef()));
    }

    public OpenSSLKey getOpenSSLKey() {
        return this.key;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (o instanceof OpenSSLECPrivateKey) {
            return this.key.equals(((OpenSSLECPrivateKey) o).key);
        }
        if (!(o instanceof ECPrivateKey)) {
            return false;
        }
        ECPrivateKey other = (ECPrivateKey) o;
        if (!getPrivateKey().equals(other.getS())) {
            return false;
        }
        ECParameterSpec spec = getParams();
        ECParameterSpec otherSpec = other.getParams();
        if (!spec.getCurve().equals(otherSpec.getCurve()) || !spec.getGenerator().equals(otherSpec.getGenerator()) || !spec.getOrder().equals(otherSpec.getOrder()) || spec.getCofactor() != otherSpec.getCofactor()) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Arrays.hashCode(NativeCrypto.EVP_marshal_private_key(this.key.getNativeRef()));
    }

    public String toString() {
        return "OpenSSLECPrivateKey{" + "params={" + NativeCrypto.EVP_PKEY_print_params(this.key.getNativeRef()) + "}}";
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        try {
            this.key = new OpenSSLKey(NativeCrypto.EVP_parse_private_key((byte[]) stream.readObject()));
            this.group = new OpenSSLECGroupContext(new NativeRef.EC_GROUP(NativeCrypto.EC_KEY_get1_group(this.key.getNativeRef())));
        } catch (OpenSSLX509CertificateFactory.ParsingException e) {
            throw new IOException(e);
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(getEncoded());
    }
}
