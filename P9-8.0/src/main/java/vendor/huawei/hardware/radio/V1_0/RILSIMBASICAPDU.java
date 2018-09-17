package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RILSIMBASICAPDU {
    public int channel;
    public int cla;
    public int command;
    public String data = new String();
    public int p1;
    public int p2;
    public int p3;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RILSIMBASICAPDU.class) {
            return false;
        }
        RILSIMBASICAPDU other = (RILSIMBASICAPDU) otherObject;
        return this.channel == other.channel && this.cla == other.cla && this.command == other.command && this.p1 == other.p1 && this.p2 == other.p2 && this.p3 == other.p3 && HidlSupport.deepEquals(this.data, other.data);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channel))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cla))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.command))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.p1))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.p2))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.p3))), Integer.valueOf(HidlSupport.deepHashCode(this.data))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".channel = ");
        builder.append(this.channel);
        builder.append(", .cla = ");
        builder.append(this.cla);
        builder.append(", .command = ");
        builder.append(this.command);
        builder.append(", .p1 = ");
        builder.append(this.p1);
        builder.append(", .p2 = ");
        builder.append(this.p2);
        builder.append(", .p3 = ");
        builder.append(this.p3);
        builder.append(", .data = ");
        builder.append(this.data);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<RILSIMBASICAPDU> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RILSIMBASICAPDU> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RILSIMBASICAPDU _hidl_vec_element = new RILSIMBASICAPDU();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.channel = _hidl_blob.getInt32(_hidl_offset + 0);
        this.cla = _hidl_blob.getInt32(4 + _hidl_offset);
        this.command = _hidl_blob.getInt32(8 + _hidl_offset);
        this.p1 = _hidl_blob.getInt32(12 + _hidl_offset);
        this.p2 = _hidl_blob.getInt32(16 + _hidl_offset);
        this.p3 = _hidl_blob.getInt32(20 + _hidl_offset);
        this.data = _hidl_blob.getString(_hidl_offset + 24);
        parcel.readEmbeddedBuffer((long) (this.data.getBytes().length + 1), _hidl_blob.handle(), 0 + (_hidl_offset + 24), false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RILSIMBASICAPDU> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RILSIMBASICAPDU) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.channel);
        _hidl_blob.putInt32(4 + _hidl_offset, this.cla);
        _hidl_blob.putInt32(8 + _hidl_offset, this.command);
        _hidl_blob.putInt32(12 + _hidl_offset, this.p1);
        _hidl_blob.putInt32(16 + _hidl_offset, this.p2);
        _hidl_blob.putInt32(20 + _hidl_offset, this.p3);
        _hidl_blob.putString(24 + _hidl_offset, this.data);
    }
}
