package com.huawei.gson.internal.bind;

import com.huawei.gson.Gson;
import com.huawei.gson.JsonDeserializer;
import com.huawei.gson.JsonSerializer;
import com.huawei.gson.TypeAdapter;
import com.huawei.gson.TypeAdapterFactory;
import com.huawei.gson.annotations.JsonAdapter;
import com.huawei.gson.internal.ConstructorConstructor;
import com.huawei.gson.reflect.TypeToken;

public final class JsonAdapterAnnotationTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;

    public JsonAdapterAnnotationTypeAdapterFactory(ConstructorConstructor constructorConstructor2) {
        this.constructorConstructor = constructorConstructor2;
    }

    @Override // com.huawei.gson.TypeAdapterFactory
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> targetType) {
        JsonAdapter annotation = (JsonAdapter) targetType.getRawType().getAnnotation(JsonAdapter.class);
        if (annotation == null) {
            return null;
        }
        return (TypeAdapter<T>) getTypeAdapter(this.constructorConstructor, gson, targetType, annotation);
    }

    /* access modifiers changed from: package-private */
    public TypeAdapter<?> getTypeAdapter(ConstructorConstructor constructorConstructor2, Gson gson, TypeToken<?> type, JsonAdapter annotation) {
        TypeAdapter<?> typeAdapter;
        JsonSerializer jsonSerializer;
        JsonDeserializer jsonDeserializer;
        Object instance = constructorConstructor2.get(TypeToken.get((Class) annotation.value())).construct();
        if (instance instanceof TypeAdapter) {
            typeAdapter = (TypeAdapter) instance;
        } else if (instance instanceof TypeAdapterFactory) {
            typeAdapter = ((TypeAdapterFactory) instance).create(gson, type);
        } else if ((instance instanceof JsonSerializer) || (instance instanceof JsonDeserializer)) {
            if (instance instanceof JsonSerializer) {
                jsonSerializer = (JsonSerializer) instance;
            } else {
                jsonSerializer = null;
            }
            if (instance instanceof JsonDeserializer) {
                jsonDeserializer = (JsonDeserializer) instance;
            } else {
                jsonDeserializer = null;
            }
            typeAdapter = new TreeTypeAdapter<>(jsonSerializer, jsonDeserializer, gson, type, null);
        } else {
            throw new IllegalArgumentException("Invalid attempt to bind an instance of " + instance.getClass().getName() + " as a @JsonAdapter for " + type.toString() + ". @JsonAdapter value must be a TypeAdapter, TypeAdapterFactory, JsonSerializer or JsonDeserializer.");
        }
        if (typeAdapter == null || !annotation.nullSafe()) {
            return typeAdapter;
        }
        return typeAdapter.nullSafe();
    }
}
