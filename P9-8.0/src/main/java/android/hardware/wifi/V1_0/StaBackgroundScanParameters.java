package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StaBackgroundScanParameters {
    public int basePeriodInMs;
    public final ArrayList<StaBackgroundScanBucketParameters> buckets = new ArrayList();
    public int maxApPerScan;
    public int reportThresholdNumScans;
    public int reportThresholdPercent;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StaBackgroundScanParameters.class) {
            return false;
        }
        StaBackgroundScanParameters other = (StaBackgroundScanParameters) otherObject;
        return this.basePeriodInMs == other.basePeriodInMs && this.maxApPerScan == other.maxApPerScan && this.reportThresholdPercent == other.reportThresholdPercent && this.reportThresholdNumScans == other.reportThresholdNumScans && HidlSupport.deepEquals(this.buckets, other.buckets);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.basePeriodInMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxApPerScan))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reportThresholdPercent))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.reportThresholdNumScans))), Integer.valueOf(HidlSupport.deepHashCode(this.buckets))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".basePeriodInMs = ");
        builder.append(this.basePeriodInMs);
        builder.append(", .maxApPerScan = ");
        builder.append(this.maxApPerScan);
        builder.append(", .reportThresholdPercent = ");
        builder.append(this.reportThresholdPercent);
        builder.append(", .reportThresholdNumScans = ");
        builder.append(this.reportThresholdNumScans);
        builder.append(", .buckets = ");
        builder.append(this.buckets);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<StaBackgroundScanParameters> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StaBackgroundScanParameters> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StaBackgroundScanParameters _hidl_vec_element = new StaBackgroundScanParameters();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.basePeriodInMs = _hidl_blob.getInt32(0 + _hidl_offset);
        this.maxApPerScan = _hidl_blob.getInt32(4 + _hidl_offset);
        this.reportThresholdPercent = _hidl_blob.getInt32(8 + _hidl_offset);
        this.reportThresholdNumScans = _hidl_blob.getInt32(12 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((16 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), (16 + _hidl_offset) + 0, true);
        this.buckets.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StaBackgroundScanBucketParameters _hidl_vec_element = new StaBackgroundScanBucketParameters();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            this.buckets.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<StaBackgroundScanParameters> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((StaBackgroundScanParameters) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.basePeriodInMs);
        _hidl_blob.putInt32(4 + _hidl_offset, this.maxApPerScan);
        _hidl_blob.putInt32(8 + _hidl_offset, this.reportThresholdPercent);
        _hidl_blob.putInt32(12 + _hidl_offset, this.reportThresholdNumScans);
        int _hidl_vec_size = this.buckets.size();
        _hidl_blob.putInt32((16 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((16 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((StaBackgroundScanBucketParameters) this.buckets.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob((16 + _hidl_offset) + 0, childBlob);
    }
}
