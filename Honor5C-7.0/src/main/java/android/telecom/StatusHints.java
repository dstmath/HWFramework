package android.telecom;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class StatusHints implements Parcelable {
    public static final Creator<StatusHints> CREATOR = null;
    private final Bundle mExtras;
    private final Icon mIcon;
    private final CharSequence mLabel;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telecom.StatusHints.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telecom.StatusHints.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telecom.StatusHints.<clinit>():void");
    }

    @Deprecated
    public StatusHints(ComponentName packageName, CharSequence label, int iconResId, Bundle extras) {
        this(label, iconResId == 0 ? null : Icon.createWithResource(packageName.getPackageName(), iconResId), extras);
    }

    public StatusHints(CharSequence label, Icon icon, Bundle extras) {
        this.mLabel = label;
        this.mIcon = icon;
        this.mExtras = extras;
    }

    @Deprecated
    public ComponentName getPackageName() {
        return new ComponentName("", "");
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    @Deprecated
    public int getIconResId() {
        return 0;
    }

    @Deprecated
    public Drawable getIcon(Context context) {
        return this.mIcon.loadDrawable(context);
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeCharSequence(this.mLabel);
        out.writeParcelable(this.mIcon, 0);
        out.writeParcelable(this.mExtras, 0);
    }

    private StatusHints(Parcel in) {
        this.mLabel = in.readCharSequence();
        this.mIcon = (Icon) in.readParcelable(getClass().getClassLoader());
        this.mExtras = (Bundle) in.readParcelable(getClass().getClassLoader());
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (other == null || !(other instanceof StatusHints)) {
            return false;
        }
        StatusHints otherHints = (StatusHints) other;
        if (Objects.equals(otherHints.getLabel(), getLabel()) && Objects.equals(otherHints.getIcon(), getIcon())) {
            z = Objects.equals(otherHints.getExtras(), getExtras());
        }
        return z;
    }

    public int hashCode() {
        return (Objects.hashCode(this.mLabel) + Objects.hashCode(this.mIcon)) + Objects.hashCode(this.mExtras);
    }
}
