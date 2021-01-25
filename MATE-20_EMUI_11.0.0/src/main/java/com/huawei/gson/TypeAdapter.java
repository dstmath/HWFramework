package com.huawei.gson;

import com.huawei.gson.internal.bind.JsonTreeReader;
import com.huawei.gson.internal.bind.JsonTreeWriter;
import com.huawei.gson.stream.JsonReader;
import com.huawei.gson.stream.JsonToken;
import com.huawei.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public abstract class TypeAdapter<T> {
    public abstract T read(JsonReader jsonReader) throws IOException;

    public abstract void write(JsonWriter jsonWriter, T t) throws IOException;

    public final void toJson(Writer out, T value) throws IOException {
        write(new JsonWriter(out), value);
    }

    public final TypeAdapter<T> nullSafe() {
        return new TypeAdapter<T>() {
            /* class com.huawei.gson.TypeAdapter.AnonymousClass1 */

            @Override // com.huawei.gson.TypeAdapter
            public void write(JsonWriter out, T value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    TypeAdapter.this.write(out, value);
                }
            }

            @Override // com.huawei.gson.TypeAdapter
            public T read(JsonReader reader) throws IOException {
                if (reader.peek() != JsonToken.NULL) {
                    return (T) TypeAdapter.this.read(reader);
                }
                reader.nextNull();
                return null;
            }
        };
    }

    public final String toJson(T value) {
        StringWriter stringWriter = new StringWriter();
        try {
            toJson(stringWriter, value);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public final JsonElement toJsonTree(T value) {
        try {
            JsonTreeWriter jsonWriter = new JsonTreeWriter();
            write(jsonWriter, value);
            return jsonWriter.get();
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }

    public final T fromJson(Reader in) throws IOException {
        return read(new JsonReader(in));
    }

    public final T fromJson(String json) throws IOException {
        return fromJson(new StringReader(json));
    }

    public final T fromJsonTree(JsonElement jsonTree) {
        try {
            return read(new JsonTreeReader(jsonTree));
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }
}
