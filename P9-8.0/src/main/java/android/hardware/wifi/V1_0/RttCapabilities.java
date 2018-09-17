package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RttCapabilities {
    public int bwSupport;
    public boolean lciSupported;
    public boolean lcrSupported;
    public byte mcVersion;
    public int preambleSupport;
    public boolean responderSupported;
    public boolean rttFtmSupported;
    public boolean rttOneSidedSupported;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RttCapabilities.class) {
            return false;
        }
        RttCapabilities other = (RttCapabilities) otherObject;
        return this.rttOneSidedSupported == other.rttOneSidedSupported && this.rttFtmSupported == other.rttFtmSupported && this.lciSupported == other.lciSupported && this.lcrSupported == other.lcrSupported && this.responderSupported == other.responderSupported && HidlSupport.deepEquals(Integer.valueOf(this.preambleSupport), Integer.valueOf(other.preambleSupport)) && HidlSupport.deepEquals(Integer.valueOf(this.bwSupport), Integer.valueOf(other.bwSupport)) && this.mcVersion == other.mcVersion;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.rttOneSidedSupported))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.rttFtmSupported))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.lciSupported))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.lcrSupported))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.responderSupported))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.preambleSupport))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bwSupport))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.mcVersion)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".rttOneSidedSupported = ");
        builder.append(this.rttOneSidedSupported);
        builder.append(", .rttFtmSupported = ");
        builder.append(this.rttFtmSupported);
        builder.append(", .lciSupported = ");
        builder.append(this.lciSupported);
        builder.append(", .lcrSupported = ");
        builder.append(this.lcrSupported);
        builder.append(", .responderSupported = ");
        builder.append(this.responderSupported);
        builder.append(", .preambleSupport = ");
        builder.append(RttPreamble.dumpBitfield(this.preambleSupport));
        builder.append(", .bwSupport = ");
        builder.append(RttBw.dumpBitfield(this.bwSupport));
        builder.append(", .mcVersion = ");
        builder.append(this.mcVersion);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(20), 0);
    }

    public static final ArrayList<RttCapabilities> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RttCapabilities> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 20), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RttCapabilities _hidl_vec_element = new RttCapabilities();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 20));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.rttOneSidedSupported = _hidl_blob.getBool(0 + _hidl_offset);
        this.rttFtmSupported = _hidl_blob.getBool(1 + _hidl_offset);
        this.lciSupported = _hidl_blob.getBool(2 + _hidl_offset);
        this.lcrSupported = _hidl_blob.getBool(3 + _hidl_offset);
        this.responderSupported = _hidl_blob.getBool(4 + _hidl_offset);
        this.preambleSupport = _hidl_blob.getInt32(8 + _hidl_offset);
        this.bwSupport = _hidl_blob.getInt32(12 + _hidl_offset);
        this.mcVersion = _hidl_blob.getInt8(16 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(20);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RttCapabilities> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 20);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RttCapabilities) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 20));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putBool(0 + _hidl_offset, this.rttOneSidedSupported);
        _hidl_blob.putBool(1 + _hidl_offset, this.rttFtmSupported);
        _hidl_blob.putBool(2 + _hidl_offset, this.lciSupported);
        _hidl_blob.putBool(3 + _hidl_offset, this.lcrSupported);
        _hidl_blob.putBool(4 + _hidl_offset, this.responderSupported);
        _hidl_blob.putInt32(8 + _hidl_offset, this.preambleSupport);
        _hidl_blob.putInt32(12 + _hidl_offset, this.bwSupport);
        _hidl_blob.putInt8(16 + _hidl_offset, this.mcVersion);
    }
}
