package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class UidRangeParcel implements Parcelable {
    public static final Parcelable.Creator<UidRangeParcel> CREATOR = new Parcelable.Creator<UidRangeParcel>() {
        /* class android.net.UidRangeParcel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UidRangeParcel createFromParcel(Parcel _aidl_source) {
            UidRangeParcel _aidl_out = new UidRangeParcel();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public UidRangeParcel[] newArray(int _aidl_size) {
            return new UidRangeParcel[_aidl_size];
        }
    };
    public int start;
    public int stop;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeInt(this.start);
        _aidl_parcel.writeInt(this.stop);
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
                this.start = _aidl_parcel.readInt();
                if (_aidl_parcel.dataPosition() - _aidl_start_pos < _aidl_parcelable_size) {
                    this.stop = _aidl_parcel.readInt();
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
