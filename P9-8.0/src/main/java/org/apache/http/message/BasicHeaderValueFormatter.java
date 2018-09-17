package org.apache.http.message;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class BasicHeaderValueFormatter implements HeaderValueFormatter {
    public static final BasicHeaderValueFormatter DEFAULT = new BasicHeaderValueFormatter();
    public static final String SEPARATORS = " ;,:@()<>\\\"/[]?={}\t";
    public static final String UNSAFE_CHARS = "\"\\";

    public static final String formatElements(HeaderElement[] elems, boolean quote, HeaderValueFormatter formatter) {
        if (formatter == null) {
            formatter = DEFAULT;
        }
        return formatter.formatElements(null, elems, quote).toString();
    }

    public CharArrayBuffer formatElements(CharArrayBuffer buffer, HeaderElement[] elems, boolean quote) {
        if (elems == null) {
            throw new IllegalArgumentException("Header element array must not be null.");
        }
        int len = estimateElementsLen(elems);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }
        for (int i = 0; i < elems.length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            formatHeaderElement(buffer, elems[i], quote);
        }
        return buffer;
    }

    protected int estimateElementsLen(HeaderElement[] elems) {
        if (elems == null || elems.length < 1) {
            return 0;
        }
        int result = (elems.length - 1) * 2;
        for (HeaderElement estimateHeaderElementLen : elems) {
            result += estimateHeaderElementLen(estimateHeaderElementLen);
        }
        return result;
    }

    public static final String formatHeaderElement(HeaderElement elem, boolean quote, HeaderValueFormatter formatter) {
        if (formatter == null) {
            formatter = DEFAULT;
        }
        return formatter.formatHeaderElement(null, elem, quote).toString();
    }

    public CharArrayBuffer formatHeaderElement(CharArrayBuffer buffer, HeaderElement elem, boolean quote) {
        if (elem == null) {
            throw new IllegalArgumentException("Header element must not be null.");
        }
        int len = estimateHeaderElementLen(elem);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }
        buffer.append(elem.getName());
        String value = elem.getValue();
        if (value != null) {
            buffer.append('=');
            doFormatValue(buffer, value, quote);
        }
        int parcnt = elem.getParameterCount();
        if (parcnt > 0) {
            for (int i = 0; i < parcnt; i++) {
                buffer.append("; ");
                formatNameValuePair(buffer, elem.getParameter(i), quote);
            }
        }
        return buffer;
    }

    protected int estimateHeaderElementLen(HeaderElement elem) {
        if (elem == null) {
            return 0;
        }
        int result = elem.getName().length();
        String value = elem.getValue();
        if (value != null) {
            result += value.length() + 3;
        }
        int parcnt = elem.getParameterCount();
        if (parcnt > 0) {
            for (int i = 0; i < parcnt; i++) {
                result += estimateNameValuePairLen(elem.getParameter(i)) + 2;
            }
        }
        return result;
    }

    public static final String formatParameters(NameValuePair[] nvps, boolean quote, HeaderValueFormatter formatter) {
        if (formatter == null) {
            formatter = DEFAULT;
        }
        return formatter.formatParameters(null, nvps, quote).toString();
    }

    public CharArrayBuffer formatParameters(CharArrayBuffer buffer, NameValuePair[] nvps, boolean quote) {
        if (nvps == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        int len = estimateParametersLen(nvps);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }
        for (int i = 0; i < nvps.length; i++) {
            if (i > 0) {
                buffer.append("; ");
            }
            formatNameValuePair(buffer, nvps[i], quote);
        }
        return buffer;
    }

    protected int estimateParametersLen(NameValuePair[] nvps) {
        if (nvps == null || nvps.length < 1) {
            return 0;
        }
        int result = (nvps.length - 1) * 2;
        for (NameValuePair estimateNameValuePairLen : nvps) {
            result += estimateNameValuePairLen(estimateNameValuePairLen);
        }
        return result;
    }

    public static final String formatNameValuePair(NameValuePair nvp, boolean quote, HeaderValueFormatter formatter) {
        if (formatter == null) {
            formatter = DEFAULT;
        }
        return formatter.formatNameValuePair(null, nvp, quote).toString();
    }

    public CharArrayBuffer formatNameValuePair(CharArrayBuffer buffer, NameValuePair nvp, boolean quote) {
        if (nvp == null) {
            throw new IllegalArgumentException("NameValuePair must not be null.");
        }
        int len = estimateNameValuePairLen(nvp);
        if (buffer == null) {
            buffer = new CharArrayBuffer(len);
        } else {
            buffer.ensureCapacity(len);
        }
        buffer.append(nvp.getName());
        String value = nvp.getValue();
        if (value != null) {
            buffer.append('=');
            doFormatValue(buffer, value, quote);
        }
        return buffer;
    }

    protected int estimateNameValuePairLen(NameValuePair nvp) {
        if (nvp == null) {
            return 0;
        }
        int result = nvp.getName().length();
        String value = nvp.getValue();
        if (value != null) {
            result += value.length() + 3;
        }
        return result;
    }

    protected void doFormatValue(CharArrayBuffer buffer, String value, boolean quote) {
        int i;
        if (!quote) {
            for (i = 0; i < value.length() && (quote ^ 1) != 0; i++) {
                quote = isSeparator(value.charAt(i));
            }
        }
        if (quote) {
            buffer.append('\"');
        }
        for (i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (isUnsafe(ch)) {
                buffer.append('\\');
            }
            buffer.append(ch);
        }
        if (quote) {
            buffer.append('\"');
        }
    }

    protected boolean isSeparator(char ch) {
        return SEPARATORS.indexOf(ch) >= 0;
    }

    protected boolean isUnsafe(char ch) {
        return UNSAFE_CHARS.indexOf(ch) >= 0;
    }
}
