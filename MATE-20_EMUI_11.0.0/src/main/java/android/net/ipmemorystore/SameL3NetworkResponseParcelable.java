package android.net.ipmemorystore;

import android.os.Parcel;
import android.os.Parcelable;

public class SameL3NetworkResponseParcelable implements Parcelable {
    public static final Parcelable.Creator<SameL3NetworkResponseParcelable> CREATOR = new Parcelable.Creator<SameL3NetworkResponseParcelable>() {
        /* class android.net.ipmemorystore.SameL3NetworkResponseParcelable.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SameL3NetworkResponseParcelable createFromParcel(Parcel _aidl_source) {
            SameL3NetworkResponseParcelable _aidl_out = new SameL3NetworkResponseParcelable();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public SameL3NetworkResponseParcelable[] newArray(int _aidl_size) {
            return new SameL3NetworkResponseParcelable[_aidl_size];
        }
    };
    public float confidence;
    public String l2Key1;
    public String l2Key2;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeString(this.l2Key1);
        _aidl_parcel.writeString(this.l2Key2);
        _aidl_parcel.writeFloat(this.confidence);
        int _aidl_end_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.setDataPosition(_aidl_start_pos);
        _aidl_parcel.writeInt(_aidl_end_pos - _aidl_start_pos);
        _aidl_parcel.setDataPosition(_aidl_end_pos);
    }

    public final void readFromParcel(Parcel _aidl_parcel) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        int _aidl_parcelable_size = _aidl_parcel.readInt();
        if (_aidl_parcelable_size >= 0) {
            try {
                this.l2Key1 = _aidl_parcel.readString();
                if (_aidl_parcel.dataPosition() - _aidl_start_pos < _aidl_parcelable_size) {
                    this.l2Key2 = _aidl_parcel.readString();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.confidence = _aidl_parcel.readFloat();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                    } else {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                    }
                }
            } finally {
                _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
