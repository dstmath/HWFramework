package huawei.android.net;

import android.os.RemoteException;
import android.os.ServiceManager;
import huawei.android.content.HwContextEx;
import huawei.android.net.IConnectivityExManager.Stub;

public class HwConnectivityExManager {
    private static volatile HwConnectivityExManager mInstance;
    IConnectivityExManager mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.net.HwConnectivityExManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.net.HwConnectivityExManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.net.HwConnectivityExManager.<clinit>():void");
    }

    public static synchronized HwConnectivityExManager getDefault() {
        HwConnectivityExManager hwConnectivityExManager;
        synchronized (HwConnectivityExManager.class) {
            if (mInstance == null) {
                mInstance = new HwConnectivityExManager();
            }
            hwConnectivityExManager = mInstance;
        }
        return hwConnectivityExManager;
    }

    public HwConnectivityExManager() {
        this.mService = null;
        this.mService = Stub.asInterface(ServiceManager.getService(HwContextEx.HW_CONNECTIVITY_EX_SERVICE));
    }

    public void setSmartKeyguardLevel(String level) {
        try {
            this.mService.setSmartKeyguardLevel(level);
        } catch (RemoteException e) {
        }
    }

    public void setUseCtrlSocket(boolean flag) {
    }
}
