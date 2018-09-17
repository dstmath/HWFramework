package android.accounts;

import android.accounts.IAccountManagerResponse.Stub;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;

public class AccountManagerResponse implements Parcelable {
    public static final Creator<AccountManagerResponse> CREATOR = new Creator<AccountManagerResponse>() {
        public AccountManagerResponse createFromParcel(Parcel source) {
            return new AccountManagerResponse(source);
        }

        public AccountManagerResponse[] newArray(int size) {
            return new AccountManagerResponse[size];
        }
    };
    private IAccountManagerResponse mResponse;

    public AccountManagerResponse(IAccountManagerResponse response) {
        this.mResponse = response;
    }

    public AccountManagerResponse(Parcel parcel) {
        this.mResponse = Stub.asInterface(parcel.readStrongBinder());
    }

    public void onResult(Bundle result) {
        try {
            this.mResponse.onResult(result);
        } catch (RemoteException e) {
        }
    }

    public void onError(int errorCode, String errorMessage) {
        try {
            this.mResponse.onError(errorCode, errorMessage);
        } catch (RemoteException e) {
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.mResponse.asBinder());
    }
}
