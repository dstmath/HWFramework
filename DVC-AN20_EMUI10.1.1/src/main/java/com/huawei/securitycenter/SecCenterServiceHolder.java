package com.huawei.securitycenter;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.securitycenter.IHwSecService;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.ISecurityCenterManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SecCenterServiceHolder {
    private static final int DEATH_BINDER_FLAG = 0;
    private static final int HW_SECCENTER_MANAGER_PLUGIN_ID = 18;
    private static final int LISTENER_INIT_SIZE = 2;
    private static final Object MANAGER_LOCK = new Object();
    private static final String SECURITY_CENTER_SERVICE = "com.huawei.securitycenter.mainservice.HwSecService";
    private static final String SECURITY_SERVICE = "securityserver";
    private static final HashMap<String, List<ServiceDieListener>> SERVICE_DIE_LISTENERS = new HashMap<>(2);
    private static final Object SERVICE_LOCK = new Object();
    public static final String SERVICE_TYPE_HW_SEC = "hwsecservice";
    public static final String SERVICE_TYPE_SECURITY_MANAGER = "securitymanager";
    private static final String TAG = "SecCenterServiceHolder";
    private static volatile IHwSecService sHwSecService;
    private static IBinder.DeathRecipient sSecManagerDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.huawei.securitycenter.SecCenterServiceHolder.AnonymousClass2 */

        public void binderDied() {
            Log.w(SecCenterServiceHolder.TAG, "SecurityCenterManager service Die");
            ISecurityCenterManager unused = SecCenterServiceHolder.sSecurityCenterManager = null;
            SecCenterServiceHolder.notifyBinderDie(SecCenterServiceHolder.SERVICE_TYPE_SECURITY_MANAGER);
        }
    };
    private static IBinder.DeathRecipient sSecServiceDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.huawei.securitycenter.SecCenterServiceHolder.AnonymousClass1 */

        public void binderDied() {
            Log.w(SecCenterServiceHolder.TAG, "HwSecService service Die");
            IHwSecService unused = SecCenterServiceHolder.sHwSecService = null;
            SecCenterServiceHolder.notifyBinderDie(SecCenterServiceHolder.SERVICE_TYPE_HW_SEC);
        }
    };
    private static volatile ISecurityCenterManager sSecurityCenterManager;

    public interface ServiceDieListener {
        void notifyServiceDie();
    }

    private SecCenterServiceHolder() {
    }

    public static void addServiceDieListener(ServiceDieListener listener, String serviceType) {
        if (listener == null) {
            Log.w(TAG, "add ServiceDieListener error:listener = null");
        } else if (!checkServiceTypeValid(serviceType)) {
            Log.w(TAG, "add ServiceDieListener error:service type is not valid!");
        } else {
            synchronized (SERVICE_DIE_LISTENERS) {
                List<ServiceDieListener> listeners = SERVICE_DIE_LISTENERS.get(serviceType);
                if (listeners == null) {
                    listeners = new ArrayList(2);
                }
                listeners.add(listener);
                SERVICE_DIE_LISTENERS.put(serviceType, listeners);
            }
        }
    }

    public static ISecurityCenterManager getSecurityCenterManager() {
        if (sSecurityCenterManager == null) {
            synchronized (MANAGER_LOCK) {
                if (sSecurityCenterManager != null) {
                    return sSecurityCenterManager;
                }
                IHwSecurityService securityService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));
                if (securityService == null) {
                    return null;
                }
                try {
                    IBinder plugin = securityService.querySecurityInterface(18);
                    sSecurityCenterManager = ISecurityCenterManager.Stub.asInterface(plugin);
                    linkToDeath(plugin, sSecManagerDeathRecipient);
                } catch (RemoteException e) {
                    Log.e(TAG, "getSecurityCenterManager failed!");
                    return null;
                }
            }
        }
        return sSecurityCenterManager;
    }

    public static IHwSecService getHwSecService() {
        if (sHwSecService == null) {
            synchronized (SERVICE_LOCK) {
                if (sHwSecService != null) {
                    return sHwSecService;
                }
                IBinder binder = ServiceManagerEx.getService(SECURITY_CENTER_SERVICE);
                sHwSecService = IHwSecService.Stub.asInterface(binder);
                try {
                    linkToDeath(binder, sSecServiceDeathRecipient);
                } catch (RemoteException e) {
                    Log.e(TAG, "getSecurityCenterManager failed!");
                    return null;
                }
            }
        }
        return sHwSecService;
    }

    private static void linkToDeath(IBinder binder, IBinder.DeathRecipient recipient) throws RemoteException {
        if (binder != null && recipient != null) {
            binder.linkToDeath(recipient, 0);
        }
    }

    /* access modifiers changed from: private */
    public static void notifyBinderDie(String serviceType) {
        List<ServiceDieListener> serviceDieListeners = new ArrayList<>(2);
        synchronized (SERVICE_DIE_LISTENERS) {
            serviceDieListeners.addAll(SERVICE_DIE_LISTENERS.get(serviceType));
        }
        for (ServiceDieListener listener : serviceDieListeners) {
            listener.notifyServiceDie();
        }
    }

    private static boolean checkServiceTypeValid(String serviceType) {
        return SERVICE_TYPE_HW_SEC.equals(serviceType) || SERVICE_TYPE_SECURITY_MANAGER.equals(serviceType);
    }
}
