package com.huawei.gson.internal.bind;

import com.huawei.gson.Gson;
import com.huawei.gson.JsonArray;
import com.huawei.gson.JsonElement;
import com.huawei.gson.JsonIOException;
import com.huawei.gson.JsonNull;
import com.huawei.gson.JsonObject;
import com.huawei.gson.JsonPrimitive;
import com.huawei.gson.JsonSyntaxException;
import com.huawei.gson.TypeAdapter;
import com.huawei.gson.TypeAdapterFactory;
import com.huawei.gson.annotations.SerializedName;
import com.huawei.gson.internal.LazilyParsedNumber;
import com.huawei.gson.reflect.TypeToken;
import com.huawei.gson.stream.JsonReader;
import com.huawei.gson.stream.JsonToken;
import com.huawei.gson.stream.JsonWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public final class TypeAdapters {
    public static final TypeAdapter<AtomicBoolean> ATOMIC_BOOLEAN = new TypeAdapter<AtomicBoolean>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass9 */

        @Override // com.huawei.gson.TypeAdapter
        public AtomicBoolean read(JsonReader in) throws IOException {
            return new AtomicBoolean(in.nextBoolean());
        }

        public void write(JsonWriter out, AtomicBoolean value) throws IOException {
            out.value(value.get());
        }
    }.nullSafe();
    public static final TypeAdapterFactory ATOMIC_BOOLEAN_FACTORY = newFactory(AtomicBoolean.class, ATOMIC_BOOLEAN);
    public static final TypeAdapter<AtomicInteger> ATOMIC_INTEGER = new TypeAdapter<AtomicInteger>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass8 */

        @Override // com.huawei.gson.TypeAdapter
        public AtomicInteger read(JsonReader in) throws IOException {
            try {
                return new AtomicInteger(in.nextInt());
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        public void write(JsonWriter out, AtomicInteger value) throws IOException {
            out.value((long) value.get());
        }
    }.nullSafe();
    public static final TypeAdapter<AtomicIntegerArray> ATOMIC_INTEGER_ARRAY = new TypeAdapter<AtomicIntegerArray>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass10 */

        @Override // com.huawei.gson.TypeAdapter
        public AtomicIntegerArray read(JsonReader in) throws IOException {
            List<Integer> list = new ArrayList<>();
            in.beginArray();
            while (in.hasNext()) {
                try {
                    list.add(Integer.valueOf(in.nextInt()));
                } catch (NumberFormatException e) {
                    throw new JsonSyntaxException(e);
                }
            }
            in.endArray();
            int length = list.size();
            AtomicIntegerArray array = new AtomicIntegerArray(length);
            for (int i = 0; i < length; i++) {
                array.set(i, list.get(i).intValue());
            }
            return array;
        }

        public void write(JsonWriter out, AtomicIntegerArray value) throws IOException {
            out.beginArray();
            int length = value.length();
            for (int i = 0; i < length; i++) {
                out.value((long) value.get(i));
            }
            out.endArray();
        }
    }.nullSafe();
    public static final TypeAdapterFactory ATOMIC_INTEGER_ARRAY_FACTORY = newFactory(AtomicIntegerArray.class, ATOMIC_INTEGER_ARRAY);
    public static final TypeAdapterFactory ATOMIC_INTEGER_FACTORY = newFactory(AtomicInteger.class, ATOMIC_INTEGER);
    public static final TypeAdapter<BigDecimal> BIG_DECIMAL = new TypeAdapter<BigDecimal>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass17 */

        @Override // com.huawei.gson.TypeAdapter
        public BigDecimal read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return new BigDecimal(in.nextString());
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        public void write(JsonWriter out, BigDecimal value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapter<BigInteger> BIG_INTEGER = new TypeAdapter<BigInteger>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass18 */

        @Override // com.huawei.gson.TypeAdapter
        public BigInteger read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return new BigInteger(in.nextString());
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        public void write(JsonWriter out, BigInteger value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapter<BitSet> BIT_SET = new TypeAdapter<BitSet>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass2 */

        @Override // com.huawei.gson.TypeAdapter
        public BitSet read(JsonReader in) throws IOException {
            BitSet bitset = new BitSet();
            in.beginArray();
            int i = 0;
            JsonToken tokenType = in.peek();
            while (tokenType != JsonToken.END_ARRAY) {
                int i2 = AnonymousClass36.$SwitchMap$com$google$gson$stream$JsonToken[tokenType.ordinal()];
                boolean set = false;
                if (i2 != 1) {
                    if (i2 == 2) {
                        set = in.nextBoolean();
                    } else if (i2 == 3) {
                        String stringValue = in.nextString();
                        try {
                            if (Integer.parseInt(stringValue) != 0) {
                                set = true;
                            }
                        } catch (NumberFormatException e) {
                            throw new JsonSyntaxException("Error: Expecting: bitset number value (1, 0), Found: " + stringValue);
                        }
                    } else {
                        throw new JsonSyntaxException("Invalid bitset value type: " + tokenType);
                    }
                } else if (in.nextInt() != 0) {
                    set = true;
                }
                if (set) {
                    bitset.set(i);
                }
                i++;
                tokenType = in.peek();
            }
            in.endArray();
            return bitset;
        }

        public void write(JsonWriter out, BitSet src) throws IOException {
            out.beginArray();
            int length = src.length();
            for (int i = 0; i < length; i++) {
                out.value(src.get(i) ? 1 : 0);
            }
            out.endArray();
        }
    }.nullSafe();
    public static final TypeAdapterFactory BIT_SET_FACTORY = newFactory(BitSet.class, BIT_SET);
    public static final TypeAdapter<Boolean> BOOLEAN = new TypeAdapter<Boolean>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass3 */

        @Override // com.huawei.gson.TypeAdapter
        public Boolean read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else if (peek == JsonToken.STRING) {
                return Boolean.valueOf(Boolean.parseBoolean(in.nextString()));
            } else {
                return Boolean.valueOf(in.nextBoolean());
            }
        }

        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapter<Boolean> BOOLEAN_AS_STRING = new TypeAdapter<Boolean>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass4 */

        @Override // com.huawei.gson.TypeAdapter
        public Boolean read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                return Boolean.valueOf(in.nextString());
            }
            in.nextNull();
            return null;
        }

        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value == null ? "null" : value.toString());
        }
    };
    public static final TypeAdapterFactory BOOLEAN_FACTORY = newFactory(Boolean.TYPE, Boolean.class, BOOLEAN);
    public static final TypeAdapter<Number> BYTE = new TypeAdapter<Number>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass5 */

        @Override // com.huawei.gson.TypeAdapter
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return Byte.valueOf((byte) in.nextInt());
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapterFactory BYTE_FACTORY = newFactory(Byte.TYPE, Byte.class, BYTE);
    public static final TypeAdapter<Calendar> CALENDAR = new TypeAdapter<Calendar>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass27 */
        private static final String DAY_OF_MONTH = "dayOfMonth";
        private static final String HOUR_OF_DAY = "hourOfDay";
        private static final String MINUTE = "minute";
        private static final String MONTH = "month";
        private static final String SECOND = "second";
        private static final String YEAR = "year";

        @Override // com.huawei.gson.TypeAdapter
        public Calendar read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            in.beginObject();
            int year = 0;
            int month = 0;
            int dayOfMonth = 0;
            int hourOfDay = 0;
            int minute = 0;
            int second = 0;
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                int value = in.nextInt();
                if (YEAR.equals(name)) {
                    year = value;
                } else if (MONTH.equals(name)) {
                    month = value;
                } else if (DAY_OF_MONTH.equals(name)) {
                    dayOfMonth = value;
                } else if (HOUR_OF_DAY.equals(name)) {
                    hourOfDay = value;
                } else if (MINUTE.equals(name)) {
                    minute = value;
                } else if (SECOND.equals(name)) {
                    second = value;
                }
            }
            in.endObject();
            return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
        }

        public void write(JsonWriter out, Calendar value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            out.name(YEAR);
            out.value((long) value.get(1));
            out.name(MONTH);
            out.value((long) value.get(2));
            out.name(DAY_OF_MONTH);
            out.value((long) value.get(5));
            out.name(HOUR_OF_DAY);
            out.value((long) value.get(11));
            out.name(MINUTE);
            out.value((long) value.get(12));
            out.name(SECOND);
            out.value((long) value.get(13));
            out.endObject();
        }
    };
    public static final TypeAdapterFactory CALENDAR_FACTORY = newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, CALENDAR);
    public static final TypeAdapter<Character> CHARACTER = new TypeAdapter<Character>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass15 */

        @Override // com.huawei.gson.TypeAdapter
        public Character read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String str = in.nextString();
            if (str.length() == 1) {
                return Character.valueOf(str.charAt(0));
            }
            throw new JsonSyntaxException("Expecting character, got: " + str);
        }

        public void write(JsonWriter out, Character value) throws IOException {
            out.value(value == null ? null : String.valueOf(value));
        }
    };
    public static final TypeAdapterFactory CHARACTER_FACTORY = newFactory(Character.TYPE, Character.class, CHARACTER);
    public static final TypeAdapter<Class> CLASS = new TypeAdapter<Class>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass1 */

        public void write(JsonWriter out, Class value) throws IOException {
            throw new UnsupportedOperationException("Attempted to serialize java.lang.Class: " + value.getName() + ". Forgot to register a type adapter?");
        }

        @Override // com.huawei.gson.TypeAdapter
        public Class read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException("Attempted to deserialize a java.lang.Class. Forgot to register a type adapter?");
        }
    }.nullSafe();
    public static final TypeAdapterFactory CLASS_FACTORY = newFactory(Class.class, CLASS);
    public static final TypeAdapter<Currency> CURRENCY = new TypeAdapter<Currency>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass25 */

        @Override // com.huawei.gson.TypeAdapter
        public Currency read(JsonReader in) throws IOException {
            return Currency.getInstance(in.nextString());
        }

        public void write(JsonWriter out, Currency value) throws IOException {
            out.value(value.getCurrencyCode());
        }
    }.nullSafe();
    public static final TypeAdapterFactory CURRENCY_FACTORY = newFactory(Currency.class, CURRENCY);
    public static final TypeAdapter<Number> DOUBLE = new TypeAdapter<Number>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass13 */

        @Override // com.huawei.gson.TypeAdapter
        public Number read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                return Double.valueOf(in.nextDouble());
            }
            in.nextNull();
            return null;
        }

        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapterFactory ENUM_FACTORY = new TypeAdapterFactory() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass30 */

        @Override // com.huawei.gson.TypeAdapterFactory
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Class<? super Object> rawType = typeToken.getRawType();
            if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
                return null;
            }
            if (!rawType.isEnum()) {
                rawType = rawType.getSuperclass();
            }
            return new EnumTypeAdapter(rawType);
        }
    };
    public static final TypeAdapter<Number> FLOAT = new TypeAdapter<Number>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass12 */

        @Override // com.huawei.gson.TypeAdapter
        public Number read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                return Float.valueOf((float) in.nextDouble());
            }
            in.nextNull();
            return null;
        }

        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapter<InetAddress> INET_ADDRESS = new TypeAdapter<InetAddress>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass23 */

        @Override // com.huawei.gson.TypeAdapter
        public InetAddress read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                return InetAddress.getByName(in.nextString());
            }
            in.nextNull();
            return null;
        }

        public void write(JsonWriter out, InetAddress value) throws IOException {
            out.value(value == null ? null : value.getHostAddress());
        }
    };
    public static final TypeAdapterFactory INET_ADDRESS_FACTORY = newTypeHierarchyFactory(InetAddress.class, INET_ADDRESS);
    public static final TypeAdapter<Number> INTEGER = new TypeAdapter<Number>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass7 */

        @Override // com.huawei.gson.TypeAdapter
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return Integer.valueOf(in.nextInt());
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapterFactory INTEGER_FACTORY = newFactory(Integer.TYPE, Integer.class, INTEGER);
    public static final TypeAdapter<JsonElement> JSON_ELEMENT = new TypeAdapter<JsonElement>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass29 */

        @Override // com.huawei.gson.TypeAdapter
        public JsonElement read(JsonReader in) throws IOException {
            switch (AnonymousClass36.$SwitchMap$com$google$gson$stream$JsonToken[in.peek().ordinal()]) {
                case 1:
                    return new JsonPrimitive(new LazilyParsedNumber(in.nextString()));
                case 2:
                    return new JsonPrimitive(Boolean.valueOf(in.nextBoolean()));
                case 3:
                    return new JsonPrimitive(in.nextString());
                case 4:
                    in.nextNull();
                    return JsonNull.INSTANCE;
                case 5:
                    JsonArray array = new JsonArray();
                    in.beginArray();
                    while (in.hasNext()) {
                        array.add(read(in));
                    }
                    in.endArray();
                    return array;
                case 6:
                    JsonObject object = new JsonObject();
                    in.beginObject();
                    while (in.hasNext()) {
                        object.add(in.nextName(), read(in));
                    }
                    in.endObject();
                    return object;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public void write(JsonWriter out, JsonElement value) throws IOException {
            if (value == null || value.isJsonNull()) {
                out.nullValue();
            } else if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    out.value(primitive.getAsNumber());
                } else if (primitive.isBoolean()) {
                    out.value(primitive.getAsBoolean());
                } else {
                    out.value(primitive.getAsString());
                }
            } else if (value.isJsonArray()) {
                out.beginArray();
                Iterator<JsonElement> it = value.getAsJsonArray().iterator();
                while (it.hasNext()) {
                    write(out, it.next());
                }
                out.endArray();
            } else if (value.isJsonObject()) {
                out.beginObject();
                for (Map.Entry<String, JsonElement> e : value.getAsJsonObject().entrySet()) {
                    out.name(e.getKey());
                    write(out, e.getValue());
                }
                out.endObject();
            } else {
                throw new IllegalArgumentException("Couldn't write " + value.getClass());
            }
        }
    };
    public static final TypeAdapterFactory JSON_ELEMENT_FACTORY = newTypeHierarchyFactory(JsonElement.class, JSON_ELEMENT);
    public static final TypeAdapter<Locale> LOCALE = new TypeAdapter<Locale>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass28 */

        @Override // com.huawei.gson.TypeAdapter
        public Locale read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            StringTokenizer tokenizer = new StringTokenizer(in.nextString(), "_");
            String language = null;
            String country = null;
            String variant = null;
            if (tokenizer.hasMoreElements()) {
                language = tokenizer.nextToken();
            }
            if (tokenizer.hasMoreElements()) {
                country = tokenizer.nextToken();
            }
            if (tokenizer.hasMoreElements()) {
                variant = tokenizer.nextToken();
            }
            if (country == null && variant == null) {
                return new Locale(language);
            }
            if (variant == null) {
                return new Locale(language, country);
            }
            return new Locale(language, country, variant);
        }

        public void write(JsonWriter out, Locale value) throws IOException {
            out.value(value == null ? null : value.toString());
        }
    };
    public static final TypeAdapterFactory LOCALE_FACTORY = newFactory(Locale.class, LOCALE);
    public static final TypeAdapter<Number> LONG = new TypeAdapter<Number>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass11 */

        @Override // com.huawei.gson.TypeAdapter
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return Long.valueOf(in.nextLong());
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapter<Number> NUMBER = new TypeAdapter<Number>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass14 */

        @Override // com.huawei.gson.TypeAdapter
        public Number read(JsonReader in) throws IOException {
            JsonToken jsonToken = in.peek();
            int i = AnonymousClass36.$SwitchMap$com$google$gson$stream$JsonToken[jsonToken.ordinal()];
            if (i == 1 || i == 3) {
                return new LazilyParsedNumber(in.nextString());
            }
            if (i == 4) {
                in.nextNull();
                return null;
            }
            throw new JsonSyntaxException("Expecting number, got: " + jsonToken);
        }

        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapterFactory NUMBER_FACTORY = newFactory(Number.class, NUMBER);
    public static final TypeAdapter<Number> SHORT = new TypeAdapter<Number>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass6 */

        @Override // com.huawei.gson.TypeAdapter
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return Short.valueOf((short) in.nextInt());
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapterFactory SHORT_FACTORY = newFactory(Short.TYPE, Short.class, SHORT);
    public static final TypeAdapter<String> STRING = new TypeAdapter<String>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass16 */

        @Override // com.huawei.gson.TypeAdapter
        public String read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else if (peek == JsonToken.BOOLEAN) {
                return Boolean.toString(in.nextBoolean());
            } else {
                return in.nextString();
            }
        }

        public void write(JsonWriter out, String value) throws IOException {
            out.value(value);
        }
    };
    public static final TypeAdapter<StringBuffer> STRING_BUFFER = new TypeAdapter<StringBuffer>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass20 */

        @Override // com.huawei.gson.TypeAdapter
        public StringBuffer read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                return new StringBuffer(in.nextString());
            }
            in.nextNull();
            return null;
        }

        public void write(JsonWriter out, StringBuffer value) throws IOException {
            out.value(value == null ? null : value.toString());
        }
    };
    public static final TypeAdapterFactory STRING_BUFFER_FACTORY = newFactory(StringBuffer.class, STRING_BUFFER);
    public static final TypeAdapter<StringBuilder> STRING_BUILDER = new TypeAdapter<StringBuilder>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass19 */

        @Override // com.huawei.gson.TypeAdapter
        public StringBuilder read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                return new StringBuilder(in.nextString());
            }
            in.nextNull();
            return null;
        }

        public void write(JsonWriter out, StringBuilder value) throws IOException {
            out.value(value == null ? null : value.toString());
        }
    };
    public static final TypeAdapterFactory STRING_BUILDER_FACTORY = newFactory(StringBuilder.class, STRING_BUILDER);
    public static final TypeAdapterFactory STRING_FACTORY = newFactory(String.class, STRING);
    public static final TypeAdapterFactory TIMESTAMP_FACTORY = new TypeAdapterFactory() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass26 */

        @Override // com.huawei.gson.TypeAdapterFactory
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            if (typeToken.getRawType() != Timestamp.class) {
                return null;
            }
            final TypeAdapter<Date> dateTypeAdapter = gson.getAdapter(Date.class);
            return new TypeAdapter<Timestamp>() {
                /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass26.AnonymousClass1 */

                @Override // com.huawei.gson.TypeAdapter
                public Timestamp read(JsonReader in) throws IOException {
                    Date date = (Date) dateTypeAdapter.read(in);
                    if (date != null) {
                        return new Timestamp(date.getTime());
                    }
                    return null;
                }

                public void write(JsonWriter out, Timestamp value) throws IOException {
                    dateTypeAdapter.write(out, value);
                }
            };
        }
    };
    public static final TypeAdapter<URI> URI = new TypeAdapter<URI>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass22 */

        @Override // com.huawei.gson.TypeAdapter
        public URI read(JsonReader in) throws IOException {
            URI uri = null;
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                String nextString = in.nextString();
                if (!"null".equals(nextString)) {
                    uri = new URI(nextString);
                }
                return uri;
            } catch (URISyntaxException e) {
                throw new JsonIOException(e);
            }
        }

        public void write(JsonWriter out, URI value) throws IOException {
            out.value(value == null ? null : value.toASCIIString());
        }
    };
    public static final TypeAdapterFactory URI_FACTORY = newFactory(URI.class, URI);
    public static final TypeAdapter<URL> URL = new TypeAdapter<URL>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass21 */

        @Override // com.huawei.gson.TypeAdapter
        public URL read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String nextString = in.nextString();
            if ("null".equals(nextString)) {
                return null;
            }
            return new URL(nextString);
        }

        public void write(JsonWriter out, URL value) throws IOException {
            out.value(value == null ? null : value.toExternalForm());
        }
    };
    public static final TypeAdapterFactory URL_FACTORY = newFactory(URL.class, URL);
    public static final TypeAdapter<UUID> UUID = new TypeAdapter<UUID>() {
        /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass24 */

        @Override // com.huawei.gson.TypeAdapter
        public UUID read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                return UUID.fromString(in.nextString());
            }
            in.nextNull();
            return null;
        }

        public void write(JsonWriter out, UUID value) throws IOException {
            out.value(value == null ? null : value.toString());
        }
    };
    public static final TypeAdapterFactory UUID_FACTORY = newFactory(UUID.class, UUID);

    private TypeAdapters() {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.gson.internal.bind.TypeAdapters$36  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass36 {
        static final /* synthetic */ int[] $SwitchMap$com$google$gson$stream$JsonToken = new int[JsonToken.values().length];

        static {
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.NUMBER.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.BOOLEAN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.STRING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.NULL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.BEGIN_ARRAY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.BEGIN_OBJECT.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.END_DOCUMENT.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.NAME.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.END_OBJECT.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$google$gson$stream$JsonToken[JsonToken.END_ARRAY.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    private static final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
        private final Map<T, String> constantToName = new HashMap();
        private final Map<String, T> nameToConstant = new HashMap();

        /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.gson.internal.bind.TypeAdapters$EnumTypeAdapter<T extends java.lang.Enum<T>> */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // com.huawei.gson.TypeAdapter
        public /* bridge */ /* synthetic */ void write(JsonWriter jsonWriter, Object obj) throws IOException {
            write(jsonWriter, (JsonWriter) ((Enum) obj));
        }

        public EnumTypeAdapter(Class<T> classOfT) {
            try {
                T[] enumConstants = classOfT.getEnumConstants();
                for (T constant : enumConstants) {
                    String name = constant.name();
                    SerializedName annotation = (SerializedName) classOfT.getField(name).getAnnotation(SerializedName.class);
                    if (annotation != null) {
                        name = annotation.value();
                        for (String alternate : annotation.alternate()) {
                            this.nameToConstant.put(alternate, constant);
                        }
                    }
                    this.nameToConstant.put(name, constant);
                    this.constantToName.put(constant, name);
                }
            } catch (NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        }

        @Override // com.huawei.gson.TypeAdapter
        public T read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                return this.nameToConstant.get(in.nextString());
            }
            in.nextNull();
            return null;
        }

        public void write(JsonWriter out, T value) throws IOException {
            out.value(value == null ? null : this.constantToName.get(value));
        }
    }

    public static <TT> TypeAdapterFactory newFactory(final TypeToken<TT> type, final TypeAdapter<TT> typeAdapter) {
        return new TypeAdapterFactory() {
            /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass31 */

            @Override // com.huawei.gson.TypeAdapterFactory
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
                if (typeToken.equals(TypeToken.this)) {
                    return typeAdapter;
                }
                return null;
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactory(final Class<TT> type, final TypeAdapter<TT> typeAdapter) {
        return new TypeAdapterFactory() {
            /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass32 */

            @Override // com.huawei.gson.TypeAdapterFactory
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
                if (typeToken.getRawType() == type) {
                    return typeAdapter;
                }
                return null;
            }

            public String toString() {
                return "Factory[type=" + type.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactory(final Class<TT> unboxed, final Class<TT> boxed, final TypeAdapter<? super TT> typeAdapter) {
        return new TypeAdapterFactory() {
            /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass33 */

            @Override // com.huawei.gson.TypeAdapterFactory
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
                Class<? super T> rawType = typeToken.getRawType();
                if (rawType == unboxed || rawType == boxed) {
                    return typeAdapter;
                }
                return null;
            }

            public String toString() {
                return "Factory[type=" + boxed.getName() + "+" + unboxed.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }

    public static <TT> TypeAdapterFactory newFactoryForMultipleTypes(final Class<TT> base, final Class<? extends TT> sub, final TypeAdapter<? super TT> typeAdapter) {
        return new TypeAdapterFactory() {
            /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass34 */

            @Override // com.huawei.gson.TypeAdapterFactory
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
                Class<? super T> rawType = typeToken.getRawType();
                if (rawType == base || rawType == sub) {
                    return typeAdapter;
                }
                return null;
            }

            public String toString() {
                return "Factory[type=" + base.getName() + "+" + sub.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }

    public static <T1> TypeAdapterFactory newTypeHierarchyFactory(final Class<T1> clazz, final TypeAdapter<T1> typeAdapter) {
        return new TypeAdapterFactory() {
            /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass35 */

            @Override // com.huawei.gson.TypeAdapterFactory
            public <T2> TypeAdapter<T2> create(Gson gson, TypeToken<T2> typeToken) {
                final Class<? super T2> requestedType = typeToken.getRawType();
                if (!clazz.isAssignableFrom(requestedType)) {
                    return null;
                }
                return new TypeAdapter<T1>() {
                    /* class com.huawei.gson.internal.bind.TypeAdapters.AnonymousClass35.AnonymousClass1 */

                    @Override // com.huawei.gson.TypeAdapter
                    public void write(JsonWriter out, T1 value) throws IOException {
                        typeAdapter.write(out, value);
                    }

                    @Override // com.huawei.gson.TypeAdapter
                    public T1 read(JsonReader in) throws IOException {
                        T1 result = (T1) typeAdapter.read(in);
                        if (result == null || requestedType.isInstance(result)) {
                            return result;
                        }
                        throw new JsonSyntaxException("Expected a " + requestedType.getName() + " but was " + result.getClass().getName());
                    }
                };
            }

            public String toString() {
                return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
            }
        };
    }
}
