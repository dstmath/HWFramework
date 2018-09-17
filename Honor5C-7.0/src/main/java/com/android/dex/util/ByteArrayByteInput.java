package com.android.dex.util;

public final class ByteArrayByteInput implements ByteInput {
    private final byte[] bytes;
    private int position;

    public ByteArrayByteInput(byte... bytes) {
        this.bytes = bytes;
    }

    public byte readByte() {
        byte[] bArr = this.bytes;
        int i = this.position;
        this.position = i + 1;
        return bArr[i];
    }
}
