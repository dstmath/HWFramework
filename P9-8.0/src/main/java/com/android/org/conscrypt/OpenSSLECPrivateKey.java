package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef.EC_GROUP;
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

public final class OpenSSLECPrivateKey implements ECPrivateKey, OpenSSLKeyHolder {
    private static final String ALGORITHM = "EC";
    private static final long serialVersionUID = -4036633595001083922L;
    protected transient OpenSSLECGroupContext group;
    protected transient OpenSSLKey key;

    public OpenSSLECPrivateKey(OpenSSLECGroupContext group, OpenSSLKey key) {
        this.group = group;
        this.key = key;
    }

    public OpenSSLECPrivateKey(OpenSSLKey key) {
        this.group = new OpenSSLECGroupContext(new EC_GROUP(NativeCrypto.EC_KEY_get1_group(key.getNativeRef())));
        this.key = key;
    }

    public OpenSSLECPrivateKey(ECPrivateKeySpec ecKeySpec) throws InvalidKeySpecException {
        try {
            this.group = OpenSSLECGroupContext.getInstance(ecKeySpec.getParams());
            this.key = new OpenSSLKey(NativeCrypto.EVP_PKEY_new_EC_KEY(this.group.getNativeRef(), null, ecKeySpec.getS().toByteArray()));
        } catch (Exception e) {
            throw new InvalidKeySpecException(e);
        }
    }

    public static OpenSSLKey wrapPlatformKey(ECPrivateKey ecPrivateKey) throws InvalidKeyException {
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
        if (params == null) {
            throw new InvalidKeyException("EC parameters not available: " + privateKey);
        }
        try {
            return new OpenSSLKey(NativeCrypto.getECPrivateKeyWrapper(privateKey, OpenSSLECGroupContext.getInstance(params).getNativeRef()), true);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException("Invalid EC parameters: " + params);
        }
    }

    private static OpenSSLKey wrapPlatformKey(ECPrivateKey ecPrivateKey, OpenSSLECGroupContext group) throws InvalidKeyException {
        return new OpenSSLKey(NativeCrypto.getECPrivateKeyWrapper(ecPrivateKey, group.getNativeRef()), true);
    }

    public static OpenSSLKey getInstance(ECPrivateKey ecPrivateKey) throws InvalidKeyException {
        try {
            OpenSSLECGroupContext group = OpenSSLECGroupContext.getInstance(ecPrivateKey.getParams());
            if (ecPrivateKey.getFormat() == null) {
                return wrapPlatformKey(ecPrivateKey, group);
            }
            return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_EC_KEY(group.getNativeRef(), null, ecPrivateKey.getS().toByteArray()));
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
        return NativeCrypto.i2d_PKCS8_PRIV_KEY_INFO(this.key.getNativeRef());
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
        } else if (!(o instanceof ECPrivateKey)) {
            return false;
        } else {
            ECPrivateKey other = (ECPrivateKey) o;
            if (!getPrivateKey().equals(other.getS())) {
                return false;
            }
            ECParameterSpec spec = getParams();
            ECParameterSpec otherSpec = other.getParams();
            if (!spec.getCurve().equals(otherSpec.getCurve()) || !spec.getGenerator().equals(otherSpec.getGenerator()) || !spec.getOrder().equals(otherSpec.getOrder())) {
                z = false;
            } else if (spec.getCofactor() != otherSpec.getCofactor()) {
                z = false;
            }
            return z;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(NativeCrypto.i2d_PKCS8_PRIV_KEY_INFO(this.key.getNativeRef()));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("OpenSSLECPrivateKey{");
        sb.append("params={");
        sb.append(NativeCrypto.EVP_PKEY_print_params(this.key.getNativeRef()));
        sb.append("}}");
        return sb.toString();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.key = new OpenSSLKey(NativeCrypto.d2i_PKCS8_PRIV_KEY_INFO((byte[]) stream.readObject()));
        this.group = new OpenSSLECGroupContext(new EC_GROUP(NativeCrypto.EC_KEY_get1_group(this.key.getNativeRef())));
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(getEncoded());
    }
}
