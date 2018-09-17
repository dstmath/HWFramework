package android.hardware;

import android.content.Context;
import android.hardware.IConsumerIrService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceManager.ServiceNotFoundException;
import android.util.Log;

public final class ConsumerIrManager {
    private static final String TAG = "ConsumerIr";
    private final String mPackageName;
    private final IConsumerIrService mService = Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CONSUMER_IR_SERVICE));

    public final class CarrierFrequencyRange {
        private final int mMaxFrequency;
        private final int mMinFrequency;

        public CarrierFrequencyRange(int min, int max) {
            this.mMinFrequency = min;
            this.mMaxFrequency = max;
        }

        public int getMinFrequency() {
            return this.mMinFrequency;
        }

        public int getMaxFrequency() {
            return this.mMaxFrequency;
        }
    }

    public ConsumerIrManager(Context context) throws ServiceNotFoundException {
        this.mPackageName = context.getPackageName();
    }

    public boolean hasIrEmitter() {
        if (this.mService == null) {
            Log.w(TAG, "no consumer ir service.");
            return false;
        }
        try {
            return this.mService.hasIrEmitter();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void transmit(int carrierFrequency, int[] pattern) {
        if (this.mService == null) {
            Log.w(TAG, "failed to transmit; no consumer ir service.");
            return;
        }
        try {
            this.mService.transmit(this.mPackageName, carrierFrequency, pattern);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public CarrierFrequencyRange[] getCarrierFrequencies() {
        if (this.mService == null) {
            Log.w(TAG, "no consumer ir service.");
            return null;
        }
        try {
            int[] freqs = this.mService.getCarrierFrequencies();
            if (freqs.length % 2 != 0) {
                Log.w(TAG, "consumer ir service returned an uneven number of frequencies.");
                return null;
            }
            CarrierFrequencyRange[] range = new CarrierFrequencyRange[(freqs.length / 2)];
            for (int i = 0; i < freqs.length; i += 2) {
                range[i / 2] = new CarrierFrequencyRange(freqs[i], freqs[i + 1]);
            }
            return range;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
