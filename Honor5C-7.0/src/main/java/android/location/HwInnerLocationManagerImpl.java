package android.location;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;

public class HwInnerLocationManagerImpl implements IHwInnerLocationManager {
    static final int CODE_GET_POWR_TYPE = 1001;
    private static final String DESCRIPTOR = "android.location.ILocationManager";
    private static final String TAG = "HwInnerLocationManagerImpl";
    private static volatile HwInnerLocationManagerImpl mInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.location.HwInnerLocationManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.location.HwInnerLocationManagerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.location.HwInnerLocationManagerImpl.<clinit>():void");
    }

    public static IHwInnerLocationManager getDefault() {
        return mInstance;
    }

    public int getPowerTypeByPackageName(String packageName) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("location");
        int _result = -1;
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeString(packageName);
            b.transact(CODE_GET_POWR_TYPE, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }
}
