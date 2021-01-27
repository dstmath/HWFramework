package org.bouncycastle.pqc.crypto.xmss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

public class XMSSUtil {

    /* access modifiers changed from: private */
    public static class CheckingStream extends ObjectInputStream {
        private static final Set components = new HashSet();
        private boolean found = false;
        private final Class mainClass;

        static {
            components.add("java.util.TreeMap");
            components.add("java.lang.Integer");
            components.add("java.lang.Number");
            components.add("org.bouncycastle.pqc.crypto.xmss.BDS");
            components.add("java.util.ArrayList");
            components.add("org.bouncycastle.pqc.crypto.xmss.XMSSNode");
            components.add("[B");
            components.add("java.util.LinkedList");
            components.add("java.util.Stack");
            components.add("java.util.Vector");
            components.add("[Ljava.lang.Object;");
            components.add("org.bouncycastle.pqc.crypto.xmss.BDSTreeHash");
        }

        CheckingStream(Class cls, InputStream inputStream) throws IOException {
            super(inputStream);
            this.mainClass = cls;
        }

        /* access modifiers changed from: protected */
        @Override // java.io.ObjectInputStream
        public Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
            if (!this.found) {
                if (objectStreamClass.getName().equals(this.mainClass.getName())) {
                    this.found = true;
                } else {
                    throw new InvalidClassException("unexpected class: ", objectStreamClass.getName());
                }
            } else if (!components.contains(objectStreamClass.getName())) {
                throw new InvalidClassException("unexpected class: ", objectStreamClass.getName());
            }
            return super.resolveClass(objectStreamClass);
        }
    }

    public static boolean areEqual(byte[][] bArr, byte[][] bArr2) {
        if (hasNullPointer(bArr) || hasNullPointer(bArr2)) {
            throw new NullPointerException("a or b == null");
        }
        for (int i = 0; i < bArr.length; i++) {
            if (!Arrays.areEqual(bArr[i], bArr2[i])) {
                return false;
            }
        }
        return true;
    }

    public static long bytesToXBigEndian(byte[] bArr, int i, int i2) {
        if (bArr != null) {
            long j = 0;
            for (int i3 = i; i3 < i + i2; i3++) {
                j = (j << 8) | ((long) (bArr[i3] & 255));
            }
            return j;
        }
        throw new NullPointerException("in == null");
    }

    public static int calculateTau(int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            if (((i >> i3) & 1) == 0) {
                return i3;
            }
        }
        return 0;
    }

    public static byte[] cloneArray(byte[] bArr) {
        if (bArr != null) {
            byte[] bArr2 = new byte[bArr.length];
            System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
            return bArr2;
        }
        throw new NullPointerException("in == null");
    }

    public static byte[][] cloneArray(byte[][] bArr) {
        if (!hasNullPointer(bArr)) {
            byte[][] bArr2 = new byte[bArr.length][];
            for (int i = 0; i < bArr.length; i++) {
                bArr2[i] = new byte[bArr[i].length];
                System.arraycopy(bArr[i], 0, bArr2[i], 0, bArr[i].length);
            }
            return bArr2;
        }
        throw new NullPointerException("in has null pointers");
    }

    public static void copyBytesAtOffset(byte[] bArr, byte[] bArr2, int i) {
        if (bArr == null) {
            throw new NullPointerException("dst == null");
        } else if (bArr2 == null) {
            throw new NullPointerException("src == null");
        } else if (i < 0) {
            throw new IllegalArgumentException("offset hast to be >= 0");
        } else if (bArr2.length + i <= bArr.length) {
            for (int i2 = 0; i2 < bArr2.length; i2++) {
                bArr[i + i2] = bArr2[i2];
            }
        } else {
            throw new IllegalArgumentException("src length + offset must not be greater than size of destination");
        }
    }

    public static Object deserialize(byte[] bArr, Class cls) throws IOException, ClassNotFoundException {
        CheckingStream checkingStream = new CheckingStream(cls, new ByteArrayInputStream(bArr));
        Object readObject = checkingStream.readObject();
        if (checkingStream.available() != 0) {
            throw new IOException("unexpected data found at end of ObjectInputStream");
        } else if (cls.isInstance(readObject)) {
            return readObject;
        } else {
            throw new IOException("unexpected class found in ObjectInputStream");
        }
    }

    public static void dumpByteArray(byte[][] bArr) {
        if (!hasNullPointer(bArr)) {
            for (byte[] bArr2 : bArr) {
                System.out.println(Hex.toHexString(bArr2));
            }
            return;
        }
        throw new NullPointerException("x has null pointers");
    }

    public static byte[] extractBytesAtOffset(byte[] bArr, int i, int i2) {
        if (bArr == null) {
            throw new NullPointerException("src == null");
        } else if (i < 0) {
            throw new IllegalArgumentException("offset hast to be >= 0");
        } else if (i2 < 0) {
            throw new IllegalArgumentException("length hast to be >= 0");
        } else if (i + i2 <= bArr.length) {
            byte[] bArr2 = new byte[i2];
            for (int i3 = 0; i3 < bArr2.length; i3++) {
                bArr2[i3] = bArr[i + i3];
            }
            return bArr2;
        } else {
            throw new IllegalArgumentException("offset + length must not be greater then size of source array");
        }
    }

    public static int getDigestSize(Digest digest) {
        if (digest != null) {
            String algorithmName = digest.getAlgorithmName();
            if (algorithmName.equals("SHAKE128")) {
                return 32;
            }
            if (algorithmName.equals("SHAKE256")) {
                return 64;
            }
            return digest.getDigestSize();
        }
        throw new NullPointerException("digest == null");
    }

    public static int getLeafIndex(long j, int i) {
        return (int) (j & ((1 << i) - 1));
    }

    public static long getTreeIndex(long j, int i) {
        return j >> i;
    }

    public static boolean hasNullPointer(byte[][] bArr) {
        if (bArr == null) {
            return true;
        }
        for (byte[] bArr2 : bArr) {
            if (bArr2 == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIndexValid(int i, long j) {
        if (j >= 0) {
            return j < (1 << i);
        }
        throw new IllegalStateException("index must not be negative");
    }

    public static boolean isNewAuthenticationPathNeeded(long j, int i, int i2) {
        if (j == 0) {
            return false;
        }
        return (j + 1) % ((long) Math.pow((double) (1 << i), (double) i2)) == 0;
    }

    public static boolean isNewBDSInitNeeded(long j, int i, int i2) {
        if (j == 0) {
            return false;
        }
        return j % ((long) Math.pow((double) (1 << i), (double) (i2 + 1))) == 0;
    }

    public static int log2(int i) {
        int i2 = 0;
        while (true) {
            i >>= 1;
            if (i == 0) {
                return i2;
            }
            i2++;
        }
    }

    public static void longToBigEndian(long j, byte[] bArr, int i) {
        if (bArr == null) {
            throw new NullPointerException("in == null");
        } else if (bArr.length - i >= 8) {
            bArr[i] = (byte) ((int) ((j >> 56) & 255));
            bArr[i + 1] = (byte) ((int) ((j >> 48) & 255));
            bArr[i + 2] = (byte) ((int) ((j >> 40) & 255));
            bArr[i + 3] = (byte) ((int) ((j >> 32) & 255));
            bArr[i + 4] = (byte) ((int) ((j >> 24) & 255));
            bArr[i + 5] = (byte) ((int) ((j >> 16) & 255));
            bArr[i + 6] = (byte) ((int) ((j >> 8) & 255));
            bArr[i + 7] = (byte) ((int) (j & 255));
        } else {
            throw new IllegalArgumentException("not enough space in array");
        }
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] toBytesBigEndian(long j, int i) {
        byte[] bArr = new byte[i];
        for (int i2 = i - 1; i2 >= 0; i2--) {
            bArr[i2] = (byte) ((int) j);
            j >>>= 8;
        }
        return bArr;
    }
}
