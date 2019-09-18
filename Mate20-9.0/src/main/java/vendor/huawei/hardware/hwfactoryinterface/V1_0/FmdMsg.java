package vendor.huawei.hardware.hwfactoryinterface.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class FmdMsg {
    public int dataSource;
    public final ArrayList<FmdEvent> events = new ArrayList<>();
    public int numEvents;
    public int version;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != FmdMsg.class) {
            return false;
        }
        FmdMsg other = (FmdMsg) otherObject;
        if (this.version == other.version && this.dataSource == other.dataSource && this.numEvents == other.numEvents && HidlSupport.deepEquals(this.events, other.events)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.version))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.dataSource))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numEvents))), Integer.valueOf(HidlSupport.deepHashCode(this.events))});
    }

    public final String toString() {
        return "{" + ".version = " + this.version + ", .dataSource = " + this.dataSource + ", .numEvents = " + this.numEvents + ", .events = " + this.events + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<FmdMsg> readVectorFromParcel(HwParcel parcel) {
        ArrayList<FmdMsg> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            FmdMsg _hidl_vec_element = new FmdMsg();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.version = hwBlob.getInt32(_hidl_offset + 0);
        this.dataSource = hwBlob.getInt32(_hidl_offset + 4);
        this.numEvents = hwBlob.getInt32(_hidl_offset + 8);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 192), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.events.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            FmdEvent _hidl_vec_element = new FmdEvent();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 192));
            this.events.add(_hidl_vec_element);
        }
        HwParcel hwParcel = parcel;
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<FmdMsg> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.version);
        _hidl_blob.putInt32(4 + _hidl_offset, this.dataSource);
        _hidl_blob.putInt32(_hidl_offset + 8, this.numEvents);
        int _hidl_vec_size = this.events.size();
        _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 192);
        while (true) {
            int _hidl_index_02 = _hidl_index_0;
            if (_hidl_index_02 < _hidl_vec_size) {
                this.events.get(_hidl_index_02).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_02 * 192));
                _hidl_index_0 = _hidl_index_02 + 1;
            } else {
                _hidl_blob.putBlob(16 + _hidl_offset + 0, childBlob);
                return;
            }
        }
    }
}
