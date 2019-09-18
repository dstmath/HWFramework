package android.os;

import android.os.Parcelable;

public class BatteryProperties implements Parcelable {
    public static final Parcelable.Creator<BatteryProperties> CREATOR = new Parcelable.Creator<BatteryProperties>() {
        public BatteryProperties createFromParcel(Parcel p) {
            return new BatteryProperties(p);
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

    public BatteryProperties() {
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
        boolean z = false;
        this.chargerAcOnline = p.readInt() == 1;
        this.chargerUsbOnline = p.readInt() == 1;
        this.chargerWirelessOnline = p.readInt() == 1;
        this.maxChargingCurrent = p.readInt();
        this.maxChargingVoltage = p.readInt();
        this.batteryStatus = p.readInt();
        this.batteryHealth = p.readInt();
        this.batteryPresent = p.readInt() == 1 ? true : z;
        this.batteryLevel = p.readInt();
        this.batteryVoltage = p.readInt();
        this.batteryTemperature = p.readInt();
        this.batteryFullCharge = p.readInt();
        this.batteryChargeCounter = p.readInt();
        this.batteryTechnology = p.readString();
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(this.chargerAcOnline ? 1 : 0);
        p.writeInt(this.chargerUsbOnline ? 1 : 0);
        p.writeInt(this.chargerWirelessOnline ? 1 : 0);
        p.writeInt(this.maxChargingCurrent);
        p.writeInt(this.maxChargingVoltage);
        p.writeInt(this.batteryStatus);
        p.writeInt(this.batteryHealth);
        p.writeInt(this.batteryPresent ? 1 : 0);
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
