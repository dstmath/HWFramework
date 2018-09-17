package com.huawei.android.pushagent.utils.f;

import android.content.Context;
import android.os.Build;
import com.huawei.android.pushagent.model.flowcontrol.a.b;
import com.huawei.android.pushagent.utils.d.c;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

public class a {
    private static void tg(Context context, int i, HashMap<Short, Object> hashMap) {
        if (context == null || hashMap == null) {
            c.sh("PushLog2951", "startMonitor, map is null");
            return;
        }
        c.sh("PushLog2951", "startMonitor, eventId is " + i);
        String rt = new com.huawei.android.pushagent.utils.d.a(context, "PushMonitor").rt(String.valueOf(i));
        b bVar = new b(86400000, 2);
        bVar.lj(rt);
        if (bVar.lg(1)) {
            c.sh("PushLog2951", "begin to startMonitor");
            new Thread(new b(context, bVar, i, hashMap)).start();
            return;
        }
        c.sh("PushLog2951", "can't report too many times");
    }

    public static void te(Context context) {
        HashMap hashMap = new HashMap();
        hashMap.put(Short.valueOf((short) 0), String.valueOf(2951));
        hashMap.put(Short.valueOf((short) 1), Build.MODEL);
        tg(context, 907124001, hashMap);
    }

    public static void tf(Context context, String str) {
        HashMap hashMap = new HashMap();
        hashMap.put(Short.valueOf((short) 0), String.valueOf(2951));
        hashMap.put(Short.valueOf((short) 1), str);
        tg(context, 907124002, hashMap);
    }

    private static void td(Context context, b bVar, int i, HashMap<Short, Object> hashMap) {
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
                bVar.lh(1);
                new com.huawei.android.pushagent.utils.d.a(context, "PushMonitor").rv(String.valueOf(i), bVar.li());
            }
        } catch (ClassNotFoundException e) {
            c.sf("PushLog2951", " ClassNotFoundException startMonitor " + e.toString());
        } catch (NoSuchMethodException e2) {
            c.sf("PushLog2951", " NoSuchMethodException startMonitor " + e2.toString());
        } catch (IllegalArgumentException e3) {
            c.sf("PushLog2951", " IllegalArgumentException startMonitor " + e3.toString());
        } catch (IllegalAccessException e4) {
            c.sf("PushLog2951", " IllegalAccessException startMonitor " + e4.toString());
        } catch (Exception e5) {
            c.sf("PushLog2951", " Exception startMonitor " + e5.toString());
        }
    }
}
