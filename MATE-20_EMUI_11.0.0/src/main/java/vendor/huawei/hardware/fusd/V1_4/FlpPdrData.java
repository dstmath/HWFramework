package vendor.huawei.hardware.fusd.V1_4;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class FlpPdrData {
    public short absoluteBearing;
    public int count;
    public int migrationDistance;
    public int relativeAltitude;
    public int relativePosX;
    public int relativePosY;
    public short reliabilityFlag;
    public long timestamp;
    public short velocityX;
    public short velocityY;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != FlpPdrData.class) {
            return false;
        }
        FlpPdrData other = (FlpPdrData) otherObject;
        if (this.timestamp == other.timestamp && this.count == other.count && this.relativePosX == other.relativePosX && this.relativePosY == other.relativePosY && this.velocityX == other.velocityX && this.velocityY == other.velocityY && this.migrationDistance == other.migrationDistance && this.relativeAltitude == other.relativeAltitude && this.absoluteBearing == other.absoluteBearing && this.reliabilityFlag == other.reliabilityFlag) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.timestamp))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.count))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.relativePosX))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.relativePosY))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.velocityX))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.velocityY))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.migrationDistance))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.relativeAltitude))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.absoluteBearing))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.reliabilityFlag))));
    }

    public final String toString() {
        return "{.timestamp = " + this.timestamp + ", .count = " + this.count + ", .relativePosX = " + this.relativePosX + ", .relativePosY = " + this.relativePosY + ", .velocityX = " + ((int) this.velocityX) + ", .velocityY = " + ((int) this.velocityY) + ", .migrationDistance = " + this.migrationDistance + ", .relativeAltitude = " + this.relativeAltitude + ", .absoluteBearing = " + ((int) this.absoluteBearing) + ", .reliabilityFlag = " + ((int) this.reliabilityFlag) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<FlpPdrData> readVectorFromParcel(HwParcel parcel) {
        ArrayList<FlpPdrData> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            FlpPdrData _hidl_vec_element = new FlpPdrData();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.timestamp = _hidl_blob.getInt64(0 + _hidl_offset);
        this.count = _hidl_blob.getInt32(8 + _hidl_offset);
        this.relativePosX = _hidl_blob.getInt32(12 + _hidl_offset);
        this.relativePosY = _hidl_blob.getInt32(16 + _hidl_offset);
        this.velocityX = _hidl_blob.getInt16(20 + _hidl_offset);
        this.velocityY = _hidl_blob.getInt16(22 + _hidl_offset);
        this.migrationDistance = _hidl_blob.getInt32(24 + _hidl_offset);
        this.relativeAltitude = _hidl_blob.getInt32(28 + _hidl_offset);
        this.absoluteBearing = _hidl_blob.getInt16(32 + _hidl_offset);
        this.reliabilityFlag = _hidl_blob.getInt16(34 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<FlpPdrData> _hidl_vec) {
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
        _hidl_blob.putInt64(0 + _hidl_offset, this.timestamp);
        _hidl_blob.putInt32(8 + _hidl_offset, this.count);
        _hidl_blob.putInt32(12 + _hidl_offset, this.relativePosX);
        _hidl_blob.putInt32(16 + _hidl_offset, this.relativePosY);
        _hidl_blob.putInt16(20 + _hidl_offset, this.velocityX);
        _hidl_blob.putInt16(22 + _hidl_offset, this.velocityY);
        _hidl_blob.putInt32(24 + _hidl_offset, this.migrationDistance);
        _hidl_blob.putInt32(28 + _hidl_offset, this.relativeAltitude);
        _hidl_blob.putInt16(32 + _hidl_offset, this.absoluteBearing);
        _hidl_blob.putInt16(34 + _hidl_offset, this.reliabilityFlag);
    }
}
