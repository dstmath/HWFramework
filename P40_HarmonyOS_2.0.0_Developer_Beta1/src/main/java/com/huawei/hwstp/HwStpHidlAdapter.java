package com.huawei.hwstp;

import android.os.IHwBinder;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.hwstp.V1_0.IHwStp;
import vendor.huawei.hardware.hwstp.V1_0.StpItem;
import vendor.huawei.hardware.hwstp.V1_1.IHwStpKernelDetectionCallback;

@HwSystemApi
public class HwStpHidlAdapter {
    public static final int EXCEPTION_FAIL = -1002;
    private static final Object HIDL_LOCK = new Object();
    public static final int STP_ROOT_THREAT = 8;
    private static final String STP_SERVICE_NAME = "hwstp";
    public static final int SUCCESS = 0;
    private static final String TAG = "HwStpHidlAdapter";
    private HwStpHidlDeathRecipient mHwStpHidlDeathRecipient;
    private IHwStp mHwStpProxyV1 = getHwStpDaemonV1();
    private vendor.huawei.hardware.hwstp.V1_1.IHwStp mHwStpProxyV2 = getHwStpDaemonV2();
    private KernelDetectionHidlCallback mKernelDetectionHidlCallback;
    private StpGetStatusByCategoryHidlCallback mStpGetStatusByCategoryHidlCallback;
    private StpGetStatusByIdHidlCallback mStpGetStatusByIdHidlCallback;
    private StpGetStatusHidlCallback mStpGetStatusHidlCallback;

    public interface HwStpHidlServiceDiedCallbackWrapper {
        void onServiceDied();
    }

    public interface KernelDetectionCallbackWrapper {
        void onEvent(int i, int i2, int i3);
    }

    public interface StpGetStatusByCategoryCallbackWrapper {
        void onValues(int i, String str);
    }

    public interface StpGetStatusByIdCallbackWrapper {
        void onValues(int i, String str);
    }

    public interface StpGetStatusCallbackWrapper {
        void onValues(int i, String str);
    }

    private IHwStp getHwStpDaemonV1() {
        synchronized (HIDL_LOCK) {
            if (this.mHwStpProxyV1 != null) {
                return this.mHwStpProxyV1;
            }
            Log.v(TAG, "mDaemon was null, reconnect to hwstp service");
            try {
                this.mHwStpProxyV1 = IHwStp.getService(STP_SERVICE_NAME);
            } catch (NoSuchElementException e) {
                Log.e(TAG, "get hwstp 1.0 daemon failed: NoSuchElementException ");
            } catch (RemoteException e2) {
                Log.e(TAG, "get hwstp 1.0 daemon failed: RemoteException");
            }
            if (this.mHwStpProxyV1 == null) {
                Log.e(TAG, "getHwStpDaemonV1: hwstp 1.0 hidl service not available");
                return null;
            }
            return this.mHwStpProxyV1;
        }
    }

    private vendor.huawei.hardware.hwstp.V1_1.IHwStp getHwStpDaemonV2() {
        synchronized (HIDL_LOCK) {
            if (this.mHwStpProxyV2 != null) {
                return this.mHwStpProxyV2;
            }
            getHwStpDaemonV1();
            if (this.mHwStpProxyV1 == null) {
                Log.e(TAG, "getHwStpDaemonV2: hwstp 1.0 hidl service not available");
                return null;
            }
            this.mHwStpProxyV2 = vendor.huawei.hardware.hwstp.V1_1.IHwStp.castFrom((IHwInterface) this.mHwStpProxyV1);
            if (this.mHwStpProxyV2 == null) {
                Log.e(TAG, "hwstp 1.1 hidl service not available");
                return null;
            }
            return this.mHwStpProxyV2;
        }
    }

    public boolean isServiceConnected() {
        boolean z;
        synchronized (HIDL_LOCK) {
            z = this.mHwStpProxyV1 != null;
        }
        return z;
    }

    public void deinitDaemon() {
        synchronized (HIDL_LOCK) {
            this.mHwStpProxyV1 = null;
            this.mHwStpProxyV2 = null;
        }
    }

    public boolean isLinkToDeath(HwStpHidlServiceDiedCallbackWrapper serviceDiedCallback) {
        IHwStp daemonV1 = getHwStpDaemonV1();
        if (daemonV1 == null) {
            return false;
        }
        vendor.huawei.hardware.hwstp.V1_1.IHwStp daemonV2 = getHwStpDaemonV2();
        if (daemonV2 == null) {
            this.mHwStpHidlDeathRecipient = new HwStpHidlDeathRecipient(serviceDiedCallback);
            boolean isLink = daemonV1.asBinder().linkToDeath(this.mHwStpHidlDeathRecipient, 0);
            if (!isLink) {
                Log.e(TAG, "hwstp 1.0 link to death failed");
            }
            return isLink;
        }
        this.mHwStpHidlDeathRecipient = new HwStpHidlDeathRecipient(serviceDiedCallback);
        boolean isLink2 = daemonV2.asBinder().linkToDeath(this.mHwStpHidlDeathRecipient, 0);
        if (!isLink2) {
            Log.e(TAG, "hwstp 1.1 link to death failed");
        }
        return isLink2;
    }

    public int stpAddThreat(int id, byte status, byte credible, byte version, String name, String addition) {
        IHwStp daemonV1 = getHwStpDaemonV1();
        if (daemonV1 == null) {
            return -1002;
        }
        StpItem item = new StpItem();
        item.id = id;
        item.status = status;
        item.credible = credible;
        item.version = version;
        item.name = name;
        vendor.huawei.hardware.hwstp.V1_1.IHwStp daemonV2 = getHwStpDaemonV2();
        if (daemonV2 == null) {
            try {
                return daemonV1.stpAddThreat(item, addition);
            } catch (RemoteException e) {
                Log.e(TAG, "hwstp 1.0: failed to call hidl func stpAddThreat");
                return -1002;
            }
        } else {
            try {
                return daemonV2.stpAddThreat(item, addition);
            } catch (RemoteException e2) {
                Log.e(TAG, "hwstp 1.1: failed to call hidl func stpAddThreat");
                return -1002;
            }
        }
    }

    public int stpGetStatus(boolean inDetail, boolean withHistory, StpGetStatusCallbackWrapper stpGetStatusCallbackWrpper) {
        IHwStp daemonV1 = getHwStpDaemonV1();
        if (daemonV1 == null) {
            return -1002;
        }
        this.mStpGetStatusHidlCallback = new StpGetStatusHidlCallback(stpGetStatusCallbackWrpper);
        vendor.huawei.hardware.hwstp.V1_1.IHwStp daemonV2 = getHwStpDaemonV2();
        if (daemonV2 == null) {
            try {
                daemonV1.stpGetStatus(inDetail, withHistory, this.mStpGetStatusHidlCallback);
                return 0;
            } catch (RemoteException e) {
                Log.e(TAG, "hwstp 1.0: failed to call hidl func stpGetStatus");
                return -1002;
            }
        } else {
            try {
                daemonV2.stpGetStatus(inDetail, withHistory, this.mStpGetStatusHidlCallback);
                return 0;
            } catch (RemoteException e2) {
                Log.e(TAG, "hwstp 1.1: failed to call hidl func stpGetStatus");
                return -1002;
            }
        }
    }

    public int stpGetStatusById(int id, boolean inDetail, boolean withHistory, StpGetStatusByIdCallbackWrapper stpGetStatusByIdCallbackWrapper) {
        IHwStp daemonV1 = getHwStpDaemonV1();
        if (daemonV1 == null) {
            return -1002;
        }
        this.mStpGetStatusByIdHidlCallback = new StpGetStatusByIdHidlCallback(stpGetStatusByIdCallbackWrapper);
        vendor.huawei.hardware.hwstp.V1_1.IHwStp daemonV2 = getHwStpDaemonV2();
        if (daemonV2 == null) {
            try {
                daemonV1.stpGetStatusById(id, inDetail, withHistory, this.mStpGetStatusByIdHidlCallback);
                return 0;
            } catch (RemoteException e) {
                Log.e(TAG, "hwstp 1.0: failed to call hidl func stpGetStatusById");
                return -1002;
            }
        } else {
            try {
                daemonV2.stpGetStatusById(id, inDetail, withHistory, this.mStpGetStatusByIdHidlCallback);
                return 0;
            } catch (RemoteException e2) {
                Log.e(TAG, "hwstp 1.1: failed to call hidl func stpGetStatusById");
                return -1002;
            }
        }
    }

    public int stpGetStatusByCategory(int category, boolean inDetail, boolean withHistory, StpGetStatusByCategoryCallbackWrapper stpGetStatusByCategoryCallbackWrapper) {
        IHwStp daemonV1 = getHwStpDaemonV1();
        if (daemonV1 == null) {
            return -1002;
        }
        this.mStpGetStatusByCategoryHidlCallback = new StpGetStatusByCategoryHidlCallback(stpGetStatusByCategoryCallbackWrapper);
        vendor.huawei.hardware.hwstp.V1_1.IHwStp daemonV2 = getHwStpDaemonV2();
        if (daemonV2 == null) {
            try {
                daemonV1.stpGetStatusByCategory(category, inDetail, withHistory, this.mStpGetStatusByCategoryHidlCallback);
                return 0;
            } catch (RemoteException e) {
                Log.e(TAG, "hwstp 1.0: failed to call hidl func stpGetStatusByCategory");
                return -1002;
            }
        } else {
            try {
                daemonV2.stpGetStatusByCategory(category, inDetail, withHistory, this.mStpGetStatusByCategoryHidlCallback);
                return 0;
            } catch (RemoteException e2) {
                Log.e(TAG, "hwstp 1.1: failed to call hidl func stpGetStatusByCategory");
                return -1002;
            }
        }
    }

    public int stpTriggerKernelDetection(int uid, int enable) {
        vendor.huawei.hardware.hwstp.V1_1.IHwStp daemonV2 = getHwStpDaemonV2();
        if (daemonV2 == null) {
            return -1002;
        }
        try {
            return daemonV2.stpTriggerKernelDetection(uid, enable);
        } catch (RemoteException e) {
            Log.e(TAG, "hwstp 1.1: faild to call hidl func stpTriggerKernelDetection");
            return -1002;
        }
    }

    public int stpRegisterKernelDetectionCallback(KernelDetectionCallbackWrapper kernelDetectionCallbackWrapper) {
        vendor.huawei.hardware.hwstp.V1_1.IHwStp daemonV2 = getHwStpDaemonV2();
        if (daemonV2 == null) {
            return -1002;
        }
        this.mKernelDetectionHidlCallback = new KernelDetectionHidlCallback(kernelDetectionCallbackWrapper);
        try {
            return daemonV2.stpRegisterKernelDetectionCallback(this.mKernelDetectionHidlCallback);
        } catch (RemoteException e) {
            Log.e(TAG, "hwstp 1.1: failed to call hidl func stpRegisterKernelDetectionCallback");
            return -1002;
        }
    }

    public int stpUnregisterKernelDetectionCallback() {
        vendor.huawei.hardware.hwstp.V1_1.IHwStp daemonV2 = getHwStpDaemonV2();
        if (daemonV2 == null) {
            return -1002;
        }
        try {
            return daemonV2.stpUnregisterKernelDetectionCallback();
        } catch (RemoteException e) {
            Log.e(TAG, "hwstp 1.1: failed to call stpUnregisterKernelDetectionCallback");
            return -1002;
        }
    }

    public int stpUpdateKernelDetectionConfig(ArrayList<Integer> confList) {
        vendor.huawei.hardware.hwstp.V1_1.IHwStp daemonV2 = getHwStpDaemonV2();
        if (daemonV2 == null) {
            return -1002;
        }
        try {
            return daemonV2.stpUpdateKernelDetectionConfig(confList);
        } catch (RemoteException e) {
            Log.e(TAG, "hwstp 1.1: failed to call hidl func stpUpdateKernelDetectionConfig");
            return -1002;
        }
    }

    private class StpGetStatusHidlCallback implements IHwStp.stpGetStatusCallback {
        private StpGetStatusCallbackWrapper mStpGetStatusCallbackWrapper;

        private StpGetStatusHidlCallback(StpGetStatusCallbackWrapper callback) {
            this.mStpGetStatusCallbackWrapper = callback;
        }

        @Override // vendor.huawei.hardware.hwstp.V1_0.IHwStp.stpGetStatusCallback
        public void onValues(int stpGetStatusRet, String outBuffer) {
            this.mStpGetStatusCallbackWrapper.onValues(stpGetStatusRet, outBuffer);
        }
    }

    private class StpGetStatusByIdHidlCallback implements IHwStp.stpGetStatusByIdCallback {
        private StpGetStatusByIdCallbackWrapper mStpGetStatusByIdCallbackWrapper;

        private StpGetStatusByIdHidlCallback(StpGetStatusByIdCallbackWrapper callback) {
            this.mStpGetStatusByIdCallbackWrapper = callback;
        }

        @Override // vendor.huawei.hardware.hwstp.V1_0.IHwStp.stpGetStatusByIdCallback
        public void onValues(int stpGetStatusRet, String outBuffer) {
            this.mStpGetStatusByIdCallbackWrapper.onValues(stpGetStatusRet, outBuffer);
        }
    }

    private class StpGetStatusByCategoryHidlCallback implements IHwStp.stpGetStatusByCategoryCallback {
        private StpGetStatusByCategoryCallbackWrapper mStpGetStatusByCategoryCallbackWrapper;

        private StpGetStatusByCategoryHidlCallback(StpGetStatusByCategoryCallbackWrapper callback) {
            this.mStpGetStatusByCategoryCallbackWrapper = callback;
        }

        @Override // vendor.huawei.hardware.hwstp.V1_0.IHwStp.stpGetStatusByCategoryCallback
        public void onValues(int stpGetStatusRet, String outBuffer) {
            this.mStpGetStatusByCategoryCallbackWrapper.onValues(stpGetStatusRet, outBuffer);
        }
    }

    private class KernelDetectionHidlCallback extends IHwStpKernelDetectionCallback.Stub {
        private KernelDetectionCallbackWrapper mKernelDetectionCallbackWrapper;

        private KernelDetectionHidlCallback(KernelDetectionCallbackWrapper callback) {
            this.mKernelDetectionCallbackWrapper = callback;
        }

        @Override // vendor.huawei.hardware.hwstp.V1_1.IHwStpKernelDetectionCallback
        public void onEvent(int uid, int pid, int isMalApp) {
            this.mKernelDetectionCallbackWrapper.onEvent(uid, pid, isMalApp);
        }
    }

    private class HwStpHidlDeathRecipient implements IHwBinder.DeathRecipient {
        private HwStpHidlServiceDiedCallbackWrapper mHwStpServiceDiedCallback;

        private HwStpHidlDeathRecipient(HwStpHidlServiceDiedCallbackWrapper callback) {
            this.mHwStpServiceDiedCallback = callback;
        }

        public void serviceDied(long cookie) {
            synchronized (HwStpHidlAdapter.HIDL_LOCK) {
                HwStpHidlAdapter.this.mHwStpProxyV1 = null;
                HwStpHidlAdapter.this.mHwStpProxyV2 = null;
                this.mHwStpServiceDiedCallback.onServiceDied();
            }
        }
    }
}
