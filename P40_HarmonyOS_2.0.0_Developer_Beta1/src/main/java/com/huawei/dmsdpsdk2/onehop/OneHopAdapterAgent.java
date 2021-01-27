package com.huawei.dmsdpsdk2.onehop;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.onehop.OneHopAdapter;

class OneHopAdapterAgent extends OneHopAdapter {
    private static final String TAG = "OneHopAdapterAgent";

    protected OneHopAdapterAgent(DMSDPAdapter adapter) {
        synchronized (ONEHOP_LOCK) {
            setDmsdpAdapter(adapter);
            IInterface dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.onehop.OneHopAdapterAgent.AnonymousClass1 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            synchronized (OneHopAdapter.ONEHOP_LOCK) {
                                HwLog.i(OneHopAdapterAgent.TAG, "OneHopAdapter onBinderDied");
                                OneHopAdapter.OneHopAdapterCallback callback = OneHopAdapterAgent.this.getOneHopAdapterCallbackAgent();
                                if (callback != null) {
                                    callback.onBinderDied();
                                }
                                OneHopAdapter.releaseInstanceAgent();
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
