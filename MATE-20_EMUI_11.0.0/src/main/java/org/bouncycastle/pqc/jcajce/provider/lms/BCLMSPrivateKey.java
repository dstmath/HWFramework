package org.bouncycastle.pqc.jcajce.provider.lms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.util.PrivateKeyFactory;
import org.bouncycastle.pqc.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.pqc.jcajce.interfaces.LMSPrivateKey;
import org.bouncycastle.util.Arrays;

public class BCLMSPrivateKey implements PrivateKey, LMSPrivateKey {
    private static final long serialVersionUID = 8568701712864512338L;
    private transient ASN1Set attributes;
    private transient LMSKeyParameters keyParams;

    public BCLMSPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        init(privateKeyInfo);
    }

    public BCLMSPrivateKey(LMSKeyParameters lMSKeyParameters) {
        this.keyParams = lMSKeyParameters;
    }

    private void init(PrivateKeyInfo privateKeyInfo) throws IOException {
        this.attributes = privateKeyInfo.getAttributes();
        this.keyParams = (LMSKeyParameters) PrivateKeyFactory.createKey(privateKeyInfo);
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        init(PrivateKeyInfo.getInstance((byte[]) objectInputStream.readObject()));
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(getEncoded());
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BCLMSPrivateKey)) {
            return false;
        }
        try {
            return Arrays.areEqual(this.keyParams.getEncoded(), ((BCLMSPrivateKey) obj).keyParams.getEncoded());
        } catch (IOException e) {
            throw new IllegalStateException("unable to perform equals");
        }
    }

    @Override // org.bouncycastle.pqc.jcajce.interfaces.LMSPrivateKey
    public LMSPrivateKey extractKeyShard(int i) {
        LMSKeyParameters lMSKeyParameters = this.keyParams;
        return lMSKeyParameters instanceof LMSPrivateKeyParameters ? new BCLMSPrivateKey(((LMSPrivateKeyParameters) lMSKeyParameters).extractKeyShard(i)) : new BCLMSPrivateKey(((HSSPrivateKeyParameters) lMSKeyParameters).extractKeyShard(i));
    }

    @Override // java.security.Key
    public String getAlgorithm() {
        return "LMS";
    }

    @Override // java.security.Key
    public byte[] getEncoded() {
        try {
            return PrivateKeyInfoFactory.createPrivateKeyInfo(this.keyParams, this.attributes).getEncoded();
        } catch (IOException e) {
            return null;
        }
    }

    @Override // java.security.Key
    public String getFormat() {
        return "PKCS#8";
    }

    @Override // org.bouncycastle.pqc.jcajce.interfaces.LMSPrivateKey
    public long getIndex() {
        if (getUsagesRemaining() != 0) {
            LMSKeyParameters lMSKeyParameters = this.keyParams;
            return lMSKeyParameters instanceof LMSPrivateKeyParameters ? (long) ((LMSPrivateKeyParameters) lMSKeyParameters).getIndex() : ((HSSPrivateKeyParameters) lMSKeyParameters).getIndex();
        }
        throw new IllegalStateException("key exhausted");
    }

    /* access modifiers changed from: package-private */
    public CipherParameters getKeyParams() {
        return this.keyParams;
    }

    @Override // org.bouncycastle.pqc.jcajce.interfaces.LMSKey
    public int getLevels() {
        LMSKeyParameters lMSKeyParameters = this.keyParams;
        if (lMSKeyParameters instanceof LMSPrivateKeyParameters) {
            return 1;
        }
        return ((HSSPrivateKeyParameters) lMSKeyParameters).getL();
    }

    @Override // org.bouncycastle.pqc.jcajce.interfaces.LMSPrivateKey
    public long getUsagesRemaining() {
        LMSKeyParameters lMSKeyParameters = this.keyParams;
        return lMSKeyParameters instanceof LMSPrivateKeyParameters ? ((LMSPrivateKeyParameters) lMSKeyParameters).getUsagesRemaining() : ((HSSPrivateKeyParameters) lMSKeyParameters).getUsagesRemaining();
    }

    @Override // java.lang.Object
    public int hashCode() {
        try {
            return Arrays.hashCode(this.keyParams.getEncoded());
        } catch (IOException e) {
            throw new IllegalStateException("unable to calculate hashCode");
        }
    }
}
