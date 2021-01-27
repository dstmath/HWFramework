package ohos.utils.fastjson.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessControlException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ohos.global.icu.text.DateFormat;
import ohos.light.bean.LightEffect;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.fastjson.PropertyNamingStrategy;
import ohos.utils.fastjson.annotation.JSONField;
import ohos.utils.fastjson.annotation.JSONType;
import ohos.utils.fastjson.parser.JSONLexer;
import ohos.utils.fastjson.parser.JavaBeanDeserializer;
import ohos.utils.fastjson.parser.ParserConfig;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.serializer.SerializerFeature;

public class TypeUtils {
    public static boolean compatibleWithJavaBean = false;
    private static volatile Map<Class, String[]> kotlinIgnores = null;
    private static volatile boolean kotlinIgnores_error = false;
    private static volatile boolean kotlin_class_klass_error = false;
    private static volatile boolean kotlin_error = false;
    private static volatile Constructor kotlin_kclass_constructor = null;
    private static volatile Method kotlin_kclass_getConstructors = null;
    private static volatile Method kotlin_kfunction_getParameters = null;
    private static volatile Method kotlin_kparameter_getName = null;
    private static volatile Class kotlin_metadata = null;
    private static volatile boolean kotlin_metadata_error = false;
    private static final ConcurrentMap<String, Class<?>> mappings = new ConcurrentHashMap(36, 0.75f, 1);
    private static boolean setAccessibleEnable = true;

    public static boolean isKotlin(Class cls) {
        if (kotlin_metadata == null && !kotlin_metadata_error) {
            try {
                kotlin_metadata = Class.forName("kotlin.Metadata");
            } catch (Throwable unused) {
                kotlin_metadata_error = true;
            }
        }
        if (kotlin_metadata == null) {
            return false;
        }
        return cls.isAnnotationPresent(kotlin_metadata);
    }

    private static boolean isKotlinIgnore(Class cls, String str) {
        String[] strArr;
        if (kotlinIgnores == null && !kotlinIgnores_error) {
            try {
                HashMap hashMap = new HashMap();
                hashMap.put(Class.forName("kotlin.ranges.CharRange"), new String[]{"getEndInclusive", "isEmpty"});
                hashMap.put(Class.forName("kotlin.ranges.IntRange"), new String[]{"getEndInclusive", "isEmpty"});
                hashMap.put(Class.forName("kotlin.ranges.LongRange"), new String[]{"getEndInclusive", "isEmpty"});
                hashMap.put(Class.forName("kotlin.ranges.ClosedFloatRange"), new String[]{"getEndInclusive", "isEmpty"});
                hashMap.put(Class.forName("kotlin.ranges.ClosedDoubleRange"), new String[]{"getEndInclusive", "isEmpty"});
                kotlinIgnores = hashMap;
            } catch (Throwable unused) {
                kotlinIgnores_error = true;
            }
        }
        if (kotlinIgnores == null || (strArr = kotlinIgnores.get(cls)) == null || Arrays.binarySearch(strArr, str) < 0) {
            return false;
        }
        return true;
    }

    public static String[] getKoltinConstructorParameters(Class cls) {
        if (kotlin_kclass_constructor == null && !kotlin_class_klass_error) {
            try {
                Class<?> cls2 = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                kotlin_kclass_constructor = cls2.getConstructor(Class.class);
                kotlin_kclass_getConstructors = cls2.getMethod("getConstructors", new Class[0]);
                kotlin_kfunction_getParameters = Class.forName("kotlin.reflect.KFunction").getMethod("getParameters", new Class[0]);
                kotlin_kparameter_getName = Class.forName("kotlin.reflect.KParameter").getMethod("getName", new Class[0]);
            } catch (Throwable unused) {
                kotlin_class_klass_error = true;
            }
        }
        if (kotlin_kclass_constructor == null || kotlin_error) {
            return null;
        }
        try {
            Iterator it = ((Iterable) kotlin_kclass_getConstructors.invoke(kotlin_kclass_constructor.newInstance(cls), new Object[0])).iterator();
            Object obj = null;
            while (it.hasNext()) {
                Object next = it.next();
                List list = (List) kotlin_kfunction_getParameters.invoke(next, new Object[0]);
                if (obj == null || list.size() != 0) {
                    obj = next;
                }
                it.hasNext();
            }
            List list2 = (List) kotlin_kfunction_getParameters.invoke(obj, new Object[0]);
            String[] strArr = new String[list2.size()];
            for (int i = 0; i < list2.size(); i++) {
                strArr[i] = (String) kotlin_kparameter_getName.invoke(list2.get(i), new Object[0]);
            }
            return strArr;
        } catch (Throwable unused2) {
            kotlin_error = true;
            return null;
        }
    }

    public static final String castToString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public static final Byte castToByte(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return Byte.valueOf(((Number) obj).byteValue());
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
            return Byte.valueOf(Byte.parseByte(str));
        }
        throw new JSONException("can not cast to byte, value : " + obj);
    }

    public static final Character castToChar(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Character) {
            return (Character) obj;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0) {
                return null;
            }
            if (str.length() == 1) {
                return Character.valueOf(str.charAt(0));
            }
            throw new JSONException("can not cast to byte, value : " + obj);
        }
        throw new JSONException("can not cast to byte, value : " + obj);
    }

    public static final Short castToShort(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return Short.valueOf(((Number) obj).shortValue());
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
            return Short.valueOf(Short.parseShort(str));
        }
        throw new JSONException("can not cast to short, value : " + obj);
    }

    public static final BigDecimal castToBigDecimal(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof BigInteger) {
            return new BigDecimal((BigInteger) obj);
        }
        String obj2 = obj.toString();
        if (obj2.length() == 0 || "null".equals(obj2)) {
            return null;
        }
        return new BigDecimal(obj2);
    }

    public static final BigInteger castToBigInteger(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigInteger) {
            return (BigInteger) obj;
        }
        if ((obj instanceof Float) || (obj instanceof Double)) {
            return BigInteger.valueOf(((Number) obj).longValue());
        }
        String obj2 = obj.toString();
        if (obj2.length() == 0 || "null".equals(obj2)) {
            return null;
        }
        return new BigInteger(obj2);
    }

    public static final Float castToFloat(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return Float.valueOf(((Number) obj).floatValue());
        }
        if (obj instanceof String) {
            String obj2 = obj.toString();
            if (obj2.length() == 0 || "null".equals(obj2)) {
                return null;
            }
            return Float.valueOf(Float.parseFloat(obj2));
        }
        throw new JSONException("can not cast to float, value : " + obj);
    }

    public static final Double castToDouble(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return Double.valueOf(((Number) obj).doubleValue());
        }
        if (obj instanceof String) {
            String obj2 = obj.toString();
            if (obj2.length() == 0 || "null".equals(obj2) || "NULL".equals(obj2)) {
                return null;
            }
            return Double.valueOf(Double.parseDouble(obj2));
        }
        throw new JSONException("can not cast to double, value : " + obj);
    }

    public static final Date castToDate(Object obj) {
        String str;
        long j;
        if (obj == null) {
            return null;
        }
        if (obj instanceof Calendar) {
            return ((Calendar) obj).getTime();
        }
        if (obj instanceof Date) {
            return (Date) obj;
        }
        long j2 = -1;
        if (obj instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) obj;
            int scale = bigDecimal.scale();
            if (scale < -100 || scale > 100) {
                j = bigDecimal.longValueExact();
            } else {
                j = bigDecimal.longValue();
            }
            j2 = j;
        } else if (obj instanceof Number) {
            j2 = ((Number) obj).longValue();
        } else if (obj instanceof String) {
            String str2 = (String) obj;
            if (str2.indexOf(45) != -1) {
                if (str2.length() == JSON.DEFFAULT_DATE_FORMAT.length()) {
                    str = JSON.DEFFAULT_DATE_FORMAT;
                } else if (str2.length() == 10) {
                    str = "yyyy-MM-dd";
                } else if (str2.length() == 19) {
                    str = "yyyy-MM-dd HH:mm:ss";
                } else {
                    str = (str2.length() == 29 && str2.charAt(26) == ':' && str2.charAt(28) == '0') ? "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" : "yyyy-MM-dd HH:mm:ss.SSS";
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(str, JSON.defaultLocale);
                simpleDateFormat.setTimeZone(JSON.defaultTimeZone);
                try {
                    return simpleDateFormat.parse(str2);
                } catch (ParseException unused) {
                    throw new JSONException("can not cast to Date, value : " + str2);
                }
            } else if (str2.length() == 0 || "null".equals(str2)) {
                return null;
            } else {
                j2 = Long.parseLong(str2);
            }
        }
        if (j2 >= 0) {
            return new Date(j2);
        }
        throw new JSONException("can not cast to Date, value : " + obj);
    }

    public static final Long castToLong(Object obj) {
        Calendar calendar = null;
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) obj;
            int scale = bigDecimal.scale();
            if (scale < -100 || scale > 100) {
                return Long.valueOf(bigDecimal.longValueExact());
            }
            return Long.valueOf(bigDecimal.longValue());
        } else if (obj instanceof Number) {
            return Long.valueOf(((Number) obj).longValue());
        } else {
            if (obj instanceof String) {
                String str = (String) obj;
                if (str.length() == 0 || "null".equals(str)) {
                    return null;
                }
                try {
                    return Long.valueOf(Long.parseLong(str));
                } catch (NumberFormatException unused) {
                    JSONLexer jSONLexer = new JSONLexer(str);
                    if (jSONLexer.scanISO8601DateIfMatch(false)) {
                        calendar = jSONLexer.calendar;
                    }
                    jSONLexer.close();
                    if (calendar != null) {
                        return Long.valueOf(calendar.getTimeInMillis());
                    }
                }
            }
            throw new JSONException("can not cast to long, value : " + obj);
        }
    }

    public static final Integer castToInt(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) obj;
            int scale = bigDecimal.scale();
            if (scale < -100 || scale > 100) {
                return Integer.valueOf(bigDecimal.intValueExact());
            }
            return Integer.valueOf(bigDecimal.intValue());
        } else if (obj instanceof Number) {
            return Integer.valueOf(((Number) obj).intValue());
        } else {
            if (obj instanceof String) {
                String str = (String) obj;
                if (str.length() == 0 || "null".equals(str)) {
                    return null;
                }
                return Integer.valueOf(Integer.parseInt(str));
            }
            throw new JSONException("can not cast to int, value : " + obj);
        }
    }

    public static final byte[] castToBytes(Object obj) {
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            return JSONLexer.decodeFast(str, 0, str.length());
        }
        throw new JSONException("can not cast to int, value : " + obj);
    }

    public static final Boolean castToBoolean(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        boolean z = false;
        if (obj instanceof BigDecimal) {
            if (((BigDecimal) obj).intValueExact() == 1) {
                z = true;
            }
            return Boolean.valueOf(z);
        } else if (obj instanceof Number) {
            if (((Number) obj).intValue() == 1) {
                z = true;
            }
            return Boolean.valueOf(z);
        } else {
            if (obj instanceof String) {
                String str = (String) obj;
                if (str.length() == 0 || "null".equals(str)) {
                    return null;
                }
                if ("true".equalsIgnoreCase(str) || "1".equals(str)) {
                    return Boolean.TRUE;
                }
                if ("false".equalsIgnoreCase(str) || LightEffect.LIGHT_ID_LED.equals(str)) {
                    return Boolean.FALSE;
                }
            }
            throw new JSONException("can not cast to int, value : " + obj);
        }
    }

    public static final <T> T castToJavaBean(Object obj, Class<T> cls) {
        return (T) cast(obj, (Class<Object>) cls, ParserConfig.global);
    }

    public static final <T> T cast(Object obj, Class<T> cls, ParserConfig parserConfig) {
        return (T) cast(obj, cls, parserConfig, 0);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    public static final <T> T cast(Object obj, Class<T> cls, ParserConfig parserConfig, int i) {
        T t;
        if (obj == 0) {
            return null;
        }
        if (cls == null) {
            throw new IllegalArgumentException("clazz is null");
        } else if (cls == obj.getClass()) {
            return obj;
        } else {
            if (!(obj instanceof Map)) {
                int i2 = 0;
                if (cls.isArray()) {
                    if (obj instanceof Collection) {
                        Collection<Object> collection = (Collection) obj;
                        T t2 = (T) Array.newInstance(cls.getComponentType(), collection.size());
                        for (Object obj2 : collection) {
                            Array.set(t2, i2, cast(obj2, (Class<Object>) cls.getComponentType(), parserConfig));
                            i2++;
                        }
                        return t2;
                    } else if (cls == byte[].class) {
                        return (T) castToBytes(obj);
                    }
                }
                if (cls.isAssignableFrom(obj.getClass())) {
                    return obj;
                }
                if (cls == Boolean.TYPE || cls == Boolean.class) {
                    return (T) castToBoolean(obj);
                }
                if (cls == Byte.TYPE || cls == Byte.class) {
                    return (T) castToByte(obj);
                }
                if ((cls == Character.TYPE || cls == Character.class) && (obj instanceof String)) {
                    String str = (String) obj;
                    if (str.length() == 1) {
                        return (T) Character.valueOf(str.charAt(0));
                    }
                }
                if (cls == Short.TYPE || cls == Short.class) {
                    return (T) castToShort(obj);
                }
                if (cls == Integer.TYPE || cls == Integer.class) {
                    return (T) castToInt(obj);
                }
                if (cls == Long.TYPE || cls == Long.class) {
                    return (T) castToLong(obj);
                }
                if (cls == Float.TYPE || cls == Float.class) {
                    return (T) castToFloat(obj);
                }
                if (cls == Double.TYPE || cls == Double.class) {
                    return (T) castToDouble(obj);
                }
                if (cls == String.class) {
                    return (T) castToString(obj);
                }
                if (cls == BigDecimal.class) {
                    return (T) castToBigDecimal(obj);
                }
                if (cls == BigInteger.class) {
                    return (T) castToBigInteger(obj);
                }
                if (cls == Date.class) {
                    return (T) castToDate(obj);
                }
                if (cls.isEnum()) {
                    return (T) castToEnum(obj, cls, parserConfig);
                }
                if (Calendar.class.isAssignableFrom(cls)) {
                    Date castToDate = castToDate(obj);
                    if (cls == Calendar.class) {
                        t = (T) Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
                    } else {
                        try {
                            t = cls.newInstance();
                        } catch (Exception e) {
                            throw new JSONException("can not cast to : " + cls.getName(), e);
                        }
                    }
                    t.setTime(castToDate);
                    return t;
                }
                if (obj instanceof String) {
                    String str2 = (String) obj;
                    if (str2.length() == 0 || "null".equals(str2)) {
                        return null;
                    }
                    if (cls == Currency.class) {
                        return (T) Currency.getInstance(str2);
                    }
                }
                throw new JSONException("can not cast to : " + cls.getName());
            } else if (cls == Map.class) {
                return obj;
            } else {
                Map map = (Map) obj;
                return (cls != Object.class || map.containsKey(JSON.DEFAULT_TYPE_KEY)) ? (T) castToJavaBean(map, cls, parserConfig, i) : obj;
            }
        }
    }

    public static final <T> T castToEnum(Object obj, Class<T> cls, ParserConfig parserConfig) {
        try {
            if (obj instanceof String) {
                String str = (String) obj;
                if (str.length() == 0) {
                    return null;
                }
                return (T) Enum.valueOf(cls, str);
            }
            if ((obj instanceof Integer) || (obj instanceof Long)) {
                int intValue = ((Number) obj).intValue();
                T[] enumConstants = cls.getEnumConstants();
                if (intValue < enumConstants.length) {
                    return enumConstants[intValue];
                }
            }
            throw new JSONException("can not cast to : " + cls.getName());
        } catch (Exception e) {
            throw new JSONException("can not cast to : " + cls.getName(), e);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    public static final <T> T cast(Object obj, Type type, ParserConfig parserConfig) {
        if (obj == 0) {
            return null;
        }
        if (type instanceof Class) {
            return (T) cast(obj, (Class) type, parserConfig, 0);
        }
        if (type instanceof ParameterizedType) {
            return (T) cast(obj, (ParameterizedType) type, parserConfig);
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
        }
        if (type instanceof TypeVariable) {
            return obj;
        }
        throw new JSONException("can not cast to : " + type);
    }

    public static final <T> T cast(Object obj, ParameterizedType parameterizedType, ParserConfig parserConfig) {
        Object obj2;
        Object obj3;
        Type rawType = parameterizedType.getRawType();
        if (rawType == List.class || rawType == ArrayList.class) {
            Type type = parameterizedType.getActualTypeArguments()[0];
            if (obj instanceof List) {
                List list = (List) obj;
                int size = list.size();
                T t = (T) new ArrayList(size);
                for (int i = 0; i < size; i++) {
                    Object obj4 = list.get(i);
                    if (!(type instanceof Class)) {
                        obj3 = cast(obj4, type, parserConfig);
                    } else if (obj4 == null || obj4.getClass() != JSONObject.class) {
                        obj3 = cast(obj4, (Class) type, parserConfig, 0);
                    } else {
                        obj3 = ((JSONObject) obj4).toJavaObject((Class) type, parserConfig, 0);
                    }
                    t.add(obj3);
                }
                return t;
            }
        }
        if (rawType == Set.class || rawType == HashSet.class || rawType == TreeSet.class || rawType == List.class || rawType == ArrayList.class) {
            Type type2 = parameterizedType.getActualTypeArguments()[0];
            if (obj instanceof Iterable) {
                T t2 = (rawType == Set.class || rawType == HashSet.class) ? (T) new HashSet() : rawType == TreeSet.class ? (T) new TreeSet() : (T) new ArrayList();
                for (T t3 : (Iterable) obj) {
                    if (!(type2 instanceof Class)) {
                        obj2 = cast(t3, type2, parserConfig);
                    } else if (t3 == null || t3.getClass() != JSONObject.class) {
                        obj2 = cast(t3, (Class) type2, parserConfig, 0);
                    } else {
                        obj2 = t3.toJavaObject((Class) type2, parserConfig, 0);
                    }
                    t2.add(obj2);
                }
                return t2;
            }
        }
        if (rawType == Map.class || rawType == HashMap.class) {
            Type type3 = parameterizedType.getActualTypeArguments()[0];
            Type type4 = parameterizedType.getActualTypeArguments()[1];
            if (obj instanceof Map) {
                T t4 = (T) new HashMap();
                for (Map.Entry entry : ((Map) obj).entrySet()) {
                    t4.put(cast(entry.getKey(), type3, parserConfig), cast(entry.getValue(), type4, parserConfig));
                }
                return t4;
            }
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.length() == 0 || "null".equals(str)) {
                return null;
            }
        }
        if (parameterizedType.getActualTypeArguments().length == 1 && (parameterizedType.getActualTypeArguments()[0] instanceof WildcardType)) {
            return (T) cast(obj, rawType, parserConfig);
        }
        throw new JSONException("can not cast to : " + parameterizedType);
    }

    public static final <T> T castToJavaBean(Map<String, Object> map, Class<T> cls, ParserConfig parserConfig) {
        return (T) castToJavaBean(map, cls, parserConfig, 0);
    }

    public static final <T> T castToJavaBean(Map<String, Object> map, Class<T> cls, ParserConfig parserConfig, int i) {
        JSONObject jSONObject;
        int i2 = 0;
        if (cls == StackTraceElement.class) {
            try {
                String str = (String) map.get("className");
                String str2 = (String) map.get("methodName");
                String str3 = (String) map.get("fileName");
                Number number = (Number) map.get("lineNumber");
                if (number != null) {
                    if (number instanceof BigDecimal) {
                        i2 = ((BigDecimal) number).intValueExact();
                    } else {
                        i2 = number.intValue();
                    }
                }
                return (T) new StackTraceElement(str, str2, str3, i2);
            } catch (Exception e) {
                throw new JSONException(e.getMessage(), e);
            }
        } else {
            Object obj = map.get(JSON.DEFAULT_TYPE_KEY);
            JavaBeanDeserializer javaBeanDeserializer = null;
            if (obj instanceof String) {
                String str4 = (String) obj;
                if (parserConfig == null) {
                    parserConfig = ParserConfig.global;
                }
                Class<?> checkAutoType = parserConfig.checkAutoType(str4, null, i);
                if (checkAutoType == null) {
                    throw new ClassNotFoundException(str4 + " not found");
                } else if (!checkAutoType.equals(cls)) {
                    return (T) castToJavaBean(map, checkAutoType, parserConfig, i);
                }
            }
            if (cls.isInterface()) {
                if (map instanceof JSONObject) {
                    jSONObject = (JSONObject) map;
                } else {
                    jSONObject = new JSONObject(map);
                }
                if (parserConfig == null) {
                    parserConfig = ParserConfig.getGlobalInstance();
                }
                return parserConfig.getDeserializer(cls) != null ? (T) JSON.parseObject(JSON.toJSONString(jSONObject), cls) : (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{cls}, jSONObject);
            } else if (cls == String.class && (map instanceof JSONObject)) {
                return (T) map.toString();
            } else {
                if (parserConfig == null) {
                    parserConfig = ParserConfig.global;
                }
                ObjectDeserializer deserializer = parserConfig.getDeserializer(cls);
                if (deserializer instanceof JavaBeanDeserializer) {
                    javaBeanDeserializer = (JavaBeanDeserializer) deserializer;
                }
                if (javaBeanDeserializer != null) {
                    return (T) javaBeanDeserializer.createInstance(map, parserConfig);
                }
                throw new JSONException("can not get javaBeanDeserializer");
            }
        }
    }

    static {
        mappings.put("byte", Byte.TYPE);
        mappings.put("short", Short.TYPE);
        mappings.put("int", Integer.TYPE);
        mappings.put("long", Long.TYPE);
        mappings.put("float", Float.TYPE);
        mappings.put("double", Double.TYPE);
        mappings.put("boolean", Boolean.TYPE);
        mappings.put("char", Character.TYPE);
        mappings.put("[byte", byte[].class);
        mappings.put("[short", short[].class);
        mappings.put("[int", int[].class);
        mappings.put("[long", long[].class);
        mappings.put("[float", float[].class);
        mappings.put("[double", double[].class);
        mappings.put("[boolean", boolean[].class);
        mappings.put("[char", char[].class);
        mappings.put("[B", byte[].class);
        mappings.put("[S", short[].class);
        mappings.put("[I", int[].class);
        mappings.put("[J", long[].class);
        mappings.put("[F", float[].class);
        mappings.put("[D", double[].class);
        mappings.put("[C", char[].class);
        mappings.put("[Z", boolean[].class);
        mappings.put("java.util.HashMap", HashMap.class);
        mappings.put("java.util.TreeMap", TreeMap.class);
        mappings.put("java.util.Date", Date.class);
        mappings.put("ohos.utils.fastjson.JSONObject", JSONObject.class);
        mappings.put("java.util.concurrent.ConcurrentHashMap", ConcurrentHashMap.class);
        mappings.put("java.text.SimpleDateFormat", SimpleDateFormat.class);
        mappings.put("java.lang.StackTraceElement", StackTraceElement.class);
        mappings.put("java.lang.RuntimeException", RuntimeException.class);
    }

    public static Class<?> getClassFromMapping(String str) {
        return mappings.get(str);
    }

    public static Class<?> loadClass(String str, ClassLoader classLoader) {
        return loadClass(str, classLoader, false);
    }

    public static Class<?> loadClass(String str, ClassLoader classLoader, boolean z) {
        if (str == null || str.length() == 0) {
            return null;
        }
        if (str.length() < 256) {
            Class<?> cls = mappings.get(str);
            if (cls != null) {
                return cls;
            }
            if (str.charAt(0) == '[') {
                Class<?> loadClass = loadClass(str.substring(1), classLoader, false);
                if (loadClass == null) {
                    return null;
                }
                return Array.newInstance(loadClass, 0).getClass();
            } else if (str.startsWith("L") && str.endsWith(";")) {
                return loadClass(str.substring(1, str.length() - 1), classLoader, false);
            } else {
                if (classLoader != null) {
                    try {
                        Class<?> loadClass2 = classLoader.loadClass(str);
                        if (z) {
                            mappings.put(str, loadClass2);
                        }
                        return loadClass2;
                    } catch (Exception unused) {
                    }
                }
                try {
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    if (!(contextClassLoader == null || contextClassLoader == classLoader)) {
                        Class<?> loadClass3 = contextClassLoader.loadClass(str);
                        if (z) {
                            try {
                                mappings.put(str, loadClass3);
                            } catch (Exception unused2) {
                                cls = loadClass3;
                            }
                        }
                        return loadClass3;
                    }
                } catch (Exception unused3) {
                }
                try {
                    cls = Class.forName(str);
                    mappings.put(str, cls);
                    return cls;
                } catch (Exception unused4) {
                    return cls;
                }
            }
        } else {
            throw new JSONException("className too long. " + str);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v54, types: [java.lang.annotation.Annotation[][]] */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x0416, code lost:
        if (r0 == null) goto L_0x037a;
     */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x0435  */
    /* JADX WARNING: Removed duplicated region for block: B:269:0x05af  */
    /* JADX WARNING: Removed duplicated region for block: B:272:0x05c2  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x0170 A[EDGE_INSN: B:299:0x0170->B:81:0x0170 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0134  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0152  */
    /* JADX WARNING: Unknown variable types count: 1 */
    public static List<FieldInfo> computeGetters(Class<?> cls, int i, boolean z, JSONType jSONType, Map<String, String> map, boolean z2, boolean z3, boolean z4, PropertyNamingStrategy propertyNamingStrategy) {
        boolean z5;
        String[] strArr;
        int i2;
        int i3;
        Iterator it;
        LinkedHashMap linkedHashMap;
        short[] sArr;
        String[] strArr2;
        JSONField[][] jSONFieldArr;
        Constructor<?>[] constructorArr;
        JSONField jSONField;
        HashMap hashMap;
        Field[] fieldArr;
        HashMap hashMap2;
        Field[] fieldArr2;
        LinkedHashMap linkedHashMap2;
        int i4;
        boolean z6;
        boolean z7;
        int i5;
        Method method;
        String str;
        int i6;
        HashMap hashMap3;
        LinkedHashMap linkedHashMap3;
        String str2;
        JSONField jSONField2;
        Field[] fieldArr3;
        int i7;
        int i8;
        String str3;
        JSONField jSONField3;
        int i9;
        Field[] fieldArr4;
        String str4;
        boolean z8;
        JSONField[][] jSONFieldArr2;
        Constructor<?>[] constructorArr2;
        int binarySearch;
        JSONField[] jSONFieldArr3;
        int length;
        int i10;
        Class<?> cls2 = cls;
        int i11 = i;
        JSONType jSONType2 = jSONType;
        Map<String, String> map2 = map;
        PropertyNamingStrategy propertyNamingStrategy2 = propertyNamingStrategy;
        LinkedHashMap linkedHashMap4 = new LinkedHashMap();
        HashMap hashMap4 = new HashMap();
        Field[] declaredFields = cls.getDeclaredFields();
        if (!z) {
            boolean isKotlin = isKotlin(cls);
            ArrayList<Method> arrayList = new ArrayList();
            Class<?> cls3 = cls2;
            while (cls3 != null && cls3 != Object.class) {
                Method[] declaredMethods = cls3.getDeclaredMethods();
                for (Method method2 : declaredMethods) {
                    int modifiers = method2.getModifiers();
                    if ((modifiers & 8) == 0 && (modifiers & 2) == 0 && (modifiers & 256) == 0 && (modifiers & 4) == 0 && !method2.getReturnType().equals(Void.TYPE) && method2.getParameterTypes().length == 0 && method2.getReturnType() != ClassLoader.class && method2.getDeclaringClass() != Object.class) {
                        arrayList.add(method2);
                    }
                }
                cls3 = cls3.getSuperclass();
            }
            Constructor<?>[] constructorArr3 = null;
            String[] strArr3 = null;
            short[] sArr2 = null;
            JSONField[][] jSONFieldArr4 = null;
            for (Method method3 : arrayList) {
                String name = method3.getName();
                if (!name.equals("getMetaClass") || !method3.getReturnType().getName().equals("groovy.lang.MetaClass")) {
                    JSONField jSONField4 = z3 ? (JSONField) method3.getAnnotation(JSONField.class) : null;
                    if (jSONField4 == null && z3) {
                        jSONField4 = getSupperMethodAnnotation(cls2, method3);
                    }
                    if (!isKotlin || !isKotlinIgnore(cls2, name)) {
                        if (jSONField4 != null || !isKotlin) {
                            strArr2 = strArr3;
                            sArr = sArr2;
                            jSONField = jSONField4;
                            jSONFieldArr = jSONFieldArr4;
                            constructorArr = constructorArr3;
                        } else {
                            if (constructorArr3 == null) {
                                constructorArr3 = cls.getDeclaredConstructors();
                                jSONFieldArr2 = jSONFieldArr4;
                                if (constructorArr3.length == 1) {
                                    ?? parameterAnnotations = constructorArr3[0].getParameterAnnotations();
                                    String[] koltinConstructorParameters = getKoltinConstructorParameters(cls);
                                    if (koltinConstructorParameters != null) {
                                        String[] strArr4 = new String[koltinConstructorParameters.length];
                                        constructorArr2 = constructorArr3;
                                        System.arraycopy(koltinConstructorParameters, 0, strArr4, 0, koltinConstructorParameters.length);
                                        Arrays.sort(strArr4);
                                        short[] sArr3 = new short[koltinConstructorParameters.length];
                                        jSONFieldArr2 = parameterAnnotations;
                                        for (short s = 0; s < koltinConstructorParameters.length; s = (short) (s + 1)) {
                                            sArr3[Arrays.binarySearch(strArr4, koltinConstructorParameters[s])] = s;
                                        }
                                        strArr3 = strArr4;
                                        sArr2 = sArr3;
                                    } else {
                                        constructorArr2 = constructorArr3;
                                        jSONFieldArr2 = parameterAnnotations;
                                        strArr3 = koltinConstructorParameters;
                                    }
                                    if (strArr3 != null && sArr2 != null && name.startsWith("get")) {
                                        String decapitalize = decapitalize(name.substring(3));
                                        binarySearch = Arrays.binarySearch(strArr3, decapitalize);
                                        int i12 = binarySearch;
                                        if (binarySearch < 0) {
                                            int i13 = 0;
                                            while (true) {
                                                if (i13 >= strArr3.length) {
                                                    break;
                                                } else if (decapitalize.equalsIgnoreCase(strArr3[i13])) {
                                                    i12 = i13;
                                                    break;
                                                } else {
                                                    i13++;
                                                }
                                            }
                                        }
                                        if (i12 >= 0 && (jSONFieldArr3 = jSONFieldArr2[sArr2[i12]]) != null) {
                                            length = jSONFieldArr3.length;
                                            i10 = 0;
                                            while (true) {
                                                if (i10 < length) {
                                                    break;
                                                }
                                                JSONField jSONField5 = jSONFieldArr3[i10];
                                                if (jSONField5 instanceof JSONField) {
                                                    sArr = sArr2;
                                                    strArr2 = strArr3;
                                                    jSONFieldArr = jSONFieldArr2;
                                                    constructorArr = constructorArr2;
                                                    jSONField = jSONField5;
                                                    break;
                                                }
                                                i10++;
                                                strArr3 = strArr3;
                                                jSONFieldArr3 = jSONFieldArr3;
                                            }
                                        }
                                    }
                                    sArr = sArr2;
                                    strArr2 = strArr3;
                                    jSONFieldArr = jSONFieldArr2;
                                    constructorArr = constructorArr2;
                                    jSONField = jSONField4;
                                }
                            } else {
                                jSONFieldArr2 = jSONFieldArr4;
                            }
                            constructorArr2 = constructorArr3;
                            String decapitalize2 = decapitalize(name.substring(3));
                            binarySearch = Arrays.binarySearch(strArr3, decapitalize2);
                            int i122 = binarySearch;
                            if (binarySearch < 0) {
                            }
                            length = jSONFieldArr3.length;
                            i10 = 0;
                            while (true) {
                                if (i10 < length) {
                                }
                                i10++;
                                strArr3 = strArr3;
                                jSONFieldArr3 = jSONFieldArr3;
                            }
                        }
                        if (jSONField != null) {
                            if (jSONField.serialize()) {
                                i5 = jSONField.ordinal();
                                i4 = SerializerFeature.of(jSONField.serialzeFeatures());
                                if (jSONField.name().length() != 0) {
                                    String name2 = jSONField.name();
                                    if (map2 == null || (name2 = map2.get(name2)) != null) {
                                        setAccessible(cls2, method3, i11);
                                        hashMap2 = hashMap4;
                                        fieldArr2 = declaredFields;
                                        linkedHashMap2 = linkedHashMap4;
                                        linkedHashMap2.put(name2, new FieldInfo(name2, method3, null, cls, null, i5, i4, jSONField, null, true));
                                        linkedHashMap4 = linkedHashMap2;
                                        fieldArr = fieldArr2;
                                        hashMap = hashMap2;
                                        i11 = i;
                                        propertyNamingStrategy2 = propertyNamingStrategy;
                                        jSONType2 = jSONType;
                                        declaredFields = fieldArr;
                                        constructorArr3 = constructorArr;
                                        jSONFieldArr4 = jSONFieldArr;
                                        strArr3 = strArr2;
                                        sArr2 = sArr;
                                        hashMap4 = hashMap;
                                        cls2 = cls;
                                    }
                                } else {
                                    fieldArr2 = declaredFields;
                                    hashMap2 = hashMap4;
                                    linkedHashMap2 = linkedHashMap4;
                                    z7 = false;
                                    z6 = true;
                                }
                            }
                            fieldArr = declaredFields;
                            hashMap = hashMap4;
                            linkedHashMap4 = linkedHashMap4;
                            jSONType2 = jSONType;
                            declaredFields = fieldArr;
                            constructorArr3 = constructorArr;
                            jSONFieldArr4 = jSONFieldArr;
                            strArr3 = strArr2;
                            sArr2 = sArr;
                            hashMap4 = hashMap;
                            cls2 = cls;
                        } else {
                            fieldArr2 = declaredFields;
                            hashMap2 = hashMap4;
                            linkedHashMap2 = linkedHashMap4;
                            z7 = false;
                            z6 = true;
                            i5 = 0;
                            i4 = 0;
                        }
                        if (name.startsWith("get")) {
                            if (name.length() >= 4) {
                                if (!name.equals("getClass")) {
                                    char charAt = name.charAt(3);
                                    if (Character.isUpperCase(charAt)) {
                                        str3 = compatibleWithJavaBean ? decapitalize(name.substring(3)) : Character.toLowerCase(name.charAt(3)) + name.substring(4);
                                    } else if (charAt == '_') {
                                        str3 = name.substring(4);
                                    } else if (charAt == 'f') {
                                        str3 = name.substring(3);
                                    } else if (name.length() >= 5 && Character.isUpperCase(name.charAt(4))) {
                                        str3 = decapitalize(name.substring(3));
                                    }
                                    if (!isJSONTypeIgnore(cls2, jSONType2, str3)) {
                                        Field field = getField(cls2, str3, fieldArr2, hashMap2);
                                        if (field != null) {
                                            JSONField jSONField6 = z3 ? (JSONField) field.getAnnotation(JSONField.class) : null;
                                            if (jSONField6 != null) {
                                                if (jSONField6.serialize()) {
                                                    int ordinal = jSONField6.ordinal();
                                                    int of = SerializerFeature.of(jSONField6.serialzeFeatures());
                                                    if (jSONField6.name().length() != 0) {
                                                        String name3 = jSONField6.name();
                                                        if (map2 == null || (name3 = map2.get(name3)) != null) {
                                                            jSONField3 = jSONField6;
                                                            i9 = ordinal;
                                                            i4 = of;
                                                            fieldArr4 = fieldArr2;
                                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                                            str4 = name3;
                                                            z8 = z6;
                                                        }
                                                    } else {
                                                        jSONField3 = jSONField6;
                                                        i9 = ordinal;
                                                        i4 = of;
                                                    }
                                                }
                                                hashMap = hashMap2;
                                                linkedHashMap4 = linkedHashMap2;
                                                fieldArr = fieldArr2;
                                                i11 = i;
                                                propertyNamingStrategy2 = propertyNamingStrategy;
                                                jSONType2 = jSONType;
                                                declaredFields = fieldArr;
                                                constructorArr3 = constructorArr;
                                                jSONFieldArr4 = jSONFieldArr;
                                                strArr3 = strArr2;
                                                sArr2 = sArr;
                                                hashMap4 = hashMap;
                                                cls2 = cls;
                                            } else {
                                                jSONField3 = jSONField6;
                                                i9 = i5;
                                            }
                                            fieldArr4 = fieldArr2;
                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                            str4 = str3;
                                            z8 = z7;
                                        } else {
                                            str4 = str3;
                                            i9 = i5;
                                            fieldArr4 = fieldArr2;
                                            jSONField3 = null;
                                            z8 = z7;
                                            propertyNamingStrategy2 = propertyNamingStrategy;
                                        }
                                        if (propertyNamingStrategy2 != null && !z8) {
                                            str4 = propertyNamingStrategy2.translate(str4);
                                        }
                                        if (map2 != null) {
                                            String str5 = map2.get(str4);
                                            if (str5 == null) {
                                                hashMap = hashMap2;
                                                fieldArr = fieldArr4;
                                                linkedHashMap4 = linkedHashMap2;
                                                i11 = i;
                                                jSONType2 = jSONType;
                                                declaredFields = fieldArr;
                                                constructorArr3 = constructorArr;
                                                jSONFieldArr4 = jSONFieldArr;
                                                strArr3 = strArr2;
                                                sArr2 = sArr;
                                                hashMap4 = hashMap;
                                                cls2 = cls;
                                            } else {
                                                str4 = str5;
                                            }
                                        }
                                        setAccessible(cls2, method3, i);
                                        hashMap3 = hashMap2;
                                        str = name;
                                        method = method3;
                                        fieldArr2 = fieldArr4;
                                        i6 = 3;
                                        linkedHashMap3 = linkedHashMap2;
                                        linkedHashMap3.put(str4, new FieldInfo(str4, method3, field, cls, null, i9, i4, jSONField, jSONField3, z4));
                                        i5 = i9;
                                    }
                                }
                                linkedHashMap4 = linkedHashMap2;
                                fieldArr = fieldArr2;
                                hashMap = hashMap2;
                                i11 = i;
                                propertyNamingStrategy2 = propertyNamingStrategy;
                                jSONType2 = jSONType;
                                declaredFields = fieldArr;
                                constructorArr3 = constructorArr;
                                jSONFieldArr4 = jSONFieldArr;
                                strArr3 = strArr2;
                                sArr2 = sArr;
                                hashMap4 = hashMap;
                                cls2 = cls;
                            }
                            i11 = i;
                            map2 = map;
                            linkedHashMap4 = linkedHashMap2;
                            fieldArr = fieldArr2;
                            hashMap = hashMap2;
                            propertyNamingStrategy2 = propertyNamingStrategy;
                            jSONType2 = jSONType;
                            declaredFields = fieldArr;
                            constructorArr3 = constructorArr;
                            jSONFieldArr4 = jSONFieldArr;
                            strArr3 = strArr2;
                            sArr2 = sArr;
                            hashMap4 = hashMap;
                            cls2 = cls;
                        } else {
                            str = name;
                            method = method3;
                            linkedHashMap3 = linkedHashMap2;
                            hashMap3 = hashMap2;
                            i6 = 3;
                        }
                        if (str.startsWith("is") && str.length() >= i6) {
                            char charAt2 = str.charAt(2);
                            if (Character.isUpperCase(charAt2)) {
                                str2 = compatibleWithJavaBean ? decapitalize(str.substring(2)) : Character.toLowerCase(str.charAt(2)) + str.substring(i6);
                            } else if (charAt2 == '_') {
                                str2 = str.substring(i6);
                            } else if (charAt2 == 'f') {
                                str2 = str.substring(2);
                            }
                            if (!isJSONTypeIgnore(cls2, jSONType2, str2)) {
                                Field field2 = getField(cls2, str2, fieldArr2, hashMap3);
                                Field field3 = field2 == null ? getField(cls2, str, fieldArr2, hashMap3) : field2;
                                if (field3 != null) {
                                    JSONField jSONField7 = z3 ? (JSONField) field3.getAnnotation(JSONField.class) : null;
                                    if (jSONField7 == null) {
                                        map2 = map;
                                        jSONField2 = jSONField7;
                                        i8 = i5;
                                        fieldArr3 = fieldArr2;
                                    } else if (jSONField7.serialize()) {
                                        int ordinal2 = jSONField7.ordinal();
                                        int of2 = SerializerFeature.of(jSONField7.serialzeFeatures());
                                        if (jSONField7.name().length() != 0) {
                                            str2 = jSONField7.name();
                                            map2 = map;
                                            if (map2 != null) {
                                                str2 = map2.get(str2);
                                            }
                                        } else {
                                            map2 = map;
                                        }
                                        jSONField2 = jSONField7;
                                        i8 = ordinal2;
                                        i7 = of2;
                                        fieldArr3 = fieldArr2;
                                        propertyNamingStrategy2 = propertyNamingStrategy;
                                        if (propertyNamingStrategy2 != null) {
                                            str2 = propertyNamingStrategy2.translate(str2);
                                        }
                                        if (map2 == null && (str2 = map2.get(str2)) == null) {
                                            fieldArr = fieldArr3;
                                            hashMap = hashMap3;
                                            i11 = i;
                                            linkedHashMap4 = linkedHashMap3;
                                        } else {
                                            hashMap = hashMap3;
                                            i11 = i;
                                            setAccessible(cls2, field3, i11);
                                            setAccessible(cls2, method, i11);
                                            fieldArr = fieldArr3;
                                            linkedHashMap4 = linkedHashMap3;
                                            linkedHashMap4.put(str2, new FieldInfo(str2, method, field3, cls, null, i8, i7, jSONField, jSONField2, z4));
                                        }
                                        jSONType2 = jSONType;
                                        declaredFields = fieldArr;
                                        constructorArr3 = constructorArr;
                                        jSONFieldArr4 = jSONFieldArr;
                                        strArr3 = strArr2;
                                        sArr2 = sArr;
                                        hashMap4 = hashMap;
                                        cls2 = cls;
                                    }
                                } else {
                                    map2 = map;
                                    i8 = i5;
                                    fieldArr3 = fieldArr2;
                                    jSONField2 = null;
                                }
                                i7 = i4;
                                propertyNamingStrategy2 = propertyNamingStrategy;
                                if (propertyNamingStrategy2 != null) {
                                }
                                if (map2 == null) {
                                }
                                hashMap = hashMap3;
                                i11 = i;
                                setAccessible(cls2, field3, i11);
                                setAccessible(cls2, method, i11);
                                fieldArr = fieldArr3;
                                linkedHashMap4 = linkedHashMap3;
                                linkedHashMap4.put(str2, new FieldInfo(str2, method, field3, cls, null, i8, i7, jSONField, jSONField2, z4));
                                jSONType2 = jSONType;
                                declaredFields = fieldArr;
                                constructorArr3 = constructorArr;
                                jSONFieldArr4 = jSONFieldArr;
                                strArr3 = strArr2;
                                sArr2 = sArr;
                                hashMap4 = hashMap;
                                cls2 = cls;
                            }
                        }
                        map2 = map;
                        linkedHashMap4 = linkedHashMap3;
                        hashMap = hashMap3;
                        fieldArr = fieldArr2;
                        i11 = i;
                        propertyNamingStrategy2 = propertyNamingStrategy;
                        jSONType2 = jSONType;
                        declaredFields = fieldArr;
                        constructorArr3 = constructorArr;
                        jSONFieldArr4 = jSONFieldArr;
                        strArr3 = strArr2;
                        sArr2 = sArr;
                        hashMap4 = hashMap;
                        cls2 = cls;
                    }
                }
            }
        }
        ArrayList arrayList2 = new ArrayList(declaredFields.length);
        for (Field field4 : declaredFields) {
            if ((field4.getModifiers() & 8) == 0 && !field4.getName().equals("this$0") && (field4.getModifiers() & 1) != 0) {
                arrayList2.add(field4);
            }
        }
        Class<? super Object> superclass = cls.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            Field[] declaredFields2 = superclass.getDeclaredFields();
            for (Field field5 : declaredFields2) {
                if ((field5.getModifiers() & 8) == 0 && (field5.getModifiers() & 1) != 0) {
                    arrayList2.add(field5);
                }
            }
            superclass = superclass.getSuperclass();
        }
        Iterator it2 = arrayList2.iterator();
        while (it2.hasNext()) {
            Field field6 = (Field) it2.next();
            JSONField jSONField8 = z3 ? (JSONField) field6.getAnnotation(JSONField.class) : null;
            String name4 = field6.getName();
            if (jSONField8 == null) {
                i3 = 0;
                i2 = 0;
            } else if (jSONField8.serialize()) {
                int ordinal3 = jSONField8.ordinal();
                int of3 = SerializerFeature.of(jSONField8.serialzeFeatures());
                if (jSONField8.name().length() != 0) {
                    name4 = jSONField8.name();
                }
                i3 = ordinal3;
                i2 = of3;
            }
            if (map2 == null || (name4 = map2.get(name4)) != null) {
                if (propertyNamingStrategy2 != null) {
                    name4 = propertyNamingStrategy2.translate(name4);
                }
                if (!linkedHashMap4.containsKey(name4)) {
                    setAccessible(cls, field6, i11);
                    it = it2;
                    linkedHashMap = linkedHashMap4;
                    linkedHashMap.put(name4, new FieldInfo(name4, null, field6, cls, null, i3, i2, null, jSONField8, z4));
                } else {
                    it = it2;
                    linkedHashMap = linkedHashMap4;
                }
                linkedHashMap4 = linkedHashMap;
                it2 = it;
            }
        }
        ArrayList arrayList3 = new ArrayList();
        if (jSONType != null) {
            strArr = jSONType.orders();
            if (strArr != null && strArr.length == linkedHashMap4.size()) {
                int length2 = strArr.length;
                int i14 = 0;
                while (true) {
                    if (i14 >= length2) {
                        z5 = true;
                        break;
                    } else if (!linkedHashMap4.containsKey(strArr[i14])) {
                        break;
                    } else {
                        i14++;
                    }
                }
                if (!z5) {
                    for (String str6 : strArr) {
                        arrayList3.add((FieldInfo) linkedHashMap4.get(str6));
                    }
                } else {
                    for (FieldInfo fieldInfo : linkedHashMap4.values()) {
                        arrayList3.add(fieldInfo);
                    }
                    if (z2) {
                        Collections.sort(arrayList3);
                    }
                }
                return arrayList3;
            }
        } else {
            strArr = null;
        }
        z5 = false;
        if (!z5) {
        }
        return arrayList3;
    }

    public static JSONField getSupperMethodAnnotation(Class<?> cls, Method method) {
        boolean z;
        JSONField jSONField;
        boolean z2;
        JSONField jSONField2;
        for (Class<?> cls2 : cls.getInterfaces()) {
            Method[] methods = cls2.getMethods();
            for (Method method2 : methods) {
                if (method2.getName().equals(method.getName())) {
                    Class<?>[] parameterTypes = method2.getParameterTypes();
                    Class<?>[] parameterTypes2 = method.getParameterTypes();
                    if (parameterTypes.length != parameterTypes2.length) {
                        continue;
                    } else {
                        int i = 0;
                        while (true) {
                            if (i >= parameterTypes.length) {
                                z2 = true;
                                break;
                            } else if (!parameterTypes[i].equals(parameterTypes2[i])) {
                                z2 = false;
                                break;
                            } else {
                                i++;
                            }
                        }
                        if (z2 && (jSONField2 = (JSONField) method2.getAnnotation(JSONField.class)) != null) {
                            return jSONField2;
                        }
                    }
                }
            }
        }
        Class<? super Object> superclass = cls.getSuperclass();
        if (superclass != null && Modifier.isAbstract(superclass.getModifiers())) {
            Class<?>[] parameterTypes3 = method.getParameterTypes();
            Method[] methods2 = superclass.getMethods();
            for (Method method3 : methods2) {
                Class<?>[] parameterTypes4 = method3.getParameterTypes();
                if (parameterTypes4.length == parameterTypes3.length && method3.getName().equals(method.getName())) {
                    int i2 = 0;
                    while (true) {
                        if (i2 >= parameterTypes3.length) {
                            z = true;
                            break;
                        } else if (!parameterTypes4[i2].equals(parameterTypes3[i2])) {
                            z = false;
                            break;
                        } else {
                            i2++;
                        }
                    }
                    if (z && (jSONField = (JSONField) method3.getAnnotation(JSONField.class)) != null) {
                        return jSONField;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isJSONTypeIgnore(Class<?> cls, JSONType jSONType, String str) {
        if (!(jSONType == null || jSONType.ignores() == null)) {
            for (String str2 : jSONType.ignores()) {
                if (str.equalsIgnoreCase(str2)) {
                    return true;
                }
            }
        }
        Class<? super Object> superclass = cls.getSuperclass();
        return (superclass == Object.class || superclass == null || !isJSONTypeIgnore(superclass, (JSONType) superclass.getAnnotation(JSONType.class), str)) ? false : true;
    }

    public static boolean isGenericParamType(Type type) {
        if (type instanceof ParameterizedType) {
            return true;
        }
        if (!(type instanceof Class)) {
            return false;
        }
        Type genericSuperclass = ((Class) type).getGenericSuperclass();
        if (genericSuperclass == Object.class || !isGenericParamType(genericSuperclass)) {
            return false;
        }
        return true;
    }

    public static Type getGenericParamType(Type type) {
        return type instanceof Class ? getGenericParamType(((Class) type).getGenericSuperclass()) : type;
    }

    public static Class<?> getClass(Type type) {
        if (type.getClass() == Class.class) {
            return (Class) type;
        }
        if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        }
        if (type instanceof TypeVariable) {
            return (Class) ((TypeVariable) type).getBounds()[0];
        }
        if (!(type instanceof WildcardType)) {
            return Object.class;
        }
        Type[] upperBounds = ((WildcardType) type).getUpperBounds();
        if (upperBounds.length == 1) {
            return getClass(upperBounds[0]);
        }
        return Object.class;
    }

    public static String decapitalize(String str) {
        if (str == null || str.length() == 0 || (str.length() > 1 && Character.isUpperCase(str.charAt(1)) && Character.isUpperCase(str.charAt(0)))) {
            return str;
        }
        char[] charArray = str.toCharArray();
        charArray[0] = Character.toLowerCase(charArray[0]);
        return new String(charArray);
    }

    public static boolean setAccessible(Class<?> cls, Member member, int i) {
        if (member != null && setAccessibleEnable) {
            Class<? super Object> superclass = cls.getSuperclass();
            if ((superclass == null || superclass == Object.class) && (member.getModifiers() & 1) != 0 && (i & 1) != 0) {
                return false;
            }
            try {
                ((AccessibleObject) member).setAccessible(true);
                return true;
            } catch (AccessControlException unused) {
                setAccessibleEnable = false;
            }
        }
        return false;
    }

    public static Field getField(Class<?> cls, String str, Field[] fieldArr) {
        return getField(cls, str, fieldArr, null);
    }

    public static Field getField(Class<?> cls, String str, Field[] fieldArr, Map<Class<?>, Field[]> map) {
        Field field0 = getField0(cls, str, fieldArr, map);
        if (field0 == null) {
            field0 = getField0(cls, "_" + str, fieldArr, map);
        }
        if (field0 == null) {
            field0 = getField0(cls, "m_" + str, fieldArr, map);
        }
        if (field0 != null) {
            return field0;
        }
        return getField0(cls, DateFormat.MINUTE + str.substring(0, 1).toUpperCase() + str.substring(1), fieldArr, map);
    }

    private static Field getField0(Class<?> cls, String str, Field[] fieldArr, Map<Class<?>, Field[]> map) {
        char charAt;
        char charAt2;
        for (Field field : fieldArr) {
            String name = field.getName();
            if (str.equals(name)) {
                return field;
            }
            if (str.length() > 2 && (charAt = str.charAt(0)) >= 'a' && charAt <= 'z' && (charAt2 = str.charAt(1)) >= 'A' && charAt2 <= 'Z' && str.equalsIgnoreCase(name)) {
                return field;
            }
        }
        Class<? super Object> superclass = cls.getSuperclass();
        Field[] fieldArr2 = null;
        if (superclass == null || superclass == Object.class) {
            return null;
        }
        if (map != null) {
            fieldArr2 = map.get(superclass);
        }
        if (fieldArr2 == null) {
            fieldArr2 = superclass.getDeclaredFields();
            if (map != null) {
                map.put(superclass, fieldArr2);
            }
        }
        return getField(superclass, str, fieldArr2, map);
    }

    public static Type getCollectionItemType(Type type) {
        Type type2;
        if (type instanceof ParameterizedType) {
            type2 = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (type2 instanceof WildcardType) {
                Type[] upperBounds = ((WildcardType) type2).getUpperBounds();
                if (upperBounds.length == 1) {
                    type2 = upperBounds[0];
                }
            }
        } else {
            if (type instanceof Class) {
                Class cls = (Class) type;
                if (!cls.getName().startsWith("java.")) {
                    type2 = getCollectionItemType(cls.getGenericSuperclass());
                }
            }
            type2 = null;
        }
        return type2 == null ? Object.class : type2;
    }

    public static Object defaultValue(Class<?> cls) {
        if (cls == Byte.TYPE) {
            return (byte) 0;
        }
        if (cls == Short.TYPE) {
            return (short) 0;
        }
        if (cls == Integer.TYPE) {
            return 0;
        }
        if (cls == Long.TYPE) {
            return 0L;
        }
        if (cls == Float.TYPE) {
            return Float.valueOf(0.0f);
        }
        if (cls == Double.TYPE) {
            return Double.valueOf(0.0d);
        }
        if (cls == Boolean.TYPE) {
            return Boolean.FALSE;
        }
        return cls == Character.TYPE ? '0' : null;
    }

    public static boolean getArgument(Type[] typeArr, TypeVariable[] typeVariableArr, Type[] typeArr2) {
        if (typeArr2 == null || typeVariableArr.length == 0) {
            return false;
        }
        boolean z = false;
        for (int i = 0; i < typeArr.length; i++) {
            Type type = typeArr[i];
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (getArgument(actualTypeArguments, typeVariableArr, typeArr2)) {
                    typeArr[i] = new ParameterizedTypeImpl(actualTypeArguments, parameterizedType.getOwnerType(), parameterizedType.getRawType());
                    z = true;
                }
            } else if (type instanceof TypeVariable) {
                boolean z2 = z;
                for (int i2 = 0; i2 < typeVariableArr.length; i2++) {
                    if (type.equals(typeVariableArr[i2])) {
                        typeArr[i] = typeArr2[i2];
                        z2 = true;
                    }
                }
                z = z2;
            }
        }
        return z;
    }

    public static double parseDouble(String str) {
        double d;
        double d2;
        int length = str.length();
        if (length > 10) {
            return Double.parseDouble(str);
        }
        long j = 0;
        boolean z = false;
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            char charAt = str.charAt(i2);
            if (charAt == '-' && i2 == 0) {
                z = true;
            } else if (charAt == '.') {
                if (i != 0) {
                    return Double.parseDouble(str);
                }
                i = (length - i2) - 1;
            } else if (charAt < '0' || charAt > '9') {
                return Double.parseDouble(str);
            } else {
                j = (j * 10) + ((long) (charAt - '0'));
            }
        }
        if (z) {
            j = -j;
        }
        switch (i) {
            case 0:
                return (double) j;
            case 1:
                d = (double) j;
                d2 = 10.0d;
                break;
            case 2:
                d = (double) j;
                d2 = 100.0d;
                break;
            case 3:
                d = (double) j;
                d2 = 1000.0d;
                break;
            case 4:
                d = (double) j;
                d2 = 10000.0d;
                break;
            case 5:
                d = (double) j;
                d2 = 100000.0d;
                break;
            case 6:
                d = (double) j;
                d2 = 1000000.0d;
                break;
            case 7:
                d = (double) j;
                d2 = 1.0E7d;
                break;
            case 8:
                d = (double) j;
                d2 = 1.0E8d;
                break;
            case 9:
                d = (double) j;
                d2 = 1.0E9d;
                break;
            default:
                return Double.parseDouble(str);
        }
        return d / d2;
    }

    public static float parseFloat(String str) {
        float f;
        float f2;
        int length = str.length();
        if (length >= 10) {
            return Float.parseFloat(str);
        }
        long j = 0;
        boolean z = false;
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            char charAt = str.charAt(i2);
            if (charAt == '-' && i2 == 0) {
                z = true;
            } else if (charAt == '.') {
                if (i != 0) {
                    return Float.parseFloat(str);
                }
                i = (length - i2) - 1;
            } else if (charAt < '0' || charAt > '9') {
                return Float.parseFloat(str);
            } else {
                j = (j * 10) + ((long) (charAt - '0'));
            }
        }
        if (z) {
            j = -j;
        }
        switch (i) {
            case 0:
                return (float) j;
            case 1:
                f = (float) j;
                f2 = 10.0f;
                break;
            case 2:
                f = (float) j;
                f2 = 100.0f;
                break;
            case 3:
                f = (float) j;
                f2 = 1000.0f;
                break;
            case 4:
                f = (float) j;
                f2 = 10000.0f;
                break;
            case 5:
                f = (float) j;
                f2 = 100000.0f;
                break;
            case 6:
                f = (float) j;
                f2 = 1000000.0f;
                break;
            case 7:
                f = (float) j;
                f2 = 1.0E7f;
                break;
            case 8:
                f = (float) j;
                f2 = 1.0E8f;
                break;
            case 9:
                f = (float) j;
                f2 = 1.0E9f;
                break;
            default:
                return Float.parseFloat(str);
        }
        return f / f2;
    }

    public static long fnv_64_lower(String str) {
        if (str == null) {
            return 0;
        }
        long j = -3750763034362895579L;
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (!(charAt == '_' || charAt == '-')) {
                if (charAt >= 'A' && charAt <= 'Z') {
                    charAt = (char) (charAt + ' ');
                }
                j = (j ^ ((long) charAt)) * 1099511628211L;
            }
        }
        return j;
    }

    public static void addMapping(String str, Class<?> cls) {
        mappings.put(str, cls);
    }

    public static Type checkPrimitiveArray(GenericArrayType genericArrayType) {
        Type genericComponentType = genericArrayType.getGenericComponentType();
        String str = "[";
        while (genericComponentType instanceof GenericArrayType) {
            genericComponentType = ((GenericArrayType) genericComponentType).getGenericComponentType();
            str = str + str;
        }
        if (!(genericComponentType instanceof Class)) {
            return genericArrayType;
        }
        Class cls = (Class) genericComponentType;
        if (!cls.isPrimitive()) {
            return genericArrayType;
        }
        try {
            if (cls == Boolean.TYPE) {
                return Class.forName(str + "Z");
            } else if (cls == Character.TYPE) {
                return Class.forName(str + "C");
            } else if (cls == Byte.TYPE) {
                return Class.forName(str + "B");
            } else if (cls == Short.TYPE) {
                return Class.forName(str + "S");
            } else if (cls == Integer.TYPE) {
                return Class.forName(str + "I");
            } else if (cls == Long.TYPE) {
                return Class.forName(str + "J");
            } else if (cls == Float.TYPE) {
                return Class.forName(str + "F");
            } else if (cls != Double.TYPE) {
                return genericArrayType;
            } else {
                return Class.forName(str + "D");
            }
        } catch (ClassNotFoundException unused) {
            return genericArrayType;
        }
    }
}
