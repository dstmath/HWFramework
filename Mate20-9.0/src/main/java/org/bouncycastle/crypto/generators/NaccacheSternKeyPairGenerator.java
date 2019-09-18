package org.bouncycastle.crypto.generators;

import java.io.PrintStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Vector;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.NaccacheSternKeyGenerationParameters;
import org.bouncycastle.crypto.params.NaccacheSternKeyParameters;
import org.bouncycastle.crypto.params.NaccacheSternPrivateKeyParameters;
import org.bouncycastle.crypto.tls.ExtensionType;

public class NaccacheSternKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static int[] smallPrimes;
    private NaccacheSternKeyGenerationParameters param;

    static {
        int[] iArr = new int[ExtensionType.negotiated_ff_dhe_groups];
        // fill-array-data instruction
        iArr[0] = 3;
        iArr[1] = 5;
        iArr[2] = 7;
        iArr[3] = 11;
        iArr[4] = 13;
        iArr[5] = 17;
        iArr[6] = 19;
        iArr[7] = 23;
        iArr[8] = 29;
        iArr[9] = 31;
        iArr[10] = 37;
        iArr[11] = 41;
        iArr[12] = 43;
        iArr[13] = 47;
        iArr[14] = 53;
        iArr[15] = 59;
        iArr[16] = 61;
        iArr[17] = 67;
        iArr[18] = 71;
        iArr[19] = 73;
        iArr[20] = 79;
        iArr[21] = 83;
        iArr[22] = 89;
        iArr[23] = 97;
        iArr[24] = 101;
        iArr[25] = 103;
        iArr[26] = 107;
        iArr[27] = 109;
        iArr[28] = 113;
        iArr[29] = 127;
        iArr[30] = 131;
        iArr[31] = 137;
        iArr[32] = 139;
        iArr[33] = 149;
        iArr[34] = 151;
        iArr[35] = 157;
        iArr[36] = 163;
        iArr[37] = 167;
        iArr[38] = 173;
        iArr[39] = 179;
        iArr[40] = 181;
        iArr[41] = 191;
        iArr[42] = 193;
        iArr[43] = 197;
        iArr[44] = 199;
        iArr[45] = 211;
        iArr[46] = 223;
        iArr[47] = 227;
        iArr[48] = 229;
        iArr[49] = 233;
        iArr[50] = 239;
        iArr[51] = 241;
        iArr[52] = 251;
        iArr[53] = 257;
        iArr[54] = 263;
        iArr[55] = 269;
        iArr[56] = 271;
        iArr[57] = 277;
        iArr[58] = 281;
        iArr[59] = 283;
        iArr[60] = 293;
        iArr[61] = 307;
        iArr[62] = 311;
        iArr[63] = 313;
        iArr[64] = 317;
        iArr[65] = 331;
        iArr[66] = 337;
        iArr[67] = 347;
        iArr[68] = 349;
        iArr[69] = 353;
        iArr[70] = 359;
        iArr[71] = 367;
        iArr[72] = 373;
        iArr[73] = 379;
        iArr[74] = 383;
        iArr[75] = 389;
        iArr[76] = 397;
        iArr[77] = 401;
        iArr[78] = 409;
        iArr[79] = 419;
        iArr[80] = 421;
        iArr[81] = 431;
        iArr[82] = 433;
        iArr[83] = 439;
        iArr[84] = 443;
        iArr[85] = 449;
        iArr[86] = 457;
        iArr[87] = 461;
        iArr[88] = 463;
        iArr[89] = 467;
        iArr[90] = 479;
        iArr[91] = 487;
        iArr[92] = 491;
        iArr[93] = 499;
        iArr[94] = 503;
        iArr[95] = 509;
        iArr[96] = 521;
        iArr[97] = 523;
        iArr[98] = 541;
        iArr[99] = 547;
        iArr[100] = 557;
        smallPrimes = iArr;
    }

    private static Vector findFirstPrimes(int i) {
        Vector vector = new Vector(i);
        for (int i2 = 0; i2 != i; i2++) {
            vector.addElement(BigInteger.valueOf((long) smallPrimes[i2]));
        }
        return vector;
    }

    private static BigInteger generatePrime(int i, int i2, SecureRandom secureRandom) {
        BigInteger bigInteger = new BigInteger(i, i2, secureRandom);
        while (bigInteger.bitLength() != i) {
            bigInteger = new BigInteger(i, i2, secureRandom);
        }
        return bigInteger;
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

    public AsymmetricCipherKeyPair generateKeyPair() {
        BigInteger generatePrime;
        BigInteger add;
        BigInteger bigInteger;
        BigInteger bigInteger2;
        BigInteger generatePrime2;
        BigInteger add2;
        BigInteger bigInteger3;
        BigInteger bigInteger4;
        BigInteger bigInteger5;
        long j;
        BigInteger bigInteger6;
        boolean z;
        BigInteger bigInteger7;
        PrintStream printStream;
        StringBuilder sb;
        String str;
        long j2;
        BigInteger bigInteger8;
        int i;
        int strength = this.param.getStrength();
        SecureRandom random = this.param.getRandom();
        int certainty = this.param.getCertainty();
        boolean isDebug = this.param.isDebug();
        if (isDebug) {
            PrintStream printStream2 = System.out;
            printStream2.println("Fetching first " + this.param.getCntSmallPrimes() + " primes.");
        }
        Vector permuteList = permuteList(findFirstPrimes(this.param.getCntSmallPrimes()), random);
        BigInteger bigInteger9 = ONE;
        BigInteger bigInteger10 = ONE;
        BigInteger bigInteger11 = bigInteger9;
        for (int i2 = 0; i2 < permuteList.size() / 2; i2++) {
            bigInteger11 = bigInteger11.multiply((BigInteger) permuteList.elementAt(i2));
        }
        for (int size = permuteList.size() / 2; size < permuteList.size(); size++) {
            bigInteger10 = bigInteger10.multiply((BigInteger) permuteList.elementAt(size));
        }
        BigInteger multiply = bigInteger11.multiply(bigInteger10);
        int bitLength = (((strength - multiply.bitLength()) - 48) / 2) + 1;
        BigInteger generatePrime3 = generatePrime(bitLength, certainty, random);
        BigInteger generatePrime4 = generatePrime(bitLength, certainty, random);
        if (isDebug) {
            System.out.println("generating p and q");
        }
        BigInteger shiftLeft = generatePrime3.multiply(bigInteger11).shiftLeft(1);
        BigInteger shiftLeft2 = generatePrime4.multiply(bigInteger10).shiftLeft(1);
        long j3 = 0;
        while (true) {
            j3++;
            int i3 = 24;
            generatePrime = generatePrime(24, certainty, random);
            add = generatePrime.multiply(shiftLeft).add(ONE);
            if (!add.isProbablePrime(certainty)) {
                bigInteger2 = shiftLeft2;
                bigInteger = shiftLeft;
            } else {
                while (true) {
                    generatePrime2 = generatePrime(i3, certainty, random);
                    if (!generatePrime.equals(generatePrime2)) {
                        bigInteger2 = shiftLeft2;
                        add2 = generatePrime2.multiply(shiftLeft2).add(ONE);
                        if (add2.isProbablePrime(certainty)) {
                            break;
                        }
                        int i4 = strength;
                        BigInteger bigInteger12 = add;
                        shiftLeft2 = bigInteger2;
                        i3 = 24;
                    }
                }
                bigInteger = shiftLeft;
                if (multiply.gcd(generatePrime.multiply(generatePrime2)).equals(ONE)) {
                    if (add.multiply(add2).bitLength() >= strength) {
                        break;
                    } else if (isDebug) {
                        PrintStream printStream3 = System.out;
                        printStream3.println("key size too small. Should be " + strength + " but is actually " + add.multiply(add2).bitLength());
                    }
                } else {
                    continue;
                }
            }
            shiftLeft2 = bigInteger2;
            shiftLeft = bigInteger;
        }
        if (isDebug) {
            PrintStream printStream4 = System.out;
            StringBuilder sb2 = new StringBuilder();
            bigInteger3 = generatePrime4;
            sb2.append("needed ");
            sb2.append(j3);
            sb2.append(" tries to generate p and q.");
            printStream4.println(sb2.toString());
        } else {
            bigInteger3 = generatePrime4;
        }
        BigInteger multiply2 = add.multiply(add2);
        BigInteger multiply3 = add.subtract(ONE).multiply(add2.subtract(ONE));
        if (isDebug) {
            System.out.println("generating g");
        }
        long j4 = 0;
        while (true) {
            Vector vector = new Vector();
            bigInteger4 = add2;
            bigInteger5 = add;
            j = j4;
            int i5 = 0;
            while (i5 != permuteList.size()) {
                BigInteger divide = multiply3.divide((BigInteger) permuteList.elementAt(i5));
                while (true) {
                    j2 = j + 1;
                    bigInteger8 = new BigInteger(strength, certainty, random);
                    i = strength;
                    if (!bigInteger8.modPow(divide, multiply2).equals(ONE)) {
                        break;
                    }
                    j = j2;
                    strength = i;
                }
                vector.addElement(bigInteger8);
                i5++;
                j = j2;
                strength = i;
            }
            int i6 = strength;
            bigInteger6 = ONE;
            int i7 = 0;
            while (i7 < permuteList.size()) {
                bigInteger6 = bigInteger6.multiply(((BigInteger) vector.elementAt(i7)).modPow(multiply.divide((BigInteger) permuteList.elementAt(i7)), multiply2)).mod(multiply2);
                i7++;
                random = random;
            }
            SecureRandom secureRandom = random;
            int i8 = 0;
            while (true) {
                if (i8 >= permuteList.size()) {
                    z = false;
                    break;
                } else if (bigInteger6.modPow(multiply3.divide((BigInteger) permuteList.elementAt(i8)), multiply2).equals(ONE)) {
                    if (isDebug) {
                        PrintStream printStream5 = System.out;
                        printStream5.println("g has order phi(n)/" + permuteList.elementAt(i8) + "\n g: " + bigInteger6);
                    }
                    z = true;
                } else {
                    i8++;
                }
            }
            if (!z) {
                if (!bigInteger6.modPow(multiply3.divide(BigInteger.valueOf(4)), multiply2).equals(ONE)) {
                    if (!bigInteger6.modPow(multiply3.divide(generatePrime), multiply2).equals(ONE)) {
                        if (!bigInteger6.modPow(multiply3.divide(generatePrime2), multiply2).equals(ONE)) {
                            if (!bigInteger6.modPow(multiply3.divide(generatePrime3), multiply2).equals(ONE)) {
                                bigInteger7 = bigInteger3;
                                if (!bigInteger6.modPow(multiply3.divide(bigInteger7), multiply2).equals(ONE)) {
                                    break;
                                }
                                if (isDebug) {
                                    PrintStream printStream6 = System.out;
                                    printStream6.println("g has order phi(n)/b\n g: " + bigInteger6);
                                }
                                bigInteger3 = bigInteger7;
                                j4 = j;
                                add = bigInteger5;
                                add2 = bigInteger4;
                                strength = i6;
                                random = secureRandom;
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
                sb.append(bigInteger6);
                printStream.println(sb.toString());
            }
            bigInteger7 = bigInteger3;
            bigInteger3 = bigInteger7;
            j4 = j;
            add = bigInteger5;
            add2 = bigInteger4;
            strength = i6;
            random = secureRandom;
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
            StringBuilder sb3 = new StringBuilder();
            sb3.append("a:.......... ");
            sb3.append(generatePrime3);
            printStream10.println(sb3.toString());
            PrintStream printStream11 = System.out;
            printStream11.println("b:.......... " + bigInteger7);
            PrintStream printStream12 = System.out;
            printStream12.println("p':......... " + generatePrime);
            PrintStream printStream13 = System.out;
            printStream13.println("q':......... " + generatePrime2);
            PrintStream printStream14 = System.out;
            printStream14.println("p:.......... " + bigInteger5);
            PrintStream printStream15 = System.out;
            printStream15.println("q:.......... " + bigInteger4);
            PrintStream printStream16 = System.out;
            printStream16.println("n:.......... " + multiply2);
            PrintStream printStream17 = System.out;
            printStream17.println("phi(n):..... " + multiply3);
            PrintStream printStream18 = System.out;
            printStream18.println("g:.......... " + bigInteger6);
            System.out.println();
        }
        NaccacheSternKeyParameters naccacheSternKeyParameters = new NaccacheSternKeyParameters(false, bigInteger6, multiply2, multiply.bitLength());
        NaccacheSternPrivateKeyParameters naccacheSternPrivateKeyParameters = new NaccacheSternPrivateKeyParameters(bigInteger6, multiply2, multiply.bitLength(), permuteList, multiply3);
        return new AsymmetricCipherKeyPair((AsymmetricKeyParameter) naccacheSternKeyParameters, (AsymmetricKeyParameter) naccacheSternPrivateKeyParameters);
    }

    public void init(KeyGenerationParameters keyGenerationParameters) {
        this.param = (NaccacheSternKeyGenerationParameters) keyGenerationParameters;
    }
}
