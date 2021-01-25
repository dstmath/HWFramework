package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NanSubscribeRequest {
    public NanDiscoveryCommonConfig baseConfigs = new NanDiscoveryCommonConfig();
    public ArrayList<byte[]> intfAddr = new ArrayList<>();
    public boolean isSsiRequiredForMatch;
    public boolean shouldUseSrf;
    public boolean srfRespondIfInAddressSet;
    public int srfType;
    public int subscribeType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanSubscribeRequest.class) {
            return false;
        }
        NanSubscribeRequest other = (NanSubscribeRequest) otherObject;
        if (HidlSupport.deepEquals(this.baseConfigs, other.baseConfigs) && this.subscribeType == other.subscribeType && this.srfType == other.srfType && this.srfRespondIfInAddressSet == other.srfRespondIfInAddressSet && this.shouldUseSrf == other.shouldUseSrf && this.isSsiRequiredForMatch == other.isSsiRequiredForMatch && HidlSupport.deepEquals(this.intfAddr, other.intfAddr)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(this.baseConfigs)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.subscribeType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.srfType))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.srfRespondIfInAddressSet))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.shouldUseSrf))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isSsiRequiredForMatch))), Integer.valueOf(HidlSupport.deepHashCode(this.intfAddr)));
    }

    public final String toString() {
        return "{.baseConfigs = " + this.baseConfigs + ", .subscribeType = " + NanSubscribeType.toString(this.subscribeType) + ", .srfType = " + NanSrfType.toString(this.srfType) + ", .srfRespondIfInAddressSet = " + this.srfRespondIfInAddressSet + ", .shouldUseSrf = " + this.shouldUseSrf + ", .isSsiRequiredForMatch = " + this.isSsiRequiredForMatch + ", .intfAddr = " + this.intfAddr + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(208), 0);
    }

    public static final ArrayList<NanSubscribeRequest> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanSubscribeRequest> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 208), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanSubscribeRequest _hidl_vec_element = new NanSubscribeRequest();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 208));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.baseConfigs.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 0);
        this.subscribeType = _hidl_blob.getInt32(_hidl_offset + 176);
        this.srfType = _hidl_blob.getInt32(_hidl_offset + 180);
        this.srfRespondIfInAddressSet = _hidl_blob.getBool(_hidl_offset + 184);
        this.shouldUseSrf = _hidl_blob.getBool(_hidl_offset + 185);
        this.isSsiRequiredForMatch = _hidl_blob.getBool(_hidl_offset + 186);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 192 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 6), _hidl_blob.handle(), _hidl_offset + 192 + 0, true);
        this.intfAddr.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            byte[] _hidl_vec_element = new byte[6];
            childBlob.copyToInt8Array((long) (_hidl_index_0 * 6), _hidl_vec_element, 6);
            this.intfAddr.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(208);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanSubscribeRequest> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 208);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 208));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        this.baseConfigs.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 0);
        _hidl_blob.putInt32(176 + _hidl_offset, this.subscribeType);
        _hidl_blob.putInt32(180 + _hidl_offset, this.srfType);
        _hidl_blob.putBool(184 + _hidl_offset, this.srfRespondIfInAddressSet);
        _hidl_blob.putBool(185 + _hidl_offset, this.shouldUseSrf);
        _hidl_blob.putBool(186 + _hidl_offset, this.isSsiRequiredForMatch);
        int _hidl_vec_size = this.intfAddr.size();
        _hidl_blob.putInt32(_hidl_offset + 192 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 192 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 6);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            long _hidl_array_offset_1 = (long) (_hidl_index_0 * 6);
            byte[] _hidl_array_item_1 = this.intfAddr.get(_hidl_index_0);
            if (_hidl_array_item_1 == null || _hidl_array_item_1.length != 6) {
                throw new IllegalArgumentException("Array element is not of the expected length");
            }
            childBlob.putInt8Array(_hidl_array_offset_1, _hidl_array_item_1);
        }
        _hidl_blob.putBlob(192 + _hidl_offset + 0, childBlob);
    }
}
