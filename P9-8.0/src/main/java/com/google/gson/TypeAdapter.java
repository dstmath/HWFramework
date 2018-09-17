package com.google.gson;

import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
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
            public void write(JsonWriter out, T value) throws IOException {
                if (value != null) {
                    TypeAdapter.this.write(out, value);
                } else {
                    out.nullValue();
                }
            }

            public T read(JsonReader reader) throws IOException {
                if (reader.peek() != JsonToken.NULL) {
                    return TypeAdapter.this.read(reader);
                }
                reader.nextNull();
                return null;
            }
        };
    }

    public final String toJson(T value) throws IOException {
        StringWriter stringWriter = new StringWriter();
        toJson(stringWriter, value);
        return stringWriter.toString();
    }

    public final JsonElement toJsonTree(T value) {
        try {
            JsonTreeWriter jsonWriter = new JsonTreeWriter();
            write(jsonWriter, value);
            return jsonWriter.get();
        } catch (Throwable e) {
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
        } catch (Throwable e) {
            throw new JsonIOException(e);
        }
    }
}
