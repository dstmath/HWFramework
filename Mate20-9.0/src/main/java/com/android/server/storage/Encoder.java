package com.android.server.storage;

@Deprecated
public interface Encoder {
    Object encode(Object obj) throws EncoderException;
}
