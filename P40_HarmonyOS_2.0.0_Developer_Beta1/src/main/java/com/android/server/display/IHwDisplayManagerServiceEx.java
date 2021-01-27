package com.android.server.display;

import android.os.Handler;
import android.util.SparseArray;
import com.android.server.display.DisplayAdapter;
import com.huawei.android.hardware.display.HwWifiDisplayParameters;
import java.util.ArrayList;

public interface IHwDisplayManagerServiceEx {
    boolean checkPermissionForHwMultiDisplay(int i);

    void checkVerificationResult(boolean z);

    void connectWifiDisplay(String str, HwWifiDisplayParameters hwWifiDisplayParameters);

    boolean createVrDisplay(String str, int[] iArr);

    boolean destroyAllVrDisplay();

    boolean destroyVrDisplay(String str);

    HwWifiDisplayParameters getHwWifiDisplayParameters();

    LogicalDisplay getVrVirtualDisplayIfNeed(SparseArray<LogicalDisplay> sparseArray, String str, LogicalDisplay logicalDisplay);

    boolean registerHwVrDisplayAdapterIfNeedLocked(ArrayList<DisplayAdapter> arrayList, Handler handler, DisplayAdapter.Listener listener, Handler handler2);

    boolean sendWifiDisplayAction(String str);

    void setHwWifiDisplayParameters(HwWifiDisplayParameters hwWifiDisplayParameters);

    void startWifiDisplayScan(int i);
}
