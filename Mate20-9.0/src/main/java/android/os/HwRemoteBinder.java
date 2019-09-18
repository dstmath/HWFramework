package android.os;

import android.os.IHwBinder;
import libcore.util.NativeAllocationRegistry;

public class HwRemoteBinder implements IHwBinder {
    private static final String TAG = "HwRemoteBinder";
    private static final NativeAllocationRegistry sNativeRegistry;
    private long mNativeContext;

    private static final native long native_init();

    private final native void native_setup_empty();

    public final native boolean equals(Object obj);

    public final native int hashCode();

    public native boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j);

    public final native void transact(int i, HwParcel hwParcel, HwParcel hwParcel2, int i2) throws RemoteException;

    public native boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient);

    public HwRemoteBinder() {
        native_setup_empty();
        sNativeRegistry.registerNativeAllocation(this, this.mNativeContext);
    }

    public IHwInterface queryLocalInterface(String descriptor) {
        return null;
    }

    static {
        NativeAllocationRegistry nativeAllocationRegistry = new NativeAllocationRegistry(HwRemoteBinder.class.getClassLoader(), native_init(), 128);
        sNativeRegistry = nativeAllocationRegistry;
    }

    private static final void sendDeathNotice(IHwBinder.DeathRecipient recipient, long cookie) {
        recipient.serviceDied(cookie);
    }
}
