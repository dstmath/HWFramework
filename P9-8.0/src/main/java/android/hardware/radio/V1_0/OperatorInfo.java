package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class OperatorInfo {
    public String alphaLong = new String();
    public String alphaShort = new String();
    public String operatorNumeric = new String();
    public int status;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != OperatorInfo.class) {
            return false;
        }
        OperatorInfo other = (OperatorInfo) otherObject;
        return HidlSupport.deepEquals(this.alphaLong, other.alphaLong) && HidlSupport.deepEquals(this.alphaShort, other.alphaShort) && HidlSupport.deepEquals(this.operatorNumeric, other.operatorNumeric) && this.status == other.status;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.alphaLong)), Integer.valueOf(HidlSupport.deepHashCode(this.alphaShort)), Integer.valueOf(HidlSupport.deepHashCode(this.operatorNumeric)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.status)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".alphaLong = ");
        builder.append(this.alphaLong);
        builder.append(", .alphaShort = ");
        builder.append(this.alphaShort);
        builder.append(", .operatorNumeric = ");
        builder.append(this.operatorNumeric);
        builder.append(", .status = ");
        builder.append(OperatorStatus.toString(this.status));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<OperatorInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<OperatorInfo> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            OperatorInfo _hidl_vec_element = new OperatorInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.alphaLong = _hidl_blob.getString(0 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.alphaLong.getBytes().length + 1), _hidl_blob.handle(), 0 + (0 + _hidl_offset), false);
        this.alphaShort = _hidl_blob.getString(16 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.alphaShort.getBytes().length + 1), _hidl_blob.handle(), 0 + (16 + _hidl_offset), false);
        this.operatorNumeric = _hidl_blob.getString(32 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.operatorNumeric.getBytes().length + 1), _hidl_blob.handle(), 0 + (32 + _hidl_offset), false);
        this.status = _hidl_blob.getInt32(48 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<OperatorInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((OperatorInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.alphaLong);
        _hidl_blob.putString(16 + _hidl_offset, this.alphaShort);
        _hidl_blob.putString(32 + _hidl_offset, this.operatorNumeric);
        _hidl_blob.putInt32(48 + _hidl_offset, this.status);
    }
}
