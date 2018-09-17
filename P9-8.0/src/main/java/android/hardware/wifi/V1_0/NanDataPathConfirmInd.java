package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanDataPathConfirmInd {
    public final ArrayList<Byte> appInfo = new ArrayList();
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
        return this.ndpInstanceId == other.ndpInstanceId && this.dataPathSetupSuccess == other.dataPathSetupSuccess && HidlSupport.deepEquals(this.peerNdiMacAddr, other.peerNdiMacAddr) && HidlSupport.deepEquals(this.appInfo, other.appInfo) && HidlSupport.deepEquals(this.status, other.status);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ndpInstanceId))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.dataPathSetupSuccess))), Integer.valueOf(HidlSupport.deepHashCode(this.peerNdiMacAddr)), Integer.valueOf(HidlSupport.deepHashCode(this.appInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.status))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".ndpInstanceId = ");
        builder.append(this.ndpInstanceId);
        builder.append(", .dataPathSetupSuccess = ");
        builder.append(this.dataPathSetupSuccess);
        builder.append(", .peerNdiMacAddr = ");
        builder.append(Arrays.toString(this.peerNdiMacAddr));
        builder.append(", .appInfo = ");
        builder.append(this.appInfo);
        builder.append(", .status = ");
        builder.append(this.status);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<NanDataPathConfirmInd> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanDataPathConfirmInd> _hidl_vec = new ArrayList();
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
        this.ndpInstanceId = _hidl_blob.getInt32(0 + _hidl_offset);
        this.dataPathSetupSuccess = _hidl_blob.getBool(4 + _hidl_offset);
        long _hidl_array_offset_0 = _hidl_offset + 5;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            this.peerNdiMacAddr[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        int _hidl_vec_size = _hidl_blob.getInt32((16 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (16 + _hidl_offset) + 0, true);
        this.appInfo.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.appInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        this.status.readEmbeddedFromParcel(parcel, _hidl_blob, 32 + _hidl_offset);
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
            ((NanDataPathConfirmInd) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.ndpInstanceId);
        _hidl_blob.putBool(4 + _hidl_offset, this.dataPathSetupSuccess);
        long _hidl_array_offset_0 = _hidl_offset + 5;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.peerNdiMacAddr[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        int _hidl_vec_size = this.appInfo.size();
        _hidl_blob.putInt32((16 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((16 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.appInfo.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((16 + _hidl_offset) + 0, childBlob);
        this.status.writeEmbeddedToBlob(_hidl_blob, 32 + _hidl_offset);
    }
}
