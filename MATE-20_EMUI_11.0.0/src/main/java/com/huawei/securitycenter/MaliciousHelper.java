package com.huawei.securitycenter;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.permission.IHsmMaliAppInfoListener;
import com.huawei.permission.MaliInfoBean;
import java.util.ArrayList;
import java.util.List;

public class MaliciousHelper {
    private static final String BEANS = "beans";
    private static final String CLEAR_BEFORE = "clearBefore";
    private static final String DATA = "data";
    private static final String FLAGS = "flags";
    private static final String GET_ALWAYS_FORBIDDEN_PERMS_METHOD = "getAlwaysForbiddenPerms";
    private static final String HASH_CODE = "hashCode";
    private static final String IS_RESTRICTED = "isRestricted";
    private static final String LISTENER = "listener";
    private static final String MALICIOUS_APP = "maliciousApp";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PRIORITY = "priority";
    private static final String QUERY_ALL_MALI_PKGS_METHOD = "queryAllMaliPkgs";
    private static final String QUERY_MALI_APP_INFO_BY_PACKAGE_METHOD = "queryMaliAppInfoByPkg";
    private static final String QUERY_MALI_APP_INFO_SHORT_METHOD = "queryMaliAppInfoShort";
    private static final String REGIST_MALI_APP_INFO_LISTENER_METHOD = "registMaliAppInfoListener";
    private static final String REPORT_MALI_INFO_BEAN_METHOD = "reportMaliInfoBean";
    private static final String SET_RESTRICT_STATUS_METHOD = "setRestrictStatus";
    private static final String TAG = "MaliciousHelper";
    private static final String UNREGIST_MALI_APP_INFO_LISTENER_METHOD = "unregistMaliAppInfoListener";

    private MaliciousHelper() {
        Log.d(TAG, "MaliciousHelper create");
    }

    @NonNull
    public static List<String> queryAllMaliPkgs() {
        IHwSecService service = SecCenterServiceHolder.getHwSecService();
        if (service != null) {
            try {
                Bundle resultBundle = service.call(QUERY_ALL_MALI_PKGS_METHOD, (Bundle) null);
                if (resultBundle == null) {
                    return new ArrayList(0);
                }
                List<String> list = resultBundle.getStringArrayList("data");
                if (list != null) {
                    return list;
                }
            } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "queryAllMaliPkgs : " + e.getMessage());
            }
        }
        return new ArrayList(0);
    }

    @NonNull
    public static List<MaliInfoBean> queryMaliAppInfoByPkg(String packageName, int flags) {
        IHwSecService service = SecCenterServiceHolder.getHwSecService();
        if (service == null) {
            return new ArrayList(0);
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putString("packageName", packageName);
            bundle.putInt(FLAGS, flags);
            Bundle resultBundle = service.call(QUERY_MALI_APP_INFO_BY_PACKAGE_METHOD, bundle);
            if (resultBundle == null) {
                return new ArrayList(0);
            }
            List<MaliInfoBean> list = resultBundle.getParcelableArrayList("data");
            if (list != null) {
                return list;
            }
            return new ArrayList(0);
        } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "queryMaliAppInfoByPkg : " + e.getMessage());
        }
    }

    @NonNull
    public static List<MaliInfoBean> queryMaliAppInfoShort(int flags) {
        IHwSecService service = SecCenterServiceHolder.getHwSecService();
        if (service == null) {
            return new ArrayList(0);
        }
        try {
            Bundle bundle = new Bundle();
            bundle.putInt(FLAGS, flags);
            Bundle resultBundle = service.call(QUERY_MALI_APP_INFO_SHORT_METHOD, bundle);
            if (resultBundle == null) {
                return new ArrayList(0);
            }
            List<MaliInfoBean> list = resultBundle.getParcelableArrayList("data");
            if (list != null) {
                return list;
            }
            return new ArrayList(0);
        } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "queryMaliAppInfoShort : " + e.getMessage());
        }
    }

    public static final void registMaliAppInfoListener(IHsmMaliAppInfoListener listener, int flags, int priority) {
        IHwSecService service = SecCenterServiceHolder.getHwSecService();
        if (service != null) {
            try {
                Bundle bundle = new Bundle();
                bundle.putBinder(LISTENER, listener.asBinder());
                bundle.putInt(FLAGS, flags);
                bundle.putInt(HASH_CODE, listener.hashCode());
                bundle.putInt(PRIORITY, priority);
                service.call(REGIST_MALI_APP_INFO_LISTENER_METHOD, bundle);
            } catch (RemoteException e) {
                Log.e(TAG, "registMaliAppInfoListener : " + e.getMessage());
            }
        }
    }

    public static void unregistMaliAppInfoListener(IHsmMaliAppInfoListener listener) {
        IHwSecService service = SecCenterServiceHolder.getHwSecService();
        if (service != null) {
            try {
                Bundle bundle = new Bundle();
                bundle.putInt(HASH_CODE, listener.hashCode());
                service.call(UNREGIST_MALI_APP_INFO_LISTENER_METHOD, bundle);
            } catch (RemoteException e) {
                Log.e(TAG, "unregistMaliAppInfoListener : " + e.getMessage());
            }
        }
    }

    public static void setRestrictStatus(String maliciousApp, boolean isRestricted) {
        IHwSecService service = SecCenterServiceHolder.getHwSecService();
        if (service != null) {
            try {
                Bundle bundle = new Bundle();
                bundle.putString(MALICIOUS_APP, maliciousApp);
                bundle.putBoolean(IS_RESTRICTED, isRestricted);
                service.call(SET_RESTRICT_STATUS_METHOD, bundle);
            } catch (RemoteException e) {
                Log.e(TAG, "setRestrictStatus : " + e.getMessage());
            }
        }
    }

    @NonNull
    public static String[] getAlwaysForbiddenPerms() {
        IHwSecService service = SecCenterServiceHolder.getHwSecService();
        if (service == null) {
            return new String[0];
        }
        try {
            Bundle resultBundle = service.call(GET_ALWAYS_FORBIDDEN_PERMS_METHOD, (Bundle) null);
            if (resultBundle == null) {
                return new String[0];
            }
            String[] list = resultBundle.getStringArray("data");
            if (list != null) {
                return list;
            }
            return new String[0];
        } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "queryMaliAppInfoByPkg : " + e.getMessage());
        }
    }
}
