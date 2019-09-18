package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class Dial {
    public String address = new String();
    public int clir;
    public final ArrayList<UusInfo> uusInfo = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != Dial.class) {
            return false;
        }
        Dial other = (Dial) otherObject;
        if (HidlSupport.deepEquals(this.address, other.address) && this.clir == other.clir && HidlSupport.deepEquals(this.uusInfo, other.uusInfo)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.address)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.clir))), Integer.valueOf(HidlSupport.deepHashCode(this.uusInfo))});
    }

    public final String toString() {
        return "{" + ".address = " + this.address + ", .clir = " + Clir.toString(this.clir) + ", .uusInfo = " + this.uusInfo + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<Dial> readVectorFromParcel(HwParcel parcel) {
        ArrayList<Dial> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            Dial _hidl_vec_element = new Dial();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.address = hwBlob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.address.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.clir = hwBlob.getInt32(_hidl_offset + 16);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 24 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), _hidl_offset + 24 + 0, true);
        this.uusInfo.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            UusInfo _hidl_vec_element = new UusInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            this.uusInfo.add(_hidl_vec_element);
        }
        HwParcel hwParcel = parcel;
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<Dial> _hidl_vec) {
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
        _hidl_blob.putString(_hidl_offset + 0, this.address);
        _hidl_blob.putInt32(16 + _hidl_offset, this.clir);
        int _hidl_vec_size = this.uusInfo.size();
        _hidl_blob.putInt32(_hidl_offset + 24 + 8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        _hidl_blob.putBool(_hidl_offset + 24 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        while (true) {
            int _hidl_index_02 = _hidl_index_0;
            if (_hidl_index_02 < _hidl_vec_size) {
                this.uusInfo.get(_hidl_index_02).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_02 * 24));
                _hidl_index_0 = _hidl_index_02 + 1;
            } else {
                _hidl_blob.putBlob(24 + _hidl_offset + 0, childBlob);
                return;
            }
        }
    }
}
