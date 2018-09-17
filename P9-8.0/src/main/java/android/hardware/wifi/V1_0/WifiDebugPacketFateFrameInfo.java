package android.hardware.wifi.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class WifiDebugPacketFateFrameInfo {
    public long driverTimestampUsec;
    public long firmwareTimestampUsec;
    public final ArrayList<Byte> frameContent = new ArrayList();
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
        return this.frameType == other.frameType && this.frameLen == other.frameLen && this.driverTimestampUsec == other.driverTimestampUsec && this.firmwareTimestampUsec == other.firmwareTimestampUsec && HidlSupport.deepEquals(this.frameContent, other.frameContent);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.frameType))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.frameLen))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.driverTimestampUsec))), Integer.valueOf(HidlSupport.deepHashCode(Long.valueOf(this.firmwareTimestampUsec))), Integer.valueOf(HidlSupport.deepHashCode(this.frameContent))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".frameType = ");
        builder.append(WifiDebugPacketFateFrameType.toString(this.frameType));
        builder.append(", .frameLen = ");
        builder.append(this.frameLen);
        builder.append(", .driverTimestampUsec = ");
        builder.append(this.driverTimestampUsec);
        builder.append(", .firmwareTimestampUsec = ");
        builder.append(this.firmwareTimestampUsec);
        builder.append(", .frameContent = ");
        builder.append(this.frameContent);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<WifiDebugPacketFateFrameInfo> readVectorFromParcel(HwParcel parcel) {
        ArrayList<WifiDebugPacketFateFrameInfo> _hidl_vec = new ArrayList();
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
        this.frameType = _hidl_blob.getInt32(0 + _hidl_offset);
        this.frameLen = _hidl_blob.getInt64(8 + _hidl_offset);
        this.driverTimestampUsec = _hidl_blob.getInt64(16 + _hidl_offset);
        this.firmwareTimestampUsec = _hidl_blob.getInt64(24 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((32 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (32 + _hidl_offset) + 0, true);
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
            ((WifiDebugPacketFateFrameInfo) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(0 + _hidl_offset, this.frameType);
        _hidl_blob.putInt64(8 + _hidl_offset, this.frameLen);
        _hidl_blob.putInt64(16 + _hidl_offset, this.driverTimestampUsec);
        _hidl_blob.putInt64(24 + _hidl_offset, this.firmwareTimestampUsec);
        int _hidl_vec_size = this.frameContent.size();
        _hidl_blob.putInt32((32 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((32 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.frameContent.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((32 + _hidl_offset) + 0, childBlob);
    }
}
