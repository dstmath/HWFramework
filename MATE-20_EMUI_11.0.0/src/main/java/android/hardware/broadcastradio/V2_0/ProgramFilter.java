package android.hardware.broadcastradio.V2_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class ProgramFilter {
    public boolean excludeModifications;
    public ArrayList<Integer> identifierTypes = new ArrayList<>();
    public ArrayList<ProgramIdentifier> identifiers = new ArrayList<>();
    public boolean includeCategories;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != ProgramFilter.class) {
            return false;
        }
        ProgramFilter other = (ProgramFilter) otherObject;
        if (HidlSupport.deepEquals(this.identifierTypes, other.identifierTypes) && HidlSupport.deepEquals(this.identifiers, other.identifiers) && this.includeCategories == other.includeCategories && this.excludeModifications == other.excludeModifications) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.identifierTypes)), Integer.valueOf(HidlSupport.deepHashCode(this.identifiers)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.includeCategories))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.excludeModifications))));
    }

    public final String toString() {
        return "{.identifierTypes = " + this.identifierTypes + ", .identifiers = " + this.identifiers + ", .includeCategories = " + this.includeCategories + ", .excludeModifications = " + this.excludeModifications + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<ProgramFilter> readVectorFromParcel(HwParcel parcel) {
        ArrayList<ProgramFilter> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ProgramFilter _hidl_vec_element = new ProgramFilter();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 0 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), _hidl_offset + 0 + 0, true);
        this.identifierTypes.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.identifierTypes.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
        }
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 16), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.identifiers.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            ProgramIdentifier _hidl_vec_element = new ProgramIdentifier();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_02 * 16));
            this.identifiers.add(_hidl_vec_element);
        }
        this.includeCategories = _hidl_blob.getBool(_hidl_offset + 32);
        this.excludeModifications = _hidl_blob.getBool(_hidl_offset + 33);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ProgramFilter> _hidl_vec) {
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
        int _hidl_vec_size = this.identifierTypes.size();
        _hidl_blob.putInt32(_hidl_offset + 0 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 0 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt32((long) (_hidl_index_0 * 4), this.identifierTypes.get(_hidl_index_0).intValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 0 + 0, childBlob);
        int _hidl_vec_size2 = this.identifiers.size();
        _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 16);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.identifiers.get(_hidl_index_02).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_02 * 16));
        }
        _hidl_blob.putBlob(_hidl_offset + 16 + 0, childBlob2);
        _hidl_blob.putBool(_hidl_offset + 32, this.includeCategories);
        _hidl_blob.putBool(_hidl_offset + 33, this.excludeModifications);
    }
}
