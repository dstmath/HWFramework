package android.view;

import android.annotation.UnsupportedAppUsage;
import android.app.WindowConfiguration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RemoteAnimationTarget implements Parcelable {
    public static final Parcelable.Creator<RemoteAnimationTarget> CREATOR = new Parcelable.Creator<RemoteAnimationTarget>() {
        /* class android.view.RemoteAnimationTarget.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RemoteAnimationTarget createFromParcel(Parcel in) {
            return new RemoteAnimationTarget(in);
        }

        @Override // android.os.Parcelable.Creator
        public RemoteAnimationTarget[] newArray(int size) {
            return new RemoteAnimationTarget[size];
        }
    };
    public static final int MODE_CHANGING = 2;
    public static final int MODE_CLOSING = 1;
    public static final int MODE_OPENING = 0;
    @UnsupportedAppUsage
    public final Rect clipRect;
    @UnsupportedAppUsage
    public final Rect contentInsets;
    @UnsupportedAppUsage
    public boolean isNotInRecents;
    @UnsupportedAppUsage
    public final boolean isTranslucent;
    @UnsupportedAppUsage
    public final SurfaceControl leash;
    @UnsupportedAppUsage
    public final int mode;
    @UnsupportedAppUsage
    public final Point position;
    @UnsupportedAppUsage
    public final int prefixOrderIndex;
    @UnsupportedAppUsage
    public final Rect sourceContainerBounds;
    @UnsupportedAppUsage
    public final Rect startBounds;
    @UnsupportedAppUsage
    public final SurfaceControl startLeash;
    @UnsupportedAppUsage
    public final int taskId;
    @UnsupportedAppUsage
    public final WindowConfiguration windowConfiguration;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public RemoteAnimationTarget(int taskId2, int mode2, SurfaceControl leash2, boolean isTranslucent2, Rect clipRect2, Rect contentInsets2, int prefixOrderIndex2, Point position2, Rect sourceContainerBounds2, WindowConfiguration windowConfig, boolean isNotInRecents2, SurfaceControl startLeash2, Rect startBounds2) {
        this.mode = mode2;
        this.taskId = taskId2;
        this.leash = leash2;
        this.isTranslucent = isTranslucent2;
        this.clipRect = new Rect(clipRect2);
        this.contentInsets = new Rect(contentInsets2);
        this.prefixOrderIndex = prefixOrderIndex2;
        this.position = new Point(position2);
        this.sourceContainerBounds = new Rect(sourceContainerBounds2);
        this.windowConfiguration = windowConfig;
        this.isNotInRecents = isNotInRecents2;
        this.startLeash = startLeash2;
        this.startBounds = startBounds2 == null ? null : new Rect(startBounds2);
    }

    public RemoteAnimationTarget(Parcel in) {
        this.taskId = in.readInt();
        this.mode = in.readInt();
        this.leash = (SurfaceControl) in.readParcelable(null);
        this.isTranslucent = in.readBoolean();
        this.clipRect = (Rect) in.readParcelable(null);
        this.contentInsets = (Rect) in.readParcelable(null);
        this.prefixOrderIndex = in.readInt();
        this.position = (Point) in.readParcelable(null);
        this.sourceContainerBounds = (Rect) in.readParcelable(null);
        this.windowConfiguration = (WindowConfiguration) in.readParcelable(null);
        this.isNotInRecents = in.readBoolean();
        this.startLeash = (SurfaceControl) in.readParcelable(null);
        this.startBounds = (Rect) in.readParcelable(null);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.taskId);
        dest.writeInt(this.mode);
        dest.writeParcelable(this.leash, 0);
        dest.writeBoolean(this.isTranslucent);
        dest.writeParcelable(this.clipRect, 0);
        dest.writeParcelable(this.contentInsets, 0);
        dest.writeInt(this.prefixOrderIndex);
        dest.writeParcelable(this.position, 0);
        dest.writeParcelable(this.sourceContainerBounds, 0);
        dest.writeParcelable(this.windowConfiguration, 0);
        dest.writeBoolean(this.isNotInRecents);
        dest.writeParcelable(this.startLeash, 0);
        dest.writeParcelable(this.startBounds, 0);
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mode=");
        pw.print(this.mode);
        pw.print(" taskId=");
        pw.print(this.taskId);
        pw.print(" isTranslucent=");
        pw.print(this.isTranslucent);
        pw.print(" clipRect=");
        this.clipRect.printShortString(pw);
        pw.print(" contentInsets=");
        this.contentInsets.printShortString(pw);
        pw.print(" prefixOrderIndex=");
        pw.print(this.prefixOrderIndex);
        pw.print(" position=");
        this.position.printShortString(pw);
        pw.print(" sourceContainerBounds=");
        this.sourceContainerBounds.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("windowConfiguration=");
        pw.println(this.windowConfiguration);
        pw.print(prefix);
        pw.print("leash=");
        pw.println(this.leash);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, this.taskId);
        proto.write(1120986464258L, this.mode);
        this.leash.writeToProto(proto, 1146756268035L);
        proto.write(1133871366148L, this.isTranslucent);
        this.clipRect.writeToProto(proto, 1146756268037L);
        this.contentInsets.writeToProto(proto, 1146756268038L);
        proto.write(1120986464263L, this.prefixOrderIndex);
        this.position.writeToProto(proto, 1146756268040L);
        this.sourceContainerBounds.writeToProto(proto, 1146756268041L);
        this.windowConfiguration.writeToProto(proto, 1146756268042L);
        SurfaceControl surfaceControl = this.startLeash;
        if (surfaceControl != null) {
            surfaceControl.writeToProto(proto, 1146756268043L);
        }
        Rect rect = this.startBounds;
        if (rect != null) {
            rect.writeToProto(proto, 1146756268044L);
        }
        proto.end(token);
    }
}
