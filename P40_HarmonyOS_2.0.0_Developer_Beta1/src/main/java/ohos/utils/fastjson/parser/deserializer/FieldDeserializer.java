package ohos.utils.fastjson.parser.deserializer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.util.FieldInfo;

public abstract class FieldDeserializer {
    public final Class<?> clazz;
    protected long[] enumNameHashCodes;
    protected Enum[] enums;
    public final FieldInfo fieldInfo;

    public abstract void parseField(DefaultJSONParser defaultJSONParser, Object obj, Type type, Map<String, Object> map);

    public FieldDeserializer(Class<?> cls, FieldInfo fieldInfo2, int i) {
        this.clazz = cls;
        this.fieldInfo = fieldInfo2;
        if (fieldInfo2 != null) {
            Class<?> cls2 = fieldInfo2.fieldClass;
            if (cls2.isEnum()) {
                Enum[] enumArr = (Enum[]) cls2.getEnumConstants();
                long[] jArr = new long[enumArr.length];
                this.enumNameHashCodes = new long[enumArr.length];
                for (int i2 = 0; i2 < enumArr.length; i2++) {
                    String name = enumArr[i2].name();
                    long j = -3750763034362895579L;
                    for (int i3 = 0; i3 < name.length(); i3++) {
                        j = (j ^ ((long) name.charAt(i3))) * 1099511628211L;
                    }
                    jArr[i2] = j;
                    this.enumNameHashCodes[i2] = j;
                }
                Arrays.sort(this.enumNameHashCodes);
                this.enums = new Enum[enumArr.length];
                for (int i4 = 0; i4 < this.enumNameHashCodes.length; i4++) {
                    int i5 = 0;
                    while (true) {
                        if (i5 >= jArr.length) {
                            break;
                        } else if (this.enumNameHashCodes[i4] == jArr[i5]) {
                            this.enums[i4] = enumArr[i5];
                            break;
                        } else {
                            i5++;
                        }
                    }
                }
            }
        }
    }

    public Enum getEnumByHashCode(long j) {
        int binarySearch;
        if (this.enums != null && (binarySearch = Arrays.binarySearch(this.enumNameHashCodes, j)) >= 0) {
            return this.enums[binarySearch];
        }
        return null;
    }

    public void setValue(Object obj, int i) throws IllegalAccessException {
        this.fieldInfo.field.setInt(obj, i);
    }

    public void setValue(Object obj, long j) throws IllegalAccessException {
        this.fieldInfo.field.setLong(obj, j);
    }

    public void setValue(Object obj, float f) throws IllegalAccessException {
        this.fieldInfo.field.setFloat(obj, f);
    }

    public void setValue(Object obj, double d) throws IllegalAccessException {
        this.fieldInfo.field.setDouble(obj, d);
    }

    public void setValue(Object obj, Object obj2) {
        if (obj2 != null || !this.fieldInfo.fieldClass.isPrimitive()) {
            Field field = this.fieldInfo.field;
            Method method = this.fieldInfo.method;
            try {
                if (this.fieldInfo.fieldAccess) {
                    if (!this.fieldInfo.getOnly) {
                        field.set(obj, obj2);
                    } else if (Map.class.isAssignableFrom(this.fieldInfo.fieldClass)) {
                        Map map = (Map) field.get(obj);
                        if (map != null) {
                            map.putAll((Map) obj2);
                        }
                    } else {
                        Collection collection = (Collection) field.get(obj);
                        if (collection != null) {
                            collection.addAll((Collection) obj2);
                        }
                    }
                } else if (!this.fieldInfo.getOnly) {
                    method.invoke(obj, obj2);
                } else if (Map.class.isAssignableFrom(this.fieldInfo.fieldClass)) {
                    Map map2 = (Map) method.invoke(obj, new Object[0]);
                    if (map2 != null) {
                        map2.putAll((Map) obj2);
                    }
                } else {
                    Collection collection2 = (Collection) method.invoke(obj, new Object[0]);
                    if (collection2 != null) {
                        collection2.addAll((Collection) obj2);
                    }
                }
            } catch (Exception e) {
                throw new JSONException("set property error, " + this.fieldInfo.name, e);
            }
        }
    }
}
