package android.service.chooser;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ChooserTarget implements Parcelable {
    public static final Creator<ChooserTarget> CREATOR = new Creator<ChooserTarget>() {
        public ChooserTarget createFromParcel(Parcel source) {
            return new ChooserTarget(source);
        }

        public ChooserTarget[] newArray(int size) {
            return new ChooserTarget[size];
        }
    };
    private static final String TAG = "ChooserTarget";
    private ComponentName mComponentName;
    private Icon mIcon;
    private Bundle mIntentExtras;
    private float mScore;
    private CharSequence mTitle;

    public ChooserTarget(CharSequence title, Icon icon, float score, ComponentName componentName, Bundle intentExtras) {
        this.mTitle = title;
        this.mIcon = icon;
        if (score > 1.0f || score < 0.0f) {
            throw new IllegalArgumentException("Score " + score + " out of range; " + "must be between 0.0f and 1.0f");
        }
        this.mScore = score;
        this.mComponentName = componentName;
        this.mIntentExtras = intentExtras;
    }

    ChooserTarget(Parcel in) {
        this.mTitle = in.readCharSequence();
        if (in.readInt() != 0) {
            this.mIcon = (Icon) Icon.CREATOR.createFromParcel(in);
        } else {
            this.mIcon = null;
        }
        this.mScore = in.readFloat();
        this.mComponentName = ComponentName.readFromParcel(in);
        this.mIntentExtras = in.readBundle();
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public float getScore() {
        return this.mScore;
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public Bundle getIntentExtras() {
        return this.mIntentExtras;
    }

    public String toString() {
        return "ChooserTarget{" + this.mComponentName + ", " + this.mIntentExtras + ", '" + this.mTitle + "', " + this.mScore + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharSequence(this.mTitle);
        if (this.mIcon != null) {
            dest.writeInt(1);
            this.mIcon.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        dest.writeFloat(this.mScore);
        ComponentName.writeToParcel(this.mComponentName, dest);
        dest.writeBundle(this.mIntentExtras);
    }
}
