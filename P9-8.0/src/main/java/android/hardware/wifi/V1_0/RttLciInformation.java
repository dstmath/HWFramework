package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RttLciInformation {
    public int altitude;
    public byte altitudeUnc;
    public int floor;
    public int heightAboveFloor;
    public int heightUnc;
    public long latitude;
    public byte latitudeUnc;
    public long longitude;
    public byte longitudeUnc;
    public int motionPattern;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RttLciInformation.class) {
            return false;
        }
        RttLciInformation other = (RttLciInformation) otherObject;
        return this.latitude == other.latitude && this.longitude == other.longitude && this.altitude == other.altitude && this.latitudeUnc == other.latitudeUnc && this.longitudeUnc == other.longitudeUnc && this.altitudeUnc == other.altitudeUnc && this.motionPattern == other.motionPattern && this.floor == other.floor && this.heightAboveFloor == other.heightAboveFloor && this.heightUnc == other.heightUnc;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.latitude))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.longitude))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.altitude))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.latitudeUnc))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.longitudeUnc))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.altitudeUnc))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.motionPattern))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.floor))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.heightAboveFloor))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.heightUnc)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".latitude = ");
        builder.append(this.latitude);
        builder.append(", .longitude = ");
        builder.append(this.longitude);
        builder.append(", .altitude = ");
        builder.append(this.altitude);
        builder.append(", .latitudeUnc = ");
        builder.append(this.latitudeUnc);
        builder.append(", .longitudeUnc = ");
        builder.append(this.longitudeUnc);
        builder.append(", .altitudeUnc = ");
        builder.append(this.altitudeUnc);
        builder.append(", .motionPattern = ");
        builder.append(RttMotionPattern.toString(this.motionPattern));
        builder.append(", .floor = ");
        builder.append(this.floor);
        builder.append(", .heightAboveFloor = ");
        builder.append(this.heightAboveFloor);
        builder.append(", .heightUnc = ");
        builder.append(this.heightUnc);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<RttLciInformation> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RttLciInformation> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RttLciInformation _hidl_vec_element = new RttLciInformation();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.latitude = _hidl_blob.getInt64(0 + _hidl_offset);
        this.longitude = _hidl_blob.getInt64(8 + _hidl_offset);
        this.altitude = _hidl_blob.getInt32(16 + _hidl_offset);
        this.latitudeUnc = _hidl_blob.getInt8(20 + _hidl_offset);
        this.longitudeUnc = _hidl_blob.getInt8(21 + _hidl_offset);
        this.altitudeUnc = _hidl_blob.getInt8(22 + _hidl_offset);
        this.motionPattern = _hidl_blob.getInt32(24 + _hidl_offset);
        this.floor = _hidl_blob.getInt32(28 + _hidl_offset);
        this.heightAboveFloor = _hidl_blob.getInt32(32 + _hidl_offset);
        this.heightUnc = _hidl_blob.getInt32(36 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RttLciInformation> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RttLciInformation) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt64(0 + _hidl_offset, this.latitude);
        _hidl_blob.putInt64(8 + _hidl_offset, this.longitude);
        _hidl_blob.putInt32(16 + _hidl_offset, this.altitude);
        _hidl_blob.putInt8(20 + _hidl_offset, this.latitudeUnc);
        _hidl_blob.putInt8(21 + _hidl_offset, this.longitudeUnc);
        _hidl_blob.putInt8(22 + _hidl_offset, this.altitudeUnc);
        _hidl_blob.putInt32(24 + _hidl_offset, this.motionPattern);
        _hidl_blob.putInt32(28 + _hidl_offset, this.floor);
        _hidl_blob.putInt32(32 + _hidl_offset, this.heightAboveFloor);
        _hidl_blob.putInt32(36 + _hidl_offset, this.heightUnc);
    }
}
