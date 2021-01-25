package vendor.huawei.hardware.hisiradio.V1_3;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NeighboringCellSsbInfo {
    public int arfcn;
    public int pci;
    public int rsrp;
    public int sinr;
    public ArrayList<SsbIdInfo> ssbIdList = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NeighboringCellSsbInfo.class) {
            return false;
        }
        NeighboringCellSsbInfo other = (NeighboringCellSsbInfo) otherObject;
        if (this.pci == other.pci && this.arfcn == other.arfcn && this.rsrp == other.rsrp && this.sinr == other.sinr && HidlSupport.deepEquals(this.ssbIdList, other.ssbIdList)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.pci))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.arfcn))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rsrp))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sinr))), Integer.valueOf(HidlSupport.deepHashCode(this.ssbIdList)));
    }

    public final String toString() {
        return "{.pci = " + this.pci + ", .arfcn = " + this.arfcn + ", .rsrp = " + this.rsrp + ", .sinr = " + this.sinr + ", .ssbIdList = " + this.ssbIdList + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<NeighboringCellSsbInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NeighboringCellSsbInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NeighboringCellSsbInfo _hidl_vec_element = new NeighboringCellSsbInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.pci = _hidl_blob.getInt32(_hidl_offset + 0);
        this.arfcn = _hidl_blob.getInt32(_hidl_offset + 4);
        this.rsrp = _hidl_blob.getInt32(_hidl_offset + 8);
        this.sinr = _hidl_blob.getInt32(_hidl_offset + 12);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 8), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.ssbIdList.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SsbIdInfo _hidl_vec_element = new SsbIdInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 8));
            this.ssbIdList.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NeighboringCellSsbInfo> _hidl_vec) {
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
        _hidl_blob.putInt32(_hidl_offset + 0, this.pci);
        _hidl_blob.putInt32(4 + _hidl_offset, this.arfcn);
        _hidl_blob.putInt32(_hidl_offset + 8, this.rsrp);
        _hidl_blob.putInt32(_hidl_offset + 12, this.sinr);
        int _hidl_vec_size = this.ssbIdList.size();
        _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 8);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.ssbIdList.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 8));
        }
        _hidl_blob.putBlob(16 + _hidl_offset + 0, childBlob);
    }
}
