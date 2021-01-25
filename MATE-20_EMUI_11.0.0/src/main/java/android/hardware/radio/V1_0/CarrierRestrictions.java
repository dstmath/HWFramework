package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CarrierRestrictions {
    public ArrayList<Carrier> allowedCarriers = new ArrayList<>();
    public ArrayList<Carrier> excludedCarriers = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CarrierRestrictions.class) {
            return false;
        }
        CarrierRestrictions other = (CarrierRestrictions) otherObject;
        if (HidlSupport.deepEquals(this.allowedCarriers, other.allowedCarriers) && HidlSupport.deepEquals(this.excludedCarriers, other.excludedCarriers)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.allowedCarriers)), Integer.valueOf(HidlSupport.deepHashCode(this.excludedCarriers)));
    }

    public final String toString() {
        return "{.allowedCarriers = " + this.allowedCarriers + ", .excludedCarriers = " + this.excludedCarriers + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<CarrierRestrictions> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CarrierRestrictions> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CarrierRestrictions _hidl_vec_element = new CarrierRestrictions();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 0 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), _hidl_offset + 0 + 0, true);
        this.allowedCarriers.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            Carrier _hidl_vec_element = new Carrier();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            this.allowedCarriers.add(_hidl_vec_element);
        }
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 56), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.excludedCarriers.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            Carrier _hidl_vec_element2 = new Carrier();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_02 * 56));
            this.excludedCarriers.add(_hidl_vec_element2);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CarrierRestrictions> _hidl_vec) {
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
        int _hidl_vec_size = this.allowedCarriers.size();
        _hidl_blob.putInt32(_hidl_offset + 0 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 0 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.allowedCarriers.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(_hidl_offset + 0 + 0, childBlob);
        int _hidl_vec_size2 = this.excludedCarriers.size();
        _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 56);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.excludedCarriers.get(_hidl_index_02).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_02 * 56));
        }
        _hidl_blob.putBlob(_hidl_offset + 16 + 0, childBlob2);
    }
}
