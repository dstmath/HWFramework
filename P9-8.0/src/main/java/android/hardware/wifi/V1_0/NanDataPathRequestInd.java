package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanDataPathRequestInd {
    public final ArrayList<Byte> appInfo = new ArrayList();
    public byte discoverySessionId;
    public int ndpInstanceId;
    public final byte[] peerDiscMacAddr = new byte[6];
    public boolean securityRequired;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanDataPathRequestInd.class) {
            return false;
        }
        NanDataPathRequestInd other = (NanDataPathRequestInd) otherObject;
        return this.discoverySessionId == other.discoverySessionId && HidlSupport.deepEquals(this.peerDiscMacAddr, other.peerDiscMacAddr) && this.ndpInstanceId == other.ndpInstanceId && this.securityRequired == other.securityRequired && HidlSupport.deepEquals(this.appInfo, other.appInfo);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.discoverySessionId))), Integer.valueOf(HidlSupport.deepHashCode(this.peerDiscMacAddr)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ndpInstanceId))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.securityRequired))), Integer.valueOf(HidlSupport.deepHashCode(this.appInfo))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".discoverySessionId = ");
        builder.append(this.discoverySessionId);
        builder.append(", .peerDiscMacAddr = ");
        builder.append(Arrays.toString(this.peerDiscMacAddr));
        builder.append(", .ndpInstanceId = ");
        builder.append(this.ndpInstanceId);
        builder.append(", .securityRequired = ");
        builder.append(this.securityRequired);
        builder.append(", .appInfo = ");
        builder.append(this.appInfo);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<NanDataPathRequestInd> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanDataPathRequestInd> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanDataPathRequestInd _hidl_vec_element = new NanDataPathRequestInd();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.discoverySessionId = _hidl_blob.getInt8(0 + _hidl_offset);
        long _hidl_array_offset_0 = _hidl_offset + 1;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            this.peerDiscMacAddr[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        this.ndpInstanceId = _hidl_blob.getInt32(8 + _hidl_offset);
        this.securityRequired = _hidl_blob.getBool(12 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((16 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (16 + _hidl_offset) + 0, true);
        this.appInfo.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.appInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanDataPathRequestInd> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((NanDataPathRequestInd) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(0 + _hidl_offset, this.discoverySessionId);
        long _hidl_array_offset_0 = _hidl_offset + 1;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.peerDiscMacAddr[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_blob.putInt32(8 + _hidl_offset, this.ndpInstanceId);
        _hidl_blob.putBool(12 + _hidl_offset, this.securityRequired);
        int _hidl_vec_size = this.appInfo.size();
        _hidl_blob.putInt32((16 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((16 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.appInfo.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((16 + _hidl_offset) + 0, childBlob);
    }
}
