package android.app;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.io.PrintWriter;

public final class RemoteAction implements Parcelable {
    public static final Parcelable.Creator<RemoteAction> CREATOR = new Parcelable.Creator<RemoteAction>() {
        /* class android.app.RemoteAction.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RemoteAction createFromParcel(Parcel in) {
            return new RemoteAction(in);
        }

        @Override // android.os.Parcelable.Creator
        public RemoteAction[] newArray(int size) {
            return new RemoteAction[size];
        }
    };
    private static final String TAG = "RemoteAction";
    private final PendingIntent mActionIntent;
    private final CharSequence mContentDescription;
    private boolean mEnabled;
    private final Icon mIcon;
    private boolean mShouldShowIcon;
    private final CharSequence mTitle;

    RemoteAction(Parcel in) {
        this.mIcon = Icon.CREATOR.createFromParcel(in);
        this.mTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mContentDescription = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mActionIntent = PendingIntent.CREATOR.createFromParcel(in);
        this.mEnabled = in.readBoolean();
        this.mShouldShowIcon = in.readBoolean();
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
        this.mShouldShowIcon = true;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public void setShouldShowIcon(boolean shouldShowIcon) {
        this.mShouldShowIcon = shouldShowIcon;
    }

    public boolean shouldShowIcon() {
        return this.mShouldShowIcon;
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
        action.setShouldShowIcon(this.mShouldShowIcon);
        return action;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        this.mIcon.writeToParcel(out, 0);
        TextUtils.writeToParcel(this.mTitle, out, flags);
        TextUtils.writeToParcel(this.mContentDescription, out, flags);
        this.mActionIntent.writeToParcel(out, flags);
        out.writeBoolean(this.mEnabled);
        out.writeBoolean(this.mShouldShowIcon);
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("title=" + ((Object) this.mTitle));
        pw.print(" enabled=" + this.mEnabled);
        pw.print(" contentDescription=" + ((Object) this.mContentDescription));
        pw.print(" icon=" + this.mIcon);
        pw.print(" action=" + this.mActionIntent.getIntent());
        pw.print(" shouldShowIcon=" + this.mShouldShowIcon);
        pw.println();
    }
}
