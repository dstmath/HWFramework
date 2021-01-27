package com.huawei.gson.stream;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

public class JsonWriter implements Closeable, Flushable {
    private static final String[] HTML_SAFE_REPLACEMENT_CHARS;
    private static final String[] REPLACEMENT_CHARS = new String[128];
    private String deferredName;
    private boolean htmlSafe;
    private String indent;
    private boolean lenient;
    private final Writer out;
    private String separator;
    private boolean serializeNulls;
    private int[] stack = new int[32];
    private int stackSize = 0;

    static {
        for (int i = 0; i <= 31; i++) {
            REPLACEMENT_CHARS[i] = String.format("\\u%04x", Integer.valueOf(i));
        }
        String[] strArr = REPLACEMENT_CHARS;
        strArr[34] = "\\\"";
        strArr[92] = "\\\\";
        strArr[9] = "\\t";
        strArr[8] = "\\b";
        strArr[10] = "\\n";
        strArr[13] = "\\r";
        strArr[12] = "\\f";
        HTML_SAFE_REPLACEMENT_CHARS = (String[]) strArr.clone();
        String[] strArr2 = HTML_SAFE_REPLACEMENT_CHARS;
        strArr2[60] = "\\u003c";
        strArr2[62] = "\\u003e";
        strArr2[38] = "\\u0026";
        strArr2[61] = "\\u003d";
        strArr2[39] = "\\u0027";
    }

    public JsonWriter(Writer out2) {
        push(6);
        this.separator = ":";
        this.serializeNulls = true;
        if (out2 != null) {
            this.out = out2;
            return;
        }
        throw new NullPointerException("out == null");
    }

    public final void setIndent(String indent2) {
        if (indent2.length() == 0) {
            this.indent = null;
            this.separator = ":";
            return;
        }
        this.indent = indent2;
        this.separator = ": ";
    }

    public final void setLenient(boolean lenient2) {
        this.lenient = lenient2;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public final void setHtmlSafe(boolean htmlSafe2) {
        this.htmlSafe = htmlSafe2;
    }

    public final boolean isHtmlSafe() {
        return this.htmlSafe;
    }

    public final void setSerializeNulls(boolean serializeNulls2) {
        this.serializeNulls = serializeNulls2;
    }

    public final boolean getSerializeNulls() {
        return this.serializeNulls;
    }

    public JsonWriter beginArray() throws IOException {
        writeDeferredName();
        return open(1, '[');
    }

    public JsonWriter endArray() throws IOException {
        return close(1, 2, ']');
    }

    public JsonWriter beginObject() throws IOException {
        writeDeferredName();
        return open(3, '{');
    }

    public JsonWriter endObject() throws IOException {
        return close(3, 5, '}');
    }

    private JsonWriter open(int empty, char openBracket) throws IOException {
        beforeValue();
        push(empty);
        this.out.write(openBracket);
        return this;
    }

    private JsonWriter close(int empty, int nonempty, char closeBracket) throws IOException {
        int context = peek();
        if (context != nonempty && context != empty) {
            throw new IllegalStateException("Nesting problem.");
        } else if (this.deferredName == null) {
            this.stackSize--;
            if (context == nonempty) {
                newline();
            }
            this.out.write(closeBracket);
            return this;
        } else {
            throw new IllegalStateException("Dangling name: " + this.deferredName);
        }
    }

    private void push(int newTop) {
        int i = this.stackSize;
        int[] iArr = this.stack;
        if (i == iArr.length) {
            this.stack = Arrays.copyOf(iArr, i * 2);
        }
        int[] iArr2 = this.stack;
        int i2 = this.stackSize;
        this.stackSize = i2 + 1;
        iArr2[i2] = newTop;
    }

    private int peek() {
        int i = this.stackSize;
        if (i != 0) {
            return this.stack[i - 1];
        }
        throw new IllegalStateException("JsonWriter is closed.");
    }

    private void replaceTop(int topOfStack) {
        this.stack[this.stackSize - 1] = topOfStack;
    }

    public JsonWriter name(String name) throws IOException {
        if (name == null) {
            throw new NullPointerException("name == null");
        } else if (this.deferredName != null) {
            throw new IllegalStateException();
        } else if (this.stackSize != 0) {
            this.deferredName = name;
            return this;
        } else {
            throw new IllegalStateException("JsonWriter is closed.");
        }
    }

    private void writeDeferredName() throws IOException {
        if (this.deferredName != null) {
            beforeName();
            string(this.deferredName);
            this.deferredName = null;
        }
    }

    public JsonWriter value(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        beforeValue();
        string(value);
        return this;
    }

    public JsonWriter jsonValue(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        beforeValue();
        this.out.append((CharSequence) value);
        return this;
    }

    public JsonWriter nullValue() throws IOException {
        if (this.deferredName != null) {
            if (this.serializeNulls) {
                writeDeferredName();
            } else {
                this.deferredName = null;
                return this;
            }
        }
        beforeValue();
        this.out.write("null");
        return this;
    }

    public JsonWriter value(boolean value) throws IOException {
        writeDeferredName();
        beforeValue();
        this.out.write(value ? "true" : "false");
        return this;
    }

    public JsonWriter value(Boolean value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        beforeValue();
        this.out.write(value.booleanValue() ? "true" : "false");
        return this;
    }

    public JsonWriter value(double value) throws IOException {
        writeDeferredName();
        if (this.lenient || (!Double.isNaN(value) && !Double.isInfinite(value))) {
            beforeValue();
            this.out.append((CharSequence) Double.toString(value));
            return this;
        }
        throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }

    public JsonWriter value(long value) throws IOException {
        writeDeferredName();
        beforeValue();
        this.out.write(Long.toString(value));
        return this;
    }

    public JsonWriter value(Number value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        String string = value.toString();
        if (this.lenient || (!string.equals("-Infinity") && !string.equals("Infinity") && !string.equals("NaN"))) {
            beforeValue();
            this.out.append((CharSequence) string);
            return this;
        }
        throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }

    @Override // java.io.Flushable
    public void flush() throws IOException {
        if (this.stackSize != 0) {
            this.out.flush();
            return;
        }
        throw new IllegalStateException("JsonWriter is closed.");
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.out.close();
        int size = this.stackSize;
        if (size > 1 || (size == 1 && this.stack[size - 1] != 7)) {
            throw new IOException("Incomplete document");
        }
        this.stackSize = 0;
    }

    private void string(String value) throws IOException {
        String replacement;
        String[] replacements = this.htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
        this.out.write(34);
        int last = 0;
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (c < 128) {
                replacement = replacements[c];
                if (replacement == null) {
                }
            } else if (c == 8232) {
                replacement = "\\u2028";
            } else if (c == 8233) {
                replacement = "\\u2029";
            }
            if (last < i) {
                this.out.write(value, last, i - last);
            }
            this.out.write(replacement);
            last = i + 1;
        }
        if (last < length) {
            this.out.write(value, last, length - last);
        }
        this.out.write(34);
    }

    private void newline() throws IOException {
        if (this.indent != null) {
            this.out.write(10);
            int size = this.stackSize;
            for (int i = 1; i < size; i++) {
                this.out.write(this.indent);
            }
        }
    }

    private void beforeName() throws IOException {
        int context = peek();
        if (context == 5) {
            this.out.write(44);
        } else if (context != 3) {
            throw new IllegalStateException("Nesting problem.");
        }
        newline();
        replaceTop(4);
    }

    private void beforeValue() throws IOException {
        int peek = peek();
        if (peek == 1) {
            replaceTop(2);
            newline();
        } else if (peek == 2) {
            this.out.append(',');
            newline();
        } else if (peek != 4) {
            if (peek != 6) {
                if (peek != 7) {
                    throw new IllegalStateException("Nesting problem.");
                } else if (!this.lenient) {
                    throw new IllegalStateException("JSON must have only one top-level value.");
                }
            }
            replaceTop(7);
        } else {
            this.out.append((CharSequence) this.separator);
            replaceTop(5);
        }
    }
}
