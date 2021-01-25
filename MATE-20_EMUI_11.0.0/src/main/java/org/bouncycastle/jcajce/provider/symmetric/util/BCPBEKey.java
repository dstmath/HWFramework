package org.bouncycastle.jcajce.provider.symmetric.util;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.Destroyable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class BCPBEKey implements PBEKey, Destroyable {
    String algorithm;
    int digest;
    private final AtomicBoolean hasBeenDestroyed = new AtomicBoolean(false);
    private final int iterationCount;
    int ivSize;
    int keySize;
    ASN1ObjectIdentifier oid;
    private final CipherParameters param;
    private final char[] password;
    private final byte[] salt;
    boolean tryWrong = false;
    int type;

    public BCPBEKey(String str, ASN1ObjectIdentifier aSN1ObjectIdentifier, int i, int i2, int i3, int i4, PBEKeySpec pBEKeySpec, CipherParameters cipherParameters) {
        this.algorithm = str;
        this.oid = aSN1ObjectIdentifier;
        this.type = i;
        this.digest = i2;
        this.keySize = i3;
        this.ivSize = i4;
        this.password = pBEKeySpec.getPassword();
        this.iterationCount = pBEKeySpec.getIterationCount();
        this.salt = pBEKeySpec.getSalt();
        this.param = cipherParameters;
    }

    public BCPBEKey(String str, CipherParameters cipherParameters) {
        this.algorithm = str;
        this.param = cipherParameters;
        this.password = null;
        this.iterationCount = -1;
        this.salt = null;
    }

    static void checkDestroyed(Destroyable destroyable) {
        if (destroyable.isDestroyed()) {
            throw new IllegalStateException("key has been destroyed");
        }
    }

    @Override // javax.security.auth.Destroyable
    public void destroy() {
        if (!this.hasBeenDestroyed.getAndSet(true)) {
            char[] cArr = this.password;
            if (cArr != null) {
                Arrays.fill(cArr, (char) 0);
            }
            byte[] bArr = this.salt;
            if (bArr != null) {
                Arrays.fill(bArr, (byte) 0);
            }
        }
    }

    @Override // java.security.Key
    public String getAlgorithm() {
        checkDestroyed(this);
        return this.algorithm;
    }

    /* access modifiers changed from: package-private */
    public int getDigest() {
        checkDestroyed(this);
        return this.digest;
    }

    @Override // java.security.Key
    public byte[] getEncoded() {
        checkDestroyed(this);
        CipherParameters cipherParameters = this.param;
        if (cipherParameters != null) {
            if (cipherParameters instanceof ParametersWithIV) {
                cipherParameters = ((ParametersWithIV) cipherParameters).getParameters();
            }
            return ((KeyParameter) cipherParameters).getKey();
        }
        int i = this.type;
        return i == 2 ? PBEParametersGenerator.PKCS12PasswordToBytes(this.password) : i == 5 ? PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(this.password) : PBEParametersGenerator.PKCS5PasswordToBytes(this.password);
    }

    @Override // java.security.Key
    public String getFormat() {
        return "RAW";
    }

    @Override // javax.crypto.interfaces.PBEKey
    public int getIterationCount() {
        checkDestroyed(this);
        return this.iterationCount;
    }

    public int getIvSize() {
        checkDestroyed(this);
        return this.ivSize;
    }

    /* access modifiers changed from: package-private */
    public int getKeySize() {
        checkDestroyed(this);
        return this.keySize;
    }

    public ASN1ObjectIdentifier getOID() {
        checkDestroyed(this);
        return this.oid;
    }

    public CipherParameters getParam() {
        checkDestroyed(this);
        return this.param;
    }

    @Override // javax.crypto.interfaces.PBEKey
    public char[] getPassword() {
        checkDestroyed(this);
        char[] cArr = this.password;
        if (cArr != null) {
            return Arrays.clone(cArr);
        }
        throw new IllegalStateException("no password available");
    }

    @Override // javax.crypto.interfaces.PBEKey
    public byte[] getSalt() {
        checkDestroyed(this);
        return Arrays.clone(this.salt);
    }

    /* access modifiers changed from: package-private */
    public int getType() {
        checkDestroyed(this);
        return this.type;
    }

    @Override // javax.security.auth.Destroyable
    public boolean isDestroyed() {
        return this.hasBeenDestroyed.get();
    }

    public void setTryWrongPKCS12Zero(boolean z) {
        this.tryWrong = z;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldTryWrongPKCS12() {
        return this.tryWrong;
    }
}
