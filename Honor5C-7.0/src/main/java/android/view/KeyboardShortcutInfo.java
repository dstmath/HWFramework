package android.view;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;

public final class KeyboardShortcutInfo implements Parcelable {
    public static final Creator<KeyboardShortcutInfo> CREATOR = null;
    private final char mBaseCharacter;
    private final Icon mIcon;
    private final int mKeycode;
    private final CharSequence mLabel;
    private final int mModifiers;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.KeyboardShortcutInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.KeyboardShortcutInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.KeyboardShortcutInfo.<clinit>():void");
    }

    public KeyboardShortcutInfo(CharSequence label, Icon icon, int keycode, int modifiers) {
        boolean z = false;
        this.mLabel = label;
        this.mIcon = icon;
        this.mBaseCharacter = '\u0000';
        if (keycode >= 0 && keycode <= KeyEvent.getMaxKeyCode()) {
            z = true;
        }
        Preconditions.checkArgument(z);
        this.mKeycode = keycode;
        this.mModifiers = modifiers;
    }

    public KeyboardShortcutInfo(CharSequence label, int keycode, int modifiers) {
        this(label, null, keycode, modifiers);
    }

    public KeyboardShortcutInfo(CharSequence label, char baseCharacter, int modifiers) {
        boolean z;
        this.mLabel = label;
        if (baseCharacter != '\u0000') {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z);
        this.mBaseCharacter = baseCharacter;
        this.mKeycode = 0;
        this.mModifiers = modifiers;
        this.mIcon = null;
    }

    private KeyboardShortcutInfo(Parcel source) {
        this.mLabel = source.readCharSequence();
        this.mIcon = (Icon) source.readParcelable(null);
        this.mBaseCharacter = (char) source.readInt();
        this.mKeycode = source.readInt();
        this.mModifiers = source.readInt();
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public int getKeycode() {
        return this.mKeycode;
    }

    public char getBaseCharacter() {
        return this.mBaseCharacter;
    }

    public int getModifiers() {
        return this.mModifiers;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharSequence(this.mLabel);
        dest.writeParcelable(this.mIcon, 0);
        dest.writeInt(this.mBaseCharacter);
        dest.writeInt(this.mKeycode);
        dest.writeInt(this.mModifiers);
    }
}
