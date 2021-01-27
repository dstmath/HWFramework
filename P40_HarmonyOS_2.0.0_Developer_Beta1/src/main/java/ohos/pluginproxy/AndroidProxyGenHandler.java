package ohos.pluginproxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

final class AndroidProxyGenHandler extends ProxyGenHandler {
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 218108160, "AndroidProxyGenHandler");

    AndroidProxyGenHandler(Class<?> cls, Object obj) {
        super(cls, obj);
    }

    @Override // java.lang.reflect.InvocationHandler
    public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
        HiLog.debug(LABEL_LOG, "invoke indicates from apk to harmony", new Object[0]);
        final Method findMethod = findMethod(method);
        if (findMethod == null) {
            HiLog.debug(LABEL_LOG, "invoke, but method not found.", new Object[0]);
            return null;
        }
        try {
            if (!Modifier.isPublic(findMethod.getModifiers())) {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    /* class ohos.pluginproxy.AndroidProxyGenHandler.AnonymousClass1 */

                    @Override // java.security.PrivilegedAction
                    public Object run() {
                        findMethod.setAccessible(true);
                        return null;
                    }
                });
            }
            if (Modifier.isStatic(findMethod.getModifiers())) {
                return findMethod.invoke(null, objArr);
            }
            return findMethod.invoke(getTarget(), objArr);
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException e) {
            HiLog.warn(LABEL_LOG, "invoke occur exception %{public}s.", e.getMessage());
            return null;
        }
    }
}
