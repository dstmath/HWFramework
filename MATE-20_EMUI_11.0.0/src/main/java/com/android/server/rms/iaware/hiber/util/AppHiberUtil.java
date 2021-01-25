package com.android.server.rms.iaware.hiber.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.iaware.hiber.bean.AbsAppInfo;
import com.android.server.rms.iaware.hiber.bean.HiberAppInfo;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.BigMemoryConstant;
import com.huawei.android.app.ActivityManagerNativeExt;
import com.huawei.android.content.pm.IPackageManagerEx;
import com.huawei.android.content.pm.UserInfoExAdapter;
import com.huawei.android.os.UserHandleEx;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppHiberUtil {
    private static final int SINGLE_ONE = 1;
    private static final String TAG = "AppHiber_Util";

    public static ArraySet<HiberAppInfo> getHiberProcInfoListByAbsAppInfo(Context cxt, AbsAppInfo appInfo) {
        List<ActivityManager.RunningAppProcessInfo> listTmp;
        ArraySet<HiberAppInfo> hiberProcInfoList = new ArraySet<>();
        if (cxt == null || appInfo == null) {
            return hiberProcInfoList;
        }
        Object obj = cxt.getSystemService(BigMemoryConstant.BIG_MEM_INFO_ITEM_TAG);
        if (!(obj instanceof ActivityManager) || (listTmp = ((ActivityManager) obj).getRunningAppProcesses()) == null || listTmp.isEmpty()) {
            return hiberProcInfoList;
        }
        for (ActivityManager.RunningAppProcessInfo app : listTmp) {
            if (appInfo.uid == app.uid && app.pkgList != null) {
                String[] strArr = app.pkgList;
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String str = strArr[i];
                    if (appInfo.pkgName.equals(str)) {
                        hiberProcInfoList.add(new HiberAppInfo(app.uid, str, app.pid, app.processName));
                        break;
                    }
                    i++;
                }
            }
        }
        return hiberProcInfoList;
    }

    public static boolean illegalHiberAppInfoArraySet(ArraySet<HiberAppInfo> tmp) {
        return tmp == null || tmp.isEmpty();
    }

    public static boolean illegalProcessInfo(ProcessInfo process) {
        if (process == null || process.mPackageName == null || process.mPackageName.size() != 1) {
            return true;
        }
        return false;
    }

    public static boolean illegalUid(int uid) {
        if (uid < 10000) {
            return true;
        }
        return false;
    }

    public static boolean isStrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static boolean isSameProc(HiberAppInfo curProcess, HiberAppInfo hisProcess) {
        return curProcess.pid == hisProcess.pid && curProcess.uid == hisProcess.uid;
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
                pidList.add(Integer.valueOf(curProcess.pid));
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
                    hisReclaimTime = hisProcess.reclaimTime;
                    needAdd = SystemClock.uptimeMillis() - hisReclaimTime >= AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME;
                }
            }
            if (needAdd) {
                pidList.add(Integer.valueOf(curProcess.pid));
            } else {
                curProcess.reclaimTime = hisReclaimTime;
            }
        }
        return intLitToArray(pidList);
    }

    public static boolean illegalAbsAppInfo(AbsAppInfo absApp) {
        return absApp == null || illegalUid(absApp.uid) || isStrEmpty(absApp.pkgName);
    }

    public static boolean isTheSameAppUnderMultiUser(String curPkg, AbsAppInfo srcApp) {
        if (isStrEmpty(curPkg) || srcApp == null || !curPkg.equals(srcApp.pkgName)) {
            return false;
        }
        int sysUserId = 0;
        UserInfoExAdapter ui = null;
        try {
            ui = ActivityManagerNativeExt.getCurrentUser();
        } catch (RemoteException e) {
            AwareLog.e(TAG, "Couldn't get current user ID; guessing it's 0");
        }
        if (ui != null) {
            sysUserId = ui.getUserId();
        }
        if (UserHandleEx.getUserId(srcApp.uid) != sysUserId) {
            return false;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = IPackageManagerEx.getApplicationInfo(curPkg, 0, sysUserId);
        } catch (RemoteException e2) {
            AwareLog.e(TAG, "getApplicationInfo RemoteException");
        }
        if (appInfo != null && appInfo.uid == srcApp.uid) {
            return true;
        }
        return false;
    }
}
