package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WifiDebugPacketFateFrameInfo {
    public long driverTimestampUsec;
    public long firmwareTimestampUsec;
    public ArrayList<Byte> frameContent = new ArrayList<>();
    public long frameLen;
    public int frameType;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != WifiDebugPacketFateFrameInfo.class) {
            return false;
        }
        WifiDebugPacketFateFrameInfo other = (WifiDebugPacketFateFrameInfo) otherObject;
        if (this.frameType == other.frameType && this.frameLen == other.frameLen && this.driverTimestampUsec == other.driverTimestampUsec && this.firmwareTimestampUsec == other.firmwareTimestampUsec && HidlSupport.deepEquals(this.frameContent, other.frameContent)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.frameType))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.frameLen))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.driverTimestampUsec))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.firmwareTimestampUsec))), Integer.valueOf(HidlSupport.deepHashCode(this.frameContent)));
    }

    public final String toString() {
        return "{.frameType = " + WifiDebugPacketFateFrameType.toString(this.frameType) + ", .frameLen = " + this.frameLen + ", .driverTimestampUsec = " + this.driverTimestampUsec + ", .firmwareTimestampUsec = " + this.firmwareTimestampUsec + ", .frameContent = " + this.frameContent + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<WifiDebugPacketFateFrameInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiDebugPacketFateFrameInfo> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            WifiDebugPacketFateFrameInfo _hidl_vec_element = new WifiDebugPacketFateFrameInfo();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.frameType = _hidl_blob.getInt32(_hidl_offset + 0);
        this.frameLen = _hidl_blob.getInt64(_hidl_offset + 8);
        this.driverTimestampUsec = _hidl_blob.getInt64(_hidl_offset + 16);
        this.firmwareTimestampUsec = _hidl_blob.getInt64(_hidl_offset + 24);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 32 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 32 + 0, true);
        this.frameContent.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.frameContent.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<WifiDebugPacketFateFrameInfo> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.frameType);
        _hidl_blob.putInt64(_hidl_offset + 8, this.frameLen);
        _hidl_blob.putInt64(16 + _hidl_offset, this.driverTimestampUsec);
        _hidl_blob.putInt64(24 + _hidl_offset, this.firmwareTimestampUsec);
        int _hidl_vec_size = this.frameContent.size();
        _hidl_blob.putInt32(_hidl_offset + 32 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 32 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), this.frameContent.get(_hidl_index_0).byteValue());
        }
        _hidl_blob.putBlob(32 + _hidl_offset + 0, childBlob);
    }
}
