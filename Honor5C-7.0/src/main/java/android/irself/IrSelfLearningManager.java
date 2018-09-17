package android.irself;

import android.irself.IIrSelfLearningManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class IrSelfLearningManager {
    public static final String IRlearning_SERVICE = "SelfbuildIRService";
    private static final String TAG = "IrSelfLearningManager";
    private static final Object mInstanceSync = null;
    private static IIrSelfLearningManager mService;
    private static volatile IrSelfLearningManager sSelf;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.irself.IrSelfLearningManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.irself.IrSelfLearningManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.irself.IrSelfLearningManager.<clinit>():void");
    }

    private IrSelfLearningManager() {
        mService = Stub.asInterface(ServiceManager.getService(IRlearning_SERVICE));
        Log.d(TAG, "mService =" + mService);
    }

    private static IIrSelfLearningManager getIrSelfLearningManagerService() {
        synchronized (mInstanceSync) {
            if (mService != null) {
                IIrSelfLearningManager iIrSelfLearningManager = mService;
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
