package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.C$Gson$Preconditions;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;

public final class TreeTypeAdapter<T> extends TypeAdapter<T> {
    private final TreeTypeAdapter<T>.GsonContextImpl context = new GsonContextImpl();
    private TypeAdapter<T> delegate;
    private final JsonDeserializer<T> deserializer;
    final Gson gson;
    private final JsonSerializer<T> serializer;
    private final TypeAdapterFactory skipPast;
    private final TypeToken<T> typeToken;

    private final class GsonContextImpl implements JsonSerializationContext, JsonDeserializationContext {
        private GsonContextImpl() {
        }

        public JsonElement serialize(Object src) {
            return TreeTypeAdapter.this.gson.toJsonTree(src);
        }

        public JsonElement serialize(Object src, Type typeOfSrc) {
            return TreeTypeAdapter.this.gson.toJsonTree(src, typeOfSrc);
        }

        public <R> R deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
            return TreeTypeAdapter.this.gson.fromJson(json, typeOfT);
        }
    }

    private static final class SingleTypeFactory implements TypeAdapterFactory {
        private final JsonDeserializer<?> deserializer;
        private final TypeToken<?> exactType;
        private final Class<?> hierarchyType;
        private final boolean matchRawType;
        private final JsonSerializer<?> serializer;

        SingleTypeFactory(Object typeAdapter, TypeToken<?> exactType2, boolean matchRawType2, Class<?> hierarchyType2) {
            JsonSerializer<?> jsonSerializer;
            JsonDeserializer<?> jsonDeserializer;
            if (typeAdapter instanceof JsonSerializer) {
                jsonSerializer = (JsonSerializer) typeAdapter;
            } else {
                jsonSerializer = null;
            }
            this.serializer = jsonSerializer;
            if (typeAdapter instanceof JsonDeserializer) {
                jsonDeserializer = (JsonDeserializer) typeAdapter;
            } else {
                jsonDeserializer = null;
            }
            this.deserializer = jsonDeserializer;
            C$Gson$Preconditions.checkArgument((this.serializer == null && this.deserializer == null) ? false : true);
            this.exactType = exactType2;
            this.matchRawType = matchRawType2;
            this.hierarchyType = hierarchyType2;
        }

        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            boolean matches;
            if (this.exactType != null) {
                matches = this.exactType.equals(type) || (this.matchRawType && this.exactType.getType() == type.getRawType());
            } else {
                matches = this.hierarchyType.isAssignableFrom(type.getRawType());
            }
            if (matches) {
                return new TreeTypeAdapter(this.serializer, this.deserializer, gson, type, this);
            }
            return null;
        }
    }

    public TreeTypeAdapter(JsonSerializer<T> serializer2, JsonDeserializer<T> deserializer2, Gson gson2, TypeToken<T> typeToken2, TypeAdapterFactory skipPast2) {
        this.serializer = serializer2;
        this.deserializer = deserializer2;
        this.gson = gson2;
        this.typeToken = typeToken2;
        this.skipPast = skipPast2;
    }

    public T read(JsonReader in) throws IOException {
        if (this.deserializer == null) {
            return delegate().read(in);
        }
        JsonElement value = Streams.parse(in);
        if (value.isJsonNull()) {
            return null;
        }
        return this.deserializer.deserialize(value, this.typeToken.getType(), this.context);
    }

    public void write(JsonWriter out, T value) throws IOException {
        if (this.serializer == null) {
            delegate().write(out, value);
        } else if (value == null) {
            out.nullValue();
        } else {
            Streams.write(this.serializer.serialize(value, this.typeToken.getType(), this.context), out);
        }
    }

    private TypeAdapter<T> delegate() {
        TypeAdapter<T> d = this.delegate;
        if (d != null) {
            return d;
        }
        TypeAdapter<T> d2 = this.gson.getDelegateAdapter(this.skipPast, this.typeToken);
        this.delegate = d2;
        return d2;
    }

    public static TypeAdapterFactory newFactory(TypeToken<?> exactType, Object typeAdapter) {
        return new SingleTypeFactory(typeAdapter, exactType, false, null);
    }

    public static TypeAdapterFactory newFactoryWithMatchRawType(TypeToken<?> exactType, Object typeAdapter) {
        return new SingleTypeFactory(typeAdapter, exactType, exactType.getType() == exactType.getRawType(), null);
    }

    public static TypeAdapterFactory newTypeHierarchyFactory(Class<?> hierarchyType, Object typeAdapter) {
        return new SingleTypeFactory(typeAdapter, null, false, hierarchyType);
    }
}
