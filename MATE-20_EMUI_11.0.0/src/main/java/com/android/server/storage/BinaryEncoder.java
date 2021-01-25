package com.android.server.storage;

@Deprecated
public interface BinaryEncoder extends Encoder {
    byte[] encode(byte[] bArr) throws EncoderException;
}
