package org.bouncycastle.crypto.engines;

import java.security.SecureRandom;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.Wrapper;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.ParametersWithRandom;

public class RFC3211WrapEngine implements Wrapper {
    private CBCBlockCipher engine;
    private boolean forWrapping;
    private ParametersWithIV param;
    private SecureRandom rand;

    public RFC3211WrapEngine(BlockCipher blockCipher) {
        this.engine = new CBCBlockCipher(blockCipher);
    }

    public String getAlgorithmName() {
        return this.engine.getUnderlyingCipher().getAlgorithmName() + "/RFC3211Wrap";
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        this.forWrapping = z;
        if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.rand = parametersWithRandom.getRandom();
            this.param = (ParametersWithIV) parametersWithRandom.getParameters();
            return;
        }
        if (z) {
            this.rand = CryptoServicesRegistrar.getSecureRandom();
        }
        this.param = (ParametersWithIV) cipherParameters;
    }

    public byte[] unwrap(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        if (!this.forWrapping) {
            int blockSize = this.engine.getBlockSize();
            if (i2 >= 2 * blockSize) {
                byte[] bArr2 = new byte[i2];
                byte[] bArr3 = new byte[blockSize];
                int i3 = 0;
                System.arraycopy(bArr, i, bArr2, 0, i2);
                System.arraycopy(bArr, i, bArr3, 0, bArr3.length);
                this.engine.init(false, new ParametersWithIV(this.param.getParameters(), bArr3));
                for (int i4 = blockSize; i4 < bArr2.length; i4 += blockSize) {
                    this.engine.processBlock(bArr2, i4, bArr2, i4);
                }
                System.arraycopy(bArr2, bArr2.length - bArr3.length, bArr3, 0, bArr3.length);
                this.engine.init(false, new ParametersWithIV(this.param.getParameters(), bArr3));
                this.engine.processBlock(bArr2, 0, bArr2, 0);
                this.engine.init(false, this.param);
                for (int i5 = 0; i5 < bArr2.length; i5 += blockSize) {
                    this.engine.processBlock(bArr2, i5, bArr2, i5);
                }
                if ((bArr2[0] & 255) <= bArr2.length - 4) {
                    byte[] bArr4 = new byte[(bArr2[0] & 255)];
                    System.arraycopy(bArr2, 4, bArr4, 0, bArr2[0]);
                    byte b = 0;
                    while (i3 != 3) {
                        int i6 = 1 + i3;
                        b |= ((byte) (~bArr2[i6])) ^ bArr4[i3];
                        i3 = i6;
                    }
                    if (b == 0) {
                        return bArr4;
                    }
                    throw new InvalidCipherTextException("wrapped key fails checksum");
                }
                throw new InvalidCipherTextException("wrapped key corrupted");
            }
            throw new InvalidCipherTextException("input too short");
        }
        throw new IllegalStateException("not set for unwrapping");
    }

    public byte[] wrap(byte[] bArr, int i, int i2) {
        if (this.forWrapping) {
            this.engine.init(true, this.param);
            int blockSize = this.engine.getBlockSize();
            int i3 = i2 + 4;
            int i4 = blockSize * 2;
            if (i3 >= i4) {
                i4 = i3 % blockSize == 0 ? i3 : ((i3 / blockSize) + 1) * blockSize;
            }
            byte[] bArr2 = new byte[i4];
            bArr2[0] = (byte) i2;
            bArr2[1] = (byte) (~bArr[i]);
            bArr2[2] = (byte) (~bArr[i + 1]);
            bArr2[3] = (byte) (~bArr[i + 2]);
            System.arraycopy(bArr, i, bArr2, 4, i2);
            byte[] bArr3 = new byte[(bArr2.length - i3)];
            this.rand.nextBytes(bArr3);
            System.arraycopy(bArr3, 0, bArr2, i3, bArr3.length);
            for (int i5 = 0; i5 < bArr2.length; i5 += blockSize) {
                this.engine.processBlock(bArr2, i5, bArr2, i5);
            }
            for (int i6 = 0; i6 < bArr2.length; i6 += blockSize) {
                this.engine.processBlock(bArr2, i6, bArr2, i6);
            }
            return bArr2;
        }
        throw new IllegalStateException("not set for wrapping");
    }
}
