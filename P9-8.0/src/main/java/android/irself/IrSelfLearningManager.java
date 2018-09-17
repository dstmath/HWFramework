package android.irself;

import android.irself.IIrSelfLearningManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class IrSelfLearningManager {
    public static final String IRlearning_SERVICE = "SelfbuildIRService";
    private static final String TAG = "IrSelfLearningManager";
    private static final Object mInstanceSync = new Object();
    private static IIrSelfLearningManager mService;
    private static volatile IrSelfLearningManager sSelf = null;

    private IrSelfLearningManager() {
        mService = Stub.asInterface(ServiceManager.getService(IRlearning_SERVICE));
        Log.d(TAG, "mService =" + mService);
    }

    private static IIrSelfLearningManager getIrSelfLearningManagerService() {
        synchronized (mInstanceSync) {
            IIrSelfLearningManager iIrSelfLearningManager;
            if (mService != null) {
                iIrSelfLearningManager = mService;
                return iIrSelfLearningManager;
            }
            mService = Stub.asInterface(ServiceManager.getService(IRlearning_SERVICE));
            iIrSelfLearningManager = mService;
            return iIrSelfLearningManager;
        }
    }

    public static IrSelfLearningManager getDefault() {
        if (sSelf == null) {
            sSelf = new IrSelfLearningManager();
        }
        return sSelf;
    }

    public boolean hasIrSelfLearning() {
        try {
            IIrSelfLearningManager hasIrServices = getIrSelfLearningManagerService();
            if (hasIrServices != null) {
                return hasIrServices.hasIrSelfLearning();
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Exception in judging whether the device supports IRSelfLearning .");
        }
        return false;
    }

    public boolean deviceInit() {
        try {
            IIrSelfLearningManager initServices = getIrSelfLearningManagerService();
            if (initServices != null) {
                return initServices.deviceInit();
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Exception in initting the device.");
        }
        return false;
    }

    public boolean deviceExit() {
        try {
            IIrSelfLearningManager exitServices = getIrSelfLearningManagerService();
            if (exitServices != null) {
                return exitServices.deviceExit();
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Exception in exitting the device.");
        }
        return false;
    }

    public boolean transmit(int carrierFrequency, int[] pattern) {
        try {
            IIrSelfLearningManager transmitServices = getIrSelfLearningManagerService();
            if (transmitServices != null) {
                return transmitServices.transmit(carrierFrequency, pattern);
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Exception in transmitting.");
        }
        return false;
    }

    public boolean startLearning() {
        try {
            IIrSelfLearningManager startLeaServices = getIrSelfLearningManagerService();
            if (startLeaServices != null) {
                return startLeaServices.startLearning();
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Exception in starting learning.");
        }
        return false;
    }

    public boolean stopLearning() {
        try {
            IIrSelfLearningManager stopLeaServices = getIrSelfLearningManagerService();
            if (stopLeaServices != null) {
                return stopLeaServices.stopLearning();
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Exception in stopping learning.");
        }
        return false;
    }

    public boolean getLearningStatus() {
        try {
            IIrSelfLearningManager getLeaServices = getIrSelfLearningManagerService();
            if (getLeaServices != null) {
                return getLeaServices.getLearningStatus();
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Exception in getting learning status.");
        }
        return false;
    }

    public int readIRFrequency() {
        try {
            IIrSelfLearningManager readIRServices = getIrSelfLearningManagerService();
            int freq = -1;
            if (readIRServices != null) {
                freq = readIRServices.readIRFrequency();
            }
            if (freq < 0) {
                Log.w(TAG, "Error halReadIRFrequency : " + freq);
            }
            return freq;
        } catch (RemoteException e) {
            Log.d(TAG, "Exception in readding IR Frequency.");
            return -1;
        }
    }

    public int[] readIRCode() {
        int[] mIRCode = new int[50];
        try {
            IIrSelfLearningManager readIRCodeServices = getIrSelfLearningManagerService();
            if (readIRCodeServices != null) {
                return readIRCodeServices.readIRCode();
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Exception in readding IR code.");
        }
        return mIRCode;
    }
}
