package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public class LMSPublicKeyParameters extends LMSKeyParameters implements LMSContextBasedVerifier {
    private final byte[] I;
    private final byte[] T1;
    private final LMOtsParameters lmOtsType;
    private final LMSigParameters parameterSet;

    public LMSPublicKeyParameters(LMSigParameters lMSigParameters, LMOtsParameters lMOtsParameters, byte[] bArr, byte[] bArr2) {
        super(false);
        this.parameterSet = lMSigParameters;
        this.lmOtsType = lMOtsParameters;
        this.I = Arrays.clone(bArr2);
        this.T1 = Arrays.clone(bArr);
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0054  */
    public static LMSPublicKeyParameters getInstance(Object obj) throws IOException {
        Throwable th;
        if (obj instanceof LMSPublicKeyParameters) {
            return (LMSPublicKeyParameters) obj;
        }
        if (obj instanceof DataInputStream) {
            DataInputStream dataInputStream = (DataInputStream) obj;
            LMSigParameters parametersForType = LMSigParameters.getParametersForType(dataInputStream.readInt());
            LMOtsParameters parametersForType2 = LMOtsParameters.getParametersForType(dataInputStream.readInt());
            byte[] bArr = new byte[16];
            dataInputStream.readFully(bArr);
            byte[] bArr2 = new byte[parametersForType.getM()];
            dataInputStream.readFully(bArr2);
            return new LMSPublicKeyParameters(parametersForType, parametersForType2, bArr2, bArr);
        } else if (obj instanceof byte[]) {
            DataInputStream dataInputStream2 = null;
            try {
                DataInputStream dataInputStream3 = new DataInputStream(new ByteArrayInputStream((byte[]) obj));
                try {
                    LMSPublicKeyParameters instance = getInstance(dataInputStream3);
                    dataInputStream3.close();
                    return instance;
                } catch (Throwable th2) {
                    th = th2;
                    dataInputStream2 = dataInputStream3;
                    if (dataInputStream2 != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (dataInputStream2 != null) {
                    dataInputStream2.close();
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
        LMSPublicKeyParameters lMSPublicKeyParameters = (LMSPublicKeyParameters) obj;
        if (this.parameterSet.equals(lMSPublicKeyParameters.parameterSet) && this.lmOtsType.equals(lMSPublicKeyParameters.lmOtsType) && Arrays.areEqual(this.I, lMSPublicKeyParameters.I)) {
            return Arrays.areEqual(this.T1, lMSPublicKeyParameters.T1);
        }
        return false;
    }

    @Override // org.bouncycastle.pqc.crypto.lms.LMSContextBasedVerifier
    public LMSContext generateLMSContext(byte[] bArr) {
        try {
            return generateOtsContext(LMSSignature.getInstance(bArr));
        } catch (IOException e) {
            throw new IllegalStateException("cannot parse signature: " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public LMSContext generateOtsContext(LMSSignature lMSSignature) {
        int type = getOtsParameters().getType();
        if (lMSSignature.getOtsSignature().getType().getType() == type) {
            return new LMOtsPublicKey(LMOtsParameters.getParametersForType(type), this.I, lMSSignature.getQ(), null).createOtsContext(lMSSignature);
        }
        throw new IllegalArgumentException("ots type from lsm signature does not match ots signature type from embedded ots signature");
    }

    @Override // org.bouncycastle.pqc.crypto.lms.LMSKeyParameters, org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        return toByteArray();
    }

    public byte[] getI() {
        return Arrays.clone(this.I);
    }

    public LMSParameters getLMSParameters() {
        return new LMSParameters(getSigParameters(), getOtsParameters());
    }

    public LMOtsParameters getOtsParameters() {
        return this.lmOtsType;
    }

    public LMSigParameters getSigParameters() {
        return this.parameterSet;
    }

    public byte[] getT1() {
        return Arrays.clone(this.T1);
    }

    public int hashCode() {
        return (((((this.parameterSet.hashCode() * 31) + this.lmOtsType.hashCode()) * 31) + Arrays.hashCode(this.I)) * 31) + Arrays.hashCode(this.T1);
    }

    /* access modifiers changed from: package-private */
    public boolean matchesT1(byte[] bArr) {
        return Arrays.areEqual(this.T1, bArr);
    }

    /* access modifiers changed from: package-private */
    public byte[] refI() {
        return this.I;
    }

    /* access modifiers changed from: package-private */
    public byte[] toByteArray() {
        return Composer.compose().u32str(this.parameterSet.getType()).u32str(this.lmOtsType.getType()).bytes(this.I).bytes(this.T1).build();
    }

    @Override // org.bouncycastle.pqc.crypto.lms.LMSContextBasedVerifier
    public boolean verify(LMSContext lMSContext) {
        return LMS.verifySignature(this, lMSContext);
    }
}
