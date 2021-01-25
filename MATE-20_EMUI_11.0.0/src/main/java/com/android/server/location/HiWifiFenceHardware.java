package com.android.server.location;

import android.hardware.gnss.V1_0.GnssLocation;
import android.location.Location;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.fusd.V1_2.IFusdLbs;
import vendor.huawei.hardware.fusd.V1_2.IWififenceCallback;
import vendor.huawei.hardware.fusd.V1_2.IWififenceInterface;

public class HiWifiFenceHardware {
    private static final String TAG = "HiFence2.HiWifiFenceHardware";
    private static final Object WATCHER_LOCK = new Object();
    private static final String WIFIFENCE_INTERFACE_IS_NULL = "mWififenceInterface is null.";
    private static final int WIFIFENCE_STATE_UNKNOWN = -1;
    private IFusdLbs mIFusdLbs;
    private WifiFenceCallback mWifiFenceCallback;
    private IWififenceInterface mWififenceInterface;

    public HiWifiFenceHardware(WifiFenceCallback wifiFenceCallback) {
        this.mWifiFenceCallback = wifiFenceCallback;
        FusedLbsServiceConnect.getInstance().registerServiceDiedNotify(new IFusedLbsServiceDied() {
            /* class com.android.server.location.HiWifiFenceHardware.AnonymousClass1 */

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceDied() {
                synchronized (HiWifiFenceHardware.WATCHER_LOCK) {
                    HiWifiFenceHardware.this.mIFusdLbs = null;
                }
                if (HiWifiFenceHardware.this.mWifiFenceCallback != null) {
                    HiWifiFenceHardware.this.mWifiFenceCallback.onFusedLbsServiceDied();
                }
            }

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceConnect() {
                HiWifiFenceHardware.this.getIFusdLbsService();
            }
        });
    }

    private ArrayList<IWififenceInterface.WifiFenceRequest> convertHidlRequestFromListM(ArrayList<Map<String, Object>> wififences) {
        ArrayList<IWififenceInterface.WifiFenceRequest> hidlRequests = new ArrayList<>();
        if (wififences == null || wififences.size() == 0) {
            LBSLog.e(TAG, "wififences null,convertHidlRequest fail.");
            return hidlRequests;
        }
        int size = wififences.size();
        LBSLog.i(TAG, false, "convertHidlRequest size = %{public}d", Integer.valueOf(size));
        for (int i = 0; i < size; i++) {
            Map<String, Object> requests = wififences.get(i);
            IWififenceInterface.WifiFenceRequest hidlRequest = new IWififenceInterface.WifiFenceRequest();
            hidlRequest.id = Integer.parseInt(requests.get("id").toString());
            hidlRequest.rssi = Integer.parseInt(requests.get("rssi").toString());
            hidlRequest.parentFenceId = Integer.parseInt(requests.get("parentFenceId").toString());
            Object bssidListObj = requests.get("bssidList");
            ArrayList<Byte> bssidList = new ArrayList<>();
            if (bssidListObj instanceof ArrayList) {
                bssidList = (ArrayList) bssidListObj;
            }
            for (int j = 0; j < bssidList.size(); j++) {
                hidlRequest.bssid.add(bssidList.get(j));
            }
            hidlRequests.add(hidlRequest);
        }
        return hidlRequests;
    }

    public boolean addWififence(int operId, ArrayList<Map<String, Object>> wififences) {
        if (wififences == null) {
            return false;
        }
        LBSLog.i(TAG, false, "addWififence size = %{public}d", Integer.valueOf(wififences.size()));
        getIFusdLbsService();
        IWififenceInterface iWififenceInterface = this.mWififenceInterface;
        if (iWififenceInterface == null) {
            LBSLog.e(TAG, WIFIFENCE_INTERFACE_IS_NULL);
            return false;
        }
        try {
            iWififenceInterface.addWifiFence(operId, convertHidlRequestFromListM(wififences));
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, "add wififence error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception addWififence", new Object[0]);
            return false;
        }
    }

    public boolean removeWififence(int operId, int[] ids) {
        if (ids == null) {
            return false;
        }
        getIFusdLbsService();
        if (this.mWififenceInterface == null) {
            LBSLog.d(TAG, WIFIFENCE_INTERFACE_IS_NULL);
            return false;
        }
        ArrayList<Integer> idList = new ArrayList<>();
        for (int i : ids) {
            idList.add(Integer.valueOf(i));
        }
        try {
            this.mWififenceInterface.removeWififences(operId, idList);
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, "remove wififence error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception removeWififences", new Object[0]);
            return false;
        }
    }

    public boolean pauseWififence(int operId, int[] ids) {
        if (ids == null) {
            return false;
        }
        getIFusdLbsService();
        if (this.mWififenceInterface == null) {
            LBSLog.d(TAG, WIFIFENCE_INTERFACE_IS_NULL);
            return false;
        }
        ArrayList<Integer> idList = new ArrayList<>();
        for (int i : ids) {
            idList.add(Integer.valueOf(i));
        }
        try {
            this.mWififenceInterface.pauseWififence(operId, idList);
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, "pause wififence error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception pauseWififence", new Object[0]);
            return false;
        }
    }

    public boolean resumeWififence(int operId, int[] ids) {
        if (ids == null) {
            return false;
        }
        getIFusdLbsService();
        if (this.mWififenceInterface == null) {
            LBSLog.d(TAG, WIFIFENCE_INTERFACE_IS_NULL);
            return false;
        }
        ArrayList<Integer> idList = new ArrayList<>();
        for (int i : ids) {
            idList.add(Integer.valueOf(i));
        }
        try {
            this.mWififenceInterface.resumeWififence(operId, idList);
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, "resume wififence error.");
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception resumeWififence", new Object[0]);
            return false;
        }
    }

    public int getWififenceState(int wififenceId) {
        getIFusdLbsService();
        IWififenceInterface iWififenceInterface = this.mWififenceInterface;
        if (iWififenceInterface == null) {
            LBSLog.d(TAG, WIFIFENCE_INTERFACE_IS_NULL);
            return -1;
        }
        try {
            return iWififenceInterface.getWififenceState(wififenceId);
        } catch (RemoteException e) {
            LBSLog.e(TAG, "get wififence state error.");
            return -1;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception getWififenceState", new Object[0]);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void getIFusdLbsService() {
        if (this.mIFusdLbs == null) {
            this.mIFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_3();
            if (this.mIFusdLbs == null) {
                this.mIFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_2();
            }
            if (this.mIFusdLbs != null) {
                try {
                    this.mWififenceInterface = this.mIFusdLbs.getWififenceInterface();
                    if (this.mWififenceInterface != null) {
                        this.mWififenceInterface.setWififenceCallback(new FusdWififenceCallback());
                    }
                } catch (RemoteException e) {
                    LBSLog.e(TAG, "register callback error");
                } catch (NoSuchElementException e2) {
                    LBSLog.e(TAG, false, "No Such Element Exception setWififenceCallback", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class FusdWififenceCallback extends IWififenceCallback.Stub {
        FusdWififenceCallback() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IWififenceCallback
        public void onWififenceAddCb(int operId, int result) throws RemoteException {
            LBSLog.i(HiWifiFenceHardware.TAG, false, "onAddWififenceCb: operId = %{public}d, result = %{public}d", Integer.valueOf(operId), Integer.valueOf(result));
            if (HiWifiFenceHardware.this.mWifiFenceCallback != null) {
                HiWifiFenceHardware.this.mWifiFenceCallback.onWififenceAddCb(operId, result);
            } else {
                LBSLog.e(HiWifiFenceHardware.TAG, false, "mWifiFenceCallback is null.", new Object[0]);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IWififenceCallback
        public void onWififenceRemoveCb(int operId, int result) throws RemoteException {
            LBSLog.i(HiWifiFenceHardware.TAG, false, "onRemoveWififenceCb: operId = %{public}d, result = %{public}d", Integer.valueOf(operId), Integer.valueOf(result));
            if (HiWifiFenceHardware.this.mWifiFenceCallback != null) {
                HiWifiFenceHardware.this.mWifiFenceCallback.onWififenceRemoveCb(operId, result);
            } else {
                LBSLog.e(HiWifiFenceHardware.TAG, false, "mWifiFenceCallback is null.", new Object[0]);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IWififenceCallback
        public void onWififenceTransitionCb(int wififenceId, GnssLocation location, int transition, long timestamp) throws RemoteException {
            if (location == null) {
                LBSLog.e(HiWifiFenceHardware.TAG, "location is null.");
                return;
            }
            Location loc = HiWifiFenceHardware.this.getLocation(location);
            if (HiWifiFenceHardware.this.mWifiFenceCallback != null) {
                HiWifiFenceHardware.this.mWifiFenceCallback.onWififenceTransitionCb(wififenceId, loc, transition, timestamp);
                LBSLog.i(HiWifiFenceHardware.TAG, "onWififenceTransitionCb end.");
                return;
            }
            LBSLog.e(HiWifiFenceHardware.TAG, "mWifiFenceCallback is null.");
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IWififenceCallback
        public void onWififenceStatusCb(int status, GnssLocation location) throws RemoteException {
            if (location == null) {
                LBSLog.e(HiWifiFenceHardware.TAG, "location is null.");
            } else if (HiWifiFenceHardware.this.mWifiFenceCallback != null) {
                HiWifiFenceHardware.this.mWifiFenceCallback.onWififenceStatusCb(status, HiWifiFenceHardware.this.getLocation(location));
                LBSLog.i(HiWifiFenceHardware.TAG, "onWififenceStatusCb end.");
            } else {
                LBSLog.e(HiWifiFenceHardware.TAG, "mWifiFenceCallback is null.");
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IWififenceCallback
        public void onWififencePauseCb(int operId, int result) throws RemoteException {
            LBSLog.i(HiWifiFenceHardware.TAG, false, "onWififencePauseCb: operId = %{public}d, result = %{public}d", Integer.valueOf(operId), Integer.valueOf(result));
            if (HiWifiFenceHardware.this.mWifiFenceCallback != null) {
                HiWifiFenceHardware.this.mWifiFenceCallback.onWififencePauseCb(operId, result);
            } else {
                LBSLog.e(HiWifiFenceHardware.TAG, "mWifiFenceCallback is null.");
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IWififenceCallback
        public void onWififenceResumeCb(int operId, int result) throws RemoteException {
            LBSLog.i(HiWifiFenceHardware.TAG, false, "onWififenceResumeCb: operId = %{public}d, result = %{public}d", Integer.valueOf(operId), Integer.valueOf(result));
            if (HiWifiFenceHardware.this.mWifiFenceCallback != null) {
                HiWifiFenceHardware.this.mWifiFenceCallback.onWififenceResumeCb(operId, result);
            } else {
                LBSLog.e(HiWifiFenceHardware.TAG, "mWifiFenceCallback is null.");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Location getLocation(GnssLocation lastLocation) {
        Location loc = new Location("gnss");
        loc.setAltitude(lastLocation.altitudeMeters);
        loc.setLatitude(lastLocation.latitudeDegrees);
        loc.setLongitude(lastLocation.longitudeDegrees);
        loc.setSpeed(lastLocation.speedMetersPerSec);
        loc.setBearing(lastLocation.bearingDegrees);
        loc.setAccuracy(lastLocation.horizontalAccuracyMeters);
        loc.setTime(lastLocation.timestamp);
        return loc;
    }
}
