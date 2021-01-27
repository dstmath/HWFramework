package android.hardware.broadcastradio.V2_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class ProgramInfo {
    public int infoFlags;
    public ProgramIdentifier logicallyTunedTo = new ProgramIdentifier();
    public ArrayList<Metadata> metadata = new ArrayList<>();
    public ProgramIdentifier physicallyTunedTo = new ProgramIdentifier();
    public ArrayList<ProgramIdentifier> relatedContent = new ArrayList<>();
    public ProgramSelector selector = new ProgramSelector();
    public int signalQuality;
    public ArrayList<VendorKeyValue> vendorInfo = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != ProgramInfo.class) {
            return false;
        }
        ProgramInfo other = (ProgramInfo) otherObject;
        if (HidlSupport.deepEquals(this.selector, other.selector) && HidlSupport.deepEquals(this.logicallyTunedTo, other.logicallyTunedTo) && HidlSupport.deepEquals(this.physicallyTunedTo, other.physicallyTunedTo) && HidlSupport.deepEquals(this.relatedContent, other.relatedContent) && HidlSupport.deepEquals(Integer.valueOf(this.infoFlags), Integer.valueOf(other.infoFlags)) && this.signalQuality == other.signalQuality && HidlSupport.deepEquals(this.metadata, other.metadata) && HidlSupport.deepEquals(this.vendorInfo, other.vendorInfo)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.selector)), Integer.valueOf(HidlSupport.deepHashCode(this.logicallyTunedTo)), Integer.valueOf(HidlSupport.deepHashCode(this.physicallyTunedTo)), Integer.valueOf(HidlSupport.deepHashCode(this.relatedContent)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.infoFlags))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.signalQuality))), Integer.valueOf(HidlSupport.deepHashCode(this.metadata)), Integer.valueOf(HidlSupport.deepHashCode(this.vendorInfo)));
    }

    public final String toString() {
        return "{.selector = " + this.selector + ", .logicallyTunedTo = " + this.logicallyTunedTo + ", .physicallyTunedTo = " + this.physicallyTunedTo + ", .relatedContent = " + this.relatedContent + ", .infoFlags = " + ProgramInfoFlags.dumpBitfield(this.infoFlags) + ", .signalQuality = " + this.signalQuality + ", .metadata = " + this.metadata + ", .vendorInfo = " + this.vendorInfo + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(120), 0);
    }

    public static final ArrayList<ProgramInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<ProgramInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 120), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ProgramInfo _hidl_vec_element = new ProgramInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 120));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.selector.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 0);
        this.logicallyTunedTo.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 32);
        this.physicallyTunedTo.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 48);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 64 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), _hidl_offset + 64 + 0, true);
        this.relatedContent.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ProgramIdentifier _hidl_vec_element = new ProgramIdentifier();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 16));
            this.relatedContent.add(_hidl_vec_element);
        }
        this.infoFlags = _hidl_blob.getInt32(_hidl_offset + 80);
        this.signalQuality = _hidl_blob.getInt32(_hidl_offset + 84);
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 88 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 32), _hidl_blob.handle(), _hidl_offset + 88 + 0, true);
        this.metadata.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            Metadata _hidl_vec_element2 = new Metadata();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob2, (long) (_hidl_index_02 * 32));
            this.metadata.add(_hidl_vec_element2);
        }
        int _hidl_vec_size3 = _hidl_blob.getInt32(_hidl_offset + 104 + 8);
        HwBlob childBlob3 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 32), _hidl_blob.handle(), _hidl_offset + 104 + 0, true);
        this.vendorInfo.clear();
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            VendorKeyValue _hidl_vec_element3 = new VendorKeyValue();
            _hidl_vec_element3.readEmbeddedFromParcel(parcel, childBlob3, (long) (_hidl_index_03 * 32));
            this.vendorInfo.add(_hidl_vec_element3);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(120);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ProgramInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 120);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 120));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.selector.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 0);
        this.logicallyTunedTo.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 32);
        this.physicallyTunedTo.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 48);
        int _hidl_vec_size = this.relatedContent.size();
        _hidl_blob.putInt32(_hidl_offset + 64 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 64 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 16);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.relatedContent.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 16));
        }
        _hidl_blob.putBlob(_hidl_offset + 64 + 0, childBlob);
        _hidl_blob.putInt32(_hidl_offset + 80, this.infoFlags);
        _hidl_blob.putInt32(_hidl_offset + 84, this.signalQuality);
        int _hidl_vec_size2 = this.metadata.size();
        _hidl_blob.putInt32(_hidl_offset + 88 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 88 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 32);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.metadata.get(_hidl_index_02).writeEmbeddedToBlob(childBlob2, (long) (_hidl_index_02 * 32));
        }
        _hidl_blob.putBlob(_hidl_offset + 88 + 0, childBlob2);
        int _hidl_vec_size3 = this.vendorInfo.size();
        _hidl_blob.putInt32(_hidl_offset + 104 + 8, _hidl_vec_size3);
        _hidl_blob.putBool(_hidl_offset + 104 + 12, false);
        HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 32);
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            this.vendorInfo.get(_hidl_index_03).writeEmbeddedToBlob(childBlob3, (long) (_hidl_index_03 * 32));
        }
        _hidl_blob.putBlob(_hidl_offset + 104 + 0, childBlob3);
    }
}
