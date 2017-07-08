package android.view.textservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.SpellCheckSpan;

public final class TextInfo implements Parcelable {
    public static final Creator<TextInfo> CREATOR = null;
    private static final int DEFAULT_COOKIE = 0;
    private static final int DEFAULT_SEQUENCE_NUMBER = 0;
    private final CharSequence mCharSequence;
    private final int mCookie;
    private final int mSequenceNumber;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.textservice.TextInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.textservice.TextInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.TextInfo.<clinit>():void");
    }

    public TextInfo(String text) {
        this(text, 0, getStringLengthOrZero(text), 0, 0);
    }

    public TextInfo(String text, int cookie, int sequenceNumber) {
        this(text, 0, getStringLengthOrZero(text), cookie, sequenceNumber);
    }

    private static int getStringLengthOrZero(String text) {
        return TextUtils.isEmpty(text) ? 0 : text.length();
    }

    public TextInfo(CharSequence charSequence, int start, int end, int cookie, int sequenceNumber) {
        if (TextUtils.isEmpty(charSequence)) {
            throw new IllegalArgumentException("charSequence is empty");
        }
        SpannableStringBuilder spannableString = new SpannableStringBuilder(charSequence, start, end);
        SpellCheckSpan[] spans = (SpellCheckSpan[]) spannableString.getSpans(0, spannableString.length(), SpellCheckSpan.class);
        for (Object removeSpan : spans) {
            spannableString.removeSpan(removeSpan);
        }
        this.mCharSequence = spannableString;
        this.mCookie = cookie;
        this.mSequenceNumber = sequenceNumber;
    }

    public TextInfo(Parcel source) {
        this.mCharSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.mCookie = source.readInt();
        this.mSequenceNumber = source.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.mCharSequence, dest, flags);
        dest.writeInt(this.mCookie);
        dest.writeInt(this.mSequenceNumber);
    }

    public String getText() {
        if (this.mCharSequence == null) {
            return null;
        }
        return this.mCharSequence.toString();
    }

    public CharSequence getCharSequence() {
        return this.mCharSequence;
    }

    public int getCookie() {
        return this.mCookie;
    }

    public int getSequence() {
        return this.mSequenceNumber;
    }

    public int describeContents() {
        return 0;
    }
}
