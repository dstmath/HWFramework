package com.android.org.bouncycastle.math.field;

import java.math.BigInteger;

class PrimeField implements FiniteField {
    protected final BigInteger characteristic;

    PrimeField(BigInteger characteristic) {
        this.characteristic = characteristic;
    }

    public BigInteger getCharacteristic() {
        return this.characteristic;
    }

    public int getDimension() {
        return 1;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PrimeField)) {
            return false;
        }
        return this.characteristic.equals(((PrimeField) obj).characteristic);
    }

    public int hashCode() {
        return this.characteristic.hashCode();
    }
}
