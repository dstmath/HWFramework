package org.bouncycastle.pqc.crypto.lms;

import java.util.Arrays;
import java.util.List;
import org.bouncycastle.pqc.crypto.ExhaustedPrivateKeyException;

class HSS {

    static class PlaceholderLMSPrivateKey extends LMSPrivateKeyParameters {
        public PlaceholderLMSPrivateKey(LMSigParameters lMSigParameters, LMOtsParameters lMOtsParameters, int i, byte[] bArr, int i2, byte[] bArr2) {
            super(lMSigParameters, lMOtsParameters, i, bArr, i2, bArr2);
        }

        /* access modifiers changed from: package-private */
        @Override // org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters
        public LMOtsPrivateKey getNextOtsPrivateKey() {
            throw new RuntimeException("placeholder only");
        }

        @Override // org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters
        public LMSPublicKeyParameters getPublicKey() {
            throw new RuntimeException("placeholder only");
        }
    }

    HSS() {
    }

    public static HSSPrivateKeyParameters generateHSSKeyPair(HSSKeyGenerationParameters hSSKeyGenerationParameters) {
        int i;
        byte[] bArr;
        LMSPrivateKeyParameters[] lMSPrivateKeyParametersArr = new LMSPrivateKeyParameters[hSSKeyGenerationParameters.getDepth()];
        LMSSignature[] lMSSignatureArr = new LMSSignature[(hSSKeyGenerationParameters.getDepth() - 1)];
        byte[] bArr2 = new byte[32];
        hSSKeyGenerationParameters.getRandom().nextBytes(bArr2);
        byte[] bArr3 = new byte[16];
        hSSKeyGenerationParameters.getRandom().nextBytes(bArr3);
        byte[] bArr4 = new byte[0];
        int i2 = 0;
        long j = 1;
        while (i2 < lMSPrivateKeyParametersArr.length) {
            if (i2 == 0) {
                lMSPrivateKeyParametersArr[i2] = new LMSPrivateKeyParameters(hSSKeyGenerationParameters.getLmsParameters()[i2].getLMSigParam(), hSSKeyGenerationParameters.getLmsParameters()[i2].getLMOTSParam(), 0, bArr3, 1 << hSSKeyGenerationParameters.getLmsParameters()[i2].getLMSigParam().getH(), bArr2);
                i = i2;
                bArr = bArr4;
            } else {
                i = i2;
                bArr = bArr4;
                lMSPrivateKeyParametersArr[i] = new PlaceholderLMSPrivateKey(hSSKeyGenerationParameters.getLmsParameters()[i2].getLMSigParam(), hSSKeyGenerationParameters.getLmsParameters()[i2].getLMOTSParam(), -1, bArr, 1 << hSSKeyGenerationParameters.getLmsParameters()[i2].getLMSigParam().getH(), bArr);
            }
            j *= (long) (1 << hSSKeyGenerationParameters.getLmsParameters()[i].getLMSigParam().getH());
            i2 = i + 1;
            bArr4 = bArr;
        }
        if (j == 0) {
            j = Long.MAX_VALUE;
        }
        return new HSSPrivateKeyParameters(hSSKeyGenerationParameters.getDepth(), Arrays.asList(lMSPrivateKeyParametersArr), Arrays.asList(lMSSignatureArr), 0, j);
    }

    public static HSSSignature generateSignature(int i, LMSContext lMSContext) {
        return new HSSSignature(i - 1, lMSContext.getSignedPubKeys(), LMS.generateSign(lMSContext));
    }

    public static HSSSignature generateSignature(HSSPrivateKeyParameters hSSPrivateKeyParameters, byte[] bArr) {
        LMSPrivateKeyParameters lMSPrivateKeyParameters;
        LMSSignedPubKey[] lMSSignedPubKeyArr;
        int l = hSSPrivateKeyParameters.getL();
        synchronized (hSSPrivateKeyParameters) {
            rangeTestKeys(hSSPrivateKeyParameters);
            List<LMSPrivateKeyParameters> keys = hSSPrivateKeyParameters.getKeys();
            List<LMSSignature> sig = hSSPrivateKeyParameters.getSig();
            int i = l - 1;
            lMSPrivateKeyParameters = hSSPrivateKeyParameters.getKeys().get(i);
            lMSSignedPubKeyArr = new LMSSignedPubKey[i];
            int i2 = 0;
            while (i2 < i) {
                int i3 = i2 + 1;
                lMSSignedPubKeyArr[i2] = new LMSSignedPubKey(sig.get(i2), keys.get(i3).getPublicKey());
                i2 = i3;
            }
            hSSPrivateKeyParameters.incIndex();
        }
        LMSContext withSignedPublicKeys = lMSPrivateKeyParameters.generateLMSContext().withSignedPublicKeys(lMSSignedPubKeyArr);
        withSignedPublicKeys.update(bArr, 0, bArr.length);
        return generateSignature(l, withSignedPublicKeys);
    }

    public static void incrementIndex(HSSPrivateKeyParameters hSSPrivateKeyParameters) {
        synchronized (hSSPrivateKeyParameters) {
            rangeTestKeys(hSSPrivateKeyParameters);
            hSSPrivateKeyParameters.incIndex();
            hSSPrivateKeyParameters.getKeys().get(hSSPrivateKeyParameters.getL() - 1).incIndex();
        }
    }

    static void rangeTestKeys(HSSPrivateKeyParameters hSSPrivateKeyParameters) {
        synchronized (hSSPrivateKeyParameters) {
            if (hSSPrivateKeyParameters.getIndex() >= hSSPrivateKeyParameters.getIndexLimit()) {
                StringBuilder sb = new StringBuilder();
                sb.append("hss private key");
                sb.append(hSSPrivateKeyParameters.isShard() ? " shard" : "");
                sb.append(" is exhausted");
                throw new ExhaustedPrivateKeyException(sb.toString());
            }
            int l = hSSPrivateKeyParameters.getL();
            List<LMSPrivateKeyParameters> keys = hSSPrivateKeyParameters.getKeys();
            int i = l;
            while (true) {
                int i2 = i - 1;
                if (keys.get(i2).getIndex() != (1 << keys.get(i2).getSigParameters().getH())) {
                    while (i < l) {
                        hSSPrivateKeyParameters.replaceConsumedKey(i);
                        i++;
                    }
                } else if (i2 == 0) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("hss private key");
                    sb2.append(hSSPrivateKeyParameters.isShard() ? " shard" : "");
                    sb2.append(" is exhausted the maximum limit for this HSS private key");
                    throw new ExhaustedPrivateKeyException(sb2.toString());
                } else {
                    i = i2;
                }
            }
        }
    }

    public static boolean verifySignature(HSSPublicKeyParameters hSSPublicKeyParameters, HSSSignature hSSSignature, byte[] bArr) {
        int i = hSSSignature.getlMinus1();
        int i2 = i + 1;
        if (i2 != hSSPublicKeyParameters.getL()) {
            return false;
        }
        LMSSignature[] lMSSignatureArr = new LMSSignature[i2];
        LMSPublicKeyParameters[] lMSPublicKeyParametersArr = new LMSPublicKeyParameters[i];
        for (int i3 = 0; i3 < i; i3++) {
            lMSSignatureArr[i3] = hSSSignature.getSignedPubKey()[i3].getSignature();
            lMSPublicKeyParametersArr[i3] = hSSSignature.getSignedPubKey()[i3].getPublicKey();
        }
        lMSSignatureArr[i] = hSSSignature.getSignature();
        LMSPublicKeyParameters lMSPublicKey = hSSPublicKeyParameters.getLMSPublicKey();
        for (int i4 = 0; i4 < i; i4++) {
            if (!LMS.verifySignature(lMSPublicKey, lMSSignatureArr[i4], lMSPublicKeyParametersArr[i4].toByteArray())) {
                return false;
            }
            try {
                lMSPublicKey = lMSPublicKeyParametersArr[i4];
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        return LMS.verifySignature(lMSPublicKey, lMSSignatureArr[i], bArr);
    }
}
