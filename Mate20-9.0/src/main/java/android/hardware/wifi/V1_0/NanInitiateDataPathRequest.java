package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanInitiateDataPathRequest {
    public final ArrayList<Byte> appInfo = new ArrayList<>();
    public int channel;
    public int channelRequestType;
    public String ifaceName = new String();
    public final byte[] peerDiscMacAddr = new byte[6];
    public int peerId;
    public final NanDataPathSecurityConfig securityConfig = new NanDataPathSecurityConfig();
    public final ArrayList<Byte> serviceNameOutOfBand = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanInitiateDataPathRequest.class) {
            return false;
        }
        NanInitiateDataPathRequest other = (NanInitiateDataPathRequest) otherObject;
        if (this.peerId == other.peerId && HidlSupport.deepEquals(this.peerDiscMacAddr, other.peerDiscMacAddr) && this.channelRequestType == other.channelRequestType && this.channel == other.channel && HidlSupport.deepEquals(this.ifaceName, other.ifaceName) && HidlSupport.deepEquals(this.securityConfig, other.securityConfig) && HidlSupport.deepEquals(this.appInfo, other.appInfo) && HidlSupport.deepEquals(this.serviceNameOutOfBand, other.serviceNameOutOfBand)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.peerId))), Integer.valueOf(HidlSupport.deepHashCode(this.peerDiscMacAddr)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channelRequestType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channel))), Integer.valueOf(HidlSupport.deepHashCode(this.ifaceName)), Integer.valueOf(HidlSupport.deepHashCode(this.securityConfig)), Integer.valueOf(HidlSupport.deepHashCode(this.appInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.serviceNameOutOfBand))});
    }

    public final String toString() {
        return "{" + ".peerId = " + this.peerId + ", .peerDiscMacAddr = " + Arrays.toString(this.peerDiscMacAddr) + ", .channelRequestType = " + NanDataPathChannelCfg.toString(this.channelRequestType) + ", .channel = " + this.channel + ", .ifaceName = " + this.ifaceName + ", .securityConfig = " + this.securityConfig + ", .appInfo = " + this.appInfo + ", .serviceNameOutOfBand = " + this.serviceNameOutOfBand + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(128), 0);
    }

    public static final ArrayList<NanInitiateDataPathRequest> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanInitiateDataPathRequest> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 128), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanInitiateDataPathRequest _hidl_vec_element = new NanInitiateDataPathRequest();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 128));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.peerId = hwBlob.getInt32(_hidl_offset + 0);
        hwBlob.copyToInt8Array(_hidl_offset + 4, this.peerDiscMacAddr, 6);
        this.channelRequestType = hwBlob.getInt32(_hidl_offset + 12);
        this.channel = hwBlob.getInt32(_hidl_offset + 16);
        this.ifaceName = hwBlob.getString(_hidl_offset + 24);
        parcel.readEmbeddedBuffer((long) (this.ifaceName.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 24 + 0, false);
        HwParcel hwParcel = parcel;
        this.securityConfig.readEmbeddedFromParcel(hwParcel, hwBlob, _hidl_offset + 40);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 96 + 8);
        HwBlob childBlob = hwParcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 96 + 0, true);
        this.appInfo.clear();
        int _hidl_index_0 = 0;
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size; _hidl_index_02++) {
            this.appInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_02 * 1))));
        }
        int _hidl_vec_size2 = hwBlob.getInt32(_hidl_offset + 112 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 1), _hidl_blob.handle(), _hidl_offset + 112 + 0, true);
        this.serviceNameOutOfBand.clear();
        while (true) {
            int _hidl_index_03 = _hidl_index_0;
            if (_hidl_index_03 < _hidl_vec_size2) {
                this.serviceNameOutOfBand.add(Byte.valueOf(childBlob2.getInt8((long) (_hidl_index_03 * 1))));
                _hidl_index_0 = _hidl_index_03 + 1;
            } else {
                return;
            }
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(128);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanInitiateDataPathRequest> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 128);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 128));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        hwBlob.putInt32(_hidl_offset + 0, this.peerId);
        hwBlob.putInt8Array(_hidl_offset + 4, this.peerDiscMacAddr);
        hwBlob.putInt32(_hidl_offset + 12, this.channelRequestType);
        hwBlob.putInt32(_hidl_offset + 16, this.channel);
        hwBlob.putString(_hidl_offset + 24, this.ifaceName);
        this.securityConfig.writeEmbeddedToBlob(hwBlob, _hidl_offset + 40);
        int _hidl_vec_size = this.appInfo.size();
        hwBlob.putInt32(_hidl_offset + 96 + 8, _hidl_vec_size);
        hwBlob.putBool(_hidl_offset + 96 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), this.appInfo.get(_hidl_index_0).byteValue());
        }
        hwBlob.putBlob(_hidl_offset + 96 + 0, childBlob);
        int _hidl_vec_size2 = this.serviceNameOutOfBand.size();
        hwBlob.putInt32(_hidl_offset + 112 + 8, _hidl_vec_size2);
        int _hidl_index_02 = 0;
        hwBlob.putBool(_hidl_offset + 112 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 1);
        while (true) {
            int _hidl_index_03 = _hidl_index_02;
            if (_hidl_index_03 < _hidl_vec_size2) {
                childBlob2.putInt8((long) (_hidl_index_03 * 1), this.serviceNameOutOfBand.get(_hidl_index_03).byteValue());
                _hidl_index_02 = _hidl_index_03 + 1;
            } else {
                hwBlob.putBlob(_hidl_offset + 112 + 0, childBlob2);
                return;
            }
        }
    }
}
