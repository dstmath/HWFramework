package android.os;

import android.annotation.UnsupportedAppUsage;
import android.os.IHwBinder;
import libcore.util.NativeAllocationRegistry;

public class HwRemoteBinder implements IHwBinder {
    private static final String TAG = "HwRemoteBinder";
    private static final NativeAllocationRegistry sNativeRegistry = new NativeAllocationRegistry(HwRemoteBinder.class.getClassLoader(), native_init(), 128);
    private long mNativeContext;

    private static final native long native_init();

    private final native void native_setup_empty();

    public final native boolean equals(Object obj);

    public final native int hashCode();

    @Override // android.os.IHwBinder
    public native boolean linkToDeath(IHwBinder.DeathRecipient deathRecipient, long j);

    @Override // android.os.IHwBinder
    public final native void transact(int i, HwParcel hwParcel, HwParcel hwParcel2, int i2) throws RemoteException;

    @Override // android.os.IHwBinder
    public native boolean unlinkToDeath(IHwBinder.DeathRecipient deathRecipient);

    @UnsupportedAppUsage
    public HwRemoteBinder() {
        native_setup_empty();
        sNativeRegistry.registerNativeAllocation(this, this.mNativeContext);
    }

    @Override // android.os.IHwBinder
    public IHwInterface queryLocalInterface(String descriptor) {
        return null;
    }

    private static final void sendDeathNotice(IHwBinder.DeathRecipient recipient, long cookie) {
        recipient.serviceDied(cookie);
    }
}
