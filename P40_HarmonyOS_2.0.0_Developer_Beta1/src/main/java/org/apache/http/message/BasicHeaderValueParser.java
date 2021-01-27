package org.apache.http.message;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class BasicHeaderValueParser implements HeaderValueParser {
    private static final char[] ALL_DELIMITERS = {PARAM_DELIMITER, ELEM_DELIMITER};
    public static final BasicHeaderValueParser DEFAULT = new BasicHeaderValueParser();
    private static final char ELEM_DELIMITER = ',';
    private static final char PARAM_DELIMITER = ';';

    public static final HeaderElement[] parseElements(String value, HeaderValueParser parser) throws ParseException {
        if (value != null) {
            if (parser == null) {
                parser = DEFAULT;
            }
            CharArrayBuffer buffer = new CharArrayBuffer(value.length());
            buffer.append(value);
            return parser.parseElements(buffer, new ParserCursor(0, value.length()));
        }
        throw new IllegalArgumentException("Value to parse may not be null");
    }

    @Override // org.apache.http.message.HeaderValueParser
    public HeaderElement[] parseElements(CharArrayBuffer buffer, ParserCursor cursor) {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor != null) {
            List elements = new ArrayList();
            while (!cursor.atEnd()) {
                HeaderElement element = parseHeaderElement(buffer, cursor);
                if (element.getName().length() != 0 || element.getValue() != null) {
                    elements.add(element);
                }
            }
            return (HeaderElement[]) elements.toArray(new HeaderElement[elements.size()]);
        } else {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
    }

    public static final HeaderElement parseHeaderElement(String value, HeaderValueParser parser) throws ParseException {
        if (value != null) {
            if (parser == null) {
                parser = DEFAULT;
            }
            CharArrayBuffer buffer = new CharArrayBuffer(value.length());
            buffer.append(value);
            return parser.parseHeaderElement(buffer, new ParserCursor(0, value.length()));
        }
        throw new IllegalArgumentException("Value to parse may not be null");
    }

    @Override // org.apache.http.message.HeaderValueParser
    public HeaderElement parseHeaderElement(CharArrayBuffer buffer, ParserCursor cursor) {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor != null) {
            NameValuePair nvp = parseNameValuePair(buffer, cursor);
            NameValuePair[] params = null;
            if (!cursor.atEnd() && buffer.charAt(cursor.getPos() - 1) != ',') {
                params = parseParameters(buffer, cursor);
            }
            return createHeaderElement(nvp.getName(), nvp.getValue(), params);
        } else {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
    }

    /* access modifiers changed from: protected */
    public HeaderElement createHeaderElement(String name, String value, NameValuePair[] params) {
        return new BasicHeaderElement(name, value, params);
    }

    public static final NameValuePair[] parseParameters(String value, HeaderValueParser parser) throws ParseException {
        if (value != null) {
            if (parser == null) {
                parser = DEFAULT;
            }
            CharArrayBuffer buffer = new CharArrayBuffer(value.length());
            buffer.append(value);
            return parser.parseParameters(buffer, new ParserCursor(0, value.length()));
        }
        throw new IllegalArgumentException("Value to parse may not be null");
    }

    @Override // org.apache.http.message.HeaderValueParser
    public NameValuePair[] parseParameters(CharArrayBuffer buffer, ParserCursor cursor) {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor != null) {
            int pos = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            while (pos < indexTo && HTTP.isWhitespace(buffer.charAt(pos))) {
                pos++;
            }
            cursor.updatePos(pos);
            if (cursor.atEnd()) {
                return new NameValuePair[0];
            }
            List params = new ArrayList();
            while (!cursor.atEnd()) {
                params.add(parseNameValuePair(buffer, cursor));
                if (buffer.charAt(cursor.getPos() - 1) == ',') {
                    break;
                }
            }
            return (NameValuePair[]) params.toArray(new NameValuePair[params.size()]);
        } else {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
    }

    public static final NameValuePair parseNameValuePair(String value, HeaderValueParser parser) throws ParseException {
        if (value != null) {
            if (parser == null) {
                parser = DEFAULT;
            }
            CharArrayBuffer buffer = new CharArrayBuffer(value.length());
            buffer.append(value);
            return parser.parseNameValuePair(buffer, new ParserCursor(0, value.length()));
        }
        throw new IllegalArgumentException("Value to parse may not be null");
    }

    @Override // org.apache.http.message.HeaderValueParser
    public NameValuePair parseNameValuePair(CharArrayBuffer buffer, ParserCursor cursor) {
        return parseNameValuePair(buffer, cursor, ALL_DELIMITERS);
    }

    private static boolean isOneOf(char ch, char[] chs) {
        if (chs == null) {
            return false;
        }
        for (char c : chs) {
            if (ch == c) {
                return true;
            }
        }
        return false;
    }

    public NameValuePair parseNameValuePair(CharArrayBuffer buffer, ParserCursor cursor, char[] delimiters) {
        String name;
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor != null) {
            boolean terminated = false;
            int pos = cursor.getPos();
            int indexFrom = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            while (true) {
                if (pos < indexTo) {
                    char ch = buffer.charAt(pos);
                    if (ch == '=') {
                        break;
                    } else if (isOneOf(ch, delimiters)) {
                        terminated = true;
                        break;
                    } else {
                        pos++;
                    }
                } else {
                    break;
                }
            }
            if (pos == indexTo) {
                terminated = true;
                name = buffer.substringTrimmed(indexFrom, indexTo);
            } else {
                name = buffer.substringTrimmed(indexFrom, pos);
                pos++;
            }
            if (terminated) {
                cursor.updatePos(pos);
                return createNameValuePair(name, null);
            }
            int i1 = pos;
            boolean qouted = false;
            boolean escaped = false;
            while (true) {
                if (pos >= indexTo) {
                    break;
                }
                char ch2 = buffer.charAt(pos);
                boolean z = false;
                if (ch2 == '\"' && !escaped) {
                    qouted = !qouted;
                }
                if (!qouted && !escaped && isOneOf(ch2, delimiters)) {
                    terminated = true;
                    break;
                }
                if (escaped) {
                    escaped = false;
                } else {
                    if (qouted && ch2 == '\\') {
                        z = true;
                    }
                    escaped = z;
                }
                pos++;
            }
            int i2 = pos;
            while (i1 < i2 && HTTP.isWhitespace(buffer.charAt(i1))) {
                i1++;
            }
            while (i2 > i1 && HTTP.isWhitespace(buffer.charAt(i2 - 1))) {
                i2--;
            }
            if (i2 - i1 >= 2 && buffer.charAt(i1) == '\"' && buffer.charAt(i2 - 1) == '\"') {
                i1++;
                i2--;
            }
            String value = buffer.substring(i1, i2);
            if (terminated) {
                pos++;
            }
            cursor.updatePos(pos);
            return createNameValuePair(name, value);
        } else {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
    }

    /* access modifiers changed from: protected */
    public NameValuePair createNameValuePair(String name, String value) {
        return new BasicNameValuePair(name, value);
    }
}
