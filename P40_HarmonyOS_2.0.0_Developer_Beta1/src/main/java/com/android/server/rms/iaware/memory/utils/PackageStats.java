package com.android.server.rms.iaware.memory.utils;

import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.SparseArray;
import java.util.Calendar;
import java.util.Date;

/* access modifiers changed from: package-private */
public class PackageStats {
    private Calendar mCalendar = Calendar.getInstance();
    private int[] mCurDate = {0, 0, 0};
    private final SparseArray<PackageState> mPackages = new SparseArray<>();

    interface BaseOperation {
        void addTimeStamp(int i, long j);

        void calcMax();

        void cleanTimeStamp(long j, long j2);
    }

    PackageStats() {
        initDate();
    }

    /* access modifiers changed from: package-private */
    public static final class PackageState extends RecordTable {
        private boolean mTraceProcess = false;
        public final String packageName;
        public final int packageUid;
        public final ArrayMap<String, ProcessState> processes = new ArrayMap<>();

        public PackageState(String pkgName, int uid) {
            this.packageName = pkgName;
            this.packageUid = uid;
            if (uid < 10000) {
                this.mTraceProcess = true;
            }
        }

        public long addRecord(int record, long timeStamp) {
            super.addTimeStamp(record, timeStamp);
            return this.mTotalRecordCounts[record];
        }

        public long addProcRecord(int record, String processName, long timeStamp) {
            if (TextUtils.isEmpty(processName)) {
                return 0;
            }
            ProcessState state = getProcessState(processName);
            state.addTimeStamp(record, timeStamp);
            return state.getRecordTable().mTotalRecordCounts[record];
        }

        public void cleanRange(long nowTime, long interval) {
            if (this.mTraceProcess) {
                int size = this.processes.size();
                for (int i = 0; i < size; i++) {
                    this.processes.valueAt(i).cleanTimeStamp(nowTime, interval);
                }
            }
            super.cleanTimeStamp(nowTime, interval);
        }

        @Override // com.android.server.rms.iaware.memory.utils.RecordTable
        public void calcMax() {
            if (this.mTraceProcess) {
                int size = this.processes.size();
                for (int i = 0; i < size; i++) {
                    this.processes.valueAt(i).calcMax();
                }
            }
            super.calcMax();
        }

        private ProcessState getProcessState(String processName) {
            ProcessState state = this.processes.get(processName);
            if (state != null) {
                return state;
            }
            ProcessState state2 = new ProcessState(this.mTraceProcess);
            this.processes.put(processName, state2);
            return state2;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ProcessState implements BaseOperation {
        private RecordTable mRecordTable;
        private final boolean mTraced;

        ProcessState(boolean traced) {
            this.mTraced = traced;
        }

        @Override // com.android.server.rms.iaware.memory.utils.PackageStats.BaseOperation
        public void addTimeStamp(int record, long timeStamp) {
            if (this.mTraced) {
                getRecordTable().addTimeStamp(record, timeStamp);
            }
        }

        @Override // com.android.server.rms.iaware.memory.utils.PackageStats.BaseOperation
        public void cleanTimeStamp(long nowTime, long interval) {
            if (this.mTraced) {
                getRecordTable().cleanTimeStamp(nowTime, interval);
            }
        }

        @Override // com.android.server.rms.iaware.memory.utils.PackageStats.BaseOperation
        public void calcMax() {
            if (this.mTraced) {
                getRecordTable().calcMax();
            }
        }

        public RecordTable getRecordTable() {
            if (this.mRecordTable == null) {
                this.mRecordTable = new RecordTable();
            }
            return this.mRecordTable;
        }
    }

    /* access modifiers changed from: package-private */
    public long addPkgRecord(int record, String packageName, int uid, long timeStamp) {
        return getPackageState(packageName, uid, true).addRecord(record, timeStamp);
    }

    /* access modifiers changed from: package-private */
    public long addProcRecord(int record, String packageName, int uid, String processName, long timeStamp) {
        return getPackageState(packageName, uid, true).addProcRecord(record, processName, timeStamp);
    }

    /* access modifiers changed from: package-private */
    public PackageState getPackageState(String packageName, int uid) {
        return getPackageState(packageName, uid, false);
    }

    /* access modifiers changed from: package-private */
    public void cleanRange(long nowTime, long interval) {
        int size = this.mPackages.size();
        for (int i = 0; i < size; i++) {
            this.mPackages.valueAt(i).cleanRange(nowTime, interval);
        }
    }

    /* access modifiers changed from: package-private */
    public void calcMax() {
        this.mCalendar.setTime(new Date());
        if (this.mCurDate[2] != this.mCalendar.get(5)) {
            int size = this.mPackages.size();
            for (int i = 0; i < size; i++) {
                this.mPackages.valueAt(i).calcMax();
            }
            initDate();
        }
    }

    private void initDate() {
        this.mCurDate[0] = this.mCalendar.get(1);
        this.mCurDate[1] = this.mCalendar.get(2);
        this.mCurDate[2] = this.mCalendar.get(5);
    }

    private PackageState getPackageState(String packageName, int uid, boolean forced) {
        PackageState pkgState = this.mPackages.get(uid);
        if (!forced || pkgState != null) {
            return pkgState;
        }
        PackageState pkgState2 = new PackageState(packageName, uid);
        this.mPackages.put(uid, pkgState2);
        return pkgState2;
    }
}
