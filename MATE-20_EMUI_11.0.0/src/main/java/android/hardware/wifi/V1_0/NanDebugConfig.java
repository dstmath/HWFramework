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
    public int[] discoveryChannelMhzVal = new int[2];
    public byte hopCountForceVal;
    public byte[] intfAddrVal = new byte[6];
    public int ouiVal;
    public byte randomFactorForceVal;
    public boolean[] useBeaconsInBandVal = new boolean[2];
    public boolean[] useSdfInBandVal = new boolean[2];
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
        if (this.validClusterIdVals == other.validClusterIdVals && this.clusterIdBottomRangeVal == other.clusterIdBottomRangeVal && this.clusterIdTopRangeVal == other.clusterIdTopRangeVal && this.validIntfAddrVal == other.validIntfAddrVal && HidlSupport.deepEquals(this.intfAddrVal, other.intfAddrVal) && this.validOuiVal == other.validOuiVal && this.ouiVal == other.ouiVal && this.validRandomFactorForceVal == other.validRandomFactorForceVal && this.randomFactorForceVal == other.randomFactorForceVal && this.validHopCountForceVal == other.validHopCountForceVal && this.hopCountForceVal == other.hopCountForceVal && this.validDiscoveryChannelVal == other.validDiscoveryChannelVal && HidlSupport.deepEquals(this.discoveryChannelMhzVal, other.discoveryChannelMhzVal) && this.validUseBeaconsInBandVal == other.validUseBeaconsInBandVal && HidlSupport.deepEquals(this.useBeaconsInBandVal, other.useBeaconsInBandVal) && this.validUseSdfInBandVal == other.validUseSdfInBandVal && HidlSupport.deepEquals(this.useSdfInBandVal, other.useSdfInBandVal)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validClusterIdVals))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.clusterIdBottomRangeVal))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.clusterIdTopRangeVal))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validIntfAddrVal))), Integer.valueOf(HidlSupport.deepHashCode(this.intfAddrVal)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validOuiVal))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ouiVal))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validRandomFactorForceVal))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.randomFactorForceVal))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validHopCountForceVal))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.hopCountForceVal))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validDiscoveryChannelVal))), Integer.valueOf(HidlSupport.deepHashCode(this.discoveryChannelMhzVal)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validUseBeaconsInBandVal))), Integer.valueOf(HidlSupport.deepHashCode(this.useBeaconsInBandVal)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.validUseSdfInBandVal))), Integer.valueOf(HidlSupport.deepHashCode(this.useSdfInBandVal)));
    }

    public final String toString() {
        return "{.validClusterIdVals = " + this.validClusterIdVals + ", .clusterIdBottomRangeVal = " + ((int) this.clusterIdBottomRangeVal) + ", .clusterIdTopRangeVal = " + ((int) this.clusterIdTopRangeVal) + ", .validIntfAddrVal = " + this.validIntfAddrVal + ", .intfAddrVal = " + Arrays.toString(this.intfAddrVal) + ", .validOuiVal = " + this.validOuiVal + ", .ouiVal = " + this.ouiVal + ", .validRandomFactorForceVal = " + this.validRandomFactorForceVal + ", .randomFactorForceVal = " + ((int) this.randomFactorForceVal) + ", .validHopCountForceVal = " + this.validHopCountForceVal + ", .hopCountForceVal = " + ((int) this.hopCountForceVal) + ", .validDiscoveryChannelVal = " + this.validDiscoveryChannelVal + ", .discoveryChannelMhzVal = " + Arrays.toString(this.discoveryChannelMhzVal) + ", .validUseBeaconsInBandVal = " + this.validUseBeaconsInBandVal + ", .useBeaconsInBandVal = " + Arrays.toString(this.useBeaconsInBandVal) + ", .validUseSdfInBandVal = " + this.validUseSdfInBandVal + ", .useSdfInBandVal = " + Arrays.toString(this.useSdfInBandVal) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(44), 0);
    }

    public static final ArrayList<NanDebugConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanDebugConfig> _hidl_vec = new ArrayList<>();
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
        this.validClusterIdVals = _hidl_blob.getBool(0 + _hidl_offset);
        this.clusterIdBottomRangeVal = _hidl_blob.getInt16(2 + _hidl_offset);
        this.clusterIdTopRangeVal = _hidl_blob.getInt16(4 + _hidl_offset);
        this.validIntfAddrVal = _hidl_blob.getBool(6 + _hidl_offset);
        _hidl_blob.copyToInt8Array(7 + _hidl_offset, this.intfAddrVal, 6);
        this.validOuiVal = _hidl_blob.getBool(13 + _hidl_offset);
        this.ouiVal = _hidl_blob.getInt32(16 + _hidl_offset);
        this.validRandomFactorForceVal = _hidl_blob.getBool(20 + _hidl_offset);
        this.randomFactorForceVal = _hidl_blob.getInt8(21 + _hidl_offset);
        this.validHopCountForceVal = _hidl_blob.getBool(22 + _hidl_offset);
        this.hopCountForceVal = _hidl_blob.getInt8(23 + _hidl_offset);
        this.validDiscoveryChannelVal = _hidl_blob.getBool(24 + _hidl_offset);
        _hidl_blob.copyToInt32Array(28 + _hidl_offset, this.discoveryChannelMhzVal, 2);
        this.validUseBeaconsInBandVal = _hidl_blob.getBool(36 + _hidl_offset);
        _hidl_blob.copyToBoolArray(37 + _hidl_offset, this.useBeaconsInBandVal, 2);
        this.validUseSdfInBandVal = _hidl_blob.getBool(39 + _hidl_offset);
        _hidl_blob.copyToBoolArray(40 + _hidl_offset, this.useSdfInBandVal, 2);
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
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 44));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putBool(0 + _hidl_offset, this.validClusterIdVals);
        _hidl_blob.putInt16(2 + _hidl_offset, this.clusterIdBottomRangeVal);
        _hidl_blob.putInt16(4 + _hidl_offset, this.clusterIdTopRangeVal);
        _hidl_blob.putBool(6 + _hidl_offset, this.validIntfAddrVal);
        long _hidl_array_offset_0 = 7 + _hidl_offset;
        byte[] _hidl_array_item_0 = this.intfAddrVal;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 6) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
        _hidl_blob.putBool(13 + _hidl_offset, this.validOuiVal);
        _hidl_blob.putInt32(16 + _hidl_offset, this.ouiVal);
        _hidl_blob.putBool(20 + _hidl_offset, this.validRandomFactorForceVal);
        _hidl_blob.putInt8(21 + _hidl_offset, this.randomFactorForceVal);
        _hidl_blob.putBool(22 + _hidl_offset, this.validHopCountForceVal);
        _hidl_blob.putInt8(23 + _hidl_offset, this.hopCountForceVal);
        _hidl_blob.putBool(24 + _hidl_offset, this.validDiscoveryChannelVal);
        long _hidl_array_offset_02 = 28 + _hidl_offset;
        int[] _hidl_array_item_02 = this.discoveryChannelMhzVal;
        if (_hidl_array_item_02 == null || _hidl_array_item_02.length != 2) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt32Array(_hidl_array_offset_02, _hidl_array_item_02);
        _hidl_blob.putBool(36 + _hidl_offset, this.validUseBeaconsInBandVal);
        long _hidl_array_offset_03 = 37 + _hidl_offset;
        boolean[] _hidl_array_item_03 = this.useBeaconsInBandVal;
        if (_hidl_array_item_03 == null || _hidl_array_item_03.length != 2) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putBoolArray(_hidl_array_offset_03, _hidl_array_item_03);
        _hidl_blob.putBool(39 + _hidl_offset, this.validUseSdfInBandVal);
        long _hidl_array_offset_04 = 40 + _hidl_offset;
        boolean[] _hidl_array_item_04 = this.useSdfInBandVal;
        if (_hidl_array_item_04 == null || _hidl_array_item_04.length != 2) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putBoolArray(_hidl_array_offset_04, _hidl_array_item_04);
    }
}
