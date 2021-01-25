package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public class HSSPrivateKeyParameters extends LMSKeyParameters {
    private long index = 0;
    private final long indexLimit;
    private final boolean isShard;
    private List<LMSPrivateKeyParameters> keys;
    private final int l;
    private List<LMSSignature> sig;

    public HSSPrivateKeyParameters(int i, List<LMSPrivateKeyParameters> list, List<LMSSignature> list2, long j, long j2) {
        super(true);
        this.l = i;
        this.keys = Collections.unmodifiableList(list);
        this.sig = Collections.unmodifiableList(list2);
        this.index = j;
        this.indexLimit = j2;
        this.isShard = false;
        resetKeyToIndex();
    }

    private HSSPrivateKeyParameters(int i, List<LMSPrivateKeyParameters> list, List<LMSSignature> list2, long j, long j2, boolean z) {
        super(true);
        this.l = i;
        this.keys = Collections.unmodifiableList(list);
        this.sig = Collections.unmodifiableList(list2);
        this.index = j;
        this.indexLimit = j2;
        this.isShard = z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x007a  */
    public static HSSPrivateKeyParameters getInstance(Object obj) throws IOException {
        Throwable th;
        if (obj instanceof HSSPrivateKeyParameters) {
            return (HSSPrivateKeyParameters) obj;
        }
        if (obj instanceof DataInputStream) {
            DataInputStream dataInputStream = (DataInputStream) obj;
            if (dataInputStream.readInt() == 0) {
                int readInt = dataInputStream.readInt();
                long readLong = dataInputStream.readLong();
                long readLong2 = dataInputStream.readLong();
                boolean readBoolean = dataInputStream.readBoolean();
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                for (int i = 0; i < readInt; i++) {
                    arrayList.add(LMSPrivateKeyParameters.getInstance(obj));
                }
                for (int i2 = 0; i2 < readInt - 1; i2++) {
                    arrayList2.add(LMSSignature.getInstance(obj));
                }
                return new HSSPrivateKeyParameters(readInt, arrayList, arrayList2, readLong, readLong2, readBoolean);
            }
            throw new IllegalStateException("unknown version for hss private key");
        } else if (obj instanceof byte[]) {
            DataInputStream dataInputStream2 = null;
            try {
                DataInputStream dataInputStream3 = new DataInputStream(new ByteArrayInputStream((byte[]) obj));
                try {
                    HSSPrivateKeyParameters instance = getInstance(dataInputStream3);
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

    private static HSSPrivateKeyParameters makeCopy(HSSPrivateKeyParameters hSSPrivateKeyParameters) {
        try {
            return getInstance(hSSPrivateKeyParameters.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    public Object clone() throws CloneNotSupportedException {
        return makeCopy(this);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HSSPrivateKeyParameters hSSPrivateKeyParameters = (HSSPrivateKeyParameters) obj;
        if (this.l == hSSPrivateKeyParameters.l && this.isShard == hSSPrivateKeyParameters.isShard && this.indexLimit == hSSPrivateKeyParameters.indexLimit && this.index == hSSPrivateKeyParameters.index && this.keys.equals(hSSPrivateKeyParameters.keys)) {
            return this.sig.equals(hSSPrivateKeyParameters.sig);
        }
        return false;
    }

    public HSSPrivateKeyParameters extractKeyShard(int i) {
        HSSPrivateKeyParameters makeCopy;
        synchronized (this) {
            long j = (long) i;
            if (getUsagesRemaining() >= j) {
                long j2 = this.index + j;
                long j3 = this.index;
                this.index += j;
                makeCopy = makeCopy(new HSSPrivateKeyParameters(this.l, new ArrayList(getKeys()), new ArrayList(getSig()), j3, j2, true));
                resetKeyToIndex();
            } else {
                throw new IllegalArgumentException("usageCount exceeds usages remaining in current leaf");
            }
        }
        return makeCopy;
    }

    @Override // org.bouncycastle.pqc.crypto.lms.LMSKeyParameters, org.bouncycastle.util.Encodable
    public synchronized byte[] getEncoded() throws IOException {
        Composer bool;
        bool = Composer.compose().u32str(0).u32str(this.l).u64str(this.index).u64str(this.indexLimit).bool(this.isShard);
        for (LMSPrivateKeyParameters lMSPrivateKeyParameters : this.keys) {
            bool.bytes(lMSPrivateKeyParameters);
        }
        for (LMSSignature lMSSignature : this.sig) {
            bool.bytes(lMSSignature);
        }
        return bool.build();
    }

    public synchronized long getIndex() {
        return this.index;
    }

    /* access modifiers changed from: package-private */
    public long getIndexLimit() {
        return this.indexLimit;
    }

    /* access modifiers changed from: package-private */
    public synchronized List<LMSPrivateKeyParameters> getKeys() {
        return this.keys;
    }

    public int getL() {
        return this.l;
    }

    public synchronized LMSParameters[] getLMSParameters() {
        LMSParameters[] lMSParametersArr;
        int size = this.keys.size();
        lMSParametersArr = new LMSParameters[size];
        for (int i = 0; i < size; i++) {
            LMSPrivateKeyParameters lMSPrivateKeyParameters = this.keys.get(i);
            lMSParametersArr[i] = new LMSParameters(lMSPrivateKeyParameters.getSigParameters(), lMSPrivateKeyParameters.getOtsParameters());
        }
        return lMSParametersArr;
    }

    public synchronized HSSPublicKeyParameters getPublicKey() {
        return new HSSPublicKeyParameters(this.l, getRootKey().getPublicKey());
    }

    /* access modifiers changed from: package-private */
    public LMSPrivateKeyParameters getRootKey() {
        return this.keys.get(0);
    }

    /* access modifiers changed from: package-private */
    public synchronized List<LMSSignature> getSig() {
        return this.sig;
    }

    public long getUsagesRemaining() {
        return this.indexLimit - this.index;
    }

    public int hashCode() {
        long j = this.indexLimit;
        long j2 = this.index;
        return (((((((((this.l * 31) + (this.isShard ? 1 : 0)) * 31) + this.keys.hashCode()) * 31) + this.sig.hashCode()) * 31) + ((int) (j ^ (j >>> 32)))) * 31) + ((int) (j2 ^ (j2 >>> 32)));
    }

    /* access modifiers changed from: package-private */
    public synchronized void incIndex() {
        this.index++;
    }

    /* access modifiers changed from: package-private */
    public boolean isShard() {
        return this.isShard;
    }

    /* access modifiers changed from: package-private */
    public void replaceConsumedKey(int i) {
        int i2 = i - 1;
        SeedDerive derivationFunction = this.keys.get(i2).getCurrentOTSKey().getDerivationFunction();
        derivationFunction.setJ(-2);
        byte[] bArr = new byte[32];
        derivationFunction.deriveSeed(bArr, true);
        byte[] bArr2 = new byte[32];
        derivationFunction.deriveSeed(bArr2, false);
        byte[] bArr3 = new byte[16];
        System.arraycopy(bArr2, 0, bArr3, 0, bArr3.length);
        ArrayList arrayList = new ArrayList(this.keys);
        LMSPrivateKeyParameters lMSPrivateKeyParameters = this.keys.get(i);
        arrayList.set(i, LMS.generateKeys(lMSPrivateKeyParameters.getSigParameters(), lMSPrivateKeyParameters.getOtsParameters(), 0, bArr3, bArr));
        ArrayList arrayList2 = new ArrayList(this.sig);
        arrayList2.set(i2, LMS.generateSign((LMSPrivateKeyParameters) arrayList.get(i2), ((LMSPrivateKeyParameters) arrayList.get(i)).getPublicKey().toByteArray()));
        this.keys = Collections.unmodifiableList(arrayList);
        this.sig = Collections.unmodifiableList(arrayList2);
    }

    /* access modifiers changed from: package-private */
    public void resetKeyToIndex() {
        boolean z;
        List<LMSPrivateKeyParameters> keys2 = getKeys();
        long[] jArr = new long[keys2.size()];
        long index2 = getIndex();
        for (int size = keys2.size() - 1; size >= 0; size--) {
            LMSigParameters sigParameters = keys2.get(size).getSigParameters();
            jArr[size] = ((long) ((1 << sigParameters.getH()) - 1)) & index2;
            index2 >>>= sigParameters.getH();
        }
        LMSPrivateKeyParameters[] lMSPrivateKeyParametersArr = (LMSPrivateKeyParameters[]) keys2.toArray(new LMSPrivateKeyParameters[keys2.size()]);
        List<LMSSignature> list = this.sig;
        LMSSignature[] lMSSignatureArr = (LMSSignature[]) list.toArray(new LMSSignature[list.size()]);
        LMSPrivateKeyParameters rootKey = getRootKey();
        if (((long) (lMSPrivateKeyParametersArr[0].getIndex() - 1)) != jArr[0]) {
            lMSPrivateKeyParametersArr[0] = LMS.generateKeys(rootKey.getSigParameters(), rootKey.getOtsParameters(), (int) jArr[0], rootKey.getI(), rootKey.getMasterSecret());
            z = true;
        } else {
            z = false;
        }
        boolean z2 = z;
        int i = 1;
        while (i < jArr.length) {
            int i2 = i - 1;
            LMSPrivateKeyParameters lMSPrivateKeyParameters = lMSPrivateKeyParametersArr[i2];
            byte[] bArr = new byte[16];
            byte[] bArr2 = new byte[32];
            SeedDerive seedDerive = new SeedDerive(lMSPrivateKeyParameters.getI(), lMSPrivateKeyParameters.getMasterSecret(), DigestUtil.getDigest(lMSPrivateKeyParameters.getOtsParameters().getDigestOID()));
            seedDerive.setQ((int) jArr[i2]);
            seedDerive.setJ(-2);
            seedDerive.deriveSeed(bArr2, true);
            byte[] bArr3 = new byte[32];
            seedDerive.deriveSeed(bArr3, false);
            System.arraycopy(bArr3, 0, bArr, 0, bArr.length);
            boolean z3 = i >= jArr.length - 1 ? jArr[i] == ((long) lMSPrivateKeyParametersArr[i].getIndex()) : jArr[i] == ((long) (lMSPrivateKeyParametersArr[i].getIndex() - 1));
            if (!(Arrays.areEqual(bArr, lMSPrivateKeyParametersArr[i].getI()) && Arrays.areEqual(bArr2, lMSPrivateKeyParametersArr[i].getMasterSecret()))) {
                lMSPrivateKeyParametersArr[i] = LMS.generateKeys(keys2.get(i).getSigParameters(), keys2.get(i).getOtsParameters(), (int) jArr[i], bArr, bArr2);
                lMSSignatureArr[i2] = LMS.generateSign(lMSPrivateKeyParametersArr[i2], lMSPrivateKeyParametersArr[i].getPublicKey().toByteArray());
            } else if (!z3) {
                lMSPrivateKeyParametersArr[i] = LMS.generateKeys(keys2.get(i).getSigParameters(), keys2.get(i).getOtsParameters(), (int) jArr[i], bArr, bArr2);
            } else {
                i++;
            }
            z2 = true;
            i++;
        }
        if (z2) {
            updateHierarchy(lMSPrivateKeyParametersArr, lMSSignatureArr);
        }
    }

    /* access modifiers changed from: protected */
    public void updateHierarchy(LMSPrivateKeyParameters[] lMSPrivateKeyParametersArr, LMSSignature[] lMSSignatureArr) {
        synchronized (this) {
            this.keys = Collections.unmodifiableList(java.util.Arrays.asList(lMSPrivateKeyParametersArr));
            this.sig = Collections.unmodifiableList(java.util.Arrays.asList(lMSSignatureArr));
        }
    }
}
