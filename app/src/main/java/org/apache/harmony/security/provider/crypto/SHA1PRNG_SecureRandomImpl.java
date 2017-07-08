package org.apache.harmony.security.provider.crypto;

import dalvik.system.BlockGuard;
import dalvik.system.BlockGuard.Policy;
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
    private static final int[] END_FLAGS = null;
    private static final int EXTRAFRAME_OFFSET = 5;
    private static final int FRAME_LENGTH = 16;
    private static final int FRAME_OFFSET = 21;
    private static final int HASHBYTES_TO_USE = 20;
    private static final int HASHCOPY_OFFSET = 0;
    private static final int[] LEFT = null;
    private static final int[] MASK = null;
    private static final int MAX_BYTES = 48;
    private static final int NEXT_BYTES = 2;
    private static final int[] RIGHT1 = null;
    private static final int[] RIGHT2 = null;
    private static final int SET_SEED = 1;
    private static final int UNDEFINED = 0;
    private static FileInputStream devURandom = null;
    private static SHA1PRNG_SecureRandomImpl myRandom = null;
    private static final long serialVersionUID = 283736797212159675L;
    private transient int[] copies;
    private transient long counter;
    private transient int nextBIndex;
    private transient byte[] nextBytes;
    private transient int[] seed;
    private transient long seedLength;
    private transient int state;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl.<clinit>():void");
    }

    public SHA1PRNG_SecureRandomImpl() {
        this.seed = new int[87];
        this.seed[82] = SHA1Constants.H0;
        this.seed[83] = SHA1Constants.H1;
        this.seed[84] = SHA1Constants.H2;
        this.seed[85] = SHA1Constants.H3;
        this.seed[86] = SHA1Constants.H4;
        this.seedLength = 0;
        this.copies = new int[37];
        this.nextBytes = new byte[HASHBYTES_TO_USE];
        this.nextBIndex = HASHBYTES_TO_USE;
        this.counter = 0;
        this.state = UNDEFINED;
    }

    private void updateSeed(byte[] bytes) {
        SHA1Impl.updateHash(this.seed, bytes, UNDEFINED, bytes.length - 1);
        this.seedLength += (long) bytes.length;
    }

    protected synchronized void engineSetSeed(byte[] seed) {
        if (seed == null) {
            throw new NullPointerException("seed == null");
        }
        if (this.state == NEXT_BYTES) {
            System.arraycopy(this.copies, UNDEFINED, this.seed, 82, EXTRAFRAME_OFFSET);
        }
        this.state = SET_SEED;
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
                myRandom.engineSetSeed(getRandomBytes(HASHBYTES_TO_USE));
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
            lastWord = UNDEFINED;
        } else {
            lastWord = (this.seed[81] + 7) >> NEXT_BYTES;
        }
        if (this.state == 0) {
            updateSeed(getRandomBytes(HASHBYTES_TO_USE));
            this.nextBIndex = HASHBYTES_TO_USE;
            if (this.seed[81] == 0) {
                lastWord = UNDEFINED;
            } else {
                lastWord = (this.seed[81] + 7) >> NEXT_BYTES;
            }
        } else if (this.state == SET_SEED) {
            System.arraycopy(this.seed, 82, this.copies, UNDEFINED, EXTRAFRAME_OFFSET);
            for (i = lastWord + 3; i < 18; i += SET_SEED) {
                this.seed[i] = UNDEFINED;
            }
            long bits = (this.seedLength << 3) + 64;
            if (this.seed[81] < MAX_BYTES) {
                this.seed[14] = (int) (bits >>> 32);
                this.seed[15] = (int) (-1 & bits);
            } else {
                this.copies[19] = (int) (bits >>> 32);
                this.copies[HASHBYTES_TO_USE] = (int) (-1 & bits);
            }
            this.nextBIndex = HASHBYTES_TO_USE;
        }
        this.state = NEXT_BYTES;
        if (bytes.length != 0) {
            int n;
            int nextByteToReturn = UNDEFINED;
            if (20 - this.nextBIndex < bytes.length + UNDEFINED) {
                n = 20 - this.nextBIndex;
            } else {
                n = bytes.length + UNDEFINED;
            }
            if (n > 0) {
                System.arraycopy(this.nextBytes, this.nextBIndex, bytes, UNDEFINED, n);
                this.nextBIndex += n;
                nextByteToReturn = n + UNDEFINED;
            }
            if (nextByteToReturn < bytes.length) {
                n = this.seed[81] & 3;
                do {
                    if (n == 0) {
                        this.seed[lastWord] = (int) (this.counter >>> 32);
                        this.seed[lastWord + SET_SEED] = (int) (this.counter & -1);
                        this.seed[lastWord + NEXT_BYTES] = END_FLAGS[UNDEFINED];
                    } else {
                        int[] iArr = this.seed;
                        iArr[lastWord] = iArr[lastWord] | ((int) ((this.counter >>> RIGHT1[n]) & ((long) MASK[n])));
                        this.seed[lastWord + SET_SEED] = (int) ((this.counter >>> RIGHT2[n]) & -1);
                        this.seed[lastWord + NEXT_BYTES] = (int) ((this.counter << LEFT[n]) | ((long) END_FLAGS[n]));
                    }
                    if (this.seed[81] > MAX_BYTES) {
                        this.copies[EXTRAFRAME_OFFSET] = this.seed[FRAME_LENGTH];
                        this.copies[6] = this.seed[17];
                    }
                    SHA1Impl.computeHash(this.seed);
                    if (this.seed[81] > MAX_BYTES) {
                        System.arraycopy(this.seed, UNDEFINED, this.copies, FRAME_OFFSET, FRAME_LENGTH);
                        System.arraycopy(this.copies, EXTRAFRAME_OFFSET, this.seed, UNDEFINED, FRAME_LENGTH);
                        SHA1Impl.computeHash(this.seed);
                        System.arraycopy(this.copies, FRAME_OFFSET, this.seed, UNDEFINED, FRAME_LENGTH);
                    }
                    this.counter++;
                    int j = UNDEFINED;
                    for (i = UNDEFINED; i < EXTRAFRAME_OFFSET; i += SET_SEED) {
                        int k = this.seed[i + 82];
                        this.nextBytes[j] = (byte) (k >>> 24);
                        this.nextBytes[j + SET_SEED] = (byte) (k >>> FRAME_LENGTH);
                        this.nextBytes[j + NEXT_BYTES] = (byte) (k >>> 8);
                        this.nextBytes[j + 3] = (byte) k;
                        j += 4;
                    }
                    this.nextBIndex = UNDEFINED;
                    if (HASHBYTES_TO_USE < bytes.length - nextByteToReturn) {
                        j = HASHBYTES_TO_USE;
                    } else {
                        j = bytes.length - nextByteToReturn;
                    }
                    if (j > 0) {
                        System.arraycopy(this.nextBytes, UNDEFINED, bytes, nextByteToReturn, j);
                        nextByteToReturn += j;
                        this.nextBIndex += j;
                    }
                } while (nextByteToReturn < bytes.length);
                return;
            }
            return;
        }
        return;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        int[] intData;
        oos.writeLong(this.seedLength);
        oos.writeLong(this.counter);
        oos.writeInt(this.state);
        oos.writeInt(this.seed[81]);
        int nRemaining = (this.seed[81] + 3) >> NEXT_BYTES;
        if (this.state != NEXT_BYTES) {
            intData = new int[(nRemaining + EXTRAFRAME_OFFSET)];
            System.arraycopy(this.seed, UNDEFINED, intData, UNDEFINED, nRemaining);
            System.arraycopy(this.seed, 82, intData, nRemaining, EXTRAFRAME_OFFSET);
        } else {
            int offset = UNDEFINED;
            if (this.seed[81] < MAX_BYTES) {
                intData = new int[(nRemaining + 26)];
            } else {
                intData = new int[(nRemaining + 42)];
                intData[UNDEFINED] = this.seed[FRAME_LENGTH];
                intData[SET_SEED] = this.seed[17];
                intData[NEXT_BYTES] = this.seed[30];
                intData[3] = this.seed[31];
                offset = 4;
            }
            System.arraycopy(this.seed, UNDEFINED, intData, offset, FRAME_LENGTH);
            offset += FRAME_LENGTH;
            System.arraycopy(this.copies, FRAME_OFFSET, intData, offset, nRemaining);
            offset += nRemaining;
            System.arraycopy(this.copies, UNDEFINED, intData, offset, EXTRAFRAME_OFFSET);
            System.arraycopy(this.seed, 82, intData, offset + EXTRAFRAME_OFFSET, EXTRAFRAME_OFFSET);
        }
        for (int i = UNDEFINED; i < intData.length; i += SET_SEED) {
            oos.writeInt(intData[i]);
        }
        oos.writeInt(this.nextBIndex);
        oos.write(this.nextBytes, this.nextBIndex, 20 - this.nextBIndex);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.seed = new int[87];
        this.copies = new int[37];
        this.nextBytes = new byte[HASHBYTES_TO_USE];
        this.seedLength = ois.readLong();
        this.counter = ois.readLong();
        this.state = ois.readInt();
        this.seed[81] = ois.readInt();
        int nRemaining = (this.seed[81] + 3) >> NEXT_BYTES;
        int i;
        if (this.state != NEXT_BYTES) {
            for (i = UNDEFINED; i < nRemaining; i += SET_SEED) {
                this.seed[i] = ois.readInt();
            }
            for (i = UNDEFINED; i < EXTRAFRAME_OFFSET; i += SET_SEED) {
                this.seed[i + 82] = ois.readInt();
            }
        } else {
            if (this.seed[81] >= MAX_BYTES) {
                this.seed[FRAME_LENGTH] = ois.readInt();
                this.seed[17] = ois.readInt();
                this.seed[30] = ois.readInt();
                this.seed[31] = ois.readInt();
            }
            for (i = UNDEFINED; i < FRAME_LENGTH; i += SET_SEED) {
                this.seed[i] = ois.readInt();
            }
            for (i = UNDEFINED; i < nRemaining; i += SET_SEED) {
                this.copies[i + FRAME_OFFSET] = ois.readInt();
            }
            for (i = UNDEFINED; i < EXTRAFRAME_OFFSET; i += SET_SEED) {
                this.copies[i] = ois.readInt();
            }
            for (i = UNDEFINED; i < EXTRAFRAME_OFFSET; i += SET_SEED) {
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
            Streams.readFully(devURandom, result, UNDEFINED, byteCount);
            BlockGuard.setThreadPolicy(originalPolicy);
            return result;
        } catch (Exception ex) {
            throw new ProviderException("Couldn't read " + byteCount + " random bytes", ex);
        } catch (Throwable th) {
            BlockGuard.setThreadPolicy(originalPolicy);
        }
    }
}
