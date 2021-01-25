package android.net;

import android.os.Parcel;
import android.os.Parcelable;

public class ResolverParamsParcel implements Parcelable {
    public static final Parcelable.Creator<ResolverParamsParcel> CREATOR = new Parcelable.Creator<ResolverParamsParcel>() {
        /* class android.net.ResolverParamsParcel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ResolverParamsParcel createFromParcel(Parcel _aidl_source) {
            ResolverParamsParcel _aidl_out = new ResolverParamsParcel();
            _aidl_out.readFromParcel(_aidl_source);
            return _aidl_out;
        }

        @Override // android.os.Parcelable.Creator
        public ResolverParamsParcel[] newArray(int _aidl_size) {
            return new ResolverParamsParcel[_aidl_size];
        }
    };
    public int baseTimeoutMsec;
    public String[] domains;
    public int maxSamples;
    public int minSamples;
    public int netId;
    public int retryCount;
    public int sampleValiditySeconds;
    public String[] servers;
    public int successThreshold;
    public String[] tlsFingerprints;
    public String tlsName;
    public String[] tlsServers;

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel _aidl_parcel, int _aidl_flag) {
        int _aidl_start_pos = _aidl_parcel.dataPosition();
        _aidl_parcel.writeInt(0);
        _aidl_parcel.writeInt(this.netId);
        _aidl_parcel.writeInt(this.sampleValiditySeconds);
        _aidl_parcel.writeInt(this.successThreshold);
        _aidl_parcel.writeInt(this.minSamples);
        _aidl_parcel.writeInt(this.maxSamples);
        _aidl_parcel.writeInt(this.baseTimeoutMsec);
        _aidl_parcel.writeInt(this.retryCount);
        _aidl_parcel.writeStringArray(this.servers);
        _aidl_parcel.writeStringArray(this.domains);
        _aidl_parcel.writeString(this.tlsName);
        _aidl_parcel.writeStringArray(this.tlsServers);
        _aidl_parcel.writeStringArray(this.tlsFingerprints);
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
                this.netId = _aidl_parcel.readInt();
                if (_aidl_parcel.dataPosition() - _aidl_start_pos < _aidl_parcelable_size) {
                    this.sampleValiditySeconds = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.successThreshold = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.minSamples = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.maxSamples = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.baseTimeoutMsec = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.retryCount = _aidl_parcel.readInt();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.servers = _aidl_parcel.createStringArray();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.domains = _aidl_parcel.createStringArray();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.tlsName = _aidl_parcel.readString();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.tlsServers = _aidl_parcel.createStringArray();
                    if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) {
                        _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
                        return;
                    }
                    this.tlsFingerprints = _aidl_parcel.createStringArray();
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
