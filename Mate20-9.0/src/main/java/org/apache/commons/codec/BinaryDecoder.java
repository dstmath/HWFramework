package org.apache.commons.codec;

@Deprecated
public interface BinaryDecoder extends Decoder {
    byte[] decode(byte[] bArr) throws DecoderException;
}
