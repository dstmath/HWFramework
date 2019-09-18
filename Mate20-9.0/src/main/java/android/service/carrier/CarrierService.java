package android.service.carrier;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.service.carrier.ICarrierService;
import android.util.Log;
import com.android.internal.telephony.ITelephonyRegistry;

public abstract class CarrierService extends Service {
    public static final String CARRIER_SERVICE_INTERFACE = "android.service.carrier.CarrierService";
    private static final String LOG_TAG = "CarrierService";
    private static ITelephonyRegistry sRegistry;
    private final ICarrierService.Stub mStubWrapper = new ICarrierServiceWrapper();

    public class ICarrierServiceWrapper extends ICarrierService.Stub {
        public static final String KEY_CONFIG_BUNDLE = "config_bundle";
        public static final int RESULT_ERROR = 1;
        public static final int RESULT_OK = 0;

        public ICarrierServiceWrapper() {
        }

        public void getCarrierConfig(CarrierIdentifier id, ResultReceiver result) {
            try {
                Bundle data = new Bundle();
                data.putParcelable(KEY_CONFIG_BUNDLE, CarrierService.this.onLoadConfig(id));
                result.send(0, data);
            } catch (Exception e) {
                Log.e(CarrierService.LOG_TAG, "Error in onLoadConfig: " + e.getMessage(), e);
                result.send(1, null);
            }
        }
    }

    public abstract PersistableBundle onLoadConfig(CarrierIdentifier carrierIdentifier);

    public CarrierService() {
        if (sRegistry == null) {
            sRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService("telephony.registry"));
        }
    }

    public final void notifyCarrierNetworkChange(boolean active) {
        try {
            if (sRegistry != null) {
                sRegistry.notifyCarrierNetworkChange(active);
            }
        } catch (RemoteException | NullPointerException e) {
        }
    }

    public IBinder onBind(Intent intent) {
        return this.mStubWrapper;
    }
}
