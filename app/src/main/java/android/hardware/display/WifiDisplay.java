package android.hardware.display;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import libcore.util.Objects;

public final class WifiDisplay implements Parcelable {
    public static final Creator<WifiDisplay> CREATOR = null;
    public static final WifiDisplay[] EMPTY_ARRAY = null;
    private final boolean mCanConnect;
    private final String mDeviceAddress;
    private final String mDeviceAlias;
    private final String mDeviceName;
    private final boolean mIsAvailable;
    private final boolean mIsRemembered;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.display.WifiDisplay.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.display.WifiDisplay.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.display.WifiDisplay.<clinit>():void");
    }

    public WifiDisplay(String deviceAddress, String deviceName, String deviceAlias, boolean available, boolean canConnect, boolean remembered) {
        if (deviceAddress == null) {
            throw new IllegalArgumentException("deviceAddress must not be null");
        } else if (deviceName == null) {
            throw new IllegalArgumentException("deviceName must not be null");
        } else {
            this.mDeviceAddress = deviceAddress;
            this.mDeviceName = deviceName;
            this.mDeviceAlias = deviceAlias;
            this.mIsAvailable = available;
            this.mCanConnect = canConnect;
            this.mIsRemembered = remembered;
        }
    }

    public String getDeviceAddress() {
        return this.mDeviceAddress;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public String getDeviceAlias() {
        return this.mDeviceAlias;
    }

    public boolean isAvailable() {
        return this.mIsAvailable;
    }

    public boolean canConnect() {
        return this.mCanConnect;
    }

    public boolean isRemembered() {
        return this.mIsRemembered;
    }

    public String getFriendlyDisplayName() {
        return this.mDeviceAlias != null ? this.mDeviceAlias : this.mDeviceName;
    }

    public boolean equals(Object o) {
        return o instanceof WifiDisplay ? equals((WifiDisplay) o) : false;
    }

    public boolean equals(WifiDisplay other) {
        if (other != null && this.mDeviceAddress.equals(other.mDeviceAddress) && this.mDeviceName.equals(other.mDeviceName)) {
            return Objects.equal(this.mDeviceAlias, other.mDeviceAlias);
        }
        return false;
    }

    public boolean hasSameAddress(WifiDisplay other) {
        return other != null ? this.mDeviceAddress.equals(other.mDeviceAddress) : false;
    }

    public int hashCode() {
        return this.mDeviceAddress.hashCode();
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.mDeviceAddress);
        dest.writeString(this.mDeviceName);
        dest.writeString(this.mDeviceAlias);
        if (this.mIsAvailable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mCanConnect) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.mIsRemembered) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        String result = this.mDeviceName + " (" + this.mDeviceAddress + ")";
        if (this.mDeviceAlias != null) {
            result = result + ", alias " + this.mDeviceAlias;
        }
        return result + ", isAvailable " + this.mIsAvailable + ", canConnect " + this.mCanConnect + ", isRemembered " + this.mIsRemembered;
    }
}
