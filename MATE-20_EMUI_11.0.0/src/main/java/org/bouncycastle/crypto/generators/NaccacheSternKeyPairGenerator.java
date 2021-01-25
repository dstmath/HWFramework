package org.bouncycastle.crypto.generators;

import java.io.PrintStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Vector;
import org.bouncycastle.asn1.eac.CertificateBody;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.NaccacheSternKeyGenerationParameters;
import org.bouncycastle.crypto.params.NaccacheSternKeyParameters;
import org.bouncycastle.crypto.params.NaccacheSternPrivateKeyParameters;
import org.bouncycastle.math.Primes;
import org.bouncycastle.util.BigIntegers;

public class NaccacheSternKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static int[] smallPrimes = {3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, CertificateBody.profileType, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, Primes.SMALL_FACTOR_LIMIT, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541, 547, 557};
    private NaccacheSternKeyGenerationParameters param;

    private static Vector findFirstPrimes(int i) {
        Vector vector = new Vector(i);
        for (int i2 = 0; i2 != i; i2++) {
            vector.addElement(BigInteger.valueOf((long) smallPrimes[i2]));
        }
        return vector;
    }

    private static BigInteger generatePrime(int i, int i2, SecureRandom secureRandom) {
        BigInteger createRandomPrime;
        do {
            createRandomPrime = BigIntegers.createRandomPrime(i, i2, secureRandom);
        } while (createRandomPrime.bitLength() != i);
        return createRandomPrime;
    }

    private static int getInt(SecureRandom secureRandom, int i) {
        int nextInt;
        int i2;
        if (((-i) & i) == i) {
            return (int) ((((long) i) * ((long) (secureRandom.nextInt() & Integer.MAX_VALUE))) >> 31);
        }
        do {
            nextInt = secureRandom.nextInt() & Integer.MAX_VALUE;
            i2 = nextInt % i;
        } while ((nextInt - i2) + (i - 1) < 0);
        return i2;
    }

    private static Vector permuteList(Vector vector, SecureRandom secureRandom) {
        Vector vector2 = new Vector();
        Vector vector3 = new Vector();
        for (int i = 0; i < vector.size(); i++) {
            vector3.addElement(vector.elementAt(i));
        }
        vector2.addElement(vector3.elementAt(0));
        while (true) {
            vector3.removeElementAt(0);
            if (vector3.size() == 0) {
                return vector2;
            }
            vector2.insertElementAt(vector3.elementAt(0), getInt(secureRandom, vector2.size() + 1));
        }
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public AsymmetricCipherKeyPair generateKeyPair() {
        BigInteger generatePrime;
        BigInteger add;
        BigInteger bigInteger;
        BigInteger bigInteger2;
        BigInteger generatePrime2;
        BigInteger add2;
        BigInteger bigInteger3;
        long j;
        BigInteger bigInteger4;
        boolean z;
        BigInteger bigInteger5;
        PrintStream printStream;
        StringBuilder sb;
        String str;
        BigInteger createRandomPrime;
        int strength = this.param.getStrength();
        SecureRandom random = this.param.getRandom();
        int certainty = this.param.getCertainty();
        boolean isDebug = this.param.isDebug();
        if (isDebug) {
            PrintStream printStream2 = System.out;
            printStream2.println("Fetching first " + this.param.getCntSmallPrimes() + " primes.");
        }
        Vector permuteList = permuteList(findFirstPrimes(this.param.getCntSmallPrimes()), random);
        BigInteger bigInteger6 = ONE;
        BigInteger bigInteger7 = bigInteger6;
        for (int i = 0; i < permuteList.size() / 2; i++) {
            bigInteger7 = bigInteger7.multiply((BigInteger) permuteList.elementAt(i));
        }
        for (int size = permuteList.size() / 2; size < permuteList.size(); size++) {
            bigInteger6 = bigInteger6.multiply((BigInteger) permuteList.elementAt(size));
        }
        BigInteger multiply = bigInteger7.multiply(bigInteger6);
        int bitLength = (((strength - multiply.bitLength()) - 48) / 2) + 1;
        BigInteger generatePrime3 = generatePrime(bitLength, certainty, random);
        BigInteger generatePrime4 = generatePrime(bitLength, certainty, random);
        if (isDebug) {
            System.out.println("generating p and q");
        }
        BigInteger shiftLeft = generatePrime3.multiply(bigInteger7).shiftLeft(1);
        BigInteger shiftLeft2 = generatePrime4.multiply(bigInteger6).shiftLeft(1);
        long j2 = 0;
        while (true) {
            j2++;
            int i2 = 24;
            generatePrime = generatePrime(24, certainty, random);
            add = generatePrime.multiply(shiftLeft).add(ONE);
            if (!add.isProbablePrime(certainty)) {
                bigInteger2 = shiftLeft2;
                bigInteger = shiftLeft;
            } else {
                while (true) {
                    generatePrime2 = generatePrime(i2, certainty, random);
                    if (!generatePrime.equals(generatePrime2)) {
                        bigInteger2 = shiftLeft2;
                        add2 = generatePrime2.multiply(shiftLeft2).add(ONE);
                        if (add2.isProbablePrime(certainty)) {
                            break;
                        }
                        shiftLeft2 = bigInteger2;
                        i2 = 24;
                    }
                }
                bigInteger = shiftLeft;
                if (!multiply.gcd(generatePrime.multiply(generatePrime2)).equals(ONE)) {
                    continue;
                } else if (add.multiply(add2).bitLength() >= strength) {
                    break;
                } else if (isDebug) {
                    PrintStream printStream3 = System.out;
                    printStream3.println("key size too small. Should be " + strength + " but is actually " + add.multiply(add2).bitLength());
                }
            }
            shiftLeft2 = bigInteger2;
            shiftLeft = bigInteger;
        }
        if (isDebug) {
            PrintStream printStream4 = System.out;
            bigInteger3 = generatePrime4;
            printStream4.println("needed " + j2 + " tries to generate p and q.");
        } else {
            bigInteger3 = generatePrime4;
        }
        BigInteger multiply2 = add.multiply(add2);
        BigInteger multiply3 = add.subtract(ONE).multiply(add2.subtract(ONE));
        if (isDebug) {
            System.out.println("generating g");
        }
        long j3 = 0;
        while (true) {
            Vector vector = new Vector();
            j = j3;
            int i3 = 0;
            while (i3 != permuteList.size()) {
                BigInteger divide = multiply3.divide((BigInteger) permuteList.elementAt(i3));
                while (true) {
                    j++;
                    createRandomPrime = BigIntegers.createRandomPrime(strength, certainty, random);
                    if (!createRandomPrime.modPow(divide, multiply2).equals(ONE)) {
                        break;
                    }
                    divide = divide;
                    add = add;
                    strength = strength;
                }
                vector.addElement(createRandomPrime);
                i3++;
                add = add;
                strength = strength;
            }
            bigInteger4 = ONE;
            for (int i4 = 0; i4 < permuteList.size(); i4++) {
                bigInteger4 = bigInteger4.multiply(((BigInteger) vector.elementAt(i4)).modPow(multiply.divide((BigInteger) permuteList.elementAt(i4)), multiply2)).mod(multiply2);
            }
            int i5 = 0;
            while (true) {
                if (i5 >= permuteList.size()) {
                    z = false;
                    break;
                } else if (bigInteger4.modPow(multiply3.divide((BigInteger) permuteList.elementAt(i5)), multiply2).equals(ONE)) {
                    if (isDebug) {
                        PrintStream printStream5 = System.out;
                        printStream5.println("g has order phi(n)/" + permuteList.elementAt(i5) + "\n g: " + bigInteger4);
                    }
                    z = true;
                } else {
                    i5++;
                }
            }
            if (!z) {
                if (!bigInteger4.modPow(multiply3.divide(BigInteger.valueOf(4)), multiply2).equals(ONE)) {
                    if (!bigInteger4.modPow(multiply3.divide(generatePrime), multiply2).equals(ONE)) {
                        if (!bigInteger4.modPow(multiply3.divide(generatePrime2), multiply2).equals(ONE)) {
                            if (!bigInteger4.modPow(multiply3.divide(generatePrime3), multiply2).equals(ONE)) {
                                bigInteger5 = bigInteger3;
                                if (!bigInteger4.modPow(multiply3.divide(bigInteger5), multiply2).equals(ONE)) {
                                    break;
                                }
                                if (isDebug) {
                                    PrintStream printStream6 = System.out;
                                    printStream6.println("g has order phi(n)/b\n g: " + bigInteger4);
                                }
                                bigInteger3 = bigInteger5;
                                j3 = j;
                                add2 = add2;
                                add = add;
                                strength = strength;
                            } else if (isDebug) {
                                printStream = System.out;
                                sb = new StringBuilder();
                                str = "g has order phi(n)/a\n g: ";
                            }
                        } else if (isDebug) {
                            printStream = System.out;
                            sb = new StringBuilder();
                            str = "g has order phi(n)/q'\n g: ";
                        }
                    } else if (isDebug) {
                        printStream = System.out;
                        sb = new StringBuilder();
                        str = "g has order phi(n)/p'\n g: ";
                    }
                } else if (isDebug) {
                    printStream = System.out;
                    sb = new StringBuilder();
                    str = "g has order phi(n)/4\n g:";
                }
                sb.append(str);
                sb.append(bigInteger4);
                printStream.println(sb.toString());
            }
            bigInteger5 = bigInteger3;
            bigInteger3 = bigInteger5;
            j3 = j;
            add2 = add2;
            add = add;
            strength = strength;
        }
        if (isDebug) {
            PrintStream printStream7 = System.out;
            printStream7.println("needed " + j + " tries to generate g");
            System.out.println();
            System.out.println("found new NaccacheStern cipher variables:");
            PrintStream printStream8 = System.out;
            printStream8.println("smallPrimes: " + permuteList);
            PrintStream printStream9 = System.out;
            printStream9.println("sigma:...... " + multiply + " (" + multiply.bitLength() + " bits)");
            PrintStream printStream10 = System.out;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("a:.......... ");
            sb2.append(generatePrime3);
            printStream10.println(sb2.toString());
            PrintStream printStream11 = System.out;
            printStream11.println("b:.......... " + bigInteger5);
            PrintStream printStream12 = System.out;
            printStream12.println("p':......... " + generatePrime);
            PrintStream printStream13 = System.out;
            printStream13.println("q':......... " + generatePrime2);
            PrintStream printStream14 = System.out;
            printStream14.println("p:.......... " + add);
            PrintStream printStream15 = System.out;
            printStream15.println("q:.......... " + add2);
            PrintStream printStream16 = System.out;
            printStream16.println("n:.......... " + multiply2);
            PrintStream printStream17 = System.out;
            printStream17.println("phi(n):..... " + multiply3);
            PrintStream printStream18 = System.out;
            printStream18.println("g:.......... " + bigInteger4);
            System.out.println();
        }
        return new AsymmetricCipherKeyPair((AsymmetricKeyParameter) new NaccacheSternKeyParameters(false, bigInteger4, multiply2, multiply.bitLength()), (AsymmetricKeyParameter) new NaccacheSternPrivateKeyParameters(bigInteger4, multiply2, multiply.bitLength(), permuteList, multiply3));
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public void init(KeyGenerationParameters keyGenerationParameters) {
        this.param = (NaccacheSternKeyGenerationParameters) keyGenerationParameters;
    }
}
