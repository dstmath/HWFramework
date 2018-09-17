package android.content.pm;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public class LabeledIntent extends Intent {
    public static final Creator<LabeledIntent> CREATOR = new Creator<LabeledIntent>() {
        public LabeledIntent createFromParcel(Parcel source) {
            return new LabeledIntent(source);
        }

        public LabeledIntent[] newArray(int size) {
            return new LabeledIntent[size];
        }
    };
    private int mIcon;
    private int mLabelRes;
    private CharSequence mNonLocalizedLabel;
    private String mSourcePackage;

    public LabeledIntent(Intent origIntent, String sourcePackage, int labelRes, int icon) {
        super(origIntent);
        this.mSourcePackage = sourcePackage;
        this.mLabelRes = labelRes;
        this.mNonLocalizedLabel = null;
        this.mIcon = icon;
    }

    public LabeledIntent(Intent origIntent, String sourcePackage, CharSequence nonLocalizedLabel, int icon) {
        super(origIntent);
        this.mSourcePackage = sourcePackage;
        this.mLabelRes = 0;
        this.mNonLocalizedLabel = nonLocalizedLabel;
        this.mIcon = icon;
    }

    public LabeledIntent(String sourcePackage, int labelRes, int icon) {
        this.mSourcePackage = sourcePackage;
        this.mLabelRes = labelRes;
        this.mNonLocalizedLabel = null;
        this.mIcon = icon;
    }

    public LabeledIntent(String sourcePackage, CharSequence nonLocalizedLabel, int icon) {
        this.mSourcePackage = sourcePackage;
        this.mLabelRes = 0;
        this.mNonLocalizedLabel = nonLocalizedLabel;
        this.mIcon = icon;
    }

    public String getSourcePackage() {
        return this.mSourcePackage;
    }

    public int getLabelResource() {
        return this.mLabelRes;
    }

    public CharSequence getNonLocalizedLabel() {
        return this.mNonLocalizedLabel;
    }

    public int getIconResource() {
        return this.mIcon;
    }

    public CharSequence loadLabel(PackageManager pm) {
        if (this.mNonLocalizedLabel != null) {
            return this.mNonLocalizedLabel;
        }
        if (!(this.mLabelRes == 0 || this.mSourcePackage == null)) {
            CharSequence label = pm.getText(this.mSourcePackage, this.mLabelRes, null);
            if (label != null) {
                return label;
            }
        }
        return null;
    }

    public Drawable loadIcon(PackageManager pm) {
        if (!(this.mIcon == 0 || this.mSourcePackage == null)) {
            Drawable icon = pm.getDrawable(this.mSourcePackage, this.mIcon, null);
            if (icon != null) {
                return icon;
            }
        }
        return null;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(this.mSourcePackage);
        dest.writeInt(this.mLabelRes);
        TextUtils.writeToParcel(this.mNonLocalizedLabel, dest, parcelableFlags);
        dest.writeInt(this.mIcon);
    }

    protected LabeledIntent(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        this.mSourcePackage = in.readString();
        this.mLabelRes = in.readInt();
        this.mNonLocalizedLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mIcon = in.readInt();
    }
}
