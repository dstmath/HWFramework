package android.bluetooth;

public class BluetoothAddressNative {
    public static final native String getMacAddress();

    public static final native boolean isLibReady();

    static {
        System.loadLibrary("btaddrjni");
    }
}
