package android.telephony.cdma;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CdmaSmsCbProgramResults implements Parcelable {
    public static final Creator<CdmaSmsCbProgramResults> CREATOR = null;
    public static final int RESULT_CATEGORY_ALREADY_ADDED = 3;
    public static final int RESULT_CATEGORY_ALREADY_DELETED = 4;
    public static final int RESULT_CATEGORY_LIMIT_EXCEEDED = 2;
    public static final int RESULT_INVALID_ALERT_OPTION = 6;
    public static final int RESULT_INVALID_CATEGORY_NAME = 7;
    public static final int RESULT_INVALID_MAX_MESSAGES = 5;
    public static final int RESULT_MEMORY_LIMIT_EXCEEDED = 1;
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_UNSPECIFIED_FAILURE = 8;
    private final int mCategory;
    private final int mCategoryResult;
    private final int mLanguage;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.cdma.CdmaSmsCbProgramResults.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.cdma.CdmaSmsCbProgramResults.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.cdma.CdmaSmsCbProgramResults.<clinit>():void");
    }

    public CdmaSmsCbProgramResults(int category, int language, int categoryResult) {
        this.mCategory = category;
        this.mLanguage = language;
        this.mCategoryResult = categoryResult;
    }

    CdmaSmsCbProgramResults(Parcel in) {
        this.mCategory = in.readInt();
        this.mLanguage = in.readInt();
        this.mCategoryResult = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCategory);
        dest.writeInt(this.mLanguage);
        dest.writeInt(this.mCategoryResult);
    }

    public int getCategory() {
        return this.mCategory;
    }

    public int getLanguage() {
        return this.mLanguage;
    }

    public int getCategoryResult() {
        return this.mCategoryResult;
    }

    public String toString() {
        return "CdmaSmsCbProgramResults{category=" + this.mCategory + ", language=" + this.mLanguage + ", result=" + this.mCategoryResult + '}';
    }

    public int describeContents() {
        return RESULT_SUCCESS;
    }
}
