package android.app;

import android.graphics.Rect;
import android.hardware.camera2.params.TonemapCurve;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Rational;
import java.util.ArrayList;
import java.util.List;

public final class PictureInPictureParams implements Parcelable {
    public static final Creator<PictureInPictureParams> CREATOR = new Creator<PictureInPictureParams>() {
        public PictureInPictureParams createFromParcel(Parcel in) {
            return new PictureInPictureParams(in);
        }

        public PictureInPictureParams[] newArray(int size) {
            return new PictureInPictureParams[size];
        }
    };
    private Rational mAspectRatio;
    private Rect mSourceRectHint;
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

        public PictureInPictureParams build() {
            return new PictureInPictureParams(this.mAspectRatio, this.mUserActions, this.mSourceRectHint);
        }
    }

    PictureInPictureParams() {
    }

    PictureInPictureParams(Parcel in) {
        if (in.readInt() != 0) {
            this.mAspectRatio = new Rational(in.readInt(), in.readInt());
        }
        if (in.readInt() != 0) {
            this.mUserActions = new ArrayList();
            in.readParcelableList(this.mUserActions, RemoteAction.class.getClassLoader());
        }
        if (in.readInt() != 0) {
            this.mSourceRectHint = (Rect) Rect.CREATOR.createFromParcel(in);
        }
    }

    PictureInPictureParams(Rational aspectRatio, List<RemoteAction> actions, Rect sourceRectHint) {
        this.mAspectRatio = aspectRatio;
        this.mUserActions = actions;
        this.mSourceRectHint = sourceRectHint;
    }

    public void copyOnlySet(PictureInPictureParams otherArgs) {
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
        if (this.mAspectRatio != null) {
            return this.mAspectRatio.floatValue();
        }
        return TonemapCurve.LEVEL_BLACK;
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
            this.mUserActions = this.mUserActions.subList(0, Math.min(this.mUserActions.size(), size));
        }
    }

    public Rect getSourceRectHint() {
        return this.mSourceRectHint;
    }

    public boolean hasSourceBoundsHint() {
        return this.mSourceRectHint != null ? this.mSourceRectHint.isEmpty() ^ 1 : false;
    }

    public int describeContents() {
        return 0;
    }

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
}
