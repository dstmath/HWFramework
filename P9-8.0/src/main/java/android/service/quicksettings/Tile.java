package android.service.quicksettings;

import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

public final class Tile implements Parcelable {
    public static final Creator<Tile> CREATOR = new Creator<Tile>() {
        public Tile createFromParcel(Parcel source) {
            return new Tile(source);
        }

        public Tile[] newArray(int size) {
            return new Tile[size];
        }
    };
    public static final int STATE_ACTIVE = 2;
    public static final int STATE_INACTIVE = 1;
    public static final int STATE_UNAVAILABLE = 0;
    private static final String TAG = "Tile";
    private CharSequence mContentDescription;
    private Icon mIcon;
    private CharSequence mLabel;
    private IQSService mService;
    private int mState = 2;
    private IBinder mToken;

    public Tile(Parcel source) {
        readFromParcel(source);
    }

    public void setService(IQSService service, IBinder stub) {
        this.mService = service;
        this.mToken = stub;
    }

    public int getState() {
        return this.mState;
    }

    public void setState(int state) {
        this.mState = state;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public void setIcon(Icon icon) {
        this.mIcon = icon;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public void setLabel(CharSequence label) {
        this.mLabel = label;
    }

    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    public void setContentDescription(CharSequence contentDescription) {
        this.mContentDescription = contentDescription;
    }

    public int describeContents() {
        return 0;
    }

    public void updateTile() {
        try {
            this.mService.updateQsTile(this, this.mToken);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't update tile");
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mIcon != null) {
            dest.writeByte((byte) 1);
            this.mIcon.writeToParcel(dest, flags);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeInt(this.mState);
        TextUtils.writeToParcel(this.mLabel, dest, flags);
        TextUtils.writeToParcel(this.mContentDescription, dest, flags);
    }

    private void readFromParcel(Parcel source) {
        if (source.readByte() != (byte) 0) {
            this.mIcon = (Icon) Icon.CREATOR.createFromParcel(source);
        } else {
            this.mIcon = null;
        }
        this.mState = source.readInt();
        this.mLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.mContentDescription = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
    }
}
