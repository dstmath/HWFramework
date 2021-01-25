package org.bouncycastle.crypto.engines;

import java.util.ArrayList;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.Wrapper;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.util.Arrays;

public class DSTU7624WrapEngine implements Wrapper {
    private static final int BYTES_IN_INTEGER = 4;
    private byte[] B = new byte[(this.engine.getBlockSize() / 2)];
    private ArrayList<byte[]> Btemp = new ArrayList<>();
    private byte[] checkSumArray = new byte[this.engine.getBlockSize()];
    private DSTU7624Engine engine;
    private boolean forWrapping;
    private byte[] intArray = new byte[4];
    private byte[] zeroArray = new byte[this.engine.getBlockSize()];

    public DSTU7624WrapEngine(int i) {
        this.engine = new DSTU7624Engine(i);
    }

    private void intToBytes(int i, byte[] bArr, int i2) {
        bArr[i2 + 3] = (byte) (i >> 24);
        bArr[i2 + 2] = (byte) (i >> 16);
        bArr[i2 + 1] = (byte) (i >> 8);
        bArr[i2] = (byte) i;
    }

    @Override // org.bouncycastle.crypto.Wrapper
    public String getAlgorithmName() {
        return "DSTU7624WrapEngine";
    }

    @Override // org.bouncycastle.crypto.Wrapper
    public void init(boolean z, CipherParameters cipherParameters) {
        if (cipherParameters instanceof ParametersWithRandom) {
            cipherParameters = ((ParametersWithRandom) cipherParameters).getParameters();
        }
        this.forWrapping = z;
        if (cipherParameters instanceof KeyParameter) {
            this.engine.init(z, cipherParameters);
            return;
        }
        throw new IllegalArgumentException("invalid parameters passed to DSTU7624WrapEngine");
    }

    @Override // org.bouncycastle.crypto.Wrapper
    public byte[] unwrap(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        if (this.forWrapping) {
            throw new IllegalStateException("not set for unwrapping");
        } else if (i2 % this.engine.getBlockSize() == 0) {
            int blockSize = (i2 * 2) / this.engine.getBlockSize();
            int i3 = blockSize - 1;
            int i4 = i3 * 6;
            byte[] bArr2 = new byte[i2];
            System.arraycopy(bArr, i, bArr2, 0, i2);
            byte[] bArr3 = new byte[(this.engine.getBlockSize() / 2)];
            System.arraycopy(bArr2, 0, bArr3, 0, this.engine.getBlockSize() / 2);
            this.Btemp.clear();
            int length = bArr2.length - (this.engine.getBlockSize() / 2);
            int blockSize2 = this.engine.getBlockSize() / 2;
            while (length != 0) {
                byte[] bArr4 = new byte[(this.engine.getBlockSize() / 2)];
                System.arraycopy(bArr2, blockSize2, bArr4, 0, this.engine.getBlockSize() / 2);
                this.Btemp.add(bArr4);
                length -= this.engine.getBlockSize() / 2;
                blockSize2 += this.engine.getBlockSize() / 2;
            }
            for (int i5 = 0; i5 < i4; i5++) {
                System.arraycopy(this.Btemp.get(blockSize - 2), 0, bArr2, 0, this.engine.getBlockSize() / 2);
                System.arraycopy(bArr3, 0, bArr2, this.engine.getBlockSize() / 2, this.engine.getBlockSize() / 2);
                intToBytes(i4 - i5, this.intArray, 0);
                for (int i6 = 0; i6 < 4; i6++) {
                    int blockSize3 = (this.engine.getBlockSize() / 2) + i6;
                    bArr2[blockSize3] = (byte) (bArr2[blockSize3] ^ this.intArray[i6]);
                }
                this.engine.processBlock(bArr2, 0, bArr2, 0);
                System.arraycopy(bArr2, 0, bArr3, 0, this.engine.getBlockSize() / 2);
                for (int i7 = 2; i7 < blockSize; i7++) {
                    int i8 = blockSize - i7;
                    System.arraycopy(this.Btemp.get(i8 - 1), 0, this.Btemp.get(i8), 0, this.engine.getBlockSize() / 2);
                }
                System.arraycopy(bArr2, this.engine.getBlockSize() / 2, this.Btemp.get(0), 0, this.engine.getBlockSize() / 2);
            }
            System.arraycopy(bArr3, 0, bArr2, 0, this.engine.getBlockSize() / 2);
            int blockSize4 = this.engine.getBlockSize() / 2;
            for (int i9 = 0; i9 < i3; i9++) {
                System.arraycopy(this.Btemp.get(i9), 0, bArr2, blockSize4, this.engine.getBlockSize() / 2);
                blockSize4 += this.engine.getBlockSize() / 2;
            }
            System.arraycopy(bArr2, bArr2.length - this.engine.getBlockSize(), this.checkSumArray, 0, this.engine.getBlockSize());
            byte[] bArr5 = new byte[(bArr2.length - this.engine.getBlockSize())];
            if (Arrays.areEqual(this.checkSumArray, this.zeroArray)) {
                System.arraycopy(bArr2, 0, bArr5, 0, bArr2.length - this.engine.getBlockSize());
                return bArr5;
            }
            throw new InvalidCipherTextException("checksum failed");
        } else {
            throw new DataLengthException("unwrap data must be a multiple of " + this.engine.getBlockSize() + " bytes");
        }
    }

    @Override // org.bouncycastle.crypto.Wrapper
    public byte[] wrap(byte[] bArr, int i, int i2) {
        if (!this.forWrapping) {
            throw new IllegalStateException("not set for wrapping");
        } else if (i2 % this.engine.getBlockSize() != 0) {
            throw new DataLengthException("wrap data must be a multiple of " + this.engine.getBlockSize() + " bytes");
        } else if (i + i2 <= bArr.length) {
            int blockSize = ((i2 / this.engine.getBlockSize()) + 1) * 2;
            int i3 = blockSize - 1;
            int i4 = i3 * 6;
            byte[] bArr2 = new byte[(this.engine.getBlockSize() + i2)];
            System.arraycopy(bArr, i, bArr2, 0, i2);
            System.arraycopy(bArr2, 0, this.B, 0, this.engine.getBlockSize() / 2);
            this.Btemp.clear();
            int length = bArr2.length - (this.engine.getBlockSize() / 2);
            int blockSize2 = this.engine.getBlockSize() / 2;
            while (length != 0) {
                byte[] bArr3 = new byte[(this.engine.getBlockSize() / 2)];
                System.arraycopy(bArr2, blockSize2, bArr3, 0, this.engine.getBlockSize() / 2);
                this.Btemp.add(bArr3);
                length -= this.engine.getBlockSize() / 2;
                blockSize2 += this.engine.getBlockSize() / 2;
            }
            int i5 = 0;
            while (i5 < i4) {
                System.arraycopy(this.B, 0, bArr2, 0, this.engine.getBlockSize() / 2);
                System.arraycopy(this.Btemp.get(0), 0, bArr2, this.engine.getBlockSize() / 2, this.engine.getBlockSize() / 2);
                this.engine.processBlock(bArr2, 0, bArr2, 0);
                i5++;
                intToBytes(i5, this.intArray, 0);
                for (int i6 = 0; i6 < 4; i6++) {
                    int blockSize3 = (this.engine.getBlockSize() / 2) + i6;
                    bArr2[blockSize3] = (byte) (bArr2[blockSize3] ^ this.intArray[i6]);
                }
                System.arraycopy(bArr2, this.engine.getBlockSize() / 2, this.B, 0, this.engine.getBlockSize() / 2);
                for (int i7 = 2; i7 < blockSize; i7++) {
                    System.arraycopy(this.Btemp.get(i7 - 1), 0, this.Btemp.get(i7 - 2), 0, this.engine.getBlockSize() / 2);
                }
                System.arraycopy(bArr2, 0, this.Btemp.get(blockSize - 2), 0, this.engine.getBlockSize() / 2);
            }
            System.arraycopy(this.B, 0, bArr2, 0, this.engine.getBlockSize() / 2);
            int blockSize4 = this.engine.getBlockSize() / 2;
            for (int i8 = 0; i8 < i3; i8++) {
                System.arraycopy(this.Btemp.get(i8), 0, bArr2, blockSize4, this.engine.getBlockSize() / 2);
                blockSize4 += this.engine.getBlockSize() / 2;
            }
            return bArr2;
        } else {
            throw new DataLengthException("input buffer too short");
        }
    }
}
