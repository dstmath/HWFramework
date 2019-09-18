package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanDataPathConfirmInd {
    public final ArrayList<Byte> appInfo = new ArrayList<>();
    public boolean dataPathSetupSuccess;
    public int ndpInstanceId;
    public final byte[] peerNdiMacAddr = new byte[6];
    public final WifiNanStatus status = new WifiNanStatus();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanDataPathConfirmInd.class) {
            return false;
        }
        NanDataPathConfirmInd other = (NanDataPathConfirmInd) otherObject;
        if (this.ndpInstanceId == other.ndpInstanceId && this.dataPathSetupSuccess == other.dataPathSetupSuccess && HidlSupport.deepEquals(this.peerNdiMacAddr, other.peerNdiMacAddr) && HidlSupport.deepEquals(this.appInfo, other.appInfo) && HidlSupport.deepEquals(this.status, other.status)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ndpInstanceId))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.dataPathSetupSuccess))), Integer.valueOf(HidlSupport.deepHashCode(this.peerNdiMacAddr)), Integer.valueOf(HidlSupport.deepHashCode(this.appInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.status))});
    }

    public final String toString() {
        return "{" + ".ndpInstanceId = " + this.ndpInstanceId + ", .dataPathSetupSuccess = " + this.dataPathSetupSuccess + ", .peerNdiMacAddr = " + Arrays.toString(this.peerNdiMacAddr) + ", .appInfo = " + this.appInfo + ", .status = " + this.status + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<NanDataPathConfirmInd> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanDataPathConfirmInd> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanDataPathConfirmInd _hidl_vec_element = new NanDataPathConfirmInd();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.ndpInstanceId = hwBlob.getInt32(_hidl_offset + 0);
        this.dataPathSetupSuccess = hwBlob.getBool(_hidl_offset + 4);
        hwBlob.copyToInt8Array(_hidl_offset + 5, this.peerNdiMacAddr, 6);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.appInfo.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.appInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        this.status.readEmbeddedFromParcel(parcel, hwBlob, _hidl_offset + 32);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanDataPathConfirmInd> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.ndpInstanceId);
        _hidl_blob.putBool(4 + _hidl_offset, this.dataPathSetupSuccess);
        _hidl_blob.putInt8Array(5 + _hidl_offset, this.peerNdiMacAddr);
        int _hidl_vec_size = this.appInfo.size();
        _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size);
        int _hidl_index_0 = 0;
        _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        while (true) {
            int _hidl_index_02 = _hidl_index_0;
            if (_hidl_index_02 < _hidl_vec_size) {
                childBlob.putInt8((long) (_hidl_index_02 * 1), this.appInfo.get(_hidl_index_02).byteValue());
                _hidl_index_0 = _hidl_index_02 + 1;
            } else {
                _hidl_blob.putBlob(16 + _hidl_offset + 0, childBlob);
                this.status.writeEmbeddedToBlob(_hidl_blob, 32 + _hidl_offset);
                return;
            }
        }
    }
}
