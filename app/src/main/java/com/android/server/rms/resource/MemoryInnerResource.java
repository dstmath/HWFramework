package com.android.server.rms.resource;

import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.rms.HwSysResManager;
import android.rms.config.ResourceConfig;
import android.util.Log;
import com.android.server.rms.collector.MemInfoReader;
import com.android.server.rms.collector.MemoryFragReader;
import com.android.server.rms.collector.ProcMemInfoReader;
import com.android.server.wifipro.WifiProCommonDefs;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class MemoryInnerResource extends HwSysInnerResImpl {
    private static final int BUDDYINFO_URGENT_PAGECOUNT = 300;
    private static final int BUDDYINFO_WARNING_PAGECOUNT = 30;
    private static boolean DEBUG = false;
    public static final String MEMORY_PARAM_MEMNEEDTORECLAIM = "MemNeedToReclaim";
    public static final String MEMORY_PARAM_PROCNEEDTORECLAIM = "ProcNeedToReclaim";
    private static final long MEMORY_SIZE_MB = 1024;
    private static final String RESOURCENAME_BUDDYINFO = "BUDDYINFO";
    private static final String RESOURCENAME_CACHED = "Cached";
    private static final String RESOURCENAME_MEMFREE = "MemFree";
    private static final String RESOURCENAME_SWAPFREE = "SwapFree";
    private static final String TAG = "RMS.MemoryInnerResource";
    private static MemoryInnerResource mMemoryInnerResource;
    private static final String[] mResourceNames = null;
    private static final int[] mThresholdLevel = null;
    private Map<String, ResourceConfig> mBuddyInfoConifg;
    private boolean mHasMemData;
    private Map<String, ResourceConfig> mMainServiceConifg;
    private final Map<String, Long> mMainServiceMemInfo;
    private final MemoryFragReader mMemFragInfo;
    private Map<String, ResourceConfig> mMeminfoConifg;
    private final Map<String, long[]> mMemoryThreshold;
    private final ProcMemInfoReader mProcMemInfoReader;
    private long mTotalMemory;
    private final MemInfoReader memInfoReader;

    private static class MemoryStatus {
        long memNeedToReclaim;
        String resouceName;
        int status;

        public MemoryStatus() {
            init();
        }

        public MemoryStatus(String resouceName, int status, long memNeedToReclaim) {
            setValue(resouceName, status, memNeedToReclaim);
        }

        public void init() {
            this.status = 1;
            this.memNeedToReclaim = 0;
        }

        public void setValue(String resouceName, int status, long memNeedToReclaim) {
            this.resouceName = resouceName;
            this.status = status;
            this.memNeedToReclaim = memNeedToReclaim;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.resource.MemoryInnerResource.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.resource.MemoryInnerResource.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.resource.MemoryInnerResource.<clinit>():void");
    }

    public MemoryInnerResource() {
        int i;
        this.mMeminfoConifg = new HashMap();
        this.mBuddyInfoConifg = new HashMap();
        this.mMainServiceConifg = new HashMap();
        this.mMemoryThreshold = new HashMap();
        this.mTotalMemory = 0;
        this.mHasMemData = false;
        this.memInfoReader = new MemInfoReader();
        this.mMemFragInfo = new MemoryFragReader();
        this.mProcMemInfoReader = new ProcMemInfoReader();
        this.mMainServiceMemInfo = new HashMap();
        ResourceConfig[] meminfoConifg = HwSysResManager.getInstance().getResourceConfig(20);
        ResourceConfig[] buddyInfoConifg = HwSysResManager.getInstance().getResourceConfig(100);
        ResourceConfig[] mainServiceConifg = HwSysResManager.getInstance().getResourceConfig(WifiProCommonDefs.TYEP_HAS_INTERNET);
        if (meminfoConifg != null) {
            for (i = 0; i < meminfoConifg.length; i++) {
                this.mMeminfoConifg.put(meminfoConifg[i].getResouceName(), meminfoConifg[i]);
            }
        }
        if (buddyInfoConifg != null) {
            for (i = 0; i < buddyInfoConifg.length; i++) {
                this.mBuddyInfoConifg.put(buddyInfoConifg[i].getResouceName(), buddyInfoConifg[i]);
            }
        }
        if (mainServiceConifg != null) {
            for (i = 0; i < mainServiceConifg.length; i++) {
                this.mMainServiceConifg.put(mainServiceConifg[i].getResouceName(), mainServiceConifg[i]);
            }
        }
    }

    public static synchronized MemoryInnerResource getInstance() {
        MemoryInnerResource memoryInnerResource;
        synchronized (MemoryInnerResource.class) {
            if (mMemoryInnerResource == null) {
                mMemoryInnerResource = new MemoryInnerResource();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create MemoryInnerResource.");
                }
            }
            memoryInnerResource = mMemoryInnerResource;
        }
        return memoryInnerResource;
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        if (this.mHasMemData) {
            if (args == null || args.keySet().size() <= 0) {
                if (DEBUG) {
                    Log.d(TAG, "system acquire");
                }
                systemAcquire(uri, observer, args);
            } else {
                if (DEBUG) {
                    Log.d(TAG, "app acquire");
                }
                appAcquire(uri, observer, args);
            }
            return 1;
        }
        Log.e(TAG, "memory data does not queryed");
        return 2;
    }

    private int appAcquire(Uri uri, IContentObserver observer, Bundle args) {
        if (args.containsKey("MemorySize")) {
            long memFree = args.getLong("MemorySize");
            this.memInfoReader.readMemInfo();
            Log.d(TAG, "need mem:" + memFree + " free mem:" + this.memInfoReader.getFreeSizeKb());
            if (memFree - this.memInfoReader.getFreeSizeKb() > 0) {
                Bundle data = new Bundle();
                data.putLong(MEMORY_PARAM_MEMNEEDTORECLAIM, memFree - this.memInfoReader.getFreeSizeKb());
                addMainServiceOverloadPids(data);
                HwSysResManager.getInstance().notifyResourceStatus(20, RESOURCENAME_MEMFREE, 4, data);
            } else {
                Log.i(TAG, "no need to reclaim");
            }
            return 1;
        }
        Log.e(TAG, "args does not contains the key MemorySize");
        return 2;
    }

    private int systemAcquire(Uri uri, IContentObserver observer, Bundle args) {
        MemoryStatus memoryStatus = null;
        MemoryStatus memStatus = getMemoryOverloadStatus();
        MemoryStatus buddyInfoStatus = getBuddyinfoOverloadStatus();
        if (memStatus != null) {
            memoryStatus = memStatus;
            Log.d(TAG, "MemoryOverload:subname=" + memStatus.resouceName + " status=" + memStatus.status + " size=" + memStatus.memNeedToReclaim);
        } else if (buddyInfoStatus != null) {
            memoryStatus = buddyInfoStatus;
            Log.d(TAG, "BuddyinfoOverload:subname=" + buddyInfoStatus.resouceName + " status=" + buddyInfoStatus.status + " size=" + buddyInfoStatus.memNeedToReclaim);
        }
        if (memoryStatus == null || memoryStatus.status == 1) {
            return 2;
        }
        Bundle data = new Bundle();
        data.putLong(MEMORY_PARAM_MEMNEEDTORECLAIM, memoryStatus.memNeedToReclaim);
        if (!RESOURCENAME_BUDDYINFO.equals(memoryStatus.resouceName)) {
            addMainServiceOverloadPids(data);
        }
        HwSysResManager.getInstance().notifyResourceStatus(20, memoryStatus.resouceName, memoryStatus.status, data);
        return 1;
    }

    public Bundle query() {
        this.memInfoReader.readMemInfo();
        this.mMemFragInfo.readMemFragInfo();
        Bundle data = new Bundle();
        long[] memInfo = this.memInfoReader.getMemInfo();
        if (memInfo != null) {
            for (ResourceConfig value : this.mMeminfoConifg.values()) {
                data.putLong(value.getResouceName(), memInfo[value.getResourceID()]);
            }
        }
        int[] info = this.mMemFragInfo.getMemFragInfo();
        if (info != null && info.length > 0) {
            for (ResourceConfig value2 : this.mBuddyInfoConifg.values()) {
                data.putInt(value2.getResouceName(), info[value2.getResourceID()]);
            }
        }
        for (ResourceConfig value22 : this.mMainServiceConifg.values()) {
            long pss = this.mProcMemInfoReader.getProcessPss(value22.getResouceName());
            this.mMainServiceMemInfo.put(value22.getResouceName(), Long.valueOf(pss));
            data.putLong(value22.getResouceName(), pss);
        }
        if (!this.mHasMemData) {
            this.mHasMemData = true;
        }
        long totalSizeKb = this.memInfoReader.getTotalSizeKb();
        if (this.mHasMemData && this.mTotalMemory != totalSizeKb && totalSizeKb > 0) {
            this.mTotalMemory = totalSizeKb;
            initMemoryThreshold();
        }
        if (DEBUG) {
            Log.d(TAG, "data=" + data.toString());
        }
        return data;
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        this.mMemoryThreshold.clear();
        this.mMainServiceMemInfo.clear();
        this.mHasMemData = false;
        this.mTotalMemory = 0;
    }

    private MemoryStatus getMemoryOverloadStatus() {
        long[] resourceInfo = getMemInfoArrayOrdered();
        MemoryStatus memStatus = null;
        int status = 1;
        for (int i = 0; i < mResourceNames.length; i++) {
            MemoryStatus resStatus = getMemroyStatus(mResourceNames[i], resourceInfo[i]);
            if (resStatus != null) {
                if (DEBUG) {
                    Log.d(TAG, resStatus.resouceName + " status:" + resStatus.status + ", memNeedToReclaim:" + resStatus.memNeedToReclaim);
                }
                if (resStatus.status > status) {
                    status = resStatus.status;
                    memStatus = resStatus;
                }
            }
        }
        if (memStatus == null || status == 1) {
            return null;
        }
        if (DEBUG) {
            Log.d(TAG, memStatus.resouceName + " overload");
        }
        return memStatus;
    }

    private long[] getMemInfoArrayOrdered() {
        return new long[]{this.memInfoReader.getCachedSizeKb(), this.memInfoReader.getFreeSizeKb(), this.memInfoReader.getSwapFreeSizeKb()};
    }

    private MemoryStatus getMemroyStatus(String memName, long memSize) {
        long[] threshold = (long[]) this.mMemoryThreshold.get(memName);
        if (threshold == null || threshold.length != mThresholdLevel.length) {
            Log.e(TAG, memName + " threshold is null or length is not 3!");
            return null;
        } else if (threshold[0] == 0 || threshold[1] == 0 || threshold[2] == 0) {
            Log.e(TAG, memName + " threshold is invalid!");
            return null;
        } else {
            long memNeedToReclaim = 0;
            int thresholdIndex = -1;
            Boolean compareFlag = Boolean.valueOf(threshold[0] > threshold[1]);
            int i = 0;
            while (i < threshold.length) {
                if (compareFlag.compareTo(Boolean.valueOf(memSize > threshold[i])) == 0) {
                    thresholdIndex = i == 0 ? i : i - 1;
                    if (thresholdIndex == -1) {
                        thresholdIndex = threshold.length - 1;
                    }
                    if (1 != mThresholdLevel[thresholdIndex]) {
                        memNeedToReclaim = Math.abs(memSize - threshold[0]);
                    }
                    return new MemoryStatus(memName, mThresholdLevel[thresholdIndex], memNeedToReclaim);
                }
                i++;
            }
            if (thresholdIndex == -1) {
                thresholdIndex = threshold.length - 1;
            }
            if (1 != mThresholdLevel[thresholdIndex]) {
                memNeedToReclaim = Math.abs(memSize - threshold[0]);
            }
            return new MemoryStatus(memName, mThresholdLevel[thresholdIndex], memNeedToReclaim);
        }
    }

    private void initMemoryThreshold() {
        long totalMemory = this.mTotalMemory;
        for (ResourceConfig value : this.mMeminfoConifg.values()) {
            if (value.getResouceNormalThreshold() >= 0) {
                if (RESOURCENAME_SWAPFREE.equals(value.getResouceName())) {
                    totalMemory = this.memInfoReader.getSwapTotalSizeKb();
                } else {
                    totalMemory = this.mTotalMemory;
                }
                long normal = (((long) value.getResouceNormalThreshold()) * totalMemory) / 100;
                long warning = (((long) value.getResouceWarningThreshold()) * totalMemory) / 100;
                long urgent = (((long) value.getResouceUrgentThreshold()) * totalMemory) / 100;
                if (DEBUG) {
                    Log.d(TAG, "initMemoryThreshold resource name: " + value.getResouceName() + " normal:" + normal + " warning:" + warning + " urgent:" + urgent);
                }
                this.mMemoryThreshold.put(value.getResouceName(), new long[]{normal, warning, urgent});
            }
        }
    }

    private MemoryStatus getBuddyinfoOverloadStatus() {
        int buddyCount = 0;
        int status = 1;
        ResourceConfig config = (ResourceConfig) this.mBuddyInfoConifg.get("order0");
        int[] info = this.mMemFragInfo.getMemFragInfo();
        if (info == null || config == null) {
            return null;
        }
        int warningThreshold = config.getResouceWarningThreshold();
        int urgentThreshold = config.getResouceUrgentThreshold();
        if (warningThreshold < 0 || urgentThreshold < 0) {
            return null;
        }
        if (DEBUG) {
            Log.d(TAG, "getBuddyinfoOverloadStatus warningThreshold: " + warningThreshold + ", urgentThreshold: " + urgentThreshold);
        }
        for (int i = info.length - 1; i >= 0; i--) {
            buddyCount += info[i];
            if (DEBUG) {
                Log.d(TAG, "getBuddyinfoOverloadStatus info[" + i + "]: " + info[i] + ", buddycount: " + buddyCount);
            }
            if (i == warningThreshold && buddyCount <= BUDDYINFO_WARNING_PAGECOUNT) {
                status = 2;
            } else if (i == urgentThreshold && buddyCount <= BUDDYINFO_URGENT_PAGECOUNT) {
                status = 4;
            }
        }
        if (status != 1) {
            return new MemoryStatus(RESOURCENAME_BUDDYINFO, status, 0);
        }
        return null;
    }

    private void addMainServiceOverloadPids(Bundle data) {
        int[] pids = getMainServiceOverloadPids();
        if (pids.length > 0) {
            if (DEBUG) {
                Log.d(TAG, "addMainServiceOverloadPids pids.size:" + pids.length);
            }
            data.putIntArray(MEMORY_PARAM_PROCNEEDTORECLAIM, pids);
        }
    }

    private int[] getMainServiceOverloadPids() {
        ArrayList<Integer> mainServicePids = new ArrayList();
        for (Entry<String, Long> entry : this.mMainServiceMemInfo.entrySet()) {
            String serviceName = (String) entry.getKey();
            long serviceMem = ((Long) entry.getValue()).longValue();
            int pid = 0;
            if (DEBUG) {
                Log.d(TAG, "serviceName:" + serviceName + " serviceMem:" + serviceMem);
            }
            if (serviceMem > ((long) ((ResourceConfig) this.mMainServiceConifg.get(serviceName)).getResouceWarningThreshold()) * MEMORY_SIZE_MB) {
                pid = this.mProcMemInfoReader.getPidForProcName(new String[]{serviceName});
                Log.d(TAG, "serviceName:" + serviceName + " pid:" + pid + " overload");
            }
            if (pid > 0) {
                mainServicePids.add(Integer.valueOf(pid));
            }
        }
        if (mainServicePids.size() <= 0) {
            return new int[0];
        }
        int[] pids = new int[mainServicePids.size()];
        for (int i = 0; i < pids.length; i++) {
            pids[i] = ((Integer) mainServicePids.get(i)).intValue();
        }
        return pids;
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }
}
