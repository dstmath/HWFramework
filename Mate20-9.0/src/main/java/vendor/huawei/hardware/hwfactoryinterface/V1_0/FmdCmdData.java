package vendor.huawei.hardware.hwfactoryinterface.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class FmdCmdData {
    public String bsn = new String();
    public int commandId;
    public int cycle;
    public int operation;
    public String station = new String();
    public int version;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != FmdCmdData.class) {
            return false;
        }
        FmdCmdData other = (FmdCmdData) otherObject;
        if (this.version == other.version && this.commandId == other.commandId && this.operation == other.operation && this.cycle == other.cycle && HidlSupport.deepEquals(this.station, other.station) && HidlSupport.deepEquals(this.bsn, other.bsn)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.version))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.commandId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.operation))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cycle))), Integer.valueOf(HidlSupport.deepHashCode(this.station)), Integer.valueOf(HidlSupport.deepHashCode(this.bsn))});
    }

    public final String toString() {
        return "{" + ".version = " + this.version + ", .commandId = " + this.commandId + ", .operation = " + this.operation + ", .cycle = " + this.cycle + ", .station = " + this.station + ", .bsn = " + this.bsn + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<FmdCmdData> readVectorFromParcel(HwParcel parcel) {
        ArrayList<FmdCmdData> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            FmdCmdData _hidl_vec_element = new FmdCmdData();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.version = hwBlob.getInt32(_hidl_offset + 0);
        this.commandId = hwBlob.getInt32(_hidl_offset + 4);
        this.operation = hwBlob.getInt32(_hidl_offset + 8);
        this.cycle = hwBlob.getInt32(_hidl_offset + 12);
        this.station = hwBlob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.station.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.bsn = hwBlob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.bsn.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<FmdCmdData> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.version);
        _hidl_blob.putInt32(4 + _hidl_offset, this.commandId);
        _hidl_blob.putInt32(8 + _hidl_offset, this.operation);
        _hidl_blob.putInt32(12 + _hidl_offset, this.cycle);
        _hidl_blob.putString(16 + _hidl_offset, this.station);
        _hidl_blob.putString(32 + _hidl_offset, this.bsn);
    }
}
