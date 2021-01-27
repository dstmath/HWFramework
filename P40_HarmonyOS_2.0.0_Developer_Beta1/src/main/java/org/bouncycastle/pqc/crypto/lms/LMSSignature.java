package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.io.Streams;

/* access modifiers changed from: package-private */
public class LMSSignature implements Encodable {
    private final LMOtsSignature otsSignature;
    private final LMSigParameters parameter;
    private final int q;
    private final byte[][] y;

    public LMSSignature(int i, LMOtsSignature lMOtsSignature, LMSigParameters lMSigParameters, byte[][] bArr) {
        this.q = i;
        this.otsSignature = lMOtsSignature;
        this.parameter = lMSigParameters;
        this.y = bArr;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x005f  */
    public static LMSSignature getInstance(Object obj) throws IOException {
        Throwable th;
        if (obj instanceof LMSSignature) {
            return (LMSSignature) obj;
        }
        if (obj instanceof DataInputStream) {
            DataInputStream dataInputStream = (DataInputStream) obj;
            int readInt = dataInputStream.readInt();
            LMOtsSignature instance = LMOtsSignature.getInstance(obj);
            LMSigParameters parametersForType = LMSigParameters.getParametersForType(dataInputStream.readInt());
            byte[][] bArr = new byte[parametersForType.getH()][];
            for (int i = 0; i < bArr.length; i++) {
                bArr[i] = new byte[parametersForType.getM()];
                dataInputStream.readFully(bArr[i]);
            }
            return new LMSSignature(readInt, instance, parametersForType, bArr);
        } else if (obj instanceof byte[]) {
            DataInputStream dataInputStream2 = null;
            try {
                DataInputStream dataInputStream3 = new DataInputStream(new ByteArrayInputStream((byte[]) obj));
                try {
                    LMSSignature instance2 = getInstance(dataInputStream3);
                    dataInputStream3.close();
                    return instance2;
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

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LMSSignature lMSSignature = (LMSSignature) obj;
        if (this.q != lMSSignature.q) {
            return false;
        }
        LMOtsSignature lMOtsSignature = this.otsSignature;
        if (lMOtsSignature == null ? lMSSignature.otsSignature != null : !lMOtsSignature.equals(lMSSignature.otsSignature)) {
            return false;
        }
        LMSigParameters lMSigParameters = this.parameter;
        if (lMSigParameters == null ? lMSSignature.parameter == null : lMSigParameters.equals(lMSSignature.parameter)) {
            return Arrays.deepEquals(this.y, lMSSignature.y);
        }
        return false;
    }

    @Override // org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        return Composer.compose().u32str(this.q).bytes(this.otsSignature.getEncoded()).u32str(this.parameter.getType()).bytes(this.y).build();
    }

    public LMOtsSignature getOtsSignature() {
        return this.otsSignature;
    }

    public LMSigParameters getParameter() {
        return this.parameter;
    }

    public int getQ() {
        return this.q;
    }

    public byte[][] getY() {
        return this.y;
    }

    public int hashCode() {
        int i = this.q * 31;
        LMOtsSignature lMOtsSignature = this.otsSignature;
        int i2 = 0;
        int hashCode = (i + (lMOtsSignature != null ? lMOtsSignature.hashCode() : 0)) * 31;
        LMSigParameters lMSigParameters = this.parameter;
        if (lMSigParameters != null) {
            i2 = lMSigParameters.hashCode();
        }
        return ((hashCode + i2) * 31) + Arrays.deepHashCode(this.y);
    }
}
