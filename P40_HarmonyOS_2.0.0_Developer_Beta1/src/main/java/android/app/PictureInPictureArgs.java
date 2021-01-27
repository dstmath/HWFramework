package android.app;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Rational;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public final class PictureInPictureArgs implements Parcelable {
    public static final Parcelable.Creator<PictureInPictureArgs> CREATOR = new Parcelable.Creator<PictureInPictureArgs>() {
        /* class android.app.PictureInPictureArgs.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PictureInPictureArgs createFromParcel(Parcel in) {
            return new PictureInPictureArgs(in);
        }

        @Override // android.os.Parcelable.Creator
        public PictureInPictureArgs[] newArray(int size) {
            return new PictureInPictureArgs[size];
        }
    };
    private Rational mAspectRatio;
    private Rect mSourceRectHint;
    private Rect mSourceRectHintInsets;
    private List<RemoteAction> mUserActions;

    public static class Builder {
        private Rational mAspectRatio;
        private Rect mSourceRectHint;
        private List<RemoteAction> mUserActions;

        public Builder setAspectRatio(Rational aspectRatio) {
            this.mAspectRatio = aspectRatio;
            return this;
        }

        public Builder setActions(List<RemoteAction> actions) {
            if (this.mUserActions != null) {
                this.mUserActions = null;
            }
            if (actions != null) {
                this.mUserActions = new ArrayList(actions);
            }
            return this;
        }

        public Builder setSourceRectHint(Rect launchBounds) {
            if (launchBounds == null) {
                this.mSourceRectHint = null;
            } else {
                this.mSourceRectHint = new Rect(launchBounds);
            }
            return this;
        }

        public PictureInPictureArgs build() {
            return new PictureInPictureArgs(this.mAspectRatio, this.mUserActions, this.mSourceRectHint);
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public PictureInPictureArgs() {
    }

    @Deprecated
    public PictureInPictureArgs(float aspectRatio, List<RemoteAction> actions) {
        setAspectRatio(aspectRatio);
        setActions(actions);
    }

    private PictureInPictureArgs(Parcel in) {
        if (in.readInt() != 0) {
            this.mAspectRatio = new Rational(in.readInt(), in.readInt());
        }
        if (in.readInt() != 0) {
            this.mUserActions = new ArrayList();
            in.readParcelableList(this.mUserActions, RemoteAction.class.getClassLoader());
        }
        if (in.readInt() != 0) {
            this.mSourceRectHint = Rect.CREATOR.createFromParcel(in);
        }
    }

    private PictureInPictureArgs(Rational aspectRatio, List<RemoteAction> actions, Rect sourceRectHint) {
        this.mAspectRatio = aspectRatio;
        this.mUserActions = actions;
        this.mSourceRectHint = sourceRectHint;
    }

    @UnsupportedAppUsage
    @Deprecated
    public void setAspectRatio(float aspectRatio) {
        this.mAspectRatio = new Rational((int) (1.0E9f * aspectRatio), 1000000000);
    }

    @UnsupportedAppUsage
    @Deprecated
    public void setActions(List<RemoteAction> actions) {
        if (this.mUserActions != null) {
            this.mUserActions = null;
        }
        if (actions != null) {
            this.mUserActions = new ArrayList(actions);
        }
    }

    @Deprecated
    public void setSourceRectHint(Rect launchBounds) {
        if (launchBounds == null) {
            this.mSourceRectHint = null;
        } else {
            this.mSourceRectHint = new Rect(launchBounds);
        }
    }

    public void copyOnlySet(PictureInPictureArgs otherArgs) {
        if (otherArgs.hasSetAspectRatio()) {
            this.mAspectRatio = otherArgs.mAspectRatio;
        }
        if (otherArgs.hasSetActions()) {
            this.mUserActions = otherArgs.mUserActions;
        }
        if (otherArgs.hasSourceBoundsHint()) {
            this.mSourceRectHint = new Rect(otherArgs.getSourceRectHint());
        }
    }

    public float getAspectRatio() {
        Rational rational = this.mAspectRatio;
        if (rational != null) {
            return rational.floatValue();
        }
        return 0.0f;
    }

    public Rational getAspectRatioRational() {
        return this.mAspectRatio;
    }

    public boolean hasSetAspectRatio() {
        return this.mAspectRatio != null;
    }

    public List<RemoteAction> getActions() {
        return this.mUserActions;
    }

    public boolean hasSetActions() {
        return this.mUserActions != null;
    }

    public void truncateActions(int size) {
        if (hasSetActions()) {
            List<RemoteAction> list = this.mUserActions;
            this.mUserActions = list.subList(0, Math.min(list.size(), size));
        }
    }

    @Deprecated
    public void setSourceRectHintInsets(Rect insets) {
        if (insets == null) {
            this.mSourceRectHintInsets = null;
        } else {
            this.mSourceRectHintInsets = new Rect(insets);
        }
    }

    public Rect getSourceRectHint() {
        return this.mSourceRectHint;
    }

    public Rect getSourceRectHintInsets() {
        return this.mSourceRectHintInsets;
    }

    public boolean hasSourceBoundsHint() {
        Rect rect = this.mSourceRectHint;
        return rect != null && !rect.isEmpty();
    }

    public boolean hasSourceBoundsHintInsets() {
        return this.mSourceRectHintInsets != null;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        if (this.mAspectRatio != null) {
            out.writeInt(1);
            out.writeInt(this.mAspectRatio.getNumerator());
            out.writeInt(this.mAspectRatio.getDenominator());
        } else {
            out.writeInt(0);
        }
        if (this.mUserActions != null) {
            out.writeInt(1);
            out.writeParcelableList(this.mUserActions, 0);
        } else {
            out.writeInt(0);
        }
        if (this.mSourceRectHint != null) {
            out.writeInt(1);
            this.mSourceRectHint.writeToParcel(out, 0);
            return;
        }
        out.writeInt(0);
    }

    public static PictureInPictureArgs convert(PictureInPictureParams params) {
        return new PictureInPictureArgs(params.getAspectRatioRational(), params.getActions(), params.getSourceRectHint());
    }

    public static PictureInPictureParams convert(PictureInPictureArgs args) {
        return new PictureInPictureParams(args.getAspectRatioRational(), args.getActions(), args.getSourceRectHint());
    }
}
