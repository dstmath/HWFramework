package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CdmaSmsMessage {
    public final CdmaSmsAddress address = new CdmaSmsAddress();
    public final ArrayList<Byte> bearerData = new ArrayList();
    public boolean isServicePresent;
    public int serviceCategory;
    public final CdmaSmsSubaddress subAddress = new CdmaSmsSubaddress();
    public int teleserviceId;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CdmaSmsMessage.class) {
            return false;
        }
        CdmaSmsMessage other = (CdmaSmsMessage) otherObject;
        return this.teleserviceId == other.teleserviceId && this.isServicePresent == other.isServicePresent && this.serviceCategory == other.serviceCategory && HidlSupport.deepEquals(this.address, other.address) && HidlSupport.deepEquals(this.subAddress, other.subAddress) && HidlSupport.deepEquals(this.bearerData, other.bearerData);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.teleserviceId))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.isServicePresent))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.serviceCategory))), Integer.valueOf(HidlSupport.deepHashCode(this.address)), Integer.valueOf(HidlSupport.deepHashCode(this.subAddress)), Integer.valueOf(HidlSupport.deepHashCode(this.bearerData))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".teleserviceId = ");
        builder.append(this.teleserviceId);
        builder.append(", .isServicePresent = ");
        builder.append(this.isServicePresent);
        builder.append(", .serviceCategory = ");
        builder.append(this.serviceCategory);
        builder.append(", .address = ");
        builder.append(this.address);
        builder.append(", .subAddress = ");
        builder.append(this.subAddress);
        builder.append(", .bearerData = ");
        builder.append(this.bearerData);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(88), 0);
    }

    public static final ArrayList<CdmaSmsMessage> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CdmaSmsMessage> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 88), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CdmaSmsMessage _hidl_vec_element = new CdmaSmsMessage();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 88));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.teleserviceId = _hidl_blob.getInt32(0 + _hidl_offset);
        this.isServicePresent = _hidl_blob.getBool(4 + _hidl_offset);
        this.serviceCategory = _hidl_blob.getInt32(8 + _hidl_offset);
        this.address.readEmbeddedFromParcel(parcel, _hidl_blob, 16 + _hidl_offset);
        this.subAddress.readEmbeddedFromParcel(parcel, _hidl_blob, 48 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((72 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), (72 + _hidl_offset) + 0, true);
        this.bearerData.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.bearerData.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(88);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CdmaSmsMessage> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 88);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CdmaSmsMessage) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 88));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.teleserviceId);
        _hidl_blob.putBool(4 + _hidl_offset, this.isServicePresent);
        _hidl_blob.putInt32(_hidl_offset + 8, this.serviceCategory);
        this.address.writeEmbeddedToBlob(_hidl_blob, 16 + _hidl_offset);
        this.subAddress.writeEmbeddedToBlob(_hidl_blob, 48 + _hidl_offset);
        int _hidl_vec_size = this.bearerData.size();
        _hidl_blob.putInt32((_hidl_offset + 72) + 8, _hidl_vec_size);
        _hidl_blob.putBool((_hidl_offset + 72) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), ((Byte) this.bearerData.get(_hidl_index_0)).byteValue());
        }
        _hidl_blob.putBlob((_hidl_offset + 72) + 0, childBlob);
    }
}
