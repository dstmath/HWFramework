package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanTransmitFollowupRequest {
    public final byte[] addr = new byte[6];
    public boolean disableFollowupResultIndication;
    public byte discoverySessionId;
    public final ArrayList<Byte> extendedServiceSpecificInfo = new ArrayList<>();
    public boolean isHighPriority;
    public int peerId;
    public final ArrayList<Byte> serviceSpecificInfo = new ArrayList<>();
    public boolean shouldUseDiscoveryWindow;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanTransmitFollowupRequest.class) {
            return false;
        }
        NanTransmitFollowupRequest other = (NanTransmitFollowupRequest) otherObject;
        if (this.discoverySessionId == other.discoverySessionId && this.peerId == other.peerId && HidlSupport.deepEquals(this.addr, other.addr) && this.isHighPriority == other.isHighPriority && this.shouldUseDiscoveryWindow == other.shouldUseDiscoveryWindow && HidlSupport.deepEquals(this.serviceSpecificInfo, other.serviceSpecificInfo) && HidlSupport.deepEquals(this.extendedServiceSpecificInfo, other.extendedServiceSpecificInfo) && this.disableFollowupResultIndication == other.disableFollowupResultIndication) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.discoverySessionId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.peerId))), Integer.valueOf(HidlSupport.deepHashCode(this.addr)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isHighPriority))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.shouldUseDiscoveryWindow))), Integer.valueOf(HidlSupport.deepHashCode(this.serviceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.extendedServiceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableFollowupResultIndication)))});
    }

    public final String toString() {
        return "{" + ".discoverySessionId = " + this.discoverySessionId + ", .peerId = " + this.peerId + ", .addr = " + Arrays.toString(this.addr) + ", .isHighPriority = " + this.isHighPriority + ", .shouldUseDiscoveryWindow = " + this.shouldUseDiscoveryWindow + ", .serviceSpecificInfo = " + this.serviceSpecificInfo + ", .extendedServiceSpecificInfo = " + this.extendedServiceSpecificInfo + ", .disableFollowupResultIndication = " + this.disableFollowupResultIndication + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<NanTransmitFollowupRequest> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanTransmitFollowupRequest> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanTransmitFollowupRequest _hidl_vec_element = new NanTransmitFollowupRequest();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.discoverySessionId = hwBlob.getInt8(_hidl_offset + 0);
        this.peerId = hwBlob.getInt32(_hidl_offset + 4);
        hwBlob.copyToInt8Array(_hidl_offset + 8, this.addr, 6);
        this.isHighPriority = hwBlob.getBool(_hidl_offset + 14);
        this.shouldUseDiscoveryWindow = hwBlob.getBool(_hidl_offset + 15);
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
        while (true) {
            int _hidl_index_03 = _hidl_index_0;
            if (_hidl_index_03 < _hidl_vec_size2) {
                this.extendedServiceSpecificInfo.add(Byte.valueOf(childBlob2.getInt8((long) (_hidl_index_03 * 1))));
                _hidl_index_0 = _hidl_index_03 + 1;
            } else {
                this.disableFollowupResultIndication = hwBlob.getBool(_hidl_offset + 48);
                return;
            }
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanTransmitFollowupRequest> _hidl_vec) {
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
        HwBlob hwBlob = _hidl_blob;
        hwBlob.putInt8(_hidl_offset + 0, this.discoverySessionId);
        hwBlob.putInt32(_hidl_offset + 4, this.peerId);
        hwBlob.putInt8Array(_hidl_offset + 8, this.addr);
        hwBlob.putBool(_hidl_offset + 14, this.isHighPriority);
        hwBlob.putBool(_hidl_offset + 15, this.shouldUseDiscoveryWindow);
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
        int _hidl_index_02 = 0;
        hwBlob.putBool(_hidl_offset + 32 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 1);
        while (true) {
            int _hidl_index_03 = _hidl_index_02;
            if (_hidl_index_03 < _hidl_vec_size2) {
                childBlob2.putInt8((long) (_hidl_index_03 * 1), this.extendedServiceSpecificInfo.get(_hidl_index_03).byteValue());
                _hidl_index_02 = _hidl_index_03 + 1;
            } else {
                hwBlob.putBlob(_hidl_offset + 32 + 0, childBlob2);
                hwBlob.putBool(_hidl_offset + 48, this.disableFollowupResultIndication);
                return;
            }
        }
    }
}
