package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NanBandSpecificConfig {
    public byte discoveryWindowIntervalVal;
    public byte dwellTimeMs;
    public byte rssiClose;
    public byte rssiCloseProximity;
    public byte rssiMiddle;
    public short scanPeriodSec;
    public boolean validDiscoveryWindowIntervalVal;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanBandSpecificConfig.class) {
            return false;
        }
        NanBandSpecificConfig other = (NanBandSpecificConfig) otherObject;
        return this.rssiClose == other.rssiClose && this.rssiMiddle == other.rssiMiddle && this.rssiCloseProximity == other.rssiCloseProximity && this.dwellTimeMs == other.dwellTimeMs && this.scanPeriodSec == other.scanPeriodSec && this.validDiscoveryWindowIntervalVal == other.validDiscoveryWindowIntervalVal && this.discoveryWindowIntervalVal == other.discoveryWindowIntervalVal;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rssiClose))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rssiMiddle))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rssiCloseProximity))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.dwellTimeMs))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.scanPeriodSec))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validDiscoveryWindowIntervalVal))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.discoveryWindowIntervalVal)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".rssiClose = ");
        builder.append(this.rssiClose);
        builder.append(", .rssiMiddle = ");
        builder.append(this.rssiMiddle);
        builder.append(", .rssiCloseProximity = ");
        builder.append(this.rssiCloseProximity);
        builder.append(", .dwellTimeMs = ");
        builder.append(this.dwellTimeMs);
        builder.append(", .scanPeriodSec = ");
        builder.append(this.scanPeriodSec);
        builder.append(", .validDiscoveryWindowIntervalVal = ");
        builder.append(this.validDiscoveryWindowIntervalVal);
        builder.append(", .discoveryWindowIntervalVal = ");
        builder.append(this.discoveryWindowIntervalVal);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(8), 0);
    }

    public static final ArrayList<NanBandSpecificConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanBandSpecificConfig> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 8), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanBandSpecificConfig _hidl_vec_element = new NanBandSpecificConfig();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 8));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.rssiClose = _hidl_blob.getInt8(0 + _hidl_offset);
        this.rssiMiddle = _hidl_blob.getInt8(1 + _hidl_offset);
        this.rssiCloseProximity = _hidl_blob.getInt8(2 + _hidl_offset);
        this.dwellTimeMs = _hidl_blob.getInt8(3 + _hidl_offset);
        this.scanPeriodSec = _hidl_blob.getInt16(4 + _hidl_offset);
        this.validDiscoveryWindowIntervalVal = _hidl_blob.getBool(6 + _hidl_offset);
        this.discoveryWindowIntervalVal = _hidl_blob.getInt8(7 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(8);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanBandSpecificConfig> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 8);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((NanBandSpecificConfig) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 8));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(0 + _hidl_offset, this.rssiClose);
        _hidl_blob.putInt8(1 + _hidl_offset, this.rssiMiddle);
        _hidl_blob.putInt8(2 + _hidl_offset, this.rssiCloseProximity);
        _hidl_blob.putInt8(3 + _hidl_offset, this.dwellTimeMs);
        _hidl_blob.putInt16(4 + _hidl_offset, this.scanPeriodSec);
        _hidl_blob.putBool(6 + _hidl_offset, this.validDiscoveryWindowIntervalVal);
        _hidl_blob.putInt8(7 + _hidl_offset, this.discoveryWindowIntervalVal);
    }
}
