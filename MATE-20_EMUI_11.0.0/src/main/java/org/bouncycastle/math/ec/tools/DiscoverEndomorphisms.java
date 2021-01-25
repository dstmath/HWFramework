package org.bouncycastle.math.ec.tools;

import java.io.PrintStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TreeSet;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

public class DiscoverEndomorphisms {
    private static final int radix = 16;

    private static boolean areRelativelyPrime(BigInteger bigInteger, BigInteger bigInteger2) {
        return bigInteger.gcd(bigInteger2).equals(ECConstants.ONE);
    }

    private static BigInteger[] calculateRange(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3) {
        return order(bigInteger.subtract(bigInteger2).divide(bigInteger3), bigInteger.add(bigInteger2).divide(bigInteger3));
    }

    private static BigInteger[] chooseShortest(BigInteger[] bigIntegerArr, BigInteger[] bigIntegerArr2) {
        return isShorter(bigIntegerArr, bigIntegerArr2) ? bigIntegerArr : bigIntegerArr2;
    }

    private static void discoverEndomorphisms(String str) {
        X9ECParameters byName = CustomNamedCurves.getByName(str);
        if (byName == null && (byName = ECNamedCurveTable.getByName(str)) == null) {
            PrintStream printStream = System.err;
            printStream.println("Unknown curve: " + str);
            return;
        }
        discoverEndomorphisms(byName, str);
    }

    public static void discoverEndomorphisms(X9ECParameters x9ECParameters) {
        if (x9ECParameters != null) {
            discoverEndomorphisms(x9ECParameters, "<UNKNOWN>");
            return;
        }
        throw new NullPointerException("x9");
    }

    private static void discoverEndomorphisms(X9ECParameters x9ECParameters, String str) {
        ECCurve curve = x9ECParameters.getCurve();
        if (ECAlgorithms.isFpCurve(curve)) {
            BigInteger characteristic = curve.getField().getCharacteristic();
            if (curve.getB().isZero() && characteristic.mod(ECConstants.FOUR).equals(ECConstants.ONE)) {
                PrintStream printStream = System.out;
                printStream.println("Curve '" + str + "' has a 'GLV Type A' endomorphism with these parameters:");
                printGLVTypeAParameters(x9ECParameters);
            }
            if (curve.getA().isZero() && characteristic.mod(ECConstants.THREE).equals(ECConstants.ONE)) {
                PrintStream printStream2 = System.out;
                printStream2.println("Curve '" + str + "' has a 'GLV Type B' endomorphism with these parameters:");
                printGLVTypeBParameters(x9ECParameters);
            }
        }
    }

    private static ArrayList enumToList(Enumeration enumeration) {
        ArrayList arrayList = new ArrayList();
        while (enumeration.hasMoreElements()) {
            arrayList.add(enumeration.nextElement());
        }
        return arrayList;
    }

    private static BigInteger[] extEuclidBezout(BigInteger[] bigIntegerArr) {
        boolean z = bigIntegerArr[0].compareTo(bigIntegerArr[1]) < 0;
        if (z) {
            swap(bigIntegerArr);
        }
        BigInteger bigInteger = bigIntegerArr[0];
        BigInteger bigInteger2 = bigIntegerArr[1];
        BigInteger bigInteger3 = ECConstants.ONE;
        BigInteger bigInteger4 = ECConstants.ZERO;
        BigInteger bigInteger5 = ECConstants.ZERO;
        BigInteger bigInteger6 = ECConstants.ONE;
        BigInteger bigInteger7 = bigInteger2;
        BigInteger bigInteger8 = bigInteger;
        while (bigInteger7.compareTo(ECConstants.ONE) > 0) {
            BigInteger[] divideAndRemainder = bigInteger8.divideAndRemainder(bigInteger7);
            BigInteger bigInteger9 = divideAndRemainder[0];
            bigInteger7 = divideAndRemainder[1];
            bigInteger8 = bigInteger7;
            bigInteger4 = bigInteger3.subtract(bigInteger9.multiply(bigInteger4));
            bigInteger3 = bigInteger4;
            bigInteger6 = bigInteger5.subtract(bigInteger9.multiply(bigInteger6));
            bigInteger5 = bigInteger6;
        }
        if (bigInteger7.signum() <= 0) {
            return null;
        }
        BigInteger[] bigIntegerArr2 = {bigInteger4, bigInteger6};
        if (z) {
            swap(bigIntegerArr2);
        }
        return bigIntegerArr2;
    }

    private static BigInteger[] extEuclidGLV(BigInteger bigInteger, BigInteger bigInteger2) {
        BigInteger bigInteger3 = ECConstants.ZERO;
        BigInteger bigInteger4 = ECConstants.ONE;
        BigInteger bigInteger5 = bigInteger3;
        BigInteger bigInteger6 = bigInteger2;
        BigInteger bigInteger7 = bigInteger;
        while (true) {
            BigInteger[] divideAndRemainder = bigInteger7.divideAndRemainder(bigInteger6);
            BigInteger bigInteger8 = divideAndRemainder[0];
            BigInteger bigInteger9 = divideAndRemainder[1];
            BigInteger subtract = bigInteger5.subtract(bigInteger8.multiply(bigInteger4));
            if (isLessThanSqrt(bigInteger6, bigInteger)) {
                return new BigInteger[]{bigInteger7, bigInteger5, bigInteger6, bigInteger4, bigInteger9, subtract};
            }
            bigInteger7 = bigInteger6;
            bigInteger5 = bigInteger4;
            bigInteger6 = bigInteger9;
            bigInteger4 = subtract;
        }
    }

    private static ECFieldElement[] findNonTrivialOrder3FieldElements(ECCurve eCCurve) {
        BigInteger modPow;
        BigInteger characteristic = eCCurve.getField().getCharacteristic();
        BigInteger divide = characteristic.divide(ECConstants.THREE);
        SecureRandom secureRandom = new SecureRandom();
        do {
            modPow = BigIntegers.createRandomInRange(ECConstants.TWO, characteristic.subtract(ECConstants.TWO), secureRandom).modPow(divide, characteristic);
        } while (modPow.equals(ECConstants.ONE));
        ECFieldElement fromBigInteger = eCCurve.fromBigInteger(modPow);
        return new ECFieldElement[]{fromBigInteger, fromBigInteger.square()};
    }

    private static ECFieldElement[] findNonTrivialOrder4FieldElements(ECCurve eCCurve) {
        ECFieldElement sqrt = eCCurve.fromBigInteger(ECConstants.ONE).negate().sqrt();
        if (sqrt != null) {
            return new ECFieldElement[]{sqrt, sqrt.negate()};
        }
        throw new IllegalStateException("Calculation of non-trivial order-4  field elements failed unexpectedly");
    }

    private static BigInteger[] intersect(BigInteger[] bigIntegerArr, BigInteger[] bigIntegerArr2) {
        BigInteger max = bigIntegerArr[0].max(bigIntegerArr2[0]);
        BigInteger min = bigIntegerArr[1].min(bigIntegerArr2[1]);
        if (max.compareTo(min) > 0) {
            return null;
        }
        return new BigInteger[]{max, min};
    }

    private static boolean isLessThanSqrt(BigInteger bigInteger, BigInteger bigInteger2) {
        BigInteger abs = bigInteger.abs();
        BigInteger abs2 = bigInteger2.abs();
        int bitLength = abs2.bitLength();
        int bitLength2 = abs.bitLength() * 2;
        return bitLength2 + -1 <= bitLength && (bitLength2 < bitLength || abs.multiply(abs).compareTo(abs2) < 0);
    }

    private static boolean isShorter(BigInteger[] bigIntegerArr, BigInteger[] bigIntegerArr2) {
        BigInteger abs = bigIntegerArr[0].abs();
        BigInteger abs2 = bigIntegerArr[1].abs();
        BigInteger abs3 = bigIntegerArr2[0].abs();
        BigInteger abs4 = bigIntegerArr2[1].abs();
        boolean z = abs.compareTo(abs3) < 0;
        return z == (abs2.compareTo(abs4) < 0) ? z : abs.multiply(abs).add(abs2.multiply(abs2)).compareTo(abs3.multiply(abs3).add(abs4.multiply(abs4))) < 0;
    }

    private static boolean isVectorBoundedBySqrt(BigInteger[] bigIntegerArr, BigInteger bigInteger) {
        return isLessThanSqrt(bigIntegerArr[0].abs().max(bigIntegerArr[1].abs()), bigInteger);
    }

    private static BigInteger isqrt(BigInteger bigInteger) {
        BigInteger shiftRight = bigInteger.shiftRight(bigInteger.bitLength() / 2);
        while (true) {
            BigInteger shiftRight2 = shiftRight.add(bigInteger.divide(shiftRight)).shiftRight(1);
            if (shiftRight2.equals(shiftRight)) {
                return shiftRight2;
            }
            shiftRight = shiftRight2;
        }
    }

    public static void main(String[] strArr) {
        if (strArr.length > 0) {
            for (String str : strArr) {
                discoverEndomorphisms(str);
            }
            return;
        }
        TreeSet<String> treeSet = new TreeSet(enumToList(ECNamedCurveTable.getNames()));
        treeSet.addAll(enumToList(CustomNamedCurves.getNames()));
        for (String str2 : treeSet) {
            discoverEndomorphisms(str2);
        }
    }

    private static BigInteger[] order(BigInteger bigInteger, BigInteger bigInteger2) {
        return bigInteger.compareTo(bigInteger2) <= 0 ? new BigInteger[]{bigInteger, bigInteger2} : new BigInteger[]{bigInteger2, bigInteger};
    }

    private static void printGLVTypeAParameters(X9ECParameters x9ECParameters) {
        BigInteger[] solveQuadraticEquation = solveQuadraticEquation(x9ECParameters.getN(), ECConstants.ONE, ECConstants.ZERO, ECConstants.ONE);
        ECFieldElement[] findNonTrivialOrder4FieldElements = findNonTrivialOrder4FieldElements(x9ECParameters.getCurve());
        printGLVTypeAParameters(x9ECParameters, solveQuadraticEquation[0], findNonTrivialOrder4FieldElements);
        System.out.println("OR");
        printGLVTypeAParameters(x9ECParameters, solveQuadraticEquation[1], findNonTrivialOrder4FieldElements);
    }

    private static void printGLVTypeAParameters(X9ECParameters x9ECParameters, BigInteger bigInteger, ECFieldElement[] eCFieldElementArr) {
        ECPoint normalize = x9ECParameters.getG().normalize();
        ECPoint normalize2 = normalize.multiply(bigInteger).normalize();
        if (normalize.getXCoord().negate().equals(normalize2.getXCoord())) {
            ECFieldElement eCFieldElement = eCFieldElementArr[0];
            if (!normalize.getYCoord().multiply(eCFieldElement).equals(normalize2.getYCoord())) {
                eCFieldElement = eCFieldElementArr[1];
                if (!normalize.getYCoord().multiply(eCFieldElement).equals(normalize2.getYCoord())) {
                    throw new IllegalStateException("Derivation of GLV Type A parameters failed unexpectedly");
                }
            }
            printProperty("Point map", "lambda * (x, y) = (-x, i * y)");
            printProperty("i", eCFieldElement.toBigInteger().toString(16));
            printProperty("lambda", bigInteger.toString(16));
            printScalarDecompositionParameters(x9ECParameters.getN(), bigInteger);
            return;
        }
        throw new IllegalStateException("Derivation of GLV Type A parameters failed unexpectedly");
    }

    private static void printGLVTypeBParameters(X9ECParameters x9ECParameters) {
        BigInteger[] solveQuadraticEquation = solveQuadraticEquation(x9ECParameters.getN(), ECConstants.ONE, ECConstants.ONE, ECConstants.ONE);
        ECFieldElement[] findNonTrivialOrder3FieldElements = findNonTrivialOrder3FieldElements(x9ECParameters.getCurve());
        printGLVTypeBParameters(x9ECParameters, solveQuadraticEquation[0], findNonTrivialOrder3FieldElements);
        System.out.println("OR");
        printGLVTypeBParameters(x9ECParameters, solveQuadraticEquation[1], findNonTrivialOrder3FieldElements);
    }

    private static void printGLVTypeBParameters(X9ECParameters x9ECParameters, BigInteger bigInteger, ECFieldElement[] eCFieldElementArr) {
        ECPoint normalize = x9ECParameters.getG().normalize();
        ECPoint normalize2 = normalize.multiply(bigInteger).normalize();
        if (normalize.getYCoord().equals(normalize2.getYCoord())) {
            ECFieldElement eCFieldElement = eCFieldElementArr[0];
            if (!normalize.getXCoord().multiply(eCFieldElement).equals(normalize2.getXCoord())) {
                eCFieldElement = eCFieldElementArr[1];
                if (!normalize.getXCoord().multiply(eCFieldElement).equals(normalize2.getXCoord())) {
                    throw new IllegalStateException("Derivation of GLV Type B parameters failed unexpectedly");
                }
            }
            printProperty("Point map", "lambda * (x, y) = (beta * x, y)");
            printProperty("beta", eCFieldElement.toBigInteger().toString(16));
            printProperty("lambda", bigInteger.toString(16));
            printScalarDecompositionParameters(x9ECParameters.getN(), bigInteger);
            return;
        }
        throw new IllegalStateException("Derivation of GLV Type B parameters failed unexpectedly");
    }

    private static void printProperty(String str, Object obj) {
        StringBuffer stringBuffer = new StringBuffer("  ");
        stringBuffer.append(str);
        while (stringBuffer.length() < 20) {
            stringBuffer.append(' ');
        }
        stringBuffer.append(": ");
        stringBuffer.append(obj.toString());
        System.out.println(stringBuffer.toString());
    }

    private static void printScalarDecompositionParameters(BigInteger bigInteger, BigInteger bigInteger2) {
        BigInteger[] extEuclidGLV = extEuclidGLV(bigInteger, bigInteger2);
        BigInteger[] bigIntegerArr = {extEuclidGLV[2], extEuclidGLV[3].negate()};
        BigInteger[] chooseShortest = chooseShortest(new BigInteger[]{extEuclidGLV[0], extEuclidGLV[1].negate()}, new BigInteger[]{extEuclidGLV[4], extEuclidGLV[5].negate()});
        if (!isVectorBoundedBySqrt(chooseShortest, bigInteger) && areRelativelyPrime(bigIntegerArr[0], bigIntegerArr[1])) {
            BigInteger bigInteger3 = bigIntegerArr[0];
            BigInteger bigInteger4 = bigIntegerArr[1];
            BigInteger divide = bigInteger3.add(bigInteger4.multiply(bigInteger2)).divide(bigInteger);
            BigInteger[] extEuclidBezout = extEuclidBezout(new BigInteger[]{divide.abs(), bigInteger4.abs()});
            if (extEuclidBezout != null) {
                BigInteger bigInteger5 = extEuclidBezout[0];
                BigInteger bigInteger6 = extEuclidBezout[1];
                if (divide.signum() < 0) {
                    bigInteger5 = bigInteger5.negate();
                }
                if (bigInteger4.signum() > 0) {
                    bigInteger6 = bigInteger6.negate();
                }
                if (divide.multiply(bigInteger5).subtract(bigInteger4.multiply(bigInteger6)).equals(ECConstants.ONE)) {
                    BigInteger subtract = bigInteger6.multiply(bigInteger).subtract(bigInteger5.multiply(bigInteger2));
                    BigInteger negate = bigInteger5.negate();
                    BigInteger negate2 = subtract.negate();
                    BigInteger add = isqrt(bigInteger.subtract(ECConstants.ONE)).add(ECConstants.ONE);
                    BigInteger[] intersect = intersect(calculateRange(negate, add, bigInteger4), calculateRange(negate2, add, bigInteger3));
                    if (intersect != null) {
                        for (BigInteger bigInteger7 = intersect[0]; bigInteger7.compareTo(intersect[1]) <= 0; bigInteger7 = bigInteger7.add(ECConstants.ONE)) {
                            BigInteger[] bigIntegerArr2 = {subtract.add(bigInteger7.multiply(bigInteger3)), bigInteger5.add(bigInteger7.multiply(bigInteger4))};
                            if (isShorter(bigIntegerArr2, chooseShortest)) {
                                chooseShortest = bigIntegerArr2;
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException();
                }
            }
        }
        BigInteger subtract2 = bigIntegerArr[0].multiply(chooseShortest[1]).subtract(bigIntegerArr[1].multiply(chooseShortest[0]));
        int bitLength = (bigInteger.bitLength() + 16) - (bigInteger.bitLength() & 7);
        BigInteger roundQuotient = roundQuotient(chooseShortest[1].shiftLeft(bitLength), subtract2);
        BigInteger negate3 = roundQuotient(bigIntegerArr[1].shiftLeft(bitLength), subtract2).negate();
        printProperty("v1", "{ " + bigIntegerArr[0].toString(16) + ", " + bigIntegerArr[1].toString(16) + " }");
        printProperty("v2", "{ " + chooseShortest[0].toString(16) + ", " + chooseShortest[1].toString(16) + " }");
        printProperty("d", subtract2.toString(16));
        printProperty("(OPT) g1", roundQuotient.toString(16));
        printProperty("(OPT) g2", negate3.toString(16));
        printProperty("(OPT) bits", Integer.toString(bitLength));
    }

    private static BigInteger roundQuotient(BigInteger bigInteger, BigInteger bigInteger2) {
        boolean z = bigInteger.signum() != bigInteger2.signum();
        BigInteger abs = bigInteger.abs();
        BigInteger abs2 = bigInteger2.abs();
        BigInteger divide = abs.add(abs2.shiftRight(1)).divide(abs2);
        return z ? divide.negate() : divide;
    }

    private static BigInteger[] solveQuadraticEquation(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4) {
        ECFieldElement sqrt = new ECFieldElement.Fp(bigInteger, bigInteger3.multiply(bigInteger3).subtract(bigInteger2.multiply(bigInteger4).shiftLeft(2)).mod(bigInteger)).sqrt();
        if (sqrt != null) {
            BigInteger bigInteger5 = sqrt.toBigInteger();
            BigInteger modInverse = bigInteger2.shiftLeft(1).modInverse(bigInteger);
            return new BigInteger[]{bigInteger5.subtract(bigInteger3).multiply(modInverse).mod(bigInteger), bigInteger5.negate().subtract(bigInteger3).multiply(modInverse).mod(bigInteger)};
        }
        throw new IllegalStateException("Solving quadratic equation failed unexpectedly");
    }

    private static void swap(BigInteger[] bigIntegerArr) {
        BigInteger bigInteger = bigIntegerArr[0];
        bigIntegerArr[0] = bigIntegerArr[1];
        bigIntegerArr[1] = bigInteger;
    }
}
