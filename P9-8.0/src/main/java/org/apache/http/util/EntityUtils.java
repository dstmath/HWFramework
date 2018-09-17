package org.apache.http.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;

@Deprecated
public final class EntityUtils {
    private EntityUtils() {
    }

    public static byte[] toByteArray(HttpEntity entity) throws IOException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        InputStream instream = entity.getContent();
        if (instream == null) {
            return new byte[0];
        }
        if (entity.getContentLength() > 2147483647L) {
            throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
        }
        int i = (int) entity.getContentLength();
        if (i < 0) {
            i = 4096;
        }
        ByteArrayBuffer buffer = new ByteArrayBuffer(i);
        try {
            byte[] tmp = new byte[4096];
            while (true) {
                int l = instream.read(tmp);
                if (l == -1) {
                    break;
                }
                buffer.append(tmp, 0, l);
            }
            return buffer.toByteArray();
        } finally {
            instream.close();
        }
    }

    public static String getContentCharSet(HttpEntity entity) throws ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        } else if (entity.getContentType() == null) {
            return null;
        } else {
            HeaderElement[] values = entity.getContentType().getElements();
            if (values.length <= 0) {
                return null;
            }
            NameValuePair param = values[0].getParameterByName("charset");
            if (param != null) {
                return param.getValue();
            }
            return null;
        }
    }

    public static String toString(HttpEntity entity, String defaultCharset) throws IOException, ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        InputStream instream = entity.getContent();
        if (instream == null) {
            return "";
        }
        if (entity.getContentLength() > 2147483647L) {
            throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
        }
        int i = (int) entity.getContentLength();
        if (i < 0) {
            i = 4096;
        }
        String charset = getContentCharSet(entity);
        if (charset == null) {
            charset = defaultCharset;
        }
        if (charset == null) {
            charset = "ISO-8859-1";
        }
        Reader reader = new InputStreamReader(instream, charset);
        CharArrayBuffer buffer = new CharArrayBuffer(i);
        try {
            char[] tmp = new char[1024];
            while (true) {
                int l = reader.read(tmp);
                if (l == -1) {
                    break;
                }
                buffer.append(tmp, 0, l);
            }
            return buffer.toString();
        } finally {
            reader.close();
        }
    }

    public static String toString(HttpEntity entity) throws IOException, ParseException {
        return toString(entity, null);
    }
}
