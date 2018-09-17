package android.hardware.usb;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class UsbPortStatus implements Parcelable {
    public static final Creator<UsbPortStatus> CREATOR = null;
    private final int mCurrentDataRole;
    private final int mCurrentMode;
    private final int mCurrentPowerRole;
    private final int mSupportedRoleCombinations;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.usb.UsbPortStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.usb.UsbPortStatus.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.usb.UsbPortStatus.<clinit>():void");
    }

    public UsbPortStatus(int currentMode, int currentPowerRole, int currentDataRole, int supportedRoleCombinations) {
        this.mCurrentMode = currentMode;
        this.mCurrentPowerRole = currentPowerRole;
        this.mCurrentDataRole = currentDataRole;
        this.mSupportedRoleCombinations = supportedRoleCombinations;
    }

    public boolean isConnected() {
        return this.mCurrentMode != 0;
    }

    public int getCurrentMode() {
        return this.mCurrentMode;
    }

    public int getCurrentPowerRole() {
        return this.mCurrentPowerRole;
    }

    public int getCurrentDataRole() {
        return this.mCurrentDataRole;
    }

    public boolean isRoleCombinationSupported(int powerRole, int dataRole) {
        return (this.mSupportedRoleCombinations & UsbPort.combineRolesAsBit(powerRole, dataRole)) != 0;
    }

    public int getSupportedRoleCombinations() {
        return this.mSupportedRoleCombinations;
    }

    public String toString() {
        return "UsbPortStatus{connected=" + isConnected() + ", currentMode=" + UsbPort.modeToString(this.mCurrentMode) + ", currentPowerRole=" + UsbPort.powerRoleToString(this.mCurrentPowerRole) + ", currentDataRole=" + UsbPort.dataRoleToString(this.mCurrentDataRole) + ", supportedRoleCombinations=" + UsbPort.roleCombinationsToString(this.mSupportedRoleCombinations) + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCurrentMode);
        dest.writeInt(this.mCurrentPowerRole);
        dest.writeInt(this.mCurrentDataRole);
        dest.writeInt(this.mSupportedRoleCombinations);
    }
}
