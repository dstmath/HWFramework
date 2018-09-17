package android.security.keymaster;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class OperationResult implements Parcelable {
    public static final Creator<OperationResult> CREATOR = new Creator<OperationResult>() {
        public OperationResult createFromParcel(Parcel in) {
            return new OperationResult(in);
        }

        public OperationResult[] newArray(int length) {
            return new OperationResult[length];
        }
    };
    public final int inputConsumed;
    public final long operationHandle;
    public final KeymasterArguments outParams;
    public final byte[] output;
    public final int resultCode;
    public final IBinder token;

    public OperationResult(int resultCode, IBinder token, long operationHandle, int inputConsumed, byte[] output, KeymasterArguments outParams) {
        this.resultCode = resultCode;
        this.token = token;
        this.operationHandle = operationHandle;
        this.inputConsumed = inputConsumed;
        this.output = output;
        this.outParams = outParams;
    }

    protected OperationResult(Parcel in) {
        this.resultCode = in.readInt();
        this.token = in.readStrongBinder();
        this.operationHandle = in.readLong();
        this.inputConsumed = in.readInt();
        this.output = in.createByteArray();
        this.outParams = (KeymasterArguments) KeymasterArguments.CREATOR.createFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.resultCode);
        out.writeStrongBinder(this.token);
        out.writeLong(this.operationHandle);
        out.writeInt(this.inputConsumed);
        out.writeByteArray(this.output);
        this.outParams.writeToParcel(out, flags);
    }
}
