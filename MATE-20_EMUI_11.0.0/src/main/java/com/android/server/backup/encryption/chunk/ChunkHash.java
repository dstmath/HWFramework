package com.android.server.backup.encryption.chunk;

import com.android.internal.util.Preconditions;
import java.util.Arrays;
import java.util.Base64;

public class ChunkHash implements Comparable<ChunkHash> {
    public static final int HASH_LENGTH_BYTES = 32;
    private static final int UNSIGNED_MASK = 255;
    private final byte[] mHash;

    public ChunkHash(byte[] hash) {
        Preconditions.checkArgument(hash.length == 32, "Hash must have 256 bits");
        this.mHash = hash;
    }

    public byte[] getHash() {
        return this.mHash;
    }

    @Override // java.lang.Object
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChunkHash)) {
            return false;
        }
        return Arrays.equals(this.mHash, ((ChunkHash) o).mHash);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Arrays.hashCode(this.mHash);
    }

    public int compareTo(ChunkHash other) {
        return lexicographicalCompareUnsignedBytes(getHash(), other.getHash());
    }

    @Override // java.lang.Object
    public String toString() {
        return Base64.getEncoder().encodeToString(this.mHash);
    }

    private static int lexicographicalCompareUnsignedBytes(byte[] left, byte[] right) {
        int minLength = Math.min(left.length, right.length);
        for (int i = 0; i < minLength; i++) {
            int result = toInt(left[i]) - toInt(right[i]);
            if (result != 0) {
                return result;
            }
        }
        return left.length - right.length;
    }

    private static int toInt(byte value) {
        return value & 255;
    }
}
