package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILDeviceVersionResponse {
    public String buildTime = new String();
    public String configurateVersion = new String();
    public String externalDbVersion = new String();
    public String externalDutName = new String();
    public String externalHwVersion = new String();
    public String externalSwVersion = new String();
    public String internalDbVersion = new String();
    public String internalDutName = new String();
    public String internalHwVersion = new String();
    public String internalSwVersion = new String();
    public String prlVersion = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILDeviceVersionResponse.class) {
            return false;
        }
        RILDeviceVersionResponse other = (RILDeviceVersionResponse) otherObject;
        return HidlSupport.deepEquals(this.buildTime, other.buildTime) && HidlSupport.deepEquals(this.externalSwVersion, other.externalSwVersion) && HidlSupport.deepEquals(this.internalSwVersion, other.internalSwVersion) && HidlSupport.deepEquals(this.externalDbVersion, other.externalDbVersion) && HidlSupport.deepEquals(this.internalDbVersion, other.internalDbVersion) && HidlSupport.deepEquals(this.externalHwVersion, other.externalHwVersion) && HidlSupport.deepEquals(this.internalHwVersion, other.internalHwVersion) && HidlSupport.deepEquals(this.externalDutName, other.externalDutName) && HidlSupport.deepEquals(this.internalDutName, other.internalDutName) && HidlSupport.deepEquals(this.configurateVersion, other.configurateVersion) && HidlSupport.deepEquals(this.prlVersion, other.prlVersion);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.buildTime)), Integer.valueOf(HidlSupport.deepHashCode(this.externalSwVersion)), Integer.valueOf(HidlSupport.deepHashCode(this.internalSwVersion)), Integer.valueOf(HidlSupport.deepHashCode(this.externalDbVersion)), Integer.valueOf(HidlSupport.deepHashCode(this.internalDbVersion)), Integer.valueOf(HidlSupport.deepHashCode(this.externalHwVersion)), Integer.valueOf(HidlSupport.deepHashCode(this.internalHwVersion)), Integer.valueOf(HidlSupport.deepHashCode(this.externalDutName)), Integer.valueOf(HidlSupport.deepHashCode(this.internalDutName)), Integer.valueOf(HidlSupport.deepHashCode(this.configurateVersion)), Integer.valueOf(HidlSupport.deepHashCode(this.prlVersion))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".buildTime = ");
        builder.append(this.buildTime);
        builder.append(", .externalSwVersion = ");
        builder.append(this.externalSwVersion);
        builder.append(", .internalSwVersion = ");
        builder.append(this.internalSwVersion);
        builder.append(", .externalDbVersion = ");
        builder.append(this.externalDbVersion);
        builder.append(", .internalDbVersion = ");
        builder.append(this.internalDbVersion);
        builder.append(", .externalHwVersion = ");
        builder.append(this.externalHwVersion);
        builder.append(", .internalHwVersion = ");
        builder.append(this.internalHwVersion);
        builder.append(", .externalDutName = ");
        builder.append(this.externalDutName);
        builder.append(", .internalDutName = ");
        builder.append(this.internalDutName);
        builder.append(", .configurateVersion = ");
        builder.append(this.configurateVersion);
        builder.append(", .prlVersion = ");
        builder.append(this.prlVersion);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(176), 0);
    }

    public static final ArrayList<RILDeviceVersionResponse> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILDeviceVersionResponse> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 176), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILDeviceVersionResponse _hidl_vec_element = new RILDeviceVersionResponse();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 176));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.buildTime = _hidl_blob.getString(0 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.buildTime.getBytes().length + 1), _hidl_blob.handle(), 0 + (0 + _hidl_offset), false);
        this.externalSwVersion = _hidl_blob.getString(16 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.externalSwVersion.getBytes().length + 1), _hidl_blob.handle(), 0 + (16 + _hidl_offset), false);
        this.internalSwVersion = _hidl_blob.getString(32 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.internalSwVersion.getBytes().length + 1), _hidl_blob.handle(), 0 + (32 + _hidl_offset), false);
        this.externalDbVersion = _hidl_blob.getString(48 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.externalDbVersion.getBytes().length + 1), _hidl_blob.handle(), 0 + (48 + _hidl_offset), false);
        this.internalDbVersion = _hidl_blob.getString(64 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.internalDbVersion.getBytes().length + 1), _hidl_blob.handle(), 0 + (64 + _hidl_offset), false);
        this.externalHwVersion = _hidl_blob.getString(80 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.externalHwVersion.getBytes().length + 1), _hidl_blob.handle(), 0 + (80 + _hidl_offset), false);
        this.internalHwVersion = _hidl_blob.getString(96 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.internalHwVersion.getBytes().length + 1), _hidl_blob.handle(), 0 + (96 + _hidl_offset), false);
        this.externalDutName = _hidl_blob.getString(112 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.externalDutName.getBytes().length + 1), _hidl_blob.handle(), 0 + (112 + _hidl_offset), false);
        this.internalDutName = _hidl_blob.getString(128 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.internalDutName.getBytes().length + 1), _hidl_blob.handle(), 0 + (128 + _hidl_offset), false);
        this.configurateVersion = _hidl_blob.getString(144 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.configurateVersion.getBytes().length + 1), _hidl_blob.handle(), 0 + (144 + _hidl_offset), false);
        this.prlVersion = _hidl_blob.getString(160 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.prlVersion.getBytes().length + 1), _hidl_blob.handle(), 0 + (160 + _hidl_offset), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(176);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILDeviceVersionResponse> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 176);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILDeviceVersionResponse) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 176));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.buildTime);
        _hidl_blob.putString(16 + _hidl_offset, this.externalSwVersion);
        _hidl_blob.putString(32 + _hidl_offset, this.internalSwVersion);
        _hidl_blob.putString(48 + _hidl_offset, this.externalDbVersion);
        _hidl_blob.putString(64 + _hidl_offset, this.internalDbVersion);
        _hidl_blob.putString(80 + _hidl_offset, this.externalHwVersion);
        _hidl_blob.putString(96 + _hidl_offset, this.internalHwVersion);
        _hidl_blob.putString(112 + _hidl_offset, this.externalDutName);
        _hidl_blob.putString(128 + _hidl_offset, this.internalDutName);
        _hidl_blob.putString(144 + _hidl_offset, this.configurateVersion);
        _hidl_blob.putString(160 + _hidl_offset, this.prlVersion);
    }
}
