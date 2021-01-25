package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.io.Streams;

public class HSSSignature implements Encodable {
    private final int lMinus1;
    private final LMSSignature signature;
    private final LMSSignedPubKey[] signedPubKey;

    public HSSSignature(int i, LMSSignedPubKey[] lMSSignedPubKeyArr, LMSSignature lMSSignature) {
        this.lMinus1 = i;
        this.signedPubKey = lMSSignedPubKeyArr;
        this.signature = lMSSignature;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0063  */
    public static HSSSignature getInstance(Object obj, int i) throws IOException {
        Throwable th;
        if (obj instanceof HSSSignature) {
            return (HSSSignature) obj;
        }
        if (obj instanceof DataInputStream) {
            int readInt = ((DataInputStream) obj).readInt();
            if (readInt == i - 1) {
                LMSSignedPubKey[] lMSSignedPubKeyArr = new LMSSignedPubKey[readInt];
                if (readInt != 0) {
                    for (int i2 = 0; i2 < lMSSignedPubKeyArr.length; i2++) {
                        lMSSignedPubKeyArr[i2] = new LMSSignedPubKey(LMSSignature.getInstance(obj), LMSPublicKeyParameters.getInstance(obj));
                    }
                }
                return new HSSSignature(readInt, lMSSignedPubKeyArr, LMSSignature.getInstance(obj));
            }
            throw new IllegalStateException("nspk exceeded maxNspk");
        } else if (obj instanceof byte[]) {
            DataInputStream dataInputStream = null;
            try {
                DataInputStream dataInputStream2 = new DataInputStream(new ByteArrayInputStream((byte[]) obj));
                try {
                    HSSSignature instance = getInstance(dataInputStream2, i);
                    dataInputStream2.close();
                    return instance;
                } catch (Throwable th2) {
                    th = th2;
                    dataInputStream = dataInputStream2;
                    if (dataInputStream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                throw th;
            }
        } else if (obj instanceof InputStream) {
            return getInstance(Streams.readAll((InputStream) obj), i);
        } else {
            throw new IllegalArgumentException("cannot parse " + obj);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        HSSSignature hSSSignature = (HSSSignature) obj;
        if (this.lMinus1 != hSSSignature.lMinus1 || this.signedPubKey.length != hSSSignature.signedPubKey.length) {
            return false;
        }
        int i = 0;
        while (true) {
            LMSSignedPubKey[] lMSSignedPubKeyArr = this.signedPubKey;
            if (i >= lMSSignedPubKeyArr.length) {
                LMSSignature lMSSignature = this.signature;
                LMSSignature lMSSignature2 = hSSSignature.signature;
                return lMSSignature != null ? lMSSignature.equals(lMSSignature2) : lMSSignature2 == null;
            } else if (!lMSSignedPubKeyArr[i].equals(hSSSignature.signedPubKey[i])) {
                return false;
            } else {
                i++;
            }
        }
    }

    @Override // org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        Composer compose = Composer.compose();
        compose.u32str(this.lMinus1);
        LMSSignedPubKey[] lMSSignedPubKeyArr = this.signedPubKey;
        if (lMSSignedPubKeyArr != null) {
            for (LMSSignedPubKey lMSSignedPubKey : lMSSignedPubKeyArr) {
                compose.bytes(lMSSignedPubKey);
            }
        }
        compose.bytes(this.signature);
        return compose.build();
    }

    public LMSSignature getSignature() {
        return this.signature;
    }

    public LMSSignedPubKey[] getSignedPubKey() {
        return this.signedPubKey;
    }

    public int getlMinus1() {
        return this.lMinus1;
    }

    public int hashCode() {
        int hashCode = ((this.lMinus1 * 31) + Arrays.hashCode(this.signedPubKey)) * 31;
        LMSSignature lMSSignature = this.signature;
        return hashCode + (lMSSignature != null ? lMSSignature.hashCode() : 0);
    }
}
