package com.google.gson.internal.bind;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.C$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;
    private final Excluder excluder;
    private final FieldNamingStrategy fieldNamingPolicy;

    static abstract class BoundField {
        final boolean deserialized;
        final String name;
        final boolean serialized;

        abstract void read(JsonReader jsonReader, Object obj) throws IOException, IllegalAccessException;

        abstract void write(JsonWriter jsonWriter, Object obj) throws IOException, IllegalAccessException;

        abstract boolean writeField(Object obj) throws IOException, IllegalAccessException;

        protected BoundField(String name, boolean serialized, boolean deserialized) {
            this.name = name;
            this.serialized = serialized;
            this.deserialized = deserialized;
        }
    }

    public static final class Adapter<T> extends TypeAdapter<T> {
        private final Map<String, BoundField> boundFields;
        private final ObjectConstructor<T> constructor;

        /* synthetic */ Adapter(ObjectConstructor x0, Map x1, AnonymousClass1 x2) {
            this(x0, x1);
        }

        private Adapter(ObjectConstructor<T> constructor, Map<String, BoundField> boundFields) {
            this.constructor = constructor;
            this.boundFields = boundFields;
        }

        public T read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                T instance = this.constructor.construct();
                try {
                    in.beginObject();
                    while (in.hasNext()) {
                        BoundField field = (BoundField) this.boundFields.get(in.nextName());
                        if (field != null) {
                            if (field.deserialized) {
                                field.read(in, instance);
                            }
                        }
                        in.skipValue();
                    }
                    in.endObject();
                    return instance;
                } catch (Throwable e) {
                    throw new JsonSyntaxException(e);
                } catch (IllegalAccessException e2) {
                    throw new AssertionError(e2);
                }
            }
            in.nextNull();
            return null;
        }

        public void write(JsonWriter out, T value) throws IOException {
            if (value != null) {
                out.beginObject();
                try {
                    for (BoundField boundField : this.boundFields.values()) {
                        if (boundField.writeField(value)) {
                            out.name(boundField.name);
                            boundField.write(out, value);
                        }
                    }
                    out.endObject();
                    return;
                } catch (IllegalAccessException e) {
                    throw new AssertionError();
                }
            }
            out.nullValue();
        }
    }

    public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor, FieldNamingStrategy fieldNamingPolicy, Excluder excluder) {
        this.constructorConstructor = constructorConstructor;
        this.fieldNamingPolicy = fieldNamingPolicy;
        this.excluder = excluder;
    }

    public boolean excludeField(Field f, boolean serialize) {
        return excludeField(f, serialize, this.excluder);
    }

    static boolean excludeField(Field f, boolean serialize, Excluder excluder) {
        return (excluder.excludeClass(f.getType(), serialize) || excluder.excludeField(f, serialize)) ? false : true;
    }

    private String getFieldName(Field f) {
        return getFieldName(this.fieldNamingPolicy, f);
    }

    static String getFieldName(FieldNamingStrategy fieldNamingPolicy, Field f) {
        SerializedName serializedName = (SerializedName) f.getAnnotation(SerializedName.class);
        return serializedName != null ? serializedName.value() : fieldNamingPolicy.translateName(f);
    }

    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> raw = type.getRawType();
        if (Object.class.isAssignableFrom(raw)) {
            return new Adapter(this.constructorConstructor.get(type), getBoundFields(gson, type, raw), null);
        }
        return null;
    }

    private BoundField createBoundField(Gson context, Field field, String name, TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
        final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
        final Gson gson = context;
        final Field field2 = field;
        final TypeToken<?> typeToken = fieldType;
        return new BoundField(name, serialize, deserialize) {
            final TypeAdapter<?> typeAdapter = ReflectiveTypeAdapterFactory.this.getFieldAdapter(gson, field2, typeToken);

            void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException {
                new TypeAdapterRuntimeTypeWrapper(gson, this.typeAdapter, typeToken.getType()).write(writer, field2.get(value));
            }

            void read(JsonReader reader, Object value) throws IOException, IllegalAccessException {
                Object fieldValue = this.typeAdapter.read(reader);
                if (fieldValue != null || !isPrimitive) {
                    field2.set(value, fieldValue);
                }
            }

            public boolean writeField(Object value) throws IOException, IllegalAccessException {
                boolean z = false;
                if (!this.serialized) {
                    return false;
                }
                if (field2.get(value) != value) {
                    z = true;
                }
                return z;
            }
        };
    }

    private TypeAdapter<?> getFieldAdapter(Gson gson, Field field, TypeToken<?> fieldType) {
        JsonAdapter annotation = (JsonAdapter) field.getAnnotation(JsonAdapter.class);
        if (annotation != null) {
            TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(this.constructorConstructor, gson, fieldType, annotation);
            if (adapter != null) {
                return adapter;
            }
        }
        return gson.getAdapter((TypeToken) fieldType);
    }

    private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw) {
        Map<String, BoundField> result = new LinkedHashMap();
        if (raw.isInterface()) {
            return result;
        }
        Type declaredType = type.getType();
        while (raw != Object.class) {
            for (Field field : raw.getDeclaredFields()) {
                boolean serialize = excludeField(field, true);
                boolean deserialize = excludeField(field, false);
                if (serialize || deserialize) {
                    field.setAccessible(true);
                    BoundField boundField = createBoundField(context, field, getFieldName(field), TypeToken.get(C$Gson$Types.resolve(type.getType(), raw, field.getGenericType())), serialize, deserialize);
                    BoundField previous = (BoundField) result.put(boundField.name, boundField);
                    if (previous != null) {
                        throw new IllegalArgumentException(declaredType + " declares multiple JSON fields named " + previous.name);
                    }
                }
            }
            type = TypeToken.get(C$Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return result;
    }
}
