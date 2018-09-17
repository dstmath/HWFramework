package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Input implements Parcelable {
    public static final Creator<Input> CREATOR = null;
    public String defaultText;
    public boolean digitOnly;
    public Duration duration;
    public boolean echo;
    public boolean helpAvailable;
    public Bitmap icon;
    public int maxLen;
    public int minLen;
    public boolean packed;
    public String text;
    public boolean ucs2;
    public boolean yesNo;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cat.Input.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cat.Input.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.Input.<clinit>():void");
    }

    Input() {
        this.text = "";
        this.defaultText = null;
        this.icon = null;
        this.minLen = 0;
        this.maxLen = 1;
        this.ucs2 = false;
        this.packed = false;
        this.digitOnly = false;
        this.echo = false;
        this.yesNo = false;
        this.helpAvailable = false;
        this.duration = null;
    }

    private Input(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.text = in.readString();
        this.defaultText = in.readString();
        this.icon = (Bitmap) in.readParcelable(null);
        this.minLen = in.readInt();
        this.maxLen = in.readInt();
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.ucs2 = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.packed = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.digitOnly = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.echo = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.yesNo = z;
        if (in.readInt() != 1) {
            z2 = false;
        }
        this.helpAvailable = z2;
        this.duration = (Duration) in.readParcelable(null);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.text);
        dest.writeString(this.defaultText);
        dest.writeParcelable(this.icon, 0);
        dest.writeInt(this.minLen);
        dest.writeInt(this.maxLen);
        if (this.ucs2) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.packed) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.digitOnly) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.echo) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.yesNo) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.helpAvailable) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeParcelable(this.duration, 0);
    }

    boolean setIcon(Bitmap Icon) {
        return true;
    }
}
