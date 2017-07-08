package huawei.android.os;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.content.HwContextEx;
import huawei.android.os.IHwAntiTheftManager.Stub;

public class HwAntiTheftManager {
    private static final String TAG = "AntiTheftManager";
    private static volatile HwAntiTheftManager mInstance;
    IHwAntiTheftManager mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.os.HwAntiTheftManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.os.HwAntiTheftManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.os.HwAntiTheftManager.<clinit>():void");
    }

    public static synchronized HwAntiTheftManager getInstance() {
        HwAntiTheftManager hwAntiTheftManager;
        synchronized (HwAntiTheftManager.class) {
            if (mInstance == null) {
                mInstance = new HwAntiTheftManager();
            }
            hwAntiTheftManager = mInstance;
        }
        return hwAntiTheftManager;
    }

    private HwAntiTheftManager() {
        this.mService = null;
        this.mService = Stub.asInterface(ServiceManager.getService(HwContextEx.HW_ANTI_THEFT_SERVICE));
    }

    public byte[] readAntiTheftData() {
        try {
            if (this.mService != null) {
                return this.mService.readAntiTheftData();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return null;
    }

    public int wipeAntiTheftData() {
        try {
            if (this.mService != null) {
                return this.mService.wipeAntiTheftData();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return -1;
    }

    public int writeAntiTheftData(byte[] writeToNative) {
        try {
            if (this.mService != null) {
                return this.mService.writeAntiTheftData(writeToNative);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return -1;
    }

    public int getAntiTheftDataBlockSize() {
        try {
            if (this.mService != null) {
                return this.mService.getAntiTheftDataBlockSize();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return 0;
    }

    public int setAntiTheftEnabled(boolean enable) {
        try {
            if (this.mService != null) {
                return this.mService.setAntiTheftEnabled(enable);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return -1;
    }

    public boolean getAntiTheftEnabled() {
        try {
            if (this.mService != null) {
                return this.mService.getAntiTheftEnabled();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return false;
    }

    public boolean checkRootState() {
        try {
            if (this.mService != null) {
                return this.mService.checkRootState();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return false;
    }

    public boolean isAntiTheftSupported() {
        try {
            if (this.mService != null) {
                return this.mService.isAntiTheftSupported();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return false;
    }
}
