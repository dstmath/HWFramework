package org.bouncycastle.crypto.engines;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Vector;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.NaccacheSternKeyParameters;
import org.bouncycastle.crypto.params.NaccacheSternPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.util.Arrays;

public class NaccacheSternEngine implements AsymmetricBlockCipher {
    private static BigInteger ONE = BigInteger.valueOf(1);
    private static BigInteger ZERO = BigInteger.valueOf(0);
    private boolean debug = false;
    private boolean forEncryption;
    private NaccacheSternKeyParameters key;
    private Vector[] lookup = null;

    private static BigInteger chineseRemainder(Vector vector, Vector vector2) {
        BigInteger bigInteger = ZERO;
        BigInteger bigInteger2 = ONE;
        for (int i = 0; i < vector2.size(); i++) {
            bigInteger2 = bigInteger2.multiply((BigInteger) vector2.elementAt(i));
        }
        for (int i2 = 0; i2 < vector2.size(); i2++) {
            BigInteger bigInteger3 = (BigInteger) vector2.elementAt(i2);
            BigInteger divide = bigInteger2.divide(bigInteger3);
            bigInteger = bigInteger.add(divide.multiply(divide.modInverse(bigInteger3)).multiply((BigInteger) vector.elementAt(i2)));
        }
        return bigInteger.mod(bigInteger2);
    }

    public byte[] addCryptedBlocks(byte[] bArr, byte[] bArr2) throws InvalidCipherTextException {
        if (this.forEncryption) {
            if (bArr.length > getOutputBlockSize() || bArr2.length > getOutputBlockSize()) {
                throw new InvalidCipherTextException("BlockLength too large for simple addition.\n");
            }
        } else if (bArr.length > getInputBlockSize() || bArr2.length > getInputBlockSize()) {
            throw new InvalidCipherTextException("BlockLength too large for simple addition.\n");
        }
        BigInteger bigInteger = new BigInteger(1, bArr);
        BigInteger bigInteger2 = new BigInteger(1, bArr2);
        BigInteger mod = bigInteger.multiply(bigInteger2).mod(this.key.getModulus());
        if (this.debug) {
            PrintStream printStream = System.out;
            printStream.println("c(m1) as BigInteger:....... " + bigInteger);
            PrintStream printStream2 = System.out;
            printStream2.println("c(m2) as BigInteger:....... " + bigInteger2);
            PrintStream printStream3 = System.out;
            printStream3.println("c(m1)*c(m2)%n = c(m1+m2)%n: " + mod);
        }
        byte[] byteArray = this.key.getModulus().toByteArray();
        Arrays.fill(byteArray, (byte) 0);
        System.arraycopy(mod.toByteArray(), 0, byteArray, byteArray.length - mod.toByteArray().length, mod.toByteArray().length);
        return byteArray;
    }

    public byte[] encrypt(BigInteger bigInteger) {
        byte[] byteArray = this.key.getModulus().toByteArray();
        Arrays.fill(byteArray, (byte) 0);
        byte[] byteArray2 = this.key.getG().modPow(bigInteger, this.key.getModulus()).toByteArray();
        System.arraycopy(byteArray2, 0, byteArray, byteArray.length - byteArray2.length, byteArray2.length);
        if (this.debug) {
            PrintStream printStream = System.out;
            printStream.println("Encrypted value is:  " + new BigInteger(byteArray));
        }
        return byteArray;
    }

    @Override // org.bouncycastle.crypto.AsymmetricBlockCipher
    public int getInputBlockSize() {
        return this.forEncryption ? ((this.key.getLowerSigmaBound() + 7) / 8) - 1 : this.key.getModulus().toByteArray().length;
    }

    @Override // org.bouncycastle.crypto.AsymmetricBlockCipher
    public int getOutputBlockSize() {
        return this.forEncryption ? this.key.getModulus().toByteArray().length : ((this.key.getLowerSigmaBound() + 7) / 8) - 1;
    }

    @Override // org.bouncycastle.crypto.AsymmetricBlockCipher
    public void init(boolean z, CipherParameters cipherParameters) {
        this.forEncryption = z;
        if (cipherParameters instanceof ParametersWithRandom) {
            cipherParameters = ((ParametersWithRandom) cipherParameters).getParameters();
        }
        this.key = (NaccacheSternKeyParameters) cipherParameters;
        if (!this.forEncryption) {
            if (this.debug) {
                System.out.println("Constructing lookup Array");
            }
            NaccacheSternPrivateKeyParameters naccacheSternPrivateKeyParameters = (NaccacheSternPrivateKeyParameters) this.key;
            Vector smallPrimes = naccacheSternPrivateKeyParameters.getSmallPrimes();
            this.lookup = new Vector[smallPrimes.size()];
            for (int i = 0; i < smallPrimes.size(); i++) {
                BigInteger bigInteger = (BigInteger) smallPrimes.elementAt(i);
                int intValue = bigInteger.intValue();
                this.lookup[i] = new Vector();
                this.lookup[i].addElement(ONE);
                if (this.debug) {
                    PrintStream printStream = System.out;
                    printStream.println("Constructing lookup ArrayList for " + intValue);
                }
                BigInteger bigInteger2 = ZERO;
                for (int i2 = 1; i2 < intValue; i2++) {
                    bigInteger2 = bigInteger2.add(naccacheSternPrivateKeyParameters.getPhi_n());
                    this.lookup[i].addElement(naccacheSternPrivateKeyParameters.getG().modPow(bigInteger2.divide(bigInteger), naccacheSternPrivateKeyParameters.getModulus()));
                }
            }
        }
    }

    @Override // org.bouncycastle.crypto.AsymmetricBlockCipher
    public byte[] processBlock(byte[] bArr, int i, int i2) throws InvalidCipherTextException {
        if (this.key == null) {
            throw new IllegalStateException("NaccacheStern engine not initialised");
        } else if (i2 > getInputBlockSize() + 1) {
            throw new DataLengthException("input too large for Naccache-Stern cipher.\n");
        } else if (this.forEncryption || i2 >= getInputBlockSize()) {
            if (!(i == 0 && i2 == bArr.length)) {
                byte[] bArr2 = new byte[i2];
                System.arraycopy(bArr, i, bArr2, 0, i2);
                bArr = bArr2;
            }
            BigInteger bigInteger = new BigInteger(1, bArr);
            if (this.debug) {
                System.out.println("input as BigInteger: " + bigInteger);
            }
            if (this.forEncryption) {
                return encrypt(bigInteger);
            }
            Vector vector = new Vector();
            NaccacheSternPrivateKeyParameters naccacheSternPrivateKeyParameters = (NaccacheSternPrivateKeyParameters) this.key;
            Vector smallPrimes = naccacheSternPrivateKeyParameters.getSmallPrimes();
            for (int i3 = 0; i3 < smallPrimes.size(); i3++) {
                BigInteger modPow = bigInteger.modPow(naccacheSternPrivateKeyParameters.getPhi_n().divide((BigInteger) smallPrimes.elementAt(i3)), naccacheSternPrivateKeyParameters.getModulus());
                Vector[] vectorArr = this.lookup;
                Vector vector2 = vectorArr[i3];
                if (vectorArr[i3].size() != ((BigInteger) smallPrimes.elementAt(i3)).intValue()) {
                    if (this.debug) {
                        System.out.println("Prime is " + smallPrimes.elementAt(i3) + ", lookup table has size " + vector2.size());
                    }
                    throw new InvalidCipherTextException("Error in lookup Array for " + ((BigInteger) smallPrimes.elementAt(i3)).intValue() + ": Size mismatch. Expected ArrayList with length " + ((BigInteger) smallPrimes.elementAt(i3)).intValue() + " but found ArrayList of length " + this.lookup[i3].size());
                }
                int indexOf = vector2.indexOf(modPow);
                if (indexOf == -1) {
                    if (this.debug) {
                        System.out.println("Actual prime is " + smallPrimes.elementAt(i3));
                        System.out.println("Decrypted value is " + modPow);
                        System.out.println("LookupList for " + smallPrimes.elementAt(i3) + " with size " + this.lookup[i3].size() + " is: ");
                        for (int i4 = 0; i4 < this.lookup[i3].size(); i4++) {
                            System.out.println(this.lookup[i3].elementAt(i4));
                        }
                    }
                    throw new InvalidCipherTextException("Lookup failed");
                }
                vector.addElement(BigInteger.valueOf((long) indexOf));
            }
            return chineseRemainder(vector, smallPrimes).toByteArray();
        } else {
            throw new InvalidCipherTextException("BlockLength does not match modulus for Naccache-Stern cipher.\n");
        }
    }

    public byte[] processData(byte[] bArr) throws InvalidCipherTextException {
        byte[] bArr2;
        if (this.debug) {
            System.out.println();
        }
        if (bArr.length > getInputBlockSize()) {
            int inputBlockSize = getInputBlockSize();
            int outputBlockSize = getOutputBlockSize();
            if (this.debug) {
                System.out.println("Input blocksize is:  " + inputBlockSize + " bytes");
                System.out.println("Output blocksize is: " + outputBlockSize + " bytes");
                System.out.println("Data has length:.... " + bArr.length + " bytes");
            }
            byte[] bArr3 = new byte[(((bArr.length / inputBlockSize) + 1) * outputBlockSize)];
            int i = 0;
            int i2 = 0;
            while (i < bArr.length) {
                int i3 = i + inputBlockSize;
                if (i3 < bArr.length) {
                    bArr2 = processBlock(bArr, i, inputBlockSize);
                    i = i3;
                } else {
                    bArr2 = processBlock(bArr, i, bArr.length - i);
                    i += bArr.length - i;
                }
                if (this.debug) {
                    System.out.println("new datapos is " + i);
                }
                if (bArr2 != null) {
                    System.arraycopy(bArr2, 0, bArr3, i2, bArr2.length);
                    i2 += bArr2.length;
                } else {
                    if (this.debug) {
                        System.out.println("cipher returned null");
                    }
                    throw new InvalidCipherTextException("cipher returned null");
                }
            }
            byte[] bArr4 = new byte[i2];
            System.arraycopy(bArr3, 0, bArr4, 0, i2);
            if (this.debug) {
                System.out.println("returning " + bArr4.length + " bytes");
            }
            return bArr4;
        }
        if (this.debug) {
            System.out.println("data size is less then input block size, processing directly");
        }
        return processBlock(bArr, 0, bArr.length);
    }

    public void setDebug(boolean z) {
        this.debug = z;
    }
}
