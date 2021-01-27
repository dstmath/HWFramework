package org.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.util.io.Streams;

public class HSSPublicKeyParameters extends LMSKeyParameters implements LMSContextBasedVerifier {
    private final int l;
    private final LMSPublicKeyParameters lmsPublicKey;

    public HSSPublicKeyParameters(int i, LMSPublicKeyParameters lMSPublicKeyParameters) {
        super(false);
        this.l = i;
        this.lmsPublicKey = lMSPublicKeyParameters;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x003d  */
    public static HSSPublicKeyParameters getInstance(Object obj) throws IOException {
        Throwable th;
        if (obj instanceof HSSPublicKeyParameters) {
            return (HSSPublicKeyParameters) obj;
        }
        if (obj instanceof DataInputStream) {
            return new HSSPublicKeyParameters(((DataInputStream) obj).readInt(), LMSPublicKeyParameters.getInstance(obj));
        }
        if (obj instanceof byte[]) {
            DataInputStream dataInputStream = null;
            try {
                DataInputStream dataInputStream2 = new DataInputStream(new ByteArrayInputStream((byte[]) obj));
                try {
                    HSSPublicKeyParameters instance = getInstance(dataInputStream2);
                    dataInputStream2.close();
                    return instance;
                } catch (Throwable th2) {
                    th = th2;
                    dataInputStream = dataInputStream2;
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (dataInputStream != null) {
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
        HSSPublicKeyParameters hSSPublicKeyParameters = (HSSPublicKeyParameters) obj;
        if (this.l != hSSPublicKeyParameters.l) {
            return false;
        }
        return this.lmsPublicKey.equals(hSSPublicKeyParameters.lmsPublicKey);
    }

    @Override // org.bouncycastle.pqc.crypto.lms.LMSContextBasedVerifier
    public LMSContext generateLMSContext(byte[] bArr) {
        try {
            HSSSignature instance = HSSSignature.getInstance(bArr, getL());
            LMSSignedPubKey[] signedPubKey = instance.getSignedPubKey();
            return signedPubKey[signedPubKey.length - 1].getPublicKey().generateOtsContext(instance.getSignature()).withSignedPublicKeys(signedPubKey);
        } catch (IOException e) {
            throw new IllegalStateException("cannot parse signature: " + e.getMessage());
        }
    }

    @Override // org.bouncycastle.pqc.crypto.lms.LMSKeyParameters, org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        return Composer.compose().u32str(this.l).bytes(this.lmsPublicKey.getEncoded()).build();
    }

    public int getL() {
        return this.l;
    }

    public LMSPublicKeyParameters getLMSPublicKey() {
        return this.lmsPublicKey;
    }

    public int hashCode() {
        return (this.l * 31) + this.lmsPublicKey.hashCode();
    }

    @Override // org.bouncycastle.pqc.crypto.lms.LMSContextBasedVerifier
    public boolean verify(LMSContext lMSContext) {
        LMSSignedPubKey[] signedPubKeys = lMSContext.getSignedPubKeys();
        if (signedPubKeys.length != getL() - 1) {
            return false;
        }
        LMSPublicKeyParameters lMSPublicKey = getLMSPublicKey();
        boolean z = false;
        for (int i = 0; i < signedPubKeys.length; i++) {
            if (!LMS.verifySignature(lMSPublicKey, signedPubKeys[i].getSignature(), signedPubKeys[i].getPublicKey().toByteArray())) {
                z = true;
            }
            lMSPublicKey = signedPubKeys[i].getPublicKey();
        }
        return lMSPublicKey.verify(lMSContext) & (!z);
    }
}
