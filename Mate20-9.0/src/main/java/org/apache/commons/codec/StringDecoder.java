package org.apache.commons.codec;

@Deprecated
public interface StringDecoder extends Decoder {
    String decode(String str) throws DecoderException;
}
