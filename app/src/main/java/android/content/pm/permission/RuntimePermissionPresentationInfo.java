package android.content.pm.permission;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class RuntimePermissionPresentationInfo implements Parcelable {
    public static final Creator<RuntimePermissionPresentationInfo> CREATOR = null;
    private static final int FLAG_GRANTED = 1;
    private static final int FLAG_STANDARD = 2;
    private final int mFlags;
    private final CharSequence mLabel;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.permission.RuntimePermissionPresentationInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.permission.RuntimePermissionPresentationInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.permission.RuntimePermissionPresentationInfo.<clinit>():void");
    }

    public RuntimePermissionPresentationInfo(CharSequence label, boolean granted, boolean standard) {
        this.mLabel = label;
        int flags = 0;
        if (granted) {
            flags = FLAG_GRANTED;
        }
        if (standard) {
            flags |= FLAG_STANDARD;
        }
        this.mFlags = flags;
    }

    private RuntimePermissionPresentationInfo(Parcel parcel) {
        this.mLabel = parcel.readCharSequence();
        this.mFlags = parcel.readInt();
    }

    public boolean isGranted() {
        return (this.mFlags & FLAG_GRANTED) != 0;
    }

    public boolean isStandard() {
        return (this.mFlags & FLAG_STANDARD) != 0;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeCharSequence(this.mLabel);
        parcel.writeInt(this.mFlags);
    }
}
