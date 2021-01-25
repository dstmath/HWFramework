package android.hardware.gnss.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class GnssLocation {
    public double altitudeMeters;
    public float bearingAccuracyDegrees;
    public float bearingDegrees;
    public short gnssLocationFlags;
    public float horizontalAccuracyMeters;
    public double latitudeDegrees;
    public double longitudeDegrees;
    public float speedAccuracyMetersPerSecond;
    public float speedMetersPerSec;
    public long timestamp;
    public float verticalAccuracyMeters;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != GnssLocation.class) {
            return false;
        }
        GnssLocation other = (GnssLocation) otherObject;
        if (HidlSupport.deepEquals(Short.valueOf(this.gnssLocationFlags), Short.valueOf(other.gnssLocationFlags)) && this.latitudeDegrees == other.latitudeDegrees && this.longitudeDegrees == other.longitudeDegrees && this.altitudeMeters == other.altitudeMeters && this.speedMetersPerSec == other.speedMetersPerSec && this.bearingDegrees == other.bearingDegrees && this.horizontalAccuracyMeters == other.horizontalAccuracyMeters && this.verticalAccuracyMeters == other.verticalAccuracyMeters && this.speedAccuracyMetersPerSecond == other.speedAccuracyMetersPerSecond && this.bearingAccuracyDegrees == other.bearingAccuracyDegrees && this.timestamp == other.timestamp) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.gnssLocationFlags))), Integer.valueOf(HidlSupport.deepHashCode(Double.valueOf(this.latitudeDegrees))), Integer.valueOf(HidlSupport.deepHashCode(Double.valueOf(this.longitudeDegrees))), Integer.valueOf(HidlSupport.deepHashCode(Double.valueOf(this.altitudeMeters))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.speedMetersPerSec))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.bearingDegrees))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.horizontalAccuracyMeters))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.verticalAccuracyMeters))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.speedAccuracyMetersPerSecond))), Integer.valueOf(HidlSupport.deepHashCode(Float.valueOf(this.bearingAccuracyDegrees))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.timestamp))));
    }

    public final String toString() {
        return "{.gnssLocationFlags = " + GnssLocationFlags.dumpBitfield(this.gnssLocationFlags) + ", .latitudeDegrees = " + this.latitudeDegrees + ", .longitudeDegrees = " + this.longitudeDegrees + ", .altitudeMeters = " + this.altitudeMeters + ", .speedMetersPerSec = " + this.speedMetersPerSec + ", .bearingDegrees = " + this.bearingDegrees + ", .horizontalAccuracyMeters = " + this.horizontalAccuracyMeters + ", .verticalAccuracyMeters = " + this.verticalAccuracyMeters + ", .speedAccuracyMetersPerSecond = " + this.speedAccuracyMetersPerSecond + ", .bearingAccuracyDegrees = " + this.bearingAccuracyDegrees + ", .timestamp = " + this.timestamp + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(64), 0);
    }

    public static final ArrayList<GnssLocation> readVectorFromParcel(HwParcel parcel) {
        ArrayList<GnssLocation> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            GnssLocation _hidl_vec_element = new GnssLocation();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.gnssLocationFlags = _hidl_blob.getInt16(0 + _hidl_offset);
        this.latitudeDegrees = _hidl_blob.getDouble(8 + _hidl_offset);
        this.longitudeDegrees = _hidl_blob.getDouble(16 + _hidl_offset);
        this.altitudeMeters = _hidl_blob.getDouble(24 + _hidl_offset);
        this.speedMetersPerSec = _hidl_blob.getFloat(32 + _hidl_offset);
        this.bearingDegrees = _hidl_blob.getFloat(36 + _hidl_offset);
        this.horizontalAccuracyMeters = _hidl_blob.getFloat(40 + _hidl_offset);
        this.verticalAccuracyMeters = _hidl_blob.getFloat(44 + _hidl_offset);
        this.speedAccuracyMetersPerSecond = _hidl_blob.getFloat(48 + _hidl_offset);
        this.bearingAccuracyDegrees = _hidl_blob.getFloat(52 + _hidl_offset);
        this.timestamp = _hidl_blob.getInt64(56 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(64);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<GnssLocation> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt16(0 + _hidl_offset, this.gnssLocationFlags);
        _hidl_blob.putDouble(8 + _hidl_offset, this.latitudeDegrees);
        _hidl_blob.putDouble(16 + _hidl_offset, this.longitudeDegrees);
        _hidl_blob.putDouble(24 + _hidl_offset, this.altitudeMeters);
        _hidl_blob.putFloat(32 + _hidl_offset, this.speedMetersPerSec);
        _hidl_blob.putFloat(36 + _hidl_offset, this.bearingDegrees);
        _hidl_blob.putFloat(40 + _hidl_offset, this.horizontalAccuracyMeters);
        _hidl_blob.putFloat(44 + _hidl_offset, this.verticalAccuracyMeters);
        _hidl_blob.putFloat(48 + _hidl_offset, this.speedAccuracyMetersPerSecond);
        _hidl_blob.putFloat(52 + _hidl_offset, this.bearingAccuracyDegrees);
        _hidl_blob.putInt64(56 + _hidl_offset, this.timestamp);
    }
}
