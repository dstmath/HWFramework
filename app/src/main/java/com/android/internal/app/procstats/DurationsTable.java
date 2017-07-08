package com.android.internal.app.procstats;

import com.android.internal.app.procstats.SparseMappingTable.Table;

public class DurationsTable extends Table {
    public DurationsTable(SparseMappingTable tableData) {
        super(tableData);
    }

    public void addDurations(DurationsTable from) {
        int N = from.getKeyCount();
        for (int i = 0; i < N; i++) {
            int key = from.getKeyAt(i);
            addDuration(SparseMappingTable.getIdFromKey(key), from.getValue(key));
        }
    }

    public void addDuration(int state, long value) {
        int key = getOrAddKey((byte) state, 1);
        setValue(key, getValue(key) + value);
    }
}
