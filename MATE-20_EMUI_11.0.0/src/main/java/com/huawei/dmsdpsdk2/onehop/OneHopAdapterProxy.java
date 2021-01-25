package com.huawei.dmsdpsdk2.onehop;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.onehop.OneHopAdapter;

class OneHopAdapterProxy extends OneHopAdapter {
    private static final String TAG = "OneHopAdapterProxy";

    protected OneHopAdapterProxy(DMSDPAdapter adapter) {
        synchronized (ONEHOP_LOCK) {
            setDmsdpAdapter(adapter);
            IInterface dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.onehop.OneHopAdapterProxy.AnonymousClass1 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            synchronized (OneHopAdapter.ONEHOP_LOCK) {
                                HwLog.i(OneHopAdapterProxy.TAG, "OneHopAdapter onBinderDied");
                                OneHopAdapter.OneHopAdapterCallback callback = OneHopAdapterProxy.this.getOneHopAdapterCallback();
                                if (callback != null) {
                                    callback.onBinderDied();
                                }
                                OneHopAdapter.releaseInstance();
                                DMSDPAdapter.disableVirtualAudio();
                            }
                        }
                    }, 0);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "Call service linkToDeath RemoteException");
                }
            }
        }
    }
}
