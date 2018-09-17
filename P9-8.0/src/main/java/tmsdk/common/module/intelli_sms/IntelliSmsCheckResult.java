package tmsdk.common.module.intelli_sms;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import tmsdk.common.tcc.SmsCheckerContentTypes;
import tmsdk.common.tcc.SmsCheckerSuggestions;

public final class IntelliSmsCheckResult implements Parcelable, SmsCheckerContentTypes, SmsCheckerSuggestions {
    public static final Creator<IntelliSmsCheckResult> CREATOR = new Creator<IntelliSmsCheckResult>() {
        /* renamed from: aS */
        public IntelliSmsCheckResult[] newArray(int i) {
            return new IntelliSmsCheckResult[i];
        }

        /* renamed from: g */
        public IntelliSmsCheckResult createFromParcel(Parcel parcel) {
            return new IntelliSmsCheckResult(parcel);
        }
    };
    private MMatchSysResult Ad;
    public int suggestion;

    public IntelliSmsCheckResult(int i, MMatchSysResult mMatchSysResult) {
        this.suggestion = i;
        this.Ad = mMatchSysResult;
    }

    public IntelliSmsCheckResult(Parcel parcel) {
        this.suggestion = parcel.readInt();
        this.Ad = (MMatchSysResult) parcel.readParcelable(MMatchSysResult.class.getClassLoader());
    }

    public static boolean shouldBeBlockedOrNot(IntelliSmsCheckResult intelliSmsCheckResult) {
        return intelliSmsCheckResult != null && (intelliSmsCheckResult.suggestion == 3 || intelliSmsCheckResult.suggestion == 2);
    }

    public int contentType() {
        return this.Ad != null ? this.Ad.contentType : 1;
    }

    public int describeContents() {
        return 0;
    }

    public Object getSysResult() {
        return this.Ad;
    }

    public boolean isCheatSMS() {
        return contentType() == 4;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.suggestion);
        parcel.writeParcelable(this.Ad, 0);
    }
}
