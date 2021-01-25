package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanMatchInd {
    public byte[] addr = new byte[6];
    public byte discoverySessionId;
    public ArrayList<Byte> extendedServiceSpecificInfo = new ArrayList<>();
    public ArrayList<Byte> matchFilter = new ArrayList<>();
    public boolean matchOccuredInBeaconFlag;
    public boolean outOfResourceFlag;
    public int peerCipherType;
    public int peerId;
    public boolean peerRequiresRanging;
    public boolean peerRequiresSecurityEnabledInNdp;
    public int rangingIndicationType;
    public int rangingMeasurementInCm;
    public byte rssiValue;
    public ArrayList<Byte> serviceSpecificInfo = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanMatchInd.class) {
            return false;
        }
        NanMatchInd other = (NanMatchInd) otherObject;
        if (this.discoverySessionId == other.discoverySessionId && this.peerId == other.peerId && HidlSupport.deepEquals(this.addr, other.addr) && HidlSupport.deepEquals(this.serviceSpecificInfo, other.serviceSpecificInfo) && HidlSupport.deepEquals(this.extendedServiceSpecificInfo, other.extendedServiceSpecificInfo) && HidlSupport.deepEquals(this.matchFilter, other.matchFilter) && this.matchOccuredInBeaconFlag == other.matchOccuredInBeaconFlag && this.outOfResourceFlag == other.outOfResourceFlag && this.rssiValue == other.rssiValue && this.peerCipherType == other.peerCipherType && this.peerRequiresSecurityEnabledInNdp == other.peerRequiresSecurityEnabledInNdp && this.peerRequiresRanging == other.peerRequiresRanging && this.rangingMeasurementInCm == other.rangingMeasurementInCm && HidlSupport.deepEquals(Integer.valueOf(this.rangingIndicationType), Integer.valueOf(other.rangingIndicationType))) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.discoverySessionId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.peerId))), Integer.valueOf(HidlSupport.deepHashCode(this.addr)), Integer.valueOf(HidlSupport.deepHashCode(this.serviceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.extendedServiceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.matchFilter)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.matchOccuredInBeaconFlag))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.outOfResourceFlag))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rssiValue))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.peerCipherType))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.peerRequiresSecurityEnabledInNdp))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.peerRequiresRanging))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rangingMeasurementInCm))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rangingIndicationType))));
    }

    public final String toString() {
        return "{.discoverySessionId = " + ((int) this.discoverySessionId) + ", .peerId = " + this.peerId + ", .addr = " + Arrays.toString(this.addr) + ", .serviceSpecificInfo = " + this.serviceSpecificInfo + ", .extendedServiceSpecificInfo = " + this.extendedServiceSpecificInfo + ", .matchFilter = " + this.matchFilter + ", .matchOccuredInBeaconFlag = " + this.matchOccuredInBeaconFlag + ", .outOfResourceFlag = " + this.outOfResourceFlag + ", .rssiValue = " + ((int) this.rssiValue) + ", .peerCipherType = " + NanCipherSuiteType.toString(this.peerCipherType) + ", .peerRequiresSecurityEnabledInNdp = " + this.peerRequiresSecurityEnabledInNdp + ", .peerRequiresRanging = " + this.peerRequiresRanging + ", .rangingMeasurementInCm = " + this.rangingMeasurementInCm + ", .rangingIndicationType = " + NanRangingIndication.dumpBitfield(this.rangingIndicationType) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(88), 0);
    }

    public static final ArrayList<NanMatchInd> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanMatchInd> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 88), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanMatchInd _hidl_vec_element = new NanMatchInd();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 88));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.discoverySessionId = _hidl_blob.getInt8(_hidl_offset + 0);
        this.peerId = _hidl_blob.getInt32(_hidl_offset + 4);
        _hidl_blob.copyToInt8Array(_hidl_offset + 8, this.addr, 6);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.serviceSpecificInfo.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.serviceSpecificInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 32 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, true);
        this.extendedServiceSpecificInfo.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.extendedServiceSpecificInfo.add(Byte.valueOf(childBlob2.getInt8((long) (_hidl_index_02 * 1))));
        }
        int _hidl_vec_size3 = _hidl_blob.getInt32(_hidl_offset + 48 + 8);
        HwBlob childBlob3 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 1), _hidl_blob.handle(), _hidl_offset + 48 + 0, true);
        this.matchFilter.clear();
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            this.matchFilter.add(Byte.valueOf(childBlob3.getInt8((long) (_hidl_index_03 * 1))));
        }
        this.matchOccuredInBeaconFlag = _hidl_blob.getBool(_hidl_offset + 64);
        this.outOfResourceFlag = _hidl_blob.getBool(_hidl_offset + 65);
        this.rssiValue = _hidl_blob.getInt8(_hidl_offset + 66);
        this.peerCipherType = _hidl_blob.getInt32(_hidl_offset + 68);
        this.peerRequiresSecurityEnabledInNdp = _hidl_blob.getBool(_hidl_offset + 72);
        this.peerRequiresRanging = _hidl_blob.getBool(_hidl_offset + 73);
        this.rangingMeasurementInCm = _hidl_blob.getInt32(_hidl_offset + 76);
        this.rangingIndicationType = _hidl_blob.getInt32(_hidl_offset + 80);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(88);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanMatchInd> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 88);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 88));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(_hidl_offset + 0, this.discoverySessionId);
        _hidl_blob.putInt32(_hidl_offset + 4, this.peerId);
        long _hidl_array_offset_0 = _hidl_offset + 8;
        byte[] _hidl_array_item_0 = this.addr;
        if (_hidl_array_item_0 == null || _hidl_array_item_0.length != 6) {
            throw new IllegalArgumentException("Array element is not of the expected length");
        }
        _hidl_blob.putInt8Array(_hidl_array_offset_0, _hidl_array_item_0);
        int _hidl_vec_size = this.serviceSpecificInfo.size();
        _hidl_blob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), this.serviceSpecificInfo.get(_hidl_index_0).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 16 + 0, childBlob);
        int _hidl_vec_size2 = this.extendedServiceSpecificInfo.size();
        _hidl_blob.putInt32(_hidl_offset + 32 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 32 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 1);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            childBlob2.putInt8((long) (_hidl_index_02 * 1), this.extendedServiceSpecificInfo.get(_hidl_index_02).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 32 + 0, childBlob2);
        int _hidl_vec_size3 = this.matchFilter.size();
        _hidl_blob.putInt32(_hidl_offset + 48 + 8, _hidl_vec_size3);
        _hidl_blob.putBool(_hidl_offset + 48 + 12, false);
        HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 1);
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            childBlob3.putInt8((long) (_hidl_index_03 * 1), this.matchFilter.get(_hidl_index_03).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 48 + 0, childBlob3);
        _hidl_blob.putBool(_hidl_offset + 64, this.matchOccuredInBeaconFlag);
        _hidl_blob.putBool(_hidl_offset + 65, this.outOfResourceFlag);
        _hidl_blob.putInt8(_hidl_offset + 66, this.rssiValue);
        _hidl_blob.putInt32(_hidl_offset + 68, this.peerCipherType);
        _hidl_blob.putBool(_hidl_offset + 72, this.peerRequiresSecurityEnabledInNdp);
        _hidl_blob.putBool(_hidl_offset + 73, this.peerRequiresRanging);
        _hidl_blob.putInt32(_hidl_offset + 76, this.rangingMeasurementInCm);
        _hidl_blob.putInt32(_hidl_offset + 80, this.rangingIndicationType);
    }
}
