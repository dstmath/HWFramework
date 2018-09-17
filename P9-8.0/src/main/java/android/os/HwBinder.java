package android.os;

import java.util.NoSuchElementException;
import libcore.util.NativeAllocationRegistry;

public abstract class HwBinder implements IHwBinder {
    private static final String TAG = "HwBinder";
    private static final NativeAllocationRegistry sNativeRegistry = new NativeAllocationRegistry(HwBinder.class.getClassLoader(), native_init(), 128);
    private long mNativeContext;

    public static final native IHwBinder getService(String str, String str2) throws RemoteException, NoSuchElementException;

    private static final native long native_init();

    private final native void native_setup();

    public abstract void onTransact(int i, HwParcel hwParcel, HwParcel hwParcel2, int i2) throws RemoteException;

    public final native void registerService(String str) throws RemoteException;

    public final native void transact(int i, HwParcel hwParcel, HwParcel hwParcel2, int i2) throws RemoteException;

    public HwBinder() {
        native_setup();
        sNativeRegistry.registerNativeAllocation(this, this.mNativeContext);
    }
}
