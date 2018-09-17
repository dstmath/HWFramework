package tmsdkobf;

import java.lang.reflect.Field;

public final class mh {
    public static Class<?> zK;

    public static Object a(Object obj, String str) throws Exception {
        return obj.getClass().getField(str).get(obj);
    }

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
        Class[] clsArr;
        Class cls = Class.forName(str);
        if (objArr != null) {
            clsArr = new Class[objArr.length];
            int length = objArr.length;
            for (int i = 0; i < length; i++) {
                clsArr[i] = objArr[i].getClass();
            }
        } else {
            clsArr = null;
        }
        return cls.getConstructor(clsArr).newInstance(objArr);
    }

    public static final boolean bY(String str) {
        Class cls = null;
        try {
            cls = Class.forName(str);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        zK = cls;
        return zK != null;
    }

    public static final int e(String str, int -l_2_I) {
        Field field = getField(str);
        if (field == null) {
            return -l_2_I;
        }
        try {
            return field.getInt(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return -l_2_I;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return -l_2_I;
        }
    }

    private static final Field getField(String str) {
        Field field = null;
        if (zK == null) {
            return null;
        }
        try {
            field = zK.getDeclaredField(str);
            field.setAccessible(true);
            return field;
        } catch (SecurityException e) {
            e.printStackTrace();
            return field;
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
            return field;
        }
    }
}
