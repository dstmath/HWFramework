package com.android.internal.http.multipart;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteArrayPartSource implements PartSource {
    private byte[] bytes;
    private String fileName;

    public ByteArrayPartSource(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
    }

    public long getLength() {
        return (long) this.bytes.length;
    }

    public String getFileName() {
        return this.fileName;
    }

    public InputStream createInputStream() {
        return new ByteArrayInputStream(this.bytes);
    }
}
