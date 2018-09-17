package android.os;

import android.os.IHwBinder.DeathRecipient;
import libcore.util.NativeAllocationRegistry;

public class HwRemoteBinder implements IHwBinder {
    private static final String TAG = "HwRemoteBinder";
    private static final NativeAllocationRegistry sNativeRegistry = new NativeAllocationRegistry(HwRemoteBinder.class.getClassLoader(), native_init(), 128);
    private long mNativeContext;

    private static final native long native_init();

    private final native void native_setup_empty();

    public native boolean linkToDeath(DeathRecipient deathRecipient, long j);

    public final native void transact(int i, HwParcel hwParcel, HwParcel hwParcel2, int i2) throws RemoteException;

    public native boolean unlinkToDeath(DeathRecipient deathRecipient);

    public HwRemoteBinder() {
        native_setup_empty();
        sNativeRegistry.registerNativeAllocation(this, this.mNativeContext);
    }

    public IHwInterface queryLocalInterface(String descriptor) {
        return null;
    }

    private static final void sendDeathNotice(DeathRecipient recipient, long cookie) {
        recipient.serviceDied(cookie);
    }
}
