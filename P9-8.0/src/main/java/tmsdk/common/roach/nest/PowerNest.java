package tmsdk.common.roach.nest;

import android.content.Context;
import android.os.HandlerThread;
import com.qq.taf.jce.JceStruct;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import tmsdkobf.pt;

public class PowerNest {
    public static final int SHARK_HTTP = 1;
    public static final int SHARK_TCP = 2;
    public static final int S_ERR_NONE = 0;
    public static final int sNestVersion = 105;

    public static Context getAppContext() {
        return pt.getAppContext();
    }

    public static boolean sendShark(int cmdId, JceStruct req, JceStruct resp, int flag, ISharkCallBackNest callbackF, long callBackTimeout) {
        return pt.sendShark(cmdId, req, resp, flag, callbackF, callBackTimeout);
    }

    public static int download(String url, String fileDir, String fileName) {
        return pt.download(url, fileDir, fileName);
    }

    public static void addTask(Runnable task, String taskName) {
        pt.addTask(task, taskName);
    }

    public static Thread newFreeThread(Runnable task, String taskName) {
        return pt.newFreeThread(task, taskName);
    }

    public static HandlerThread newFreeHandlerThread(String taskName) {
        return pt.newFreeHandlerThread(taskName);
    }

    public static void saveStringData(int modelId, String msg) {
        pt.saveStringData(modelId, msg);
    }

    public static void saveActionData(int modelId) {
        pt.saveActionData(modelId);
    }

    public static void saveMultiValueData(int modelId, int integerData) {
        pt.saveMultiValueData(modelId, integerData);
    }

    public static void tryReportData() {
        pt.tryReportData();
    }

    public static int runHttpSession(int requestId, String servantName, String funcName, HashMap<String, Object> requestParams, String respKey, Class<?> respClass, AtomicReference<Object> resp) {
        return pt.runHttpSession(requestId, servantName, funcName, requestParams, respKey, respClass, resp);
    }

    public static int bsPatch(String oldfile, String patchfile, String newfile, int flag) {
        return pt.bsPatch(oldfile, patchfile, newfile, flag);
    }

    public static String fileMd5(String file) {
        return pt.fileMd5(file);
    }

    public static String getByteMd5(byte[] text) {
        return pt.getByteMd5(text);
    }

    public static void putInt(String key, int value) {
        pt.putInt(key, value);
    }

    public static int getInt(String key, int defaultvalue) {
        return pt.getInt(key, defaultvalue);
    }

    public static void putLong(String key, long value) {
        pt.putLong(key, value);
    }

    public static long getLong(String key, long defaultvalue) {
        return pt.getLong(key, defaultvalue);
    }

    public static void putString(String key, String value) {
        pt.putString(key, value);
    }

    public static String getString(String key, String defaultvalue) {
        return pt.getString(key, defaultvalue);
    }

    public static void remove(String key) {
        pt.remove(key);
    }
}
