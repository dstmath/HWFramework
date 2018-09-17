package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StaLinkLayerIfaceStats {
    public int avgRssiMgmt;
    public int beaconRx;
    public final StaLinkLayerIfacePacketStats wmeBePktStats = new StaLinkLayerIfacePacketStats();
    public final StaLinkLayerIfacePacketStats wmeBkPktStats = new StaLinkLayerIfacePacketStats();
    public final StaLinkLayerIfacePacketStats wmeViPktStats = new StaLinkLayerIfacePacketStats();
    public final StaLinkLayerIfacePacketStats wmeVoPktStats = new StaLinkLayerIfacePacketStats();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StaLinkLayerIfaceStats.class) {
            return false;
        }
        StaLinkLayerIfaceStats other = (StaLinkLayerIfaceStats) otherObject;
        return this.beaconRx == other.beaconRx && this.avgRssiMgmt == other.avgRssiMgmt && HidlSupport.deepEquals(this.wmeBePktStats, other.wmeBePktStats) && HidlSupport.deepEquals(this.wmeBkPktStats, other.wmeBkPktStats) && HidlSupport.deepEquals(this.wmeViPktStats, other.wmeViPktStats) && HidlSupport.deepEquals(this.wmeVoPktStats, other.wmeVoPktStats);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.beaconRx))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.avgRssiMgmt))), Integer.valueOf(HidlSupport.deepHashCode(this.wmeBePktStats)), Integer.valueOf(HidlSupport.deepHashCode(this.wmeBkPktStats)), Integer.valueOf(HidlSupport.deepHashCode(this.wmeViPktStats)), Integer.valueOf(HidlSupport.deepHashCode(this.wmeVoPktStats))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".beaconRx = ");
        builder.append(this.beaconRx);
        builder.append(", .avgRssiMgmt = ");
        builder.append(this.avgRssiMgmt);
        builder.append(", .wmeBePktStats = ");
        builder.append(this.wmeBePktStats);
        builder.append(", .wmeBkPktStats = ");
        builder.append(this.wmeBkPktStats);
        builder.append(", .wmeViPktStats = ");
        builder.append(this.wmeViPktStats);
        builder.append(", .wmeVoPktStats = ");
        builder.append(this.wmeVoPktStats);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(136), 0);
    }

    public static final ArrayList<StaLinkLayerIfaceStats> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StaLinkLayerIfaceStats> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 136), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StaLinkLayerIfaceStats _hidl_vec_element = new StaLinkLayerIfaceStats();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 136));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.beaconRx = _hidl_blob.getInt32(0 + _hidl_offset);
        this.avgRssiMgmt = _hidl_blob.getInt32(4 + _hidl_offset);
        this.wmeBePktStats.readEmbeddedFromParcel(parcel, _hidl_blob, 8 + _hidl_offset);
        this.wmeBkPktStats.readEmbeddedFromParcel(parcel, _hidl_blob, 40 + _hidl_offset);
        this.wmeViPktStats.readEmbeddedFromParcel(parcel, _hidl_blob, 72 + _hidl_offset);
        this.wmeVoPktStats.readEmbeddedFromParcel(parcel, _hidl_blob, 104 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(136);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<StaLinkLayerIfaceStats> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 136);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((StaLinkLayerIfaceStats) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 136));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.beaconRx);
        _hidl_blob.putInt32(4 + _hidl_offset, this.avgRssiMgmt);
        this.wmeBePktStats.writeEmbeddedToBlob(_hidl_blob, 8 + _hidl_offset);
        this.wmeBkPktStats.writeEmbeddedToBlob(_hidl_blob, 40 + _hidl_offset);
        this.wmeViPktStats.writeEmbeddedToBlob(_hidl_blob, 72 + _hidl_offset);
        this.wmeVoPktStats.writeEmbeddedToBlob(_hidl_blob, 104 + _hidl_offset);
    }
}
