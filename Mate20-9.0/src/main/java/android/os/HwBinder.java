package android.os;

import android.annotation.SystemApi;
import java.util.NoSuchElementException;
import libcore.util.NativeAllocationRegistry;

@SystemApi
public abstract class HwBinder implements IHwBinder {
    private static final String TAG = "HwBinder";
    private static final NativeAllocationRegistry sNativeRegistry;
    private long mNativeContext;

    @SystemApi
    public static final native void configureRpcThreadpool(long j, boolean z);

    @SystemApi
    public static final native IHwBinder getService(String str, String str2, boolean z) throws RemoteException, NoSuchElementException;

    @SystemApi
    public static final native void joinRpcThreadpool();

    private static final native long native_init();

    private static native void native_report_sysprop_change();

    private final native void native_setup();

    @SystemApi
    public abstract void onTransact(int i, HwParcel hwParcel, HwParcel hwParcel2, int i2) throws RemoteException;

    @SystemApi
    public final native void registerService(String str) throws RemoteException;

    public final native void transact(int i, HwParcel hwParcel, HwParcel hwParcel2, int i2) throws RemoteException;

    @SystemApi
    public HwBinder() {
        native_setup();
        sNativeRegistry.registerNativeAllocation(this, this.mNativeContext);
    }

    @SystemApi
    public static final IHwBinder getService(String iface, String serviceName) throws RemoteException, NoSuchElementException {
        return getService(iface, serviceName, false);
    }

    static {
        NativeAllocationRegistry nativeAllocationRegistry = new NativeAllocationRegistry(HwBinder.class.getClassLoader(), native_init(), 128);
        sNativeRegistry = nativeAllocationRegistry;
    }

    @SystemApi
    public static void enableInstrumentation() {
        native_report_sysprop_change();
    }

    public static void reportSyspropChanged() {
        native_report_sysprop_change();
    }
}
