package com.android.server.location;

import android.content.Context;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.fusd.V1_2.IFusdLbs;
import vendor.huawei.hardware.fusd.V1_2.IMMCallback;
import vendor.huawei.hardware.fusd.V1_2.IMMInterface;
import vendor.huawei.hardware.fusd.V1_4.IMMCallback;
import vendor.huawei.hardware.fusd.V1_4.ISDMCallback;
import vendor.huawei.hardware.fusd.V1_4.ISDMInterface;

public class MmHardware {
    private static final int MM_TYPE = 0;
    private static final int PDM_TYPE = 1;
    private static final int SDM_TYPE = 2;
    private static final String TAG = "MmHardware";
    private static final Object WATCHER_LOCK = new Object();
    private static MmHardware sMmHardware;
    private IFusdLbs mIFusdLbsV2;
    private vendor.huawei.hardware.fusd.V1_4.IFusdLbs mIFusdLbsV4;
    private IMMInterface mIMMInterfaceV2;
    private vendor.huawei.hardware.fusd.V1_4.IMMInterface mIMMInterfaceV4;
    private ISDMInterface mISDMInterfaceV4;
    private MmCallback mMMCallback;
    private SdmCallback mSdMCallback;

    public static MmHardware getInstance(Context context, MmCallback callback, SdmCallback sdmCallback) {
        MmHardware mmHardware;
        synchronized (WATCHER_LOCK) {
            if (sMmHardware == null) {
                sMmHardware = new MmHardware(context, callback, sdmCallback);
            }
            mmHardware = sMmHardware;
        }
        return mmHardware;
    }

    private MmHardware(Context context, MmCallback callback, SdmCallback sdmCallback) {
        getIFusdLbsService();
        this.mMMCallback = callback;
        this.mSdMCallback = sdmCallback;
        FusedLbsServiceConnect.getInstance().registerServiceDiedNotify(new IFusedLbsServiceDied() {
            /* class com.android.server.location.MmHardware.AnonymousClass1 */

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceDied() {
            }

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceConnect() {
                MmHardware.this.getIFusdLbsService();
            }
        });
        LBSLog.i(TAG, false, "MmHardware init completed.", new Object[0]);
    }

    public boolean sendMmData(byte[] data, int type) {
        if (data == null) {
            LBSLog.e(TAG, false, "sendMmData data is null.", new Object[0]);
            return false;
        }
        ArrayList<Byte> sendData = new ArrayList<>();
        for (byte b : data) {
            sendData.add(Byte.valueOf(b));
        }
        getIFusdLbsService();
        try {
            if (this.mIMMInterfaceV4 != null) {
                if (type == 0) {
                    LBSLog.i(TAG, false, "sendMmData data size = %{public}d", Integer.valueOf(sendData.size()));
                    this.mIMMInterfaceV4.sendMMData(sendData);
                }
                if (type != 1) {
                    return true;
                }
                LBSLog.i(TAG, false, "sendPdrMMData data size = %{public}d", Integer.valueOf(sendData.size()));
                this.mIMMInterfaceV4.sendPdrMMData(sendData);
                return true;
            } else if (this.mIMMInterfaceV2 == null || type != 0) {
                return true;
            } else {
                LBSLog.i(TAG, false, "sendMmData data size = %{public}d", Integer.valueOf(sendData.size()));
                this.mIMMInterfaceV2.sendMMData(sendData);
                return true;
            }
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "IMMInterface error", new Object[0]);
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception sendMmData", new Object[0]);
            return false;
        }
    }

    public boolean sendSDMData(String tileId, byte[] data, int bufLen, int totalLen) {
        if (data == null || tileId == null) {
            LBSLog.e(TAG, false, "sendSDMData data is null.", new Object[0]);
            return false;
        }
        ArrayList<Byte> sendData = new ArrayList<>();
        for (byte b : data) {
            sendData.add(Byte.valueOf(b));
        }
        getIFusdLbsService();
        try {
            if (this.mISDMInterfaceV4 != null) {
                LBSLog.i(TAG, false, "sendSDMData data size = %{public}d", Integer.valueOf(sendData.size()));
                this.mISDMInterfaceV4.sendSdmData(Long.parseLong(tileId), sendData, bufLen, totalLen);
                return true;
            }
            LBSLog.e(TAG, false, "ISDMInterface is null", new Object[0]);
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "sendSDMData error.", new Object[0]);
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception sendSDMData", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void getIFusdLbsService() {
        if (this.mIFusdLbsV4 == null) {
            if (this.mIFusdLbsV2 == null) {
                this.mIFusdLbsV4 = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_4();
                if (this.mIFusdLbsV4 != null) {
                    try {
                        this.mIMMInterfaceV4 = this.mIFusdLbsV4.getMMInterfaceV1_4();
                        if (this.mIMMInterfaceV4 != null) {
                            LBSLog.i(TAG, false, "LbsService registerMMCallback", new Object[0]);
                            this.mIMMInterfaceV4.registerMMCallbackv1_4(new MapMatchingCallbackV4());
                        } else {
                            LBSLog.w(TAG, false, "IMMInterfaceV1.4 is null...", new Object[0]);
                        }
                        this.mISDMInterfaceV4 = this.mIFusdLbsV4.getSDMInterface();
                        if (this.mISDMInterfaceV4 != null) {
                            LBSLog.i(TAG, false, "LbsService registerSDMCallback sending", new Object[0]);
                            this.mISDMInterfaceV4.registerSDMCallback(new MapMatchingSDMCallback());
                        } else {
                            LBSLog.w(TAG, false, "ISDMInterface is null", new Object[0]);
                        }
                    } catch (RemoteException e) {
                        LBSLog.e(TAG, false, "register callback error", new Object[0]);
                    } catch (NoSuchElementException e2) {
                        LBSLog.e(TAG, false, "No Such Element Exception", new Object[0]);
                    }
                } else if (this.mIFusdLbsV2 == null) {
                    getIFusdLbsServiceV2();
                }
            }
        }
    }

    private void getIFusdLbsServiceV2() {
        this.mIFusdLbsV2 = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_3();
        if (this.mIFusdLbsV2 == null) {
            this.mIFusdLbsV2 = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_2();
        }
        IFusdLbs iFusdLbs = this.mIFusdLbsV2;
        if (iFusdLbs != null) {
            try {
                this.mIMMInterfaceV2 = iFusdLbs.getMMInterface();
                if (this.mIMMInterfaceV2 != null) {
                    LBSLog.i(TAG, false, "LbsService registerMMCallback sending...", new Object[0]);
                    this.mIMMInterfaceV2.registerMMCallback(new MapMatchingCallbackV2());
                    return;
                }
                LBSLog.w(TAG, false, "IMMInterface is null...", new Object[0]);
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "register callback error", new Object[0]);
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class MapMatchingCallbackV2 extends IMMCallback.Stub {
        MapMatchingCallbackV2() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IMMCallback
        public void onMmDataRequest(double latitude, double longitude, int size) throws RemoteException {
            if (MmHardware.this.mMMCallback != null) {
                MmHardware.this.mMMCallback.onMmDataRequest(latitude, longitude, size);
            } else {
                LBSLog.w(MmHardware.TAG, false, "mMMCallback is null.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class MapMatchingCallbackV4 extends IMMCallback.Stub {
        MapMatchingCallbackV4() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IMMCallback
        public void onMmDataRequest(double latitude, double longitude, int size) throws RemoteException {
            if (MmHardware.this.mMMCallback != null) {
                MmHardware.this.mMMCallback.onMmDataRequest(latitude, longitude, size);
            } else {
                LBSLog.w(MmHardware.TAG, false, "mMMCallback is null.", new Object[0]);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.IMMCallback
        public void onPDRMmDataRequest(double latitude, double longitude, int size) throws RemoteException {
            if (MmHardware.this.mMMCallback != null) {
                MmHardware.this.mMMCallback.onPdrMmDataRequest(latitude, longitude, size);
            } else {
                LBSLog.w(MmHardware.TAG, false, "mMMCallback is null.", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class MapMatchingSDMCallback extends ISDMCallback.Stub {
        MapMatchingSDMCallback() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.ISDMCallback
        public void onSdmDataRequest(long tileId, int reserved1) throws RemoteException {
            LBSLog.i(MmHardware.TAG, false, "onSdmDataRequest tile = %{public}l", Long.valueOf(tileId));
            if (MmHardware.this.mSdMCallback != null) {
                MmHardware.this.mSdMCallback.onSdmDataRequest(tileId, reserved1);
            } else {
                LBSLog.w(MmHardware.TAG, false, "mSdMCallback is null.", new Object[0]);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.ISDMCallback
        public void onAckSdmDataResponse(long tileId, int sec) throws RemoteException {
            LBSLog.i(MmHardware.TAG, false, "onAckSdmDataResponse tileId: %{public}l", Long.valueOf(tileId));
            if (MmHardware.this.mSdMCallback != null) {
                MmHardware.this.mSdMCallback.onAckSdmDataResponse(tileId, sec);
            } else {
                LBSLog.w(MmHardware.TAG, false, "mSdMCallback is null.", new Object[0]);
            }
        }
    }
}
