package org.apache.commons.codec.net;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

@Deprecated
abstract class RFC1522Codec {
    /* access modifiers changed from: protected */
    public abstract byte[] doDecoding(byte[] bArr) throws DecoderException;

    /* access modifiers changed from: protected */
    public abstract byte[] doEncoding(byte[] bArr) throws EncoderException;

    /* access modifiers changed from: protected */
    public abstract String getEncoding();

    RFC1522Codec() {
    }

    /* access modifiers changed from: protected */
    public String encodeText(String text, String charset) throws EncoderException, UnsupportedEncodingException {
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

    /* access modifiers changed from: protected */
    public String decodeText(String text) throws DecoderException, UnsupportedEncodingException {
        if (text == null) {
            return null;
        }
        if (!text.startsWith("=?") || !text.endsWith("?=")) {
            throw new DecoderException("RFC 1522 violation: malformed encoded content");
        }
        int termnator = text.length() - 2;
        int to = text.indexOf("?", 2);
        if (to == -1 || to == termnator) {
            throw new DecoderException("RFC 1522 violation: charset token not found");
        }
        String charset = text.substring(2, to);
        if (!charset.equals("")) {
            int from = to + 1;
            int to2 = text.indexOf("?", from);
            if (to2 == -1 || to2 == termnator) {
                throw new DecoderException("RFC 1522 violation: encoding token not found");
            }
            String encoding = text.substring(from, to2);
            if (getEncoding().equalsIgnoreCase(encoding)) {
                int from2 = to2 + 1;
                return new String(doDecoding(text.substring(from2, text.indexOf("?", from2)).getBytes("US-ASCII")), charset);
            }
            throw new DecoderException("This codec cannot decode " + encoding + " encoded content");
        }
        throw new DecoderException("RFC 1522 violation: charset not specified");
    }
}
