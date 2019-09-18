package org.apache.http.message;

import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class BasicLineParser implements LineParser {
    public static final BasicLineParser DEFAULT = new BasicLineParser();
    protected final ProtocolVersion protocol;

    public BasicLineParser(ProtocolVersion proto) {
        this.protocol = proto == null ? HttpVersion.HTTP_1_1 : proto;
    }

    public BasicLineParser() {
        this(null);
    }

    public static final ProtocolVersion parseProtocolVersion(String value, LineParser parser) throws ParseException {
        if (value != null) {
            if (parser == null) {
                parser = DEFAULT;
            }
            CharArrayBuffer buffer = new CharArrayBuffer(value.length());
            buffer.append(value);
            return parser.parseProtocolVersion(buffer, new ParserCursor(0, value.length()));
        }
        throw new IllegalArgumentException("Value to parse may not be null.");
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0040  */
    public ProtocolVersion parseProtocolVersion(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor != null) {
            String protoname = this.protocol.getProtocol();
            int protolength = protoname.length();
            int indexFrom = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            skipWhitespace(buffer, cursor);
            int i = cursor.getPos();
            if (i + protolength + 4 <= indexTo) {
                boolean z = false;
                boolean ok = true;
                int j = 0;
                while (true) {
                    boolean z2 = true;
                    if (ok && j < protolength) {
                        if (buffer.charAt(i + j) != protoname.charAt(j)) {
                            z2 = false;
                        }
                        ok = z2;
                        j++;
                    } else if (ok) {
                        if (buffer.charAt(i + protolength) == '/') {
                            z = true;
                        }
                        ok = z;
                    }
                }
                if (ok) {
                }
                if (ok) {
                    int i2 = i + protolength + 1;
                    int period = buffer.indexOf(46, i2, indexTo);
                    if (period != -1) {
                        try {
                            int major = Integer.parseInt(buffer.substringTrimmed(i2, period));
                            int i3 = period + 1;
                            int blank = buffer.indexOf(32, i3, indexTo);
                            if (blank == -1) {
                                blank = indexTo;
                            }
                            try {
                                int minor = Integer.parseInt(buffer.substringTrimmed(i3, blank));
                                cursor.updatePos(blank);
                                return createProtocolVersion(major, minor);
                            } catch (NumberFormatException e) {
                                throw new ParseException("Invalid protocol minor version number: " + buffer.substring(indexFrom, indexTo));
                            }
                        } catch (NumberFormatException e2) {
                            throw new ParseException("Invalid protocol major version number: " + buffer.substring(indexFrom, indexTo));
                        }
                    } else {
                        throw new ParseException("Invalid protocol version number: " + buffer.substring(indexFrom, indexTo));
                    }
                } else {
                    throw new ParseException("Not a valid protocol version: " + buffer.substring(indexFrom, indexTo));
                }
            } else {
                throw new ParseException("Not a valid protocol version: " + buffer.substring(indexFrom, indexTo));
            }
        } else {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
    }

    /* access modifiers changed from: protected */
    public ProtocolVersion createProtocolVersion(int major, int minor) {
        return this.protocol.forVersion(major, minor);
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0063  */
    public boolean hasProtocolVersion(CharArrayBuffer buffer, ParserCursor cursor) {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor != null) {
            int index = cursor.getPos();
            String protoname = this.protocol.getProtocol();
            int protolength = protoname.length();
            boolean z = false;
            if (buffer.length() < protolength + 4) {
                return false;
            }
            if (index < 0) {
                index = (buffer.length() - 4) - protolength;
            } else if (index == 0) {
                while (index < buffer.length() && HTTP.isWhitespace(buffer.charAt(index))) {
                    index++;
                }
            }
            if (index + protolength + 4 > buffer.length()) {
                return false;
            }
            boolean ok = true;
            int j = 0;
            while (true) {
                boolean z2 = true;
                if (ok && j < protolength) {
                    if (buffer.charAt(index + j) != protoname.charAt(j)) {
                        z2 = false;
                    }
                    ok = z2;
                    j++;
                } else if (ok) {
                    if (buffer.charAt(index + protolength) == '/') {
                        z = true;
                    }
                    ok = z;
                }
            }
            if (ok) {
            }
            return ok;
        } else {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
    }

    public static final RequestLine parseRequestLine(String value, LineParser parser) throws ParseException {
        if (value != null) {
            if (parser == null) {
                parser = DEFAULT;
            }
            CharArrayBuffer buffer = new CharArrayBuffer(value.length());
            buffer.append(value);
            return parser.parseRequestLine(buffer, new ParserCursor(0, value.length()));
        }
        throw new IllegalArgumentException("Value to parse may not be null.");
    }

    public RequestLine parseRequestLine(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor != null) {
            int indexFrom = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            try {
                skipWhitespace(buffer, cursor);
                int i = cursor.getPos();
                int blank = buffer.indexOf(32, i, indexTo);
                if (blank >= 0) {
                    String method = buffer.substringTrimmed(i, blank);
                    cursor.updatePos(blank);
                    skipWhitespace(buffer, cursor);
                    int i2 = cursor.getPos();
                    int blank2 = buffer.indexOf(32, i2, indexTo);
                    if (blank2 >= 0) {
                        String uri = buffer.substringTrimmed(i2, blank2);
                        cursor.updatePos(blank2);
                        ProtocolVersion ver = parseProtocolVersion(buffer, cursor);
                        skipWhitespace(buffer, cursor);
                        if (cursor.atEnd()) {
                            return createRequestLine(method, uri, ver);
                        }
                        throw new ParseException("Invalid request line: " + buffer.substring(indexFrom, indexTo));
                    }
                    throw new ParseException("Invalid request line: " + buffer.substring(indexFrom, indexTo));
                }
                throw new ParseException("Invalid request line: " + buffer.substring(indexFrom, indexTo));
            } catch (IndexOutOfBoundsException e) {
                throw new ParseException("Invalid request line: " + buffer.substring(indexFrom, indexTo));
            }
        } else {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
    }

    /* access modifiers changed from: protected */
    public RequestLine createRequestLine(String method, String uri, ProtocolVersion ver) {
        return new BasicRequestLine(method, uri, ver);
    }

    public static final StatusLine parseStatusLine(String value, LineParser parser) throws ParseException {
        if (value != null) {
            if (parser == null) {
                parser = DEFAULT;
            }
            CharArrayBuffer buffer = new CharArrayBuffer(value.length());
            buffer.append(value);
            return parser.parseStatusLine(buffer, new ParserCursor(0, value.length()));
        }
        throw new IllegalArgumentException("Value to parse may not be null.");
    }

    public StatusLine parseStatusLine(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
        String reasonPhrase;
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor != null) {
            int indexFrom = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            try {
                ProtocolVersion ver = parseProtocolVersion(buffer, cursor);
                skipWhitespace(buffer, cursor);
                int i = cursor.getPos();
                int blank = buffer.indexOf(32, i, indexTo);
                if (blank < 0) {
                    blank = indexTo;
                }
                int statusCode = Integer.parseInt(buffer.substringTrimmed(i, blank));
                int i2 = blank;
                if (i2 < indexTo) {
                    reasonPhrase = buffer.substringTrimmed(i2, indexTo);
                } else {
                    reasonPhrase = "";
                }
                return createStatusLine(ver, statusCode, reasonPhrase);
            } catch (NumberFormatException e) {
                throw new ParseException("Unable to parse status code from status line: " + buffer.substring(indexFrom, indexTo));
            } catch (IndexOutOfBoundsException e2) {
                throw new ParseException("Invalid status line: " + buffer.substring(indexFrom, indexTo));
            }
        } else {
            throw new IllegalArgumentException("Parser cursor may not be null");
        }
    }

    /* access modifiers changed from: protected */
    public StatusLine createStatusLine(ProtocolVersion ver, int status, String reason) {
        return new BasicStatusLine(ver, status, reason);
    }

    public static final Header parseHeader(String value, LineParser parser) throws ParseException {
        if (value != null) {
            if (parser == null) {
                parser = DEFAULT;
            }
            CharArrayBuffer buffer = new CharArrayBuffer(value.length());
            buffer.append(value);
            return parser.parseHeader(buffer);
        }
        throw new IllegalArgumentException("Value to parse may not be null");
    }

    public Header parseHeader(CharArrayBuffer buffer) throws ParseException {
        return new BufferedHeader(buffer);
    }

    /* access modifiers changed from: protected */
    public void skipWhitespace(CharArrayBuffer buffer, ParserCursor cursor) {
        int pos = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        while (pos < indexTo && HTTP.isWhitespace(buffer.charAt(pos))) {
            pos++;
        }
        cursor.updatePos(pos);
    }
}
