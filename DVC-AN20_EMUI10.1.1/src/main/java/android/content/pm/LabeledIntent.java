package android.content.pm;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class LabeledIntent extends Intent {
    public static final Parcelable.Creator<LabeledIntent> CREATOR = new Parcelable.Creator<LabeledIntent>() {
        /* class android.content.pm.LabeledIntent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LabeledIntent createFromParcel(Parcel source) {
            return new LabeledIntent(source);
        }

        @Override // android.os.Parcelable.Creator
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
        String str;
        CharSequence label;
        CharSequence charSequence = this.mNonLocalizedLabel;
        if (charSequence != null) {
            return charSequence;
        }
        int i = this.mLabelRes;
        if (i == 0 || (str = this.mSourcePackage) == null || (label = pm.getText(str, i, null)) == null) {
            return null;
        }
        return label;
    }

    public Drawable loadIcon(PackageManager pm) {
        String str;
        Drawable icon;
        int i = this.mIcon;
        if (i == 0 || (str = this.mSourcePackage) == null || (icon = pm.getDrawable(str, i, null)) == null) {
            return null;
        }
        return icon;
    }

    @Override // android.os.Parcelable, android.content.Intent
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

    @Override // android.content.Intent
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        this.mSourcePackage = in.readString();
        this.mLabelRes = in.readInt();
        this.mNonLocalizedLabel = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mIcon = in.readInt();
    }
}
