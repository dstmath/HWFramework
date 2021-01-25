package com.huawei.dmsdpsdk2.pad;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.pad.PadAdapter;

class PadAdapterProxy extends PadAdapter {
    private static final String TAG = "PadAdapterProxy";

    protected PadAdapterProxy(DMSDPAdapter adapter) {
        synchronized (PAD_LOCK) {
            setDmsdpAdapter(adapter);
            IInterface dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.pad.PadAdapterProxy.AnonymousClass1 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            synchronized (PadAdapter.PAD_LOCK) {
                                HwLog.i(PadAdapterProxy.TAG, "PadAdapter onBinderDied");
                                PadAdapter.PadAdapterCallback callback = PadAdapterProxy.this.getPadAdapterCallback();
                                if (callback != null) {
                                    callback.onBinderDied();
                                }
                                PadAdapter.releaseInstance();
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
