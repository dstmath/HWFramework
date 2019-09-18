package android.hardware.radio.V1_1;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class KeepaliveRequest {
    public int cid;
    public final ArrayList<Byte> destinationAddress = new ArrayList<>();
    public int destinationPort;
    public int maxKeepaliveIntervalMillis;
    public final ArrayList<Byte> sourceAddress = new ArrayList<>();
    public int sourcePort;
    public int type;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != KeepaliveRequest.class) {
            return false;
        }
        KeepaliveRequest other = (KeepaliveRequest) otherObject;
        if (this.type == other.type && HidlSupport.deepEquals(this.sourceAddress, other.sourceAddress) && this.sourcePort == other.sourcePort && HidlSupport.deepEquals(this.destinationAddress, other.destinationAddress) && this.destinationPort == other.destinationPort && this.maxKeepaliveIntervalMillis == other.maxKeepaliveIntervalMillis && this.cid == other.cid) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.type))), Integer.valueOf(HidlSupport.deepHashCode(this.sourceAddress)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.sourcePort))), Integer.valueOf(HidlSupport.deepHashCode(this.destinationAddress)), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.destinationPort))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.maxKeepaliveIntervalMillis))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cid)))});
    }

    public final String toString() {
        return "{" + ".type = " + KeepaliveType.toString(this.type) + ", .sourceAddress = " + this.sourceAddress + ", .sourcePort = " + this.sourcePort + ", .destinationAddress = " + this.destinationAddress + ", .destinationPort = " + this.destinationPort + ", .maxKeepaliveIntervalMillis = " + this.maxKeepaliveIntervalMillis + ", .cid = " + this.cid + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(64), 0);
    }

    public static final ArrayList<KeepaliveRequest> readVectorFromParcel(HwParcel parcel) {
        ArrayList<KeepaliveRequest> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            KeepaliveRequest _hidl_vec_element = new KeepaliveRequest();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        this.type = hwBlob.getInt32(_hidl_offset + 0);
        int _hidl_vec_size = hwBlob.getInt32(_hidl_offset + 8 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, true);
        this.sourceAddress.clear();
        int _hidl_index_0 = 0;
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size; _hidl_index_02++) {
            this.sourceAddress.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_02 * 1))));
        }
        this.sourcePort = hwBlob.getInt32(_hidl_offset + 24);
        int _hidl_vec_size2 = hwBlob.getInt32(_hidl_offset + 32 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, true);
        this.destinationAddress.clear();
        while (true) {
            int _hidl_index_03 = _hidl_index_0;
            if (_hidl_index_03 < _hidl_vec_size2) {
                this.destinationAddress.add(Byte.valueOf(childBlob2.getInt8((long) (_hidl_index_03 * 1))));
                _hidl_index_0 = _hidl_index_03 + 1;
            } else {
                this.destinationPort = hwBlob.getInt32(_hidl_offset + 48);
                this.maxKeepaliveIntervalMillis = hwBlob.getInt32(_hidl_offset + 52);
                this.cid = hwBlob.getInt32(_hidl_offset + 56);
                return;
            }
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(64);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<KeepaliveRequest> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        HwBlob hwBlob = _hidl_blob;
        hwBlob.putInt32(_hidl_offset + 0, this.type);
        int _hidl_vec_size = this.sourceAddress.size();
        hwBlob.putInt32(_hidl_offset + 8 + 8, _hidl_vec_size);
        hwBlob.putBool(_hidl_offset + 8 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), this.sourceAddress.get(_hidl_index_0).byteValue());
        }
        hwBlob.putBlob(_hidl_offset + 8 + 0, childBlob);
        hwBlob.putInt32(_hidl_offset + 24, this.sourcePort);
        int _hidl_vec_size2 = this.destinationAddress.size();
        hwBlob.putInt32(_hidl_offset + 32 + 8, _hidl_vec_size2);
        int _hidl_index_02 = 0;
        hwBlob.putBool(_hidl_offset + 32 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 1);
        while (true) {
            int _hidl_index_03 = _hidl_index_02;
            if (_hidl_index_03 < _hidl_vec_size2) {
                childBlob2.putInt8((long) (_hidl_index_03 * 1), this.destinationAddress.get(_hidl_index_03).byteValue());
                _hidl_index_02 = _hidl_index_03 + 1;
            } else {
                hwBlob.putBlob(_hidl_offset + 32 + 0, childBlob2);
                hwBlob.putInt32(_hidl_offset + 48, this.destinationPort);
                hwBlob.putInt32(_hidl_offset + 52, this.maxKeepaliveIntervalMillis);
                hwBlob.putInt32(_hidl_offset + 56, this.cid);
                return;
            }
        }
    }
}
