package ohos.pluginproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/* access modifiers changed from: package-private */
public abstract class ProxyGenHandler implements InvocationHandler {
    private static final String APK_CONTEXT_CLASS = "android.content.Context";
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 218108160, "ProxyGenHandler");
    private Class<?> cls = null;
    private Method[] methods = null;
    private Object target = null;

    ProxyGenHandler(Class<?> cls2, Object obj) {
        this.cls = cls2;
        this.target = obj;
        try {
            this.methods = cls2.getDeclaredMethods();
        } catch (SecurityException unused) {
            HiLog.warn(LABEL_LOG, "ProxyGenHandler getDeclaredMethods occur exception.", new Object[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public Object getTarget() {
        return this.target;
    }

    /* access modifiers changed from: package-private */
    public Method findMethod(Method method) {
        Method[] methodArr;
        if (!(method == null || (methodArr = this.methods) == null)) {
            for (Method method2 : methodArr) {
                if (compareMethod(method, method2)) {
                    return method2;
                }
            }
        }
        return null;
    }

    private boolean compareMethod(Method method, Method method2) {
        if (!method.getName().equals(method2.getName())) {
            return false;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?>[] parameterTypes2 = method2.getParameterTypes();
        if (parameterTypes.length != parameterTypes2.length) {
            HiLog.warn(LABEL_LOG, "compareMethod  param type len not equal", new Object[0]);
            return false;
        }
        int i = 0;
        while (true) {
            boolean z = true;
            if (i >= parameterTypes.length) {
                return true;
            }
            String name = parameterTypes[i].getName();
            String name2 = parameterTypes2[i].getName();
            if (!name.equals(name2)) {
                if ((!"ohos.app.AbilityContext".equals(name) && !"ohos.app.Context".equals(name)) || !APK_CONTEXT_CLASS.equals(name2)) {
                    z = false;
                }
                if (!z) {
                    return false;
                }
            }
            i++;
        }
    }
}
