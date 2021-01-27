package android.apex;

import android.os.Parcel;
import android.os.Parcelable;

public class HepInfoList implements Parcelable {
    public static final Parcelable.Creator<HepInfoList> CREATOR = new Parcelable.Creator<HepInfoList>() {
        /* class android.apex.HepInfoList.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HepInfoList createFromParcel(Parcel _aidl_source) {
            HepInfoList _aidl_out = new HepInfoList();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public HepInfoList[] newArray(int _aidl_size) {
            return new HepInfoList[_aidl_size];
        }
    };
    public HepInfo[] hepInfos;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeTypedArray(this.hepInfos, 0);
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
                this.hepInfos = (HepInfo[]) _aidl_parcel.createTypedArray(HepInfo.CREATOR);
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
