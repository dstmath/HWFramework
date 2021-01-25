package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KeyParameter;

public abstract class SerpentEngineBase implements BlockCipher {
    protected static final int BLOCK_SIZE = 16;
    static final int PHI = -1640531527;
    static final int ROUNDS = 32;
    protected int X0;
    protected int X1;
    protected int X2;
    protected int X3;
    protected boolean encrypting;
    protected int[] wKey;

    SerpentEngineBase() {
    }

    protected static int rotateLeft(int i, int i2) {
        return (i >>> (-i2)) | (i << i2);
    }

    protected static int rotateRight(int i, int i2) {
        return (i << (-i2)) | (i >>> i2);
    }

    /* access modifiers changed from: protected */
    public final void LT() {
        int rotateLeft = rotateLeft(this.X0, 13);
        int rotateLeft2 = rotateLeft(this.X2, 3);
        this.X1 = rotateLeft((this.X1 ^ rotateLeft) ^ rotateLeft2, 1);
        this.X3 = rotateLeft((this.X3 ^ rotateLeft2) ^ (rotateLeft << 3), 7);
        this.X0 = rotateLeft((rotateLeft ^ this.X1) ^ this.X3, 5);
        this.X2 = rotateLeft((this.X3 ^ rotateLeft2) ^ (this.X1 << 7), 22);
    }

    /* access modifiers changed from: protected */
    public abstract void decryptBlock(byte[] bArr, int i, byte[] bArr2, int i2);

    /* access modifiers changed from: protected */
    public abstract void encryptBlock(byte[] bArr, int i, byte[] bArr2, int i2);

    @Override // org.bouncycastle.crypto.BlockCipher
    public String getAlgorithmName() {
        return "Serpent";
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int getBlockSize() {
        return 16;
    }

    /* access modifiers changed from: protected */
    public final void ib0(int i, int i2, int i3, int i4) {
        int i5 = ~i;
        int i6 = i2 ^ i;
        int i7 = (i5 | i6) ^ i4;
        int i8 = i3 ^ i7;
        this.X2 = i6 ^ i8;
        int i9 = (i6 & i4) ^ i5;
        this.X1 = (this.X2 & i9) ^ i7;
        this.X3 = (i & i7) ^ (this.X1 | i8);
        this.X0 = this.X3 ^ (i9 ^ i8);
    }

    /* access modifiers changed from: protected */
    public final void ib1(int i, int i2, int i3, int i4) {
        int i5 = i4 ^ i2;
        int i6 = i ^ (i2 & i5);
        int i7 = i5 ^ i6;
        this.X3 = i3 ^ i7;
        int i8 = i2 ^ (i5 & i6);
        int i9 = this.X3;
        this.X1 = i6 ^ (i9 | i8);
        int i10 = ~this.X1;
        int i11 = i8 ^ i9;
        this.X0 = i10 ^ i11;
        this.X2 = (i10 | i11) ^ i7;
    }

    /* access modifiers changed from: protected */
    public final void ib2(int i, int i2, int i3, int i4) {
        int i5 = i2 ^ i4;
        int i6 = ~i5;
        int i7 = i ^ i3;
        int i8 = i3 ^ i5;
        this.X0 = (i2 & i8) ^ i7;
        this.X3 = (((i | i6) ^ i4) | i7) ^ i5;
        int i9 = ~i8;
        int i10 = this.X0 | this.X3;
        this.X1 = i9 ^ i10;
        this.X2 = (i9 & i4) ^ (i10 ^ i7);
    }

    /* access modifiers changed from: protected */
    public final void ib3(int i, int i2, int i3, int i4) {
        int i5 = i | i2;
        int i6 = i2 ^ i3;
        int i7 = i ^ (i2 & i6);
        int i8 = i3 ^ i7;
        int i9 = i4 | i7;
        this.X0 = i6 ^ i9;
        int i10 = (i9 | i6) ^ i4;
        this.X2 = i8 ^ i10;
        int i11 = i5 ^ i10;
        int i12 = this.X0;
        this.X3 = i7 ^ (i12 & i11);
        this.X1 = this.X3 ^ (i11 ^ i12);
    }

    /* access modifiers changed from: protected */
    public final void ib4(int i, int i2, int i3, int i4) {
        int i5 = i2 ^ ((i3 | i4) & i);
        int i6 = i3 ^ (i & i5);
        this.X1 = i4 ^ i6;
        int i7 = ~i;
        int i8 = this.X1;
        this.X3 = (i6 & i8) ^ i5;
        int i9 = (i8 | i7) ^ i4;
        this.X0 = this.X3 ^ i9;
        this.X2 = (i7 ^ i8) ^ (i5 & i9);
    }

    /* access modifiers changed from: protected */
    public final void ib5(int i, int i2, int i3, int i4) {
        int i5 = ~i3;
        int i6 = (i2 & i5) ^ i4;
        int i7 = i & i6;
        this.X3 = (i2 ^ i5) ^ i7;
        int i8 = this.X3 | i2;
        this.X1 = i6 ^ (i & i8);
        int i9 = i4 | i;
        this.X0 = (i5 ^ i8) ^ i9;
        this.X2 = ((i ^ i3) | i7) ^ (i2 & i9);
    }

    /* access modifiers changed from: protected */
    public final void ib6(int i, int i2, int i3, int i4) {
        int i5 = ~i;
        int i6 = i ^ i2;
        int i7 = i3 ^ i6;
        int i8 = (i3 | i5) ^ i4;
        this.X1 = i7 ^ i8;
        int i9 = i6 ^ (i7 & i8);
        this.X3 = i8 ^ (i2 | i9);
        int i10 = i2 | this.X3;
        this.X0 = i9 ^ i10;
        this.X2 = (i4 & i5) ^ (i10 ^ i7);
    }

    /* access modifiers changed from: protected */
    public final void ib7(int i, int i2, int i3, int i4) {
        int i5 = (i & i2) | i3;
        int i6 = (i | i2) & i4;
        this.X3 = i5 ^ i6;
        int i7 = ~i4;
        int i8 = i2 ^ i6;
        int i9 = this.X3;
        this.X1 = ((i7 ^ i9) | i8) ^ i;
        int i10 = i8 ^ i3;
        int i11 = this.X1;
        this.X0 = i10 ^ (i4 | i11);
        this.X2 = ((i & i9) ^ this.X0) ^ (i5 ^ i11);
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void init(boolean z, CipherParameters cipherParameters) {
        if (cipherParameters instanceof KeyParameter) {
            this.encrypting = z;
            this.wKey = makeWorkingKey(((KeyParameter) cipherParameters).getKey());
            return;
        }
        throw new IllegalArgumentException("invalid parameter passed to " + getAlgorithmName() + " init - " + cipherParameters.getClass().getName());
    }

    /* access modifiers changed from: protected */
    public final void inverseLT() {
        int rotateRight = (rotateRight(this.X2, 22) ^ this.X3) ^ (this.X1 << 7);
        int rotateRight2 = rotateRight(this.X0, 5) ^ this.X1;
        int i = this.X3;
        int i2 = rotateRight2 ^ i;
        int rotateRight3 = rotateRight(i, 7);
        int rotateRight4 = rotateRight(this.X1, 1);
        this.X3 = (rotateRight3 ^ rotateRight) ^ (i2 << 3);
        this.X1 = (rotateRight4 ^ i2) ^ rotateRight;
        this.X2 = rotateRight(rotateRight, 3);
        this.X0 = rotateRight(i2, 13);
    }

    /* access modifiers changed from: protected */
    public abstract int[] makeWorkingKey(byte[] bArr);

    @Override // org.bouncycastle.crypto.BlockCipher
    public final int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) {
        if (this.wKey == null) {
            throw new IllegalStateException(getAlgorithmName() + " not initialised");
        } else if (i + 16 > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (i2 + 16 > bArr2.length) {
            throw new OutputLengthException("output buffer too short");
        } else if (this.encrypting) {
            encryptBlock(bArr, i, bArr2, i2);
            return 16;
        } else {
            decryptBlock(bArr, i, bArr2, i2);
            return 16;
        }
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void reset() {
    }

    /* access modifiers changed from: protected */
    public final void sb0(int i, int i2, int i3, int i4) {
        int i5 = i ^ i4;
        int i6 = i3 ^ i5;
        int i7 = i2 ^ i6;
        this.X3 = (i4 & i) ^ i7;
        int i8 = i ^ (i2 & i5);
        this.X2 = (i3 | i8) ^ i7;
        int i9 = this.X3 & (i6 ^ i8);
        this.X1 = (~i6) ^ i9;
        this.X0 = (~i8) ^ i9;
    }

    /* access modifiers changed from: protected */
    public final void sb1(int i, int i2, int i3, int i4) {
        int i5 = (~i) ^ i2;
        int i6 = (i | i5) ^ i3;
        this.X2 = i4 ^ i6;
        int i7 = i2 ^ (i4 | i5);
        int i8 = this.X2 ^ i5;
        this.X3 = (i6 & i7) ^ i8;
        int i9 = i7 ^ i6;
        this.X1 = this.X3 ^ i9;
        this.X0 = i6 ^ (i9 & i8);
    }

    /* access modifiers changed from: protected */
    public final void sb2(int i, int i2, int i3, int i4) {
        int i5 = ~i;
        int i6 = i2 ^ i4;
        this.X0 = (i3 & i5) ^ i6;
        int i7 = i3 ^ i5;
        int i8 = this.X0;
        int i9 = i2 & (i3 ^ i8);
        this.X3 = i7 ^ i9;
        this.X2 = i ^ ((i9 | i4) & (i8 | i7));
        this.X1 = (this.X3 ^ i6) ^ (this.X2 ^ (i4 | i5));
    }

    /* access modifiers changed from: protected */
    public final void sb3(int i, int i2, int i3, int i4) {
        int i5 = i ^ i2;
        int i6 = i & i3;
        int i7 = i | i4;
        int i8 = i3 ^ i4;
        int i9 = i6 | (i5 & i7);
        this.X2 = i8 ^ i9;
        int i10 = (i7 ^ i2) ^ i9;
        this.X0 = i5 ^ (i8 & i10);
        int i11 = this.X2 & this.X0;
        this.X1 = i10 ^ i11;
        this.X3 = (i2 | i4) ^ (i8 ^ i11);
    }

    /* access modifiers changed from: protected */
    public final void sb4(int i, int i2, int i3, int i4) {
        int i5 = i ^ i4;
        int i6 = i3 ^ (i4 & i5);
        int i7 = i2 | i6;
        this.X3 = i5 ^ i7;
        int i8 = ~i2;
        this.X0 = (i5 | i8) ^ i6;
        int i9 = i8 ^ i5;
        this.X2 = (i7 & i9) ^ (this.X0 & i);
        this.X1 = (i ^ i6) ^ (i9 & this.X2);
    }

    /* access modifiers changed from: protected */
    public final void sb5(int i, int i2, int i3, int i4) {
        int i5 = ~i;
        int i6 = i ^ i2;
        int i7 = i ^ i4;
        this.X0 = (i3 ^ i5) ^ (i6 | i7);
        int i8 = this.X0;
        int i9 = i4 & i8;
        this.X1 = (i6 ^ i8) ^ i9;
        int i10 = i7 ^ (i8 | i5);
        this.X2 = (i6 | i9) ^ i10;
        this.X3 = (i10 & this.X1) ^ (i2 ^ i9);
    }

    /* access modifiers changed from: protected */
    public final void sb6(int i, int i2, int i3, int i4) {
        int i5 = ~i;
        int i6 = i ^ i4;
        int i7 = i2 ^ i6;
        int i8 = i3 ^ (i5 | i6);
        this.X1 = i2 ^ i8;
        int i9 = (i6 | this.X1) ^ i4;
        this.X2 = (i8 & i9) ^ i7;
        int i10 = i9 ^ i8;
        this.X0 = this.X2 ^ i10;
        this.X3 = (i10 & i7) ^ (~i8);
    }

    /* access modifiers changed from: protected */
    public final void sb7(int i, int i2, int i3, int i4) {
        int i5 = i2 ^ i3;
        int i6 = (i3 & i5) ^ i4;
        int i7 = i ^ i6;
        this.X1 = i2 ^ ((i4 | i5) & i7);
        this.X3 = (i & i7) ^ i5;
        int i8 = i7 ^ (this.X1 | i6);
        int i9 = this.X3;
        this.X2 = i6 ^ (i9 & i8);
        this.X0 = (~i8) ^ (i9 & this.X2);
    }
}
