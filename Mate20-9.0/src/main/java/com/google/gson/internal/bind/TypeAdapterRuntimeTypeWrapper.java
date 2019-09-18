package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

final class TypeAdapterRuntimeTypeWrapper<T> extends TypeAdapter<T> {
    private final Gson context;
    private final TypeAdapter<T> delegate;
    private final Type type;

    TypeAdapterRuntimeTypeWrapper(Gson context2, TypeAdapter<T> delegate2, Type type2) {
        this.context = context2;
        this.delegate = delegate2;
        this.type = type2;
    }

    public T read(JsonReader in) throws IOException {
        return this.delegate.read(in);
    }

    public void write(JsonWriter out, T value) throws IOException {
        TypeAdapter chosen = this.delegate;
        Type runtimeType = getRuntimeTypeIfMoreSpecific(this.type, value);
        if (runtimeType != this.type) {
            TypeAdapter runtimeTypeAdapter = this.context.getAdapter(TypeToken.get(runtimeType));
            if (!(runtimeTypeAdapter instanceof ReflectiveTypeAdapterFactory.Adapter)) {
                chosen = runtimeTypeAdapter;
            } else if (!(this.delegate instanceof ReflectiveTypeAdapterFactory.Adapter)) {
                chosen = this.delegate;
            } else {
                chosen = runtimeTypeAdapter;
            }
        }
        chosen.write(out, value);
    }

    private Type getRuntimeTypeIfMoreSpecific(Type type2, Object value) {
        if (value == null) {
            return type2;
        }
        if (type2 == Object.class || (type2 instanceof TypeVariable) || (type2 instanceof Class)) {
            return value.getClass();
        }
        return type2;
    }
}
