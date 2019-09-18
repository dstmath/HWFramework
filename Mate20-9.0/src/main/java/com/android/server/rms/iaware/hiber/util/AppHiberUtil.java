package com.android.server.rms.iaware.hiber.util;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.iaware.hiber.bean.AbsAppInfo;
import com.android.server.rms.iaware.hiber.bean.HiberAppInfo;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppHiberUtil {
    private static final int SINGLE_ONE = 1;

    public static ArraySet<HiberAppInfo> getHiberProcInfoListByAbsAppInfo(Context cxt, AbsAppInfo appInfo) {
        if (cxt == null || appInfo == null) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> listTmp = ((ActivityManager) cxt.getSystemService("activity")).getRunningAppProcesses();
        if (listTmp == null || listTmp.isEmpty()) {
            return null;
        }
        ArraySet<HiberAppInfo> HiberProcInfoList = new ArraySet<>();
        for (ActivityManager.RunningAppProcessInfo app : listTmp) {
            if (appInfo.mUid == app.uid && app.pkgList != null) {
                String[] strArr = app.pkgList;
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String str = strArr[i];
                    if (appInfo.mPkgName.equals(str)) {
                        HiberProcInfoList.add(new HiberAppInfo(app.uid, str, app.pid, app.processName));
                        break;
                    }
                    i++;
                }
            }
        }
        if (HiberProcInfoList.isEmpty()) {
            HiberProcInfoList = null;
        }
        return HiberProcInfoList;
    }

    public static boolean illegalHiberAppInfoArraySet(ArraySet<HiberAppInfo> tmp) {
        return tmp == null || tmp.isEmpty();
    }

    public static boolean illegalProcessInfo(ProcessInfo process) {
        if (process == null || process.mPackageName == null || 1 != process.mPackageName.size()) {
            return true;
        }
        return false;
    }

    public static boolean illegalUid(int uid) {
        if (MemoryConstant.getReclaimEnhanceSwitch() || uid >= 10000) {
            return false;
        }
        return true;
    }

    public static boolean isStrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static boolean isSameProc(HiberAppInfo curProcess, HiberAppInfo hisProcess) {
        return curProcess.mPid == hisProcess.mPid && curProcess.mUid == hisProcess.mUid;
    }

    private static int[] intLitToArray(List<Integer> pidList) {
        if (pidList == null || pidList.isEmpty()) {
            return AppHibernateCst.EMPTY_INT_ARRAY;
        }
        int size = pidList.size();
        int[] pidArray = new int[size];
        for (int i = 0; i < size; i++) {
            pidArray[i] = pidList.get(i).intValue();
        }
        return pidArray;
    }

    public static int[] getPidsFromList(ArraySet<HiberAppInfo> currentChildList) {
        List<Integer> pidList = new ArrayList<>();
        Iterator<HiberAppInfo> it = currentChildList.iterator();
        while (it.hasNext()) {
            HiberAppInfo curProcess = it.next();
            if (curProcess != null) {
                pidList.add(Integer.valueOf(curProcess.mPid));
            }
        }
        return intLitToArray(pidList);
    }

    public static int[] getDiffPidArray(ArraySet<HiberAppInfo> hisChildList, ArraySet<HiberAppInfo> currentChildList) {
        if (illegalHiberAppInfoArraySet(hisChildList)) {
            return getPidsFromList(currentChildList);
        }
        List<Integer> pidList = new ArrayList<>();
        long hisReclaimTime = 0;
        Iterator<HiberAppInfo> it = currentChildList.iterator();
        while (it.hasNext()) {
            HiberAppInfo curProcess = it.next();
            boolean needAdd = true;
            Iterator<HiberAppInfo> it2 = hisChildList.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                HiberAppInfo hisProcess = it2.next();
                if (isSameProc(curProcess, hisProcess)) {
                    hisReclaimTime = hisProcess.mReclaimTime;
                    needAdd = SystemClock.uptimeMillis() - hisReclaimTime >= AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME;
                }
            }
            if (needAdd) {
                pidList.add(Integer.valueOf(curProcess.mPid));
            } else {
                curProcess.mReclaimTime = hisReclaimTime;
            }
        }
        return intLitToArray(pidList);
    }

    public static boolean illegalAbsAppInfo(AbsAppInfo absApp) {
        return absApp == null || illegalUid(absApp.mUid) || isStrEmpty(absApp.mPkgName);
    }

    public static boolean isTheSameAppUnderMultiUser(String curPkg, AbsAppInfo srcApp) {
        boolean z = false;
        if (isStrEmpty(curPkg) || srcApp == null || !curPkg.equals(srcApp.mPkgName)) {
            return false;
        }
        int sysUserId = 0;
        ApplicationInfo appInfo = null;
        UserInfo ui = null;
        try {
            ui = ActivityManagerNative.getDefault().getCurrentUser();
        } catch (RemoteException e) {
            AwareLog.e("AppHiber_Util", "Couldn't get current user ID; guessing it's " + 0);
        }
        if (ui != null) {
            sysUserId = ui.id;
        }
        if (UserHandle.getUserId(srcApp.mUid) != sysUserId) {
            return false;
        }
        try {
            appInfo = AppGlobals.getPackageManager().getApplicationInfo(curPkg, 0, sysUserId);
        } catch (RemoteException e2) {
            AwareLog.e("AppHiber_Util", "getApplicationInfo RemoteException");
        }
        if (appInfo != null && appInfo.uid == srcApp.mUid) {
            z = true;
        }
        return z;
    }
}
