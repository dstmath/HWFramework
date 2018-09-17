package android.view.textservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public final class SentenceSuggestionsInfo implements Parcelable {
    public static final Creator<SentenceSuggestionsInfo> CREATOR = null;
    private final int[] mLengths;
    private final int[] mOffsets;
    private final SuggestionsInfo[] mSuggestionsInfos;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.textservice.SentenceSuggestionsInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.textservice.SentenceSuggestionsInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.textservice.SentenceSuggestionsInfo.<clinit>():void");
    }

    public SentenceSuggestionsInfo(SuggestionsInfo[] suggestionsInfos, int[] offsets, int[] lengths) {
        if (suggestionsInfos == null || offsets == null || lengths == null) {
            throw new NullPointerException();
        } else if (suggestionsInfos.length == offsets.length && offsets.length == lengths.length) {
            int infoSize = suggestionsInfos.length;
            this.mSuggestionsInfos = (SuggestionsInfo[]) Arrays.copyOf(suggestionsInfos, infoSize);
            this.mOffsets = Arrays.copyOf(offsets, infoSize);
            this.mLengths = Arrays.copyOf(lengths, infoSize);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public SentenceSuggestionsInfo(Parcel source) {
        this.mSuggestionsInfos = new SuggestionsInfo[source.readInt()];
        source.readTypedArray(this.mSuggestionsInfos, SuggestionsInfo.CREATOR);
        this.mOffsets = new int[this.mSuggestionsInfos.length];
        source.readIntArray(this.mOffsets);
        this.mLengths = new int[this.mSuggestionsInfos.length];
        source.readIntArray(this.mLengths);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSuggestionsInfos.length);
        dest.writeTypedArray(this.mSuggestionsInfos, 0);
        dest.writeIntArray(this.mOffsets);
        dest.writeIntArray(this.mLengths);
    }

    public int describeContents() {
        return 0;
    }

    public int getSuggestionsCount() {
        return this.mSuggestionsInfos.length;
    }

    public SuggestionsInfo getSuggestionsInfoAt(int i) {
        if (i < 0 || i >= this.mSuggestionsInfos.length) {
            return null;
        }
        return this.mSuggestionsInfos[i];
    }

    public int getOffsetAt(int i) {
        if (i < 0 || i >= this.mOffsets.length) {
            return -1;
        }
        return this.mOffsets[i];
    }

    public int getLengthAt(int i) {
        if (i < 0 || i >= this.mLengths.length) {
            return -1;
        }
        return this.mLengths[i];
    }
}
