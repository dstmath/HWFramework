package android.app;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.io.PrintWriter;

public final class RemoteAction implements Parcelable {
    public static final Creator<RemoteAction> CREATOR = new Creator<RemoteAction>() {
        public RemoteAction createFromParcel(Parcel in) {
            return new RemoteAction(in);
        }

        public RemoteAction[] newArray(int size) {
            return new RemoteAction[size];
        }
    };
    private static final String TAG = "RemoteAction";
    private final PendingIntent mActionIntent;
    private final CharSequence mContentDescription;
    private boolean mEnabled;
    private final Icon mIcon;
    private final CharSequence mTitle;

    RemoteAction(Parcel in) {
        this.mIcon = (Icon) Icon.CREATOR.createFromParcel(in);
        this.mTitle = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mContentDescription = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mActionIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(in);
        this.mEnabled = in.readBoolean();
    }

    public RemoteAction(Icon icon, CharSequence title, CharSequence contentDescription, PendingIntent intent) {
        if (icon == null || title == null || contentDescription == null || intent == null) {
            throw new IllegalArgumentException("Expected icon, title, content description and action callback");
        }
        this.mIcon = icon;
        this.mTitle = title;
        this.mContentDescription = contentDescription;
        this.mActionIntent = intent;
        this.mEnabled = true;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    public PendingIntent getActionIntent() {
        return this.mActionIntent;
    }

    public RemoteAction clone() {
        RemoteAction action = new RemoteAction(this.mIcon, this.mTitle, this.mContentDescription, this.mActionIntent);
        action.setEnabled(this.mEnabled);
        return action;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        this.mIcon.writeToParcel(out, 0);
        TextUtils.writeToParcel(this.mTitle, out, flags);
        TextUtils.writeToParcel(this.mContentDescription, out, flags);
        this.mActionIntent.writeToParcel(out, flags);
        out.writeBoolean(this.mEnabled);
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("title=" + this.mTitle);
        pw.print(" enabled=" + this.mEnabled);
        pw.print(" contentDescription=" + this.mContentDescription);
        pw.print(" icon=" + this.mIcon);
        pw.print(" action=" + this.mActionIntent.getIntent());
        pw.println();
    }
}
