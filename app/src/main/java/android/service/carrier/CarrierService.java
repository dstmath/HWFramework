package android.service.carrier;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.carrier.ICarrierService.Stub;
import com.android.internal.telephony.ITelephonyRegistry;

public abstract class CarrierService extends Service {
    public static final String CARRIER_SERVICE_INTERFACE = "android.service.carrier.CarrierService";
    private static ITelephonyRegistry sRegistry;
    private final Stub mStubWrapper;

    private class ICarrierServiceWrapper extends Stub {
        private ICarrierServiceWrapper() {
        }

        public PersistableBundle getCarrierConfig(CarrierIdentifier id) {
            return CarrierService.this.onLoadConfig(id);
        }
    }

    public abstract PersistableBundle onLoadConfig(CarrierIdentifier carrierIdentifier);

    public CarrierService() {
        this.mStubWrapper = new ICarrierServiceWrapper();
        if (sRegistry == null) {
            sRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        }
    }

    public final void notifyCarrierNetworkChange(boolean active) {
        try {
            if (sRegistry != null) {
                sRegistry.notifyCarrierNetworkChange(active);
            }
        } catch (RemoteException e) {
        }
    }

    public IBinder onBind(Intent intent) {
        return this.mStubWrapper;
    }
}
