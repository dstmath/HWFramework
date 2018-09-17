package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NanDiscoveryCommonConfig {
    public int configRangingIndications;
    public boolean disableDiscoveryTerminationIndication;
    public boolean disableFollowupReceivedIndication;
    public boolean disableMatchExpirationIndication;
    public byte discoveryCount;
    public int discoveryMatchIndicator;
    public short discoveryWindowPeriod;
    public short distanceEgressCm;
    public short distanceIngressCm;
    public final ArrayList<Byte> extendedServiceSpecificInfo = new ArrayList();
    public int rangingIntervalMsec;
    public boolean rangingRequired;
    public final ArrayList<Byte> rxMatchFilter = new ArrayList();
    public final NanDataPathSecurityConfig securityConfig = new NanDataPathSecurityConfig();
    public final ArrayList<Byte> serviceName = new ArrayList();
    public final ArrayList<Byte> serviceSpecificInfo = new ArrayList();
    public byte sessionId;
    public short ttlSec;
    public final ArrayList<Byte> txMatchFilter = new ArrayList();
    public boolean useRssiThreshold;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanDiscoveryCommonConfig.class) {
            return false;
        }
        NanDiscoveryCommonConfig other = (NanDiscoveryCommonConfig) otherObject;
        return this.sessionId == other.sessionId && this.ttlSec == other.ttlSec && this.discoveryWindowPeriod == other.discoveryWindowPeriod && this.discoveryCount == other.discoveryCount && HidlSupport.deepEquals(this.serviceName, other.serviceName) && this.discoveryMatchIndicator == other.discoveryMatchIndicator && HidlSupport.deepEquals(this.serviceSpecificInfo, other.serviceSpecificInfo) && HidlSupport.deepEquals(this.extendedServiceSpecificInfo, other.extendedServiceSpecificInfo) && HidlSupport.deepEquals(this.rxMatchFilter, other.rxMatchFilter) && HidlSupport.deepEquals(this.txMatchFilter, other.txMatchFilter) && this.useRssiThreshold == other.useRssiThreshold && this.disableDiscoveryTerminationIndication == other.disableDiscoveryTerminationIndication && this.disableMatchExpirationIndication == other.disableMatchExpirationIndication && this.disableFollowupReceivedIndication == other.disableFollowupReceivedIndication && HidlSupport.deepEquals(this.securityConfig, other.securityConfig) && this.rangingRequired == other.rangingRequired && this.rangingIntervalMsec == other.rangingIntervalMsec && HidlSupport.deepEquals(Integer.valueOf(this.configRangingIndications), Integer.valueOf(other.configRangingIndications)) && this.distanceIngressCm == other.distanceIngressCm && this.distanceEgressCm == other.distanceEgressCm;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.sessionId))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.ttlSec))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.discoveryWindowPeriod))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.discoveryCount))), Integer.valueOf(HidlSupport.deepHashCode(this.serviceName)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.discoveryMatchIndicator))), Integer.valueOf(HidlSupport.deepHashCode(this.serviceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.extendedServiceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.rxMatchFilter)), Integer.valueOf(HidlSupport.deepHashCode(this.txMatchFilter)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.useRssiThreshold))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableDiscoveryTerminationIndication))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableMatchExpirationIndication))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableFollowupReceivedIndication))), Integer.valueOf(HidlSupport.deepHashCode(this.securityConfig)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.rangingRequired))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rangingIntervalMsec))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.configRangingIndications))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.distanceIngressCm))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.distanceEgressCm)))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".sessionId = ");
        builder.append(this.sessionId);
        builder.append(", .ttlSec = ");
        builder.append(this.ttlSec);
        builder.append(", .discoveryWindowPeriod = ");
        builder.append(this.discoveryWindowPeriod);
        builder.append(", .discoveryCount = ");
        builder.append(this.discoveryCount);
        builder.append(", .serviceName = ");
        builder.append(this.serviceName);
        builder.append(", .discoveryMatchIndicator = ");
        builder.append(NanMatchAlg.toString(this.discoveryMatchIndicator));
        builder.append(", .serviceSpecificInfo = ");
        builder.append(this.serviceSpecificInfo);
        builder.append(", .extendedServiceSpecificInfo = ");
        builder.append(this.extendedServiceSpecificInfo);
        builder.append(", .rxMatchFilter = ");
        builder.append(this.rxMatchFilter);
        builder.append(", .txMatchFilter = ");
        builder.append(this.txMatchFilter);
        builder.append(", .useRssiThreshold = ");
        builder.append(this.useRssiThreshold);
        builder.append(", .disableDiscoveryTerminationIndication = ");
        builder.append(this.disableDiscoveryTerminationIndication);
        builder.append(", .disableMatchExpirationIndication = ");
        builder.append(this.disableMatchExpirationIndication);
        builder.append(", .disableFollowupReceivedIndication = ");
        builder.append(this.disableFollowupReceivedIndication);
        builder.append(", .securityConfig = ");
        builder.append(this.securityConfig);
        builder.append(", .rangingRequired = ");
        builder.append(this.rangingRequired);
        builder.append(", .rangingIntervalMsec = ");
        builder.append(this.rangingIntervalMsec);
        builder.append(", .configRangingIndications = ");
        builder.append(NanRangingIndication.dumpBitfield(this.configRangingIndications));
        builder.append(", .distanceIngressCm = ");
        builder.append(this.distanceIngressCm);
        builder.append(", .distanceEgressCm = ");
        builder.append(this.distanceEgressCm);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(176), 0);
    }

    public static final ArrayList<NanDiscoveryCommonConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanDiscoveryCommonConfig> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 176), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanDiscoveryCommonConfig _hidl_vec_element = new NanDiscoveryCommonConfig();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 176));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        this.sessionId = _hidl_blob.getInt8(0 + _hidl_offset);
        this.ttlSec = _hidl_blob.getInt16(2 + _hidl_offset);
        this.discoveryWindowPeriod = _hidl_blob.getInt16(4 + _hidl_offset);
        this.discoveryCount = _hidl_blob.getInt8(6 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((8 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (8 + _hidl_offset) + 0, true);
        this.serviceName.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.serviceName.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        this.discoveryMatchIndicator = _hidl_blob.getInt32(24 + _hidl_offset);
        _hidl_vec_size = _hidl_blob.getInt32((32 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (32 + _hidl_offset) + 0, true);
        this.serviceSpecificInfo.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.serviceSpecificInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        _hidl_vec_size = _hidl_blob.getInt32((48 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (48 + _hidl_offset) + 0, true);
        this.extendedServiceSpecificInfo.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.extendedServiceSpecificInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        _hidl_vec_size = _hidl_blob.getInt32((64 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (64 + _hidl_offset) + 0, true);
        this.rxMatchFilter.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.rxMatchFilter.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        _hidl_vec_size = _hidl_blob.getInt32((80 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (80 + _hidl_offset) + 0, true);
        this.txMatchFilter.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.txMatchFilter.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        this.useRssiThreshold = _hidl_blob.getBool(96 + _hidl_offset);
        this.disableDiscoveryTerminationIndication = _hidl_blob.getBool(97 + _hidl_offset);
        this.disableMatchExpirationIndication = _hidl_blob.getBool(98 + _hidl_offset);
        this.disableFollowupReceivedIndication = _hidl_blob.getBool(99 + _hidl_offset);
        this.securityConfig.readEmbeddedFromParcel(parcel, _hidl_blob, 104 + _hidl_offset);
        this.rangingRequired = _hidl_blob.getBool(160 + _hidl_offset);
        this.rangingIntervalMsec = _hidl_blob.getInt32(164 + _hidl_offset);
        this.configRangingIndications = _hidl_blob.getInt32(168 + _hidl_offset);
        this.distanceIngressCm = _hidl_blob.getInt16(172 + _hidl_offset);
        this.distanceEgressCm = _hidl_blob.getInt16(174 + _hidl_offset);
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(176);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanDiscoveryCommonConfig> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 176);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((NanDiscoveryCommonConfig) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 176));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt8(0 + _hidl_offset, this.sessionId);
        _hidl_blob.putInt16(2 + _hidl_offset, this.ttlSec);
        _hidl_blob.putInt16(4 + _hidl_offset, this.discoveryWindowPeriod);
        _hidl_blob.putInt8(6 + _hidl_offset, this.discoveryCount);
        int _hidl_vec_size = this.serviceName.size();
        _hidl_blob.putInt32((8 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((8 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.serviceName.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((8 + _hidl_offset) + 0, childBlob);
        _hidl_blob.putInt32(24 + _hidl_offset, this.discoveryMatchIndicator);
        _hidl_vec_size = this.serviceSpecificInfo.size();
        _hidl_blob.putInt32((32 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((32 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.serviceSpecificInfo.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((32 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.extendedServiceSpecificInfo.size();
        _hidl_blob.putInt32((48 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((48 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.extendedServiceSpecificInfo.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((48 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.rxMatchFilter.size();
        _hidl_blob.putInt32((64 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((64 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.rxMatchFilter.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((64 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.txMatchFilter.size();
        _hidl_blob.putInt32((80 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((80 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.txMatchFilter.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((80 + _hidl_offset) + 0, childBlob);
        _hidl_blob.putBool(96 + _hidl_offset, this.useRssiThreshold);
        _hidl_blob.putBool(97 + _hidl_offset, this.disableDiscoveryTerminationIndication);
        _hidl_blob.putBool(98 + _hidl_offset, this.disableMatchExpirationIndication);
        _hidl_blob.putBool(99 + _hidl_offset, this.disableFollowupReceivedIndication);
        this.securityConfig.writeEmbeddedToBlob(_hidl_blob, 104 + _hidl_offset);
        _hidl_blob.putBool(160 + _hidl_offset, this.rangingRequired);
        _hidl_blob.putInt32(164 + _hidl_offset, this.rangingIntervalMsec);
        _hidl_blob.putInt32(168 + _hidl_offset, this.configRangingIndications);
        _hidl_blob.putInt16(172 + _hidl_offset, this.distanceIngressCm);
        _hidl_blob.putInt16(174 + _hidl_offset, this.distanceEgressCm);
    }
}
