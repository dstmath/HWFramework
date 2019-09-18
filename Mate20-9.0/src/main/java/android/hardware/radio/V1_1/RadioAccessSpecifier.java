package android.hardware.radio.V1_1;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RadioAccessSpecifier {
    public final ArrayList<Integer> channels = new ArrayList<>();
    public final ArrayList<Integer> eutranBands = new ArrayList<>();
    public final ArrayList<Integer> geranBands = new ArrayList<>();
    public int radioAccessNetwork;
    public final ArrayList<Integer> utranBands = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RadioAccessSpecifier.class) {
            return false;
        }
        RadioAccessSpecifier other = (RadioAccessSpecifier) otherObject;
        if (this.radioAccessNetwork == other.radioAccessNetwork && HidlSupport.deepEquals(this.geranBands, other.geranBands) && HidlSupport.deepEquals(this.utranBands, other.utranBands) && HidlSupport.deepEquals(this.eutranBands, other.eutranBands) && HidlSupport.deepEquals(this.channels, other.channels)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.radioAccessNetwork))), Integer.valueOf(HidlSupport.deepHashCode(this.geranBands)), Integer.valueOf(HidlSupport.deepHashCode(this.utranBands)), Integer.valueOf(HidlSupport.deepHashCode(this.eutranBands)), Integer.valueOf(HidlSupport.deepHashCode(this.channels))});
    }

    public final String toString() {
        return "{" + ".radioAccessNetwork = " + RadioAccessNetworks.toString(this.radioAccessNetwork) + ", .geranBands = " + this.geranBands + ", .utranBands = " + this.utranBands + ", .eutranBands = " + this.eutranBands + ", .channels = " + this.channels + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(72), 0);
    }

    public static final ArrayList<RadioAccessSpecifier> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RadioAccessSpecifier> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 72), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RadioAccessSpecifier _hidl_vec_element = new RadioAccessSpecifier();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 72));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.radioAccessNetwork = hwBlob.getInt32(_hidl_offset + 0);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.geranBands.clear();
        int _hidl_index_0 = 0;
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size; _hidl_index_02++) {
            this.geranBands.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_02 * 4))));
        }
        int _hidl_vec_size2 = hwBlob.getInt32(_hidl_offset + 24 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 4), _hidl_blob.handle(), _hidl_offset + 24 + 0, true);
        this.utranBands.clear();
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size2; _hidl_index_03++) {
            this.utranBands.add(Integer.valueOf(childBlob2.getInt32((long) (_hidl_index_03 * 4))));
        }
        int _hidl_vec_size3 = hwBlob.getInt32(_hidl_offset + 40 + 8);
        HwBlob childBlob3 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 4), _hidl_blob.handle(), _hidl_offset + 40 + 0, true);
        this.eutranBands.clear();
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size3; _hidl_index_04++) {
            this.eutranBands.add(Integer.valueOf(childBlob3.getInt32((long) (_hidl_index_04 * 4))));
        }
        int _hidl_vec_size4 = hwBlob.getInt32(_hidl_offset + 56 + 8);
        HwBlob childBlob4 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size4 * 4), _hidl_blob.handle(), _hidl_offset + 56 + 0, true);
        this.channels.clear();
        while (true) {
            int _hidl_index_05 = _hidl_index_0;
            if (_hidl_index_05 < _hidl_vec_size4) {
                this.channels.add(Integer.valueOf(childBlob4.getInt32((long) (_hidl_index_05 * 4))));
                _hidl_index_0 = _hidl_index_05 + 1;
            } else {
                return;
            }
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(72);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RadioAccessSpecifier> _hidl_vec) {
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
        HwBlob hwBlob = _hidl_blob;
        hwBlob.putInt32(_hidl_offset + 0, this.radioAccessNetwork);
        int _hidl_vec_size = this.geranBands.size();
        hwBlob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        hwBlob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt32((long) (_hidl_index_0 * 4), this.geranBands.get(_hidl_index_0).intValue());
        }
        hwBlob.putBlob(_hidl_offset + 8 + 0, childBlob);
        int _hidl_vec_size2 = this.utranBands.size();
        hwBlob.putInt32(_hidl_offset + 24 + 8, _hidl_vec_size2);
        hwBlob.putBool(_hidl_offset + 24 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 4);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            childBlob2.putInt32((long) (_hidl_index_02 * 4), this.utranBands.get(_hidl_index_02).intValue());
        }
        hwBlob.putBlob(_hidl_offset + 24 + 0, childBlob2);
        int _hidl_vec_size3 = this.eutranBands.size();
        hwBlob.putInt32(_hidl_offset + 40 + 8, _hidl_vec_size3);
        hwBlob.putBool(_hidl_offset + 40 + 12, false);
        HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 4);
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            childBlob3.putInt32((long) (_hidl_index_03 * 4), this.eutranBands.get(_hidl_index_03).intValue());
        }
        hwBlob.putBlob(_hidl_offset + 40 + 0, childBlob3);
        int _hidl_vec_size4 = this.channels.size();
        hwBlob.putInt32(_hidl_offset + 56 + 8, _hidl_vec_size4);
        int _hidl_index_04 = 0;
        hwBlob.putBool(_hidl_offset + 56 + 12, false);
        HwBlob childBlob4 = new HwBlob(_hidl_vec_size4 * 4);
        while (true) {
            int _hidl_index_05 = _hidl_index_04;
            if (_hidl_index_05 < _hidl_vec_size4) {
                childBlob4.putInt32((long) (_hidl_index_05 * 4), this.channels.get(_hidl_index_05).intValue());
                _hidl_index_04 = _hidl_index_05 + 1;
            } else {
                hwBlob.putBlob(_hidl_offset + 56 + 0, childBlob4);
                return;
            }
        }
    }
}
