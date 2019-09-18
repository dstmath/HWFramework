package android.emcom;

import android.content.Context;
import android.emcom.IEmcomManager;
import android.emcom.IHandoffSdkInterface;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public final class EmcomManager {
    private static final int DEF_MAINCARD_PS_STATUS = 0;
    public static final int EMCOMMANAGER_ERR = -1;
    public static final int EMCOMMANAGER_OK = 0;
    private static final String HANDOFF_DATA_TYPE = "handoff_data_type";
    private static final int HANDOFF_DATA_TYPE_AIRSHARING = 4;
    private static final int HANDOFF_DATA_TYPE_DEFAULT = 0;
    private static final int HANDOFF_RELATION_MAP_MAX = 5;
    private static final int SLICE_OPERATION_FAILED = 1001;
    private static final String TAG = "EmcomManager";
    private static EmcomManager mEmcomManager;
    private IHandoffSdkInterface mHandoffService;
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
        this.mService = IEmcomManager.Stub.asInterface(ServiceManager.getService(TAG));
        if (this.mService == null) {
            Log.i(TAG, "IEmcomManager getService() is null ");
        }
        return this.mService;
    }

    private IHandoffSdkInterface getHandoffSdkService() {
        this.mHandoffService = IHandoffSdkInterface.Stub.asInterface(ServiceManager.getService("com.huawei.pcassistant.handoffsdk.HandoffSdkService"));
        if (this.mHandoffService == null) {
            Log.i(TAG, "IHandoffSdkInterface getService() is null ");
        }
        return this.mHandoffService;
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

    public void notifyHwAppData(String module, String pkgName, String info) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.notifyHwAppData(module, pkgName, info);
        } catch (RemoteException e) {
            Log.i(TAG, "notifyHwAppData RemoteException: " + e.toString());
        }
    }

    public void notifyAppData(String info) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.notifyAppData(info);
        } catch (RemoteException e) {
            Log.i(TAG, "notifyAppData RemoteException: " + e.toString());
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

    public String getSmartcareData(String module, String pkgName, String jsonStr) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return null;
        }
        try {
            return service.getSmartcareData(module, pkgName, jsonStr);
        } catch (RemoteException e) {
            Log.i(TAG, "getSmartcareData RemoteException: " + e.toString());
            return null;
        }
    }

    public int registerAppCallback(String packageName, ISliceSdkCallback callback) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e("SliceSdkLogger", "EmcomManager: getEmcomservice is null ");
            return 1001;
        }
        try {
            return service.registerAppCallback(packageName, callback);
        } catch (RemoteException e) {
            Log.e("SliceSdkLogger", "EmcomManager: registerAppCallback: RemoteException ");
            return 1001;
        }
    }

    public void activeSlice(String packageName, String version, int sessionNumber, String serverList) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e("SliceSdkLogger", "EmcomManager: getEmcomservice is null ");
            return;
        }
        try {
            service.activeSlice(packageName, version, sessionNumber, serverList);
        } catch (RemoteException e) {
            Log.e("SliceSdkLogger", "EmcomManager: activeSlice: RemoteException ");
        }
    }

    public void deactiveSlice(String packageName, String version, int sessionNumber, String saId) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e("SliceSdkLogger", "EmcomManager: getEmcomservice is null ");
            return;
        }
        try {
            service.deactiveSlice(packageName, version, sessionNumber, saId);
        } catch (RemoteException e) {
            Log.e("SliceSdkLogger", "EmcomManager: deactiveSlice: RemoteException ");
        }
    }

    public void updateAppInfo(String packageName, String version, int sessionNumber, String saId, String appInfoJson) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e("SliceSdkLogger", "EmcomManager: getEmcomservice is null ");
            return;
        }
        try {
            service.updateAppInfo(packageName, version, sessionNumber, saId, appInfoJson);
        } catch (RemoteException e) {
            Log.e("SliceSdkLogger", "EmcomManager: updateAppInfo: RemoteException ");
        }
    }

    public String getRuntimeInfo(String packageName) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e("SliceSdkLogger", "EmcomManager: getEmcomservice is null ");
            return null;
        }
        try {
            return service.getRuntimeInfo(packageName);
        } catch (RemoteException e) {
            Log.e("SliceSdkLogger", "EmcomManager: getPhoneInfo: RemoteException ");
            return null;
        }
    }

    public int registerHandoff(String packageName, int dataType, IHandoffSdkCallback callback) {
        Log.d(TAG, "registerHandoff packageName: " + packageName + " DataType: " + dataType);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "registerHandoff getEmcomservice is null package: " + packageName);
            return -1;
        }
        try {
            int retCode = service.registerHandoff(packageName, dataType, callback);
            if (retCode != 0) {
                return retCode;
            }
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "registerHandoff RemoteException package: " + packageName);
            e.printStackTrace();
            return -1;
        }
    }

    public int notifyHandoffServiceStart(IHandoffServiceCallback callback) {
        Log.d(TAG, "notifyHandoffServiceStart callback: " + callback);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "notifyHandoffServiceStart getEmcomservice is null");
            return -1;
        }
        try {
            int retCode = service.notifyHandoffServiceStart(callback);
            if (retCode != 0) {
                return retCode;
            }
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "notifyHandoffServiceStart RemoteException ");
            e.printStackTrace();
            return -1;
        }
    }

    public int notifyHandoffServiceStop() {
        Log.d(TAG, "notifyHandoffServiceStop ");
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "notifyHandoffServiceStop getEmcomservice is null");
            return -1;
        }
        try {
            return service.notifyHandoffServiceStop();
        } catch (RemoteException e) {
            Log.e(TAG, "notifyHandoffServiceStop RemoteException ");
            return -1;
        }
    }

    public void notifyHandoffStateChg(int state) {
        Log.d(TAG, "notifyHandoffStateChg state: " + state);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "notifyHandoffStateChg getEmcomservice is null");
            return;
        }
        try {
            service.notifyHandoffStateChg(state);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyHandoffStateChg RemoteException ");
            e.printStackTrace();
        }
    }

    public int notifyHandoffDataEvent(String packageName, String para) {
        Log.d(TAG, "notifyHandoffDataEvent packageName: " + packageName + " para: " + para);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "notifyHandoffDataEvent getEmcomservice is null");
            return -1;
        }
        try {
            int retCode = service.notifyHandoffDataEvent(packageName, para);
            if (retCode != 0) {
                return retCode;
            }
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "notifyHandoffDataEvent RemoteException ");
            e.printStackTrace();
            return -1;
        }
    }

    public int startHandoffService(String packageName, JSONObject para) {
        Log.d(TAG, "startHandoffService packageName: " + packageName);
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "startHandoffService getHandoffSdkService is null package: " + packageName);
            return -1;
        } else if (!isEnableHandoff()) {
            Log.d(TAG, "startHandoffService but handoff disconnect packageName: " + packageName);
            return -1;
        } else {
            try {
                int retCode = service.startHandoffService(packageName, para.toString());
                if (retCode != 0) {
                    return retCode;
                }
                return 0;
            } catch (RemoteException e) {
                Log.e(TAG, "startHandoffService RemoteException package: " + packageName);
                e.printStackTrace();
                return -1;
            }
        }
    }

    public int stopHandoffService(String packageName, JSONObject para) {
        Log.d(TAG, "stopHandoffService packageName: " + packageName);
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "stopHandoffService getHandoffSdkService is null package: " + packageName);
            return -1;
        } else if (!isEnableHandoff()) {
            Log.d(TAG, "stopHandoffService but handoff disconnect packageName: " + packageName);
            return -1;
        } else {
            try {
                int retCode = service.stopHandoffService(packageName, para.toString());
                if (retCode != 0) {
                    return retCode;
                }
                return 0;
            } catch (RemoteException e) {
                Log.e(TAG, "stopHandoffService RemoteException package: " + packageName);
                e.printStackTrace();
                return -1;
            }
        }
    }

    public int syncHandoffData(String packageName, JSONObject para) {
        Log.d(TAG, "syncHandoffData packageName: " + packageName);
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "syncHandoffData getHandoffSdkService is null package: " + packageName);
            return -1;
        } else if (para.optInt(HANDOFF_DATA_TYPE, 0) == 4 || isEnableHandoff()) {
            try {
                int retCode = service.syncHandoffData(packageName, para.toString());
                if (retCode != 0) {
                    return retCode;
                }
                return 0;
            } catch (RemoteException e) {
                Log.e(TAG, "syncHandoffData RemoteException package: " + packageName);
                e.printStackTrace();
                return -1;
            }
        } else {
            Log.d(TAG, "syncHandoffData but handoff disconnect packageName: " + packageName);
            return -1;
        }
    }

    public boolean isHandoffServiceSupported(String packageName, int serviceType) {
        Log.d(TAG, "isHandoffServiceSupported packageName: " + packageName);
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "isHandoffServiceSupported getHandoffSdkService is null ");
            return false;
        }
        try {
            return service.isHandoffServiceSupported(packageName, serviceType);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException package: " + packageName);
            return false;
        }
    }

    public Map<String, List<String>> getHandoffBindRelationMap(String packageName, int serviceType) {
        Log.d(TAG, "getHandoffBindRelationMap packageName: " + packageName);
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "null package: " + packageName);
            return new HashMap(5);
        }
        try {
            return service.getHandoffBindRelationMap(packageName, serviceType);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException package: " + packageName);
            return new HashMap(5);
        }
    }

    public int unbindHandoffRelation(String packageName, int serviceType, String para) {
        Log.d(TAG, "unbindHandoffRelation packageName: " + packageName);
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "null package: " + packageName);
            return -1;
        }
        try {
            return service.unbindHandoffRelation(packageName, serviceType, para);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException package: " + packageName);
            return -1;
        }
    }

    public boolean isEnableHandoff() {
        boolean bEnable = false;
        Log.d(TAG, "isEnableHandoff");
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "isEnableHandoff getHandoffSdkService is null ");
            return false;
        }
        try {
            bEnable = service.isEnableHandoff();
        } catch (RemoteException e) {
            Log.e(TAG, "isEnableHandoff RemoteException  ");
            e.printStackTrace();
        }
        return bEnable;
    }

    public void notifySmartMp(int status) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.notifySmartMp(status);
        } catch (RemoteException e) {
            Log.e(TAG, "notifySmartMp: RemoteException ");
        }
    }

    public boolean isSmartMpEnable() {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "getEmcomservice is null ");
            return false;
        }
        try {
            return service.isSmartMpEnable();
        } catch (RemoteException e) {
            Log.e(TAG, "isSmartMpEnable(): RemoteException ");
            return false;
        }
    }

    public void listenHiCom(IListenDataCallback callback, String listenInfo) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.listenHiCom(callback, listenInfo);
        } catch (RemoteException e) {
            Log.i(TAG, "listenHiCom: RemoteException: " + e.toString());
        }
    }
}
