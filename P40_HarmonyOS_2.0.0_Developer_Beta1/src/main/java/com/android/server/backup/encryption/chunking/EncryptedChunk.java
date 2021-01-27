package com.android.server.backup.encryption.chunking;

import com.android.internal.util.Preconditions;
import com.android.server.backup.encryption.chunk.ChunkHash;
import java.util.Arrays;
import java.util.Objects;

public class EncryptedChunk {
    public static final int KEY_LENGTH_BYTES = 32;
    public static final int NONCE_LENGTH_BYTES = 12;
    private byte[] mEncryptedBytes;
    private ChunkHash mKey;
    private byte[] mNonce;

    public static EncryptedChunk create(ChunkHash key, byte[] nonce, byte[] encryptedBytes) {
        Preconditions.checkArgument(nonce.length == 12, "Nonce does not have the correct length.");
        return new EncryptedChunk(key, nonce, encryptedBytes);
    }

    private EncryptedChunk(ChunkHash key, byte[] nonce, byte[] encryptedBytes) {
        this.mKey = key;
        this.mNonce = nonce;
        this.mEncryptedBytes = encryptedBytes;
    }

    public ChunkHash key() {
        return this.mKey;
    }

    public byte[] nonce() {
        return this.mNonce;
    }

    public byte[] encryptedBytes() {
        return this.mEncryptedBytes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EncryptedChunk)) {
            return false;
        }
        EncryptedChunk encryptedChunkOrdering = (EncryptedChunk) o;
        if (!Arrays.equals(this.mEncryptedBytes, encryptedChunkOrdering.mEncryptedBytes) || !Arrays.equals(this.mNonce, encryptedChunkOrdering.mNonce) || !this.mKey.equals(encryptedChunkOrdering.mKey)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mKey, Integer.valueOf(Arrays.hashCode(this.mNonce)), Integer.valueOf(Arrays.hashCode(this.mEncryptedBytes)));
    }
}
