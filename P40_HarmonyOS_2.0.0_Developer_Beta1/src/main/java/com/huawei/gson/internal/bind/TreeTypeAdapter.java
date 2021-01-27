package com.huawei.gson.internal.bind;

import com.huawei.gson.Gson;
import com.huawei.gson.JsonDeserializationContext;
import com.huawei.gson.JsonDeserializer;
import com.huawei.gson.JsonElement;
import com.huawei.gson.JsonParseException;
import com.huawei.gson.JsonSerializationContext;
import com.huawei.gson.JsonSerializer;
import com.huawei.gson.TypeAdapter;
import com.huawei.gson.TypeAdapterFactory;
import com.huawei.gson.internal.C$Gson$Preconditions;
import com.huawei.gson.internal.Streams;
import com.huawei.gson.reflect.TypeToken;
import com.huawei.gson.stream.JsonReader;
import com.huawei.gson.stream.JsonWriter;
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

    public TreeTypeAdapter(JsonSerializer<T> serializer2, JsonDeserializer<T> deserializer2, Gson gson2, TypeToken<T> typeToken2, TypeAdapterFactory skipPast2) {
        this.serializer = serializer2;
        this.deserializer = deserializer2;
        this.gson = gson2;
        this.typeToken = typeToken2;
        this.skipPast = skipPast2;
    }

    @Override // com.huawei.gson.TypeAdapter
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

    @Override // com.huawei.gson.TypeAdapter
    public void write(JsonWriter out, T value) throws IOException {
        JsonSerializer<T> jsonSerializer = this.serializer;
        if (jsonSerializer == null) {
            delegate().write(out, value);
        } else if (value == null) {
            out.nullValue();
        } else {
            Streams.write(jsonSerializer.serialize(value, this.typeToken.getType(), this.context), out);
        }
    }

    private TypeAdapter<T> delegate() {
        TypeAdapter<T> d = this.delegate;
        if (d != null) {
            return d;
        }
        TypeAdapter<T> delegateAdapter = this.gson.getDelegateAdapter(this.skipPast, this.typeToken);
        this.delegate = delegateAdapter;
        return delegateAdapter;
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

    /* access modifiers changed from: private */
    public static final class SingleTypeFactory implements TypeAdapterFactory {
        private final JsonDeserializer<?> deserializer;
        private final TypeToken<?> exactType;
        private final Class<?> hierarchyType;
        private final boolean matchRawType;
        private final JsonSerializer<?> serializer;

        SingleTypeFactory(Object typeAdapter, TypeToken<?> exactType2, boolean matchRawType2, Class<?> hierarchyType2) {
            JsonSerializer<?> jsonSerializer;
            JsonDeserializer<?> jsonDeserializer = null;
            if (typeAdapter instanceof JsonSerializer) {
                jsonSerializer = (JsonSerializer) typeAdapter;
            } else {
                jsonSerializer = null;
            }
            this.serializer = jsonSerializer;
            this.deserializer = typeAdapter instanceof JsonDeserializer ? (JsonDeserializer) typeAdapter : jsonDeserializer;
            C$Gson$Preconditions.checkArgument((this.serializer == null && this.deserializer == null) ? false : true);
            this.exactType = exactType2;
            this.matchRawType = matchRawType2;
            this.hierarchyType = hierarchyType2;
        }

        @Override // com.huawei.gson.TypeAdapterFactory
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            boolean matches;
            TypeToken<?> typeToken = this.exactType;
            if (typeToken != null) {
                matches = typeToken.equals(type) || (this.matchRawType && this.exactType.getType() == type.getRawType());
            } else {
                matches = this.hierarchyType.isAssignableFrom(type.getRawType());
            }
            if (matches) {
                return new TreeTypeAdapter(this.serializer, this.deserializer, gson, type, this);
            }
            return null;
        }
    }

    private final class GsonContextImpl implements JsonSerializationContext, JsonDeserializationContext {
        private GsonContextImpl() {
        }

        @Override // com.huawei.gson.JsonSerializationContext
        public JsonElement serialize(Object src) {
            return TreeTypeAdapter.this.gson.toJsonTree(src);
        }

        @Override // com.huawei.gson.JsonSerializationContext
        public JsonElement serialize(Object src, Type typeOfSrc) {
            return TreeTypeAdapter.this.gson.toJsonTree(src, typeOfSrc);
        }

        @Override // com.huawei.gson.JsonDeserializationContext
        public <R> R deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
            return (R) TreeTypeAdapter.this.gson.fromJson(json, typeOfT);
        }
    }
}
