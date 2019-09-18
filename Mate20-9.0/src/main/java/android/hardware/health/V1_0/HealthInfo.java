package android.hardware.health.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class HealthInfo {
    public int batteryChargeCounter;
    public int batteryCurrent;
    public int batteryCycleCount;
    public int batteryFullCharge;
    public int batteryHealth;
    public int batteryLevel;
    public boolean batteryPresent;
    public int batteryStatus;
    public String batteryTechnology = new String();
    public int batteryTemperature;
    public int batteryVoltage;
    public boolean chargerAcOnline;
    public boolean chargerUsbOnline;
    public boolean chargerWirelessOnline;
    public int maxChargingCurrent;
    public int maxChargingVoltage;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != HealthInfo.class) {
            return false;
        }
        HealthInfo other = (HealthInfo) otherObject;
        if (this.chargerAcOnline == other.chargerAcOnline && this.chargerUsbOnline == other.chargerUsbOnline && this.chargerWirelessOnline == other.chargerWirelessOnline && this.maxChargingCurrent == other.maxChargingCurrent && this.maxChargingVoltage == other.maxChargingVoltage && this.batteryStatus == other.batteryStatus && this.batteryHealth == other.batteryHealth && this.batteryPresent == other.batteryPresent && this.batteryLevel == other.batteryLevel && this.batteryVoltage == other.batteryVoltage && this.batteryTemperature == other.batteryTemperature && this.batteryCurrent == other.batteryCurrent && this.batteryCycleCount == other.batteryCycleCount && this.batteryFullCharge == other.batteryFullCharge && this.batteryChargeCounter == other.batteryChargeCounter && HidlSupport.deepEquals(this.batteryTechnology, other.batteryTechnology)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.chargerAcOnline))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.chargerUsbOnline))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.chargerWirelessOnline))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxChargingCurrent))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxChargingVoltage))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.batteryStatus))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.batteryHealth))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.batteryPresent))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.batteryLevel))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.batteryVoltage))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.batteryTemperature))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.batteryCurrent))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.batteryCycleCount))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.batteryFullCharge))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.batteryChargeCounter))), Integer.valueOf(HidlSupport.deepHashCode(this.batteryTechnology))});
    }

    public final String toString() {
        return "{" + ".chargerAcOnline = " + this.chargerAcOnline + ", .chargerUsbOnline = " + this.chargerUsbOnline + ", .chargerWirelessOnline = " + this.chargerWirelessOnline + ", .maxChargingCurrent = " + this.maxChargingCurrent + ", .maxChargingVoltage = " + this.maxChargingVoltage + ", .batteryStatus = " + BatteryStatus.toString(this.batteryStatus) + ", .batteryHealth = " + BatteryHealth.toString(this.batteryHealth) + ", .batteryPresent = " + this.batteryPresent + ", .batteryLevel = " + this.batteryLevel + ", .batteryVoltage = " + this.batteryVoltage + ", .batteryTemperature = " + this.batteryTemperature + ", .batteryCurrent = " + this.batteryCurrent + ", .batteryCycleCount = " + this.batteryCycleCount + ", .batteryFullCharge = " + this.batteryFullCharge + ", .batteryChargeCounter = " + this.batteryChargeCounter + ", .batteryTechnology = " + this.batteryTechnology + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(72), 0);
    }

    public static final ArrayList<HealthInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<HealthInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 72), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            HealthInfo _hidl_vec_element = new HealthInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 72));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.chargerAcOnline = hwBlob.getBool(_hidl_offset + 0);
        this.chargerUsbOnline = hwBlob.getBool(_hidl_offset + 1);
        this.chargerWirelessOnline = hwBlob.getBool(_hidl_offset + 2);
        this.maxChargingCurrent = hwBlob.getInt32(_hidl_offset + 4);
        this.maxChargingVoltage = hwBlob.getInt32(_hidl_offset + 8);
        this.batteryStatus = hwBlob.getInt32(_hidl_offset + 12);
        this.batteryHealth = hwBlob.getInt32(_hidl_offset + 16);
        this.batteryPresent = hwBlob.getBool(_hidl_offset + 20);
        this.batteryLevel = hwBlob.getInt32(_hidl_offset + 24);
        this.batteryVoltage = hwBlob.getInt32(_hidl_offset + 28);
        this.batteryTemperature = hwBlob.getInt32(_hidl_offset + 32);
        this.batteryCurrent = hwBlob.getInt32(_hidl_offset + 36);
        this.batteryCycleCount = hwBlob.getInt32(_hidl_offset + 40);
        this.batteryFullCharge = hwBlob.getInt32(_hidl_offset + 44);
        this.batteryChargeCounter = hwBlob.getInt32(_hidl_offset + 48);
        this.batteryTechnology = hwBlob.getString(_hidl_offset + 56);
        parcel.readEmbeddedBuffer((long) (this.batteryTechnology.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 56 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(72);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<HealthInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 72);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 72));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putBool(0 + _hidl_offset, this.chargerAcOnline);
        _hidl_blob.putBool(1 + _hidl_offset, this.chargerUsbOnline);
        _hidl_blob.putBool(2 + _hidl_offset, this.chargerWirelessOnline);
        _hidl_blob.putInt32(4 + _hidl_offset, this.maxChargingCurrent);
        _hidl_blob.putInt32(8 + _hidl_offset, this.maxChargingVoltage);
        _hidl_blob.putInt32(12 + _hidl_offset, this.batteryStatus);
        _hidl_blob.putInt32(16 + _hidl_offset, this.batteryHealth);
        _hidl_blob.putBool(20 + _hidl_offset, this.batteryPresent);
        _hidl_blob.putInt32(24 + _hidl_offset, this.batteryLevel);
        _hidl_blob.putInt32(28 + _hidl_offset, this.batteryVoltage);
        _hidl_blob.putInt32(32 + _hidl_offset, this.batteryTemperature);
        _hidl_blob.putInt32(36 + _hidl_offset, this.batteryCurrent);
        _hidl_blob.putInt32(40 + _hidl_offset, this.batteryCycleCount);
        _hidl_blob.putInt32(44 + _hidl_offset, this.batteryFullCharge);
        _hidl_blob.putInt32(48 + _hidl_offset, this.batteryChargeCounter);
        _hidl_blob.putString(56 + _hidl_offset, this.batteryTechnology);
    }
}
