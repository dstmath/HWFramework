package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class Menu implements Parcelable {
    public static final Creator<Menu> CREATOR = null;
    public int defaultItem;
    public boolean helpAvailable;
    public List<Item> items;
    public boolean itemsIconSelfExplanatory;
    public PresentationType presentationType;
    public boolean softKeyPreferred;
    public String title;
    public List<TextAttribute> titleAttrs;
    public Bitmap titleIcon;
    public boolean titleIconSelfExplanatory;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cat.Menu.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cat.Menu.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.Menu.<clinit>():void");
    }

    public Menu() {
        this.items = new ArrayList();
        this.title = null;
        this.titleAttrs = null;
        this.defaultItem = 0;
        this.softKeyPreferred = false;
        this.helpAvailable = false;
        this.titleIconSelfExplanatory = false;
        this.itemsIconSelfExplanatory = false;
        this.titleIcon = null;
        this.presentationType = PresentationType.NAVIGATION_OPTIONS;
    }

    private Menu(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.title = in.readString();
        this.titleIcon = (Bitmap) in.readParcelable(null);
        this.items = new ArrayList();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            this.items.add((Item) in.readParcelable(null));
        }
        this.defaultItem = in.readInt();
        this.softKeyPreferred = in.readInt() == 1;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.helpAvailable = z;
        if (in.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.titleIconSelfExplanatory = z;
        if (in.readInt() != 1) {
            z2 = false;
        }
        this.itemsIconSelfExplanatory = z2;
        this.presentationType = PresentationType.values()[in.readInt()];
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.title);
        dest.writeParcelable(this.titleIcon, flags);
        int size = this.items.size();
        dest.writeInt(size);
        for (int i3 = 0; i3 < size; i3++) {
            dest.writeParcelable((Parcelable) this.items.get(i3), flags);
        }
        dest.writeInt(this.defaultItem);
        if (this.softKeyPreferred) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.helpAvailable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.titleIconSelfExplanatory) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.itemsIconSelfExplanatory) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.presentationType.ordinal());
    }
}
