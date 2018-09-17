package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class ImsSmsMessage {
    public final ArrayList<CdmaSmsMessage> cdmaMessage = new ArrayList();
    public final ArrayList<GsmSmsMessage> gsmMessage = new ArrayList();
    public int messageRef;
    public boolean retry;
    public int tech;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != ImsSmsMessage.class) {
            return false;
        }
        ImsSmsMessage other = (ImsSmsMessage) otherObject;
        return this.tech == other.tech && this.retry == other.retry && this.messageRef == other.messageRef && HidlSupport.deepEquals(this.cdmaMessage, other.cdmaMessage) && HidlSupport.deepEquals(this.gsmMessage, other.gsmMessage);
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.tech))), Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.retry))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.messageRef))), Integer.valueOf(HidlSupport.deepHashCode(this.cdmaMessage)), Integer.valueOf(HidlSupport.deepHashCode(this.gsmMessage))});
    }

    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(".tech = ");
        builder.append(RadioTechnologyFamily.toString(this.tech));
        builder.append(", .retry = ");
        builder.append(this.retry);
        builder.append(", .messageRef = ");
        builder.append(this.messageRef);
        builder.append(", .cdmaMessage = ");
        builder.append(this.cdmaMessage);
        builder.append(", .gsmMessage = ");
        builder.append(this.gsmMessage);
        builder.append("}");
        return builder.toString();
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(48), 0);
    }

    public static final ArrayList<ImsSmsMessage> readVectorFromParcel(HwParcel parcel) {
        ArrayList<ImsSmsMessage> _hidl_vec = new ArrayList();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 48), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ImsSmsMessage _hidl_vec_element = new ImsSmsMessage();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 48));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        this.tech = _hidl_blob.getInt32(0 + _hidl_offset);
        this.retry = _hidl_blob.getBool(4 + _hidl_offset);
        this.messageRef = _hidl_blob.getInt32(8 + _hidl_offset);
        int _hidl_vec_size = _hidl_blob.getInt32((16 + _hidl_offset) + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 88), _hidl_blob.handle(), (16 + _hidl_offset) + 0, true);
        this.cdmaMessage.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CdmaSmsMessage _hidl_vec_element = new CdmaSmsMessage();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 88));
            this.cdmaMessage.add(_hidl_vec_element);
        }
        _hidl_vec_size = _hidl_blob.getInt32((32 + _hidl_offset) + 8);
        childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), (32 + _hidl_offset) + 0, true);
        this.gsmMessage.clear();
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            GsmSmsMessage _hidl_vec_element2 = new GsmSmsMessage();
            _hidl_vec_element2.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 32));
            this.gsmMessage.add(_hidl_vec_element2);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(48);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<ImsSmsMessage> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 48);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((ImsSmsMessage) _hidl_vec.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 48));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        int _hidl_index_0;
        _hidl_blob.putInt32(0 + _hidl_offset, this.tech);
        _hidl_blob.putBool(4 + _hidl_offset, this.retry);
        _hidl_blob.putInt32(8 + _hidl_offset, this.messageRef);
        int _hidl_vec_size = this.cdmaMessage.size();
        _hidl_blob.putInt32((16 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((16 + _hidl_offset) + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 88);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((CdmaSmsMessage) this.cdmaMessage.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 88));
        }
        _hidl_blob.putBlob((16 + _hidl_offset) + 0, childBlob);
        _hidl_vec_size = this.gsmMessage.size();
        _hidl_blob.putInt32((32 + _hidl_offset) + 8, _hidl_vec_size);
        _hidl_blob.putBool((32 + _hidl_offset) + 12, false);
        childBlob = new HwBlob(_hidl_vec_size * 32);
        for (_hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            ((GsmSmsMessage) this.gsmMessage.get(_hidl_index_0)).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 32));
        }
        _hidl_blob.putBlob((32 + _hidl_offset) + 0, childBlob);
    }
}
