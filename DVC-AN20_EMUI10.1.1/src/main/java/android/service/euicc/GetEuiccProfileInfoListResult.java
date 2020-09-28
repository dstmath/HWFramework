package android.service.euicc;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;
import java.util.List;

@SystemApi
public final class GetEuiccProfileInfoListResult implements Parcelable {
    public static final Parcelable.Creator<GetEuiccProfileInfoListResult> CREATOR = new Parcelable.Creator<GetEuiccProfileInfoListResult>() {
        /* class android.service.euicc.GetEuiccProfileInfoListResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GetEuiccProfileInfoListResult createFromParcel(Parcel in) {
            return new GetEuiccProfileInfoListResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public GetEuiccProfileInfoListResult[] newArray(int size) {
            return new GetEuiccProfileInfoListResult[size];
        }
    };
    private final boolean mIsRemovable;
    private final EuiccProfileInfo[] mProfiles;
    @Deprecated
    public final int result;

    public int getResult() {
        return this.result;
    }

    public List<EuiccProfileInfo> getProfiles() {
        EuiccProfileInfo[] euiccProfileInfoArr = this.mProfiles;
        if (euiccProfileInfoArr == null) {
            return null;
        }
        return Arrays.asList(euiccProfileInfoArr);
    }

    public boolean getIsRemovable() {
        return this.mIsRemovable;
    }

    public GetEuiccProfileInfoListResult(int result2, EuiccProfileInfo[] profiles, boolean isRemovable) {
        this.result = result2;
        this.mIsRemovable = isRemovable;
        if (this.result == 0) {
            this.mProfiles = profiles;
        } else if (profiles == null || profiles.length <= 0) {
            this.mProfiles = null;
        } else {
            throw new IllegalArgumentException("Error result with non-empty profiles: " + result2);
        }
    }

    private GetEuiccProfileInfoListResult(Parcel in) {
        this.result = in.readInt();
        this.mProfiles = (EuiccProfileInfo[]) in.createTypedArray(EuiccProfileInfo.CREATOR);
        this.mIsRemovable = in.readBoolean();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.result);
        dest.writeTypedArray(this.mProfiles, flags);
        dest.writeBoolean(this.mIsRemovable);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
