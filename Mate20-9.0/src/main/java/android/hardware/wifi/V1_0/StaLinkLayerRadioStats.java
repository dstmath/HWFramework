package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StaLinkLayerRadioStats {
    public int onTimeInMs;
    public int onTimeInMsForScan;
    public int rxTimeInMs;
    public int txTimeInMs;
    public final ArrayList<Integer> txTimeInMsPerLevel = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StaLinkLayerRadioStats.class) {
            return false;
        }
        StaLinkLayerRadioStats other = (StaLinkLayerRadioStats) otherObject;
        if (this.onTimeInMs == other.onTimeInMs && this.txTimeInMs == other.txTimeInMs && HidlSupport.deepEquals(this.txTimeInMsPerLevel, other.txTimeInMsPerLevel) && this.rxTimeInMs == other.rxTimeInMs && this.onTimeInMsForScan == other.onTimeInMsForScan) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.onTimeInMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.txTimeInMs))), Integer.valueOf(HidlSupport.deepHashCode(this.txTimeInMsPerLevel)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rxTimeInMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.onTimeInMsForScan)))});
    }

    public final String toString() {
        return "{" + ".onTimeInMs = " + this.onTimeInMs + ", .txTimeInMs = " + this.txTimeInMs + ", .txTimeInMsPerLevel = " + this.txTimeInMsPerLevel + ", .rxTimeInMs = " + this.rxTimeInMs + ", .onTimeInMsForScan = " + this.onTimeInMsForScan + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<StaLinkLayerRadioStats> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StaLinkLayerRadioStats> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StaLinkLayerRadioStats _hidl_vec_element = new StaLinkLayerRadioStats();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.onTimeInMs = hwBlob.getInt32(_hidl_offset + 0);
        this.txTimeInMs = hwBlob.getInt32(_hidl_offset + 4);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.txTimeInMsPerLevel.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.txTimeInMsPerLevel.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
        }
        this.rxTimeInMs = hwBlob.getInt32(_hidl_offset + 24);
        this.onTimeInMsForScan = hwBlob.getInt32(_hidl_offset + 28);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<StaLinkLayerRadioStats> _hidl_vec) {
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
        _hidl_blob.putInt32(_hidl_offset + 0, this.onTimeInMs);
        _hidl_blob.putInt32(4 + _hidl_offset, this.txTimeInMs);
        int _hidl_vec_size = this.txTimeInMsPerLevel.size();
        _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        while (true) {
            int _hidl_index_02 = _hidl_index_0;
            if (_hidl_index_02 < _hidl_vec_size) {
                childBlob.putInt32((long) (_hidl_index_02 * 4), this.txTimeInMsPerLevel.get(_hidl_index_02).intValue());
                _hidl_index_0 = _hidl_index_02 + 1;
            } else {
                _hidl_blob.putBlob(8 + _hidl_offset + 0, childBlob);
                _hidl_blob.putInt32(24 + _hidl_offset, this.rxTimeInMs);
                _hidl_blob.putInt32(28 + _hidl_offset, this.onTimeInMsForScan);
                return;
            }
        }
    }
}
