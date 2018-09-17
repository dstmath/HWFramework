package org.apache.commons.codec;

@Deprecated
public interface Decoder {
    Object decode(Object obj) throws DecoderException;
}
