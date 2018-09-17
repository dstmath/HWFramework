package com.google.gson.stream;

import com.android.server.devicepolicy.StorageUtils;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

public class JsonWriter implements Closeable, Flushable {
    private static final String[] HTML_SAFE_REPLACEMENT_CHARS = ((String[]) REPLACEMENT_CHARS.clone());
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
            REPLACEMENT_CHARS[i] = String.format("\\u%04x", new Object[]{Integer.valueOf(i)});
        }
        REPLACEMENT_CHARS[34] = "\\\"";
        REPLACEMENT_CHARS[92] = "\\\\";
        REPLACEMENT_CHARS[9] = "\\t";
        REPLACEMENT_CHARS[8] = "\\b";
        REPLACEMENT_CHARS[10] = "\\n";
        REPLACEMENT_CHARS[13] = "\\r";
        REPLACEMENT_CHARS[12] = "\\f";
        HTML_SAFE_REPLACEMENT_CHARS[60] = "\\u003c";
        HTML_SAFE_REPLACEMENT_CHARS[62] = "\\u003e";
        HTML_SAFE_REPLACEMENT_CHARS[38] = "\\u0026";
        HTML_SAFE_REPLACEMENT_CHARS[61] = "\\u003d";
        HTML_SAFE_REPLACEMENT_CHARS[39] = "\\u0027";
    }

    public JsonWriter(Writer out) {
        push(6);
        this.separator = ":";
        this.serializeNulls = true;
        if (out != null) {
            this.out = out;
            return;
        }
        throw new NullPointerException("out == null");
    }

    public final void setIndent(String indent) {
        if (indent.length() != 0) {
            this.indent = indent;
            this.separator = ": ";
            return;
        }
        this.indent = null;
        this.separator = ":";
    }

    public final void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public final void setHtmlSafe(boolean htmlSafe) {
        this.htmlSafe = htmlSafe;
    }

    public final boolean isHtmlSafe() {
        return this.htmlSafe;
    }

    public final void setSerializeNulls(boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
    }

    public final boolean getSerializeNulls() {
        return this.serializeNulls;
    }

    public JsonWriter beginArray() throws IOException {
        writeDeferredName();
        return open(1, "[");
    }

    public JsonWriter endArray() throws IOException {
        return close(1, 2, "]");
    }

    public JsonWriter beginObject() throws IOException {
        writeDeferredName();
        return open(3, "{");
    }

    public JsonWriter endObject() throws IOException {
        return close(3, 5, "}");
    }

    private JsonWriter open(int empty, String openBracket) throws IOException {
        beforeValue(true);
        push(empty);
        this.out.write(openBracket);
        return this;
    }

    private JsonWriter close(int empty, int nonempty, String closeBracket) throws IOException {
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
        if (this.stackSize == this.stack.length) {
            int[] newStack = new int[(this.stackSize * 2)];
            System.arraycopy(this.stack, 0, newStack, 0, this.stackSize);
            this.stack = newStack;
        }
        int[] iArr = this.stack;
        int i = this.stackSize;
        this.stackSize = i + 1;
        iArr[i] = newTop;
    }

    private int peek() {
        if (this.stackSize != 0) {
            return this.stack[this.stackSize - 1];
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
        beforeValue(false);
        string(value);
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
        beforeValue(false);
        this.out.write("null");
        return this;
    }

    public JsonWriter value(boolean value) throws IOException {
        writeDeferredName();
        beforeValue(false);
        this.out.write(!value ? StorageUtils.SDCARD_RWMOUNTED_STATE : StorageUtils.SDCARD_ROMOUNTED_STATE);
        return this;
    }

    public JsonWriter value(double value) throws IOException {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
        }
        writeDeferredName();
        beforeValue(false);
        this.out.append(Double.toString(value));
        return this;
    }

    public JsonWriter value(long value) throws IOException {
        writeDeferredName();
        beforeValue(false);
        this.out.write(Long.toString(value));
        return this;
    }

    public JsonWriter value(Number value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        String string = value.toString();
        if (!this.lenient && (string.equals("-Infinity") || string.equals("Infinity") || string.equals("NaN"))) {
            throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
        }
        beforeValue(false);
        this.out.append(string);
        return this;
    }

    public void flush() throws IOException {
        if (this.stackSize != 0) {
            this.out.flush();
            return;
        }
        throw new IllegalStateException("JsonWriter is closed.");
    }

    public void close() throws IOException {
        this.out.close();
        int size = this.stackSize;
        if (size <= 1 && (size != 1 || this.stack[size - 1] == 7)) {
            this.stackSize = 0;
            return;
        }
        throw new IOException("Incomplete document");
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x004d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void string(String value) throws IOException {
        String[] replacements = !this.htmlSafe ? REPLACEMENT_CHARS : HTML_SAFE_REPLACEMENT_CHARS;
        this.out.write("\"");
        int last = 0;
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            String replacement;
            if (c >= 128) {
                if (c == 8232) {
                    replacement = "\\u2028";
                } else if (c == 8233) {
                    replacement = "\\u2029";
                }
                if (last < i) {
                    this.out.write(value, last, i - last);
                }
                this.out.write(replacement);
                last = i + 1;
            } else {
                replacement = replacements[c];
                if (replacement == null) {
                }
                if (last < i) {
                }
                this.out.write(replacement);
                last = i + 1;
            }
        }
        if (last < length) {
            this.out.write(value, last, length - last);
        }
        this.out.write("\"");
    }

    private void newline() throws IOException {
        if (this.indent != null) {
            this.out.write("\n");
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

    private void beforeValue(boolean root) throws IOException {
        switch (peek()) {
            case 1:
                replaceTop(2);
                newline();
                return;
            case 2:
                this.out.append(',');
                newline();
                return;
            case 4:
                this.out.append(this.separator);
                replaceTop(5);
                return;
            case 6:
                break;
            case 7:
                if (!this.lenient) {
                    throw new IllegalStateException("JSON must have only one top-level value.");
                }
                break;
            default:
                throw new IllegalStateException("Nesting problem.");
        }
        if (this.lenient || root) {
            replaceTop(7);
            return;
        }
        throw new IllegalStateException("JSON must start with an array or an object.");
    }
}
