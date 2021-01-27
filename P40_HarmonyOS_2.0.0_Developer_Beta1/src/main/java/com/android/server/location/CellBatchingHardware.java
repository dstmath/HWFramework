package com.android.server.location;

import android.os.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import vendor.huawei.hardware.fusd.V1_1.CellTrajectoryData;
import vendor.huawei.hardware.fusd.V1_3.CellBatchingRequest;
import vendor.huawei.hardware.fusd.V1_3.ICellBatchingCallback;
import vendor.huawei.hardware.fusd.V1_3.ICellBatchingInterface;
import vendor.huawei.hardware.fusd.V1_3.IFusdLbs;

public class CellBatchingHardware {
    private static final int DEFAULT_SIZE = 16;
    private static final int MSG_FUSIONDEAMON_DIED = 1;
    private static final String TAG = "CellBatching.CellBatchingHardware";
    private static final Object WATCHER_LOCK = new Object();
    private boolean isDisConnect = false;
    private CellBatchingCallback mCellBatchingCallback;
    private ICellBatchingInterface mICellBatchingInterface;
    private IFusdLbs mIFusdLbs;
    private int mInterval;
    private int mStatus;

    public CellBatchingHardware(CellBatchingCallback callback) {
        this.mCellBatchingCallback = callback;
        FusedLbsServiceConnect.getInstance().registerServiceDiedNotify(new IFusedLbsServiceDied() {
            /* class com.android.server.location.CellBatchingHardware.AnonymousClass1 */

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceDied() {
                LBSLog.i(CellBatchingHardware.TAG, "onFusedLbsServiceDied...");
                CellBatchingHardware.this.isDisConnect = true;
                synchronized (CellBatchingHardware.WATCHER_LOCK) {
                    CellBatchingHardware.this.mIFusdLbs = null;
                }
                if (CellBatchingHardware.this.mCellBatchingCallback != null) {
                    CellBatchingHardware.this.mCellBatchingCallback.onFusedLbsServiceDied();
                }
            }

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceConnect() {
                CellBatchingHardware.this.getIFusdLbsService();
                if (CellBatchingHardware.this.mStatus == 1) {
                    CellBatchingHardware cellBatchingHardware = CellBatchingHardware.this;
                    cellBatchingHardware.sendException2Cellbatching(cellBatchingHardware.mInterval, 1);
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public class FusdCellBatchingCallback extends ICellBatchingCallback.Stub {
        FusdCellBatchingCallback() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_3.ICellBatchingCallback
        public void onCellBatchingChanged(ArrayList<CellTrajectoryData> data) throws RemoteException {
            if (data == null || data.size() == 0) {
                LBSLog.e(CellBatchingHardware.TAG, "data is null!");
            } else if (CellBatchingHardware.this.mCellBatchingCallback != null) {
                CellBatchingHardware.this.mCellBatchingCallback.onCellBatchingChanged(CellBatchingHardware.this.getClientCellBatchingDataFromFusd(data));
                LBSLog.i(CellBatchingHardware.TAG, "send MSG_CELLBATCHING_CHANGED.");
            } else {
                LBSLog.i(CellBatchingHardware.TAG, false, "mCellBatchingCallback is null.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<Map<String, String>> getClientCellBatchingDataFromFusd(ArrayList<CellTrajectoryData> cellTrajectoryResults) {
        ArrayList<Map<String, String>> clientResultLists = new ArrayList<>();
        int size = cellTrajectoryResults.size();
        for (int i = 0; i < size; i++) {
            Map<String, String> clientResults = new HashMap<>(16);
            CellTrajectoryData hidlResult = cellTrajectoryResults.get(i);
            clientResults.put("timestamplow", String.valueOf(hidlResult.timestamplow));
            clientResults.put("timestamphigh", String.valueOf(hidlResult.timestamphigh));
            clientResults.put("cid", String.valueOf(hidlResult.cid));
            clientResults.put("lac", String.valueOf((int) hidlResult.lac));
            clientResults.put("rssi", String.valueOf((int) hidlResult.rssi));
            clientResults.put("mcc", String.valueOf((int) hidlResult.mcc));
            clientResults.put("mnc", String.valueOf((int) hidlResult.mnc));
            clientResultLists.add(clientResults);
        }
        return clientResultLists;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void getIFusdLbsService() {
        if (this.mIFusdLbs == null) {
            this.mIFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_3();
            if (this.mIFusdLbs != null) {
                try {
                    this.mICellBatchingInterface = this.mIFusdLbs.getCellBatchingInterface();
                    if (this.mICellBatchingInterface != null) {
                        this.mICellBatchingInterface.registerCellBatchingCallback(new FusdCellBatchingCallback());
                    }
                } catch (RemoteException e) {
                    LBSLog.e(TAG, "register callback error");
                }
            }
        }
    }

    public boolean cellBatchingSwitch(int status, int interval, int reserved1, int reserved2) {
        getIFusdLbsService();
        if (this.mICellBatchingInterface == null) {
            LBSLog.e(TAG, "mICellBatchingInterface is null.");
            return false;
        }
        this.mInterval = interval;
        this.mStatus = status;
        CellBatchingRequest requestHidl = new CellBatchingRequest();
        requestHidl.status = status;
        requestHidl.interval = interval;
        requestHidl.reserved1 = reserved1;
        requestHidl.reserved2 = reserved2;
        try {
            return this.mICellBatchingInterface.cellBatchingSwitch(requestHidl);
        } catch (RemoteException e) {
            LBSLog.e(TAG, "cellBatchingSwitch error.");
            return false;
        }
    }

    public boolean flushCellBatching() {
        getIFusdLbsService();
        if (this.mICellBatchingInterface == null) {
            LBSLog.e(TAG, "mICellBatchingInterface is null.");
            return false;
        }
        LBSLog.i(TAG, false, "isDisConnect = %{public}b", Boolean.valueOf(this.isDisConnect));
        if (this.isDisConnect) {
            this.isDisConnect = false;
            if (this.mStatus == 1) {
                sendException2Cellbatching(this.mInterval, 1);
            }
        }
        try {
            return this.mICellBatchingInterface.flushCellBatching();
        } catch (RemoteException e) {
            LBSLog.e(TAG, "flushCellBatching error.");
            return false;
        }
    }

    public boolean sendException2Cellbatching(int interval, int status) {
        return cellBatchingSwitch(status, interval, 0, 0);
    }
}
