package com.android.server.backup.encryption.chunking;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public interface Chunker {

    public interface ChunkConsumer {
        void accept(byte[] bArr) throws GeneralSecurityException;
    }

    void chunkify(InputStream inputStream, ChunkConsumer chunkConsumer) throws IOException, GeneralSecurityException;
}
