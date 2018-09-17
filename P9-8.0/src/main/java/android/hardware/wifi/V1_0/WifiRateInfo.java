package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WifiRateInfo {
    public int bitRateInKbps;
    public int bw;
    public int nss;
    public int preamble;
    public byte rateMcsIdx;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WifiRateInfo.class) {
            return false;
        }
        WifiRateInfo other = (WifiRateInfo) otherObject;
        return this.preamble == other.preamble && this.nss == other.nss && this.bw == other.bw && this.rateMcsIdx == other.rateMcsIdx && this.bitRateInKbps == other.bitRateInKbps;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.preamble))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.nss))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bw))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rateMcsIdx))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bitRateInKbps)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".preamble = ");
        builder.append(WifiRatePreamble.toString(this.preamble));
        builder.append(", .nss = ");
        builder.append(WifiRateNss.toString(this.nss));
        builder.append(", .bw = ");
        builder.append(WifiChannelWidthInMhz.toString(this.bw));
        builder.append(", .rateMcsIdx = ");
        builder.append(this.rateMcsIdx);
        builder.append(", .bitRateInKbps = ");
        builder.append(this.bitRateInKbps);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(20), 0);
    }

    public static final ArrayList<WifiRateInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiRateInfo> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 20), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiRateInfo _hidl_vec_element = new WifiRateInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 20));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.preamble = _hidl_blob.getInt32(0 + _hidl_offset);
        this.nss = _hidl_blob.getInt32(4 + _hidl_offset);
        this.bw = _hidl_blob.getInt32(8 + _hidl_offset);
        this.rateMcsIdx = _hidl_blob.getInt8(12 + _hidl_offset);
        this.bitRateInKbps = _hidl_blob.getInt32(16 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(20);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WifiRateInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 20);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((WifiRateInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 20));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.preamble);
        _hidl_blob.putInt32(4 + _hidl_offset, this.nss);
        _hidl_blob.putInt32(8 + _hidl_offset, this.bw);
        _hidl_blob.putInt8(12 + _hidl_offset, this.rateMcsIdx);
        _hidl_blob.putInt32(16 + _hidl_offset, this.bitRateInKbps);
    }
}
