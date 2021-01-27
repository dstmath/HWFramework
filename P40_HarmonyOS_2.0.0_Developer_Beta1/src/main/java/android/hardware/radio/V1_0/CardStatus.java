package android.hardware.radio.V1_0;

import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class CardStatus {
    public ArrayList<AppStatus> applications = new ArrayList<>();
    public int cardState;
    public int cdmaSubscriptionAppIndex;
    public int gsmUmtsSubscriptionAppIndex;
    public int imsSubscriptionAppIndex;
    public int universalPinState;

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != CardStatus.class) {
            return false;
        }
        CardStatus other = (CardStatus) otherObject;
        if (this.cardState == other.cardState && this.universalPinState == other.universalPinState && this.gsmUmtsSubscriptionAppIndex == other.gsmUmtsSubscriptionAppIndex && this.cdmaSubscriptionAppIndex == other.cdmaSubscriptionAppIndex && this.imsSubscriptionAppIndex == other.imsSubscriptionAppIndex && HidlSupport.deepEquals(this.applications, other.applications)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cardState))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.universalPinState))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.gsmUmtsSubscriptionAppIndex))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.cdmaSubscriptionAppIndex))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.imsSubscriptionAppIndex))), Integer.valueOf(HidlSupport.deepHashCode(this.applications)));
    }

    public final String toString() {
        return "{.cardState = " + CardState.toString(this.cardState) + ", .universalPinState = " + PinState.toString(this.universalPinState) + ", .gsmUmtsSubscriptionAppIndex = " + this.gsmUmtsSubscriptionAppIndex + ", .cdmaSubscriptionAppIndex = " + this.cdmaSubscriptionAppIndex + ", .imsSubscriptionAppIndex = " + this.imsSubscriptionAppIndex + ", .applications = " + this.applications + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(40), 0);
    }

    public static final ArrayList<CardStatus> readVectorFromParcel(HwParcel parcel) {
        ArrayList<CardStatus> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 40), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            CardStatus _hidl_vec_element = new CardStatus();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 40));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.cardState = _hidl_blob.getInt32(_hidl_offset + 0);
        this.universalPinState = _hidl_blob.getInt32(_hidl_offset + 4);
        this.gsmUmtsSubscriptionAppIndex = _hidl_blob.getInt32(_hidl_offset + 8);
        this.cdmaSubscriptionAppIndex = _hidl_blob.getInt32(_hidl_offset + 12);
        this.imsSubscriptionAppIndex = _hidl_blob.getInt32(_hidl_offset + 16);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 24 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 64), _hidl_blob.handle(), _hidl_offset + 24 + 0, true);
        this.applications.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            AppStatus _hidl_vec_element = new AppStatus();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * 64));
            this.applications.add(_hidl_vec_element);
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob(40);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<CardStatus> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 40);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 40));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putInt32(_hidl_offset + 0, this.cardState);
        _hidl_blob.putInt32(4 + _hidl_offset, this.universalPinState);
        _hidl_blob.putInt32(_hidl_offset + 8, this.gsmUmtsSubscriptionAppIndex);
        _hidl_blob.putInt32(_hidl_offset + 12, this.cdmaSubscriptionAppIndex);
        _hidl_blob.putInt32(16 + _hidl_offset, this.imsSubscriptionAppIndex);
        int _hidl_vec_size = this.applications.size();
        _hidl_blob.putInt32(_hidl_offset + 24 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 24 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 64);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.applications.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * 64));
        }
        _hidl_blob.putBlob(24 + _hidl_offset + 0, childBlob);
    }
}
