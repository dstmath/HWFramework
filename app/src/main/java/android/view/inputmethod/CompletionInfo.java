package android.view.inputmethod;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public final class CompletionInfo implements Parcelable {
    public static final Creator<CompletionInfo> CREATOR = null;
    private final long mId;
    private final CharSequence mLabel;
    private final int mPosition;
    private final CharSequence mText;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.inputmethod.CompletionInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.inputmethod.CompletionInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.CompletionInfo.<clinit>():void");
    }

    public CompletionInfo(long id, int index, CharSequence text) {
        this.mId = id;
        this.mPosition = index;
        this.mText = text;
        this.mLabel = null;
    }

    public CompletionInfo(long id, int index, CharSequence text, CharSequence label) {
        this.mId = id;
        this.mPosition = index;
        this.mText = text;
        this.mLabel = label;
    }

    private CompletionInfo(Parcel source) {
        this.mId = source.readLong();
        this.mPosition = source.readInt();
        this.mText = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.mLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
    }

    public long getId() {
        return this.mId;
    }

    public int getPosition() {
        return this.mPosition;
    }

    public CharSequence getText() {
        return this.mText;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public String toString() {
        return "CompletionInfo{#" + this.mPosition + " \"" + this.mText + "\" id=" + this.mId + " label=" + this.mLabel + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeInt(this.mPosition);
        TextUtils.writeToParcel(this.mText, dest, flags);
        TextUtils.writeToParcel(this.mLabel, dest, flags);
    }

    public int describeContents() {
        return 0;
    }
}
