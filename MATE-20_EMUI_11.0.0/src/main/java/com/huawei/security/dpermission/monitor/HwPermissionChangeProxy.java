package com.huawei.security.dpermission.monitor;

import android.content.Context;
import android.content.pm.PackageManager;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.security.dpermission.DPermissionInitializer;
import com.huawei.security.dpermission.DPermissionUtils;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.security.dpermissionkit.DPermissionKit;

public class HwPermissionChangeProxy implements InvocationHandler {
    private static final String CONVERT_CALLER_ERROR = "convert caller error";
    private static final int CONVERT_OPERATION_ERROR = -1;
    private static final long CONVERT_PERM_TYPE_ERROR = -1;
    private static final String CONVERT_PKGNAME_ERROR = "convert pkgName error";
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "HwPermissionChangeProxy");
    private static final int INDEX_CALLER = 0;
    private static final int INDEX_OPERATION = 3;
    private static final int INDEX_PERMTYPE = 2;
    private static final int INDEX_PKGNAME = 1;
    private static final String METHOD_EQUALS = "equals";
    private static final String METHOD_HASH_CODE = "hashCode";
    private static final String METHOD_ON_PERMISSION_CHANGED = "onPermissionChanged";
    private static final String METHOD_TO_STRING = "toString";
    private static Context sContext;

    public static Object newInstance(Class<?>[] clsArr, Context context) {
        sContext = context;
        return Proxy.newProxyInstance(HwPermissionChangeProxy.class.getClassLoader(), clsArr, new HwPermissionChangeProxy());
    }

    @Override // java.lang.reflect.InvocationHandler
    public Object invoke(Object obj, Method method, Object[] objArr) {
        boolean z = false;
        if (method == null) {
            HiLog.error(DPERMISSION_LABEL, "invoke methodName is null!", new Object[0]);
            return null;
        }
        String name = method.getName();
        if (METHOD_ON_PERMISSION_CHANGED.equals(name)) {
            String convertToString = convertToString(getArgsElement(objArr, 0), CONVERT_CALLER_ERROR);
            String convertToString2 = convertToString(getArgsElement(objArr, 1), CONVERT_PKGNAME_ERROR);
            long convertToLong = convertToLong(getArgsElement(objArr, 2), -1);
            int convertToInt = convertToInt(getArgsElement(objArr, 3), -1);
            if (isNotSyncPermissionType(convertToLong)) {
                HiLog.debug(DPERMISSION_LABEL, "permType: %{public}d not need notify change", new Object[]{Long.valueOf(convertToLong)});
                return null;
            }
            HiLog.debug(DPERMISSION_LABEL, "caller: %{public}s, pkgName: %{public}s, permType: %{public}d, operation: %{public}d", new Object[]{convertToString, convertToString2, Long.valueOf(convertToLong), Integer.valueOf(convertToInt)});
            notifyPermissionChange(convertToString2);
        }
        if (METHOD_HASH_CODE.equals(name)) {
            return Integer.valueOf(hashCode());
        }
        if (METHOD_TO_STRING.equals(name)) {
            return toString();
        }
        if (!METHOD_EQUALS.equals(name)) {
            return null;
        }
        if (getArgsElement(objArr, 1) == null) {
            return false;
        }
        if (hashCode() == getArgsElement(objArr, 1).hashCode()) {
            z = true;
        }
        return Boolean.valueOf(z);
    }

    private boolean isNotSyncPermissionType(long j) {
        return DPermissionUtils.getNotSyncPermissionTypeSet().contains(Long.valueOf(j));
    }

    private Object getArgsElement(Object[] objArr, int i) {
        if (objArr == null || i < 0 || i >= objArr.length) {
            return null;
        }
        return objArr[i];
    }

    private void notifyPermissionChange(String str) {
        Context context = sContext;
        PackageManager packageManager = context != null ? context.getPackageManager() : null;
        if (packageManager == null) {
            HiLog.error(DPERMISSION_LABEL, "get packageManager failed", new Object[0]);
            return;
        }
        try {
            DPermissionKit.getInstance().notifyUidPermissionChanged(packageManager.getPackageUidAsUser(str, ActivityManagerEx.getCurrentUser()));
        } catch (PackageManager.NameNotFoundException unused) {
            HiLog.error(DPERMISSION_LABEL, "invoke PackageManager.NameNotFoundException: %{public}s", new Object[]{str});
        }
    }

    public static String convertToString(Object obj, String str) {
        return obj instanceof String ? (String) obj : str;
    }

    public static long convertToLong(Object obj, long j) {
        return obj instanceof Long ? ((Long) obj).longValue() : j;
    }

    public static int convertToInt(Object obj, int i) {
        return obj instanceof Integer ? ((Integer) obj).intValue() : i;
    }
}
