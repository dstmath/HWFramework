package ohos.utils.fastjson.parser;

import java.io.Closeable;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.PropertyNamingStrategy;
import ohos.utils.fastjson.annotation.JSONType;
import ohos.utils.fastjson.parser.deserializer.FieldDeserializer;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.serializer.ArrayCodec;
import ohos.utils.fastjson.serializer.BigDecimalCodec;
import ohos.utils.fastjson.serializer.BooleanCodec;
import ohos.utils.fastjson.serializer.CollectionCodec;
import ohos.utils.fastjson.serializer.DateCodec;
import ohos.utils.fastjson.serializer.IntegerCodec;
import ohos.utils.fastjson.serializer.MiscCodec;
import ohos.utils.fastjson.serializer.NumberCodec;
import ohos.utils.fastjson.serializer.StringCodec;
import ohos.utils.fastjson.util.FieldInfo;
import ohos.utils.fastjson.util.IdentityHashMap;
import ohos.utils.fastjson.util.TypeUtils;

public class ParserConfig {
    private static long[] denyList = {-7600952144447537354L, -4082057040235125754L, -2364987994247679115L, -676156662527871184L, -254670111376247151L, 1502845958873959152L, 4147696707147271408L, 5347909877633654828L, 5751393439502795295L, 7702607466162283393L};
    public static ParserConfig global = new ParserConfig();
    public boolean autoTypeSupport;
    public ClassLoader defaultClassLoader;
    private final IdentityHashMap<ObjectDeserializer> deserializers = new IdentityHashMap<>(1024);
    public PropertyNamingStrategy propertyNamingStrategy;
    public final SymbolTable symbolTable = new SymbolTable(16384);

    public static ParserConfig getGlobalInstance() {
        return global;
    }

    public ParserConfig() {
        this.deserializers.put(SimpleDateFormat.class, MiscCodec.instance);
        this.deserializers.put(Date.class, DateCodec.instance);
        this.deserializers.put(Calendar.class, DateCodec.instance);
        this.deserializers.put(Map.class, MapDeserializer.instance);
        this.deserializers.put(HashMap.class, MapDeserializer.instance);
        this.deserializers.put(LinkedHashMap.class, MapDeserializer.instance);
        this.deserializers.put(TreeMap.class, MapDeserializer.instance);
        this.deserializers.put(ConcurrentMap.class, MapDeserializer.instance);
        this.deserializers.put(ConcurrentHashMap.class, MapDeserializer.instance);
        this.deserializers.put(Collection.class, CollectionCodec.instance);
        this.deserializers.put(List.class, CollectionCodec.instance);
        this.deserializers.put(ArrayList.class, CollectionCodec.instance);
        this.deserializers.put(Object.class, JavaObjectDeserializer.instance);
        this.deserializers.put(String.class, StringCodec.instance);
        this.deserializers.put(Character.TYPE, MiscCodec.instance);
        this.deserializers.put(Character.class, MiscCodec.instance);
        this.deserializers.put(Byte.TYPE, NumberCodec.instance);
        this.deserializers.put(Byte.class, NumberCodec.instance);
        this.deserializers.put(Short.TYPE, NumberCodec.instance);
        this.deserializers.put(Short.class, NumberCodec.instance);
        this.deserializers.put(Integer.TYPE, IntegerCodec.instance);
        this.deserializers.put(Integer.class, IntegerCodec.instance);
        this.deserializers.put(Long.TYPE, IntegerCodec.instance);
        this.deserializers.put(Long.class, IntegerCodec.instance);
        this.deserializers.put(BigInteger.class, BigDecimalCodec.instance);
        this.deserializers.put(BigDecimal.class, BigDecimalCodec.instance);
        this.deserializers.put(Float.TYPE, NumberCodec.instance);
        this.deserializers.put(Float.class, NumberCodec.instance);
        this.deserializers.put(Double.TYPE, NumberCodec.instance);
        this.deserializers.put(Double.class, NumberCodec.instance);
        this.deserializers.put(Boolean.TYPE, BooleanCodec.instance);
        this.deserializers.put(Boolean.class, BooleanCodec.instance);
        this.deserializers.put(Class.class, MiscCodec.instance);
        this.deserializers.put(char[].class, ArrayCodec.instance);
        this.deserializers.put(Object[].class, ArrayCodec.instance);
        this.deserializers.put(UUID.class, MiscCodec.instance);
        this.deserializers.put(TimeZone.class, MiscCodec.instance);
        this.deserializers.put(Locale.class, MiscCodec.instance);
        this.deserializers.put(Currency.class, MiscCodec.instance);
        this.deserializers.put(URI.class, MiscCodec.instance);
        this.deserializers.put(URL.class, MiscCodec.instance);
        this.deserializers.put(Pattern.class, MiscCodec.instance);
        this.deserializers.put(Charset.class, MiscCodec.instance);
        this.deserializers.put(Number.class, NumberCodec.instance);
        this.deserializers.put(StackTraceElement.class, MiscCodec.instance);
        this.deserializers.put(Serializable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(Cloneable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(Comparable.class, JavaObjectDeserializer.instance);
        this.deserializers.put(Closeable.class, JavaObjectDeserializer.instance);
    }

    public ObjectDeserializer getDeserializer(Type type) {
        ObjectDeserializer objectDeserializer = this.deserializers.get(type);
        if (objectDeserializer != null) {
            return objectDeserializer;
        }
        if (type instanceof Class) {
            return getDeserializer((Class) type, type);
        }
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return getDeserializer((Class) rawType, type);
            }
            return getDeserializer(rawType);
        }
        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getDeserializer(upperBounds[0]);
            }
        }
        return JavaObjectDeserializer.instance;
    }

    public ObjectDeserializer getDeserializer(Class<?> cls, Type type) {
        ObjectDeserializer objectDeserializer;
        JSONType jSONType;
        Class<?> mappingTo;
        ObjectDeserializer objectDeserializer2 = this.deserializers.get(type);
        if (objectDeserializer2 != null) {
            return objectDeserializer2;
        }
        if (type == null) {
            type = cls;
        }
        ObjectDeserializer objectDeserializer3 = this.deserializers.get(type);
        if (objectDeserializer3 != null) {
            return objectDeserializer3;
        }
        if (!isPrimitive(cls) && (jSONType = (JSONType) cls.getAnnotation(JSONType.class)) != null && (mappingTo = jSONType.mappingTo()) != Void.class) {
            return getDeserializer(mappingTo, mappingTo);
        }
        if ((type instanceof WildcardType) || (type instanceof TypeVariable) || (type instanceof ParameterizedType)) {
            objectDeserializer3 = this.deserializers.get(cls);
        }
        if (objectDeserializer3 != null) {
            return objectDeserializer3;
        }
        ObjectDeserializer objectDeserializer4 = this.deserializers.get(type);
        if (objectDeserializer4 != null) {
            return objectDeserializer4;
        }
        if (cls.isEnum()) {
            objectDeserializer = new EnumDeserializer(cls);
        } else if (cls.isArray()) {
            objectDeserializer = ArrayCodec.instance;
        } else if (cls == Set.class || cls == HashSet.class || cls == Collection.class || cls == List.class || cls == ArrayList.class) {
            objectDeserializer = CollectionCodec.instance;
        } else if (Collection.class.isAssignableFrom(cls)) {
            objectDeserializer = CollectionCodec.instance;
        } else if (Map.class.isAssignableFrom(cls)) {
            objectDeserializer = MapDeserializer.instance;
        } else if (Throwable.class.isAssignableFrom(cls)) {
            objectDeserializer = new ThrowableDeserializer(this, cls);
        } else {
            objectDeserializer = cls.getName().equals("android.net.Uri") ? MiscCodec.instance : new JavaBeanDeserializer(this, cls, type);
        }
        putDeserializer(type, objectDeserializer);
        return objectDeserializer;
    }

    public ObjectDeserializer registerIfNotExists(Class<?> cls) {
        return registerIfNotExists(cls, cls.getModifiers(), false, true, true, true);
    }

    public ObjectDeserializer registerIfNotExists(Class<?> cls, int i, boolean z, boolean z2, boolean z3, boolean z4) {
        ObjectDeserializer objectDeserializer = this.deserializers.get(cls);
        if (objectDeserializer != null) {
            return objectDeserializer;
        }
        JavaBeanDeserializer javaBeanDeserializer = new JavaBeanDeserializer(this, cls, cls, JavaBeanInfo.build(cls, i, cls, z, z2, z3, z4, this.propertyNamingStrategy));
        putDeserializer(cls, javaBeanDeserializer);
        return javaBeanDeserializer;
    }

    public boolean containsKey(Class cls) {
        return this.deserializers.get(cls) != null;
    }

    public FieldDeserializer createFieldDeserializer(ParserConfig parserConfig, Class<?> cls, FieldInfo fieldInfo) {
        Class<?> cls2 = fieldInfo.fieldClass;
        if (cls2 == List.class || cls2 == ArrayList.class || (cls2.isArray() && !cls2.getComponentType().isPrimitive())) {
            return new ListTypeFieldDeserializer(parserConfig, cls, fieldInfo);
        }
        return new DefaultFieldDeserializer(parserConfig, cls, fieldInfo);
    }

    public void putDeserializer(Type type, ObjectDeserializer objectDeserializer) {
        this.deserializers.put(type, objectDeserializer);
    }

    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == Boolean.class || cls == Character.class || cls == Byte.class || cls == Short.class || cls == Integer.class || cls == Long.class || cls == Float.class || cls == Double.class || cls == BigInteger.class || cls == BigDecimal.class || cls == String.class || cls == Date.class || cls == java.sql.Date.class || cls == Time.class || cls == Timestamp.class;
    }

    public Class<?> checkAutoType(String str, Class<?> cls, int i) {
        if (str == null) {
            return null;
        }
        if (str.length() < 128) {
            if (str.length() >= 3) {
                long charAt = (((long) str.charAt(0)) ^ -3750763034362895579L) * 1099511628211L;
                if (charAt == -5808493101479473382L) {
                    throw new JSONException("autoType is not support. " + str);
                } else if ((charAt ^ ((long) str.charAt(str.length() - 1))) * 1099511628211L != 655701488918567152L) {
                    long charAt2 = (((((((long) str.charAt(0)) ^ -3750763034362895579L) * 1099511628211L) ^ ((long) str.charAt(1))) * 1099511628211L) ^ ((long) str.charAt(2))) * 1099511628211L;
                    for (int i2 = 3; i2 < str.length(); i2++) {
                        charAt2 = (charAt2 ^ ((long) str.charAt(i2))) * 1099511628211L;
                        if (Arrays.binarySearch(denyList, charAt2) >= 0 && TypeUtils.getClassFromMapping(str) == null) {
                            throw new JSONException("autoType is not support. " + str);
                        }
                    }
                    Class<?> classFromMapping = TypeUtils.getClassFromMapping(str);
                    if (classFromMapping != null) {
                        return classFromMapping;
                    }
                    Class<?> findClass = this.deserializers.findClass(str);
                    if (findClass != null) {
                        return findClass;
                    }
                    Class<?> loadClass = TypeUtils.loadClass(str, this.defaultClassLoader, false);
                    if (loadClass == null || cls == null || loadClass == HashMap.class) {
                        if (loadClass.isAnnotationPresent(JSONType.class)) {
                            TypeUtils.addMapping(str, loadClass);
                            return loadClass;
                        }
                        int i3 = Feature.SupportAutoType.mask;
                        if ((i & i3) == 0 && (i3 & JSON.DEFAULT_PARSER_FEATURE) == 0 && !this.autoTypeSupport) {
                            throw new JSONException("autoType is not support : " + str);
                        }
                        TypeUtils.addMapping(str, loadClass);
                        return loadClass;
                    } else if (cls.isAssignableFrom(loadClass)) {
                        TypeUtils.addMapping(str, loadClass);
                        return loadClass;
                    } else {
                        throw new JSONException("type not match. " + str + " -> " + cls.getName());
                    }
                } else {
                    throw new JSONException("autoType is not support. " + str);
                }
            }
        }
        throw new JSONException("autoType is not support. " + str);
    }
}
