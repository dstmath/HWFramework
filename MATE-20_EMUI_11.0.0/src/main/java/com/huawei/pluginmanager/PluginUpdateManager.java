package com.huawei.pluginmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.huawei.pluginmanager.IPluginManager;
import java.util.List;

public final class PluginUpdateManager {
    private static final String ACTION_START_CONFIRMATION_DIALOG = "com.huawei.systemserver.action.PLUGIN_INSTALL_START_CONFIRMATION_DIALOG";
    public static final int DEFAULT_ERROR_STATUS = -1;
    public static final int DEFAULT_ERROR_TASKID = -1;
    public static final int DIALOG_TYPE_FORCE_UPDATE = 1;
    public static final int DIALOG_TYPE_INSTALL_NEW = 0;
    public static final int FLAG_DONT_KILL_PROCESS = 1;
    public static final int FLAG_INSTALL_TYPE_AUTO = 4;
    public static final int FLAG_NETWORK_MOBILE = 2;
    public static final int FLAG_ONLY_PERFORM_BASE_DEXOPT = 8;
    private static final String HW_SYSTEM_SERVER_PACKAGE = "com.huawei.systemserver";
    public static final String KEY_NETWORK_MODE = "network_mode";
    public static final String KEY_PACKAGE_SIZE = "package_size";
    public static final String KEY_START_CONFIRMATION_DIALOG_TYPE = "start_confirmation_dialog_type";
    public static final int STATE_DOWNLOAD_FAIL = -6;
    public static final int STATE_DOWNLOAD_START = 2;
    public static final int STATE_DOWNLOAD_SUCCESS = 4;
    public static final int STATE_FAIL = -11;
    public static final int STATE_INSTALL_START = 10;
    public static final int STATE_MERGE_DIFF_FAIL = -9;
    public static final int STATE_MERGE_DIFF_SUCCESS = 8;
    public static final int STATE_MERGE_VERIFY_FAIL = -10;
    public static final int STATE_MERGE_VERIFY_SUCCESS = 9;
    public static final int STATE_NO_NETWORK = -2;
    public static final int STATE_NO_NEW_VERSION = -4;
    public static final int STATE_PARAMETER_EXCEPTION = -1;
    public static final int STATE_REQUEST_EXCEPTION = -3;
    public static final int STATE_SERVER_CONNECT = 100;
    public static final int STATE_SERVER_DISCONNECT = -100;
    public static final int STATE_SERVER_EXCEPTION = -99;
    public static final int STATE_SPACE_NOT_ENOUGH = -5;
    public static final int STATE_SUCCESS = 0;
    public static final int STATE_UNZIP_FAIL = -8;
    public static final int STATE_UNZIP_SUCCESS = 7;
    public static final int STATE_VERIFY_FAIL = -7;
    public static final int STATE_VERIFY_SUCCESS = 5;
    private static final String TAG = "PluginUpdateManager";
    private static Context sContext;
    private static PluginUpdateManager sInstance;
    private static String sPackageName;

    private PluginUpdateManager(Context context) {
        sContext = context;
        sPackageName = context.getPackageName();
    }

    private static IPluginManager getService() {
        return IPluginManager.Stub.asInterface(ServiceManager.getService("pluginmanager"));
    }

    public static synchronized PluginUpdateManager getInstance(Context context) {
        PluginUpdateManager pluginUpdateManager;
        synchronized (PluginUpdateManager.class) {
            if (context != null) {
                if (sInstance == null) {
                    sInstance = new PluginUpdateManager(context);
                } else {
                    if (!sPackageName.equals(context.getPackageName())) {
                        throw new IllegalArgumentException("the packageName of context must be same!");
                    }
                }
                pluginUpdateManager = sInstance;
            } else {
                throw new IllegalArgumentException("context is null!");
            }
        }
        return pluginUpdateManager;
    }

    public int queryPluginBasicInfoByCategory(List<String> pluginCategory, IPluginQueryCallback queryCallback) {
        if (queryCallback == null) {
            Log.e(TAG, "queryPluginBasicInfoByCategory illegal parameter!");
            return -1;
        }
        try {
            IPluginManager service = getService();
            if (service != null) {
                return service.queryPluginBasicInfoByCategory(sPackageName, pluginCategory, queryCallback);
            }
            Log.e(TAG, "queryPluginBasicInfoByCategory pluginManager service is null.");
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "queryPluginBasicInfoByCategory RemoteException");
            return -1;
        }
    }

    public int queryPluginBasicInfoByName(List<String> pluginNames, IPluginQueryCallback queryCallback) {
        if (ArrayUtils.isEmpty(pluginNames) || queryCallback == null) {
            Log.e(TAG, "queryPluginBasicInfoByName illegal parameter!");
            return -1;
        }
        try {
            IPluginManager service = getService();
            if (service != null) {
                return service.queryPluginBasicInfoByName(sPackageName, pluginNames, queryCallback);
            }
            Log.e(TAG, "queryPluginBasicInfoByName pluginManagerService is null.");
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "queryPluginBasicInfoByName RemoteException");
            return -1;
        }
    }

    public int queryPluginDetailInfo(List<String> pluginNames, IPluginQueryDetailCallback queryCallback) {
        if (ArrayUtils.isEmpty(pluginNames) || queryCallback == null) {
            Log.e(TAG, "queryPluginDetailInfo illegal parameter!");
            return -1;
        }
        try {
            IPluginManager service = getService();
            if (service != null) {
                return service.queryPluginDetailInfo(sPackageName, pluginNames, queryCallback);
            }
            Log.e(TAG, "queryPluginDetailInfo pluginManagerService is null.");
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "queryPluginDetailInfo RemoteException");
            return -1;
        }
    }

    public boolean registerAutoUpdate(boolean isOn) {
        try {
            IPluginManager service = getService();
            if (service == null) {
                Log.e(TAG, "registerAutoUpdate pluginManagerService is null.");
                return false;
            }
            service.registerAutoUpdate(sPackageName, isOn);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "registerAutoUpdatePlugins. RemoteException");
            return false;
        }
    }

    public int startInstall(List<String> pluginNames, int flags, IPluginUpdateStateListener listener) {
        if (ArrayUtils.isEmpty(pluginNames) || listener == null) {
            Log.e(TAG, "startInstall. illegal parameter!");
            return -1;
        }
        try {
            IPluginManager service = getService();
            if (service != null) {
                return service.startInstall(sPackageName, pluginNames, flags, listener);
            }
            Log.e(TAG, "startInstall pluginManagerService is null.");
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "startInstall. RemoteException !");
            return -1;
        }
    }

    public int uninstall(List<String> pluginNames, int flags, IPluginUpdateStateListener listener) {
        if (ArrayUtils.isEmpty(pluginNames) || listener == null) {
            Log.e(TAG, "uninstall. illegal parameter!");
            return -1;
        }
        try {
            IPluginManager service = getService();
            if (service != null) {
                return service.uninstall(sPackageName, pluginNames, flags, listener);
            }
            Log.e(TAG, "uninstall pluginmanager Service is null.");
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "uninstall. RemoteException !");
            return -1;
        }
    }

    public boolean cancelInstall(int taskId) {
        if (taskId <= 0) {
            Log.e(TAG, "cancelInstall. error task id");
            return false;
        }
        try {
            IPluginManager service = getService();
            if (service == null) {
                Log.e(TAG, "cancelInstall pluginmanager Service is null.");
                return false;
            }
            service.cancelInstall(sPackageName, taskId);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "cancelInstall. RemoteException !");
            return false;
        }
    }

    public boolean startConfirmationDialogForResult(int type, int networkMode, long totalBytes, Activity activity, int requestCode) {
        if (totalBytes <= 0 || activity == null || requestCode < 0) {
            Log.e(TAG, "startConfirmationDialogForResult illegal parameter!");
            return false;
        }
        Intent intent = new Intent(ACTION_START_CONFIRMATION_DIALOG);
        intent.setPackage(HW_SYSTEM_SERVER_PACKAGE);
        Bundle options = new Bundle();
        options.putInt(KEY_START_CONFIRMATION_DIALOG_TYPE, type);
        options.putInt("network_mode", networkMode);
        options.putLong(KEY_PACKAGE_SIZE, totalBytes);
        intent.putExtras(options);
        activity.startActivityForResult(intent, requestCode);
        return true;
    }
}
