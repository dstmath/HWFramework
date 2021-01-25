package com.android.server.rms.iaware.cpu;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.iaware.AppTypeRecoManager;
import android.rms.iaware.AwareLog;
import com.android.server.am.HwActivityManagerServiceEx;
import com.huawei.android.app.ActivityManagerExt;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.os.FileUtilsEx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class CpuCommonUtil {
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "CpuCommonUtil";

    protected static int parseInt(String value) {
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse string failed:" + value);
            return -1;
        }
    }

    protected static List<String> getProcsList(String filePath) {
        File file = new File(filePath);
        List<String> procsList = new ArrayList<>();
        if (!file.exists() || !file.canRead()) {
            AwareLog.e(TAG, "getProcsList file not exists or canot read!");
            return procsList;
        }
        String groupProcs = null;
        try {
            groupProcs = FileUtilsEx.readTextFile(file, 0, (String) null);
        } catch (IOException e) {
            AwareLog.e(TAG, "IOException + " + e.getMessage());
        }
        if (groupProcs == null || groupProcs.length() == 0) {
            return procsList;
        }
        for (String str : groupProcs.split("\n")) {
            String strProc = str.trim();
            if (strProc.length() != 0) {
                procsList.add(strProc);
            }
        }
        return procsList;
    }

    protected static String getThreadName(String tidPath) {
        String commFilePath = tidPath + "/comm";
        FileInputStream input = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufReader = null;
        String tidName = null;
        try {
            input = new FileInputStream(commFilePath);
            inputStreamReader = new InputStreamReader(input, "UTF-8");
            bufReader = new BufferedReader(inputStreamReader);
            tidName = bufReader.readLine();
        } catch (FileNotFoundException e) {
            AwareLog.e(TAG, "exception file not found, file path: " + commFilePath);
        } catch (UnsupportedEncodingException e2) {
            AwareLog.e(TAG, "UnsupportedEncodingException ");
        } catch (IOException e3) {
            AwareLog.e(TAG, "getSystemServerThreads failed!");
        } catch (Throwable th) {
            FileContent.closeBufferedReader(bufReader);
            FileContent.closeInputStreamReader(inputStreamReader);
            FileContent.closeFileInputStream(input);
            throw th;
        }
        FileContent.closeBufferedReader(bufReader);
        FileContent.closeInputStreamReader(inputStreamReader);
        FileContent.closeFileInputStream(input);
        return tidName;
    }

    protected static void getThreads(int pid, List<Integer> tidArray) {
        File[] subFiles = new File("/proc/" + pid + "/task/").listFiles();
        if (subFiles == null) {
            AwareLog.e(TAG, "getThreads null");
            return;
        }
        for (File eachTidFile : subFiles) {
            try {
                int tid = getThreadId(eachTidFile.getCanonicalPath());
                if (tid > 0) {
                    tidArray.add(Integer.valueOf(tid));
                }
            } catch (IOException e) {
            }
        }
    }

    protected static int getThreadId(String tidPath) {
        String tidStr;
        if (tidPath == null || (tidStr = getTidStr(tidPath)) == null || tidStr.isEmpty()) {
            return -1;
        }
        return parseInt(tidStr);
    }

    protected static String getTidStr(String tidPath) {
        if (tidPath == null || tidPath.isEmpty()) {
            return null;
        }
        String[] subStr = tidPath.split("task/");
        if (subStr.length == 2) {
            return subStr[1];
        }
        AwareLog.e(TAG, "getTid failed, error path is " + tidPath);
        return null;
    }

    protected static String getPackageNameForPid(int pid) {
        try {
            IBinder am = ActivityManagerExt.getIActivityManagerBinder();
            if (am == null) {
                return null;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeInt(pid);
            am.transact(504, data, reply, 0);
            reply.readException();
            String res = reply.readString();
            data.recycle();
            reply.recycle();
            return res;
        } catch (RemoteException e) {
            AwareLog.w(TAG, "getPackageNameForPid faild");
            return null;
        }
    }

    protected static int getAppTypeByPid(int pid) {
        String pkg;
        if (pid >= 0 && (pkg = getPackageNameForPid(pid)) != null) {
            return AppTypeRecoManager.getInstance().getAppType(pkg);
        }
        return -1;
    }

    protected static int getRenderTid(int pid) {
        int renderTid = HwActivityManagerServiceEx.getRenderTid(pid);
        if (renderTid >= 0) {
            return renderTid;
        }
        HwActivityManager.setProcessRecForPid(pid);
        return HwActivityManagerServiceEx.getRenderTid(pid);
    }
}
