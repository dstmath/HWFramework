package android.hardware.wifi.V1_2;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanDataPathScheduleUpdateInd {
    public final ArrayList<NanDataPathChannelInfo> channelInfo = new ArrayList<>();
    public final ArrayList<Integer> ndpInstanceIds = new ArrayList<>();
    public final byte[] peerDiscoveryAddress = new byte[6];

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanDataPathScheduleUpdateInd.class) {
            return false;
        }
        NanDataPathScheduleUpdateInd other = (NanDataPathScheduleUpdateInd) otherObject;
        if (HidlSupport.deepEquals(this.peerDiscoveryAddress, other.peerDiscoveryAddress) && HidlSupport.deepEquals(this.channelInfo, other.channelInfo) && HidlSupport.deepEquals(this.ndpInstanceIds, other.ndpInstanceIds)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(this.peerDiscoveryAddress)), Integer.valueOf(HidlSupport.deepHashCode(this.channelInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.ndpInstanceIds))});
    }

    public final String toString() {
        return "{" + ".peerDiscoveryAddress = " + Arrays.toString(this.peerDiscoveryAddress) + ", .channelInfo = " + this.channelInfo + ", .ndpInstanceIds = " + this.ndpInstanceIds + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<NanDataPathScheduleUpdateInd> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanDataPathScheduleUpdateInd> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanDataPathScheduleUpdateInd _hidl_vec_element = new NanDataPathScheduleUpdateInd();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        hwBlob.copyToInt8Array(_hidl_offset + 0, this.peerDiscoveryAddress, 6);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 12), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.channelInfo.clear();
        int _hidl_index_0 = 0;
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size; _hidl_index_02++) {
            NanDataPathChannelInfo _hidl_vec_element = new NanDataPathChannelInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_02 * 12));
            this.channelInfo.add(_hidl_vec_element);
        }
        int _hidl_vec_size2 = hwBlob.getInt32(_hidl_offset + 24 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 4), _hidl_blob.handle(), _hidl_offset + 24 + 0, true);
        this.ndpInstanceIds.clear();
        while (true) {
            int _hidl_index_03 = _hidl_index_0;
            if (_hidl_index_03 < _hidl_vec_size2) {
                this.ndpInstanceIds.add(Integer.valueOf(childBlob2.getInt32((long) (_hidl_index_03 * 4))));
                _hidl_index_0 = _hidl_index_03 + 1;
            } else {
                return;
            }
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanDataPathScheduleUpdateInd> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        hwBlob.putInt8Array(_hidl_offset + 0, this.peerDiscoveryAddress);
        int _hidl_vec_size = this.channelInfo.size();
        hwBlob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        hwBlob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 12);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size; _hidl_index_02++) {
            this.channelInfo.get(_hidl_index_02).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_02 * 12));
        }
        hwBlob.putBlob(_hidl_offset + 8 + 0, childBlob);
        int _hidl_vec_size2 = this.ndpInstanceIds.size();
        hwBlob.putInt32(_hidl_offset + 24 + 8, _hidl_vec_size2);
        hwBlob.putBool(_hidl_offset + 24 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 4);
        while (true) {
            int _hidl_index_03 = _hidl_index_0;
            if (_hidl_index_03 < _hidl_vec_size2) {
                childBlob2.putInt32((long) (_hidl_index_03 * 4), this.ndpInstanceIds.get(_hidl_index_03).intValue());
                _hidl_index_0 = _hidl_index_03 + 1;
            } else {
                hwBlob.putBlob(_hidl_offset + 24 + 0, childBlob2);
                return;
            }
        }
    }
}
