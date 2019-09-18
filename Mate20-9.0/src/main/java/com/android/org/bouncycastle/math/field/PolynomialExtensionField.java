package com.android.org.bouncycastle.math.field;

public interface PolynomialExtensionField extends ExtensionField {
    Polynomial getMinimalPolynomial();
}
