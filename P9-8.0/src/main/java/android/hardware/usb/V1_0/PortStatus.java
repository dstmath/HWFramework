package android.hardware.usb.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class PortStatus {
    public boolean canChangeDataRole;
    public boolean canChangeMode;
    public boolean canChangePowerRole;
    public int currentDataRole;
    public int currentMode;
    public int currentPowerRole;
    public String portName = new String();
    public int supportedModes;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != PortStatus.class) {
            return false;
        }
        PortStatus other = (PortStatus) otherObject;
        return HidlSupport.deepEquals(this.portName, other.portName) && this.currentDataRole == other.currentDataRole && this.currentPowerRole == other.currentPowerRole && this.currentMode == other.currentMode && this.canChangeMode == other.canChangeMode && this.canChangeDataRole == other.canChangeDataRole && this.canChangePowerRole == other.canChangePowerRole && this.supportedModes == other.supportedModes;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.portName)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.currentDataRole))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.currentPowerRole))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.currentMode))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.canChangeMode))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.canChangeDataRole))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.canChangePowerRole))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.supportedModes)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".portName = ");
        builder.append(this.portName);
        builder.append(", .currentDataRole = ");
        builder.append(PortDataRole.toString(this.currentDataRole));
        builder.append(", .currentPowerRole = ");
        builder.append(PortPowerRole.toString(this.currentPowerRole));
        builder.append(", .currentMode = ");
        builder.append(PortMode.toString(this.currentMode));
        builder.append(", .canChangeMode = ");
        builder.append(this.canChangeMode);
        builder.append(", .canChangeDataRole = ");
        builder.append(this.canChangeDataRole);
        builder.append(", .canChangePowerRole = ");
        builder.append(this.canChangePowerRole);
        builder.append(", .supportedModes = ");
        builder.append(PortMode.toString(this.supportedModes));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<PortStatus> readVectorFromParcel(HwParcel parcel) {
        ArrayList<PortStatus> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            PortStatus _hidl_vec_element = new PortStatus();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.portName = _hidl_blob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.portName.getBytes().length + 1), _hidl_blob.handle(), 0 + (_hidl_offset + 0), false);
        this.currentDataRole = _hidl_blob.getInt32(16 + _hidl_offset);
        this.currentPowerRole = _hidl_blob.getInt32(20 + _hidl_offset);
        this.currentMode = _hidl_blob.getInt32(24 + _hidl_offset);
        this.canChangeMode = _hidl_blob.getBool(28 + _hidl_offset);
        this.canChangeDataRole = _hidl_blob.getBool(29 + _hidl_offset);
        this.canChangePowerRole = _hidl_blob.getBool(30 + _hidl_offset);
        this.supportedModes = _hidl_blob.getInt32(32 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<PortStatus> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((PortStatus) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.portName);
        _hidl_blob.putInt32(16 + _hidl_offset, this.currentDataRole);
        _hidl_blob.putInt32(20 + _hidl_offset, this.currentPowerRole);
        _hidl_blob.putInt32(24 + _hidl_offset, this.currentMode);
        _hidl_blob.putBool(28 + _hidl_offset, this.canChangeMode);
        _hidl_blob.putBool(29 + _hidl_offset, this.canChangeDataRole);
        _hidl_blob.putBool(30 + _hidl_offset, this.canChangePowerRole);
        _hidl_blob.putInt32(32 + _hidl_offset, this.supportedModes);
    }
}
