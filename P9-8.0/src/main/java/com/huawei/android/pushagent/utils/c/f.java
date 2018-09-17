package com.huawei.android.pushagent.utils.c;

import com.huawei.android.pushagent.utils.d.c;
import java.lang.reflect.InvocationTargetException;

public abstract class f implements c {
    public String getDeviceId() {
        try {
            Class cls = Class.forName("com.huawei.android.os.BuildEx");
            Object invoke = cls.getDeclaredMethod("getUDID", new Class[0]).invoke(cls, new Object[0]);
            if (invoke == null || !(invoke instanceof String)) {
                c.sf("PushLog2951", "udid is null");
                return null;
            }
            c.sh("PushLog2951", "get udid successful.");
            return (String) invoke;
        } catch (ClassNotFoundException e) {
            c.sf("PushLog2951", "not support udid class");
        } catch (NoSuchMethodException e2) {
            c.sf("PushLog2951", "not support udid method");
        } catch (SecurityException e3) {
            c.sf("PushLog2951", "not support udid method");
        } catch (IllegalAccessException e4) {
            c.sf("PushLog2951", "not support udid invoke");
        } catch (IllegalArgumentException e5) {
            c.sf("PushLog2951", "not support udid invoke");
        } catch (InvocationTargetException e6) {
            c.sf("PushLog2951", "not support udid invoke");
        }
    }
}
