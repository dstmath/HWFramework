package vendor.huawei.hardware.qcomradio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RilSysInfor {
    public int infoType;
    public final ArrayList<Integer> nDatas = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RilSysInfor.class) {
            return false;
        }
        RilSysInfor other = (RilSysInfor) otherObject;
        if (this.infoType == other.infoType && HidlSupport.deepEquals(this.nDatas, other.nDatas)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.infoType))), Integer.valueOf(HidlSupport.deepHashCode(this.nDatas))});
    }

    public final String toString() {
        return "{" + ".infoType = " + RilSysInforType.toString(this.infoType) + ", .nDatas = " + this.nDatas + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(24), 0);
    }

    public static final ArrayList<RilSysInfor> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RilSysInfor> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RilSysInfor _hidl_vec_element = new RilSysInfor();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.infoType = hwBlob.getInt32(_hidl_offset + 0);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.nDatas.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.nDatas.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(24);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RilSysInfor> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.infoType);
        int _hidl_vec_size = this.nDatas.size();
        _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        while (true) {
            int _hidl_index_02 = _hidl_index_0;
            if (_hidl_index_02 < _hidl_vec_size) {
                childBlob.putInt32((long) (_hidl_index_02 * 4), this.nDatas.get(_hidl_index_02).intValue());
                _hidl_index_0 = _hidl_index_02 + 1;
            } else {
                _hidl_blob.putBlob(8 + _hidl_offset + 0, childBlob);
                return;
            }
        }
    }
}
