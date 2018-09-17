package com.huawei.android.pushagent.utils.b;

import com.huawei.android.pushagent.utils.d.c;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public abstract class a {
    public static Field[] ot(Class cls) {
        Object obj = null;
        if (cls.getSuperclass() != null) {
            obj = ot(cls.getSuperclass());
        }
        Object declaredFields = cls.getDeclaredFields();
        if (obj == null || obj.length <= 0) {
            return declaredFields;
        }
        Object obj2 = new Field[(declaredFields.length + obj.length)];
        System.arraycopy(obj, 0, obj2, 0, obj.length);
        System.arraycopy(declaredFields, 0, obj2, obj.length, declaredFields.length);
        return obj2;
    }

    public static Class ou(Field field) {
        if (Map.class.isAssignableFrom(field.getType())) {
            return ov(field, 1);
        }
        if (List.class.isAssignableFrom(field.getType())) {
            return ov(field, 0);
        }
        return null;
    }

    private static Class ov(Field field, int i) {
        int i2 = 0;
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            return null;
        }
        Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
        if (actualTypeArguments == null || actualTypeArguments.length <= i) {
            return null;
        }
        try {
            if (actualTypeArguments[i] instanceof Class) {
                return (Class) actualTypeArguments[i];
            }
            String obj = actualTypeArguments[i].toString();
            int indexOf = obj.indexOf("class ");
            if (indexOf >= 0) {
                i2 = indexOf;
            }
            indexOf = obj.indexOf("<");
            if (indexOf < 0) {
                indexOf = obj.length();
            }
            return Class.forName(obj.substring(i2, indexOf));
        } catch (ClassNotFoundException e) {
            c.sj("PushLog2951", "getType ClassNotFoundException");
            return null;
        } catch (Exception e2) {
            c.sj("PushLog2951", "getType Exception");
            return null;
        }
    }

    public static Field ow(Field field, boolean z) {
        field.setAccessible(z);
        return field;
    }
}
