package com.huawei.dmsdpsdk2.hicar;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.DMSDPAdapter;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.hicar.CarAdapter;

class CarAdapterProxy extends CarAdapter {
    private static final String TAG = "CarAdapterProxy";

    protected CarAdapterProxy(DMSDPAdapter adapter) {
        synchronized (CAR_LOCK) {
            setDmsdpAdapter(adapter);
            IInterface dmsdpService = adapter.getDMSDPService();
            if (dmsdpService != null) {
                try {
                    dmsdpService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.dmsdpsdk2.hicar.CarAdapterProxy.AnonymousClass1 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            synchronized (CarAdapter.CAR_LOCK) {
                                HwLog.i(CarAdapterProxy.TAG, "CarAdapter onBinderDied");
                                CarAdapter.CarAdapterCallback carAdapterCallback = CarAdapterProxy.this.getCarAdapterCallback();
                                if (carAdapterCallback != null) {
                                    carAdapterCallback.onBinderDied();
                                }
                                CarAdapter.releaseInstance();
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
