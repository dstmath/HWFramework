package com.huawei.systemmanager.power;

import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import java.util.ArrayList;
import java.util.List;

public class HwBatteryStatsManager {
    private static List<HwBatterySipper> getBatterySipperList(IBatteryStats hwBatteryStats, Context ctx, boolean reload, int statstype) {
        if (hwBatteryStats == null || ctx == null) {
            return null;
        }
        BatteryStatsHelper batteryStatsHelper = hwBatteryStats.getInnerBatteryStatsHelper();
        synchronized (batteryStatsHelper) {
            if (reload) {
                try {
                    batteryStatsHelper.clearStats();
                } catch (Throwable th) {
                    throw th;
                }
            }
            UserManager uMgr = (UserManager) ctx.getSystemService("user");
            List<HwBatterySipper> batterysipper = new ArrayList<>();
            if (uMgr == null) {
                return batterysipper;
            }
            List<UserHandle> userProfiles = uMgr.getUserProfiles();
            List<BatterySipper> usaglist = batteryStatsHelper.getUsageList();
            if (userProfiles != null) {
                if (usaglist != null) {
                    batteryStatsHelper.refreshStats(statstype, userProfiles);
                    for (BatterySipper sipper : usaglist) {
                        batterysipper.add(new HwBatterySipper(sipper));
                    }
                    return batterysipper;
                }
            }
            ArrayList arrayList = new ArrayList();
            return arrayList;
        }
    }

    public static List<HwBatterySipper> getBatterySipperListSinceUnplugged(IBatteryStats hwBatteryStats, Context ctx, boolean reload) {
        return getBatterySipperList(hwBatteryStats, ctx, reload, 2);
    }

    public static List<HwBatterySipper> getBatterySipperListSinceChanged(IBatteryStats hwBatteryStatsImpl, Context ctx, boolean reload) {
        return getBatterySipperList(hwBatteryStatsImpl, ctx, reload, 0);
    }

    public static IBatteryStats getIBatteryStats(Context context, boolean collectBatteryBroadcast) {
        return HwBatteryStatsImpl.get(context, collectBatteryBroadcast);
    }

    public static IHwPowerProfile getIHwPowerProfile(Context context) {
        return HwBatteryStatsImpl.get(context, true).getIHwPowerProfile();
    }

    public static IHwPGSdk getIHwPGSdk() {
        return HwPGSdkImpl.getInstance();
    }

    public static void adjustBackupgroundResultInSipper(HwBatterySipper sipper, double uidpower) {
        if (sipper != null) {
            sipper.getBatterySipper().totalPowerMah = uidpower;
            sipper.getBatterySipper().cpuTimeMs -= sipper.getBatterySipper().cpuFgTimeMs;
            sipper.getBatterySipper().cpuFgTimeMs = 0;
        }
    }
}
