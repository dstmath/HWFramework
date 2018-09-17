package com.android.org.bouncycastle.math.field;

public interface ExtensionField extends FiniteField {
    int getDegree();

    FiniteField getSubfield();
}
