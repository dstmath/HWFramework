package android.hardware.radio.V1_4;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class EmergencyNumber {
    public int categories;
    public String mcc = new String();
    public String mnc = new String();
    public String number = new String();
    public int sources;
    public ArrayList<String> urns = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != EmergencyNumber.class) {
            return false;
        }
        EmergencyNumber other = (EmergencyNumber) otherObject;
        if (HidlSupport.deepEquals(this.number, other.number) && HidlSupport.deepEquals(this.mcc, other.mcc) && HidlSupport.deepEquals(this.mnc, other.mnc) && HidlSupport.deepEquals(Integer.valueOf(this.categories), Integer.valueOf(other.categories)) && HidlSupport.deepEquals(this.urns, other.urns) && HidlSupport.deepEquals(Integer.valueOf(this.sources), Integer.valueOf(other.sources))) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.number)), Integer.valueOf(HidlSupport.deepHashCode(this.mcc)), Integer.valueOf(HidlSupport.deepHashCode(this.mnc)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.categories))), Integer.valueOf(HidlSupport.deepHashCode(this.urns)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sources))));
    }

    public final String toString() {
        return "{.number = " + this.number + ", .mcc = " + this.mcc + ", .mnc = " + this.mnc + ", .categories = " + EmergencyServiceCategory.dumpBitfield(this.categories) + ", .urns = " + this.urns + ", .sources = " + EmergencyNumberSource.dumpBitfield(this.sources) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(80), 0);
    }

    public static final ArrayList<EmergencyNumber> readVectorFromParcel(HwParcel parcel) {
        ArrayList<EmergencyNumber> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 80), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            EmergencyNumber _hidl_vec_element = new EmergencyNumber();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 80));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.number = _hidl_blob.getString(_hidl_offset + 0);
        parcel.readEmbeddedBuffer((long) (this.number.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 0 + 0, false);
        this.mcc = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.mcc.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.mnc = _hidl_blob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.mnc.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
        this.categories = _hidl_blob.getInt32(_hidl_offset + 48);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 56 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), _hidl_offset + 56 + 0, true);
        this.urns.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            new String();
            String _hidl_vec_element = childBlob.getString((long) (_hidl_index_0 * 16));
            parcel.readEmbeddedBuffer((long) (_hidl_vec_element.getBytes().length + 1), childBlob.handle(), (long) ((_hidl_index_0 * 16) + 0), false);
            this.urns.add(_hidl_vec_element);
        }
        this.sources = _hidl_blob.getInt32(_hidl_offset + 72);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(80);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<EmergencyNumber> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 80);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 80));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putString(_hidl_offset + 0, this.number);
        _hidl_blob.putString(16 + _hidl_offset, this.mcc);
        _hidl_blob.putString(32 + _hidl_offset, this.mnc);
        _hidl_blob.putInt32(48 + _hidl_offset, this.categories);
        int _hidl_vec_size = this.urns.size();
        _hidl_blob.putInt32(_hidl_offset + 56 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 56 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putString((long) (_hidl_index_0 * 16), this.urns.get(_hidl_index_0));
        }
        _hidl_blob.putBlob(56 + _hidl_offset + 0, childBlob);
        _hidl_blob.putInt32(72 + _hidl_offset, this.sources);
    }
}
