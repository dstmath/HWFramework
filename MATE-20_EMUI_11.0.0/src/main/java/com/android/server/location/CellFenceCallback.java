package com.android.server.location;

import java.util.ArrayList;
import java.util.Map;

public interface CellFenceCallback {
    void onCellFenceChanged(ArrayList<Map<String, String>> arrayList);

    void onCellTrajectoryChanged(ArrayList<Map<String, String>> arrayList);

    void onCellfenceAdd(int i, int i2);

    void onCellfenceOper(int i, int i2);

    void onFusedLbsServiceDied();
}
