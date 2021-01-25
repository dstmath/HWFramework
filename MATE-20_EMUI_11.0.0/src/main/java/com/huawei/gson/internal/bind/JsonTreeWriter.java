package com.huawei.gson.internal.bind;

import com.huawei.gson.JsonArray;
import com.huawei.gson.JsonElement;
import com.huawei.gson.JsonNull;
import com.huawei.gson.JsonObject;
import com.huawei.gson.JsonPrimitive;
import com.huawei.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public final class JsonTreeWriter extends JsonWriter {
    private static final JsonPrimitive SENTINEL_CLOSED = new JsonPrimitive("closed");
    private static final Writer UNWRITABLE_WRITER = new Writer() {
        /* class com.huawei.gson.internal.bind.JsonTreeWriter.AnonymousClass1 */

        @Override // java.io.Writer
        public void write(char[] buffer, int offset, int counter) {
            throw new AssertionError();
        }

        @Override // java.io.Writer, java.io.Flushable
        public void flush() throws IOException {
            throw new AssertionError();
        }

        @Override // java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            throw new AssertionError();
        }
    };
    private String pendingName;
    private JsonElement product = JsonNull.INSTANCE;
    private final List<JsonElement> stack = new ArrayList();

    public JsonTreeWriter() {
        super(UNWRITABLE_WRITER);
    }

    public JsonElement get() {
        if (this.stack.isEmpty()) {
            return this.product;
        }
        throw new IllegalStateException("Expected one JSON element but was " + this.stack);
    }

    private JsonElement peek() {
        List<JsonElement> list = this.stack;
        return list.get(list.size() - 1);
    }

    private void put(JsonElement value) {
        if (this.pendingName != null) {
            if (!value.isJsonNull() || getSerializeNulls()) {
                ((JsonObject) peek()).add(this.pendingName, value);
            }
            this.pendingName = null;
        } else if (this.stack.isEmpty()) {
            this.product = value;
        } else {
            JsonElement element = peek();
            if (element instanceof JsonArray) {
                ((JsonArray) element).add(value);
                return;
            }
            throw new IllegalStateException();
        }
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter beginArray() throws IOException {
        JsonArray array = new JsonArray();
        put(array);
        this.stack.add(array);
        return this;
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter endArray() throws IOException {
        if (this.stack.isEmpty() || this.pendingName != null) {
            throw new IllegalStateException();
        } else if (peek() instanceof JsonArray) {
            List<JsonElement> list = this.stack;
            list.remove(list.size() - 1);
            return this;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter beginObject() throws IOException {
        JsonObject object = new JsonObject();
        put(object);
        this.stack.add(object);
        return this;
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter endObject() throws IOException {
        if (this.stack.isEmpty() || this.pendingName != null) {
            throw new IllegalStateException();
        } else if (peek() instanceof JsonObject) {
            List<JsonElement> list = this.stack;
            list.remove(list.size() - 1);
            return this;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter name(String name) throws IOException {
        if (this.stack.isEmpty() || this.pendingName != null) {
            throw new IllegalStateException();
        } else if (peek() instanceof JsonObject) {
            this.pendingName = name;
            return this;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter value(String value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        put(new JsonPrimitive(value));
        return this;
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter nullValue() throws IOException {
        put(JsonNull.INSTANCE);
        return this;
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter value(boolean value) throws IOException {
        put(new JsonPrimitive(Boolean.valueOf(value)));
        return this;
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter value(Boolean value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        put(new JsonPrimitive(value));
        return this;
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter value(double value) throws IOException {
        if (isLenient() || (!Double.isNaN(value) && !Double.isInfinite(value))) {
            put(new JsonPrimitive((Number) Double.valueOf(value)));
            return this;
        }
        throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter value(long value) throws IOException {
        put(new JsonPrimitive((Number) Long.valueOf(value)));
        return this;
    }

    @Override // com.huawei.gson.stream.JsonWriter
    public JsonWriter value(Number value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        if (!isLenient()) {
            double d = value.doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
            }
        }
        put(new JsonPrimitive(value));
        return this;
    }

    @Override // com.huawei.gson.stream.JsonWriter, java.io.Flushable
    public void flush() throws IOException {
    }

    @Override // com.huawei.gson.stream.JsonWriter, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (this.stack.isEmpty()) {
            this.stack.add(SENTINEL_CLOSED);
            return;
        }
        throw new IOException("Incomplete document");
    }
}
