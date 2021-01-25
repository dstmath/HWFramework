package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class PrivateDnsConfigParcel implements Parcelable {
    public static final Parcelable.Creator<PrivateDnsConfigParcel> CREATOR = new Parcelable.Creator<PrivateDnsConfigParcel>() {
        /* class android.net.PrivateDnsConfigParcel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PrivateDnsConfigParcel createFromParcel(Parcel _aidl_source) {
            PrivateDnsConfigParcel _aidl_out = new PrivateDnsConfigParcel();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public PrivateDnsConfigParcel[] newArray(int _aidl_size) {
            return new PrivateDnsConfigParcel[_aidl_size];
        }
    };
    public String hostname;
    public String[] ips;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeString(this.hostname);
        _aidl_parcel.writeStringArray(this.ips);
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
                this.hostname = _aidl_parcel.readString();
                if (_aidl_parcel.dataPosition() - _aidl_start_pos < _aidl_parcelable_size) {
                    this.ips = _aidl_parcel.createStringArray();
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
