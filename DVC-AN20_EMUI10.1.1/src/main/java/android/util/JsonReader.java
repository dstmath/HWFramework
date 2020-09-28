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
        JsonToken jsonToken = this.token;
        if (jsonToken != null) {
            return jsonToken;
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
                    JsonToken jsonToken2 = JsonToken.END_DOCUMENT;
                    this.token = jsonToken2;
                    return jsonToken2;
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

    @Override // java.io.Closeable, java.lang.AutoCloseable
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
        List<JsonScope> list = this.stack;
        return list.get(list.size() - 1);
    }

    private JsonScope pop() {
        List<JsonScope> list = this.stack;
        return list.remove(list.size() - 1);
    }

    private void push(JsonScope newTop) {
        this.stack.add(newTop);
    }

    private void replaceTop(JsonScope newTop) {
        List<JsonScope> list = this.stack;
        list.set(list.size() - 1, newTop);
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
                if (this.pos < this.limit || fillBuffer(1)) {
                    char[] cArr = this.buffer;
                    int i = this.pos;
                    if (cArr[i] == '>') {
                        this.pos = i + 1;
                    }
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
        int i;
        int i2;
        int i3 = 0;
        while (true) {
            i = this.pos;
            if (i3 >= i) {
                break;
            }
            if (this.buffer[i3] == '\n') {
                this.bufferStartLine++;
                this.bufferStartColumn = 1;
            } else {
                this.bufferStartColumn++;
            }
            i3++;
        }
        int i4 = this.limit;
        if (i4 != i) {
            this.limit = i4 - i;
            char[] cArr = this.buffer;
            System.arraycopy(cArr, i, cArr, 0, this.limit);
        } else {
            this.limit = 0;
        }
        this.pos = 0;
        do {
            Reader reader = this.in;
            char[] cArr2 = this.buffer;
            int i5 = this.limit;
            int total = reader.read(cArr2, i5, cArr2.length - i5);
            if (total == -1) {
                return false;
            }
            this.limit += total;
            if (this.bufferStartLine == 1 && (i2 = this.bufferStartColumn) == 1 && this.limit > 0 && this.buffer[0] == 65279) {
                this.pos++;
                this.bufferStartColumn = i2 - 1;
            }
        } while (this.limit < minimum);
        return true;
    }

    private int getLineNumber() {
        int result = this.bufferStartLine;
        for (int i = 0; i < this.pos; i++) {
            if (this.buffer[i] == '\n') {
                result++;
            }
        }
        return result;
    }

    private int getColumnNumber() {
        int result = this.bufferStartColumn;
        for (int i = 0; i < this.pos; i++) {
            if (this.buffer[i] == '\n') {
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
                if (!(c == '\t' || c == '\n' || c == '\r' || c == ' ')) {
                    if (c == '#') {
                        checkLenient();
                        skipToEndOfLine();
                    } else if (c != '/') {
                        return c;
                    } else {
                        if (this.pos == this.limit && !fillBuffer(1)) {
                            return c;
                        }
                        checkLenient();
                        char[] cArr2 = this.buffer;
                        int i2 = this.pos;
                        char peek = cArr2[i2];
                        if (peek == '*') {
                            this.pos = i2 + 1;
                            if (skipTo("*/")) {
                                this.pos += 2;
                            } else {
                                throw syntaxError("Unterminated comment");
                            }
                        } else if (peek != '/') {
                            return c;
                        } else {
                            this.pos = i2 + 1;
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
                if (c == '\r') {
                    return;
                }
            } else {
                return;
            }
        } while (c != '\n');
    }

    private boolean skipTo(String toFind) throws IOException {
        while (true) {
            if (this.pos + toFind.length() > this.limit && !fillBuffer(toFind.length())) {
                return false;
            }
            for (int c = 0; c < toFind.length(); c++) {
                if (this.buffer[this.pos + c] != toFind.charAt(c)) {
                    this.pos++;
                }
            }
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0054, code lost:
        if (r0 != null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0056, code lost:
        r0 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005c, code lost:
        r0.append(r7.buffer, r1, r7.pos - r1);
     */
    private String nextString(char quote) throws IOException {
        StringBuilder builder = null;
        do {
            int start = this.pos;
            while (true) {
                int i = this.pos;
                if (i >= this.limit) {
                    break;
                }
                char[] cArr = this.buffer;
                this.pos = i + 1;
                char c = cArr[i];
                if (c == quote) {
                    if (this.skipping) {
                        return "skipped!";
                    }
                    if (builder == null) {
                        return this.stringPool.get(cArr, start, (this.pos - start) - 1);
                    }
                    builder.append(cArr, start, (this.pos - start) - 1);
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
        } while (fillBuffer(1));
        throw syntaxError("Unterminated string");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x004f, code lost:
        checkLenient();
     */
    private String nextLiteral(boolean assignOffsetsOnly) throws IOException {
        String result;
        StringBuilder builder = null;
        this.valuePos = -1;
        this.valueLength = 0;
        int i = 0;
        while (true) {
            int i2 = this.pos;
            if (i2 + i < this.limit) {
                char c = this.buffer[i2 + i];
                if (!(c == '\t' || c == '\n' || c == '\f' || c == '\r' || c == ' ')) {
                    if (c != '#') {
                        if (c != ',') {
                            if (!(c == '/' || c == '=')) {
                                if (!(c == '{' || c == '}' || c == ':')) {
                                    if (c != ';') {
                                        switch (c) {
                                            case '[':
                                            case ']':
                                                break;
                                            case '\\':
                                                break;
                                            default:
                                                i++;
                                        }
                                    }
                                }
                            }
                        }
                    }
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
        return getClass().getSimpleName() + " near " + ((Object) getSnippet());
    }

    private char readEscapeCharacter() throws IOException {
        if (this.pos != this.limit || fillBuffer(1)) {
            char[] cArr = this.buffer;
            int i = this.pos;
            this.pos = i + 1;
            char escaped = cArr[i];
            if (escaped == 'b') {
                return '\b';
            }
            if (escaped == 'f') {
                return '\f';
            }
            if (escaped == 'n') {
                return '\n';
            }
            if (escaped == 'r') {
                return '\r';
            }
            if (escaped == 't') {
                return '\t';
            }
            if (escaped != 'u') {
                return escaped;
            }
            if (this.pos + 4 <= this.limit || fillBuffer(4)) {
                String hex = this.stringPool.get(this.buffer, this.pos, 4);
                this.pos += 4;
                return (char) Integer.parseInt(hex, 16);
            }
            throw syntaxError("Unterminated escape sequence");
        }
        throw syntaxError("Unterminated escape sequence");
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
        int i = this.valuePos;
        if (i == -1) {
            return JsonToken.STRING;
        }
        if (this.valueLength == 4) {
            char[] cArr = this.buffer;
            if ('n' == cArr[i] || 'N' == cArr[i]) {
                char[] cArr2 = this.buffer;
                int i2 = this.valuePos;
                if ('u' == cArr2[i2 + 1] || 'U' == cArr2[i2 + 1]) {
                    char[] cArr3 = this.buffer;
                    int i3 = this.valuePos;
                    if ('l' == cArr3[i3 + 2] || 'L' == cArr3[i3 + 2]) {
                        char[] cArr4 = this.buffer;
                        int i4 = this.valuePos;
                        if ('l' == cArr4[i4 + 3] || 'L' == cArr4[i4 + 3]) {
                            this.value = "null";
                            return JsonToken.NULL;
                        }
                    }
                }
            }
        }
        if (this.valueLength == 4) {
            char[] cArr5 = this.buffer;
            int i5 = this.valuePos;
            if ('t' == cArr5[i5] || 'T' == cArr5[i5]) {
                char[] cArr6 = this.buffer;
                int i6 = this.valuePos;
                if ('r' == cArr6[i6 + 1] || 'R' == cArr6[i6 + 1]) {
                    char[] cArr7 = this.buffer;
                    int i7 = this.valuePos;
                    if ('u' == cArr7[i7 + 2] || 'U' == cArr7[i7 + 2]) {
                        char[] cArr8 = this.buffer;
                        int i8 = this.valuePos;
                        if ('e' == cArr8[i8 + 3] || 'E' == cArr8[i8 + 3]) {
                            this.value = TRUE;
                            return JsonToken.BOOLEAN;
                        }
                    }
                }
            }
        }
        if (this.valueLength == 5) {
            char[] cArr9 = this.buffer;
            int i9 = this.valuePos;
            if ('f' == cArr9[i9] || 'F' == cArr9[i9]) {
                char[] cArr10 = this.buffer;
                int i10 = this.valuePos;
                if ('a' == cArr10[i10 + 1] || 'A' == cArr10[i10 + 1]) {
                    char[] cArr11 = this.buffer;
                    int i11 = this.valuePos;
                    if ('l' == cArr11[i11 + 2] || 'L' == cArr11[i11 + 2]) {
                        char[] cArr12 = this.buffer;
                        int i12 = this.valuePos;
                        if ('s' == cArr12[i12 + 3] || 'S' == cArr12[i12 + 3]) {
                            char[] cArr13 = this.buffer;
                            int i13 = this.valuePos;
                            if ('e' == cArr13[i13 + 4] || 'E' == cArr13[i13 + 4]) {
                                this.value = FALSE;
                                return JsonToken.BOOLEAN;
                            }
                        }
                    }
                }
            }
        }
        this.value = this.stringPool.get(this.buffer, this.valuePos, this.valueLength);
        return decodeNumber(this.buffer, this.valuePos, this.valueLength);
    }

    private JsonToken decodeNumber(char[] chars, int offset, int length) {
        char c;
        int i;
        int i2 = offset;
        char c2 = chars[i2];
        if (c2 == '-') {
            i2++;
            c2 = chars[i2];
        }
        if (c2 == '0') {
            i = i2 + 1;
            c = chars[i];
        } else if (c2 < '1' || c2 > '9') {
            return JsonToken.STRING;
        } else {
            i = i2 + 1;
            c = chars[i];
            while (c >= '0' && c <= '9') {
                i++;
                c = chars[i];
            }
        }
        if (c == '.') {
            int i3 = i + 1;
            char c3 = chars[i3];
            while (c >= '0' && c <= '9') {
                i3 = i + 1;
                c3 = chars[i3];
            }
        }
        if (c == 'e' || c == 'E') {
            int i4 = i + 1;
            char c4 = chars[i4];
            if (c4 == '+' || c4 == '-') {
                i4++;
                c4 = chars[i4];
            }
            if (c4 < '0' || c4 > '9') {
                return JsonToken.STRING;
            }
            int i5 = i4 + 1;
            char c5 = chars[i5];
            while (c5 >= '0' && c5 <= '9') {
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
