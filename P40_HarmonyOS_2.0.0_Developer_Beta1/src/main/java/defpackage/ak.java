package defpackage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/* renamed from: ak  reason: default package */
public final class ak {
    SharedPreferences T;

    public ak(Context context, String str) {
        if (context == null) {
            throw new NullPointerException("context is null!");
        }
        this.T = c("/data/misc/hwpush", str);
    }

    private static SharedPreferences c(String str, String str2) {
        File file = new File(str, str2 + ".xml");
        try {
            Constructor<?> declaredConstructor = Class.forName("android.app.SharedPreferencesImpl").getDeclaredConstructor(File.class, Integer.TYPE);
            declaredConstructor.setAccessible(true);
            return (SharedPreferences) declaredConstructor.newInstance(file, 0);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Log.e("PushLogSys", "get SharedPreferences error: " + ao.a(e));
            return null;
        }
    }
}
