package com.huawei.hiai.awareness.common;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.hiai.awareness.common.log.LogUtil;
import com.huawei.msdp.movement.HwMSDPOtherParameters;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {
    public static final String CA_PACKAGE_NAME = "com.huawei.hiai";
    public static final String MSDP_PACKAGE_NAME = "com.huawei.msdp";
    public static final String TAG = "";
    private static boolean mCAInstalled = false;
    private static boolean mIsMsdpInstalled = false;

    public static boolean isEmpty(String s) {
        if (Build.VERSION.SDK_INT < 9) {
            return false;
        }
        if (s == null || s.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean checkApkExist(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName) || context == null) {
            return false;
        }
        try {
            if (context.getPackageManager().getApplicationInfo(packageName, AwarenessConstants.POWER_MODE_CHANGED_ACTION) != null) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e("", "checkApkExist false e = " + e);
            return false;
        }
    }

    public static synchronized boolean checkCAInstalled(Context context) {
        boolean z;
        synchronized (Utils.class) {
            if (mCAInstalled) {
                z = true;
            } else if (context == null) {
                LogUtil.e("", "checkMsdpInstalled() context = null");
                z = false;
            } else {
                mCAInstalled = checkApkExist(context, "com.huawei.hiai");
                z = mCAInstalled;
            }
        }
        return z;
    }

    public static synchronized boolean checkMsdpInstalled(Context context) {
        boolean z;
        synchronized (Utils.class) {
            if (mIsMsdpInstalled) {
                z = true;
            } else if (context == null) {
                LogUtil.e("", "checkMsdpInstalled() context = null");
                z = false;
            } else {
                mIsMsdpInstalled = checkApkExist(context, MSDP_PACKAGE_NAME);
                z = mIsMsdpInstalled;
            }
        }
        return z;
    }

    public static HwMSDPOtherParameters getHwMSDPOtherParametersbyString(String hwMSDPOtherParameters) {
        LogUtil.d("", "enter into getHwMSDPOtherParametersbyString");
        if (hwMSDPOtherParameters == null || hwMSDPOtherParameters.length() == 0) {
            LogUtil.e("", "getHwMSDPOtherParametersbyString arrayString is empty!");
            return null;
        }
        String[] allStr = hwMSDPOtherParameters.split(",");
        if (5 != allStr.length) {
            LogUtil.e("", "getHwMSDPOtherParametersbyString arrayString is lenth error!");
            return null;
        }
        try {
            return new HwMSDPOtherParameters(Double.parseDouble(allStr[0].split("=")[1]), Double.parseDouble(allStr[1].split("=")[1]), Double.parseDouble(allStr[2].split("=")[1]), Double.parseDouble(allStr[3].split("=")[1]), allStr[4].replace("Param5=", ""));
        } catch (NumberFormatException e) {
            LogUtil.e("", "getHwMSDPOtherParametersbyString param is illegal!");
            return null;
        }
    }

    public static String actionSupport(int action, ConcurrentHashMap<String, Integer> map, String[] arrayString) {
        if (map == null || map.isEmpty() || arrayString == null || arrayString.length == 0) {
            LogUtil.e("", "actionSupport parameters illegal!");
            return null;
        }
        int length = arrayString.length;
        for (int i = 0; i < length; i++) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (arrayString[i] != null && arrayString[i].equals(entry.getKey()) && action == entry.getValue().intValue()) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public static String actionSupport(int action, ArrayMap<String, Integer> map, String[] arrayString) {
        if (map == null || map.isEmpty() || arrayString == null || arrayString.length == 0) {
            LogUtil.e("", "actionSupport parameters illegal!");
            return null;
        }
        int length = arrayString.length;
        for (int i = 0; i < length; i++) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (arrayString[i] != null && arrayString[i].equals(entry.getKey()) && action == entry.getValue().intValue()) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public static ArrayList getArrayListFromString(String strArrayList) {
        ArrayList arrayList = new ArrayList();
        if (strArrayList == null || strArrayList.length() == 0) {
            LogUtil.e("", "getArrayListFromString arrayString is empty!");
        } else {
            LogUtil.d("", "getArrayListFromString arrayString is strArrayList:" + strArrayList);
            String[] allStr = strArrayList.replace("[", "").replace("]", "").replace(" ", "").split(",");
            int length = allStr.length;
            LogUtil.d("", "getArrayListFromString arrayString is length:" + length);
            int i = 0;
            while (i < length) {
                try {
                    arrayList.add(Integer.valueOf(Integer.parseInt(allStr[i])));
                    i++;
                } catch (NumberFormatException e) {
                    LogUtil.e("", "getArrayListFromString param is illegal!");
                    arrayList.clear();
                }
            }
            LogUtil.d("", "getArrayListFromString,arrayList:" + arrayList.toString());
        }
        return arrayList;
    }

    public static void increaseRegisterTimes(String topKey, int action, ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> actionsRegistertimes) {
        if (topKey == null || actionsRegistertimes == null) {
            LogUtil.e("", "increaseRegisterTimes parameters illegal!");
        } else if (actionsRegistertimes.containsKey(topKey)) {
            ConcurrentHashMap<Integer, Integer> map = actionsRegistertimes.get(topKey);
            if (map.containsKey(Integer.valueOf(action))) {
                map.put(Integer.valueOf(action), Integer.valueOf(map.get(Integer.valueOf(action)).intValue() + 1));
            } else {
                map.put(Integer.valueOf(action), 1);
            }
        } else {
            ConcurrentHashMap<Integer, Integer> map2 = new ConcurrentHashMap<>();
            map2.put(Integer.valueOf(action), 1);
            actionsRegistertimes.putIfAbsent(topKey, map2);
        }
    }

    public static void decreaseRegisterTimes(String topKey, int action, ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> actionsRegistertimes) {
        int value = 0;
        if (topKey == null || actionsRegistertimes == null) {
            LogUtil.e("", "decreaseRegisterTimes parameters illegal!");
        } else if (actionsRegistertimes.containsKey(topKey)) {
            ConcurrentHashMap<Integer, Integer> map = actionsRegistertimes.get(topKey);
            if (map.containsKey(Integer.valueOf(action))) {
                if (map.get(Integer.valueOf(action)).intValue() - 1 > 0) {
                    value = map.get(Integer.valueOf(action)).intValue() - 1;
                }
                map.put(Integer.valueOf(action), Integer.valueOf(value));
                return;
            }
            map.put(Integer.valueOf(action), 0);
        }
    }

    public static Boolean isNeedUnregisterAction(int action, ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> actionsRegistertimes) {
        if (actionsRegistertimes == null) {
            LogUtil.e("", "isNeedUnregisterAction parameters illegal!");
            return true;
        }
        for (Map.Entry<String, ConcurrentHashMap<Integer, Integer>> entry : actionsRegistertimes.entrySet()) {
            ConcurrentHashMap<Integer, Integer> map = entry.getValue();
            if (!map.isEmpty() && map.containsKey(Integer.valueOf(action)) && map.get(Integer.valueOf(action)).intValue() > 0) {
                return false;
            }
        }
        return true;
    }

    public static void printActionsRegistertimesHashMap(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> actionsRegistertimes) {
        LogUtil.d("", "enter into printActionsRegistertimesHashMap");
        if (actionsRegistertimes == null || actionsRegistertimes.isEmpty()) {
            LogUtil.e("", "printActionsRegistertimesHashMap parameters illegal!");
            return;
        }
        for (Map.Entry<String, ConcurrentHashMap<Integer, Integer>> entry : actionsRegistertimes.entrySet()) {
            ConcurrentHashMap<Integer, Integer> map = entry.getValue();
            if (!map.isEmpty()) {
                for (Map.Entry<Integer, Integer> entry1 : map.entrySet()) {
                    LogUtil.d("", "printActionsRegistertimesHashMap: key:" + entry1.getKey() + ",value:" + entry1.getValue());
                }
            }
        }
    }

    public static ArrayList getArrayListFromTopKey(String topKey) {
        if (topKey == null || topKey.length() == 0) {
            LogUtil.e("", "getArrayListFromTopKey parameters illegal!");
            return null;
        }
        LogUtil.d("", "enter into getArrayListFromTopKey topKey:" + topKey);
        String fenceKey = topKey.split(";")[1];
        LogUtil.d("", "getArrayListFromTopKey fenceKey:" + fenceKey);
        ArrayList arrayList = new ArrayList();
        if (fenceKey.contains(AwarenessConstants.SECOND_ACTION_SPLITE_TAG)) {
            String[] splitfenceKey = fenceKey.split(AwarenessConstants.SECOND_ACTION_SPLITE_TAG);
            if (splitfenceKey.length != 2) {
                return null;
            }
            ArrayList arrayList2 = getArrayListFromString(splitfenceKey[1]);
            String[] allStr = splitfenceKey[0].split(",");
            if (allStr.length != 3) {
                return arrayList2;
            }
            try {
                arrayList2.add(Integer.valueOf(Integer.parseInt(allStr[2])));
                return arrayList2;
            } catch (NumberFormatException e) {
                LogUtil.e("", "getArrayListFromTopKey() NumberFormatException ");
                arrayList2.clear();
                return arrayList2;
            }
        } else {
            String[] allStr2 = fenceKey.split(",");
            if (allStr2.length != 3) {
                return arrayList;
            }
            try {
                arrayList.add(Integer.valueOf(Integer.parseInt(allStr2[2])));
                return arrayList;
            } catch (NumberFormatException e2) {
                LogUtil.e("", "getArrayListFromTopKey()  NumberFormatException ");
                arrayList.clear();
                return arrayList;
            }
        }
    }
}
