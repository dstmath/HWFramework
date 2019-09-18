package org.bouncycastle.crypto.engines;

import org.bouncycastle.asn1.cmc.BodyPartID;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.MaxBytesExceededException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.SkippingStreamCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Pack;
import org.bouncycastle.util.Strings;

public class Salsa20Engine implements SkippingStreamCipher {
    public static final int DEFAULT_ROUNDS = 20;
    private static final int STATE_SIZE = 16;
    private static final int[] TAU_SIGMA = Pack.littleEndianToInt(Strings.toByteArray("expand 16-byte kexpand 32-byte k"), 0, 8);
    protected static final byte[] sigma = Strings.toByteArray("expand 32-byte k");
    protected static final byte[] tau = Strings.toByteArray("expand 16-byte k");
    private int cW0;
    private int cW1;
    private int cW2;
    protected int[] engineState;
    private int index;
    private boolean initialised;
    private byte[] keyStream;
    protected int rounds;
    protected int[] x;

    public Salsa20Engine() {
        this(20);
    }

    public Salsa20Engine(int i) {
        this.index = 0;
        this.engineState = new int[16];
        this.x = new int[16];
        this.keyStream = new byte[64];
        this.initialised = false;
        if (i <= 0 || (i & 1) != 0) {
            throw new IllegalArgumentException("'rounds' must be a positive, even number");
        }
        this.rounds = i;
    }

    private boolean limitExceeded() {
        int i = this.cW0 + 1;
        this.cW0 = i;
        if (i == 0) {
            int i2 = this.cW1 + 1;
            this.cW1 = i2;
            if (i2 == 0) {
                int i3 = this.cW2 + 1;
                this.cW2 = i3;
                return (i3 & 32) != 0;
            }
        }
        return false;
    }

    private boolean limitExceeded(int i) {
        this.cW0 += i;
        if (this.cW0 < i && this.cW0 >= 0) {
            int i2 = this.cW1 + 1;
            this.cW1 = i2;
            if (i2 == 0) {
                int i3 = this.cW2 + 1;
                this.cW2 = i3;
                return (i3 & 32) != 0;
            }
        }
        return false;
    }

    private void resetLimitCounter() {
        this.cW0 = 0;
        this.cW1 = 0;
        this.cW2 = 0;
    }

    protected static int rotl(int i, int i2) {
        return (i >>> (-i2)) | (i << i2);
    }

    public static void salsaCore(int i, int[] iArr, int[] iArr2) {
        int[] iArr3 = iArr;
        int[] iArr4 = iArr2;
        if (iArr3.length != 16) {
            throw new IllegalArgumentException();
        } else if (iArr4.length != 16) {
            throw new IllegalArgumentException();
        } else if (i % 2 == 0) {
            boolean z = false;
            int i2 = iArr3[0];
            int i3 = iArr3[1];
            int i4 = iArr3[2];
            int i5 = iArr3[3];
            int i6 = iArr3[4];
            int i7 = iArr3[5];
            int i8 = iArr3[6];
            int i9 = 7;
            int i10 = iArr3[7];
            int i11 = iArr3[8];
            int i12 = 9;
            int i13 = iArr3[9];
            int i14 = iArr3[10];
            int i15 = iArr3[11];
            int i16 = iArr3[12];
            int i17 = 13;
            int i18 = iArr3[13];
            int i19 = iArr3[14];
            int i20 = iArr3[15];
            int i21 = i;
            while (i21 > 0) {
                int rotl = rotl(i2 + i16, i9) ^ i6;
                int rotl2 = i11 ^ rotl(rotl + i2, i12);
                int rotl3 = i16 ^ rotl(rotl2 + rotl, i17);
                int rotl4 = i2 ^ rotl(rotl3 + rotl2, 18);
                int rotl5 = i13 ^ rotl(i7 + i3, i9);
                int rotl6 = i18 ^ rotl(rotl5 + i7, i12);
                int rotl7 = i3 ^ rotl(rotl6 + rotl5, i17);
                int rotl8 = rotl(rotl7 + rotl6, 18) ^ i7;
                int rotl9 = i19 ^ rotl(i14 + i8, 7);
                int rotl10 = i4 ^ rotl(rotl9 + i14, 9);
                int rotl11 = i8 ^ rotl(rotl10 + rotl9, 13);
                int rotl12 = i14 ^ rotl(rotl11 + rotl10, 18);
                int rotl13 = i5 ^ rotl(i20 + i15, 7);
                int rotl14 = i10 ^ rotl(rotl13 + i20, 9);
                int i22 = i21;
                int rotl15 = i15 ^ rotl(rotl14 + rotl13, 13);
                int rotl16 = i20 ^ rotl(rotl15 + rotl14, 18);
                int i23 = rotl3;
                i3 = rotl7 ^ rotl(rotl4 + rotl13, 7);
                i4 = rotl10 ^ rotl(i3 + rotl4, 9);
                int rotl17 = rotl13 ^ rotl(i4 + i3, 13);
                i2 = rotl4 ^ rotl(rotl17 + i4, 18);
                i8 = rotl11 ^ rotl(rotl8 + rotl, 7);
                i10 = rotl14 ^ rotl(i8 + rotl8, 9);
                int rotl18 = rotl(i10 + i8, 13) ^ rotl;
                i15 = rotl15 ^ rotl(rotl12 + rotl5, 7);
                i11 = rotl2 ^ rotl(i15 + rotl12, 9);
                i13 = rotl5 ^ rotl(i11 + i15, 13);
                i14 = rotl12 ^ rotl(i13 + i11, 18);
                i16 = i23 ^ rotl(rotl16 + rotl9, 7);
                i18 = rotl6 ^ rotl(i16 + rotl16, 9);
                i19 = rotl9 ^ rotl(i18 + i16, 13);
                i20 = rotl16 ^ rotl(i19 + i18, 18);
                i21 = i22 - 2;
                i5 = rotl17;
                i6 = rotl18;
                i7 = rotl(rotl18 + i10, 18) ^ rotl8;
                iArr3 = iArr;
                int[] iArr5 = iArr2;
                z = false;
                i17 = 13;
                i12 = 9;
                i9 = 7;
            }
            char c = z;
            int[] iArr6 = iArr2;
            iArr6[c] = i2 + iArr3[c];
            iArr6[1] = i3 + iArr3[1];
            iArr6[2] = i4 + iArr3[2];
            iArr6[3] = i5 + iArr3[3];
            iArr6[4] = i6 + iArr3[4];
            iArr6[5] = i7 + iArr3[5];
            iArr6[6] = i8 + iArr3[6];
            iArr6[7] = i10 + iArr3[7];
            iArr6[8] = i11 + iArr3[8];
            iArr6[9] = i13 + iArr3[9];
            iArr6[10] = i14 + iArr3[10];
            iArr6[11] = i15 + iArr3[11];
            iArr6[12] = i16 + iArr3[12];
            iArr6[13] = i18 + iArr3[13];
            iArr6[14] = i19 + iArr3[14];
            iArr6[15] = i20 + iArr3[15];
        } else {
            throw new IllegalArgumentException("Number of rounds must be even");
        }
    }

    /* access modifiers changed from: protected */
    public void advanceCounter() {
        int[] iArr = this.engineState;
        int i = iArr[8] + 1;
        iArr[8] = i;
        if (i == 0) {
            int[] iArr2 = this.engineState;
            iArr2[9] = iArr2[9] + 1;
        }
    }

    /* access modifiers changed from: protected */
    public void advanceCounter(long j) {
        int i = (int) (j >>> 32);
        int i2 = (int) j;
        if (i > 0) {
            int[] iArr = this.engineState;
            iArr[9] = iArr[9] + i;
        }
        int i3 = this.engineState[8];
        int[] iArr2 = this.engineState;
        iArr2[8] = iArr2[8] + i2;
        if (i3 != 0 && this.engineState[8] < i3) {
            int[] iArr3 = this.engineState;
            iArr3[9] = iArr3[9] + 1;
        }
    }

    /* access modifiers changed from: protected */
    public void generateKeyStream(byte[] bArr) {
        salsaCore(this.rounds, this.engineState, this.x);
        Pack.intToLittleEndian(this.x, bArr, 0);
    }

    public String getAlgorithmName() {
        if (this.rounds == 20) {
            return "Salsa20";
        }
        return "Salsa20" + "/" + this.rounds;
    }

    /* access modifiers changed from: protected */
    public long getCounter() {
        return (((long) this.engineState[9]) << 32) | (((long) this.engineState[8]) & BodyPartID.bodyIdMax);
    }

    /* access modifiers changed from: protected */
    public int getNonceSize() {
        return 8;
    }

    public long getPosition() {
        return (getCounter() * 64) + ((long) this.index);
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        byte[] key;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            byte[] iv = parametersWithIV.getIV();
            if (iv == null || iv.length != getNonceSize()) {
                throw new IllegalArgumentException(getAlgorithmName() + " requires exactly " + getNonceSize() + " bytes of IV");
            }
            CipherParameters parameters = parametersWithIV.getParameters();
            if (parameters == null) {
                if (this.initialised) {
                    key = null;
                } else {
                    throw new IllegalStateException(getAlgorithmName() + " KeyParameter can not be null for first initialisation");
                }
            } else if (parameters instanceof KeyParameter) {
                key = ((KeyParameter) parameters).getKey();
            } else {
                throw new IllegalArgumentException(getAlgorithmName() + " Init parameters must contain a KeyParameter (or null for re-init)");
            }
            setKey(key, iv);
            reset();
            this.initialised = true;
            return;
        }
        throw new IllegalArgumentException(getAlgorithmName() + " Init parameters must include an IV");
    }

    /* access modifiers changed from: protected */
    public void packTauOrSigma(int i, int[] iArr, int i2) {
        int i3 = (i - 16) / 4;
        iArr[i2] = TAU_SIGMA[i3];
        iArr[i2 + 1] = TAU_SIGMA[i3 + 1];
        iArr[i2 + 2] = TAU_SIGMA[i3 + 2];
        iArr[i2 + 3] = TAU_SIGMA[i3 + 3];
    }

    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        if (!this.initialised) {
            throw new IllegalStateException(getAlgorithmName() + " not initialised");
        } else if (i + i2 > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (i3 + i2 > bArr2.length) {
            throw new OutputLengthException("output buffer too short");
        } else if (!limitExceeded(i2)) {
            for (int i4 = 0; i4 < i2; i4++) {
                bArr2[i4 + i3] = (byte) (this.keyStream[this.index] ^ bArr[i4 + i]);
                this.index = (this.index + 1) & 63;
                if (this.index == 0) {
                    advanceCounter();
                    generateKeyStream(this.keyStream);
                }
            }
            return i2;
        } else {
            throw new MaxBytesExceededException("2^70 byte limit per IV would be exceeded; Change IV");
        }
    }

    public void reset() {
        this.index = 0;
        resetLimitCounter();
        resetCounter();
        generateKeyStream(this.keyStream);
    }

    /* access modifiers changed from: protected */
    public void resetCounter() {
        int[] iArr = this.engineState;
        this.engineState[9] = 0;
        iArr[8] = 0;
    }

    /* access modifiers changed from: protected */
    public void retreatCounter() {
        int[] iArr;
        if (this.engineState[8] == 0 && this.engineState[9] == 0) {
            throw new IllegalStateException("attempt to reduce counter past zero.");
        }
        int[] iArr2 = this.engineState;
        int i = iArr2[8] - 1;
        iArr2[8] = i;
        if (i == -1) {
            iArr[9] = this.engineState[9] - 1;
        }
    }

    /* access modifiers changed from: protected */
    public void retreatCounter(long j) {
        int i = (int) (j >>> 32);
        int i2 = (int) j;
        if (i != 0) {
            if ((((long) this.engineState[9]) & BodyPartID.bodyIdMax) >= (((long) i) & BodyPartID.bodyIdMax)) {
                int[] iArr = this.engineState;
                iArr[9] = iArr[9] - i;
            } else {
                throw new IllegalStateException("attempt to reduce counter past zero.");
            }
        }
        if ((((long) this.engineState[8]) & BodyPartID.bodyIdMax) >= (((long) i2) & BodyPartID.bodyIdMax)) {
            int[] iArr2 = this.engineState;
            iArr2[8] = iArr2[8] - i2;
        } else if (this.engineState[9] != 0) {
            int[] iArr3 = this.engineState;
            iArr3[9] = iArr3[9] - 1;
            int[] iArr4 = this.engineState;
            iArr4[8] = iArr4[8] - i2;
        } else {
            throw new IllegalStateException("attempt to reduce counter past zero.");
        }
    }

    public byte returnByte(byte b) {
        if (!limitExceeded()) {
            byte b2 = (byte) (b ^ this.keyStream[this.index]);
            this.index = (this.index + 1) & 63;
            if (this.index == 0) {
                advanceCounter();
                generateKeyStream(this.keyStream);
            }
            return b2;
        }
        throw new MaxBytesExceededException("2^70 byte limit per IV; Change IV");
    }

    public long seekTo(long j) {
        reset();
        return skip(j);
    }

    /* access modifiers changed from: protected */
    public void setKey(byte[] bArr, byte[] bArr2) {
        if (bArr != null) {
            if (bArr.length == 16 || bArr.length == 32) {
                int length = (bArr.length - 16) / 4;
                this.engineState[0] = TAU_SIGMA[length];
                this.engineState[5] = TAU_SIGMA[length + 1];
                this.engineState[10] = TAU_SIGMA[length + 2];
                this.engineState[15] = TAU_SIGMA[length + 3];
                Pack.littleEndianToInt(bArr, 0, this.engineState, 1, 4);
                Pack.littleEndianToInt(bArr, bArr.length - 16, this.engineState, 11, 4);
            } else {
                throw new IllegalArgumentException(getAlgorithmName() + " requires 128 bit or 256 bit key");
            }
        }
        Pack.littleEndianToInt(bArr2, 0, this.engineState, 6, 2);
    }

    public long skip(long j) {
        long j2;
        if (j >= 0) {
            if (j >= 64) {
                long j3 = j / 64;
                advanceCounter(j3);
                j2 = j - (j3 * 64);
            } else {
                j2 = j;
            }
            int i = this.index;
            this.index = (this.index + ((int) j2)) & 63;
            if (this.index < i) {
                advanceCounter();
            }
        } else {
            long j4 = -j;
            if (j4 >= 64) {
                long j5 = j4 / 64;
                retreatCounter(j5);
                j4 -= j5 * 64;
            }
            for (long j6 = 0; j6 < j4; j6++) {
                if (this.index == 0) {
                    retreatCounter();
                }
                this.index = (this.index - 1) & 63;
            }
        }
        generateKeyStream(this.keyStream);
        return j;
    }
}
