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
            List<JsonScope> list = this.stack;
            list.remove(list.size() - 1);
            if (context == nonempty) {
                newline();
            }
            this.out.write(closeBracket);
            return this;
        }
        throw new IllegalStateException("Nesting problem: " + this.stack);
    }

    private JsonScope peek() {
        List<JsonScope> list = this.stack;
        return list.get(list.size() - 1);
    }

    private void replaceTop(JsonScope topOfStack) {
        List<JsonScope> list = this.stack;
        list.set(list.size() - 1, topOfStack);
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
            this.out.append((CharSequence) Double.toString(value));
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
            this.out.append((CharSequence) string);
            return this;
        }
        throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
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
            if (c == '\f') {
                this.out.write("\\f");
            } else if (c == '\r') {
                this.out.write("\\r");
            } else if (c == '\"' || c == '\\') {
                this.out.write(92);
                this.out.write(c);
            } else if (c == 8232 || c == 8233) {
                this.out.write(String.format("\\u%04x", Integer.valueOf(c)));
            } else {
                switch (c) {
                    case '\b':
                        this.out.write("\\b");
                        continue;
                    case '\t':
                        this.out.write("\\t");
                        continue;
                    case '\n':
                        this.out.write("\\n");
                        continue;
                    default:
                        if (c > 31) {
                            this.out.write(c);
                            break;
                        } else {
                            this.out.write(String.format("\\u%04x", Integer.valueOf(c)));
                            continue;
                        }
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

    /* access modifiers changed from: package-private */
    /* renamed from: android.util.JsonWriter$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$util$JsonScope = new int[JsonScope.values().length];

        static {
            try {
                $SwitchMap$android$util$JsonScope[JsonScope.EMPTY_DOCUMENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$util$JsonScope[JsonScope.EMPTY_ARRAY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$util$JsonScope[JsonScope.NONEMPTY_ARRAY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$util$JsonScope[JsonScope.DANGLING_NAME.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$util$JsonScope[JsonScope.NONEMPTY_DOCUMENT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private void beforeValue(boolean root) throws IOException {
        int i = AnonymousClass1.$SwitchMap$android$util$JsonScope[peek().ordinal()];
        if (i != 1) {
            if (i == 2) {
                replaceTop(JsonScope.NONEMPTY_ARRAY);
                newline();
            } else if (i == 3) {
                this.out.append(',');
                newline();
            } else if (i == 4) {
                this.out.append((CharSequence) this.separator);
                replaceTop(JsonScope.NONEMPTY_OBJECT);
            } else if (i != 5) {
                throw new IllegalStateException("Nesting problem: " + this.stack);
            } else {
                throw new IllegalStateException("JSON must have only one top-level value.");
            }
        } else if (this.lenient || root) {
            replaceTop(JsonScope.NONEMPTY_DOCUMENT);
        } else {
            throw new IllegalStateException("JSON must start with an array or an object.");
        }
    }
}
