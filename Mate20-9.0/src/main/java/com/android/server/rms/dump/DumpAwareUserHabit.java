package com.android.server.rms.dump;

import android.os.SystemClock;
import android.rms.iaware.AppTypeRecoManager;
import com.android.server.rms.algorithm.AwareUserHabit;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DumpAwareUserHabit {
    private static final int MINCOUNT = 3;
    private static final String TAG = "AwareUserHabit";
    private static final int TOPMAX = 10000;
    private static final int TOPN = 5;

    public static final void dumpAwareUserHabit(PrintWriter pw, String[] args) {
        PrintWriter printWriter = pw;
        String[] strArr = args;
        if (printWriter != null && strArr != null) {
            AwareUserHabit userHabit = AwareUserHabit.getInstance();
            if (userHabit == null) {
                printWriter.println("user habit is not ready");
            } else if (!userHabit.isEnable()) {
                printWriter.println("user habit is not enable");
            } else {
                int length = strArr.length;
                boolean isGetTopIMList = false;
                boolean isGetAppType = false;
                boolean isGetAppListByType = false;
                boolean isGetPGProtectList = false;
                int i = 0;
                while (i < length) {
                    String arg = strArr[i];
                    if (isGetPGProtectList) {
                        try {
                            List<String> result = userHabit.getForceProtectApps(Integer.parseInt(arg));
                            if (result == null || result.size() <= 0) {
                                printWriter.println("invald input value or not used data");
                            } else {
                                printWriter.println(result.toString());
                            }
                            return;
                        } catch (NumberFormatException e) {
                            NumberFormatException numberFormatException = e;
                            printWriter.println("Bad input value: " + arg);
                            return;
                        }
                    } else if ("userTrack".equals(arg)) {
                        Map<String, String> result2 = userHabit.getUserTrackAppSortDumpInfo();
                        if (result2 != null) {
                            printWriter.println(result2.toString());
                        } else {
                            printWriter.println("result is null");
                        }
                        return;
                    } else if ("habitProtectList".equals(arg)) {
                        userHabit.dumpHabitProtectList(printWriter);
                        return;
                    } else if ("getHabitProtectList".equals(arg)) {
                        List<String> result3 = userHabit.getHabitProtectList(10000, 10000);
                        if (result3 != null) {
                            printWriter.println(result3.toString());
                        } else {
                            printWriter.println("result is null");
                        }
                        return;
                    } else if ("getHabitProtectListAll".equals(arg)) {
                        List<String> result4 = userHabit.getHabitProtectListAll(10000, 10000);
                        if (result4 != null) {
                            printWriter.println(result4.toString());
                        } else {
                            printWriter.println("result is null");
                        }
                        return;
                    } else if ("getMostUsed".equals(arg)) {
                        List<String> result5 = userHabit.getMostFrequentUsedApp(5, 3);
                        if (result5 != null) {
                            printWriter.println(result5.toString());
                        } else {
                            printWriter.println("result is null");
                        }
                        return;
                    } else if ("getTopN".equals(arg)) {
                        List<String> result6 = userHabit.getTopN(5);
                        if (result6 != null) {
                            printWriter.println(result6.toString());
                        } else {
                            printWriter.println("result is null");
                        }
                        return;
                    } else if ("getAllTopList".equals(arg)) {
                        Map<String, Integer> result7 = userHabit.getAllTopList();
                        if (result7 != null) {
                            printWriter.println(result7.toString());
                        } else {
                            printWriter.println("result is null");
                        }
                        return;
                    } else if ("getLastPkgName".equals(arg)) {
                        printWriter.println(userHabit.getLastPkgName());
                        return;
                    } else {
                        if ("PGProtectList".equals(arg)) {
                            isGetPGProtectList = true;
                        } else if ("getLongTimeRunningApps".equals(arg)) {
                            List<String> result8 = userHabit.recognizeLongTimeRunningApps();
                            if (result8 != null) {
                                printWriter.println(result8);
                            } else {
                                printWriter.println("all the apps are used recently");
                            }
                            return;
                        } else if ("getLruCache".equals(arg)) {
                            LinkedHashMap<String, Long> result9 = userHabit.getLruCache();
                            if (result9 != null) {
                                long now = SystemClock.elapsedRealtime();
                                StringBuffer s = new StringBuffer();
                                s.append("pkgName:      backgroundTime:\n");
                                for (Map.Entry entry : result9.entrySet()) {
                                    s.append((String) entry.getKey());
                                    s.append("    ");
                                    s.append(String.valueOf((now - ((Long) entry.getValue()).longValue()) / 1000));
                                    s.append("s \n");
                                    now = now;
                                    result9 = result9;
                                }
                                long j = now;
                                printWriter.println(s.toString());
                            } else {
                                printWriter.println("result is null");
                            }
                            return;
                        } else if ("getClockTypeAppList".equals(arg)) {
                            printWriter.println(AppTypeRecoManager.getInstance().getAlarmApps());
                            return;
                        } else if ("getAppListByType".equals(arg)) {
                            isGetAppListByType = true;
                        } else {
                            if (isGetAppListByType) {
                                printgetAppListByType(printWriter, arg);
                            }
                            if ("getAppType".equals(arg)) {
                                isGetAppType = true;
                            } else if (isGetAppType) {
                                int type = AppTypeRecoManager.getInstance().getAppType(arg);
                                printWriter.println("pkgname:" + arg + " type:" + type);
                                return;
                            } else if (!dumpAwareUserHabitEx(printWriter, strArr, arg, userHabit)) {
                                if ("getTopIMList".equals(arg)) {
                                    isGetTopIMList = true;
                                } else if (isGetTopIMList) {
                                    printGetTopIMList(pw, args);
                                    return;
                                }
                            } else {
                                return;
                            }
                        }
                        i++;
                    }
                }
            }
        }
    }

    private static final boolean dumpAwareUserHabitEx(PrintWriter pw, String[] args, String arg, AwareUserHabit userHabit) {
        if ("getFilterApp".equals(arg)) {
            Set<String> result = userHabit.getFilterApp();
            if (result != null) {
                pw.println(result);
            } else {
                pw.println("result is null");
            }
            return true;
        } else if ("getGCMAppList".equals(arg)) {
            List<String> result2 = userHabit.getGCMAppList();
            if (result2 != null) {
                pw.println(result2);
            } else {
                pw.println("result is null");
            }
            return true;
        } else if ("getMostFreqAppByType".equals(arg)) {
            printGetMostFreqAppByType(userHabit, pw, args);
            return true;
        } else if (!"getMostFreqAppByTypeEx".equals(arg)) {
            return false;
        } else {
            userHabit.dumpMostFreqAppByTypeEx(pw, args);
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
                pw.println("Bad input value param1: " + Arrays.toString(args));
                return;
            }
        }
        List<String> result = AppTypeRecoManager.getInstance().dumpTopIMList(topN);
        if (result != null) {
            pw.println(result.toString());
        }
    }
}
