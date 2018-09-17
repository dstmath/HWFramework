package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanEnableRequest {
    public final NanConfigRequest configParams = new NanConfigRequest();
    public final NanDebugConfig debugConfigs = new NanDebugConfig();
    public byte hopCountMax;
    public final boolean[] operateInBand = new boolean[2];

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanEnableRequest.class) {
            return false;
        }
        NanEnableRequest other = (NanEnableRequest) otherObject;
        return HidlSupport.deepEquals(this.operateInBand, other.operateInBand) && this.hopCountMax == other.hopCountMax && HidlSupport.deepEquals(this.configParams, other.configParams) && HidlSupport.deepEquals(this.debugConfigs, other.debugConfigs);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.operateInBand)), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.hopCountMax))), Integer.valueOf(HidlSupport.deepHashCode(this.configParams)), Integer.valueOf(HidlSupport.deepHashCode(this.debugConfigs))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".operateInBand = ");
        builder.append(Arrays.toString(this.operateInBand));
        builder.append(", .hopCountMax = ");
        builder.append(this.hopCountMax);
        builder.append(", .configParams = ");
        builder.append(this.configParams);
        builder.append(", .debugConfigs = ");
        builder.append(this.debugConfigs);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(80), 0);
    }

    public static final ArrayList<NanEnableRequest> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanEnableRequest> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 80), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanEnableRequest _hidl_vec_element = new NanEnableRequest();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 80));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        long _hidl_array_offset_0 = _hidl_offset + 0;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            this.operateInBand[_hidl_index_0_0] = _hidl_blob.getBool(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        this.hopCountMax = _hidl_blob.getInt8(2 + _hidl_offset);
        this.configParams.readEmbeddedFromParcel(parcel, _hidl_blob, 4 + _hidl_offset);
        this.debugConfigs.readEmbeddedFromParcel(parcel, _hidl_blob, 36 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(80);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanEnableRequest> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 80);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((NanEnableRequest) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 80));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        long _hidl_array_offset_0 = _hidl_offset + 0;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            _hidl_blob.putBool(_hidl_array_offset_0, this.operateInBand[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_blob.putInt8(2 + _hidl_offset, this.hopCountMax);
        this.configParams.writeEmbeddedToBlob(_hidl_blob, 4 + _hidl_offset);
        this.debugConfigs.writeEmbeddedToBlob(_hidl_blob, 36 + _hidl_offset);
    }
}
