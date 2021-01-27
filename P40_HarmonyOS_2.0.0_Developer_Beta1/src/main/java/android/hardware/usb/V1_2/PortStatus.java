package android.hardware.usb.V1_2;

import android.hardware.usb.V1_1.PortStatus_1_1;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class PortStatus {
    public int contaminantDetectionStatus;
    public int contaminantProtectionStatus;
    public PortStatus_1_1 status_1_1 = new PortStatus_1_1();
    public int supportedContaminantProtectionModes;
    public boolean supportsEnableContaminantPresenceDetection;
    public boolean supportsEnableContaminantPresenceProtection;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != PortStatus.class) {
            return false;
        }
        PortStatus other = (PortStatus) otherObject;
        if (HidlSupport.deepEquals(this.status_1_1, other.status_1_1) && HidlSupport.deepEquals(Integer.valueOf(this.supportedContaminantProtectionModes), Integer.valueOf(other.supportedContaminantProtectionModes)) && this.supportsEnableContaminantPresenceProtection == other.supportsEnableContaminantPresenceProtection && this.contaminantProtectionStatus == other.contaminantProtectionStatus && this.supportsEnableContaminantPresenceDetection == other.supportsEnableContaminantPresenceDetection && this.contaminantDetectionStatus == other.contaminantDetectionStatus) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.status_1_1)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.supportedContaminantProtectionModes))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.supportsEnableContaminantPresenceProtection))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.contaminantProtectionStatus))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.supportsEnableContaminantPresenceDetection))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.contaminantDetectionStatus))));
    }

    public final String toString() {
        return "{.status_1_1 = " + this.status_1_1 + ", .supportedContaminantProtectionModes = " + ContaminantProtectionMode.dumpBitfield(this.supportedContaminantProtectionModes) + ", .supportsEnableContaminantPresenceProtection = " + this.supportsEnableContaminantPresenceProtection + ", .contaminantProtectionStatus = " + ContaminantProtectionStatus.toString(this.contaminantProtectionStatus) + ", .supportsEnableContaminantPresenceDetection = " + this.supportsEnableContaminantPresenceDetection + ", .contaminantDetectionStatus = " + ContaminantDetectionStatus.toString(this.contaminantDetectionStatus) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(72), 0);
    }

    public static final ArrayList<PortStatus> readVectorFromParcel(HwParcel parcel) {
        ArrayList<PortStatus> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 72), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            PortStatus _hidl_vec_element = new PortStatus();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 72));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.status_1_1.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.supportedContaminantProtectionModes = _hidl_blob.getInt32(48 + _hidl_offset);
        this.supportsEnableContaminantPresenceProtection = _hidl_blob.getBool(52 + _hidl_offset);
        this.contaminantProtectionStatus = _hidl_blob.getInt32(56 + _hidl_offset);
        this.supportsEnableContaminantPresenceDetection = _hidl_blob.getBool(60 + _hidl_offset);
        this.contaminantDetectionStatus = _hidl_blob.getInt32(64 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(72);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<PortStatus> _hidl_vec) {
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
        this.status_1_1.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        _hidl_blob.putInt32(48 + _hidl_offset, this.supportedContaminantProtectionModes);
        _hidl_blob.putBool(52 + _hidl_offset, this.supportsEnableContaminantPresenceProtection);
        _hidl_blob.putInt32(56 + _hidl_offset, this.contaminantProtectionStatus);
        _hidl_blob.putBool(60 + _hidl_offset, this.supportsEnableContaminantPresenceDetection);
        _hidl_blob.putInt32(64 + _hidl_offset, this.contaminantDetectionStatus);
    }
}
