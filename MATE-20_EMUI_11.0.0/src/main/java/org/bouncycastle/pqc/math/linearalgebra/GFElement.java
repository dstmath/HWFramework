package org.bouncycastle.pqc.math.linearalgebra;

import java.math.BigInteger;

public interface GFElement {
    GFElement add(GFElement gFElement) throws RuntimeException;

    void addToThis(GFElement gFElement) throws RuntimeException;

    Object clone();

    boolean equals(Object obj);

    int hashCode();

    GFElement invert() throws ArithmeticException;

    boolean isOne();

    boolean isZero();

    GFElement multiply(GFElement gFElement) throws RuntimeException;

    void multiplyThisBy(GFElement gFElement) throws RuntimeException;

    GFElement subtract(GFElement gFElement) throws RuntimeException;

    void subtractFromThis(GFElement gFElement);

    byte[] toByteArray();

    BigInteger toFlexiBigInt();

    String toString();

    String toString(int i);
}
