package android.apex;

import android.os.Parcel;
import android.os.Parcelable;

public class HepInfo implements Parcelable {
    public static final Parcelable.Creator<HepInfo> CREATOR = new Parcelable.Creator<HepInfo>() {
        /* class android.apex.HepInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HepInfo createFromParcel(Parcel _aidl_source) {
            HepInfo _aidl_out = new HepInfo();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public HepInfo[] newArray(int _aidl_size) {
            return new HepInfo[_aidl_size];
        }
    };
    public String packageName;
    public String packagePath;
    public long status;
    public long versionCode;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeString(this.packageName);
        _aidl_parcel.writeString(this.packagePath);
        _aidl_parcel.writeLong(this.versionCode);
        _aidl_parcel.writeLong(this.status);
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
                this.packageName = _aidl_parcel.readString();
                if (_aidl_parcel.dataPosition() - _aidl_start_pos < _aidl_parcelable_size) {
                    this.packagePath = _aidl_parcel.readString();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.versionCode = _aidl_parcel.readLong();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.status = _aidl_parcel.readLong();
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
