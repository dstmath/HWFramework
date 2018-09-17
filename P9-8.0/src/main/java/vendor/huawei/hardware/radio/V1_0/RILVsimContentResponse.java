package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILVsimContentResponse {
    public int cardCap;
    public int cardType;
    public String hplmn = new String();
    public String imsi = new String();
    public int index;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILVsimContentResponse.class) {
            return false;
        }
        RILVsimContentResponse other = (RILVsimContentResponse) otherObject;
        return this.index == other.index && this.cardCap == other.cardCap && this.cardType == other.cardType && HidlSupport.deepEquals(this.imsi, other.imsi) && HidlSupport.deepEquals(this.hplmn, other.hplmn);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.index))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cardCap))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cardType))), Integer.valueOf(HidlSupport.deepHashCode(this.imsi)), Integer.valueOf(HidlSupport.deepHashCode(this.hplmn))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".index = ");
        builder.append(this.index);
        builder.append(", .cardCap = ");
        builder.append(this.cardCap);
        builder.append(", .cardType = ");
        builder.append(this.cardType);
        builder.append(", .imsi = ");
        builder.append(this.imsi);
        builder.append(", .hplmn = ");
        builder.append(this.hplmn);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<RILVsimContentResponse> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILVsimContentResponse> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILVsimContentResponse _hidl_vec_element = new RILVsimContentResponse();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.index = _hidl_blob.getInt32(0 + _hidl_offset);
        this.cardCap = _hidl_blob.getInt32(4 + _hidl_offset);
        this.cardType = _hidl_blob.getInt32(8 + _hidl_offset);
        this.imsi = _hidl_blob.getString(16 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.imsi.getBytes().length + 1), _hidl_blob.handle(), 0 + (16 + _hidl_offset), false);
        this.hplmn = _hidl_blob.getString(32 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.hplmn.getBytes().length + 1), _hidl_blob.handle(), 0 + (32 + _hidl_offset), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILVsimContentResponse> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILVsimContentResponse) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.index);
        _hidl_blob.putInt32(4 + _hidl_offset, this.cardCap);
        _hidl_blob.putInt32(8 + _hidl_offset, this.cardType);
        _hidl_blob.putString(16 + _hidl_offset, this.imsi);
        _hidl_blob.putString(32 + _hidl_offset, this.hplmn);
    }
}
