package com.huawei.gson.internal.bind;

import com.huawei.gson.FieldNamingStrategy;
import com.huawei.gson.Gson;
import com.huawei.gson.JsonSyntaxException;
import com.huawei.gson.TypeAdapter;
import com.huawei.gson.TypeAdapterFactory;
import com.huawei.gson.annotations.JsonAdapter;
import com.huawei.gson.annotations.SerializedName;
import com.huawei.gson.internal.C$Gson$Types;
import com.huawei.gson.internal.ConstructorConstructor;
import com.huawei.gson.internal.Excluder;
import com.huawei.gson.internal.ObjectConstructor;
import com.huawei.gson.internal.Primitives;
import com.huawei.gson.internal.reflect.ReflectionAccessor;
import com.huawei.gson.reflect.TypeToken;
import com.huawei.gson.stream.JsonReader;
import com.huawei.gson.stream.JsonToken;
import com.huawei.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
    private final ReflectionAccessor accessor = ReflectionAccessor.getInstance();
    private final ConstructorConstructor constructorConstructor;
    private final Excluder excluder;
    private final FieldNamingStrategy fieldNamingPolicy;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;

    public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor2, FieldNamingStrategy fieldNamingPolicy2, Excluder excluder2, JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory2) {
        this.constructorConstructor = constructorConstructor2;
        this.fieldNamingPolicy = fieldNamingPolicy2;
        this.excluder = excluder2;
        this.jsonAdapterFactory = jsonAdapterFactory2;
    }

    public boolean excludeField(Field f, boolean serialize) {
        return excludeField(f, serialize, this.excluder);
    }

    static boolean excludeField(Field f, boolean serialize, Excluder excluder2) {
        return !excluder2.excludeClass(f.getType(), serialize) && !excluder2.excludeField(f, serialize);
    }

    private List<String> getFieldNames(Field f) {
        SerializedName annotation = (SerializedName) f.getAnnotation(SerializedName.class);
        if (annotation == null) {
            return Collections.singletonList(this.fieldNamingPolicy.translateName(f));
        }
        String serializedName = annotation.value();
        String[] alternates = annotation.alternate();
        if (alternates.length == 0) {
            return Collections.singletonList(serializedName);
        }
        List<String> fieldNames = new ArrayList<>(alternates.length + 1);
        fieldNames.add(serializedName);
        for (String alternate : alternates) {
            fieldNames.add(alternate);
        }
        return fieldNames;
    }

    @Override // com.huawei.gson.TypeAdapterFactory
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> raw = type.getRawType();
        if (!Object.class.isAssignableFrom(raw)) {
            return null;
        }
        return new Adapter(this.constructorConstructor.get(type), getBoundFields(gson, type, raw));
    }

    private BoundField createBoundField(final Gson context, final Field field, String name, final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
        final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
        JsonAdapter annotation = (JsonAdapter) field.getAnnotation(JsonAdapter.class);
        final TypeAdapter<?> mapped = null;
        if (annotation != null) {
            mapped = this.jsonAdapterFactory.getTypeAdapter(this.constructorConstructor, context, fieldType, annotation);
        }
        final boolean jsonAdapterPresent = mapped != null;
        if (mapped == null) {
            mapped = context.getAdapter(fieldType);
        }
        return new BoundField(name, serialize, deserialize) {
            /* class com.huawei.gson.internal.bind.ReflectiveTypeAdapterFactory.AnonymousClass1 */

            /* access modifiers changed from: package-private */
            @Override // com.huawei.gson.internal.bind.ReflectiveTypeAdapterFactory.BoundField
            public void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException {
                TypeAdapter t;
                Object fieldValue = field.get(value);
                if (jsonAdapterPresent) {
                    t = mapped;
                } else {
                    t = new TypeAdapterRuntimeTypeWrapper(context, mapped, fieldType.getType());
                }
                t.write(writer, fieldValue);
            }

            /* access modifiers changed from: package-private */
            @Override // com.huawei.gson.internal.bind.ReflectiveTypeAdapterFactory.BoundField
            public void read(JsonReader reader, Object value) throws IOException, IllegalAccessException {
                Object fieldValue = mapped.read(reader);
                if (fieldValue != null || !isPrimitive) {
                    field.set(value, fieldValue);
                }
            }

            @Override // com.huawei.gson.internal.bind.ReflectiveTypeAdapterFactory.BoundField
            public boolean writeField(Object value) throws IOException, IllegalAccessException {
                if (this.serialized && field.get(value) != value) {
                    return true;
                }
                return false;
            }
        };
    }

    private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw) {
        Map<String, BoundField> result = new LinkedHashMap<>();
        if (raw.isInterface()) {
            return result;
        }
        Type declaredType = type.getType();
        TypeToken<?> type2 = type;
        Class<?> raw2 = raw;
        while (raw2 != Object.class) {
            Field[] fields = raw2.getDeclaredFields();
            int length = fields.length;
            boolean z = false;
            int i = 0;
            while (i < length) {
                Field field = fields[i];
                boolean serialize = excludeField(field, true);
                boolean deserialize = excludeField(field, z);
                if (serialize || deserialize) {
                    this.accessor.makeAccessible(field);
                    Type fieldType = C$Gson$Types.resolve(type2.getType(), raw2, field.getGenericType());
                    List<String> fieldNames = getFieldNames(field);
                    int size = fieldNames.size();
                    int i2 = 0;
                    BoundField replaced = null;
                    while (i2 < size) {
                        String name = fieldNames.get(i2);
                        if (i2 != 0) {
                            serialize = false;
                        }
                        replaced = result.put(name, createBoundField(context, field, name, TypeToken.get(fieldType), serialize, deserialize));
                        if (replaced != null) {
                            replaced = replaced;
                        }
                        i2++;
                        serialize = serialize;
                        fieldNames = fieldNames;
                        size = size;
                        field = field;
                    }
                    if (replaced != null) {
                        throw new IllegalArgumentException(declaredType + " declares multiple JSON fields named " + replaced.name);
                    }
                }
                i++;
                z = false;
            }
            type2 = TypeToken.get(C$Gson$Types.resolve(type2.getType(), raw2, raw2.getGenericSuperclass()));
            raw2 = type2.getRawType();
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public static abstract class BoundField {
        final boolean deserialized;
        final String name;
        final boolean serialized;

        /* access modifiers changed from: package-private */
        public abstract void read(JsonReader jsonReader, Object obj) throws IOException, IllegalAccessException;

        /* access modifiers changed from: package-private */
        public abstract void write(JsonWriter jsonWriter, Object obj) throws IOException, IllegalAccessException;

        /* access modifiers changed from: package-private */
        public abstract boolean writeField(Object obj) throws IOException, IllegalAccessException;

        protected BoundField(String name2, boolean serialized2, boolean deserialized2) {
            this.name = name2;
            this.serialized = serialized2;
            this.deserialized = deserialized2;
        }
    }

    public static final class Adapter<T> extends TypeAdapter<T> {
        private final Map<String, BoundField> boundFields;
        private final ObjectConstructor<T> constructor;

        Adapter(ObjectConstructor<T> constructor2, Map<String, BoundField> boundFields2) {
            this.constructor = constructor2;
            this.boundFields = boundFields2;
        }

        @Override // com.huawei.gson.TypeAdapter
        public T read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            T instance = this.constructor.construct();
            try {
                in.beginObject();
                while (in.hasNext()) {
                    BoundField field = this.boundFields.get(in.nextName());
                    if (field != null) {
                        if (field.deserialized) {
                            field.read(in, instance);
                        }
                    }
                    in.skipValue();
                }
                in.endObject();
                return instance;
            } catch (IllegalStateException e) {
                throw new JsonSyntaxException(e);
            } catch (IllegalAccessException e2) {
                throw new AssertionError(e2);
            }
        }

        @Override // com.huawei.gson.TypeAdapter
        public void write(JsonWriter out, T value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            try {
                for (BoundField boundField : this.boundFields.values()) {
                    if (boundField.writeField(value)) {
                        out.name(boundField.name);
                        boundField.write(out, value);
                    }
                }
                out.endObject();
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
    }
}
