package org.bouncycastle.pqc.math.linearalgebra;

public class GF2mVector extends Vector {
    private GF2mField field;
    private int[] vector;

    public GF2mVector(GF2mField gF2mField, byte[] bArr) {
        this.field = new GF2mField(gF2mField);
        int i = 8;
        int i2 = 1;
        while (gF2mField.getDegree() > i) {
            i2++;
            i += 8;
        }
        if (bArr.length % i2 == 0) {
            this.length = bArr.length / i2;
            this.vector = new int[this.length];
            int i3 = 0;
            int i4 = 0;
            while (i3 < this.vector.length) {
                int i5 = i4;
                int i6 = 0;
                while (i6 < i) {
                    int[] iArr = this.vector;
                    iArr[i3] = ((bArr[i5] & 255) << i6) | iArr[i3];
                    i6 += 8;
                    i5++;
                }
                if (gF2mField.isElementOfThisField(this.vector[i3])) {
                    i3++;
                    i4 = i5;
                } else {
                    throw new IllegalArgumentException("Byte array is not an encoded vector over the given finite field.");
                }
            }
            return;
        }
        throw new IllegalArgumentException("Byte array is not an encoded vector over the given finite field.");
    }

    public GF2mVector(GF2mField gF2mField, int[] iArr) {
        this.field = gF2mField;
        this.length = iArr.length;
        int length = iArr.length - 1;
        while (length >= 0) {
            if (gF2mField.isElementOfThisField(iArr[length])) {
                length--;
            } else {
                throw new ArithmeticException("Element array is not specified over the given finite field.");
            }
        }
        this.vector = IntUtils.clone(iArr);
    }

    public GF2mVector(GF2mVector gF2mVector) {
        this.field = new GF2mField(gF2mVector.field);
        this.length = gF2mVector.length;
        this.vector = IntUtils.clone(gF2mVector.vector);
    }

    public Vector add(Vector vector2) {
        throw new RuntimeException("not implemented");
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GF2mVector)) {
            return false;
        }
        GF2mVector gF2mVector = (GF2mVector) obj;
        if (!this.field.equals(gF2mVector.field)) {
            return false;
        }
        return IntUtils.equals(this.vector, gF2mVector.vector);
    }

    public byte[] getEncoded() {
        int i = 8;
        int i2 = 1;
        while (this.field.getDegree() > i) {
            i2++;
            i += 8;
        }
        byte[] bArr = new byte[(this.vector.length * i2)];
        int i3 = 0;
        int i4 = 0;
        while (i3 < this.vector.length) {
            int i5 = i4;
            int i6 = 0;
            while (i6 < i) {
                bArr[i5] = (byte) (this.vector[i3] >>> i6);
                i6 += 8;
                i5++;
            }
            i3++;
            i4 = i5;
        }
        return bArr;
    }

    public GF2mField getField() {
        return this.field;
    }

    public int[] getIntArrayForm() {
        return IntUtils.clone(this.vector);
    }

    public int hashCode() {
        return (this.field.hashCode() * 31) + this.vector.hashCode();
    }

    public boolean isZero() {
        for (int length = this.vector.length - 1; length >= 0; length--) {
            if (this.vector[length] != 0) {
                return false;
            }
        }
        return true;
    }

    public Vector multiply(Permutation permutation) {
        int[] vector2 = permutation.getVector();
        if (this.length == vector2.length) {
            int[] iArr = new int[this.length];
            for (int i = 0; i < vector2.length; i++) {
                iArr[i] = this.vector[vector2[i]];
            }
            return new GF2mVector(this.field, iArr);
        }
        throw new ArithmeticException("permutation size and vector size mismatch");
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < this.vector.length; i++) {
            for (int i2 = 0; i2 < this.field.getDegree(); i2++) {
                stringBuffer.append(((1 << (i2 & 31)) & this.vector[i]) != 0 ? '1' : '0');
            }
            stringBuffer.append(' ');
        }
        return stringBuffer.toString();
    }
}
