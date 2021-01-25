package com.android.server.rms.iaware.memory.action;

import android.content.Context;
import android.os.Bundle;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.rms.collector.ResourceCollector;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.memory.utils.MemoryReader;
import com.android.server.rms.memrepair.ProcStateStatisData;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Action {
    private static final int COMPRESS_RATIO = 3;
    private static final int USS_LENGTH = 2;
    protected Context mContext;
    protected AtomicBoolean mInterrupt = new AtomicBoolean(false);

    public abstract int execute(Bundle bundle);

    public abstract void reset();

    public Action(Context context) {
        this.mContext = context;
    }

    public void interrupt(boolean interrupted) {
        this.mInterrupt.set(interrupted);
    }

    public boolean reqInterrupt(Bundle extras) {
        return false;
    }

    public boolean canBeExecuted() {
        return true;
    }

    public int getLastExecFailCount() {
        return 0;
    }

    protected static final class PkgMemHolder {
        private Map<String, Long> mUssMap = new ArrayMap();

        PkgMemHolder(int uid, List<AwareProcessInfo> procs) {
            init(uid, procs);
        }

        /* access modifiers changed from: package-private */
        public void init(int uid, List<AwareProcessInfo> procs) {
            long killedMem;
            ProcStateStatisData handle = ProcStateStatisData.getInstance();
            for (AwareProcessInfo proc : procs) {
                if (MemoryConstant.isExactKillSwitch()) {
                    killedMem = handle.getProcUss(uid, proc.procPid);
                    if (killedMem == 0) {
                        long[] outUss = new long[2];
                        ResourceCollector.getPss(proc.procPid, outUss, (long[]) null);
                        killedMem = outUss[0] + (outUss[1] / 3);
                    }
                    if (killedMem == 0) {
                        killedMem = MemoryReader.getPssForPid(proc.procPid);
                    }
                } else {
                    killedMem = MemoryConstant.APP_AVG_USS;
                }
                this.mUssMap.put(uid + ProcStateStatisData.SEPERATOR_CHAR + proc.procPid, Long.valueOf(killedMem));
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d("AwareMem_Action", "init: uid = " + uid + ", pid = " + proc.procPid + ", uss = " + killedMem);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public long getKilledMem(int uid, List<Integer> pids) {
            long killedMem = 0;
            for (Integer pid : pids) {
                Long uss = this.mUssMap.get(uid + ProcStateStatisData.SEPERATOR_CHAR + pid);
                if (uss == null || uss.longValue() <= 0) {
                    uss = Long.valueOf((long) MemoryConstant.APP_AVG_USS);
                }
                killedMem += uss.longValue();
                if (AwareLog.getDebugLogSwitch()) {
                    AwareLog.d("AwareMem_Action", "getKilledMem: uid: " + uid + ", pid: " + pid + ", uss: " + uss);
                }
            }
            return killedMem;
        }
    }
}
