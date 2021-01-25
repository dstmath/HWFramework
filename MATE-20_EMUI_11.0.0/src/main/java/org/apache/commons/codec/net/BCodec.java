package org.apache.commons.codec.net;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringDecoder;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.binary.Base64;

@Deprecated
public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
    private String charset = "UTF-8";

    public BCodec() {
    }

    public BCodec(String charset2) {
        this.charset = charset2;
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.commons.codec.net.RFC1522Codec
    public String getEncoding() {
        return "B";
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.commons.codec.net.RFC1522Codec
    public byte[] doEncoding(byte[] bytes) throws EncoderException {
        if (bytes == null) {
            return null;
        }
        return Base64.encodeBase64(bytes);
    }

    /* access modifiers changed from: protected */
    @Override // org.apache.commons.codec.net.RFC1522Codec
    public byte[] doDecoding(byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        return Base64.decodeBase64(bytes);
    }

    public String encode(String value, String charset2) throws EncoderException {
        if (value == null) {
            return null;
        }
        try {
            return encodeText(value, charset2);
        } catch (UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage());
        }
    }

    @Override // org.apache.commons.codec.StringEncoder
    public String encode(String value) throws EncoderException {
        if (value == null) {
            return null;
        }
        return encode(value, getDefaultCharset());
    }

    @Override // org.apache.commons.codec.StringDecoder
    public String decode(String value) throws DecoderException {
        if (value == null) {
            return null;
        }
        try {
            return decodeText(value);
        } catch (UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage());
        }
    }

    @Override // org.apache.commons.codec.Encoder
    public Object encode(Object value) throws EncoderException {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return encode((String) value);
        }
        throw new EncoderException("Objects of type " + value.getClass().getName() + " cannot be encoded using BCodec");
    }

    @Override // org.apache.commons.codec.Decoder
    public Object decode(Object value) throws DecoderException {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return decode((String) value);
        }
        throw new DecoderException("Objects of type " + value.getClass().getName() + " cannot be decoded using BCodec");
    }

    public String getDefaultCharset() {
        return this.charset;
    }
}
