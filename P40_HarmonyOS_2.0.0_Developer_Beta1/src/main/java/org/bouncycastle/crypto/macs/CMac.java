package org.bouncycastle.crypto.macs;

import org.bouncycastle.asn1.eac.CertificateHolderAuthorization;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;
import org.bouncycastle.util.Pack;

public class CMac implements Mac {
    private byte[] Lu;
    private byte[] Lu2;
    private byte[] ZEROES;
    private byte[] buf;
    private int bufOff;
    private BlockCipher cipher;
    private byte[] mac;
    private int macSize;
    private byte[] poly;

    public CMac(BlockCipher blockCipher) {
        this(blockCipher, blockCipher.getBlockSize() * 8);
    }

    public CMac(BlockCipher blockCipher, int i) {
        if (i % 8 != 0) {
            throw new IllegalArgumentException("MAC size must be multiple of 8");
        } else if (i <= blockCipher.getBlockSize() * 8) {
            this.cipher = new CBCBlockCipher(blockCipher);
            this.macSize = i / 8;
            this.poly = lookupPoly(blockCipher.getBlockSize());
            this.mac = new byte[blockCipher.getBlockSize()];
            this.buf = new byte[blockCipher.getBlockSize()];
            this.ZEROES = new byte[blockCipher.getBlockSize()];
            this.bufOff = 0;
        } else {
            throw new IllegalArgumentException("MAC size must be less or equal to " + (blockCipher.getBlockSize() * 8));
        }
    }

    private byte[] doubleLu(byte[] bArr) {
        byte[] bArr2 = new byte[bArr.length];
        int i = (-shiftLeft(bArr, bArr2)) & GF2Field.MASK;
        int length = bArr.length - 3;
        byte b = bArr2[length];
        byte[] bArr3 = this.poly;
        bArr2[length] = (byte) (b ^ (bArr3[1] & i));
        int length2 = bArr.length - 2;
        bArr2[length2] = (byte) ((bArr3[2] & i) ^ bArr2[length2]);
        int length3 = bArr.length - 1;
        bArr2[length3] = (byte) ((i & bArr3[3]) ^ bArr2[length3]);
        return bArr2;
    }

    private static byte[] lookupPoly(int i) {
        int i2 = i * 8;
        int i3 = 135;
        switch (i2) {
            case 64:
            case 320:
                i3 = 27;
                break;
            case 128:
            case CertificateHolderAuthorization.CVCA /* 192 */:
                break;
            case 160:
                i3 = 45;
                break;
            case 224:
                i3 = 777;
                break;
            case 256:
                i3 = 1061;
                break;
            case 384:
                i3 = 4109;
                break;
            case 448:
                i3 = 2129;
                break;
            case 512:
                i3 = 293;
                break;
            case 768:
                i3 = 655377;
                break;
            case 1024:
                i3 = 524355;
                break;
            case 2048:
                i3 = 548865;
                break;
            default:
                throw new IllegalArgumentException("Unknown block size for CMAC: " + i2);
        }
        return Pack.intToBigEndian(i3);
    }

    private static int shiftLeft(byte[] bArr, byte[] bArr2) {
        int length = bArr.length;
        int i = 0;
        while (true) {
            length--;
            if (length < 0) {
                return i;
            }
            int i2 = bArr[length] & 255;
            bArr2[length] = (byte) (i | (i2 << 1));
            i = (i2 >>> 7) & 1;
        }
    }

    @Override // org.bouncycastle.crypto.Mac
    public int doFinal(byte[] bArr, int i) {
        byte[] bArr2;
        if (this.bufOff == this.cipher.getBlockSize()) {
            bArr2 = this.Lu;
        } else {
            new ISO7816d4Padding().addPadding(this.buf, this.bufOff);
            bArr2 = this.Lu2;
        }
        int i2 = 0;
        while (true) {
            byte[] bArr3 = this.mac;
            if (i2 < bArr3.length) {
                byte[] bArr4 = this.buf;
                bArr4[i2] = (byte) (bArr4[i2] ^ bArr2[i2]);
                i2++;
            } else {
                this.cipher.processBlock(this.buf, 0, bArr3, 0);
                System.arraycopy(this.mac, 0, bArr, i, this.macSize);
                reset();
                return this.macSize;
            }
        }
    }

    @Override // org.bouncycastle.crypto.Mac
    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName();
    }

    @Override // org.bouncycastle.crypto.Mac
    public int getMacSize() {
        return this.macSize;
    }

    @Override // org.bouncycastle.crypto.Mac
    public void init(CipherParameters cipherParameters) {
        validate(cipherParameters);
        this.cipher.init(true, cipherParameters);
        byte[] bArr = this.ZEROES;
        byte[] bArr2 = new byte[bArr.length];
        this.cipher.processBlock(bArr, 0, bArr2, 0);
        this.Lu = doubleLu(bArr2);
        this.Lu2 = doubleLu(this.Lu);
        reset();
    }

    @Override // org.bouncycastle.crypto.Mac
    public void reset() {
        int i = 0;
        while (true) {
            byte[] bArr = this.buf;
            if (i < bArr.length) {
                bArr[i] = 0;
                i++;
            } else {
                this.bufOff = 0;
                this.cipher.reset();
                return;
            }
        }
    }

    @Override // org.bouncycastle.crypto.Mac
    public void update(byte b) {
        int i = this.bufOff;
        byte[] bArr = this.buf;
        if (i == bArr.length) {
            this.cipher.processBlock(bArr, 0, this.mac, 0);
            this.bufOff = 0;
        }
        byte[] bArr2 = this.buf;
        int i2 = this.bufOff;
        this.bufOff = i2 + 1;
        bArr2[i2] = b;
    }

    @Override // org.bouncycastle.crypto.Mac
    public void update(byte[] bArr, int i, int i2) {
        if (i2 >= 0) {
            int blockSize = this.cipher.getBlockSize();
            int i3 = this.bufOff;
            int i4 = blockSize - i3;
            if (i2 > i4) {
                System.arraycopy(bArr, i, this.buf, i3, i4);
                this.cipher.processBlock(this.buf, 0, this.mac, 0);
                this.bufOff = 0;
                i2 -= i4;
                i += i4;
                while (i2 > blockSize) {
                    this.cipher.processBlock(bArr, i, this.mac, 0);
                    i2 -= blockSize;
                    i += blockSize;
                }
            }
            System.arraycopy(bArr, i, this.buf, this.bufOff, i2);
            this.bufOff += i2;
            return;
        }
        throw new IllegalArgumentException("Can't have a negative input length!");
    }

    /* access modifiers changed from: package-private */
    public void validate(CipherParameters cipherParameters) {
        if (cipherParameters != null && !(cipherParameters instanceof KeyParameter)) {
            throw new IllegalArgumentException("CMac mode only permits key to be set.");
        }
    }
}
