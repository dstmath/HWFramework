package android.os;

import android.os.Parcelable.Creator;

public class BatteryProperties implements Parcelable {
    public static final Creator<BatteryProperties> CREATOR = null;
    public int batteryChargeCounter;
    public int batteryHealth;
    public int batteryLevel;
    public boolean batteryPresent;
    public int batteryStatus;
    public String batteryTechnology;
    public int batteryTemperature;
    public int batteryVoltage;
    public boolean chargerAcOnline;
    public boolean chargerUsbOnline;
    public boolean chargerWirelessOnline;
    public int maxChargingCurrent;
    public int maxChargingVoltage;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.BatteryProperties.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.BatteryProperties.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.BatteryProperties.<clinit>():void");
    }

    public void set(BatteryProperties other) {
        this.chargerAcOnline = other.chargerAcOnline;
        this.chargerUsbOnline = other.chargerUsbOnline;
        this.chargerWirelessOnline = other.chargerWirelessOnline;
        this.maxChargingCurrent = other.maxChargingCurrent;
        this.maxChargingVoltage = other.maxChargingVoltage;
        this.batteryStatus = other.batteryStatus;
        this.batteryHealth = other.batteryHealth;
        this.batteryPresent = other.batteryPresent;
        this.batteryLevel = other.batteryLevel;
        this.batteryVoltage = other.batteryVoltage;
        this.batteryTemperature = other.batteryTemperature;
        this.batteryChargeCounter = other.batteryChargeCounter;
        this.batteryTechnology = other.batteryTechnology;
    }

    private BatteryProperties(Parcel p) {
        boolean z;
        boolean z2 = true;
        if (p.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.chargerAcOnline = z;
        if (p.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.chargerUsbOnline = z;
        if (p.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.chargerWirelessOnline = z;
        this.maxChargingCurrent = p.readInt();
        this.maxChargingVoltage = p.readInt();
        this.batteryStatus = p.readInt();
        this.batteryHealth = p.readInt();
        if (p.readInt() != 1) {
            z2 = false;
        }
        this.batteryPresent = z2;
        this.batteryLevel = p.readInt();
        this.batteryVoltage = p.readInt();
        this.batteryTemperature = p.readInt();
        this.batteryChargeCounter = p.readInt();
        this.batteryTechnology = p.readString();
    }

    public void writeToParcel(Parcel p, int flags) {
        int i;
        int i2 = 1;
        if (this.chargerAcOnline) {
            i = 1;
        } else {
            i = 0;
        }
        p.writeInt(i);
        if (this.chargerUsbOnline) {
            i = 1;
        } else {
            i = 0;
        }
        p.writeInt(i);
        if (this.chargerWirelessOnline) {
            i = 1;
        } else {
            i = 0;
        }
        p.writeInt(i);
        p.writeInt(this.maxChargingCurrent);
        p.writeInt(this.maxChargingVoltage);
        p.writeInt(this.batteryStatus);
        p.writeInt(this.batteryHealth);
        if (!this.batteryPresent) {
            i2 = 0;
        }
        p.writeInt(i2);
        p.writeInt(this.batteryLevel);
        p.writeInt(this.batteryVoltage);
        p.writeInt(this.batteryTemperature);
        p.writeInt(this.batteryChargeCounter);
        p.writeString(this.batteryTechnology);
    }

    public int describeContents() {
        return 0;
    }
}
