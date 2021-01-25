package android.security.keymaster;

import android.annotation.UnsupportedAppUsage;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class OperationResult implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<OperationResult> CREATOR = new Parcelable.Creator<OperationResult>() {
        /* class android.security.keymaster.OperationResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OperationResult createFromParcel(Parcel in) {
            return new OperationResult(in);
        }

        @Override // android.os.Parcelable.Creator
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

    public OperationResult(int resultCode2, IBinder token2, long operationHandle2, int inputConsumed2, byte[] output2, KeymasterArguments outParams2) {
        this.resultCode = resultCode2;
        this.token = token2;
        this.operationHandle = operationHandle2;
        this.inputConsumed = inputConsumed2;
        this.output = output2;
        this.outParams = outParams2;
    }

    public OperationResult(int resultCode2) {
        this(resultCode2, null, 0, 0, null, null);
    }

    protected OperationResult(Parcel in) {
        this.resultCode = in.readInt();
        this.token = in.readStrongBinder();
        this.operationHandle = in.readLong();
        this.inputConsumed = in.readInt();
        this.output = in.createByteArray();
        this.outParams = KeymasterArguments.CREATOR.createFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.resultCode);
        out.writeStrongBinder(this.token);
        out.writeLong(this.operationHandle);
        out.writeInt(this.inputConsumed);
        out.writeByteArray(this.output);
        this.outParams.writeToParcel(out, flags);
    }
}
