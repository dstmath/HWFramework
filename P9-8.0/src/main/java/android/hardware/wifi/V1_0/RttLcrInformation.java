package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class RttLcrInformation {
    public String civicInfo = new String();
    public final byte[] countryCode = new byte[2];

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RttLcrInformation.class) {
            return false;
        }
        RttLcrInformation other = (RttLcrInformation) otherObject;
        return HidlSupport.deepEquals(this.countryCode, other.countryCode) && HidlSupport.deepEquals(this.civicInfo, other.civicInfo);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.countryCode)), Integer.valueOf(HidlSupport.deepHashCode(this.civicInfo))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".countryCode = ");
        builder.append(Arrays.toString(this.countryCode));
        builder.append(", .civicInfo = ");
        builder.append(this.civicInfo);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<RttLcrInformation> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RttLcrInformation> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RttLcrInformation _hidl_vec_element = new RttLcrInformation();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        long _hidl_array_offset_0 = _hidl_offset + 0;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            this.countryCode[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        this.civicInfo = _hidl_blob.getString(8 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.civicInfo.getBytes().length + 1), _hidl_blob.handle(), (8 + _hidl_offset) + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RttLcrInformation> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RttLcrInformation) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        long _hidl_array_offset_0 = _hidl_offset + 0;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.countryCode[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_blob.putString(8 + _hidl_offset, this.civicInfo);
    }
}
