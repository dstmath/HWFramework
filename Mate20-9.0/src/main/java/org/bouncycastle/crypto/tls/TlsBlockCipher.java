package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.security.SecureRandom;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class TlsBlockCipher implements TlsCipher {
    protected TlsContext context;
    protected BlockCipher decryptCipher;
    protected BlockCipher encryptCipher;
    protected boolean encryptThenMAC;
    protected byte[] randomData = new byte[256];
    protected TlsMac readMac;
    protected boolean useExplicitIV;
    protected TlsMac writeMac;

    public TlsBlockCipher(TlsContext tlsContext, BlockCipher blockCipher, BlockCipher blockCipher2, Digest digest, Digest digest2, int i) throws IOException {
        byte[] bArr;
        byte[] bArr2;
        ParametersWithIV parametersWithIV;
        ParametersWithIV parametersWithIV2;
        TlsContext tlsContext2 = tlsContext;
        BlockCipher blockCipher3 = blockCipher;
        BlockCipher blockCipher4 = blockCipher2;
        int i2 = i;
        this.context = tlsContext2;
        tlsContext.getNonceRandomGenerator().nextBytes(this.randomData);
        this.useExplicitIV = TlsUtils.isTLSv11(tlsContext);
        this.encryptThenMAC = tlsContext.getSecurityParameters().encryptThenMAC;
        int digestSize = !this.useExplicitIV ? (2 * i2) + digest.getDigestSize() + digest2.getDigestSize() + blockCipher.getBlockSize() + blockCipher2.getBlockSize() : r1;
        byte[] calculateKeyBlock = TlsUtils.calculateKeyBlock(tlsContext2, digestSize);
        TlsContext tlsContext3 = tlsContext2;
        byte[] bArr3 = calculateKeyBlock;
        TlsMac tlsMac = new TlsMac(tlsContext3, digest, bArr3, 0, digest.getDigestSize());
        int digestSize2 = 0 + digest.getDigestSize();
        TlsMac tlsMac2 = r1;
        TlsMac tlsMac3 = new TlsMac(tlsContext3, digest2, bArr3, digestSize2, digest2.getDigestSize());
        int digestSize3 = digestSize2 + digest2.getDigestSize();
        KeyParameter keyParameter = new KeyParameter(calculateKeyBlock, digestSize3, i2);
        int i3 = digestSize3 + i2;
        KeyParameter keyParameter2 = new KeyParameter(calculateKeyBlock, i3, i2);
        int i4 = i3 + i2;
        if (this.useExplicitIV) {
            bArr2 = new byte[blockCipher.getBlockSize()];
            bArr = new byte[blockCipher2.getBlockSize()];
        } else {
            bArr2 = Arrays.copyOfRange(calculateKeyBlock, i4, blockCipher.getBlockSize() + i4);
            int blockSize = i4 + blockCipher.getBlockSize();
            bArr = Arrays.copyOfRange(calculateKeyBlock, blockSize, blockCipher2.getBlockSize() + blockSize);
            i4 = blockSize + blockCipher2.getBlockSize();
        }
        if (i4 == digestSize) {
            if (tlsContext.isServer()) {
                this.writeMac = tlsMac2;
                this.readMac = tlsMac;
                this.encryptCipher = blockCipher4;
                this.decryptCipher = blockCipher3;
                parametersWithIV = new ParametersWithIV(keyParameter2, bArr);
                parametersWithIV2 = new ParametersWithIV(keyParameter, bArr2);
            } else {
                this.writeMac = tlsMac;
                this.readMac = tlsMac2;
                this.encryptCipher = blockCipher3;
                this.decryptCipher = blockCipher4;
                parametersWithIV = new ParametersWithIV(keyParameter, bArr2);
                parametersWithIV2 = new ParametersWithIV(keyParameter2, bArr);
            }
            this.encryptCipher.init(true, parametersWithIV);
            this.decryptCipher.init(false, parametersWithIV2);
            return;
        }
        throw new TlsFatalAlert(80);
    }

    /* access modifiers changed from: protected */
    public int checkPaddingConstantTime(byte[] bArr, int i, int i2, int i3, int i4) {
        byte b;
        int i5;
        int i6 = i + i2;
        byte b2 = bArr[i6 - 1];
        int i7 = (b2 & 255) + 1;
        if ((!TlsUtils.isSSL(this.context) || i7 <= i3) && i4 + i7 <= i2) {
            int i8 = i6 - i7;
            b = 0;
            while (true) {
                int i9 = i8 + 1;
                b = (byte) (b | (bArr[i8] ^ b2));
                if (i9 >= i6) {
                    break;
                }
                i8 = i9;
            }
            i5 = b != 0 ? 0 : i7;
        } else {
            i5 = 0;
            b = 0;
            i7 = 0;
        }
        byte[] bArr2 = this.randomData;
        while (i7 < 256) {
            b = (byte) (b | (bArr2[i7] ^ b2));
            i7++;
        }
        bArr2[0] = (byte) (b ^ bArr2[0]);
        return i5;
    }

    /* access modifiers changed from: protected */
    public int chooseExtraPadBlocks(SecureRandom secureRandom, int i) {
        return Math.min(lowestBitSet(secureRandom.nextInt()), i);
    }

    public byte[] decodeCiphertext(long j, short s, byte[] bArr, int i, int i2) throws IOException {
        int i3;
        byte[] bArr2 = bArr;
        int i4 = i;
        int i5 = i2;
        int blockSize = this.decryptCipher.getBlockSize();
        int size = this.readMac.getSize();
        int max = this.encryptThenMAC ? blockSize + size : Math.max(blockSize, size + 1);
        if (this.useExplicitIV) {
            max += blockSize;
        }
        if (i5 >= max) {
            int i6 = this.encryptThenMAC ? i5 - size : i5;
            if (i6 % blockSize == 0) {
                if (this.encryptThenMAC) {
                    int i7 = i4 + i5;
                    if (!Arrays.constantTimeAreEqual(this.readMac.calculateMac(j, s, bArr2, i4, i5 - size), Arrays.copyOfRange(bArr2, i7 - size, i7))) {
                        throw new TlsFatalAlert(20);
                    }
                }
                if (this.useExplicitIV) {
                    this.decryptCipher.init(false, new ParametersWithIV(null, bArr2, i4, blockSize));
                    i4 += blockSize;
                    i6 -= blockSize;
                }
                int i8 = i4;
                int i9 = i6;
                for (int i10 = 0; i10 < i9; i10 += blockSize) {
                    int i11 = i8 + i10;
                    this.decryptCipher.processBlock(bArr2, i11, bArr2, i11);
                }
                int checkPaddingConstantTime = checkPaddingConstantTime(bArr2, i8, i9, blockSize, this.encryptThenMAC ? 0 : size);
                boolean z = checkPaddingConstantTime == 0;
                int i12 = i9 - checkPaddingConstantTime;
                if (!this.encryptThenMAC) {
                    i3 = i12 - size;
                    int i13 = i8 + i3;
                    z |= !Arrays.constantTimeAreEqual(this.readMac.calculateMacConstantTime(j, s, bArr2, i8, i3, i9 - size, this.randomData), Arrays.copyOfRange(bArr2, i13, i13 + size));
                } else {
                    i3 = i12;
                }
                if (!z) {
                    return Arrays.copyOfRange(bArr2, i8, i3 + i8);
                }
                throw new TlsFatalAlert(20);
            }
            throw new TlsFatalAlert(21);
        }
        throw new TlsFatalAlert(50);
    }

    public byte[] encodePlaintext(long j, short s, byte[] bArr, int i, int i2) {
        int i3;
        int i4;
        byte[] bArr2;
        int i5 = i2;
        int blockSize = this.encryptCipher.getBlockSize();
        int size = this.writeMac.getSize();
        ProtocolVersion serverVersion = this.context.getServerVersion();
        int i6 = (blockSize - 1) - ((!this.encryptThenMAC ? i5 + size : i5) % blockSize);
        if ((this.encryptThenMAC || !this.context.getSecurityParameters().truncatedHMac) && !serverVersion.isDTLS() && !serverVersion.isSSL()) {
            i6 += chooseExtraPadBlocks(this.context.getSecureRandom(), (255 - i6) / blockSize) * blockSize;
        }
        int i7 = i6;
        int i8 = size + i5 + i7 + 1;
        if (this.useExplicitIV) {
            i8 += blockSize;
        }
        byte[] bArr3 = new byte[i8];
        if (this.useExplicitIV) {
            byte[] bArr4 = new byte[blockSize];
            this.context.getNonceRandomGenerator().nextBytes(bArr4);
            this.encryptCipher.init(true, new ParametersWithIV(null, bArr4));
            System.arraycopy(bArr4, 0, bArr3, 0, blockSize);
            bArr2 = bArr;
            i4 = i;
            i3 = 0 + blockSize;
        } else {
            bArr2 = bArr;
            i4 = i;
            i3 = 0;
        }
        System.arraycopy(bArr2, i4, bArr3, i3, i5);
        int i9 = i3 + i5;
        if (!this.encryptThenMAC) {
            byte[] calculateMac = this.writeMac.calculateMac(j, s, bArr2, i4, i5);
            System.arraycopy(calculateMac, 0, bArr3, i9, calculateMac.length);
            i9 += calculateMac.length;
        }
        int i10 = i9;
        int i11 = 0;
        while (i11 <= i7) {
            bArr3[i10] = (byte) i7;
            i11++;
            i10++;
        }
        while (i3 < i10) {
            this.encryptCipher.processBlock(bArr3, i3, bArr3, i3);
            i3 += blockSize;
        }
        if (!this.encryptThenMAC) {
            return bArr3;
        }
        byte[] bArr5 = bArr3;
        byte[] calculateMac2 = this.writeMac.calculateMac(j, s, bArr3, 0, i10);
        System.arraycopy(calculateMac2, 0, bArr5, i10, calculateMac2.length);
        int length = calculateMac2.length;
        return bArr5;
    }

    public int getPlaintextLimit(int i) {
        int i2;
        int blockSize = this.encryptCipher.getBlockSize();
        int size = this.writeMac.getSize();
        if (this.useExplicitIV) {
            i -= blockSize;
        }
        if (this.encryptThenMAC) {
            int i3 = i - size;
            i2 = i3 - (i3 % blockSize);
        } else {
            i2 = (i - (i % blockSize)) - size;
        }
        return i2 - 1;
    }

    public TlsMac getReadMac() {
        return this.readMac;
    }

    public TlsMac getWriteMac() {
        return this.writeMac;
    }

    /* access modifiers changed from: protected */
    public int lowestBitSet(int i) {
        if (i == 0) {
            return 32;
        }
        int i2 = 0;
        while ((i & 1) == 0) {
            i2++;
            i >>= 1;
        }
        return i2;
    }
}
