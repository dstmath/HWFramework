package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.crypto.Digest;

/* access modifiers changed from: package-private */
public class LMS {
    static final short D_INTR = -31869;
    static final short D_LEAF = -32126;

    LMS() {
    }

    public static byte[] algorithm6a(LMSSignature lMSSignature, byte[] bArr, int i, byte[] bArr2) {
        int q = lMSSignature.getQ();
        if (lMSSignature.getOtsSignature().getType().getType() == i) {
            LMSContext createOtsContext = new LMOtsPublicKey(LMOtsParameters.getParametersForType(i), bArr, q, null).createOtsContext(lMSSignature);
            LmsUtils.byteArray(bArr2, createOtsContext);
            LMSigParameters parameter = lMSSignature.getParameter();
            int h = parameter.getH();
            byte[][] y = lMSSignature.getY();
            byte[] lm_ots_validate_signature_calculate = LM_OTS.lm_ots_validate_signature_calculate(createOtsContext);
            int i2 = (1 << h) + q;
            Digest digest = DigestUtil.getDigest(parameter.getDigestOID());
            byte[] bArr3 = new byte[digest.getDigestSize()];
            digest.update(bArr, 0, bArr.length);
            LmsUtils.u32str(i2, digest);
            LmsUtils.u16str(D_LEAF, digest);
            digest.update(lm_ots_validate_signature_calculate, 0, lm_ots_validate_signature_calculate.length);
            digest.doFinal(bArr3, 0);
            int i3 = 0;
            while (i2 > 1) {
                if ((i2 & 1) == 1) {
                    digest.update(bArr, 0, bArr.length);
                    LmsUtils.u32str(i2 / 2, digest);
                    LmsUtils.u16str(D_INTR, digest);
                    digest.update(y[i3], 0, y[i3].length);
                    digest.update(bArr3, 0, bArr3.length);
                } else {
                    digest.update(bArr, 0, bArr.length);
                    LmsUtils.u32str(i2 / 2, digest);
                    LmsUtils.u16str(D_INTR, digest);
                    digest.update(bArr3, 0, bArr3.length);
                    digest.update(y[i3], 0, y[i3].length);
                }
                digest.doFinal(bArr3, 0);
                i2 /= 2;
                i3++;
            }
            return bArr3;
        }
        throw new IllegalArgumentException("ots type from lsm signature does not match ots signature type from embedded ots signature");
    }

    public static LMSPrivateKeyParameters generateKeys(LMSigParameters lMSigParameters, LMOtsParameters lMOtsParameters, int i, byte[] bArr, byte[] bArr2) throws IllegalArgumentException {
        if (bArr2 != null && bArr2.length >= lMSigParameters.getM()) {
            return new LMSPrivateKeyParameters(lMSigParameters, lMOtsParameters, i, bArr, 1 << lMSigParameters.getH(), bArr2);
        }
        throw new IllegalArgumentException("root seed is less than " + lMSigParameters.getM());
    }

    public static LMSSignature generateSign(LMSContext lMSContext) {
        return new LMSSignature(lMSContext.getPrivateKey().getQ(), LM_OTS.lm_ots_generate_signature(lMSContext.getPrivateKey(), lMSContext.getQ(), lMSContext.getC()), lMSContext.getSigParams(), lMSContext.getPath());
    }

    public static LMSSignature generateSign(LMSPrivateKeyParameters lMSPrivateKeyParameters, byte[] bArr) {
        LMSContext generateLMSContext = lMSPrivateKeyParameters.generateLMSContext();
        generateLMSContext.update(bArr, 0, bArr.length);
        return generateSign(generateLMSContext);
    }

    public static boolean verifySignature(LMSPublicKeyParameters lMSPublicKeyParameters, LMSContext lMSContext) {
        LMSSignature lMSSignature = (LMSSignature) lMSContext.getSignature();
        LMSigParameters parameter = lMSSignature.getParameter();
        int h = parameter.getH();
        byte[][] y = lMSSignature.getY();
        byte[] lm_ots_validate_signature_calculate = LM_OTS.lm_ots_validate_signature_calculate(lMSContext);
        int q = (1 << h) + lMSSignature.getQ();
        byte[] i = lMSPublicKeyParameters.getI();
        Digest digest = DigestUtil.getDigest(parameter.getDigestOID());
        byte[] bArr = new byte[digest.getDigestSize()];
        digest.update(i, 0, i.length);
        LmsUtils.u32str(q, digest);
        LmsUtils.u16str(D_LEAF, digest);
        digest.update(lm_ots_validate_signature_calculate, 0, lm_ots_validate_signature_calculate.length);
        digest.doFinal(bArr, 0);
        int i2 = 0;
        while (q > 1) {
            if ((q & 1) == 1) {
                digest.update(i, 0, i.length);
                LmsUtils.u32str(q / 2, digest);
                LmsUtils.u16str(D_INTR, digest);
                digest.update(y[i2], 0, y[i2].length);
                digest.update(bArr, 0, bArr.length);
            } else {
                digest.update(i, 0, i.length);
                LmsUtils.u32str(q / 2, digest);
                LmsUtils.u16str(D_INTR, digest);
                digest.update(bArr, 0, bArr.length);
                digest.update(y[i2], 0, y[i2].length);
            }
            digest.doFinal(bArr, 0);
            q /= 2;
            i2++;
        }
        return lMSPublicKeyParameters.matchesT1(bArr);
    }

    public static boolean verifySignature(LMSPublicKeyParameters lMSPublicKeyParameters, LMSSignature lMSSignature, byte[] bArr) {
        LMSContext generateOtsContext = lMSPublicKeyParameters.generateOtsContext(lMSSignature);
        LmsUtils.byteArray(bArr, generateOtsContext);
        return verifySignature(lMSPublicKeyParameters, generateOtsContext);
    }

    public static boolean verifySignature(LMSPublicKeyParameters lMSPublicKeyParameters, byte[] bArr, byte[] bArr2) {
        LMSContext generateLMSContext = lMSPublicKeyParameters.generateLMSContext(bArr);
        LmsUtils.byteArray(bArr2, generateLMSContext);
        return verifySignature(lMSPublicKeyParameters, generateLMSContext);
    }
}
