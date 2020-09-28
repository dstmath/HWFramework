package android.app.prediction;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SystemApi
public final class AppTargetEvent implements Parcelable {
    public static final int ACTION_DISMISS = 2;
    public static final int ACTION_LAUNCH = 1;
    public static final int ACTION_PIN = 3;
    public static final Parcelable.Creator<AppTargetEvent> CREATOR = new Parcelable.Creator<AppTargetEvent>() {
        /* class android.app.prediction.AppTargetEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppTargetEvent createFromParcel(Parcel parcel) {
            return new AppTargetEvent(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AppTargetEvent[] newArray(int size) {
            return new AppTargetEvent[size];
        }
    };
    private final int mAction;
    private final String mLocation;
    private final AppTarget mTarget;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
    }

    private AppTargetEvent(AppTarget target, String location, int actionType) {
        this.mTarget = target;
        this.mLocation = location;
        this.mAction = actionType;
    }

    private AppTargetEvent(Parcel parcel) {
        this.mTarget = (AppTarget) parcel.readParcelable(null);
        this.mLocation = parcel.readString();
        this.mAction = parcel.readInt();
    }

    public AppTarget getTarget() {
        return this.mTarget;
    }

    public String getLaunchLocation() {
        return this.mLocation;
    }

    public int getAction() {
        return this.mAction;
    }

    public boolean equals(Object o) {
        if (!getClass().equals(o != null ? o.getClass() : null)) {
            return false;
        }
        AppTargetEvent other = (AppTargetEvent) o;
        if (!this.mTarget.equals(other.mTarget) || !this.mLocation.equals(other.mLocation) || this.mAction != other.mAction) {
            return false;
        }
        return true;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mTarget, 0);
        dest.writeString(this.mLocation);
        dest.writeInt(this.mAction);
    }

    @SystemApi
    public static final class Builder {
        private int mAction;
        private String mLocation;
        private AppTarget mTarget;

        public Builder(AppTarget target, int actionType) {
            this.mTarget = target;
            this.mAction = actionType;
        }

        public Builder setLaunchLocation(String location) {
            this.mLocation = location;
            return this;
        }

        public AppTargetEvent build() {
            return new AppTargetEvent(this.mTarget, this.mLocation, this.mAction);
        }
    }
}
