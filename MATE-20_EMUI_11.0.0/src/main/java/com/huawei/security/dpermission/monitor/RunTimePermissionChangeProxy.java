package com.huawei.security.dpermission.monitor;

import com.huawei.android.app.ActivityManagerEx;
import com.huawei.security.dpermission.DPermissionInitializer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.security.dpermissionkit.DPermissionKit;

public class RunTimePermissionChangeProxy implements InvocationHandler {
    private static final int CONVERT_UID_ERROR = -1;
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "RunTimePermissionChangeProxy");
    private static final String METHOD_EQUALS = "equals";
    private static final String METHOD_HASH_CODE = "hashCode";
    private static final String METHOD_ON_PERMISSION_CHANGED = "onPermissionsChanged";
    private static final String METHOD_TO_STRING = "toString";

    public static Object newInstance(Class<?>[] clsArr) {
        return Proxy.newProxyInstance(RunTimePermissionChangeProxy.class.getClassLoader(), clsArr, new RunTimePermissionChangeProxy());
    }

    @Override // java.lang.reflect.InvocationHandler
    public Object invoke(Object obj, Method method, Object[] objArr) {
        boolean z = false;
        if (method == null) {
            HiLog.error(DPERMISSION_LABEL, "invoke methodName is null!", new Object[0]);
            return null;
        }
        String name = method.getName();
        boolean equals = METHOD_ON_PERMISSION_CHANGED.equals(name);
        boolean z2 = (objArr == null || objArr[0] == null) ? false : true;
        if (equals && z2) {
            int currentUser = (ActivityManagerEx.getCurrentUser() * 100000) + (HwPermissionChangeProxy.convertToInt(objArr[0], -1) % 100000);
            HiLog.debug(DPERMISSION_LABEL, "Runtime Permission Changed uid -> %{public}d", new Object[]{Integer.valueOf(currentUser)});
            DPermissionKit.getInstance().notifyUidPermissionChanged(currentUser);
            return null;
        } else if (METHOD_HASH_CODE.equals(name)) {
            return Integer.valueOf(hashCode());
        } else {
            if (METHOD_TO_STRING.equals(name)) {
                return toString();
            }
            if (!METHOD_EQUALS.equals(name)) {
                return null;
            }
            if (!z2) {
                return false;
            }
            if (hashCode() == objArr[0].hashCode()) {
                z = true;
            }
            return Boolean.valueOf(z);
        }
    }
}
