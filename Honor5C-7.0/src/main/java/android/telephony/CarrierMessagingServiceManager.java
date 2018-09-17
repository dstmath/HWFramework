package android.telephony;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.service.carrier.ICarrierMessagingService;
import android.service.carrier.ICarrierMessagingService.Stub;
import com.android.internal.util.Preconditions;

public abstract class CarrierMessagingServiceManager {
    private volatile CarrierMessagingServiceConnection mCarrierMessagingServiceConnection;

    private final class CarrierMessagingServiceConnection implements ServiceConnection {
        private CarrierMessagingServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            CarrierMessagingServiceManager.this.onServiceReady(Stub.asInterface(service));
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    protected abstract void onServiceReady(ICarrierMessagingService iCarrierMessagingService);

    public boolean bindToCarrierMessagingService(Context context, String carrierPackageName) {
        Preconditions.checkState(this.mCarrierMessagingServiceConnection == null);
        Intent intent = new Intent("android.service.carrier.CarrierMessagingService");
        intent.setPackage(carrierPackageName);
        this.mCarrierMessagingServiceConnection = new CarrierMessagingServiceConnection();
        return context.bindService(intent, this.mCarrierMessagingServiceConnection, 1);
    }

    public void disposeConnection(Context context) {
        Preconditions.checkNotNull(this.mCarrierMessagingServiceConnection);
        context.unbindService(this.mCarrierMessagingServiceConnection);
        this.mCarrierMessagingServiceConnection = null;
    }
}
