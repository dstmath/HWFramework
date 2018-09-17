package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILImsHandover {
    public int dstTech;
    public int errorCode;
    public final RILImsExtra hoExtra = new RILImsExtra();
    public int hoType;
    public int srcTech;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILImsHandover.class) {
            return false;
        }
        RILImsHandover other = (RILImsHandover) otherObject;
        return this.hoType == other.hoType && this.srcTech == other.srcTech && this.dstTech == other.dstTech && HidlSupport.deepEquals(this.hoExtra, other.hoExtra) && this.errorCode == other.errorCode;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.hoType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.srcTech))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.dstTech))), Integer.valueOf(HidlSupport.deepHashCode(this.hoExtra)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.errorCode)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".hoType = ");
        builder.append(RILImsHandoverState.toString(this.hoType));
        builder.append(", .srcTech = ");
        builder.append(RILImsRadioTechType.toString(this.srcTech));
        builder.append(", .dstTech = ");
        builder.append(RILImsRadioTechType.toString(this.dstTech));
        builder.append(", .hoExtra = ");
        builder.append(this.hoExtra);
        builder.append(", .errorCode = ");
        builder.append(this.errorCode);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<RILImsHandover> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILImsHandover> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILImsHandover _hidl_vec_element = new RILImsHandover();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.hoType = _hidl_blob.getInt32(0 + _hidl_offset);
        this.srcTech = _hidl_blob.getInt32(4 + _hidl_offset);
        this.dstTech = _hidl_blob.getInt32(8 + _hidl_offset);
        this.hoExtra.readEmbeddedFromParcel(parcel, _hidl_blob, 16 + _hidl_offset);
        this.errorCode = _hidl_blob.getInt32(48 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILImsHandover> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILImsHandover) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.hoType);
        _hidl_blob.putInt32(4 + _hidl_offset, this.srcTech);
        _hidl_blob.putInt32(8 + _hidl_offset, this.dstTech);
        this.hoExtra.writeEmbeddedToBlob(_hidl_blob, 16 + _hidl_offset);
        _hidl_blob.putInt32(48 + _hidl_offset, this.errorCode);
    }
}
