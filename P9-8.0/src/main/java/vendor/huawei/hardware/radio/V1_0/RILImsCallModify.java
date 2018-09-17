package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILImsCallModify {
    public int callIndex;
    public final RILImsCallDetails currCallDetails = new RILImsCallDetails();
    public final RILImsCallDetails destCallDetails = new RILImsCallDetails();
    public int modifyReason;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILImsCallModify.class) {
            return false;
        }
        RILImsCallModify other = (RILImsCallModify) otherObject;
        return this.callIndex == other.callIndex && HidlSupport.deepEquals(this.currCallDetails, other.currCallDetails) && HidlSupport.deepEquals(this.destCallDetails, other.destCallDetails) && this.modifyReason == other.modifyReason;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.callIndex))), Integer.valueOf(HidlSupport.deepHashCode(this.currCallDetails)), Integer.valueOf(HidlSupport.deepHashCode(this.destCallDetails)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.modifyReason)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".callIndex = ");
        builder.append(this.callIndex);
        builder.append(", .currCallDetails = ");
        builder.append(this.currCallDetails);
        builder.append(", .destCallDetails = ");
        builder.append(this.destCallDetails);
        builder.append(", .modifyReason = ");
        builder.append(this.modifyReason);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<RILImsCallModify> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILImsCallModify> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILImsCallModify _hidl_vec_element = new RILImsCallModify();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.callIndex = _hidl_blob.getInt32(0 + _hidl_offset);
        this.currCallDetails.readEmbeddedFromParcel(parcel, _hidl_blob, 4 + _hidl_offset);
        this.destCallDetails.readEmbeddedFromParcel(parcel, _hidl_blob, 12 + _hidl_offset);
        this.modifyReason = _hidl_blob.getInt32(20 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILImsCallModify> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILImsCallModify) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.callIndex);
        this.currCallDetails.writeEmbeddedToBlob(_hidl_blob, 4 + _hidl_offset);
        this.destCallDetails.writeEmbeddedToBlob(_hidl_blob, 12 + _hidl_offset);
        _hidl_blob.putInt32(20 + _hidl_offset, this.modifyReason);
    }
}
