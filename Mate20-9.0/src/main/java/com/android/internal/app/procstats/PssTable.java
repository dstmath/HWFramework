package com.android.internal.app.procstats;

import com.android.internal.app.procstats.SparseMappingTable;

public class PssTable extends SparseMappingTable.Table {
    public PssTable(SparseMappingTable tableData) {
        super(tableData);
    }

    public void mergeStats(PssTable that) {
        PssTable pssTable = that;
        int N = that.getKeyCount();
        for (int i = 0; i < N; i++) {
            int key = pssTable.getKeyAt(i);
            mergeStats(SparseMappingTable.getIdFromKey(key), (int) pssTable.getValue(key, 0), pssTable.getValue(key, 1), pssTable.getValue(key, 2), pssTable.getValue(key, 3), pssTable.getValue(key, 4), pssTable.getValue(key, 5), pssTable.getValue(key, 6), pssTable.getValue(key, 7), pssTable.getValue(key, 8), pssTable.getValue(key, 9));
        }
    }

    public void mergeStats(int state, int inCount, long minPss, long avgPss, long maxPss, long minUss, long avgUss, long maxUss, long minRss, long avgRss, long maxRss) {
        int i = inCount;
        long j = minPss;
        long j2 = avgPss;
        long j3 = maxPss;
        long j4 = minUss;
        long j5 = avgUss;
        long j6 = maxUss;
        int key = getOrAddKey((byte) state, 10);
        long count = getValue(key, 0);
        if (count == 0) {
            setValue(key, 0, (long) i);
            setValue(key, 1, j);
            setValue(key, 2, j2);
            setValue(key, 3, j3);
            setValue(key, 4, j4);
            setValue(key, 5, j5);
            setValue(key, 6, maxUss);
            setValue(key, 7, minRss);
            setValue(key, 8, avgRss);
            setValue(key, 9, maxRss);
            long j7 = count;
            long j8 = maxUss;
            return;
        }
        long count2 = count;
        long count3 = maxRss;
        setValue(key, 0, ((long) i) + count2);
        if (getValue(key, 1) > j) {
            setValue(key, 1, j);
        }
        long val = getValue(key, 2);
        long j9 = val;
        setValue(key, 2, (long) (((((double) val) * ((double) count2)) + (((double) j2) * ((double) i))) / ((double) (((long) i) + count2))));
        if (getValue(key, 3) < j3) {
            setValue(key, 3, j3);
        }
        if (getValue(key, 4) > j4) {
            setValue(key, 4, j4);
        }
        long val2 = getValue(key, 5);
        long j10 = val2;
        long count4 = count2;
        long j11 = avgUss;
        setValue(key, 5, (long) (((((double) val2) * ((double) count2)) + (((double) j11) * ((double) i))) / ((double) (((long) i) + count4))));
        long j12 = maxUss;
        if (getValue(key, 6) < j12) {
            setValue(key, 6, j12);
        }
        if (getValue(key, 7) > j4) {
            setValue(key, 7, j4);
        }
        setValue(key, 8, (long) (((((double) getValue(key, 8)) * ((double) count4)) + (((double) j11) * ((double) i))) / ((double) (((long) i) + count4))));
        if (getValue(key, 9) < j12) {
            setValue(key, 9, j12);
        }
    }
}
