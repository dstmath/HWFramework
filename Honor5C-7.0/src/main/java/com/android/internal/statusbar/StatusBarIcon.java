package com.android.internal.statusbar;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.text.TextUtils;

public class StatusBarIcon implements Parcelable {
    public static final Creator<StatusBarIcon> CREATOR = null;
    public CharSequence contentDescription;
    public Icon icon;
    public int iconLevel;
    public int number;
    public String pkg;
    public UserHandle user;
    public boolean visible;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.statusbar.StatusBarIcon.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.statusbar.StatusBarIcon.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.statusbar.StatusBarIcon.<clinit>():void");
    }

    public StatusBarIcon(UserHandle user, String resPackage, Icon icon, int iconLevel, int number, CharSequence contentDescription) {
        this.visible = true;
        if (icon.getType() == 2 && TextUtils.isEmpty(icon.getResPackage())) {
            icon = Icon.createWithResource(resPackage, icon.getResId());
        }
        this.pkg = resPackage;
        this.user = user;
        this.icon = icon;
        this.iconLevel = iconLevel;
        this.number = number;
        this.contentDescription = contentDescription;
    }

    public StatusBarIcon(String iconPackage, UserHandle user, int iconId, int iconLevel, int number, CharSequence contentDescription) {
        this(user, iconPackage, Icon.createWithResource(iconPackage, iconId), iconLevel, number, contentDescription);
    }

    public String toString() {
        return "StatusBarIcon(icon=" + this.icon + (this.iconLevel != 0 ? " level=" + this.iconLevel : "") + (this.visible ? " visible" : "") + " user=" + this.user.getIdentifier() + (this.number != 0 ? " num=" + this.number : "") + " )";
    }

    public StatusBarIcon clone() {
        StatusBarIcon that = new StatusBarIcon(this.user, this.pkg, this.icon, this.iconLevel, this.number, this.contentDescription);
        that.visible = this.visible;
        return that;
    }

    public StatusBarIcon(Parcel in) {
        this.visible = true;
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        boolean z;
        this.icon = (Icon) in.readParcelable(null);
        this.pkg = in.readString();
        this.user = (UserHandle) in.readParcelable(null);
        this.iconLevel = in.readInt();
        if (in.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.visible = z;
        this.number = in.readInt();
        this.contentDescription = in.readCharSequence();
    }

    public void writeToParcel(Parcel out, int flags) {
        int i = 0;
        out.writeParcelable(this.icon, 0);
        out.writeString(this.pkg);
        out.writeParcelable(this.user, 0);
        out.writeInt(this.iconLevel);
        if (this.visible) {
            i = 1;
        }
        out.writeInt(i);
        out.writeInt(this.number);
        out.writeCharSequence(this.contentDescription);
    }

    public int describeContents() {
        return 0;
    }
}
