package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class RttConfig {
    public byte[] addr = new byte[6];
    public int burstDuration;
    public int burstPeriod;
    public int bw;
    public WifiChannelInfo channel = new WifiChannelInfo();
    public boolean mustRequestLci;
    public boolean mustRequestLcr;
    public int numBurst;
    public int numFramesPerBurst;
    public int numRetriesPerFtmr;
    public int numRetriesPerRttFrame;
    public int peer;
    public int preamble;
    public int type;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RttConfig.class) {
            return false;
        }
        RttConfig other = (RttConfig) otherObject;
        if (HidlSupport.deepEquals(this.addr, other.addr) && this.type == other.type && this.peer == other.peer && HidlSupport.deepEquals(this.channel, other.channel) && this.burstPeriod == other.burstPeriod && this.numBurst == other.numBurst && this.numFramesPerBurst == other.numFramesPerBurst && this.numRetriesPerRttFrame == other.numRetriesPerRttFrame && this.numRetriesPerFtmr == other.numRetriesPerFtmr && this.mustRequestLci == other.mustRequestLci && this.mustRequestLcr == other.mustRequestLcr && this.burstDuration == other.burstDuration && this.preamble == other.preamble && this.bw == other.bw) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.addr)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.peer))), Integer.valueOf(HidlSupport.deepHashCode(this.channel)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.burstPeriod))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numBurst))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numFramesPerBurst))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numRetriesPerRttFrame))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.numRetriesPerFtmr))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.mustRequestLci))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.mustRequestLcr))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.burstDuration))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.preamble))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.bw))));
    }

    public final String toString() {
        return "{.addr = " + Arrays.toString(this.addr) + ", .type = " + RttType.toString(this.type) + ", .peer = " + RttPeerType.toString(this.peer) + ", .channel = " + this.channel + ", .burstPeriod = " + this.burstPeriod + ", .numBurst = " + this.numBurst + ", .numFramesPerBurst = " + this.numFramesPerBurst + ", .numRetriesPerRttFrame = " + this.numRetriesPerRttFrame + ", .numRetriesPerFtmr = " + this.numRetriesPerFtmr + ", .mustRequestLci = " + this.mustRequestLci + ", .mustRequestLcr = " + this.mustRequestLcr + ", .burstDuration = " + this.burstDuration + ", .preamble = " + RttPreamble.toString(this.preamble) + ", .bw = " + RttBw.toString(this.bw) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(68), 0);
    }

    public static final ArrayList<RttConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RttConfig> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 68), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RttConfig _hidl_vec_element = new RttConfig();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 68));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.copyToInt8Array(0 + _hidl_offset, this.addr, 6);
        this.type = _hidl_blob.getInt32(8 + _hidl_offset);
        this.peer = _hidl_blob.getInt32(12 + _hidl_offset);
        this.channel.readEmbeddedFromParcel(parcel, _hidl_blob, 16 + _hidl_offset);
        this.burstPeriod = _hidl_blob.getInt32(32 + _hidl_offset);
        this.numBurst = _hidl_blob.getInt32(36 + _hidl_offset);
        this.numFramesPerBurst = _hidl_blob.getInt32(40 + _hidl_offset);
        this.numRetriesPerRttFrame = _hidl_blob.getInt32(44 + _hidl_offset);
        this.numRetriesPerFtmr = _hidl_blob.getInt32(48 + _hidl_offset);
        this.mustRequestLci = _hidl_blob.getBool(52 + _hidl_offset);
        this.mustRequestLcr = _hidl_blob.getBool(53 + _hidl_offset);
        this.burstDuration = _hidl_blob.getInt32(56 + _hidl_offset);
        this.preamble = _hidl_blob.getInt32(60 + _hidl_offset);
        this.bw = _hidl_blob.getInt32(64 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(68);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RttConfig> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 68);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 68));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        long _hidl_array_offset_0 = 0 + _hidl_offset;
        byte[] _hidl_array_item_0 = this.addr;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 6) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
        _hidl_blob.putInt32(8 + _hidl_offset, this.type);
        _hidl_blob.putInt32(12 + _hidl_offset, this.peer);
        this.channel.writeEmbeddedToBlob(_hidl_blob, 16 + _hidl_offset);
        _hidl_blob.putInt32(32 + _hidl_offset, this.burstPeriod);
        _hidl_blob.putInt32(36 + _hidl_offset, this.numBurst);
        _hidl_blob.putInt32(40 + _hidl_offset, this.numFramesPerBurst);
        _hidl_blob.putInt32(44 + _hidl_offset, this.numRetriesPerRttFrame);
        _hidl_blob.putInt32(48 + _hidl_offset, this.numRetriesPerFtmr);
        _hidl_blob.putBool(52 + _hidl_offset, this.mustRequestLci);
        _hidl_blob.putBool(53 + _hidl_offset, this.mustRequestLcr);
        _hidl_blob.putInt32(56 + _hidl_offset, this.burstDuration);
        _hidl_blob.putInt32(60 + _hidl_offset, this.preamble);
        _hidl_blob.putInt32(64 + _hidl_offset, this.bw);
    }
}
