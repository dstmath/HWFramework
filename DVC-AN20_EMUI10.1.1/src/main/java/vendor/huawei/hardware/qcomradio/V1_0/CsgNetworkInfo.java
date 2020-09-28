package vendor.huawei.hardware.qcomradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CsgNetworkInfo {
    public String csgId = new String();
    public int networkRat;
    public String plmn = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CsgNetworkInfo.class) {
            return false;
        }
        CsgNetworkInfo other = (CsgNetworkInfo) otherObject;
        if (HidlSupport.deepEquals(this.csgId, other.csgId) && this.networkRat == other.networkRat && HidlSupport.deepEquals(this.plmn, other.plmn)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.csgId)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.networkRat))), Integer.valueOf(HidlSupport.deepHashCode(this.plmn)));
    }

    public final String toString() {
        return "{" + ".csgId = " + this.csgId + ", .networkRat = " + this.networkRat + ", .plmn = " + this.plmn + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<CsgNetworkInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CsgNetworkInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CsgNetworkInfo _hidl_vec_element = new CsgNetworkInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.csgId = _hidl_blob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.csgId.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.networkRat = _hidl_blob.getInt32(_hidl_offset + 16);
        this.plmn = _hidl_blob.getString(_hidl_offset + 24);
        parcel.readEmbeddedBuffer((long) (this.plmn.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 24 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CsgNetworkInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(0 + _hidl_offset, this.csgId);
        _hidl_blob.putInt32(16 + _hidl_offset, this.networkRat);
        _hidl_blob.putString(24 + _hidl_offset, this.plmn);
    }
}
