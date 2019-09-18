package org.bouncycastle.pqc.crypto.sphincs;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.tls.CipherSuite;
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
        Tree.leafaddr leafaddr2 = leafaddr;
        Tree.leafaddr leafaddr3 = new Tree.leafaddr(leafaddr2);
        byte[] bArr5 = new byte[2048];
        byte[] bArr6 = new byte[1024];
        byte[] bArr7 = new byte[68608];
        leafaddr3.subleaf = 0;
        while (leafaddr3.subleaf < 32) {
            Seed.get_seed(hashFunctions2, bArr6, (int) (leafaddr3.subleaf * 32), bArr3, leafaddr3);
            leafaddr3.subleaf++;
        }
        HashFunctions hashFunctions3 = hashFunctions2;
        Wots wots = new Wots();
        leafaddr3.subleaf = 0;
        while (leafaddr3.subleaf < 32) {
            wots.wots_pkgen(hashFunctions3, bArr7, (int) (leafaddr3.subleaf * 67 * 32), bArr6, (int) (leafaddr3.subleaf * 32), bArr4, 0);
            leafaddr3.subleaf++;
            hashFunctions3 = hashFunctions2;
            wots = wots;
        }
        leafaddr3.subleaf = 0;
        while (leafaddr3.subleaf < 32) {
            Tree.l_tree(hashFunctions2, bArr5, (int) (1024 + (leafaddr3.subleaf * 32)), bArr7, (int) (leafaddr3.subleaf * 67 * 32), bArr4, 0);
            leafaddr3.subleaf++;
        }
        int i3 = 0;
        for (int i4 = 32; i4 > 0; i4 >>>= 1) {
            for (int i5 = 0; i5 < i4; i5 += 2) {
                hashFunctions2.hash_2n_n_mask(bArr5, ((i4 >>> 1) * 32) + ((i5 >>> 1) * 32), bArr5, (i4 * 32) + (i5 * 32), bArr4, 2 * (7 + i3) * 32);
            }
            i3++;
        }
        int i6 = (int) leafaddr2.subleaf;
        int i7 = i2;
        for (int i8 = 0; i8 < i7; i8++) {
            System.arraycopy(bArr5, ((32 >>> i8) * 32) + (((i6 >>> i8) ^ 1) * 32), bArr2, i + (i8 * 32), 32);
        }
        System.arraycopy(bArr5, 32, bArr, 0, 32);
    }

    static void validate_authpath(HashFunctions hashFunctions2, byte[] bArr, byte[] bArr2, int i, byte[] bArr3, int i2, byte[] bArr4, int i3) {
        byte[] bArr5 = new byte[64];
        if ((i & 1) != 0) {
            for (int i4 = 0; i4 < 32; i4++) {
                bArr5[32 + i4] = bArr2[i4];
            }
            for (int i5 = 0; i5 < 32; i5++) {
                bArr5[i5] = bArr3[i2 + i5];
            }
        } else {
            for (int i6 = 0; i6 < 32; i6++) {
                bArr5[i6] = bArr2[i6];
            }
            for (int i7 = 0; i7 < 32; i7++) {
                bArr5[32 + i7] = bArr3[i2 + i7];
            }
        }
        int i8 = i;
        int i9 = i2 + 32;
        int i10 = 0;
        while (i10 < i3 - 1) {
            int i11 = i8 >>> 1;
            if ((i11 & 1) != 0) {
                hashFunctions2.hash_2n_n_mask(bArr5, 32, bArr5, 0, bArr4, 2 * (7 + i10) * 32);
                for (int i12 = 0; i12 < 32; i12++) {
                    bArr5[i12] = bArr3[i9 + i12];
                }
            } else {
                hashFunctions2.hash_2n_n_mask(bArr5, 0, bArr5, 0, bArr4, 2 * (7 + i10) * 32);
                for (int i13 = 0; i13 < 32; i13++) {
                    bArr5[i13 + 32] = bArr3[i9 + i13];
                }
            }
            i9 += 32;
            i10++;
            i8 = i11;
        }
        hashFunctions2.hash_2n_n_mask(bArr, 0, bArr5, 0, bArr4, 2 * ((7 + i3) - 1) * 32);
    }

    private void zerobytes(byte[] bArr, int i, int i2) {
        for (int i3 = 0; i3 != i2; i3++) {
            bArr[i + i3] = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] crypto_sign(HashFunctions hashFunctions2, byte[] bArr, byte[] bArr2) {
        HashFunctions hashFunctions3 = hashFunctions2;
        byte[] bArr3 = bArr;
        byte[] bArr4 = new byte[41000];
        byte[] bArr5 = new byte[32];
        byte[] bArr6 = new byte[64];
        long[] jArr = new long[8];
        byte[] bArr7 = new byte[32];
        byte[] bArr8 = new byte[32];
        byte[] bArr9 = new byte[1024];
        byte[] bArr10 = new byte[1088];
        for (int i = 0; i < 1088; i++) {
            bArr10[i] = bArr2[i];
        }
        System.arraycopy(bArr10, 1056, bArr4, 40968, 32);
        Digest messageHash = hashFunctions2.getMessageHash();
        byte[] bArr11 = new byte[messageHash.getDigestSize()];
        messageHash.update(bArr4, 40968, 32);
        messageHash.update(bArr3, 0, bArr3.length);
        messageHash.doFinal(bArr11, 0);
        zerobytes(bArr4, 40968, 32);
        for (int i2 = 0; i2 != jArr.length; i2++) {
            jArr[i2] = Pack.littleEndianToLong(bArr11, i2 * 8);
        }
        long j = jArr[0] & 1152921504606846975L;
        System.arraycopy(bArr11, 16, bArr5, 0, 32);
        System.arraycopy(bArr5, 0, bArr4, 39912, 32);
        Tree.leafaddr leafaddr = new Tree.leafaddr();
        leafaddr.level = 11;
        byte[] bArr12 = bArr5;
        leafaddr.subtree = 0;
        leafaddr.subleaf = 0;
        System.arraycopy(bArr10, 32, bArr4, 39944, 1024);
        byte[] bArr13 = bArr10;
        byte[] bArr14 = bArr9;
        byte[] bArr15 = bArr8;
        byte[] bArr16 = bArr7;
        Tree.treehash(hashFunctions3, bArr4, 40968, 5, bArr13, leafaddr, bArr4, 39944);
        Digest messageHash2 = hashFunctions2.getMessageHash();
        messageHash2.update(bArr4, 39912, 1088);
        messageHash2.update(bArr3, 0, bArr3.length);
        messageHash2.doFinal(bArr6, 0);
        Tree.leafaddr leafaddr2 = new Tree.leafaddr();
        leafaddr2.level = 12;
        leafaddr2.subleaf = (long) ((int) (j & 31));
        leafaddr2.subtree = j >>> 5;
        for (int i3 = 0; i3 < 32; i3++) {
            bArr4[i3] = bArr12[i3];
        }
        byte[] bArr17 = bArr13;
        System.arraycopy(bArr17, 32, bArr14, 0, 1024);
        for (int i4 = 0; i4 < 8; i4++) {
            bArr4[32 + i4] = (byte) ((int) ((j >>> (8 * i4)) & 255));
        }
        byte[] bArr18 = bArr15;
        Seed.get_seed(hashFunctions3, bArr18, 0, bArr17, leafaddr2);
        new Horst();
        byte[] bArr19 = bArr18;
        Wots wots = new Wots();
        int horst_sign = 40 + Horst.horst_sign(hashFunctions3, bArr4, 40, bArr16, bArr18, bArr14, bArr6);
        int i5 = 0;
        for (int i6 = 12; i5 < i6; i6 = 12) {
            leafaddr2.level = i5;
            Seed.get_seed(hashFunctions3, bArr19, 0, bArr17, leafaddr2);
            byte[] bArr20 = bArr4;
            int i7 = i5;
            byte[] bArr21 = bArr14;
            wots.wots_sign(hashFunctions3, bArr20, horst_sign, bArr16, bArr19, bArr21);
            int i8 = horst_sign + 2144;
            compute_authpath_wots(hashFunctions3, bArr16, bArr20, i8, leafaddr2, bArr17, bArr21, 5);
            horst_sign = i8 + CipherSuite.TLS_DH_RSA_WITH_AES_128_GCM_SHA256;
            leafaddr2.subleaf = (long) ((int) (leafaddr2.subtree & 31));
            leafaddr2.subtree >>>= 5;
            i5 = i7 + 1;
            bArr17 = bArr17;
            hashFunctions3 = hashFunctions2;
        }
        zerobytes(bArr17, 0, 1088);
        return bArr4;
    }

    public byte[] generateSignature(byte[] bArr) {
        return crypto_sign(this.hashFunctions, bArr, this.keyData);
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        this.keyData = z ? ((SPHINCSPrivateKeyParameters) cipherParameters).getKeyData() : ((SPHINCSPublicKeyParameters) cipherParameters).getKeyData();
    }

    /* access modifiers changed from: package-private */
    public boolean verify(HashFunctions hashFunctions2, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        byte[] bArr4 = bArr;
        byte[] bArr5 = bArr2;
        byte[] bArr6 = new byte[2144];
        byte[] bArr7 = new byte[32];
        byte[] bArr8 = new byte[32];
        byte[] bArr9 = new byte[41000];
        byte[] bArr10 = new byte[1056];
        if (bArr5.length == 41000) {
            byte[] bArr11 = new byte[64];
            for (int i = 0; i < 1056; i++) {
                bArr10[i] = bArr3[i];
            }
            byte[] bArr12 = new byte[32];
            for (int i2 = 0; i2 < 32; i2++) {
                bArr12[i2] = bArr5[i2];
            }
            System.arraycopy(bArr5, 0, bArr9, 0, 41000);
            Digest messageHash = hashFunctions2.getMessageHash();
            messageHash.update(bArr12, 0, 32);
            messageHash.update(bArr10, 0, 1056);
            messageHash.update(bArr4, 0, bArr4.length);
            messageHash.doFinal(bArr11, 0);
            long j = 0;
            for (int i3 = 0; i3 < 8; i3++) {
                j ^= ((long) (bArr9[32 + i3] & 255)) << (8 * i3);
            }
            new Horst();
            Horst.horst_verify(hashFunctions2, bArr8, bArr9, 40, bArr10, bArr11);
            Wots wots = new Wots();
            int i4 = 13352;
            int i5 = 0;
            long j2 = j;
            while (i5 < 12) {
                byte[] bArr13 = bArr10;
                wots.wots_verify(hashFunctions2, bArr6, bArr9, i4, bArr8, bArr10);
                int i6 = i4 + 2144;
                Tree.l_tree(hashFunctions2, bArr7, 0, bArr6, 0, bArr13, 0);
                byte[] bArr14 = bArr9;
                byte[] bArr15 = bArr8;
                byte[] bArr16 = bArr7;
                validate_authpath(hashFunctions2, bArr8, bArr7, (int) (j2 & 31), bArr14, i6, bArr13, 5);
                j2 >>= 5;
                i4 = i6 + CipherSuite.TLS_DH_RSA_WITH_AES_128_GCM_SHA256;
                i5++;
                bArr9 = bArr14;
                bArr10 = bArr13;
            }
            byte[] bArr17 = bArr10;
            byte[] bArr18 = bArr8;
            boolean z = true;
            for (int i7 = 0; i7 < 32; i7++) {
                if (bArr18[i7] != bArr17[i7 + 1024]) {
                    z = false;
                }
            }
            return z;
        }
        throw new IllegalArgumentException("signature wrong size");
    }

    public boolean verifySignature(byte[] bArr, byte[] bArr2) {
        return verify(this.hashFunctions, bArr, bArr2, this.keyData);
    }
}
