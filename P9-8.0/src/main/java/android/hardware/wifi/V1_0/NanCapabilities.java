package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NanCapabilities {
    public int maxAppInfoLen;
    public int maxConcurrentClusters;
    public int maxExtendedServiceSpecificInfoLen;
    public int maxMatchFilterLen;
    public int maxNdiInterfaces;
    public int maxNdpSessions;
    public int maxPublishes;
    public int maxQueuedTransmitFollowupMsgs;
    public int maxServiceNameLen;
    public int maxServiceSpecificInfoLen;
    public int maxSubscribeInterfaceAddresses;
    public int maxSubscribes;
    public int maxTotalMatchFilterLen;
    public int supportedCipherSuites;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanCapabilities.class) {
            return false;
        }
        NanCapabilities other = (NanCapabilities) otherObject;
        return this.maxConcurrentClusters == other.maxConcurrentClusters && this.maxPublishes == other.maxPublishes && this.maxSubscribes == other.maxSubscribes && this.maxServiceNameLen == other.maxServiceNameLen && this.maxMatchFilterLen == other.maxMatchFilterLen && this.maxTotalMatchFilterLen == other.maxTotalMatchFilterLen && this.maxServiceSpecificInfoLen == other.maxServiceSpecificInfoLen && this.maxExtendedServiceSpecificInfoLen == other.maxExtendedServiceSpecificInfoLen && this.maxNdiInterfaces == other.maxNdiInterfaces && this.maxNdpSessions == other.maxNdpSessions && this.maxAppInfoLen == other.maxAppInfoLen && this.maxQueuedTransmitFollowupMsgs == other.maxQueuedTransmitFollowupMsgs && this.maxSubscribeInterfaceAddresses == other.maxSubscribeInterfaceAddresses && HidlSupport.deepEquals(Integer.valueOf(this.supportedCipherSuites), Integer.valueOf(other.supportedCipherSuites));
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxConcurrentClusters))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxPublishes))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxSubscribes))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxServiceNameLen))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxMatchFilterLen))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxTotalMatchFilterLen))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxServiceSpecificInfoLen))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxExtendedServiceSpecificInfoLen))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxNdiInterfaces))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxNdpSessions))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxAppInfoLen))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxQueuedTransmitFollowupMsgs))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxSubscribeInterfaceAddresses))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.supportedCipherSuites)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".maxConcurrentClusters = ");
        builder.append(this.maxConcurrentClusters);
        builder.append(", .maxPublishes = ");
        builder.append(this.maxPublishes);
        builder.append(", .maxSubscribes = ");
        builder.append(this.maxSubscribes);
        builder.append(", .maxServiceNameLen = ");
        builder.append(this.maxServiceNameLen);
        builder.append(", .maxMatchFilterLen = ");
        builder.append(this.maxMatchFilterLen);
        builder.append(", .maxTotalMatchFilterLen = ");
        builder.append(this.maxTotalMatchFilterLen);
        builder.append(", .maxServiceSpecificInfoLen = ");
        builder.append(this.maxServiceSpecificInfoLen);
        builder.append(", .maxExtendedServiceSpecificInfoLen = ");
        builder.append(this.maxExtendedServiceSpecificInfoLen);
        builder.append(", .maxNdiInterfaces = ");
        builder.append(this.maxNdiInterfaces);
        builder.append(", .maxNdpSessions = ");
        builder.append(this.maxNdpSessions);
        builder.append(", .maxAppInfoLen = ");
        builder.append(this.maxAppInfoLen);
        builder.append(", .maxQueuedTransmitFollowupMsgs = ");
        builder.append(this.maxQueuedTransmitFollowupMsgs);
        builder.append(", .maxSubscribeInterfaceAddresses = ");
        builder.append(this.maxSubscribeInterfaceAddresses);
        builder.append(", .supportedCipherSuites = ");
        builder.append(NanCipherSuiteType.dumpBitfield(this.supportedCipherSuites));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<NanCapabilities> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanCapabilities> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanCapabilities _hidl_vec_element = new NanCapabilities();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.maxConcurrentClusters = _hidl_blob.getInt32(0 + _hidl_offset);
        this.maxPublishes = _hidl_blob.getInt32(4 + _hidl_offset);
        this.maxSubscribes = _hidl_blob.getInt32(8 + _hidl_offset);
        this.maxServiceNameLen = _hidl_blob.getInt32(12 + _hidl_offset);
        this.maxMatchFilterLen = _hidl_blob.getInt32(16 + _hidl_offset);
        this.maxTotalMatchFilterLen = _hidl_blob.getInt32(20 + _hidl_offset);
        this.maxServiceSpecificInfoLen = _hidl_blob.getInt32(24 + _hidl_offset);
        this.maxExtendedServiceSpecificInfoLen = _hidl_blob.getInt32(28 + _hidl_offset);
        this.maxNdiInterfaces = _hidl_blob.getInt32(32 + _hidl_offset);
        this.maxNdpSessions = _hidl_blob.getInt32(36 + _hidl_offset);
        this.maxAppInfoLen = _hidl_blob.getInt32(40 + _hidl_offset);
        this.maxQueuedTransmitFollowupMsgs = _hidl_blob.getInt32(44 + _hidl_offset);
        this.maxSubscribeInterfaceAddresses = _hidl_blob.getInt32(48 + _hidl_offset);
        this.supportedCipherSuites = _hidl_blob.getInt32(52 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanCapabilities> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((NanCapabilities) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.maxConcurrentClusters);
        _hidl_blob.putInt32(4 + _hidl_offset, this.maxPublishes);
        _hidl_blob.putInt32(8 + _hidl_offset, this.maxSubscribes);
        _hidl_blob.putInt32(12 + _hidl_offset, this.maxServiceNameLen);
        _hidl_blob.putInt32(16 + _hidl_offset, this.maxMatchFilterLen);
        _hidl_blob.putInt32(20 + _hidl_offset, this.maxTotalMatchFilterLen);
        _hidl_blob.putInt32(24 + _hidl_offset, this.maxServiceSpecificInfoLen);
        _hidl_blob.putInt32(28 + _hidl_offset, this.maxExtendedServiceSpecificInfoLen);
        _hidl_blob.putInt32(32 + _hidl_offset, this.maxNdiInterfaces);
        _hidl_blob.putInt32(36 + _hidl_offset, this.maxNdpSessions);
        _hidl_blob.putInt32(40 + _hidl_offset, this.maxAppInfoLen);
        _hidl_blob.putInt32(44 + _hidl_offset, this.maxQueuedTransmitFollowupMsgs);
        _hidl_blob.putInt32(48 + _hidl_offset, this.maxSubscribeInterfaceAddresses);
        _hidl_blob.putInt32(52 + _hidl_offset, this.supportedCipherSuites);
    }
}
