package com.android.server.backup.encryption.chunking;

import java.io.IOException;

public class LengthlessEncryptedChunkEncoder implements EncryptedChunkEncoder {
    @Override // com.android.server.backup.encryption.chunking.EncryptedChunkEncoder
    public void writeChunkToWriter(BackupWriter writer, EncryptedChunk chunk) throws IOException {
        writer.writeBytes(chunk.nonce());
        writer.writeBytes(chunk.encryptedBytes());
    }

    @Override // com.android.server.backup.encryption.chunking.EncryptedChunkEncoder
    public int getEncodedLengthOfChunk(EncryptedChunk chunk) {
        return chunk.nonce().length + chunk.encryptedBytes().length;
    }

    @Override // com.android.server.backup.encryption.chunking.EncryptedChunkEncoder
    public int getChunkOrderingType() {
        return 1;
    }
}
