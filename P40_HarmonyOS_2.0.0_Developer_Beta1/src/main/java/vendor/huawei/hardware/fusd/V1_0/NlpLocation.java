package vendor.huawei.hardware.fusd.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NlpLocation {
    public float accuracy;
    public double altitude;
    public float bearing;
    public short flags;
    public int isValid;
    public double latitude;
    public double longitude;
    public int matchedWifi;
    public int scannedWifi;
    public int source;
    public float speed;
    public long timestamp;
    public int usedWifi;
    public int wlanScanage;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NlpLocation.class) {
            return false;
        }
        NlpLocation other = (NlpLocation) otherObject;
        if (this.timestamp == other.timestamp && this.latitude == other.latitude && this.longitude == other.longitude && this.altitude == other.altitude && this.speed == other.speed && this.bearing == other.bearing && this.accuracy == other.accuracy && this.matchedWifi == other.matchedWifi && this.scannedWifi == other.scannedWifi && this.usedWifi == other.usedWifi && this.wlanScanage == other.wlanScanage && this.isValid == other.isValid && this.source == other.source && this.flags == other.flags) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.timestamp))), Integer.valueOf(HidlSupport.deepHashCode(Double.valueOf(this.latitude))), Integer.valueOf(HidlSupport.deepHashCode(Double.valueOf(this.longitude))), Integer.valueOf(HidlSupport.deepHashCode(Double.valueOf(this.altitude))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.speed))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.bearing))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.accuracy))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.matchedWifi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.scannedWifi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.usedWifi))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.wlanScanage))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.isValid))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.source))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.flags))));
    }

    public final String toString() {
        return "{.timestamp = " + this.timestamp + ", .latitude = " + this.latitude + ", .longitude = " + this.longitude + ", .altitude = " + this.altitude + ", .speed = " + this.speed + ", .bearing = " + this.bearing + ", .accuracy = " + this.accuracy + ", .matchedWifi = " + this.matchedWifi + ", .scannedWifi = " + this.scannedWifi + ", .usedWifi = " + this.usedWifi + ", .wlanScanage = " + this.wlanScanage + ", .isValid = " + this.isValid + ", .source = " + this.source + ", .flags = " + ((int) this.flags) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(72), 0);
    }

    public static final ArrayList<NlpLocation> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NlpLocation> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 72), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NlpLocation _hidl_vec_element = new NlpLocation();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 72));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.timestamp = _hidl_blob.getInt64(0 + _hidl_offset);
        this.latitude = _hidl_blob.getDouble(8 + _hidl_offset);
        this.longitude = _hidl_blob.getDouble(16 + _hidl_offset);
        this.altitude = _hidl_blob.getDouble(24 + _hidl_offset);
        this.speed = _hidl_blob.getFloat(32 + _hidl_offset);
        this.bearing = _hidl_blob.getFloat(36 + _hidl_offset);
        this.accuracy = _hidl_blob.getFloat(40 + _hidl_offset);
        this.matchedWifi = _hidl_blob.getInt32(44 + _hidl_offset);
        this.scannedWifi = _hidl_blob.getInt32(48 + _hidl_offset);
        this.usedWifi = _hidl_blob.getInt32(52 + _hidl_offset);
        this.wlanScanage = _hidl_blob.getInt32(56 + _hidl_offset);
        this.isValid = _hidl_blob.getInt32(60 + _hidl_offset);
        this.source = _hidl_blob.getInt32(64 + _hidl_offset);
        this.flags = _hidl_blob.getInt16(68 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(72);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NlpLocation> _hidl_vec) {
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
        _hidl_blob.putInt64(0 + _hidl_offset, this.timestamp);
        _hidl_blob.putDouble(8 + _hidl_offset, this.latitude);
        _hidl_blob.putDouble(16 + _hidl_offset, this.longitude);
        _hidl_blob.putDouble(24 + _hidl_offset, this.altitude);
        _hidl_blob.putFloat(32 + _hidl_offset, this.speed);
        _hidl_blob.putFloat(36 + _hidl_offset, this.bearing);
        _hidl_blob.putFloat(40 + _hidl_offset, this.accuracy);
        _hidl_blob.putInt32(44 + _hidl_offset, this.matchedWifi);
        _hidl_blob.putInt32(48 + _hidl_offset, this.scannedWifi);
        _hidl_blob.putInt32(52 + _hidl_offset, this.usedWifi);
        _hidl_blob.putInt32(56 + _hidl_offset, this.wlanScanage);
        _hidl_blob.putInt32(60 + _hidl_offset, this.isValid);
        _hidl_blob.putInt32(64 + _hidl_offset, this.source);
        _hidl_blob.putInt16(68 + _hidl_offset, this.flags);
    }
}
