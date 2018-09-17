package com.huawei.android.pushagent.utils.b;

import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.a.a;
import com.huawei.android.pushagent.utils.d.c;
import com.huawei.android.pushagent.utils.e;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class b {
    private static final Class[] ey = new Class[]{String.class, Object.class, Integer.class, Short.class, Long.class, Byte.class, Float.class, Double.class, Boolean.class};
    private static final Class[] ez = new Class[]{String.class, Object.class, Integer.class, Short.class, Long.class, Byte.class, Float.class, Double.class, Character.class, Boolean.class};
    private static final Map<Class, c> fa = new HashMap();

    static {
        d dVar = new d();
        fa.put(Integer.TYPE, dVar);
        fa.put(Integer.class, dVar);
        e eVar = new e();
        fa.put(Long.TYPE, eVar);
        fa.put(Long.class, eVar);
        f fVar = new f();
        fa.put(Float.TYPE, fVar);
        fa.put(Float.class, fVar);
        g gVar = new g();
        fa.put(Double.TYPE, gVar);
        fa.put(Double.class, gVar);
        h hVar = new h();
        fa.put(Short.TYPE, hVar);
        fa.put(Short.class, hVar);
        i iVar = new i();
        fa.put(Byte.TYPE, iVar);
        fa.put(Byte.class, iVar);
        j jVar = new j();
        fa.put(Boolean.TYPE, jVar);
        fa.put(Boolean.class, jVar);
    }

    public static <T> T pv(String str, Class<T> cls, Class... clsArr) {
        if (TextUtils.isEmpty(str)) {
            throw pm(false, "Input json string cannot be empty!", new Object[0]);
        }
        pb(cls);
        return pc(str, cls, clsArr);
    }

    private static <T> T pc(String str, Class<T> cls, Class[] clsArr) {
        try {
            return ps(new JSONObject(str), cls, clsArr);
        } catch (JSONException e) {
            try {
                return pr(new JSONArray(str), cls, clsArr);
            } catch (JSONException e2) {
                throw pl("Input string is not valid json string!", new Object[0]);
            }
        }
    }

    public static <T> T oy(String str, Class<T> cls, Class... clsArr) {
        try {
            return pv(str, cls, clsArr);
        } catch (JSONException e) {
            c.sj("PushLog2951", "toObject JSONException");
            return null;
        } catch (Exception e2) {
            c.sj("PushLog2951", "toObject error");
            return null;
        }
    }

    public static String oz(Object obj) {
        try {
            return pu(obj, false);
        } catch (JSONException e) {
            throw e;
        } catch (IllegalAccessException e2) {
            throw pl("toJson error", new Object[0]);
        }
    }

    private static String pu(Object obj, boolean z) {
        if (obj == null) {
            return "";
        }
        pb(obj.getClass());
        if (obj instanceof List) {
            return pk((List) obj, z);
        }
        if (obj instanceof Map) {
            return po((Map) obj, z);
        }
        if (obj instanceof JSONObject) {
            return obj.toString();
        }
        return pp(obj, z);
    }

    private static String pp(Object obj, boolean z) {
        Field[] ot = a.ot(obj.getClass());
        if (ot.length <= 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('{');
        int length = ot.length;
        for (int i = 0; i < length; i++) {
            ot[i] = a.ow(ot[i], true);
            if (pi(ot[i])) {
                String pe = pe(ot[i]);
                Object obj2 = ot[i].get(obj);
                String px = (z && ot[i].isAnnotationPresent(com.huawei.android.pushagent.datatype.a.b.class)) ? obj2 != null ? "\"******\"" : null : px(obj2, z);
                if (px != null) {
                    stringBuilder.append('\"').append(pe).append("\":").append(px);
                    if (i < length - 1) {
                        stringBuilder.append(',');
                    }
                }
            }
        }
        pd(stringBuilder);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    private static String px(Object obj, boolean z) {
        if (obj == null) {
            return null;
        }
        String str;
        if ((obj instanceof String) || (obj instanceof Character)) {
            str = "\"" + e.uz(obj.toString()) + "\"";
        } else if ((obj instanceof Integer) || (obj instanceof Long) || (obj instanceof Boolean) || (obj instanceof Float) || (obj instanceof Byte) || (obj instanceof Double) || (obj instanceof Short)) {
            str = obj.toString();
        } else if (obj instanceof List) {
            str = pk((List) obj, z);
        } else if (obj instanceof Map) {
            str = po((Map) obj, z);
        } else if (obj.getClass().isArray()) {
            str = pa(obj, z);
        } else {
            str = pu(obj, z);
        }
        return str;
    }

    private static String pk(List list, boolean z) {
        if (list.size() <= 0) {
            return "[]";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        for (int i = 0; i < list.size(); i++) {
            String px = px(list.get(i), z);
            if (px != null) {
                stringBuilder.append(px).append(',');
            }
        }
        pd(stringBuilder);
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    private static String pa(Object obj, boolean z) {
        int length = Array.getLength(obj);
        if (length <= 0) {
            return "[]";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        for (int i = 0; i < length; i++) {
            String px = px(Array.get(obj, i), z);
            if (px != null) {
                stringBuilder.append(px).append(',');
            }
        }
        pd(stringBuilder);
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    private static String po(Map map, boolean z) {
        if (map.size() <= 0) {
            return "{}";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('{');
        Iterable<Entry> entrySet = map.entrySet();
        int size = entrySet.size();
        int i = 0;
        for (Entry entry : entrySet) {
            int i2 = i + 1;
            String str = (String) entry.getKey();
            String px = px(entry.getValue(), z);
            if (px != null) {
                stringBuilder.append('\"').append(str).append("\":");
                stringBuilder.append(px);
            }
            if (i2 < size && px != null) {
                stringBuilder.append(',');
            }
            i = i2;
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    private static void pd(StringBuilder stringBuilder) {
        int length = stringBuilder.length();
        if (length > 0 && stringBuilder.charAt(length - 1) == ',') {
            stringBuilder.delete(length - 1, length);
        }
    }

    private static void pb(Class cls) {
        if (cls.isPrimitive()) {
            throw pl("Root obj class (%s) cannot be primitive type!", cls);
        }
        for (Class cls2 : ez) {
            if (cls == cls2) {
                throw pl("Root obj class (%s) is invalid in conversion", cls);
            }
        }
    }

    private static <T> T ps(JSONObject jSONObject, Class<T> cls, Class[] clsArr) {
        Class cls2 = null;
        if (Collection.class.isAssignableFrom(cls)) {
            throw pl("Obj class %s is Collection type which mismatches with JsonObject", cls);
        } else if (cls.isArray()) {
            throw pl("Obj class %s is array type which mismatches with JsonObject", cls);
        } else if (Map.class.isAssignableFrom(cls)) {
            if (clsArr != null && clsArr.length > 0) {
                cls2 = clsArr[0];
            }
            return pn(cls, cls2, jSONObject);
        } else {
            try {
                return pf(jSONObject, cls.getConstructor(new Class[0]).newInstance(new Object[0]));
            } catch (NoSuchMethodException e) {
                throw pl("No default constructor for class %s", cls);
            } catch (IllegalAccessException e2) {
                throw pl("New instance failed for %s", cls);
            } catch (InstantiationException e3) {
                throw pl("New instance failed for %s", cls);
            } catch (InvocationTargetException e4) {
                throw pl("New instance failed for %s", cls);
            }
        }
    }

    private static <T> T pf(JSONObject jSONObject, T t) {
        Field[] ot = a.ot(t.getClass());
        for (Field ow : ot) {
            Field ow2 = a.ow(ow2, true);
            if (pi(ow2)) {
                Object opt = jSONObject.opt(pe(ow2));
                if (!(opt == null || JSONObject.NULL == opt)) {
                    pg(t, ow2, opt);
                }
            }
        }
        return t;
    }

    private static void pg(Object obj, Field field, Object obj2) {
        Object obj3 = null;
        try {
            obj3 = pw(field.getType(), a.ou(field), obj2);
            field.set(obj, obj3);
        } catch (RuntimeException e) {
            c.sj("PushLog2951", obj.getClass().getName() + ".fromJson runtime exception, fieldName: " + field.getName() + ", field: " + field);
        } catch (Exception e2) {
            c.sj("PushLog2951", obj.getClass().getName() + ".fromJson error, fieldName: " + field.getName() + ", field:" + field);
            pq(obj, field, obj3);
        }
    }

    private static void pq(Object obj, Field field, Object obj2) {
        if (obj2 != null && ((obj2 instanceof String) ^ 1) == 0) {
            try {
                Class type = field.getType();
                if (type.isPrimitive()) {
                    if (Integer.TYPE == type) {
                        field.set(obj, Integer.valueOf(Integer.parseInt((String) obj2)));
                    } else if (Float.TYPE == type) {
                        field.set(obj, Float.valueOf(Float.parseFloat((String) obj2)));
                    } else if (Long.TYPE == type) {
                        field.set(obj, Long.valueOf(Long.parseLong((String) obj2)));
                    } else if (Boolean.TYPE == type) {
                        field.set(obj, Boolean.valueOf(Boolean.parseBoolean((String) obj2)));
                    } else if (Double.TYPE == type) {
                        field.set(obj, Double.valueOf(Double.parseDouble((String) obj2)));
                    } else if (Short.TYPE == type) {
                        field.set(obj, Short.valueOf(Short.parseShort((String) obj2)));
                    } else if (Byte.TYPE == type) {
                        field.set(obj, Byte.valueOf(Byte.parseByte((String) obj2)));
                    } else if (Character.TYPE == type) {
                        field.set(obj, Character.valueOf(((String) obj2).charAt(0)));
                    }
                }
            } catch (Throwable th) {
                c.sf("PushLog2951", "processValueError");
            }
        }
    }

    private static boolean ph(Class cls) {
        if (cls.isPrimitive()) {
            return true;
        }
        for (Class cls2 : ey) {
            if (cls == cls2) {
                return true;
            }
        }
        return false;
    }

    private static Object pw(Class cls, Class cls2, Object obj) {
        if (ph(cls)) {
            return pt(cls, obj);
        }
        if (List.class.isAssignableFrom(cls)) {
            return pj(cls, cls2, obj);
        }
        if (Map.class.isAssignableFrom(cls)) {
            return pn(cls, cls2, obj);
        }
        if (obj instanceof JSONObject) {
            return ps((JSONObject) obj, cls, new Class[]{cls2});
        } else if (obj instanceof JSONArray) {
            return pr((JSONArray) obj, cls, new Class[]{cls2});
        } else {
            throw pl("value from json error, field class: %s", cls);
        }
    }

    private static Object pt(Class cls, Object obj) {
        if (String.class == cls) {
            return e.va(obj);
        }
        c cVar;
        if ((cls.isPrimitive() || Number.class.isAssignableFrom(cls)) && (obj instanceof Number)) {
            Number number = (Number) obj;
            cVar = (c) fa.get(cls);
            if (cVar != null) {
                return cVar.py(number);
            }
            c.sj("PushLog2951", "cannot find value reader for:" + cls);
            return null;
        } else if (cls != Boolean.class) {
            return obj;
        } else {
            cVar = (c) fa.get(cls);
            if (cVar != null) {
                return cVar.py(obj);
            }
            c.sj("PushLog2951", "cannot find value reader for:" + cls);
            return null;
        }
    }

    private static Map pn(Class cls, Class cls2, Object obj) {
        if (cls2 == null) {
            cls2 = String.class;
        }
        if (obj instanceof JSONObject) {
            Map linkedHashMap;
            if (Map.class == cls) {
                linkedHashMap = new LinkedHashMap();
            } else if (Map.class.isAssignableFrom(cls)) {
                try {
                    linkedHashMap = (Map) cls.newInstance();
                } catch (InstantiationException e) {
                    throw pl("Fail to initiate %s", cls);
                } catch (IllegalAccessException e2) {
                    throw pl("Fail to initiate %s", cls);
                }
            } else {
                throw pl("%s is not Map type", cls);
            }
            JSONObject jSONObject = (JSONObject) obj;
            Iterator keys = jSONObject.keys();
            while (keys.hasNext()) {
                String str = (String) keys.next();
                Object pw = pw(cls2, null, jSONObject.get(str));
                if (pw != null) {
                    if (cls2.isAssignableFrom(pw.getClass())) {
                        linkedHashMap.put(str, pw);
                    } else {
                        c.sf("PushLog2951", "mapFromJson error, memberClass:" + cls2 + ", valueClass:" + pw.getClass());
                    }
                }
            }
            return linkedHashMap;
        }
        throw pl("jsonValue is not JSONObject", new Object[0]);
    }

    private static List pj(Class cls, Class cls2, Object obj) {
        int i = 0;
        if (cls2 == null) {
            cls2 = String.class;
        }
        if (obj instanceof JSONArray) {
            List arrayList;
            if (cls == List.class) {
                arrayList = new ArrayList();
            } else if (List.class.isAssignableFrom(cls)) {
                try {
                    arrayList = (List) cls.newInstance();
                } catch (InstantiationException e) {
                    throw pl("Fail to initiate %s", cls);
                } catch (IllegalAccessException e2) {
                    throw pl("Fail to initiate %s", cls);
                }
            } else {
                throw pl("%s is not List type", cls);
            }
            JSONArray jSONArray = (JSONArray) obj;
            while (i < jSONArray.length()) {
                Object pw = pw(cls2, null, jSONArray.get(i));
                if (pw != null) {
                    if (cls2.isAssignableFrom(pw.getClass())) {
                        arrayList.add(pw);
                    } else {
                        c.sf("PushLog2951", "listFromJson error, memberClass:" + cls2 + ", valueClass:" + pw.getClass());
                    }
                }
                i++;
            }
            return arrayList;
        }
        throw pl("jsonobject is not JSONArray", new Object[0]);
    }

    private static <T> T pr(JSONArray jSONArray, Class<T> cls, Class[] clsArr) {
        Class cls2 = null;
        if (List.class.isAssignableFrom(cls)) {
            if (clsArr != null && clsArr.length > 0) {
                cls2 = clsArr[0];
            }
            return pj(cls, cls2, jSONArray);
        }
        throw pl("Obj class (%s) is not List type", cls);
    }

    private static JSONException pm(boolean z, String str, Object... objArr) {
        String format = String.format(Locale.ENGLISH, str, objArr);
        if (z) {
            c.sj("PushLog2951", format);
        }
        return new JSONException(format);
    }

    private static JSONException pl(String str, Object... objArr) {
        return pm(true, str, objArr);
    }

    private static String pe(Field field) {
        com.huawei.android.pushagent.datatype.a.c cVar = (com.huawei.android.pushagent.datatype.a.c) field.getAnnotation(com.huawei.android.pushagent.datatype.a.c.class);
        if (cVar != null && (TextUtils.isEmpty(cVar.vn()) ^ 1) != 0) {
            return cVar.vn();
        }
        String name = field.getName();
        if (name.endsWith("__")) {
            return name.substring(0, name.length() - "__".length());
        }
        return name;
    }

    private static boolean pi(Field field) {
        boolean z = false;
        if (field == null) {
            return false;
        }
        String name = field.getName();
        if (!(Modifier.isStatic(field.getModifiers()) || name == null || (name.contains("$") ^ 1) == 0)) {
            z = field.isAnnotationPresent(a.class) ^ 1;
        }
        return z;
    }

    public static Map<String, Object> ox(String str) {
        Map<String, Object> hashMap = new HashMap();
        if (TextUtils.isEmpty(str)) {
            return hashMap;
        }
        try {
            JSONObject jSONObject = new JSONObject(str);
            Iterator keys = jSONObject.keys();
            while (keys.hasNext()) {
                String valueOf = String.valueOf(keys.next());
                hashMap.put(valueOf, jSONObject.get(valueOf));
            }
        } catch (Throwable e) {
            c.se("PushLog2951", e.toString(), e);
        }
        return hashMap;
    }
}
