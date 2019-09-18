package com.huawei.android.emcom;

import android.emcom.EmailInfo;
import android.emcom.EmcomManager;
import android.emcom.IHandoffSdkCallback;
import android.emcom.IHandoffServiceCallback;
import android.emcom.ISliceSdkCallback;
import android.emcom.VideoInfo;
import android.os.RemoteException;
import android.util.Log;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class EmcomManagerEx {
    private static final String TAG = "EmcomManagerEx";
    private static volatile EmcomManagerEx mEmcomManagerEx;

    public static synchronized EmcomManagerEx getInstance() {
        EmcomManagerEx emcomManagerEx;
        synchronized (EmcomManagerEx.class) {
            if (mEmcomManagerEx == null) {
                mEmcomManagerEx = new EmcomManagerEx();
            }
            emcomManagerEx = mEmcomManagerEx;
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

    public int registerAppCallback(String packageName, ISliceSdkCallback callback) throws NoSuchMethodError {
        return EmcomManager.getInstance().registerAppCallback(packageName, callback);
    }

    public void activeSlice(String packageName, String version, int sessionNumber, String serverList) throws NoSuchMethodError {
        EmcomManager.getInstance().activeSlice(packageName, version, sessionNumber, serverList);
    }

    public void deactiveSlice(String packageName, String version, int sessionNumber, String saId) throws NoSuchMethodError {
        EmcomManager.getInstance().deactiveSlice(packageName, version, sessionNumber, saId);
    }

    public void updateAppInfo(String packageName, String version, int sessionNumber, String saId, String appInfoJson) throws NoSuchMethodError {
        EmcomManager.getInstance().updateAppInfo(packageName, version, sessionNumber, saId, appInfoJson);
    }

    public String getRuntimeInfo(String packageName) throws NoSuchMethodError {
        return EmcomManager.getInstance().getRuntimeInfo(packageName);
    }

    public int registerHandoff(String packageName, int dataType, IHandoffSdkCallback callback) {
        Log.d(TAG, "registerHandoff packageName: " + packageName + " DataType: " + dataType);
        return EmcomManager.getInstance().registerHandoff(packageName, dataType, callback);
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
        Log.d(TAG, "notifyHandoffDataEvent packageName: " + packageName + " para: " + para);
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
        Log.d(TAG, "syncHandoffData packageName: " + packageName + " para: " + para.toString());
        return EmcomManager.getInstance().syncHandoffData(packageName, para);
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

    public boolean isEnableHandoff() {
        boolean isEnableHandoff = EmcomManager.getInstance().isEnableHandoff();
        Log.d(TAG, "isEnableHandoff: " + isEnableHandoff);
        return isEnableHandoff;
    }

    public static void notifySmartMp(int status) {
        EmcomManager.getInstance().notifySmartMp(status);
        Log.i(TAG, "notifySmartMp: staus = " + status);
    }
}
