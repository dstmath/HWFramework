package com.android.internal.app.procstats;

import com.android.internal.app.procstats.SparseMappingTable.Table;

public class PssTable extends Table {
    public PssTable(SparseMappingTable tableData) {
        super(tableData);
    }

    public void mergeStats(PssTable that) {
        int N = that.getKeyCount();
        for (int i = 0; i < N; i++) {
            int key = that.getKeyAt(i);
            mergeStats(SparseMappingTable.getIdFromKey(key), (int) that.getValue(key, 0), that.getValue(key, 1), that.getValue(key, 2), that.getValue(key, 3), that.getValue(key, 4), that.getValue(key, 5), that.getValue(key, 6));
        }
    }

    public void mergeStats(int state, int inCount, long minPss, long avgPss, long maxPss, long minUss, long avgUss, long maxUss) {
        int key = getOrAddKey((byte) state, 7);
        long count = getValue(key, 0);
        if (count == 0) {
            setValue(key, 0, (long) inCount);
            setValue(key, 1, minPss);
            setValue(key, 2, avgPss);
            setValue(key, 3, maxPss);
            setValue(key, 4, minUss);
            setValue(key, 5, avgUss);
            setValue(key, 6, maxUss);
            return;
        }
        setValue(key, 0, ((long) inCount) + count);
        if (getValue(key, 1) > minPss) {
            setValue(key, 1, minPss);
        }
        setValue(key, 2, (long) (((((double) getValue(key, 2)) * ((double) count)) + (((double) avgPss) * ((double) inCount))) / ((double) (((long) inCount) + count))));
        if (getValue(key, 3) < maxPss) {
            setValue(key, 3, maxPss);
        }
        if (getValue(key, 4) > minUss) {
            setValue(key, 4, minUss);
        }
        setValue(key, 5, (long) (((((double) getValue(key, 5)) * ((double) count)) + (((double) avgUss) * ((double) inCount))) / ((double) (((long) inCount) + count))));
        if (getValue(key, 6) < maxUss) {
            setValue(key, 6, maxUss);
        }
    }
}
