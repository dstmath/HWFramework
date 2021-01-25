package com.android.server.rms.dump;

import android.os.SystemClock;
import android.rms.iaware.AppTypeRecoManager;
import com.android.server.rms.algorithm.ActivityTopManagerRt;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.ActivityEventManager;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DumpAwareUserHabit {
    private static final String DISABLE_DEBUG = "disable_debug";
    private static final String ENABLE_DEBUG = "enable_debug";
    private static final String GET_ALL_TOP_LIST = "getAllTopList";
    private static final String GET_APP_LIST_BY_TYPE = "getAppListByType";
    private static final String GET_APP_TYPE = "getAppType";
    private static final String GET_CLOCK_TYPE_APP_LIST = "getClockTypeAppList";
    private static final String GET_FILTER_APP = "getFilterApp";
    private static final String GET_GCM_APP_LIST = "getGCMAppList";
    private static final String GET_HABIT_PROTECT_LIST = "getHabitProtectList";
    private static final String GET_HABIT_PROTECT_LIST_ALL = "getHabitProtectListAll";
    private static final String GET_LAST_PKG_NAME = "getLastPkgName";
    private static final String GET_LONG_TIME_RUNNING_APPS = "getLongTimeRunningApps";
    private static final String GET_LRU_CACHE = "getLruCache";
    private static final String GET_MOST_FREQ_APP_BY_TYPE = "getMostFreqAppByType";
    private static final String GET_MOST_FREQ_APP_BY_TYPE_EX = "getMostFreqAppByTypeEx";
    private static final String GET_MOST_USED = "getMostUsed";
    private static final String GET_TOP_IM_LIST = "getTopIMList";
    private static final String GET_TOP_N = "getTopN";
    private static final String HABIT_PROTECT_LIST = "habitProtectList";
    private static final int MINCOUNT = 0;
    private static final String PG_PROTECT_LIST = "PGProtectList";
    private static final String SCENE_INFO = "scene_info";
    private static final String TAG = "AwareUserHabit";
    private static final int TOPMAX = 10000;
    private static final int TOPN = 10;
    private static final String TOP_ACTIVITY = "topActivity";
    private static final String USER_TRACK = "userTrack";

    public static void dumpAwareUserHabit(PrintWriter pw, String[] args) {
        AwareUserHabit userHabit = AwareUserHabit.getInstance();
        if (checkParamValid(pw, args, userHabit)) {
            boolean isGetPGProtectList = false;
            boolean isGetAppListByType = false;
            boolean isGetAppType = false;
            boolean isGetTopIMList = false;
            for (String arg : args) {
                if (!dumpForceProtectApps(arg, pw, isGetPGProtectList, userHabit) && !dumpTopActivityAndSceneInfo(arg, pw) && !dumpUserTrackAndProcetList(arg, pw, userHabit) && !dumpHabitResult(arg, pw, userHabit) && !dumpTopIMList(arg, pw, isGetTopIMList, args) && !dumpHabitResultEx(arg, pw, isGetAppListByType, isGetAppType)) {
                    if (PG_PROTECT_LIST.equals(arg)) {
                        isGetPGProtectList = true;
                    } else if (GET_APP_LIST_BY_TYPE.equals(arg)) {
                        isGetAppListByType = true;
                    } else if (GET_APP_TYPE.equals(arg)) {
                        isGetAppType = true;
                    } else if (GET_TOP_IM_LIST.equals(arg)) {
                        isGetTopIMList = true;
                    } else if (dumpAwareUserHabitEx(pw, args, arg, userHabit)) {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    private static boolean checkParamValid(PrintWriter pw, String[] args, AwareUserHabit userHabit) {
        if (pw == null || args == null) {
            return false;
        }
        if (userHabit == null) {
            pw.println("user habit is not ready");
            return false;
        } else if (userHabit.isEnable()) {
            return true;
        } else {
            pw.println("user habit is not enable");
            return false;
        }
    }

    private static boolean dumpAwareUserHabitEx(PrintWriter pw, String[] args, String arg, AwareUserHabit userHabit) {
        if (GET_FILTER_APP.equals(arg)) {
            Set<String> result = userHabit.getFilterApp();
            if (result != null) {
                pw.println(result);
            } else {
                pw.println("result is null");
            }
            return true;
        } else if (GET_GCM_APP_LIST.equals(arg)) {
            List<String> result2 = userHabit.getGCMAppList();
            if (result2 != null) {
                pw.println(result2);
            } else {
                pw.println("result is null");
            }
            return true;
        } else if (GET_MOST_FREQ_APP_BY_TYPE.equals(arg)) {
            printGetMostFreqAppByType(userHabit, pw, args);
            return true;
        } else if (GET_MOST_FREQ_APP_BY_TYPE_EX.equals(arg)) {
            userHabit.dumpMostFreqAppByTypeEx(pw, args);
            return true;
        } else if (!GET_LRU_CACHE.equals(arg)) {
            return false;
        } else {
            LinkedHashMap<String, Long> result3 = userHabit.getLruCache();
            if (result3 != null) {
                long now = SystemClock.elapsedRealtime();
                StringBuffer strBuffer = new StringBuffer();
                strBuffer.append("pkgName:      backgroundTime:");
                strBuffer.append(System.lineSeparator());
                for (Map.Entry<String, Long> entry : result3.entrySet()) {
                    strBuffer.append(entry.getKey());
                    strBuffer.append("    ");
                    strBuffer.append(String.valueOf((now - entry.getValue().longValue()) / 1000));
                    strBuffer.append("s ");
                    strBuffer.append(System.lineSeparator());
                }
                pw.println(strBuffer.toString());
                return true;
            }
            pw.println("result is null");
            return true;
        }
    }

    private static void printgetAppListByType(PrintWriter pw, String arg) {
        try {
            Set<String> result = AppTypeRecoManager.getInstance().getAppsByType(Integer.parseInt(arg));
            if (result.size() > 0) {
                pw.println(result.toString());
            } else {
                pw.println("invald input value or not used data");
            }
        } catch (NumberFormatException e) {
            pw.println("Bad input value: " + arg);
        }
    }

    private static void printGetMostFreqAppByType(AwareUserHabit userHabit, PrintWriter pw, String[] args) {
        int appType = 0;
        int appNum = -1;
        if (args.length > 2) {
            try {
                appType = Integer.parseInt(args[args.length - 2]);
                try {
                    appNum = Integer.parseInt(args[args.length - 1]);
                } catch (NumberFormatException e) {
                    pw.println("Bad input value param2: " + Arrays.toString(args));
                    return;
                }
            } catch (NumberFormatException e2) {
                pw.println("Bad input value param1: " + Arrays.toString(args));
                return;
            }
        }
        List<String> result = userHabit.getMostFreqAppByType(appType, appNum);
        if (result != null) {
            pw.println(result.toString());
        }
    }

    private static void printGetTopIMList(PrintWriter pw, String[] args) {
        int topN = 0;
        if (args.length > 1) {
            try {
                topN = Integer.parseInt(args[args.length - 1]);
            } catch (NumberFormatException e) {
                pw.println("Bad input value param");
                return;
            }
        }
        List<String> result = AppTypeRecoManager.getInstance().dumpTopImList(topN);
        if (result != null) {
            pw.println(result.toString());
        }
    }

    private static boolean dumpTopActivityAndSceneInfo(String arg, PrintWriter pw) {
        if (TOP_ACTIVITY.equals(arg)) {
            ActivityTopManagerRt habit = ActivityTopManagerRt.obtainExistInstance();
            if (habit == null) {
                return true;
            }
            List<String> result = habit.getTopActivityDumpInfo();
            if (result != null) {
                pw.println(result.toString());
            } else {
                pw.println("result is null");
            }
            return true;
        } else if (ENABLE_DEBUG.equals(arg)) {
            ActivityEventManager.getInstance().enableDebug();
            return true;
        } else if (DISABLE_DEBUG.equals(arg)) {
            ActivityEventManager.getInstance().disableDebug();
            return true;
        } else if (!SCENE_INFO.equals(arg)) {
            return false;
        } else {
            ActivityEventManager.getInstance().dumpSceneInfo(pw);
            return true;
        }
    }

    private static boolean dumpForceProtectApps(String arg, PrintWriter pw, boolean isGetPGProtectList, AwareUserHabit userHabit) {
        if (!isGetPGProtectList) {
            return false;
        }
        try {
            List<String> result = userHabit.getForceProtectApps(Integer.parseInt(arg));
            if (result == null || result.size() <= 0) {
                pw.println("invald input value or not used data");
            } else {
                pw.println(result.toString());
            }
            return true;
        } catch (NumberFormatException e) {
            pw.println("Bad input value: " + arg);
            return true;
        }
    }

    private static boolean dumpUserTrackAndProcetList(String arg, PrintWriter pw, AwareUserHabit userHabit) {
        if (USER_TRACK.equals(arg)) {
            Map<String, String> result = userHabit.getUserTrackAppSortDumpInfo();
            if (result != null) {
                pw.println(result.toString());
            } else {
                pw.println("result is null");
            }
            return true;
        } else if (HABIT_PROTECT_LIST.equals(arg)) {
            userHabit.dumpHabitProtectList(pw);
            return true;
        } else if (GET_HABIT_PROTECT_LIST.equals(arg)) {
            List<String> result2 = userHabit.getHabitProtectList(10000, 10000);
            if (result2 != null) {
                pw.println(result2.toString());
            } else {
                pw.println("result is null");
            }
            return true;
        } else if (!GET_HABIT_PROTECT_LIST_ALL.equals(arg)) {
            return false;
        } else {
            List<String> result3 = userHabit.getHabitProtectListAll(10000, 10000);
            if (result3 != null) {
                pw.println(result3.toString());
            } else {
                pw.println("result is null");
            }
            return true;
        }
    }

    private static boolean dumpHabitResult(String arg, PrintWriter pw, AwareUserHabit userHabit) {
        if (GET_MOST_USED.equals(arg)) {
            List<String> result = userHabit.getMostFrequentUsedApp(10, 0);
            if (result != null) {
                pw.println(result.toString());
            } else {
                pw.println("result is null");
            }
            return true;
        } else if (GET_TOP_N.equals(arg)) {
            List<String> result2 = userHabit.getTopN(10);
            if (result2 != null) {
                pw.println(result2.toString());
            } else {
                pw.println("result is null");
            }
            return true;
        } else if (GET_ALL_TOP_LIST.equals(arg)) {
            Map<String, Integer> result3 = userHabit.getAllTopList();
            if (result3 != null) {
                pw.println(result3.toString());
            } else {
                pw.println("result is null");
            }
            return true;
        } else if (GET_LAST_PKG_NAME.equals(arg)) {
            pw.println(userHabit.getLastPkgName());
            return true;
        } else if (!GET_LONG_TIME_RUNNING_APPS.equals(arg)) {
            return false;
        } else {
            List<String> result4 = userHabit.recognizeLongTimeRunningApps();
            if (result4 != null) {
                pw.println(result4);
            } else {
                pw.println("all the apps are used recently");
            }
            return true;
        }
    }

    private static boolean dumpTopIMList(String arg, PrintWriter pw, boolean isGetTopIMList, String[] args) {
        if (!isGetTopIMList) {
            return false;
        }
        printGetTopIMList(pw, args);
        return true;
    }

    private static boolean dumpHabitResultEx(String arg, PrintWriter pw, boolean isGetAppListByType, boolean isGetAppType) {
        if (GET_CLOCK_TYPE_APP_LIST.equals(arg)) {
            Set<String> result = AppTypeRecoManager.getInstance().getAppsByType(5);
            result.addAll(AppTypeRecoManager.getInstance().getAppsByType(310));
            pw.println(result);
            return true;
        }
        if (isGetAppListByType) {
            printgetAppListByType(pw, arg);
        }
        if (!isGetAppType) {
            return false;
        }
        int type = AppTypeRecoManager.getInstance().getAppType(arg);
        int atti = AppTypeRecoManager.getInstance().getAppAttribute(arg);
        pw.println("pkgname:" + arg + " type:" + type + " atti:" + atti);
        return true;
    }
}
