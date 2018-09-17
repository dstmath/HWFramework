package android.service.quicksettings;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

public final class Tile implements Parcelable {
    public static final Creator<Tile> CREATOR = null;
    public static final int STATE_ACTIVE = 2;
    public static final int STATE_INACTIVE = 1;
    public static final int STATE_UNAVAILABLE = 0;
    private static final String TAG = "Tile";
    private ComponentName mComponentName;
    private CharSequence mContentDescription;
    private Icon mIcon;
    private CharSequence mLabel;
    private IQSService mService;
    private int mState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.quicksettings.Tile.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.quicksettings.Tile.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.quicksettings.Tile.<clinit>():void");
    }

    public Tile(Parcel source) {
        this.mState = STATE_ACTIVE;
        readFromParcel(source);
    }

    public Tile(ComponentName componentName) {
        this.mState = STATE_ACTIVE;
        this.mComponentName = componentName;
    }

    public void setService(IQSService service) {
        this.mService = service;
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public IQSService getQsService() {
        return this.mService;
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
        return STATE_UNAVAILABLE;
    }

    public void updateTile() {
        try {
            this.mService.updateQsTile(this);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't update tile");
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mComponentName != null) {
            dest.writeByte((byte) 1);
            this.mComponentName.writeToParcel(dest, flags);
        } else {
            dest.writeByte((byte) 0);
        }
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
        if (source.readByte() != null) {
            this.mComponentName = (ComponentName) ComponentName.CREATOR.createFromParcel(source);
        } else {
            this.mComponentName = null;
        }
        if (source.readByte() != null) {
            this.mIcon = (Icon) Icon.CREATOR.createFromParcel(source);
        } else {
            this.mIcon = null;
        }
        this.mState = source.readInt();
        this.mLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.mContentDescription = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
    }
}
