package tmsdkobf;

import android.os.IBinder;
import android.os.IInterface;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public final class mi {
    private static IBinder mRemote;
    private static HashMap<String, IBinder> sCache;
    private static Class<?> zL;
    private static Method zM;
    private static Method zN;
    private static Method zO;
    private static Method zP;

    static {
        try {
            zL = Class.forName("android.os.ServiceManager");
            zM = zL.getDeclaredMethod("getService", new Class[]{String.class});
            zN = zL.getDeclaredMethod("addService", new Class[]{String.class, IBinder.class});
            zO = zL.getDeclaredMethod("checkService", new Class[]{String.class});
            zP = zL.getDeclaredMethod("listServices", new Class[0]);
            Field declaredField = zL.getDeclaredField("sCache");
            declaredField.setAccessible(true);
            sCache = (HashMap) declaredField.get(null);
            Field declaredField2 = zL.getDeclaredField("sServiceManager");
            declaredField2.setAccessible(true);
            mRemote = ((IInterface) declaredField2.get(null)).asBinder();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        } catch (NoSuchFieldException e4) {
            e4.printStackTrace();
        } catch (IllegalArgumentException e5) {
            e5.printStackTrace();
        } catch (IllegalAccessException e6) {
            e6.printStackTrace();
        }
    }

    private static Object a(Method method, Object... objArr) {
        Object obj = null;
        try {
            return method.invoke(null, objArr);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return obj;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return obj;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return obj;
        }
    }

    public static IBinder checkService(String str) {
        return (IBinder) a(zO, str);
    }

    public static IBinder getService(String str) {
        return (IBinder) a(zM, str);
    }
}
