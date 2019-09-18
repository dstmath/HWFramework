package com.android.server.rms.iaware.memory.utils;

import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.SparseArray;
import java.util.Calendar;
import java.util.Date;

class PackageStats {
    private Calendar mCalendar = Calendar.getInstance();
    private int[] mCurDate = {0, 0, 0};
    private final SparseArray<PackageState> mPackages = new SparseArray<>();

    interface BaseOperation {
        void addTimestamp(int i, long j);

        void calcMax();

        void cleanTimestamp(long j, long j2);
    }

    static final class PackageState extends RecordTable {
        public final String mPackageName;
        public final ArrayMap<String, ProcessState> mProcesses = new ArrayMap<>();
        private boolean mTraceProcess = false;
        public final int mUid;

        public PackageState(String packageName, int uid) {
            this.mPackageName = packageName;
            this.mUid = uid;
            if (uid < 10000) {
                this.mTraceProcess = true;
            }
        }

        public long addRecord(int record, long timeStamp) {
            super.addTimestamp(record, timeStamp);
            return this.mTotalRecordCounts[record];
        }

        public long addProcRecord(int record, String processName, long timeStamp) {
            if (TextUtils.isEmpty(processName)) {
                return 0;
            }
            ProcessState state = getProcessState(processName);
            state.addTimestamp(record, timeStamp);
            return state.getRecordTable().mTotalRecordCounts[record];
        }

        public void cleanRange(long nowTime, long interval) {
            if (this.mTraceProcess) {
                int size = this.mProcesses.size();
                for (int i = 0; i < size; i++) {
                    this.mProcesses.valueAt(i).cleanTimestamp(nowTime, interval);
                }
            }
            super.cleanTimestamp(nowTime, interval);
        }

        public void calcMax() {
            if (this.mTraceProcess) {
                int size = this.mProcesses.size();
                for (int i = 0; i < size; i++) {
                    this.mProcesses.valueAt(i).calcMax();
                }
            }
            super.calcMax();
        }

        private ProcessState getProcessState(String processName) {
            ProcessState state = this.mProcesses.get(processName);
            if (state != null) {
                return state;
            }
            ProcessState state2 = new ProcessState(this.mTraceProcess);
            this.mProcesses.put(processName, state2);
            return state2;
        }
    }

    static final class ProcessState implements BaseOperation {
        private RecordTable mRecordTable;
        private final boolean mTraced;

        ProcessState(boolean traced) {
            this.mTraced = traced;
        }

        public void addTimestamp(int record, long timeStamp) {
            if (this.mTraced) {
                getRecordTable().addTimestamp(record, timeStamp);
            }
        }

        public void cleanTimestamp(long nowTime, long interval) {
            if (this.mTraced) {
                getRecordTable().cleanTimestamp(nowTime, interval);
            }
        }

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

    PackageStats() {
        initDate();
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
