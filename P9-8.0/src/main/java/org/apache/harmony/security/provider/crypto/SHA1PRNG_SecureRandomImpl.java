package org.apache.harmony.security.provider.crypto;

import android.icu.impl.UCharacterProperty;
import android.icu.text.DateTimePatternGenerator;
import dalvik.system.BlockGuard;
import dalvik.system.BlockGuard.Policy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.ProviderException;
import java.security.SecureRandomSpi;
import libcore.io.Streams;
import libcore.util.EmptyArray;

public class SHA1PRNG_SecureRandomImpl extends SecureRandomSpi implements Serializable {
    private static final int COUNTER_BASE = 0;
    private static final int[] END_FLAGS = new int[]{Integer.MIN_VALUE, UCharacterProperty.SCRIPT_X_WITH_INHERITED, 32768, 128};
    private static final int EXTRAFRAME_OFFSET = 5;
    private static final int FRAME_LENGTH = 16;
    private static final int FRAME_OFFSET = 21;
    private static final int HASHBYTES_TO_USE = 20;
    private static final int HASHCOPY_OFFSET = 0;
    private static final int[] LEFT = new int[]{0, 24, 16, 8};
    private static final int[] MASK = new int[]{-1, 16777215, DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH, 255};
    private static final int MAX_BYTES = 48;
    private static final int NEXT_BYTES = 2;
    private static final int[] RIGHT1 = new int[]{0, 40, 48, 56};
    private static final int[] RIGHT2 = new int[]{0, 8, 16, 24};
    private static final int SET_SEED = 1;
    private static final int UNDEFINED = 0;
    private static FileInputStream devURandom = null;
    private static SHA1PRNG_SecureRandomImpl myRandom = null;
    private static final long serialVersionUID = 283736797212159675L;
    private transient int[] copies;
    private transient long counter;
    private transient int nextBIndex;
    private transient byte[] nextBytes;
    private transient int[] seed = new int[87];
    private transient long seedLength;
    private transient int state;

    static {
        try {
            devURandom = new FileInputStream(new File("/dev/urandom"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public SHA1PRNG_SecureRandomImpl() {
        this.seed[82] = SHA1Constants.H0;
        this.seed[83] = SHA1Constants.H1;
        this.seed[84] = SHA1Constants.H2;
        this.seed[85] = SHA1Constants.H3;
        this.seed[86] = SHA1Constants.H4;
        this.seedLength = 0;
        this.copies = new int[37];
        this.nextBytes = new byte[20];
        this.nextBIndex = 20;
        this.counter = 0;
        this.state = 0;
    }

    private void updateSeed(byte[] bytes) {
        SHA1Impl.updateHash(this.seed, bytes, 0, bytes.length - 1);
        this.seedLength += (long) bytes.length;
    }

    protected synchronized void engineSetSeed(byte[] seed) {
        if (seed == null) {
            throw new NullPointerException("seed == null");
        }
        if (this.state == 2) {
            System.arraycopy(this.copies, 0, this.seed, 82, 5);
        }
        this.state = 1;
        if (seed.length != 0) {
            updateSeed(seed);
        }
    }

    protected synchronized byte[] engineGenerateSeed(int numBytes) {
        if (numBytes < 0) {
            throw new NegativeArraySizeException(Integer.toString(numBytes));
        } else if (numBytes == 0) {
            return EmptyArray.BYTE;
        } else {
            if (myRandom == null) {
                myRandom = new SHA1PRNG_SecureRandomImpl();
                myRandom.engineSetSeed(getRandomBytes(20));
            }
            byte[] myBytes = new byte[numBytes];
            myRandom.engineNextBytes(myBytes);
            return myBytes;
        }
    }

    protected synchronized void engineNextBytes(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes == null");
        }
        int lastWord;
        int i;
        if (this.seed[81] == 0) {
            lastWord = 0;
        } else {
            lastWord = (this.seed[81] + 7) >> 2;
        }
        if (this.state == 0) {
            updateSeed(getRandomBytes(20));
            this.nextBIndex = 20;
            if (this.seed[81] == 0) {
                lastWord = 0;
            } else {
                lastWord = (this.seed[81] + 7) >> 2;
            }
        } else if (this.state == 1) {
            System.arraycopy(this.seed, 82, this.copies, 0, 5);
            for (i = lastWord + 3; i < 18; i++) {
                this.seed[i] = 0;
            }
            long bits = (this.seedLength << 3) + 64;
            if (this.seed[81] < 48) {
                this.seed[14] = (int) (bits >>> 32);
                this.seed[15] = (int) (-1 & bits);
            } else {
                this.copies[19] = (int) (bits >>> 32);
                this.copies[20] = (int) (-1 & bits);
            }
            this.nextBIndex = 20;
        }
        this.state = 2;
        if (bytes.length != 0) {
            int n;
            int nextByteToReturn = 0;
            if (20 - this.nextBIndex < bytes.length + 0) {
                n = 20 - this.nextBIndex;
            } else {
                n = bytes.length + 0;
            }
            if (n > 0) {
                System.arraycopy(this.nextBytes, this.nextBIndex, bytes, 0, n);
                this.nextBIndex += n;
                nextByteToReturn = n + 0;
            }
            if (nextByteToReturn < bytes.length) {
                n = this.seed[81] & 3;
                do {
                    if (n == 0) {
                        this.seed[lastWord] = (int) (this.counter >>> 32);
                        this.seed[lastWord + 1] = (int) (this.counter & -1);
                        this.seed[lastWord + 2] = END_FLAGS[0];
                    } else {
                        int[] iArr = this.seed;
                        iArr[lastWord] = iArr[lastWord] | ((int) ((this.counter >>> RIGHT1[n]) & ((long) MASK[n])));
                        this.seed[lastWord + 1] = (int) ((this.counter >>> RIGHT2[n]) & -1);
                        this.seed[lastWord + 2] = (int) ((this.counter << LEFT[n]) | ((long) END_FLAGS[n]));
                    }
                    if (this.seed[81] > 48) {
                        this.copies[5] = this.seed[16];
                        this.copies[6] = this.seed[17];
                    }
                    SHA1Impl.computeHash(this.seed);
                    if (this.seed[81] > 48) {
                        System.arraycopy(this.seed, 0, this.copies, 21, 16);
                        System.arraycopy(this.copies, 5, this.seed, 0, 16);
                        SHA1Impl.computeHash(this.seed);
                        System.arraycopy(this.copies, 21, this.seed, 0, 16);
                    }
                    this.counter++;
                    int j = 0;
                    for (i = 0; i < 5; i++) {
                        int k = this.seed[i + 82];
                        this.nextBytes[j] = (byte) (k >>> 24);
                        this.nextBytes[j + 1] = (byte) (k >>> 16);
                        this.nextBytes[j + 2] = (byte) (k >>> 8);
                        this.nextBytes[j + 3] = (byte) k;
                        j += 4;
                    }
                    this.nextBIndex = 0;
                    if (20 < bytes.length - nextByteToReturn) {
                        j = 20;
                    } else {
                        j = bytes.length - nextByteToReturn;
                    }
                    if (j > 0) {
                        System.arraycopy(this.nextBytes, 0, bytes, nextByteToReturn, j);
                        nextByteToReturn += j;
                        this.nextBIndex += j;
                    }
                } while (nextByteToReturn < bytes.length);
            }
        }
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        int[] intData;
        oos.writeLong(this.seedLength);
        oos.writeLong(this.counter);
        oos.writeInt(this.state);
        oos.writeInt(this.seed[81]);
        int nRemaining = (this.seed[81] + 3) >> 2;
        if (this.state != 2) {
            intData = new int[(nRemaining + 5)];
            System.arraycopy(this.seed, 0, intData, 0, nRemaining);
            System.arraycopy(this.seed, 82, intData, nRemaining, 5);
        } else {
            int offset = 0;
            if (this.seed[81] < 48) {
                intData = new int[(nRemaining + 26)];
            } else {
                intData = new int[(nRemaining + 42)];
                intData[0] = this.seed[16];
                intData[1] = this.seed[17];
                intData[2] = this.seed[30];
                intData[3] = this.seed[31];
                offset = 4;
            }
            System.arraycopy(this.seed, 0, intData, offset, 16);
            offset += 16;
            System.arraycopy(this.copies, 21, intData, offset, nRemaining);
            offset += nRemaining;
            System.arraycopy(this.copies, 0, intData, offset, 5);
            System.arraycopy(this.seed, 82, intData, offset + 5, 5);
        }
        for (int writeInt : intData) {
            oos.writeInt(writeInt);
        }
        oos.writeInt(this.nextBIndex);
        oos.write(this.nextBytes, this.nextBIndex, 20 - this.nextBIndex);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.seed = new int[87];
        this.copies = new int[37];
        this.nextBytes = new byte[20];
        this.seedLength = ois.readLong();
        this.counter = ois.readLong();
        this.state = ois.readInt();
        this.seed[81] = ois.readInt();
        int nRemaining = (this.seed[81] + 3) >> 2;
        int i;
        if (this.state != 2) {
            for (i = 0; i < nRemaining; i++) {
                this.seed[i] = ois.readInt();
            }
            for (i = 0; i < 5; i++) {
                this.seed[i + 82] = ois.readInt();
            }
        } else {
            if (this.seed[81] >= 48) {
                this.seed[16] = ois.readInt();
                this.seed[17] = ois.readInt();
                this.seed[30] = ois.readInt();
                this.seed[31] = ois.readInt();
            }
            for (i = 0; i < 16; i++) {
                this.seed[i] = ois.readInt();
            }
            for (i = 0; i < nRemaining; i++) {
                this.copies[i + 21] = ois.readInt();
            }
            for (i = 0; i < 5; i++) {
                this.copies[i] = ois.readInt();
            }
            for (i = 0; i < 5; i++) {
                this.seed[i + 82] = ois.readInt();
            }
        }
        this.nextBIndex = ois.readInt();
        Streams.readFully(ois, this.nextBytes, this.nextBIndex, 20 - this.nextBIndex);
    }

    private static byte[] getRandomBytes(int byteCount) {
        if (byteCount <= 0) {
            throw new IllegalArgumentException("Too few bytes requested: " + byteCount);
        }
        Policy originalPolicy = BlockGuard.getThreadPolicy();
        try {
            BlockGuard.setThreadPolicy(BlockGuard.LAX_POLICY);
            byte[] result = new byte[byteCount];
            Streams.readFully(devURandom, result, 0, byteCount);
            BlockGuard.setThreadPolicy(originalPolicy);
            return result;
        } catch (Exception ex) {
            throw new ProviderException("Couldn't read " + byteCount + " random bytes", ex);
        } catch (Throwable th) {
            BlockGuard.setThreadPolicy(originalPolicy);
        }
    }
}
