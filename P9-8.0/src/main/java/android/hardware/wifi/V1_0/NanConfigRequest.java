package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanConfigRequest {
    public final NanBandSpecificConfig[] bandSpecificConfig = new NanBandSpecificConfig[2];
    public boolean disableDiscoveryAddressChangeIndication;
    public boolean disableJoinedClusterIndication;
    public boolean disableStartedClusterIndication;
    public boolean includePublishServiceIdsInBeacon;
    public boolean includeSubscribeServiceIdsInBeacon;
    public int macAddressRandomizationIntervalSec;
    public byte masterPref;
    public byte numberOfPublishServiceIdsInBeacon;
    public byte numberOfSubscribeServiceIdsInBeacon;
    public short rssiWindowSize;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanConfigRequest.class) {
            return false;
        }
        NanConfigRequest other = (NanConfigRequest) otherObject;
        return this.masterPref == other.masterPref && this.disableDiscoveryAddressChangeIndication == other.disableDiscoveryAddressChangeIndication && this.disableStartedClusterIndication == other.disableStartedClusterIndication && this.disableJoinedClusterIndication == other.disableJoinedClusterIndication && this.includePublishServiceIdsInBeacon == other.includePublishServiceIdsInBeacon && this.numberOfPublishServiceIdsInBeacon == other.numberOfPublishServiceIdsInBeacon && this.includeSubscribeServiceIdsInBeacon == other.includeSubscribeServiceIdsInBeacon && this.numberOfSubscribeServiceIdsInBeacon == other.numberOfSubscribeServiceIdsInBeacon && this.rssiWindowSize == other.rssiWindowSize && this.macAddressRandomizationIntervalSec == other.macAddressRandomizationIntervalSec && HidlSupport.deepEquals(this.bandSpecificConfig, other.bandSpecificConfig);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.masterPref))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableDiscoveryAddressChangeIndication))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableStartedClusterIndication))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableJoinedClusterIndication))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.includePublishServiceIdsInBeacon))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.numberOfPublishServiceIdsInBeacon))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.includeSubscribeServiceIdsInBeacon))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.numberOfSubscribeServiceIdsInBeacon))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.rssiWindowSize))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.macAddressRandomizationIntervalSec))), Integer.valueOf(HidlSupport.deepHashCode(this.bandSpecificConfig))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".masterPref = ");
        builder.append(this.masterPref);
        builder.append(", .disableDiscoveryAddressChangeIndication = ");
        builder.append(this.disableDiscoveryAddressChangeIndication);
        builder.append(", .disableStartedClusterIndication = ");
        builder.append(this.disableStartedClusterIndication);
        builder.append(", .disableJoinedClusterIndication = ");
        builder.append(this.disableJoinedClusterIndication);
        builder.append(", .includePublishServiceIdsInBeacon = ");
        builder.append(this.includePublishServiceIdsInBeacon);
        builder.append(", .numberOfPublishServiceIdsInBeacon = ");
        builder.append(this.numberOfPublishServiceIdsInBeacon);
        builder.append(", .includeSubscribeServiceIdsInBeacon = ");
        builder.append(this.includeSubscribeServiceIdsInBeacon);
        builder.append(", .numberOfSubscribeServiceIdsInBeacon = ");
        builder.append(this.numberOfSubscribeServiceIdsInBeacon);
        builder.append(", .rssiWindowSize = ");
        builder.append(this.rssiWindowSize);
        builder.append(", .macAddressRandomizationIntervalSec = ");
        builder.append(this.macAddressRandomizationIntervalSec);
        builder.append(", .bandSpecificConfig = ");
        builder.append(Arrays.toString(this.bandSpecificConfig));
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(32), 0);
    }

    public static final ArrayList<NanConfigRequest> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanConfigRequest> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanConfigRequest _hidl_vec_element = new NanConfigRequest();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.masterPref = _hidl_blob.getInt8(0 + _hidl_offset);
        this.disableDiscoveryAddressChangeIndication = _hidl_blob.getBool(1 + _hidl_offset);
        this.disableStartedClusterIndication = _hidl_blob.getBool(2 + _hidl_offset);
        this.disableJoinedClusterIndication = _hidl_blob.getBool(3 + _hidl_offset);
        this.includePublishServiceIdsInBeacon = _hidl_blob.getBool(4 + _hidl_offset);
        this.numberOfPublishServiceIdsInBeacon = _hidl_blob.getInt8(5 + _hidl_offset);
        this.includeSubscribeServiceIdsInBeacon = _hidl_blob.getBool(6 + _hidl_offset);
        this.numberOfSubscribeServiceIdsInBeacon = _hidl_blob.getInt8(7 + _hidl_offset);
        this.rssiWindowSize = _hidl_blob.getInt16(_hidl_offset + 8);
        this.macAddressRandomizationIntervalSec = _hidl_blob.getInt32(12 + _hidl_offset);
        long _hidl_array_offset_0 = _hidl_offset + 16;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            this.bandSpecificConfig[_hidl_index_0_0] = new NanBandSpecificConfig();
            this.bandSpecificConfig[_hidl_index_0_0].readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_array_offset_0);
            _hidl_array_offset_0 += 8;
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(32);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanConfigRequest> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 32);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((NanConfigRequest) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(0 + _hidl_offset, this.masterPref);
        _hidl_blob.putBool(1 + _hidl_offset, this.disableDiscoveryAddressChangeIndication);
        _hidl_blob.putBool(2 + _hidl_offset, this.disableStartedClusterIndication);
        _hidl_blob.putBool(3 + _hidl_offset, this.disableJoinedClusterIndication);
        _hidl_blob.putBool(4 + _hidl_offset, this.includePublishServiceIdsInBeacon);
        _hidl_blob.putInt8(5 + _hidl_offset, this.numberOfPublishServiceIdsInBeacon);
        _hidl_blob.putBool(6 + _hidl_offset, this.includeSubscribeServiceIdsInBeacon);
        _hidl_blob.putInt8(7 + _hidl_offset, this.numberOfSubscribeServiceIdsInBeacon);
        _hidl_blob.putInt16(_hidl_offset + 8, this.rssiWindowSize);
        _hidl_blob.putInt32(12 + _hidl_offset, this.macAddressRandomizationIntervalSec);
        long _hidl_array_offset_0 = _hidl_offset + 16;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 2; _hidl_index_0_0++) {
            this.bandSpecificConfig[_hidl_index_0_0].writeEmbeddedToBlob(_hidl_blob, _hidl_array_offset_0);
            _hidl_array_offset_0 += 8;
        }
    }
}
