package com.android.server.storage;

@Deprecated
public interface Decoder {
    Object decode(Object obj) throws DecoderException;
}
