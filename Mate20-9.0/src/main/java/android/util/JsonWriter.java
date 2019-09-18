package android.util;

import android.provider.SettingsStringUtil;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public final class JsonWriter implements Closeable {
    private String indent;
    private boolean lenient;
    private final Writer out;
    private String separator;
    private final List<JsonScope> stack = new ArrayList();

    public JsonWriter(Writer out2) {
        this.stack.add(JsonScope.EMPTY_DOCUMENT);
        this.separator = SettingsStringUtil.DELIMITER;
        if (out2 != null) {
            this.out = out2;
            return;
        }
        throw new NullPointerException("out == null");
    }

    public void setIndent(String indent2) {
        if (indent2.isEmpty()) {
            this.indent = null;
            this.separator = SettingsStringUtil.DELIMITER;
            return;
        }
        this.indent = indent2;
        this.separator = ": ";
    }

    public void setLenient(boolean lenient2) {
        this.lenient = lenient2;
    }

    public boolean isLenient() {
        return this.lenient;
    }

    public JsonWriter beginArray() throws IOException {
        return open(JsonScope.EMPTY_ARRAY, "[");
    }

    public JsonWriter endArray() throws IOException {
        return close(JsonScope.EMPTY_ARRAY, JsonScope.NONEMPTY_ARRAY, "]");
    }

    public JsonWriter beginObject() throws IOException {
        return open(JsonScope.EMPTY_OBJECT, "{");
    }

    public JsonWriter endObject() throws IOException {
        return close(JsonScope.EMPTY_OBJECT, JsonScope.NONEMPTY_OBJECT, "}");
    }

    private JsonWriter open(JsonScope empty, String openBracket) throws IOException {
        beforeValue(true);
        this.stack.add(empty);
        this.out.write(openBracket);
        return this;
    }

    private JsonWriter close(JsonScope empty, JsonScope nonempty, String closeBracket) throws IOException {
        JsonScope context = peek();
        if (context == nonempty || context == empty) {
            this.stack.remove(this.stack.size() - 1);
            if (context == nonempty) {
                newline();
            }
            this.out.write(closeBracket);
            return this;
        }
        throw new IllegalStateException("Nesting problem: " + this.stack);
    }

    private JsonScope peek() {
        return this.stack.get(this.stack.size() - 1);
    }

    private void replaceTop(JsonScope topOfStack) {
        this.stack.set(this.stack.size() - 1, topOfStack);
    }

    public JsonWriter name(String name) throws IOException {
        if (name != null) {
            beforeName();
            string(name);
            return this;
        }
        throw new NullPointerException("name == null");
    }

    public JsonWriter value(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        beforeValue(false);
        string(value);
        return this;
    }

    public JsonWriter nullValue() throws IOException {
        beforeValue(false);
        this.out.write("null");
        return this;
    }

    public JsonWriter value(boolean value) throws IOException {
        beforeValue(false);
        this.out.write(value ? "true" : "false");
        return this;
    }

    public JsonWriter value(double value) throws IOException {
        if (this.lenient || (!Double.isNaN(value) && !Double.isInfinite(value))) {
            beforeValue(false);
            this.out.append(Double.toString(value));
            return this;
        }
        throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }

    public JsonWriter value(long value) throws IOException {
        beforeValue(false);
        this.out.write(Long.toString(value));
        return this;
    }

    public JsonWriter value(Number value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        String string = value.toString();
        if (this.lenient || (!string.equals("-Infinity") && !string.equals("Infinity") && !string.equals("NaN"))) {
            beforeValue(false);
            this.out.append(string);
            return this;
        }
        throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    public void close() throws IOException {
        this.out.close();
        if (peek() != JsonScope.NONEMPTY_DOCUMENT) {
            throw new IOException("Incomplete document");
        }
    }

    private void string(String value) throws IOException {
        this.out.write("\"");
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            switch (c) {
                case 8:
                    this.out.write("\\b");
                    break;
                case 9:
                    this.out.write("\\t");
                    break;
                case 10:
                    this.out.write("\\n");
                    break;
                case 12:
                    this.out.write("\\f");
                    break;
                case 13:
                    this.out.write("\\r");
                    break;
                case '\"':
                case '\\':
                    this.out.write(92);
                    this.out.write(c);
                    break;
                case 8232:
                case 8233:
                    this.out.write(String.format("\\u%04x", new Object[]{Integer.valueOf(c)}));
                    break;
                default:
                    if (c > 31) {
                        this.out.write(c);
                        break;
                    } else {
                        this.out.write(String.format("\\u%04x", new Object[]{Integer.valueOf(c)}));
                        break;
                    }
            }
        }
        this.out.write("\"");
    }

    private void newline() throws IOException {
        if (this.indent != null) {
            this.out.write("\n");
            for (int i = 1; i < this.stack.size(); i++) {
                this.out.write(this.indent);
            }
        }
    }

    private void beforeName() throws IOException {
        JsonScope context = peek();
        if (context == JsonScope.NONEMPTY_OBJECT) {
            this.out.write(44);
        } else if (context != JsonScope.EMPTY_OBJECT) {
            throw new IllegalStateException("Nesting problem: " + this.stack);
        }
        newline();
        replaceTop(JsonScope.DANGLING_NAME);
    }

    private void beforeValue(boolean root) throws IOException {
        switch (peek()) {
            case EMPTY_DOCUMENT:
                if (this.lenient || root) {
                    replaceTop(JsonScope.NONEMPTY_DOCUMENT);
                    return;
                }
                throw new IllegalStateException("JSON must start with an array or an object.");
            case EMPTY_ARRAY:
                replaceTop(JsonScope.NONEMPTY_ARRAY);
                newline();
                return;
            case NONEMPTY_ARRAY:
                this.out.append(',');
                newline();
                return;
            case DANGLING_NAME:
                this.out.append(this.separator);
                replaceTop(JsonScope.NONEMPTY_OBJECT);
                return;
            case NONEMPTY_DOCUMENT:
                throw new IllegalStateException("JSON must have only one top-level value.");
            default:
                throw new IllegalStateException("Nesting problem: " + this.stack);
        }
    }
}
