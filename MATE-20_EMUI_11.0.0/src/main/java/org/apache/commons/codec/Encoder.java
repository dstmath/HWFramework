package org.apache.commons.codec;

@Deprecated
public interface Encoder {
    Object encode(Object obj) throws EncoderException;
}
