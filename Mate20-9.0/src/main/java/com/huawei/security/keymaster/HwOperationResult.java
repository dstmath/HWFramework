package com.huawei.security.keymaster;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class HwOperationResult implements Parcelable {
    public static final Parcelable.Creator<HwOperationResult> CREATOR = new Parcelable.Creator<HwOperationResult>() {
        public HwOperationResult createFromParcel(Parcel in) {
            return new HwOperationResult(in);
        }

        public HwOperationResult[] newArray(int length) {
            return new HwOperationResult[length];
        }
    };
    public final int inputConsumed;
    public final long operationHandle;
    public final HwKeymasterArguments outParams;
    public final byte[] output;
    public final int resultCode;
    public final IBinder token;

    public HwOperationResult(int resultCode2, IBinder token2, long operationHandle2, int inputConsumed2, byte[] output2, HwKeymasterArguments outParams2) {
        this.resultCode = resultCode2;
        this.token = token2;
        this.operationHandle = operationHandle2;
        this.inputConsumed = inputConsumed2;
        this.output = output2;
        this.outParams = outParams2;
    }

    protected HwOperationResult(Parcel in) {
        this.resultCode = in.readInt();
        this.token = in.readStrongBinder();
        this.operationHandle = in.readLong();
        this.inputConsumed = in.readInt();
        this.output = in.createByteArray();
        this.outParams = HwKeymasterArguments.CREATOR.createFromParcel(in);
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
