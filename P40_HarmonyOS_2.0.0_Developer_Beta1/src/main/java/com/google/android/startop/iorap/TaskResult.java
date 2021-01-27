package com.google.android.startop.iorap;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TaskResult implements Parcelable {
    public static final Parcelable.Creator<TaskResult> CREATOR = new Parcelable.Creator<TaskResult>() {
        /* class com.google.android.startop.iorap.TaskResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TaskResult createFromParcel(Parcel in) {
            return new TaskResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public TaskResult[] newArray(int size) {
            return new TaskResult[size];
        }
    };
    public static final int STATE_BEGAN = 0;
    public static final int STATE_COMPLETED = 2;
    public static final int STATE_ERROR = 3;
    private static final int STATE_MAX = 3;
    public static final int STATE_ONGOING = 1;
    public final int state;

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format("{state: %d}", Integer.valueOf(this.state));
    }

    @Override // java.lang.Object
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof TaskResult) {
            return equals((TaskResult) other);
        }
        return false;
    }

    private boolean equals(TaskResult other) {
        return this.state == other.state;
    }

    public TaskResult(int state2) {
        this.state = state2;
        checkConstructorArguments();
    }

    private void checkConstructorArguments() {
        CheckHelpers.checkStateInRange(this.state, 3);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.state);
    }

    private TaskResult(Parcel in) {
        this.state = in.readInt();
        checkConstructorArguments();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
