package ohos.nfc.tag;

import java.util.Arrays;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class ResponseInfo implements Sequenceable {
    private static final int RESULT_EXCEEDED_LENGTH = 3;
    private static final int RESULT_FAILURE = 1;
    private static final int RESULT_SUCCESS = 0;
    private static final int RESULT_TAGLOST = 2;
    private byte[] mResponseData;
    private int mResponseLength;
    private int mResult;

    public byte[] getResponse() {
        int i = this.mResult;
        if (i == 0) {
            byte[] bArr = this.mResponseData;
            return bArr != null ? Arrays.copyOf(bArr, bArr.length) : new byte[0];
        } else if (i != 2) {
            return i != 3 ? new byte[0] : new byte[0];
        } else {
            return new byte[0];
        }
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.mResult);
        parcel.writeInt(this.mResponseLength);
        parcel.writeByteArray(this.mResponseData);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.mResult = parcel.readInt();
        this.mResponseLength = parcel.readInt();
        this.mResponseData = parcel.readByteArray();
        return true;
    }
}
