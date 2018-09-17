package org.json;

public class JSONTokener {
    private final String in;
    private int pos;

    public JSONTokener(String in) {
        if (in != null && in.startsWith("﻿")) {
            in = in.substring(1);
        }
        this.in = in;
    }

    public Object nextValue() throws JSONException {
        int c = nextCleanInternal();
        switch (c) {
            case -1:
                throw syntaxError("End of input");
            case 34:
            case 39:
                return nextString((char) c);
            case 91:
                return readArray();
            case 123:
                return readObject();
            default:
                this.pos--;
                return readLiteral();
        }
    }

    private int nextCleanInternal() throws JSONException {
        while (this.pos < this.in.length()) {
            String str = this.in;
            int i = this.pos;
            this.pos = i + 1;
            int c = str.charAt(i);
            switch (c) {
                case 9:
                case 10:
                case 13:
                case 32:
                    break;
                case 35:
                    skipToEndOfLine();
                    break;
                case 47:
                    if (this.pos == this.in.length()) {
                        return c;
                    }
                    switch (this.in.charAt(this.pos)) {
                        case '*':
                            this.pos++;
                            int commentEnd = this.in.indexOf("*/", this.pos);
                            if (commentEnd != -1) {
                                this.pos = commentEnd + 2;
                                break;
                            }
                            throw syntaxError("Unterminated comment");
                        case '/':
                            this.pos++;
                            skipToEndOfLine();
                            break;
                        default:
                            return c;
                    }
                default:
                    return c;
            }
        }
        return -1;
    }

    private void skipToEndOfLine() {
        while (this.pos < this.in.length()) {
            char c = this.in.charAt(this.pos);
            if (c == 13 || c == 10) {
                this.pos++;
                return;
            }
            this.pos++;
        }
    }

    public String nextString(char quote) throws JSONException {
        StringBuilder builder = null;
        int start = this.pos;
        while (this.pos < this.in.length()) {
            String str = this.in;
            int i = this.pos;
            this.pos = i + 1;
            char c = str.charAt(i);
            if (c == quote) {
                if (builder == null) {
                    return new String(this.in.substring(start, this.pos - 1));
                }
                builder.append(this.in, start, this.pos - 1);
                return builder.toString();
            } else if (c == '\\') {
                if (this.pos == this.in.length()) {
                    throw syntaxError("Unterminated escape sequence");
                }
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(this.in, start, this.pos - 1);
                builder.append(readEscapeCharacter());
                start = this.pos;
            }
        }
        throw syntaxError("Unterminated string");
    }

    private char readEscapeCharacter() throws JSONException {
        String str = this.in;
        int i = this.pos;
        this.pos = i + 1;
        char escaped = str.charAt(i);
        switch (escaped) {
            case 'b':
                return 8;
            case 'f':
                return 12;
            case 'n':
                return 10;
            case 'r':
                return 13;
            case 't':
                return 9;
            case 'u':
                if (this.pos + 4 > this.in.length()) {
                    throw syntaxError("Unterminated escape sequence");
                }
                String hex = this.in.substring(this.pos, this.pos + 4);
                this.pos += 4;
                try {
                    return (char) Integer.parseInt(hex, 16);
                } catch (NumberFormatException e) {
                    throw syntaxError("Invalid escape sequence: " + hex);
                }
            default:
                return escaped;
        }
    }

    private Object readLiteral() throws JSONException {
        String literal = nextToInternal("{}[]/\\:,=;# \t\f");
        if (literal.length() == 0) {
            throw syntaxError("Expected literal value");
        } else if ("null".equalsIgnoreCase(literal)) {
            return JSONObject.NULL;
        } else {
            if ("true".equalsIgnoreCase(literal)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(literal)) {
                return Boolean.FALSE;
            }
            if (literal.indexOf(46) == -1) {
                int base = 10;
                String number = literal;
                if (literal.startsWith("0x") || literal.startsWith("0X")) {
                    number = literal.substring(2);
                    base = 16;
                } else if (literal.startsWith(AndroidHardcodedSystemProperties.JAVA_VERSION) && literal.length() > 1) {
                    number = literal.substring(1);
                    base = 8;
                }
                try {
                    long longValue = Long.parseLong(number, base);
                    if (longValue > 2147483647L || longValue < -2147483648L) {
                        return Long.valueOf(longValue);
                    }
                    return Integer.valueOf((int) longValue);
                } catch (NumberFormatException e) {
                }
            }
            try {
                return Double.valueOf(literal);
            } catch (NumberFormatException e2) {
                return new String(literal);
            }
        }
    }

    private String nextToInternal(String excluded) {
        int start = this.pos;
        while (this.pos < this.in.length()) {
            char c = this.in.charAt(this.pos);
            if (c == 13 || c == 10 || excluded.indexOf(c) != -1) {
                return this.in.substring(start, this.pos);
            }
            this.pos++;
        }
        return this.in.substring(start);
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0017 A:{LOOP_START} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private JSONObject readObject() throws JSONException {
        JSONObject result = new JSONObject();
        int first = nextCleanInternal();
        if (first == 125) {
            return result;
        }
        if (first != -1) {
            this.pos--;
        }
        while (true) {
            Object name = nextValue();
            if (name instanceof String) {
                int separator = nextCleanInternal();
                if (separator == 58 || separator == 61) {
                    if (this.pos < this.in.length() && this.in.charAt(this.pos) == '>') {
                        this.pos++;
                    }
                    result.put((String) name, nextValue());
                    switch (nextCleanInternal()) {
                        case 44:
                        case 59:
                            break;
                        case 125:
                            return result;
                        default:
                            throw syntaxError("Unterminated object");
                    }
                }
                throw syntaxError("Expected ':' after " + name);
            } else if (name == null) {
                throw syntaxError("Names cannot be null");
            } else {
                throw syntaxError("Names must be strings, but " + name + " is of type " + name.getClass().getName());
            }
        }
    }

    private JSONArray readArray() throws JSONException {
        JSONArray result = new JSONArray();
        boolean hasTrailingSeparator = false;
        while (true) {
            switch (nextCleanInternal()) {
                case -1:
                    throw syntaxError("Unterminated array");
                case 44:
                case 59:
                    result.put(null);
                    hasTrailingSeparator = true;
                    break;
                case 93:
                    if (hasTrailingSeparator) {
                        result.put(null);
                    }
                    return result;
                default:
                    this.pos--;
                    result.put(nextValue());
                    switch (nextCleanInternal()) {
                        case 44:
                        case 59:
                            hasTrailingSeparator = true;
                            break;
                        case 93:
                            return result;
                        default:
                            throw syntaxError("Unterminated array");
                    }
            }
        }
    }

    public JSONException syntaxError(String message) {
        return new JSONException(message + this);
    }

    public String toString() {
        return " at character " + this.pos + " of " + this.in;
    }

    public boolean more() {
        return this.pos < this.in.length();
    }

    public char next() {
        if (this.pos >= this.in.length()) {
            return 0;
        }
        String str = this.in;
        int i = this.pos;
        this.pos = i + 1;
        return str.charAt(i);
    }

    public char next(char c) throws JSONException {
        char result = next();
        if (result == c) {
            return result;
        }
        throw syntaxError("Expected " + c + " but was " + result);
    }

    public char nextClean() throws JSONException {
        int nextCleanInt = nextCleanInternal();
        return nextCleanInt == -1 ? 0 : (char) nextCleanInt;
    }

    public String next(int length) throws JSONException {
        if (this.pos + length > this.in.length()) {
            throw syntaxError(length + " is out of bounds");
        }
        String result = this.in.substring(this.pos, this.pos + length);
        this.pos += length;
        return result;
    }

    public String nextTo(String excluded) {
        if (excluded != null) {
            return nextToInternal(excluded).trim();
        }
        throw new NullPointerException("excluded == null");
    }

    public String nextTo(char excluded) {
        return nextToInternal(String.valueOf(excluded)).trim();
    }

    public void skipPast(String thru) {
        int thruStart = this.in.indexOf(thru, this.pos);
        this.pos = thruStart == -1 ? this.in.length() : thru.length() + thruStart;
    }

    public char skipTo(char to) {
        int index = this.in.indexOf(to, this.pos);
        if (index == -1) {
            return 0;
        }
        this.pos = index;
        return to;
    }

    public void back() {
        int i = this.pos - 1;
        this.pos = i;
        if (i == -1) {
            this.pos = 0;
        }
    }

    public static int dehexchar(char hex) {
        if (hex >= '0' && hex <= '9') {
            return hex - 48;
        }
        if (hex >= 'A' && hex <= 'F') {
            return (hex - 65) + 10;
        }
        if (hex < 'a' || hex > 'f') {
            return -1;
        }
        return (hex - 97) + 10;
    }
}
