package android.util;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import libcore.internal.StringPool;

public final class JsonReader implements Closeable {
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private final char[] buffer = new char[1024];
    private int bufferStartColumn = 1;
    private int bufferStartLine = 1;
    private final Reader in;
    private boolean lenient = false;
    private int limit = 0;
    private String name;
    private int pos = 0;
    private boolean skipping;
    private final List<JsonScope> stack = new ArrayList();
    private final StringPool stringPool = new StringPool();
    private JsonToken token;
    private String value;
    private int valueLength;
    private int valuePos;

    public JsonReader(Reader in2) {
        push(JsonScope.EMPTY_DOCUMENT);
        this.skipping = false;
        if (in2 != null) {
            this.in = in2;
            return;
        }
        throw new NullPointerException("in == null");
    }

    public void setLenient(boolean lenient2) {
        this.lenient = lenient2;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public void beginArray() throws IOException {
        expect(JsonToken.BEGIN_ARRAY);
    }

    public void endArray() throws IOException {
        expect(JsonToken.END_ARRAY);
    }

    public void beginObject() throws IOException {
        expect(JsonToken.BEGIN_OBJECT);
    }

    public void endObject() throws IOException {
        expect(JsonToken.END_OBJECT);
    }

    private void expect(JsonToken expected) throws IOException {
        peek();
        if (this.token == expected) {
            advance();
            return;
        }
        throw new IllegalStateException("Expected " + expected + " but was " + peek());
    }

    public boolean hasNext() throws IOException {
        peek();
        return (this.token == JsonToken.END_OBJECT || this.token == JsonToken.END_ARRAY) ? false : true;
    }

    public JsonToken peek() throws IOException {
        if (this.token != null) {
            return this.token;
        }
        switch (peekStack()) {
            case EMPTY_DOCUMENT:
                replaceTop(JsonScope.NONEMPTY_DOCUMENT);
                JsonToken firstToken = nextValue();
                if (this.lenient || this.token == JsonToken.BEGIN_ARRAY || this.token == JsonToken.BEGIN_OBJECT) {
                    return firstToken;
                }
                throw new IOException("Expected JSON document to start with '[' or '{' but was " + this.token);
            case EMPTY_ARRAY:
                return nextInArray(true);
            case NONEMPTY_ARRAY:
                return nextInArray(false);
            case EMPTY_OBJECT:
                return nextInObject(true);
            case DANGLING_NAME:
                return objectValue();
            case NONEMPTY_OBJECT:
                return nextInObject(false);
            case NONEMPTY_DOCUMENT:
                try {
                    JsonToken token2 = nextValue();
                    if (this.lenient) {
                        return token2;
                    }
                    throw syntaxError("Expected EOF");
                } catch (EOFException e) {
                    JsonToken jsonToken = JsonToken.END_DOCUMENT;
                    this.token = jsonToken;
                    return jsonToken;
                }
            case CLOSED:
                throw new IllegalStateException("JsonReader is closed");
            default:
                throw new AssertionError();
        }
    }

    private JsonToken advance() throws IOException {
        peek();
        JsonToken result = this.token;
        this.token = null;
        this.value = null;
        this.name = null;
        return result;
    }

    public String nextName() throws IOException {
        peek();
        if (this.token == JsonToken.NAME) {
            String result = this.name;
            advance();
            return result;
        }
        throw new IllegalStateException("Expected a name but was " + peek());
    }

    public String nextString() throws IOException {
        peek();
        if (this.token == JsonToken.STRING || this.token == JsonToken.NUMBER) {
            String result = this.value;
            advance();
            return result;
        }
        throw new IllegalStateException("Expected a string but was " + peek());
    }

    public boolean nextBoolean() throws IOException {
        peek();
        if (this.token == JsonToken.BOOLEAN) {
            boolean result = this.value == TRUE;
            advance();
            return result;
        }
        throw new IllegalStateException("Expected a boolean but was " + this.token);
    }

    public void nextNull() throws IOException {
        peek();
        if (this.token == JsonToken.NULL) {
            advance();
            return;
        }
        throw new IllegalStateException("Expected null but was " + this.token);
    }

    public double nextDouble() throws IOException {
        peek();
        if (this.token == JsonToken.STRING || this.token == JsonToken.NUMBER) {
            double result = Double.parseDouble(this.value);
            advance();
            return result;
        }
        throw new IllegalStateException("Expected a double but was " + this.token);
    }

    public long nextLong() throws IOException {
        long result;
        peek();
        if (this.token == JsonToken.STRING || this.token == JsonToken.NUMBER) {
            try {
                result = Long.parseLong(this.value);
            } catch (NumberFormatException e) {
                double asDouble = Double.parseDouble(this.value);
                long result2 = (long) asDouble;
                if (((double) result2) == asDouble) {
                    result = result2;
                } else {
                    throw new NumberFormatException(this.value);
                }
            }
            advance();
            return result;
        }
        throw new IllegalStateException("Expected a long but was " + this.token);
    }

    public int nextInt() throws IOException {
        int result;
        peek();
        if (this.token == JsonToken.STRING || this.token == JsonToken.NUMBER) {
            try {
                result = Integer.parseInt(this.value);
            } catch (NumberFormatException e) {
                double asDouble = Double.parseDouble(this.value);
                int result2 = (int) asDouble;
                if (((double) result2) == asDouble) {
                    result = result2;
                } else {
                    throw new NumberFormatException(this.value);
                }
            }
            advance();
            return result;
        }
        throw new IllegalStateException("Expected an int but was " + this.token);
    }

    public void close() throws IOException {
        this.value = null;
        this.token = null;
        this.stack.clear();
        this.stack.add(JsonScope.CLOSED);
        this.in.close();
    }

    public void skipValue() throws IOException {
        this.skipping = true;
        try {
            if (!hasNext() || peek() == JsonToken.END_DOCUMENT) {
                throw new IllegalStateException("No element left to skip");
            }
            int count = 0;
            do {
                JsonToken token2 = advance();
                if (token2 != JsonToken.BEGIN_ARRAY) {
                    if (token2 != JsonToken.BEGIN_OBJECT) {
                        if (token2 == JsonToken.END_ARRAY || token2 == JsonToken.END_OBJECT) {
                            count--;
                            continue;
                        }
                    }
                }
                count++;
                continue;
            } while (count != 0);
        } finally {
            this.skipping = false;
        }
    }

    private JsonScope peekStack() {
        return this.stack.get(this.stack.size() - 1);
    }

    private JsonScope pop() {
        return this.stack.remove(this.stack.size() - 1);
    }

    private void push(JsonScope newTop) {
        this.stack.add(newTop);
    }

    private void replaceTop(JsonScope newTop) {
        this.stack.set(this.stack.size() - 1, newTop);
    }

    private JsonToken nextInArray(boolean firstElement) throws IOException {
        if (firstElement) {
            replaceTop(JsonScope.NONEMPTY_ARRAY);
        } else {
            int nextNonWhitespace = nextNonWhitespace();
            if (nextNonWhitespace != 44) {
                if (nextNonWhitespace == 59) {
                    checkLenient();
                } else if (nextNonWhitespace == 93) {
                    pop();
                    JsonToken jsonToken = JsonToken.END_ARRAY;
                    this.token = jsonToken;
                    return jsonToken;
                } else {
                    throw syntaxError("Unterminated array");
                }
            }
        }
        int nextNonWhitespace2 = nextNonWhitespace();
        if (!(nextNonWhitespace2 == 44 || nextNonWhitespace2 == 59)) {
            if (nextNonWhitespace2 != 93) {
                this.pos--;
                return nextValue();
            } else if (firstElement) {
                pop();
                JsonToken jsonToken2 = JsonToken.END_ARRAY;
                this.token = jsonToken2;
                return jsonToken2;
            }
        }
        checkLenient();
        this.pos--;
        this.value = "null";
        JsonToken jsonToken3 = JsonToken.NULL;
        this.token = jsonToken3;
        return jsonToken3;
    }

    private JsonToken nextInObject(boolean firstElement) throws IOException {
        if (!firstElement) {
            int nextNonWhitespace = nextNonWhitespace();
            if (!(nextNonWhitespace == 44 || nextNonWhitespace == 59)) {
                if (nextNonWhitespace == 125) {
                    pop();
                    JsonToken jsonToken = JsonToken.END_OBJECT;
                    this.token = jsonToken;
                    return jsonToken;
                }
                throw syntaxError("Unterminated object");
            }
        } else if (nextNonWhitespace() != 125) {
            this.pos--;
        } else {
            pop();
            JsonToken jsonToken2 = JsonToken.END_OBJECT;
            this.token = jsonToken2;
            return jsonToken2;
        }
        int quote = nextNonWhitespace();
        if (quote != 34) {
            if (quote != 39) {
                checkLenient();
                this.pos--;
                this.name = nextLiteral(false);
                if (this.name.isEmpty()) {
                    throw syntaxError("Expected name");
                }
                replaceTop(JsonScope.DANGLING_NAME);
                JsonToken jsonToken3 = JsonToken.NAME;
                this.token = jsonToken3;
                return jsonToken3;
            }
            checkLenient();
        }
        this.name = nextString((char) quote);
        replaceTop(JsonScope.DANGLING_NAME);
        JsonToken jsonToken32 = JsonToken.NAME;
        this.token = jsonToken32;
        return jsonToken32;
    }

    private JsonToken objectValue() throws IOException {
        int nextNonWhitespace = nextNonWhitespace();
        if (nextNonWhitespace != 58) {
            if (nextNonWhitespace == 61) {
                checkLenient();
                if ((this.pos < this.limit || fillBuffer(1)) && this.buffer[this.pos] == '>') {
                    this.pos++;
                }
            } else {
                throw syntaxError("Expected ':'");
            }
        }
        replaceTop(JsonScope.NONEMPTY_OBJECT);
        return nextValue();
    }

    private JsonToken nextValue() throws IOException {
        int c = nextNonWhitespace();
        if (c != 34) {
            if (c == 39) {
                checkLenient();
            } else if (c == 91) {
                push(JsonScope.EMPTY_ARRAY);
                JsonToken jsonToken = JsonToken.BEGIN_ARRAY;
                this.token = jsonToken;
                return jsonToken;
            } else if (c != 123) {
                this.pos--;
                return readLiteral();
            } else {
                push(JsonScope.EMPTY_OBJECT);
                JsonToken jsonToken2 = JsonToken.BEGIN_OBJECT;
                this.token = jsonToken2;
                return jsonToken2;
            }
        }
        this.value = nextString((char) c);
        JsonToken jsonToken3 = JsonToken.STRING;
        this.token = jsonToken3;
        return jsonToken3;
    }

    private boolean fillBuffer(int minimum) throws IOException {
        for (int i = 0; i < this.pos; i++) {
            if (this.buffer[i] == 10) {
                this.bufferStartLine++;
                this.bufferStartColumn = 1;
            } else {
                this.bufferStartColumn++;
            }
        }
        if (this.limit != this.pos) {
            this.limit -= this.pos;
            System.arraycopy(this.buffer, this.pos, this.buffer, 0, this.limit);
        } else {
            this.limit = 0;
        }
        this.pos = 0;
        do {
            int read = this.in.read(this.buffer, this.limit, this.buffer.length - this.limit);
            int total = read;
            if (read == -1) {
                return false;
            }
            this.limit += total;
            if (this.bufferStartLine == 1 && this.bufferStartColumn == 1 && this.limit > 0 && this.buffer[0] == 65279) {
                this.pos++;
                this.bufferStartColumn--;
            }
        } while (this.limit < minimum);
        return true;
    }

    private int getLineNumber() {
        int result = this.bufferStartLine;
        for (int i = 0; i < this.pos; i++) {
            if (this.buffer[i] == 10) {
                result++;
            }
        }
        return result;
    }

    private int getColumnNumber() {
        int result = this.bufferStartColumn;
        for (int i = 0; i < this.pos; i++) {
            if (this.buffer[i] == 10) {
                result = 1;
            } else {
                result++;
            }
        }
        return result;
    }

    private int nextNonWhitespace() throws IOException {
        while (true) {
            if (this.pos < this.limit || fillBuffer(1)) {
                char[] cArr = this.buffer;
                int i = this.pos;
                this.pos = i + 1;
                char c = cArr[i];
                if (!(c == 13 || c == ' ')) {
                    if (c == '#') {
                        checkLenient();
                        skipToEndOfLine();
                    } else if (c != '/') {
                        switch (c) {
                            case 9:
                            case 10:
                                break;
                            default:
                                return c;
                        }
                    } else if (this.pos == this.limit && !fillBuffer(1)) {
                        return c;
                    } else {
                        checkLenient();
                        char peek = this.buffer[this.pos];
                        if (peek == '*') {
                            this.pos++;
                            if (skipTo("*/")) {
                                this.pos += 2;
                            } else {
                                throw syntaxError("Unterminated comment");
                            }
                        } else if (peek != '/') {
                            return c;
                        } else {
                            this.pos++;
                            skipToEndOfLine();
                        }
                    }
                }
            } else {
                throw new EOFException("End of input");
            }
        }
    }

    private void checkLenient() throws IOException {
        if (!this.lenient) {
            throw syntaxError("Use JsonReader.setLenient(true) to accept malformed JSON");
        }
    }

    private void skipToEndOfLine() throws IOException {
        char c;
        do {
            if (this.pos < this.limit || fillBuffer(1)) {
                char[] cArr = this.buffer;
                int i = this.pos;
                this.pos = i + 1;
                c = cArr[i];
                if (c == 13) {
                    return;
                }
            } else {
                return;
            }
        } while (c != 10);
    }

    private boolean skipTo(String toFind) throws IOException {
        while (true) {
            int c = 0;
            if (this.pos + toFind.length() > this.limit && !fillBuffer(toFind.length())) {
                return false;
            }
            while (true) {
                int c2 = c;
                if (c2 >= toFind.length()) {
                    return true;
                }
                if (this.buffer[this.pos + c2] != toFind.charAt(c2)) {
                    break;
                }
                c = c2 + 1;
            }
            this.pos++;
        }
    }

    private String nextString(char quote) throws IOException {
        StringBuilder builder = null;
        do {
            int start = this.pos;
            while (this.pos < this.limit) {
                char[] cArr = this.buffer;
                int i = this.pos;
                this.pos = i + 1;
                char c = cArr[i];
                if (c == quote) {
                    if (this.skipping) {
                        return "skipped!";
                    }
                    if (builder == null) {
                        return this.stringPool.get(this.buffer, start, (this.pos - start) - 1);
                    }
                    builder.append(this.buffer, start, (this.pos - start) - 1);
                    return builder.toString();
                } else if (c == '\\') {
                    if (builder == null) {
                        builder = new StringBuilder();
                    }
                    builder.append(this.buffer, start, (this.pos - start) - 1);
                    builder.append(readEscapeCharacter());
                    start = this.pos;
                }
            }
            if (builder == null) {
                builder = new StringBuilder();
            }
            builder.append(this.buffer, start, this.pos - start);
        } while (fillBuffer(1) != 0);
        throw syntaxError("Unterminated string");
    }

    private String nextLiteral(boolean assignOffsetsOnly) throws IOException {
        String result;
        this.valuePos = -1;
        this.valueLength = 0;
        StringBuilder builder = null;
        int i = 0;
        while (true) {
            if (this.pos + i < this.limit) {
                switch (this.buffer[this.pos + i]) {
                    case 9:
                    case 10:
                    case 12:
                    case 13:
                    case ' ':
                    case ',':
                    case ':':
                    case '[':
                    case ']':
                    case '{':
                    case '}':
                        break;
                    case '#':
                    case '/':
                    case ';':
                    case '=':
                    case '\\':
                        checkLenient();
                        break;
                    default:
                        i++;
                        continue;
                }
            } else if (i >= this.buffer.length) {
                if (builder == null) {
                    builder = new StringBuilder();
                }
                builder.append(this.buffer, this.pos, i);
                this.valueLength += i;
                this.pos += i;
                i = 0;
                if (!fillBuffer(1)) {
                }
            } else if (!fillBuffer(i + 1)) {
                this.buffer[this.limit] = 0;
            }
        }
        if (assignOffsetsOnly && builder == null) {
            this.valuePos = this.pos;
            result = null;
        } else if (this.skipping) {
            result = "skipped!";
        } else if (builder == null) {
            result = this.stringPool.get(this.buffer, this.pos, i);
        } else {
            builder.append(this.buffer, this.pos, i);
            result = builder.toString();
        }
        this.valueLength += i;
        this.pos += i;
        return result;
    }

    public String toString() {
        return getClass().getSimpleName() + " near " + getSnippet();
    }

    private char readEscapeCharacter() throws IOException {
        if (this.pos != this.limit || fillBuffer(1)) {
            char[] cArr = this.buffer;
            int i = this.pos;
            this.pos = i + 1;
            char escaped = cArr[i];
            if (escaped == 'b') {
                return 8;
            }
            if (escaped == 'f') {
                return 12;
            }
            if (escaped == 'n') {
                return 10;
            }
            if (escaped == 'r') {
                return 13;
            }
            switch (escaped) {
                case 't':
                    return 9;
                case 'u':
                    if (this.pos + 4 <= this.limit || fillBuffer(4)) {
                        String hex = this.stringPool.get(this.buffer, this.pos, 4);
                        this.pos += 4;
                        return (char) Integer.parseInt(hex, 16);
                    }
                    throw syntaxError("Unterminated escape sequence");
                default:
                    return escaped;
            }
        } else {
            throw syntaxError("Unterminated escape sequence");
        }
    }

    private JsonToken readLiteral() throws IOException {
        this.value = nextLiteral(true);
        if (this.valueLength != 0) {
            this.token = decodeLiteral();
            if (this.token == JsonToken.STRING) {
                checkLenient();
            }
            return this.token;
        }
        throw syntaxError("Expected literal value");
    }

    private JsonToken decodeLiteral() throws IOException {
        if (this.valuePos == -1) {
            return JsonToken.STRING;
        }
        if (this.valueLength == 4 && (('n' == this.buffer[this.valuePos] || 'N' == this.buffer[this.valuePos]) && (('u' == this.buffer[this.valuePos + 1] || 'U' == this.buffer[this.valuePos + 1]) && (('l' == this.buffer[this.valuePos + 2] || 'L' == this.buffer[this.valuePos + 2]) && ('l' == this.buffer[this.valuePos + 3] || 'L' == this.buffer[this.valuePos + 3]))))) {
            this.value = "null";
            return JsonToken.NULL;
        } else if (this.valueLength == 4 && (('t' == this.buffer[this.valuePos] || 'T' == this.buffer[this.valuePos]) && (('r' == this.buffer[this.valuePos + 1] || 'R' == this.buffer[this.valuePos + 1]) && (('u' == this.buffer[this.valuePos + 2] || 'U' == this.buffer[this.valuePos + 2]) && ('e' == this.buffer[this.valuePos + 3] || 'E' == this.buffer[this.valuePos + 3]))))) {
            this.value = TRUE;
            return JsonToken.BOOLEAN;
        } else if (this.valueLength == 5 && (('f' == this.buffer[this.valuePos] || 'F' == this.buffer[this.valuePos]) && (('a' == this.buffer[this.valuePos + 1] || 'A' == this.buffer[this.valuePos + 1]) && (('l' == this.buffer[this.valuePos + 2] || 'L' == this.buffer[this.valuePos + 2]) && (('s' == this.buffer[this.valuePos + 3] || 'S' == this.buffer[this.valuePos + 3]) && ('e' == this.buffer[this.valuePos + 4] || 'E' == this.buffer[this.valuePos + 4])))))) {
            this.value = FALSE;
            return JsonToken.BOOLEAN;
        } else {
            this.value = this.stringPool.get(this.buffer, this.valuePos, this.valueLength);
            return decodeNumber(this.buffer, this.valuePos, this.valueLength);
        }
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v0, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v13, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v15, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v16, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v17, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v4, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v6, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v8, types: [char] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r1v9, types: [char] */
    private JsonToken decodeNumber(char[] chars, int offset, int length) {
        int c;
        int i;
        int i2 = offset;
        int c2 = chars[i2];
        if (c2 == '-') {
            i2++;
            c2 = chars[i2];
        }
        if (c2 == 48) {
            i = i2 + 1;
            c = chars[i];
        } else if (c2 < 49 || c2 > 57) {
            return JsonToken.STRING;
        } else {
            i = i2 + 1;
            c = chars[i];
            while (c >= 48 && c <= 57) {
                i++;
                c = chars[i];
            }
        }
        if (c == 46) {
            int i3 = i + 1;
            char c3 = chars[i3];
            while (c >= 48 && c <= 57) {
                i3 = i + 1;
                c3 = chars[i3];
            }
        }
        if (c == 101 || c == 69) {
            int i4 = i + 1;
            int c4 = chars[i4];
            if (c4 == '+' || c4 == '-') {
                i4++;
                c4 = chars[i4];
            }
            if (c4 < 48 || c4 > 57) {
                return JsonToken.STRING;
            }
            int i5 = i4 + 1;
            int c5 = chars[i5];
            while (c5 >= 48 && c5 <= 57) {
                i5 = i + 1;
                c5 = chars[i5];
            }
        }
        if (i == offset + length) {
            return JsonToken.NUMBER;
        }
        return JsonToken.STRING;
    }

    private IOException syntaxError(String message) throws IOException {
        throw new MalformedJsonException(message + " at line " + getLineNumber() + " column " + getColumnNumber());
    }

    private CharSequence getSnippet() {
        StringBuilder snippet = new StringBuilder();
        int beforePos = Math.min(this.pos, 20);
        snippet.append(this.buffer, this.pos - beforePos, beforePos);
        snippet.append(this.buffer, this.pos, Math.min(this.limit - this.pos, 20));
        return snippet;
    }
}
