package vendor.huawei.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class RspMsgPayload {
    public int nData;
    public final ArrayList<Integer> nDatas = new ArrayList();
    public String strData = new String();
    public final ArrayList<String> strDatas = new ArrayList();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != RspMsgPayload.class) {
            return false;
        }
        RspMsgPayload other = (RspMsgPayload) otherObject;
        return this.nData == other.nData && HidlSupport.deepEquals(this.nDatas, other.nDatas) && HidlSupport.deepEquals(this.strData, other.strData) && HidlSupport.deepEquals(this.strDatas, other.strDatas);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.nData))), Integer.valueOf(HidlSupport.deepHashCode(this.nDatas)), Integer.valueOf(HidlSupport.deepHashCode(this.strData)), Integer.valueOf(HidlSupport.deepHashCode(this.strDatas))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".nData = ");
        builder.append(this.nData);
        builder.append(", .nDatas = ");
        builder.append(this.nDatas);
        builder.append(", .strData = ");
        builder.append(this.strData);
        builder.append(", .strDatas = ");
        builder.append(this.strDatas);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(56), 0);
    }

    public static final ArrayList<RspMsgPayload> readVectorFromParcel(HwParcel parcel) {
        ArrayList<RspMsgPayload> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 56), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            RspMsgPayload _hidl_vec_element = new RspMsgPayload();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 56));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        this.nData = _hidl_blob.getInt32(0 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((8 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 4), _hidl_blob.handle(), (8 + _hidl_offset) + 0, true);
        this.nDatas.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.nDatas.add(Integer.valueOf(childBlob.getInt32((long) (_hidl_index_0 * 4))));
        }
        this.strData = _hidl_blob.getString(24 + _hidl_offset);
        parcel.readEmbeddedBuffer((long) (this.strData.getBytes().length + 1), _hidl_blob.handle(), (24 + _hidl_offset) + 0, false);
        _hidl_vec_size = _hidl_blob.getInt32((40 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 16), _hidl_blob.handle(), (40 + _hidl_offset) + 0, true);
        this.strDatas.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            String _hidl_vec_element = new String();
            _hidl_vec_element = childBlob.getString((long) (_hidl_index_0 * 16));
            HwParcel hwParcel = parcel;
            hwParcel.readEmbeddedBuffer((long) (_hidl_vec_element.getBytes().length + 1), childBlob.handle(), (long) ((_hidl_index_0 * 16) + 0), false);
            this.strDatas.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(56);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<RspMsgPayload> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 56);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((RspMsgPayload) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 56));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt32(0 + _hidl_offset, this.nData);
        int _hidl_vec_size = this.nDatas.size();
        _hidl_blob.putInt32((8 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((8 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 4);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt32((long) (_hidl_index_0 * 4), ((Integer) this.nDatas.get(_hidl_index_0)).intValue());
        }
        _hidl_blob.putBlob((8 + _hidl_offset) + 0, childBlob);
        _hidl_blob.putString(24 + _hidl_offset, this.strData);
        _hidl_vec_size = this.strDatas.size();
        _hidl_blob.putInt32((40 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((40 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 16);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putString((long) (_hidl_index_0 * 16), (String) this.strDatas.get(_hidl_index_0));
        }
        _hidl_blob.putBlob((40 + _hidl_offset) + 0, childBlob);
    }
}
