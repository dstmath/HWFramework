package com.android.server.location;

import android.os.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.fusd.V1_1.CellFenceDownData;
import vendor.huawei.hardware.fusd.V1_1.CellFenceStatus;
import vendor.huawei.hardware.fusd.V1_1.CellTrajectoryData;
import vendor.huawei.hardware.fusd.V1_1.ICellFenceCallback;
import vendor.huawei.hardware.fusd.V1_2.CellFenceAddData;
import vendor.huawei.hardware.fusd.V1_2.ICellFenceInterface;
import vendor.huawei.hardware.fusd.V1_2.IFusdLbs;

public class HiCellFenceHardware {
    private static final int DEFAULT_SIZE = 16;
    public static final int FENCE_ID_MAX_NUM = 100000;
    private static final int MSG_FUSIONDEAMON_DIED = 7;
    private static final int NOTIFICATION_RESPONSIVENESS_IMMEDIATELY = -1;
    private static final String TAG = "HiFence.HiCellFenceHardware";
    private static final int UNKNOWN_TIMER_MS = -1;
    private static final Object WATCHER_LOCK = new Object();
    private CellFenceCallback mCellFenceCallback;
    private ICellFenceInterface mICellFenceInterface;
    private IFusdLbs mIFusdLbs;

    public HiCellFenceHardware(CellFenceCallback callback) {
        this.mCellFenceCallback = callback;
        FusedLbsServiceConnect.getInstance().registerServiceDiedNotify(new IFusedLbsServiceDied() {
            /* class com.android.server.location.HiCellFenceHardware.AnonymousClass1 */

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceDied() {
                synchronized (HiCellFenceHardware.WATCHER_LOCK) {
                    HiCellFenceHardware.this.mIFusdLbs = null;
                }
                if (HiCellFenceHardware.this.mCellFenceCallback != null) {
                    HiCellFenceHardware.this.mCellFenceCallback.onFusedLbsServiceDied();
                }
            }

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceConnect() {
                LBSLog.i(HiCellFenceHardware.TAG, "getIFusdLbsService():onFusedLbsServiceConnect.");
                HiCellFenceHardware.this.getIFusdLbsService();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void getIFusdLbsService() {
        if (this.mIFusdLbs == null) {
            LBSLog.i(TAG, "getIFusdLbsService():mIFusdLbs is null");
            this.mIFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_3();
            if (this.mIFusdLbs == null) {
                LBSLog.i(TAG, "getIFusdLbsService(),V1_3,mIFusdLbs is null");
                this.mIFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_2();
            } else {
                LBSLog.i(TAG, "getIFusdLbsService(),mIFusdLbs is not null");
            }
            if (this.mIFusdLbs != null) {
                try {
                    LBSLog.i(TAG, "getIFusdLbsService(),mIFusdLbs is not null");
                    this.mICellFenceInterface = this.mIFusdLbs.getCellFenceInterfaceV1_2();
                    if (this.mICellFenceInterface != null) {
                        LBSLog.i(TAG, "getIFusdLbsService().registerCellFenceCallback");
                        this.mICellFenceInterface.registerCellFenceCallback(new FusdCellFenceCallback());
                    } else {
                        LBSLog.i(TAG, "getIFusdLbsService(),mICellFenceInterface is null");
                    }
                } catch (RemoteException e) {
                    LBSLog.e(TAG, "register callback error");
                } catch (NoSuchElementException e2) {
                    LBSLog.e(TAG, false, "No Such Element Exception register callback", new Object[0]);
                }
            } else {
                LBSLog.e(TAG, "getIFusdLbsService(),mIFusdLbs is null");
            }
        }
    }

    public boolean addCellFence(int cellId, ArrayList<Map<String, Object>> dataList) {
        if (dataList == null) {
            return false;
        }
        getIFusdLbsService();
        if (this.mICellFenceInterface == null) {
            LBSLog.i(TAG, "cellFenceSwitch mICellFenceInterface is null.");
            return false;
        }
        ArrayList<CellFenceAddData> downDataList = new ArrayList<>();
        int size = dataList.size();
        for (int i = 0; i < size; i++) {
            Map<String, Object> data = dataList.get(i);
            CellFenceAddData downdata = new CellFenceAddData();
            downdata.fid = Integer.parseInt(data.get("fidOrType").toString());
            downdata.parendId = 0;
            downdata.notifRespMs = -1;
            downdata.unknownTimerMs = -1;
            ArrayList<Integer> ids = new ArrayList<>();
            Object idsObj = data.get("ids");
            if (idsObj != null && (idsObj instanceof ArrayList)) {
                ids = (ArrayList) idsObj;
            }
            for (int j = 0; j < ids.size(); j++) {
                downdata.id.add(Integer.valueOf(ids.get(j).intValue()));
            }
            downDataList.add(downdata);
        }
        try {
            this.mICellFenceInterface.addCellFenceV1_2(cellId, downDataList);
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, "addCellFence error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception register callback", new Object[0]);
            return false;
        }
    }

    public boolean addCellFenceExt(int cellId, ArrayList<Map<String, Object>> dataList) {
        LBSLog.i(TAG, "addCellFenceExt.");
        if (dataList == null) {
            return false;
        }
        getIFusdLbsService();
        if (this.mICellFenceInterface == null) {
            LBSLog.i(TAG, " addCellFenceExt mICellFenceInterface is null.");
            return false;
        }
        ArrayList<CellFenceAddData> downDataList = new ArrayList<>();
        int size = dataList.size();
        for (int i = 0; i < size; i++) {
            Map<String, Object> data = dataList.get(i);
            CellFenceAddData downdata = new CellFenceAddData();
            downdata.fid = Integer.parseInt(data.get("fidOrType").toString());
            downdata.parendId = Integer.parseInt(data.get("parentFenceId").toString());
            downdata.notifRespMs = -1;
            downdata.unknownTimerMs = -1;
            ArrayList<Integer> ids = new ArrayList<>();
            Object idsObj = data.get("ids");
            if (idsObj != null && (idsObj instanceof ArrayList)) {
                ids = (ArrayList) idsObj;
            }
            for (int j = 0; j < ids.size(); j++) {
                downdata.id.add(Integer.valueOf(ids.get(j).intValue()));
            }
            downDataList.add(downdata);
        }
        try {
            this.mICellFenceInterface.addCellFenceV1_2(cellId, downDataList);
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, "addCellFenceExt error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception addCellFenceExt", new Object[0]);
            return false;
        }
    }

    public boolean operateCellfence(int id, int fidOrType, ArrayList<Integer> ids) {
        LBSLog.i(TAG, false, "operateCellfence id = %{public}d, mFidOrType = %{public}d", Integer.valueOf(id), Integer.valueOf(fidOrType));
        getIFusdLbsService();
        if (this.mICellFenceInterface == null) {
            return false;
        }
        CellFenceDownData downdata = new CellFenceDownData();
        downdata.fidOrType = fidOrType;
        for (int i = 0; i < ids.size(); i++) {
            downdata.id.add(Integer.valueOf(ids.get(i).intValue()));
        }
        try {
            this.mICellFenceInterface.operCellFence(id, downdata);
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, "operateCellfence error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception operateCellfence", new Object[0]);
            return false;
        }
    }

    public boolean injectGnssFenceResult(ArrayList<Map<String, Integer>> statusList) {
        LBSLog.i(TAG, "injectGnssFenceResult");
        getIFusdLbsService();
        if (this.mICellFenceInterface == null) {
            LBSLog.e(TAG, "injectGnssFenceResult mICellFenceInterface is null.");
            return false;
        }
        ArrayList<CellFenceStatus> fusdStatusList = new ArrayList<>();
        int size = statusList.size();
        for (int i = 0; i < size; i++) {
            Map<String, Integer> status = statusList.get(i);
            CellFenceStatus fusdStatus = new CellFenceStatus();
            fusdStatus.fid = status.get("fid").intValue();
            fusdStatus.status = (short) status.get("status").intValue();
            fusdStatusList.add(fusdStatus);
        }
        try {
            LBSLog.i(TAG, false, "injectGnssFenceResult, size = %{public}d", Integer.valueOf(fusdStatusList.size()));
            return this.mICellFenceInterface.injectGnssFenceResult(fusdStatusList);
        } catch (RemoteException e) {
            LBSLog.e(TAG, "injectGnssFenceResult error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception injectGnssFenceResult", new Object[0]);
            return false;
        }
    }

    public boolean cellTrajectorySwitch(int status) {
        getIFusdLbsService();
        if (this.mICellFenceInterface == null) {
            LBSLog.e(TAG, "cellTrajectorySwitch mICellFenceInterface is null.");
            return false;
        }
        try {
            LBSLog.i(TAG, false, "cellTrajectorySwitch, status = %{public}d", Integer.valueOf(status));
            return this.mICellFenceInterface.cellTrajectorySwitch(status);
        } catch (RemoteException e) {
            LBSLog.e(TAG, "cellTrajectorySwitch error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception cellTrajectorySwitch", new Object[0]);
            return false;
        }
    }

    public boolean requestTrajectory() {
        LBSLog.i(TAG, "requestTrajectory.");
        getIFusdLbsService();
        ICellFenceInterface iCellFenceInterface = this.mICellFenceInterface;
        if (iCellFenceInterface == null) {
            LBSLog.e(TAG, "requestTrajectory mICellFenceInterface is null.");
            return false;
        }
        try {
            return iCellFenceInterface.requestTrajectory();
        } catch (RemoteException e) {
            LBSLog.e(TAG, "requestTrajectory error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception requestTrajectory", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public class FusdCellFenceCallback extends ICellFenceCallback.Stub {
        FusdCellFenceCallback() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.ICellFenceCallback
        public void onCellFenceChanged(ArrayList<CellFenceStatus> fences) throws RemoteException {
            LBSLog.i(HiCellFenceHardware.TAG, "onCellFenceChanged!");
            if (fences == null || fences.size() == 0) {
                LBSLog.e(HiCellFenceHardware.TAG, "fences is null!");
            } else if (HiCellFenceHardware.this.mCellFenceCallback != null) {
                HiCellFenceHardware.this.mCellFenceCallback.onCellFenceChanged(HiCellFenceHardware.this.getClientCellResultFromFusd(fences));
                LBSLog.i(HiCellFenceHardware.TAG, "onCellFenceChanged end.");
            } else {
                LBSLog.e(HiCellFenceHardware.TAG, "mCellFenceCallback is null,onCellFenceChanged.");
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.ICellFenceCallback
        public void onCellTrajectoryChanged(ArrayList<CellTrajectoryData> data) throws RemoteException {
            LBSLog.i(HiCellFenceHardware.TAG, "onCellTrajectoryChanged!");
            if (data == null || data.size() == 0) {
                LBSLog.e(HiCellFenceHardware.TAG, "data is null!");
            } else if (HiCellFenceHardware.this.mCellFenceCallback != null) {
                HiCellFenceHardware.this.mCellFenceCallback.onCellTrajectoryChanged(HiCellFenceHardware.this.getClientCellTrajectoryDataFromFusd(data));
                LBSLog.i(HiCellFenceHardware.TAG, "onCellTrajectoryChanged end.");
            } else {
                LBSLog.e(HiCellFenceHardware.TAG, "mCellFenceCallback is null,onCellTrajectoryChanged.");
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.ICellFenceCallback
        public void onCellfenceAdd(int id, int result) throws RemoteException {
            LBSLog.i(HiCellFenceHardware.TAG, false, "onCellfenceAdd, id = %{public}d, result = %{public}d", Integer.valueOf(id), Integer.valueOf(result));
            if (HiCellFenceHardware.this.mCellFenceCallback != null) {
                HiCellFenceHardware.this.mCellFenceCallback.onCellfenceAdd(id, result);
                LBSLog.i(HiCellFenceHardware.TAG, "onCellfenceAdd end.");
                return;
            }
            LBSLog.e(HiCellFenceHardware.TAG, "mCellFenceCallback is null,onCellfenceAdd.");
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.ICellFenceCallback
        public void onCellfenceOper(int id, int result) throws RemoteException {
            LBSLog.i(HiCellFenceHardware.TAG, false, "onCellfenceOper, id = %{public}d, result = %{public}d", Integer.valueOf(id), Integer.valueOf(result));
            if (HiCellFenceHardware.this.mCellFenceCallback != null) {
                HiCellFenceHardware.this.mCellFenceCallback.onCellfenceOper(id, result);
                LBSLog.i(HiCellFenceHardware.TAG, "onCellfenceAdd end.");
                return;
            }
            LBSLog.e(HiCellFenceHardware.TAG, "mCellFenceCallback is null,onCellfenceOper.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<Map<String, String>> getClientCellResultFromFusd(ArrayList<CellFenceStatus> results) {
        ArrayList<Map<String, String>> clientResults = new ArrayList<>();
        int size = results.size();
        for (int i = 0; i < size; i++) {
            CellFenceStatus hidlResult = results.get(i);
            Map<String, String> clientResult = new HashMap<>(16);
            clientResult.put("fid", String.valueOf(hidlResult.fid));
            clientResult.put("status", String.valueOf((int) hidlResult.status));
            clientResult.put("reserved", String.valueOf((int) hidlResult.reserved));
            clientResults.add(clientResult);
        }
        return clientResults;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<Map<String, String>> getClientCellTrajectoryDataFromFusd(ArrayList<CellTrajectoryData> results) {
        ArrayList<Map<String, String>> clientResultLists = new ArrayList<>();
        int size = results.size();
        for (int i = 0; i < size; i++) {
            Map<String, String> clientResults = new HashMap<>(16);
            CellTrajectoryData hidlResult = results.get(i);
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
}
