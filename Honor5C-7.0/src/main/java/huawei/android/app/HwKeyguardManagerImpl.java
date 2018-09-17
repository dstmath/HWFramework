package huawei.android.app;

import android.app.HwKeyguardManager;
import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

public class HwKeyguardManagerImpl implements HwKeyguardManager {
    private static final int CODE_IS_LOCKSCREEND_DISABLED = 1000;
    private static HwKeyguardManager mInstance;
    private IWindowManager mWM;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.app.HwKeyguardManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.app.HwKeyguardManagerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.app.HwKeyguardManagerImpl.<clinit>():void");
    }

    public static HwKeyguardManager getDefault() {
        return mInstance;
    }

    public HwKeyguardManagerImpl() {
        this.mWM = WindowManagerGlobal.getWindowManagerService();
    }

    public boolean isLockScreenDisabled(Context context) {
        boolean result = false;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            if (this.mWM != null) {
                IBinder windowManagerBinder = this.mWM.asBinder();
                if (windowManagerBinder != null) {
                    data.writeInterfaceToken("android.view.IWindowManager");
                    windowManagerBinder.transact(1001, data, reply, 0);
                    reply.readException();
                    result = reply.readInt() == 1;
                    Log.d("HwKeyguardManagerImpl", "isLockScreenDisabled HwKeyguardManagerImpl result = " + result);
                }
            }
            reply.recycle();
            data.recycle();
            return result;
        } catch (RemoteException e) {
            return false;
        }
    }
}
