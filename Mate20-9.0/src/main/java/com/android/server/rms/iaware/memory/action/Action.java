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
    protected Context mContext;
    protected AtomicBoolean mInterrupt = new AtomicBoolean(false);

    protected static final class PkgMemHolder {
        private Map<String, Long> mUssMap = new ArrayMap();

        PkgMemHolder(int uid, List<AwareProcessInfo> procs) {
            init(uid, procs);
        }

        /* access modifiers changed from: package-private */
        public void init(int uid, List<AwareProcessInfo> procs) {
            long killedMem;
            int i = uid;
            ProcStateStatisData handle = ProcStateStatisData.getInstance();
            for (AwareProcessInfo proc : procs) {
                if (MemoryConstant.isExactKillSwitch()) {
                    killedMem = handle.getProcUss(i, proc.mPid);
                    if (killedMem == 0) {
                        long[] outUss = new long[2];
                        ResourceCollector.getPssFast(proc.mPid, outUss, null);
                        killedMem = outUss[0] + (outUss[1] / 3);
                        AwareLog.d("AwareMem_Action", "getSimpleUss=" + killedMem);
                    }
                    if (killedMem == 0) {
                        killedMem = MemoryReader.getPssForPid(proc.mPid);
                        AwareLog.d("AwareMem_Action", "getUss=" + killedMem);
                    }
                } else {
                    killedMem = MemoryConstant.APP_AVG_USS;
                }
                this.mUssMap.put(i + "|" + proc.mPid, Long.valueOf(killedMem));
                AwareLog.d("AwareMem_Action", "init: uid:" + i + ", pid:" + proc.mPid + ",uss:" + killedMem);
            }
        }

        /* access modifiers changed from: package-private */
        public long getKilledMem(int uid, List<Integer> pids) {
            long killedMem = 0;
            for (Integer pid : pids) {
                Long uss = this.mUssMap.get(uid + "|" + pid);
                if (uss == null || 0 >= uss.longValue()) {
                    uss = Long.valueOf(MemoryConstant.APP_AVG_USS);
                }
                killedMem += uss.longValue();
                AwareLog.i("AwareMem_Action", "getKilledMem: uid:" + uid + ", pid:" + pid + ",uss:" + uss);
            }
            return killedMem;
        }
    }

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
}
