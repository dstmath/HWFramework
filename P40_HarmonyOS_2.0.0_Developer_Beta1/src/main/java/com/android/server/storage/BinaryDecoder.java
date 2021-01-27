package com.android.server.storage;

@Deprecated
public interface BinaryDecoder extends Decoder {
    byte[] decode(byte[] bArr) throws DecoderException;
}
