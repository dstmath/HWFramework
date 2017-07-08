package tmsdkobf;

import java.lang.reflect.Field;

/* compiled from: Unknown */
public final class ng {
    public static Class<?> BY;

    public static Object a(Object obj, String str, Object[] objArr) throws Exception {
        Class cls = obj.getClass();
        Class[] clsArr = new Class[objArr.length];
        int length = objArr.length;
        for (int i = 0; i < length; i++) {
            clsArr[i] = objArr[i].getClass();
            Class cls2;
            if (clsArr[i] == Integer.class) {
                cls2 = Integer.TYPE;
                clsArr[i] = cls2;
            } else if (clsArr[i] == Boolean.class) {
                cls2 = Boolean.TYPE;
                clsArr[i] = cls2;
            }
        }
        return cls.getMethod(str, clsArr).invoke(obj, objArr);
    }

    public static Object a(String str, Object[] objArr) throws Exception {
        Class[] clsArr = null;
        Class cls = Class.forName(str);
        if (objArr != null) {
            clsArr = new Class[objArr.length];
            int length = objArr.length;
            for (int i = 0; i < length; i++) {
                clsArr[i] = objArr[i].getClass();
            }
        }
        return cls.getConstructor(clsArr).newInstance(objArr);
    }

    public static Object b(Object obj, String str) throws Exception {
        return obj.getClass().getField(str).get(obj);
    }

    public static final boolean cK(String str) {
        Class cls = null;
        try {
            cls = Class.forName(str);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        BY = cls;
        return BY != null;
    }

    private static final Field getField(String str) {
        Field field = null;
        if (BY != null) {
            try {
                field = BY.getDeclaredField(str);
                field.setAccessible(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
            }
        }
        return field;
    }

    public static final int h(String str, int i) {
        Field field = getField(str);
        if (field != null) {
            try {
                i = field.getInt(null);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            }
        }
        return i;
    }
}
