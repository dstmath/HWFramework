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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {
    private final ConstructorConstructor constructorConstructor;
    private final Excluder excluder;
    private final FieldNamingStrategy fieldNamingPolicy;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;

    public static final class Adapter<T> extends TypeAdapter<T> {
        private final Map<String, BoundField> boundFields;
        private final ObjectConstructor<T> constructor;

        Adapter(ObjectConstructor<T> constructor2, Map<String, BoundField> boundFields2) {
            this.constructor = constructor2;
            this.boundFields = boundFields2;
        }

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
                    if (field == null || !field.deserialized) {
                        in.skipValue();
                    } else {
                        field.read(in, instance);
                    }
                }
                in.endObject();
                return instance;
            } catch (IllegalStateException e) {
                throw new JsonSyntaxException((Throwable) e);
            } catch (IllegalAccessException e2) {
                throw new AssertionError(e2);
            }
        }

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

    static abstract class BoundField {
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

    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> raw = type.getRawType();
        if (!Object.class.isAssignableFrom(raw)) {
            return null;
        }
        return new Adapter(this.constructorConstructor.get(type), getBoundFields(gson, type, raw));
    }

    private BoundField createBoundField(Gson context, Field field, String name, TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
        final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
        JsonAdapter annotation = (JsonAdapter) field.getAnnotation(JsonAdapter.class);
        TypeAdapter<?> mapped = null;
        if (annotation != null) {
            mapped = this.jsonAdapterFactory.getTypeAdapter(this.constructorConstructor, context, fieldType, annotation);
        }
        final boolean jsonAdapterPresent = mapped != null;
        if (mapped == null) {
            mapped = context.getAdapter(fieldType);
        }
        final TypeAdapter<?> typeAdapter = mapped;
        final Field field2 = field;
        final Gson gson = context;
        final TypeToken<?> typeToken = fieldType;
        return new BoundField(name, serialize, deserialize) {
            /* access modifiers changed from: package-private */
            public void write(JsonWriter writer, Object value) throws IOException, IllegalAccessException {
                TypeAdapter t;
                Object fieldValue = field2.get(value);
                if (jsonAdapterPresent) {
                    t = typeAdapter;
                } else {
                    t = new TypeAdapterRuntimeTypeWrapper(gson, typeAdapter, typeToken.getType());
                }
                t.write(writer, fieldValue);
            }

            /* access modifiers changed from: package-private */
            public void read(JsonReader reader, Object value) throws IOException, IllegalAccessException {
                Object fieldValue = typeAdapter.read(reader);
                if (fieldValue != null || !isPrimitive) {
                    field2.set(value, fieldValue);
                }
            }

            public boolean writeField(Object value) throws IOException, IllegalAccessException {
                if (this.serialized && field2.get(value) != value) {
                    return true;
                }
                return false;
            }
        };
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.Class<java.lang.Object>} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r23v2, resolved type: com.google.gson.reflect.TypeToken<?>} */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a6, code lost:
        r23 = com.google.gson.reflect.TypeToken.get(com.google.gson.internal.C$Gson$Types.resolve(r23.getType(), r24, r24.getGenericSuperclass()));
        r24 = r23.getRawType();
        r23 = r23;
     */
    /* JADX WARNING: Incorrect type for immutable var: ssa=java.lang.Class<?>, code=java.lang.Class, for r24v0, types: [java.lang.Class<?>, java.lang.Class] */
    /* JADX WARNING: Multi-variable type inference failed */
    private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class raw) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        if (!raw.isInterface()) {
            Type declaredType = type.getType();
            TypeToken<?> type2 = type;
            while (raw != Object.class) {
                Field[] fields = raw.getDeclaredFields();
                int length = fields.length;
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 >= length) {
                        break;
                    }
                    Field field = fields[i2];
                    boolean serialize = excludeField(field, true);
                    boolean deserialize = excludeField(field, false);
                    if (serialize || deserialize) {
                        field.setAccessible(true);
                        Type fieldType = C$Gson$Types.resolve(type2.getType(), raw, field.getGenericType());
                        List<String> fieldNames = getFieldNames(field);
                        BoundField previous = null;
                        int size = fieldNames.size();
                        for (int i3 = 0; i3 < size; i3++) {
                            String name = fieldNames.get(i3);
                            if (i3 != 0) {
                                serialize = false;
                            }
                            LinkedHashMap linkedHashMap2 = linkedHashMap;
                            BoundField replaced = (BoundField) linkedHashMap2.put(name, createBoundField(context, field, name, TypeToken.get(fieldType), serialize, deserialize));
                            if (previous == null) {
                                previous = replaced;
                            }
                        }
                        if (previous != null) {
                            throw new IllegalArgumentException(declaredType + " declares multiple JSON fields named " + previous.name);
                        }
                    }
                    i = i2 + 1;
                }
            }
        }
        return linkedHashMap;
    }
}
