package com.android.server.location;

import java.util.ArrayList;
import java.util.Map;

public interface CellBatchingCallback {
    void onCellBatchingChanged(ArrayList<Map<String, String>> arrayList);

    void onFusedLbsServiceDied();
}
