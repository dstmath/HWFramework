package tmsdkobf;

import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import tmsdk.common.TMSDKContext;

public class lk {
    public static final Uri yx = Uri.parse("content://sms");
    public static final Uri yy = Uri.parse("content://mms");

    public static int a(boolean z, String str, String str2) {
        if (!z) {
            return 0;
        }
        if (VERSION.SDK_INT < 19) {
            return 0;
        }
        Context applicaionContext = TMSDKContext.getApplicaionContext();
        try {
            int i = applicaionContext.getPackageManager().getApplicationInfo(str, 1).uid;
            try {
                Object systemService = applicaionContext.getSystemService("appops");
                Class cls = Class.forName(systemService.getClass().getName());
                Field declaredField = cls.getDeclaredField("mService");
                declaredField.setAccessible(true);
                cls.getDeclaredField(str2).setAccessible(true);
                cls.getDeclaredField("MODE_ALLOWED").setAccessible(true);
                Object obj = declaredField.get(systemService);
                Method declaredMethod = Class.forName(obj.getClass().getName()).getDeclaredMethod("setMode", new Class[]{Integer.TYPE, Integer.TYPE, String.class, Integer.TYPE});
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(obj, new Object[]{Integer.valueOf(r10.getInt(null)), Integer.valueOf(i), str, Integer.valueOf(r11.getInt(null))});
                return 1;
            } catch (Throwable th) {
                return 2;
            }
        } catch (Throwable th2) {
            return 2;
        }
    }
}
