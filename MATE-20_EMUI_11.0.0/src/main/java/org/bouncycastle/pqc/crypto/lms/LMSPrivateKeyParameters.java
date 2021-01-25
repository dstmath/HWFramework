package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.ExhaustedPrivateKeyException;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public class LMSPrivateKeyParameters extends LMSKeyParameters {
    private static CacheKey T1 = new CacheKey(1);
    private static CacheKey[] internedKeys = new CacheKey[129];
    private final byte[] I;
    private final byte[] masterSecret;
    private int maxCacheR;
    private final int maxQ;
    private final LMOtsParameters otsParameters;
    private final LMSigParameters parameters;
    private LMSPublicKeyParameters publicKey;
    private int q;
    private Map<CacheKey, byte[]> tCache;
    private Digest tDigest;

    /* access modifiers changed from: private */
    public static class CacheKey {
        private final int index;

        CacheKey(int i) {
            this.index = i;
        }

        public boolean equals(Object obj) {
            return (obj instanceof CacheKey) && ((CacheKey) obj).index == this.index;
        }

        public int hashCode() {
            return this.index;
        }
    }

    static {
        internedKeys[1] = T1;
        int i = 2;
        while (true) {
            CacheKey[] cacheKeyArr = internedKeys;
            if (i < cacheKeyArr.length) {
                cacheKeyArr[i] = new CacheKey(i);
                i++;
            } else {
                return;
            }
        }
    }

    private LMSPrivateKeyParameters(LMSPrivateKeyParameters lMSPrivateKeyParameters, int i, int i2) {
        super(true);
        this.parameters = lMSPrivateKeyParameters.parameters;
        this.otsParameters = lMSPrivateKeyParameters.otsParameters;
        this.q = i;
        this.I = lMSPrivateKeyParameters.I;
        this.maxQ = i2;
        this.masterSecret = lMSPrivateKeyParameters.masterSecret;
        this.maxCacheR = 1 << this.parameters.getH();
        this.tCache = lMSPrivateKeyParameters.tCache;
        this.tDigest = DigestUtil.getDigest(this.parameters.getDigestOID());
        this.publicKey = lMSPrivateKeyParameters.publicKey;
    }

    public LMSPrivateKeyParameters(LMSigParameters lMSigParameters, LMOtsParameters lMOtsParameters, int i, byte[] bArr, int i2, byte[] bArr2) {
        super(true);
        this.parameters = lMSigParameters;
        this.otsParameters = lMOtsParameters;
        this.q = i;
        this.I = Arrays.clone(bArr);
        this.maxQ = i2;
        this.masterSecret = Arrays.clone(bArr2);
        this.maxCacheR = 1 << (this.parameters.getH() + 1);
        this.tCache = new WeakHashMap();
        this.tDigest = DigestUtil.getDigest(lMSigParameters.getDigestOID());
    }

    private byte[] calcT(int i) {
        int h = 1 << getSigParameters().getH();
        if (i >= h) {
            LmsUtils.byteArray(getI(), this.tDigest);
            LmsUtils.u32str(i, this.tDigest);
            LmsUtils.u16str(-32126, this.tDigest);
            LmsUtils.byteArray(LM_OTS.lms_ots_generatePublicKey(getOtsParameters(), getI(), i - h, getMasterSecret()), this.tDigest);
        } else {
            int i2 = i * 2;
            byte[] findT = findT(i2);
            byte[] findT2 = findT(i2 + 1);
            LmsUtils.byteArray(getI(), this.tDigest);
            LmsUtils.u32str(i, this.tDigest);
            LmsUtils.u16str(-31869, this.tDigest);
            LmsUtils.byteArray(findT, this.tDigest);
            LmsUtils.byteArray(findT2, this.tDigest);
        }
        byte[] bArr = new byte[this.tDigest.getDigestSize()];
        this.tDigest.doFinal(bArr, 0);
        return bArr;
    }

    private byte[] findT(CacheKey cacheKey) {
        synchronized (this.tCache) {
            byte[] bArr = this.tCache.get(cacheKey);
            if (bArr != null) {
                return bArr;
            }
            byte[] calcT = calcT(cacheKey.index);
            this.tCache.put(cacheKey, calcT);
            return calcT;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x0096  */
    public static LMSPrivateKeyParameters getInstance(Object obj) throws IOException {
        Throwable th;
        if (obj instanceof LMSPrivateKeyParameters) {
            return (LMSPrivateKeyParameters) obj;
        }
        if (obj instanceof DataInputStream) {
            DataInputStream dataInputStream = (DataInputStream) obj;
            if (dataInputStream.readInt() == 0) {
                LMSigParameters parametersForType = LMSigParameters.getParametersForType(dataInputStream.readInt());
                LMOtsParameters parametersForType2 = LMOtsParameters.getParametersForType(dataInputStream.readInt());
                byte[] bArr = new byte[16];
                dataInputStream.readFully(bArr);
                int readInt = dataInputStream.readInt();
                int readInt2 = dataInputStream.readInt();
                int readInt3 = dataInputStream.readInt();
                if (readInt3 < 0) {
                    throw new IllegalStateException("secret length less than zero");
                } else if (readInt3 <= dataInputStream.available()) {
                    byte[] bArr2 = new byte[readInt3];
                    dataInputStream.readFully(bArr2);
                    return new LMSPrivateKeyParameters(parametersForType, parametersForType2, readInt, bArr, readInt2, bArr2);
                } else {
                    throw new IOException("secret length exceeded " + dataInputStream.available());
                }
            } else {
                throw new IllegalStateException("expected version 0 lms private key");
            }
        } else if (obj instanceof byte[]) {
            DataInputStream dataInputStream2 = null;
            try {
                DataInputStream dataInputStream3 = new DataInputStream(new ByteArrayInputStream((byte[]) obj));
                try {
                    LMSPrivateKeyParameters instance = getInstance(dataInputStream3);
                    dataInputStream3.close();
                    return instance;
                } catch (Throwable th2) {
                    th = th2;
                    dataInputStream2 = dataInputStream3;
                    if (dataInputStream2 != null) {
                        dataInputStream2.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (dataInputStream2 != null) {
                }
                throw th;
            }
        } else if (obj instanceof InputStream) {
            return getInstance(Streams.readAll((InputStream) obj));
        } else {
            throw new IllegalArgumentException("cannot parse " + obj);
        }
    }

    public static LMSPrivateKeyParameters getInstance(byte[] bArr, byte[] bArr2) throws IOException {
        LMSPrivateKeyParameters instance = getInstance(bArr);
        instance.publicKey = LMSPublicKeyParameters.getInstance(bArr2);
        return instance;
    }

    public boolean equals(Object obj) {
        LMSPublicKeyParameters lMSPublicKeyParameters;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LMSPrivateKeyParameters lMSPrivateKeyParameters = (LMSPrivateKeyParameters) obj;
        if (this.q != lMSPrivateKeyParameters.q || this.maxQ != lMSPrivateKeyParameters.maxQ || !Arrays.areEqual(this.I, lMSPrivateKeyParameters.I)) {
            return false;
        }
        LMSigParameters lMSigParameters = this.parameters;
        if (lMSigParameters == null ? lMSPrivateKeyParameters.parameters != null : !lMSigParameters.equals(lMSPrivateKeyParameters.parameters)) {
            return false;
        }
        LMOtsParameters lMOtsParameters = this.otsParameters;
        if (lMOtsParameters == null ? lMSPrivateKeyParameters.otsParameters != null : !lMOtsParameters.equals(lMSPrivateKeyParameters.otsParameters)) {
            return false;
        }
        if (!Arrays.areEqual(this.masterSecret, lMSPrivateKeyParameters.masterSecret)) {
            return false;
        }
        LMSPublicKeyParameters lMSPublicKeyParameters2 = this.publicKey;
        if (lMSPublicKeyParameters2 == null || (lMSPublicKeyParameters = lMSPrivateKeyParameters.publicKey) == null) {
            return true;
        }
        return lMSPublicKeyParameters2.equals(lMSPublicKeyParameters);
    }

    public LMSPrivateKeyParameters extractKeyShard(int i) {
        LMSPrivateKeyParameters lMSPrivateKeyParameters;
        synchronized (this) {
            if (this.q + i < this.maxQ) {
                lMSPrivateKeyParameters = new LMSPrivateKeyParameters(this, this.q, this.q + i);
                this.q += i;
            } else {
                throw new IllegalArgumentException("usageCount exceeds usages remaining");
            }
        }
        return lMSPrivateKeyParameters;
    }

    /* access modifiers changed from: package-private */
    public byte[] findT(int i) {
        if (i >= this.maxCacheR) {
            return calcT(i);
        }
        CacheKey[] cacheKeyArr = internedKeys;
        return findT(i < cacheKeyArr.length ? cacheKeyArr[i] : new CacheKey(i));
    }

    /* access modifiers changed from: package-private */
    public LMOtsPrivateKey getCurrentOTSKey() {
        LMOtsPrivateKey lMOtsPrivateKey;
        synchronized (this) {
            if (this.q < this.maxQ) {
                lMOtsPrivateKey = new LMOtsPrivateKey(this.otsParameters, this.I, this.q, this.masterSecret);
            } else {
                throw new ExhaustedPrivateKeyException("ots private keys expired");
            }
        }
        return lMOtsPrivateKey;
    }

    @Override // org.bouncycastle.pqc.crypto.lms.LMSKeyParameters, org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        return Composer.compose().u32str(0).u32str(this.parameters.getType()).u32str(this.otsParameters.getType()).bytes(this.I).u32str(this.q).u32str(this.maxQ).u32str(this.masterSecret.length).bytes(this.masterSecret).build();
    }

    public byte[] getI() {
        return Arrays.clone(this.I);
    }

    public synchronized int getIndex() {
        return this.q;
    }

    public byte[] getMasterSecret() {
        return Arrays.clone(this.masterSecret);
    }

    /* access modifiers changed from: package-private */
    public LMOtsPrivateKey getNextOtsPrivateKey() {
        LMOtsPrivateKey lMOtsPrivateKey;
        synchronized (this) {
            if (this.q < this.maxQ) {
                lMOtsPrivateKey = new LMOtsPrivateKey(this.otsParameters, this.I, this.q, this.masterSecret);
                incIndex();
            } else {
                throw new ExhaustedPrivateKeyException("ots private key exhausted");
            }
        }
        return lMOtsPrivateKey;
    }

    public LMOtsParameters getOtsParameters() {
        return this.otsParameters;
    }

    public LMSPublicKeyParameters getPublicKey() {
        LMSPublicKeyParameters lMSPublicKeyParameters;
        synchronized (this) {
            if (this.publicKey == null) {
                this.publicKey = new LMSPublicKeyParameters(this.parameters, this.otsParameters, findT(T1), this.I);
            }
            lMSPublicKeyParameters = this.publicKey;
        }
        return lMSPublicKeyParameters;
    }

    public LMSigParameters getSigParameters() {
        return this.parameters;
    }

    public long getUsagesRemaining() {
        return (long) (this.maxQ - this.q);
    }

    public int hashCode() {
        int hashCode = ((this.q * 31) + Arrays.hashCode(this.I)) * 31;
        LMSigParameters lMSigParameters = this.parameters;
        int i = 0;
        int hashCode2 = (hashCode + (lMSigParameters != null ? lMSigParameters.hashCode() : 0)) * 31;
        LMOtsParameters lMOtsParameters = this.otsParameters;
        int hashCode3 = (((((hashCode2 + (lMOtsParameters != null ? lMOtsParameters.hashCode() : 0)) * 31) + this.maxQ) * 31) + Arrays.hashCode(this.masterSecret)) * 31;
        LMSPublicKeyParameters lMSPublicKeyParameters = this.publicKey;
        if (lMSPublicKeyParameters != null) {
            i = lMSPublicKeyParameters.hashCode();
        }
        return hashCode3 + i;
    }

    /* access modifiers changed from: package-private */
    public synchronized void incIndex() {
        this.q++;
    }
}
