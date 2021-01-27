package com.android.server.backup.encryption.chunking;

import java.io.IOException;

public class InlineLengthsEncryptedChunkEncoder implements EncryptedChunkEncoder {
    public static final int BYTES_LENGTH = 4;
    private final LengthlessEncryptedChunkEncoder mLengthlessEncryptedChunkEncoder = new LengthlessEncryptedChunkEncoder();

    @Override // com.android.server.backup.encryption.chunking.EncryptedChunkEncoder
    public void writeChunkToWriter(BackupWriter writer, EncryptedChunk chunk) throws IOException {
        writer.writeBytes(toByteArray(this.mLengthlessEncryptedChunkEncoder.getEncodedLengthOfChunk(chunk)));
        this.mLengthlessEncryptedChunkEncoder.writeChunkToWriter(writer, chunk);
    }

    @Override // com.android.server.backup.encryption.chunking.EncryptedChunkEncoder
    public int getEncodedLengthOfChunk(EncryptedChunk chunk) {
        return this.mLengthlessEncryptedChunkEncoder.getEncodedLengthOfChunk(chunk) + 4;
    }

    @Override // com.android.server.backup.encryption.chunking.EncryptedChunkEncoder
    public int getChunkOrderingType() {
        return 2;
    }

    static byte[] toByteArray(int value) {
        return new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
    }
}
