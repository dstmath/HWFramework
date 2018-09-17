package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class IccIo {
    public String aid = new String();
    public int command;
    public String data = new String();
    public int fileId;
    public int p1;
    public int p2;
    public int p3;
    public String path = new String();
    public String pin2 = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != IccIo.class) {
            return false;
        }
        IccIo other = (IccIo) otherObject;
        return this.command == other.command && this.fileId == other.fileId && HidlSupport.deepEquals(this.path, other.path) && this.p1 == other.p1 && this.p2 == other.p2 && this.p3 == other.p3 && HidlSupport.deepEquals(this.data, other.data) && HidlSupport.deepEquals(this.pin2, other.pin2) && HidlSupport.deepEquals(this.aid, other.aid);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.command))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.fileId))), Integer.valueOf(HidlSupport.deepHashCode(this.path)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.p1))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.p2))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.p3))), Integer.valueOf(HidlSupport.deepHashCode(this.data)), Integer.valueOf(HidlSupport.deepHashCode(this.pin2)), Integer.valueOf(HidlSupport.deepHashCode(this.aid))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".command = ");
        builder.append(this.command);
        builder.append(", .fileId = ");
        builder.append(this.fileId);
        builder.append(", .path = ");
        builder.append(this.path);
        builder.append(", .p1 = ");
        builder.append(this.p1);
        builder.append(", .p2 = ");
        builder.append(this.p2);
        builder.append(", .p3 = ");
        builder.append(this.p3);
        builder.append(", .data = ");
        builder.append(this.data);
        builder.append(", .pin2 = ");
        builder.append(this.pin2);
        builder.append(", .aid = ");
        builder.append(this.aid);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(88), 0);
    }

    public static final ArrayList<IccIo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<IccIo> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 88), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            IccIo _hidl_vec_element = new IccIo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 88));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.command = _hidl_blob.getInt32(0 + _hidl_offset);
        this.fileId = _hidl_blob.getInt32(4 + _hidl_offset);
        this.path = _hidl_blob.getString(8 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.path.getBytes().length + 1), _hidl_blob.handle(), 0 + (8 + _hidl_offset), false);
        this.p1 = _hidl_blob.getInt32(24 + _hidl_offset);
        this.p2 = _hidl_blob.getInt32(28 + _hidl_offset);
        this.p3 = _hidl_blob.getInt32(32 + _hidl_offset);
        this.data = _hidl_blob.getString(40 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.data.getBytes().length + 1), _hidl_blob.handle(), 0 + (40 + _hidl_offset), false);
        this.pin2 = _hidl_blob.getString(56 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.pin2.getBytes().length + 1), _hidl_blob.handle(), 0 + (56 + _hidl_offset), false);
        this.aid = _hidl_blob.getString(72 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.aid.getBytes().length + 1), _hidl_blob.handle(), 0 + (72 + _hidl_offset), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(88);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<IccIo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 88);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((IccIo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 88));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.command);
        _hidl_blob.putInt32(4 + _hidl_offset, this.fileId);
        _hidl_blob.putString(8 + _hidl_offset, this.path);
        _hidl_blob.putInt32(24 + _hidl_offset, this.p1);
        _hidl_blob.putInt32(28 + _hidl_offset, this.p2);
        _hidl_blob.putInt32(32 + _hidl_offset, this.p3);
        _hidl_blob.putString(40 + _hidl_offset, this.data);
        _hidl_blob.putString(56 + _hidl_offset, this.pin2);
        _hidl_blob.putString(72 + _hidl_offset, this.aid);
    }
}
