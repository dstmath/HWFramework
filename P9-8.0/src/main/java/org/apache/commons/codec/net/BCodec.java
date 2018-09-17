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

    public BCodec(String charset) {
        this.charset = charset;
    }

    protected String getEncoding() {
        return "B";
    }

    protected byte[] doEncoding(byte[] bytes) throws EncoderException {
        if (bytes == null) {
            return null;
        }
        return Base64.encodeBase64(bytes);
    }

    protected byte[] doDecoding(byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        return Base64.decodeBase64(bytes);
    }

    public String encode(String value, String charset) throws EncoderException {
        if (value == null) {
            return null;
        }
        try {
            return encodeText(value, charset);
        } catch (UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage());
        }
    }

    public String encode(String value) throws EncoderException {
        if (value == null) {
            return null;
        }
        return encode(value, getDefaultCharset());
    }

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

    public Object encode(Object value) throws EncoderException {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return encode((String) value);
        }
        throw new EncoderException("Objects of type " + value.getClass().getName() + " cannot be encoded using BCodec");
    }

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
