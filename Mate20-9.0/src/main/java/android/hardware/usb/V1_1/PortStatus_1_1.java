package android.hardware.usb.V1_1;

import android.hardware.usb.V1_0.PortStatus;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class PortStatus_1_1 {
    public int currentMode;
    public final PortStatus status = new PortStatus();
    public int supportedModes;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != PortStatus_1_1.class) {
            return false;
        }
        PortStatus_1_1 other = (PortStatus_1_1) otherObject;
        if (HidlSupport.deepEquals(this.status, other.status) && HidlSupport.deepEquals(Integer.valueOf(this.supportedModes), Integer.valueOf(other.supportedModes)) && this.currentMode == other.currentMode) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.status)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.supportedModes))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.currentMode)))});
    }

    public final String toString() {
        return "{" + ".status = " + this.status + ", .supportedModes = " + PortMode_1_1.dumpBitfield(this.supportedModes) + ", .currentMode = " + PortMode_1_1.toString(this.currentMode) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<PortStatus_1_1> readVectorFromParcel(HwParcel parcel) {
        ArrayList<PortStatus_1_1> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            PortStatus_1_1 _hidl_vec_element = new PortStatus_1_1();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.status.readEmbeddedFromParcel(parcel, _hidl_blob, 0 + _hidl_offset);
        this.supportedModes = _hidl_blob.getInt32(40 + _hidl_offset);
        this.currentMode = _hidl_blob.getInt32(44 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<PortStatus_1_1> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.status.writeEmbeddedToBlob(_hidl_blob, 0 + _hidl_offset);
        _hidl_blob.putInt32(40 + _hidl_offset, this.supportedModes);
        _hidl_blob.putInt32(44 + _hidl_offset, this.currentMode);
    }
}
