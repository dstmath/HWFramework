package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanDebugConfig {
    public short clusterIdBottomRangeVal;
    public short clusterIdTopRangeVal;
    public final int[] discoveryChannelMhzVal = new int[2];
    public byte hopCountForceVal;
    public final byte[] intfAddrVal = new byte[6];
    public int ouiVal;
    public byte randomFactorForceVal;
    public final boolean[] useBeaconsInBandVal = new boolean[2];
    public final boolean[] useSdfInBandVal = new boolean[2];
    public boolean validClusterIdVals;
    public boolean validDiscoveryChannelVal;
    public boolean validHopCountForceVal;
    public boolean validIntfAddrVal;
    public boolean validOuiVal;
    public boolean validRandomFactorForceVal;
    public boolean validUseBeaconsInBandVal;
    public boolean validUseSdfInBandVal;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanDebugConfig.class) {
            return false;
        }
        NanDebugConfig other = (NanDebugConfig) otherObject;
        return this.validClusterIdVals == other.validClusterIdVals && this.clusterIdBottomRangeVal == other.clusterIdBottomRangeVal && this.clusterIdTopRangeVal == other.clusterIdTopRangeVal && this.validIntfAddrVal == other.validIntfAddrVal && HidlSupport.deepEquals(this.intfAddrVal, other.intfAddrVal) && this.validOuiVal == other.validOuiVal && this.ouiVal == other.ouiVal && this.validRandomFactorForceVal == other.validRandomFactorForceVal && this.randomFactorForceVal == other.randomFactorForceVal && this.validHopCountForceVal == other.validHopCountForceVal && this.hopCountForceVal == other.hopCountForceVal && this.validDiscoveryChannelVal == other.validDiscoveryChannelVal && HidlSupport.deepEquals(this.discoveryChannelMhzVal, other.discoveryChannelMhzVal) && this.validUseBeaconsInBandVal == other.validUseBeaconsInBandVal && HidlSupport.deepEquals(this.useBeaconsInBandVal, other.useBeaconsInBandVal) && this.validUseSdfInBandVal == other.validUseSdfInBandVal && HidlSupport.deepEquals(this.useSdfInBandVal, other.useSdfInBandVal);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validClusterIdVals))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.clusterIdBottomRangeVal))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.clusterIdTopRangeVal))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validIntfAddrVal))), Integer.valueOf(HidlSupport.deepHashCode(this.intfAddrVal)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validOuiVal))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ouiVal))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validRandomFactorForceVal))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.randomFactorForceVal))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validHopCountForceVal))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.hopCountForceVal))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validDiscoveryChannelVal))), Integer.valueOf(HidlSupport.deepHashCode(this.discoveryChannelMhzVal)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validUseBeaconsInBandVal))), Integer.valueOf(HidlSupport.deepHashCode(this.useBeaconsInBandVal)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validUseSdfInBandVal))), Integer.valueOf(HidlSupport.deepHashCode(this.useSdfInBandVal))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".validClusterIdVals = ");
        builder.append(this.validClusterIdVals);
        builder.append(", .clusterIdBottomRangeVal = ");
        builder.append(this.clusterIdBottomRangeVal);
        builder.append(", .clusterIdTopRangeVal = ");
        builder.append(this.clusterIdTopRangeVal);
        builder.append(", .validIntfAddrVal = ");
        builder.append(this.validIntfAddrVal);
        builder.append(", .intfAddrVal = ");
        builder.append(Arrays.toString(this.intfAddrVal));
        builder.append(", .validOuiVal = ");
        builder.append(this.validOuiVal);
        builder.append(", .ouiVal = ");
        builder.append(this.ouiVal);
        builder.append(", .validRandomFactorForceVal = ");
        builder.append(this.validRandomFactorForceVal);
        builder.append(", .randomFactorForceVal = ");
        builder.append(this.randomFactorForceVal);
        builder.append(", .validHopCountForceVal = ");
        builder.append(this.validHopCountForceVal);
        builder.append(", .hopCountForceVal = ");
        builder.append(this.hopCountForceVal);
        builder.append(", .validDiscoveryChannelVal = ");
        builder.append(this.validDiscoveryChannelVal);
        builder.append(", .discoveryChannelMhzVal = ");
        builder.append(Arrays.toString(this.discoveryChannelMhzVal));
        builder.append(", .validUseBeaconsInBandVal = ");
        builder.append(this.validUseBeaconsInBandVal);
        builder.append(", .useBeaconsInBandVal = ");
        builder.append(Arrays.toString(this.useBeaconsInBandVal));
        builder.append(", .validUseSdfInBandVal = ");
        builder.append(this.validUseSdfInBandVal);
        builder.append(", .useSdfInBandVal = ");
        builder.append(Arrays.toString(this.useSdfInBandVal));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(44), 0);
    }

    public static final ArrayList<NanDebugConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanDebugConfig> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 44), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanDebugConfig _hidl_vec_element = new NanDebugConfig();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 44));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0_0;
        this.validClusterIdVals = _hidl_blob.getBool(0 + _hidl_offset);
        this.clusterIdBottomRangeVal = _hidl_blob.getInt16(2 + _hidl_offset);
        this.clusterIdTopRangeVal = _hidl_blob.getInt16(4 + _hidl_offset);
        this.validIntfAddrVal = _hidl_blob.getBool(6 + _hidl_offset);
        long _hidl_array_offset_0 = _hidl_offset + 7;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            this.intfAddrVal[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        this.validOuiVal = _hidl_blob.getBool(13 + _hidl_offset);
        this.ouiVal = _hidl_blob.getInt32(16 + _hidl_offset);
        this.validRandomFactorForceVal = _hidl_blob.getBool(20 + _hidl_offset);
        this.randomFactorForceVal = _hidl_blob.getInt8(21 + _hidl_offset);
        this.validHopCountForceVal = _hidl_blob.getBool(22 + _hidl_offset);
        this.hopCountForceVal = _hidl_blob.getInt8(23 + _hidl_offset);
        this.validDiscoveryChannelVal = _hidl_blob.getBool(24 + _hidl_offset);
        _hidl_array_offset_0 = _hidl_offset + 28;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            this.discoveryChannelMhzVal[_hidl_index_0_0] = _hidl_blob.getInt32(_hidl_array_offset_0);
            _hidl_array_offset_0 += 4;
        }
        this.validUseBeaconsInBandVal = _hidl_blob.getBool(36 + _hidl_offset);
        _hidl_array_offset_0 = _hidl_offset + 37;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            this.useBeaconsInBandVal[_hidl_index_0_0] = _hidl_blob.getBool(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        this.validUseSdfInBandVal = _hidl_blob.getBool(39 + _hidl_offset);
        _hidl_array_offset_0 = _hidl_offset + 40;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            this.useSdfInBandVal[_hidl_index_0_0] = _hidl_blob.getBool(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(44);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanDebugConfig> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 44);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((NanDebugConfig) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 44));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0_0;
        _hidl_blob.putBool(0 + _hidl_offset, this.validClusterIdVals);
        _hidl_blob.putInt16(2 + _hidl_offset, this.clusterIdBottomRangeVal);
        _hidl_blob.putInt16(_hidl_offset + 4, this.clusterIdTopRangeVal);
        _hidl_blob.putBool(6 + _hidl_offset, this.validIntfAddrVal);
        long _hidl_array_offset_0 = _hidl_offset + 7;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.intfAddrVal[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_blob.putBool(13 + _hidl_offset, this.validOuiVal);
        _hidl_blob.putInt32(16 + _hidl_offset, this.ouiVal);
        _hidl_blob.putBool(20 + _hidl_offset, this.validRandomFactorForceVal);
        _hidl_blob.putInt8(21 + _hidl_offset, this.randomFactorForceVal);
        _hidl_blob.putBool(22 + _hidl_offset, this.validHopCountForceVal);
        _hidl_blob.putInt8(23 + _hidl_offset, this.hopCountForceVal);
        _hidl_blob.putBool(24 + _hidl_offset, this.validDiscoveryChannelVal);
        _hidl_array_offset_0 = _hidl_offset + 28;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            _hidl_blob.putInt32(_hidl_array_offset_0, this.discoveryChannelMhzVal[_hidl_index_0_0]);
            _hidl_array_offset_0 += 4;
        }
        _hidl_blob.putBool(36 + _hidl_offset, this.validUseBeaconsInBandVal);
        _hidl_array_offset_0 = _hidl_offset + 37;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            _hidl_blob.putBool(_hidl_array_offset_0, this.useBeaconsInBandVal[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_blob.putBool(39 + _hidl_offset, this.validUseSdfInBandVal);
        _hidl_array_offset_0 = _hidl_offset + 40;
        for (_hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            _hidl_blob.putBool(_hidl_array_offset_0, this.useSdfInBandVal[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
    }
}
