package com.android.org.bouncycastle.math.field;

import com.android.org.bouncycastle.util.Integers;
import java.math.BigInteger;

class GenericPolynomialExtensionField implements PolynomialExtensionField {
    protected final Polynomial minimalPolynomial;
    protected final FiniteField subfield;

    GenericPolynomialExtensionField(FiniteField subfield, Polynomial polynomial) {
        this.subfield = subfield;
        this.minimalPolynomial = polynomial;
    }

    public BigInteger getCharacteristic() {
        return this.subfield.getCharacteristic();
    }

    public int getDimension() {
        return this.subfield.getDimension() * this.minimalPolynomial.getDegree();
    }

    public FiniteField getSubfield() {
        return this.subfield;
    }

    public int getDegree() {
        return this.minimalPolynomial.getDegree();
    }

    public Polynomial getMinimalPolynomial() {
        return this.minimalPolynomial;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GenericPolynomialExtensionField)) {
            return false;
        }
        GenericPolynomialExtensionField other = (GenericPolynomialExtensionField) obj;
        if (this.subfield.equals(other.subfield)) {
            z = this.minimalPolynomial.equals(other.minimalPolynomial);
        }
        return z;
    }

    public int hashCode() {
        return this.subfield.hashCode() ^ Integers.rotateLeft(this.minimalPolynomial.hashCode(), 16);
    }
}
