package com.android.server.location;

import android.content.Context;
import android.os.RemoteException;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.fusd.V1_4.IFusdLbs;
import vendor.huawei.hardware.fusd.V1_4.IGnssEEData;
import vendor.huawei.hardware.fusd.V1_4.IGnssEEDataCallback;

public class PgnssHardware {
    private static final String TAG = "PgnssHardware";
    private static final Object WATCHER_LOCK = new Object();
    private static PgnssHardware sPgnssHardware;
    private PgnssDataCallback mCallback;
    private IFusdLbs mFusdLbs;

    public static PgnssHardware getInstance(Context context, PgnssDataCallback callback) {
        PgnssHardware pgnssHardware;
        synchronized (WATCHER_LOCK) {
            if (sPgnssHardware == null) {
                sPgnssHardware = new PgnssHardware(context, callback);
            }
            pgnssHardware = sPgnssHardware;
        }
        return pgnssHardware;
    }

    private PgnssHardware(Context context, PgnssDataCallback callback) {
        getIFusdLbsService();
        this.mCallback = callback;
        FusedLbsServiceConnect.getInstance().registerServiceDiedNotify(new IFusedLbsServiceDied() {
            /* class com.android.server.location.PgnssHardware.AnonymousClass1 */

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceDied() {
            }

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceConnect() {
                PgnssHardware.this.getIFusdLbsService();
            }
        });
        LBSLog.i(TAG, false, "PgnssHardware init completed.", new Object[0]);
    }

    public boolean sendEeData(String data) {
        if (data == null) {
            LBSLog.e(TAG, false, "sendMmData data is null.", new Object[0]);
            return false;
        }
        getIFusdLbsService();
        try {
            if (this.mFusdLbs == null) {
                return true;
            }
            IGnssEEData gnssEeData = this.mFusdLbs.getExtensionEEData();
            if (gnssEeData == null) {
                LBSLog.e(TAG, false, "gnssEeData is null", new Object[0]);
                return false;
            }
            gnssEeData.injectEEData(data);
            return true;
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "IMMInterface error", new Object[0]);
            return false;
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception sendEeData", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void getIFusdLbsService() {
        if (this.mFusdLbs == null) {
            try {
                this.mFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_4();
                if (!(this.mFusdLbs == null || this.mFusdLbs.getExtensionEEData() == null)) {
                    this.mFusdLbs.getExtensionEEData().setCallback(new GnssEEDataCallback());
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "getExtensionEEData failed", new Object[0]);
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception setCallback", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class GnssEEDataCallback extends IGnssEEDataCallback.Stub {
        GnssEEDataCallback() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_4.IGnssEEDataCallback
        public void downloadRequestCb() {
            if (PgnssHardware.this.mCallback != null) {
                PgnssHardware.this.mCallback.downloadRequestCb();
            }
        }
    }
}
