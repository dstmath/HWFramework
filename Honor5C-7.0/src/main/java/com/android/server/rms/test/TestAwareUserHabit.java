package com.android.server.rms.test;

import android.content.Context;
import android.os.SystemClock;
import com.android.server.rms.algorithm.AwareUserHabit;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import junit.framework.Assert;

public final class TestAwareUserHabit extends Assert {
    private static final int MINCOUNT = 3;
    private static final String TAG = "AwareUserHabit";
    private static final int TOPMAX = 10000;
    private static final int TOPN = 5;

    public static final void testAwareUserHabit(PrintWriter pw, Context context, String[] args) {
        AwareUserHabit userHabit = AwareUserHabit.getInstance();
        boolean isGetPGProtectList = false;
        boolean isGetAppType = false;
        if (userHabit == null) {
            pw.println("user habit is not ready");
        } else if (userHabit.isEnable()) {
            if (args != null) {
                for (String arg : args) {
                    List<String> result;
                    if (isGetPGProtectList) {
                        try {
                            result = userHabit.getForceProtectApps(Integer.parseInt(arg));
                            if (result == null || result.size() <= 0) {
                                pw.println("invald input value or not used data");
                            } else {
                                pw.println(result.toString());
                            }
                            return;
                        } catch (NumberFormatException e) {
                            pw.println("Bad input value: " + arg);
                            return;
                        }
                    }
                    if ("userTrack".equals(arg)) {
                        Map<String, String> result2 = userHabit.getUserTrackAppSortDumpInfo();
                        if (result2 != null) {
                            pw.println(result2.toString());
                        } else {
                            pw.println("result is null");
                        }
                        return;
                    }
                    if ("habitProtectList".equals(arg)) {
                        userHabit.dumpHabitProtectList(pw);
                        return;
                    }
                    if ("getHabitProtectList".equals(arg)) {
                        result = userHabit.getHabitProtectList(TOPMAX, TOPMAX);
                        if (result != null) {
                            pw.println(result.toString());
                        } else {
                            pw.println("result is null");
                        }
                        return;
                    }
                    if ("getHabitProtectListAll".equals(arg)) {
                        result = userHabit.getHabitProtectListAll(TOPMAX, TOPMAX);
                        if (result != null) {
                            pw.println(result.toString());
                        } else {
                            pw.println("result is null");
                        }
                        return;
                    }
                    if ("getMostUsed".equals(arg)) {
                        result = userHabit.getMostFrequentUsedApp(TOPN, MINCOUNT);
                        if (result != null) {
                            pw.println(result.toString());
                        } else {
                            pw.println("result is null");
                        }
                        return;
                    }
                    if ("getTopN".equals(arg)) {
                        result = userHabit.getTopN(TOPN);
                        if (result != null) {
                            pw.println(result.toString());
                        } else {
                            pw.println("result is null");
                        }
                        return;
                    }
                    if ("getLastPkgName".equals(arg)) {
                        pw.println(userHabit.getLastPkgName());
                        return;
                    }
                    if ("PGProtectList".equals(arg)) {
                        isGetPGProtectList = true;
                    } else {
                        if ("getLongTimeRunningApps".equals(arg)) {
                            result = userHabit.recognizeLongTimeRunningApps();
                            if (result != null) {
                                pw.println(result);
                            } else {
                                pw.println("all the apps are used recently");
                            }
                            return;
                        }
                        if ("getLruCache".equals(arg)) {
                            LinkedHashMap<String, Long> result3 = userHabit.getLruCache();
                            if (result3 != null) {
                                long now = SystemClock.elapsedRealtime();
                                StringBuffer s = new StringBuffer();
                                s.append("pkgName:      backgroundTime:\n");
                                for (Entry entry : result3.entrySet()) {
                                    s.append((String) entry.getKey()).append("    ").append(String.valueOf((now - ((Long) entry.getValue()).longValue()) / 1000)).append("s \n");
                                }
                                pw.println(s.toString());
                            } else {
                                pw.println("result is null");
                            }
                            return;
                        }
                        if ("getClockTypeAppList".equals(arg)) {
                            pw.println(userHabit.getAppListByType(10));
                            return;
                        }
                        Set<String> result4;
                        if ("getFilterApp".equals(arg)) {
                            result4 = userHabit.getFilterApp();
                            if (result4 != null) {
                                pw.println(result4);
                            } else {
                                pw.println("result is null");
                            }
                            return;
                        }
                        if ("getGCMAppList".equals(arg)) {
                            result = userHabit.getGCMAppList();
                            if (result != null) {
                                pw.println(result);
                            } else {
                                pw.println("result is null");
                            }
                            return;
                        }
                        if ("getAppListByType".equals(arg)) {
                            isGetAppType = true;
                        } else if (isGetAppType) {
                            try {
                                result4 = userHabit.getAppListByType(Integer.parseInt(arg));
                                if (result4.size() > 0) {
                                    pw.println(result4.toString());
                                } else {
                                    pw.println("invald input value or not used data");
                                }
                                return;
                            } catch (NumberFormatException e2) {
                                pw.println("Bad input value: " + arg);
                                return;
                            }
                        }
                    }
                }
            }
        } else {
            pw.println("user habit is not enable");
        }
    }
}
