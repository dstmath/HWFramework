package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanDataPathSecurityConfig {
    public int cipherType;
    public final ArrayList<Byte> passphrase = new ArrayList();
    public final byte[] pmk = new byte[32];
    public int securityType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanDataPathSecurityConfig.class) {
            return false;
        }
        NanDataPathSecurityConfig other = (NanDataPathSecurityConfig) otherObject;
        return this.securityType == other.securityType && this.cipherType == other.cipherType && HidlSupport.deepEquals(this.pmk, other.pmk) && HidlSupport.deepEquals(this.passphrase, other.passphrase);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.securityType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cipherType))), Integer.valueOf(HidlSupport.deepHashCode(this.pmk)), Integer.valueOf(HidlSupport.deepHashCode(this.passphrase))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".securityType = ");
        builder.append(NanDataPathSecurityType.toString(this.securityType));
        builder.append(", .cipherType = ");
        builder.append(NanCipherSuiteType.toString(this.cipherType));
        builder.append(", .pmk = ");
        builder.append(Arrays.toString(this.pmk));
        builder.append(", .passphrase = ");
        builder.append(this.passphrase);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<NanDataPathSecurityConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanDataPathSecurityConfig> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanDataPathSecurityConfig _hidl_vec_element = new NanDataPathSecurityConfig();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.securityType = _hidl_blob.getInt32(0 + _hidl_offset);
        this.cipherType = _hidl_blob.getInt32(4 + _hidl_offset);
        long _hidl_array_offset_0 = _hidl_offset + 8;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 32; _hidl_index_0_0++) {
            this.pmk[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        int _hidl_vec_size = _hidl_blob.getInt32((40 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (40 + _hidl_offset) + 0, true);
        this.passphrase.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.passphrase.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanDataPathSecurityConfig> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((NanDataPathSecurityConfig) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.securityType);
        _hidl_blob.putInt32(4 + _hidl_offset, this.cipherType);
        long _hidl_array_offset_0 = _hidl_offset + 8;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 32; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.pmk[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        int _hidl_vec_size = this.passphrase.size();
        _hidl_blob.putInt32((40 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((40 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.passphrase.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((40 + _hidl_offset) + 0, childBlob);
    }
}
