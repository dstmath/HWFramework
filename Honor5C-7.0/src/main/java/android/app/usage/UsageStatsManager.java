package android.app.usage;

import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class UsageStatsManager {
    public static final int INTERVAL_BEST = 4;
    public static final int INTERVAL_COUNT = 4;
    public static final int INTERVAL_DAILY = 0;
    public static final int INTERVAL_MONTHLY = 2;
    public static final int INTERVAL_WEEKLY = 1;
    public static final int INTERVAL_YEARLY = 3;
    private static final UsageEvents sEmptyResults = null;
    private final Context mContext;
    private final IUsageStatsManager mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.usage.UsageStatsManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.usage.UsageStatsManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.usage.UsageStatsManager.<clinit>():void");
    }

    public UsageStatsManager(Context context, IUsageStatsManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public List<UsageStats> queryUsageStats(int intervalType, long beginTime, long endTime) {
        try {
            ParceledListSlice<UsageStats> slice = this.mService.queryUsageStats(intervalType, beginTime, endTime, this.mContext.getOpPackageName());
            if (slice != null) {
                return slice.getList();
            }
        } catch (RemoteException e) {
        }
        return Collections.emptyList();
    }

    public List<ConfigurationStats> queryConfigurations(int intervalType, long beginTime, long endTime) {
        try {
            ParceledListSlice<ConfigurationStats> slice = this.mService.queryConfigurationStats(intervalType, beginTime, endTime, this.mContext.getOpPackageName());
            if (slice != null) {
                return slice.getList();
            }
        } catch (RemoteException e) {
        }
        return Collections.emptyList();
    }

    public UsageEvents queryEvents(long beginTime, long endTime) {
        try {
            UsageEvents iter = this.mService.queryEvents(beginTime, endTime, this.mContext.getOpPackageName());
            if (iter != null) {
                return iter;
            }
        } catch (RemoteException e) {
        }
        return sEmptyResults;
    }

    public Map<String, UsageStats> queryAndAggregateUsageStats(long beginTime, long endTime) {
        List<UsageStats> stats = queryUsageStats(INTERVAL_COUNT, beginTime, endTime);
        if (stats.isEmpty()) {
            return Collections.emptyMap();
        }
        ArrayMap<String, UsageStats> aggregatedStats = new ArrayMap();
        int statCount = stats.size();
        for (int i = INTERVAL_DAILY; i < statCount; i += INTERVAL_WEEKLY) {
            UsageStats newStat = (UsageStats) stats.get(i);
            UsageStats existingStat = (UsageStats) aggregatedStats.get(newStat.getPackageName());
            if (existingStat == null) {
                aggregatedStats.put(newStat.mPackageName, newStat);
            } else {
                existingStat.add(newStat);
            }
        }
        return aggregatedStats;
    }

    public boolean isAppInactive(String packageName) {
        try {
            return this.mService.isAppInactive(packageName, UserHandle.myUserId());
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setAppInactive(String packageName, boolean inactive) {
        try {
            this.mService.setAppInactive(packageName, inactive, UserHandle.myUserId());
        } catch (RemoteException e) {
        }
    }

    public void whitelistAppTemporarily(String packageName, long duration, UserHandle user) {
        try {
            this.mService.whitelistAppTemporarily(packageName, duration, user.getIdentifier());
        } catch (RemoteException e) {
        }
    }

    public void onCarrierPrivilegedAppsChanged() {
        try {
            this.mService.onCarrierPrivilegedAppsChanged();
        } catch (RemoteException e) {
        }
    }
}
