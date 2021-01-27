package com.android.internal.os;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ByteTransferPipe extends TransferPipe {
    static final String TAG = "ByteTransferPipe";
    private ByteArrayOutputStream mOutputStream;

    public ByteTransferPipe() throws IOException {
    }

    public ByteTransferPipe(String bufferPrefix) throws IOException {
        super(bufferPrefix, TAG);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.os.TransferPipe
    public OutputStream getNewOutputStream() {
        this.mOutputStream = new ByteArrayOutputStream();
        return this.mOutputStream;
    }

    public byte[] get() throws IOException {
        go(null);
        return this.mOutputStream.toByteArray();
    }
}
