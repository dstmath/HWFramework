package android.hardware.input;

import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class KeyboardLayout implements Parcelable, Comparable<KeyboardLayout> {
    public static final Creator<KeyboardLayout> CREATOR = null;
    private final String mCollection;
    private final String mDescriptor;
    private final String mLabel;
    private final LocaleList mLocales;
    private final int mPriority;
    private final int mProductId;
    private final int mVendorId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.input.KeyboardLayout.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.input.KeyboardLayout.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.input.KeyboardLayout.<clinit>():void");
    }

    public KeyboardLayout(String descriptor, String label, String collection, int priority, LocaleList locales, int vid, int pid) {
        this.mDescriptor = descriptor;
        this.mLabel = label;
        this.mCollection = collection;
        this.mPriority = priority;
        this.mLocales = locales;
        this.mVendorId = vid;
        this.mProductId = pid;
    }

    private KeyboardLayout(Parcel source) {
        this.mDescriptor = source.readString();
        this.mLabel = source.readString();
        this.mCollection = source.readString();
        this.mPriority = source.readInt();
        this.mLocales = (LocaleList) LocaleList.CREATOR.createFromParcel(source);
        this.mVendorId = source.readInt();
        this.mProductId = source.readInt();
    }

    public String getDescriptor() {
        return this.mDescriptor;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public String getCollection() {
        return this.mCollection;
    }

    public LocaleList getLocales() {
        return this.mLocales;
    }

    public int getVendorId() {
        return this.mVendorId;
    }

    public int getProductId() {
        return this.mProductId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDescriptor);
        dest.writeString(this.mLabel);
        dest.writeString(this.mCollection);
        dest.writeInt(this.mPriority);
        this.mLocales.writeToParcel(dest, 0);
        dest.writeInt(this.mVendorId);
        dest.writeInt(this.mProductId);
    }

    public int compareTo(KeyboardLayout another) {
        int result = Integer.compare(another.mPriority, this.mPriority);
        if (result == 0) {
            result = this.mLabel.compareToIgnoreCase(another.mLabel);
        }
        if (result == 0) {
            return this.mCollection.compareToIgnoreCase(another.mCollection);
        }
        return result;
    }

    public String toString() {
        if (this.mCollection.isEmpty()) {
            return this.mLabel;
        }
        return this.mLabel + " - " + this.mCollection;
    }
}
