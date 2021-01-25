package com.huawei.dmsdpsdk2.pad;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.pad.PadAdapter;

class PadAdapterAgent extends PadAdapter {
    private static final String TAG = "PadAdapterAgent";

    protected PadAdapterAgent(DMSDPAdapter adapter) {
        synchronized (PAD_LOCK) {
            setDmsdpAdapter(adapter);
            IInterface dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.pad.PadAdapterAgent.AnonymousClass1 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            synchronized (PadAdapter.PAD_LOCK) {
                                HwLog.i(PadAdapterAgent.TAG, "PadAdapter onBinderDied");
                                PadAdapter.PadAdapterCallback callback = PadAdapterAgent.this.getPadAdapterCallbackAgent();
                                if (callback != null) {
                                    callback.onBinderDied();
                                }
                                PadAdapter.releaseInstanceAgent();
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
