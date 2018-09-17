package android.emcom;

import android.content.Context;
import android.emcom.IEmcomManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public final class EmcomManager {
    private static final int DEF_MAINCARD_PS_STATUS = 0;
    private static final String TAG = "EmcomManager";
    private static EmcomManager mEmcomManager;
    private IEmcomManager mService;

    public static synchronized EmcomManager getInstance() {
        EmcomManager emcomManager;
        synchronized (EmcomManager.class) {
            if (mEmcomManager == null) {
                mEmcomManager = new EmcomManager();
            }
            emcomManager = mEmcomManager;
        }
        return emcomManager;
    }

    private IEmcomManager getService() {
        if (this.mService != null) {
            return this.mService;
        }
        this.mService = Stub.asInterface(ServiceManager.getService(TAG));
        if (this.mService == null) {
            Log.i(TAG, "IEmcomManager getService() is null ");
        }
        return this.mService;
    }

    public XEngineAppInfo getAppInfo(Context context) {
        if (context == null) {
            Log.i(TAG, "context is null!");
            return null;
        }
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return null;
        }
        try {
            return service.getAppInfo(context.getPackageName());
        } catch (RemoteException e) {
            Log.i(TAG, "getAppInfo RemoteException ");
            return null;
        }
    }

    public void accelerate(Context context, int grade) {
        accelerateWithMainCardPsStatus(context, grade, 0);
    }

    public void accelerateWithMainCardPsStatus(Context context, int grade, int mainCardPsStatus) {
        if (context == null) {
            Log.i(TAG, "context is null!");
            return;
        }
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.accelerateWithMainCardServiceStatus(context.getPackageName(), grade, mainCardPsStatus);
        } catch (RemoteException e) {
            Log.i(TAG, "accelerate RemoteException ");
        }
    }

    public void notifyEmailData(EmailInfo eci) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.notifyEmailData(eci);
        } catch (RemoteException e) {
            Log.i(TAG, "notifyEmailData RemoteException ");
        }
    }

    public void notifyVideoData(VideoInfo eci) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.notifyVideoData(eci);
        } catch (RemoteException e) {
            Log.i(TAG, "notifyVideoData RemoteException ");
        }
    }

    public void responseForParaUpgrade(int paratype, int pathtype, int result) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.responseForParaUpgrade(paratype, pathtype, result);
            Log.i(TAG, "responseForParaUpgrade: paratype = " + paratype + ", pathtype = " + pathtype + ", result = " + result);
        } catch (RemoteException e) {
            Log.e(TAG, "responseForParaUpgrade RemoteException ");
        }
    }

    public void updateAppExperienceStatus(int uid, int experience, int rrt) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.updateAppExperienceStatus(uid, experience, rrt);
        } catch (RemoteException e) {
            Log.i(TAG, "updateAppExperienceStatus RemoteException ");
        }
    }

    public void notifyRunningStatus(int type, String packageName) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.notifyRunningStatus(type, packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyRunningStatus: RemoteException ");
        }
    }
}
