package com.android.server.rms.statistic;

import com.android.server.rms.config.HwConfigReader;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.utils.Utils;
import java.util.ArrayList;
import java.util.Map;

public final class HwStatisticCtl {
    private static final String TAG = "HwStatisticCtl";
    private static final boolean isBetaUser = false;
    private int mCollectCount;
    private HwConfigReader mConfig;
    private boolean mIsInit;
    private ArrayList<Map<String, HwResRecord>> mResRecordMaps;
    private HwTimeStatistic mTimeStatistic;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.statistic.HwStatisticCtl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.statistic.HwStatisticCtl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.statistic.HwStatisticCtl.<clinit>():void");
    }

    public HwStatisticCtl(HwConfigReader config) {
        this.mIsInit = false;
        this.mResRecordMaps = new ArrayList();
        this.mConfig = config;
        this.mTimeStatistic = new HwTimeStatistic();
    }

    public void init() {
        if (!this.mIsInit) {
            this.mCollectCount = 0;
            initTimeStatistic();
            initResStatistic();
            this.mIsInit = true;
        }
    }

    public void statisticGroups() {
        if (this.mIsInit) {
            this.mCollectCount++;
            int cycle_num = 0;
            Iterable groupIDs = null;
            if (this.mConfig != null) {
                groupIDs = this.mConfig.getCountGroupID();
            }
            if (r1 != null) {
                for (Integer intValue : r1) {
                    int id = intValue.intValue();
                    if (this.mConfig != null) {
                        cycle_num = this.mConfig.getGroupSampleCycleNum(id);
                    }
                    if (cycle_num > 0 && this.mCollectCount % cycle_num == 0) {
                        if ((Utils.RMSVERSION & 1) != 0) {
                            statisticOneGroup(id);
                        }
                        if ((Utils.RMSVERSION & 2) != 0) {
                            trimOneGroup(id);
                        }
                    }
                }
            }
        }
    }

    private void statisticOneGroup(int groupID) {
        HwResStatistic resStatistic = HwResStatisticImpl.getResStatistic(groupID);
        if (resStatistic != null) {
            resStatistic.statistic(resStatistic.sample(groupID));
        }
    }

    private void trimOneGroup(int groupID) {
        HwResStatistic resStatistic = HwResStatisticImpl.getResStatistic(groupID);
        if (resStatistic != null) {
            resStatistic.acquire(groupID);
        }
    }

    private void initTimeStatistic() {
        long saveInterval = 0;
        long statisticPeroid = 0;
        if (this.mConfig != null) {
            saveInterval = ((long) this.mConfig.getSaveInterval()) * AppHibernateCst.DELAY_ONE_MINS;
            statisticPeroid = ((long) this.mConfig.getCountInterval(isBetaUser)) * AppHibernateCst.DELAY_ONE_MINS;
        }
        this.mTimeStatistic.init(saveInterval, statisticPeroid, 0);
    }

    private void initResStatistic() {
        ArrayList groupIDs = null;
        if (this.mConfig != null) {
            groupIDs = this.mConfig.getCountGroupID();
        }
        if (groupIDs != null) {
            int size = groupIDs.size();
            for (int index = 0; index < size; index++) {
                int groupID = ((Integer) groupIDs.get(index)).intValue();
                initGroup(groupID);
                Map<String, HwResRecord> recordMap = obtainResRecordMap(groupID);
                if (recordMap != null) {
                    this.mResRecordMaps.add(recordMap);
                }
            }
        }
    }

    private void initGroup(int groupID) {
        HwResStatistic resStatistic = HwResStatisticImpl.getResStatistic(groupID);
        if (resStatistic != null) {
            resStatistic.init(this.mConfig);
        }
    }

    private Map<String, HwResRecord> obtainResRecordMap(int groupID) {
        HwResStatistic resStatistic = HwResStatisticImpl.getResStatistic(groupID);
        if (resStatistic != null) {
            return resStatistic.obtainResRecordMap();
        }
        return null;
    }
}
