package ohos.pluginproxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

final class HarmonyProxyGenHandler extends ProxyGenHandler {
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 218108160, "HarmonyProxyGenHandler");

    HarmonyProxyGenHandler(Class cls, Object obj) {
        super(cls, obj);
    }

    @Override // java.lang.reflect.InvocationHandler
    public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
        ClassLoader classLoader;
        HiLog.debug(LABEL_LOG, "invoke indicates from harmony to apk", new Object[0]);
        final Method findMethod = findMethod(method);
        if (findMethod == null) {
            HiLog.debug(LABEL_LOG, "invoke, but method is not found.", new Object[0]);
            return null;
        }
        if (objArr != null && objArr.length > 0) {
            Class<?>[] parameterTypes = findMethod.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].isInterface() && (classLoader = parameterTypes[i].getClassLoader()) != null) {
                    try {
                        objArr[i] = PluginProxyUtils.createProxyObject(Class.forName(parameterTypes[i].getName(), true, classLoader), new AndroidProxyGenHandler(objArr[i].getClass(), objArr[i]));
                    } catch (ClassNotFoundException e) {
                        HiLog.debug(LABEL_LOG, "forName occur exception:%{public}s", e.getMessage());
                    }
                }
            }
        }
        Object[] convertParameters = PluginProxyUtils.convertParameters(objArr);
        try {
            if (!Modifier.isPublic(findMethod.getModifiers())) {
                AccessController.doPrivileged(new PrivilegedAction() {
                    /* class ohos.pluginproxy.HarmonyProxyGenHandler.AnonymousClass1 */

                    @Override // java.security.PrivilegedAction
                    public Object run() {
                        findMethod.setAccessible(true);
                        return null;
                    }
                });
            }
            if (Modifier.isStatic(findMethod.getModifiers())) {
                return findMethod.invoke(null, convertParameters);
            }
            return findMethod.invoke(getTarget(), convertParameters);
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException e2) {
            HiLog.warn(LABEL_LOG, "invoke occur exception %{public}s.", e2.getMessage());
            return null;
        }
    }
}
