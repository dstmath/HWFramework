package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StaLinkLayerIfacePacketStats {
    public long lostMpdu;
    public long retries;
    public long rxMpdu;
    public long txMpdu;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StaLinkLayerIfacePacketStats.class) {
            return false;
        }
        StaLinkLayerIfacePacketStats other = (StaLinkLayerIfacePacketStats) otherObject;
        return this.rxMpdu == other.rxMpdu && this.txMpdu == other.txMpdu && this.lostMpdu == other.lostMpdu && this.retries == other.retries;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.rxMpdu))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.txMpdu))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.lostMpdu))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.retries)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".rxMpdu = ");
        builder.append(this.rxMpdu);
        builder.append(", .txMpdu = ");
        builder.append(this.txMpdu);
        builder.append(", .lostMpdu = ");
        builder.append(this.lostMpdu);
        builder.append(", .retries = ");
        builder.append(this.retries);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<StaLinkLayerIfacePacketStats> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StaLinkLayerIfacePacketStats> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StaLinkLayerIfacePacketStats _hidl_vec_element = new StaLinkLayerIfacePacketStats();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.rxMpdu = _hidl_blob.getInt64(0 + _hidl_offset);
        this.txMpdu = _hidl_blob.getInt64(8 + _hidl_offset);
        this.lostMpdu = _hidl_blob.getInt64(16 + _hidl_offset);
        this.retries = _hidl_blob.getInt64(24 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<StaLinkLayerIfacePacketStats> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((StaLinkLayerIfacePacketStats) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt64(0 + _hidl_offset, this.rxMpdu);
        _hidl_blob.putInt64(8 + _hidl_offset, this.txMpdu);
        _hidl_blob.putInt64(16 + _hidl_offset, this.lostMpdu);
        _hidl_blob.putInt64(24 + _hidl_offset, this.retries);
    }
}
