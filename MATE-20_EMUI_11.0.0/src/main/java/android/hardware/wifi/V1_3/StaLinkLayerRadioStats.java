package android.hardware.wifi.V1_3;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class StaLinkLayerRadioStats {
    public android.hardware.wifi.V1_0.StaLinkLayerRadioStats V1_0 = new android.hardware.wifi.V1_0.StaLinkLayerRadioStats();
    public ArrayList<WifiChannelStats> channelStats = new ArrayList<>();
    public int onTimeInMsForBgScan;
    public int onTimeInMsForHs20Scan;
    public int onTimeInMsForNanScan;
    public int onTimeInMsForPnoScan;
    public int onTimeInMsForRoamScan;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != StaLinkLayerRadioStats.class) {
            return false;
        }
        StaLinkLayerRadioStats other = (StaLinkLayerRadioStats) otherObject;
        if (HidlSupport.deepEquals(this.V1_0, other.V1_0) && this.onTimeInMsForNanScan == other.onTimeInMsForNanScan && this.onTimeInMsForBgScan == other.onTimeInMsForBgScan && this.onTimeInMsForRoamScan == other.onTimeInMsForRoamScan && this.onTimeInMsForPnoScan == other.onTimeInMsForPnoScan && this.onTimeInMsForHs20Scan == other.onTimeInMsForHs20Scan && HidlSupport.deepEquals(this.channelStats, other.channelStats)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.V1_0)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.onTimeInMsForNanScan))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.onTimeInMsForBgScan))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.onTimeInMsForRoamScan))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.onTimeInMsForPnoScan))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.onTimeInMsForHs20Scan))), Integer.valueOf(HidlSupport.deepHashCode(this.channelStats)));
    }

    public final String toString() {
        return "{.V1_0 = " + this.V1_0 + ", .onTimeInMsForNanScan = " + this.onTimeInMsForNanScan + ", .onTimeInMsForBgScan = " + this.onTimeInMsForBgScan + ", .onTimeInMsForRoamScan = " + this.onTimeInMsForRoamScan + ", .onTimeInMsForPnoScan = " + this.onTimeInMsForPnoScan + ", .onTimeInMsForHs20Scan = " + this.onTimeInMsForHs20Scan + ", .channelStats = " + this.channelStats + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(72), 0);
    }

    public static final ArrayList<StaLinkLayerRadioStats> readVectorFromParcel(HwParcel parcel) {
        ArrayList<StaLinkLayerRadioStats> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 72), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            StaLinkLayerRadioStats _hidl_vec_element = new StaLinkLayerRadioStats();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 72));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.V1_0.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 0);
        this.onTimeInMsForNanScan = _hidl_blob.getInt32(_hidl_offset + 32);
        this.onTimeInMsForBgScan = _hidl_blob.getInt32(_hidl_offset + 36);
        this.onTimeInMsForRoamScan = _hidl_blob.getInt32(_hidl_offset + 40);
        this.onTimeInMsForPnoScan = _hidl_blob.getInt32(_hidl_offset + 44);
        this.onTimeInMsForHs20Scan = _hidl_blob.getInt32(_hidl_offset + 48);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 56 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 24), _hidl_blob.handle(), _hidl_offset + 56 + 0, true);
        this.channelStats.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiChannelStats _hidl_vec_element = new WifiChannelStats();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 24));
            this.channelStats.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(72);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<StaLinkLayerRadioStats> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 72);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 72));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.V1_0.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 0);
        _hidl_blob.putInt32(32 + _hidl_offset, this.onTimeInMsForNanScan);
        _hidl_blob.putInt32(36 + _hidl_offset, this.onTimeInMsForBgScan);
        _hidl_blob.putInt32(40 + _hidl_offset, this.onTimeInMsForRoamScan);
        _hidl_blob.putInt32(44 + _hidl_offset, this.onTimeInMsForPnoScan);
        _hidl_blob.putInt32(48 + _hidl_offset, this.onTimeInMsForHs20Scan);
        int _hidl_vec_size = this.channelStats.size();
        _hidl_blob.putInt32(_hidl_offset + 56 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 56 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 24);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.channelStats.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 24));
        }
        _hidl_blob.putBlob(56 + _hidl_offset + 0, childBlob);
    }
}
