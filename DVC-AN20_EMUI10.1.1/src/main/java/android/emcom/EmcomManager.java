package android.emcom;

import android.emcom.IEmcomManager;
import android.emcom.IHandoffSdkInterface;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.softnet.connect.IAdvertiseOption;
import com.huawei.softnet.connect.IConnectionCallback;
import com.huawei.softnet.connect.IDiscoveryCallback;
import com.huawei.softnet.connect.IListenOption;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;
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
    private static final int ONEHOP_RESULT_ERROR_GENERIC = -1;
    private static final int ONEHOP_RESULT_OK = 0;
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

    public void responseForParaUpgrade(int paraType, int pathType, int result) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "getEmcomservice is null ");
            return;
        }
        try {
            service.responseForParaUpgrade(paraType, pathType, result);
            Log.i(TAG, "responseForParaUpgrade: paraType = " + paraType + ", pathType = " + pathType + ", result = " + result);
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

    public int registerHandoff(String packageName, int dataType, IHandoffSdkCallback callback) {
        Log.d(TAG, "registerHandoff packageName: " + packageName + " DataType: " + dataType);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "registerHandoff getEmcomservice is null package: " + packageName);
            return -1;
        }
        try {
            int retCode = service.registerHandoff(packageName, dataType, callback, new Binder());
            if (retCode != 0) {
                return retCode;
            }
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "registerHandoff RemoteException package: " + packageName);
            return -1;
        }
    }

    public int registerOneHop(String packageName, int dataType, IOneHopAppCallback callback) {
        Log.d(TAG, "registerOneHop packageName: " + packageName + " DataType: " + dataType);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "registerOneHop getEmcomservice is null package: " + packageName);
            return -1;
        }
        try {
            int retCode = service.registerOneHop(packageName, dataType, callback, new Binder());
            if (retCode != 0) {
                return retCode;
            }
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "registerOneHop RemoteException package: " + packageName);
            return -1;
        }
    }

    public int unregisterOneHop(String packageName, int dataType) {
        Log.d(TAG, "unregisterOneHop packageName: " + packageName + " DataType: " + dataType);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "unregisterOneHop getEmcomservice is null package: " + packageName);
            return -1;
        }
        try {
            int retCode = service.unregisterOneHop(packageName, dataType);
            if (retCode != 0) {
                return retCode;
            }
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterOneHop RemoteException package: " + packageName);
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
        }
    }

    public int notifyHandoffDataEvent(String packageName, String para) {
        Log.d(TAG, "notifyHandoffDataEvent packageName: " + packageName);
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
        } else if (!isTrustApp(packageName)) {
            Log.e(TAG, "startHandoffService this app is invalid, " + packageName);
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
        } else if (!isTrustApp(packageName)) {
            Log.e(TAG, "stopHandoffService this app is invalid, " + packageName);
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
        } else if (!isTrustApp(packageName)) {
            Log.e(TAG, "syncHandoffData this app is invalid, " + packageName);
            return -1;
        } else {
            try {
                int retCode = service.syncHandoffData(packageName, para.toString());
                if (retCode != 0) {
                    return retCode;
                }
                return 0;
            } catch (RemoteException e) {
                Log.e(TAG, "syncHandoffData RemoteException package: " + packageName);
                return -1;
            }
        }
    }

    public int oneHopSend(String packageName, JSONObject para) {
        Log.d(TAG, "oneHopSend packageName: " + packageName);
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "oneHopSend getHandoffSdkService is null package: " + packageName);
            return -1;
        } else if (!isTrustApp(packageName)) {
            Log.e(TAG, "oneHopSend this app is invalid, " + packageName);
            return -1;
        } else {
            try {
                int retCode = service.syncHandoffData(packageName, para.toString());
                if (retCode != 0) {
                    return retCode;
                }
                return 0;
            } catch (RemoteException e) {
                Log.e(TAG, "oneHopSend RemoteException package: " + packageName);
                return -1;
            }
        }
    }

    public boolean isHandoffServiceSupported(String packageName, int serviceType) {
        Log.d(TAG, "isHandoffServiceSupported packageName: " + packageName);
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "isHandoffServiceSupported getHandoffSdkService is null ");
            return false;
        } else if (!isTrustApp(packageName)) {
            Log.e(TAG, "isHandoffServiceSupported this app is invalid, " + packageName);
            return false;
        } else {
            try {
                return service.isHandoffServiceSupported(packageName, serviceType);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException package: " + packageName);
                return false;
            }
        }
    }

    public Map<String, List<String>> getHandoffBindRelationMap(String packageName, int serviceType) {
        Log.d(TAG, "getHandoffBindRelationMap packageName: " + packageName);
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "null package: " + packageName);
            return new HashMap(5);
        } else if (!isTrustApp(packageName)) {
            Log.e(TAG, "getHandoffBindRelationMap this app is invalid, " + packageName);
            return new HashMap(5);
        } else {
            try {
                return service.getHandoffBindRelationMap(packageName, serviceType);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException package: " + packageName);
                return new HashMap(5);
            }
        }
    }

    public int unbindHandoffRelation(String packageName, int serviceType, String para) {
        Log.d(TAG, "unbindHandoffRelation packageName: " + packageName);
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "null package: " + packageName);
            return -1;
        } else if (!isTrustApp(packageName)) {
            Log.e(TAG, "unbindHandoffRelation this app is invalid, " + packageName);
            return -1;
        } else {
            try {
                return service.unbindHandoffRelation(packageName, serviceType, para);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException package: " + packageName);
                return -1;
            }
        }
    }

    public boolean isEnableHandoff() {
        Log.d(TAG, "isEnableHandoff");
        IHandoffSdkInterface service = getHandoffSdkService();
        if (service == null) {
            Log.e(TAG, "isEnableHandoff getHandoffSdkService is null ");
            return false;
        }
        try {
            return service.isEnableHandoff();
        } catch (RemoteException e) {
            Log.e(TAG, "isEnableHandoff RemoteException  ");
            return false;
        }
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

    public int activeCongestionConrolAlg(String packageName, JSONObject para) {
        Log.d(TAG, "activeCongestionConrolAlg: packageName: " + packageName);
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return -1;
        }
        try {
            Log.d(TAG, "activeCongestionConrolAlg:  package: " + packageName + ", alg: " + para.toString());
            return service.activeCongestionConrolAlg(packageName, para.toString());
        } catch (RemoteException e) {
            Log.i(TAG, "activeCongestionConrolAlg: RemoteException package: " + packageName + ", " + e.toString());
            return -1;
        }
    }

    public int deactiveCongestionControlAlg(String packageName) {
        Log.d(TAG, "deactiveCongestionControlAlg: packageName: " + packageName);
        IEmcomManager service = getService();
        if (service == null) {
            Log.i(TAG, "getEmcomservice is null ");
            return -1;
        }
        try {
            return service.deactiveCongestionControlAlg(packageName);
        } catch (RemoteException e) {
            Log.i(TAG, "deactiveCCAlg: RemoteException package: " + packageName + ", " + e.toString());
            return -1;
        }
    }

    public int onehopRegisterModule(String moduleName, IOnehopCallback callback) {
        Log.d(TAG, "onehopRegisterModule moduleName: " + moduleName);
        if (callback == null) {
            Log.d(TAG, "onehopRegisterModule callback null");
        }
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "onehopRegisterModule getEmcomservice is null moduleName: " + moduleName);
            return -1;
        }
        try {
            return service.onehopRegisterModule(moduleName, callback);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopRegisterModule RemoteException moduleName: " + moduleName);
            return -1;
        }
    }

    public int onehopRegisterModuleEx(String moduleName, IOnehopExCallback callback) {
        Log.d(TAG, "onehopRegisterModuleEx moduleName: " + moduleName);
        if (callback == null) {
            Log.d(TAG, "onehopRegisterModuleEx callback null");
        }
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "onehopRegisterModuleEx getEmcomservice is null moduleName: " + moduleName);
            return -1;
        }
        try {
            return service.onehopRegisterModuleEx(moduleName, callback);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopRegisterModuleEx RemoteException moduleName: " + moduleName);
            return -1;
        }
    }

    public int onehopUnregisterModule(String moduleName) {
        Log.d(TAG, "onehopUnregisterModule moduleName: " + moduleName);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "onehopUnregisterModule getEmcomservice is null moduleName: " + moduleName);
            return -1;
        }
        try {
            return service.onehopUnregisterModule(moduleName);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopUnregisterModule RemoteException moduleName: " + moduleName);
            return -1;
        }
    }

    public int onehopStartDeviceFind(String moduleName, boolean trust) {
        Log.d(TAG, "onehopStartDeviceFind moduleName: " + moduleName + "trust: " + trust);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "onehopStartDeviceFind getEmcomservice is null moduleName: " + moduleName + "trust: " + trust);
            return -1;
        }
        try {
            return service.onehopStartDeviceFind(moduleName, trust);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopStartDeviceFind RemoteException moduleName: " + moduleName + "trust: " + trust);
            return -1;
        }
    }

    public List<OnehopDeviceInfo> onehopGetDeviceList(String moduleName) {
        Log.d(TAG, "onehopGetDeviceList moduleName: " + moduleName);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "onehopGetDeviceList getEmcomservice is null moduleName: " + moduleName);
            return new ArrayList();
        }
        try {
            return service.onehopGetDeviceList(moduleName);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopGetDeviceList RemoteException moduleName: " + moduleName);
            return new ArrayList();
        }
    }

    public int onehopSendData(OnehopSendDataPara onehopSendDataPara) {
        Log.d(TAG, "onehopSendData " + onehopSendDataPara);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "onehopSendData getEmcomservice is null " + onehopSendDataPara);
            return -1;
        }
        try {
            return service.onehopSendData(onehopSendDataPara);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopSendData RemoteException " + onehopSendDataPara);
            return -1;
        }
    }

    public int onehopRegisterDeviceListChange(String moduleName, boolean trust) {
        Log.d(TAG, "onehopRegisterDeviceListChange moduleName: " + moduleName + "trust: " + trust);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "onehopRegisterDeviceListChange getEmcomservice is null moduleName: " + moduleName + "trust: " + trust);
            return -1;
        }
        try {
            return service.onehopRegisterDeviceListChange(moduleName, trust);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopRegisterDeviceListChange RemoteException moduleName: " + moduleName + "trust: " + trust);
            return -1;
        }
    }

    public int onehopUnregisterDeviceListChange(String moduleName) {
        Log.d(TAG, "onehopUnregisterDeviceListChange moduleName: " + moduleName);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "onehopUnregisterDeviceListChange getEmcomservice is null moduleName: " + moduleName);
            return -1;
        }
        try {
            return service.onehopUnregisterDeviceListChange(moduleName);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopUnregisterDeviceListChange RemoteException moduleName: " + moduleName);
            return -1;
        }
    }

    public String onehopGetVersion() {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "getEmcomservice is null ");
            return BuildConfig.FLAVOR;
        }
        try {
            return service.onehopGetVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "onehopGetVersion(): RemoteException ");
            return BuildConfig.FLAVOR;
        }
    }

    public int onehopConnectDevice(String deviceId, String moduleName, int type, String extInfo) {
        Log.d(TAG, "onehopConnectDevice moduleName: " + moduleName);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "onehopConnectDevice getEmcomservice is null moduleName: " + moduleName);
            return -1;
        }
        try {
            return service.onehopConnectDevice(deviceId, moduleName, type, extInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopConnectDevice RemoteException moduleName: " + moduleName);
            return -1;
        }
    }

    public int softnetSubscribe(String pkgName, String moduleId, IListenOption scanOption, IDiscoveryCallback callback) {
        if (pkgName == null || moduleId == null || scanOption == null || callback == null) {
            Log.e(TAG, "softnetSubscribe input error");
        }
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "softnetSubscribe getEmcomservice is null moduleName: " + moduleId);
            return -1;
        }
        Log.d(TAG, "softnetSubscribe moduleId: " + moduleId);
        try {
            return service.softnetSubscribe(pkgName, moduleId, scanOption, callback);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopConnectDevice RemoteException moduleName: " + moduleId);
            return -1;
        }
    }

    public int softnetPublish(String pkgName, String moduleId, IAdvertiseOption advOption, IConnectionCallback callback) {
        if (pkgName == null || moduleId == null || advOption == null || callback == null) {
            Log.e(TAG, "softnetPublish input error");
        }
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "softnetPublish getEmcomservice is null moduleName: " + moduleId);
            return -1;
        }
        Log.d(TAG, "softnetPublish moduleId: " + moduleId);
        try {
            return service.softnetPublish(pkgName, moduleId, advOption, callback);
        } catch (RemoteException e) {
            Log.e(TAG, "softnetPublish RemoteException moduleName: " + moduleId);
            return -1;
        }
    }

    public int onehopDisconnectDevice(String deviceId, String moduleName, int type, String extInfo) {
        Log.d(TAG, "onehopDisconnectDevice moduleName: " + moduleName);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "onehopDisconnectDevice getEmcomservice is null moduleName: " + moduleName);
            return -1;
        }
        try {
            return service.onehopDisconnectDevice(deviceId, moduleName, type, extInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopDisconnectDevice RemoteException moduleName: " + moduleName);
            return -1;
        }
    }

    public int registerExternalMp(String appInfo, IExternalMpCallback callback) {
        Log.d(TAG, "registerExternalMp appInfo: " + appInfo);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "registerExternalMp getEmcomservice is null! ");
            return -1;
        }
        try {
            return service.registerExternalMp(appInfo, callback);
        } catch (RemoteException e) {
            Log.e(TAG, "onehopDisconnectDevice RemoteException!");
            return -1;
        }
    }

    public void notifyExternalMpPopStartGuide(String appInfo) {
        Log.d(TAG, "notifyExternalMpPopStartGuide appInfo: " + appInfo);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "notifyExternalMpPopStartGuide getEmcomservice is null! ");
            return;
        }
        try {
            service.notifyExternalMpPopStartGuide(appInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyExternalMpPopStartGuide RemoteException!");
        }
    }

    public void notifyExternalMpAppServiceStart(String appInfo) {
        Log.d(TAG, "notifyExternalMpAppServiceStart appInfo: " + appInfo);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "notifyExternalMpAppServiceStart getEmcomservice is null! ");
            return;
        }
        try {
            service.notifyExternalMpAppServiceStart(appInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyExternalMpAppServiceStart RemoteException!");
        }
    }

    public void notifyExternalMpEnabled(String appInfo) {
        Log.d(TAG, "notifyExternalMpEnable appInfo: " + appInfo);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "notifyExternalMpEnable getEmcomservice is null! ");
            return;
        }
        try {
            service.notifyExternalMpEnabled(appInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyExternalMpEnable RemoteException!");
        }
    }

    public void notifyExternalMpAppServiceStop(String appInfo) {
        Log.d(TAG, "notifyExternalMpAppServiceStop appInfo: " + appInfo);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "notifyExternalMpAppServiceStop getEmcomservice is null! ");
            return;
        }
        try {
            service.notifyExternalMpAppServiceStop(appInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyExternalMpAppServiceStop RemoteException!");
        }
    }

    public int unregisterExternalMp(String appInfo) {
        Log.d(TAG, "unregisterExternalMp appInfo: " + appInfo);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "unregisterExternalMp getEmcomservice is null! ");
            return -1;
        }
        try {
            return service.unregisterExternalMp(appInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterExternalMp RemoteException!");
            return -1;
        }
    }

    public boolean isMetHicomPopGuideConditions(String pkgName) {
        Log.d(TAG, "isMetHicomPopGuideConditions pkgName: " + pkgName);
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "isMetHicomPopGuideConditions getEmcomservice is null! ");
            return false;
        }
        try {
            return service.isMetHicomPopGuideConditions(pkgName);
        } catch (RemoteException e) {
            Log.e(TAG, "isMetHicomPopGuideConditions RemoteException!");
            return false;
        }
    }

    public void notifyMpDnsResult(int uid, String host, int netType, String[] v4Addrs, String[] v6Addrs) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "notifyMpDnsResult Emcomservice is null!");
            return;
        }
        try {
            service.notifyMpDnsResult(uid, host, netType, v4Addrs, v6Addrs);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyMpDnsResult RemoteException!");
        }
    }

    public int registerDeviceStateCb(String deviceId, IStateCallback callback) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "regDeviceStateCall getEmcomservice is null");
            return -1;
        }
        try {
            return service.registerDeviceStateCb(deviceId, callback);
        } catch (RemoteException e) {
            Log.e(TAG, "regDeviceStateCall RemoteException");
            return -1;
        }
    }

    public int unRegisterDeviceStateCb(String deviceId) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "unRegDeviceStateCallback getEmcomservice is null");
            return -1;
        }
        try {
            return service.unRegisterDeviceStateCb(deviceId);
        } catch (RemoteException e) {
            Log.e(TAG, "unRegDeviceStateCallback RemoteException");
            return -1;
        }
    }

    public int disconnectDevice(String deviceId, List<String> serviceTypeList) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "disconnectDevice getEmcomservice is null");
            return -1;
        }
        try {
            return service.disconnectDevice(deviceId, serviceTypeList);
        } catch (RemoteException e) {
            Log.e(TAG, "disconnectDevice RemoteException");
            return -1;
        }
    }

    public int registerDeviceConnectManagerCb(String deviceId, String serviceType, IConnectCallback callback) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "regDeviceConnectManager getEmcomservice is null");
            return -1;
        }
        try {
            return service.registerDeviceConnectManagerCb(deviceId, serviceType, callback);
        } catch (RemoteException e) {
            Log.e(TAG, "regDeviceConnectManager RemoteException");
            return -1;
        }
    }

    public int unRegisterDeviceConnectManagerCb(String deviceId, String serviceType) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "unRegDeviceConnectManager getEmcomservice is null");
            return -1;
        }
        try {
            return service.unRegisterDeviceConnectManagerCb(deviceId, serviceType);
        } catch (RemoteException e) {
            Log.e(TAG, "unRegDeviceConnectManager RemoteException");
            return -1;
        }
    }

    public void notifyConnectStateChanged(String deviceId, String serviceType, int state) {
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "notifyConnectStateChange getEmcomservice is null");
            return;
        }
        try {
            service.notifyConnectStateChanged(deviceId, serviceType, state);
        } catch (RemoteException e) {
            Log.e(TAG, "notifyConnectStateChange RemoteException");
        }
    }

    private boolean isTrustApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "isTrustApp invalid package name " + packageName);
            return false;
        }
        IEmcomManager service = getService();
        if (service == null) {
            Log.e(TAG, "isTrustApp getEmcomservice is null");
            return false;
        }
        try {
            return service.isTrustApp(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "isTrustApp RemoteException");
            return false;
        }
    }
}
