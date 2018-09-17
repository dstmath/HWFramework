package android.os;

import android.os.Parcelable.Creator;

public class BatteryProperties implements Parcelable {
    public static final Creator<BatteryProperties> CREATOR = new Creator<BatteryProperties>() {
        public BatteryProperties createFromParcel(Parcel p) {
            return new BatteryProperties(p, null);
        }

        public BatteryProperties[] newArray(int size) {
            return new BatteryProperties[size];
        }
    };
    public int batteryChargeCounter;
    public int batteryFullCharge;
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

    /* synthetic */ BatteryProperties(Parcel p, BatteryProperties -this1) {
        this(p);
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
        this.batteryFullCharge = other.batteryFullCharge;
        this.batteryChargeCounter = other.batteryChargeCounter;
        this.batteryTechnology = other.batteryTechnology;
    }

    private BatteryProperties(Parcel p) {
        boolean z;
        boolean z2 = true;
        this.chargerAcOnline = p.readInt() == 1;
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
        this.batteryFullCharge = p.readInt();
        this.batteryChargeCounter = p.readInt();
        this.batteryTechnology = p.readString();
    }

    public void writeToParcel(Parcel p, int flags) {
        int i;
        int i2 = 1;
        p.writeInt(this.chargerAcOnline ? 1 : 0);
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
        p.writeInt(this.batteryFullCharge);
        p.writeInt(this.batteryChargeCounter);
        p.writeString(this.batteryTechnology);
    }

    public int describeContents() {
        return 0;
    }
}
