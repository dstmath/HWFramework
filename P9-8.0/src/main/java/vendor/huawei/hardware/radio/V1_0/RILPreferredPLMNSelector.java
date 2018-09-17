package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILPreferredPLMNSelector {
    public int fmt;
    public int gsmAcT;
    public int gsmCompactAcT;
    public int idx;
    public int lteACT;
    public int plmn;
    public int utranAcT;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILPreferredPLMNSelector.class) {
            return false;
        }
        RILPreferredPLMNSelector other = (RILPreferredPLMNSelector) otherObject;
        return this.idx == other.idx && this.fmt == other.fmt && this.plmn == other.plmn && this.gsmAcT == other.gsmAcT && this.gsmCompactAcT == other.gsmCompactAcT && this.utranAcT == other.utranAcT && this.lteACT == other.lteACT;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.idx))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.fmt))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.plmn))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.gsmAcT))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.gsmCompactAcT))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.utranAcT))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.lteACT)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".idx = ");
        builder.append(this.idx);
        builder.append(", .fmt = ");
        builder.append(this.fmt);
        builder.append(", .plmn = ");
        builder.append(this.plmn);
        builder.append(", .gsmAcT = ");
        builder.append(this.gsmAcT);
        builder.append(", .gsmCompactAcT = ");
        builder.append(this.gsmCompactAcT);
        builder.append(", .utranAcT = ");
        builder.append(this.utranAcT);
        builder.append(", .lteACT = ");
        builder.append(this.lteACT);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(28), 0);
    }

    public static final ArrayList<RILPreferredPLMNSelector> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILPreferredPLMNSelector> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 28), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILPreferredPLMNSelector _hidl_vec_element = new RILPreferredPLMNSelector();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 28));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.idx = _hidl_blob.getInt32(0 + _hidl_offset);
        this.fmt = _hidl_blob.getInt32(4 + _hidl_offset);
        this.plmn = _hidl_blob.getInt32(8 + _hidl_offset);
        this.gsmAcT = _hidl_blob.getInt32(12 + _hidl_offset);
        this.gsmCompactAcT = _hidl_blob.getInt32(16 + _hidl_offset);
        this.utranAcT = _hidl_blob.getInt32(20 + _hidl_offset);
        this.lteACT = _hidl_blob.getInt32(24 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(28);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILPreferredPLMNSelector> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 28);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILPreferredPLMNSelector) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 28));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.idx);
        _hidl_blob.putInt32(4 + _hidl_offset, this.fmt);
        _hidl_blob.putInt32(8 + _hidl_offset, this.plmn);
        _hidl_blob.putInt32(12 + _hidl_offset, this.gsmAcT);
        _hidl_blob.putInt32(16 + _hidl_offset, this.gsmCompactAcT);
        _hidl_blob.putInt32(20 + _hidl_offset, this.utranAcT);
        _hidl_blob.putInt32(24 + _hidl_offset, this.lteACT);
    }
}
