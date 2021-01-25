package com.android.server.backup.encryption.chunking;

import com.android.internal.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.OutputStream;

public class DiffScriptBackupWriter implements BackupWriter {
    private static final int ENCRYPTION_DIFF_SCRIPT_MAX_CHUNK_SIZE_BYTES = 1048576;
    private long mBytesWritten;
    private final SingleStreamDiffScriptWriter mWriter;

    public static DiffScriptBackupWriter newInstance(OutputStream outputStream) {
        return new DiffScriptBackupWriter(new SingleStreamDiffScriptWriter(outputStream, 1048576));
    }

    @VisibleForTesting
    DiffScriptBackupWriter(SingleStreamDiffScriptWriter writer) {
        this.mWriter = writer;
    }

    @Override // com.android.server.backup.encryption.chunking.BackupWriter
    public void writeBytes(byte[] bytes) throws IOException {
        for (byte b : bytes) {
            this.mWriter.writeByte(b);
        }
        this.mBytesWritten += (long) bytes.length;
    }

    @Override // com.android.server.backup.encryption.chunking.BackupWriter
    public void writeChunk(long start, int length) throws IOException {
        this.mWriter.writeChunk(start, length);
        this.mBytesWritten += (long) length;
    }

    @Override // com.android.server.backup.encryption.chunking.BackupWriter
    public long getBytesWritten() {
        return this.mBytesWritten;
    }

    @Override // com.android.server.backup.encryption.chunking.BackupWriter
    public void flush() throws IOException {
        this.mWriter.flush();
    }
}
