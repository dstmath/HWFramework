package org.apache.commons.codec.net;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringDecoder;
import org.apache.commons.codec.StringEncoder;

@Deprecated
public class QCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
    private static byte BLANK = (byte) 32;
    private static final BitSet PRINTABLE_CHARS = new BitSet(256);
    private static byte UNDERSCORE = (byte) 95;
    private String charset = "UTF-8";
    private boolean encodeBlanks = false;

    static {
        int i;
        PRINTABLE_CHARS.set(32);
        PRINTABLE_CHARS.set(33);
        PRINTABLE_CHARS.set(34);
        PRINTABLE_CHARS.set(35);
        PRINTABLE_CHARS.set(36);
        PRINTABLE_CHARS.set(37);
        PRINTABLE_CHARS.set(38);
        PRINTABLE_CHARS.set(39);
        PRINTABLE_CHARS.set(40);
        PRINTABLE_CHARS.set(41);
        PRINTABLE_CHARS.set(42);
        PRINTABLE_CHARS.set(43);
        PRINTABLE_CHARS.set(44);
        PRINTABLE_CHARS.set(45);
        PRINTABLE_CHARS.set(46);
        PRINTABLE_CHARS.set(47);
        for (i = 48; i <= 57; i++) {
            PRINTABLE_CHARS.set(i);
        }
        PRINTABLE_CHARS.set(58);
        PRINTABLE_CHARS.set(59);
        PRINTABLE_CHARS.set(60);
        PRINTABLE_CHARS.set(62);
        PRINTABLE_CHARS.set(64);
        for (i = 65; i <= 90; i++) {
            PRINTABLE_CHARS.set(i);
        }
        PRINTABLE_CHARS.set(91);
        PRINTABLE_CHARS.set(92);
        PRINTABLE_CHARS.set(93);
        PRINTABLE_CHARS.set(94);
        PRINTABLE_CHARS.set(96);
        for (i = 97; i <= 122; i++) {
            PRINTABLE_CHARS.set(i);
        }
        PRINTABLE_CHARS.set(123);
        PRINTABLE_CHARS.set(124);
        PRINTABLE_CHARS.set(125);
        PRINTABLE_CHARS.set(126);
    }

    public QCodec(String charset) {
        this.charset = charset;
    }

    protected String getEncoding() {
        return "Q";
    }

    protected byte[] doEncoding(byte[] bytes) throws EncoderException {
        if (bytes == null) {
            return null;
        }
        byte[] data = QuotedPrintableCodec.encodeQuotedPrintable(PRINTABLE_CHARS, bytes);
        if (this.encodeBlanks) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == BLANK) {
                    data[i] = UNDERSCORE;
                }
            }
        }
        return data;
    }

    protected byte[] doDecoding(byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }
        int i;
        boolean hasUnderscores = false;
        for (byte b : bytes) {
            if (b == UNDERSCORE) {
                hasUnderscores = true;
                break;
            }
        }
        if (!hasUnderscores) {
            return QuotedPrintableCodec.decodeQuotedPrintable(bytes);
        }
        byte[] tmp = new byte[bytes.length];
        for (i = 0; i < bytes.length; i++) {
            byte b2 = bytes[i];
            if (b2 != UNDERSCORE) {
                tmp[i] = b2;
            } else {
                tmp[i] = BLANK;
            }
        }
        return QuotedPrintableCodec.decodeQuotedPrintable(tmp);
    }

    public String encode(String pString, String charset) throws EncoderException {
        if (pString == null) {
            return null;
        }
        try {
            return encodeText(pString, charset);
        } catch (UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage());
        }
    }

    public String encode(String pString) throws EncoderException {
        if (pString == null) {
            return null;
        }
        return encode(pString, getDefaultCharset());
    }

    public String decode(String pString) throws DecoderException {
        if (pString == null) {
            return null;
        }
        try {
            return decodeText(pString);
        } catch (UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage());
        }
    }

    public Object encode(Object pObject) throws EncoderException {
        if (pObject == null) {
            return null;
        }
        if (pObject instanceof String) {
            return encode((String) pObject);
        }
        throw new EncoderException("Objects of type " + pObject.getClass().getName() + " cannot be encoded using Q codec");
    }

    public Object decode(Object pObject) throws DecoderException {
        if (pObject == null) {
            return null;
        }
        if (pObject instanceof String) {
            return decode((String) pObject);
        }
        throw new DecoderException("Objects of type " + pObject.getClass().getName() + " cannot be decoded using Q codec");
    }

    public String getDefaultCharset() {
        return this.charset;
    }

    public boolean isEncodeBlanks() {
        return this.encodeBlanks;
    }

    public void setEncodeBlanks(boolean b) {
        this.encodeBlanks = b;
    }
}
