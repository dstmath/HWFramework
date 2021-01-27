package org.bouncycastle.crypto.params;

import java.math.BigInteger;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.util.Integers;

public class DHPublicKeyParameters extends DHKeyParameters {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private BigInteger y;

    public DHPublicKeyParameters(BigInteger bigInteger, DHParameters dHParameters) {
        super(false, dHParameters);
        this.y = validate(bigInteger, dHParameters);
    }

    private static int legendre(BigInteger bigInteger, BigInteger bigInteger2) {
        int bitLength = bigInteger2.bitLength();
        int[] fromBigInteger = Nat.fromBigInteger(bitLength, bigInteger);
        int[] fromBigInteger2 = Nat.fromBigInteger(bitLength, bigInteger2);
        int length = fromBigInteger2.length;
        int i = 0;
        while (true) {
            if (fromBigInteger[0] == 0) {
                Nat.shiftDownWord(length, fromBigInteger, 0);
            } else {
                int numberOfTrailingZeros = Integers.numberOfTrailingZeros(fromBigInteger[0]);
                if (numberOfTrailingZeros > 0) {
                    Nat.shiftDownBits(length, fromBigInteger, numberOfTrailingZeros, 0);
                    int i2 = fromBigInteger2[0];
                    i ^= (numberOfTrailingZeros << 1) & (i2 ^ (i2 >>> 1));
                }
                int compare = Nat.compare(length, fromBigInteger, fromBigInteger2);
                if (compare == 0) {
                    break;
                }
                if (compare < 0) {
                    i ^= fromBigInteger[0] & fromBigInteger2[0];
                    fromBigInteger2 = fromBigInteger;
                    fromBigInteger = fromBigInteger2;
                }
                while (true) {
                    int i3 = length - 1;
                    if (fromBigInteger[i3] != 0) {
                        break;
                    }
                    length = i3;
                }
                Nat.sub(length, fromBigInteger, fromBigInteger2, fromBigInteger);
            }
        }
        if (Nat.isOne(length, fromBigInteger2)) {
            return 1 - (i & 2);
        }
        return 0;
    }

    private BigInteger validate(BigInteger bigInteger, DHParameters dHParameters) {
        if (bigInteger != null) {
            BigInteger p = dHParameters.getP();
            if (bigInteger.compareTo(TWO) < 0 || bigInteger.compareTo(p.subtract(TWO)) > 0) {
                throw new IllegalArgumentException("invalid DH public key");
            }
            BigInteger q = dHParameters.getQ();
            if (q == null) {
                return bigInteger;
            }
            if (!p.testBit(0) || p.bitLength() - 1 != q.bitLength() || !p.shiftRight(1).equals(q)) {
                if (ONE.equals(bigInteger.modPow(q, p))) {
                    return bigInteger;
                }
            } else if (1 == legendre(bigInteger, p)) {
                return bigInteger;
            }
            throw new IllegalArgumentException("Y value does not appear to be in correct group");
        }
        throw new NullPointerException("y value cannot be null");
    }

    @Override // org.bouncycastle.crypto.params.DHKeyParameters
    public boolean equals(Object obj) {
        return (obj instanceof DHPublicKeyParameters) && ((DHPublicKeyParameters) obj).getY().equals(this.y) && super.equals(obj);
    }

    public BigInteger getY() {
        return this.y;
    }

    @Override // org.bouncycastle.crypto.params.DHKeyParameters
    public int hashCode() {
        return this.y.hashCode() ^ super.hashCode();
    }
}
