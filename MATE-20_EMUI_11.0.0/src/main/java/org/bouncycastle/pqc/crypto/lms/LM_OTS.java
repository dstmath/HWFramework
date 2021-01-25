package org.bouncycastle.pqc.crypto.lms;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

/* access modifiers changed from: package-private */
public class LM_OTS {
    private static final short D_MESG = -32383;
    private static final short D_PBLC = -32640;
    private static final int ITER_J = 22;
    private static final int ITER_K = 20;
    private static final int ITER_PREV = 23;
    private static final int MAX_HASH = 32;
    private static final int SEED_LEN = 32;
    private static final int SEED_RANDOMISER_INDEX = -3;

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

    public static LMOtsSignature lm_ots_generate_signature(LMOtsPrivateKey lMOtsPrivateKey, byte[] bArr, boolean z) {
        LMOtsParameters parameter = lMOtsPrivateKey.getParameter();
        int n = parameter.getN();
        int p = parameter.getP();
        int w = parameter.getW();
        byte[] bArr2 = new byte[(p * n)];
        byte[] bArr3 = new byte[32];
        if (!z) {
            SeedDerive derivationFunction = lMOtsPrivateKey.getDerivationFunction();
            derivationFunction.setJ(-3);
            derivationFunction.deriveSeed(bArr3, false);
        }
        SeedDerive derivationFunction2 = lMOtsPrivateKey.getDerivationFunction();
        byte[] bArr4 = new byte[34];
        Digest digest = DigestUtil.getDigest(parameter.getDigestOID());
        if (!z) {
            LmsUtils.byteArray(lMOtsPrivateKey.getI(), digest);
            LmsUtils.u32str(lMOtsPrivateKey.getQ(), digest);
            LmsUtils.u16str(D_MESG, digest);
            LmsUtils.byteArray(bArr3, digest);
            LmsUtils.byteArray(bArr, 0, bArr.length, digest);
            digest.doFinal(bArr4, 0);
        } else {
            System.arraycopy(bArr, 0, bArr4, 0, n);
        }
        int cksm = cksm(bArr4, n, parameter);
        bArr4[n] = (byte) ((cksm >>> 8) & GF2Field.MASK);
        bArr4[n + 1] = (byte) cksm;
        int i = n + 23;
        byte[] build = Composer.compose().bytes(lMOtsPrivateKey.getI()).u32str(lMOtsPrivateKey.getQ()).padUntil(0, i).build();
        derivationFunction2.setJ(0);
        int i2 = 0;
        while (i2 < p) {
            Pack.shortToBigEndian((short) i2, build, 20);
            int i3 = 23;
            derivationFunction2.deriveSeed(build, i2 < p + -1, 23);
            int coef = coef(bArr4, i2, w);
            for (int i4 = 0; i4 < coef; i4++) {
                build[22] = (byte) i4;
                digest.update(build, 0, i);
                i3 = 23;
                digest.doFinal(build, 23);
            }
            System.arraycopy(build, i3, bArr2, n * i2, n);
            i2++;
        }
        return new LMOtsSignature(parameter, bArr3, bArr2);
    }

    public static boolean lm_ots_validate_signature(LMOtsPublicKey lMOtsPublicKey, LMOtsSignature lMOtsSignature, byte[] bArr, boolean z) throws LMSException {
        if (lMOtsSignature.getType().equals(lMOtsPublicKey.getParameter())) {
            return Arrays.areEqual(lm_ots_validate_signature_calculate(lMOtsPublicKey.getParameter(), lMOtsPublicKey.getI(), lMOtsPublicKey.getQ(), lMOtsSignature, bArr, z), lMOtsPublicKey.getK());
        }
        throw new LMSException("public key and signature ots types do not match");
    }

    public static byte[] lm_ots_validate_signature_calculate(LMOtsParameters lMOtsParameters, byte[] bArr, int i, LMOtsSignature lMOtsSignature, byte[] bArr2, boolean z) {
        byte[] c = lMOtsSignature.getC();
        byte[] y = lMOtsSignature.getY();
        byte[] bArr3 = new byte[34];
        if (z) {
            System.arraycopy(bArr2, 0, bArr3, 0, lMOtsParameters.getN());
        } else {
            Digest digest = DigestUtil.getDigest(lMOtsParameters.getDigestOID());
            LmsUtils.byteArray(bArr, digest);
            LmsUtils.u32str(i, digest);
            LmsUtils.u16str(D_MESG, digest);
            LmsUtils.byteArray(c, digest);
            LmsUtils.byteArray(bArr2, digest);
            digest.doFinal(bArr3, 0);
        }
        int n = lMOtsParameters.getN();
        int w = lMOtsParameters.getW();
        int p = lMOtsParameters.getP();
        int cksm = cksm(bArr3, n, lMOtsParameters);
        bArr3[n] = (byte) ((cksm >>> 8) & GF2Field.MASK);
        bArr3[n + 1] = (byte) cksm;
        Digest digest2 = DigestUtil.getDigest(lMOtsParameters.getDigestOID());
        LmsUtils.byteArray(bArr, digest2);
        LmsUtils.u32str(i, digest2);
        LmsUtils.u16str(D_PBLC, digest2);
        Composer u32str = Composer.compose().bytes(bArr).u32str(i);
        int i2 = n + 23;
        byte[] build = u32str.padUntil(0, i2).build();
        int i3 = (1 << w) - 1;
        Digest digest3 = DigestUtil.getDigest(lMOtsParameters.getDigestOID());
        for (int i4 = 0; i4 < p; i4++) {
            Pack.shortToBigEndian((short) i4, build, 20);
            System.arraycopy(y, i4 * n, build, 23, n);
            for (int coef = coef(bArr3, i4, w); coef < i3; coef++) {
                build[22] = (byte) coef;
                digest3.update(build, 0, i2);
                digest3.doFinal(build, 23);
            }
            digest2.update(build, 23, n);
        }
        byte[] bArr4 = new byte[n];
        digest2.doFinal(bArr4, 0);
        return bArr4;
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
