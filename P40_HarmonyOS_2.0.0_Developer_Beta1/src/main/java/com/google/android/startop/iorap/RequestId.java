package com.google.android.startop.iorap;

import android.os.Parcel;
import android.os.Parcelable;

public class RequestId implements Parcelable {
    public static final Parcelable.Creator<RequestId> CREATOR = new Parcelable.Creator<RequestId>() {
        /* class com.google.android.startop.iorap.RequestId.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RequestId createFromParcel(Parcel in) {
            return new RequestId(in);
        }

        @Override // android.os.Parcelable.Creator
        public RequestId[] newArray(int size) {
            return new RequestId[size];
        }
    };
    private static Object mLock = new Object();
    private static long mNextRequestId = 0;
    public final long requestId;

    public static RequestId nextValueForSequence() {
        long currentRequestId;
        synchronized (mLock) {
            currentRequestId = mNextRequestId;
            mNextRequestId++;
        }
        return new RequestId(currentRequestId);
    }

    private RequestId(long requestId2) {
        this.requestId = requestId2;
        checkConstructorArguments();
    }

    private void checkConstructorArguments() {
        if (this.requestId < 0) {
            throw new IllegalArgumentException("request id must be non-negative");
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format("{requestId: %d}", Long.valueOf(this.requestId));
    }

    @Override // java.lang.Object
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof RequestId) {
            return equals((RequestId) other);
        }
        return false;
    }

    private boolean equals(RequestId other) {
        return this.requestId == other.requestId;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.requestId);
    }

    private RequestId(Parcel in) {
        this.requestId = in.readLong();
        checkConstructorArguments();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
