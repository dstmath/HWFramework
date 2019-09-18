package com.google.gson;

import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.Primitives;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.ArrayTypeAdapter;
import com.google.gson.internal.bind.CollectionTypeAdapterFactory;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.internal.bind.MapTypeAdapterFactory;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.internal.bind.SqlDateTypeAdapter;
import com.google.gson.internal.bind.TimeTypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
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
    private final ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>> calls;
    private final ConstructorConstructor constructorConstructor;
    private final Excluder excluder;
    private final List<TypeAdapterFactory> factories;
    private final FieldNamingStrategy fieldNamingStrategy;
    private final boolean generateNonExecutableJson;
    private final boolean htmlSafe;
    private final JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory;
    private final boolean lenient;
    private final boolean prettyPrinting;
    private final boolean serializeNulls;
    private final Map<TypeToken<?>, TypeAdapter<?>> typeTokenCache;

    static class FutureTypeAdapter<T> extends TypeAdapter<T> {
        private TypeAdapter<T> delegate;

        FutureTypeAdapter() {
        }

        public void setDelegate(TypeAdapter<T> typeAdapter) {
            if (this.delegate != null) {
                throw new AssertionError();
            }
            this.delegate = typeAdapter;
        }

        public T read(JsonReader in) throws IOException {
            if (this.delegate != null) {
                return this.delegate.read(in);
            }
            throw new IllegalStateException();
        }

        public void write(JsonWriter out, T value) throws IOException {
            if (this.delegate == null) {
                throw new IllegalStateException();
            }
            this.delegate.write(out, value);
        }
    }

    public Gson() {
        this(Excluder.DEFAULT, FieldNamingPolicy.IDENTITY, Collections.emptyMap(), false, false, false, DEFAULT_ESCAPE_HTML, false, false, false, LongSerializationPolicy.DEFAULT, Collections.emptyList());
    }

    Gson(Excluder excluder2, FieldNamingStrategy fieldNamingStrategy2, Map<Type, InstanceCreator<?>> instanceCreators, boolean serializeNulls2, boolean complexMapKeySerialization, boolean generateNonExecutableGson, boolean htmlSafe2, boolean prettyPrinting2, boolean lenient2, boolean serializeSpecialFloatingPointValues, LongSerializationPolicy longSerializationPolicy, List<TypeAdapterFactory> typeAdapterFactories) {
        this.calls = new ThreadLocal<>();
        this.typeTokenCache = new ConcurrentHashMap();
        this.constructorConstructor = new ConstructorConstructor(instanceCreators);
        this.excluder = excluder2;
        this.fieldNamingStrategy = fieldNamingStrategy2;
        this.serializeNulls = serializeNulls2;
        this.generateNonExecutableJson = generateNonExecutableGson;
        this.htmlSafe = htmlSafe2;
        this.prettyPrinting = prettyPrinting2;
        this.lenient = lenient2;
        List<TypeAdapterFactory> factories2 = new ArrayList<>();
        factories2.add(TypeAdapters.JSON_ELEMENT_FACTORY);
        factories2.add(ObjectTypeAdapter.FACTORY);
        factories2.add(excluder2);
        factories2.addAll(typeAdapterFactories);
        factories2.add(TypeAdapters.STRING_FACTORY);
        factories2.add(TypeAdapters.INTEGER_FACTORY);
        factories2.add(TypeAdapters.BOOLEAN_FACTORY);
        factories2.add(TypeAdapters.BYTE_FACTORY);
        factories2.add(TypeAdapters.SHORT_FACTORY);
        TypeAdapter<Number> longAdapter = longAdapter(longSerializationPolicy);
        factories2.add(TypeAdapters.newFactory(Long.TYPE, Long.class, longAdapter));
        factories2.add(TypeAdapters.newFactory(Double.TYPE, Double.class, doubleAdapter(serializeSpecialFloatingPointValues)));
        factories2.add(TypeAdapters.newFactory(Float.TYPE, Float.class, floatAdapter(serializeSpecialFloatingPointValues)));
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
        factories2.add(new MapTypeAdapterFactory(this.constructorConstructor, complexMapKeySerialization));
        this.jsonAdapterFactory = new JsonAdapterAnnotationTypeAdapterFactory(this.constructorConstructor);
        factories2.add(this.jsonAdapterFactory);
        factories2.add(TypeAdapters.ENUM_FACTORY);
        factories2.add(new ReflectiveTypeAdapterFactory(this.constructorConstructor, fieldNamingStrategy2, excluder2, this.jsonAdapterFactory));
        this.factories = Collections.unmodifiableList(factories2);
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

    private TypeAdapter<Number> doubleAdapter(boolean serializeSpecialFloatingPointValues) {
        if (serializeSpecialFloatingPointValues) {
            return TypeAdapters.DOUBLE;
        }
        return new TypeAdapter<Number>() {
            public Double read(JsonReader in) throws IOException {
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

    private TypeAdapter<Number> floatAdapter(boolean serializeSpecialFloatingPointValues) {
        if (serializeSpecialFloatingPointValues) {
            return TypeAdapters.FLOAT;
        }
        return new TypeAdapter<Number>() {
            public Float read(JsonReader in) throws IOException {
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

    private static TypeAdapter<Number> longAdapter(LongSerializationPolicy longSerializationPolicy) {
        if (longSerializationPolicy == LongSerializationPolicy.DEFAULT) {
            return TypeAdapters.LONG;
        }
        return new TypeAdapter<Number>() {
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
            public void write(JsonWriter out, AtomicLong value) throws IOException {
                longAdapter.write(out, Long.valueOf(value.get()));
            }

            public AtomicLong read(JsonReader in) throws IOException {
                return new AtomicLong(((Number) longAdapter.read(in)).longValue());
            }
        }.nullSafe();
    }

    private static TypeAdapter<AtomicLongArray> atomicLongArrayAdapter(final TypeAdapter<Number> longAdapter) {
        return new TypeAdapter<AtomicLongArray>() {
            public void write(JsonWriter out, AtomicLongArray value) throws IOException {
                out.beginArray();
                int length = value.length();
                for (int i = 0; i < length; i++) {
                    longAdapter.write(out, Long.valueOf(value.get(i)));
                }
                out.endArray();
            }

            public AtomicLongArray read(JsonReader in) throws IOException {
                List<Long> list = new ArrayList<>();
                in.beginArray();
                while (in.hasNext()) {
                    list.add(Long.valueOf(((Number) longAdapter.read(in)).longValue()));
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

    public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
        TypeAdapter<?> cached = this.typeTokenCache.get(type == null ? NULL_KEY_SURROGATE : type);
        if (cached != null) {
            return cached;
        }
        Map<TypeToken<?>, FutureTypeAdapter<?>> threadCalls = this.calls.get();
        boolean requiresThreadLocalCleanup = false;
        if (threadCalls == null) {
            threadCalls = new HashMap<>();
            this.calls.set(threadCalls);
            requiresThreadLocalCleanup = DEFAULT_ESCAPE_HTML;
        }
        TypeAdapter<?> ongoingCall = (FutureTypeAdapter) threadCalls.get(type);
        if (ongoingCall != null) {
            return ongoingCall;
        }
        try {
            FutureTypeAdapter<T> call = new FutureTypeAdapter<>();
            threadCalls.put(type, call);
            for (TypeAdapterFactory factory : this.factories) {
                TypeAdapter<?> candidate = factory.create(this, type);
                if (candidate != null) {
                    call.setDelegate(candidate);
                    this.typeTokenCache.put(type, candidate);
                    return candidate;
                }
            }
            throw new IllegalArgumentException("GSON cannot handle " + type);
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
                skipPastFound = DEFAULT_ESCAPE_HTML;
            }
        }
        throw new IllegalArgumentException("GSON cannot serialize " + type);
    }

    public <T> TypeAdapter<T> getAdapter(Class<T> type) {
        return getAdapter(TypeToken.get(type));
    }

    public JsonElement toJsonTree(Object src) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        return toJsonTree(src, src.getClass());
    }

    public JsonElement toJsonTree(Object src, Type typeOfSrc) {
        JsonTreeWriter writer = new JsonTreeWriter();
        toJson(src, typeOfSrc, (JsonWriter) writer);
        return writer.get();
    }

    public String toJson(Object src) {
        if (src == null) {
            return toJson((JsonElement) JsonNull.INSTANCE);
        }
        return toJson(src, (Type) src.getClass());
    }

    public String toJson(Object src, Type typeOfSrc) {
        StringWriter writer = new StringWriter();
        toJson(src, typeOfSrc, (Appendable) writer);
        return writer.toString();
    }

    public void toJson(Object src, Appendable writer) throws JsonIOException {
        if (src != null) {
            toJson(src, (Type) src.getClass(), writer);
        } else {
            toJson((JsonElement) JsonNull.INSTANCE, writer);
        }
    }

    public void toJson(Object src, Type typeOfSrc, Appendable writer) throws JsonIOException {
        try {
            toJson(src, typeOfSrc, newJsonWriter(Streams.writerForAppendable(writer)));
        } catch (IOException e) {
            throw new JsonIOException((Throwable) e);
        }
    }

    public void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws JsonIOException {
        TypeAdapter<?> adapter = getAdapter(TypeToken.get(typeOfSrc));
        boolean oldLenient = writer.isLenient();
        writer.setLenient(DEFAULT_ESCAPE_HTML);
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
            throw new JsonIOException((Throwable) e);
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
            throw new JsonIOException((Throwable) e);
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
        writer.setLenient(DEFAULT_ESCAPE_HTML);
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
            throw new JsonIOException((Throwable) e);
        } catch (Throwable th) {
            writer.setLenient(oldLenient);
            writer.setHtmlSafe(oldHtmlSafe);
            writer.setSerializeNulls(oldSerializeNulls);
            throw th;
        }
    }

    public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return Primitives.wrap(classOfT).cast(fromJson(json, (Type) classOfT));
    }

    public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        if (json == null) {
            return null;
        }
        return fromJson((Reader) new StringReader(json), typeOfT);
    }

    public <T> T fromJson(Reader json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
        JsonReader jsonReader = newJsonReader(json);
        Object object = fromJson(jsonReader, (Type) classOfT);
        assertFullConsumption(object, jsonReader);
        return Primitives.wrap(classOfT).cast(object);
    }

    public <T> T fromJson(Reader json, Type typeOfT) throws JsonIOException, JsonSyntaxException {
        JsonReader jsonReader = newJsonReader(json);
        T object = fromJson(jsonReader, typeOfT);
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
                throw new JsonSyntaxException((Throwable) e);
            } catch (IOException e2) {
                throw new JsonIOException((Throwable) e2);
            }
        }
    }

    public <T> T fromJson(JsonReader reader, Type typeOfT) throws JsonIOException, JsonSyntaxException {
        boolean isEmpty = DEFAULT_ESCAPE_HTML;
        boolean oldLenient = reader.isLenient();
        reader.setLenient(DEFAULT_ESCAPE_HTML);
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
            throw new JsonSyntaxException((Throwable) e);
        } catch (IllegalStateException e2) {
            throw new JsonSyntaxException((Throwable) e2);
        } catch (IOException e3) {
            throw new JsonSyntaxException((Throwable) e3);
        } catch (Throwable th) {
            reader.setLenient(oldLenient);
            throw th;
        }
    }

    public <T> T fromJson(JsonElement json, Class<T> classOfT) throws JsonSyntaxException {
        return Primitives.wrap(classOfT).cast(fromJson(json, (Type) classOfT));
    }

    public <T> T fromJson(JsonElement json, Type typeOfT) throws JsonSyntaxException {
        if (json == null) {
            return null;
        }
        return fromJson((JsonReader) new JsonTreeReader(json), typeOfT);
    }

    public String toString() {
        return "{serializeNulls:" + this.serializeNulls + ",factories:" + this.factories + ",instanceCreators:" + this.constructorConstructor + "}";
    }
}
