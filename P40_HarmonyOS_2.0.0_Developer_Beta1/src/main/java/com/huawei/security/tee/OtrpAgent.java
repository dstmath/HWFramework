package com.huawei.security.tee;

import android.app.Application;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.hwpanpayservice.IHwTEEService;
import java.util.Optional;

public class OtrpAgent {
    private static final int INVALID_RESULT = -1;
    private static final Object LOCK = new Object();
    private static final int SUCCESS = 0;
    private static final String TAG = "OtrpAgentManager";
    private static IHwTEEService sHwOtrpPlugin = null;

    private OtrpAgent() {
    }

    private static IHwTEEService getOtrpPlugin() {
        synchronized (LOCK) {
            Log.i(TAG, "Get sHwOtrpPlugin start.");
            Application application = ActivityThreadEx.currentApplication();
            if (application == null) {
                sHwOtrpPlugin = null;
                Log.e(TAG, "Application is null.");
                return sHwOtrpPlugin;
            }
            Optional<IHwTEEService> remoteService = OtrpConnectManager.getRemoteService(application.getApplicationContext());
            if (!remoteService.isPresent()) {
                Log.e(TAG, "Error, IHwOtrpPlugin is null.");
            } else {
                sHwOtrpPlugin = remoteService.get();
            }
            Log.i(TAG, "Get remote service success.");
            return sHwOtrpPlugin;
        }
    }

    public static synchronized String processOtrpMsg(String reqMsg) {
        synchronized (OtrpAgent.class) {
            Log.i(TAG, "Process otrp message start.");
            String resMsg = null;
            if (reqMsg == null) {
                return null;
            }
            IHwTEEService hwTEEService = getOtrpPlugin();
            if (hwTEEService != null) {
                try {
                    resMsg = hwTEEService.processOtrpMsg(reqMsg);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when otrp jni is invoked.");
                }
            }
            Log.d(TAG, "Process otrp message end.");
            return resMsg;
        }
    }

    public static synchronized int activeTrustApplication(String sdId, String taId) {
        synchronized (OtrpAgent.class) {
            Log.i(TAG, "Active trust application start.");
            if (taId != null) {
                if (taId.length() > 0) {
                    int result = -1;
                    IHwTEEService hwTEEService = getOtrpPlugin();
                    if (hwTEEService != null) {
                        try {
                            result = hwTEEService.activeTrustApplication(sdId, taId);
                        } catch (RemoteException e) {
                            Log.e(TAG, "RemoteException when otrp jni is invoked.");
                        }
                    }
                    Log.i(TAG, "Active trust application end." + result);
                    return result;
                }
            }
            return -1;
        }
    }

    public static synchronized int init(Context context) {
        synchronized (OtrpAgent.class) {
            if (context == null) {
                Log.e(TAG, "Context is null.");
                return -1;
            } else if (getOtrpPlugin() != null) {
                Log.d(TAG, "Init HwTeeService is success.");
                return 0;
            } else {
                Log.d(TAG, "Init HwTeeService is failed.");
                return -1;
            }
        }
    }

    public static synchronized int unInit(Context context) {
        synchronized (OtrpAgent.class) {
            if (context == null) {
                Log.e(TAG, "Context is null.");
                return -1;
            }
            Application application = ActivityThreadEx.currentApplication();
            if (application == null) {
                Log.e(TAG, "Application is null.");
                return -1;
            }
            int result = OtrpConnectManager.unBindHwTeeService(application.getApplicationContext());
            sHwOtrpPlugin = null;
            Log.d(TAG, "unInit HwTeeService is: " + result);
            return result;
        }
    }
}
