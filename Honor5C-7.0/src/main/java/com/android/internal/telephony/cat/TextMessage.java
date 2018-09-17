package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TextMessage implements Parcelable {
    public static final Creator<TextMessage> CREATOR = null;
    public Duration duration;
    public Bitmap icon;
    public boolean iconSelfExplanatory;
    public boolean isHighPriority;
    public boolean responseNeeded;
    public String text;
    public String title;
    public boolean userClear;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cat.TextMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cat.TextMessage.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.TextMessage.<clinit>():void");
    }

    TextMessage() {
        this.title = "";
        this.text = null;
        this.icon = null;
        this.iconSelfExplanatory = false;
        this.isHighPriority = false;
        this.responseNeeded = true;
        this.userClear = false;
        this.duration = null;
    }

    private TextMessage(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.title = "";
        this.text = null;
        this.icon = null;
        this.iconSelfExplanatory = false;
        this.isHighPriority = false;
        this.responseNeeded = true;
        this.userClear = false;
        this.duration = null;
        this.title = in.readString();
        this.text = in.readString();
        this.icon = (Bitmap) in.readParcelable(null);
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.iconSelfExplanatory = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isHighPriority = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.responseNeeded = z;
        if (in.readInt() != 1) {
            z2 = false;
        }
        this.userClear = z2;
        this.duration = (Duration) in.readParcelable(null);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.title);
        dest.writeString(this.text);
        dest.writeParcelable(this.icon, 0);
        if (this.iconSelfExplanatory) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.isHighPriority) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.responseNeeded) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.userClear) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeParcelable(this.duration, 0);
    }

    public String toString() {
        return "title=" + this.title + " text=" + this.text + " icon=" + this.icon + " iconSelfExplanatory=" + this.iconSelfExplanatory + " isHighPriority=" + this.isHighPriority + " responseNeeded=" + this.responseNeeded + " userClear=" + this.userClear + " duration=" + this.duration;
    }
}
