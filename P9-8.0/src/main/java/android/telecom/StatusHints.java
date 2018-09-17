package android.telecom;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.LogException;
import java.util.Objects;

public final class StatusHints implements Parcelable {
    public static final Creator<StatusHints> CREATOR = new Creator<StatusHints>() {
        public StatusHints createFromParcel(Parcel in) {
            return new StatusHints(in, null);
        }

        public StatusHints[] newArray(int size) {
            return new StatusHints[size];
        }
    };
    private final Bundle mExtras;
    private final Icon mIcon;
    private final CharSequence mLabel;

    /* synthetic */ StatusHints(Parcel in, StatusHints -this1) {
        this(in);
    }

    @Deprecated
    public StatusHints(ComponentName packageName, CharSequence label, int iconResId, Bundle extras) {
        this(label, iconResId == 0 ? null : Icon.createWithResource(packageName.getPackageName(), iconResId), extras);
    }

    public StatusHints(CharSequence label, Icon icon, Bundle extras) {
        this.mLabel = label;
        this.mIcon = icon;
        this.mExtras = extras;
    }

    @Deprecated
    public ComponentName getPackageName() {
        return new ComponentName(LogException.NO_VALUE, LogException.NO_VALUE);
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    @Deprecated
    public int getIconResId() {
        return 0;
    }

    @Deprecated
    public Drawable getIcon(Context context) {
        return this.mIcon.loadDrawable(context);
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeCharSequence(this.mLabel);
        out.writeParcelable(this.mIcon, 0);
        out.writeParcelable(this.mExtras, 0);
    }

    private StatusHints(Parcel in) {
        this.mLabel = in.readCharSequence();
        this.mIcon = (Icon) in.readParcelable(getClass().getClassLoader());
        this.mExtras = (Bundle) in.readParcelable(getClass().getClassLoader());
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (other == null || !(other instanceof StatusHints)) {
            return false;
        }
        StatusHints otherHints = (StatusHints) other;
        if (Objects.equals(otherHints.getLabel(), getLabel()) && Objects.equals(otherHints.getIcon(), getIcon())) {
            z = Objects.equals(otherHints.getExtras(), getExtras());
        }
        return z;
    }

    public int hashCode() {
        return (Objects.hashCode(this.mLabel) + Objects.hashCode(this.mIcon)) + Objects.hashCode(this.mExtras);
    }
}
