package com.android.internal.http.multipart;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteArrayPartSource implements PartSource {
    private byte[] bytes;
    private String fileName;

    public ByteArrayPartSource(String fileName2, byte[] bytes2) {
        this.fileName = fileName2;
        this.bytes = bytes2;
    }

    @Override // com.android.internal.http.multipart.PartSource
    public long getLength() {
        return (long) this.bytes.length;
    }

    @Override // com.android.internal.http.multipart.PartSource
    public String getFileName() {
        return this.fileName;
    }

    @Override // com.android.internal.http.multipart.PartSource
    public InputStream createInputStream() {
        return new ByteArrayInputStream(this.bytes);
    }
}
