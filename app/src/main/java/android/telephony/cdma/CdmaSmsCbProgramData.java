package android.telephony.cdma;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CdmaSmsCbProgramData implements Parcelable {
    public static final int ALERT_OPTION_DEFAULT_ALERT = 1;
    public static final int ALERT_OPTION_HIGH_PRIORITY_ONCE = 10;
    public static final int ALERT_OPTION_HIGH_PRIORITY_REPEAT = 11;
    public static final int ALERT_OPTION_LOW_PRIORITY_ONCE = 6;
    public static final int ALERT_OPTION_LOW_PRIORITY_REPEAT = 7;
    public static final int ALERT_OPTION_MED_PRIORITY_ONCE = 8;
    public static final int ALERT_OPTION_MED_PRIORITY_REPEAT = 9;
    public static final int ALERT_OPTION_NO_ALERT = 0;
    public static final int ALERT_OPTION_VIBRATE_ONCE = 2;
    public static final int ALERT_OPTION_VIBRATE_REPEAT = 3;
    public static final int ALERT_OPTION_VISUAL_ONCE = 4;
    public static final int ALERT_OPTION_VISUAL_REPEAT = 5;
    public static final Creator<CdmaSmsCbProgramData> CREATOR = null;
    public static final int OPERATION_ADD_CATEGORY = 1;
    public static final int OPERATION_CLEAR_CATEGORIES = 2;
    public static final int OPERATION_DELETE_CATEGORY = 0;
    private final int mAlertOption;
    private final int mCategory;
    private final String mCategoryName;
    private final int mLanguage;
    private final int mMaxMessages;
    private final int mOperation;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.cdma.CdmaSmsCbProgramData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.cdma.CdmaSmsCbProgramData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.cdma.CdmaSmsCbProgramData.<clinit>():void");
    }

    public CdmaSmsCbProgramData(int operation, int category, int language, int maxMessages, int alertOption, String categoryName) {
        this.mOperation = operation;
        this.mCategory = category;
        this.mLanguage = language;
        this.mMaxMessages = maxMessages;
        this.mAlertOption = alertOption;
        this.mCategoryName = categoryName;
    }

    CdmaSmsCbProgramData(Parcel in) {
        this.mOperation = in.readInt();
        this.mCategory = in.readInt();
        this.mLanguage = in.readInt();
        this.mMaxMessages = in.readInt();
        this.mAlertOption = in.readInt();
        this.mCategoryName = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mOperation);
        dest.writeInt(this.mCategory);
        dest.writeInt(this.mLanguage);
        dest.writeInt(this.mMaxMessages);
        dest.writeInt(this.mAlertOption);
        dest.writeString(this.mCategoryName);
    }

    public int getOperation() {
        return this.mOperation;
    }

    public int getCategory() {
        return this.mCategory;
    }

    public int getLanguage() {
        return this.mLanguage;
    }

    public int getMaxMessages() {
        return this.mMaxMessages;
    }

    public int getAlertOption() {
        return this.mAlertOption;
    }

    public String getCategoryName() {
        return this.mCategoryName;
    }

    public String toString() {
        return "CdmaSmsCbProgramData{operation=" + this.mOperation + ", category=" + this.mCategory + ", language=" + this.mLanguage + ", max messages=" + this.mMaxMessages + ", alert option=" + this.mAlertOption + ", category name=" + this.mCategoryName + '}';
    }

    public int describeContents() {
        return ALERT_OPTION_NO_ALERT;
    }
}
