package android.view.textservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class SuggestionsInfo implements Parcelable {
    public static final Creator<SuggestionsInfo> CREATOR = null;
    private static final String[] EMPTY = null;
    public static final int RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS = 4;
    public static final int RESULT_ATTR_IN_THE_DICTIONARY = 1;
    public static final int RESULT_ATTR_LOOKS_LIKE_TYPO = 2;
    private int mCookie;
    private int mSequence;
    private final String[] mSuggestions;
    private final int mSuggestionsAttributes;
    private final boolean mSuggestionsAvailable;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.textservice.SuggestionsInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.textservice.SuggestionsInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.SuggestionsInfo.<clinit>():void");
    }

    public SuggestionsInfo(int suggestionsAttributes, String[] suggestions) {
        this(suggestionsAttributes, suggestions, 0, 0);
    }

    public SuggestionsInfo(int suggestionsAttributes, String[] suggestions, int cookie, int sequence) {
        if (suggestions == null) {
            this.mSuggestions = EMPTY;
            this.mSuggestionsAvailable = false;
        } else {
            this.mSuggestions = suggestions;
            this.mSuggestionsAvailable = true;
        }
        this.mSuggestionsAttributes = suggestionsAttributes;
        this.mCookie = cookie;
        this.mSequence = sequence;
    }

    public SuggestionsInfo(Parcel source) {
        boolean z = true;
        this.mSuggestionsAttributes = source.readInt();
        this.mSuggestions = source.readStringArray();
        this.mCookie = source.readInt();
        this.mSequence = source.readInt();
        if (source.readInt() != RESULT_ATTR_IN_THE_DICTIONARY) {
            z = false;
        }
        this.mSuggestionsAvailable = z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        dest.writeInt(this.mSuggestionsAttributes);
        dest.writeStringArray(this.mSuggestions);
        dest.writeInt(this.mCookie);
        dest.writeInt(this.mSequence);
        if (this.mSuggestionsAvailable) {
            i = RESULT_ATTR_IN_THE_DICTIONARY;
        } else {
            i = 0;
        }
        dest.writeInt(i);
    }

    public void setCookieAndSequence(int cookie, int sequence) {
        this.mCookie = cookie;
        this.mSequence = sequence;
    }

    public int getCookie() {
        return this.mCookie;
    }

    public int getSequence() {
        return this.mSequence;
    }

    public int getSuggestionsAttributes() {
        return this.mSuggestionsAttributes;
    }

    public int getSuggestionsCount() {
        if (this.mSuggestionsAvailable) {
            return this.mSuggestions.length;
        }
        return -1;
    }

    public String getSuggestionAt(int i) {
        return this.mSuggestions[i];
    }

    public int describeContents() {
        return 0;
    }
}
