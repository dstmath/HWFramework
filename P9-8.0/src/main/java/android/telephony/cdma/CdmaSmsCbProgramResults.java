package android.telephony.cdma;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CdmaSmsCbProgramResults implements Parcelable {
    public static final Creator<CdmaSmsCbProgramResults> CREATOR = new Creator<CdmaSmsCbProgramResults>() {
        public CdmaSmsCbProgramResults createFromParcel(Parcel in) {
            return new CdmaSmsCbProgramResults(in);
        }

        public CdmaSmsCbProgramResults[] newArray(int size) {
            return new CdmaSmsCbProgramResults[size];
        }
    };
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
        return 0;
    }
}
