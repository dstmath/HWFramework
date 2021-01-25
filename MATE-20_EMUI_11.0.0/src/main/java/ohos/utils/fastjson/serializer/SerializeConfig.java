package ohos.utils.fastjson.serializer;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.AbstractSequentialList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONAware;
import ohos.utils.fastjson.JSONStreamAware;
import ohos.utils.fastjson.PropertyNamingStrategy;
import ohos.utils.fastjson.util.IdentityHashMap;

public class SerializeConfig {
    public static final SerializeConfig globalInstance = new SerializeConfig();
    public PropertyNamingStrategy propertyNamingStrategy;
    private final IdentityHashMap<ObjectSerializer> serializers = new IdentityHashMap<>(1024);
    protected String typeKey = JSON.DEFAULT_TYPE_KEY;

    public static final SerializeConfig getGlobalInstance() {
        return globalInstance;
    }

    public ObjectSerializer registerIfNotExists(Class<?> cls) {
        return registerIfNotExists(cls, cls.getModifiers(), false, true, true, true);
    }

    public ObjectSerializer registerIfNotExists(Class<?> cls, int i, boolean z, boolean z2, boolean z3, boolean z4) {
        ObjectSerializer objectSerializer = this.serializers.get(cls);
        if (objectSerializer != null) {
            return objectSerializer;
        }
        JavaBeanSerializer javaBeanSerializer = new JavaBeanSerializer(cls, i, null, z, z2, z3, z4, this.propertyNamingStrategy);
        this.serializers.put(cls, javaBeanSerializer);
        return javaBeanSerializer;
    }

    public SerializeConfig() {
        this.serializers.put(Boolean.class, BooleanCodec.instance);
        this.serializers.put(Character.class, MiscCodec.instance);
        this.serializers.put(Byte.class, IntegerCodec.instance);
        this.serializers.put(Short.class, IntegerCodec.instance);
        this.serializers.put(Integer.class, IntegerCodec.instance);
        this.serializers.put(Long.class, IntegerCodec.instance);
        this.serializers.put(Float.class, NumberCodec.instance);
        this.serializers.put(Double.class, NumberCodec.instance);
        this.serializers.put(Number.class, NumberCodec.instance);
        this.serializers.put(BigDecimal.class, BigDecimalCodec.instance);
        this.serializers.put(BigInteger.class, BigDecimalCodec.instance);
        this.serializers.put(String.class, StringCodec.instance);
        this.serializers.put(Object[].class, ArrayCodec.instance);
        this.serializers.put(Class.class, MiscCodec.instance);
        this.serializers.put(SimpleDateFormat.class, MiscCodec.instance);
        this.serializers.put(Locale.class, MiscCodec.instance);
        this.serializers.put(Currency.class, MiscCodec.instance);
        this.serializers.put(TimeZone.class, MiscCodec.instance);
        this.serializers.put(UUID.class, MiscCodec.instance);
        this.serializers.put(URI.class, MiscCodec.instance);
        this.serializers.put(URL.class, MiscCodec.instance);
        this.serializers.put(Pattern.class, MiscCodec.instance);
        this.serializers.put(Charset.class, MiscCodec.instance);
    }

    /* JADX WARNING: Removed duplicated region for block: B:76:0x019f  */
    /* JADX WARNING: Removed duplicated region for block: B:83:? A[RETURN, SYNTHETIC] */
    public ObjectSerializer get(Class<?> cls) {
        ArraySerializer arraySerializer;
        ObjectSerializer enumSerializer;
        Class<? super Object> superclass;
        boolean z;
        MiscCodec miscCodec;
        ObjectSerializer objectSerializer = this.serializers.get(cls);
        if (objectSerializer != null) {
            return objectSerializer;
        }
        if (Map.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap = this.serializers;
            enumSerializer = new MapSerializer();
            identityHashMap.put(cls, enumSerializer);
        } else if (AbstractSequentialList.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap2 = this.serializers;
            enumSerializer = CollectionCodec.instance;
            identityHashMap2.put(cls, enumSerializer);
        } else if (List.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap3 = this.serializers;
            enumSerializer = new ListSerializer();
            identityHashMap3.put(cls, enumSerializer);
        } else if (Collection.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap4 = this.serializers;
            enumSerializer = CollectionCodec.instance;
            identityHashMap4.put(cls, enumSerializer);
        } else if (Date.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap5 = this.serializers;
            enumSerializer = DateCodec.instance;
            identityHashMap5.put(cls, enumSerializer);
        } else if (JSONAware.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap6 = this.serializers;
            enumSerializer = MiscCodec.instance;
            identityHashMap6.put(cls, enumSerializer);
        } else if (JSONSerializable.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap7 = this.serializers;
            enumSerializer = MiscCodec.instance;
            identityHashMap7.put(cls, enumSerializer);
        } else if (JSONStreamAware.class.isAssignableFrom(cls)) {
            IdentityHashMap<ObjectSerializer> identityHashMap8 = this.serializers;
            enumSerializer = MiscCodec.instance;
            identityHashMap8.put(cls, enumSerializer);
        } else if (cls.isEnum() || !((superclass = cls.getSuperclass()) == null || superclass == Object.class || !superclass.isEnum())) {
            IdentityHashMap<ObjectSerializer> identityHashMap9 = this.serializers;
            enumSerializer = new EnumSerializer();
            identityHashMap9.put(cls, enumSerializer);
        } else {
            if (cls.isArray()) {
                Class<?> componentType = cls.getComponentType();
                ObjectSerializer objectSerializer2 = get(componentType);
                IdentityHashMap<ObjectSerializer> identityHashMap10 = this.serializers;
                ArraySerializer arraySerializer2 = new ArraySerializer(componentType, objectSerializer2);
                identityHashMap10.put(cls, arraySerializer2);
                arraySerializer = arraySerializer2;
            } else if (Throwable.class.isAssignableFrom(cls)) {
                JavaBeanSerializer javaBeanSerializer = new JavaBeanSerializer(cls, this.propertyNamingStrategy);
                javaBeanSerializer.features |= SerializerFeature.WriteClassName.mask;
                this.serializers.put(cls, javaBeanSerializer);
                arraySerializer = javaBeanSerializer;
            } else if (TimeZone.class.isAssignableFrom(cls)) {
                IdentityHashMap<ObjectSerializer> identityHashMap11 = this.serializers;
                enumSerializer = MiscCodec.instance;
                identityHashMap11.put(cls, enumSerializer);
            } else if (Charset.class.isAssignableFrom(cls)) {
                IdentityHashMap<ObjectSerializer> identityHashMap12 = this.serializers;
                enumSerializer = MiscCodec.instance;
                identityHashMap12.put(cls, enumSerializer);
            } else if (Enumeration.class.isAssignableFrom(cls)) {
                IdentityHashMap<ObjectSerializer> identityHashMap13 = this.serializers;
                enumSerializer = MiscCodec.instance;
                identityHashMap13.put(cls, enumSerializer);
            } else if (Calendar.class.isAssignableFrom(cls)) {
                IdentityHashMap<ObjectSerializer> identityHashMap14 = this.serializers;
                enumSerializer = DateCodec.instance;
                identityHashMap14.put(cls, enumSerializer);
            } else {
                Class<?>[] interfaces = cls.getInterfaces();
                int length = interfaces.length;
                boolean z2 = false;
                int i = 0;
                while (true) {
                    z = true;
                    if (i >= length) {
                        z = false;
                        break;
                    }
                    Class<?> cls2 = interfaces[i];
                    if (cls2.getName().equals("net.sf.cglib.proxy.Factory") || cls2.getName().equals("org.springframework.cglib.proxy.Factory")) {
                        break;
                    } else if (cls2.getName().equals("javassist.util.proxy.ProxyObject")) {
                        break;
                    } else {
                        i++;
                    }
                }
                z = false;
                z2 = true;
                if (z2 || z) {
                    ObjectSerializer objectSerializer3 = get(cls.getSuperclass());
                    this.serializers.put(cls, objectSerializer3);
                    return objectSerializer3;
                }
                if (cls.getName().startsWith("android.net.Uri$")) {
                    miscCodec = MiscCodec.instance;
                } else {
                    miscCodec = new JavaBeanSerializer(cls, this.propertyNamingStrategy);
                }
                this.serializers.put(cls, miscCodec);
                arraySerializer = miscCodec;
            }
            return arraySerializer != null ? this.serializers.get(cls) : arraySerializer;
        }
        arraySerializer = enumSerializer;
        if (arraySerializer != null) {
        }
    }

    public boolean put(Type type, ObjectSerializer objectSerializer) {
        return this.serializers.put(type, objectSerializer);
    }

    public String getTypeKey() {
        return this.typeKey;
    }

    public void setTypeKey(String str) {
        this.typeKey = str;
    }
}
