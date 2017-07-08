package com.huawei.android.app;

import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.util.Singleton;
import android.view.IWindowManager;

public class WindowManagerEx {
    private static final int IS_INPUT_METHOD_VISIBLE_TOKEN = 1004;
    private static final String LOG_TAG = "WindowManagerEx";
    private static final Singleton<IWindowManager> gDefault = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.app.WindowManagerEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.app.WindowManagerEx.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.app.WindowManagerEx.<clinit>():void");
    }

    public static boolean isInputMethodVisible() throws RemoteException {
        int ret = 0;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            getDefault().asBinder().transact(IS_INPUT_METHOD_VISIBLE_TOKEN, data, reply, 0);
            reply.readException();
            ret = reply.readInt();
            Log.e(LOG_TAG, "ret: " + ret);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "isInputMethodVisible", e);
        }
        if (ret > 0) {
            return true;
        }
        return false;
    }

    private static IWindowManager getDefault() {
        return (IWindowManager) gDefault.get();
    }
}
