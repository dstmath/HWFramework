package defpackage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/* renamed from: bc  reason: default package */
public final class bc {
    SharedPreferences ap;

    public bc(Context context, String str) {
        if (context == null) {
            throw new NullPointerException("context is null!");
        }
        this.ap = a("/data/misc/hwpush", str);
    }

    private static SharedPreferences a(String str, String str2) {
        File file = new File(str, str2 + ".xml");
        try {
            Constructor<?> declaredConstructor = Class.forName("android.app.SharedPreferencesImpl").getDeclaredConstructor(new Class[]{File.class, Integer.TYPE});
            declaredConstructor.setAccessible(true);
            return (SharedPreferences) declaredConstructor.newInstance(new Object[]{file, 0});
        } catch (ClassNotFoundException e) {
            Log.e("PushLogSys", e.toString());
            return null;
        } catch (NoSuchMethodException e2) {
            Log.e("PushLogSys", e2.toString());
            return null;
        } catch (InstantiationException e3) {
            Log.e("PushLogSys", e3.toString());
            return null;
        } catch (IllegalAccessException e4) {
            Log.e("PushLogSys", e4.toString());
            return null;
        } catch (IllegalArgumentException e5) {
            Log.e("PushLogSys", e5.toString());
            return null;
        } catch (InvocationTargetException e6) {
            Log.e("PushLogSys", e6.toString());
            return null;
        }
    }
}
