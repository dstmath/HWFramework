package com.huawei.gson;

import com.huawei.gson.internal.ConstructorConstructor;
import com.huawei.gson.internal.Excluder;
import com.huawei.gson.internal.Primitives;
import com.huawei.gson.internal.Streams;
import com.huawei.gson.internal.bind.ArrayTypeAdapter;
import com.huawei.gson.internal.bind.CollectionTypeAdapterFactory;
import com.huawei.gson.internal.bind.DateTypeAdapter;
import com.huawei.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
import com.huawei.gson.internal.bind.JsonTreeReader;
import com.huawei.gson.internal.bind.JsonTreeWriter;
import com.huawei.gson.internal.bind.MapTypeAdapterFactory;
import com.huawei.gson.internal.bind.ObjectTypeAdapter;
import com.huawei.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.huawei.gson.internal.bind.SqlDateTypeAdapter;
import com.huawei.gson.internal.bind.TimeTypeAdapter;
import com.huawei.gson.internal.bind.TypeAdapters;
import com.huawei.gson.reflect.TypeToken;
import com.huawei.gson.stream.JsonReader;
import com.huawei.gson.stream.JsonToken;
import com.huawei.gson.stream.JsonWriter;
import com.huawei.gson.stream.MalformedJsonException;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public final class Gson {
    static final boolean DEFAULT_COMPLEX_MAP_KEYS = false;
    static final boolean DEFAULT_ESCAPE_HTML = true;
    static final boolean DEFAULT_JSON_NON_EXECUTABLE = false;
    static final boolean DEFAULT_LENIENT = false;
    static final boolean DEFAULT_PRETTY_PRINT = false;
    static final boolean DEFAULT_SERIALIZE_NULLS = false;
    static final boolean DEFAULT_SPECIALIZE_FLOAT_VALUES = false;
    private static final String JSON_NON_EXECUTABLE_PREFIX = ")]}'\n";
    private static final TypeToken<?> NULL_KEY_SURROGATE = TypeToken.get(Object.class);
    final List<TypeAdapterFactory> builderFactories;
    final List<TypeAdapterFactory> builderHierarchyFactories;
    private final ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>> calls;
    final boolean complexMapKeySerialization;
    private final ConstructorConstructor constructorConstructor;
    final String datePattern;
    final int dateStyle;
    final Excluder excluder;
    final List<TypeAdapterFactory> factories;
    final FieldNamingStrategy fieldNamingStrategy;
    final boolean generateNonExecutableJson;
    final boolean htmlSafe;
    final Map<Type, InstanceCreator<?>> instanceCreators;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;
    final boolean lenient;
    final LongSerializationPolicy longSerializationPolicy;
    final boolean prettyPrinting;
    final boolean serializeNulls;
    final boolean serializeSpecialFloatingPointValues;
    final int timeStyle;
    private final Map<TypeToken<?>, TypeAdapter<?>> typeTokenCache;

    public Gson() {
        this(Excluder.DEFAULT, FieldNamingPolicy.IDENTITY, Collections.emptyMap(), false, false, false, true, false, false, false, LongSerializationPolicy.DEFAULT, null, 2, 2, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    Gson(Excluder excluder2, FieldNamingStrategy fieldNamingStrategy2, Map<Type, InstanceCreator<?>> instanceCreators2, boolean serializeNulls2, boolean complexMapKeySerialization2, boolean generateNonExecutableGson, boolean htmlSafe2, boolean prettyPrinting2, boolean lenient2, boolean serializeSpecialFloatingPointValues2, LongSerializationPolicy longSerializationPolicy2, String datePattern2, int dateStyle2, int timeStyle2, List<TypeAdapterFactory> builderFactories2, List<TypeAdapterFactory> builderHierarchyFactories2, List<TypeAdapterFactory> factoriesToBeAdded) {
        this.calls = new ThreadLocal<>();
        this.typeTokenCache = new ConcurrentHashMap();
        this.excluder = excluder2;
        this.fieldNamingStrategy = fieldNamingStrategy2;
        this.instanceCreators = instanceCreators2;
        this.constructorConstructor = new ConstructorConstructor(instanceCreators2);
        this.serializeNulls = serializeNulls2;
        this.complexMapKeySerialization = complexMapKeySerialization2;
        this.generateNonExecutableJson = generateNonExecutableGson;
        this.htmlSafe = htmlSafe2;
        this.prettyPrinting = prettyPrinting2;
        this.lenient = lenient2;
        this.serializeSpecialFloatingPointValues = serializeSpecialFloatingPointValues2;
        this.longSerializationPolicy = longSerializationPolicy2;
        this.datePattern = datePattern2;
        this.dateStyle = dateStyle2;
        this.timeStyle = timeStyle2;
        this.builderFactories = builderFactories2;
        this.builderHierarchyFactories = builderHierarchyFactories2;
        List<TypeAdapterFactory> factories2 = new ArrayList<>();
        factories2.add(TypeAdapters.JSON_ELEMENT_FACTORY);
        factories2.add(ObjectTypeAdapter.FACTORY);
        factories2.add(excluder2);
        factories2.addAll(factoriesToBeAdded);
        factories2.add(TypeAdapters.STRING_FACTORY);
        factories2.add(TypeAdapters.INTEGER_FACTORY);
        factories2.add(TypeAdapters.BOOLEAN_FACTORY);
        factories2.add(TypeAdapters.BYTE_FACTORY);
        factories2.add(TypeAdapters.SHORT_FACTORY);
        TypeAdapter<Number> longAdapter = longAdapter(longSerializationPolicy2);
        factories2.add(TypeAdapters.newFactory(Long.TYPE, Long.class, longAdapter));
        factories2.add(TypeAdapters.newFactory(Double.TYPE, Double.class, doubleAdapter(serializeSpecialFloatingPointValues2)));
        factories2.add(TypeAdapters.newFactory(Float.TYPE, Float.class, floatAdapter(serializeSpecialFloatingPointValues2)));
        factories2.add(TypeAdapters.NUMBER_FACTORY);
        factories2.add(TypeAdapters.ATOMIC_INTEGER_FACTORY);
        factories2.add(TypeAdapters.ATOMIC_BOOLEAN_FACTORY);
        factories2.add(TypeAdapters.newFactory(AtomicLong.class, atomicLongAdapter(longAdapter)));
        factories2.add(TypeAdapters.newFactory(AtomicLongArray.class, atomicLongArrayAdapter(longAdapter)));
        factories2.add(TypeAdapters.ATOMIC_INTEGER_ARRAY_FACTORY);
        factories2.add(TypeAdapters.CHARACTER_FACTORY);
        factories2.add(TypeAdapters.STRING_BUILDER_FACTORY);
        factories2.add(TypeAdapters.STRING_BUFFER_FACTORY);
        factories2.add(TypeAdapters.newFactory(BigDecimal.class, TypeAdapters.BIG_DECIMAL));
        factories2.add(TypeAdapters.newFactory(BigInteger.class, TypeAdapters.BIG_INTEGER));
        factories2.add(TypeAdapters.URL_FACTORY);
        factories2.add(TypeAdapters.URI_FACTORY);
        factories2.add(TypeAdapters.UUID_FACTORY);
        factories2.add(TypeAdapters.CURRENCY_FACTORY);
        factories2.add(TypeAdapters.LOCALE_FACTORY);
        factories2.add(TypeAdapters.INET_ADDRESS_FACTORY);
        factories2.add(TypeAdapters.BIT_SET_FACTORY);
        factories2.add(DateTypeAdapter.FACTORY);
        factories2.add(TypeAdapters.CALENDAR_FACTORY);
        factories2.add(TimeTypeAdapter.FACTORY);
        factories2.add(SqlDateTypeAdapter.FACTORY);
        factories2.add(TypeAdapters.TIMESTAMP_FACTORY);
        factories2.add(ArrayTypeAdapter.FACTORY);
        factories2.add(TypeAdapters.CLASS_FACTORY);
        factories2.add(new CollectionTypeAdapterFactory(this.constructorConstructor));
        factories2.add(new MapTypeAdapterFactory(this.constructorConstructor, complexMapKeySerialization2));
        this.jsonAdapterFactory = new JsonAdapterAnnotationTypeAdapterFactory(this.constructorConstructor);
        factories2.add(this.jsonAdapterFactory);
        factories2.add(TypeAdapters.ENUM_FACTORY);
        factories2.add(new ReflectiveTypeAdapterFactory(this.constructorConstructor, fieldNamingStrategy2, excluder2, this.jsonAdapterFactory));
        this.factories = Collections.unmodifiableList(factories2);
    }

    public GsonBuilder newBuilder() {
        return new GsonBuilder(this);
    }

    public Excluder excluder() {
        return this.excluder;
    }

    public FieldNamingStrategy fieldNamingStrategy() {
        return this.fieldNamingStrategy;
    }

    public boolean serializeNulls() {
        return this.serializeNulls;
    }

    public boolean htmlSafe() {
        return this.htmlSafe;
    }

    private TypeAdapter<Number> doubleAdapter(boolean serializeSpecialFloatingPointValues2) {
        if (serializeSpecialFloatingPointValues2) {
            return TypeAdapters.DOUBLE;
        }
        return new TypeAdapter<Number>() {
            /* class com.huawei.gson.Gson.AnonymousClass1 */

            /* Return type fixed from 'java.lang.Double' to match base method */
            @Override // com.huawei.gson.TypeAdapter
            public Number read(JsonReader in) throws IOException {
                if (in.peek() != JsonToken.NULL) {
                    return Double.valueOf(in.nextDouble());
                }
                in.nextNull();
                return null;
            }

            public void write(JsonWriter out, Number value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                Gson.checkValidFloatingPoint(value.doubleValue());
                out.value(value);
            }
        };
    }

    private TypeAdapter<Number> floatAdapter(boolean serializeSpecialFloatingPointValues2) {
        if (serializeSpecialFloatingPointValues2) {
            return TypeAdapters.FLOAT;
        }
        return new TypeAdapter<Number>() {
            /* class com.huawei.gson.Gson.AnonymousClass2 */

            /* Return type fixed from 'java.lang.Float' to match base method */
            @Override // com.huawei.gson.TypeAdapter
            public Number read(JsonReader in) throws IOException {
                if (in.peek() != JsonToken.NULL) {
                    return Float.valueOf((float) in.nextDouble());
                }
                in.nextNull();
                return null;
            }

            public void write(JsonWriter out, Number value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                Gson.checkValidFloatingPoint((double) value.floatValue());
                out.value(value);
            }
        };
    }

    static void checkValidFloatingPoint(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(value + " is not a valid double value as per JSON specification. To override this behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.");
        }
    }

    private static TypeAdapter<Number> longAdapter(LongSerializationPolicy longSerializationPolicy2) {
        if (longSerializationPolicy2 == LongSerializationPolicy.DEFAULT) {
            return TypeAdapters.LONG;
        }
        return new TypeAdapter<Number>() {
            /* class com.huawei.gson.Gson.AnonymousClass3 */

            @Override // com.huawei.gson.TypeAdapter
            public Number read(JsonReader in) throws IOException {
                if (in.peek() != JsonToken.NULL) {
                    return Long.valueOf(in.nextLong());
                }
                in.nextNull();
                return null;
            }

            public void write(JsonWriter out, Number value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    out.value(value.toString());
                }
            }
        };
    }

    private static TypeAdapter<AtomicLong> atomicLongAdapter(final TypeAdapter<Number> longAdapter) {
        return new TypeAdapter<AtomicLong>() {
            /* class com.huawei.gson.Gson.AnonymousClass4 */

            public void write(JsonWriter out, AtomicLong value) throws IOException {
                TypeAdapter.this.write(out, Long.valueOf(value.get()));
            }

            @Override // com.huawei.gson.TypeAdapter
            public AtomicLong read(JsonReader in) throws IOException {
                return new AtomicLong(((Number) TypeAdapter.this.read(in)).longValue());
            }
        }.nullSafe();
    }

    private static TypeAdapter<AtomicLongArray> atomicLongArrayAdapter(final TypeAdapter<Number> longAdapter) {
        return new TypeAdapter<AtomicLongArray>() {
            /* class com.huawei.gson.Gson.AnonymousClass5 */

            public void write(JsonWriter out, AtomicLongArray value) throws IOException {
                out.beginArray();
                int length = value.length();
                for (int i = 0; i < length; i++) {
                    TypeAdapter.this.write(out, Long.valueOf(value.get(i)));
                }
                out.endArray();
            }

            @Override // com.huawei.gson.TypeAdapter
            public AtomicLongArray read(JsonReader in) throws IOException {
                List<Long> list = new ArrayList<>();
                in.beginArray();
                while (in.hasNext()) {
                    list.add(Long.valueOf(((Number) TypeAdapter.this.read(in)).longValue()));
                }
                in.endArray();
                int length = list.size();
                AtomicLongArray array = new AtomicLongArray(length);
                for (int i = 0; i < length; i++) {
                    array.set(i, list.get(i).longValue());
                }
                return array;
            }
        }.nullSafe();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v4, resolved type: java.util.Map<com.huawei.gson.reflect.TypeToken<?>, com.huawei.gson.TypeAdapter<?>> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r7v1. Raw type applied. Possible types: com.huawei.gson.TypeAdapter<T>, com.huawei.gson.TypeAdapter<?> */
    public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
        TypeAdapter<T> typeAdapter = (TypeAdapter<T>) this.typeTokenCache.get(type == null ? NULL_KEY_SURROGATE : type);
        if (typeAdapter != null) {
            return typeAdapter;
        }
        Map<TypeToken<?>, FutureTypeAdapter<?>> threadCalls = this.calls.get();
        boolean requiresThreadLocalCleanup = false;
        if (threadCalls == null) {
            threadCalls = new HashMap();
            this.calls.set(threadCalls);
            requiresThreadLocalCleanup = true;
        }
        FutureTypeAdapter<?> futureTypeAdapter = threadCalls.get(type);
        if (futureTypeAdapter != null) {
            return futureTypeAdapter;
        }
        try {
            FutureTypeAdapter<?> futureTypeAdapter2 = new FutureTypeAdapter<>();
            threadCalls.put(type, futureTypeAdapter2);
            for (TypeAdapterFactory factory : this.factories) {
                TypeAdapter typeAdapter2 = (TypeAdapter<T>) factory.create(this, type);
                if (typeAdapter2 != null) {
                    futureTypeAdapter2.setDelegate(typeAdapter2);
                    this.typeTokenCache.put(type, typeAdapter2);
                    return typeAdapter2;
                }
            }
            throw new IllegalArgumentException("GSON (2.8.6) cannot handle " + type);
        } finally {
            threadCalls.remove(type);
            if (requiresThreadLocalCleanup) {
                this.calls.remove();
            }
        }
    }

    public <T> TypeAdapter<T> getDelegateAdapter(TypeAdapterFactory skipPast, TypeToken<T> type) {
        if (!this.factories.contains(skipPast)) {
            skipPast = this.jsonAdapterFactory;
        }
        boolean skipPastFound = false;
        for (TypeAdapterFactory factory : this.factories) {
            if (skipPastFound) {
                TypeAdapter<T> candidate = factory.create(this, type);
                if (candidate != null) {
                    return candidate;
                }
            } else if (factory == skipPast) {
                skipPastFound = true;
            }
        }
        throw new IllegalArgumentException("GSON cannot serialize " + type);
    }

    public <T> TypeAdapter<T> getAdapter(Class<T> type) {
        return getAdapter(TypeToken.get((Class) type));
    }

    public JsonElement toJsonTree(Object src) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        return toJsonTree(src, src.getClass());
    }

    public JsonElement toJsonTree(Object src, Type typeOfSrc) {
        JsonTreeWriter writer = new JsonTreeWriter();
        toJson(src, typeOfSrc, writer);
        return writer.get();
    }

    public String toJson(Object src) {
        if (src == null) {
            return toJson((JsonElement) JsonNull.INSTANCE);
        }
        return toJson(src, src.getClass());
    }

    public String toJson(Object src, Type typeOfSrc) {
        StringWriter writer = new StringWriter();
        toJson(src, typeOfSrc, writer);
        return writer.toString();
    }

    public void toJson(Object src, Appendable writer) throws JsonIOException {
        if (src != null) {
            toJson(src, src.getClass(), writer);
        } else {
            toJson((JsonElement) JsonNull.INSTANCE, writer);
        }
    }

    public void toJson(Object src, Type typeOfSrc, Appendable writer) throws JsonIOException {
        try {
            toJson(src, typeOfSrc, newJsonWriter(Streams.writerForAppendable(writer)));
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }

    public void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws JsonIOException {
        TypeAdapter<?> adapter = getAdapter(TypeToken.get(typeOfSrc));
        boolean oldLenient = writer.isLenient();
        writer.setLenient(true);
        boolean oldHtmlSafe = writer.isHtmlSafe();
        writer.setHtmlSafe(this.htmlSafe);
        boolean oldSerializeNulls = writer.getSerializeNulls();
        writer.setSerializeNulls(this.serializeNulls);
        try {
            adapter.write(writer, src);
            writer.setLenient(oldLenient);
            writer.setHtmlSafe(oldHtmlSafe);
            writer.setSerializeNulls(oldSerializeNulls);
        } catch (IOException e) {
            throw new JsonIOException(e);
        } catch (AssertionError e2) {
            AssertionError error = new AssertionError("AssertionError (GSON 2.8.6): " + e2.getMessage());
            error.initCause(e2);
            throw error;
        } catch (Throwable th) {
            writer.setLenient(oldLenient);
            writer.setHtmlSafe(oldHtmlSafe);
            writer.setSerializeNulls(oldSerializeNulls);
            throw th;
        }
    }

    public String toJson(JsonElement jsonElement) {
        StringWriter writer = new StringWriter();
        toJson(jsonElement, (Appendable) writer);
        return writer.toString();
    }

    public void toJson(JsonElement jsonElement, Appendable writer) throws JsonIOException {
        try {
            toJson(jsonElement, newJsonWriter(Streams.writerForAppendable(writer)));
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }

    public JsonWriter newJsonWriter(Writer writer) throws IOException {
        if (this.generateNonExecutableJson) {
            writer.write(JSON_NON_EXECUTABLE_PREFIX);
        }
        JsonWriter jsonWriter = new JsonWriter(writer);
        if (this.prettyPrinting) {
            jsonWriter.setIndent("  ");
        }
        jsonWriter.setSerializeNulls(this.serializeNulls);
        return jsonWriter;
    }

    public JsonReader newJsonReader(Reader reader) {
        JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(this.lenient);
        return jsonReader;
    }

    public void toJson(JsonElement jsonElement, JsonWriter writer) throws JsonIOException {
        boolean oldLenient = writer.isLenient();
        writer.setLenient(true);
        boolean oldHtmlSafe = writer.isHtmlSafe();
        writer.setHtmlSafe(this.htmlSafe);
        boolean oldSerializeNulls = writer.getSerializeNulls();
        writer.setSerializeNulls(this.serializeNulls);
        try {
            Streams.write(jsonElement, writer);
            writer.setLenient(oldLenient);
            writer.setHtmlSafe(oldHtmlSafe);
            writer.setSerializeNulls(oldSerializeNulls);
        } catch (IOException e) {
            throw new JsonIOException(e);
        } catch (AssertionError e2) {
            AssertionError error = new AssertionError("AssertionError (GSON 2.8.6): " + e2.getMessage());
            error.initCause(e2);
            throw error;
        } catch (Throwable th) {
            writer.setLenient(oldLenient);
            writer.setHtmlSafe(oldHtmlSafe);
            writer.setSerializeNulls(oldSerializeNulls);
            throw th;
        }
    }

    public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return (T) Primitives.wrap(classOfT).cast(fromJson(json, (Type) classOfT));
    }

    public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        if (json == null) {
            return null;
        }
        return (T) fromJson(new StringReader(json), typeOfT);
    }

    public <T> T fromJson(Reader json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
        JsonReader jsonReader = newJsonReader(json);
        Object object = fromJson(jsonReader, classOfT);
        assertFullConsumption(object, jsonReader);
        return (T) Primitives.wrap(classOfT).cast(object);
    }

    public <T> T fromJson(Reader json, Type typeOfT) throws JsonIOException, JsonSyntaxException {
        JsonReader jsonReader = newJsonReader(json);
        T object = (T) fromJson(jsonReader, typeOfT);
        assertFullConsumption(object, jsonReader);
        return object;
    }

    private static void assertFullConsumption(Object obj, JsonReader reader) {
        if (obj != null) {
            try {
                if (reader.peek() != JsonToken.END_DOCUMENT) {
                    throw new JsonIOException("JSON document was not fully consumed.");
                }
            } catch (MalformedJsonException e) {
                throw new JsonSyntaxException(e);
            } catch (IOException e2) {
                throw new JsonIOException(e2);
            }
        }
    }

    public <T> T fromJson(JsonReader reader, Type typeOfT) throws JsonIOException, JsonSyntaxException {
        boolean isEmpty = true;
        boolean oldLenient = reader.isLenient();
        reader.setLenient(true);
        try {
            reader.peek();
            isEmpty = false;
            T object = getAdapter(TypeToken.get(typeOfT)).read(reader);
            reader.setLenient(oldLenient);
            return object;
        } catch (EOFException e) {
            if (isEmpty) {
                reader.setLenient(oldLenient);
                return null;
            }
            throw new JsonSyntaxException(e);
        } catch (IllegalStateException e2) {
            throw new JsonSyntaxException(e2);
        } catch (IOException e3) {
            throw new JsonSyntaxException(e3);
        } catch (AssertionError e4) {
            AssertionError error = new AssertionError("AssertionError (GSON 2.8.6): " + e4.getMessage());
            error.initCause(e4);
            throw error;
        } catch (Throwable th) {
            reader.setLenient(oldLenient);
            throw th;
        }
    }

    public <T> T fromJson(JsonElement json, Class<T> classOfT) throws JsonSyntaxException {
        return (T) Primitives.wrap(classOfT).cast(fromJson(json, (Type) classOfT));
    }

    public <T> T fromJson(JsonElement json, Type typeOfT) throws JsonSyntaxException {
        if (json == null) {
            return null;
        }
        return (T) fromJson(new JsonTreeReader(json), typeOfT);
    }

    /* access modifiers changed from: package-private */
    public static class FutureTypeAdapter<T> extends TypeAdapter<T> {
        private TypeAdapter<T> delegate;

        FutureTypeAdapter() {
        }

        public void setDelegate(TypeAdapter<T> typeAdapter) {
            if (this.delegate == null) {
                this.delegate = typeAdapter;
                return;
            }
            throw new AssertionError();
        }

        @Override // com.huawei.gson.TypeAdapter
        public T read(JsonReader in) throws IOException {
            TypeAdapter<T> typeAdapter = this.delegate;
            if (typeAdapter != null) {
                return typeAdapter.read(in);
            }
            throw new IllegalStateException();
        }

        @Override // com.huawei.gson.TypeAdapter
        public void write(JsonWriter out, T value) throws IOException {
            TypeAdapter<T> typeAdapter = this.delegate;
            if (typeAdapter != null) {
                typeAdapter.write(out, value);
                return;
            }
            throw new IllegalStateException();
        }
    }

    public String toString() {
        return "{serializeNulls:" + this.serializeNulls + ",factories:" + this.factories + ",instanceCreators:" + this.constructorConstructor + "}";
    }
}
