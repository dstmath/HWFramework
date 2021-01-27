package vendor.huawei.hardware.hwfactoryinterface.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class FmdEvent {
    public String bsn = new String();
    public int cycle;
    public String description = new String();
    public String deviceName = new String();
    public int errorCode;
    public String firmware = new String();
    public int itemId;
    public String maxThreshold = new String();
    public String minThreshold = new String();
    public String result = new String();
    public String station = new String();
    public String testName = new String();
    public String time = new String();
    public String value = new String();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != FmdEvent.class) {
            return false;
        }
        FmdEvent other = (FmdEvent) otherObject;
        if (this.errorCode == other.errorCode && this.itemId == other.itemId && this.cycle == other.cycle && HidlSupport.deepEquals(this.result, other.result) && HidlSupport.deepEquals(this.station, other.station) && HidlSupport.deepEquals(this.bsn, other.bsn) && HidlSupport.deepEquals(this.time, other.time) && HidlSupport.deepEquals(this.deviceName, other.deviceName) && HidlSupport.deepEquals(this.testName, other.testName) && HidlSupport.deepEquals(this.value, other.value) && HidlSupport.deepEquals(this.minThreshold, other.minThreshold) && HidlSupport.deepEquals(this.maxThreshold, other.maxThreshold) && HidlSupport.deepEquals(this.firmware, other.firmware) && HidlSupport.deepEquals(this.description, other.description)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.errorCode))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.itemId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cycle))), Integer.valueOf(HidlSupport.deepHashCode(this.result)), Integer.valueOf(HidlSupport.deepHashCode(this.station)), Integer.valueOf(HidlSupport.deepHashCode(this.bsn)), Integer.valueOf(HidlSupport.deepHashCode(this.time)), Integer.valueOf(HidlSupport.deepHashCode(this.deviceName)), Integer.valueOf(HidlSupport.deepHashCode(this.testName)), Integer.valueOf(HidlSupport.deepHashCode(this.value)), Integer.valueOf(HidlSupport.deepHashCode(this.minThreshold)), Integer.valueOf(HidlSupport.deepHashCode(this.maxThreshold)), Integer.valueOf(HidlSupport.deepHashCode(this.firmware)), Integer.valueOf(HidlSupport.deepHashCode(this.description)));
    }

    public final String toString() {
        return "{.errorCode = " + this.errorCode + ", .itemId = " + this.itemId + ", .cycle = " + this.cycle + ", .result = " + this.result + ", .station = " + this.station + ", .bsn = " + this.bsn + ", .time = " + this.time + ", .deviceName = " + this.deviceName + ", .testName = " + this.testName + ", .value = " + this.value + ", .minThreshold = " + this.minThreshold + ", .maxThreshold = " + this.maxThreshold + ", .firmware = " + this.firmware + ", .description = " + this.description + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(192), 0);
    }

    public static final ArrayList<FmdEvent> readVectorFromParcel(HwParcel parcel) {
        ArrayList<FmdEvent> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 192), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            FmdEvent _hidl_vec_element = new FmdEvent();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 192));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.errorCode = _hidl_blob.getInt32(_hidl_offset + 0);
        this.itemId = _hidl_blob.getInt32(_hidl_offset + 4);
        this.cycle = _hidl_blob.getInt32(_hidl_offset + 8);
        this.result = _hidl_blob.getString(_hidl_offset + 16);
        parcel.readEmbeddedBuffer((long) (this.result.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, false);
        this.station = _hidl_blob.getString(_hidl_offset + 32);
        parcel.readEmbeddedBuffer((long) (this.station.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, false);
        this.bsn = _hidl_blob.getString(_hidl_offset + 48);
        parcel.readEmbeddedBuffer((long) (this.bsn.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 48 + 0, false);
        this.time = _hidl_blob.getString(_hidl_offset + 64);
        parcel.readEmbeddedBuffer((long) (this.time.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 64 + 0, false);
        this.deviceName = _hidl_blob.getString(_hidl_offset + 80);
        parcel.readEmbeddedBuffer((long) (this.deviceName.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 80 + 0, false);
        this.testName = _hidl_blob.getString(_hidl_offset + 96);
        parcel.readEmbeddedBuffer((long) (this.testName.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 96 + 0, false);
        this.value = _hidl_blob.getString(_hidl_offset + 112);
        parcel.readEmbeddedBuffer((long) (this.value.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 112 + 0, false);
        this.minThreshold = _hidl_blob.getString(_hidl_offset + 128);
        parcel.readEmbeddedBuffer((long) (this.minThreshold.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 128 + 0, false);
        this.maxThreshold = _hidl_blob.getString(_hidl_offset + 144);
        parcel.readEmbeddedBuffer((long) (this.maxThreshold.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 144 + 0, false);
        this.firmware = _hidl_blob.getString(_hidl_offset + 160);
        parcel.readEmbeddedBuffer((long) (this.firmware.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 160 + 0, false);
        this.description = _hidl_blob.getString(_hidl_offset + 176);
        parcel.readEmbeddedBuffer((long) (this.description.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 176 + 0, false);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(192);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<FmdEvent> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 192);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 192));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.errorCode);
        _hidl_blob.putInt32(4 + _hidl_offset, this.itemId);
        _hidl_blob.putInt32(8 + _hidl_offset, this.cycle);
        _hidl_blob.putString(16 + _hidl_offset, this.result);
        _hidl_blob.putString(32 + _hidl_offset, this.station);
        _hidl_blob.putString(48 + _hidl_offset, this.bsn);
        _hidl_blob.putString(64 + _hidl_offset, this.time);
        _hidl_blob.putString(80 + _hidl_offset, this.deviceName);
        _hidl_blob.putString(96 + _hidl_offset, this.testName);
        _hidl_blob.putString(112 + _hidl_offset, this.value);
        _hidl_blob.putString(128 + _hidl_offset, this.minThreshold);
        _hidl_blob.putString(144 + _hidl_offset, this.maxThreshold);
        _hidl_blob.putString(160 + _hidl_offset, this.firmware);
        _hidl_blob.putString(176 + _hidl_offset, this.description);
    }
}
