package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

/* access modifiers changed from: package-private */
public class LM_OTS {
    static final short D_MESG = -32383;
    private static final short D_PBLC = -32640;
    private static final int ITER_J = 22;
    private static final int ITER_K = 20;
    private static final int ITER_PREV = 23;
    static final int MAX_HASH = 32;
    static final int SEED_LEN = 32;
    static final int SEED_RANDOMISER_INDEX = -3;

    LM_OTS() {
    }

    public static int cksm(byte[] bArr, int i, LMOtsParameters lMOtsParameters) {
        int w = (1 << lMOtsParameters.getW()) - 1;
        int i2 = 0;
        for (int i3 = 0; i3 < (i * 8) / lMOtsParameters.getW(); i3++) {
            i2 = (i2 + w) - coef(bArr, i3, lMOtsParameters.getW());
        }
        return i2 << lMOtsParameters.getLs();
    }

    public static int coef(byte[] bArr, int i, int i2) {
        return (bArr[(i * i2) / 8] >>> (((~i) & ((8 / i2) - 1)) * i2)) & ((1 << i2) - 1);
    }

    public static LMOtsSignature lm_ots_generate_signature(LMOtsPrivateKey lMOtsPrivateKey, byte[] bArr, byte[] bArr2) {
        LMOtsParameters parameter = lMOtsPrivateKey.getParameter();
        int n = parameter.getN();
        int p = parameter.getP();
        int w = parameter.getW();
        byte[] bArr3 = new byte[(p * n)];
        Digest digest = DigestUtil.getDigest(parameter.getDigestOID());
        SeedDerive derivationFunction = lMOtsPrivateKey.getDerivationFunction();
        int cksm = cksm(bArr, n, parameter);
        bArr[n] = (byte) ((cksm >>> 8) & GF2Field.MASK);
        bArr[n + 1] = (byte) cksm;
        int i = n + 23;
        byte[] build = Composer.compose().bytes(lMOtsPrivateKey.getI()).u32str(lMOtsPrivateKey.getQ()).padUntil(0, i).build();
        derivationFunction.setJ(0);
        int i2 = 0;
        while (i2 < p) {
            Pack.shortToBigEndian((short) i2, build, 20);
            int i3 = 23;
            derivationFunction.deriveSeed(build, i2 < p + -1, 23);
            int coef = coef(bArr, i2, w);
            for (int i4 = 0; i4 < coef; i4++) {
                build[22] = (byte) i4;
                digest.update(build, 0, i);
                i3 = 23;
                digest.doFinal(build, 23);
            }
            System.arraycopy(build, i3, bArr3, n * i2, n);
            i2++;
        }
        return new LMOtsSignature(parameter, bArr2, bArr3);
    }

    public static LMOtsSignature lm_ots_generate_signature(LMSigParameters lMSigParameters, LMOtsPrivateKey lMOtsPrivateKey, byte[][] bArr, byte[] bArr2, boolean z) {
        byte[] bArr3;
        byte[] bArr4 = new byte[34];
        if (!z) {
            LMSContext signatureContext = lMOtsPrivateKey.getSignatureContext(lMSigParameters, bArr);
            LmsUtils.byteArray(bArr2, 0, bArr2.length, signatureContext);
            bArr3 = signatureContext.getC();
            bArr4 = signatureContext.getQ();
        } else {
            bArr3 = new byte[32];
            System.arraycopy(bArr2, 0, bArr4, 0, lMOtsPrivateKey.getParameter().getN());
        }
        return lm_ots_generate_signature(lMOtsPrivateKey, bArr4, bArr3);
    }

    public static boolean lm_ots_validate_signature(LMOtsPublicKey lMOtsPublicKey, LMOtsSignature lMOtsSignature, byte[] bArr, boolean z) throws LMSException {
        if (lMOtsSignature.getType().equals(lMOtsPublicKey.getParameter())) {
            return Arrays.areEqual(lm_ots_validate_signature_calculate(lMOtsPublicKey, lMOtsSignature, bArr), lMOtsPublicKey.getK());
        }
        throw new LMSException("public key and signature ots types do not match");
    }

    public static byte[] lm_ots_validate_signature_calculate(LMOtsPublicKey lMOtsPublicKey, LMOtsSignature lMOtsSignature, byte[] bArr) {
        LMSContext createOtsContext = lMOtsPublicKey.createOtsContext(lMOtsSignature);
        LmsUtils.byteArray(bArr, createOtsContext);
        return lm_ots_validate_signature_calculate(createOtsContext);
    }

    public static byte[] lm_ots_validate_signature_calculate(LMSContext lMSContext) {
        LMOtsPublicKey publicKey = lMSContext.getPublicKey();
        LMOtsParameters parameter = publicKey.getParameter();
        Object signature = lMSContext.getSignature();
        LMOtsSignature otsSignature = signature instanceof LMSSignature ? ((LMSSignature) signature).getOtsSignature() : (LMOtsSignature) signature;
        int n = parameter.getN();
        int w = parameter.getW();
        int p = parameter.getP();
        byte[] q = lMSContext.getQ();
        int cksm = cksm(q, n, parameter);
        q[n] = (byte) ((cksm >>> 8) & GF2Field.MASK);
        q[n + 1] = (byte) cksm;
        byte[] i = publicKey.getI();
        int q2 = publicKey.getQ();
        Digest digest = DigestUtil.getDigest(parameter.getDigestOID());
        LmsUtils.byteArray(i, digest);
        LmsUtils.u32str(q2, digest);
        LmsUtils.u16str(D_PBLC, digest);
        Composer u32str = Composer.compose().bytes(i).u32str(q2);
        int i2 = n + 23;
        byte[] build = u32str.padUntil(0, i2).build();
        int i3 = (1 << w) - 1;
        byte[] y = otsSignature.getY();
        Digest digest2 = DigestUtil.getDigest(parameter.getDigestOID());
        for (int i4 = 0; i4 < p; i4++) {
            Pack.shortToBigEndian((short) i4, build, 20);
            System.arraycopy(y, i4 * n, build, 23, n);
            for (int coef = coef(q, i4, w); coef < i3; coef++) {
                build[22] = (byte) coef;
                digest2.update(build, 0, i2);
                digest2.doFinal(build, 23);
            }
            digest.update(build, 23, n);
        }
        byte[] bArr = new byte[n];
        digest.doFinal(bArr, 0);
        return bArr;
    }

    public static LMOtsPublicKey lms_ots_generatePublicKey(LMOtsPrivateKey lMOtsPrivateKey) {
        return new LMOtsPublicKey(lMOtsPrivateKey.getParameter(), lMOtsPrivateKey.getI(), lMOtsPrivateKey.getQ(), lms_ots_generatePublicKey(lMOtsPrivateKey.getParameter(), lMOtsPrivateKey.getI(), lMOtsPrivateKey.getQ(), lMOtsPrivateKey.getMasterSecret()));
    }

    static byte[] lms_ots_generatePublicKey(LMOtsParameters lMOtsParameters, byte[] bArr, int i, byte[] bArr2) {
        Digest digest = DigestUtil.getDigest(lMOtsParameters.getDigestOID());
        byte[] build = Composer.compose().bytes(bArr).u32str(i).u16str(-32640).padUntil(0, 22).build();
        digest.update(build, 0, build.length);
        Digest digest2 = DigestUtil.getDigest(lMOtsParameters.getDigestOID());
        byte[] build2 = Composer.compose().bytes(bArr).u32str(i).padUntil(0, digest2.getDigestSize() + 23).build();
        SeedDerive seedDerive = new SeedDerive(bArr, bArr2, DigestUtil.getDigest(lMOtsParameters.getDigestOID()));
        seedDerive.setQ(i);
        seedDerive.setJ(0);
        int p = lMOtsParameters.getP();
        int n = lMOtsParameters.getN();
        int w = (1 << lMOtsParameters.getW()) - 1;
        int i2 = 0;
        while (i2 < p) {
            seedDerive.deriveSeed(build2, i2 < p + -1, 23);
            Pack.shortToBigEndian((short) i2, build2, 20);
            for (int i3 = 0; i3 < w; i3++) {
                build2[22] = (byte) i3;
                digest2.update(build2, 0, build2.length);
                digest2.doFinal(build2, 23);
            }
            digest.update(build2, 23, n);
            i2++;
        }
        byte[] bArr3 = new byte[digest.getDigestSize()];
        digest.doFinal(bArr3, 0);
        return bArr3;
    }
}
