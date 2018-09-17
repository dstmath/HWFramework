package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class PcoDataInfo {
    public String bearerProto = new String();
    public int cid;
    public final ArrayList<Byte> contents = new ArrayList();
    public int pcoId;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != PcoDataInfo.class) {
            return false;
        }
        PcoDataInfo other = (PcoDataInfo) otherObject;
        return this.cid == other.cid && HidlSupport.deepEquals(this.bearerProto, other.bearerProto) && this.pcoId == other.pcoId && HidlSupport.deepEquals(this.contents, other.contents);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cid))), Integer.valueOf(HidlSupport.deepHashCode(this.bearerProto)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.pcoId))), Integer.valueOf(HidlSupport.deepHashCode(this.contents))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".cid = ");
        builder.append(this.cid);
        builder.append(", .bearerProto = ");
        builder.append(this.bearerProto);
        builder.append(", .pcoId = ");
        builder.append(this.pcoId);
        builder.append(", .contents = ");
        builder.append(this.contents);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<PcoDataInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<PcoDataInfo> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            PcoDataInfo _hidl_vec_element = new PcoDataInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.cid = _hidl_blob.getInt32(0 + _hidl_offset);
        this.bearerProto = _hidl_blob.getString(8 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.bearerProto.getBytes().length + 1), _hidl_blob.handle(), (8 + _hidl_offset) + 0, false);
        this.pcoId = _hidl_blob.getInt32(24 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((32 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (32 + _hidl_offset) + 0, true);
        this.contents.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.contents.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<PcoDataInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((PcoDataInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.cid);
        _hidl_blob.putString(_hidl_offset + 8, this.bearerProto);
        _hidl_blob.putInt32(24 + _hidl_offset, this.pcoId);
        int _hidl_vec_size = this.contents.size();
        _hidl_blob.putInt32((_hidl_offset + 32) + 8, _hidl_vec_size);
        _hidl_blob.putBool((_hidl_offset + 32) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.contents.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((_hidl_offset + 32) + 0, childBlob);
    }
}
