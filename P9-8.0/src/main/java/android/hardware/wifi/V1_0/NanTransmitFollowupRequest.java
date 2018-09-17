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
    public final ArrayList<Byte> extendedServiceSpecificInfo = new ArrayList();
    public boolean isHighPriority;
    public int peerId;
    public final ArrayList<Byte> serviceSpecificInfo = new ArrayList();
    public boolean shouldUseDiscoveryWindow;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanTransmitFollowupRequest.class) {
            return false;
        }
        NanTransmitFollowupRequest other = (NanTransmitFollowupRequest) otherObject;
        return this.discoverySessionId == other.discoverySessionId && this.peerId == other.peerId && HidlSupport.deepEquals(this.addr, other.addr) && this.isHighPriority == other.isHighPriority && this.shouldUseDiscoveryWindow == other.shouldUseDiscoveryWindow && HidlSupport.deepEquals(this.serviceSpecificInfo, other.serviceSpecificInfo) && HidlSupport.deepEquals(this.extendedServiceSpecificInfo, other.extendedServiceSpecificInfo) && this.disableFollowupResultIndication == other.disableFollowupResultIndication;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.discoverySessionId))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.peerId))), Integer.valueOf(HidlSupport.deepHashCode(this.addr)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isHighPriority))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.shouldUseDiscoveryWindow))), Integer.valueOf(HidlSupport.deepHashCode(this.serviceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.extendedServiceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableFollowupResultIndication)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".discoverySessionId = ");
        builder.append(this.discoverySessionId);
        builder.append(", .peerId = ");
        builder.append(this.peerId);
        builder.append(", .addr = ");
        builder.append(Arrays.toString(this.addr));
        builder.append(", .isHighPriority = ");
        builder.append(this.isHighPriority);
        builder.append(", .shouldUseDiscoveryWindow = ");
        builder.append(this.shouldUseDiscoveryWindow);
        builder.append(", .serviceSpecificInfo = ");
        builder.append(this.serviceSpecificInfo);
        builder.append(", .extendedServiceSpecificInfo = ");
        builder.append(this.extendedServiceSpecificInfo);
        builder.append(", .disableFollowupResultIndication = ");
        builder.append(this.disableFollowupResultIndication);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<NanTransmitFollowupRequest> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanTransmitFollowupRequest> _hidl_vec = new ArrayList();
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
        int _hidl_index_0;
        this.discoverySessionId = _hidl_blob.getInt8(0 + _hidl_offset);
        this.peerId = _hidl_blob.getInt32(4 + _hidl_offset);
        long _hidl_array_offset_0 = _hidl_offset + 8;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            this.addr[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        this.isHighPriority = _hidl_blob.getBool(14 + _hidl_offset);
        this.shouldUseDiscoveryWindow = _hidl_blob.getBool(15 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((16 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (16 + _hidl_offset) + 0, true);
        this.serviceSpecificInfo.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.serviceSpecificInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        _hidl_vec_size = _hidl_blob.getInt32((32 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (32 + _hidl_offset) + 0, true);
        this.extendedServiceSpecificInfo.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.extendedServiceSpecificInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        this.disableFollowupResultIndication = _hidl_blob.getBool(48 + _hidl_offset);
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
            ((NanTransmitFollowupRequest) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt8(0 + _hidl_offset, this.discoverySessionId);
        _hidl_blob.putInt32(4 + _hidl_offset, this.peerId);
        long _hidl_array_offset_0 = _hidl_offset + 8;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.addr[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_blob.putBool(14 + _hidl_offset, this.isHighPriority);
        _hidl_blob.putBool(15 + _hidl_offset, this.shouldUseDiscoveryWindow);
        int _hidl_vec_size = this.serviceSpecificInfo.size();
        _hidl_blob.putInt32((16 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((16 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.serviceSpecificInfo.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((16 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.extendedServiceSpecificInfo.size();
        _hidl_blob.putInt32((32 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((32 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.extendedServiceSpecificInfo.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((32 + _hidl_offset) + 0, childBlob);
        _hidl_blob.putBool(48 + _hidl_offset, this.disableFollowupResultIndication);
    }
}
