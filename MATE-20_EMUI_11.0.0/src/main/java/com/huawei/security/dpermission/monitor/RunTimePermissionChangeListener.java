package com.huawei.security.dpermission.monitor;

import android.content.Context;
import android.content.pm.PackageManager;
import com.huawei.security.dpermission.DPermissionInitializer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class RunTimePermissionChangeListener {
    private static final String ADD_PERM_LISTENER = "addOnPermissionsChangeListener";
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "RunTimePermissionChangeListener");
    private static final Object INSTANCE_LOCK = new Object();
    private static final String ON_PERM_CHANGED_LISTENER = "android.content.pm.PackageManager$OnPermissionsChangedListener";
    private static volatile RunTimePermissionChangeListener sInstance;
    private Context mContext;
    private Object mRuntimePermissionChangedListener;

    private RunTimePermissionChangeListener(Context context) {
        this.mContext = context;
    }

    public static RunTimePermissionChangeListener getInstance(Context context) {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new RunTimePermissionChangeListener(context);
                }
            }
        }
        return sInstance;
    }

    public void register() {
        Context context = this.mContext;
        PackageManager packageManager = context != null ? context.getPackageManager() : null;
        if (packageManager == null) {
            HiLog.error(DPERMISSION_LABEL, "register get PMG failed!", new Object[0]);
            return;
        }
        try {
            Class<?> cls = Class.forName(ON_PERM_CHANGED_LISTENER);
            Method declaredMethod = packageManager.getClass().getDeclaredMethod(ADD_PERM_LISTENER, cls);
            this.mRuntimePermissionChangedListener = RunTimePermissionChangeProxy.newInstance(new Class[]{cls});
            HiLog.info(DPERMISSION_LABEL, "register mRuntimePermissionChangedListener is %{public}s", new Object[]{this.mRuntimePermissionChangedListener});
            declaredMethod.invoke(packageManager, this.mRuntimePermissionChangedListener);
        } catch (ClassNotFoundException unused) {
            HiLog.error(DPERMISSION_LABEL, "register()# class not found.", new Object[0]);
        } catch (NoSuchMethodException unused2) {
            HiLog.error(DPERMISSION_LABEL, "register()# no such method.", new Object[0]);
        } catch (IllegalAccessException unused3) {
            HiLog.error(DPERMISSION_LABEL, "register()# illegal access.", new Object[0]);
        } catch (InvocationTargetException unused4) {
            HiLog.error(DPERMISSION_LABEL, "register()# invocation target exception.", new Object[0]);
        }
    }
}
