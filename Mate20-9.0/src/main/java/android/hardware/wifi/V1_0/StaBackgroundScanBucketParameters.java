package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StaBackgroundScanBucketParameters {
    public int band;
    public int bucketIdx;
    public int eventReportScheme;
    public int exponentialBase;
    public int exponentialMaxPeriodInMs;
    public int exponentialStepCount;
    public final ArrayList<Integer> frequencies = new ArrayList<>();
    public int periodInMs;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StaBackgroundScanBucketParameters.class) {
            return false;
        }
        StaBackgroundScanBucketParameters other = (StaBackgroundScanBucketParameters) otherObject;
        if (this.bucketIdx == other.bucketIdx && this.band == other.band && HidlSupport.deepEquals(this.frequencies, other.frequencies) && this.periodInMs == other.periodInMs && HidlSupport.deepEquals(Integer.valueOf(this.eventReportScheme), Integer.valueOf(other.eventReportScheme)) && this.exponentialMaxPeriodInMs == other.exponentialMaxPeriodInMs && this.exponentialBase == other.exponentialBase && this.exponentialStepCount == other.exponentialStepCount) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bucketIdx))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.band))), Integer.valueOf(HidlSupport.deepHashCode(this.frequencies)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.periodInMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.eventReportScheme))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.exponentialMaxPeriodInMs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.exponentialBase))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.exponentialStepCount)))});
    }

    public final String toString() {
        return "{" + ".bucketIdx = " + this.bucketIdx + ", .band = " + WifiBand.toString(this.band) + ", .frequencies = " + this.frequencies + ", .periodInMs = " + this.periodInMs + ", .eventReportScheme = " + StaBackgroundScanBucketEventReportSchemeMask.dumpBitfield(this.eventReportScheme) + ", .exponentialMaxPeriodInMs = " + this.exponentialMaxPeriodInMs + ", .exponentialBase = " + this.exponentialBase + ", .exponentialStepCount = " + this.exponentialStepCount + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<StaBackgroundScanBucketParameters> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StaBackgroundScanBucketParameters> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StaBackgroundScanBucketParameters _hidl_vec_element = new StaBackgroundScanBucketParameters();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.bucketIdx = hwBlob.getInt32(_hidl_offset + 0);
        this.band = hwBlob.getInt32(_hidl_offset + 4);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.frequencies.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.frequencies.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
        }
        this.periodInMs = hwBlob.getInt32(_hidl_offset + 24);
        this.eventReportScheme = hwBlob.getInt32(_hidl_offset + 28);
        this.exponentialMaxPeriodInMs = hwBlob.getInt32(_hidl_offset + 32);
        this.exponentialBase = hwBlob.getInt32(_hidl_offset + 36);
        this.exponentialStepCount = hwBlob.getInt32(_hidl_offset + 40);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<StaBackgroundScanBucketParameters> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.bucketIdx);
        _hidl_blob.putInt32(4 + _hidl_offset, this.band);
        int _hidl_vec_size = this.frequencies.size();
        _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        while (true) {
            int _hidl_index_02 = _hidl_index_0;
            if (_hidl_index_02 < _hidl_vec_size) {
                childBlob.putInt32((long) (_hidl_index_02 * 4), this.frequencies.get(_hidl_index_02).intValue());
                _hidl_index_0 = _hidl_index_02 + 1;
            } else {
                _hidl_blob.putBlob(8 + _hidl_offset + 0, childBlob);
                _hidl_blob.putInt32(24 + _hidl_offset, this.periodInMs);
                _hidl_blob.putInt32(28 + _hidl_offset, this.eventReportScheme);
                _hidl_blob.putInt32(32 + _hidl_offset, this.exponentialMaxPeriodInMs);
                _hidl_blob.putInt32(36 + _hidl_offset, this.exponentialBase);
                _hidl_blob.putInt32(40 + _hidl_offset, this.exponentialStepCount);
                return;
            }
        }
    }
}
