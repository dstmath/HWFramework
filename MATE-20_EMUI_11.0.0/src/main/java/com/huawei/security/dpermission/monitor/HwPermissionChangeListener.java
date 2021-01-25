package com.huawei.security.dpermission.monitor;

import android.content.Context;
import com.huawei.security.dpermission.DPermissionInitializer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class HwPermissionChangeListener {
    private static final String ADD_PERM_LISTENER = "addOnPermissionsChangeListener";
    private static final String CHANGE_LISTENER_CLASS_NAME = "com.huawei.securitycenter.HwPermissionManager$OnHwPermissionChangeListener";
    private static final HiLogLabel DPERMISSION_LABEL = new HiLogLabel(3, (int) DPermissionInitializer.DPERMISSION_LOG_ID, "HwPermissionChangeListener");
    private static final String GET_INSTANCE = "getInstance";
    private static final String HW_PERM_MANAGER = "com.huawei.securitycenter.HwPermissionManager";
    private static final Object INSTANCE_LOCK = new Object();
    private static final String ON_PERM_LISTENER = "$OnHwPermissionChangeListener";
    private static volatile HwPermissionChangeListener sInstance;
    private Context mContext;
    private Object mHwPermissionChangeListener;

    private HwPermissionChangeListener(Context context) {
        this.mContext = context;
    }

    public static HwPermissionChangeListener getInstance(Context context) {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new HwPermissionChangeListener(context);
                }
            }
        }
        return sInstance;
    }

    public void register() {
        try {
            Class<?> cls = Class.forName(HW_PERM_MANAGER);
            Object invoke = cls.getDeclaredMethod(GET_INSTANCE, new Class[0]).invoke(null, new Object[0]);
            Class<?> cls2 = Class.forName(CHANGE_LISTENER_CLASS_NAME);
            Method declaredMethod = cls.getDeclaredMethod(ADD_PERM_LISTENER, cls2);
            this.mHwPermissionChangeListener = HwPermissionChangeProxy.newInstance(new Class[]{cls2}, this.mContext);
            HiLog.info(DPERMISSION_LABEL, "register mHwPermissionChangeListener is %{public}s", new Object[]{this.mHwPermissionChangeListener});
            declaredMethod.invoke(invoke, this.mHwPermissionChangeListener);
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
