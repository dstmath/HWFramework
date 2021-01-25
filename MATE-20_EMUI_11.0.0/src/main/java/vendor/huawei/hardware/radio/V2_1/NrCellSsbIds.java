package vendor.huawei.hardware.radio.V2_1;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NrCellSsbIds {
    public int arfcn;
    public long cid;
    public int nbCellCount;
    public ArrayList<NeighboringCellSsbInfo> nbCellSsbList = new ArrayList<>();
    public int pci;
    public int rsrp;
    public ArrayList<SsbIdInfo> sCellSsbList = new ArrayList<>();
    public int sinr;
    public int timingAdvance;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NrCellSsbIds.class) {
            return false;
        }
        NrCellSsbIds other = (NrCellSsbIds) otherObject;
        if (this.arfcn == other.arfcn && this.cid == other.cid && this.pci == other.pci && this.rsrp == other.rsrp && this.sinr == other.sinr && this.timingAdvance == other.timingAdvance && HidlSupport.deepEquals(this.sCellSsbList, other.sCellSsbList) && this.nbCellCount == other.nbCellCount && HidlSupport.deepEquals(this.nbCellSsbList, other.nbCellSsbList)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.arfcn))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.cid))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.pci))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rsrp))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sinr))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.timingAdvance))), Integer.valueOf(HidlSupport.deepHashCode(this.sCellSsbList)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.nbCellCount))), Integer.valueOf(HidlSupport.deepHashCode(this.nbCellSsbList)));
    }

    public final String toString() {
        return "{.arfcn = " + this.arfcn + ", .cid = " + this.cid + ", .pci = " + this.pci + ", .rsrp = " + this.rsrp + ", .sinr = " + this.sinr + ", .timingAdvance = " + this.timingAdvance + ", .sCellSsbList = " + this.sCellSsbList + ", .nbCellCount = " + this.nbCellCount + ", .nbCellSsbList = " + this.nbCellSsbList + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(72), 0);
    }

    public static final ArrayList<NrCellSsbIds> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NrCellSsbIds> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 72), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NrCellSsbIds _hidl_vec_element = new NrCellSsbIds();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 72));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.arfcn = _hidl_blob.getInt32(_hidl_offset + 0);
        this.cid = _hidl_blob.getInt64(_hidl_offset + 8);
        this.pci = _hidl_blob.getInt32(_hidl_offset + 16);
        this.rsrp = _hidl_blob.getInt32(_hidl_offset + 20);
        this.sinr = _hidl_blob.getInt32(_hidl_offset + 24);
        this.timingAdvance = _hidl_blob.getInt32(_hidl_offset + 28);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 32 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 8), _hidl_blob.handle(), _hidl_offset + 32 + 0, true);
        this.sCellSsbList.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            SsbIdInfo _hidl_vec_element = new SsbIdInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 8));
            this.sCellSsbList.add(_hidl_vec_element);
        }
        this.nbCellCount = _hidl_blob.getInt32(_hidl_offset + 48);
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 56 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 32), _hidl_blob.handle(), _hidl_offset + 56 + 0, true);
        this.nbCellSsbList.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            NeighboringCellSsbInfo _hidl_vec_element2 = new NeighboringCellSsbInfo();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_02 * 32));
            this.nbCellSsbList.add(_hidl_vec_element2);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(72);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NrCellSsbIds> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 72);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 72));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.arfcn);
        _hidl_blob.putInt64(_hidl_offset + 8, this.cid);
        _hidl_blob.putInt32(_hidl_offset + 16, this.pci);
        _hidl_blob.putInt32(_hidl_offset + 20, this.rsrp);
        _hidl_blob.putInt32(_hidl_offset + 24, this.sinr);
        _hidl_blob.putInt32(_hidl_offset + 28, this.timingAdvance);
        int _hidl_vec_size = this.sCellSsbList.size();
        _hidl_blob.putInt32(_hidl_offset + 32 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 32 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 8);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.sCellSsbList.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 8));
        }
        _hidl_blob.putBlob(_hidl_offset + 32 + 0, childBlob);
        _hidl_blob.putInt32(_hidl_offset + 48, this.nbCellCount);
        int _hidl_vec_size2 = this.nbCellSsbList.size();
        _hidl_blob.putInt32(_hidl_offset + 56 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 56 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 32);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.nbCellSsbList.get(_hidl_index_02).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_02 * 32));
        }
        _hidl_blob.putBlob(_hidl_offset + 56 + 0, childBlob2);
    }
}
