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
    public ArrayList<Byte> extendedServiceSpecificInfo = new ArrayList<>();
    public int rangingIntervalMsec;
    public boolean rangingRequired;
    public ArrayList<Byte> rxMatchFilter = new ArrayList<>();
    public NanDataPathSecurityConfig securityConfig = new NanDataPathSecurityConfig();
    public ArrayList<Byte> serviceName = new ArrayList<>();
    public ArrayList<Byte> serviceSpecificInfo = new ArrayList<>();
    public byte sessionId;
    public short ttlSec;
    public ArrayList<Byte> txMatchFilter = new ArrayList<>();
    public boolean useRssiThreshold;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanDiscoveryCommonConfig.class) {
            return false;
        }
        NanDiscoveryCommonConfig other = (NanDiscoveryCommonConfig) otherObject;
        if (this.sessionId == other.sessionId && this.ttlSec == other.ttlSec && this.discoveryWindowPeriod == other.discoveryWindowPeriod && this.discoveryCount == other.discoveryCount && HidlSupport.deepEquals(this.serviceName, other.serviceName) && this.discoveryMatchIndicator == other.discoveryMatchIndicator && HidlSupport.deepEquals(this.serviceSpecificInfo, other.serviceSpecificInfo) && HidlSupport.deepEquals(this.extendedServiceSpecificInfo, other.extendedServiceSpecificInfo) && HidlSupport.deepEquals(this.rxMatchFilter, other.rxMatchFilter) && HidlSupport.deepEquals(this.txMatchFilter, other.txMatchFilter) && this.useRssiThreshold == other.useRssiThreshold && this.disableDiscoveryTerminationIndication == other.disableDiscoveryTerminationIndication && this.disableMatchExpirationIndication == other.disableMatchExpirationIndication && this.disableFollowupReceivedIndication == other.disableFollowupReceivedIndication && HidlSupport.deepEquals(this.securityConfig, other.securityConfig) && this.rangingRequired == other.rangingRequired && this.rangingIntervalMsec == other.rangingIntervalMsec && HidlSupport.deepEquals(Integer.valueOf(this.configRangingIndications), Integer.valueOf(other.configRangingIndications)) && this.distanceIngressCm == other.distanceIngressCm && this.distanceEgressCm == other.distanceEgressCm) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.sessionId))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.ttlSec))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.discoveryWindowPeriod))), Integer.valueOf(HidlSupport.deepHashCode(Byte.valueOf(this.discoveryCount))), Integer.valueOf(HidlSupport.deepHashCode(this.serviceName)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.discoveryMatchIndicator))), Integer.valueOf(HidlSupport.deepHashCode(this.serviceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.extendedServiceSpecificInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.rxMatchFilter)), Integer.valueOf(HidlSupport.deepHashCode(this.txMatchFilter)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.useRssiThreshold))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableDiscoveryTerminationIndication))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableMatchExpirationIndication))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.disableFollowupReceivedIndication))), Integer.valueOf(HidlSupport.deepHashCode(this.securityConfig)), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.rangingRequired))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.rangingIntervalMsec))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.configRangingIndications))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.distanceIngressCm))), Integer.valueOf(HidlSupport.deepHashCode(Short.valueOf(this.distanceEgressCm))));
    }

    public final String toString() {
        return "{.sessionId = " + ((int) this.sessionId) + ", .ttlSec = " + ((int) this.ttlSec) + ", .discoveryWindowPeriod = " + ((int) this.discoveryWindowPeriod) + ", .discoveryCount = " + ((int) this.discoveryCount) + ", .serviceName = " + this.serviceName + ", .discoveryMatchIndicator = " + NanMatchAlg.toString(this.discoveryMatchIndicator) + ", .serviceSpecificInfo = " + this.serviceSpecificInfo + ", .extendedServiceSpecificInfo = " + this.extendedServiceSpecificInfo + ", .rxMatchFilter = " + this.rxMatchFilter + ", .txMatchFilter = " + this.txMatchFilter + ", .useRssiThreshold = " + this.useRssiThreshold + ", .disableDiscoveryTerminationIndication = " + this.disableDiscoveryTerminationIndication + ", .disableMatchExpirationIndication = " + this.disableMatchExpirationIndication + ", .disableFollowupReceivedIndication = " + this.disableFollowupReceivedIndication + ", .securityConfig = " + this.securityConfig + ", .rangingRequired = " + this.rangingRequired + ", .rangingIntervalMsec = " + this.rangingIntervalMsec + ", .configRangingIndications = " + NanRangingIndication.dumpBitfield(this.configRangingIndications) + ", .distanceIngressCm = " + ((int) this.distanceIngressCm) + ", .distanceEgressCm = " + ((int) this.distanceEgressCm) + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(176), 0);
    }

    public static final ArrayList<NanDiscoveryCommonConfig> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanDiscoveryCommonConfig> _hidl_vec = new ArrayList<>();
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
        this.sessionId = _hidl_blob.getInt8(_hidl_offset + 0);
        this.ttlSec = _hidl_blob.getInt16(_hidl_offset + 2);
        this.discoveryWindowPeriod = _hidl_blob.getInt16(_hidl_offset + 4);
        this.discoveryCount = _hidl_blob.getInt8(_hidl_offset + 6);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.serviceName.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.serviceName.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        this.discoveryMatchIndicator = _hidl_blob.getInt32(_hidl_offset + 24);
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 32 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, true);
        this.serviceSpecificInfo.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.serviceSpecificInfo.add(Byte.valueOf(childBlob2.getInt8((long) (_hidl_index_02 * 1))));
        }
        int _hidl_vec_size3 = _hidl_blob.getInt32(_hidl_offset + 48 + 8);
        HwBlob childBlob3 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size3 * 1), _hidl_blob.handle(), _hidl_offset + 48 + 0, true);
        this.extendedServiceSpecificInfo.clear();
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            this.extendedServiceSpecificInfo.add(Byte.valueOf(childBlob3.getInt8((long) (_hidl_index_03 * 1))));
        }
        int _hidl_vec_size4 = _hidl_blob.getInt32(_hidl_offset + 64 + 8);
        HwBlob childBlob4 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size4 * 1), _hidl_blob.handle(), _hidl_offset + 64 + 0, true);
        this.rxMatchFilter.clear();
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
            this.rxMatchFilter.add(Byte.valueOf(childBlob4.getInt8((long) (_hidl_index_04 * 1))));
        }
        int _hidl_vec_size5 = _hidl_blob.getInt32(_hidl_offset + 80 + 8);
        HwBlob childBlob5 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size5 * 1), _hidl_blob.handle(), _hidl_offset + 80 + 0, true);
        this.txMatchFilter.clear();
        for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
            this.txMatchFilter.add(Byte.valueOf(childBlob5.getInt8((long) (_hidl_index_05 * 1))));
        }
        this.useRssiThreshold = _hidl_blob.getBool(_hidl_offset + 96);
        this.disableDiscoveryTerminationIndication = _hidl_blob.getBool(_hidl_offset + 97);
        this.disableMatchExpirationIndication = _hidl_blob.getBool(_hidl_offset + 98);
        this.disableFollowupReceivedIndication = _hidl_blob.getBool(_hidl_offset + 99);
        this.securityConfig.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 104);
        this.rangingRequired = _hidl_blob.getBool(_hidl_offset + 160);
        this.rangingIntervalMsec = _hidl_blob.getInt32(_hidl_offset + 164);
        this.configRangingIndications = _hidl_blob.getInt32(_hidl_offset + 168);
        this.distanceIngressCm = _hidl_blob.getInt16(_hidl_offset + 172);
        this.distanceEgressCm = _hidl_blob.getInt16(_hidl_offset + 174);
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
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 176));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt8(_hidl_offset + 0, this.sessionId);
        _hidl_blob.putInt16(_hidl_offset + 2, this.ttlSec);
        _hidl_blob.putInt16(_hidl_offset + 4, this.discoveryWindowPeriod);
        _hidl_blob.putInt8(_hidl_offset + 6, this.discoveryCount);
        int _hidl_vec_size = this.serviceName.size();
        _hidl_blob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), this.serviceName.get(_hidl_index_0).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 8 + 0, childBlob);
        _hidl_blob.putInt32(_hidl_offset + 24, this.discoveryMatchIndicator);
        int _hidl_vec_size2 = this.serviceSpecificInfo.size();
        _hidl_blob.putInt32(_hidl_offset + 32 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 32 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 1);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            childBlob2.putInt8((long) (_hidl_index_02 * 1), this.serviceSpecificInfo.get(_hidl_index_02).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 32 + 0, childBlob2);
        int _hidl_vec_size3 = this.extendedServiceSpecificInfo.size();
        _hidl_blob.putInt32(_hidl_offset + 48 + 8, _hidl_vec_size3);
        _hidl_blob.putBool(_hidl_offset + 48 + 12, false);
        HwBlob childBlob3 = new HwBlob(_hidl_vec_size3 * 1);
        for (int _hidl_index_03 = 0; _hidl_index_03 < _hidl_vec_size3; _hidl_index_03++) {
            childBlob3.putInt8((long) (_hidl_index_03 * 1), this.extendedServiceSpecificInfo.get(_hidl_index_03).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 48 + 0, childBlob3);
        int _hidl_vec_size4 = this.rxMatchFilter.size();
        _hidl_blob.putInt32(_hidl_offset + 64 + 8, _hidl_vec_size4);
        _hidl_blob.putBool(_hidl_offset + 64 + 12, false);
        HwBlob childBlob4 = new HwBlob(_hidl_vec_size4 * 1);
        for (int _hidl_index_04 = 0; _hidl_index_04 < _hidl_vec_size4; _hidl_index_04++) {
            childBlob4.putInt8((long) (_hidl_index_04 * 1), this.rxMatchFilter.get(_hidl_index_04).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 64 + 0, childBlob4);
        int _hidl_vec_size5 = this.txMatchFilter.size();
        _hidl_blob.putInt32(_hidl_offset + 80 + 8, _hidl_vec_size5);
        _hidl_blob.putBool(_hidl_offset + 80 + 12, false);
        HwBlob childBlob5 = new HwBlob(_hidl_vec_size5 * 1);
        for (int _hidl_index_05 = 0; _hidl_index_05 < _hidl_vec_size5; _hidl_index_05++) {
            childBlob5.putInt8((long) (_hidl_index_05 * 1), this.txMatchFilter.get(_hidl_index_05).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 80 + 0, childBlob5);
        _hidl_blob.putBool(_hidl_offset + 96, this.useRssiThreshold);
        _hidl_blob.putBool(_hidl_offset + 97, this.disableDiscoveryTerminationIndication);
        _hidl_blob.putBool(_hidl_offset + 98, this.disableMatchExpirationIndication);
        _hidl_blob.putBool(_hidl_offset + 99, this.disableFollowupReceivedIndication);
        this.securityConfig.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 104);
        _hidl_blob.putBool(_hidl_offset + 160, this.rangingRequired);
        _hidl_blob.putInt32(_hidl_offset + 164, this.rangingIntervalMsec);
        _hidl_blob.putInt32(_hidl_offset + 168, this.configRangingIndications);
        _hidl_blob.putInt16(_hidl_offset + 172, this.distanceIngressCm);
        _hidl_blob.putInt16(_hidl_offset + 174, this.distanceEgressCm);
    }
}
