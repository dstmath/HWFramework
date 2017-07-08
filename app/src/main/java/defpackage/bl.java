package defpackage;

import android.content.Context;
import android.os.Build;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

/* renamed from: bl */
public class bl {
    private static void a(Context context, int i, HashMap hashMap) {
        if (context == null || hashMap == null) {
            aw.i("PushLog2828", "startMonitor, map is null");
            return;
        }
        aw.i("PushLog2828", "startMonitor, eventId is " + i);
        String string = new bt(context, "PushMonitor").getString(String.valueOf(i));
        al alVar = new al(86400000, 2);
        alVar.i(string);
        if (alVar.k(1)) {
            aw.i("PushLog2828", "begin to startMonitor");
            new Thread(new bm(context, alVar, i, hashMap)).start();
            return;
        }
        aw.i("PushLog2828", "can't report too many times");
    }

    private static void a(Context context, al alVar, int i, HashMap hashMap) {
        try {
            Class cls = Class.forName("android.util.IMonitor");
            Class cls2 = Class.forName("android.util.IMonitor$EventStream");
            Method declaredMethod = cls.getDeclaredMethod("openEventStream", new Class[]{Integer.TYPE});
            Method declaredMethod2 = cls.getDeclaredMethod("closeEventStream", new Class[]{cls2});
            Method declaredMethod3 = cls.getDeclaredMethod("sendEvent", new Class[]{cls2});
            Object invoke = declaredMethod.invoke(cls, new Object[]{Integer.valueOf(i)});
            if (invoke != null) {
                for (Entry entry : hashMap.entrySet()) {
                    short shortValue = ((Short) entry.getKey()).shortValue();
                    Object value = entry.getValue();
                    cls2.getDeclaredMethod("setParam", new Class[]{Short.TYPE, value.getClass()}).invoke(invoke, new Object[]{Short.valueOf(shortValue), value});
                }
                declaredMethod3.invoke(cls, new Object[]{invoke});
                declaredMethod2.invoke(cls, new Object[]{invoke});
                alVar.l(1);
                new bt(context, "PushMonitor").f(String.valueOf(i), alVar.bG());
            }
        } catch (ClassNotFoundException e) {
            aw.e("PushLog2828", " ClassNotFoundException startMonitor " + e.toString());
        } catch (NoSuchMethodException e2) {
            aw.e("PushLog2828", " NoSuchMethodException startMonitor " + e2.toString());
        } catch (IllegalArgumentException e3) {
            aw.e("PushLog2828", " IllegalArgumentException startMonitor " + e3.toString());
        } catch (IllegalAccessException e4) {
            aw.e("PushLog2828", " IllegalAccessException startMonitor " + e4.toString());
        } catch (Exception e5) {
            aw.e("PushLog2828", " Exception startMonitor " + e5.toString());
        }
    }

    public static void ad(Context context) {
        HashMap hashMap = new HashMap();
        hashMap.put(Short.valueOf((short) 0), String.valueOf(2828));
        hashMap.put(Short.valueOf((short) 1), Build.MODEL);
        bl.a(context, 907124001, hashMap);
    }

    public static void v(Context context, String str) {
        HashMap hashMap = new HashMap();
        hashMap.put(Short.valueOf((short) 0), String.valueOf(2828));
        hashMap.put(Short.valueOf((short) 1), str);
        bl.a(context, 907124002, hashMap);
    }
}
