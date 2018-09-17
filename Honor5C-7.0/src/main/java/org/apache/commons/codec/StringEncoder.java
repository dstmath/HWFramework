package org.apache.commons.codec;

@Deprecated
public interface StringEncoder extends Encoder {
    String encode(String str) throws EncoderException;
}
