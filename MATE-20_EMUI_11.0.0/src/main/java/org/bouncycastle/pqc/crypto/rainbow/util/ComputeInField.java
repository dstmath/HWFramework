package org.bouncycastle.pqc.crypto.rainbow.util;

import java.lang.reflect.Array;

public class ComputeInField {
    private short[][] A;
    short[] x;

    private void computeZerosAbove() throws RuntimeException {
        for (int length = this.A.length - 1; length > 0; length--) {
            for (int i = length - 1; i >= 0; i--) {
                short[][] sArr = this.A;
                short s = sArr[i][length];
                short invElem = GF2Field.invElem(sArr[length][length]);
                if (invElem != 0) {
                    int i2 = length;
                    while (true) {
                        short[][] sArr2 = this.A;
                        if (i2 >= sArr2.length * 2) {
                            break;
                        }
                        short multElem = GF2Field.multElem(s, GF2Field.multElem(sArr2[length][i2], invElem));
                        short[][] sArr3 = this.A;
                        sArr3[i][i2] = GF2Field.addElem(sArr3[i][i2], multElem);
                        i2++;
                    }
                } else {
                    throw new RuntimeException("The matrix is not invertible");
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0058, code lost:
        r0 = r1;
     */
    private void computeZerosUnder(boolean z) throws RuntimeException {
        int length = z ? this.A.length * 2 : this.A.length + 1;
        int i = 0;
        while (i < this.A.length - 1) {
            int i2 = i + 1;
            int i3 = i2;
            while (true) {
                short[][] sArr = this.A;
                if (i3 >= sArr.length) {
                    break;
                }
                short s = sArr[i3][i];
                short invElem = GF2Field.invElem(sArr[i][i]);
                if (invElem != 0) {
                    for (int i4 = i; i4 < length; i4++) {
                        short multElem = GF2Field.multElem(s, GF2Field.multElem(this.A[i][i4], invElem));
                        short[][] sArr2 = this.A;
                        sArr2[i3][i4] = GF2Field.addElem(sArr2[i3][i4], multElem);
                    }
                    i3++;
                } else {
                    throw new IllegalStateException("Matrix not invertible! We have to choose another one!");
                }
            }
        }
    }

    private void substitute() throws IllegalStateException {
        short[][] sArr = this.A;
        short invElem = GF2Field.invElem(sArr[sArr.length - 1][sArr.length - 1]);
        if (invElem != 0) {
            short[] sArr2 = this.x;
            short[][] sArr3 = this.A;
            sArr2[sArr3.length - 1] = GF2Field.multElem(sArr3[sArr3.length - 1][sArr3.length], invElem);
            for (int length = this.A.length - 2; length >= 0; length--) {
                short[][] sArr4 = this.A;
                short s = sArr4[length][sArr4.length];
                for (int length2 = sArr4.length - 1; length2 > length; length2--) {
                    s = GF2Field.addElem(s, GF2Field.multElem(this.A[length][length2], this.x[length2]));
                }
                short invElem2 = GF2Field.invElem(this.A[length][length]);
                if (invElem2 != 0) {
                    this.x[length] = GF2Field.multElem(s, invElem2);
                } else {
                    throw new IllegalStateException("Not solvable equation system");
                }
            }
            return;
        }
        throw new IllegalStateException("The equation system is not solvable");
    }

    public short[][] addSquareMatrix(short[][] sArr, short[][] sArr2) {
        if (sArr.length == sArr2.length && sArr[0].length == sArr2[0].length) {
            short[][] sArr3 = (short[][]) Array.newInstance(short.class, sArr.length, sArr.length);
            for (int i = 0; i < sArr.length; i++) {
                for (int i2 = 0; i2 < sArr2.length; i2++) {
                    sArr3[i][i2] = GF2Field.addElem(sArr[i][i2], sArr2[i][i2]);
                }
            }
            return sArr3;
        }
        throw new RuntimeException("Addition is not possible!");
    }

    public short[] addVect(short[] sArr, short[] sArr2) {
        if (sArr.length == sArr2.length) {
            short[] sArr3 = new short[sArr.length];
            for (int i = 0; i < sArr3.length; i++) {
                sArr3[i] = GF2Field.addElem(sArr[i], sArr2[i]);
            }
            return sArr3;
        }
        throw new RuntimeException("Multiplication is not possible!");
    }

    public short[][] inverse(short[][] sArr) {
        try {
            this.A = (short[][]) Array.newInstance(short.class, sArr.length, sArr.length * 2);
            if (sArr.length == sArr[0].length) {
                for (int i = 0; i < sArr.length; i++) {
                    for (int i2 = 0; i2 < sArr.length; i2++) {
                        this.A[i][i2] = sArr[i][i2];
                    }
                    for (int length = sArr.length; length < sArr.length * 2; length++) {
                        this.A[i][length] = 0;
                    }
                    this.A[i][this.A.length + i] = 1;
                }
                computeZerosUnder(true);
                for (int i3 = 0; i3 < this.A.length; i3++) {
                    short invElem = GF2Field.invElem(this.A[i3][i3]);
                    for (int i4 = i3; i4 < this.A.length * 2; i4++) {
                        this.A[i3][i4] = GF2Field.multElem(this.A[i3][i4], invElem);
                    }
                }
                computeZerosAbove();
                short[][] sArr2 = (short[][]) Array.newInstance(short.class, this.A.length, this.A.length);
                for (int i5 = 0; i5 < this.A.length; i5++) {
                    for (int length2 = this.A.length; length2 < this.A.length * 2; length2++) {
                        sArr2[i5][length2 - this.A.length] = this.A[i5][length2];
                    }
                }
                return sArr2;
            }
            throw new RuntimeException("The matrix is not invertible. Please choose another one!");
        } catch (RuntimeException e) {
            return null;
        }
    }

    public short[][] multMatrix(short s, short[][] sArr) {
        short[][] sArr2 = (short[][]) Array.newInstance(short.class, sArr.length, sArr[0].length);
        for (int i = 0; i < sArr.length; i++) {
            for (int i2 = 0; i2 < sArr[0].length; i2++) {
                sArr2[i][i2] = GF2Field.multElem(s, sArr[i][i2]);
            }
        }
        return sArr2;
    }

    public short[] multVect(short s, short[] sArr) {
        short[] sArr2 = new short[sArr.length];
        for (int i = 0; i < sArr2.length; i++) {
            sArr2[i] = GF2Field.multElem(s, sArr[i]);
        }
        return sArr2;
    }

    public short[][] multVects(short[] sArr, short[] sArr2) {
        if (sArr.length == sArr2.length) {
            short[][] sArr3 = (short[][]) Array.newInstance(short.class, sArr.length, sArr2.length);
            for (int i = 0; i < sArr.length; i++) {
                for (int i2 = 0; i2 < sArr2.length; i2++) {
                    sArr3[i][i2] = GF2Field.multElem(sArr[i], sArr2[i2]);
                }
            }
            return sArr3;
        }
        throw new RuntimeException("Multiplication is not possible!");
    }

    public short[] multiplyMatrix(short[][] sArr, short[] sArr2) throws RuntimeException {
        if (sArr[0].length == sArr2.length) {
            short[] sArr3 = new short[sArr.length];
            for (int i = 0; i < sArr.length; i++) {
                for (int i2 = 0; i2 < sArr2.length; i2++) {
                    sArr3[i] = GF2Field.addElem(sArr3[i], GF2Field.multElem(sArr[i][i2], sArr2[i2]));
                }
            }
            return sArr3;
        }
        throw new RuntimeException("Multiplication is not possible!");
    }

    public short[][] multiplyMatrix(short[][] sArr, short[][] sArr2) throws RuntimeException {
        if (sArr[0].length == sArr2.length) {
            this.A = (short[][]) Array.newInstance(short.class, sArr.length, sArr2[0].length);
            for (int i = 0; i < sArr.length; i++) {
                for (int i2 = 0; i2 < sArr2.length; i2++) {
                    for (int i3 = 0; i3 < sArr2[0].length; i3++) {
                        short multElem = GF2Field.multElem(sArr[i][i2], sArr2[i2][i3]);
                        short[][] sArr3 = this.A;
                        sArr3[i][i3] = GF2Field.addElem(sArr3[i][i3], multElem);
                    }
                }
            }
            return this.A;
        }
        throw new RuntimeException("Multiplication is not possible!");
    }

    public short[] solveEquation(short[][] sArr, short[] sArr2) {
        if (sArr.length != sArr2.length) {
            return null;
        }
        try {
            this.A = (short[][]) Array.newInstance(short.class, sArr.length, sArr.length + 1);
            this.x = new short[sArr.length];
            for (int i = 0; i < sArr.length; i++) {
                for (int i2 = 0; i2 < sArr[0].length; i2++) {
                    this.A[i][i2] = sArr[i][i2];
                }
            }
            for (int i3 = 0; i3 < sArr2.length; i3++) {
                this.A[i3][sArr2.length] = GF2Field.addElem(sArr2[i3], this.A[i3][sArr2.length]);
            }
            computeZerosUnder(false);
            substitute();
            return this.x;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
