package huawei.android.hardware.usb;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.content.HwContextEx;
import huawei.android.hardware.usb.IHwUsbManagerEx.Stub;

public class HwUsbManagerEx {
    private static final String TAG = "HwUsbManagerEx";
    private static volatile HwUsbManagerEx mInstance;
    IHwUsbManagerEx mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.hardware.usb.HwUsbManagerEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.hardware.usb.HwUsbManagerEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.hardware.usb.HwUsbManagerEx.<clinit>():void");
    }

    public static synchronized HwUsbManagerEx getInstance() {
        HwUsbManagerEx hwUsbManagerEx;
        synchronized (HwUsbManagerEx.class) {
            if (mInstance == null) {
                mInstance = new HwUsbManagerEx();
            }
            hwUsbManagerEx = mInstance;
        }
        return hwUsbManagerEx;
    }

    private HwUsbManagerEx() {
        this.mService = null;
        this.mService = Stub.asInterface(ServiceManager.getService(HwContextEx.HW_USB_EX_SERVICE));
    }

    public void allowUsbHDB(boolean alwaysAllow, String publicKey) {
        try {
            if (this.mService != null) {
                this.mService.allowUsbHDB(alwaysAllow, publicKey);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbEx service binder error!");
        }
    }

    public void denyUsbHDB() {
        try {
            if (this.mService != null) {
                this.mService.denyUsbHDB();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }

    public void clearUsbHDBKeys() {
        try {
            if (this.mService != null) {
                this.mService.clearUsbHDBKeys();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }
}
