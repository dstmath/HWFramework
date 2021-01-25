package org.bouncycastle.pqc.crypto.sphincs;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.MessageSigner;
import org.bouncycastle.pqc.crypto.sphincs.Tree;
import org.bouncycastle.util.Pack;

public class SPHINCS256Signer implements MessageSigner {
    private final HashFunctions hashFunctions;
    private byte[] keyData;

    public SPHINCS256Signer(Digest digest, Digest digest2) {
        if (digest.getDigestSize() != 32) {
            throw new IllegalArgumentException("n-digest needs to produce 32 bytes of output");
        } else if (digest2.getDigestSize() == 64) {
            this.hashFunctions = new HashFunctions(digest, digest2);
        } else {
            throw new IllegalArgumentException("2n-digest needs to produce 64 bytes of output");
        }
    }

    static void compute_authpath_wots(HashFunctions hashFunctions2, byte[] bArr, byte[] bArr2, int i, Tree.leafaddr leafaddr, byte[] bArr3, byte[] bArr4, int i2) {
        Tree.leafaddr leafaddr2 = new Tree.leafaddr(leafaddr);
        byte[] bArr5 = new byte[2048];
        byte[] bArr6 = new byte[1024];
        byte[] bArr7 = new byte[68608];
        leafaddr2.subleaf = 0;
        while (leafaddr2.subleaf < 32) {
            Seed.get_seed(hashFunctions2, bArr6, (int) (leafaddr2.subleaf * 32), bArr3, leafaddr2);
            leafaddr2.subleaf++;
        }
        Wots wots = new Wots();
        leafaddr2.subleaf = 0;
        while (leafaddr2.subleaf < 32) {
            wots.wots_pkgen(hashFunctions2, bArr7, (int) (leafaddr2.subleaf * 67 * 32), bArr6, (int) (leafaddr2.subleaf * 32), bArr4, 0);
            leafaddr2.subleaf++;
        }
        leafaddr2.subleaf = 0;
        while (leafaddr2.subleaf < 32) {
            Tree.l_tree(hashFunctions2, bArr5, (int) ((leafaddr2.subleaf * 32) + 1024), bArr7, (int) (leafaddr2.subleaf * 67 * 32), bArr4, 0);
            leafaddr2.subleaf++;
        }
        int i3 = 0;
        for (int i4 = 32; i4 > 0; i4 >>>= 1) {
            for (int i5 = 0; i5 < i4; i5 += 2) {
                hashFunctions2.hash_2n_n_mask(bArr5, ((i4 >>> 1) * 32) + ((i5 >>> 1) * 32), bArr5, (i4 * 32) + (i5 * 32), bArr4, (i3 + 7) * 2 * 32);
            }
            i3++;
        }
        int i6 = (int) leafaddr.subleaf;
        for (int i7 = 0; i7 < i2; i7++) {
            System.arraycopy(bArr5, ((32 >>> i7) * 32) + (((i6 >>> i7) ^ 1) * 32), bArr2, i + (i7 * 32), 32);
        }
        System.arraycopy(bArr5, 32, bArr, 0, 32);
    }

    static void validate_authpath(HashFunctions hashFunctions2, byte[] bArr, byte[] bArr2, int i, byte[] bArr3, int i2, byte[] bArr4, int i3) {
        byte[] bArr5 = new byte[64];
        if ((i & 1) != 0) {
            for (int i4 = 0; i4 < 32; i4++) {
                bArr5[i4 + 32] = bArr2[i4];
            }
            for (int i5 = 0; i5 < 32; i5++) {
                bArr5[i5] = bArr3[i2 + i5];
            }
        } else {
            for (int i6 = 0; i6 < 32; i6++) {
                bArr5[i6] = bArr2[i6];
            }
            for (int i7 = 0; i7 < 32; i7++) {
                bArr5[i7 + 32] = bArr3[i2 + i7];
            }
        }
        int i8 = i2 + 32;
        int i9 = 0;
        int i10 = i;
        while (i9 < i3 - 1) {
            int i11 = i10 >>> 1;
            if ((i11 & 1) != 0) {
                hashFunctions2.hash_2n_n_mask(bArr5, 32, bArr5, 0, bArr4, (i9 + 7) * 2 * 32);
                for (int i12 = 0; i12 < 32; i12++) {
                    bArr5[i12] = bArr3[i8 + i12];
                }
            } else {
                hashFunctions2.hash_2n_n_mask(bArr5, 0, bArr5, 0, bArr4, (i9 + 7) * 2 * 32);
                for (int i13 = 0; i13 < 32; i13++) {
                    bArr5[i13 + 32] = bArr3[i8 + i13];
                }
            }
            i8 += 32;
            i9++;
            i10 = i11;
        }
        hashFunctions2.hash_2n_n_mask(bArr, 0, bArr5, 0, bArr4, ((i3 + 7) - 1) * 2 * 32);
    }

    private void zerobytes(byte[] bArr, int i, int i2) {
        for (int i3 = 0; i3 != i2; i3++) {
            bArr[i + i3] = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] crypto_sign(HashFunctions hashFunctions2, byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = new byte[41000];
        byte[] bArr4 = new byte[32];
        byte[] bArr5 = new byte[64];
        long[] jArr = new long[8];
        byte[] bArr6 = new byte[32];
        byte[] bArr7 = new byte[32];
        byte[] bArr8 = new byte[1024];
        byte[] bArr9 = new byte[1088];
        for (int i = 0; i < 1088; i++) {
            bArr9[i] = bArr2[i];
        }
        System.arraycopy(bArr9, 1056, bArr3, 40968, 32);
        Digest messageHash = hashFunctions2.getMessageHash();
        byte[] bArr10 = new byte[messageHash.getDigestSize()];
        messageHash.update(bArr3, 40968, 32);
        messageHash.update(bArr, 0, bArr.length);
        messageHash.doFinal(bArr10, 0);
        zerobytes(bArr3, 40968, 32);
        for (int i2 = 0; i2 != jArr.length; i2++) {
            jArr[i2] = Pack.littleEndianToLong(bArr10, i2 * 8);
        }
        long j = jArr[0] & 1152921504606846975L;
        System.arraycopy(bArr10, 16, bArr4, 0, 32);
        System.arraycopy(bArr4, 0, bArr3, 39912, 32);
        Tree.leafaddr leafaddr = new Tree.leafaddr();
        leafaddr.level = 11;
        leafaddr.subtree = 0;
        leafaddr.subleaf = 0;
        System.arraycopy(bArr9, 32, bArr3, 39944, 1024);
        Tree.treehash(hashFunctions2, bArr3, 40968, 5, bArr9, leafaddr, bArr3, 39944);
        Digest messageHash2 = hashFunctions2.getMessageHash();
        messageHash2.update(bArr3, 39912, 1088);
        messageHash2.update(bArr, 0, bArr.length);
        messageHash2.doFinal(bArr5, 0);
        Tree.leafaddr leafaddr2 = new Tree.leafaddr();
        leafaddr2.level = 12;
        leafaddr2.subleaf = (long) ((int) (j & 31));
        leafaddr2.subtree = j >>> 5;
        for (int i3 = 0; i3 < 32; i3++) {
            bArr3[i3] = bArr4[i3];
        }
        byte[] bArr11 = bArr8;
        System.arraycopy(bArr9, 32, bArr11, 0, 1024);
        for (int i4 = 0; i4 < 8; i4++) {
            bArr3[32 + i4] = (byte) ((int) ((j >>> (i4 * 8)) & 255));
        }
        Seed.get_seed(hashFunctions2, bArr7, 0, bArr9, leafaddr2);
        new Horst();
        byte[] bArr12 = bArr9;
        Wots wots = new Wots();
        int horst_sign = 40 + Horst.horst_sign(hashFunctions2, bArr3, 40, bArr6, bArr7, bArr11, bArr5);
        int i5 = 0;
        for (int i6 = 12; i5 < i6; i6 = 12) {
            leafaddr2.level = i5;
            Seed.get_seed(hashFunctions2, bArr7, 0, bArr12, leafaddr2);
            wots.wots_sign(hashFunctions2, bArr3, horst_sign, bArr6, bArr7, bArr11);
            int i7 = horst_sign + 2144;
            compute_authpath_wots(hashFunctions2, bArr6, bArr3, i7, leafaddr2, bArr12, bArr11, 5);
            horst_sign = i7 + 160;
            leafaddr2.subleaf = (long) ((int) (leafaddr2.subtree & 31));
            leafaddr2.subtree >>>= 5;
            i5++;
            bArr11 = bArr11;
            bArr12 = bArr12;
        }
        zerobytes(bArr12, 0, 1088);
        return bArr3;
    }

    @Override // org.bouncycastle.pqc.crypto.MessageSigner
    public byte[] generateSignature(byte[] bArr) {
        return crypto_sign(this.hashFunctions, bArr, this.keyData);
    }

    @Override // org.bouncycastle.pqc.crypto.MessageSigner
    public void init(boolean z, CipherParameters cipherParameters) {
        this.keyData = z ? cipherParameters instanceof ParametersWithRandom ? ((SPHINCSPrivateKeyParameters) ((ParametersWithRandom) cipherParameters).getParameters()).getKeyData() : ((SPHINCSPrivateKeyParameters) cipherParameters).getKeyData() : ((SPHINCSPublicKeyParameters) cipherParameters).getKeyData();
    }

    /* access modifiers changed from: package-private */
    public boolean verify(HashFunctions hashFunctions2, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        byte[] bArr4 = new byte[2144];
        byte[] bArr5 = new byte[32];
        byte[] bArr6 = new byte[32];
        byte[] bArr7 = new byte[41000];
        byte[] bArr8 = new byte[1056];
        if (bArr2.length == 41000) {
            byte[] bArr9 = new byte[64];
            for (int i = 0; i < 1056; i++) {
                bArr8[i] = bArr3[i];
            }
            byte[] bArr10 = new byte[32];
            for (int i2 = 0; i2 < 32; i2++) {
                bArr10[i2] = bArr2[i2];
            }
            System.arraycopy(bArr2, 0, bArr7, 0, 41000);
            Digest messageHash = hashFunctions2.getMessageHash();
            messageHash.update(bArr10, 0, 32);
            messageHash.update(bArr8, 0, 1056);
            messageHash.update(bArr, 0, bArr.length);
            messageHash.doFinal(bArr9, 0);
            long j = 0;
            for (int i3 = 0; i3 < 8; i3++) {
                j ^= ((long) (bArr7[32 + i3] & 255)) << (i3 * 8);
            }
            new Horst();
            Horst.horst_verify(hashFunctions2, bArr6, bArr7, 40, bArr8, bArr9);
            Wots wots = new Wots();
            int i4 = 13352;
            int i5 = 0;
            long j2 = j;
            while (i5 < 12) {
                wots.wots_verify(hashFunctions2, bArr4, bArr7, i4, bArr6, bArr8);
                int i6 = i4 + 2144;
                Tree.l_tree(hashFunctions2, bArr5, 0, bArr4, 0, bArr8, 0);
                validate_authpath(hashFunctions2, bArr6, bArr5, (int) (j2 & 31), bArr7, i6, bArr8, 5);
                j2 >>= 5;
                i4 = i6 + 160;
                i5++;
                bArr7 = bArr7;
                bArr8 = bArr8;
            }
            boolean z = true;
            for (int i7 = 0; i7 < 32; i7++) {
                if (bArr6[i7] != bArr8[i7 + 1024]) {
                    z = false;
                }
            }
            return z;
        }
        throw new IllegalArgumentException("signature wrong size");
    }

    @Override // org.bouncycastle.pqc.crypto.MessageSigner
    public boolean verifySignature(byte[] bArr, byte[] bArr2) {
        return verify(this.hashFunctions, bArr, bArr2, this.keyData);
    }
}
