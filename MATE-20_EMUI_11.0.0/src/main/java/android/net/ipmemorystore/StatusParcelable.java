package android.net.ipmemorystore;

import android.os.Parcel;
import android.os.Parcelable;

public class StatusParcelable implements Parcelable {
    public static final Parcelable.Creator<StatusParcelable> CREATOR = new Parcelable.Creator<StatusParcelable>() {
        /* class android.net.ipmemorystore.StatusParcelable.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public StatusParcelable createFromParcel(Parcel _aidl_source) {
            StatusParcelable _aidl_out = new StatusParcelable();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public StatusParcelable[] newArray(int _aidl_size) {
            return new StatusParcelable[_aidl_size];
        }
    };
    public int resultCode;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeInt(this.resultCode);
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
                this.resultCode = _aidl_parcel.readInt();
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
