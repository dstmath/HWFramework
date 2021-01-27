package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.engines.ChaCha7539Engine;
import org.bouncycastle.crypto.macs.Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class ChaCha20Poly1305 implements AEADCipher {
    private static final long AAD_LIMIT = -1;
    private static final int BUF_SIZE = 64;
    private static final long DATA_LIMIT = 274877906880L;
    private static final int KEY_SIZE = 32;
    private static final int MAC_SIZE = 16;
    private static final int NONCE_SIZE = 12;
    private static final byte[] ZEROES = new byte[15];
    private long aadCount;
    private final byte[] buf;
    private int bufPos;
    private final ChaCha7539Engine chacha20;
    private long dataCount;
    private byte[] initialAAD;
    private final byte[] key;
    private final byte[] mac;
    private final byte[] nonce;
    private final Mac poly1305;
    private int state;

    private static final class State {
        static final int DEC_AAD = 6;
        static final int DEC_DATA = 7;
        static final int DEC_FINAL = 8;
        static final int DEC_INIT = 5;
        static final int ENC_AAD = 2;
        static final int ENC_DATA = 3;
        static final int ENC_FINAL = 4;
        static final int ENC_INIT = 1;
        static final int UNINITIALIZED = 0;

        private State() {
        }
    }

    public ChaCha20Poly1305() {
        this(new Poly1305());
    }

    public ChaCha20Poly1305(Mac mac2) {
        this.key = new byte[32];
        this.nonce = new byte[12];
        this.buf = new byte[80];
        this.mac = new byte[16];
        this.state = 0;
        if (mac2 == null) {
            throw new NullPointerException("'poly1305' cannot be null");
        } else if (16 == mac2.getMacSize()) {
            this.chacha20 = new ChaCha7539Engine();
            this.poly1305 = mac2;
        } else {
            throw new IllegalArgumentException("'poly1305' must be a 128-bit MAC");
        }
    }

    private void checkAAD() {
        int i = this.state;
        int i2 = 2;
        if (i != 1) {
            if (i == 2) {
                return;
            }
            if (i != 4) {
                i2 = 6;
                if (i != 5) {
                    if (i != 6) {
                        throw new IllegalStateException();
                    }
                    return;
                }
            } else {
                throw new IllegalStateException("ChaCha20Poly1305 cannot be reused for encryption");
            }
        }
        this.state = i2;
    }

    private void checkData() {
        int i;
        switch (this.state) {
            case 1:
            case 2:
                i = 3;
                break;
            case 3:
            case 7:
                return;
            case 4:
                throw new IllegalStateException("ChaCha20Poly1305 cannot be reused for encryption");
            case 5:
            case 6:
                i = 7;
                break;
            default:
                throw new IllegalStateException();
        }
        finishAAD(i);
    }

    private void finishAAD(int i) {
        padMAC(this.aadCount);
        this.state = i;
    }

    private void finishData(int i) {
        padMAC(this.dataCount);
        byte[] bArr = new byte[16];
        Pack.longToLittleEndian(this.aadCount, bArr, 0);
        Pack.longToLittleEndian(this.dataCount, bArr, 8);
        this.poly1305.update(bArr, 0, 16);
        this.poly1305.doFinal(this.mac, 0);
        this.state = i;
    }

    private long incrementCount(long j, int i, long j2) {
        long j3 = (long) i;
        if (j - Long.MIN_VALUE <= (j2 - j3) - Long.MIN_VALUE) {
            return j + j3;
        }
        throw new IllegalStateException("Limit exceeded");
    }

    private void initMAC() {
        byte[] bArr = new byte[64];
        try {
            this.chacha20.processBytes(bArr, 0, 64, bArr, 0);
            this.poly1305.init(new KeyParameter(bArr, 0, 32));
        } finally {
            Arrays.clear(bArr);
        }
    }

    private void padMAC(long j) {
        int i = ((int) j) & 15;
        if (i != 0) {
            this.poly1305.update(ZEROES, 0, 16 - i);
        }
    }

    private void processData(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        if (i3 <= bArr2.length - i2) {
            this.chacha20.processBytes(bArr, i, i2, bArr2, i3);
            this.dataCount = incrementCount(this.dataCount, i2, DATA_LIMIT);
            return;
        }
        throw new OutputLengthException("Output buffer too short");
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void reset(boolean z, boolean z2) {
        Arrays.clear(this.buf);
        if (z) {
            Arrays.clear(this.mac);
        }
        this.aadCount = 0;
        this.dataCount = 0;
        this.bufPos = 0;
        switch (this.state) {
            case 1:
            case 5:
                break;
            case 2:
            case 3:
            case 4:
                this.state = 4;
                return;
            case 6:
            case 7:
            case 8:
                this.state = 5;
                break;
            default:
                throw new IllegalStateException();
        }
        if (z2) {
            this.chacha20.reset();
        }
        initMAC();
        byte[] bArr = this.initialAAD;
        if (bArr != null) {
            processAADBytes(bArr, 0, bArr.length);
        }
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int doFinal(byte[] bArr, int i) throws IllegalStateException, InvalidCipherTextException {
        int i2;
        if (bArr == null) {
            throw new NullPointerException("'out' cannot be null");
        } else if (i >= 0) {
            checkData();
            Arrays.clear(this.mac);
            int i3 = this.state;
            if (i3 == 3) {
                int i4 = this.bufPos;
                i2 = i4 + 16;
                if (i <= bArr.length - i2) {
                    if (i4 > 0) {
                        processData(this.buf, 0, i4, bArr, i);
                        this.poly1305.update(bArr, i, this.bufPos);
                    }
                    finishData(4);
                    System.arraycopy(this.mac, 0, bArr, i + this.bufPos, 16);
                } else {
                    throw new OutputLengthException("Output buffer too short");
                }
            } else if (i3 == 7) {
                int i5 = this.bufPos;
                if (i5 >= 16) {
                    i2 = i5 - 16;
                    if (i <= bArr.length - i2) {
                        if (i2 > 0) {
                            this.poly1305.update(this.buf, 0, i2);
                            processData(this.buf, 0, i2, bArr, i);
                        }
                        finishData(8);
                        if (!Arrays.constantTimeAreEqual(16, this.mac, 0, this.buf, i2)) {
                            throw new InvalidCipherTextException("mac check in ChaCha20Poly1305 failed");
                        }
                    } else {
                        throw new OutputLengthException("Output buffer too short");
                    }
                } else {
                    throw new InvalidCipherTextException("data too short");
                }
            } else {
                throw new IllegalStateException();
            }
            reset(false, true);
            return i2;
        } else {
            throw new IllegalArgumentException("'outOff' cannot be negative");
        }
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public String getAlgorithmName() {
        return "ChaCha20Poly1305";
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public byte[] getMac() {
        return Arrays.clone(this.mac);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int getOutputSize(int i) {
        int max = Math.max(0, i) + this.bufPos;
        int i2 = this.state;
        if (i2 == 1 || i2 == 2 || i2 == 3) {
            return max + 16;
        }
        if (i2 == 5 || i2 == 6 || i2 == 7) {
            return Math.max(0, max - 16);
        }
        throw new IllegalStateException();
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int getUpdateOutputSize(int i) {
        int max = Math.max(0, i) + this.bufPos;
        int i2 = this.state;
        if (!(i2 == 1 || i2 == 2 || i2 == 3)) {
            if (i2 == 5 || i2 == 6 || i2 == 7) {
                max = Math.max(0, max - 16);
            } else {
                throw new IllegalStateException();
            }
        }
        return max - (max % 64);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        byte[] bArr;
        ParametersWithIV parametersWithIV;
        byte[] bArr2;
        KeyParameter keyParameter;
        if (cipherParameters instanceof AEADParameters) {
            AEADParameters aEADParameters = (AEADParameters) cipherParameters;
            int macSize = aEADParameters.getMacSize();
            if (128 == macSize) {
                keyParameter = aEADParameters.getKey();
                bArr2 = aEADParameters.getNonce();
                parametersWithIV = new ParametersWithIV(keyParameter, bArr2);
                bArr = aEADParameters.getAssociatedText();
            } else {
                throw new IllegalArgumentException("Invalid value for MAC size: " + macSize);
            }
        } else if (cipherParameters instanceof ParametersWithIV) {
            parametersWithIV = (ParametersWithIV) cipherParameters;
            keyParameter = (KeyParameter) parametersWithIV.getParameters();
            bArr2 = parametersWithIV.getIV();
            bArr = null;
        } else {
            throw new IllegalArgumentException("invalid parameters passed to ChaCha20Poly1305");
        }
        this.initialAAD = bArr;
        if (keyParameter == null) {
            if (this.state == 0) {
                throw new IllegalArgumentException("Key must be specified in initial init");
            }
        } else if (32 != keyParameter.getKey().length) {
            throw new IllegalArgumentException("Key must be 256 bits");
        }
        if (bArr2 == null || 12 != bArr2.length) {
            throw new IllegalArgumentException("Nonce must be 96 bits");
        } else if (this.state == 0 || !z || !Arrays.areEqual(this.nonce, bArr2) || (keyParameter != null && !Arrays.areEqual(this.key, keyParameter.getKey()))) {
            if (keyParameter != null) {
                System.arraycopy(keyParameter.getKey(), 0, this.key, 0, 32);
            }
            System.arraycopy(bArr2, 0, this.nonce, 0, 12);
            this.chacha20.init(true, parametersWithIV);
            this.state = z ? 1 : 5;
            reset(true, false);
        } else {
            throw new IllegalArgumentException("cannot reuse nonce for ChaCha20Poly1305 encryption");
        }
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void processAADByte(byte b) {
        checkAAD();
        this.aadCount = incrementCount(this.aadCount, 1, AAD_LIMIT);
        this.poly1305.update(b);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void processAADBytes(byte[] bArr, int i, int i2) {
        if (bArr == null) {
            throw new NullPointerException("'in' cannot be null");
        } else if (i < 0) {
            throw new IllegalArgumentException("'inOff' cannot be negative");
        } else if (i2 < 0) {
            throw new IllegalArgumentException("'len' cannot be negative");
        } else if (i <= bArr.length - i2) {
            checkAAD();
            if (i2 > 0) {
                this.aadCount = incrementCount(this.aadCount, i2, AAD_LIMIT);
                this.poly1305.update(bArr, i, i2);
            }
        } else {
            throw new DataLengthException("Input buffer too short");
        }
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int processByte(byte b, byte[] bArr, int i) throws DataLengthException {
        checkData();
        int i2 = this.state;
        if (i2 == 3) {
            byte[] bArr2 = this.buf;
            int i3 = this.bufPos;
            bArr2[i3] = b;
            int i4 = i3 + 1;
            this.bufPos = i4;
            if (i4 != 64) {
                return 0;
            }
            processData(bArr2, 0, 64, bArr, i);
            this.poly1305.update(bArr, i, 64);
            this.bufPos = 0;
            return 64;
        } else if (i2 == 7) {
            byte[] bArr3 = this.buf;
            int i5 = this.bufPos;
            bArr3[i5] = b;
            int i6 = i5 + 1;
            this.bufPos = i6;
            if (i6 != bArr3.length) {
                return 0;
            }
            this.poly1305.update(bArr3, 0, 64);
            processData(this.buf, 0, 64, bArr, i);
            byte[] bArr4 = this.buf;
            System.arraycopy(bArr4, 64, bArr4, 0, 16);
            this.bufPos = 16;
            return 64;
        } else {
            throw new IllegalStateException();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0095 A[LOOP:2: B:27:0x0093->B:28:0x0095, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00b2  */
    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException {
        int i4;
        int i5 = i;
        int i6 = i2;
        if (bArr == null) {
            throw new NullPointerException("'in' cannot be null");
        } else if (i5 < 0) {
            throw new IllegalArgumentException("'inOff' cannot be negative");
        } else if (i6 < 0) {
            throw new IllegalArgumentException("'len' cannot be negative");
        } else if (i5 > bArr.length - i6) {
            throw new DataLengthException("Input buffer too short");
        } else if (i3 >= 0) {
            checkData();
            int i7 = this.state;
            if (i7 == 3) {
                if (this.bufPos != 0) {
                    while (true) {
                        if (i6 <= 0) {
                            break;
                        }
                        i6--;
                        byte[] bArr3 = this.buf;
                        int i8 = this.bufPos;
                        int i9 = i5 + 1;
                        bArr3[i8] = bArr[i5];
                        int i10 = i8 + 1;
                        this.bufPos = i10;
                        if (i10 == 64) {
                            processData(bArr3, 0, 64, bArr2, i3);
                            this.poly1305.update(bArr2, i3, 64);
                            this.bufPos = 0;
                            i4 = 64;
                            i5 = i9;
                            break;
                        }
                        i5 = i9;
                    }
                    while (i6 >= 64) {
                        int i11 = i3 + i4;
                        processData(bArr, i5, 64, bArr2, i11);
                        this.poly1305.update(bArr2, i11, 64);
                        i5 += 64;
                        i6 -= 64;
                        i4 += 64;
                    }
                    if (i6 > 0) {
                        System.arraycopy(bArr, i5, this.buf, 0, i6);
                        this.bufPos = i6;
                    }
                }
                i4 = 0;
                while (i6 >= 64) {
                }
                if (i6 > 0) {
                }
            } else if (i7 == 7) {
                i4 = 0;
                for (int i12 = 0; i12 < i6; i12++) {
                    byte[] bArr4 = this.buf;
                    int i13 = this.bufPos;
                    bArr4[i13] = bArr[i5 + i12];
                    int i14 = i13 + 1;
                    this.bufPos = i14;
                    if (i14 == bArr4.length) {
                        this.poly1305.update(bArr4, 0, 64);
                        processData(this.buf, 0, 64, bArr2, i3 + i4);
                        byte[] bArr5 = this.buf;
                        System.arraycopy(bArr5, 64, bArr5, 0, 16);
                        this.bufPos = 16;
                        i4 += 64;
                    }
                }
            } else {
                throw new IllegalStateException();
            }
            return i4;
        } else {
            throw new IllegalArgumentException("'outOff' cannot be negative");
        }
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void reset() {
        reset(true, true);
    }
}
