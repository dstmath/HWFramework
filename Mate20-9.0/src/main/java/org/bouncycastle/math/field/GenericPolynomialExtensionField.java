package org.bouncycastle.math.field;

import java.math.BigInteger;
import org.bouncycastle.util.Integers;

class GenericPolynomialExtensionField implements PolynomialExtensionField {
    protected final Polynomial minimalPolynomial;
    protected final FiniteField subfield;

    GenericPolynomialExtensionField(FiniteField finiteField, Polynomial polynomial) {
        this.subfield = finiteField;
        this.minimalPolynomial = polynomial;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GenericPolynomialExtensionField)) {
            return false;
        }
        GenericPolynomialExtensionField genericPolynomialExtensionField = (GenericPolynomialExtensionField) obj;
        return this.subfield.equals(genericPolynomialExtensionField.subfield) && this.minimalPolynomial.equals(genericPolynomialExtensionField.minimalPolynomial);
    }

    public BigInteger getCharacteristic() {
        return this.subfield.getCharacteristic();
    }

    public int getDegree() {
        return this.minimalPolynomial.getDegree();
    }

    public int getDimension() {
        return this.subfield.getDimension() * this.minimalPolynomial.getDegree();
    }

    public Polynomial getMinimalPolynomial() {
        return this.minimalPolynomial;
    }

    public FiniteField getSubfield() {
        return this.subfield;
    }

    public int hashCode() {
        return this.subfield.hashCode() ^ Integers.rotateLeft(this.minimalPolynomial.hashCode(), 16);
    }
}
