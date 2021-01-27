package android.hardware;

import android.content.Context;
import android.hardware.IConsumerIrService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public final class ConsumerIrManager {
    private static final String TAG = "ConsumerIr";
    private final String mPackageName;
    private final IConsumerIrService mService = IConsumerIrService.Stub.asInterface(ServiceManager.getServiceOrThrow(Context.CONSUMER_IR_SERVICE));

    public ConsumerIrManager(Context context) throws ServiceManager.ServiceNotFoundException {
        this.mPackageName = context.getPackageName();
    }

    public boolean hasIrEmitter() {
        IConsumerIrService iConsumerIrService = this.mService;
        if (iConsumerIrService == null) {
            Log.w(TAG, "no consumer ir service.");
            return false;
        }
        try {
            return iConsumerIrService.hasIrEmitter();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void transmit(int carrierFrequency, int[] pattern) {
        IConsumerIrService iConsumerIrService = this.mService;
        if (iConsumerIrService == null) {
            Log.w(TAG, "failed to transmit; no consumer ir service.");
            return;
        }
        try {
            iConsumerIrService.transmit(this.mPackageName, carrierFrequency, pattern);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public CarrierFrequencyRange[] getCarrierFrequencies() {
        IConsumerIrService iConsumerIrService = this.mService;
        if (iConsumerIrService == null) {
            Log.w(TAG, "no consumer ir service.");
            return null;
        }
        try {
            int[] freqs = iConsumerIrService.getCarrierFrequencies();
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
