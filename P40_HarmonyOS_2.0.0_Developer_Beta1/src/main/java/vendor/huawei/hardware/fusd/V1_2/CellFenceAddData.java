package vendor.huawei.hardware.fusd.V1_2;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CellFenceAddData {
    public int fid;
    public ArrayList<Integer> id = new ArrayList<>();
    public int notifRespMs;
    public int parendId;
    public int unknownTimerMs;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CellFenceAddData.class) {
            return false;
        }
        CellFenceAddData other = (CellFenceAddData) otherObject;
        if (this.fid == other.fid && this.parendId == other.parendId && this.notifRespMs == other.notifRespMs && this.unknownTimerMs == other.unknownTimerMs && HidlSupport.deepEquals(this.id, other.id)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.fid))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.parendId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.notifRespMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.unknownTimerMs))), Integer.valueOf(HidlSupport.deepHashCode(this.id)));
    }

    public final String toString() {
        return "{.fid = " + this.fid + ", .parendId = " + this.parendId + ", .notifRespMs = " + this.notifRespMs + ", .unknownTimerMs = " + this.unknownTimerMs + ", .id = " + this.id + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<CellFenceAddData> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CellFenceAddData> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CellFenceAddData _hidl_vec_element = new CellFenceAddData();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.fid = _hidl_blob.getInt32(_hidl_offset + 0);
        this.parendId = _hidl_blob.getInt32(_hidl_offset + 4);
        this.notifRespMs = _hidl_blob.getInt32(_hidl_offset + 8);
        this.unknownTimerMs = _hidl_blob.getInt32(_hidl_offset + 12);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.id.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.id.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CellFenceAddData> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.fid);
        _hidl_blob.putInt32(4 + _hidl_offset, this.parendId);
        _hidl_blob.putInt32(_hidl_offset + 8, this.notifRespMs);
        _hidl_blob.putInt32(_hidl_offset + 12, this.unknownTimerMs);
        int _hidl_vec_size = this.id.size();
        _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt32((long) (_hidl_index_0 * 4), this.id.get(_hidl_index_0).intValue());
        }
        _hidl_blob.putBlob(16 + _hidl_offset + 0, childBlob);
    }
}
