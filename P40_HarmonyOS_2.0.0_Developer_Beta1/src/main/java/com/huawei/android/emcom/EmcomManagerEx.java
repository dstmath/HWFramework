package com.huawei.android.emcom;

import android.emcom.EmailInfo;
import android.emcom.EmcomManager;
import android.emcom.IConnectCallback;
import android.emcom.IExternalMpCallback;
import android.emcom.IHandoffSdkCallback;
import android.emcom.IHandoffServiceCallback;
import android.emcom.IOneHopAppCallback;
import android.emcom.IOneHopAuthReqCallback;
import android.emcom.IOnehopCallback;
import android.emcom.IStateCallback;
import android.emcom.OnehopCallback;
import android.emcom.OnehopDeviceInfo;
import android.emcom.OnehopSendDataPara;
import android.emcom.VideoInfo;
import android.os.RemoteException;
import android.util.Log;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class EmcomManagerEx {
    public static final int ONEHOP_RESULT_ERROR_GENERIC = -1;
    public static final int ONEHOP_RESULT_ERROR_INPUT_PARA_INVALID = -4;
    public static final int ONEHOP_RESULT_ERROR_NOT_REGISTERED = -2;
    public static final int ONEHOP_RESULT_ERROR_PERMISSION_REJECTED = -3;
    public static final int ONEHOP_RESULT_OK = 0;
    public static final int ORDERED_BROADCAST_NEED_CONTINUE = 1;
    public static final int ORDERED_BROADCAST_NOT_NEED_CONTINUE = 0;
    private static final String TAG = "EmcomManagerEx";
    private static volatile EmcomManagerEx sEmcomManagerEx;

    public static synchronized EmcomManagerEx getInstance() {
        EmcomManagerEx emcomManagerEx;
        synchronized (EmcomManagerEx.class) {
            if (sEmcomManagerEx == null) {
                sEmcomManagerEx = new EmcomManagerEx();
            }
            emcomManagerEx = sEmcomManagerEx;
        }
        return emcomManagerEx;
    }

    public static void notifyEmailData(Object obj) throws RemoteException {
        if (obj instanceof EmailInfo) {
            EmailInfo eci = (EmailInfo) obj;
            Log.d(TAG, "notifyEmailData eci=" + eci);
            EmcomManager.getInstance().notifyEmailData(eci);
            return;
        }
        Log.d(TAG, "illegal EmailData");
    }

    public static void notifyVideoData(Object obj) throws RemoteException {
        if (obj instanceof VideoInfo) {
            VideoInfo vci = (VideoInfo) obj;
            Log.e(TAG, "notifyVideoData vci = " + vci);
            EmcomManager.getInstance().notifyVideoData(vci);
            return;
        }
        Log.d(TAG, "illegal VideolData");
    }

    public static void notifyHwAppData(String module, String pkgName, String obj) throws RemoteException {
        if (obj != null) {
            Log.d(TAG, "notifyHwAppData info = " + obj);
            EmcomManager.getInstance().notifyHwAppData(module, pkgName, obj);
            return;
        }
        Log.d(TAG, "illegal notifyHwAppData");
    }

    public void responseForParaUpgrade(int paratype, int pathtype, int result) {
        EmcomManager.getInstance().responseForParaUpgrade(paratype, pathtype, result);
        Log.i(TAG, "responseForParaUpgrade: paratype = " + paratype + ", pathtype = " + pathtype + ", result = " + result);
    }

    public int registerHandoff(String packageName, int dataType, IHandoffSdkCallback callback) {
        Log.d(TAG, "registerHandoff packageName: " + packageName + " DataType: " + dataType);
        return EmcomManager.getInstance().registerHandoff(packageName, dataType, callback);
    }

    public int registerOneHop(String packageName, int dataType, IOneHopAppCallback callback) {
        Log.d(TAG, "registerOneHop packageName: " + packageName + "DataType: " + dataType);
        return EmcomManager.getInstance().registerOneHop(packageName, dataType, callback);
    }

    public int unregisterOneHop(String packageName, int dataType) {
        Log.d(TAG, "unregisterOneHop packageName: " + packageName + "DataType: " + dataType);
        return EmcomManager.getInstance().unregisterOneHop(packageName, dataType);
    }

    public int notifyHandoffServiceStart(IHandoffServiceCallback callback) {
        Log.d(TAG, "notifyHandoffServiceStart ");
        return EmcomManager.getInstance().notifyHandoffServiceStart(callback);
    }

    public int notifyHandoffServiceStop() {
        Log.d(TAG, "notifyHandoffServiceStop ");
        return EmcomManager.getInstance().notifyHandoffServiceStop();
    }

    public void notifyHandoffStateChg(int state) {
        Log.d(TAG, "notifyHandoffStateChg state: " + state);
        EmcomManager.getInstance().notifyHandoffStateChg(state);
    }

    public int notifyHandoffDataEvent(String packageName, String para) {
        Log.d(TAG, "notifyHandoffDataEvent packageName: " + packageName);
        return EmcomManager.getInstance().notifyHandoffDataEvent(packageName, para);
    }

    public int startHandoffService(String packageName, JSONObject para) {
        Log.d(TAG, "startHandoffService packageName: " + packageName + " para: " + para.toString());
        return EmcomManager.getInstance().startHandoffService(packageName, para);
    }

    public int stopHandoffService(String packageName, JSONObject para) {
        Log.d(TAG, "stopHandoffService packageName: " + packageName + " para: " + para.toString());
        return EmcomManager.getInstance().stopHandoffService(packageName, para);
    }

    public int syncHandoffData(String packageName, JSONObject para) {
        Log.d(TAG, "syncHandoffData packageName: " + packageName);
        return EmcomManager.getInstance().syncHandoffData(packageName, para);
    }

    public int oneHopSend(String packageName, JSONObject para) {
        Log.d(TAG, "oneHopSend packageName: " + packageName);
        return EmcomManager.getInstance().oneHopSend(packageName, para);
    }

    public boolean isEnableHandoff() {
        boolean isEnableHandoff = EmcomManager.getInstance().isEnableHandoff();
        Log.d(TAG, "isEnableHandoff: " + isEnableHandoff);
        return isEnableHandoff;
    }

    public boolean isHandoffServiceSupported(String packageName, int serviceType) {
        Log.d(TAG, "isHandoffServiceSupported packageName: " + packageName);
        return EmcomManager.getInstance().isHandoffServiceSupported(packageName, serviceType);
    }

    public Map<String, List<String>> getHandoffBindRelationMap(String packageName, int serviceType) {
        Log.d(TAG, "getHandoffBindRelationMap packageName: " + packageName);
        return EmcomManager.getInstance().getHandoffBindRelationMap(packageName, serviceType);
    }

    public int unbindHandoffRelation(String packageName, int serviceType, String para) {
        Log.d(TAG, "unbindHandoffRelation packageName: " + packageName);
        return EmcomManager.getInstance().unbindHandoffRelation(packageName, serviceType, para);
    }

    public static void notifySmartMp(int status) {
        EmcomManager.getInstance().notifySmartMp(status);
        Log.i(TAG, "notifySmartMp: staus = " + status);
    }

    public int activeCongestionConrolAlg(String packageName, JSONObject para) {
        Log.d(TAG, "activeCongestionConrolAlg: packageName = " + packageName + " para: " + para.toString());
        return EmcomManager.getInstance().activeCongestionConrolAlg(packageName, para);
    }

    public int deactiveCongestionControlAlg(String packageName) {
        Log.d(TAG, "deactiveCongestionControlAlg: packageName = " + packageName);
        return EmcomManager.getInstance().deactiveCongestionControlAlg(packageName);
    }

    public int onehopRegisterModule(String moduleName, IOnehopCallback callback) {
        Log.d(TAG, "onehopRegisterModule moduleName: " + moduleName);
        if (callback == null) {
            Log.d(TAG, "onehopRegisterModule callback null");
        }
        return EmcomManager.getInstance().onehopRegisterModule(moduleName, callback);
    }

    public int onehopRegister(String moduleName, OnehopCallback callback, String extInfo) {
        Log.d(TAG, "onehopRegister moduleName: " + moduleName);
        return EmcomManager.getInstance().onehopRegister(moduleName, callback, extInfo);
    }

    public int onehopUnregister(String moduleName, String extInfo) {
        Log.d(TAG, "onehopUnregister moduleName: " + moduleName);
        return EmcomManager.getInstance().onehopUnregister(moduleName, extInfo);
    }

    public int onehopUnregisterModule(String moduleName) {
        Log.d(TAG, "onehopUnregisterModule moduleName: " + moduleName);
        return EmcomManager.getInstance().onehopUnregisterModule(moduleName);
    }

    public int onehopStartDeviceFind(String moduleName, boolean trust) {
        Log.d(TAG, "onehopStartDeviceFind moduleName: " + moduleName + "trust: " + trust);
        return EmcomManager.getInstance().onehopStartDeviceFind(moduleName, trust);
    }

    public int onehopStartDiscovery(String moduleName, String extInfo) {
        Log.d(TAG, "onehopStartDiscovery moduleName: " + moduleName);
        return EmcomManager.getInstance().onehopStartDiscovery(moduleName, extInfo);
    }

    public List<OnehopDeviceInfo> onehopGetDeviceList(String moduleName) {
        Log.d(TAG, "onehopGetDeviceList moduleName: " + moduleName);
        return EmcomManager.getInstance().onehopGetDeviceList(moduleName);
    }

    public int onehopSendData(OnehopSendDataPara onehopSendDataPara) {
        if (onehopSendDataPara != null) {
            Log.d(TAG, "onehopSendData " + onehopSendDataPara.toString());
        }
        return EmcomManager.getInstance().onehopSendData(onehopSendDataPara);
    }

    public int onehopRegisterDeviceListChange(String moduleName, boolean trust) {
        Log.d(TAG, "onehopRegisterDeviceListChange moduleName: " + moduleName + "trust: " + trust);
        return EmcomManager.getInstance().onehopRegisterDeviceListChange(moduleName, trust);
    }

    public int onehopUnregisterDeviceListChange(String moduleName) {
        Log.d(TAG, "onehopUnregisterDeviceListChange moduleName: " + moduleName);
        return EmcomManager.getInstance().onehopUnregisterDeviceListChange(moduleName);
    }

    public int onehopConnectDevice(String deviceId, String moduleName, int type, String extInfo) {
        Log.d(TAG, "onehopConnectDevice moduleName: " + moduleName + " type: " + type);
        return EmcomManager.getInstance().onehopConnectDevice(deviceId, moduleName, type, extInfo);
    }

    public String onehopGetVersion() {
        Log.d(TAG, "onehopGetVersion");
        return EmcomManager.getInstance().onehopGetVersion();
    }

    public int onehopDisconnectDevice(String deviceId, String moduleName, int type, String extInfo) {
        Log.d(TAG, "onehopDisconnectDevice moduleName: " + moduleName + " type: " + type);
        return EmcomManager.getInstance().onehopDisconnectDevice(deviceId, moduleName, type, extInfo);
    }

    public int registerExternalMp(String appInfo, IExternalMpCallback callback) {
        Log.d(TAG, "registerExternalMp appInfo: " + appInfo);
        return EmcomManager.getInstance().registerExternalMp(appInfo, callback);
    }

    public void notifyExternalMpPopStartGuide(String appInfo) {
        Log.d(TAG, "notifyExternalMpPopStartGuide appInfo: " + appInfo);
        EmcomManager.getInstance().notifyExternalMpPopStartGuide(appInfo);
    }

    public void notifyExternalMpAppServiceStart(String appInfo) {
        Log.d(TAG, "notifyExternalMpAppServiceStart appInfo: " + appInfo);
        EmcomManager.getInstance().notifyExternalMpAppServiceStart(appInfo);
    }

    public void notifyExternalMpEnabled(String appInfo) {
        Log.d(TAG, "notifyExternalMpEnable appInfo: " + appInfo);
        EmcomManager.getInstance().notifyExternalMpEnabled(appInfo);
    }

    public void notifyExternalMpAppServiceStop(String appInfo) {
        Log.d(TAG, "notifyExternalMpAppServiceStop appInfo: " + appInfo);
        EmcomManager.getInstance().notifyExternalMpAppServiceStop(appInfo);
    }

    public int unregisterExternalMp(String appInfo) {
        Log.d(TAG, "unregisterExternalMp appInfo: " + appInfo);
        return EmcomManager.getInstance().unregisterExternalMp(appInfo);
    }

    public int registerDeviceStateCb(String deviceId, IStateCallback callback) {
        Log.d(TAG, "registerDeviceStateCb.");
        return EmcomManager.getInstance().registerDeviceStateCb(deviceId, callback);
    }

    public int unRegisterDeviceStateCb(String deviceId) {
        Log.d(TAG, "unRegisterDeviceStateCb.");
        return EmcomManager.getInstance().unRegisterDeviceStateCb(deviceId);
    }

    public int disconnectDevice(String deviceId, List<String> serviceTypeList) {
        Log.d(TAG, "disconnectDevice.");
        return EmcomManager.getInstance().disconnectDevice(deviceId, serviceTypeList);
    }

    public int registerDeviceConnectManagerCb(String deviceId, String serviceType, IConnectCallback callback) {
        Log.d(TAG, "registerDeviceConnectManagerCb  serviceType: " + serviceType);
        return EmcomManager.getInstance().registerDeviceConnectManagerCb(deviceId, serviceType, callback);
    }

    public int unRegisterDeviceConnectManagerCb(String deviceId, String serviceType) {
        Log.d(TAG, "unRegDeviceConnectManager  serviceType: " + serviceType);
        return EmcomManager.getInstance().unRegisterDeviceConnectManagerCb(deviceId, serviceType);
    }

    public void notifyConnectStateChanged(String deviceId, String serviceType, int state) {
        Log.d(TAG, "notifyConnectStateChanged  serviceType: " + serviceType + " state:" + state);
        EmcomManager.getInstance().notifyConnectStateChanged(deviceId, serviceType, state);
    }

    public int onehopAuthReq(String packageName, IOneHopAuthReqCallback callback) {
        Log.d(TAG, "onehopAuthReq  packageName: " + packageName);
        return EmcomManager.getInstance().onehopAuthReq(packageName, callback);
    }
}
