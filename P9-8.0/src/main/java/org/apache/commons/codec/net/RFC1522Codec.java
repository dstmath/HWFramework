package org.apache.commons.codec.net;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

@Deprecated
abstract class RFC1522Codec {
    protected abstract byte[] doDecoding(byte[] bArr) throws DecoderException;

    protected abstract byte[] doEncoding(byte[] bArr) throws EncoderException;

    protected abstract String getEncoding();

    RFC1522Codec() {
    }

    protected String encodeText(String text, String charset) throws EncoderException, UnsupportedEncodingException {
        if (text == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("=?");
        buffer.append(charset);
        buffer.append('?');
        buffer.append(getEncoding());
        buffer.append('?');
        buffer.append(new String(doEncoding(text.getBytes(charset)), "US-ASCII"));
        buffer.append("?=");
        return buffer.toString();
    }

    protected String decodeText(String text) throws DecoderException, UnsupportedEncodingException {
        if (text == null) {
            return null;
        }
        if (text.startsWith("=?") && (text.endsWith("?=") ^ 1) == 0) {
            int termnator = text.length() - 2;
            int to = text.indexOf("?", 2);
            if (to == -1 || to == termnator) {
                throw new DecoderException("RFC 1522 violation: charset token not found");
            }
            String charset = text.substring(2, to);
            if (charset.equals("")) {
                throw new DecoderException("RFC 1522 violation: charset not specified");
            }
            int from = to + 1;
            to = text.indexOf("?", from);
            if (to == -1 || to == termnator) {
                throw new DecoderException("RFC 1522 violation: encoding token not found");
            }
            String encoding = text.substring(from, to);
            if (getEncoding().equalsIgnoreCase(encoding)) {
                from = to + 1;
                return new String(doDecoding(text.substring(from, text.indexOf("?", from)).getBytes("US-ASCII")), charset);
            }
            throw new DecoderException("This codec cannot decode " + encoding + " encoded content");
        }
        throw new DecoderException("RFC 1522 violation: malformed encoded content");
    }
}
