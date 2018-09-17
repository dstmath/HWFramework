package org.apache.commons.codec;

@Deprecated
public interface BinaryEncoder extends Encoder {
    byte[] encode(byte[] bArr) throws EncoderException;
}
