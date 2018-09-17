package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public final class NanInitiateDataPathRequest {
    public final ArrayList<Byte> appInfo = new ArrayList();
    public int channel;
    public int channelRequestType;
    public String ifaceName = new String();
    public final byte[] peerDiscMacAddr = new byte[6];
    public int peerId;
    public final NanDataPathSecurityConfig securityConfig = new NanDataPathSecurityConfig();
    public final ArrayList<Byte> serviceNameOutOfBand = new ArrayList();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanInitiateDataPathRequest.class) {
            return false;
        }
        NanInitiateDataPathRequest other = (NanInitiateDataPathRequest) otherObject;
        return this.peerId == other.peerId && HidlSupport.deepEquals(this.peerDiscMacAddr, other.peerDiscMacAddr) && this.channelRequestType == other.channelRequestType && this.channel == other.channel && HidlSupport.deepEquals(this.ifaceName, other.ifaceName) && HidlSupport.deepEquals(this.securityConfig, other.securityConfig) && HidlSupport.deepEquals(this.appInfo, other.appInfo) && HidlSupport.deepEquals(this.serviceNameOutOfBand, other.serviceNameOutOfBand);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.peerId))), Integer.valueOf(HidlSupport.deepHashCode(this.peerDiscMacAddr)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channelRequestType))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.channel))), Integer.valueOf(HidlSupport.deepHashCode(this.ifaceName)), Integer.valueOf(HidlSupport.deepHashCode(this.securityConfig)), Integer.valueOf(HidlSupport.deepHashCode(this.appInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.serviceNameOutOfBand))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".peerId = ");
        builder.append(this.peerId);
        builder.append(", .peerDiscMacAddr = ");
        builder.append(Arrays.toString(this.peerDiscMacAddr));
        builder.append(", .channelRequestType = ");
        builder.append(NanDataPathChannelCfg.toString(this.channelRequestType));
        builder.append(", .channel = ");
        builder.append(this.channel);
        builder.append(", .ifaceName = ");
        builder.append(this.ifaceName);
        builder.append(", .securityConfig = ");
        builder.append(this.securityConfig);
        builder.append(", .appInfo = ");
        builder.append(this.appInfo);
        builder.append(", .serviceNameOutOfBand = ");
        builder.append(this.serviceNameOutOfBand);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(128), 0);
    }

    public static final ArrayList<NanInitiateDataPathRequest> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanInitiateDataPathRequest> _hidl_vec = new ArrayList();
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
        int _hidl_index_0;
        this.peerId = _hidl_blob.getInt32(0 + _hidl_offset);
        long _hidl_array_offset_0 = _hidl_offset + 4;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            this.peerDiscMacAddr[_hidl_index_0_0] = _hidl_blob.getInt8(_hidl_array_offset_0);
            _hidl_array_offset_0++;
        }
        this.channelRequestType = _hidl_blob.getInt32(12 + _hidl_offset);
        this.channel = _hidl_blob.getInt32(16 + _hidl_offset);
        this.ifaceName = _hidl_blob.getString(24 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.ifaceName.getBytes().length + 1), _hidl_blob.handle(), (24 + _hidl_offset) + 0, false);
        this.securityConfig.readEmbeddedFromParcel(parcel, _hidl_blob, 40 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((96 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (96 + _hidl_offset) + 0, true);
        this.appInfo.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.appInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        _hidl_vec_size = _hidl_blob.getInt32((112 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (112 + _hidl_offset) + 0, true);
        this.serviceNameOutOfBand.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.serviceNameOutOfBand.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
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
            ((NanInitiateDataPathRequest) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 128));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt32(0 + _hidl_offset, this.peerId);
        long _hidl_array_offset_0 = _hidl_offset + 4;
        for (int _hidl_index_0_0 = 0; _hidl_index_0_0 < 6; _hidl_index_0_0++) {
            _hidl_blob.putInt8(_hidl_array_offset_0, this.peerDiscMacAddr[_hidl_index_0_0]);
            _hidl_array_offset_0++;
        }
        _hidl_blob.putInt32(12 + _hidl_offset, this.channelRequestType);
        _hidl_blob.putInt32(16 + _hidl_offset, this.channel);
        _hidl_blob.putString(24 + _hidl_offset, this.ifaceName);
        this.securityConfig.writeEmbeddedToBlob(_hidl_blob, 40 + _hidl_offset);
        int _hidl_vec_size = this.appInfo.size();
        _hidl_blob.putInt32((96 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((96 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.appInfo.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((96 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.serviceNameOutOfBand.size();
        _hidl_blob.putInt32((112 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((112 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 1);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.serviceNameOutOfBand.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((112 + _hidl_offset) + 0, childBlob);
    }
}
