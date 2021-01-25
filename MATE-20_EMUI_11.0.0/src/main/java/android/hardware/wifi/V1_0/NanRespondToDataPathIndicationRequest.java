package android.hardware.wifi.V1_0;

import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback;
import android.os.HidlSupport;
import android.os.HwBlob;
import android.os.HwParcel;
import java.util.ArrayList;
import java.util.Objects;

public final class NanRespondToDataPathIndicationRequest {
    public boolean acceptRequest;
    public ArrayList<Byte> appInfo = new ArrayList<>();
    public String ifaceName = new String();
    public int ndpInstanceId;
    public NanDataPathSecurityConfig securityConfig = new NanDataPathSecurityConfig();
    public ArrayList<Byte> serviceNameOutOfBand = new ArrayList<>();

    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null || otherObject.getClass() != NanRespondToDataPathIndicationRequest.class) {
            return false;
        }
        NanRespondToDataPathIndicationRequest other = (NanRespondToDataPathIndicationRequest) otherObject;
        if (this.acceptRequest == other.acceptRequest && this.ndpInstanceId == other.ndpInstanceId && HidlSupport.deepEquals(this.ifaceName, other.ifaceName) && HidlSupport.deepEquals(this.securityConfig, other.securityConfig) && HidlSupport.deepEquals(this.appInfo, other.appInfo) && HidlSupport.deepEquals(this.serviceNameOutOfBand, other.serviceNameOutOfBand)) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return Objects.hash(Integer.valueOf(HidlSupport.deepHashCode(Boolean.valueOf(this.acceptRequest))), Integer.valueOf(HidlSupport.deepHashCode(Integer.valueOf(this.ndpInstanceId))), Integer.valueOf(HidlSupport.deepHashCode(this.ifaceName)), Integer.valueOf(HidlSupport.deepHashCode(this.securityConfig)), Integer.valueOf(HidlSupport.deepHashCode(this.appInfo)), Integer.valueOf(HidlSupport.deepHashCode(this.serviceNameOutOfBand)));
    }

    public final String toString() {
        return "{.acceptRequest = " + this.acceptRequest + ", .ndpInstanceId = " + this.ndpInstanceId + ", .ifaceName = " + this.ifaceName + ", .securityConfig = " + this.securityConfig + ", .appInfo = " + this.appInfo + ", .serviceNameOutOfBand = " + this.serviceNameOutOfBand + "}";
    }

    public final void readFromParcel(HwParcel parcel) {
        readEmbeddedFromParcel(parcel, parcel.readBuffer(112), 0);
    }

    public static final ArrayList<NanRespondToDataPathIndicationRequest> readVectorFromParcel(HwParcel parcel) {
        ArrayList<NanRespondToDataPathIndicationRequest> _hidl_vec = new ArrayList<>();
        HwBlob _hidl_blob = parcel.readBuffer(16);
        int _hidl_vec_size = _hidl_blob.getInt32(8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * ISupplicantStaIfaceCallback.StatusCode.FILS_AUTHENTICATION_FAILURE), _hidl_blob.handle(), 0, true);
        _hidl_vec.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            NanRespondToDataPathIndicationRequest _hidl_vec_element = new NanRespondToDataPathIndicationRequest();
            _hidl_vec_element.readEmbeddedFromParcel(parcel, childBlob, (long) (_hidl_index_0 * ISupplicantStaIfaceCallback.StatusCode.FILS_AUTHENTICATION_FAILURE));
            _hidl_vec.add(_hidl_vec_element);
        }
        return _hidl_vec;
    }

    public final void readEmbeddedFromParcel(HwParcel parcel, HwBlob _hidl_blob, long _hidl_offset) {
        this.acceptRequest = _hidl_blob.getBool(_hidl_offset + 0);
        this.ndpInstanceId = _hidl_blob.getInt32(_hidl_offset + 4);
        this.ifaceName = _hidl_blob.getString(_hidl_offset + 8);
        parcel.readEmbeddedBuffer((long) (this.ifaceName.getBytes().length + 1), _hidl_blob.handle(), _hidl_offset + 8 + 0, false);
        this.securityConfig.readEmbeddedFromParcel(parcel, _hidl_blob, _hidl_offset + 24);
        int _hidl_vec_size = _hidl_blob.getInt32(_hidl_offset + 80 + 8);
        HwBlob childBlob = parcel.readEmbeddedBuffer((long) (_hidl_vec_size * 1), _hidl_blob.handle(), _hidl_offset + 80 + 0, true);
        this.appInfo.clear();
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            this.appInfo.add(Byte.valueOf(childBlob.getInt8((long) (_hidl_index_0 * 1))));
        }
        int _hidl_vec_size2 = _hidl_blob.getInt32(_hidl_offset + 96 + 8);
        HwBlob childBlob2 = parcel.readEmbeddedBuffer((long) (_hidl_vec_size2 * 1), _hidl_blob.handle(), _hidl_offset + 96 + 0, true);
        this.serviceNameOutOfBand.clear();
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            this.serviceNameOutOfBand.add(Byte.valueOf(childBlob2.getInt8((long) (_hidl_index_02 * 1))));
        }
    }

    public final void writeToParcel(HwParcel parcel) {
        HwBlob _hidl_blob = new HwBlob((int) ISupplicantStaIfaceCallback.StatusCode.FILS_AUTHENTICATION_FAILURE);
        writeEmbeddedToBlob(_hidl_blob, 0);
        parcel.writeBuffer(_hidl_blob);
    }

    public static final void writeVectorToParcel(HwParcel parcel, ArrayList<NanRespondToDataPathIndicationRequest> _hidl_vec) {
        HwBlob _hidl_blob = new HwBlob(16);
        int _hidl_vec_size = _hidl_vec.size();
        _hidl_blob.putInt32(8, _hidl_vec_size);
        _hidl_blob.putBool(12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * ISupplicantStaIfaceCallback.StatusCode.FILS_AUTHENTICATION_FAILURE);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            _hidl_vec.get(_hidl_index_0).writeEmbeddedToBlob(childBlob, (long) (_hidl_index_0 * ISupplicantStaIfaceCallback.StatusCode.FILS_AUTHENTICATION_FAILURE));
        }
        _hidl_blob.putBlob(0, childBlob);
        parcel.writeBuffer(_hidl_blob);
    }

    public final void writeEmbeddedToBlob(HwBlob _hidl_blob, long _hidl_offset) {
        _hidl_blob.putBool(_hidl_offset + 0, this.acceptRequest);
        _hidl_blob.putInt32(_hidl_offset + 4, this.ndpInstanceId);
        _hidl_blob.putString(_hidl_offset + 8, this.ifaceName);
        this.securityConfig.writeEmbeddedToBlob(_hidl_blob, _hidl_offset + 24);
        int _hidl_vec_size = this.appInfo.size();
        _hidl_blob.putInt32(_hidl_offset + 80 + 8, _hidl_vec_size);
        _hidl_blob.putBool(_hidl_offset + 80 + 12, false);
        HwBlob childBlob = new HwBlob(_hidl_vec_size * 1);
        for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
            childBlob.putInt8((long) (_hidl_index_0 * 1), this.appInfo.get(_hidl_index_0).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 80 + 0, childBlob);
        int _hidl_vec_size2 = this.serviceNameOutOfBand.size();
        _hidl_blob.putInt32(_hidl_offset + 96 + 8, _hidl_vec_size2);
        _hidl_blob.putBool(_hidl_offset + 96 + 12, false);
        HwBlob childBlob2 = new HwBlob(_hidl_vec_size2 * 1);
        for (int _hidl_index_02 = 0; _hidl_index_02 < _hidl_vec_size2; _hidl_index_02++) {
            childBlob2.putInt8((long) (_hidl_index_02 * 1), this.serviceNameOutOfBand.get(_hidl_index_02).byteValue());
        }
        _hidl_blob.putBlob(_hidl_offset + 96 + 0, childBlob2);
    }
}
