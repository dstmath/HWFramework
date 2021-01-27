package com.huawei.android.appassistant;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import com.huawei.android.appassistant.IAppAssistantService;
import java.util.ArrayList;
import java.util.List;

public class HwAppAssistantManager {
    public static final String ASSISTANT_SERVICE = "appassistant";
    private static final Object INSTANCE_SYNC = new Object();
    private static final String TAG = "HwAppAssistantManager";
    private static IAppAssistantService sService;

    private HwAppAssistantManager() {
    }

    private static IAppAssistantService getService() {
        synchronized (INSTANCE_SYNC) {
            if (sService != null) {
                return sService;
            }
            sService = IAppAssistantService.Stub.asInterface(ServiceManager.getService(ASSISTANT_SERVICE));
            return sService;
        }
    }

    public static boolean addAssistantList(List<String> packageName, String func) {
        IAppAssistantService service = getService();
        if (service != null) {
            try {
                return service.addAssistantList(packageName, func);
            } catch (RemoteException e) {
                Slog.e(TAG, "addAssistantList Exception!");
                return false;
            }
        } else {
            Slog.e(TAG, "addAssistantList service is null!");
            return false;
        }
    }

    public static boolean delAssistantList(List<String> packageName, String func) {
        IAppAssistantService service = getService();
        if (service != null) {
            try {
                return service.delAssistantList(packageName, func);
            } catch (RemoteException e) {
                Slog.e(TAG, "delAssistantList Exception!");
                return false;
            }
        } else {
            Slog.e(TAG, "delAssistantList service is null!");
            return false;
        }
    }

    public static boolean isAssistantForeground(String func) {
        IAppAssistantService service = getService();
        if (service != null) {
            try {
                return service.isAssistantForeground(func);
            } catch (RemoteException e) {
                Slog.e(TAG, "isAssistantForeground Exception!");
                return false;
            }
        } else {
            Slog.e(TAG, "isAssistantForeground service is null!");
            return false;
        }
    }

    public static boolean isInAssistantList(String packageName, String func) {
        IAppAssistantService service = getService();
        if (service != null) {
            try {
                return service.isInAssistantList(packageName, func);
            } catch (RemoteException e) {
                Slog.e(TAG, "isInAssistantList Exception!");
                return false;
            }
        } else {
            Slog.e(TAG, "isInAssistantList service is null!");
            return false;
        }
    }

    public static List<String> getAssistantList(String func) {
        IAppAssistantService service = getService();
        if (service != null) {
            try {
                return service.getAssistantList(func);
            } catch (RemoteException e) {
                Slog.e(TAG, "getAssistantList Exception!");
            }
        } else {
            Slog.e(TAG, "getAssistantList service is null!");
            return new ArrayList();
        }
    }

    public static boolean isHasAssistantFunc(String func) {
        IAppAssistantService service = getService();
        if (service != null) {
            try {
                return service.isHasAssistantFunc(func);
            } catch (RemoteException e) {
                Slog.e(TAG, "isHasAssistantFunc Exception!");
                return false;
            }
        } else {
            Slog.e(TAG, "isHasAssistantFunc service is null!");
            return false;
        }
    }
}
