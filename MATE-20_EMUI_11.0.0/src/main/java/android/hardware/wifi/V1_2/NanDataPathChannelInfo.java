package android.hardware.wifi.V1_2;

import android.hardware.wifi.V1_0.WifiChannelWidthInMhz;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NanDataPathChannelInfo {
    public int channelBandwidth;
    public int channelFreq;
    public int numSpatialStreams;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanDataPathChannelInfo.class) {
            return false;
        }
        NanDataPathChannelInfo other = (NanDataPathChannelInfo) otherObject;
        if (this.channelFreq == other.channelFreq && this.channelBandwidth == other.channelBandwidth && this.numSpatialStreams == other.numSpatialStreams) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channelFreq))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channelBandwidth))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numSpatialStreams))));
    }

    public final String toString() {
        return "{.channelFreq = " + this.channelFreq + ", .channelBandwidth = " + WifiChannelWidthInMhz.toString(this.channelBandwidth) + ", .numSpatialStreams = " + this.numSpatialStreams + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(12), 0);
    }

    public static final ArrayList<NanDataPathChannelInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanDataPathChannelInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanDataPathChannelInfo _hidl_vec_element = new NanDataPathChannelInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 12));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.channelFreq = _hidl_blob.getInt32(0 + _hidl_offset);
        this.channelBandwidth = _hidl_blob.getInt32(4 + _hidl_offset);
        this.numSpatialStreams = _hidl_blob.getInt32(8 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(12);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanDataPathChannelInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 12));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.channelFreq);
        _hidl_blob.putInt32(4 + _hidl_offset, this.channelBandwidth);
        _hidl_blob.putInt32(8 + _hidl_offset, this.numSpatialStreams);
    }
}
