package android.net.ipmemorystore;

import android.os.Parcel;
import android.os.Parcelable;

public class Blob implements Parcelable {
    public static final Parcelable.Creator<Blob> CREATOR = new Parcelable.Creator<Blob>() {
        /* class android.net.ipmemorystore.Blob.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Blob createFromParcel(Parcel _aidl_source) {
            Blob _aidl_out = new Blob();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public Blob[] newArray(int _aidl_size) {
            return new Blob[_aidl_size];
        }
    };
    public byte[] data;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeByteArray(this.data);
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
                this.data = _aidl_parcel.createByteArray();
                if (_aidl_parcel.dataPosition() - _aidl_start_pos < _aidl_parcelable_size) {
                    _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
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
