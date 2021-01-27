package android.view;

import android.annotation.UnsupportedAppUsage;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class AppTransitionAnimationSpec implements Parcelable {
    public static final Parcelable.Creator<AppTransitionAnimationSpec> CREATOR = new Parcelable.Creator<AppTransitionAnimationSpec>() {
        /* class android.view.AppTransitionAnimationSpec.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppTransitionAnimationSpec createFromParcel(Parcel in) {
            return new AppTransitionAnimationSpec(in);
        }

        @Override // android.os.Parcelable.Creator
        public AppTransitionAnimationSpec[] newArray(int size) {
            return new AppTransitionAnimationSpec[size];
        }
    };
    public final GraphicBuffer buffer;
    public final Rect rect;
    public final int taskId;

    @UnsupportedAppUsage
    public AppTransitionAnimationSpec(int taskId2, GraphicBuffer buffer2, Rect rect2) {
        this.taskId = taskId2;
        this.rect = rect2;
        this.buffer = buffer2;
    }

    public AppTransitionAnimationSpec(Parcel in) {
        this.taskId = in.readInt();
        this.rect = (Rect) in.readParcelable(null);
        this.buffer = (GraphicBuffer) in.readParcelable(null);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.taskId);
        dest.writeParcelable(this.rect, 0);
        dest.writeParcelable(this.buffer, 0);
    }

    public String toString() {
        return "{taskId: " + this.taskId + ", buffer: " + this.buffer + ", rect: " + this.rect + "}";
    }
}
