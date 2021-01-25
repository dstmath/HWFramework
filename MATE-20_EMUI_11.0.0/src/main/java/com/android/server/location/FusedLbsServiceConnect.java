package com.android.server.location;

import android.os.Handler;
import android.os.IHwBinder;
import android.os.Message;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import vendor.huawei.hardware.fusd.V1_2.IFusdLbs;

public class FusedLbsServiceConnect {
    private static final int MSG_CONNECTED = 1;
    private static final int MSG_DISCONNECTED = 2;
    private static final int MSG_RECONNECTION = 0;
    private static final Object NOTIFY_LOCK = new Object();
    private static final int RETRY_TIME = 3;
    private static final int RETRY_TIME_OUT = 2000;
    private static final String TAG = "HigeoLocationProvider.FusedLbsServiceConnect";
    private static final Object WATCHER_LOCK = new Object();
    private static volatile FusedLbsServiceConnect sFusedLbsServiceConnect;
    private List<IFusedLbsServiceDied> fusedLbsDiedList = new ArrayList();
    private ConcurrentHashMap<HidlServiceDeathHandler, Object> mDeathHandlerMap = new ConcurrentHashMap<>();
    private Handler mHandler = new Handler() {
        /* class com.android.server.location.FusedLbsServiceConnect.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                if (FusedLbsServiceConnect.this.getIFusdLbsServiceV1_5() == null && FusedLbsServiceConnect.this.getIFusdLbsServiceV1_4() == null && FusedLbsServiceConnect.this.getIFusdLbsServiceV1_3() == null) {
                    FusedLbsServiceConnect.this.getIFusdLbsServiceV1_2();
                }
                FusedLbsServiceConnect.access$008(FusedLbsServiceConnect.this);
                if (FusedLbsServiceConnect.this.mReconCount <= 3) {
                    synchronized (FusedLbsServiceConnect.NOTIFY_LOCK) {
                        FusedLbsServiceConnect.this.mHandler.sendEmptyMessageDelayed(0, 2000);
                    }
                }
            } else if (i == 1) {
                synchronized (FusedLbsServiceConnect.NOTIFY_LOCK) {
                    for (IFusedLbsServiceDied fused : FusedLbsServiceConnect.this.fusedLbsDiedList) {
                        fused.onFusedLbsServiceConnect();
                    }
                }
            } else if (i == 2) {
                synchronized (FusedLbsServiceConnect.NOTIFY_LOCK) {
                    for (IFusedLbsServiceDied fused2 : FusedLbsServiceConnect.this.fusedLbsDiedList) {
                        fused2.onFusedLbsServiceDied();
                    }
                }
            }
        }
    };
    private IFusdLbs mIFusdLbsV2;
    private vendor.huawei.hardware.fusd.V1_3.IFusdLbs mIFusdLbsV3;
    private vendor.huawei.hardware.fusd.V1_4.IFusdLbs mIFusdLbsV4;
    private vendor.huawei.hardware.fusd.V1_5.IFusdLbs mIFusdLbsV5;
    private int mReconCount = 0;

    static /* synthetic */ int access$008(FusedLbsServiceConnect x0) {
        int i = x0.mReconCount;
        x0.mReconCount = i + 1;
        return i;
    }

    private FusedLbsServiceConnect() {
    }

    public void registerServiceDiedNotify(IFusedLbsServiceDied fusedLbsDied) {
        synchronized (NOTIFY_LOCK) {
            this.fusedLbsDiedList.add(fusedLbsDied);
        }
    }

    public static FusedLbsServiceConnect getInstance() {
        if (sFusedLbsServiceConnect == null) {
            synchronized (WATCHER_LOCK) {
                if (sFusedLbsServiceConnect == null) {
                    sFusedLbsServiceConnect = new FusedLbsServiceConnect();
                }
            }
        }
        return sFusedLbsServiceConnect;
    }

    public synchronized IFusdLbs getIFusdLbsServiceV1_2() {
        if (this.mIFusdLbsV2 != null) {
            return this.mIFusdLbsV2;
        }
        try {
            this.mIFusdLbsV2 = IFusdLbs.getService();
            if (this.mIFusdLbsV2 != null) {
                LBSLog.d(TAG, false, "getIFusdLbsServiceV1_2 service ok.", new Object[0]);
                HidlServiceDeathHandler hidlDeathHandler = new HidlServiceDeathHandler();
                this.mIFusdLbsV2.linkToDeath(hidlDeathHandler, 0);
                this.mDeathHandlerMap.put(hidlDeathHandler, this.mIFusdLbsV2);
                this.mHandler.removeMessages(0);
                this.mHandler.sendEmptyMessage(1);
            }
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "Exception getting mIFusdLbsV2", new Object[0]);
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception mIFusdLbsV2.", new Object[0]);
        }
        return this.mIFusdLbsV2;
    }

    public synchronized vendor.huawei.hardware.fusd.V1_3.IFusdLbs getIFusdLbsServiceV1_3() {
        if (this.mIFusdLbsV3 != null) {
            return this.mIFusdLbsV3;
        }
        try {
            this.mIFusdLbsV3 = vendor.huawei.hardware.fusd.V1_3.IFusdLbs.getService();
            if (this.mIFusdLbsV3 != null) {
                LBSLog.d(TAG, false, "getIFusdLbsServiceV1_3 service ok.", new Object[0]);
                HidlServiceDeathHandler hidlDeathHandler = new HidlServiceDeathHandler();
                this.mIFusdLbsV3.linkToDeath(hidlDeathHandler, 0);
                this.mDeathHandlerMap.put(hidlDeathHandler, this.mIFusdLbsV3);
                this.mHandler.removeMessages(0);
                this.mHandler.sendEmptyMessage(1);
            }
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "Exception getting mIFusdLbsV3", new Object[0]);
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception mIFusdLbsV3.", new Object[0]);
        }
        return this.mIFusdLbsV3;
    }

    public synchronized vendor.huawei.hardware.fusd.V1_4.IFusdLbs getIFusdLbsServiceV1_4() {
        if (this.mIFusdLbsV4 != null) {
            return this.mIFusdLbsV4;
        }
        try {
            this.mIFusdLbsV4 = vendor.huawei.hardware.fusd.V1_4.IFusdLbs.getService();
            if (this.mIFusdLbsV4 != null) {
                LBSLog.d(TAG, false, "getIFusdLbsServiceV1_4 service ok.", new Object[0]);
                HidlServiceDeathHandler hidlDeathHandler = new HidlServiceDeathHandler();
                this.mIFusdLbsV4.linkToDeath(hidlDeathHandler, 0);
                this.mDeathHandlerMap.put(hidlDeathHandler, this.mIFusdLbsV4);
                this.mHandler.removeMessages(0);
                this.mHandler.sendEmptyMessage(1);
            }
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "Exception getting mIFusdLbsV4", new Object[0]);
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception mIFusdLbsV4.", new Object[0]);
        }
        return this.mIFusdLbsV4;
    }

    public synchronized vendor.huawei.hardware.fusd.V1_5.IFusdLbs getIFusdLbsServiceV1_5() {
        if (this.mIFusdLbsV5 != null) {
            return this.mIFusdLbsV5;
        }
        try {
            this.mIFusdLbsV5 = vendor.huawei.hardware.fusd.V1_5.IFusdLbs.getService();
            if (this.mIFusdLbsV5 != null) {
                LBSLog.d(TAG, false, "getIFusdLbsServiceV1_5 service ok.", new Object[0]);
                HidlServiceDeathHandler hidlDeathHandler = new HidlServiceDeathHandler();
                this.mIFusdLbsV5.linkToDeath(hidlDeathHandler, 0);
                this.mDeathHandlerMap.put(hidlDeathHandler, this.mIFusdLbsV5);
                this.mHandler.removeMessages(0);
                this.mHandler.sendEmptyMessage(1);
            }
        } catch (RemoteException e) {
            LBSLog.e(TAG, false, "Exception getting mIFusdLbsV5", new Object[0]);
        } catch (NoSuchElementException e2) {
            LBSLog.e(TAG, false, "No Such Element Exception mIFusdLbsV1_5.", new Object[0]);
        }
        return this.mIFusdLbsV5;
    }

    /* access modifiers changed from: private */
    public class HidlServiceDeathHandler implements IHwBinder.DeathRecipient {
        private HidlServiceDeathHandler() {
        }

        public void serviceDied(long cookie) {
            try {
                synchronized (FusedLbsServiceConnect.NOTIFY_LOCK) {
                    if (!FusedLbsServiceConnect.this.mDeathHandlerMap.isEmpty()) {
                        for (Map.Entry<HidlServiceDeathHandler, Object> entry : FusedLbsServiceConnect.this.mDeathHandlerMap.entrySet()) {
                            Object fusdLbsObj = entry.getValue();
                            if (fusdLbsObj != null && (fusdLbsObj instanceof IFusdLbs)) {
                                ((IFusdLbs) fusdLbsObj).unlinkToDeath((IHwBinder.DeathRecipient) entry.getKey());
                            }
                        }
                        FusedLbsServiceConnect.this.mDeathHandlerMap.clear();
                        FusedLbsServiceConnect.this.mIFusdLbsV5 = null;
                        FusedLbsServiceConnect.this.mIFusdLbsV4 = null;
                        FusedLbsServiceConnect.this.mIFusdLbsV3 = null;
                        FusedLbsServiceConnect.this.mIFusdLbsV2 = null;
                        FusedLbsServiceConnect.this.mHandler.sendEmptyMessage(2);
                        FusedLbsServiceConnect.this.mHandler.sendEmptyMessageDelayed(0, 2000);
                    }
                }
            } catch (RemoteException e) {
                LBSLog.e(FusedLbsServiceConnect.TAG, false, "Exception unlinkToDeath", new Object[0]);
            } catch (NoSuchElementException e2) {
                LBSLog.e(FusedLbsServiceConnect.TAG, false, "No Such Element Exception unlinkToDeath", new Object[0]);
            }
        }
    }
}
