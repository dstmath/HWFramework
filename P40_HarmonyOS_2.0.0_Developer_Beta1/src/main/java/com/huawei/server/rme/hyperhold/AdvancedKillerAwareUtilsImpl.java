package com.huawei.server.rme.hyperhold;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import com.android.server.mtm.iaware.appmng.appclean.CleanSource;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.iaware.memory.utils.MemoryUtils;
import com.huawei.server.rme.hyperhold.AdvancedKillerAwareUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedKillerAwareUtilsImpl extends AdvancedKillerAwareUtils {
    private final int IMPORTANT_SYS_APP_SPECIAL_WEIGHT = 2000;
    private final String IMPORTANT_SYS_APP_TAG = "important_sys_apps";
    private Context context;

    public AdvancedKillerAwareUtilsImpl(Context context2) {
        super(context2);
        this.context = context2;
    }

    public Map<String, AdvancedKillerPackageInfo> getPackageInfoMap(AdvancedKillerAwareUtils.MemoryLevelListType type) {
        String packageName;
        List<AwareProcessBlockInfo> procGroups = generatePackageList(type);
        if (procGroups == null || procGroups.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> importantSysApps = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.APP_CLEAN.getDesc(), "important_sys_apps");
        Map<String, AdvancedKillerPackageInfo> packageInfos = new HashMap<>();
        for (int i = 0; i < procGroups.size(); i++) {
            AwareProcessBlockInfo awareBlock = procGroups.get(i);
            if (type != AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_CACHED_PROCESS) {
                packageName = awareBlock.procPackageName;
            } else {
                packageName = getFirstProcessName(awareBlock);
            }
            List<AwareProcessInfo> awareProcessList = awareBlock.procProcessList;
            if (packageName != null && !packageName.isEmpty() && awareProcessList != null && !awareProcessList.isEmpty()) {
                if ((type == AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_0 || type == AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_1) && importantSysApps.contains(packageName)) {
                    awareBlock.procWeight = 2000;
                }
                packageInfos.put(packageName, new AdvancedKillerPackageInfoImpl(awareBlock));
            }
        }
        return packageInfos;
    }

    public long getMemAvailable() {
        return MemoryReader.getInstance().getMemAvailable();
    }

    public boolean setSchedPriority() {
        return CleanSource.setSchedPriority();
    }

    public void resetSchedPriority(boolean isProcFast) {
        CleanSource.resetSchedPriority(isProcFast);
    }

    public void beginKillFast() {
        ProcessCleaner.getInstance(this.context).beginKillFast();
    }

    public void endKillFast() {
        ProcessCleaner.getInstance(this.context).endKillFast();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.server.rme.hyperhold.AdvancedKillerAwareUtilsImpl$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKillerAwareUtils$MemoryLevelListType = new int[AdvancedKillerAwareUtils.MemoryLevelListType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKillerAwareUtils$MemoryLevelListType[AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_0.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKillerAwareUtils$MemoryLevelListType[AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_1.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKillerAwareUtils$MemoryLevelListType[AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_2.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKillerAwareUtils$MemoryLevelListType[AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_3.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKillerAwareUtils$MemoryLevelListType[AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_4.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKillerAwareUtils$MemoryLevelListType[AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_5.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private int getInternalMemLevel(AdvancedKillerAwareUtils.MemoryLevelListType type) {
        switch (AnonymousClass1.$SwitchMap$com$huawei$server$rme$hyperhold$AdvancedKillerAwareUtils$MemoryLevelListType[type.ordinal()]) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return 5;
            default:
                return 0;
        }
    }

    private List<AwareProcessBlockInfo> generatePackageList(AdvancedKillerAwareUtils.MemoryLevelListType type) {
        AwareAppMngSortPolicy policy;
        if (type == AdvancedKillerAwareUtils.MemoryLevelListType.LEVEL_CACHED_PROCESS) {
            policy = MemoryUtils.getCachedCleanPolicy();
        } else {
            policy = MemoryUtils.getAppMngSortPolicy(2, 3, getInternalMemLevel(type));
        }
        List<AwareProcessBlockInfo> list = MemoryUtils.getAppMngProcGroup(policy, 2);
        if (list != null) {
            list.removeIf($$Lambda$KOObcVsaJsxvOT87mUp4OCwLSo.INSTANCE);
        }
        return list;
    }

    private String getFirstProcessName(AwareProcessBlockInfo block) {
        AwareProcessInfo proc;
        String procName;
        if (block == null || block.procProcessList == null || block.procProcessList.isEmpty() || (proc = block.procProcessList.get(0)) == null || proc.procProcInfo == null || (procName = proc.procProcInfo.mProcessName) == null) {
            return "";
        }
        return procName;
    }
}
