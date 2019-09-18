package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanMatchInd {
    public final byte[] addr = new byte[6];
    public byte discoverySessionId;
    public final ArrayList<Byte> extendedServiceSpecificInfo = new ArrayList<>();
    public final ArrayList<Byte> matchFilter = new ArrayList<>();
    public boolean matchOccuredInBeaconFlag;
    public boolean outOfResourceFlag;
    public int peerCipherType;
    public int peerId;
    public boolean peerRequiresRanging;
    public boolean peerRequiresSecurityEnabledInNdp;
    public int rangingIndicationType;
    public int rangingMeasurementInCm;
    public byte rssiValue;
    public final ArrayList<Byte> serviceSpecificInfo = new ArrayList<>();

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
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.discoverySessionId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.peerId))), Integer.valueOf(HidlSupport.deepHashCode(this.addr)), Integer.valueOf(HidlSupport.deepHashCode(this.serviceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.extendedServiceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.matchFilter)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.matchOccuredInBeaconFlag))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.outOfResourceFlag))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.rssiValue))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.peerCipherType))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.peerRequiresSecurityEnabledInNdp))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.peerRequiresRanging))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rangingMeasurementInCm))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rangingIndicationType)))});
    }

    public final String toString() {
        return "{" + ".discoverySessionId = " + this.discoverySessionId + ", .peerId = " + this.peerId + ", .addr = " + Arrays.toString(this.addr) + ", .serviceSpecificInfo = " + this.serviceSpecificInfo + ", .extendedServiceSpecificInfo = " + this.extendedServiceSpecificInfo + ", .matchFilter = " + this.matchFilter + ", .matchOccuredInBeaconFlag = " + this.matchOccuredInBeaconFlag + ", .outOfResourceFlag = " + this.outOfResourceFlag + ", .rssiValue = " + this.rssiValue + ", .peerCipherType = " + NanCipherSuiteType.toString(this.peerCipherType) + ", .peerRequiresSecurityEnabledInNdp = " + this.peerRequiresSecurityEnabledInNdp + ", .peerRequiresRanging = " + this.peerRequiresRanging + ", .rangingMeasurementInCm = " + this.rangingMeasurementInCm + ", .rangingIndicationType = " + NanRangingIndication.dumpBitfield(this.rangingIndicationType) + "}";
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
        HwBlob hwBlob = _hidl_blob;
        this.discoverySessionId = hwBlob.getInt8(_hidl_offset + 0);
        this.peerId = hwBlob.getInt32(_hidl_offset + 4);
        hwBlob.copyToInt8Array(_hidl_offset + 8, this.addr, 6);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 16 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 16 + 0, true);
        this.serviceSpecificInfo.clear();
        int _hidl_index_0 = 0;
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size; _hidl_index_02++) {
            this.serviceSpecificInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_02 * 1))));
        }
        int _hidl_vec_size2 = hwBlob.getInt32(_hidl_offset + 32 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, true);
        this.extendedServiceSpecificInfo.clear();
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size2; _hidl_index_03++) {
            this.extendedServiceSpecificInfo.add(Byte.valueOf(childBlob2.getInt8((long) (_hidl_index_03 * 1))));
        }
        int _hidl_vec_size3 = hwBlob.getInt32(_hidl_offset + 48 + 8);
        HwBlob childBlob3 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 1), _hidl_blob.handle(), _hidl_offset + 48 + 0, true);
        this.matchFilter.clear();
        while (true) {
            int _hidl_index_04 = _hidl_index_0;
            if (_hidl_index_04 < _hidl_vec_size3) {
                this.matchFilter.add(Byte.valueOf(childBlob3.getInt8((long) (_hidl_index_04 * 1))));
                _hidl_index_0 = _hidl_index_04 + 1;
            } else {
                this.matchOccuredInBeaconFlag = hwBlob.getBool(_hidl_offset + 64);
                this.outOfResourceFlag = hwBlob.getBool(_hidl_offset + 65);
                this.rssiValue = hwBlob.getInt8(_hidl_offset + 66);
                this.peerCipherType = hwBlob.getInt32(_hidl_offset + 68);
                this.peerRequiresSecurityEnabledInNdp = hwBlob.getBool(_hidl_offset + 72);
                this.peerRequiresRanging = hwBlob.getBool(_hidl_offset + 73);
                this.rangingMeasurementInCm = hwBlob.getInt32(_hidl_offset + 76);
                this.rangingIndicationType = hwBlob.getInt32(_hidl_offset + 80);
                return;
            }
        }
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
        HwBlob hwBlob = _hidl_blob;
        hwBlob.putInt8(_hidl_offset + 0, this.discoverySessionId);
        hwBlob.putInt32(_hidl_offset + 4, this.peerId);
        hwBlob.putInt8Array(_hidl_offset + 8, this.addr);
        int _hidl_vec_size = this.serviceSpecificInfo.size();
        hwBlob.putInt32(_hidl_offset + 16 + 8, _hidl_vec_size);
        hwBlob.putBool(_hidl_offset + 16 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), this.serviceSpecificInfo.get(_hidl_index_0).byteValue());
        }
        hwBlob.putBlob(_hidl_offset + 16 + 0, childBlob);
        int _hidl_vec_size2 = this.extendedServiceSpecificInfo.size();
        hwBlob.putInt32(_hidl_offset + 32 + 8, _hidl_vec_size2);
        hwBlob.putBool(_hidl_offset + 32 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 1);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            childBlob2.putInt8((long) (_hidl_index_02 * 1), this.extendedServiceSpecificInfo.get(_hidl_index_02).byteValue());
        }
        hwBlob.putBlob(_hidl_offset + 32 + 0, childBlob2);
        int _hidl_vec_size3 = this.matchFilter.size();
        hwBlob.putInt32(_hidl_offset + 48 + 8, _hidl_vec_size3);
        int _hidl_index_03 = 0;
        hwBlob.putBool(_hidl_offset + 48 + 12, false);
        HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 1);
        while (true) {
            int _hidl_index_04 = _hidl_index_03;
            if (_hidl_index_04 < _hidl_vec_size3) {
                childBlob3.putInt8((long) (_hidl_index_04 * 1), this.matchFilter.get(_hidl_index_04).byteValue());
                _hidl_index_03 = _hidl_index_04 + 1;
            } else {
                hwBlob.putBlob(_hidl_offset + 48 + 0, childBlob3);
                hwBlob.putBool(_hidl_offset + 64, this.matchOccuredInBeaconFlag);
                hwBlob.putBool(_hidl_offset + 65, this.outOfResourceFlag);
                hwBlob.putInt8(_hidl_offset + 66, this.rssiValue);
                hwBlob.putInt32(_hidl_offset + 68, this.peerCipherType);
                hwBlob.putBool(_hidl_offset + 72, this.peerRequiresSecurityEnabledInNdp);
                hwBlob.putBool(_hidl_offset + 73, this.peerRequiresRanging);
                hwBlob.putInt32(_hidl_offset + 76, this.rangingMeasurementInCm);
                hwBlob.putInt32(_hidl_offset + 80, this.rangingIndicationType);
                return;
            }
        }
    }
}
